/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.BiSSELikelihoodCalculator;

import java.util.Vector;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.diverse.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.ParametersExplorer;

public class BiSSELikelihoodCalculator extends MesquiteModule implements ParametersExplorable, Evaluator {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ParametersExplorer.class, getName() + "  uses a Parameters Explorer to show likelihood surfaces.",
		"The parameter explorer is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/

	// bunch of stuff copied from zMargLikeCateg - need to prune!!


	DEQNumSolver solver;
	double [][] probsExt, probsData;
	double[] yStart;  //initial value for numerical integration
	double[] d,e; //intermediate results
	double[][] savedRootEstimates; // holds the estimates at the root (for simulation priors, etc.)
	MesquiteNumber probabilityValue;
	int numStates;

	long underflowCheckFrequency = 2;   //how often to check that not about to underflow; 1 checks every time
	long underflowCheck = 1;
	double underflowCompensation = 1;
	MesquiteNumber minChecker;
	// hooks for capturing context for table dump.
	Tree lastTree;
	CharacterDistribution lastCharDistribution;

	// Number of steps per branch, reduce for a faster, possibily sloppier result
	double stepCount = 1000; 

	//In version 1.1. the assumption about the root prior for model estimation, ancestral state reconstruction and simulation is assumed to be embedded in the model
	//Thus, the control is removed here
	public static final int ROOT_IGNOREPRIOR = 0;  // likelihoodignore's model's prior
	public static final int ROOT_USEPRIOR = 1;  // calculates ancestral states imposing model's prior
	public boolean showRootModeChoices = true;  
	//TODO: allow user choice ������������������������
	int rootMode = ROOT_USEPRIOR; //what if anything is done with prior probabilities of states at subroot?  
	StringArray rootModes;
	MesquiteString rootModeName;

	MesquiteBoolean intermediatesToConsole = new MesquiteBoolean(false);
	MesquiteBoolean getStartFromConstrainedModel = new MesquiteBoolean(false);

	boolean[] deleted = null;
	MesquiteParameter s0p;
	MesquiteParameter s1p;
	MesquiteParameter mu0p;
	MesquiteParameter mu1p;
	MesquiteParameter q01p;
	MesquiteParameter q10p;
	MesquiteParameter[] paramsForExploration;
	MesquiteParameter[] previousParams;
	ParametersExplorer explorer;
	MesquiteBoolean conditionOnSurvival;
	SpecExtincCategModel speciesModel;
	RandomBetween rng;
	int iterations = 2;
	boolean suspended = false;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		probabilityValue = new MesquiteNumber();
		minChecker = new MesquiteNumber(MesquiteDouble.unassigned);

		solver = new RKF45Solver();
		rng = new RandomBetween(System.currentTimeMillis());
		//following is for the parameters explorer
		final double def = MesquiteDouble.unassigned;
		final double inf = MesquiteDouble.infinite;
		s0p = new MesquiteParameter("lambda0", "Rate of speciation with state 0", 0.1, 0, inf, 0.000, 1);
		s1p = new MesquiteParameter("lambda1", "Rate of speciation with state 1", 0.1, 0, inf, 0.000, 1);
		mu0p = new MesquiteParameter("mu0", "Rate of extinction with state 0", 0.1, 0, inf, 0.000, 1);
		mu1p = new MesquiteParameter("mu1", "Rate of extinction with state 1", 0.1, 0, inf, 0.000, 1);
		q01p = new MesquiteParameter("q01", "Rate of 0->1 changes", 0.1, 0, inf, 0.000, 1);
		q10p = new MesquiteParameter("q10", "Rate of 1->0 changes", 0.1, 0, inf, 0.000, 1);
		if (MesquiteThread.isScripting()  && !MesquiteThread.suppressInteractionAsLibrary)
			suspended = true;
		paramsForExploration= new MesquiteParameter[]{s0p, s1p, mu0p, mu1p, q01p, q10p};
		speciesModel = new SpecExtincCategModel();
		speciesModel.setParams(paramsForExploration);

		//		parameters = MesquiteParameter.cloneArray(paramsForExploration, null);
		previousParams = new MesquiteParameter[6];
		for (int i = 0; i<6; i++)
			previousParams [i] = new MesquiteParameter();

		conditionOnSurvival = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Condition on Survival", MesquiteModule.makeCommand("conditionOnSurvival", this), conditionOnSurvival);
		addCheckMenuItem(null,"Get Start from Constrained Model", MesquiteModule.makeCommand("getStartFromConstrainedModel", this), getStartFromConstrainedModel);
		MesquiteSubmenuSpec mLO = addSubmenu(null, "Likelihood Calculation", null); 
		addItemToSubmenu(null, mLO, "Steps per Branch...", makeCommand("setStepCount", this));
		addItemToSubmenu(null, mLO, "Optimization Iterations...", makeCommand("setIterations", this));
		addItemToSubmenu(null, mLO, "Underflow Checking...", makeCommand("setUnderflowCheckFreq", this));
		addMenuSeparator();
		addMenuItem("Show Parameters Explorer", makeCommand("showParamExplorer",this));
		//addCheckMenuItem(null, "Intermediates to console", makeCommand("toggleIntermediatesToConsole",this), intermediatesToConsole);
		//		addMenuSeparator();
		rootModes = new StringArray(2);  
		rootModes.setValue(ROOT_IGNOREPRIOR, "Ignore Root State Frequencies");  //the strings passed will be the menu item labels
		rootModes.setValue(ROOT_USEPRIOR, "Use Root State Frequencies as Prior");
		rootModeName = new MesquiteString(rootModes.getValue(rootMode));  //this helps the menu keep track of checkmenuitems

		postBean("startJob", false);
		//	if (showRootModeChoices){
		/*Treatment of prior at root; currently user interface hidden unless preferences file put in place*/
		//	MesquiteSubmenuSpec mssr = addSubmenu(null, "Root Reconstruction", makeCommand("setRootMode", this), rootModes); 
		//	mssr.setSelected(rootModeName);
		/**/

		return true;
	}
	boolean reportCladeValues = false;
	public void setReportCladeLocalValues(boolean reportCladeValues){
		this.reportCladeValues = reportCladeValues;
	}
	public boolean getReportCladeLocalValues(){
		return reportCladeValues;
	}
	public void employeeQuit(MesquiteModule employee){
		if (employee == explorer)
			explorer = null;
	}
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("suspend ");
		temp.addLine("setUnderflowCheckFreq " + underflowCheckFrequency);
		temp.addLine("setStepCount " + stepCount);
		temp.addLine("setIterations " + iterations);
		temp.addLine("conditionOnSurvival  " + conditionOnSurvival.toOffOnString());
		temp.addLine("getStartFromConstrainedModel " + getStartFromConstrainedModel.toOffOnString());
		if (explorer != null)
			temp.addLine("showParamExplorer ", explorer);
		temp.addLine("resume ");
		return temp;
	}

	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets the frequency of checking for underflow", "[integer, 1 or greater]", commandName, "setUnderflowCheckFreq")) {
			int freq = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(freq) && !MesquiteThread.isScripting())
				freq = MesquiteInteger.queryInteger(containerOfModule(), "Checking frequency", "Frequency at which underflow checking is performed in likelihood calculations.  A value of n means checking is performed on each nth calculation; higher numbers mean the calculations go faster but are at risk of underflow problems.  Values over 10 are not recommended", (int)underflowCheckFrequency, 1, 10000);

			if (MesquiteInteger.isCombinable(freq) && freq >=0 && freq!=underflowCheckFrequency){
				underflowCheckFrequency = freq; //change mode
				if (!MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspended = true;
		}
		else if (checker.compare(getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspended = false;
			parametersChanged();
		}
		else if (checker.compare(getClass(), "Returns last result string", null, commandName, "getLastResultString")) {
			return lastResultString;
		}
		else if (checker.compare(getClass(), "Sets the number of steps per branch", "[double, 1 or greater]", commandName, "setStepCount")) {
			double steps = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(steps) && !MesquiteThread.isScripting())
				steps = MesquiteDouble.queryDouble(containerOfModule(), "Steps per branch", "Number of divisions of each branch for numerical integration.  Higher numbers mean the calculations are more accurate but go more slowly.  Values under 100 are not recommended", stepCount, 10, 1000000);

			if (MesquiteDouble.isCombinable(steps) && steps >=0 && steps!=stepCount){
				stepCount = steps; //change mode
				if (!MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets the number of iterations in the likelihood optimization", "[integer, 1 or greater]", commandName, "setIterations")) {
			int it = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(it) && !MesquiteThread.isScripting())
				it = MesquiteInteger.queryInteger(containerOfModule(), "Optimization Iterations", "Number of random starting points for likelihood optimizationi.  Higher numbers mean the optimization is more thorough  but goes more slowly.", iterations, 1, 1000);

			if (MesquiteInteger.isCombinable(it) && it >=0 && it!=iterations){
				iterations = it; //change mode
				if (!MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to condition by survival", "[on; off]", commandName, "conditionOnSurvival")) {
			conditionOnSurvival.toggleValue(new Parser().getFirstToken(arguments));
			if (!MesquiteThread.isScripting())parametersChanged();
		}
		else if (checker.compare(getClass(), "Shows the parameters explorer", "", commandName, "showParamExplorer")) {
			explorer = (ParametersExplorer)hireEmployee(ParametersExplorer.class, "Parameters explorer");
			if (explorer == null)
				return null;
			//	explorer.makeMenu("Parameters");
			if (!explorer.setExplorable(this)){
				explorer.iQuit();
				return null;
			}

			return explorer;
		}
		else if (checker.compare(getClass(),"Sets whether to write intermediate branch values to console","[on; off]", commandName, "toggleIntermediatesToConsole")){
			intermediatesToConsole.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(getClass(),"Sets whether to start searches for six parameter models from estimates of canonical five parameter models","[on; off]", commandName, "getStartFromConstrainedModel")){
			getStartFromConstrainedModel.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	private void initProbs(int nodes, int numStates) {
		this.numStates = numStates;
		if (yStart == null || yStart.length != numStates*2 || d == null || e == null || probsData==null || probsData.length!=nodes || probsData[0].length!=numStates){
			probsData = new double[nodes][numStates];
			probsExt = new double[nodes][numStates];
			yStart = new double[2*numStates];
			d = new double[numStates];
			e = new double[numStates];
		}
		Double2DArray.zeroArray(probsData);
		Double2DArray.zeroArray(probsExt);
	}


	/*.................................................................................................................*/
	private double checkUnderflow(double[] probs){
		minChecker.setValue(MesquiteDouble.unassigned);
		for (int i=0; i<probs.length; i++)
			minChecker.setMeIfIAmMoreThan(probs[i]);
		double q = minChecker.getDoubleValue();
		if (q == 0)
			return 0;
		else {
			for (int i=0; i<probs.length; i++)
				probs[i] /= q;
		}
		return -Math.log(q);
	}
	/*.................................................................................................................*/
	/* assess branch length variation to choose time slices */
	private double assessBranchLengths(int node, Tree tree, MesquiteDouble min, MesquiteDouble max) {
		double length = tree.getBranchLength(node, 1.0, deleted);
		min.setValue(MesquiteDouble.minimum(min.getValue(), length));
		max.setValue(MesquiteDouble.maximum(max.getValue(), length));
		for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
			length += assessBranchLengths(nd,tree,min, max);
		}
		return length;
	}


	/* assumes hard polytomies */

	// This might hold the intermediate step results if requested for debugging
	Vector integrationResults = null;

	/*.................................................................................................................*/
	/* now returns underflow compensation */
	private double downPass(int node, Tree tree, SpecExtincCategModel model, DEQNumSolver solver, CategoricalDistribution observedStates) {
		double logComp;
		int place = 0;
		try {
			if (tree == null || observedStates == null || e == null || d == null){
				MesquiteMessage.printStackTrace("ERROR: downpass in BISSELikelihoodCalculator with null object: tree " + tree + " observedStates " + observedStates + " model " + model + " d " + d + " e " + e);
				return MesquiteDouble.unassigned;
			}
			place = 1;
			if (tree.nodeIsTerminal(node)) { //initial conditions from observations if terminal
				place = 2;
				long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node));
				int obs = CategoricalState.minimum(observed); //NOTE: just minimum observed!
				for (int state = 0;state < numStates;state++){
					e[state]=0;
					if ((state == obs))
						d[state] = 1;
					else
						d[state] = 0;
				}
				place = 3;
				logComp = 0.0;  // no compensation required yet
			}
			else { //initial conditions from daughters if internal
				place = 4;
				logComp = 0.0;
				for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
					logComp += downPass(nd,tree,model,solver,observedStates);
				}
				place = 5;
				for(int state = 0; state < numStates;state++){
					d[state] = 1;  //two here to anticipate two daughters???
					e[state] = 1;
					//TODO: either filter to permit only ultrametric, or redo calculations to permit extinct nodes.
					//TODO: likewise for polytomies
					place = 6;
					for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
						e[state] = probsExt[nd][state];
						d[state] *= probsData[nd][state];
					}

					if (node != tree.getRoot()){  //condition on splitting at root; thus don't include rate if at root
						d[state] *= model.getSRate(state);
					}

				}
				place = 7;
				if (underflowCheckFrequency>=0 && ++underflowCheck % underflowCheckFrequency == 0){
					logComp += checkUnderflow(d);
				}
				place = 8;
			}
			if (node == tree.getRoot()){
				place = 9;
				for(int i=0;i<numStates;i++){
					probsExt[node][i] = e[i];
					probsData[node][i] = d[i];
				}
				place = 10;
				return logComp;
			}
			else{
				place = 11;
				double x = 0;
				double length = tree.getBranchLength(node,1.0,deleted);

				double h = getStepSize(length);  

				for(int i=0;i<numStates;i++){
					yStart[i] = e[i];
					yStart[i+numStates] = d[i];
				}
				place = 12;
				if (intermediatesToConsole.getValue()){
					MesquiteMessage.print("node " + node);
					if (node == tree.getRoot())
						MesquiteMessage.println(" is root");
					else
						MesquiteMessage.println("");
					MesquiteMessage.println("At start, y is " + DoubleArray.toString(yStart));
				}
				place = 13;
				integrationResults = solver.integrate(x,yStart,h,length,model,integrationResults,intermediatesToConsole.getValue());    
				
				double[] yEnd = null;
				if (integrationResults != null)
					yEnd = (double[])integrationResults.lastElement();
				if (yEnd != null && yEnd.length == 2*numStates){
					place = 14;
					for(int i=0;i<numStates;i++){
						probsExt[node][i] = yEnd[i];
						probsData[node][i] = yEnd[i+numStates];
					}
				}
				else {
					place = 15;
					MesquiteMessage.warnProgrammer("Vector returned by solver not the same size supplied!");
					for(int i=0;i<numStates;i++){
						probsExt[node][i]=0;   //is this the right thing here?
						probsData[node][i]=0;  //this is probably the best choice here
					}
				}
				place = 16;
				if (intermediatesToConsole.getValue()){
					place = 17;
					MesquiteMessage.println("Intermediate values");
					StringBuffer stateMsg = new StringBuffer(1000);
					//stateMsg.delete(0,stateMsg.length());  //flush everything
					x = h;
					double [] tempResults;
					place = 18;
					for(int i=0;i<integrationResults.size();i++){
						place = 19;
						if(i%100 == 0){
							String xString = MesquiteDouble.toFixedWidthString(x, 13, false);
							stateMsg.append("x= "+ xString + " y =[");
							tempResults = (double[])integrationResults.get(i);
							for(int j=0;j<tempResults.length;j++)
								stateMsg.append(MesquiteDouble.toFixedWidthString(tempResults[j],13,false)+" ");
							stateMsg.append("]\n");
							place = 20;
						}   
						x += h;
					}
					place = 21;
					stateMsg.append("Final value; \n");
					stateMsg.append("x= " + h*stepCount + " y =[");
					tempResults = (double[])integrationResults.lastElement();
					for(int j=0;j<tempResults.length;j++)
						stateMsg.append(MesquiteDouble.toFixedWidthString(tempResults[j],13,false)+" ");
					stateMsg.append("]\n");
					MesquiteMessage.println(stateMsg.toString());
					place = 22;
				}
				return logComp;
			}
		}
		catch (NullPointerException e){
			MesquiteMessage.println("ERROR: downpass in BISSELikelihoodCalculator with null object, place :" + place);
			MesquiteMessage.println("      tree " + tree + " observedStates " + observedStates + " model " + model + " d " + d + " e " + e);
			MesquiteMessage.println("      probsExt " + probsExt + " probsData " + probsData + " yStart " + yStart + " integrationResults " + integrationResults);
			throw e;
		}
	}
	/*------------------------------------------------------------------------------------------*/
	double avgBranchLength;
	double getStepSize(double length){		
		//average branch will have stepCount steps; longer branches will have proportionately more steps up to a maximum of 4*stepCount, shorter to minimum of stepCount/2
		double proposedSteps = stepCount*length/avgBranchLength;
		if (proposedSteps < stepCount/2)
			proposedSteps = stepCount/2;
		else if (proposedSteps > 4* stepCount)
			proposedSteps =4* stepCount;
		return length/proposedSteps;       //this will need tweaking!
	}
	/*------------------------------------------------------------------------------------------*/
	/** these methods for ParametersExplorable interface */
	public MesquiteParameter[] getExplorableParameters(){
		return paramsForExploration;
	}
	MesquiteNumber likelihood = new MesquiteNumber();
	public double calculate(MesquiteString resultString){
		if (suspended)
			return MesquiteDouble.unassigned;
		calculateLogProbability( lastTree,  lastCharDistribution, paramsForExploration, likelihood, resultString);
		return likelihood.getDoubleValue();
	}
	public void restoreAfterExploration(){
	}
	/*------------------------------------------------------------------------------------------*/
	int lastMaxState = 1;
	long count = 0;

	boolean anyNegative(double[] params){
		if (params == null)
			return false;
		for (int i=0; i<params.length; i++)
			if (params[i]<0)
				return true;
		return false;
	}
	int badCount = 1;
	/*.................................................................................................................*/
	public double evaluate(double[] params, Object bundle){
		if (anyNegative(params))
			return 1e100 + 1e99*badCount++;
		Object[] b = ((Object[])bundle);
		Tree tree = (Tree)b[0];
		CategoricalDistribution states = (CategoricalDistribution)b[1];
		SpecExtincCategModel model = (SpecExtincCategModel)b[2];
		model.setParamValuesUsingConstraints(params);

		double result =  logLike(tree, states, model);
		if (!MesquiteDouble.isCombinable(result) || result < -1e100 || result >= 1e100)
			result = 1e100 + 1e99*badCount++;
		if (count++ % 10 == 0)
			CommandRecord.getRecSIfNull().tick("Evaluating: -log likelihood " + MesquiteDouble.toStringDigitsSpecified(result, 4) + " params " + MesquiteParameter.toString(params));
		return result;
	}
	/*.................................................................................................................*/
	double[] oneParam = new double[1];
	/*.................................................................................................................*/
	public double evaluate(MesquiteDouble param, Object bundle){
		oneParam[0] = param.getValue();
		if (anyNegative(oneParam))
			return 1e100 + 1e99*badCount++;
		Object[] b = ((Object[])bundle);
		Tree tree = (Tree)b[0];
		CategoricalDistribution states = (CategoricalDistribution)b[1];
		SpecExtincCategModel model = (SpecExtincCategModel)b[2];
		model.setParamValuesUsingConstraints(oneParam);
		double result =  logLike(tree, states, model);
		if (!MesquiteDouble.isCombinable(result) || result < -1e100 || result > 1e100)
			result = 1e100 + 1e99*badCount++;
		if (count++ % 10 == 0)
			CommandRecord.getRecSIfNull().tick("Evaluating: -log likelihood " + MesquiteDouble.toStringDigitsSpecified(result, 4) + " param " + MesquiteParameter.toString(oneParam));
		return result;
	}
	double[] freq = new double[2];
	/*.................................................................................................................*/
	public double logLike(Tree tree, CategoricalDistribution states, SpecExtincCategModel model) {  
		if (model==null || states == null || tree == null)
			return MesquiteDouble.unassigned;
		int root = tree.getRoot(deleted);
		initProbs(tree.getNumNodeSpaces(),lastMaxState);
		double logComp = downPass(root, tree, model, solver, states);
		freq[0] = stationaryFreq0(model);
		freq[1] = 1.0 - freq[0];
		double likelihood = 0.0;
		if (rootMode == ROOT_USEPRIOR){
			if (conditionOnSurvival.getValue()){
				for (int i=0;  i<lastMaxState; i++)
					likelihood += freq[i]*probsData[root][i]/(1-probsExt[root][i])/(1-probsExt[root][i]);
			}
			else
				for (int i=0;  i<lastMaxState; i++) 
					likelihood += freq[i]*probsData[root][i];
		}
		else if (conditionOnSurvival.getValue()){
			for (int i=0;  i<lastMaxState; i++)
				likelihood += probsData[root][i]/(1-probsExt[root][i])/(1-probsExt[root][i]);
		}
		else
			for (int i=0;  i<lastMaxState; i++) 
				likelihood += probsData[root][i];
		return (-(Math.log(likelihood) - logComp));
	}
	/*.................................................................................................................*/
	public double stationaryFreq0(SpecExtincCategModel model) {  
		if (model==null)
			return MesquiteDouble.unassigned;
		final double d = model.getSRate(0)-model.getSRate(1)+model.getERate(1)-model.getERate(0);
		final double noise = (model.getSRate(0)+model.getSRate(1)+model.getERate(1)+model.getERate(0))*1E-14;
		final double q01 = model.getCRate(0);
		final double q10 = model.getCRate(1);
		if (Math.abs(d ) < noise){
			if (q01 + q10 == 0)
				return 0.5;
			return q10/(q01+q10);
		}
		double part = d - q01 - q10;
		part = part*part + 4*d*q10;
		if (part >=0)
			part = Math.sqrt(part);
		else
			return MesquiteDouble.unassigned;
		final double plus = (q01 + q10 - d + part) / (-2*d);
		final double minus = (q01 + q10 - d - part) / (-2*d);
		if (minus < 0 || minus >1)
			return plus;
		else if (plus < 0 || plus >1)
			return minus;
		else
			return MesquiteDouble.unassigned;
	}
	String lastResultString;
	/*
	 Options:
	 1. calculation determined entirely by params.  Constraints etc. determined within params array which is contained in species model
	 2. predefined: constraint lambda0-mu0 = lambda1-mu1
	 Should return estimated values in the MesquiteParameter array that was input
	/*.................................................................................................................*/
	public void calculateLogProbability(Tree tree, CharacterDistribution obsStates, MesquiteParameter[] params, MesquiteNumber prob, MesquiteString resultString) {  
		if (speciesModel==null || obsStates==null || prob == null || tree == null)
			return;
		lastTree = tree;
		lastCharDistribution = obsStates;
		boolean failed = false;
		prob.setToUnassigned();
		if (suspended)
			return;
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		int workingMaxState;
		if (tree.hasReticulations() || tree.hasUnbranchedInternals(tree.getRoot())){
			MesquiteMessage.warnProgrammer("Tree has reticulations or unbranched internal nodes; these are not allowed in BiSSE calcuclations.");
			return;
		}
		else if (observedStates.getMaxState() <= 0){
			MesquiteMessage.warnProgrammer("Character Distribution appears to be constant; cannot calculate likelihood of tree and character (Speciation/Extinction)");
			return;
		}
		else if (observedStates.getMaxState() >1){
			MesquiteMessage.warnProgrammer("Character Distribution is not binary; cannot calculate likelihood of tree and character (Speciation/Extinction)");
			return;
		}
		else
			workingMaxState = observedStates.getMaxState()+1; 
		lastMaxState = workingMaxState;
		speciesModel.getOriginalParams(previousParams);
		speciesModel.setParams(params);
		double currentStep = stepCount;

		double negLogLikelihood = 0;
		String modelString = "";
		MesquiteDouble minBranchLength = new MesquiteDouble();
		MesquiteDouble maxBranchLength = new MesquiteDouble();
		avgBranchLength = assessBranchLengths(tree.getRoot(), tree, minBranchLength, maxBranchLength)/tree.numberOfNodesInClade(tree.getRoot());
		/*to report stationary frequencies 
		if (true){
			negLogLikelihood = stationaryFreq0(speciesModel);
		}
		else 
		 */
		if (speciesModel.isFullySpecified()){
			negLogLikelihood = logLike(tree, observedStates, speciesModel);
			modelString  = speciesModel.toString();
		}
		else {	

			logln("Estimating model with parameters " + speciesModel.toStringForScript());
			//========================== all 6 parameters unspecified ==============================
			if (speciesModel.numberSpecified() ==0 && speciesModel.numberEffectiveParameters() == 6){  //all unspecified
				//some or all parameters are unassigned, and thus need to be estimated
				//First, we need to go through the parameters array to construct a mapping between our local free parameters and the original
				Optimizer opt = new Optimizer(this);
				Object[] bundle = new Object[] {tree, observedStates, speciesModel};
				stepCount = 100;
				double useIterations = iterations;
				double[] suggestions1;
				double[] suggestions2;
				double[] suggestions3;
				if (getStartFromConstrainedModel.getValue()){
					suggestions1 = estimate5(tree,observedStates,1,new double[]{0.75, 0.5, 0.2, 0.05, 0.1});  
					suggestions2 = estimate5(tree,observedStates,3,new double[]{0.5, 1, 0.35, 0.05, 0.1});
					suggestions3 = estimate5(tree,observedStates,5,new double[]{0.5,0.3,0.5,0.4,0.075});
				}
				else {
					suggestions1 = new double[]{1, 0.5, 0.5, 0.2, 0.1, 0.05};
					suggestions2 = new double[]{0.5, 1, 0.2, 0.5, 0.05, 0.1};
					suggestions3 = new double[]{0.5,0.3,0.5,0.4,0.05, 0.1};                    
				}

				double bestL = 1e101;
				double[] suggestions = null;
				int bestS = -1;
				String attemptName = "";
				double evs1 = MesquiteDouble.unassigned;
				double evs2 = MesquiteDouble.unassigned;
				double evs3 = MesquiteDouble.unassigned;
				if (suggestions1 != null)
					evs1 = evaluate(suggestions1,bundle);
				if (suggestions2 != null)
					evs2 = evaluate(suggestions2,bundle);
				if (suggestions3 != null)
					evs3 = evaluate(suggestions3,bundle);
				if (MesquiteDouble.isCombinable(evs1) && evs1 < 1e99 && MesquiteDouble.isCombinable(evs2) && evs2 < 1e99 && MesquiteDouble.isCombinable(evs3) && evs3 < 1e99){
					logln("Bisse Calculations: Tree " + tree.getName() + " and character " + obsStates.getName());
					logln("Bisse Calculations: Estimating all 6 parameters, phase 1: step count 100");
					double negLogLikelihood1 = opt.optimize(suggestions1, bundle);
					logln("Bisse Calculations: neg. Log Likelihood first attempt:" + negLogLikelihood1);
					double negLogLikelihood2 = opt.optimize(suggestions2, bundle);
					logln("Bisse Calculations: neg. Log Likelihood second attempt:" + negLogLikelihood2);
					double negLogLikelihood3 = opt.optimize(suggestions3, bundle);
					logln("Bisse Calculations: neg. Log Likelihood third attempt:" + negLogLikelihood3);
					if (negLogLikelihood1 < negLogLikelihood2 && negLogLikelihood1 < negLogLikelihood3) {
						suggestions = suggestions1;
						bestS = -1;
						bestL = negLogLikelihood1;
						attemptName = " first attempt" ;
					}
					else if (negLogLikelihood2 < negLogLikelihood1 && negLogLikelihood2 < negLogLikelihood3) {
						suggestions = suggestions2;
						bestS = -2;
						bestL = negLogLikelihood2;
						attemptName = " second attempt" ;
					}
					else {
						suggestions = suggestions3;
						bestS = -3;
						bestL = negLogLikelihood3;
						attemptName = " third attempt" ;                        
					}
				}
				else{
					useIterations = iterations + 3;
				}
				double[][] randomSuggestions = new double[(int)useIterations][6];

				for (int i = 0; i< useIterations; i++){  
					double max = 1.0;
					if (i< useIterations/2){
						max =  1.0;
						for (int k = 0; k<6; k++){
							randomSuggestions[i][k] = rng.randomDoubleBetween(0, max);
						}
						if (i % 2 == 0){ //every other one enforce e < s
							randomSuggestions[i][3] = rng.randomDoubleBetween(0, randomSuggestions[i][1] );
							randomSuggestions[i][4] = rng.randomDoubleBetween(0, randomSuggestions[i][2] );
						}
					}
					else { //0 to 1 first then 0 to 10.0
						max =  rng.randomDoubleBetween(0, 10.0);
						for (int k = 0; k<6; k++){
							randomSuggestions[i][k] = max - 0.5 + rng.randomDoubleBetween(0, 2.0);
						}
						if (i % 2 == 0){ //every other one enforce e < s
							if (randomSuggestions[i][3]> randomSuggestions[i][1])
								randomSuggestions[i][3] = rng.randomDoubleBetween(MesquiteDouble.maximum(randomSuggestions[i][1]-1.0, 0), randomSuggestions[i][1] );
							if (randomSuggestions[i][4]> randomSuggestions[i][2])
								randomSuggestions[i][4] = rng.randomDoubleBetween(MesquiteDouble.maximum(randomSuggestions[2][1]-1.0, 0), randomSuggestions[2][1] );
						}
						if (evaluate(randomSuggestions[i], bundle) > 1e99) //likelihood bad; try a 0 to 1 one instead
							for (int k = 0; k<6; k++)
								randomSuggestions[i][k] = rng.randomDoubleBetween(0, max);
					}
				}
				for (int i = 0; i< useIterations; i++){ // 0 to 10
					if (evaluate(randomSuggestions[i], bundle) < 1e99){  //don't start if it hits surface in NaN-land
						logln("BiSSE calculations: random suggestions " + i + " :" + DoubleArray.toString(randomSuggestions[i]));
						double nLL = opt.optimize(randomSuggestions[i], bundle);
						stepCount = 1000;
						nLL = evaluate(randomSuggestions[i], bundle);
						stepCount = 100;
						logln("BiSSE calculations: random attempt " + i + " neg. Log Likelihood:" + nLL + " : " + DoubleArray.toString(randomSuggestions[i]));
						if (nLL < bestL){
							bestS = i;
							bestL = nLL;
							attemptName = "random attempt " + i ;
						}
					}
					else 
						logln("BiSSE calculations: random attempt " + i + " failed because starting position had undefined likelihood");
				}
				if (bestS>=0)
					suggestions = randomSuggestions[bestS];
				if (suggestions == null){
					logln("BiSSE calculations: Estimating parameters failed");
					failed = true;
				}
				else {
					//logln("BiSSE calculations: Estimating parameters, phase 2: step count 100; best so far " + evaluate(suggestions, bundle));
					stepCount = 1000;
					logln("BiSSE calculations: Estimating parameters, phase 2: step count 1000; best so far " + evaluate(suggestions, bundle));
					logln("BiSSE calculations: Estimating parameters, phase 2: step count 1000 starting from results of preliminary " + attemptName);
					negLogLikelihood = opt.optimize(suggestions, bundle);
					logln("BiSSE calculations: neg. Log Likelihood final attempt:" + negLogLikelihood);

					speciesModel.setParamValuesUsingConstraints(suggestions);
					modelString  = speciesModel.toString() + " [est.]";
				}
			}
			//========================== 2 - 5 parameters unspecified ==============================
			else if (speciesModel.numberEffectiveParameters() > 1) {  //2 to 5 parameters unassigned; should have separate for 1!
				//some parameters are unassigned, and thus need to be estimated
				//First, we need to go through the parameters array to construct a mapping between our local free parameters and the original
				Optimizer opt = new Optimizer(this);
				Object[] bundle = new Object[] {tree, observedStates, speciesModel};
				stepCount = 100;
				int looseParameter = -1;
				int numParams = speciesModel.numberEffectiveParameters();
				double[] suggestions1 = new double[numParams];
				logln("BiSSE calculations: Tree " + tree.getName() + " and character " + obsStates.getName());
				logln("BiSSE calculations: Estimating " + numParams + " free parameters");
				for (int i=0; i < suggestions1.length; i++)
					suggestions1[i] = 0.1*(i+1);
				logln("BiSSE calculations: Estimating parameters, phase 1: step count 100");
				double bestL = MesquiteDouble.unassigned;
				int bestS = -1;

				if (getStartFromConstrainedModel.getValue() && (numParams == 4)){                    
					if (speciesModel.parameters[1].getConstrainedTo() == null)
						looseParameter = 1;
					else if (speciesModel.parameters[3].getConstrainedTo() == null)
						looseParameter = 3;
					else if (speciesModel.parameters[5].getConstrainedTo() == null)
						looseParameter = 5;
					for (int i=0; i < suggestions1.length; i++)
						suggestions1[i] = 0.1*(i+1);
					suggestions1 = estimate3(tree,observedStates,looseParameter,suggestions1);  //TODO these may need more work
					logln("Generating starting point for 4 parameter model from canonical 3 parameter model");
					logln("First random suggestion will be replaced by estimate from 3 parameter model");
				}
				else {
					for (int i=0; i < suggestions1.length; i++)
						suggestions1[i] = 0.1*(i+1);
				}

				String attemptName = "random attempt";
				double[][] randomSuggestions = new double[iterations][numParams];
				for (int i = 0; i< iterations; i++){  
					if (i< iterations/2){//0 to 1 
						for (int k = 0; k<numParams; k++){
							randomSuggestions[i][k] = rng.randomDoubleBetween(0, 1.0);
						}
					}
					else { //00 to 10.0
						double max =  rng.randomDoubleBetween(0, 10.0);
						for (int k = 0; k<numParams; k++){
							randomSuggestions[i][k] = max - 0.5 + rng.randomDoubleBetween(0, 2.0);
						}

						if (evaluate(randomSuggestions[i], bundle) > 1e99) //likelihood bad; try a 0 to 1 one instead
							for (int k = 0; k<numParams; k++)
								randomSuggestions[i][k] = rng.randomDoubleBetween(0, 1.0);
					}
				}

				if (getStartFromConstrainedModel.getValue() && (numParams == 4)){
					if (looseParameter != -1){
						logln("Setting first random suggestion to " + DoubleArray.toString(suggestions1));
						for(int i=0;i<suggestions1.length;i++)
							randomSuggestions[0][i]=suggestions1[i];
					}
				}

				int totalCount = 0;
				for (int i = 0; i< iterations; i++){ // 0 to 10
					totalCount++;
					if (evaluate(randomSuggestions[i], bundle) < 1e99){  //don't start if it hits surface in NaN-land
						logln("BiSSE calculations: random suggestions " + i + " :" + DoubleArray.toString(randomSuggestions[i]));
						double nLL = opt.optimize(randomSuggestions[i], bundle);
						stepCount = 1000;
						nLL = evaluate(randomSuggestions[i], bundle);
						stepCount = 100;
						logln("BiSSE calculations: attempt " +totalCount + " neg. Log Likelihood:" + nLL + " : " + DoubleArray.toString(randomSuggestions[i]));
						if (nLL < bestL || MesquiteDouble.isUnassigned(bestL)){
							bestS = i;
							bestL = nLL;
							attemptName = "random attempt " + i ;
						}
					}
					else {
						if (totalCount-iterations < 10){
							logln("BiSSE calculations: random attempt " + totalCount + " failed because starting position had undefined likelihood");
							i--;  //10 extra free
						}
						else {
							logln("BiSSE calculations: attempt failed because starting position had undefined likelihood");
							i = iterations;
						}
					}
				}
				if (bestS>=0){
					double[] suggestions = randomSuggestions[bestS];
					//logln("BiSSE calculations: Estimating parameters, phase 2: step count 100; best so far " + evaluate(suggestions, bundle));
					stepCount = 1000;
					logln("BiSSE calculations: Estimating parameters, phase 2: step count 1000; best so far " + evaluate(suggestions, bundle));
					logln("BiSSE calculations: Estimating parameters, phase 2: step count 1000 starting from results of preliminary " + attemptName);
					negLogLikelihood = opt.optimize(suggestions, bundle);
					logln("BiSSE calculations: neg. Log Likelihood final attempt:" + negLogLikelihood);

					speciesModel.setParamValuesUsingConstraints(suggestions);
					modelString  = speciesModel.toString() + " [est.]";
				}
				else {
					logln("BiSSE calculations: Estimating parameters failed");
					failed = true;
				}


			}
			//========================== 1 parameter unspecified ==============================
			else  {  //1 parametersunassigned;
				Optimizer opt = new Optimizer(this);
				Object[] bundle = new Object[] {tree, observedStates, speciesModel};
				stepCount = 100;

				logln("BiSSE calculations: Tree " + tree.getName() + " and character " + obsStates.getName());
				logln("BiSSE calculations: Estimating one free parameter");
				logln("BiSSE calculations: Estimating parameter, phase 1: step count 100");
				MesquiteDouble suggestion = new MesquiteDouble(1);
				negLogLikelihood = opt.optimize(suggestion, 0, 100, bundle);
				logln("BiSSE calculations: neg. Log Likelihood first attempt:" + negLogLikelihood);
				double bestL = negLogLikelihood;
				double bestP = suggestion.getValue();
				negLogLikelihood = opt.optimize(suggestion, 0.0, 10, bundle);
				logln("BiSSE calculations: neg. Log Likelihood second attempt:" + negLogLikelihood);
				if (bestL < negLogLikelihood)
					suggestion.setValue(bestP);
				else{
					bestP = suggestion.getValue();
					bestL = negLogLikelihood;
				}
				negLogLikelihood = opt.optimize(suggestion, 0.0, 1, bundle);
				logln("BiSSE calculations: neg. Log Likelihood third attempt:" + negLogLikelihood);
				if (bestL < negLogLikelihood)
					suggestion.setValue(bestP);
				else{
					bestP = suggestion.getValue();
					bestL = negLogLikelihood;
				}
				stepCount = 1000;
				logln("BiSSE calculations: Estimating parameters, phase 2: step count 1000");
				negLogLikelihood = opt.optimize(suggestion, suggestion.getValue() * 0.6, suggestion.getValue() * 1.4, bundle);
				logln("BiSSE calculations: neg. Log Likelihood final attempt:" + negLogLikelihood);

				oneParam[0] = suggestion.getValue();
				speciesModel.setParamValuesUsingConstraints(oneParam);
				modelString  = speciesModel.toString() + " [est.]";

			}
		}
		speciesModel.getCurrentParams(params);
		paramsForExploration[0].setValue(speciesModel.getSRate(0));
		paramsForExploration[1].setValue(speciesModel.getSRate(1));
		paramsForExploration[2].setValue(speciesModel.getERate(0));
		paramsForExploration[3].setValue(speciesModel.getERate(1));
		paramsForExploration[4].setValue(speciesModel.getCRate(0));
		paramsForExploration[5].setValue(speciesModel.getCRate(1));
		stepCount = currentStep;

		if (prob!=null){
			prob.setValue(negLogLikelihood);
			prob.copyAuxiliaries(params);
			prob.setName("BiSSE -lnLikelihood (prop.)");
		}
		double likelihood = Math.exp(-negLogLikelihood);
		if (MesquiteDouble.isUnassigned(negLogLikelihood))
			likelihood = MesquiteDouble.unassigned;
		if (resultString!=null) {
			if (failed){
				resultString.setValue("Estimation of BiSSE parameters failed");
				prob.deassignAllValues();
			}
			else{
				String s = "Sp/Ext Likelihood (Char. dep.) " + modelString + ";  -log L.:"+ MesquiteDouble.toString(negLogLikelihood) + " [L. "+ MesquiteDouble.toString(likelihood) + "]";
				s += "  " + getParameters();
				s += "\nNOTE: reported are not likelihoods but rather values proportional to likelihoods";
				resultString.setValue(s);
			}
		}

		lastResultString = tree.getName() + "\t" + obsStates.getName() +"\t" + speciesModel.toStringForAnalysis() + "\t" + MesquiteDouble.toString(negLogLikelihood);
		speciesModel.setParams(previousParams);
	}

	int quickIterations;
	private SpecExtincCategModel localModel5 = new SpecExtincCategModel();
	private double[] estimate5(Tree tree, CategoricalDistribution observedStates,int toConstrain,double suggestions[]){
		//some parameters are unassigned, and thus need to be estimated
		//First, we need to go through the parameters array to construct a mapping between our local free parameters and the original
		final String logPrefix = "Bisse (5 parameter starting point): ";
		quickIterations = iterations/2;
		MesquiteParameter [] localParams = new MesquiteParameter[6]; 
		for(int i= 0; i<6;i++){
			localParams[i] = new MesquiteParameter();
			if (i == toConstrain)
				localParams[i].setConstrainedTo(localParams[i-1], false);            
		}
		localModel5.setParams(localParams);
		Optimizer opt = new Optimizer(this);
		Object[] bundle = new Object[] {tree, observedStates, localModel5};
		stepCount = 100;
		int numParams = 5;
		logln(logPrefix + "constraining parameter " + toConstrain + " to generate starting point for 6 parameter estimate");
		logln(logPrefix + "Estimating parameters, phase 1: step count 100");
		double bestL = MesquiteDouble.unassigned;
		double nLL = MesquiteDouble.unassigned;
		int bestS = -2;
		if (evaluate(suggestions,bundle)<1e99){
			logln("Trying initial suggestion :" + DoubleArray.toString(suggestions));
			nLL = opt.optimize(suggestions,bundle);
			stepCount = 1000;
			nLL = evaluate(suggestions, bundle);
			stepCount = 100;
			logln(logPrefix + "initial starting point suggestion neg. Log Likelihood:" + nLL + " : " + DoubleArray.toString(suggestions));
			if (nLL < bestL || MesquiteDouble.isUnassigned(bestL)){
				bestS = -1;
				bestL = nLL;
			}
		}
		String attemptName = "random attempt";
		double[][] randomSuggestions = new double[quickIterations][numParams];
		for (int i = 0; i< quickIterations; i++){  
			if (i< quickIterations/2){//0 to 1 
				for (int k = 0; k<numParams; k++){
					randomSuggestions[i][k] = rng.randomDoubleBetween(0, 1.0);
				}
			}
			else { //00 to 10.0
				double max =  rng.randomDoubleBetween(0, 10.0);
				for (int k = 0; k<numParams; k++){
					randomSuggestions[i][k] = max - 0.5 + rng.randomDoubleBetween(0, 2.0);
				}

				if (evaluate(randomSuggestions[i], bundle) > 1e99) //likelihood bad; try a 0 to 1 one instead
					for (int k = 0; k<numParams; k++)
						randomSuggestions[i][k] = rng.randomDoubleBetween(0, 1.0);
			}
		}
		for (int i = 0; i< quickIterations; i++){ // 0 to 10
			if (evaluate(randomSuggestions[i], bundle) < 1e99){  //don't start if it hits surface in NaN-land
				nLL = opt.optimize(randomSuggestions[i], bundle);
				stepCount = 1000;
				nLL = evaluate(randomSuggestions[i], bundle);
				stepCount = 100;
				logln(logPrefix + "attempt " + i + " neg. Log Likelihood:" + nLL + " : " + DoubleArray.toString(randomSuggestions[i]));
				if (nLL < bestL || MesquiteDouble.isUnassigned(bestL)){
					bestS = i;
					bestL = nLL;
					attemptName = "random attempt " + i ;
				}
			}
			else 
				logln(logPrefix + "random attempt " + i + " failed because starting position had undefined likelihood");
		}
		if (bestS>=-1){
			if (bestS > 0)
				suggestions = randomSuggestions[bestS];
			//logln(logPrefix + "Estimating parameters, phase 2: step count 100; best so far " + evaluate(suggestions, bundle));
			stepCount = 1000;
			logln(logPrefix + "Estimating parameters, phase 2: step count 1000; best so far " + evaluate(suggestions, bundle));
			logln(logPrefix + "Estimating parameters, phase 2: step count 1000 starting from results of preliminary " + attemptName);
			double negLogLikelihood = opt.optimize(suggestions, bundle);
			logln(logPrefix + "neg. Log Likelihood final attempt:" + negLogLikelihood);

			final double [] result = new double[6];
			for(int i= 0;i<result.length;i++){
				if(i<toConstrain)
					result[i] = suggestions[i];
				else
					result[i] = suggestions[i-1];
			}
			return result;
		}
		logln(logPrefix + "Estimating parameters failed");
		return null;
	}



	private SpecExtincCategModel localModel3 = new SpecExtincCategModel();
	private double[] estimate3(Tree tree, CategoricalDistribution observedStates,int looseParameter, double suggestions[]){
		//some parameters are unassigned, and thus need to be estimated
		//First, we need to go through the parameters array to construct a mapping between our local free parameters and the original
		final String logPrefix = "Bisse (3 parameter starting point): ";
		quickIterations = iterations/2;
		MesquiteParameter [] localParams = new MesquiteParameter[6]; 
		for(int i= 0; i<6;i++){
			localParams[i] = new MesquiteParameter();
			if (i % 2 == 1) // get 1,3,5
				localParams[i].setConstrainedTo(localParams[i-1], false);            
		}
		localModel3.setParams(localParams);
		double [] localSuggestions = DoubleArray.copyIntoDifferentSize(suggestions, 3, MesquiteDouble.unassigned);
		Optimizer opt = new Optimizer(this);
		Object[] bundle = new Object[] {tree, observedStates, localModel3};
		stepCount = 100;
		int numParams = 3;
		logln("Bisse: constraining parameters 1,3,5 to generate starting point for 4 parameter estimate");
		logln("Bisse: Estimating three free parameters");
		logln("Bisse: Estimating parameters, phase 1: step count 100");
		double bestL = MesquiteDouble.unassigned;
		double nLL = MesquiteDouble.unassigned;
		int bestS = -2;
		if (evaluate(localSuggestions,bundle)<1e99){
			logln(logPrefix + DoubleArray.toString(localSuggestions));
			nLL = opt.optimize(localSuggestions,bundle);
			stepCount = 1000;
			nLL = evaluate(localSuggestions, bundle);
			stepCount = 100;
			logln(logPrefix + "initial starting point suggestion neg. Log Likelihood:" + nLL + " : " + DoubleArray.toString(suggestions));
			if (nLL < bestL || MesquiteDouble.isUnassigned(bestL)){
				bestS = -1;
				bestL = nLL;
			}
		}

		String attemptName = "random attempt";
		double[][] randomSuggestions = new double[quickIterations][numParams];
		for (int i = 0; i< quickIterations; i++){  
			if (i< quickIterations/2){//0 to 1 
				for (int k = 0; k<numParams; k++){
					randomSuggestions[i][k] = rng.randomDoubleBetween(0, 1.0);
				}
			}
			else { //00 to 10.0
				double max =  rng.randomDoubleBetween(0, 10.0);
				for (int k = 0; k<numParams; k++){
					randomSuggestions[i][k] = max - 0.5 + rng.randomDoubleBetween(0, 2.0);
				}

				if (evaluate(randomSuggestions[i], bundle) > 1e99) //likelihood bad; try a 0 to 1 one instead
					for (int k = 0; k<numParams; k++)
						randomSuggestions[i][k] = rng.randomDoubleBetween(0, 1.0);
			}
		}
		for (int i = 0; i< quickIterations; i++){ // 0 to 10
			if (evaluate(randomSuggestions[i], bundle) < 1e99){  //don't start if it hits surface in NaN-land
				logln(logPrefix + "random suggestions " + i + " :" + DoubleArray.toString(randomSuggestions[i]));
				nLL = opt.optimize(randomSuggestions[i], bundle);
				stepCount = 1000;
				nLL = evaluate(randomSuggestions[i], bundle);
				stepCount = 100;
				logln(logPrefix + "attempt " + i + " neg. Log Likelihood:" + nLL + " : " + DoubleArray.toString(randomSuggestions[i]));
				if (nLL < bestL || MesquiteDouble.isUnassigned(bestL)){
					bestS = i;
					bestL = nLL;
					attemptName = "random attempt " + i ;
				}
			}
			else 
				logln(logPrefix + "random attempt " + i + " failed because starting position had undefined likelihood");
		}
		if (bestS>=-1){
			if (bestS > 0)
				localSuggestions = randomSuggestions[bestS];
			//logln(logPrefix + "Estimating parameters, phase 2: step count 100; best so far " + evaluate(localSuggestions, bundle));
			stepCount = 1000;
			logln(logPrefix + "Estimating parameters, phase 2: step count 1000; best so far " + evaluate(localSuggestions, bundle));
			logln(logPrefix + "Estimating parameters, phase 2: step count 1000 starting from results of preliminary " + attemptName);
			double negLogLikelihood = opt.optimize(localSuggestions, bundle);
			logln(logPrefix + "neg. Log Likelihood final attempt:" + negLogLikelihood);
			final double [] result = new double[4];
			result[0] = localSuggestions[0];
			if (looseParameter == 1){
				result[1]=localSuggestions[0];
				result[2]=localSuggestions[1];
				result[3]=localSuggestions[2];
			}
			else if (looseParameter == 3){
				result[1]=localSuggestions[1];
				result[2]=localSuggestions[1];
				result[3]=localSuggestions[2];
			}
			else if (looseParameter == 5){
				result[1]=localSuggestions[1];
				result[2]=localSuggestions[2];
				result[3]=localSuggestions[2];                           
			}

			return result;
		}
		logln(logPrefix + "Estimating parameters failed");
		return null;
	}


	public Class getDutyClass() {
		// TODO Auto-generated method stub
		return BiSSELikelihoodCalculator.class;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "BiSSE Speciation/Extinction Likelihood Calculator";
	}
	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "1.01";
	}

	public String getExplanation(){
		return "Calculates likelihood with a tree of a species diversification model whose speciation and extinction rates depend on the state of a binary character (BiSSE model, Maddison, Midford & Otto, 2007).";
	}

	public boolean isPrerelease(){
		return false;
	}
}
/*============================================================================*/
class SpecExtincCategModel implements DESystem {

	MesquiteParameter[] original, parameters;
	int[] constraintMap, effectiveParamMapping;
	boolean[] checked;
	public SpecExtincCategModel(){
		constraintMap = new int[6];
		checked = new boolean[6];
	}

	/*
	 *  The probability values are passed the probs array in the following order
	 *  probs[0] = E0 = extinction probability in lineage starting at t in the past at state 0
	 *  probs[1] = E1 = extinction probability in lineage starting at t in the past at state 1
	 *  probs[2] = P0 = probability of explaining the data, given system is in state 0 at time t
	 *  probs[3] = P1 = probability of explaining the data, given system in in state 1 at time t
	 * @see mesquite.diverse.lib.DESystem#calculateDerivative(double, double[])
	 */
	public String toString(){
		return MesquiteParameter.toString(parameters);
	}
	public String toStringForAnalysis(){
		return MesquiteParameter.toStringForAnalysis(parameters);
	}
	public String toStringForScript(){
		return MesquiteParameter.paramsToScriptString(parameters);
	}
	/*.................................................................................................................*/
	public double[]calculateDerivative(double t,double probs[],double[] result){
		// for clarity
		final double extProb0 = probs[0];
		final double extProb1 = probs[1];
		final double dataProb0 = probs[2];
		final double dataProb1 = probs[3];
		final double lambda0 = parameters[0].getValue();
		final double lambda1 = parameters[1].getValue();
		final double mu0 = parameters[2].getValue();
		final double mu1 = parameters[3].getValue();
		final double q01 = parameters[4].getValue();
		final double q10 = parameters[5].getValue();

		result[0] = -(mu0+q01+lambda0)*extProb0 + lambda0*extProb0*extProb0 + mu0 + q01*extProb1; 
		result[1] = -(mu1+q10+lambda1)*extProb1 + lambda1*extProb1*extProb1 + mu1 + q10*extProb0;            
		result[2] = -(mu0+q01+lambda0)*dataProb0 + 2*lambda0*extProb0*dataProb0 + q01*dataProb1;
		result[3] = -(mu1+q10+lambda1)*dataProb1 + 2*lambda1*extProb1*dataProb1 + q10*dataProb0;
		return result;
	}

	public boolean isFullySpecified(){
		return MesquiteParameter.numberSpecified(original) == 6;
	}
	public int numberSpecified(){
		return MesquiteParameter.numberSpecified(original);
	}
	/*.................................................................................................................*/
	public int numberEffectiveParameters(){
		if (effectiveParamMapping == null)
			return 0;
		return effectiveParamMapping.length;
	}

	/*.................................................................................................................*/
	public void setParams(MesquiteParameter[] params){
		original = MesquiteParameter.cloneArray(params, original);
		parameters = MesquiteParameter.cloneArray(params, parameters);
		for (int i=0; i<6; i++){
			int c = MesquiteParameter.getWhichConstrained(params, i);

			if (c >= 0)
				constraintMap[i] = c;
			else
				constraintMap[i] = i;
			checked[i] = false;
		}
		int count = 0;
		for (int i=0; i<6; i++){  //figuring out what are the unset parameters;
			int mapee = constraintMap[i];
			if (!checked[mapee])
				if (!original[mapee].isCombinable())
					count++;
			checked[mapee] = true;
		}
		if (effectiveParamMapping == null || effectiveParamMapping.length != count)
			effectiveParamMapping = new int[count];
		count = 0;
		for (int i=0; i<6; i++)
			checked[i] = false;
		for (int i=0; i<6; i++){  //figuring out what are the unset parameters;
			int mapee = constraintMap[i];
			if (!checked[mapee])
				if (!original[mapee].isCombinable()){
					effectiveParamMapping[count] = mapee;
					count++;
				}
			checked[mapee] = true;
		}
	}

	/*.................................................................................................................*/
	public void setParamValuesUsingConstraints(double[] params){
		if (params == null || params.length == 0)
			return;
		if (params.length == 6 && effectiveParamMapping == null){
			for (int i=0; i<6; i++)
				parameters[i].setValue(params[i]);
		}
		else if (effectiveParamMapping== null){
			MesquiteMessage.warnProgrammer("Sp/Ext setParamValuesUsingConstraints with effective params NULL");
		}
		else if (params.length != effectiveParamMapping.length){
			MesquiteMessage.warnProgrammer("Sp/Ext setParamValuesUsingConstraints with effective params size mismatch " + params.length + " " + effectiveParamMapping.length);
		}
		else {
			for (int i=0; i<params.length; i++) {
				int mapee = effectiveParamMapping[i]; //find parameter representing constraint group
				//now find all params constrained to it, and set them
				for (int k = 0; k< parameters.length; k++){
					if (constraintMap[k] == mapee)
						parameters[k].setValue(params[i]);
				}
			}
		}
	}

	/*.................................................................................................................*/
	public MesquiteParameter[] getOriginalParams(MesquiteParameter[] params){
		return MesquiteParameter.cloneArray(original, params);
	}
	/*.................................................................................................................*/
	public MesquiteParameter[] getCurrentParams(MesquiteParameter[] params){
		return MesquiteParameter.cloneArray(parameters, params);
	}
	/*.................................................................................................................*/
	public double getSRate(int state) {
		if (state == 0)
			return parameters[0].getValue();
		else if (state == 1)
			return parameters[1].getValue();
		else return MesquiteDouble.unassigned;
	}

	/*.................................................................................................................*/
	public double getERate(int state) {
		if (state == 0)
			return parameters[2].getValue();
		else if (state == 1)
			return parameters[3].getValue();
		else return MesquiteDouble.unassigned;
	}
	/*.................................................................................................................*/
	public double getCRate(int state) {
		if (state == 0)
			return parameters[4].getValue();
		else if (state == 1)
			return parameters[5].getValue();
		else return MesquiteDouble.unassigned;
	}

}


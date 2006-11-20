package mesquite.diverse.SpExtCategCharMLCalculator;

import java.util.Vector;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.diverse.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.ParametersExplorer;

public class SpExtCategCharMLCalculator extends MesquiteModule implements ParametersExplorable, Evaluator {

	// bunch of stuff copied from zMargLikeCateg - need to prune!!


	DEQNumSolver solver;
	double [][] probsExt, probsData;
	double[] yStart;  //initial value for numerical integration
	double[] d,e; //intermediate results
	double[][] savedRootEstimates; // holds the estimates at the root (for simulation priors, etc.)
	MesquiteNumber probabilityValue;
	int numStates;

	long underflowCheckFrequency = 2; //2;  //how often to check that not about to underflow; 1 checks every time
	long underflowCheck = 1;
	double underflowCompensation = 1;
	MesquiteNumber minChecker;
	// hooks for capturing context for table dump.
	Tree lastTree;
	CharacterDistribution lastCharDistribution;

	// Number of steps per branch, reduce for a faster, possibily sloppier result
	double stepCount = 1000;  //default 10000

	//In version 1.1. the assumption about the root prior for model estimation, ancestral state reconstruction and simulation is assumed to be embedded in the model
	//Thus, the control is removed here
	public static final int ROOT_IGNOREPRIOR = 0;  // likelihoodignore's model's prior
	public static final int ROOT_USEPRIOR = 1;  // calculates ancestral states imposing model's prior
	public boolean showRootModeChoices = false; 
	int rootMode = ROOT_USEPRIOR; //what if anything is done with prior probabilities of states at subroot?  
	StringArray rootModes;
	MesquiteString rootModeName;

	static final int REPORT_Proportional = 0;  
	static final int REPORT_Raw = 1;
	static final int REPORT_Log = 2;
	StringArray reportModes;
	int reportMode = REPORT_Proportional;
	MesquiteString reportModeName;
	MesquiteBoolean intermediatesToConsole = new MesquiteBoolean(false);

	boolean[] deleted = null;
	MesquiteParameter s0p;
	MesquiteParameter s1p;
	MesquiteParameter e0p;
	MesquiteParameter e1p;
	MesquiteParameter t01p;
	MesquiteParameter t10p;
//	MesquiteParameter[] parameters;
	MesquiteParameter[] paramsForExploration;
	MesquiteParameter[] previousParams;
	ParametersExplorer explorer;
	MesquiteBoolean conditionOnSurvival;
	SpecExtincCategModel speciesModel;
	RandomBetween rng;
	int iterations = 2;
	boolean suspended = false;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		loadPreferences();
		probabilityValue = new MesquiteNumber();
		minChecker = new MesquiteNumber(MesquiteDouble.unassigned);

		solver = new RK4Solver();
		rng = new RandomBetween(System.currentTimeMillis());
		//following is for the parameters explorer
		double def = MesquiteDouble.unassigned;
		s0p = new MesquiteParameter("s0", "Rate of speciation with state 0", 0.1, 0, MesquiteDouble.infinite, 0.000, 1);
		s1p = new MesquiteParameter("s1", "Rate of speciation with state 1", 0.1, 0, MesquiteDouble.infinite, 0.000, 1);
		e0p = new MesquiteParameter("e0", "Rate of extinction with state 0", 0.1, 0, MesquiteDouble.infinite, 0.000, 1);
		e1p = new MesquiteParameter("e1", "Rate of extinction with state 1", 0.1, 0, MesquiteDouble.infinite, 0.000, 1);
		t01p = new MesquiteParameter("r01", "Rate of 0->1 changes", 0.1, 0, MesquiteDouble.infinite, 0.000, 1);
		t10p = new MesquiteParameter("r10", "Rate of 1->0 changes", 0.1, 0, MesquiteDouble.infinite, 0.000, 1);

		paramsForExploration= new MesquiteParameter[]{s0p, s1p, e0p, e1p, t01p, t10p};
		speciesModel = new SpecExtincCategModel();
		speciesModel.setParams(paramsForExploration);

//		parameters = MesquiteParameter.cloneArray(paramsForExploration, null);
		previousParams = new MesquiteParameter[6];
		for (int i = 0; i<6; i++)
			previousParams [i] = new MesquiteParameter();

		conditionOnSurvival = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Condition on Survival", MesquiteModule.makeCommand("conditionOnSurvival", this), conditionOnSurvival);
		MesquiteSubmenuSpec mLO = addSubmenu(null, "Likelihood Calculation", null); 
		addItemToSubmenu(null, mLO, "Steps per Branch...", makeCommand("setStepCount", this));
		addItemToSubmenu(null, mLO, "Optimization Iterations...", makeCommand("setIterations", this));
		addItemToSubmenu(null, mLO, "Underflow Checking...", makeCommand("setUnderflowCheckFreq", this));
		addMenuItem("-", null);
		addMenuItem("Show Parameters Explorer", makeCommand("showParamExplorer",this));
		addCheckMenuItem(null, "Intermediates to console", makeCommand("toggleIntermediatesToConsole",this), intermediatesToConsole);
//		addMenuItem("-", null);
		rootModes = new StringArray(2);  
		rootModes.setValue(ROOT_IGNOREPRIOR, "Ignore Root State Frequencies");  //the strings passed will be the menu item labels
		rootModes.setValue(ROOT_USEPRIOR, "Use Root State Frequencies as Prior");
		rootModeName = new MesquiteString(rootModes.getValue(rootMode));  //this helps the menu keep track of checkmenuitems

		//if (showRootModeChoices && getHiredAs() != MargLikeAncStCLForModel.class){
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
		if (explorer != null)
			temp.addLine("showParamExplorer ", explorer);
		temp.addLine("resume ");
		return temp;
	}

	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets the frequency of checking for underflow", "[integer, 1 or greater]", commandName, "setUnderflowCheckFreq")) {
			int freq = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(freq) && !commandRec.scripting())
				freq = MesquiteInteger.queryInteger(containerOfModule(), "Checking frequency", "Frequency at which underflow checking is performed in likelihood calculations.  A value of n means checking is performed on each nth calculation; higher numbers mean the calculations go faster but are at risk of underflow problems.  Values over 10 are not recommended", (int)underflowCheckFrequency, 1, 10000);

			if (MesquiteInteger.isCombinable(freq) && freq >=0 && freq!=underflowCheckFrequency){
				underflowCheckFrequency = freq; //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspended = true;
		}
		else if (checker.compare(getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspended = false;
		}
		else if (checker.compare(getClass(), "Returns last result string", null, commandName, "getLastResultString")) {
			return lastResultString;
		}
		else if (checker.compare(getClass(), "Sets the number of steps per branch", "[double, 1 or greater]", commandName, "setStepCount")) {
			double steps = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(steps) && !commandRec.scripting())
				steps = MesquiteDouble.queryDouble(containerOfModule(), "Steps per branch", "Number of divisions of each branch for numerical integration.  Higher numbers mean the calculations are more accurate but go more slowly.  Values under 100 are not recommended", stepCount, 10, 1000000);

			if (MesquiteDouble.isCombinable(steps) && steps >=0 && steps!=stepCount){
				stepCount = steps; //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets the number of iterations in the likelihood optimization", "[integer, 1 or greater]", commandName, "setIterations")) {
			int it = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(it) && !commandRec.scripting())
				it = MesquiteInteger.queryInteger(containerOfModule(), "Optimization Iterations", "Number of random starting points for likelihood optimizationi.  Higher numbers mean the optimization is more thorough  but goes more slowly.", iterations, 1, 1000);

			if (MesquiteInteger.isCombinable(it) && it >=0 && it!=iterations){
				iterations = it; //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to condition by survival", "[on; off]", commandName, "conditionOnSurvival")) {
			conditionOnSurvival.toggleValue(new Parser().getFirstToken(arguments));
			if (!commandRec.scripting())parametersChanged(null, commandRec);
		}
		else if (checker.compare(getClass(), "Writes table to console", "", commandName, "showParamExplorer")) {
			explorer = (ParametersExplorer)hireEmployee(commandRec, ParametersExplorer.class, "Parameters explorer");
			if (explorer == null)
				return null;
			explorer.setExplorable(this, commandRec);
			return explorer;
		}
		else if (checker.compare(getClass(),"Sets whether to write intermediate branch values to console","[on; off]", commandName, "toggleIntermediatesToConsole")){
			intermediatesToConsole.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}
	/*.................................................................................................................*/
	private void initProbs(int nodes, int numStates) {
		this.numStates = numStates;
		if (yStart == null || yStart.length != numStates*2 || probsData==null || probsData.length!=nodes || probsData[0].length!=numStates){
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
//			Debugg.println("underflow comp used " + q);
			for (int i=0; i<probs.length; i++)
				probs[i] /= q;
		}
		return -Math.log(q);
	}
	/*.................................................................................................................*/
	/* assumes hard polytomies */

	// This might hold the intermediate step results if requested for debugging
	Vector integrationResults = null;

	/* now returns underflow compensation */
	private double downPass(int node, Tree tree, SpecExtincCategModel model, DEQNumSolver solver, CategoricalDistribution observedStates) {
		double logComp;
		if (tree.nodeIsTerminal(node)) { //initial conditions from observations if terminal
			long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node));
			int obs = CategoricalState.minimum(observed); //NOTE: just minimum observed!
			//Debugg.println("node " + node + " state " + CategoricalState.toString(observed));
			for (int state = 0;state < numStates;state++){
				e[state]=0;
				if ((state == obs))
					d[state] = 1;
				else
					d[state] = 0;
			}
			logComp = 0.0;  // no compensation required yet
		}
		else { //initial conditions from daughters if internal
			logComp = 0.0;
			for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
				logComp += downPass(nd,tree,model,solver,observedStates);
			}
			for(int state = 0; state < numStates;state++){
				d[state] = 1;  //two here to anticipate two daughters???
				e[state] = 1;
				//TODO: either filter to permit only ultrametric, or redo calculations to permit extinct nodes.
				//TODO: likewise for polytomies
				for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
					// Debugg.println("probsExt["+nd +"][" + state + "] = " + probsExt[nd][state]);
					// Debugg.println("probsData["+nd +"][" + state + "] = " + probsData[nd][state]);
					e[state] = probsExt[nd][state];
					d[state] *= probsData[nd][state];
				}

				if (node != tree.getRoot()){  //condition on splitting at root; thus don't include rate if at root
					d[state] *= model.getSRate(state);
				}

			}
			if (underflowCheckFrequency>=0 && ++underflowCheck % underflowCheckFrequency == 0){
				logComp += checkUnderflow(d);
			}
		}
		if (node == tree.getRoot()){
			for(int i=0;i<numStates;i++){
				probsExt[node][i] = e[i];
				probsData[node][i] = d[i];
			}
			return logComp;
		}
		else{
			double x = 0;
			double length = tree.getBranchLength(node,1.0,deleted);
			double h = length/stepCount;       //this will need tweaking!
			for(int i=0;i<numStates;i++){
				yStart[i] = e[i];
				yStart[i+numStates] = d[i];
			}
			if (intermediatesToConsole.getValue()){
				MesquiteMessage.print("node " + node);
				if (node == tree.getRoot())
					MesquiteMessage.println(" is root");
				else
					MesquiteMessage.println("");
				MesquiteMessage.println("At start, y is " + DoubleArray.toString(yStart));
			}
			integrationResults = solver.integrate(x,yStart,h,length,model,integrationResults,intermediatesToConsole.getValue());        
			double[] yEnd = (double[])integrationResults.lastElement();
			if (yEnd.length == 2*numStates){
				for(int i=0;i<numStates;i++){
					probsExt[node][i] = yEnd[i];
					probsData[node][i] = yEnd[i+numStates];
				}
			}
			else {
				MesquiteMessage.warnProgrammer("Vector returned by solver not the same size supplied!");
				for(int i=0;i<numStates;i++){
					probsExt[node][i]=0;   //is this the right thing here?
					probsData[node][i]=0;  //this is probably the best choice here
				}
			}
			if (intermediatesToConsole.getValue()){
				MesquiteMessage.println("Intermediate values");
				StringBuffer stateMsg = new StringBuffer(1000);
				//stateMsg.delete(0,stateMsg.length());  //flush everything
				x = h;
				double [] tempResults;
				for(int i=0;i<integrationResults.size();i++){
					if(i%100 == 0){
						String xString = MesquiteDouble.toFixedWidthString(x, 13, false);
						stateMsg.append("x= "+ xString + " y =[");
						tempResults = (double[])integrationResults.get(i);
						for(int j=0;j<tempResults.length;j++)
							stateMsg.append(MesquiteDouble.toFixedWidthString(tempResults[j],13,false)+" ");
						stateMsg.append("]\n");
					}   
					x += h;
				}
				stateMsg.append("Final value; \n");
				stateMsg.append("x= " + h*stepCount + " y =[");
				tempResults = (double[])integrationResults.lastElement();
				for(int j=0;j<tempResults.length;j++)
					stateMsg.append(MesquiteDouble.toFixedWidthString(tempResults[j],13,false)+" ");
				stateMsg.append("]\n");
				MesquiteMessage.println(stateMsg.toString());
			}
			return logComp;
		}
	}
	/*------------------------------------------------------------------------------------------*/
	/** these methods for ParametersExplorable interface */
	public MesquiteParameter[] getExplorableParameters(){
		return paramsForExploration;
	}
	MesquiteNumber likelihood = new MesquiteNumber();
	public double calculate(MesquiteString resultString, CommandRecord commandRec){
		if (suspended)
			return MesquiteDouble.unassigned;
		calculateLogProbability( lastTree,  lastCharDistribution, paramsForExploration, likelihood, resultString, commandRec);
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
	/*.................................................................................................................*/
	public double evaluate(double[] params, Object bundle){
		if (anyNegative(params))
			return 1e100;
		Object[] b = ((Object[])bundle);
		Tree tree = (Tree)b[0];
		CategoricalDistribution states = (CategoricalDistribution)b[1];
		SpecExtincCategModel model = (SpecExtincCategModel)b[2];
		model.setParamValuesUsingConstraints(params);
		double result =  logLike(tree, states, model);
		if (!MesquiteDouble.isCombinable(result) || result < -1e100 || result > 1e100)
			result = 1e100;
		if (count++ % 10 == 0)
			CommandRecord.getRecSIfNull().tick("Evaluating: -log likelihood " + MesquiteDouble.toString(result, 4) + " params " + MesquiteParameter.toString(params));
		return result;
	}
	/*.................................................................................................................*/
	double[] oneParam = new double[1];
	/*.................................................................................................................*/
	public double evaluate(MesquiteDouble param, Object bundle){
		oneParam[0] = param.getValue();
		if (anyNegative(oneParam))
			return 1e100;
		Object[] b = ((Object[])bundle);
		Tree tree = (Tree)b[0];
		CategoricalDistribution states = (CategoricalDistribution)b[1];
		SpecExtincCategModel model = (SpecExtincCategModel)b[2];
		model.setParamValuesUsingConstraints(oneParam);
		double result =  logLike(tree, states, model);
		if (!MesquiteDouble.isCombinable(result) || result < -1e100 || result > 1e100)
			result = 1e100;
		if (count++ % 10 == 0)
			CommandRecord.getRecSIfNull().tick("Evaluating: -log likelihood " + MesquiteDouble.toString(result, 4) + " param " + MesquiteParameter.toString(oneParam));
		return result;
	}
	/*.................................................................................................................*/
	public double logLike(Tree tree, CategoricalDistribution states, SpecExtincCategModel model) {  
		if (model==null)
			return MesquiteDouble.unassigned;
		int root = tree.getRoot(deleted);
		initProbs(tree.getNumNodeSpaces(),lastMaxState);
		double logComp = downPass(root, tree, model, solver, states);
		double likelihood = 0.0;
		/*~~~~~~~~~~~~~~ calculateLogProbability ~~~~~~~~~~~~~~*/
		if (conditionOnSurvival.getValue()){
			for (int i=0;  i<lastMaxState; i++)
				likelihood += probsData[root][i]/(1-probsExt[root][i])/(1-probsExt[root][i]);
		}
		else
			for (int i=0;  i<lastMaxState; i++) 
				likelihood += probsData[root][i];
		double negLogLikelihood = -(Math.log(likelihood) - logComp);
		return negLogLikelihood;
	}
	String lastResultString;
	/*
	 Options:
	 1. calculation determined entirely by params.  Constraints etc. determined within params array which is contained in species model
	 2. predefined: constraint s0-e0 = s1-e1
	/*.................................................................................................................*/
	public void calculateLogProbability(Tree tree, CharacterDistribution obsStates, MesquiteParameter[] params, MesquiteNumber prob, MesquiteString resultString, CommandRecord commandRec) {  
		if (speciesModel==null || obsStates==null || prob == null)
			return;
		lastTree = tree;
		lastCharDistribution = obsStates;
		prob.setToUnassigned();
		if (suspended)
			return;
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		int workingMaxState;
		if (observedStates.getMaxState() <= 0){
			MesquiteMessage.warnProgrammer("Character Distribution appears to be constant; cannot calculated likelihood of tree and character");
			return;
		}
		else
			workingMaxState = observedStates.getMaxState()+1; 
		lastMaxState = workingMaxState;
		speciesModel.getParams(previousParams);
		speciesModel.setParams(params);
		double currentStep = stepCount;

		double negLogLikelihood = 0;
		String modelString = "";
		if (speciesModel.isFullySpecified()){
			negLogLikelihood = logLike(tree, observedStates, speciesModel);
			modelString  = speciesModel.toString();
		}
		//========================== all 6 parameters unspecified ==============================
		else if (speciesModel.numberSpecified() ==0 && speciesModel.numberEffectiveParameters() == 6){  //all unspecified
			//some or all parameters are unassigned, and thus need to be estimated
			//First, we need to go through the parameters array to construct a mapping between our local free parameters and the original
			Optimizer opt = new Optimizer(this);
			Object[] bundle = new Object[] {tree, observedStates, speciesModel};
			stepCount = 100;
			double useIterations = iterations;
			double[] suggestions1 = new double[]{1, 0.5, 0.5, 0.2, 0.1, 0.05};
			double[] suggestions2 = new double[]{0.5, 1, 0.2, 0.5, 0.05, 0.1};
			double bestL = 1e101;
			double[] suggestions = null;
			int bestS = -1;
			String attemptName = "";
			if (evaluate(suggestions1, bundle) < 1e99 && evaluate(suggestions2, bundle) < 1e99){
				logln("Sp/Ext Categ Char: Tree " + tree.getName() + " and character " + obsStates.getName());
				logln("Sp/Ext Categ Char: Estimating all 6 parameters, phase 1: step count 100");
				double negLogLikelihood1 = opt.optimize(suggestions1, bundle);
				logln("Sp/Ext Categ Char: neg. Log Likelihood first attempt:" + negLogLikelihood1);
				double negLogLikelihood2 = opt.optimize(suggestions2, bundle);
				logln("Sp/Ext Categ Char: neg. Log Likelihood second attempt:" + negLogLikelihood2);
				if (negLogLikelihood1 < negLogLikelihood2) {
					suggestions = suggestions1;
					bestS = -1;
					bestL = negLogLikelihood1;
					attemptName = " first attempt" ;
				}
				else {
					suggestions = suggestions2;
					bestS = -2;
					bestL = negLogLikelihood2;
					attemptName = " second attempt" ;
				}
			}
			else
				useIterations = iterations + 2;

			double[][] randomSuggestions = new double[iterations][6];
			
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

				}
			}
			for (int i = 0; i< useIterations; i++){ // 0 to 10
				if (evaluate(randomSuggestions[i], bundle) < 1e99){  //don't start if it hits surface in NaN-land
					logln("Sp/Ext Categ Char: random suggestions " + i + " :" + DoubleArray.toString(randomSuggestions[i]));
					double nLL = opt.optimize(randomSuggestions[i], bundle);
					stepCount = 1000;
					nLL = evaluate(randomSuggestions[i], bundle);
					stepCount = 100;
					logln("Sp/Ext Categ Char: random attempt " + i + " neg. Log Likelihood:" + nLL + " : " + DoubleArray.toString(randomSuggestions[i]));
					if (nLL < bestL){
						bestS = i;
						bestL = nLL;
						attemptName = "random attempt " + i ;
					}
				}
				else 
					logln("Sp/Ext Categ Char: random attempt " + i + " failed because starting position had undefined likleihood");
			}
			if (bestS>=0)
				suggestions = randomSuggestions[bestS];
			if (suggestions == null){
				logln("Sp/Ext Categ Char: Estimating parameters failed");
			}
			else {
				logln("Sp/Ext Categ Char: Estimating parameters, phase 2: step count 100; best so far " + evaluate(suggestions, bundle));
				stepCount = 1000;
				logln("Sp/Ext Categ Char: Estimating parameters, phase 2: step count 1000; best so far " + evaluate(suggestions, bundle));
				logln("Sp/Ext Categ Char: Estimating parameters, phase 2: step count 1000 starting from results of preliminary " + attemptName);
				negLogLikelihood = opt.optimize(suggestions, bundle);
				logln("Sp/Ext Categ Char: neg. Log Likelihood final attempt:" + negLogLikelihood);

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
			int numParams = speciesModel.numberEffectiveParameters();
			double[] suggestions1 = new double[numParams];
			logln("Sp/Ext Categ Char: Tree " + tree.getName() + " and character " + obsStates.getName());
			logln("Sp/Ext Categ Char: Estimating " + numParams + " free parameters");
			for (int i=0; i < suggestions1.length; i++)
				suggestions1[i] = 0.1*(i+1);
			logln("Sp/Ext Categ Char: Estimating parameters, phase 1: step count 100");
			double bestL = MesquiteDouble.unassigned;
			int bestS = -1;

			String attemptName = "random attempt";
			double[][] randomSuggestions = new double[iterations][numParams];
			for (int i = 0; i< iterations; i++){  
				double max = 1.0;
				if (i< iterations/2){
					max =  1.0;
					for (int k = 0; k<numParams; k++){
						randomSuggestions[i][k] = rng.randomDoubleBetween(0, max);
					}
				}
				else { //0 to 1 first then 0 to 10.0
					for (int k = 0; k<numParams; k++){
						max =  rng.randomDoubleBetween(0, 10.0);
						//randomSuggestions[i][k] = max - 0.5 + rng.randomDoubleBetween(0, 2.0);
					}

				}
			}
			for (int i = 0; i< iterations; i++){ // 0 to 10
				if (evaluate(randomSuggestions[i], bundle) < 1e99){  //don't start if it hits surface in NaN-land
					logln("Sp/Ext Categ Char: random suggestions " + i + " :" + DoubleArray.toString(randomSuggestions[i]));
					double nLL = opt.optimize(randomSuggestions[i], bundle);
					stepCount = 1000;
					nLL = evaluate(randomSuggestions[i], bundle);
					stepCount = 100;
					logln("Sp/Ext Categ Char: attempt " + i + " neg. Log Likelihood:" + nLL + " : " + DoubleArray.toString(randomSuggestions[i]));
					if (nLL < bestL || MesquiteDouble.isUnassigned(bestL)){
						bestS = i;
						bestL = nLL;
						attemptName = "random attempt " + i ;
					}
				}
				else 
					logln("Sp/Ext Categ Char: random attempt " + i + " failed because starting position had undefined likleihood");
			}
			if (bestS>=0){
				double[] suggestions = randomSuggestions[bestS];
				logln("Sp/Ext Categ Char: Estimating parameters, phase 2: step count 100; best so far " + evaluate(suggestions, bundle));
				stepCount = 1000;
				logln("Sp/Ext Categ Char: Estimating parameters, phase 2: step count 1000; best so far " + evaluate(suggestions, bundle));
				logln("Sp/Ext Categ Char: Estimating parameters, phase 2: step count 1000 starting from results of preliminary " + attemptName);
				negLogLikelihood = opt.optimize(suggestions, bundle);
				logln("Sp/Ext Categ Char: neg. Log Likelihood final attempt:" + negLogLikelihood);

				speciesModel.setParamValuesUsingConstraints(suggestions);
				modelString  = speciesModel.toString() + " [est.]";
			}
			else 
				logln("Sp/Ext Categ Char: Estimating parameters failed");


		}
		//========================== 1 parameter unspecified ==============================
		else  {  //1 parametersunassigned;
			Optimizer opt = new Optimizer(this);
			Object[] bundle = new Object[] {tree, observedStates, speciesModel};
			stepCount = 100;

			logln("Sp/Ext Categ Char: Tree " + tree.getName() + " and character " + obsStates.getName());
			logln("Sp/Ext Categ Char: Estimating one free parameter");
			logln("Sp/Ext Categ Char: Estimating parameter, phase 1: step count 100");
			MesquiteDouble suggestion = new MesquiteDouble(1);
			negLogLikelihood = opt.optimize(suggestion, 0, 100, bundle);
			logln("Sp/Ext Categ Char: neg. Log Likelihood first attempt:" + negLogLikelihood);
			double bestL = negLogLikelihood;
			double bestP = suggestion.getValue();
			negLogLikelihood = opt.optimize(suggestion, 0.0, 10, bundle);
			logln("Sp/Ext Categ Char: neg. Log Likelihood second attempt:" + negLogLikelihood);
			if (bestL < negLogLikelihood)
				suggestion.setValue(bestP);
			else{
				bestP = suggestion.getValue();
				bestL = negLogLikelihood;
			}
			negLogLikelihood = opt.optimize(suggestion, 0.0, 1, bundle);
			logln("Sp/Ext Categ Char: neg. Log Likelihood third attempt:" + negLogLikelihood);
			if (bestL < negLogLikelihood)
				suggestion.setValue(bestP);
			else{
				bestP = suggestion.getValue();
				bestL = negLogLikelihood;
			}
			stepCount = 1000;
			logln("Sp/Ext Categ Char: Estimating parameters, phase 2: step count 1000");
			negLogLikelihood = opt.optimize(suggestion, suggestion.getValue() * 0.6, suggestion.getValue() * 1.4, bundle);
			logln("Sp/Ext Categ Char: neg. Log Likelihood final attempt:" + negLogLikelihood);

			oneParam[0] = suggestion.getValue();
			speciesModel.setParamValuesUsingConstraints(oneParam);
			modelString  = speciesModel.toString() + " [est.]";

		}
		stepCount = currentStep;

		if (prob!=null)
			prob.setValue(negLogLikelihood);
		double likelihood = Math.exp(-negLogLikelihood);
		if (MesquiteDouble.isUnassigned(negLogLikelihood))
			likelihood = MesquiteDouble.unassigned;
		if (resultString!=null) {
			String s = "Sp/Ext Likelihood (Char. dep.) " + modelString + ";  -log L.:"+ MesquiteDouble.toString(negLogLikelihood) + " [L. "+ MesquiteDouble.toString(likelihood) + "]";
			s += "  " + getParameters();
			resultString.setValue(s);
		}
		lastResultString = tree.getName() + "\t" + obsStates.getName() +"\t" + speciesModel.toStringForAnalysis() + "\t" + MesquiteDouble.toString(negLogLikelihood);
		speciesModel.setParams(previousParams);
	}



	public Class getDutyClass() {
		// TODO Auto-generated method stub
		return SpExtCategCharMLCalculator.class;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Integrating (speciation/extinction) Likelihood Calculator for Categorical Data";
	}
	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getExplanation(){
		return "Calculates likelihood of a tree and tip values using an extinction/speciation model expressed as a system of differential equations.";
	}

	public boolean isPrerelease(){
		return true;
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
	 * @see mesquite.correl.lib.DESystem#calculateDerivative(double, double[])
	 */
	public String toString(){
		return MesquiteParameter.toString(parameters);
	}
	public String toStringForAnalysis(){
		return MesquiteParameter.toStringForAnalysis(parameters);
	}
	public double[]calculateDerivative(double t,double probs[],double[] result){
		// for clarity
//		Debugg.println("probs " + probs.length + " result " + result.length);
		double extProb0 = probs[0];
		double extProb1 = probs[1];
		double dataProb0 = probs[2];
		double dataProb1 = probs[3];
		double s0 = parameters[0].getValue();
		double s1 = parameters[1].getValue();
		double e0 = parameters[2].getValue();
		double e1 = parameters[3].getValue();
		double t01 = parameters[4].getValue();
		double t10 = parameters[5].getValue();

		result[0] = -(e0+t01+s0)*extProb0 + s0*extProb0*extProb0 + e0 + t01*extProb1; 
		result[1] = -(e1+t10+s1)*extProb1 + s1*extProb1*extProb1 + e1 + t10*extProb0;            
		result[2] = -(e0+t01+s0)*dataProb0 + 2*s0*extProb0*dataProb0 + t01*dataProb1;
		result[3] = -(e1+t10+s1)*dataProb1 + 2*s1*extProb1*dataProb1 + t10*dataProb0;
		return result;
	}

	public boolean isFullySpecified(){
		return MesquiteParameter.numberSpecified(original) == 6;
	}
	public int numberSpecified(){
		return MesquiteParameter.numberSpecified(original);
	}
	public int numberEffectiveParameters(){
		if (effectiveParamMapping == null)
			return 0;
		return effectiveParamMapping.length;
	}

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
//		Debugg.println("constraintMap " + constraintMap.length + "  " + IntegerArray.toString(constraintMap));
//		Debugg.println("effective mapping " + effectiveParamMapping.length + "  " + IntegerArray.toString(effectiveParamMapping));
	}
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
		//	Debugg.println("examined params " + MesquiteParameter.toString(parameters));
	}

	public void getParams(MesquiteParameter[] params){
		if (params != null)
			params = MesquiteParameter.cloneArray(original, params);
	}
	public double getSRate(int state) {
		if (state == 0)
			return parameters[0].getValue();
		else if (state == 1)
			return parameters[1].getValue();
		else return MesquiteDouble.unassigned;
	}

	public double getERate(int state) {
		if (state == 0)
			return parameters[2].getValue();
		else if (state == 1)
			return parameters[3].getValue();
		else return MesquiteDouble.unassigned;
	}

}

/*============================================================================*
class EqualDiversificationModel implements DESystem {
 /*This is based on a reparametrizaition to r = s - e, a = e/s (following Nee et al)
  How to back convert?  Given r and a, what are s and ?
  s = r + e;
  e = s*a;
  thus s = r + s*a
  s-sa =r
  if (a != 1)
  	s = r/(1-a);  //note this doesn't work if e = s
  	e = ra/(1-a)
  else
  	
  e = s*a;
  r = s - (s*a)
  r = s(1-a)
  s = r/(1-a)
  
  
	
}
*/
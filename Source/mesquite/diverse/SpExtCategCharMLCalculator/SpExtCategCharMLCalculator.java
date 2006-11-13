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
	MesquiteParameter s0p = new MesquiteParameter();
	MesquiteParameter s1p = new MesquiteParameter();
	MesquiteParameter e0p = new MesquiteParameter();
	MesquiteParameter e1p = new MesquiteParameter();
	MesquiteParameter t01p = new MesquiteParameter();
	MesquiteParameter t10p = new MesquiteParameter();
	MesquiteParameter[] parameters;
	MesquiteDouble[] paramsExpl;
	MesquiteDouble[] tempParams;
	ParametersExplorer explorer;
	MesquiteBoolean conditionOnSurvival;
	SpecExtincCategModel speciesModel;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		loadPreferences();
		probabilityValue = new MesquiteNumber();
		minChecker = new MesquiteNumber(MesquiteDouble.unassigned);

		speciesModel = new SpecExtincCategModel(0.001, 0.001, 0.005, 0.001, 0.01, 0.01);
		solver = new RK4Solver();

		//following is for the parameters explorer
		s0p.setName("s0");
		s0p.setExplanation("Rate of speciation with state 0");
		s0p.setMinimumAllowed(0);
		s0p.setMaximumAllowed(MesquiteDouble.infinite);
		s0p.setMinimumSuggested(0.000);
		s0p.setMaximumSuggested(1);
		s0p.setValue(0.1);
		s1p.setName("s1");
		s1p.setExplanation("Rate of speciation with state 1");
		s1p.setMinimumSuggested(0.0);
		s1p.setMaximumSuggested(1);
		s1p.setMinimumAllowed(0);
		s1p.setMaximumAllowed(MesquiteDouble.infinite);
		s1p.setValue(0.1);
		e0p.setName("e0");
		e0p.setExplanation("Rate of extinction with state 0");
		e0p.setMinimumSuggested(0.000);
		e0p.setMaximumSuggested(1);
		e0p.setMinimumAllowed(0);
		e0p.setMaximumAllowed(MesquiteDouble.infinite);
		e0p.setValue(0.1);
		e1p.setName("e1");
		e1p.setExplanation("Rate of extinction with state 1");
		e1p.setMinimumSuggested(0.000);
		e1p.setMaximumSuggested(1);
		e1p.setMinimumAllowed(0);
		e1p.setMaximumAllowed(MesquiteDouble.infinite);
		e1p.setValue(0.1);
		t01p.setName("r01");
		t01p.setExplanation("Rate of 0->1 changes");
		t01p.setMinimumSuggested(0.00);
		t01p.setMaximumSuggested(1);
		t01p.setMinimumAllowed(0);
		t01p.setMaximumAllowed(MesquiteDouble.infinite);
		t01p.setValue(0.1);
		t10p.setName("r10");
		t10p.setExplanation("Rate of 1->0 changes");
		t10p.setMinimumSuggested(0.000);
		t10p.setMaximumSuggested(1);
		t10p.setMinimumAllowed(0);
		t10p.setMaximumAllowed(MesquiteDouble.infinite);
		t10p.setValue(0.1);
		parameters = new MesquiteParameter[]{s0p, s1p, e0p, e1p, t01p, t10p};
		paramsExpl = new MesquiteDouble[6];
		tempParams = new MesquiteDouble[6];
		for (int i = 0; i<6; i++){
			paramsExpl [i] = new MesquiteDouble();
			tempParams [i] = new MesquiteDouble();
		}
		conditionOnSurvival = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Condition on Survival", MesquiteModule.makeCommand("conditionOnSurvival", this), conditionOnSurvival);
		addMenuItem("Steps per Branch...", makeCommand("setStepCount", this));
		addMenuItem("Show Parameters Explorer", makeCommand("showParamExplorer",this));
		addCheckMenuItem(null, "Intermediates to console", makeCommand("toggleIntermediatesToConsole",this), intermediatesToConsole);
		rootModes = new StringArray(2);  
		rootModes.setValue(ROOT_IGNOREPRIOR, "Ignore Root State Frequencies");  //the strings passed will be the menu item labels
		rootModes.setValue(ROOT_USEPRIOR, "Use Root State Frequencies as Prior");
		rootModeName = new MesquiteString(rootModes.getValue(rootMode));  //this helps the menu keep track of checkmenuitems

		//if (showRootModeChoices && getHiredAs() != MargLikeAncStCLForModel.class){
		/*Treatment of prior at root; currently user interface hidden unless preferences file put in place*/
		//	MesquiteSubmenuSpec mssr = addSubmenu(null, "Root Reconstruction", makeCommand("setRootMode", this), rootModes); 
		//	mssr.setSelected(rootModeName);
		/**/
		MesquiteSubmenuSpec mLO = addSubmenu(null, "Likelihood Optimization", null); 
		addItemToSubmenu(null, mLO, "Underflow Checking...", makeCommand("setUnderflowCheckFreq", this));

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
		temp.addLine("setUnderflowCheckFreq " + underflowCheckFrequency);
		temp.addLine("setStepCount " + stepCount);
		temp.addLine("conditionOnSurvival  " + conditionOnSurvival.toOffOnString());
		if (explorer != null)
			temp.addLine("showParamExplorer ", explorer);
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
		else if (checker.compare(getClass(), "Sets the number of steps per branch", "[integer, 1 or greater]", commandName, "setStepCount")) {
			double steps = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(steps) && !commandRec.scripting())
				steps = MesquiteDouble.queryDouble(containerOfModule(), "Steps per branch", "Number of divisions of each branch for numerical integration.  Higher numbers mean the calculations are more accurate but go more slowly.  Values under 100 are not recommended", stepCount, 10, 1000000);

			if (MesquiteDouble.isCombinable(steps) && steps >=0 && steps!=stepCount){
				stepCount = steps; //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to condition by survival", "[on; off]", commandName, "conditionOnSurvival")) {
			conditionOnSurvival.toggleValue(new Parser().getFirstToken(arguments));
			parametersChanged(null, commandRec);
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
		return parameters;
	}
	MesquiteNumber likelihood = new MesquiteNumber();
	public double calculate(MesquiteString resultString, CommandRecord commandRec){
		for (int i = 0; i<6; i++)
			paramsExpl[i].setValue(parameters[i].getValue());
		calculateLogProbability( lastTree,  lastCharDistribution, paramsExpl, likelihood, resultString, commandRec);
		return likelihood.getDoubleValue();
	}
	public void restoreAfterExploration(){
	}
	/*------------------------------------------------------------------------------------------*/
	int lastMaxState = 1;
	double[] doubleParams = new double[6];
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
		double result =  logLike((Tree)((Object[])bundle)[0], (CategoricalDistribution)((Object[])bundle)[1], params);
		if (count++ % 10 == 0)
			CommandRecord.getRecSIfNull().tick("Evaluating: likelihood " + result + " params " + DoubleArray.toString(params));
		//if (count % 10 ==0)
		//	Debugg.println("Evaluating: likelihood " + result + " params " + DoubleArray.toString(params));
		if (!MesquiteDouble.isCombinable(result))
			result = 1e100;
		return result;
	}
	/*.................................................................................................................*/
	public double evaluate(MesquiteDouble param, Object bundle){
		if (!param.isCombinable() || param.getValue()<0)
			return 1e100;
				Object[] b = ((Object[])bundle);
		Tree tree = (Tree)b[0];
		CategoricalDistribution states = (CategoricalDistribution)b[1];
		double[] params = (double[])b[2];

		return 0;
	}
	/*.................................................................................................................*/
	public double logLike(Tree tree, CategoricalDistribution states, double[] params) {  
		if (speciesModel==null)
			return MesquiteDouble.unassigned;
		speciesModel.setParams(params);
		int root = tree.getRoot(deleted);
		initProbs(tree.getNumNodeSpaces(),lastMaxState);
		double logComp = downPass(root, tree,speciesModel, solver, states);
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
	
	/*.................................................................................................................*/
	public void calculateLogProbability(Tree tree, CharacterDistribution obsStates, MesquiteDouble[] params, MesquiteNumber prob, MesquiteString resultString, CommandRecord commandRec) {  
		if (speciesModel==null || obsStates==null || prob == null)
			return;
		lastTree = tree;
		lastCharDistribution = obsStates;
		prob.setToUnassigned();
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		boolean estimated = false;
		int workingMaxState;
		if (observedStates.getMaxState() <= 0){
			MesquiteMessage.warnProgrammer("Character Distribution appears to be constant; cannot calculated likelihood of tree and character");
			return;
		}
		else
			workingMaxState = observedStates.getMaxState()+1; 
		lastMaxState = workingMaxState;
		speciesModel.getParams(tempParams);
		speciesModel.setParams(params);
		
		for (int i = 0; i< 6; i++)
			doubleParams[i] = params[i].getValue();
		
		double negLogLikelihood = 0;
		String modelString = "";
		if (speciesModel.isFullySpecified()){
			negLogLikelihood = logLike(tree, observedStates, doubleParams);
			modelString  = speciesModel.toString();
		}
		else {
				Optimizer opt = new Optimizer(this);
				Object[] bundle = new Object[] {tree, observedStates, doubleParams};
				stepCount = 100;
			double[] suggestions = new double[]{1, 0.5, 1, 0.5, 0.1, 0.1};
			logln("Sp/Ext Categ Char: Estimating parameters, phase 1: step count 100");
			negLogLikelihood = opt.optimize(suggestions, bundle);
			stepCount = 1000;
			logln("Sp/Ext Categ Char: Estimating parameters, phase 2: step count 1000");
			negLogLikelihood = opt.optimize(suggestions, bundle);
			
			speciesModel.setParams(suggestions);
			modelString  = speciesModel.toString() + " [est.]";

		}

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
		speciesModel.setParams(tempParams);
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

class SpecExtincCategModel implements DESystem {


	private double e0;   //extinction rate in state 0
	private double s0;   //speciation rate in state 0
	private double e1;   //extinction rate in state 1
	private double s1;   //speciation rate in state 1
	private double t01;  //transition rate from state 0 to state 1
	private double t10;  //transition rate from state 1 to state 0


	public SpecExtincCategModel(double e0, double s0, double e1, double s1, double t01, double t10){
		this.e0 = e0;
		this.s0 = s0;
		this.e1 = e1;
		this.s1 = s1;
		this.t01 = t01;
		this.t10 = t10;
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
		return "Model s0=" + MesquiteDouble.toString(s0, 4) +" s1=" + MesquiteDouble.toString(s1, 4) +" e0=" + MesquiteDouble.toString(e0, 4) +" e1=" + MesquiteDouble.toString(e1, 4) +" t01=" + MesquiteDouble.toString(t01, 4) +" t10=" + MesquiteDouble.toString(t10, 4);
	}
	public double[]calculateDerivative(double t,double probs[],double[] result){
		// for clarity
//Debugg.println("probs " + probs.length + " result " + result.length);
		double extProb0 = probs[0];
		double extProb1 = probs[1];
		double dataProb0 = probs[2];
		double dataProb1 = probs[3];
		result[0] = -(e0+t01+s0)*extProb0 + s0*extProb0*extProb0 + e0 + t01*extProb1; 
		result[1] = -(e1+t10+s1)*extProb1 + s1*extProb1*extProb1 + e1 + t10*extProb0;            
		result[2] = -(e0+t01+s0)*dataProb0 + 2*s0*extProb0*dataProb0 + t01*dataProb1;
		result[3] = -(e1+t10+s1)*dataProb1 + 2*s1*extProb1*dataProb1 + t10*dataProb0;
		return result;
	}

	public boolean isFullySpecified(){
		return (MesquiteDouble.isCombinable(s0) && MesquiteDouble.isCombinable(s1) &&MesquiteDouble.isCombinable(e0) &&MesquiteDouble.isCombinable(e1) &&MesquiteDouble.isCombinable(t01) &&MesquiteDouble.isCombinable(t10));
	}
	
	public void setParams(MesquiteDouble[] params){
		this.s0 = params[0].getValue();
		this.s1 = params[1].getValue();
		this.e0 = params[2].getValue();
		this.e1 = params[3].getValue();
		this.t01 = params[4].getValue();
		this.t10 = params[5].getValue();
	}
	public void setParams(double[] params){
		this.s0 = params[0];
		this.s1 = params[1];
		this.e0 = params[2];
		this.e1 = params[3];
		this.t01 = params[4];
		this.t10 = params[5];
	}

	public void getParams(MesquiteDouble[] params){
		params[0].setValue(s0);
		params[1].setValue(s1);
		params[2].setValue(e0);
		params[3].setValue(e1);
		params[4].setValue(t01);
		params[5].setValue(t10);
	}
	public double getSRate(int state) {
		if (state == 0)
			return s0;
		else if (state == 1)
			return s1;
		else return MesquiteDouble.unassigned;
	}

	public double getERate(int state) {
		if (state == 0)
			return e0;
		else if (state == 1)
			return e1;
		else return MesquiteDouble.unassigned;
	}

}


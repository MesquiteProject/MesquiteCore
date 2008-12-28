/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.SpecExtincMLCalculator;

import java.util.Vector;

import mesquite.diverse.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.ParametersExplorer;

public class SpecExtincMLCalculator extends MesquiteModule implements ParametersExplorable, Evaluator {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ParametersExplorer.class, getName() + "  uses a Parameters Explorer to show likelihood surfaces.",
		"The parameter explorer is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}

	// bunch of stuff copied from zMargLikeCateg - need to prune!!

	double [] probsExt, probsData;
	double[] yStart;  //initial value for numerical integration

	MesquiteNumber probabilityValue;

	long underflowCheckFrequency = -1; //2;  //how often to check that not about to underflow; 1 checks every time
	long underflowCheck = 1;
	double underflowCompensation = 1;
	MesquiteNumber minChecker;

	// Number of steps per branch, reduce for a faster, possibily sloppier result
	double stepCount = 1000;  //default 10000

	MesquiteBoolean intermediatesToConsole = new MesquiteBoolean(false);
	MesquiteBoolean conditionOnSurvival;

	boolean[] deleted = null;
	SpecExtincModel model;
	DEQNumSolver solver;

	MesquiteParameter sp = new MesquiteParameter(); //used only for parameter exploration
	MesquiteParameter ep = new MesquiteParameter();//used only for parameter exploration
	MesquiteParameter[] parameters;
	ParametersExplorer explorer;
	MesquiteNumber[] paramsToReport = new MesquiteNumber[2];
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		solver = new RK4Solver();
		probabilityValue = new MesquiteNumber();
		minChecker = new MesquiteNumber(MesquiteDouble.unassigned);
		model = new SpecExtincModel(MesquiteDouble.unassigned, MesquiteDouble.unassigned);
		conditionOnSurvival = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Condition on Survival", MesquiteModule.makeCommand("conditionOnSurvival", this), conditionOnSurvival);
		addMenuItem("Show Parameters Explorer", makeCommand("showParamExplorer",this));
		addMenuItem("Steps per Branch...", makeCommand("setStepCount", this));
		addCheckMenuItem(null, "Intermediates to console", makeCommand("toggleIntermediatesToConsole",this), intermediatesToConsole);

		MesquiteSubmenuSpec mLO = addSubmenu(null, "Likelihood Optimization", null); 
		addItemToSubmenu(null, mLO, "Underflow Checking...", makeCommand("setUnderflowCheckFreq", this));
		//following is for the parameters explorer
		sp.setName("lambda");
		sp.setExplanation("Rate of speciation");
		sp.setMinimumAllowed(0);
		sp.setMaximumAllowed(MesquiteDouble.infinite);
		sp.setMinimumSuggested(0.0000);
		sp.setMaximumSuggested(1);
		sp.setValue(0.1);
		ep.setName("mu");
		ep.setExplanation("Rate of extinction");
		ep.setMinimumSuggested(0.0000);
		ep.setMaximumSuggested(1);
		ep.setMinimumAllowed(0);
		ep.setMaximumAllowed(MesquiteDouble.infinite);
		ep.setValue(0.01);
		parameters = new MesquiteParameter[]{sp, ep};
		paramsToReport[0] = new MesquiteNumber();
		paramsToReport[0].setName("lambda (sp. rate)");
		paramsToReport[1] = new MesquiteNumber();
		paramsToReport[1].setName("mu (ext. rate)");
		return true;
	}
	public void employeeQuit(MesquiteModule employee){
		if (employee == explorer)
			explorer = null;
	}


	boolean reportCladeValues = false;
	public void setReportCladeLocalValues(boolean reportCladeValues){
		this.reportCladeValues = reportCladeValues;
	}
	public boolean getReportCladeLocalValues(){
		return reportCladeValues;
	}
	public Snapshot getSnapshot(MesquiteFile file) {
		final Snapshot temp = new Snapshot();
		temp.addLine("setUnderflowCheckFreq " + underflowCheckFrequency);
		temp.addLine("setStepCount " + stepCount);
		temp.addLine("conditionOnSurvival  " + conditionOnSurvival.toOffOnString());
		if (explorer != null)
			temp.addLine("showParamExplorer ", explorer);
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
		else if (checker.compare(getClass(), "Show Parameter Explorer", "", commandName, "showParamExplorer")) {
			explorer = (ParametersExplorer)hireEmployee(ParametersExplorer.class, "Parameters explorer");
			if (explorer == null)
				return null;
			// explorer.makeMenu("Parameters");
			explorer.setExplorable(this);
			return explorer;
		}
		else if (checker.compare(this.getClass(), "Sets whether to condition by survival", "[on; off]", commandName, "conditionOnSurvival")) {
			conditionOnSurvival.toggleValue(new Parser().getFirstToken(arguments));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(getClass(), "Sets the number of steps per branch", "[integer, 1 or greater]", commandName, "setStepCount")) {
			double steps = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(steps) && !MesquiteThread.isScripting())
				steps = MesquiteDouble.queryDouble(containerOfModule(), "Steps per branch", "Number of divisions of each branch for numerical integration.  Higher numbers mean the calculations are more accurate but go more slowly.  Values under 100 are not recommended", stepCount, 10, 1000000);

			if (MesquiteDouble.isCombinable(steps) && steps >=0 && steps!=stepCount){
				stepCount = steps; //change mode
				if (!MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(),"Sets whether to write intermediate branch values to console","[on; off]", commandName, "toggleIntermediatesToConsole")){
			intermediatesToConsole.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*---------------------------------------------------------------------------------*/
	public void parametersChangedNotifyExpl(Notification n){
		if (!MesquiteThread.isScripting())
			parametersChanged(n);

		if (explorer != null)
			explorer.explorableChanged(this);
	}
	/*.................................................................................................................*/
	private void initProbs(int nodes) {
		if (probsData==null || probsData.length!=nodes){
			probsData = new double[nodes];
			probsExt = new double[nodes];
			yStart = new double[2];
		}
		DoubleArray.zeroArray(probsData);
		DoubleArray.zeroArray(probsExt);
		DoubleArray.zeroArray(yStart);
	}


	/*.................................................................................................................*/
	/* assumes hard polytomies */

	// This might hold the intermediate step results if requested for debugging
	Vector integrationResults = null;

	/* now returns underflow compensation */
	private double downPass(int node, Tree tree, SpecExtincModel model, DEQNumSolver solver) {
		double logComp;
		double d = 1;
		double e = 0;
		if (tree.nodeIsTerminal(node)) { //initial conditions from observations if terminal
			e = 0;
			d = 1;
			logComp = 0.0;  // no compensation required yet
		}
		else { //initial conditions from daughters if internal
			logComp = 0.0;
			for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
				logComp += downPass(nd,tree,model,solver);
			}

			d = 1;  //two here to anticipate two daughters???
			e = 1;
			//TODO: either filter to permit only ultrametric, or redo calculations to permit extinct nodes.
			//TODO: likewise for polytomies
			for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
				e = probsExt[nd];
				d *= probsData[nd];
			}

			if (node != tree.getRoot()){  //condition on splitting at root; thus don't include rate if at root
				d *= model.getS();
			}


			if (underflowCheckFrequency>=0 && ++underflowCheck % underflowCheckFrequency == 0){
				if (d  < 0.01) {
					logComp +=  -Math.log(d);
					d = 1.0;
				}
			}
		}
		if (node == tree.getRoot()){
			probsExt[node]= e;
			probsData[node] = d;

			return logComp;
		}
		else{
			double x = 0;
			double length = tree.getBranchLength(node,1.0,deleted);
			double h = length/stepCount;       //this will need tweaking!

			yStart[0] = e;
			yStart[1] = d;

			if (intermediatesToConsole.getValue()){
				MesquiteMessage.print("node " + node);
				if (node == tree.getRoot())
					MesquiteMessage.println(" is root");
				else
					MesquiteMessage.println("");
				MesquiteMessage.println("At start, y is " + DoubleArray.toString(yStart));
			}
			integrationResults = solver.integrate(x,yStart,h,length,model,integrationResults,intermediatesToConsole.getValue());    
			if (integrationResults == null)
				return MesquiteDouble.unassigned;
			double[] yEnd = (double[])integrationResults.lastElement();
			if (yEnd.length == 2){

				probsExt[node] = yEnd[0];
				probsData[node] = yEnd[1];

			}
			else {
				MesquiteMessage.warnProgrammer("Vector returned by solver not the same size supplied!");

				probsExt[node]=0;   //is this the right thing here?
				probsData[node]=0;  //this is probably the best choice here

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
	public void restoreAfterExploration(){
	}
	MesquiteNumber likelihood = new MesquiteNumber();
	MesquiteDouble lamdaExp = new MesquiteDouble();
	MesquiteDouble muExp = new MesquiteDouble();

	public double calculate(MesquiteString resultString){
		lamdaExp.setValue(sp.getValue());
		muExp.setValue(ep.getValue());
		calculateLogProbability( lastTree, likelihood, lamdaExp, muExp, resultString);
		return likelihood.getDoubleValue();
	}
	Tree lastTree;

	/*.................................................................................................................*/
	MesquiteDouble tempLambda = new MesquiteDouble();
	MesquiteDouble tempMu = new MesquiteDouble();
	long count = 0;
	/*.................................................................................................................*/
	boolean anyNegativeOrUncombinable(double[] params){
		if (params == null)
			return false;
		for (int i=0; i<params.length; i++) {
			if (params[i]<0)
				return true;
			else if (!MesquiteDouble.isCombinable(params[i]))
					return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public double evaluate(double[] params, Object bundle){
		if (anyNegativeOrUncombinable(params))
			return 1e100;
		if (params.length == 2) {
			double result = logLike((Tree)((Object[])bundle)[0], params[0], params[1]);
			if (count++ % 10 == 0)
				CommandRecord.tick("Evaluating: -log likelihood " + MesquiteDouble.toStringDigitsSpecified(result, 4) + "  lambda " + params[0] + " mu " + params[1]);
			return result;
		}
		final Object[] b = ((Object[])bundle);
		Tree tree = (Tree)b[0];
		MesquiteDouble lambda = (MesquiteDouble)b[1];
		MesquiteDouble mu = (MesquiteDouble)b[2];

		double result = 0;
		if (lambda.isUnassigned())
			result = logLike(tree, params[0], mu.getValue());
		else if (mu.isUnassigned())
			result = logLike(tree, lambda.getValue(), params[0]);
		else
			result = MesquiteDouble.unassigned;
		if (!MesquiteDouble.isCombinable(result) || result < -1e100 || result > 1e100)
			result = 1e100;
		if (count++ % 10 == 0)
			CommandRecord.tick("Evaluating: -log likelihood " + MesquiteDouble.toStringDigitsSpecified(result, 4) + "  lambda " + lambda + " mu " + mu);
		return result;
	}
	/*.................................................................................................................*/
	public double evaluate(MesquiteDouble param, Object bundle){
		if (!param.isCombinable() || param.getValue()<0)
			return 1e100;
		final Object[] b = ((Object[])bundle);
		Tree tree = (Tree)b[0];
		MesquiteDouble lambda = (MesquiteDouble)b[1];
		MesquiteDouble mu = (MesquiteDouble)b[2];
		
		double result = 0;
		if (lambda.isUnassigned())
			result = logLike(tree, param.getValue(), mu.getValue());
		else if (mu.isUnassigned())
			result = logLike(tree, lambda.getValue(), param.getValue());
		else
			result = MesquiteDouble.unassigned;
		if (!MesquiteDouble.isCombinable(result) || result < -1e100 || result > 1e100)
			result = 1e100;
		if (count++ % 10 == 0)
			CommandRecord.tick("Evaluating: -log likelihood " + MesquiteDouble.toStringDigitsSpecified(result, 4) + "  lambda " + lambda + " mu " + mu);
		return result;
	}

	/*.................................................................................................................*/
	public double logLike(Tree tree, double lambda, double mu) {  
		if (model==null)
			return MesquiteDouble.unassigned;
		model.setE(mu);
		model.setS(lambda);
		initProbs(tree.getNumNodeSpaces());
		final int root = tree.getRoot(deleted);
		double  logComp = downPass(root, tree, model, solver);
		double likelihood = 0.0;
		if (conditionOnSurvival.getValue())
			likelihood = probsData[root]/(1-probsExt[root])/(1-probsExt[root]);
		else
			likelihood = probsData[root];
		return (-(Math.log(likelihood) - logComp));
	}
	/*.................................................................................................................*/
	public void calculateLogProbability(Tree tree, MesquiteNumber prob, MesquiteDouble lambda, MesquiteDouble mu, MesquiteString resultString) {  
		if (model==null ||  prob == null || lambda ==null || mu == null)
			return;
		lastTree = tree;
		prob.setToUnassigned();
		String estimatedLambda = "";
		String estimatedMu = "";

		double negLogLikelihood = MesquiteDouble.unassigned;
		//if lambda and mu are not both specified, then estimate
		if (lambda.isUnassigned() || mu.isUnassigned()){
			double currentE = model.getE();
			double currentS = model.getS();
			final Optimizer opt = new Optimizer(this);
			final Object[] bundle = new Object[] {tree, lambda, mu};
			if (lambda.isUnassigned() && mu.isUnassigned()){ //both unassigned
				double[] suggestions = new double[]{1, 1};
				logln("Sp/Ext: Estimating parameters");
				negLogLikelihood = opt.optimize(suggestions, bundle);
				lambda.setValue(suggestions[0]);
				mu.setValue(suggestions[1]);
				estimatedLambda = "[est.]";
				estimatedMu = "[est.]";
			}
			else { //one of two unassigned
				/*
					double[] suggestions = new double[]{1};
					negLogLikelihood = opt.optimize(suggestions, bundle);
					lambda.setValue(suggestions[0]);
					/**/
				/**/
				MesquiteDouble suggestion = new MesquiteDouble(1);
				double currentStep = stepCount;
				stepCount = 100;
				logln("Sp/Ext: Estimating parameters, phase 1: step count 100");
				negLogLikelihood = opt.optimize(suggestion, 0.0, 10, bundle);
				logln("Sp/Ext: neg. Log Likelihood first attempt:" + negLogLikelihood);
				double best = negLogLikelihood;
				double first = suggestion.getValue();
				negLogLikelihood = opt.optimize(suggestion, 0.0, 100, bundle);
				logln("Sp/Ext: neg. Log Likelihood second attempt:" + negLogLikelihood);
				if (best < negLogLikelihood)
					suggestion.setValue(first);
				stepCount = 1000;
				logln("Sp/Ext: Estimating parameters, phase 2: step count 1000");
				negLogLikelihood = opt.optimize(suggestion, suggestion.getValue() * 0.6, suggestion.getValue() * 1.4, bundle);
				logln("Sp/Ext: neg. Log Likelihood final attempt:" + negLogLikelihood);
				stepCount = currentStep;
				if (lambda.isUnassigned()){
					lambda.setValue(suggestion.getValue());
					/**/
					estimatedLambda = "[est.]";
				}
				else {
					mu.setValue(suggestion.getValue()); /**/
					estimatedMu = "[est.]";
				}
			}
			model.setE(currentE);
			model.setS(currentS);
		}
		else 
			negLogLikelihood = logLike(tree, lambda.getValue(), mu.getValue());
		/*======*
		model.setE(mu.getValue());
		model.setS(lambda.getValue());
        initProbs(tree.getNumNodeSpaces());
		int root = tree.getRoot(deleted);
		double  logComp = downPass(root, tree,model, solver);
		double likelihood = 0.0;
		if (conditionOnSurvival.getValue())
			likelihood = probsData[root]/(1-probsExt[root])/(1-probsExt[root]);
		else
			likelihood = probsData[root];

		double negLogLikelihood = -(Math.log(likelihood) - logComp);
		/*======*/

		if (prob!=null){
			prob.setValue(negLogLikelihood);
			paramsToReport[0].setValue(lambda.getValue());
			paramsToReport[1].setValue(mu.getValue());
			prob.copyAuxiliaries(paramsToReport);
			prob.setName("Speciation/Extinction -lnLikelihood");
	}
	double likelihood = Math.exp(-negLogLikelihood);
		if (MesquiteDouble.isUnassigned(negLogLikelihood))
			likelihood = MesquiteDouble.unassigned;
		if (resultString!=null) {
			String s = "Sp/Ext Likelihood with spec. rate " + lambda + " " + estimatedLambda + "; ext. rate " + mu + " " + estimatedMu + ";  -log L.:"+ MesquiteDouble.toString(negLogLikelihood) + " [L. "+ MesquiteDouble.toString(likelihood) + "]";
			s += "  " + getParameters();
			resultString.setValue(s);
		}
	}


	public Class getDutyClass() {
		return SpecExtincMLCalculator.class;
	}

	public String getName() {
		return "Speciation/extinction Likelihood Calculator";
	}
	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "1.0";
	}

	public String getExplanation(){
		return "Calculates likelihoods using a speciation/extinction model reduced from the BiSSE model (Maddison, Midford & Otto 2007) ";
	}

	public boolean isPrerelease(){
		return false;
	}


}

class SpecExtincModel implements DESystem {

	private double e;   //extinction rate in state 0
	private double s;   //speciation rate in state 0

	public SpecExtincModel(double e, double s){
		this.e = e;
		this.s = s;
	}

	public boolean isFullySpecified(){
		return MesquiteDouble.isCombinable(e) && MesquiteDouble.isCombinable(s);
	}

	/*
	 *  The probability values are passed the probs array in the following order
	 *  probs[0] = E0 = extinction probability in lineage starting at t in the past at state 0
	 *  probs[1] = P0 = probability of explaining the data, given system is in state 0 at time t
	 * @see mesquite.correl.lib.DESystem#calculateDerivative(double, double[])
	 */
	public String toString(){
		return "Speciation/Extinction Model lambda=" + MesquiteDouble.toStringDigitsSpecified(s, 4) + " mu=" + MesquiteDouble.toStringDigitsSpecified(e, 4);
	}

	/*.................................................................................................................*/
	public double[]calculateDerivative(double t,double probs[],double[] result){
		// for clarity
		final double extProb = probs[0];
		final double dataProb = probs[1];
		result[0] = -(e+s)*extProb + s*extProb*extProb + e; 
		result[1] = -(e+s)*dataProb + 2*s*extProb*dataProb;
		return result;
	}

	public void setE(double e){
		this.e = e;
	}

	public void setS(double s){
		this.s = s;
	}


	public double getS() {
		return s;
	}

	public double getE() {
		return e;
	}
}


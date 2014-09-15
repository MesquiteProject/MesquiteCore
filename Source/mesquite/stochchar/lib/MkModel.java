/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.stochchar.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

public class MkModel extends ProbPhenCategCharModel implements CModelEstimator, Evaluator {
	double rate = MesquiteDouble.unassigned;  
	boolean hasDefaultValues=true;
	boolean unassigned = true;
	MesquiteInteger pos = new MesquiteInteger(0);
	public MkModel (String name, Class dataClass){
		super(name, dataClass);
		maxStateDefined = CategoricalState.maxCategoricalState;
		maxState = CategoricalState.maxCategoricalState;
	}

	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		MkModel j = new MkModel(name, getStateClass());
		completeDaughterClone(formerClone, j);
		return j;

	}

	public void deassignParameters(){
		rate = MesquiteDouble.unassigned;
		unassigned = true;
	}
	public double evaluate(MesquiteDouble param, Object param2){
		if (param2 instanceof TreeDataModelBundle) {
			TreeDataModelBundle b = (TreeDataModelBundle)param2;
			rate = param.getValue();
			double result =  - b.getLikelihoodCalculator().logLikelihoodCalc(b.getTree(), this, b.getCharacterDistribution());
			return result;
		}
		else
			return 0;
	}

	public double evaluate(double[] x, Object param){
		if (x == null || x.length != 1)
			return 0;
		if (param instanceof TreeDataModelBundle) {
			TreeDataModelBundle b = (TreeDataModelBundle)param;
			rate = x[0];
			double result =  - b.getLikelihoodCalculator().logLikelihoodCalc(b.getTree(), this, b.getCharacterDistribution());
			return result;
		}
		else
			return 0;
	}
	/*.................................................................................................................*/
	MesquiteTimer timer = new MesquiteTimer();
	MesquiteInteger steps = new MesquiteInteger();
	public static double optWidth = 0.0;
	public void estimateParameters(Tree tree, CharacterDistribution observedStates, CLikelihoodCalculator lc){
		boolean constant = (observedStates.isConstant(tree, tree.getRoot()));

		if (constant || !(observedStates instanceof CategoricalDistribution)){
			rate = 0;
		}
		else {

			timer.start();
			Optimizer opt = new Optimizer(this);
			TreeDataModelBundle bundle = new TreeDataModelBundle(tree, this, observedStates, null, lc);
			MesquiteDouble r = new MesquiteDouble(0.1);

			if (optWidth > 0.0000001)
				progressiveOptimize(tree, bundle, opt, r, 0, optWidth);
			else {
				r.setValue(0.1);
				double best = progressiveOptimize(tree, bundle, opt, r, 0, 1);
				double b1Rate = r.getValue();

				r.setValue(0.1);
				double b10 = progressiveOptimize(tree, bundle, opt, r, 0, 10);

				if (best < b10)
					r.setValue(b1Rate);

				/*
	 			r.setValue(0.1);
				double best = progressiveOptimize(tree, bundle, opt, r, 0, 1);
	 			double b1Rate = r.getValue();

	 			r.setValue(0.1);
				double b10 = progressiveOptimize(tree, bundle, opt, r, 0, 10);

				if (best > b10)
					best = b10;

	 			r.setValue(10);
				double b1010 = progressiveOptimize(tree, bundle, opt, r, 0, 10);
				if (best > b1010)
					best = b1010;
				r.setValue(best);
				 */
			}

			rate = r.getValue();
			timer.end();
		}
	}


	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md){
		if (md == null || !(md instanceof MkModel))
			return;
		super.copyToClone(md);
		MkModel model = (MkModel)md;
		if (unassigned) {
			model.rate = MesquiteDouble.unassigned;
			model.unassigned = true;
		}
		else
			model.setInstantaneousRate(rate);
	}

	/** Returns whether parameters of model are fully specified.  If so, it can be used for evolving states.*/
	public boolean isFullySpecified(){
		return !unassigned && rate != MesquiteDouble.unassigned;
	}
	/*.................................................................................................................*/
	/** Performs command (for Commandable interface) */
	public Object doCommand(String commandName, String arguments, CommandChecker checker){
		if (checker.compare(this.getClass(), "Sets the instantaneous rate of change in the model", "[rate of change; must be > 0]", commandName, "setRate")) {
			if (isBuiltIn())
				return null;
			pos.setValue(0);
			String token = ParseUtil.getToken(arguments, pos);
			if (MesquiteString.explicitlyUnassigned(token)) {
				setInstantaneousRate(MesquiteDouble.unassigned);
				return null;
			}
			pos.setValue(0);
			double newRate = MesquiteDouble.fromString(arguments, pos);
			double a = getInstantaneousRate();
			if (!MesquiteDouble.isCombinable(newRate)) {
				newRate= MesquiteDouble.queryDouble(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Set Rate", "Rate of change:", a);
			}
			if (newRate>=0  && newRate!=a && MesquiteDouble.isCombinable(newRate)) {
				setInstantaneousRate(newRate);
			}
		}
		else if (checker.compare(this.getClass(), "Sets the maximum allowed state for this model", "[0...maxCategoricalState]", commandName, "setMaxState")) {
			if (isBuiltIn())
				return null;
			int m = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(m))
				m = MesquiteInteger.queryInteger(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Set maximum allowed state", "Maximum allowed state for this model:", maxStateDefined);
			if (m>0 && m<=CategoricalState.maxCategoricalState  && m!=maxStateDefined) {
				maxState = m;
				maxStateDefined = m;
				notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
			}
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/** returns name of model class (e.g. "stepmatrix")  SHOULD MATCH NAME returned by getNEXUSNameOfModelClass of module*/
	public String getNEXUSClassName() {
		return "Mk1";
	}
	public void fromString (String description, MesquiteInteger stringPos, int format) { 
		hasDefaultValues = false;
		ParseUtil.getToken(description, stringPos);
		rate =  MesquiteDouble.fromString(description, stringPos);
		unassigned = !MesquiteDouble.isCombinable(rate);
		String s = ParseUtil.getToken(description, stringPos);
		int m =  MesquiteInteger.fromString(description, stringPos);
		if (MesquiteInteger.isCombinable(m) && "maxState".equalsIgnoreCase(s)) {
			maxState = m;
			maxStateDefined = m;
		}
		else {
			maxState = CategoricalState.maxCategoricalState;
			maxStateDefined = CategoricalState.maxCategoricalState;
		}
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN),CharacterModel.class, true);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN),CharacterModel.class, false);
	}

	public String getParameters () {
		String result ="rate " + MesquiteDouble.toString(rate);
		if (unassigned)
			result += " [est.]";
		return result;
	}
	public String toString () {
		return "Mk1 model (" + getID() + ") " + getParameters();
	}
	MesquiteNumber[] forExport;
	/* --------------------------------------------- */
	public MesquiteNumber[] getParameterValues() {
		if (forExport == null){
			forExport = new MesquiteNumber[1];
			forExport[0] = new MesquiteNumber();
		}

		forExport[0].setName("rate");
		forExport[0].setValue(rate);
		return forExport;
	}
	public String getExplanation () {
		return "One-parameter Markov k-state model (Lewis, 2001); a generalization of the Jukes-Cantor model.";
	}
	public String getNexusSpecification() {
		String s= " ";
		if (!StringUtil.blank(getAnnotation()))
			s +=" [!" + getAnnotation() + "] ";
		if (unassigned)
			s += "rate ? ";
		else
			s += "rate " + MesquiteDouble.toString(rate);
		if (maxStateDefined != CategoricalState.maxCategoricalState && maxStateDefined>=0)
			s += " maxState " + maxStateDefined;
		return s;
	}
	/** Return an explanation of the settings used in calculations (e.g., optimization settings).  These are not the parameters of the model. */
	public String getSettingsString(){
		return "(Opt.:  width " + MesquiteDouble.toString(optWidth) + ")";
	}
	/* ---------------------------------------------*/
	public double instantaneousRate (int beginState, int endState, Tree tree, int node){
		if (beginState <0 || beginState> maxState || endState < 0 || endState> maxState) {
			return 0;
		}
		else {
			if (beginState == endState)
				return -(maxState)*rate; //so row of rate matrix adds to 0
			return rate;
		}
	}
	/* ---------------------------------------------*/
	public double getInstantaneousRate() {
		return rate;
	}
	public void setInstantaneousRate(double r) {
		setInstantaneousRate(r, true);
	}
	public void setInstantaneousRate(double r, boolean notify) {
		hasDefaultValues = false;
		rate = r;
		unassigned = !MesquiteDouble.isCombinable(r);
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
	private double instantaneousToProb(double instantaneous, double branchLength, int numStates) {
		if (MesquiteDouble.isUnassigned(instantaneous)) {
			MesquiteMessage.warnProgrammer("Instantaneous rate is unassigned in model " + getName());
			return 0;
		}
		return 1.0/(numStates)*(1.0- Math.exp(-instantaneous*numStates*branchLength));

		/* in 0.994 and before the following was used:
		return 1.0/(numStates)*(1.0- Math.exp(-instantaneous*numStates/(numStates-1)*branchLength));
		This assumes "instantaneous" is the overall rate of change, regardless of to what state the chance occurs,
		as opposed to rate of each particular change (the latter in the style of the q's of Schluter et al & Pagel)
		 */
	}

	public int evolveState (int beginState, Tree tree, int node) {
		double r = randomNumGen.nextDouble();
		double probChangeEach =instantaneousToProb(rate, tree.getBranchLength(node, 1.0), (maxState +1));
		double probStasis =1.0 -(probChangeEach * (maxState));
		double accumProb = 0;
		for (int i=0; i<=maxState; i++) {
			if (i== beginState)
				accumProb +=  probStasis;
			else
				accumProb +=  probChangeEach;
			if (r< accumProb)
				return i;
		}
		return maxState;
	}
	public double transitionProbability (int beginState, int endState, Tree tree, int node){
		if (!inStates(beginState) || !inStates(endState)) {
			return 0;
		}
		else if (beginState == endState) {
			if (maxState == -1)
				return 1.0;
			else
				return (1.0 -(instantaneousToProb(rate, tree.getBranchLength(node, 1.0), (maxState+1)) * (maxState)));
		}
		else {
			return (instantaneousToProb(rate, tree.getBranchLength(node, 1.0), (maxState+1)));
		}
	}

	public String getModelTypeName(){
		return "Markov k-state 1 param.";
	}
}


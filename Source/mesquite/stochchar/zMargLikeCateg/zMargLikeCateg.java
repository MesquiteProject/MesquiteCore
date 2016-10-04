/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.stochchar.zMargLikeCateg;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class zMargLikeCateg extends MargLikeAncStCLForModel implements MesquiteListener, CLikelihoodCalculator {
	//CategoricalDistribution observedStates;
	double[][] downProbs, upProbs, finalProbs;  // probability at node N given state s at base
	double[] underflowCompDown, underflowCompUp;  // ln of compensation for underflow
	double[] empirical;
	MesquiteNumber probabilityValue;
	int numStates;
	ProbabilityCategCharModel tempModel;
	ProbabilityCategCharModel passedModel;
	long underflowCheckFrequency = 2;  //how often to check that not about to underflow; 1 checks every time
	long underflowCheck = 1;
	MesquiteNumber minChecker;

	//In version 1.1. the assumption about the root prior for model estimation, ancestral state reconstruction and simulation is assumed to be embedded in the model
	//Thus, the control is removed here
	public static final int ROOT_IGNOREPRIOR = 0;  // likelihoodignore's model's prior
	public static final int ROOT_USEPRIOR = 1;  // calculates ancesteral states imposing model's prior
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

	boolean fillFrequencies = true;
	double decisionThreshold = 2.0;
	boolean reportRootMode = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		probabilityValue = new MesquiteNumber();
		minChecker = new MesquiteNumber(MesquiteDouble.unassigned);


		reportModes = new StringArray(3);  
		if (getHiredAs() == MargLikeAncStForModel.class){
			reportModes.setValue(REPORT_Proportional, "Proportional Likelihoods");  //the strings passed will be the menu item labels
			reportModes.setValue(REPORT_Raw, "Raw Likelihoods");
			reportModes.setValue(REPORT_Log, "Negative Log Likelihoods");
			reportModeName = new MesquiteString(reportModes.getValue(reportMode));  //this helps the menu keep track of checkmenuitems
			MesquiteSubmenuSpec mss = addSubmenu(null, "Report Likelihoods As", makeCommand("setReportMode", this), reportModes); 
			mss.setSelected(reportModeName);

		}
		addMenuItem("Likelihood Decision Threshold...", makeCommand("setDecision", this));
		rootModes = new StringArray(2);  
		rootModes.setValue(ROOT_IGNOREPRIOR, "Ignore Root State Frequencies");  //the strings passed will be the menu item labels
		rootModes.setValue(ROOT_USEPRIOR, "Use Root State Frequencies as Prior");
		rootModeName = new MesquiteString(rootModes.getValue(rootMode));  //this helps the menu keep track of checkmenuitems

		if (showRootModeChoices && getHiredAs() != MargLikeAncStCLForModel.class){
			/*Treatment of prior at root; currently user interface hidden unless preferences file put in place*/
			MesquiteSubmenuSpec mssr = addSubmenu(null, "Root Reconstruction", makeCommand("setRootMode", this), rootModes); 
			mssr.setSelected(rootModeName);
			/**/
		}

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
	/*public void setRootMode(int newMode){
		if (newMode >=0 && newMode!=rootMode){
			rootMode = newMode; //change mode
			rootModeName.setValue(rootModes.getValue(rootMode)); //so that menu item knows to become checked
			parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
		}
	}
	public int getRootMode(){
		return rootMode;
	}
	/*.................................................................................................................*
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0 && prefs[0] !=null  && prefs[0].length()>0) {
			showRootModeChoices = ('y' == prefs[0].charAt(0));
		}
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {

		Snapshot temp = new Snapshot();
		if (getHiredAs() == MargLikeAncStForModel.class){
			temp.addLine("setReportMode " + ParseUtil.tokenize(reportModes.getValue(reportMode)));
			temp.addLine("setRootMode "  + ParseUtil.tokenize(rootModes.getValue(rootMode)));
			/**/
		}
		temp.addLine("setDecision " + decisionThreshold);
		/**/
		temp.addLine("setUnderflowCheckFreq " + underflowCheckFrequency);
		return temp;
	}


	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the threshold T for deciding whether ancestral state is included among estimates; states with likelihoods T times less than best are not included in estimate", "[threshold]", commandName, "setDecision")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			double T = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(T))
				T = MesquiteDouble.queryDouble(containerOfModule(), "Likelihood Decision Threshold", "Indicate the threshold T for deciding whether ancestral state is included among estimates; states with log likelihoods higher (worse) than the best by T or more are not included in estimate.  This is relevant when likelihoods are not merely reported but a decision is made, e.g. in Trace Character History if Display Proportional To Weights is not selected, or in Trace Character Over Trees.", decisionThreshold, 0.00000001, 1000000000000.0);
			if (!MesquiteDouble.isCombinable(T))
				return null;
			if (decisionThreshold != T) {
				decisionThreshold = T;
				if (!reportCladeValues)
					parametersChanged(); 
			}
		}
		else if (checker.compare(getClass(), "Sets the report mode", null, commandName, "setReportMode")) {
			if (getHiredAs() == MargLikelihoodForModel.class)
				return null;
			String name = parser.getFirstToken(arguments); //get argument passed of option chosen
			int newMode = reportModes.indexOf(name); //see if the option is recognized by its name
			if (newMode >=0 && newMode!=reportMode){
				reportMode = newMode; //change mode
				reportModeName.setValue(reportModes.getValue(reportMode)); //so that menu item knows to become checked
				if (!reportCladeValues)
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets the root mode", null, commandName, "setRootMode")) {
			String name = parser.getFirstToken(arguments); //get argument passed of option chosen
			int newMode = rootModes.indexOf(name); //see if the option is recognized by its name
			if (newMode >=0 && newMode!=rootMode){
				rootMode = newMode; //change mode
				rootModeName.setValue(rootModes.getValue(rootMode)); //so that menu item knows to become checked
				parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets the frequency of checking for underflow", "[integer, 1 or greater]", commandName, "setUnderflowCheckFreq")) {
			int freq = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(freq) && !MesquiteThread.isScripting())
				freq = MesquiteInteger.queryInteger(containerOfModule(), "Checking frequency", "Frequency at which underflow checking is performed in likelihood calculations.  A value of n means checking is performed on each nth calculation; higher numbers mean the calculations go faster but are at risk of underflow problems.  Values over 10 are not recommended", (int)underflowCheckFrequency, 1, 10000);

			if (MesquiteInteger.isCombinable(freq) && freq >=0 && freq!=underflowCheckFrequency){
				underflowCheckFrequency = freq; //change mode
				parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	private void initProbs(int nodes, int numStates) {
		this.numStates = numStates;
		if (downProbs==null || downProbs.length!=nodes || downProbs[0].length!=numStates){
			downProbs = new double[nodes][numStates];
			upProbs = new double[nodes][numStates];
			finalProbs = new double[nodes][numStates];
			underflowCompDown = new double[nodes];
			underflowCompUp = new double[nodes];
			empirical = new double[numStates];
		}
		Double2DArray.zeroArray(downProbs);
		Double2DArray.zeroArray(upProbs);
		Double2DArray.zeroArray(finalProbs);
		DoubleArray.zeroArray(underflowCompDown);
		DoubleArray.zeroArray(underflowCompUp);
		DoubleArray.zeroArray(empirical);
	}
	/*.................................................................................................................*/
	/* calculates for each i at node N, the sum of probabilities for all possible j that can be placed at d*/
	private double probFromSection (Tree tree, int d, int i, double[] ProbsD, ProbabilityCategCharModel model, boolean downwards) {
		double prob = 0;
		if (downwards){
			for (int j=0; j< numStates; j++) 
				prob += model.transitionProbability(i, j, tree, d)*ProbsD[j]; 
		}
		else {
			for (int j=0; j< numStates; j++) 
				prob += model.transitionProbability(j,i, tree, d)*ProbsD[j]; 
		}
		return prob;
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
	/* assumes hard polytomies */
	private   void downPass(int node, Tree tree, ProbabilityCategCharModel model, CategoricalDistribution observedStates) {
		if (tree.nodeIsTerminal(node)) {
			long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node));
/*Uncertain multistate starts here OliverZMarg*/
			int[] states = CategoricalState.expand(observed);
			DoubleArray.zeroArray(downProbs[node]);
			if(states.length > 0 && !CategoricalState.isUnassigned(observed) && !CategoricalState.isInapplicable(observed)){
				for(int i = 0; i < states.length; i++){
					downProbs[node][states[i]] = 1; //Assign probability of 1 for all observed states at tips.
				}
			}/*Polymorphism ends here*/
			
/*Original Implementation:
			int obs = CategoricalState.minimum(observed); //NOTE: just minimum observed!
			DoubleArray.zeroArray(downProbs[node]);
			if (obs>=0 && obs < downProbs[node].length && !CategoricalState.isUnassigned(observed) && !CategoricalState.isInapplicable(observed)) 
				downProbs[node][obs] = 1;
/*End Original Implementation*/
		}
		else {
			DoubleArray.zeroArray(downProbs[node]);
			underflowCompDown[node] =0;
			for (int d = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted)) {
				downPass(d, tree, model, observedStates);
				underflowCompDown[node] += underflowCompDown[d];
			}

			for (int i=0; i<numStates; i++) {
				double prob = 1.0;
				for (int d = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted)) {
					double scnProb = probFromSection(tree, d, i, downProbs[d], model, true); 
					prob *= scnProb; 
				}
				downProbs[node][i] = prob;
			}
			if (++underflowCheck % underflowCheckFrequency == 0){
				underflowCompDown[node] += checkUnderflow(downProbs[node]);
			}
		}

	}
	/*.................................................................................................................*/
	/* assumes hard polytomies */
	private   void upPass(int node, Tree tree,  ProbabilityCategCharModel model, CategoricalDistribution observedStates) {
		underflowCompUp[node] = 0;
		if (node == tree.getRoot(deleted)) {  
			if (tree.rootIsReal(deleted)){
				/*~~~~~~~~~~~~~~ upPass ~~~~~~~~~~~~~~*
				if (rootMode == SUBROOT_PRIOR) {
					for (int i=0; i<numStates; i++) 
						upProbs[node][i] = model.priorProbability(i);
				}
				else if (rootMode == SUBROOT_EMPIRICAL) {
					calculateEmpirical(tree, observedStates, empirical);
					for (int i=0; i<numStates && i<empirical.length; i++) 
						upProbs[node][i] = empirical[i]; 
				}
				 */
			}
		}
		else {
			DoubleArray.zeroArray(upProbs[node]);
			if (tree.motherOfNode(node, deleted) == tree.getRoot(deleted) && !tree.rootIsReal(deleted) && !tree.nodeIsPolytomous(tree.getRoot(deleted))) {  //special case of node just above dichotomous ghost root
				int sister;
				if (tree.nodeExists(tree.nextSisterOfNode(node, deleted)))
					sister = tree.nextSisterOfNode(node, deleted);
				else
					sister = tree.previousSisterOfNode(node, deleted);

				for (int i=0; i<numStates; i++) 
					upProbs[node][i] = downProbs[sister][i];
			}
			else if (tree.nodeIsInternal(node)) {
				int mother = tree.motherOfNode(node, deleted);
				for (int i=0; i<numStates; i++) {
					double prob = 1.0;

					// accumulate downstates from sisters
					for (int d = tree.firstDaughterOfNode(mother, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted)) {
						if (d!=node) {
							prob *= probFromSection(tree, d, i, downProbs[d], model, true);
						}
					}

					/*~~~~~~~~~~~~~~ upPass ~~~~~~~~~~~~~~*/
					if (mother != tree.getRoot(deleted)){
						//get upstate from ancestor
						prob *= probFromSection(tree, mother, i, upProbs[mother], model, false);  //pass mother because that's the branch length
					}
					//mother is the root.  Do we use a prior also for the recontruction?
					else if (rootMode == ROOT_USEPRIOR){
						prob *= model.priorProbability(i);
					}
					upProbs[node][i] = prob;
				}
				// accumulate compensations from sisters
				for (int d = tree.firstDaughterOfNode(mother, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted)) {
					if (d!=node)
						underflowCompUp[node] += underflowCompDown[d];
				}

				//accumulate compensations from ancestor
				if (mother!=tree.getRoot(deleted))
					underflowCompUp[node] += underflowCompUp[mother];
			}
			if (++underflowCheck % underflowCheckFrequency == 0){
				underflowCompUp[node] += checkUnderflow(upProbs[node]);
			}
		}
		for (int d = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted)) {
			upPass(d, tree, model, observedStates);
		}
	}

	/*.................................................................................................................*/
	public   void finalPass(int node, Tree tree, CategoricalHistory statesAtNodes, ProbabilityCategCharModel model, CategoricalDistribution observedStates) {
		if (node==tree.getRoot(deleted) && (!tree.rootIsReal(deleted))) { //root isn't real; skip

		}
		/*~~~~~~~~~~~~~~ finalPass ~~~~~~~~~~~~~~*/
		else if (node==tree.getRoot(deleted)){
			if (rootMode == ROOT_IGNOREPRIOR) { //root but not using subroot prior; just use downprobs
				DoubleArray.zeroArray(finalProbs[node]);
				for (int i=0; i<numStates; i++) 
					finalProbs[node][i] = downProbs[node][i]; 
			}
			else if (rootMode == ROOT_USEPRIOR) { //root  using root prior
				DoubleArray.zeroArray(finalProbs[node]);
				for (int i=0; i<numStates; i++) 
					finalProbs[node][i] = downProbs[node][i]*model.priorProbability(i); 
			}
		}
		else if (!tree.nodeIsTerminal(node)) { //internal, including root with subroot prior
			DoubleArray.zeroArray(finalProbs[node]);
			for (int i=0; i<numStates; i++) 
				finalProbs[node][i] = downProbs[node][i]*probFromSection(tree, node, i, upProbs[node], model, false); //POLARITY
		}
		for (int d = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted)) {
			finalPass(d, tree, statesAtNodes, model, observedStates);
		}
	}
	/*.................................................................................................................*/
	double[] readjustComp(Tree tree, int node, double[] compensated, boolean log){
		double[] d = new double[compensated.length];
		double comp = underflowCompDown[node];
		if (tree.getRoot(deleted)!=node)
			comp += underflowCompUp[node];
		if (log){
			for (int i=0; i<d.length; i++)
				d[i] = comp - Math.log(compensated[i]);
		}
		else {
			for (int i=0; i<d.length; i++)
				d[i] = compensated[i]*Math.exp(-comp);
		}
		return d;
	}
	/*.................................................................................................................*/
	public   void storeFinal(int node, Tree tree, CategoricalHistory statesAtNodes, CategoricalDistribution observedStates, double dThresh) {
		if (tree.nodeIsTerminal(node)) {
			long st = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node));
			statesAtNodes.setState(node, st);
			if (fillFrequencies) {
				int numSt = CategoricalState.cardinality(st);
				for (int i=0; i<numStates; i++) {
					if (CategoricalState.isElement(st, i))
						finalProbs[node][i] = 1.0/numSt;
					else
						finalProbs[node][i] =0;
				}
				statesAtNodes.setFrequencies(node, finalProbs[node]);
			}
		}
		else if (tree.rootIsReal(deleted) || node!=tree.getRoot(deleted)) {
			statesAtNodes.setState(node, CategoricalState.chooseHighestWithinFactor(finalProbs[node],dThresh));
			if (fillFrequencies) {
				if (reportMode == REPORT_Proportional){
					double[] f = new double[numStates];
					double sum = 0;
					for (int i=0; i<numStates; i++)
						sum+= finalProbs[node][i];
					for (int i=0; i<numStates; i++)
						f[i] = finalProbs[node][i]/sum;  
					statesAtNodes.setFrequencies(node, f);
				}
				else if (reportMode == REPORT_Raw)
					statesAtNodes.setFrequencies(node, readjustComp(tree, node, finalProbs[node], false));
				else { //REPORT_Log
					double[] f = readjustComp(tree, node, finalProbs[node], false);
					for (int i=0; i<numStates; i++)
						f[i] = -Math.log(f[i]);  
					statesAtNodes.setFrequencies(node, f);
				}
			}
		}
		for (int d = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted)) 
			storeFinal(d, tree, statesAtNodes,observedStates, dThresh);
	}
	/*.................................................................................................................*/
	public   void storeDown(int node, Tree tree, CategoricalHistory statesAtNodes, CategoricalDistribution observedStates) {
		if (tree.nodeIsTerminal(node)) {
			long st = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node));
			statesAtNodes.setState(node, st);

			int numSt = CategoricalState.cardinality(st);
			for (int i=0; i<numStates; i++) {
				if (CategoricalState.isElement(st, i))
					downProbs[node][i] = 1.0/numSt;
				else
					downProbs[node][i] =0;
			}
			statesAtNodes.setFrequencies(node, downProbs[node]);

		}
		else  {  
			statesAtNodes.setFrequencies(node, readjustComp(tree, node, downProbs[node], false));
		}
		for (int d = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted)) 
			storeDown(d, tree, statesAtNodes,observedStates);
	}
	/*.................................................................................................................*/
	private boolean hasZeroOrNegLengthBranches(Tree tree, int N, boolean countRoot) {
		if (tree.getBranchLength(N) <= 0.0 && (countRoot || tree.getRoot() != N))
			return true;
		if (tree.nodeIsInternal(N)){
			for (int d = tree.firstDaughterOfNode(N, deleted); tree.nodeExists(d); d = tree.nextSisterOfNode(d, deleted))
				if (hasZeroOrNegLengthBranches(tree, d, countRoot))
					return true;
		}
		return false;
	}
	//boolean warnedMissing = false;
	boolean warnedPolymorphic = false;
	boolean warnedMaxState = false;
	boolean warnedUnbranchedInternals = false;
	boolean warnedReticulations = false;
	boolean warnedNotContiguous = false;
	boolean warnedSoftPoly = false;
	boolean warnedZeroLength = false;
	boolean warnedUnrootedCladeValuesMode = false;
	boolean[] deleted;

	boolean warn(CategoricalDistribution observedStates, ProbabilityCategCharModel model, Tree tree, MesquiteString resultString){
/*  OliverZMarg had commented this out; re-enforced for polymorphisms (but not uncertainties) in 3.04 */
		if (observedStates.hasPolymorphicStatesInTaxon(tree, tree.getRoot())) {
			String s = "Polymorphic taxa are not currently supported by Categorical data likelihood calculations.  Calculations for one or more characters were not completed.";
			if (!warnedPolymorphic) {
				discreetAlert( s);
				warnedPolymorphic = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		} 
		if (tree.hasSoftPolytomies(tree.getRoot())) {
			String message = "Trees with soft polytomies not allowed by Categorical data likelihood calculations.  Calculations for one or more trees were not completed.";
			if (!warnedSoftPoly){
				discreetAlert( message);
				warnedSoftPoly = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (hasZeroOrNegLengthBranches(tree, tree.getRoot(), ROOT_IGNOREPRIOR != rootMode && ROOT_USEPRIOR != rootMode) ) {
			String message = "Trees with zero or negative length branches are not allowed by Categorical data likelihood calculations.  You can collapse these to polytomies using the Collapse zero-length branches in the Alter/Transform tree submenu.  Calculations for one or more trees were not completed.";
			message += " TREE: " + tree.writeTree();

			if (!warnedZeroLength){
				discreetAlert( message);
				warnedZeroLength = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (!tree.rootIsReal() && reportCladeValues) {
			String message = "Unrooted trees are not allowed by Categorical data likelihood calculations when used to report clade values only.  Calculations for one or more trees were not completed.";
			message += " TREE: " + tree.writeTree();

			if (!warnedUnrootedCladeValuesMode){
				discreetAlert( message);
				warnedUnrootedCladeValuesMode = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		long allStates = observedStates.getAllStates(tree, tree.getRoot());
		int max = CategoricalState.maximum(allStates);
		if (max > model.getMaxStateDefined()) {
			String s = "Character distibution includes state values larger than allowed by current model of character evolution.  Calculations for one or more characters were not completed.";
			if (!warnedMaxState) {
				discreetAlert( s);
				warnedMaxState = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		if (allStates != CategoricalState.span(0, CategoricalState.maximum(allStates))) {  //fixed in 1. 12; had been min - max, not 0 - max
			String s = "Categorical data likelihood calculations currently require state distribution to include only contiguous states starting at 0 (e.g., character with only states 2 and 3 not allowed, and should instead be recoded to states 0 and 1).  Calculations for one or more characters were not completed.";
			if (!warnedNotContiguous) {
				if (!MesquiteThread.isScripting())
					alert(s);
				else
					logln(s);
				warnedNotContiguous = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		if (tree.hasUnbranchedInternals(tree.getRoot())) {
			String s = "Likelihood calculations cannot be done because tree has unbranched internal nodes.";
			if (!warnedUnbranchedInternals) {
				discreetAlert( s);
				warnedUnbranchedInternals = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		if (tree.hasReticulations()) {
			String s = "Likelihood calculations cannot be done because tree has reticulations.";
			if (!warnedReticulations) {
				discreetAlert( s);
				warnedReticulations = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		if (observedStates.hasMissing(tree, tree.getRoot()) || observedStates.hasInapplicable(tree, tree.getRoot())){
			if (deleted == null || deleted.length <  tree.getNumNodeSpaces())
				deleted = new boolean[tree.getNumNodeSpaces()];
			for (int i = 0; i<deleted.length; i++) deleted[i] = false;
			for (int it = 0; it< tree.getTaxa().getNumTaxa(); it++)
				if (observedStates.isUnassigned(it) || observedStates.isInapplicable(it)) {
					tree.virtualDeleteTaxon(it, deleted);
				}
		}
		else
			deleted = null;
		/*	if (observedStates.hasMissing(tree, tree.getRoot()) || observedStates.hasInapplicable(tree, tree.getRoot())) {
			String s ="Missing data & Gaps are not currently supported by Categorical data likelihood calculations.  Calculations for one or more characters were not completed.";
			if (!warnedMissing) {
				discreetAlert( s);
				warnedMissing = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}*/
		return false;
	}

	/*.................................................................................................................*/
	void calculateEmpirical(Tree tree, CategoricalDistribution observedStates, double[] empirical){
		DoubleArray.zeroArray(empirical);
		int n = 0;
		for (int it =0; it<observedStates.getNumTaxa(); it++) {
			if (tree.taxonInTree(it)){
				long s = observedStates.getState(it);
				if (CategoricalState.isCombinable(s)) {
					n++;
					for (int i=0; i<empirical.length; i++){
						if (CategoricalState.isElement(s, i))
							empirical[i]++;
					}
				}
			}
		}	
		if (n>0)
			for (int i=0; i<empirical.length; i++){
				empirical[i] /= n;
			}
	}

	/*.................................................................................................................*/
	public  void calculateStates(Tree tree, CharacterDistribution obsStates, CharacterHistory statesAtNodes, CharacterModel model, MesquiteString resultString, MesquiteNumber prob) {  
		if (model==null || statesAtNodes==null || obsStates==null || !(model instanceof ProbabilityCategCharModel) || !(obsStates instanceof CategoricalDistribution))
			return;
		estCount =0;
		zeroHit = false;
		if (prob !=null)
			prob.setToUnassigned();
		this.passedModel = (ProbabilityCategCharModel)model;
		if (tempModel == null || tempModel.getClass() != model.getClass()){
			tempModel = (ProbabilityCategCharModel)passedModel.cloneModelWithMotherLink(null);
	 		tempModel.setUserVisible(false);
		}
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		if (warn(observedStates, passedModel, tree, resultString))
			return;
		int root = tree.getRoot(deleted);
		ProbabilityCategCharModel useModel;
		double comp;
		boolean estimated = false;
		if (((ProbabilityModel)model).isFullySpecified()) {
			useModel = passedModel;
			useModel.setCharacterDistribution(observedStates);
		}
		else if (model instanceof CModelEstimator) {
			passedModel.copyToClone(tempModel);
			useModel = tempModel;
			useModel.setCharacterDistribution(observedStates);
			((CModelEstimator)useModel).estimateParameters(tree, observedStates, this);
			estimated = true;
		}
		else {
			if (prob!=null)
				prob.setToUnassigned();
			if (resultString!=null)
				resultString.setValue("Model with parameters incompletely specified.");
			return;
		}
		initProbs(tree.getNumNodeSpaces(), useModel.getMaxState()+1); //todo: the model should use the observedStates to set this; observedStates should carry info about its maximun conceivable number of states?
		downPass(root, tree, useModel, observedStates);
		comp = underflowCompDown[root];
		statesAtNodes.deassignStates();

		if (reportCladeValues){
			storeDown(root, tree, (CategoricalHistory)statesAtNodes,  observedStates);
			if (getHiredAs() == MargLikeAncStForModel.class)
				((CharacterStates)statesAtNodes).setExplanation("Clade values for " + reportModes.getValue(reportMode));
			else
				((CharacterStates)statesAtNodes).setExplanation("" );
				
			reportRootMode = false;
		}
		else {
			upPass(root, tree,  useModel, observedStates);
			finalPass(root, tree, (CategoricalHistory)statesAtNodes,  useModel, observedStates);
			storeFinal(root, tree, (CategoricalHistory)statesAtNodes,  observedStates, Math.exp(decisionThreshold));
			((CharacterStates)statesAtNodes).setExplanation(reportModes.getValue(reportMode));
			reportRootMode = !useModel.priorAlwaysFlat();
		}
		String rep = passedModel.getName() + " [" + useModel.getParameters() + "]";
		double likelihood = 0.0;

		/*~~~~~~~~~~~~~~ calculateStates ~~~~~~~~~~~~~~*/
		for (int i=0;  i<=useModel.getMaxState(); i++) 
			likelihood += downProbs[root][i] * useModel.priorProbability(i);

		double negLogLikelihood = -(Math.log(likelihood) - comp);
		if (prob!=null)
			prob.setValue(negLogLikelihood);
		if (resultString!=null) {
			String s = "Marginal prob. recon. with model " + rep + "  -log L.:" + MesquiteDouble.toString(negLogLikelihood);
			if (estimated){
				String set = passedModel.getSettingsString();
				if (set !=null)
					s += " " + set;
			}
			s += "  " + getParameters();
			if (reportMode == REPORT_Log)
				s += "\nNOTE: Because neg. log likelihoods are being reported, larger values imply lower support.  This may cause graphic displays to be confusing for internal nodes.";
			resultString.setValue(s);
		}
	}
	/*.................................................................................................................*
	// not yet working
	public Object getLikelihoodSurface(Tree tree, CharacterDistribution obsStates, CharacterModel model, double[] inputBounds, double[] outputBounds) {  
		if (((ProbabilityCategCharModel)model).isFullySpecified())
			return null;
		ProbabilityCategCharModel useModel = (ProbabilityCategCharModel)tempModel;
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		tempModel.setCharacterDistribution( observedStates);
		return tempModel.getLikelihoodSurface(tree, observedStates, this, 100, outputBounds); //also pass upper & lower bounds


	}

	/*.................................................................................................................*/
	public  void estimateParameters(Tree tree, CharacterDistribution obsStates, CharacterModel model, MesquiteString resultString) {  
		if (model==null || obsStates==null || !(obsStates instanceof CategoricalDistribution) || !(model instanceof ProbabilityCategCharModel))
			return;
		estCount =0;
		reportRootMode = false;
		zeroHit = false;
		ProbabilityCategCharModel useModel;
		this.passedModel = (ProbabilityCategCharModel)model;
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		if (warn(observedStates, passedModel, tree, resultString))
			return;
		String rep;
		double comp;
		int root = tree.getRoot(deleted);

		if (((ProbabilityModel)model).isFullySpecified()) {
			if (resultString!=null)
				resultString.setValue("Parameters already fully specified in " + passedModel.getName() + " [" + passedModel.getParameters() + "]");
			return;
		}
		else if (model instanceof CModelEstimator) {
			useModel = (ProbabilityCategCharModel)passedModel;
			useModel.setCharacterDistribution(observedStates);
			((CModelEstimator)useModel).estimateParameters(tree, observedStates, this);
			if (resultString!=null) {
				String s = "Model as estimated: " + useModel.getName() + " [" + useModel.getParameters() + "]";
				String set = useModel.getSettingsString();
				if (set !=null)
					s += " " + set;

				s += "  " + getParameters();
				resultString.setValue(s);
			}
		}
		else {
			if (resultString!=null)
				resultString.setValue("Model does not support estimation of parameters.");
			return;
		}
	}

	/*.................................................................................................................*/
	public  void calculateLogProbability(Tree tree, CharacterDistribution obsStates, CharacterModel model, MesquiteString resultString, MesquiteNumber prob) {  
		if (model==null || obsStates==null || prob == null || !(model instanceof ProbabilityCategCharModel) || !(obsStates instanceof CategoricalDistribution))
			return;
		estCount =0;
		zeroHit = false;
		reportRootMode = false;
		prob.setToUnassigned();
		ProbabilityCategCharModel useModel;
		this.passedModel = (ProbabilityCategCharModel)model;
		if (tempModel == null || tempModel.getClass() != model.getClass()){
			tempModel = (ProbabilityCategCharModel)passedModel.cloneModelWithMotherLink(null);
	 		tempModel.setUserVisible(false);
		}
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		if (warn(observedStates, passedModel, tree, resultString))
			return;
		String rep;
		double comp;
		int root = tree.getRoot(deleted);
		boolean estimated = false;

		if (((ProbabilityModel)model).isFullySpecified()) {
			useModel = (ProbabilityCategCharModel)passedModel;
			passedModel.setCharacterDistribution( observedStates);
			initProbs(tree.getNumNodeSpaces(), passedModel.getMaxState()+1); //the model should use the observedStates to set this; observedStates should carry info about its maximun conceivable number of states?
			downPass(root, tree, passedModel, observedStates);
			comp = underflowCompDown[root];
			rep = passedModel.getName() + " [" + useModel.getParameters() + "]";
		}
		else if (model instanceof CModelEstimator) {
			passedModel.copyToClone(tempModel);
			useModel = (ProbabilityCategCharModel)tempModel;
			tempModel.setCharacterDistribution(observedStates);
			((CModelEstimator)useModel).estimateParameters(tree, observedStates, this);
			estimated = true;
			rep = passedModel.getName() + " [" + useModel.getParameters() + "]";
			initProbs(tree.getNumNodeSpaces(), useModel.getMaxState()+1); //the model should use the observedStates to set this; observedStates should carry info about its maximun conceivable number of states?
			downPass(root, tree, useModel, observedStates);
			comp = underflowCompDown[root];
		}
		else {
			if (prob!=null)
				prob.setToUnassigned();
			if (resultString!=null)
				resultString.setValue("Model with parameters incompletely specified.");
			return;
		}
		double likelihood = 0.0;
		/*~~~~~~~~~~~~~~ calculateLogProbability ~~~~~~~~~~~~~~*/

		for (int i=0;  i<=useModel.getMaxState(); i++) 
			likelihood += downProbs[root][i] * useModel.priorProbability(i);


		double negLogLikelihood = -(Math.log(likelihood) - comp);
		if (prob!=null){
			prob.setValue(negLogLikelihood);
			if (estimated)
				prob.copyAuxiliaries(useModel.getParameterValues());
			else
				prob.copyAuxiliaries((MesquiteNumber[])null);
			prob.setName("-lnLikelihood");
		}
		if (resultString!=null) {
			String s = "Marginal prob. with model " + rep + " -log L.:"+ MesquiteDouble.toString(negLogLikelihood) + " [L. "+ MesquiteDouble.toString(likelihood * Math.exp(-comp)) + "]";
			if (estimated){
				String set = passedModel.getSettingsString();
				if (set !=null)
					s += " " + set;
			}
			s += "  " + getParameters();
			resultString.setValue(s);
		}
	}
	int estCount =0;
	boolean zeroHit = false;
	/*.................................................................................................................*/
	public double logLikelihoodCalc(Tree tree, ProbabilityModel pModel, CharacterDistribution states){
		if (zeroHit)
			return -0.001*(++estCount);  // to shortcircuit the optimizer wandering around zero with very high rates
		reportRootMode = false;
		CategoricalDistribution observedStates = (CategoricalDistribution)states;
		ProbabilityCategCharModel model = (ProbabilityCategCharModel)pModel;
		model.setCharacterDistribution(observedStates);
		initProbs(tree.getNumNodeSpaces(), model.getMaxState()+1); //the model should use the observedStates to set this; observedStates should carry info about its maximun conceivable number of states?
		estCount++;
		int root = tree.getRoot(deleted);
		downPass(root, tree,model, observedStates);
		double likelihood = 0.0;
		double comp;
		/*~~~~~~~~~~~~~~ logLikelihoodCalc ~~~~~~~~~~~~~~*/

		for (int i=0;  i<=model.getMaxState(); i++) 
			likelihood += downProbs[root][i]*model.priorProbability(i);

		comp = underflowCompDown[root];
		double logLike = Math.log(likelihood) - comp;
		if (logLike> -0.00001) {
			zeroHit = true;
		}

		return (logLike);
	}
	/*.................................................................................................................*/
	public boolean compatibleWithContext(CharacterModel model, CharacterDistribution observedStates) {
		boolean allowedType = observedStates instanceof CategoricalDistribution;
		if (allowedType && observedStates !=null){
			Class sc = observedStates.getStateClass();
			allowedType = allowedType &&  !(DNAState.class.isAssignableFrom(sc) || ProteinState.class.isAssignableFrom(sc));  //disallow molecular characters for now
		}
		return (allowedType && model instanceof ProbabilityCategCharModel);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Maximum likelihood reconstruct (Generic categorical)";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Assesses likelihood for categorical characters, and r econstructs ancestral states by the maximum marginal probability (\"MLE\") criterion" ;
	}

	/*.................................................................................................................*/
	public String getParameters() {
		String s = "";
		if (showRootModeChoices && getHiredAs() != MargLikeAncStCLForModel.class && reportRootMode) {
			s += "Using root mode " + rootModes.getValue(rootMode);
		}
		if (getHiredAs() == MargLikeAncStForModel.class) {
			if (showRootModeChoices)
				s += "; ";
			s += "Reporting likelihoods as " + reportModes.getValue(reportMode);
		}
		s += "; Threshold when decisions made: " + MesquiteDouble.toString(decisionThreshold);
		return s;
	}
}


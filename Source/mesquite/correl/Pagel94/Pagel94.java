/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 This module copyright 2006 P. Midford and W. Maddison


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.correl.Pagel94;

import java.awt.Checkbox;
import java.awt.Label;
import mesquite.categ.lib.CategoricalAdjustable;
import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalHistory;
import mesquite.categ.lib.CategoricalState;
import mesquite.correl.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.AdjustableDistribution;
import mesquite.lib.characters.CharacterDistribution;

public class Pagel94 extends Pagel94Calculator {

	private CategoricalDistribution observedStates1;
	private CategoricalDistribution observedStates2;
	private MesquiteLong seed;
	private long originalSeed=System.currentTimeMillis(); //0L;
	private CategoricalHistory[] evolvingStates;

	private ProgressIndicator progress = null;

	private PagelMatrixModel modelConstrained;
	private PagelMatrixModel model8;
	private PagelMatrixModel model4; //USED temporarily to find starting for 6 param case

	private final static int INDEPENDENT4 = 0;  //used to test whether any affect of Y on X or X on Y: 4 parameter model, X and Y are independent of each other
	private final static int XINDEPENDENTofY = 1;  //used to test whether X depends on Y:  6 parameter model, Y is contingent on X & X is independent of Y)
	private final static int YINDEPENDENTofX = 2;  //used to test whether Y depends on X:  6 parameter model, X is contingent on Y & Y is independent of X)
	private int currentConstrainedModel = INDEPENDENT4;
	private StringArray constrainedModelNames;
	private MesquiteString currentConstrainedModelName;

	private int simCount = 100;
	private int completedCount = 0;
	private MesquiteBoolean presentPValue;
	private CLogger logger;
	private MesquiteBoolean resimulateConstantCharacters;
	private int constantCharCount = 0;
	private int numIterations = 10;


	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		seed = new MesquiteLong(originalSeed);
		// And here
		constrainedModelNames = new StringArray(3);  
		MesquiteSubmenuSpec effectSubmenu = addSubmenu(null, "Test For", makeCommand("setConstrainedModelToUse", this), constrainedModelNames);
		constrainedModelNames.setValue(0, "Any Effect");  //the strings passed will be the menu item labels
		constrainedModelNames.setValue(1, "X depends on Y");
		constrainedModelNames.setValue(2, "Y depends on X");
		currentConstrainedModelName = new MesquiteString(constrainedModelNames.getValue(currentConstrainedModel));  //this helps the menu keep track of checkmenuitems
		effectSubmenu.setSelected(currentConstrainedModelName);

		presentPValue = new MesquiteBoolean(false);
		resimulateConstantCharacters = new MesquiteBoolean(false);  // turned off for now
		addCheckMenuItem(null, "Present P Value", makeCommand("togglePresentPValue", this), presentPValue);
		addMenuItem("Likelihood Iterations (Pagel 94)...", makeCommand("setNumIterations", this));
		addMenuItem("Set Seed (Pagel 94)...", makeCommand("setSeed", this));
		addMenuItem("Set Simulation Replicates...", makeCommand("setSimCount", this));
		

		if (getEmployer() instanceof CLogger)
			setLogger(((CLogger)getEmployer()));
		if (!MesquiteThread.isScripting()){
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Pagel 94 parameters",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			dialog.addLabel("Test for:", Label.LEFT);
			RadioButtons comparison = dialog.addRadioButtons (new String[] {"Any effect", "X depends on Y", "Y depends on X"}, 0);
			IntegerField iterationsField = dialog.addIntegerField("Extra Iterations (intensity of likelihood search)", numIterations, 5, 0, MesquiteInteger.infinite);
			dialog.addHorizontalLine(1);
			Checkbox pValueBox = dialog.addCheckBox("Present p-value", presentPValue.getValue());
			IntegerField simField = dialog.addIntegerField("Number of simulations to estimate p-value", simCount, 5, 0, MesquiteInteger.infinite);
			
			dialog.completeAndShowDialog(true);
			dialog.dispose();
			if (buttonPressed.getValue()==0)  {
				simCount = simField.getValue();
				numIterations = iterationsField.getValue();
				presentPValue.setValue(pValueBox.getState());
				currentConstrainedModel = comparison.getValue();

			}
			else
				return false;

		}
		postBean("startJob", false);
		if (currentConstrainedModel == INDEPENDENT4)
			modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL4PARAM);
		else {
			model4 = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL4PARAM);  //used merely to help 6 param
			if (currentConstrainedModel == XINDEPENDENTofY)
				modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL6PARAMINDEPENDENTX);
			else  //constrainedModelToUse == YINDEPENDENTofX
				modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL6PARAMINDEPENDENTY);
		}
		model8 = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL8PARAM);
		return true;
	}
	
	public boolean requestPrimaryChoice(){
		return true;
	}

	public void setLogger(CLogger logger){
		this.logger = logger;
	}
	void writeToLogger(String s){
		if (logger != null)
			logger.cwrite(s);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setSeed " + originalSeed); 
		temp.addLine("setConstrainedModelToUse " + ParseUtil.tokenize(constrainedModelNames.getValue(currentConstrainedModel)));
		temp.addLine("setSimCount " + getSimCount()); 
		temp.addLine("togglePresentPValue " + presentPValue.toOffOnString());
		temp.addLine("setNumIterations " + numIterations);
		return temp;
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the random number seed to that passed", "[long integer seed]", commandName, "setSeed")) {
			long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
			if (!MesquiteLong.isCombinable(s) && !MesquiteThread.isScripting()){
				s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for character evolution simulation", originalSeed);
			}
			if (MesquiteLong.isCombinable(s)){
				originalSeed = s;
				seed.setValue(originalSeed);
				if (!MesquiteThread.isScripting()) parametersChanged(); //?
			}
			return null;
		}
		else if (checker.compare(getClass(), "Sets the effect to test for", null, commandName, "setConstrainedModelToUse")) {
			String name = parser.getFirstToken(arguments); //get argument passed of option chosen
			int newMode = constrainedModelNames.indexOf(name); //see if the option is recognized by its name
			if (newMode >=0 && newMode!=currentConstrainedModel){
				currentConstrainedModel = newMode; //change mode
				currentConstrainedModelName.setValue(constrainedModelNames.getValue(currentConstrainedModel)); //so that menu item knows to become checked
				model8 = null; //to force redo model
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
			

		}
		else if (checker.compare(this.getClass(), "Sets the number of iterations for likelihood search", "[number]", commandName, "setNumIterations")) {
			MesquiteInteger pos = new MesquiteInteger();
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Likelihood iterations", "Intensity of likelihood search for 8 parameter model for Pagel 94 (Number of extra iterations):", numIterations, 0, MesquiteInteger.infinite);
			if (newNum>0  && newNum!=numIterations) {
				numIterations = newNum;
				if (!MesquiteThread.isScripting()){
					parametersChanged();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the number of simulations to run to estimate p-value of likelihood difference", "[nonnegative integer]", commandName, "setSimCount")){
			int sCount = MesquiteInteger.fromFirstToken(arguments,stringPos);
			if (!MesquiteInteger.isCombinable(sCount))
				sCount = MesquiteInteger.queryInteger(containerOfModule(),"Number of simulations", "Number of simulations to estimate p-value for Pagel 94 analysis", getSimCount(),0,1000,true);
			if (MesquiteInteger.isCombinable(sCount)){
				setSimCount(sCount);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
			else 
				setSimCount(0);
			return null;

		}
		else if (checker.compare(this.getClass(), "Sets whether or not P values are to be presented", "[on; off]", commandName, "togglePresentPValue")) {
			presentPValue.toggleValue(parser.getFirstToken(arguments));

			if (!MesquiteThread.isScripting()) {

				if (presentPValue.getValue()){
					int sCount = MesquiteInteger.queryInteger(containerOfModule(),"Number of simulations", "Number of simulations to estimate p-value for Pagel 94 analysis", getSimCount(),0,10000,true);
					if (MesquiteInteger.isCombinable(sCount))
						setSimCount(sCount);
					else
						presentPValue.setValue(false);
					parametersChanged();
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	public void initialize(Tree tree, CharacterDistribution charStates1, CharacterDistribution charStates2) {
		if (!(charStates1 instanceof CategoricalDistribution ||
				charStates2 instanceof CategoricalDistribution)) {
			if (!(charStates1 instanceof CategoricalDistribution))
				MesquiteMessage.warnProgrammer("Quitting because the first character is not Categorical");
			else
				MesquiteMessage.warnProgrammer("Quitting because the second character is not Categorical");
			iQuit();
			return;
		}
		observedStates1 = (CategoricalDistribution)charStates1;
		observedStates2 = (CategoricalDistribution)charStates2;
		if (observedStates1.getMaxState() > 1 ||
				observedStates2.getMaxState() > 1) {
			if (observedStates1.getMaxState() > 1)
				MesquiteMessage.warnProgrammer("Quitting because the first character doesn't seem to be binary -- getMaxState() returned " + observedStates1.getMaxState());
			else
				MesquiteMessage.warnProgrammer("Quitting because the second character doesn't seem to be binary -- getMaxState() returned " + observedStates2.getMaxState());
			iQuit();
		}
		if (model8 == null){  //note: model8==null is signal that model needs remaking, in case of change
			if (currentConstrainedModel == INDEPENDENT4)
				modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL4PARAM);
			else {
				model4 = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL4PARAM);  //used merely to help 6 param
				if (currentConstrainedModel == XINDEPENDENTofY)
					modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL6PARAMINDEPENDENTX);
				else  //constrainedModelToUse == YINDEPENDENTofX
					modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL6PARAMINDEPENDENTY);
			}
			model8 = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL8PARAM);
		}
	}

	public void setSeed(long newSeed){
		originalSeed = newSeed;
		seed.setValue(newSeed);
	}

	public long getSeed(){
		return seed.getValue();
	}

	public int getSimCount(){
		return simCount;
	}

	public void setSimCount(int newCount){
		simCount = newCount;
	}

	private boolean hasZeroOrNegLengthBranches(Tree tree, int N, boolean countRoot) {
		if (tree.getBranchLength(N) <= 0.0 && (countRoot || tree.getRoot() != N))
			return true;
		if (tree.nodeIsInternal(N)){
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (hasZeroOrNegLengthBranches(tree, d, countRoot))
					return true;
		}
		return false;
	}
	private boolean warnedMissing = false;
	private boolean warnedPolymorphic = false;
	private boolean warnedMaxState = false;
	private boolean warnedUnbranchedInternals = false;
	private boolean warnedReticulations = false;
	private boolean warnedSoftPoly = false;
	private boolean warnedZeroLength = false;
	private boolean warn(CategoricalDistribution observedStates1, CategoricalDistribution observedStates2, Tree tree, MesquiteString resultString){
		if (observedStates1.hasMultipleStatesInTaxon(tree, tree.getRoot()) || observedStates2.hasMultipleStatesInTaxon(tree, tree.getRoot())) {
			String s = "Polymorphic or uncertain taxa are not currently supported in Pagel94 calculations.  Calculations were not completed.";
			if (!warnedPolymorphic) {
				discreetAlert( s);
				warnedPolymorphic = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		if (tree.hasSoftPolytomies(tree.getRoot())) {
			String message = "Trees with soft polytomies are not currently supported in Pagel94 calculations.  Calculations were not completed.";
			if (!warnedSoftPoly){
				discreetAlert( message);
				warnedSoftPoly = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (hasZeroOrNegLengthBranches(tree, tree.getRoot(), false) ) {
			String message = "Trees with zero or negative length branches are not currently supported in Pagel94 calculations.  Calculations were not completed.";
			message += " TREE: " + tree.writeTree();

			if (!warnedZeroLength){
				discreetAlert( message);
				warnedZeroLength = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		long allStates = observedStates1.getAllStates(tree, tree.getRoot()) | observedStates2.getAllStates(tree, tree.getRoot());
		int max = CategoricalState.maximum(allStates);
		if (max > 1) {
			String s = "Character distibution includes state values larger than 1; this is not currently supported in Pagel94 calculations.  Calculations were not completed.";
			if (!warnedMaxState) {
				discreetAlert( s);
				warnedMaxState = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		if (tree.hasUnbranchedInternals(tree.getRoot())) {
			String s = "Pagel94 calculations cannot be done because tree has unbranched internal nodes.";
			if (!warnedUnbranchedInternals) {
				discreetAlert( s);
				warnedUnbranchedInternals = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		if (tree.hasReticulations()) {
			String s = "Pagel94 calculations cannot be done because tree has reticulations.";
			if (!warnedReticulations) {
				discreetAlert( s);
				warnedReticulations = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		if (observedStates1.hasMissing(tree, tree.getRoot()) || observedStates1.hasInapplicable(tree, tree.getRoot()) || observedStates2.hasMissing(tree, tree.getRoot()) || observedStates2.hasInapplicable(tree, tree.getRoot())) {
			String s ="Missing data & Gaps are not currently supported by Pagel94 calculations.  Calculations were not completed.";
			if (!warnedMissing) {
				discreetAlert( s);
				warnedMissing = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}
		return false;
	}


	public void calculateNumber(Tree tree, CharacterDistribution charStates1, CharacterDistribution charStates2, MesquiteNumber result, MesquiteString resultString) {

		clearResultAndLastResult(result);
		if (tree == null || charStates1 == null || charStates2 == null)
			return;
		if (result == null)
			result = new MesquiteNumber();  //for saveLastResult
		double resultConstrained;
		double result8;
		double pvalue = MesquiteDouble.unassigned;

		if (!(charStates1 instanceof CategoricalDistribution ||
				charStates2 instanceof CategoricalDistribution)) {
			result.setValue(MesquiteDouble.unassigned);  
			if (resultString != null)
				resultString.setValue("Character not categorical; inappropriate for Pagel94 calculations");
			return;
		}

		observedStates1 = (CategoricalDistribution)charStates1;
		observedStates2 = (CategoricalDistribution)charStates2;
		if (warn(observedStates1, observedStates2, tree, resultString))
			return;
		logln("Pagel 94 analysis: Using characters " + observedStates1.getName() + " and " + observedStates2.getName());

		if (model8 == null){  //note: model8 == null is signal that model needs remaking, in case of change
			if (currentConstrainedModel == INDEPENDENT4)
				modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL4PARAM);
			else {
				model4 = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL4PARAM);
				if (currentConstrainedModel == XINDEPENDENTofY)
					modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL6PARAMINDEPENDENTX);
				else  //constrainedModelToUse == YINDEPENDENTofX
					modelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL6PARAMINDEPENDENTY);
			}
			model8 = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL8PARAM);
		}

		MesquiteInteger numIt = model8.getExtraSearch();
		numIt.setValue(numIterations);
		logln("Pagel 94 analysis: Calculating likelihood for 4 parameter model");

		if (currentConstrainedModel != INDEPENDENT4){  //constrained is 6 param model; therefore go to 4 for starting point
			model4.estimateParameters(tree,observedStates1,observedStates2);
			logln("Pagel 94 analysis: Calculating likelihood for 6 parameter model");
			modelConstrained.setParametersFromSimplerModel(model4.getParams(),PagelMatrixModel.MODEL4PARAM);
		}

		modelConstrained.estimateParameters(tree,observedStates1,observedStates2);
		resultConstrained = modelConstrained.evaluate(modelConstrained.getParams(),null);

		logln("Pagel 94 analysis: Calculating likelihood for 8 parameter model (" + numIterations + " iterations)");
		if (currentConstrainedModel != INDEPENDENT4)  //4 param model to use for starting point could be either constrained or temporary one
			model8.setParametersFromSimplerModel(model4.getParams(),PagelMatrixModel.MODEL4PARAM);
		else
			model8.setParametersFromSimplerModel(modelConstrained.getParams(),PagelMatrixModel.MODEL4PARAM);
		model8.estimateParameters(tree,observedStates1,observedStates2);
		result8 = model8.evaluate(model8.getParams(),null);

		double score = resultConstrained-result8;
		if (logger!= null){ 
			writeToLogger("\nTesting: " + constrainedModelNames.getValue(currentConstrainedModel));
			if (currentConstrainedModel == INDEPENDENT4)
				writeToLogger("\n\nFor four parameter model (independent, no effect of X on Y or Y on X): \n");  
			else if (currentConstrainedModel == XINDEPENDENTofY)
				writeToLogger("\n\nFor 6 parameter model (X's evolution is independent of Y): \n");  
			else  //constrainedModelToUse == YINDEPENDENTofX
				writeToLogger("\n\nFor 6 parameter model (Y's evolution is independent of X): \n");  
			writeToLogger(modelConstrained.getParameters());
			writeToLogger("\n\nlog Likelihood is " + (-1*resultConstrained));
			writeToLogger("\n\n\nFor eight parameter model : \n");
			writeToLogger(model8.getParameters());
			writeToLogger("\n\nlog Likelihood is " + (-1*result8));

			writeToLogger("\n\nDifference is " + score);
		}

		if (presentPValue.getValue() && simCount>0){
			logln("Pagel 94 analysis: Estimating p value using simulations (Generating " + simCount + " simulated data sets)");
			// Begin Simulation
			CategoricalAdjustable[] SimData = new CategoricalAdjustable[2];
			double independentScore = 0;  // keep compiler happy
			double dependentScore;
			evolvingStates = new CategoricalHistory[2];
			double[][] rootPriors = modelConstrained.getRootPriors();

			
			PagelMatrixModel savedModelConstrained = null;
			if (currentConstrainedModel == INDEPENDENT4)
				savedModelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL4PARAM); 
			else if (currentConstrainedModel == XINDEPENDENTofY)
				savedModelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL6PARAMINDEPENDENTX); 
			else  //constrainedModelToUse == YINDEPENDENTofX
				savedModelConstrained = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL6PARAMINDEPENDENTY); 

			modelConstrained.copyToClone(savedModelConstrained);
			PagelMatrixModel savedModel8 = new PagelMatrixModel("",CategoricalState.class,PagelMatrixModel.MODEL8PARAM);
			model8.copyToClone(savedModel8);
			double diffs[] = new double[simCount];
			DoubleArray.deassignArray(diffs);
			constantCharCount=0;
			boolean hasAborted = false;
			progress = new ProgressIndicator(getProject(),"Running simulations", "Running", simCount, true);
			progress.start();
			try {
				for (int simNumber = 0;(simNumber<simCount)&&!hasAborted;simNumber++){
					progress.setCurrentValue(simNumber+1);  // so user sees 1-based count
					progress.setText("Running simulation " + (simNumber+1) + " of " + simCount, false);
					modelConstrained.setProgress(progress);
					model8.setProgress(progress);
					SimData[0] = new CategoricalAdjustable(observedStates1.getTaxa(),observedStates1.getNumNodes());
					SimData[1] = new CategoricalAdjustable(observedStates2.getTaxa(),observedStates2.getNumNodes());
					evolvingStates[0] = new CategoricalHistory(tree.getTaxa(), tree.getNumNodeSpaces());
					evolvingStates[1] = new CategoricalHistory(tree.getTaxa(), tree.getNumNodeSpaces());
					getSimulatedCharacters(SimData,tree, savedModelConstrained, rootPriors, seed);
					if (!eitherCharacterConstant(tree,SimData[0],SimData[1])) {
						if (progress.isAborted()){
							hasAborted = true;
						}
						if (!hasAborted){
							if (currentConstrainedModel != INDEPENDENT4){  //constrained is 6 param model; therefore go to 4 for starting point
								model4.estimateParameters(tree,SimData[0],SimData[1]);
								modelConstrained.setParametersFromSimplerModel(model4.getParams(),PagelMatrixModel.MODEL4PARAM);
							}

							modelConstrained.estimateParameters(tree,SimData[0],SimData[1]);
							independentScore = modelConstrained.evaluate(modelConstrained.getParams(),null);
						}
						if (progress.isAborted()){
							hasAborted = true;
						}
						if (!hasAborted){
							if (currentConstrainedModel != INDEPENDENT4)  //constrained is 6 param model; therefore go to 4 for starting point
								model8.setParametersFromSimplerModel(model4.getParams(),PagelMatrixModel.MODEL4PARAM); 
							else
								model8.setParametersFromSimplerModel(modelConstrained.getParams(),PagelMatrixModel.MODEL4PARAM); 
							model8.estimateParameters(tree,SimData[0],SimData[1]);
							dependentScore= model8.evaluate(model8.getParams(),null);
							diffs[simNumber] = independentScore-dependentScore;
						}
						if (progress.isAborted()){
							hasAborted = true;
						}
					}
					else if(resimulateConstantCharacters.getValue()){
						while(eitherCharacterConstant(tree,SimData[0],SimData[1])){
							logln("Resimulating Characters");
							SimData[0] = new CategoricalAdjustable(observedStates1.getTaxa(),observedStates1.getNumNodes());
							SimData[1] = new CategoricalAdjustable(observedStates2.getTaxa(),observedStates2.getNumNodes());
							evolvingStates[0] = new CategoricalHistory(tree.getTaxa(), tree.getNumNodeSpaces());
							evolvingStates[1] = new CategoricalHistory(tree.getTaxa(), tree.getNumNodeSpaces());
							getSimulatedCharacters(SimData,tree, savedModelConstrained, rootPriors, seed);
							hasAborted = progress.isAborted();
						}
						if (!hasAborted){
							if (currentConstrainedModel != INDEPENDENT4){  //constrained is 6 param model; therefore go to 4 for starting point
								model4.estimateParameters(tree,SimData[0],SimData[1]);
								modelConstrained.setParametersFromSimplerModel(model4.getParams(),PagelMatrixModel.MODEL4PARAM);
							}
							modelConstrained.estimateParameters(tree,SimData[0],SimData[1]);
							independentScore = modelConstrained.evaluate(modelConstrained.getParams(),null);
							hasAborted = progress.isAborted();
						}
						if (!hasAborted){
							if (currentConstrainedModel != INDEPENDENT4)  //constrained is 6 param model; therefore go to 4 for starting point
								model8.setParametersFromSimplerModel(model4.getParams(),PagelMatrixModel.MODEL4PARAM); 
							else
								model8.setParametersFromSimplerModel(modelConstrained.getParams(),PagelMatrixModel.MODEL4PARAM); 
							model8.estimateParameters(tree,SimData[0],SimData[1]);
							hasAborted = progress.isAborted();
						}
						if (!hasAborted){
							dependentScore= model8.evaluate(model8.getParams(),null);
							logln("Scores (indep,dep): " + independentScore + ", " + dependentScore);           	 
							diffs[simNumber] = independentScore-dependentScore;
							hasAborted = progress.isAborted();
						}
					}
					else{
						diffs[simNumber] = 0;   //if character is constant the 4-parameter and 8-parameter models ought to be =
						constantCharCount++;
					}
				}
			}
			catch(PagelMatrixModel.StuckSearchException e){
				hasAborted = true;
			}
			progress.goAway();
			modelConstrained.setProgress(null);
			model8.setProgress(null);
			DoubleArray.sort(diffs);
			int position;
			for (position = 0;position<simCount;position++)
				if (score<diffs[position])
					break;
			if (hasAborted){
				int completedDiffs = countAssigned(diffs);
				if (completedDiffs>0){
					pvalue = 1-(1.0*position)/(1.0*completedDiffs);
					writeToLogger("\np-value from " + completedDiffs +" simulations is " + pvalue + "\n");
					if (constantCharCount >0)
						writeToLogger("Simulation set includes " + constantCharCount + " sets with constant characters");
					completedCount = completedDiffs;
				}
				else{
					pvalue = MesquiteDouble.unassigned;
					writeToLogger("\nNo simulations completed");
					completedCount = 0;
				}
			}
			else {
				pvalue = 1-(1.0*position)/(1.0*simCount);
				writeToLogger ("\np-value from " + simCount + " simulations is " + pvalue + "\n");
				if (constantCharCount >0)
					writeToLogger("Simulation set includes " + constantCharCount + " sets with constant characters");
				completedCount = simCount;
			}
			result.setValue(score);
			String message = "Difference in log likelihoods = " + result.getDoubleValue();
			result.setValue(pvalue);
			if (result.isUnassigned())
				resultString.setValue(message + ". No p-value was calculated, see MesquiteLog for details");
			else
				resultString.setValue(message + ". p-value = " + result.getDoubleValue() + " (from " + completedCount + " simulations)" );
		}
		else {
			if (result != null)
				result.setValue(score);
			if (result.isUnassigned())
				resultString.setValue("No likelihood was calculated, see MesquiteLog for details");
			else
				resultString.setValue("Difference in log likelihoods = " + result.getDoubleValue());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}

	private boolean eitherCharacterConstant(Tree tree,CategoricalAdjustable char1, CategoricalAdjustable char2) {
		return (char1.isConstant(tree,tree.getRoot()) || char2.isConstant(tree,tree.getRoot()));
	}



	// Wayne, this might be a useful addition to DoubleArray??
	private int countAssigned(double[] data){
		int count = 0;
		for(int i=0;i<data.length;i++)
			if (!MesquiteDouble.isUnassigned(data[i]))
				count++;
		return count;
	}

	private CharacterDistribution[] getSimulatedCharacters(CategoricalAdjustable[] statesAtTips, Tree tree, PagelMatrixModel model, double[][] rootPriors, MesquiteLong seed){
		if (tree == null) {
			MesquiteMessage.warnProgrammer("tree null in Pagel94 getSimulatedCharacters ");
			return null;
		}
		if (statesAtTips == null){
			MesquiteMessage.warnProgrammer("statesAtTips is null in Pagel94 getSimulatedCharacters");
			return null;
		}
		for (int i=0;i<statesAtTips.length;i++) {
			if (!(statesAtTips[i] instanceof CategoricalAdjustable))
				statesAtTips[i] = new CategoricalAdjustable(tree.getTaxa(), tree.getTaxa().getNumTaxa());
			else
				statesAtTips[i] = (CategoricalAdjustable)((AdjustableDistribution)statesAtTips[i]).adjustSize(tree.getTaxa());
		}
		for (int i=0;i<statesAtTips.length;i++) {
			if (statesAtTips[i] instanceof CategoricalAdjustable)
				((CategoricalAdjustable)statesAtTips[i]).deassignStates();
		}
		//probabilityModel.setMaxStateSimulatable(probabilityModel.getMaxStateDefined()); //should allow user to set # states
		if (model == null) {
			MesquiteMessage.warnProgrammer("Model is null in Pagel94 getSimulatedCharacters");
			return null;
		}
		if (!model.isFullySpecified()) {
			MesquiteMessage.warnProgrammer("Model was not fully specified during estimate or wrong model used ");
			return null;
		}	
		//failed = false;
		for (int i=0;i<statesAtTips.length;i++){
			evolvingStates[i] =(CategoricalHistory) statesAtTips[i].adjustHistorySize(tree, evolvingStates[i]);
		}
		if (seed!=null)
			model.setSeed(seed.getValue());
		long[] rootstate = model.getRootStates(tree,rootPriors);
		for (int c=0;c<rootstate.length;c++)
			evolvingStates[c].setState(tree.getRoot(), rootstate[c]);  //starting rootCategoricalState.makeSet(0)); //
		evolve(tree, (CategoricalAdjustable[])statesAtTips, model, tree.getRoot());
		if (seed!=null)
			seed.setValue(model.getSeed());
		return statesAtTips;
	}
	/*.................................................................................................................*/

	private void evolve(Tree tree, CategoricalAdjustable[] statesAtTips, PagelMatrixModel model, int node) {
		if (node!=tree.getRoot()) {
			int size = statesAtTips.length;
			int[] stateAtAncestor = new int[size];
			int[] stateAtNode;
			for (int c=0;c<size;c++){
				long states = evolvingStates[c].getState(tree.motherOfNode(node));
				stateAtAncestor[c] = CategoricalState.minimum(states);
			}
			stateAtNode = model.evolveState(stateAtAncestor,tree,node);
			for (int c=0;c<size;c++){
				long states = CategoricalState.makeSet(stateAtNode[c]);
				evolvingStates[c].setState(node,states);
				if (tree.nodeIsTerminal(node)) {   				
					statesAtTips[c].setState(tree.taxonNumberOfNode(node), states);
				}
			}
		}
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			evolve(tree, statesAtTips, model, daughter);
	}
	/*.................................................................................................................*/


	public String getVeryShortName() {
		return "Test from Pagel1994";
	}

	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getName() {
		return "Pagel's 1994 test of correlated (discrete) character evolution";
	}

	public String getExplanation(){
		return "A statistical test, described in Pagel(1994), for nonindependent evolution of two discrete, binary characters";
	}

	// This isPrerelease also proxies for PagelMatrixModel in mesquite.correl.lib  
	public boolean isPrerelease(){
		return false;
	}

}



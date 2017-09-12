/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison & Peter Midford. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.stochchar.StochCharMapper;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.meristic.lib.MeristicState;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;
import mesquite.stochchar.CurrentProbModels.CurrentProbModels;
import mesquite.stochchar.CurrentProbModelsSim.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class StochCharMapper extends CharMapper {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ProbModelSourceLike.class, getName() + " needs an indication of what probabilistic model to apply to the character.",
		"The indicator of probabilistic models can be selected initially");
		EmployeeNeed e2 = registerEmployeeNeed(MargLikeAncStCLForModel.class, getName() + "  needs methods to calculate likelihoods.",
		"The methods to calculate likelihoods are chosen automatically according to the probability model used in the calculation");
	}
	CharacterDistribution observedStates;
	MargLikeAncStCLForModel reconstructTask;
	ProbModelSourceLike modelTask;
	MesquiteString modelTaskName;
	ProbPhenCategCharModel model;  //CharacterModel model;  - temporary restriction to nonMolecular models PEM 6-Jan-2006
	MesquiteNumber likelihood;
	boolean warnedNoCalc = false;
	CharacterHistory statesAtNodes;
	int oldNumTaxa;
	boolean oneAtATime = false;
	boolean oneAtATimeCHGBL = false;
	Random generator;
	double pi_s[];
	private MesquiteLong seed;
	private long originalSeed=System.currentTimeMillis(); //0L;
	int MAXEVENTS = 1000; 
	/*  	
get prior from model
enable prior editing within models
handle models needing estimation
	 */
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		hireAllEmployees(MargLikeAncStCLForModel.class);
		for (int i = 0; i<getNumberOfEmployees() && reconstructTask==null; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof MargLikeAncStCLForModel){
				((MargLikeAncStCLForModel)e).setReportCladeLocalValues(true);
			}


		}
		String purpose = whatIsMyPurpose();
		if (StringUtil.blank(purpose))
			purpose = "for " + getName();
		if (modelTask == null)
			modelTask = (ProbModelSourceLike)hireEmployee(ProbModelSourceLike.class, "Source of probability character models ("+ purpose + ")");
		if (modelTask == null)
			return sorry(getName() + " couldn't start because no source of models of character evolution obtained.");
		modelTaskName = new MesquiteString(modelTask.getName());
		likelihood = new MesquiteNumber();
		seed = new MesquiteLong(1);

		seed.setValue(originalSeed);
		generator = new Random(originalSeed);  //seed?
		MesquiteSubmenuSpec mss = addSubmenu(null, "Source of probability models", makeCommand("setModelSource", this), ProbModelSourceLike.class);
		mss.setCompatibilityCheck(new ModelCompatibilityInfo(ProbabilityCategCharModel.class, null));
		mss.setSelected(modelTaskName);
		addMenuItem("Set Seed (" + purpose + ")...", makeCommand("setSeed",  this));
		addMenuItem("Max. Events per Branch (Stoch. Char. Map.)...", makeCommand("setMax",  this));
		return true;
	}

	public void setOneCharacterAtATime(boolean chgbl){
		oneAtATimeCHGBL = chgbl;
		oneAtATime = true;
		modelTask.setOneCharacterAtATime(chgbl);
	}

	/*.................................................................................................................*/
	/** Returns citation for a modules*/
	public String getCitation(){
		return "Midford, P.E. & W.P. Maddison. 2006.  Stochastic character mapping module for Mesquite. Version 1.1.";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setModelSource ",modelTask);
		temp.addLine("setSeed " + originalSeed);
		temp.addLine("setMax " + MAXEVENTS);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module used to supply character models", "[name of module]", commandName, "setModelSource")) {
			ProbModelSourceLike temp=  (ProbModelSourceLike)replaceEmployee(ProbModelSourceLike.class, arguments, "Source of probability character models", modelTask);
			if (temp!=null) {
				modelTask= temp;
				incrementMenuResetSuppression();
				modelTaskName.setValue(modelTask.getName());
				if (oneAtATime)
					modelTask.setOneCharacterAtATime(oneAtATimeCHGBL);
				parametersChanged();
				decrementMenuResetSuppression();
			}
			return modelTask;
		}
		else if (checker.compare(this.getClass(), "Sets the random number seed to that passed", "[long integer seed]", commandName, "setSeed")) {
			long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
			if (!MesquiteLong.isCombinable(s)){
				String purpose = whatIsMyPurpose();
				if (StringUtil.blank(purpose))
					purpose = "for " + getName();
				s = MesquiteLong.queryLong(containerOfModule(), "Random number seed " + purpose, "Enter an integer value for the random number seed for character evolution simulation", originalSeed);
			}
			if (MesquiteLong.isCombinable(s)){
				originalSeed = s;
				seed.setValue(originalSeed);
				parametersChanged(); //?
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Sets the maximum number of events per branch", "[ integer]", commandName, "setMax")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(s)){
				s = MesquiteInteger.queryInteger(containerOfModule(), "Maximum Number of Events", "Maximum number of events per branch permitted in character evolution simulation", MAXEVENTS, 1, 1000000, true);
			}
			if (MesquiteInteger.isCombinable(s)){
				MAXEVENTS = s;

				parametersChanged(); //?
			}
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		if (m == modelTask)
			iQuit();
	}
	public boolean allowsStateWeightChoice(){
		return false;
	}
	private long loopSeed(long ic){
		generator.setSeed(originalSeed);

		long rnd = originalSeed;
		for (int i = 0; i<ic; i++)
			rnd =  generator.nextInt();
		generator.setSeed(rnd+1);
		return rnd + 1; 
	}
	boolean warned = false;
	boolean verbose = false;
	CharacterData oldData = null;
	CategoricalHistory cladeLikelihoods = null;
	ProbabilityCategCharModel tempModel;
	Tree tree;
	/*.................................................................................................................*/
	public  long getNumberOfMappings(){
		return MesquiteLong.infinite;
	}
  	public  void prepareForMappings(boolean permissiveOfNoSeparateMappings) {
  	}

	/*.................................................................................................................*/
	public  void setObservedStates(Tree tree, CharacterDistribution observedStates){
		if(verbose) MesquiteMessage.println("setObservedStates ");			
		this.observedStates = observedStates;
		if (tree==null || observedStates==null)
			return;
		this.tree = tree;
		likelihood.setToUnassigned();

		/* At present this redoes the likelihood calculations each time before sampling a history.  
		 * In future the likelihood calculations should be done once and cached, and redone only if tree or observedStates have changed
		 * since last request */

		//a barrier (temporary) while likelihood calculations support only simple categorical
		Class stateClass = observedStates.getStateClass();
		if (MeristicState.class.isAssignableFrom(stateClass) || DNAState.class.isAssignableFrom(stateClass) || ProteinState.class.isAssignableFrom(stateClass) || ContinuousState.class.isAssignableFrom(stateClass)) {
			String s = "Likelihood calculations cannot be performed ";
			if (DNAState.class.isAssignableFrom(stateClass))
				s += "currently with DNA or RNA data.  The calculations were not done for some characters.";
			else if (ProteinState.class.isAssignableFrom(stateClass))
				s += "currently with protein data.  The calculations were not done for some characters.";
			else if (ContinuousState.class.isAssignableFrom(stateClass))
				s += "currently with continuous valued data.  The calculations were not done for some characters.";
			else if (MeristicState.class.isAssignableFrom(stateClass))
				s += "currently with meristic data.  The calculations were not done for some characters.";
			if (!warnedNoCalc) {
				discreetAlert( s);
				warnedNoCalc = true;
			}
			cladeLikelihoods = null;
			return;
		}

		cladeLikelihoods = (CategoricalHistory)observedStates.adjustHistorySize(tree, cladeLikelihoods);
		ProbPhenCategCharModel origModel = null;
		//getting the model
		if (modelTask.getCharacterModel(observedStates) instanceof ProbPhenCategCharModel )
			origModel = (ProbPhenCategCharModel)modelTask.getCharacterModel(observedStates);
		if (origModel == null && !MesquiteThread.isScripting()){
			if (observedStates.getParentData()!= oldData)
				warned = false;
			if (!warned){
				if (observedStates.getParentData()!=null && modelTask instanceof CurrentProbModels) {
					oldData = observedStates.getParentData();
					if (AlertDialog.query(containerOfModule(), "Assign models?", "There are currently no probability models assigned to the characters.  Do you want to assign a model to all characters unassigned?")) {
						((CurrentProbModels)modelTask).chooseAndFillUnassignedCharacters(observedStates.getParentData());
						if (modelTask.getCharacterModel(observedStates) instanceof ProbPhenCategCharModel )
							origModel = (ProbPhenCategCharModel)modelTask.getCharacterModel(observedStates);
						else AlertDialog.notice(containerOfModule(),"Incompatible model","The model you selected is not compatible with Stochastic Character Mapping at this time.");
					}
				}
				else {
					discreetAlert( "Sorry, there is no probabilistic model of evolution available for the character; likelihood calculations cannot be accomplished.  Please make sure that the source of models chosen is compatible with this character type.");
					warned = true;
					return;
				}
			}
			warned = true;
		}


		//getting the reconstructing module
		if (reconstructTask == null || !reconstructTask.compatibleWithContext(origModel, observedStates)) { 
			reconstructTask = null;
			for (int i = 0; i<getNumberOfEmployees() && reconstructTask==null; i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof MargLikeAncStCLForModel)
					if (((MargLikeAncStCLForModel)e).compatibleWithContext(origModel, observedStates)) {
						reconstructTask=(MargLikeAncStCLForModel)e;
					}
			}
		}

		if (reconstructString != null)
			reconstructString.setValue("");
		//doing the likelihood calculations
		if (reconstructTask != null) { 
			//========  1 ===========
			if (verbose) MesquiteMessage.println("Step 1");
			//Nielsen's Step 1:  calculate the clade likelihoods.  In the future this will be done once and cached
			if (tempModel == null || tempModel.getClass() != origModel.getClass()){
				tempModel = (ProbabilityCategCharModel)origModel.cloneModelWithMotherLink(null);
    	 		tempModel.setUserVisible(false);
			}
			origModel.copyToClone(tempModel);
			model = (ProbPhenCategCharModel)tempModel;
			cladeLikelihoods.deassignStates();
			reconstructTask.estimateParameters( tree,  observedStates,  model, null);
			reconstructTask.calculateStates( tree,  observedStates,  cladeLikelihoods, model, reconstructString, likelihood);

		}
		else {
			String s = "Stochastic mapping cannot be performed because no module was found to perform the calculations for the probability model \"" + model.getName() + "\" with the characters specified.";

			if (!warnedNoCalc) {
				discreetAlert( s);
				warnedNoCalc = true;
			}
		}
	}
	MesquiteString reconstructString = new MesquiteString();
	public   boolean getMapping(long im, CharacterHistory resultStates, MesquiteString resultString){
	if (tree==null || observedStates==null || resultStates == null || !(resultStates instanceof CategoricalHistory))
			return false;

		resultStates.deassignStates();
		if (cladeLikelihoods == null || !cladeLikelihoods.frequenciesExist()){
			if (resultString!=null)
				resultString.append("Stochastic Character Mapping could not be done because likeihoods could not be calculated");
			return false;
		}
		if (verbose) MesquiteMessage.println("Step 2a");
		loopSeed(im);
		if (verbose) MesquiteMessage.println("mapping " + im);			
		//now take clade likelihoods and make a sampled history
		//get a copy of the results; we will then modify them into the sampled history
		resultStates = cladeLikelihoods.clone(resultStates);
		CategoricalHistory sampledHistory = (CategoricalHistory)resultStates;
		sampledHistory.disposeFrequencies(); //don't want to remember these
		//========  2a ===========
		//Nielsen's Step 2a: choose a state at the root based on prior at root and root likelihood's
		int stateCount = cladeLikelihoods.getMaxState()+1;
		if (stateCount>model.getMaxState()+1)
			stateCount= model.getMaxState()+1;
		//if (verbose) MesquiteMessage.println("cladeLikelihoods thinks maxState is" + maxState);


		pi_s = new double[stateCount];  //these are the pi(i)'s

		for (int i = 0;i<stateCount;i++)
			pi_s[i]=model.priorProbability(i);  //getting the prior from the model

		double denominator = 0.0;

		for (int possibleState = 0; possibleState<stateCount;possibleState++){
			double stateFrequency = cladeLikelihoods.getFrequency(tree.getRoot(),possibleState);
			if (verbose) MesquiteMessage.println("At root; stateFrequency for state " + possibleState + " is " + stateFrequency);
			if (MesquiteDouble.isCombinable(stateFrequency))
				denominator += pi_s[possibleState]*stateFrequency;
		}
		if (denominator == 0){  // something's gone wrong
			MesquiteMessage.printStackTrace("2a Bad denominator in root likelihood calculations " );
			if (resultString!=null)
				resultString.append("Stochastic Character Mapping failed:  2a Bad denominator in root likelihood calculations");
			return false;
		}
		if (verbose) MesquiteMessage.println("Denominator is " + denominator);
		double sum = 0.0;
		double randomValue = generator.nextDouble();


		for (int candidate = 0;candidate<stateCount;candidate++) {
			double candidateFrequency = cladeLikelihoods.getFrequency(tree.getRoot(),candidate);
			if (MesquiteDouble.isCombinable(candidateFrequency)) {
				sum += (pi_s[candidate]*candidateFrequency)/denominator;
				if (sum > randomValue) {
					sampledHistory.setState(tree.getRoot(),CategoricalState.makeSet(candidate));
					sampledHistory.addInternodeEvent(tree.getRoot(), new CategInternodeEvent(CategoricalState.makeSet(candidate), 0, false));
					if (verbose) MesquiteMessage.println("I picked " + candidate + " at the root.");
					break;
				}
			}
		}


		if (verbose) MesquiteMessage.println("sampledHistory at root is set to " + sampledHistory.getState(tree.getRoot()));

		//========  2b ===========
		//Nielsen's Step 2b: then go up through the tree recursively choosing a state at each node based on its clade likelihoods and the state chosen for its immediate ancestor 
		if (verbose) MesquiteMessage.println("Step 2b");
		if (!sampleStatesUp(tree,tree.getRoot(),cladeLikelihoods,sampledHistory,model)){
			if (resultString!=null)
				resultString.append("Stochastic Character Mapping failed:  2b Bad denominator in root likelihood calculations");
			
			return false;
		}

		//========  3 ===========
		//Nielsen's Step 3:  sample changes along the internodes 
		if (verbose) MesquiteMessage.println("Step 3");
		boolean success = sampleInternodeChanges(tree,tree.getRoot(),sampledHistory,model);
		if (!success){
			if (resultString!=null)
				resultString.append(" Number of events in sampled character history on a branch exceeds maximum allowed (" + MAXEVENTS + ")");
			resultStates.deassignStates();
		}
		else {
			if (resultString!=null)
				resultString.append("Stochastic Character Mapping\nBased on: " + reconstructString);
		}
		statesAtNodes=resultStates;
		return success;
	}

	// This implements the recursion for Nielsen's step 2b
	private boolean sampleStatesUp(Tree tree,int node,CategoricalHistory cladeLikelihoods,CategoricalHistory sampledHistory,ProbPhenCategCharModel model){
		int stateCount = cladeLikelihoods.getMaxState()+1;  // Pass this in?
		if (stateCount>model.getMaxState()+1)
			stateCount= model.getMaxState()+1;
		if (node != tree.getRoot()) {
			int motherSampledState = CategoricalState.getOnlyElement(sampledHistory.getState(tree.motherOfNode(node)));
			sampledHistory.addInternodeEvent(node, new CategInternodeEvent(sampledHistory.getState(tree.motherOfNode(node)), 0, false));

			// ask the model for transition values from the mother's state to each of the candidate states
			if (verbose) MesquiteMessage.println("Mother is " + tree.motherOfNode(node)+ " SampledState is "+ sampledHistory.getState(tree.motherOfNode(node)));
			double Pij[] = new double[stateCount];
			for (int j=0;j<stateCount;j++){
				Pij[j]= model.transitionProbability(motherSampledState,j,tree,node);
				if (verbose) MesquiteMessage.println("Pij["+node+","+j+"] set to "+ Pij[j]);
			}
			double denominator = 0.0;
			for (int possibleState = 0; possibleState < stateCount;possibleState++){
				double stateFrequency = cladeLikelihoods.getFrequency(node,possibleState); //Peter: this had been tree.getRoot() instead of node -- should be node?
				if (MesquiteDouble.isCombinable(stateFrequency))
					denominator += Pij[possibleState]*stateFrequency;
			}
			if (denominator == 0){  // something's gone wrong
				MesquiteMessage.printStackTrace("2b Bad denominator in node likelihood calculations " + stateCount);
				return false;
			}
			if (verbose) MesquiteMessage.println("denominator " + denominator);
			double sum = 0.0;
			double randomValue = generator.nextDouble();
			for (int candidate = 0;candidate<stateCount;candidate++) {
				double candidateFrequency = cladeLikelihoods.getFrequency(node,candidate);
				if (MesquiteDouble.isCombinable(candidateFrequency)) {
					sum += (Pij[candidate]*candidateFrequency)/denominator;
					if (verbose) MesquiteMessage.println("candidateFrequency " + candidateFrequency + " for state " + candidate + " at node " +node + " sum " + sum);
					if (sum > randomValue) {
						sampledHistory.setState(node,CategoricalState.makeSet(candidate));
						if (verbose) MesquiteMessage.println("I picked " + candidate + " at node " + node);
						break;
					}
				}
			}
		}
		if (tree.nodeIsInternal(node)){
			for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d)) {
				if (!sampleStatesUp(tree,d,cladeLikelihoods,sampledHistory,model))
					return false;
			}

		}
		return true;
	}

	/*.................................................................................................................*/
	/** This implements the recursion for Nielsen's step 3 */
	MesquiteInteger numChanges = new MesquiteInteger(0);
	private boolean sampleInternodeChanges(Tree tree,int node,CategoricalHistory sampledHistory,ProbPhenCategCharModel model) {

		if (node != tree.getRoot()) {
			int startState = CategoricalState.getOnlyElement(sampledHistory.getState(tree.motherOfNode(node)));
			int endState = CategoricalState.getOnlyElement(sampledHistory.getState(node));
			double iRate = model.instantaneousRate(startState,endState,tree,node);
			double branchLength = tree.getBranchLength(node, 1.0);
			Vector internodeEvents = null;
			numChanges.setValue(0);

			internodeEvents = doSimulation(tree,node,startState,endState,model,cladeLikelihoods, numChanges);
			if (numChanges.getValue()<0)
				return false;

			if (internodeEvents != null) {
				Iterator eventIterator = internodeEvents.iterator();
				while(eventIterator.hasNext()){
					CategInternodeEvent event = (CategInternodeEvent)eventIterator.next();
					sampledHistory.addInternodeEvent(node,event);

				}
			}
		}
		if (tree.nodeIsInternal(node)){
			for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d)) 
				if (!sampleInternodeChanges(tree,d,sampledHistory, model))
					return false;
		}
		return true;
	}

	/*.................................................................................................................*/
	private Vector doSimulation(Tree tree, int node, int start, int end, ProbPhenCategCharModel model,CategoricalHistory cladeLikelihoods, MesquiteInteger numChanges){
		double nextLength;
		double totalLength;
		double branchLength = tree.getBranchLength(node, 1.0);
		Vector result;
		int nextState;
		int stateCount = cladeLikelihoods.getMaxState()+1;

		do {
			result = null;
			nextState = start;
			totalLength = 0.0;

			while (totalLength < branchLength){
				// currentRate is negative (negative values on main diagonal); indicates when the currentState will change to something else
				double currentRate = model.instantaneousRate(nextState,nextState,tree,node);
				double randomValue = generator.nextDouble();
				nextLength = Math.log(randomValue)/currentRate;  //generate a number from exponential distribution
                                                                  //negative value in currentRate cancels the negative value from log(0<x<1)
				if (nextLength+totalLength<branchLength) { // a state change has been simulated to happen before the end of the branch
					if (result == null) result = new Vector();  // start recording changes if haven't already started
					double denominator = -1*currentRate;        // must equal -1* sum of the other transitions
					double stateChoice = generator.nextDouble();
					double sum = 0.0;
					for (int candidate = 0;candidate<stateCount;candidate++) {
						if (candidate != nextState){
							double candidateRate = model.instantaneousRate(nextState,candidate,tree,node);
							if (MesquiteDouble.isCombinable(candidateRate)) {

								sum += model.instantaneousRate(nextState,candidate,tree,node)/denominator;
								if (sum > stateChoice) {
									nextState = candidate;
									if (verbose) MesquiteMessage.println("I picked " + candidate + " at time " + (nextLength+totalLength)/branchLength + " from node " + node);
									CategInternodeEvent thisEvent = new CategInternodeEvent(CategoricalState.makeSet(candidate), (totalLength+nextLength)/branchLength, true);
									result.addElement(thisEvent);
									if (result.size() > MAXEVENTS){
										numChanges.setValue(-1);
										return null;
									}
									break;
								}
							}
						}
					}
				}
				totalLength +=nextLength;
			}
		} while(nextState != end);    
		return result;
	}


	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Stochastic Character Mapping (Categorical)";
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
		return "Coordinates reconstruction of ancestral states using stochastic character mapping. " ;
	}

}



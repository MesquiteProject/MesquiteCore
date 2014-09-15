/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 
. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.BiSSETreeCharSim;



import java.util.Vector;

import mesquite.lib.*;
import mesquite.categ.lib.*;
import mesquite.diverse.lib.*;

/** ======================================================================== */
public class BiSSETreeCharSim extends TreeCharSimulate {
	protected RandomBetween rng;
	protected double rateCharStateChange0 = 0.005; 
	protected double rateCharStateChange1 = 0.005; 
	protected double spnForState0 = 0.1;
	protected double spnForState1 = 0.1;
	protected double extForState0 = 0.05;
	protected double extForState1 = 0.05;
	private int ceilingSize = MesquiteInteger.unassigned;
	private int treeSize = MesquiteInteger.unassigned;
	private Vector goodTrees = new Vector();  // Don't need an actual tree vector here, since this only holds possible trees.
	private Vector goodHistories = new Vector();  // save the history as well!

	protected double prior1AtRoot = 0.5; //TODO: have toggle to set what is used
	protected boolean keepAllExtinct = false;

	//IF BOOLEAN SET THEN increment spnForState0 up by 0.01 or some such each new tree


	protected double eventRate0;    //will be set to rateCharStateChange0+spnForState0+extForState0
	protected double eventRate1;    //will be set to rateCharStateChange1+spnForState1+extForState1

	//codes for the three types of events
	protected static final int EXTINCTIONEVENT = 0;
	protected static final int SPECIATIONEVENT = 1;
	protected static final int CHANGEEVENT = 2;
	protected static final int ERROREVENT = 3;


	private double global0Rate;
	private double global1Rate;

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng= new RandomBetween(1);

		/*if (!MesquiteThread.isScripting()){//!
			if (!showDialog())
				return false;
		}*/
		addMenuItem("Rate of Change of Character change from state 0 to 1...", makeCommand("setCRate0",  this));
		addMenuItem("Rate of Change of Character change from state 1 to 0...", makeCommand("setCRate1", this));
		addMenuItem("Rate of Speciation if state 0...", makeCommand("setSRate0",  this));
		addMenuItem("Rate of Speciation if state 1...", makeCommand("setSRate1",  this));
		addMenuItem("Rate of Extinction if state 0...", makeCommand("setERate0",  this));
		addMenuItem("Rate of Extinction if state 1...", makeCommand("setERate1",  this));
	//	addMenuItem("Desired Tree size",makeCommand("setTreeSize",this));
		addMenuItem("Maximum Tree size to search", makeCommand("setCeilingSize",this));
	//	addMenuItem("Save Extinct lineages", makeCommand("toggleKeepAllExtinct",this));
		addMenuItem("Prior on state at root...", makeCommand("setPrior",  this));
		return true;
	}
	public boolean showDialog(Taxa taxa){
		if (taxa != null)
			treeSize = taxa.getNumTaxa();
		ceilingSize = (int)(1.2*treeSize);
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dlog = new ExtensibleDialog(containerOfModule(), "Simulation Parameters",  buttonPressed);
		dlog.defaultCancelLabel = null;
		DoubleField[] value = new DoubleField[7];
	//	IntegerField treeField;
		IntegerField ceilingField;
		//Checkbox keepExtinctBox;
		value[0] = dlog.addDoubleField("Rate 0 -> 1: ", rateCharStateChange0, 6, 0, 100);
		value[1] = dlog.addDoubleField("Rate 1 -> 0: ", rateCharStateChange1, 6, 0, 100);
		value[2] = dlog.addDoubleField("Rate of Speciation with 0: ", spnForState0, 6, 0, 100);
		value[3] = dlog.addDoubleField("Rate of Speciation with 1 :", spnForState1, 6, 0, 100);
		value[4] = dlog.addDoubleField("Rate of Extinction with 0: ", extForState0, 6, 0, 100);
		value[5] = dlog.addDoubleField("Rate of Extinction with 1: ", extForState1, 6, 0, 100);
		
		value[6] = dlog.addDoubleField("Prior probability of state 1 at root (? = use stationary freq.): ", MesquiteDouble.unassigned, 6, 0, 1.0);
		value[6].setPermitUnassigned(true);
		dlog.addLabel("Desired Tree Size: " + treeSize);
		ceilingField = dlog.addIntegerField("Continue to Tree Size: ", ceilingSize,4,3,10000);
		//keepExtinctBox = dlog.addCheckBox("Keep Extinct Lineages", keepAllExtinct);

		dlog.completeAndShowDialog(true);

		boolean ok = (dlog.query()==0);
		boolean success = false;

		if (ok) {
			success = true;
			for (int i=0; i<6; i++)
				if (!MesquiteDouble.isCombinable(value[i].getValue()))
					success = false;
		//	if (!MesquiteInteger.isCombinable(treeField.getValue()))
		//		success = false;
			if (!MesquiteInteger.isCombinable(ceilingField.getValue()))
				success = false;
			if (success & (ceilingField.getValue()< treeSize))  // not necessarily best place to enforce this
				success = false;
			if (!success){
				dlog.dispose();
				return false;
			}
			rateCharStateChange0 = value[0].getValue();
			rateCharStateChange1 = value[1].getValue();
			spnForState0 = value[2].getValue();
			spnForState1 = value[3].getValue();
			extForState0 = value[4].getValue();
			extForState1 = value[5].getValue();
			prior1AtRoot = value[6].getValue();
		//	treeSize = treeField.getValue();
			ceilingSize = ceilingField.getValue();
		//	keepAllExtinct = keepExtinctBox.getState();
		}
		else {
			dlog.dispose();   
			return false;
		}
		dlog.dispose();   
		return true;
	}
	/*.................................................................................................................*/
	public double stationaryFreq0() {  
		double d = spnForState0-spnForState1+extForState1-extForState0;
		double q01 = rateCharStateChange0;
		double q10 = rateCharStateChange1;
		if (Math.abs(d ) < 1e-100){
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
		double plus = (q01 + q10 - d + part) / (-2*d);
		double minus = (q01 + q10 - d - part) / (-2*d);
		if (minus < 0 || minus >1)
			return plus;
		else if (plus < 0 || plus >1)
			return minus;
		else
			return MesquiteDouble.unassigned;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setCRate0 " + rateCharStateChange0);
		temp.addLine("setCRate1 " + rateCharStateChange1);
		temp.addLine("setSRate0 " + spnForState0);
		temp.addLine("setSRate1 " + spnForState1);
		temp.addLine("setERate0 " + extForState0);
		temp.addLine("setERate1 " + extForState1);
		temp.addLine("setPrior " + prior1AtRoot);
		//temp.addLine("setTreeSize " + treeSize);
		temp.addLine("setCeilingSize " + ceilingSize);
		temp.addLine("toggleKeepAllExtinct " + MesquiteBoolean.toOffOnString(keepAllExtinct));
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the rate of change of the categorical character", "[number]", commandName, "setCRate0")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Evolution of Speciation Controlling Character 0 -> 1", rateCharStateChange0);
			if (MesquiteDouble.isCombinable(s)) {
				rateCharStateChange0 = s;
				eventRate0 = rateCharStateChange0+spnForState0+extForState0;
				prior1AtRoot =  1.0 - stationaryFreq0();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the rate of change of the categorical character", "[number]", commandName, "setCRate1")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Evolution of Speciation Controlling Character 1 -> 0", rateCharStateChange1);
			if (MesquiteDouble.isCombinable(s)) {
				rateCharStateChange1 = s;
				eventRate1 = rateCharStateChange1+spnForState1+extForState1;
				prior1AtRoot =  1.0 - stationaryFreq0();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the rate of speciation if 0", "[number]", commandName, "setSRate0")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Speciation if 0", spnForState0);
			if (MesquiteDouble.isCombinable(s)) {
				spnForState0 = s;
				eventRate0 = rateCharStateChange0+spnForState0+extForState0;
				prior1AtRoot =  1.0 - stationaryFreq0();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the rate of speciation if 1", "[number]", commandName, "setSRate1")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Speciation if 1", spnForState1);
			if (MesquiteDouble.isCombinable(s)) {
				spnForState1 = s;
				eventRate1 = rateCharStateChange1+spnForState1+extForState1;
				prior1AtRoot =  1.0 - stationaryFreq0();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the rate of extinction if 0", "[number]", commandName, "setERate0")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Extinction if 0", extForState0);
			if (MesquiteDouble.isCombinable(s)) {
				extForState0 = s;
				eventRate0 = rateCharStateChange0+spnForState0+extForState0;
				prior1AtRoot =  1.0 - stationaryFreq0();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the rate of extinction if 1", "[number]", commandName, "setERate1")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Extinction if 1", extForState1);
			if (MesquiteDouble.isCombinable(s)) {
				extForState1 = s;
				eventRate1 = rateCharStateChange1+spnForState1+extForState1;
				prior1AtRoot =  1.0 - stationaryFreq0();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		/*
		else if (checker.compare(this.getClass(), "Sets desired tree size", "[number]", commandName, "setTreeSize")) {
			pos.setValue(0);
			int s = MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteInteger.queryInteger(containerOfModule(), "Root state", "Probability of state 1 at root", treeSize);
			if (MesquiteInteger.isCombinable(s)) {
				treeSize = s;
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}*/
		else if (checker.compare(this.getClass(), "Sets tree size limit (to stop search)", "[number]", commandName, "setCeilingSize")) {
			pos.setValue(0);
			int s = MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteInteger.queryInteger(containerOfModule(), "Root state", "Probability of state 1 at root", ceilingSize);
			if (MesquiteInteger.isCombinable(s) && (s >= treeSize)) {
				ceilingSize = s;
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the prior probability of state 1 at root", "[number]", commandName, "setPrior")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Root state", "Probability of state 1 at root", prior1AtRoot);
			if (MesquiteDouble.isCombinable(s)) {
				prior1AtRoot = s;
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not extinct lineages are preserved", "[on or off]", commandName, "toggleKeepAllExtinct")) {
			if (StringUtil.blank(arguments))
				keepAllExtinct = !keepAllExtinct;
			else {
				String s = ParseUtil.getFirstToken(arguments, pos);
				if ("on".equalsIgnoreCase(s))
					keepAllExtinct = true;
				else if  ("off".equalsIgnoreCase(s))
					keepAllExtinct = false;
			}
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	protected long countSpeciations = 0;
	protected long countExtinctions = 0;
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	protected void flipState(long [] states, int index){
		if (states[index] == 1L)
			states[index] = 2L; //state 0 to 1
		else
			states[index] = 1L;//state 1 to 0
	}
	protected long numFlips;
	/*.................................................................................................................*/
	protected void addLengthToAllTerminals(MesquiteTree tree, int node, double increment, boolean[] taxaInTree){
		if (tree.nodeIsTerminal(node)) {
			int taxon = tree.taxonNumberOfNode(node);
			if (taxaInTree[taxon]){    //not extinct (keepAllExtinct means save, not continue growing)
				double current = tree.getBranchLength(node, MesquiteDouble.unassigned);
				if (MesquiteDouble.isCombinable(current))
					tree.setBranchLength(node, current + increment, false);
				else
					tree.setBranchLength(node, increment, false);  	 	
			}
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			addLengthToAllTerminals(tree, d, increment, taxaInTree);
		}
	}
	/*.................................................................................................................*/
	// This sums all the change rates of all the active terminal nodes in the tree
	protected double getGlobalRate(Tree tree,int node,boolean[] taxaInTree,long[] localHistory){
		if (tree.nodeIsTerminal(node)){
			int taxon = tree.taxonNumberOfNode(node);
			if (taxaInTree[taxon]){   //not extinct
				if (localHistory[node] == 1L){ //1L is state 0
					global0Rate += eventRate0;
					return eventRate0;
				}
				else{   //should be 2L, which is state 1
					if (localHistory[node] != 2L)
						MesquiteMessage.warnProgrammer("Character history has a bad state in getGlobalRate at " + node);
					global1Rate += eventRate1;
					return eventRate1;
				}
			}
			else
				return 0;
		}
		else {
			double result = 0;
			for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d))
				result += getGlobalRate(tree,d,taxaInTree,localHistory);
			return result;
		}
	}
	/*.................................................................................................................*/    
	// generates the time to the next event on the tree based on a negative exponential distribution
	protected double nextTime(double globalRate){
		return -1*Math.log(rng.nextDouble())/globalRate; 
	}

	/*.................................................................................................................*/
	// this picks the node where the next event is occurring; the chance of choosing a node
	// is proportional to the sum of its rates
	protected int nextNode(Tree tree,int node,boolean [] taxaInTree, long[] localHistory,double globalRate,double limit, MesquiteDouble sum){
		if (tree.nodeIsTerminal(node)){
			int taxon = tree.taxonNumberOfNode(node);
			if (taxaInTree[taxon]){   //not extinct
				if (localHistory[node] == 1L){ //1L is state 0
					sum.add(eventRate0/globalRate);
					if (sum.getValue()>limit)
						return node;
					else
						return -1;
				}
				else{   //should be 2L, which is state 1
					if (localHistory[node] != 2L)
						MesquiteMessage.warnProgrammer("Character history has a bad state in nextNode at " + node);
					sum.add(eventRate1/globalRate);
					if (sum.getValue()>limit)
						return node;
					else
						return -1;                
				}
			}
			else
				return -1;
		}
		else {
			for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d)){
				int result = nextNode(tree,d,taxaInTree,localHistory,globalRate,limit,sum);
				if (result>0)
					return result;
			}
			return -1;
		}
	}

	/*.................................................................................................................*/
	//choose which of the three event types it will be
	protected int nextChange(long [] localHistory, double limit,int node){
		if (node<0)
			return ERROREVENT;
		if (localHistory[node] == 1L){ //1L is state 0
			if (rateCharStateChange0/eventRate0 > limit)
				return CHANGEEVENT;
			else if (((rateCharStateChange0+spnForState0)/eventRate0) > limit)
				return SPECIATIONEVENT;
			else
				return EXTINCTIONEVENT;
		}
		else {  //should be 2L, which is state 1
			if (localHistory[node] != 2L)
				MesquiteMessage.warnProgrammer("Character history has a bad state in nextChange at " + node);
			if (rateCharStateChange1/eventRate1 > limit)
				return CHANGEEVENT;
			else if (((rateCharStateChange1+spnForState1)/eventRate1) > limit)
				return SPECIATIONEVENT;                
			else
				return EXTINCTIONEVENT;
		}
	}


	/*.................................................................................................................*/
	protected void executeEvent(MesquiteTree tree,int eventNode,int eventChange, boolean [] taxaInTree,long [] localHistory,MesquiteInteger countOfSpecies){
		switch (eventChange){
		case EXTINCTIONEVENT:{
			int it = tree.taxonNumberOfNode(eventNode);
			if (it >= 0 && taxaInTree[it]){
				if (!keepAllExtinct){
					tree.deleteClade(eventNode, false);
					countOfSpecies.decrement();
				}
				taxaInTree[it] = false;
				countExtinctions++;
				CommandRecord.tick("Went extinct at node " + eventNode + " ; total number of species " + countOfSpecies + "; total speciations: " + countSpeciations  + "; total extinctions: " + countExtinctions );
			}
			break;
		}
		case SPECIATIONEVENT:{
			int taxon = tree.taxonNumberOfNode(eventNode);
			if (taxon >=0 && taxaInTree[taxon]){
				long statesAtNode = localHistory[eventNode];
				tree.splitTerminal(taxon, -1, false);
				countSpeciations++;
				int firstD = tree.firstDaughterOfNode(eventNode);
				int lastD = tree.lastDaughterOfNode(eventNode);
				localHistory[firstD] = statesAtNode;
				localHistory[lastD] = statesAtNode;
				tree.setBranchLength(firstD, 0, false);
				tree.setBranchLength(lastD, 0, false);
				taxaInTree[tree.taxonNumberOfNode(firstD)] = true;
				taxaInTree[tree.taxonNumberOfNode(lastD)] = true;
				countOfSpecies.increment();
				CommandRecord.tick("Speciated at node " + eventNode + " ; total number of species " + countOfSpecies + "; total speciations: " + countSpeciations  + "; total extinctions: " + countExtinctions );
			}
			else{
				MesquiteMessage.warnProgrammer("Failed to speciate; taxon is " + taxon + "eventNode is " + eventNode);
			}
			break;
		}
		case CHANGEEVENT:{
			flipState(localHistory, eventNode);
			numFlips++;
			break;
		}
		}
	}



	/*.................................................................................................................*/
	public String getDataType(){
		return CategoricalData.DATATYPENAME;
	}

	protected MesquiteInteger countOfSpecies = new MesquiteInteger(0);
	protected MesquiteDouble accumulator = new MesquiteDouble(0.0);

	/*.................................................................................................................*/
	public  void doSimulation(Taxa taxa, int replicateNumber, ObjectContainer treeContainer, ObjectContainer characterHistoryContainer, MesquiteLong seed){
		if (!initialized)
			initialize(taxa);
		//save random seed used to make tree under tree.seed for use in recovering later
		rng.setSeed(seed.getValue());
		int originalNumTaxa = taxa.getNumTaxa();
		
		if (MesquiteInteger.isUnassigned(treeSize))
		    treeSize = originalNumTaxa;
		if (MesquiteInteger.isUnassigned(ceilingSize))
		    ceilingSize = (treeSize/5)*6;   //integer version of add 20%

		MesquiteTree tree = null;
		CategoricalHistory charHistory = null;
		long [] localHistory = null;
		
		int numTaxa = taxa.getNumTaxa();
		if (numTaxa < ceilingSize)
			taxa.addTaxa(numTaxa-1, ceilingSize-numTaxa, false);  // avoid error at end?
		numTaxa = taxa.getNumTaxa();
		if (numTaxa < ceilingSize)
			MesquiteMessage.warnProgrammer("Taxa block failed to grow to ceiling size; numTaxa = " + numTaxa);
		Object t = treeContainer.getObject();
		if (t == null || !(t instanceof MesquiteTree))
			tree = new MesquiteTree(taxa);
		else
			tree = (MesquiteTree)t;
		Object c = characterHistoryContainer.getObject();
		if (c == null || !(c instanceof CategoricalHistory)){
			//charHistory = new CategoricalHistory(taxa);  // remove
			localHistory = new long[tree.getNumNodeSpaces()];
			for(int i=0;i<localHistory.length;i++)
				localHistory[i]= CategoricalState.unassigned;
		}
		else { 
			charHistory = (CategoricalHistory)c;
			localHistory = new long[charHistory.getNumTaxa()];
			for(int i=0;i<localHistory.length;i++){
				localHistory[i]= charHistory.getState(i);
			}
		}

		int attempts = 0;
		boolean done = false;
		boolean wentExtinct = false;
		int patience = 100; //TODO: make this user settable
		boolean[] taxaInTree = new boolean[taxa.getNumTaxa()];
		for (int i=0; i< taxaInTree.length; i++)
			taxaInTree[i] = false;
		long state = 1L; //state 0
		String hitsString = "";
		while (attempts < patience && !done){
			countSpeciations = 0;
			countExtinctions = 0;
			countOfSpecies.setValue(2);
			goodTrees.clear();
			goodHistories.clear();

			wentExtinct = false;
			state = 1L; //state 0
			if (rng.nextDouble()<prior1AtRoot)
				state = 2L; //state 1
			for (int i=0; i<tree.getNumNodeSpaces(); i++){  //state 0
				//charHistory.setState(i, state);
				localHistory[i] = state;
			}

			tree.setToDefaultBush(2, false);
			for (int i=0; i< taxaInTree.length; i++)
				taxaInTree[i] = false;
			taxaInTree[0] = true;
			taxaInTree[1] = true;
			tree.setAllBranchLengths(0, false);

			double generations = 0;
			CommandRecord.tick("Attempt " + (attempts+1) + " to simulate tree ");
			numFlips =0;
			hitsString = "";
			while (countOfSpecies.getValue()<numTaxa && !wentExtinct){
				global0Rate = 0;
				global1Rate = 0;
				double globalRate = getGlobalRate(tree,tree.getRoot(),taxaInTree,localHistory);
				double eventTime = nextTime(globalRate);
				accumulator.setValue(0.0);
				double limit = rng.nextDouble();
				int eventNode = nextNode(tree,tree.getRoot(),taxaInTree,localHistory,globalRate,limit,accumulator);
				limit = rng.nextDouble();
				int eventChange = nextChange(localHistory, limit, eventNode);
				if (eventChange == ERROREVENT)
					discreetAlert("BiSSE simulation failed (no nextnode found).");
				generations += eventTime;
				addLengthToAllTerminals(tree,tree.getRoot(),eventTime,taxaInTree);
				executeEvent(tree,eventNode,eventChange,taxaInTree,localHistory,countOfSpecies);
				if (countOfSpecies.getValue() == 0 || tree.numberOfTerminalsInClade(tree.getRoot()) == 0){
					wentExtinct = true;             
					CommandRecord.tick("Extinction event (species in tree currently:  " + countOfSpecies.getValue() + ") [attempt: "+ (attempts+1) + "] ");
				}
				else if (eventChange == SPECIATIONEVENT){
					if (countOfSpecies.getValue() == treeSize){   // save a copy of the tree
						MesquiteTree ct = tree.cloneTree(); 
						globalRate = getGlobalRate(tree,tree.getRoot(),taxaInTree,localHistory);
						double nextTime = rng.nextDouble()*nextTime(globalRate);
						addLengthToAllTerminals(ct,ct.getRoot(),nextTime,taxaInTree);
						goodTrees.add(ct);
						goodHistories.add(localHistory.clone());
						hitsString = hitsString + "\t" + generations;
					}
					else if (keepAllExtinct){  // keeping extinct means numTaxa tracks countSpeciations
						if (countSpeciations > (numTaxa - 2)){
							taxa.addTaxa(numTaxa-1, 5, false);  //notify true or false?
							boolean [] newTaxaInTree = new boolean[numTaxa+5];
							int i;
							for(i=0;i<taxaInTree.length;i++)
								newTaxaInTree[i]=taxaInTree[i];
							for(;i<newTaxaInTree.length;i++)
								newTaxaInTree[i] = false;
							taxaInTree = newTaxaInTree;
							long[] newLocalHistory = new long[tree.getNumNodeSpaces()+20];
							for(i=0;i<tree.getNumNodeSpaces();i++)
								newLocalHistory[i] = localHistory[i];
							for(;i<newLocalHistory.length;i++)
								newLocalHistory[i] = CategoricalState.unassigned;
						}
					}

					CommandRecord.tick("Speciation event (species in tree currently:  " + countOfSpecies.getValue()  + ") [attempt: "+ (attempts+1) + "] ");
				}
			}
			if (!wentExtinct){
				done = true;

				tree.reshuffleTerminals(rng);
			}
			attempts++;
		}
		if (!done){
			tree.setToDefaultBush(2, false);
			tree.setAllBranchLengths(0, false);
			for(int i=0;i<localHistory.length;i++)
				localHistory[i]= CategoricalState.unassigned;
			goodTrees.clear();
			MesquiteMessage.warnUser("Attempt to simulate speciation/extinction failed because clade went entirely extinct " + patience + " times");
		}
		else  {
			hitsString = "";
			if (goodTrees.size() == 0)
				MesquiteMessage.warnProgrammer("Problem with saving trees of predetermined size - vector is empty");
			else if (goodTrees.size() ==1){
				tree = (MesquiteTree)goodTrees.get(0);
				charHistory = new CategoricalHistory(taxa);
				charHistory.setStates((long []) goodHistories.get(0));
			}
			else {
				int treeCount = goodTrees.size();
				final double incr = 1.0/(double)treeCount;
				final double choose = rng.nextDouble();
				int acc = 0;
				boolean d = false;
				while (!d){
					acc++;
					if (choose < (acc*incr)){
						d = true;
					}
				}
				MesquiteMessage.println("Chose " + acc + " of " + treeCount + " trees.");
				tree = (MesquiteTree)goodTrees.get(acc-1);
				if (true){
				int[] terminals = tree.getTerminalTaxa(tree.getRoot());
				for (int i =0; i< terminals.length; i++){
					if (terminals[i]>= treeSize){ //taxon that needs packing to lower value
						final int k = findVacant(terminals, treeSize);
						if (k >=0){
							int node = tree.nodeOfTaxonNumber(terminals[i]);
							tree.setTaxonNumber(node, k, false);
							terminals[i] = k;
						}
					}
				}

				tree.reshuffleTerminals(rng);
				}
				charHistory = new CategoricalHistory(taxa);
				charHistory.setStates((long []) goodHistories.get(acc-1));
			}
			MesquiteMessage.println("Tree and character " + (replicateNumber +1) + " successfully evolved.");
			tree.setName("Sim. sp/ext with char. " + (replicateNumber +1) + " [#ext. " + countExtinctions + " #st.chg. " + numFlips + " root " + CategoricalState.toString(state) + "]" + hitsString);
			if (!keepAllExtinct){
				int taxSize = MesquiteInteger.minimum(treeSize, originalNumTaxa); 
			    if (originalNumTaxa ==3)   //oops, originalNumTaxa was the uninformative default, trust treeSize here
			        taxSize = treeSize; 
				if (taxSize < taxa.getNumTaxa())
					taxa.deleteTaxa(taxSize, taxa.getNumTaxa()-taxSize, false);
			}
		}
		treeContainer.setObject(tree);
		characterHistoryContainer.setObject(charHistory);
		seed.setValue(rng.nextLong());  //see for next time

	}
	public int findVacant(int[] terminals, int targetTreeSize){
		for (int i=0; i<targetTreeSize; i++)
			if (IntegerArray.indexOf(terminals, i)<0)
				return i;
		return -1;
	}
	boolean initialized = false;
	public void initialize(Taxa taxa){
		initialized = true;
		if (!MesquiteThread.isScripting()){
		    showDialog(taxa);
		}
		eventRate0 = rateCharStateChange0+spnForState0+extForState0;
		eventRate1 = rateCharStateChange1+spnForState1+extForState1;
		if (prior1AtRoot == MesquiteDouble.unassigned)
			prior1AtRoot =  1.0 - stationaryFreq0();
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "BiSSE Trees & Characters";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Generates tree by a speciation/extinction model in which a character controls rates of speciation/extinction.  Time to next event drawn from negative exponential distribution." ;
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return "Rates: " + MesquiteDouble.toStringDigitsSpecified(rateCharStateChange0, 4) + "," + MesquiteDouble.toStringDigitsSpecified(rateCharStateChange1, 4) + "/" + MesquiteDouble.toStringDigitsSpecified(spnForState0, 4) + "," +MesquiteDouble.toStringDigitsSpecified(spnForState1, 4) + "/" +MesquiteDouble.toStringDigitsSpecified(extForState0, 4) + "," +MesquiteDouble.toStringDigitsSpecified(extForState1, 4);
	}

	/*.................................................................................................................*/
}


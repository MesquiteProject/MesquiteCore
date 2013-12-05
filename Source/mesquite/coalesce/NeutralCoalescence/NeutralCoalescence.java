/* Mesquite source code.  Copyright 1997-2010 W. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.NeutralCoalescence;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
/* ======================================================================== */
public class NeutralCoalescence extends CoalescerObed {  //coalescer
	RandomBetween rNG;
	ExponentialDistribution waitingTime;
	int basePopulationSize = 10000;
	CoalescedNode[] thisGeneration, previousGeneration, temp;
	long initialSeed = System.currentTimeMillis(); 
	MesquiteBoolean exponentialApprox;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		rNG= new RandomBetween(initialSeed);
 		waitingTime = new ExponentialDistribution(initialSeed);
 		exponentialApprox = new MesquiteBoolean(true);
 		MesquiteSubmenuSpec csub = addSubmenu(null, "Coalescence simulation");
 		if (getHiredAs() != CoalescerObed.class)
 			addItemToSubmenu(null, csub, "Set Ne...", makeCommand("setEffective", this));
 		addCheckMenuItemToSubmenu(null, csub, "Exponential approximation", makeCommand("toggleExponential", this), exponentialApprox);
 		if (!MesquiteThread.isScripting()){
				int eff = MesquiteInteger.queryInteger(containerOfModule(), "Population Size", "Set Effective population size", basePopulationSize, 10, 1000000000, false);
				if (MesquiteInteger.isCombinable(eff))
					basePopulationSize = eff;
 		}
 		return true;
  	 }
  	 
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
 		if (getHiredAs() != CoalescerObed.class)
  	 		temp.addLine("setEffective " + basePopulationSize);
		temp.addLine("toggleExponential " + exponentialApprox.toOffOnString());
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the effective population size for coalescence simulations", "[effective population size]", commandName, "setEffective")) {
    	 		int eff = MesquiteInteger.fromFirstToken(arguments, pos);
    	 		if (!MesquiteInteger.isCombinable(eff))
				eff = MesquiteInteger.queryInteger(containerOfModule(), "Population Size", "Set Effective population size", basePopulationSize);
    	 		if (!MesquiteInteger.isCombinable(eff))
    	 			return null;
	    	 	if (basePopulationSize != eff) {
	    	 		basePopulationSize = eff;
 				parametersChanged(); 
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether to use the exponential approximation or to simulate full population with individual genes", "[on = use exponential approximation; off]", commandName, "toggleExponential")) {
    	 		boolean current = exponentialApprox.getValue();
    	 		if (StringUtil.blank(arguments)) {
    	 			exponentialApprox.setValue(!current);
	    	 		parametersChanged();
    	 		}
    	 		else {
	    	 		exponentialApprox.toggleValue(parser.getFirstToken(arguments));
	    	 		if (current!=exponentialApprox.getValue()) {
	    	 			parametersChanged();
	    	 		}
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.....................................UTILITIES.........................................................*/
   	public int getPopulationSize(){
   		return basePopulationSize;
   	}
   	public void setPopulationSize(int pS){
   		basePopulationSize = pS;
   	}
	boolean warnPopSizeSmall = true;
	boolean firstWarning = true;
	int[] ancestorChoices;
	int[] ancestorChoicesSummary;
	int[] ancestorChoicesCounts;
	

	void recordChoice(int ancestorChosen){
		int pos = -1;
		int i;
		for (i=0; i<ancestorChoicesSummary.length && pos<0 && ancestorChoicesSummary[i]!= MesquiteInteger.unassigned; i++)
			if (ancestorChoicesSummary[i]==ancestorChosen)
				pos = i;
		if (pos <0 && i<ancestorChoicesSummary.length) {
			ancestorChoicesSummary[i]=ancestorChosen;
			ancestorChoicesCounts[i]=1;
		}
		else
			ancestorChoicesCounts[pos]++;
	}
	boolean warnNoMatch = true;
	boolean warnNoMotherError = true;
	/*.................................................................................................................*/
   	private void coalesceIndividualModel(MesquiteTree tree,  int[] containing, int whichContaining, int populationSize, long generations, MesquiteLong seed, boolean untilOne) {
		
		if (ancestorChoices==null|| ancestorChoices.length < tree.getNumNodeSpaces()) {
			ancestorChoices = new int[tree.getNumNodeSpaces()]; 
			ancestorChoicesSummary = new int[tree.getNumNodeSpaces()]; 
			ancestorChoicesCounts = new int[tree.getNumNodeSpaces()]; 
		}
			
		for (int i=0; i<ancestorChoicesSummary.length; i++) {
			ancestorChoicesSummary[i]=MesquiteInteger.unassigned;
			ancestorChoicesCounts[i] = 0;
			ancestorChoices[i] = -1;
		}
		
		int numGenerations = 0;
		while (IntegerArray.moreThanOneOccurrence(containing, whichContaining) && (numGenerations<generations || untilOne) ) {
			for (int i=0; i<ancestorChoicesSummary.length && ancestorChoicesSummary[i]!=MesquiteInteger.unassigned; i++) {
				ancestorChoicesSummary[i]=MesquiteInteger.unassigned;
				ancestorChoicesCounts[i] = 0;
			}
			for (int i=0; i<containing.length; i++){ //adding to branch lengths
				if (containing[i] == whichContaining)  //this is a gene tree node contained in the populations
					tree.setBranchLength(i, tree.getBranchLength(i, 0)+1, false);
			}			
			for (int i=0; i<containing.length && i<ancestorChoices.length; i++){ //choosing ancestor randomly
				if (containing[i] == whichContaining){  //this is a gene tree node contained in the populations
					ancestorChoices[i] = rNG.randomIntBetween(0, populationSize-1);  //choosing an ancestor
					recordChoice(ancestorChoices[i]);
				}
				else
					ancestorChoices[i]=-1;
			}	
			for (int c=0; c<ancestorChoicesSummary.length && ancestorChoicesSummary[c]!=MesquiteInteger.unassigned; c++){ //looking for ancestor numbers
				int numThatChoseThis = ancestorChoicesCounts[c];
				if (numThatChoseThis>1){
					if (numThatChoseThis==2){
						// coalesce two that chose this
						int ancestorChoice = ancestorChoicesSummary[c];
						int i = IntegerArray.indexOf(ancestorChoices, ancestorChoice);
						int oldContaining = containing[i];
						containing[i]=0;
						ancestorChoices[i] = -1;
						int j = IntegerArray.indexOf(ancestorChoices, ancestorChoice);
						containing[j]=0;
						ancestorChoices[j] = -1;
						double iLength = tree.getBranchLength(i, 0);
						double jLength = tree.getBranchLength(j, 0);
						tree.moveBranch(i, j, false);
						tree.setBranchLength(i, iLength, false);
						tree.setBranchLength(j, jLength, false);
						if (tree.nodeExists(tree.motherOfNode(i))){
							tree.setBranchLength(tree.motherOfNode(i), 0, false);
							containing[tree.motherOfNode(i)]=whichContaining;
							ancestorChoices[tree.motherOfNode(i)]=-1;
						}
						else {
							if (warnNoMotherError){
								String w = "There has been an error in the Neutral Coalescence simulation.  It may be due to a contained taxon (e.g., haplotype) occurring in more than one containing taxon (e.g., species), which is not allowed in the current version of the simulation.  Details are written in the Mesquite Log file";
								w+="  This error message will not be repeated during this calculation.";
								discreetAlert( w);
								logln("Details of Neutral Coalescence error: mother doesn't exist (from node " + i + " oldContaining " + oldContaining + "  whichContaining " + whichContaining + " containing.length " + containing.length + " ancestorChoices.length " + ancestorChoices.length + "  ancestorChoice " + ancestorChoice + " c " + c + ") tree " + tree.writeTreeWithNodeNumbers());
								logln("  ancestor choices " + IntegerArray.toString(ancestorChoices));
							}
							warnNoMotherError = false;
							return;
						}
					}
					else {
						// coalesce the first two that chose this
						int ancestorChoice = ancestorChoicesSummary[c];
						int i = IntegerArray.indexOf(ancestorChoices, ancestorChoice);
						containing[i]=0;
						ancestorChoices[i] = -1;
						int j = IntegerArray.indexOf(ancestorChoices, ancestorChoice);
						containing[j]=0;
						ancestorChoices[j] = -1;
						double iLength = tree.getBranchLength(i, 0);
						double jLength = tree.getBranchLength(j, 0);
						tree.moveBranch(i, j, false);
						tree.setBranchLength(i, iLength, false);
						tree.setBranchLength(j, jLength, false);
						int newNode = tree.motherOfNode(i);
						if (!tree.nodeExists(newNode))
								MesquiteMessage.warnProgrammer("oops newNode doesn't exist ");
						for (int k=2; k<numThatChoseThis;k++){
							j = IntegerArray.indexOf(ancestorChoices, ancestorChoice);
							if (j<0)
								MesquiteMessage.warnProgrammer("oops index negative ");
							else {
								containing[j]=0;
								ancestorChoices[j] = -1;
								jLength = tree.getBranchLength(j, 0);
								tree.moveBranch(j, newNode, false);
								int new2Node = tree.motherOfNode(newNode);
								tree.collapseBranch(newNode, false);
								tree.setBranchLength(j, jLength, false);
								newNode = new2Node;
							}
						}
						tree.setBranchLength(newNode, 0, false);
						containing[newNode]=whichContaining;
						ancestorChoices[newNode]=-1;
					}
				}
			}	
			numGenerations++;
		}
		if (!IntegerArray.moreThanOneOccurrence(containing, whichContaining) &&  (numGenerations<generations) && MesquiteLong.isCombinable(generations)) {
				int lone  = IntegerArray.indexOf(containing, whichContaining);
				if (tree.nodeExists(lone))
					tree.setBranchLength(lone, tree.getBranchLength(lone, 0) + (generations-numGenerations +1), false);
		}
			
		seed.setValue(rNG.nextLong());
   	}
	/*.................................................................................................................*/
   	private long nChoose2(int n){
   		return n*(n-1)/2;
   	}
   	private int matchingIndex(int[] containing, int whichContaining, int iTH){
   		int count =0;
   		for (int i=0; i<containing.length; i++){
   			if (containing[i]==whichContaining){
   				if (iTH == count)
   					return i;
   				count++;
   			}
   		}
   		return -1;
   	}

	/*.................................................................................................................*/
   	private void coalesceQuick(MesquiteTree tree,  int[] containing, int whichContaining, int populationSize, long generations, MesquiteLong seed, boolean untilOne) {
		int numContained = IntegerArray.countSame(containing, whichContaining); //number contained gene copies in whichContaining node

		long numGenerations = 0;
		while (IntegerArray.moreThanOneOccurrence(containing, whichContaining) && (numGenerations<generations || untilOne) ) {
			double wait = waitingTime.nextExponential(populationSize*1.0/nChoose2(numContained));
			if (numGenerations + (long)wait <generations || untilOne){  //next waiting time is still within range allowed
				for (int i=0; i<containing.length; i++){ //adding to branch lengths
					if (containing[i] == whichContaining)  //this is a gene tree node contained in the populations
						tree.setBranchLength(i, tree.getBranchLength(i, 0)+(double)((long)wait), false);
				}			
				// coalesce two
				int iTH = rNG.randomIntBetween(0, numContained-1);
				int jTH = rNG.randomIntBetween(0, numContained-1);
				while (jTH==iTH)
					jTH = rNG.randomIntBetween(0, numContained-1);
					
				int i = matchingIndex(containing, whichContaining, iTH);
				int j = matchingIndex(containing, whichContaining, jTH);
				if (i<0 || j < 0) {
					if (warnNoMatch){
						String w = "There has been an error in the Neutral Coalescence simulation.  It may be due to a contained taxon (e.g., haplotype) occurring in more than one containing taxon (e.g., species), which is not allowed in the current version of the simulation.  Details are written in the Mesquite Log file";
						w+="  This error message will not be repeated during this calculation.";
						discreetAlert( w);
						logln("Details of Neutral Coalescence error: matching index not found (from node " + i + "  whichContaining " + whichContaining + " containing.length " + containing.length + ") tree " + tree.writeTreeWithNodeNumbers());
					}
					warnNoMatch = false;
					return;
				}
				containing[i]=0;
				containing[j]=0;

				double iLength = tree.getBranchLength(i, 0);
				double jLength = tree.getBranchLength(j, 0);
				tree.moveBranch(i, j, false);
				numContained--;
				tree.setBranchLength(i, iLength, false);
				tree.setBranchLength(j, jLength, false);
				if (tree.nodeExists(tree.motherOfNode(i))){
					tree.setBranchLength(tree.motherOfNode(i), 0, false);
					containing[tree.motherOfNode(i)]=whichContaining;
				}
				else {
					if (warnNoMotherError){
						String w = "There has been an error in the Neutral Coalescence simulation.  It may be due to a contained taxon (e.g., haplotype) occurring in more than one containing taxon (e.g., species), which is not allowed in the current version of the simulation.  Details are written in the Mesquite Log file";
						w+="  This error message will not be repeated during this calculation.";
						discreetAlert( w);
						logln("Details of Neutral Coalescence error: mother doesn't exist (from node " + i  + "  whichContaining " + whichContaining + " containing.length " + containing.length + ") tree " + tree.writeTreeWithNodeNumbers());
						logln("  ancestor choices " + IntegerArray.toString(ancestorChoices));
					}
					warnNoMotherError = false;
					return;
				}
			}
			else {
				for (int i=0; i<containing.length; i++){ //adding to branch lengths
					if (containing[i] == whichContaining)  //this is a gene tree node contained in the populations
						tree.setBranchLength(i, tree.getBranchLength(i, 0)+(double)(generations-numGenerations), false);
				}
			}			
			numGenerations+= (long)wait;
		}
		if (numGenerations<generations) { // didn't exceed waiting time; thus must have ended loop because it went to one
				for (int i=0; i<containing.length; i++){ //adding to branch lengths
					if (containing[i] == whichContaining)  //this is a gene tree node contained in the populations
						tree.setBranchLength(i, tree.getBranchLength(i, 0)+(double)(generations-numGenerations), false);
				}
		}
		seed.setValue(waitingTime.nextLong());
   	}
	int numWarnings =0;
	/*.................................................................................................................*/
	/**Passed as arguments are: 
		- the contained tree, 
		- an integer array indicating for each of the nodes in the tree what node in the containing tree contains it 
		- which node of the containing tree one is coalescing within
		- the Ne adjustment, i.e. the multiplier of the base population size (e.g., from the branch width)
		- the number of generations allowed for coalescence.
		- the random number seed
		- whether coalescence is to go to completion, or merely for the specified number of generations
		*/
   	public void coalesce(MesquiteTree tree,  int[] containing, int whichContaining, double neAdjustment, long generations, MesquiteLong seed, boolean untilOne) {
		if (generations ==0 && !untilOne)
			return;
		if (tree == null)
			return;
		if (containing == null)
			return;
		if (!MesquiteLong.isCombinable(generations))
			untilOne = true;
		int populationSize;
		if (neAdjustment == 1.0)
			populationSize = (int)basePopulationSize;
		else
			populationSize = (int)(basePopulationSize * neAdjustment);
		//if populationSize is below geneCopies, use exactly the number of gene copies
		//if ne is not combinable or not positive, use defaultPopSize
		rNG.setSeed(seed.getValue());
		waitingTime.setSeed(seed.getValue());

		int numContained = IntegerArray.countSame(containing, whichContaining);
		if (numContained> populationSize) {
			if (firstWarning) {
				firstWarning = false;
				if (MesquiteThread.isScripting() || !AlertDialog.query(containerOfModule(), "Pop. size too small", "Population size too small for number of gene copies; will be reset (Ne = " + populationSize + ", # copies = " + numContained + ").  Do you want to continue to be warned of this in the log window?", "Yes", "No"))
					warnPopSizeSmall = false;
			}
			else if (warnPopSizeSmall && numWarnings++<5)
				MesquiteMessage.warnProgrammer("Population size too small for number of gene copies; will be reset (ne = " + populationSize + ", # copies = " + numContained + ")");
			else if (warnPopSizeSmall && numWarnings==6)
				MesquiteMessage.warnProgrammer("Population size too small for number of gene copies; will be reset.  Subsequent warnings will be suppressed.");
			populationSize = numContained;
		}
		if (!exponentialApprox.getValue())
			coalesceIndividualModel(tree, containing, whichContaining, populationSize, generations, seed, untilOne);
		else
			coalesceQuick(tree, containing, whichContaining, populationSize, generations, seed, untilOne);
   	}
   	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Neutral Coalescence";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs neutral coalescence within a population by a simple model of a neutral gene with constant population size."
 		+"Remembers a tree of nodes, with branch lengths assigned according to generation of coalescence.  The default population size is 10000." ;
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getParameters() {
 		return "Base population size: " + basePopulationSize;
   	 }
}



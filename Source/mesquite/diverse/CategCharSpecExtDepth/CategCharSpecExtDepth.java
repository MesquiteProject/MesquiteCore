/* Mesquite source code.  Copyright 1997-2002 W. Maddison & D. Maddison. 
Version 0.992.  September 2002.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.CategCharSpecExtDepth;

import mesquite.categ.lib.CategoricalHistory;
import mesquite.categ.lib.CategoricalState;
import mesquite.diverse.CategCharSpecExtNExp.CategCharSpecExtNExp;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleField;
import mesquite.lib.ExtensibleDialog;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteTree;
import mesquite.lib.ObjectContainer;
import mesquite.lib.RandomBetween;
import mesquite.lib.Snapshot;
import mesquite.lib.Taxa;

public class CategCharSpecExtDepth extends CategCharSpecExtNExp {

	private int expectedSize = 500;
	private final static int maxSizeMultiple = 4;  // size of largest tree in terms of expected size; fail if bigger
	private boolean keepAllExtinct = false;


	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		rng= new RandomBetween(1);
		if (!commandRec.scripting()){ //!
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dlog = new ExtensibleDialog(containerOfModule(), "Simulation Parameters",  buttonPressed);
			DoubleField[] value = new DoubleField[6];
			IntegerField treeSize;
			value[0] = dlog.addDoubleField("Rate 0 -> 1: ", rateCharStateChange0, 6, 0, 100);
			value[1] = dlog.addDoubleField("Rate 1 -> 0: ", rateCharStateChange1, 6, 0, 100);
			value[2] = dlog.addDoubleField("Rate Speciation with 0: ", spnForState0, 6, 0, 100);
			value[3] = dlog.addDoubleField("Rate Speciation with 1 :", spnForState1, 6, 0, 100);
			value[4] = dlog.addDoubleField("Rate Extinction with 0: ", extForState0, 6, 0, 100);
			value[5] = dlog.addDoubleField("Rate Extinction with 1: ", extForState1, 6, 0, 100);
			treeSize = dlog.addIntegerField("Expected Tree Size: ", expectedSize, 4, 3, 10000);
			dlog.completeAndShowDialog(true);

			boolean ok = (dlog.query()==0);
			boolean success = false;

			if (ok) {
				success = true;
				for (int i=0; i<6; i++)
					if (!MesquiteDouble.isCombinable(value[i].getValue()))
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
				expectedSize = treeSize.getValue();
			}
			else
				return false;

			dlog.dispose();
		}

		eventRate0 = rateCharStateChange0+spnForState0+extForState0;
		eventRate1 = rateCharStateChange1+spnForState1+extForState1;

		prior1AtRoot =  1.0 - stationaryFreq0();

		addMenuItem("Rate of Change of Character change from state 0 to 1...", makeCommand("setCRate0",  this));
		addMenuItem("Rate of Change of Character change from state 1 to 0...", makeCommand("setCRate1", this));
		addMenuItem("Rate of Speciation if state 0...", makeCommand("setSRate0",  this));
		addMenuItem("Rate of Speciation if state 1...", makeCommand("setSRate1",  this));
		addMenuItem("Rate of Extinction if state 0...", makeCommand("setERate0",  this));
		addMenuItem("Rate of Extinction if state 1...", makeCommand("setERate1",  this));
		addMenuItem("Expected Tree Size...",makeCommand("setExpectedSize", this));
		addMenuItem("Prior on state at root...", makeCommand("setPrior",  this));
		return true;
	}

	private double timeForBreadth(int breadth){
		double xhat = stationaryFreq0();
		return Math.log((double)breadth)/(xhat*(spnForState0-extForState0) + (1-xhat)*(spnForState1-extForState1));
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
		temp.addLine("setExpectedSize " + expectedSize);
		temp.addLine("setPrior " + prior1AtRoot);
		return temp;
	}

	private MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the rate of change of the categorical character", "[number]", commandName, "setCRate0")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Evolution of Speciation Controlling Character 0 -> 1", rateCharStateChange0);
			if (MesquiteDouble.isCombinable(s)) {
				rateCharStateChange0 = s;
				eventRate0 = rateCharStateChange0+spnForState0+extForState0;
				prior1AtRoot =  1.0 - stationaryFreq0();
				if (!commandRec.scripting())
					parametersChanged(null, commandRec);
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
				if (!commandRec.scripting())
					parametersChanged(null, commandRec);
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
				if (!commandRec.scripting())
					parametersChanged(null, commandRec);
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
				if (!commandRec.scripting())
					parametersChanged(null, commandRec);
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
				if (!commandRec.scripting())
					parametersChanged(null, commandRec);
			}
		}
		else if (checker.compare(this.getClass(), "Sets the rate of extinction if 1", "[number]", commandName, "setERate1")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Extinction if 1", extForState1);
			if (MesquiteDouble.isCombinable(s)) {
				extForState1 = s;
				prior1AtRoot =  1.0 - stationaryFreq0();
				eventRate1 = rateCharStateChange1+spnForState1+extForState1;
				if (!commandRec.scripting())
					parametersChanged(null, commandRec);
			}
		}
		else if (checker.compare(this.getClass(), "Sets the expected size of trees (to calculate depth)", "[number]", commandName, "setExpectedSize")) {
			pos.setValue(0);
			int t = MesquiteInteger.fromString(arguments,pos);
			if (!MesquiteInteger.isCombinable(t))
				t = MesquiteInteger.queryInteger(containerOfModule(), "Size", "Expected Tree Size", expectedSize);
			if (MesquiteInteger.isCombinable(t)){
				expectedSize = t;
				if (!commandRec.scripting())
					parametersChanged(null,commandRec);
			}
		}
		else if (checker.compare(this.getClass(), "Sets the prior probability of state 1 at root", "[number]", commandName, "setPrior")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Root state", "Probability of state 1 at root", prior1AtRoot);
			if (MesquiteDouble.isCombinable(s)) {
				prior1AtRoot = s;
				if (!commandRec.scripting())
					parametersChanged(null, commandRec);
			}
		}
		else return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}

	/*.................................................................................................................*/
	protected void executeEvent(MesquiteTree tree,int eventNode, int eventChange,boolean [] taxaInTree, long [] localHistory,MesquiteInteger countOfSpecies, CommandRecord commandRec){
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
				//commandRec.tick("Went extinct at node " + eventNode + " ; total number of species " + countOfSpecies + "; total speciations: " + countSpeciations  + "; total extinctions: " + countExtinctions );
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
				if (tree.taxonNumberOfNode(lastD) == -1)
					Debugg.println("lastD =" + lastD + "; taxonNumber is " + tree.taxonNumberOfNode(lastD));
				localHistory[firstD]=statesAtNode;
				localHistory[lastD] = statesAtNode;
				taxaInTree[tree.taxonNumberOfNode(firstD)] = true;
				taxaInTree[tree.taxonNumberOfNode(lastD)] = true;
				countOfSpecies.increment();
				// commandRec.tick("Speciated at node " + eventNode + " ; total number of species " + countOfSpecies + "; total speciations: " + countSpeciations  + "; total extinctions: " + countExtinctions );
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
	public  void doSimulation(Taxa taxa, int replicateNumber, ObjectContainer treeContainer, ObjectContainer characterHistoryContainer, MesquiteLong seed, CommandRecord commandRec){
		//save random seed used to make tree under tree.seed for use in recovering later
		rng.setSeed(seed.getValue());
		MesquiteTree tree = null;
		CategoricalHistory charHistory = null;
        long localHistory[] = null;

		int numTaxa = taxa.getNumTaxa();
		if (numTaxa < maxSizeMultiple*expectedSize){
			taxa.addTaxa(numTaxa-1, maxSizeMultiple*expectedSize-numTaxa, true);  // avoid error at end?
		}
		numTaxa = taxa.getNumTaxa();
		if (numTaxa < maxSizeMultiple*expectedSize){
			MesquiteMessage.warnProgrammer("Taxa block failed to grow to 6*expected size; numTaxa = " + numTaxa);
		}

		Object t = treeContainer.getObject();
		if (t == null || !(t instanceof MesquiteTree))
			tree = new MesquiteTree(taxa);
		else
			tree = (MesquiteTree)t;

		Object c = characterHistoryContainer.getObject();
        if (c == null || !(c instanceof CategoricalHistory)){
            charHistory = (CategoricalHistory)charHistory.adjustSize(tree);
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

		double targetHeight = timeForBreadth(expectedSize);
		Debugg.println("xhat = " + stationaryFreq0());
		Debugg.println("target height is " + targetHeight);

		int attempts = 0;
		boolean done = false;
		boolean wentExtinct = false;
		boolean overFlow = false;
		int overFlowCount = 0;
		int patience = 100; //TODO: make this user settable
		boolean taxaInTree[] = new boolean[taxa.getNumTaxa()];
		for (int i=0; i< taxaInTree.length; i++)
			taxaInTree[i] = false;
		long state = 1L; //state 0
		Debugg.println("prior 1 at root " + prior1AtRoot);
		String hitsString = "";
		double minGen500 = 0;
		double maxGen500 = 0;
		while (attempts < patience && !done){
			countSpeciations = 0;
			countExtinctions = 0;
			countOfSpecies.setValue(2);
			wentExtinct = false;
			overFlow = false;
			state = 1L; //state 0
			if (rng.nextDouble()<prior1AtRoot)
				state = 2L; //state 1
			for (int i=0; i<tree.getNumNodeSpaces(); i++){  //state 0
                localHistory[i] = state;
			}

			tree.setToDefaultBush(2, false);
			taxaInTree[0] = true;
			taxaInTree[1] = true;
			tree.setAllBranchLengths(0, false);

			double generations = 0;
			//logln("sim using  logSpnForState0 " + logSpnForState0 + " logSpnForState1 " + logSpnForState1 + " rateChangePerIncrement " + rateChangePerIncrement);
			commandRec.tick("Attempt " + (attempts+1) + " to simulate tree ");
			numFlips =0;
			hitsString = "";
			while (generations<targetHeight && !wentExtinct && !overFlow){
				double globalRate = getGlobalRate(tree,tree.getRoot(),taxaInTree,localHistory);
				double eventTime = nextTime(globalRate);
				accumulator.setValue(0.0);
				double limit = rng.nextDouble();
				int eventNode = nextNode(tree,tree.getRoot(),taxaInTree,localHistory,globalRate,limit,accumulator);
				limit = rng.nextDouble();
				int eventChange = nextChange(localHistory, limit, eventNode);
				if (generations+eventTime > targetHeight){
					addLengthToAllTerminals(tree,tree.getRoot(),targetHeight-generations,taxaInTree);
					generations = targetHeight;
				}
				else {
					generations += eventTime;
					addLengthToAllTerminals(tree,tree.getRoot(),eventTime,taxaInTree);
					executeEvent(tree,eventNode,eventChange,taxaInTree,localHistory,countOfSpecies, commandRec);
					if (countOfSpecies.getValue() == 0 || tree.numberOfTerminalsInClade(tree.getRoot()) == 0){
						wentExtinct = true;             
						commandRec.tick("Extinction event (height of tree currently:  " + generations + ") [attempt: "+ (attempts+1) + "] ");
					}
					else if (eventChange == SPECIATIONEVENT){
						if (countOfSpecies.getValue() == 500){
							if (minGen500 == 0)
								minGen500 = generations;
							if (generations>maxGen500)
								maxGen500 = generations;
							hitsString = hitsString + "\t" + generations;
						}
						if (countOfSpecies.getValue()>taxaInTree.length-1){
							overFlow = true;
							overFlowCount++;
						}

						commandRec.tick("Speciation event (height of tree currently:  " + generations  + ") [attempt: "+ (attempts+1) + "] ");
					}
				}
			}
			if (!wentExtinct && !overFlow)
				done = true;
			attempts++;
		}
		if (!done){
			tree.setToDefaultBush(2, false);
			tree.setAllBranchLengths(0, false);
			charHistory.deassignStates();
			MesquiteMessage.warnUser("Attempt to simulate speciation/extinction failed because clade went entirely extinct " + patience + " times");
		}
		else  {
			hitsString = "\t" + minGen500 + "\t" + maxGen500 + hitsString;
			MesquiteMessage.println("Tree and character " + (replicateNumber +1) + " successfully evolved.");
			MesquiteMessage.println("A total of " + overFlowCount + " tree size overflows occurred; final size was: " + countOfSpecies.getValue());
			tree.setName("Sim. sp/ext with char. " + (replicateNumber +1) + " [#ext. " + countExtinctions + " #st.chg. " + numFlips + " root " + CategoricalState.toString(state) + "]");
		}
		//  MesquiteMessage.println("number of character changes: " + numFlips);
		treeContainer.setObject(tree);
		characterHistoryContainer.setObject(charHistory);

		seed.setValue(rng.nextLong());  //see for next time

	}


	/*.................................................................................................................*/
	public String getName() {
		return "Depth-limited Evolving Binary Speciation/Extinction Character estimating depth from desired tree size";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Generates tree by a speciation/extinction model in which a character controlling rates of speciation/extinction.  Uses negative exponential distribution and expected time to tree size to set depth." ;
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return "Rates: " + MesquiteDouble.toString(rateCharStateChange0, 4) + "," + MesquiteDouble.toString(rateCharStateChange1, 4) + "/" + MesquiteDouble.toString(spnForState0, 4) + "," +MesquiteDouble.toString(spnForState1, 4) + "/" +MesquiteDouble.toString(extForState0, 4) + "," +MesquiteDouble.toString(extForState1, 4);
	}


}

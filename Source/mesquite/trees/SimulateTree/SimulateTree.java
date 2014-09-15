/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.SimulateTree;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.coalesce.lib.Coalescer;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Supplies simulated trees.  This is a coordinator; the simulations themselves are done by modules hired.*/
public class SimulateTree extends TreeSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSimulate.class, getName() + "  uses a specific simulator to generate trees.",
		"The specific simulator can be chosen initially or in the Tree Simulator submenu");
		e.setEmphasize(true);
		e.setPriority(1);
	}
	int currentTree=0;

	long originalSeed=System.currentTimeMillis(); //0L;
	MesquiteTree tree;
	TreeSimulate simulatorTask;
	MesquiteLong seed;
	Random rng;
	MesquiteString simulatorName;
	int notifyEvery = 0;
	long count = 0;
	long[][] symmetries;
	Taxa currentTaxa = null;
	MesquiteCommand stC;
	MesquiteBoolean repeatableSequence = new MesquiteBoolean(true);

	public Color getItemColor(Taxa taxa, int ic){  // 26 Aug 07:  Why was this red and green???????
		/*  		if (ic % 2 == 0)
   			return Color.red;
   		else
   			return Color.green;
		 */
		return null;
	}
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (arguments ==null) {
			if (MesquiteThread.isScripting())
				simulatorTask= (TreeSimulate)hireNamedEmployee(TreeSimulate.class, StringUtil.tokenize("Equal rates Markov"));
			if (simulatorTask == null && MesquiteThread.isScripting())
				simulatorTask= (TreeSimulate)hireNamedEmployee(TreeSimulate.class, StringUtil.tokenize("Equiprobable Trees"));
			if (simulatorTask == null)
				simulatorTask= (TreeSimulate)hireEmployee(TreeSimulate.class, "Tree simulator");
			if (simulatorTask == null) {
				return sorry("Simulated Trees could not start because no simulator module was obtained");
			}
		}
		else  {
			simulatorTask= (TreeSimulate)hireNamedEmployee(TreeSimulate.class, arguments);
			if (simulatorTask == null) {
				return sorry("Simulated Trees could not start because the requested simulator module was not obtained");
			}
		}
		stC = makeCommand("setTreeSimulator",  this);
		simulatorTask.setHiringCommand(stC);
		if (RandomBetween.askSeed && !MesquiteThread.isScripting()){
			long response = MesquiteLong.queryLong(containerOfModule(), "Seed for Tree simulation", "Set Random Number seed for tree simulation:", originalSeed);
			if (MesquiteLong.isCombinable(response))
				originalSeed = response;
		}
		seed = new MesquiteLong(1);
		seed.setValue(originalSeed);
		rng = new Random(originalSeed);
		simulatorName = new MesquiteString();
		simulatorName.setValue(simulatorTask.getName());
		if (numModulesAvailable(TreeSimulate.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Simulator", stC, TreeSimulate.class);
			mss.setSelected(simulatorName);
		}
		addMenuItem("Set Seed (Tree simulation)...", makeCommand("setSeed",  this));
		addCheckMenuItem(null, "Simulation Repeatable", makeCommand("toggleRepeatable",  this), repeatableSequence);

		return true;
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
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return TreeSimulate.class;
	}


	public void endJob(){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		storePreferences();
		super.endJob();
	}
	int changes = 0;
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		int code = Notification.getCode(notification);
		if (obj == currentTaxa && !(code == MesquiteListener.SELECTION_CHANGED)) {

			changes++;
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void disposing(Object obj){
		if (obj == currentTaxa) {
			setHiringCommand(null); //since there is no rehiring
			iQuit();
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setTreeSimulator ", simulatorTask);
		temp.addLine("setSeed " + originalSeed);
		temp.addLine("toggleRepeatable " + repeatableSequence.toOffOnString());
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module simulating trees", "[name of module]", commandName, "setTreeSimulator")) {
			TreeSimulate temp=  (TreeSimulate)replaceEmployee(TreeSimulate.class, arguments, "Tree simulator", simulatorTask);
			if (temp!=null) {
				simulatorTask = temp;
				simulatorTask.setHiringCommand(stC);
				simulatorName.setValue(simulatorTask.getName());
				simulatorTask.initialize(currentTaxa);
				seed.setValue(originalSeed);
				if (tree!=null && tree instanceof MesquiteTree) {
					((MesquiteTree)tree).removeAllSensitives();
					((MesquiteTree)tree).deassignAssociated();
					tree = null;
				}
				if (!MesquiteThread.isScripting())
					parametersChanged(); //?
			}
			return temp;
		}
		else if (checker.compare(this.getClass(), "Toggles the sequence of trees is to be repeatable", null, commandName, "toggleRepeatable")) {
			repeatableSequence.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (!MesquiteThread.isScripting())
				parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Resets the random number seed to the current system time", null, commandName, "resetSeed")) {
			originalSeed = System.currentTimeMillis();
			if (!MesquiteThread.isScripting())
				parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Sets the random number seed to that passed", "[long integer seed]", commandName, "setSeed")) {
			long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
			if (!MesquiteLong.isCombinable(s)){
				s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for tree simulation", originalSeed);
			}
			if (MesquiteLong.isCombinable(s)){
				originalSeed = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); //?
			}
		}
		else if (checker.compare(this.getClass(), "Notifies the user periodically how many trees have been simulated", "[how many trees between notifications]", commandName, "notifyEvery")) {
			int notify = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(notify) && notify>0)
				notifyEvery = notify;
			else
				notifyEvery = -1;
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/

	public void setPreferredTaxa(Taxa taxa){
		if (taxa !=currentTaxa) {
			if (currentTaxa!=null)
				currentTaxa.removeListener(this);
			currentTaxa = taxa;
			currentTaxa.addListener(this);
		}

	}
	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		setPreferredTaxa(taxa);
		if (simulatorTask!=null)
			simulatorTask.initialize(taxa);
	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int itree) {  //TODO: should this reuse the same Tree object? forces user to use immediately or clone for storages
		setPreferredTaxa(taxa);
		currentTree=itree;
		if (taxa==null) {
			MesquiteMessage.warnProgrammer("taxa null in getTree of SimulateTree");
			return null;
		}
		if (notifyEvery>0 && count++ % notifyEvery==0)
			System.out.println("   tree " + currentTree);
		tree = new MesquiteTree(taxa);
		if (repeatableSequence.getValue()){
			rng.setSeed(originalSeed);
			long rnd = originalSeed;  //v. 1. 12 this had been 0 and thus first would always use seed 1
			for (int it = 0; it<= currentTree; it++) 
				rnd =  rng.nextInt();
			rng.setSeed(rnd+1);
			seed.setValue(rnd + 1); //v. 1. 1, October 05: changed so as to avoid two adjacent trees differing merely by a frameshift of random numbers
		}
		else
			seed.setValue(rng.nextInt()); 

		tree =  (MesquiteTree)simulatorTask.getSimulatedTree(taxa, tree, currentTree, null, seed);
		if (tree ==null)
			return null;
		((MesquiteTree)tree).setName(getTreeNameString(taxa, currentTree));
		return tree;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		if (simulatorTask!=null)
			return simulatorTask.getNumberOfTrees(taxa);
		return MesquiteInteger.infinite;
	}

	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		if (simulatorTask==null) return "";
		return "Tree # " + MesquiteTree.toExternal(itree)  + " simulated by " + simulatorTask.getName();
	}
	/*.................................................................................................................*/
	public String getCurrentTreeNameString() {
		if (simulatorTask==null) return "";
		return "Tree # " + MesquiteTree.toExternal(currentTree)  + " simulated by " + simulatorTask.getName();
	}
	/*.................................................................................................................*/
	public String getParameters() {
		if (simulatorTask==null) return "";
		return "Tree simulator: " + simulatorTask.getName() + ". [seed: " + originalSeed + "]";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Simulated Trees";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies trees from simulations.";
	}
}


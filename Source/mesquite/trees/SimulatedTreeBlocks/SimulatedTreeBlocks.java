/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.SimulatedTreeBlocks;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SimulatedTreeBlocks extends TreeBlockSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSimulate.class, getName() + "  uses a specific simulator to generate trees.",
		"The specific simulator can be chosen initially or in the Tree Simulator submenu");
		e.setPriority(1);
	}
	/*.................................................................................................................*/
	int currentTreeBlockIndex=0;
	TreeVector currentTreeBlock = null;
	TreeVector lastUsedTreeBlock = null;
	TreeSimulate simulatorTask;
	Taxa preferredTaxa =null;
	Taxa currentTaxa = null;
	MesquiteLong seed;
	long originalSeed=System.currentTimeMillis(); //0L;
	static int numTrees = 10;
	Random rng;
	MesquiteInteger pos = new MesquiteInteger(0);
	MesquiteString simulatorName;
	MesquiteCommand stC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
	    	currentTreeBlockIndex = 0;
		simulatorTask = (TreeSimulate)hireEmployee(TreeSimulate.class, "Tree simulator");
		if (simulatorTask==null)
			return sorry(getName() + " couldn't start because no tree simulator module was obtained.");
    	 	stC = makeCommand("setTreeSimulator",  this);
    	 	simulatorTask.setHiringCommand(stC);
    	 	seed = new MesquiteLong(1);
    	 	seed.setValue(originalSeed);
    	 	rng = new Random(originalSeed);
 		simulatorName = new MesquiteString();
		if (numModulesAvailable(TreeSimulate.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Simulator", stC, TreeSimulate.class);
 			mss.setSelected(simulatorName);
  		}
		addMenuItem( "Number of Trees...", makeCommand("setNumberTrees",  this));
		if (!MesquiteThread.isScripting()){
			int n = MesquiteInteger.queryInteger(containerOfModule(), "Trees per block?", "How many trees to simulate per tree block?", numTrees);
			if (!MesquiteInteger.isCombinable(n) || n<=0)
				return false;
			numTrees = n;
		}
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
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
	public void endJob(){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		int code = Notification.getCode(notification);
		if (obj == currentTaxa && !(code == MesquiteListener.SELECTION_CHANGED)) {
				parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTreeSimulator ", simulatorTask);
   	 	temp.addLine("setNumberTrees " + numTrees);
 	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the number of trees simulated in each tree block", "[number of trees]", commandName, "setNumberTrees")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set Number of Trees", "Number of Trees to simulate:", numTrees, 0, MesquiteInteger.infinite);
    	 		if (newNum>0  && newNum!=numTrees) {
    	 			numTrees = newNum;
				parametersChanged();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the module simulating trees", "[name of module]", commandName, "setTreeSimulator")) {
    	 		TreeSimulate temp=  (TreeSimulate)replaceEmployee(TreeSimulate.class, arguments, "Tree simulator", simulatorTask);
	    	 	if (temp!=null) {
	    	 		simulatorTask = temp;
		    	 	simulatorTask.setHiringCommand(stC);
	    	 		simulatorName.setValue(simulatorTask.getName());
	    	 		simulatorTask.initialize(currentTaxa);
	    	 		seed.setValue(originalSeed);
 				parametersChanged(); //?
 			}
 			return temp;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void disposing(Object obj){
		if (obj == currentTaxa) {
			setHiringCommand(null); //since there is no rehiring
			iQuit();
		}
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
	/*.................................................................................................................
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		//TODO: should this respond to nothing???
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
   	private Tree getTree(Taxa taxa, int whichTree, long baseSeed) {
		rng.setSeed(baseSeed);
		long rnd = baseSeed;  //v. 1. 12 this had been 0 and thus first would always use seed 1
		for (int it = 0; it<=whichTree; it++)
			rnd =  rng.nextInt();
		rng.setSeed(rnd+1);
		seed.setValue(rnd + 1);  //v. 1. 1, October 05: changed so as to avoid two adjacent trees differing merely by a frameshift of random numbers
		Tree tree =  simulatorTask.getSimulatedTree(taxa, null, whichTree, null, seed); //TODO: this should be passed scripting
		if (tree instanceof AdjustableTree)
			((AdjustableTree)tree).setName("Simulated tree " + MesquiteTree.toExternal(whichTree));
   		return tree;
   	}
	/*.................................................................................................................*/
   	public TreeVector getFirstBlock(Taxa taxa) {
   		setPreferredTaxa(taxa);
   		currentTreeBlockIndex=0;
   		return getCurrentBlock(taxa);
   	}
	/*.................................................................................................................*/
   	public TreeVector getBlock(Taxa taxa, int ic) {
   		setPreferredTaxa(taxa);
   		currentTreeBlockIndex=ic;
   		return getCurrentBlock(taxa);
   	}
	/*.................................................................................................................*/
   	public TreeVector getCurrentBlock(Taxa taxa) {
   		setPreferredTaxa(taxa);
   		if (taxa == null)
   			return null;
   		int min = taxa.getNumTaxa();
   		TreeVector trees = new TreeVector(taxa);
   		trees.setName("Simulated " + currentTreeBlockIndex);
		for (int i = 0; i<numTrees; i++) {
			trees.addElement(getTree(taxa, i, originalSeed + currentTreeBlockIndex), false);
   		}
   		return trees;
   	}
	/*.................................................................................................................*/
   	public TreeVector getNextBlock(Taxa taxa) {
   		setPreferredTaxa(taxa);
   		currentTreeBlockIndex++;
   		return getCurrentBlock(taxa);
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTreeBlocks(Taxa taxa) {
   		setPreferredTaxa(taxa);
   		return MesquiteInteger.infinite;
   	}
   
	/*.................................................................................................................*/
   	public String getTreeBlockNameString(Taxa taxa, int index) {
   		setPreferredTaxa(taxa);
		return "Simulated tree block " + index;
   	}
	/*.................................................................................................................*/
   	public String getCurrentTreeNameString(Taxa taxa) {
   		setPreferredTaxa(taxa);
		return "Simulated tree block " + currentTreeBlock;
   	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "Simulated Tree Blocks";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Creates a tree block of simulated trees." ;
   	 }
	/*.................................................................................................................*/
   	public String getParameters() {
		if (currentTaxa!=null)
			return "Simulated trees Lists (last set of taxa used: " + currentTaxa.getName() + ")";
		else
			return "Simulated trees Lists";
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
}


/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.RandomlyModifiedTrees;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.RandomBetween;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.TreeSource;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.treefarm.lib.RndTreeModifier;

/* ======================================================================== */
public class RandomlyModifiedTrees extends TreeSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(RndTreeModifier.class, getName() + "  needs a method to randomly modify trees.",
		"The method to randomly modify trees can be selected initially or in the Random Modifier of Tree submenu");
		EmployeeNeed e2 = registerEmployeeNeed(TreeSource.class, getName() + "  needs a source of trees to modify randomly.",
		"The source of trees can be selected initially");
		//e2.setDivertChainMessage("testing");
	}
	/*.................................................................................................................*/
	TreeSource treeSourceTask;
	RandomBetween rng = new RandomBetween();
	RndTreeModifier modifierTask;
	MesquiteString modifierName;
	MesquiteCommand stC;
	int currentTree;
	MesquiteLong seed;
	long originalSeed=System.currentTimeMillis(); //0L;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		treeSourceTask = (TreeSource)hireEmployee(TreeSource.class, "Source of trees to be modified");
 		if (treeSourceTask == null) {
 			return sorry(getName() + " couldn't start because no source of a current tree to serve as a basis for modification was obtained.");
 		}
	 	modifierTask = (RndTreeModifier)hireNamedEmployee(RndTreeModifier.class, arguments);
		if (modifierTask == null)
			modifierTask = (RndTreeModifier)hireEmployee(RndTreeModifier.class, "Random modifier of tree");
 		if (modifierTask == null) {
 			return sorry(getName() + " couldn't start because no random tree modifier was obtained.");
 		}
 	 	stC = makeCommand("setModifier",  this);
 	 	modifierTask.setHiringCommand(stC);
 		modifierName = new MesquiteString();
	   	 modifierName.setValue(modifierTask.getName());
		if (numModulesAvailable(RndTreeModifier.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Random Modifier of Tree", stC, RndTreeModifier.class);
 			mss.setSelected(modifierName);
  		}
 	 	seed = new MesquiteLong(1);
 	 	seed.setValue(originalSeed);
  		addMenuItem("Set Seed (Random tree modification)...", makeCommand("setSeed",  this));
  		return true;
  	 }
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTreeSource ", treeSourceTask); 
  	 	temp.addLine("setSeed " + originalSeed);
  	 	temp.addLine("setModifier ", modifierTask); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the source of the trees to be modified", "[name of module]", commandName, "setTreeSource")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees to be modified", treeSourceTask);
			if (temp !=null){
				treeSourceTask = temp;
				if (!MesquiteThread.isScripting())
					parametersChanged();
    	 			return treeSourceTask;
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the tree modifier", "[name of module]", commandName, "setModifier")) {
			RndTreeModifier temp = (RndTreeModifier)replaceEmployee(RndTreeModifier.class, arguments, "Random modifier of tree", modifierTask);
			if (temp !=null){
				modifierTask = temp;
    	 			modifierName.setValue(modifierTask.getName());
		 	 	modifierTask.setHiringCommand(stC);
				parametersChanged();
    	 			return modifierTask;
    	 		}
    	 	}
     	 	 else if (checker.compare(this.getClass(), "Sets the random number seed", "[long integer seed]", commandName, "setSeed")) {
    	 		long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
    	 		if (!MesquiteLong.isCombinable(s)){
    	 			s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for random modification of tree", originalSeed);
    	 		}
    	 		if (MesquiteLong.isCombinable(s)){
    	 			originalSeed = s;
 				parametersChanged(); //?
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
	/*.................................................................................................................*/
  	public void setPreferredTaxa(Taxa taxa){
  	}
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		if (treeSourceTask!=null)
   			treeSourceTask.initialize(taxa);
   	}
	/*.................................................................................................................*/
	protected Tree getBasisTree(Taxa taxa, int i){
   		Tree t =  treeSourceTask.getTree(taxa, i);
 		return t;
	}
	/*.................................................................................................................*/
   	public Tree getTree(Taxa taxa, int ic) {  
   		MesquiteTree modified = null;
   		int code = 0;
   		try {
   			code = 1;
	   		Tree tree = getBasisTree(taxa, ic);
   			code = 2;
	   		currentTree = ic;
   			code = 3;
	   		if (tree == null)
	   			return null;
	   		code = 4;
	   		rng.setSeed(originalSeed);
	   		code = 5;
	   		long rnd = originalSeed;  //v. 1. 12 this had been 0 and thus first would always use seed 1
	   		code = 6;

	   		for (int it = 0; it<=ic; it++) 
	   			rnd =  rng.nextInt();
	   		code = 7;
	   		rng.setSeed(rnd+1);
	   		seed.setValue(rnd + 1); //v. 1. 1, October 05: changed so as to avoid two adjacent trees differing merely by a frameshift of random numbers
	   		code = 8;
	   		modified =  tree.cloneTree();
	   		code = 9;
	   		modifierTask.modifyTree(tree, modified, rng);
   			code = 10;
	   		modified.setName("Randomly modified from " + tree.getName() + " (#" + currentTree + ")");
	   		MesquiteDouble md = new MesquiteDouble(0.2);
	   		md.setName("test");
	   		modified.attachIfUniqueName(md);
	   		
   		}
   		catch (Error e){
   			MesquiteMessage.warnProgrammer("Error " + e + " ===== " + code);
   		}
   		return modified;
   	}
	/*.................................................................................................................*/
 	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
 		if (employee != treeSourceTask || Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED)
 			super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		return  treeSourceTask.getNumberOfTrees(taxa);
   	}
   
	/*.................................................................................................................*/
   	public String getTreeNameString(Taxa taxa, int itree) {
   		return "Random modification of tree #" +  (itree+1);
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		if (treeSourceTask == null || modifierTask == null)
   			return "Randomly modifying trees";
   		return"Randomly modifying trees from: " + treeSourceTask.getParameters() + ". Modifications: " + modifierTask.getParameters() + ". (seed: " + originalSeed + ")";
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Randomly Modify Trees";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Modifies each of a series of trees by random changes; the i'th tree from this module comes by modifying the i'th tree from the original source of trees.";
   	 }
}


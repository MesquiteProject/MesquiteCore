/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.RandomModifTree;
/*~~  */


import mesquite.lib.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class RandomModifTree extends SourceModifiedTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(RndTreeModifier.class, getName() + "  needs a method to randomly modify trees.",
		"The method to randomly modify trees can be selected initially or in the Random Modifier of Tree submenu");
	}
	int currentTree=0;

	MesquiteLong seed;
	long originalSeed=System.currentTimeMillis(); //0L;
	RandomBetween rng = new RandomBetween();
	RndTreeModifier modifierTask;
	MesquiteString modifierName;
	MesquiteCommand stC;
	  MesquiteTimer timer = new MesquiteTimer();
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		   timer.start();
		if (!super.startJob(arguments, condition, hiredByName))
 			return false;
 		if (arguments ==null)
 			modifierTask = (RndTreeModifier)hireEmployee(RndTreeModifier.class, "Random modifier of tree");
	 	else {
	 		modifierTask = (RndTreeModifier)hireNamedEmployee(RndTreeModifier.class, arguments);
 			if (modifierTask == null)
 				modifierTask = (RndTreeModifier)hireEmployee(RndTreeModifier.class, "Random modifier of tree");
 		}
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
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return RndTreeModifier.class;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setSeed " + originalSeed);
  	 	temp.addLine("setModifier ", modifierTask); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the tree modifier", "[name of module]", commandName, "setModifier")) {
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
   	public Tree getTree(Taxa taxa, int ic) {  
   		MesquiteTree modified = null;
   		int code = 0;
   		try {
   			code = 1;
	   		Tree tree = getBasisTree(taxa);
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
	   		for (int it = 0; it<=currentTree; it++) 
	   			rnd =  rng.nextInt();
	   		rng.setSeed(rnd+1);
	   		seed.setValue(rnd + 1);  //v. 1. 1, October 05: changed so as to avoid two adjacent trees differing merely by a frameshift of random numbers
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
   			System.out.println("Error " + e + " ===== " + code);
   		}
   		return modified;
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		Tree tree = getBasisTree(taxa);
		if (tree ==null)
			return 0;
		else
			return MesquiteInteger.infinite;
   	}
   
	/*.................................................................................................................*/
   	public String getTreeNameString(Taxa taxa, int itree) {
   		return "Random modification #" + (itree+1);
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		return"Randomly modifying tree from: " + getBasisTreeSource().getParameters() + ". [seed: " + originalSeed + "]";
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Randomly Modify Current Tree";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Modifies current tree by random changes.";
   	 }
   	 
}




/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.ProbRndTreeModifier;

import java.util.Random;

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.RandomBetween;
import mesquite.lib.Snapshot;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.treefarm.lib.RndTreeModifier;

/** This module is basically a random tree modifier, but it works by hiring another one, and only invoking it on a tree with a particular probability */
public class ProbRndTreeModifier extends RndTreeModifier {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(RndTreeModifier.class, getName() + "  needs a method to modify trees.",
		"The method to modify the tree can be selected initially or in the Random Modifier of Tree submenu");
	}
	Random probModifyRNG = new RandomBetween(System.currentTimeMillis()); ;
	RndTreeModifier modifierTask;
	MesquiteString modifierName;
	MesquiteCommand stC;
	double defaultProbability = 0.5;
	MesquiteDouble prob = new MesquiteDouble(defaultProbability);
	
	public boolean startJob(String arguments, Object condition,boolean hiredByName) {
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
			MesquiteSubmenuSpec mss = addSubmenu(null, "Random Modifier of Tree (used by Occasionally Modify)", stC, RndTreeModifier.class);
			mss.setSelected(modifierName);
		}
  		addMenuItem("Set Probability of Random Tree Modification...", makeCommand("setProbability",  this));
  		if (!MesquiteThread.isScripting()) {
			double s = MesquiteDouble.queryDouble(containerOfModule(), "Random number seed", "Enter a value for the probability a tree will be randomly modified", prob.getValue());
	 		 if (MesquiteDouble.isCombinable(s)){
	 			 prob.setValue(s);
	 		 }
  		}
		return true;
	}

	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return RndTreeModifier.class;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setProbability " + prob.getValue());
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
  	 	 else if (checker.compare(this.getClass(), "Sets the probability of modification", "[probability (a double)]", commandName, "setProbability")) {
	 		 double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
	 		 if (!MesquiteDouble.isCombinable(s)){
	 			 s = MesquiteDouble.queryDouble(containerOfModule(), "Random number seed", "Enter a value for the probability a tree will be randomly modified", prob.getValue());
	 		 }
	 		 if (MesquiteDouble.isCombinable(s)){
	 			 prob.setValue(s);
	 			 parametersChanged(); //?
	 		 }
	 	 }
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	int count=0;
	
	public void modifyTree(Tree tree, MesquiteTree modified, RandomBetween rng) {
		if (modifierTask==null || tree==null)
			return;
		probModifyRNG.setSeed(rng.nextInt());
		if (probModifyRNG.nextDouble()<=prob.getValue())
			modifierTask.modifyTree(tree,modified, rng);
	}

	public boolean isPrerelease(){
		return false;
	}
	
	public String getName() {
		return "Occasionally Randomly Modify";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

	public String getExplanation() {
		return "With specified probabililty, will ask random tree modifier to modify current tree.";
	}

}

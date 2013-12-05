/* Mesquite source code, Treefarm package.  Copyright 1997-2010 W. Maddison, D. Maddison and P. Midford. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.DetermModifTree;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class DetermModifTree extends SourceModifiedTree {
  	 public String getName() {
 		return "Modify Current Tree";
    	 }
    	 public String getExplanation() {
 		return "Modifies current tree.";
    	 }
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DetTreeModifier.class, getName() + "  needs a method to modify the tree.",
		"The method to modify the tree can be selected initially, or later under the Modifier of Tree submenu");
	}
	int currentTree=0;
	DetTreeModifier modifierTask;
	MesquiteString modifierName;
	MesquiteCommand stC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		if (!super.startJob(arguments, condition, hiredByName))
 			return false;
 		if (arguments ==null)
 			modifierTask = (DetTreeModifier)hireEmployee(DetTreeModifier.class, "Modifier of tree");
	 	else {
	 		modifierTask = (DetTreeModifier)hireNamedEmployee(DetTreeModifier.class, arguments);
 			if (modifierTask == null)
 				modifierTask = (DetTreeModifier)hireEmployee(DetTreeModifier.class, "Modifier of tree");
 		}
 		if (modifierTask == null) {
 			return sorry(getName() + " couldn't start because no tree modifier was obtained.");
 		}
 	 	stC = makeCommand("setModifier",  this);
 	 	modifierTask.setHiringCommand(stC);
 		modifierName = new MesquiteString();
	    	modifierName.setValue(modifierTask.getName());
		if (numModulesAvailable(DetTreeModifier.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Modifier of Tree", stC, DetTreeModifier.class);
 			mss.setSelected(modifierName);
  		}
  		return true;
  	 }
	/*.................................................................................................................*/
  	public void setPreferredTaxa(Taxa taxa){
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
	public  Class getHireSubchoice(){
		return DetTreeModifier.class;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setModifier ", modifierTask); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the tree modifier", "[name of module]", commandName, "setModifier")) {
				DetTreeModifier temp = (DetTreeModifier)replaceEmployee(DetTreeModifier.class, arguments, "Modifier of tree", modifierTask);
				if (temp !=null){
					modifierTask = temp;
	    	 		modifierName.setValue(modifierTask.getName());
			 	 	modifierTask.setHiringCommand(stC);
					parametersChanged();
	    	 			return modifierTask;
	    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
	/*.................................................................................................................*/
   	public Tree getTree(Taxa taxa, int ic) {  
   		Tree tree = getBasisTree(taxa);
   		currentTree = ic;
   		if (tree == null)
   			return null;
   		MesquiteTree modified =  tree.cloneTree();
   		modifierTask.modifyTree(tree, modified, currentTree);
   		modified.setName("Modified from " + tree.getName() + " (#" + (currentTree + 1) + ")");
   		return modified;
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		Tree tree = getBasisTree(taxa);
		if (tree ==null)
			return 0;
		else
			return modifierTask.getNumberOfTrees(tree);
   	}
	/*.................................................................................................................*/
   	public String getTreeNameString(Taxa taxa, int itree) {
   		return "Modification #" + (itree +1);
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		return"Modifying trees from: " + getBasisTreeSource().getParameters();
   	}
	/*.................................................................................................................*/
    	 
}


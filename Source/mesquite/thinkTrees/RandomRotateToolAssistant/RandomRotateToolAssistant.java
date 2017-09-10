/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.thinkTrees.RandomRotateToolAssistant;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.coalesce.InsertNode.InsertNode;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class RandomRotateToolAssistant extends TreeDisplayAssistantI {
	public Vector extras;
	 public String getFunctionIconPath(){
   		 return getPath() + "randomRotate.gif";
   	 }
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		RandomRotateToolExtra newPj = new RandomRotateToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Randomly Rotate Branches";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies a tool for tree windows that randomly rotates branches in clade touched." ;
   	 }
	public boolean isSubstantive(){
		return false;
	}   	 
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 330;  
	}

}

/* ======================================================================== */
class RandomRotateToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool randomRotateTool;
	RandomRotateToolAssistant randomRotateModule;
	MesquiteCommand randomRotateCommand;
	AdjustableTree tree = null;
	Random rng = new Random(System.currentTimeMillis());
	
	public RandomRotateToolExtra (RandomRotateToolAssistant ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		randomRotateModule = ownerModule;
		randomRotateCommand = MesquiteModule.makeCommand("rotateBranches",  this);
		randomRotateTool = new TreeTool(this, "randomlyRotate", ownerModule.getPath(),"randomRotate.gif", 7,3,"Randomly rotate branches", "This tool is used to randomly rotate all nodes within the clade touched.");
		randomRotateTool.setTouchedCommand(randomRotateCommand);
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(randomRotateTool);
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (tree instanceof AdjustableTree)
			this.tree = (AdjustableTree)tree;
		else this.tree =null;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Randomly rotates nodes in the clade", "[branch number]", commandName, "rotateBranches")) {
			pos.setValue(0);

			int branchFound= MesquiteInteger.fromString(arguments,pos);
			if (tree!=null && tree.randomlyRotateDescendants(branchFound, rng, true))
				((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));

		}
		return null;
	}
	public void turnOff() {
		randomRotateModule.extras.removeElement(this);
		super.turnOff();
	}
}



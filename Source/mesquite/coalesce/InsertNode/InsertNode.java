/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.InsertNode;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class InsertNode extends TreeDisplayAssistantI {
	public Vector extras;
	 public String getFunctionIconPath(){
   		 return getPath() + "insertNode.gif";
   	 }
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		InsertToolExtra newPj = new InsertToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public String getName() {
		return "Insert Node";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Provides a tool with which to insert nodes along a branch.";
   	 }
}

/* ======================================================================== */
class InsertToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool insertTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	InsertNode insertModule;
	AdjustableTree tree;
	
	public InsertToolExtra (InsertNode ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		insertModule = ownerModule;
		insertTool = new TreeTool(this, "InsertNode", ownerModule.getPath(), "insertNode.gif", 7,7,"Insert Node", "This tool is used to insert a node along a branch.  The branch collapse (polytomy) tool can be used to remove nodes.");
		insertTool.setTouchedCommand(MesquiteModule.makeCommand("insertNode",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(insertTool);
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (tree instanceof AdjustableTree)
			this.tree = (AdjustableTree)tree;
		else
			this.tree = null;
	}
	
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (tree instanceof AdjustableTree)
			this.tree = (AdjustableTree)tree;
		else
			this.tree = null;
	}

	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		
    	 	if (checker.compare(this.getClass(), "Inserts an unbranched node along a branch", "[node below which to insert]", commandName, "insertNode")) {
  			if (tree == null)
  				return null;
  			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
  			if (branchFound >0 && MesquiteInteger.isCombinable(branchFound)) {
  				double bL = tree.getBranchLength(branchFound);
  					
				 int n = tree.insertNode(branchFound, false);
				 if (n<=0)
				 	ownerModule.alert("Sorry, the node couldn't be inserted, possibly because there are already too many nodes");
				 else {
	  				if (MesquiteDouble.isCombinable(bL)) {
					 	tree.setBranchLength(branchFound, bL/2.0, false);
					 	tree.setBranchLength(n, bL/2.0, false);
				 	 }
				 	if (tree instanceof Listenable)
				 		((Listenable)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
				 	 treeDisplay.pleaseUpdate(true);
				 }
   			}
 	 	}
 		return null;
 	}
	public void turnOff() {
		insertModule.extras.removeElement(this);
		super.turnOff();
	}
}

	



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.opentree.OpenTreeLabeller;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class OpenTreeLabeller extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "openTree.gif";
	}
	/*.................................................................................................................*/
	public boolean loadModule(){
		return false;
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
		OpenTreeToolExtra newPj = new OpenTreeToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public String getName() {
		return "Open Tree Labeller";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to label nodes and tips of the tree for OpenTree.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}

/* ======================================================================== */
class OpenTreeToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool openTreeTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	OpenTreeLabeller insertModule;
	AdjustableTree tree;

	public OpenTreeToolExtra (OpenTreeLabeller ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		insertModule = ownerModule;
		openTreeTool = new TreeTool(this, "OpenTreeLabeller", ownerModule.getPath(), "openTree.gif", 7,1,"Open Tree Labeller", "This tool is used to insert a node along a branch.  The branch collapse (polytomy) tool can be used to remove nodes.");
		openTreeTool.setTouchedCommand(MesquiteModule.makeCommand("labelBranch",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(openTreeTool);
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

		if (checker.compare(this.getClass(), "Labels a node for Open Tree", "[node to label]", commandName, "labelBranch")) {
			if (tree == null)
				return null;
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (branchFound >0 && MesquiteInteger.isCombinable(branchFound)) {
				double bL = tree.getBranchLength(branchFound);
//				if (tree instanceof Listenable)
//					((Listenable)tree).notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_ADDED));
				treeDisplay.pleaseUpdate(true);

			}
		}
		return null;
	}
	public void turnOff() {
		insertModule.extras.removeElement(this);
		super.turnOff();
	}
	
	
}





/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodeAssociatesListStarter;

import java.util.*;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lists.lib.ListModule;
import mesquite.trees.NodeAssociatesList.NodeAssociatesList;
import mesquite.trees.NodeAssociatesZDisplayControl.NodeAssociatesZDisplayControl;

/* ======================================================================== */
public class NodeAssociatesListStarter extends TreeDisplayAssistantI  {
	NodeAssociatesList nodeAssocListModule = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeAssocListModule= (NodeAssociatesList)hireNamedEmployee(ListModule.class, "#NodeAssociatesList");
		if (nodeAssocListModule == null)
			return sorry(getName() + " couldn't start because no list module found");
		return true;
	}

	public void employeeQuit(MesquiteModule m){
			iQuit();
	}
	
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		nodeAssocListModule.setTree((MesquiteTree)tree);
	}

	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		NodeAssociatesExtra extra = new NodeAssociatesExtra(this, treeDisplay, nodeAssocListModule);
		return extra;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node/Branch Properties List Starter";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Starts list module of node associates." ;
	}

}

class NodeAssociatesExtra extends TreeDisplayExtra {
	NodeAssociatesList nodeAssocListModule;
	public NodeAssociatesExtra(NodeAssociatesListStarter module, TreeDisplay treeDisplay, NodeAssociatesList nodeAssocListModule){
		super(module, treeDisplay);
		this.nodeAssocListModule = nodeAssocListModule;
	}
	public void setTree(Tree tree) {
		if (tree instanceof MesquiteTree)
			nodeAssocListModule.setTree((MesquiteTree)tree);
	}
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	
	/**to inform TreeDisplayExtra that cursor has just entered branch N*/
	public void cursorEnterBranch(Tree tree, int N, Graphics g){
		nodeAssocListModule.cursorEnterBranch((MesquiteTree)tree, N);

	}
	/**to inform TreeDisplayExtra that cursor has just exited branch N*/
	public void cursorExitBranch(Tree tree, int N, Graphics g){
		nodeAssocListModule.cursorExitBranch((MesquiteTree)tree, N);

	}
	/**to inform TreeDisplayExtra that cursor has just touched branch N*/
	public void cursorTouchBranch(Tree tree, int N, Graphics g){
		nodeAssocListModule.cursorTouchBranch((MesquiteTree)tree, N);
	}
	/**to inform TreeDisplayExtra that cursor has just moved OUTSIDE of taxa or branches*/
	public void cursorMove(Tree tree, int x, int y, Graphics g){
		nodeAssocListModule.cursorMove((MesquiteTree)tree);
	
	}

}

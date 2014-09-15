/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.SelectBranches;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SelectBranches extends TreeDisplayAssistantI {
	public Vector extras;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		MesquiteSubmenuSpec mss = addSubmenu(null, "Select Branches");
		addItemToSubmenu(null, mss, "Select All Branches", makeCommand("selectAll",  this));
		addItemToSubmenu(null, mss, "Invert Selection", makeCommand("invertSelection",  this));
		addItemToSubmenu(null, mss, "Zero-length Branches", makeCommand("selectZero",  this));
		addItemToSubmenu(null, mss, "Negative-length Branches", makeCommand("selectNeg",  this));
		addItemToSubmenu(null, mss, "Branches of Unassigned Length", makeCommand("selectUnassignedLength",  this));
		addItemToSubmenu(null, mss, "Internal Branches", makeCommand("selectInternal",  this));
		addItemToSubmenu(null, mss, "Terminal Branches", makeCommand("selectTerminal",  this));
		addItemToSubmenu(null, mss, "Polytomous Nodes", makeCommand("selectPolytomies",  this));
		addItemToSubmenu(null, mss, "Unbranched Internals", makeCommand("selectUnbranched",  this));
		addItemToSubmenu(null, mss, "Root", makeCommand("selectRoot",  this));
		return true;
	} 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	private void invertSelection(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			invertSelection(tree, daughter);
		}
		tree.setSelected(node, !tree.getSelected(node));
	}
	/*.................................................................................................................*/
	private void selectZero(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			selectZero(tree, daughter);
		}
		if (tree.getBranchLength(node) == 0.0)
			tree.setSelected(node, true);
		else
			tree.setSelected(node, false);
	}
	/*.................................................................................................................*/
	private void selectNeg(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			selectNeg(tree, daughter);
		}
		if (tree.getBranchLength(node) < 0.0)
			tree.setSelected(node, true);
		else
			tree.setSelected(node, false);
	}
	/*.................................................................................................................*/
	private void selectUnassignedLength(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			selectUnassignedLength(tree, daughter);
		}
		if (tree.branchLengthUnassigned(node))
			tree.setSelected(node, true);
		else
			tree.setSelected(node, false);
	}
	/*.................................................................................................................*/
	private void selectInternal(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			selectInternal(tree, daughter);
		}
		if (tree.nodeIsInternal(node))
			tree.setSelected(node, true);
		else
			tree.setSelected(node, false);
	}
	/*.................................................................................................................*/
	private void selectTerminal(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			selectTerminal(tree, daughter);
		}
		if (tree.nodeIsTerminal(node))
			tree.setSelected(node, true);
		else
			tree.setSelected(node, false);
	}
	/*.................................................................................................................*/
	private void selectPolytomies(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			selectPolytomies(tree, daughter);
		}
		if (tree.nodeIsPolytomous(node))
			tree.setSelected(node, true);
		else
			tree.setSelected(node, false);
	}
	/*.................................................................................................................*/
	private void selectUnbranched(MesquiteTree tree, int node){
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			selectUnbranched(tree, daughter);
		}
		if (tree.nodeIsUnbranchedInternal(node))
			tree.setSelected(node, true);
		else
			tree.setSelected(node, false);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Inverts selection of branches", null, commandName, "invertSelection")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					invertSelection((MesquiteTree)tree, tree.getRoot());
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects all branches", null, commandName, "selectAll")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					((MesquiteTree)tree).deselectAll();
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects zero-length branches", null, commandName, "selectZero")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					selectZero((MesquiteTree)tree, tree.getRoot());
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects negative-length branches", null, commandName, "selectNeg")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					selectNeg((MesquiteTree)tree, tree.getRoot());
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects branches of unassigned length", null, commandName, "selectUnassignedLength")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					selectUnassignedLength((MesquiteTree)tree, tree.getRoot());
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects internal branches", null, commandName, "selectInternal")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					selectInternal((MesquiteTree)tree, tree.getRoot());
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects terminal branches", null, commandName, "selectTerminal")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					selectTerminal((MesquiteTree)tree, tree.getRoot());
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects polytomous nodes", null, commandName, "selectPolytomies")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					selectPolytomies((MesquiteTree)tree, tree.getRoot());
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects unbranched internal nodes", null, commandName, "selectUnbranched")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					selectUnbranched((MesquiteTree)tree, tree.getRoot());
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects root", null, commandName, "selectRoot")) {
			for (int i =0; i<extras.size(); i++){
				TreeDisplayExtra e = (TreeDisplayExtra)extras.elementAt(i);
				Tree tree = e.getTreeDisplay().getTree();
				if (tree instanceof MesquiteTree){
					((MesquiteTree)tree).deselectAll();
					((MesquiteTree)tree).setSelected(tree.getRoot(), true);
					((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		SelectToolExtra newPj = new SelectToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
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
	public String getName() {
		return "Select Branches";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool and menu with which to select branches in a tree window.";
	}
	public void getSubfunctions(){
		registerSubfunction(new FunctionExplanation("Select Branch", "(A tool of the Tree Window) Selects the touched branch", null, getPath() + "select.gif"));
		registerSubfunction(new FunctionExplanation("Select Clade", "(A tool of the Tree Window) Selects the touched clade", null, getPath() + "selectClade.gif"));
		super.getSubfunctions();
	}
}

/* ======================================================================== */
class SelectToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool selectCladeTool;//selectTool, 
	MesquiteMenuItemSpec hideMenuItem = null;
	SelectBranches selectModule;
	MesquiteTree tree;

	public SelectToolExtra (SelectBranches ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		selectModule = ownerModule;
		/*selectTool = new TreeTool(this, "SelectBranches", ownerModule.getPath(), "select.gif", 5,3,"Select Branch", "This tool is used to select branches.  By holding down the Control key as you click, branch selection will accumulate.  By holding down the Shift key, the smallest clade containing the touched branch and already-selected branches will be selected.");
		selectTool.setTouchedCommand(MesquiteModule.makeCommand("selectBranch",  this));
		selectTool.setTouchedFieldCommand(MesquiteModule.makeCommand("deselectAllBranches",  this));*/
		selectCladeTool = new TreeTool(this, "SelectClade", ownerModule.getPath(), "selectClade.gif", 9,7,"Select Clade", "This tool is used to select clades.  By holding down the Control key as you click, clade selection will accumulate.  By holding down the Shift key, the smallest clade containing the touched clade and already-selected clades will be selected.");
		selectCladeTool.setTouchedCommand(MesquiteModule.makeCommand("selectClade",  this));
		selectCladeTool.setTouchedFieldCommand(MesquiteModule.makeCommand("deselectAllBranches",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			//((MesquiteWindow)ownerModule.containerOfModule()).addTool(selectTool);
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(selectCladeTool);
		}
	}
	/*.................................................................................................................*/
	private void wrapSelections(MesquiteTree tree, int node, boolean selectedBelow){
		if (selectedBelow)
			tree.setSelected(node, true);
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			wrapSelections(tree, daughter, selectedBelow || tree.getSelected(node));
		}
	}
	/*.................................................................................................................*/
	private void selectedTwiceAbove(MesquiteTree tree, int node){
		int numSelectedAbove = 0;
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			selectedTwiceAbove(tree, daughter);
			if (tree.anySelectedInClade(daughter))
				numSelectedAbove ++;
		}
		if (numSelectedAbove>1)
			tree.setSelected(node, true);
	}
	/*.................................................................................................................*/
	private void shrinkWrapSelections(MesquiteTree tree, int node){
		selectedTwiceAbove(tree, node);
		wrapSelections(tree, node, false);
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (tree instanceof MesquiteTree)
			this.tree = (MesquiteTree)tree;
		else
			this.tree = null;
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (tree instanceof MesquiteTree)
			this.tree = (MesquiteTree)tree;
		else
			this.tree = null;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		//remember which selected
		return null;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Sets the selection on the branch (if the control modifier is passed, then selection accumulates unless the branch touched is already selected; if the shift modifier is passed, then selection accumulates and the smallest clade containing all selected nodes is selected.", "[node number][x coordinate][y coordinate][modifiers]", commandName, "selectBranch")) {
			if (tree == null)
				return null;
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (arguments.indexOf("shift")>=0) {  //select smallest containing clade
				tree.setSelected(branchFound, !tree.getSelected(branchFound));
				shrinkWrapSelections(tree, tree.getRoot());
				tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				treeDisplay.pleaseUpdate(true);
			}
			else if (arguments.indexOf("control")>=0) {  //single accumulate selection
				tree.setSelected(branchFound, !tree.getSelected(branchFound));
				tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				treeDisplay.pleaseUpdate(true);
			}
			else if (branchFound >0) {
				/*if (tree.getSelected(branchFound)) {
		   			tree.deselectAll();
		   			tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		   			treeDisplay.pleaseUpdate(true);
	   				//treeDisplay.getTreeDrawing().setHighlightsOn(false);
	   			}
	   			else {*/
				tree.deselectAll();
				tree.setSelected(branchFound, true);
				tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

				treeDisplay.pleaseUpdate(true);
				//treeDisplay.getTreeDrawing().setHighlightsOn(true);
				//}
			}
		}
		else if (checker.compare(this.getClass(), "Deselects all branches",  null, commandName, "deselectAllBranches")) {
			if (tree == null)
				return null;
			tree.deselectAll();
			tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

			treeDisplay.pleaseUpdate(true);

		}
		else if (checker.compare(this.getClass(), "Sets selection to entire clade above node touched (selection accumlates if shift or control modifiers passed)",  "[node number][x coordinate][y coordinate][modifiers]", commandName, "selectClade")) {
			if (tree == null)
				return null;
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (arguments.indexOf("shift")>=0) {  //select smallest containing clade
				selectClade(tree, branchFound, !tree.getSelected(branchFound));
				shrinkWrapSelections(tree, tree.getRoot());
				tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				treeDisplay.pleaseUpdate(true);
			}
			else if (arguments.indexOf("control")>=0) {  //single accumulate selection
				selectClade(tree, branchFound, !tree.getSelected(branchFound));
				tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				treeDisplay.pleaseUpdate(true);
			}
			else if (branchFound >0) {
				if (tree.getSelected(branchFound)) { //deselect only if just this clade selected
					selectClade(tree, branchFound, false);
					if (tree.anySelected()) {
						tree.deselectAll();
						selectClade(tree, branchFound, true);
					}
					tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					treeDisplay.pleaseUpdate(true);
				}
				else {
					tree.deselectAll();
					selectClade(tree, branchFound, true);
					tree.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

					treeDisplay.pleaseUpdate(true);
				}
			}
		}
		return null;
	}
	/*-----------------------------------------*/
	/** Returns whether there are any selected nodes in the clade */
	private void selectClade(MesquiteTree tree, int node, boolean select) {
		tree.setSelected(node, select);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			selectClade(tree, d, select);
		}
	}
	public void turnOff() {
		selectModule.extras.removeElement(this);
		super.turnOff();
	}
}





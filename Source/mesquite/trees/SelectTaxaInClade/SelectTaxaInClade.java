/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.SelectTaxaInClade;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class SelectTaxaInClade extends TreeDisplayAssistantI {
	public Vector extras;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		SelectTaxaToolExtra newPj = new SelectTaxaToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Select Taxa";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to select taxa in a clade in a tree window.";
	}
	public void getSubfunctions(){
		//registerSubfunction(new FunctionExplanation("Select Taxa", "(A tool of the Tree Window) Selects the touched terminal taxa", null, getPath() + "selectTaxa.gif"));
		registerSubfunction(new FunctionExplanation("Select Taxa in Clade", "(A tool of the Tree Window) Selects all terminal taxa in the clade of the node touched", null, getPath() + "selectTaxaInClade.gif"));
		super.getSubfunctions();
	}
}

/* ======================================================================== */
class SelectTaxaToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool selectCladeTool; //, selectTool;
	SelectTaxaInClade selectModule;
	MesquiteTree tree;

	public SelectTaxaToolExtra (SelectTaxaInClade ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		selectModule = ownerModule;
		/*selectTool = new TreeTool(this, "SelectTaxa", ownerModule.getPath(), "selectTaxa.gif", 8,6,"Select Taxa", "This tool is used to select terminal taxa.  By holding down the Control key as you click, selection will accumulate.  By holding down the Shift key, the taxa in the smallest clade containing the touched taxon and already-selected taxa will be selected.");
		selectTool.setTouchedTaxonCommand(MesquiteModule.makeCommand("selectTaxa",  this));
		selectTool.setTouchedFieldCommand(MesquiteModule.makeCommand("deselectAllTaxa",  this));
		*
		*/
		selectCladeTool = new TreeTool(this, "SelectTaxaInClade", ownerModule.getPath(), "selectTaxaInClade.gif", 8,6,"Select Taxa In Clade", "This tool is used to select terminal taxa within clades.  By holding down the Control key as you click, selection will accumulate.  By holding down the Shift key, the taxa in the smallest clade containing the touched branch and already-selected taxa will be selected.");
		selectCladeTool.setTouchedCommand(MesquiteModule.makeCommand("selectTaxaInClade",  this));
		selectCladeTool.setTouchedFieldCommand(MesquiteModule.makeCommand("deselectAllTaxa",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			//((MesquiteWindow)ownerModule.containerOfModule()).addTool(selectTool);
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(selectCladeTool);
		}
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

		if (checker.compare(this.getClass(), "Sets selected taxa to those in clade above node touched (selection accumulates if control or shift modifiers passed)",  "[node number][x coordinate][y coordinate][modifiers]", commandName, "selectTaxaInClade")) {
			if (tree == null)
				return null;
			Taxa taxa = tree.getTaxa();
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (arguments.indexOf("shift")>=0) {  //select smallest containing clade
				selectClade(tree, branchFound, true);
				shrinkWrapSelections(tree, tree.getRoot());
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				treeDisplay.pleaseUpdate(false);
			}
			else if (arguments.indexOf("control")>=0) {  //single accumulate selection
				if (allSelected(tree, branchFound))
					selectClade(tree, branchFound, false);
				else
					selectClade(tree, branchFound, true);
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				treeDisplay.pleaseUpdate(false);
			}
			else if (branchFound >0) {
				taxa.deselectAll();
				selectClade(tree, branchFound, true);
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

				treeDisplay.pleaseUpdate(false);
			}
		}
		else if (checker.compare(this.getClass(), "Selects taxa (selection accumulates if control or shift modifiers passed)",  "[taxon number][x coordinate][y coordinate][modifiers]", commandName, "selectTaxa")) {
			if (tree == null)
				return null;
			Taxa taxa = tree.getTaxa();
			int taxonFound= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(taxonFound))
				return null;
			if (arguments.indexOf("shift")>=0) {  //select smallest containing clade
				selectTaxon(taxa, taxonFound, true);
				shrinkWrapSelections(tree, tree.getRoot());
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				treeDisplay.pleaseUpdate(false);
			}
			else if (arguments.indexOf("control")>=0) {  //toggle selection of that taxon

				selectTaxon(taxa, taxonFound, !taxa.getSelected(taxonFound));
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				treeDisplay.pleaseUpdate(false);
			}
			else if (taxonFound >=0) {
				taxa.deselectAll();
				selectTaxon(taxa, taxonFound, true);
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

				treeDisplay.pleaseUpdate(false);
			}
		}
		else if (checker.compare(this.getClass(), "Deselects all taxa",  null, commandName, "deselectAllTaxa")) {
			if (tree == null)
				return null;
			Taxa taxa = tree.getTaxa();
			taxa.deselectAll();
			taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

			treeDisplay.pleaseUpdate(false);

		}
		return null;
	}
	/*-----------------------------------------*/
	/** Selects or deselects all nodes in the clade */
	private void selectTaxon(Taxa taxa, int it, boolean select) {
		taxa.setSelected(it, select);
	}
	/*-----------------------------------------*/
	/** Selects or deselects all nodes in the clade */
	private void selectClade(MesquiteTree tree, int node, boolean select) {
		if (tree.nodeIsTerminal(node)){
			int t = tree.taxonNumberOfNode(node);
			tree.getTaxa().setSelected(t, select);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			selectClade(tree, d, select);
		}
	}
	/*-----------------------------------------*/
	/** Returns whether all nodes in clade selected */
	private boolean allSelected(MesquiteTree tree, int node) {
		if (tree.nodeIsTerminal(node)){
			int t = tree.taxonNumberOfNode(node);
			return (tree.getTaxa().getSelected(t));
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			if (!allSelected(tree, d))
				return false;
		}
		return true;
	}
	/*-----------------------------------------*/
	/** Returns whether all nodes in clade selected */
	private boolean anySelected(MesquiteTree tree, int node) {
		if (tree.nodeIsTerminal(node)){
			int t = tree.taxonNumberOfNode(node);
			return (tree.getTaxa().getSelected(t));
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			if (anySelected(tree, d))
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	private void wrapSelections(MesquiteTree tree, int node, boolean selectedBelow){
		if (tree.nodeIsTerminal(node)){
			if (selectedBelow){
				int t = tree.taxonNumberOfNode(node);
				tree.getTaxa().setSelected(t, true);
			}
			return;
		}
		boolean selectAbove = selectedBelow || selectedTwiceAbove(tree, node);
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			wrapSelections(tree, daughter, selectAbove);
		}
	}
	/*.................................................................................................................*/
	private boolean selectedTwiceAbove(MesquiteTree tree, int node){
		if (tree.nodeIsTerminal(node)){
			int t = tree.taxonNumberOfNode(node);
			return (tree.getTaxa().getSelected(t));
		}
		int numSelectedAbove = 0;
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			if (anySelected(tree, daughter))
				numSelectedAbove ++;
		}
		return (numSelectedAbove>1);
	}
	/*.................................................................................................................*/
	private void shrinkWrapSelections(MesquiteTree tree, int node){
		wrapSelections(tree, node, false);
	}
	public void turnOff() {
		selectModule.extras.removeElement(this);
		super.turnOff();
	}
}





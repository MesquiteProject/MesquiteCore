/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.AugmentTreeRandomly;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class AugmentTreeRandomly extends RndTreeModifier {
	int mode = addToAnyIgnoreLengths;
	static final int addToAnyIgnoreLengths = 0;
	static final int addToAnyConsiderLengths = 1;
	static final int addOnlyToOriginalBranches = 2;
	StringArray modes;
	MesquiteString modeName;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		modes = new StringArray(3);
		modes.setValue(addToAnyIgnoreLengths, "To Any, Ignoring Length");
		modes.setValue(addToAnyConsiderLengths, "To Any, Considering Length");
		modes.setValue(addOnlyToOriginalBranches, "Only To Original Branches");
		modeName = new MesquiteString(modes.getValue(mode));
		MesquiteSubmenuSpec mss = addSubmenu(null, "Augment on Which Branches", makeCommand("setMode",  this), modes);
		mss.setSelected(modeName);
		if (!MesquiteThread.isScripting()){
			int newMode = ListDialog.queryList(containerOfModule(), "Augment on Which Branches?", "On which branches should taxa be added?", MesquiteString.helpString, modes.getStrings(), 0);
			if (MesquiteInteger.isCombinable(newMode)) {
				mode = newMode;
	    			modeName.setValue(modes.getValue(mode));
	    		}
		}
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
   	 	temp.addLine("setMode " + mode);
 	 	return temp;
  	 }
	NameReference colorNameRef = NameReference.getNameReference("color");
	private void setColor(Tree tree, int node, boolean[] added){
		if (tree == null || !(tree instanceof Associable))
			return;
		Associable aTree = (Associable)tree;
		if (aTree.getWhichAssociatedLong(colorNameRef)==null)
			aTree.makeAssociatedLongs("color");
		aTree.setAssociatedLong(colorNameRef, node, ColorDistribution.numberOfGreen, true);
		added[node] = true;
	}
	/** goes through the tree returning which node is the nodeNumberTH found in the traversal */
	private boolean colorClades(Tree tree, int node, boolean[] added){
		if (tree.nodeIsTerminal(node)) {
			return added[node];
		}
		boolean allDescendants = true;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			boolean desc = colorClades(tree, d, added);
			allDescendants = allDescendants && desc;
		}
		if (allDescendants) {
			setColor(tree, node, added);
		}
		return allDescendants;
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	/**/
    	 	if (checker.compare(this.getClass(), "Sets where to add taxa in augmentation", "[0 = add to any branches, ignoring length; " +
    	 				"1 = add to any branches, considering branch length; 2 = add only to original branches]", commandName, "setMode")) {
			String name = parser.getFirstToken(arguments);
			int newMode = modes.indexOf(name);
			if (newMode >=0 && newMode!=mode){
				mode = newMode;
	    			modeName.setValue(modes.getValue(mode));
	    			if (!MesquiteThread.isScripting())
	    				parametersChanged();
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	 
   	 boolean warned = false;
	/*.................................................................................................................*/
   	 public void modifyTree(Tree tree, MesquiteTree augmented, RandomBetween rng){
   		if (tree == null)
   			return;
   		Taxa taxa = tree.getTaxa();
   		if (mode == addOnlyToOriginalBranches) {
   			augmented.deselectAll();
   			selectAllNodes(augmented, augmented.getRoot());
   		}
   		boolean found = false;
   		boolean[] added = new boolean[tree.getNumNodeSpaces()];
   		for (int i = 0; i<added.length; i++) 
   			added[i] = false;
   		for (int it = 0; it<taxa.getNumTaxa(); it++)
   			if (!tree.taxonInTree(it)) {
   				found = true;
   				if (mode == addToAnyConsiderLengths) {
   					double totalLength = totalBranchLength(augmented, augmented.getRoot(), false);
   					double choice = (rng.nextDouble() * totalLength);
   					int recipientNode = selectedNodeInTraversalByLength(augmented, choice, false);
   					if (!augmented.nodeExists(recipientNode))
   						MesquiteMessage.warnProgrammer("Error: recipient node doesn't exist; choice " + choice + " recipientNode " + recipientNode);
   					else {
	  					augmented.graftTaxon(it, recipientNode, false);
	  					setColor(augmented, augmented.nodeOfTaxonNumber(it), added);
	  					augmented.setBranchLength(augmented.motherOfNode(augmented.nodeOfTaxonNumber(it)), augmented.getBranchLength(recipientNode, 1.0) - excessLength, false);
	   					augmented.setBranchLength(recipientNode, excessLength, false);
	   					double shortest = augmented.shortestPathAboveNode(recipientNode, 1.0);
	   					double tallest = augmented.tallestPathAboveNode(recipientNode, 1.0);
	   					//set length of grafted to randomly between highest and lowest in sister clade
	   					augmented.setBranchLength(augmented.nodeOfTaxonNumber(it), excessLength+(shortest+(tallest-shortest)*rng.nextDouble()), false); 
   					}
  				}
   				else {
	   				if (mode == addOnlyToOriginalBranches){
	   					int numChoices = tree.numberOfNodesInClade(tree.getRoot());
	   					// choose one and add it in augmented
	   					int choice = rng.randomIntBetween(0, numChoices-1);
	   					int recipientNode = selectedNodeInTraversal(augmented, choice, true);
	   					if (!augmented.nodeExists(recipientNode))
   							MesquiteMessage.warnProgrammer("Error: recipient node doesn't exist; choice " + choice + " numChoices " + numChoices + " recipientNode " + recipientNode + " tree " + augmented.writeTree() + " (a)");
	   					else {
	   						augmented.graftTaxon(it, recipientNode, false);
	  						setColor(augmented, augmented.nodeOfTaxonNumber(it), added);
	  					}
	   					
	   				}
	   				else if (mode == addToAnyIgnoreLengths) {
	   					int numChoices = augmented.numberOfNodesInClade(augmented.getRoot());
	   					int choice = rng.randomIntBetween(0, numChoices-1);
	   					int recipientNode = selectedNodeInTraversal(augmented, choice, false);
	   					if (!augmented.nodeExists(recipientNode))
   							MesquiteMessage.warnProgrammer("Error: recipient node doesn't exist; choice " + choice + " numChoices " + numChoices + " recipientNode " + recipientNode + " tree " + augmented.writeTree() + " (b)");
	   					else {
	   						augmented.graftTaxon(it, recipientNode, false);
	  						setColor(augmented, augmented.nodeOfTaxonNumber(it), added);
	  					}
	   				}
   				}
   			}
   		if (!found && !warned){
   			discreetAlert( getName() + " was not able to augment the tree because all taxa in the taxa block are already in the base tree being used.");
   			warned = true;
   		}
   		colorClades(augmented, augmented.getRoot(), added);
   		if (mode == addOnlyToOriginalBranches)
   			augmented.deselectAll();
   	}
	/*-----------------------------------------*/
	private int countNodes;
	private int nodeFound;
	/** goes through the tree returning which node is the nodeNumberTH found in the traversal */
	private void findSelectedNodeInTraversal(Tree tree, int node, int nodeNumber, boolean selectedOnly){
		if (tree.getSelected(node) || !selectedOnly) {
			countNodes++;
			if ((countNodes==nodeNumber) && (nodeFound==0)) {
				nodeFound=node;
				return;
			}
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && nodeFound==0; d = tree.nextSisterOfNode(d))
				findSelectedNodeInTraversal(tree, d, nodeNumber, selectedOnly);
	}
	/*-----------------------------------------*/
	/**Returns indexTH selected node in tree traversal*/
	private int selectedNodeInTraversal(Tree tree, int index, boolean selectedOnly){
		countNodes=-1;
		nodeFound=0;
		findSelectedNodeInTraversal(tree, tree.getRoot(), index, selectedOnly);
		return nodeFound;
	}

	private void selectAllNodes(MesquiteTree tree, int node){
		tree.setSelected(node, true);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				selectAllNodes(tree, d);
	}
	private double totalBranchLength(Tree tree, int node, boolean selectedOnly){
		double tot = 0;
		if (tree.getSelected(node) || !selectedOnly)
			tot = tree.getBranchLength(node, 1.0);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			tot += totalBranchLength(tree, d, selectedOnly);
		return tot;
	}
	/** goes through the tree returning which node is the nodeNumberTH found in the traversal */
	double countLength;
	double excessLength;
	private void findSelectedNodeInTraversalByLength(Tree tree, int node, double targetLength, boolean selectedOnly){
		if (tree.getSelected(node) || !selectedOnly){
			countLength+= tree.getBranchLength(node, 1.0);
		}
		if ((countLength>=targetLength) && (nodeFound==0)) {
			nodeFound=node;
			excessLength = countLength-targetLength;
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && nodeFound==0; d = tree.nextSisterOfNode(d))
				findSelectedNodeInTraversalByLength(tree, d, targetLength, selectedOnly);
	}
	/*-----------------------------------------*/
	/**Returns indexTH selected node in tree traversal*/
	private int selectedNodeInTraversalByLength(Tree tree, double targetLength, boolean selectedOnly){
		countLength=0;
		excessLength = 0;
		nodeFound=0;
		findSelectedNodeInTraversalByLength(tree, tree.getRoot(), targetLength, selectedOnly);
		return nodeFound;
	}


	/*.................................................................................................................*/
   	public String getTreeNameString(Taxa taxa, int itree) {
   		return "Random augmentation #" + itree;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Augment Tree Randomly";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Augments tree by random placement of excluded taxa.  There are three modes: adding only to original branches, ignoring lengths; adding to any branch, considering branch length; adding to any branch, ignoring branch length.  With lengths considered, probability of placing taxon on a branch is proportional to the branch's length.";
   	 }
	/*.................................................................................................................*/
   	public String getParameters() {
   		return"Placement of added taxa: " + modes.getValue(mode);
   	}
   	 
}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.search.SPRRearranger;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SPRRearranger extends TreeSwapper {
	boolean nodeIsRoot = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		return true;
  	 }
  	 
	/*.................................................................................................................*
	private void findAttach(AdjustableTree tree, int node, int whichNode, int cutNode, MesquiteInteger total, MesquiteInteger attach){
		if (node == cutNode)
			return;
		if (tree.motherOfNode(cutNode)!=node && !tree.nodesAreSisters(cutNode, node))
			total.increment();
		if (total.getValue()==whichNode) {
			attach.setValue(node);
			return;
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && attach.isUnassigned(); d = tree.nextSisterOfNode(d)) {
			findAttach(tree, d, whichNode, cutNode, total, attach);
		}
	}
	/*.................................................................................................................*
	private void findBranchesOfRearrangement(AdjustableTree tree, int node,  MesquiteInteger total, int target, MesquiteInteger cut, MesquiteInteger attach, int numNodesTotal){
		if (!cut.isUnassigned())
			return;
		int current = total.getValue();
		if (tree.nodeIsTerminal(node)){
			total.add(numNodesTotal-3); //TODO: -2 if node is polytomous
			if (total.getValue()>=target) {
				cut.setValue(node);
				MesquiteInteger counter = new MesquiteInteger(0);
				findAttach(tree, tree.getRoot(), target-current, node, counter, attach);
			}
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && cut.isUnassigned(); d = tree.nextSisterOfNode(d)) {
				findBranchesOfRearrangement(tree, d, total, target, cut, attach, numNodesTotal);
			}
			if (!cut.isUnassigned())
				return;
			total.add(numNodesTotal-tree.numberOfNodesInClade(node)-2);//TODO: -1 if node is polytomous
			if (total.getValue()>=target) {
				cut.setValue(node);
				MesquiteInteger counter = new MesquiteInteger(-1);
				findAttach(tree, tree.getRoot(), target-current, node, counter, attach);
			}
		}
	}
	/*.................................................................................................................*/
	/** This method is more for insurance = it guarantees that the root is used if that is what is needed.  Will avoid problems if root node has its number changed. */
	private int getBaseNode(AdjustableTree tree, int baseNode) {
		if (nodeIsRoot)
			return tree.getRoot();
		else
			return baseNode;
	}
	/*.................................................................................................................*/
	/** finding the cut node for rearrangement # target, by checking total number of moves a branch can make*/
	private void findCutNode(AdjustableTree tree,int baseNode,  int node,MesquiteInteger cut, MesquiteInteger whichMove, MesquiteLong total, long target){
		if (!cut.isUnassigned())
			return;
		long currentCount = total.getValue();
		total.add(numberOfMovesNodeCanMake(tree, getBaseNode(tree,baseNode), node));
		if (currentCount<=target && total.getValue()>target) {
			cut.setValue(node);
			whichMove.setValue((int)(target-currentCount));
			return;
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && cut.isUnassigned(); d = tree.nextSisterOfNode(d)) {
			findCutNode(tree, getBaseNode(tree,baseNode), d, cut, whichMove, total, target);
		}
	}
	/*.................................................................................................................*/
	/** finding the cut node for rearrangement # target, by checking total number of moves a branch can make*/
	private void findAttachNode(AdjustableTree tree,int baseNode, int node,int cutNode,  MesquiteInteger attach, MesquiteLong total, long target){
		if (!attach.isUnassigned())
			return;
		//seeing if valid attach node for given cutNode
		if (node==cutNode) //don't even go into that clade
			return;
		int motherOfCut = tree.motherOfNode(cutNode);
		if ((node!=motherOfCut && motherOfCut!=tree.motherOfNode(node)) || tree.nodeIsPolytomous(motherOfCut)) {
			//can move only if not mother or sister, unless polytomous
			//is a legitimate target; see if it's the index wanted
			if (total.getValue() == target) {
				attach.setValue(node);
				return;
			}
			total.add(1);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && attach.isUnassigned(); d = tree.nextSisterOfNode(d)) {
			findAttachNode(tree, getBaseNode(tree,baseNode), d, cutNode, attach, total, target);
		}
	}
	/*.................................................................................................................*/
	private void upCountTotal(AdjustableTree tree, int baseNode, int node,MesquiteLong total, int numNodesTotal){
		total.add(numberOfMovesNodeCanMake(tree, getBaseNode(tree,baseNode), node));
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			upCountTotal(tree, getBaseNode(tree,baseNode), d, total, numNodesTotal);
		}
	}
	
	/*.................................................................................................................*/
	/** Returns the number of moves a node can make.  Is number of nodes in the tree outside of its own clade (since node can't move into its own
	clade) and, if its mother is dichotomous, minus 2 (if dichtomous, it can't move onto its own ancestor since it's already there, and 
	it can move onto a sister only if it has more than one sister).*/
	private long numberOfMovesNodeCanMake(AdjustableTree tree,int baseNode,  int node){
		int root = getBaseNode(tree,baseNode);
		if (!tree.nodeExists(node) || root==node)
			return 0;
		int numInClade = tree.numberOfNodesInClade(node);
		int numInTree = tree.numberOfNodesInClade(root);
		int mother = tree.motherOfNode(node);
		if (!tree.nodeIsPolytomous(mother))
			return numInTree-numInClade-2;
		else
			return numInTree-numInClade;
	}
	/* TODO: currently this only works if baseNode = root */
	//  Bad things (e.g. thread death) can happen if baseNode != tree.getRoot(). J.C. Oliver 2013.
	/** baseNode <strong>must</strong> be the root of {@code tree}.*/
	public long numberOfRearrangements(AdjustableTree tree, int baseNode) {
		nodeIsRoot = baseNode == tree.getRoot();
		MesquiteLong counter = new MesquiteLong(0); 
		upCountTotal(tree, baseNode, baseNode, counter, tree.numberOfNodesInClade(baseNode));
		return counter.getValue();
	}

	/*.................................................................................................................*/
	/** Returns the number of rearrangments.*/
	public long numberOfRearrangements(AdjustableTree tree){
		nodeIsRoot = true;
		return numberOfRearrangements(tree, tree.getRoot());
	}
	int count = 0;
	int rearrangement=0;
	/*.................................................................................................................*/
	/* TODO: currently this only works if baseNode = root */
	/** Rearranges the tree to the i'th rearrangment. */
	public void rearrange(AdjustableTree tree, int baseNode, long i) {
		if (tree==null) // || i<0 || i> 2*tree.numberOfInternalsInClade(tree.getRoot())-1  //TODO: check to see that not out of bounds
			return;
		nodeIsRoot = baseNode == tree.getRoot();
		count = 0;
		MesquiteLong total = new MesquiteLong(0);
		MesquiteInteger cut = new MesquiteInteger();
		MesquiteInteger whichMove = new MesquiteInteger(0);
		cut.setToUnassigned();
		findCutNode(tree, baseNode,  baseNode, cut, whichMove,  total, i);
		
		MesquiteInteger attach = new MesquiteInteger();
		attach.setToUnassigned();
		total.setValue(0);
		findAttachNode(tree, baseNode, baseNode,cut.getValue(),  attach, total, whichMove.getValue());
		if (!cut.isUnassigned() && !attach.isUnassigned()) {
			tree.moveBranch(cut.getValue(), attach.getValue(), false);
		}
	}
	public void rearrange(AdjustableTree tree, long i){
		nodeIsRoot = true;
		rearrange(tree,tree.getRoot(),i);
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "SPR Rearranger";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Rearranges a tree by subtree pruning and regrafting (SPR).";
   	 }
   	 public boolean requestPrimaryChoice(){
   	 	return true;
   	 }
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }

}


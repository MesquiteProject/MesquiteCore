/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.util.*;
import java.math.*;

/** This interface matches some of the basic tree changing methods of MesquiteTree.  It was
created (Aug. 2000) to allow methods to use the interface, in case tree structures from other 
programs needed to be used.  Currently, though, AdjustableTree is not much used directly in
Mesquite.*/
public interface AdjustableTree extends Tree {
	
	/** Sets the name of the tree.*/
	public void setName(String name) ;
	
	public void setSelected(int node, boolean selected);
	
	/** Sets the  tree vector to which the tree belongs.*/
	public void setTreeVector(TreeVector treeVector) ;
	/** Sets whether tree is rooted.*/
	public void setRooted(boolean rooted, boolean notify) ;
	/** Sets whether polytomies are to be assumed soft; 0= hard; 1 = soft; 2 = use default.*/
	public void setPolytomiesAssumption(int assumption, boolean notify);
	/** Sets par to be an additional parent of node.  Used to make reticulations.  Currently there
	is no way to subtract parents, nor to rearrange trees that contain reticulations.*/
	public void setParentOfNode(int node, int par, boolean notify) ;
	/** An indelicate quick way to delete all reticulations (extra parents) from a node.*/
	public void snipReticulations(int node);
	/** Sets the branch length of node.*/
	public  void setAllBranchLengths(double length, boolean notify) ; 
	/** Sets the branch length of node (stored as a double internally).*/
	public  void setBranchLength(int node, int length, boolean notify) ; 
	/** Sets the branch length of node.*/
	public  void setBranchLength(int node, double length, boolean notify) ; 
	/** Sets the tree to be a default bush.*/
	public void setToDefaultBush(int numTaxa, boolean notify) ;
	/** Sets the tree to be a default ladder.*/
	public void setToDefaultLadder(int numTaxa, boolean notify) ;
	/** Returns the first tree in the sequence of all possible trees (polytomies are set to first resolution)*/
	//public void setToFirstInSequence();
	/** Returns the next tree in the sequence of all possible trees (polytomies are set to first resolution)*/
	//public boolean setToNextInSequence();
	/** Sets the node label to the passed string (doesn't copy string; just uses reference) */
	public void setNodeLabel(String s, int node);
	/** Branches a terminal node off taxon number taxonNum, and assigns it taxon newNumber  */
	public void splitTerminal(int taxonNum, int newNumber, boolean notify) ;
	/** Sprouts a new daughter from node and returns it. */
	public int sproutDaughter(int node, boolean notify) ;
	/** Interchanges two branches of tree.*/
	public boolean interchangeBranches(int node, int otherNode, boolean notify) ;   
	/** Randomly rotate branches of tree.*/
 	public boolean randomlyRotateDescendants(int node, Random rng, boolean notify);
	/** reroots tree at taxonSet, if non null, otherwise reroots at first taxon in tree, and right-ladderizes tree.*/
 	public boolean standardize(TaxaSelectionSet taxonSet, boolean notify);

	/** Collapses branch to yield polytomy.*/
	public boolean collapseBranch(int node, boolean notify) ;   
	/** Collapses all internal branches within clade above node, to yield bush.*/
	public boolean collapseAllBranches(int node, boolean below, boolean notify) ;   
	/** Excise node and clade above it from tree, zeroing information at each node in clade.*/
	public boolean deleteClade(int node, boolean notify);
	/** Excise node and clade above it from tree but leave the clade intact, in case it is to be attached elsewhere.*/
	public boolean snipClade(int node, boolean notify) ;   
	/** Attach terminal taxon to tree along given branch.*/
	public boolean graftTaxon(int taxon, int toN, boolean notify) ;   
	/** Attach node fromN (and any clade to attached to it) along node toN.  Returns the new node created to attach fromN*/
	public int graftClade(int fromN, int toN, boolean notify) ;   
	
	public boolean graftCladeFromDescription(String TreeDescription, int node, MesquiteInteger stringLoc, TaxonNamer namer);
	/** Move branch so as to excise branchFrom from its current position, and attach it to branch beneath
	node branchTo.  If successful, rename as given (to ensure renamed before notified).*/
	public boolean moveBranch(int branchFrom, int branchTo, boolean notify) ;
	/** Inserts a new node on the branch represented by "node", and returns the number of the inserted node */
	public int insertNode(int node, boolean notify);
	/** reroot the clade below node atNode.*/
	public boolean reroot(int atNode, int cladeRoot, boolean notify) ;
	/** ultrametricizes the tree.*/
	public void arbitrarilyUltrametricize();
}



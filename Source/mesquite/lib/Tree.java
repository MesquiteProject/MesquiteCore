/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
import java.math.*;
import java.util.*;

/** This interface matches the readable methods of MesquiteTree, and was created so that translator classes could
be written that could access tree structures from other programs.  Most calculations are passed trees under the
guise of interface Tree, which allows these calculations to be used on tree structures other than MesquiteTree, and
which ensures they will not modify the tree.  Some modules that need to modify tree structures check to see whether the passed
Tree is an instance of AdjustableTree or MesquiteTree.  The change to using interfaces (from the previous system
which just passed MesquiteTree under the name Tree) was done Aug. 2000, and the change has not fully settled in yet.
 */
public interface Tree extends Listable {
	/** writing mode -- puts full taxon name in tree descriptions */
	public static final int BY_NAMES = 0;
	/** writing mode -- puts number of taxon in tree descriptions */
	public static final int BY_NUMBERS = 1;
	/** writing mode -- puts label from translation table, if available, in tree descriptions */
	public static final int BY_TABLE = 2;
	public MesquiteTree cloneTree();
	/** Sets this tree to be a clone of that passed.*/
	public void setToClone(MesquiteTree tree);
	public boolean isDefined();
	/** Returns false if tree is null, has no nodes, or is locked.*/
	public boolean isValid();
	/** Returns false if tree is null, has no nodes, or is locked, or if some taxonNumbers are negative.*/
	public boolean isLocked();
	public long getID();
	public long getVersionNumber();
	public Taxa getTaxa();
	public int getRoot();
	public int getSubRoot();
	public boolean getRooted();
	/** Returns whether root is real.  It isn't real if the tree is unrooted.*/
	public boolean rootIsReal();
	/** Returns true if trees have same topology. */
	public boolean equalsTopology(Tree tree, boolean checkBranchLengths);
	/** Returns the polytomies assumption for this tree; 0= hard; 1 = soft; 2 = use default.*/
	public int getPolytomiesAssumption();
	/** Returns the number of terminal taxa in the Taxa to which the tree refers.  This is not necessarily the 
	number of terminal taxa in the tree itself (for that, call numberOfTerminalsInClade(root)).*/
	public int getNumTaxa();
	/** Returns the number of node spaces available in storage arrays within tree object.*/
	public int getNumNodeSpaces();
	/** Returns the number of a randomly chosen node.*/
	public int randomNode(RandomBetween rng, boolean allowRoot);
	/** Returns true if N is a valid node designation in tree.*/
	public  boolean nodeExists(int node);
	/** Returns whether node is part of tree. Differs from nodeExists by doing full recursion (more reliable, slower).*/
	public boolean nodeInTree(int sought);
	/** Returns true if node is an internal node.*/
	public  boolean nodeIsInternal(int node);
	/** Returns true if node is a terminal node.*/
	public  boolean nodeIsTerminal(int node);
	/** Returns whether taxon is part of tree. */
	public boolean taxonInTree(int taxonNum);
	/** Returns the taxon number of the node.  This is -1 if the node is internal,
	the taxon number if terminal.*/
	public  int taxonNumberOfNode(int node); 
	/** Returns the terminal node corresponding to the given taxon number; 0 if the taxon  is not part of the tree.*/
	public int nodeOfTaxonNumber(int taxonNum);
	/** Returns the immediate ancestor of node (mother) if node is non-reticulate.
	If node is reticulate, returns the first (primary) parent.*/
	public  int motherOfNode(int node);
	/** Returns the node's mother's mother.*/
	public  int grandmotherOfNode(int node);
	public  int firstLegalDaughterOfNode(int node, int[] legality);
	public  int nextLegalSisterOfNode(int node, int[] legality);
	public  int lastLegalDaughterOfNode(int node, int[] legality);
	public  int getLegalRoot(int[] legality);
	/** Returns the number of parents of node.*/
	public int numberOfParentsOfNode(int node);
	/** Returns the indexTH parent of node.*/
	public int parentOfNode(int node, int index);
	/** Returns the array of parents of node.  If node is not reticulate, the array returned contains
	only the mother node.*/
	public int[] parentsOfNode(int node);
	/** Returns true iff first branch is a descendant of second branch.*/
	public boolean descendantOf(int branchD, int branchA); 
	/** Returns depth of second branch below first; -1 if not ancestral.*/
	public int depthToAncestor(int branchD, int branchA); 
	/** Returns the deepest path in the clade, in terms of numbers of nodes*/
	public int deepestPath(int node);
	/** Returns the first (left-most) daughter of node.*/
	public  int firstDaughterOfNode(int node);
	/** Returns the right-most daughter of node.*/
	public int lastDaughterOfNode(int node);
	/** Returns the array of daughters of a node.  Normally it will be best to cycle through the
	daughters as shown in the recursion example in the documentation for the Tree class.*/
	public int[] daughtersOfNode(int node);
	/** Returns true if branchD is an immediate daughter of branchA */
	public boolean daughterOf(int branchD, int branchA); 
	/** Returns true if node is the first (leftmost) daughter of its mother.*/
	public boolean nodeIsFirstDaughter(int node);
	/** Returns the index of the node among its sisters (i.e., if it is the first daughter of
	its mother, the second , third, etc.*/
	public int whichDaughter(int node);
	/** Returns the indexTH daughter of node.*/
	public int indexedDaughterOfNode(int node, int index);
	/** Returns the number of daughters of the node.*/
	public int numberOfDaughtersOfNode(int node);
	/** Returns if branchD is descendant of branchA, then which daughter of branchA does it descend from?
		Returns 0 if not descendant.*/
	public int whichDaughterDescendantOf(int branchD, int branchA); 
	/** Returns the node's sister immediately to the right.  If the node has no
	sister to the right, returns 0 (which is not a valid node designation).*/
	public  int nextSisterOfNode(int node);
	/** Returns the node's sister immediately to the left.  If the node has no 
	sister to the right, returns 0 (which is not a valid node designation).*/
	public int previousSisterOfNode(int node);
	/** Returns true if branch1 and branch2 are sisters by their mother (i.e., primary parent).
	There is currently no method to return whether two nodes share at least one parent.*/
	public  boolean nodesAreSisters(int branch1, int branch2); 
	/** Returns whether tree has nodes with more than one parent.*/
	public  boolean hasReticulations();
	/** Returns whether clade has unbranched internal nodes.*/
	public  boolean hasUnbranchedInternals(int node);
	/** Returns true if the node is an internal node with only a single daughter.*/
	public boolean nodeIsUnbranchedInternal(int node);
	/** Returns whether clade has polytomies.*/
	public  boolean hasPolytomies(int node);
	/** Returns whether clade has soft polytomies (uncertainties).*/
	public  boolean hasSoftPolytomies(int node);
	/** Returns true if the node is polytomous (has more than two daughters).*/
	public boolean nodeIsPolytomous(int node);
	/** Returns true if the node has one descendant, two, or is a hard polytomy.*/
	public boolean nodeIsHard(int node); //TODO: in future allow individual trees or nodes to store whether polytomy is hard or soft
	/** Returns true if the node is a soft polytomy.*/
	public boolean nodeIsSoft(int node);
	/** Returns number of total nodes (internal and external) in clade.*/
	public  int numberOfNodesInClade(int node);
	/** Returns number of terminal taxa in clade.*/
	public  int numberOfTerminalsInClade(int node);
	/** Returns number of internal nodes in clade.*/
	public  int numberOfInternalsInClade(int node);
	/** Returns the left-most terminal that is descendant from node.*/
	public  int leftmostTerminalOfNode(int node);
	/** Returns the right-most terminal that is descendant from node.*/
	public  int rightmostTerminalOfNode(int node);
	/** Returns list of terminal taxa of clade of node.*/
	public Bits getTerminalTaxaAsBits(int node);
	/** Returns list of terminal taxa of clade of node.*/
	public int[] getTerminalTaxa(int node);
	/** Returns list of terminal taxa NOT in clade of node, as bits.*/
	public Bits getTerminalTaxaComplementAsBits(int node);
	/** Returns list of terminal taxa NOT in clade of node.  Into this version you pass the int[] of the terminal taxa in the tree, for speed's sake. */
	public int[] getTerminalTaxaComplement(int node);
	/** Returns list of terminal taxa NOT in clade of node.  Into this version you pass the int[] of the terminal taxa in the tree, for speed's sake. */
	public int[] getTerminalTaxaComplement(int[] allTerminals, int node);
	/** Returns list of terminal taxa NOT in clade of node.  Into this version you pass int[]'s, for speed's sake. */
	public int[] getTerminalTaxaComplement(int[] allTerminals, int[] nodeTerminals);
	/** Returns true iff the terminal taxa listed in "terminals" form a clade. */
	public boolean isClade(Bits terminals);
	/** Returns true iff the terminal taxa listed in "terminals" form a convex part of the tree.  Also returns the two nodes that form the boundary*/
	public boolean isConvex(Bits terminals, MesquiteInteger descendantBoundary);
	/** Returns true iff the terminal taxa listed in "terminals" form a convex part of the tree. */
	public boolean isConvex(Bits terminals);
	/**Returns indexTH node in tree traversal*/
	public int nodeInTraversal(int index);
	/**Returns indexTH node in traversal through clade*/
	public int nodeInTraversal(int index, int cladeRoot);
	/** Returns next node in preorder traversal.*/
	public int nextInPreorder(int node);
	/** Returns next node in postorder traversal.*/
	public int nextInPostorder(int node);
	/** Returns first node in postorder traversal.*/
	public int firstInPostorder();
	/** Returns true if the first node is an ancestor of the second node.*/
	public boolean isAncestor(int potentialAncestor, int node); 
	/** Returns most recent common ancestor of two branches.*/
	public int mrca(int branchA, int branchB); 
	/** Returns most recent common ancestor of the terminals designated in terminals.*/
	public int mrca(Bits terminals);
	/*New code added Aug.14.07 oliver*/ //TODO: delete new code comments
	/** Returns most recent common ancestor for an array of nodes.*/
	public int mrca(int[] nodes);
	/** Returns most recent common ancestor for an array of nodes; if boolean ignoreMissing = false, it 
	 * will only return a non-zero (existing) node if all the nodes in the array are present in the 
	 * tree.  If ignoreMissing = true, it will return the mrca of those nodes passed in the node array
	 * that are actually present in the tree; nodes in the node array that are not present in the tree 
	 * are ignored.*/
	public int mrca(int[] nodes, boolean ignoreMissing);
	/** Returns most recent common ancestor for an array of taxon numbers; if boolean ignoreMissing = false, it 
	 * will only return a non-zero (existing) node if all the taxons in the array are present in the 
	 * tree.  If ignoreMissing = true, it will return the mrca of those taxons passed in the taxon array
	 * that are actually present in the tree; taxons in the taxon array that are not present in the tree 
	 * are ignored.*/
	public int mrcaTaxons(int[] taxons, boolean ignoreMissing);
	/*End new code added Aug.14.07 oliver*/
	/** Returns most recent common ancestor of selected taxa.*/
	public int mrcaSelected(); 
	/** Returns the closest ancestor that has more than one daughter.*/
	public  int branchingAncestor(int node);
	/** Returns the closest descendant that has more than one daughter, or is terminal.*/
	public  int branchingDescendant(int node);
	/** Returns the next (clockwise) to node connected to anc.  This is one of the UR procedures, designed
	to allow unrooted style traversal through the tree*/
	public int nextAroundUR(int anc, int node);
	/** Returns the first (left-most) daughter of node in an UNROOTED sense where the node
	is treated as descendant from anc. This is one of the UR procedures, designed
	to allow unrooted style traversal through the tree*/
	public  int firstDaughterOfNodeUR(int anc, int node);
	/** Returns the node's sister immediately to the right in an UNROOTED sense where the node
	is treated as descendant from anc, which is descendant from ancAnc.  If the node has no
	sister to the right, returns 0 (which is not a valid node designation). This is one of the UR procedures, designed
	to allow unrooted style traversal through the tree*/
	public  int nextSisterOfNodeUR(int ancAnc, int anc, int node);
	/** Returns the first (left-most) daughter of node in an UNROOTED sense where the node
	is treated as descendant from anc. This is one of the UR procedures, designed
	to allow unrooted style traversal through the tree*/
	public  int lastDaughterOfNodeUR(int anc, int node);
	/** Returns what node number in Mesquite's standard rooted sense corresponds to the anc-node branch.*/
	public  int nodeOfBranchUR(int anc, int node);
	/** Returns all of the "daughters" of a node, treating the tree as unrooted.  That is, it returns as
	 * one of the daughters the mother of the node (which should be the the last entry in the array). 
	 * If you pass into root the MRCA of a subtree containing "node", then it will treat
	 * that subtree as unrooted.  Note:  if node is not the root or a descendant of root, then this will return null */
	public int[] daughtersOfNodeUR (int root, int node);
	
	//============virtual taxon deletion ==================
	/** Marks taxon (and any nodes required by it) as deleted virtually in the boolean array.  Used in conjunction with subsequent
	 * traversals that ignore the deleted area, e.g. for ignoring taxa with missing data.*/
	public void virtualDeleteTaxon(int it, boolean[] deleted);
	/** Writes a tree description into the StringBuffer, filtering virtually deleted nodes */
	public void writeTree(int node, StringBuffer treeDescription, boolean[] deleted);
	/** Returns the root, filtering virtually deleted nodes*/
	public  int getRoot(boolean[] deleted);
	/** Returns whether root is real.  It isn't real if the tree is unrooted.*/
	public boolean rootIsReal(boolean[] deleted);
	/** Returns the first (left-most) daughter of node filtering virtuallyDeleted nodes.*/
	public  int firstDaughterOfNode(int node, boolean[] deleted);
	/** Returns the last (right-most) daughter of node filtering virtuallyDeleted nodes.*/
	public  int lastDaughterOfNode(int node, boolean[] deleted);
	/** Returns the node's sister immediately to the right, filtering virtually deleted nodes.*/
	public  int nextSisterOfNode(int node, boolean[] deleted);
	/** Returns the node's sister immediately to the left, filtering virtually deleted nodes.*/
	public  int previousSisterOfNode(int node, boolean[] deleted);
	/** Returns the immediate ancestor of node (mother) if node is non-reticulate, filtering virtually deleted nodes*/
	public  int motherOfNode(int node, boolean[] deleted);
	/** Returns the branch length of the node, filtered by virtual deletion of nodes.*/
	public  double getBranchLength(int node, boolean[] deleted);
	/** Returns the branch length of the node.  If the branch length is unassigned, pass back the double passed in, filtered by virtual deletion of nodes*/
	public  double getBranchLength(int node, double ifUnassigned, boolean[] deleted);
	/** Returns whether branch length of node is unassigned, filtering virtually deleted nodes.*/
	public  boolean branchLengthUnassigned(int node, boolean[] deleted);
	//============ ==================
	/** Puts into TreeVector trees from each rerooting within the clade of the node passed*/ 
	public void makeAllRootings(int cladeRoot, TreeVector trees);
	/** Returns true if tree has all branch lengths assigned.*/
	public boolean allLengthsAssigned(boolean includeRoot); 
	/** Returns true if tree has branch lengths.*/
	public boolean hasBranchLengths(); 
	/** Returns the branch length of the node.*/
	public  double getBranchLength(int node); 
	/** Returns the branch length of the node.  If the branch length is unassigned, pass back the double passed in*/
	public  double getBranchLength(int node, double ifUnassigned); 
	public  boolean branchLengthUnassigned(int node);
	public double tallestPathAboveNode (int node);
	public double tallestPathAboveNodeUR (int anc, int node);
	public double tallestPathAboveNode (int node, double perUnassignedLength);
	/** returns the node whose distance from the root in branch length is the tallest*/
	public int tallestNode (int node);
	public int tallestNode (int node, double perUnassignedLength);
	/** returns tallest path in number of nodes, not branch length, above a node */
	public int mostStepsAboveNode (int node);
	/** returns total of branchlengths from node up to tallest terminal */
	public double tallestPathAboveNodeUR (int anc, int node, double perUnassignedLength);
	public double distanceToRoot (int node, boolean countUnassigned, double perUnassignedLength);
	public boolean getSelected(int node);
	public boolean anySelected();
	/** Returns the number of nodes selected in the clade */
	public int numberSelectedInClade( int node);
	/** Returns whether there are any selected nodes in the clade */
	public boolean anySelectedInClade(int node);
	/** Gets the first node selected in clade of node node */
	public int getFirstSelected(int node);
	public int getNumberAssociatedBits();
	public Bits getAssociatedBits(int index);
	public Bits getWhichAssociatedBits(NameReference nRef);
	public boolean getAssociatedBit(NameReference nRef, int index);
	public int getNumberAssociatedLongs();
	public LongArray getAssociatedLongs(int index);
	public LongArray getWhichAssociatedLong(NameReference nRef);
	public long getAssociatedLong(NameReference nRef, int index);
	public int getNumberAssociatedDoubles();
	public DoubleArray getWhichAssociatedDouble(NameReference nRef);
	public DoubleArray getAssociatedDoubles(int index);
	public double getAssociatedDouble(NameReference nRef, int index);
	public int getNumberAssociatedObjects();
	public ObjectArray getAssociatedObjects(int index);
	public ObjectArray getWhichAssociatedObject(NameReference nRef);
	public Object getAssociatedObject(NameReference nRef, int index);
	public boolean hasName();
	public String writeTree();

	public boolean ancestorHasNameReference(NameReference nameRef, int node);
	public int ancestorWithNameReference(NameReference nameRef, int node);

	/** Returns a simple string describing the tree in standard parenthesis notation (Newick standard), excluding associated information except branch lengths.
	To be used for output to user, not internally (as it may lose information).*/
	public String writeTreeSimpleByNames();
	public String writeTreeSimpleByNumbers();
	public String writeTreeByT0Names(boolean includeBranchLengths);
	public String writeTree(int byWhat);
	public String writeTree(int byWhat, boolean associatedUseComments);
	public String writeTreeSimple(int byWhat, boolean includeAssociated, boolean includeBranchLengths, boolean includeNodeLabels, boolean zeroBased, String delimiter);
	public String writeClade(int node, int byWhat, boolean associatedUseComments);
	/** Returns a tree description using the namer to supply taxon names */
	public String writeTreeWithNamer(TaxonNamer namer, boolean includeBranchLengths, boolean includeAssocAndProperties);
	public boolean hasNodeLabels();
	public String getNodeLabel(int node);
	public boolean nodeHasLabel(int node);
	public void dispose();

	public int getFileIndex();
	public void setFileIndex(int index);
}



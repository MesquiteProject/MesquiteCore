package mesquite.lib;

import mesquite.assoc.lib.*;

public class TreeUtil {
	
	/** Returns true iff the two trees have the same topologies.  The two trees must use the same block of taxa */
	public static boolean identicalTopologies (Tree tree1, Tree tree2, boolean checkBranchLengths) {
		if (tree1==null || tree2==null)
			return false;
		if (tree1.getTaxa().equals(tree2.getTaxa())) {
			return tree1.equalsTopology(tree2, checkBranchLengths);
		}
		return false;
	}
	/** Returns true iff the contained taxa in all containing taxa are monophyletic . */
	public static boolean containedAllMonophyletic (Tree containedTree, Tree containingTree, TaxaAssociation association) {
		if (containedTree==null || containingTree==null || association == null)
			return false;
		Taxa containingTaxa = containingTree.getTaxa();
		
		for (int outer=0;  outer<containingTaxa.getNumTaxa(); outer++) {  
			Bits associates = association.getAssociatesAsBits(containingTaxa, outer);
			if (!containedTree.isClade(associates))
				return false;
		}
		return true;
	}
	
		

	/** Returns true iff the two trees from different taxa blocks have the same topologies; this is used for associated trees in which one is a containing and one a contained tree. *
	public static boolean containedMatchesContaining (Tree containedTree, Tree containingTree, TaxaAssociation association) {
		if (containedTree==null || containingTree==null || association == null)
			return false;
		if (!containedAllMonophyletic(containedTree, containingTree, association))  // check for monophyly of each contained group
				return false;
		Taxa containedTaxa = containedTree.getTaxa();
		Taxa containingTaxa = containingTree.getTaxa();

// now we know all of the associates are monophyletic within a containing branch.  Let's see if the deeper branches match.
		
		
		
		return true;
	}
	/*......................................*/
	
}

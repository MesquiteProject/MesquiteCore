/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.73, July 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
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

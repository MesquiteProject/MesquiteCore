/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.pairwise.lib;

import mesquite.lib.tree.Tree;


/* ----------------------------------------- */
public class TaxaPath {
	int taxon1, taxon2, baseN;
	public TaxaPath () {
		baseN=-1;
		taxon1=-1;
		taxon2=-1;
	}
	public int getBase() {
		return baseN;
	}
	public void setBase(int b) {
		baseN = b;
	}
	public  int gettaxon1() {
		return taxon1;
	}
	public  int  gettaxon2() {
		return taxon2;
	}
	
	public void setTaxon (int taxon, int index) {
		if (index==1)
			taxon1= taxon;
		else 
			taxon2=taxon;
	}
	
	public void setNode(Tree tree, int node) {
		int tNN = tree.taxonNumberOfNode(node);
		if (taxon1==-1)
			taxon1= tNN;
		else if (taxon2==-1)
			taxon2=tNN;
		else {
			if (tree.whichDaughterDescendantOf(node, baseN) == tree.whichDaughterDescendantOf(tree.nodeOfTaxonNumber(taxon2), baseN))
				taxon2 = tNN;  
			else
				taxon1 = tNN;
		}
	}
}


/* Mesquite source code, Treefarm package.  Copyright 1997-2007 W. Maddison, D. Maddison and P. Midford. 
Version 2.0, September 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BranchLengthFromRoot;
/*~~  */


import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/** ======================================================================== */
public class BranchLengthFromRoot extends NForTaxonWithTree {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public void calculateNumbers(Taxa taxa, Tree tree, NumberArray results, MesquiteString resultsString){
		if (tree == null)
			return;
		clearResultAndLastResult(results);
		results.zeroArray();
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			if (tree.taxonInTree(it))
				results.setValue(it, tree.distanceToRoot(tree.nodeOfTaxonNumber(it), false, 0.0));
		}
		saveLastResult(results);
		saveLastResultString(resultsString);
	}
	/*.................................................................................................................*/
	public String getVeryShortName() {
		return "Branch Length from Root";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Branch Length from Root";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "For each taxon calculates the sum of the branch lengths from that taxon to the root of the tree.";
	}

}


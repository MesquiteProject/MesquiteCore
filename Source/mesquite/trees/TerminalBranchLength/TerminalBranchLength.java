/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.TerminalBranchLength;
/*~~  */


import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.treefarm.lib.*;

/** ======================================================================== */
public class TerminalBranchLength extends NForTaxonWithTree {
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
			if (tree.taxonInTree(it)) {
				int node = tree.nodeOfTaxonNumber(it);
				double terminalBranchLength = tree.getBranchLength(node);
    			if (tree.daughterOf(node, tree.getRoot()) && (tree.numberOfDaughtersOfNode(tree.getRoot())<=2) && (tree.numberOfTerminalsInClade(tree.getRoot()) >2)) {  // have to add in other branch
     		       double otherLength = tree.getBranchLength(tree.nextSisterOfNode(node));
     		        if (MesquiteDouble.isCombinable(otherLength))
     		        	terminalBranchLength+= otherLength;
     			}
				results.setValue(it, terminalBranchLength);
			}
		}
		saveLastResult(results);
		saveLastResultString(resultsString);
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 370;  
	}
	/*.................................................................................................................*/
	public String getVeryShortName() {
		return "Terminal Branch Length";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Terminal Branch Length";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "For each taxon, calculates the length of the terminal branch on the tree, if present.  If a taxon is a descendant of a dichotomous root in a tree with more than 2 taxa, then the length of the other descendant is also added in. .";
	}

}


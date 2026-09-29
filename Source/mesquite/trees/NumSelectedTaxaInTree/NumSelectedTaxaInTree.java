/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

 */

package mesquite.trees.NumSelectedTaxaInTree;


import mesquite.lib.Bits;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeUtil;

/* ======================================================================== */
public class NumSelectedTaxaInTree extends NumberForTree {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
	}

	/*-----------------------------------------*/
	int countSelectedTaxa(Tree tree,  int node, Taxa taxa) {
		if (tree.nodeIsTerminal(node)){
			if (taxa.isSelected(tree.taxonNumberOfNode(node)))
				return 1;
			else
				return 0;
		}
		int count = 0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			count += countSelectedTaxa(tree, d, taxa);
		return count;
	}

	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || tree==null)
			return;
		clearResultAndLastResult(result);

		result.setValue(countSelectedTaxa(tree, tree.getRoot(), tree.getTaxa()));
		if (resultString!=null)
			resultString.setValue("Number of selected taxa in tree: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public String getVeryShortName() {
		return "Num. Selected Taxa";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Number of Selected Taxa in Tree";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Counts the number of selected taxa in the tree.";
	}
}

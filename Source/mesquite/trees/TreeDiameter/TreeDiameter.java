/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

created:
25 May 08 (DRM)
 */

package mesquite.trees.TreeDiameter;


import mesquite.lib.DoubleArray;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeUtil;

/* ======================================================================== */
public class TreeDiameter extends NumberForTree {
	double[] pathLengths = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
	}
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || tree==null)
			return;
		clearResultAndLastResult(result);
		if (tree.hasBranchLengths()) {
			double diam = TreeUtil.getTreeDiameter(tree);
				result.setValue(diam);
		}
		else
			result.setValue(0);
		if (resultString!=null)
			resultString.setValue("Diameter: "+ result.toString());
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
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Tree Diameter";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Calculates the longest path from tip to tip in the tree.";
	}
}

/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.7, August 2009.
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

package mesquite.coalesce.AverageTreeDepth;


import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class AverageTreeDepth extends NumberForTree {
	double[] pathLengths = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public void treeDepth(Tree tree, int node, double depth) {
		if (!tree.branchLengthUnassigned(node) && tree.getRoot()!=node)  //don't count the root
			depth+= tree.getBranchLength(node);
		if (tree.nodeIsTerminal(node)) { 
			pathLengths[node] = depth;
		}
		else {
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				treeDepth(tree, daughter,depth);
			}
		}
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
			pathLengths = new double[tree.getNumNodeSpaces()];
			DoubleArray.deassignArray(pathLengths);
			treeDepth(tree, tree.getRoot(),0.0);
			int count = 0;
			double total = 0.0;
			for (int i=0;i<pathLengths.length; i++){
				if (MesquiteDouble.isCombinable(pathLengths[i])) {
					count++;
					total += pathLengths[i];
				}	
			}
			if (count==0)
				result.setValue(0);
			else
				result.setValue(total/count);
		}
		else
			result.setValue(0);
		if (resultString!=null)
			resultString.setValue("Average Tree Depth: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Average Tree Depth";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Calculates the average path length (in branch length) from terminals to the root, treating unassigned lengths as 0.  The length of the root is not counted.";
	}
}

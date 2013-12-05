/* Mesquite source code.  Copyright 1997-2010 W. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.TreeTotalPathLength;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class TreeTotalPathLength extends NumberForTree {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 
  	 public double totalPath(Tree tree, int node) {
  	 	if (tree.nodeIsTerminal(node)) { 
  	 		if (tree.branchLengthUnassigned(node))
  	 			return 0;
  	 		else
  	 			return tree.getBranchLength(node);
  	 	}
  	 	else {
	  	 	double sum = 0;
		  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			sum += totalPath(tree, daughter);
	  		}
	 	 	if (!tree.branchLengthUnassigned(node))
	  	 			sum += tree.getBranchLength(node);
 	 		return sum;
  		}
  	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){
   	}
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (tree.hasBranchLengths())
			result.setValue(totalPath(tree, tree.getRoot()));
		else
			result.setValue(0);
		if (resultString!=null)
			resultString.setValue("Sum of Branch Lengths: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Sum of Branch Lengths";
   	 }
    		/*.................................................................................................................*/
    		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
    		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
    		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    		public int getVersionOfFirstRelease(){
    			return 200;  
    		}
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Calculates the sum of branch lengths of the tree, treating unassigned lengths as 0.  The length of the root is counted.";
   	 }
}


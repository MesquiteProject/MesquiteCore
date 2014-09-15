/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.TreeDepth;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class TreeDepth extends NumberForTree {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 
  	 public double treeDepth(Tree tree, int node) {
  	 	if (tree.nodeIsTerminal(node)) { 
  	 		if (tree.branchLengthUnassigned(node))
  	 			return 0;
  	 		else
  	 			return tree.getBranchLength(node);
  	 	}
  	 	else {
	  	 	double tallest = 0;
	  	 	double candidate = 0;
	  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			candidate = treeDepth(tree, daughter);
	  			if (candidate > tallest)
	  				tallest = candidate; 
	  		}
  	 		if (tree.branchLengthUnassigned(node) || tree.getRoot()==node)  //don't count the root
  	 			return tallest;
  	 		else
	  			return tallest + tree.getBranchLength(node);
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
		if (tree.hasBranchLengths())
			result.setValue(treeDepth(tree, tree.getRoot()));
		else
			result.setValue(0);
		if (resultString!=null)
			resultString.setValue("Tree Depth: "+ result.toString());
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
		return "Tree Depth";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Calculates the deepest path (in branch length) from terminals to the root, treating unassigned lengths as 0.  The length of the root is not counted.";
   	 }
}


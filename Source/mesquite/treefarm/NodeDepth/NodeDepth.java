/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.NodeDepth;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;



/* ======================================================================== */
public class NodeDepth extends NumbersForNodes {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		return true;
  	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){
    	}
	/*.................................................................................................................*/
	public   void visitNodes(int node, Tree tree, NumberArray result) {
		result.setValue(node, tree.tallestPathAboveNode(node, 0));

		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			visitNodes(d, tree, result);
	}
	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, NumberArray result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (resultString!=null)
			resultString.setValue("");
		if (tree == null )
			return;

		visitNodes(tree.getRoot(), tree, result);
		saveLastResult(result);
		saveLastResultString(resultString);

	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Node Depth";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Calculates the depth of each node from the highest terminal in its clade.";
   	 }
   	public boolean isPrerelease(){
   		return false;
   	}
}


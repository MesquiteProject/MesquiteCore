/* Mesquite source code, Treefarm package.  Copyright 1997-2010 W. Maddison, D. Maddison and P. Midford. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.DeleteReticulations;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class DeleteReticulations extends TreeAltererMult {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
  	 
  	void zap(AdjustableTree tree, int node){
  	 	if (tree.nodeIsInternal(node)) { 
  	 		if (tree.numberOfParentsOfNode(node)>1)
  	 			tree.snipReticulations(node);
	  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			zap(tree, daughter);
	   	 	}
 		}
  	}
   
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
			zap(tree, tree.getRoot());
			if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
			return true;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Delete Reticulations";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Deletes secondary parents of nodes with more than one immediate ancestor.  There is no control over which parents get deleted." ;
   	 }
}


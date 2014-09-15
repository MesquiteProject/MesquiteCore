/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.ResolvePolytomiesToZero;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ResolvePolytomiesToZero extends TreeAltererMult {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
  	 
  	void resolveNode(AdjustableTree tree, int node){
  	 	if (tree.nodeIsInternal(node)) { 
  	 		int numDaughters = 0;
	  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			resolveNode(tree, daughter);
	  			numDaughters++;
	   	 	}
	   	 	if (tree.nodeIsPolytomous(node)){
	  	 		for (int i=0; i<numDaughters-2; i++){
	  	 			int first = tree.firstDaughterOfNode(node);
	  	 			int moving = tree.indexedDaughterOfNode(node, 1);
	  	 			double mLength = tree.getBranchLength(moving);
	  	 			double fLength = tree.getBranchLength(first);
	  	 			tree.moveBranch(moving, first, false);
	  	 			tree.setBranchLength(first, fLength, false);
	  	 			int newNode = tree.firstDaughterOfNode(node);
	  	 			tree.setBranchLength(newNode, 0, false);
	  	 			tree.setBranchLength(moving, mLength, false);
	  	 		}
	  	 	}
 		}
  	}
   
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
			resolveNode(tree, tree.getRoot());
			if (tree instanceof Listened && notify) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
			return true;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Resolve Polytomies (to 0-length branches)";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Resolves polytomies arbitrarily and assigned the resulting new branches zero length." ;
   	 }
}


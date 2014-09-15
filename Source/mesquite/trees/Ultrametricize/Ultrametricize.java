/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.Ultrametricize;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class Ultrametricize extends BranchLengthsAltererMult {
	double resultNum;
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
   
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		tree.arbitrarilyUltrametricize();
		//allOnes(tree, tree.getRoot());
		//ut(tree, tree.getRoot(), tree.tallestPathAboveNode(tree.getRoot(), 0));
		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
		return true;
	}
	/*.................................................................................................................*
   	 private void allOnes(AdjustableTree tree, int node){
		if (!MesquiteDouble.isCombinable(tree.getBranchLength(node))) {
			tree.setBranchLength(node, 1, false);
		}
		for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
			allOnes(tree, daughter);
		}
   	 }
   	 private void ut(AdjustableTree tree, int node, double targetHeight){
		if (tree.nodeIsTerminal(node)) {
			tree.setBranchLength(node, targetHeight, false);
		}
		else {
	   	 	double heightAbove = tree.tallestPathAboveNode(node, 0);
			double nodeLength;
			if (heightAbove==0) 
				nodeLength = targetHeight/2;
			else
				nodeLength = targetHeight-heightAbove;
			if (tree.getRoot()!=node)
				tree.setBranchLength(node, nodeLength, false);
			for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
				ut(tree, daughter, targetHeight - nodeLength);
			}
		}
   	 	
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Arbitrarily Ultrametricize";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Adjusts a tree's branch lengths so that the distances among terminal taxa are ultrametric (i.e. like a molecular clock, all tips reaching to same level).  This is not done with any sophisticated smoothing algorithm; rather, branches are just stretched until they reach to same level." ;
   	 }
}


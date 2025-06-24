/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.RatioLongestBranches;

import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.tree.Tree;

/** this is a silly little module that can be used as a demonstration for NumberForTree modules */
public class RatioLongestBranches extends NumberForTree {
	MesquiteNumber nt;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		nt= new MesquiteNumber();
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
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return 361;  
 	}
	/*-----------------------------------------*/
 	public void getLongestBranches(Tree tree, int node, MesquiteDouble longest, MesquiteDouble secondLongest) {
 		double length = tree.getBranchLength(node);
 		if (tree.getRoot() != node && MesquiteDouble.isCombinable(length)){
 			double currentLongest = longest.getValue();
 			double currentSecondLongest = secondLongest.getValue();
 			if (!longest.isCombinable() || MesquiteDouble.lessThan(currentLongest, length, 0)){
 				longest.setValue(length);
 				secondLongest.setValue(currentLongest);
 			}
 			else if (!secondLongest.isCombinable() || MesquiteDouble.lessThan(currentSecondLongest, length, 0)){
 				secondLongest.setValue(length);
 			}
 		}
 			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
 				getLongestBranches(tree, d, longest, secondLongest);
 		
 	}
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 if (result==null || tree==null)
    	 		return;
    	clearResultAndLastResult(result);
    	MesquiteDouble longest = new MesquiteDouble();
    	MesquiteDouble secondLongest = new MesquiteDouble();
    	getLongestBranches(tree, tree.getRoot(), longest, secondLongest);
    	if (longest.isCombinable() && secondLongest.isCombinable() && secondLongest.getValue() != 0){
		nt.setValue(longest.getValue()/secondLongest.getValue());
		result.setValue(nt);
		if (resultString!=null)
			resultString.setValue("Ratio: "+ nt.toString());
    	}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
    	 public String getName() {
		return "Ratio of Longest Branches";
   	 }
	/*.................................................................................................................*/
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Gets the ratio of the two greatest branch lengths in a tree." ;
   	 }
   	 
}


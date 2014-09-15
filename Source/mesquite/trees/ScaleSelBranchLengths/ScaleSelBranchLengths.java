/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.ScaleSelBranchLengths;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ScaleSelBranchLengths extends BranchLengthsAlterer {
	double resultNum;
	double scale = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		scale = MesquiteDouble.queryDouble(containerOfModule(), "Scale lengths of selected branches", "Multiply all branch lengths by", 1.0);
		return true;
  	 }
  	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
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
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
  	 		if (MesquiteDouble.isCombinable(scale) && tree instanceof MesquiteTree) {
  	 			if (tree.hasBranchLengths()){
   					((MesquiteTree)tree).doCommand("scaleLengthSelectedBranches", MesquiteDouble.toString(scale), CommandChecker.defaultChecker);
					if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
   					return true;
   				}
   				else discreetAlert( "Branch lengths of tree are all unassigned.  Cannot scale selected branch lengths.");
   			}
   			return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Scale Selected Branch Lengths";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Scale Selected Branch Lengths...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Adjusts lengths of a tree's selected branches by multiplying them by an amount." ;
   	 }
}


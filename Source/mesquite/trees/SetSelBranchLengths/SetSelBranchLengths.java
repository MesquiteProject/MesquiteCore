/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.SetSelBranchLengths;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SetSelBranchLengths extends BranchLengthsAlterer {
	double scale = MesquiteDouble.impossible;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
			MesquiteDouble io = new MesquiteDouble(1.0);
			boolean OK = QueryDialogs.queryDouble(containerOfModule(), "Set lengths of selected branches", "Set lengths of selected branches to", io);
			if (!OK)
				return false;

 		scale = io.getValue();
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
  	 		if ((MesquiteDouble.impossible != scale) && tree instanceof MesquiteTree) {
   				((MesquiteTree)tree).doCommand("setLengthSelectedBranches", MesquiteDouble.toString(scale), CommandChecker.defaultChecker);
				if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
				return true;
			}
			return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Assign Selected Branch Lengths";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Assign Selected Branch Lengths...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Assigns a value for branch length for all of a tree's selected branches." ;
   	 }
}


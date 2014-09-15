/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.AlterLengths;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class AlterLengths extends TreeListUtility { 
    	 public String getName() {
		return "Alter Branch Lengths";
   	 }
   	 public String getExplanation() {
		return "Alters branch lengths of selected trees.";
   	 }
 	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(BranchLengthsAltererMult.class, getName() + "  needs a method to alter branch lengths.",
		"The method to alter branch lengths can be chosen from the Alter Branch Lengths submenu");
	}
	BranchLengthsAltererMult alterTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
 		if (arguments ==null)
			alterTask = (BranchLengthsAltererMult)hireEmployee(BranchLengthsAltererMult.class, "Module to alter branch lengths");
	 	else {
			alterTask = (BranchLengthsAltererMult)hireNamedEmployee(BranchLengthsAltererMult.class, arguments);
 			if (alterTask == null)
				alterTask = (BranchLengthsAltererMult)hireEmployee(BranchLengthsAltererMult.class, "Module to alter branch lengths");
 		}
 		if (alterTask == null) {
 			return sorry(getName() + " couldn't start because no branch lengths alterer was obtained.");
 		}
		return true;
	}
	
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return BranchLengthsAltererMult.class;
	}
	
   	/** if returns true, then requests to remain on even after operateData is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean isPrerelease(){
   		return false;
   	}
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	/** Called to operate on the data in all cells.  Returns true if data altered*/
   	public boolean operateOnTrees(TreeVector trees){
   		if (trees == null)
   			return false;
   		Taxa taxa = trees.getTaxa();
   		int numTrees = trees.size();
   		boolean doAll = !trees.anySelected();
   		log("\nAltering Branch Lengths ");
   		int dotFreq = 1;
   		if (numTrees >100 && numTrees<500)
   			dotFreq=5;
   		else if (numTrees>500)
   			dotFreq=10;


		
		for (int j=0; j<numTrees; j++){
			if (doAll || trees.getSelected(j)){
				Tree tree = trees.getTree(j);
				if (tree!=null && tree instanceof MesquiteTree) {
					CommandRecord.tick("Altering tree " + j + " of " + numTrees);
			   		if (j % dotFreq == 0)
			   			log(".");
 					boolean success = alterTask.transformTree((MesquiteTree)tree, null, false);
 				}
			}
		}
			trees.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
			
		
		return true;
		
	}
}


	



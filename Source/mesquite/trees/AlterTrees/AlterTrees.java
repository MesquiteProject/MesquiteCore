/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.AlterTrees;
/*~~  */

import mesquite.lib.CommandRecord;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.duties.TreeAltererMult;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lists.lib.TreeListUtility;

/* ======================================================================== */
public class AlterTrees extends TreeListUtility { 
    	 public String getName() {
		return "Alter Trees";
   	 }
   	 public String getExplanation() {
		return "Alters selected trees.";
   	 }
 	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeAltererMult.class, getName() + "  needs a method to alter trees.",
		"The method to alter trees is selected initially.");
	}
	/*.................................................................................................................*/
	TreeAltererMult alterTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
 		if (arguments ==null)
			alterTask = (TreeAltererMult)hireEmployee(TreeAltererMult.class, "Module to alter trees");
	 	else {
			alterTask = (TreeAltererMult)hireNamedEmployee(TreeAltererMult.class, arguments);
 			if (alterTask == null)
				alterTask = (TreeAltererMult)hireEmployee(TreeAltererMult.class, "Module to alter trees");
 		}
 		if (alterTask == null) {
 			return sorry(getName() + " couldn't start because no tree alterer was obtained.");
 		}
		return true;
	}
	
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return TreeAltererMult.class;
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
   	
   	boolean firstTime = true;
   	/** Called to operate on the data in all cells.  Returns true if data altered*/
   	public boolean operateOnTrees(TreeVector trees){
   		if (trees == null)
   			return false;
   		Taxa taxa = trees.getTaxa();
   		int numTrees = trees.size();
   		boolean doAll = !trees.anySelected();
   		log("\nAltering Trees ");
   		int dotFreq = 1;
   		if (numTrees >100 && numTrees<500)
   			dotFreq=5;
   		else if (numTrees>500)
   			dotFreq=10;
   		firstTime = true;
		for (int j=0; j<numTrees; j++){
			if (doAll || trees.getSelected(j)){
				Tree tree = trees.getTree(j);

				if (tree!=null && tree instanceof MesquiteTree)  {
					CommandRecord.tick("Altering tree " + j + " of " + numTrees);
			   		if (j % dotFreq == 0)
			   			log(".");
					boolean success = alterTask.transformTree((MesquiteTree)tree, null, true); 
					firstTime = false;
 				}
			}
		}
		logln("");
		trees.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
		return true;
		
	}
   	
	public boolean okToInteractWithUser(int howImportant, String messageToUser){
		return firstTime;
	}

}


	



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.SetAssumptionSoft;

import mesquite.lib.Listened;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.duties.TreeAltererMult;
import mesquite.lib.tree.AdjustableTree;

/* ======================================================================== */
public class SetAssumptionSoft extends TreeAltererMult {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
   
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree!=null) {
			tree.setPolytomiesAssumption(1, false);
			if (tree instanceof Listened && notify) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Set Polytomy Assumption to Soft";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Sets assumption that any polytomies in the tree are soft." ;
   	 }
}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

/* December 2003  D.R. Maddison */

package mesquite.treefarm.RemoveSelectedNodes;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class RemoveSelectedNodes extends TreeAltererMult {
	boolean unselectedAlreadyWarned = false;
	Taxa currentTaxa = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
	/*.................................................................................................................*/
 	public void endJob() {
  	 	if (currentTaxa != null)
  	 		currentTaxa.removeListener(this);
 		super.endJob();
   	 }

	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj == currentTaxa && code == MesquiteListener.SELECTION_CHANGED){
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	
   	
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree==null)
			return false;
		Taxa taxa = tree.getTaxa();
		if (currentTaxa != taxa){
			if (currentTaxa != null)
				currentTaxa.removeListener(this);
			currentTaxa = taxa;
			currentTaxa.addListener(this);
		}
		if (!tree.anySelected()) {
 			if (!unselectedAlreadyWarned)
 				discreetAlert("Before selected nodes can be removed, some must be selected");
 			unselectedAlreadyWarned = true;
			return false;
		}
		boolean changed = false;
		for (int node = 0; node< tree.getNumNodeSpaces(); node++){
			if (tree.nodeExists(node) && tree.getSelected(node) && tree.nodeIsInternal(node)){
				tree.collapseBranch(node, false);
				changed = true;
			}
		}
		if (notify && changed && tree instanceof Listened) {
			((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
		}
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Remove Selected Internal Nodes from Tree";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Removes the selected internal nodes from the tree." ;
   	 }
}


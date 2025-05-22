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

package mesquite.treefarm.CutSelectedTaxa;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.AdjustableTree;

/* ======================================================================== */
public class CutSelectedTaxa extends TreeAltererMult {
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
		return 300;  
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
		if (!taxa.anySelected()) {
 			if (!unselectedAlreadyWarned)
 				discreetAlert("Before taxa can be cut, some taxa must be selected");
 			unselectedAlreadyWarned = true;
			return false;
		}
		boolean changed = false;
		for (int i = 0; i< taxa.getNumTaxa(); i++){
			int node = tree.nodeOfTaxonNumber(i);
			if (tree.nodeExists(node) && taxa.getSelected(i)){
				tree.deleteClade(node, false);
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
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Cut Selected Taxa from Tree";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Cuts the selected taxa from the tree." ;
   	 }
}


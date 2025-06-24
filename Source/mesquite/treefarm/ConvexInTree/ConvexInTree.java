/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.ConvexInTree;

import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.duties.BooleanForTree;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;

/** this is a little module that deterimines whether or not a set of taxa is a clade in a tree */
public class ConvexInTree extends BooleanForTree implements MesquiteListener {
	Taxa currentTaxa = null;
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
   		return true;  
   	}
  	 public void endJob(){
  	 	if (currentTaxa != null)
  	 		currentTaxa.removeListener(this);
  	 	super.endJob();
  	 }
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj == currentTaxa && code == MesquiteListener.SELECTION_CHANGED){
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
	public void calculateBoolean(Tree tree, MesquiteBoolean result, MesquiteString resultString) {
    	 	if (result==null || tree==null)
    	 		return;
		Taxa taxa = tree.getTaxa();
		if (currentTaxa != taxa){
			if (currentTaxa != null)
				currentTaxa.removeListener(this);
			currentTaxa = taxa;
			currentTaxa.addListener(this);
		}
		if (!taxa.anySelected()) {
 			return;
		}
		boolean bt = tree.isConvex(taxa.getSelectedBits());
		result.setValue(bt);
		if (resultString!=null)
			if (bt)
				resultString.setValue("Taxa convex");
			else
				resultString.setValue("Taxa not convex");
	}
	/*.................................................................................................................*/
	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/
	public boolean getUserChooseable(){
		return true;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
    	 public String getName() {
		return "Selected Taxa Convex in Tree";
   	 }
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "Convex?";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Determines if the selected taxa are convex in a tree.  That is, does there exist a rooting of the tree in which the selected taxa form a clade?" ;
   	 }
   	 
}


/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.RemoveAssociated;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.PropertyDisplayRecord;
import mesquite.lib.tree.PropertyRecord;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class RemoveAssociated extends TreeAltererMult {
	Listable[] toRemove = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}


	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree instanceof Associable) {
			MesquiteTree mTree = (MesquiteTree)tree;
			if (!MesquiteThread.isScripting() && okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")){
				PropertyDisplayRecord[] properties = mTree.getPropertyRecords();
				if (properties == null || properties.length == 0)
					return false;
				ListableVector lv = new ListableVector();
				for (int i=0; i<properties.length; i++)
					lv.addElement(properties[i], false);
				toRemove = ListDialog.queryListMultiple(containerOfModule(), "Which Properties to remove?", "Which properties to remove from the branches/nodes?", null, lv, null);
				if (toRemove == null)
					return false;
			}
			if (toRemove != null && toRemove.length>0){
				for (int i=0; i<toRemove.length; i++){
					PropertyRecord pr = (PropertyRecord)toRemove[i];
					if (pr.kind == Associable.BITS)
						mTree.removeAssociatedBits(pr.getNameReference());
					else if (pr.kind == Associable.LONGS)
						mTree.removeAssociatedLongs(pr.getNameReference());
					if (pr.kind == Associable.DOUBLES)
						mTree.removeAssociatedDoubles(pr.getNameReference());
					if (pr.kind == Associable.STRINGS)
						mTree.removeAssociatedStrings(pr.getNameReference());
					if (pr.kind == Associable.OBJECTS)
						mTree.removeAssociatedObjects(pr.getNameReference());
				}
			}
			if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		} //Debugg.println( allow choices of what
		return true;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Remove Properties of Branches/Nodes";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Deletes properties (associated information) from all branches/nodes." ;
	}
}


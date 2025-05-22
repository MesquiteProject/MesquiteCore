/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.AddSelectedTaxaToTree;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.AdjustableTree;

/* ======================================================================== */
public class AddSelectedTaxaToTree extends TreeAltererMult {
	public String getName() {
		return "Add Selected Taxa To Tree";
	}
	public String getExplanation() {
		return "To a tree with some taxa excluded, adds those taxa that are selected." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
	boolean unselectedAlreadyWarned = false;
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree==null)
			return false;
		Taxa taxa = tree.getTaxa();
		if (!taxa.anySelected()) {
 			if (!unselectedAlreadyWarned)
 				discreetAlert("You must select taxa to indicate which ones are to be added to the tree or trees");
 			unselectedAlreadyWarned = true;
			return false;
		}
		boolean changed = false;
		for (int i = 0; i< taxa.getNumTaxa(); i++){
			int node = tree.nodeOfTaxonNumber(i);
			if (!tree.nodeExists(node) && taxa.getSelected(i)){
				tree.graftTaxon(i, tree.getRoot(), false);
				changed = true;
			}
		}
		if (notify && changed && tree instanceof Listened) {
			((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
		}
		return true;
	}
}


/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

/* December 2003  D.R. Maddison */

package mesquite.treefarm.OutgroupRoot;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class OutgroupRoot extends TreeAltererMult {
	int warnings = 0;
	int treeNumber = 0;
	boolean unselectedAlreadyWarned = false;
	static final int warningsLimit = 10;
	String notRooted = "";
	Taxa currentTaxa = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
	/*.................................................................................................................*/
 	public void endJob() {
 		if (warnings>warningsLimit) 
 			logln("  (In addition, among the chosen trees,  numbers " + notRooted + " among others could not be rerooted)");
 		else if (warnings>1)
 			logln("  (In addition, among the chosen trees,  numbers " + notRooted + " could not be rerooted)");
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
 				discreetAlert("Before trees can be rerooted using the selected taxa as outgroups, some taxa must be selected");
 			unselectedAlreadyWarned = true;
			return false;
		}
		treeNumber ++;
		MesquiteInteger descendantNode = new MesquiteInteger(-1);
		tree.setRooted(true, notify);
		boolean isconvex = tree.isConvex(taxa.getSelectedBits(), descendantNode);
		if (isconvex && descendantNode.getValue() >=0) {
			double oldBL = tree.getBranchLength(descendantNode.getValue());
			if (tree.reroot(descendantNode.getValue(), tree.getRoot(), false)) {
				if (MesquiteDouble.isCombinable(oldBL)) {
					int node = tree.getRoot();
					int rootDescendants = tree.numberOfDaughtersOfNode(node);
					double newBL = oldBL/rootDescendants;
					for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
	   					tree.setBranchLength(daughter, newBL, notify);
					}
				}
			}
			if (resultString != null)
				resultString.setValue("Tree rerooted");
		}
		else {
			String w = "Tree " + treeNumber + " among chosen trees could not be rerooted between the selected and unselected taxa, as the unselected taxa cannot be made monophyletic";
			if (resultString != null)
				resultString.setValue(w);
			warnings++;
			if (warnings<2)
				logln(w);
			else if (warnings<=warningsLimit)
				notRooted += " " + treeNumber;
			return false;

		}
		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Root tree with selected taxa as outgroup";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Roots the tree between the selected taxa and the remainder, if possible." ;
   	 }
 	public String getKeywords(){
 		return "reroots";
 	}
}


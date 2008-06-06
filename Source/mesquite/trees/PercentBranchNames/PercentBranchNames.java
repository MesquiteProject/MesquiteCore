/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.PercentBranchNames;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class PercentBranchNames extends BranchNamesAltererMult {
	double resultNum;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
		return true;
	}

	/*-----------------------------------------*/
	/** Sets the branch length of nodes (stored as a double internally).*/
	private void convertLabelsToPercentages(AdjustableTree tree, int node) { 
		if (tree.nodeIsInternal(node))
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				convertLabelsToPercentages(tree, daughter);
		String label = tree.getNodeLabel(node);
		double labelNumber = MesquiteDouble.fromString(label);
		if (MesquiteDouble.isCombinable(labelNumber)) {  // it is a double
			if (labelNumber >=0.0 && labelNumber <= 1.0) {
				labelNumber = labelNumber*100;
				long newLabelNumber = Math.round(labelNumber);
				tree.setNodeLabel("" + newLabelNumber, node);
			}
		}
	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		convertLabelsToPercentages(tree, tree.getRoot());
		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Convert Node Names to Percentages";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Convert Names to Percentages";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "If the names of nodes are numbers between 0 and 1, multiplies the number by 100 and converts to an integer." ;
	}
}

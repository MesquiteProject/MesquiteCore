/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.SetAllLineageWidths;

import mesquite.lib.Listened;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.NameReference;
import mesquite.lib.Notification;
import mesquite.lib.duties.TreeAltererMult;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.ui.QueryDialogs;

/* ======================================================================== */
public class SetAllLineageWidths extends TreeAltererMult {
	double resultNum;
	double scale = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		MesquiteDouble io = new MesquiteDouble(1.0);
		boolean OK = QueryDialogs.queryDouble(containerOfModule(), "Set lineage widths", "Set all lineage widths to", io);
		if (!OK)
			return false;

		scale = io.getValue();
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
 	/*-----------------------------------------*/
 	NameReference widthNameReference = NameReference.getNameReference("width");
	/*.................................................................................................................*/
 	private   void setLineageWidths(MesquiteTree tree, int node, double w) {
 		tree.setAssociatedDouble(widthNameReference, node, w);
 		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
 			setLineageWidths(tree, d, w);
 	}
 	/*.................................................................................................................*/
 	public   void setAllWidths(MesquiteTree tree, double w) {
 			if (((MesquiteTree)tree).getAssociatedDoubles(widthNameReference)==null)
 				((MesquiteTree)tree).makeAssociatedDoubles("width");
 			setLineageWidths((MesquiteTree)tree, tree.getRoot(), w);
 	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
	 		if ((MesquiteDouble.impossible != scale) && tree instanceof MesquiteTree) {
	 			setAllWidths((MesquiteTree)tree, scale);
				if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
				return true;
			}
			return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Set All Lineage Widths";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Set All Lineage Widths...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Assigns a value for lineage width for all of a tree's branches." ;
   	 }
}


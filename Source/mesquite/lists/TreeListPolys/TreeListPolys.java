/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TreeListPolys;
/*~~  */

import mesquite.lib.Notification;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lists.lib.TreeListAssistant;

/* ======================================================================== */
public class TreeListPolys extends TreeListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Polytomies Present";
	}
	public String getExplanation() {
		return "Indicates whether polytomies are present." ;
	}
	/*.................................................................................................................*/
	TreeVector treesBlock;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public void setTableAndTreeBlock(MesquiteTable table, TreeVector trees){
		treesBlock = trees;
	}
	public String getTitle() {
		return "Polytomies?";
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == treesBlock)
			treesBlock=null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	public String getStringForTree(int ic){
		if (treesBlock==null)
			return "";
		Tree tree = treesBlock.getTree(ic);
		if (tree ==null)
			return "";
		if (tree.hasPolytomies(tree.getRoot()))
			return "Yes";
		else
			return "No";
	}
	public String getWidestString(){
		return " Polytomies? ";
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
}


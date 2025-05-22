/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.TreeNameDeleteSuffix;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class TreeNameDeleteSuffix extends TreeListUtility { 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
   	/** if returns true, then requests to remain on even after operateData is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean isPrerelease(){
   		return false;
   	}
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	
   	static String rememberedSuffix = "";
   	/** Called to operate on the data in all cells.  Returns true if data altered*/
   	public boolean operateOnTrees(TreeVector trees){
   		if (trees == null)
   			return false;

   		boolean noneSelected = !trees.anySelected();

		int treeCount = 0;
    	 
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Asking for suffix")) 
			rememberedSuffix = MesquiteString.queryString(containerOfModule(), "Suffix to delete" , "Suffix to delete from tree names: ", rememberedSuffix);
		
		if (StringUtil.blank(rememberedSuffix))
			return false;

		String suffix = rememberedSuffix;
		for (int j=0; j<trees.size(); j++){
			if (noneSelected || trees.getSelected(j)){
				Tree tree = trees.getTree(j);
				if (tree!=null || !(tree instanceof MesquiteTree)) {
					String name = tree.getName();
					if (!StringUtil.blank(name) && name.endsWith(suffix)) {
						name = name.substring(0, name.length()-suffix.length());
						((MesquiteTree)tree).setName(name);
						treeCount++;
					}
				}
			}
		}
		logln("Suffix removed from " + treeCount + " tree(s).");
		trees.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));	
		return true;
		
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Delete suffix from tree names...";
	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Delete suffix from tree names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Deletes a specified suffix from the tree names.";
   	 }
}


	



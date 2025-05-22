/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.SelectLinkedMatricesOfSelectedTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class SelectLinkedMatricesOfSelectedTrees extends TreeListUtility { 
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
   	
   	/** Called to operate on the data in all cells.  Returns true if data altered*/
   	public boolean operateOnTrees(TreeVector trees){
   		if (trees == null || !trees.anySelected())
   			return false;
   		boolean sel = false;
		ListableVector datas = getProject().getCharacterMatrices();
		for (int j=0; j<trees.size(); j++){
			if (trees.getSelected(j)){
				Tree tree = trees.getTree(j);
				CharacterData linkedMatrix = ((MesquiteTree)tree).findLinkedMatrix(getProject());
				if (linkedMatrix != null){
					int iM = datas.indexOf(linkedMatrix);
					datas.setSelected(iM, true);
					sel = true;
				}
			}
		}
		if (sel)
			datas.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));	
		return true;
		
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Select Matrices Linked to Selected Trees";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Selects matrices that are linked to the selected trees.";
   	 }
}


	



/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.SelectLinkedTreesOfSelectedMatrices;
/*~~  */

import mesquite.lib.Debugg;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.Notification;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lists.lib.CharMatricesListUtility;
import mesquite.lists.lib.TreeListUtility;

/* ======================================================================== */
public class SelectLinkedTreesOfSelectedMatrices extends CharMatricesListUtility { 
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
		return true;
	}
	public boolean requestPrimaryChoice(){
		return false;  
	}

	/** Called to operate on the data in all cells.  Returns true if data altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		if (datas == null)
			return false;
		ListableVector treeVectors = getProject().getTreeVectors();

		for (int j=0; j<treeVectors.size(); j++){
			boolean sel = false;
			TreeVector trees = (TreeVector)treeVectors.elementAt(j);
			Debugg.errln("@findin for tree vector " + j);
			//now we have this tree vector. Let's go through the matrices to see which ones might have matches among the trees, and select those trees
			for (int k = 0; k< datas.size(); k++){
				CharacterData dataK = (CharacterData)datas.elementAt(k);
				Debugg.errln("@findin for matrix " + k);
				if (trees.getTaxa() == dataK.getTaxa()){
					for (int itr = 0; itr<trees.size(); itr++){
						MesquiteTree tree = (MesquiteTree) trees.getTree(itr);
						if (tree.isMatrixLinked(dataK)){
							trees.setSelected(itr, true);
							sel = true;
						}
					}
				}
			}
			if (sel)
				trees.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));	
		}
		return true;

	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Select Trees Linked to Selected Matrices";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Selects trees that are linked to the selected matrices.";
	}
}






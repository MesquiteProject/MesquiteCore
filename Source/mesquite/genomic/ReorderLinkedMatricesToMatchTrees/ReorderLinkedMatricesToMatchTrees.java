/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ReorderLinkedMatricesToMatchTrees;
/*~~  */

import java.util.Vector;

import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lists.lib.TreeListUtility;
import mesquite.lib.characters.CharacterData;

/* ======================================================================== */
public class ReorderLinkedMatricesToMatchTrees extends TreeListUtility { 
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
		return true;  
	}
	/** Called to operate on the data in all cells.  Returns true if data altered*/
	public boolean operateOnTrees(TreeVector trees){
		if (trees == null)
			return false;

		Vector v = pauseAllPausables();
		// go through trees. If find match then move that matrix into next place
		ListableVector datas = getProject().getCharacterMatrices();
		int currentPlace = 0;
		for (int itree = 0; itree < trees.size(); itree++){
			MesquiteTree tree = (MesquiteTree)trees.getTree(itree);
			for (int im = 0; im < datas.size(); im++){
				CharacterData data = (CharacterData)datas.elementAt(im);
				if (tree.isMatrixLinked(data)){
					datas.removeElement(data,false);
					datas.insertElementAt(data, currentPlace++, false);
				}
			}
		}
		datas.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
		unpauseAllPausables(v);
		return true;

	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Reorder linked matrices to sequence of trees";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Reorder linked matrices to sequence of trees";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Reorders any linked matrices to match sequence of trees.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
}






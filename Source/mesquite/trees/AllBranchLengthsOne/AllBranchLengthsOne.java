/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.AllBranchLengthsOne;

import mesquite.lib.Listened;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.duties.BranchLengthsAltererMult;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;

/* ======================================================================== */
public class AllBranchLengthsOne extends BranchLengthsAltererMult {
	/*.................................................................................................................*/
	public String getName() {
		return "All Branch Lengths to 1.0";
	}
	public String getExplanation() {
		return "Assigns a value of 1.0 for branch length for all of a tree's branches." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree instanceof MesquiteTree) {
			((MesquiteTree)tree).setAllBranchLengths(1.0, false);
			if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));

			return true;
		}
		return false;

	}
}


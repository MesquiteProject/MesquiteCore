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
/*Oliver May 2008*/

package mesquite.trees.SetBranchLengthsIfLessThan;

import mesquite.lib.*;
import mesquite.lib.duties.*;

public class SetBranchLengthsIfLessThan extends BranchLengthsAltererMult {
	MesquiteDouble assignLength;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		assignLength = new MesquiteDouble(0.0000001);
		if(!MesquiteThread.isScripting()){
			double tempL = MesquiteDouble.queryDouble(containerOfModule(), "Set Length", "Enter length to assign to branches with lengths less than or equal to zero:", assignLength.getValue());
			if(MesquiteDouble.isCombinable(tempL))
				assignLength.setValue(tempL);
		}
		return true;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
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
	public boolean requestPrimaryChoice(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		setLengths(tree, tree.getRoot());
		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
			return true;
	}
	/*.................................................................................................................*/
	private void setLengths(AdjustableTree tree, int node){
		if(tree.getBranchLength(node) <= 0 && MesquiteDouble.isCombinable(tree.getBranchLength(node))){
			tree.setBranchLength(node, assignLength.getValue(), false);
		}
		for(int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)){
			setLengths(tree, daughter);
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Assign Branch Lengths Less than or Equal to Zero...";
	}
 	/*.................................................................................................................*/
	public String getExplanation(){
		return "Assigns a user-provided value to all branches with lengths less than or equal to zero.";
	}
}
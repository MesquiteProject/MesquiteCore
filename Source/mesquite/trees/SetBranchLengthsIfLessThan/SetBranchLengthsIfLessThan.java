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
	public boolean isSubstantive(){
  	 	return false;
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
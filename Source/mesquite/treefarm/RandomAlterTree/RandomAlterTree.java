/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/


package mesquite.treefarm.RandomAlterTree;

import mesquite.lib.Debugg;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.RandomBetween;
import mesquite.lib.duties.TreeAltererMult;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;
import mesquite.treefarm.lib.RndTreeModifier;

/* ======================================================================== */
public class RandomAlterTree extends TreeAltererMult {
	RndTreeModifier randomModifierTask;
	RandomBetween rng = new RandomBetween();
	long originalSeed=System.currentTimeMillis(); //0L;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		randomModifierTask = (RndTreeModifier)hireEmployee(RndTreeModifier.class, "Random modifier of tree");
		if (randomModifierTask == null)
			return sorry(getName() + " couldn't start because a random modifier was not found.");
   		rng.setSeed(originalSeed);
		return true;
  	 }
	/*.................................................................................................................*/
 	public void endJob() {
 		super.endJob();
   	 }

	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree==null)
			return false;
		randomModifierTask.modifyTree(tree, (MesquiteTree)tree, rng);
		if (tree instanceof Listened && notify) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true; //note in History
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Randomly modify tree";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Randomly modifies existing trees." ;
   	 }
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}


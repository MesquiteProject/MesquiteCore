/* Mesquite source code, Treefarm package.  Copyright 1997-2011 W. Maddison, D. Maddison and P. Midford. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.BLfromDivergenceTimes;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.*;



/* ======================================================================== */
public class BLfromDivergenceTimes extends BranchLengthsAltererMult {
	/*.................................................................................................................*/
	public String getName() {
		return "Branch Lengths from current, reinterpreted as Divergence Times";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Reassigns branch lengths under the assumption that currently assigned branch lengths are actually divergence times.";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
	}
	/*.................................................................................................................*/
	public double visitNodes(int node, AdjustableTree tree) {
		double depth = tree.getBranchLength(node);
		if (tree.nodeIsTerminal(node)){
			if (!MesquiteDouble.isCombinable(depth))
				depth = 0;
			return depth;
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			double daughterDepth = visitNodes(d, tree);
			if (MesquiteDouble.isCombinable(daughterDepth) && MesquiteDouble.isCombinable(depth))
				tree.setBranchLength(d, depth-daughterDepth, false);
			else
				tree.setBranchLength(d, MesquiteDouble.unassigned, false);
		}
		return depth;
	}
	/*.................................................................................................................*/
	public double cleanInversions(int node, AdjustableTree tree, MesquiteBoolean clean) {
		if (!clean.isUnassigned() && !clean.getValue())
			return 0;
		double depth = tree.getBranchLength(node);
		if (tree.nodeIsTerminal(node)){
			if (!MesquiteDouble.isCombinable(depth))
				depth = 0;
			return depth;
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && (clean.getValue() || clean.isUnassigned()); d = tree.nextSisterOfNode(d)) {
			double daughterDepth = cleanInversions(d, tree, clean);
			boolean 	inverted = MesquiteDouble.isCombinable(daughterDepth) && MesquiteDouble.isCombinable(depth) && daughterDepth>depth;
			boolean missing = MesquiteDouble.isCombinable(daughterDepth) && !MesquiteDouble.isCombinable(depth);

			if (inverted || missing){
					if (clean.isUnassigned()){
						if (!MesquiteThread.isScripting()){
							if (AlertDialog.query(containerOfModule(), "Clean up divergences?", "This tree has inverted divergence times (with a daughter node older than a parent node) or missing divergence times.  Do you want to interpret branch lengths with these problems cleaned up, if possible?", "Clean Up", "Don't Clean Up"))
								clean.setValue(true);
							else
								clean.setValue(false);
						}
					}
					if (!clean.isUnassigned() && !clean.getValue())
						return 0;
					tree.setBranchLength(node, daughterDepth, false);
					depth = daughterDepth;
				}


		}
		return depth;
	}
	/*.................................................................................................................*/
	//Should be overridden; when previous version is deleted in future, this will be abstract.  returns whether successfully transformed.
	public boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree == null)
			return false;
		MesquiteBoolean clean = new MesquiteBoolean(true);
		clean.setToUnassigned();
		
		cleanInversions(tree.getRoot(), tree, clean);

		visitNodes(tree.getRoot(), tree);

		if (notify && tree instanceof Listened)
			((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
		return true;


	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 274;  
	}

	public boolean isPrerelease(){
		return false;
	}
}


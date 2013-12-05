/* Mesquite source code, Treefarm package.  Copyright 1997-2010 W. Maddison, D. Maddison and P. Midford. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.NNAssocValues;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.*;



/* ======================================================================== */
public class NNAssocValues extends BranchLengthsAlterer {
	/*.................................................................................................................*/
	public String getName() {
		return "Obtain Branch Lengths from Values Attached to Nodes";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Assigns branch lengths to the tree taken from values already attached to nodes, for instance support values or divergence times from other programs.";
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
	public   void visitNodes(int node, AdjustableTree tree, NameReference nr) {
		double value = tree.getAssociatedDouble(nr,node);
		if (MesquiteDouble.isCombinable(value))
			tree.setBranchLength(node, value, false);
		else
			tree.setBranchLength(node, MesquiteDouble.unassigned, false);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			visitNodes(d, tree, nr);
	}
	/*.................................................................................................................*/
	//Should be overridden; when previous version is deleted in future, this will be abstract.  returns whether successfully transformed.
	public boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree == null)
			return false;
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedDoubles();
		boolean[] shown = new boolean[num]; //bigger than needed probably
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (da != null)
				v.addElement(new MesquiteString(da.getName(), ""), false);
		}
		if (v.size()==0)
			alert("This Tree has no values attached to nodes");
		else {
			Listable result = ListDialog.queryList(containerOfModule(), "Choose attached value", "Choose attached value to transfer to branch lengths", null, v, 0);
			if (result != null){
				MesquiteString name = (MesquiteString)result;
				String sName = name.getName();
				NameReference nr = NameReference.getNameReference(sName);

				visitNodes(tree.getRoot(), tree, nr);

				if (notify && tree instanceof Listened)
					((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
				return true;
			}

		}

		return false;

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


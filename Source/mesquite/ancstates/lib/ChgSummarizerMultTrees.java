/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ancstates.lib;

import mesquite.lib.MesquiteModule;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;


/* ======================================================================== */
/**.*/

public abstract class ChgSummarizerMultTrees extends MesquiteModule  {
	boolean sensitiveToBranchSelection = false;

   	 public Class getDutyClass() {
   	 	return ChgSummarizerMultTrees.class;
   	 }
 	public String getDutyName() {
 		return "Summarizer of changes over multiple trees";
   	 }
 	/* this is a special duty class with only one intended subclass, designed to be used in one of two ways, to summarize
 	 * changes over trees without reference to a tree with selected branches, the other wiith reference to such a tree.
 	 * If setup() is called, it is assumed the latter is in effect.
 	 * if setTree(tree) is called, it is assumed the former is in effect*/
	public abstract  void setTree(Tree tree);
	public abstract void setup(Taxa taxa, boolean poppedOut);
	public boolean getSensitiveToBranchSelection() {
		return sensitiveToBranchSelection;
	}
	public void setSensitiveToBranchSelection(boolean sensitiveToBranchSelection) {
		this.sensitiveToBranchSelection = sensitiveToBranchSelection;
	}

}




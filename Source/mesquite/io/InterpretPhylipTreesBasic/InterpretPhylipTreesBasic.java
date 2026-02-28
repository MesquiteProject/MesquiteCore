/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretPhylipTreesBasic;
/*~~  */

import mesquite.io.lib.InterpretPhylipTrees;
import mesquite.io.lib.TryNexusFirstTreeFileInterpreter;
import mesquite.lib.MesquiteStringBuffer;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;


/* ============  a file interpreter for Phylip trees ============*/

public class InterpretPhylipTreesBasic extends InterpretPhylipTrees implements TryNexusFirstTreeFileInterpreter{
	/*.................................................................................................................*/
	public String getName() {
		return "Simple Newick/Phylip Treefile";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports and exports Newick trees in the simple tree format used by Phylip and other programs." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	//In this version, does writes only basic NEWICK. Properties not included.
	protected void exportTrees(Taxa taxa, TreeVector treeVector, MesquiteStringBuffer outputBuffer) { 
		Tree tree;
		if (treeVector !=null && treeVector.size()>0) {
			for (int iTree = 0; iTree < treeVector.size(); iTree++) {
				tree = (Tree)treeVector.elementAt(iTree);
				outputBuffer.append(tree.writeTreeSimpleByNames());  //or Tree.BY_NUMBERS  or Tree.BY_NAMES
				// if do it BY_NAMES, make sure you truncate the taxon names to 10 characters!!
				outputBuffer.append(getLineEnding());
			}
		}
	}


}
	


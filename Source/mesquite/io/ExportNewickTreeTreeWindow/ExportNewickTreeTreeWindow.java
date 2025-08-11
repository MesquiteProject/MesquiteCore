/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.ExportNewickTreeTreeWindow;
/*~~  */

import mesquite.io.lib.InterpretPhylipTrees;
import mesquite.io.lib.TryNexusFirstTreeFileInterpreter;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.duties.OneTreeSource;
import mesquite.lib.duties.TreeWindowMaker;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.TreeContext;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ListDialog;


/* ============  a file interpreter for Phylip trees ============*/

public class ExportNewickTreeTreeWindow extends InterpretPhylipTrees {
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		boolean treeContextAvailable = findEmployerWithDuty(TreeContext.class) != null || findNearestColleagueWithDuty(TreeContext.class) != null;
		return treeContextAvailable;
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}
	TreeVector previousVector = null;
	/*.................................................................................................................*/
	public TreeVector findTreesToExport(MesquiteFile file, Taxa taxa, String arguments, boolean usePrevious){
		if (usePrevious && previousVector!= null)
		return previousVector;
		if (taxa == null)
			taxa = getProject().chooseTaxa(containerOfModule(), "Choose taxa whose tree you might to export");
		OneTreeSource treeTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of tree to be exported");
		if (treeTask != null) {
			treeTask.initialize(taxa);
			MesquiteTree origtree = (MesquiteTree)treeTask.getTree(taxa);
			if (origtree == null){
				alert("No tree found");
			}
			else {
				MesquiteTree tree = origtree.cloneTree();
				TreeVector tv = new TreeVector(taxa);
				tv.addElement(tree, false);
				previousVector = tv;
				return tv;
			}
		}

		return null;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Export Tree in Tree Window";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports tree in tree window as a simple Newick tree in a file." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}


}



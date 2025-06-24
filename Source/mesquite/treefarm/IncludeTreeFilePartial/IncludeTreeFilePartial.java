/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.IncludeTreeFilePartial;
/*~~  */

import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.duties.FileAssistantTM;
import mesquite.lib.duties.FileElementManager;
import mesquite.lib.duties.TreeSource;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ExtensibleDialog;


/* ======================================================================== */
public class IncludeTreeFilePartial extends FileAssistantTM {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		includePartialTreeFile();
		return true;
	}
	/*-----------------------------------------------------------------*/
	void includePartialTreeFile(){
		//moved from ManageTrees
		if (!MesquiteThread.isScripting()){  //only non-scripting
			MesquiteInteger buttonPressed = new MesquiteInteger();
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Trees to Include",  buttonPressed);
			IntegerField firstTree = dialog.addIntegerField("First Tree to Read:", 1, 16);
			IntegerField lastTree = dialog.addIntegerField("Last Tree to Read:", MesquiteInteger.infinite, 16);
			IntegerField everyNth = dialog.addIntegerField("Sample Every nth tree:", 1, 16);
			dialog.completeAndShowDialog(true);
			dialog.dispose();
			if (buttonPressed.getValue()!=0) 
				return;
			String arguments = "";

			TreeSource temp = (TreeSource)hireNamedEmployee(TreeSource.class, "#mesquite.trees.ManyTreesFromFile.ManyTreesFromFile");
			if (temp == null)
				discreetAlert( "Sorry, the file could not be read because the module \"Trees Directly from File\" could not be started");
			else {
				int start = 0;
				int last = MesquiteInteger.infinite;
				int every = 1;
				if (MesquiteInteger.isCombinable(firstTree.getValue()) && firstTree.getValue() != 1)
					start = firstTree.getValue() - 1;
				if (MesquiteInteger.isCombinable(lastTree.getValue()))
					last = (lastTree.getValue() - 1);
				if (MesquiteInteger.isCombinable(everyNth.getValue()) && everyNth.getValue() != 1)
					every = (everyNth.getValue());
				Tree first = temp.getTree(null, 0);  //get first tree to figure out taxa block!
				if (first == null) {
					fireEmployee(temp);
					discreetAlert( "Sorry, no tree was obtained");
					return;
				}
				Taxa taxa = first.getTaxa();
				MesquiteFile file = getProject().getHomeFile();
				if (file == null){
					fireEmployee(temp);
					return;
				}
				TreeVector trees = new TreeVector(taxa);
				Tree tree = null;
				for (int i= start; (i < last) && (i == start || tree !=null); i+= every){
					tree = temp.getTree(taxa, i);
					if (tree !=null)
						trees.addElement(tree, false);
				}

				trees.setName(temp.getParameters());
				trees.addToFile(file, getProject(), (FileElementManager)findElementManager(TreeVector.class));

				fireEmployee(temp);
				resetAllMenuBars();
			}
		}

	}

	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return false;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice() { 
		return true;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Include Partial Sample from Tree File...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Partial Sample from Tree File";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Samples trees from a tree file partially, e.g. including only every 100th tree. This is designed to sample a very large tree file, e.g. from a long Bayesian run." ;
	}

}



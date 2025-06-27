/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.RearrangedTree;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.TreeSwapper;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.treefarm.lib.DetTreeModifier;

/* TODO: dichotomize tree before giving to swapper, in case swapper can't handle polytomies?  */
public class RearrangedTree extends DetTreeModifier {
	public String getName() {
		return "Rearranged tree";
	}
	public String getExplanation() {
		return "Supplies trees that are rearrangments of a given tree.  The original tree is NOT included among the rearrangements.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSwapper.class, getName() + "  needs a method to rearrange the tree.",
		"The method to rearrange the tree can be selected initially or in the Branch Rearranger submenu");
	}
	int currentTree=0;
	TreeSwapper swapTask;
	MesquiteString swapName;
	MesquiteCommand stC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		swapTask = (TreeSwapper)hireEmployee(TreeSwapper.class, "Tree Rearranger");
		if (swapTask==null)
			return sorry(getName() + " couldn't start because no tree rearranging module was obtained");
		stC = makeCommand("setSwapper",  this);
		swapTask.setHiringCommand(stC);
		swapName = new MesquiteString();
		swapName.setValue(swapTask.getName());
		if (numModulesAvailable(TreeSwapper.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Branch Rearranger", stC, TreeSwapper.class);
			mss.setSelected(swapName);
		}
		return true;
	}

	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("setSwapper ", swapTask); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the branch swapper", "[name of module]", commandName, "setSwapper")) {
			TreeSwapper temp = (TreeSwapper)replaceEmployee(TreeSwapper.class, arguments, "Branch Rearranger", swapTask);
			if (temp !=null){
				swapTask = temp;
				swapName.setValue(swapTask.getName());
				swapTask.setHiringCommand(stC);
				parametersChanged();
				return swapTask;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void modifyTree(Tree original, MesquiteTree modified, int ic){
		swapTask.rearrange(modified, ic);
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Tree tree) {
		if (tree == null) 
			return 0;
		if (!(tree instanceof AdjustableTree))
			return 0;

		int i = (int)swapTask.numberOfRearrangements((AdjustableTree)tree);
		return i;
	}



}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.SearchForBetterTree;

import java.awt.Button;
import mesquite.trees.lib.*;
import java.awt.Checkbox;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.ui.ExtensibleDialog;


/*  TODO: ask option of showing live updates
 * */

public class SearchForBetterTree extends TreeAlterer {
	int currentTree=0;
	TreeSwapper swapTask;
	NumberForTree numberTask;
	boolean smallerIsBetter = true;
	boolean liveUpdates = true;
	RandomBetween rng = new RandomBetween(System.currentTimeMillis());
	TreeOptimizer treeOptimizer;

	public String getName() {
		return "Search for Better Tree";
	}
	public String getExplanation() {
		return "Finds better trees (by the chosen optimality criterion) by rearranging the current tree.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSwapper.class, getName() + "  needs a method to rearrange the tree.",
		"The method to rearrange the tree can be selected initially or in the Branch Rearranger submenu");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		swapTask = (TreeSwapper)hireEmployee(TreeSwapper.class, "Tree Rearranger");
		if (swapTask==null)
			return sorry(getName() + " couldn't start because no tree rearranging module was obtained");
		numberTask = (NumberForTree)hireEmployee(NumberForTree.class, "Statistic to calculate for tree");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");
		if (!MesquiteThread.isScripting()){
			if (!queryOptions())
				return false;
		}
		treeOptimizer = new TreeOptimizer(this, numberTask, swapTask);
		if (treeOptimizer== null)
			return sorry(getName() + " couldn't start because the tree optimizer could not be created.");
		return true;
	}
	/*.................................................................................................................*/

	public boolean isPrerelease(){
		return false;
	}
	public boolean requestPrimaryChoice(){
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		if (numberTask !=null)
			return numberTask.getCompatibilityTest();
		return null;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		String help = "If you select \"minimize objection function\", then Mesquite will search for trees that have smaller values of the number calculated for each tree; if you turn this option off, Mesquite will look for trees with larger values. ";
		help+="For example, if Treelength were the value calculated for each tree, one typically would search for trees with smaller values; i.e., one would choose \"minimize objection function\".";
		help+= " If \"live updates\" is chosen, then each time a better tree is found, the tree will be redrawn (if it is visible in a tree window) and any calculations based upon the tree,"; 
		help+=" such as tracing of a character history, a tree legend, or a chart, will be redone, which may substantially increase the time taken for the search.";
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Options for Search for Better Tree",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.appendToHelpString(help);
		
		dialog.addLabel("Options for Search for Better Tree");
		
		Checkbox smallerIsBetterBox = dialog.addCheckBox("minimize objection function", smallerIsBetter);
		Checkbox liveUpdatesBox = dialog.addCheckBox("update display and calculations based upon tree as it is rearranged", liveUpdates);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			smallerIsBetter = smallerIsBetterBox.getState();
			liveUpdates = liveUpdatesBox.getState();
			//storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("setSwapper ", swapTask); 
		return temp;
	}
	/*.................................................................................................................*
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
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (treeOptimizer ==null)
			return false;
		treeOptimizer.setLiveUpdates(liveUpdates);
		treeOptimizer.setBiggerIsBetter(!smallerIsBetter);
		treeOptimizer.setNotify(notify);
		return treeOptimizer.searchForBetterTree(tree,  tree.getRoot(),  rng,  resultString);
		//return TreeSearchUtil.searchForBetterTree(this,  tree,  tree.getRoot(), swapTask,  numberTask,  rng,  resultString,  smallerIsBetter,  liveUpdates,  notify);
	//	return TreeSearchUtil.searchForBetterTree(this,  tree,  tree.getRoot(),30000.0, false, false, swapTask,  numberTask,  rng,  resultString,  smallerIsBetter,  liveUpdates,  notify, true, false);
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 201;  
	}

	
}

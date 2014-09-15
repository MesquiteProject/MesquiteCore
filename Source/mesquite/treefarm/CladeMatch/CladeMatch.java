/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.CladeMatch;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;



/* ======================================================================== */
public class CladeMatch extends NumbersForNodes {
	public String getExplanation() {
		return "Calculates a number relating each clade in the tree to another current tree (e.g., a tree-to-tree distance metric).";
	}
	public String getName() {
		return "Clade Match with Another Current Tree";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a source of a comparison tree.",
		"The source of comparison tree is arranged initially");
		EmployeeNeed e2 = registerEmployeeNeed(NumberFor2Trees.class, getName() + "  needs a method to compare trees.",
		"The method to compare trees can be chosen initially or in the Tree-Tree Value submenu");
	}
	Tree otherTree = null;
	OneTreeSource otherTreeTask;
	NumberFor2Trees treeDifferenceTask;		// module that calculates inter-tree distances
	MesquiteString treeDifferenceTaskName;	// String for use in subment for distance metric
	MesquiteCommand tdC;					// Command to react to menu choice of tree difference metric
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		otherTreeTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of tree for comparison");
		if (otherTreeTask == null) {
			return sorry(getName() + " couldn't start because no source of a comparison tree was obtained.");
		}
		treeDifferenceTask = (NumberFor2Trees)hireEmployee(NumberFor2Trees.class, "Value to calculate between tree and other tree");
		if (treeDifferenceTask == null) {
			return sorry(getName() + " couldn't start because no module to calculate a number for 2 trees was obtained.");
		}
		tdC = makeCommand("setTreeDifferenceTask",  this);
		treeDifferenceTask.setHiringCommand(tdC);
		treeDifferenceTaskName = new MesquiteString(treeDifferenceTask.getName());
		if (numModulesAvailable(NumberFor2Trees.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree-Tree Value", tdC, NumberFor2Trees.class);
			mss.setSelected(treeDifferenceTaskName);
		}
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setTreeSource ", otherTreeTask); 
		temp.addLine("setTreeDifferenceTask ", treeDifferenceTask);
		return temp;
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the tree-to-tree value calculator", "[name of module]", commandName, "setTreeDifferenceTask")) {
			NumberFor2Trees temp = (NumberFor2Trees)replaceEmployee(NumberFor2Trees.class, arguments, "Tree-to-tree value", treeDifferenceTask);
			if (temp != null) {
				treeDifferenceTask = temp;
				treeDifferenceTask.setHiringCommand(tdC);
				treeDifferenceTaskName.setValue(treeDifferenceTask.getName());
				parametersChanged();
			}
			return treeDifferenceTask;
		}
		else if (checker.compare(this.getClass(), "Sets the source of the comparison tree", "[name of module]", commandName, "setTreeSource")) {
			OneTreeSource temp = (OneTreeSource)replaceEmployee(OneTreeSource.class, arguments, "Source of other tree", otherTreeTask);
			if (temp !=null){
				otherTreeTask = temp;
				parametersChanged();
				return otherTreeTask;
			}
		}
		else 
			 super.doCommand(commandName, arguments, checker);
		return null;
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		if (otherTreeTask!=null)
			otherTreeTask.initialize(tree.getTaxa());
		if (treeDifferenceTask!=null)
			treeDifferenceTask.initialize(tree, otherTree);
	}
	/*.................................................................................................................*/
	MesquiteTree tempTree;
	public   void visitNodes(int node, Tree tree, Tree otherTree, NumberArray result) {
		if (tree.nodeIsInternal(node)){// && tree.numberOfTerminalsInClade(node)>3){
			tempTree.readTree(tree.writeClade(node, MesquiteTree.BY_NUMBERS, false));
			MesquiteNumber mN = new MesquiteNumber();
			mN.setToUnassigned();
			treeDifferenceTask.calculateNumber(tempTree, otherTree, mN, null);
			result.setValue(node, mN);

		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			visitNodes(d, tree, otherTree, result);
	}
	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, NumberArray result, MesquiteString resultString) {
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (resultString!=null)
			resultString.setValue("");
		if (tree == null || otherTreeTask==null)
			return;

		otherTree = otherTreeTask.getTree(tree.getTaxa());
		if (otherTree == null||treeDifferenceTask == null)
			return;
		tempTree = new MesquiteTree(tree.getTaxa());
		visitNodes(tree.getRoot(), tree, otherTree, result);
		saveLastResult(result);
		saveLastResultString(resultString);

	}
	/*.................................................................................................................*/
	public String getParameters() {
		if (treeDifferenceTask ==null)
			return null;
		String s = "Comparison: " + treeDifferenceTask.getName(); 
		if (otherTree !=null)
			s += "; comparison tree: " + otherTree;
		return s;
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
		if (treeDifferenceTask ==null)
			return getName();
		String s = treeDifferenceTask.getName(); 
		if (otherTree !=null)
			s += "; comparison tree: " + otherTree;
		return s; 
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
}


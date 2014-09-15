/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForCharOnTree;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumForCharOnTree extends NumberForCharacter implements NumberForCharacterIncr, NumForCharTreeDep {
	public String getName() {
		return "Character value with tree";
	}
	public String getNameForMenuItem() {
		return "Character value with tree...";
	}
	public String getVeryShortName() {
		if (numberTask ==null)
			return "Character value with tree";
		else
			return numberTask.getVeryShortName() + " (with tree)";
	}
	public String getExplanation() {
		return "Coordinates the calculation of a number for a character based on a tree." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForCharAndTree.class, getName() + "  needs a method to calculate the value for a character using the trees.",
		"The method to calculate values can be selected initially or in the Values submenu");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(TreeSource.class, getName() + "  needs a source for trees.",
		"The source for trees can be requested initially or using the Tree Source submenu");
		//e2.setDivertChainMessage("testing");
	}
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return NumberForCharAndTree.class;
	}
	/*.................................................................................................................*/
	NumberForCharAndTree numberTask;
	TreeSource treeTask;
	Taxa taxa;
	Tree tree;
	int currentTree = 0;
	MesquiteString treeSourceName;
	MesquiteCommand tsC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeTask = (TreeSource)hireEmployee(TreeSource.class, "Source of trees (for Character Value)");
		if (treeTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained");
		treeSourceName = new MesquiteString(treeTask.getName());
		tsC = makeCommand("setTreeSource",  this);
		treeTask.setHiringCommand(tsC);
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source", tsC);
			mss.setSelected(treeSourceName);
			mss.setList(TreeSource.class);
		}
		if (arguments !=null) {
			numberTask = (NumberForCharAndTree)hireNamedEmployee(NumberForCharAndTree.class, arguments);
			if (numberTask == null)
				return sorry(getName() + " couldn't start because the requested calculator module wasn't successfully hired.");
		}
		else {
			numberTask = (NumberForCharAndTree)hireEmployee(NumberForCharAndTree.class, "Value to calculate for character");
			if (numberTask == null)
				return sorry(getName() + " couldn't start because no calculator module obtained.");
		}
		if (getHiredAs() == NumberForCharacter.class){ //not hired as incrementable; offer menu item to change
			addMenuItem( "Choose tree...", makeCommand("chooseTree",  this));
		}
		return true;
	}
	public boolean returnsMultipleValues(){
		return numberTask.returnsMultipleValues();
	}
	public CompatibilityTest getCompatibilityTest(){
		if (numberTask !=null)
			return numberTask.getCompatibilityTest();
		return null;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee != treeTask || Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED)
			super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("setTreeSource " , treeTask);
		if (getHiredAs() == NumberForCharacter.class){ //not hired as incrementable; offer menu item to change
			temp.addLine("setTreeNumber " + currentTree);
		}

		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the source of trees", "[name of tree source module]", commandName, "setTreeSource")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees (for Character Value)", treeTask);
			if (temp !=null){
				treeTask = temp;
				treeTask.setPreferredTaxa(taxa);
				treeTask.setHiringCommand(tsC);
				treeSourceName.setValue(treeTask.getName());
				parametersChanged();
				return treeTask;
			}
		}
		else if (checker.compare(this.getClass(), "Present a dialog box to choose a tree from the current tree source", null, commandName, "chooseTree")) {
			int ic=treeTask.queryUserChoose(taxa, "for " + getName());
			if (MesquiteInteger.isCombinable(ic)) {
				currentTree = ic;
				parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Sets the tree to be the i'th one from the current tree source", "[number of tree to be used]", commandName, "setTreeNumber")) {
			int ic = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (MesquiteInteger.isCombinable(ic)) {
				currentTree = ic;
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(CharacterDistribution charStates){
		if (charStates == null)
			return;
		CharacterData data = charStates.getParentData();
		if (data!=null) {
			taxa = data.getTaxa();
			treeTask.initialize(taxa);
			numberTask.initialize(treeTask.getTree(taxa, 0), charStates);
		}
		if (taxa==null)
			taxa = getProject().chooseTaxa(containerOfModule(), "Taxa"); 

	}
	public void setCurrent(long i){
		if (treeTask==null || taxa==null){
			currentTree = (int)i;
		}
		else if ((i>=0) && (i<treeTask.getNumberOfTrees(taxa))) {
			currentTree = (int)i;
		}
	}
	public long getCurrent(){
		return currentTree;
	}
	public long getMin(){
		return 0;
	}
	public long getMax(){
		if (taxa==null)
			return 0;
		else
			return treeTask.getNumberOfTrees(taxa);
	}
	public String getItemTypeName(){
		return "Tree";
	}
	public long toInternal(long i){
		return MesquiteTree.toInternal((int)i);
	}
	public long toExternal(long i){
		return MesquiteTree.toExternal((int)i);
	}
	MesquiteString rs = new MesquiteString();
	/*.................................................................................................................*/
	public  void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
		if (result==null)
			return;
		clearResultAndLastResult(result);
		if (taxa==null){
			initialize(charStates);
		}
		tree = treeTask.getTree(taxa, currentTree);
		rs.setValue("");
		numberTask.calculateNumber(tree, charStates, result, rs);
		if (resultString!=null) {
			resultString.setValue("For tree " + tree.getName() + ", ");
			resultString.append(rs.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	public String getParameters(){
		if (tree ==null)
			return "Calculator: " + numberTask.getName(); //of which tree??
		else
			return "Calculator: " + numberTask.getName() + " with tree \"" + tree.getName() + "\""; 
	}


}


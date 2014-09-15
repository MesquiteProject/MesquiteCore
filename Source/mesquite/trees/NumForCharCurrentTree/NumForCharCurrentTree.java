/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForCharCurrentTree;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumForCharCurrentTree extends NumberForCharacter implements NumForCharTreeDep {
	public String getName() {
		return "Character value with current tree";
	}
	public String getVeryShortName() {
		if (numberTask ==null)
			return "Character value with current tree";
		else
			return numberTask.getVeryShortName() + " (current tree)";
	}
	public String getNameForMenuItem() {
		return "Character value with current tree...";
	}
	public String getExplanation() {
		return "Coordinates the calculation of a number for a character based on a current tree." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForCharAndTree.class, getName() + "  needs a method to calculate the value for a character using the tree.",
		"The method to calculate values can be selected initially or in the Values submenu");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a source for a current tree.",
		"The source for a current tree is arranged initially");
	}
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return NumberForCharAndTree.class;
	}
	/*.................................................................................................................*/
	NumberForCharAndTree numberTask;
	OneTreeSource treeTask;
	Taxa taxa;
	Tree tree;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of current tree");
		if (treeTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained.");
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
		ntC =makeCommand("setNumberTask",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(NumberForCharAndTree.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Values (for char. on tree)", ntC, NumberForCharAndTree.class);
			mss.setSelected(numberTaskName);
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
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setNumberTask ", numberTask);  
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that calculates numbers for characters with the current tree", "[name of module]", commandName, "setNumberTask")) {
			NumberForCharAndTree temp =  (NumberForCharAndTree)replaceEmployee(NumberForCharAndTree.class, arguments, "Number for character and tree", numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				parametersChanged();
				return numberTask;
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
			numberTask.initialize(treeTask.getTree(taxa), charStates);
		}

		if (taxa==null)
			taxa = getProject().chooseTaxa(containerOfModule(), "Taxa"); 
	}
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
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
		tree = treeTask.getTree(taxa);
		rs.setValue("");
		numberTask.calculateNumber(tree, charStates, result, rs);
		if (resultString!=null) {
			resultString.setValue("For current tree, ");
			resultString.append(rs.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==treeTask) {
			parametersChanged(notification);
		}
		else if (employee==numberTask) {
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public String getParameters(){
		if (tree ==null)
			return "Calculator: " + numberTask.getName(); //of which tree??
		else
			return "Calculator: " + numberTask.getName() + " with tree \"" + tree.getName() + "\""; 
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
		if (tree ==null)
			return numberTask.getName() + " with current tree"; //of which tree??
		else
			return numberTask.getName() + " with tree \"" + tree.getName() + "\""; 
	}


}


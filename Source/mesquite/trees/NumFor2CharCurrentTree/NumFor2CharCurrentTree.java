/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumFor2CharCurrentTree;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumFor2CharCurrentTree extends NumberFor2Characters  {
	public String getName() {
		return "Value for 2 characters with current tree";
	}
	public String getVeryShortName() {
		if (numberTask ==null)
			return "2 Character value (current tree)";
		else
			return numberTask.getVeryShortName() + " (current tree)";
	}
	public String getNameForMenuItem() {
		return "Value for 2 characters with current tree...";
	}
	public String getExplanation() {
		return "Coordinates the calculation of a number for two characters based on a current tree." ;
	}	
	
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberFor2CharAndTree.class, getName() + "  needs a method to calculate the value for two characters using the tree.",
		"The method to calculate values can be selected initially or in the Values submenu");
		EmployeeNeed e2 = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a source for a current tree.",
		"The source for a current tree is arranged initially");
	}
	/*.................................................................................................................*/
	NumberFor2CharAndTree numberTask;
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
		numberTask = (NumberFor2CharAndTree)hireEmployee(NumberFor2CharAndTree.class, "Value to calculate for two characters on current tree");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");

		ntC =makeCommand("setNumberTask",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(NumberFor2CharAndTree.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, NumberFor2CharAndTree.class);
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
			NumberFor2CharAndTree temp =  (NumberFor2CharAndTree)replaceEmployee(NumberFor2CharAndTree.class, arguments, "Number for 2 characters and tree", numberTask);
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
	public void initialize(CharacterDistribution charStates1, CharacterDistribution charStates2){
		if (charStates1 == null)
			return;
		CharacterData data = charStates1.getParentData();
		if (data!=null) {
			taxa = data.getTaxa();
			treeTask.initialize(taxa);
			numberTask.initialize(treeTask.getTree(taxa), charStates1, charStates2);
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
	public  void calculateNumber(CharacterDistribution charStates1, CharacterDistribution charStates2, MesquiteNumber result, MesquiteString resultString) {
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (taxa==null){
			initialize(charStates1, charStates2);
		}
		tree = treeTask.getTree(taxa);
		rs.setValue("");
		numberTask.calculateNumber(tree, charStates1, charStates2, result, rs);
		if (resultString!=null) {
			resultString.setValue("For current tree, ");
			resultString.append(rs.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==treeTask || employee==numberTask) {
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
	/*.................................................................................................................*/


}


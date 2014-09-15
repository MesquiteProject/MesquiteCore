/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForTreeCladeValue;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumForTreeCladeValue extends NumberForTree {
	public String getName() {
		return "Value for Clade of Selected Taxa";
	}
	public String getVeryShortName() {
		if (numberTask ==null)
			return "Value for Clade of Selected Taxa";
		else
			return numberTask.getVeryShortName();
	}
	public String getNameForMenuItem() {
		return "Value for Clade of Selected Taxa....";
	}
	public String getExplanation() {
		return "Coordinates the calculation of a value for the clade of selected terminal taxa.  If no terminal taxa are selected, the value at the root is given.  If the selected taxa do not make a clade, no answer is returned." ;
	}	
	public int getVersionOfFirstRelease(){
		return 260;  
	}
	
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumbersForNodes.class, getName() + "  needs a method to calculate values for nodes.",
		"The method to calculate values can be selected initially or using the Values submenu");
		e.setPriority(1);
	}
	/*.................................................................................................................*/
	NumbersForNodes numberTask;
	Taxa taxa;
	Tree tree;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	MesquiteNumber lastValue;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumbersForNodes)hireNamedEmployee(NumbersForNodes.class, arguments);
			if (numberTask == null)
				return sorry(getName() + " couldn't start because the requested calculator module wasn't successfully hired.");
		}
		else {
			numberTask = (NumbersForNodes)hireEmployee(NumbersForNodes.class, "Value to calculate for selected clade");
			if (numberTask == null)
				return sorry(getName() + " couldn't start because no calculator module obtained.");
		}
		lastValue = new MesquiteNumber();
		ntC =makeCommand("setNumberTask",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(NumbersForNodes.class)>1 && numberTask.getCompatibilityTest()==null) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, NumbersForNodes.class);
			mss.setSelected(numberTaskName);
		}
		return true;
	}
	
	public boolean returnsMultipleValues(){
		return false;
}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
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
			NumbersForNodes temp =  (NumbersForNodes)replaceEmployee(NumbersForNodes.class, arguments, "Number for character and tree", numberTask);

			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (!MesquiteThread.isScripting())
					parametersChanged();
				return numberTask;
			}
		}
		else if (checker.compare(this.getClass(), "Returns most recent value calculated", null, commandName, "getMostRecentNumber")) {
			return lastValue;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		if (tree == null)
			return;
		Taxa prevTaxa = taxa;
		taxa = tree.getTaxa();
		if (prevTaxa != null && prevTaxa != taxa){
			prevTaxa.removeListener(this);
			taxa.addListener(this);
		}
		else if (prevTaxa == null){
			taxa.addListener(this);
		}
		numberTask.initialize(tree);
	}
	
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj == taxa && code == MesquiteListener.SELECTION_CHANGED){
			parametersChanged();
		}
	}
	MesquiteString rs = new MesquiteString();
	NumberArray nr = new NumberArray();
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		lastValue.setToUnassigned();
		if (result==null || tree == null)
			return;
	   	clearResultAndLastResult(result);
		if (taxa==null){
			initialize(tree);
		}
		boolean bt = tree.isClade(taxa.getSelectedBits());
		if (!bt && taxa.anySelected()){
			if (resultString != null)
				resultString.setValue("Selected taxa do not form clade");
			return;
		}
		nr.deassignArray();
		nr.resetSize(tree.getNumNodeSpaces());
		rs.setValue("");
		numberTask.calculateNumbers(tree, nr, rs); ///Tree tree, NumberArray result, MesquiteString resultString
		int node = tree.getRoot();
		if (taxa.anySelected())
			node = tree.mrca(taxa.getSelectedBits());
		nr.placeValue(node, result);
		if (resultString!=null) {
			resultString.setValue(numberTask.getName());
			if (node == tree.getRoot())
				resultString.append(" (of root): ");
			else
				resultString.append(" (of selected clade): ");
			resultString.append(result.toString());
		}
		lastValue.setValue(result);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getParameters(){
			return "Calculator: " + numberTask.getName(); 
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
			return numberTask.getName();
	}
	/*.................................................................................................................*/



}


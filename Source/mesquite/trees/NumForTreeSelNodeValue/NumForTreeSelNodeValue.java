/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForTreeSelNodeValue;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumForTreeSelNodeValue extends NumberForTree {
	public String getName() {
		return "Value for Node Selected In Current Tree";
	}
	public String getVeryShortName() {
		if (numberTask ==null)
			return "Value for Node Selected In Current Tree";
		else
			return numberTask.getVeryShortName();
	}
	public String getNameForMenuItem() {
		return "Value for Node Selected In Current Tree....";
	}
	public String getExplanation() {
		return "Coordinates the calculation of a value for a node selected in a current tree.  If no node or more than one node is selected in current tree, no result is returned.  " + 
		"If the assessed tree does not share the same clade of the selected node, no answer is returned." ;
	}	
	public int getVersionOfFirstRelease(){
		return 260;  
	}
	
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumbersForNodes.class, getName() + "  needs a method to calculate values for nodes.",
		"The method to calculate values can be selected initially or using the Values submenu");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a source of a comparison tree.",
		"The source of comparison tree is arranged initially");
	}
	/*.................................................................................................................*/
	Tree otherTree = null;
	OneTreeSource otherTreeTask;
	NumbersForNodes numberTask;
	Taxa taxa;
	Tree tree;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	MesquiteNumber lastValue;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		otherTreeTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of tree for comparison");
		if (otherTreeTask == null) {
			return sorry(getName() + " couldn't start because no source of a current tree was obtained.");
		}
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
		temp.addLine("setTreeSource ", otherTreeTask); 
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
		else if (checker.compare(this.getClass(), "Sets the source of the comparison tree", "[name of module]", commandName, "setTreeSource")) {
			OneTreeSource temp = (OneTreeSource)replaceEmployee(OneTreeSource.class, arguments, "Source of other tree", otherTreeTask);
			if (temp !=null){
				otherTreeTask = temp;
				parametersChanged();
				return otherTreeTask;
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
		taxa = tree.getTaxa();
			otherTreeTask.initialize(tree.getTaxa());
		numberTask.initialize(tree);
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
		otherTree = otherTreeTask.getTree(tree.getTaxa());
		if (otherTree == null || !(otherTree instanceof Associable)){
			if (resultString != null)
				resultString.setValue("No appropriate current tree is available from which to obtain selected node.");
			return;
		}
		Associable oT = (Associable)otherTree;
		if (oT.numberSelected() != 1){
			if (resultString != null)
				resultString.setValue("Current tree has no or more than one node selected.");
			return;
		}
		int oN = oT.firstSelected();
		Bits tS = otherTree.getTerminalTaxaAsBits(oN);
		boolean bt = tree.isClade(tS);
		
		if (!bt){
			if (resultString != null)
				resultString.setValue("Node selected in current tree does not represent clade in this tree.");
			return;
		}
		
		nr.deassignArray();
		nr.resetSize(tree.getNumNodeSpaces());
		rs.setValue("");
		numberTask.calculateNumbers(tree, nr, rs); ///Tree tree, NumberArray result, MesquiteString resultString
		int node = tree.mrca(tS);
		nr.placeValue(node, result);
		if (resultString!=null) {
			resultString.setValue(numberTask.getName());
			if (node == tree.getRoot())
				resultString.append(" (of root): ");
			else
				resultString.append(" (of selected node): ");
			resultString.append(result.toString());
		}
		lastValue.setValue(result);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getParameters(){
			String s= "Calculator: " + numberTask.getName(); 
			if (otherTree !=null)
				s += "; current tree with selected node " + otherTree;
			return s;
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
			return numberTask.getName();
	}
	/*.................................................................................................................*/



}


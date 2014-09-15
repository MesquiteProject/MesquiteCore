/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.AverageTreeValue;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class AverageTreeValue extends NumberForTreeBlock {
	public String getName() {
		return "Average Tree Value";
	}
	public String getVeryShortName() {
		if (treeValueTask==null)
			return getName();
		return "Average " + treeValueTask.getName();
	}
	public String getExplanation() {
		return "Calculates the average of some value for trees in a tree block." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTree.class, getName() + "  needs a method to calculate values for the trees.",
		"The method to calculate values can be selected initially or from the Values submenu");
	}
	/*.................................................................................................................*/
	MesquiteNumber average;
	MesquiteNumber treeValue;
	NumberForTree treeValueTask;
	Taxa oldTaxa = null;
	MesquiteString charSourceName;
	MesquiteCommand mc;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeValue=new MesquiteNumber();
		average=new MesquiteNumber();
		treeValueTask = (NumberForTree)hireEmployee(NumberForTree.class, "Value to calculate for trees");
		if (treeValueTask == null)
			return sorry(getName() + " couldn't start because no calculator module was obtained");
		mc =makeCommand("setCalculator",  this);
		treeValueTask.setHiringCommand(mc);
		MesquiteSubmenuSpec mss = addSubmenu(null, "Values", mc);//TODO: checkmark

		mss.setList(NumberForTree.class);
		return true;
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}

	/*.................................................................................................................*/
	public void initialize(TreeVector trees) {
		treeValueTask.initialize(trees.getTree(0));
	}
	/*.................................................................................................................*/
	public void calculateNumber(TreeVector trees, MesquiteNumber result, MesquiteString resultString) {
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		average.setValue((int)0);
		int count = 0;
		MesquiteNumber tl = new MesquiteNumber();
		for (int itr = 0; itr<trees.size(); itr++) {
			Tree tree = trees.getTree(itr);
			tl.setToUnassigned();
			treeValueTask.calculateNumber(tree, tl, null);
			if (tl.isCombinable()) {
				average.add(tl);
				count++;
			}
		}
		average.divideBy(count);
		result.setValue(average);
		if (resultString!=null)
			resultString.setValue("Average "+ treeValueTask.getName() + ": " + average.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setCalculator ", treeValueTask);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module to calculate numbers for the trees", "[name of module]", commandName, "setCalculator")) {
			NumberForTree temp =   (NumberForTree)replaceEmployee(NumberForTree.class, arguments, "Which value to calculate?", treeValueTask);
			if (temp!=null) {
				treeValueTask = temp;
				treeValueTask.setHiringCommand(mc);
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public String getParameters() {
		if (treeValueTask==null)
			return null;
		return "Average for " + treeValueTask.getName();
	}
}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.TreeSatisfiesValue;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.awt.*;

/** this is a module that deterimines whether or not a tree has a value the same as, greater than or less than a specified value */
public class TreeSatisfiesValue extends BooleanForTree implements MesquiteListener {
	public String getVeryShortName() {
		return "Tree Satisfies Value?";
	}
	public String getName() {
		return "Tree Value Satisfies Criterion";
	}
	public String getExplanation() {
		return "Determines if the tree has a value either greater than, less than or equal to one specified." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTree.class, getName() + "  needs a method to calculate values for trees.",
		"The method to calculate values for trees can be selected initially or in the Type of Value submenu");
	}
	MesquiteBoolean equals, greaterThan, lessThan;
	boolean defaultEquals = true;
	boolean defaultGT = false;
	boolean defaultLT = false;
	NumberForTree numberTask = null;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	MesquiteNumber target = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numberTask = (NumberForTree)hireEmployee(NumberForTree.class, "Values for trees to satisfy");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculating module obtained for values for trees.");
		target = new MesquiteNumber(0);
		equals = new MesquiteBoolean(defaultEquals);
		greaterThan = new MesquiteBoolean(defaultGT);
		lessThan = new MesquiteBoolean(defaultLT);
		if (!MesquiteThread.isScripting()) 
			if (!presentOptions())				return false;
		numberTaskName = new MesquiteString();
		numberTaskName.setValue("Criterion for Tree Values");
		numberTask.setHiringCommand(ntC);
		MesquiteSubmenuSpec mss = addSubmenu(null, getName());
		if (numModulesAvailable(NumberForTree.class)>1) {
			addItemToSubmenu(null, mss,"Type of Value...", MesquiteModule.makeCommand("setValues",  this));
		}
		addItemToSubmenu(null, mss, "Set Target Value...", MesquiteModule.makeCommand("setTarget",  this));

		addItemToSubmenu(null, mss, "-", null);

		addCheckMenuItemToSubmenu(null, mss, "Equals", MesquiteModule.makeCommand("toggleEquals",  this), equals);
		addCheckMenuItemToSubmenu(null, mss, "Greater Than", MesquiteModule.makeCommand("toggleGT",  this), greaterThan);
		addCheckMenuItemToSubmenu(null, mss, "Less Than", MesquiteModule.makeCommand("toggleLT",  this), lessThan);
		return true;
	}
	public boolean presentOptions(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Criteria",  buttonPressed);
		queryDialog.addLabel("Value trees need to satisfy", Label.CENTER);
		DoubleField df = queryDialog.addDoubleField("Target Value", target.getDoubleValue(), 10);
		Checkbox E = queryDialog.addCheckBox ("Equal to target", true);
		Checkbox GT = queryDialog.addCheckBox ("Greater than target", false);
		Checkbox LT = queryDialog.addCheckBox ("Less than target", false);

		queryDialog.completeAndShowDialog(true);

		boolean ok = (queryDialog.query()==0);

		if (ok) {
			target.setValue(df.getValue());
			equals.setValue(E.getState());
			greaterThan.setValue(GT.getState());
			lessThan.setValue(LT.getState());
		}

		queryDialog.dispose();
		return ok;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setValues ", numberTask);

		temp.addLine("setTarget " + target);
		if (equals.getValue()!=defaultEquals)
			temp.addLine("toggleEquals " + equals.toOffOnString());
		if (greaterThan.getValue()!=defaultGT)
			temp.addLine("toggleGT " + greaterThan.toOffOnString());
		if (lessThan.getValue()!=defaultLT)
			temp.addLine("toggleLT " + lessThan.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the target value trees must satisfy", "[target value]", commandName, "setTarget")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			double T = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(T))
				T = MesquiteDouble.queryDouble(containerOfModule(), "Target Value", "Sets the target value that trees must satisfy.", target.getDoubleValue());
			if (!MesquiteDouble.isCombinable(T))
				return null;
			if (target.getDoubleValue() != T) {
				target.setValue(T);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value equal to that in cell touched", "[on = selects equal; off]", commandName, "toggleEquals")) {
			boolean current = equals.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			equals.toggleValue(ParseUtil.getFirstToken(arguments, io));
			if (current != equals.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value greater than that in cell touched", "[on = selects greater than; off]", commandName, "toggleGT")) {
			boolean current = greaterThan.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			greaterThan.toggleValue(ParseUtil.getFirstToken(arguments, io));
			if (current != equals.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value less than that in cell touched", "[on = selects less than; off]", commandName, "toggleLT")) {
			boolean current = lessThan.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			lessThan.toggleValue(ParseUtil.getFirstToken(arguments, io));
			if (current != equals.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the module to calculate the value for trees to satisfy", "[name of module]", commandName, "setValues")) {
			NumberForTree temp =  (NumberForTree)replaceEmployee(NumberForTree.class, arguments, "Values for trees to satisfy", numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (!MesquiteThread.isScripting()) {
					parametersChanged();
				}

				return numberTask;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void calculateBoolean(Tree tree, MesquiteBoolean result, MesquiteString resultString) {
		if (result==null || tree==null)
			return;
		MesquiteNumber r = new MesquiteNumber();

		numberTask.calculateNumber(tree, r, null);
		boolean bt = ((greaterThan.getValue() && r.isMoreThan(target)) || (lessThan.getValue() && r.isLessThan(target)) || (equals.getValue() && r.equals(target)));
		result.setValue(bt);
		boolean first = true;
		String s = "";
		if (greaterThan.getValue()) {
			s += " greater than";
			first = false;
		}
		if (lessThan.getValue()) {
			if (!first)
				s += " or";
			s += " less than";
			first = false;
		}
		if (equals.getValue()) {
			if (!first)
				s += " or";
			s += " equal to";
		}
		if (resultString!=null)
			if (bt)
				resultString.setValue("Tree's value for " + numberTask.getName() + " is " + s + " the target value of " + target + ", and thus satisfies the criterion");
			else
				resultString.setValue("Tree's value for " + numberTask.getName() + " is NOT " + s + " the target value of " + target + ", and thus fails the criterion");
	}
	/*.................................................................................................................*/
	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/
	public boolean getUserChooseable(){
		return true;
	}


}


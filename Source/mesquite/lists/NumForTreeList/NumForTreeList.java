/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.NumForTreeList;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class NumForTreeList extends TreeListAssistant implements MesquiteListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Number for Tree (in List of Trees window)";
	}
	public String getNameForMenuItem() {
		return "Number for tree";
	}

	public String getExplanation() {
		return "Supplies numbers for trees for a trees list window." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTree.class, getName() + " needs a method to calculate a value for each of the trees.",
		"You can select a value to show in the Number For Trees submenu of the Columns menu of the List of Trees Window. ");
		e.setPriority(1);
	}
	/*.................................................................................................................*/
	NumberForTree numberTask;
	TreeVector treesBlock;
	boolean suppressed = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForTree)hireNamedEmployee(NumberForTree.class, arguments);
			if (numberTask==null) {
				return sorry("Number for tree (for list) can't start because the requested calculator module wasn't successfully hired");
			}
		}
		else {
			numberTask = (NumberForTree)hireEmployee(NumberForTree.class, "Value to calculate for trees (for tree list)");
			if (numberTask==null) {
				return sorry("Number for tree (for list) can't start because no calculator module was successfully hired");
			}
		}
		return true;
	}
	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return NumberForTree.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suppress"); 
		temp.addLine("setValueTask ", numberTask); 
		temp.addLine("desuppress"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a number for a tree", "[name of module]", commandName, "setValueTask")) {
			NumberForTree temp= (NumberForTree)replaceEmployee(NumberForTree.class, arguments, "Number for a tree", numberTask);
			if (temp!=null) {
				numberTask = temp;
				if (!suppressed){
					doCalcs();
					parametersChanged();
				}
				return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Suppresses calculation", null, commandName, "suppress")) {
			suppressed = true;
		}
		else if (checker.compare(this.getClass(), "Releases suppression of calculation", null, commandName, "desuppress")) {
			if (suppressed){
				suppressed = false;
				outputInvalid();
				doCalcs();
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void setTableAndTreeBlock(MesquiteTable table, TreeVector trees){
		treesBlock = trees;
		if (!suppressed)
			doCalcs();
	}
	public String getTitle() {
		if (numberTask==null)
			return "";
		return numberTask.getVeryShortName();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == treesBlock)
			treesBlock=null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		if (!suppressed){
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (!suppressed){
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	NumberArray na = new NumberArray(0);
	StringArray explArray = new StringArray(0);
	/*.................................................................................................................*/
	public void doCalcs(){
		if (suppressed || numberTask==null || treesBlock == null)
			return;
		
		int numTrees = treesBlock.size();
		na.resetSize(numTrees);
		explArray.resetSize(numTrees);
		MesquiteNumber mn = new MesquiteNumber();
		MesquiteString expl = new MesquiteString();
		for (int ic=0; ic<numTrees; ic++) {
			CommandRecord.tick("Number for tree in tree list; examining tree " + ic);
			Tree tree = treesBlock.getTree(ic);
		//	if (tree instanceof MesquiteTree)
		//		((MesquiteTree)tree).setAssignedNumber(ic);
			mn.setToUnassigned();
			numberTask.calculateNumber(tree, mn, expl);
			na.setValue(ic, mn);
			explArray.setValue(ic, expl.getValue());
		}
	}
	public String getExplanationForRow(int ic){
		if (explArray == null || explArray.getSize() <= ic)
			return null;
		return explArray.getValue(ic);
	}
	public String getStringForTree(int ic){
		if (na==null)
			return "";

		return na.toString(ic);
	}
	public String getWidestString(){
		if (numberTask==null)
			return "888888";
		return numberTask.getVeryShortName()+"   ";
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
}


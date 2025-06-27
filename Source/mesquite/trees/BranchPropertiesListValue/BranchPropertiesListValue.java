/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BranchPropertiesListValue;

import mesquite.lib.CommandChecker;
import mesquite.lib.StringUtil;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.MesquiteTree;
import mesquite.trees.lib.BranchPropertiesListAssistant;

/* ======================================================================== */
public class BranchPropertiesListValue extends BranchPropertiesListAssistant  {
	MesquiteTree tree =null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("How to Edit Values...", makeCommand("howToEdit", this));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Value of Branch/Node Property";
	}
	public String getVeryShortName() {
		if (node >=0)
			return "Value at Node " + node;
		return "Value";
	}
	public String getExplanation() {
		return "Shows the value of property (a number, string, or other object) belonging to a branch or node." ;
	}


	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;

	}

 	public void setTree(MesquiteTree tree){
 		this.tree = tree;
		parametersChanged();
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Explains how to edit", null, commandName, "howToEdit")) {
			alert("Some of these values can be edited for a branch/node by right-clicking on the branch, then choosing the item in the drop down menu that appears.");
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	int node = -1;
	
	/*.................................................................................................................*/
	public void cursorTouchBranch(MesquiteTree tree, int N){
		node = N;
		parametersChanged();
	}
	public void cursorEnterBranch(MesquiteTree tree, int N){
		node = N;
		parametersChanged();
	}
	public void cursorExitBranch(MesquiteTree tree, int N){
		node = -1;
		parametersChanged();
	}
	public void cursorMove(MesquiteTree tree){
		node = -1;
		parametersChanged();
	}
	/*.................................................................................................................*/
	int maxwidest = 8;
	public String getWidestString(){
		int w = maxwidest;
		if (table == null)
			return "888888";
			
		for (int ic = 0; ic<table.getNumRows(); ic++){
			String sIC = getStringForRow(ic);
			if (StringUtil.notEmpty(sIC)){
				if (sIC.length()>w)
					w = sIC.length();
			}
		}
		String eights =  "8888888888888888888888888888888888888888888888888888888888888888";
		if (w>50)
			w = 50;
		maxwidest = w;
		return eights.substring(0, w);
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Value";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/** Returns whether to use the string from getStringForRow; otherwise call drawInCell*/
	public boolean useString(int ic){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	public String getStringForRow(int ic) {
		if (tree == null || node<0)
			return "—";
		DisplayableBranchProperty property = getPropertyAtRow(ic);
		if (property != null){
			return property.getStringAtNode(tree, node, false, true, true);
		}
		return "?";

	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 400;  
	}


}

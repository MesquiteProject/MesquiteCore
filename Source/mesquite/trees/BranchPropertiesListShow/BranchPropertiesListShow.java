/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BranchPropertiesListShow;

import mesquite.lib.CommandChecker;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.MesquiteTree;
import mesquite.trees.lib.BranchPropertiesListAssistant;

/* ======================================================================== */
public class BranchPropertiesListShow extends BranchPropertiesListAssistant  {
	MesquiteTree tree =null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Show Selected", makeCommand("show", this));
		addMenuItem("Hide Selected", makeCommand("hide", this));
		addMenuItem("Control Display of Properties on Tree...", makeCommand("controlAppearance", this));
		getEmployer().addMenuItem("Control Display of Properties on Tree...", makeCommand("controlAppearance", this));
		addMenuItem("Explanation...", makeCommand("explain", this));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Information Showing on Tree?";
	}
	public String getVeryShortName() {
		return "Showing?";
	}
	public String getExplanation() {
		return "Shows whether attached information is shown in the tree window." ;
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
		if (checker.compare(this.getClass(), "Shows the items", null, commandName, "show")) {
				showHideInTree(true);
		}
		else if (checker.compare(this.getClass(), "Shows the items", null, commandName, "hide")) {
			showHideInTree(false);
	}
		else if (checker.compare(this.getClass(), "Shows the items", null, commandName, "controlAppearance")) {
			controlAppearanceOnTree();
	}
		else if (checker.compare(this.getClass(), "Explain", null, commandName, "explain")) {
			discreetAlert("This column shows which values are shown on the branches of the tree in the tree window. "
					+"You can control them here, or by the menu item \"Display Branch/Node Properties\" in the Tree menu. "
					+"\n\nThat menu item also allows you to control the font and placement on the tree."
					+ "\n\nNode labels and branch lengths can also be shown on the tree in other ways, using items in the Text menu.");
	}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	/*.................................................................................................................*/
	void showHideInTree(boolean show){
		if (table == null)
			return;
		if (!table.anyRowSelected()){
			discreetAlert("Please selected rows before attempting to show or hide them here");
			return;
		}
		DisplayableBranchProperty[] mis = new DisplayableBranchProperty[table.numRowsSelected()];
		int count = 0;
		for (int ir = 0; ir<table.getNumRows(); ir++){
			if (table.isRowSelected(ir)){
				mis[count++] = getPropertyAtRow(ir);
			}
		}
		pleaseShowHideOnTree(mis, show);
	}
	
	
	/*.................................................................................................................*/
	public String getWidestString(){
		return "Showing?";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Showing?";
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
	/*.................................................................................................................*/
	public String getStringForRow(int ic) {
			if (isShowingOnTree(getPropertyAtRow(ic)))
			return "✓";
			else
				return "✗";

	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 400;  
	}


}

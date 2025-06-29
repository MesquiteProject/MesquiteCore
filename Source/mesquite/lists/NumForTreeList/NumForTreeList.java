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

import java.awt.Color;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.NumberArray;
import mesquite.lib.Pausable;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.MesquiteColorTable;
import mesquite.lists.lib.TreeListAssistant;

/* ======================================================================== */
public class NumForTreeList extends TreeListAssistant implements MesquiteListener, Pausable {
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
	boolean suppressedByScript = false;
	MesquiteBoolean shadeCells = new MesquiteBoolean(false);
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
		addCheckMenuItem(null, "Color Cells", makeCommand("toggleShadeCells",  this), shadeCells); 
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
		temp.addLine("toggleShadeCells " + shadeCells.toOffOnString());
		temp.addLine("desuppress"); 
		return temp;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a number for a tree", "[name of module]", commandName, "setValueTask")) {
			NumberForTree temp= (NumberForTree)replaceEmployee(NumberForTree.class, arguments, "Number for a tree", numberTask);
			if (temp!=null) {
				numberTask = temp;
				if (okToCalc()){
					doCalcs();
					parametersChanged();
				}
				return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to color cells", "[on or off]", commandName, "toggleShadeCells")) {
			boolean current = shadeCells.getValue();
			shadeCells.toggleValue(parser.getFirstToken(arguments));
			if (current!=shadeCells.getValue()) {
				outputInvalid();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Suppresses calculation", null, commandName, "suppress")) {
			suppressedByScript = true;
		}
		else if (checker.compare(this.getClass(), "Releases suppression of calculation", null, commandName, "desuppress")) {
			if (suppressedByScript){
				suppressedByScript = false;
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
		if (okToCalc())
			doCalcs();
	}
	public String getTitle() {
		if (numberTask==null)
			return "";
		return numberTask.getVeryShortName();
	}
	/*.................................................................................................................*/
	/** Indicate what could be paused */
	public void addPausables(Vector pausables) {
		if (pausables != null)
			pausables.addElement(this);
	}
	/** to ask Pausable to pause*/
	public void pause() {
		paused = true;
	}
	/** to ask a Pausable to unpause (i.e. to resume regular activity)*/
	public void unpause() {
		paused = false;
		doCalcs();
		parametersChanged(null);
	}
	/*.................................................................................................................*/
	boolean paused = false;
	boolean okToCalc() {
		return !suppressedByScript && !paused;
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
		if (okToCalc()){
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (okToCalc()){
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/** Gets background color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getBackgroundColorOfCell(int ic, boolean selected){
		if (!shadeCells.getValue())
			return null;
		if (min.isCombinable() && max.isCombinable() && na != null && na.isCombinable(ic)){
			return MesquiteColorTable.getGreenScale(na.getDouble(ic), min.getDoubleValue(), max.getDoubleValue(), false);
		}
		return null;
	}
	/*.................................................................................................................*/
	NumberArray na = new NumberArray(0);
	StringArray explArray = new StringArray(0);
	MesquiteNumber min = new MesquiteNumber();
	MesquiteNumber max = new MesquiteNumber();
	/*.................................................................................................................*/
	public void doCalcs(){
		if (!okToCalc() || numberTask==null || treesBlock == null)
			return;

		int numTrees = treesBlock.size();
		na.deassignArrayToInteger();
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
		na.placeMinimumValue(min);
		na.placeMaximumValue(max);
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


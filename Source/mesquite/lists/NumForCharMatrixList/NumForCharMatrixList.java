/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.NumForCharMatrixList;
/*~~  */

import java.awt.Color;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.NumberArray;
import mesquite.lib.Pausable;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.NumberForMatrix;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MesquiteColorTable;
import mesquite.lib.ui.SingleLineTextField;
import mesquite.lists.lib.CharMatricesListAssistant;

/* ======================================================================== */
public class NumForCharMatrixList extends CharMatricesListAssistant implements MesquiteListener, Pausable  {
	/*.................................................................................................................*/
	public String getName() {
		return "Number for Matrix (in List of Character Matrices window)";
	}
	public String getNameForMenuItem() {
		return "Number for Matrix";
	}
	public String getExplanation() {
		return "Supplies numbers for character matrcies for a character matrices list window." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForMatrix.class, getName() + " needs a method to calculate a value for each of the character matrices.",
				"You can select a value to show in the Number For Character Matrices submenu of the Columns menu of the List of Character Matrices Window. ");
	}
	NumberForMatrix numberTask;
	MesquiteBoolean shadeCells = new MesquiteBoolean(false);
	MesquiteTable table;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForMatrix)hireNamedEmployee(NumberForMatrix.class, arguments);
			if (numberTask==null) {
				return sorry("Number for character matrix (for list) can't start because the requested module was not successfully hired");
			}
		}
		else {
		numberTask = (NumberForMatrix)hireEmployee(NumberForMatrix.class, "Value to calculate for character matrix (for List of Matrices window)");
		if (numberTask==null) {
			return sorry("Number for character matrix (for list) can't start because the no calculating module was successfully hired");
		}
		}
		shadeCells.setValue(false);
		addCheckMenuItem(null, "Color Cells", makeCommand("toggleShadeCells",  this), shadeCells); 
		addMenuItem(null, "Select based on value...", makeCommand("selectBasedOnValue",  this));
		return true;
	}

	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
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
		return !paused;
	}

	public void setTableAndObject(MesquiteTable table, Object obj){
		if (datas !=null)
			datas.removeListener(this);
		if (obj instanceof ListableVector)
			this.datas = (ListableVector)obj;
		datas.addListener(this);
		this.table = table;
		doCalcs();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == datas)
			datas=null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		// below should be appearsCosmeticOrSelection if matrix initiating 
		if (Notification.appearsCosmeticOrSelection(notification))
			return;
		if (notification.getCode()!=MesquiteListener.LOCK_CHANGED)
			doCalcs();
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return NumberForMatrix.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setValueTask ", numberTask); 
		temp.addLine("toggleShadeCells " + shadeCells.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public boolean querySelectBounds(MesquiteNumber lessThan, MesquiteNumber moreThan) {
		if (lessThan==null || moreThan==null || numberTask==null)
			return false;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Select based upon value",buttonPressed); 

		dialog.addLargeOrSmallTextLabel("Select based upon value of " + numberTask.getNameOfValueCalculated());

		SingleLineTextField moreThanField = dialog.addTextField("Select values greater than or equal to ","",20);
		SingleLineTextField lessThanField = dialog.addTextField("Select values less than or equal to ","",20);


		dialog.completeAndShowDialog(true);

		if (buttonPressed.getValue()==0)  {
			String s = moreThanField.getText();
			moreThan.setValue(s);
			s = lessThanField.getText();
			lessThan.setValue(s);
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	void selectBasedOnValue() {
		if (MesquiteThread.isScripting())
			return;
		if (table==null || datas==null || na==null)
			return;
		MesquiteNumber lessThan = new MesquiteNumber();
		MesquiteNumber moreThan = new MesquiteNumber();
		if (!querySelectBounds(lessThan, moreThan))
			return;
		if (!lessThan.isCombinable() && ! moreThan.isCombinable())
			return;
		MesquiteNumber value = new MesquiteNumber();
		for (int i=0; i<datas.getNumberOfParts(); i++) {
			na.placeValue(i, value);

			if (lessThan.isCombinable() && moreThan.isCombinable()) {
				if ((lessThan.isMoreThan(value)|| lessThan.equals(value)) &&  (moreThan.isLessThan(value) ||  moreThan.equals(value))) {
					table.selectRow(i);
					datas.setSelected(i, true);
					table.redrawFullRow(i);
				}
			} else if (lessThan.isCombinable() && (lessThan.isMoreThan(value)|| lessThan.equals(value))) {
				table.selectRow(i);
				datas.setSelected(i, true);
				table.redrawFullRow(i);
			} else if (moreThan.isCombinable() && (moreThan.isLessThan(value) ||  moreThan.equals(value))) {
				table.selectRow(i);
				datas.setSelected(i, true);
				table.redrawFullRow(i);
			}
		}

		datas.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a number for a character matrix", "[name of module]", commandName, "setValueTask")) {
			NumberForMatrix temp= (NumberForMatrix)hireNamedEmployee(NumberForMatrix.class, arguments);
			Class prev = null;
			if (numberTask != null)
				prev = numberTask.getClass();
			if (temp!=null) {
				numberTask = temp;
				if (prev != numberTask.getClass()){
					doCalcs();
					outputInvalid();
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
		else if (checker.compare(this.getClass(), "Selects list rows based on value of this column", null, commandName, "selectBasedOnValue")) {
			selectBasedOnValue();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public String getTitle() {
		if (numberTask==null)
			return "";
		return numberTask.getVeryShortName();
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
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (Notification.appearsCosmetic(notification) || (notification!=null && notification.getCode()==MesquiteListener.LOCK_CHANGED)) 
			return;
		doCalcs();
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	NumberArray na = new NumberArray(0);
	StringArray explArray = new StringArray(0);
	MesquiteNumber min = new MesquiteNumber();
	MesquiteNumber max = new MesquiteNumber();
	/*.................................................................................................................*/
	public void doCalcs(){
		if (!okToCalc() || numberTask==null || datas == null)
			return;
		outputInvalid();
		int numBlocks = datas.size();
		explArray.resetSize(numBlocks);
		MesquiteString expl = new MesquiteString();
		na.deassignArrayToInteger();
		na.resetSize(numBlocks);
		MesquiteNumber mn = new MesquiteNumber();
		for (int ic=0; ic<numBlocks; ic++) {
			CharacterData data = (CharacterData)datas.elementAt(ic);
			mn.setToUnassigned();
			numberTask.calculateNumber(data.getMCharactersDistribution(), mn, expl);
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
	public String getStringForRow(int ic){
		if (na==null)
			return "";

		return na.toString(ic);
	}
	public String getWidestString(){
		if (numberTask==null)
			return "88888888";
		String name = numberTask.getVeryShortName()+"   ";
		if (StringUtil.blank(name) || name.length()<8)
			return "88888888";
		return name;
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
	/*.................................................................................................................*/
	public void endJob() {
		if (datas !=null)
			datas.removeListener(this);
		super.endJob();
	}

}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.NumForTaxaList;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class NumForTaxaList extends TaxonListAssistant implements MesquiteListener {

	/*.................................................................................................................*/
	public String getName() {
		return "Number for Taxon (in List of Taxa window)";
	}
	public String getNameForMenuItem() {
		return "Number for Taxon";
	}
	public String getExplanation() {
		return "Supplies numbers for taxa for a taxa list window." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTaxon.class, getName()  + " needs a method to calculate a value for each of the taxa.",
		"You can select a value to show in the Number For Taxa submenu of the Columns menu of the List of Taxa Window. ");
	}
	/*.................................................................................................................*/
	NumberForTaxon numberTask;
	MesquiteBoolean shadeCells;
	Taxa taxa;
	boolean suppressed = false;
	MesquiteTable table;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForTaxon)hireNamedEmployee(NumberForTaxon.class, arguments);
			if (numberTask==null) {
				return sorry("Number for taxon (for list) can't start because the requested calculator module wasn't successfully hired");
			}
		}
		else {
			numberTask = (NumberForTaxon)hireEmployee(NumberForTaxon.class, "Value to calculate for taxa (for taxa list)");
			if (numberTask==null) {
				return sorry("Number for taxon (for list) can't start because no calculator module was successfully hired");
			}
		}
		shadeCells = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Color Cells", makeCommand("toggleShadeCells",  this), shadeCells);
		addMenuItem(null, "Select based on value...", makeCommand("selectBasedOnValue",  this));
		addMenuItem(null, "Recalculate", makeCommand("recalculate",  this));
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
		return NumberForTaxon.class;
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
	public void SelectBasedOnValue() {
		if (MesquiteThread.isScripting())
			return;
		if (table==null || taxa==null || na==null)
			return;
		MesquiteNumber lessThan = new MesquiteNumber();
		MesquiteNumber moreThan = new MesquiteNumber();
		if (!querySelectBounds(lessThan, moreThan))
			return;
		if (!lessThan.isCombinable() && ! moreThan.isCombinable())
			return;
		MesquiteNumber value = new MesquiteNumber();
		for (int i=0; i<taxa.getNumTaxa(); i++) {
			na.placeValue(i, value);

			if (lessThan.isCombinable() && moreThan.isCombinable()) {
				if ((lessThan.isMoreThan(value)|| lessThan.equals(value)) &&  (moreThan.isLessThan(value) ||  moreThan.equals(value))) {
					table.selectRow(i);
					taxa.setSelected(i, true);
					table.redrawFullRow(i);
				}
			} else if (lessThan.isCombinable() && (lessThan.isMoreThan(value)|| lessThan.equals(value))) {
				table.selectRow(i);
				taxa.setSelected(i, true);
				table.redrawFullRow(i);
			} else if (moreThan.isCombinable() && (moreThan.isLessThan(value) ||  moreThan.equals(value))) {
				table.selectRow(i);
				taxa.setSelected(i, true);
				table.redrawFullRow(i);
			}
		}
		
			taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a number for a taxon", "[name of module]", commandName, "setValueTask")) {
			NumberForTaxon temp= (NumberForTaxon)replaceEmployee(NumberForTaxon.class, arguments, "Number for a taxon", numberTask);
			if (temp!=null) {
				numberTask = temp;
				if (!suppressed){
					doCalcs();
					parametersChanged();
				}
				return temp;
			}
		}

		else if (checker.compare(this.getClass(), "Requests a recalculation of the numbers (this may be unnecessary)", null, commandName, "recalculate")) {
			doCalcs();
			parametersChanged();
		}

		else if (checker.compare(this.getClass(), "Selects list rows based on value of this column", null, commandName, "selectBasedOnValue")) {
			SelectBasedOnValue();
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
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		this.table = table;
		this.taxa = taxa;
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
		if (obj == taxa)
			taxa=null;
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
	MesquiteNumber min = new MesquiteNumber();
	MesquiteNumber max = new MesquiteNumber();
	/*.................................................................................................................*/
	public void doCalcs(){
		if (suppressed || numberTask==null || taxa == null)
			return;
		int numTaxa = taxa.getNumTaxa();
		na.resetSize(numTaxa);
		na.deassignArrayToInteger();
		explArray.resetSize(numTaxa);
		MesquiteNumber mn = new MesquiteNumber();
		MesquiteString expl = new MesquiteString();
		
		MesquiteTimer timer = new MesquiteTimer();
		timer.start();
		
		for (int ic=0; ic<numTaxa; ic++) {
			CommandRecord.tick("Number for taxon in taxon list; examining taxon " + ic);
			Taxon taxon = taxa.getTaxon(ic);
			mn.setToUnassigned();
			numberTask.calculateNumber(taxon, mn, expl);
			na.setValue(ic, mn);
			explArray.setValue(ic, expl.getValue());
		}
		na.placeMinimumValue(min);
		na.placeMaximumValue(max);
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
	public String getExplanationForRow(int ic){
		if (explArray == null || explArray.getSize() <= ic)
			return null;
		return explArray.getValue(ic);
	}
	public boolean useString(int ic){
		return true;
	}
	public String getStringForTaxon(int ic){
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
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
}


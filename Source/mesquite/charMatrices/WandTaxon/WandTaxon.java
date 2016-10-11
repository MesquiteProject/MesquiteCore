/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.WandTaxon; 

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.characters.*;

/*   to do:
	- deal with Option cursor, Shift cursor
 */


/* ======================================================================== */
public class WandTaxon extends DataWindowAssistantI {
	TableTool taxaWandTool; 
	MesquiteTable table;
	Taxa taxa;
	MesquiteBoolean equals, greaterthan, lessthan,selectByText, selectByColor;
	boolean defaultEquals = true;
	boolean defaultGT = false;
	boolean defaultLT = false;
	boolean taxaAreRows = true;
	MesquiteMenuItemSpec eItem, gItem, lItem;
	MesquiteCollator collator;
	public String getFunctionIconPath(){
		return getPath() + "taxaWand.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			taxaWandTool = new TableTool(this, "taxaWand", getPath() , "taxaWand.gif", 4,4,"Select taxa with same", "This tool selects taxa according to criteria of being the same, greater than, or less than the value in the cell touched.  The criteria can be set using the menu that appears when you click on the tool button", MesquiteModule.makeCommand("taxaWandTouch",  this) , null, null);
			((MesquiteWindow)containerOfModule()).addTool(taxaWandTool);
			setUseMenubar(false); //menu available by touching  button
			taxaWandTool.setPopUpOwner(this);
			taxaWandTool.setWorksOnRowNames(false);
		}
		else return false;
		equals = new MesquiteBoolean(defaultEquals);
		greaterthan = new MesquiteBoolean(defaultGT);
		lessthan = new MesquiteBoolean(defaultLT);
		selectByText = new MesquiteBoolean(true);

		selectByColor = new MesquiteBoolean(!selectByText.getValue());
		addCheckMenuItem(null, "Select by Text", makeCommand("selectByText",  this), selectByText);
		addCheckMenuItem(null, "Select by Color", makeCommand("selectByColor",  this), selectByColor);
		addMenuSeparator();
		eItem = addCheckMenuItem(null, "Equal", makeCommand("toggleEquals",  this), equals);
		gItem = addCheckMenuItem(null, "Greater than", makeCommand("toggleGT",  this), greaterthan);
		lItem = addCheckMenuItem(null, "Less than", makeCommand("toggleLT",  this), lessthan);
		collator = new MesquiteCollator();
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		if (data !=null)
			this.taxa = data.getTaxa();
		this.taxaAreRows = true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (equals.getValue()!=defaultEquals)
			temp.addLine("toggleEquals " + equals.toOffOnString());
		if (greaterthan.getValue()!=defaultGT)
			temp.addLine("toggleGT " + greaterthan.toOffOnString());
		if (lessthan.getValue()!=defaultLT)
			temp.addLine("toggleLT " + lessthan.toOffOnString());
		if (!selectByText.getValue())
			temp.addLine("selectByColor");
		return temp;
	}
	boolean satisfiesCriteria(String one, String two){
		if (equals.getValue() && one.equals(two))
			return true;
		double dOne = MesquiteDouble.fromString(one);
		double dTwo = MesquiteDouble.fromString(two);
		if (MesquiteDouble.isCombinable(dOne) && MesquiteDouble.isCombinable(dTwo)) {
			if (greaterthan.getValue() && (dTwo>dOne))
				return true;
			if (lessthan.getValue() && (dTwo<dOne))
				return true;
			return false;
		}
		int order = collator.compare(one, two);
		if (greaterthan.getValue() && (order == -1))
			return true;
		if (lessthan.getValue() && (order == 1))
			return true;
		if (equals.getValue() && (order == 0))
			return true;
		return false;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Touches on a cell with the taxon wand to select taxa similar as defined by the options", "[column touched][row touched]", commandName, "taxaWandTouch")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column= MesquiteInteger.fromString(arguments, io);
			int row= MesquiteInteger.fromString(arguments, io);
			if ((selectByColor.getValue() || MesquiteInteger.isNonNegative(column)) && (MesquiteInteger.isNonNegative(row))) {
				boolean commandSelected =  (arguments.indexOf("command")>=0);
				boolean subtractFromSelection = commandSelected && taxa.getSelected(row);
				if ((arguments.indexOf("shift")<0) && !commandSelected)
					table.deselectAll();
				table.offAllEdits();
				if (selectByText.getValue()){
					String text = table.getMatrixText(column, row);
					if (!taxaAreRows){  //each row is a taxon; hence go through this column to find which rows to select
						for (int i=0; i<table.getNumColumns(); i++){
							if (satisfiesCriteria(text, table.getMatrixText(i, row))) {
								if (subtractFromSelection) 
									taxa.setSelected(i, false);
								else 
									taxa.setSelected(i, true);
							}
						}
					}
					else {
						for (int i=0; i<table.getNumRows(); i++){
							if (satisfiesCriteria(text, table.getMatrixText(column, i))) {
								if (subtractFromSelection) {
									taxa.setSelected(i, false);
								}
								else {
									taxa.setSelected(i, true);
								}
							}
						}
					}
				}
				else {
					if (getEmployer() instanceof mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker){
						mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker mb = (mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker)getEmployer();
						mb.selectSameColorRow(column, row, subtractFromSelection);
					}
				}

				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
		else if (checker.compare(this.getClass(), "Sets the wand to select by text", null, commandName, "selectByText")) {
			selectByText.setValue(true);
			selectByColor.setValue(false);
			if (taxaWandTool != null)
				taxaWandTool.setWorksOnRowNames(false);
			eItem.setEnabled(true);
			gItem.setEnabled(true);
			lItem.setEnabled(true);
		}
		else if (checker.compare(this.getClass(), "Sets the wand to select by color", null, commandName, "selectByColor")) {
			selectByText.setValue(false);
			selectByColor.setValue(true);
			if (taxaWandTool != null)
				taxaWandTool.setWorksOnRowNames(true);
			eItem.setEnabled(false);
			gItem.setEnabled(false);
			lItem.setEnabled(false);
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value equal to that in cell touched", "[on = selects equal; off]", commandName, "toggleEquals")) {
			boolean current = equals.getValue();
			equals.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value greater than that in cell touched", "[on = selects greater than; off]", commandName, "toggleGT")) {
			boolean current = greaterthan.getValue();
			greaterthan.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value less than that in cell touched", "[on = selects less than; off]", commandName, "toggleLT")) {
			boolean current = lessthan.getValue();
			lessthan.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Wand (data)";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to select taxa automatically.";
	}
}






/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.WandBlock;
import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/*   to do:
	- deal with Option cursor, Shift cursor
	- pass taxaAreRows into this
 */


/* ======================================================================== */
public class WandBlock extends DataWindowAssistantI {
	TableTool wandTool;
	CharacterData data;
	MesquiteTable table;
	boolean defaultAllDataCells = false;
	MesquiteBoolean allDataCells;
	//MesquiteMenuItemSpec eItem, gItem, lItem;
	//MesquiteCollator collator;
	 public String getFunctionIconPath(){
   		 return getPath() + "wandBlock.gif";
   	 }
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			wandTool = new TableTool(this, "magicWand", getPath(), "wandBlock.gif", 4,4,"Select cells in a block", "This tool selects cells in a block of cells.", MesquiteModule.makeCommand("wandTouch",  this) , null, null);
			((MesquiteWindow)containerOfModule()).addTool(wandTool);
			wandTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching oning button
		}
		else return false;
		allDataCells = new MesquiteBoolean(defaultAllDataCells);
		addCheckMenuItem(null, "From first data cell to last", makeCommand("toggleAllDataCells",  this), allDataCells);
//		collator = new MesquiteCollator();
		return true;
	}
	/*.................................................................................................................*
	public boolean loadModule(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (allDataCells.getValue()!=defaultAllDataCells)
			temp.addLine("toggleAllDataCells " + allDataCells.toOffOnString());
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(),  "Touches on a cell with the wand to select cells similar as defined by the options", "[column touched][row touched]", commandName, "wandTouch")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column= MesquiteInteger.fromString(arguments, io);
			int row= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isNonNegative(column)&& (MesquiteInteger.isNonNegative(row))) {
				boolean optionDown = arguments.indexOf("option")>=0;
				boolean commandSelected =  (arguments.indexOf("command")>=0);
				boolean subtractFromSelection = commandSelected && table.isCellSelected(column, row);
				if ((arguments.indexOf("shift")<0) && !commandSelected)
					table.deselectAll();

				table.offAllEdits();
				if (allDataCells.getValue()!=optionDown)
					selectAllDataCellsInTaxon(column, row);
				else
					selectBlockInTaxon(column, row);
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				

			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects from the first data cell to the last in a taxon", "[on = all data cells; off]", commandName, "toggleAllDataCells")) {
			allDataCells.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void selectBlockInTaxon(int column, int row) {
		if (data == null || table == null)
			return;
		if (!data.isInapplicable(column,  row)) {
			for (int ic=column; ic>=0; ic--){
				if (!data.isInapplicable(ic, row))
					table.selectCell(ic, row);
				else
					break;
			}
			for (int ic=column+1; ic<data.getNumChars(); ic++){
				if (!data.isInapplicable(ic, row))
					table.selectCell(ic, row);
				else
					break;
			}
		}
	}
	/*.................................................................................................................*/
	public void selectAllDataCellsInTaxon(int column, int row) {
		if (data == null || table == null)
			return;
		int icStart = data.firstApplicable(row);
		int icEnd = data.lastApplicable(row);
		if (icStart>=0 && icEnd<data.getNumChars())
			for (int ic=icStart; ic<=icEnd; ic++)
				table.selectCell(ic, row);
	}

		/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Select Block (data)";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to select cells in a block in a matrix.";
	}
}






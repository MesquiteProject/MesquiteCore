/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.PopupStateSelector; //DRM started April 02

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class PopupStateSelector extends DataWindowAssistantI implements ToolKeyListener {
	TableTool popupStateSelectorTool;
	CategoricalData data;
	MesquiteTable table;
	MesquiteCommand keyCommand;
	int firstColumnTouched = -2;
	int firstRowTouched = -2;
	CategoricalState fillState;

	public String getFunctionIconPath(){
		return getPath() + "quickKeySelector.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			popupStateSelectorTool = new TableTool(this, "quickKeySelector", getPath(), "quickKeySelector.gif", 4,4,"Select and type", "This tool allows you to select cells; any keystrokes will then be entered into selected cells.", MesquiteModule.makeCommand("fillTouchCell",  this), null, null);  //MesquiteModule.makeCommand("quickKeySelectorTouch",  this) 
			popupStateSelectorTool.setToolKeyListener(this);
			popupStateSelectorTool.setUseTableTouchRules(true);
			//quickKeySelectorTool.setAllowAnnotate(true);
			((MesquiteWindow)containerOfModule()).addTool(popupStateSelectorTool);
			popupStateSelectorTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching oning button
		}
		else return false;
		return true;
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
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
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		if (data instanceof CategoricalData)
			this.data = (CategoricalData)data;
		else
			this.data = null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "P Entry";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to quickly enter data.  If this tool is active, then typing a key will cause that value to be entered into all selected cells.";
	}
	/*.................................................................................................................*/
	public void paintSelectedCells(MesquiteTable table, CharacterData data) {
		
		UndoReference undoReference = new UndoReference(data,this, new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});

		if (fillState != null) {
			boolean success = false;
			if (table.anyCellSelected()) {
				if (table.isCellSelected(firstColumnTouched, firstRowTouched)) {
					for (int i=0; i<table.getNumColumns(); i++)
						for (int j=0; j<table.getNumRows(); j++)
							if (table.isCellSelected(i,j)) {
								data.setState(i,j, fillState);
							}
					success = true;
				}
			}
			if (table.anyRowSelected()) {
				if (table.isRowSelected(firstRowTouched)) {
					for (int j=0; j<table.getNumRows(); j++) {
						if (table.isRowSelected(j))
							for (int i=0; i<table.getNumColumns(); i++)
								data.setState(i,j, fillState);
					}
					success = true;
				}
			}
			if (table.anyColumnSelected()) {
				if (table.isColumnSelected(firstColumnTouched)) {
					for (int i=0; i<table.getNumColumns(); i++){
						if (table.isColumnSelected(i))
							for (int j=0; j<table.getNumRows(); j++) 
								data.setState(i,j, fillState);
					}
					success = true;
				}
			}

			if (success){
				table.repaintAll();
				data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, null, undoReference));
			}

		}
	}
	/*.................................................................................................................*/

	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Fill touched cell or selected cells with current paint states", "[column touched] [row touched]", commandName, "fillTouchCell")) {
			if (table!=null && data !=null){
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				MesquiteInteger io = new MesquiteInteger(0);
				firstColumnTouched= MesquiteInteger.fromString(arguments, io);
				firstRowTouched= MesquiteInteger.fromString(arguments, io);
				fillState = new CategoricalState(0);

				if (!table.rowLegal(firstRowTouched)|| !table.columnLegal(firstColumnTouched))
					return null;
				if ((table.isCellSelected(firstColumnTouched, firstRowTouched))||(table.isRowSelected(firstRowTouched))||(table.isColumnSelected(firstColumnTouched))) {
					paintSelectedCells(table, data); // the touched cell or column is selected; therefore, just fill the selection.
					firstColumnTouched= -2;
					firstRowTouched= -2;
				}
				else {
					table.selectCell(firstColumnTouched, firstRowTouched);
					table.deSelectAndRedrawOutsideBlock(firstColumnTouched, firstRowTouched, firstColumnTouched, firstRowTouched);
				}
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

}






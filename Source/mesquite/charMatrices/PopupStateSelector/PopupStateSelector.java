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
public class PopupStateSelector extends DataWindowAssistantI  {
	TableTool popupStateSelectorTool;
	CategoricalData data;
	MesquiteTable table;
	MesquiteCommand keyCommand;
	int columnTouched = -2;
	int rowTouched = -2;
	MesquiteWindow window;
	CategoricalState fillState;
	MesquitePopup popup;
	MesquiteCommand respondCommand;

	public String getFunctionIconPath(){
		return getPath() + "popUpStateSelector.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			popupStateSelectorTool = new TableTool(this, "popUpStateSelector", getPath(), "popUpStateSelector.gif", 4,4,"Popup Menu State Entry", "This tool allows you to set states in a cell using a popup menu.", MesquiteModule.makeCommand("touchCell",  this), null, null);   
			popupStateSelectorTool.setWorksOnColumnNames(false);
			popupStateSelectorTool.setWorksOnRowNames(false);
			popupStateSelectorTool.setWorksOnMatrixPanel(true);
			popupStateSelectorTool.setWorksOnCornerPanel(false);
			window = (MesquiteWindow)containerOfModule();
			window.addTool(popupStateSelectorTool);
			respondCommand = makeCommand("respond", this);
		}
		else return false;
		return true;
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
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
		return "Popup Menu State Entry";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to quickly enter data by providing a popup menu of states.";
	}
	/*.................................................................................................................*/
	void addToPopup(String s,int response){
		if (popup==null)
			return;
		popup.addItem(s, this, respondCommand, Integer.toString(response));
	}
	/*.................................................................................................................*/
	void addToPopup(String s,MesquiteCommand command, int response){
		if (popup==null)
			return;
		popup.addItem(s, this, command, Integer.toString(response));
	}

	/*.................................................................................................................*/
	public void paintSelectedCells(MesquiteTable table, CharacterData data) {

		UndoReference undoReference = new UndoReference(data,this, new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});

		if (fillState != null) {
			boolean success = false;
			if (table.anyCellSelected()) {
				if (table.isCellSelected(columnTouched, rowTouched)) {
					for (int i=0; i<table.getNumColumns(); i++)
						for (int j=0; j<table.getNumRows(); j++)
							if (table.isCellSelected(i,j)) {
								data.setState(i,j, fillState);
							}
					success = true;
				}
			}
			if (table.anyRowSelected()) {
				if (table.isRowSelected(rowTouched)) {
					for (int j=0; j<table.getNumRows(); j++) {
						if (table.isRowSelected(j))
							for (int i=0; i<table.getNumColumns(); i++)
								data.setState(i,j, fillState);
					}
					success = true;
				}
			}
			if (table.anyColumnSelected()) {
				if (table.isColumnSelected(columnTouched)) {
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


	static int missingResponse = -200;
	static int inapplicableResponse = -201;
	static int extraMenuItems = 5;

	/*.................................................................................................................*/
	private void addStateNamesToPopup(int ic) {
		int responseNumber = 0;

		for (int i=0; i<CategoricalState.maxCategoricalState && i<data.maxStateWithName(ic)+extraMenuItems; i++) {
			addToPopup(data.getStateName(ic, i), responseNumber++);
		}

		popup.addSeparator();
		addToPopup(" " + data.getUnassignedSymbol(), missingResponse);
		addToPopup(" " + data.getInapplicableSymbol(), inapplicableResponse);


	}
	/*.................................................................................................................*/

	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Fill touched cell or selected cells with current paint states", "[column touched] [row touched]", commandName, "touchCell")) {
			if (table!=null && data !=null){
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				MesquiteInteger io = new MesquiteInteger(0);

				columnTouched= MesquiteInteger.fromString(arguments, io);
				rowTouched= MesquiteInteger.fromString(arguments, io);
				if (popup==null)
					popup = new MesquitePopup(table.getMatrixPanel());
				popup.removeAll();

				addStateNamesToPopup(columnTouched);

				if (!table.rowLegal(rowTouched)|| !table.columnLegal(columnTouched))
					return null;

				if ((table.isCellSelected(columnTouched, rowTouched))||(table.isRowSelected(rowTouched))||(table.isColumnSelected(columnTouched))) {
					table.deSelectAndRedrawOutsideBlock(columnTouched, 0, columnTouched, table.getNumRows()-1);
				}
				else {
					table.selectCell(columnTouched, rowTouched);
					table.redrawCell(columnTouched, rowTouched);
					table.deSelectAndRedrawOutsideBlock(columnTouched, rowTouched, columnTouched, rowTouched);
				}

				popup.showPopup(table.getBounds().x+table.getColumnX(columnTouched), table.getBounds().y+table.getRowY(rowTouched));


			}
		}
		else if (checker.compare(this.getClass(), "Responds to choice of popup menu", "[choice number]", commandName, "respond")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int response = MesquiteInteger.fromString(arguments, io);
			fillState=null;
			if (response == missingResponse) {
				fillState = new CategoricalState();
				fillState.setToUnassigned();
			}
			else if (response == inapplicableResponse) {
				fillState = new CategoricalState();
				fillState.setToInapplicable();
			}
			else if (response>=0)			
				fillState = new CategoricalState(CategoricalState.makeSet(response));

			if (fillState!=null) {
				if ((table.isCellSelected(columnTouched, rowTouched))||(table.isRowSelected(rowTouched))||(table.isColumnSelected(columnTouched))) {
					paintSelectedCells(table, data); // the touched cell or column is selected; therefore, just fill the selection.
					columnTouched= -2;
					rowTouched= -2;
				}
				else {
					paintSelectedCells(table, data); // the touched cell or column is selected; therefore, just fill the selection.
					columnTouched= -2;
					rowTouched= -2;
				}
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 340;  
	}

}






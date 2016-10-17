/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.DataPainter; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class DataPainter extends DataWindowAssistantI {
	MesquiteTable table;
	CharacterData data;
	CharacterState fillState;
	boolean neverFilled = true;
	TableTool fillTool;
	TableTool dropperTool;
	int firstColumnTouched = -2;
	int firstRowTouched = -2;
	public void getSubfunctions(){
		registerSubfunction(new FunctionExplanation("Fill selected cells (bucket tool)", "(A tool in the character matrix editor) Fills touched or selected cells with text", null, getRootImageDirectoryPath() + "bucket.gif"));
		registerSubfunction(new FunctionExplanation("Remember text (eyedropper tool)", "(A tool in the character matrix editor) Sets the text to be filled by the bucket to that touched", null, getRootImageDirectoryPath() + "dropper.gif"));
		super.getSubfunctions();
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			MesquiteCommand dragCommand = MesquiteModule.makeCommand("fillDragCell",  this);
			dragCommand.setSuppressLogging(true);
			fillTool = new TableTool(this, "fill", MesquiteModule.getRootImageDirectoryPath(), "bucket.gif", 13,14,"Fill with missing data", "This tool fills selected cells with text.  The text to be used can be determined by touching the tool button and selecting the menu item, or by using the dropper tool.", MesquiteModule.makeCommand("fillTouchCell",  this) , dragCommand, MesquiteModule.makeCommand("fillDropCell",  this));
			fillTool.setOptionsCommand(MesquiteModule.makeCommand("fillOptions",  this));
			dropperTool = new TableTool(this, "dropper", MesquiteModule.getRootImageDirectoryPath(),"dropper.gif", 1,14,"Copy states", "This tool fills the paint bucket with the text in the cell touched on", MesquiteModule.makeCommand("dropperTouchCell",  this) , null, null);
			((MesquiteWindow)containerOfModule()).addTool(fillTool);
			((MesquiteWindow)containerOfModule()).addTool(dropperTool);
		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		return true;
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
		this.data = data;
		fillState = data.makeCharacterState();
	}

	/*.................................................................................................................*/
	public void paintSelectedCells(MesquiteTable table, CharacterData data) {
		
		UndoReference undoReference = new UndoReference(data,this, new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});

		if (fillState != null && !neverFilled) {
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

				if (!table.rowLegal(firstRowTouched)|| !table.columnLegal(firstColumnTouched))
					return null;
				if (neverFilled){
					String fillString = MesquiteString.queryString(containerOfModule(), "Fill cells", "Fill cells with states:", "");
					if (StringUtil.blank(fillString))
						return null;
					neverFilled = false;
					fillState = data.getCharacterState(fillState,0, 0); //just to have a template
					fillState.setValue(fillString, data);
					fillTool.setDescription("Fill with \"" + fillState.toString()+ " \"");
					dropperTool.setDescription("Copy state (current: " + fillState.toString() + ")");
					((MesquiteWindow)containerOfModule()).toolTextChanged();
				}
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
		else if (checker.compare(this.getClass(), "Tracking paint bucket as it is dragged.", "[column dragged] [row dragged]", commandName, "fillDragCell")) {
			if (table!=null && data !=null && (firstColumnTouched>=0)&& (firstRowTouched>=0)){
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				MesquiteInteger io = new MesquiteInteger(0);
				int columnDragged = MesquiteInteger.fromString(arguments, io);
				int rowDragged= MesquiteInteger.fromString(arguments, io);
				if (!table.rowLegal(rowDragged)|| !table.columnLegal(columnDragged))
					return null;
				table.deSelectAndRedrawOutsideBlock(firstColumnTouched, firstRowTouched, columnDragged, rowDragged);
				table.selectBlock(firstColumnTouched, firstRowTouched, columnDragged, rowDragged);
				table.redrawBlock(firstColumnTouched, firstRowTouched, columnDragged, rowDragged);
			}
		}
		else if (checker.compare(this.getClass(), "Filling cells with paint after the paint bucket is no longer dragged.", "[column dropped] [row dropped]", commandName, "fillDropCell")) {
			if (table!=null && data !=null && (firstColumnTouched>=0)&& (firstRowTouched>=0)){
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				MesquiteInteger io = new MesquiteInteger(0);
				int columnDropped = MesquiteInteger.fromString(arguments, io);
				int rowDropped= MesquiteInteger.fromString(arguments, io);
				if (!table.rowLegal(rowDropped)|| !table.columnLegal(columnDropped))
					return null;
				paintSelectedCells(table, data); // the touched cell or column is selected; therefore, just fill the selection.
			}
		}
		else if (checker.compare(this.getClass(), "Queries the user for the paint states", null, commandName, "touchTool")) {
			if (table!=null && data !=null){
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				String fillString;
				if (!fillState.isUnassigned())
					fillString = MesquiteString.queryString(containerOfModule(), "Fill state", "State with which to fill using paint bucket:", fillState.toDisplayString());
				else
					fillString = MesquiteString.queryString(containerOfModule(), "Fill state", "State with which to fill using paint bucket:", "");
				if (StringUtil.blank(fillString))
					return null;
				neverFilled = false;
				fillState = data.getCharacterState(fillState,0, 0); //just to have a template
				fillState.setValue(fillString, data);
				fillTool.setDescription("Fill with \"" + fillState.toDisplayString()+ " \"");
				dropperTool.setDescription("Copy state (current: " + fillState.toDisplayString() + ")");
				((MesquiteWindow)containerOfModule()).toolTextChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Queries the user what paint to use", null, commandName, "fillOptions")) {
			if (data.isEditInhibited()){
				discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
				return null;
			}
			MesquiteButton button = fillTool.getButton();
			if (button!=null){
				MesquiteInteger io = new MesquiteInteger(0);
				int x= MesquiteInteger.fromString(arguments, io); //getting x and y from arguments
				int y= MesquiteInteger.fromString(arguments, io);
				MesquitePopup popup = new MesquitePopup(button);
				popup.addItem("Specify Fill State...", this, MesquiteModule.makeCommand("touchTool", this));
				popup.showPopup(x,y+6);
			}

		}
		else if (checker.compare(this.getClass(), "Sets the paint states to those in the cell touched", "[column touched] [row touched]", commandName, "dropperTouchCell")) {
			if (table!=null && data !=null && fillTool != null && dropperTool != null){
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				MesquiteInteger io = new MesquiteInteger(0);
				int column= MesquiteInteger.fromString(arguments, io);
				int row= MesquiteInteger.fromString(arguments, io);
				if (!table.rowLegal(row)|| !table.columnLegal(column))
					return null;
				neverFilled = false;
				fillState = data.getCharacterState(fillState,column, row);
				if (fillState == null)
					return null;
				fillTool.setDescription("Fill with \"" + fillState.toString()+ " \"");
				dropperTool.setDescription("Copy state (current: " + fillState.toString() + ")");
				if (((MesquiteWindow)containerOfModule()) != null)
					((MesquiteWindow)containerOfModule()).toolTextChanged();
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Data Painter";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies the paint can tool and dropper tool for filling cells in a character data editor." ;
	}

}



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.SelectedBlockMover;




import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.ui.MesquiteCursor;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.categ.lib.*;
import mesquite.align.lib.*;


/*
 * The block selector might be easier if it highlighted the block during
the process of selection so that you can see more exactly which
sequences you were including, but that's a very minor point and not
critical.

 * */

/* ======================================================================== */
public  class SelectedBlockMover extends MultiBlockMoveBase {
	protected SelectedBlockTool moveTool;

	boolean defaultSelectWholeSequences = false;
	MesquiteBoolean selectWholeSequences =new MesquiteBoolean(defaultSelectWholeSequences);



	public Class getDutyClass() {
		return SelectedBlockMover.class;
	}
	public String getDutyName() {
		return "Selected Block Mover";
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			MesquiteCommand touchCommand = MesquiteModule.makeCommand("moveTouchCell",  this);
			touchCommand.setSuppressLogging(true);
			MesquiteCommand dragCommand = MesquiteModule.makeCommand("moveDragCell",  this);
			dragCommand.setSuppressLogging(true);
			MesquiteCommand dropCommand = MesquiteModule.makeCommand("moveDropCell",  this);
			dropCommand.setSuppressLogging(true);
			moveTool = new SelectedBlockTool(this, "selectedBlockMover", getPath(), "SelectedBlockMover.gif", 8,8,"CrossHair.gif",8,8,"Moves a selected block of multiple sequences","This tool moves selected blocks of sequences for manual alignment.", touchCommand , dragCommand, dropCommand);
			setOptionTools();
			moveTool.setDeselectIfOutsideOfCells(false);
			moveTool.setAcceptsOutsideDrops(true);
			((MesquiteWindow)containerOfModule()).addTool(moveTool);
			moveTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching on button
		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		//addPopUpMenuItems();
		addBasicMultiSequenceMenuItems();
		addCheckMenuItem(null, "Select entire sequences", makeCommand("toggleWholeSequences",  this), selectWholeSequences);
		return true;
	}
	public void initialize(MesquiteTable table, CharacterData data) {
		currentBlock = new CellBlock((CategoricalData)data, table);
		currentBlock.setRight(true);
		currentBlock.setLeft(true);
		data.addListener(this);
		inhibitionChanged();
	}
	/*.................................................................................................................*/
	public void addExtraSnapshotItems(Snapshot temp) {
		if (selectWholeSequences.getValue()!=defaultSelectWholeSequences)
			temp.addLine("toggleWholeSequences " + selectWholeSequences.toOffOnString());
	}
	/* ................................................................................................................. */
	public void inhibitionChanged(){
		if (moveTool!=null && data!=null)
			moveTool.setEnabled(!data.isEditInhibited());
	}

	/*.................................................................................................................*/
	public void setOptionTools(){
	}
	/*.................................................................................................................*/
	public boolean canMoveLeft(){
		if (currentBlock==null)
			return false;
		return (!currentBlock.isLocked()&&currentBlock.getCurrentLeftMovement()<currentBlock.getMaxLeftMovement());
	}
	/*.................................................................................................................*/
	public boolean canMoveRight(){
		if (currentBlock==null)
			return false;
		return (!currentBlock.isLocked()&&currentBlock.getCurrentRightMovement()<currentBlock.getMaxRightMovement());
	}
	/*.................................................................................................................*/
	public boolean wholeSelectedBlock(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean wholeSequence(){
		return selectWholeSequences.getValue();
	}
	/*.................................................................................................................*/
	protected void redrawTable() {
		table.redrawOldAndNewBlocks(currentBlock.getPreviousFirstCharInBlock(), firstSequenceInBlock, currentBlock.getPreviousLastCharInBlock(), lastSequenceInBlock, currentBlock.getCurrentFirstCharInBlock(), firstSequenceInBlock, currentBlock.getCurrentLastCharInBlock(), lastSequenceInBlock,true);
	}

	/*.................................................................................................................*/
	public  void getFirstAndLastSequences(boolean optionDown){
		MesquiteInteger firstRow = new MesquiteInteger();
		MesquiteInteger lastRow = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();
		MesquiteInteger lastColumn = new MesquiteInteger();

		if (table.findBlockSurroundingCell(firstColumnTouched, firstRowTouched, firstRow,  lastRow,  firstColumn,  lastColumn) ) {
			if (firstColumnTouched>=firstColumn.getValue() && firstColumnTouched<=lastColumn.getValue() && firstRowTouched>=firstRow.getValue() && firstRowTouched<=lastRow.getValue()){
				firstSequenceInBlock = firstRow.getValue();
				lastSequenceInBlock = lastRow.getValue();
			}

		}
		else {
			firstSequenceInBlock = MesquiteInteger.unassigned;
			lastSequenceInBlock = MesquiteInteger.unassigned;
		}
	}
	/*.................................................................................................................*/

	public  Bits getWhichTaxa(boolean optionDown){
		Bits whichTaxa = new Bits(data.getNumTaxa());
		getFirstAndLastSequences(optionDown);
		if (!MesquiteInteger.isCombinable(firstSequenceInBlock) || !MesquiteInteger.isCombinable(lastSequenceInBlock))
			return null;
		for (int it=firstSequenceInBlock; it<=lastSequenceInBlock; it++)
			whichTaxa.setBit(it);
		return whichTaxa;

	}

	/*.................................................................................................................*/
	public  boolean acceptableSelection(){
		MesquiteInteger firstRow = new MesquiteInteger();
		MesquiteInteger lastRow = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();
		MesquiteInteger lastColumn = new MesquiteInteger();
		if (table.findBlockSurroundingCell(firstColumnTouched, firstRowTouched, firstRow,  lastRow,  firstColumn,  lastColumn) ) {
			return table.boundedBlockSelected( firstRow,  lastRow,  firstColumn,  lastColumn);
		}
		return false;

	}
	/*.................................................................................................................*/
	public boolean findBlocks(boolean optionDown) {
		//get rectangular selected block of which firstRowTouched, firstColumnTouched is a part.

		MesquiteInteger firstRow = new MesquiteInteger();
		MesquiteInteger lastRow = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();
		MesquiteInteger lastColumn = new MesquiteInteger();

		if (table.findBlockSurroundingCell(firstColumnTouched, firstRowTouched, firstRow,  lastRow,  firstColumn,  lastColumn) ) {
			if (firstColumnTouched>=firstColumn.getValue() && firstColumnTouched<=lastColumn.getValue() && firstRowTouched>=firstRow.getValue() && firstRowTouched<=lastRow.getValue()){
				currentBlock.setAllBlocks(firstColumn.getValue() , lastColumn.getValue() , firstRow.getValue(), lastRow.getValue());
				currentBlock.setWhichTaxa(firstRow.getValue(), lastRow.getValue());

				currentBlock.setMaximumMovements();
				return true;
			}

		}
		return false;
	}
	/*.................................................................................................................*/
	public  boolean mouseDown(boolean optionDown, boolean shiftDown){
		betweenCells = false;
		leftEdge = false;
		rightEdge = false;

		if (table.isCellSelected(firstColumnTouched, firstRowTouched)) {  //then we are in the selected part between columns, about to start a move, if possible
			choosingNewSelection = false;
			if (!acceptableSelection()) {
				currentlyMoving=false;
				return false;
			}
			if (!prepareToMoveMultiSequences(optionDown)){
				currentlyMoving=false;
				return false;
			}
		}	else {
			choosingNewSelection = true;
			table.deselectAll();
			table.selectCell(firstColumnTouched, firstRowTouched);
		}
		return true;

	}
	/*.................................................................................................................*/
	public  void trackNewSelection(int column, int row){
		MesquiteInteger firstInBlock= new MesquiteInteger();
		MesquiteInteger lastInBlock= new MesquiteInteger();
		MesquiteBoolean cellHasInapplicable = new MesquiteBoolean(false);
		MesquiteBoolean leftIsInapplicable = new MesquiteBoolean(false);
		MesquiteBoolean rightIsInapplicable = new MesquiteBoolean(false);
		int firstColumn = MesquiteInteger.minimum(firstColumnTouched, column);
		int lastColumn = MesquiteInteger.maximum(firstColumnTouched, column);
		int firstRow = MesquiteInteger.minimum(firstRowTouched, row);
		int lastRow = MesquiteInteger.maximum(firstRowTouched, row);
		if (!data.isInapplicable(firstColumnTouched, firstRowTouched) && !data.isInapplicable(column, row)) {
			currentBlock.getCellBlock(firstColumn, lastColumn, firstRow, lastRow, firstInBlock, lastInBlock,  wholeSelectedBlock(), wholeSequence(), wholeSequence(), cellHasInapplicable, leftIsInapplicable, rightIsInapplicable);
			table.selectBlock(firstInBlock.getValue(),firstRow, lastInBlock.getValue(), lastRow);
			table.repaintCells(liveUpdate.getValue());
		}
	}

	/*.................................................................................................................*/
	public  boolean mouseDragged(int columnDragged, int rowDragged,	int percentHorizontal, int percentVertical){
		if (choosingNewSelection) {
			trackNewSelection(columnDragged, rowDragged);

		} else if (currentlyMoving) {
			dragMultiSequences(percentHorizontal,  rowDragged,  columnDragged);
		}
		return true;
	}
	/*.................................................................................................................*/
	public  boolean mouseDropped(int columnDropped, int rowDropped,	int percentHorizontal, int percentVertical){
		if (choosingNewSelection) {
			if (rowDropped == table.getMatrixPanel().BEYONDMATRIX) rowDropped = table.numRowsTotal-1;
			if (rowDropped<0) rowDropped=0;
		} else if (!currentlyMoving){
			table.deselectAllNotify();
		}
		if (!table.rowLegal(rowDropped)|| !table.columnLegal(columnDropped))
			return false;
		if (choosingNewSelection) {
			trackNewSelection(columnDropped, rowDropped);

			choosingNewSelection=false;
		} else if (currentlyMoving){
			afterMoveMultiSequences();
		} else stopMoving();
		return true;
	}



	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Toggles whether the entire sequences are moved.", "[on = moveWholeSequences; off]", commandName, "toggleWholeSequences")) {
			selectWholeSequences.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Selected Block Mover";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Moves selected blocks of multiple sequences." ;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}






	public class SelectedBlockTool extends TableTool {
		MesquiteCursor crossHairCursor=null;


		public SelectedBlockTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String extraImageFileName, int extraHotX, int extraHotY, String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand dragCommand, MesquiteCommand droppedCommand) {
			super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation, touchedCommand, dragCommand, droppedCommand);
			this.initiator = initiator;
			setDeselectIfOutsideOfCells(true);
			setHasTemporaryOptionCursor(true);
			setHasTemporaryShiftCursor(true);

			this.name = name;

			crossHairCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, extraImageFileName, extraHotX, extraHotY);
		}
		public void turningOff(){
		}

		public void cursorInCell(int modifiers, int column, int row, int regionInCellH, int regionInCellV, EditorPanel panel){
			if (table.isCellSelected(column, row))  {
				setCurrentStandardCursor(null);
			}
			else {
				setCurrentStandardCursor(crossHairCursor);
			}
		}

	}


}



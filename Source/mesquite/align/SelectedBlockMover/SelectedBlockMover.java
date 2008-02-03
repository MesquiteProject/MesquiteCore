package mesquite.align.SelectedBlockMover;


/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.
	 Version 1.11, June 2006.
	 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
	 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
	 Perhaps with your help we can be more than a few, and make Mesquite better.

	 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
	 Mesquite's web site is http://mesquiteproject.org

	 This source code and its compiled class files are free and modifiable under the terms of 
	 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.align.lib.*;

/* ======================================================================== */
public  class SelectedBlockMover extends MultiBlockMoveBase {
	protected SelectedBlockTool moveTool;

	boolean defaultMoveWholeSequences = false;
	MesquiteBoolean moveWholeSequences =new MesquiteBoolean(defaultMoveWholeSequences);

	

	public Class getDutyClass() {
		return SelectedBlockMover.class;
	}
	public String getDutyName() {
		return "Selected Block Mover";
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			moveTool = new SelectedBlockTool(this, "selectedBlockMover", getPath(), "SelectedBlockMover.gif", 8,8,"CrossHair.gif",8,8,"Moves a selected block of multiple sequences","This tool moves selected blocks of sequences for manual alignment.", MesquiteModule.makeCommand("moveTouchCell",  this) , MesquiteModule.makeCommand("moveDragCell",  this), MesquiteModule.makeCommand("moveDropCell",  this));
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
		addCheckMenuItem(null, "Move entire sequences", makeCommand("toggleWholeSequences",  this), moveWholeSequences);
		return true;
	}
	public void initialize(MesquiteTable table, CharacterData data) {
		currentBlock = new CellBlock((CategoricalData)data, table);
		currentBlock.setRight(true);
		currentBlock.setLeft(true);
	}
	/*.................................................................................................................*/
	public void addExtraSnapshotItems(Snapshot temp) {
		if (moveWholeSequences.getValue()!=defaultMoveWholeSequences)
			temp.addLine("toggleWholeSequences " + moveWholeSequences.toOffOnString());
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
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
		return moveWholeSequences.getValue();
	}
	/*.................................................................................................................*/

	protected void stopMoving() {
		currentlyMoving = false;
		table.repaintAll();
	}
	/*.................................................................................................................*/
	public  void getFirstAndLastSequences(){
		MesquiteInteger firstRow = new MesquiteInteger();
		MesquiteInteger lastRow = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();
		MesquiteInteger lastColumn = new MesquiteInteger();

		if (table.singleCellBlockSelected( firstRow,  lastRow,  firstColumn,  lastColumn) ) {
			if (firstColumnTouched>=firstColumn.getValue() && firstColumnTouched<=lastColumn.getValue() && firstRowTouched>=firstRow.getValue() && firstRowTouched<=lastRow.getValue()){
				firstSequenceInBlock = firstRow.getValue();
				lastSequenceInBlock = lastRow.getValue();
			}
				
		}
	}
	/*.................................................................................................................*/
	public boolean findBlocks() {
		//get rectangular selected block of which firstRowTouched, firstColumnTouched is a part.
	
		MesquiteInteger firstRow = new MesquiteInteger();
		MesquiteInteger lastRow = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();
		MesquiteInteger lastColumn = new MesquiteInteger();

		if (table.singleCellBlockSelected( firstRow,  lastRow,  firstColumn,  lastColumn) ) {
			if (firstColumnTouched>=firstColumn.getValue() && firstColumnTouched<=lastColumn.getValue() && firstRowTouched>=firstRow.getValue() && firstRowTouched<=lastRow.getValue()){
				currentBlock.setAllBlocks(firstColumn.getValue() , lastColumn.getValue() , firstRow.getValue(), lastRow.getValue());
				currentBlock.setMaximumMovements();
				return true;
			}
				
		}
		return false;
	}
	/*.................................................................................................................*
	public void getXYFromCommandInfo(int column, int row, int percentHorizontal, int percentVertical, MesquiteInteger x, MesquiteInteger y){
		int Y = table.getRowY(row) + (int)(table.getRowHeight(row)*(0.01*percentVertical));
		int X = table.getColumnX(column) + (int)(table.getColumnWidth(column)*(0.01*percentHorizontal));
		if (x!=null) x.setValue(X);
		if (y!=null) y.setValue(Y);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		MesquiteInteger x= new MesquiteInteger();
		MesquiteInteger y = new MesquiteInteger();
		if (checker.compare(this.getClass(), "Touched.", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveTouchCell")) {

			if (table!=null && data !=null && currentBlock !=null){
				optionDown = arguments.indexOf("option")>=0;
				MesquiteInteger io = new MesquiteInteger(0);
				firstColumnTouched= MesquiteInteger.fromString(arguments, io);
				firstRowTouched= MesquiteInteger.fromString(arguments, io);

				firstTouchPercentHorizontal= MesquiteInteger.fromString(arguments, io);
				firstTouchPercentVertical= MesquiteInteger.fromString(arguments, io);

				betweenCells = false;
				leftEdge = false;
				rightEdge = false;

				if (table.isCellSelected(firstColumnTouched, firstRowTouched)) {  //then we are in the selected part between columns, about to start a move, if possible
					choosingNewSelection = false;
					if (!prepareToMoveMultiSequences()){
						currentlyMoving=false;
						return null;
					}
				}	else {
					choosingNewSelection = true;
					table.deselectAll();
					table.selectCell(firstColumnTouched, firstRowTouched);
				}

			}
		}
		else if (checker.compare(this.getClass(), "Dragging", "[column dragged] [row dragged] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveDragCell")) {
			if (table!=null && data !=null){
				MesquiteInteger io = new MesquiteInteger(0);
				int columnDragged = MesquiteInteger.fromString(arguments, io);
				int rowDragged= MesquiteInteger.fromString(arguments, io);
				int percentHorizontal= MesquiteInteger.fromString(arguments, io);
				int percentVertical= MesquiteInteger.fromString(arguments, io);

				if (choosingNewSelection) {
					previousRowDragged = rowDragged;

					table.deselectAllCells(false);
					table.selectBlock(MesquiteInteger.minimum(firstColumnTouched, columnDragged), MesquiteInteger.minimum(firstRowTouched, rowDragged),MesquiteInteger.maximum(firstColumnTouched, columnDragged),MesquiteInteger.maximum(firstRowTouched, rowDragged));

				//table.selectBlock(MesquiteInteger.minimum(firstColumnTouched, columnDragged), MesquiteInteger.minimum(firstRowTouched, rowDragged),MesquiteInteger.maximum(firstColumnTouched, columnDragged),MesquiteInteger.maximum(firstRowTouched, rowDragged));

					
				} else if (currentlyMoving) {
					dragMultiSequences(percentHorizontal,  rowDragged,  columnDragged);
					//table.deselectAllCells(false);
					currentBlock.deselectOthersAndSelectBlock();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Dropping.", "[column dropped] [row dropped] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveDropCell")) {
			if (table!=null && data !=null && (firstColumnTouched>=0)&& (firstRowTouched>=0)){
				MesquiteInteger io = new MesquiteInteger(0);
				int columnDropped = MesquiteInteger.fromString(arguments, io);
				int rowDropped= MesquiteInteger.fromString(arguments, io);
				if (choosingNewSelection) {
					if (rowDropped == table.getMatrixPanel().BEYONDMATRIX) rowDropped = table.numRowsTotal-1;
					if (rowDropped<0) rowDropped=0;
				}
				if (!table.rowLegal(rowDropped)|| !table.columnLegal(columnDropped))
					return null;
				if (choosingNewSelection) {
					GraphicsUtil.shimmerVerticalOn(table.getGraphics(), table.getMatrixPanel(),  table.getRowY(firstRowTouched),  table.getRowY(rowDropped), table.getColumnX(firstColumnTouched));
					
					MesquiteInteger firstInBlock= new MesquiteInteger();
					MesquiteInteger lastInBlock= new MesquiteInteger();
					MesquiteBoolean cellHasInapplicable = new MesquiteBoolean(false);
					MesquiteBoolean leftIsInapplicable = new MesquiteBoolean(false);
					MesquiteBoolean rightIsInapplicable = new MesquiteBoolean(false);
					int firstColumn = MesquiteInteger.minimum(firstColumnTouched, columnDropped);
					int lastColumn = MesquiteInteger.maximum(firstColumnTouched, columnDropped);
					int firstRow = MesquiteInteger.minimum(firstRowTouched, rowDropped);
					int lastRow = MesquiteInteger.maximum(firstRowTouched, rowDropped);
					currentBlock.getCellBlock(firstColumn, lastColumn, firstRow, lastRow, firstInBlock, lastInBlock,  wholeSelectedBlock(), wholeSequence(), wholeSequence(), cellHasInapplicable, leftIsInapplicable, rightIsInapplicable);
					table.selectBlock(firstInBlock.getValue(),firstRowTouched, lastInBlock.getValue(), rowDropped);
					table.repaintAll();
					
					choosingNewSelection=false;
				} else if (currentlyMoving){
					moveMultiSequences();
				} else stopMoving();
			}
		}
		else if (checker.compare(this.getClass(), "Toggles whether the entire sequences are moved.", "[on = moveWholeSequences; off]", commandName, "toggleWholeSequences")) {
			moveWholeSequences.toggleValue(parser.getFirstToken(arguments));
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






	public class SelectedBlockTool extends TableTool {
		MesquiteCursor crossHairCursor=null;


		public SelectedBlockTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String extraImageFileName, int extraHotX, int extraHotY, String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand dragCommand, MesquiteCommand droppedCommand) {
			super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation, touchedCommand, dragCommand, droppedCommand);
			this.initiator = initiator;
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



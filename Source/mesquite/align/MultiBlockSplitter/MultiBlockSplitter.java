package mesquite.align.MultiBlockSplitter;


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
public  class MultiBlockSplitter extends MultiBlockMoveBase {
	protected MultiBlockTool moveTool;
	CellBlock leftCellBlock =null;
	CellBlock rightCellBlock =null;

	boolean defaultMoveWholeSequenceOnOneSide = false;
	MesquiteBoolean moveWholeSequenceOnOneSide =new MesquiteBoolean(defaultMoveWholeSequenceOnOneSide);

	protected int endOfLeftBlockAtTouch = 0;
	protected int startOfRightBlockAtTouch = 0;


	public Class getDutyClass() {
		return MultiBlockSplitter.class;
	}
	public String getDutyName() {
		return "Multi Block Splitter";
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			moveTool = new MultiBlockTool(this, "multiBlockSplitter", getPath(), "MultiBlockSplitter.gif", 8,8,"CrossHair.gif",8,8,"Splits multiple sequences","This tool splits and moves blocks of sequences for manual alignment.", MesquiteModule.makeCommand("moveTouchCell",  this) , MesquiteModule.makeCommand("moveDragCell",  this), MesquiteModule.makeCommand("moveDropCell",  this));
			setOptionTools();
			moveTool.setDeselectIfOutsideOfCells(false);
			moveTool.setAcceptsOutsideDrops(true);
			moveTool.setOptionImageFileName( "multiBlockSplitterOption.gif", 8, 8);

			((MesquiteWindow)containerOfModule()).addTool(moveTool);
			moveTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching on button
		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		addBasicMultiSequenceMenuItems();
		addCheckMenuItem(null, "Move full sequences on each side", makeCommand("toggleAllOnSide",  this), moveWholeSequenceOnOneSide);
		return true;
	}
	/*.................................................................................................................*/
	public void addExtraSnapshotItems(Snapshot temp) {
		if (moveWholeSequenceOnOneSide.getValue()!=defaultMoveWholeSequenceOnOneSide)
			temp.addLine("toggleAllOnSide " + moveWholeSequenceOnOneSide.toOffOnString());
	}
	/*.................................................................................................................*/
	public void setOptionTools(){
	}
	/*.................................................................................................................*/
	public boolean canMoveLeft(){
		if (leftCellBlock==null || rightCellBlock==null)
			return false;
		return (!leftCellBlock.isLocked()&&leftCellBlock.getCurrentLeftMovement()<leftCellBlock.getMaxLeftMovement());
	}
	/*.................................................................................................................*/
	public boolean canMoveRight(){
		if (leftCellBlock==null || rightCellBlock==null)
			return false;
		return (!rightCellBlock.isLocked()&&rightCellBlock.getCurrentRightMovement()<rightCellBlock.getMaxRightMovement());
	}
	public void clearBetweenSelection() {
		table.clearBetweenColumnSelection();
		table.clearBetweenRowSelection();
		table.repaintAll();
	}
	/*.................................................................................................................*/
	public boolean wholeSelectedBlock(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean wholeSequenceToLeft(){
		return moveWholeSequenceOnOneSide.getValue();
	}
	/*.................................................................................................................*/
	public boolean wholeSequenceToRight(){
		return moveWholeSequenceOnOneSide.getValue();
	}
	/*.................................................................................................................*
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	public void initialize(MesquiteTable table, CharacterData data) {
		leftCellBlock = new CellBlock((CategoricalData)data, table);
		leftCellBlock.setLeft(true);
		leftCellBlock.setRight(false);
		rightCellBlock = new CellBlock((CategoricalData)data, table);
		rightCellBlock.setRight(true);
		rightCellBlock.setLeft(false);

	}
	/*.................................................................................................................*/
	protected void stopMoving() {
		table.clearBetweenColumnSelection();
		super.stopMoving();
	}
	/*.................................................................................................................*/
	void switchBlocks(boolean switchToRight) {
		if (switchToRight) {
			if (!rightCellBlock.isLocked()) {
				leftCellBlock.restoreCharBlock(dataChanged);
				currentBlock = rightCellBlock;
				if (leftEdge)
					effectiveFirstColumnTouched = firstColumnTouched;
				else
					effectiveFirstColumnTouched = firstColumnTouched+1;
				currentlyMovingRight = true;
			}
		} else {
			if (!leftCellBlock.isLocked()) {
				rightCellBlock.restoreCharBlock(dataChanged);
				currentBlock = leftCellBlock;
				if (leftEdge)
					effectiveFirstColumnTouched = firstColumnTouched-1;
				else
					effectiveFirstColumnTouched = firstColumnTouched;
				currentlyMovingRight = false;
			}
		}
		currentMoveFromOriginal = 0;

	}
	/*.................................................................................................................*/
	public void checkSwitchBlocks(int moveFromOriginal) {

		if (canMoveRight() && moveFromOriginal>0 &&  !currentlyMovingRight) {  //was dragging left, now switch to dragging right
			switchBlocks(true);
		}
		else if (canMoveLeft() && moveFromOriginal<0 && currentlyMovingRight) {  //was dragging right, now switch to dragging left
			switchBlocks(false);
		}

	}
	/*.................................................................................................................*/
	public void addCharactersToBlocks(int added, boolean toStart) {
		leftCellBlock.addCharacters(Math.abs(added),toStart);
		rightCellBlock.addCharacters(Math.abs(added),toStart);
	}

	/*.................................................................................................................*/
	protected void redrawTable() {
		table.redrawOldAndNewBlocks(currentBlock.getPreviousFirstCharInBlock(), currentBlock.getPreviousLastCharInBlock(), currentBlock.getCurrentFirstCharInBlock(), currentBlock.getCurrentLastCharInBlock(), currentBlock.getWhichTaxa(),false);
	}
	/*.................................................................................................................*/
	public void resetBlocks() {
		leftCellBlock.reset();
		rightCellBlock.reset();
	}
	public  void getFirstAndLastSequences(boolean optionDown){
		if (!optionDown) {
			firstSequenceInBlock = table.getStartBetweenRowSelection();
			lastSequenceInBlock = table.getEndBetweenRowSelection();
		}
		else {
			firstSequenceInBlock = 0;
			lastSequenceInBlock = data.getNumTaxa()-1;
		}
	}
	public  Bits getWhichTaxa(boolean optionDown){
		Bits whichTaxa = new Bits(data.getNumTaxa());
		for (int it=table.getStartBetweenRowSelection(); it<=table.getEndBetweenRowSelection(); it++)
			whichTaxa.setBit(it);
		if (optionDown) 
			whichTaxa.invertAllBits();
		return whichTaxa;
		
	}

	/*.................................................................................................................*/
	public boolean findBlocks(boolean optionDown) {
		MesquiteInteger firstInBlock= new MesquiteInteger();
		MesquiteInteger lastInBlock= new MesquiteInteger();
		MesquiteBoolean cellHasInapplicable = new MesquiteBoolean(false);
		MesquiteBoolean leftIsInapplicable = new MesquiteBoolean(false);
		MesquiteBoolean rightIsInapplicable = new MesquiteBoolean(false);
		int startOfBlock = table.numColumnsTotal;
		int endOfBlock = 0;

		leftCellBlock.setWhichTaxa(table.getStartBetweenRowSelection(), table.getEndBetweenRowSelection());
		if (optionDown)
			leftCellBlock.reverseWhichTaxa();
		leftCellBlock.getCellBlock(endOfLeftBlockAtTouch, endOfLeftBlockAtTouch, leftCellBlock.getWhichTaxa(), firstInBlock, lastInBlock,  wholeSelectedBlock(), wholeSequenceToLeft(), wholeSequenceToRight(), cellHasInapplicable, leftIsInapplicable, rightIsInapplicable);
		startOfBlock = MesquiteInteger.minimum(startOfBlock,firstInBlock.getValue());
		rightCellBlock.setWhichTaxa(table.getStartBetweenRowSelection(), table.getEndBetweenRowSelection());
		if (optionDown)
			rightCellBlock.reverseWhichTaxa();
		rightCellBlock.getCellBlock(startOfRightBlockAtTouch,startOfRightBlockAtTouch, rightCellBlock.getWhichTaxa(), firstInBlock, lastInBlock,  wholeSelectedBlock(), wholeSequenceToLeft(), wholeSequenceToRight(), cellHasInapplicable, leftIsInapplicable, rightIsInapplicable);
		endOfBlock = MesquiteInteger.maximum(endOfBlock,lastInBlock.getValue());

		if (rightIsInapplicable.getValue()&& leftIsInapplicable.getValue()) {
			firstColumnTouched = -1;
			return false;
		}
		if (leftIsInapplicable.getValue()){
			leftCellBlock.setLocked(true);
			currentlyMovingRight=true;
		}
		if (rightIsInapplicable.getValue()) {
			rightCellBlock.setLocked(true);
			currentlyMovingRight=false;
		}

		leftCellBlock.setAllBlocks(startOfBlock, firstColumnTouched, firstSequenceInBlock, lastSequenceInBlock);
		rightCellBlock.setAllBlocks(firstColumnTouched+1, endOfBlock, firstSequenceInBlock, lastSequenceInBlock);
		leftCellBlock.setMaximumMovements();
		rightCellBlock.setMaximumMovements();

		if (currentlyMovingRight)
			currentBlock=rightCellBlock;
		else
			currentBlock = leftCellBlock;
		return true;
	}
	/*.................................................................................................................*/

	public void getSplitRows(MesquiteInteger top, MesquiteInteger bottom, int row, int percentVertical, boolean onDrop) {
		if (row == table.getMatrixPanel().BEYONDMATRIX) row = table.numRowsTotal-1;
		if (row<0) row=0;
		int topRow = firstRowTouched;
		int bottomRow = row;
		if (firstRowTouched<row){ // we've gone down;
			topRow = firstRowTouched;
			bottomRow = row;
			if (firstTouchPercentVertical>60)
				topRow++;
			if (percentVertical<40)
				bottomRow--;
		}
		else if (firstRowTouched>row){ // we've gone up;
			topRow = row;
			bottomRow = firstRowTouched;
			if (firstTouchPercentVertical<40)
				bottomRow--;
			if (percentVertical>60)
				topRow++;
		} 
		else if (!onDrop)
			if (firstTouchPercentVertical < percentVertical){
				if (firstTouchPercentVertical>60)
					topRow++;
				if (percentVertical<40)
					bottomRow--;
			}
			else if (firstTouchPercentVertical > percentVertical){
				if (firstTouchPercentVertical<40)
					bottomRow--;
				if (percentVertical>60)
					topRow++;
			}
		if (topRow<0)
			topRow=0;
		if (bottomRow>=table.numRowsTotal)
			bottomRow=table.numRowsTotal-1;
		if (top!=null)
			top.setValue(topRow);
		if (bottom!=null)
			bottom.setValue(bottomRow);
	}
	int previousDrawTop =-1;
	int previousDrawBottom=-1 ;

	/*.................................................................................................................*/
	public boolean mouseDown(boolean optionDown, boolean shiftDown) {
		effectiveFirstColumnTouched = firstColumnTouched;
		endOfLeftBlockAtTouch = firstColumnTouched;
		startOfRightBlockAtTouch = firstColumnTouched;

		betweenCells = false;
		leftEdge = false;
		rightEdge = false;
		if (firstTouchPercentHorizontal<=edgePercent) {  //at far left edge of cell
			endOfLeftBlockAtTouch = firstColumnTouched-1;
			leftEdge = true;
			firstColumnTouched --;
			currentlyMovingRight=true;
			betweenCells=true;
		} else if (firstTouchPercentHorizontal>=(100-edgePercent)) {  //at far right edge of cell
			startOfRightBlockAtTouch = firstColumnTouched+1;
			rightEdge = true;
			currentlyMovingRight=false;
			betweenCells=true;
		}

		if (table.inBetweenSelectionRowColumns(firstColumnTouched, firstRowTouched)) {  //then we are in the selected part between columns, about to start a move, if possible
			choosingNewSelection = false;
			if (!prepareToMoveMultiSequences(optionDown)){
				currentlyMoving=false;
				return false;
			}
		}	else {
			choosingNewSelection = true;
			table.deselectAll();
			previousDrawTop = -1;
			previousDrawBottom = -1;
		}
		return true;
	}
	/*.................................................................................................................*/
	public boolean mouseDragged(int columnDragged, int rowDragged,	int percentHorizontal, int percentVertical) {
		if (choosingNewSelection) {
			MesquiteInteger topRow = new MesquiteInteger();
			MesquiteInteger bottomRow = new MesquiteInteger();
			getSplitRows(topRow, bottomRow, rowDragged, percentVertical, false);

			if (topRow.isCombinable() && bottomRow.isCombinable()) {
				if (previousDrawTop>0 && previousDrawBottom>0) {
					if (previousDrawTop<topRow.getValue())
						table.redrawBlock(firstColumnTouched, previousDrawTop, firstColumnTouched+1, topRow.getValue()-1);
					if (previousDrawBottom>bottomRow.getValue())
						table.redrawBlock(firstColumnTouched, bottomRow.getValue()+1, firstColumnTouched+1, previousDrawBottom);
				}
				Graphics g = table.getMatrixPanel().getGraphics();
				int x = table.getColumnX(firstColumnTouched)-MesquiteTable.BETWEENLINEWIDTH/2;
				int y = table.getRowY(topRow.getValue()-1)+1;
				int width = MesquiteTable.BETWEENLINEWIDTH;
				int height =  table.getRowY(bottomRow.getValue()) - y;
				g.setColor(Color.black);
				g.fillRect(x, y, width, height);
				previousDrawTop = topRow.getValue();
				previousDrawBottom = bottomRow.getValue();

			}
			previousRowDragged = rowDragged;

		} else if (currentlyMoving) {
			dragMultiSequences(percentHorizontal,  rowDragged,  columnDragged);
		}
		return true;
	}
	/*.................................................................................................................*/
	public boolean mouseDropped(int columnDropped, int rowDropped,	int percentHorizontal, int percentVertical) {
		if (choosingNewSelection) {
			if (rowDropped == table.getMatrixPanel().BEYONDMATRIX) rowDropped = table.numRowsTotal-1;
			if (rowDropped<0) rowDropped=0;
		} else {
			table.deselectAllNotify();
		}
		if (!table.rowLegal(rowDropped)|| !table.columnLegal(columnDropped))
			return false;
		if (choosingNewSelection) {
			table.setStartBetweenColumnSelection(firstColumnTouched);
			table.setEndBetweenColumnSelection(firstColumnTouched);


			MesquiteInteger topRow = new MesquiteInteger();
			MesquiteInteger bottomRow = new MesquiteInteger();
			getSplitRows(topRow, bottomRow, rowDropped, percentVertical, true);

			if (topRow.isCombinable() && bottomRow.isCombinable() && topRow.getValue()<=bottomRow.getValue()) {
				table.setStartBetweenRowSelection(topRow.getValue());
				table.setEndBetweenRowSelection(bottomRow.getValue());
				table.repaintAll();
			}

			choosingNewSelection=false;
		} else if (currentlyMoving){
			moveMultiSequences();
		} else stopMoving();
		return true;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Toggles whether the entire sequences on one side are moved.", "[on = moveWholeSequenceOnOneSide; off]", commandName, "toggleAllOnSide")) {
			moveWholeSequenceOnOneSide.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Splits multi-sequence block";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Splits blocks of multiple sequences." ;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}






	public class MultiBlockTool extends TableTool {
		MesquiteCursor crossHairCursor=null;
		MesquiteCursor optionEdgeCursor=null;
		MesquiteCursor optionBetweenSelectionCursor = null;


		public MultiBlockTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String extraImageFileName, int extraHotX, int extraHotY, String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand dragCommand, MesquiteCommand droppedCommand) {
			super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation, touchedCommand, dragCommand, droppedCommand);
			this.initiator = initiator;
			this.name = name;
			setDeselectIfOutsideOfCells(true);
			setHasTemporaryOptionCursor(true);
			setHasTemporaryShiftCursor(true);


			crossHairCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, extraImageFileName, extraHotX, extraHotY);
			optionBetweenSelectionCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, "multiBlockSplitterOption.gif", 8, 8);

		}
		public void setOptionEdgeCursor(String extraImageFileName, int extraHotX, int extraHotY) {
			optionEdgeCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, extraImageFileName, extraHotX, extraHotY);
		}
		public void turningOff(){
			((MultiBlockSplitter)initiator).clearBetweenSelection();
		}

		public void cursorInCell(int modifiers, int column, int row, int regionInCellH, int regionInCellV, EditorPanel panel){
			if (table.inBetweenSelection(column, row, regionInCellH, regionInCellV))  {
				setCurrentStandardCursor(null);
				if (optionBetweenSelectionCursor!=null && MesquiteEvent.optionKeyDown(modifiers)) {
					setCurrentOptionCursor(optionBetweenSelectionCursor);
				}
			}
			else {
				setCurrentOptionCursor(crossHairCursor);
				setCurrentStandardCursor(crossHairCursor);
				if (optionEdgeCursor!=null) {
					setCurrentOptionCursor(optionEdgeCursor);
				}
			}
		}

	}


}



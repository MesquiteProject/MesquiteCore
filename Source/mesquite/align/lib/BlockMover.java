/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.lib; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public abstract class BlockMover extends DataWindowAssistantI {
	protected MesquiteTable table;
	CharacterData  data;
	int currentNumChars = 0;
	int currentNumTaxa = 0;

	protected AlignTool moveTool;
	CellBlock cellBlock =null;
	boolean defaultCanExpand = false;
	MesquiteBoolean canExpand =new MesquiteBoolean(defaultCanExpand);

	MesquiteBoolean warnCheckSum = new MesquiteBoolean(true);
	long originalCheckSum;
	int edgePercent = 20;
	int previousPercentHorizontal=0;
	int previousColumnDragged=-2;
	int firstColumnTouched = -2;
	int effectiveFirstColumnTouched = -2;
	int firstRowTouched = -2;
	boolean betweenCells = false;
	boolean atLeftEdgeOfCell = false;
	boolean atRightEdgeOfCell = false;
	boolean alreadyMoved = false;
	boolean lastWasMoveRight=false;
	boolean optionDown = false;
	MesquiteBoolean liveUpdate;
	boolean defaultLiveUpdate = false;
	MesquiteBoolean dataChanged = new MesquiteBoolean(false);
	UndoReference undoReference = null;

	public Class getDutyClass() {
		return BlockMover.class;
	}
	public String getDutyName() {
		return "Block Mover";
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
			moveTool = new AlignTool(this, getToolName(), getPath(), getCellToolImageFileName(), getCellToolHotSpot().x,getCellToolHotSpot().y,getSplitToolImageFileName(), getSplitToolHotSpot().x,getSplitToolHotSpot().y,getFullDescriptionForTool(),getExplanationForTool(), touchCommand, dragCommand, dropCommand);
			setOptionTools();
			//moveTool.setUseTableTouchRules(true);
			((MesquiteWindow)containerOfModule()).addTool(moveTool);
			moveTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching on button
		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		addPopUpMenuItems();
		liveUpdate = new MesquiteBoolean(defaultLiveUpdate);
		addCheckMenuItem(null, "Live update of analyses, etc.", makeCommand("toggleLiveUpdate",  this), liveUpdate);
		addCheckMenuItem(null, "Can expand matrix", makeCommand("toggleCanExpand",  this), canExpand);
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public abstract String getToolName();
	/*.................................................................................................................*/
	public abstract String getCellToolImageFileName();
	/*.................................................................................................................*/
	public abstract Point getCellToolHotSpot();
	/*.................................................................................................................*/
	public abstract String getSplitToolImageFileName();
	/*.................................................................................................................*/
	public abstract Point getSplitToolHotSpot();
	/*.................................................................................................................*/
	public abstract String getExplanationForTool();
	/*.................................................................................................................*/
	public abstract String getFullDescriptionForTool();
	/*.................................................................................................................*/
	public void addPopUpMenuItems(){
	}
	/*.................................................................................................................*/
	public void setOptionTools(){
	}
	/*.................................................................................................................*/
	public boolean allowSplits(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean canMoveLeft(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean canMoveRight(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean wholeSelectedBlock(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean wholeSequenceToLeft(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean wholeSequenceToRight(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean getOptionDown(){
		return optionDown;
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		//David:  add compatibility check for CategoricalData
		if (!(data instanceof CategoricalData))
			return;
		this.table = table;
		this.data = data;
		currentNumChars = data.getNumChars();
		currentNumTaxa = data.getNumTaxa();
		initialize(table,data);
	}
	/*.................................................................................................................*/
	public void initialize(MesquiteTable table, CharacterData data){
		cellBlock = new mesquite.lib.characters.CellBlock((CategoricalData)data, table);
		currentNumChars = data.getNumChars();
		currentNumTaxa = data.getNumTaxa();

	}
	/*.................................................................................................................*/
	public void checkCellBlockIntegrity(MesquiteTable table, CharacterData data){
		if (data.getNumChars()!=currentNumChars || data.getNumTaxa()!=currentNumTaxa) {
			initialize(table,data);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (liveUpdate.getValue()!=defaultLiveUpdate)
			temp.addLine("toggleLiveUpdate " + liveUpdate.toOffOnString());
		if (canExpand.getValue()!=defaultCanExpand)
			temp.addLine("toggleCanExpand " + canExpand.toOffOnString());
		return temp;
	}
	boolean movingRight = false;
	int storedPreviousColumnDragged = MesquiteInteger.unassigned;
	int storedPreviousPercentHorizontal = MesquiteInteger.unassigned;
	int gapsAvailableToRight = 0;
	int gapsAvailableToLeft = 0;
	boolean directionRecorded = false;
	boolean pleaseDefineCellBlock = false;


	/*.................................................................................................................*/
	private boolean mouseDragged(String arguments) {
		MesquiteInteger io = new MesquiteInteger(0);
		int columnDragged = MesquiteInteger.fromString(arguments, io);
		int rowDragged= MesquiteInteger.fromString(arguments, io);

		int percentHorizontal= MesquiteInteger.fromString(arguments, io);
		if (!table.rowLegal(rowDragged)|| !table.columnLegal(columnDragged) || (previousColumnDragged == columnDragged && previousPercentHorizontal == percentHorizontal))
			return false;
		if (!canMoveRight() && columnDragged>effectiveFirstColumnTouched)
			columnDragged = effectiveFirstColumnTouched;
		if (!canMoveLeft() && columnDragged<effectiveFirstColumnTouched)
			columnDragged = effectiveFirstColumnTouched;
		int moveFromOriginal = columnDragged-effectiveFirstColumnTouched;
		//	boolean switchDirections = movingRight != moveFromOriginal>0;
		movingRight = moveFromOriginal>0;
		if (!directionRecorded) {  // first time dragged, need to record details of move direction for next time through

			if (moveFromOriginal>0 || (moveFromOriginal==0 &&  previousPercentHorizontal > percentHorizontal)) {
				movingRight = true;
				lastWasMoveRight = true;
				directionRecorded = true;
				pleaseDefineCellBlock = true;
			} else if (moveFromOriginal<0 || (moveFromOriginal==0 &&  previousPercentHorizontal < percentHorizontal)) {
				movingRight = false;
				lastWasMoveRight = false;
				directionRecorded = true;
				pleaseDefineCellBlock = true; 
			}

		} else {
			//	boolean switchDirections = movingRight != moveFromOriginal>0;
			int moveFromPrevious = columnDragged-previousColumnDragged;
			previousColumnDragged = columnDragged;
			previousPercentHorizontal = percentHorizontal;
			MesquiteBoolean isTerminalBlock = new MesquiteBoolean(false);
			MesquiteInteger boundaryOfAvailableSpace = new MesquiteInteger(0);

			if (canMoveRight() && betweenCells && (moveFromOriginal>0 || (moveFromOriginal==0 && previousPercentHorizontal > percentHorizontal)) && (!lastWasMoveRight || pleaseDefineCellBlock)) {  //was dragging left, now switch to dragging right


				if (alreadyMoved)
					cellBlock.restoreCharBlock(dataChanged);
				if (atLeftEdgeOfCell)
					effectiveFirstColumnTouched = firstColumnTouched;
				else
					effectiveFirstColumnTouched = firstColumnTouched+1;
				moveFromOriginal = columnDragged-effectiveFirstColumnTouched;
				cellBlock.switchCharBlock(effectiveFirstColumnTouched,cellBlock.getOriginalLastCharInFullBlock());
				gapsAvailableToRight = data.checkCellMoveDistanceAvailable(data.getNumChars()-cellBlock.getCurrentLastCharInBlock(), cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentLastCharInBlock(), firstRowTouched,firstRowTouched, isTerminalBlock, boundaryOfAvailableSpace, canExpand.getValue());
				gapsAvailableToLeft = data.checkCellMoveDistanceAvailable(-cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentLastCharInBlock(), firstRowTouched,firstRowTouched, isTerminalBlock, boundaryOfAvailableSpace, canExpand.getValue());

				lastWasMoveRight = true;
				alreadyMoved = false;
				if (MesquiteInteger.isCombinable(storedPreviousColumnDragged))
					previousColumnDragged = storedPreviousColumnDragged;
				if (MesquiteInteger.isCombinable(storedPreviousPercentHorizontal))
					previousPercentHorizontal = storedPreviousPercentHorizontal;
				pleaseDefineCellBlock = false;
			}
			else if (canMoveLeft() && betweenCells && (moveFromOriginal<0 || (moveFromOriginal==0 && previousPercentHorizontal < percentHorizontal)) &&  (lastWasMoveRight || pleaseDefineCellBlock)) {  //was dragging right, now switch to dragging left


				if (alreadyMoved)
					cellBlock.restoreCharBlock(dataChanged);
				if (atLeftEdgeOfCell)
					effectiveFirstColumnTouched = firstColumnTouched-1;
				else
					effectiveFirstColumnTouched = firstColumnTouched;
				moveFromOriginal = columnDragged-effectiveFirstColumnTouched;

				cellBlock.switchCharBlock(cellBlock.getOriginalFirstCharInFullBlock(),effectiveFirstColumnTouched);
				gapsAvailableToRight = data.checkCellMoveDistanceAvailable(data.getNumChars()-cellBlock.getCurrentLastCharInBlock(), cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentLastCharInBlock(), firstRowTouched,firstRowTouched, isTerminalBlock, boundaryOfAvailableSpace, canExpand.getValue());
				gapsAvailableToLeft = data.checkCellMoveDistanceAvailable(-cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentLastCharInBlock(), firstRowTouched,firstRowTouched, isTerminalBlock, boundaryOfAvailableSpace, canExpand.getValue());

				lastWasMoveRight = false;
				alreadyMoved = false;
				if (MesquiteInteger.isCombinable(storedPreviousColumnDragged))
					previousColumnDragged = storedPreviousColumnDragged;
				if (MesquiteInteger.isCombinable(storedPreviousPercentHorizontal))
					previousPercentHorizontal = storedPreviousPercentHorizontal;
				pleaseDefineCellBlock = false;
			}
			else  if (moveFromOriginal>= gapsAvailableToLeft && moveFromOriginal <= gapsAvailableToRight) {
				cellBlock.setCurrentCharBlock(cellBlock.getOriginalFirstCharInBlock()+moveFromOriginal,cellBlock.getOriginalLastCharInBlock()+ moveFromOriginal);

				if (moveFromPrevious != 0) {  // move it over from previous position by this amount; at least, that is the request
					int redrawStartBlock = cellBlock.getPreviousFirstCharInBlock();
					int redrawEndBlock = cellBlock.getPreviousLastCharInBlock();
					alreadyMoved= true;

					int distanceToMove = data.checkCellMoveDistanceAvailable(moveFromPrevious, cellBlock.getPreviousFirstCharInBlock(), cellBlock.getPreviousLastCharInBlock(), firstRowTouched,firstRowTouched, isTerminalBlock, boundaryOfAvailableSpace, canExpand.getValue());

					if (distanceToMove!=0) {

						if ((moveFromPrevious > 0 && distanceToMove<= moveFromPrevious) || (moveFromPrevious<0 && distanceToMove>=moveFromPrevious))  {  // we can't move it as much as desired
							cellBlock.setCurrentCharBlock(cellBlock.getPreviousFirstCharInBlock()+distanceToMove,cellBlock.getPreviousLastCharInBlock()+ distanceToMove);
						}
						int showDistance = cellBlock.getCurrentFirstCharInBlock()-cellBlock.getOriginalFirstCharInBlock();
						if (moveFromPrevious>0)
							redrawEndBlock = cellBlock.getCurrentFirstCharInBlock()-1;
						else 
							redrawStartBlock = cellBlock.getCurrentLastCharInBlock()+1;


						int added = data.moveCells(cellBlock.getPreviousFirstCharInBlock(), cellBlock.getPreviousLastCharInBlock(), distanceToMove, firstRowTouched,firstRowTouched, canExpand.getValue(), false, true, false,dataChanged,null, null);
						if (added<0){ //now start adjusting all the values as we may have added taxa at the start of the matrix
							added = -added;
							cellBlock.addToCharBlockValues(added);
							firstColumnTouched += added;
							effectiveFirstColumnTouched += added;
							columnDragged += added;
							previousColumnDragged += added;
						}
						if (added!=0 || liveUpdate.getValue()) {
							Notification notification = new Notification(MesquiteListener.DATA_CHANGED, new int[] {firstRowTouched});
							notification.setSubcodes(new int[] {MesquiteListener.SINGLE_TAXON});
							data.notifyListeners(this, notification);
							data.notifyInLinked(notification);
						}

						//data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
						table.redrawBlock(MesquiteInteger.minimum(cellBlock.getPreviousFirstCharInBlock(),cellBlock.getCurrentFirstCharInBlock()) , firstRowTouched, MesquiteInteger.maximum(cellBlock.getPreviousLastCharInBlock(),cellBlock.getCurrentLastCharInBlock()), firstRowTouched);

						//table.redrawBlockBlank(redrawStartBlock, firstRowTouched, redrawEndBlock, firstRowTouched); //draw the blanks that are opened up
						//table.redrawBlockOffset(firstInBlock.getValue()+showDistance, firstRowTouched, lastInBlock.getValue()+showDistance , firstRowTouched, -showDistance, 0);
					}
				}
			} else {
				if (MesquiteInteger.isCombinable(storedPreviousColumnDragged))
					previousColumnDragged = storedPreviousColumnDragged;
				if (MesquiteInteger.isCombinable(storedPreviousPercentHorizontal))
					previousPercentHorizontal = storedPreviousPercentHorizontal;

			}
			storedPreviousColumnDragged = previousColumnDragged;
			storedPreviousPercentHorizontal = previousPercentHorizontal;
			cellBlock.transferCurrentToPrevious();
		}
		return true;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "move touched cell or selected cells", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveTouchCell")) {
			if (table!=null && data !=null && cellBlock !=null){
				directionRecorded = false;
				pleaseDefineCellBlock = false;
				storedPreviousColumnDragged = MesquiteInteger.unassigned;
				storedPreviousPercentHorizontal = MesquiteInteger.unassigned;

				checkCellBlockIntegrity(table,data);
				adjustSelection();
				optionDown = arguments.indexOf("option")>=0;

				MesquiteInteger io = new MesquiteInteger(0);
				firstColumnTouched= MesquiteInteger.fromString(arguments, io);
				effectiveFirstColumnTouched = firstColumnTouched;
				firstRowTouched= MesquiteInteger.fromString(arguments, io);
				originalCheckSum = ((CategoricalData)data).storeCheckSum(0, data.getNumChars(),firstRowTouched, firstRowTouched);
				int percentHorizontal= MesquiteInteger.fromString(arguments, io);
				betweenCells = false;
				atLeftEdgeOfCell = false;
				atRightEdgeOfCell = false;
				if (percentHorizontal<=edgePercent) {  //at far left edge of cell
					if (allowSplits())
						betweenCells = true;
					atLeftEdgeOfCell = true;
				}
				if (percentHorizontal>=(100-edgePercent)) {  //at far right edge of cell
					if (allowSplits())
						betweenCells = true;
					atRightEdgeOfCell = true;
				}

				previousPercentHorizontal = percentHorizontal;
				MesquiteInteger firstInBlock= new MesquiteInteger();
				MesquiteInteger lastInBlock= new MesquiteInteger();
				MesquiteBoolean cellHasInapplicable = new MesquiteBoolean();
				MesquiteBoolean leftIsInapplicable = new MesquiteBoolean();
				MesquiteBoolean rightIsInapplicable = new MesquiteBoolean();
				cellBlock.getBlockInSequence(firstColumnTouched, firstRowTouched, firstInBlock, lastInBlock,  wholeSelectedBlock(), wholeSequenceToLeft(), wholeSequenceToRight(), cellHasInapplicable, leftIsInapplicable, rightIsInapplicable);
				if (cellHasInapplicable.getValue()){ // then the cell touched has inapplicable data
					firstColumnTouched = -1;
					return null;
				}

				cellBlock.setOriginalFullBlockOnTouch(firstInBlock.getValue(),lastInBlock.getValue(), firstRowTouched,firstRowTouched);
				lastWasMoveRight=false;

				if (betweenCells) {   //set it initially as if we were pulling to the rightho
					if (atLeftEdgeOfCell) 
						firstInBlock.setValue(firstColumnTouched);
					else 
						effectiveFirstColumnTouched = firstColumnTouched+1;
					firstInBlock.setValue(effectiveFirstColumnTouched);
				}
				previousColumnDragged = effectiveFirstColumnTouched;
				cellBlock.setAllBlocks(firstInBlock.getValue(),lastInBlock.getValue(), firstRowTouched,firstRowTouched);
				if (!canExpand.getValue())
					undoReference = new UndoReference(data,this,0,data.getNumChars(), firstRowTouched,firstRowTouched);
				else
					undoReference = new UndoReference(data,this);
				MesquiteBoolean isTerminalBlock = new MesquiteBoolean(false);
				MesquiteInteger boundaryOfAvailableSpace = new MesquiteInteger(0);
				gapsAvailableToRight = data.checkCellMoveDistanceAvailable(data.getNumChars()-cellBlock.getCurrentLastCharInBlock(), cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentLastCharInBlock(), firstRowTouched,firstRowTouched, isTerminalBlock, boundaryOfAvailableSpace, canExpand.getValue());
				gapsAvailableToLeft = data.checkCellMoveDistanceAvailable(-cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentFirstCharInBlock(), cellBlock.getCurrentLastCharInBlock(), firstRowTouched,firstRowTouched, isTerminalBlock, boundaryOfAvailableSpace, canExpand.getValue());
			}
		}
		else if (checker.compare(this.getClass(), "moving dragged.", "[column dragged] [row dragged] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveDragCell")) {
			if (!mouseDragged(arguments))
				return null;
		}
		else if (checker.compare(this.getClass(), "moving cells.", "[column dropped] [row dropped] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveDropCell")) {
			if (table!=null && data !=null && (firstColumnTouched>=0)&& (firstRowTouched>=0)){
				checkCellBlockIntegrity(table,data);
				MesquiteInteger io = new MesquiteInteger(0);
				int columnDropped = MesquiteInteger.fromString(arguments, io);
				int rowDropped= MesquiteInteger.fromString(arguments, io);
				if (!table.rowLegal(rowDropped)|| !table.columnLegal(columnDropped))
					return null;
				//		int moveFromOriginal = columnDropped-firstColumnTouched;
				//		move(firstInBlock.getValue(), lastInBlock.getValue(), moveFromOriginal, firstRowTouched, false);
				boolean success = ((CategoricalData)data).examineCheckSum(0, data.getNumChars(),firstRowTouched, firstRowTouched, "WARNING!  This tool has changed the data inappropriately!", warnCheckSum, originalCheckSum);
				if (dataChanged.getValue()) {
					Notification notification = new Notification(MesquiteListener.DATA_CHANGED, new int[] {firstRowTouched}, undoReference);
					notification.setSubcodes(new int[] {MesquiteListener.SINGLE_TAXON});
					data.notifyListeners(this, notification);
					data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
				}
				if (!success){
					if (undoReference!=null) {
						Undoer[] undoer = undoReference.getUndoer();
						if (undoer!=null && undoer[0]!=null)
							undoer[0].undo();
					}
				}
				table.repaintAll();
			}
		}
		else if (checker.compare(this.getClass(), "Toggles whether live update is active", "[on = live update; off]", commandName, "toggleLiveUpdate")) {
			boolean current = liveUpdate.getValue();
			liveUpdate.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Toggles whether the matrix is allowed to expand if one attempts to move a block beyond the edges of the matrix.", "[on = canExpand; off]", commandName, "toggleCanExpand")) {
			boolean current = canExpand.getValue();
			canExpand.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	protected void adjustSelection() {
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Move block";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Moves blocks in a sequence." ;
	}

}


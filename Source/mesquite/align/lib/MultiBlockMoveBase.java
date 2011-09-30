/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.lib;




import java.awt.Color;
import java.awt.Graphics;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public  abstract class MultiBlockMoveBase extends DataWindowAssistantI {
	protected MesquiteTable table;
	protected CharacterData  data;
	protected CellBlock currentBlock = null;
	protected int currentNumTaxa = 0;
	protected int currentNumChars = 0;

	protected MesquiteBoolean warnCheckSum = new MesquiteBoolean(true);
	protected long originalCheckSum;
	protected int edgePercent = 50;
	protected int previousPercentHorizontal=0;
	protected int previousColumnDragged=-2;
	protected int previousRowDragged = -2;
	protected int firstColumnTouched = -2;
	protected int effectiveFirstColumnTouched = -2;
	protected int firstRowTouched = -2;
	protected boolean betweenCells = false;
	protected boolean leftEdge = false;
	protected boolean rightEdge = false;
	protected boolean currentlyMoving = false;
	protected boolean currentlyMovingRight=false;
	protected boolean optionDown = false;
	protected Bits originalWhichTaxa;

	protected boolean defaultCanExpand = false;
	protected MesquiteBoolean canExpand =new MesquiteBoolean(defaultCanExpand);

	protected boolean defaultLiveUpdate = false;
	protected MesquiteBoolean liveUpdate = new MesquiteBoolean(defaultLiveUpdate);

	protected MesquiteBoolean dataChanged = new MesquiteBoolean(false);



	protected boolean choosingNewSelection = true;
	protected int firstTouchPercentHorizontal;
	protected int firstTouchPercentVertical;
	protected int firstSequenceInBlock;
	protected int lastSequenceInBlock;
	protected int currentMoveFromOriginal = 0;

	protected long pendingCommandsIDLimit = 0;

	/*.................................................................................................................*/
	protected void addBasicMultiSequenceMenuItems() {
		addCheckMenuItem(null, "Live update of analyses, etc.", makeCommand("toggleLiveUpdate",  this), liveUpdate);
		addCheckMenuItem(null, "Can expand matrix", makeCommand("toggleCanExpand",  this), canExpand);
	}
	/*.................................................................................................................*/
	public void addExtraSnapshotItems(Snapshot temp) {
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (liveUpdate.getValue()!=defaultLiveUpdate)
			temp.addLine("toggleLiveUpdate " + liveUpdate.toOffOnString());
		if (canExpand.getValue()!=defaultCanExpand)
			temp.addLine("toggleCanExpand " + canExpand.toOffOnString());
		addExtraSnapshotItems(temp);
		return temp;
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean wholeSelectedBlock(){
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

	public void initialize(MesquiteTable table, CharacterData data) {
		currentBlock = new CellBlock((CategoricalData)data, table);
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
		initialize( table,  data);
	}
	/*.................................................................................................................*/
	public void checkCurrentBlockIntegrity(MesquiteTable table, CharacterData data){
		if (data.getNumChars()!=currentNumChars || data.getNumTaxa()!=currentNumTaxa) {
			initialize(table,data);
			currentNumChars = data.getNumChars();
			currentNumTaxa = data.getNumTaxa();
		}
	}

	/*.................................................................................................................*/
	protected void stopMoving() {
		currentlyMoving = false;
		table.repaintCells(liveUpdate.getValue());
		undoReference = null;
	}
	/*.................................................................................................................*/
	public void addCharactersToBlocks(int added, boolean toStart) {
		currentBlock.addCharacters(Math.abs(added),toStart);
	}

	/*.................................................................................................................*/
	public void resetBlocks() {
		currentBlock.reset();
	}
	/*.................................................................................................................*/
	public void checkSwitchBlocks(int moveFromOriginal) {
	}
	/*.................................................................................................................*/
	public abstract boolean findBlocks(boolean optionDown);

	public abstract void getFirstAndLastSequences(boolean optionDown);
	public  abstract Bits getWhichTaxa(boolean optionDown);

	/*.................................................................................................................*/
	public boolean prepareToMoveMultiSequences(boolean optionDown) {

		getFirstAndLastSequences(optionDown);
		currentMoveFromOriginal = 0;
		originalWhichTaxa = getWhichTaxa(optionDown);
		if (originalWhichTaxa==null)
			return false;
		originalCheckSum = ((CategoricalData)data).storeCheckSum(0, data.getNumChars(),originalWhichTaxa);

		if (!canExpand.getValue())
			undoReference = new UndoReference(data,this,0,data.getNumChars(), firstSequenceInBlock,lastSequenceInBlock);
		else
			undoReference = new UndoReference(data,this);
		resetBlocks();
		previousPercentHorizontal = firstTouchPercentHorizontal;

		if (!findBlocks(optionDown)) 
			return false;


		effectiveFirstColumnTouched = firstColumnTouched;
		previousColumnDragged = effectiveFirstColumnTouched;
		currentlyMoving = true;
		return true;
	}
	/*.................................................................................................................*/
	protected void redrawTable() {
	}
	/*.................................................................................................................*/
	public boolean attemptBlockMove(int candidateMovement) {
		if (currentBlock==null) 
			return false;
		if (candidateMovement !=0) {  // move it over from previous position by this amount; at least, that is the request
			int distanceToMove = currentBlock.movementAllowed(candidateMovement, canExpand.getValue());
			if (distanceToMove!=0) {
				long[] checkSums = null;
				int numTaxaToMove=0;
				Bits taxaToMove = currentBlock.getWhichTaxa();
				if (MesquiteTrunk.debugMode) {
					//logln("Candidate movement: " + candidateMovement + ", memory available: " + MesquiteTrunk.getMaxAvailableMemory());
					logln("Candidate movement: " + candidateMovement);
					numTaxaToMove = taxaToMove.numBitsOn();
					checkSums = new long[numTaxaToMove];
					int count = 0;
					for (int it=0; it<data.getNumTaxa() && count<numTaxaToMove; it++) {
						if (taxaToMove.isBitOn(it)) {
							checkSums[count] = ((CategoricalData)data).storeCheckSum(0, data.getNumChars(),it,it);
							count++;
						}
					}
				}
				int added = data.moveCells(currentBlock.getCurrentFirstCharInBlock(), currentBlock.getCurrentLastCharInBlock(), distanceToMove, taxaToMove, canExpand.getValue(), false, true, false,dataChanged,null);
				if (MesquiteTrunk.debugMode) {
					numTaxaToMove = taxaToMove.numBitsOn();
					int count = 0;
					for (int it=0; it<data.getNumTaxa() && count<numTaxaToMove; it++) {
						if (taxaToMove.isBitOn(it)) {
							long newCheckSum = ((CategoricalData)data).storeCheckSum(0, data.getNumChars(),it,it);
							if (newCheckSum!=checkSums[count]) {
								logln("CHECKSUM of taxon " + (it+1) + " changed!!!!!!");
							}
							count++;
						}
					}
				}
				if (added<0){ //now start adjusting all the values as we may have added taxa at the start of the matrix
					firstColumnTouched -= added;
					effectiveFirstColumnTouched -= added;
					previousColumnDragged -= added;
				} 
				if (added!=0){  //we've added some characters
					addCharactersToBlocks(Math.abs(added), added<0);
					if (MesquiteTrunk.debugMode)
						logln("Number of characters added: " + Math.abs(added));
				}
				currentBlock.adjustToMove(distanceToMove);

				if (added!=0 || liveUpdate.getValue()) {
					data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
					data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
				}

				currentMoveFromOriginal += distanceToMove;

				redrawTable();

				currentBlock.transferCurrentToPrevious();
				pendingCommandsIDLimit = PendingCommand.numInstances;
				MesquiteCommand.deleteSpecifiedCommands(0, pendingCommandsIDLimit, this, "moveDragCell");
			}
		}

		return true;
	}

	/*.................................................................................................................*/
	public boolean dragMultiSequences(int percentHorizontal, int rowDragged, int columnDragged) {
		if (currentBlock==null)
			return false;

		if (!table.rowLegal(rowDragged)|| !table.columnLegal(columnDragged) || (previousColumnDragged == columnDragged && previousPercentHorizontal == percentHorizontal))
			return false;

		double exactMoveFromOriginal = columnDragged-firstColumnTouched;
		exactMoveFromOriginal+= 0.01*(percentHorizontal-firstTouchPercentHorizontal);
		int moveFromOriginal = (int)exactMoveFromOriginal;
		int candidateMovement =  moveFromOriginal - currentMoveFromOriginal;

		previousColumnDragged = columnDragged;
		previousPercentHorizontal = percentHorizontal;

		checkSwitchBlocks(moveFromOriginal);

		return attemptBlockMove(candidateMovement);
	}
	/*.................................................................................................................*/
	public boolean afterMoveMultiSequences() {
		boolean success = ((CategoricalData)data).examineCheckSum(0, data.getNumChars(),currentBlock.getWhichTaxa(), "WARNING! The data have been altered inappropriately by this tool! The changes you have made will be undone.", warnCheckSum, originalCheckSum);
		if (!success) {   //&& MesquiteTrunk.debugMode) 
			logln("WARNING! The data have been altered inappropriately by this tool! The changes you have made will be undone.");
			logln("Original sequences to be moved: " + originalWhichTaxa.getListOfBitsOn(1));
			logln("Sequences moved: " + currentBlock.getWhichTaxa().getListOfBitsOn(1));
		}
		if (dataChanged.getValue()) {
			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, null, undoReference));
			data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));  //TODO: have undo for linked?  or is this automatically taken care of?
		}
		if (!success){
			if (undoReference!=null) {
				Undoer[] undoer = undoReference.getUndoer();
				if (undoer!=null && undoer[0]!=null)
					undoer[0].undo();
			}
		}
		stopMoving();
		return true;
	}

	UndoReference undoReference = null;
	/*.................................................................................................................*/
	public abstract boolean mouseDown(boolean optionDown, boolean shiftDown);
	/*.................................................................................................................*/
	public abstract boolean mouseDragged(int columnDragged, int rowDragged,	int percentHorizontal, int percentVertical);
	/*.................................................................................................................*/
	public abstract boolean mouseDropped(int columnDropped, int rowDropped,	int percentHorizontal, int percentVertical);

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Touched.", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveTouchCell")) {
			if (table!=null && data !=null){
				optionDown = arguments.indexOf("option")>=0;
				boolean shiftDown = arguments.indexOf("shift")>=0;
				MesquiteInteger io = new MesquiteInteger(0);
				firstColumnTouched= MesquiteInteger.fromString(arguments, io);
				firstRowTouched= MesquiteInteger.fromString(arguments, io);
				firstTouchPercentHorizontal= MesquiteInteger.fromString(arguments, io);
				firstTouchPercentVertical= MesquiteInteger.fromString(arguments, io);

				if (!mouseDown(optionDown, shiftDown))
					return null;
			}
		}
		else if (checker.compare(this.getClass(), "Dragging", "[column dragged] [row dragged] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveDragCell")) {
			if (table!=null && data !=null){
				MesquiteInteger io = new MesquiteInteger(0);
				int columnDragged = MesquiteInteger.fromString(arguments, io);
				int rowDragged= MesquiteInteger.fromString(arguments, io);
				int percentHorizontal= MesquiteInteger.fromString(arguments, io);
				int percentVertical= MesquiteInteger.fromString(arguments, io);

				if (!mouseDragged (columnDragged, rowDragged, percentHorizontal, percentVertical)) {
					//undoReference = null;
					return null;
				}

			}
		}
		else if (checker.compare(this.getClass(), "Dropping.", "[column dropped] [row dropped] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveDropCell")) {
			if (table!=null && data !=null && (firstColumnTouched>=0)&& (firstRowTouched>=0)){
				MesquiteInteger io = new MesquiteInteger(0);
				int columnDropped = MesquiteInteger.fromString(arguments, io);
				int rowDropped= MesquiteInteger.fromString(arguments, io);
				int droppedPercentHorizontal= MesquiteInteger.fromString(arguments, io);
				int droppedPercentVertical= MesquiteInteger.fromString(arguments, io);
				if (!mouseDropped (columnDropped, rowDropped, droppedPercentHorizontal, droppedPercentVertical))
					//undoReference = null;
					return null;

			}
		}
		else if (checker.compare(this.getClass(), "Toggles whether live update is active", "[on = live update; off]", commandName, "toggleLiveUpdate")) {
			liveUpdate.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Toggles whether the matrix is allowed to expand if one attempts to move a block beyond the edges of the matrix.", "[on = canExpand; off]", commandName, "toggleCanExpand")) {
			canExpand.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}


}



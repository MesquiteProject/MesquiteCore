package mesquite.align.lib;


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

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public  abstract class MultiBlockMoveBase extends DataWindowAssistantI {
	protected MesquiteTable table;
	protected CharacterData  data;
	protected CellBlock currentBlock = null;

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

	protected boolean defaultCanExpand = true;
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
		initialize( table,  data);
	}

	/*.................................................................................................................*/
	protected void stopMoving() {
		currentlyMoving = false;
//		table.clearBetweenColumnSelection();
		table.repaintAll();
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
	public abstract boolean findBlocks();

	public abstract void getFirstAndLastSequences();

	/*.................................................................................................................*/
	public boolean prepareToMoveMultiSequences() {
		getFirstAndLastSequences();
		currentMoveFromOriginal = 0;
		originalCheckSum = ((CategoricalData)data).storeCheckSum(0, data.getNumChars(),firstSequenceInBlock, lastSequenceInBlock);
		resetBlocks();
		previousPercentHorizontal = firstTouchPercentHorizontal;

		if (!findBlocks())
			return false;


		effectiveFirstColumnTouched = firstColumnTouched;
		previousColumnDragged = effectiveFirstColumnTouched;
		currentlyMoving = true;
		return true;
	}
	/*.................................................................................................................*/
	public boolean attemptBlockMove(int candidateMovement) {
		if (currentBlock==null) 
			return false;
		if (candidateMovement !=0) {  // move it over from previous position by this amount; at least, that is the request
			int distanceToMove = currentBlock.movementAllowed(candidateMovement, canExpand.getValue());

			if (distanceToMove!=0) {
				int added = data.moveCells(currentBlock.getCurrentFirstCharInBlock(), currentBlock.getCurrentLastCharInBlock(), distanceToMove, firstSequenceInBlock, lastSequenceInBlock, canExpand.getValue(), false, true, false,dataChanged);
				if (added<0){ //now start adjusting all the values as we may have added taxa at the start of the matrix
					firstColumnTouched -= added;
					effectiveFirstColumnTouched -= added;
					previousColumnDragged -= added;
				} 
				if (added!=0){  //we've added some characters
					addCharactersToBlocks(Math.abs(added), added<0);
				}
				currentBlock.adjustToMove(distanceToMove);

				if (added!=0 || liveUpdate.getValue()) {
					data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
					data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
				}

				currentMoveFromOriginal += distanceToMove;

				table.redrawBlock(MesquiteInteger.minimum(currentBlock.getPreviousFirstCharInBlock(),currentBlock.getCurrentFirstCharInBlock()) , firstSequenceInBlock, MesquiteInteger.maximum(currentBlock.getPreviousLastCharInBlock(),currentBlock.getCurrentLastCharInBlock()), lastSequenceInBlock);

				currentBlock.transferCurrentToPrevious();
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
	public boolean moveMultiSequences() {
		((CategoricalData)data).examineCheckSum(0, data.getNumChars(),firstSequenceInBlock, lastSequenceInBlock, "Bad checksum!", warnCheckSum, originalCheckSum);
		if (dataChanged.getValue()) {
			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
		}
		stopMoving();
		return true;
	}


	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Toggles whether live update is active", "[on = live update; off]", commandName, "toggleLiveUpdate")) {
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



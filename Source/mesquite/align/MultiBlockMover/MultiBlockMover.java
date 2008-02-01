package mesquite.align.MultiBlockMover;


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
	import mesquite.lib.duties.*;
	import mesquite.lib.table.*;
	import mesquite.categ.lib.*;
	import mesquite.align.lib.*;

	/* ======================================================================== */
	public  class MultiBlockMover extends DataWindowAssistantI {
		MesquiteTable table;
		CharacterData  data;
		protected MultiBlockTool moveTool;
		CellBlock currentBlock = null;
		CellBlock leftCellBlock =null;
		CellBlock rightCellBlock =null;
		boolean defaultCanExpand = true;
		MesquiteBoolean canExpand =new MesquiteBoolean(defaultCanExpand);

		MesquiteBoolean warnCheckSum = new MesquiteBoolean(true);
		long originalCheckSum;
		int edgePercent = 50;
		int previousPercentHorizontal=0;
		int previousColumnDragged=-2;
		int previousRowDragged = -2;
		int firstColumnTouched = -2;
		int effectiveFirstColumnTouched = -2;
		int firstRowTouched = -2;
		boolean betweenCells = false;
		boolean leftEdge = false;
		boolean rightEdge = false;
		boolean currentlyMoving = false;
		boolean currentlyMovingRight=false;
		boolean optionDown = false;
		MesquiteBoolean liveUpdate;
		boolean defaultLiveUpdate = false;
		MesquiteBoolean dataChanged = new MesquiteBoolean(false);


		public Class getDutyClass() {
			return MultiBlockMover.class;
		}
		public String getDutyName() {
			return "Block Mover";
		}

		/*.................................................................................................................*/
		public boolean startJob(String arguments, Object condition, boolean hiredByName) {
			if (containerOfModule() instanceof MesquiteWindow) {
				moveTool = new MultiBlockTool(this, "multiBlockMover", getPath(), "MultiBlockMover.gif", 8,8,"CrossHair.gif",8,8,"Move multiple sequences","This tool moves blocks of sequences for manual alignment.", MesquiteModule.makeCommand("moveTouchCell",  this) , MesquiteModule.makeCommand("moveDragCell",  this), MesquiteModule.makeCommand("moveDropCell",  this));
				setOptionTools();
				moveTool.setDeselectIfOutsideOfCells(false);
				moveTool.setAcceptsOutsideDrops(true);
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
			//cellBlock = new CellBlock((CategoricalData)data, table);
			leftCellBlock = new CellBlock((CategoricalData)data, table);
			leftCellBlock.setLeft(true);
			rightCellBlock = new CellBlock((CategoricalData)data, table);
			rightCellBlock.setRight(true);
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
		/*.................................................................................................................*/
		boolean choosingNewSelection = true;
		int firstTouchPercentHorizontal;
		int firstTouchPercentVertical;
		int firstSequenceInBlock;
		int lastSequenceInBlock;
		int maxLeftMovement = 0;
		int maxRightMovement = 0;
		int currentMoveFromOriginal = 0;
		/*.................................................................................................................*/
		public void resetBetweenColumns(int column, int originalRow, int newRow) {
			int oldTopRow = table.getStartBetweenRowSelection();
			int oldBotRow = table.getEndBetweenRowSelection();
			int topRow = MesquiteInteger.minimum(originalRow, newRow);
			int botRow = MesquiteInteger.maximum(originalRow, newRow);
			table.setStartBetweenRowSelection(topRow);
			table.setEndBetweenRowSelection(botRow);
			//table.redrawBlock(column,MesquiteInteger.minimum(oldTopRow, topRow), column, MesquiteInteger.maximum(oldBotRow, botRow));
			table.getMatrixPanel().drawBetweenSelection(table.getGraphics());
		}

		/*.................................................................................................................*/
		void stopMoving() {
			currentlyMoving = false;
			table.clearBetweenColumnSelection();
			table.repaintAll();
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
		public boolean prepareToMoveMultiSequences() {
			currentMoveFromOriginal = 0;
			originalCheckSum = ((CategoricalData)data).storeCheckSum(0, data.getNumChars(),firstSequenceInBlock, lastSequenceInBlock);
			leftCellBlock.reset();
			rightCellBlock.reset();
			previousPercentHorizontal = firstTouchPercentHorizontal;
			MesquiteInteger firstInBlock= new MesquiteInteger();
			MesquiteInteger lastInBlock= new MesquiteInteger();
			MesquiteBoolean cellHasInapplicable = new MesquiteBoolean();
			MesquiteBoolean leftIsInapplicable = new MesquiteBoolean();
			MesquiteBoolean rightIsInapplicable = new MesquiteBoolean();
			int startOfBlock = table.numColumnsTotal;
			int endOfBlock = 0;
			for (int blockRow = firstSequenceInBlock; blockRow<= lastSequenceInBlock; blockRow++) {
				leftCellBlock.getBlockInSequence(firstColumnTouched, blockRow, firstInBlock, lastInBlock,  wholeSelectedBlock(), wholeSequenceToLeft(), wholeSequenceToRight(), cellHasInapplicable, leftIsInapplicable, rightIsInapplicable);
				startOfBlock = MesquiteInteger.minimum(startOfBlock,firstInBlock.getValue());
				rightCellBlock.getBlockInSequence(firstColumnTouched, blockRow, firstInBlock, lastInBlock,  wholeSelectedBlock(), wholeSequenceToLeft(), wholeSequenceToRight(), cellHasInapplicable, leftIsInapplicable, rightIsInapplicable);
				endOfBlock = MesquiteInteger.maximum(endOfBlock,lastInBlock.getValue());
			}

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
			maxLeftMovement = leftCellBlock.gapColumnsToLeftOfBlock();
			maxRightMovement = rightCellBlock.gapColumnsToRightOfBlock();
			leftCellBlock.setMaximumMovements(maxLeftMovement, 0);
			rightCellBlock.setMaximumMovements(0, maxRightMovement);

			if (currentlyMovingRight)
				currentBlock=rightCellBlock;
			else
				currentBlock = leftCellBlock;

			effectiveFirstColumnTouched = firstColumnTouched;
			previousColumnDragged = effectiveFirstColumnTouched;
			currentlyMoving = true;
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

			boolean movingRight = moveFromOriginal>0;
			boolean movingLeft = moveFromOriginal<0;

			if (canMoveRight() && movingRight &&  !currentlyMovingRight) {  //was dragging left, now switch to dragging right
				switchBlocks(true);
			}
			else if (canMoveLeft() && movingLeft && currentlyMovingRight) {  //was dragging right, now switch to dragging left
				switchBlocks(false);
			}

			if (currentBlock!=null) {
				if (candidateMovement !=0) {  // move it over from previous position by this amount; at least, that is the request
					int distanceToMove = currentBlock.movementAllowed(candidateMovement, canExpand.getValue());

					if (distanceToMove!=0) {

						int added = data.moveCells(currentBlock.getCurrentFirstCharInBlock(), currentBlock.getCurrentLastCharInBlock(), distanceToMove, firstSequenceInBlock, lastSequenceInBlock, canExpand.getValue(), false, true, false,dataChanged);
						if (added<0){ //now start adjusting all the values as we may have added taxa at the start of the matrix
							added = -added;
							firstColumnTouched += added;
							effectiveFirstColumnTouched += added;
							columnDragged += added;
							previousColumnDragged += added;
						} 
						if (added!=0){
							if (!movingRight)
								leftCellBlock.addCharacters(added, movingLeft);
							rightCellBlock.addCharacters(added, movingLeft);
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
			}

			return true;
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
			if (checker.compare(this.getClass(), "Touched.", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveTouchCell")) {

				if (table!=null && data !=null && leftCellBlock !=null && rightCellBlock !=null){
					optionDown = arguments.indexOf("option")>=0;
					MesquiteInteger io = new MesquiteInteger(0);
					firstColumnTouched= MesquiteInteger.fromString(arguments, io);
					effectiveFirstColumnTouched = firstColumnTouched;
					firstRowTouched= MesquiteInteger.fromString(arguments, io);


					firstTouchPercentHorizontal= MesquiteInteger.fromString(arguments, io);
					firstTouchPercentVertical= MesquiteInteger.fromString(arguments, io);

					betweenCells = false;
					leftEdge = false;
					rightEdge = false;
					if (firstTouchPercentHorizontal<=edgePercent) {  //at far left edge of cell
						leftEdge = true;
						firstColumnTouched --;
						currentlyMovingRight=true;
						betweenCells=true;
					} else if (firstTouchPercentHorizontal>=(100-edgePercent)) {  //at far right edge of cell
						rightEdge = true;
						currentlyMovingRight=false;
						betweenCells=true;
					}

					if (table.inBetweenSelectionRowColumns(firstColumnTouched, firstRowTouched)) {
						choosingNewSelection = false;
						firstSequenceInBlock = table.getStartBetweenRowSelection();
						lastSequenceInBlock = table.getEndBetweenRowSelection();
						if (!prepareToMoveMultiSequences()){
							currentlyMoving=false;
							return null;
						}
					}	else {
						choosingNewSelection = true;
					/*	table.setStartBetweenColumnSelection(firstColumnTouched);
						table.setEndBetweenColumnSelection(firstColumnTouched);
						table.setStartBetweenRowSelection(firstRowTouched);
						table.setEndBetweenRowSelection(firstRowTouched);
						table.redrawBlock(firstColumnTouched, firstRowTouched, firstColumnTouched, firstRowTouched);
						*/

					}

				}
			}
			else if (checker.compare(this.getClass(), "Dragging", "[column dragged] [row dragged] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveDragCell")) {
				if (table!=null && data !=null){
					MesquiteInteger io = new MesquiteInteger(0);
					int columnDragged = MesquiteInteger.fromString(arguments, io);
					int rowDragged= MesquiteInteger.fromString(arguments, io);
					int percentHorizontal= MesquiteInteger.fromString(arguments, io);

					if (choosingNewSelection) {
						//resetBetweenColumns(firstColumnTouched, firstRowTouched, rowDragged);
						previousRowDragged = rowDragged;

					} else if (currentlyMoving) {
						dragMultiSequences(percentHorizontal,  rowDragged,  columnDragged);
					}
				}
			}
			else if (checker.compare(this.getClass(), "Dropping.", "[column dropped] [row dropped] [percent horizontal] [percent vertical] [modifiers]", commandName, "moveDropCell")) {
				if (table!=null && data !=null && (firstColumnTouched>=0)&& (firstRowTouched>=0)){
					MesquiteInteger io = new MesquiteInteger(0);
					int columnDropped = MesquiteInteger.fromString(arguments, io);
					int rowDropped= MesquiteInteger.fromString(arguments, io);
					if (rowDropped == table.getMatrixPanel().BEYONDMATRIX) rowDropped = table.numRowsTotal-1;
					if (rowDropped<0) rowDropped=0;
					if (!table.rowLegal(rowDropped)|| !table.columnLegal(columnDropped))
						return null;
					if (choosingNewSelection) {
						//GraphicsUtil.shimmerVerticalOn(table.getGraphics(), table.getMatrixPanel(),  table.getRowY(firstRowTouched),  table.getRowY(rowDropped), table.getColumnX(firstColumnTouched));
						if (rowDropped != firstRowTouched) {
							table.setStartBetweenColumnSelection(firstColumnTouched);
							table.setEndBetweenColumnSelection(firstColumnTouched);

							int droppedPercentHorizontal= MesquiteInteger.fromString(arguments, io);
							int droppedPercentVertical= MesquiteInteger.fromString(arguments, io);

							int topRow = firstRowTouched;
							int bottomRow = rowDropped;
							if (firstRowTouched<rowDropped){ // we've gone down;
								topRow = firstRowTouched;
								bottomRow = rowDropped;
								if (firstTouchPercentVertical>60)
									topRow++;
								if (droppedPercentVertical<40)
									bottomRow--;
							}
							else if (firstRowTouched>rowDropped){ // we've gone down;
								topRow = rowDropped;
								bottomRow = firstRowTouched;
								if (firstTouchPercentVertical<40)
									bottomRow--;
								if (droppedPercentVertical>60)
									topRow++;
							}
							if (topRow<0)
								topRow=0;
							if (bottomRow>=table.numRowsTotal)
								bottomRow=table.numRowsTotal-1;
							if (topRow<=bottomRow) {
								table.setStartBetweenRowSelection(topRow);
								table.setEndBetweenRowSelection(bottomRow);
								table.repaintAll();
							}
						}
					} else if (currentlyMoving){
						moveMultiSequences();
					} else stopMoving();
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
				return super.doCommand(commandName, arguments, checker);
			return null;
		}
		/*.................................................................................................................*/
		public String getName() {
			return "Move multi-sequence block";
		}
		/*.................................................................................................................*/
		/** returns an explanation of what the module does.*/
		public String getExplanation() {
			return "Moves blocks of multiple sequences." ;
		}






		public class MultiBlockTool extends TableTool {
			MesquiteCursor crossHairCursor=null;
			MesquiteCursor optionEdgeCursor=null;


			public MultiBlockTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String extraImageFileName, int extraHotX, int extraHotY, String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand dragCommand, MesquiteCommand droppedCommand) {
				super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation, touchedCommand, dragCommand, droppedCommand);
				this.initiator = initiator;
				this.name = name;

				crossHairCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, extraImageFileName, extraHotX, extraHotY);
			}
			public void setOptionEdgeCursor(String extraImageFileName, int extraHotX, int extraHotY) {
				optionEdgeCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, extraImageFileName, extraHotX, extraHotY);
			}
			public void cursorInCell(int modifiers, int column, int row, int regionInCellH, int regionInCellV, EditorPanel panel){
				if (table.inBetweenSelection(column, row, regionInCellH, regionInCellV))  {
					setCurrentStandardCursor(null);
					if (optionCursor!=null)
						setCurrentOptionCursor(optionCursor);
				}
				else {
					setCurrentStandardCursor(crossHairCursor);
					if (optionEdgeCursor!=null)
						setCurrentOptionCursor(optionEdgeCursor);
				}
			}

		}


	}



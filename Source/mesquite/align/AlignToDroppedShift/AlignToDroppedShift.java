/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.AlignToDroppedShift; 


import java.awt.*;

import mesquite.align.lib.*;
import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.*;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.QueryDialogs;
import mesquite.lib.duties.*;


/* ======================================================================== */
public  class AlignToDroppedShift extends AlignShiftToDroppedBase {
	boolean defaultShiftToDragged = false;
	boolean defaultUseWindow = true;
	int windowLength = 100;
	MesquiteBoolean shiftToDragged = new MesquiteBoolean(defaultShiftToDragged);
	MesquiteBoolean shiftToDropped = new MesquiteBoolean(!defaultShiftToDragged);
	MesquiteBoolean useWindow = new MesquiteBoolean(defaultUseWindow);
	MesquiteBoolean localizedAlignmentRegion = new MesquiteBoolean(true);




	public String getFunctionIconPath(){  //path to icon explaining function, e.g. a tool
		return getPath() + "shiftToDropped.gif";
	}

	public void addToSnapshot(Snapshot temp) {
		if (shiftToDragged.getValue()!=defaultShiftToDragged)
			temp.addLine("toggleShiftToDragged " + shiftToDragged.toOffOnString());
		if (useWindow.getValue()!=defaultUseWindow)
			temp.addLine("useWindow " + useWindow.toOffOnString());
		temp.addLine("setWindowLength " + windowLength);
	}

	/*.................................................................................................................*/
	public void processExtraSingleXMLPreference (String tag, String content) {
		if ("shiftToDragged".equalsIgnoreCase(tag)){
			shiftToDragged.setValue(MesquiteBoolean.fromTrueFalseString(content));
			shiftToDropped.setValue(!shiftToDragged.getValue());
		}
		if ("useWindow".equalsIgnoreCase(tag)){
			useWindow.setValue(MesquiteBoolean.fromTrueFalseString(content));
		}
		if ("windowLength".equalsIgnoreCase(tag))
			windowLength = MesquiteInteger.fromString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "shiftToDragged",shiftToDragged);
		StringUtil.appendXMLTag(buffer, 2, "useWindow",useWindow);
		StringUtil.appendXMLTag(buffer, 2, "windowLength",windowLength);
		return super.preparePreferencesForXML()+buffer.toString();
	}

	/*.................................................................................................................*/
	public  TableTool getTool(MesquiteCommand touchCommand, MesquiteCommand dragCommand, MesquiteCommand dropCommand){
		return new TableTool(this, "shiftToDropped", getPath(), "shiftToDropped.gif", 13,14,"Pairwise Shifter: Shifts touched sequences to the sequence on which they are dropped.", "Shifts touched sequences to the sequence on which they are dropped.", touchCommand, dragCommand, dropCommand);

	}
	/*.................................................................................................................*/
	public FunctionExplanation getFunctionExplanation(){
		return new FunctionExplanation("Shift Aligner", "(A tool of a Character Matrix Window) Shift touched sequences to the sequence on which they are dropped.", null, getPath() + "shiftToDropped.gif");
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	
	/*.................................................................................................................*/
	protected boolean useWindow() {
		return useWindow.getValue();
	}

	/*.................................................................................................................*/
	public void addExtraMenus(){
		addCheckMenuItem(null, "Shift Dragged Sequence so that Dragged Base Matches its Counterpart", makeCommand("toggleShiftToDragged",  this), shiftToDragged);
		addCheckMenuItem(null, "Shift Dragged Sequence so that Base on Which it is Dropped Matches its Counterpart", makeCommand("toggleShiftToDropped",  this), shiftToDropped);
		addMenuSeparator();
		addCheckMenuItem(null, "Examine Defined Window Around Base for Match", makeCommand("useWindow",  this), useWindow);
		addMenuItem("Window Length...", MesquiteModule.makeCommand("setWindowLength", this));
	}
	/*.................................................................................................................*/
	protected boolean alwaysAlignEntireSequences() {
		return false;
	}


	/*.................................................................................................................*/
	void getWindowBoundaries(int it, int column, MesquiteInteger windowStart, MesquiteInteger windowEnd) {
		if (windowStart==null || windowEnd == null)
			return;
		int count = 0;
		int start = 0;
		int end = data.getNumChars();
		for (int ic=column-1; ic>=0; ic--) {  // let's find the start of the window
			if (!data.isInapplicable(ic, it)){
				count++;
				start=ic;
				if (count>= windowLength/2) {
					break;
				}
			}
		}
		int lastWindowHalfLength = windowLength-count;  
		count = 0;
		for (int ic=column; ic<data.getNumChars(); ic++) {  // let's find the end of the window
			if (!data.isInapplicable(ic, it)){
				count++;
				end = ic;
				if (count>=lastWindowHalfLength) {
					break;
				}
			}
		}
		if (count<lastWindowHalfLength) {  // there wasn't enough at the end
			int firstHalfWindowLength = windowLength-count;
			count = 0;

			for (int ic=column-1; ic>=0; ic--) {  // let's find the start of the window
				if (!data.isInapplicable(ic, it)){
					count++;
					start=ic;
					if (count>= firstHalfWindowLength) {
						break;
					}
				}
			}
		}
		windowStart.setValue(start);
		windowEnd.setValue(end);

	}
	MesquiteInteger windowStart = new MesquiteInteger(0);
	MesquiteInteger windowEnd = new MesquiteInteger(0);

	/*.................................................................................................................*/
	protected long[][] windowAlignment(int rowToAlign, int recipientRow, MesquiteInteger columnDropped, MesquiteInteger columnDragged) {
		windowEnd.setValue(data.getNumChars());
		MesquiteNumber score = new MesquiteNumber();
		long[][] aligned = null;
		boolean shiftToDropped = shiftToDragged.getValue() == optionDown;
		if (shiftToDropped) {
			getWindowBoundaries(recipientRow, columnDropped.getValue(), windowStart, windowEnd);
			Debugg.println("DROPPED  windowStart: " + windowStart + ", windowEnd: " + windowEnd);
			aligned = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), recipientRow, windowStart.getValue(), windowEnd.getValue(), rowToAlign,0, data.getNumChars(),true,score);
		} else {
			getWindowBoundaries(rowToAlign, columnDragged.getValue(), windowStart, windowEnd);
			Debugg.println("DRAGGED windowStart: " + windowStart + ", windowEnd: " + windowEnd);
			aligned = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), recipientRow, 0, data.getNumChars(), rowToAlign,windowStart.getValue(), windowEnd.getValue(),true,score);
		}
		return aligned;
	}

	/*.................................................................................................................*/
	protected void alignShiftTouchedToDropped(long[][] aligned, long[] rowToAlignAlignment, int rowToAlign, int recipientRow, MesquiteInteger columnDropped, MesquiteInteger columnDragged, boolean droppedOnData, boolean draggedOnData){
		boolean shiftToDropped = shiftToDragged.getValue() == optionDown;
		int amountToMove = 0;
		int oldNumChars = data.getNumChars();
		int effectiveColumnDropped = columnDropped.getValue();
		int effectiveColumnDragged = columnDragged.getValue();

		if (shiftToDropped) {
			if (!droppedOnData) {  // not dropped on data; find nearest dropcell
				if (droppedCellCount<=0){  //this means it was dropped to the left of the first cell in the dropped sequence 
					droppedCellCount=1;
					for (int icDropped=0; icDropped<data.getNumChars(); icDropped++) {  // let's add up how many data-filled cells are up to the sequence dropped
						if (!data.isInapplicable(icDropped, recipientRow)){
							effectiveColumnDropped = icDropped;
							break;
						}
					}
				} else { // was either internal gap or terminal gap at right end
					for (int icDropped=columnDropped.getValue(); icDropped>=0; icDropped--) {  // let's shift downward until we find the first cell
						if (!data.isInapplicable(icDropped, recipientRow)){
							effectiveColumnDropped = icDropped;
							break;
						}
					}
				}
			} 
			//let's find where the dropped cell is in the new alignment
			int droppedAlignmentCount=0;
			if (useWindow()) {
				droppedAlignmentCount=windowStart.getValue();   // give it a head start because of all the cells that were not sent to the aligner
			}
			int droppedCellPositionInAlignment = 0;

			for (int ic=0; ic<rowToAlignAlignment.length; ic++) {  // let's see the position of droppedCellCount in the alignment of this sequence
				if (!CategoricalState.isInapplicable(aligned[ic][0])) {
					droppedAlignmentCount++;
					if (droppedAlignmentCount>=droppedCellCount) { // we have found the position in the alignment
						droppedCellPositionInAlignment = ic;   
						break;
					}
				}
			}
			// we know it was dropped on the position that is at droppedCellPositionInAlignment in the new alignment.

			//let's see how many cells are in the alignment up to that point in the dragged sequence
			int draggedAlignmentCellCount=0;
			int lastFilledCellDraggedAlignmentPosition =-1;
			for (int ic=0; ic<rowToAlignAlignment.length && ic<=droppedCellPositionInAlignment; ic++) {  // let's see how many cells are in the dragged sequence up to that point
				if (!CategoricalState.isInapplicable(rowToAlignAlignment[ic])) {
					draggedAlignmentCellCount++;
					lastFilledCellDraggedAlignmentPosition=ic;  // within the alignment, this is the last cell, to the left of the dropped site,  in the dragged sequence that has data
				}
			}
			int extraGapsInNewAlignment = 0;  // this will record how many 
			if (lastFilledCellDraggedAlignmentPosition>=0)  // there is data to the left of the dropped site in the dragged sequence
				extraGapsInNewAlignment= droppedCellPositionInAlignment-lastFilledCellDraggedAlignmentPosition;
			else{  // there was nothing to the left; now we have to look to the right
				int firstFilledCellDraggedAlignmentPosition =-1;
				for (int ic=droppedCellPositionInAlignment; ic<rowToAlignAlignment.length; ic++) {  // let's see how many cells are in the dragged sequence up to that point
					if (!CategoricalState.isInapplicable(rowToAlignAlignment[ic])) {  //let's find first one
						draggedAlignmentCellCount=1;
						firstFilledCellDraggedAlignmentPosition=ic;  // within the alignment, this is the last cell, to the left of the dropped site,  in the dragged sequence that has data
						break;
					}
				}
				extraGapsInNewAlignment= droppedCellPositionInAlignment-firstFilledCellDraggedAlignmentPosition;
			}

			// we now know what cell was moved onto the dropped cell.  Where was this cell in the original alignment?

			int draggedCellCountInOriginal=0;
			int lastFilledCellDraggedOriginalPosition =0;
			for (int ic=0; ic<data.getNumChars() && draggedCellCountInOriginal<=draggedAlignmentCellCount; ic++) {  
				if (!data.isInapplicable(ic, rowToAlign)){
					draggedCellCountInOriginal++;
					lastFilledCellDraggedOriginalPosition=ic;
				}
			}

			int positionInOriginalAlignment= lastFilledCellDraggedOriginalPosition+extraGapsInNewAlignment;
			amountToMove = effectiveColumnDropped-positionInOriginalAlignment+1;
			if (useWindow())
				amountToMove=amountToMove-windowLength/2;
			
		} 
		
		else {  //shift to dragged sequence
			if (!draggedOnData) {  // not dragged on data; find nearest dragcell
				if (draggedCellCount<=0){  //this means it was dragged to the left of the first cell in the dragged sequence 
					draggedCellCount=1;
					for (int ic=0; ic<data.getNumChars(); ic++) {  // let's add up how many data-filled cells are up to the sequence dropped
						if (!data.isInapplicable(ic, rowToAlign)){
							effectiveColumnDragged = ic;
							break;
						}
					}
				} else { // was either internal gap or terminal gap at right end
					for (int ic=columnDragged.getValue(); ic>=0; ic--) {  // let's shift downward until we find the first cell
						if (!data.isInapplicable(ic, rowToAlign)){
							effectiveColumnDragged = ic;
							break;
						}
					}
				}
			} 

			//let's find where the dragged cell is in the new alignment
			int draggedAlignmentCount=0;
			if (useWindow()) {
				draggedAlignmentCount=windowStart.getValue();   // give it a head start because of all the cells that were not sent to the aligner
			}
			int draggedCellPositionInAlignment = 0;

			for (int ic=0; ic<rowToAlignAlignment.length; ic++) {  // let's see the position of draggedCellCount in the alignment of this sequence
				if (!CategoricalState.isInapplicable(aligned[ic][1])) {
					draggedAlignmentCount++;
					if (draggedAlignmentCount>=draggedCellCount) { // we have found the position in the alignment
						draggedCellPositionInAlignment = ic;   
						break;
					}
				}
			}
			// we know it was dragged on the position that is at draggedCellPositionInAlignment in the new alignment.

			//let's see how many cells are in the alignment up to that point in the dropped sequence
			int droppedAlignmentCellCount=0;
			int lastFilledCellDroppedAlignmentPosition =-1;
			for (int ic=0; ic<rowToAlignAlignment.length && ic<=draggedCellPositionInAlignment; ic++) {  // let's see how many cells are in the dragged sequence up to that point
				if (!CategoricalState.isInapplicable(aligned[ic][0])) {
					droppedAlignmentCellCount++;
					lastFilledCellDroppedAlignmentPosition=ic;
				}
			}
			int extraGapsInNewAlignment= draggedCellPositionInAlignment-lastFilledCellDroppedAlignmentPosition;

			// we now know onto what cell the cell was dropped .  Where was this cell in the original alignment?

			int droppedCellCountInOriginal=0;
			int lastFilledCellDroppedOriginalPosition =0;
			for (int ic=0; ic<data.getNumChars() && droppedCellCountInOriginal<=droppedAlignmentCellCount; ic++) {  
				if (!data.isInapplicable(ic, recipientRow)){
					droppedCellCountInOriginal++;
					lastFilledCellDroppedOriginalPosition=ic;
				}
			}
			int positionOfDroppedInOriginalAlignment= lastFilledCellDroppedOriginalPosition+extraGapsInNewAlignment;

			amountToMove = positionOfDroppedInOriginalAlignment-effectiveColumnDragged-1;
			if (useWindow())
				amountToMove=amountToMove+windowLength/2;
		}

		MesquiteBoolean dataChanged = new MesquiteBoolean (false);
		MesquiteInteger charAdded = new MesquiteInteger(0);
		int added = 0;
		if (shiftOnlySelectedPiece){
			added = data.moveCells(firstColumnSelected.getValue(), lastColumnSelected.getValue(), amountToMove, rowToAlign, rowToAlign, true, false, true, true, dataChanged,charAdded, null);
			table.deSelectBlock(0, rowToAlign, data.getNumChars()-1, rowToAlign);
			//data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED, null, null));
			table.redrawFullRow(rowToAlign);
		}
		else
			added = data.shiftAllCells(amountToMove, rowToAlign, true, true, true, dataChanged,charAdded, null);
		if (charAdded.isCombinable() && charAdded.getValue()!=0) {
			if (data instanceof DNAData) {
				((DNAData)data).assignCodonPositionsToTerminalChars(charAdded.getValue());
				//						((DNAData)data).assignGeneticCodeToTerminalChars(charAdded.getValue());
			}
			if (amountToMove<0) { // then we added to the front
				columnDropped.add(-amountToMove);
				columnDragged.add(-amountToMove);
			}
		}
		//MAY NEED TO NOTIFY!!!!!!
		/*	data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, null, null));
		data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED, null, null));
		if (oldNumChars!=data.getNumChars()){
			data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null, null));
			data.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED, null, null));
		}

*/

	}

	int droppedCellCount=0;
	int draggedCellCount=0;
	/*.................................................................................................................*/
	public void preRevCompSetup(int rowToAlign, int recipientRow, int columnDropped, int columnDragged){
		droppedCellCount=0;
		for (int ic=0; ic<=columnDropped && ic<data.getNumChars(); ic++) {  // let's add up how many data-filled cells are up to the sequence dropped
			if (!data.isInapplicable(ic, recipientRow))
				droppedCellCount++;
		}
		draggedCellCount=0;
		for (int ic=0; ic<=columnDragged && ic<data.getNumChars(); ic++) {  // let's add up how many data-filled cells are up to the sequence dragged
			if (!data.isInapplicable(ic, rowToAlign))
				draggedCellCount++;
		}
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Toggles whether the sequences are shifted to match the cell on which they are dropped or the site by which they are dragged.", "[on; off]", commandName, "toggleShiftToDragged")) {
			if (ignoreCommand()) return null;
			shiftToDragged.toggleValue(parser.getFirstToken(arguments));
			shiftToDropped.setValue(!shiftToDragged.getValue());
		}
		else if (checker.compare(this.getClass(), "Toggles whether the sequences are shifted to match the cell on which they are dropped or the site by which they are dragged.", "[on; off]", commandName, "toggleShiftToDropped")) {
			if (ignoreCommand()) return null;
			shiftToDropped.toggleValue(parser.getFirstToken(arguments));
			shiftToDragged.setValue(!shiftToDropped.getValue());
		}
		else if (checker.compare(this.getClass(), "Toggles whether a defined window is examined for the match (as opposed to the entire sequence).", "[on; off]", commandName, "useWindow")) {
			if (ignoreCommand()) return null;
			useWindow.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets the window length).", "[value]", commandName, "setWindowLength")) {
			if (ignoreCommand()) return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int newWindowLength = MesquiteInteger.fromString(arguments, io);
			if (newWindowLength<0 || !MesquiteInteger.isCombinable(newWindowLength)){
				if (!MesquiteThread.isScripting()) {
					MesquiteInteger value = new MesquiteInteger(windowLength);
					QueryDialogs.queryInteger(containerOfModule(),"Window length", "Window length for match comparison", true, value);
					if (value.getValue()>0 && value.isCombinable())
						windowLength=value.getValue();

				}
			}
			else{
				windowLength = newWindowLength;
			}
			resetAligner();
			//parametersChanged(null);

		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getProductName() {
		return "Shift";
	}
	/*.................................................................................................................*/
	public String getActionName() {
		return "Shift";
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Shift Dragged Sequence To Dropped Sequence";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies a tool that can be used on a set of sequences.  Sequences dragged by this tool and dropped onto another sequence will be shifted to that other sequence (pairwise)." ;
	}

}



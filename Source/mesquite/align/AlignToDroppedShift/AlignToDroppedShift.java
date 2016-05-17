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
import mesquite.lib.duties.*;


/* ======================================================================== */
public  class AlignToDroppedShift extends AlignShiftToDroppedBase {
	public String getFunctionIconPath(){  //path to icon explaining function, e.g. a tool
		return getPath() + "shiftToDropped.gif";
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
		return true;
	}

	/*.................................................................................................................*/
	 protected void alignShiftTouchedToDropped(long[][] aligned, long[] newAlignment, int rowToAlign, int recipientRow, int columnDropped, boolean droppedOnData){
		if (!droppedOnData) {  // not dropped on data; find nearest dropcell
			if (droppedCellCount<=0){  //this means it was dropped to the left of the first cell in the dropped sequence 
				droppedCellCount=1;
				for (int icDropped=0; icDropped<data.getNumChars(); icDropped++) {  // let's add up how many data-filled cells are up to the sequence dropped
					if (!data.isInapplicable(icDropped, recipientRow)){
						columnDropped=icDropped;
						break;
					}
				}
			} else { // was either internal gap or terminal gap at right end
				for (int icDropped=columnDropped; icDropped>=0; icDropped--) {  // let's shift downward until we find the first cell
					if (!data.isInapplicable(icDropped, recipientRow)){
						columnDropped=icDropped;
						break;
					}
				}
			}
		} 

		//let's find where the dropped cell is in the new alignment
		int droppedAlignmentCount=0;
		int droppedCellPositionInAlignment = 0;

		for (int ic=0; ic<newAlignment.length; ic++) {  // let's see the position of droppedCellCount in the alignment of this sequence
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
		for (int ic=0; ic<newAlignment.length && ic<=droppedCellPositionInAlignment; ic++) {  // let's see how many cells are in the dragged sequence up to that point
			if (!CategoricalState.isInapplicable(newAlignment[ic])) {
				draggedAlignmentCellCount++;
				lastFilledCellDraggedAlignmentPosition=ic;
			}
		}
		int extraGapsInNewAlignment= droppedCellPositionInAlignment-lastFilledCellDraggedAlignmentPosition;
		
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
		int amountToMove = columnDropped-positionInOriginalAlignment+1;

		MesquiteBoolean dataChanged = new MesquiteBoolean (false);
		MesquiteInteger charAdded = new MesquiteInteger(0);
		int added = data.shiftAllCells(amountToMove, rowToAlign, true, true, true, dataChanged,charAdded, null);
		if (charAdded.isCombinable() && charAdded.getValue()!=0 && data instanceof DNAData) {
			((DNAData)data).assignCodonPositionsToTerminalChars(charAdded.getValue());
			//						((DNAData)data).assignGeneticCodeToTerminalChars(charAdded.getValue());
		}
		//MAY NEED TO NOTIFY!!!!!!


		}

	int droppedCellCount=0;

	/*.................................................................................................................*/
	public void preRevCompSetup(int rowToAlign, int recipientRow, int columnDropped, int columnDragged){
		droppedCellCount=0;
		for (int icDropped=0; icDropped<=columnDropped && icDropped<data.getNumChars(); icDropped++) {  // let's add up how many data-filled cells are up to the sequence dropped
			if (!data.isInapplicable(icDropped, recipientRow))
				droppedCellCount++;
		}
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
	  if (checker.compare(this.getClass(), "Toggles whether the data integrity is checked or not after each use.", "[on; off]", commandName, "toggleWarnCheckSum")) {
			if (ignoreCommand()) return null;
			boolean current = warnCheckSum.getValue();
			warnCheckSum.toggleValue(parser.getFirstToken(arguments));
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
		return -100;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Shift To Dropped";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies a tool that can be used on a set of sequences.  Sequences dropped by this tool on another sequence will be shifted to that other sequence (pairwise)." ;
	}

}



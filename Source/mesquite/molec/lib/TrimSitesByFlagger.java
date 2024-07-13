/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.lib;
import java.util.Vector;



import mesquite.lib.*;

import mesquite.lib.characters.CharacterData;


public abstract class TrimSitesByFlagger extends SequenceTrimmer  {
	protected CharacterData data;
	protected MatrixFlagger flaggerTask; // hired by specific subclasses representing those flaggers
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	MatrixFlags flags = null;
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean trimMatrix(CharacterData data,  UndoReference undoReference){
		if (flaggerTask == null)
			return false;
		flags = flaggerTask.flagMatrix( data, flags);
		if (flags == null || flags.getNumChars()<data.getNumChars())
			return false;
		UndoInstructions undoInstructions = null;
		if (undoReference!=null)
			undoInstructions =data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.CHAR_DELETED});

		data.incrementNotifySuppress();
		Vector v = pauseAllPausables();
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();

		/* old
		 * NOTE: this code allows reporting of what contiguous blocks were deleted, but causes full recalculations for each discontiguity
		int ic = data.getNumChars()-1;
		int firstInBlockDeleted = -1;
		int lastInBlockDeleted = -1;
		int blockCount = 0;
		while(ic>=0) {
			if (flags.isBitOn(ic)){  // we've found a selected one
				lastInBlockDeleted = ic;
				while(ic>=0) {  // now let's look for the first non-selected one
					if (flags.isBitOn(ic))
						firstInBlockDeleted = ic;
					else break;
					ic--;
				}

				blockCount++;
				if (blockCount % 50 == 0)
					logln("Deleting characters, block " + blockCount);
				//There is a huge time cost here in deleteParts in ObjSpecsSet every loop
				// better to design new deleteCharacters and asosciated deleteParts that is passed an array of blocks to be deleted (start and end)
				//Debugg.printlnd
				data.deleteCharacters(firstInBlockDeleted, lastInBlockDeleted-firstInBlockDeleted+1, false);  // now prepare contiguous block for deletion
				data.deleteInLinked(firstInBlockDeleted, lastInBlockDeleted-firstInBlockDeleted+1, false);
			}
			ic--;
		}
		 */
		boolean anyDeletion = false;
		String report = "Trimmed:";
		Bits bits = flags.getCharacterFlags();
		if (bits.anyBitsOn()){
			data.deletePartsFlagged(bits, false);
			data.deleteInLinkedFlagged(bits, false);
			anyDeletion = true;
			int numCD = bits.numBitsOn();
			report += " " + numCD + " character";
			if (numCD>1)
				report += "s";
		}
		bits = flags.getTaxonFlags();
		if (bits.anyBitsOn()){
			data.getTaxa().deleteTaxaFlagged(bits, false);
			int numT = bits.numBitsOn();
			if (numT>1)
				report += " " + numT + " taxa";
			else
				report += " " + numT + " taxon";
			anyDeletion = true;
		}
		boolean[][] toMakeGaps = flags.getCellFlags();
		int numC =0;
		for (int ic = 0; ic< data.getNumChars() && ic<toMakeGaps.length; ic++)
			for (int it = 0; it<data.getNumTaxa() && it<toMakeGaps[ic].length; it++)
				if (toMakeGaps[ic][it]){
					data.setToInapplicable(ic, it);
					numC++;
				}
		if (numC>0){
			report += " " + numC + " cell";
			if (numC>1)
				report += "s";
			anyDeletion = true;
		}

		if (!anyDeletion)
			report = "Nothing trimmed";
		logln(report);

		if (getProject() != null)
			getProject().decrementProjectWindowSuppression();
		unpauseAllPausables(v);
		data.decrementNotifySuppress();

		data.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));


		if (undoReference!=null){
			if (undoInstructions!=null){
				undoInstructions.setNewData(data);
				undoReference.setUndoer(undoInstructions);
				undoReference.setResponsibleModule(this);
			}
		}
		return true;
	}

}
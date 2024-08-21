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
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlagger;


public abstract class TrimSitesByFlagger extends SequenceTrimmer  {
	protected CharacterData data;
	protected MatrixFlagger flaggerTask; // hired by specific subclasses representing those flaggers
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	MatrixFlags flags = null;

	/*Subclass override and return true if they want to be called iteratively*/
	protected boolean pleaseIterate(){
		return false;
	}


	
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean trimMatrix(CharacterData data,  UndoReference undoReference){
		if (flaggerTask == null)
			return false;
		if (data.getNumChars()==0)
		return false;
		UndoInstructions undoInstructions = null;
		if (undoReference!=null)
			undoInstructions =data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.CHAR_DELETED});

		data.incrementNotifySuppress();
		Vector v = pauseAllPausables();
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();

		boolean iterate = pleaseIterate();
		boolean done = false;

		while (!done){
			flags = flaggerTask.flagMatrix( data, flags);
			if (flags != null && flags.getNumChars()>=data.getNumChars()){
				if (flags.anyFlagsSet()) {
					int numSitesToDelete = flags.getCharacterFlags().numBitsOn();
					data.deleteByMatrixFlags(flags);
					logln(" " + numSitesToDelete + " character(s) deleted.");
					if (!iterate)
						done = true;
				}
				else
					done = true;
			}
			else done = true;
		}


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
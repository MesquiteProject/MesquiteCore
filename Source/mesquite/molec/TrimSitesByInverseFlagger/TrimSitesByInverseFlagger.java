/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.TrimSitesByInverseFlagger;
import java.util.Vector;



import mesquite.lib.*;

import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlagger;
import mesquite.lib.duties.MatrixFlaggerForTrimming;
import mesquite.molec.lib.SequenceTrimmer;


public class TrimSitesByInverseFlagger extends SequenceTrimmer  {
	protected CharacterData data;
	protected MatrixFlagger flaggerTask; // hired by specific subclasses representing those flaggers
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		flaggerTask = (MatrixFlagger)hireEmployee(MatrixFlaggerForTrimming.class, "Trimming by the inverse of which method?");
		if (flaggerTask == null)
			return false;
		return true;
	}

	MatrixFlags flags = null;


	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setFlagger", flaggerTask); 
		return temp;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets flagger", "[name of module]", commandName, "setFlagger")) {
			MatrixFlagger temp = (MatrixFlagger)hireNamedEmployee(MatrixFlagger.class, arguments);
			if (temp !=null){
				flaggerTask = temp;
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
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

		flags = flaggerTask.flagMatrix( data, flags);
		if (flags != null && flags.getNumChars()>=data.getNumChars()){
			flags.invertCharacters();
			if (flags.anyFlagsSet()) {
				data.deleteByMatrixFlags(flags);
				data.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
				data.notifyInLinked(new Notification(MesquiteListener.PARTS_DELETED));
			}
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

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Inverse Trim Sites";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Inverse Trim Sites...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Deletes sites or blocks of sites by the inverse of a standard trimming method." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
}
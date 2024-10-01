/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.TrimSequences;
/*~~  */

import java.util.*;


import mesquite.lib.*;
import mesquite.lib.characters.AltererWholeCharacterAddRemove;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TrimSequences extends MolecularDataAlterer implements AltererWholeCharacterAddRemove {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(SequenceTrimmer.class, getName() + " needs a module to trim sequences.",
		"");
	}
	MolecularData data ;
	SequenceTrimmer trimmer;
	MesquiteTable table;

	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		if (arguments ==null)
 			trimmer = (SequenceTrimmer)hireEmployee(SequenceTrimmer.class, "Sequence trimmer");
	 	else {
	 		trimmer = (SequenceTrimmer)hireNamedEmployee(SequenceTrimmer.class, arguments);
 			if (trimmer == null)
 				trimmer = (SequenceTrimmer)hireEmployee(SequenceTrimmer.class, "Sequence trimmer");
 		}
 		if (trimmer == null) {
 			return sorry(getName() + " couldn't start because no sequence trimmer was obtained.");
 		}
		return true;
	}
	public  Class getHireSubchoice(){
		return SequenceTrimmer.class;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String reportStatus(){
		if (trimmer == null)
			return "no trimmer";
			return trimmer.reportStatus();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setTrimmer", trimmer); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets trimmer module", "[name of module]", commandName, "setTrimmer")) {
			SequenceTrimmer temp = (SequenceTrimmer)replaceEmployee(SequenceTrimmer.class, arguments, "Sequence trimmer", trimmer);
			if (temp !=null){
				trimmer = temp;
				return temp;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		
		UndoInstructions undoInstructions = null;
		if (undoReference!=null)
			undoInstructions =data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.CHAR_DELETED});
		
		data.incrementNotifySuppress();
		Vector v = pauseAllPausables();
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		
		if (trimmer!=null) {
			boolean a = trimmer.trimMatrix(data, null); //second parameter would be UndoReference!
			if (a) {
				if (table != null)
					table.repaintAll();
				data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
				data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
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
		return SUCCEEDED;
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
	}


	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Trim Sequences";
	}
	/*.................................................................................................................*/
	public String getNameAndParameters() {
		if (trimmer == null)
			return "Trim Sequences"; 
		return "Trim Sequences (" + trimmer.getName() + ")";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Arranges for sites to be trimmed." ;
	}


}


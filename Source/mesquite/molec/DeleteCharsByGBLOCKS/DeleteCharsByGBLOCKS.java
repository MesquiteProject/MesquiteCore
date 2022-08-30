/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.DeleteCharsByGBLOCKS;
/*~~  */



import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.*;

import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharacterSelector;
import mesquite.lib.duties.DataAlterer;
import mesquite.lib.table.*;

/* ======================================================================== */
public class DeleteCharsByGBLOCKS extends DataAlterer implements AltererWholeCharacterAddRemove {
	CharacterData data;
	CharacterSelector gblocksTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		gblocksTask = (CharacterSelector)hireNamedEmployee(CharacterSelector.class, "#GBLOCKSSelector");
		if (gblocksTask == null)
			return false;
		return true;
	}

	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		data.deselectAll();
		gblocksTask.selectCharacters( data);
		
		UndoInstructions undoInstructions = null;
		if (undoReference!=null)
			undoInstructions =data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.CHAR_DELETED});
		

		//NOTE: this code allows reporting of what contiguous blocks were deleted, but causes full recalculations for each discontiguity
		int ic = data.getNumChars()-1;
		int firstInBlockDeleted = -1;
		int lastInBlockDeleted = -1;
		while(ic>=0) {
			if (data.getSelected(ic)){  // we've found a selected one
				lastInBlockDeleted = ic;
				while(ic>=0) {  // now let's look for the first non-selected one
					if (data.getSelected(ic))
						firstInBlockDeleted = ic;
					else break;
					ic--;
				}

				data.deleteCharacters(firstInBlockDeleted, lastInBlockDeleted-firstInBlockDeleted+1, false);  // now prepare contiguous block for deletion
				data.deleteInLinked(firstInBlockDeleted, lastInBlockDeleted-firstInBlockDeleted+1, false);
			}
			ic--;
		}
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
	public void alterCell(CharacterData ddata, int ic, int it){
		/*CategoricalData data = (CategoricalData)ddata;
		if (data.isUnassigned(ic, it))
			data.setState(ic, it, CategoricalState.inapplicable);
		 */
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
		return "Delete Sites by GBLOCKS";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Deletes characters according to GBLOCKS criteria." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}



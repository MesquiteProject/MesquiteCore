/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.TrimTermAmbiguities;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNADataAlterer;
import mesquite.categ.lib.DNAState;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoInstructions;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererDNACell;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAltererParallelizable;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.molec.lib.SequenceTrimmer;

public class TrimTermAmbiguities extends SequenceTrimmer {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean trimMatrix(CharacterData dData, UndoReference undoReference){
		if (dData==null)
			return false;
	
		if (!(dData instanceof DNAData)){
			MesquiteMessage.warnProgrammer(getName() + " requires DNA data");
			return false;
		}
		DNAData data = (DNAData)dData;


   		UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
		boolean changed = false;
		Taxa taxa = data.getTaxa();
		boolean anyTaxaSelected = taxa.anySelected();
		for (int it = 0; it<taxa.getNumTaxa(); it++)
			if (!anyTaxaSelected || taxa.isSelected(it)) {
				
				for (int ic = 0; ic<data.getNumChars(); ic++){  // check start
					if (!data.isInapplicable(ic, it)) {
						if (data.isAmbiguousOrPolymorphic(ic, it)) {
							data.setState(ic, it, DNAState.inapplicable);
							changed = true;
						} else
							break;
					}
				}
				for (int ic = data.getNumChars()-1; ic>=0; ic--){  // check end
					if (!data.isInapplicable(ic, it)) {
						if (data.isAmbiguousOrPolymorphic(ic, it)) {
							data.setState(ic, it, DNAState.inapplicable);
							changed = true;
						} else
							break;
					}
				}
			}
		if (undoInstructions!=null) {
			undoInstructions.setNewData(data);
			if (undoReference!=null){
				undoReference.setUndoer(undoInstructions);
				undoReference.setResponsibleModule(this);
			}
		}
		return changed;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
	public String getName() {
		return "Trim Terminal Ambiguous Bases";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Removes any ambiguous bases (e.g., R, Y, N) from the start and end of each sequence." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return  403;  
	}

}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.TrimTermPartTriplets;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNADataAlterer;
import mesquite.categ.lib.DNAState;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoInstructions;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererDNACell;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;

public class TrimTermPartTriplets extends DNADataAlterer  implements AltererDNACell {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData dData, MesquiteTable table,  UndoReference undoReference){
		if (dData==null)
			return -10;
	
		if (!(dData instanceof DNAData)){
			MesquiteMessage.warnProgrammer(getName() + " requires DNA data");
			return ResultCodes.INCOMPATIBLE_DATA;
		}
		DNAData data = (DNAData)dData;
		if (!data.someCoding()) 
			return 10;

   		UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
		boolean changed = false;
		
		for (int it = 0; it<data.getNumTaxa(); it++)
			if (table == null || !table.anyRowSelected()||table.wholeRowSelectedAnyWay(it)) {
				for (int ic = 0; ic<data.getNumChars(); ic++){  // check start
					if (!data.isInapplicable(ic, it)) {
						if (data.getCodonPosition(ic)==2) {
							data.setState(ic, it, DNAState.inapplicable);
							changed=true;
							if (data.getCodonPosition(ic+1)==3)
								data.setState(ic+1, it, DNAState.inapplicable);
						}
						else if (data.getCodonPosition(ic)==3) {
							data.setState(ic, it, DNAState.inapplicable);
							changed=true;
						}
						break;
					}
				}
				for (int ic = data.getNumChars()-1; ic>=0; ic--){  // check end
					if (!data.isInapplicable(ic, it)) {
						if (data.getCodonPosition(ic)==2) {
							data.setState(ic, it, DNAState.inapplicable);
							changed=true;
							if (data.getCodonPosition(ic-1)==1)
								data.setState(ic-1, it, DNAState.inapplicable);
						}
						else if (data.getCodonPosition(ic)==1) {
							data.setState(ic, it, DNAState.inapplicable);
							changed=true;
						}
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
		if ( changed)
			return ResultCodes.SUCCEEDED;
			return ResultCodes.MEH;
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
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
		return "Remove Terminal Incomplete Codons";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Removes any incomplete codons from the start and end of each sequence." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return  201;  
	}

}


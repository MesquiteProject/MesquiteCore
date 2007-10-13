package mesquite.molec.TrimTermPartTriplets;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;

public class TrimTermPartTriplets extends DNADataAlterer {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData dData, MesquiteTable table,  UndoReference undoReference){
		if (dData==null || table==null)
			return false;
	
		if (!(dData instanceof DNAData)){
			MesquiteMessage.warnProgrammer(getName() + " requires DNA data");
			return false;
		}
		DNAData data = (DNAData)dData;
		if (!data.someCoding()) 
			return false;

		UndoInstructions undoInstructions = data.getUndoInstructionsAllData();
		boolean changed = false;
		
		for (int it = 0; it<data.getNumTaxa(); it++)
			if (table.isRowSelected(it)) {
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
		undoInstructions.setNewData(data);
		if (undoReference!=null){
			undoReference.setUndoer(undoInstructions);
			undoReference.setResponsibleModule(this);
		}
		return changed;
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
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
		return "Trim Terminal Incomplete Codons";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Trims any incomplete codons from the start and end of each sequence." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return  NEXTRELEASE;  
	}

}


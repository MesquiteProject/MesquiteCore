package mesquite.align.ShiftToMinimizeStops;

/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;



/* ======================================================================== */
public class ShiftToMinimizeStops extends DNADataAlterer {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData data, int ic, int it){
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		if (data==null || table==null || !(data instanceof DNAData))
			return false;

		DNAData dnaData = (DNAData)data;
		if (!dnaData.someCoding()) 
			return false;
		UndoInstructions undoInstructions = data.getUndoInstructionsAllData();

		MesquiteBoolean dataChanged = new MesquiteBoolean();
		int[] numStops= new int[3];
		for (int it=0; it<dnaData.getNumTaxa(); it++) 
			if (table.isRowSelected(it)) {
				numStops[0]= dnaData.getAminoAcidNumbers(it,ProteinData.TER);
				if (numStops[0]>0) {
					int added = data.shiftSequence(1, it, true, true, false, dataChanged);
					numStops[1] = dnaData.getAminoAcidNumbers(it,ProteinData.TER);
					if (numStops[1]>0) {
						added = data.shiftSequence(1, it, true, true, false, dataChanged);
						numStops[2] = dnaData.getAminoAcidNumbers(it,ProteinData.TER);
						if (numStops[0]<=numStops[1] && numStops[0]<=numStops[2] ) {  // no change is best, but have shifted by 2, need to shift back
							added = data.shiftSequence(-2, it, true, true, false, dataChanged);
							dataChanged.setValue(false);
						}
						else if (numStops[1]<=numStops[0] && numStops[1]<=numStops[2] ) {  // one shift change is best, but have shifted by 1, need to shift back
							added = data.shiftSequence(-1, it, true, true, false, dataChanged);							
						}
					}

				}
			}

		undoInstructions.setNewData(data);
		if (undoReference!=null){
			undoReference.setUndoer(undoInstructions);
			undoReference.setResponsibleModule(this);
		}

		return dataChanged.getValue();

	}

	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Shift to Minimize Stop Codons";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Shift to Minimize Stop Codons";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shifts each sequence by 0, 1, or 2 bases to minimize number of stop codons implied." ;
	}

}



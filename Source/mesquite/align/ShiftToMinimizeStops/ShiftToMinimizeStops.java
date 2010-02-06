/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.72, December 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
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
		return alterData(data,table,undoReference,null);
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference, AlteredDataParameters alteredDataParameters){
		if (data==null || table==null || !(data instanceof DNAData))
			return false;

		DNAData dnaData = (DNAData)data;
		if (!dnaData.someCoding()) 
			return false;
		UndoInstructions undoInstructions = data.getUndoInstructionsAllData();

		MesquiteBoolean dataChanged = new MesquiteBoolean();
		MesquiteInteger charAdded = new MesquiteInteger(0);
		boolean someCharAdded = false;
		int singleTaxonChanged = -1;
		
		
		int[] numStops= new int[3];
		for (int it=0; it<dnaData.getNumTaxa(); it++) 
			if (table.wholeRowSelectedAnyWay(it)) {
				numStops[0]= dnaData.getAminoAcidNumbers(it,ProteinData.TER);   //unshifted amount
				if (numStops[0]>0) {
					int added = data.shiftAllCells(1, it, true, true, false, dataChanged,charAdded);
					if (charAdded.isCombinable() && charAdded.getValue()!=0) {
						dnaData.assignCodonPositionsToTerminalChars(charAdded.getValue());
						someCharAdded=true;
					}
					numStops[1] = dnaData.getAminoAcidNumbers(it,ProteinData.TER);  //amount if shift by 1
					if (numStops[1]>0) {
						added = data.shiftAllCells(1, it, true, true, false, dataChanged,charAdded);
						if (charAdded.isCombinable() && charAdded.getValue()!=0) {
							dnaData.assignCodonPositionsToTerminalChars(charAdded.getValue());
							someCharAdded=true;
						}
						numStops[2] = dnaData.getAminoAcidNumbers(it,ProteinData.TER);  //amount if shift by 2
						if (numStops[0]<=numStops[1] && numStops[0]<=numStops[2] ) {  // no change is best, but have shifted by 2, need to shift back
							added = data.shiftAllCells(-2, it, true, true, false, dataChanged,charAdded);
							dataChanged.setValue(false);
						}
						else if (numStops[1]<=numStops[0] && numStops[1]<=numStops[2] ) {  // one shift change is best, but have shifted by 1, need to shift back
							added = data.shiftAllCells(-1, it, true, true, false, dataChanged,charAdded);							
						}
					}
					if (dataChanged.getValue())
						if (singleTaxonChanged<0)
							singleTaxonChanged=it;
						else if (MesquiteInteger.isCombinable(singleTaxonChanged))
							singleTaxonChanged=MesquiteInteger.unassigned;

				}
			}
		if (!someCharAdded && alteredDataParameters!=null) {
			if (singleTaxonChanged>=0 && MesquiteInteger.isCombinable(singleTaxonChanged)) {
				alteredDataParameters.setSubcodes(new int[] {MesquiteListener.SINGLE_TAXON});
				alteredDataParameters.setParameters(new int[] {singleTaxonChanged});

			} else
				alteredDataParameters.setSubcodes(new int[] {MesquiteListener.ALL_CELLS_ONLY_SHIFTED});
		}

		if (undoInstructions!=null) {
			undoInstructions.setNewData(data);
			if (undoReference!=null){
				undoReference.setUndoer(undoInstructions);
				undoReference.setResponsibleModule(this);
			}
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
		return -111;  
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



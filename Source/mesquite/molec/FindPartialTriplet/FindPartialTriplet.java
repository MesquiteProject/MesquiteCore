package mesquite.molec.FindPartialTriplet;
/*~~  */

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class FindPartialTriplet extends FindSequenceCriterionG {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	public boolean showOptions(CharacterData data, MesquiteTable table){
		return true;
	}
	public boolean findNext(CharacterData data, MesquiteTable table, MesquiteInteger charFound, MesquiteInteger length, MesquiteInteger taxonFound) {
		length.setValue(0);
		int firstTaxon = taxonFound.getValue();
		int firstChar = charFound.getValue();
		int numTaxa = data.getNumTaxa();
		int numChars = data.getNumChars();
		MesquiteInteger matchLength = new MesquiteInteger(3);
		for (int it = firstTaxon; it< numTaxa; it++){
			for (int ic = firstChar; ic< numChars; ic++) {
				if (isPartialTriplet(data, table, ic, it, matchLength)){
					charFound.setValue(ic);
					taxonFound.setValue(it);
					length.setValue(matchLength.getValue());
					return true;
				}
			}
		}
		return false;
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean isPartialTriplet(CharacterData dData, MesquiteTable table,  int ic, int it, MesquiteInteger matchLength){
		if (dData==null || table==null)
			return false;

		if (!(dData instanceof DNAData)){
			MesquiteMessage.warnProgrammer(getName() + " requires DNA data");
			return false;
		}
		DNAData data = (DNAData)dData;
	
		return data.isStartOfPartialTriplet(ic, it, matchLength);
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Find Incomplete Codons";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Finds the next occurrence of a partial codon triplet in a matrix of molecular data." ;
	}
	public boolean showOptions(boolean findAll, CharacterData data, MesquiteTable table) {
		return true;
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return  NEXTRELEASE;  
	}


}


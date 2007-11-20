package mesquite.align.AlignDNAToProtein;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.*;
import mesquite.charMatrices.CharMatrixCoordIndep.CharMatrixCoordIndep;




public class AlignDNAToProtein extends DNADataAltererCon {
	CharMatrixCoordIndep characterSourceTask;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharMatrixCoordIndep.class, getName() + " needs a module to supply a character matrix.",
		"The matrix source is chosen initially.");
	}


	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		characterSourceTask = (CharMatrixCoordIndep)hireCompatibleEmployee(CharMatrixCoordIndep.class, ProteinState.class, "Protein Matrix");
//	characterSourceTask = (CharMatrixCoordIndep)hireCompatibleEmployee(CharMatrixCoordIndep.class, ProteinState.class, "Protein Matrix");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no protein matrix obtained");

		return true;
	}

	/*.................................................................................................................*/
	public boolean forceAlignment(DNAData dnaData, ProteinData proteinData, int it) {
		
		int numBases = dnaData.getNumberApplicableInTaxon(it, true);
		int numAAs = proteinData.getNumberApplicableInTaxon(it, true);
		if (numAAs*3!= numBases) {
			logln("Number of nucleotides in sequence does not correspond to number of amino acids in protein sequence (taxon " + it + ")");
			return false;
		}
		
		int numInDNA = dnaData.getNumChars();
		int numInProtein = proteinData.getNumChars();
		MesquiteBoolean dataChanged = new MesquiteBoolean();
		if (numInDNA<numInProtein*3) {
			dnaData.addCharacters(numInDNA, numInProtein*3-numInDNA, false);
			dataChanged.setValue(true);
		}
		dnaData.collapseGapsInCellBlockRight(it, 0, dnaData.getNumChars()-1, false);   //move everything to right so we only need to shift them left

		int ic = 0;
		while (ic<numInProtein) {   // now zip through the protein and find bases and move the nucleotides to correspond
			ic = proteinData.nextApplicable(it, ic, true)	;
			if (ic<0) break;
			int posInDNA = ic*3;
			for (int codPos=0; codPos<=2; codPos++){
				int toMove = dnaData.nextApplicable(it, posInDNA, true);
				if (toMove>=0) {
					int distance = toMove-posInDNA;
					dnaData.moveCells(toMove,toMove, -distance, it, false, false, true,  false, dataChanged);
					posInDNA++;
				}
			}
			ic++;
		}
		
		
	return dataChanged.getValue();
	

	}
	/*.................................................................................................................*/
	public boolean alterBlockInTaxon(CharacterData data, int icStart, int icEnd, int it) {
		if (data==null || !(data instanceof DNAData))
			return false;
		Taxa taxa = data.getTaxa();

		MCharactersDistribution m =  characterSourceTask.getCurrentMatrix(taxa);
		if (m == null)
			return false;
		ProteinData proteinData = (ProteinData)m.getParentData();

		return forceAlignment((DNAData)data,proteinData, it);
	}

	public String getName() {
		return "Align DNA to Protein";
	}

	public String getExplanation() {
		return "Realigns a DNA matrix to match the alignment in an amino acid alignment.";
	}
	public boolean requestPrimaryChoice() {
		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}



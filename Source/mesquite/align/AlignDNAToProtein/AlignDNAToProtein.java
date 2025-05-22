/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.AlignDNAToProtein;

import java.awt.Checkbox;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.molec.lib.*;
import mesquite.charMatrices.CharMatrixCoordIndep.CharMatrixCoordIndep;




public class AlignDNAToProtein extends DNADataAltererCon  implements AltererAlignShift {
	CharMatrixCoordIndep characterSourceTask;
	boolean adjustCodonPositions = true;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharMatrixCoordIndep.class, getName() + " needs a module to supply a character matrix.",
		"The matrix source is chosen initially.");
	}


	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		characterSourceTask = (CharMatrixCoordIndep)hireCompatibleEmployee(CharMatrixCoordIndep.class, ProteinState.class, "Protein Matrix");
//	characterSourceTask = (CharMatrixCoordIndep)hireCompatibleEmployee(CharMatrixCoordIndep.class, ProteinState.class, "Protein Matrix");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no protein matrix obtained");
		if (!MesquiteThread.isScripting())
			if (!queryOptions())
				return false;

		return true;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "AlignDNAToProtein",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Align DNA to Protein");
		Checkbox adjustCodonPositionsBox = dialog.addCheckBox("adjust codon positions if necessary in re-aligned sequences", adjustCodonPositions);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			adjustCodonPositions = adjustCodonPositionsBox.getState();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public boolean forceAlignment(DNAData dnaData, ProteinData proteinData, int it) {
		if (dnaData == null || proteinData == null)
			return false;
		int numBases = dnaData.getNumberApplicableInTaxon(it, true);
		int numAAs = proteinData.getNumberApplicableInTaxon(it, true);
		if (numAAs*3!= numBases) {
			logln("Number of nucleotides (\" + numBases + \") in sequence does not correspond to number of amino acids  (\" + numAAs + \") in protein sequence (taxon " + (it+1) + ")");
			return false;
		}
		
		int numInDNA = dnaData.getNumChars();
		int numInProtein = proteinData.getNumChars();
		MesquiteBoolean dataChanged = new MesquiteBoolean();
		MesquiteInteger charAdded = new MesquiteInteger(0);
		if (numInDNA<numInProtein*3) {
			dnaData.addCharacters(numInDNA, numInProtein*3-numInDNA, false);
			dnaData.assignCodonPositionsToTerminalChars(numInProtein*3-numInDNA);
//			dnaData.assignGeneticCodeToTerminalChars(numInProtein*3-numInDNA);
			dnaData.addInLinked(numInDNA, numInProtein*3-numInDNA, true);
			dataChanged.setValue(true);
		}
		dnaData.collapseGapsInCellBlockRight(it, 0, dnaData.getNumChars()-1, false);   //move everything to right so we only need to shift them left
		//note that this means it only works on whole sequences! with codon positions defined

		int icProtein = 0;
		int icDNA = 0;
		
		while (icProtein<numInProtein) {   // now zip through the protein and find bases and move the nucleotides to correspond
			icProtein = proteinData.nextApplicable(it, icProtein, true)	;
			if (icProtein<0) break;
			int adjustedPosInDNA = icProtein*3; // new location
			for (int codPos=0; codPos<=2; codPos++){
				icDNA = dnaData.nextApplicable(it, icDNA, true);
				if (icDNA>=0) {
					int distance = icDNA-adjustedPosInDNA;
					dnaData.moveCells(icDNA,icDNA, -distance, it,it, false, false, true,  false, dataChanged, charAdded, null);
					if (charAdded.isCombinable() && charAdded.getValue()!=0) {
						dnaData.assignCodonPositionsToTerminalChars(charAdded.getValue());
//						dnaData.assignGeneticCodeToTerminalChars(charAdded.getValue());
					}
					else if (adjustCodonPositions)  // adjust codpos so it fits for this taxon
						dnaData.setCodonPosition(adjustedPosInDNA, codPos+1, false, false);
					adjustedPosInDNA++;
					icDNA++;
				}
			}
			icProtein++;
		}
		
		
	return dataChanged.getValue();
	

	}
	/*.................................................................................................................*/
	public boolean alterBlockInTaxon(CharacterData data, int icStart, int icEnd, int it) {
		if (data==null || !(data instanceof DNAData))
			return false;
		Taxa taxa = data.getTaxa();

		MCharactersDistribution m =  characterSourceTask.getCurrentMatrix(taxa);
		if (m == null || !(m.getParentData() instanceof ProteinData))
			return false;
		ProteinData proteinData = (ProteinData)m.getParentData();

		return forceAlignment((DNAData)data,proteinData, it);
	}

	public boolean alterBlockOfCharacters(CharacterData data, int icStart, int icEnd) {
		if (data==null || !(data instanceof DNAData))
			return false;
		Taxa taxa = data.getTaxa();

		MCharactersDistribution m =  characterSourceTask.getCurrentMatrix(taxa);
		if (m == null || !(m.getParentData() instanceof ProteinData))
			return false;
		ProteinData proteinData = (ProteinData)m.getParentData();
		boolean result = false;
		for (int it = 0; it< data.getNumTaxa(); it++){
			boolean success = forceAlignment((DNAData)data,proteinData, it);
			result = result || success;
		}
		return result;
	}
	public String getName() {
		return "Align DNA to Protein";
	}

	public String getNameForMenuItem() {
		return "Align DNA to Protein...";
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
		return -111;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}



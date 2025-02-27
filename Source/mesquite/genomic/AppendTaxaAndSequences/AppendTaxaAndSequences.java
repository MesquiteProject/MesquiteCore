/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.AppendTaxaAndSequences;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.basic.ManageTaxaPartitions.ManageTaxaPartitions;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteFrame;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class AppendTaxaAndSequences extends FileAssistantFM {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		findAndAppend();
		return true;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Appends taxa and sequences from a file", null, commandName, "append")) {
			findAndAppend();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public boolean requestPrimaryChoice(){
		return true;
	}

	/*.................................................................................................................**/
	private void findAndAppend(){
		MesquiteProject proj = getProject();
		MesquiteString directoryName = new MesquiteString();
		MesquiteString fileName = new MesquiteString();
		Taxa useTaxa = null;
		Taxa receivingTaxa = null;
		if (proj.getNumberTaxas()==0){
			alert("You can append only to a file that already has a taxa block");
			return;
		}
		else
			receivingTaxa = proj.chooseTaxa(containerOfModule(), "To which block of taxa do you want to append the incoming taxa and sequences?", false);
		boolean transferDone = false;
		MesquiteFile.openFileDialog("Please select a NEXUS file whose taxa and sequences you want to append to this one. Only the first taxa block will be read!", directoryName, fileName);
		if (!fileName.isBlank()){
			TaxaGroupVector groupsVector = (TaxaGroupVector)proj.getFileElement(TaxaGroupVector.class, 0);
			Listable[] previousGroups = groupsVector.getElementArray();
			Listable[] previousTaxas = proj.getTaxas().getElementArray();
			MesquiteFile fileToRead = new MesquiteFile(directoryName.getValue(), fileName.getValue());
			proj.addFile(fileToRead);
			fileToRead.setProject(proj);
			//only do this if there's a set of taxa; ask user to choose if there are more than one
			NexusFileInterpreter mb = (NexusFileInterpreter)findNearestColleagueWithDuty(NexusFileInterpreter.class);
			mb.readFile(getProject(), fileToRead, " @noWarnMissingReferent  @noWarnUnrecognized @noWarnDupTaxaBlock @readOneTaxaBlockOnly @justTheseBlocks.TAXA.CHARACTERS.DATA.SETS.LABELS");

			//CharacterState state = new CategoricalState();

			CharactersManager charactersManager = (CharactersManager)proj.getCoordinatorModule().findElementManager(CharacterData.class);
			Taxa incomingTaxa = proj.getTaxa(fileToRead, 0);
			Listable[] currentTaxas = new Listable[]{incomingTaxa};
			int countWarnings = 0;

			if (incomingTaxa != null){ // 
				for (int incomingTaxonNumber = 0; incomingTaxonNumber<incomingTaxa.getNumTaxa(); incomingTaxonNumber++){

					String incomingTaxonName = incomingTaxa.getTaxonName(incomingTaxonNumber);
					int receivingTaxonNumber = receivingTaxa.whichTaxonNumber(incomingTaxonName);
					boolean existingTaxon = true;
					if (receivingTaxonNumber <0){ 
						receivingTaxa.addTaxa(receivingTaxa.getNumTaxa(), 1, true);  //need to notify so all matrices are made aware
						receivingTaxonNumber = receivingTaxa.getNumTaxa()-1;
						Taxon newTaxon = receivingTaxa.getTaxon(receivingTaxonNumber);
						newTaxon.setName(incomingTaxonName);
						existingTaxon = false;
					}
					//	progIndicator.setSecondaryMessage("Taxon: " + incomingName);
					// OK, the taxon is read (new or not)
					for (int iM = 0; iM<proj.getNumberCharMatrices(fileToRead); iM++){

						CharacterData incomingMatrix = proj.getCharacterMatrix(fileToRead, iM);

						//if (incomingMatrix instanceof DNAData){
						String incomingMatrixName = incomingMatrix.getName();
						CharacterData receivingMatrix = proj.getCharacterMatrixByReference(null,  receivingTaxa, null, incomingMatrixName);
						if (receivingMatrix == null || !(receivingMatrix.getDataTypeName().equalsIgnoreCase(incomingMatrix.getDataTypeName()))){  //matrix of same name and kind not found; make a new one (new locus)
							receivingMatrix = charactersManager.newCharacterData(receivingTaxa, 0, incomingMatrix.getDataTypeName()); //this is manager of receiving project
							receivingMatrix.setName(incomingMatrixName);
							receivingMatrix.addToFile(receivingTaxa.getFile(), proj, null);
						}
						/*Now time to pull sequence into receivingMatrix 	 */
						transferDone = true;
						int incomingSeqLeng = incomingMatrix.lastApplicable(incomingTaxonNumber) + 1;
						if (incomingSeqLeng>receivingMatrix.getNumChars())
							receivingMatrix.addCharacters(receivingMatrix.getNumChars(), incomingSeqLeng-receivingMatrix.getNumChars(), false);
						boolean doTransfer = true; //for now, always overwrite as long as their is sequence coming in
						if (existingTaxon){
							if (incomingSeqLeng == 0) //no sequence coming in, so don't transfer
								doTransfer = false;
							else if (receivingMatrix.hasDataForTaxon(receivingTaxonNumber, true)) {
								for (int ic = 0; ic< receivingMatrix.getNumChars(); ic++) //delete existing sequence to prepare to receive other //ZQ: do this, or have it as query at start?
									receivingMatrix.setToInapplicable(ic, receivingTaxonNumber);
								if (++countWarnings <10)
									logln("Data in matrix " + receivingMatrix.getName() + " replaced for taxon " + receivingTaxa.getTaxonName(receivingTaxonNumber));
								else if (countWarnings == 10)
									logln("Data replaced for other matrices or taxa as well");
							}
						}
						if (doTransfer){
							CharacterState state = null;
							for (int ic = 0; ic< receivingMatrix.getNumChars() && ic< incomingMatrix.getNumChars(); ic++){
								state = incomingMatrix.getCharacterState(state, ic, incomingTaxonNumber);
								receivingMatrix.setState(ic, receivingTaxonNumber, state);
							}
						}
						//}
					}
				}
				ManageTaxaPartitions partManager = (ManageTaxaPartitions)findElementManager(TaxaPartition.class);
				partManager.transferCurrentPartitionAndGroups( proj, previousTaxas, currentTaxas, receivingTaxa, previousGroups);
			}
			if (!transferDone)
				alert("Nothing was appended, perhaps because no appropriate matrices were found. "
						+"Only the first taxa block in the incoming file is read, and so if that file had multiple taxa blocks, the appropriate matrices may belong to a subsequent taxa block."); 
			else {
				receivingTaxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
				}
			//***************
			proj.getCoordinatorModule().closeFile(fileToRead, true);

		}



		iQuit();

	}
	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return true;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Quick Merge Taxa & Matrices from File...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Quick Merge Taxa & Matrices";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reads taxa and matrices (especially sequence matrices) from a NEXUS file and concatenates them to taxa blocks and matrices in the current file, matching taxa and matrices by name. "
				+	"Only the first taxa block and its matrices are read from the incoming file. Trees, codon positions, and other details are not read. "
				+ "This is quicker and less awkward than Careful Merge Taxa & Matrices, but it is less careful, in that it relies on matching names to indicate identity. "
				+" Tuned for workflows, e.g., phylogenomics, in which new taxa and sequences are added into a growing base data file. See also Include Data from Flipped FASTAs for an alternative model.";  
	}

}



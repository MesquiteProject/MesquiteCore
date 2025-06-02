/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.genomic.CombineFlippedFastas;

import java.io.File;


import mesquite.lib.characters.CharacterData;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.io.InterpretFastaDNA.InterpretFastaDNA;
import mesquite.lib.*;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.GeneralFileMakerMultiple;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.ProgressIndicator;

/* ======================================================================== */
public class CombineFlippedFastas extends GeneralFileMakerMultiple {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}

	/*.................................................................................................................*/
	//If incomign project is null, establish new project. 
	public void processDirectory(String directoryPath, MesquiteProject project){
		if (StringUtil.blank(directoryPath) || project == null)
			return;
		Taxa taxa= null;

			ListableVector taxas = new ListableVector();
			for (int iT = 0; iT<project.getNumberTaxas(); iT++){
				Taxa eTaxa = project.getTaxa(iT);
				taxas.addElement(eTaxa, false);
			}
			if (taxas.size()==1){
				taxa = (Taxa)taxas.elementAt(0);
			}
			else if (taxas.size()>1){
				Listable chosen = ListDialog.queryList(containerOfModule(), "With which taxa to fuse?", "Please choose the block of taxa with which you want the sequences imported to be fused", null, "OK", "Cancel", taxas.getElementArray(), 0);
				taxa = (Taxa)chosen; //if null then will establish new
				if (taxa == null)
					return;
			}
		
		File directory = new File(directoryPath);
		boolean abort = false;
		String path = "";
		StringBuffer results = new StringBuffer();
		boolean taxaNew = false;
		if (directory!=null) {
			if (directory.exists() && directory.isDirectory()) {
				//FileCoordinator fileCoord = getFileCoordinator();  //this is the temporary one for this GeneralFileMaker
				//if (fileCoord == null){
				//	alert("oops, no file coordinator");
				//}
			//	if (project == null)
			//		project = fileCoord.initiateProject(directoryPath, new MesquiteFile()); //this is the reading project

				int countWarnings = 0;
				//If taxa is not passed, need to establish new project
				if (taxa == null){
					TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
					taxa = taxaManager.quietMakeNewTaxaBlock(0);
					taxa.setName("Taxa from " + StringUtil.getLastItem(directoryPath, MesquiteFile.fileSeparator));
					taxaNew = true;
				}
				MesquiteProject recProject = taxa.getProject();  //either the existing project (if taxa had been passed in) or the new one
				CharactersManager charactersManager = (CharactersManager)recProject.getCoordinatorModule().findElementManager(CharacterData.class);
				//-----
				if (!taxaNew)
					logln("Adding taxa to block " + taxa.getName() + " in file " + taxa.getFile().getFileName());
				InterpretFastaDNA importer = (InterpretFastaDNA)findNearestColleagueWithDuty(InterpretFastaDNA.class);
				String[] files = directory.list();
				ProgressIndicator progIndicator = new ProgressIndicator(null,"Processing Folder of Data Files", files.length);
				progIndicator.start();
				int filesFound = 0;
				DNAState state = new DNAState();

				logln("The first few files will be slow; they may take a few minutes to process.");
				int lociAdded = 0;
				for (int i=0; i<files.length; i++) {
					progIndicator.setCurrentValue(i);
					if (progIndicator.isAborted()) {
						logln("Stopped by user\n");
						abort = true;
					}
					if (abort)
						break;
					if (files[i]!=null) {
						boolean acceptableFile = (StringUtil.endsWithIgnoreCase(files[i], ".fas") || StringUtil.endsWithIgnoreCase(files[i], ".fasta"));
						if (acceptableFile){
							path = directoryPath + MesquiteFile.fileSeparator + files[i];
							File cFile = new File(path);

							if (cFile.exists() && !cFile.isDirectory() && (!files[i].startsWith("."))) {
								MesquiteFile file = new MesquiteFile();
								file.setPath(path);
								project.addFile(file);
								file.setProject(project);
								logln("Reading file " + files[i]);
								MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
								importer.readFile(project, file, null);
								MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);

								//The file is read. Get its one taxa block
								String taxonName = StringUtil.getAllButLastItem(files[i], "."); //all but file extension
								CommandRecord.tick("File read for taxon " + taxonName);

								Taxa loci = project.getTaxa(file, 0);

								if (loci != null){
									CharacterData incomingFlippedMatrix = project.getCharacterMatrix(file, loci, null, 0, false);
									if (incomingFlippedMatrix != null){
										progIndicator.setSecondaryMessage("Taxon: " + taxonName + " with " + loci.getNumTaxa() + " loci");
										//logln(" (" + loci.getNumTaxa() + " loci)");

										//OK, ready to go. Have matrix. Will add new taxon based on the name of the file, and transfer over its sequences
										boolean existingTaxon = true;
										int receivingTaxonNumber = taxa.whichTaxonNumber(taxonName);
										if (receivingTaxonNumber <0){
											taxa.addTaxa(taxa.getNumTaxa(), 1, true);  //need to notify so all matrices are made aware
											receivingTaxonNumber = taxa.getNumTaxa()-1;
											Taxon newTaxon = taxa.getTaxon(receivingTaxonNumber);
											newTaxon.setName(taxonName);
											existingTaxon = false;
										}
										//For each "taxon", find corresponding matrix already in project
										for (int iLocus = 0; iLocus < loci.getNumTaxa(); iLocus++){
											CommandRecord.tick("For taxon " + taxonName + ", recovering sequence #" + (iLocus+1));
											String locusName = loci.getTaxonName(iLocus);
											CharacterData locusMatrix = recProject.getCharacterMatrixByReference(null,  taxa, null, locusName);
											if (!(locusMatrix instanceof DNAData))
												locusMatrix = null;
											if (locusMatrix == null) { //no matrix yet; establish
												locusMatrix = charactersManager.newCharacterData(taxa, 0, DNAData.DATATYPENAME); //this is manager of receiving project
												locusMatrix.setName(locusName, false);
												locusMatrix.addToFile(taxa.getFile(), recProject, null);
												lociAdded++;
												if (lociAdded == 1)
													log("   New Loci .");
												else if (lociAdded%100 == 0)
													log(" " + lociAdded);
												else if (lociAdded%10 == 0)
													log(".");
											}
											

											/*Now time to pull sequence into locus matrix 
											 * Sequence on row iLocus of incomingFlippedMatrix corresponds to sequence for newTaxon in locusMatrix
											 */
											int incomingSeqLeng = incomingFlippedMatrix.lastApplicable(iLocus) + 1;
											if (incomingSeqLeng>locusMatrix.getNumChars())
												locusMatrix.addCharacters(locusMatrix.getNumChars(), incomingSeqLeng-locusMatrix.getNumChars(), false);
											boolean doTransfer = true; //for now, alway overwrite
											if (existingTaxon){
												if (incomingSeqLeng == 0)
													doTransfer = false;
												else {
													for (int ic = 0; ic< locusMatrix.getNumChars(); ic++) //delete existing sequence to prepare to receive other
														locusMatrix.setToInapplicable(ic, receivingTaxonNumber);
													if (++countWarnings <10)
														logln("Data in matrix " + locusMatrix.getName() + " replaced for taxon " + taxa.getTaxonName(receivingTaxonNumber));
													else if (countWarnings == 10)
														logln("Data replaced for other matrices or taxa as well");
												}
											}
											for (int ic = 0; ic< locusMatrix.getNumChars() && ic< incomingFlippedMatrix.getNumChars(); ic++){
												state = (DNAState)incomingFlippedMatrix.getCharacterState(state, ic, iLocus);
												locusMatrix.setState(ic, receivingTaxonNumber, state);

											}
										}
										logln("");
									}
								}


								project.getCoordinatorModule().closeFile(file, true);
								filesFound++;

							}

						}
					
					}
					
				}
				if (filesFound == 0){
					if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "No files found"))  
						alert("No appropriate files with extensions (.fas or .fasta) were found in folder.");
					else
						discreetAlert("No appropriate files with extensions (.fas or .fasta) were found in folder.");

				}
				MesquiteMessage.beep();
				progIndicator.goAway();
				project.developing = false;  //so the coordinator knows it's OK to dispose
				if (!taxaNew)
					taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			}


		}
	}
	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		incrementMenuResetSuppression();

		String directoryPath = MesquiteFile.chooseDirectory("Choose folder containing FASTA files, one per taxon:", null); 
		if (StringUtil.blank(directoryPath))
			return null;
		FileCoordinator fileCoord = getFileCoordinator();
		proj = fileCoord.initiateProject(directoryPath, new MesquiteFile()); //this is the reading project
		
		// if there is a project, and it has taxa, can ask whether to add to it
		processDirectory(directoryPath, proj);
		if (proj != null){
			String fileSuggestion = StringUtil.getLastItem(directoryPath, MesquiteFile.fileSeparator);
			if (StringUtil.blank(fileSuggestion))
				fileSuggestion = directoryPath;
			fileSuggestion += ".nex";
			proj.getHomeFile().setFileName(fileSuggestion);
			proj.developing = false;
			if (proj.getHomeFile().changeLocation("Save imported file as NEXUS file")) {
				getFileCoordinator().writeFile(proj.getHomeFile()); 
				if (proj.getTaxa(0) != null)
					proj.getTaxa(0).setDirty(true); //just to force it to resave if quitting
			}
			else
				proj = null;
		}
		decrementMenuResetSuppression();
		return proj;
	}

	/*.................................................................................................................*/
	public boolean loadModule() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
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
	public String getName() {
		return "Combine Flipped FASTA Files";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Combine Flipped FASTA Files (One per Taxon)";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Reads taxa & sequences. Imports all \"flipped\" FASTA files in a folder, each containing the sequences of many loci for a single taxon, to establish and build a file with with all of the taxa and a matrix for each of those loci."
				+" Each input file should be named by the taxon name, and each sequence should be named for its locus. As each file is read, sequences are matched by name to the locus matrices being accumulated."
				+" Tuned for phylogenomics workflows that maintain a library of flipped fasta files that can be combined for varied studies with different taxon sampling. "
				+" Flipped FASTA files can be produced using File, Export, Flipped FASTA files (One per taxon). (Note: to add to existing matrices, use Include Flipped FASTA Files in the Include & Merge submenu.)" ;
	}


}

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

import java.awt.Checkbox;
import java.awt.Label;
import java.io.File;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.io.InterpretFastaDNA.InterpretFastaDNA;
import mesquite.io.InterpretFlippedFastaDNA.InterpretFlippedFastaDNA;
import mesquite.lib.CommandRecord;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTimer;
import mesquite.lib.Notification;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.GeneralFileMakerMultiple;
import mesquite.lib.duties.NexusFileInterpreter;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.duties.TaxonNameAlterer;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lib.ui.QueryDialogs;
import mesquite.lib.ui.RadioButtons;

/* ======================================================================== */
public class CombineFlippedFastas extends GeneralFileMakerMultiple {
	TaxonNameAlterer nameAlterer;
	int alterNames = 0; //don't alter; 1 = alter
	int replacementRule = CharacterData.MERGE_useLongest; 
	public boolean queryReplacementRules = false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		//Debugg.println rebuild as Extensible dialog to put the caution about "taxon" into a separate label after radio buttons
		boolean goAhead = introductoryOptions();
		/*int result = QueryDialogs.queryTwoRadioButtons(containerOfModule(), "Combining Taxonwise FASTA files", 
				"This imports all of the taxonwise FASTA files in a folder. (Touch the help (?) button for an explanation "
						+ "of what a taxonwise FASTA file is.)"
						+ "\n\nDo you want to alter or adjust the names of loci (e.g., by deleting part of the name) "
						+"as the taxonwise FASTA files are being read?\n\nNote: If you choose to alter the locus names, some of the choices in the "
						+"subsequent dialog box refer to \"taxon names\", but it's actually the locus names that are getting altered."
						+" The reason for this misnaming is that Mesquite is set to interpret rows "
						+"in a file as taxa, but in these taxonwise fasta files, the rows are loci.", 
						"What is a taxonwise FASTA file? "
								+"A FASTA file is often in one of two orentations, locuswise or taxonwise:"
								+"<ul><li>A <b>locuswise FASTA file</b> concerns data for a single locus for each of many taxa.</li>"
								+ "<li>A <b>taxonwise FASTA file</b> concerns data for a single taxon, listing the sequences in each of many loci.</li></ul>"
								+"A genome assembly file for a single taxon is usually a taxonwise FASTA file, "
								+"each sequence being a contig. However, taxonwise FASTA files can be compiled by this feature only if homologs have been"
								+" identified and named as such in each file. "
								+"A locus appearing in different files needs to have a name that is at least partially consistent from file to file.", 
								"Don't alter locus names", "Alter locus names");
		/*
			\n\nIf you choose to alter the names, note that some of the choices in the next dialog will 	*/
		if (!goAhead)
			return false;
		loadPreferences();
		if (alterNames == 1) {
			nameAlterer = (TaxonNameAlterer)hireEmployee(TaxonNameAlterer.class, "How to alter locus names (even though some say \"taxon names\")");
			if (nameAlterer == null)
				return false;
		}
		return true;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("alterNames".equalsIgnoreCase(tag))
			alterNames = MesquiteInteger.fromString(content);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "alterNames", alterNames);  
		return buffer.toString();
	}

	
	
	boolean introductoryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(containerOfModule(), "Combining Taxonwise FASTA files",buttonPressed);
		id.addLabel("This imports all of the taxonwise FASTA files in a folder.");
		id.addBlankLine();
		id.addLargeTextLabel("A taxonwise FASTA file concerns a single taxon, "
				+ "and contains sequences for various loci. Touch the help (?) button for more explanation.");
		id.addBlankLine();
		id.addHorizontalLine(2);
		id.addLabel("Adjust names of incoming loci?");
		id.addBlankLine();
		id.addLabel("Do you want to alter or adjust the names of loci", Label.LEFT);
		id.addLabel("(e.g., by deleting part of the name)", Label.LEFT);
		id.addLabel("as the taxonwise FASTA files are being read?", Label.LEFT);


		String helpString = "<h3>What is a taxonwise FASTA file?</h3>"
				+"A FASTA file is often in one of two orentations, locuswise or taxonwise:"
				+"<ul><li>A <b>locuswise FASTA file</b> concerns data for a single locus for each of many taxa. If you have a series of such files, "
				+"you would say your data are arranged locuswise.</li>"
				+ "<li>A <b>taxonwise FASTA file</b> concerns data for a single taxon, listing the sequences in each of many loci. If you have a series of such files, "
				+"you would say your data are arranged taxonwise.</li></ul>"
				+"An example of a taxonwise FASTA file is a genome assembly file for a single taxon, "
				+"each sequence being a contig. However, taxonwise FASTA files can be compiled by this feature only if homologs have been"
				+" identified and named as such in each file. "
				+"A locus appearing in different files needs to have a name that is at least partially consistent from file to file, "
				+"so that Mesquite can recognize them as belonging to the same locus, and thus be compiled into a single matrix "
				+"(and eventually alignment).";
		id.appendToHelpString(helpString);



		id.addBlankLine();
		RadioButtons radio = id.addRadioButtons(new String[] {"Don't alter locus names", "Alter locus names"},alterNames);
		id.addBlankLine();
		id.addLargeOrSmallTextLabel("Note: If you choose to alter the locus names, some of the subsequent choices "
				+"refer to \"taxon names\", but it's actually the locus names that are getting altered."
				+" The reason for this misnaming is that Mesquite is set to interpret rows "
				+"in a file as taxa, but in these taxonwise FASTA files, the rows are loci.");

		id.completeAndShowDialog(true);

		if (buttonPressed.getValue()==0)  {
			alterNames = radio.getValue();
			storePreferences();
		}
		id.dispose();
		return buttonPressed.getValue()==0;
	}
	void duplicateLocusNamesOptions() {
		if (queryReplacementRules) //default is to not query and use MERGE_useLongest
			return;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(containerOfModule(), "Duplicate locus names",buttonPressed);
		id.addLargeTextLabel("An incoming sequence appears to belong to a locus that already has data for this taxon.");
		id.addBlankLine();
		RadioButtons radio = id.addRadioButtons(new String[] {"Use the longest sequence", 
				"Retain the existing sequence", "Replace existing by incoming sequence"},alterNames);
		id.addBlankLine();

		id.completeAndShowDialog("OK",null,null,"OK");

		if (buttonPressed.getValue()==0)  {
			int v = radio.getValue();
			if (v == 0)
				replacementRule = CharacterData.MERGE_useLongest;
			else if (v == 1)
				replacementRule = CharacterData.MERGE_preferReceiving;
			else if (v == 2)
				replacementRule = CharacterData.MERGE_preferIncoming;
			
		}
		id.dispose();
	}

	boolean firstFile = true;
	public boolean okToInteractWithUser(int howImportant, String messageToUser){
		return firstFile;
	}/**/
	
	String[] acceptableFileExtensions = new String[]{".fas", ".fasta", ".fna"};
	/*.................................................................................................................*/
	
	public String getAcceptableFileExtensions () {
		String s="";
		for (int i=0; i<acceptableFileExtensions.length; i++) {
			if (i>0)
				s+= " or ";
			s+=acceptableFileExtensions[i];
		}
		return s;

	}
	/*.................................................................................................................*/
	
	public boolean acceptableFileName (String fileName) {
		for (int i=0; i<acceptableFileExtensions.length; i++)
			if (StringUtil.endsWithIgnoreCase(fileName, acceptableFileExtensions[i]))
					return true;
		return false;
	}
	
	/*.................................................................................................................*/
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
		boolean replacementQueried = false;
		File directory = new File(directoryPath);
		boolean abort = false;
		String path = "";
		StringBuffer results = new StringBuffer();
		boolean taxaNew = false;
		incrementMenuResetSuppression();
		incrementNEXUSBlockSortSuppression();
		MesquiteTimer overallTime = new MesquiteTimer();
		overallTime.start();
		if (directory!=null) {
			if (directory.exists() && directory.isDirectory()) {
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
				InterpretFlippedFastaDNA importer = (InterpretFlippedFastaDNA)findNearestColleagueWithDuty(InterpretFlippedFastaDNA.class);
				String[] files = directory.list();
				if (files == null || files.length ==0)
					return;

				String message = null; //"Looking for acceptable files";
				int iF = 0;
				while (message == null && iF<files.length){
					if (acceptableFileName(files[iF]))
						message = "Reading file: " + files[iF];
					iF++;
				}
				if (message == null)
					message = "Looking for acceptable files";
				ProgressIndicator progIndicator = new ProgressIndicator(null,"Processing Folder of Data Files", message, files.length, true);//MesquiteProject mp, String title, String initialMessage, long total, boolean showStop
				progIndicator.start();
				int filesFound = 0;
				DNAState state = new DNAState();

				int lociAdded = 0;

				// ============ GOING THROUGH DIRECTORY OF FILES, each representing a taxon, within which each "taxon" represents a locus ===========
				for (int i=0; i<files.length; i++) {
					progIndicator.setCurrentValue(i);
					if (progIndicator.isAborted()) {
						logln("Stopped by user\n");
						abort = true;
					}
					if (abort)
						break;
					if (files[i]!=null) {

						boolean acceptableFile = acceptableFileName(files[i]);   
						if (acceptableFile){
							path = directoryPath + MesquiteFile.fileSeparator + files[i];
							File cFile = new File(path);

							if (cFile.exists() && !cFile.isDirectory() && (!files[i].startsWith("."))) {
								progIndicator.setText("Reading taxonwise FASTA file: " + files[i]);
								MesquiteFile file = new MesquiteFile();
								file.setPath(path);
								project.addFile(file);
								file.setProject(project);
								if (files.length<20)
									logln("Reading taxonwise FASTA file " + files[i]);
								else if (i == 0)
									log(" [File " + (i+1) + "]");
								else
									log(" [" + (i+1) + "]");

								//===================================================
								/*
								Here the taxon file (i.e. the taxonwise fasta file) for a single taxon is read. 
								The sequences within it will be interpreted as taxa by Mesquite, but in fact 
								each is the sequence for a particular locus.
								 */
								MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
								importer.readFile(project, file, null);
								MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);

								//The file has been read. Figure out the name for the taxon represented by this file
								String taxonName = StringUtil.getAllButLastItem(files[i], "."); //all but file extension
								CommandRecord.tick("File read for taxon " + taxonName);

								//Now let's look at the "taxa", i.e. loci, in this file
								Taxa loci = project.getTaxa(file, 0);


								if (loci != null){

									//First, alter the names of the loci if requested <<<<======= NEW 4.01 Altering the name of the loci
									if (nameAlterer != null)
										nameAlterer.alterTaxonNames(loci, null);
									firstFile = false; //This is remembered so alterTaxonNames doesn't ask for options next time; see okToInteractWithUser above

									CharacterData incomingFlippedMatrix = project.getCharacterMatrix(file, loci, null, 0, false);
									if (incomingFlippedMatrix != null){
										progIndicator.setSecondaryMessage("Taxon: " + taxonName + " with " + loci.getNumTaxa() + " loci");

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
										//For each locus, i.e. taxon in the incoming matrix, use its name to find corresponding matrix already in the main project
										for (int iLocus = 0; iLocus < loci.getNumTaxa(); iLocus++){

											CommandRecord.tick("For taxon " + taxonName + ", recovering sequence #" + (iLocus+1));

											//Get the name of the iLocus'th locus in the taxonwise fasta file
											String locusName = loci.getTaxonName(iLocus);

											//Can we find a matrix by this name already in the project?
											CharacterData locusMatrix = recProject.getCharacterMatrixByReference(null,  taxa, null, locusName);
											if (!(locusMatrix instanceof DNAData))
												locusMatrix = null;

											if (locusMatrix == null) { //This must be a new locus. Establish a new matrix for it
												locusMatrix = charactersManager.newCharacterData(taxa, 0, DNAData.DATATYPENAME); //this is manager of receiving project
												locusMatrix.setName(locusName, false);
												locusMatrix.addToFile(taxa.getFile(), recProject, null);
												lociAdded++;
												if (lociAdded == 1)
													log("   Adding Loci .");
												else if (lociAdded%100 == 0)
													log(" " + lociAdded);
												else if (lociAdded%10 == 0)
													log(".");
											}

											/*Now we have either found or made a locus matrix for the locus. 
											 * Next, pull sequence into locus matrix 
											 * Sequence on row iLocus of incomingFlippedMatrix corresponds to sequence for newTaxon in locusMatrix
											 */

											/*
											CharacterData.MERGE_useLongest
											CharacterData.MERGE_preferReceiving
											CharacterData.MERGE_preferIncoming
											 */

											if (existingTaxon){
												int incomingSeqLeng = incomingFlippedMatrix.getTotalNumApplicable(iLocus, false);
												if (incomingSeqLeng > 0){ 
													//System.err.println("@ existing Taxon ");

													int existingSeqLeng = locusMatrix.getTotalNumApplicable(receivingTaxonNumber, false);
													boolean doTransfer = false;
													if (existingSeqLeng>0 && !replacementQueried){
														duplicateLocusNamesOptions();
														replacementQueried = true;
													}
													
													if (replacementRule == CharacterData.MERGE_preferReceiving) {
														doTransfer = existingSeqLeng == 0 && incomingSeqLeng > 0;
													}
													else if (replacementRule == CharacterData.MERGE_preferIncoming){
														doTransfer = incomingSeqLeng > 0;
													}
													else if (replacementRule == CharacterData.MERGE_useLongest)
														doTransfer = existingSeqLeng < incomingSeqLeng;

													if (doTransfer){
														int incomingSeqLengNeeded = incomingFlippedMatrix.lastApplicable(iLocus) + 1;
														if (incomingSeqLengNeeded>locusMatrix.getNumChars())
															locusMatrix.addCharacters(locusMatrix.getNumChars(), incomingSeqLengNeeded-locusMatrix.getNumChars(), false);
														for (int ic = 0; ic< locusMatrix.getNumChars(); ic++) //*%* delete existing sequence to prepare to receive other
															locusMatrix.setToInapplicable(ic, receivingTaxonNumber);
														for (int ic = 0; ic< locusMatrix.getNumChars() && ic< incomingFlippedMatrix.getNumChars(); ic++){
															state = (DNAState)incomingFlippedMatrix.getCharacterState(state, ic, iLocus);
															locusMatrix.setState(ic, receivingTaxonNumber, state);
														}
														if (existingSeqLeng>0){
															if (++countWarnings <10)
																logln("Data in matrix " + locusMatrix.getName() + " replaced for taxon " + taxa.getTaxonName(receivingTaxonNumber));
															else if (countWarnings == 10)
																logln("Data replaced for other matrices or taxa as well");
														}
													}
												}
											}
											else {
												//System.err.println("@ not existing Taxon ");
												int incomingSeqLengNeeded = incomingFlippedMatrix.lastApplicable(iLocus) + 1;
												if (incomingSeqLengNeeded>locusMatrix.getNumChars())
													locusMatrix.addCharacters(locusMatrix.getNumChars(), incomingSeqLengNeeded-locusMatrix.getNumChars(), false);
												for (int ic = 0; ic< locusMatrix.getNumChars() && ic< incomingFlippedMatrix.getNumChars(); ic++){
													state = (DNAState)incomingFlippedMatrix.getCharacterState(state, ic, iLocus);
													locusMatrix.setState(ic, receivingTaxonNumber, state);

												}
											}
										}
										if (files.length<20)
											logln("");
									}
								}
								//==========================================
								project.getCoordinatorModule().closeFile(file, true);
								filesFound++;
							}
						}
					}
				}
				if (files.length>=20)
					logln("");
				overallTime.end();
				if (filesFound == 0){
					if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "No files found"))  
						alert("No appropriate files with extensions (" + getAcceptableFileExtensions() + ") were found in folder.");
					else
						discreetAlert("No appropriate files with extensions (" + getAcceptableFileExtensions() + ") were found in folder.");
				}
				else
					logln("Taxonwise Fastas read for " + files.length + " taxa; " + lociAdded + " different loci found. [" + overallTime.timeSinceLastInSeconds() + " sec.]" );

				MesquiteMessage.beep();
				progIndicator.goAway();
				project.developing = false;  //so the coordinator knows it's OK to dispose
				if (!taxaNew)
					taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			}
		}
		decrementNEXUSBlockSortSuppression();
		FileCoordinator fCoord =project.getCoordinatorModule();
		if (fCoord!= null){
			NexusFileInterpreter fi = (NexusFileInterpreter)fCoord.findEmployeeWithDuty(NexusFileInterpreter.class);
			if (fi != null)
				fi.sortAllBlocks();
		}
		fireEmployee(nameAlterer);

		decrementMenuResetSuppression();
	}
	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		incrementMenuResetSuppression();

		String directoryPath = MesquiteFile.chooseDirectory("Choose folder containing FASTA files, one per taxon:", null); 
		if (StringUtil.blank(directoryPath))
			return null;
		FileCoordinator fileCoord = getFileCoordinator();
		proj = fileCoord.initiateProject(directoryPath, new MesquiteFile()); //this is the reading project

		if (proj != null){
			String fileSuggestion = StringUtil.getLastItem(directoryPath, MesquiteFile.fileSeparator);
			if (StringUtil.blank(fileSuggestion))
				fileSuggestion = directoryPath;
			fileSuggestion += ".nex";
			proj.getHomeFile().setFileName(fileSuggestion);
			if (proj.getHomeFile().changeLocation("Save imported file as NEXUS file")) {
				processDirectory(directoryPath, proj);
				MesquiteWindow w = fileCoord.getModuleWindow();
				if (w != null) {
					w.setWindowSize(300, 300);  //so it has a decent window size if the user doesn't save again
				}
				getFileCoordinator().writeFile(proj.getHomeFile()); 
				if (proj.getTaxa(0) != null)
					proj.getTaxa(0).setDirty(true); //just to force it to resave if quitting
			}
			else
				proj = null;
			proj.developing = false;
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
		return false;
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
		return 400;  
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Combine Taxonwise (Multi-Locus) FASTA Files";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Combine Taxonwise (Multi-Locus) FASTA Files...";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Reads taxa & sequences. Imports all taxonwise FASTA files in a folder. "
				+"Each taxonwise FASTA file contains the sequences of many loci for a single taxon."
				+" This import will compile a single file with all of the taxa and a matrix for each of those loci."
				+" Each input file should be named by the taxon name, and each sequence within the file should be named for its locus. "
				+" As each file is read, sequences are matched by name to the locus among those being accumulated."
				+" Tuned for phylogenomics workflows that maintain a library of taxonwise fasta files that "
				+"can be combined for varied studies with different taxon sampling. "
				+" Taxonwise FASTA files can be produced using File, Export, Taxonwise (Multi-Locus) FASTA files."
				+" (Note: to add to existing matrices, use Include Data from Taxonwise (Multi-Locus) FASTAs in the Include & Merge submenu.)" ;
	}


}

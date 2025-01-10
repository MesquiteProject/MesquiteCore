/* Mesquite source code.  Copyright 2016 and onward, W. Maddison and D. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.dmanager.ProcessDataFiles; 

import java.awt.Button;
import java.awt.Choice;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.Vector;

import javax.swing.JLabel;

import mesquite.categ.lib.CategoricalState;
import mesquite.genomic.FlagBySpruceup.FlagBySpruceup;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.Debugg;
import mesquite.lib.EmployeeVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTimer;
import mesquite.lib.Puppeteer;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.FileInterpreter;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.FileProcessor;
import mesquite.lib.duties.GeneralFileMaker;
import mesquite.lib.duties.NexusFileInterpreter;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lib.ui.SingleLineTextField;


/* ======================================================================== */
public class ProcessDataFiles extends GeneralFileMaker implements ActionListener { 
	/*.................................................................................................................*/

	MesquiteProject processProject = null;
	FileCoordinator fileCoord = null;
	String directoryPath=null;
	String lastDirectoryUsed=null;
	ProgressIndicator progIndicator = null;
	FileInterpreter importer = null;
	MesquiteFile writingFile;

	String preferencesScript = null;
	String currentScript = null;
	boolean incorporateScript = false;
	Vector fileProcessors = null;
	boolean cancelProcessing = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("script".equalsIgnoreCase(tag))
			preferencesScript = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("importerString".equalsIgnoreCase(tag))
			importerString = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("fileExtension".equalsIgnoreCase(tag))
			fileExtension = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("lastDirectoryUsed".equalsIgnoreCase(tag))
			lastDirectoryUsed = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "script", preferencesScript);  
		StringUtil.appendXMLTag(buffer, 2, "importerString", importerString);  
		StringUtil.appendXMLTag(buffer, 2, "fileExtension", fileExtension);  
		StringUtil.appendXMLTag(buffer, 2, "lastDirectoryUsed", lastDirectoryUsed);  
		return buffer.toString();
	}
	/*.................................................................................................................*/

	
	/*=============================================================================================================*/
	/* Primarily user interface methods
	 *    */
/*.................................................................................................................*/
	//execute script to load previous processors and their parameters
	void executeScript(String script) {
		Puppeteer p = new Puppeteer(this);
		CommandRecord mr = MesquiteThread.getCurrentCommandRecord();

		MesquiteThread.setCurrentCommandRecord(CommandRecord.macroRecord); //was macroRecord
		p.execute(this, script, new MesquiteInteger(0), null, false);
		MesquiteThread.setCurrentCommandRecord(mr);

		EmployeeVector employees = processProject.getCoordinatorModule().getEmployeeVector();
		for (int i= 0; i<employees.size(); i++) {
			MesquiteModule mb = (MesquiteModule)employees.elementAt(i);
			if (mb instanceof FileProcessor)
				recordProcessor((FileProcessor)mb);
		}
	
	}
	/*.................................................................................................................*/
	List processorList = null;
	boolean fromSavedScript = false;
	
	void removeAllProcessors() {
		if (processorList != null)
			processorList.removeAll();
		if (fileProcessors == null)
			return;
		for (int i= 0; i< fileProcessors.size(); i++){
			FileProcessor fProcessor = (FileProcessor)fileProcessors.elementAt(i);
			processProject.getCoordinatorModule().fireEmployee(fProcessor);
		}
		if (fileProcessors != null)
			fileProcessors.removeAllElements();

	}
	
	void resetProcessorList() {
		if (processorList != null)
			processorList.removeAll();
		for (int i = 0; i<fileProcessors.size(); i++){
			if (fileProcessors.elementAt(i)!=null)
				processorList.add("(" + (i+1) + ") " + ((FileProcessor)fileProcessors.elementAt(i)).getNameAndParameters());
		}
			
	}
	
	JLabel intro1, intro2;
	void setIntro(boolean fromSavedScript) {
		if (fileProcessors.size()==0) {
			intro1.setText("For each file examined, how do you want to process it?");
			intro2.setText("");
		}
		else {
			if (fromSavedScript) {
				intro1.setText("For each file examined, how do you want to process it?");
				intro2.setText("The processing steps used previously are:");

			}
			else {
				intro1.setText("Do you want to add another step in processing each file?");
				intro2.setText("The processing steps already requested are:");
			}
		}
	}
	/*.................................................................................................................*/
	public boolean showProcessDialog() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Processing Files",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		intro1 = dialog.addLabel("xxxxxxxxxxxxxxxx  xxxxxxxxxxxxxxxx  xxxxxxxxxxxxxxx  xxxxxxxxx");
		intro2 = dialog.addLabel("                  ");
		setIntro(fromSavedScript);
		
		String[] steps = new String[fileProcessors.size()];
		for (int i = 0; i<steps.length; i++){
			if (fileProcessors.elementAt(i)!=null)
				steps[i] = "(" + (i+1) + ") " + ((FileProcessor)fileProcessors.elementAt(i)).getNameAndParameters();
		}
		processorList = dialog.addList (steps, null, null, 8);

		dialog.addBlankLine();
		Button loadButton = null;
		loadButton = dialog.addAListenedButton("Load Script", null, this);
		loadButton.setActionCommand("load");
		Button clearButton = null;
		clearButton = dialog.addAListenedButton("Clear All", null, this);
		clearButton.setActionCommand("clear");
		Button addButton = null;
		addButton = dialog.addAListenedButton("Add Step", null, this);
		addButton.setActionCommand("add");
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		
		Button resetParamButton = null;
		resetParamButton = dialog.addAListenedButton("Review Settings", null, this);
		resetParamButton.setActionCommand("resetParams");
		dialog.addHorizontalLine(1);
		dialog.completeAndShowDialog("Process", "Cancel", null, "PROCESS");

		dialog.dispose();
		pauseUntilThreadDone();
		return (buttonPressed.getValue()==0);
	}
	PDFThread thread = null;
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		pauseUntilThreadDone();
		 thread = new PDFThread(this,  e.getActionCommand());
		thread.start();
	}
	void pauseUntilThreadDone() {
		try {
		while(thread != null) {
			Thread.sleep(20);
		}			
	}
	catch(Exception ex) {
	}
	}
	void doActionButton(String command) {
		if ("Add".equalsIgnoreCase(command)) { //You have hit ADD, so let's add to current script. 
		//Look for and hire the next processor, and capture its script for later use
		boolean wasUTIS = MesquiteThread.unknownThreadIsScripting;
		MesquiteThread.unknownThreadIsScripting = false;
		FileProcessor processor = (FileProcessor)processProject.getCoordinatorModule().hireEmployee(FileProcessor.class, "File processor (" + (fileProcessors.size() + 1)+ ")");
		MesquiteThread.unknownThreadIsScripting = wasUTIS;
		if (processor != null) {
			processor.setBaseDirectory(directoryPath);
			currentScript += "\naddProcessor " + " #" + processor.getClass().getName() + ";\n";
			String sn =Snapshot.getSnapshotCommands(processor, getProject().getHomeFile(), "  ");
			currentScript +="\ntell It;\n" + sn + "\nendTell;";
			recordProcessor(processor);
			setIntro(fromSavedScript);
			resetProcessorList();
			fromSavedScript = false;
		}
	}
	else if ("Clear".equalsIgnoreCase(command)) { //You have hit Clear all, so remove all processors. 
		removeAllProcessors();
		fromSavedScript = false;
		setIntro(fromSavedScript);
		currentScript = "";
		preferencesScript = "";
	} 
	else if ("Load".equalsIgnoreCase(command)) {  //You have hit Load, choose and execute stored script
		MesquiteFile f = MesquiteFile.open(true, (FilenameFilter)null, "Open text file with processing script", null);
		if (f!= null) {
			String script = MesquiteFile.getFileContentsAsString(f.getPath());
			if (!StringUtil.blank(script)) {
				removeAllProcessors();
				
				executeScript(script);
				currentScript = script;
				preferencesScript = script;
				resetProcessorList();
				fromSavedScript = true;
				setIntro(fromSavedScript);
			}
		}
	} 
	else if ("resetParams".equalsIgnoreCase(command)) {//Ask all processors to re-query regarding options
		for (int i= 0; i< fileProcessors.size(); i++){
			FileProcessor fProcessor = (FileProcessor)fileProcessors.elementAt(i);
			fProcessor.employeesQueryLocalOptions();
		}
		currentScript = recaptureScript();
		preferencesScript = currentScript;
		resetProcessorList();

	}
	}
	/*.................................................................................................................*/
	// set up the processors.
	protected boolean selectProcessors(){
		if (!firstFile)
			return true;
		//If there is a preferences script, start with it.
		if (preferencesScript != null){
			executeScript(preferencesScript); 			
			fromSavedScript = true;
		}	
		currentScript = preferencesScript;
		if (currentScript == null)
			currentScript = "";

		//Now show Dialog to allow user to set processing choices
		if (!showProcessDialog()){
			removeAllProcessors();
			return false;
		}

		//Process must have been hit. Capture the current script.
		preferencesScript = currentScript;
		storePreferences();

		return true;
	}
	/*.....................................................................................................*/
	void recordProcessor(FileProcessor processor){
		if (fileProcessors == null)
			fileProcessors = new Vector();
		if (fileProcessors.indexOf(processor)<0)
			fileProcessors.addElement(processor);
		int place = fileProcessors.indexOf(processor);
		if (place >0) {
			FileProcessor prev = ((FileProcessor)fileProcessors.elementAt(place-1));
			processor.setPreviousProcessorLabel("(" + (place) + ") " + prev.getNameAndParameters());
		}
	}
	/*.....................................................................................................*/
	String recaptureScript() {
		if (fileProcessors == null)
			return "";
		String s = "";
		for (int i = 0; i< fileProcessors.size(); i++) {
			FileProcessor processor = (FileProcessor)fileProcessors.elementAt(i);
			s += "\naddProcessor " + " #" + processor.getClass().getName() + ";\n";
			String sn =Snapshot.getSnapshotCommands(processor, getProject().getHomeFile(), "  ");
			s +="\ntell It;\n" + sn + "\nendTell;";
		}
		return s;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module processing files", "[name of module]", commandName, "addProcessor")) {
			FileProcessor processor = (FileProcessor)processProject.getCoordinatorModule().hireNamedEmployee(FileProcessor.class, arguments);
			if (processor!=null) {
				processor.setBaseDirectory(directoryPath);
				recordProcessor(processor);
			}
			return processor;
		}
		else 
			super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*=============================================================================================================*/
/* File processing methods   */
	/*.................................................................................................................*/
	
	public void writeFile(MesquiteFile nMF){
		NexusFileInterpreter nfi =(NexusFileInterpreter) fileCoord.findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
		if (nfi!=null) {
			nfi.writeFile(processProject, nMF);
		}
	}


	protected boolean firstResultsOverall = true;
	protected boolean firstResultsOverallFound = false;
	protected StringBuffer resultsHeading = new StringBuffer();

	/*.................................................................................................................*/
	boolean[] warned;
	protected boolean beforeProcessFiles() {
		if (fileProcessors != null){
			boolean success = true;
			for (int i= 0; i< fileProcessors.size() && success; i++){
				FileProcessor fProcessor = (FileProcessor)fileProcessors.elementAt(i);
				success = fProcessor.beforeProcessingSeriesOfFiles();
				if (!success)
					logln("Sorry,  " + fProcessor.getNameAndParameters() + " did not succeed.");
			}
			return success;
		}
		return true;
	}
	/*.................................................................................................................*/
	protected boolean afterProcessFiles() {
		if (fileProcessors != null){
			boolean success = true;
			for (int i= 0; i< fileProcessors.size() && success; i++){
				FileProcessor fProcessor = (FileProcessor)fileProcessors.elementAt(i);
				success = fProcessor.afterProcessingSeriesOfFiles();
				if (!success)
					logln("Sorry,  " + fProcessor.getNameAndParameters() + " did not succeed.");
			}
			return success;
		}
		return true;
	}
	/*.................................................................................................................*/
	protected boolean processFile(MesquiteFile fileToRead, StringBuffer results, MesquiteBoolean requestToSequester) {
		logln("Processing file " + fileToRead.getName() + " in " + fileToRead.getDirectoryName() + "...");
		if (fileProcessors == null)
			fileProcessors = new Vector();
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator2 = new ProgressIndicator(null,"Importing File "+ fileToRead.getName(), fileToRead.existingLength());
		progIndicator2.start();
		fileToRead.linkProgressIndicator(progIndicator2);
		fileToRead.readMesquiteBlock = false;
		if (fileToRead.openReading()) {
			importer.readFile(getProject(), fileToRead, null);	
			getProject().getCoordinatorModule().wrapUpAfterFileRead(fileToRead);
			//fileToRead.changeLocation(getSavedFilesDirectoryPath(fileToRead), fileToRead.getName() + ".nex");

			if (!selectProcessors()){  //needs to be done here after file read in case processors need to know if there are matrices etc in file
				if (progIndicator!=null) {
					progIndicator.setAbort();
					progIndicator.goAway();
				}
				processProject.removeAllFileElements(TreeVector.class, false);	
				processProject.removeAllFileElements(CharacterData.class, false);	
				processProject.removeAllFileElements(Taxa.class, false);	
				decrementMenuResetSuppression();
				firstFile = false;
				return false;
			}
			boolean firstResult = true;
			if (warned == null) {
				warned = new boolean[fileProcessors.size()];
				for (int i= 0; i< fileProcessors.size(); i++)
					warned[i] = false;
			}

			if (fileProcessors != null){
				boolean continuePlease = true;
				MesquiteString result = new MesquiteString();
				results.append(fileToRead.getFileName());
				for (int i= 0; i< fileProcessors.size() && continuePlease; i++){
					FileProcessor fProcessor = (FileProcessor)fileProcessors.elementAt(i);
					if (fProcessor!=null) {
						result.setValue((String)null);
						int returnCode = fProcessor.processFile(fileToRead, result);

						if (returnCode!=0) { //Debugg.println this fails if doing gene trees and raxml fails to get tree because of 3 taxa. Should there be different levels of failure?
							logln("Sorry,  " + fProcessor.getNameAndParameters() + " did not succeed in processing the file " + fileToRead.getFileName());
							if (returnCode <0 && !warned[i]) { //Debugg.println this fails if doing gene trees and raxml fails to get tree because of 3 taxa. Should there be different levels of failure?
								continuePlease = AlertDialog.query(containerOfModule(), "Processing step failed", "Processing of file " + fileToRead.getFileName() + " by " + fProcessor.getNameAndParameters() + " failed. Do you want to continue with this file?", "Continue", "Stop with This File");
								warned[i] = true;
							}
						}
						else if (firstFile) {
							logln("First file processed using " + fProcessor.getNameAndParameters());
						}
						if (continuePlease) {
							if (fProcessor.pleaseSequester()) {
								if (requestToSequester!=null)
									requestToSequester.setValue(true);
								fProcessor.setPleaseSequester(false);
							}
							if (returnCode == 0)
								logln("" + fProcessor.getNameAndParameters() + " successfully processed the file " + fileToRead.getFileName());
							if (result.getValue() != null) {
								firstResultsOverallFound = true;
								results.append("\t");
								results.append(result.getValue());
								results.append("");
								result.setValue((String)null);
								if (firstResultsOverall){
									if (firstResult)
										resultsHeading.append("File");
									resultsHeading.append("\t");
									resultsHeading.append(fProcessor.getNameAndParameters());
								}
							} else if (firstResultsOverall) {
								resultsHeading.append(fProcessor.getNameAndParameters()+StringUtil.lineEnding());
							}

						}
					} else
						logln("There was a problem processing files; one of the processors was null.");

				}
				firstResult = false;
			}

			results.append("\n");
			if (firstResultsOverall){
				resultsHeading.append("\n");
			}

			firstFile = false;
			processProject.removeAllFileElements(TreeVector.class, false);	
			processProject.removeAllFileElements(CharacterData.class, false);	
			processProject.removeAllFileElements(Taxa.class, false);	

		}
		decrementMenuResetSuppression();
		return true;
	}

	/*.................................................................................................................*/
	protected void adjustFile(MesquiteFile fileToWrite){
	}

	static String fileExtension = "";
	/*.................................................................................................................*/

	protected boolean firstFile = true;
	protected  void processDirectory(String directoryPath){
		if (StringUtil.blank(directoryPath))
			return;
		File directory = new File(directoryPath);
		MesquiteTimer timer = new MesquiteTimer();
		firstFile = true;
		processProject.getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(this);
		boolean abort = false;
		String path = "";
		StringBuffer results = new StringBuffer();
		if (directory!=null) {
			if (directory.exists() && directory.isDirectory()) {

				MesquiteBoolean requestToSequester= new MesquiteBoolean (false);
				String[] files = directory.list();
				progIndicator = new ProgressIndicator(null,"Processing Folder of Data Files", files.length);
				progIndicator.start();
				boolean sFDMade = false;

				String header = "Processing of files in " + directoryPath + StringUtil.lineEnding();
				Date dnow = new Date(System.currentTimeMillis());
				logln(StringUtil.getDateTime(dnow));
				header += StringUtil.getDateTime(dnow) + StringUtil.lineEnding() + StringUtil.lineEnding();
				MesquiteFile.putFileContents(writingFile.getDirectoryName() + "ProcessingResults", header, true);
				beforeProcessFiles();
				MesquiteThread.setQuietPlease(true);
				int filesFound = 0;
				for (int i=0; i<files.length; i++) {
					progIndicator.setCurrentValue(i);
					requestToSequester.setValue(false);
					if (progIndicator.isAborted()|| cancelProcessing) 
						abort = true;
					if (abort)
						break;
					if (i==1)
						timer.start();

					if (files[i]!=null) {
						boolean acceptableFile = (StringUtil.blank(fileExtension) || StringUtil.endsWithIgnoreCase(files[i], fileExtension));
						if (acceptableFile){
							path = directoryPath + MesquiteFile.fileSeparator + files[i];
							File cFile = new File(path);
							MesquiteFile file = new MesquiteFile();
							file.setPath(path);
							getProject().addFile(file);
							file.setProject(getProject());

							if (cFile.exists() && !cFile.isDirectory() && (!files[i].startsWith("."))) {
								results.setLength(0);
								filesFound++;
								boolean processFileRequestCancelled = !processFile( file, results, requestToSequester);  
								if (processFileRequestCancelled) 
									return;
								if ( firstResultsOverall && resultsHeading.length()>0){
									MesquiteFile.appendFileContents(writingFile.getDirectoryName() + "ProcessingResults", resultsHeading.toString(), true);
									MesquiteFile.appendFileContents(writingFile.getDirectoryName() + "ProcessingResults", StringUtil.lineEnding(), true);
									firstResultsOverall = false;
								}
								if (results.length()>0){
									MesquiteFile.appendFileContents(writingFile.getDirectoryName() + "ProcessingResults", results.toString(), true);
									MesquiteFile.appendFileContents(writingFile.getDirectoryName() + "ProcessingResults", StringUtil.lineEnding(), true);
								}
								logln(" ");
								if (requestToSequester.getValue()) {
									String sequesteredFileDirectoryPath = directoryPath + MesquiteFile.fileSeparator + "sequesteredFiles";
									if (!sFDMade)
										MesquiteFile.createDirectory(sequesteredFileDirectoryPath);
									File newFile = new File(sequesteredFileDirectoryPath+MesquiteFile.fileSeparator+MesquiteFile.getFileNameFromFilePath(path));
									cFile.renameTo(newFile); 
								}
							}

							processProject.getCoordinatorModule().closeFile(file, true);
						}
					}
				}
				if (filesFound == 0){
					if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "No files found"))  
						alert("No appropriate files with extension " + fileExtension + " were found in folder.");
					else
						discreetAlert("No appropriate files with extension " + fileExtension + " were found in folder.");

				}
				MesquiteThread.setQuietPlease(false);
				afterProcessFiles();

				String finalScript = recaptureScript();
				MesquiteFile.putFileContents(writingFile.getDirectoryName() + "ProcessingScript", finalScript, true);

				removeAllProcessors();
				progIndicator.goAway();
			}


		}
		//Debugg.println see if any other suppressions are on! pauseables?
		zeroMenuResetSuppression();
		logln("Total time for processing (excluding first file): " + timer.timeSinceVeryStartInHoursMinutesSeconds());

		processProject.getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(null);
	}
	public boolean okToInteractWithUser(int howImportant, String messageToUser){
		return firstFile;
	}
	/*.................................................................................................................*/
	protected String importerString = "NEXUS file";
	protected boolean getImporterName(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog =  new ExtensibleDialog(containerOfModule(), "File Format", buttonPressed);
		String message = "Choose the format of the data files to be processed";
		dialog.addLargeTextLabel(message);
		dialog.addBlankLine();
		dialog.suppressNewPanel();

		MesquiteModule[] fInterpreters = getFileCoordinator().getImmediateEmployeesWithDuty(FileInterpreterI.class);
		int count=1;
		for (int i=0; i<fInterpreters.length; i++) {
			if (((FileInterpreterI)fInterpreters[i]).canImport())
				count++;
		}
		String [] exporterNames = new String[count];
		exporterNames[0] = "NEXUS file";
		count = 1;
		int rememberedNumber = 0;
		for (int i=0; i<fInterpreters.length; i++)
			if (((FileInterpreterI)fInterpreters[i]).canImport()) {
				exporterNames[count] = fInterpreters[i].getName();
				if (exporterNames[count].equalsIgnoreCase(importerString))
					rememberedNumber = count;
				count++;
			}
		Choice exporterChoice = dialog.addPopUpMenu ("File Format", exporterNames, rememberedNumber);
		SingleLineTextField extension = dialog.addTextField ("Process only files with this extension (e.g. .nex, .fas): ", fileExtension, 5);
		dialog.addBlankLine();
		dialog.completeAndShowDialog();
		importerString = exporterChoice.getSelectedItem();
		fileExtension = extension.getText();
		dialog.dispose();
		dialog = null;
		storePreferences();
		return importerString != null;

	}
	/*.................................................................................................................*/


	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		boolean success= false;
		directoryPath = MesquiteFile.chooseDirectory("Choose directory containing data files:", lastDirectoryUsed); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
		if (StringUtil.blank(directoryPath))
			return null;
		fileCoord = getFileCoordinator();
		writingFile = new MesquiteFile();
		boolean importerFound = getImporterName(); //DLOG: Here asks for file importer

		if (!importerFound)
			return null;
		if (StringUtil.blank(importerString))
			return null;
		processProject = fileCoord.initiateProject(writingFile.getFileName(), writingFile);
		processProject.isProcessDataFilesProject = true;
		importer = (FileInterpreter)fileCoord.findEmployeeWithName(importerString);


		// what file reader?
		// filter by extension?
		// save script
		//
		lastDirectoryUsed = directoryPath;
		writingFile.setPath(directoryPath+MesquiteFile.fileSeparator+"temp.nex");
		processDirectory(directoryPath);  //DLOG: here asks for file extension filter and whether to save as NEXUS
	//and inside that, //DLOG asks abotu processors
		storePreferences(); //Do this regardless of success
		if (success){
			//project.autosave = true;
			processProject.isProcessDataFilesProject = false;
			return processProject;
		}
		processProject.isProcessDataFilesProject = false;
		processProject.developing = false;
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Process Data Files";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Process Data Files...";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}

	/*.................................................................................................................*/
	/** Returns the shortcut used for the menu item for the module*/
	public int getShortcutForMenuItem(){
		return KeyEvent.VK_O;
	}
	/*.................................................................................................................*/
	/** Returns whether the shortcut needs shift*/
	public boolean getShortcutForMenuItemNeedsShift(){
		return true;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Processes a folder of data files.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}

}


class PDFThread extends MesquiteThread {
	String command = null;
	ProcessDataFiles ownerModule;
	public PDFThread(ProcessDataFiles ownerModule, String command){
		this.ownerModule = ownerModule;
		this.command = command;
	}
	public void run() {
		ownerModule.doActionButton(command);
		ownerModule.thread = null;
	}

}




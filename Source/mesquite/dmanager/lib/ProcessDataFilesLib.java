/* Mesquite source code.  Copyright 2016 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.dmanager.lib; 

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.TextArea;
import java.io.*;
import java.util.Date;
import java.util.Vector;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class ProcessDataFilesLib extends GeneralFileMaker { 
	protected MesquiteProject project = null;
	protected FileCoordinator fileCoord = null;
	protected String directoryPath=null;
	protected ProgressIndicator progIndicator = null;
	protected FileInterpreter importer = null;
	protected MesquiteFile writingFile;

	protected String script = null;
	protected boolean incorporateScript = false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("script".equalsIgnoreCase(tag))
			script = StringUtil.cleanXMLEscapeCharacters(content);

	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "script", script);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	protected Vector fileProcessors = null;
	protected boolean cancelProcessing = false;

	protected boolean hireProcessorsIfNeeded(){
		if (!firstTime)
			return true;

		if (fileProcessors == null){
			fileProcessors = new Vector();

			String currentScript = null;
			while (showAlterDialog(fileProcessors.size())){
				if (currentScript == null){
					currentScript = script;
					if (currentScript == null)
						currentScript = "";
				}
				if (incorporateScript){
					if (script != null){    //HERE IT SHOULD QUERY and give a choice of options like availabel macros, saved with names, rather than just the single previous script
						Puppeteer p = new Puppeteer(this);
						CommandRecord mr = MesquiteThread.getCurrentCommandRecord();
						MesquiteThread.setCurrentCommandRecord(CommandRecord.macroRecord);
						p.execute(this, script, new MesquiteInteger(0), null, false);
						MesquiteThread.setCurrentCommandRecord(mr);
					}	
					incorporateScript = false;
				}
				else {
					FileProcessor processor = (FileProcessor)project.getCoordinatorModule().hireEmployee(FileProcessor.class, "File processor (" + (fileProcessors.size() + 1)+ ")");
					if (processor == null)
						return false;
					currentScript += "\naddProcessor " + " #" + processor.getClass().getName() + ";\n";
					String sn =Snapshot.getSnapshotCommands(processor, getProject().getHomeFile(), "  ");
					currentScript +="\ntell It;\n" + sn + "\nendTell;";
					fileProcessors.addElement(processor);
					if (cancelProcessing){
						if (fileProcessors != null){
							for (int i= 0; i< fileProcessors.size(); i++){
								FileProcessor alterer = (FileProcessor)fileProcessors.elementAt(i);
								project.getCoordinatorModule().fireEmployee(alterer);
							}
						}
						return false;
					}
				}

			}
			script = currentScript;
			storePreferences();
		}
		return true;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module processing files", "[name of module]", commandName, "addProcessor")) {
			FileProcessor processor = (FileProcessor)project.getCoordinatorModule().hireNamedEmployee(FileProcessor.class, arguments);
			if (processor!=null) {
				if (fileProcessors == null)
					fileProcessors = new Vector();
				fileProcessors.addElement(processor);
			}
			return processor;
		}
		else 
			super.doCommand(commandName, arguments, checker);
		return null;
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
	public void writeFile(MesquiteFile nMF){
		NexusFileInterpreter nfi =(NexusFileInterpreter) fileCoord.findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
		if (nfi!=null) {
			nfi.writeFile(project, nMF);
		}
	}



	/*.................................................................................................................*/
	public boolean showAlterDialog(int count) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Add File Processor?",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		boolean initialSetup = count == 0;
		if (initialSetup){
			dialog.addLabel("For each file examined, do you want to process it?");
			String b2 = "Use Previous";
			if (StringUtil.blank(script))
				b2 = null;
			dialog.completeAndShowDialog("Yes", "No", b2, "No");
		}
		else {
			dialog.addLabel("For each file examined, do you want to add another step in processing it?");
			dialog.addLabel("The processing steps already requested are:");
			String[] steps = new String[fileProcessors.size()];
			for (int i = 0; i<steps.length; i++){
				if (fileProcessors.elementAt(i)!=null)
					steps[i] = "(" + (i+1) + ") " + ((FileProcessor)fileProcessors.elementAt(i)).getNameAndParameters();
			}
			dialog.addList (steps, null, null, 8);
			dialog.completeAndShowDialog("Add", "Done", "Cancel", "Done");
		}
		dialog.dispose();
		boolean addProcess =  (buttonPressed.getValue()==0);
		if (buttonPressed.getValue()==2) {
			if (initialSetup) {
				addProcess = true;
				incorporateScript = true;				
			}
			else
				cancelProcessing = true;
		}
		else if (initialSetup)
			script = "";
		return addProcess;
	}

	protected boolean firstResultsOverall = true;
	protected boolean firstResultsOverallFound = false;
	protected StringBuffer resultsHeading = new StringBuffer();
	/*.................................................................................................................*/
	protected void processFile(MesquiteFile fileToRead, StringBuffer results) {
		logln("Processing file " + fileToRead.getName() + " in " + fileToRead.getDirectoryName() + "...");
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(null,"Importing File "+ fileToRead.getName(), fileToRead.existingLength());
		progIndicator.start();
		fileToRead.linkProgressIndicator(progIndicator);
		fileToRead.readMesquiteBlock = false;
		if (fileToRead.openReading()) {
			importer.readFile(getProject(), fileToRead, null);	
			getProject().getCoordinatorModule().wrapUpAfterFileRead(fileToRead);
			fileToRead.changeLocation(fileToRead.getDirectoryName() + "savedFiles" + MesquiteFile.fileSeparator, fileToRead.getName() + ".nex");

			if (!hireProcessorsIfNeeded()){  //needs to be done here after file read in case alterers need to know if there are matrices etc in file
				return;
			}

			boolean firstResult = true;
			if (fileProcessors != null){
				boolean success = true;
				MesquiteString result = new MesquiteString();
				results.append(fileToRead.getFileName());
				for (int i= 0; i< fileProcessors.size() && success; i++){
					FileProcessor alterer = (FileProcessor)fileProcessors.elementAt(i);
					if (alterer!=null) {
						result.setValue((String)null);
						success = alterer.processFile(fileToRead, result);

						if (!success)
							logln("Sorry,  " + alterer.getNameAndParameters() + " did not succeed in processing the file " + fileToRead.getFileName());
						else {
							logln("" + alterer.getNameAndParameters() + " successfully processed the file " + fileToRead.getFileName());
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
									resultsHeading.append(alterer.getNameAndParameters());
								}
							}
						}
					} else
						logln("There was a problem processing files; one of the processors was null.");

				}
				firstResult = false;
			}
			//processData((DNAData)data, taxa, proteinCoding);   // query about this
			results.append("\n");
			if (firstResultsOverall){
				resultsHeading.append("\n");
			}
			if (autoNEXUSSave)
				writeFile(fileToRead);
			adjustFile(fileToRead);

			firstTime = false;
			project.removeAllFileElements(TreeVector.class, false);	
			project.removeAllFileElements(CharacterData.class, false);	
			project.removeAllFileElements(Taxa.class, false);	

		}
		decrementMenuResetSuppression();
	}

	/*.................................................................................................................*/
	protected void adjustFile(MesquiteFile fileToWrite){
	}
	static boolean autoNEXUSSave = true;
	static String fileExtension = "";
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Process file options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Process file options");
		Checkbox autoSave = dialog.addCheckBox("Save files as NEXUS", autoNEXUSSave);
		SingleLineTextField extension = dialog.addTextField ("Process only files with this extension (e.g. .nex, .fas): ", "", 5);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			autoNEXUSSave=autoSave.getState();
			fileExtension = extension.getText();
			//storePreferences();

		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/

	protected boolean firstTime = true;
	protected  void processDirectory(String directoryPath){
		if (StringUtil.blank(directoryPath))
			return;
		File directory = new File(directoryPath);
		//	parser = new Parser();
		firstTime = true;
		project.getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(this);
		boolean abort = false;
		String path = "";
		StringBuffer results = new StringBuffer();
		if (directory!=null) {
			if (directory.exists() && directory.isDirectory()) {
				//Hire file alterers

				if (queryOptions()){
					String[] files = directory.list();
					progIndicator = new ProgressIndicator(null,"Processing Folder of Data Files", files.length);
					progIndicator.start();
					MesquiteFile.createDirectory(directoryPath + MesquiteFile.fileSeparator + "savedFiles");
					String header = "Processing of files in " + directoryPath + StringUtil.lineEnding();
					Date dnow = new Date(System.currentTimeMillis());
					logln(StringUtil.getDateTime(dnow));
					header += StringUtil.getDateTime(dnow) + StringUtil.lineEnding() + StringUtil.lineEnding();
					MesquiteFile.putFileContents(writingFile.getDirectoryName() + "ProcessingResults.txt", header, true);
					for (int i=0; i<files.length; i++) {
						progIndicator.setCurrentValue(i);
						if (progIndicator.isAborted()|| cancelProcessing) 
							abort = true;
						if (abort)
							break;
						if (files[i]!=null) {
							boolean acceptableFile = (StringUtil.blank(fileExtension) || StringUtil.endsWithIgnoreCase(files[i], fileExtension));
							if (acceptableFile){
								path = directoryPath + MesquiteFile.fileSeparator + files[i];
								File cFile = new File(path);
								MesquiteFile file = new MesquiteFile();
								file.setPath(path);
								getProject().addFile(file);
								file.setProject(getProject());
								//	getProject().setHomeFile(file);
								if (cFile.exists() && !cFile.isDirectory() && (!files[i].startsWith("."))) {
									results.setLength(0);
									processFile( file, results);
									if (firstResultsOverallFound && firstResultsOverall && resultsHeading.length()>0){
										MesquiteFile.appendFileContents(writingFile.getDirectoryName() + "ProcessingResults.txt", resultsHeading.toString(), true);
										MesquiteFile.appendFileContents(writingFile.getDirectoryName() + "ProcessingResults.txt", StringUtil.lineEnding(), true);
										firstResultsOverall = false;
									}
									if (results.length()>0){
										MesquiteFile.appendFileContents(writingFile.getDirectoryName() + "ProcessingResults.txt", results.toString(), true);
										MesquiteFile.appendFileContents(writingFile.getDirectoryName() + "ProcessingResults.txt", StringUtil.lineEnding(), true);
									}
									logln(" ");
								}
								
								project.getCoordinatorModule().closeFile(file, true);
							}
						}
					}
					progIndicator.goAway();
				}

			}
		}
		project.getCoordinatorModule().setWhomToAskIfOKToInteractWithUser(null);
	}
	public boolean okToInteractWithUser(int howImportant, String messageToUser){
		return firstTime;
	}
	/*.................................................................................................................*/
	protected String importerString = "NEXUS file";
	protected String getImporterName(){
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
		for (int i=0; i<fInterpreters.length; i++)
			if (((FileInterpreterI)fInterpreters[i]).canImport()) {
				exporterNames[count] = fInterpreters[i].getName();
				count++;
			}

		Choice exporterChoice = dialog.addPopUpMenu ("File Format", exporterNames, 0);
		exporterChoice.select(importerString);
		dialog.addBlankLine();
		dialog.completeAndShowDialog();
		importerString = exporterChoice.getSelectedItem();

		dialog.dispose();
		dialog = null;
		return importerString;

	}
	/*.................................................................................................................*/


	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		boolean success= false;
		directoryPath = MesquiteFile.chooseDirectory("Choose directory containing data files:", null); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
		if (StringUtil.blank(directoryPath))
			return null;
		fileCoord = getFileCoordinator();
		writingFile = new MesquiteFile();
		String importerString = getImporterName();
		if (StringUtil.blank(importerString))
			return null;
		project = fileCoord.initiateProject(writingFile.getFileName(), writingFile);
		importer = (FileInterpreter)fileCoord.findEmployeeWithName(importerString);


		// what file reader?
		// filter by extension?
		// save script
		//

		writingFile.setPath(directoryPath+MesquiteFile.fileSeparator+"temp.nex");
		processDirectory(directoryPath);

		if (success){
			project.autosave = true;
			return project;
		}
		project.developing = false;
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
	public String getExplanation() {
		return "Processes a folder of data files.";
	}
}





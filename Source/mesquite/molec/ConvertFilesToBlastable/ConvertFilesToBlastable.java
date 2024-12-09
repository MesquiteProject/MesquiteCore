/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.
Version 1.0   December 2011
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.molec.ConvertFilesToBlastable; 

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import mesquite.externalCommunication.AppHarvester.AppHarvester;
import mesquite.externalCommunication.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.molec.lib.*;


/* ======================================================================== */
public class ConvertFilesToBlastable extends UtilitiesAssistant implements ActionListener,  AppUser, ProcessWatcher, OutputFileProcessor { 
	boolean preferencesSet = false;
	ExternalProcessManager externalProcessManager;
	boolean scriptBased = false;
	boolean databasesInDefaultLocation = true;
	String blastDatabaseFolderPath = "";
	static String previousDirectory = null;
	ProgressIndicator progIndicator = null;
	String extension ="fas";

	static String blastProgram = "makeblastdb";

	
	boolean hasApp=false;
	boolean useDefaultExecutablePath = true;  //newApp
	String blastExecutableFolderPath = "";
	AppInformationFile appInfoFile;


	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		addMenuItem(null, "Make BLASTable files from FASTA...", makeCommand("makeBLASTable", this));
		loadPreferences();
		appInfoFile = getAppInfoFile();
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public boolean getHasApp() {
		return hasApp;
	}
	public void setHasApp(boolean hasApp) {
		this.hasApp = hasApp;
	}
	public String getAppOfficialName() {
		return "blast";
	}
	public String getProgramName(){
		return "BLAST";
	}

	public boolean getDefaultExecutablePathAllowed() {
		return getHasApp();
	}
	/*.................................................................................................................*/
	public AppInformationFile getAppInfoFile() {
		AppInformationFile aif = AppHarvester.getAppInfoFileForProgram(this);
		setHasApp(aif!=null);
		return aif;
	}
	/*.................................................................................................................*/
	public String getDefaultExecutablePath(){
		if (appInfoFile==null) {
			appInfoFile = getAppInfoFile();
		}
		if (appInfoFile!=null) {
			String fullPath = appInfoFile.getFullPath();
			Debugg.println(getAppInfoForLog());
			return fullPath;
		}
		return null;
	}
	/*.................................................................................................................*/
	public String getVersionFromAppInfo(){
		if (!useDefaultExecutablePath || !getDefaultExecutablePathAllowed()) 
			return null;
		if (appInfoFile==null) {
			appInfoFile = getAppInfoFile();
		}
		if (appInfoFile!=null) {
			return appInfoFile.getVersion();
		}
		return null;
	}

	/*.................................................................................................................*/
	public String getAppInfoForLog(){
		if (appInfoFile==null) {
			appInfoFile = getAppInfoFile();
		}
		if (appInfoFile!=null) {
			StringBuffer sb = new StringBuffer(0);
			sb.append("\nmakeblastdb version " + appInfoFile.getVersion());
			return sb.toString();
		}
		return null;
	}
	/*.................................................................................................................*/
	public String getExecutablePath(){
		if (useDefaultExecutablePath && getDefaultExecutablePathAllowed()) 
			return getDefaultExecutablePath();
		else
			return blastExecutableFolderPath;
	}

	public boolean useAppInAppFolder() {
		return useDefaultExecutablePath && getDefaultExecutablePathAllowed();
	}

	
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		 if ("blastExecutableFolderPath".equalsIgnoreCase(tag))
			blastExecutableFolderPath = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("useDefaultExecutablePath".equalsIgnoreCase(tag))
			useDefaultExecutablePath = MesquiteBoolean.fromTrueFalseString(content);
		else  if ("extension".equalsIgnoreCase(tag))
			extension = StringUtil.cleanXMLEscapeCharacters(content);

		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "blastExecutableFolderPath", blastExecutableFolderPath);  
		StringUtil.appendXMLTag(buffer, 2, "useDefaultExecutablePath", useDefaultExecutablePath);  
		StringUtil.appendXMLTag(buffer, 2, "extension", extension);  

		preferencesSet = true;
		return buffer.toString();
	}
	SingleLineTextField executablePathField =  null;
	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("browseExecutable")) {
			String directoryName = "";
			String path = MesquiteFile.chooseDirectory("Choose directory containing the BLAST programs", directoryName);
			if (StringUtil.notEmpty(path))
				executablePathField.setText(path);
		}
	}

	Checkbox defaultExecutablePathCheckBox;
	/*.................................................................................................................*/
	public boolean queryOptions() {
		appInfoFile = getAppInfoFile();

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Make BLASTable Database Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Make BLASTable Database Options");
		StringBuffer sb = new StringBuffer();
		sb.append("To use this local BLAST tool, you need to have installed the BLAST program on this computer. ");
		dialog.appendToHelpString(sb.toString());


		//public TextArea addTextAreaSmallFont (String initialString, int numRows, int numColumns) {


		//Checkbox executablesInDefaultLocationBox = dialog.addCheckBox("BLAST programs in default location", executablesInDefaultLocation);
	//	executablePathField = dialog.addTextField("Path to folder containing BLAST programs", blastExecutableFolderPath, 40);
	/*	if (getDefaultExecutablePathAllowed()) {
			defaultExecutablePathCheckBox = dialog.addCheckBox("Use built-in apps for BLAST programs", useDefaultExecutablePath);
			executablePathField = dialog.addTextField("Path to alternative folder containing BLAST programs", blastExecutableFolderPath, 40);
		} else {
			executablePathField = dialog.addTextField("Path to alternative folder containing BLAST programs", blastExecutableFolderPath, 40);
			useDefaultExecutablePath=false;
		}
		Button browseButton = dialog.addAListenedButton("Browse...",null, this);
		browseButton.setActionCommand("browseExecutable");
		*/

		AppChooser appChooser = new AppChooser(this, useDefaultExecutablePath, blastExecutableFolderPath);
		appChooser.addToDialog(dialog);

		
		SingleLineTextField extensionField = dialog.addTextField("Extension for FASTA files:", extension, 10);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			
			useDefaultExecutablePath = appChooser.useBuiltInExecutable(); //for preference writing
			blastExecutableFolderPath = appChooser.getManualPath(); //for preference writing

			/*if (defaultExecutablePathCheckBox!=null)
				useDefaultExecutablePath = defaultExecutablePathCheckBox.getState();
			String tempPath = executablePathField.getText();
			if (StringUtil.blank(tempPath) && !useDefaultExecutablePath){
				MesquiteMessage.discreetNotifyUser("The path to BLAST programs must be entered.");
				return false;
			}
			blastExecutableFolderPath = tempPath;
			*/
			
			String tempExtension = extensionField.getText();
			if (StringUtil.notEmpty(tempExtension)){
				extension = tempExtension;
			}

			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public void makeBLASTdb(String directoryPath, String fileName) {

		String unique = MesquiteTrunk.getUniqueIDBase();
		String runningFilePath = directoryPath + "running" + MesquiteFile.massageStringToFilePathSafe(unique);
		String outFileName = "blastResults" + MesquiteFile.massageStringToFilePathSafe(unique);
		String outFilePath = directoryPath + outFileName;
		String[] outputFilePaths = new String[1];
		outputFilePaths[0] = outFilePath;
		
		String fileNameBase = StringUtil.getAllButLastItem(fileName, ".");
		
		String blastArguments = " -in " + NCBIUtil.getBLASTFileInputName(fileName) + " -out " + StringUtil.blanksToUnderline(fileNameBase + "DB") + " -dbtype nucl -blastdb_version 4";


		String blastCommand = blastProgram + blastArguments;
		String programPath = blastProgram;
		programPath = getExecutablePath() + MesquiteFile.fileSeparator +blastProgram;
		
		logln("\n...................\nBLAST command: \n" + blastCommand);



		boolean success = false;

		String arguments = blastArguments;
		arguments=StringUtil.stripBoundingWhitespace(arguments);
		externalProcessManager = new ExternalProcessManager(this, directoryPath, programPath, arguments, getName(), outputFilePaths, this, this, true);
		externalProcessManager.setStdOutFileName(ShellScriptRunner.stOutFileName);

		success = externalProcessManager.executeInShell();
		if (success)
			success = externalProcessManager.monitorAndCleanUpShell(null);

		if (getProject()!=null)
			getProject().decrementProjectWindowSuppression();
	}	
	/*.................................................................................................................*/
	public void processFiles(String directoryPath) { 
		if (StringUtil.blank(directoryPath)) {
			directoryPath = MesquiteFile.chooseDirectory("Choose directory containing FASTA files to be processed:", previousDirectory); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
		}
		if (StringUtil.blank(directoryPath)) {
			return;
		}
		File directory = new File(directoryPath);
		previousDirectory = directory.getParent();
		storePreferences();
		if (directory.exists() && directory.isDirectory()) {
			String[] files = directory.list();
			progIndicator = new ProgressIndicator(getProject(),"Making BLASTable databases", files.length);
			progIndicator.setStopButtonName("Stop");
			progIndicator.start();
			boolean abort = false;
			int numProcessed = 0;

			for (int i=0; i<files.length; i++) {
				if (progIndicator!=null){
					progIndicator.setCurrentValue(i);
					progIndicator.setText("Number of files processed: " + numProcessed);
					if (progIndicator.isAborted())
						abort = true;
				}
				if (abort)
					break;
				if (files[i]==null )
					;
				else {
					if ((!files[i].startsWith(".")) && (files[i].endsWith("." + extension))) {
						makeBLASTdb(directoryPath, files[i]);
						numProcessed++;
					}
				}
			}

			if (!abort) {
				progIndicator.spin();
			}

			if (progIndicator!=null)
				progIndicator.goAway();

		}
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Calls makeblastdb on each FASTA file in directory", null, commandName, "makeBLASTable")) {
			if (queryOptions())
				processFiles(null);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Make BLASTable files from FASTA";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Make BLASTable files from FASTA...";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Calls makeblastdb on each FASTA file in directory.";
	}
	/*.................................................................................................................*/
	@Override
	public String[] modifyOutputPaths(String[] outputFilePaths) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void processOutputFile(String[] outputFilePaths, int fileNum) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void processCompletedOutputFiles(String[] outputFilePaths) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean continueProcess(Process proc) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean userAborted() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean fatalErrorDetected() {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean warnIfError() {
		return true;
	}

}





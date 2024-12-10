/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.LocalBlaster;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import mesquite.lib.*;
import mesquite.molec.lib.*;
import mesquite.externalCommunication.AppHarvester.AppHarvester;
import mesquite.externalCommunication.lib.*;

/*  Initiator: DRM
 * */

public class LocalBlaster extends Blaster implements ActionListener,  AppUser, ProcessWatcher, OutputFileProcessor {
	boolean preferencesSet = false;
	String programOptions = "" ;
	String databaseString = "*" ;
	int numThreads = 1;
	MesquiteTimer timer = new MesquiteTimer();
	boolean localBlastDBHasTaxonomyIDs = false;
	boolean useIDInDefinition = false;
	String[] databaseArray = null;
	int numDatabases = 0;
	ExternalProcessManager externalProcessManager;
	static final  boolean scriptBased = false;
	boolean databasesInDefaultLocation = true;
	String blastDatabaseFolderPath = "";
	boolean pathWithSpaces = false;

	
	boolean hasApp=false;
	boolean useDefaultExecutablePath = true;  //newApp
	String blastExecutableFolderPath = "";
	AppInformationFile appInfoFile;


	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		programOptions = "";
		timer.start();
		appInfoFile = getAppInfoFile();
		loadPreferences();
		return true;
	}

	/*
	 -evalue 0.01
	 */

	public  int getUpperLimitMaxHits(){
		return 100;
	}

	public boolean userAborted(){
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
	public boolean requestPrimaryChoice() {
		return true;
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
			sb.append("\nBLAST executable version " + appInfoFile.getVersion());
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
		if ("programOptions".equalsIgnoreCase(tag))
			programOptions = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("databases".equalsIgnoreCase(tag))
			databaseString = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("numThreads".equalsIgnoreCase(tag))
			numThreads = MesquiteInteger.fromString(content);
		else if ("localBlastDBHasTaxonomyIDs".equalsIgnoreCase(tag))
			localBlastDBHasTaxonomyIDs = MesquiteBoolean.fromTrueFalseString(content);
		else if ("useIDInDefinition".equalsIgnoreCase(tag))
			useIDInDefinition = MesquiteBoolean.fromTrueFalseString(content);
		else if ("blastExecutableFolderPath".equalsIgnoreCase(tag))
			blastExecutableFolderPath = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("blastDatabaseFolderPath".equalsIgnoreCase(tag))
			blastDatabaseFolderPath = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("useDefaultExecutablePath".equalsIgnoreCase(tag))
			useDefaultExecutablePath = MesquiteBoolean.fromTrueFalseString(content);
		else if ("databasesInDefaultLocation".equalsIgnoreCase(tag))
			databasesInDefaultLocation = MesquiteBoolean.fromTrueFalseString(content);
		

		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "programOptions", programOptions);  
		StringUtil.appendXMLTag(buffer, 2, "databases", databaseString);  
		StringUtil.appendXMLTag(buffer, 2, "numThreads", numThreads);  
		StringUtil.appendXMLTag(buffer, 2, "localBlastDBHasTaxonomyIDs", localBlastDBHasTaxonomyIDs);  
		StringUtil.appendXMLTag(buffer, 2, "useIDInDefinition", useIDInDefinition);  
		StringUtil.appendXMLTag(buffer, 2, "blastExecutableFolderPath", blastExecutableFolderPath);  
		StringUtil.appendXMLTag(buffer, 2, "blastDatabaseFolderPath", blastDatabaseFolderPath);  
		StringUtil.appendXMLTag(buffer, 2, "useDefaultExecutablePath", useDefaultExecutablePath);  
		StringUtil.appendXMLTag(buffer, 2, "databasesInDefaultLocation", databasesInDefaultLocation);  

		preferencesSet = true;
		return buffer.toString();
	}

	public boolean initialize() {
		if (!MesquiteThread.isScripting())
			return queryOptions();
		return true;
	}
	/*.................................................................................................................*/
	public String getStdErr() {
		 if (externalProcessManager!=null)
			return externalProcessManager.getStdErr();
		return "";
	}
	/*.................................................................................................................*/
	public boolean useDefaultStdOutFileName() {
		return false;
	}
	/*.................................................................................................................*/
	public String getStdOut() {
		if (externalProcessManager!=null)
			return externalProcessManager.getStdOut();
		return "";
	}
	
	public boolean stopExecution(){
		if (externalProcessManager!=null) {
			externalProcessManager.stopExecution();
		}
		return false;
	}
	

	/*.................................................................................................................*/
	public String getDatabaseName () {
		if (databaseString==null)
			return "";
		return databaseString;
	}

	/*.................................................................................................................*/
	public int getNumPhdFilesInDirectory(File folder, String aceFileDirectoryPath) {
		int count = 0;
		return count;
	}
	/*.................................................................................................................*/
	public void allInFolder () {
		File folder = new File(blastDatabaseFolderPath);
		int count=0;
		if (folder.isDirectory()) {
			String[] files = folder.list();
			for (int i=0; i<files.length; i++) { // going through the folders and finding the nsq files to count them
				if (files[i]!=null ) {
					String filePath = blastDatabaseFolderPath + MesquiteFile.fileSeparator + files[i];
					File cFile = new File(filePath);
					if (cFile.exists()) {
						if (!cFile.isDirectory()) {
							 if (files[i].endsWith(".nsq")) {
								count++;
							}
						}
					}
				}
			}
			databaseArray= new String[count];
			numDatabases=count;
			count=0;
			for (int i=0; i<files.length; i++) { // now that we have the count, store details
				if (files[i]!=null ) {
					String filePath = blastDatabaseFolderPath + MesquiteFile.fileSeparator + files[i];
					File cFile = new File(filePath);
					if (cFile.exists()) {
						if (!cFile.isDirectory()) {
							 if (files[i].endsWith(".nsq")) {
								 String fileNameBase = StringUtil.getAllButLastItem(files[i], ".");
								databaseArray[count]= blastDatabaseFolderPath + MesquiteFile.fileSeparator +fileNameBase;
								count++;
							}
						}
					}
				}
			}
		}

	}
	/*.................................................................................................................*/
	public void processDatabases (String s) {
		if (s==null)
			databaseArray=null;
		else if (s.equals("*")) {
			if (databasesInDefaultLocation)
				MesquiteMessage.discreetNotifyUser("Can only process all files if in non-default location");
			else
				allInFolder();
		}
		else if (s.indexOf(",")<0){
			databaseArray= new String[1];
			if (databasesInDefaultLocation)
				databaseArray[0]=s;
			else 
				databaseArray[0]= blastDatabaseFolderPath + MesquiteFile.fileSeparator +s;
			numDatabases=1;
		} else {
			Parser parser = new Parser(s);
			parser.setWhitespaceString(",");
			parser.setPunctuationString("");
			String token = "";
			int count=0;
			while (!parser.atEnd()) {
				token = parser.getNextToken();
				token = StringUtil.stripBoundingWhitespace(token);
				if (StringUtil.notEmpty(token))
					count++;
			}
			numDatabases=count;
			if (count==0){
				databaseArray = null;
				return;
			}
			databaseArray = new String[count];
			parser.setString(s);
			parser.setConvertUnderscoresToBlanks(false);
			count=0;
			while (!parser.atEnd()) {
				token = parser.getNextToken();
				token = StringUtil.stripBoundingWhitespace(token);
				if (StringUtil.notEmpty(token)) {
					if (databasesInDefaultLocation)
						databaseArray[count]=token;
					else 
						databaseArray[count]= blastDatabaseFolderPath + MesquiteFile.fileSeparator +token;
					count++;
				}
			}
		}
	}

	SingleLineTextField executablePathField =  null;
	SingleLineTextField databasePathField =  null;
	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("browseExecutable")) {
			String directoryName = "";
			String path = MesquiteFile.chooseDirectory("Choose directory containing the BLAST programs", directoryName);
			if (StringUtil.notEmpty(path))
				executablePathField.setText(path);
		} else if (e.getActionCommand().equalsIgnoreCase("browseDatabase")) {
			String directoryName = "";
			String path = MesquiteFile.chooseDirectory("Choose directory containing the BLAST databases", directoryName);
			if (StringUtil.notEmpty(path))
				databasePathField.setText(path);
		}
	}

	Checkbox defaultExecutablePathCheckBox =  null;

	/*.................................................................................................................*/
	public boolean queryOptions() {
		appInfoFile = getAppInfoFile();
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Local BLAST Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Local BLAST Options");
		StringBuffer sb = new StringBuffer();
		sb.append("To use this local BLAST tool, you need to have installed the BLAST program on this computer, and need to have also set up local BLAST databases on your computer. ");
		sb.append("You can do this by following the instructions at <a href=\"http://www.ncbi.nlm.nih.gov/guide/howto/run-blast-local/\">http://www.ncbi.nlm.nih.gov/guide/howto/run-blast-local/</a>.  <br><br>If you build your own BLAST database from ");
		sb.append("a fasta file using the makeblastdb command-line tool (e.g., using \"makeblastdb -in fastafilename -out outputblastabledatabasefilename -dbtype nucl\"), then both Use ID in Definition and Local BLAST database has NCBI taxonomy IDs should be unchecked. ");
		sb.append("<br><br>");
		sb.append("If you are going to do a blastX to a local protein database that you downloaded from GenBank, you will need to check Use ID in Definition.");
		dialog.appendToHelpString(sb.toString());

		AppChooser appChooser = new AppChooser(this, useDefaultExecutablePath, blastExecutableFolderPath);
		appChooser.addToDialog(dialog);
		IntegerField numThreadsField = dialog.addIntegerField("Number of processor threads to use:", numThreads,4, 1, Integer.MAX_VALUE);

/*		if (getDefaultExecutablePathAllowed()) {
			defaultExecutablePathCheckBox = dialog.addCheckBox("Use built-in apps for BLAST programs", useDefaultExecutablePath);
			executablePathField = dialog.addTextField("Path to alternative folder containing BLAST programs", blastExecutableFolderPath, 40);
		} else {
			executablePathField = dialog.addTextField("Path to alternative folder containing BLAST programs", blastExecutableFolderPath, 40);
			useDefaultExecutablePath=false;
		}
		Button browseButton = dialog.addAListenedButton("Browse...",null, this);
		browseButton.setActionCommand("browseExecutable");
*/

		//public TextArea addTextAreaSmallFont (String initialString, int numRows, int numColumns) {
		SingleLineTextField programOptionsField = dialog.addTextField("Additional BLAST options:", programOptions, 40, true);
		Checkbox useIDInDefinitionBox = dialog.addCheckBox("Use ID in Definition (NCBI-provided databases)", useIDInDefinition);
		Checkbox localBlastDBHasTaxonomyIDsBox = dialog.addCheckBox("Local BLAST database has NCBI taxonomy IDs", localBlastDBHasTaxonomyIDs);

		dialog.addHorizontalLine(1);
		Checkbox databasesInDefaultLocationBox = dialog.addCheckBox("BLAST databases in default location", databasesInDefaultLocation);
		databasePathField = dialog.addTextField("Path to folder containing BLAST databases", blastDatabaseFolderPath, 40);
		Button browse2Button = dialog.addAListenedButton("Browse...",null, this);
		browse2Button.setActionCommand("browseDatabase");

		dialog.addLabel("Databases to search (separate by commas):");
		TextArea databasesField = dialog.addTextAreaSmallFont(databaseString, 5, 50);

		
		
		

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			databaseString = databasesField.getText();
			databaseString = StringUtil.removeNewLines(databaseString);
			programOptions = programOptionsField.getText();
			numThreads = numThreadsField.getValue();
			localBlastDBHasTaxonomyIDs = localBlastDBHasTaxonomyIDsBox.getState();
			useIDInDefinition = useIDInDefinitionBox.getState();
			
			useDefaultExecutablePath = appChooser.useBuiltInExecutable(); //for preference writing
			blastExecutableFolderPath = appChooser.getManualPath(); //for preference writing

/*			if (defaultExecutablePathCheckBox!=null)
				useDefaultExecutablePath = defaultExecutablePathCheckBox.getState();
			String tempPath = executablePathField.getText();
			if (StringUtil.blank(tempPath) && !useDefaultExecutablePath){
				MesquiteMessage.discreetNotifyUser("The path to BLAST programs must be entered.");
				return false;
			}
			blastExecutableFolderPath = tempPath;
*/			
			databasesInDefaultLocation = databasesInDefaultLocationBox.getState();
			String tempPath = databasePathField.getText();
			if (StringUtil.blank(tempPath) && !databasesInDefaultLocation){
				MesquiteMessage.discreetNotifyUser("The path to BLAST databases must be entered.");
				return false;
			}
			blastDatabaseFolderPath = tempPath;
			if (StringUtil.containsBlanks(blastDatabaseFolderPath)) {
				MesquiteMessage.discreetNotifyUser("Path to BLAST databases contains at least one blank; BLAST functions may fail.");
				pathWithSpaces = true;
			}
			processDatabases(databaseString);
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	public int getNumDatabases (){
		return numDatabases;
	}

	public String getDatabase (int iDatabase){
		if (databaseArray==null || databaseArray.length==0)
			return null;
		if (iDatabase<0 || iDatabase>= databaseArray.length)
			return null;
		return databaseArray[iDatabase];
//		return NCBIUtil.getBLASTFileInputName(databaseArray[iDatabase]);
	}
	

	/*.................................................................................................................*/
	public void blastForMatches(String database, String blastType, String sequenceName, String sequence, boolean isNucleotides, int numHits, int maxTime,  double eValueCutoff, int wordSize, StringBuffer blastResponse, boolean writeCommand) {

		getProject().incrementProjectWindowSuppression();

		String unique = MesquiteTrunk.getUniqueIDBase();
		String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  
		String fileName = "sequenceToSearch" + MesquiteFile.massageStringToFilePathSafe(unique) + ".fa";   
		String filePath = rootDir +  fileName;

		StringBuffer fileBuffer = new StringBuffer();
		fileBuffer.append(NCBIUtil.createFastaString(sequenceName, sequence, isNucleotides));
		MesquiteFile.putFileContents(filePath, fileBuffer.toString(), true);


		String runningFilePath = rootDir + "running" + MesquiteFile.massageStringToFilePathSafe(unique);
		String outFileName = "blastResults" + MesquiteFile.massageStringToFilePathSafe(unique);
		String outFilePath = rootDir + outFileName;
		String[] outputFilePaths = new String[1];
		outputFilePaths[0] = outFilePath;

		StringBuffer shellScript = new StringBuffer(1000);
		shellScript.append(ShellScriptUtil.getChangeDirectoryCommand(MesquiteTrunk.isWindows(), rootDir));
		String blastArguments =  "  -query " + fileName;
		blastArguments+= " -db "+database;
		//blastArguments+= " -db "+NCBIUtil.getBLASTFileInputName(database);
		blastArguments+=" -task blastn";		// TODO:  does this need to change if the blastType differs?

		if (eValueCutoff>=0.0)
			blastArguments+= " -evalue "+eValueCutoff;

		if (wordSize>3)
			blastArguments+= " -word_size "+wordSize;
		if (numThreads>1)
			blastArguments+="  -num_threads " + numThreads;

		//	blastCommand+= " -gapopen 5 -gapextend 2 -reward 1 -penalty -3 ";



		blastArguments+=" -out " + outFileName + " -outfmt 5";		
		blastArguments+=" -max_target_seqs " + numHits; // + " -num_alignments " + numHits;// + " -num_descriptions " + numHits;		
		blastArguments+=" " + programOptions + StringUtil.lineEnding();
		String blastCommand = blastType + blastArguments;
		String programPath = blastType;
		programPath = getExecutablePath() + MesquiteFile.fileSeparator +blastType;
		shellScript.append(blastCommand);
		if (writeCommand)
			logln("\n...................\nBLAST command: \n" + blastCommand);

		String scriptPath = rootDir + "batchScript" + MesquiteFile.massageStringToFilePathSafe(unique) + ".bat";
		MesquiteFile.putFileContents(scriptPath, shellScript.toString(), true);

		timer.timeSinceLast();

		boolean success = false;
		if (scriptBased) 
			success = ShellScriptUtil.executeAndWaitForShell(scriptPath, runningFilePath, null, true, getName(),null,null, this, true);
		else {
			String arguments = blastArguments;
			arguments=StringUtil.stripBoundingWhitespace(arguments);
			externalProcessManager = new ExternalProcessManager(this, rootDir, programPath, arguments, getName(), outputFilePaths, this, this, true);
			if (useDefaultStdOutFileName())
				externalProcessManager.setStdOutFileName(ShellScriptRunner.stOutFileName);
			else
				externalProcessManager.setStdOutFileName(outFileName);
			success = externalProcessManager.executeInShell();
			if (success)
				success = externalProcessManager.monitorAndCleanUpShell(null);
			if (!success && pathWithSpaces) {
				setBlastErrorMessage("Path to BLAST databases contains at least one blank; BLAST functions likely failed for this reason. Change folder or database file names so that they have no spaces.");
			}
			if (!success)
				setWarnErrorMessage(false);
		}

		
		if (success){
			String results = MesquiteFile.getFileContentsAsString(outFilePath, -1, 1000, false);
			if (blastResponse!=null && StringUtil.notEmpty(results)){
				blastResponse.setLength(0);
				blastResponse.append(results);
			}
		}
		deleteSupportDirectory();
		if (getProject()!=null)
			getProject().decrementProjectWindowSuppression();
		logln("   BLAST completed in " +timer.timeSinceLastInSeconds()+" seconds");
	}	

	/*.................................................................................................................*/
	public String getFastaFromIDs(String queryTaxonName, String[] idList, boolean isNucleotides, StringBuffer blastResponse, int databaseNumber) {
		int count = 0;
		for (int i=0; i<idList.length; i++) 
			if (StringUtil.notEmpty(idList[i]))
				count++;
		if (count==0)
			return null;
		if (blastx)
			return NCBIUtil.fetchGenBankSequencesFromIDs(idList,  isNucleotides, null, false,  blastResponse,  null);

		count = 0;
		StringBuffer queryString = new StringBuffer();
		for (int i=0; i<idList.length; i++) 
			if (StringUtil.notEmpty(idList[i])){
				if (count>0)
					queryString.append(",");
				queryString.append("'"+idList[i]+"'");
				count++;
			}

		getProject().incrementProjectWindowSuppression();

		String unique = MesquiteTrunk.getUniqueIDBase();
		String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  

		String runningFilePath = rootDir + "running" + MesquiteFile.massageStringToFilePathSafe(unique);

		String outFileName = "blastResults" + MesquiteFile.massageStringToFilePathSafe(unique);
		String outFilePath = rootDir + outFileName;
		String[] outputFilePaths = new String[1];
		outputFilePaths[0] = outFilePath;

		StringBuffer shellScript = new StringBuffer(1000);
		shellScript.append(ShellScriptUtil.getChangeDirectoryCommand(MesquiteTrunk.isWindows(), rootDir));

		String programPath = "blastdbcmd";
		programPath = getExecutablePath() + MesquiteFile.fileSeparator +"blastdbcmd";

		String blastArguments = "  -entry "+queryString + " -outfmt %f";
		blastArguments+= " -db "+databaseArray[databaseNumber];
		//blastArguments+= " -db "+NCBIUtil.getBLASTFileInputName(databaseArray[databaseNumber]);
		blastArguments+=" -out " + outFileName;		

		String blastCommand = "blastdbcmd" + blastArguments;

		shellScript.append(blastCommand);

		String scriptPath = rootDir + "batchScript" + MesquiteFile.massageStringToFilePathSafe(unique) + ".bat";
		MesquiteFile.putFileContents(scriptPath, shellScript.toString(), true);

		

		boolean success = false;
		if (scriptBased) 
			success = ShellScriptUtil.executeAndWaitForShell(scriptPath, runningFilePath, null, true, getName(),null,null, this, true);
		else {
			String arguments = blastArguments;
			arguments=StringUtil.stripBoundingWhitespace(arguments);
			externalProcessManager = new ExternalProcessManager(this, rootDir, programPath, arguments, getName(), outputFilePaths, this, this, true, false, true, true);
			externalProcessManager.setBasicProcessInformation("\nQuery sequence: "+ queryTaxonName+"\nBLAST database "+databaseArray[databaseNumber]);
			if (useDefaultStdOutFileName())
				externalProcessManager.setStdOutFileName(ShellScriptRunner.stOutFileName);
			else
				externalProcessManager.setStdOutFileName(outFileName);
			externalProcessManager.setRemoveQuotes(true);
			success = externalProcessManager.executeInShell();
			if (success)
				success = externalProcessManager.monitorAndCleanUpShell(null);
		}

		
		
		
		if (success){
			String results = MesquiteFile.getFileContentsAsString(outFilePath, -1, 1000, false);
			if (blastResponse!=null && StringUtil.notEmpty(results)){
				blastResponse.setLength(0);
				blastResponse.append(results);
			}
			deleteSupportDirectory();
			getProject().decrementProjectWindowSuppression();
			return results;
		}
		deleteSupportDirectory();
		getProject().decrementProjectWindowSuppression();
		return null;
	}	

	/*.................................................................................................................*/
	public  String getTaxonomyFromID(String id, boolean isNucleotides, boolean writeLog, StringBuffer report){
		if (blastx)
			id = NCBIUtil.cleanUpID(id);
		if (localBlastDBHasTaxonomyIDs)
			return NCBIUtil.fetchTaxonomyFromSequenceID(id, isNucleotides, writeLog, report);
		return null;
	}



	/*.................................................................................................................*/
	public  void postProcessingCleanup(BLASTResults blastResult){
		if (useIDInDefinition){
			//			blastResult.setIDFromDefinition("|", 2);
			blastResult.setIDFromDefinition();
			blastResult.setAccessionFromDefinition("|", 4);
		}

	}

	public String[] modifyOutputPaths(String[] outputFilePaths) {
		// TODO Auto-generated method stub
		return outputFilePaths;
	}

	public void processOutputFile(String[] outputFilePaths, int fileNum) {
		// TODO Auto-generated method stub
		
	}

	public void processCompletedOutputFiles(String[] outputFilePaths) {
		// TODO Auto-generated method stub
		
	}


	public  String[] getNucleotideIDsfromProteinIDs(String[] ID){
		ID = NCBIUtil.cleanUpID(ID);
		return NCBIUtil.getNucIDsFromProtIDs(ID);
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

	public String getName() {
		return "BLAST Local Server";
	}

	public String getExplanation() {
		return "BLASTs a database on the same computer as Mesquite.";
	}

	public boolean continueProcess(Process proc) {
		return true;
	}
	public boolean fatalErrorDetected() {
		return false;
	}
	public boolean warnIfError() {
		return getWarnErrorMessage();
	}



}

package mesquite.align.lib;

import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
/*~~  */

import java.util.Random;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.ProteinData;
import mesquite.externalCommunication.AppHarvester.AppHarvester;
import mesquite.externalCommunication.lib.AppChooser;
import mesquite.externalCommunication.lib.AppInformationFile;
import mesquite.externalCommunication.lib.AppUser;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.Debugg;
import mesquite.lib.ExternalProcessManager;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTimer;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Notification;
import mesquite.lib.OutputFileProcessor;
import mesquite.lib.ProcessWatcher;
import mesquite.lib.ResultCodes;
import mesquite.lib.ShellScriptRunner;
import mesquite.lib.ShellScriptUtil;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharMatrixManager;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.misc.AlertWithLinkToDirectory;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public abstract class ExternalSequenceAligner extends MultipleSequenceAligner implements ActionListener, OutputFileProcessor, ProcessWatcher, AppUser{
	String programPath;
	SingleLineTextField programPathField =  null;
	boolean preferencesSet = false;
	boolean includeGaps = false;
	String programOptions = "" ;
	Random rng;
	protected boolean scriptBased = false;
	public static int runs = 0;
	ShellScriptRunner scriptRunner;
	ExternalProcessManager externalProcessManager;
	boolean useDefaultExecutablePath=true;
	protected AppInformationFile appInfoFile;
	boolean hasApp=false;
	

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new Random(System.currentTimeMillis());
		programOptions = getDefaultProgramOptions();
		loadPreferences();
		hasApp = AppHarvester.builtinAppExists(getAppOfficialName());
		
		//scriptBased = MesquiteTrunk.isJava VersionLessThan(1.7);
		return true;
	}
	public abstract String getProgramCommand();
	public abstract String getProgramName();
	public abstract String getDefaultProgramOptions();
	public abstract String getDNAExportInterpreter () ;
	public abstract String getProteinExportInterpreter () ;
	public abstract String getDNAImportInterpreter () ;
	public abstract String getExportExtension();
	public abstract String getImportExtension();
	public abstract String getProteinImportInterpreter () ;
	public abstract void appendDefaultOptions(StringBuffer shellScript, String inFilePath, String outFilePath, MolecularData data);

	/*.................................................................................................................*/
	public String getStdErr() {
		if (scriptBased){
			if (scriptRunner!=null)
				return scriptRunner.getStdErr();
		}
		else if (externalProcessManager!=null)
			return externalProcessManager.getStdErr();
		return "";
	}
	/*.................................................................................................................*/
	public boolean useDefaultStdOutFileName() {
		return false;
	}
	/*.................................................................................................................*/
	public String getCitation()  {
		String addendum = "";
		if (useDefaultExecutablePath && getDefaultExecutablePathAllowed()) 
			if (appInfoFile!=null) {
				if (StringUtil.notEmpty(appInfoFile.getCitation())) {
					addendum = " " + appInfoFile.getCitation();
				} else if (StringUtil.notEmpty(appInfoFile.getCitationURL())) {
					addendum = " For citation information about " + getProgramName() + " see: " + appInfoFile.getCitationURL();
				}
			}
		return super.getCitation() + addendum;
	}

	/*.................................................................................................................*
	public String getAppNameWithinAppsDirectory() {
		return null;
	}
	/*.................................................................................................................*
	public AppInformationFile getAppInfoFile() {
		return AppHarvester.getAppInfoFileForProgram(this);
	}

	/*.................................................................................................................*
	public String getDefaultExecutablePath(){
		if (appInfoFile==null) {
			appInfoFile = new AppInformationFile(getAppNameWithinAppsDirectory());
			boolean success = false;
			if (appInfoFile!=null)
				success = appInfoFile.processAppInfoFile();
			if (!success) appInfoFile=null;
		}
		if (appInfoFile!=null) {
			String fullPath = appInfoFile.getFullPath();
			return fullPath;
		}
		return null;
	}

	/*.................................................................................................................*/
	public String getAppVariant(){
		if (appInfoFile==null) {
			appInfoFile = getAppInfoFile();
		}
		if (appInfoFile!=null) {
			return appInfoFile.getAppVariant();
		}
		return null;
	}
	/*.................................................................................................................*/
	public String getDefaultExecutablePath(){
		if (appInfoFile==null) {
			appInfoFile = getAppInfoFile();
		}
		if (appInfoFile!=null) {
			return appInfoFile.getFullPath();
		}
		return null;
	}

	/*.................................................................................................................*/
	public String getExecutablePath(){
		if (useDefaultExecutablePath && getDefaultExecutablePathAllowed()) 
			return getDefaultExecutablePath();
		else
			return programPath;
	}

	public boolean getHasApp() {
		return hasApp;
	}
	public String getAppOfficialName() {
		return "";
	}

	public void appChooserDialogBoxEntryChanged() {}

	/*.................................................................................................................*/
	public AppInformationFile getAppInfoFile() {
		return AppHarvester.getAppInfoFileForProgram(this);
	}



	/*.................................................................................................................*/
	public String getStdOut() {
		if (scriptBased){
			if (scriptRunner!=null)
				return scriptRunner.getStdOut();
		}
		else if (externalProcessManager!=null)
			return externalProcessManager.getStdOut();
		return "";
	}

	public boolean userAborted(){
		return false;
	}

	public String checkStatus(){
		return null;
	}
	public boolean stopExecution(){
		if (scriptBased){
			if (scriptRunner!=null)
				scriptRunner.stopExecution();
		}
		else if (externalProcessManager!=null) {
			externalProcessManager.stopExecution();
		}
		return false;
	}



	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean programOptionsComeFirst(){
		return false;  
	}
	/*.................................................................................................................*/
	public String getProgramPath(){
		return getExecutablePath();  
	}


	public boolean getDefaultExecutablePathAllowed() {
		return getHasApp();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("defaultExecutablePath".equalsIgnoreCase(tag))
			useDefaultExecutablePath = MesquiteBoolean.fromTrueFalseString(content);
		if ("programPath".equalsIgnoreCase(tag)) {
			programPath = StringUtil.cleanXMLEscapeCharacters(content);
		}
		else if ("programOptions".equalsIgnoreCase(tag))
			programOptions = StringUtil.cleanXMLEscapeCharacters(content);

		preferencesSet = true;
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		//if (getDefaultExecutablePathAllowed())
		StringUtil.appendXMLTag(buffer, 2, "defaultExecutablePath", useDefaultExecutablePath);  
		StringUtil.appendXMLTag(buffer, 2, "programPath", programPath);  
		StringUtil.appendXMLTag(buffer, 2, "programOptions", programOptions);  

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setIncludeGaps " + includeGaps);
		temp.addLine("codonAlign " + codonAlign);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether to include gaps", "[true or false]", commandName, "setIncludeGaps")) {
			includeGaps = MesquiteBoolean.fromTrueFalseString(parser.getFirstToken(arguments));
		}
		else	if (checker.compare(this.getClass(), "Sets whether to do codon alignment", "[true or false]", commandName, "codonAlign")) {
			codonAlign = MesquiteBoolean.fromTrueFalseString(parser.getFirstToken(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}	

	/*.................................................................................................................*/
	public String getHelpString(){
		return "";
	}
	/*.................................................................................................................*/
	public String getHelpURL(){
		return "";
	}
	/*.................................................................................................................*/
	/** If a subclass wishes to add more options to the GUI dialog box, the GUI elements (CheckBoxes, etc.) should created in an override of this method. */
	public void queryProgramOptions(ExtensibleDialog dialog) {
	}
	/*.................................................................................................................*/
	/** Any GUI elements (CheckBoxes, etc.) that were created in queryProgramOptions should have their values harvested here. */
	public void processQueryProgramOptions(ExtensibleDialog dialog) {
	}
	/*.................................................................................................................*/
	/** The command-line flags for the queryProgramOptions should be entered here. */
	public String getQueryProgramOptions() {
		return "";
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
		if (queryOptions(codonAlign))   // DAVIDQUERY:  Is it ok to just pass codonAlign?
			storePreferences();
	}
	AppChooser appChooser;
	/*.................................................................................................................*/
	public boolean queryOptions(boolean codonAlignAvailable) {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options"))  
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), getProgramName() + " Locations & Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel(getProgramName() + " - File Locations & Options");
		dialog.appendToHelpString(getHelpString());
		dialog.setHelpURL(getHelpURL());

		
		appChooser = new AppChooser(this, this, useDefaultExecutablePath, programPath);
		appChooser.addToDialog(dialog);

/*		Checkbox defaultExecutablePathCheckBox =  null;
		if (getDefaultExecutablePathAllowed()) {
			defaultExecutablePathCheckBox = dialog.addCheckBox("Use built-in app path for "+ getProgramName(), useDefaultExecutablePath);
			programPathField = dialog.addTextField("Path to alternative version:", programPath, 40);
		} else
			programPathField = dialog.addTextField("Path to " + getProgramName() + ":", programPath, 40);
		Button programBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		programBrowseButton.setActionCommand("programBrowse");
	*/	
		

		Checkbox codonAlignCheckBox = null;
		if (!codonAlignAvailable)
			codonAlign=false;
		codonAlignCheckBox = dialog.addCheckBox("codon alignment", codonAlign);
		codonAlignCheckBox.setEnabled(codonAlignAvailable);

		Checkbox includeGapsCheckBox = dialog.addCheckBox("include gaps", includeGaps);

		queryProgramOptions(dialog);
		
		

		SingleLineTextField programOptionsField = dialog.addTextField("Additional " + getProgramName() + " options:", programOptions, 26, true);



		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			
			programPath = appChooser.getManualPath(); //for preference writing
			useDefaultExecutablePath = appChooser.useBuiltInExecutable(); //for preference writing

/*			programPath = programPathField.getText();
			if (defaultExecutablePathCheckBox!=null)
				useDefaultExecutablePath = defaultExecutablePathCheckBox.getState();
				
*/
			/*
			 * File pp = new File(programPath);
			if (!pp.canExecute()){
				alert( "Sorry, the file or location specified for " + getProgramName() + " appears to be incorrect, as it appears not to be an executable program.  The attempt to align may fail.  Please make sure that you have specified correctly the location of " + getProgramName());
			}
			 */
			programOptions = programOptionsField.getText();
			includeGaps = includeGapsCheckBox.getState();
			if (codonAlignCheckBox !=null)
				codonAlign = codonAlignCheckBox.getState();
			processQueryProgramOptions(dialog);
			storePreferences();
			//preferencesSet = true;
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	private boolean saveExportFile(MolecularData data, String directoryPath, String fileName, boolean[] taxaToAlign, int firstSite, int lastSite) {
		if (data==null)
			return false;

		runs++;
		String path = createSupportDirectory() + MesquiteFile.fileSeparator + fileName;  // place files in support directory for module
		incrementMenuResetSuppression();
		Taxa taxa = data.getTaxa();

		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(path), CommandChecker.defaultChecker); //TODO: never scripting???
		TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
		Taxa newTaxa =taxa.cloneTaxa(taxaToAlign);
		newTaxa.addToFile(tempDataFile, null, taxaManager);

		//rename taxa so program doesn't screw around with names
		for (int it=0; it<newTaxa.getNumTaxa(); it++)
			newTaxa.setTaxonName(it, "t" + it);
		logln("Number of taxa to be aligned: " + newTaxa.getNumTaxa());
		CharMatrixManager matrixManager = data.getMatrixManager();
		int numNewChars=0;
		int firstChar = -1;
		
		if (codonAlign) {
			firstSite= firstSite / 3;
			lastSite = lastSite / 3;
		}
		
		
		for (int ic=0; ic<data.getNumChars(); ic++){
			if (data.getSelected(ic) || (firstSite>=0 && MesquiteInteger.isCombinable(firstSite) && ic>= firstSite && lastSite<data.getNumChars() && MesquiteInteger.isCombinable(lastSite) && ic<= lastSite)){
				numNewChars++;
				if (firstChar<0) //first one found
					firstChar=ic;
			}
		}
		MolecularData newData = (MolecularData)matrixManager.getNewData(newTaxa, numNewChars);
		for (int ic=0; ic<newData.getNumChars(); ic++){
			if (taxaToAlign!=null) {
				int count=0;
				for (int i  =  0; i<taxaToAlign.length; i++) 
					if (taxaToAlign[i]) {
						newData.setState(ic, count, data.getState(ic+firstChar, i));
						count++;
					}
			}
			else for (int it=0; it<newTaxa.getNumTaxa(); it++)
				newData.setState(ic, it, data.getState(ic+firstChar, it));
		}
		//newData = data.cloneData(); ???		
		if (numNewChars > 0) {
			newData.setName(data.getName());
			newData.addToFile(tempDataFile, getProject(), null);
		} 

		boolean success = false;
		FileInterpreterI exporter=null;
		if (data instanceof DNAData)
			exporter = (FileInterpreterI)coord.findEmployeeWithName(getDNAExportInterpreter());
		else if (data instanceof ProteinData)
			exporter = (FileInterpreterI)coord.findEmployeeWithName(getProteinExportInterpreter());
		if (exporter!=null) {
			String ext = exporter.preferredDataFileExtension();
			if (StringUtil.blank(ext) || StringUtil.endsWithIgnoreCase(fileName, ext))
				ext = "";
			else
				ext = "." + ext;


			String s = "file = " + StringUtil.tokenize(fileName + ext) + " directory = " + StringUtil.tokenize(directoryPath) + " noTrees suppressAllGapTaxa";
			if (includeGaps)
				s+= " includeGaps";
			//if (data.anySelected()) 
			//	s += " writeOnlySelectedData";
			s+= " usePrevious";
			success = coord.export(exporter, tempDataFile, s);
		}

		tempDataFile.close();

		decrementMenuResetSuppression();
		return success;
	}
	protected boolean optionsAlreadySet = false;
	/*.................................................................................................................*/
	boolean codonAlignmentAvailable(MCategoricalDistribution matrix, boolean[] taxaToAlign, int firstSite, int lastSite, int firstTaxon, int lastTaxon) {
		CharacterData data = matrix.getParentData();
		if (!(data instanceof DNAData))
			return false;
		DNAData dData = (DNAData)data;
		if (!dData.allSequencesMultiplesOfThree())
			return false;
		return true;
	}
	/*.................................................................................................................*/
	public long[][] alignSequences(MCategoricalDistribution matrix, boolean[] taxaToAlign, int firstSite, int lastSite, int firstTaxon, int lastTaxon, MesquiteInteger resultCode) {

		boolean codonAlignAvailable = codonAlignmentAvailable( matrix, taxaToAlign, firstSite,  lastSite,  firstTaxon,  lastTaxon);

		if (!optionsAlreadySet && !queryOptions(codonAlignAvailable)){
			if (resultCode != null)
				resultCode.setValue(ResultCodes.USER_STOPPED);
			return null;
		}
		if (!(matrix.getParentData() != null && matrix.getParentData() instanceof MolecularData)){
			discreetAlert( "Sorry, " + getName() + " works only if given a full MolecularData object");
			if (resultCode != null)
				resultCode.setValue(ResultCodes.INCOMPATIBLE_DATA);
			return null;
		}
		if (StringUtil.blank(getProgramPath())) {
			discreetAlert( "No path to the program has been specified. " + getName() + " cannot function without a program indicated.");
			if (resultCode != null)
				resultCode.setValue(ResultCodes.SPECIFICATION_MISSING);
			return null;
			
		}	
		MolecularData data = (MolecularData)matrix.getParentData();
		boolean isProtein = data instanceof ProteinData;
		MolecularData dataToAlign = data;
		if (codonAlign && !isProtein) {
			DNAData dData = (DNAData)data;
			dData.collapseGapsInCellBlock(firstTaxon, lastTaxon, 0, data.getNumChars()-1, false);
			if (dData.someCoding())
				dataToAlign = dData.getProteinData(null, true);
			if (dataToAlign==null) {
				dataToAlign=data;
				codonAlign=false;
			} 

		}
		boolean pleaseStorePref = false;
		/*  commented out DRM 27 Dec 2023
		 * 		if (!preferencesSet) {
			programPath = MesquiteFile.openFileDialog("Choose " + getProgramName()+ ": ", null, null);
			if (StringUtil.blank(programPath)){
				return null;
			}
			if (!programPath.endsWith(MesquiteFile.fileSeparator))
				programPath+=MesquiteFile.fileSeparator;
			pleaseStorePref = true;
		}
		File pp = new File(programPath);
		if (!pp.canExecute()){
			discreetAlert( "Sorry, the file or location specified for " + getProgramName() + " appears to be incorrect, as it appears not to be an executable program.  The attempt to align may fail.  Please make sure that you have specified correctly the location of " + getProgramName());
		}
		 */
		getProject().incrementProjectWindowSuppression();
		if (pleaseStorePref)
			storePreferences();
		data.incrementEditInhibition();
		String unique = MesquiteTrunk.getUniqueIDBase() + Math.abs(rng.nextInt());

		String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  //replace this with current directory of file
		//rootDir = "/test/";

		//		StringBuffer fileBuffer = getFileInBuffer(data);
		String fileName = "tempAlign" + MesquiteFile.massageStringToFilePathSafe(unique) + getExportExtension();   //replace this with actual file name?
		String filePath = rootDir +  fileName;

		boolean success = false;
		


		if (codonAlign) {
			logln("Conducting codon alignment by translating to amino acids, aligning those, and then forcing nucleotides to match that alignment");
			logln("Exporting amino acid file for " + getProgramName());
		}
		else
			logln("Exporting file for " + getProgramName());
		int numTaxaToAlign=data.getNumTaxa();


		if (taxaToAlign!=null){
			success = saveExportFile(dataToAlign, rootDir, fileName, taxaToAlign, firstSite, lastSite);
			int count=0;
			for (int j=0; j<taxaToAlign.length; j++)
				if (taxaToAlign[j])
					count++;
			numTaxaToAlign=count;
		}
		else if (!(firstTaxon==0 && lastTaxon==matrix.getNumTaxa())) {  // we are doing something other than all taxa.    // this should be matrix.getNumTaxa()-1 to avoid all taxa, but if you do that, it skips to the next option, and it fails.  
			boolean[] taxaToAlignLocal = new boolean[matrix.getNumTaxa()];
			for (int it = 0; it<matrix.getNumTaxa(); it++)
				taxaToAlignLocal[it] =  (it>=firstTaxon && it<= lastTaxon);
			success = saveExportFile(dataToAlign, rootDir, fileName, taxaToAlignLocal, firstSite, lastSite);
			numTaxaToAlign=lastTaxon-firstTaxon+1;
		}
		else
			success = saveExportFile(dataToAlign, rootDir, fileName, null, -1, -1);

		if (!success) {
			logln("File export failed");
			if (resultCode != null)
				resultCode.setValue(ResultCodes.FILE_PROBLEM);
			data.decrementEditInhibition();
			return null;
		}
		if (resultCode != null)
			resultCode.setValue(ResultCodes.ERROR);  //As default; turned to success later with success
		String runningFilePath = rootDir + "running" + MesquiteFile.massageStringToFilePathSafe(unique);
		String outFileName = "alignedFile" + MesquiteFile.massageStringToFilePathSafe(unique) + getImportExtension();
		String outFilePath = rootDir + outFileName;
		String[] outputFilePaths = new String[1];
		outputFilePaths[0] = outFilePath;

		//		MesquiteFile.putFileContents(filePath, fileBuffer.toString(), true);

		StringBuffer shellScript = new StringBuffer(1000);
		shellScript.append(ShellScriptUtil.getChangeDirectoryCommand(MesquiteTrunk.isWindows(), rootDir));
		shellScript.append(getProgramCommand());
		StringBuffer argumentsForLogging = new StringBuffer();
		if (programOptions == null);
		programOptions = "";

		logln("Options: " + programOptions + " " + getQueryProgramOptions());
		if (programOptionsComeFirst()){
			shellScript.append(" " + programOptions + " " + getQueryProgramOptions() + " ");
			argumentsForLogging.append(" " + programOptions + " " + getQueryProgramOptions() + " ");
		}
		appendDefaultOptions(shellScript, fileName,  outFileName,  data);
		appendDefaultOptions(argumentsForLogging, fileName,  outFileName,  data);

		if (!programOptionsComeFirst()){
			shellScript.append(" " + programOptions + " "+ getQueryProgramOptions());
			argumentsForLogging.append(" " + programOptions + " "+ getQueryProgramOptions());
		}
		shellScript.append(StringUtil.lineEnding());
		shellScript.append(ShellScriptUtil.getRemoveCommand(MesquiteTrunk.isWindows(), runningFilePath));

		String scriptPath = rootDir + "alignerScript" + MesquiteFile.massageStringToFilePathSafe(unique) + ".bat";
		if (scriptBased)
			MesquiteFile.putFileContents(scriptPath, shellScript.toString(), false);

		logln("Requesting the operating system to run " + getProgramName());
		logln("Location of  " + getProgramName()+ ": " + getProgramPath());
		logln("Arguments given in running alignment program:\r" + argumentsForLogging.toString()); 
		MesquiteTimer timer = new MesquiteTimer();
		timer.start();
		ProgressIndicator progressIndicator = null;
		if (!MesquiteThread.getHintToSuppressProgressIndicatorsCurrentThread())
			progressIndicator = new ProgressIndicator(getProject(), getProgramName()+" alignment in progress");
		if (progressIndicator != null)
			progressIndicator.start();

		if (scriptBased) {
			scriptRunner = new ShellScriptRunner(scriptPath, runningFilePath, null, true, getName(), outputFilePaths, this, this, true);  //scriptPath, runningFilePath, null, true, name, outputFilePaths, outputFileProcessor, watcher, true
			success = scriptRunner.executeInShell();
			if (success)
				success = scriptRunner.monitorAndCleanUpShell(progressIndicator);
		} else {
			String arguments = argumentsForLogging.toString();

			arguments=StringUtil.stripBoundingWhitespace(arguments);
			externalProcessManager = new ExternalProcessManager(this, rootDir, getProgramPath(), arguments,getName(), outputFilePaths, this, this, true);
			//ShellScriptUtil.changeDirectory(rootDir, rootDir);
			if (useDefaultStdOutFileName())
				externalProcessManager.setStdOutFileName(ShellScriptRunner.stOutFileName);
			else
				externalProcessManager.setStdOutFileName(outFileName);
			success = externalProcessManager.executeInShell(); //This brings a contained window to the fore, unnecessarily, but if the contents of executeInShell are disabled, it still does.
			if (success) {
				success = externalProcessManager.monitorAndCleanUpShell(progressIndicator);
			}
		}

		if (progressIndicator != null) {
			if (progressIndicator.isAborted())
				logln("Alignment stopped by user\n");
			progressIndicator.goAway();
		}
		File outputFile = new File(outFilePath);
		if (!outputFile.exists() || outputFile.length() ==0){
			success = false;
			logln("Alignment file doesn't exist or is of zero length.");
			if (resultCode != null)
				resultCode.setValue(ResultCodes.FILE_PROBLEM);
			data.decrementEditInhibition();
			if (okToInteractWithUser(MesquiteModule.CAN_PROCEED_ANYWAY, "Alignment failed")){
				AlertWithLinkToDirectory alert = new AlertWithLinkToDirectory(containerOfModule(),"Alignment failed", "Alignment file doesn't exist or is of zero length. Please examine the analysis folder for information.", rootDir);
			}
			return null;
		}
		
		if (success){
			logln("Alignment completed by external program in " + timer.timeSinceLastInSeconds() + " seconds");
			logln("Processing results...");
			FileCoordinator coord = getFileCoordinator();
			MesquiteFile tempDataFile = null;
			CommandRecord oldCR = MesquiteThread.getCurrentCommandRecord();
			CommandRecord scr = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(scr);
			String failureText = StringUtil.tokenize("Output file containing aligned sequences ");
			if (dataToAlign instanceof DNAData)
				tempDataFile = (MesquiteFile)coord.doCommand("linkFileExp", failureText +" " + StringUtil.tokenize(outFilePath) + " " + StringUtil.tokenize(getDNAImportInterpreter()) + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
			else
				tempDataFile = (MesquiteFile)coord.doCommand("linkFileExp", failureText +" " + StringUtil.tokenize(outFilePath) + " " + StringUtil.tokenize(getProteinImportInterpreter()) + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
			MesquiteThread.setCurrentCommandRecord(oldCR);
			CharacterData alignedData = getProject().getCharacterMatrix(tempDataFile,  0);
			alignedData.setName("Temp matrix for alignment");
			alignedData.removeTaxaThatAreEntirelyGaps(); 
			//DAVIDCHECK: Debugg.println: the above will delete the taxa even if they have data in other matrices. Is this OK? 
			//Also, this is the only call to that method. Perhaps move the code here?

			long[][] aligned = null;
			Taxa alignedTaxa =  alignedData.getTaxa();
			Taxa originalTaxa =  data.getTaxa();

			if (alignedData!=null) {
				logln("Acquired aligned data; now processing alignment.");
				int numChars = alignedData.getNumChars();
				//sorting to get taxon names in correct order
				int[] keys = new int[alignedData.getNumTaxa()];
				for (int it = 0; it<alignedData.getNumTaxa(); it++){
					String name = alignedTaxa.getTaxonName(it);
					
					keys[it] = MesquiteInteger.fromString(name.substring(1, name.length()));  //this is original taxon number
					if (it<numTaxaToAlign && !MesquiteInteger.isCombinable(keys[it])) {   // unsuccessful
						MesquiteMessage.println("Processing unsuccessful: can't find incoming taxon \"" + name+"\"");
						MesquiteMessage.println("  Taxa in incoming: ");
						for (int i=0; i<alignedData.getNumTaxa(); i++) {
							MesquiteMessage.println("    "+ alignedTaxa.getTaxonName(i) + " (" + i + ")\tsome data: " + alignedData.hasDataForTaxon(i));
						}
						MesquiteMessage.println("  Number of taxa in incoming matrix: "+ alignedTaxa.getNumTaxa());
						if (taxaToAlign!=null) {
							MesquiteMessage.println("  Number of taxa set to true in taxaToAlign: "+ numTaxaToAlign);
						}
						MesquiteMessage.println("  Number of taxa in original: "+ data.getNumTaxa());

						success=false;
						break;
					}

				}
				if (success) {
					for (int i=1; i<alignedTaxa.getNumTaxa() ; i++) {
						for (int j= i-1; j>=0 && j+1<keys.length && keys[j]>keys[j+1]; j--) {
							alignedTaxa.swapParts(j, j+1, false);
							int kj = keys[j];
							keys[j] = keys[j+1];
							keys[j+1] = kj;
							//alignedData.swapTaxa(j, j+1);
						}
					}
					alignedTaxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
					alignedData.changed(this, alignedTaxa, new Notification(MesquiteListener.PARTS_MOVED));
					if (alignedData instanceof MolecularData){
						aligned = new long[alignedData.getNumChars()][originalTaxa.getNumTaxa()];
						for (int ic = 0; ic<alignedData.getNumChars(); ic++)
							for (int it = 0; it<alignedData.getNumTaxa(); it++){
								//	String name = alignedTaxa.getTaxonName(it);
								//	int iCurrent = MesquiteInteger.fromString(name.substring(1, name.length()));  //this is original taxon number
								//	int iTaxon = IntegerArray.indexOf(keys, iCurrent);
								//if (iTaxon>=0 && MesquiteInteger.isCombinable(iTaxon))
								aligned[ic][keys[it]] = ((MolecularData)alignedData).getState(ic, it);
							}
					}
					if (resultCode != null)
						resultCode.setValue(ResultCodes.SUCCEEDED);
				}
			} 
			else 
				MesquiteMessage.println("Processing unsuccessful: alignedData is null");

			if (tempDataFile!=null)
				tempDataFile.close();
			getProject().decrementProjectWindowSuppression();
			if (runs == 1)
				deleteSupportDirectory();
			runs--;
			data.decrementEditInhibition();
			if (success) 
				return aligned;
			return null;
		}
		if (runs == 1)
			deleteSupportDirectory();
		runs--;
		getProject().decrementProjectWindowSuppression();
		data.decrementEditInhibition();
		return null;
	}	

	/*.................................................................................................................*/
	public boolean recoverProgramResults(MolecularData data, String outFilePath){

		//reading aligned file
		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = null;
		String failureText = StringUtil.tokenize("Output file containing aligned sequences");
		if (data instanceof DNAData)
			tempDataFile = (MesquiteFile)coord.doCommand("linkFileExp", failureText +" " + StringUtil.tokenize(outFilePath) + " " + StringUtil.tokenize(getDNAImportInterpreter()) + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
		else
			tempDataFile = (MesquiteFile)coord.doCommand("linkFileExp", failureText +" " + StringUtil.tokenize(outFilePath) + " " + StringUtil.tokenize(getProteinImportInterpreter()) + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
		CharacterData alignedData = getProject().getCharacterMatrix(tempDataFile,  0);
		return true;
	}	

	public void processOutputFile(String[] outputFilePaths, int fileNum) {
		// TODO Auto-generated method stub

	}
	public void processCompletedOutputFiles(String[] outputFilePaths) {
		// TODO Auto-generated method stub

	}
	public String[] modifyOutputPaths(String[] outputFilePaths) {
		// TODO Auto-generated method stub
		return outputFilePaths;
	}
	public boolean continueProcess(Process proc) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean stdErrorsAreFatal(){
		return false;
	}

	public boolean fatalErrorDetected() {
		String stdErr = getStdErr();
		if (stdErrorsAreFatal() && StringUtil.notEmpty(stdErr))
			return false;
		return false;
	}
	public boolean warnIfError() {
		return true;
	}


	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}

	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("programBrowse")) {

			programPath = MesquiteFile.openFileDialog("Choose " + getProgramName()+ ": ", null, null);
			if (!StringUtil.blank(programPath)) {
				programPathField.setText(programPath);
			}
		}
	}

}

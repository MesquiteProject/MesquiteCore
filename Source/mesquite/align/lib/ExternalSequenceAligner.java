package mesquite.align.lib;

/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.
Version 2.01, December 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
/*~~  */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.align.lib.*;

/* ======================================================================== */
public abstract class ExternalSequenceAligner extends MultipleSequenceAligner implements ActionListener{
	String programPath;
	SingleLineTextField programPathField =  null;
	boolean preferencesSet = false;
	String programOptions = "" ;
	Random rng;
	public static int runs = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new Random(System.currentTimeMillis());
		programOptions = getDefaultProgramOptions();
		loadPreferences();
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
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getProgramPath(){
		return programPath;  
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
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
		StringUtil.appendXMLTag(buffer, 2, "programPath", programPath);  
		StringUtil.appendXMLTag(buffer, 2, "programOptions", programOptions);  

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public String getHelpString(){
		return "";
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryFilesDialog = new ExtensibleDialog(containerOfModule(), getProgramName() + " Locations & Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		queryFilesDialog.addLabel(getProgramName() + " - File Locations & Options");
		queryFilesDialog.appendToHelpString(getHelpString());

		programPathField = queryFilesDialog.addTextField("Path to " + getProgramName() + ":", programPath, 40);
		Button programBrowseButton = queryFilesDialog.addAListenedButton("Browse...",null, this);
		programBrowseButton.setActionCommand("programBrowse");

		SingleLineTextField programOptionsField = queryFilesDialog.addTextField(getProgramName() + " options:", programOptions, 26, true);

		queryFilesDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			programPath = programPathField.getText();
			programOptions = programOptionsField.getText();
			storePreferences();
		}
		queryFilesDialog.dispose();
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
		CharMatrixManager matrixManager = data.getMatrixManager();
		int numNewChars=0;
		int firstChar = -1;
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
		newData.setName(data.getName());
		newData.addToFile(tempDataFile, getProject(), null);

		boolean success = false;
		FileInterpreterI exporter=null;
		if (data instanceof DNAData)
			exporter = (FileInterpreterI)coord.findEmployeeWithName(getDNAExportInterpreter());
		else if (data instanceof ProteinData)
			exporter = (FileInterpreterI)coord.findEmployeeWithName(getProteinExportInterpreter());
		if (exporter!=null) {
			String ext = exporter.preferredDataFileExtension();
			if (StringUtil.blank(ext))
				ext = "";
			else
				ext = "." + ext;


			String s = "file = " + StringUtil.tokenize(fileName + ext) + " directory = " + StringUtil.tokenize(directoryPath) + " noTrees suppressAllGapTaxa";
			//if (data.anySelected()) 
			//	s += " writeOnlySelectedData";
			s+= " usePrevious";
			success = coord.export(exporter, tempDataFile, s);
		}

		tempDataFile.close();

		decrementMenuResetSuppression();
		return success;
	}
	/*.................................................................................................................*/
	public long[][] alignSequences(MCategoricalDistribution matrix, boolean[] taxaToAlign, int firstSite, int lastSite, int firstTaxon, int lastTaxon) {
		if (!queryOptions())
			return null;
		if (!(matrix.getParentData() != null && matrix.getParentData() instanceof MolecularData)){
			discreetAlert( "Sorry, " + getName() + " works only if given a full MolecularData object");
			return null;
		}
		MolecularData data = (MolecularData)matrix.getParentData();
		boolean isProtein = data instanceof ProteinData;
		boolean pleaseStorePref = false;
		if (!preferencesSet) {
			programPath = MesquiteFile.chooseDirectory("Choose directory containing" + getProgramName() + ": ");
			if (StringUtil.blank(programPath))
				return null;
			if (!programPath.endsWith(MesquiteFile.fileSeparator))
				programPath+=MesquiteFile.fileSeparator;
			pleaseStorePref = true;
		}
		getProject().incrementProjectWindowSuppression();
		if (pleaseStorePref)
			storePreferences();
		data.setEditorInhibition(true);
		String unique = MesquiteTrunk.getUniqueIDBase() + Math.abs(rng.nextInt());

		String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  //replace this with current directory of file

//		StringBuffer fileBuffer = getFileInBuffer(data);
		String fileName = "tempAlign" + MesquiteFile.massageStringToFilePathSafe(unique) + getExportExtension();   //replace this with actual file name?
		String filePath = rootDir +  fileName;

		boolean success = false;
		
		if (taxaToAlign!=null)
			success = saveExportFile(data, rootDir, fileName, taxaToAlign, firstSite, lastSite);
		else if (!(firstTaxon==0 && lastTaxon==matrix.getNumTaxa())) {  // we are doing something other than all taxa.
			boolean[] taxaToAlignLocal = new boolean[matrix.getNumTaxa()];
			for (int it = 0; it<matrix.getNumTaxa(); it++)
				taxaToAlignLocal[it] =  (it>=firstTaxon && it<= lastTaxon);
			success = saveExportFile(data, rootDir, fileName, taxaToAlignLocal, firstSite, lastSite);
		}
		else
			success = saveExportFile(data, rootDir, fileName, null, -1, -1);

		if (!success) {
			data.setEditorInhibition(false);
			return null;
		}
		String runningFilePath = rootDir + "running" + MesquiteFile.massageStringToFilePathSafe(unique);
		String outFileName = "alignedFile" + MesquiteFile.massageStringToFilePathSafe(unique) + getImportExtension();
		String outFilePath = rootDir + outFileName;

//		MesquiteFile.putFileContents(filePath, fileBuffer.toString(), true);

		StringBuffer shellScript = new StringBuffer(1000);
		shellScript.append(ShellScriptUtil.getChangeDirectoryCommand(rootDir));
		shellScript.append(getProgramCommand());
		appendDefaultOptions(shellScript, fileName,  outFileName,  data);
		
		shellScript.append(" " + programOptions + StringUtil.lineEnding());
//		shellScript.append(ShellScriptUtil.getRemoveCommand(runningFilePath));

		String scriptPath = rootDir + "alignerScript" + MesquiteFile.massageStringToFilePathSafe(unique) + ".bat";
		MesquiteFile.putFileContents(scriptPath, shellScript.toString(), true);

  
		 success = ShellScriptUtil.executeAndWaitForShell(scriptPath, runningFilePath, null, true, getName());

		if (success){
			FileCoordinator coord = getFileCoordinator();
			MesquiteFile tempDataFile = null;
			CommandRecord oldCR = MesquiteThread.getCurrentCommandRecord();
			CommandRecord scr = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(scr);
			if (data instanceof DNAData)
				tempDataFile = (MesquiteFile)coord.doCommand("linkFile", StringUtil.tokenize(outFilePath) + " " + StringUtil.tokenize(getDNAImportInterpreter()) + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
			else
				tempDataFile = (MesquiteFile)coord.doCommand("linkFile", StringUtil.tokenize(outFilePath) + " " + StringUtil.tokenize(getProteinImportInterpreter()) + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
			MesquiteThread.setCurrentCommandRecord(oldCR);
			CharacterData alignedData = getProject().getCharacterMatrix(tempDataFile,  0);
			long[][] aligned = null;
			Taxa alignedTaxa =  alignedData.getTaxa();
			Taxa originalTaxa =  data.getTaxa();

			if (alignedData!=null) {
				int numChars = alignedData.getNumChars();
				//sorting to get taxon names in correct order
				int[] keys = new int[alignedData.getNumTaxa()];
				for (int it = 0; it<alignedData.getNumTaxa(); it++){
					String name = alignedTaxa.getTaxonName(it);
					keys[it] = MesquiteInteger.fromString(name.substring(1, name.length()));  //this is original taxon number
					if (!MesquiteInteger.isCombinable(keys[it])) {
						success=false;
						break;
					}

				}
				if (success) {
					for (int i=1; i<alignedTaxa.getNumTaxa(); i++) {
						for (int j= i-1; j>=0 && keys[j]>keys[j+1]; j--) {
							alignedTaxa.swapParts(j, j+1);
							int kj = keys[j];
							keys[j] = keys[j+1];
							keys[j+1] = kj;
							//alignedData.swapTaxa(j, j+1);
						}
					}
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
				}
			}
			if (tempDataFile!=null)
				tempDataFile.close();
			getProject().decrementProjectWindowSuppression();
			if (runs == 1)
				deleteSupportDirectory();
			runs--;
			data.setEditorInhibition(false);
			if (success) 
				return aligned;
			return null;
		}
		if (runs == 1)
			deleteSupportDirectory();
		runs--;
		getProject().decrementProjectWindowSuppression();
		data.setEditorInhibition(false);
		return null;
	}	

	/*.................................................................................................................*/
	public boolean recoverProgramResults(MolecularData data, String outFilePath){

		//reading aligned file
		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = null;
		if (data instanceof DNAData)
			tempDataFile = (MesquiteFile)coord.doCommand("linkFile", StringUtil.tokenize(outFilePath) + " " + StringUtil.tokenize(getDNAImportInterpreter()) + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
		else
			tempDataFile = (MesquiteFile)coord.doCommand("linkFile", StringUtil.tokenize(outFilePath) + " " + StringUtil.tokenize(getProteinImportInterpreter()) + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
		CharacterData alignedData = getProject().getCharacterMatrix(tempDataFile,  0);
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

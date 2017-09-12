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

import java.awt.Checkbox;
import java.awt.TextArea;

import mesquite.lib.*;
import mesquite.molec.lib.*;

/*  Initiator: DRM
 * */

public class LocalBlaster extends Blaster implements ShellScriptWatcher {
	boolean preferencesSet = false;
	String programOptions = "" ;
	String databaseString = "nt" ;
	int numThreads = 1;
	MesquiteTimer timer = new MesquiteTimer();
	boolean localBlastDBHasTaxonomyIDs = true;
	boolean useIDInDefinition = true;
	String[] databaseArray = null;
	int numDatabases = 0;


	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		programOptions = "";
		timer.start();
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

		preferencesSet = true;
		return buffer.toString();
	}

	public boolean initialize() {
		if (!MesquiteThread.isScripting())
			return queryOptions();
		return true;
	}
	/*.................................................................................................................*/
	public String getDatabaseName () {
		if (databaseString==null)
			return "";
		return databaseString;
	}

	/*.................................................................................................................*/
	public void processDatabases (String s) {
		if (s==null)
			databaseArray=null;
		else if (s.indexOf(",")<0){
			databaseArray= new String[1];
			databaseArray[0] = s;
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
					databaseArray[count]=token;
					count++;
				}
			}
		}
	}


	/*.................................................................................................................*/
	public boolean queryOptions() {
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


		//public TextArea addTextAreaSmallFont (String initialString, int numRows, int numColumns) {


		dialog.addLabel("Databases to search (separate by commas):");
		TextArea databasesField = dialog.addTextAreaSmallFont(databaseString, 5, 50);
		SingleLineTextField programOptionsField = dialog.addTextField("Additional BLAST options:", programOptions, 26, true);
		Checkbox useIDInDefinitionBox = dialog.addCheckBox("Use ID in Definition (NCBI-provided databases)", useIDInDefinition);
		Checkbox localBlastDBHasTaxonomyIDsBox = dialog.addCheckBox("Local BLAST database has NCBI taxonomy IDs", localBlastDBHasTaxonomyIDs);
		IntegerField numThreadsField = dialog.addIntegerField("Number of processor threads to use:", numThreads,20, 1, Integer.MAX_VALUE);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			databaseString = databasesField.getText();
			databaseString = StringUtil.removeNewLines(databaseString);
			processDatabases(databaseString);
			programOptions = programOptionsField.getText();
			numThreads = numThreadsField.getValue();
			localBlastDBHasTaxonomyIDs = localBlastDBHasTaxonomyIDsBox.getState();
			useIDInDefinition = useIDInDefinitionBox.getState();

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

		StringBuffer shellScript = new StringBuffer(1000);
		shellScript.append(ShellScriptUtil.getChangeDirectoryCommand(rootDir));
		String blastCommand = blastType + "  -query " + fileName;
		blastCommand+= " -db "+database;
		blastCommand+=" -task blastn";		// TODO:  does this need to change if the blastType differs?

		if (eValueCutoff>=0.0)
			blastCommand+= " -evalue "+eValueCutoff;

		if (wordSize>3)
			blastCommand+= " -word_size "+wordSize;
		if (numThreads>1)
			blastCommand+="  -num_threads " + numThreads;

		//	blastCommand+= " -gapopen 5 -gapextend 2 -reward 1 -penalty -3 ";



		blastCommand+=" -out " + outFileName + " -outfmt 5";		
		blastCommand+=" -max_target_seqs " + numHits; // + " -num_alignments " + numHits;// + " -num_descriptions " + numHits;		
		blastCommand+=" " + programOptions + StringUtil.lineEnding();
		shellScript.append(blastCommand);
		if (writeCommand)
			logln("\n...................\nBLAST command: \n" + blastCommand);

		String scriptPath = rootDir + "batchScript" + MesquiteFile.massageStringToFilePathSafe(unique) + ".bat";
		MesquiteFile.putFileContents(scriptPath, shellScript.toString(), true);

		timer.timeSinceLast();

		boolean success = ShellScriptUtil.executeAndWaitForShell(scriptPath, runningFilePath, null, true, getName(),null,null, this, true);

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
	public String getFastaFromIDs(String[] idList, boolean isNucleotides, StringBuffer blastResponse, int databaseNumber) {
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

		StringBuffer shellScript = new StringBuffer(1000);
		shellScript.append(ShellScriptUtil.getChangeDirectoryCommand(rootDir));

		String blastCommand = "blastdbcmd  -entry "+queryString + " -outfmt %f";
		blastCommand+= " -db "+databaseArray[databaseNumber];
		blastCommand+=" -out " + outFileName;		

		shellScript.append(blastCommand);

		String scriptPath = rootDir + "batchScript" + MesquiteFile.massageStringToFilePathSafe(unique) + ".bat";
		MesquiteFile.putFileContents(scriptPath, shellScript.toString(), true);

		boolean success = ShellScriptUtil.executeAndWaitForShell(scriptPath, runningFilePath, null, true, getName(),null,null, this, true);

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

	public boolean continueShellProcess(Process proc) {
		return true;
	}
	public boolean fatalErrorDetected() {
		return false;
	}



}

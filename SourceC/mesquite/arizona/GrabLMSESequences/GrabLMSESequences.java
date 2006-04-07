package mesquite.arizona.GrabLMSESequences;

import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class GrabLMSESequences extends UtilitiesAssistant { 
	final String folder = "fasta";
	final int qualThreshold = 20;
	final boolean truncateMixedEnds = true;
	final int mixedEndWindow = 10;
	final int mixedEndThreshold = 5;
	final static String sequenceDir = "SequenceFiles";
	final static String chromatDir = "Chromatograms";
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){
		addMenuItem(null, "Grab LMSE Sequences...", makeCommand("grabSequences", this));
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
   	 
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Grabs sequences from LMSE", null, commandName, "grabSequences")) {
    	 		processFileWithFTPs(commandRec);
    	 	}
    	 	else
    	 		return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
   	 }

	/*.................................................................................................................*/
   	public boolean processFileWithFTPs(CommandRecord commandRec){
			MesquiteString LMSEDirPath = new MesquiteString();
			MesquiteString LMSEFileName = new MesquiteString();
			String LMSEFilePath = MesquiteFile.openFileDialog("Choose HTML file from LMSE listing sequences to download:", LMSEDirPath, LMSEFileName);
			String LMSEFileContents = "";
			Parser parser = null;
			boolean haveLMSEFile = false;
			if (!StringUtil.blank(LMSEFilePath)) {
					LMSEFileContents = MesquiteFile.getFileContentsAsString(LMSEFilePath);
					if (!StringUtil.blank(LMSEFileContents)) {
						parser = new Parser(LMSEFileContents);
						haveLMSEFile = true;
					}
			}
			else {
				return false;
			}
	 		String directoryPath = MesquiteFile.chooseDirectory("Choose directory into which files should be saved:"); 
			if (StringUtil.blank(directoryPath))
				return false;
			if (haveLMSEFile) {
				String token = parser.getNextToken();
				String appendDir = "";
				File newDir = new File(directoryPath + MesquiteFile.fileSeparator + chromatDir );
				try { newDir.mkdir(); }
				catch (SecurityException e) { }
				newDir = new File(directoryPath + MesquiteFile.fileSeparator + sequenceDir);
				try { newDir.mkdir(); }
				catch (SecurityException e) { }
				int count = 0;
				while (!StringUtil.blank(token)) {
					token = parser.getNextToken();
					if (token.toLowerCase().startsWith("ftp://public.arl.arizona.edu/pub/seq")) {
//Debugg.println("token: " );
						if (token.toLowerCase().endsWith("ab1") || token.toLowerCase().endsWith("CRO"))
							appendDir = MesquiteFile.fileSeparator + chromatDir;
						else
							appendDir = MesquiteFile.fileSeparator + sequenceDir;
						if (MesquiteFile.downloadFTPFile(token, directoryPath+appendDir)) {
							count++;
							logln("  (" + count + ")");
						}
						else {
							logln("File could not be downloaded");
							break;
						}
					}
				}
				logln("" + count + " files downloaded");
			}
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Grab LMSE Sequences";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return false;
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Grab sequences from LMSE (GATC) at the University of Arizona";
   	 }
}

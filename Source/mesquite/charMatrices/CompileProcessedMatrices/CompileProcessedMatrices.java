/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.CompileProcessedMatrices; 

import java.awt.FileDialog;
import java.util.Vector;

import mesquite.categ.lib.ProteinData;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ColorTheme;

/* ======================================================================== */
public class CompileProcessedMatrices extends FileProcessor {
	String saveFile = null;
	String tempFile = null;
	boolean openAfterCompiled = true;
	boolean OACOptionAlreadySet = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false; 
	}

	/** if returns true, then requests to remain on even after alterFile is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setOpenAfterward " + openAfterCompiled);  
		return temp;
	}
	
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether to open after compilation", "[path]", commandName, "setOpenAfterward")) {
			openAfterCompiled = MesquiteBoolean.fromTrueFalseString(parser.getFirstToken(arguments));
			OACOptionAlreadySet = true;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}	
	
	/*.................................................................................................................*/
	void queryOpenAfter() {
		int def = 2;
		if (openAfterCompiled)
			def = 1;
		String ast = "";
		String when = "";
		if (!StringUtil.blank(previousProcessorLabel)) {
			ast = "*";
			when = "\n\n* after " + previousProcessorLabel;
		}

		openAfterCompiled = AlertDialog.query(containerOfModule(), "Open when done?", "Open file of compiled matrices" + ast + " when finished?" + when, "Open", "Don't", def);
		OACOptionAlreadySet = true;
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
	 	queryOpenAfter();
	}
	/*.................................................................................................................*
	public void processSingleXMLPreference (String tag, String content) {
		if ("openAfterCompiled".equalsIgnoreCase(tag)) {
			openAfterCompiled = MesquiteBoolean.fromOffOnString(content);
		}

	
}
/*.................................................................................................................*
public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "openAfterCompiled", MesquiteBoolean.toOffOnString(openAfterCompiled));  
		return buffer.toString();
	}	
*/
ListableVector taxonNames = new ListableVector();
	/*.................................................................................................................*/
	void checkTaxonList(CharacterData data, String saveFile){
		Taxa taxa = data.getTaxa();
		boolean added = false;
		for (int it = 0; it<taxa.getNumTaxa(); it++){

			if (taxonNames.indexOfByName(taxa.getTaxonName(it))<0){
				MesquiteString n = new MesquiteString(taxa.getTaxonName(it));
				n.setName(taxa.getTaxonName(it));
				taxonNames.addElement(n, false);
				added = true;
			}
		}
		if (added) {
			String matrices = 	 MesquiteFile.getFileContentsAsString(tempFile, -1,100, false);
			if (matrices == null)
				matrices = "";
			String block = "#NEXUS" + StringUtil.lineEnding() + "BEGIN TAXA;" + StringUtil.lineEnding() + " DIMENSIONS NTAX=" + taxonNames.size() + ";" + StringUtil.lineEnding() + " TAXLABELS" + StringUtil.lineEnding() + "   ";
			for (int it = 0; it<taxonNames.size(); it++){
				MesquiteString n = (MesquiteString)taxonNames.elementAt(it);
				block += ParseUtil.tokenize(n.getName()) + "  ";
			}
			block += ";" + StringUtil.lineEnding() + "END;" + StringUtil.lineEnding() + StringUtil.lineEnding();
			MesquiteFile.putFileContents(saveFile, block + matrices, true);
		}
	}
	void writeMatrixToFile(String file, CharacterData data, String matrixName){
		MesquiteFile.appendFileContents(file, "BEGIN CHARACTERS;" + StringUtil.lineEnding() + "\tTITLE " + ParseUtil.tokenize(matrixName) + ";" + StringUtil.lineEnding() , true);
		MesquiteFile.appendFileContents(file, "\tDIMENSIONS NCHAR= " + data.getNumChars() + " ;" + StringUtil.lineEnding() , true);
		if (data instanceof ProteinData)
			MesquiteFile.appendFileContents(file, "\tFORMAT DATATYPE = PROTEIN GAP = - MISSING = ?;" + StringUtil.lineEnding() + "\tMATRIX" + StringUtil.lineEnding() , true);
		else
			MesquiteFile.appendFileContents(file, "\tFORMAT DATATYPE = DNA GAP = - MISSING = ?;" + StringUtil.lineEnding() + "\tMATRIX" + StringUtil.lineEnding() , true);
		for (int it = 0; it < data.getNumTaxa(); it++){
			MesquiteFile.appendFileContents(file, "\t" + ParseUtil.tokenize(data.getTaxa().getTaxonName(it)) + "\t" , true);
			MesquiteStringBuffer description = new MesquiteStringBuffer();
			for (int ic =0; ic<data.getNumChars(); ic++){
				data.statesIntoNEXUSStringBuffer(ic, it, description);
			}
			MesquiteFile.appendFileContents(file, description.toString() + StringUtil.lineEnding(), true);
		}
		MesquiteFile.appendFileContents(file, "\t;" + StringUtil.lineEnding() + "END;" + StringUtil.lineEnding() + StringUtil.lineEnding() , true);
	}
	/*.................................................................................................................*/

	/** Called to alter file. */
	public int processFile(MesquiteFile file){

		if (saveFile == null || okToInteractWithUser(CAN_PROCEED_ANYWAY, "Asking for file to save")){ //need to check if can proceed
			loadPreferences();
			
			String message = "Output File for Compiled Matrices(s)";
			if (!StringUtil.blank(previousProcessorLabel))
				message = message + " [after " + previousProcessorLabel + "] ";
			MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), message, FileDialog.SAVE);
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setVisible(true);
			String fileName=fdlg.getFile();
			String directory=fdlg.getDirectory();
			// fdlg.dispose();
			if (StringUtil.blank(fileName) || StringUtil.blank(directory)) {
				return -1;
			}
			saveFile = MesquiteFile.composePath(directory, fileName);
			tempFile = MesquiteFile.composePath(directory, MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase() + fileName)) ;
		 	if (!OACOptionAlreadySet)
		 		queryOpenAfter();
			storePreferences();
		}
		if (saveFile == null)
			return -1;
		Listable[] matrices = proj.getFileElements(CharacterData.class);	
		if (matrices == null)
			return -1;
		for (int im = 0; im < matrices.length; im++){
			CharacterData data = (CharacterData)matrices[im];
			if (data.getFile() == file && data.getNumChars()>0){
				checkTaxonList(data, saveFile);
				String name = cleanFileName(file.getFileName());
				
				writeMatrixToFile(saveFile, data, name);
				writeMatrixToFile(tempFile, data, name);
			}
		}
		return 0;

	}
	String cleanFileName(String fName) {
		if (fName.endsWith(".fas") || fName.endsWith(".nexus") || fName.endsWith(".fasta") || fName.endsWith(".nex") || fName.endsWith(".phy") ||fName.endsWith(".FAS") || fName.endsWith(".NEXUS") || fName.endsWith(".FASTA") || fName.endsWith(".NEX"))
		 return StringUtil.getAllButLastItem(fName, ".");
		return fName;
	}
	/*.................................................................................................................*/
	/** Called after processing a series of files.*/
	public  boolean afterProcessingSeriesOfFiles(){
		MesquiteFile.deleteFile(tempFile);

		if (openAfterCompiled) {
			String commands = "newThread; tell Mesquite; openFile" + StringUtil.tokenize(saveFile) + "; endTell;";
			Puppeteer p = new Puppeteer(this);
			MesquiteInteger pos = new MesquiteInteger(0);
			p.execute(getFileCoordinator(), commands, pos, "", false);
		}
		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 330;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Compile Processed Matrices into One File";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Compiles matrices from each file processed into a combined NEXUS file." ;
	}

}



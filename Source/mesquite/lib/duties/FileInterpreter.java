/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.duties;

import java.awt.FileDialog;

import mesquite.lib.Bits;
import mesquite.lib.MainThread;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteFileDialog;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteStringBuffer;
import mesquite.lib.NameReference;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.ProgressIndicator;


/* ======================================================================== */
/**Superclass of file interpreting modules (e.g., NEXUS file reader/writer).  Different subclasses are expected
to read different data file formats.  Example module: "Interpret NEXUS files" (class InterpretNexus).  Example of use:
see BasicFileCoordinator.*/

public abstract class FileInterpreter extends MesquiteModule  {
	public static final int CURRENTDELIMITER=0;
	public static final int MACOSDELIMITER=1;
	public static final int WINDOWSDELIMITER=2;
	public static final int UNIXDELIMITER=3;
	public int lineDelimiter = CURRENTDELIMITER;
	
	//For them moment we'll leave these, but why should these be both here and in MesquiteFile?
	public boolean writeOnlySelectedData = false;
	public boolean writeOnlySelectedTaxa = false;
	public boolean writeTaxaWithAllMissing = true;  //default changed to true as true  after 2. 75
	public boolean writeExcludedCharacters = true;
	public boolean writeCharactersWithNoData = true;
	public boolean writeCharLabels = true;

	//protected String filePath2=null;

	
	protected static int STOPIMPORT = -1;
	protected static int DONTADD = 0;
	protected static int REPLACEDATA = 1;
	protected static int REPLACEIFEMPTYOTHERWISEADD = 2;
	protected static int REPLACEIFEMPTYOTHERWISEIGNORE = 3;
	protected static int ADDASNEW = 4;
	
	protected static int treatmentOfIncomingDuplicates = REPLACEIFEMPTYOTHERWISEADD;
	protected  boolean selectIncoming = false;

//	protected static boolean defaultReplaceDataOfTaxonWithSameName = false;
	protected static int defaultReplaceDataOfTaxonWithSameNameInt = ADDASNEW;
	protected static boolean defaultHasQueriedAboutSameNameTaxa = false;
//	protected boolean replaceDataOfTaxonWithSameName = defaultReplaceDataOfTaxonWithSameName;
	protected int replaceDataOfTaxonWithSameNameInt = defaultReplaceDataOfTaxonWithSameNameInt;
	protected boolean hasQueriedAboutSameNameTaxa = defaultHasQueriedAboutSameNameTaxa;
	protected int totalFilesToImport = 1;
	protected int importFileNumber = 0;
	protected boolean multiFileImport = false;
	protected int lastNewTaxonFilled = -1;
	protected int maximumTaxonFilled =-1;
	protected int originalNumTaxa =-1;

	public Class getDutyClass() {
		return FileInterpreter.class;
	}
 	 public String[] getDefaultModule() {
 	 	return new String[] {"#InterpretNEXUS", "#InterpretFastaDNA", "#InterpretFastaProtein"};
 	 }
	public String getFunctionIconPath(){
		return getRootImageDirectoryPath() + "functionIcons/fileInterpret.gif";
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public void reset(){
//		replaceDataOfTaxonWithSameName = defaultReplaceDataOfTaxonWithSameName;
		replaceDataOfTaxonWithSameNameInt = defaultReplaceDataOfTaxonWithSameNameInt;
		hasQueriedAboutSameNameTaxa = defaultHasQueriedAboutSameNameTaxa;
		
	}

	/** This is deprecated and should not be overridden.  The subsequent methods should be used instead.*/
	public boolean canExport(){
		return false;
	}
	/** returns whether module is able ever to export.*/
	public boolean canExportEver(){
		return canExport();
	}
	/** returns whether module has something it can export in the project.  Should be overridden*/
	public boolean canExportProject(MesquiteProject project){
		return canExport();
	}

	/** returns whether module can export a character data matrix of the given type.  Should be overridden*/
	public boolean canExportData(Class dataClass){
		return canExport();
	}
	
	public int[] getNewTaxaAdded(){
		return null;
	}
	
	/** Returns whether the module can read (import) files with data of class dataClass*/
	public boolean canImport(Class dataClass){
		return canImport();
	}
	/** Returns whether the module can read (import) files */
	public boolean canImport(){
		return false;
	}
	/** Returns whether the module can read (import) files considering the passed argument string (e.g., fuse) */
	public boolean canImport(String arguments, Class dataClass){
		boolean fuse = parser.hasFileReadingArgument(arguments, "fuseTaxaCharBlocks");
		if (fuse)
			return false;
		return canImport(dataClass);
	}
	/** Returns whether the module can read (import) files considering the passed argument string (e.g., fuse) */
	public boolean canImport(String arguments){
		return canImport(arguments,null);
	}

	/** reads a file using the methods of MesquiteFile and places its data into the given MesquiteProject
	which will already have been instantiated.  Recall that a MesquiteProject is not the external file on disk or server,
	but is rather the collection of taxa, data, trees, etc. that together typically make up the information in
	a NEXUS file.  The external file is referred to by the MesquiteFile.*/
	public abstract void readFile(MesquiteProject mf, MesquiteFile mNF, String arguments);

	//	public abstract void readFile();

	public String getDutyName() {
		return "File Interpreter";
	}

	/*.................................................................................................................*/
	public String getLineEnding() {
		if (lineDelimiter == CURRENTDELIMITER) 
			return StringUtil.lineEnding();
		else if (lineDelimiter == MACOSDELIMITER) 
			return "\r";
		else if (lineDelimiter == WINDOWSDELIMITER) 
			return "\r\n";
		else if (lineDelimiter == UNIXDELIMITER) 
			return "\n";
		return StringUtil.lineEnding();
	}

	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "";
	}
	/*.................................................................................................................*/
	/** Returns wether this interpreter uses a flavour of NEXUS.  Used only to determine whether or not to add "nex" as a file extension to imported files (if already NEXUS, doesn't).**/
	/*public boolean usesNEXUSflavor(){
		return false;
	}
*/
	NameReference previousTaxaNameRef = NameReference.getNameReference("previousTaxon");
	NameReference newlyAddedTaxaNameRef = NameReference.getNameReference("newlyAddedTaxon");
	
	/*.................................................................................................................*/
	public void startRecordingTaxa(Taxa taxa){
		taxa.clearAllAssociatedBits(newlyAddedTaxaNameRef);
		taxa.clearAllAssociatedBits(previousTaxaNameRef);
		for (int it=0; it<taxa.getNumTaxa(); it++)
			recordAsPreviousTaxon(taxa,it);
	}
	/*.................................................................................................................*/
	public void recordAsPreviousTaxon(Taxa taxa, int it){
		taxa.setAssociatedBit(previousTaxaNameRef, it, true);
	}
	/*.................................................................................................................*/
	public void recordAsNewlyAddedTaxon(Taxa taxa, int it){
		taxa.setAssociatedBit(newlyAddedTaxaNameRef, it, true);
	}
	/*.................................................................................................................*/
	public Bits getNewlyAddedTaxa(Taxa taxa){
		Bits newTaxa = new Bits(taxa.getNumTaxa());
		newTaxa.clearAllBits();
		for (int it=0; it<taxa.getNumTaxa(); it++)
			if (taxa.getAssociatedBit(newlyAddedTaxaNameRef, it))
				newTaxa.setBit(it);
		return newTaxa;
	}
	/*.................................................................................................................*/
	public void endRecordingTaxa(Taxa taxa){
		taxa.clearAllAssociatedBits(newlyAddedTaxaNameRef);
		taxa.clearAllAssociatedBits(previousTaxaNameRef);
	}


	/*.................................................................................................................*/
	protected String stripNex(String name) {
		if (name == null)
			return null;
		int length = name.length();
		if (length<4)
			return name;
		String last4 = name.substring(length-4, length);
		if (last4.equalsIgnoreCase(".nex")) 
			return name.substring(0, length-4);
		else
			return name;
	}

	/*.................................................................................................................*/
	public void setLineDelimiter(int newDelimiter) {
		lineDelimiter = newDelimiter;
	}

	public boolean isSubstantive(){
		return false;  
	}
	/*.................................................................................................................*/
	public void finishImport(ProgressIndicator progIndicator, MesquiteFile file, boolean abort){
		if (progIndicator!=null)
			progIndicator.goAway();
		if (file!=null) 
			file.closeReading();
		if (abort){ //���		
			if (file!=null)
				file.close();
			resetAllMenuBars();
			//decrementMenuResetSuppression();
			return;
		}
		/*
		 * 	FileCoordinator fc = getFileCoordinator();
		if (fc!=null && getProject().getNumberCharMatrices()>0) {
			MesquiteModule dwc = (MesquiteModule)fc.doCommand("getEmployee", "'Data Window Coordinator'",CommandChecker.defaultChecker);
			if (dwc!=null) {
				for (int i=0; i< getProject().getNumberCharMatrices(); i++)
					dwc.doCommand("showDataWindow", ""+i,CommandChecker.defaultChecker);
			}
		}
		 */
	}
	/*.................................................................................................................*/
	public String suggestedFileName( String suffix, String suggestedFileEnding) {
			String name = getProject().getHomeFileName();
			if (suffix==null)
				suffix="";
			if (name==null)
				name = "untitled.";
			else if (suggestedFileEnding.equalsIgnoreCase("NEX"))
				name = stripNex(name)+"2.";
			else 
				name = stripNex(name) + ".";
			if (StringUtil.notEmpty(suffix))
				name+=suffix+".";
			name += suggestedFileEnding;
			return name;

	}
	/*.................................................................................................................*/
	public String getPathForExport(String arguments, String suggestedFileName, MesquiteString dir, MesquiteString fn) {
		//check arguments for filename and directory
		String tempFileName=null;
		String tempDirectoryName=null;
		String token = parser.getFirstToken(arguments);
		while (token !=null){
			if (token.equalsIgnoreCase("file")){
				parser.getNextToken(); // =
				tempFileName = parser.getNextToken();
			}
			else if (token.equalsIgnoreCase("directory")){
				parser.getNextToken(); // =
				tempDirectoryName = parser.getNextToken();
				if (tempDirectoryName != null && !tempDirectoryName.endsWith(MesquiteFile.fileSeparator)) //(tempDirectoryName.length()-1) != MesquiteFile.fileSeparator.charAt(0))
					tempDirectoryName += MesquiteFile.fileSeparator;
			}
			token = parser.getNextToken();
		}
		if (StringUtil.blank(tempFileName)){
			String queryMessage = "Export to file";
			MainThread.incrementSuppressWaitWindow();
			MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), queryMessage, FileDialog.SAVE);   // Save File dialog box
			if (suggestedFileName == null)
				suggestedFileName = "untitled";
			fdlg.setFile(suggestedFileName);

			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setVisible(true);
			tempFileName=fdlg.getFile();
			tempDirectoryName=fdlg.getDirectory();
			//	fdlg.dispose();
			MainThread.decrementSuppressWaitWindow();
		}
		if (!StringUtil.blank(tempFileName)) {
			if (StringUtil.blank(tempDirectoryName))
				tempDirectoryName = "";
			if (dir!=null)
				dir.setValue(tempDirectoryName);
			if (fn != null){
				fn.setValue(tempFileName);
			}
			MesquiteFile mf = new MesquiteFile();
			mf.setLocation(tempFileName, tempDirectoryName, true);
			if (mf != null){
				if (!mf.canCreateOrRewrite()){
					discreetAlert("Sorry, the file \"" + mf.getFileName() + "\"could not be written because of problems concerning the file system.  See diagnosis in the Mesquite Log");
					String report = mf.diagnosePathIssues();
					logln("DIAGNOSIS of folder and file status:\n" + report);
					return null;
				}
			}
			return tempDirectoryName+tempFileName;
		}
		return null;

	}
	/*.................................................................................................................*/
	public String getExportedFileDirectory(String filePath){
		return MesquiteFile.getDirectoryPathFromFilePath(filePath);
	}
	/*.................................................................................................................*/
	public String getExportedFileName(String filePath){
		if (filePath!=null) {
			return MesquiteFile.getFileNameFromFilePath(filePath);
		}
		return null;
	}
	
	/*.................................................................................................................*/

	public void saveExportedFile(MesquiteStringBuffer output, String arguments, String suggestedFileName) {

		String filePath = getPathForExport(arguments, suggestedFileName, null, null);
		if (filePath!=null) {
			MesquiteFile.putFileContents(filePath, output, true);
			//logln("File exported to " + filePath);
		}
	}
	/*.................................................................................................................*/
	public void saveExportedFileWithExtension(MesquiteStringBuffer outputBuffer, String arguments, String suffix, String extension, String filePath) {
		if (outputBuffer == null)
			return;

		if (StringUtil.blank(filePath)) {
			String name = suggestedFileName(suffix, extension);
			saveExportedFile(outputBuffer, arguments, name);
		}
		else {
				MesquiteFile.putFileContents(filePath, outputBuffer, true);
		}
	}
	/*.................................................................................................................*/
	public void saveExportedFileWithExtension(MesquiteStringBuffer outputBuffer, String arguments, String extension) {
		if (outputBuffer == null)
			return;
		String name = suggestedFileName(null, extension);
		String filePath = getPathForExport(arguments, name, null, null);
		if (filePath!=null) 
			MesquiteFile.putFileContents(filePath, outputBuffer, true);
	}
	/*.................................................................................................................*/
	public void saveExportedFileToFilePath(String filePath, MesquiteStringBuffer outputBuffer) {
		if (outputBuffer == null)
			return;
		if (filePath!=null) 
			MesquiteFile.putFileContents(filePath, outputBuffer, true);
	}
	/*.................................................................................................................*/
	/** Returns the Character data as a StringBuffer in the Interperter's format.  
	 * SHOULD BE AVOIDED in general, for memory reasons.
	 * Called only by writeMatrixToFile, which should be overridden to write line by line instead for memory reasons. */
	protected  MesquiteStringBuffer getDataAsFileText(MesquiteFile file, CharacterData data) {
		return null;
	}
	/*.................................................................................................................*/
	public boolean writeMatrixToFile(CharacterData data, String path) {
		//The file should contain the path to be written
		MesquiteStringBuffer msb = getDataAsFileText(data.getFile(), data);
		if (msb!=null) {
			MesquiteFile.putFileContents(path, msb, true);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	
	public int getTotalFilesToImport() {
		return totalFilesToImport;
	}
	public void setTotalFilesToImport(int totalFilesToImport) {
		this.totalFilesToImport = totalFilesToImport;
	}
	public int getImportFileNumber() {
		return importFileNumber;
	}
	public void setImportFileNumber(int importFileNumber) {
		this.importFileNumber = importFileNumber;
	}
	public boolean getMultiFileImport() {
		return multiFileImport;
	}
	public void setMultiFileImport(boolean multiFileImport) {
		this.multiFileImport = multiFileImport;
	}
	public int getLastNewTaxonFilled() {
		return lastNewTaxonFilled;
	}
	public void setLastNewTaxonFilled(int lastNewTaxonFilled) {
		this.lastNewTaxonFilled = lastNewTaxonFilled;
		maximumTaxonFilled = lastNewTaxonFilled;
	}
	public int getMaximumTaxonFilled() {
		return maximumTaxonFilled;
	}
	public void setMaximumTaxonFilled(int maximumTaxonFilled) {
		this.maximumTaxonFilled = maximumTaxonFilled;
	}
	public void checkMaximumTaxonFilled(int taxonFilled) {
		if (taxonFilled> this.maximumTaxonFilled)
			 maximumTaxonFilled = taxonFilled;
	}
	public int getOriginalNumTaxa() {
		return originalNumTaxa;
	}
	public void setOriginalNumTaxa(int originalNumTaxa) {
		this.originalNumTaxa = originalNumTaxa;
	}


}




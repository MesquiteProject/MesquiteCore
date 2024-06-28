/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.SaveMatricesFileProcessor; 


import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class SaveMatricesFileProcessor extends FileProcessor {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(FileInterpreterI.class, getName() + " needs a file exporter.",
				null);
		e2.setPriority(2);
	}
	MesquiteTable table;
	CharacterData data;
	MesquiteSubmenuSpec mss= null;
	FileInterpreter exporterTask;
	String exporterString = null;
	String directoryPath, baseDirectoryPath;
	String relativeDirectoryPath;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!MesquiteThread.isScripting()) {
			directoryPath = getProject().getHomeDirectoryName();
			directoryPath = MesquiteFile.chooseDirectory("Where to save files?"); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
			if (StringUtil.blank(directoryPath))
				return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
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
   	/** Called to inform module what is base directory of files.*/
   	public void setBaseDirectory(String path){
   		baseDirectoryPath = path;
		if (baseDirectoryPath != null && directoryPath == null && relativeDirectoryPath != null)
			directoryPath = MesquiteFile.composePath(baseDirectoryPath, relativeDirectoryPath);
  	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setFileInterpreter ", exporterTask);  
		if (directoryPath.contains(baseDirectoryPath))
			temp.addLine("setRelativeDirectoryPath " + ParseUtil.tokenize(MesquiteFile.decomposePath(baseDirectoryPath, directoryPath)));  
		else
			temp.addLine("setDirectoryPath " + ParseUtil.tokenize(directoryPath));  
		return temp;
	}
	/*.................................................................................................................*/
	public void queryOptionsOtherThanEmployees() {
		String current = "";
		if (directoryPath != null)
			current = " (current: " + StringUtil.getLastItem(directoryPath, MesquiteFile.fileSeparator) + ")";

		String temp = MesquiteFile.chooseDirectory("Where to save files?" + current); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
		if (!StringUtil.blank(temp)) {
			directoryPath = temp;
			if (baseDirectoryPath!= null) {
				relativeDirectoryPath = MesquiteFile.decomposePath(baseDirectoryPath, directoryPath);
			}
			}
		queryOptions();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
//set file interpreter not being respected..arguments.
		if (checker.compare(this.getClass(), "Sets the module that alters data", "[name of module]", commandName, "setFileInterpreter")) {
			FileInterpreter temp =  (FileInterpreter)replaceEmployee(FileInterpreter.class, arguments, "Exporter", exporterTask);
			if (temp!=null) {
				exporterTask = temp;
				exporterString = exporterTask.getName();
				return exporterTask;
			}

		}
		else if (checker.compare(this.getClass(), "Sets the directory path", "[path]", commandName, "setDirectoryPath")) {
			directoryPath = parser.getFirstToken(arguments);
		}
		else if (checker.compare(this.getClass(), "Sets the relative directory path", "[path]", commandName, "setRelativeDirectoryPath")) {
			String relativeDirectoryPath = parser.getFirstToken(arguments);
			if (baseDirectoryPath != null)
				directoryPath = MesquiteFile.composePath(baseDirectoryPath, relativeDirectoryPath);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** if returns true, then requests to remain on even after alterFile is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}

	/*.................................................................................................................*/
	boolean queryOptions(){
		Taxa taxa = null;
		if (getProject().getNumberTaxas()==0) {
			discreetAlert("Data matrices cannot be exported until taxa exist in file.");
			return false;
		}
		else 
			taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to save copies of character matrices?");
		if (taxa == null)
			return false;

		incrementMenuResetSuppression();
		getProject().incrementProjectWindowSuppression();

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog =  new ExtensibleDialog(containerOfModule(), "Export Matrices", buttonPressed);
		String message = "This will export matrices to separate files";
		dialog.addLargeTextLabel(message);
		dialog.addBlankLine();
		dialog.suppressNewPanel();

		MesquiteModule[] fInterpreters = getFileCoordinator().getImmediateEmployeesWithDuty(FileInterpreterI.class);
		int count=1;
		for (int i=0; i<fInterpreters.length; i++) {
			if (((FileInterpreterI)fInterpreters[i]).canExportEver())
				count++;
		}
		String [] exporterNames = new String[count];
		exporterNames[0] = "NEXUS file";
		count = 1;
		for (int i=0; i<fInterpreters.length; i++)
			if (((FileInterpreterI)fInterpreters[i]).canExportEver()) {
				exporterNames[count] = fInterpreters[i].getName();
				count++;
			}

		Choice exporterChoice = dialog.addPopUpMenu ("File Format", exporterNames, 0);
		exporterChoice.select(exporterString);
		dialog.addBlankLine();
		dialog.completeAndShowDialog();


		exporterString = exporterChoice.getSelectedItem();

		dialog.dispose();
		dialog = null;
		return true;

	}
	/*.................................................................................................................*/
	/** Called to alter file. */
	public boolean processFile(MesquiteFile file){
		boolean usePrevious = false;
		if (exporterString == null && okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying about options")){ //need to check if can proceed
			if (!queryOptions())  
				return false;

		}
		else
			usePrevious = true;
		MesquiteProject proj = file.getProject();
		FileCoordinator coord = getFileCoordinator();
		if (exporterString == null)
			exporterString = "NEXUS file";

		exporterTask = (FileInterpreter)coord.findEmployeeWithName(exporterString);
		if (exporterTask == null)
			return false;
		Taxa taxa;
		if (proj == null)
			return false;
		getProject().incrementProjectWindowSuppression();
		incrementMenuResetSuppression();
		CompatibilityTest test = exporterTask.getCompatibilityTest();
		boolean multiple = proj.getNumberCharMatrices(file)>1;
		for (int im = 0; im < proj.getNumberCharMatrices(file); im++){
			CharacterData data = proj.getCharacterMatrix(file, im);
			if (test == null || test.isCompatible(data.getStateClass(), getProject(), this)) {
				taxa = data.getTaxa();

				String filePath = file.getPath();
				String fileName = file.getFileName();
				if (filePath.endsWith(".nex") || filePath.endsWith(".fas")){
					filePath = filePath.substring(0, filePath.length()-4);
					fileName = fileName.substring(0, fileName.length()-4);

				}
				String path = directoryPath;
				if (path == null)
					path = filePath;
				path= path + MesquiteFile.fileSeparator + fileName;
				if (multiple){
					path = path + (im + 1);
					fileName = fileName + (im + 1);
				}

				path = path + "." + exporterTask.preferredDataFileExtension(); 
				if (!StringUtil.blank(exporterTask.preferredDataFileExtension()) && !fileName.endsWith(exporterTask.preferredDataFileExtension()))
					fileName = fileName + "." + exporterTask.preferredDataFileExtension();
				MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(path), CommandChecker.defaultChecker); //TODO: never scripting???
				TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
				Taxa newTaxa =taxa.cloneTaxa();
				newTaxa.addToFile(tempDataFile, null, taxaManager);

				tempDataFile.exporting =1;
				if (data.getNumChars()  == 0){
					MesquiteMessage.warnUser("Matrix to be written has no characters; it will not be written.  Name: " + data.getName() + " (type: " + data.getDataTypeName() + ")");
					return false;
				}
				CharacterData			newMatrix = data.cloneData();
				if (newMatrix == null){
					MesquiteMessage.warnUser("Matrix NOT successfully cloned for file saving: " + data.getName() + " (type: " + data.getDataTypeName() + "; " + data.getNumChars() + " characters)");
					return false;
				}
				newMatrix.setName(data.getName());

				logln("Saving file " + path);	
				newMatrix.addToFile(tempDataFile, getProject(), null);
				data.copyCurrentSpecsSetsTo(newMatrix);
				tempDataFile.setPath(path);


				//should allow choice here
				saveFile(exporterString, tempDataFile, fileName, directoryPath, coord, usePrevious); 
				tempDataFile.exporting = 2;  //to say it's the second or later export in sequence

				newMatrix.deleteMe(false);
				newMatrix = null;
				tempDataFile.close();
				System.gc();
			}
		}



		resetAllMenuBars();
		getProject().decrementProjectWindowSuppression();
		decrementMenuResetSuppression();

		return true;
	}
	public void saveFile(String exporterName, MesquiteFile file, String fileName, String directoryPath, FileCoordinator coord, boolean usePrevious){
		if (exporterName.equals("NEXUS file"))
			coord.writeFile(file);
		else if (exporterTask instanceof FileInterpreterI) {
			String s = "file = " + StringUtil.tokenize(fileName) + " directory = " + StringUtil.tokenize(directoryPath) + " noTrees";
			if (usePrevious)
				s += " usePrevious";
			coord.export((FileInterpreterI)exporterTask, file, s);
		}
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Export Matrices";
	}
	/*.................................................................................................................*/
	public String getNameAndParameters() {
		String addendum = "";
		if (exporterString != null)
			addendum += " as " + exporterString;
		if (directoryPath != null) 
				addendum += " in " + StringUtil.getLastItem(directoryPath, MesquiteFile.fileSeparator);
			
		return "Export Matrices" + addendum;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages file exporting modules to export all matrices in a file." ;
	}

}



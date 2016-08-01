/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.SaveMatrixCopies; 

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.charMatrices.lib.*;

public class SaveMatrixCopies extends FileInit implements ItemListener {
	ExtensibleDialog dialog = null;
	String exporterString = "NEXUS file";

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0)
			exporterString = prefs[0];
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "exporterString", exporterString);  
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("exporterString".equalsIgnoreCase(tag))
			exporterString = StringUtil.cleanXMLEscapeCharacters(content);
	}

	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		MesquiteSubmenuSpec mmis2 = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "Save Copy of Matrix", makeCommand("saveCopMatrix",  this),  (ListableVector)getProject().datas);
		mmis2.setBehaviorIfNoChoice(MesquiteSubmenuSpec.SHOW_SUBMENU);
		getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "Save Multiple Matrices", makeCommand("saveCopMatrices",  this), CharMatrixSource.class);
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	private void saveCopMatrix(CharacterData data, String path) {
		if (data==null)
			return;
		incrementMenuResetSuppression();
		Taxa taxa = data.getTaxa();

		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(path), CommandChecker.defaultChecker); //TODO: never scripting???
		TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
		Taxa newTaxa =taxa.cloneTaxa();
		newTaxa.addToFile(tempDataFile, null, taxaManager);

		CharacterData newData = data.cloneData();
		newData.setName(data.getName());
		newData.addToFile(tempDataFile, getProject(), null);
		coord.writeFile(tempDataFile);
		tempDataFile.close();
		decrementMenuResetSuppression();
	}

	public void saveFile(String exporterName, MesquiteFile file, String fileName, String directoryPath, boolean usePrevious, FileCoordinator coord){
		if (exporterName.equals("NEXUS file"))
			coord.writeFile(file);
		else {
			FileInterpreterI exporter = (FileInterpreterI)coord.findEmployeeWithName(exporterName);
			if (exporter!=null) {
				String ext = exporter.preferredDataFileExtension();
				if (StringUtil.blank(ext))
					ext = "";
				else
					ext = "." + ext;
				String s = "file = " + StringUtil.tokenize(fileName + ext) + " directory = " + StringUtil.tokenize(directoryPath) + " noTrees";
				if (usePrevious)
					s+= " usePrevious";
				coord.export(exporter, file, s);
			}
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Saves a copy of the character data matrix to a separate file", "[id number of data matrix]", commandName, "saveCopMatrix")) {
			int t = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(t) && t< getProject().getNumberCharMatricesVisible()) {
				long id  = MesquiteLong.fromString(parser.getNextToken());
				CharacterData d = getProject().getCharacterMatrixVisible(t);
				if (d!=null) {
					String path = MesquiteFile.saveFileAsDialog("Save copy of matrix to file");
					if (!StringUtil.blank(path))
						saveCopMatrix(d, path);
				}
			}

		}
		else if (checker.compare(this.getClass(), "Saves copies of a series of character matrices to files", "[name of module to fill the matrix]", commandName, "saveCopMatrices")) {
			//ask user how which taxa, how many characters
			//create chars block and add to file
			//return chars
			CharacterData newMatrix=null;
			Taxa taxa = null;
			if (getProject().getNumberTaxas()==0) {
				alert("Data matrices cannot be created until taxa exist in file.");
				return null;
			}
			else 
				taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to save copies of character matrices?");
			if (taxa == null)
				return null;

			//	MesquiteFile file=chooseFile();
			MainThread.incrementSuppressWaitWindow();
			CharMatrixSource characterSourceTask;
			if (StringUtil.blank(arguments))
				characterSourceTask = (CharMatrixSource)hireEmployee(CharMatrixSource.class, "Export matrices from:");
			else
				characterSourceTask = (CharMatrixSource)hireNamedEmployee(CharMatrixSource.class, arguments);
			if (characterSourceTask != null) {
				incrementMenuResetSuppression();
				getProject().incrementProjectWindowSuppression();

				MesquiteInteger buttonPressed = new MesquiteInteger(1);
				int num = characterSourceTask.getNumberOfMatrices(taxa);
				//ExtensibleDialog dialog = templateManger.getChooseTemplateDLOG(taxa, templateContainer, buttonPressed);
				dialog = new ExtensibleDialog(containerOfModule(), "Save Multiple Matrices", buttonPressed);
				String message = "This will save a series of files, each containing a matrix, using the matrix source: " + characterSourceTask.getName();
				dialog.addLargeTextLabel(message);
				dialog.addBlankLine();
				useBaseName = dialog.addRadioButtons (new String[] {"Name files by matrix names", "Name files by base name and number"}, 0);
				
				baseLabel = dialog.addLabel("Base name for files:");
				dialog.suppressNewPanel();
				baseNameField = dialog.addTextField("untitled");
				useBaseName.addItemListener(this);
				baseNameField.setEnabled(useBaseName.getValue() == 1);
				baseLabel.setEnabled(useBaseName.getValue() == 1);
				SingleLineTextField numReps = null;
				if (!MesquiteInteger.isCombinable(num)) {
					dialog.addBlankLine();
					dialog.addLabel("Number of matrices:");
					dialog.suppressNewPanel();
					numReps = dialog.addTextField("10");
				}

				MesquiteModule[] fInterpreters = getFileCoordinator().getImmediateEmployeesWithDuty(FileInterpreterI.class);
				int count=0;
				for (int i=0; i<fInterpreters.length; i++) {
					if (((FileInterpreterI)fInterpreters[i]).canExportEver())
						count++;
				}
				String [] exporterNames = new String[count];
				count = 0;
				for (int i=0; i<fInterpreters.length; i++)
					if (((FileInterpreterI)fInterpreters[i]).canExportEver()) {
						exporterNames[count] = fInterpreters[i].getName();
						count++;
					}

				Choice exporterChoice = dialog.addPopUpMenu ("File Format", exporterNames, 0);
				exporterChoice.select(exporterString);
				dialog.addBlankLine();
				dialog.completeAndShowDialog();
				if (dialog.query() != 0)
					return bailOut(null, characterSourceTask, null);
				String baseName = baseNameField.getText();
				if (numReps != null)
					num = MesquiteInteger.fromString(numReps.getText());
				if (!MesquiteInteger.isCombinable(num)) 
					return bailOut(null, characterSourceTask, null);
				boolean useBaseNameForFiles = useBaseName.getValue() == 1;
				String directoryPath = MesquiteFile.chooseDirectory("Where to save files?"); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
				if (StringUtil.blank(directoryPath))
					return bailOut(null, characterSourceTask, null);
				String basePath = directoryPath + MesquiteFile.fileSeparator ; //+ baseName;
				exporterString = exporterChoice.getSelectedItem();

				dialog.dispose();
				useBaseName.removeItemListener(this);
				dialog = null;


				StringBuffer outputBuffer=null;
				String s2 = "";

				FileCoordinator coord = getFileCoordinator();
				MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(basePath + baseName + ".nex"), CommandChecker.defaultChecker); //TODO: never scripting???
				TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
				Taxa newTaxa =taxa.cloneTaxa();
				newTaxa.addToFile(tempDataFile, null, taxaManager);

				ProgressIndicator progIndicator = new ProgressIndicator(getProject(),"Saving files", num);
				progIndicator.start();
				progIndicator.setCurrentValue(0);
				boolean usePrevious = false;
				tempDataFile.exporting =1;
				ListableVector names = new ListableVector();
				
				try {
					for (int iMatrix = 0; iMatrix<num; iMatrix++){
						if (progIndicator!=null)
							progIndicator.setText("Saving file "+(iMatrix+1)+" of " + num);
						MCharactersDistribution matrix = characterSourceTask.getMatrix(taxa, iMatrix);
						if (matrix==null)
							return bailOut(tempDataFile, characterSourceTask, progIndicator);

						CharactersManager manageCharacters = (CharactersManager)findElementManager(CharacterData.class);
						CharMatrixManager manager = manageCharacters.getMatrixManager(matrix.getCharacterDataClass());
						if (manager != null){
							String fileName = baseName + (iMatrix+1);
							if (!useBaseNameForFiles)
								fileName = characterSourceTask.getMatrixName(taxa, iMatrix);
							fileName = names.getUniqueName(fileName, "-");
							newMatrix = matrix.makeCharacterData(manager, taxa);
							newMatrix.setName(characterSourceTask.getMatrixName(taxa, iMatrix));
							
							logln("Saving file " + basePath + fileName + "\n" + newMatrix.getExplanation() + "\n");	
							newMatrix.addToFile(tempDataFile, getProject(), null);
							TreeVector trees = null;
							if (matrix.getBasisTree()!=null) {
								trees = new TreeVector(taxa);
								trees.setName("Basis Tree");
								trees.setAnnotation("Tree used a basis for character matrix.  For example the matrix may have been simulated on the tree.", false);
								trees.addElement(matrix.getBasisTree().cloneTree(), false);//no need to establish listener to Taxa, as temporary
								trees.addToFile(tempDataFile, getProject(), null);
							}
							tempDataFile.setPath(basePath +  fileName + ".nex");  //if nexus

							names.addElement(new MesquiteString(fileName, fileName), false);
							//should allow choice here
							saveFile(exporterString, tempDataFile, fileName, directoryPath, usePrevious, coord); 
							tempDataFile.exporting = 2;  //to say it's the second or later export in sequence

							newMatrix.deleteMe(false);
							newMatrix = null;
							if (trees != null)
								trees.deleteMe(false);
							trees = null;
							System.gc();
						}
						if (progIndicator!=null) {
							progIndicator.setCurrentValue(iMatrix+1);
							if (progIndicator.isAborted()) {
								break;
							}
						}
						usePrevious = true;
					}
				}
				catch (Exception e) {
					// clean up anything added
					e.printStackTrace();

					MesquiteMessage.notifyUser("There was a problem with creating or saving the matrices, and the process was aborted.");

					if (newMatrix!=null) {
						newMatrix.deleteMe(false);
						newMatrix = null;
					}
				}
				names.dispose(true);
				
				tempDataFile.close();
				
				if (progIndicator!=null) 
					progIndicator.goAway();

				fireEmployee(characterSourceTask);
				resetAllMenuBars();
				getProject().decrementProjectWindowSuppression();
				decrementMenuResetSuppression();
			}
			MainThread.decrementSuppressWaitWindow();

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	RadioButtons useBaseName = null;
	JLabel baseLabel = null;
	SingleLineTextField baseNameField;
	public void itemStateChanged(ItemEvent e) {
		if (useBaseName != null && baseNameField != null && baseLabel != null){
			baseNameField.setEnabled(useBaseName.getValue() == 1);
			baseLabel.setEnabled(useBaseName.getValue() == 1);
		}
	}


	private Object bailOut(MesquiteFile tempDataFile, MesquiteModule characterSourceTask, ProgressIndicator progIndicator){
		if (tempDataFile !=null)
			tempDataFile.close();
		if (progIndicator !=null)
			progIndicator.goAway();
		if (dialog != null) {
			dialog.dispose();
			dialog = null;
		}
		fireEmployee(characterSourceTask);
		resetAllMenuBars();
		getProject().decrementProjectWindowSuppression();
		decrementMenuResetSuppression();
		MainThread.decrementSuppressWaitWindow();
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Save matrix copies";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Provides for the saving of copies of matrices to separate files.  This is available under the Characters menu." ;
	}
}



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharMatricesListExport;

import mesquite.lists.lib.*;

import java.util.*;

import javax.swing.JLabel;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lib.ui.RadioButtons;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== */
public class CharMatricesListExport extends CharMatricesListProcessorUtility implements ItemListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Export Matrices ";
	}

	public String getNameForMenuItem() {
		return "Export Matrices...";
	}

	public String getExplanation() {
		return "Exports selected matrices in List of Character Matrices window." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		 return true;
	}
	/*.................................................................................................................*/

	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	ExtensibleDialog dialog;
	RadioButtons useBaseName = null;
	JLabel baseLabel = null;
	SingleLineTextField baseNameField;
	String exporterString = "NEXUS file";

	public void itemStateChanged(ItemEvent e) {
		if (useBaseName != null && baseNameField != null && baseLabel != null){
			baseNameField.setEnabled(useBaseName.getValue() == 1);
			baseLabel.setEnabled(useBaseName.getValue() == 1);
		}
	}
	private boolean bailOut(MesquiteFile tempDataFile, ProgressIndicator progIndicator){
		if (tempDataFile !=null)
			tempDataFile.close();
		if (progIndicator !=null)
			progIndicator.goAway();
		if (dialog != null) {
			dialog.dispose();
			dialog = null;
		}
		resetAllMenuBars();
		getProject().decrementProjectWindowSuppression();
		decrementMenuResetSuppression();
		MainThread.decrementSuppressWaitWindow();
		return false;
	}
	//code copied from SaveMatrixCopies
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
	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		if (datas.size() == 0)
		return false;
		incrementMenuResetSuppression();
		getProject().incrementProjectWindowSuppression();

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		int num = datas.size();
		dialog = new ExtensibleDialog(containerOfModule(), "Save Matrices", buttonPressed);
		String message = "This will save a series of files, each containing a matrix";
		dialog.addLargeTextLabel(message);
		dialog.addBlankLine();
		useBaseName = dialog.addRadioButtons (new String[] {"Name files by matrix names", "Name files by base name and number"}, 0);
		
		baseLabel = dialog.addLabel("Base name for files:");
		dialog.suppressNewPanel();
		baseNameField = dialog.addTextField("untitled");
		useBaseName.addItemListener(this);
		baseNameField.setEnabled(useBaseName.getValue() == 1);
		baseLabel.setEnabled(useBaseName.getValue() == 1);

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
			return bailOut(null, null);
		String baseName = baseNameField.getText();
		if (!MesquiteInteger.isCombinable(num)) 
			return bailOut(null, null);
		boolean useBaseNameForFiles = useBaseName.getValue() == 1;
		String directoryPath = MesquiteFile.chooseDirectory("Where to save files?"); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
		if (StringUtil.blank(directoryPath))
			return bailOut(null, null);
		String basePath = directoryPath + MesquiteFile.fileSeparator ; //+ baseName;
		exporterString = exporterChoice.getSelectedItem();

		dialog.dispose();
		useBaseName.removeItemListener(this);
		dialog = null;



		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(basePath + baseName + ".nex"), CommandChecker.defaultChecker); //TODO: never scripting???
		TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
		CharacterData newMatrix=null;
		CharacterData data = (CharacterData)datas.elementAt(0);
		Taxa newTaxa =data.getTaxa().cloneTaxa(); //Debugg.println allow for a different taxa block on a later matrix!!!!!!!!
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
				data = (CharacterData)datas.elementAt(iMatrix);
				MCharactersDistribution matrix = data.getMCharactersDistribution();
				if (matrix==null)
					return bailOut(tempDataFile, progIndicator);

				CharactersManager manageCharacters = (CharactersManager)findElementManager(CharacterData.class);
				CharMatrixManager manager = manageCharacters.getMatrixManager(matrix.getCharacterDataClass());
				if (manager != null){
					String fileName = baseName + (iMatrix+1);
					if (!useBaseNameForFiles)
						fileName = data.getName();
					fileName = names.getUniqueName(fileName, "-");
					newMatrix = matrix.makeCharacterData(manager, newTaxa);
					newMatrix.setName(data.getName());
					
					logln("Saving file " + basePath + fileName + "\n" + newMatrix.getExplanation() + "\n");	
					newMatrix.addToFile(tempDataFile, getProject(), null);
					tempDataFile.setPath(basePath +  fileName + ".nex");  //if nexus

					names.addElement(new MesquiteString(fileName, fileName), false);
					//should allow choice here
					saveFile(exporterString, tempDataFile, fileName, directoryPath, usePrevious, coord); 
					tempDataFile.exporting = 2;  //to say it's the second or later export in sequence

					newMatrix.deleteMe(false);
					newMatrix = null;
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

		resetAllMenuBars();
		getProject().decrementProjectWindowSuppression();
		decrementMenuResetSuppression();	
		return true;
}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	public void endJob() {
		super.endJob();
	}

}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.io.ExportFusedPhylip;

import java.awt.Checkbox;

import mesquite.categ.lib.*;
import mesquite.io.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.CharactersManager;

public class ExportFusedPhylip extends InterpretPhylip {

	boolean badImportWarningGiven = false;
	boolean useTranslationTable = true;

	/*.................................................................................................................*/
	public void setPhylipState(CharacterData data, int ic, int it, char c){
	}
	/*.................................................................................................................*/
	public boolean initializeExport(Taxa taxa) {  
		if (useTranslationTable){
			taxonNamer = new SimpleNamesTaxonNamer();
			taxonNamer.initialize(taxa);
		}

		return true;  
	}
	/*........................../*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}

	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return project.getNumberCharMatrices(CategoricalState.class) > 0;  //
	}
	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==ProteinState.class || dataClass==DNAState.class || dataClass==CategoricalState.class);
	}
	/*.................................................................................................................*
	public boolean getExportTrees(){
		return false;
	}
/*.................................................................................................................*/
	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  
		return null;  //not needed as can't import
	}
	/*.................................................................................................................*/
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, StringBuffer outputBuffer){
		data.statesIntoStringBuffer(ic, it, outputBuffer, false);
	}
	boolean exportRAxMLModelFile = true;
	/*.................................................................................................................*/
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		PhylipExporterDialog exportDialog = new PhylipExporterDialog(this,containerOfModule(), "Export Phylip Options", buttonPressed);

		Checkbox excludedCharactersCheckbox = exportDialog.addCheckBox("export excluded characters", localWriteExcludedChars);
		Checkbox exportTreesCheckbox = exportDialog.addCheckBox("export trees if present", exportTrees);
		Checkbox exportRAxMLModelFileCheckbox = exportDialog.addCheckBox("save RAxML model file", exportRAxMLModelFile);
		Checkbox useTranslationTableCheckbox = exportDialog.addCheckBox("use simple taxon names (for RAxML)", useTranslationTable);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		if (ok) {
			localWriteExcludedChars = excludedCharactersCheckbox.getState();
			exportTrees = exportTreesCheckbox.getState();
			exportRAxMLModelFile = exportRAxMLModelFileCheckbox.getState();
			useTranslationTable = useTranslationTableCheckbox.getState();
			userSpecifiedWriteExcludedChars = true;
			taxonNameLength = exportDialog.getTaxonNamesLength();
		}
		exportDialog.dispose();
		return ok;
	}	
	/*.................................................................................................................*/
	public boolean exportMultipleMatrices(){
		return true;
	}
	/*.................................................................................................................*/
	public String getTranslationTablePath(){
		return getExportedFileDirectory()+IOUtil.translationTableFileName;
	}
	/*.................................................................................................................*/
	public void writeExtraFiles(Taxa taxa){
		if (useTranslationTable) {
			String table = ((SimpleNamesTaxonNamer)taxonNamer).getTranslationTable(taxa);
			if (StringUtil.notEmpty(table)) {
				String filePath = getTranslationTablePath();
				if (StringUtil.notEmpty(filePath))
					MesquiteFile.putFileContents(filePath, table, true);
			}
		}
		if (exportRAxMLModelFile) {
			StringBuffer sb = new StringBuffer();
			int numMatrices = getProject().getNumberCharMatricesVisible(CategoricalState.class);
			CharacterData data;
			int numCharWrite=0;
			int totalNumCharWrite=1;
			if (MesquiteThread.isScripting()){
			}
			else {

				for (int im = 0; im<numMatrices; im++) {
					data = getProject().getCharacterMatrixVisible(taxa, im, CategoricalState.class);
					if (data==null) continue;

					if (!writeExcludedCharacters){
						numCharWrite =  data.numCharsCurrentlyIncluded(this.writeOnlySelectedData);
					} else {
						numCharWrite =  data.numberSelected(this.writeOnlySelectedData);
					}

					if (data!=null) {
						String name = StringUtil.cleanseStringOfFancyChars(data.getName());
						name = StringUtil.blanksToUnderline(name);
						if (data instanceof DNAData) 
							sb.append("DNA, ");
						else if (data instanceof ProteinData) 
							sb.append("JTT, ");
						else if (data instanceof CategoricalData)   //TODO: check if binary or multistate
							sb.append("MULTI, ");
						sb.append(name+ " = " + totalNumCharWrite + "-" + (totalNumCharWrite+numCharWrite-1)+StringUtil.lineEnding());
					}
					totalNumCharWrite+= numCharWrite;
				}
				String filePath = MesquiteFile.saveFileAsDialog("Save RAxML model file");
				if (StringUtil.blank(sb.toString()) || StringUtil.blank(filePath))
					return;
				MesquiteFile.putFileContents(filePath, sb.toString(), true);
			}
		}
	}
	/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return getProject().chooseData(containerOfModule(), file, null, CategoricalState.class, "Select data to export");
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Fused Matrix Export (Phylip/RAxML)";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports Phylip matrices that consist of multiple matrices, preparing them for RAxML." ;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}

}

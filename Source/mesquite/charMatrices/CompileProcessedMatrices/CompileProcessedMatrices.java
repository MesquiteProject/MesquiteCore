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

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class CompileProcessedMatrices extends FileProcessor {
	String saveFile = null;
	String tempFile = null;
	TaxonNameAlterer nameAlterer;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
	ListableVector taxonNames = new ListableVector();
	void checkTaxonList(CharacterData data, String saveFile){
		Taxa taxa = data.getTaxa();
		boolean added = false;
		if (nameAlterer != null)
			nameAlterer.alterTaxonNames(taxa, null);
		for (int it = 0; it<taxa.getNumTaxa(); it++){
		
			if (taxonNames.indexOfByName(taxa.getTaxonName(it))<0){
				MesquiteString n = new MesquiteString(taxa.getTaxonName(it));
				n.setName(taxa.getTaxonName(it));
				taxonNames.addElement(n, false);
				added = true;
			}
		}
		if (added) {
			String matrices = 	 MesquiteFile.getFileContentsAsString(tempFile);
			if (matrices == null)
				matrices = "";
			String block = "#NEXUS" + StringUtil.lineEnding() + "BEGIN TAXA;" + StringUtil.lineEnding() + " DIMENSIONS NTAX=" + taxonNames.size() + ";" + StringUtil.lineEnding() + " TAXLABELS" + StringUtil.lineEnding() + "   ";
			for (int it = 0; it<taxonNames.size(); it++){
				MesquiteString n = (MesquiteString)taxonNames.elementAt(it);
				block += n.getName() + "  ";
			}
			block += ";" + StringUtil.lineEnding() + "END;" + StringUtil.lineEnding() + StringUtil.lineEnding();
			MesquiteFile.putFileContents(saveFile, block + matrices, true);
		}
	}
	void writeMatrixToFile(String file, CharacterData data, String matrixName){
		MesquiteFile.appendFileContents(file, "BEGIN CHARACTERS;" + StringUtil.lineEnding() + "\tTITLE " + ParseUtil.tokenize(matrixName) + ";" + StringUtil.lineEnding() , true);
		MesquiteFile.appendFileContents(file, "\tDIMENSIONS NCHAR= " + data.getNumChars() + " ;" + StringUtil.lineEnding() , true);
		MesquiteFile.appendFileContents(file, "\tFORMAT DATATYPE = DNA GAP = - MISSING = ?;" + StringUtil.lineEnding() + "\tMATRIX" + StringUtil.lineEnding() , true);
		for (int it = 0; it < data.getNumTaxa(); it++){
			MesquiteFile.appendFileContents(file, "\t" + ParseUtil.tokenize(data.getTaxa().getTaxonName(it)) + "\t" , true);
			StringBuffer description = new StringBuffer();
			for (int ic =0; ic<data.getNumChars(); ic++){
				data.statesIntoNEXUSStringBuffer(ic, it, description);
			}
			MesquiteFile.appendFileContents(file, description.toString() + StringUtil.lineEnding(), true);
		}
		MesquiteFile.appendFileContents(file, "\t;" + StringUtil.lineEnding() + "END;" + StringUtil.lineEnding() + StringUtil.lineEnding() , true);
	}
	/*.................................................................................................................*/

	/** Called to alter file. */
	public boolean processFile(MesquiteFile file){

		if (saveFile == null || okToInteractWithUser(CAN_PROCEED_ANYWAY, "Asking for file to save")){ //need to check if can proceed

			MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), "Output File for Matrices(s)", FileDialog.SAVE);
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setVisible(true);
			String fileName=fdlg.getFile();
			String directory=fdlg.getDirectory();
			// fdlg.dispose();
			if (StringUtil.blank(fileName) || StringUtil.blank(directory))
				return false;
			saveFile = MesquiteFile.composePath(directory, fileName);
			tempFile = MesquiteFile.composePath(directory, MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase() + fileName)) ;
			if (AlertDialog.query(containerOfModule(), "Alter names?", "The matrices found in the files of the selected folder will be accumulated into a single NEXUS file.  All taxa will be accumulated into a single block."
					+"In case the different files have variants of taxon names, do you want to alter the names of each matrix as it is read?", "Alter Names", "No"))
					nameAlterer = (TaxonNameAlterer) hireEmployee(TaxonNameAlterer.class, "How do you want to alter the names of taxa in each matrix?");
		}
		if (saveFile == null)
			return false;
		Listable[] matrices = proj.getFileElements(CharacterData.class);	
		if (matrices == null)
			return false;
		for (int im = 0; im < matrices.length; im++){
			CharacterData data = (CharacterData)matrices[im];
			if (data.getFile() == file){
				checkTaxonList(data, saveFile);
				writeMatrixToFile(saveFile, data, file.getFileName());
				writeMatrixToFile(tempFile, data, file.getFileName());
			}
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



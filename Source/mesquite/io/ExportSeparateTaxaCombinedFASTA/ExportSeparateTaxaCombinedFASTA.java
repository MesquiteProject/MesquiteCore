/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.ExportSeparateTaxaCombinedFASTA;

/*~~  */
//Manaus
import java.awt.*;


import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;


/* ============  a file exporter ============*/

public class ExportSeparateTaxaCombinedFASTA extends FileInterpreterI {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		//	EmployeeNeed e = registerEmployeeNeed(VoucherInfoCoord.class, "Voucher information is needed for FASTA export for Genbank submissions.",
		//			"This is activated automatically when you choose this exporter.");
	}
	//VoucherInfoCoord voucherInfoTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//voucherInfoTask = (VoucherInfoCoord)hireEmployee(VoucherInfoCoord.class, null);
		return true;
	}
	public void readFile(MesquiteProject mf, MesquiteFile mNF, String arguments) {

	}

	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;  //
	}
	public boolean canImport(String arguments){
		return false;
	}
	/** returns whether module is able ever to export.*/
	public boolean canExportEver(){
		return true;
	}
	/** returns whether module has something it can export in the project.  Should be overridden*/
	public boolean canExportProject(MesquiteProject project){
		return project.getNumberCharMatrices(MolecularState.class) > 0;  //
	}

	/** returns whether module can export a character data matrix of the given type.  Should be overridden*/
	public boolean canExportData(Class dataClass){
		if (dataClass==null) return false;
		return ((MolecularState.class).isAssignableFrom(dataClass)); 
	}

	protected int taxonNameLengthLimit() {
		return 50;
	}

	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	String fileName = "untitled.nex";
	protected boolean buildFileName = false;

	
	/*.................................................................................................................*/
	public String getFileName(Taxa taxa, int it, CharacterData data, int index, String identifierString) {
		String fileName = "";
		fileName=StringUtil.cleanseStringOfFancyChars(taxa.getName(it),false,true);

		fileName += ".fas";

		return fileName;
	}


	/*.................................................................................................................*/
	public String getSequenceName(Taxa taxa, int it, String voucherID) {
		return taxa.getTaxonName(it);
	}
	/*.................................................................................................................*/
	public String getIdentifierString() {
		return "";
	}
	/*.................................................................................................................*/
	public synchronized boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		//		boolean usePrevious = args.parameterExists("usePrevious");

	/*	if (!MesquiteThread.isScripting())
			if (!getExportOptions(false, true))
				return false;
*/
		String directory = MesquiteFile.chooseDirectory("Choose directory into which files will be saved:");
		if (StringUtil.blank(directory))
			return false;
		if (!directory.endsWith(MesquiteFile.fileSeparator))
			directory+=MesquiteFile.fileSeparator;


		StringBuffer buffer = new StringBuffer(500);

		Taxa taxa = (Taxa)getProject().chooseTaxa(containerOfModule(),"For which block of taxa to export FASTA files?");
		int numTaxa = taxa.getNumTaxa();
		for (int it = 0; it<numTaxa; it++) {
			if (!writeOnlySelectedTaxa || taxa.getSelected(it)){
				buffer.setLength(0);
				int numMatrices = getProject().getNumberCharMatrices(null, taxa, MolecularState.class, true);
				for (int iM = 0; iM < numMatrices; iM++){
					CharacterData data = getProject().getCharacterMatrixVisible(taxa, iM, MolecularState.class);
					if (data != null) {
						StringBuffer taxMatrixFastaBuffer = ((MolecularData)data).getSequenceAsFasta(false,false,it, data.getName());
						String taxMatrixFasta = taxMatrixFastaBuffer.toString();
						if (StringUtil.notEmpty(taxMatrixFasta)){
							buffer.append(taxMatrixFasta);
						}
					}
				}
				String filePath = directory;
				filePath = directory+taxa.getTaxonName(it) + ".fas";
				MesquiteFile.putFileContents(filePath, buffer.toString(), true);
			}
		}

		return true;
	}


	/*.................................................................................................................*/
	public String getName() {
		return "FASTA File for each taxon, with sequences from each matrix";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports for each taxon a FASTA file whose sequences represent that taxon's sequences in each of the matrices." ;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.bayesian.ExportForBPP;


import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;
import mesquite.categ.lib.*;



public class ExportForBPP extends FileInterpreterI {
	AssociationSource associationTask;
	OneTreeSource oneTreeSourceTask;
	Taxa currentTaxa = null;
	boolean suspend = false;
	boolean phased = false;

	/*.................................................................................................................*/
	public boolean loadModule(){
		return true;
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
	}

	public boolean isPrerelease(){
		return true;
	}
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "ctl";
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return true;
		//return (project.getNumberOfFileElements(TreeVector.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return true;
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}

	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}


	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	boolean includeTaxaBlock = true;
	boolean convertToBranchLengths = true;
	String fileName = "BPP.ctl";

	public boolean getExportOptions(TreeVector trees){
		/*MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export tree file for DELINEATE", buttonPressed);
		String helpString = "This will save a tree file ready to be uploaded into Open Tree (opentreeoflife.org).  It will optionally convert node values "+
		"such as consensus frequences as branch lengths (as that is how Open Tree imports support values for branches).";
		exportDialog.appendToHelpString(helpString);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		Checkbox convertToBranchLengthsBox = exportDialog.addCheckBox("convert node values to branch lengths", convertToBranchLengths);

		exportDialog.completeAndShowDialog();

		boolean ok = (exportDialog.query()==0);

		convertToBranchLengths = convertToBranchLengthsBox.getState();

		exportDialog.dispose();
		return ok;
		*/
		return true;
	}	
	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		setPreferredTaxa(taxa);
		if (oneTreeSourceTask == null) {
			oneTreeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of containing tree");
		}
		if (oneTreeSourceTask == null)
			return;
		if (associationTask==null) {
			associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
			if (associationTask == null) 
				return;
		}

	}
	public void endJob(){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		storePreferences();
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void disposing(Object obj){
		if (obj == currentTaxa) {
			setHiringCommand(null); //since there is no rehiring
			iQuit();
		}
	}

	/*.................................................................................................................*/

	public void setPreferredTaxa(Taxa taxa){
		if (taxa !=currentTaxa) {
			if (currentTaxa!=null)
				currentTaxa.removeListener(this);
			currentTaxa = taxa;
			currentTaxa.addListener(this);
		}

	}


	/*.................................................................................................................*/
	private String prepareExportName(String name) {
		return StringUtil.tokenize(name);
	}
	/*.................................................................................................................*/
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, StringBuffer outputBuffer){
		data.statesIntoStringBuffer(ic, it, outputBuffer, false);
	}
	/*.................................................................................................................*/
	public void exportBlock(Taxa taxa, CharacterData data, StringBuffer outputBuffer, boolean writeTaxonNames) { 
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		int taxonNameLength=taxa.getLongestTaxonNameLength()+2;
		String pad = "          ";
		while (pad.length() < taxonNameLength)
			pad += "  ";

		for (int it = 0; it<numTaxa; it++){
			if (writeTaxonNames) {   // first block
				String name = "";
				name = (taxa.getTaxonName(it)+ pad);
				name = name.substring(0,taxonNameLength);
				name = StringUtil.blanksToUnderline(StringUtil.stripTrailingWhitespace(name));

				outputBuffer.append("^"+name);
				//	if (taxonNameLength>name.length())
				for (int i=0;i<taxonNameLength-name.length()+1; i++)
					outputBuffer.append(" ");
			}
			//outputBuffer.append(" ");
			for (int ic = 0; ic<numChars; ic++) {
				if ((!writeOnlySelectedData || (data.getSelected(ic))) && (writeExcludedCharacters || data.isCurrentlyIncluded(ic))&& (writeCharactersWithNoData || data.hasDataForCharacter(ic))){
					int currentSize = outputBuffer.length();
					appendPhylipStateToBuffer(data, ic, it, outputBuffer);
					if (outputBuffer.length()-currentSize>1) {
						alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
						return;
					}
				}
			}
			outputBuffer.append(getLineEnding());


		}
	}

		/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments, Taxa taxa) { 
		return getProject().chooseData(containerOfModule(), file, taxa, MolecularState.class, "Select data to export");
	}

	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		
		Taxa taxa = 	 getProject().chooseTaxa(getModuleWindow(), "Choose taxa block for populations"); 
		if (taxa==null) 
			return false;

		if (oneTreeSourceTask==null || taxa!=currentTaxa || associationTask==null) {
			initialize(taxa);
			if (oneTreeSourceTask==null || associationTask==null)
				return false;
			oneTreeSourceTask.initialize(currentTaxa);
		}

		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(null)) {
				oneTreeSourceTask=null;
				associationTask=null;
				return false;
			}

		Tree tree = oneTreeSourceTask.getTree(currentTaxa);
		
		TaxaAssociation association = associationTask.getCurrentAssociation(currentTaxa);
		if (association==null)
			return false;
		
		
		Taxa otherTaxa = association.getOtherTaxa(taxa);
		CharacterData data = findDataToExport(file, arguments, otherTaxa);
		if (data == null)
			return false;

		StringBuffer matrixBuffer = new StringBuffer(otherTaxa.getNumTaxa()*(20 + data.getNumChars()));
		matrixBuffer.append(Integer.toString(otherTaxa.getNumTaxa())+" ");
		matrixBuffer.append(""+data.getNumChars()+this.getLineEnding());		
		exportBlock(otherTaxa,  data,  matrixBuffer, true);

			
			
			
		StringBuffer controlFileBuffer = new StringBuffer(100);
		controlFileBuffer.append("species&tree = "+taxa.getNumTaxa());
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			controlFileBuffer.append(" "+prepareExportName(taxa.getTaxonName(it)));
		}
		controlFileBuffer.append("\n\n");
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			Taxon taxon = taxa.getTaxon(it);
			int numAssociates = association.getNumAssociates(taxon);
			if (it>0)
				controlFileBuffer.append(" ");
			controlFileBuffer.append(""+numAssociates);
		}
		controlFileBuffer.append("\n\n");
		controlFileBuffer.append(tree.writeTree(Tree.BY_NAMES));
		controlFileBuffer.append("\n\n");
		controlFileBuffer.append("phase = ");
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			if (phased) 
				controlFileBuffer.append(" "+0);
			else 
				controlFileBuffer.append(" "+1);
		}


		StringBuffer imapFileBuffer = new StringBuffer(100);
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			Taxon taxon = taxa.getTaxon(it);
			Taxon[] associatedTaxa = association.getAssociates(taxon);
			for (int j=0; j<associatedTaxa.length; j++) {
				imapFileBuffer.append(prepareExportName(associatedTaxa[j].getName())+"\t" +prepareExportName(taxon.getName())+"\n");
			}
		}
		imapFileBuffer.append("\n\n");

		oneTreeSourceTask = null;
		associationTask = null;
		
		
		if (controlFileBuffer!=null) {
			saveExportedFileWithExtension(matrixBuffer, arguments, "chars.txt");
			saveExportedFileWithExtension(controlFileBuffer, arguments, preferredDataFileExtension());
			saveExportedFileWithExtension(imapFileBuffer, arguments, "imap.txt");
			return true;
		}

		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Export for BPP";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		String s =  "Exports part of the files for BPP.  ";
		return s;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}


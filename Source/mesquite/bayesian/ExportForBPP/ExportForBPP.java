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

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "startOfControlFile", startOfControlFile);  
		StringUtil.appendXMLTag(buffer, 2, "endOfControlFile", endOfControlFile);  
		StringUtil.appendXMLTag(buffer, 2, "breakAtSelectedNodes", breakAtSelectedNodes);  
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("startOfControlFile".equalsIgnoreCase(tag))
			startOfControlFile = StringUtil.cleanXMLEscapeCharacters(content);
		if ("endOfControlFile".equalsIgnoreCase(tag))
			endOfControlFile = StringUtil.cleanXMLEscapeCharacters(content);
		if ("breakAtSelectedNodes".equalsIgnoreCase(tag))
			breakAtSelectedNodes = MesquiteBoolean.fromTrueFalseString(content);
	}

	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	String startOfControlFile = "speciesdelimitation = 1 1 2 1\nspeciestree = 0\nspeciesmodelprior = 1\n";
	String endOfControlFile = "\ncleandata = 0\n\nthetaprior = 3 0.004\n\ntauprior = 3 0.002\n\nfinetune =  1: 5 0.001 0.001  0.001 0.3 0.33 1.0\n\nprint = 1 0 0 0 0\nburnin = 8000\nsampfreq = 10\nnsample = 1000\n\n";
	boolean breakAtSelectedNodes = true;
	
	String fileName = "BPP.ctl";

	public boolean getExportOptions(TreeVector trees){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(this.containerOfModule(), "BPP export options", buttonPressed);
		
		dialog.addLabel("Text for start of control file");
		TextArea startOfCtlFileField = dialog.addTextArea(startOfControlFile, 8);
		
		dialog.addLabel("Text for end of control file");
		TextArea endOfCtlFileField = dialog.addTextArea(endOfControlFile, 8);
		
		Checkbox breakAtSelectedNodesCheckbox = dialog.addCheckBox("break at selected nodes", breakAtSelectedNodes);


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			startOfControlFile = startOfCtlFileField.getText();
			endOfControlFile = endOfCtlFileField.getText();
			breakAtSelectedNodes = breakAtSelectedNodesCheckbox.getState();
			
		}
		//storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return (buttonPressed.getValue()==0);
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
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, MesquiteStringBuffer outputBuffer){
		data.statesIntoStringBuffer(ic, it, outputBuffer, false);
	}
	/*.................................................................................................................*/
	public void exportBlock(Taxa taxa, Bits taxaToInclude, CharacterData data, MesquiteStringBuffer outputBuffer, boolean writeTaxonNames) { 
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		int taxonNameLength=taxa.getLongestTaxonNameLength()+2;
		String pad = "          ";
		while (pad.length() < taxonNameLength)
			pad += "  ";

		for (int it = 0; it<numTaxa; it++){
			if (taxaToInclude==null || taxaToInclude.isBitOn(it)) {
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
						long currentSize = outputBuffer.length();
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
	}

		/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments, Taxa taxa) { 
		return getProject().chooseData(containerOfModule(), file, taxa, MolecularState.class, "Select data to export");
	}

	/*.................................................................................................................*/
	public boolean exportSubTree(String dirPath, String baseFileName, Taxa populationTaxa, Taxa specimenTaxa, MesquiteTree tree, TaxaAssociation association, int nodeNumber) { //if file is null, consider whole project open to export

		Bits populationTerminals = tree.getTerminalTaxaAsBits(nodeNumber);  // these are the terminals that need exporting
		Bits specimens = association.getAssociatesAsBits(populationTaxa, populationTerminals);

		MesquiteStringBuffer matrixBuffer = new MesquiteStringBuffer(100);
		int numMatrices = getProject().getNumberCharMatrices(null, specimenTaxa, MolecularState.class, true);
		if (numMatrices<1)
			return false;
		for (int iM = 0; iM < numMatrices; iM++){
			CharacterData data = getProject().getCharacterMatrixVisible(specimenTaxa, iM, MolecularState.class);
			if (data != null) {		
				matrixBuffer.append(Integer.toString(specimenTaxa.getNumTaxa())+" ");
				matrixBuffer.append(""+data.getNumChars()+this.getLineEnding());		
				exportBlock(specimenTaxa,  specimens, data,  matrixBuffer, true);
				matrixBuffer.append(this.getLineEnding());		
			}
		}
			
			
		MesquiteStringBuffer controlFileBuffer = new MesquiteStringBuffer(100);
		Random random = new Random(System.currentTimeMillis());
		controlFileBuffer.append("seed = "+ random.nextInt()+"\n\n");
		
		controlFileBuffer.append("seqfile = " + baseFileName+".chars.txt"+"\n");
		controlFileBuffer.append("imapfile = " + baseFileName +".imap.txt"+"\n");
		controlFileBuffer.append("outfile = " + baseFileName+".out.txt"+"\n");
		controlFileBuffer.append("mcmcfile = " + baseFileName +".mcmc.txt"+"\n\n");


		controlFileBuffer.append(startOfControlFile);
		controlFileBuffer.append("\n\n");
		controlFileBuffer.append("species&tree = "+populationTerminals.numBitsOn());
		for (int it=0; it<populationTaxa.getNumTaxa(); it++) {
			if (populationTerminals.isBitOn(it)) {
			controlFileBuffer.append(" "+prepareExportName(populationTaxa.getTaxonName(it)));
			}
		}
		controlFileBuffer.append("\n");
		for (int it=0; it<populationTaxa.getNumTaxa(); it++) {
			if (populationTerminals.isBitOn(it)) {
				Taxon taxon = populationTaxa.getTaxon(it);
				int numAssociates = association.getNumAssociates(taxon);
				if (it>0)
					controlFileBuffer.append(" ");
				controlFileBuffer.append(""+numAssociates);
			}
		}
	
		
		controlFileBuffer.append("\n");
		controlFileBuffer.append(tree.writeTreeSimpleByNames(nodeNumber, false));   
		controlFileBuffer.append("\n\n");
		controlFileBuffer.append("phase = ");
		for (int it=0; it<populationTaxa.getNumTaxa(); it++) {
			if (populationTerminals.isBitOn(it)) {
				if (phased) 
					controlFileBuffer.append(" "+0);
				else 
					controlFileBuffer.append(" "+1);
			}
		}
		controlFileBuffer.append("\n\nusedata = 1\n");
		controlFileBuffer.append("nloci = " +numMatrices+"\n\n");
		controlFileBuffer.append(endOfControlFile);


		MesquiteStringBuffer imapFileBuffer = new MesquiteStringBuffer(100);
		for (int it=0; it<populationTaxa.getNumTaxa(); it++) {
			if (populationTerminals.isBitOn(it)) {
				Taxon taxon = populationTaxa.getTaxon(it);
				Taxon[] associatedTaxa = association.getAssociates(taxon);
				for (int j=0; j<associatedTaxa.length; j++) {
					imapFileBuffer.append(prepareExportName(associatedTaxa[j].getName())+"\t" +prepareExportName(taxon.getName())+"\n");
				}
			}
		}
		imapFileBuffer.append("\n\n");

		if (controlFileBuffer!=null) {
			saveExportedFileToFilePath(dirPath + MesquiteFile.fileSeparator+baseFileName+".chars.txt", matrixBuffer);
			saveExportedFileToFilePath(dirPath + MesquiteFile.fileSeparator+baseFileName+".ctl", controlFileBuffer);
			saveExportedFileToFilePath(dirPath + MesquiteFile.fileSeparator+baseFileName+".imap.txt", imapFileBuffer);
			return true;
		}

		return false;
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
		Tree simpleTree = oneTreeSourceTask.getTree(currentTaxa);
		if (simpleTree==null ||  !(simpleTree instanceof MesquiteTree))
			return false;
		
		MesquiteTree tree = (MesquiteTree)simpleTree;
		
		TaxaAssociation association = associationTask.getCurrentAssociation(currentTaxa);
		if (association==null)
			return false;
		
		Taxa otherTaxa = association.getOtherTaxa(taxa);
		
		String dirPath = MesquiteFile.chooseDirectory("Choose export directory");
		
		if (dirPath==null)
			return false;
				
		boolean success = false;
		
		if (!tree.anySelected() || !breakAtSelectedNodes) {
			if (exportSubTree(dirPath, "bpp00", taxa, otherTaxa, tree, association, tree.getRoot())) {
				success = true;
			}
		} else {
			Bits selectedBits = tree.getSelectedBits();
			Bits allPopulationTerminals = tree.getTerminalTaxaAsBits(tree.getRoot()); 
			boolean nested = false;
			
			/*for (int i=0; i<tree.getNumberOfParts(); i++) {
				if (selectedBits.isBitOn(i)) {
					Bits terminals = tree.getTerminalTaxaAsBits(i); 
				}
			}
			*/
			
			for (int i=0; i<tree.getNumberOfParts(); i++) {
				if (selectedBits.isBitOn(i)) {
					Bits terminalsInSelectedClade = tree.getTerminalTaxaAsBits(i); 
					allPopulationTerminals.clearBits(terminalsInSelectedClade);  // now lets clear them
					if (tree.anySelectedInClade(i, false)) {
						nested = true;
					}
				}
			}
			if (allPopulationTerminals.anyBitsOn()) { // then some terminals aren't included in the selected clades.
				Debugg.println("\nWARNING:  Some terminals not included");
			} else if (nested) {  // all population terminals are included, but there is a nesting of selected clades
				Debugg.println("\nWARNING:  Selected clades are nested");
			} else {
				boolean exportAll = true;
				int counter = 0;
				int numDigits=2;
				int numParts = selectedBits.numBitsOn();
				if (numParts>1000)
					numDigits=4;
				else if (numParts>100)
					numDigits=3;
				for (int i=0; i<tree.getNumberOfParts(); i++) {
					if (selectedBits.isBitOn(i)) {
						boolean exported = exportSubTree(dirPath, "bpp" + StringUtil.getIntegerAsStringWithLeadingZeros(counter, numDigits), taxa, otherTaxa, tree, association, i);  //TODO: 
						if (!exported) {
							exportAll=false;
						}
						counter++;
					}
				}
				success = exportAll;
			}
		}

		
		if (success) {
			oneTreeSourceTask = null;
			associationTask = null;
			return true;
		} else {
			oneTreeSourceTask = null;
			associationTask = null;
			return false;
		}
			
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


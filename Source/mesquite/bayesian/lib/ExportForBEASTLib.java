/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.bayesian.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.basic.ManageSetsBlock.ManageSetsBlock;
import mesquite.categ.lib.*;



public abstract class ExportForBEASTLib extends FileInterpreterI  {


	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "nex";
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return (project.getNumberCharMatrices( CategoricalState.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==CategoricalState.class);
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}

	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}

	/*.................................................................................................................*
	public void processSingleXMLPreference (String tag, String content) {
		if ("duplicateTaxonNames".equalsIgnoreCase(tag))
			duplicateTaxonSets = MesquiteBoolean.fromTrueFalseString(content);
		super.processSingleXMLPreference(tag, content);
		preferencesSet = true;
	}

	/*.................................................................................................................*
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "duplicateTaxonNames", duplicateTaxonSets);  
		buffer.append(super.preparePreferencesForXML());
		preferencesSet = true;
		return buffer.toString();
	}


	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	protected boolean convertAmbiguities = false;
	protected boolean preferencesSet = false;
	protected boolean useData = true;
	protected String addendum = "";
	protected String fileName = "untitled.nex";
	/*.................................................................................................................*/
	public abstract String getProgramName();
	/*.................................................................................................................*

	public boolean getExportOptions(CharacterData data, boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), getName(), buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		String helpString = "If you check \"duplicate taxon sets\", Mesquite will produce two copies of each taxon set.  This will allow you to easily create BEAUTi both a MRCA Prior and a "
				+ "Sampled Ancestors MRCA Prior for each taxon set.  As of 25 March 2023 BEAUTi required manual creation of a second taxon set to create the second prior.";
		exportDialog.appendToHelpString(helpString);
		Checkbox duplicateTaxonSetsCheckBox = exportDialog.addCheckBox("duplicate taxon sets", duplicateTaxonSets);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		if (ok) {
			duplicateTaxonSets = duplicateTaxonSetsCheckBox.getState();
			storePreferences();
		}

		exportDialog.dispose();
		return ok;
	}		/*.................................................................................................................*/
	String getTaxonName(Taxa taxa, int it){
		return StringUtil.simplifyIfNeededForOutput(taxa.getTaxonName(it),true);
	}
	/*.................................................................................................................*/
	String nexusStringForSpecsSet(TaxaSelectionSet taxaSet, Taxa taxa, MesquiteFile file, boolean isCurrent){
			String s= "";
			if (taxaSet!=null && (taxaSet.getFile()==file || (taxaSet.getFile()==null && taxa.getFile()==file))) {
				String sT = "";
				int continuing = 0;
				int lastWritten = -1;
				for (int ic=0; ic<taxa.getNumTaxa(); ic++) {
					if (taxaSet.isBitOn(ic)) {
						sT += " " + getTaxonName(taxa,ic);
					}
				}
				if (!StringUtil.blank(sT)) {
					s+= "\tTAXSET " ;
					String set1 = s;
					set1+= StringUtil.simplifyIfNeededForOutput(taxaSet.getName(),true) + " ";
					/*if (file.getProject().getNumberTaxas()>1) {
						set1+= " (TAXA = " +  StringUtil.tokenize(taxa.getName()) + ")";
					}
					*/
					set1+= " = "+  sT + ";" + StringUtil.lineEnding();
					s=set1;
				}
			}
			return s;
   	}
		/*.................................................................................................................*/
	 String getLocalNexusCommands(MesquiteFile file, String blockName){ 
		String s= "";
		String specSet ="";
		for (int ids = 0; ids<file.getProject().getNumberTaxas(); ids++) {

			Taxa taxa =  file.getProject().getTaxa(ids);
			if (taxa.getFile() == file){
				int numSets = taxa.getNumSpecsSets(TaxaSelectionSet.class);
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaSelectionSet.class);
				if (ssv!=null){
					TaxaSelectionSet ms = (TaxaSelectionSet)taxa.getCurrentSpecsSet(TaxaSelectionSet.class);
					if (ms!=null && (ms.getNexusBlockStored()==null || blockName.equalsIgnoreCase(ms.getNexusBlockStored()))) {
						ms.setNexusBlockStored(blockName);
						ms.setName("UNTITLED");
						specSet = nexusStringForSpecsSet(ms, taxa, file, true);
						s += specSet;
					}


					for (int ims = 0; ims<numSets; ims++) {
						s += nexusStringForSpecsSet((TaxaSelectionSet)taxa.getSpecsSet(ims, TaxaSelectionSet.class), taxa, file, false);
						s += specSet;
					}
				}
			}

		}
		return s;
	}
/*.................................................................................................................*/
	 String getSetsBlock(MesquiteFile file){
		String contents = getLocalNexusCommands(file, "SETS");
		if (StringUtil.blank(contents))
			return null;
		String blocks="BEGIN SETS;" + StringUtil.lineEnding()+ contents;
		blocks += "END;" + StringUtil.lineEnding();
		return blocks;
	}

	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");
		CategoricalData data = (CategoricalData)getProject().chooseData(containerOfModule(), file, null, CategoricalState.class, "Select data to export");
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		MesquiteString dir = new MesquiteString();
		MesquiteString fn = new MesquiteString();
		String suggested = fileName;
		if (file !=null)
			suggested = file.getFileName();
		MesquiteFile f;
		loadPreferences();
	/*	if (!usePrevious){
			if (!getExportOptions(data, data.anySelected(), data.getTaxa().anySelected()))
				return false;
		}
		*/
		addendum = getSetsBlock(file);

		
		String path = getPathForExport(arguments, suggested, dir, fn);
		if (path != null) {
			f = MesquiteFile.newFile(dir.getValue(), fn.getValue());
			if (f !=null){
				f.useSimplifiedNexus = true;
				f.useDataBlocks = useData;
				f.ambiguityToMissing = convertAmbiguities;
				f.simplifyNames =true;
				f.openWriting(true);
				f.writeLine("#NEXUS" + StringUtil.lineEnding());
				if (!useData)
					f.writeLine(((TaxaManager)findElementManager(Taxa.class)).getTaxaBlock(data.getTaxa(), null, file));
				data.getMatrixManager().writeCharactersBlock(data, null, f, null);
				if (addendum != null)
					f.writeLine(addendum);
				f.closeWriting();
				return true;
			}
		}
		return false;
	}



}


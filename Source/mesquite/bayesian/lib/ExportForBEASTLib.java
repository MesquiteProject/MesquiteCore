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
		 if ("defaultSearchString".equalsIgnoreCase(tag))
			 defaultSearchString = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "defaultSearchString", defaultSearchString);  
		return buffer.toString();
	}
	/*.................................................................................................................*
	public  void actionPerformed(ActionEvent e) {
		 if (e.getActionCommand().equalsIgnoreCase("defSearch")) {
			String temp = MesquiteString.queryString(containerOfModule(), "Default Search String", "Default string setting search parameters for MCMC run", defaultSearchString, 5);
			if (temp != null){
				defaultSearchString = temp;
				storePreferences();
			}
		}
	}


	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	protected boolean convertAmbiguities = false;
	protected boolean simplifyNames = true;
	protected boolean useData = true;
	protected String addendum = "";
	protected String fileName = "untitled.nex";
	/*.................................................................................................................*/
	public abstract String getProgramName();
	/*.................................................................................................................*/
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
						if (continuing == 0) {
							sT += " " + getTaxonName(taxa,ic);
							lastWritten = ic;
							continuing = 1;
						}
						else if (continuing == 1) {
							sT += " - ";
							continuing = 2;
						}
					}
					else if (continuing>0) {
						if (lastWritten != ic-1) {
							sT += " " + getTaxonName(taxa,ic);
							lastWritten = ic-1;
						}
						else
							lastWritten = -1;
						continuing = 0;
					}

				}
				if (continuing>1)
					sT += " " + getTaxonName(taxa, taxa.getNumTaxa()-1);
				if (!StringUtil.blank(sT)) {
					s+= "\tTAXSET " ;
					if (isCurrent)
						s += "* ";
					s+= StringUtil.tokenize(taxaSet.getName()) + " ";
					if (file.getProject().getNumberTaxas()>1)
						s+= " (TAXA = " +  StringUtil.tokenize(taxa.getName()) + ")";
					s+= " = "+  sT + ";" + StringUtil.lineEnding();
				}
			}
			return s;
   	}
		/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		String s= "";
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
						s += nexusStringForSpecsSet(ms, taxa, file, true);
					}


					for (int ims = 0; ims<numSets; ims++) {
						s += nexusStringForSpecsSet((TaxaSelectionSet)taxa.getSpecsSet(ims, TaxaSelectionSet.class), taxa, file, false);
					}
				}
			}

		}
		return s;
	}
/*.................................................................................................................*/
	public String getSetsBlock(MesquiteFile file){
		String contents = getNexusCommands(file, "SETS");
		if (StringUtil.blank(contents))
			return null;
		String blocks="BEGIN SETS;" + StringUtil.lineEnding()+ contents;
		blocks += "END;" + StringUtil.lineEnding();
		return blocks;
	}

	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
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
		addendum = getSetsBlock(file);
		
		
		String path = getPathForExport(arguments, suggested, dir, fn);
		if (path != null) {
			f = MesquiteFile.newFile(dir.getValue(), fn.getValue());
			if (f !=null){
				f.useSimplifiedNexus = true;
				f.useDataBlocks = useData;
				f.ambiguityToMissing = convertAmbiguities;
				f.simplifyNames =simplifyNames;
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


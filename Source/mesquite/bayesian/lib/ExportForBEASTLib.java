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

import java.awt.Checkbox;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.lib.Arguments;
import mesquite.lib.Bits;
import mesquite.lib.*;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.Parser;
import mesquite.lib.SpecsSetVector;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.CharactersGroup;
import mesquite.lib.characters.CodonPositionsSet;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaSelectionSet;



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

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("exportCharPartition".equalsIgnoreCase(tag))
			exportCharPartition = MesquiteBoolean.fromTrueFalseString(content);
		super.processSingleXMLPreference(tag, content);
		preferencesSet = true;
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "exportCharPartition", exportCharPartition);  
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
	protected boolean exportCharPartition = true;
	/*.................................................................................................................*/
	public abstract String getProgramName();
	/*.................................................................................................................*/

	public boolean getExportOptions(CharacterData data, boolean dataSelected, boolean taxaSelected){
		//THIS IS ONLY CALLED if character groups present!  (see below)
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), getName(), buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
//		String helpString = "";
//		exportDialog.appendToHelpString(helpString);
		Checkbox exportCharPartitionCheckBox = exportDialog.addCheckBox("include character partition in file as CHARSETs", exportCharPartition);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		if (ok) {
			exportCharPartition = exportCharPartitionCheckBox.getState();
			storePreferences();
		}

		exportDialog.dispose();
		return ok;
	}		
	
	/*.................................................................................................................*/
	String getTaxonName(Taxa taxa, int it){
		return StringUtil.simplifyIfNeededForOutput(taxa.getTaxonName(it),true);
	}

	/*.................................................................................................................*/
	String nexusStringForTaxSet(TaxaSelectionSet taxaSet, Taxa taxa, MesquiteFile file, boolean isCurrent){
		String s= "";
		if (taxaSet!=null && (taxaSet.getFile()==file || (taxaSet.getFile()==null && taxa.getFile()==file))) {
			String sT = "";
			for (int ic=0; ic<taxa.getNumTaxa(); ic++) {
				if (taxaSet.isBitOn(ic)) {
					sT += " " + getTaxonName(taxa,ic);
				}
			}
			if (!StringUtil.blank(sT)) {
				s+= "\tTAXSET " ;
				String set1 = s;
				set1+= StringUtil.simplifyIfNeededForOutput(taxaSet.getName(),true) + " ";
				if (file.getProject().getNumberTaxas()>1) {
					set1+= " (TAXA = " +  StringUtil.tokenize(taxa.getName()) + ")";
				}

				set1+= " = "+  sT + ";" + StringUtil.lineEnding();
				s=set1;
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	StringBuffer conflictingTaxsets(MesquiteFile file){ 
		StringBuffer sb = new StringBuffer(0);
		boolean conflict = false;
		for (int ids = 0; ids<file.getProject().getNumberTaxas(); ids++) {
			Taxa taxa =  file.getProject().getTaxa(ids);
			if (taxa.getFile() == file){
				int numSets = taxa.getNumSpecsSets(TaxaSelectionSet.class);
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaSelectionSet.class);
				if (ssv!=null){
					TaxaSelectionSet ms = (TaxaSelectionSet)taxa.getCurrentSpecsSet(TaxaSelectionSet.class);
					if (ms!=null && (ms.getNexusBlockStored()==null || "SETS".equalsIgnoreCase(ms.getNexusBlockStored()))) {
						// compare ms to rest
						for (int ims = 0; ims<numSets; ims++) {						// compare ms to rest
							TaxaSelectionSet ms2 = (TaxaSelectionSet)taxa.getSpecsSet(ims, TaxaSelectionSet.class);
							Bits bits = ms.getBits();
							Bits bits2 = ms2.getBits();
							if (!Bits.compatible(bits,bits2)) {
								conflict=true;
								sb.append("  Default taxon set conflicts with taxon set \"" + ms2.getName()+"\"\n");
							}
						}
					}

					for (int ims = 0; ims<numSets; ims++) {
						ms = (TaxaSelectionSet)taxa.getSpecsSet(ims, TaxaSelectionSet.class);
						for (int jms = 0; jms<numSets; jms++) {						// compare ms to rest
							if (ims!=jms) {
								TaxaSelectionSet ms2 = (TaxaSelectionSet)taxa.getSpecsSet(jms, TaxaSelectionSet.class);
								Bits bits = ms.getBits();
								Bits bits2 = ms2.getBits();
								if (!Bits.compatible(bits,bits2)) {
									conflict=true;
									sb.append("  Taxon set \""+ms.getName()+"\" conflicts with taxon set \"" + ms2.getName()+"\"\n");
								}
							}
						}
					}
				}
			}

		}
		if (conflict)
			return sb;
		else
			return null;
	}


	/*.................................................................................................................*/
	
	/*.................................................................................................................*/

	private String getCHARSETS(CharacterData data){
		boolean writeStandardPartition = false;
		CharactersGroup[] parts =null;
		CharacterPartition characterPartition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		if (characterPartition!=null) {
			parts = characterPartition.getGroups();
			writeStandardPartition = parts!=null;
		}

		boolean writeCodPosPartition = false;
		if (data instanceof DNAData)
			writeCodPosPartition = ((DNAData)data).someCoding();

		String charSetList = "";
		if (writeStandardPartition && exportCharPartition) {
			int numCharSets = 0;
			for (int i=0; i<parts.length; i++) {
				String q = ListableVector.getListOfMatches((Listable[])characterPartition.getProperties(), parts[i], CharacterStates.toExternal(0));
				if (q != null) {
					charSetList +=  "\n\tcharset " + StringUtil.simplifyIfNeededForOutput(parts[i].getName(),true) + " = " + q + ";";
					numCharSets++;
				}
			}
		} else if (writeCodPosPartition) {  // only do this if there are no defined partitions
			//codon positions if nucleotide
			//String names = "";
			CodonPositionsSet codSet = (CodonPositionsSet)data.getCurrentSpecsSet(CodonPositionsSet.class);
			for (int iw = 0; iw<4; iw++){
				String locs = codSet.getListOfMatches(iw);
				if (!StringUtil.blank(locs)) {
					String charSetName = "";
					if (iw==0) 
						charSetName = StringUtil.tokenize("nonCoding");
					else 
						charSetName = StringUtil.tokenize("codonPos" + iw);			
					charSetList += "\n\tcharset " + charSetName + " = " +  locs + ";";
				}
			}
			
		}

		return charSetList;
	}


	/*.................................................................................................................*/
	String getLocalNexusCommands(MesquiteFile file, String blockName, CategoricalData data){ 
		String s= "";
		String specSet ="";
		Taxa taxa = data.getTaxa();

		int numSets = taxa.getNumSpecsSets(TaxaSelectionSet.class);
		SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaSelectionSet.class);
		if (ssv!=null){
			TaxaSelectionSet ms = (TaxaSelectionSet)taxa.getCurrentSpecsSet(TaxaSelectionSet.class);
			if (ms!=null && (ms.getNexusBlockStored()==null || blockName.equalsIgnoreCase(ms.getNexusBlockStored()))) {
				ms.setNexusBlockStored(blockName);
				ms.setName("UNTITLED");
				specSet = nexusStringForTaxSet(ms, taxa, file, true);
				s += specSet;
			}


			for (int ims = 0; ims<numSets; ims++) {
				s += nexusStringForTaxSet((TaxaSelectionSet)taxa.getSpecsSet(ims, TaxaSelectionSet.class), taxa, file, false);
				s += specSet;
			}
		}
		s+=getCHARSETS(data);
		return s;
	}
	/*.................................................................................................................*/
	String getSetsBlock(MesquiteFile file, CategoricalData data){
		String contents = getLocalNexusCommands(file, "SETS", data);
		if (StringUtil.blank(contents))
			return null;
		String blocks="BEGIN SETS;" + StringUtil.lineEnding()+ contents;
		
		blocks += "\nEND;" + StringUtil.lineEnding();
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
		
		
		StringBuffer conflictingTaxSets = conflictingTaxsets(file);
		if (conflictingTaxSets!=null) {
			logln("\nConflicting taxon sets:");
			logln(conflictingTaxSets.toString());
			logln("");
			showLogWindow(true);
			MesquiteMessage.discreetNotifyUser("WARNING: Incompatible (partly overlapping) taxon sets (see log window). All taxon sets must be subsets of others or contain no taxa in common. The resulting file will not be importable into BEAUTi.");
			return false;
		}
		
/*		Taxa taxa = data.getTaxa();
		if (taxa.taxonNamesTooComplexForExternalProgam(ExternalProgramUtil.externalBEAST)) {
			MesquiteMessage.discreetNotifyUser("WARNING: Taxon names have characters that may prevent BEAST from running. "
					+ "It is strongly recommended that you select all of the taxon names and choose Matrix > Taxon Names > Simplify Taxon Names");
		}
		*/
		
		MesquiteString dir = new MesquiteString();
		MesquiteString fn = new MesquiteString();
		String suggested = fileName;
		if (file !=null)
			suggested = file.getFileName();
		MesquiteFile f;
		loadPreferences();
		if (!usePrevious && data.hasCharacterGroups()){   
			if (!getExportOptions(data, data.anySelected(), data.getTaxa().anySelected()))
				return false;
		}
		addendum = getSetsBlock(file, data );


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


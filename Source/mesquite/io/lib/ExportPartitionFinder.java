package mesquite.io.lib;

import java.awt.Checkbox;

import mesquite.assoc.lib.AssociationSource;
import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.lib.Arguments;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.IntegerArray;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteStringBuffer;
import mesquite.lib.MesquiteThread;
import mesquite.lib.NumberArray;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.TaxonNamer;
import mesquite.lib.TreeVector;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.CharactersGroup;
import mesquite.lib.characters.CodonPositionsSet;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.io.lib.*;



public abstract class ExportPartitionFinder extends FileInterpreterI {
	
	protected int taxonNameLength = 500;

	public boolean branchesLinked;
	public boolean separateCodPos;
	public String models;
	public int modelSelection;
	public int schemes;
	

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true; 
	}

	public boolean isPrerelease(){
		return false;
	}
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}

	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "phy";
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean isProtein() {  
		return false;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return (project.getNumberCharMatricesVisible(CategoricalState.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return CategoricalData.class.isAssignableFrom(dataClass);
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}
	/*.................................................................................................................*/
	public abstract CharacterData findDataToExport(MesquiteFile file, String arguments);

	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}

	protected TaxonNamer taxonNamer = null;

	public void setTaxonNamer(TaxonNamer namer) {
		this.taxonNamer = namer;
	}

	public static final int AIC = 0;
	public static final int AICc = 1;
	public static final int BIC = 2;
	
	public static final int ALL = 0;
	public static final int GREEDY = 1;
	public static final int RCLUSTER = 2;
	public static final int HCLUSTER = 3;
	
	/*.................................................................................................................*/
	boolean hasSomeCodPos(Listable[] listArray, Object obj, NumberArray numberArray){
		for (int i=0; i<listArray.length && i<numberArray.getNumParts(); i++) {
			if (listArray[i]==obj && (numberArray.getInt(i)>=1 && numberArray.getInt(i)<=3)) {
				return true;
			}

		}
		return false;
	}
	boolean hasSomeCodPos(NumberArray numberArray){
		for (int i=0;  i<numberArray.getNumParts(); i++) {
			if ((numberArray.getInt(i)>=1 && numberArray.getInt(i)<=3)) {
				return true;
			}

		}
		return false;
	}
	/*.................................................................................................................*/
	public String getPartitionList(CharacterData data, CharacterPartition charPartition, boolean separateCodePos){
		boolean subdivideByCodPos = false;
		CodonPositionsSet codPosSet=null;
		if (separateCodePos && data instanceof DNAData) {
			codPosSet = (CodonPositionsSet)data.getCurrentSpecsSet(CodonPositionsSet.class);
			if (codPosSet!=null)
				subdivideByCodPos=true;
		}

		StringBuffer sb = new StringBuffer();
		if (charPartition == null){
			boolean hasCodPos = subdivideByCodPos && hasSomeCodPos(codPosSet.getNumberArray());
			if (!separateCodePos || !hasCodPos)
				return null;
			String s = "";
			String q = IntegerArray.getListOfMatches(codPosSet.getNumberArray(), 0,1);  //noncoding
			if (q != null) {
				sb.append(s+"nonCoding" + " = ");
				sb.append(q + ";\n");
			}
			for (int codpos = 1; codpos<=3; codpos++) {
				q = IntegerArray.getListOfMatches(codPosSet.getNumberArray(), codpos,1);
				if (q != null) {
					sb.append(s+"pos"+codpos + " = ");
					sb.append(q + ";\n");
				}
			}
		}
		else {
		CharactersGroup[] parts = charPartition.getGroups();
		if (parts!=null)
			for (int i=0; i<parts.length; i++) {
				String s = parts[i].getName();
				s = StringUtil.cleanseStringOfFancyChars(s, false, true);
				s = StringUtil.blanksToUnderline(s);
				String q = null;
				Listable[] partition = (Listable[])charPartition.getProperties();
				if (!writeExcludedCharacters)
					partition = data.removeExcludedFromListable(partition);
				boolean hasCodPos = subdivideByCodPos && hasSomeCodPos(partition, parts[i], codPosSet.getNumberArray());

				if (subdivideByCodPos && hasCodPos) {
					q = ListableVector.getListOfMatches(partition, parts[i], codPosSet.getNumberArray(), 0,1, true);  //noncoding
					if (q != null) {
						sb.append(s+"_nonCoding" + " = ");
						sb.append(q + ";\n");
					}
					for (int codpos = 1; codpos<=3; codpos++) {
						q = ListableVector.getListOfMatches(partition, parts[i], codPosSet.getNumberArray(), codpos,1, true);
						if (q != null) {
							sb.append(s+"_pos"+codpos + " = ");
							sb.append(q + ";\n");
						}
					}
				}
				else {
					q = ListableVector.getListOfMatches(partition, parts[i], 1, false);
					if (q != null) {
						sb.append(s + " = ");
						sb.append(q + ";\n");
					}
				}
			}
		}
		return sb.toString();
	}

	/*.................................................................................................................*/
	public String getPartitionFinderCFGText(CharacterData data, CharacterPartition partition, String fileName){
		StringBuffer sb = new StringBuffer();

		sb.append("## ALIGNMENT FILE ##\n");
		sb.append("alignment = "+ fileName+";\n\n");

		sb.append("## BRANCHLENGTHS: linked | unlinked ##\n");
		if (branchesLinked)
			sb.append("branchlengths = linked;\n\n");
		else
			sb.append("branchlengths = unlinked;\n\n");

		if (isProtein())
			sb.append("## MODELS OF EVOLUTION for PartitionFinderProtein: all_protein | <list> ##\n");
		else 
			sb.append("## MODELS OF EVOLUTION for PartitionFinder: all | mrbayes | beast | <list> ##\n");
		sb.append("models = "+ models+";\n\n");

		sb.append("## MODEL SELECTION: AIC | AICc | BIC ##\n");
		switch (modelSelection) {
		case AIC: 
			sb.append("model_selection = AIC;\n\n");
			break;
		case AICc: 
			sb.append("model_selection = AICc;\n\n");
			break;
		case BIC: 
			sb.append("model_selection = BIC;\n\n");
			break;
		default:
			sb.append("model_selection = BIC;\n\n");
			break;
		}

		sb.append("## DATA BLOCKS ##\n");
		sb.append("[data_blocks]\n");
		String s = getPartitionList(data, partition, separateCodPos);
		sb.append(s);
		sb.append("\n");


		sb.append("## SCHEMES, search: all | greedy | rcluster | hcluster | user ##\n");
		sb.append("[schemes]\n");
		switch (schemes) {
		case ALL: 
			sb.append("search = all;\n\n");
			break;
		case GREEDY: 
			sb.append("search = greedy;\n\n");
			break;
		case RCLUSTER: 
			sb.append("search = rcluster;\n\n");
			break;
		case HCLUSTER: 
			sb.append("search = hcluster;\n\n");
			break;
		default:
			sb.append("search = greedy;\n\n");
			break;
		}


		return sb.toString();
	}
	/*.................................................................................................................*/
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, MesquiteStringBuffer outputBuffer){
		data.statesIntoStringBuffer(ic, it, outputBuffer, false);
	}
	/*.................................................................................................................*/
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		PartitionFinderDialog exportDialog = new PartitionFinderDialog(this,containerOfModule(), "Export PartitionFinder Options", buttonPressed,isProtein());
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		
		exportDialog.dispose();
		models = exportDialog.getModels();
		branchesLinked = exportDialog.getLinked();
		separateCodPos = exportDialog.getSeparateCodPos();
		modelSelection = exportDialog.getModelSelection();
		writeExcludedCharacters = exportDialog.getWriteExcluded();
		schemes = exportDialog.getScheme();

		
		
		return ok;
	}	

	/*.................................................................................................................*/
	public void exportBlock(Taxa taxa, CharacterData data, MesquiteStringBuffer outputBuffer, int startChar, int endChar) { 
		int numTaxa = taxa.getNumTaxa();
		int maxNameLength = taxa.getLongestTaxonNameLength()+1;
		int numChars = data.getNumChars();
		int counter;
		String pad = "          ";
		while (pad.length() < maxNameLength)
			pad += "  ";

		for (int it = 0; it<numTaxa; it++){
			if ((!writeOnlySelectedTaxa || taxa.getSelected(it)) && (writeTaxaWithAllMissing || data.hasDataForTaxon(it))){
				if (startChar==0) {   // first block
					String name = "";
					if (taxonNamer!=null)
						name = taxonNamer.getNameToUse(taxa,it)+pad;
					else
						name = (taxa.getTaxonName(it)+ pad);
					name = name.substring(0,maxNameLength);
					name = StringUtil.cleanseStringOfFancyChars(name, false, true);
					name = StringUtil.underlineToBlanks(name);
					name = StringUtil.blanksToUnderline(StringUtil.stripTrailingWhitespace(name));

					outputBuffer.append(name);
					//	if (taxonNameLength>name.length())
					for (int i=0;i<maxNameLength-name.length()+1; i++)
						outputBuffer.append(" ");
				}
				//outputBuffer.append(" ");
				counter = startChar;
				for (int ic = startChar; ic<numChars; ic++) {
					if ((!writeOnlySelectedData || (data.getSelected(ic))) && (writeExcludedCharacters || data.isCurrentlyIncluded(ic))){
						long currentSize = outputBuffer.length();
						appendPhylipStateToBuffer(data, ic, it, outputBuffer);
						if (outputBuffer.length()-currentSize>1) {
							alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
							return;
						}
						counter++;
						if (counter>endChar)
							break;
					}
				}
				outputBuffer.append(getLineEnding());
			}
		}
	}

	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		writeTaxaWithAllMissing = false;
		
		CharacterData data = findDataToExport(file, arguments);
		Taxa t = null;
		if (data != null)
			t = data.getTaxa();
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		CharacterPartition partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		Taxa taxa = data.getTaxa();
		boolean dataAnySelected = false;
		if (data != null)
			dataAnySelected =data.anySelected();
		if (!MesquiteThread.isScripting())
			if (!getExportOptions(dataAnySelected, taxa.anySelected()))
				return false;
		if (partition==null && !separateCodPos) {
			discreetAlert("The data cannot be exported because partitions (character groups) are not assigned, and use of codon positions is not requested.");
			return false;
		}

		int numTaxa = taxa.getNumTaxa();

		int numTaxaWrite;
		int countTaxa = 0;
		for (int it = 0; it<numTaxa; it++)
			if ((!writeOnlySelectedTaxa || taxa.getSelected(it)) && (writeTaxaWithAllMissing || data.hasDataForTaxon(it)))
				countTaxa++;
		numTaxaWrite = countTaxa;

		int numChars = 0;
		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(numTaxa*(20L + numChars));


		if (data != null){
			numChars = data.getNumChars();
			outputBuffer.append(Integer.toString(numTaxaWrite)+" ");
			if (!writeExcludedCharacters)
				outputBuffer.append(Integer.toString(data.getNumCharsIncluded(this.writeOnlySelectedData))+this.getLineEnding());		
			else {
				int numCharWrite = data.numberSelected(this.writeOnlySelectedData); 
				outputBuffer.append(Integer.toString(numCharWrite)+this.getLineEnding());	
			}
			int blockSize=50;

			exportBlock(taxa, data, outputBuffer, 0, numChars);
		}

		saveExportedFileWithExtension(outputBuffer, arguments, "phy");
		

		String cfgString = getPartitionFinderCFGText(data, partition, getExportedFileName());
		String cfgFilePath = getExportedFilePath();
		cfgFilePath=StringUtil.getAllButLastItem(cfgFilePath, MesquiteFile.fileSeparator)+MesquiteFile.fileSeparator+"partition_finder.cfg";
		MesquiteFile.putFileContents(cfgFilePath, cfgString, true);

		
		return true;
	}

	public String getName() {
		return "Export for " + getProgramName();
	}
	public String getProgramName() {
		return "PartitionFinder";
	}
	public String getExplanation() {
		return "Exports a .phy file containing your data prepared for " + getProgramName() +" and creates a .cfg file.";
	}
}

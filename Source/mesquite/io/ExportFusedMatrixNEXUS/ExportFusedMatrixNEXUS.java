/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.ExportFusedMatrixNEXUS;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;
import mesquite.basic.ManageTaxaPartitions.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;



public class ExportFusedMatrixNEXUS extends FileInterpreterI {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(AssociationSource.class, getName() + "  needs information to indicate how taxa in different blocks are associated.",
		"The source of information as to how taxa taxa in different blocks are associated is arranged on export");
	}
	/*.................................................................................................................*/
	AssociationSource associationTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
	}

	public boolean isPrerelease(){
		return false;
	}
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "nex";
	}
	/*.................................................................................................................*/
	/** Returns wether this interpreter uses a flavour of NEXUS.  Used only to determine whether or not to add "nex" as a file extension to imported files (if already NEXUS, doesn't).**/
	public boolean usesNEXUSflavor(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
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
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}


	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	boolean convertAmbiguities = false;
	boolean useData = true;
	String addendum = "";
	String fileName = "untitled.nex";
	boolean permitMixed = false;
	boolean generateMBBlock = true;
	String lineEnding;
	boolean simplifyNames = false;
	
	boolean removeExcluded = false;

	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export Fused Matrix", buttonPressed);
		exportDialog.setSuppressLineEndQuery(false);
		exportDialog.setDefaultButton(null);
		exportDialog.addLabel("Fusing matrices into a single matrix, and exporting");
		if (getProject().getNumberTaxas()>1)
			exportDialog.addLargeOrSmallTextLabel("This exporter uses associations between taxa to be able to blend matrices from different taxa blocks.  Ensure such associations are set up (through Taxa&Trees menu) before attempting to export.");
		exportDialog.addHorizontalLine(1);
		Checkbox permitMixedMatrices = exportDialog.addCheckBox("Permit fused matrix with mixed data types (e.g., DNA, protein, standard)", permitMixed);
		Checkbox removeExcludedBox = exportDialog.addCheckBox("Remove excluded characters", removeExcluded);
		Checkbox generateMB = exportDialog.addCheckBox("Generate MrBayes block", generateMBBlock);
		Checkbox simplifyNamesCheckBox = exportDialog.addCheckBox("Simplify names as required for MrBayes", simplifyNames);
		//		Checkbox convertToMissing = exportDialog.addCheckBox("convert partial ambiguities to missing", convertAmbiguities);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		//		convertAmbiguities = convertToMissing.getState();
		permitMixed = permitMixedMatrices.getState();
		generateMBBlock = generateMB.getState();
		removeExcluded = removeExcludedBox.getState();
		simplifyNames = simplifyNamesCheckBox.getState();

		exportDialog.dispose();
		lineEnding = getLineEnding();
		return ok;
	}	

	/*.................................................................................................................*/
	boolean heterogeneous = false;
	Class previousClass = null;
	boolean firstNEXUSpartition = true;

	int addDataTypes(Taxa taxa, StringBuffer buffer, StringBuffer MBpartitionBuffer,  StringBuffer NEXUSpartitionBuffer, StringBuffer NEXUSCharSetBuffer, Vector datas, int totNumChars, Class dataSuperclass){
		int numMatrices = getProject().getNumberCharMatrices(null, taxa, dataSuperclass, true);
		for (int iM = 0; iM < numMatrices; iM++){
			CharacterData data = getProject().getCharacterMatrixVisible( taxa, iM, dataSuperclass);
			if (data != null) { 
				if (needsComma)
					buffer.append(',');
				needsComma = true;
				if (data instanceof RNAData){
					if (previousClass != null && previousClass != RNAData.class)
						heterogeneous = true;
					previousClass = RNAData.class;
					buffer.append(" rna");
				}
				else if (data instanceof DNAData){
					if (previousClass != null && previousClass != DNAData.class)
						heterogeneous = true;
					previousClass = DNAData.class;
					buffer.append(" dna");
				}
				else if (data instanceof ProteinData){
					if (previousClass != null && previousClass != ProteinData.class)
						heterogeneous = true;
					previousClass = ProteinData.class;
					buffer.append(" protein");
				}
				else if (data instanceof CategoricalData){
					if (previousClass != null && previousClass != CategoricalData.class)
						heterogeneous = true;
					previousClass = CategoricalData.class;
					buffer.append(" standard");
				}
				else if (data instanceof ContinuousData){
					if (previousClass != null && previousClass != ContinuousData.class)
						heterogeneous = true;
					previousClass = ContinuousData.class;
					buffer.append(" continuous");
				}
				int numToAdd = data.getNumChars();
				if (removeExcluded)
					numToAdd = data.getNumCharsIncluded();
				int currentTotNumChars = totNumChars + numToAdd;

				buffer.append(": " + (totNumChars+1) + "-" + currentTotNumChars);

				if (simplifyNames)
					MBpartitionBuffer.append(lineEnding + "\tcharset " + StringUtil.simplifyIfNeededForOutput(data.getName(),true) + " = " + (totNumChars+1) + "-" + (currentTotNumChars) + ";");
				else
					MBpartitionBuffer.append(lineEnding + "\tcharset " + StringUtil.tokenize(data.getName()) + " = " + (totNumChars+1) + "-" + (currentTotNumChars) + ";");

				boolean writeCodPosPartition = false;
				if (data instanceof DNAData)
					writeCodPosPartition = ((DNAData)data).someCoding();
				if (writeCodPosPartition) {
					//codon positions if nucleotide
					String codPos = "";
					boolean[] include = null;
					if (removeExcluded)
						include = data.getBooleanArrayOfIncluded();

					CodonPositionsSet codSet = (CodonPositionsSet)data.getCurrentSpecsSet(CodonPositionsSet.class);
					for (int iw = 0; iw<4; iw++){
						String locs = codSet.getListOfMatches(iw, totNumChars,include);
						if (!StringUtil.blank(locs)) {
							String charSetName = "";
							if (iw==0) 
								charSetName = StringUtil.tokenize("nonCoding");
							else 
								charSetName = StringUtil.tokenize("codonPos" + iw);
							codPos += "\n\tcharset " + charSetName + " = " + locs + ";";
						}
					}
					//String codPos = ((DNAData)data).getCodonsAsNexusCharSets(totNumChars, data.getName(), numberCharSets, charSetList);
					if (!StringUtil.blank(codPos)) {
						MBpartitionBuffer.append(lineEnding + codPos + lineEnding);
					}			
				}


				NEXUSCharSetBuffer.append(lineEnding + "\tcharset " + StringUtil.tokenize(data.getName()) + " = " + (totNumChars+1) + "-" + (currentTotNumChars) + ";");
				if (!firstNEXUSpartition)
					NEXUSpartitionBuffer.append(", ");
				NEXUSpartitionBuffer.append(StringUtil.tokenize(data.getName()) + " : " + (totNumChars+1) + "-" + (currentTotNumChars) + " ");
				firstNEXUSpartition = false;
				datas.addElement(data);
				if (removeExcluded) 
					totNumChars += data.getNumCharsIncluded();
				else
					totNumChars += data.getNumChars();
				logln("Fused matrix will contain " + numToAdd + " characters from matrix " + data.getName());
				
			}
		}
		return totNumChars;
	}

	int getNumberQueuedCharMatrices(Vector datas, Taxa taxa, Class dataClass){
		int count=0;
		if (datas == null)
			return 0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if ((data.isUserVisible()) && !data.isDoomed() && (taxa == null || taxa == data.getTaxa()) && (dataClass==null || getProject().compatibleMatrix(dataClass, data)))
				count++;
		}
		return count;
	}
	CharacterData getQueuedCharMatrix(Vector datas, Taxa taxa, int j, Class dataClass){
		int count=0;
		for (int i=0; i<datas.size(); i++) { 
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);

			if ((data.isUserVisible()) && !data.isDoomed() && (taxa == null || taxa == data.getTaxa()) && (dataClass == null || getProject().compatibleMatrix(dataClass, data))) {
				if (count==j) {
					return data;
				}
				count++;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	void composeForTaxon(Vector datas, Taxon[] components, int iTaxaBlock, StringBuffer buffer, boolean master, Class dataSuperclass){
		if (!master && (components == null || components.length == 0)){ //no representatives in this taxa block; write gaps for those matrices
			Taxa taxa = getProject().getTaxa(iTaxaBlock);
			int numMatrices = getNumberQueuedCharMatrices(datas, taxa, dataSuperclass);
			for (int iM = 0; iM < numMatrices; iM++){
				CharacterData data = getQueuedCharMatrix(datas, taxa, iM, dataSuperclass);
				if (data != null){
					for (int ic=0; ic<data.getNumChars(); ic++)
						if (!removeExcluded || data.isCurrentlyIncluded(ic))
							buffer.append('-');
				}
			}
			return;
		}
		Taxon first = components[0];  //this will tell taxa block;
		Taxa taxa = first.getTaxa();
		int numMatrices = getNumberQueuedCharMatrices(datas, taxa, dataSuperclass);
		for (int iM = 0; iM < numMatrices; iM++){
			CharacterData data = getQueuedCharMatrix(datas, taxa, iM, dataSuperclass);
			CategoricalState cs = (CategoricalState)data.makeCharacterState();
			CategoricalData categData = (CategoricalData)data;
			if (data != null){
				for (int ic=0; ic<data.getNumChars(); ic++){
					if (!removeExcluded || data.isCurrentlyIncluded(ic)){
						if (components.length == 1){
							int it = components[0].getNumber();  
							if (heterogeneous && !permitMixed){
								buffer.append(CategoricalState.toNEXUSString(categData.getState(ic, it)));
							}
							else 
								data.statesIntoNEXUSStringBuffer(ic, it, buffer);
						}
						else {
							long fusedState = categData.getState(ic, components[0].getNumber());
							for (int itc = 1; itc < components.length; itc++){
								fusedState = fusedState | categData.getState(ic, components[itc].getNumber());
							}
							//CategoricalState.mergeStates(fusedState,categData.getState(ic, components[itc].getNumber()));
							if (heterogeneous && !permitMixed){
								buffer.append(CategoricalState.toNEXUSString(fusedState));
							}
							else {
								cs.setValue(fusedState);
								buffer.append(cs.toNEXUSString());
							}
						}
					}
				}
			} 
		}
	}
	/*.................................................................................................................*/
	void composeForMasterTaxon(Vector datas, Taxa masterTaxa, int it, Taxon[][] components, StringBuffer buffer, Class dataSuperclass){
		String taxonName = masterTaxa.getTaxonName(it);
		if (simplifyNames)
			buffer.append(StringUtil.simplifyIfNeededForOutput(taxonName,true)+ "   ");
		else
			buffer.append(StringUtil.tokenize(taxonName) + "   ");
		composeForTaxon(datas, new Taxon[]{masterTaxa.getTaxon(it)}, -1, buffer, true, dataSuperclass);
		if (components != null)
			for (int iTaxaBlock = 0; iTaxaBlock<components.length; iTaxaBlock++){
				if (iTaxaBlock != getProject().getTaxaNumber(masterTaxa))
					composeForTaxon(datas, components[iTaxaBlock], iTaxaBlock, buffer, false, dataSuperclass);
			}
	}
	/*.................................................................................................................*/
	boolean needsComma= false;
	String lsetStart(Class targetClass, Vector datas){
		boolean[] matching = new boolean[datas.size()];
		String s = "";
		int count =0;
		boolean first = true;
		for (int i=0; i<datas.size(); i++){
			matching[i] =(datas.elementAt(i).getClass() == targetClass);  // a little dicey givenpossibility of subclasses
			if (matching[i])
				count++;
		}
		if (count>0){
			s += (lineEnding + "\tlset applyto = (");
			for (int i=0; i<matching.length; i++){
				if (matching[i]){
					if (!first)
						s += (", ");
					first = false;
					s +=(Integer.toString(i+1));
				}
			}
			s +=(") ");
		}
		return s;
	}
	/*.................................................................................................................*/
	void fillMatrix(MesquiteFile file, Taxa masterTaxa, StringBuffer buffer, StringBuffer mrBayesBlockBuffer, Class dataSuperclass){
		if (masterTaxa == null || buffer==null)
			return;
		if (mrBayesBlockBuffer == null)
			mrBayesBlockBuffer = new StringBuffer(); //just to avoid crashes, & absorb "appends"
		MesquiteProject project = getProject();
		int numTaxaBlocks = project.getNumberTaxas();
		if (associationTask == null && numTaxaBlocks>1){
			associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
		}

		buffer.append("#NEXUS" + lineEnding + lineEnding + "begin data;" + lineEnding);
		StringBuffer[] taxaStrings = new StringBuffer[masterTaxa.getNumTaxa()];
		StringBuffer dataTypesBuffer = new StringBuffer();
		
		ManageTaxaPartitions manageTaxaPartitions = (ManageTaxaPartitions)findNearestColleagueWithDuty(mesquite.basic.ManageTaxaPartitions.ManageTaxaPartitions.class);
		StringBuffer labelsBuffer = null;
		if (manageTaxaPartitions!=null) {
			labelsBuffer = new StringBuffer(lineEnding + "BEGIN LABELS;"+lineEnding);
			String labels = manageTaxaPartitions.getNexusCommands(file, "LABELS");
			labelsBuffer.append(labels + lineEnding + "END;" + lineEnding);
		}
		
		StringBuffer NEXUSpartitionBuffer = new StringBuffer(lineEnding + "BEGIN SETS;" + lineEnding);
		NEXUSpartitionBuffer.append(lineEnding+ manageTaxaPartitions.getNexusCommands(file, "SETS"));
		NEXUSpartitionBuffer.append(lineEnding+ "\tCHARPARTITION * matrices = ");
		StringBuffer NEXUSCharSetBuffer = new StringBuffer();

		Vector datas = new Vector();
		for (int it=0; it< masterTaxa.getNumTaxa(); it++)
			taxaStrings[it] = new StringBuffer(100);

		Taxon[][][] components = new Taxon[masterTaxa.getNumTaxa()][project.getNumberTaxas()][];
		needsComma = false;
		heterogeneous = false;
		firstNEXUSpartition = true;
		previousClass = null;
		mrBayesBlockBuffer.append(lineEnding + "begin mrbayes;");
		int totNumChars =  addDataTypes(masterTaxa, dataTypesBuffer, mrBayesBlockBuffer, NEXUSpartitionBuffer, NEXUSCharSetBuffer, datas, 0, dataSuperclass);
		if (associationTask != null){

			for (int iTaxaBlock = 0; iTaxaBlock < project.getNumberTaxas(); iTaxaBlock++){
				Taxa aTaxa = project.getTaxa(iTaxaBlock);
				if (aTaxa != masterTaxa){
					TaxaAssociation assoc = associationTask.getAssociation(masterTaxa, aTaxa, 0);  //Todo: permit choice
					if (assoc != null){
						totNumChars = addDataTypes(assoc.getOtherTaxa(masterTaxa), dataTypesBuffer,mrBayesBlockBuffer,  NEXUSpartitionBuffer, NEXUSCharSetBuffer, datas, totNumChars, dataSuperclass);
						for (int it = 0; it<masterTaxa.getNumTaxa(); it++){
							components[it][iTaxaBlock] = assoc.getAssociates(masterTaxa.getTaxon(it));
						}
					}
				}
			}
		}
		mrBayesBlockBuffer.append(lineEnding + "\tpartition matrices = " + datas.size() + ": ");
		NEXUSpartitionBuffer.append(";");
		NEXUSpartitionBuffer.append(NEXUSCharSetBuffer);
		NEXUSpartitionBuffer.append(lineEnding + "END;"+lineEnding);

		for (int i=0; i<datas.size(); i++){
			if (i != 0)
				mrBayesBlockBuffer.append(", ");
			String name = ((CharacterData)datas.elementAt(i)).getName();
			if (simplifyNames)
				name = StringUtil.simplifyIfNeededForOutput(name,true);
			else
				name=StringUtil.tokenize(name);
			mrBayesBlockBuffer.append(name);
		}
		mrBayesBlockBuffer.append(";");
		mrBayesBlockBuffer.append(lineEnding +"\tset partition = matrices;");

		//RNA
		String s = lsetStart(RNAData.class, datas);
		if (s.length()>0){
			mrBayesBlockBuffer.append("\t");
			mrBayesBlockBuffer.append(s);
			mrBayesBlockBuffer.append(" nst=6 rates=invgamma;");
		}
		//DNA
		s = lsetStart(DNAData.class, datas);
		if (s != null && s.length()>0){
			mrBayesBlockBuffer.append("\t");
			mrBayesBlockBuffer.append(s);
			mrBayesBlockBuffer.append(" nst=6 rates=invgamma;");
		}

		int count =0;
		for (int i=0; i<datas.size(); i++){
			if (datas.elementAt(i).getClass() == ProteinData.class)  
				count++;
		}
		if (count > 0)
			mrBayesBlockBuffer.append(lineEnding + "\tprset aamodelpr=mixed;");


		mrBayesBlockBuffer.append(lineEnding + "\tunlink statefreq=(all) revmat=(all) shape=(all) pinvar=(all); " + lineEnding + "\tprset applyto=(all) ratepr=variable;" + lineEnding + "\tmcmcp ngen= 10000000 relburnin=yes burninfrac=0.5 printfreq=1000  samplefreq=1000 nchains=4 savebrlens=yes;" + lineEnding + "\tmcmc;" + lineEnding + "end;");


		buffer.append("dimensions ntax=" + masterTaxa.getNumTaxa() + " nchar=" + totNumChars + ";" + lineEnding);
		buffer.append("format datatype = ");
		if (heterogeneous){
			if (permitMixed)
				buffer.append("mixed(" + dataTypesBuffer + ")");
			else
				buffer.append("standard");
		}
		else if (previousClass == RNAData.class)
			buffer.append("rna");
		else if (previousClass == DNAData.class)
			buffer.append("dna");
		else if (previousClass == ProteinData.class)
			buffer.append("protein");
		else if (previousClass == CategoricalData.class)
			buffer.append("standard");
		else if (previousClass == ContinuousData.class)
			buffer.append("continuous");

		buffer.append(" gap = - missing =?;" + lineEnding + "matrix" + lineEnding);
		for (int it=0; it< masterTaxa.getNumTaxa(); it++)
			composeForMasterTaxon(datas, masterTaxa, it, components[it], taxaStrings[it], dataSuperclass);
		buffer.append(lineEnding);
		for (int i=0; i< masterTaxa.getNumTaxa(); i++){
			buffer.append(taxaStrings[i]);
			buffer.append(lineEnding);
			taxaStrings[i].setLength(0);
		}
		buffer.append(lineEnding + ";" + lineEnding + "end;" + lineEnding);
		if (labelsBuffer!=null)
			buffer.append(labelsBuffer);
		buffer.append(NEXUSpartitionBuffer);

		if (generateMBBlock)
			buffer.append(mrBayesBlockBuffer);
		datas.removeAllElements(); 
	}
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(false, false))
				return false;
		Taxa masterTaxa = (Taxa)getProject().chooseTaxa(containerOfModule(), "Select master block of taxa", false);
		StringBuffer buffer = new StringBuffer(500);
		StringBuffer mrBayesBlockBuffer = new StringBuffer();
		fillMatrix(file, masterTaxa, buffer, mrBayesBlockBuffer, CategoricalState.class);
		


		saveExportedFileWithExtension(buffer, arguments, "nex");
		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Fused Matrix Export (NEXUS)";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports NEXUS files with matrices fused." ;
	}
	/*.................................................................................................................*/


}



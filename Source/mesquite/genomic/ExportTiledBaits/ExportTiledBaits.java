package mesquite.genomic.ExportTiledBaits;

import java.awt.Checkbox;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.FileInterpreterI;

public class ExportTiledBaits extends FileInterpreterI {

//TODO:  warn about uncertainty
	
	
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		//	EmployeeNeed e = registerEmployeeNeed(VoucherInfoCoord.class, "Voucher information is needed for FASTA export for Genbank submissions.",
		//			"This is activated automatically when you choose this exporter.");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public void readFile(MesquiteProject mf, MesquiteFile mNF, String arguments) {

	}

	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;  
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
		return project.getNumberCharMatrices(DNAState.class) > 0;  //
	}

	/** returns whether module can export a character data matrix of the given type.  Should be overridden*/
	public boolean canExportData(Class dataClass){
		if (dataClass==null) return false;
		return ((DNAState.class).isAssignableFrom(dataClass)); 
	}

	protected int taxonNameLengthLimit() {
		return 50;
	}

	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
//	String fileName = "untitled.fas";
	int baitLength = 120;
	int tileAmount = 60;
	boolean getEndBaitIfShiftedEnough = false;
	int shiftThresholdForEndBait = 30;
//	boolean arbitrarilyResolveAmbiguity = true;
	boolean includeShortBaits = false;

	//	protected boolean buildFileName = false;

	/*.................................................................................................................*/
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export Tiled Baits", buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);

		IntegerField baitLengthField= exportDialog.addIntegerField("Bait length", baitLength, 8);
		IntegerField tileAmountField= exportDialog.addIntegerField("Tile amount", tileAmount, 8);
		Checkbox includeShortBaitsBox= exportDialog.addCheckBox("Include short baits", includeShortBaits);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		if (ok) {
			baitLength = baitLengthField.getValue();
			tileAmount = tileAmountField.getValue();
			includeShortBaits = includeShortBaitsBox.getState();
		}

		exportDialog.dispose();
		return ok;
	}	
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {  
		return "fas";
	}

	/*.................................................................................................................*/
	public String getFileName(Taxa taxa, int it, CharacterData data, int index, String identifierString) {
		String fileName = "";
		fileName=StringUtil.cleanseStringOfFancyChars(taxa.getName(it),false,true);

		fileName += ".fas";

		return fileName;
	}


	/*.................................................................................................................*/
	public String getSequenceName(DNAData data, Taxa taxa, int it) {
		String s = data.getName() + ": " + taxa.getTaxonName(it);
		return s;
	}
	/*.................................................................................................................*/
	public String getTiledBaitAsFasta(DNAData data, Taxa taxa, int it,  int icStart, int siteNumber, MesquiteInteger nextStart) {
		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));
		outputBuffer.append(">"+ getSequenceName(data,taxa, it) + " " + siteNumber + "-");
		StringBuffer dataBuffer = new StringBuffer(120);
		int count = 0;
		int ic =0;
		boolean nextStartSet = false;
		for (ic = icStart; ic<numChars && count<baitLength; ic++) {
			if (!data.isUnassigned(ic, it) && !data.isInapplicable(ic,it)) {
				if (data.isMultistateOrUncertainty(ic, it)) {  //TODO
				}
				else {
					data.statesIntoStringBuffer(ic, it, dataBuffer, false);
				}
				count++;
				if (count>=tileAmount && !nextStartSet) {
					nextStart.setValue(ic);
					nextStartSet = true;
				}
			}
		}
		if (!nextStartSet) // must mean we didn't actually have enough for the next tile
			nextStart.setValue(numChars);
		outputBuffer.append((siteNumber+count-1));
		outputBuffer.append(StringUtil.lineEnding());
		outputBuffer.append(dataBuffer);
		outputBuffer.append(StringUtil.lineEnding());
		if (count>=baitLength || includeShortBaits)
			return outputBuffer.toString();
		else
			return "";
	}
	/*.................................................................................................................*/
	public String getTiledBaitsAsFasta(DNAData data, Taxa taxa, int it) {
		if (data.hasDataForTaxon(it)) {
			int numTaxa = taxa.getNumTaxa();
			int numChars = data.getNumChars();
			StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));
			int numSites = data.numNotInapplicableNotUnassigned(it);
			int siteCounter=1;
			int count = 0;
			if (numSites>=baitLength || includeShortBaits) {
				int icStart = 0;
				MesquiteInteger nextStart = new MesquiteInteger(0);
				while (icStart < numChars) {  
					if (!(includeShortBaits || icStart<numChars+baitLength))
						break;
					if (!data.isUnassigned(icStart, it) && !data.isInapplicable(icStart,it)) {  //got our next one
						String bait = getTiledBaitAsFasta(data, taxa, it, icStart, siteCounter, nextStart);
						if (StringUtil.blank(bait))  // too short
							break;
						else
							outputBuffer.append(bait);
						icStart =nextStart.getValue();
						siteCounter +=tileAmount;
					}
					icStart++;
				}
				return outputBuffer.toString();
			}
		}
		return "";
	}
	/*.................................................................................................................*/
	public synchronized boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		//Arguments args = new Arguments(new Parser(arguments), true);
		//		boolean usePrevious = args.parameterExists("usePrevious");
		
		if (!MesquiteThread.isScripting())
			if (!getExportOptions(false, true))
				return false;
		 
		StringBuffer buffer = new StringBuffer(500);


		for (int taxaNumber=0; taxaNumber<getProject().getNumberTaxas(file); taxaNumber++) {
			Taxa taxa = (Taxa)getProject().getTaxa(file,taxaNumber);
			int numMatrices = getProject().getNumberCharMatrices(null, taxa, DNAState.class, true);
			for (int iM = 0; iM < numMatrices; iM++){
				DNAData data = (DNAData)getProject().getCharacterMatrixVisible(taxa, iM, DNAState.class);
				if (data != null) {
					int numTaxa = taxa.getNumTaxa();
					for (int it = 0; it<numTaxa; it++) {
						if (!writeOnlySelectedTaxa || taxa.getSelected(it)){
							String currentSequence = getTiledBaitsAsFasta(data, taxa, it);
							if (StringUtil.notEmpty(currentSequence)){
								buffer.append(currentSequence);
							}
						}
					}
				}
			}
		}

		if (buffer!=null) {
			saveExportedFileWithExtension(buffer, arguments, preferredDataFileExtension());
			return true;
		}

		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Export Tiled Baits";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports for a FASTA file containing all of the sequences tiled." ;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

}

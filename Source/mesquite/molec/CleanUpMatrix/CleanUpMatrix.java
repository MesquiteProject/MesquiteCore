package mesquite.molec.CleanUpMatrix;

import java.awt.Checkbox;

import mesquite.categ.lib.CategDataAlterer;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.MolecularDataUtil;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererAlignShift;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;


/*  Authors: David Maddison, Wayne Maddison
 * 
 * TODO:
 * 	- store preferences?
 *  
 */
/* ======================================================================== */
public class CleanUpMatrix extends CategDataAlterer implements AltererAlignShift {
	boolean reverseComplementIfNecessary = true;
	boolean multipleSequenceAlignment = true;
	int referenceSequence = 1;
	boolean setCodonPositions = false;
	CategDataAlterer aligner = null;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*
	public void processSingleXMLPreference (String tag, String content) {
		if ("matchFraction".equalsIgnoreCase(tag)) {
			matchFraction = MesquiteDouble.fromString(content);
		}
	
		preferencesSet = true;
}
/*.................................................................................................................*
public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "matchFraction", matchFraction);  

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public boolean queryOptions(int numTaxa) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Clean Up Matrix Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		queryDialog.addLabel("Clean Up Matrix Options");
		
		Checkbox reverseComplementBox = queryDialog.addCheckBox("reverse complement if necessary", reverseComplementIfNecessary);
		RadioButtons alignRadios = queryDialog.addRadioButtons(new String[] {"multiple sequence alignment", "shift to match chosen sequence"}, 0);
		IntegerField shiftToMatchField =  queryDialog.addIntegerField ("Reference sequence", 1, 4, 1, numTaxa);
		Checkbox setCodPosBox = queryDialog.addCheckBox("set codon positions to minimize stop codons", setCodonPositions);

		queryDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			reverseComplementIfNecessary = reverseComplementBox.getState();
			multipleSequenceAlignment = alignRadios.getValue()==0;
			if (!multipleSequenceAlignment){
				referenceSequence = shiftToMatchField.getValue()-1;
				if (referenceSequence<0)
					referenceSequence=0;
			}
			setCodonPositions = setCodPosBox.getState();
			storePreferences();
		}
		queryDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public boolean isReverseComplementIfNecessary() {
		return reverseComplementIfNecessary;
	}
	public void setReverseComplementIfNecessary(boolean reverseComplementIfNecessary) {
		this.reverseComplementIfNecessary = reverseComplementIfNecessary;
	}
	public boolean isMultipleSequenceAlignment() {
		return multipleSequenceAlignment;
	}
	public void setMultipleSequenceAlignment(boolean multipleSequenceAlignment) {
		this.multipleSequenceAlignment = multipleSequenceAlignment;
	}
	public int getReferenceSequence() {
		return referenceSequence;
	}
	public void setReferenceSequence(int referenceSequence) {
		this.referenceSequence = referenceSequence;
	}
	public boolean isSetCodonPositions() {
		return setCodonPositions;
	}
	public void setSetCodonPositions(boolean setCodonPositions) {
		this.setCodonPositions = setCodonPositions;
	}
	/*.................................................................................................................*/
   	public void alterCell(CharacterData data, int ic, int it){
   	}
	/*.................................................................................................................*/
   	private void processData(DNAData data, Taxa taxa) {
   		if (reverseComplementIfNecessary)
  			MolecularDataUtil.reverseComplementSequencesIfNecessary(data, module, taxa, 0, taxa.getNumTaxa(), referenceSequence, false, false, false);
   		if (multipleSequenceAlignment){
   			if (aligner==null)
   				aligner= (CategDataAlterer)hireNamedEmployee(CategDataAlterer.class, "#AMultipleAlignService");
   			if (aligner!=null)
   				aligner.alterData(data, null,  null);
   		} else
   			MolecularDataUtil.pairwiseAlignMatrix(this, data, referenceSequence, false);
   		if (setCodonPositions){
   			MolecularDataUtil.setCodonPositionsToMinimizeStops(data, module, taxa, 0, taxa.getNumTaxa());
   		}
   		// then alter taxon names
   		//open character matrix
   		// color by amino acid if protein coding

	}
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		if (data==null)
			return -10;
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying about options")){ //need to check if can proceed
   			if (!queryOptions(data.getNumTaxa()))
   				return ResultCodes.USER_STOPPED;
		}

		if (!(data instanceof DNAData))
			return ResultCodes.INCOMPATIBLE_DATA;
	//	try{
		processData((DNAData)data,data.getTaxa());
//		}
//		catch (ArrayIndexOutOfBoundsException e){
//			return false;
//		}
		return ResultCodes.SUCCEEDED;
   	}
   	
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
 	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	   /*.................................................................................................................*/
   	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
   	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
   	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
      	public int getVersionOfFirstRelease(){
      		return 300;  
    }
    	/*.................................................................................................................*/
   	 public String getName() {
		return "Clean Up Matrix";
  	 }
 	/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Clean Up Matrix...";
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "This will adjust sequences according to the option chosen (e.g., by reverse complementing, aligning the matrix, and then adjusting the codon postions)." ;
   	 }
   	 
}



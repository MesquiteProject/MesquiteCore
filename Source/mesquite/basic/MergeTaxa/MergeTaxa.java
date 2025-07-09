/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.MergeTaxa;

import java.awt.Checkbox;
import java.awt.Label;

import mesquite.assoc.lib.AssociationsManager;
import mesquite.assoc.lib.TaxaAssociation;
import mesquite.categ.lib.CategoricalData;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.ResultCodes;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.TaxonMerger;
import mesquite.lib.duties.TaxonUtility;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;


/* ======================================================================== */
public class MergeTaxa extends TaxonMerger {
	//	int maxNameLength=30;
	boolean keepEntireName=true;
	static int USEFIRSTNAME = 0;
	static int KEEPENTIRE = 1;
	static int KEEPPARTIAL = 2;
	int keepMode=USEFIRSTNAME;
	int startLengthToKeep = 10;
	int endLengthToKeep = 4;
	boolean preferencesSet = false;

	boolean keepUnmergedTaxa = false;  //this is the one saved to preferences
	boolean retainOriginals = false; //this is the temporary one sorted out during querying
	boolean addMergedToName = true;
	boolean verboseReport = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("startLengthToKeep".equalsIgnoreCase(tag)) {
			startLengthToKeep = MesquiteInteger.fromString(content);
		} else  if ("endLengthToKeep".equalsIgnoreCase(tag)) {
			endLengthToKeep = MesquiteInteger.fromString(content);
		} else  if ("keepEntireName".equalsIgnoreCase(tag)) {   // retain this for older installations before keepMode was added
			keepEntireName = MesquiteBoolean.fromTrueFalseString(content);
			if (keepEntireName)
				keepMode=KEEPENTIRE;
			else
				keepMode = KEEPPARTIAL;
		} else  if ("keepMode".equalsIgnoreCase(tag)) {
			keepMode = MesquiteInteger.fromString(content);
		} else  if ("mergeRule".equalsIgnoreCase(tag)) {
			mergeRule = MesquiteInteger.fromString(content);
		} else  if ("setMultiplestatesToUncertainty".equalsIgnoreCase(tag)) { //old, v. 4.00 and before
			boolean setMultiplestatesToUncertainty = setMultiplestatesToUncertainty = MesquiteBoolean.fromTrueFalseString(content);
			if (setMultiplestatesToUncertainty)
				mergeRule = CharacterData.MERGE_blendMultistateAsUncertainty;
			else
				mergeRule = CharacterData.MERGE_blendMultistateAsPolymorphism;
		} else  if ("addMergedToName".equalsIgnoreCase(tag)) {
			addMergedToName = MesquiteBoolean.fromTrueFalseString(content);
		} else  if ("verboseReport".equalsIgnoreCase(tag)) {
			verboseReport = MesquiteBoolean.fromTrueFalseString(content);
		}  else  if ("keepUnmergedTaxa".equalsIgnoreCase(tag)) {
			keepUnmergedTaxa = MesquiteBoolean.fromTrueFalseString(content);
		}  

		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "startLengthToKeep", startLengthToKeep);  
		StringUtil.appendXMLTag(buffer, 2, "endLengthToKeep", endLengthToKeep);  
		StringUtil.appendXMLTag(buffer, 2, "keepMode", keepMode);  
		StringUtil.appendXMLTag(buffer, 2, "mergeRule", mergeRule);  
		StringUtil.appendXMLTag(buffer, 2, "addMergedToName", addMergedToName);  
		StringUtil.appendXMLTag(buffer, 2, "verboseReport", verboseReport);  
		StringUtil.appendXMLTag(buffer, 2, "keepUnmergedTaxa", keepUnmergedTaxa);  

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/

	/*
	 * Merge rules:
	public static final int MERGE_blendMultistateAsPolymorphism = 0;
	public static final int MERGE_blendMultistateAsUncertainty = 1;
	public static final int MERGE_useLongest = 2;
	public static final int MERGE_preferReceiving = 3;
	public static final int MERGE_preferIncoming = 4;

	 * */
	int mergeRule = CharacterData.MERGE_useLongest;
	/*.................................................................................................................*/
	public boolean queryOptions(Taxa taxa, boolean[] toBeMerged, boolean formTaxonName, String dialogTitle, boolean permitRetainOriginals) {

		boolean nonCategFound = false;
		int numMatricesWithMultiple = 0;
		String matrixList =  "";

		if (toBeMerged != null){
			int numMatrices = getProject().getNumberCharMatrices(taxa);
			boolean[] multipleTaxaWithData = new boolean[numMatrices];
			for (int iM = 0; iM < numMatrices; iM++){
				multipleTaxaWithData[iM]=false;
				CharacterData data = getProject().getCharacterMatrix(taxa, iM);
				boolean dataFound=false;
				for (int it = 0; it<taxa.getNumTaxa() && !multipleTaxaWithData[iM]; it++) {
					if (toBeMerged[it] && data.hasDataForTaxon(it)) {
						if (dataFound) {
							multipleTaxaWithData[iM] = true;
							numMatricesWithMultiple++;
							if (numMatricesWithMultiple <6) {
								if (StringUtil.blank(matrixList)) {
									matrixList += data.getName();
								} else
									matrixList += ", " + data.getName();
							}
							else if (numMatricesWithMultiple == 6)
								matrixList += " and others";
							if (!(data instanceof CategoricalData)){
								nonCategFound = true;
							}
						}
						else
							dataFound=true;
					}

				}
			}
		}


		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Merge Taxa",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		queryDialog.appendToHelpString("This will cause the taxa to be merged together into a single taxon. ");
		String s = "Their character states in their character matrices will be merged.  " 
				+ "Other associated information like footnotes, attachments, and so forth WILL NOT be merged and will be lost from all but the first taxon."
				+ "We recommend you save a version of the data file before you do this.  You will not be able to undo this. ";
		queryDialog.appendToHelpString(s);
		queryDialog.addLabel(dialogTitle);

		queryDialog.addHorizontalLine(1);

		RadioButtons choices = null;
		IntegerField startLengthToKeepField = null;
		IntegerField endLengthToKeepField = null;
		Checkbox addMergedToNameBox = null;
		if (formTaxonName){
			queryDialog.addLabel("Name of merged taxon", Label.CENTER, true, true);
			choices = queryDialog.addRadioButtons (new String[]{"Use first taxon's name", "Merge taxon names, retaining full length", "Merge taxon names, retaining partial names:"}, keepMode);
			startLengthToKeepField = queryDialog.addIntegerField("                                                    Number of characters from start of each name to retain:", startLengthToKeep, 6, 0, 200);
			endLengthToKeepField = queryDialog.addIntegerField("                                                    Number of characters from end of each name to retain:", endLengthToKeep, 6, 0, 200);

			addMergedToNameBox = queryDialog.addCheckBox("Add \"merged\" to name if not done automatically", addMergedToName);
		}

		queryDialog.addHorizontalLine(1);
		Checkbox keepUnmergedTaxaBox = null;
		if (permitRetainOriginals) {
			keepUnmergedTaxaBox = queryDialog.addCheckBox("Keep original, unmerged taxa", keepUnmergedTaxa);
			queryDialog.addHorizontalLine(1);
		}
		queryDialog.addLabel("If multiple merged taxa have data for a matrix:",Label.LEFT);
		RadioButtons mergeRulesRB = queryDialog.addRadioButtons(new String[]{"Blend data; treat multiple states as polymorphism", "Blend data; treat multiple states as uncertainty", "Use states of taxon with most data (e.g., longest sequence)", "Prefer states of first selected taxon", "Prefer states of taxa merged into it"}, mergeRule);

		if (StringUtil.notEmpty(matrixList)){
			queryDialog.addHorizontalLine(1);
			if (numMatricesWithMultiple>1)
				queryDialog.addLabel("Note: For " + numMatricesWithMultiple+ " matrices, ", Label.LEFT);
			else
				queryDialog.addLabel("Note: For 1 matrix, ", Label.LEFT);
			queryDialog.addLabel("there are at least two taxa to be merged that each contain data: ",Label.LEFT);
			queryDialog.addLabel(matrixList, Label.CENTER, true, true);
			String ncS = ".";
			if (nonCategFound)
				ncS = ",";
			queryDialog.addLabel("The data in these taxa for these matrices will be merged according to the rules chosen" + ncS, Label.LEFT);
			if (nonCategFound){
				queryDialog.addLabel("except for the continous or meristic matrices, for which the blending options will not work;", Label.LEFT);
				queryDialog.addLabel("only the first taxon's states will be kept for those characters in which they both have data.", Label.LEFT);
			}
			queryDialog.addHorizontalLine(1);
		}
		queryDialog.addLargeOrSmallTextLabel("CAUTION: You will not be able to undo this. Associated information like footnotes, attachments, and so forth may be lost from all but the first taxon.");
		Checkbox verboseCB = queryDialog.addCheckBox("Give verbose report", verboseReport);
		queryDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			if (formTaxonName){
				keepMode = choices.getValue();
				startLengthToKeep = startLengthToKeepField.getValue();
				endLengthToKeep = endLengthToKeepField.getValue();
				addMergedToName = addMergedToNameBox.getState();
			}
			mergeRule = mergeRulesRB.getValue();
			verboseReport = verboseCB.getState();
			if (permitRetainOriginals)
				keepUnmergedTaxa = keepUnmergedTaxaBox.getState();
			storePreferences();
		}
		retainOriginals = keepUnmergedTaxa && permitRetainOriginals;
		queryDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/*.................................................................................................................*/
	boolean doMerge(Taxa taxa, boolean[] selected, String taxonName, StringBuffer reportRecord){
		int numSelected = 0;
		for (int i = 0; i< selected.length; i++)
			if (selected[i]) numSelected++;
		if (numSelected <1)
			return false;
		int numMatrices = getProject().getNumberCharMatrices(taxa);
		int firstSelected =-1;
		for (int i = 0; i< selected.length; i++)
			if (selected[i]) {
				firstSelected = i;
				break;
			}
		String originalTaxonName = taxa.getTaxonName(firstSelected);

		StringBuffer sb = new StringBuffer();
		String mergedNames = "Merger of " + numSelected +":";
		for (int it = 0; it<taxa.getNumTaxa(); it++) 
			if (selected[it]) {
				if (it != firstSelected)
					mergedNames += ",";
				mergedNames += " " + taxa.getTaxonName(it);
			}
		
		if (taxonName == null){
			//now let's merge the taxon names
			int count=0;
			for (int it = 0; it<taxa.getNumTaxa(); it++) {
				if (selected[it]) {
					String s = taxa.getTaxonName(it);
					if (keepMode==KEEPPARTIAL && startLengthToKeep>0 && endLengthToKeep>0) {
						if (s.length()>startLengthToKeep+endLengthToKeep) {
							sb.append(s.substring(0,startLengthToKeep)+s.substring(s.length()-endLengthToKeep));
						}
					} else {
						sb.append(s);
						if (keepMode==USEFIRSTNAME) {
							break;
						}
					}
					count++;
					if (count<numSelected)
						sb.append("+");
				}
			}
			if (addMergedToName || keepUnmergedTaxa)
				sb.append(" merged");

		}
		else
			sb.append(taxonName);

		//	selected[firstSelected] = false;

		/*
		 * Merge rules:
		 	CharacterData.MERGE_preferReceiving = 0;
			CharacterData.MERGE_preferIncoming = 1;
			CharacterData.MERGE_useLongest = 2;
			CharacterData.MERGE_blendMultistateAsUncertainty = 3;

		 * */
		int destinationTaxon = firstSelected;
		if (retainOriginals) {
			int lastSelected = taxa.lastSelected();
			taxa.addTaxa(lastSelected, 1, true);
			destinationTaxon=lastSelected+1;
		}
		String report = "";
		for (int iM = 0; iM < numMatrices; iM++){
			CharacterData data = getProject().getCharacterMatrix(taxa, iM);

			boolean[] ma = data.mergeTaxa(destinationTaxon, selected, mergeRule);
			if (ma!= null){
				if (verboseReport){
					if (mergeRule == CharacterData.MERGE_blendMultistateAsUncertainty || mergeRule == CharacterData.MERGE_blendMultistateAsPolymorphism){
						if (data instanceof CategoricalData)
							report += "For matrix \"" + data.getName() + "\", the following taxa when merged to taxon \"" + originalTaxonName + "\" required merging of character states:\n";
						else
							report += "For matrix \"" + data.getName() + "\", states of the following taxa may have been discarded when merging with taxon \"" + originalTaxonName + "\":\n";
					}
					else {
						report += "For matrix \"" + data.getName() + "\", taxon \"" + originalTaxonName + "\" and also the following taxa had data, and thus a choice of one or the other was made:\n";
					}
					for (int it = 0; it< ma.length; it++){
						if (ma[it])
							report += "  " + taxa.getTaxonName(it) + "\n";
					}
				}
				else {
					if (StringUtil.notEmpty(report))
						report += ",";
					report += " " + data.getName();
				}
			}
		}

		//Merging in taxa associations
		AssociationsManager assocManager = (AssociationsManager)getFileCoordinator().findEmployeeWithDuty(AssociationsManager.class);
		for (int iA = 0; iA < assocManager.getNumberOfAssociations(taxa); iA++){
			TaxaAssociation assoc = assocManager.getAssociation(taxa, iA);
			assoc.mergeTaxa(taxa, selected, destinationTaxon);

		}
		taxa.setTaxonName(destinationTaxon, sb.toString());
		taxa.setAnnotation(destinationTaxon, mergedNames);
		if (reportRecord != null && !StringUtil.blank(report)){
			if (!verboseReport)
				reportRecord.append("Matrices with data in multiple merged taxa: ");
			reportRecord.append(report);
			reportRecord.append("\n");
		}

		if (!retainOriginals) {
			for (int it =  taxa.getNumTaxa() -1; it>= firstSelected; it--){
				if (it<selected.length && selected[it] && it!=destinationTaxon) {
					taxa.deleteTaxa(it, 1, false);
				}
			}

		}
		return true;	
	}

	/*.................................................................................................................*/
	/** Called to merge the set of taxa indicated by the boolean array. If a bit is set, that is one of the taxa to be merged.  */
	public int mergeTaxa(Taxa taxa, boolean[] toBeMerged, String taxonName, StringBuffer report){

		int numMatrices = getProject().getNumberCharMatrices(taxa);
		boolean success =  doMerge(taxa, toBeMerged, taxonName, report);
		if (!retainOriginals) {
			taxa.notifyListeners(this, new Notification(PARTS_DELETED));
			for (int iM = 0; iM < numMatrices; iM++){
				CharacterData data = getProject().getCharacterMatrix(taxa, iM);
				data.notifyListeners(this, new Notification(PARTS_DELETED));
			}
		}

		if (success)
			return ResultCodes.SUCCEEDED;
		else
			return ResultCodes.ERROR;

	}
	/*.................................................................................................................*/
	public String getName() {
		return "Merge Taxa";
	}
	/*.................................................................................................................*
	public String getNameForMenuItem() {
		return "Merge Selected Taxa...";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Merges taxa and their character states.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;   //highly modified in 401
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}






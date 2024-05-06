/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class GBLOCKSCalculator implements  XMLPreferencesProcessor, ActionListener {


	static final double defaultIS = 0.50;   
	static final double defaultFS = 0.85;  
	static final int defaultCP=8;  
	static final int defaultBL=10;  
	static final double defaultGapThreshold = 0.0;
	static final boolean defaultChooseAmbiguousSites = true;
	static final boolean defaultCountWithinApplicable = false;
	//static final double defaultTermGapsPropForgiven = 0.0;
	static final boolean defaultIgnoreTaxaWithoutSequence = true;

	double IS = defaultIS;   // fraction of identical residues that is upper boundary for non-conserved sequences
	double FS = defaultFS;  // fraction of identical residues that is upper boundary for conserved sequences
	int CP=defaultCP;  //block size limit for non-conserved blocks
	int BL=defaultBL;  //  small region block size limit 
	double gapThreshold = defaultGapThreshold;   // the fraction of gaps allowed at a site

	boolean removeAllGaps = true;
	boolean chooseAmbiguousSites = defaultChooseAmbiguousSites;
	boolean countWithinApplicable = defaultCountWithinApplicable;   // count fractions of identical residues only within those taxa without gaps at a site
	//double termGapsPropForgiven = defaultTermGapsPropForgiven;
	boolean ignoreTaxaWithoutSequence = defaultIgnoreTaxaWithoutSequence;
	boolean[] taxonHasSequence=null;

	//boolean preferencesSet = false;


	public GBLOCKSCalculator (MesquiteModule ownerModule, String xmlPrefsString){
		XMLUtil.readXMLPreferences(ownerModule, this, xmlPrefsString);

	}



	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("IS".equalsIgnoreCase(tag))
			IS = MesquiteDouble.fromString(content);
		if ("FS".equalsIgnoreCase(tag))
			FS = MesquiteDouble.fromString(content);
		if ("CP".equalsIgnoreCase(tag))
			CP = MesquiteInteger.fromString(content);
		if ("BL".equalsIgnoreCase(tag))
			BL = MesquiteInteger.fromString(content);
		if ("gapThreshold".equalsIgnoreCase(tag))
			gapThreshold = MesquiteDouble.fromString(content);
		if ("chooseAmbiguousSites".equalsIgnoreCase(tag))
			chooseAmbiguousSites = MesquiteBoolean.fromTrueFalseString(content);
		if ("countWithinApplicable".equalsIgnoreCase(tag))
			countWithinApplicable = MesquiteBoolean.fromTrueFalseString(content);
		//if ("termGapsPropForgiven".equalsIgnoreCase(tag))
		//	termGapsPropForgiven = MesquiteDouble.fromString(content);
		//	if ("ignoreTaxaWithoutSequence".equalsIgnoreCase(tag))
		//		ignoreTaxaWithoutSequence = MesquiteBoolean.fromTrueFalseString(content);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "IS", IS);  
		StringUtil.appendXMLTag(buffer, 2, "FS", FS);  
		StringUtil.appendXMLTag(buffer, 2, "CP", CP);  
		StringUtil.appendXMLTag(buffer, 2, "BL", BL);  
		StringUtil.appendXMLTag(buffer, 2, "gapThreshold", gapThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "chooseAmbiguousSites", chooseAmbiguousSites);  
		StringUtil.appendXMLTag(buffer, 2, "countWithinApplicable", countWithinApplicable);  
		//StringUtil.appendXMLTag(buffer, 2, "termGapsPropForgiven", termGapsPropForgiven);  
		//	StringUtil.appendXMLTag(buffer, 2, "ignoreTaxaWithoutSequence", ignoreTaxaWithoutSequence);  

		return buffer.toString();
	}

	public boolean getChooseAmbiguousSites() {
		return chooseAmbiguousSites;
	}

	public void setChooseAmbiguousSites(boolean chooseAmbiguousSites) {
		this.chooseAmbiguousSites = chooseAmbiguousSites;
	}


	Button useDefaultsButton = null;
	DoubleField ISfield = null;
	DoubleField FSfield=null;
	Checkbox countWithinApplicableCheckbox=null;
	IntegerField CPfield =null;
	IntegerField BLfield=null;
	DoubleField gapThresholdField=null;
	RadioButtons chooseAmbiguousSitesRadioButtons=null;
	DoubleField termGapsPropForgivenField=null;
	//Checkbox ignoreTaxaWithoutSequenceCheckbox=null;
	/*.................................................................................................................*/
	public String getActionToUse(String action) {
		String actionToUse = "Choose";
		if (StringUtil.notEmpty(action))
			actionToUse=action;
		return actionToUse;
	}

	/*.................................................................................................................*/
	/**This queryOptions is provided in case the module that uses this GBLOCKSCalculator doesn't want to add extra options, and just wants to use
	 * a simple dialog box to query for options. If the ownermodule wishes, it can make its own dialog box; use this one as a template. */
	public boolean queryOptions(MesquiteModule mb, String action) {
		if (!mb.okToInteractWithUser(mb.CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		String actionToUse = getActionToUse(action);

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(mb.containerOfModule(),  actionToUse+ " using GBLOCKS Algorithm",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		String helpString = "This feature will " + actionToUse.toLowerCase() +  " characters using the GBLOCKS algorithm, as described in Castresana (2000).";

		dialog.appendToHelpString(helpString);

		dialog.addLabel(actionToUse + " Characters using Extended GBLOCKS Algorithm");

		addToQueryOptions(dialog, action);

		dialog.addNewDialogPanel();
		useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			processQueryOptions(dialog);
			mb.storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("setToDefaults")) {
			setToDefaults();
		} 
	}

	/*.................................................................................................................*/
	public void addToQueryOptions(ExtensibleDialog dialog, String action) {
		ISfield = dialog.addDoubleField("Minimum fraction of identical residues for conserved positions (b1)", IS, 4);
		FSfield = dialog.addDoubleField("Minimum fraction of identical residues for highly-conserved positions (b2)", FS, 4);
		CPfield = dialog.addIntegerField("Maximum length of non-conserved blocks (b3)", CP, 4);
		BLfield = dialog.addIntegerField("Minimum length of a block (b4)", BL, 4);
		dialog.addHorizontalLine(1);
		dialog.addLabel("Mesquite-specific extensions:");
		countWithinApplicableCheckbox = dialog.addCheckBox("Count fractions only within taxa with non-gaps at that position", countWithinApplicable);

		gapThresholdField = dialog.addDoubleField("Fraction of gaps allowed in a character", gapThreshold, 4);
		//termGapsPropForgivenField = dialog.addDoubleField("Prop. terminal gaps forgiven", termGapsPropForgiven, 4);
		//ignoreTaxaWithoutSequenceCheckbox = dialog.addCheckBox("Ignore taxa without any sequence*", ignoreTaxaWithoutSequence);
		dialog.addHorizontalLine(1);
		String actionToUse = getActionToUse(action);

	//	chooseAmbiguousSitesCheckbox = dialog.addCheckBox(actionToUse.toLowerCase()+ " sites in ambiguously aligned regions", chooseAmbiguousSites);
		int c = 0;
		if (!chooseAmbiguousSites)
			c = 1;
		chooseAmbiguousSitesRadioButtons = dialog.addRadioButtons (new String[] {action + " \"bad\" blocks (doubtfully aligned)", action + " \"good\" blocks (reasonably aligned)"}, c);

		dialog.addHorizontalLine(1);
	}
	/*.................................................................................................................*/
	public void processQueryOptions(ExtensibleDialog dialog) {
		IS = ISfield.getValue();
		FS = FSfield.getValue();
		CP = CPfield.getValue();
		BL = BLfield.getValue();
		gapThreshold = gapThresholdField.getValue();
		countWithinApplicable = countWithinApplicableCheckbox.getState();
		int c  = chooseAmbiguousSitesRadioButtons.getValue();
		chooseAmbiguousSites = (c == 0);
		
		//termGapsPropForgiven = termGapsPropForgivenField.getValue();
		//ignoreTaxaWithoutSequence = ignoreTaxaWithoutSequenceCheckbox.getState();
	}
	/*.................................................................................................................*/
	public void setToDefaults() {
		ISfield.setValue(defaultIS);	
		FSfield.setValue(defaultFS);
		CPfield.setValue(defaultCP);
		BLfield.setValue(defaultBL);
		gapThresholdField.setValue(defaultGapThreshold);
		countWithinApplicableCheckbox.setState(defaultCountWithinApplicable);
		if (defaultChooseAmbiguousSites)
			chooseAmbiguousSitesRadioButtons.setValue(0);
		else
			chooseAmbiguousSitesRadioButtons.setValue(1);
	//	termGapsPropForgivenField.setValue(defaultTermGapsPropForgiven);
		//ignoreTaxaWithoutSequenceCheckbox.setState(defaultIgnoreTaxaWithoutSequence);
	}


	/*.................................................................................................................*/
	int maxNumberIdenticalResidues (CategoricalData data, int ic) {
		int[] numTaxaWithResidue = null;
		if (data instanceof DNAData)
			numTaxaWithResidue= new int[DNAState.maxDNAState+1];
		else if (data instanceof ProteinData)
			numTaxaWithResidue= new int[ProteinState.maxProteinState+1];

		CategoricalState cs = null;
		for (int it = 0; it<data.getNumTaxa(); it++) {   // calculate proportions
			cs = (CategoricalState)data.getCharacterState(cs, ic, it);
			if (!(cs.isUnassigned() || cs.isInapplicable() || !cs.isCombinable() || cs.cardinality()>1)) {  // single element
				int state = cs.getOnlyElement(cs.getValue());
				if (state>=0 && state<numTaxaWithResidue.length)
					numTaxaWithResidue[state]++;
			}
		}

		int maxCount=0;
		for (int is=0; is<numTaxaWithResidue.length; is++){
			if (numTaxaWithResidue[is]>maxCount)
				maxCount=numTaxaWithResidue[is];
		}

		return maxCount;
	}
	/*.................................................................................................................*/
	int numNonGaps (CategoricalData data, int ic) {
		int count=0;
		for (int it = 0; it<data.getNumTaxa(); it++) {   // calculate proportions
			if (!data.isInapplicable(ic, it)) { // doesn't need to consider terminal gaps etc. since if the base is there, it's there!
				count++;
			}
		}
		return count;
	}

	/*.................................................................................................................*/
	int[] firstBase, lastBase, numSequencesAtSite;
	int numTaxaWithSequence;
	/*.................................................................................................................*/
	int getNumTaxaCountableAtSite(int ic) {
		if (ic<0 || ic>numSequencesAtSite.length)
			return 0;
		return numSequencesAtSite[ic];
	}
	/*.................................................................................................................*/
	int getNumTaxaInTerminalGapsAtSite(int ic) {
		if (ic<0 || ic>numSequencesAtSite.length)
			return 0;
		return numSequencesAtSite[ic];
	}
	boolean isInTermGaps(int ic, int it) {
		if (it<0 || it>firstBase.length)
			return false;
		if (ic<firstBase[it] || ic>lastBase[it])
			return true;
		return false;
	}

	//A gap is forgiven if it is a terminal gap, and the number of terminal gaps at this site is below the proportion threshold
	boolean isGapForgiven(int ic, int it) {
		if (!taxonHasSequence[it])
			return true;
/*	//	if (termGapsPropForgiven==0)
	//		return false;
		if (!isInTermGaps(ic, it))
			return false;
		// count number of sequences within terminal gap region
		int countTG =0;
		int countTotal = 0;
		for (int it2 = 0; it2<taxonHasSequence.length; it2++) {
			if (taxonHasSequence[it2]) {
				countTotal++;
				if (isInTermGaps(ic, it2))
					countTG++;
			}
		}
		double propInTermGaps = 1.0*countTG/countTotal;
		return propInTermGaps <= termGapsPropForgiven;
		*/
		return false;
	}
	/*.................................................................................................................*/
	boolean tooManyGaps (CategoricalData data, int ic) {
		if (taxonHasSequence==null)
			return false;
		

		if (removeAllGaps) {  // if removeAllGaps is true, having a single gap is too many
			for (int it = 0; it<data.getNumTaxa(); it++) {
				if (data.isInapplicable(ic, it) && !isGapForgiven(ic, it)) //any unpermitted gap is too many //$$$ previous version didn't permit taxa to be sequenceless
					return true;
			}
		} else {
			int count = 0;
			for (int it = 0; it<data.getNumTaxa(); it++) {
				if ((data.isInapplicable(ic, it) && !isGapForgiven(ic, it)) && taxonHasSequence[it])
					count++;
			}
			if (count >(int)(gapThreshold*getNumTaxaCountableAtSite(ic)))  //more than permitted
				return true;
		}
		return false;
	}

	/*.................................................................................................................*/
	static final int STATUSUNSET=0;
	static final int NONCONSERVED = 1;
	static final int WITHGAP = 4;
	static final int CONSERVED = 2;
	static final int HIGHLYCONSERVED = 3;

	/*.................................................................................................................*/
	void setCharacterStatus(CategoricalData data, int[] status, int[] firstBase, int[] lastBase){
		if (data==null || status == null )
			return;
		for (int ic=0; ic<data.getNumChars() && ic<status.length; ic++) {
			if (tooManyGaps(data,ic))
				status[ic] = NONCONSERVED; 
			else {
				int maxIdentical = maxNumberIdenticalResidues(data, ic);
				if (!countWithinApplicable) {
					if (maxIdentical<(int)(IS*getNumTaxaCountableAtSite(ic))+1)
						status[ic] = NONCONSERVED; 
					else if (maxIdentical<(int)(FS*getNumTaxaCountableAtSite(ic)))
						status[ic] = CONSERVED; 
					else 
						status[ic] = HIGHLYCONSERVED; 
				} else {
					int totalResidues = numNonGaps(data,ic);
					if (1.0*maxIdentical/totalResidues<IS)
						status[ic] = NONCONSERVED; 
					else if (1.0*maxIdentical/totalResidues<FS)
						status[ic] = CONSERVED; 
					else 
						status[ic] = HIGHLYCONSERVED; 
				}
			}
		}
	}

	/*.................................................................................................................*/
	void setToSelectRange(boolean[] setToSelect, int icStart, int icEnd){
		if (setToSelect==null)
			return;
		for (int ic=icStart; ic<=icEnd && ic<setToSelect.length; ic++)
			setToSelect[ic]=true;
	}
	/*.................................................................................................................*/
	void examineRemainingBlock(int[] status, boolean[] setToSelect, int icStart, int icEnd){
		for (int ic=icStart; ic<=icEnd && ic<setToSelect.length; ic++)
			if (status[ic]!=HIGHLYCONSERVED)  // if it is not highly conserved, then need to select it
				setToSelect[ic]=true;
			else  // the moment we have a highly conserved one, get out of loop
				break;
		for (int ic=icEnd; ic>=icStart; ic--)
			if (status[ic]!=HIGHLYCONSERVED)  // if it is not highly conserved, then need to select it
				setToSelect[ic]=true;
			else  // the moment we have a highly conserved one, get out of loop
				break;
	}
	/*.................................................................................................................*/
	void examineRegionAroundGap(int[] status, boolean[] setToSelect, int icGap){
		setToSelect[icGap]=true;
		for (int ic=icGap+1; ic<setToSelect.length; ic++){  // look up
			if (setToSelect[ic])  // the moment we find one we have already excluded, break
				break;
			if (status[ic] == NONCONSERVED)  // if it is not highly conserved, then need to select it
				setToSelect[ic]=true;
		}
		for (int ic=icGap-1; ic>=0; ic--) {
			if (setToSelect[ic])  // the moment we find one we have already excluded, break
				break;
			if (status[ic] == NONCONSERVED)  // if it is not highly conserved, then need to select it
				setToSelect[ic]=true;
		}
	}

	/*.................................................................................................................*/
	/** Called to mark characters*/
	public boolean markCharacters(CharacterData data, MesquiteModule mb, boolean[] charToMark, StringBuffer results){
		if (charToMark==null)
			return false;
		if (data!=null && data.getNumChars()>0){

			//preparing array memory
			if (taxonHasSequence == null || taxonHasSequence.length != data.getNumTaxa()) {
				taxonHasSequence = new boolean[data.getNumTaxa()];
				firstBase = new int[data.getNumTaxa()]; // for taxon it, first base in sequence
				lastBase = new int[data.getNumTaxa()]; // for taxon ic, last base in sequence
			}			
			if (numSequencesAtSite == null || numSequencesAtSite.length != data.getNumChars())
				numSequencesAtSite = new int[data.getNumChars()];  // at char ic, the number of taxa that are "in sequence", i.e. not in terminal gap area


			// figure out which taxa have any sequence at all
			numTaxaWithSequence = 0;
			for (int it = 0; it<data.getNumTaxa() && it<taxonHasSequence.length; it++) {
				taxonHasSequence[it]=data.anyApplicableAfter(0, it);
				if (taxonHasSequence[it])
					numTaxaWithSequence++;
			}


			removeAllGaps=(gapThreshold==0.0);

			//figuring out first and last bases so that edges of applicability of sequences can be considered
			for (int it = 0; it< data.getNumTaxa(); it++) {
				int first = -1;
				for (int ic=0; ic<data.getNumChars(); ic++) {
					if (!data.isInapplicable(ic, it)) {
						first=ic;
						break;
					}
				}
				firstBase[it] = first;

				int last = -1;
				for (int ic=data.getNumChars()-1; ic>=0; ic--) {
					if (!data.isInapplicable(ic, it)) {
						last=ic;
						break;
					}
				}
				lastBase[it] = last;
			}
			//figuring out number of taxa that are "in sequence" for each site
			for (int ic=0; ic<data.getNumChars(); ic++) {
				/*if (termGapsPropForgiven > 0.0) {
					int count = 0;
					for (int it = 0; it< data.getNumTaxa(); it++)
						if (lastBase[it]<0 || (ic >= firstBase[it] && ic <=lastBase[it]))
							count++;
					numSequencesAtSite[ic]=count;
				}
				else { */
					numSequencesAtSite[ic]=numTaxaWithSequence;
				//}
			}


			//boolean[] charToMark = new boolean[data.getNumChars()];
			int[] status = new int[data.getNumChars()];
			for (int ic=0; ic<status.length; ic++) {
				status[ic]=STATUSUNSET;
				charToMark[ic] = false;
			}

			setCharacterStatus((CategoricalData)data, status, firstBase, lastBase);

			// ======  first look for "stretches of contiguous nonconserved positions"
			int blockStart=-1;
			for (int ic=0; ic<data.getNumChars(); ic++) {
				if (status[ic]==NONCONSERVED){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 > CP)  // block is big enough to be selected
							setToSelectRange(charToMark,blockStart,ic);
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						if (ic-blockStart > CP)  // block is big enough to be selected
							setToSelectRange(charToMark,blockStart,ic-1);
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  examining flanks

			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<charToMark.length; ic++){
				if (!charToMark[ic]){  // we are in a remaining block
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						examineRemainingBlock(status, charToMark,blockStart,ic);
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						if (ic-blockStart > CP)  // block is big enough to be selected
							examineRemainingBlock(status, charToMark,blockStart,ic-1);
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  checking for small blocks
			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<charToMark.length; ic++){
				if (!charToMark[ic]){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 < BL){  // block is big enough to be selected
							setToSelectRange(charToMark,blockStart,ic);
						}
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						if (ic-blockStart < BL){  // block is big enough to be selected
							setToSelectRange(charToMark,blockStart,ic-1);
						}
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  checking for gaps and nearby non-conserved


			for (int ic=0; ic<data.getNumChars() && ic<charToMark.length; ic++){
				if (!charToMark[ic] && tooManyGaps((CategoricalData)data,ic)){  // let's start at any gap that is not yet excluded
					examineRegionAroundGap(status, charToMark,ic);

				}
			}

			// ======  checking for small blocks
			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<charToMark.length; ic++){
				if (!charToMark[ic]){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 < BL){  // block is big enough to be selected
							setToSelectRange(charToMark,blockStart,ic);
						}
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						if (ic-blockStart < BL){  // block is big enough to be selected
							setToSelectRange(charToMark,blockStart,ic-1);
						}
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  Now report the results

			blockStart=-1;
			StringBuffer blocks = new StringBuffer();
			for (int ic=0; ic<data.getNumChars() && ic<charToMark.length; ic++){
				if (!charToMark[ic]){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix
						blocks.append(" [" + (blockStart+1)+" " + (ic+1)+ "]");
					}
				} else {  // let's check to see if we have reached the end of a  block
					if (blockStart>=0){ // we were within the block, now just one past it
						blocks.append(" [" + (blockStart+1)+" " + (ic)+ "]");
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			if (results!=null) {
				results.append("\nGBLOCKS analysis\n");
				if (countWithinApplicable) {
					results.append("Minimum fraction of identical residues for a conserved position: " + IS+"\n");
					results.append("Minimum fraction of identical residues for a highly-conserved position: " + FS+"\n");
					results.append("Counting fraction within only those taxa that have non-gaps at that position\n");
				} else {
					results.append("Minimum fraction of identical residues for a conserved position: " + IS+"\n");
					results.append("Minimum fraction of identical residues for a highly-conserved position: " + FS+"\n");
					//results.append("Minimum number of identical residues for a conserved position: " + ISint+"\n");
					//results.append("Minimum number of identical residues for a highly-conserved position: " + FSint+"\n");
					results.append("Counting fraction within all taxa\n");
				}
				results.append("Maximum number of contiguous non-conserved positions: " + CP+"\n");
				results.append("Minimum length of a block: " + BL+"\n");
				if (removeAllGaps)
					results.append("Allowed gaps within a position: none\n");
				else {
					results.append("Allowed fraction of gaps within a position: " + gapThreshold+"\n");
				}

				results.append("Flank positions of the blocks chosen by the GBLOCKS algorithm: \n");
				results.append(blocks.toString());

				if (chooseAmbiguousSites)
					results.append("\nNote:  selected characters are those that are the least conserved and more ambiguously aligned regions, and would typically be excluded before analysis.\n");
				else 
					results.append("\nNote:  selected characters are those that are the more conserved regions, and would typically be included in any analysis.\n");
			}


		}
		return true;
	}

}


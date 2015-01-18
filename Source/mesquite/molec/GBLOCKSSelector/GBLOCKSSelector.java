/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.GBLOCKSSelector;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class GBLOCKSSelector extends CharacterSelector {

	double IS = 0.50;   // fraction of identical residues that is upper boundary for non-conserved sequences
	int ISint=0;  // integral version of above
	double FS = 0.85;  // fraction of identical residues that is upper boundary for conserved sequences
	int FSint = 0;  //integeral version of above
	int CP=8;  //block size limit for non-conserved blocks
	int BL=10;  //  small region block size limit 
	double gapThreshold = 0.0;
	int gapThresholdInt = 0;
	boolean removeAllGaps = true;
	boolean selectOnesToExclude = true;
	boolean countWithinApplicable = true;
	boolean[] hasApplicable=null;

	boolean preferencesSet = false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	public boolean getPreferencesSet() {
		return preferencesSet;
	}
	public void setPreferencesSet(boolean b) {
		preferencesSet = b;
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
		if ("selectOnesToExclude".equalsIgnoreCase(tag))
			selectOnesToExclude = MesquiteBoolean.fromTrueFalseString(content);
		if ("countWithinApplicable".equalsIgnoreCase(tag))
			countWithinApplicable = MesquiteBoolean.fromTrueFalseString(content);

		preferencesSet = true;
	}
	
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "IS", IS);  
		StringUtil.appendXMLTag(buffer, 2, "FS", FS);  
		StringUtil.appendXMLTag(buffer, 2, "CP", CP);  
		StringUtil.appendXMLTag(buffer, 2, "BL", BL);  
		StringUtil.appendXMLTag(buffer, 2, "gapThreshold", gapThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "selectOnesToExclude", selectOnesToExclude);  
		StringUtil.appendXMLTag(buffer, 2, "countWithinApplicable", countWithinApplicable);  

		preferencesSet = true;
		return buffer.toString();
	}
	
	/*.................................................................................................................*/
	public boolean queryOptions() {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options"))  //Debugg.println needs to check that options set well enough to proceed anyway
			return true;
		
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Select using GBLOCKS Algorithm",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
	//	dialog.addLabel("RAxML - Options and Locations");
		String helpString = "This feature will select characters using the GBLOCKS algorithm, as described in Castresana (2000).";

		dialog.appendToHelpString(helpString);
		
		dialog.addLabel("Select Characters using Extended GBLOCKS Algorithm");
		
		DoubleField ISfield = dialog.addDoubleField("Minimum fraction of identical residues for conserved positions", IS, 4);
		DoubleField FSfield = dialog.addDoubleField("Minimum fraction of identical residues for highly-conserved positions", FS, 4);
		Checkbox countWithinApplicableCheckbox = dialog.addCheckBox("count fraction only within taxa with non-gaps at that position", countWithinApplicable);
		dialog.addHorizontalLine(1);
		IntegerField CPfield = dialog.addIntegerField("Maximum length of non-conserved blocks", CP, 4);
		IntegerField BLfield = dialog.addIntegerField("Minimum length of a block", BL, 4);

		DoubleField gapThresholdField = dialog.addDoubleField("Fraction of gaps allowed in a character", gapThreshold, 4);
		
		dialog.addHorizontalLine(1);

		Checkbox selectOnesToExcludeCheckbox = dialog.addCheckBox("select blocks to be excluded", selectOnesToExclude);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			IS = ISfield.getValue();
			FS = FSfield.getValue();
			CP = CPfield.getValue();
			BL = BLfield.getValue();
			gapThreshold = gapThresholdField.getValue();
			selectOnesToExclude = selectOnesToExcludeCheckbox.getState();
			countWithinApplicable = countWithinApplicableCheckbox.getState();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
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
			if (!data.isInapplicable(ic, it)) { 
				count++;
			}
		}
		return count;
	}

	/*.................................................................................................................*/
	boolean tooManyGaps (CategoricalData data, int ic) {
		if (hasApplicable==null)
			return false;
		if (removeAllGaps) {
			for (int it = 0; it<data.getNumTaxa(); it++) {
				if (data.isInapplicable(ic, it))
					return true;
			}
		} else {
			int count = 0;
			for (int it = 0; it<data.getNumTaxa(); it++) {
				if (data.isInapplicable(ic, it) && it<hasApplicable.length && hasApplicable[it])
					count++;
			}
			if (count >gapThresholdInt)
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
	void setCharacterStatus(CategoricalData data, int[] status){
		if (data==null || status == null )
			return;
		for (int ic=0; ic<data.getNumChars() && ic<status.length; ic++) {
			if (tooManyGaps(data,ic))
				status[ic] = NONCONSERVED; 
			else {
				int maxIdentical = maxNumberIdenticalResidues(data, ic);
				if (!countWithinApplicable) {
					if (maxIdentical<ISint)
						status[ic] = NONCONSERVED; 
					else if (maxIdentical<FSint)
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
	/** Called to select characters*/
	public void selectCharacters(CharacterData data){
		if (data!=null && data.getNumChars()>0){
			
			if(!queryOptions())
				return;
			
			hasApplicable = new boolean[data.getNumTaxa()];
			for (int it = 0; it<data.getNumTaxa() && it<hasApplicable.length; it++) {
				hasApplicable[it]=data.anyApplicableAfter(0, it);
			}

			int numTaxaWithApplicable = data.getNumTaxaWithAnyApplicable();
			ISint= (int)(IS*numTaxaWithApplicable)+1;
			FSint= (int)(FS*numTaxaWithApplicable);
			gapThresholdInt = 	(int)(gapThreshold*numTaxaWithApplicable);
			removeAllGaps=(gapThreshold==0.0);
			
			
			boolean[] setToSelect = new boolean[data.getNumChars()];
			int[] status = new int[data.getNumChars()];
			for (int ic=0; ic<status.length; ic++) {
				status[ic]=STATUSUNSET;
				setToSelect[ic] = false;
			}
			
			setCharacterStatus((CategoricalData)data, status);

			// ======  first look for "stretches of contiguous nonconserved positions"
			int blockStart=-1;
			for (int ic=0; ic<data.getNumChars(); ic++) {
				if (status[ic]==NONCONSERVED){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
						//Debugg.println("start of conserved block: " + (ic+1));
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 > CP)  // block is big enough to be selected
							setToSelectRange(setToSelect,blockStart,ic);
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						//Debugg.println("   end of conserved block: " + (ic));
						if (ic-blockStart > CP)  // block is big enough to be selected
							setToSelectRange(setToSelect,blockStart,ic-1);
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  examining flanks

			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<setToSelect.length; ic++){
				if (!setToSelect[ic]){  // we are in a remaining block
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
						//Debugg.println("start of remaining block: " + (ic+1));
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						examineRemainingBlock(status, setToSelect,blockStart,ic);
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						//Debugg.println("   end of remaining block: " + (ic));
						if (ic-blockStart > CP)  // block is big enough to be selected
							examineRemainingBlock(status, setToSelect,blockStart,ic-1);
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  checking for small blocks
			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<setToSelect.length; ic++){
				if (!setToSelect[ic]){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
				//Debugg.println("start of conserved block: " + (ic+1));
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 < BL){  // block is big enough to be selected
							setToSelectRange(setToSelect,blockStart,ic);
						}
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						//Debugg.println("   end of conserved block: " + (ic));
						if (ic-blockStart < BL){  // block is big enough to be selected
							setToSelectRange(setToSelect,blockStart,ic-1);
						}
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  checking for gaps and nearby non-conserved
			

			for (int ic=0; ic<data.getNumChars() && ic<setToSelect.length; ic++){
				if (!setToSelect[ic] && tooManyGaps((CategoricalData)data,ic)){  // let's start at any gap that is not yet excluded
					examineRegionAroundGap(status, setToSelect,ic);

				}
			}

			// ======  checking for small blocks
			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<setToSelect.length; ic++){
				if (!setToSelect[ic]){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
				//Debugg.println("start of conserved block: " + (ic+1));
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 < BL){  // block is big enough to be selected
							setToSelectRange(setToSelect,blockStart,ic);
						}
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						//Debugg.println("   end of conserved block: " + (ic));
						if (ic-blockStart < BL){  // block is big enough to be selected
							setToSelectRange(setToSelect,blockStart,ic-1);
						}
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  Now report the results

			blockStart=-1;
			StringBuffer results = new StringBuffer();
			for (int ic=0; ic<data.getNumChars() && ic<setToSelect.length; ic++){
				if (!setToSelect[ic]){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix
						results.append(" [" + (blockStart+1)+" " + (ic+1)+ "]");
					}
				} else {  // let's check to see if we have reached the end of a  block
					if (blockStart>=0){ // we were within the block, now just one past it
						results.append(" [" + (blockStart+1)+" " + (ic)+ "]");
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			logln("\nGBLOCKS analysis");
			if (countWithinApplicable) {
				logln("Minimum fraction of identical residues for a conserved position: " + IS);
				logln("Minimum fraction of identical residues for a highly-conserved position: " + FS);
				logln("Counting fraction within only those taxa that have non-gaps at that position");
			} else {
				logln("Minimum number of identical residues for a conserved position: " + ISint);
				logln("Minimum number of identical residues for a highly-conserved position: " + FSint);
				logln("Counting fraction within all taxa");
			}
			logln("Maximum number of contiguous non-conserved positions: " + CP);
			logln("Minimum length of a block: " + BL);
			if (removeAllGaps)
				logln("Allowed gaps within a position: none");
			else {
				logln("Allowed fraction of gaps within a position: " + gapThreshold);
			}
			
			logln("Flank positions of the blocks chosen by the GBLOCKS algorithm: ");
			logln(results.toString());
			
			if (selectOnesToExclude)
				logln("\nNote:  selected characters are those that are the least conserved and more ambiguously aligned regions, and would typically be excluded before analysis.");
			else 
				logln("\nNote:  selected characters are those that are the more conserved regions, and would typically be included in any analysis.");


			// ======  now select the characters chosen
			for (int ic=0; ic<data.getNumChars() && ic<setToSelect.length; ic++)
				if (setToSelect[ic]==selectOnesToExclude)
					data.setSelected(ic, true);


			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "GBLOCKS Selector";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "GBLOCKS Selector...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Selects characters according to an extended version of the GBLOCKS algorithm (Castresana, 2000)." ;
	}

}


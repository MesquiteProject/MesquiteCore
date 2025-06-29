/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.ShiftOtherToMatch;
/*~~  */

import java.awt.Checkbox;

import mesquite.categ.lib.CategDataAlterer;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.MolecularDataUtil;
import mesquite.lib.Bits;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.ResultCodes;
import mesquite.lib.StringUtil;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererAlignShift;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ExtensibleDialog;



/* ======================================================================== */
public class ShiftOtherToMatch extends CategDataAlterer  implements AltererAlignShift{
	CharacterState cs1;
	CharacterState cs2 ;
	double matchFraction=0.75;
	int it1= MesquiteInteger.unassigned;
	int it2= MesquiteInteger.unassigned;
	boolean preferencesSet = false;
	boolean reverseComplementIfNecessary = true;
	boolean shiftOneBlockOnly = false;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("matchFraction".equalsIgnoreCase(tag)) {
			matchFraction = MesquiteDouble.fromString(content);
		}
		else if ("reverseComplementIfNecessary".equalsIgnoreCase(tag)) {
			reverseComplementIfNecessary = MesquiteBoolean.fromTrueFalseString(content);
		}
		else if ("shiftOneBlockOnly".equalsIgnoreCase(tag)) {
			shiftOneBlockOnly = MesquiteBoolean.fromTrueFalseString(content);
		}
	
		preferencesSet = true;
}
/*.................................................................................................................*/
public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "matchFraction", matchFraction);  
		StringUtil.appendXMLTag(buffer, 2, "reverseComplementIfNecessary", reverseComplementIfNecessary);  
		StringUtil.appendXMLTag(buffer, 2, "shiftOneBlockOnly", shiftOneBlockOnly);  

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
public boolean queryOptions(int it, int max) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryFilesDialog = new ExtensibleDialog(containerOfModule(), "Shift Other To Match",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		queryFilesDialog.addLabel("Shift Other To Match");
				
		if (!MesquiteInteger.isCombinable(it1) || !MesquiteInteger.isCombinable(it2) || it1>max || it2>max){
			if (it>=max) {  // then it1 has to go back to first taxon
				it1=0;
				it2=it-1;
			} else {
				it1 = it+1;  // it+1 to bump it over selected one
				it2=max;  // 
			}
		//	if (it2==it)
		//	it2--;
		}
		if (it2<it1)
			it2=it1;
		IntegerField it1Field =  queryFilesDialog.addIntegerField ("First Sequence",(it1+1), 4, 1, max+1);
		IntegerField it2Field =  queryFilesDialog.addIntegerField ("Last Sequence", (it2+1), 4, 1, max+1);
		DoubleField matchFractionField = queryFilesDialog.addDoubleField ("Fraction of Match", matchFraction, 6, 0.00001, 1.0);
		
		Checkbox reverseSequencesCheckBox = queryFilesDialog.addCheckBox("Reverse complement sequences if necessary", reverseComplementIfNecessary);
		Checkbox shiftOneBlockOnlyBox = queryFilesDialog.addCheckBox("Shift only a single block of data", shiftOneBlockOnly);
		
		queryFilesDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			matchFraction = matchFractionField.getValue();
			it1 = it1Field.getValue()-1;
			it2 = it2Field.getValue()-1;
			reverseComplementIfNecessary = reverseSequencesCheckBox.getState();
			shiftOneBlockOnly = shiftOneBlockOnlyBox.getState();
			storePreferences();
		}
		queryFilesDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
   	public void alterCell(CharacterData data, int ic, int it){
   	}
	/*.................................................................................................................*/
	boolean findMatchInSequence(CharacterData data, int masterRow, int masterStart, int masterEnd, int it, int start, MesquiteInteger matchEnd){
		if (data.dataMatches(it, start, masterRow, masterStart, masterEnd, matchEnd, false, true, matchFraction, cs1, cs2)) {
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	boolean findMatch(CharacterData data, MesquiteTable table, int masterRow, int masterStart, int masterEnd, int it, MesquiteInteger matchStart, MesquiteInteger matchEnd){
		for (int i = 0; i<data.getNumChars(); i++) {  // cycle through possible starting points of match
			if (findMatchInSequence(data,masterRow, masterStart, masterEnd, it, i, matchEnd)){
				matchStart.setValue(i);
				return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		if (data==null || table==null)
			return -10;
		MesquiteInteger row = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();  // this is the first column selected in the block
		MesquiteInteger lastColumn = new MesquiteInteger();  // this is the last column selected
		if (table.onlySingleRowBlockSelected(row,firstColumn, lastColumn)) {
			if (!queryOptions(row.getValue(), data.getNumTaxa()-1))
					return ResultCodes.USER_STOPPED;
			MesquiteBoolean dataChanged = new MesquiteBoolean (false);
			MesquiteInteger charAdded = new MesquiteInteger(0);

			cs1 = data.getCharacterState(null, 0, 0); //to serve as persistent container
			cs2  = data.getCharacterState(null, 0, 0);
			MesquiteInteger matchStart = new MesquiteInteger();  // this will receive from the matcher the start of the match in the candidate sequence
			MesquiteInteger matchEnd = new MesquiteInteger();   // this will receive the end of the match
			boolean match=false;
			int totalAddedToStart = 0;
			boolean someAdded = false;
			for (int it = it1; it<=it2; it++) {  
				if (row.getValue()!=it && data.hasDataForTaxon(it)) {  // June 2021 added hasDataForTaxon check
					match = findMatch(data,table, row.getValue(), firstColumn.getValue(), lastColumn.getValue(), it,matchStart,matchEnd);
					if (reverseComplementIfNecessary && !match && data instanceof DNAData) {
						MolecularDataUtil.reverseComplementSequencesIfNecessary((DNAData)data, this, data.getTaxa(), it, it, row.getValue(), false, false, false);
						match = findMatch(data,table, row.getValue(), firstColumn.getValue(), lastColumn.getValue(), it,matchStart,matchEnd);  // added 31 October 2015
					}
					if (match) {
						int added = 0;
						if (shiftOneBlockOnly) {
							int startBlock = data.getStartofBlock(matchStart.getValue(), it, true);
							int endBlock = data.getEndofBlock(matchStart.getValue(), it, true);
							int distance = firstColumn.getValue()-matchStart.getValue();
							Bits whichTaxa = new Bits(data.getNumTaxa());
							whichTaxa.clearAllBits();
							whichTaxa.setBit(it);
							added = data.moveCells(startBlock, endBlock, distance, whichTaxa, true, false, true, false, dataChanged, charAdded, null);
						} else {
							added = data.shiftAllCells(firstColumn.getValue()-matchStart.getValue(), it, true, true, true, dataChanged,charAdded, null);
						}
						if (charAdded.isCombinable() && charAdded.getValue()!=0 && data instanceof DNAData) {
							((DNAData)data).assignCodonPositionsToTerminalChars(charAdded.getValue());
							//							((DNAData)data).assignGeneticCodeToTerminalChars(charAdded.getValue());
						}
						if (added!=0)
							someAdded=true;
						if (added<0) {
							totalAddedToStart -=added;
							firstColumn.add(-added);
							lastColumn.add(-added);
						}
					}
				}
			}
			if (totalAddedToStart>0) {
				if (table!=null) {
					table.shiftHorizScroll(totalAddedToStart);
					table.selectBlock(firstColumn.getValue(), row.getValue(), lastColumn.getValue(), row.getValue());  //Wayne: why doesn't this select a block in the matrix?
				}
			}
			if ( dataChanged.getValue())
				return ResultCodes.SUCCEEDED;
			return ResultCodes.MEH;
		}
		else {
			discreetAlert( "A portion of only one sequence can be selected.");
			return -13;
		}
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
      		return -100;  
    }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Shift Other To Match Selected";
   	 }
    		/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Shift Other To Match Selected...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Shifts other sequences to match the one selected." ;
   	 }
   	 
}




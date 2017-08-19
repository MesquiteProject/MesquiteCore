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

import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.align.lib.*;



/* ======================================================================== */
public class ShiftOtherToMatch extends CategDataAlterer  implements AltererAlignShift{
	CharacterState cs1;
	CharacterState cs2 ;
	double matchFraction=0.75;
	int it1= MesquiteInteger.unassigned;
	int it2= MesquiteInteger.unassigned;
	boolean preferencesSet = false;
	boolean reverseComplementIfNecessary = true;
	
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
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("matchFraction".equalsIgnoreCase(tag)) {
			matchFraction = MesquiteDouble.fromString(content);
		}
		else if ("reverseComplementIfNecessary".equalsIgnoreCase(tag)) {
			reverseComplementIfNecessary = MesquiteBoolean.fromOffOnString(content);
		}
	
		preferencesSet = true;
}
/*.................................................................................................................*/
public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "matchFraction", matchFraction);  
		StringUtil.appendXMLTag(buffer, 2, "reverseComplementIfNecessary", reverseComplementIfNecessary);  

		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
public boolean queryOptions(int it, int max) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryFilesDialog = new ExtensibleDialog(containerOfModule(), "Shift Other To Match",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		queryFilesDialog.addLabel("Shift Other To Match");
		
		if (!MesquiteInteger.isCombinable(it1) || !MesquiteInteger.isCombinable(it2) || it1>max || it2>max){
			if (it>=max) {  
				it1=1;
			} else
				it1 = it+2;  // it+1 to bump it over one, +2 because of the translation to 1-based numbering
			it2=max;
		//	if (it2==it)
		//	it2--;
		}
		IntegerField it1Field =  queryFilesDialog.addIntegerField ("First Sequence", it1, 4, 1, max);
		IntegerField it2Field =  queryFilesDialog.addIntegerField ("Last Sequence", it2, 4, 1, max);
		DoubleField matchFractionField = queryFilesDialog.addDoubleField ("Fraction of Match", matchFraction, 6, 0.00001, 1.0);
		
		Checkbox reverseSequencesCheckBox = queryFilesDialog.addCheckBox("Reverse complement sequences if necessary", reverseComplementIfNecessary);
		
		queryFilesDialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			matchFraction = matchFractionField.getValue();
			it1 = it1Field.getValue();
			it2 = it2Field.getValue();
			reverseComplementIfNecessary = reverseSequencesCheckBox.getState();
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
   	public boolean alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		if (data==null || table==null)
			return false;
		MesquiteInteger row = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();  // this is the first column selected in the block
		MesquiteInteger lastColumn = new MesquiteInteger();  // this is the last column selected
		if (table.onlySingleRowBlockSelected(row,firstColumn, lastColumn)) {
			if (!queryOptions(row.getValue(), data.getNumTaxa()-1))
					return false;
			MesquiteBoolean dataChanged = new MesquiteBoolean (false);
			MesquiteInteger charAdded = new MesquiteInteger(0);

			cs1 = data.getCharacterState(null, 0, 0); //to serve as persistent container
			cs2  = data.getCharacterState(null, 0, 0);
			MesquiteInteger matchStart = new MesquiteInteger();  // this will receive from the matcher the start of the match in the candidate sequence
			MesquiteInteger matchEnd = new MesquiteInteger();   // this will receive the end of the match
			boolean match=false;
			int totalAddedToStart = 0;
			boolean someAdded = false;
			for (int it = it1-1; it<=it2-1; it++) {  // must subtract 1 off of it1 and it2 as these are 1-based numbers
				if (row.getValue()!=it) {
					match = findMatch(data,table, row.getValue(), firstColumn.getValue(), lastColumn.getValue(), it,matchStart,matchEnd);
					if (reverseComplementIfNecessary && !match && data instanceof DNAData) {
						MolecularDataUtil.reverseComplementSequencesIfNecessary((DNAData)data, this, data.getTaxa(), it, it, row.getValue(), false, false, false);
						match = findMatch(data,table, row.getValue(), firstColumn.getValue(), lastColumn.getValue(), it,matchStart,matchEnd);  // added 31 October 2015
					}
					if (match) {
						int added = data.shiftAllCells(firstColumn.getValue()-matchStart.getValue(), it, true, true, true, dataChanged,charAdded, null);
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
			return dataChanged.getValue();
		}
		else {
   			discreetAlert( "A portion of only one sequence can be selected.");
			return false;
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




/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.MatchingSequence;
/*~~  */

import java.awt.Checkbox;
import java.awt.TextArea;

import mesquite.categ.lib.DNAData;
import mesquite.lib.CommandRecord;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.molec.lib.FindSequenceCriterionG;
/* ======================================================================== *

*new in 1.05*

/* ======================================================================== */
public class MatchingSequence extends FindSequenceCriterionG {
	String sequence = null;
	String sequenceR = null;
	String sequenceC = null;
	String sequenceRC = null;
	int numMismatch = 0;
	boolean preferMinimal;
	boolean asIs = true;
	boolean reverse = false;
	boolean complement = false;
	boolean reverseComplement = false;
	boolean findAll = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean showOptions(boolean findAll, CharacterData data, MesquiteTable table){
	
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog findDialog = new ExtensibleDialog(containerOfModule(), "Find Sequence",buttonPressed);
			findDialog.addLargeOrSmallTextLabel ("Search for this sequence");
			String s = sequence;
			if (sequence == null)
				s = "";
			TextArea seq = findDialog.addTextArea(s, 3, 60, null);
			
			IntegerField nM = findDialog.addIntegerField("Number of allowed mismatches", numMismatch, 5);
			Checkbox preferMinimalCB = null;
			if (!findAll)
				preferMinimalCB = findDialog.addCheckBox("Prefer minimal mismatches", preferMinimal);
			else
				preferMinimal = false;
			Checkbox ai = findDialog.addCheckBox("Search for sequence as is", asIs);
			Checkbox rc = findDialog.addCheckBox("Search for reverse complement", reverseComplement);
			Checkbox r = findDialog.addCheckBox("Search for reverse", reverse);
			Checkbox c = findDialog.addCheckBox("Search for complement", complement);
			findDialog.setDefaultComponent(seq);
			findDialog.completeAndShowDialog("OK","Cancel",null,"OK");
			
			if (buttonPressed.getValue()==0) {
				this.findAll = findAll;
				sequence = seq.getText();
				sequence = StringUtil.stripWhitespace(sequence);
				sequence = StringUtil.delete(sequence, '-');
				if (sequence != null && sequence.length()>0 && nM.isValidInteger()){
					if (!findAll)
						preferMinimal = preferMinimalCB.getState();
					numMismatch = nM.getValue();
					sequenceR = DNAData.reverseString(sequence);
					sequenceC = DNAData.complementString(sequence);
					sequenceRC = DNAData.reverseString(sequenceC);
					reverseComplement = rc.getState();
					complement = c.getState();
					asIs = ai.getState();
					reverse = r.getState();
				}
				else sequence = null;
				
				findDialog.dispose();
			}
			else
				return false;
			if (sequence == null)
				return false;
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	public boolean findNext(CharacterData data, MesquiteTable table, MesquiteInteger charFound, MesquiteInteger length, MesquiteInteger taxonFound) {
			if (!preferMinimal) {
				if (findNext(data, table, sequence, numMismatch, charFound, length, taxonFound)){
	   				if (!findAll)
	   					logln("Sequence match found starting at site " + (charFound.getValue()+1) + " for taxon " + (taxonFound.getValue()+1));
					return true;
				}
				return false;
			}
			else for (int nM = 0; nM<= numMismatch; nM++) {
	   				if (findNext(data, table, sequence, nM, charFound, length, taxonFound)){
		   				if (!findAll)
		   					logln("Sequence match found allowing " + nM + " mismatches starting at site " + (charFound.getValue()+1) + " for taxon " + (taxonFound.getValue()+1));
	   					return true;
	   				}

   			}
   			return false;
   		
   	}
/**/

	int firstTaxon = 0;
	int firstChar = 0;
   	boolean findNext(CharacterData data, MesquiteTable table, String sequence, int numMismatch, MesquiteInteger charFound, MesquiteInteger length, MesquiteInteger taxonFound) {
  			length.setValue(0);
  			firstTaxon = taxonFound.getValue();
  			firstChar = charFound.getValue();
			for (int it = firstTaxon; it< data.getNumTaxa(); it++){
				CommandRecord.tick("Searching taxon " + data.getTaxa().getTaxonName(it));
	   			for (int ic = firstChar; ic< data.getNumChars(); ic++) {
	   				int extra = 0;
	   				boolean found = false;
	   				if (asIs && (extra = sequencesMatch(data, table,it, ic, sequence, numMismatch))>=0)  // v 1. 12 this had been greater than and thus would miss exact matches
	   					found = true;
   					else if (complement && (extra = sequencesMatch(data, table, it, ic, sequenceC, numMismatch))>=0)
   						found = true;
   					else if (complement && (extra = sequencesMatch(data, table,it, ic, sequenceR, numMismatch))>=0)
   						found = true;
   					else if (reverseComplement && (extra = sequencesMatch(data,table, it, ic, sequenceRC, numMismatch))>=0)
   						found = true;
   					
   					
   					if (found) {
  						charFound.setValue(ic);
  						taxonFound.setValue(it);
				   		for (int i = 0; i<extra + sequence.length(); i++) 
				   			table.selectCell(ic + i, it);
						
  						length.setValue(extra + sequence.length());
  						
   						return true;
   					}
   				}
   			}
   			return false;
   		
   	}
	/*.................................................................................................................*/
   	//-1 if not match, 0 or positive for number of extras if match
   	int sequencesMatch(CharacterData data, MesquiteTable table, int it, int checkChar, String sequence, int numMismatch) {
   		if (sequence == null || data == null || table == null)
   			return -1;
   		int length = sequence.length();
   		if (checkChar + (length)>=data.getNumChars()){ //would extend past end of sequence; go to next taxon
			firstTaxon++;
			if (firstTaxon>= data.getNumTaxa())
				firstTaxon = 0;
			firstChar = 0;
			return -1;
   		}
   		int mismatches = 0;
   		int extra = 0;
   		for (int site= 0; site < length; site++){
   			String cell = null;
   			while (data.isInapplicable(site+checkChar+extra, it) && site+checkChar+extra<data.getNumChars())
   				extra++;
   			if (length+checkChar+extra>=data.getNumChars()){//would extend past end of sequence
				firstTaxon++;
				if (firstTaxon>= data.getNumTaxa())
					firstTaxon = 0;
				firstChar = 0;
   				return -1;
   			}
   			cell = table.getMatrixText(site+checkChar+extra, it);
   			if (cell != null && !cell.equalsIgnoreCase(String.valueOf(sequence.charAt(site)))) {
   				mismatches++;
   				if (mismatches>numMismatch){
	   				firstChar++;
   					return -1;
   				}
   			}
   		}
	   	firstChar++;
   		return extra;
   		
   	}

	/*.................................................................................................................*/
	 public String getName() {
	return "Matching Sequence";
	 }
		/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Matching Sequence...";
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Finds the next occurrence of a sequence in a matrix of molecular data.  Allows a certain number of mismatches." ;
   	 }
   	 
}



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.CompareMatrices; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.ListDialog;

/* ======================================================================== */
public class CompareMatrices extends DataUtilityNoAlterer { 
	CharacterData data;
	TextDisplayer displayer;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
   	/** if returns true, then requests to remain on even after operateData is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return true;
   	}
   	/** Called to operate on the data in all cells.  Returns true if data altered*/
   	public boolean operateOnData(CharacterData data){
		this.data = data;
		Taxa taxa = data.getTaxa();
		int numSets = getProject().getNumberCharMatricesVisible(taxa);
		int numSetsDiff = numSets;
		for (int i = 0; i<numSets; i++) {
			CharacterData pData =getProject().getCharacterMatrixVisible(taxa, i);
			if (pData== data)
				numSetsDiff--;
			else if (pData.getClass() != data.getClass())
				numSetsDiff--;
		}
		if (numSetsDiff<=0) {
			alert("Sorry, there are no other compatible data matrices available for comparison.  If the other matrix is in another file, open the file as a linked file before attempting to compare.");
			return false;
		}
		else {
			Listable[] matrices = new Listable[numSetsDiff];
			int count=0;
			for (int i = 0; i<numSets; i++) {
				CharacterData pData =getProject().getCharacterMatrixVisible(taxa, i);
				if (pData!= data && (pData.getClass() == data.getClass())) {
					matrices[count]=pData;
					count++;
				}
			}
			boolean differenceFound=false;
			CharacterData oData = (CharacterData)ListDialog.queryList(containerOfModule(), "Compare with", "Compare data matrix with:", MesquiteString.helpString,matrices, 0);
			if (oData==null)
				return false;
				
			String result = "Comparison of matrix \"" + data.getName() + "\" with matrix \"" + oData.getName() + StringUtil.lineEnding() + StringUtil.lineEnding();
			if (oData.getNumChars()!=data.getNumChars()) {
				result+= "The matrices differ in number of characters. (" + data.getName() + " with " + data.getNumChars() + " and " + oData.getName() + " with " + oData.getNumChars() + ")";
				CharacterState cs1 = null;
				CharacterState cs2 = null;
				for (int it = 0; it<data.getNumTaxa() && it<oData.getNumTaxa() && !differenceFound; it++){
					boolean firstInTaxon = true;
					for (int ic = 0; ic<data.getNumChars() && ic<oData.getNumChars() && !differenceFound; ic++){
						cs1 = data.getCharacterState(cs1, ic, it);
						cs2 = oData.getCharacterState(cs2, ic, it);
						if (!cs1.equals(cs2, false, true)) {
							if (firstInTaxon){
								log("taxon " + (it+1) + ", characters: ");
								firstInTaxon = false;
							}
							log(" " + (ic+1) + ": " + cs1 + "≠" + cs2);
							differenceFound = true;
							result+= "  The first difference of characters is in taxon " + Taxon.toExternal(it) + " (" + taxa.getTaxonName(it) +  ") and character " + CharacterStates.toExternal(ic) + " (" + data.getName() + ": " + cs1.toString() + "; " + oData.getName() + ": " + cs2.toString() + ")" + StringUtil.lineEnding();
						}
					}
					if (!firstInTaxon)
						logln("");
				}
				differenceFound = true;
			}
			else {
				CharacterState cs1 = null;
				CharacterState cs2 = null;
				int numStateDiffs = 0;
				for (int it = 0; it<data.getNumTaxa() && it<oData.getNumTaxa() && numStateDiffs <1000; it++){
					boolean firstInTaxon = true;
					for (int ic = 0; ic<data.getNumChars() && ic<oData.getNumChars() && numStateDiffs <1000; ic++){
						cs1 = data.getCharacterState(cs1, ic, it);
						cs2 = oData.getCharacterState(cs2, ic, it);
						if (!cs1.equals(cs2, false, true)) {
							if (firstInTaxon){
								log("taxon " + (it+1) + ", characters: ");
								firstInTaxon = false;
							}
							numStateDiffs++;
							differenceFound = true;
							log(" " + (ic+1) + ": " + cs1 + "≠" + cs2);
							result+= "Taxon " + Taxon.toExternal(it) + " (" + taxa.getTaxonName(it) +  "); character " + CharacterStates.toExternal(ic) + " differs (" + data.getName() + ": " + cs1.toString() + "; " + oData.getName() + " with " + cs2.toString() + ")" + StringUtil.lineEnding();
						}
					}
					if (!firstInTaxon)
						logln("");
				}
				if (numStateDiffs>999)
					result += "More than 1000 differences found; suspending comparison.\n";
			}
			if (!differenceFound)
				result+= "No differences were detected";
	 	 	Random rn = new Random(System.currentTimeMillis());
	 	 	String path = MesquiteModule.prefsDirectory + MesquiteFile.fileSeparator + "compare" + Integer.toString(Math.abs(rn.nextInt()));
			MesquiteFile.putFileContents(path, result, true);
			displayFile(path);
			return true;
		}
	}
	/*.................................................................................................................*/
 	public TextDisplayer displayFile(String pathName) {
		displayer = (TextDisplayer)hireEmployee(TextDisplayer.class, null);
		if (displayer!=null)
			displayer.showFile(pathName, -1, true); 
		return displayer;
 	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
 	public void employeeQuit(MesquiteModule employee) {
 		if (employee==displayer)
 			iQuit();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Compare Other Matrix";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Compares matrix in data editor with another.";
   	 }
}


	



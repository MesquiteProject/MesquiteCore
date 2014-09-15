/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.CompareBasesWithTaxon;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class CompareBasesWithTaxon extends DNADataUtility { 
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	/** if returns true, then requests to remain on even after operateData is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
   	/** Called to operate on the data in all cells.  Returns true if data altered*/
   	public boolean operateOnData(CharacterData data){
		this.data = data;
		Taxa taxa = data.getTaxa();
		DNAData dnaData = (DNAData)data;
		String s = "";
		boolean anySelected = taxa.anySelected();
		if (anySelected)
			s = " selected";
		Taxon taxon = taxa.userChooseTaxon(containerOfModule(), "With which taxon to compare the" + s + " taxa?");
		if (taxon !=null){
			int[][] count = new int[4][4];
			Integer2DArray.zeroArray(count);
			int referenceTaxon = taxa.whichTaxonNumber(taxon);
			String names = null;
			int numComp = 0;
			for (int it=0; it<taxa.getNumTaxa(); it++){
				if (!anySelected || taxa.getSelected(it)){
					numComp++;
					if (numComp>1)
						names = "";
					else
						names = " (" + taxa.getTaxonName(it) + ")";	
					for (int ic = 0; ic<dnaData.getNumChars(); ic++){
						long referenceStates = dnaData.getState(ic, referenceTaxon);
						long targetStates = dnaData.getState(ic, it);
						//for each state in reference, if the state is also in target, then don't count
						// if state is not in target, then count one difference from ref to target
						for (int sRef = 0; sRef <4; sRef++){ //go through ACGT
							if (CategoricalState.isElement(referenceStates, sRef)){
								for (int sTarg = 0; sTarg <4; sTarg++){ //go through ACGT
									if (CategoricalState.isElement(targetStates, sTarg)){
										count[sRef][sTarg]++;
									}
								}
							}
						}
					}
				}
			}
			String output = "\nComparing " + taxon.getName() + " (reference) to " + numComp + " species " + names + '\n';
			output+= "Ref.\tStates In Compared Taxa\n\tA\tC\tG\tT            \n";
			output += "A" + countsToString(count[0]) + '\n';
			output += "C" + countsToString(count[1]) + '\n';
			output += "G" + countsToString(count[2]) + '\n';
			output += "T" + countsToString(count[3]) + '\n';
			showLogWindow();
			logln(output);
			return true;
		}
		else
			return false;
	}
	String countsToString(int[] c){
		String s = "";
		for (int i = 0; i<c.length; i++)
			s += "\t" + Integer.toString(c[i]);
		return s;
	}
	
	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Compare Bases with Taxon";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Compares nucleotide bases of selected taxa with those of a chosen taxon.";
   	 }
}


	



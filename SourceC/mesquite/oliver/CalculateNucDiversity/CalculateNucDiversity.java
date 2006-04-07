package mesquite.oliver.CalculateNucDiversity;
/*~~ */
	/*This module will calculate the Nucleotide Diversity for a given set of sequences */

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

/** ===================================================*/

public class CalculateNucDiversity extends DataUtility{
	CharacterState csi = null;	//These two variables will hold the data for comparisons
	CharacterState csj = null;
	
	/*...................................*/
	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){
		return true;
	} // end of public boolean startJob
	/** Called to select taxa */
	
	public boolean operateOnData(CharacterData data, CommandRecord commandRec){
		if (data == null){
			alert("Sorry, Calculate Nucleotide Diversity requires a data matrix");
			return false;
		} // end of statement checking for data matrix
		
		Taxa taxa = data.getTaxa();
		
		int k = 0; // This variable will represent the running sum of Kij
	
		if(taxa != null && taxa.getNumTaxa() > 0){

			outerLoop: for(int taxai = 0; taxai < (taxa.getNumTaxa() - 1); taxai++){
				/** Selects reference sequence */
				middleLoop: for(int taxaj = (taxai + 1); taxaj < taxa.getNumTaxa(); taxaj++){
					innerLoop: for(int sitei = 0; sitei < data.getNumChars(); sitei++){
						csi = data.getCharacterState(csi, sitei, taxai);
						csj = data.getCharacterState(csj, sitei, taxaj);
						if (!csi.equals(csj)){
							k++;
						}// end of conditional comparing the two sequences at the ith site (sitei)
					}// end of innerLoop
				}// end of middleLoop
			}// end of outerLoop
		double pi;
		int n = taxa.getNumTaxa();
		int L = data.getNumChars();
		pi = ((double)k / (((double)(n * (n - 1)))/2))/(double)L;
		logln("Nucleotide diversity = " + pi);
		
		}// end of conditional checking for non-empty matrix
		return true;
	}// end of operateOnData
	
/*...............................................*/
	public String getName() {
		return "Calculate Nucleotide Diversity";
	}// end of getName method
/*...............................................*/
	public String getVersion() {
		return "1.0b";
	}// end of getVersion method
/*...............................................*/
	public boolean requestPrimaryChoice() {
		return true;
	}
/*...............................................*/
/** returns an explanation of what the module does*/
	public String getExplanation (){
		return "Calculates Nucleotide Diversity, or pi, from a DNA sequence matrix.";
	} // end of getExplanation method
} // end of class CalculateNucleotideDiversity

/*Mesquite source code. Copyright 1997-2005 W. Maddison and D. Maddison.*/

package mesquite.oliver.NucleotideDiversity;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.*;

/* ==============================================================================*/
/** Calculates the Nucleotide Diversity in a DNA data matrix.  As an extention of NumberForMatrix, it can work on stored or simulated characters.*/
public class NucleotideDiversity extends NumberForMatrix {

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		return true;
	} // end of startJob method

	public double nucleotideDiversity(MCharactersDistribution data) {
		Taxa taxa = data.getTaxa();
		CharacterState csi = null;	//These two variables will hold the data for comparisons
		CharacterState csj = null;

		int k = 0; // This variable will represent the running sum of Kij
	
		if(taxa != null && taxa.getNumTaxa() > 0){

			// outerLoop selects reference taxon, it:
			outerLoop: for(int it = 0; it < (taxa.getNumTaxa() - 1); it++){

				/*middleLoop selects the taxon to compare, jt */
				middleLoop: for(int jt = (it + 1); jt < taxa.getNumTaxa(); jt++){

					// innerLoop loops through each character, comparing the state of character ic between taxa it and jt
					innerLoop: for(int ic = 0; ic < data.getNumChars(); ic++){
						csi = data.getCharacterState(csi, ic, it);
						csj = data.getCharacterState(csj, ic, jt);
						if (!csi.equals(csj)){
							k++;
						}// end of conditional comparing the two sequences at the ith site (sitei)
					}// end of innerLoop
				}// end of middleLoop
			}// end of outerLoop
		double n = (double)taxa.getNumTaxa();
		double L = (double)data.getNumChars();
		double pi = ((double)k / (((n * (n - 1)))/2))/L;
		// DEBUGGER: logln("Nucleotide diversity = " + pi);
		
		return pi;
		
		}// end of conditional checking for non-empty matrix

		else return -1; // returns a negative value for nucleotide diversity; actual values are between 0 and 1.
		
	} // end of nuceotideDiversity method
		
	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from happening at inopportune times (e.p., while a long chart calculation is in mid-progress*/
	public void initialize(MCharactersDistribution data, CommandRecord commandRec) {
	} // end of initialize method

	/* The calculateNumber method (below) checks for a data matrix, and if one is found, 
	 * calls the nucelotideDiversity method from above: */
	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		if (result == null)
			return;
		if (data.getNumChars()>0) {
			/* calling the nucleotideDiversity method and setting the variable 'result' 
			 * to the double value returned from the nucleotideDiversity method */
			result.setValue(nucleotideDiversity(data)); 
		} // end of true portion of data!=null conditional
		else {
			alert("Sorry, Calculate Nucleotide Diversity requires a DNA data matrix.");
			result.setValue(-1); // DEBUGGER: maybe should do something, like throw an alert if result=-1.
		} // end of false portion of data!=null conditional
		if (resultString!=null)
			resultString.setValue("Nucleotide Diversity: " + result.toString());
		// else
			// DEBUGGER: resultString.setValue("Sorry, Calculate Nucleotide Diversity requires a DNA data matrix.");
	} // end of calculateNumber method

	public boolean isPrerelease (){
		return false;
	} // end of isPrerelease method

	public String getName() {
		return "Nucleotide Diversity";
	} // end of getName method

	public String getVersion(){
		return "1.0b";
	} // end of getVersion method
	
	public String getExplanation(){
		return "Calculates the nucleotide diversity for a given matrix of DNA data.  Current version counts gaps as 5th characters (theoretically; this requires additional testing to determine if it is so).";
	} // end of getExplanation method
	
} // end of NucleotideDiversity class

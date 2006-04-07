/* Mesquite module ~~Copyright 1997-2005 W. & D. Maddison*/
package mesquite.oliver.SelectUniqueTaxa;
/*~~ */

	/*This module identifies and selects the taxon with the first occurrance of a unique character state pattern.  Taxa with identical character state 
	patterns succeeding the sele cted taxon are not selected.  This module	is intendied to be part of a larger module which will allow redundant 
	character patterns to be omitted froma character matrix, whilc retaining information regarding the taxa associated with each unique character 
	state pattern.  Reducing the number of taxa in a matrix will reduce computation time necessary for tree searches.  A detailed description of 
	the process is found at the end of this file.  */

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

/**==========================================================*/

public class SelectUniqueTaxa extends DataUtility {
	CharacterState csi = null;		//These two variables will hold the data for comparsions
	CharacterState csj = null;

	/*..........................................................................................*/

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){
		return true;
	} //end of public boolean startJob

	/** Called to select taxa */

	public boolean operateOnData(CharacterData data, CommandRecord commandRec){
		if (data == null){
			alert("Sorry, Select Unique Taxa requires a data matrix.");
			return false;
		} // end of if statement checking for data matrix
		
		Taxa taxa = data.getTaxa();

		boolean[] unique = new boolean[taxa.getNumTaxa()];

		if (taxa != null && taxa.getNumTaxa() > 0) { // this if statement makes sure that the matrix isn't empty

			outerLoop: for (int i = 0; i <taxa.getNumTaxa(); i++) { 
		
				unique[i] = true;

				middleLoop: for (int j = 0; j < i; j++) {
			
					innerLoop: for (int k = 0; k < data.getNumChars(); k++) {

						csj = data.getCharacterState(csj, k, j);
						csi = data.getCharacterState(csi, k, i);
						if (!csi.equals(csj)) {
							unique[i] = true;
							break innerLoop;
						} else unique[i] = false;

					} // inner (k) loop - over all characters

				if(unique[i] == false){
					// Debugg.println(taxa.getTaxonName(j)+"="+taxa.getTaxonName(i)); TODO: un-comment this for a printout to the Mesquite Log of haplotypes identities, along with Debugg.println below.
					break middleLoop;
				} 

				} // middle (j) loop - over all previous taxa


				taxa.setSelected(i, unique[i]); // selects first occurrance of a unique set of character states
				// if (unique[i])
					// Debugg.println(taxa.getTaxonName(i)+"="+taxa.getTaxonName(i)); TODO: un-comment this for a printout to the Mesquite Log of haplotypes identities, along with conditional on previous line and Debugg.println above.
				
			} // outer (i) loop - over all taxa

			taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED), null);

		} // end of conditional checking for non-empty matrix

		return true;  // returns a value of true from the operateOnData method

	} // end of operateOnData method

/*.....................................................................................*/

	public String getName() { // this method returns the string which should appear in the Utilities menu
		return "Select Unique Taxa";
	} // end of getName method

	/*.....................................................................................*/

	public String getVersion() {
		return "1.0";
	} // end of getVersion method

	/*.....................................................................................*/

	/** returns an explanation of what the module does*/
	public String getExplanation (){
		return "Selects the first taxon of each unique character state pattern (e.g. haplotype).  After this utility is used, you can reverse selection (in 'Select' menu) and delete selected taxa.  This will result in a matrix of unique haplotypes.";
	} // explanation end

		/*Description of the process: A reference sequence is designated, and 
		sequences are compared agains the reference sequence.  Reference 
		sequences are selected increasingly, and compared to those sequences 
		lower in the array.  For example, if the 4th sequence is the reference 
		sequence, it is compared to the 1st, 2nd, and 3rd sequence only, 
		regardless of the number of other sequences in the matrix.  This 
		process has a large amount of redundancy in it, and may need to be 
		resolved.  
		The process starts on the first sequence (which is element 0 in the 
		array) which is defined as unique in the first statement within outerLoop, 
		below.  On the first iteration of the outerLoop, middleLoop is not 
		exectued, because j = 0 = i.  On the second interation of outerLoop, 
		i = 1, and the second sequence is the reference sequence.  This time, 
		because j = 0, and i = 1 (and thus j < i), middleLoop is executed 
		--- documentation incomplete
		*/

} // end of class selectUniqueTaxa

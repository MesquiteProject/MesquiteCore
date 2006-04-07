/* Mesquite module ~~Copyright 1997-2005 W. & D. Maddison*/
package mesquite.oliver.nca.HaplotypeSelector;
/*~~ */

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

/**==========================================================*/

public class HaplotypeSelector extends DataUtility {
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

		int haplotypeIdentity[] = new int[taxa.getNumTaxa()];
		
		boolean[] unique = new boolean[taxa.getNumTaxa()];

		if (taxa != null && taxa.getNumTaxa() > 0) { // this if statement makes sure that the matrix isn't empty

			
			initializeHaplotypeIdentity: for(int initial = 0; initial < taxa.getNumTaxa(); initial++)
				haplotypeIdentity[initial] = initial;
			
			outerLoop: for (int i = 0; i <taxa.getNumTaxa(); i++) { 
		
				unique[i] = true;

				middleLoop: for (int j = 0; j < i; j++) {
			
					innerLoop: for (int k = 0; k < data.getNumChars(); k++) {

						csj = data.getCharacterState(csj, k, j);
						csi = data.getCharacterState(csi, k, i);
						if (!csi.equals(csj)) {
							unique[i] = true;
							break innerLoop;
						} else {
							unique[i] = false;
						}

					} // inner (k) loop - over all characters

					if(unique[i] == false){
						haplotypeIdentity[i] = j;
						Debugg.println(taxa.getName(i) + " is the same as " + taxa.getName(j));
						break middleLoop;
					}
				} // middle (j) loop - over all previous taxa

					taxa.setSelected(i, unique[i]); // selects first occurrance of a unique set of character states

			} // outer (i) loop - over all taxa

			taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED), null);

		} // end of conditional checking for non-empty matrix

		return true;  // returns a value of true from the operateOnData method

	} // end of operateOnData method

/*.....................................................................................*/

	public String getName() { // this method returns the string which should appear in the Utilities menu
		return "Haplotype Selector";
	} // end of getName method

	/*.....................................................................................*/
	public boolean requestPrimaryChoice() {
		return false;
	}
	
	/*.....................................................................................*/
	public String getVersion() {
		return "1.0";
	} // end of getVersion method

	/*.....................................................................................*/

	/** returns an explanation of what the module does*/
	public String getExplanation (){
		return "Selects the first taxon of each unique character state pattern (e.g. haplotype).  After this utility is used, you can invert selection (in Blah blah menu) and delete selected taxa.  This will result in a matrix of unique haplotypes.";
	} // explanation end

	
} // end of class selectUniqueTaxa

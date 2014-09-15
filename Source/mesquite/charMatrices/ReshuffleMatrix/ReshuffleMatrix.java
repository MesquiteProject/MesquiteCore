/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ReshuffleMatrix;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.charMatrices.lib.*;

/* ======================================================================== */
public class ReshuffleMatrix extends RandomMatrixModifier {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  	 	return true; 
  	 }

	/*.................................................................................................................*/
  	public void modifyMatrix(MCharactersDistribution matrix, MAdjustableDistribution modified, RandomBetween rng){
		if (matrix==null || modified == null)
			return;
		int numTaxa = matrix.getNumTaxa();
		int numChars = matrix.getNumChars();
		if (modified.getNumTaxa()!=numTaxa || modified.getNumChars()!=numChars)
			modified.setSize(numChars, numTaxa);

		for (int ic = 0; ic<numChars; ic++) {
	   		modified.transferFrom(ic, matrix.getCharacterDistribution(ic));
	   		for (int i=0; i < (numTaxa-1); i++) {
	   			int sh = rng.randomIntBetween(i, numTaxa-1);
	   			if (sh != i)
	   				modified.tradeStatesBetweenTaxa(ic, i, sh);
	   		}
 	 	}
   	}
	/*.................................................................................................................*/
    public String getName() {
   		return "Reshuffle States Within Characters";
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
 	/*.................................................................................................................*/
 	 public boolean requestPrimaryChoice() {
		return true;
  	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Shuffles (permutes) character states among taxa within each character." ;
   	 }
   	 
}


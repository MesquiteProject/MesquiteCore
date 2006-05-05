/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.
Version 1.1, May 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ReshuffleWTaxaByPart;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.charMatrices.lib.*;

/* ======================================================================== */
public class ReshuffleWTaxaByPart extends RandomMatrixModifier {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
  	 	return true; 
  	 }

	/*.................................................................................................................*/
  	public void modifyMatrix(MCharactersDistribution matrix, MAdjustableDistribution modified, RandomBetween rng, CommandRecord commandRec){
  		mmByPart(matrix, modified, rng, commandRec);
   	}
	/*.................................................................................................................*/
  	public void mm(MCharactersDistribution matrix, MAdjustableDistribution modified, RandomBetween rng, CommandRecord commandRec){
		if (matrix==null || modified == null)
			return;
		int numTaxa = matrix.getNumTaxa();
		int numChars = matrix.getNumChars();
		if (modified.getNumTaxa()!=numTaxa || modified.getNumChars()!=numChars)
			modified.setSize(numChars, numTaxa);

		for (int ic = 0; ic<numChars; ic++) {
	   		modified.transferFrom(ic, matrix.getCharacterDistribution(ic));
 	 	}
		CharacterState cs1 = null;
		CharacterState cs2 = null;
	   	for (int it=0; it < (numTaxa-1); it++) {
	   		for (int ic = 0; ic<numChars; ic++) {
	   			int sh = rng.randomIntBetween(ic, numChars-1);
	   			if (sh != ic) {
	   				cs1 = modified.getCharacterState(cs1, ic, it);
	   				cs2 = modified.getCharacterState(cs2, sh, it);
	   				modified.setCharacterState(cs1, sh, it);
	   				modified.setCharacterState(cs2, ic, it);
	   			}
	   		}
 	 	}
   	}
	/*.................................................................................................................*/
  	public void mmByPart(MCharactersDistribution matrix, MAdjustableDistribution modified, RandomBetween rng, CommandRecord commandRec){
		if (matrix==null || modified == null)
			return;
			
		CharacterData data = matrix.getParentData();
		if (data == null) {
			mm(matrix, modified, rng, commandRec);
		return;
  	}
		
		CharacterPartition partition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		CharactersGroup[] groups = null;
		if (partition != null)
			groups = partition.getGroups();
		if (data == null){
			mm(matrix, modified, rng, commandRec);
			return;
		}
		
		CharacterState cs1=null, cs2=null;
		int numTaxa = matrix.getNumTaxa();
		int numChars = matrix.getNumChars();
		if (modified.getNumTaxa()!=numTaxa || modified.getNumChars()!=numChars)
			modified.setSize(numChars, numTaxa);
		
			for (int ic = 0; ic<numChars; ic++) 
		   		modified.transferFrom(ic, matrix.getCharacterDistribution(ic));
			for (int it = 0; it<numTaxa; it++) {
		   		for (int g = 0; g< groups.length; g++){ //examining group
		   			int numCharInGroup = partition.getNumberInGroup(groups[g]);
		   			
			   		for (int i=0; i < (numCharInGroup-1); i++) {
			   			int sh = rng.randomIntBetween(i, numCharInGroup-1);
			   			if (sh != i){
			   				int ic1 = getCharacterNumber(i, groups[g], numChars, partition);
			   				int ic2 = getCharacterNumber(sh, groups[g], numChars, partition);
			   				cs1 = modified.getCharacterState(cs1, ic1, it);
			   				cs2 = modified.getCharacterState(cs2, ic2, it);
			   				modified.setCharacterState(cs1, ic2, it);
			   				modified.setCharacterState(cs2, ic1, it);
			   		
			   			}
			   		}
		   		}
	   			int numCharInGroup = partition.getNumberInGroup(null);
	   			
		   		for (int i=0; i < (numCharInGroup-1); i++) {
		   			int sh = rng.randomIntBetween(i, numCharInGroup-1);
		   			if (sh != i){
		   				int ic1 = getCharacterNumber(i, null, numChars, partition);
		   				int ic2 = getCharacterNumber(sh, null, numChars, partition);
		   				cs1 = modified.getCharacterState(cs1, ic1, it);
		   				cs2 = modified.getCharacterState(cs2, ic2, it);
		   				modified.setCharacterState(cs1, ic2, it);
		   				modified.setCharacterState(cs2, ic1, it);
		   		
		   			}
		   		}
	 	 	}
 	 	
   	}
   	int getCharacterNumber(int numInGroup, CharactersGroup group, int numChars, CharacterPartition partition){
   		int count = 0;
   		for (int ic = 0; ic<numChars; ic++){
   			if (partition.getCharactersGroup(ic) == group){
   				if (count == numInGroup)
   					return ic;
   				count++;
   			}
   		}
   		return -1;
   	}
   	
   	
	/*.................................................................................................................*/
    	 public String getName() {
   		//return "Reshuffle Matrix Within Taxa";
   		return "Reshuffle Within Taxa (Char. Partitioned)";
   	 }
	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    	public int getVersionOfFirstRelease(){
    		return 110;  
    	}
    	/*.................................................................................................................*/
    	public boolean isPrerelease(){
    		return false;
    	}

	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
 	/*.................................................................................................................*/
 	 public boolean requestPrimaryChoice() {
		return false;
  	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Shuffles (permutes) character states among characters of each character partition, within each taxon." ;
   	 }
   	 
}

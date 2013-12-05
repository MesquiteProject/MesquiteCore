/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.PercentGaps;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class PercentGaps extends NumberForCharacter  implements NumForCharTreeIndep { 
	double resultNum;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(CharacterDistribution charStates){
   	}
	/*.................................................................................................................*/
	public  void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		int numTaxa =charStates.getNumTaxa();
		int tot = 0;
		int count=0;
		for (int it = 0; it<numTaxa; it++) {
			if (charStates.isInapplicable(it))
				count++;
			if (!charStates.isUnassigned(it))
				tot++;
		}
		resultNum = 1.000*count/tot;
		result.setValue(resultNum);
		if (resultString!=null)
			resultString.setValue("Proportion Gaps: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Proportion Gaps In Character";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the proportion of gaps (inapplicable codings) in a character across taxa.  Does not include missing (unassigned) data." ;
   	 }
   	 
}


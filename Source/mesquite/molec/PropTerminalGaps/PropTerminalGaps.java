/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.PropTerminalGaps;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class PropTerminalGaps extends NumberForCharacter  implements NumForCharTreeIndep { 
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
		CharacterData data = charStates.getParentData();
		int ic = charStates.getParentCharacter();
		if (data == null || ic<0){
			if (resultString != null)
				resultString.setValue("Cannot calculate prop. terminal gaps because no parent matrix associated with character");
			return;
		}
			
		int numTaxa =charStates.getNumTaxa();
		int tot = 0;
		int count=0;
		for (int it = 0; it<numTaxa; it++) {
			tot++;
			if (isTerminalInapplicable(data, ic, it))
				count++;
		}
		resultNum = 1.000*count/tot;
		result.setValue(resultNum);
		if (resultString!=null)
			resultString.setValue("Proportion Terminal Gaps: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	private boolean isTerminalInapplicable(CharacterData data, int ic, int it){
		if (!data.isInapplicable(ic, it))
			return false;
		if (ic == 0 || ic == data.getNumChars()-1)
			return true;
		if (ic>data.getNumChars()/2){
			if (clearRight(data, ic, it))
				return true;
			if (clearLeft(data, ic, it))
				return true;
		}
		else {
			if (clearLeft(data, ic, it))
				return true;
			if (clearRight(data, ic, it))
				return true;
		}
		return false;
		
	}
	boolean clearLeft(CharacterData data, int ic, int it){
		for (int i = ic-1; i>=0; i--)
			if (!data.isInapplicable(i, it))
				return false;
		return true;
	}
	boolean clearRight(CharacterData data, int ic, int it){
		for (int i = ic+1; i<data.getNumChars(); i++)
			if (!data.isInapplicable(i, it))
				return false;
		return true;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Proportion Terminal Gaps";
   	 }
    		/*.................................................................................................................*/
    		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
    		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
    		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    		public int getVersionOfFirstRelease(){
    			return 200;  
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
 		return "Calculates the proportion of Terminal gaps (inapplicable codings) in a character across taxa.  Does not include missing (unassigned) data." ;
   	 }
   	 
}


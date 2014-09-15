/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.PercentPolymorphic;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class PercentPolymorphic extends NumberForCharacter  implements NumForCharTreeIndep { 
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
		if (result==null || charStates == null)
			return;
		clearResultAndLastResult(result);
		if (!(charStates instanceof CategoricalDistribution)){
			if (resultString != null)
				resultString.setValue("Polymorphism can be calculated only for categorical characters");
			return;
		}
		CategoricalDistribution states = (CategoricalDistribution)charStates;
		int numTaxa =states.getNumTaxa();
		int tot = 0;
		int count=0;
		for (int it = 0; it<numTaxa; it++) {
			long s = states.getState(it);
			if (!CategoricalState.isInapplicable(s)){ //1. 06 changed to exclude inapplicable
				tot++;
				if (CategoricalState.hasMultipleStates(s))
					count++;
			}
		}
		resultNum = 1.0*count/tot;
		result.setValue(resultNum);
		if (resultString!=null)
			resultString.setValue("Proportion Polymorphic: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Proportion Polymorphic";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Calculates the proportion of taxa coded as polymorphic or partially uncertain in a character." ;
	}

}


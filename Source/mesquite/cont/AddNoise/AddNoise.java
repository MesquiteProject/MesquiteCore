/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.AddNoise;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;
//import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class AddNoise extends ContDataAlterer implements AltererRandomizations {
	CharacterState fillState;
	Random rng;
	double standardDeviation;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new Random();
		rng.setSeed(System.currentTimeMillis());
		return true;
	}
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
			if (!(data instanceof ContinuousData))
				return false;
			standardDeviation = MesquiteDouble.queryDouble(containerOfModule(), "Add Noise", "Variance of noise", 1.0);
			if (!MesquiteDouble.isCombinable(standardDeviation) || standardDeviation<=0)
				return false;
			standardDeviation = Math.sqrt(standardDeviation);
			return alterContentOfCells(data,table, undoReference);
   	}

	/*.................................................................................................................*/
   	public void alterCell(CharacterData data, int ic, int it){
   		((ContinuousData)data).setState(ic,it, 0, ((ContinuousData)data).getState(ic, it,0)+ standardDeviation*rng.nextGaussian());
   	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add Noise (Gaussian)";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Adds normally distributed noise to existing states.  Only the first item of a continuous matrix is modified." ;
   	 }
   	 
}



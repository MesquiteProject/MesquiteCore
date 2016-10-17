/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.RandomFillCont;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.table.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
//import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class RandomFillCont extends ContDataAlterer implements AltererRandomizations {
	CharacterState fillState;
	Random rng;
	MesquiteDouble mean;
	MesquiteDouble variance;
	double standardDeviation;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new Random();
		rng.setSeed(System.currentTimeMillis());
		mean = new MesquiteDouble(0.0);
		variance = new MesquiteDouble(1.0);
		standardDeviation = 1.0;
		return true;
	}
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
			
   			boolean did=false;
			if (!(data instanceof ContinuousData))
				return false;
			MesquiteBoolean answer = new MesquiteBoolean(true);
			MesquiteDouble.queryTwoDoubles(containerOfModule(), "Random fill", "Mean of filled states", "Variance of filled states", answer, mean, variance);
			if (!answer.getValue() && variance.getValue()>=0 && (variance.isCombinable()))
				return false;
			standardDeviation = Math.sqrt(variance.getValue());
			ContinuousData cData = (ContinuousData)data;
			return alterContentOfCells(data,table, undoReference);
   	}

	/*.................................................................................................................*/
   	public void alterCell(CharacterData data, int ic, int it){
   		((ContinuousData)data).setState(ic,it, 0, standardDeviation*rng.nextGaussian()+ mean.getValue());
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Random Fill (Gaussian/Normal)";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Fills cells with a random state, using a Normal distribution." ;
   	 }
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
   	 
}



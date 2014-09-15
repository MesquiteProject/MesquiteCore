/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.cont.RandomFillUniform;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.table.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
//import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class RandomFillUniform extends ContDataAlterer {
	CharacterState fillState;
	Random rng;
	MesquiteDouble max;
	MesquiteDouble min;
	double standardDeviation;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new Random();
		rng.setSeed(System.currentTimeMillis());
		min = new MesquiteDouble(0.0);
		max = new MesquiteDouble(1.0);
		return true;
	}
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){

		boolean did=false;
		if (!(data instanceof ContinuousData))
			return false;
		MesquiteBoolean answer = new MesquiteBoolean(true);
		MesquiteDouble.queryTwoDoubles(containerOfModule(), "Random fill (Uniform)", "Minimum of filled states", "Maximum of filled states", answer, min, max);
		if (!answer.getValue() && min.isCombinable() && (max.isCombinable()) && max.getValue()>=min.getValue())
			return false;
		ContinuousData cData = (ContinuousData)data;
		return alterContentOfCells(data,table, undoReference);
	}

	/*.................................................................................................................*/
	public void alterCell(CharacterData data, int ic, int it){
		((ContinuousData)data).setState(ic,it, 0, rng.nextDouble()*(max.getValue() - min.getValue())+ min.getValue());
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Random Fill (Uniform)";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Random Fill (Uniform)...";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Fills cells with a random state, uniformly." ;
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



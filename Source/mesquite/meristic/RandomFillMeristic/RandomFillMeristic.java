package mesquite.meristic.RandomFillMeristic;

/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
/*~~  */


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.meristic.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class RandomFillMeristic extends MeristicDataAlterer {
	CharacterState fillState;
	RandomBetween rng;
	MesquiteInteger max;
	MesquiteInteger min;
	double standardDeviation;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new RandomBetween(System.currentTimeMillis());
		min = new MesquiteInteger(0);
		max = new MesquiteInteger(10);
		return true;
	}
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){

		boolean did=false;
		if (!(data instanceof MeristicData))
			return false;
		MesquiteBoolean answer = new MesquiteBoolean(true);
		MesquiteInteger.queryTwoIntegers(containerOfModule(), "Random fill (Meristic Uniform)", "Minimum of filled states", "Maximum of filled states", answer, min, max, 0, MeristicState.infinite, 0, MeristicState.infinite, "");
		if (!(answer.getValue() && min.isCombinable() && (max.isCombinable()) && max.getValue()>=min.getValue()))
			return false;
		MeristicData cData = (MeristicData)data;
		return alterContentOfCells(data,table, undoReference);
	}

	/*.................................................................................................................*/
	public void alterCell(CharacterData data, int ic, int it){
		((MeristicData)data).setState(ic,it, 0, rng.randomIntBetween(min.getValue(),max.getValue()));
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Random Fill (Meristic Uniform)";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Random Fill (Meristic Uniform)...";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 250;  
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




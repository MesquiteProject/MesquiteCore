package mesquite.charMatrices.RandomFillWithMissing;

/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
/*~~  */

import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class RandomFillWithMissing extends CategDataAlterer implements AltererRandomizations {
	Random rng;

	double probAlter = 1.0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new Random();
		rng.setSeed(System.currentTimeMillis());
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Convert Some Cells to Missing",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Convert Some Cells to Missing");
		DoubleField probAlterField = dialog.addDoubleField("Probability of converting a cell to missing data: ", probAlter, 6);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			probAlter = probAlterField.getValue();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData data, int ic, int it){
		double alterTest = rng.nextDouble();
		if (alterTest<=probAlter) {
			((CategoricalData)data).setState(ic,it,CategoricalState.unassigned);
			if (!MesquiteLong.isCombinable(numCellsAltered))
				numCellsAltered = 0;
			numCellsAltered++;
		}
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		if (!MesquiteThread.isScripting())
			if (!queryOptions())
				return USER_STOPPED;
		return alterContentOfCells(data,table, undoReference);
	}

	//	Double d = new Double(value);

	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false; 
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Sprinkle Missing in Cells...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Sprinkle Missing in Cells";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Fills cells with a missing data with a specified probability." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 201;  
	}

}



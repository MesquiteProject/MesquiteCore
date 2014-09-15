/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.RandomFillCateg;
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
public class RandomFillCateg extends CategDataAlterer {
	CharacterState fillState;
	int maxState = 1;
	Random rng;

	double probAlter = 1.0;
	boolean alterOnlyNonGapCells = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new Random();
		rng.setSeed(System.currentTimeMillis());
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean queryOptions(boolean queryMaxState) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Random Fill",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Random Fill");
		dialog.appendToHelpString("Each state is chosen equiprobably when filling the matrix randomly.");
		IntegerField maximumStateField =null;
		if (queryMaxState)
			maximumStateField = dialog.addIntegerField("Maximum State:", maxState, 8);
		DoubleField probAlterField = dialog.addDoubleField("Probability of altering each cell: ", probAlter, 6);
		Checkbox alterOnlyNonGapCellsBox = dialog.addCheckBox("fill only those cells currently containing data or missing values", alterOnlyNonGapCells);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			if (queryMaxState && maximumStateField!=null)
				maxState = maximumStateField.getValue();
			probAlter = probAlterField.getValue();
			alterOnlyNonGapCells = alterOnlyNonGapCellsBox.getState();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData data, int ic, int it){
		double alterTest = rng.nextDouble();
		if (alterTest<=probAlter){
			if (alterOnlyNonGapCells) {
				if (!((CategoricalData)data).isInapplicable(ic,it))
					((CategoricalData)data).setState(ic,it, randomState());
			}
			else ((CategoricalData)data).setState(ic,it, randomState());
			if (!MesquiteLong.isCombinable(numCellsAltered))
				numCellsAltered = 0;
			numCellsAltered++;
		}
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		if (!(data instanceof CategoricalData))
			return false;
		boolean queryState=false;
		if (data instanceof DNAData)
			maxState = 3;
		else if (data instanceof ProteinData)
			maxState = 19;
		else {	
			maxState=1;
			queryState = true;
		}
		if (!MesquiteThread.isScripting())
			if (!queryOptions(queryState))
				return false;
		/*		else {
				maxState = MesquiteInteger.queryInteger(containerOfModule(), "Maximum State", "Each state is chosen equiprobably when filling the matrix randomly.  What should be the maximum state value?", 1, 1, 50, true);
				if (!MesquiteInteger.isCombinable(maxState))
					return false;
			}
		 */
		return alterContentOfCells(data,table, undoReference);
	}

	//	Double d = new Double(value);

	/*.................................................................................................................*/
	long randomState(){
		double value = rng.nextDouble() * (maxState+1);
		int e = (int)value;
		if ((e>=0)&&(e<=CategoricalState.maxCategoricalState))
			return CategoricalState.makeSet(e);
		else {
			return CategoricalState.makeSet(0);
		}
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
	public String getName() {
		return "Random Fill (Categorical)";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Random Fill (Categorical)...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Fills cells with a randomly-chosen state. For DNA data, states A, C, G, and T are chosen with equal probability; for other data, states up to and including the maximum state value are chosen with equal probability." ;
	}

}




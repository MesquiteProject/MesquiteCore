/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.ConvertToRY;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class ConvertToRY extends DNADataAlterer {
	MesquiteTable table;
	DNAState charState = new DNAState();

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		this.table = table;
		if (!(data instanceof DNAData)){
			MesquiteMessage.warnProgrammer("Can use " + getName() + " only on nucleotide data");
			return false;
		}
		return alterContentOfCells(data,table, undoReference);
	}
	

	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
		DNAData data = (DNAData)ddata;
		charState = (DNAState) data.getCharacterState(charState, ic, it);
		long newSet = 0;
		boolean hasData = false;
		if (charState.hasPurine()) {
			newSet = charState.addToSet(newSet, 0); //A
			newSet = charState.addToSet(newSet, 2); //G
			hasData = true;
		}
		if (charState.hasPyrimidine()) {
			newSet = charState.addToSet(newSet, 1); //C
			newSet = charState.addToSet(newSet, 3); //T
			hasData = true;
		}
		if (hasData) {
			newSet = charState.setUncertainty(newSet, true);
			data.setState(ic, it, newSet);
			if (charState.getValue()!=newSet) {
				if (!MesquiteLong.isCombinable(numCellsAltered))
					numCellsAltered = 0;
				numCellsAltered++;
			}
		}
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Convert to RY";
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
		return "Converts nucleotide data to RY data in selected region." ;
	}

}


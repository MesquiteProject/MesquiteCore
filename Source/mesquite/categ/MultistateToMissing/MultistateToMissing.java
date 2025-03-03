/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.categ.MultistateToMissing;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class MultistateToMissing extends CategDataAlterer implements AltererConvertGapMissPolyUncert {
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
		if (!(data instanceof CategoricalData)){
			MesquiteMessage.warnProgrammer("Attempt to convert multistate characters to missing in non-categorical data");
			return ResultCodes.INCOMPATIBLE_DATA;
		}
		return alterContentOfCells(data,table,undoReference);
	}
	/*.................................................................................................................*/
	public void alterCell(CharacterData ddata, int ic, int it){
		CategoricalData data = (CategoricalData)ddata;
		long s = data.getState(ic,it);
		if (CategoricalState.cardinality(s)<=1)
			return;
		data.setState(ic, it, CategoricalState.unassigned);
		if (!MesquiteLong.isCombinable(numCellsAltered))
			numCellsAltered = 0;
		numCellsAltered++;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Convert Uncert./Polymorph. to Missing";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Alters categorical data by converting uncertain or polymorphic entries to missing data." ;
	}

}



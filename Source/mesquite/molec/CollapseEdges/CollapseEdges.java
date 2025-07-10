/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.CollapseEdges;
/*~~  */

import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.MolecularDataAlterer;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererWholeCharacterAddRemove;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAltererParallelizable;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public class CollapseEdges extends MolecularDataAlterer implements AltererWholeCharacterAddRemove, DataAltererParallelizable{
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData cData, MesquiteTable table,  UndoReference undoReference){
		if (!(cData instanceof MolecularData))
			return ResultCodes.INCOMPATIBLE_DATA;
		MolecularData data = (MolecularData)cData;
		boolean changed = data.stripRightTerminalGaps(false);
		boolean leftChanged = data.stripLeftTerminalGaps(false);
		changed = changed || leftChanged;
		if (changed)
			return ResultCodes.SUCCEEDED;
			return ResultCodes.MEH;
   	}
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Remove Terminal Gaps-Only Characters";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Remove Terminal Gaps-Only Characters";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Removes characters at edges of matrix that are gaps only." ;
   	 }
   	 
}



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.Fill; 

import mesquite.lib.MesquiteString;
import mesquite.lib.ResultCodes;
import mesquite.lib.StringUtil;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererSimpleCell;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.DataAlterer;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public class Fill extends DataAlterer implements AltererSimpleCell {
	CharacterState fillState=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public void alterCell(CharacterData data, int ic, int it){
   		if (fillState!= null)
   			data.setState(ic,it, fillState);
   	}
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData data, MesquiteTable table,UndoReference undoReference){
  		String fillString;
		if ((table==null && data!=null)||!table.anyMainTableCellSelected())
			fillString = MesquiteString.queryString(containerOfModule(), "Fill all cells", "Fill entire matrix with states:", "");
		else
			fillString = MesquiteString.queryString(containerOfModule(), "Fill selected cells", "Fill selected cells with states:", "");
		if (StringUtil.blank(fillString))
			return ResultCodes.USER_STOPPED;
		
   		fillState = data.getCharacterState(fillState, 0, 0); //just to have a template
   		fillState.setValue(fillString, data);
		if (fillState.isImpossible()) 
			return -20;
		return alterContentOfCells(data,table, undoReference);
   	}

   	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Fill";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Fill...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Fills cells with a uniform state in a character data editor." ;
   	 }
   	 
}



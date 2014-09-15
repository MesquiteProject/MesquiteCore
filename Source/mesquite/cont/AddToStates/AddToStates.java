/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.AddToStates;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.table.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class AddToStates extends ContDataAlterer {
	double translateAmount =0.0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
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
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
			if (!(data instanceof ContinuousData))
				return false;
			double d = MesquiteDouble.queryDouble(containerOfModule(), "Add value", "Add to values in matrix:", translateAmount);
			if (MesquiteDouble.isCombinable(d))
				translateAmount = d;
			else
				return false;
			return alterContentOfCells(data,table, undoReference); //todo: in future pass object here which will be passed to alterCell; avoids sloppy practice of relying on "global" variable translateAmount
   	}

   	
   	public void alterCell(CharacterData ddata, int ic, int it){
		ContinuousData data = (ContinuousData)ddata;
		for (int item = 0; item<data.getNumItems(); item++){
			double state = data.getState(ic,it, item);
			if (MesquiteDouble.isCombinable(state))
				data.setState(ic,it, item, state+translateAmount);
		}
	}
	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add value";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Alters continuous data by adding a value.  All items of the matrix are similarly modified." ;
   	 }
   	 
}



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.MultiplyStates;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.table.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class MultiplyStates extends ContDataAlterer  implements AltererContinuousTransformations {
	double scalingFactor = 1.0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
		
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
   			boolean did=false;
			if (!(data instanceof ContinuousData))
				return false;
			double d = MesquiteDouble.queryDouble(containerOfModule(), "Multiply values", "Multiply values in matrix by:", scalingFactor); //todo: have dialog also ask to what item; also in other similar ContDataAlterer's
			if (MesquiteDouble.isCombinable(d))
				scalingFactor = d;
			else
				return false;
			return alterContentOfCells(data,table, undoReference);
   	}

	/*.................................................................................................................*/
   	public void alterCell(CharacterData ddata, int ic, int it){
		ContinuousData data = (ContinuousData)ddata;
		for (int item = 0; item<data.getNumItems(); item++){
			double state = data.getState(ic,it, item);
			if (MesquiteDouble.isCombinable(state))
				data.setState(ic,it, item, state*scalingFactor);
		}
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
    	 public String getName() {
		return "Multiply by value";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Alters continuous data by multiplying by a value.  All items of the matrix are similarly modified." ;
   	 }
   	 
}



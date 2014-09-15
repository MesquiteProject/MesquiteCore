/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectVariableCharacters;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SelectVariableCharacters extends CharacterSelector {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
	boolean variableAmongSelectedTaxa(Taxa taxa, boolean all, CharacterData data, int ic){
		CharacterState fcs = null;
		CharacterState cs = null;
		boolean first = true;
		for (int it = 0; it<taxa.getNumTaxa(); it++) {
			if (taxa.getSelected(it) || all)
				if (!data.isUnassigned(ic, it)){ //only start in first assigned
					if (first)
						fcs =  data.getCharacterState(null, ic, it);
					first = false;
					cs = data.getCharacterState(cs, ic, it);
					if (!cs.equals(fcs, true))
						return true;
				}
		}
		return false;
	}
	
	
   	/** Called to select characters*/
   	public void selectCharacters(CharacterData data){
   		if (data!=null && data.getNumChars()>0){
   			Taxa taxa = data.getTaxa();
   			boolean anyTaxaSelected = taxa.anySelected();
   			for (int i=0; i<data.getNumChars(); i++) {
   				if (variableAmongSelectedTaxa(taxa, !anyTaxaSelected, data, i))
   					data.setSelected(i, true);
   			}
   			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
   		}
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Variable among taxa";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects characters that are variable among the selected taxa" ;
   	 }
   	 
}



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
package mesquite.categ.CategVariable;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.ParsimonyModelSet;
import mesquite.categ.lib.*;

public class CategVariable extends BooleanForCharacter {

	public String getName() {
		return "Variable (Mult. States)";
	}
	public String getExplanation() {
		return "Indicates whether a character has multiple observed states.  If taxa are selected, only the selected taxa are examined.";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}

	public int charIsVariable(CharacterData data, int ic) {
		CategoricalData cData = (CategoricalData)data;
		long intersection = CategoricalState.statesBitsMask;
		boolean anySel = cData.getTaxa().anySelected();
		for (int it=0; it<cData.getNumTaxa(); it++){
			if (!anySel || cData.getTaxa().getSelected(it)){
				long state = cData.getState(ic, it);
				if (CategoricalState.isCombinable(state)){
					if (CategoricalState.cardinality(state)>1){ //polymorphic or uncertain
						if (CategoricalState.isUncertain(state)){ //uncertain; ok if overlaps
							intersection &= state;
							if (intersection == 0L)
								return 1;
						}
						else
							return 1;
					}
					else {
						intersection &= state;
						if (intersection == 0L)
							return 1;
					}
				}
			}
		}
		return 0;
	}


	public void calculateBoolean(CharacterData data, int ic, MesquiteBoolean result, MesquiteString resultString) {
		if (data==null || result==null)
			return;
		result.setToUnassigned();
		if (!(data instanceof CategoricalData))
			return;
		Taxa taxa = data.getTaxa();
		int inform = charIsVariable(data,ic);

		if (inform==0)
			result.setValue(false);
		else if (inform==1)
			result.setValue(true);
	}

	/*.................................................................................................................*/
	public String getValueString(boolean on){
		if (on) return "multiple states";
		else return "0 or 1 states";
	}


	public boolean isPrerelease() {
		return false;
	}


}

/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.72, December 2009.
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

	public void calculateBoolean(CharacterData data, int ic, MesquiteBoolean result, MesquiteString resultString) {
		if (data==null || result==null)
			return;
		result.setToUnassigned();
		if (!(data instanceof CategoricalData))
			return;
		Taxa taxa = data.getTaxa();
		CategoricalData cData = (CategoricalData)data;

		result.setValue(cData.charIsVariable(ic, true));
	}

	/*.................................................................................................................*/
	public String getTrueString(){
		return "Multiple States";
	}
	/*.................................................................................................................*/
	public String getFalseString(){
		return "² 1 state";
	}


	public boolean isPrerelease() {
		return false;
	}


}

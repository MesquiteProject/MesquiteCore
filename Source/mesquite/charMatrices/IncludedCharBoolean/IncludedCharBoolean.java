/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.IncludedCharBoolean;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class IncludedCharBoolean extends BooleanForCharacter {

	public String getName() {
		return "Character Included";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public void calculateBoolean(CharacterData data, int ic, MesquiteBoolean result, MesquiteString resultString) {
		if (data==null || result==null)
			return;
		boolean included = data.isCurrentlyIncluded(ic);

		result.setValue(included);
		resultString.setValue(getValueString(included));
	}
	/*.................................................................................................................*/
	public boolean displayTrueAsDark(){
		return false;
	}
	
	/*.................................................................................................................*/
	public String getTrueString(){
		return "Included";
	}
	/*.................................................................................................................*/
	public String getFalseString(){
		return "Excluded";
	}

	public String getExplanation() {
		return "A boolean that is true if a character is currently included, and false if the character is currently excluded.";
	}


	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}

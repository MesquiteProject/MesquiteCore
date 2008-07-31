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

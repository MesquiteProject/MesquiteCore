package mesquite.thinkTrees.InformCharBoolean;

import mesquite.categ.lib.CategoricalData;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.BooleanForCharacter;

public class InformCharBoolean extends BooleanForCharacter {

	public String getName() {
		return "Parsimony Informative (Unordered)";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}



	
	public void calculateBoolean(CharacterData data, int ic, MesquiteBoolean result, MesquiteString resultString) {
		if (data==null || result==null)
			return;
		if (!(data instanceof CategoricalData))
			return;
		boolean inform = ((CategoricalData)data).charIsUnorderedInformative(ic);

			result.setValue(inform);
		if (resultString!=null)
			resultString.setValue(getValueString(inform));
	}
	/*.................................................................................................................*/
	public String getTrueString(){
		return "Informative";
	}
	/*.................................................................................................................*/
	public String getFalseString(){
		return "Uninformative";
	}

	public boolean isPrerelease() {
		return false; 
	}
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return 330;  
 	}


}

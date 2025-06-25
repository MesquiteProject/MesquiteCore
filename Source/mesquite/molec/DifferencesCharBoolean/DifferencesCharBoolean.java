package mesquite.molec.DifferencesCharBoolean;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.BooleanForCharacter;

public class DifferencesCharBoolean extends BooleanForCharacter {

	public String getName() {
		return "Differences Present";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*..........................................  CategoricalData  ..................................................*/
	public boolean charHasDifferences(CategoricalData data, int ic, boolean selectedOnly, boolean considerAllVariants) {
		long intersection = CategoricalState.statesBitsMask;
		boolean anySel = data.getTaxa().anySelected();
		long firstState = CategoricalState.impossible;
		for (int it=0; it<data.getNumTaxa(); it++){
			if (!selectedOnly || !anySel || data.getTaxa().getSelected(it)){
				long state = data.getState(ic, it);
				if (firstState==CategoricalState.impossible)
					firstState=state;
				if (considerAllVariants && state!=firstState && firstState!=CategoricalState.inapplicable && state!=CategoricalState.inapplicable)
					return true;
				if (CategoricalState.isCombinable(state)){
					if (CategoricalState.cardinality(state)>1){ //polymorphic or uncertain
						if (CategoricalState.isUncertain(state)){ //uncertain; ok if overlaps
							intersection &= state;
							if (intersection == 0L)
								return true;
						}
						else
							return true;
					}
					else {
						intersection &= state;
						if (intersection == 0L)
							return true;
					}
				}
				if (data.isInternalInapplicable(ic, it) && data.hasDataForCharacter(ic, selectedOnly))
					return true;
			}
		}
		return false;
	}


	
	public void calculateBoolean(CharacterData data, int ic, MesquiteBoolean result, MesquiteString resultString) {
		if (data==null || result==null)
			return;
		if (!(data instanceof CategoricalData))
			return;
		CategoricalData cData = (CategoricalData)data;
		boolean differences = charHasDifferences(cData, ic, true, true);

			result.setValue(differences);
		if (resultString!=null)
			resultString.setValue(getValueString(differences));
	}
	/*.................................................................................................................*/
	public String getTrueString(){
		return "Differences Present";
	}
	/*.................................................................................................................*/
	public String getFalseString(){
		return "No Differences";
	}

	public String getExplanation() {
		return "A boolean that is true if a character has variation in states, or some with states and some with gaps/missing data; taxa without any data are not considered.";
	}

	public boolean isPrerelease() {
		return false;
	}
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return NEXTRELEASE;  
 	}


}

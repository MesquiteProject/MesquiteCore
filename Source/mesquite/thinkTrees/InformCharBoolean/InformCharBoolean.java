package mesquite.thinkTrees.InformCharBoolean;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.ParsimonyModelSet;
import mesquite.categ.lib.*;

public class InformCharBoolean extends BooleanForCharacter {

	public String getName() {
		return "Parsimony Informative";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public int charIsParsimonyUninformative(CharacterData data, int ic) {
		CategoricalData cData = (CategoricalData)data;
		long allStates = cData.getAllStates(ic,false);
		if (CategoricalState.cardinality(allStates)<2) {  // at most one state in character
			return 0;
		}
		else {  //two or more states
			int[] stateFreqs =  cData.getStateFrequencyArray(ic);
			int maxState = cData.getMaxState();
			boolean moreThanOne = false;
			for (int i = 0; i<=maxState; i++) {
				if (stateFreqs[i]>1) {  // state present in at least two taxa
					if (moreThanOne) {
						return 1;   //this is the second character we have found that is present in more than one taxon
					}
					moreThanOne = true;
				}
			}
			// if we are here, then it means there are more than one state, but there is at most one that is present in two or more taxa.			
			ModelSet modelSet = (ModelSet) data.getCurrentSpecsSet(ParsimonyModelSet.class);
			if ("unordered".equalsIgnoreCase(modelSet.getModel(ic).getName()))  //it is unordered; definitely uninformative
				return 0;
			

		}
		return -1;
	}

	
	public void calculateBoolean(CharacterData data, int ic, MesquiteBoolean result, MesquiteString resultString) {
		if (data==null || result==null)
			return;
		if (!(data instanceof CategoricalData))
			return;
		Taxa taxa = data.getTaxa();
		int inform = charIsParsimonyUninformative(data,ic);

		if (inform==0)
			result.setValue(false);
		else if (inform==1)
			result.setValue(true);
		else
			result.setToUnassigned();
		if (resultString!=null)
			resultString.setValue(getValueString(inform==1));
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

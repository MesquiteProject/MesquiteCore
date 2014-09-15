/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;


/** A class that provides a General Time Reversible rate matrix. */
/* ======================================================================== */
public class StateFreqUserDNAModel extends StateFreqUserModel {
	
//	DoubleSqMatrixFields rateMatrixField;

	public StateFreqUserDNAModel (CompositProbCategModel probabilityModel, double[] stateFrequencies) {
		super(probabilityModel,4,stateFrequencies);
 	}

 	/*.................................................................................................................*/
	public  CharacterModel cloneModelWithMotherLink(CharacterModel formerClone) {
		StateFreqUserDNAModel model = new StateFreqUserDNAModel(probabilityModel, getStateFrequencies());
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel pm){
		if (pm == null)
			return;
		if (pm instanceof StateFreqUserDNAModel) {
			StateFreqUserDNAModel stateFreqUser = (StateFreqUserDNAModel)pm;
			for (int i = 0; i<getNumStates(); i++) 
				stateFreqUser.setStateFreq(i,getStateFreq(i));
		}
		super.copyToClone(pm);
	}
	/*.................................................................................................................*/
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "StateFrequencyUserSpecifiedDNA";
	}
 	/*.................................................................................................................*/
	public String getNexusSpecification() {
		String s = "frequencies = (" + toStateFreqString() +  ") ";
		return s;
	}
	/*.................................................................................................................*/
	public Class dataClass(){
		return DNAState.class;
	}
 	/*.................................................................................................................*/
	public void fromString (String description, MesquiteInteger stringPos, int format) {
		hasDefaultValues = false;
		ParseUtil.getToken(description, stringPos);   // RMatrix
		ParseUtil.getToken(description, stringPos);  // =
		ParseUtil.getToken(description, stringPos);  // (
		for (int i = 0; i<getNumStates(); i++) {
				String s = ParseUtil.getToken(description, stringPos);
				if (s.equalsIgnoreCase(")") || StringUtil.blank(s)) 
					return;
				setStateFreq(i,MesquiteDouble.fromString(s));
			}
		checkNormality(getStateFrequencies());
	}
  	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "User-specified state frequency model, with values (A C G T): " + toStateFreqString();
	}

}



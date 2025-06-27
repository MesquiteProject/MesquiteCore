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

import mesquite.lib.characters.CharacterModel;
import mesquite.lib.ui.ExtensibleDialog;


/* ======================================================================== */
public class StateFreqDNAEqual extends StateFreqDNAModel {
	public StateFreqDNAEqual (CompositProbCategModel probabilityModel) {
		super(probabilityModel);
	}
 	/*.................................................................................................................*/
	/** Sets the equilibrium state frequencies to be all equal. */
	public void setStateFrequencies (){
		double probEach =1.0/getNumStates();
		for (int i=0; i<getNumStates(); i++) 
			setStateFreq(i,probEach);
	}
	/*.................................................................................................................*/
	public String getNexusSpecification(){
		return "";
	}
 	/*.................................................................................................................*/
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		StateFreqDNAEqual model = new StateFreqDNAEqual(probabilityModel);
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md){
		if (md == null || !(md instanceof StateFreqDNAEqual))
			return;
		StateFreqDNAEqual model = (StateFreqDNAEqual)md;
		model.setStateFrequencies();
		super.copyToClone(md);
	}
	/** Returns whether parameters of model are fully specified.  If so, it can be used for evolving states.*/
	public boolean isFullySpecified(){
		return true;
	}
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "equal";
	}
	/** returns name of model*/
	public String getName() {
		return "Equal Frequencies";
	}
	/** returns name of model*/
	public String getNEXUSName() {
		return "equal";
	}
	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
			return true;
	}
	/*.................................................................................................................*/
	public boolean checkOptions() {
		return true;
	}
 	/*.................................................................................................................*/
	public String checkOptionsReport() {
		return "";
	}
	/*.................................................................................................................*/
	public void setOptions() {}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "Equal Frequencies";
	}
}


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


/* ======================================================================== */
public class StateFreqDNAEmpir extends StateFreqDNAModel {
	//DNAData data = null;
	boolean specified = false;
	
	public StateFreqDNAEmpir (CompositProbCategModel probabilityModel) {
		super(probabilityModel);
	}
	public StateFreqDNAEmpir () {
		super(null);
	}
	/*.................................................................................................................*/
	public String getNexusSpecification(){ //could indicate matrix used
		return "";
	}
 	/*.................................................................................................................*/
	/** Sets the state frequencies; doesn't currently have an impact, as when this is invoked the data are not present. */
	public void setStateFrequencies (){
	}
	public boolean isFullySpecified(){
		return true;
	}
 	/*.................................................................................................................*/
 		public boolean needsEmpirical(){
		return true;
	}
 	/*.................................................................................................................*/
	public void initialize() {
//		if (probabilityModel!=null && probabilityModel.getData()==null)
//			probabilityModel.acquireData();
	}
 	/*.................................................................................................................*/
	public void taxaSet() {
//		resetStateFrequencies();
	}
 	/*.................................................................................................................*/
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		StateFreqDNAEmpir model = new StateFreqDNAEmpir(probabilityModel);
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/*.................................................................................................................*/
	public void recalcAfterSetMCharactersStatesHolder (){
		MCharactersStatesHolder d = probabilityModel.getMCharactersStatesHolder();
		if ((d==null) || !(DNAData.class.isAssignableFrom(d.getCharacterDataClass()))){
			MesquiteMessage.warnProgrammer("Empirical data in model null or not DNA data; will use equal freq. (StateFreqDNAEmpir)");
			return;
		}
		MCategoricalStates data = (MCategoricalStates)d;
		if (data!=null){
			specified = true;
			long numTotal=0;
	   		double[] stateTotal = new double[4];
			for (int it=0; it<data.getNumTaxa(); it++)
				for (int ic = 0; ic<data.getNumChars(); ic++) {
					if (data.isCurrentlyIncluded(ic)) {   
						long stateSet = data.getState(ic, it);
						int card = CategoricalState.cardinality(stateSet);
						if (DNAState.isCombinable(stateSet)) {
							for (int state=0; state<4; state++)
								if (DNAState.isElement(stateSet, state))
									stateTotal[state]+= 1.0/card;
							numTotal++;
						}
					}
				}
			for (int state=0; state<4; state++) {
					setStateFreq(state,  (stateTotal[state]/numTotal));
			}
		}
		else {
			MesquiteMessage.warnProgrammer("data null; will use equal freq.");
			for (int state=0; state<4; state++)
				setStateFreq(state,  0.25);  // give warning!!!
		}
	}
 	/*.................................................................................................................*/
	/** Resets the equilibrium state frequencies to be all equal. */
	public void resetStateFrequencies (){
		MCharactersStatesHolder d = probabilityModel.getMCharactersStatesHolder();
		if (!(DNAData.class.isAssignableFrom(d.getCharacterDataClass()))){
			MesquiteMessage.warnProgrammer("Empirical data in model null or not DNA data; will use equal freq. (StateFreqDNAEmpir)");
			return;
		}
		MCategoricalStates data = (MCategoricalStates)d;
		if (data!=null){
			setDefaultNumChars(data.getNumChars());
			specified = true;
			long numTotal = 0;
	   		double[] stateTotal = new double[4];
			for (int it=0; it<data.getNumTaxa(); it++)
				for (int ic = 0; ic<data.getNumChars(); ic++) {
					if (data.isCurrentlyIncluded(ic)) {   
						long stateSet = data.getState(ic, it);
						int card = CategoricalState.cardinality(stateSet);
						if (DNAState.isCombinable(stateSet)) {
							for (int state=0; state<4; state++)
								if (DNAState.isElement(stateSet, state)) {
									stateTotal[state]+= 1.0/card;
								}
							numTotal++;
						}
					}
				}
			for (int state=0; state<4; state++) {
					setStateFreq(state,  (stateTotal[state]/numTotal));
			}
		}
		else {
			MesquiteMessage.warnProgrammer("data null; will use equal freq.");
			for (int state=0; state<4; state++)
				setStateFreq(state,  0.25);  // give warning!!!
		}
	}
	public boolean isBuiltIn() {
		return true;
	}
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "empirical";
	}
	/** returns name of model for nexus*/
	public String getNEXUSName() {
		return "empirical";
	}
	/** returns name of model*/
	public String getName() {
		return "Empirical Frequencies";
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
		String s= "Empirical Frequencies";
		if (probabilityModel!=null) {
			MCharactersStatesHolder d = probabilityModel.getMCharactersStatesHolder();
			if (d!=null) {
				CharacterData data = d.getParentData();
				if (data!= null)
					s += " from matrix " + data.getName();
			}
		}
		if (specified) {
			s += " (A=" + MesquiteDouble.toString(getStateFreq(0, null, 0));
			s += " C=" + MesquiteDouble.toString(getStateFreq(1, null, 0));
			s += " G=" + MesquiteDouble.toString(getStateFreq(2, null, 0));
			s += " T=" + MesquiteDouble.toString(getStateFreq(3, null, 0))+")";
		}
		return s;
	}
}


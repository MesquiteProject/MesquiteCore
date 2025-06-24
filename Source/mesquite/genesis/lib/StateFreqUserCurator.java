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
/*~~  */

import mesquite.lib.duties.EditingCurator;
/* ======================================================================== */
public abstract class StateFreqUserCurator extends StateFreqCurator implements EditingCurator {
	StateFreqUserModel modelToEdit;
	/*.................................................................................................................*
  	 public void projectEstablished(){
  	 }
   
	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return StateFreqUserModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public abstract int getNumStates();
	/*.................................................................................................................*/
	public void setNumStates(int numStates){
		if (modelToEdit!=null)
			modelToEdit.setNumStates(numStates);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "StateFrequencyUserSpecified";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "StateFrequencyUserSpecified";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return StateFreqUserModel.class;
	}
	/*.................................................................................................................*
  	public CharacterModel makeNewModel(String name) {
 		StateFreqUserModel model = new StateFreqUserModel(null, 4, null);
 		model.setName(name);
       		return model;
   	}
   
	/*.................................................................................................................*
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		StateFreqUserModel model = new StateFreqUserModel();
 		model.setName(name);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "User-Specified State Frequency Model";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "User-Specified State Frequencies...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains user-specified state frequency models." ;
   	 }
}



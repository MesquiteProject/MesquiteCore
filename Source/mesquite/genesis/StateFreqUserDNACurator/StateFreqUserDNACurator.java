/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.StateFreqUserDNACurator;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
/* ======================================================================== */
public class StateFreqUserDNACurator extends StateFreqUserCurator {
	StateFreqUserDNAModel modelToEdit;
	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return StateFreqUserDNAModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public int getNumStates(){
		return 4;
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "StateFrequencyUserSpecifiedDNA";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "StateFrequencyUserSpecifiedDNA";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return StateFreqUserDNAModel.class;
	}
	/*.................................................................................................................*/
  	public CharacterModel makeNewModel(String name) {
 		StateFreqUserDNAModel model = new StateFreqUserDNAModel(null, null);
 		model.setName(name);
       		return model;
   	}
   
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		StateFreqUserDNAModel model = new StateFreqUserDNAModel(null,null);
 		model.setName(name);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "User-Specified Nucleotide Frequency Model";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "User-Specified Nucleotide Frequencies...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains user-specified nucleotide frequency models." ;
   	 }
}



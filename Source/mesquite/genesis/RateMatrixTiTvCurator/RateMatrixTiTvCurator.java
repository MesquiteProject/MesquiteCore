/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.RateMatrixTiTvCurator;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
/* ======================================================================== */
public class RateMatrixTiTvCurator extends RateMatrixCurator implements EditingCurator {
	RateMatrixTiTvModel modelToEdit;
	/*.................................................................................................................*
  	 public void projectEstablished(){
  	 }
   
	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return RateMatrixTiTvModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "RateMatrixTiTv";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "RateMatrixTiTv";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return RateMatrixTiTvModel.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		RateMatrixTiTvModel model = new RateMatrixTiTvModel();
 		model.setName(name);
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		RateMatrixTiTvModel model = new RateMatrixTiTvModel();
 		model.setName(name);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Ti/Tv Rate Matrix Model";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Ti/Tv Rate Matrix Model...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains two-parameter rate matrix models with a transition and transversion rates." ;
   	 }
 	/*.................................................................................................................*/
	/** Returns help string. */
	public String getHelpString (){
		return "Transition/Transversion model. Enter the ratio of transitions to transversions.";
	}
}



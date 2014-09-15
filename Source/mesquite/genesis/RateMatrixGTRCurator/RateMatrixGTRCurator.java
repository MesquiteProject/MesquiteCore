/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.RateMatrixGTRCurator;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;

/** ======================================================================== */
public class RateMatrixGTRCurator extends RateMatrixCurator implements EditingCurator {
	RateMatrixGTRModel modelToEdit;
	/*.................................................................................................................*
  	 public void projectEstablished(){
  	 }
   
	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return RateMatrixGTRModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "RateMatrixGTR";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "RateMatrixGTR";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return RateMatrixGTRModel.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		RateMatrixGTRModel model = new RateMatrixGTRModel();
 		model.setName(name);
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		RateMatrixGTRModel model = new RateMatrixGTRModel();
 		model.setName(name);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "GTR Rate Matrix Model";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "GTR Rate Matrix Model...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains six-parameter general time-reversible (GTR) rate matrix models." ;
   	 }
 	/*.................................................................................................................*/
	/** Returns help string. */
	public String getHelpString (){
		String s = "General Time Reversible model.  Enter the rate values for each kind of change.  As this is a reversible model, you only enter the rates going one way; the reverse rates are automatically presumed to be equivalent. ";
		return s+"  For example, the rate of C to A changes will be presumed to be the same as the rate you enter for A to C changes.";
	}
}



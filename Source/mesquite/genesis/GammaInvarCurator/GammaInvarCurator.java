/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.GammaInvarCurator;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
/* ======================================================================== */
public class GammaInvarCurator extends CharRatesCurator implements EditingCurator {
	/*.................................................................................................................*
  	 public void projectEstablished(){ 
  	 }
   
	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return CharRatesGammaInvar.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "GammaInvar";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "GammaInvar";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return CharRatesGammaInvar.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		CharRatesGammaInvar model = new CharRatesGammaInvar();
 		model.setName(name);
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		CharRatesGammaInvar model = new CharRatesGammaInvar();
 		model.setName(name);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Gamma Invar Rates Model";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Gamma Invar Rates Model...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains Gamma rate variation models with a proportion of invariable characters." ;
   	 }
}



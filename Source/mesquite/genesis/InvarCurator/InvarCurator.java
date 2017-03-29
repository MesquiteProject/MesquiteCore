/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.InvarCurator;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
/* ======================================================================== */
public class InvarCurator extends CharRatesCurator implements EditingCurator {
	/*.................................................................................................................*
  	 public void projectEstablished(){
		CharRatesModel charRatesInvar = new CharRatesInvar();
    		charRatesInvar.addToFile(null, getProject(), null);
    		charRatesInvar.setBuiltIn(true);
    		((CharRatesInvar)charRatesInvar).setShape(0.5);
    		charRatesInvar.setName("Gamma 0.5");
  	 }
   
	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return CharRatesInvar.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "Proportion Invariable";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "pInvar";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return CharRatesInvar.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		CharRatesInvar model = new CharRatesInvar();
 		model.setName(name);
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		CharRatesInvar model = new CharRatesInvar();
 		model.setName(name);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Proportion Invariable Model...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Proportion Invariable Model";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains models that specify that a certain proportion of characters are invariable." ;
   	 }
}



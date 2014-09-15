/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.CodePosRatesCurator;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
/* ======================================================================== */
public class CodePosRatesCurator extends CharRatesCurator  implements EditingCurator {
	/*.................................................................................................................*
  	 public void projectEstablished(){
		CharRatesModel charRatesInvar = new CharRatesCodePos();
    		charRatesInvar.addToFile(null, getProject(), null);
    		charRatesInvar.setBuiltIn(true);
    		((CharRatesCodePos)charRatesInvar).setShape(0.5);
    		charRatesInvar.setName("Gamma 0.5");
  	 }
   
	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return CharRatesCodePos.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "Codon Position Rates";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "CodePosRates";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return CharRatesCodePos.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		CharRatesCodePos model = new CharRatesCodePos();
 		model.setName(name);
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		CharRatesCodePos model = new CharRatesCodePos();
 		model.setName(name);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Codon Position Rates Model...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Codon Position Rates Model";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains models that specify rates for codon positions." ;
   	 }
	/*.................................................................................................................*/
    	 public String getHelpString() {
		return "Enter the rates for codon positions 1, 2, and 3, as well as non-coding sites (N) and unspecified sites (?).  If you choose \"use positions in existing matrix\", the codon position of the nth character any created matrix will match that of the nth character in an existing matrix.";
	}
}



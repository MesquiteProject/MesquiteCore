/* Mesquite source code (Genesis package).  Copyright 2001-2011 D. Maddison and W. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.StateFreqDNAEmpCurator;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
import mesquite.lib.characters.*;
/* ======================================================================== */
public class StateFreqDNAEmpCurator extends StateFreqCurator {
	StateFreqDNAEmpir modelToEdit;
	/*.................................................................................................................*
  	 public void projectEstablished(){
  	 }
   
	/*.................................................................................................................*/
   	public MesquiteModule showEditor(CharacterModel model){
   		modelToEdit = (StateFreqDNAEmpir)model;
   		//modelToEdit.resetStateFrequencies();
		return this;
   	}
	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return StateFreqDNAEmpir.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "Empirical";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "Empirical";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return StateFreqDNAEmpir.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		StateFreqDNAEmpir model = new StateFreqDNAEmpir();
 		model.setName(name);
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		StateFreqDNAEmpir model = new StateFreqDNAEmpir();
 		model.setName(name);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "DNA Empirical State Frequencies Model";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "DNA Empirical State Frequencies Model...";
   	 }
	/*.................................................................................................................*/
  	 public boolean IsBuiltIn() {
		return true;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains model of state frequences that match those found in an empirical matrix." ; 
   	 }
}



/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretNBRFProtein;
/*~~  */

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.ProteinState;
import mesquite.io.lib.InterpretNBRF;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.taxa.Taxa;

/* ============  a file interpreter for Protein  NBRF files ============*/

public class InterpretNBRFProtein extends InterpretNBRF {
/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;  //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return project.getNumberCharMatrices(ProteinState.class) > 0;  //
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==ProteinState.class);
	}
	/*.................................................................................................................*/
	public boolean canImport(Class dataClass){
		return (dataClass==ProteinState.class);
	}
/*.................................................................................................................*/
	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  
		 return charTask.newCharacterData(taxa, 0, ProteinData.DATATYPENAME);  //
	}
/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return getProject().chooseData(containerOfModule(), file, null, ProteinState.class, "Select data to export");
	}
	
	boolean	badImportWarningGiven = false;

/*.................................................................................................................*/
	public void setNBRFState(CharacterData data, int ic, int it, char c) { 
		((ProteinData)data).setState(ic, it, c);    // setting state to that specified by character c
   		if (CategoricalState.isImpossible(((CategoricalData)data).getState(ic,it))){
   			data.badImport = true;
			MesquiteTrunk.errorReportedDuringRun = true;
  			if (!badImportWarningGiven)
   				discreetAlert("THE DATA WILL BE INCORRECTLY IMPORTED.  The imported sequence includes symbols not interpretable as Protein sequence data.  If this is a DNA data file, please use the NBRF DNA interpreter. " + 
					"Also, please ensure this is not an rtf or zip file or other format that is not simple text.  This warning may not be given again, but you may see subsequent warnings about impossible states.");
   			badImportWarningGiven = true;
   		}
	}
/*.................................................................................................................*/
	public String getLineStart(){  
		return ">P1; ";
	}	
/*.................................................................................................................*/
    	 public String getName() {
		return "NBRF/PIR (protein)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports NBRF files that consist of amino acid sequence data." ;
   	 }
   	 
}
	


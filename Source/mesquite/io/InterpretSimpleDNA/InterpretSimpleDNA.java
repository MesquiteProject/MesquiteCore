/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretSimpleDNA;
/*~~  */

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.io.lib.InterpretSimple;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.taxa.Taxa;


/* ============  a file interpreter for DNA/RNA  Simple files ============*/

public class InterpretSimpleDNA extends InterpretSimple {
/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;  //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return project.getNumberCharMatrices(DNAState.class) > 0;  //
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		if (dataClass==null) return false;
		return ((DNAState.class).isAssignableFrom(dataClass)); 
	}
	/*.................................................................................................................*/
	public  boolean isCategorical (){
		return true;
	}
	/*.................................................................................................................*/
	public boolean canImport(Class dataClass){
		if (dataClass==null) return false;
		return ((DNAState.class).isAssignableFrom(dataClass)); 
	}
/*.................................................................................................................*/
	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  
		 return charTask.newCharacterData(taxa, 0, DNAData.DATATYPENAME);  //
	}
/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return getProject().chooseData(containerOfModule(), file, null, DNAState.class, "Select data to export");
	}
/*.................................................................................................................*/
    	 public String getName() {
		return "Simple (DNA/RNA)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports simple matrices that consist of DNA/RNA sequence data." ;
   	 }
   	 
}
	


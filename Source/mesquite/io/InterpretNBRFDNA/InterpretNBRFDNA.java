/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretNBRFDNA;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.io.lib.*;


/* ============  a file interpreter for DNA/RNA  NBRF files ============*/

public class InterpretNBRFDNA extends InterpretNBRF {
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
   	boolean		badImportWarningGiven = false;
/*.................................................................................................................*/
	public void setNBRFState(CharacterData data, int ic, int it, char c) { 
   		if ((c=='U')||(c=='u')) {
   			((DNAData)data).setDisplayAsRNA(true);
   		}
   		((DNAData)data).setState(ic,it,c);
   		if (CategoricalState.isImpossible(((CategoricalData)data).getState(ic,it))){
   			data.badImport = true;
			MesquiteTrunk.errorReportedDuringRun = true;
  			if (!badImportWarningGiven)
   				discreetAlert("THE DATA WILL BE INCORRECTLY IMPORTED.  The imported sequence includes symbols not interpretable as DNA sequence data.  If this is a protein data file, please use the NBRF protein interpreter. " + 
   						"Also, please ensure this is not an rtf or zip file or other format that is not simple text.  This warning may not be given again, but you may see subsequent warnings about impossible states.");
   			badImportWarningGiven = true;
   		}
	}
							
/*.................................................................................................................*/
	public String getLineStart(){  
		return ">DL; ";
	}	
/*.................................................................................................................*/
    	 public String getName() {
		return "NBRF/PIR (DNA/RNA)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports NBRF files that consist of DNA/RNA sequence data." ;
   	 }
   	 
}
	


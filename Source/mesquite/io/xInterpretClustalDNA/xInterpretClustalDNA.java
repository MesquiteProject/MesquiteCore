/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.xInterpretClustalDNA;
/*~~  */

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.io.lib.InterpretClustal;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.taxa.Taxa;


/* ============  a file interpreter for DNA/RNA  Clustal files ============*/

public class xInterpretClustalDNA extends InterpretClustal {
/*.................................................................................................................*/
	public boolean canExportEver() {  
		return false;
		// return getProject().getNumberCharMatrices(DNAState.class) > 0;  //
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		 return false; 
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
    	 public String getName() {
		return "Clustal (DNA/RNA)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports Clustal files that consist of DNA or RNA sequence data." ;
   	 }
   	 
}
	


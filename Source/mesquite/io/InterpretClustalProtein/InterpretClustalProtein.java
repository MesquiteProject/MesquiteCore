/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretClustalProtein;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.io.lib.*;

/* ============  a file interpreter for Protein  Clustal files ============*/

public class InterpretClustalProtein extends InterpretClustal {
/*.................................................................................................................*/
	public boolean canExporEvert() {  
		 return false;
		 // return getProject().getNumberCharMatrices(ProteinState.class) > 0;  //
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		 return false; 
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
    	 public String getName() {
		return "Clustal (protein)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports Clustal files that consist of amino acid sequence data." ;
   	 }
   	 
}
	


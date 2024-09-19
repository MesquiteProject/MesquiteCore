/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretPhylipCat;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.io.lib.*;


/* ============  a file interpreter for Categorical Phylip files ============*/

public class InterpretPhylipCat extends InterpretPhylip {
/*.................................................................................................................*/
	public boolean getInterleaved(){
		return false;
	}	
/*.................................................................................................................*/
	public void setPhylipState(CharacterData data, int ic, int it, char c){
		if ((c=='P')||(c=='p')||(c=='B')||(c=='b'))
			((CategoricalData)data).setState(ic, it, CategoricalState.makeSet(0,1));
		else
			((CategoricalData)data).setState(ic, it, c);
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==CategoricalState.class);
	}
/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;  //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return project.getNumberCharMatrices(CategoricalState.class) > project.getNumberCharMatrices(MolecularState.class);  //must be some categorical, and not all molecular
	}
/*.................................................................................................................*/
	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  
		 return charTask.newCharacterData(taxa, 0, CategoricalData.DATATYPENAME);  //
	}
/*.................................................................................................................*/
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, MesquiteStringBuffer outputBuffer){
		outputBuffer.append(statesToStringPolyChar((CategoricalData)data, ic, it,'P'));
	}
	/*..........................................  CategoricalData  ..................................................*/
   	/** returns string describing the state(s) of character ic in taxon it.  Uses default symbols, and uses
   		polyChar for polymorphic cells and missing for uncertainty. 
   		USED BY PHYLIP EXPORT*/
	public String statesToStringPolyChar(CategoricalData data, int ic,int it, char polyChar){
		long s =data.getState(ic, it); //[ic][it];
		String stateString="";
		if (s==CategoricalState.inapplicable) 
			return stateString+data.getInapplicableSymbol();
		else if (s==CategoricalState.unassigned) 
			return stateString+data.getUnassignedSymbol(); 
		else if (CategoricalState.cardinality(s)>1) {
			if (CategoricalState.isUncertain(s))
				return stateString+data.getUnassignedSymbol(); 
			else
				return stateString+polyChar; 
		}
		else
			return stateString+data.getDefaultStateSymbol(CategoricalState.minimum(s));
	}
/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		CharacterData dataToExport = getProject().chooseData(containerOfModule(), file, null, CategoricalState.class, "Select data to export");
		if (dataToExport == null)
			return null;
		if (((CategoricalData)dataToExport).getMaxState()>2) {
			AlertDialog.notice(containerOfModule(), "Can't export", "This matrix has some characters with states greater than 1, and thus it cannot be exported in Phylip categorical format.");
			return null;
		}
		else
			return dataToExport;
	}
/*.................................................................................................................*/
    	 public String getName() {
		return "Phylip (categorical data)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports Phylip matrices that consist of basic categorical data with just two states. Exported data will consist of default symbols (0 and 1)." ;
   	 }
   	 
}
	


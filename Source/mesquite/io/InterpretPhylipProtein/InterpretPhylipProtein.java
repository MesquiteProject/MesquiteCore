/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretPhylipProtein;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.io.lib.*;


/* ============  a file interpreter for protein Phylip files ============*/

public class InterpretPhylipProtein extends InterpretPhylip {
	
		boolean badImportWarningGiven = false;

/*.................................................................................................................*/
	public void setPhylipState(CharacterData data, int ic, int it, char c){
		((CategoricalData)data).setState(ic, it, c);
   		if (CategoricalState.isImpossible(((CategoricalData)data).getState(ic,it))){
   			data.badImport = true;
			MesquiteTrunk.errorReportedDuringRun = true;
			if (!badImportWarningGiven)
   				discreetAlert("THE DATA WILL BE INCORRECTLY IMPORTED.  The imported sequence includes symbols not interpretable as Protein sequence data.  If this is a DNA data file, please use the Phylip DNA interpreter. " + 
					"Also, please ensure this is not an rtf or zip file or other format that is not simple text.  This warning may not be given again, but you may see subsequent warnings about impossible states.");
   			badImportWarningGiven = true;
   		}
	}
/*........................../*.................................................................................................................*/
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
	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  
		 return charTask.newCharacterData(taxa, 0, ProteinData.DATATYPENAME);  //
	}
/*.................................................................................................................*/
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, StringBuffer outputBuffer){
		data.statesIntoStringBuffer(ic, it, outputBuffer, false);
	}
/*.................................................................................................................*/
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		PhylipExporterDialog exportDialog = new PhylipExporterDialog(this,containerOfModule(), "Export Phylip Options", buttonPressed);
		
		Checkbox interleavedCheckBox = exportDialog.addCheckBox("interleave matrix", exportInterleaved);
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		
		exportInterleaved = interleavedCheckBox.getState();
		taxonNameLength = exportDialog.getTaxonNamesLength();
		exportDialog.dispose();
		return ok;
	}	
/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return getProject().chooseData(containerOfModule(), file, null, ProteinState.class, "Select data to export");
	}
/*.................................................................................................................*/
    	 public String getName() {
		return "Phylip (protein)";
   	 }
/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports Phylip matrices that consist of amino acid sequence data." ;
   	 }
   	 
}
	


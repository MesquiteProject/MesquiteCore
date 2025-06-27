/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.InterpretPOYDNA;
/*~~  */

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.lib.Arguments;
import mesquite.lib.ExporterDialog;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteStringBuffer;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;


/* ============  an exporter for POY files ============*/

public class InterpretPOYDNA extends FileInterpreterI {

/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
/*.................................................................................................................*/
	public String preferredDataFileExtension() {
 		return "";
   	 }
/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;  //
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		 return (project.getNumberCharMatrices( DNAState.class) > 0) ;
	}
	
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==DNAState.class);
	}
/*.................................................................................................................*/
	public boolean canImport() {  
		 return false;
	}

/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}


/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		setLineDelimiter(UNIXDELIMITER);
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export NBRF Options", buttonPressed);
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		
		exportDialog.dispose();
		return ok;
	}	

	
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");
		DNAData data = (DNAData)getProject().chooseData(containerOfModule(), file, null, DNAState.class, "Select data to export");
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		Taxa taxa = data.getTaxa();
		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(data.anySelected(), taxa.anySelected()))
				return false;

		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(numTaxa*(20 + numChars));
		
		for (int it = 0; it<numTaxa; it++){
			if (!writeOnlySelectedTaxa || (taxa.getSelected(it))){
//				outputBuffer.append(ParseUtil.tokenize(taxa.getTaxonName(it)) + "\t");  as in Mesquite 1. 06
				outputBuffer.append(StringUtil.cleanseStringOfFancyChars(taxa.getTaxonName(it),true,false) +getLineEnding());  // 1. 1: hanged to this in response to Kip Will's posting
				for (int ic = 0; ic<numChars; ic++) {
					if (!writeOnlySelectedData || (data.getSelected(ic))){
						long currentSize = outputBuffer.length();
						if (!data.isInapplicable(ic,it)) {
							data.statesIntoStringBuffer(ic, it, outputBuffer, false);
						}
						if (outputBuffer.length()-currentSize>1) {
							alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
							return false;
						}
					}
				}
				outputBuffer.append(getLineEnding()+getLineEnding());
			}
		}
		
		saveExportedFileWithExtension(outputBuffer, arguments, "poy");
		return true;
	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "POY  (DNA/RNA)";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Exports POY files of DNA/RNA sequence data." ;
   	 }
	/*.................................................................................................................*/
   	 
   	 
}
	


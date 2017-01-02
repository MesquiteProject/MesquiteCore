/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.ExportDescriptions;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.MolecularState;
import mesquite.lib.Arguments;
import mesquite.lib.ExporterDialog;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteThread;
import mesquite.lib.NameReference;
import mesquite.lib.ParseUtil;
import mesquite.lib.Parser;
import mesquite.lib.Taxa;
import mesquite.lib.duties.FileInterpreterI;



public class ExportDescriptions extends FileInterpreterI {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
	}
	/*.................................................................................................................*/
	/** returns whether this module is a prerelease version.  This returns "TRUE" here, forcing modules to override to claim they are not prerelease */
	public boolean isPrerelease(){
		return true; 
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
		return (project.getNumberCharMatrices( CategoricalState.class) - project.getNumberCharMatrices( MolecularState.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==CategoricalState.class);
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}

	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}


	/* ============================  exporting ============================*/
	int style = 0;
	/*.................................................................................................................*/

	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export Simple Table Options", buttonPressed);


		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		exportDialog.dispose();
		return ok;
	}	

	NameReference notesNameRef = NameReference.getNameReference("notes");


	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		CategoricalData data = (CategoricalData)getProject().chooseData(containerOfModule(), file, null, CategoricalState.class, "Select data to export");
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
		StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));

		for (int it = 0; it<numTaxa; it++){
			if (!writeOnlySelectedTaxa || (taxa.getSelected(it))){
				outputBuffer.append(ParseUtil.tokenize(taxa.getTaxonName(it)) +getLineEnding());
				for (int ic = 0; ic<numChars; ic++) {
					if (!writeOnlySelectedData || (data.getSelected(ic))){
						if  (!data.isInapplicable(ic,it)&&!data.isUnassigned(ic, it)) {
							if (data.characterHasName(ic)) 
								outputBuffer.append(data.getCharacterName(ic));
							else
								outputBuffer.append("Character " + (ic+1));
							outputBuffer.append(" ");
							data.statesIntoStringBuffer(ic, it, outputBuffer, true);
							outputBuffer.append(". ");
						}
					}
				}
				outputBuffer.append(getLineEnding()+getLineEnding());
			}
		}

		saveExportedFileWithExtension(outputBuffer, arguments, "txt");
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module does substantive calculations affecting analysis results, 
		or only a graphical/UI/input-output module */
	public boolean isSubstantive(){
		return false;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 320;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Descriptions";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports information in a matrix as a description." ;
	}
}

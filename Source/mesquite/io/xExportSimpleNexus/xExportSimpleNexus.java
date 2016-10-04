/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.xExportSimpleNexus;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;



public class xExportSimpleNexus extends FileInterpreterI {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
	}

	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "nex";
	}
	/*.................................................................................................................*/
	/** Returns wether this interpreter uses a flavour of NEXUS.  Used only to determine whether or not to add "nex" as a file extension to imported files (if already NEXUS, doesn't).**/
	public boolean usesNEXUSflavor(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return (project.getNumberCharMatrices( CategoricalState.class) > 0) ;
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
	/*.................................................................................................................*/
	boolean convertAmbiguities = false;
	boolean interleaveAllowed = false;
	boolean writeCharLabels = false;
	boolean useData = true;
	String addendum = "";
	String fileName = "untitled.nex";
	public boolean getExportOptions(CharacterData data, boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export NEXUS Options", buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		Checkbox useDataBlock = exportDialog.addCheckBox("use DATA instead of TAXA/CHARACTERS blocks", useData);
		Checkbox charLabelsBox = exportDialog.addCheckBox("write CharLabels", writeCharLabels);
		Checkbox interleaveBox = exportDialog.addCheckBox("allow interleaved matrix", interleaveAllowed);
		Checkbox convertToMissing = exportDialog.addCheckBox("convert partial ambiguities to missing", convertAmbiguities);
		exportDialog.addLabel("Addendum to file: ");

		TextArea fsText =exportDialog.addTextAreaSmallFont(addendum,8);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		convertAmbiguities = convertToMissing.getState();
		useData = useDataBlock.getState();
		interleaveAllowed = interleaveBox.getState();
		writeCharLabels = charLabelsBox.getState();
		addendum = fsText.getText();
		exportDialog.dispose();
		return ok;
	}	


	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);

		CategoricalData data = null;
		String path = null;
		MesquiteFile f;
		MesquiteString dir = new MesquiteString();
		MesquiteString fn = new MesquiteString();
		boolean scripting = args.parameterExists("script");
		if (MesquiteThread.isScripting() || scripting){
			int im = args.getParameterValueAsInt("matrix");
			dir.setValue(args.getParameterValue("directory"));
			if (dir.getValue() == null)
				dir.setValue(getProject().getHomeDirectoryName());
			fn.setValue(args.getParameterValue("file"));
			path = dir.getValue() + fn.getValue();
			data = (CategoricalData)getProject().getCharacterMatrix(im, CategoricalState.class);
			if (data ==null) {
				showLogWindow(true);
				logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
				return false;
			}
			useData = args.parameterExists("useData");
			convertAmbiguities = args.parameterExists("convertAmbiguities");
			interleaveAllowed = args.parameterExists("interleaveAllowed");
			writeCharLabels = args.parameterExists("writeCharLabels");
			writeOnlySelectedTaxa = args.parameterExists("writeOnlySelectedTaxa");
			
		}
		else {
			boolean usePrevious = args.parameterExists("usePrevious");
			if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying about options"))
				usePrevious = true;
			data = (CategoricalData)getProject().chooseData(containerOfModule(), file, null, CategoricalState.class, "Select data to export");
			if (data ==null) {
				showLogWindow(true);
				logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
				return false;
			}
			String suggested = fileName;
			if (file !=null)
				suggested = file.getFileName();
			if (!usePrevious){
				if (!getExportOptions(data, data.anySelected(), data.getTaxa().anySelected()))
					return false;
			}
			path = getPathForExport(arguments, suggested, dir, fn);
		}
		if (path != null) {
			f = MesquiteFile.newFile(dir.getValue(), fn.getValue());
			if (f !=null){
				f.useSimplifiedNexus = true;
				f.useDataBlocks = useData;
				f.ambiguityToMissing = convertAmbiguities;
				f.interleaveAllowed = interleaveAllowed;
				f.writeCharLabels = writeCharLabels;
				//  writeOnlySelectedData ;
				// writeOnlyIncludedData ;
				f.writeOnlySelectedTaxa = writeOnlySelectedTaxa ;

				f.openWriting(true);
				f.writeLine("#NEXUS" + StringUtil.lineEnding());
				if (!useData)
					f.writeLine(((TaxaManager)findElementManager(Taxa.class)).getTaxaBlock(data.getTaxa(), null, f));
				data.getMatrixManager().writeCharactersBlock(data, null, f, null);
				if (addendum != null)
					f.writeLine(addendum);
				f.closeWriting();
				return true;
			}
		}
		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Simplified NEXUS";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports NEXUS files, for instance of old fashioned style (using DATA block)." ;
	}
	/*.................................................................................................................*/


}



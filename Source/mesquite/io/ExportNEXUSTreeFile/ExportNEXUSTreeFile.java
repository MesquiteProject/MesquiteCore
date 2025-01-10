/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.ExportNEXUSTreeFile;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ListDialog;
import mesquite.categ.lib.*;



public class ExportNEXUSTreeFile extends FileInterpreterI {

/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
	public boolean isPrerelease(){
		return false;
	}
	public boolean isSubstantive(){
		return true;
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
		 return (project.getNumberOfFileElements(TreeVector.class) > 0) ;
	}
	
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return false;
	}
/*.................................................................................................................*/
	public boolean canImport() {  
		 return false;
	}

/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
	}
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
		return 201;  
 	}


/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	boolean includeTaxaBlock = false;
	String fileName = "untitledTrees.nex";
	String addendum = "";
	
	public boolean getExportOptions(TreeVector trees){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export NEXUS Trees file", buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		Checkbox itCheckBox = exportDialog.addCheckBox("Include Taxa Block", includeTaxaBlock);
		exportDialog.addLabel("Addendum: ");
		
		addendum = "";
		
		TextArea fsText =exportDialog.addTextAreaSmallFont(addendum,16);
				
		exportDialog.completeAndShowDialog();
			
		boolean ok = (exportDialog.query()==0);
		
		includeTaxaBlock = itCheckBox.getState();

		addendum = fsText.getText();
		exportDialog.dispose();
		return ok;
	}	
	
	
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");
		Listable[] blocks = getProject().getFileElements(TreeVector.class);
		if (blocks ==null) {
			showLogWindow(true);
			logln("WARNING: No trees block is available for export.\n");
			return false;
		}
		TreeVector trees =  (TreeVector)ListDialog.queryList(containerOfModule(), "Choose Tree Block", "Choose tree block for export", null, blocks, 0);
		TreesManager tm =  (TreesManager)findElementManager(TreeVector.class);
		if (trees ==null) {
			return false;
		}
		MesquiteString dir = new MesquiteString();
		MesquiteString fn = new MesquiteString();
		String suggested = fileName;
		if (file !=null)
			suggested = file.getFileName();
		MesquiteFile f;
		if (!usePrevious){
			if (!getExportOptions(trees))
				return false;
		}
		String path = getPathForExport(arguments, suggested, dir, fn);
		if (path != null) {
			f = MesquiteFile.newFile(dir.getValue(), fn.getValue());
			if (f !=null){
				f.openWriting(true);
				f.writeLine("#NEXUS" + StringUtil.lineEnding());
				if (includeTaxaBlock)
					f.writeLine(((TaxaManager)findElementManager(Taxa.class)).getTaxaBlock(trees.getTaxa(), null, file));
				String block = tm.getTreeBlock( trees, null);
				if (block != null)
					f.writeLine(block);
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
		return "Export NEXUS Tree File";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Exports NEXUS file with a tree block, and optionally a taxa block." ;
   	 }
	/*.................................................................................................................*/
   	 
   	 
}
	


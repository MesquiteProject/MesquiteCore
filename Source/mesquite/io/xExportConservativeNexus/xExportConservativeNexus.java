/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.xExportConservativeNexus;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;



public class xExportConservativeNexus extends FileInterpreterI {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  //make this depend on taxa reader being found?)
	}

	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "nex";
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return true;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return true;
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
	String fileName = "untitled.nex";
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		NexusFileInterpreter writer = (NexusFileInterpreter)getFileCoordinator().findEmployeeWithDuty(NexusFileInterpreter.class);
		if (writer == null){
			discreetAlert("Sorry, no NEXUS file writer found");
			return false;
		}
		String path = null;
		String oldDir = file.getDirectoryName();
		String oldName = file.getFileName();
		boolean wasConservative = file.useConservativeNexus;
		
		MesquiteString dir = new MesquiteString();
		MesquiteString fn = new MesquiteString();
		boolean scripting = args.parameterExists("script");
		if (MesquiteThread.isScripting() || scripting){
			dir.setValue(args.getParameterValue("directory"));
			if (dir.getValue() == null)
				dir.setValue(getProject().getHomeDirectoryName());
			fn.setValue(args.getParameterValue("file"));
			path = dir.getValue() + fn.getValue();
		}
		else {
			String suggested = fileName;
			if (file !=null)
				suggested = file.getFileName();
			path = getPathForExport(arguments, suggested, dir, fn);
		}
		if (path != null) {
			file.setLocs(true, null, fn.getValue(), dir.getValue());
				file.useConservativeNexus = true;
				writer.writeFile(getProject(), file);
				file.setLocs(true, null, oldName, oldDir);
				file.useConservativeNexus = wasConservative;
		}
		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Conservative NEXUS";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports NEXUS files that avoid TITLE, LINK, IDS, the MESQUITE block, and other commands used mostly by Mesquite" ;
	}
	/*.................................................................................................................*/


}



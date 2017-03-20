/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.io.InterpretMrBayesConTreeFile;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;



public class InterpretMrBayesConTreeFile extends FileInterpreterI implements NEXUSInterpreter {

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
	/*public boolean usesNEXUSflavor(){
		return true;
	}*/
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return false;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return false;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return false;
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return true;
	}
	public boolean exportFile(MesquiteFile file, String arguments) { 	
		return false;
	}
	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		NexusFileInterpreter nfi = (NexusFileInterpreter)mf.getCoordinatorModule().findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
		if (nfi!=null && nfi.canReadFile(file)){
			file.mrBayesReadingMode = true;
			nfi.readFile(getProject(), file, arguments);	
			file.mrBayesReadingMode = false;
		}
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Import Bayesian Consensus Tree File";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports Bayesian Consensus Tree File (a NEXUS file) to capture metadata recorded at nodes" ;
	}
	/*.................................................................................................................*/


}



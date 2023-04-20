/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.dmanager.FlipTaxonLocus;

import java.io.File;

import mesquite.externalCommunication.lib.PythonUtil;
import mesquite.lib.*;
import mesquite.lib.duties.GeneralFileMaker;

/* ======================================================================== */
public class FlipTaxonLocus extends GeneralFileMaker {

	ExternalProcessManager externalRunner;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}

	/*.................................................................................................................*/
	public void processDirectory(String directoryPath) {
		if (!PythonUtil.pythonAvailable())
			PythonUtil.pythonSettings(this);
		if (!PythonUtil.pythonAvailable())
			return;

		boolean success = false;
		ProgressIndicator progressIndicator = new ProgressIndicator(getProject(), "Python script in progress");
		progressIndicator.start();
		String pythonCommand = PythonUtil.python3Path;
		String pythonOptions = StringUtil.protectFilePathForCommandLine(getPath()+"flipTaxonLocustoLocusTaxon.py");

		if (!PythonUtil.python3Available()) {
			pythonCommand = PythonUtil.python2Path;
			pythonOptions = StringUtil.protectFilePathForCommandLine(getPath()+"flipTaxonLocustoLocusTaxon2.py");
		}
		
		externalRunner = new ExternalProcessManager(this, directoryPath, pythonCommand, pythonOptions, getName(), null, null, null, true, true);
		externalRunner.setStdOutFileName(ShellScriptRunner.stOutFileName);
		success = externalRunner.executeInShell();
		if (success)
			success = externalRunner.monitorAndCleanUpShell(progressIndicator);
		if (progressIndicator.isAborted())
			logln("Aborted by user\n");
		progressIndicator.goAway();
	}	


	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		String directoryPath = MesquiteFile.chooseDirectory("Choose directory containing data files:", null); 
		if (StringUtil.blank(directoryPath))
			return null;

		File directory = new File(directoryPath);
		if (directory!=null) {
			if (directory.exists() && directory.isDirectory()) {
				processDirectory(directoryPath);
			}
		}

		return null;
	}

	/*.................................................................................................................*/
	public boolean loadModule() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 381;  
	}

/*.................................................................................................................*/
	public String getName() {
		return "Flip Taxon x Locus to Locus x Taxon";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Flip Taxon x Locus to Locus x Taxon...";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Invokes python to take a series of FASTA files each containing all of a taxon's sequences in each of a series of loci, and creating from this a series of FASTA files each containing all of the sequences for one locus for all taxa." ;
	}


}

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
//		String pythonVersionStOut= ExternalProcessManager.executeAndGetStandardErr(this, "/Library/Frameworks/Python.framework/Versions/2.7/Resources/Python.app/Contents/MacOS/Python", "-V");
		
//		String pythonVersionStOut= ExternalProcessManager.executeAndGetStandardErr(this, "python", "-V");
/*		String raxmlng= ExternalProcessManager.executeAndGetStandardOut(this, "/usr/local/bin/raxml-ng", "-v");
		logln("\nraxml-ng version: " + raxmlng);
		String iqTreeV= ExternalProcessManager.executeAndGetStandardOut(this, "/usr/local/bin/iqtree2", "-V");
		logln("\niq-tree version: " + iqTreeV);
*/
		
		boolean success = false;
		MesquiteTimer timer = new MesquiteTimer();
		timer.start();

		ProgressIndicator progressIndicator = new ProgressIndicator(getProject(), "Python script in progress");
		progressIndicator.start();

		String pythonCommand = "python";
		String pythonOptions = StringUtil.protectFilePathForCommandLine(getPath()+"flipTaxonLocustoLocusTaxon.py");
		
		externalRunner = new ExternalProcessManager(this, directoryPath, pythonCommand, pythonOptions, getName(), null, null, null, true, true);
//		externalRunner = new ExternalProcessManager(this, directoryPath, pythonProgramCommands, getName(), null, null, null, true);
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

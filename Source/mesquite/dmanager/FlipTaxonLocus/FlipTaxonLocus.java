package mesquite.dmanager.FlipTaxonLocus;

import java.io.File;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.ProteinData;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.GeneralFileMaker;
import mesquite.lib.MesquiteTrunk;

/* ======================================================================== */
public class FlipTaxonLocus extends GeneralFileMaker {

	//protected String directoryPath=null;
	ExternalProcessManager externalRunner;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		return true;
	}

	/*.................................................................................................................*/
	public void processDirectory(String directoryPath) {

		boolean success = false;
		MesquiteTimer timer = new MesquiteTimer();
		timer.start();

		ProgressIndicator progressIndicator = new ProgressIndicator(getProject(), "Python script in progress");
		progressIndicator.start();
		String outFileName = "pythOut.txt";
		String outFilePath = directoryPath + outFileName;
		String[] outputFilePaths = new String[1];
		outputFilePaths[0] = outFilePath;
		String pythonCode = getPath()+MesquiteFile.fileSeparator+"flipTaxonLocustoLocusTaxon.py";

		String arguments = "";

		arguments=StringUtil.stripBoundingWhitespace(arguments);
		externalRunner = new ExternalProcessManager(this, directoryPath, "python", pythonCode, getName(), outputFilePaths, null, null, true);
//		externalRunner.setStdOutFileName(ShellScriptRunner.stOutFileName);
		success = externalRunner.executeInShell();
		if (success)
			success = externalRunner.monitorAndCleanUpShell(progressIndicator);


		if (progressIndicator.isAborted()){
			logln("Aborted by user\n");
		}
		progressIndicator.goAway();

		if (success){}
	}	


	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		boolean success= false;
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
	public String getName() {
		return "Flip Taxon x Locus to Locus x Taxon...";
	}


}

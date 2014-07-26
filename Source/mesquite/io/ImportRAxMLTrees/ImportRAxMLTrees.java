package mesquite.io.ImportRAxMLTrees;

import mesquite.io.lib.*;
import mesquite.lib.*;

public class ImportRAxMLTrees extends InterpretPhylipTrees {
	
	String infoFileName = "RAxML_info.output";
	/*.................................................................................................................*/
	public boolean initializeTreeImport(MesquiteFile file, Taxa taxa) {  
		 String translationFile = null;
		 String directoryPath = file.getDirectoryName();
		 String translationTablePath = directoryPath+IOUtil.translationTableFileName;
		 translationFile = MesquiteFile.getFileContentsAsString(translationTablePath);
		 if (StringUtil.notEmpty(translationFile)){
			 taxonNamer = new SimpleTaxonNamer();
			 ((SimpleTaxonNamer)taxonNamer).loadTranslationTable(taxa, translationFile);
		 }
		 else 
			 taxonNamer = null;
		 return true;
	}
	/*.................................................................................................................*/
	public boolean importExtraFiles(MesquiteFile file, Taxa taxa, TreeVector trees) {  
		String directoryPath = file.getDirectoryName();
		String summary = MesquiteFile.getFileContentsAsString(directoryPath+infoFileName);
		if (StringUtil.notEmpty(summary)) {
			DoubleArray finalValues = new DoubleArray(trees.size());
			IOUtil.readRAxMLInfoFile(this, summary, true, trees, finalValues);
		}
		return true;
	}

	public String getTreeNameBase () {
		return "RAxML tree ";
	}

	/*.................................................................................................................*/
	public String getName() {
		return "RAxML Results Import";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports RAxML trees and associated data." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}

}

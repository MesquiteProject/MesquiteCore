/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.ShowTreesFromLiveFile;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.FileAssistantFM;
import mesquite.treefarm.OpenLiveTreeFile.OpenLiveTreeFile;

/* ======================================================================== */
public class ShowTreesFromLiveFile extends FileAssistantFM {
	OpenLiveTreeFile importerTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/*String directoryPath = MesquiteFile.chooseDirectory("Choose folder containing FASTA files, one per taxon:", null); 
		if (StringUtil.blank(directoryPath))
			return false;*/
		importerTask = (OpenLiveTreeFile)hireNamedEmployee(OpenLiveTreeFile.class, "#OpenLiveTreeFile");
		if (importerTask == null)
			return false;
		if (!MesquiteThread.isScripting())
 			importerTask.readFile(getProject(), null, false);

		iQuit();
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setImporterTask", importerTask);
		temp.addLine("setPath " + StringUtil.tokenize(importerTask.getPath()));
		return temp;
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	if (checker.compare(this.getClass(), "Returns the importer", "[]", commandName, "setImporterTask")) {
	   	 		return importerTask;
     	 	}
     	 	else if (checker.compare(this.getClass(), "Set path", "[path]", commandName, "setPath")) {
     	 		String path = parser.getFirstToken(arguments);
     	 		
     			importerTask.readFile(getProject(), path, false);
     	 	}
   	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
     	 	return null;
   	 }

	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return false;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice() { 
		return false;
	}

	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Show Trees from Live Tree File...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Show Trees from Live Tree File";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 400;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Opens a tree window showing trees from a separate NEXUS or simple Newick/Phylip tree file. You can use this to monitor an ongoing tree inference analysis, because if the file changes, new trees are automatically read. By default, the tree window shows the last tree in the file.\n"
				+" This is designed to work with a tree file that includes only one taxa block." ;
	}

}



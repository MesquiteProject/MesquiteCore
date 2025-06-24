/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.dmanager.ProcessFile;
/*~~  */


import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.duties.FileInit;
import mesquite.lib.duties.FileProcessor;

public class ProcessFile extends FileInit {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		getFileCoordinator().addMenuItem(MesquiteTrunk.editMenu, "Process File...", new MesquiteCommand("processFile", this));
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}
	boolean firstTime = true;
	public boolean okToInteractWithUser(int howImportant, String messageToUser){
		if (firstTime){
			firstTime = false;
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Processes the file using a FileProcessor", null, commandName, "processFile")) {
			firstTime = true;
			MesquiteFile f=  checker.getFile();
			if (f == null)
				f = getProject().getHomeFile();
			FileProcessor processor = (FileProcessor)hireEmployee(FileProcessor.class, "File processor ");
			if (processor == null)
				return null;
			getFileCoordinator().setWhomToAskIfOKToInteractWithUser(this);
			MesquiteString result = new MesquiteString();
			result.setValue((String)null);
			processor.processFile(f, result);
			firstTime = true;
			fireEmployee(processor);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Process File";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Calls a File Processor to process the current file.  Options depend on modules installed." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 303;  
	}
}



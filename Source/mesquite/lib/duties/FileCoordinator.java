/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.duties;

import java.awt.*;

import mesquite.lib.*;

/* ======================================================================== */
/**
 * The defining superclass for file coordinating modules. A module of this class is the fundamental module second only to the MesquiteTrunk module itself. MesquiteTrunk hires a file coordinating module for each MesquiteProject. Example module: Basic File Coordinator
 */

public abstract class FileCoordinator extends MesquiteModule {
	public boolean getSearchableAsModule(){
		return false;
	}
	public static int totalCreated = 0;

	public static int totalFinalized = 0;

	public FileCoordinator() {
		super();
		FileCoordinator.totalCreated++;
	}
	
	public void finalize() throws Throwable {
		FileCoordinator.totalFinalized++;
		super.finalize();
	}

	public Class getDutyClass() {
		return FileCoordinator.class;
	}

	public String getDutyName() {
		return "File Coordinator";
	}
	public abstract void elementAdded(FileElement e);
	public abstract void elementDisposed(FileElement e);
	/** make a new blank MesquiteProject with user input. */
	public abstract MesquiteFile createProject(String pathname, boolean createTaxaBlockIfNew);

	/** make a new blank MesquiteProject without user input. */
	public abstract MesquiteFile createBlankProject();
	
	public abstract void refreshGraphicsProjectWindow();
	public abstract void refreshProjectWindow();
	public abstract void refreshInProjectWindow(FileElement element);
	public abstract void showProjectWindow();
	public abstract void closeWindows();
	/**
	 * make a MesquiteProject, reading the information from the given path of a file (if local, interprets as file on disk; if not, as URL specification).
	 */
	public MesquiteFile readProject(boolean local, String pathname, String arguments){  //will be overridden by BasicFileCoordinator
		return readProject(local, pathname, arguments, null);
	}
	public abstract MesquiteFile readProject(boolean local, String pathname, String arguments, Class importerSubclass);
	
	/** make a MesquiteProject, using a module. */
	public abstract MesquiteFile readProjectGeneral(String arguments);

	public abstract void wrapUpAfterFileRead(MesquiteFile f);  //call after read() methods of file interpreters called outside context of the file Coordinator's project/file reading
	public abstract MesquiteFile getNEXUSFileForReading(String arguments, String message);

	public FileInterpreter findImporter(MesquiteFile f, int fileType, String arguments){
		return findImporter(f, fileType, null, arguments);
	}
	public abstract FileInterpreter findImporter(MesquiteFile f, int fileType, Class subClass, String arguments);

	public FileInterpreter findImporter(String fileContents, String fileName, int fileType, String arguments,boolean mustReadFromString, Class stateClass){
		return findImporter(fileContents, fileName, fileType, null, arguments, mustReadFromString, stateClass);
	}

	public abstract FileInterpreter findImporter(String fileContents, String fileName, int fileType, Class subClass, String arguments,boolean mustReadFromString, Class stateClass);

	public abstract MesquiteProject initiateProject(String pathName, MesquiteFile homeFile);

	/** write the given file; */
	public abstract void writeFile(MesquiteFile file);

	/** exports the given file; */
	public abstract boolean export(FileInterpreterI exporter, MesquiteFile file, String arguments);

	/** closes the given file; returns false if cancelled by user. */
	public abstract boolean closeFile(MesquiteFile file);
	/** closes the given file; returns false if cancelled by user.  Does so quietly, without interaction*/
	public abstract boolean closeFile(MesquiteFile fi, boolean quietly);

	/** Saves file. */
	public abstract void saveFile(MesquiteFile file);

	/** Saves file. */
	public abstract void saveFileAs(MesquiteFile file);

	/** Saves all files in the project. */
	public abstract void saveAllFiles();

	/* ................................................................................................................. */
	/** Finds the first employee in the m's clade of employees that manages a particular subclass of file element */
	public abstract ElementManager findManager(MesquiteModule m, Class fileElementClass);

	/** Displays a given local text file; */
	public abstract TextDisplayer displayFile(String pathName, int maxCharacters);

	/** Displays a given text file, local or not; */
	public abstract TextDisplayer displayFile(MesquiteFile file, int maxCharacters);

	/** Displays a given text string; */
	public abstract TextDisplayer displayText(String text, String windowTitle);

	public boolean isSubstantive() {
		return false;
	}

	public abstract Snapshot getIDSnapshot(MesquiteFile file);
}


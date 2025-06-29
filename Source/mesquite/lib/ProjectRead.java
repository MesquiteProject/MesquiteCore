/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.io.InputStream;
import java.util.Date;

import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.ui.MesquiteDialog;

/* ======================================================================== */
public class ProjectRead implements Runnable {
	String arguments;
	int  category;
	MesquiteModule mesquite;
	ObjectContainer projCont = null;
	public static int totalCreated = 0; //to find memory leaks
	public static int totalFinalized = 0;
	boolean wasScripting;
	Class importerSubclass = null;
	public InputStream stream;
	public ProjectRead (String arguments,  int  category, MesquiteModule mesquite, ObjectContainer p) {
		projCont = p;
		this.arguments = arguments;
		wasScripting = MesquiteThread.isScripting();
		this.category = category;  //This is whether it's regular, URL, general, etc.
		this.mesquite = mesquite;
		totalCreated++;
		//spontaneousIndicator = false;
	}
	public void finalize() throws Throwable {
		totalFinalized++;
		super.finalize();
	}
	public String getCurrentCommandName(){
		return "Reading file";
	}
	public String getCurrentCommandExplanation(){
		return null;
	}
	public void setImporterSubclass(Class importerSubclass){  //this forces the import query to show even if Mesquite thinks it can handle this as a NEXUS file
		this.importerSubclass = importerSubclass;
	}
	/*.................................................................................................................*/
	private MesquiteProject openGeneral(String arguments){
		mesquite.incrementMenuResetSuppression();
		FileCoordinator mb = (FileCoordinator)mesquite.hireEmployee(FileCoordinator.class, null); //if this could be scripted, pass into method
		if (mb==null)
			mesquite.alert("Mesquite cannot function: no file coordinator available");
		else {
			mesquite.logln("Opening external");
			Date d = new Date(System.currentTimeMillis());
			mesquite.logln("     at " + d.toString());
			/*===getting project===*/
			CommandRecord comRec = new CommandRecord(wasScripting);
			CommandRecord prevRec = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(comRec);
			mb.readProjectGeneral(arguments);
			MesquiteThread.setCurrentCommandRecord(prevRec);
			/*================*/
			mesquite.resetAllMenuBars();
			mesquite.decrementMenuResetSuppression();
			MesquiteProject project =mb.getProject();
			if (projCont != null)
				projCont.setObject(project);
			if (project==null)
				mb.iQuit();
			return project;
		}
		mesquite.decrementMenuResetSuppression();
		return null;
	}
	/*.................................................................................................................*/
	private MesquiteProject openURLString(String urlString){
		mesquite.incrementMenuResetSuppression();
		FileCoordinator mb = (FileCoordinator)mesquite.hireEmployee(FileCoordinator.class, null); //if this could be scripted, pass into method
		if (mb==null)
			mesquite.alert("Mesquite cannot function: no file coordinator available");
		else {
			if (urlString==null)
				mesquite.logln("Opening URL");
			else
				mesquite.logln("Opening URL " + urlString);
			Date d = new Date(System.currentTimeMillis());
			mesquite.logln("     at " + d.toString());
			//patience = 30;
			/*===READING FILE===*/
			CommandRecord comRec = new CommandRecord(wasScripting);
			CommandRecord prevRec = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(comRec);
			mb.readProject(false, urlString, arguments);
			/*================*/
			mesquite.resetAllMenuBars();
			mesquite.decrementMenuResetSuppression();
			MesquiteProject project =mb.getProject();
			if (projCont != null)
				projCont.setObject(project);
			if (project==null)
				mb.iQuit();
			return project;
		}
		mesquite.decrementMenuResetSuppression();
		return null;
	}
	/*.................................................................................................................*/
	private MesquiteProject openStream(){
		mesquite.incrementMenuResetSuppression();
		FileCoordinator mb = (FileCoordinator)mesquite.hireEmployee(FileCoordinator.class, null); //if this could be scripted, pass into method
		if (mb==null)
			mesquite.alert("Mesquite cannot function: no file coordinator available");
		else {
			mesquite.logln("Opening Stream " + stream);
			Date d = new Date(System.currentTimeMillis());
			mesquite.logln("     at " + d.toString());
			//patience = 30;
			/*===READING FILE===*/
			CommandRecord comRec = new CommandRecord(wasScripting);
			CommandRecord prevRec = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(comRec);
			//mb.readProject(false, urlString, arguments);
			/*================*/
			mesquite.resetAllMenuBars();
			mesquite.decrementMenuResetSuppression();
			MesquiteProject project =mb.getProject();
			if (projCont != null)
				projCont.setObject(project);
			if (project==null)
				mb.iQuit();
			return project;
		}
		mesquite.decrementMenuResetSuppression();
		return null;
	}
	String originalArguments = null; //hackathon
	public void setOriginalArguments(String orig){
		originalArguments = orig;
	}
	/*.................................................................................................................*/
	/* give alternative method which takes full file and passes it to FileCoordinator,
	which then uses alternative methods in MesquiteProject reader that parses this string
	instead of input stream*/
	private MesquiteProject openFile(String pathname){
		mesquite.incrementMenuResetSuppression();

		FileCoordinator mb = (FileCoordinator)mesquite.hireEmployee(FileCoordinator.class, null);
		if (mb==null)
			mesquite.alert("Mesquite cannot function: no file coordinator available");
		else {

			if (pathname==null)
				mesquite.logln("Opening file"); 
			else
				mesquite.logln("Opening file " + pathname);
			Date d = new Date(System.currentTimeMillis());
			mesquite.logln("     at " + d.toString());
			//patience = 30;
			/*===READING FILE===*/
			CommandRecord comRec = new CommandRecord(wasScripting);
			CommandRecord prevRec = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(comRec);

			if (originalArguments == null) //hackathon
				originalArguments = arguments;
			mb.readProject(true, pathname, originalArguments, importerSubclass); 
			/*================*/
			mesquite.resetAllMenuBars();
			mesquite.decrementMenuResetSuppression();
			MesquiteProject project =mb.getProject();
			if (projCont != null)
				projCont.setObject(project);
			if (project==null) {
				mb.iQuit();
			}
			return project;
		}
		mesquite.decrementMenuResetSuppression();
		return null;
	}
	/*.................................................................................................................*/
	/* makes and returns a new project.*/
	private MesquiteProject newFile(String arguments, boolean makeTaxa){
		FileCoordinator mb = (FileCoordinator)mesquite.hireEmployee(FileCoordinator.class, null);
		if (mb==null) {
			mesquite.alert("Mesquite cannot function: no file coordinator available");
			return null;
		}
		else {
			mb.createProject(arguments, makeTaxa); 
			MesquiteProject project = mb.getProject();
			if (projCont != null)
				projCont.setObject(project);
			if (project==null){
				mb.iQuit();
				return null;
			}
			MesquiteFile mf = project.getHomeFile();
			if (mf != null)
				mf.okForRecentRereading = true;
			if (!MesquiteThread.isScripting())
				mb.doCommand("saveNewFile", null, CommandChecker.defaultChecker);
			return mb.getProject();
		}
	}
	/*.................................................................................................................*/
	MesquiteThread thread;
	public void setThread(MesquiteThread thread) {
		this.thread = thread;
	}
	/**/
	/** DOCUMENT */
	public void run() {
		try {
			MesquiteModule.incrementMenuResetSuppression();
			CommandRecord comRec = new CommandRecord(wasScripting);
			CommandRecord prevRec = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(comRec);
			//spontaneousIndicator = false;
			Thread t = Thread.currentThread();
			if (t instanceof MesquiteThread){
				((MesquiteThread)t).setCurrent(1);
			}
			MesquiteProject proj = null;
			if (category <= 0)
				proj = newFile(arguments, category==0);
			else if (category == 1)
				proj = openFile(arguments);
			else if (category == 2)
				proj = openURLString(arguments);
			else if (category == 3)
				proj = openGeneral(arguments);

			if (originalArguments !=null){
				Parser parser = new Parser();
				if (parser.hasFileReadingArgument(originalArguments, "scriptToFileCoordinator")){
					String commands = parser.getFileReadingArgumentSubtype(originalArguments, "scriptToFileCoordinator");
					if (StringUtil.notEmpty(commands)){
						CommandRecord prev = MesquiteThread.getCurrentCommandRecord();
						CommandRecord cRec = new CommandRecord(true);
						MesquiteThread.setCurrentCommandRecord(cRec);
						MesquiteInteger pos = new MesquiteInteger(0);
						Puppeteer puppeteer = new Puppeteer(proj.getCoordinatorModule());
						puppeteer.execute(proj.getCoordinatorModule(), commands, pos, null, false, null, null);
						MesquiteThread.setCurrentCommandRecord(prev);
					}
				}
			}
			if (proj != null){
				MesquiteFile mf = proj.getHomeFile();
				if (mf != null)
					MesquiteTrunk.recentFileRecord(mf, true);  //updating Recents
			}
			//	else if (category == 4)
			//		openStream(arguments);
			if (thread !=null) {
				thread.setProgressIndicator(null);
				if (comRec != null && comRec.getProgressIndicator()!=null)
					MesquiteThread.doomIndicator(comRec.getProgressIndicator().getProgressWindow());
			}
			if (t instanceof MesquiteThread){
				((MesquiteThread)t).setCommandRecord(null);
			}
			thread = null;
			MesquiteTrunk.mesquiteTrunk.logWindow.showPrompt();
			MesquiteModule.decrementMenuResetSuppression();
		}
		catch (Exception e){
			MesquiteFile.throwableToLog(this, e);
			MesquiteTrunk.mesquiteTrunk.exceptionAlert(e, "File reading could not be completed because an exception or error occurred (i.e. a crash; " + e.getClass() + "). If you save any files, you might best use Save As... in case data were lost or file saving doesn't work properly.");
			MesquiteModule.decrementMenuResetSuppression();

		}
		catch (Error e){
			if (e instanceof OutOfMemoryError)
				MesquiteTrunk.mesquiteTrunk.discreetAlert("OutofMemoryError.  See file startingMesquiteAndMemoryAllocation.txt in the Mesquite_Folder for information on how to increase memory allocated to Mesquite.");
			else if (!(e instanceof ThreadDeath)){
				MesquiteFile.throwableToLog(this, e);
				MesquiteDialog.closeWizard(); 
				MesquiteTrunk.mesquiteTrunk.exceptionAlert(e, "File reading could not be completed because an exception or error occurred (i.e. a crash; " + e.getClass() + "). If you save any files, you might best use Save As... in case data were lost or file saving doesn't work properly.");
			}
			MesquiteModule.decrementMenuResetSuppression();
			throw e;
		}
	}

}


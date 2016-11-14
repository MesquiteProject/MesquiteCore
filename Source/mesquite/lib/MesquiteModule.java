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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringEscapeUtils;

import mesquite.lib.duties.*;
import mesquite.tol.lib.BaseHttpRequestMaker;
import edu.stanford.ejalbert.*;  //for Browserlauncher



/* ============================Class MesquiteModule============================================ */
/** The Mesquite system operates around a tree of modules (subclasses of MesquiteModule), with the class containing the "main" method being
the <strong>trunk</strong> module, and the other modules like branches that are attached to the trunk.<p>

When Mesquite starts up, the trunk starts up and surveys the mesquite folder to acquire information about all of the available modules
(i.e., all of the available subclasses of MesquiteModule). To be found, a module must be in a folder (directory) of the same name as itself; that is,
a module whose class file is "MyModule.class" must be in a directory named "MyModule".  Mesquite remembers the modules in a vector of MesquiteModuleInfo objects.  
Later, when modules need to be "hired" to perform certain tasks, this vector of information can be used to help choose which module to hire.  To be hired, a module is instantiated
and linked to its employer module.  Thus, a new branch sprouts in the Mesquite bureaucratic hierarchy. 
<p>

Modules are chosen for hire by other modules according to their talents.  What talents a module has is implicit in its superclass.  For instance, a module
that subclasses the NumberForTree class (itself a subclass of MesquiteModule) calculates a number for a tree.  Any module that
wants numbers calculated for trees can therefore hire a module that is an instance of the NumberForTree class. 
The basic talent-defining classes are part of the Mesquite Class LIbrary, in the subpackage duties.  There is in addition a system to determine whether a
module is compatible with a particular condition (e.g. operating on continuous-valued data.)<p>

The only types of module that the trunk knows of directly are the FileCoordinator and FileInterpreter classes.  Each instance
of the File Coordinator is associated with one file, and it is responsible for using employee modules to
read and write the file, and to hire tree windows and data windows.  The trunk is a specific subclass of MesquiteModule,
a MesquiteTrunk<p>  

Because menus are composed hierarchically along the employee tree,  menu composition is most easily done where there is easy access to
employee-employer relations.  For this reason, it is most convenient to have the menu composition embedded within the module code.  In order not to
burden the MesquiteModule class with numerous methods to handle employee-employer relations, the MesquiteModule class was split into three: its superclass,
EmployeeEmployer (which handles employee-employer relations), MenuOwner (which handles all menu issues) and MesquiteModule itself, which handles its basic functions.
EmployeeEmployer and MenuOwner are intended to be used ONLY as superclasses to MesquiteModule<p>

The MesquiteModule class is more or less equivalent to the MacClade proto4 ModuleRecord, while its instantiated objects
are more or less equivalent to the MacClade proto4 Tasks.  Hence, "task" is sometimes used in names for
MesquiteModule objects.<p>
 */


public abstract class MesquiteModule extends EmployerEmployee implements Commandable, Showable, Logger, FunctionExplainable,  Identifiable, FileDirtier, MesquiteListener, XMLPreferencesProcessor {
	/*.................................................................................................................*/
	/** returns build date of the Mesquite system (e.g., "22 September 2003") */
	public final static String getBuildDate() {
		return "14 November 2016";
	}
	/*.................................................................................................................*/
	/** returns version of the Mesquite system */
	public final static String getMesquiteVersion() {
		return "3.10+";
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	//As of 3.0 this becomes fixed, not changing with version)
	public static String errorReportURL =  "http://mesquiteproject.org/pyMesquiteFeedback";
	/*.................................................................................................................*/
	/** returns letter in the build number of the Mesquite system (e.g., "e" of "e58") */
	public final static String getBuildLetter() {
		//see comment under getBuildNumber
		return "";
	}
	/*.................................................................................................................*/
	/** returns number in the build number of the Mesquite system (e.g., 58 of "e58") */
	public final static int getBuildNumber() {
		//as of 26 Dec 08, build naming changed from letter + number to just number.  Accordingly j105 became 473, based on
		// highest build numbers of d51+e81+g97+h66+i69+j105 + 3 for a, b, c
		return 	795;  
	}
	//0.95.80    14 Mar 01 - first beta release 
	//0.96  2 April 01 beta  - second beta release
	//0.97 30 May 01 - not a release version
	//0.98  24 July 01 0.98
	//0.99   released 21 Aug 02   hits at posting: 12860 home page; 3251 download; 466 source
	//0.991 = d21   released 14 Sep 02    hits at posting: 13599 home page; 3460 download; 502 source
	//0.992 = d24   released 27 Sep 02    approximate hits at posting: 14171 home page; 3590 download; 525 source
	//0.993 = d42   released 10 Jan 03    approximate hits at posting: 16122 home page; 4458 download; 671 source
	//0.994 = d51   released 7 Feb 03    approximate hits at posting: 16790 home page; 4831 download; 720 source
	//0.995 = e23   released 21 May 03    approximate hits at posting: 19389 home page; 6088 download; 898 source
	//0.996 = e30   released 21 June 03     approximate hits at posting: 20028 home page; 6349 download; 958 source
	//1.0 = e58   released 22 September 03    approximate hits at posting: 21315 home page; 6880 download; 1087 source
	//1.01 = e80   released 14 January 04    approximate hits at posting: 25636 home page; 8753 download; 1359 source
	//        = e81   released 17 January 04    minor change to MesquiteTable to prevent problem with autoSizeColumns under Windows
	//1.02 = g6  released 6 May 04      approximate hits at posting: 28719 home page; 10170 download; 1574 source
	//        = g7 released 12 May 04  minor change to fix cosmetic bug
	//1.03 = g19 released 1 July 04      approximate hits at posting: 30058 home page; 10803 download; 1685 source
	//1.04 = g21 released 1 September 04      approximate hits at posting: 31636 home page; 11462 download; 1806 source
	//1.05 = g24 released 29 September 04      approximate hits at posting: 32502 home page; 11827 download; 1864 source
	//1.06 = g97 released 30 August 05      approximate hits at posting: 44106 home page; 16865 download; 2478 source
	//1.1 = h60 released 18 May 06      approximate hits at posting: 56303 home page; 22012 download; 3168 source
	//24may just before evoldir announcement 56781/22266/3209
	//1.11 = h64 released  21 June 06    approximate hits 58671/ 23167/3319
	//1.12 = h66 released 23 September 06
	//2.0 = i68 released 21 September 07  80089 / 31816 / 4666; i69 released 24 Sept to fix java 1.4 ExtensibleDialog setCaretPosition crash
	//2.01 = j27 released 7 December 07 85399/ 34315 / 4910; j28 released 7 December 07 to fix coalescence counting bug
	//2.5 = j77 released 9 June 08  95129/ 38754 / 5526
	//2.6 = 486 released 24 January 09  107577/ 45169 / 6123
	//2.7 = 510 released 26 August 09  121070/ 52820 / 6830
	//2.71 = 514 released 7 Sept 09  121886/ 53466 / 6867
	//2.72 = 527 released 11 Dec 09  129448/ 58128 / 7207;  528 released 20 Dec 09 to fix non-substantive bugs
	//2.73 = 544 released 25 July 10  144981 / 67206 / 7817
	//2.74 = 550 released 3 October 10  150117 / 71997 / 7980
	//2.75 = 564 released 30 September 2011  179839 / 91129 / 8939
	//       = 565 included 4 October 2011  in Chromaseq release; slight changes to UndoInstructions for Chromaseq
	//       = 566 update 10 October 2011, small fix in this module to re-enable error reporting of NullPointerExceptions and ArrayIndexOutOfBoundsExceptions
	//3.00  = 644 released 29 Aug 2014; 645 released 4 Sept 2014
	//3.01  = 658 released 19 Sep 2014
	//3.02  = 681 released 6 January 2015
	//3.03  = 702 released 31 March 2015
	//3.04  = 725 released 16 August 2015
	//3.10  = 765 released 27 June 2016
	/*.................................................................................................................*/
	/** returns a string if this is a special version of Mesquite */
	public final static String getSpecialVersion() {
		return "";
	}
	/*.................................................................................................................*/
	public final static String getBuildVersion() {
		return " (build " + getBuildLetter() + getBuildNumber() + ")";
	}
	/*.................................................................................................................*/


	LeakFinder leakFinder = MesquiteTrunk.leakFinderObject;
	/** Static storage so that everyone can find the trunk MesquiteModule object*/
	public static MesquiteTrunk mesquiteTrunk; 
	/** The root of the mesquite classpath.*/
	public static File mesquiteDirectory=null;  
	/** The root of the mesquite classpath.*/
	public static String mesquiteDirectoryPath=null;  
	/** The directory that includes all the preference files.*/
	public static File prefsDirectory=null;  

	/** The user's directory.*/
	public static File userDirectory=null;  
	/** The window, created by mesquiteTrunk, that displays the log.*/
	public static LogWindow logWindow;
	/** true if name of MesquiteModule is to be shown in alerts*/
	private static final boolean showModuleInAlert = true;
	/** true if name of MesquiteModule is to be shown in log entries*/
	private static final boolean showModuleInLog = false;
	/** true if alerts should show a dialog, or merely write to log.*/
	private static final boolean alertUseDialog = true;
	/** the file path to the web browser for showing web pages*/
	protected static String browserString = null;	
	/** true if does extra check for module compatibility at startup.*/
	public static boolean checkMethodsAtStartup = false;

	public static final int NEXTRELEASE = Integer.MAX_VALUE;

	/** this is for modules to store their last MesquiteNumber result for later use; intended for NumberForItem subclasses */
	protected  Object lastResult;
	/** this is for modules to store their last result string for later use; intended for NumberForItem subclasses */
	protected  String lastResultString; 


	/** The default author for this machine and user account */
	public static Author author = new Author();

	/** This module was hired as a default choice in scripting*/
	public boolean hiredAsDefaultInScripting = false;
	//
	/** Has module requested enabling of macro auto-save?*/
	private boolean autoSaveMacros = false;

	private boolean lastEmployee = false;
	public static int totalFinalized = 0;
	/** the MesquiteModuleInfo that refers to the module*/
	protected MesquiteModuleInfo moduleInfo = null;
	/** The project that the module was hired under.  Almost all modules are descendant from a file coordinating module that belongs to
	a specific project*/
	protected MesquiteProject proj=null;  
	/** for the paging system (may be defunct)*/
	Vector pagingsEphemeral = new Vector();
	/** for the paging system (may be defunct)*/
	Vector pagingsPersistent = new Vector();

	Vector subfunctions = new Vector();
	public ListableVector attachments = new ListableVector();

	public static boolean textEdgeRemembered = false;
	public static int textEdgeCompensationHeight = 5; //6 on mac; 7 on pc
	public static int textEdgeCompensationWidth = 10; //12 on mac; 28 on pc
	private static int instantiations = 0;
	private int idNumber;
	private String permanentIDString = null;
	private String assignedIDString = null;
	private static Random randomNumberGenerator;
	static {
		randomNumberGenerator = new Random(System.currentTimeMillis());
	}

	/** The constructor in general is to be avoided, because modules are instantiated momentarily on startup to gather
	information.  The usual functions of a constructor are performed by startJob*/
	public MesquiteModule () {
		super();
		setModule(this);
		instantiations++;
		idNumber = instantiations;
		permanentIDString = Integer.toString(idNumber) + "." + System.currentTimeMillis() + "." + Math.abs(randomNumberGenerator.nextLong());
		assignedIDString = null;//new String(permanentIDString);
	}
	/*.................................................................................................................*/
	/** instantiations of modules are numbered sequentially so they can be referred to by number*/
	public long getID(){
		return idNumber;
	}
	/*.................................................................................................................*/
	/** A string that uniquely refers to this module (unique even across runs of Mesquite)*/
	public String getPermanentIDString(){
		if (assignedIDString == null)
			return permanentIDString;
		else
			return assignedIDString;
	}
	/*.................................................................................................................*/
	/** A string that should uniquely refer to this module during this run time
	public String getAssignedIDString(){
		return assignedIDString;
	}
	/*.................................................................................................................*/
	/** Broadcasts that an id has been assigned to a module.  This is used for scripting, in which a module assigns itself
	an id string in a snapshot (e.g., a TreeContext) that an interested module can clue in to (e.g., TreeOfContext).  This
	allows the interested module to hook up to the assigning module even if the former was script-created before the latter
	(see interaction between BasicTreeWindow and TreeOfContext)*/
	public void broadCastAssignedID(MesquiteModule module, String assignedID){
		if (assignedID == null)
			return;
		if (employees ==null)
			return;
		Enumeration enumeration=employees.elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule mb = (MesquiteModule)enumeration.nextElement();
			mb.broadCastAssignedID(module, assignedID);
		}
	}

	/*.................................................................................................................*/
	public void incrementNumStarts(){
		getModuleInfo().incrementNumStarts();
	}
	/*.................................................................................................................*/
	/** superStartJob is called automatically when an employee is hired.  This is intended for use by superclasses of modules that need
	their own constructor-like call, without relying on the subclass to be polite enough to call super.startJob().*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	/** startJob is called automatically when an employee is hired.  The parameter scripting indicates if the hiring occurs in the context
	of automated scripting (e.g., on reading a Mesquite block of a NEXUS file).  The module can limit user interface calls (e.g. dialog boxes)
	when scripting occurs.<p>
	The MesquiteModule should override this method, to add code to 
	initialize things it needs, and to hire relevant necessary employees.  startJob must return
	true if the module was successfully started; false otherwise.  Thus, if the module needs a data matrix
	but the file has none, it returns false and the hiring process is undone.<p>
	Most modules will ignore the arguments and condition.*/
	public abstract boolean startJob(String arguments, Object condition, boolean hiredByName);


	/*.................................................................................................................*/
	/** Resets the hiring condition, e.g. that matrices returned must be continuous.  This may not always work as it was added in '09 in Stored Characters.*/
	public void setHiringCondition(Object condition){
	}
	/*.................................................................................................................*/
	/** endJob is called as a module is quitting; modules should put their clean up code here.*/
	public void endJob() {
		if (menuItemsSpecs != null) {
			menuItemsSpecs.dispose(true);
		}
		menuItemsSpecs = null;
	}
	/*.................................................................................................................*/
	public void finalize() throws Throwable {
		totalFinalized++;
		super.finalize();
	}
	/*.................................................................................................................*/
	/** dispose is called automatically when an employee is fired. It fires all employees and their subemployees etc.  
	The MesquiteModule should call endJob to 
	finalize things it needs..  NOTE: if a module wants to quit on its own accord, it should call "iQuit" so that
	the replacement hiring system can take effect.*/
	protected void dispose() {
	//	Debugg.println("########### dispose module " + this);
		if (assignedMenuSpec !=null)
			assignedMenuSpec.removeGuestModule(this);
		if (moduleMenuSpec !=null)
			moduleMenuSpec.removeGuestModule(this);
		doomed = true;
		MesquiteWindow w = getModuleWindow();
		if (w !=null) {
			if (!(w instanceof SystemWindow)){ //not needed, since quitting
				w.removeAll();
			}				
			w.setVisible(false);
		}
		boolean employerDoomed =  (employer!=null && employer.doomed);

		if (!employerDoomed)
			incrementMenuResetSuppression();
		//storePreferences();
		if (pagingsEphemeral!=null)
			pagingsEphemeral.removeAllElements();
		if (pagingsPersistent!=null)
			pagingsPersistent.removeAllElements();
		disposeMenuSpecifications();
		if (w !=null) {
			if (!w.disposed()) {
				w.dispose();
			}
			resetAllMenuBars();
		}

		//	if (employer!=null && quit) //TODO: only call this if the employer quit on its own
		//		employer.employeeQuit(this);
		if (MesquiteTrunk.trackActivity) MesquiteMessage.notifyProgrammer ("MesquiteModule " + getName() + "  closing down ");
		closeDownAllEmployees (this);
		if (employer!=null && !employer.doomed && employer.employees!=null) {
			employer.employees.removeElement(this, false);
		}
		if (!employerDoomed)
			resetContainingMenuBar();
		employer = null; //MEMORY: cut this out to promote memory leaks (tying modules together WHILE debugging NEW aug 99
		if (employees!=null)
			employees.dispose();
		employees = null;
		if (!employerDoomed)
			decrementMenuResetSuppression();
		proj = null;
		totalDisposed++;
	}
	/*.................................................................................................................*/
	/** Notifies all employees that a file is about to be closed.*/
	public void fileCloseRequested () {
		if (employees==null || doomed)
			return;
		Enumeration e = employees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mbe = (MesquiteModule)obj;
			if (mbe!=null) {
				mbe.fileCloseRequested(); 
			}
		}
	}
	/*.................................................................................................................*/
	/** To be called by a module to close down on its own (as opposed to being fired).  This might happen, for
	example, if conditions change so that the module can no longer function (e.g. all stored matrices are deleted 
	from a file, and so StoredMatrices can no longer supply matrices). A module could
	also call its own endJob() method, but iQuit is to be preferred because it evokes the automatic replacement
	hiring system if available.  (See setHiringCommand of EmployerEmployee) */
	public final void iQuit(){
		iQuit(true);
	}
	public final void iQuit(boolean giveMessage){
		incrementMenuResetSuppression();
		MesquiteCommand command = getHiringCommand();
		doomAll();
		MesquiteModule employerMod = employer;
		MesquiteModule localRefEmployer = employer;
		boolean employerDoomed =  (employer==null || employer.doomed || employerMod.quittingConditions());
		if (!employerDoomed) {
			localRefEmployer.incrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
			localRefEmployer.refreshBrowser(MesquiteModule.class);
		}
		endJob();
		dispose();
		resetAllWindowsMenus();
		/* if hiringCommand isn't null, then look among the module info's for possible replacements
		and use the hiringCommand to attempt to hire them.
		In looking for possible replacements:
			-- use the hiringCondition to find compatible modules
			-- don't choose again the current module
		With each candidate replacement, call the command's doIt method, passing the name of 
		the candidate as an argument.  If the Object returned by doIt is non-null and is an instance of the
		candidate module, then it is assume the replacement was successful and the search for replacements is ended.
		(It may make sense to notify the user of the replacement.)
		 */
		if (employerDoomed || employerMod.quittingConditions()) {
		}
		else if (command!=null){
			resetContainingMenuBar();
			if (giveMessage)
				alert("Module \"" + getName() + "\" has quit; a replacement module will be sought."); //TODO: ask user

			MesquiteModuleInfo c=null;
			//			boolean found = false;
			if (getHiringCondition()==null){
				String name =ListDialog.queryList(module.containerOfModule(), "Select replacement", "Select module to replace " + getName(), MesquiteString.helpString, getHiredAs(), null, employerMod);
				if (StringUtil.blank(name)) {

					//alert("Module \"" + getName() + "\" quit but none chosen to replace it.  This may");
					hiredAs = null;
					hiringCondition = null;
					employerMod.employeeQuit(this);
					decrementMenuResetSuppression();
					if (!employerDoomed) localRefEmployer.decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
					return;
				}
				CommandRecord cr = new CommandRecord(false);
				cr.setEmergencyRehire(true);
				Object mb = command.doIt(StringUtil.tokenize(name));
				if (mb!=null && mb instanceof MesquiteModule && ((MesquiteModule)mb).getModuleInfo()!= getModuleInfo() && getHiredAs()!=null && getHiredAs().isAssignableFrom(mb.getClass())) {
					if (giveMessage)
						alert("Module \"" + ((Listable)mb).getName() + "\" hired to replace \"" + getName() + "\", which has quit");
					decrementMenuResetSuppression();
					if (!employerDoomed) localRefEmployer.decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
					return;
				}


				MesquiteModuleInfo prevC = null;
				while ((c = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(getHiredAs(), c)) != null) {  // if wasn't successful, find first that works.
					if (c!=prevC){
						cr = new CommandRecord(false);
						cr.setEmergencyRehire(true);
						CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
						MesquiteThread.setCurrentCommandRecord(cr);
						mb = command.doIt(StringUtil.tokenize(c.getName()));
						MesquiteThread.setCurrentCommandRecord(prevR);
						if (mb!=null && mb instanceof MesquiteModule && ((MesquiteModule)mb).getModuleInfo()!= getModuleInfo() && getHiredAs()!=null && getHiredAs().isAssignableFrom(mb.getClass())) {
							alert("Module \"" + ((Listable)mb).getName() + "\" hired to replace \"" + getName() + "\", which has quit");
							decrementMenuResetSuppression();
							if (!employerDoomed) localRefEmployer.decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
							return;
						}
						prevC = c;
					}
				}
			}
			else {
				String name =ListDialog.queryList(module.containerOfModule(), "Select replacement", "Select module to replace " + getName(), MesquiteString.helpString, getHiredAs(), getHiringCondition(), employerMod);
				if (StringUtil.blank(name)) {
					hiredAs = null;
					hiringCondition = null;
					employerMod.employeeQuit(this);
					decrementMenuResetSuppression();
					if (!employerDoomed) localRefEmployer.decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
					return;
				}
				Object mb = command.doIt(StringUtil.tokenize(name));
				if (mb!=null && mb instanceof MesquiteModule && ((MesquiteModule)mb).getModuleInfo()!= getModuleInfo() && getHiredAs()!=null && getHiredAs().isAssignableFrom(mb.getClass())) {
					if (giveMessage)
						alert("Module \"" + ((Listable)mb).getName() + "\" hired to replace \"" + getName() + "\", which has quit");
					decrementMenuResetSuppression();
					if (!employerDoomed) localRefEmployer.decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
					return;
				}

				MesquiteModuleInfo prevC = null;
				while ((c = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(getHiredAs(), c, getHiringCondition(), getProject(), employerMod)) != null) {  // if wasn't successful, find first that works.
					if (c!=prevC){
						mb = command.doIt(StringUtil.tokenize(c.getName()));
						if (mb!=null && mb instanceof MesquiteModule && ((MesquiteModule)mb).getModuleInfo()!= getModuleInfo() && getHiredAs()!=null && getHiredAs().isAssignableFrom(mb.getClass())) {
							if (giveMessage)
								alert("Module \"" + ((Listable)mb).getName() + "\" hired to replace \"" + getName() + "\", which has quit");
							decrementMenuResetSuppression();
							if (!employerDoomed) localRefEmployer.decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
							return;
						}
					}
					prevC = c;
				}
			}
			alert("Employee quit but no replacement hire was found");
			employerMod.employeeQuit(this);

		}
		else {
			//alert("Module \"" + getName() + "\" has quit."); 
			employerMod.employeeQuit(this);
		}

		decrementMenuResetSuppression();
		if (!employerDoomed) localRefEmployer.decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);

	}
	/*.................................................................................................................*/
	/** Query module as to whether conditions are such that it will have to quit soon -- e.g. if its taxa block has been doomed.  The tree window, data window, 
	etc. override this to return true if their object is doomed. This is useful in case MesquiteListener disposing method is not called for an employer before one of its
	employees discovers that it needs to quit.  If the employer is going to quit anyway,there is no use to use auto rehire for the quit employee.*/
	public boolean quittingConditions(){
		if (employer!=null) {
			return employer.quittingConditions();
		}
		return false;
	}

	/*.................................................................................................................*/
	/** A method called immediately after the file has been established but not yet read in.*/
	public void projectEstablished() {
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
	}
	/*.................................................................................................................*/
	/** A method called immediately before a Mesquite block is to be read */
	public void aboutToReadMesquiteBlock(MesquiteFile f) {
	}
	/*.................................................................................................................*/
	/** A method called immediately before a file is to be saved.*/
	public void fileAboutToBeWritten(MesquiteFile f) {
	}
	/*.................................................................................................................*/
	/** A method called when a FileElement added to the project; module can respond as needed (e.g., 
 	InitializeParsimony can add default model set to a CharacterData.  (currently only called for Taxa and CharacterData additions).*/
	public void fileElementAdded(FileElement element) {
	}
	/*.................................................................................................................*/
	/** Returns duty Class the module belongs to; should be defined not by module itself but by abstract class representing duty */
	public abstract Class getDutyClass();
	/*.................................................................................................................*/
	/** Returns module info for module*/
	public MesquiteModuleInfo getModuleInfo(){
		return moduleInfo;
	}
	/** Notifies all employees that a class field has changed.*/
	public void classFieldChanged (Class c, String fieldName) {
		if (employees==null || doomed)
			return;
		Enumeration e = employees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mbe = (MesquiteModule)obj;
			if (mbe!=null) {
				mbe.classFieldChanged(c, fieldName);
			}
		}
	}
	/*.................................................................................................................*/
	Listened optionalListened = null;
	public Listened getParametersChangedNotifier(){
		if (optionalListened == null)
			optionalListened = new Listened();
		return optionalListened;
	}
	/*.................................................................................................................*/
	/** A generic call to tell employer that the module's parameters have changed
	sufficiently that its basic calculations are no longer valid, and a recalculation should
	be requested.  The employer receives the message as a call to its employeeParametersChanged.
	(Typically, this might follow an outputInvalid call, which */
	public final void parametersChanged(Notification notification) {
		//CommandRecord.tick("Parameters of module changed");
		if (employer!=null && !doomed) {
			if (!hasOldStyleEPC(employer))
				employer.employeeParametersChanged(this, this, notification);
		}
		if (optionalListened != null)
			optionalListened.notifyListeners(this, notification);
	}
	public final void parametersChanged() {
		parametersChanged(null);
	}
	
		/*.................................................................................................................*/
	/** A generic call to ask employer whether to handle something myself as employee */
	public final boolean dearEmployerShouldIHandleThis(Notification notification) {
		//CommandRecord.tick("Parameters of module changed");
		if (employer!=null && !doomed) {
				return employer.employeeRequestingIndependenceOfAction(this, this, notification);
		}
		return true;
	}

	/*.................................................................................................................*/
	private boolean hasOldStyleEPC(MesquiteModule mb){
		try {
			mb.getClass().getMethod("employeeParametersChanged", new Class[] {MesquiteModule.class, MesquiteModule.class, CommandRecord.class});
		}
		catch(NoSuchMethodException e){
			return false;
		}
		catch(SecurityException e){
			return false; //hope for best
		}
		alert("The module " + employer.getName() +" appears to be old and incompatible with the current version of Mesquite.");
		return true;
	}
	/*.................................................................................................................*/
	/** Generated by an employee calling its parametersChanged method.  The MesquiteModule should act accordingly, for instance, asking
	the employee to do a recalculation. */
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		//CommandRecord.tick("Parameters of employee module changed");
		if (employer!=null && !doomed) {
			if (!hasOldStyleEPC(employer))
				employer.employeeParametersChanged(this, source, notification);
		}
	}
	/*.................................................................................................................*/
	/** Generated by an employee calling its dearEmployerShouldIHandleThis method.  The MesquiteModule should respond false if the employer wants to handle it itself. */
	public boolean employeeRequestingIndependenceOfAction(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employer!=null && !doomed) {
				return employer.employeeRequestingIndependenceOfAction(this, source, notification);
		}
		return true;
	}
	/*.................................................................................................................*/
	/** This passes an employeeOutputInvalid call to the employer, and so on, until some employer overrides it to do something about it.  
	The purpose of this is not to call for recalculations, but primarily to allow the output to be turned blank while
	long recalculations are done. To force recalculation, parametersChanged should be called after outputInvalid is called*/
	public final void outputInvalid() {
		if (employer!=null && !doomed)
			employer.employeeOutputInvalid(this, this);
	}
	/*.................................................................................................................*/
	/** Generated by an employee calling its outputInvalide method.  The MesquiteModule should blank any output. */
	public void employeeOutputInvalid(MesquiteModule employee, MesquiteModule source) {
		if (employer!=null && !doomed)
			employer.employeeOutputInvalid(this, source);
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
	}
	/*.................................................................................................................*/
	/** Sets this module to remain one of the last employees of its employers. */
	public void setToLastEmployee(boolean last){
		lastEmployee = last;
	}
	/*.................................................................................................................*/
	/** Returns whether this module is to remain one of the last employees of its employers. */
	public boolean getIfLastEmployee(){
		return lastEmployee;
	}
	/*.................................................................................................................*/
	/** Sets whether module has requested to enable auto-save of macros. */
	public void setAutoSaveMacros(boolean aut){
		autoSaveMacros = aut;
	}
	/*.................................................................................................................*/
	/** Returns whether module has requested to enable auto-save of macros. */
	public boolean getAutoSaveMacros(){
		return autoSaveMacros;
	}
	/*.................................................................................................................*/
	/** Called typically by employer to indicate that will be called soon, therefore don't update graphics. */
	public void onHold() {
	}
	/*.................................................................................................................*/
	/** Called typically by employer to turn off hold, therefore don't update graphics. */
	public void offHold() {
	}
	/*.................................................................................................................*/
	public String supportDirectoryPath (){
		return getTempDirectoryPath()  + MesquiteFile.fileSeparator +  getShortClassName(this.getClass());
	}
	/*.................................................................................................................*/
	public static String getTempDirectoryPath (){
		return MesquiteTrunk.tempDirectory; //+ Math.abs(rng.nextInt()));
	}
	/*.................................................................................................................*/
	private String prefsPath (){
		return prefsDirectory  + MesquiteFile.fileSeparator +  getShortClassName(this.getClass()) +".pref";
	}
	/*.................................................................................................................*/
	private String prefsPathXML (){
		return prefsDirectory  + MesquiteFile.fileSeparator +  getShortClassName(this.getClass()) +".xml";
	}
	/*.................................................................................................................*/
	public String createTempDirectory(MesquiteBoolean success){
		boolean baseDirectoryExists = false;
		String base = prefsDirectory  + MesquiteFile.fileSeparator +  "temp";
		if (!MesquiteFile.fileOrDirectoryExists(base)) {
			File f = new File(base);
			baseDirectoryExists = f.mkdir();
		}  else 
			baseDirectoryExists = true;
		if (success!=null && !baseDirectoryExists)
			success.setValue(false);
		base = prefsDirectory  + MesquiteFile.fileSeparator +  "temp" + MesquiteFile.fileSeparator +  "temp" + MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase()) ;
		if (baseDirectoryExists) {
			if (!MesquiteFile.fileOrDirectoryExists(base)) {
				File f = new File(base);
				boolean b = f.mkdir();
				if (success!=null)
					success.setValue(b);
			} else if (success!=null)
				success.setValue(true);
		}
		MesquiteTrunk.tempDirectory = base;
		return base;
	}
	/*.................................................................................................................*/
	public String createTempDirectory(){
		return createTempDirectory(null);
	}
	/*.................................................................................................................*/
	public void deleteTempDirectory(){
		String directoryPath = getTempDirectoryPath();
		MesquiteFile.deleteDirectory(directoryPath);
	}

	/*.................................................................................................................*/
	public String createEmptySupportDirectory(MesquiteBoolean success){
		String base = supportDirectoryPath();
		if (MesquiteFile.fileOrDirectoryExists(base)) 
			MesquiteFile.deleteDirectory(base);
		if (!MesquiteFile.fileOrDirectoryExists(base)) {
			File f = new File(base);
			boolean b = f.mkdir();
			if (success!=null)
				success.setValue(b);
		} else if (success!=null)
			success.setValue(true);

		return base;
	}
	/*.................................................................................................................*/
	public String createSupportDirectory(MesquiteBoolean success){
		String base = supportDirectoryPath();
		if (!MesquiteFile.fileOrDirectoryExists(base)) {
			File f = new File(base);
			boolean b = f.mkdir();
			if (success!=null)
				success.setValue(b);
		} else if (success!=null)
			success.setValue(true);

		return base;
	}
	/*.................................................................................................................*/
	public String createSupportDirectory(){
		return createSupportDirectory(null);
	}

	/*.................................................................................................................*/
	public void deleteSupportDirectory(){
		String directoryPath = supportDirectoryPath();
		MesquiteFile.deleteDirectory(directoryPath);
	}


	/* XMLPreferencesDocumentation
	 * Herewith a description of how a module can use the simple, one-level deep XML preferences system:
 		1. in startjob, call loadPreferences();
 		2. call storePreferences(); where appropriate in the module (e.g., after you press ok on an options dialog box)
 		3. override preparePreferencesForXML() to prepare the XML to write
  		4. override processSingleXMLPreference(String, String) to read XML tag as it comes in.

  		Here are examples of preparePreferencesForXML and processSingleXMLPreference to use as models.  
  		These presume we have three variables whose values we want to store:

  		boolean booleanValue;
  		String stringValue;
  		int intValue;

  		and that we want to name the tags the same as the variable names:

	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "mesquiteBoolean", mesquiteBoolean);   
		StringUtil.appendXMLTag(buffer, 2, "booleanValue", booleanValue);   
		StringUtil.appendXMLTag(buffer, 2, "integerValue", integerValue);  
		StringUtil.appendXMLTag(buffer, 2, "stringValue", stringValue);  
		return buffer.toString();
	}

	public void processSingleXMLPreference (String tag, String content) {
		if ("mesquiteBoolean".equalsIgnoreCase(tag))
			mesquiteBoolean.setValue(content);
		else if ("booleanValue".equalsIgnoreCase(tag))
			booleanValue = MesquiteBoolean.fromTrueFalseString(content);
		else if ("integerValue".equalsIgnoreCase(tag))
			integerValue = MesquiteInteger.fromString(content);
		else if ("stringValue".equalsIgnoreCase(tag))
			stringValue = StringUtil.cleanXMLEscapeCharacters(content);
	}



	/*.................................................................................................................*/
	public String getXMLModuleName (){
		String moduleName = getShortClassName(this.getClass());
		if (this == mesquiteTrunk)
			moduleName = "MesquiteTrunk";
		return moduleName;
	}
	/*.................................................................................................................*/
	public String getStartOfXMLPrefs (int version){
		StringBuffer buffer = new StringBuffer(60);
		StringUtil.appendStartOfXMLFile(buffer);
		buffer.append("<mesquite>\n");
		buffer.append("\t<" + getXMLModuleName() + ">\n");
		buffer.append("\t\t<version>" +version + "</version>\n");
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public String getEndOfXMLPrefs (){
		StringBuffer buffer = new StringBuffer(60);
		buffer.append("\t</" + getXMLModuleName() + ">\n");
		buffer.append("</mesquite>\n");
		return buffer.toString();
	}


	/*.................................................................................................................*/
	/** This causes the file "prefs" in the module's directory to be read, and the contents
	are then sent to "processPreferencesFromFile".*/
	public final void loadPreferences (MesquiteString xml) {

		if (MesquiteFile.fileExists(prefsPathXML())){	
			String prefsStringXML = MesquiteFile.getFileContentsAsString(prefsPathXML());
			if (prefsStringXML!=null) {
				if (xml!=null)
					xml.setValue(prefsStringXML);
				if (!parseFullXMLDocument(prefsStringXML)) {

					XMLUtil.readXMLPreferencesFromFile(this,this, prefsPathXML());

					//					XMLUtil.readXMLPreferences(this,this, prefsStringXML);

				}
			}
			// the xml pref exists; check to see if the old one exists; if so, delete it
			if (MesquiteFile.fileExists(prefsPath())){  //old one exists; delete it
				MesquiteFile.deleteFile(prefsPath());
			}

		}
		else if (MesquiteFile.fileExists(prefsPath())){  //note that the XML version thus supercedes the .prefs version
			String[] prefsString = MesquiteFile.getFileContentsAsStrings(prefsPath());
			if (prefsString!=null)
				processPreferencesFromFile(prefsString);
		}

		//the following was added build 487 v 2. 6, to permit prefs that followed Mesquite_Folder to override (e.g., for classroom copies)
		String overridePrefsPath = getInstallationSettingsPath() + "prefs.xml";
		if (MesquiteFile.fileExists(overridePrefsPath)){	
			String prefsStringXML = MesquiteFile.getFileContentsAsString(overridePrefsPath);
			if (prefsStringXML!=null) {
				if (xml!=null)
					xml.setValue(prefsStringXML);
				if (!parseFullXMLDocument(prefsStringXML)) {
					System.out.println("Prefs overridden via file in settings directory for module " + getName());
					XMLUtil.readXMLPreferences(this,this, prefsStringXML);
				}
			}
		}
	}
	/*.................................................................................................................*/
	/** This causes the file "prefs" in the module's directory to be read, and the contents
	are then sent to "processPreferencesFromFile".*/
	public final void loadPreferences () {
		loadPreferences(null);
	}
	protected static boolean isCorrectRootTag(String tagName, Class classObj) {
		return getShortClassName(classObj).equalsIgnoreCase(tagName);
	}
	/**
	 * Hook for subclasses to provide their own xml preferences parsing implementations
	 * @return
	 */
	protected boolean parseFullXMLDocument(String xmlString) {
		return false;
	}
	/*.................................................................................................................*/
	/** This is called following a "processPreferencesFromXML" call by a module.  A module can override it
	to process an individual XML preferences string*/
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
	}
	/*.................................................................................................................*/
	/** This is called following a "loadPreferences" call by a module.  A module can override it
	to process the preferences string*/
	public void processPreferencesFromFile (String[] prefs) {
	}
	/*.................................................................................................................*/
	/** This causes the file "prefs" in the module's directory to be written, using contents
	returned by the module via "preparePreferencesForFile"*/
	public final void storePreferences () {
		String prefsStringXML = preparePreferencesForXML();
		if (prefsStringXML!=null) {
			boolean isAlreadyFullDocument = prefsStringXML.startsWith("<?xml");
			// if subclasses return a full document, just write that out.  
			// otherwise add the beginning and end of the document
			String xmlToWrite = isAlreadyFullDocument ? prefsStringXML : getStartOfXMLPrefs(getXMLPrefsVersion()) + prefsStringXML+ getEndOfXMLPrefs(); 
			MesquiteFile.putFileContents(prefsPathXML(), xmlToWrite, false);			
		} else {
			String[] prefsString = preparePreferencesForFile();
			if (prefsString!=null)
				MesquiteFile.putFileContents(prefsPath(), prefsString, false);
		}
	}
	/*.................................................................................................................*/
	/** This is called following a "storePreferences" call by a module. A module should override it
	to indicate the strings to save to its preferences file. */
	public String[] preparePreferencesForFile () {
		return null;
	}
	/*.................................................................................................................*/
	public int getXMLPrefsVersion () {
		return 1;
	}
	public boolean xmlPrefsVersionMustMatch() {
		return false;
	}
	/*.................................................................................................................*/
	/** This is called following a "storePreferences" call by a module. A module should override it
	to indicate the strings to save to its preferences file. */
	public String preparePreferencesForXML () {
		return null;
	}
	/*.................................................................................................................*/
	/** returns path to this module's directory for installation settings (i.e., belonging to Mesquite_Folder, not to the user)*/
	public String getInstallationSettingsPath() {  
		if (this == MesquiteTrunk.mesquiteTrunk)
			return getRootPath() + "settings" + MesquiteFile.fileSeparator;
		String base = getRootPath() + "settings" + MesquiteFile.fileSeparator;
		String modulePath = moduleInfo.getClassName();
		modulePath = modulePath.substring(modulePath.indexOf(".")+1, modulePath.length());
		modulePath = StringUtil.getAllButLastItem(modulePath, ".");
		modulePath = StringUtil.replace(modulePath, ".", MesquiteFile.fileSeparator) +MesquiteFile.fileSeparator ;
		//modulePath = modulePath.substring(9, modulePath.length());
		return  base + modulePath;
	}
	/*.................................................................................................................*/
	/** returns path to this module's directory*/
	public String getPath() {  
		if (this == MesquiteTrunk.mesquiteTrunk)
			return getRootPath() + "mesquite" + MesquiteFile.fileSeparator;
		if (moduleInfo == null)
			return null;
		return  moduleInfo.getDirectoryPath();
	}
	/*.................................................................................................................*/
	/** returns path to this module's package directory*/
	public String getPackagePath() {  
		if (this == MesquiteTrunk.mesquiteTrunk)
			return getRootPath() + "mesquite" + MesquiteFile.fileSeparator;
		return  moduleInfo.getPackagePath();
	}
	/*.................................................................................................................*/
	/** returns path to the image directory for this module's images*/
	public String getPackageImagesPath() {  
		if (this == MesquiteTrunk.mesquiteTrunk)
			return getRootPath() + "mesquite" + MesquiteFile.fileSeparator;
		return  moduleInfo.getPackagePath() + "images"+ MesquiteFile.fileSeparator;
	}
	/*.................................................................................................................*/
	/** returns path to the root directory of Mesquite (i.e., above mesquite, images, etc.)*/
	public static String getRootPath() {
		String s= StringUtil.getAllButLastItem(mesquiteDirectoryPath, MesquiteFile.fileSeparator);
		if (s==null)
			return null;
		else
			return s + MesquiteFile.fileSeparator;
	}
	/*.................................................................................................................*/
	/** returns path to the root directory of Mesquite images*/
	public static String getRootImageDirectoryPath() {
		String s = getRootPath();
		if (s==null)
			return null;
		else
			return s + "images/";
	}
	/*.................................................................................................................*/
	public static String getSizedRootImageFilePath(int s, String imageFileName){
		return getRootImageDirectoryPath() + s + imageFileName;
	}
	/*.................................................................................................................*/
	/** returns path to the root directory of the documentation of Mesquite*
	public static String getDocsPath() {
		String s= StringUtil.getAllButLastItem(mesquiteDirectoryPath, MesquiteFile.fileSeparator);
		if (s==null)
			return null;
		else
			return s + MesquiteFile.fileSeparator + "docs/mesquite/"; xxx
	}
	/*.................................................................................................................*/
	/** returns a local file path expected by the module.  This allows the module to say "I am going to need this".  Mesquite
 	check on startup and issues warning if not found . */
	public String  getExpectedPath(){ 
		return null;
	}
	/*.................................................................................................................*/
	/** An employee can call this to know if it's ok to query for options or do any other UI interaction involving a user response.
	 * An employer can override this to say "no".*/
	public boolean okToInteractWithUser(int howImportant, String messageToUser){
		if (whomToAskIfOKToInteractWithUser!=null)
			return whomToAskIfOKToInteractWithUser.okToInteractWithUser(howImportant, messageToUser);
		if (employer!= null)
			return employer.okToInteractWithUser(howImportant, messageToUser);
		return true;
	}
	public static final int CAN_PROCEED_ANYWAY = 0;
	public static final int WILL_FAIL_OTHERWISE = 1;

	MesquiteModule whomToAskIfOKToInteractWithUser = null;
	public void setWhomToAskIfOKToInteractWithUser(MesquiteModule m){
		whomToAskIfOKToInteractWithUser = m;
	}
	/*.................................................................................................................*/
	/** Displays an alert in log; also in dialog if flag is set.*/
	public void alert(String s, String windowTitle, String logTitle) {
		if (s == null)
			return;
		if (startupBailOut)
			return;
		if (showModuleInAlert)
			logln(logTitle+": (" + getName() + "): " + s);
		else
			logln(logTitle+": " + s);

		if (alertUseDialog) {
			AlertDialog.notice(containerOfModule(),windowTitle, s);
		}
	}
	/*.................................................................................................................*/
	/** Displays an alert in log; also in dialog if flag is set.*/
	public void alertHTML(String s, String windowTitle, String logTitle) {
		alertHTML(s, windowTitle, logTitle, 400, 400);

	}
	/*.................................................................................................................*/
	/** Displays an alert in log; also in dialog if flag is set.*/
	public void alertHTML(String s, String windowTitle, String logTitle, int width, int height) {
		if (s == null)
			return;
		if (startupBailOut)
			return;
		if (showModuleInAlert)
			logln(logTitle+": (" + getName() + "): " + s);
		else
			logln(logTitle+": " + s);

		if (alertUseDialog) {
			AlertDialog.noticeHTML(containerOfModule(),windowTitle, s, width, height, null);
		}
	}
	/*.................................................................................................................*/
	/** Displays an alert in log.*/
	public void alert(String s) {
		alert(s,"Alert", "ALERT");
	}

	/*.................................................................................................................*/
	private boolean isNonReportable(Throwable e){  //here keep a list of exceptions that are not Mesquite's problem...
		if (e == null)
			return true;
		if (e instanceof IllegalStateException)
			return true;
		if (e.getClass() == RuntimeException.class)
			return true;
		return false;
	}
	/*.................................................................................................................*/
	/** Displays an alert in connection to a detected error and offers to send to server*/
	public void reportableProblemAlert(String s) {
		MesquiteTrunk.errorReportedDuringRun = true;
		MesquiteDialog.cleanUpWizard();
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread)
			((MesquiteThread)t).doCleanUp();
		logln(s);
		Exception e = new Exception();


		if (!PhoneHomeUtil.phoneHomeSuccessful || !MesquiteTrunk.reportErrors || MesquiteTrunk.suppressErrorReporting || MesquiteThread.isScripting()){
			return;
		}
		String addendum = "";

		if (MainThread.emergencyCancelled || MesquiteTrunk.errorReportedToHome){// if emergency cancelled, reporting suppressed because silly user didn't restart!  Also, only one report per run.
			discreetAlert(s);

		}
		else if (AlertDialog.query(containerOfModule(), "Problem", s + "\n\nPlease send a report of this problem to the Mesquite server, to help us debug it and improve Mesquite.  None of your data will be sent, but your log file up to this point will be sent." + addendum, "OK, Send Report and Continue", "Close without sending"))
			reportCrashToHome(e, s);
	}
	/*.................................................................................................................*/
	/** Displays an alert in connection to an exception*/
	public void exceptionAlert(Throwable e, String s) {
		MesquiteTrunk.errorReportedDuringRun = true;
		StackTraceElement[] stt = e.getStackTrace();
		String rep = MesquiteException.lastLocMessage() + "\n";
		rep += getRootPath() + "\n";
		rep += e + "\n";
		rep += s + "\n";
		for (int i= 0; i< stt.length; i++)
			rep += stt[i] + "\n";
		s = rep;
		MesquiteDialog.cleanUpWizard();
		Thread t = Thread.currentThread();
		if (t instanceof MesquiteThread)
			((MesquiteThread)t).doCleanUp();
		logln(s);
		if (!PhoneHomeUtil.phoneHomeSuccessful || !MesquiteTrunk.reportErrors || MesquiteTrunk.suppressErrorReporting){
			if (!MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Crash", s, "OK", "Force Quit"))
				MesquiteTrunk.mesquiteTrunk.exit(true, 0);
			return;
		}
		if (MesquiteThread.isScripting()){
			return;
		}
		String addendum = "";
		if (isNonReportable(e)){
			StackTraceElement[] stack = e.getStackTrace();
			String report = MesquiteException.lastLocMessage() + "\n";
			report += e + "\n";
			report += s + "\n";
			for (int i= 0; i< stack.length; i++)
				report += stack[i] + "\n";
			logln(report);
		}
		else if (MainThread.emergencyCancelled || MesquiteTrunk.errorReportedToHome){// if emergency cancelled, reporting suppressed because silly user didn't restart!  Also, only one report per run.
			if (!MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Crash", s, "OK", "Force Quit"))
				MesquiteTrunk.mesquiteTrunk.exit(true, 0);

		}
		else {
			int resp = AlertDialog.query(containerOfModule(), "Crash", s + "\n\nPlease send a report of this crash to the Mesquite server, to help us debug it and improve Mesquite.  None of your data will be sent, but your log file up to this point will be sent." + addendum, "OK, Send Report and Continue", "OK, Send and Force Quit", "Close without sending");
			if (resp < 2)
				reportCrashToHome(e, s);
			if (resp == 1)
				MesquiteTrunk.mesquiteTrunk.exit(true, 0);
		}
	}

	/*.................................................................................................................*/
	/** Reports crash or error to Mesquite server*/
	public void reportCrashToHome(Throwable e, String s) {
		StackTraceElement[] stack = e.getStackTrace();
		String report = MesquiteException.lastLocMessage() + "\n";
		report += e + "\n";
		report += s + "\n";
		for (int i= 0; i< stack.length; i++)
			report += stack[i] + "\n";
		if (MainThread.emergencyCancelled)
			report += "\n\nEMERGENCY CANCELLED";

		report += "\n\n\n";
		report += logWindow.getText();
		report += "\n\n\n";
		reportProblemToHome(report);
		MesquiteTrunk.errorReportedToHome = true;
	}
	/*.................................................................................................................*/
	/** Displays an alert in connection to an exception*/
	public void reportableAlert(String s, String details) {
		if (!PhoneHomeUtil.phoneHomeSuccessful || !MesquiteTrunk.reportErrors || MesquiteTrunk.suppressErrorReporting){
			alert(s);
			return;
		}
		String addendum = "";
		if (MainThread.emergencyCancelled || MesquiteTrunk.errorReportedToHome){// if emergency cancelled, reporting suppressed because silly user didn't restart!  Also, only one report per run.
			discreetAlert(s);
		}
		else {
			boolean q = AlertDialog.query(containerOfModule(), "Error", s + "\n\nPlease send a report of this error to the Mesquite server, to help us understand how often this happens.  None of your data will be sent." + addendum, "OK, Send Report",  "Close without sending");
			if (q){
				reportProblemToHome(s + "\n\n" + details + "\n\n@@@@@@@@@@@@@@@\n\n" + logWindow.getText());
				MesquiteTrunk.errorReportedToHome = true;
			}
		}
	}
	/*.................................................................................................................*/
	public String getClassName() {
		return moduleInfo.getClassName();
	}
	/*.................................................................................................................*/

	/** posts a Bean to the bean log on the MesquiteServer*/
	public void postBean(String notes, boolean notifyUser) {
		if (!MesquiteTrunk.reportUse){
			return;
		}
		if (MesquiteTrunk.noBeans) {
			if (notifyUser) 
				logln("No beans were sent as the -nb flag was set.");
			return;
		}
		int numPairs=3;
		if (StringUtil.notEmpty(notes))
			numPairs=4;
		NameValuePair[] pairs = new NameValuePair[numPairs];
		pairs[0] = new NameValuePair("module", StringUtil.tokenize(moduleInfo.getClassName()));
		pairs[1] = new NameValuePair("version", StringUtil.tokenize(getVersion()));
		pairs[2] = new NameValuePair("build", ""+getBuildNumberOfPackage());
		if (numPairs>=4)
			pairs[3] = new NameValuePair("notes", StringUtil.tokenize(notes));

		if (BaseHttpRequestMaker.sendInfoToServer(pairs, "http://mesquiteproject.org/pyMesquiteBeans", null, 0)){  // changed to not retry
			if (notifyUser) 
				MesquiteMessage.println("Bean sent to Mesquite server.");
		}
		else
			if (notifyUser) 
				logln("Sorry, Mesquite was unable to connect to the server to send the bean.");
	}
	/*.................................................................................................................*/
	/** posts a Bean to the bean log on the MesquiteServer*/
	public void postBean() {
		postBean("", false);
	}
	/*.................................................................................................................*/
	/** Displays an alert in connection to an exception*/
	public void reportProblemToHome(String s) {
		String email = MesquiteString.queryString(containerOfModule(), "E-mail for follow up?", "[Optional] Thank you for reporting this problem.  " + 
				"In order to fix this bug, we may need to contact you for more details.  " + 
				"If you don't mind our contacting you, please indicate your email address here.  Thanks. " + 
				"(If you want a response urgently, please send an email directly to info@mesquiteproject.org.)", "");
		String report = "build " + MesquiteTrunk.mesquiteTrunk.getBuildNumber() + "\n";
		report += "java:" + System.getProperty("java.version") +"; vm:" + System.getProperty("java.vendor") + "; os:" + System.getProperty("os.name") + "; osversion:" + System.getProperty("os.version") + "; arch:" + System.getProperty("os.arch") + "\n";
		if (!StringUtil.blank(email))
			report += "EMAIL\t" + email +  "\n\n";
		else
			report += "EMAIL NOT GIVEN\n\n";

		report += s + "\n";
		StringBuffer response = new StringBuffer();
		if (BaseHttpRequestMaker.postToServer(report, errorReportURL, response)){
			String r = response.toString();
			if (r == null || r.indexOf("mq3rs")<0)
				discreetAlert("Sorry, Mesquite was unable to communicate properly with the server to send the report.");
			else
				AlertDialog.noticeHTML(containerOfModule(),"Note", r, 600, 400, null);
		}
		else
			discreetAlert("Sorry, Mesquite was unable to connect to the server to send the report.");


	}
	/*.................................................................................................................*/
	/** If scripting, puts alert in log; otherwise puts up alert dialog.*/
	public void discreetAlert( String s) {
		discreetAlert(MesquiteThread.isScripting(), s);
	}
	/*.................................................................................................................*/
	/** If scripting, puts alert in log; otherwise puts up alert dialog.*/
	public void discreetAlert(boolean beDiscreet, String s) {
		if (StringUtil.blank(s))
			return;
		if (beDiscreet)
			logln("Note: " + s);
		else
			alert(s,"Note", "Note");
	}
	/*.................................................................................................................*/
	/** If scripting, puts alert in log; otherwise puts up alert dialog.*/
	public void discreetAlertHTML(String s) {
		if (MesquiteThread.isScripting())
			logln("Note: " + s);
		else
			AlertDialog.noticeHTML(containerOfModule(),"Note", s, 600, 400, null);
	}
	/*.................................................................................................................*/
	/** Displays a message and returns false.  For use when a method fails and is returning false to indicate this, and needs to present a notice to the user.
	(This method was made so as to change easily statements of "return false" so that they also gave a message, without having to split into two lines.) */
	public boolean sorry(String s) {
		CommandRecord.tick("Sorry, module couldn't be started");
		if (!startupBailOut){
			if (!MesquiteThread.isScripting()) 
				alert(s);
			else
				logln("Message from " + getNameForMenuItem() + ": " + s);
		}
		return false;
	}

	boolean useSysOut(){
		return (!MesquiteTrunk.suppressSystemOutPrintln && ((!MesquiteTrunk.isMacOS() || (MesquiteTrunk.isMacOSX() && MesquiteTrunk.getJavaVersionAsDouble()>1.39))));

	}
	/*.................................................................................................................*/
	/** Places string in log AND in System.out.println.*/
	public void log(String s, boolean echoToDetails) {
		logNoEcho(s);
		if (useSysOut())
			System.out.print(s);
		if (echoToDetails)
			CommandRecord.tick(s);
	}
	/*.................................................................................................................*/
	/** Places string in log AND in System.out.println.*/
	public void log(String s) {
		logNoEcho(s);
		if (useSysOut())
			System.out.print(s);
	}
	/*.................................................................................................................*/
	/** Places string and newline character in log AND in System.out.println.*/
	public void logln(String s, boolean echoToDetails) {
		loglnNoEcho(s);
		if (useSysOut())
			System.out.println(s);
		if (echoToDetails)
			CommandRecord.tick(s);
	}
	/*.................................................................................................................*/
	public void loglnEchoToStringBuffer(String s, StringBuffer sb){
		logln(s);
		if (sb!=null) {
			sb.append(s);
			sb.append(StringUtil.lineEnding());   
		}
	}
	/*.................................................................................................................*/
	/** Places string and newline character in log AND in System.out.println.*/
	public void logln(String s) {
		loglnNoEcho(s);
		MesquiteThread.loglnToThreadLogger(s);
		if (useSysOut())
			System.out.println(s);
	}
	/*.................................................................................................................*/
	/** Places string in log.*/
	public void logNoEcho(String s) {
		if (logWindow!=null) {
			if (showModuleInLog)
				logWindow.append(getName() + "-- " + s);
			else
				logWindow.append(s);
		}
		MesquiteFile.writeToLog(s);  //TODO: have a flag that turns this on
		if (this != mesquiteTrunk){
			Object cOM = containerOfModule();
			if (cOM instanceof MesquiteWindow)
				((MesquiteWindow)cOM).log(s); //Wayne: this seems to be causing loss of focus in lo9g window
		}
		CommandRecord cRec = MesquiteThread.getCurrentCommandRecord();
		if (cRec != null && cRec.getEchoCommunicator()!=null)
			cRec.getEchoCommunicator().log(s);
	}
	/*.................................................................................................................*/
	/** Places string and newline character in log.*/
	public void loglnNoEcho(String s) {
		if (logWindow!=null) {
			if (showModuleInLog)
				logWindow.append(getName() + "-- " + s + "\n");
			else
				logWindow.append(s + "\n");
		}
		MesquiteFile.writeToLog(s+ StringUtil.lineEnding());
		if (this != mesquiteTrunk){
			Object cOM = containerOfModule();
			if (cOM != null && cOM instanceof MesquiteWindow)
				((MesquiteWindow)cOM).logln(s);
		}
		CommandRecord cRec = MesquiteThread.getCurrentCommandRecord();
		if (cRec != null && cRec.getEchoCommunicator()!=null)
			cRec.getEchoCommunicator().log(s);
	}
	/*.................................................................................................................*/
	/** Shows the log window.*/
	public static void showLogWindow() {
		showLogWindow(true);
	}
	/*.................................................................................................................*/
	/** Shows the log window.*/
	public static void showLogWindow(boolean bringToFront) {
		if (logWindow!=null) {
			if (bringToFront || !logWindow.isVisible())
				logWindow.setVisible(true);
		}
	}
	/*.................................................................................................................*/
	/** This requests that ALL tool palettes be recomposed.*/
	public static final void resetAllToolPalettes() {
		Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw!=null) {
				ToolPalette palette = mw.getPalette();
				if (palette != null) {
					palette.recheckSize();
					palette.repaint();
					palette.repaintAll();
				}
			}
		}
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
	}
	/*.................................................................................................................*/
	/** Requests modules to start profiling.*/
	public void startProfiling(){
		Enumeration e = employees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mbe = (MesquiteModule)obj;
			mbe.startProfiling();
		}
	}
	/*.................................................................................................................*/
	/** Requests modules to report profiling.*/
	public void reportProfiling(){
		Enumeration e = employees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mbe = (MesquiteModule)obj;
			mbe.reportProfiling();
		}
	}
	public boolean permanentIDExists(String id) {
		if (id == null || employees ==null)
			return false;
		if (id.equalsIgnoreCase(getPermanentIDString())) //had been getAssignedIDString
			return true;
		Enumeration enumeration=employees.elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule mb = (MesquiteModule)enumeration.nextElement();
			if (mb.permanentIDExists(id))
				return true;
		}
		return false;
	}

	/*.................................................................................................................*/
	/** Sets  last results variables to unassigned or null */
	public void clearResultAndLastResult(Object result){
		if (result instanceof MesquiteNumber) {
			((MesquiteNumber)result).setToUnassigned();
			((MesquiteNumber)result).copyAuxiliaries((MesquiteNumber[])null);

		}
		else if (result instanceof NumberArray) {
			((NumberArray)result).deassignArray();

		}
		lastResult = null;
		lastResultString=null;
	}
	/*.................................................................................................................*/
	/** Sets all last result variables to unassigned or null */
	public void clearLastResult(){
		lastResult = null;
		lastResultString=null;
	}
	/*.................................................................................................................*/
	/** Store a value in lastResult.*/
	public void saveLastResult(Object result){
		if (result==null)
			this.lastResult=null;
		else if (result instanceof MesquiteNumber) {
			this.lastResult = new MesquiteNumber((MesquiteNumber)result, true);
		}
		else if (result instanceof NumberArray) {
			this.lastResult = ((NumberArray)result).cloneArray();
		}
		else
			this.lastResult = result;

	}
	/*.................................................................................................................*/
	/** Store a value in lastResult.*/
	public void saveLastResult(double lastResult){
		this.lastResult = new MesquiteNumber(lastResult);
	}
	/*.................................................................................................................*/
	/** Store a value in lastResult.*/
	public void saveLastResult(int lastResult){
		this.lastResult = new MesquiteNumber(lastResult);
	}
	/*.................................................................................................................*/
	/** Store a value in lastResult.*/
	public void saveLastResult(long lastResult){
		this.lastResult = new MesquiteNumber(lastResult);
	}
	/*.................................................................................................................*/
	/** Store a value in lastResultString.*/
	public void saveLastResultString(String lastResultString){
		this.lastResultString = lastResultString;
	}
	/*.................................................................................................................*/
	/** Store a value in lastResultString.*/
	public void saveLastResultString(MesquiteString lastResultString){
		if (lastResultString==null)
			this.lastResultString = null;
		else
			this.lastResultString = lastResultString.getValue();
	}
	/*.................................................................................................................*/
	/** return whether or not this module should have snapshot saved when saving a macro given the current snapshot mode.*/
	public boolean satisfiesSnapshotMode(){
		return (MesquiteTrunk.snapshotMode == Snapshot.SNAPALL);
	}
	/*.................................................................................................................*/
	/** return the value of items to be snapshotted when saving a macro.*/
	public int getMacroSnapshotMode(){
		return Snapshot.SNAPALL;
	}
	/*.................................................................................................................*/
	/** return the module responsible for snapshotting when saving a macro.*/
	public MesquiteModule getMacroSnapshotModule(){
		return this;
	}
	/*.................................................................................................................*/
	/** return the command string to get the module responsible for snapshotting when saving a macro.*/
	public String getMacroSnapshotModuleCommand(){
		return null;
	}

	public Object doCommand(String commandName, String arguments) {
		return doCommand(commandName, arguments, CommandChecker.defaultChecker);
	}

	/*.................................................................................................................*/
	/** A request for the MesquiteModule to perform a command.  It is passed two strings, the name of the command and the arguments.
	This should be overridden by any module that wants to respond to a command.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(MesquiteModule.class, null, null, commandName, "showEmployeeTree")) {
			EmployeeTree etM = (EmployeeTree)findImmediateEmployeeWithDuty(EmployeeTree.class);
			if (etM == null)
				etM = (EmployeeTree)hireEmployee(EmployeeTree.class, null);
			if (etM!=null)
				etM.showEmployeeTreeWindow(this);
			return etM;
		}
		else if (checker.compare(MesquiteModule.class, "Show employers of this module", null, commandName, "employers")) {
			logln(getEmployerPath());
		}
		else  if (checker.compare(MesquiteModule.class, "Executes a shell script", "[path to script file]", commandName, "executeShellScript")) {
			String scriptPath = parser.getFirstToken(arguments);
			if (StringUtil.blank(scriptPath)) 
				return null;
			else {
				ShellScriptUtil.executeAndWaitForShell(scriptPath, getName());
			}
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "dumpParameterTree")) {
			System.out.println("=============\nParameters of modules of window of " + getName() + "\n" + listEmployeeParameters("") + "\n=============");
		}
		else  if (checker.compare(MesquiteModule.class, "Shows manual in browser", null, commandName, "showManual")) {
			showManual();
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "showCommandPage")) {
			showWebPage(getCommandPagePath(), true);
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "showMiniInfoWindow")) {
			return new ModuleInfoWindow(module);
		} 
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "openSourceInEclipse")) {
			openSourceInEclipse();
		}
		else if (checker.compare(getClass(), "Logs last result string", null, commandName, "logLastResultString")) {
			logln("Last result:\n" +  lastResultString);
		}
		else if (checker.compare(getClass(), "Returns last result string", null, commandName, "getLastResultString")) {
			return lastResultString;
		}
		else if (checker.compare(getClass(), "Returns last MesquiteNumber result", null, commandName, "getLastResult")) {
			return lastResult;
		}

		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "setAssignedID")) {
			if (getFileCoordinator()!=null && getFileCoordinator().permanentIDExists(arguments))
				assignedIDString = getPermanentIDString(); // make new one if old exists
			else
				assignedIDString = arguments;
			if (getFileCoordinator()!=null)
				getFileCoordinator().broadCastAssignedID(this, arguments);
			else
				mesquiteTrunk.broadCastAssignedID(this, arguments);
			return assignedIDString;
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "getWindow")) {
			return getModuleWindow();
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "getContainingWindow")) {
			return containerOfModule();
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "saveMacro")) {
			MesquiteTrunk.snapshotMode = getMacroSnapshotMode();
			MesquiteModule mb = getMacroSnapshotModule();
			if (mb ==null){
				discreetAlert("The Macro snapshot module can't be found.");
				return null;
			}
			boolean otherModule = !mb.equals(this);
			String prefix = "";
			if (otherModule) {
				prefix = "\t" + getMacroSnapshotModuleCommand()+";" + StringUtil.lineEnding();
				prefix+="\t\ttell It;" + StringUtil.lineEnding();
			}
			String recipe = Snapshot.getSnapshotCommands(getMacroSnapshotModule(), null, "");
			if (otherModule) {
				recipe = prefix + recipe + "\t\tendTell;" + StringUtil.lineEnding();

			}
			MesquiteMacro.saveMacro(this, "Untitled Macro for " + getNameForMenuItem(), 0, recipe);
			MesquiteTrunk.snapshotMode = Snapshot.SNAPALL;
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "applyMacro")) {
			MesquiteModuleInfo mmi = getModuleInfo();
			if (mmi==null)
				return null;
			Vector macros = mmi.getMacros();
			if (macros ==null || macros.size()<=0)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int macroNumber = MesquiteInteger.fromString(arguments, io); 
			if (macroNumber>=0 && macroNumber<macros.size()) {
				MesquiteMacro macro = (MesquiteMacro)macros.elementAt(macroNumber);
				if (macro!=null) {
					Puppeteer p = new Puppeteer(this);
					p.applyMacroFile(macro.getPath(), this);
				}
			}

			return null;
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "showWindow")) {
			if (getModuleWindow()!=null) {
				getModuleWindow().setVisible(true, MesquiteThread.isScripting());
				//getModuleWindow().repaintAll();
			}
			return getModuleWindow();
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "showWindowForce")) {

			if (getModuleWindow()!=null) {
				getModuleWindow().setVisible(true);
				getModuleWindow().getParentFrame().showFrontWindow();
				//getModuleWindow().repaint();
			}
			return getModuleWindow();
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "repaintWindow")) {
			if (getModuleWindow()!=null) {
				getModuleWindow().repaintAll();
			}
			return null;
		}
		else if (checker.compare(MesquiteModule.class, null, null, commandName, "showFile")) {
			if (arguments==null)
				return null;
			FileCoordinator fc = getFileCoordinator();
			if (fc!=null)
				return fc.displayFile(MesquiteFile.composePath(getProject().getHomeDirectoryName(), parser.getFirstToken(arguments)), -1);  //

		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "getFileCoordinator")) {
			return getFileCoordinator();
		}
		else  if (checker.compare(MesquiteModule.class, null, null, commandName, "getProject")) {
			return getProject();
		}
		else if (checker.compare(MesquiteModule.class, null, null, commandName, "getProjectID")) {
			MesquiteProject proj = getProject();
			if (proj!=null)
				return new MesquiteInteger((int)proj.getID());
		}
		else if  (checker.compare(MesquiteModule.class, null, null, commandName, "getEmployee")) {
			MesquiteModule mb =  findEmployeeWithName(parser.getFirstToken(arguments));
			return mb;
			//TODO: add getEmployeeWithDuty and pass duty class name
		}
		else if  (checker.compare(MesquiteModule.class, null, null, commandName, "getImmediateEmployee")) {
			MesquiteModule mb =  findEmployeeWithName(parser.getFirstToken(arguments),true);
			return mb;
		}
		else if  (checker.compare(MesquiteModule.class, null, null, commandName, "hireEmployee")) {
			MesquiteModule mb = hireNamedEmployee(MesquiteModule.class, arguments);
			return mb;
		}
		else if  (checker.compare(MesquiteModule.class, null, null, commandName, "notifyParametersChanged")) {
			parametersChanged();
		}
		else if  (checker.compare(MesquiteModule.class, null, null, commandName, "fireEmployee")) {
			MesquiteModule mb = findEmployeeWithName(parser.getFirstToken(arguments));
			if (mb!=null)
				fireEmployee(mb);
		}
		else if  (checker.compare(MesquiteModule.class, null, null, commandName, "resign")) {
			iQuit();
		}
		else if (checker.getAccumulateMode()) {
			if (this instanceof CommandableOwner){
				Commandable[] commandables = ((CommandableOwner)this).getCommandablesForAccumulation();

				for (int i=0; commandables!=null && i<commandables.length; i++){
					checker.addString("<ul><li><strong>Commands for " + getShortClassName(commandables[i].getClass())+ "</strong>");
					checker.accumulateOnlyFrom(commandables[i].getClass());
					commandables[i].doCommand(null, null, checker);//���
					checker.addString("</ul>");
				}
			}
		}
		else {
			//AFTERDEMO:
			if (commandName!=null && !checker.getAccumulateMode() && checker.warnIfNoResponse) {
				MesquiteMessage.warnProgrammer("Module " + getName() + " (" + (getClass().getName()) + ") did not respond to command " + commandName + " with arguments (" + arguments + ")");
			}
		}
		/* // the following should be place so that superclasses can respond to command, at least where latter is commandable
    	 	else 
    	 		return  super.doCommand(commandName, arguments, checker);
		 */
		return null;
	}
	/*___________________________ Windows __________________________*/
	/*.................................................................................................................*/
	/** Returns the window of the MesquiteModule, or if none, the window of the nearest employer with a window.
	EMBEDDED VERSION  *
 	public Frame containerOfModule() {
		Object f = getParent ();
		while (! (f instanceof Frame) && !(f==null))
			f = ((Component) f).getParent ();
		return  (Frame) f;
   	 }
	/*.................................................................................................................*/
	/** Returns the window of the MesquiteModule, or if none, the window of the nearest employer with a window.
	NONEMBEDDED VERSION*/
	public MesquiteWindow containerOfModule() {
		if (getModuleWindow()!=null ) 
			return getModuleWindow();
		else if (employer!=null)
			return employer.containerOfModule();
		else
			return null;
	}
	/*.................................................................................................................*/
	/** Returns the window of the MesquiteModule, or if none, the window of the nearest employer with a window.
	NONEMBEDDED VERSION*/
	public MesquiteWindow visibleContainerOfModule() {
		if (getModuleWindow()!=null && getModuleWindow().isVisible()) 
			return getModuleWindow();
		else if (employer!=null)
			return employer.visibleContainerOfModule();
		else
			return null;
	}
	/*.................................................................................................................*/
	/** Returns the employer with a window*/
	public MesquiteModule employerWithWindow() {
		if (getModuleWindow()!=null) 
			return this;
		else if (employer!=null)
			return employer.employerWithWindow();
		else
			return null;
	}
	/*.................................................................................................................*/
	/** Returns the project of the MesquiteModule*/
	public MesquiteProject getProject() {
		if (proj!=null) 
			return proj;
		else if (employer!=null)
			return employer.getProject();
		else {
			//System.out.println("Error: request by \"" + getName() + "\" for current project, but project is null");
			return null;
		}
	}
	/*.................................................................................................................*/
	/** sets the file of the MesquiteModule*/
	public void setProject(MesquiteProject proj) {
		this.proj = proj;
	}
	/*.................................................................................................................*/
	/** Requests a window to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow != null)
			whichWindow.hide();
		//whichWindow.dispose();
	}
	/*.................................................................................................................*/
	/** Requests a panel to close.*/
	public void panelGoAway(Panel p) {
	}

	/*__________________________________________________________*/
	/*___________________________ Commands __________________________*/
	/*.................................................................................................................*/
	/**  Commands are created here.  This method is used in case Mesquite wants to catalogue available Commands
	(so far, this has not proved useful).  The commands used to be stored within a single static vector, but this caused many otherwise
	defunct objects (the Commandables that are the object of the commands) to be referenced and thus not GC'd.
	In the future, commands might be more sophisticated,
	and passed here will be not only the commandName, but also the parameter types expected and how they are to be called.
	See the sublibrary for more information about Commands. */
	public final static MesquiteCommand makeCommand(String commandName, Commandable ownerObject){
		MesquiteCommand c = new MesquiteCommand(commandName, ownerObject);
		return c;
	}
	/*__________________________________________________________*/



	/*__________________________________________________________*/
	/*___________________________ NEXUS file reading/writing __________________________*/
	/*.................................................................................................................*/
	/** Adds NEXUS block to the file. */
	public void addNEXUSBlock(NexusBlock nb){
		FileCoordinator fCoord = getFileCoordinator();
		NexusFileInterpreter fi = (NexusFileInterpreter)fCoord.findEmployeeWithDuty(NexusFileInterpreter.class);
		if (fi!=null) {
			fi.addBlock(nb);
		}
	}
	/*.................................................................................................................*/
	/** Remove NEXUS block from the file. */
	public void removeNEXUSBlock(NexusBlock nb){
		FileCoordinator fCoord = getFileCoordinator();
		NexusFileInterpreter fi = (NexusFileInterpreter)fCoord.findEmployeeWithDuty(NexusFileInterpreter.class);
		if (fi!=null) {
			fi.removeBlock(nb);
		}
	}
	/*.................................................................................................................*/
	/** Finds the NEXUS block containing the given FileElement. */
	public NexusBlock findNEXUSBlock(FileElement e){
		try{
			FileCoordinator fCoord = getFileCoordinator();

			NexusFileInterpreter fi = (NexusFileInterpreter)fCoord.findEmployeeWithDuty(NexusFileInterpreter.class);
			if (fi!=null)
				return fi.findBlock(e);
			else return null;
		}
		catch(Exception ex){
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Read the nexus block passed (passed only to modules claiming they can read it). */
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments) {return null;} 
	/** Read the nexus command in the given block (passed only to modules claiming they can read it).  Returns true if successful*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){
		return false;
	}
	/*.................................................................................................................*/
	private boolean findReaderForCommand (MesquiteFile mf, NexusBlock nBlock, String blockName, String commandName, String command, MesquiteString comment, MesquiteModule mb) {
		if (mb ==null || mb.getEmployeeVector()== null)
			return false;
		Enumeration enumeration=mb.getEmployeeVector().elements(); 

		//look among employees for one that can read
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			MesquiteModule employeeModule = (MesquiteModule)obj;
			MesquiteModuleInfo mbi = employeeModule.getModuleInfo();
			if (mbi==null)
				System.out.println("no employees of ownerModule!!!");
			else if (mbi.nexusCommandTest!=null) {
				if (mbi.nexusCommandTest.readsWritesCommand(blockName, commandName, command)) {
					if (employeeModule.readNexusCommand(mf, nBlock, blockName, command, comment))
						return true;
				}
			}
		}
		enumeration=mb.getEmployeeVector().elements(); // WHY ARE ONLY EMPLOYEES OF FILE COORDINATOR USED????
		//look among employees for one that can read
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			MesquiteModule employeeModule = (MesquiteModule)obj;
			if (findReaderForCommand(mf, nBlock, blockName, commandName, command, comment, employeeModule)) {
				return true;
			}
		}

		return false;
	}
	/*.................................................................................................................*/
	/** Read the unrecognized command, first by looking for a module reading it, second by storing it as unrecognized within the nexus block. */
	public void readUnrecognizedCommand(MesquiteFile file, NexusBlock nBlock, String blockName, FileBlock block, String commandName, String command, StringBuffer blockComments, MesquiteString comment){
		if (!findReaderForCommand(file, nBlock, blockName, commandName, command, comment, getFileCoordinator())) {
			if (")".equals(commandName) || "null".equals(commandName)) //to catch an only file reading/writing problem
				file.setOpenAsUntitled("There appears to be a problem in the file format.  Illegal NEXUS command (\"" + commandName + "\", in " + blockName + " block). ");
			else if (";".equals(commandName)) //to catch an only file reading/writing problem
				;
			else if (nBlock!=null) 
				nBlock.storeUnrecognizedCommand(command);
		}
	}
	/*.................................................................................................................*/
	/** Writes any nexus commands to file. */
	public boolean writeNexusCommands(MesquiteFile file, String blockName, MesquiteString pending){ 
		String s = getNexusCommands(file, blockName);
		if (!StringUtil.blank(s)) {
			if (pending != null && !pending.isBlank()){

				file.writeLine(pending.toString());
				pending.setValue("");
			}
			file.writeLine(s);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/** Return any nexus commands belonging to block in file. */
	public String getNexusCommands(MesquiteFile file, String blockName){ return "";}

	/*.................................................................................................................*/
	/** check if any adjustments are needed before writing, .e.g. resolve name conflicts.  Format is file type, e.g. NEXUS, NEXML. */
	public void preWritingCheck(MesquiteFile file, String format){		
	}


	/** Return Mesquite commands that will put the module (approximately) back into its current state. Used
	so that on file save, a Mesquite block can be saved that will return the user more or less to previous state. */
	public Snapshot getSnapshot(MesquiteFile file) {  //this allows employees to be dealt with
		Snapshot temp = new Snapshot();

		/* examples
 		temp.addLine("toggleMesquiteBoolean " + mesquiteBoolean.toOffOnString());
		temp.addLine("setPrimerInfoSource " +  StringUtil.tokenize(primerInfoTask.getClassName()));  

		 */

		return temp;
	}

	/*__________________________________________________________*/
	/** for the paging system (may be defunct)*/
	public void pageModule(MesquiteModule fromModule, boolean persistent){
		if (!persistent)
			pagingsEphemeral.addElement(fromModule);
		else
			pagingsPersistent.addElement(fromModule);
	}
	/*.................................................................................................................*/
	/** for the paging system (may be defunct)*/
	public void removePaging(MesquiteModule fromModule){
		pagingsEphemeral.removeElement(fromModule);
		pagingsPersistent.removeElement(fromModule);
	}
	/*.................................................................................................................*/
	/** for the paging system (may be defunct)*/
	protected void returningPage(MesquiteModule pagedModule){
	}
	/*.................................................................................................................*/
	/** for the paging system (may be defunct)*/
	protected void respondToPaging(){
		Enumeration enumeration = pagingsEphemeral.elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule fromModule = (MesquiteModule)enumeration.nextElement();
			fromModule.returningPage(this);
		}
		enumeration = pagingsPersistent.elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule fromModule = (MesquiteModule)enumeration.nextElement();
			fromModule.returningPage(this);
		}
		pagingsEphemeral.removeAllElements();
	}
	/*___________________________ Utilities __________________________*/
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	private boolean parametersInPath(MesquiteModule module) {
		if  (StringUtil.blank(module.getParameters())) {
			if (module.getEmployeeVector()==null)
				return false;
			int num = module.getEmployeeVector().size();
			for (int i=0; i<num; i++) {
				Object obj = module.getEmployeeVector().elementAt(i);
				MesquiteModule mb = (MesquiteModule)obj;
				if (parametersInPath(mb))
					return true;
			}
		}
		else
			return true;
		return false;
	}
	/*.................................................................................................................*/
	public String accumulateParameters(String spacer) {
		if (parametersInPath(this)) {
			String thisBranch;
			if  (StringUtil.blank(getParameters()))
				thisBranch = spacer + getName()  + StringUtil.lineEnding();
			else
				thisBranch=spacer + getName() + ":  " + getParameters() + StringUtil.lineEnding();
			if (getEmployeeVector()!=null){
				int num = getEmployeeVector().size();
				spacer +="    ";
				for (int i=0; i<num; i++) {
					Object obj = getEmployeeVector().elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					thisBranch += mb.accumulateParameters(spacer);
				}
			}
			return thisBranch;
		}
		else
			return "";
	}
	/** returns current parameters, for logging etc.. */
	public String getParameters() {
		return "";
	}
	/** returns name plus current parameters, for logging etc.. */
	public String getNameAndParameters() {
		String p = getParameters();
		if (StringUtil.blank(p))
			return getName();
		else
			return getName() + " (" + p + ")";
	}
	/*.................................................................................................................*/
	/** Lists  the parameters of  all employees of this MesquiteModule.*/
	//TODO: use stringbuffer here (pass it)
	public String listEmployeeParameters (String spacer) {
		String thisBranch="";

		thisBranch += spacer +getName() + ": " + getParameters() + StringUtil.lineEnding();
		int num = employees.size();
		spacer +="  ";
		for (int i=0; i<num; i++) {
			Object obj = employees.elementAt(i);
			MesquiteModule mb = (MesquiteModule)obj;
			//thisBranch += spacer + mb.getName() + ": " + mb.getParameters() + StringUtil.lineEnding();
			thisBranch += mb.listEmployeeParameters(spacer);
		}
		return thisBranch;
	}

	public String listEmployees (String spacer, MesquiteInteger count) {
		String thisBranch="";

		int num = employees.size();
		spacer = "    " + spacer;
		for (int i=0; i<num; i++) {
			count.increment();
			Object obj = employees.elementAt(i);
			MesquiteModule mb = (MesquiteModule)obj;
			thisBranch += spacer + count.getValue() + "  -- " + mb.getName() + StringUtil.lineEnding();
			thisBranch += mb.listEmployees(spacer, count);
		}
		return thisBranch;
	}
	MesquiteModule getDeepEmployee (int target, MesquiteInteger count) {
		for (int i=0; i<employees.size(); i++) {
			count.increment();
			MesquiteModule mb = (MesquiteModule)employees.elementAt(i);
			if (count.getValue() == target)
				return mb;
			MesquiteModule found = mb.getDeepEmployee(target, count);
			if (found !=null)
				return found;
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns short (not full package) name of class*/
	public static String getShortClassName(Class classToShow) {
		if (classToShow == null) {
			return "";
		}
		else {
			return StringUtil.getLastItem(classToShow.getName(), ".");
		}
	}
	/*.................................................................................................................*/
	/** returns short (not full package) name of class*/
	public String getShortClassName() {
		return StringUtil.getLastItem(getClass().getName(), ".");
	}
	/*.................................................................................................................*/
	/** returns whether name of Module matches passed String.  First checks to see if name matches directly (e.g., "Tree Window"); then checks to see if matches at whole package path
	(e.g. mesquite.minimal.BasicTreeWindowMaker.BasicTreeWindowMaker); then checks to see if matches at immediate classname (e.g., BasicTreeWindowMaker)*/
	public boolean nameMatches(String s) {
		if (s==null)
			return false;
		if (s.equalsIgnoreCase(getName()))
			return true;
		if (s.equals(getClass().getName()))
			return true;
		if (s.equals(getShortClassName()))
			return true;
		s = StringUtil.getLastItem(s, ".");
		if (s!=null && s.equals(getShortClassName()))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether the passed class, or a superclass or interface of it, has the passed name*/
	public static boolean nameIsInstanceOf(String n, Class c){
		if (c==null || StringUtil.blank(n))
			return false;
		Class sup= c;
		while (sup!=null) {
			if (n.equalsIgnoreCase(getShortClassName(sup)))
				return true;
			if (n.equalsIgnoreCase(sup.getName()))
				return true;
			sup = sup.getSuperclass();
		}
		//also check using  Class[] getInterfaces() to see if the class has an interface of the given name
		Class[] interfaces = c.getInterfaces();
		for (int i=0; i<interfaces.length; i++) {
			if (interfaces[i].getName().equalsIgnoreCase(n))
				return true;
		}
		return false;

	}
	/*.................................................................................................................*/
	/** returns path to manual.  Null if manual doesn't exist*/
	public String getManualPath() {
		if (MesquiteTrunk.isApplet()) 
			return null;
		String manualPath;
		String manP;
		if (this instanceof MesquiteTrunk)
			return mesquiteWebSite;
		else
			manP= "manual.html";
		manualPath = getPath() + manP; 
		File testing = new File(manualPath);
		if (testing.exists())
			return manualPath;
		else
			return null;
	}
	/*.................................................................................................................*/
	/** returns path to page showing the commands.  Null if page doesn't exist*/
	public String getCommandPagePath() {
		if (MesquiteTrunk.isApplet()) 
			return null;
		if (!CommandChecker.documentationComposed) {
			CommandChecker checker = new CommandChecker();
			checker.composeDocumentation();
		}	
		String manualPath;
		manualPath = MesquiteModule.prefsDirectory + MesquiteFile.fileSeparator + "commands" + MesquiteFile.fileSeparator  + MesquiteModule.getShortClassName(getClass()) + ".html"; 

		File testing = new File(manualPath);
		if (testing.exists()) {
			return manualPath;
		}
		else {
			return null;
		}
	}

	public static final String mesquiteWebSite = "http://mesquiteproject.wikispaces.com";
	/*.................................................................................................................*/
	/** returns path to manual.  Null if manual doesn't exist*/
	public String getBrowserManualPath() { 
		if (MesquiteTrunk.isApplet()) 
			return null;
		String manualPath;
		//String manP;
		if (this instanceof MesquiteTrunk)
			return mesquiteWebSite;
		else
			manualPath= getPath() + "manual.html";
		File testing = new File(manualPath);
		if (testing.exists()) {
			return manualPath;
		}
		else
			return null;
	}
	/*.................................................................................................................*/
	/** shows manual for module*/
	public void showManual() {
		showWebPage(getBrowserManualPath(), false); 
	}
	/*.................................................................................................................*/
	//TODO: have showWebPage(MesquiteFile file, String path) and showWebPage(MesquiteProject project, String path) to say relative to what (otherwise relative to Mesquite home folder)
	/** requests browser to show page.  boolean indicates whether the standard auto-generated documentation pages for all the modules should be composed if they haven't been. *
 	public static void TEMPshowWebPage(String path, boolean autoCompose) {
		if (path !=null) {
			if (MesquiteTrunk.isApplet()) {
	 			//TODO: FILL THIS IN
			}
			else {
				if (System.getProperty("os.name").startsWith("Mac OS X") && path.indexOf(":/")<0) {
					osXshowLocalWebPage(path, autoCompose);
					return;
				}
				if (path.indexOf(":/")<0) {//local file
					File testing = new File(path);
					if (!testing.exists()) {
						MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the file could not be found." );
						return;
					}
					path = MesquiteFile.massageFilePathToURL(path);
				}
				try {
					BrowserLauncher.openURL(path);
				}
				catch (IOException e) {
					browserString = null;
					MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the web browser could not be used properly.  There may be a problem with insufficient memory or the location of the web page or browser." );
				}
			}
		}
 	}

 	/**
	 * Write the classname to a text file in Mesquite_Folder so that the eclipse plugin
	 * can read that and open the file.
	 *
	 */
	private void openSourceInEclipse() {
		String path = getRootPath() + MesquiteFile.fileSeparator + "mesquiteclassname.txt";
		File mesquiteClassnameFile = new File(path);
		FileOutputStream fos = null;
		try {
			if (!mesquiteClassnameFile.exists()) {
				mesquiteClassnameFile.createNewFile();
			}
			fos = new FileOutputStream(mesquiteClassnameFile);
			String fullClassName = getClass().getName();
			fos.write(fullClassName.getBytes());
		} catch (Exception e) {
			MesquiteTrunk.mesquiteTrunk.alert("Unable to open the source in Eclipse.");
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void showHTMLSnippet(String s){
		mesquiteTrunk.showHTMLSnippet(s);
	}
	/*.................................................................................................................*/ 	
	public static void showWebPage(String path, boolean autoCompose) {
		showWebPage(path,autoCompose,true);
	}
	/*.................................................................................................................*/
	public static void showWebPage(String path, boolean autoCompose, boolean removePastNumberSign) {
		if (path !=null) {
			if (MesquiteTrunk.isApplet()) {
				//TODO: FILL THIS IN
			}
			else {
				String pathToCheck = path;
				if (path.indexOf("#")>0 && removePastNumberSign)
					pathToCheck = StringUtil.getAllButLastItem(path, "#");
				path = pathToCheck;  //Todo: this is temporary, as the launching methods don't seem to handle within-page anchors
				String[] browserCommand = null;
				boolean remote = path.indexOf(":/")>=0;

				boolean useDesktop = false;
				if (MesquiteTrunk.getJavaVersionAsDouble()>= 1.6){  // let's check to see if this will work first
					try {
						if (Desktop.isDesktopSupported()) {
							Desktop desktop = Desktop.getDesktop();
							if (desktop.isSupported(Desktop.Action.BROWSE)) {
								useDesktop = true;
							}
						}
					} catch (Exception e) {
					}
				}

				if (useDesktop){  
					Desktop d = Desktop.getDesktop();
					try {
						URI uri = null;
						if (path.indexOf("http:/")<0 && path.indexOf("https:/")<0){ // it's a local reference
							File file = new File(path);
							uri=file.toURI();
						} else
							uri = new URI(path);
						if (!remote && !CommandChecker.documentationComposed && autoCompose) {
							CommandChecker checker = new CommandChecker();
							checker.composeDocumentation();
						}	
						d.browse(uri);
					}
					catch (IOException e) {
						browserString = null;
						MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the web browser could not be used properly.  There may be a problem with insufficient memory or the location of the web page or browser." );
					} catch (URISyntaxException e) {
						MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the address was not interpretable." );
					}
				}
				else if (MesquiteTrunk.isMacOSX()){ //Mac OS X
					if (remote) { //remote OSX file, use browser laucher
						try {
							BrowserLauncher.openURL(path);
						}
						catch (IOException e) {
							browserString = null;
							MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the web browser could not be used properly.  There may be a problem with insufficient memory or the location of the web page or browser." );
						}
						return;
					}
					else { 
						if (!remote && !CommandChecker.documentationComposed && autoCompose) {
							CommandChecker checker = new CommandChecker();
							checker.composeDocumentation();
						}	
						File testing = new File(pathToCheck);
						if (!testing.exists()) {
							MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the file could not be found. (" + pathToCheck + ")" );
							return;
						}
						if (!CommandChecker.documentationComposed && autoCompose) {
							CommandChecker checker = new CommandChecker();
							checker.composeDocumentation();
						}	
						browserString = "open";
						String brs = "Safari.app";
						File br = new File("/Applications/Safari.app");
						if (!br.exists())
							brs = "Firefox.app";
						if (!br.exists())
							brs = "Internet Explorer.app";

						String[] b = {browserString, "-a", brs, path}; 

						browserCommand = b;
						try {
							//String[] browserCommand = {browserString, arg1, arg2, arg3};
							//if (MesquiteTrunk.isMacOSXLeopard())  //bug in 10.5 occasionally prevented Safari from starting
							//	Runtime.getRuntime().exec(new String[]{browserString, "-a", brs});
							Runtime.getRuntime().exec(browserCommand); 
						}
						catch (IOException e) {
							browserString = null;
							MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the web browser could not be used properly.  There may be a problem with insufficient memory or the location of the web page or browser." );
						}
					}
				}
				else {

					try {
						BrowserLauncher.openURL(path);
						return;
					}
					catch (IOException e) {
					}
					if (!remote) {//local file
						File testing = new File(pathToCheck);
						if (!testing.exists()) {
							MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the file could not be found." );
							return;
						}
						path = MesquiteFile.massageFilePathToURL(path);
					}
					browserString = MesquiteFile.checkFilePath(browserString, "Please select a web browser.");
					if (StringUtil.blank(browserString)){
						browserString = MesquiteString.queryString(mesquiteTrunk.containerOfModule(), "Enter browser path", "If you wish, enter the path to the browser (E.g., /hard_disk/programs/myBrowser.exe)", "");
						if (StringUtil.blank(browserString))
							return;
					}
					String[] b = {browserString, path};
					browserCommand = b;
					try {
						//String[] browserCommand = {browserString, arg1, arg2, arg3};

						Runtime.getRuntime().exec(browserCommand); 
					}
					catch (IOException e) {
						browserString = null;
						MesquiteTrunk.mesquiteTrunk.alert("The requested page could not be shown, because the web browser could not be used properly.  There may be a problem with insufficient memory or the location of the web page or browser." );
					}
				}
			}
		}
	}	
	/*.................................................................................................................*/
	/** Asks for hierarchy browser panel (File coordinator or trunk should override to return.*/
	public HPanel getBrowserPanel(){
		if (employer!=null)
			return getEmployer().getBrowserPanel();
		else
			return null;
	}
	/*.................................................................................................................*/
	/** In some way shows the module, either by showing its window or its place in hierarchy*/
	public void showMe(){
		if (getModuleWindow()!=null) {
			getModuleWindow().toFront();
		}
		else {
			HPanel hp;
			EmployeeTree etM = (EmployeeTree)findImmediateEmployeeWithDuty(EmployeeTree.class);
			if (etM == null)
				etM = (EmployeeTree)hireEmployee( EmployeeTree.class, null);
			if (etM!=null) {
				hp = etM.showEmployeeTreeWindow(MesquiteTrunk.mesquiteTrunk);
				if (hp !=null)
					hp.highlightNode(this);
			}
		}
	}
	/*__________________________________________________________*/
	/*_____________________ NEEDED ON MESQUITE STARTUP____________________________*/
	/*.................................................................................................................*/
	/** Returns the name of the module in very short form.  For use for column headings and other constrained places.  Unless overridden returns getName()*/
	public String getVeryShortName(){
		return getName();
	}
	/*.................................................................................................................*/
	/** Returns the name of the module for menu items.  Unless overridden returns getName()*/
	public String getNameForMenuItem(){
		return getName();
	}
	/*.................................................................................................................*/
	/** Returns the name of the module*/
	public abstract String getName();
	/*.................................................................................................................*/
	/** Returns the authors of the module.  */
	public String getAuthors()  {
		MesquiteModuleInfo mmi = getPackageIntroModule();
		if (mmi!=null && mmi.getPackageAuthors()!=null)
			return mmi.getPackageAuthors();
		//if part of standard
		if (inStandardPackages()) {
			return MesquiteTrunk.mesquiteTrunk.getAuthors();
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the version number string of the module. */
	public String  getVersion(){ 
		if (this instanceof PackageIntro){
			if (!StringUtil.blank(((PackageIntro)this).getPackageVersion()))
				return ((PackageIntro)this).getPackageVersion();
		}
		MesquiteModuleInfo mmi = getPackageIntroModule();
		if (mmi!=null && !StringUtil.blank(mmi.getPackageVersion()))
			return mmi.getPackageVersion();

		//if part of standard
		if (inStandardPackages()) {
			return MesquiteTrunk.mesquiteTrunk.getVersion();
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns the version number string of the module. */
	public int  getBuildNumberOfPackage(){ 
		if (this instanceof PackageIntro){
			if (!StringUtil.blank(((PackageIntro)this).getPackageVersion()))
				return ((PackageIntro)this).getPackageBuildNumber();
		}
		MesquiteModuleInfo mmi = getPackageIntroModule();
		if (mmi!=null && (mmi.getPackageBuildNumber()!=0))
			return mmi.getPackageBuildNumber();

		return 0;
	}
	/*.................................................................................................................*/
	/** returns the version number string of the module as an integer. */
	public int  getVersionInt(){ 
		MesquiteModuleInfo mmi = getPackageIntroModule();
		if (mmi!=null && mmi.getPackageVersionInt()!=0)
			return mmi.getPackageVersionInt();
		//if part of standard
		if (inStandardPackages()) {
			return MesquiteTrunk.mesquiteTrunk.getVersionInt();
		}
		return 0;
	}
	/*.................................................................................................................*/
	/** returns the year released, as a string.  //TODO: SHOULD BE ABSTRACT TO FORCE INCLUSION*/
	public String  getDateReleased(){ 
		MesquiteModuleInfo mmi = getPackageIntroModule();
		if (mmi!=null && mmi.getPackageDateReleased()!=null)
			return mmi.getPackageDateReleased();
		//if part of standard
		if (inStandardPackages()) {
			return MesquiteTrunk.mesquiteTrunk.getDateReleased();
		}
		return null;
	}

	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		return null;
	}

	// store info about other packages
	/*.................................................................................................................*/
	/** Returns whether or not the module does substantive calculations and thus should be cited.  If true, its citation will
 	appear in the citations panel of the windows */
	public boolean showCitation()  {
		return false;
	}
	/*.................................................................................................................*/
	/** Returns the citation of the module. By default this returns the citation of the package in which the module resides, and returns if
 	only if "showCitation" returns true.  The method may be overridden for module-specific citations.*/
	public String getCitation()  {
		if (!showCitation())
			return null;
		MesquiteModuleInfo mmi = getPackageIntroModule();
		if (mmi!=null && mmi.getPackageCitation()!=null)
			return mmi.getPackageCitation();
		//if part of standard
		if (inStandardPackages()) {
			return MesquiteTrunk.mesquiteTrunk.getCitation();
		}
		return null;
	}
	private boolean inStandardPackages(){
		//String packageName = StringUtil.getAllButLastItem(StringUtil.getAllButLastItem(getClass().getName(), "."), ".");  //getting package string
		for (int i=0; i<MesquiteTrunk.standardPackages.length; i++)
			if (getClass().getName().startsWith("mesquite." + MesquiteTrunk.standardPackages[i]))
				return true;
		for (int i=0; i<MesquiteTrunk.standardExtras.length; i++)
			if (getClass().getName().startsWith("mesquite." + MesquiteTrunk.standardExtras[i]))
				return true;
		return false;

	}
	/*.................................................................................................................*/
	/** Returns the name of the module introducing package of modules (e.g., "#aParsimonyIntro")*/
	public MesquiteModuleInfo getPackageIntroModule(){
		String packageName = StringUtil.getAllButLastItem(StringUtil.getAllButLastItem(getClass().getName(), "."), ".");  //getting package string
		return MesquiteTrunk.mesquiteModulesInfoVector.findModule(packageName, PackageIntroInterface.class);
	}
	/*.................................................................................................................*/
	/** returns whether this module is a prerelease version.  This returns "TRUE" here, forcing modules to override to claim they are not prerelease */
	public boolean isPrerelease(){
		return true;  //LEAVE AS TRUE HERE, forcing overriding
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package. 
	 * Use NEXTRELEASE if bound for the next release version of Mesquite.*/
	public int getVersionOfFirstRelease(){
		return 0;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean getHideable() {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module does substantive calculations affecting analysis results, 
		or only a graphical/UI/input-output module */
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this or employee modules are substantive and prerelease */
	public boolean anySubstantivePrereleases(){
		if (isSubstantive() && isPrerelease())
			return true;
		if (employees ==null)
			return false;
		Enumeration enumeration=employees.elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule mb = (MesquiteModule)enumeration.nextElement();
			if (mb.anySubstantivePrereleases())
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether the module is compatible with the current OS, Mesquite system, Java VM, and so on.  If false then the module will not be loaded as a possibility at startup*/
	public boolean  compatibleWithSystem(){ 
		return true;
	}
	/*.................................................................................................................*/
	/** Returns whether the module should be loaded on startup.  This can be overridden to exclude modules from consideration. */  
	public boolean  loadModule(){ 
		return true;
	}
	/*.................................................................................................................*/
	public String getAppletInfo() {
		return getName() + ", version " + getVersion();
	}

	/*.................................................................................................................*/
	public String toString() {
		return getName() + " (id# " + getID() +")";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.  //TODO: SHOULD BE ABSTRACT TO FORCE INCLUSION*/
	public  String getHTMLExplanation()  {
		if (!StringUtil.blank(getExplanation()))
			return getExplanation();
		return "";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.  //TODO: SHOULD BE ABSTRACT TO FORCE INCLUSION*/
	public  String getExplanation()  {
		return "";
	}
	/*.................................................................................................................*/
	/** returns an icon to accompany the explanation of what the module does.  */
	public  String getFunctionIconPath()  {
		return "";
	}
	public void getSubfunctions(){
	}
	public Vector getSubfunctionsVector(){
		return subfunctions;
	}
	public FunctionExplanation registerSubfunction(FunctionExplanation e){
		subfunctions.addElement(e);
		return e;
	}
	/*.................................................................................................................*/
	/** returns keywords related to what the module does, for help-related searches. */ 
	public  String getKeywords()  {
		return "";
	}

	/*.................................................................................................................*/
	/** for FileDirtier interface */
	public void fileDirtiedByCommand(MesquiteCommand command){
		if (getProject()!=null && getProject().getHomeFile()!=null)
			getProject().getHomeFile().setDirtiedByCommand(true);
	}

	/*.................................................................................................................*/
	/** Returns the name of the duty; set by the duty-defining library classes.*/
	public String getDateAndTime() {
		long time = System.currentTimeMillis();
		Date dnow = new Date(time);
		return dnow.toString();
	}

	/*.................................................................................................................*/
	/** Returns the name of the duty; set by the duty-defining library classes.*/
	public String getDutyName() {
		return "";
	}
	/** Returns a String containing the URL for this module */
	public String getURLString(){
		return "";
	}
	/** Returns whether or not the URL for this module is a relative reference from the PackageIntro directory */
	public boolean URLinPackageIntro(){
		return false;
	}
	/*.................................................................................................................*/
	/** Returns name of default module for superclass.*/
	public String[] getDefaultModule (){return null;}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){return null;}
	/*.................................................................................................................*/
	/** Returns NexusBlockTest so interpreter knows if this module can interpret a block. */
	public  NexusBlockTest getNexusBlockTest(){return null;}
	/*.................................................................................................................*/
	/** Returns NexusCommandTest so interpreter knows if this module can interpret a command. */
	public  NexusCommandTest getNexusCommandTest(){return null;}
	/*.................................................................................................................*/
	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/
	public boolean getUserChooseable(){
		return true;
	}

	public void processSingleXMLPreference (String tag, String flavor, String content){
	}

	/*.................................................................................................................*/
	/** Returns duty Class that module would like to hire immediately upon hiring. If a module wants to present a user with a choice of 
	employees to hire, it can request a submenu listing these possible employees.  However, if one of these employees would like to immediately
	(on startup) hire a user-specified subemployee (without having to go through a dialog box, etc.), this employee can indicate this by
	passing back through getHireSubchoice the duty class of the subemployee it would like to hire.  Mesquite then lists a subsubmenu
	indicating possible subemployees.  That way, the user not only chooses the employee, but specifies the subemployee.  When
	the employee is started via startJob, the tokenized string of the name of the
	module that the user requested as subemployee is passed as an argument.*/
	public  Class getHireSubchoice(){return null;}
	/*.................................................................................................................*/
	/** Returns duty Classes that module should NOT hire immediately upon hiring. */
	public  Class[] getDontHireSubchoice(){return null;}
	/*.................................................................................................................*/
	/** Called during Mesquite startup when the list of available modules is being constructed.*/
	public void mesquiteStartup(){
	}
}




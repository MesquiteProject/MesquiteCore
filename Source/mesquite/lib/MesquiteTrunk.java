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

import mesquite.lib.duties.*;
import mesquite.lib.simplicity.InterfaceManager;
import mesquite.trunk.ProjectTreeWindow;

import java.util.*;



/* ����������������������������������������������������������������� */
/* ======================================================================== */
/**  The superclass of the Mesquite class, which resides within the trunk MesquiteModule, has the "main" for the program
Mesquite, and instantiates itself as mesquiteTrunk.  MesquiteTrunk is placed in the class library so that all MesquiteModules (modules) can communicate 
with the trunk. */

public abstract class MesquiteTrunk extends MesquiteModule  
{
	public static boolean startedAsLibrary = false;
	//turns on checking of classes in FileElement, NexusBlock and MesquiteCommand (possibly others), to detect memory leaks
	public static final boolean checkMemory = false;  
	public static boolean attemptingToQuit = false;
	public static LeakFinder leakFinderObject;
	public static String tempDirectory ="";
	public static String[] startupArguments = null;  //ask if flag "-myFlag" is in startupArguments by StringArray.indexOf(MesquiteTrunk.startupArguments, "-myFlag")>=0
	public static boolean errorReportedDuringRun = false;
	public static boolean errorReportedToHome = false;
	public static boolean suppressMenuResponse = false;
	static {
		leakFinderObject = new LeakFinder();
	}
	/** Vector of information about all the modules (MesquiteModules).*/
	public static ModulesInfoVector mesquiteModulesInfoVector;  // the vector of information on all available modules
	/** Vector of all windows.*/
	public static ListableVector windowVector;
	/** Vector of all dialogs.*/
	public static ListableVector dialogVector;

	/** Vector of information records of installed (not necessarily loaded) packages.*/
	public ListableVector packages;

	/** List of standard packages.*/
	public static String[] standardPackages = new String[]{
		"minimal",
		"charMatrices",
		"basic", 
		"trees",
		"categ", 
		"cont", 
		"ancstates",
		"charts",
		"lists",
		"io", 
		"parsimony", 
		"stochchar", 
		"genesis"
	};
	/** List of standard packages.*/
	public static String[] standardExtras = new String[]{
		"batchArch",
		"assoc", 
		"correl",
		"coalesce", 
		"diverse",
		"rhetenor", 
		"align",
		"consensus",
		"treefarm",
		"molec",
		"search", 
		"distance",
		"ornamental",
		"pairwise",
		"dmanager",
		"mb",
		"meristic",
		"tol"
	};
	public HelpSearchManager helpSearchManager;
	public InterfaceManager interfaceManager;
	//hackathon
	public static boolean suppressSystemOutPrintln = false;
	/** Default directory used in Open file dialogs and for consoles' current directory.  Adjusted by minimal.BasicFileCoordinator */
	public static String suggestedDirectory = null;
	/** Time of startup.*/
	public static long startupTime;

	/** Name of user logged in */
	public static String currentAuthor = null;

	/** Menu specifications of special menus owned by the trunk of Mesquite.*/
	public static MesquiteMenuSpec fileMenu, editMenu, charactersMenu, treesMenu, analysisMenu, windowsMenu, helpMenu, utilitiesMenu;  
	public static MesquiteSubmenuSpec defaultsSubmenu, setupSubmenu;
	/** Commands belonging to special menu items owned by the trunk of Mesquite.  */
	public MesquiteCommand newFileCommand, openFileCommand,  openURLCommand, showLicenseCommand, resetMenusCommand,currentCommandCommand, pendingCommandsCommand,  forceQuitCommand, quitCommand, showAllCommand, closeAllCommand, saveAllCommand;
	public MesquiteSubmenuSpec openExternalSMS;
	/** True if MesquiteModule hiring and firing should be logged.*/
	public static boolean trackActivity = false;
	/** The name of the current module set loaded */
	protected String configuration = null;
	protected static Projects projects=null;  
	private BrowseHierarchy drawTask=null;
	protected static boolean defaultHideMesquiteWindow = false;
	public static boolean substantivePrereleasesExist = false;
	public static int numPrevLogs = 5;

	public static int  maxNumMatrixUndoTaxa = 1000;
	public static int  maxNumMatrixUndoChars = 15000;

	public static String logFileName = "Mesquite_Log";
	public static String recentFilesFileName = "RecentFiles.xml";
	protected ProjectTreeWindow projectsWindow;
	public int lastNotice = 0;  //here for phone home system
	public int lastNoticeForMyVersion = 0;//here for phone home system
	public int lastVersionNoticed = 0;//here for phone home system

	public StringBuffer jarFilesLoaded = new StringBuffer();


	public static ConsoleThread consoleThread = null;


	public static boolean consoleListenSuppressed = false;
	public static boolean suppressVersionReporting = false;
	public static boolean suppressErrorReporting = false;
	public static boolean noBeans = false;
	public static boolean startedFromExecutable = false;
	public static boolean debugMode = false;
	public static boolean reportUnregisteredNeeds = false;
	public static boolean mesquiteExiting = false;
	public static int snapshotMode = Snapshot.SNAPALL;




	//true if Mesquite should check website for notices
	public static boolean phoneHome = true;
	public static boolean reportErrors = true;
	public static boolean reportUse = true;

	/** The panel in which all Mesquite "windows" fit if run as applet embedded in page.*/
	public Panel embeddedPanel;

	protected static boolean isApplication = false;

	protected static Image logo;
	public void init(){
	}
	public abstract void exit(boolean emergency, int status);
	public void dispose(){
		mesquiteModulesInfoVector.dispose();
		windowVector.dispose();
		dialogVector.dispose();
		packages.dispose();
		super.dispose();
	}
	public abstract double getMesquiteVersionNumber();
	/*.................................................................................................................*/
	public abstract void reportMemory();
	public abstract void searchKeyword(String s, boolean useBrowser);
	public abstract void searchData(String s, MesquiteWindow window);
	/*.................................................................................................................*/
	/** Placed here so that MesquiteModules are able to request that the trunk module of Mesquite
	open a new file */
	public abstract MesquiteProject openFile(String pathname);
	/*.................................................................................................................*/
	/** Placed here so that MesquiteModules are able to request that the trunk module of Mesquite
	open a new file */
	public abstract MesquiteProject newProject(String pathname, int code, boolean actAsScriptingRegardless);
	
	
	/*.................................................................................................................*/
	public abstract MesquiteProject openOrImportFileHandler(String path, String completeArguments, Class importerSubclass);
	/*.................................................................................................................*/
	/** make a blank projects */
	public static MesquiteProject makeBlankProject(){
		FileCoordinator coord = (FileCoordinator)MesquiteTrunk.mesquiteTrunk.hireEmployee(FileCoordinator.class, null);
		MesquiteFile file = coord.createBlankProject();
		return file.getProject();
	}
	/*.................................................................................................................*/
	/** add a project to Mesquite's list of currently active projects */
	public void addProject(MesquiteProject proj){
		if (projects == null)
			projects = new Projects();
		projects.addProject(proj);
	}
	/*.................................................................................................................*/
	/** remove a project to Mesquite's list of currently active projects */
	public void removeProject(MesquiteProject proj){
		if (projects != null)
			projects.removeProject(proj);
	}
	/*.................................................................................................................*/
	/** get list of currently active projects */
	public static Projects getProjectList(){
		return projects;
	}
	/*.................................................................................................................*/
	public static Thread startupShutdownThread = null;
	public boolean isStartupShutdownThread(Thread t) {
		return t == startupShutdownThread;
	}
	/*.................................................................................................................*/
	public Class getDutyClass() {
		return MesquiteTrunk.class;
	}
	/*.................................................................................................................*/
	public String getDutyName() {
		return "Central Mesquite module";
	}
	public void repaintSearchStrip(){
		if (projectsWindow != null)
			projectsWindow.repaintSearchStrip();
		if (logWindow != null)
			logWindow.repaintSearchStrip();
	}
	/*.................................................................................................................*/
	public static boolean isMacOS(){
		return !StringUtil.blank(System.getProperty("mrj.version"));
	}
	public static boolean isMacOSX(){
		return System.getProperty("os.name").startsWith("Mac OS X");
	}
	/*.................................................................................................................*/
	/** Returns the first three characters of "java.version" as a string; e.g., "1.4.1" is returned as the string 1.4 */
	public static String getJavaVersionAsString(){
		return System.getProperty("java.version").substring(0,3);
	}
	/*.................................................................................................................*/
	/** Returns the full  "java.version" as a string; e.g., "1.4.1" is returned as the string 1.4.1 */
	public static String getJavaVersionAsStringFull(){
		return System.getProperty("java.version");
	}
	/*.................................................................................................................*/
	/** Returns the first three characters of "java.version" as a double; e.g., "1.4.1" is returned as the double 1.4 */
	public static double getJavaVersionAsDouble(){
		try {
			Double versionDouble = Double.valueOf(System.getProperty("java.version").substring(0,3));
			return versionDouble.doubleValue();
		}
		catch (NumberFormatException e) {
			return 0.0;
		}
	}
	/*.................................................................................................................*/
	public static boolean isJavaVersionLessThan(double queryVersion){
		return getJavaVersionAsDouble() < queryVersion;
	}
	
	/*.................................................................................................................*/
	public static int getOSXVersion() {
	    String osVersion = System.getProperty("os.version");
	    String[] fragments = osVersion.split("\\.");

	    // sanity check the "10." part of the version
	    if (!fragments[0].equals("10")) return -1;
	    if (fragments.length < 2) return -1;

	    try {
	        int minorVers = Integer.parseInt(fragments[1]);
	        return minorVers;
	    } catch (NumberFormatException e) {
	    }
	    return -1;
	}
	/*.................................................................................................................*/
	public static boolean isMacOSXPanther(){
		String mrj = System.getProperty("mrj.version");
		if (mrj == null)
			return false;
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()==3);
	}
	/*.................................................................................................................*/
	public static boolean isMacOSXPanther33(){
		String mrj = System.getProperty("mrj.version");
		if (mrj == null)
			return false;
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()==3) && mrj.startsWith("3.3");
	}
	/*.................................................................................................................*/
	public static boolean isMacOSXAfterJaguarRunning33(){
		String mrj = System.getProperty("mrj.version");
		if (mrj == null)
			return false;
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()>=3) && mrj.startsWith("3.3");
	} 	
	/*.................................................................................................................*/
	public static boolean isMacOSXBeforePanther(){
		String mrj = System.getProperty("mrj.version");
		if (mrj == null)
			return false;
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()<3);
	} 	
	/*.................................................................................................................*/
	public static boolean isMacOSXBeforeSnowLeopard(){
		String mrj = System.getProperty("mrj.version");
		if (mrj == null)
			return false;
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()<3);
	} 	
	/*.................................................................................................................*/
	public static boolean isMacOSXLeopard(){
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()==5);
	} 	
	/*.................................................................................................................*/
	public static boolean isMacOSXYosemite(){
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()==10);
	} 	
	/*.................................................................................................................*/
	public static boolean isMacOSXYosemiteOrLater(){
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()>=10);
//		return System.getProperty("os.name").startsWith("Mac OS X") && (System.getProperty("os.version").indexOf("10.10")>=0 || System.getProperty("os.version").indexOf("10.11")>=0 || System.getProperty("os.version").indexOf("10.12")>=0|| System.getProperty("os.version").indexOf("10.13")>=0);
	} 	
	/*.................................................................................................................*/
	public static boolean isMacOSXJaguar(){
		String mrj = System.getProperty("mrj.version");
		if (mrj == null)
			return false;
		return System.getProperty("os.name").startsWith("Mac OS X") && (getOSXVersion()==2) && mrj.startsWith("3.3");
	}
	public static boolean isLinux(){
		return System.getProperty("os.name").indexOf("Linux")>=0 || System.getProperty("os.name").indexOf("linux")>=0 || System.getProperty("os.name").indexOf("LINUX")>=0;
	}
	public static boolean isWindows(){
		return System.getProperty("os.name").indexOf("Windows")>=0 || System.getProperty("os.name").indexOf("windows")>=0 || System.getProperty("os.name").indexOf("WINDOWS")>=0;
	}
	/*.................................................................................................................*/
	public static String getUniqueIDBase(){
		return MesquiteTrunk.author.getCode() +  Long.toHexString(System.currentTimeMillis());
	}
	/*.................................................................................................................*/
	public String getConfiguration(){
		if (configuration == null)
			return "All installed modules";
		else
			return configuration;
	}
	/*.................................................................................................................*/
	public void setConfiguration(String s){
		configuration = s;
	}
	/*.................................................................................................................*/
	public abstract void substantivePrereleasesFound();
	/*.................................................................................................................*/
	/** Returns  whether Mesquite is running as applet or application*/
	public static boolean isApplet(){
		return !isApplication;
	}
	public static void requestLogFocus(){
		if (logWindow != null)
			logWindow.requestFocus();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(this.getClass(), null, null, commandName, "showAbout")) {
			if (getModuleWindow()!=null)
				getModuleWindow().setVisible(true);
		}
		else if (checker.compare(this.getClass(), null, null, commandName, "hideAbout")) {
			if (getModuleWindow()!=null)
				getModuleWindow().hide();
		}
		else if (checker.compare(this.getClass(), null, null, commandName, "projects")) {
			if (projects != null) {
				int n = projects.getNumProjects();
				if (n == 0)
					logln("There are no projects open");
				else for (int i = 0; i<n; i++)
					logln(projects.getProject(i).getName());
			}
		}
		else if (checker.compare(this.getClass(), null, null, commandName, "explainNoSave")) {
			if (MesquiteTrunk.isApplet())
				alert("You cannot save files because Mesquite is being run as a web applet.");
			else if (getProjectList().getNumProjects()==0)
				alert("You cannot save a file because none is open.");
			else if (getProjectList().getNumProjects()>1)
				alert("This menu item belongs to a window not associated with any particular open file or project.  To save a file, go to a window associated with it and select Save from its File menu.");
		}
		else if (checker.compare(this.getClass(), null, null, commandName, "gp")) {
			if (projects != null) {
				int i = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
				if (MesquiteInteger.isCombinable(i))
					return projects.getProject(i);
			}
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public HPanel getBrowserPanel(){
		if (drawTask==null)
			drawTask= (BrowseHierarchy)hireEmployee(BrowseHierarchy.class, null);
		if (drawTask==null)
			return null;
		return drawTask.makeHierarchyPanel();
	}
	/*.................................................................................................................*/
	/** Updates the  menu items to ensure that they are enabled or disabled as appropriate.  */
	public final static void resetMenuItemEnabling(){
		try {
			Enumeration e = mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteWindow mw = (MesquiteWindow)obj;
				MenuBar mbar = mw.getMenuBar();
				if (mbar!=null) {
					int numMenus = mbar.getMenuCount();
					for (int imenu = 0; imenu<numMenus; imenu++) {
						Menu menu = mbar.getMenu(imenu);
						resetEnabling(menu);
					}
				}
				Vector v = mw.getOwnerModule().embeddedMenusVector;
				if (v != null){
					for (int i = 0; i< v.size(); i++){
						Menu menu = (Menu)v.elementAt(i);

						resetEnabling(menu);
					}
				}
			}
		}
		catch (Exception e){
		}
	}

	/*.................................................................................................................*/
	private static void resetEnabling(Menu menu) {
		int numItems = menu.getItemCount();
		for (int i = 0; i<numItems; i++) {
			MenuItem mi = menu.getItem(i);
			if (mi instanceof MesquiteSubmenu) {
				((MesquiteSubmenu)mi).resetEnable();
				resetEnabling((Menu)mi);
			}
			else if (mi instanceof Menu)
				resetEnabling((Menu)mi);
			else if (mi instanceof MesquiteMenuItem) {
				((MesquiteMenuItem)mi).resetEnable();
			}
			else if (mi instanceof MesquiteCheckMenuItem) {
				((MesquiteCheckMenuItem)mi).resetEnable();
			}
		}
	}
	static boolean resetCheckSuppressed = false;
	public final static void suppressResetCheckMenuItems(){
		resetCheckSuppressed =true;
	}
	public final static void resumeResetCheckMenuItems(){
		resetCheckSuppressed = false;
		resetCheckMenuItems();
	}
	static boolean resetNeededForCheckMenuItems = false;
	/*.................................................................................................................*/
	/** Updates the checked menu items to ensure that they are checked or not as appropriate.  */
	public final static void checkForResetCheckMenuItems(){
		if (!resetNeededForCheckMenuItems)
			return;
		if (resetCheckSuppressed)
			return;
		try {
			Enumeration e = mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteWindow mw = (MesquiteWindow)obj;
				MenuBar mbar = mw.getMenuBar();
				if (mbar!=null) {
					int numMenus = mbar.getMenuCount();
					for (int imenu = 0; imenu<numMenus; imenu++) {
						Menu menu = mbar.getMenu(imenu);
						resetChecks(menu);
					}
				}
				if (mw.getOwnerModule() != null){
					Vector v = mw.getOwnerModule().embeddedMenusVector;
					if (v != null){
						for (int i = 0; i< v.size(); i++){
							Menu menu = (Menu)v.elementAt(i);
							resetChecks(menu);
						}
					}
				}


			}
			resetNeededForCheckMenuItems = false;
		}
		catch (Exception e){
		}
	}
	/*.................................................................................................................*/
	/** Updates the checked menu items to ensure that they are checked or not as appropriate.  */
	public final static void resetCheckMenuItems(){//now simply sets a flag that is dealt with on the ClockWatcherThread
		resetNeededForCheckMenuItems = true;
	}

	/*.................................................................................................................*/
	public static void resetChecks(Menu menu) {
		int numItems = menu.getItemCount();
		for (int i = 0; i<numItems; i++) {
			MenuItem mi = menu.getItem(i);
			if (mi instanceof MesquiteSubmenu) {
				((MesquiteSubmenu)mi).resetCheck();
				resetChecks((Menu)mi);
			}
			else if (mi instanceof Menu)
				resetChecks((Menu)mi);
			else if (mi instanceof MesquiteCheckMenuItem) {
				((MesquiteCheckMenuItem)mi).resetCheck();
			}
		}
	}
	/*.................................................................................................................*/
	private static void resetChecks(Menu menu, MesquiteMenuSpec menuSpec) {
		int numItems = menu.getItemCount();
		for (int i = 0; i<numItems; i++) {
			MenuItem mi = menu.getItem(i);
			if (mi instanceof MesquiteSubmenu && menuSpec == ((MesquiteSubmenu)mi).getSpecification()) {
				((MesquiteSubmenu)mi).resetCheck();
				resetChecks((Menu)mi);
			}
			else if (mi instanceof Menu)
				resetChecks((Menu)mi, menuSpec);
			else if (mi instanceof MesquiteCheckMenuItem && menu instanceof MesquiteSubmenu && menuSpec == ((MesquiteSubmenu)menu).getSpecification()) {
				((MesquiteCheckMenuItem)mi).resetCheck();
			}
		}
	}
	/*.................................................................................................................*/
	public static void resetChecks(MesquiteMenuSpec menuSpec) {
		Enumeration e = mesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			MenuBar mbar = mw.getMenuBar();
			if (mbar!=null) {
				int numMenus = mbar.getMenuCount();
				for (int imenu = 0; imenu<numMenus; imenu++) {
					Menu menu = mbar.getMenu(imenu);
					resetChecks(menu, menuSpec);
				}
			}
		}
	}
	/*.................................................................................................................*/
	public static long getMaxAvailableMemory () {
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		long maxMem =  rt.maxMemory();
		long totalMem =  rt.totalMemory();
		long freeMem = rt.freeMemory(); 			
		long maxAvailMem = maxMem - totalMem + freeMem;
		//("Free memory : " + maxMem + " - " + totalMem + " + " + freeMem + " = " + maxAvailMem);
		return maxAvailMem; 

		/* What's being done here? 
		 * 
		 *    maxMem =  the most the VM will possibly take              |--------------------------------------------------------|
		 *    totalMem = the amount of mem currently used by VM   |-------------------------------|
		 *    freeMem = the amount (of total) that is currently free                        |--------------|
		 *    maxAvailMem = the most memory available                                      |--------------.-------------------------|
		 *                                                                                                         {freeMem} + {maxMem-totalMem}
		 */

	}
	/** add a package splash banner to Mesquite's list of currently active packages */
	public abstract void addSplash(MesquiteModuleInfo mmi);
}


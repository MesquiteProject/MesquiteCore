/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite;

import java.awt.Checkbox;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Taskbar;
import java.awt.TextArea;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;

import mesquite.lib.Associable;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.CommandThread;
import mesquite.lib.ConsoleThread;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.Listened;
import mesquite.lib.LogWindow;
import mesquite.lib.MainThread;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMacro;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteModuleInfo;
import mesquite.lib.MesquitePackageRecord;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NexusBlock;
import mesquite.lib.ObjectContainer;
import mesquite.lib.ParseUtil;
import mesquite.lib.PendingCommand;
import mesquite.lib.PhoneHomeUtil;
import mesquite.lib.ProjectRead;
import mesquite.lib.ProjectReadThread;
import mesquite.lib.Projects;
import mesquite.lib.ShellScriptUtil;
import mesquite.lib.SpecsSet;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.BrowseHierarchy;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.GeneralFileMakerMultiple;
import mesquite.lib.duties.GeneralFileMakerSingle;
import mesquite.lib.duties.MesquiteInit;
import mesquite.lib.duties.TextDisplayer;
import mesquite.lib.duties.WindowHolder;
import mesquite.lib.misc.ClassVector;
import mesquite.lib.misc.HPanel;
import mesquite.lib.simplicity.InterfaceManager;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ChecklistDialog;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.ExplanationArea;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.HelpSearchManager;
import mesquite.lib.ui.HelpSearchStrip;
import mesquite.lib.ui.InfoBar;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteColorTable;
import mesquite.lib.ui.MesquiteDialogParent;
import mesquite.lib.ui.MesquiteFrame;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquiteMenuBar;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.MesquitePanel;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.trunk.AboutWindow;
import mesquite.trunk.ApplicationHandler9;
import mesquite.trunk.ClockWatcherThread;
import mesquite.trunk.ConfigFileRecord;
import mesquite.trunk.FileOpener;
import mesquite.trunk.JarLoader;
import mesquite.trunk.ModuleLoader;
import mesquite.trunk.PhoneHomeThread;
import mesquite.trunk.ProjectTreeWindow;
import mesquite.trunk.StartupThread;
import mesquite.trunk.WelcomeDialog;

/* The root module of the tree of modules.  This contains the main method which uses MesquiteModuleLoader to compile information
about available modules.  Also hires FileCoordinator modules (which in turn hire others to do analyses).   */

public class Mesquite extends MesquiteTrunk 
{
	/*.................................................................................................................*/
	public String getCitation() {
		return "Maddison, W.P. & D.R. Maddison. 2025. Mesquite: A modular system for evolutionary analysis.  Version " + getVersion() + ".  https://www.mesquiteproject.org";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return "4.0";
	}

	/*.................................................................................................................*/
	public int getVersionInt() {
		return 400;
	}
	/*.................................................................................................................*/
	public double getMesquiteVersionNumber(){
		return 4.00;
	}
	/*.................................................................................................................*/
	public String getDateReleased() {
		return "June 2025"; //"April 2007";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		//See MesquiteModule for version reporter and error reporter URLs
		//See Installer for updates.xml URLs

		/*if (true)
			return "https://raw.githubusercontent.com/wmaddisn/Tuatara/refs/heads/master/docs/notices.xml";   

		else */
			if (!isPrerelease() && !debugMode)
			return "http://www.mesquiteproject.org/noticesAndUpdates/notices.xml";   
		else
			return "https://raw.githubusercontent.com/MesquiteProject/MesquiteCore/development/noticesAndUpdates/noticesPrerelease.xml";   


		/* Version 3.2 through 3.4 
		if (!isPrerelease() && !debugMode)
			return "https://raw.githubusercontent.com/MesquiteProject/MesquiteCore/master/noticesAndUpdates/notices.xml";   
		else
			return "https://raw.githubusercontent.com/MesquiteProject/MesquiteCore/development/noticesAndUpdates/noticesPrerelease.xml";   

		 */

		/* Version 2.75 through 3.11
		if (!isPrerelease() && !debugMode)
			return "http://mesquiteproject.org/mesquite/notice/notices.xml";   
		else
			return "http://mesquiteproject.org/mesquite/prereleasenotices/notices.xml";   
		 */
	}
	/*.................................................................................................................*/
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed ee = registerEmployeeNeed(FileCoordinator.class, "The file coordinator supervises activities with files.",
				"These are activated when new projects/files are made or read. ");
		ee.setAsEntryPoint(null);
		EmployeeNeed e = registerEmployeeNeed(MesquiteInit.class, "INIT modules assist Mesquite.",
				"These are activated automatically. ");
		e.setAsEntryPoint(null);
		EmployeeNeed e2 = registerEmployeeNeed(BrowseHierarchy.class, "Mesquite uses these modules for hierarchical display of employee and project trees.",
				"These are activated automatically. ");
		e2.setAsEntryPoint(null);
		EmployeeNeed e3 = registerEmployeeNeed(WindowHolder.class, "Mesquite uses a WindowHolder to assist displaying windows.",
				"These are activated automatically. ");
		e3.setAsEntryPoint(null);
	}
	ClockWatcherThread cwt;
	public static Image general, calculations, display, left, right;
	public boolean openFilesNowUsed = false;
	public boolean ready=false;
	public int numDirectories = 0;
	private boolean preferencesSet = false;
	protected int lastVersionUsedInt = 0;
	protected String lastVersionRun = "1.04";
	//private String storedManualString = null;
	private boolean showLogWindow = true;
	private boolean showAbout = true;
	private boolean consoleMode = false;
	private Vector splashURLInfo = new Vector(3);
	private ListableVector splashNames = new ListableVector(3);
	public static AboutWindow about;
	private int numUses =0;
	String configFile = "all";
	HPanel browser;
	ListableVector configurations;
	private  FileOpener fileHandler;

	/*.................................................................................................................*/
	public void endJob() {
		deleteTempDirectory();
		if (MesquiteTrunk.startedFromFlex2) {
			String currentPIDPath = System.getProperty("user.home") + MesquiteFile.fileSeparator + "Mesquite_Support_Files" + MesquiteFile.fileSeparator + MesquiteTrunk.encapsulatedPathOfExecutable+ MesquiteFile.fileSeparator + "currentPID.txt";
			MesquiteFile.deleteFile(currentPIDPath);
		}
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public void dispose(){
		/*	if (logWindow != null){
			MesquiteFrame f = logWindow.getParentFrame();
			f.setVisible(false);
		}*/
		splashNames.dispose();
		configurations.dispose();
		super.dispose();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void substantivePrereleasesFound(){
		if (!substantivePrereleasesExist){
			substantivePrereleasesExist = true;
			if (about!=null && about.aboutPanel!=null)
				about.aboutPanel.repaint();
		}
	}


	/*.................................................................................................................*/
	static boolean verboseStartup = false;
	public void init()
	{
		verboseStartup = false;
		long startingTime = System.currentTimeMillis();
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
		if (verboseStartup) System.out.println("main init 1");
		if (isApplet())
			mesquiteTrunk = this;
		author.setCurrent(true);
		incrementMenuResetSuppression();
		boolean makeNewPrefsDirectory = false;
		configurations = new ListableVector(); 
		packages = new ListableVector();

		/* 
(1) Look for Mesquite.pref file in .Mesquite_Prefs folder of user.home.  If exists, read it 
(2) Try to find mesquite manual.  If not found, query user for manual.  Remember that it hadn't been found, and later store its place
		 */

		if (verboseStartup) System.out.println("main init 2");

		// [Search for MQLINUX]
		/*if (isLinux()) {
			linuxGWAThread = new LinuxGWAThread();
			linuxGWAThread.start();
		}
*/

		String sep = MesquiteFile.fileSeparator;

		//finding mesquite directory
		ClassLoader cl = mesquite.Mesquite.class.getClassLoader();
		URL mesquiteDirectoryURL = cl.getResource("mesquite/Mesquite.class");
		String loc = "";
		try {
			URI mesquiteDirectoryURI = mesquiteDirectoryURL.toURI();  // convert to URI so that encoding is taken care of properly 
			loc = mesquiteDirectoryURI.getPath();  // then get the path
		} catch (URISyntaxException e) {
			loc = mesquiteDirectoryURL.getPath();    // have to give it something, and this shouuld work at this point
			if (MesquiteTrunk.debugMode)
				e.printStackTrace();
		}

		String sepp = MesquiteFile.fileSeparator;
		if (loc.indexOf(sepp)<0){  // the current OS's file separate is NOT present, therefore must presume it is the "/" which is what ClassLoader should return anyway
			sepp = "/";
			if (loc.indexOf(sepp)<0)
				System.out.println("Not a recognized separator in path to Mesquite class!");
		}
		loc = loc.substring(0, loc.lastIndexOf(sepp));  //get rid of /Mesquite.class
		loc = loc.substring(0, loc.lastIndexOf(sepp));  // go down a level to Mesquite_Folder

		mesquiteDirectory = new File(loc);

		/*
		try {
			//if (startedFromNestedStarter)  //for OS X executable built by Oracle appBundler
			loc = StringUtil.encodeURIPath(loc);  // not sure why this is needed, but it seems to be
			URI uri = new URI(loc);
			mesquiteDirectory = new File(uri.getSchemeSpecificPart());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		 */

		if (mesquiteDirectory == null){
			StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), ":");
			while (st.hasMoreElements()){
				String token = st.nextToken();
				File mesquiteClass = new File(token + MesquiteFile.fileSeparator + "mesquite" + MesquiteFile.fileSeparator + "Mesquite.class");
				if (mesquiteClass.exists()) {
					mesquiteDirectory = new File(token);
					if (!mesquiteDirectory.exists())
						mesquiteDirectory = null;
				}
			}
		}



		if (mesquiteDirectory ==null){
			File mesquiteClass = new File(System.getProperty("user.dir") + MesquiteFile.fileSeparator + "mesquite" + MesquiteFile.fileSeparator + "Mesquite.class");
			if (mesquiteClass.exists()) {
				mesquiteDirectory = new File(System.getProperty("user.dir"));
				if (!mesquiteDirectory.exists())
					mesquiteDirectory = null;
			}
		}
		if (verboseStartup) System.out.println("main init 3");

		/* EMBEDDED delete these if embedded */
		userDirectory = new File(System.getProperty("user.home")+sep);  //used to be user.dir, but caused permission problems in linux

		String supportFilesPath = System.getProperty("user.home") + sep + "Mesquite_Support_Files";
		supportFilesDirectory = new File(supportFilesPath);
		if (!supportFilesDirectory.exists()){
			String supportFilesPathALT = System.getProperty("user.home") + sep + ".Mesquite_Support_Files";
			File supportFilesDirectoryALT = new File(supportFilesPathALT);
			if (supportFilesDirectoryALT.exists()){
				supportFilesPath = supportFilesPathALT;
				supportFilesDirectory = supportFilesDirectoryALT;
			}
			else
				supportFilesDirectory.mkdir();
		}
		boolean supportFilesWritable = MesquiteFile.canWrite(supportFilesPath);
		boolean writabilityWarned = false;
		if (!supportFilesWritable || !supportFilesDirectory.exists()){
			String oldPath = supportFilesPath;
			supportFilesPath = mesquiteDirectory.getPath() + sep + "Mesquite_Support_Files";
			supportFilesDirectory = new File(supportFilesPath);
			if (!supportFilesDirectory.exists())
				supportFilesDirectory.mkdir();
			if (!supportFilesDirectory.exists()){ 
				writabilityWarned = true;
				if (MesquiteWindow.headless)
					System.out.println("Mesquite does not have permission to write its log file or its preferences files.  It has attempted to create both " + oldPath +
							"\" and \"" + supportFilesPath + "\" but failed.");
				else
					alert("Mesquite does not have permission to write its log file or its preferences files.  It has attempted to create both " + oldPath +
							"\" and \"" + supportFilesPath + "\" but failed.  This will prevent Mesquite from functioning properly.  Please check that your system permits " +
							"Mesquite to write to " + userDirectory);
			}
		}
		if (verboseStartup) System.out.println("main init 4");



		MesquiteModule.prefsDirectory = new File(supportFilesPath + sep + "Mesquite_Prefs"); //checking both possible names
		File prefsFile = new File(prefsDirectory.toString() + sep + "Mesquite.pref");
		File prefsFileXML = new File(prefsDirectory.toString() + sep + "Mesquite.xml");


		if (verboseStartup) System.out.println("main init 5");
		if (!MesquiteModule.prefsDirectory.exists() || !MesquiteModule.prefsDirectory.isDirectory()) {
			makeNewPrefsDirectory = true;
			setMesquiteDirectoryPath();
		}
		else if (prefsFile.exists() || prefsFileXML.exists()) {
			if (MesquiteModule.mesquiteDirectory==null) 
				setMesquiteDirectoryPath();
			else
				setMesquiteDirectoryPath();
		}
		else {
			setMesquiteDirectoryPath();
		}
		if (verboseStartup) System.out.println("main init 6");

		MesquiteModule.mesquiteHomePageURL = getRootPath() + "docs/home.html";
		MesquiteModule.mesquiteDocsURL = getRootPath() + "docs/";

		if (verboseStartup) System.out.println("main init 6a");
		if (starter != null){ // because of Java 9 classloading issues, rely on starter class to make class loader if it exists
			try {
				if (verboseStartup) System.out.println("main init 6b");
				Method gsn = starter.getClass().getDeclaredMethod("getStartupNotices", null);
				startupNotices = (Vector)gsn.invoke(starter, null);
				if (startupNotices != null)
					System.out.println("Received startupNotices from start.Mesquite");
				//the following could thro
				if (verboseStartup) System.out.println("main init 6c");
				Method gmcl = starter.getClass().getDeclaredMethod("getMesquiteClassLoader", null);
				Object obj = gmcl.invoke(starter, null);
				if (verboseStartup) System.out.println("main init 6d");
				if (obj instanceof URLClassLoader)
					basicClassLoader = (URLClassLoader)obj;
				if (basicClassLoader!= null)
					System.out.println("Received URLClassLoader from start.Mesquite");
				if (verboseStartup) System.out.println("main init 6e");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		if (verboseStartup) System.out.println("main init 6f");
		if (basicClassLoader == null){ 
			basicClassLoader = makeModuleClassLoader(MesquiteModule.getRootPath(), null, new Vector());
			System.out.println("No URLClassLoader received from start.Mesquite; made one after startup");
		}

		if (verboseStartup) System.out.println("main init 6g");
		addToStartupNotices("Current class loader " + this.getClass().getClassLoader());
		addToStartupNotices("Module class loader " + basicClassLoader);
		addToStartupNotices("==================================\n");

		if (verboseStartup) System.out.println("main init 6h");
		if (prefsFile.exists() || prefsFileXML.exists()) {
			loadPreferences();
			if (!preferencesSet) {
				setMesquiteDirectoryPath();
			}
		}
		readRecordOfRecentFiles();
		if (verboseStartup) System.out.println("main init 7");


		String logPath = supportFilesPath + sep + MesquiteTrunk.logFileName; 
		File logFile = new File(logPath);

		boolean logFileExistsButCantWrite = (logFile.exists() &&!logFile.canWrite());
		boolean logFileAbsentButCantWriteContainer = (!logFile.exists() && !(new File(supportFilesPath).canWrite()));
		if (verboseStartup) System.out.println("main init 8");
		if ((logFileExistsButCantWrite || logFileAbsentButCantWriteContainer) && !writabilityWarned){
			if (MesquiteWindow.headless)
				System.out.println("Mesquite does not have permission to write its log file.");
			else
				alert("Mesquite does not have permission to write its log file.  This may indicate issues with your system configuration that will prevent Mesquite from functioning properly.  Please check that your system permits " +
						"Mesquite to write to " + userDirectory);
			MesquiteMessage.println("Log can't be written (" + logFileExistsButCantWrite + ", " + logFileAbsentButCantWriteContainer + " at " + logPath + ")");
		}
		if (verboseStartup) System.out.println("main init 9");

		if (logFile.exists()) {
			for (int i = MesquiteTrunk.numPrevLogs-1; i>0; i--) {
				String iPath = supportFilesPath  + sep + MesquiteTrunk.logFileName + "_(Previous#" + i + ")";
				String iPlusOnePath = supportFilesPath + sep + MesquiteTrunk.logFileName + "_(Previous#" + (i+1) + ")";
				MesquiteFile.rename(iPath, iPlusOnePath);
			}
			MesquiteFile.rename(logPath, supportFilesPath + sep + MesquiteTrunk.logFileName + "_(Previous#1)");
		}
		if (verboseStartup) System.out.println("main init 10");


		/**/
		/*
		(3) Try to find the logo for the about window
		 */
		String logInitString = "Mesquite version " + getMesquiteVersion() + getBuildVersion() + " " + MesquiteModule.getBuildDate() + "\n";
		logInitString += "https://www.mesquiteproject.org\n";
		if (StringUtil.notEmpty(MesquiteModule.getSpecialVersion()))
			logInitString  +="  " + MesquiteModule.getSpecialVersion()+ "\n";
		logInitString  += ("\nCopyright (c) 1997-2025 W. Maddison and D. Maddison\n");
		logInitString  += "The basic Mesquite package (class library and basic modules) is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License. "
				+ "  Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.  For details on license and "
				+ "lack of warranty see the GNU Lesser General Public License by selecting \"Display License\" from the Window menu or at www.gnu.org\n"
				+ "\nPrincipal Authors: Wayne Maddison & David Maddison\nDevelopment Team: Wayne Maddison, David Maddison, Daisie Huang, Peter Midford, Rutger Vos, Jeff Oliver"
				+ "\nDevelopment Team Alumnus: Danny Mandel\n* * * * * * * * * * * * * * * *\n";

		if (verboseStartup) System.out.println("main init 11");
		logWindow = new LogWindow(logInitString);
		if (verboseStartup) System.out.println("main init 12");
		logWindow.setAsPrimaryMesquiteWindow(true);
		setModuleWindow(logWindow);
		if (verboseStartup) System.out.println("main init 13");
		if (MesquiteTrunk.mesquiteTrunk.isPrerelease()) {
			mesquiteTrunk.logo = MesquiteImage.getImage(MesquiteModule.getRootPath() + "images" + MesquiteFile.fileSeparator + "mesquiteBeta.gif");
		}
		else
			mesquiteTrunk.logo = MesquiteImage.getImage(MesquiteModule.getRootPath() + "images" + MesquiteFile.fileSeparator + "mesquite.gif");

		BufferedImage equiv = null;
		try {

			equiv = ImageIO.read(new File(MesquiteModule.getRootPath() + "images" + MesquiteFile.fileSeparator + "equivocal.gif"));
			GraphicsUtil.missingDataTexture = new TexturePaint(equiv, new Rectangle(0, 0, 16, 16));
		} catch (IOException e) {
			MesquiteMessage.println(" IOException trying to read equivocal texture ");
		}

		if (verboseStartup) System.out.println("main init 14");
		MediaTracker mt = new MediaTracker(logWindow.getOuterContentsArea());
		mt.addImage(logo, 0);
		try {
			mt.waitForAll();
		} catch (Exception e) {
			MesquiteMessage.warnProgrammer("logo image exception------------");
			e.printStackTrace();
		}
		if (verboseStartup) System.out.println("main init 15");
		if (about !=null)
			about.setImage(logo);
		if (verboseStartup) System.out.println("main init 16");


		/*EMBEDDED include following if embedded *
		setLayout( new BorderLayout() );
		embeddedPanel = new MQPanel();
		add("Center", embeddedPanel);
		embeddedPanel.setBackground(Color.cyan);
		embeddedPanel.setVisible(true);
		embeddedPanel.setLayout(null);
		embeddedPanel.repaint();
		/*EMBEDDED include above if embedded */


		if (verboseStartup) System.out.println("main init 17");
		if (isApplet()) { 
			mesquiteTrunk = this;
			prepareMesquite();
		}
		if (verboseStartup) System.out.println("main init 18");
		if (isJavaVersionLessThan(1.8)){
			discreetAlert("This version of Mesquite requires Java 1.8 or higher.  Please update your version of Java, or use an older version of Mesquite.  Please be aware however that earlier versions of Mesquite may have bugs that affect your results.  Please check the Mesquite website for information.");
			exit(true, 0);
		}

		if (verboseStartup) System.out.println("main init 19");
		logWindow.setConsoleMode(consoleMode);
		if (verboseStartup) System.out.println("main init 19a");
		//logWindow.setWindowSize(380,351); //this and later setWindowSize done because of bug in OS X 1.4.X
		logWindow.setWindowLocation(8, 8, false);
		if (verboseStartup) System.out.println("main init 19b");
		if (showLogWindow)
			logWindow.setVisible(true);
		if (verboseStartup) System.out.println("main init 19c");
		logWindow.setWindowSize(400,450);

		if (verboseStartup) System.out.println("main init 20");
		//		logln(System.getenv().toString());
		logln(" ");
		//logln(notice);
		startupTime = System.currentTimeMillis();
		Date dnow = new Date(startupTime);
		logln(StringUtil.getDateTime(dnow));
		//logln( dnow.toString());
		if (startupTime <1100000000000L) {
			discreetAlert("The clock on your computer appears to be set incorrectly.  This version of Mesquite was compiled after November 2004, but your computer's date appears to be " + dnow.toString());
		}
		String mrj = System.getProperty("mrj.version");
		if (mrj == null)
			mrj = "";
		else
			mrj = "; MRJ version " + mrj;
		if (verboseStartup) System.out.println("main init 21");
		logln("Running under Java " + System.getProperty("java.version") +"; virtual machine by " + System.getProperty("java.vendor") + mrj + " on " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (architecture: " + System.getProperty("os.arch") + ")");
		logln("User: " + System.getProperty("user.name") );
	//	logln(" ");
		/* EMBEDDED add following if embedded *
		logWindow.setVisible(false);
		 /* */
		logln(jarFilesLoaded.toString());


		makeEmployeeVector();
		if (verboseStartup) System.out.println("main init 22");


		Random rNG = new Random();

		MesquiteWindow.centerWindow(about);


		if (!preferencesSet){
			//WelcomeDialog is being phased out
			if (false && !MesquiteWindow.headless && !MesquiteThread.suppressInteractionAsLibrary){  //hackathon
				String s = "Welcome to Mesquite.  If this is the first time you've run Mesquite, you might want to try the example files to learn about how to use it (example files in \"Mesquite_Folder/examples\").  You might also check the menu items in the Help menu for links to documentation.";
				s += "\n\nAlso, please sign up to the Mesquite Discussion e-mail list to learn about bugs and updates (instructions for signing up at http://www.mesquiteproject.org).";
				new WelcomeDialog(containerOfModule(), "Welcome to Mesquite", s);
			}
			if (makeNewPrefsDirectory) {
				MesquiteModule.prefsDirectory.mkdir();
			}
			storePreferences();
		}
		if (verboseStartup) System.out.println("main init 23");
		if (about !=null) {
			if (isApplet())
				about.setVisible(false);
			else {
				if (verboseStartup) System.out.println("main init 23a");
				MesquiteWindow.centerWindow(about);
				if (verboseStartup) System.out.println("main init 23b");
				about.setVisible(true);
				if (verboseStartup) System.out.println("main init 23c");
			}
			about.setBackground(ColorTheme.getInterfaceBackground());
			about.repaint();
		}
		if (verboseStartup) System.out.println("main init 24");

		tempDirectory = createTempDirectory();



		/*----*/

		setSuggestedDirectory(mesquiteDirectoryPath + "examples");
		appsDirectory = mesquiteDirectoryPath + "apps";
		pythonDirectory = mesquiteDirectoryPath + "python";
		if (mesquiteDirectory!=null)
			logln("Mesquite directory: " + mesquiteDirectoryPath);
		else
			logln("Mesquite directory null ");
		if (userDirectory!=null)
			logln("Log file located in: " + supportFilesPath);
		if (prefsDirectory!=null) {
			logln("Prefs directory: " + prefsDirectory.getAbsolutePath());
			if (MesquiteWindow.headless)  //hackathon
				MesquiteFile.putFileContents(prefsDirectory + MesquiteFile.fileSeparator + "mesquiteHeadlessLocation.txt", mesquiteDirectoryPath, true);
			else
				MesquiteFile.putFileContents(prefsDirectory + MesquiteFile.fileSeparator + "mesquiteLocation.txt", mesquiteDirectoryPath, true);
		}
		else
			logln("Prefs directory null ");

		logln(" ");

		//NOTE: debugMode isn't set until modules are loaded

		/* loading in modules (MesquiteModules) */
		MesquiteModule.mesquiteTrunk = this;


		if (verboseStartup) System.out.println("main init 25");
		setupColors();

		if (MesquiteWindow.headless){
			logln("RUNNING HEADLESS MODE");
		}

		if (InterfaceManager.enabled){
			interfaceManager = new InterfaceManager();
			//InterfaceManager.importSettingsFiles();
		}

		ModuleLoader mBL = new ModuleLoader(this);
		boolean minimalStartup = MesquiteFile.fileExists(MesquiteModule.getRootPath() + "minimalStartup");
		if (minimalStartup)
			logln("File called \"minimalStartup\" detected at " + MesquiteModule.getRootPath()+ ".  Mesquite will start with a minimal configuration of modules.");

		//==============  LOADING MODULES   ===============
		if (verboseStartup) System.out.println("main init 26");
		mBL.init(configFile, configurations, minimalStartup);
		if (verboseStartup) System.out.println("main init 27");
		//==============================================
		
		int count = 0;
		if (isPrerelease())
			count++;
		String citationsString = "\nMesquite version " + getMesquiteVersion() + getBuildVersion() ;
		if (StringUtil.notEmpty(MesquiteModule.getSpecialVersion()))
			citationsString  +="  " + MesquiteModule.getSpecialVersion();
		citationsString += ":\n" + getCitation() + "\n";
		int numExtra = 0;
		for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
			if (mmi.isSubstantive() && mmi.isPrerelease()) {
				count++;
			}
			if (mmi.getIsPackageIntro() && !mmi.isBuiltInPackage()){
				numExtra++;
				citationsString+= "\n" + mmi.getPackageName();
				if (!StringUtil.blank(mmi.getPackageVersion()))
					citationsString += " version " + mmi.getPackageVersion();
				if (mmi.getPackageBuildNumber() > 0)
					citationsString += " (build " + mmi.getPackageBuildNumber() + ")";
				if (StringUtil.notEmpty(mmi.getPackageDateReleased()))
					citationsString += ", " + mmi.getPackageDateReleased();
				if (!StringUtil.blank(mmi.getPackageAuthors()))
					citationsString += " (by " + mmi.getPackageAuthors() + ")";
				if (!StringUtil.blank(mmi.getPackageCitation()))
					citationsString += ":\n" + mmi.getPackageCitation() + "";
				citationsString +="\n";
			}
		}
		if (verboseStartup) System.out.println("main init 28");

		try {
	        Taskbar taskbar = Taskbar.getTaskbar();
	        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
	            java.awt.Image icon = Toolkit.getDefaultToolkit().getImage(MesquiteModule.getRootPath() + "images" + MesquiteFile.fileSeparator + "openMesquite.png");
	            taskbar.setIconImage(icon);
	        }
	    } catch (UnsupportedOperationException e) {
	    } catch (SecurityException e) {
	    }


		logln(" ");
		logln(Integer.toString(mesquiteModulesInfoVector.size()) + " modules installed.");

		if (count > 0){
			logln(Integer.toString(count) + " prerelease substantive modules installed.");
		}
		logln(" ");

		String ackn = "Mesquite uses BrowserLauncher by Eric Albert, corejava.Format by Horstmann & Cornell, ByteBuddy (http://bytebuddy.net), and OpenPDF (https://github.com/LibrePDF/OpenPDF)  .";
		ackn += "  Some modules make use of JAMA by The MathWorks and NIST, and JSci by Mark Hale, Jaco van Kooten and others (see Mesquite source code for details).";
		ackn += "  The PAL library by Drummond and Strimmer is used by the GTR substitution model for DNA sequence simulations.";
		if (MesquiteTrunk.isWindows())
			ackn += " The Windows executable is wrapped with Launch4j by Grzegorz Kowal, copyright Grzegorz Kowal 2005-2014, distributed under a BSD license (http://opensource.org/licenses/bsd-license.html) and a MIT license (http://opensource.org/licenses/mit-license.html).";
		logln(ackn);
		logln(" ");
		makeMenu("Mesquite"); //just in case employees have no where else to put

		openSpecialSubmenuSpec = new MesquiteSubmenuSpec(fileMenu, "Open Special", this);
		MesquiteMenuItemSpec mmiO = addItemToSubmenu(fileMenu, openSpecialSubmenuSpec, "One File", null);
		mmiO.setEnabled(false); // this wasn't getting enamed,f or some reason....
		addModuleMenuItemsSeparatelyToSubmenu(fileMenu, openSpecialSubmenuSpec, makeCommand("openGeneral", this), GeneralFileMakerSingle.class);
		addItemToSubmenu(fileMenu, openSpecialSubmenuSpec, "-", null);
		mmiO = addItemToSubmenu(fileMenu, openSpecialSubmenuSpec, "Multiple Files", null);
		mmiO.setEnabled(false); // this wasn't getting enamed,f or some reason....
		addModuleMenuItemsSeparatelyToSubmenu(fileMenu, openSpecialSubmenuSpec, makeCommand("openGeneral", this), GeneralFileMakerMultiple.class);
		addItemToSubmenu(fileMenu, openSpecialSubmenuSpec,"-", null);
		MesquiteCommand eOCC =   new MesquiteCommand("explainOpenChoices", this);
		eOCC.bypassQueue = true; //for some reason putting the HTML dialog on AWTEventThread helps!
		addItemToSubmenu(fileMenu, openSpecialSubmenuSpec,"Explain These Choices...",   eOCC);

		//	openSpecialSubmenuSpec.setCommand(makeCommand("openGeneral", this));
		//	openSpecialSubmenuSpec.setList(GeneralFileMakerSingle.class);

		addMenuItem(helpMenu, "Mesquite Documentation", makeCommand("showHomePage",  this));
		addMenuItem(helpMenu, "Support and Advice", makeCommand("showSupport",  this));
		addMenuItem(helpMenu, "Modules Loaded", makeCommand("showModules",  this));
		addMenuItem(helpMenu, "Keyword Search...", makeCommand("keywordSearch",  this));
		/*New code added Feb.05.07 oliver*/ //TODO: Delete new code comments	
		addMenuItem(helpMenu, "Web Search...", makeCommand("webSearch",  this));
		/*End new code added Feb.05.07 oliver*/
		/*New code added April.02.07 oliver*/ //TODO: Delete new code comments
		addMenuItem(helpMenu, "Mesquite FAQs", makeCommand("mesquiteFAQ", this));
		/*End new code added April.02.07 oliver*/
		addMenuItem(helpMenu, "Scripting Commands", makeCommand("showCommands",  this));
		addMenuItem(helpMenu, "-", null);
		for (int i=0; i<splashNames.size(); i++) {
			MesquiteString ms = (MesquiteString)splashNames.elementAt(i);
			MesquiteCommand mc = makeCommand("showPackageInfo", this);
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)splashURLInfo.elementAt(i);
			mc.setDefaultArguments(ParseUtil.tokenize(mmi.getSplashURL()));
			addMenuItem(helpMenu, ms.getValue(), mc);
		}
		addMenuItem(helpMenu, "-", null);
		addMenuItem(helpMenu, "Active Module Tree", makeCommand("showEmployeeTree",  this));
		addMenuItem(helpMenu, "Show Web Page Listing Installed Modules", makeCommand("showWebPageAllModules",  this));
		addMenuItem(helpMenu, "List active modules", makeCommand("dumpEmployeeTree",  this));
		addMenuItem(helpMenu, "List prerelease modules", makeCommand("dumpSubstantivePrerelease",  this));
		//addMenuItem(helpMenu, "List ALL prerelease modules", makeCommand("dumpAllPrerelease",  this)); //delete this before release
		addMenuItem(helpMenu, "List all modules", makeCommand("dumpAllModules",  this));
		addMenuItem(helpMenu, "List new modules", makeCommand("dumpNewModules",  this));
		if (MesquiteTrunk.mesquiteTrunk.isPrerelease()) 
			addMenuItem(helpMenu, "List modules new for next release", makeCommand("dumpNextReleaseModules",  this));
		addMenuItem(helpMenu, "Show Developer Statistics", makeCommand("showStatistics",  this));

		MesquiteSubmenuSpec mms = addSubmenu(fileMenu, "Activate/Deactivate Packages");
		mms.setFilterable(false);
		addItemToSubmenu(fileMenu, mms, "Use All Installed Packages", makeCommand("setConfigAll",  this));
		addItemToSubmenu(fileMenu, mms, "Choose Configuration...", makeCommand("setConfig",  this));
		addItemToSubmenu(fileMenu, mms, "Delete Configuration...", makeCommand("deleteConfig",  this));
		addItemToSubmenu(fileMenu, mms, "-", null);
		addItemToSubmenu(fileMenu, mms, "Define Configuration...", makeCommand("defineConfig",  this));
		MesquiteSubmenuSpec macroSM = addSubmenu(fileMenu, "Macros");
		macroSM.setFilterable(false);
		addItemToSubmenu(fileMenu, macroSM, "Show Macro List...", makeCommand("showMacros",  this));
		addItemToSubmenu(fileMenu, macroSM, "Edit Macro Information...", makeCommand("editMacroInfo",  this));
		addItemToSubmenu(fileMenu, macroSM, "Delete Auto-saved Macros...", makeCommand("deleteAutoMacros",  this));
		addMenuItem(fileMenu, "Rename Log File...", makeCommand("renameLog",  this));
		addMenuItem(fileMenu, "-", null);

		new MesquiteColorTable(); //initialize default charstate colors

		resetContainingMenuBar();
		/* hire all inits */
		if (verboseStartup) System.out.println("main init 29");
		hireAllEmployees(MesquiteInit.class);

		logln("\n------------------------------------");
		if (numExtra ==0)
			logln("Citation for Mesquite:");
		else if (numExtra ==1)
			logln("Citations for Mesquite and extra package installed:");
		else
			logln("Citations for Mesquite and extra packages installed:");
		//Mesquite citation
		logln(citationsString);

		logln("====================================");
		if (!isApplet()){
			//setModuleWindow(null);
			BrowseHierarchy projectHierarchyTask= (BrowseHierarchy)hireEmployee(BrowseHierarchy.class, "Hierarchy browser");
			projectsWindow = new ProjectTreeWindow(this, projectHierarchyTask);
			//setModuleWindow(projectsWindow);
			//projectsWindow.setVisible(true);
			showLogWindow();
		}
		helpSearchManager = new HelpSearchManager();
		helpSearchManager.makeWindow();
		if (InterfaceManager.enabled){
			if (InterfaceManager.simplicityModule != null)
				InterfaceManager.simplicityModule.init();
		}
		if (about !=null) {
			about.setQuitIfGoAway(false);
			about.hide();
		}
		if (verboseStartup) System.out.println("main init 30");

		/* */
		addMenuItem(MesquiteTrunk.fileMenu, "Check Now for Notices/Installs...", new MesquiteCommand("checkNotices", this));
		if (MesquiteTrunk.phoneHome){
			phoneHomeThread = new PhoneHomeThread();
			phoneHomeThread.setContactMessage(earlyWarning);
			phoneHomeThread.start();
		}
		/**/

		if (verboseStartup) System.out.println("main init 31");
		decrementMenuResetSuppression();
		
		
		
		if (MesquiteTrunk.mesquiteTrunk.isPrerelease()) 
			logln("\nTHIS IS A PRERELEASE (ALPHA or BETA) VERSION: We discourage you from publishing results with this version of Mesquite, unless you check with the authors.\n");
		if (debugMode){ 
			logln("############### Startup Notices ###############");
			for (int i=0; i<startupNotices.size(); i++)
				if (startupNotices == null)
					logln(" startupNotices null");
				else
					logln((String)startupNotices.elementAt(i));
			logln("########################################");
		}

		if (logWindow!=null) {
			if (logWindow.isConsoleMode()){
				logln("Command-line Mode On.  Type \"help\" for some console commands.  Note: command-line mode is experimental.  Currently it is not properly protected against simultaneous calculations, e.g. doing different modifications simultaneously of the same tree or data.");
				logWindow.showPrompt();
			}
			logWindow.setVisible(true);
			logWindow.resetWindowSizeForce();

		}
		
		
		/* */
		cwt = new ClockWatcherThread(this);
		cwt.start();  
		/**/
		if (!MesquiteTrunk.consoleListenSuppressed) {
			ConsoleThread cot = new ConsoleThread(this, this, true);
			cot.start();
			consoleThread = cot;
		}
		if (isJavaVersionLessThan(1.9)){
			String warning1_8 = "WARNING: You are running an old version of Java, 1.8 or lower. It is HIGHLY RECOMMENDED that you update your version of Java to the latest JDK version of Java"
					+" (for example, JDK 21 or later) from oracle.com. Because you are using an old version of Java, Mesquite will crash with some important functions.";
			if (!java1_8warned)
				discreetAlert(warning1_8);
			else
				logln("\n\n=============\n" + warning1_8 + "\n=============\n\n");
			java1_8warned = true;
		}
		storePreferences();


		if (verboseStartup) System.out.println("main init 32 ");
		if (debugMode) MesquiteMessage.println("startup time: " + (System.currentTimeMillis()-startingTime));
		if (MesquiteTrunk.debugMode)
			addMenuItem(helpMenu, "Test Error Reporting", makeCommand("testError", this));
		
		postExtraPackagesReport();

	} 

	/*.................................................................................................................*/
	public void refreshBrowser(Class c){
		if (c == FileElement.class) {
			MesquiteMessage.printStackTrace();
		}
		else if (c == MesquiteProject.class || c == MesquiteFile.class) {
			if (projectsWindow!=null)
				projectsWindow.renew();
		}
		else
			super.refreshBrowser(c);
	}
	/*.................................................................................................................*/
	/** add a package splash banner to Mesquite's list of currently active packages */
	public void addSplash(MesquiteModuleInfo mmi){
		if (InterfaceManager.enabled){
			if (mmi.getModuleClass() == mesquite.Mesquite.class)
				;
			else if (mmi.getIsPackageIntro()){
				String name = mmi.getPackageName();
				Class c = mmi.getModuleClass();
				String path =  StringUtil.getAllButLastItem(StringUtil.getAllButLastItem(c.getName(), "."), ".");
				InterfaceManager.addPackageToList(name, path, mmi.getExplanation(), mmi.getHideable(), true);
			}
			else if (mmi.getHideable()) {
				String name = mmi.getName();
				Class c = mmi.getModuleClass();
				String path =  StringUtil.getAllButLastItem(c.getName(), ".");
				InterfaceManager.addPackageToList(name, path, mmi.getExplanation(), mmi.getHideable(), false);
			}
		}
		if (mmi.hasSplash() || mmi.getSplashURL()!=null) {
			if (about !=null && mmi.hasSplash()) 
				about.addSplash(mmi);
			if (!StringUtil.blank(mmi.getSplashURL())) {
				splashURLInfo.addElement(mmi);
				if (StringUtil.blank(mmi.getPackageName()))
					splashNames.addElement(new MesquiteString(mmi.getName()), false);
				else
					splashNames.addElement(new MesquiteString(mmi.getPackageName()), false);
			}
		}
	}
	/*.................................................................................................................EMBEDDED delete this if embedded */
	private void setMesquiteDirectoryPath(){
		if (mesquiteDirectory!=null){
			mesquiteDirectoryPath = mesquiteDirectory.getAbsolutePath();
			if (isWindows() && mesquiteDirectoryPath.lastIndexOf(MesquiteFile.fileSeparator + ".")== mesquiteDirectoryPath.length()-2)
				mesquiteDirectoryPath = mesquiteDirectoryPath.substring(0, mesquiteDirectoryPath.length()-1);
			else if (mesquiteDirectoryPath.lastIndexOf(MesquiteFile.fileSeparator)!= mesquiteDirectoryPath.length()-1)
				mesquiteDirectoryPath+= MesquiteFile.fileSeparator;
		}
	}
	/*.................................................................................................................EMBEDDED delete this if embedded *
	private void findMesquiteDirectory(){
		String sep = "" + MesquiteFile.fileSeparator;
		if (mesquiteDirectory!=null) {
			String manualString = mesquiteDirectory + sep + "docs/mesquite" + sep + "manual.html";
			File manualM = new File(manualString);
			if (manualM.exists()) {
				storedManualString = manualString;
			}
		}
		else {
			String manualString = mesquiteDirectory  + sep + "docs/mesquite" + sep + "manual.html";
			File manual = new File(manualString);

			storedManualString = manualString; 
		}

		setMesquiteDirectoryPath();
	}
	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			preferencesSet = true; //done to see that prefs file found; if not ask for registration
			//showLogWindow=prefs[0].charAt(0) == 'L';
			if (prefs.length<2)
				return;
			//storedManualString = prefs[1];
			if (prefs.length<3)
				return;

			int iq = MesquiteInteger.fromString(prefs[2]);
			if (MesquiteInteger.isCombinable(iq))
				MesquiteModule.textEdgeCompensationHeight = iq;
			MesquiteModule.textEdgeRemembered = true;
			if (prefs.length<4)
				return;			
			iq = MesquiteInteger.fromString(prefs[3]);
			if (MesquiteInteger.isCombinable(iq))
				MesquiteModule.textEdgeCompensationWidth = iq;

			if (prefs.length<6)
				return;
			/* EMBEDDED disable if embedded */
			setMesquiteDirectoryPath();
			/**/
			if (prefs.length<7)
				return;
			iq = MesquiteInteger.fromString(prefs[6]);
			if (prefs.length<8)
				return;
			numUses = MesquiteInteger.fromString(prefs[7]);
			if (!MesquiteInteger.isCombinable(numUses))
				numUses = 0;
			else
				numUses++;
			if (prefs.length<9)
				return;
			browserString = prefs[8];
			if (prefs.length<10)
				return;
			if (prefs[9].length()>0)
				defaultHideMesquiteWindow=prefs[9].charAt(0) == 'H';
			if (prefs.length<11)
				return;
			if (prefs[10].length()>0)
				numDirectories =MesquiteInteger.fromString(prefs[10], false);
			if (prefs.length<12)
				return;
			if (prefs[11].length()>0)
				configFile = prefs[11];
			if (prefs.length<13)
				return;
			if (prefs[12]!=null)
				consoleMode = "console".equals(prefs[12]);
			if (prefs.length<14)
				return;
			if (prefs[13] !=null) {
				int numLogs = MesquiteInteger.fromString(prefs[13]);
				if (MesquiteInteger.isCombinable(numLogs))
					MesquiteTrunk.numPrevLogs = numLogs;

			}
			if (prefs.length<15) {
				lastVersionRun = "1.04"; //104 or before
				return;
			}
			else if (prefs[14] != null) {
				lastVersionRun = prefs[14];
			}
			if (prefs.length<16)
				return;
			if (prefs.length<17)
				return;
			if (prefs[16] != null) {
				int search = MesquiteInteger.fromString(prefs[16]);
				if (MesquiteInteger.isCombinable(search))
					HelpSearchStrip.searchMODE = search;
			}
			if (prefs.length<18)
				return;
			if (prefs[17] != null) {
				if (prefs[17].equalsIgnoreCase("true"))
					MesquiteCommand.logEverything = true;
			}
			if (prefs.length<19)
				return;
			if (prefs[18] != null) {
				MesquiteInteger pos = new MesquiteInteger(0);
				int lastVersionUsedInt = MesquiteInteger.fromString(prefs[18], pos);
				int lastVersionNoticed = MesquiteInteger.fromString(prefs[18], pos);
				int lastNotice = MesquiteInteger.fromString(prefs[18], pos);
				int lastNoticeForMyVersion = MesquiteInteger.fromString(prefs[18], pos);
				if (MesquiteInteger.isCombinable(lastVersionUsedInt))
					this.lastVersionUsedInt = lastVersionUsedInt;
				if (MesquiteInteger.isCombinable(lastVersionNoticed))
					this.lastVersionNoticed = lastVersionNoticed;
				if (MesquiteInteger.isCombinable(lastNotice))
					this.lastNotice = lastNotice;
				if (MesquiteInteger.isCombinable(lastNoticeForMyVersion))
					this.lastNoticeForMyVersion = lastNoticeForMyVersion;
				if (lastVersionUsedInt != getVersionInt())
					this.lastNoticeForMyVersion = 0;
			}
		}
	}
	public void processSingleXMLPreference (String tag, String content) {
		preferencesSet = true; //done to see that prefs file found; if not ask for registration
		if ("storedManualString".equalsIgnoreCase(tag)){
			//	storedManualString = (content);  DEFUNCT with v. 3
		}
		else if ("textEdgeCompensationHeight".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq)) {
				MesquiteModule.textEdgeCompensationHeight = iq;
				MesquiteModule.textEdgeRemembered = true;
			}
		}
		else if ("textEdgeCompensationWidth".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq)){
				MesquiteModule.textEdgeCompensationWidth = iq;
				MesquiteModule.textEdgeRemembered = true;
				if (MesquiteModule.textEdgeCompensationWidth>20)  //put max
					MesquiteModule.textEdgeCompensationWidth = 20;
			}
		}
		else if ("numUses".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				numUses = iq+1;
			else
				numUses = 0;
		}
		else if ("browserString".equalsIgnoreCase(tag)){
			browserString = (content);
		}
		else if ("java1_8warned".equalsIgnoreCase(tag)){
			java1_8warned = MesquiteBoolean.fromTrueFalseString(content);
		}
		else if ("defaultHideMesquiteWindow".equalsIgnoreCase(tag)){
			defaultHideMesquiteWindow=MesquiteBoolean.fromTrueFalseString(content);
		}
		else if ("numDirectories".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				numDirectories = iq;
		}
		else if ("configFile".equalsIgnoreCase(tag)){
			configFile = (content);
		}
		else if ("consoleMode".equalsIgnoreCase(tag)){
			consoleMode = MesquiteBoolean.fromTrueFalseString(content);
		}
		else if ("numPrevLogs".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				numPrevLogs = iq;
		}
		else if ("scriptRecoveryDelay".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				ShellScriptUtil.recoveryDelay = iq;
		}
		else if ("maxNumMatrixUndoTaxa".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				maxNumMatrixUndoTaxa = iq;
		}
		else if ("maxNumMatrixUndoChars".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				maxNumMatrixUndoChars = iq;
		}
		else if ("lastVersionRun".equalsIgnoreCase(tag)){
			lastVersionRun = (content);
		}
		else if ("listUrgeGiven".equalsIgnoreCase(tag)){
		}
		else if ("searchMODE".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				HelpSearchStrip.searchMODE = iq;
		}
		else if ("logEverything".equalsIgnoreCase(tag)){
			MesquiteCommand.logEverything = MesquiteBoolean.fromTrueFalseString(content);
		}
		else if ("lastVersionUsedInt".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				lastVersionUsedInt = iq;
		}
		else if ("lastVersionNoticed".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				lastVersionNoticed = iq;
		}
		else if ("lastNotice".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				lastNotice = iq;
		}
		else if ("lastNoticeForMyVersion".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				lastNoticeForMyVersion = iq;

			if (lastVersionUsedInt != getVersionInt())
				lastNoticeForMyVersion = 0;		
		}
		else if ("mesquiteHeadlessPath".equalsIgnoreCase(tag)){ //hackathon
			previousMesquiteHeadlessPath = (content);
		}
		else if ("mesquitePath".equalsIgnoreCase(tag)){
			previousMesquitePath = (content);
		}
		else if ("projPanelWidth".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq))
				MesquiteFrame.defaultResourcesWidth = iq;
		}
		else if ("colorTheme".equalsIgnoreCase(tag)){
			int iq = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(iq)){
				ColorTheme.THEME = iq;
				ColorTheme.THEME_FOR_NEXT_STARTUP = iq;
			}
		}
		else if ("suggestedDirectory".equalsIgnoreCase(tag)){
			MesquiteTrunk.setSuggestedDirectory(StringUtil.cleanXMLEscapeCharacters(content));
		}

		/* EMBEDDED disable if embedded */
		setMesquiteDirectoryPath();
		/**/

	}

	/*.................................................................................................................*/
	String previousMesquiteHeadlessPath = "";//hackathon
	String previousMesquitePath = "";
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		//	StringUtil.appendXMLTag(buffer, 2, "storedManualString", storedManualString);  
		StringUtil.appendXMLTag(buffer, 2, "textEdgeCompensationHeight", MesquiteModule.textEdgeCompensationHeight);  
		StringUtil.appendXMLTag(buffer, 2, "textEdgeCompensationWidth", MesquiteModule.textEdgeCompensationWidth);  
		StringUtil.appendXMLTag(buffer, 2, "numUses", numUses);  
		StringUtil.appendXMLTag(buffer, 2, "browserString", browserString);  
		StringUtil.appendXMLTag(buffer, 2, "colorTheme", ColorTheme.THEME_FOR_NEXT_STARTUP);  
		StringUtil.appendXMLTag(buffer, 2, "defaultHideMesquiteWindow", defaultHideMesquiteWindow);  
		StringUtil.appendXMLTag(buffer, 2, "numDirectories", numDirectories);  
		StringUtil.appendXMLTag(buffer, 2, "projPanelWidth", MesquiteFrame.defaultResourcesWidth);  
		StringUtil.appendXMLTag(buffer, 2, "configFile", configFile);  
		StringUtil.appendXMLTag(buffer, 2, "consoleMode", consoleMode);  
		StringUtil.appendXMLTag(buffer, 2, "numPrevLogs", numPrevLogs);  
		StringUtil.appendXMLTag(buffer, 2, "scriptRecoveryDelay", ShellScriptUtil.recoveryDelay);  
		StringUtil.appendXMLTag(buffer, 2, "maxNumMatrixUndoTaxa", maxNumMatrixUndoTaxa);  
		StringUtil.appendXMLTag(buffer, 2, "maxNumMatrixUndoChars", maxNumMatrixUndoChars);  
		StringUtil.appendXMLTag(buffer, 2, "java1_8warned", java1_8warned);  

		StringUtil.appendXMLTag(buffer, 2, "lastVersionRun", lastVersionRun);  
		//	StringUtil.appendXMLTag(buffer, 2, "listUrgeGiven", listUrgeGiven);  
		StringUtil.appendXMLTag(buffer, 2, "searchMODE", HelpSearchStrip.searchMODE);  
		StringUtil.appendXMLTag(buffer, 2, "logEverything", MesquiteCommand.logEverything);  
		StringUtil.appendXMLTag(buffer, 2, "lastVersionUsedInt", lastVersionUsedInt);  
		StringUtil.appendXMLTag(buffer, 2, "lastVersionNoticed", lastVersionNoticed);  
		StringUtil.appendXMLTag(buffer, 2, "lastNotice", lastNotice);  
		StringUtil.appendXMLTag(buffer, 2, "lastNoticeForMyVersion", lastNoticeForMyVersion);  
		StringUtil.appendXMLTag(buffer, 2, "suggestedDirectory", MesquiteTrunk.getSuggestedDirectory());  

		if (MesquiteWindow.headless) { //hackathon
			StringUtil.appendXMLTag(buffer, 2, "mesquiteHeadlessPath", stripPath(mesquiteDirectoryPath));  
			StringUtil.appendXMLTag(buffer, 2, "mesquitePath", stripPath(previousMesquitePath));  
		}
		else {
			StringUtil.appendXMLTag(buffer, 2, "mesquiteHeadlessPath", stripPath(previousMesquiteHeadlessPath));  
			StringUtil.appendXMLTag(buffer, 2, "mesquitePath", stripPath(mesquiteDirectoryPath));  
		}

		return buffer.toString();
	}
	String stripPath(String path){
		if (path.lastIndexOf("../") == path.length()-3 || path.lastIndexOf("..") == path.length()-2){
			path = StringUtil.getAllButLastItem(path, "/..");
			path =  StringUtil.getAllButLastItem(path, "/");
		}
		return path;
	}
	/*.................................................................................................................*
	public String[] preparePreferencesForFile () {
		String[] prefs;
		prefs = new String[19];

		if (logWindow != null && logWindow.isVisible())
			prefs[0] = "L";
		else
			prefs[0]= "N";
		prefs[0] += "A02Y8O*=#";
		prefs[1]= "" + storedManualString;
		prefs[2]= Integer.toString(MesquiteModule.textEdgeCompensationHeight);
		prefs[3]= Integer.toString(MesquiteModule.textEdgeCompensationWidth);
		prefs[4]= "mq";
		prefs[5]= "0";
		prefs[6] = "1"; 
		prefs[7]= Integer.toString(numUses);
		if (browserString != null)
			prefs[8] = browserString;
		else
			prefs[8] = "";
		if (showAbout)
			prefs[9] = "H";
		else
			prefs[9] = "S";
		prefs[10] = Integer.toString(numDirectories);
		prefs[11] = configFile;
		if (logWindow != null && logWindow.isConsoleMode())
			prefs[12] = "console";
		else
			prefs[12] = "noconsole";
		prefs[13] = Integer.toString(MesquiteTrunk.numPrevLogs);
		prefs[14]= getMesquiteVersion();
		if (listUrgeGiven)
			prefs[15] = "U";
		else
			prefs[15] = "N";
		prefs[16] = Integer.toString(HelpSearchStrip.searchMODE);
		prefs[17] = Boolean.toString(MesquiteCommand.logEverything);
		prefs[18] = Integer.toString(getVersionInt()) + " " + Integer.toString(lastVersionNoticed) + " " + Integer.toString(lastNotice)  + " " + Integer.toString(lastNoticeForMyVersion);
		return prefs;
	}
	/*.................................................................................................................*/
	void setupColors(){
		/*for (int i=0; i<ColorDistribution.numColorSchemes; i++){
			String[] colorStrings = MesquiteFile.getFileContentsAsStrings(MesquiteModule.getRootPath() + "images/colors/" + i);
			if (colorStrings !=null && colorStrings.length>=5){
				stringPos.setValue(0);
				/*The old system brought in full color schemes from the files.  As of v. 2. 1, the color scheme is muted, more gray,
		 * and only two colors are brought in, projectLight and projectDark.  The old color scheme setting is still here, though nullified by
		 * subsequent calls *
				float r = (float)MesquiteDouble.fromString(colorStrings[0], stringPos);
				float g = (float)MesquiteDouble.fromString(colorStrings[0], stringPos);
				float b = (float)MesquiteDouble.fromString(colorStrings[0], stringPos);
				//ColorDistribution.pale[i] = new Color(r, g, b);
				//ColorDistribution.pale[i] = ColorDistribution.veryLightGray;//ggray
				stringPos.setValue(0);
				r = (float)MesquiteDouble.fromString(colorStrings[1], stringPos);
				g = (float)MesquiteDouble.fromString(colorStrings[1], stringPos);
				b = (float)MesquiteDouble.fromString(colorStrings[1], stringPos);
				//ColorDistribution.projectLight[i] = new Color(r, g, b);
				ColorDistribution.projectLight[i] = new Color(r, g, b);
				//ColorDistribution.projectLight[i] = ColorDistribution.veryLightGray;//ggray
				stringPos.setValue(0);
				r = (float)MesquiteDouble.fromString(colorStrings[2], stringPos);
				g = (float)MesquiteDouble.fromString(colorStrings[2], stringPos);
				b = (float)MesquiteDouble.fromString(colorStrings[2], stringPos);
				//ColorDistribution.medium[i] = new Color(r, g, b);
				//ColorDistribution.medium[i] = Color.lightGray;//ggray
				stringPos.setValue(0);
				r = (float)MesquiteDouble.fromString(colorStrings[3], stringPos);
				g = (float)MesquiteDouble.fromString(colorStrings[3], stringPos);
				b = (float)MesquiteDouble.fromString(colorStrings[3], stringPos);
				//ColorDistribution.dark[i] = new Color(r, g, b);
				ColorDistribution.projectDark[i] = new Color(r, g, b);
				//ColorDistribution.dark[i] = Color.gray;//ggray
				stringPos.setValue(0);
				r = (float)MesquiteDouble.fromString(colorStrings[4], stringPos);
				g = (float)MesquiteDouble.fromString(colorStrings[4], stringPos);
				b = (float)MesquiteDouble.fromString(colorStrings[4], stringPos);
				//ColorDistribution.project[i] = new Color(r, g, b);
				//ColorDistribution.project[i] = Color.lightGray;//ggray
			}
			else {
			/*	ColorDistribution.pale[i] = ColorDistribution.veryLightGray;
				ColorDistribution.projectLight[i] = ColorDistribution.veryLightGray;
				ColorDistribution.medium[i] = Color.lightGray;
				ColorDistribution.dark[i] = Color.gray;
				ColorDistribution.project[i] = Color.lightGray;*
				ColorDistribution.projectLight[i] = ColorDistribution.paleGoldenRod;
				ColorDistribution.projectDark[i] = ColorDistribution.sienna;

			}
			InfoBar.baseImage[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images/colors/infoBarBase.gif");  
			//InfoBar.baseImage[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images/colors/" + i + "infoBarBase.gif");  
			InfoBar.graphicsTab[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images/colors/" + i + "graphicsTab.gif");  
			InfoBar.textTab[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images/colors/" + i + "textTab.gif");  
			InfoBar.parametersTab[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images/colors/" + i + "parametersTab.gif");  
			InfoBar.modulesTab[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images/colors/" + i + "modulesTab.gif");  
			InfoBar.citationsTab[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images/colors/" + i + "citationsTab.gif");  
			MesquiteButton.offImage[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() + "images/colors/blank-off.gif");  //gray
			//MesquiteButton.offImage[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() + "images/colors/" + i + "blank-off.gif");  
			MesquiteButton.onImage[i]=  MesquiteImage.getImage(MesquiteModule.getRootPath() + "images/colors/" + i + "blank-on.gif");  
		}
		 */
		InfoBar.triangleImage=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "infoBarTriangle.gif");  
		InfoBar.triangleImageDown=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "triangleDown.gif");  
		InfoBar.releaseImage=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "release.gif");  
		InfoBar.prereleaseImage=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "prerelease.gif");  
		ExplanationArea.plusImage=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "explanationPlus.gif");  
		ExplanationArea.minusImage=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "explanationMinus.gif");  
		ExplanationArea.minusOffImage=  MesquiteImage.getImage(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "explanationMinusOff.gif");  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Mesquite";
	}
	/*.................................................................................................................*/
	public String getDutyName() {
		return "Root module for Mesquite system";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "This is the central module for the Mesquite system. " +
				"In it is the main method that starts up the Mesquite application.  This module " +
				"loads information for all of the other modules, and hires FileCoordinator " +
				"modules as needed to deal with open files.  Thus, in the tree of employees " +
				"that active modules make, this module is at its root.";
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return "";
	}
	/*.................................................................................................................*/
	public String getAuthors() {
		return "Wayne P. Maddison and David R. Maddison";
	}



	/*.................................................................................................................*/
	/*New code added Feb.05.07 oliver*/ //TODO: Delete new code comments	
	/*.................................................................................................................*/
	private void searchWeb(String queryTerm, MesquiteBoolean addMesquiteToSearch, MesquiteBoolean restrictSearchToManual){
		String textToGoogle = "";
		queryTerm = queryTerm.replaceAll("\'", "\"");
		queryTerm = queryTerm.replaceAll("\\\"", "%22");
		String[] result = queryTerm.split("\\s");
		for(int x = 0; x < result.length; x++){
			if (x == 0)
				textToGoogle = result[x];
			else
				textToGoogle += "+" + result[x];
		}
		if (addMesquiteToSearch.getValue())
			textToGoogle += "+" + "Mesquite";
		if (restrictSearchToManual.getValue())
			textToGoogle += "&as_sitesearch=http%3A%2F%2Fwww.mesquiteproject.org%2F";
		showWebPage("http://www.google.com/search?q=" + textToGoogle, false);
	}
	/*End new code added Feb.05.07 oliver*/
	/*New code added April.02.07 oliver*/ //TODO: delete new code comments
	/*.................................................................................................................*/
	private void openMesquiteFAQ(){
		String manualPath = mesquiteDocsURL + "/FAQ.html" ; 
		showWebPage(manualPath, false);
	}
	/*End new code added April.02.07 oliver*/

	public void employeeQuit(MesquiteModule mb){
		if (helpSearchManager!=null)
			helpSearchManager.employeeQuit(mb);
		super.employeeQuit(mb);
	}
	/*.................................................................................................................*/

	public void exit(boolean emergency, int status){
		if (startedAsLibrary){
			if (!emergency){
				logln("Mesquite is being used by another program.  You should avoid asking Mesquite to quit, and instead let the other program ask Mesquite to quit");
				return;
			}
			mesquiteExiting = true;
			doomed = true;
			MesquiteFrame f = logWindow.getParentFrame();
			f.setVisible(false);
			iQuit();
			while (windowVector.size()>0) {
				Enumeration e = windowVector.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					MesquiteWindow mw = (MesquiteWindow)obj;
					mw.setVisible(false);
					MesquiteFrame ff = mw.getParentFrame();
					if (ff != null){
						ff.setVisible(false);
						ff.dispose();
					}
					mw.dispose();
				}
			}
			MesquiteFile.closeLog();		
		}
		else
			System.exit(status);
	}
	/*.................................................................................................................*/
	public void start() {
		incrementMenuResetSuppression();
		ready = true;
		if (fileHandler !=null && fileHandler.isWaiting())
			fileHandler.openFilesNow();
		/*
		else if (isApplet()) { //EMBEDDED
			String urlString = getParameter("URL");
			if (urlString!=null) {
				openURLString(urlString);
			}
		}
		 */
		decrementMenuResetSuppression();
	}

	/*.................................................................................................................*/
	public MesquiteProject openGeneral(String readerString){
		return newProject(readerString, 3);
	}
	/*.................................................................................................................*/
	public MesquiteProject openURLString(String urlString){
		return newProject(urlString, 2);
	}
	/*.................................................................................................................*/
	public MesquiteProject openFile(String pathname, String originalArguments, Class importerSubclass){ //hackathon
		return newProject(pathname, 1, false, originalArguments,  importerSubclass);
	}
	/*.................................................................................................................*/

	public MesquiteProject openFile(String pathname){
		return newProject(pathname, 1,  false, null, null);
	}
	/*.................................................................................................................*/
	/* give alternative method which takes full file and passes it to FileCoordinator,
	which then uses alternative methods in MesquiteProject reader that parses this string
	instead of input stream*/
	public MesquiteProject openFile(String pathname, Class importerSubclass){
		return newProject(pathname, 1,  false, null, importerSubclass);
	}
	/*.................................................................................................................*/
	/* makes and returns a new project, in process making taxa block.*/
	private MesquiteProject newFileWithTaxa(String arguments){
		MesquiteProject proj =  newProject(arguments, 0);
		if (proj == null)
			return null;
		MesquiteFile mF = proj.getHomeFile();
		if (mF != null)
			mF.okForRecentRereading = true;
		return proj;
	}
	/*.................................................................................................................*/
	/* makes and returns a new project.*/
	private MesquiteProject newFile(String arguments){
		MesquiteProject proj = newProject(arguments, -1);
		if (proj == null)
			return null;
		MesquiteFile mF = proj.getHomeFile();
		if (mF != null)
			mF.okForRecentRereading = true;
		return proj;
	}
	/*.................................................................................................................*/
	/* makes and returns a new project.*/
	public MesquiteProject newProject(String arguments, int code){
		return newProject(arguments, code, false);
	}
	/* makes and returns a new project.*/ //hackathon
	public MesquiteProject newProject(String arguments, int code, boolean actAsScriptingRegardless){
		return newProject(arguments, code, actAsScriptingRegardless, null, null);
	}
	/* makes and returns a new project.* NOT USED/ //hackathon
	public MesquiteProject newProject(String arguments, int code, boolean actAsScriptingRegardless, String originalArguments){
		return newProject(arguments, code, actAsScriptingRegardless, originalArguments, null);
	}
	/*.................................................................................................................*/
	/* makes and returns a new project.*///hackathon
	public MesquiteProject newProject(String arguments, int code, boolean actAsScriptingRegardless, String originalArguments, Class importerSubclass){
		if (MesquiteThread.isScripting() || actAsScriptingRegardless) {
			ObjectContainer projCont = new ObjectContainer();
			ProjectRead pr = new ProjectRead(arguments,  code, mesquiteTrunk, projCont);
			if (originalArguments != null)
				pr.setOriginalArguments(originalArguments);
			pr.setImporterSubclass(importerSubclass);
			pr.run();
			MesquiteProject p = (MesquiteProject)projCont.getObject();
			projCont.setObject(null); //This is done because Threads not being finalized, and need to remove references to projects
			return p;
		}
		else {
			ProjectRead pr = new ProjectRead(arguments,  code, mesquiteTrunk, null);
			ProjectReadThread pt = new ProjectReadThread(pr);
			pt.indicatorSuppressed = MesquiteThread.getHintToSuppressProgressIndicatorsCurrentThread();
			if (originalArguments != null)
				pr.setOriginalArguments(originalArguments);

			pr.setImporterSubclass(importerSubclass);
			pt.settempID(arguments);
			pr.setThread(pt);
			pt.start();
			return null;
		}
	}
	
	/*.................................................................................................................*/
	/** Requests a window to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
	}
	boolean closeAllProjects(){
		Projects projs = getProjectList();
		MesquiteProject[] ps = new MesquiteProject[projs.getNumProjects()];
		for (int i= 0; i<ps.length; i++) {
			ps[i]= projs.getProject(i);
		}
		for (int i= 0; i<ps.length; i++) {
			MesquiteProject p = ps[i];
			if (p!=null) {
				FileCoordinator fc = p.getCoordinatorModule();
				if (fc!=null) {
					if (!fc.closeFile(p.getHomeFile())){
						logln("Sorry, unable to close " + p.getName());
						return false;
					}
				}
			}
		}
		return true;
	}
	void saveAllProjects(){
		Projects projs = getProjectList();
		MesquiteProject[] ps = new MesquiteProject[projs.getNumProjects()];
		for (int i= 0; i<ps.length; i++) {
			ps[i]= projs.getProject(i);
		}
		for (int i= 0; i<ps.length; i++) {
			MesquiteProject p = ps[i];
			if (p!=null) {
				FileCoordinator fc = p.getCoordinatorModule();
				if (fc!=null) 
					fc.saveAllFiles();
			}
		}
	}
	public void fileDirtiedByCommand(MesquiteCommand command){
		if (command !=null && command.getOwner() == this && ("quit".equalsIgnoreCase(command.getName()) || "exit".equalsIgnoreCase(command.getName())))
			return;
		super.fileDirtiedByCommand(command);
	}
	/*.................................................................................................................*/
	private ListableVector getMacroRecords(boolean autoOnly){
		ListableVector macros = new ListableVector();
		for (int i = 0; i<mesquiteModulesInfoVector.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
			if (mmi.getMacros()!=null && mmi.getMacros().size()>0){
				Vector mForM = mmi.getMacros();
				for (int j = 0; j<mForM.size(); j++){
					MesquiteMacro mmr = (MesquiteMacro)mForM.elementAt(j);
					if (!autoOnly || mmr.isAutoSave())
						macros.addElement(mmr, false);
				}
			}
		}
		if (macros.size() == 0)
			return null;
		return macros;
	}
	public void reportMemory(){
		if (!checkMemory) {
			logln("Memory status cannot be reported because checkMemory is not set");
			return;
		}
		Vector wcre,wct, wctf;
		wcre = MesquiteWindow.classesCreated;
		wct = MesquiteWindow.countsOfClasses;
		wctf = MesquiteWindow.countsOfClassesFinalized;
		logln("Window classes created     " );
		for (int i=0; i<wcre.size(); i++){
			logln("    " + wcre.elementAt(i) + "  created: " + wct.elementAt(i) + "  finalized: " + wctf.elementAt(i));
		}

		Vector cre, fin, ct, ctd;
		cre = FileElement.classesCreated;
		fin = FileElement.classesFinalized;
		ct = FileElement.countsOfClasses;
		ctd = FileElement.countsOfClassesDisposed;
		if (cre.size()>fin.size()){
			logln("File element classes created but not finalized    " );
			for (int i=0; i<cre.size(); i++){
				if (fin.indexOf(cre.elementAt(i))<0)
					logln("    " + cre.elementAt(i) + "  count: " + ct.elementAt(i));
			}
		}
		logln("File element classes with fewer disposals than creations    " );
		for (int i=0; i<ct.size() && i<ctd.size(); i++){
			MesquiteInteger mi = (MesquiteInteger)ct.elementAt(i);
			MesquiteInteger mid = (MesquiteInteger)ctd.elementAt(i);
			if (mi.getValue()!=mid.getValue())
				logln("    " + cre.elementAt(i) + "  created: " + mi + " disposed: " + mid);
		}

		logln("NexusBlock.totalFinalized " +  NexusBlock.totalFinalized + " NexusBlock.totalDisposed " + NexusBlock.totalDisposed + " NexusBlock.totalCreated " +  NexusBlock.totalCreated);
		cre = NexusBlock.classesCreated;
		fin = NexusBlock.classesFinalized;
		ct = NexusBlock.countsOfClasses;
		ctd = NexusBlock.countsOfClassesFinalized;
		logln("NexusBlock class instantiations    " );
		for (int i=0; i<cre.size(); i++){
			logln("    " + cre.elementAt(i) + "  count: " + ct.elementAt(i) + "  ever finalized? " + (fin.indexOf(cre.elementAt(i))>=0) + "  num finalizations " + ctd.elementAt(i));
		}

		logln("ListableVector.totalFinalized " +  ListableVector.totalFinalized + " ListableVector.totalDisposed " + ListableVector.totalDisposed + " ListableVector.totalCreated " +  ListableVector.totalCreated);
		logln("MesquiteCommand.totalFinalized " +  MesquiteCommand.totalFinalized + " MesquiteCommand.totalDisposed " + MesquiteCommand.totalDisposed + " MesquiteCommand.totalCreated " +  MesquiteCommand.totalCreated);

		cre = MesquiteCommand.classesLinked;
		fin = MesquiteCommand.classesUnlinked;
		ct = MesquiteCommand.countsOfClasses;
		if (cre.size()>fin.size()){
			logln("MesquiteCommand objects linked but not unlinked    " );
			for (int i=0; i<cre.size(); i++){
				if (fin.indexOf(cre.elementAt(i))<0)
					logln("    " + cre.elementAt(i) + "  count: " + ct.elementAt(i));
			}
		}

		logln(" MesquiteMenuBar.totalFinalized " +  MesquiteMenuBar.totalFinalized + "  MesquiteMenuBar.totalCreated " +  MesquiteMenuBar.totalCreated);
		logln(" MesquiteMenuItem.totalFinalized " +  MesquiteMenuItem.totalFinalized + " MesquiteMenuItem.totalDisposed " + MesquiteMenuItem.totalDisposed + " MesquiteMenuItem.totalCreated " +  MesquiteMenuItem.totalCreated);
		logln(" MesquiteMenuItemSpec.totalFinalized " +  MesquiteMenuItemSpec.totalFinalized + " MesquiteMenuItemSpec.totalDisposed " + MesquiteMenuItemSpec.totalDisposed + " MesquiteMenuItemSpec.totalCreated " +  MesquiteMenuItemSpec.totalCreated);
		logln(" MesquiteModule.totalFinalized " +  MesquiteModule.totalFinalized + " EmployerEmployee.totalDisposed " +  EmployerEmployee.totalDisposed + " EmployerEmployee.totalCreated " +  EmployerEmployee.totalCreated);
		logln("Projects.projectsAdded " + Projects.projectsAdded + " Projects.projectsRemoved " +  Projects.projectsRemoved);
		logln("FileCoordinator.totalFinalized " + FileCoordinator.totalFinalized + " FileCoordinator.totalCreated " +  FileCoordinator.totalCreated);
		logln("MesquiteTree.totalFinalized " + MesquiteTree.totalFinalized  + " MesquiteTree.totalDisposed " + MesquiteTree.totalDisposed + " MesquiteTree.totalCreated " +  MesquiteTree.totalCreated);
		logln("Taxon.totalFinalized " + Taxon.totalFinalized  + " Taxon.totalCreated " +  Taxon.totalCreated);
		logln("Associable.totalFinalizedA " + Associable.totalFinalizedA  + " Associable.totalDisposedA " +  Associable.totalDisposedA + " Associable.totalCreatedA " +  Associable.totalCreatedA);
		logln("CharacterData.totalDisposed " + CharacterData.totalDisposed + " CharacterData.totalCreated " +  CharacterData.totalCreated);
		logln("TreeVector.totalDisposed " + TreeVector.totalDisposed + " TreeVector.totalCreated " +  TreeVector.totalCreated);
		logln("SpecsSet.totalDisposed " + SpecsSet.totalDisposed + " SpecsSet.totalCreated " +  SpecsSet.totalCreated);
		logln("Listened.listenersRemaining " + Listened.listenersRemaining);
		logln("Listened.listenersReport\n" + Listened.reportListeners());
		logln("Threads");
		for (int i=0; i<MesquiteThread.threads.size(); i++){
			logln("    " + MesquiteThread.threads.elementAt(i));
		}
	}
	/*.................................................................................................................*/
	public void showHTMLSnippet(String s){
		if (helpSearchManager != null)
			helpSearchManager.showHTML(s);
	}
	/*.................................................................................................................*/
	public void searchKeyword(String s, boolean useBrowser){
		if (helpSearchManager != null)
			helpSearchManager.searchKeyword(s, useBrowser);

	}
	public void searchData(String s, MesquiteWindow window){
		if (helpSearchManager != null)
			helpSearchManager.searchData(s, window);
	}
	ProgressIndicator omp = null;
	/* ...................................................... */
	/**
	 * Composes and shows a web page listing all of the modules.
	 */
	public void composePageOfModules() {
		omp = new ProgressIndicator(null, "Composing web page of modules", MesquiteTrunk.mesquiteModulesInfoVector.size(), false);
		omp.start();
		omp.setCurrentValue(0);

		StringBuffer sb = new StringBuffer();
		int count=0;
		String prevPackageName="";
		Vector<String> vector = new Vector<String>();

		for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
			if (mmi.loadModule()) {
				if (prevPackageName == null || (mmi!=null && prevPackageName != null && !prevPackageName.equalsIgnoreCase(mmi.getPackageName()))) {
					prevPackageName=mmi.getPackageName();
					if (prevPackageName!=null){
						Collections.sort(vector);
						for(int j=0; j < vector.size(); j++){
							sb.append(vector.get(j));
						}
						vector.clear();
						sb.append("<hr>");
						String vers = mmi.getVersion();
						if (vers == null)
							vers = "";
						if (StringUtil.blank(vers))
							sb.append( "<h2>"+ mmi.getPackageName()+"</h2>");
						else
							sb.append( "<h2>"+ mmi.getPackageName() + ", version " + vers + "</h2>");
					}
				}
				vector.add("<li><b>"+mmi.getName() + "</b>:\t" + mmi.getExplanation()  + "</li>");
				count++;
			}
		} 
		if (!vector.isEmpty()) {
			Collections.sort(vector);
			for(int j=0; j < vector.size(); j++){
				sb.append(vector.get(j));
			}
		}
		String allModulesHTML = " <title>Modules in Mesquite</title>";
		allModulesHTML += "<body>";
		allModulesHTML += "<h1>List of All Installed Modules in Mesquite</h1>";
		allModulesHTML += "Total number of modules: <b>"+count+"</b>";
		allModulesHTML += ("<ul>");
		allModulesHTML += sb.toString();
		allModulesHTML += ("</ul>");
		allModulesHTML += ("</body>");
		String modulesListPath= MesquiteModule.prefsDirectory + MesquiteFile.fileSeparator + "allModules.html";

		MesquiteFile.putFileContents(modulesListPath,allModulesHTML, true);
		omp.goAway();
		omp = null;
		File testing = new File(modulesListPath);
		if (testing.exists()) {
			showWebPage(modulesListPath, true);
		}

	}

	public String getNumModuleStarts() {
		StringBuffer sb=new StringBuffer();
		for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
			int starts = mmi.getNumStarts();
			if (starts>0)
				sb.append(mmi.getClassName() + "\t" + starts+StringUtil.lineEnding());
		}
		return sb.toString();
	}
	public MesquiteProject openOrImportFileHandler(String path, String completeArguments, Class importerSubclass){
		MesquiteProject f;
		CommandRecord comRec  = MesquiteThread.getCurrentCommandRecord();
		CommandRecord cr;
		if (comRec == null || comRec == CommandRecord.nonscriptingRecord || comRec.getID()<2) {
			cr =new CommandRecord((CommandThread)null, false);
			if (comRec == null)
				cr.setFromCommandLine(Thread.currentThread() instanceof ConsoleThread);
			else
				cr.setFromCommandLine(comRec.isFromCommandLine());
		}
		else
			cr = comRec;
		CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
		MesquiteThread.setCurrentCommandRecord(cr);

		if (StringUtil.blank(path)) {
			f = openFile(null, completeArguments, importerSubclass); 
		}
		else {
			String baseN = null;
			if (MesquiteThread.isScripting()) {
				MesquiteFile base = null;
				base = CommandRecord.getScriptingFileS();
				if (base !=null)
					baseN = base.getDirectoryName();
			}
			if (!MesquiteFile.fileExists(path) && (!StringUtil.blank(baseN) && MesquiteFile.fileExists(baseN+path)))
				path = baseN+path;
			f= openFile(path, completeArguments, importerSubclass);
		}
		MesquiteThread.setCurrentCommandRecord(prevR);
		return f;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	String noticeLocation = "http://"; //before release, change URL to "http://"
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "A command that is ignored; can give to menu items so that they are active but do nothing", "[]", commandName, "null")) {
			
		}
		else if (checker.compare(this.getClass(), "Sets the packages of modules loaded at startup, using a configuration file", null, commandName, "setConfig")) {
			//need Module Activation menu item with submenu items: Use All Installed Modules; Choose Module set
			//use MesquiteTrunks.configs, a vector of configurations found (each stores name, explanation)
			Listable[] list = new Listable[configurations.size()];
			for (int i=0; i<configurations.size(); i++){
				Object o = configurations.elementAt(i);
				ConfigFileRecord cfr = (ConfigFileRecord)o;
				list[i]= cfr;
			}
			String c = configuration;
			if (c == null)
				c = "All Installed Modules";
			Listable chosen = ListDialog.queryList(containerOfModule(), "Choose configuration", "Choose the configuration file that indicates what packages of modules are to be loaded at startup.  Current configuration is \"" + c + "\".", MesquiteString.helpString, list, 0);
			if (chosen != null){
				configFile = ((ConfigFileRecord)chosen).getPath();
				storePreferences();
				discreetAlert("You will need to restart Mesquite to load the selected configuration");
			}

		}
		else if (checker.compare(this.getClass(), "Defines a set of  packages to be loaded at startup, using a configuration file", null, commandName, "defineConfig")) {
			boolean[] selected = new boolean[packages.size()];
			for (int i= 0; i<packages.size(); i++){
				MesquitePackageRecord mpr = (MesquitePackageRecord)packages.elementAt(i);
				selected[i] =mpr.getLoaded();

			}
			String c = getConfiguration();
			if (c == null)
				c = "All Installed Modules";
			Listable[] list = ChecklistDialog.queryListMultiple(containerOfModule(), "Define Configuration", "Shown are packages of modules available in this installation of Mesquite.  The currently loaded packages are shown in boldface.  Select and deselect to define a new configuration of packages to be loaded.  Current configuration is \"" + c + "\".", packages, selected);
			if (list!=null && list.length>0) {
				String[] contents = new String[list.length+1];

				MesquiteBoolean answer = new MesquiteBoolean(false);
				MesquiteString name = new MesquiteString("User-defined module configuration");
				MesquiteString explanation = new MesquiteString("");
				MesquiteString.queryTwoStrings(containerOfModule(), "Specify saved configuration", "Name of configuration of module packages", "Explanation of configuration of module packages",  answer, name, explanation,true);
				if (!answer.getValue())
					return null;
				contents[0] = ParseUtil.tokenize(name.getValue()) + "   " +  ParseUtil.tokenize(explanation.getValue());
				for (int i=0; i<list.length; i++) {
					MesquitePackageRecord mpr = (MesquitePackageRecord)list[i];
					contents[i+1] = mpr.getPackageName();
				}
				String base = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator +"configs";
				if (!MesquiteFile.fileExists(base)) {
					File f = new File(base);
					f.mkdir();
				}
				String candidate = base + "/test1.config";
				int count = 2;
				while (MesquiteFile.fileExists(candidate)){
					candidate = base + "/test" + (count++) + ".config";
				}
				MesquiteFile.putFileContents(candidate, contents, true); 
				configFile = candidate;
				storePreferences();
				discreetAlert("You will need to restart Mesquite to load the defined configuration");
				ConfigFileRecord cfr = new ConfigFileRecord(true);
				cfr.cStored[0] = candidate;
				cfr.cStored[1] = name.getValue();
				cfr.cStored[2]= explanation.getValue();
				configurations.addElement(cfr, false);
			}
		}
		else if (checker.compare(this.getClass(), "Brings up a dialog box by which user-defined configuration files can be deleted", null, commandName, "deleteConfig")) {

			//first get list of user-defined configs; must filter configurations vector to include only user-defined.  Then delete corresponding files!
			ListableVector ud = new ListableVector();
			for (int i= 0; i<configurations.size(); i++){
				ConfigFileRecord config = (ConfigFileRecord)configurations.elementAt(i);
				if (config.isUserDefined())
					ud.addElement(config, false);
			}
			String c = configuration;
			if (c == null)
				c = "All Installed Modules";
			Listable[] list = ListDialog.queryListMultiple(containerOfModule(), "Delete Configuration", "Select the configurations to be deleted and hit \"OK\" to delete them.  Current configuration is \"" + c + "\".", MesquiteString.helpString, "Delete", false, ud, null);
			if (list!=null && list.length>0) {
				if (AlertDialog.query(containerOfModule(), "Delete?", "Are you sure you want to delete the configuration(s)?")){
					for (int i=0; i<list.length; i++) {
						ConfigFileRecord config = (ConfigFileRecord)list[i];
						File f = new File(config.getPath());
						f.delete();
						configurations.removeElement(config, false);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Brings up a dialog box to change name and explanation of user-saved macros", null, commandName, "editMacroInfo")) {
			MesquiteMacro macro = MesquiteMacro.findMacro(parser.getFirstToken(arguments));
			if (macro==null)
				return null;
			String[] oldFile = MesquiteFile.getFileContentsAsStrings(macro.getPath());
			if (oldFile == null || oldFile.length ==0)
				return null;
			MesquiteBoolean answer = new MesquiteBoolean(false);
			MesquiteString name = new MesquiteString(macro.getName());
			MesquiteString explanation = new MesquiteString(macro.getRawExplanation());
			MesquiteString.queryTwoStrings(containerOfModule(), "Edit Macro Information", "Name of macro for window (to appear as menu item)", "Explanation of macro",  answer, name, explanation, true);
			if (!answer.getValue())
				return null;
			macro.setName(name.getValue());
			macro.setExplanation(explanation.getValue());
			//SAVE FILE
			oldFile[0] = "telling " +   macro.getModuleInfo().getClassName()  + "  " + ParseUtil.tokenize(name.getValue()) + "   " +  ParseUtil.tokenize(explanation.getValue()) + ";" + StringUtil.lineEnding();
			MesquiteFile.putFileContents(macro.getPath(), oldFile, true);
			resetAllMenuBars();
		}
		else if (checker.compare(this.getClass(), "Returns a file coordinator for a project", "[number of project]", commandName, "getFileCoordinator")) {
			int iProj = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(iProj) && iProj<projects.getNumProjects()){
				MesquiteProject proj = projects.getProject(iProj);
				return proj.getCoordinatorModule();
			}
		}
		else if (checker.compare(this.getClass(), "Brings up a dialog box to examine macro files", null, commandName, "showMacros")) {

			//first get list of user-defined configs; must filter configurations vector to include only user-defined.  Then delete corresponding files!
			ListableVector ud = getMacroRecords(false);

			Listable[] list = ListDialog.queryListMultiple(containerOfModule(), "Macro Information", "These are the installed macros.  Touch on their names to see an explanation.", MesquiteString.helpString, ud, null);
		}
		else if (checker.compare(this.getClass(), "Crashes with a NullPointerException", null, commandName, "crash")) {
			logln("Mesquite will now crash with a NullPointerException.  Please enjoy.");
			MesquiteInteger mi = null;
			logln("This will crash: " +mi.getValue());
		}
		else if (checker.compare(this.getClass(), "Checks for notices or installs at the location specified", null, commandName, "checkNotices")) {
			noticeLocation = MesquiteString.queryString(containerOfModule(), "Notices Location", "Indicate URL to notices file to be checked.  (For instance, for Mesquite the default notices file is at " + getHomePhoneNumber() + ")\n\nNote: There may be a delay as Mesquite waits for the response from the server.", noticeLocation);
			PhoneHomeUtil.checkForNotices(noticeLocation); 
			PhoneHomeUtil.adHocRecord = null;
			resetAllMenuBars();
		}
		else if (checker.compare(this.getClass(), "Brings up a dialog box by which automatically-defined macros files can be deleted", null, commandName, "deleteAutoMacros")) {

			//first get list of user-defined configs; must filter configurations vector to include only user-defined.  Then delete corresponding files!
			ListableVector ud = getMacroRecords(true);

			Listable[] list = ListDialog.queryListMultiple(containerOfModule(), "Delete Macro", "Select the macros to be deleted and hit \"OK\" to delete them", MesquiteString.helpString, "Delete", false, ud, null);

			if (list!=null && list.length>0) {
				if (AlertDialog.query(containerOfModule(), "Delete?", "Are you sure you want to delete the macro(s)?")){
					for (int i=0; i<list.length; i++) {
						MesquiteMacro macro = (MesquiteMacro)list[i];
						MesquiteModuleInfo mmi = macro.getModuleInfo();
						File f = new File(macro.getPath());
						f.delete();
						if (mmi !=null)
							mmi.removeMacro(macro);
					}
					resetAllMenuBars();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Redirects log saving to the given filename", "[filename]", commandName, "redirectLog")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name))
				name = MesquiteString.queryString(containerOfModule(), "Redirect Log", "Indicate name of log file to which subsequent log output is to be directored.  It will be saved in the directory " + supportFilesDirectory, MesquiteTrunk.logFileName);
			if (StringUtil.blank(name))
				return null;
			logln("Log redirected to \"" + name + "\"");
			MesquiteFile.closeLog();
			MesquiteTrunk.logFileName = name;
		}
		else if (checker.compare(this.getClass(), "Changes the current log file's name", "[filename]", commandName, "renameLog")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name))
				name = MesquiteString.queryString(containerOfModule(), "Rename Log", "Indicate new name of log file.  It will be saved in the directory " + supportFilesDirectory, MesquiteTrunk.logFileName);
			if (StringUtil.blank(name))
				return null;
			MesquiteFile.closeLog();
			logln("Log renamed to \"" + name + "\"");
			String currentPath = supportFilesDirectory  + MesquiteFile.fileSeparator + MesquiteTrunk.logFileName;
			MesquiteTrunk.logFileName = name;
			MesquiteFile.rename(currentPath, supportFilesDirectory + MesquiteFile.fileSeparator + MesquiteTrunk.logFileName);


		}
		else if (checker.compare(this.getClass(), "Indicates that all installed modules are to be loaded at startup", null, commandName, "setConfigAll")) {
			configFile = "all";
			storePreferences();
			discreetAlert("You will need to restart Mesquite to load all of the modules");
		}

		else if (checker.compare(this.getClass(), "Opens file on disk.  The file will be opened as a separate project (i.e. not sharing information) from any other files currently open.", "[name and path of file] - if parameter absent then presents user with dialog box to choose file", commandName, "openFile")) {
			String path = ParseUtil.getFirstToken(arguments, stringPos);
			String completeArguments = arguments;
			return openOrImportFileHandler( path,  completeArguments, null);

		}
		else if (checker.compare(this.getClass(), "Explains the File>Open Special items", "[]", commandName, "explainOpenChoices")) {
			String explanation = "<html><body><h3>Open Special</h3>These menu items open files as separate (independent) projects. "
					+ "Those at the top of the submenu open a single file; those at the bottom open multiple files in a folder."
					+" The different options serve different purposes and have different limitations.<br><br>";
			explanation += "<b>Reads One File</b><br><ul>";
			MesquiteMenuItemSpec mmis = new MesquiteMenuItemSpec(null, "", module, null);  //temporary; doesn't get registered; just helps find compatible modules
			mmis.setList(GeneralFileMakerSingle.class);
			MesquiteModuleInfo mbi = null;
			while ((mbi = getNextCompatibleModuleOfDuty(mbi, mmis)) != null) {
				explanation += "<li><b>" + StringUtil.protectForXML(mbi.getNameForMenuItem()) + "</b>— " + StringUtil.protectForXML(mbi.getExplanation()) + "</li><br>";
			}
			explanation += "</ul><b>Reads Multiple Files in a Folder</b><br><ul>";
			mmis.setList(GeneralFileMakerMultiple.class);
			mbi = null;
			while ((mbi = getNextCompatibleModuleOfDuty(mbi, mmis)) != null) {
				explanation += "<li><b>" + StringUtil.protectForXML(mbi.getNameForMenuItem()) + "</b>— " + StringUtil.protectForXML(mbi.getExplanation()) + "</li><br>";
			}
			explanation += "</ul></body></html>";
			//AlertDialog.noticeHTML(containerOfModule(), "Open Special Submenu", explanation, 500, 500, null);
			alertHTML(containerOfModule().getParentFrame(), explanation,"Open Special Submenu", null, 600, 500);
		}
		else if (checker.compare(this.getClass(), "Opens recent file.  The file will be opened as a separate project (i.e. not sharing information) from any other files currently open.", "[name and path of file]", commandName, "openRecent")) {
			String path = ParseUtil.getFirstToken(arguments, stringPos);
			MesquiteFile currentFile = MesquiteTrunk.getProjectList().findFile(path);
			if (currentFile == null) {
				String completeArguments = arguments;
				return openOrImportFileHandler( path,  completeArguments, null);
			}
			else {
				MesquiteProject p = currentFile.getProject();
				if (p!= null)
					p.getCoordinatorModule().doCommand("allToFront", null);
			}

		}
		else if (checker.compare(this.getClass(), "Clears list of recent files.", null, commandName, "clearRecent")) {
			clearRecentFiles(true);
			resetAllMenuBars();
		}
		else if (checker.compare(this.getClass(), "Opens file on web server.  The file will be opened as a separate project (i.e. not sharing information) from any other files currently open.", "[URL of file] - if parameter absent then presents user with dialog box to enter URL", commandName, "openURL")){
			arguments = ParseUtil.getFirstToken(arguments, stringPos);
			return openURLString(arguments);
		}
		else if (checker.compare(this.getClass(), "Opens module for project reading or making.", "[module]", commandName, "openGeneral")){
			//arguments = ParseUtil.getFirstToken(arguments, stringPos);
			return openGeneral(arguments);
		}
		else if (checker.compare(this.getClass(), "Closes file and any other files linked to it (i.e. closes the project)", "[ID number of project]", commandName, "closeProjectByID")) {
			int num = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(num)) {
				MesquiteProject proj = MesquiteTrunk.mesquiteTrunk.projects.getProjectByID(num);
				if (proj==null)
					return null;
				FileCoordinator coord = proj.getCoordinatorModule();
				if (coord==null)
					return null;
				coord.closeFile(proj.getHomeFile());
			}
		}
		else if (checker.compare(this.getClass(), "Makes a new file (as a new project)", null, commandName, "newProject")) {
			return newFileWithTaxa(arguments);
		}
		else if (checker.compare(this.getClass(), "Makes a new file (as a new project)", null, commandName, "newEmptyProject")) {
			return newFile(arguments);
		}
		else if (checker.compare(this.getClass(), "Forces a reset of the menus.", null, commandName, "resetMenus")) {
			zeroMenuResetSuppression();
			resetAllMenuBars();
			zeroMenuResetSuppression();
		}
		else if (checker.compare(this.getClass(), "Shows the GNU Lesser General Public License.", null, commandName, "showLicense")) {
			TextDisplayer displayer = (TextDisplayer)hireEmployee(TextDisplayer.class, null);
			if (displayer!=null)
				displayer.showFile(getRootPath()  + "lesser.txt", 100000, true); 
			return displayer;
		}
		/*	else if (checker.compare(this.getClass(), "Shows the currently executing command and offers to stop it.", null, commandName, "currentCommand")) {
			if (MainThread.getCurrentlyExecuting()!=null) {
				MainThread.mainThread.suspend();
				if (!AlertDialog.query(containerOfModule(), "Current Command", "The current command is\n" + MainThread.getCurrentlyExecuting().getListName(), "Continue", "STOP COMMAND")) {
					MainThread.getCurrentlyExecuting().interrupted = true;
					MainThread.mainThread.interrupt();
					MainThread.mainThread.stop();
					alert("Stopped " + MainThread.getCurrentlyExecuting().getListName()); 
					MainThread.mainThread = new MainThread();
					MainThread.mainThread.start();
				}
				else 
					MainThread.mainThread.resume();
			}
			else
				alert("There is no command currently executing");
		}
		 */
		else if (checker.compare(this.getClass(), "Shows the list of pending commands and offers some to be suspended.", null, commandName, "pendingCommands")) {
			if (MesquiteCommand.anyQueuedCommands(true)) {
				String message = "This is a list of pending commands.  If you want to suspend any commands, select them and hit STOP COMMAND.  Otherwise, hit CANCEL.  The currently executing command is NOT listed.";
				Listable[] commandsToKill = ListDialog.queryListMultiple(containerOfModule(), "Commands pending", message, MesquiteString.helpString, "STOP COMMAND", false, MainThread.listPendingCommands(), null);
				if (commandsToKill !=null && commandsToKill.length>0){
					for (int i=0; i<commandsToKill.length;  i++)
						MainThread.pendingCommands.removeElement(commandsToKill[i], false);
				}
			}
			else
				alert("There are no commands pending");
		}
		else if (checker.compare(this.getClass(), "Reports memory status, for debugging of memory leaks.  Static boolean checkMemory needs to have been set first.", null, commandName, "reportMemory")) {
			reportMemory();
		}
		else if (checker.compare(this.getClass(), "Quits Mesquite", null, commandName, "quit") || checker.compare(this.getClass(), "Quits Mesquite", null, commandName, "exit")) { //"exit" here also in case command line use
			if (startedAsLibrary){
				logln("Mesquite is being used by another program.  You should avoid asking Mesquite to quit, and instead let the other program ask Mesquite to quit");
				return null;
			}
			MesquiteThread.acceptNonMesquiteThreads = true;
			PendingCommand pc = MainThread.getCurrentlyExecuting();

			if (pc!=null && pc.getCommandRecord() != MesquiteThread.getCurrentCommandRecord() && !pc.permitQuitUnqueried()) {
				if (AlertDialog.query(containerOfModule(), "Command executing", "Are you sure you want to quit?  There is a command currently executing (" + MainThread.getCurrentlyExecuting().getListName() + ").", "Continue", "Quit")){
					//CommandRecord.checkThread = true;
					System.out.println("Quit cancelled");
					return null;
				}
			}
			MesquiteTrunk.startupShutdownThread = Thread.currentThread();

			incrementMenuResetSuppression();
			attemptingToQuit = true;
			if (!closeAllProjects()){
				decrementMenuResetSuppression();
				//CommandRecord.checkThread = true;
				System.out.println("Quit cancelled");
				attemptingToQuit = false;
				MesquiteTrunk.startupShutdownThread = null;
				return null;
			}
			if (debugMode){
				/**/
				logln("==vvvvvvvvvvvvvvvvvvvv==");
				logln("The following is used to to check efficiencies.");
				logln("Associable.totalPartsAdded " + Associable.totalPartsAdded);
				logln("ListableVector.totalElementsAdded " + Associable.totalPartsAdded);
				logln("MesquiteWindow.totalCheckDoomedCount " + MesquiteWindow.totalCheckDoomedCount);
				logln("mesquite.minimal.BasicFileCoordinator.BasicFileCoordinator.totalProjectPanelRefreshes " + mesquite.minimal.BasicFileCoordinator.BasicFileCoordinator.totalProjectPanelRefreshes);
				logln("--Classes painted\n" + MesquiteWindow.componentsPainted.recordsToString());
				logln("Listened.notifications " + Listened.notifications);
				logln("--Classes notifying\n" + Listened.classes.recordsToString());
				logln("--Classes notified\n" + Listened.classesNotified.recordsToString());
				logln("MesquiteProject.totalFinalized " +  MesquiteProject.totalFinalized + " totalDisposed " + MesquiteProject.totalDisposed + " totalCreated " +  MesquiteProject.totalCreated);
				logln("MesquiteFile.totalFinalized " +  MesquiteFile.totalFinalized + " totalDisposed " + MesquiteFile.totalDisposed + " totalCreated " +  MesquiteFile.totalCreated);
				logln("ProjectRead.totalFinalized " +  ProjectRead.totalFinalized + " totalCreated " +  ProjectRead.totalCreated);
				logln("FileElement.totalFinalized " +  FileElement.totalFinalized + " totalDisposed " + FileElement.totalDisposed + " totalCreated " +  FileElement.totalCreated+ " totalTrueFileElementCreated " +  FileElement.totalTrueFileElementCreated);
				logln("MesquiteFrame.totalFinalized " +  MesquiteFrame.totalFinalized + " totalDisposed " + MesquiteFrame.totalDisposed + " totalCreated " +  MesquiteFrame.totalCreated);
				logln("MesquiteWindow.totalFinalized " +  MesquiteWindow.totalFinalized + " totalDisposed " + MesquiteWindow.totalDisposed + " numDisposing " +  MesquiteWindow.numDisposing + " totalCreated " +  MesquiteWindow.totalCreated);
				logln("MesquiteDialogParent.totalFinalized " +  MesquiteDialogParent.totalFinalized + " totalDisposed " + MesquiteDialogParent.totalDisposed + " totalCreated " +  MesquiteDialogParent.totalCreated);
				logln("ListableVector.totalFinalized " +  ListableVector.totalFinalized + " totalDisposed " + ListableVector.totalDisposed + " totalCreated " +  ListableVector.totalCreated);
				logln("MesquiteMenuBar.totalFinalized " +  MesquiteMenuBar.totalFinalized +  " totalCreated " +  MesquiteMenuBar.totalCreated);
				logln("MesquiteMenuItem.totalFinalized " +  MesquiteMenuItem.totalFinalized + " totalDisposed " + MesquiteMenuItem.totalDisposed + " totalCreated " +  MesquiteMenuItem.totalCreated);
				logln("MesquiteMenuItemSpec.totalFinalized " +  MesquiteMenuItemSpec.totalFinalized + " totalDisposed " + MesquiteMenuItemSpec.totalDisposed + " totalCreated " +  MesquiteMenuItemSpec.totalCreated);
				logln("NexusBlock.totalFinalized " +  NexusBlock.totalFinalized + " totalDisposed " + NexusBlock.totalDisposed + " totalCreated " +  NexusBlock.totalCreated);
				logln("MesquiteModule.totalFinalized " +  MesquiteModule.totalFinalized + " totalDisposed " + MesquiteModule.totalDisposed + " totalCreated " +  MesquiteModule.totalCreated);
				if (MesquiteTrunk.checkMemory){
					logln("==vvvvvvvvvvvvvvvvvvvv==");
					logln("The following is used to trace memory leaks");
					reportMemory();
					logln("==^^^^^^^^^^^^^^^^^^^^==");
				}

			}
			logln("\n");
			logln("All modules that have been started at least once, and the number of times they have been started:");
			logln(getNumModuleStarts());
			/*
				if (Listened.listenersRemaining>0) {
					for (int i = 0; i<Listened.allListeners.size(); i++){
						Object r = Listened.allListeners.elementAt(i);
						MesquiteMessage.println("listener remaining " + r);
					}

				}
			 */
			//	logln("Mesquite is quitting with some listeners remaining.  This is a symptom that some objects were not properly disposed, and it may relate to inflation of memory use.  Please report this problem.");
			/**/


			/*
				logln("total time to startup modules "  + EmployerEmployee.startupTime.getAccumulatedTime() );
				logln("total time to make window "  + MesquiteWindow.startingTime.getAccumulatedTime() );
				logln("total time to show window "  + MesquiteWindow.windowShowTime.getAccumulatedTime() );
				logln("total time to compose menus "  + MenuOwner.menuCompositionTime.getAccumulatedTime() );
				logln("total time to reset menus "  + MesquiteWindow.resetMenuTime.getAccumulatedTime() );
				logln("total time to make tables  "  + MesquiteTable.tableTime.getAccumulatedTime() );
				logln("Modules: total started: " + EmployerEmployee.totalCreated + "; total ended: " + EmployerEmployee.totalDisposed);
				logln("Trees: total created: " + MesquiteTree.totalCreated + "; total disposed: " + MesquiteTree.totalDisposed);
			 */
			doomed = true;
			showAbout = (about!= null && !about.isShowing());
			storePreferences();
			logln("Main Mesquite module quitting  " );

			iQuit();
			if (debugMode)
				logln("disposing remaining windows  " );
			while (windowVector.size()>0) {
				Enumeration e = windowVector.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					MesquiteWindow mw = (MesquiteWindow)obj;
					mw.setVisible(false);
					mw.dispose();
				}
			}
			if (debugMode)
				logln("exiting " );
			MesquiteFile.closeLog();
			mesquiteExiting = true;
			if (!startedAsLibrary)
				System.exit(0);
		}
		else if (checker.compare(this.getClass(), "Force Quit Mesquite (data may be lost) via click go-away", null, commandName, "goAwayForceQuit")) {
			incrementMenuResetSuppression();
			doomed = true;
			while (windowVector.size()>0) {
				Enumeration e = windowVector.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					MesquiteWindow mw = (MesquiteWindow)obj;
					mw.setVisible(false);
					mw.dispose();
				}
			}
			showAbout = false;
			System.exit(0);
		}
		else if (checker.compare(this.getClass(), "Force Quit Mesquite (data may be lost)", null, commandName, "forceQuit")) {
			System.exit(0);
		}
		else if (checker.compare(this.getClass(), "Closes down all threads and modules, but doesn't stop JVM", null, commandName, "vanish")) {
			mesquiteExiting = true;
			doomed = true;
			iQuit();
			while (windowVector.size()>0) {
				Enumeration e = windowVector.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					MesquiteWindow mw = (MesquiteWindow)obj;
					mw.setVisible(false);
					mw.dispose();
				}
			}
			MesquiteFile.closeLog();		
		}
		else if (checker.compare(this.getClass(), "Brings all windows to front", null, commandName, "showAllWindows")) {
			Enumeration e = windowVector.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteWindow mw = (MesquiteWindow)obj;
				MesquiteFrame mf = mw.getParentFrame();
				if (mf.isVisible()) {
					mf.setVisible(true);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Closes all current projects and files", null, commandName, "closeAllProjects")) {
			closeAllProjects();
		}
		else if (checker.compare(this.getClass(), "Saves all current projects and files", null, commandName, "saveAllProjects")) {
			int numFiles = 0;
			Projects projs = getProjectList();
			for (int i= 0; i<projs.getNumProjects(); i++) {
				numFiles += projs.getProject(i).getNumberLinkedFiles();
			}
			String s= "Do you want to save all of the open files?  There are currently " + numFiles + " files open";
			if (numFiles == getProjectList().getNumProjects())
				s += ".";
			else
				s += " in " + getProjectList().getNumProjects() + " separate projects." ;
			if (MesquiteThread.isScripting() || AlertDialog.query(containerOfModule(), "Save all?", s, "Save", "Cancel", 1))
				saveAllProjects();
		}
		else if (checker.compare(this.getClass(), "Performs keyword search to find module with particular function.", null, commandName, "keywordSearch")) {
			//system could log employers used for various modules to give examples of use
			String s = parser.getFirstToken(arguments);
			if (StringUtil.blank(s) && !MesquiteThread.isScripting())
				s = MesquiteString.queryString(containerOfModule(), "Keyword", "Enter a search string.  Mesquite will search through module information for a match (currently, the string as a whole must match).", "");
			if (s!=null) {
				searchKeyword(s, true);
			}
		}
		/*New code added Feb.05.07 oliver*/ //TODO: Delete new code comments
		else if (checker.compare(this.getClass(), "Searches the internet for user-provided terms.", null, commandName, "webSearch")) {
			String queryTerm = "";
			MesquiteBoolean addMesquiteToSearch = new MesquiteBoolean(true);
			MesquiteBoolean restrictSearchToManual = new MesquiteBoolean(true);
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog webQueryDialog = new ExtensibleDialog(containerOfModule(), "Web Search", buttonPressed);
			webQueryDialog.addLargeOrSmallTextLabel("Enter a search string. Mesquite will open a web browser and search for the terms using the Google search engine.");
			TextArea queryText = webQueryDialog.addTextArea("", 4);
			Checkbox addMesquite = webQueryDialog.addCheckBox("Include 'Mesquite' in search terms?", addMesquiteToSearch.getValue());
			Checkbox restrictToManual = webQueryDialog.addCheckBox("Restrict search to Mesquite's online manual?", restrictSearchToManual.getValue());
			webQueryDialog.appendToHelpString("The terms entered will be searched for on your web browser, using the Google search engine.  Use single or double quotes to group terms in your search.");
			webQueryDialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				queryTerm = queryText.getText();
				addMesquiteToSearch.setValue(addMesquite.getState());
				restrictSearchToManual.setValue(restrictToManual.getState());
			}
			if (queryTerm!=null && buttonPressed.getValue()==0){
				searchWeb(queryTerm, addMesquiteToSearch, restrictSearchToManual);
			}
			webQueryDialog.dispose();
		}
		/*End new code added Feb.05.07 oliver*/
		/*New code added April.04.07 oliver*/ //TODO: Delete new code comments
		else if (checker.compare(this.getClass(), "Opens the Mesquite FAQ in a web browser", null, commandName, "mesquiteFAQ")){
			openMesquiteFAQ();
		}
		/*End new code added April.04.07 oliver*/
		else if (checker.compare(this.getClass(), "Shows web page of installed and loaded modules", null, commandName, "showModules")) {
			if (MesquiteTrunk.isApplet()) 
				return null;
			if (!CommandChecker.documentationComposed) {
				CommandChecker cchecker = new CommandChecker();
				cchecker.composeDocumentation();
			}	
			String moduleListPath= MesquiteModule.prefsDirectory + MesquiteFile.fileSeparator + "modules.html";
			File testing = new File(moduleListPath);
			if (testing.exists()) {
				showWebPage(moduleListPath, true);
			}
		}
		else if (checker.compare(this.getClass(), "Shows web page introducing a package of installed modules", null, commandName, "showPackageInfo")) {
			if (MesquiteTrunk.isApplet()) 
				return null;
			String packageURL= parser.getFirstToken(arguments); //MesquiteModule.prefsDirectory + MesquiteFile.fileSeparator + "modules.html";
			if (StringUtil.notEmpty(packageURL)) {
				if (packageURL.startsWith("http"))
					showWebPage(packageURL,false);
				else {
					File testing = new File(packageURL);
					if (testing.exists()) {
						showWebPage(packageURL, false);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Shows web page of available commands for scripting", null, commandName, "showCommands")) {
			if (MesquiteTrunk.isApplet()) 
				return null;
			if (!CommandChecker.documentationComposed) {
				CommandChecker cchecker = new CommandChecker();
				cchecker.composeDocumentation();
			}	
			String commandListPath= MesquiteModule.prefsDirectory + MesquiteFile.fileSeparator + "commands.html";
			File testing = new File(commandListPath);
			if (testing.exists()) {
				showWebPage(commandListPath, true);
			}
		}
		else if (checker.compare(this.getClass(), "Shows home page of Mesquite", null, commandName, "showHomePage")) {
			if (MesquiteTrunk.isApplet()) 
				return null;

			String manualPath = mesquiteHomePageURL; 
			//File testing = new File(manualPath);
			showWebPage(manualPath, false);

		}
		else if (checker.compare(this.getClass(), "Shows Support page of Mesquite manual", null, commandName, "showSupport")) {
			if (MesquiteTrunk.isApplet()) 
				return null;

			String manualPath = mesquiteDocsURL + "Getting Help.html" ;
			showWebPage(manualPath, false);
		}
		else if (checker.compare(this.getClass(), "Sets whether the Mesquite window appears automatically when no windows are showing", "[on or off]", commandName, "windowAutoShow")){
			MesquiteWindow.autoShow = MesquiteBoolean.fromOffOnString(ParseUtil.getFirstToken(arguments, stringPos));
		}
		else if (checker.compare(this.getClass(), "Returns string (\"on\", \"off\") whether the Mesquite window appears automatically when no windows are showing", null, commandName, "getWindowAutoShow")){
			return MesquiteBoolean.toOffOnString(MesquiteWindow.autoShow);
		}
		else if (checker.compare(this.getClass(), "Dumps the entire employee tree to the log", null, commandName, "dumpEmployeeTree")) {
			showLogWindow();
			logln("======= Module Employee Tree =======\n" + listEmployees("  ") + "\n================================");
		}
		else if (checker.compare(this.getClass(), "Shows some statistics that might be of interest to developers", null, commandName, "showStatistics")) {
			showLogWindow();
			logln("Number of modules installed " + MesquiteTrunk.mesquiteModulesInfoVector.size());
			dumpFileList(getRootPath() + "mesquite", "mesquite", " ");
			logln("MesquiteWindow.totalCheckDoomedCount " + MesquiteWindow.totalCheckDoomedCount);
			logln("--Classes painted\n" + MesquiteWindow.componentsPainted.recordsToString());
			logln("Listened.notifications " + Listened.notifications);
			logln("--Classes notifying\n" + Listened.classes.recordsToString());
			logln("--Classes notified\n" + Listened.classesNotified.recordsToString());
		}
		else if (checker.compare(this.getClass(), "Dumps to the log a list of those modules that are substantive and (prerelease or without citations)", null, commandName, "dumpSubstantivePrerelease")) {
			showLogWindow();
			logln("\nModules that are marked as substantive and prerelease:\n");
			int count = 0;
			if (isPrerelease()) {
				count++;
				logln(getClass().getName());
			}
			for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
				if (mmi.isSubstantive() && mmi.isPrerelease()) {
					logln(mmi.getClassName());
					count++;
				}
			}
			if (count == 0)
				logln("   There are no modules marked as substantive and prerelease.\n");
			else
				logln("\n[number of substantive and prerelease: " + count + "]\n");
			if (false){ //these are useful to list to find those who should be asking to have their citations shown!
				logln("======= Substantive Modules that don't ask to have their citations shown =======");
				count = 0;
				for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
					MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
					if (mmi.isSubstantive() && !mmi.showCitation()) {
						logln(mmi.getClassName());
						count++;
					}
				}
				logln("============ number substantive but without citation " + count + " ==============");
			}
		}	
		else if (checker.compare(this.getClass(), "Dumps to the log a list of those modules that are prerelease", null, commandName, "dumpAllPrerelease")) {
			showLogWindow();
			logln("\nModules that are marked as prerelease:\n");
			int count = 0;
			if (isPrerelease()) {
				count++;
				logln(getClass().getName());
			}
			for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
				if (mmi.isPrerelease()) {
					logln(mmi.getClassName());
					count++;
				}
			}
			if (count == 0)
				logln("   There are no modules marked as prerelease.\n");
			else
				logln("\n[number of prerelease: " + count + "]\n");
		}	
		else if (checker.compare(this.getClass(), "Dumps to the log a list of modules, their names and explanations, and their needs", null, commandName, "dumpEN")) {
			showLogWindow();
			logln("\nModules and their informations:\n");
			for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
				String s = "<li><strong>" + mmi.getName() + ":</strong>&nbsp;&nbsp;" + mmi.getExplanation() + " &nbsp;&nbsp;<font color = AAAAAA> " +mmi.getClassName() + "</font>";
				if (mmi.hasNeeds()){
					Vector v = mmi.getEmployeeNeedsVector();
					s += "<ul>";
					for (int k = 0; k<v.size(); k++){
						EmployeeNeed need = (EmployeeNeed)v.elementAt(k);
						s += "<li>" + need.getDutyClass().getName() + "</li>";
					}
					s += "</ul>";
				}
				s += "</li>";
				logln(s);
			}

		}	
		else if (checker.compare(this.getClass(), "Dumps to the log a list of those modules that are new to this version", null, commandName, "dumpNewModules")) {
			showLogWindow();
			logln("\n=======================================\nModules that are new to this release of Mesquite:\n");
			int count = 0;
			for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
				if (mmi.getVersionOfFirstRelease()>= getVersionInt()) {
					logln(mmi.getName());
					logln("\t" + mmi.getExplanation());
					logln("\t  ["+mmi.getClassName()+"]");
					logln("");
					count++;
				}
			}
			if (count == 0)
				logln("   There are no new modules.\n");
			else
				logln("\n[number of new modules: " + count + "]\n");
		}	
		else if (checker.compare(this.getClass(), "Dumps to the log a list of those modules that are new to the next release version", null, commandName, "dumpNextReleaseModules")) {
			showLogWindow();
			logln("\n=======================================\nModules that will be new in the next release of Mesquite:\n");
			int count = 0;
			for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
				if (mmi.getVersionOfFirstRelease()== NEXTRELEASE) {
					logln(mmi.getName());
					logln("\t" + mmi.getExplanation());
					logln("\t  ["+mmi.getClassName()+"]");
					logln("");
					count++;
				}
			}
			if (count == 0)
				logln("   There will be no new modules.\n");
			else
				logln("\n[number of new modules in next release: " + count + "]\n");
		}	
		else if (checker.compare(this.getClass(), "Dumps to the log a list of all modules installed", null, commandName, "dumpAllModules")) {
			showLogWindow();
			logln("\n=======================================\nModules installed:\n");
			int count = 0;
			for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
				String vers = mmi.getVersion();
				if (vers == null)
					vers = "";
				else
					vers = " (" + mmi.getPackageName() + ", version " + vers + ")";
				logln(mmi.getName() + "\t" + mmi.getExplanation() + vers);
				count++;
			}
			logln("Modules installed: " + count);

		}	
		else if (checker.compare(this.getClass(), "Shows a web page of all modules installed", null, commandName, "showWebPageAllModules")) {
			composePageOfModules();
		}	
		else if (checker.compare(this.getClass(), "Dumps to the log a list of all modules installed", null, commandName, "dumpAllModuleClasses")) {
			showLogWindow();
			logln("\n=======================================\nModules installed:\n");
			int count = 0;
			for (int i= 0; i<mesquiteModulesInfoVector.size(); i++){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)mesquiteModulesInfoVector.elementAt(i);
				logln(mmi.getClassName());
				count++;
			}

		}	
		else if (checker.compare(this.getClass(), "Sends Error to Server", null, commandName, "testError")) {
			reportableAlert("STRING:ERROR", "DETAILS:TESTING ERROR REPORTING");
		}
		else if (checker.compare(this.getClass(), "Causes intentional crash", null, commandName, "pleaseCrash")) {
			String s = null;
			fakeCrashCount++;
			System.err.println("###PleaseCrash### " + fakeCrashCount);
			s.substring(0, 2);
		}
		else if (checker.compare(this.getClass(), "Shows useless dialog", null, commandName, "fakeDialog")) {
			AlertDialog.query(containerOfModule(), "Hello", "Hi!", "OK", "OK");
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
int fakeCrashCount = 0;
	/*.................................................................................................................*/
	private void dumpFileList(String path, String name, String spacer){
		File f = new File(path);  //
		if (!f.exists())
			return;
		if (name != null){
			if (f.isDirectory())
				MesquiteMessage.println(spacer + name + "--------------"); 
			else if (name.indexOf(".class")>=0)
				MesquiteMessage.println(spacer + name + "    " + (new Date(f.lastModified()))); //class files only
		}

		if (f.isDirectory()){  // is a directory; hence look inside at each item
			String[] filesList = f.list();
			for (int i=0; i<filesList.length; i++){
				dumpFileList(path+ MesquiteFile.fileSeparator + filesList[i], filesList[i], spacer + "   ");
			}
		}
	}
	/*.................................................................................................................*/
	public static void prepareMesquite(){
		windowVector = new ListableVector();
		dialogVector = new ListableVector();
		fileMenu=new MesquiteMenuSpec(null, "File", mesquiteTrunk, true);
		fileMenu.setFilterable(false);
		editMenu=new MesquiteMenuSpec(null, "Edit", mesquiteTrunk, true);
		windowsMenu=new MesquiteMenuSpec(null, "View", mesquiteTrunk, true);
		charactersMenu = new MesquiteMenuSpec(null, "Characters", mesquiteTrunk, true);
		treesMenu = new MesquiteMenuSpec(null, "Taxa&Trees", mesquiteTrunk, true);
		analysisMenu = new MesquiteMenuSpec(null, "Analysis", mesquiteTrunk, true);
		helpMenu = new MesquiteMenuSpec(null, "Help", mesquiteTrunk, true);
		helpMenu.setFilterable(false);
		projects = new Projects();
		mesquiteTrunk.newFileCommand = makeCommand("newProject",  mesquiteTrunk);
		mesquiteTrunk.openFileCommand = makeCommand("openFile",  mesquiteTrunk);
		mesquiteTrunk.openRecentCommand = makeCommand("openRecent",  mesquiteTrunk);
		mesquiteTrunk.clearRecentCommand = makeCommand("clearRecent",  mesquiteTrunk);
		mesquiteTrunk.openURLCommand = makeCommand("openURL",  mesquiteTrunk);
		//mesquiteTrunk.currentCommandCommand = makeCommand("currentCommand",  mesquiteTrunk);
		//mesquiteTrunk.currentCommandCommand.setQueueBypass(true);
		mesquiteTrunk.pendingCommandsCommand = makeCommand("pendingCommands",  mesquiteTrunk);
		mesquiteTrunk.pendingCommandsCommand.setQueueBypass(true);
		mesquiteTrunk.resetMenusCommand = makeCommand("resetMenus",  mesquiteTrunk);
		if (!MesquiteTrunk.developmentMode)
			mesquiteTrunk.resetMenusCommand.setSuppressLogging(true);
		mesquiteTrunk.showLicenseCommand = makeCommand("showLicense",  mesquiteTrunk);
		mesquiteTrunk.quitCommand = makeCommand("quit",  mesquiteTrunk);
		mesquiteTrunk.quitCommand.setQueueBypass(true);
		mesquiteTrunk.quitCommand.setOKOnOtherThread(true);
		mesquiteTrunk.forceQuitCommand = makeCommand("forceQuit",  mesquiteTrunk);
		mesquiteTrunk.forceQuitCommand.setQueueBypass(!MesquiteTrunk.isLinux());
		mesquiteTrunk.forceQuitCommand.setOKOnOtherThread(true);
		mesquiteTrunk.showAllCommand = makeCommand("showAllWindows", mesquiteTrunk);
		mesquiteTrunk.closeAllCommand = makeCommand("closeAllProjects", mesquiteTrunk);
		mesquiteTrunk.saveAllCommand = makeCommand("saveAllProjects", mesquiteTrunk);

		about =new AboutWindow(mesquiteTrunk);
		about.setResizable(false);
		//about.setWindowSize(AboutWindow.totalWidth, AboutWindow.aboutHeight); 
		about.setQuitIfGoAway(true);

		MesquiteWindow.centerWindow(about);

		/*EMBEDDED	if embedded delete the following    */
		//about.addToWindow(mesquiteTrunk);
		/**/
		CommandChecker.registerClass(Mesquite.class, MesquiteModule.class);

		CommandChecker.registerClass(AboutWindow.class, MesquiteWindow.class);
		CommandChecker.registerClass(MesquitePanel.class, MesquitePanel.class);

		CommandChecker.registerClass(System.class, System.class);
	}

	String earlyWarning = "";
	/*
	private void registerMacHandlers(){
		if (!MesquiteWindow.GUIavailable)
			return;
		else if (MesquiteTrunk.isMacOSX()) {
			try {
				fileHandler = new EAWTHandler(this);
				((EAWTHandler)fileHandler).register();

			} catch (NoSuchMethodError e) {
				String w = "File handling failed to register with macOS (NSME).";
				earlyWarning += w;
				System.out.println(w);
			}
			catch (NoClassDefFoundError e) {
				String w = "File handling failed to register with macOS (NCME).";
				earlyWarning += w;
				System.out.println(w);
			}
			catch (IllegalAccessError e) {
				String w = "File handling failed to register with macOS (IAE).";
				earlyWarning += w;
				System.out.println(w);
			}
			catch (Throwable e) {
			}
		}
	}
	 */


	public Mesquite(){
		super();
	}

	/* separated as a constructor in v. 2. 01 in preparation for Mesquite being instantiated other than as standalone application */
	public Mesquite(String args[]){
		super();
		boolean outputVersion = false;
		boolean otherArgumentFound = false;
		if (args !=null){
			startupArguments = new String[args.length];
			for (int i=0; i<args.length; i++){
				startupArguments[i] = args[i];
				if (args[i]!=null){
					if (args[i].equals("-w"))
						MesquiteWindow.suppressAllWindows = true;
					else if (args[i].equals("-b"))
						MesquiteTrunk.consoleListenSuppressed = true;
					else if (args[i].equals("-nv"))
						MesquiteTrunk.suppressVersionReporting = true;
					else if (args[i].equals("-ne"))
						MesquiteTrunk.suppressErrorReporting = true;
					else if (args[i].equals("-nb"))
						MesquiteTrunk.noBeans = true;
					else if (args[i].equals("-mqflex2"))
						MesquiteTrunk.startedFromFlex2 = true;
					else if (args[i].equals("-mqex"))
						MesquiteTrunk.startedFromExecutable = true;
					else if (args[i].equals("-development")) {
						MesquiteTrunk.developmentMode = true;
					}
					else if (args[i].equals("-mqcat"))
						; //started from Catalina executable
					else if (args[i].equals("-mq17")) {
						MesquiteMessage.warnUser("This executable is not compatible with current Mesquite");
					}
					else if (args[i].equals("-debug"))
						MesquiteTrunk.debugMode = true;
					else if (args[i].equals("--version"))
						outputVersion = true;
					else
						otherArgumentFound = true;
				}
			}
		}
		if (outputVersion){
			Writer stream;
			try {
				stream = new OutputStreamWriter(new FileOutputStream("MesquiteVersion.txt"));
				stream.write(getVersion() + "\t" + getBuildNumber());
				stream.flush();
				stream.close();
			}
			catch( Throwable e ) {
			}

			System.out.println(getVersion() + "\t" + getBuildNumber());
			if (!otherArgumentFound){
				if (!startedAsLibrary)
					System.exit(0);
				return;
			}
		}
		if (MesquiteTrunk.debugMode)
			System.out.println("main constructor 1");
		MesquiteWindow.componentsPainted = new ClassVector();//for detecting efficiency problems
		Listened.classes = new ClassVector();//for detecting efficiency problems
		Listened.classesNotified = new ClassVector();//for detecting efficiency problems
		if (MesquiteWindow.GUIavailable){
			try { //code testing for GUI from Kevin Herrboldt via java-dev@lists.apple.com
				Frame foo = new Frame();

				if (foo==null)
					MesquiteWindow.GUIavailable = false;// no GUI available, need to be non-gui
				else {
					foo.pack();
					foo.getToolkit();       // will throw exception if unable to open GUI
				}
			} catch (Throwable t) {
				MesquiteWindow.GUIavailable = false;// no GUI available, need to be non-gui
			}
		}
		if (MesquiteTrunk.debugMode)
			System.out.println("main constructor 2");
		try {
			if (MesquiteTrunk.debugMode)
				System.out.println("main constructor 3");
			MesquiteTrunk.startupShutdownThread = Thread.currentThread();
			incrementMenuResetSuppression();
			isApplication = true;

			// create a new instance of this applet 
			mesquiteTrunk = this;
			Mesquite mesq = (Mesquite)mesquiteTrunk;  //for easy of reference below
			if (MesquiteTrunk.isWindows()){
				textEdgeCompensationHeight = 7; //6 on mac; 7 on pc
				textEdgeCompensationWidth = 22; //12 on mac; 28 on pc
			}

			/*/if JDK≥9 register quit handler */
			if (getJavaVersionAsDouble()>=1.9){ //if before Java 9.0 or before then add to the system class loader in the old fashioned way
				applicationHandler9 = new ApplicationHandler9(this);
			}
			/*	else
				mesq.registerMacHandlers();  //no longer useful unless old Java & OS
			 */


			if (MesquiteTrunk.debugMode)
				System.out.println("main constructor 4");
			MainThread.mainThread = new MainThread();
			MainThread.mainThread.start();
			if (MesquiteTrunk.debugMode)
				System.out.println("main constructor 5");
			prepareMesquite();
			if (MesquiteTrunk.debugMode)
				System.out.println("main constructor 6");

			// initialize the applet
			mesquiteTrunk.init();
			//EMBEDDED: include this  
			((Mesquite)mesquiteTrunk).start(); 
			MesquiteCommand.nullCommand = new MesquiteCommand("null", MesquiteTrunk.mesquiteTrunk);
			MesquiteCommand.nullCommand.setSuppressLogging(true);
			if (MesquiteTrunk.debugMode)
				System.out.println("main constructor 7");
			// open the files requested at startup
			if (args != null && args.length>0) {
				// report arguments
				String s = "Arguments: ";
				for ( int i = 0; i < args.length; i++ ) {
					if (!MesquiteTrunk.startedFromFlex2 || (args[i]!=null && args[i].startsWith("-"))){
						if (!args[i].equalsIgnoreCase("-null"))
							s += " [ " + args[i] + " ]";
					}
				}
				MesquiteTrunk.mesquiteTrunk.logln(s);
				if (MesquiteTrunk.developmentMode)
					MesquiteTrunk.mesquiteTrunk.logln("Development mode enabled.");
				// get the total memory for my app

				for ( int i = 0; i < args.length; i++ ) {
					if (args[i]!=null && !args[i].startsWith("-")) {
						if (MesquiteTrunk.startedFromFlex2) { //the argument is not a file to open, but rather the encapsulatedpath of the app on macOS to find the list of files to open
							if (MesquiteTrunk.debugMode)
								MesquiteTrunk.mesquiteTrunk.logln("Encapsulated path to me: "+ args[i]);

							MesquiteTrunk.encapsulatedPathOfExecutable= args[i];
							//record the encapsulated path to the filesToOpen so that ConsoleThread can find any list of files
						}
						else
							mesquiteTrunk.openFile(args[i]);
					}
				}
			}

			if (MesquiteTrunk.debugMode)
				System.out.println("main constructor 8");
			mesquiteTrunk.resetAllMenuBars();
			MesquiteTrunk.startupShutdownThread = null;
			decrementMenuResetSuppression();
		}
		catch (Exception e){
			if (!Mesquite.mesquiteExiting){
				MesquiteFile.throwableToLog(this, e);
				exceptionAlert(e, "Exception caught in Mesquite");
			}
		}
		catch (Error e){
			if (!Mesquite.mesquiteExiting){
				MesquiteFile.throwableToLog(this, e);
				exceptionAlert(e, "Error caught in Mesquite");
			}

		}
		if (MesquiteTrunk.debugMode)
			System.out.println("main constructor 6");
		if (about !=null && mesquiteTrunk.getProjectList()!=null && mesquiteTrunk.getProjectList().getNumProjects()>0 && defaultHideMesquiteWindow)
			about.hide();


	}

	/*.................................................................................................................*
	public boolean externalStart(String args[]){
		MesquiteWindow.GUIavailable = true;
		mesquiteTrunk = this;
		OMStartupThread t = new OMStartupThread(args);
		t.start();
		return true;
	}
	 */ 

	/*.................................................................................................................*/
	//DavidCheck: the following 3 methods were all changed
	static void collectAllJars(String path, Vector urls, Vector jars){
		File dir = new File(path);
		if (dir.exists() && dir.isDirectory()){
			String[] subs = dir.list();
			if (subs != null && subs.length>0){
				for (int i = 0; i<subs.length; i++){
					if (subs[i].equals("jars")){
						String jarsPath = MesquiteFile.composePath(path, "jars");
						addToStartupNotices("    Registering jars in <" + jarsPath + ">");
						File jarsDirectory = new File(jarsPath);
						if (jarsDirectory.exists() && jarsDirectory.isDirectory()){
							String[] jarsList = jarsDirectory.list();
							if (jarsList != null && jarsList.length > 0){
								for (int k = 0; k<jarsList.length; k++){
									String absPath = MesquiteFile.composePath(jarsPath, jarsList[k]);
									jars.addElement(absPath);
									File d = new File(absPath);
									try {
										urls.addElement(d.toURL());
									} catch (MalformedURLException e) {
										e.printStackTrace();
									}
								}
							}
						}

					}
					else
						collectAllJars(MesquiteFile.composePath(path, subs[i]), urls, jars);
				}
			}
		}
	}
	static void addToStartupNotices(String s){
		if (startupNotices == null && starter != null){ // because of Java 9 classloading issues, rely on starter class to make class loader if it exists
			try {
				Method gsn = starter.getClass().getDeclaredMethod("getStartupNotices", null);
				startupNotices = (Vector)gsn.invoke(starter, null);
			} catch (Exception e) {
				System.out.println("Failed to get startup notices vector");
			}
		}
		if (startupNotices != null)
			startupNotices.addElement(s);
		System.out.println(s);
	}
	static void addClasspathsHere(Vector urls, Vector jars, String absPath){
		addToStartupNotices("    Registering path to <" + absPath + ">");
		File d = new File(absPath);
		//Add the jars in the package to the classpath
		collectAllJars(absPath, urls, jars);
		//Add the package directory to the classpath
		try {
			urls.addElement(d.toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	/*.................................................................................................................*/
	public static String correctClassPath(String path) {
		if (MesquiteTrunk.isWindows()) {
			return path.replace("/", "\\");
		} else  {
			return path.replace("\\", "/");
		}
	}

	static Vector startupNotices = null;
	/*.................................................................................................................*/
	/*Dec 2O17: to deal with shift in Java 9 that system class loader is no longer a URL class loader, we need to make our own for module loader, that adds paths to modules*/
	public static URLClassLoader makeModuleClassLoader(String mesquiteDirectoryPath, URLClassLoader classLoader, Vector v){
		startupNotices = v;
		if (MesquiteTrunk.debugMode)
			System.out.println("$$$ URLClassLoader.class in makeModuleClassLoader " + URLClassLoader.class); 
		try {

			Vector urls = new Vector(); //A vector to hold all the URLs of classpaths
			Vector jars = new Vector(); //A vector to hold all the paths to jar files
			File mesquiteDirectory = new File(mesquiteDirectoryPath);		
			URL u =null;
			addToStartupNotices("Setting up class loader for <" + mesquiteDirectory + ">");
			//Add the basic Mesquite_Folder to the classpath
			urls.addElement(mesquiteDirectory.toURL());
			//Accumulate all jars in Mesquite_Folder to classpath
			collectAllJars(mesquiteDirectoryPath, urls, jars);
			String classpathstxt = MesquiteFile.composePath(mesquiteDirectoryPath , MesquiteModule.classpathsFileName);
			String[] paths = null;
			if (MesquiteFile.fileExists(classpathstxt))
				paths = MesquiteFile.getFileContentsAsStringsForStarter(classpathstxt);
			if (paths != null){
				//Go through each package listed in classpaths.txt
				for (int i = 0; i<paths.length; i++){
					if (!paths[i].startsWith("#")) { //paths can be commented out with leading #

						addClasspathsHere(urls, jars, MesquiteFile.composePath(mesquiteDirectoryPath, correctClassPath(paths[i])));
					}
				}
			}
			//Adding stuff from Mesquite_Folder/additionalMesquiteModules
			addClasspathsHere(urls, jars, MesquiteFile.composePath(mesquiteDirectoryPath, "additionalMesquiteModules"));

			//Adding stuff from Mesquite_Support_Files/classes
			addClasspathsHere(urls, jars, System.getProperty("user.home") + System.getProperty("file.separator") + "Mesquite_Support_Files" + System.getProperty("file.separator") + "classes");


			if (getJavaVersionAsDouble()<1.9){ //if before Java 9.0 or before then add to the system class loader in the old fashioned way
				addToStartupNotices(" Java version is before 9.0; using system class loader for modules and jars.");
				URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
				for (int i = 0; i<urls.size(); i++){
					JarLoader.addURL((URL)urls.elementAt(i));
				}
				return sysloader;
			}
			//Java 9.0 or above. Add all URLs to a URLClassLoader (for modules at least) but then also add jars  manually with ByteBuddy, just in case.
			if (classLoader == null  || MesquiteTrunk.isMacOSX()  || MesquiteTrunk.isLinux()) { 
				addToStartupNotices(" Java version is 9.0 or later; making new URLClassLoader for modules.");
				//If no class loader was supplied, make one and give it the URLs for classpaths
				//(the drawback of this is that as a new class loader, it may not be used for mesquite.Mesquite)
				URL[] us= new URL[urls.size()];
				for (int i = 0; i<urls.size(); i++){
					us[i] = (URL)urls.elementAt(i);
				}
				classLoader = new URLClassLoader(us);
			}
			else {
				addToStartupNotices(" Java version is 9.0 or later; using URLClassLoader supplied by start.Mesquite and adding to it.");
				//if  URLClassLoader is presented, use that and add to it, bypassing the protected status of addURL by using reflection
				// NOTE: this generates a warning, and should be eventually eliminated when we can figure out how. It's currently needed when run on Windows
				/* Here is the warning:
	WARNING: An illegal reflective access operation has occurred
	WARNING: Illegal reflective access by mesquite.Mesquite (file:/Users/david/Documents/Mesquite Workspace/MesquiteCore/Mesquite_Folder/) to method java.net.URLClassLoader.addURL(java.net.URL)
	WARNING: Please consider reporting this to the maintainers of mesquite.Mesquite
	WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
	WARNING: All illegal access operations will be denied in a future release
				 */
				Method method = classLoader.getClass().getDeclaredMethod("addURL",new Class[]{URL.class});
				method.setAccessible(true);
				for (int i = 0; i<urls.size(); i++){
					method.invoke(classLoader,new Object[]{ (URL)urls.elementAt(i) });
				}
			}
			/*The above will generate a classloader that can load all the modules, since they are loaded explicitly with this class loader by ModuleLoader. 
			 * However, the jars may or may not be loaded, because they aren't loaded manually by Mesquite, but rather automatically by the current class loader when needed. 
			 * If the current classloader isn't the URL one made here, then jars loading can fail. The one known case as of
			 * January 2O18 is when run under Eclipse and Java 1.9. Eclipse supplies its own classloader; it can find the core jars as they are in the project's build path, but it can't
			 * find the jars of packages added via classpaths.txt. For this reason we now use ByteBuddy to add the jars to the system classpath.*/

			//Now to add jars to system class loader via ByteBuddy. This will not always be needed, as in some contexts the jars having been added to certain classloaders in the above code will suffice
			addToStartupNotices(" Adding jars to system classloader via ByteBuddy, just in case");
			String jarPath="";
			for (int i = 0; i<jars.size(); i++){
				jarPath = (String)jars.elementAt(i);
				if (verboseStartup) System.out.println("loading " + jarPath);
				if (jarPath.endsWith("jar"))
					JarLoader.addJarFileToClassPath(jarPath);
			}
			return classLoader;
		} 
		catch (Throwable t) {
			t.printStackTrace();
		}
		return classLoader;
	}
	/*.................................................................................................................*/
	/* See start.Mesquite.java for an overview of how Mesquite starts up post-2O17*/
	public static void mainViaStarter(String args[], Object starter){
		MesquiteTrunk.mesquiteTrunk.starter = starter;
		addToStartupNotices("Mesquite started via starter class");
		main(args);
	}
	/*.................................................................................................................*/
	public static void main(String args[])
	{
		addToStartupNotices("Mesquite Startup arguments:" + StringArray.toString(args));
		MesquiteWindow.GUIavailable = !MesquiteWindow.headless;
		StartupThread sThread = new StartupThread(args);
		sThread.start(); //for bug in Linus post-Java-8
		//mesquiteTrunk = new Mesquite(args);
	}


}



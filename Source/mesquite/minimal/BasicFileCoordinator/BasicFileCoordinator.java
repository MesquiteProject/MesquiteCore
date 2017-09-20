/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.BasicFileCoordinator;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.charMatrices.lib.MatrixInfoExtraPanel;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/** The "vice president" of the Mesquite system (the MesquiteTrunk module "Mesquite" being the president).  One of these is hired to coordinate each project.
Should actually be named ProjectCoordinator.*/
public class BasicFileCoordinator extends FileCoordinator implements PackageIntroInterface {
	public static long totalProjectPanelRefreshes = 0;
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(FileInit.class, "Modules assist with tasks connected to each project or file.",
				"");
		e2.setAsEntryPoint(null);
		e2.setSuppressListing(true);
		EmployeeNeed e3 = registerEmployeeNeed(FileElementManager.class, "Modules assist with management of taxa, matrices, trees and other data to each project or file.",
				"");
		e3.setAsEntryPoint(null);
		e3.setSuppressListing(true);
		EmployeeNeed e4 = registerEmployeeNeed(FileInterpreter.class, "Modules assist with reading and writing files of various formats.",
				"");
		e4.setAsEntryPoint(null);
		e4.setSuppressListing(true);
		EmployeeNeed e5 = registerEmployeeNeed(FileAssistant.class, "Modules assist with files.",
				"");
		e5.setAsEntryPoint("newAssistant");
		//e5.setSuppressListing(true);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Basic File Coordinator";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates the reading, maintenance and linkages of files, which include Taxa, Trees, CharacterData, and other objects." 
				+ " This module is hired directly by the Mesquite trunk module.";
	}
	MesquiteMenuSpec elementsMenu;

	boolean ready = false;
	Vector firstToKnowModules;
	ProjectWindow pw;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//	elementsMenu = makeMenu("Elements");

		loadPreferences();
		firstToKnowModules = new Vector();
		MesquiteModule e;
		hireAllEmployees(FileInterpreter.class);
		hireInit( FileInit.class, "Manage Authors"); 
		firstToKnowModules.addElement(hireInit( FileElementManager.class, "#mesquite.minimal.ManageTaxa.ManageTaxa")); 
		firstToKnowModules.addElement(hireInit( FileElementManager.class, "#mesquite.trees.ManageTrees.ManageTrees"));
		firstToKnowModules.addElement(hireInit( FileInit.class, "#mesquite.trees.BasicTreeWindowCoord.BasicTreeWindowCoord"));
		firstToKnowModules.addElement(hireInit( FileInit.class, "#mesquite.charMatrices.BasicDataWindowCoord.BasicDataWindowCoord"));
		firstToKnowModules.addElement(hireInit( FileElementManager.class, "#mesquite.charMatrices.ManageCharacters.ManageCharacters"));
		hireInit( FileInit.class, "Manage Character Models"); 
		hireInit( FileInit.class, "Manage parsimony model sets");
		hireInit( FileInit.class, "Manage probability model sets");
		hireInit( FileInit.class, "Manage character sets");
		hireInit( FileInit.class, "Manage SETS blocks");
		hireInit( FileInit.class, "Manage ASSUMPTIONS blocks");
		hireInit( FileInit.class, "Manage NOTES blocks");
		hireAllOtherEmployees(FileElementManager.class);
		return true;
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "suggestedDirectory", MesquiteTrunk.suggestedDirectory);  
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("suggestedDirectory".equalsIgnoreCase(tag)){
			MesquiteTrunk.suggestedDirectory = StringUtil.cleanXMLEscapeCharacters(content);
		}
	}

	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			MesquiteTrunk.suggestedDirectory = prefs[0];
		}
	}
	/*.................................................................................................................*/
	/** Returns the  integer version of the MesquiteCore version  that this package requires to function*/
	public int getMinimumMesquiteVersionRequiredInt(){
		return 100;  
	}
	/*.................................................................................................................*/
	/** Returns the String version of the MesquiteCore version number that this package requires to function*/
	public String getMinimumMesquiteVersionRequired(){
		return "1.00";  
	}
	/*.................................................................................................................*/
	private MesquiteModule hireInit( Class dutyClass, String name){
		if (mesquiteTrunk.mesquiteModulesInfoVector.findModule(dutyClass, name) == null)
			return null;
		MesquiteModule e = hireNamedEmployee(dutyClass, StringUtil.tokenize(name));
		if (e!=null)
			e.setPriorityFixed(true);
		return e;
	}
	public void endJob() {
		doomed = true;
		incrementMenuResetSuppression();
		disposeMenuSpecifications();
		MesquiteProject p=getProject();
		if (p!=null) {	
			p.isDoomed = true;
			int count = 0;
			boolean done = false;
			while (p.developing && !MesquiteThread.isScripting() && !done) {
				count++;
				if (count % 10000 == 0){
					MesquiteMessage.printStackTrace("Failure to close project");
					if (MesquiteThread.isScripting() || AlertDialog.query(containerOfModule(), "Force Close?", "The project is not responding to the request to close it.  Force it to close?", "Force Close", "Continue", 1))
						done = true;
				}
			}
			closeWindows();
			if (pw != null)
				pw.dispose();
			pw = null;
			setModuleWindow(null);
			doomEmployees(this);
			p.dispose();
			MesquiteTrunk.mesquiteTrunk.removeProject(p);
		}
		decrementMenuResetSuppression();
		setProject(null);
		super.endJob();
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		if (m instanceof FileAssistantC) {
			resetAllMenuBars();
		}
		super.employeeQuit(m);
	}
	private void doomEmployees(MesquiteModule mb){
		if (mb.getEmployeeVector()==null)
			return;
		Enumeration e = mb.getEmployeeVector().elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mbe = (MesquiteModule)obj;
			if (mbe!=null) {
				doomEmployees(mbe);
				mbe.doom();
			}
		}
	}
	public void closeWindows(){
		if (getModuleWindow() != null){
			getModuleWindow().getParentFrame().setVisible(false);
		}
		Enumeration e = MesquiteTrunk.mesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw.isVisible() && mw.getOwnerModule()!=null && mw.getOwnerModule().getProject()== getProject())
				mw.getParentFrame().setVisible(false);
		}

	}
	/*.................................................................................................................*/
	public MesquiteProject initiateProject(String pathName, MesquiteFile homeFile) {
		MesquiteProject p = new MesquiteProject((FileCoordinator)this);
		p.incrementProjectWindowSuppression();
		setProject(p);
		pw =  new ProjectWindow(this);
		p.setFrame(pw.getParentFrame());
		setModuleWindow(pw);
		if (Thread.currentThread() instanceof ProjectReadThread)
			pw.setWindowSize(2, 500);
		else
			pw.setWindowSize(700, 500);
		pw.setWindowLocation(8,8, false); //TODO: should set staggered positions

		MesquiteTrunk.mesquiteTrunk.addProject(getProject());
		p.addFile(homeFile);
		homeFile.setProject(p);
		p.setHomeFile(homeFile);
		for (int i = 0; i<firstToKnowModules.size(); i++){
			MesquiteModule mb = (MesquiteModule)firstToKnowModules.elementAt(i);
			if (mb != null)
				mb.projectEstablished();
		}
		hireAllOtherEmployees(FileInitEarlyLoad.class);  //TODO: NOTSCRIPTING???
		hireAllOtherEmployees(FileInit.class);  //TODO: NOTSCRIPTING???
		if (getProject()==null)
			alert("FILE NULL");

		///THESE SHOULD BE ONLY AFTER Project made
		addModuleMenuItems(MesquiteTrunk.windowsMenu, makeCommand("newAssistant",  this), FileAssistantN.class);
		MesquiteMenuItemSpec mi;
		mi = addMenuItem(MesquiteTrunk.analysisMenu, "Chart Wizard...", makeCommand("chartWizard",  this));
		mi.setArgument("mesquite.charts.");

		mi.setZone(0);
		if (numCompatibleModulesAvailable(FileAssistantCH.class, null, this)>0) {
			mi = addSubmenu(MesquiteTrunk.analysisMenu, "New Bar & Line Chart for", makeCommand("newAssistant",  this), FileAssistantCH.class);
			mi.setZone(0);
		}
		if (numCompatibleModulesAvailable(FileAssistantCS.class, null, this)>0) {
			mi = addSubmenu(MesquiteTrunk.analysisMenu, "New Scattergram for", makeCommand("newAssistant",  this), FileAssistantCS.class);
			mi.setZone(0);
		}
		if (numCompatibleModulesAvailable(FileAssistantCP.class, null, this)>0) {
			mi = addSubmenu(MesquiteTrunk.analysisMenu, "New Pairwise Chart for", makeCommand("newAssistant",  this), FileAssistantCP.class);
			mi.setZone(0);
		}
		MesquiteSubmenuSpec mss = addSubmenu(MesquiteTrunk.analysisMenu, "Current Charts", makeCommand("showChart", this));
		mss.setZone(1);
		mi = addMenuItem(MesquiteTrunk.analysisMenu, "-", null);
		mi.setZone(1);
		Enumeration e = MesquiteTrunk.windowVector.elements();
		mss.setList(MesquiteTrunk.windowVector);
		mss.setListableFilter(ChartWindow.class);
		//	MesquiteSubmenuSpec mss2 = addSubmenu(MesquiteTrunk.analysisMenu, "Other", makeCommand("newAssistant",  this), FileAssistantA.class);
		addMenuItem(MesquiteTrunk.analysisMenu, "-", null);
		addModuleMenuItems(MesquiteTrunk.analysisMenu, makeCommand("newAssistant",  this), FileAssistantA.class);

		broadcastProjectEstablished(this);
		p.refreshProjectWindow();
		p.decrementProjectWindowSuppression();
		return p;
	}
	/** Dumps list of available modules to System.out.*/
	public void dumpModuleList () {
		int num = MesquiteTrunk.mesquiteModulesInfoVector.size();
		logln("\n\n=====All Modules Loaded=====");
		MesquiteModuleInfo mbi;
		for (int i=0; i<num; i++){
			mbi = (MesquiteModuleInfo)MesquiteTrunk.mesquiteModulesInfoVector.elementAt(i);
			logln(mbi.getClassName() + "              duty class " + mbi.getDutyClass());
		}
		logln("=================\n\n");
	}
	/*.................................................................................................................*/
	public MesquiteFile createProject(String pathName, boolean createTaxaBlockIfNew) { 
		//TODO: ALLOW THIS ONLY ONCE FOR A FILE COORDINATOR
		MainThread.incrementSuppressWaitWindow();
		incrementMenuResetSuppression();
		String dirName;
		String fileName;

		boolean brandNew = false;
		if (StringUtil.blank(pathName) && !MesquiteThread.isScripting()) {
			MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), "New file:", FileDialog.SAVE);
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setFile("untitled.nex");
			fdlg.setVisible(true);
			dirName= fdlg.getDirectory();
			fileName= fdlg.getFile();
			// fdlg.dispose();
			if (StringUtil.blank(dirName) || StringUtil.blank(fileName)) {
				decrementMenuResetSuppression();
				MainThread.decrementSuppressWaitWindow();
				if (StringUtil.blank(dirName) &&  StringUtil.blank(fileName))
					logln("New Project cancelled by user");
				else if ( StringUtil.blank(fileName))
					logln("New Project not created because file name not supplied.");
				else if ( StringUtil.blank(dirName))
					logln("New Project not created because directory name not supplied.");
				return null;
			}
			brandNew = true;
		}
		else {
			dirName = StringUtil.getAllButLastItem(pathName, MesquiteFile.fileSeparator, "/") + MesquiteFile.fileSeparator;
			fileName = StringUtil.getLastItem(pathName, MesquiteFile.fileSeparator, "/");
		}
		MesquiteFile thisFile =MesquiteFile.newFile(dirName, fileName);

		MesquiteProject p=null;
		if (thisFile!=null) {
			p = initiateProject(thisFile.getFileName(), thisFile);
			p.incrementProjectWindowSuppression();
			thisFile.setProject(p);
			//add MesquiteBlock
			afterProjectRead();
			if (brandNew && createTaxaBlockIfNew) {
				MesquiteInteger buttonPressed = new MesquiteInteger(1);
				ExtensibleDialog makeFileDialog = new ExtensibleDialog(containerOfModule(), "New File Options",buttonPressed);
				makeFileDialog.addLargeOrSmallTextLabel ("Do you want your new file to include taxa?");
				Checkbox tx = makeFileDialog.addCheckBox("Make Taxa Block", true);
				SingleLineTextField nameTaxa = makeFileDialog.addTextField("Name", "Taxa", 30);
				IntegerField numberTaxa = makeFileDialog.addIntegerField("Number of Taxa", 3, 6);
				makeFileDialog.addHorizontalLine(1);
				//Checkbox showTree = makeFileDialog.addCheckBox("Show Tree Window", true);
				//makeFileDialog.addHorizontalLine(1);
				makeFileDialog.addLargeOrSmallTextLabel ("If your file includes taxa, do you want it to include a character matrix?");
				Checkbox mcm = makeFileDialog.addCheckBox("Make Character Matrix", false);
				makeFileDialog.completeAndShowDialog("OK",null,null,"OK");

				CommandRecord oldCommandRec = MesquiteThread.getCurrentCommandRecord();
				CommandRecord scriptRec = new CommandRecord(true);
				MesquiteThread.setCurrentCommandRecord(scriptRec);
				boolean showSubstantiveWindow = false;

				if (buttonPressed.getValue()==0) {
					if (tx.getState() && numberTaxa.isValidInteger()){
						makeFileDialog.dispose();
						TaxaManager mTaxa = (TaxaManager)findEmployeeWithDuty(TaxaManager.class);
						Object t = null;
						if (mTaxa!=null) 
							t = mTaxa.doCommand("newTaxa",Integer.toString(numberTaxa.getValue()) + " " + ParseUtil.tokenize(nameTaxa.getText()), CommandChecker.defaultChecker); //treat as scripting so as not to complain if something not found 19Jan02

						if (t!=null){
							if (mcm.getState()){
								CharactersManager mChars = (CharactersManager)findEmployeeWithDuty(CharactersManager.class);
								if (mChars!=null) {
									mChars.doCommand("newMatrix",null, CommandChecker.defaultChecker); 
									mChars.doCommand("showLastMatrixWindow",null, CommandChecker.defaultChecker);
									showSubstantiveWindow = true;
								}
							}
							if (!showSubstantiveWindow){
								showProjectWindow();
								mTaxa.doCommand("showTaxa","0", CommandChecker.defaultChecker);
							}

						}
						else {
							makeFileDialog.dispose();
							showProjectWindow();
						}
					}
					else {
						makeFileDialog.dispose();
						showProjectWindow();
					}
				}
				else {
					makeFileDialog.dispose();
					showProjectWindow();
				}
				MesquiteThread.setCurrentCommandRecord(oldCommandRec);
			}
			MesquiteWindow w = getModuleWindow();
			if (w != null){
				if (!MesquiteThread.isScripting())
					w.setWindowSizeForce(700, 500);
				((ProjectWindow)w).resume(false);
			}

			p.decrementProjectWindowSuppression();
			p.refreshProjectWindow();
		}
		MesquiteWindow.hideClock();
		resetAllMenuBars();
		if (p!=null)
			p.developing = false;
		if (getModuleWindow()!=null) {
			getModuleWindow().getParentFrame().showFrontWindow();
		}
		decrementMenuResetSuppression();
		MainThread.decrementSuppressWaitWindow();
		return thisFile;
	}
	public void showProjectWindow(){
		doCommand("showWindow", null, CommandChecker.defaultChecker); //treat as scripting so as not to complain if something not found 19Jan02
	}
	/*.................................................................................................................*/
	public MesquiteFile createBlankProject() { 
		MainThread.incrementSuppressWaitWindow();
		incrementMenuResetSuppression();
		boolean brandNew = false;
		MesquiteFile thisFile =MesquiteFile.newFile(MesquiteTrunk.userDirectory.toString(), "temp.nex");

		MesquiteProject p=null;
		if (thisFile!=null) {
			p = initiateProject(thisFile.getFileName(), thisFile);
			thisFile.setProject(p);
			afterProjectRead();

		}
		MesquiteWindow.hideClock();
		resetAllMenuBars();
		if (p!=null)
			p.developing = false;
		decrementMenuResetSuppression();
		MainThread.decrementSuppressWaitWindow();
		return thisFile;
	}

	public void refreshProjectWindow(){
		if (getProject().refreshSuppression == 0){
			getProject().refreshPending = false;
			MesquiteWindow w = getModuleWindow();
			if (w != null && w instanceof ProjectWindow) {
				((ProjectWindow)w).refresh();
				((ProjectWindow)w).refreshGraphics();
			}
		}
		else 
			getProject().refreshPending = true;
	}
	public void refreshInProjectWindow(FileElement element){
		MesquiteWindow w = getModuleWindow();
		if (w != null && w instanceof ProjectWindow)
			((ProjectWindow)w).refresh(element);
	}
	public void refreshGraphicsProjectWindow(){
		MesquiteWindow w = getModuleWindow();
		if (w != null && w instanceof ProjectWindow)
			((ProjectWindow)w).refreshGraphics();
	}

	/*.................................................................................................................*/
	public void elementAdded(FileElement e){
		refreshProjectWindow();
	}
	/*.................................................................................................................*/
	public void elementDisposed(FileElement e){
		refreshProjectWindow();
	}
	/*.................................................................................................................*/
	public MesquiteFile createFile(String pathName){
		MainThread.incrementSuppressWaitWindow();
		String dirName;
		String fileName;
		if (StringUtil.blank(pathName)) {
			MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), "New linked file:", FileDialog.SAVE);
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			fdlg.setFile("linked.nex");
			fdlg.setVisible(true);
			dirName= fdlg.getDirectory();
			fileName= fdlg.getFile();
			// fdlg.dispose();
			if (StringUtil.blank(dirName) || StringUtil.blank(fileName)) {
				decrementMenuResetSuppression();
				MainThread.decrementSuppressWaitWindow();
				if (StringUtil.blank(dirName) &&  StringUtil.blank(fileName))
					logln("New File cancelled by user");
				else if ( StringUtil.blank(fileName))
					logln("New File not created because file name not supplied.");
				else if ( StringUtil.blank(dirName))
					logln("New File not created because directory name not supplied.");
				return null;
			}
		}
		else {
			dirName = StringUtil.getAllButLastItem(pathName, MesquiteFile.fileSeparator, "/") + MesquiteFile.fileSeparator;
			fileName = StringUtil.getLastItem(pathName, MesquiteFile.fileSeparator, "/");
		}
		MainThread.decrementSuppressWaitWindow();
		return MesquiteFile.newFile(dirName, fileName);
	}
	/*.................................................................................................................*/
	public MesquiteFile newLinkedFile(String pathName) {  //makes new linked file
		incrementMenuResetSuppression();
		MesquiteFile thisFile;
		if (StringUtil.blank(pathName)){
			MainThread.incrementSuppressWaitWindow();
			MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), "New linked file:", FileDialog.SAVE);
			fdlg.setFile("linked.nex");
			fdlg.setBackground(ColorTheme.getInterfaceBackground());
			String dirName;
			String fileName;

			fdlg.setVisible(true);
			dirName= fdlg.getDirectory();
			fileName= fdlg.getFile();
			// fdlg.dispose();
			MainThread.decrementSuppressWaitWindow();
			if (StringUtil.blank(dirName) || StringUtil.blank(fileName)) {
				decrementMenuResetSuppression();
				if (StringUtil.blank(dirName) &&  StringUtil.blank(fileName))
					logln("New File cancelled by user");
				else if ( StringUtil.blank(fileName))
					logln("New File not created because file name not supplied.");
				else if ( StringUtil.blank(dirName))
					logln("New File not created because directory name not supplied.");
				return null;
			}
			thisFile = MesquiteFile.newFile(dirName, fileName);
		}
		else {
			if (pathName.indexOf(MesquiteFile.fileSeparator)<0) {
				thisFile =MesquiteFile.newFile(getProject().getHomeDirectoryName(), pathName);
			}
			else {
				String dirName = StringUtil.getAllButLastItem(pathName, MesquiteFile.fileSeparator, "/") + MesquiteFile.fileSeparator;
				String fileName = StringUtil.getLastItem(pathName, MesquiteFile.fileSeparator, "/");
				thisFile =MesquiteFile.newFile(dirName, fileName);
			}
		}
		if (thisFile!=null) {
			getProject().addFile(thisFile);
			thisFile.setProject(getProject());
			broadcastFileRead(this, thisFile);
		}
		resetAllMenuBars();
		decrementMenuResetSuppression();
		return thisFile;
	}
	/*.................................................................................................................*/
	public MesquiteFile readProject(boolean local, String pathName, String arguments, Class importerSubclass) { 

		//ALLOW THIS ONLY ONCE FOR A FILE COORDINATOR
		if (getProject()!=null) { 
			alert("Error: attempt to read second project for single file coordinator");
			return null;
		}


		incrementMenuResetSuppression();
		MesquiteProject p=null;
		String message;
		if (local)
			message = "Open file as independent project:";
		else
			message = "Open URL as independent project:";

		MesquiteFile thisFile =MesquiteFile.open(local, pathName, message, MesquiteTrunk.suggestedDirectory);
		if (thisFile!=null && (thisFile.isLocal() && thisFile.existingLength()<=0)) { 
			alert("Error: File is empty.");
			return null;
		}

		if (thisFile!=null){
			MesquiteTrunk.suggestedDirectory = thisFile.getDirectoryName();
			storePreferences();
		}

		MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);

		boolean imp = false; //was it imported???

		if (thisFile!=null && !StringUtil.blank(thisFile.getFileName())) {

			logln("Location: " + thisFile.getDirectoryName() + thisFile.getFileName());
			logln("");

			FileInterpreter fileInterp;



			//first try nexus.  If can't be read, then make list and query user...
			NexusFileInterpreter nfi = (NexusFileInterpreter)findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
			if (importerSubclass== null && nfi!=null && nfi.canReadFile(thisFile))  
				fileInterp = nfi;
			else {
				imp = true;
				fileInterp = findImporter(thisFile, 0, importerSubclass, arguments);
			}
			if (fileInterp !=null) {
				p = initiateProject(thisFile.getFileName(), thisFile);
				p.timePreviouslySavedByFile = MesquiteFile.fileOrDirectoryLastModified(thisFile.getDirectoryName() + thisFile.getFileName());
				p.incrementProjectWindowSuppression();
				MesquiteFile sf = CommandRecord.getScriptingFileS();
				if (MesquiteThread.isScripting())
					CommandRecord.setScriptingFileS(thisFile);
				if (pw != null)
					pw.suppress();
				if (parser.tokenIndexOfIgnoreCase(arguments, "useStandardizedTaxonNames")>=0)
					thisFile.useStandardizedTaxonNames = true;
				int tI = parser.tokenIndexOfIgnoreCase(arguments, "taxa");
				if (tI>=0){
					String ref = parser.getTokenNumber(arguments, tI+2);
					Taxa taxa = getProject().getTaxa(ref);
					thisFile.setCurrentTaxa(taxa);
				}
				fileInterp.readFile(getProject(), thisFile, arguments);
				if (pw != null){
					if (fileInterp != nfi)
						pw.setWindowSize(700, 500);
					pw.resume();
				}
				CommandRecord.setScriptingFileS(sf);
				p.decrementProjectWindowSuppression();

				if (thisFile.isClosed()){
					if (p !=null)
						p.developing = false;
					resetAllMenuBars();
					decrementMenuResetSuppression();
					iQuit();
					return null;
				}
				else {
					// "Import" or "Translate"  Open Special NEXUS File
					// behaviour: autosave or not

					p.fileSaved(thisFile);  // If used import system, then doesn't add extension or save file if it was some type of NEXUS file 
					if (imp && (!(fileInterp instanceof NEXUSInterpreter)) && local && parser.tokenIndexOfIgnoreCase(arguments, "suppressImportFileSave")<0){//was imported; change name
						thisFile.changeLocation(thisFile.getDirectoryName(), thisFile.getFileName()+".nex");
						if (MesquiteThread.isScripting() || thisFile.changeLocation("Save imported file as NEXUS file")) {
							afterProjectRead();
							writeFile(thisFile); 
						}
						else {
							if (p !=null)
								p.developing = false;
							resetAllMenuBars();
							decrementMenuResetSuppression();
							iQuit();
							return null;
						}
					}
					else
						afterProjectRead();
				}
			}
			else {
				alert("Sorry, an interpreter was not found for this file");
			}
		}
		resetAllMenuBars();
		if (p!=null)
			p.developing = false;
		decrementMenuResetSuppression();
		if (p!=null)
			p.refreshProjectWindow();
		if (noWindowsShowing()){
			doCommand("showWindow", null, CommandChecker.defaultChecker); //TODO: will this always be non-scripting???
			if (imp){
				if (getProject().getNumberCharMatrices()>0){
					MesquiteModule mbb = findEmployeeWithName("Data Window Coordinator");
					if (mbb != null)
						mbb.doCommand("showDataWindow", "0", CommandChecker.defaultChecker);
				}
				else if (getProject().getNumberTaxas()>0){
					MesquiteModule mbb = findEmployeeWithName("Manage TAXA blocks");
					if (mbb != null)
						mbb.doCommand("showTaxa", "0", CommandChecker.defaultChecker);
				}
			}
		}

		if (thisFile != null && thisFile.getCloseAfterReading()){
			closeFile(thisFile);
			MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
			return null;
		}
		MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
		return thisFile;
	}
	/*.................................................................................................................*/
	public MesquiteFile readProjectGeneral(String arguments) { 
		if (getProject()!=null) { 
			alert("Error: attempt to read second project for single file coordinator");
			return null;
		}
		incrementMenuResetSuppression();
		MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
		parser.setString(arguments);
		//hire reader
		String reader = parser.getFirstToken();
		String extraArgs = parser.getNextToken();
		GeneralFileMaker e = (GeneralFileMaker)hireNamedEmployee(GeneralFileMaker.class, ParseUtil.tokenize(reader));

		MesquiteProject p = null;
		if (e != null) {
			p = e.establishProject(extraArgs);
			if (p!=null){
				p.incrementProjectWindowSuppression();
				if (pw == null){
					pw =  new ProjectWindow(this);
					p.setFrame(pw.getParentFrame());
					setModuleWindow(pw);

					pw.setWindowSize(700, 500);
					pw.setWindowLocation(8,8, false);
				}

				p.developing = false;
				afterProjectRead();
				refreshProjectWindow();
				p.decrementProjectWindowSuppression();
			}
			resetAllMenuBars();
		}
		decrementMenuResetSuppression();
		if (p==null){
			fireEmployee(e);
			iQuit();
			return null;
		}
		MesquiteFile thisFile = p.getHomeFile();
		if (noWindowsShowing())
			doCommand("showWindow", null, CommandChecker.defaultChecker); //TODO: will this always be non-scripting???
		if (thisFile != null && thisFile.getCloseAfterReading()){
			closeFile(thisFile);
			MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
			return null;
		}
		if (p.autosave){
			saveAllFiles();
			p.autosave = false;
		}
		MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
		return thisFile;
	}
	/*.................................................................................................................*/
	private boolean noWindowsShowing(){
		Enumeration e = MesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw !=null && !mw.getMinimalMenus() && mw.isVisible() && mw.ownerModule!=null && mw.ownerModule.getProject()==getProject())
				return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	public MesquiteFile readLinkedFile(String pathName, String importer, String arguments, int fileType, String fileDescriptionText){ //make new/read new linked file
		if (MesquiteThread.isScripting()) {
			ObjectContainer f = new ObjectContainer();
			FileRead pt = new FileRead(pathName, importer, arguments, fileType,   this, 0, f, fileDescriptionText);

			pt.run();
			MesquiteFile mf = (MesquiteFile)f.getObject();
			f.setObject(null);
			return mf;
		}
		else {
			FileRead fr = new FileRead(pathName, importer, arguments, fileType,   this, 0, null, fileDescriptionText);

			MesquiteThread pt = new MesquiteThread(fr);

			pt.setCommandRecord(MesquiteThread.getCurrentCommandRecord());
			pt.start();
			return null;
		}
	}
	/*.................................................................................................................*/
	public void includeFile(String pathName, String importer, String arguments, int fileType, String fileDescriptionText){ //make new/read new linked file
		FileRead pt = new FileRead(pathName, importer, arguments, fileType,   this, 1, null, fileDescriptionText);
		if (MesquiteThread.isScripting()) {
			pt.run();
		}
		else {
			MesquiteThread mt = new MesquiteThread(pt);
			mt.start();
		}
	}
	/*.................................................................................................................*/
	public void includeFileFuse(String pathName, String importer, String arguments, int fileType, String fileDescriptionText){ //make new/read new linked file  DONE special to put on same thread
		getProject().incrementProjectWindowSuppression();
		FileRead pt = new FileRead(pathName, importer, arguments, fileType,   this, 1, null, fileDescriptionText);
		pt.run();
		cleanFusedReadingSuppressions();
		getProject().decrementProjectWindowSuppression();
	}
	void cleanFusedReadingSuppressions(){
		int num = getProject().getNumberCharMatrices();
		for (int im = 0; im< num; im++){
			CharacterData data = getProject().getCharacterMatrix(im);
			data.setSuppressSpecssetReading(false);
		}
	}
	/*.................................................................................................................*/
	public MesquiteFile getNEXUSFileForReading(String arguments, String message){ 
		String path = parser.getFirstToken(arguments); //optional argument


		MesquiteFile file = null;
		if (StringUtil.blank(path)) {
			file =MesquiteFile.open(true, (String)null, message, getProject().getHomeDirectoryName());
		}
		else if (path.indexOf(MesquiteFile.fileSeparator)<0) {
			file =MesquiteFile.open(getProject().getHomeDirectoryName(), path);
		}
		else {
			String dirName = StringUtil.getAllButLastItem(path, MesquiteFile.fileSeparator, "/") + MesquiteFile.fileSeparator;
			String fileName = StringUtil.getLastItem(path, MesquiteFile.fileSeparator, "/");
			file =MesquiteFile.open(dirName, fileName);
		}
		if (file == null)
			return null;
		NexusFileInterpreter nfi = (NexusFileInterpreter)findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
		if (nfi==null || !nfi.canReadFile(file)) {
			return null; //not a nexus file
		}
		return file;
	}
	/*.................................................................................................................*/
	public FileBlock getNEXUSBlock(String blockType, String path){ 
		MesquiteFile file = null;
		if (StringUtil.blank(path)) {
			String message = "Choose file from which to obtain " + blockType + " block";
			file =MesquiteFile.open(true, (String)null, message, getProject().getHomeDirectoryName());
		}
		else if (path.indexOf(MesquiteFile.fileSeparator)<0) {
			file =MesquiteFile.open(getProject().getHomeDirectoryName(), path);
		}
		else {
			String dirName = StringUtil.getAllButLastItem(path, MesquiteFile.fileSeparator, "/") + MesquiteFile.fileSeparator;
			String fileName = StringUtil.getLastItem(path, MesquiteFile.fileSeparator, "/");
			file =MesquiteFile.open(dirName, fileName);
		}
		if (file == null)
			return null;
		NexusFileInterpreter nfi = (NexusFileInterpreter)findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
		if (!nfi.canReadFile(file)) {
			return null;
		}
		FileBlock block = nfi.readOneBlock(getProject(), file, blockType, 0);
		file.dispose();
		return block;
	}
	/*.................................................................................................................*/
	public TextDisplayer displayText(String text, String title) {
		TextDisplayer displayer = (TextDisplayer)hireEmployee(TextDisplayer.class, null);
		if (displayer!=null){
			displayer.showText(text, title, true); 
		}
		return displayer;
	}
	/*.................................................................................................................*/
	public TextDisplayer displayFile(MesquiteFile file, int maxCharacters) {
		TextDisplayer displayer = (TextDisplayer)hireEmployee(TextDisplayer.class, null);
		if (displayer!=null) {
			displayer.showFile(file, maxCharacters, true,9, true); 
		}
		return displayer;
	}
	/*.................................................................................................................*/
	public TextDisplayer displayFile(String pathName, int maxCharacters) {
		TextDisplayer displayer = (TextDisplayer)hireEmployee(TextDisplayer.class, null);
		if (displayer!=null) {
			displayer.showFile(pathName, maxCharacters, true,9, true); 
		}
		return displayer;
	}
	/*.................................................................................................................*/
	void afterProjectRead() {
		if (getModuleWindow()!=null) {
			getModuleWindow().resetTitle(); //WHAT IF MULTIPLE LINKED FILES??
		}
		FileInit employee;
		Enumeration enumeration=getEmployeeVector().elements();

		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			((MesquiteModule)obj).setProject(getProject());
		}
		broadcastFileRead(this, getProject().getHomeFile());
		ready = true;
		if (getModuleWindow()!=null) {
			getModuleWindow().getParentFrame().showFrontWindow();
			//	getModuleWindow().getParentFrame().fixFrontness();
		}

		resetAllMenuBars();
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been established but not yet read in.*/
	void broadcastProjectEstablished(MesquiteModule module) {
		if (module==null)
			return;

		EmployeeVector e = module.getEmployeeVector();
		if (e==null)
			return;
		for (int i = 0; i<e.size(); i++) {
			MesquiteModule employee = (MesquiteModule)e.elementAt(i);

			if (employee!=null){
				if (firstToKnowModules.indexOf(employee)<0)
					employee.projectEstablished();
				broadcastProjectEstablished(employee);
			}
		}
	}
	/*.................................................................................................................*/
	/** A method called immediately after a file has been read in.*/
	public void wrapUpAfterFileRead(MesquiteFile f) {
		broadcastFileRead(this, f);
	}
	/*.................................................................................................................*/
	/** A method called immediately after a file has been read in.*/
	void broadcastFileRead(MesquiteModule module, MesquiteFile f) {
		if (module==null)
			return;
		EmployeeVector e = module.getEmployeeVector();
		if (e==null)
			return;
		for (int i = 0; i<e.size(); i++) {
			MesquiteModule employee = (MesquiteModule)e.elementAt(i);
			if (employee!=null){
				employee.fileReadIn(f);
				broadcastFileRead(employee, f);
			}
		}
	}
	/*.................................................................................................................*/
	/** A method called immediately before a file is to be written.*/
	void broadcastFileAboutToBeSaved(MesquiteModule module, MesquiteFile f) {
		if (module==null)
			return;
		EmployeeVector e = module.getEmployeeVector();
		if (e==null)
			return;
		for (int i = 0; i<e.size(); i++) {
			MesquiteModule employee = (MesquiteModule)e.elementAt(i);
			if (employee!=null){
				employee.fileAboutToBeWritten(f);
				broadcastFileAboutToBeSaved(employee, f);
			}
		}
	}
	/*.................................................................................................................*/
	/** THIS APPEARS NO LONGER USED*/
	public String userPutFile(){
		MainThread.incrementSuppressWaitWindow();
		MesquiteFileDialog fdlg= new MesquiteFileDialog(containerOfModule(), "Save file", FileDialog.SAVE);
		fdlg.setBackground(ColorTheme.getInterfaceBackground());
		fdlg.setVisible(true);
		String s = fdlg.getDirectory() + fdlg.getFile();
		// fdlg.dispose();
		MainThread.decrementSuppressWaitWindow();
		return s;
	}
	/*.................................................................................................................*/
	/*  */
	public void saveLinkagesAs(String pathName){
		MesquiteMessage.warnProgrammer("Oops: saving linkages not yet working");
		//writes Nexus file with Mesquite script requesting all linked files to be opened
		// needs home file and other files in order.
		//(NEED vector of files)
	}
	/*.................................................................................................................*/
	/*  */
	public void saveAllFiles(){
		if (getProject() ==null)
			return;
		Enumeration enumeration=getProject().getFiles().elements();
		MesquiteFile fi;
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof MesquiteFile) {
				fi = (MesquiteFile)obj;
				if (fi.isLocal())
					writeFile(fi);
				else
					MesquiteMessage.notifyUser("File \"" + fi.getName() + "\" cannot be written because it was accessed as a URL");
			}
		}
	}
	/*.................................................................................................................*/
	/*  */
	public boolean closeFile(MesquiteFile fi){
		return closeFile(fi, false);
	}
	public boolean closeFile(MesquiteFile fi, boolean quietly){
		if (getProject() ==null)
			return false;
		incrementMenuResetSuppression();
		if (fi!=null) {
			if (getProject().getHomeFile() == fi) { //should ask for all files
				if (quietly || MesquiteThread.isScripting()) {
					waitWriting(null);
					logln("Closing " + getProject().getName());
					getProject().isDoomed = true;
					iQuit();
				}
				else {
					fileCloseRequested();
					if (!getProject().isDirty()) {
						waitWriting(null);
						logln("Closing " + getProject().getName());
						getProject().isDoomed = true;
						iQuit();
					}
					else {
						ListableVector files = getProject().getFiles();
						if (files != null){
							Enumeration enumeration=files.elements();
							if (enumeration != null){
								MesquiteFile fiP;
								while (enumeration.hasMoreElements()){
									Object obj = enumeration.nextElement();
									if (obj instanceof MesquiteFile) {
										fiP = (MesquiteFile)obj;

										if (fiP!=null && fiP.isDirty() && fiP.isLocal()) {
											String message = "Do you want to save changes to \"" + fiP.getName() + "\" before closing?";
											int q = AlertDialog.query(containerOfModule(), "Save changes?",  message, "Save", "Cancel", "Don't Save");
											if (q==0) {
												logln("Writing " + fiP.getName());
												writeFile(fiP);
											}
											else if (q==1) {
												logln("File close cancelled by user");
												decrementMenuResetSuppression();
												return false;
											}
										}
									}
								}
							}
						}
						logln("Closing " + getProject().getName());
						iQuit();
					}
				}
			}
			else {
				if (quietly || MesquiteThread.isScripting() || !fi.isDirty()) {
					waitWriting(fi);
					fi.close();
				}
				else if (fi.isLocal()){
					fileCloseRequested();
					String message = "Do you want to save changes to \"" + fi.getName() + "\" before closing?";
					int q = AlertDialog.query(containerOfModule(), "Save changes?",  message, "Save", "Cancel", "Don't Save");
					if (q==0) 
						writeFile(fi);
					else if (q==1) {
						logln("File close cancelled by user");
						decrementMenuResetSuppression();
						return false;
					}
					logln("Closing file " + fi.getName());
					fi.close();
				}
			}
		}
		decrementMenuResetSuppression();
		return true;
	}
	private void waitWriting(MesquiteFile f){
		try {
			boolean done = false;
			while (!done){
				if (f == null) {
					done = true;
					ListableVector files = getProject().getFiles();
					for (int i=0; i<files.size(); i++){
						if (((MesquiteFile)files.elementAt(i)).isWriting())
							done = false;
					}
				}
				else
					done = !f.isWriting();
				if (!done)
					Thread.sleep(50);
			}
		}
		catch (InterruptedException e){
		}
	}
	/*.................................................................................................................*/
	public void revertToSaved(boolean queryIfDirty){
		boolean siga = false;
		if (!queryIfDirty)
			siga = true;
		else if (!MesquiteThread.isScripting()){
			if (getProject() == null || getProject().getHomeFile() == null)
				siga = false;
			else if (getProject().isDirty()){
				if (getProject().getNumberLinkedFiles()>1){
					String hfn = getProject().getHomeFileName();
					siga = AlertDialog.query(containerOfModule(), "Discard unsaved changes?", "Do you want to discard all unsaved changes in all linked files and revert to the saved version of the home file (" + hfn + ")?", "Discard Changes", "Cancel");
				}
				else
					siga = AlertDialog.query(containerOfModule(), "Discard unsaved changes?", "Do you want to discard all unsaved changes and revert to the saved version of the file?", "Discard Changes", "Cancel");
			}
			else
				siga = true;
		}
		if (siga){
			MesquiteTrunk.mesquiteTrunk.openFile(getProject().getHomeFile().getPath());
			iQuit();
		}
	}
	/*.................................................................................................................*/
	/*  */
	public void closeFile(int id){
		MesquiteFile fi = getProject().getFileByID(id);
		closeFile(fi);
	}
	public void closeFile(String id){
		if (getProject() == null)
			return;
		MesquiteFile fi = getProject().getFile(id);
		closeFile(fi);
	}
	/*.................................................................................................................*/
	/*  */
	public boolean export(FileInterpreterI exporter, MesquiteFile file, String arguments){
		if (exporter!=null){
			MainThread.incrementSuppressWaitWindow();
			//check first that file can be written.
			if (file != null){
				if (!file.canCreateOrRewrite()){
					discreetAlert("Sorry, the file \"" + file.getFileName() + "\"could not be exported because of problems concerning the file system.  See diagnosis in the Mesquite Log");
					String report = file.diagnosePathIssues();
					logln("DIAGNOSIS of folder and file status:\n" + report);
					return false;
				}
			}
			boolean success = exporter.exportFile(file, arguments); 
			MainThread.decrementSuppressWaitWindow();
			return success;
		}
		return false;
	}
	/*.................................................................................................................*/
	/*  */
	public void saveFile(int id){
		if (getProject() ==null)
			return;
		MainThread.incrementSuppressWaitWindow();
		MesquiteFile fi;
		if (!MesquiteInteger.isCombinable(id) && getProject().getNumberLinkedFiles()==1)
			fi = getProject().getHomeFile();
		else
			fi = getProject().getFileByID(id);
		if (fi != null) { 
			saveFile(fi);

		}
		else 
			MesquiteMessage.notifyUser("Request to save file id#" + MesquiteInteger.toString(id)+ "; file not found");
		MainThread.decrementSuppressWaitWindow();
	}
	/*.................................................................................................................*/
	/*  */
	public void saveFile(MesquiteFile fi){
		if (getProject() ==null)
			return;
		getProject().incrementProjectWindowSuppression();
		MainThread.incrementSuppressWaitWindow();
		if (fi != null) { 
			if (fi.isLocal()){
				writeFile(fi);
				if (!MesquiteThread.isScripting())
					MesquiteWindow.repaintAllWindows();
			}
			else
				MesquiteMessage.notifyUser("File \"" + fi.getName() + "\" cannot be written because it was accessed as a URL");
		}
		MainThread.decrementSuppressWaitWindow();
		if (getProject() != null)
			getProject().decrementProjectWindowSuppression();
	}
	/*.................................................................................................................*/
	/*  */
	public void saveFileAs(int id){
		if (getProject() ==null)
			return;
		getProject().incrementProjectWindowSuppression();
		MesquiteFile fi;
		if (!MesquiteInteger.isCombinable(id) && getProject().getNumberLinkedFiles()==1)
			fi = getProject().getHomeFile();
		else
			fi = getProject().getFileByID(id);
		saveFileAs(fi);
		getProject().decrementProjectWindowSuppression();
	}
	public void saveFileAs(MesquiteFile fi){
		getProject().incrementProjectWindowSuppression();
		MainThread.incrementSuppressWaitWindow();
		if (fi !=null){
			/*if (!fi.isLocal())
				MesquiteMessage.notifyUser("File \"" + fi.getName() + "\" cannot be written because it was accessed as a URL");
			else */
			if (fi.changeLocation("Save file")) {
				fi.setWriteProtected(false);
				writeFile(fi);
			}
		}
		//TODO: change titles of windows and menu items!!!
		MainThread.decrementSuppressWaitWindow();
		getProject().decrementProjectWindowSuppression();
	}
	public void renameAndSaveFile(int id, String name){
		if (StringUtil.blank(name))
			return;
		getProject().incrementProjectWindowSuppression();
		MainThread.incrementSuppressWaitWindow();
		MesquiteFile fi;
		if (!MesquiteInteger.isCombinable(id) && getProject().getNumberLinkedFiles()==1)
			fi = getProject().getHomeFile();
		else
			fi = getProject().getFileByID(id);
		if (fi !=null){
			fi.changeLocation(fi.getDirectoryName(), name);
			fi.setWriteProtected(false);
			writeFile(fi);
		}
		MainThread.decrementSuppressWaitWindow();
		getProject().decrementProjectWindowSuppression();
	}
	/*.................................................................................................................*/
	/*  */
	public void writeFile(MesquiteFile nMF){
		if (!MesquiteThread.isScripting() && nMF.getDirectoryName() == null){
			boolean success = nMF.changeLocation("Save file");
			if (!success)
				return;
		}
		broadcastFileAboutToBeSaved(this, nMF);
		Runtime rt = Runtime.getRuntime();
		rt.gc();

		NexusFileInterpreter nfi = (NexusFileInterpreter)findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
		if (nfi!=null) {
			nfi.writeFile(getProject(), nMF);
		}
		else 
			MesquiteMessage.println("File interpreter not found to write file " + nMF.getName());
	}
	/*.................................................................................................................*/
	/** Finds the first employee in the heirarchy that manages a particular subclass of FileElement */
	public ElementManager findManager(MesquiteModule m, Class fileElementClass) {
		if (fileElementClass == null)
			return null;
		ElementManager r = findExactManager(m, fileElementClass);
		if (r!=null)
			return r;
		else
			return findCapableManager(m, fileElementClass);
	}
	/*.................................................................................................................*/
	/** Finds the first employee in the heirarchy that manages a particular subclass of FileElement */
	private ElementManager findExactManager(MesquiteModule m, Class fileElementClass) {
		if (m instanceof ElementManager) {
			ElementManager fEM = (ElementManager)m;
			if (fEM!=null){
				Class eC = fEM.getElementClass();
				if (eC != null && eC ==fileElementClass) {  //manager must handle fileElementClass or its superclasses
					return fEM;
				}
			}
		}
		EmployeeVector ev = m.getEmployeeVector();
		if (ev==null)
			return null;
		Enumeration enumeration=ev.elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule mb = (MesquiteModule)enumeration.nextElement();
			ElementManager fEM = findExactManager(mb, fileElementClass);
			if (fEM != null)
				return fEM;
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Finds the first employee in the heirarchy that manages a particular subclass of FileElement */
	public ElementManager findCapableManager(MesquiteModule m, Class fileElementClass) {
		if (m instanceof ElementManager) {
			ElementManager fEM = (ElementManager)m;
			if (fEM != null) {
				Class eC = fEM.getElementClass();
				if (eC != null && eC.isAssignableFrom(fileElementClass)) {  //manager must handle fileElementClass or its superclasses
					return fEM;
				}
			}
		}
		EmployeeVector ev =m.getEmployeeVector();
		if (ev == null)
			return null;
		Enumeration enumeration=ev.elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule mb = (MesquiteModule)enumeration.nextElement();
			ElementManager fEM = findCapableManager(mb, fileElementClass);
			if (fEM != null)
				return fEM;
		}
		return null;
	}
	private String findImporterName(String arguments){  //hackathon
		String fRA = parser.getFirstToken(arguments); //path
		fRA = parser.getNextToken(); //extra arguments

		while (!StringUtil.blank(fRA)) {
			if (fRA.equalsIgnoreCase(StringUtil.argumentMarker + "interpreter")) {
				parser.getNextToken(); //eating up =
				return parser.getNextToken();
			}
			fRA = parser.getNextToken();
		}
		return null;
	}
	/*.................................................................................................................*/
	public FileInterpreter findImporter(String fileContents, String fileName, int fileType, Class importerSubclass, String arguments,boolean mustReadFromString, Class stateClass) {
		//hackathon
		String importerName = findImporterName(arguments);
		if (importerName != null){
			MesquiteModule mb= findEmployeeWithName( importerName, true);
			if (mb instanceof FileInterpreterI){
				FileInterpreterI importer = (FileInterpreterI)mb;
				if (importer.canImport(arguments))
					return importer;
			}
		}

		TextDisplayer fd = displayText(fileContents, fileName);  //TODO: should say if scripting

		MesquiteModule[] fInterpreters = null;
		if (importerSubclass != null)
			fInterpreters = getImmediateEmployeesWithDuty(importerSubclass);
		else if (fileType == 0)
			fInterpreters = getImmediateEmployeesWithDuty(FileInterpreterI.class);
		else
			fInterpreters = getImmediateEmployeesWithDuty(FileInterpreterITree.class);
		if (fInterpreters==null)
			return null;
		int count = 0;
		for (int i=0; i<fInterpreters.length; i++){
			boolean stateOK = (stateClass==null || ((FileInterpreterI)fInterpreters[i]).canImport(stateClass));
			if (((FileInterpreterI)fInterpreters[i]).canImport(arguments) && (fInterpreters[i] instanceof ReadFileFromString || !mustReadFromString) && stateOK)
				count++;
		}
		if (count == 0 ){
			if (fd!=null)
				fireEmployee(fd);
			return null;
		}
		MesquiteModule[] fInterpretersCanImport = new MesquiteModule[count];
		count = 0;
		for (int i=0; i<fInterpreters.length; i++){
			boolean stateOK = (stateClass==null || ((FileInterpreterI)fInterpreters[i]).canImport(stateClass));
			if (((FileInterpreterI)fInterpreters[i]).canImport(arguments) && (fInterpreters[i] instanceof ReadFileFromString || !mustReadFromString) && stateOK)
				fInterpretersCanImport[count++] = fInterpreters[i];
		}
		fInterpretersCanImport = prioritize(fInterpretersCanImport, FileInterpreterI.class);
		boolean fuse = parser.hasFileReadingArgument(arguments, "fuseTaxaCharBlocks");
		String message = "Please choose an interpreter for this file";
		if (fuse)
			message += "\n(Only some file formats are supported, because this is a special merging operation)";
		Listable fInt = ListDialog.queryList(fd.containerOfModule(), "Translate File", message, MesquiteString.helpString,fInterpretersCanImport, 0);

		if (fInt instanceof FileInterpreterI && !MesquiteThread.isScripting())
			((FileInterpreterI)fInt).getImportOptions(fuse);

		if (fd!=null)
			fireEmployee(fd);
		return (FileInterpreter)fInt;
	}
	/*.................................................................................................................*/
	public FileInterpreter findImporter(MesquiteFile f, int fileType, Class importerSubclass, String arguments) {
		String s = MesquiteFile.getFileContentsAsString(f.getPath(), 4000); 
		if (StringUtil.blank(s))
			return null;
		return findImporter(s,f.getName(),fileType, importerSubclass, arguments, false, null);
	}
	/*.................................................................................................................*/
	public Snapshot getIDSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		Snapshot fromProject = getProject().getSnapshot(file);
		if (fromProject != null && fromProject.getNumLines() > 0) {
			temp.addLine("getProject");
			temp.addLine("tell It");
			temp.incorporate(fromProject, true);
			temp.addLine("endTell");
		}
		temp.addLine("timeSaved " + System.currentTimeMillis());
		ListableVector files = getProject().getFiles();
		for (int i = 0; i<files.size(); i++) {
			MesquiteFile e=(MesquiteFile)files.elementAt(i);
			if (e!=getProject().getHomeFile())
				temp.addLine("linkFile " + StringUtil.tokenize(MesquiteFile.decomposePath(getProject().getHomeFile().getDirectoryName(), e.getPath())));  //quote //todo: should parse name relative to path to home file!!!!!
		}
		return temp;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		/*
   	 	ListableVector files = getProject().getFiles();
		for (int i = 0; i<files.size(); i++) {
			MesquiteFile e=(MesquiteFile)files.elementAt(i);
			if (e!=getProject().getHomeFile())
				temp.addLine("linkFile " + StringUtil.tokenize(e.getName()));  //quote //todo: should parse name relative to path to home file!!!!!

		}
		 */
		if (pw != null){
			temp.addLine("getWindow");
			temp.addLine("tell It");
			temp.incorporate(pw.getSnapshot(file), true);
			temp.addLine("endTell");

		}
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (!(e instanceof FileAssistant)) {
				temp.addLineIfAnythingToTell("getEmployee " , ((MesquiteModule)e));  //addLineIfAnythingToTell
			}
		}
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof FileAssistant) {
				temp.addLine("newAssistant " , ((MesquiteModule)e)); 
			}
		}
		return temp;
	}
	/*.................................................................................................................*/
	public void showInitTreeWindow(Taxa taxa, boolean force){
		MesquiteModule btwc = findEmployeeWithName("#BasicTreeWindowCoord", true);
		if (btwc !=null) {
			Commandable c = (Commandable)btwc.doCommand("makeTreeWindow","#" + taxa.getAssignedID(), CommandChecker.defaultChecker); 
			if (c!=null) {
				if (force)
					c.doCommand("showWindowForce",null, CommandChecker.defaultChecker);
				else c.doCommand("showWindow",null, CommandChecker.defaultChecker);
			}
		}
	}
	/*.................................................................................................................*/
	/** these are the commands that shouldn't dirty the file */
	public void fileDirtiedByCommand(MesquiteCommand command){
		if (command !=null && command.getOwner() == this) {
			if ("saveFiles".equalsIgnoreCase(command.getName()))
				return;
			else if ("saveFile".equalsIgnoreCase(command.getName()))
				return;
			else if ("saveFileAs".equalsIgnoreCase(command.getName()))
				return;
			else if ("saveLinkagesAs".equalsIgnoreCase(command.getName()))
				return;
			else if ("showFileOnDisk".equalsIgnoreCase(command.getName()))
				return;
			else if ("closeFile".equalsIgnoreCase(command.getName()))
				return;
			else if ("closeProject".equalsIgnoreCase(command.getName()))
				return;
		}
		super.fileDirtiedByCommand(command);
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
	private String filterIfMarkedArgument(String s){
		if (s == null || s.length() <1 || s.charAt(0) == StringUtil.argumentMarker)
			return null;
		return s;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the total number of character data matrices stored in the project", null, commandName, "getNumberOfDataSets")) {
			return (new MesquiteInteger(getProject().getNumberCharMatrices()));
		}
		else if (checker.compare(this.getClass(), "Returns a vector of the character data matrices stored in the project", null, commandName, "getDataSets")) {
			return (getProject().getCharacterMatrices());
		}
		else if (checker.compare(this.getClass(), "Sets the home file of the project so it is write protected", null, commandName, "writeProtect")) { //TODO: should use arguments to toggle
			getProject().getHomeFile().setWriteProtected(true);
		}
		else if (checker.compare(this.getClass(), "Returns the number of sets of taxa stored in the project", null, commandName, "getNumberOfTaxas")) {
			MesquiteInteger mi = new MesquiteInteger(getProject().getNumberTaxas());
			return (mi);
		}
		else if (checker.compare(this.getClass(),  "Returns a vector of the sets of taxa stored in the project", null, commandName, "getTaxas")) {
			return (getProject().getTaxas());
		}
		//TODO: getTaxa(id), getDataSet(id)
		else if (checker.compare(this.getClass(), "Returns a character model stored in the project", "[name of character model", commandName, "getCharacterModel")) {
			return getProject().getCharacterModel(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Reads from the file the time the file was last saved (used in IDSnapshot)", "[time in milliseconds]", commandName, "timeSaved")) {
			long time = MesquiteLong.fromString(arguments);
			if (MesquiteLong.isCombinable(time))
				getProject().timePreviouslySavedAsRecorded = time;
			double diff = Math.abs(getProject().timePreviouslySavedAsRecorded - getProject().timePreviouslySavedByFile)/1000.0;
		}
		else if (checker.compare(this.getClass(), "Reads a file as linked to a current file, so that they can share information, with an explanation as to what link is for, for error reporting", "[fileDescriptionText][path to file][importer]", commandName, "linkFileExp")) {
			String fileDescriptionText = parser.getFirstToken(arguments);
			String path = filterIfMarkedArgument(parser.getNextToken());
			String importer = filterIfMarkedArgument(parser.getNextToken());
			return readLinkedFile(path, importer, arguments, 0, fileDescriptionText);
		}
		else if (checker.compare(this.getClass(), "Reads a file as linked to a current file, so that they can share information", "[path to file][importer][failureText]", commandName, "linkFile")) {
			String path = filterIfMarkedArgument(parser.getFirstToken(arguments));
			String importer = filterIfMarkedArgument(parser.getNextToken());
			return readLinkedFile(path, importer, arguments, 0, null);
		}
		else if (checker.compare(this.getClass(), "Reads a file at a URL as linked to a current file, so that they can share information", "[URL]", commandName, "linkURL")) {
			MesquiteMessage.notifyUser("Sorry, can't yet read a linked URL");
			//	return (readLinkedURL(arguments));
		}
		else if (checker.compare(this.getClass(), "Reads a file and incorporates all of its information into the home file of the current project", "[path to file]", commandName, "includeFile")) {
			String path = filterIfMarkedArgument(parser.getFirstToken(arguments));
			String importer = filterIfMarkedArgument(parser.getNextToken());
			includeFile(path, importer, arguments, 0, null);
		}
		else if (checker.compare(this.getClass(), "Reads a file and incorporates all of its information into the home file of the current project", "[path to file]", commandName, "includeFileFuse")) {
			String path = filterIfMarkedArgument(parser.getFirstToken(arguments));
			String importer = filterIfMarkedArgument(parser.getNextToken());
			includeFileFuse(path, importer, arguments, 0, null);
		}
		else if (checker.compare(this.getClass(), "Reverts to saved by closing ALL files without saving then opening the home file of project", null, commandName, "revert")) {
			revertToSaved(true);
		}
		else if (checker.compare(this.getClass(), "Opens a NEXUS file and returns it for reading", "[path to file]", commandName, "getNEXUSFileForReading")) {
			return getNEXUSFileForReading(arguments, "Choose file to open");
		}
		else if (checker.compare(this.getClass(), "Opens a NEXUS file and returns the first block of a given type", "[block type][path to file]", commandName, "getNEXUSFileBlock")) {
			String blockType = parser.getFirstToken(arguments); //required argument
			String path = parser.getNextToken();//optional argument

			return getNEXUSBlock(blockType, path);
		}
		else if (checker.compare(this.getClass(), "Reads a file at a URL and incorporates all of its information into the home file of the current project", "[URL]", commandName, "includeURL")) {
			MesquiteMessage.notifyUser("Sorry, can't yet include a URL");
			//	return (readLinkedURL(arguments));
		}
		else if (checker.compare(this.getClass(), "Reads a tree file as linked to a current file, so that they can share information", "[path to file]", commandName, "linkTreeFile")) {
			String path = filterIfMarkedArgument(parser.getFirstToken(arguments));
			String importer = filterIfMarkedArgument(parser.getNextToken());
			String failureText = parser.getNextToken();
			return (readLinkedFile(path, importer, arguments, 1, failureText));
		}
		else if (checker.compare(this.getClass(), "Reads a tree file and incorporates all of its information into the home file of the current project", "[path to file]", commandName, "includeTreeFile")) {
			String path = filterIfMarkedArgument(parser.getFirstToken(arguments));
			String importer = filterIfMarkedArgument(parser.getNextToken());
			String failureText = parser.getNextToken();
			includeFile(path, importer, arguments, 1, failureText);
		}
		else if (checker.compare(this.getClass(), "Create a new linked file", "[path to file]", commandName, "newLinkedFile")) {
			MesquiteFile file = (newLinkedFile(parser.getFirstToken(arguments)));
			return file;
		}
		else if (checker.compare(this.getClass(), "Brings a current chart window to the fore", "[number of chart window]", commandName, "showChart")) {
			pos.setValue(0);
			int which = MesquiteInteger.fromString(arguments, pos);
			if ((which == 0 || MesquiteInteger.isPositive(which))) {
				ListableVector ws = MesquiteTrunk.windowVector;
				int count = 0;
				for (int i=0; i<ws.size(); i++) {
					Object obj = ws.elementAt(i);
					if (obj instanceof ChartWindow){
						if (which == count) {
							((ChartWindow)obj).show();
							return obj;
						}
						count++;
					}
				}
			}
			return null;

		}
		else if (checker.compare(this.getClass(), "Returns a reference to the file assistant module", "[name of module]", commandName, "getAssistant")) {
			MesquiteModule mb = findEmployeeWithName(parser.getFirstToken(arguments), true);
			return mb;
		}
		else if (checker.compare(this.getClass(), "Brings up the chart wizard", null, commandName, "chartWizard")) {

			if (!MesquiteThread.isScripting()) {
				showChartWizard(null);
			}

		}
		else if (checker.compare(this.getClass(), "Hires new file assistant module", "[name of module]", commandName, "newAssistant")) {
			return (FileAssistant)hireNamedEmployee(FileAssistant.class, arguments);
		}
		else if (checker.compare(this.getClass(), "Save all files in project", null, commandName, "saveFiles")) {  //all files in project
			String fileNames ="";
			if (proj == null)
				return null;
			for (int i = 0; i< proj.getNumberLinkedFiles(); i++){
				if (proj.getFile(i) != null)
					fileNames += "\n\n   " + proj.getFile(i).getFileName();
			}
			if (MesquiteThread.isScripting() || AlertDialog.query(containerOfModule(), "Save All Files?", "Do you want to save all of the following files?" + fileNames + "\n\n(If you want to save only one, go to the submenu of the File menu.)"))
				saveAllFiles();
		}
		else if (checker.compare(this.getClass(), "Saves file", "[number of file]", commandName, "saveFile")) {  //Single file only
			saveFile(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
		}
		else if (checker.compare(this.getClass(), "Saves home file", "[number of file]", commandName, "saveHomeFile")) {  //Single file only
			saveFile((int)getProject().getHomeFile().getID());
		}
		else if (checker.compare(this.getClass(), "Renames file", "[number of file]", commandName, "renameAndSaveFile")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			int i = MesquiteInteger.fromString(arguments, pos);
			String name = ParseUtil.getToken(arguments, pos);
			renameAndSaveFile(i, name);
		}
		else if (checker.compare(this.getClass(), "Saves file as... to create a separate copy", "[number of file]", commandName, "saveFileAs")) {
			saveFileAs(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
		}
		else if (checker.compare(this.getClass(), "Gives information about the project", null, commandName, "getInfo")) {
			String st = "This represents a project, which currently represents information from ";
			if (proj.getNumberLinkedFiles() == 1) {
				st = "The current file is " + proj.getHomeFileName();
				if (!StringUtil.blank(proj.getHomeDirectoryName()))
					st += ", located at " + proj.getHomeDirectoryName() + ".";
			}
			else {
				st = "The current project includes " + proj.getNumberLinkedFiles() + " files.  The home file of the project is " + proj.getHomeFileName();
				if (!StringUtil.blank(proj.getHomeDirectoryName()))
					st += ", located at " + proj.getHomeDirectoryName() + ".";
				String[] s = proj.getFiles().getStrings();


				st += "\n\nThe other files linked in the project are:\n";
				for (int i=1; i<s.length; i++)
					st += "     " + s[i] + "\n";
			}
			alert(st);
		}
		else if (checker.compare(this.getClass(), "Exports information to another format", null, commandName, "export")) {
			String exporterName = parser.getFirstToken(arguments);
			if (getProject() == null)
				return null;
			//which file?
			MesquiteFile file =null;
			int id = MesquiteInteger.fromString(parser.getNextToken());
			if (MesquiteInteger.isCombinable(id) && getProject().getNumberLinkedFiles()>1)
				file = getProject().getFileByID(id);
			else
				file = getProject().getHomeFile();
			if (file == null)
				return null;
			FileInterpreterI exporter = (FileInterpreterI)findEmployeeWithName(exporterName);
			if (exporter == null){
				MesquiteModule[] fInterpreters = getImmediateEmployeesWithDuty(FileInterpreterI.class);
				if (fInterpreters==null)
					return null;
				int count = 0;
				for (int i=0; i<fInterpreters.length; i++){
					try {
						if (((FileInterpreterI)fInterpreters[i]).canExportProject(getProject()))
							count++;
					}
					catch (Exception e){
					}
				}
				if (count == 0){
					discreetAlert( "Sorry, there are no exporters available that can handle the data types in this project.");
					return null;
				}
				MesquiteModule[] fInterpretersCanExport = new MesquiteModule[count];
				count = 0;
				for (int i=0; i<fInterpreters.length; i++){
					try {
						if (((FileInterpreterI)fInterpreters[i]).canExportProject(getProject()))
							fInterpretersCanExport[count++] = fInterpreters[i];
					}
					catch (Exception e){
					}
				}
				//prioritize list
				fInterpretersCanExport = prioritize(fInterpretersCanExport, FileInterpreterI.class);

				exporter = (FileInterpreterI)ListDialog.queryList(containerOfModule(), "Export format", "Export part or all of the information as a file of the following format",MesquiteString.helpString, fInterpretersCanExport, 0);
			}
			export(exporter, file, arguments);
		}
		else if (checker.compare(this.getClass(), "NOT FUNCTIONING", null, commandName, "saveLinkagesAs")) {
			saveLinkagesAs(parser.getFirstToken(arguments));
			//writeFile(false, null);
		}
		else if (checker.compare(this.getClass(), "Closes file", "[number of file]", commandName, "closeFile")) {
			closeFile(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
		}
		else if (checker.compare(this.getClass(), "Closes all tabs", null, commandName, "closeAllWindows")) {
			int safetyNet = 0;
			while (safetyNet < 50 && closeEmployeeWindows(this))
				safetyNet++;
		}
		else if (checker.compare(this.getClass(), "Show file on disk", "[number of file]", commandName, "showFileOnDisk")) {
			if (getProject() == null)
				return null;
			String path = "";
			int id = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			MesquiteFile fi = getProject().getFileByID(id);
			if (fi == null && getProject().getNumberLinkedFiles()==1)
				fi = getProject().getHomeFile();
			if (fi != null) {
				if (fi.isLocal()) {
					path = fi.getDirectoryName();
					MesquiteFile.showDirectory(path);
					return null;
				}
				else if (fi.getURL()!= null){

					path = fi.getURL().toString();
					showWebPage(path, false);
					return null;
				}
			}
		}
		else if (checker.compare(this.getClass(), "Closes file", "[name of file]", commandName, "closeFileByName")) {
			//query...
			closeFile(arguments);
		}
		else if (checker.compare(this.getClass(), "Closes this project (which may consist of a single open file)", null, commandName, "closeProject")) {
			MesquiteProject p=getProject();
			if (MesquiteThread.isScripting()){
				if (p != null)
					p.developing = false; //so that project closes!
			}
			if (p != null)
				closeFile(p.getHomeFile());
			if (!MesquiteThread.isScripting() && MesquiteTrunk.getProjectList().getNumProjects()==0){
				MesquiteWindow w = MesquiteTrunk.mesquiteTrunk.getModuleWindow();
				if (w!=null)
					w.setVisible(true);
			}
		}
		else if (checker.compare(this.getClass(), "Returns path to this project", null, commandName, "getPathToProject")) {
			return getProject().getHomeDirectoryName();
		}
		else if (checker.compare(this.getClass(), "Returns name of home file of this project", null, commandName, "getHomeFileName")) {
			return getProject().getHomeFileName();
		}
		else if (checker.compare(this.getClass(), "Returns this project", null, commandName, "getProject")) {
			return getProject();
		}
		else if (checker.compare(this.getClass(), "Returns identification number of this project", null, commandName, "getProjectID")) {
			return new MesquiteInteger((int)getProject().getID());
		}
		else if (checker.compare(this.getClass(), "Brings all of the windows belonging to the project to the front.", null, commandName, "allToFront")) {
			Enumeration e = MesquiteTrunk.mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteWindow mw = (MesquiteWindow)obj;
				if (mw.isVisible() && mw.getOwnerModule()!=null && mw.getOwnerModule().getProject()== getProject())
					mw.getParentFrame().toFront();
			}
		}
		else if (checker.compare(this.getClass(), "Not used; placed here to suppress warning that command \"end\" from Mesquite block not handled", null, commandName, "end")) { //this is end of MesquiteBlock; placed here to suppress warning that command not handled
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	void showChartWizard(String targetName){
		int availableChartTypes = 0;
		if (targetName != null){
			MesquiteModuleInfo mb = null;
			while ((mb = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule (FileAssistantCH.class, mb, null, null, this)) != null) {
				if (targetName.equalsIgnoreCase(mb.getNameForMenuItem()))
					availableChartTypes += 1;
			}
			mb = null;
			while ((mb = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule (FileAssistantCS.class, mb, null, null, this)) != null){
				if (targetName.equalsIgnoreCase(mb.getNameForMenuItem()))
					availableChartTypes += 4;
			}
		}
		else {
			String explanation = "<h3>Entities?</h3>First, each <strong>data point</strong> of your chart refers to an <strong>entity</strong>.  For example, each data point might represent a <strong>character</strong>, and tells us a particular value for the character."
					+ " Alternatively, the data points could refer to <strong>matrices</strong>, or <strong>taxa</strong>, or <strong>trees</strong>.  Thus, your chart will summarize values that apply to each of a series of different entities.  "
					+ "(This could seem confusing, because some data points refer to both a character and a tree, for example.  However, if the chart shows a value for a character in each of multiple trees, "
					+"then you want a Trees chart.  If it shows a value for each of many characters with a single tree, then you want a Characters chart.)";
			String question = "What are the entities to which the data points of this chart refer?";
			//
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Chart Wizard: Entities",  buttonPressed);
			//dialog.addLargeTextLabel(explanation);
			//dialog.addBlankLine();
			dialog.appendToHelpString(explanation);
			dialog.addLabel(question);

			Vector v = new Vector();
			Vector vW = new Vector();
			MesquiteModuleInfo mb = null;
			while ((mb = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule (FileAssistantCH.class, mb, null, null, this)) != null) {
				v.addElement(mb.getNameForMenuItem());
				vW.addElement(new MesquiteInteger(1));
			}
			mb = null;
			while ((mb = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule (FileAssistantCS.class, mb, null, null, this)) != null){
				String s = mb.getNameForMenuItem();
				int inS = v.indexOf(s);
				if (inS<0) {
					v.addElement(s);
					vW.addElement(new MesquiteInteger(4));
				}
				else if (inS < vW.size()) {
					MesquiteInteger mi = (MesquiteInteger)vW.elementAt(inS);
					mi.add(4); //will have value 5, there do both
				}
			}
			/*
			 * 	while ((mb = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule (FileAssistantCP.class, mb, null, null, this)) != null){
			String s = mb.getNameForMenuItem();
			if (v.indexOf(s)<0)
				v.addElement(s);
		}*/
			dialog.addBlankLine();
			String[] labels = new String[v.size()];
			for (int i = 0; i<v.size(); i++)
				labels[i] = (String)v.elementAt(i);
			RadioButtons radio = dialog.addRadioButtons(labels, 0);
			dialog.addBlankLine();

			dialog.completeAndShowDialog(true);

			boolean ok = (dialog.query()==0);
			if (radio.getValue() < v.size() && radio.getValue() < vW.size()){
				if (ok)
					targetName = (String)v.elementAt(radio.getValue());
				MesquiteInteger chartTypeAvailable = (MesquiteInteger)vW.elementAt(radio.getValue());
				availableChartTypes = chartTypeAvailable.getValue();
			}
		}
		if (!StringUtil.blank(targetName)) {
			int whichChart = -1;
			if (availableChartTypes== 1)//only histogram
				whichChart = 0;
			else if (availableChartTypes == 4)//only scattergram
				whichChart = 1;
			else {

				String explanation2 = "<h3>One or Two Dimensions?</h3>Second, your chart could display a <strong>single variable</strong> (among multiple entities) or the relationship between <strong>two variables</strong>.  A one-variable chart will be a histogram/bar chart/line chart."
						+" A two-variable chart will be a scatterplot or scattergram.";
				String question2 = "Do you want a one-variable or two-variable chart?";
				MesquiteInteger buttonPressed = new MesquiteInteger(1);
				ExtensibleDialog dialog2 = new ExtensibleDialog(containerOfModule(), "Chart Wizard: Dimensions",  buttonPressed);
				//dialog2.addLargeTextLabel(explanation2);
				dialog2.appendToHelpString(explanation2);
				dialog2.addLabel(question2);
				dialog2.addBlankLine();
				RadioButtons radio2 = dialog2.addRadioButtons(new String[]{"One (Bar chart, line chart, histogram)", "Two (Scattergram)"}, 0);
				dialog2.addBlankLine();
				dialog2.completeAndShowDialog(true);
				boolean ok = (dialog2.query()==0);
				if (ok)
					whichChart = radio2.getValue();
			}
			if (whichChart >=0){
				MesquiteModuleInfo mb = null;
				mb = null;
				String hireName = null;
				if (whichChart == 0){
					while (hireName == null && (mb = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule (FileAssistantCH.class, mb, null, null, this)) != null) 
						if (mb.getNameForMenuItem().equalsIgnoreCase(targetName)){
							hireName = "#" + mb.getClassName();

						}
				}
				else {
					while (hireName == null && (mb = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule (FileAssistantCS.class, mb, null, null, this)) != null) 
						if (mb.getNameForMenuItem().equalsIgnoreCase(targetName)){
							hireName = "#" + mb.getClassName();
						}
				}
				doCommand("newAssistant", hireName, CommandChecker.defaultChecker); 
			}
		}



	}
	/*.................................................................................................................*/
	/**Returns command to hire employee if clonable*/
	public String getClonableEmployeeCommand(MesquiteModule employee){
		if (employee!=null && employee.getEmployer()==this) {
			if (employee.getHiredAs()==FileAssistant.class)
				return ("newAssistant " + StringUtil.tokenize(employee.getName()) + ";"); //quote
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		String s =""; 
		if (getProject()!=null) 
			s += "Project name: " + getProject().getName();
		return s;
	}
	/*.................................................................................................................*/
	/** Returns the URL of document shown when splash screen icon touched. By default, returns path to module's manual*/
	public String getSplashURL(){
		return null; //return getPath() + "index.html"; 
	}
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
		return true; 
	}

	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
	public String getPackageName(){
		return "Basic Mesquite Package";
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String getPackageVersion() {
		return null;//this causes defult Mesquite info to be used
	}
	/*.................................................................................................................*/
	public String getPackageAuthors() {
		return null; //this causes defult Mesquite info to be used
	}
	/*.................................................................................................................*/
	public String getPackageDateReleased() {
		return null; //this causes defult Mesquite info to be used
	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
	public String getPackageCitation(){
		return null;//this causes default Mesquite info to be used
	}
	/*.................................................................................................................*/
	/** Returns version for a package of modules as an integer*/
	public int getPackageVersionInt(){
		return 0;//this causes default Mesquite info to be used
	}
	/*.................................................................................................................*/
	/** Returns build number for a package of modules as an integer*/
	public int getPackageBuildNumber(){
		return 0;//this causes default Mesquite info to be used
	}
	/*.................................................................................................................*/
	/** Returns the URL for the web page about the package*/
	public String getPackageURL(){
		return null;//this causes default Mesquite info to be used
	}
	/*.................................................................................................................*/
	/** Returns the integer version of Mesquite for which this package was first released*/
	public int getVersionOfFirstRelease(){
		return 0;//this causes default Mesquite info to be used
	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return true;
	}
	/*.................................................................................................................*/
	/** Requests a window to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		//Debug.println("disposing of window");
		whichWindow.hide();
		//whichWindow.dispose();
	}

	public boolean getHideable(){
		return false;
	}

}

/*======================================================================== */

/* ======================================================================== */
class FileRead implements CommandRecordHolder, Runnable {
	String arguments, path;
	CommandRecord comRec;
	BasicFileCoordinator ownerModule;
	ObjectContainer f;
	String importer;
	int category;
	int fileType;
	String fileDescriptionText = null;
	public FileRead (String path, String importer, String arguments, int fileType,  BasicFileCoordinator ownerModule, int category, ObjectContainer f, String filePurposeText) {
		this.f = f;
		this.arguments = arguments;
		this.path = path;
		this.comRec =  MesquiteThread.getCurrentCommandRecord();
		this.fileDescriptionText = filePurposeText;

		this.ownerModule = ownerModule;
		this.category = category;
		this.importer = importer;
		this.fileType = fileType;
		//setCurrent(1);
	}
	/*
	public MesquiteFile getFile(){
	 	return linkedFile;
	}
	 */
	public CommandRecord getCommandRecord(){

		return comRec;
	}
	public void setCommandRecord(CommandRecord c){

		comRec = c;
	}
	public String getCurrentCommandName(){
		return "Reading file";
	}
	public String getCurrentCommandExplanation(){
		return null;
	}

	private void warnLinkedNotFound(String pathName){
		String warning = "";
		if (fileDescriptionText != null){
			warning = fileDescriptionText + " cannot be found. The file was expected by a script to be at the location \"" + pathName + "\". " +
					"Because this file cannot be found, the remainder of the script might not execute properly, and various warning messages might be given.";
		}
		else
			warning = "A script refers to a linked file \"" + pathName + "\" that cannot be found. Because this linked file cannot be found, the remainder of the script might not " +
					"execute properly, and various warning messages might be given.  A possible cause of this is that someone saved a file with a second file linked, and then separated the two files." + 
					" To avoid this problem in the future, either unlink files before saving, or maintain the linked file in the same relative position (e.g., in the same directory).";

		ownerModule.alert(warning);
	}
	/*.................................................................................................................*/
	public MesquiteFile readLinkedFile(String pathName){ //make new/read new linked file//TODO: should say if scripting
		MesquiteFile linkedFile;
		if (ownerModule == null || ownerModule.getProject() == null)
			return null;
		ownerModule.incrementMenuResetSuppression();
		if (StringUtil.blank(pathName)) {
			String message = "Link file to existing project:";
			if (category == MesquiteFile.INCLUDED)
				message = "Include file into existing file:";
			linkedFile =MesquiteFile.open(true, (String)null, message, ownerModule.getProject().getHomeDirectoryName());
		}
		else if (pathName.indexOf(MesquiteFile.fileSeparator)<0) {
			if (MesquiteThread.isScripting() && !MesquiteFile.fileExists(ownerModule.getProject().getHomeDirectoryName(), pathName))
				warnLinkedNotFound(pathName);
			linkedFile =MesquiteFile.open(ownerModule.getProject().getHomeDirectoryName(), pathName);
		}
		else {
			String dirName = StringUtil.getAllButLastItem(pathName, MesquiteFile.fileSeparator, "/") + MesquiteFile.fileSeparator;
			String fileName = StringUtil.getLastItem(pathName, MesquiteFile.fileSeparator, "/");
			if (MesquiteThread.isScripting() && !MesquiteFile.fileExists(dirName, fileName))
				warnLinkedNotFound(pathName);
			linkedFile =MesquiteFile.open(dirName, fileName);
		}
		boolean imp = false;
		boolean wasTranslated = false;

		MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
		if (linkedFile!=null) {
			linkedFile.fileReadingArguments = arguments;
			Parser parser = new Parser();
			if (parser.tokenIndexOfIgnoreCase(arguments, "useStandardizedTaxonNames")>=0)
				linkedFile.useStandardizedTaxonNames = true;
			int tI = parser.tokenIndexOfIgnoreCase(arguments, "taxa");
			if (tI>=0){
				String ref = parser.getTokenNumber(arguments, tI+2);
				Taxa taxa = ownerModule.getProject().getTaxa(ref);
				linkedFile.setCurrentTaxa(taxa);
			}
			linkedFile.setReadCategory(category);
			if (f!=null)
				f.setObject(linkedFile);
			ownerModule.getProject().addFile(linkedFile);
			linkedFile.setProject(ownerModule.getProject());
			FileInterpreter fileInterp =null;
			if (!StringUtil.blank(importer))
				fileInterp = (FileInterpreter)ownerModule.findEmployeeWithName(importer);
			NexusFileInterpreter nfi = (NexusFileInterpreter)ownerModule.findImmediateEmployeeWithDuty(NexusFileInterpreter.class);
			if (fileInterp!=null) {
				if (fileInterp != nfi)
					imp = true;
			}
			else if (nfi!=null && nfi.canReadFile(linkedFile)) {
				fileInterp = nfi;
			}
			else if (!StringUtil.blank(importer)) {
				MesquiteModule mb = ownerModule.findEmployeeWithName(importer);
				if (mb instanceof FileInterpreter) {
					fileInterp = (FileInterpreter)mb;
					imp = true;
				}
			}
			else {

				fileInterp = ownerModule.findImporter(linkedFile, fileType, arguments);
				imp = true;
			}
			if (fileInterp !=null) {
				if (!(fileInterp instanceof NEXUSInterpreter))
					wasTranslated = true;
				MesquiteFile sf = CommandRecord.getScriptingFileS();

				CommandRecord.setScriptingFileS(linkedFile);
				if (ownerModule.pw != null)
					ownerModule.pw.suppress();

				if (fileType == 0 || fileInterp == nfi)
					fileInterp.readFile(ownerModule.getProject(), linkedFile, arguments);
				else 
					((FileInterpreterITree)fileInterp).readTreeFile(ownerModule.getProject(), linkedFile, arguments);
				if (ownerModule.pw != null) {
					ownerModule.pw.resume();
					ownerModule.pw.getParentFrame().setAsFrontWindow(ownerModule.getProject().activeWindowOfProject);
				}
				CommandRecord.setScriptingFileS(sf);
				linkedFile.fileReadingArguments = null;
				ownerModule.broadcastFileRead(ownerModule, linkedFile);

			}
			else {
				ownerModule.alert("Sorry, an interpreter was not found for this file");
				ownerModule.getProject().removeFile(linkedFile);
				ownerModule.decrementMenuResetSuppression();
				MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
				linkedFile.fileReadingArguments = null;
				return null;
			}
		}
		else {
			ownerModule.decrementMenuResetSuppression();
			MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
			return null;
		}

		if (category == MesquiteFile.INCLUDED){
			ownerModule.getProject().mergeFile(linkedFile);
		}
		else if (!linkedFile.isClosed()) {
			ownerModule.getProject().fileSaved(linkedFile);
			Parser parser = new Parser();
			//For linked files, no subclasses of interpreter can be specified in advance, and thus NEXUS files will be automatically read by InterpretNexus
			if (imp && wasTranslated && parser.tokenIndexOfIgnoreCase(arguments, "suppressImportFileSave")<0){//was imported; change name
				linkedFile.changeLocation(linkedFile.getDirectoryName(), linkedFile.getFileName()+".nex");  
				if (MesquiteThread.isScripting() || linkedFile.changeLocation("Save imported linked file as NEXUS file"))
					ownerModule.writeFile(linkedFile);  		

				else {
					linkedFile.close();
				}
			}
		}
		ownerModule.resetAllMenuBars();
		ownerModule.getProject().refreshProjectWindow();
		MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
		ownerModule.decrementMenuResetSuppression();
		if (linkedFile !=null && linkedFile.getCloseAfterReading()){
			ownerModule.closeFile(linkedFile);
			return null;
		}
		return linkedFile;
	}
	/** DOCUMENT */
	public void run() {
		MesquiteThread.numFilesBeingRead++;
		try {
			if (ownerModule.getProject() != null)
				ownerModule.getProject().incrementProjectWindowSuppression();

			readLinkedFile(path);
			if (ownerModule.getProject() != null)
				ownerModule.getProject().decrementProjectWindowSuppression();

		}
		catch (Exception e){
			MesquiteFile.throwableToLog(this, e);
			MesquiteTrunk.mesquiteTrunk.alert("File reading could not be completed because an exception or error occurred (i.e. a crash; " + e.getClass() + "). If you save any files, you might best use Save As... in case data were lost or file saving doesn't work properly. To report this as a bug, PLEASE send along the Mesquite_Log file from Mesquite_Support_Files.");

		}
		catch (Error e){
			if (e instanceof OutOfMemoryError)
				MesquiteMessage.println("OutofMemoryError.  See file startingMesquiteAndMemoryAllocation.txt in the Mesquite_Folder for information on how to increase memory allocated to Mesquite.");
			if (!(e instanceof ThreadDeath)){
				MesquiteDialog.closeWizard();
				MesquiteFile.throwableToLog(this, e);
				MesquiteTrunk.mesquiteTrunk.alert("File reading could not be completed because an exception or error occurred (i.e. a crash; " + e.getClass() + "). If you save any files, you might best use Save As... in case data were lost or file saving doesn't work properly. To report this as a bug, PLEASE send along the Mesquite_Log file from Mesquite_Support_Files.");
			}
			MesquiteThread.numFilesBeingRead--;
			throw e;
		}
		MesquiteThread.numFilesBeingRead--;
	}

}

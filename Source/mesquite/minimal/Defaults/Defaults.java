/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.Defaults;
/*~~  */

import java.awt.Font;

import mesquite.externalCommunication.lib.PythonUtil;
import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteException;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.RandomBetween;
import mesquite.lib.ShellScriptUtil;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.CharacterSource;
import mesquite.lib.duties.DefaultsAssistant;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.MesquiteInit;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.FontUtil;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MQComponentHelper;
import mesquite.lib.ui.MesquiteDialog;
import mesquite.lib.ui.MesquiteFrame;
import mesquite.lib.ui.MesquiteSubmenu;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.parsimony.lib.ParsAncStatesForModel;

/** Controls some of Mesquite's default settings (like fonts in windows). */

public class Defaults extends MesquiteInit  {
	/*.................................................................................................................*/
	public String getName() {
		return "Defaults";
	}
	public String getExplanation() {
		return "Supervises some Mesquite-wide defaults";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(DefaultsAssistant.class, "Modules are used to assist with setting defaults.",
				"The defaults are presented in the Defaults submenu of the File menu.");
	}
	/*.................................................................................................................*/
	MesquiteBoolean respectFileSpecificResourceWidth, useOtherChoices, console, askSeed, useReports, permitXOR;
	MesquiteBoolean taxonTruncTrees, permitSpaceUnderscoreEquivalentTrees, printTreeNameByDefault, protectGraphics;
	MesquiteBoolean tabbedWindows, debugMode, wizards, logAll, phoneHome, secondaryChoicesOnInDialogs, subChoicesOnInDialogs, tilePopouts; 
	MesquiteString themeName;
	StringArray themes;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		useOtherChoices = new MesquiteBoolean(false);
		askSeed = new MesquiteBoolean(false);
		console = new MesquiteBoolean(MesquiteTrunk.mesquiteTrunk.logWindow.isConsoleMode());
		logAll = new MesquiteBoolean(MesquiteCommand.logEverything);
		permitXOR = new MesquiteBoolean(GraphicsUtil.permitXOR);
		protectGraphics = new MesquiteBoolean(MQComponentHelper.protectGraphics);
		wizards = new MesquiteBoolean(MesquiteDialog.useWizards);
		tabbedWindows = new MesquiteBoolean(MesquiteWindow.compactWindows);
		//tilePopouts = new MesquiteBoolean(MesquiteFrame.popIsTile);
		taxonTruncTrees = new MesquiteBoolean(MesquiteTree.permitTruncTaxNames);
		permitSpaceUnderscoreEquivalentTrees = new MesquiteBoolean(MesquiteTree.permitSpaceUnderscoreEquivalent);
		printTreeNameByDefault = new MesquiteBoolean(TreeDisplay.printTreeNameByDefault);
		debugMode = new MesquiteBoolean(false);
		phoneHome = new MesquiteBoolean(MesquiteTrunk.phoneHome);
		useReports = new MesquiteBoolean(MesquiteTrunk.reportUse);
		secondaryChoicesOnInDialogs = new MesquiteBoolean(true);
		subChoicesOnInDialogs = new MesquiteBoolean(true);
		respectFileSpecificResourceWidth = new MesquiteBoolean(MesquiteFrame.respectFileSpecificResourceWidth);
		ParsAncStatesForModel.countStepsInTermPolymorphisms = new MesquiteBoolean(true);

		//useDotPrefs = new MesquiteBoolean(MesquiteModule.prefsDirectory.toString().indexOf(".Mesquite_Prefs")>=0);
		loadPreferences();
		EmployerEmployee.useOtherChoices = useOtherChoices.getValue();
		EmployerEmployee.secondaryChoicesOnInDialogs = secondaryChoicesOnInDialogs.getValue();
		EmployerEmployee.subChoicesOnInDialogs = subChoicesOnInDialogs.getValue();
		RandomBetween.askSeed = askSeed.getValue();
		MesquiteTrunk.defaultsSubmenu = MesquiteTrunk.mesquiteTrunk.addSubmenu(MesquiteTrunk.fileMenu, "Defaults");
		MesquiteTrunk.defaultsSubmenu.setFilterable(false);
		MesquiteTrunk.setupSubmenu = MesquiteTrunk.mesquiteTrunk.addSubmenu(MesquiteTrunk.fileMenu, "Setup");
		MesquiteTrunk.setupSubmenu.setFilterable(false);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Open Windows as Tabs", makeCommand("toggleTabbedWindows",  this), tabbedWindows);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Use Wizard-style Dialogs", makeCommand("toggleWizards",  this), wizards);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Contact Mesquite Server on Startup", makeCommand("togglePhoneHome",  this), phoneHome);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Send Error Reports Automatically to Mesquite Server", makeCommand("toggleErrorReports",  this), MesquiteException.reportErrorsAutomatically);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Send Usage Reports to Mesquite Server", makeCommand("toggleUseReports",  this), useReports);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Use Log Window for Commands", makeCommand("toggleConsoleMode",  this), console);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Log All Commands", makeCommand("toggleLogAll",  this), logAll);
		MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Previous Logs Saved...", makeCommand("setNumPrevLog",  this));


		MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "-", null);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Use \"Other Choices\" for Secondary Choices", makeCommand("toggleOtherChoices",  this), useOtherChoices);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Secondary Choices Shown By Default in Dialog Boxes", makeCommand("toggleSecondaryChoicesOnInDialogs",  this), secondaryChoicesOnInDialogs);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Show Subchoices in Module Dialogs", makeCommand("toggleSubChoicesOnInDialogs",  this), subChoicesOnInDialogs);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Use Stored Characters/Matrices by Default", makeCommand("toggleStoredAsDefault",  this), CharacterSource.storedAsDefault);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Close Calculations if Matrices Used are Deleted (req. restart)", makeCommand("toggleCloseIfMatrixDeleted",  this), CharacterSource.closeIfMatrixDeleted);
		MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Delay on Script File Recovery...", makeCommand("setDelayScriptFileRecovery",  this));
		//		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Close Tree Window if Tree Block Used is Deleted", makeCommand("toggleCloseIfTreeBlockDeleted",  this), TreeSource.closeIfTreeBlockDeleted);
		MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "-", null);
		if (!MesquiteTrunk.isMacOSX())
			MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Forget Default Web Browser", makeCommand("forgetBrowser",  this));
		themes = ColorTheme.getColorThemes();
		MesquiteSubmenuSpec mTheme = MesquiteTrunk.mesquiteTrunk.addSubmenu(MesquiteTrunk.defaultsSubmenu, "Color Theme", makeCommand("setColorTheme",  this), themes);
		themeName = new MesquiteString(themes.getValue(ColorTheme.THEME));  //this helps the menu keep track of checkmenuitems
		mTheme.setSelected(themeName);


		MesquiteSubmenuSpec sm = FontUtil.getFontSubmenuSpec(MesquiteTrunk.defaultsSubmenu, "Default Font", MesquiteTrunk.mesquiteTrunk,this);
		sm.setFilterable(false);

		sm = MesquiteTrunk.mesquiteTrunk.addSubmenu(MesquiteTrunk.defaultsSubmenu,"Default Font Size", makeCommand("setDefaultFontSize",  this), MesquiteSubmenu.getFontSizeList());		
		sm.setFilterable(false);
		MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "-", null);

		MesquiteTrunk.mesquiteTrunk.addMenuItem(MesquiteTrunk.defaultsSubmenu, "Max # Matrices/Tree Blocks Listed in Project Panel...", makeCommand("setProjectPanelMaxItemsSeparate",  this));
		//MesquiteTrunk.mesquiteTrunk.addMenuItem(MesquiteTrunk.defaultsSubmenu,"Overall Max # Items in Project Panel...", makeCommand("setProjectPanelMaxItems",  this));		
		sm = MesquiteTrunk.mesquiteTrunk.addSubmenu(MesquiteTrunk.defaultsSubmenu,"Project Panel Font Size", makeCommand("setProjectPanelFontSize",  this), MesquiteSubmenu.getFontSizeList());		
		sm.setFilterable(false);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Use File-Specific Project Panel Width", makeCommand("respectFileSpecificResourceWidth",  this), respectFileSpecificResourceWidth);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Permit Inverted Highlights (XORMode)", makeCommand("toggleXORMode",  this), permitXOR);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Thread-protect Graphics", makeCommand("protectGraphics",  this), protectGraphics);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Ask for Random Number Seeds", makeCommand("toggleAskSeed",  this), askSeed);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Count Steps in Polymorphisms with Unord/Ord Parsimony", makeCommand("toggleCountStepsInTermPolymorphisms",  this), ParsAncStatesForModel.countStepsInTermPolymorphisms);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Permit Partial Names in Tree Reading", makeCommand("togglePartNamesTrees",  this), taxonTruncTrees);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Permit Spaces and Underscores Equivalent in Tree Reading", makeCommand("toggleSpaceUnderscoreTrees",  this), permitSpaceUnderscoreEquivalentTrees);
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Print Tree Names by Default", makeCommand("printTreeNameByDefault",  this), printTreeNameByDefault);
		MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Matrix Limits for Undo...", makeCommand("setMaxMatrixSizeUndo",  this));
		MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, "Python Settings...", makeCommand("pythonSettings",  this));

		//comment out debug mode menu item for users 
		MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Debug Mode", makeCommand("toggleDebugMode",  this), debugMode);
		MesquiteTrunk.mesquiteTrunk.addMenuItem(MesquiteTrunk.fileMenu, "-", null);


		hireAllEmployees(DefaultsAssistant.class);
		return true;
	}

	public void endJob(){
		
		storePreferences();
		super.endJob();
	}

	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>1) {
			int fontSize = MesquiteInteger.fromString(prefs[1]);
			if (MesquiteInteger.isCombinable(fontSize)) {
				Font fontToSet = new Font (prefs[0], MesquiteWindow.defaultFont.getStyle(), fontSize);
				if (fontToSet!= null) {
					MesquiteWindow.defaultFont = fontToSet;
				}
			}
			//if (prefs.length>2 && prefs[2] !=null) //post 2. 01 changed name to switch factory default to false
			//		useOtherChoices.setValue("useOther".equalsIgnoreCase(prefs[2]));
			if (prefs.length>3 && prefs[3] !=null)
				MesquiteTrunk.setSuggestedDirectory(prefs[3]);
			if (prefs.length>4 && prefs[4] !=null)
				askSeed.setValue("askSeed".equalsIgnoreCase(prefs[4]));
			if (prefs.length>5 && prefs[5] !=null){
					permitXOR.setValue("permitXOR".equalsIgnoreCase(prefs[5]));
					GraphicsUtil.permitXOR = permitXOR.getValue();
			}
			if (prefs.length>6 && prefs[6] !=null){
				taxonTruncTrees.setValue("permitTruncTaxonNamesTrees".equalsIgnoreCase(prefs[6]));
				MesquiteTree.permitTruncTaxNames = taxonTruncTrees.getValue();
			}
			if (prefs.length>7 && prefs[7] !=null){
				tabbedWindows.setValue("tabbed".equalsIgnoreCase(prefs[7]));
				MesquiteWindow.compactWindows = tabbedWindows.getValue();
			}
			if (prefs.length>8 && prefs[8] !=null){
				debugMode.setValue("debug".equalsIgnoreCase(prefs[8]));
				MesquiteTrunk.debugMode = debugMode.getValue();
			}
			if (prefs.length>9 && prefs[9] !=null){
				wizards.setValue("useWizards".equalsIgnoreCase(prefs[9]));
				MesquiteDialog.useWizards = wizards.getValue();
			}
			if (prefs.length>10 && prefs[10] !=null){
				phoneHome.setValue("phoneHome".equalsIgnoreCase(prefs[10]));
				MesquiteTrunk.phoneHome = phoneHome.getValue();
			}
			if (prefs.length>11 && prefs[11] !=null)
				secondaryChoicesOnInDialogs.setValue("secondaryChoicesOnInDialogs".equalsIgnoreCase(prefs[11]));
		}
	}

	public void processSingleXMLPreference (String tag, String content) {
		if ("useOtherChoicesInMenus".equalsIgnoreCase(tag)){  //post 2. 01 changed name to switch factory default to false
			useOtherChoices.setValue(content);
			EmployerEmployee.useOtherChoices = useOtherChoices.getValue();
		}
		else if ("askSeed".equalsIgnoreCase(tag)){
			askSeed.setValue(content);
			RandomBetween.askSeed = askSeed.getValue();
		}
		else if ("countStepsInTermPolymorphisms".equalsIgnoreCase(tag)){
			ParsAncStatesForModel.countStepsInTermPolymorphisms.setValue(content);
		}
		else if ("permitXOR".equalsIgnoreCase(tag)){
				permitXOR.setValue(content);
				GraphicsUtil.permitXOR = permitXOR.getValue();
		}
		else if ("protectGraphics".equalsIgnoreCase(tag)){
			protectGraphics.setValue(content);
				MQComponentHelper.protectGraphics = protectGraphics.getValue();
		}
		else if ("taxonTruncTrees".equalsIgnoreCase(tag)){
			taxonTruncTrees.setValue(content);
			MesquiteTree.permitTruncTaxNames = taxonTruncTrees.getValue();
		}
		else if ("permitSpaceUnderscoreEquivalentTrees".equalsIgnoreCase(tag)){
			permitSpaceUnderscoreEquivalentTrees.setValue(content);
			MesquiteTree.permitSpaceUnderscoreEquivalent = permitSpaceUnderscoreEquivalentTrees.getValue();
		}
		else if ("printTreeNameByDefault".equalsIgnoreCase(tag)){
			printTreeNameByDefault.setValue(content);
			TreeDisplay.printTreeNameByDefault = printTreeNameByDefault.getValue();
		}
		else if ("maxLinesOfAnyElementInPanelQueried".equalsIgnoreCase(tag)){
			MesquiteBoolean q = new MesquiteBoolean();
			q.setValue(content);
			FileCoordinator.maxLinesOfAnyElementInPanelQueried = q.getValue();
		}
		else if ("maxLinesOfAnyElementInPanel".equalsIgnoreCase(tag)) {
			int m = MesquiteInteger.fromString(content);
			FileCoordinator.maxLinesOfAnyElementInPanel = m;
		}
		else if ("maxLinesOfMatricesTreeBlocksSeparateInPanel".equalsIgnoreCase(tag)) {
			int m = MesquiteInteger.fromString(content);
			FileCoordinator.maxLinesOfMatricesTreeBlocksSeparateInPanel = m;
		}
		/*	else if ("tilePopouts".equalsIgnoreCase(tag)){
			tilePopouts.setValue(content);
			MesquiteFrame.popIsTile = tilePopouts.getValue();
		}*/
		else if ("tabbedWindows".equalsIgnoreCase(tag)){
			tabbedWindows.setValue(content);
			MesquiteWindow.compactWindows = tabbedWindows.getValue();
		}
		else if ("respectFileSpecificResourceWidth".equalsIgnoreCase(tag)){
			respectFileSpecificResourceWidth.setValue(content);
			MesquiteFrame.respectFileSpecificResourceWidth = respectFileSpecificResourceWidth.getValue();
		}
		else if ("debugMode".equalsIgnoreCase(tag)){
			debugMode.setValue(content);
			MesquiteTrunk.debugMode = debugMode.getValue();
		}
		else if ("wizards".equalsIgnoreCase(tag)){
			wizards.setValue(content);
			MesquiteDialog.useWizards = wizards.getValue();
		}
		else if ("phoneHome".equalsIgnoreCase(tag)){
			phoneHome.setValue(content);
			MesquiteTrunk.phoneHome = phoneHome.getValue();
		}
		else if ("autoErrorReports".equalsIgnoreCase(tag)){
			MesquiteException.reportErrorsAutomatically.setValue(content);
		}
		else if ("useReports".equalsIgnoreCase(tag)){
			useReports.setValue(content);
			MesquiteTrunk.reportUse= useReports.getValue();
		}
		else if ("showSecondaryChoicesInDialogs".equalsIgnoreCase(tag)){//post 2. 01 changed name to switch factory default to true
			secondaryChoicesOnInDialogs.setValue(content);
			EmployerEmployee.secondaryChoicesOnInDialogs = secondaryChoicesOnInDialogs.getValue();
		}
		else if ("consoleMode".equalsIgnoreCase(tag)){
			console.setValue(content);
			MesquiteTrunk.mesquiteTrunk.logWindow.setConsoleMode(console.getValue());
		}
		else if ("subChoicesOnInDialogs".equalsIgnoreCase(tag)){
			subChoicesOnInDialogs.setValue(content);
			EmployerEmployee.subChoicesOnInDialogs = subChoicesOnInDialogs.getValue();
		}
		else if ("storedAsDefault".equalsIgnoreCase(tag)){
			CharacterSource.storedAsDefault.setValue(content);
		}
		else if ("closeIfMatrixDeleted".equalsIgnoreCase(tag)){
			CharacterSource.closeIfMatrixDeleted.setValue(content);
		}
		/*
		 * else if ("closeIfTreeBlockDeleted".equalsIgnoreCase(tag)){
			TreeSource.closeIfTreeBlockDeleted.setValue(content);
		}*/
		else if ("defaultFont".equalsIgnoreCase(tag)){
			String defFont = StringUtil.cleanXMLEscapeCharacters(content);
			Font fontToSet = new Font (defFont, MesquiteWindow.defaultFont.getStyle(), MesquiteWindow.defaultFont.getSize());
			if (fontToSet!= null) 
				MesquiteWindow.defaultFont = fontToSet;
		}
		else if ("defaultFontSize".equalsIgnoreCase(tag)) {
			int defFontSize = MesquiteInteger.fromString(content);
			Font fontToSet = new Font (MesquiteWindow.defaultFont.getName(), MesquiteWindow.defaultFont.getStyle(), defFontSize);
			if (fontToSet!= null) 
				MesquiteWindow.defaultFont = fontToSet;
		}
		else if ("resourcesFontSize".equalsIgnoreCase(tag)) {
			int defFontSize = MesquiteInteger.fromString(content);
			if (MesquiteInteger.isCombinable(defFontSize)) 
				MesquiteFrame.resourcesFontSize = defFontSize;
		}
		else if ("suggestedDirectory".equalsIgnoreCase(tag)){
			MesquiteTrunk.setSuggestedDirectory(StringUtil.cleanXMLEscapeCharacters(content));
		}
		else if ("python2Path".equalsIgnoreCase(tag)){
			PythonUtil.python2Path = StringUtil.cleanXMLEscapeCharacters(content);
		}
		else if ("python3Path".equalsIgnoreCase(tag)){
			PythonUtil.python3Path = StringUtil.cleanXMLEscapeCharacters(content);
		}
	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "defaultFont", MesquiteWindow.defaultFont.getName());  
		StringUtil.appendXMLTag(buffer, 2, "defaultFontSize", MesquiteWindow.defaultFont.getSize());
		StringUtil.appendXMLTag(buffer, 2, "resourcesFontSize", MesquiteFrame.resourcesFontSize);
		StringUtil.appendXMLTag(buffer, 2, "maxLinesOfAnyElementInPanel", FileCoordinator.maxLinesOfAnyElementInPanel);
		StringUtil.appendXMLTag(buffer, 2, "maxLinesOfAnyElementInPanelQueried", FileCoordinator.maxLinesOfAnyElementInPanelQueried);
		StringUtil.appendXMLTag(buffer, 2, "maxLinesOfMatricesTreeBlocksSeparateInPanel", FileCoordinator.maxLinesOfMatricesTreeBlocksSeparateInPanel);
		StringUtil.appendXMLTag(buffer, 2, "useOtherChoicesInMenus", useOtherChoices);   
		StringUtil.appendXMLTag(buffer, 2, "suggestedDirectory", MesquiteTrunk.getSuggestedDirectory());
		StringUtil.appendXMLTag(buffer, 2, "python2Path", PythonUtil.python2Path);
		StringUtil.appendXMLTag(buffer, 2, "python3Path", PythonUtil.python3Path);
		StringUtil.appendXMLTag(buffer, 2, "askSeed", askSeed);   
		StringUtil.appendXMLTag(buffer, 2, "permitXOR", permitXOR);   
		StringUtil.appendXMLTag(buffer, 2, "protectGraphics", protectGraphics);   
		StringUtil.appendXMLTag(buffer, 2, "countStepsInTermPolymorphisms", ParsAncStatesForModel.countStepsInTermPolymorphisms);  
		StringUtil.appendXMLTag(buffer, 2, "taxonTruncTrees", taxonTruncTrees);   
		StringUtil.appendXMLTag(buffer, 2, "permitSpaceUnderscoreEquivalentTrees", permitSpaceUnderscoreEquivalentTrees);   
		StringUtil.appendXMLTag(buffer, 2, "printTreeNameByDefault", printTreeNameByDefault);   
		StringUtil.appendXMLTag(buffer, 2, "tabbedWindows", tabbedWindows);   
		StringUtil.appendXMLTag(buffer, 2, "respectFileSpecificResourceWidth", respectFileSpecificResourceWidth);   
		StringUtil.appendXMLTag(buffer, 2, "tilePopouts", tilePopouts);   
		StringUtil.appendXMLTag(buffer, 2, "debugMode", debugMode);   
		StringUtil.appendXMLTag(buffer, 2, "wizards", wizards);   
		StringUtil.appendXMLTag(buffer, 2, "phoneHome", phoneHome);   
		StringUtil.appendXMLTag(buffer, 2, "autoErrorReports", MesquiteException.reportErrorsAutomatically);   
		StringUtil.appendXMLTag(buffer, 2, "useReports", useReports);   
		StringUtil.appendXMLTag(buffer, 2, "showSecondaryChoicesInDialogs", secondaryChoicesOnInDialogs);   
		StringUtil.appendXMLTag(buffer, 2, "subChoicesOnInDialogs", subChoicesOnInDialogs);   
		StringUtil.appendXMLTag(buffer, 2, "consoleMode", console);   
		StringUtil.appendXMLTag(buffer, 2, "storedAsDefault", CharacterSource.storedAsDefault);   
		StringUtil.appendXMLTag(buffer, 2, "closeIfMatrixDeleted", CharacterSource.closeIfMatrixDeleted);   
		//StringUtil.appendXMLTag(buffer, 2, "closeIfTreeBlockDeleted", TreeSource.closeIfTreeBlockDeleted);   
		return buffer.toString();
	}
	/*.................................................................................................................*
	public String[] preparePreferencesForFile () {
		String[] prefs= new String[12];
		prefs[0] = MesquiteWindow.defaultFont.getName();
		prefs[1] = Integer.toString(MesquiteWindow.defaultFont.getSize());
		if (useOtherChoices.getValue())
			prefs[2] = "useOther";
		else
			prefs[2] = "dontUseOther";
		prefs[3] = MesquiteTrunk.suggestedDirectory;
		if (askSeed.getValue())
			prefs[4] = "askSeed";
		else
			prefs[4] = "dontAskSeed";
		if (xorMode.getValue())
			prefs[5] = "suppressXORMode";
		else
			prefs[5] = "useXORMode";
		if (taxonTruncTrees.getValue())
			prefs[6] = "permitTruncTaxonNamesTrees";
		else
			prefs[6] = "noTruncTaxonNamesTrees";
		if (tabbedWindows.getValue())
			prefs[7] = "tabbed";
		else
			prefs[7] = "noTabbed";
		if (debugMode.getValue())
			prefs[8] = "debug";
		else
			prefs[8] = "noDebug";
		if (wizards.getValue())
			prefs[9] = "useWizards";
		else
			prefs[9] = "noUseWizards";
		if (phoneHome.getValue())
			prefs[10] = "phoneHome";
		else
			prefs[10] = "DontPhoneHome";
		if (secondaryChoicesOnInDialogs.getValue())
			prefs[11] = "secondaryChoicesOnInDialogs";
		else
			prefs[11] = "secondaryChoicesOffInDialogs";
		return prefs;
	}



	/*.................................................................................................................*/
	/** Respond to commands sent to the window. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets the default font of windows", "[name of font]", commandName, "setFont")) {
			if (MesquiteWindow.defaultFont==null)
				return null;
			Font fontToSet = new Font (parser.getFirstToken(arguments), MesquiteWindow.defaultFont.getStyle(), MesquiteWindow.defaultFont.getSize());
			if (fontToSet!= null) {
				MesquiteWindow.defaultFont = fontToSet;
				storePreferences();
			}
		}
		else if (checker.compare(getClass(), "Sets the default font of windows", "[name of font]", commandName, FontUtil.setFontOther)) {
			if (MesquiteWindow.defaultFont==null)
				return null;
			String fontName = FontUtil.getFontNameFromDialog(containerOfModule());
			if (fontName!=null) {
				logln("Set default font to " + fontName);
				Font fontToSet = new Font (fontName, MesquiteWindow.defaultFont.getStyle(), MesquiteWindow.defaultFont.getSize());
				if (fontToSet!= null) {
					MesquiteWindow.defaultFont = fontToSet;
					storePreferences();
				}
			}
		}
		else if (checker.compare(getClass(), "Sets the default font size of windows", "[font size]", commandName, "setDefaultFontSize")) {
			if (MesquiteWindow.defaultFont==null)
				return null;
			int fontSize = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0));
			if (!MesquiteInteger.isCombinable(fontSize))
				fontSize = MesquiteInteger.queryInteger(containerOfModule(), "Font size", "Font size for window", MesquiteWindow.defaultFont.getSize(), 4, 256);
			if (!MesquiteInteger.isCombinable(fontSize))
				return null;
			Font fontToSet = new Font (MesquiteWindow.defaultFont.getName(), MesquiteWindow.defaultFont.getStyle(), fontSize);
			if (fontToSet!= null) {
				MesquiteWindow.defaultFont = fontToSet;
				storePreferences();
			}
		}
		else if (checker.compare(getClass(), "Sets the color theme", null, commandName, "setColorTheme")) {
			String name = parser.getFirstToken(arguments); //get argument passed of option chosen
			int newMode = themes.indexOf(name); //see if the option is recognized by its name
			if (newMode >=0 && newMode!=ColorTheme.THEME_FOR_NEXT_STARTUP){
				ColorTheme.THEME_FOR_NEXT_STARTUP = newMode; //change mode
				MesquiteTrunk.mesquiteTrunk.storePreferences();
				discreetAlert("The change in color theme will occur the next time you start Mesquite");
			}
		}
		else if (checker.compare(getClass(), "Sets the maximum number of items of a particular type in Project Panel", "[number]", commandName, "setProjectPanelMaxItems")) {
			int num = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0));
			if (!MesquiteInteger.isCombinable(num))
				num= MesquiteInteger.queryInteger(containerOfModule(), "Maximum items of a kind in project panel", "To save memory and time, Mesquite limits how many items" 
			+ " of a particular kind are shown in the project panel. If you'd like to change the limit, indicate a new maximum. You will need to restart Mesquite for the change to take effect.", FileCoordinator.maxLinesOfAnyElementInPanel);
			if (!MesquiteInteger.isCombinable(num))
				return null;
			FileCoordinator.maxLinesOfAnyElementInPanel = num;
			FileCoordinator.maxLinesOfAnyElementInPanelQueried = true;
			storePreferences();
		}
		else if (checker.compare(getClass(), "Sets the maximum number of Matrices or Tree Blocks shown separately in Project Panel", "[number]", commandName, "setProjectPanelMaxItemsSeparate")) {
			int num = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0));
			if (!MesquiteInteger.isCombinable(num))
				num= MesquiteInteger.queryInteger(containerOfModule(), "Maximum items of Matrices or Tree Blocks shown separately in project panel", null, FileCoordinator.maxLinesOfMatricesTreeBlocksSeparateInPanel);
			if (!MesquiteInteger.isCombinable(num))
				return null;
			FileCoordinator.maxLinesOfMatricesTreeBlocksSeparateInPanel = num;
			storePreferences();
		}
		else if (checker.compare(getClass(), "Sets the font size of project panel", "[font size]", commandName, "setProjectPanelFontSize")) {
			int fontSize = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0));
			if (!MesquiteInteger.isCombinable(fontSize))
				fontSize = MesquiteInteger.queryInteger(containerOfModule(), "Font size", "Font size for window", MesquiteWindow.defaultFont.getSize(), 4, 256);
			if (!MesquiteInteger.isCombinable(fontSize))
				return null;
			MesquiteFrame.resourcesFontSize = fontSize;
			for (int i = 0; i< MesquiteTrunk.getProjectList().getNumProjects(); i++){
				MesquiteProject proj = MesquiteTrunk.getProjectList().getProject(i);
				proj.getCoordinatorModule().refreshGraphicsProjectWindow();
			}
			storePreferences();
		}
		else if (checker.compare(getClass(), "Forgets the default web browser", null, commandName, "forgetBrowser")) {
			browserString = null;
		}
		else if (checker.compare(getClass(), "Sets the number of previous logs saved", "[num logs]", commandName, "setNumPrevLog")) {
			int numLogs = MesquiteInteger.fromString(arguments);
			if (!MesquiteInteger.isCombinable(numLogs))
				numLogs = MesquiteInteger.queryInteger(containerOfModule(), "Number of Logs", "Number of previous log files retained", MesquiteTrunk.numPrevLogs, 2, 10000);
			if (!MesquiteInteger.isCombinable(numLogs) || numLogs == MesquiteTrunk.numPrevLogs)
				return null;
			MesquiteTrunk.numPrevLogs = numLogs;
			MesquiteTrunk.mesquiteTrunk.storePreferences();

		}
		else if (checker.compare(getClass(), "Sets the delay in recovery of files during scripting external programs (e.g., alignment, tree inference)", "[seconds]", commandName, "setDelayScriptFileRecovery")) {
			int delay = MesquiteInteger.fromString(arguments);
			String helpString = "When Mesquite uses external programs for alignment or tree inference, the results are sometimes not recovered properly.  "
					+"This has been seen on Linux. By introducing a delay of a few seconds, the asynchronous file writing may have time to complete, "
					+" allowing the results to be recovered properly.";
			if (!MesquiteInteger.isCombinable(delay))
				delay = MesquiteInteger.queryInteger(containerOfModule(), "Seconds delay", "Delay before Mesquite attempts to recover results from external programs (in seconds)", "", helpString, ShellScriptUtil.recoveryDelay, 0, 100);
			if (!MesquiteInteger.isCombinable(delay) || delay == ShellScriptUtil.recoveryDelay)
				return null;
			ShellScriptUtil.recoveryDelay = delay;
			MesquiteTrunk.mesquiteTrunk.storePreferences();

		}
		else if (checker.compare(getClass(), "Sets the maximum number of taxa and characters for whole-matrix undos to work in data editor", "[num taxa][num chars]", commandName, "setMaxMatrixSizeUndo")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			parser.setString(arguments);
			int maxTaxa = MesquiteInteger.fromString(parser.getFirstToken());
			int maxChars = MesquiteInteger.fromString(parser.getNextToken());
			if (!MesquiteInteger.isCombinable(maxTaxa)|| !MesquiteInteger.isCombinable(maxChars)) {
				MesquiteBoolean answer= new MesquiteBoolean(false);
				MesquiteInteger maxNumTaxa=new MesquiteInteger(MesquiteTrunk.maxNumMatrixUndoTaxa);
				MesquiteInteger maxNumChars = new MesquiteInteger(MesquiteTrunk.maxNumMatrixUndoChars);
				String helpString = "For some operations in the data editor to be undone, the entire data matrix needs to be remembered beforehand.  However, for large matrices, this can require more "
						+"memory than Mesquite has.  By default, Mesquite disallows this whole-matrix undo capability if the number of taxa exceeds 1000 and the number of characters exceeds 15000. "
						+" You can change these maxima here.  Note that BOTH values need to be exceeded for whole-matrix Undo to be disabled.  To specify that all matrices should fully undoable, enter a value of -1.";
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Maximum size of fully undoable matrix", "Maximum number of taxa", "Maximum number of characters",  answer,  maxNumTaxa,  maxNumChars,-1,Integer.MAX_VALUE,-1, Integer.MAX_VALUE, helpString);
				if (!answer.getValue() || !maxNumTaxa.isCombinable() || ! maxNumChars.isCombinable()) 
					return null;
				MesquiteTrunk.maxNumMatrixUndoTaxa = maxNumTaxa.getValue();
				MesquiteTrunk.maxNumMatrixUndoChars = maxNumChars.getValue();
				MesquiteTrunk.mesquiteTrunk.storePreferences();
			}

		}
		else if (checker.compare(getClass(), "Allows one to adjust settings for Python", "", commandName, "pythonSettings")) {
			PythonUtil.pythonSettings(this);
}
		else if (checker.compare(getClass(), "Sets whether to show windows of each project as tabs within a single window", null, commandName, "toggleTabbedWindows")) {
			tabbedWindows.toggleValue(null);
			MesquiteWindow.compactWindows = tabbedWindows.getValue();
			discreetAlert( "You need to close and reopen files to have the change in using tabs versus windows take effect");
			storePreferences();
		}
		else if (checker.compare(getClass(), "Sets whether to use file-specific project panel widths", null, commandName, "respectFileSpecificResourceWidth")) {
			respectFileSpecificResourceWidth.toggleValue(null);
			MesquiteFrame.respectFileSpecificResourceWidth = respectFileSpecificResourceWidth.getValue();
			storePreferences();
		}
		/*	else if (checker.compare(getClass(), "Sets whether to tile windows popped out, instead of pop them out to separate windows", null, commandName, "toggleTilePopouts")) {
			tilePopouts.toggleValue(null);
			MesquiteFrame.popIsTile = tilePopouts.getValue();
			storePreferences();
		}*/
		else if (checker.compare(getClass(), "Sets whether to use log window as input console for commands", null, commandName, "toggleConsoleMode")) {
			console.toggleValue(null);
			MesquiteTrunk.mesquiteTrunk.logWindow.setConsoleMode(console.getValue());
			if (!MesquiteThread.isScripting()) {
				if (console.getValue()) {
					logln("Command-line Mode On.  Type \"help\" for some console commands.  Note: command-line mode is experimental.  Currently it is not properly protected against simultaneous calculations, e.g. doing different modifications simultaneously of the same tree or data.");
					MesquiteTrunk.mesquiteTrunk.logWindow.showPrompt();
				}
				else
					logln("\nConsole Mode Off");
			}
			MesquiteTrunk.mesquiteTrunk.storePreferences();
		}
		else if (checker.compare(getClass(), "Sets whether to log all commands", null, commandName, "toggleLogAll")) {
			logAll.toggleValue(null);
			MesquiteCommand.logEverything = logAll.getValue();
			if (!MesquiteThread.isScripting()) {
				if (logAll.getValue()) {
					logln("Highly verbose logging on (Logging all commands).");
				}
				else
					logln("Normal logging in effect");
			}
			MesquiteTrunk.mesquiteTrunk.storePreferences();
		}
		else if (checker.compare(getClass(), "Sets whether to use debug mode", null, commandName, "toggleDebugMode")) {
			debugMode.toggleValue(null);
			MesquiteTrunk.debugMode = debugMode.getValue();
			storePreferences();
			return debugMode;
		}
		else if (checker.compare(getClass(), "Sets whether to check Mesquite web site for notices on startup", null, commandName, "togglePhoneHome")) {
			phoneHome.toggleValue(null);
			MesquiteTrunk.phoneHome = phoneHome.getValue();
			storePreferences();
			return phoneHome;
		}
		else if (checker.compare(getClass(), "Sets whether to send error reports to Mesquite server", null, commandName, "toggleErrorReports")) {
			MesquiteException.reportErrorsAutomatically.toggleValue(null);
			storePreferences();
			return MesquiteException.reportErrorsAutomatically;
		}
		else if (checker.compare(getClass(), "Sets whether to send use reports to Mesquite server", null, commandName, "toggleUseReports")) {
			useReports.toggleValue(null);
			MesquiteTrunk.reportUse = useReports.getValue();
			storePreferences();
			return useReports;
		}
		else if (checker.compare(getClass(), "Sets whether to use wizard-style dialogs", null, commandName, "toggleWizards")) {
			wizards.toggleValue(null);
			MesquiteDialog.useWizards = wizards.getValue();
			storePreferences();
			return wizards;
		}
		else if (checker.compare(getClass(), "Sets whether to use xor mode", null, commandName, "toggleXORMode")) {
			permitXOR.toggleValue(null);
			GraphicsUtil.permitXOR = permitXOR.getValue();
			resetAllMenuBars();
			return permitXOR;
		}
		else if (checker.compare(getClass(), "Sets whether to protect graphics for thread-safety", null, commandName, "protectGraphics")) {
			protectGraphics.toggleValue(null);
			MQComponentHelper.protectGraphics = protectGraphics.getValue();
			resetAllMenuBars();
			return protectGraphics;
		}
		else if (checker.compare(getClass(), "Sets whether to permit taxon name truncation in trees", null, commandName, "togglePartNamesTrees")) {
			taxonTruncTrees.toggleValue(null);
			MesquiteTree.permitTruncTaxNames = taxonTruncTrees.getValue();
			resetAllMenuBars();
			storePreferences();
			return taxonTruncTrees;
		}
		else if (checker.compare(getClass(), "Sets whether to permit spaces and underscores to be equivalent in taxon names in trees", null, commandName, "toggleSpaceUnderscoreTrees")) {
			permitSpaceUnderscoreEquivalentTrees.toggleValue(null);
			MesquiteTree.permitSpaceUnderscoreEquivalent = permitSpaceUnderscoreEquivalentTrees.getValue();

			resetAllMenuBars();
			storePreferences();
			return permitSpaceUnderscoreEquivalentTrees;
		}
		else if (checker.compare(getClass(), "Sets whether names of trees will be printed on tree by default", null, commandName, "printTreeNameByDefault")) {
			printTreeNameByDefault.toggleValue(null);
			TreeDisplay.printTreeNameByDefault = printTreeNameByDefault.getValue();
			resetAllMenuBars();
			storePreferences();
			return printTreeNameByDefault;
		}
		else if (checker.compare(getClass(), "Sets whether to place secondary choices for modules into an \"Other Choices...\" dialog box", null, commandName, "toggleOtherChoices")) {
			useOtherChoices.toggleValue(null);
			EmployerEmployee.useOtherChoices = useOtherChoices.getValue();
			resetAllMenuBars();
			storePreferences();
			return useOtherChoices;
		}
		else if (checker.compare(getClass(), "Sets whether to have secondary choices shown by default in dialog boxes", null, commandName, "toggleSecondaryChoicesOnInDialogs")) {
			secondaryChoicesOnInDialogs.toggleValue(null);
			EmployerEmployee.secondaryChoicesOnInDialogs = secondaryChoicesOnInDialogs.getValue();
			resetAllMenuBars();
			storePreferences();
			return secondaryChoicesOnInDialogs;
		}
		else if (checker.compare(getClass(), "Sets whether to have subchoices shown in module dialog boxes", null, commandName, "toggleSubChoicesOnInDialogs")) {
			subChoicesOnInDialogs.toggleValue(null);
			EmployerEmployee.subChoicesOnInDialogs = subChoicesOnInDialogs.getValue();
			storePreferences();
			return subChoicesOnInDialogs;
		}
		else if (checker.compare(getClass(), "Sets whether to use Stored Characters/Matrices by default", null, commandName, "toggleStoredAsDefault")) {
			CharacterSource.storedAsDefault.toggleValue(null);
			storePreferences();
			return CharacterSource.storedAsDefault;
		}
		else if (checker.compare(getClass(), "Sets whether to close a calculation if the matrix it uses is deleted", null, commandName, "toggleCloseIfMatrixDeleted")) {
			CharacterSource.closeIfMatrixDeleted.toggleValue(null);
			storePreferences();
			return CharacterSource.closeIfMatrixDeleted;
		}
		/*	else if (checker.compare(getClass(), "Sets whether to close a tree window automatically if the tree block it uses is deleted", null, commandName, "toggleCloseIfTreeBlockDeleted")) {
			TreeSource.closeIfTreeBlockDeleted.toggleValue(null);
			storePreferences();
			return TreeSource.closeIfTreeBlockDeleted;
		}*/
		else if (checker.compare(getClass(), "Sets whether to place ask for random number seeds when calculations requested", null, commandName, "toggleAskSeed")) {
			askSeed.toggleValue(null);
			RandomBetween.askSeed = askSeed.getValue();
			storePreferences();
			return askSeed;
		}
		else if (checker.compare(getClass(), "Sets whether to count steps in terminal polymorphisms in parsimony calculations.", null, commandName, "toggleCountStepsInTermPolymorphisms")) {
			
			ParsAncStatesForModel.countStepsInTermPolymorphisms.toggleValue(null);
			discreetAlert("You will need to re-initiate any calculations or restart Mesquite for this change to take effect.");
			storePreferences();
			return ParsAncStatesForModel.countStepsInTermPolymorphisms;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
}


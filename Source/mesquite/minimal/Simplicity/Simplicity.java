/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.minimal.Simplicity;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.simplicity.*;

import java.util.zip.*;
import java.util.*;
import java.awt.Font;
import java.io.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


public class Simplicity extends SimplicityManagerModule {

	/*todo
	 *  --  have default.xml that is loaded if there is none in prefs yet
	 *  -- all hidden; none hidden; 
	 ���  -- accomodate packages with no intro
	 * -- menu searching should say that item is hidden??? or at least warn some not found because simple interface?
	 *  -- manual pages for simplification
	 *  -- design several simplifications
	 *  -- make sure submenus can be turned off
	 *  -- how much of window to change colour; fix bugs
	 *  -- David's bug comments 14 Dec
	 *  --have package intros return pathtopackage which by default would be mesquite.XXXX
	 *  */
	MesquiteBoolean lockSimplicity;
	String themeToLoad = null;
	SimplifyControlWindow simplicityWindow;
	boolean usingLockedSimplification = false;
	public String getName() {
		return "Simplicity Manager";
	}
	public String getExplanation() {
		return "A small module to supervise the interface for simplification management." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 260;  
	}
	/*.................................................................................................................*/

	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		lockSimplicity = new MesquiteBoolean(false);
		makeInstallationSettingsPathIfNeeded();
		
		if (InterfaceManager.enabled)
			MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu,"Lock In Simple Mode", makeCommand("toggleLockSimplicity",  this), lockSimplicity);
		InterfaceManager.simplicityModule = this;  //remember me
		loadPreferences();
		return true;
	}

	/*.................................................................................................................*/
	public void init(){
		importSettingsFiles();
		addMissingPackageIntros(InterfaceManager.allPackages);
		simplicityWindow = new SimplifyControlWindow(this, MesquiteTrunk.mesquiteTrunk.interfaceManager, InterfaceManager.allPackages);
		setModuleWindow(simplicityWindow);
		showLogWindow();

//		simplicityWindow.addPackages(InterfaceManager.allPackages);
		if (!usingLockedSimplification){
			loadSettingsFileByName(themeToLoad);
			lock(InterfaceManager.locked);
		}
		else {
			InterfaceManager.setSimpleMode(true);
			InterfaceManager.setLock(true);
		}
		resetSimplicity();
	}

	public  void resetSimplicity(){
		if (simplicityWindow != null)
			simplicityWindow.resetSimplicity();
		storePreferences();
	}
	public void lock(boolean L){
		lockSimplicity.setValue(L);
		if (simplicityWindow != null)
			simplicityWindow.lock(L);
	}
	boolean hasIntro(ObjectContainer module, Vector allPackages){
		String[] sMOD = (String[])module.getObject();
		if ("true".equals(sMOD[3]))  //is intro
			return true;
		String pathMOD = sMOD[0];
		if (pathMOD.equals("mesquite"))
			return true;
		for (int i=0; i< allPackages.size(); i++){
			ObjectContainer ms = (ObjectContainer)allPackages.elementAt(i);
			if (ms !=module){
				String[] s = (String[])ms.getObject();
				String path = s[0];
				if (!(path.equals("mesquite")) && pathMOD.startsWith(path))  //intro found
					return true;
			}
		}
		return false;
	}
	void addMissingPackageIntros(Vector allPackages){
		for (int i=0; i< allPackages.size(); i++){
			ObjectContainer ms = (ObjectContainer)allPackages.elementAt(i);
			if (!hasIntro(ms, allPackages)){
				String[] s = (String[])ms.getObject();
				String path =  StringUtil.getAllButLastItem(s[0], ".");
				InterfaceManager.addPackageToList(path + " Package", path, path + " Package", true, true);
			}
		}
	}
	/*---------------------------*/
	public void importSettingsFiles(){
		
		String basePath = 		getInstallationSettingsPath();
		StringArray custom = null;
		File f = new File(basePath);
		if (f.exists() && f.isDirectory()){
			String[] list = f.list();
			if (list != null){
				for (int i=0; i<list.length; i++){
					StringArray ms = importFile(basePath + list[i], false);
					if (ms != null){
						if (list[i].equalsIgnoreCase("locked.xml")){
							usingLockedSimplification = true;  //this will later be used as a cue to set to simple mode & lock
							custom = ms;
						}
						InterfaceManager.settingsFiles.addElement(ms, false);
					}
				}

			}
		}
		basePath = 		getPath() + "simplifications" + MesquiteFile.fileSeparator;
		f = new File(basePath);
		if (f.exists() && f.isDirectory()){
			String[] list = f.list();
			if (list != null){
				for (int i=0; i<list.length; i++){
					StringArray ms = importFile(basePath + list[i], true);
					if (ms != null)
						InterfaceManager.settingsFiles.addElement(ms, false);
				}

			}
		}

		if (custom == null)
			custom = importFile(MesquiteTrunk.prefsDirectory.toString() + MesquiteFile.fileSeparator +  "Simplification.xml", false);
		loadSettingsFile(custom);
	}
	StringArray importFile(String path, boolean isDefault){
		if (!MesquiteFile.fileExists(path))
			return null;
		String settingsXML = MesquiteFile.getFileContentsAsString(path);
		Element root = XMLUtil.getRootXMLElementFromString("mesquite",settingsXML);
		if (root==null)
			return null;
		Element element = root.element("simplicitySettings");
		if (element != null) {
			Element versionElement = element.element("version");
			if (versionElement == null)
				return null;
			else {
				int version = MesquiteInteger.fromString(element.elementText("version"));
				boolean acceptableVersion = version==1;
				if (acceptableVersion) {
					String name = (element.elementText("name"));
					StringArray s =  new StringArray(3);
					s.setName(name);
					s.setValue(XML, settingsXML);
					s.setValue(PATH, path);
					if (isDefault)
						s.setValue(DEFAULTORNOT, "default");
					else
						s.setValue(DEFAULTORNOT, "nonDefault");
					return s;
				}
			}
		} 
		return null;
	}
	public  void loadSettingsFileByName(String name){
		if (name == null)
			return;
		StringArray s = (StringArray)InterfaceManager.settingsFiles.getElement(name);
		if (s == null)
			return;
		loadSettingsFile(s);
	}
	public  void loadSettingsFile(int i){
		if (!MesquiteInteger.isCombinable(i) || i<0 || i>= InterfaceManager.settingsFiles.size())
			return;
		StringArray s = (StringArray)InterfaceManager.settingsFiles.elementAt(i);
		loadSettingsFile(s);
	}
	public  void deleteSettingsFile(int i){
		if (!MesquiteInteger.isCombinable(i) || i<0 || i>= InterfaceManager.settingsFiles.size())
			return;
		StringArray s = (StringArray)InterfaceManager.settingsFiles.elementAt(i);
		String path = s.getValue(PATH);
		MesquiteFile.deleteFile(path);
		InterfaceManager.settingsFiles.removeElement(s, false);
	}
	static final int XML = 0;
	static final int PATH = 1;
	static final int DEFAULTORNOT = 2;
	public  void renameSettingsFile(int i, String newName){
		if (!MesquiteInteger.isCombinable(i) || i<0 || i>= InterfaceManager.settingsFiles.size())
			return;
		StringArray s = (StringArray)InterfaceManager.settingsFiles.elementAt(i);
		String path = s.getValue(PATH);
		String settingsXML = s.getValue(XML);
		Element root = XMLUtil.getRootXMLElementFromString("mesquite",settingsXML);
		if (root==null)
			return;
		Element element = root.element("simplicitySettings");
		if (element != null) {
			Element versionElement = element.element("version");
			if (versionElement == null)
				return ;
			else {
				int version = MesquiteInteger.fromString(element.elementText("version"));
				boolean acceptableVersion = version==1;
				if (acceptableVersion) {
					Element name = element.element("name");

					Element settingsFile = DocumentHelper.createElement("mesquite");
					Document doc = DocumentHelper.createDocument(settingsFile);
					Element hidden = DocumentHelper.createElement("simplicitySettings");
					settingsFile.add(hidden);
					XMLUtil.addFilledElement(hidden, "version","1");
					XMLUtil.addFilledElement(hidden, "name",newName);
					Element hp = element.element("hiddenPackages");
					element.remove(hp);
					hidden.add(hp);
					hp = element.element("hiddenMenuItems");
					element.remove(hp);
					hidden.add(hp);
					hp = element.element("hiddenTools");
					element.remove(hp);
					hidden.add(hp);
					s.setName(newName);
					MesquiteFile.putFileContents(path, XMLUtil.getDocumentAsXMLString(doc), false);
				}
			} 
		}
	}
	public  String nameOfSettingsFile(int i){
		if (!MesquiteInteger.isCombinable(i) || i<0 || i>= InterfaceManager.settingsFiles.size())
			return null;
		StringArray s = (StringArray)InterfaceManager.settingsFiles.elementAt(i);
		return s.getName();
	}

	boolean settingsLoaded = false;
	public void loadSettingsFile(StringArray s){
		if (s == null)
			return;
		settingsLoaded = true;
		InterfaceManager.hiddenPackages.removeAllElements(false);
		InterfaceManager.hiddenMenuItems.removeAllElements(false);
		InterfaceManager.hiddenTools.removeAllElements(false);
		String settingsXML = s.getValue(XML);
		Element root = XMLUtil.getRootXMLElementFromString("mesquite",settingsXML);
		if (root==null)
			return;
		Element element = root.element("simplicitySettings");
		if (element != null) {
			Element versionElement = element.element("version");
			if (versionElement == null)
				return ;
			else {
				int version = MesquiteInteger.fromString(element.elementText("version"));
				boolean acceptableVersion = version==1;
				if (acceptableVersion) {
					String name = (element.elementText("name"));
					InterfaceManager.themeName = name;
					Element packages = element.element("hiddenPackages");
					if (packages != null){
						List packageElements = packages.elements("package");
						for (Iterator iter = packageElements.iterator(); iter.hasNext();) {   // this is going through all of the notices
							Element hiddenPackage = (Element) iter.next();
							String pkg = hiddenPackage.element("name").getText();
							InterfaceManager.addPackageToHidden(pkg, false);						
						}
					}
					Element menuItems = element.element("hiddenMenuItems");
					if (menuItems != null){
						List menuElements = menuItems.elements("menuItem");
						for (Iterator iter = menuElements.iterator(); iter.hasNext();) {   // this is going through all of the notices
							Element hiddenM = (Element) iter.next();
							String label = XMLUtil.getTextFromElement(hiddenM, "label");
							String arguments = XMLUtil.getTextFromElement(hiddenM, "arguments");
							String command = XMLUtil.getTextFromElement(hiddenM, "command");
							String commandableClass = XMLUtil.getTextFromElement(hiddenM, "commandableClass");
							String dutyClass = XMLUtil.getTextFromElement(hiddenM, "dutyClass");
							InterfaceManager.hiddenMenuItems.addElement(new MenuVisibility(label, arguments, command, commandableClass, dutyClass), false);
						}
					}
					Element tools = element.element("hiddenTools");
					if (tools != null){
						List buttonElement = tools.elements("tool");
						for (Iterator iter = buttonElement.iterator(); iter.hasNext();) {   // this is going through all of the notices
							Element hiddenT = (Element) iter.next();
							String n = XMLUtil.getTextFromElement(hiddenT,"name");
							String d = XMLUtil.getTextFromElement(hiddenT,"description");
							InterfaceManager.hiddenTools.addElement(new MesquiteString(n, d), false);
						}
					}
					storePreferences();
				}
			}
		} 
		InterfaceManager.reset();

	}
	/*---------------------------*/
	public void settingsChanged(){
		MesquiteFile.putFileContents(MesquiteTrunk.prefsDirectory.toString() + MesquiteFile.fileSeparator +  "Simplification.xml", makeSettingsFile("Custom"), false);
		InterfaceManager.themeName = null;
		storePreferences();
		if (simplicityWindow != null)
			simplicityWindow.resetSimplicity();
	}
	/*---------------------------*/
	public  String nameOfSimplification(int i){
		if (!MesquiteInteger.isCombinable(i) || i<0 || i>= InterfaceManager.settingsFiles.size())
			return null;
		StringArray s = (StringArray)InterfaceManager.settingsFiles.elementAt(i);
		String name = s.getName();
		return name;

	}
	/*---------------------------*/
	public String makeSettingsFile(String name){
		Element settingsFile = DocumentHelper.createElement("mesquite");
		Document doc = DocumentHelper.createDocument(settingsFile);
		Element hidden = DocumentHelper.createElement("simplicitySettings");
		settingsFile.add(hidden);
		XMLUtil.addFilledElement(hidden, "version","1");
		XMLUtil.addFilledElement(hidden, "name",name);

		Element hiddenPkgs = DocumentHelper.createElement("hiddenPackages");
		hidden.add(hiddenPkgs);
		for (int i=0; i<InterfaceManager.hiddenPackages.size(); i++){
			MesquiteString ms = (MesquiteString)InterfaceManager.hiddenPackages.elementAt(i);
			Element elem = DocumentHelper.createElement("package");
			hiddenPkgs.add(elem);
			XMLUtil.addFilledElement(elem, "name",ms.getName());
		}

		Element hiddenMs = DocumentHelper.createElement("hiddenMenuItems");
		hidden.add(hiddenMs);
		for (int i=0; i<InterfaceManager.hiddenMenuItems.size(); i++){
			MenuVisibility mv = (MenuVisibility)InterfaceManager.hiddenMenuItems.elementAt(i);
			Element elem = DocumentHelper.createElement("menuItem");
			hiddenMs.add(elem);
			XMLUtil.addFilledElement(elem, "label",mv.label);
			if (mv.arguments != null)
				XMLUtil.addFilledElement(elem, "arguments",mv.arguments);
			XMLUtil.addFilledElement(elem, "command",mv.command);
			XMLUtil.addFilledElement(elem, "commandableClass",mv.commandableClassName);
			XMLUtil.addFilledElement(elem, "dutyClass",mv.dutyClass);
		}
		Element hiddenT = DocumentHelper.createElement("hiddenTools");
		hidden.add(hiddenT);
		for (int i=0; i<InterfaceManager.hiddenTools.size(); i++){
			MesquiteString s = (MesquiteString)InterfaceManager.hiddenTools.elementAt(i);
			Element elem = DocumentHelper.createElement("tool");
			hiddenT.add(elem);
			XMLUtil.addFilledElement(elem, "name",s.getName());
			XMLUtil.addFilledElement(elem, "description",s.getValue());
		}
		return XMLUtil.getDocumentAsXMLString(doc);
	}
	public void processSingleXMLPreference (String tag, String content) {
		if ("lockSimplicity".equalsIgnoreCase(tag)){
			lockSimplicity.setValue(content);
			InterfaceManager.setLock(lockSimplicity.getValue());
		}
		/*	else if ("editingMode".equalsIgnoreCase(tag)){
			MesquiteBoolean c =new MesquiteBoolean();
			c.setValue(content);
			InterfaceManager.setEditingMode(c.getValue());
		}
		 */
		else if ("simplicityMode".equalsIgnoreCase(tag)){
			MesquiteBoolean c =new MesquiteBoolean();
			c.setValue(content);
			InterfaceManager.setSimpleMode(c.getValue());
		}
		else if ("theme".equalsIgnoreCase(tag)){
			themeToLoad = content;
		}
		else if ("notheme".equalsIgnoreCase(tag)){
			themeToLoad = null;
		}
	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "lockSimplicity", lockSimplicity);   
		StringUtil.appendXMLTag(buffer, 2, "simplicityMode", InterfaceManager.isSimpleMode());   
		if (InterfaceManager.themeName != null)
			StringUtil.appendXMLTag(buffer, 2, "theme", InterfaceManager.themeName);   
		else
			StringUtil.appendXMLTag(buffer, 2, "notheme", "");   
		//	StringUtil.appendXMLTag(buffer, 2, "editingMode", InterfaceManager.isEditingMode());   
		return buffer.toString();
	}
	String instructions;
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets interface to FULL", null, commandName, "full")) {
			InterfaceManager.setSimpleMode(false);
			InterfaceManager.setEditingMode(false);
			InterfaceManager.reset();
			storePreferences();
		}
		else if (checker.compare(this.getClass(), "Sets interface to SIMPLE", null, commandName, "simple")) {
			InterfaceManager.setSimpleMode(true);
			InterfaceManager.setEditingMode(false);
			if (!settingsLoaded){
				Listable simp = ListDialog.queryList(containerOfModule(), "Choose simplification", "What simplification would you like to use?", null, InterfaceManager.settingsFiles, 0);
				if (simp != null)
					loadSettingsFile((StringArray)simp);
			}
			InterfaceManager.reset();
			storePreferences();
		}
		else if (checker.compare(this.getClass(), "Turns on interface editing", null, commandName, "edit")) {
			InterfaceManager.setEditingMode(true);
			InterfaceManager.reset();
		}
		else if (checker.compare(this.getClass(), "Turns off interface editing", null, commandName, "offEdit")) {
			InterfaceManager.setEditingMode(false);
			InterfaceManager.reset();
		}
		else	if (checker.compare(getClass(), "Sets whether to lock the simplicity mode", null, commandName, "toggleLockSimplicity")) {
			lockSimplicity.toggleValue(null);
			InterfaceManager.setLock(lockSimplicity.getValue());
			storePreferences();
			return lockSimplicity;
		}
		/*else if (checker.compare(this.getClass(), "Toggles whether menu visibility is being edited.", null, commandName, "toggleEditMenuVisibility")) {
			if (InterfaceManager.isEditingMode()) {
				InterfaceManager.mode = InterfaceManager.SIMPLE;
			}
			else{
				InterfaceManager.mode = InterfaceManager.EDITING;
			}
			InterfaceManager.reset();
		}
		else if (checker.compare(this.getClass(), "Saves the current simplification", null, commandName, "saveCurrent")) {
			saveCurrentSettings();
		}*/
		else if (checker.compare(this.getClass(), "Saves the current simplification", null, commandName, "saveCurrent")) {
			MesquiteString result = new MesquiteString("Custom Simplification");
			if (QueryDialogs.queryString(containerOfModule(), "Simplification Name", "Name of �Simplification:", result)){
				String contents =  makeSettingsFile(result.getValue());
				String path = MesquiteFile.getUniqueModifiedFileName(getInstallationSettingsPath() + "simplification", "xml");

				MesquiteFile.putFileContents(path, contents, false, false);
				StringArray sa = new StringArray(3);
				sa.setName(result.getValue());
				sa.setValue(XML, contents);
				sa.setValue(PATH, path);
				InterfaceManager.settingsFiles.addElement(sa, false);

			}
		}
		else if (checker.compare(this.getClass(), "Loads a simplification", null, commandName, "load")) {
			int i = MesquiteInteger.fromString( new Parser(arguments));
			loadSettingsFile(i);
			if (!InterfaceManager.isEditingMode() && !InterfaceManager.isSimpleMode()){
				InterfaceManager.setSimpleMode(true);
				InterfaceManager.reset();
			}
		}
		else if (checker.compare(this.getClass(), "Deletes a simplification", null, commandName, "delete")) {
			int i = MesquiteInteger.fromString( new Parser(arguments));
			if (MesquiteInteger.isCombinable(i))
				deleteSettingsFile(i);
		}
		else if (checker.compare(this.getClass(), "Renames a simplification", null, commandName, "rename")) {
			int i = MesquiteInteger.fromString( new Parser(arguments));
			if (!MesquiteInteger.isCombinable(i))
				return null;
			//deleteSettingsFile(i);
			MesquiteString ms = new MesquiteString(nameOfSettingsFile(i));
			if (QueryDialogs.queryString(containerOfModule(), "Rename Simplification", "New Name of Simplification:", ms)){
				renameSettingsFile(i, ms.getValue());
			}
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
	public boolean isHideable() {
		return false;
	}

}



package mesquite.lib.simplicity;

import mesquite.lib.ListableVector;
import mesquite.lib.MenuVisibility;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMenuItem;
import mesquite.lib.MesquiteMenuItemSpec;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquitePopup;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteSubmenu;
import mesquite.lib.MesquiteTool;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.MesquiteWindow;
import mesquite.lib.ObjectContainer;
import mesquite.lib.Parser;
import mesquite.lib.XMLUtil;
import mesquite.lib.duties.WindowHolder;
import java.util.*;
import java.util.List;
import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class InterfaceManager {

	/*vvvvvvvvvvvvvvvvvvvvv*/
	public static final boolean enabled = true;
	public static boolean locked = false;
	/*^^^^^^^^^^^^^^^^^^^*/

/*todo
 *  --  have default.xml that is loaded if there is none in prefs yet
 *  -- accomodate packages with no intro
 *  -- manual pages for simplification
 *  -- design several simplifications
 *  -- make sure submenus can be turned off
 *  --have package intros return pathtopackage which by default would be mesquite.XXXX
 *  */

	MesquiteModule interfaceWindowBabysitter;
	Parser parser = new Parser();
	InterfaceManagerWindow ww;
	/*.................................................................................................................*/
	public void makeWindow(){
		if (interfaceWindowBabysitter != null)
			return;
		interfaceWindowBabysitter = MesquiteTrunk.mesquiteTrunk.hireNamedEmployee (WindowHolder.class, "#WindowBabysitter");
		ww = new InterfaceManagerWindow(interfaceWindowBabysitter, this);
		interfaceWindowBabysitter.setModuleWindow(ww);
		ww.addPackages(allPackages);
		ww.lock(locked);
	}

	Vector allPackages = new Vector();
	public void addPackageToList(String name, String path, String explanation, boolean isHideable, boolean isPackage){
		allPackages.addElement(new ObjectContainer(name, new String[]{path, explanation, Boolean.toString(isHideable), Boolean.toString(isPackage)}));
	}

	/*.................................................................................................................*/
	//STATIC
	//modes
	public static final int ALL = 0;
	public static final int SIMPLE = 1;
	public static final int EDITING = 2;

	public static int mode = ALL;

	//status
	public static final int NORMAL = 0;
	public static final int HIDDEN = 1;
	public static final int TOBEHIDDEN = 2;
	public static final int HIDDENCLASS = 3;

	static MesquiteWindow simplicityWindow;
	public static ListableVector hiddenMenuItems;
	public static ListableVector hiddenPackages;
	public static ListableVector hiddenTools;
	public static ListableVector settingsFiles;
	static {
		hiddenMenuItems = new ListableVector();
		hiddenPackages = new ListableVector();
		if (false && enabled){
			addPackageToHidden("mesquite.align");
			addPackageToHidden("mesquite.assoc");
			addPackageToHidden("mesquite.batchArch");
			addPackageToHidden("mesquite.coalesce");
			addPackageToHidden("mesquite.correl");
			addPackageToHidden("mesquite.distance");
			addPackageToHidden("mesquite.diverse");
			addPackageToHidden("mesquite.dmanager");
			addPackageToHidden("mesquite.genesis");
			addPackageToHidden("mesquite.mb");
			addPackageToHidden("mesquite.ornamental");
			addPackageToHidden("mesquite.pairwise");
			addPackageToHidden("mesquite.rhetenor");
			addPackageToHidden("mesquite.stochchar");
			addPackageToHidden("mesquite.treefarm");
			addPackageToHidden("mesquite.tol");
		}
		hiddenTools = new ListableVector();
		settingsFiles = new ListableVector();
	}

	public static void setLock(boolean lock){
		boolean wasLocked = locked;
		locked = lock;
		if (lock)
			mode = SIMPLE;
		if (simplicityWindow != null)
			((InterfaceManagerWindow)simplicityWindow).lock(lock);
		reset();
	}
	/*.................................................................................................................*/
	public static void report(){
		if (!enabled)
			return;
		MesquiteMessage.println("-----vvv-----");
		if (mode == EDITING)
			MesquiteMessage.println("EDITING");
		MesquiteMessage.println("MENU ITEMS");
		for (int i = 0; i< hiddenMenuItems.size(); i++){
			MenuVisibility h = (MenuVisibility)hiddenMenuItems.elementAt(i);
			MesquiteMessage.println("HIDDEN " + h.getName() + " h commandable " + h.commandableClassName + " arguments " + h.arguments + " command " + h.command);
		}
		MesquiteMessage.println("...");
		MesquiteMessage.println("TOOLS");
		for (int i = 0; i< hiddenTools.size(); i++){
			MesquiteString h = (MesquiteString)hiddenTools.elementAt(i);
			MesquiteMessage.println("HIDDEN " + h.getName() + " =  " + h.getValue());
		}
		MesquiteMessage.println("...");
		MesquiteMessage.println("PACKAGES");
		for (int i = 0; i< hiddenPackages.size(); i++){
			MesquiteString h = (MesquiteString)hiddenPackages.elementAt(i);
			MesquiteMessage.println("HIDDEN " + h.getName() + " =  " + h.getValue());
		}
		MesquiteMessage.println("-----^^^-----");

	}
	public static void addPackageToHidden(String packagePath){
		hiddenPackages.addElement(new MesquiteString(packagePath, packagePath), false);
	}
	public static void removePackageFromHidden(String packagePath){
		int i = hiddenPackages.indexOfByName(packagePath);
		if (i>=0)
			hiddenPackages.removeElementAt(i, false);
	}

	public static void addToolToHidden(MesquiteTool tool){ //TODO: should be based on more than just name and description!
		if (onHiddenToolList(tool))
			return;
		hiddenTools.addElement(new MesquiteString(tool.getName(), tool.getDescription()), false);
	}
	public static void removeToolFromHidden(String name, String description){ //TODO: should be based on more than just name and description!
		for (int i = 0; i<hiddenTools.size(); i++){
			MesquiteString vis = (MesquiteString)hiddenTools.elementAt(i);
			String hiddenName = vis.getName();
			String hiddenDescr = vis.getValue();
			if (hiddenName != null && name.equals(hiddenName) && hiddenDescr != null && description.equals(hiddenDescr)){
				hiddenTools.removeElementAt(i, false);
			}
		}

	}

	public static void addMenuItemToHidden(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (onHiddenMenuItemList(label, arguments, command, dutyClass))
			return;
		if (command == null)
			return;
		Class commandable = null;
		Object owner = command.getOwner();
		if (owner != null)
			commandable = owner.getClass();
		String commandableClassName = null;
		if (commandable != null)
			commandableClassName = commandable.getName();
		String dcName = null;
		if (dutyClass != null)
			dcName = dutyClass.getName();
		hiddenMenuItems.addElement(new MenuVisibility(label, arguments, command.getName(), commandableClassName, dcName), false);
	}

	public static void removeMenuItemFromHidden(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (!onHiddenMenuItemList(label, arguments, command, dutyClass))
			return;
		if (command == null)
			return;
		Class commandable = null;
		Object owner = command.getOwner();
		if (owner != null)
			commandable = owner.getClass();
		String commandableClassName = null;
		if (commandable != null)
			commandableClassName = commandable.getName();
		String dcName = null;
		if (dutyClass != null)
			dcName = dutyClass.getName();
		for (int i = 0; i<hiddenMenuItems.size(); i++){
			MenuVisibility vis = (MenuVisibility)hiddenMenuItems.elementAt(i);
			if (vis.matchesMenuItem(label, arguments, command.getName(), commandableClassName, dcName)){
				hiddenMenuItems.removeElement(vis, false);
				return;
			}
		}
	}

	static boolean onHiddenMenuItemList(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (command == null)
			return onHiddenMenuItemList(label, arguments, null, null, dutyClass);
		Object commandable = command.getOwner();
		Class c = null;
		if (commandable != null)
			c = commandable.getClass();

		if (arguments == null)
			arguments = command.getDefaultArguments();
		return onHiddenMenuItemList(label, arguments, command.getName(), c, dutyClass);
	}

	static boolean onHiddenMenuItemList(String label, String arguments, String command, Class commandable, Class dutyClass){
		String commandableClassName = null;
		if (commandable != null)
			commandableClassName = commandable.getName();
		String dcName = null;
		if (dutyClass != null)
			dcName = dutyClass.getName();
		for (int i = 0; i<hiddenMenuItems.size(); i++){
			MenuVisibility vis = (MenuVisibility)hiddenMenuItems.elementAt(i);
			if (vis.matchesMenuItem(label, arguments, command, commandableClassName, dcName)){
				return true;
			}
		}
		return false;
	}
	static boolean onHiddenClassList(Class c){
		if (c == null)
			return false;
		String name = c.getName();
		for (int i = 0; i<hiddenPackages.size(); i++){
			MesquiteString vis = (MesquiteString)hiddenPackages.elementAt(i);
			String hidden = vis.getName();
			if (hidden != null && name.startsWith(hidden))
				return true;
		}

		return false;
	}
	static boolean onHiddenClassList(String pkg){
		if (pkg == null)
			return false;
		String name = pkg;
		for (int i = 0; i<hiddenPackages.size(); i++){
			MesquiteString vis = (MesquiteString)hiddenPackages.elementAt(i);
			String hidden = vis.getName();
			if (hidden != null && name.startsWith(hidden))
				return true;
		}

		return false;
	}
	static boolean onHiddenClassListExactly(String pkg){
		if (pkg == null)
			return false;
		String name = pkg;
		for (int i = 0; i<hiddenPackages.size(); i++){
			MesquiteString vis = (MesquiteString)hiddenPackages.elementAt(i);
			String hidden = vis.getName();
			if (hidden != null && name.equals(hidden))
				return true;
		}

		return false;
	}
	static boolean onHiddenToolList(MesquiteTool tool){
		if (tool == null)
			return false;
		Object initiator = tool.getInitiator();
		if (initiator != null && onHiddenClassList(initiator.getClass()))
			return true;
		String name = tool.getName();
		String descr = tool.getDescription();
		return onHiddenToolList(name, descr);
	}
	static boolean onHiddenToolList(String name, String descr){
		for (int i = 0; i<hiddenTools.size(); i++){
			MesquiteString vis = (MesquiteString)hiddenTools.elementAt(i);
			String hiddenName = vis.getName();
			String hiddenDescr = vis.getValue();
			if (hiddenName != null && name.equals(hiddenName) && hiddenDescr != null && descr.equals(hiddenDescr))
				return true;
		}

		return false;
	}
	public static int isHiddenTool(MesquiteTool tool){
		if (!enabled)
			return NORMAL;
		if (mode == ALL)
			return NORMAL;
		if (tool == null)
			return NORMAL;
		Object initiator = tool.getInitiator();
		if (initiator != null && onHiddenClassList(initiator.getClass()))
			return HIDDENCLASS;
		String name = tool.getName();
		String descr = tool.getDescription();
		boolean toolHidden =  onHiddenToolList(name, descr);
		if (toolHidden){
			if (mode == SIMPLE)
				return HIDDEN;
			if (mode == EDITING)
				return TOBEHIDDEN;
		}
		return NORMAL;
	}
	public static int isHiddenMenuItem(MesquiteMenuItemSpec mmi, String label, String arguments, MesquiteCommand command, Class moduleClass){
		if (!enabled)
			return NORMAL;
		return isHiddenMenuItem(mmi, label, arguments, command, moduleClass, (Class)null);
	}

	public static int isHiddenMenuItem(MesquiteMenuItemSpec mmi, String label, String arguments, MesquiteCommand command, Class moduleClass, Class dutyClass){
		if (!enabled)
			return NORMAL;
		if (mode == ALL)
			return NORMAL;
		boolean classHidden = onHiddenClassList(moduleClass);
		if (!classHidden && mmi != null && mmi.getOwnerClass() != null)
			classHidden = onHiddenClassList(mmi.getOwnerClass());
		if (!classHidden && mmi != null && mmi.getCommand() != null && mmi.getCommand().getOwner() != null)
			classHidden = onHiddenClassList(mmi.getCommand().getOwner().getClass());
		if (classHidden){
			if (mode == SIMPLE)
				return HIDDEN;
			if (mode == EDITING)
				return HIDDENCLASS;
		}
		boolean onList = onHiddenMenuItemList(label, arguments, command, dutyClass);

		if (onList){ 
			if (mode == SIMPLE)
				return HIDDEN;
			if (mode == EDITING)
				return TOBEHIDDEN;
		}
		return NORMAL;
	}
	/*---------------------------*/
	public static void getLoadSaveMenuItems(MesquitePopup popup){
		if (simplicityWindow != null){
			popup.add(new MesquiteMenuItem("Save Current Simplification", null, new MesquiteCommand("saveCurrent", simplicityWindow), null));
			MesquiteSubmenu ms = new MesquiteSubmenu("Load Simplification", popup, null);
			for (int i = 0; i< settingsFiles.size(); i++){
				MesquiteString sf = (MesquiteString)settingsFiles.elementAt(i);
				ms.add(new MesquiteMenuItem(sf.getName(), null, new MesquiteCommand("load", "" + i, simplicityWindow), null));
			}
			popup.add(ms);
		}
	}
	/*---------------------------*/
	public static void resetSimplicity(){
		if (simplicityWindow != null)
			((InterfaceManagerWindow)simplicityWindow).resetSimplicity();

	}
	/*---------------------------*/
	public static void importSettingsFiles(){
		String basePath = MesquiteTrunk.mesquiteTrunk.getRootPath() + "extras" + MesquiteFile.fileSeparator + "simplifications" + MesquiteFile.fileSeparator;
		File f = new File(basePath);
		if (f.exists() && f.isDirectory()){
			String[] list = f.list();
			if (list != null){
				for (int i=0; i<list.length; i++){
					MesquiteString ms = importFile(basePath + list[i]);
					if (ms != null)
						settingsFiles.addElement(ms, false);
				}

			}
		}
		MesquiteString custom = importFile(MesquiteTrunk.prefsDirectory.toString() + MesquiteFile.fileSeparator +  "Simplification.xml");
		loadSettingsFile(custom);
	}
	static MesquiteString importFile(String path){
		String settingsXML = MesquiteFile.getFileContentsAsString(path);
		Element root = XMLUtil.getRootXMLElementFromString("mesquite",settingsXML);
		if (root==null)
			return null;
		Element element = root.element("hidden");
		if (element != null) {
			Element versionElement = element.element("version");
			if (versionElement == null)
				return null;
			else {
				int version = MesquiteInteger.fromString(element.elementText("version"));
				boolean acceptableVersion = version==1;
				if (acceptableVersion) {
					String name = (element.elementText("name"));
					return new MesquiteString(name, settingsXML);

				}
			}
		} 
		return null;
	}
	public static  void loadSettingsFile(int i){
		if (!MesquiteInteger.isCombinable(i) || i<0 || i>= settingsFiles.size())
			return;
		MesquiteString s = (MesquiteString)settingsFiles.elementAt(i);
		loadSettingsFile(s);
	}
	public static void loadSettingsFile(MesquiteString s){
		if (s == null)
			return;
		hiddenPackages.removeAllElements(false);
		hiddenMenuItems.removeAllElements(false);
		hiddenTools.removeAllElements(false);
		String settingsXML = s.getValue();
		Element root = XMLUtil.getRootXMLElementFromString("mesquite",settingsXML);
		if (root==null)
			return;
		Element element = root.element("hidden");
		if (element != null) {
			Element versionElement = element.element("version");
			if (versionElement == null)
				return ;
			else {
				int version = MesquiteInteger.fromString(element.elementText("version"));
				boolean acceptableVersion = version==1;
				if (acceptableVersion) {
					String name = (element.elementText("name"));
					Element packages = element.element("hiddenPackages");
					if (packages != null){
						List packageElements = packages.elements("package");
						for (Iterator iter = packageElements.iterator(); iter.hasNext();) {   // this is going through all of the notices
							Element hiddenPackage = (Element) iter.next();
							String pkg = hiddenPackage.element("name").getText();
							addPackageToHidden(pkg);						
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
							hiddenMenuItems.addElement(new MenuVisibility(label, arguments, command, commandableClass, dutyClass), false);
						}
					}
					Element tools = element.element("hiddenTools");
					if (tools != null){
						List buttonElement = tools.elements("tool");
						for (Iterator iter = buttonElement.iterator(); iter.hasNext();) {   // this is going through all of the notices
							Element hiddenT = (Element) iter.next();
							String n = XMLUtil.getTextFromElement(hiddenT,"name");
							String d = XMLUtil.getTextFromElement(hiddenT,"description");
							hiddenTools.addElement(new MesquiteString(n, d), false);
						}
					}
				}
			}
		} 
		reset();

	}
	/*---------------------------*/
	public static void saveCurrentSettingsFile(){
		Element settingsFile = DocumentHelper.createElement("mesquite");
		Document doc = DocumentHelper.createDocument(settingsFile);
		Element hidden = DocumentHelper.createElement("hidden");
		settingsFile.add(hidden);
		XMLUtil.addFilledElement(hidden, "version","1");
		XMLUtil.addFilledElement(hidden, "name","Custom");

		Element hiddenPkgs = DocumentHelper.createElement("hiddenPackages");
		hidden.add(hiddenPkgs);
		for (int i=0; i<hiddenPackages.size(); i++){
			MesquiteString ms = (MesquiteString)hiddenPackages.elementAt(i);
			Element elem = DocumentHelper.createElement("package");
			hiddenPkgs.add(elem);
			XMLUtil.addFilledElement(elem, "name",ms.getName());
		}

		Element hiddenMs = DocumentHelper.createElement("hiddenMenuItems");
		hidden.add(hiddenMs);
		for (int i=0; i<hiddenMenuItems.size(); i++){
			MenuVisibility mv = (MenuVisibility)hiddenMenuItems.elementAt(i);
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
		for (int i=0; i<hiddenTools.size(); i++){
			MesquiteString s = (MesquiteString)hiddenTools.elementAt(i);
			Element elem = DocumentHelper.createElement("tool");
			hiddenT.add(elem);
			XMLUtil.addFilledElement(elem, "name",s.getName());
			XMLUtil.addFilledElement(elem, "description",s.getValue());
		}

		MesquiteFile.putFileContents(MesquiteTrunk.prefsDirectory.toString() + MesquiteFile.fileSeparator +  "Simplification.xml", XMLUtil.getDocumentAsXMLString(doc), false);
	}

	/*---------------------------*/
	public static void reset(){
		MesquiteTrunk.resetAllMenuBars();
		MesquiteTrunk.resetAllToolPalettes();
		MesquiteWindow.resetAllSimplicity();
		//	report();
	}

}


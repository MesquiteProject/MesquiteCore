package mesquite.lib.simplicity;

import mesquite.lib.*;
import java.awt.*;
import java.util.*;

public class InterfaceManager {

	/*vvvvvvvvvvvvvvvvvvvvv*/
	public static final boolean enabled = true;
	public static boolean locked = false;
	/*^^^^^^^^^^^^^^^^^^^*/

	Parser parser = new Parser();

	/*.................................................................................................................*/
	//STATIC
	//modes
	/*	private static final int ALL = 0;
	private static final int SIMPLE = 1;
	private static final int EDITING = 2;
	 */
	private static boolean simpleMode = false;
	private static boolean editingMode = false;
	public static boolean isEditingMode(){
		return editingMode;
	}
	public static void setEditingMode(boolean ed){
		editingMode = ed;
	}
	public static boolean isSimpleMode(){
		return simpleMode;
	}
	public static void setSimpleMode(boolean simple){
		simpleMode = simple;
	}
	
	public static String themeName = null;
	
	//status
	public static final int NORMAL = 0;
	public static final int HIDDEN = 1;
	public static final int TOBEHIDDEN = 2;
	public static final int HIDDENCLASS = 3;

	public static Vector allPackages = new Vector();
	public static SimplicityManagerModule simplicityModule;
	public static ListableVector hiddenMenuItems;
	public static ListableVector hiddenPackages;
	public static ListableVector hiddenTools;
	public static ListableVector settingsFiles;
	static {
		hiddenMenuItems = new ListableVector();
		hiddenPackages = new ListableVector();
		hiddenTools = new ListableVector();
		settingsFiles = new ListableVector();
	}

	public static void setLock(boolean lock){
		boolean wasLocked = locked;
		locked = lock;
		if (lock)
			editingMode = true;
		if (simplicityModule != null)
			simplicityModule.lock(lock);
		reset();
	}
	/*.................................................................................................................*/
	public static void report(){
		if (!enabled)
			return;
		MesquiteMessage.println("-----vvv-----");
		if (isEditingMode())
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
	public static void addPackageToList(String name, String path, String explanation, boolean isHideable, boolean isPackage){
		allPackages.addElement(new ObjectContainer(name, new String[]{path, explanation, Boolean.toString(isHideable), Boolean.toString(isPackage)}));
	}

	static void autoSave(){
		if (simplicityModule != null)
			simplicityModule.settingsChanged();
	}
	public static void addPackageToHidden(String packagePath, boolean save){
		hiddenPackages.addElement(new MesquiteString(packagePath, packagePath), false);
		if (save) 
			autoSave();
	}
	public static void removePackageFromHidden(String packagePath, boolean save){
		int i = hiddenPackages.indexOfByName(packagePath);
		if (i>=0){
			hiddenPackages.removeElementAt(i, false);
			if (save) 
				autoSave();
		}
	}

	public static void addToolToHidden(MesquiteTool tool, boolean save){ //TODO: should be based on more than just name and description!
		if (onHiddenToolList(tool))
			return;
		hiddenTools.addElement(new MesquiteString(tool.getName(), tool.getDescription()), false);
		if (save) 
			autoSave();
	}
	public static void removeToolFromHidden(String name, String description, boolean save){ //TODO: should be based on more than just name and description!
		for (int i = 0; i<hiddenTools.size(); i++){
			MesquiteString vis = (MesquiteString)hiddenTools.elementAt(i);
			String hiddenName = vis.getName();
			String hiddenDescr = vis.getValue();
			if (hiddenName != null && name.equals(hiddenName) && hiddenDescr != null && description.equals(hiddenDescr)){
				hiddenTools.removeElementAt(i, false);
				if (save) 
					autoSave();
				return;
			}
		}

	}

	public static void addMenuItemToHidden(String label, String arguments, MesquiteCommand command, Class dutyClass, boolean save){
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
		if (save) 
			autoSave();
	}

	public static void removeMenuItemFromHidden(String label, String arguments, MesquiteCommand command, Class dutyClass, boolean save){
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
		int size = hiddenMenuItems.size();
		for (int i = 0; i<size; i++){
			MenuVisibility vis = (MenuVisibility)hiddenMenuItems.elementAt(i);
			if (vis.matchesMenuItem(label, arguments, command.getName(), commandableClassName, dcName)){
				hiddenMenuItems.removeElement(vis, false);
				if (save) 
					autoSave();
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
	static int countC = 0;
	static int countS = 0;
	public static boolean onHiddenClassList(Class c){
		if (c == null)
			return false;
		String name = c.getName();
		int size = hiddenPackages.size();
		for (int i = 0; i<size; i++){
			MesquiteString vis = (MesquiteString)hiddenPackages.elementAt(i);
			String hidden = vis.getName();
			if (hidden != null && name.startsWith(hidden))
				return true;
		}

		return false;
	}
	public static boolean onHiddenClassList(String pkg){
		if (StringUtil.blank(pkg))
			return false;
		String name = pkg;
		int size = hiddenPackages.size();
		for (int i = 0; i<size; i++){
			MesquiteString vis = (MesquiteString)hiddenPackages.elementAt(i);
			String hidden = vis.getName();
			if (hidden != null && name.startsWith(hidden))
				return true;
		}

		return false;
	}
	public static boolean onHiddenClassListExactly(String pkg){
		if (StringUtil.blank(pkg))
			return false;
		String name = pkg;
		int size = hiddenPackages.size();
		for (int i = 0; i<size; i++){
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
		if (!isEditingMode() && !isSimpleMode())
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
			if (!isEditingMode() && isSimpleMode())
				return HIDDEN;
			if (isEditingMode())
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
		if (!isEditingMode() && !isSimpleMode())
			return NORMAL;
		boolean classHidden = onHiddenClassList(moduleClass);
		if (!classHidden && !StringUtil.blank(arguments)){
			classHidden = onHiddenClassList(arguments);
		}
		if (!classHidden && mmi != null && mmi.getOwnerClass() != null)
			classHidden = onHiddenClassList(mmi.getOwnerClass());
		if (!classHidden && mmi != null && mmi.getCommand() != null && mmi.getCommand().getOwner() != null)
			classHidden = onHiddenClassList(mmi.getCommand().getOwner().getClass());
		if (classHidden){
			if (!isEditingMode() && isSimpleMode())
				return HIDDEN;
			if (isEditingMode())
				return HIDDENCLASS;
		}
		boolean onList = onHiddenMenuItemList(label, arguments, command, dutyClass);

		if (onList){ 
			if (!isEditingMode() && isSimpleMode())
				return HIDDEN;
			if (isEditingMode())
				return TOBEHIDDEN;
		}
		return NORMAL;
	}
	/*---------------------------*/
	public static boolean isFilterable(Menu menu){
		if (menu instanceof MesquiteMenu){
			return (((MesquiteMenu)menu).isFilterable());

		}
		return true;
	}
	/*---------------------------
	public static void getLoadSaveMenu(MesquitePopup popup){
		if (simplicityModule != null){
			popup.add(new MesquiteMenuItem("Save Current Simplification", null, new MesquiteCommand("saveCurrent", simplicityModule), null));
			MesquiteSubmenu ms = new MesquiteSubmenu("Load Simplification", popup, null);
			for (int i = 0; i< settingsFiles.size(); i++){
				MesquiteString sf = (MesquiteString)settingsFiles.elementAt(i);
				ms.add(new MesquiteMenuItem(sf.getName(), null, new MesquiteCommand("load", "" + i, simplicityModule), null));
			}
			popup.add(ms);
		}
	}
	public static void getSaveRenameDeleteMenuItems(java.awt.Menu menu){
		if (simplicityModule != null){
			menu.add(new MesquiteMenuItem("Save Current...", null, new MesquiteCommand("saveCurrent", simplicityModule), null));
			MesquiteSubmenu ms = new MesquiteSubmenu("Rename...", menu, null);
			for (int i = 0; i< settingsFiles.size(); i++){
				StringArray sf = (StringArray)settingsFiles.elementAt(i);
				ms.add(new MesquiteMenuItem(sf.getName(), null, new MesquiteCommand("rename", "" + i, simplicityModule), null));
			}
			menu.add(ms);
			MesquiteSubmenu ms2 = new MesquiteSubmenu("Delete...", menu, null);
			for (int i = 0; i< settingsFiles.size(); i++){
				StringArray sf = (StringArray)settingsFiles.elementAt(i);
				ms2.add(new MesquiteMenuItem(sf.getName(), null, new MesquiteCommand("delete", "" + i, simplicityModule), null));
			}
			menu.add(ms2);
		}
	}
	public static void getLoadMenuItems(java.awt.Menu menu){
		if (simplicityModule != null){
			for (int i = 0; i< settingsFiles.size(); i++){
				StringArray sf = (StringArray)settingsFiles.elementAt(i);
				menu.add(new MesquiteMenuItem(sf.getName(), null, new MesquiteCommand("load", "" + i, simplicityModule), null));
			}
		}
	}*/
	public static void addSettingsMenuItems(java.awt.Menu menu, String command, boolean includeDefaults){
		if (simplicityModule != null){
			for (int i = 0; i< settingsFiles.size(); i++){
				StringArray sf = (StringArray)settingsFiles.elementAt(i);
				if (includeDefaults || sf.getValue(2) == null || !sf.getValue(2).equalsIgnoreCase("default"))
					menu.add(new MesquiteMenuItem(sf.getName(), null, new MesquiteCommand(command, "" + i, simplicityModule), null));
			}
		}
	}
	public static boolean settingsWritable(){
		if (simplicityModule == null)
			return false;
		return MesquiteFile.canWrite(simplicityModule.getInstallationSettingsPath());
	}
	/*---------------------------*/
	public static void resetSimplicity(){
		if (simplicityModule != null)
			simplicityModule.resetSimplicity();

	}


	/*---------------------------*/
	public static void reset(){
		MesquiteTrunk.resetAllMenuBars();
		MesquiteTrunk.resetAllToolPalettes();
		MesquiteWindow.resetAllSimplicity();
		//	report();
	}

}


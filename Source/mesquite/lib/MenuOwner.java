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
import java.applet.Applet;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.awt.event.*;

import mesquite.distance.lib.TaxaDistFromMatrix;
import mesquite.lib.duties.*;
import mesquite.lib.simplicity.InterfaceManager;

/* ��������������������������� Menus ������������������������������� */

/* ======================================================================== */
/**Menus in Mesquite are composed by the code in the MenuOwner class, which is designed to be a superclass only for
the MesquiteModule class.  The methods of the MenuOwner class were separated from MesquiteModule because the latter
was becoming unwieldy.  We expect MenuOwner will never be used apart from its status as superclass to MesquiteModule.<p>

MesquiteModules request menus and menu items by creating MesquiteMenuSpec's (for menus, using
makeMenu or addAuxiliaryMenu) or MesquiteMenuItemSpec's (for items, using addMenuItem).  These
Specs store the necessary information to allow actual Java Menu and MenuItems to be created when
menu bars are composed using resetMenus (of MesquiteWindow) and composeMenuBar (of MesquiteModule).
Menu bars in general are re-composed when employees are hired, because the new employees may have menu items that need adding.<p>

All menu items operate via a Command; thus, to define a MesquiteMenuItem you need to pass it
the command that is to be invoked when the menu item is selected.  Similarly the MiniScrolls are passed
a command to be called on changes of their index, and MesquiteButtons are passed a command to be called
when they are touched.  Branch moves will soon also be done via tool objects that store commands.
 */
public abstract class MenuOwner implements Doomable { //EMBEDDED: extends Applet
	public static final int MAXPRIORITY=25;  //needs to be 25 to accommodate genetic codes
	public static boolean considerPriorities = false;  //this allow ones to turn off the priority system
	//protected MesquiteMenuSpec defaultsMenuSpec; 
	//protected MesquiteSubmenu defaultSubMenu;

	/** The module corresponding to this object (maybe always will be the same as this object */
	protected MesquiteModule module;

	/** The window, if any, belonging to the MesquiteModule*/
	MesquiteWindow window=null; 

	/** Vector of specifications for module's menu items*/
	MenuItemsSpecsVector menuItemsSpecs=null; 
	/** The specification for the module's own menu.  Each module gets one menu it can request as its basic.  All otherwise unplaced menu items of the module and its
	employees are placed there.  The module can also specify auxiliary menus.*/
	MesquiteMenuSpec moduleMenuSpec;  
	/** The specification for the menu to which items are assigned (if no moduleMenu)*/
	MesquiteMenuSpec assignedMenuSpec;  
	/** The vector of the module's auxiliary menus.*/
	Vector auxiliaryMenus;

	/*if flag off, this modules menus are not put in menubar; instead can be accessed in popupmenu*/
	private boolean useMenuBar=true;
	private MRPopup popUp;

	public static boolean menuTracing = false;

	MesquiteMenuItem undoMenuItem = null;
	boolean undoEnabled = true;

	MesquiteMenuItem previousToolMenuItem = null;


	protected Parser parser = new Parser();

	/** true if name of MesquiteModule is to be shown in alerts*/
	private static final boolean showModuleInAlert = true;
	/** true if name of MesquiteModule is to be shown in log entries*/
	private static final boolean showModuleInLog = false;
	/** true if alerts should show a dialog, or merely write to log.*/
	private static final boolean alertUseDialog = true;

	protected boolean doomed = false;
	private static int menuSuppression = 0;
	private static boolean resetAllMenuPending = false;
	private static boolean resetWindowsMenuPending = false;

	private static int composeCount = 0;

	private static MenuShortcut newShortcut, openShortcut, saveShortcut, printShortcut, getInfoShortcut, quitShortcut, ccShortcut, previousToolShortcut;
	private static MenuShortcut undoShortcut, copyShortcut, cutShortcut, clearShortcut, selectAllShortcut, pasteShortcut;

	public static String leftBracket, rightBracket;

	static {

		// A list of keyboard equivalents is at the bottom of this file

		newShortcut = new MenuShortcut(KeyEvent.VK_N);
		openShortcut = new MenuShortcut(KeyEvent.VK_O);
		saveShortcut = new MenuShortcut(KeyEvent.VK_S);
		printShortcut = new MenuShortcut(KeyEvent.VK_P);
		getInfoShortcut = new MenuShortcut(KeyEvent.VK_I);
		quitShortcut = new MenuShortcut(KeyEvent.VK_Q);
		ccShortcut = new MenuShortcut(KeyEvent.VK_PERIOD);

		undoShortcut = new MenuShortcut(KeyEvent.VK_Z);
		previousToolShortcut = new MenuShortcut(KeyEvent.VK_T);
		copyShortcut = new MenuShortcut(KeyEvent.VK_C);
		cutShortcut = new MenuShortcut(KeyEvent.VK_X);
		clearShortcut = new MenuShortcut(KeyEvent.VK_CLEAR);
		selectAllShortcut = new MenuShortcut(KeyEvent.VK_A);
		pasteShortcut = new MenuShortcut(KeyEvent.VK_V);
		/*if (MesquiteTrunk.isMacOSX()){
			leftBracket = "◀";//byte[] b = {(byte) 226, (byte)150, (byte)160};  new String(b, "UTF-8");
			rightBracket = "▶"; //byte[] bb = {(byte) 226, (byte)150, (byte)161};  new String(bb, "UTF-8");
		}
		else {*/
		leftBracket = "«";//byte[] b = {(byte) 226, (byte)150, (byte)160};  new String(b, "UTF-8");
		rightBracket = "»"; //byte[] bb = {(byte) 226, (byte)150, (byte)161};  new String(bb, "UTF-8");
		//}

	}
	/** The constructor in general is to be avoided, because modules are instantiated momentarily on startup to gather
	information.  The usual functions of a constructor are performed by startJob*/
	public MenuOwner () {
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	protected void setModule(MesquiteModule mb){
		module = mb;
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	public void setModuleWindow(MesquiteWindow w){
		window = w;
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	public MesquiteWindow getModuleWindow(){
		return window;
	}

	/*.................................................................................................................*/
	/** DOCUMENT */
	public boolean isDoomed(){
		return doomed;
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	public void doom(){
		doomed = true;
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	protected void disposeMenuSpecifications() {
		if (menuItemsSpecs!=null) {
			Enumeration e = menuItemsSpecs.elements();
			while(e.hasMoreElements()){
				MesquiteMenuItemSpec mmis = (MesquiteMenuItemSpec)e.nextElement();
				mmis.disconnect();
			}
			menuItemsSpecs.dispose();
		}
		if (auxiliaryMenus!=null) {
			auxiliaryMenus.removeAllElements();
		}
		moduleMenuSpec=null;
		menuItemsSpecs = null;
		auxiliaryMenus = null;
		assignedMenuSpec = null;
	}
	public MenuItemsSpecsVector getMenuItemSpecs(){
		return menuItemsSpecs;
	}
	public boolean needsMenu() {
		if (menuItemsSpecs != null && menuItemsSpecs.size()>0)
			return true;
		if (module == null)
			return false;
		EmployeeVector employees = module.getEmployeeVector();
		if (employees == null)
			return false;
		Enumeration e = employees.elements();
		while(e.hasMoreElements()){
			MesquiteModule mb = (MesquiteModule)e.nextElement();
			if (mb.needsMenu())
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/** This resets the menu bar in which the MesquiteModule menu items reside*/
	public void resetContainingMenuBar() {

		if (resetAllMenuPending)
			return;
		else if (menuSuppression==0) { //currently menu rebuilding is not suppressed; therefore do immediately
			if (window!=null) { 
				window.resetMenuPending = false;
				if (!doomed)
					window.resetMenus(false);
			}
			else if (module.getEmployer()!=null)
				module.getEmployer().resetContainingMenuBar();
		}
		else { //find containerOfModule() and sets its menu bar as needing reset
			MesquiteWindow w = window;
			MesquiteModule mb = module;
			while (mb!=null) {
				if (mb.window!=null) {
					mb.window.resetMenuPending = true;
					return;
				}
				mb = mb.getEmployer();
			}
		}
	}
	/*.................................................................................................................*/
	/** This requests that the Windows menu of ALL menu bars be recomposed.*/
	public static final void resetAllWindowsMenus(){ 
		if (menuSuppression==0) {
			resetWindowsMenuPending = false;
			Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteWindow mw = (MesquiteWindow)obj;
				if (mw!=null) {
					if (!mw.resetMenuPending){
						MesquiteModule owner = mw.getOwnerModule();
						if (owner!=null && !owner.isDoomed())
							owner.recomposeWindowsMenu((MesquiteMenuBar)mw.getMenuBar(), mw);
					}
				}
			}
		}
		else 
			resetWindowsMenuPending = true; //set this so will later know that full reset is needed
	}
	static int allMenuBarRests = 0;
	/*.................................................................................................................*/
	/** This requests that ALL menu bars be recomposed.*/
	public static final void resetAllMenuBars() {
		if (!MesquiteThread.okToResetUI())
			return;
		resetWindowsMenuPending = false;
		if (menuSuppression==0) {
			allMenuBarRests++;
			MesquiteTimer timer = new MesquiteTimer();
			if (MesquiteTrunk.debugMode)
				timer.start();
				
			MesquiteTrunk.suppressResetCheckMenuItems();
			resetAllMenuPending = false;

			Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteWindow mw = (MesquiteWindow)obj;
				if (mw!=null) {
					mw.resetMenuPending = false;
					MesquiteModule owner = mw.getOwnerModule();
					if (owner==null || !owner.isDoomed())
						mw.resetMenus(false);
				}
			}
			MesquiteTrunk.resumeResetCheckMenuItems();
			if (MesquiteTrunk.debugMode){
				MesquiteModule.mesquiteTrunk.logln("\n>>>- All Menus Reset (" + allMenuBarRests + " times). This reset took " + timer.timeSinceLastInSeconds() + " seconds -<<< \n");  //temporary; to check efficiency
				timer.end();
				timer=null;
			}
		}
		else 
			resetAllMenuPending = true; //set this so will later know that full reset is needed
	}

	/*for debugging */
	public static boolean reportMenuReset = false;
	/*.................................................................................................................*/
	/** Increments suppression level of menus; if 0 then menus can be reset. */
	public static final void incrementMenuResetSuppression(){

		menuSuppression++;
		if (menuSuppression ==0)
			menuSuppression = 1;
		if (reportMenuReset){
			MesquiteMessage.printStackTrace("increment menuSuppression " + menuSuppression);
		}
	}
	/*.................................................................................................................*/
	/** Decrements suppression level of menus to zero; then menus can be reset. */
	public static final void zeroMenuResetSuppression(){
		menuSuppression =1;
		decrementMenuResetSuppression();
		warnAlreadyZero = false;
	}
	static boolean warnAlreadyZero = true;
	/*.................................................................................................................*/
	/** Decrements suppression level of menus; if 0 then menus can be reset. */
	public static final void decrementMenuResetSuppression(){
		if (menuSuppression ==0) {
			if (warnAlreadyZero)
				MesquiteMessage.warnProgrammer("decrementMenuResetSuppression when already zero");
			return;
		}
		menuSuppression--;
		if (menuSuppression<0)
			menuSuppression =0;
		if (reportMenuReset){
			MesquiteMessage.printStackTrace("decrement menuSuppression " + menuSuppression);
		}
		if (menuSuppression ==0){  //menu suppression just removed and requests pending; reset menus
			if (resetAllMenuPending)
				resetAllMenuBars();
			else {
				if (resetWindowsMenuPending)
					resetAllWindowsMenus();
				Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					MesquiteWindow mw = (MesquiteWindow)obj;
					MesquiteModule owner = mw.getOwnerModule();
					if (mw!=null && mw.resetMenuPending && (owner==null || !owner.isDoomed())){
						mw.resetMenuPending = false;
						mw.resetMenus(false);
					}
				}
			}
		}
	}
	/** Gets whether menu resets are to be suppressed. */
	public static final int getMenuResetSuppression(){
		return menuSuppression;
	}
	/** Sets suppression level of menu resets. */
	public static final void setMenuResetSuppression(int r){
		menuSuppression = r;
	}
	/*.................................................................................................................*/
	/** This indicates what menu is to be used (e.g., employer sets it).  All of its otherwise unplaced menu items, and those of its
	employees, will be placed there.*/
	public final void setMenuToUse(MesquiteMenuSpec menu){
		if (!useMenuBar)
			return;
		assignedMenuSpec = menu;
		if (menu!=null)
			menu.addGuestModule(module);
	}
	/*.................................................................................................................*/
	boolean usingGuestMenu = false;
	/** A module requests of this module to have its menu items as guests.*/
	public final void setUsingGuestMenu(boolean usingGuestMenu){
		this.usingGuestMenu = usingGuestMenu;
	}
	/*.................................................................................................................*/
	/** A module requests of this module to have its menu items as guests.*/
	public final void requestGuestMenuPlacement(MesquiteModule mb){
		if (moduleMenuSpec!=null){
			moduleMenuSpec.addGuestModule(mb);
			mb.setUsingGuestMenu(true);
		}
	}
	/*.................................................................................................................*/
	/** This requests a main menu for the MesquiteModule.  All of its otherwise unplaced menu items, and those of its
	employees, will be placed there.*/
	public final MesquiteMenuSpec makeMenu(String menuName){
		if (moduleMenuSpec!=null)
			MesquiteMessage.warnProgrammer("Error: module requesting second menu: " + menuName + " (module " + getClass().getName());
		return moduleMenuSpec = new MesquiteMenuSpec(null, menuName, module);
	}

	/*.................................................................................................................*/
	/** This returns the main menu for the MesquiteModule.*/
	public MesquiteMenuSpec getMenu(){
		return moduleMenuSpec;
	}
	/*.................................................................................................................*/
	/** This requests the main menu for the MesquiteModule be destroyed.*/
	public final void destroyMenu(){
		if (moduleMenuSpec!=null)
			moduleMenuSpec = null;
	}
	/*.................................................................................................................*/
	/** This requests an auxiliary menu for the MesquiteModule*/
	public final MesquiteMenuSpec addAuxiliaryMenu(String menuName){
		if (auxiliaryMenus==null)
			auxiliaryMenus= new Vector();
		MesquiteMenuSpec newMenu=new MesquiteMenuSpec(null, menuName, module);
		auxiliaryMenus.addElement(newMenu);
		return newMenu;
	}
	/*.................................................................................................................*/
	/** Finds a menu of given name from employers*/
	public final MesquiteMenuSpec findMenuAmongEmployers(String menuName){
		if (menuName==null)
			return null;
		if (moduleMenuSpec!=null)
			if (menuName.equals(moduleMenuSpec.getLabel()))
				return moduleMenuSpec;
		if (auxiliaryMenus!=null) {
			for (int i=0; i<auxiliaryMenus.size(); i++)
				if (menuName.equals(((MesquiteMenuSpec)auxiliaryMenus.elementAt(i)).getLabel()))
					return ((MesquiteMenuSpec)auxiliaryMenus.elementAt(i));
		}
		if (module.getEmployer()!=null)
			return module.getEmployer().findMenuAmongEmployers(menuName);
		return null;
	}
	/*.................................................................................................................*/
	/** DOCUMENT */
	public void checkMISVector() {
		if (menuItemsSpecs==null)
			menuItemsSpecs = new MenuItemsSpecsVector();
	}
	/*.................................................................................................................*/
	/** Sets whether module's menu items are to appear in menubar or not. */
	public final void setUseMenubar(boolean useMenuBar){
		this.useMenuBar=useMenuBar;
	}
	/*.................................................................................................................*/
	/** Returns whether module's menu items are to appear in menubar or not. Does not apply to menu items in special menus (file, edit, windows, help).
	Menu items don't appear in menu bar if the useMenuBar flag is set to false either for this module or for one of its employers */
	public final boolean getUseMenubar(){
		if (!useMenuBar)
			return false;
		if (window==null && moduleMenuSpec==null && assignedMenuSpec == null && module.getEmployer()!=null) {
			return module.getEmployer().getUseMenubar(); 
		}
		return true;
	}
	/*.................................................................................................................*/
	/** Shows module's menus in popup. */
	public final void showPopUp(Container cont, int x, int y){
		if (cont == null || !cont.isVisible())
			return;
		if (popUp==null)
			popUp = new MRPopup(cont);
		else
			popUp.removeAll();
		composeMenuDescendants(popUp);
		try {
			cont.add(popUp);
			popUp.show(cont, x,y);
		}
		catch (Exception e){
		}
		//cont.remove(popUp); //todo: this is zapping the menu on linux immediately
	}
	/*.................................................................................................................*/
	/** Called during Mesquite startup when the list of available modules is being constructed; is mined for information about menu commands.
	When "accumulating" is set, the method is being called not to build menus but to accumulate information about them.
	Best to be overridden for any defined menus.  The reason for this separate method is so that it can serve both to define menus when modules are hired,
	but also can serve to accumulate documentation about available menu items at Mesquite startup.  In the latter, strange things are done to subvert addMenuItem
	addSubmenu and other such methods below so that they divert information to the information accumulator instead of actually making menu item specifications. The system would
	have been designed a bit differently had information accumulation been a function from the start.*/
	public void defineMenus(boolean accumulating){
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a dividing line to the module's containing menu. */
	public final MesquiteMenuItemSpec addMenuSeparator(){
		return addMenuItem("-",null);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a dividing line to the module's containing menu.
	 * @deprecated
	 *     */
	public  final MesquiteMenuItemSpec addMenuLine(){
		return addMenuSeparator();
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a menu item to the module's containing menu.  When selected, the given command will be executed. */
	public final MesquiteMenuItemSpec addMenuItem(String itemName, MesquiteCommand command){
		MesquiteMenuItemSpec mmis = MesquiteMenuItemSpec.getMMISpec(null, itemName, module, command);
		return (mmis);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a menu item to the given menu.  When selected, the given command will be executed. */
	public final MesquiteMenuItemSpec addMenuItem(MesquiteMenuSpec whichMenu, String itemName, MesquiteCommand command){
		MesquiteMenuItemSpec mmis =MesquiteMenuItemSpec.getMMISpec(whichMenu, itemName, module, command);
		return (mmis);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a menu item to the given menu.. */
	public final MesquiteMenuItemSpec addMenuItem(MesquiteMenuSpec whichMenu, MesquiteMenuItemSpec item){
		item.setInMenu(whichMenu);
		menuItemsSpecs.addElement(item, false);
		return (item);
	}
	/*.................................................................................................................*/
	/** Adds a series of menu items, one for each of the modules belonging to the given dutyClass, to a menu.  This differs from
	the similar addSubMenu in that menu items are not placed in a submenu.  Each menu item has its own command associated. */
	public final MesquiteMenuItemSpec addModuleMenuItems(MesquiteMenuSpec whichMenu, MesquiteCommand command, Class dutyClass){
		if (dutyClass ==null)
			return null;
		MesquiteMenuItemSpec mmis =MesquiteMenuItemSpec.getMMISpec(whichMenu, null, module, command);
		if (mmis==null) return null;
		mmis.setList(dutyClass);
		return (mmis);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a submenu of the given name.  This submenu will not have a command associated with it.  Instead, menu items with their
	own independent commands can be added to it using addItemToSubmenu. */
	public final MesquiteSubmenuSpec addSubmenu(MesquiteMenuSpec whichMenu, String submenuName){
		MesquiteSubmenuSpec mmis =MesquiteSubmenuSpec.getMSSSpec(whichMenu, submenuName, module);
		return (mmis);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a submenu of the given name.  What to fill the submenu with SHOULD BE INDICATED BY A SUBSEQUENT CALL TO MesquiteSubmenuSpec.setList. 
	Then, submenu created will be automatically
	formulated, and additional items should *not* be added using addItemToSubmenu.  The submenu itself has a command
	stored with it, and upon receiving a selection even it will append the <strong>item name</strong> selected as argument.*/
	public final MesquiteSubmenuSpec addSubmenu(MesquiteMenuSpec whichMenu, String submenuName, MesquiteCommand command){
		MesquiteSubmenuSpec mmis =addSubmenu(whichMenu, submenuName);
		if (mmis==null) return null;
		mmis.setCommand(command);
		return (mmis);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a submenu of the given name with all the modules belonging to the given dutyClass. The submenu created will be automatically
	formulated, and additional items should *not* be added using addItemToSubmenu.  The submenu itself has a command
	stored with it, and upon receiving a selection even it will append the <strong>item name</strong> selected as argument.*/
	public final MesquiteSubmenuSpec addSubmenu(MesquiteMenuSpec whichMenu, String submenuName, MesquiteCommand command, Class dutyClass){
		MesquiteSubmenuSpec mmis =addSubmenu(whichMenu, submenuName);
		if (mmis==null) return null;
		mmis.setCommand(command);
		mmis.setList(dutyClass);
		return (mmis);
	}
	/*.................................................................................................................*/
	/** Adds a submenu of the given name with all the items in the ListableVector listed as items. The submenu created will be automatically
	formulated, and additional items should *not* be added using addItemToSubmenu.  The submenu itself has a command
	stored with it, and upon receiving a selection even it will append the <strong>number of the item</strong> selected as argument.*/
	public final MesquiteSubmenuSpec addSubmenu(MesquiteMenuSpec whichMenu, String submenuName, MesquiteCommand command, ListableVector lVector){
		MesquiteSubmenuSpec mmis =addSubmenu(whichMenu, submenuName);
		if (mmis==null) return null;
		mmis.setCommand(command);
		mmis.setList(lVector);
		return (mmis);
	}
	/*.................................................................................................................*/
	/** Adds a submenu of the given name with all the items in the String array listed as items. The submenu created will be automatically
	formulated, and additional items should *not* be added using addItemToSubmenu.  The submenu itself has a command
	stored with it, and upon receiving a selection even it will append the <strong>number of the item</strong> selected as argument.*/
	public final MesquiteSubmenuSpec addSubmenu(MesquiteMenuSpec whichMenu, String submenuName, MesquiteCommand command, StringLister names){
		MesquiteSubmenuSpec mmis =addSubmenu(whichMenu, submenuName);
		if (mmis==null) return null;
		mmis.setCommand(command);
		mmis.setList(names);
		return (mmis);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a dividing line to the given submenu of the given menu. */
	public final MesquiteMenuItemSpec addLineToSubmenu(MesquiteMenuSpec whichMenu, MesquiteSubmenuSpec submenu){
		return addItemToSubmenu(whichMenu, submenu, "-", null);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a menu item to the given submenu of the given menu.  When selected, the given command will be executed. */
	public final MesquiteMenuItemSpec addItemToSubmenu(MesquiteMenuSpec whichMenu, MesquiteSubmenuSpec submenu, String itemName, MesquiteCommand command){
		MesquiteMenuItemSpec mmis =MesquiteMenuItemSpec.getMMISpec(whichMenu, itemName, module, command);
		if (mmis!=null)
			mmis.setInSubmenu(submenu);
		return (mmis);
	}
	/*.................................................................................................................*/
	/* ����� */
	/** Adds a menu item to the given submenu of the given menu.  When selected, the given command will be executed. */
	public final MesquiteCMenuItemSpec addCheckMenuItemToSubmenu(MesquiteMenuSpec whichMenu, MesquiteSubmenuSpec submenu, String itemName, MesquiteCommand command, MesquiteBoolean b){
		MesquiteCMenuItemSpec mmis =MesquiteCMenuItemSpec.getMCMISpec(whichMenu, itemName, module, command, b);
		if (mmis!=null)
			mmis.setInSubmenu(submenu);
		return (mmis);
	}

	/*.................................................................................................................*/
	/* ����� */
	/** Adds a menu item to the given submenu of the given menu.  When selected, the given command will be executed. */
	public final MesquiteMenuItemSpec addItemToSubmenu(MesquiteMenuSpec whichMenu, MesquiteSubmenuSpec submenu, MesquiteMenuItemSpec item){
		checkMISVector();
		item.setInMenu(whichMenu);
		item.setInSubmenu(submenu);
		menuItemsSpecs.addElement(item, false);
		return (item);
	}
	/*.................................................................................................................*/
	/** Add check menu item.  Checked according to state of passed MesquiteBoolean */
	public final MesquiteCMenuItemSpec addCheckMenuItem(MesquiteMenuSpec whichMenu, String itemName, MesquiteCommand command, MesquiteBoolean checkBoolean){
		MesquiteCMenuItemSpec mmis =MesquiteCMenuItemSpec.getMCMISpec(whichMenu, itemName, module, command, checkBoolean);
		return (mmis);
	}
	/*.................................................................................................................*/
	public int getNumMenuItemSpecs(){
		if (menuItemsSpecs==null)
			return 0;
		return menuItemsSpecs.size();
	}
	/*.................................................................................................................*/
	/** Delete indicated menu item. */
	public final void deleteItemsOfMenu(MesquiteMenuSpec whichMenu){
		if (menuItemsSpecs!=null) {
			for (int i = menuItemsSpecs.size()-1; i>=0; i--){
				MesquiteMenuItemSpec mmis = (MesquiteMenuItemSpec)menuItemsSpecs.elementAt(i);
				if (nestedInMenu(mmis, whichMenu))
					deleteMenuItem(mmis);
			}
		}

	}
	private boolean nestedInMenu(MesquiteMenuItemSpec mmis, MesquiteMenuSpec whichMenu){
		MesquiteMenuItemSpec m = mmis;
		while (m != null && m.getMenu() != whichMenu)
			m = m.getMenu();
		return m != null;

	}
	/*.................................................................................................................*/
	/** Delete indicated menu item. */
	public final void deleteAllMenuItems(){
		if (menuItemsSpecs!=null) {
			for (int i = menuItemsSpecs.size()-1; i>=0; i--)
				deleteMenuItem((MesquiteMenuItemSpec)menuItemsSpecs.elementAt(i));
		}

	}
	/*.................................................................................................................*/
	/** Delete indicated menu item. */
	public final void deleteMenuItem(MesquiteMenuItemSpec whichMenuItem){
		if (whichMenuItem!=null) {
			if (menuItemsSpecs!=null) {
				menuItemsSpecs.removeElement(whichMenuItem, false);
				if (whichMenuItem instanceof MesquiteCMenuItemSpec)
					((MesquiteCMenuItemSpec)whichMenuItem).releaseBoolean();
				whichMenuItem.disconnect();
			}
		}
	}
	/*.................................................................................................................*/
	/** Return the specific menu that had been assigned to contain its items (as opposed to the default menu to contain them). */
	public final MesquiteMenuSpec getContainingMenuSpec() {
		if (assignedMenuSpec!=null)
			return assignedMenuSpec;
		else if (moduleMenuSpec!=null) {
			return moduleMenuSpec;
		}
		else if (module.getEmployer()!=null)
			return module.getEmployer().getContainingMenuSpec();
		else
			return null;
	}
	/*.................................................................................................................*/
	/**
	This composes the menu bar for a MesquiteModule.  It is only called for modules that own windows (i.e., a frame that
	can own a menu bar).  It adds menus in the following sequence:
	<ol>
	<li> the File menu belonging to the Mesquite trunk module,
	<li> the menus belonging to this module's ancestors (employers).  Only the directly ancestral menu items are
	shown; not the menu items that may fall within an employer's menu but which are contributed by aunts and sisters
	of the current module
	<li> the menu items of the current module on its menu or if none on an employer's menu
	<li> the menu items of the module's employees (on the module's menu or if none on an employer's menu)
	<li> the Menus of descendant employees
	<li> the auxiliary menus of the current module
	<li> the Windows menu belonging to the Mesquite trunk module
	</ol>
	 */

	public final synchronized void composeMenuBar(MesquiteMenuBar menuBar, MesquiteWindow whichWindow) {
		if (module.isDoomed())
			return;
		if (System.getProperty("os.name").indexOf("Mac OS")<0 && whichWindow.minimalMenus)
			return; //minimalMenu windows don't have menu bars in Windows etc.
		try {
			composeCount++;
			/**/
			if (MesquiteTrunk.fileMenu==null)
				MesquiteMessage.warnProgrammer("WARNING: file menu null in composeMenuBar for " + module.getName());
			/*else if (window==null) {
				MesquiteMessage.warnProgrammer("WARNING: window null in composeMenuBar for " + module.getName());
				MesquiteMessage.printStackTrace();
				return;
			}
			 */
			else if (menuBar==null) {
				MesquiteMessage.warnProgrammer("WARNING: menuBar null in composeMenuBar for " + module.getName());
			}
			if (module.isDoomed())
				return;
			composeFileMenu(menuBar, MesquiteTrunk.fileMenu, whichWindow);
			if (module.isDoomed())
				return;
			composeEditMenu(menuBar, MesquiteTrunk.editMenu, whichWindow);
			if (module.isDoomed())
				return;
			composeSpecificMenu(menuBar, null, MesquiteTrunk.charactersMenu, true);
			if (module.isDoomed())
				return;
			composeSpecificMenu(menuBar, null, MesquiteTrunk.treesMenu, true);

			//MENUS FORMERLY TO RIGHT OF WINDOW=SPECIFIC
			if (module.isDoomed())
				return;
			MesquiteMenu cMenu = composeSpecificMenuByZones(menuBar, null, MesquiteTrunk.analysisMenu, true);
			//MesquiteMenu.add(cMenu, new MesquiteMenuItem("Save Window as Macro...", module, MesquiteModule.makeCommand("saveMacroForAnalysis", whichWindow)));  //commandArgument
			//	MesquiteSubmenu macrosSubmenu = makeMacrosSubmenu(cMenu, module.getFileCoordinator(), MesquiteMacro.ANALYSIS, "Macros Making Windows");
			//	cMenu.add(macrosSubmenu);
			//addBottom(cMenu, null, "!");
			if (module.isDoomed())
				return;
			MesquiteMenu wMenu = fillWindowsMenu(menuBar, null, whichWindow);
			menuBar.add(wMenu);
			if (module.isDoomed())
				return;
			//=============MENUS FORMERLY TO RIGHT OF WINDOW=SPECIFIC

			//@@@@@@@@========  menus that are specific to this module/window
			resetEmbeddedMenus(whichWindow);

			//else {
			//	if (MesquiteTrunk.isMacOSX()){
			Menu spot = new Menu(leftBracket);
			//	spot.setFont(new Font ("SanSerif", Font.PLAIN, 12));
			spot.add(new MenuItem("Menus between " + leftBracket + " " + rightBracket));
			spot.add(new MenuItem("  refer to current window"));
			menuBar.add(spot);
			int numBeforeSpecificMenus = menuBar.getMenuCount();
			MesquiteMenu menu;  
			if (moduleMenuSpec!=null) {
				menu = MesquiteMenu.getMenu(moduleMenuSpec);
			}
			else menu = null;
			MesquiteMenu ancestralMenu=null;
			if (module.isDoomed())
				return;
			if (module.getEmployer()!=null)
				ancestralMenu= module.getEmployer().composeMenuAncestors(menuBar);

			MesquiteMenu menuToUse=null;
			menuToUse=menu;

			if (menuToUse !=null  && !module.isDoomed())
				addMyMenuItems(menuToUse);

			ListableVector L =module.getEmployeeVector();
			if (L!=null) {
				int num = L.size();
				for (int i=0; i<num; i++){
					Object obj = L.elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					if (mb !=null && !mb.isDoomed() && mb.getUseMenubar() && !mb.usingGuestMenu && mb.window==null && mb.moduleMenuSpec==null && mb.assignedMenuSpec == null) 
						mb.composeMenuDescendants(menuToUse);
				}
			}


			addBottom(menu, null, "%");
			if (menu!=null && menu.getItemCount()>0) {  //why is this menu and not menuToUse????
				menuBar.add(menu);
			}

			if (module.isDoomed())
				return;
			composeMenusOfDescendants(menuBar);

			if (module.isDoomed())
				return;
			if (auxiliaryMenus!=null) {
				int num = auxiliaryMenus.size();
				for (int i=0; i<num; i++){
					Object obj = auxiliaryMenus.elementAt(i);
					MesquiteMenuSpec m = (MesquiteMenuSpec)obj;
					if (m!=null) {
						composeSpecificMenu(menuBar, null, m, true);
					}
				}
			}
			//}
			if (!MesquiteTrunk.isMacOSX() && whichWindow.isLoneWindow()){
				MesquiteMenu wwMenu = fillWindowMenu(menuBar, whichWindow);
				menuBar.add(wwMenu);
			}
			if (numBeforeSpecificMenus == menuBar.getMenuCount()){
				menuBar.remove(spot);
			}
			else {
				Menu spot2 = new Menu(rightBracket);
				spot2.add(new MenuItem("Menus between " + leftBracket + " " + rightBracket));
				spot2.add(new MenuItem("  refer to current window"));
				menuBar.add(spot2);
			}
			//		}
			MesquiteMenu hMenu = composeSpecificMenu(menuBar, null, MesquiteTrunk.helpMenu, false);

			menuBar.setHelpMenu(hMenu);
			//@@@@@@@@========  menus that are specific to this module/window



			//RIGHT MENUS USED TO GO HERE


			if (module.isDoomed())
				return;
			if (menuBar!=null) {
				for (int i=menuBar.getMenuCount()-1; i>=1; i--){  //leave first (file menu) in place
					try{
						if (menuEmpty(menuBar.getMenu(i))) 
							menuBar.remove(i);
						else
							removeEmptySubmenus(menuBar.getMenu(i));
					}
					catch (Exception e){
					}
				}
			}
			if (menuBar!=null) {
				for (int i=0; i<menuBar.getMenuCount(); i++)  
					sortSubmenusBySpecsOrder(menuBar.getMenu(i));  //this is a kludge to repair an issue of ordering: submenus of submenus floating to top
			}

		}
		catch (NullPointerException e){
			e.printStackTrace();
		}
	}

	public void removeEmptySubmenus(Menu parent){
		for (int i=parent.getItemCount()-1; i>=0; i--){  
			try{
				MenuItem item = parent.getItem(i);
				if (item instanceof Menu){
					if (menuEmpty((Menu)item))
						parent.remove(i);
					else 
						removeEmptySubmenus((Menu)item);
				}
			}
			catch (Exception e){
			}
		}
	}
	/*-------------------------------------------------------------*/
	public Vector getEmbeddedMenusVector(){
		return embeddedMenusVector;
	}

	public void resetEmbeddedMenus(MesquiteWindow whichWindow){

		if ((window != null && window.getShowInfoBar()) && (MesquiteTrunk.isMacOSX() || (whichWindow == null || !whichWindow.isLoneWindow()))) // && MesquiteTrunk.isMacOSX())   //these menus belong in the window, as long as an info bar is shown
			embeddedMenusVector = composeEmbeddedMenuBar(whichWindow);
		else
			embeddedMenusVector = null;
	}
	Vector embeddedMenusVector = null;
	public Vector composeEmbeddedMenuBar(MesquiteWindow whichWindow){
		MesquitePopup menu;
		Vector menuVector = new Vector();

		if (moduleMenuSpec!=null) {
			menu = MesquitePopup.getPopupMenu(moduleMenuSpec, whichWindow.infoBar);
		}
		else menu = null;
		MesquitePopup ancestralMenu=null;
		if (module.isDoomed())
			return null;
		if (module.getEmployer()!=null)
			ancestralMenu= module.getEmployer().composeMenuAncestors(menuVector);

		MesquitePopup menuToUse=menu;

		if (menuToUse !=null  && !module.isDoomed())
			addMyMenuItems(menuToUse);

		ListableVector L =module.getEmployeeVector();
		if (L!=null) {
			int num = L.size();
			for (int i=0; i<num; i++){
				Object obj = L.elementAt(i);
				MesquiteModule mb = (MesquiteModule)obj;
				if (mb !=null && !mb.isDoomed() && mb.getUseMenubar() && !mb.usingGuestMenu && mb.window==null && mb.moduleMenuSpec==null && mb.assignedMenuSpec == null) 
					mb.composeMenuDescendants(menuToUse);
			}
		}


		addBottom(menu, null, "%");
		if (menu!=null && menu.getItemCount()>0) {  //why is this menu and not menuToUse????
			menuVector.add(menu);
		}

		if (module.isDoomed())
			return null;
		composeMenusOfDescendants(menuVector);

		if (module.isDoomed())
			return null;
		if (auxiliaryMenus!=null) {
			int num = auxiliaryMenus.size();
			for (int i=0; i<num; i++){
				Object obj = auxiliaryMenus.elementAt(i);
				MesquiteMenuSpec m = (MesquiteMenuSpec)obj;
				if (m!=null) {
					composeSpecificMenu(menuVector, null, m, true);
				}
			}
		}

		if (module != MesquiteTrunk.mesquiteTrunk){  //Window menu
			MesquitePopup windowMenu = fillWindowMenu(menuVector, whichWindow);
			menuVector.add(windowMenu);
		}
		return menuVector;
		/*
		 * MesquiteMenu ancestralMenu=null;
		if (module.isDoomed())
			return;
		if (module.getEmployer()!=null)
			ancestralMenu= module.getEmployer().composeMenuAncestors(menuBar);

		MesquiteMenu menuToUse=null;
		menuToUse=menu;

		if (menuToUse !=null  && !module.isDoomed())
			addMyMenuItems(menuToUse);

		ListableVector L =module.getEmployeeVector();
		if (L!=null) {
			int num = L.size();
			for (int i=0; i<num; i++){
				Object obj = L.elementAt(i);
				MesquiteModule mb = (MesquiteModule)obj;
				if (mb !=null && !mb.isDoomed() && mb.getUseMenubar() && mb.window==null && mb.moduleMenuSpec==null && mb.assignedMenuSpec == null) 
					mb.composeMenuDescendants(menuToUse);
			}
		}


		addBottom(menu, null, "%");
		if (menu!=null && menu.getItemCount()>0) {  //why is this menu and not menuToUse????
			menuBar.add(menu);
		}

		if (module.isDoomed())
			return;
		composeMenusOfDescendants(menuBar);

		if (module.isDoomed())
			return;
		if (auxiliaryMenus!=null) {
			int num = auxiliaryMenus.size();
			for (int i=0; i<num; i++){
				Object obj = auxiliaryMenus.elementAt(i);
				MesquiteMenuSpec m = (MesquiteMenuSpec)obj;
				if (m!=null) {
					composeSpecificMenu(menuBar, null, m, true);
				}
			}
		}
		if (module != MesquiteTrunk.mesquiteTrunk){
			MesquiteMenu windowMenu = fillWindowMenu(menuBar, whichWindow);
			menuBar.add(windowMenu);
		}
		 */
	}
	void addBottom(Menu menu, MesquiteModule mb, String e){
		if (menu == null)
			return;
		long menuOwnerID = -1;
		MesquiteModule useModule = module;
		if (mb !=null) 
			useModule = mb;
		long moduleID = useModule.getID();
		boolean myMenu = false;
		Vector guests=null;

		MesquiteMenuSpec mms = null;
		if (menu instanceof MesquitePopup)
			mms = ((MesquitePopup)menu).getSpecification();
		else if (menu instanceof MesquiteMenu)
			mms = ((MesquiteMenu)menu).getSpecification();
		if (mms!=null) {
			menuOwnerID = mms.getOwnerModuleID();
			myMenu = (mms == useModule.moduleMenuSpec || mms == useModule.assignedMenuSpec);
			guests = mms.getGuests();
		}
		if (guests!=null)
			for (int i=0; i<guests.size(); i++) { 
				MesquiteModule guest = (MesquiteModule)guests.elementAt(i);
				guest.addMyMenuItems(menu);
			}


		/*
	# % @
		 */
		if (menuOwnerID == moduleID && myMenu)
			addMacrosMenus(menu, useModule);
		if (guests!=null)
			for (int i=0; i<guests.size(); i++) { 
				MesquiteModule guest = (MesquiteModule)guests.elementAt(i);
				addMacrosMenus(menu, guest);
			}

	}
	private void addMacrosMenus(Menu menu, MesquiteModule useModule){
		if (useModule.getAutoSaveMacros()) {
			MesquiteMenu.add(menu, new MenuItem("-"));
			MesquiteMenu.add(menu, new MesquiteMenuItem("Save Macro for " +  useModule.getNameForMenuItem() + "...", useModule, useModule.makeCommand("saveMacro", useModule)));  //commandArgument
		}
		if (!(useModule instanceof FileCoordinator)) {
			MesquiteSubmenu ms = makeMacrosSubmenu(menu, useModule, 0, "Macros for " + useModule.getNameForMenuItem());
			if (ms!=null) 
				menu.add(ms);
		}
	}
	/*.................................................................................................................*/
	/**
	This recomposes the Windows menu for a MesquiteModule.  It is only called for modules that own windows (i.e., a frame that
	can own a menu bar). 
	 */
	public final synchronized void recomposeWindowsMenu(MesquiteMenuBar menuBar, MesquiteWindow whichWindow) {
		MesquiteMenu currentWindowsMenu = null;
		if (menuBar==null)
			return;
		else {
			int currentWindowsNumber = menuBar.getMenuCount()-2;
			for (int i=menuBar.getMenuCount()-1; i>=0 && currentWindowsMenu==null; i--){  //leave first (file menu) in place
				Menu m = menuBar.getMenu(i);
				if (m instanceof MesquiteMenu && ((MesquiteMenu)m).getSpecification() == MesquiteTrunk.windowsMenu){
					currentWindowsMenu = (MesquiteMenu)m;
					currentWindowsMenu.removeAll();
					currentWindowsNumber = i;
				}
			}
		}
		fillWindowsMenu(menuBar, currentWindowsMenu, whichWindow);
	}
	private MesquitePopup fillWindowMenu(Vector menuBar, MesquiteWindow whichWindow){
		MesquitePopup wMenu = MesquitePopup.getPopupMenu(new MesquiteMenuSpec(null, "Window", module), whichWindow.infoBar);  
		if (whichWindow!=null) {
			if (whichWindow.permitViewMode()){   
				MesquiteSubmenu setViewModeMenu = MesquiteSubmenu.getSubmenu("View Mode", wMenu, module);
				setViewModeMenu.add(new MesquiteMenuItem("Graphics (Standard)", module, MesquiteModule.makeCommand("showPage", whichWindow), Integer.toString(0)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Text", module, module.makeCommand("showPage", whichWindow), Integer.toString(1)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Parameters", module, module.makeCommand("showPage", whichWindow), Integer.toString(2)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Modules", module, module.makeCommand("showPage", whichWindow), Integer.toString(3)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Citations", module, module.makeCommand("showPage", whichWindow), Integer.toString(4)));  //commandArgument
				wMenu.add(setViewModeMenu);
			}
			//wMenu.add(whichWindow.infoBarMenuItem);
			if (module.getEmployer()!=null && module.getEmployer().getClonableEmployeeCommand(module)!=null) {
				wMenu.add(whichWindow.cloneWindowMenuItem);
			}
			//experimental
			wMenu.add("-");
			wMenu.add(whichWindow.saveRecipeMenuItem);


			MesquiteSubmenu macrosSubmenu = makeMacrosSubmenu(wMenu, module.getFileCoordinator(), 0, "Macros");
			if (macrosSubmenu !=null) {
				wMenu.add(macrosSubmenu);
			}
			MesquiteSubmenu scriptingSubmenu=MesquiteSubmenu.getSubmenu("Scripting", wMenu, module);  //make submenu
			scriptingSubmenu.add(whichWindow.snapshotMenuItem); //��� scripting
			scriptingSubmenu.add(whichWindow.sendScriptMenuItem);  //��� scripting
			wMenu.add(scriptingSubmenu);
			wMenu.add("-");
			whichWindow.setPopTileMenuItemNames();
			if (!whichWindow.isPoppedOut()) {
				wMenu.add(whichWindow.popOutWindowMenuItem);
				wMenu.add(whichWindow.tileOutWindowMenuItem);
			} else {
				if (whichWindow.getPopAsTile())
					wMenu.add(whichWindow.tileOutWindowMenuItem);
				else 
					wMenu.add(whichWindow.popOutWindowMenuItem);
			}

			//wMenu.add("-", insertPoint);
		}
		return wMenu;
	}
	private MesquiteMenu fillWindowMenu(MesquiteMenuBar menuBar, MesquiteWindow whichWindow){
		MesquiteMenu wMenu = MesquiteMenu.getMenu(new MesquiteMenuSpec(null, "Window", module));  
		if (whichWindow!=null) {
			if (whichWindow.permitViewMode()){  
				MesquiteSubmenu setViewModeMenu = MesquiteSubmenu.getSubmenu("View Mode", wMenu, module);
				setViewModeMenu.add(new MesquiteMenuItem("Graphics (Standard)", module, MesquiteModule.makeCommand("showPage", whichWindow), Integer.toString(0)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Text", module, module.makeCommand("showPage", whichWindow), Integer.toString(1)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Parameters", module, module.makeCommand("showPage", whichWindow), Integer.toString(2)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Modules", module, module.makeCommand("showPage", whichWindow), Integer.toString(3)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Citations", module, module.makeCommand("showPage", whichWindow), Integer.toString(4)));  //commandArgument
				wMenu.add(setViewModeMenu);
			}
			//wMenu.add(whichWindow.infoBarMenuItem);
			if (module.getEmployer()!=null && module.getEmployer().getClonableEmployeeCommand(module)!=null) {
				wMenu.add(whichWindow.cloneWindowMenuItem);
			}
			//experimental
			wMenu.add("-");
			wMenu.add(whichWindow.saveRecipeMenuItem);


			MesquiteSubmenu macrosSubmenu = makeMacrosSubmenu(wMenu, module.getFileCoordinator(), 0, "Macros");
			if (macrosSubmenu !=null) {
				wMenu.add(macrosSubmenu);
			}
			MesquiteSubmenu scriptingSubmenu=MesquiteSubmenu.getSubmenu("Scripting", wMenu, module);  //make submenu
			scriptingSubmenu.add(whichWindow.snapshotMenuItem); //��� scripting
			scriptingSubmenu.add(whichWindow.sendScriptMenuItem);  //��� scripting
			wMenu.add(scriptingSubmenu);
			wMenu.add("-");
			whichWindow.setPopTileMenuItemNames();
			if (!whichWindow.isPoppedOut()) {
				wMenu.add(whichWindow.popOutWindowMenuItem);
				wMenu.add(whichWindow.tileOutWindowMenuItem);
			} else {
				if (whichWindow.getPopAsTile())
					wMenu.add(whichWindow.tileOutWindowMenuItem);
				else 
					wMenu.add(whichWindow.popOutWindowMenuItem);
			}

			//wMenu.add("-", insertPoint);
		}
		return wMenu;
	}	

	private MesquiteMenu fillWindowsMenu(MesquiteMenuBar menuBar, MesquiteMenu currentWindowsMenu, MesquiteWindow whichWindow){
		MesquiteMenu wMenu = currentWindowsMenu; //composeSpecificMenu(menuBar, currentWindowsMenu, MesquiteTrunk.windowsMenu, false);
		if (wMenu ==null)
			wMenu= MesquiteMenu.getMenu(MesquiteTrunk.windowsMenu);
		if (whichWindow!=null) {
			/*
			 * if (whichWindow.permitViewMode()){   
				MesquiteSubmenu setViewModeMenu = MesquiteSubmenu.getSubmenu("View Mode", wMenu, module);
				setViewModeMenu.add(new MesquiteMenuItem("Graphics (Standard)", module, module.makeCommand("showPage", whichWindow), Integer.toString(0)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Text", module, module.makeCommand("showPage", whichWindow), Integer.toString(1)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Parameters", module, module.makeCommand("showPage", whichWindow), Integer.toString(2)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Modules", module, module.makeCommand("showPage", whichWindow), Integer.toString(3)));  //commandArgument
				setViewModeMenu.add(new MesquiteMenuItem("Citations", module, module.makeCommand("showPage", whichWindow), Integer.toString(4)));  //commandArgument
				wMenu.add(setViewModeMenu);
			}
			//wMenu.add(whichWindow.infoBarMenuItem);
			 */
			MesquiteMenuItem explanationsMenuItem = new MesquiteMenuItem(whichWindow.explanationsMenuItemSpec);
			wMenu.add(explanationsMenuItem);
			/*if (module.getEmployer()!=null && module.getEmployer().getClonableEmployeeCommand(module)!=null) {
				wMenu.add(whichWindow.cloneWindowMenuItem);
			}
			 */
			//experimental
			wMenu.add("-");
			/*
			wMenu.add(whichWindow.saveRecipeMenuItem);


			MesquiteSubmenu macrosSubmenu = makeMacrosSubmenu(wMenu, module.getFileCoordinator(), 0, "Macros");
			if (macrosSubmenu !=null) {
				wMenu.add(macrosSubmenu);
			}
			MesquiteSubmenu scriptingSubmenu=MesquiteSubmenu.getSubmenu("Scripting", wMenu, module);  //make submenu
			scriptingSubmenu.add(whichWindow.snapshotMenuItem); //��� scripting
			scriptingSubmenu.add(whichWindow.sendScriptMenuItem);  //��� scripting
			wMenu.add(scriptingSubmenu);
			 */
			//wMenu.add("-", insertPoint);
		}
		wMenu.add("-");
		wMenu.add(new MesquiteMenuItem("Bring All Windows To Front", module, MesquiteTrunk.mesquiteTrunk.showAllCommand));
		MesquiteSubmenu msm = new MesquiteSubmenu("Projects", wMenu, MesquiteModule.mesquiteTrunk);
		wMenu.add(msm);
		for (int i=0; i< MesquiteTrunk.projects.getNumProjects(); i++){
			MesquiteProject proj = MesquiteTrunk.projects.getProject(i);
			msm.add(new MesquiteMenuItem(proj.getName(), proj.getCoordinatorModule(), MesquiteModule.makeCommand("allToFront", proj.getCoordinatorModule())));
		}
		addCurrentWindows(wMenu);
		composeSpecificMenu(menuBar, wMenu, MesquiteTrunk.windowsMenu, false);
		return wMenu;
	}
	/*.................................................................................................................*/
	boolean menuEmpty(Menu menu) {
		if (menu.getItemCount() ==0)
			return true;
		else {
			for (int i=0; i<menu.getItemCount(); i++) {
				MenuItem item = menu.getItem(i);
				if (item instanceof Menu) {  //item is submenu; return nonempty if item in it
					if (!menuEmpty((Menu)item))
						return false;
				}
				else
					return false; //item is not submenu; return nonempty
			}
			return true;
		}
	}
	/*.................................................................................................................*/
	final MesquiteMenu composeMenuAncestors(MesquiteMenuBar menuBar) {
		MesquiteMenu putHere;
		if (moduleMenuSpec!=null) {
			putHere = MesquiteMenu.getMenu(moduleMenuSpec);
			addMyMenuItems(putHere);
			//addBottom(putHere, null, "^");
			menuBar.add(putHere);
			if (module.getEmployer()!=null) 
				module.getEmployer().composeMenuAncestors(menuBar);
			return putHere;
		}
		else if (module.getEmployer()!=null) {
			putHere = module.getEmployer().composeMenuAncestors(menuBar);
			if (putHere!=null) {
				addMyMenuItems(putHere);
			}
			return putHere;
		}
		return null;
	}
	/*.................................................................................................................*/
	final MesquitePopup composeMenuAncestors(Vector menuBar) {
		MesquitePopup putHere;
		if (moduleMenuSpec!=null) {
			putHere = MesquitePopup.getPopupMenu(moduleMenuSpec, null);
			addMyMenuItems(putHere);
			//addBottom(putHere, null, "^");
			menuBar.addElement(putHere);
			if (module.getEmployer()!=null) 
				module.getEmployer().composeMenuAncestors(menuBar);
			return putHere;
		}
		else if (module.getEmployer()!=null) {
			putHere = module.getEmployer().composeMenuAncestors(menuBar);
			if (putHere!=null) {
				addMyMenuItems(putHere);
			}
			return putHere;
		}
		return null;
	}
	/*.................................................................................................................*/
	final boolean inMenuBar(Menu menu) {  
		if (menu==null)
			return false;
		if (menu instanceof MesquitePopup)
			return false;
		MenuContainer p = menu.getParent();
		while (p!=null) {
			if (p instanceof MesquitePopup)
				return false;
			else if (p instanceof MenuBar) 
				return true;
			else if (p instanceof MenuComponent)
				p = ((MenuComponent)p).getParent();
			else
				return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	final void composeMenuDescendants(Menu menu) {  
		if (doomed) 
			;//MesquiteMessage.println("Error: composing menu of module that has been turned off: " + getName());
		else {
			if (menuTracing) MesquiteMessage.notifyProgrammer("Composing menu of " + module.getName());
			addMyMenuItems(menu);
			ListableVector L =module.getEmployeeVector();
			if (L!=null) {
				int num = L.size();
				for (int i=0; i<num; i++){
					Object obj = L.elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					if ((mb.getUseMenubar() || !inMenuBar(menu)) && !mb.usingGuestMenu &&  mb.window==null && mb.moduleMenuSpec==null && mb.assignedMenuSpec == null) {
						mb.composeMenuDescendants(menu);
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	final void composeMenusOfDescendants(Vector menuBar) {  
		if (doomed) 
			;//MesquiteMessage.println("Error: composing menu of module that has been turned off: " + getName());
		else {
			if (menuTracing) MesquiteMessage.notifyProgrammer("Composing menu of " + module.getName());
			ListableVector L =module.getEmployeeVector();
			if (L!=null) {
				int num = L.size();
				for (int i=0; i<num; i++){
					Object obj = L.elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					if (mb.getUseMenubar() && !mb.usingGuestMenu && mb.window==null ) {
						if (mb.moduleMenuSpec!=null) {
							MesquitePopup menu = MesquitePopup.getPopupMenu(mb.moduleMenuSpec, null);
							mb.composeMenuDescendants(menu);
							addBottom(menu, mb, "@");
							if (menu.getItemCount()>0) {
								menuBar.add(menu);
							}
						}
						if (mb.auxiliaryMenus!=null) {
							num = mb.auxiliaryMenus.size();
							for (int j=0; j<num; j++){
								obj = mb.auxiliaryMenus.elementAt(j);
								MesquiteMenuSpec m = (MesquiteMenuSpec)obj;
								if (m!=null) {
									mb.composeSpecificMenu(menuBar, null, m, true);
								}
							}
						}
						mb.composeMenusOfDescendants(menuBar);
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	final void composeMenusOfDescendants(MesquiteMenuBar menuBar) {    ///TO BE DELETED
		if (doomed) 
			;//MesquiteMessage.println("Error: composing menu of module that has been turned off: " + getName());
		else {
			if (menuTracing) MesquiteMessage.notifyProgrammer("Composing menu of " + module.getName());
			ListableVector L =module.getEmployeeVector();
			if (L!=null) {
				int num = L.size();
				for (int i=0; i<num; i++){
					Object obj = L.elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					if (mb.getUseMenubar() && !mb.usingGuestMenu && mb.window==null ) {
						if (mb.moduleMenuSpec!=null) {
							MesquiteMenu menu = MesquiteMenu.getMenu(mb.moduleMenuSpec);
							mb.composeMenuDescendants(menu);
							addBottom(menu, mb, "@");
							if (menu.getItemCount()>0) {
								menuBar.add(menu);
							}
						}
						if (mb.auxiliaryMenus!=null) {
							num = mb.auxiliaryMenus.size();
							for (int j=0; j<num; j++){
								obj = mb.auxiliaryMenus.elementAt(j);
								MesquiteMenuSpec m = (MesquiteMenuSpec)obj;
								if (m!=null) {
									mb.composeSpecificMenu(menuBar, null, m, true);
								}
							}
						}
						mb.composeMenusOfDescendants(menuBar);
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	int addListableToMenu (int currentCount, Menu menu, MesquiteMenuItemSpec mmi, Object ccc, QualificationsTest qualificationsTest, MesquiteInteger j, int priorityLevel) {
		if (mmi == null)
			return 0;
		Enumeration e = mmi.getListableVector().elements();
		int count = currentCount;
		while (count++<128 && e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof Listened && !((Listened)obj).isUserVisible()){
			}
			else if (!considerPriorities || ((obj instanceof Priority && ((Priority)obj).getPriority()==priorityLevel) || (!(obj instanceof Priority) && priorityLevel==0))) {
				Listable mw = (Listable)obj;
				String name = null;
				if (mw instanceof MesquiteModule)
					name = ((MesquiteModule)mw).getNameForMenuItem();
				else
					name = mw.getName();
				if (mmi.getListableFilter()==null || mmi.getListableFilter().isInstance(mw)) {
					int hiddenStatus = 0;
					Class moduleClass = null;
					if (mw instanceof MesquiteModule)
						moduleClass = mw.getClass();
					else if (mw instanceof MesquiteModuleInfo)
						moduleClass = ((MesquiteModuleInfo)mw).getModuleClass();
					if (InterfaceManager.isFilterable(menu) && ((hiddenStatus = InterfaceManager.isHiddenMenuItem(mmi, name,  j.toString(), mmi.command, moduleClass)) == InterfaceManager.HIDDEN))
						;
					else if (mw instanceof CompatibilityChecker && (ccc !=null || qualificationsTest != null)){
						boolean compatible = true;
						if (qualificationsTest != null)
							compatible = compatible && qualificationsTest.isQualified(mw);

						if (ccc != null) 
							compatible = compatible && ((CompatibilityChecker)mw).isCompatible(ccc, null, null);


						if (compatible) {
							MesquiteMenuItem m =new MesquiteMenuItem(name, null /*mmi.ownerModule*/, mmi.command, j.toString());
							m.setHiddenStatus(hiddenStatus);
							j.add(1);
							m.setDocument(mmi.getDocumentItems());
							m.setReferent(mw);
							MesquiteMenu.add(menu, m);
						}
					}
					else  {
						MesquiteMenuItem m =new MesquiteMenuItem(name, null /*mmi.ownerModule*/, mmi.command, j.toString());
						j.add(1);
						m.setHiddenStatus(hiddenStatus);
						m.setDocument(mmi.getDocumentItems());
						m.setReferent(mw);
						MesquiteMenu.add(menu, m);
					}
				}
			}			
		}
		return count;
	}
	/*.................................................................................................................*/
	final boolean addListsToMenu(MesquiteMenuItemSpec mmi, Menu menu) {
		if (mmi == null)
			return false;
		if (mmi.getDutyClass()!=null) {  //module dutyClass specified; need to create list of modules to choose
			MesquiteModuleInfo mbi=null;
			int count = 0;
			while (count++<128 && (mbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(mmi.getDutyClass(), mbi))!=null) {
				int hiddenStatus = 0;
				if (moduleIsCompatible(mmi, mbi)  && mbi.getUserChooseable() && (!InterfaceManager.isFilterable(menu) || (hiddenStatus = InterfaceManager.isHiddenMenuItem(mmi, mbi.getNameForMenuItem(),  StringUtil.tokenize(mbi.getName()), mmi.command, mbi.getModuleClass(), mmi.getDutyClass())) != InterfaceManager.HIDDEN)) {
					if (mbi.getHireSubchoice()==null) {
						MesquiteMenuItem m = new MesquiteMenuItem(mbi.getNameForMenuItem(), null /*mmi.ownerModule*/, mmi.command, StringUtil.tokenize(mbi.getName()));
						m.setHiddenStatus(hiddenStatus, mmi.getDutyClass());
						m.setDocument(mmi.getDocumentItems());
						m.setReferent(mbi);
						MesquiteMenu.add(menu, m);

					}
					else {
						MesquiteSubmenu submenu=MesquiteSubmenu.getSubmenu(mbi.getNameForMenuItem(), menu, module);  //make submenu
						submenu.setHiddenStatus(hiddenStatus, mmi.getDutyClass());
						submenu.setReferent(mbi);
						MesquiteMenu.add(menu, submenu);

						//populate it with subchoices
						MesquiteModuleInfo smbi=null;
						int count2 = 0;
						int countPrimary2 = 0;
						int countOthers2 = 0;
						if (EmployerEmployee.useOtherChoices)
							while (count2++<128 && (smbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModuleFilteredByNot( mbi.getHireSubchoice(), mbi.getDontHireSubchoice(), smbi))!=null) {
								if (!smbi.getUserChooseable())
									;
								else if (smbi.isPrimary(mbi.getHireSubchoice())) 
									countPrimary2++;
								else
									countOthers2++;
							}
						boolean useOthers2 = EmployerEmployee.useOtherChoices &&  countOthers2>0 && countPrimary2>0;
						Listable[] others2 = null;
						if (useOthers2)
							others2 = new Listable[countOthers2];
						smbi = null;
						countOthers2 =0;
						count2 = 0;
						while (count2++<128 && (smbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModuleFilteredByNot( mbi.getHireSubchoice(), mbi.getDontHireSubchoice(),smbi))!=null) {
							int hiddenStatus2 = 0;
							if (!smbi.getUserChooseable())
								;
							else if (InterfaceManager.isFilterable(submenu) && (hiddenStatus2 = InterfaceManager.isHiddenMenuItem(mmi, smbi.getNameForMenuItem(),  "$ " + StringUtil.tokenize(mbi.getName()) + "  " + StringUtil.tokenize(smbi.getName()), mmi.command, smbi.getModuleClass(), mbi.getHireSubchoice())) == InterfaceManager.HIDDEN)
								;
							else if (useOthers2 && !smbi.isPrimary(mbi.getHireSubchoice()))
								others2[countOthers2++] = smbi;
							else {
								MesquiteMenuItem m = new MesquiteMenuItem(smbi.getNameForMenuItem(), null /*mmi.ownerModule*/, mmi.command, "$ " + StringUtil.tokenize(mbi.getName()) + "  " + StringUtil.tokenize(smbi.getName()));
								m.setDocument(mmi.getDocumentItems());
								m.setHiddenStatus(hiddenStatus2, mbi.getHireSubchoice());
								m.setReferent(smbi);
								submenu.add(m);
							}
						}
						if (useOthers2) {
							//make new mesquite menu item "others"
							submenu.add(new MenuItem("-"));
							MesquiteMenuItem othersItem =new MesquiteMenuItem("Other Choices...", null, mmi.command, "$ " + StringUtil.tokenize(mbi.getName()) + "  ");  
							othersItem.setOthers(others2);
							submenu.add(othersItem);
						}
					}
				}
			}
			return true;
		}
		else if (mmi.getListableVector()!=null) { //
			MesquiteInteger j=new MesquiteInteger(0);
			Object o = mmi.getCompatibilityCheck();
			QualificationsTest qualificationsTest = mmi.getQualificationsTest();
			//int count = 0;
			int count = 0;
			if (considerPriorities)
				for (int i=1; i<MAXPRIORITY; i++)
					count = addListableToMenu(count, menu, mmi, o, qualificationsTest, j, i);
			count = addListableToMenu(count, menu, mmi, o, qualificationsTest, j, 0);
			return true;
		}
		else if (mmi.getStrings()!=null) {  //
			String[] names = mmi.getStrings();
			for (int j=0; j<names.length; j++) {
				if (names[j]!=null) {
					int hiddenStatus = 0;
					if (!InterfaceManager.isFilterable(menu) || (hiddenStatus =InterfaceManager.isHiddenMenuItem(mmi, names[j],  StringUtil.tokenize(names[j]), mmi.command , null)) != InterfaceManager.HIDDEN){
						MesquiteMenuItem m =new MesquiteMenuItem(names[j], null /*mmi.ownerModule*/, mmi.command, StringUtil.tokenize(names[j]) + "  " + j);
						m.setHiddenStatus(hiddenStatus);
						m.setDocument(mmi.getDocumentItems());
						MesquiteMenu.add(menu, m);
					}
				}
			}
			return true;
		}
		return false;
	}
	/*................................................................................................................. */
	final MesquiteSubmenu addSubmenuIfAbsent(Menu containingMenu, MesquiteSubmenuSpec msms) {
		if (msms == null)
			return null;
		if (doomed) {
			;//MesquiteMessage.println("Error: composing menu of module that has been turned off: " + getName());
		}
		else {
			MesquiteSubmenu submenu = findSubmenu(containingMenu, msms);
			if (submenu ==null) {  //submenu not yet created; create it
				submenu=MesquiteSubmenu.getSubmenu(msms, containingMenu, module);
				if (msms.getSelected()!=null) //if selected string is available for check marks
					submenu.setSelected(msms.getSelected());
				MesquiteMenu.add(containingMenu, submenu);
				addMySpecificMenuItems(msms, submenu);   //NEW Nov 07
			}
			return submenu;
		}
		return null;
	}
	/*.................................................................................................................*/
	final MesquiteSubmenu addSubmenuWithListsIfAbsent(Menu containingMenu, MesquiteSubmenuSpec msms) {
		if (msms == null)
			return null;
		//find or make submenu
		boolean needToAddSubmenu = false;
		MesquiteSubmenu submenu = findSubmenu(containingMenu, msms);
		if (submenu ==null) {  //submenu not yet created; create it
			submenu=MesquiteSubmenu.getSubmenu(msms, containingMenu, module);
			if (msms.getSelected()!=null) //if selected string is available for check marks
				submenu.setSelected(msms.getSelected());
			needToAddSubmenu = true;
		}
		fillSubmenu(containingMenu, submenu, msms, needToAddSubmenu);
		if (needToAddSubmenu)
			addMySpecificMenuItems(msms, submenu);   //NEW Nov 07
		return submenu;
	}
	boolean moduleIsCompatible(MesquiteMenuItemSpec mmi, MesquiteModuleInfo mbi){
		if (mmi == null)
			return false;
		Object cc = null;
		cc = mmi.getCompatibilityCheck();
		if (cc == null && mmi.getMenu()!= null)
			cc = mmi.getMenu().getCompatibilityCheck();
		if (cc != null && !mbi.isCompatible(cc, module.getProject(), null))
			return false;
		QualificationsTest qualificationsTest = mmi.getQualificationsTest();
		if (qualificationsTest != null && !qualificationsTest.isQualified(mbi))
			return false;
		return (mmi.getListableFilter()==null || mmi.getListableFilter().isAssignableFrom(mbi.getModuleClass()) || ((CompatibilityChecker)mbi).isCompatible(mmi.getListableFilter(), null, null));
	}
	/*.................................................................................................................*/
	int fillSubmenuWithListable (int currentCount, Menu menu, MesquiteSubmenu submenu, MesquiteSubmenuSpec msms, Object ccc, QualificationsTest qualificationsTest, MesquiteInteger j, int priorityLevel) {
		if (msms == null)
			return 0;
		Enumeration e = msms.getListableVector().elements();
		int count=currentCount;
		while (count++<128 && e.hasMoreElements()) {
			Object obj = e.nextElement();

			if (obj instanceof Listened && !((Listened)obj).isUserVisible()){
			}
			else if (!considerPriorities || ((obj instanceof Priority && ((Priority)obj).getPriority()==priorityLevel) || (!(obj instanceof Priority) && priorityLevel==0))) {
				Listable mw = (Listable)obj;

				String name = null;
				String referentID = null;
				if (mw instanceof MesquiteModule) {
					name = ((MesquiteModule)mw).getNameForMenuItem();
					referentID = Long.toString(((MesquiteModule)mw).getID());
				}
				else {
					name = mw.getName();
					if (mw instanceof Identifiable) {
						referentID = Long.toString(((Identifiable)mw).getID());
					}
				}

				if (msms.getListableFilter()==null || msms.getListableFilter().isInstance(mw) || mw instanceof CompatibilityChecker) {

					boolean OK = !(mw instanceof CompatibilityChecker) || (ccc ==null && qualificationsTest == null);
					if (!OK){  //second chance, might be compatible
						OK = true;
						if (qualificationsTest != null)
							OK = qualificationsTest.isQualified(mw);
						if (ccc != null)
							OK = OK && ((CompatibilityChecker)mw).isCompatible(ccc, null, null);

						OK = OK &&  ((CompatibilityChecker)mw).isCompatible(msms.getListableFilter(), null, null);
					}
					if (OK){
						int hiddenStatus = 0;
						Class moduleClass = null;
						if (mw instanceof MesquiteModule)
							moduleClass = mw.getClass();
						else if (mw instanceof MesquiteModuleInfo)
							moduleClass = ((MesquiteModuleInfo)mw).getModuleClass();
						if (msms.isFilterable() && (hiddenStatus = InterfaceManager.isHiddenMenuItem(msms, name,   j.toString(), msms.command , moduleClass)) == InterfaceManager.HIDDEN)
							;
						else if (submenu.getSelected()!=null){ //selected string available for checkmark 
							MesquiteCheckMenuItem m =new MesquiteCheckMenuItem(name, null /*msms.ownerModule*/, msms.command, j.toString() + " " + ParseUtil.tokenize(name), submenu.getSelected());
							m.setReferentID(referentID);
							j.add(1);
							//m.setDocument(msms.getDocumentItems());
							m.setHiddenStatus(hiddenStatus);
							m.setReferent(mw);
							submenu.add(m);
						}
						else {
							MesquiteMenuItem m =new MesquiteMenuItem(name, null /*msms.ownerModule*/, msms.command, j.toString());
							j.add(1);
							m.setHiddenStatus(hiddenStatus);
							m.setDocument(msms.getDocumentItems());
							m.setReferent(mw);
							submenu.add(m);
						}
					}
				}

			}
		}
		return count;
	}
	MenuItem[] primaryItems = new MenuItem[500];
	MenuItem[] secondaryItems = new MenuItem[500];
	MenuItem[] primaryItems2 = new MenuItem[500];
	MenuItem[] secondaryItems2 = new MenuItem[500];
	private void zeroArray(MenuItem[] m){
		for (int i= 0; i<m.length; i++)
			m[i] = null;
	}

	/*.................................................................................................................*/
	final boolean fillSubmenu(Menu menu, MesquiteSubmenu submenu, MesquiteSubmenuSpec msms, boolean needToAddSubmenu) {
		if (msms == null)
			return false;
		if (msms.getDutyClass()!=null) {        //module dutyClass specified; need to create list of modules to choose
			MesquiteModuleInfo mbi=null;
			int countPrimary = 0;
			int countOthers = 0;
			int count = 0;
			zeroArray(primaryItems);
			zeroArray(secondaryItems);
			int countItems = 0;
			//First, find if there are both primary and secondary choices.  If so, use "Other Choice..." system.
			//	if (EmployerEmployee.useOtherChoices)
			while (count++<128 && (mbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule( msms.getDutyClass(), mbi))!=null) {
				if (moduleIsCompatible(msms, mbi)) {
					if (!mbi.getUserChooseable())
						;
					else if (mbi.isPrimary(msms.getDutyClass())) 
						countPrimary++;
					else
						countOthers++;
				}
			}
			boolean useOthers = EmployerEmployee.useOtherChoices && countOthers>0 && countPrimary>0;
			Listable[] others = null;
			if (useOthers)
				others = new Listable[countOthers];
			mbi = null;
			countOthers = 0;
			count = 0;
			while (count++<128 && (mbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule( msms.getDutyClass(), mbi))!=null) {
				boolean primary = mbi.isPrimary(msms.getDutyClass());
				if (moduleIsCompatible(msms, mbi)) {
					int hiddenStatus = 0;
					if (!mbi.getUserChooseable())
						;
					else if (useOthers && !primary)
						others[countOthers++] = mbi;
					else 	if (msms.isFilterable() && (hiddenStatus =InterfaceManager.isHiddenMenuItem(msms, mbi.getNameForMenuItem(),  StringUtil.tokenize(mbi.getName()), msms.command, mbi.getModuleClass(), msms.getDutyClass() )) == InterfaceManager.HIDDEN){
					}
					else if (mbi.getHireSubchoice()==null) { // potential employee hasn't indicated it would like subemployee submenu
						if (submenu.getSelected()!=null){ //selected string available for checkmark 
							MesquiteCheckMenuItem m =new MesquiteCheckMenuItem(mbi.getNameForMenuItem(), null, msms.command, StringUtil.tokenize(mbi.getName()), submenu.getSelected());  
							//m.setDocument(msms.getDocumentItems());
							m.setReferent(mbi);
							m.setHiddenStatus(hiddenStatus, msms.getDutyClass());
							if (primary)
								primaryItems[countItems++] = m;
							else
								secondaryItems[countOthers++] = m;
							//	submenu.add(m);
						}
						else {
							MesquiteMenuItem m =new MesquiteMenuItem(mbi.getNameForMenuItem(), null, msms.command, StringUtil.tokenize(mbi.getName()));  
							m.setDocument(msms.getDocumentItems());
							m.setReferent(mbi);
							m.setHiddenStatus(hiddenStatus, msms.getDutyClass());
							if (primary)
								primaryItems[countItems++] = m;
							else
								secondaryItems[countOthers++] = m;

							//submenu.add(m);
						}
					}
					else { // potential employee has indicated it would like subemployee submenu
						MesquiteSubmenu submenu2=MesquiteSubmenu.getSubmenu(mbi.getNameForMenuItem(), submenu, module);  //make submenu
						submenu2.setHiddenStatus(hiddenStatus, msms.getDutyClass());
						if (primary)
							primaryItems[countItems++] = submenu2;
						else
							secondaryItems[countOthers++] = submenu2;
						//submenu.add(submenu2);
						submenu2.setReferent(mbi);
						//populate it with subchoices
						MesquiteModuleInfo smbi=null;
						int count2 = 0;
						int countPrimary2 = 0;
						int countOthers2 = 0;
						zeroArray(primaryItems2);
						zeroArray(secondaryItems2);
						int countItems2 = 0;
						//		if (EmployerEmployee.useOtherChoices)
						while (count2++<128 && (smbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModuleFilteredByNot( mbi.getHireSubchoice(), mbi.getDontHireSubchoice(), smbi))!=null) {
							if (!smbi.getUserChooseable())
								;
							else if (smbi.isPrimary(mbi.getHireSubchoice())) 
								countPrimary2++;
							else
								countOthers2++;
						}
						boolean useOthers2 = EmployerEmployee.useOtherChoices && countOthers2>0 && countPrimary2>0;
						Listable[] others2 = null;
						if (useOthers2)
							others2 = new Listable[countOthers2];
						smbi = null;
						countOthers2 =0;
						count2 = 0;
						while (count2++<128 && (smbi = MesquiteTrunk.mesquiteModulesInfoVector.findNextModuleFilteredByNot( mbi.getHireSubchoice(), mbi.getDontHireSubchoice(), smbi))!=null) {
							boolean primary2 = smbi.isPrimary(mbi.getHireSubchoice());
							int hiddenStatus2 = 0;
							//TODO: this tokenization of the names for argument will not work if name of module includes '  -- must use full tokenization/detokenization
							if (!smbi.getUserChooseable() || (msms.isFilterable() && (hiddenStatus2 = InterfaceManager.isHiddenMenuItem(msms, smbi.getNameForMenuItem(),  "$ " + StringUtil.tokenize(mbi.getName()) + "  " + StringUtil.tokenize(smbi.getName()), msms.command, smbi.getModuleClass(), mbi.getHireSubchoice() )) == InterfaceManager.HIDDEN)) 
								;
							else if (useOthers2 && !primary2)
								others2[countOthers2++] = smbi;
							else if (submenu.getSelected()!=null){ //selected string available for checkmark 
								MesquiteCheckMenuItem m =new MesquiteCheckMenuItem(smbi.getNameForMenuItem(), null, msms.command, "$ " + StringUtil.tokenize(mbi.getName()) + "  " + StringUtil.tokenize(smbi.getName()), submenu.getSelected());  
								//m.setDocument(msms.getDocumentItems());
								m.setReferent(smbi);
								m.setHiddenStatus(hiddenStatus2, mbi.getHireSubchoice());
								//submenu2.add(m);
								if (primary2)
									primaryItems2[countItems2++] = m;
								else
									secondaryItems2[countOthers2++] = m;
							}
							else {
								MesquiteMenuItem m =new MesquiteMenuItem(smbi.getNameForMenuItem(), null, msms.command, "$ " + StringUtil.tokenize(mbi.getName()) + "  " + StringUtil.tokenize(smbi.getName()));
								m.setDocument(msms.getDocumentItems());
								m.setHiddenStatus(hiddenStatus2, mbi.getHireSubchoice());
								m.setReferent(smbi);
								//submenu2.add(m);
								if (primary2)
									primaryItems2[countItems2++] = m;
								else
									secondaryItems2[countOthers2++] = m;
							}
						}
						for (int i=0; i<primaryItems2.length && primaryItems2[i] != null; i++)
							submenu2.add(primaryItems2[i]);
						if (!useOthers2 && countOthers2>0 && countItems2>0){
							submenu2.add(new MenuItem("-"));
							submenu2.add(new MenuItem("-"));
						}
						for (int i=0; i<secondaryItems2.length && secondaryItems2[i] != null; i++)
							submenu2.add(secondaryItems2[i]);
						if (useOthers2) {
							//make new mesquite menu item "others"
							submenu2.add(new MenuItem("-"));
							MesquiteMenuItem othersItem2 =new MesquiteMenuItem("Other Choices...", null, msms.command, "$ " + StringUtil.tokenize(mbi.getName()) + "  ");  
							othersItem2.setOthers(others2);
							submenu2.add(othersItem2);
						}
					}
				}

			}
			for (int i=0; i<primaryItems.length && primaryItems[i] != null; i++)
				submenu.add(primaryItems[i]);
			if (!useOthers && countOthers>0 && countItems>0){
				submenu.add(new MenuItem("-"));
				submenu.add(new MenuItem("-"));
			}
			for (int i=0; i<secondaryItems.length && secondaryItems[i] != null; i++)
				submenu.add(secondaryItems[i]);
			if (useOthers) {
				//make new mesquite menu item "others"
				submenu.add(new MenuItem("-"));
				MesquiteMenuItem othersItem =new MesquiteMenuItem("Other Choices...", null /*msms.ownerModule*/, msms.command, null);  
				othersItem.setOthers(others);
				submenu.add(othersItem);
			}
			if (submenu.getItemCount()<=1 && msms.getBehaviorIfNoChoice() != MesquiteSubmenuSpec.SHOW_SUBMENU){
				int b = msms.getBehaviorIfNoChoice();
				int hiddenStatus = 0;
				if (msms.isFilterable() && (hiddenStatus = InterfaceManager.isHiddenMenuItem(msms, msms.getSubmenuName(),  null, msms.command, null)) == InterfaceManager.HIDDEN) 
					;
				else if (b == MesquiteSubmenuSpec.ONEDISABLE_ZERODISABLE || (submenu.getItemCount()==0 && (b == MesquiteSubmenuSpec.ONESUBMENU_ZERODISABLE || b == MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE))){
					//add a menu item here instead of submenu & disable it
					MesquiteMenuItem m = new MesquiteMenuItem(msms);
					m.setHiddenStatus(hiddenStatus);
					MesquiteMenu.add(menu, m);
					m.setEnabled(false);
					needToAddSubmenu = false;
				}
				else  if (submenu.getItemCount()==1 && b == MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE){
					//add a menu item here instead of submenu
					MesquiteMenuItem m = new MesquiteMenuItem(msms);
					m.setHiddenStatus(hiddenStatus);
					MesquiteMenu.add(menu, m);
					needToAddSubmenu = false;
				}
			}
			if (needToAddSubmenu)
				MesquiteMenu.add(menu, submenu);
			return true;
		}
		else if (msms.getListableVector()!=null) {  
			MesquiteInteger j = new MesquiteInteger(0); 

			Object o = msms.getCompatibilityCheck();
			QualificationsTest qualificationsTest = msms.getQualificationsTest();

			int count = 0;
			if (considerPriorities)
				for (int i=1; i<MAXPRIORITY; i++)
					count = fillSubmenuWithListable(count, menu, submenu, msms, o, qualificationsTest, j, i);
			count = fillSubmenuWithListable(count, menu, submenu, msms, o, qualificationsTest, j, 0);

			if (submenu.getItemCount()<=1 && msms.getBehaviorIfNoChoice() != MesquiteSubmenuSpec.SHOW_SUBMENU){
				int b = msms.getBehaviorIfNoChoice();
				int hiddenStatus = 0;
				if (msms.isFilterable() && (hiddenStatus = InterfaceManager.isHiddenMenuItem(msms, msms.getSubmenuName(),  null, msms.command, null)) == InterfaceManager.HIDDEN) 
					;
				else if (b == MesquiteSubmenuSpec.ONEDISABLE_ZERODISABLE || (submenu.getItemCount()==0 && (b == MesquiteSubmenuSpec.ONESUBMENU_ZERODISABLE || b == MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE))){
					//add a menu item here instead of submenu & disable it
					MesquiteMenuItem m = new MesquiteMenuItem(msms);
					MesquiteMenu.add(menu, m);
					m.setHiddenStatus(hiddenStatus);
					m.setEnabled(false);
					needToAddSubmenu = false;
				}
				else  if (submenu.getItemCount()==1 && b == MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE){
					//add a menu item here instead of submenu
					MesquiteMenuItem m = new MesquiteMenuItem(msms);
					m.setHiddenStatus(hiddenStatus);
					MesquiteMenu.add(menu, m);
					needToAddSubmenu = false;
				}
			}
			if (needToAddSubmenu)
				MesquiteMenu.add(menu, submenu);
			return true;
		}
		else if (msms.getStrings()!=null) {  //
			String[] names = msms.getStrings();
			for (int j=0; j<names.length; j++) {
				if (names[j]!=null) {
					int hiddenStatus = 0;
					if (msms.isFilterable() && (hiddenStatus = InterfaceManager.isHiddenMenuItem(msms, names[j],  StringUtil.tokenize(names[j]), msms.command, null)) == InterfaceManager.HIDDEN) 
						;
					else if (submenu.getSelected()!=null){
						MesquiteCheckMenuItem m =new MesquiteCheckMenuItem(names[j], null /*msms.ownerModule*/, msms.command, StringUtil.tokenize(names[j]), submenu.getSelected());
						//m.setDocument(msms.getDocumentItems());
						m.setHiddenStatus(hiddenStatus);

						submenu.add(m); 
					}
					else {
						MesquiteMenuItem m =new MesquiteMenuItem(names[j], null /*msms.ownerModule*/, msms.command, StringUtil.tokenize(names[j]) + " " + j);
						m.setDocument(msms.getDocumentItems());
						m.setHiddenStatus(hiddenStatus);
						submenu.add(m); 
					}
				}
			}
			if (submenu.getItemCount()<=1 && msms.getBehaviorIfNoChoice() != MesquiteSubmenuSpec.SHOW_SUBMENU){
				int b = msms.getBehaviorIfNoChoice();
				int hiddenStatus = 0;
				if (msms.isFilterable() && (hiddenStatus = InterfaceManager.isHiddenMenuItem(msms, msms.getSubmenuName(),  null, msms.command, null)) == InterfaceManager.HIDDEN) 
					;
				else if (b == MesquiteSubmenuSpec.ONEDISABLE_ZERODISABLE || (submenu.getItemCount()==0 && (b == MesquiteSubmenuSpec.ONESUBMENU_ZERODISABLE || b == MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE))){
					//add a menu item here instead of submenu & disable it
					MesquiteMenuItem m = new MesquiteMenuItem(msms);
					m.setHiddenStatus(hiddenStatus);
					MesquiteMenu.add(menu, m);
					m.setEnabled(false);
					needToAddSubmenu = false;
				}
				else  if (submenu.getItemCount()==1 && b == MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE){
					//add a menu item here instead of submenu
					MesquiteMenuItem m = new MesquiteMenuItem(msms);
					m.setHiddenStatus(hiddenStatus);
					MesquiteMenu.add(menu, m);
					needToAddSubmenu = false;
				}
			}
			if (needToAddSubmenu)
				MesquiteMenu.add(menu, submenu);
			return true;
		}
		//---------------------------------------
		return false;
	}
	boolean descendantsWithMenus(MesquiteModule mb){
		boolean withMenus = false;
		ListableVector L =mb.getEmployeeVector();
		if (L!=null) {
			int num = L.size();
			for (int i=0; i<num; i++){
				Object obj = L.elementAt(i);
				MesquiteModule mbe = (MesquiteModule)obj;
				if (mbe.menuItemsSpecs!=null && mbe.menuItemsSpecs.size()>0)
					return true;
				if (descendantsWithMenus(mbe))
					return true;
			}
		}
		return false;
	}
	/*.................................................................................................................
	This adds the module's menu items to a menu.  The menu is generally either the module's own menu or, if
	the module has none, in the nearest menu in its employer chain.*/
	final void addMyMenuItems(Menu menu){
		try {
			if (menuTracing) MesquiteMessage.notifyProgrammer("         adding menus of " + toString());
			if (menuItemsSpecs!=null) {
				for (int i=0; i<menuItemsSpecs.size(); i++) {
					MesquiteMenuItemSpec mmi = (MesquiteMenuItemSpec)menuItemsSpecs.elementAt(i);
					boolean useThisMenu = false;
					if (mmi != null){
						if (mmi.whichMenu==null)
							useThisMenu = true;
						else if (menu instanceof MesquiteMenu){
							useThisMenu = mmi.whichMenu == ((MesquiteMenu)menu).getSpecification();
						}
						else if (menu instanceof MesquitePopup){
							useThisMenu = mmi.whichMenu == ((MesquitePopup)menu).getSpecification();
						}
						useThisMenu = useThisMenu && assignedMenuSpec==null;
					}
					if (useThisMenu) {
						int hiddenStatus = 0;
						String arg = null;
						if (mmi.getArgument() != null)
							arg = mmi.getArgument();
						else if (mmi.command != null)
							arg = mmi.command.getDefaultArguments();
						if (InterfaceManager.isFilterable(menu) && (hiddenStatus = InterfaceManager.isHiddenMenuItem(mmi, mmi.getName(),  arg, mmi.command, null)) == InterfaceManager.HIDDEN) 
							;
						else if (mmi instanceof MesquiteSubmenuSpec) {  //add submenu

							MesquiteSubmenu ms = addSubmenuWithListsIfAbsent(menu,  ((MesquiteSubmenuSpec)mmi));
							ms.setHiddenStatus(hiddenStatus);

							/*if (submenu!=null) {
							addListsToMenu(mmi, submenu);
							//MesquiteMenu.add(menu, submenu);
						}
							 */
						}
						else if (mmi.submenuIn() != null) {
							MesquiteSubmenu submenu= addSubmenuIfAbsent(menu,  mmi.submenuIn());
							if (submenu !=null) {  //CHECK
								if (mmi instanceof MesquiteCMenuItemSpec) {
									MesquiteCheckMenuItem cmi = null;
									submenu.add(cmi = new MesquiteCheckMenuItem((MesquiteCMenuItemSpec)mmi));
									cmi.setHiddenStatus(hiddenStatus);
								}
								else if (submenu.getSelected()!=null)  {//selected string available for checkmark
									MesquiteCheckMenuItem cmi = null;
									submenu.add(cmi = new MesquiteCheckMenuItem(mmi.getName(), null /*mmi.ownerModule*/, mmi.command, StringUtil.tokenize(mmi.getName()), submenu.getSelected()));  //todo: name should be tokenized???
									cmi.setHiddenStatus(hiddenStatus);
								}
								else {
									MesquiteMenuItem mi = null;
									submenu.add(mi = new MesquiteMenuItem(mmi));
									mi.setHiddenStatus(hiddenStatus);
									//MesquiteMenu.add(menu, submenu);
								}
							}
						}
						else if (!addListsToMenu(mmi, menu)){ //CHECK
							if (mmi instanceof MesquiteCMenuItemSpec){
								MesquiteCheckMenuItem cmi = null;
								MesquiteMenu.add(menu, cmi = new MesquiteCheckMenuItem((MesquiteCMenuItemSpec)mmi));
								cmi.setHiddenStatus(hiddenStatus);
							}
							else {
								MesquiteMenuItem mi = null;
								MesquiteMenu.add(menu, mi = new MesquiteMenuItem(mmi));
								mi.setHiddenStatus(hiddenStatus);
							}
						}
						if (menuTracing && mmi!=null) MesquiteMessage.notifyProgrammer("MENU Item " + mmi.itemName + " added");
					}
				}
			}
			/*boolean macroMenuMade = false;
		if (module.getAutoSaveMacros()) {
			MesquiteMenu.add(menu, new MenuItem("-"));
			MesquiteMenu.add(menu, new MesquiteMenuItem("Save Macro for " +  module.getNameForMenuItem() + "...", module, module.makeCommand("saveMacro", module)));  //commandArgument
			macroMenuMade = true;
		}
		if (!(module instanceof FileCoordinator)) {
			MesquiteSubmenu ms = makeMacrosSubmenu(menu, module, 0, "Macros for " + module.getNameForMenuItem());
			if (ms!=null) {
				if (!macroMenuMade)
					MesquiteMenu.add(menu, new MenuItem("-"));
				menu.add(ms);
				macroMenuMade = true;
			}
		}
		if (descendantsWithMenus(module) && macroMenuMade)
			MesquiteMenu.add(menu, new MenuItem("-"));
			 */
		}
		catch (Exception e){
		}
	}
	private final MesquiteSubmenu makeMacrosSubmenu(Menu menu, MesquiteModule mb, int menuNum, String label){
		if (mb == null)
			return null;
		MesquiteModuleInfo mmi = mb.getModuleInfo();
		if (mmi!=null){
			Vector macros = mb.getModuleInfo().getMacros();
			if (macros!=null && macros.size()>0){
				MesquiteSubmenu submenu=MesquiteSubmenu.getSubmenu(label, menu, mb);
				for (int i=0; i<macros.size(); i++) {
					MesquiteMacro macro = (MesquiteMacro)macros.elementAt(i);
					if (macro!=null && macro.getPreferredMenu() == menuNum) 
						submenu.add(new MesquiteMenuItem(macro.getName(), mb, mb.makeCommand("applyMacro", mb), Integer.toString(i)));  //commandArgument
				}
				return submenu;
			}
		}
		return null;
	}
	/*................................................................................................................. */
	final void addFilesToSubmenu(MesquiteSubmenu submenu, MesquiteProject proj, boolean onlyIfLocal, MesquiteCommand command){
		Enumeration enumeration=proj.getFiles().elements();
		MesquiteFile fi;
		int count = 0;
		while (count++<128 && enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof MesquiteFile) {
				fi = (MesquiteFile)obj;
				if (!onlyIfLocal || fi.isLocal())
					submenu.add(new MesquiteMenuItem(fi.getName(), MesquiteModule.mesquiteTrunk, command, Long.toString(fi.getID())));
			}
		}
	}
	/*................................................................................................................. */
	private final MesquiteMenu composeFileMenu(MesquiteMenuBar menuBar, MesquiteMenuSpec menuSpec, MesquiteWindow whichWindow) {
		if (menuSpec!=null) {
			MesquiteMenu newMenu= MesquiteMenu.getMenu(menuSpec);
			String projName;
			MesquiteProject proj = module.getProject();

			//if module has no project, i.e. is trunkward of a file coordinator module, then if there is just one project, treat the file menu as if it were for that project
			if (proj == null) {
				if (MesquiteTrunk.getProjectList().getNumProjects()==1)
					proj = MesquiteTrunk.getProjectList().getProject(0);
			}
			if (proj==null)
				projName = "";
			else if (StringUtil.titled(proj.getName()))
				projName = proj.getName();
			else
				projName = "current project...";

			MesquiteMenuItem newItem =new MesquiteMenuItem("New", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.newFileCommand);
			//TODO: should allow New Linked!!!!
			newItem.setShortcut(newShortcut);
			newMenu.add(newItem);
			if (proj!=null){
				MesquiteMenuItem newLinkedItem =new MesquiteMenuItem("New Linked File", MesquiteModule.mesquiteTrunk, proj.getNewLinkFileCommand());
				newMenu.add(newLinkedItem);
			}
			int numLinkedFiles = 0;
			if (proj!=null)
				numLinkedFiles = proj.getNumberLinkedFiles();
			/*-------------- open file menu or submenu 
			MesquiteSubmenu openFileSubmenu=MesquiteSubmenu.getSubmenu("Open", newMenu, module);  //make submenu
			newMenu.add(openFileSubmenu);
			if (proj!=null && numLinkedFiles!=0) {
				openFileSubmenu.add(new MesquiteMenuItem("(as separate project)", null, null));
				openFileSubmenu.add(new MenuItem("-"));
			}
			 */
			if (!MesquiteTrunk.isApplet()) {
				MesquiteMenuItem openItem = new MesquiteMenuItem("Open File...", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.openFileCommand);
				openItem.setShortcut(openShortcut);
				newMenu.add(openItem);
		//		MesquiteMenuItem importItem = new MesquiteMenuItem("Open Special NEXUS File...", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.openSpecialNEXUSCommand);
		//		newMenu.add(importItem);
			}
			/*-------------- open URL menu or submenu */
			//newMenu.add(new MesquiteMenuItem("Open URL...", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.openURLCommand));


			addSubmenuWithListsIfAbsent(newMenu,  MesquiteTrunk.mesquiteTrunk.openExternalSMS);


			if (proj!=null && numLinkedFiles>0) {
				if (!MesquiteTrunk.isApplet()) {
					newMenu.add(new MesquiteMenuItem("Link File...", MesquiteModule.mesquiteTrunk, proj.getLinkFileCommand()));
					newMenu.add(new MesquiteMenuItem("Include File...", MesquiteModule.mesquiteTrunk, proj.getIncludeFileCommand()));
				}


			}

			/*-------------- Close submenu TODO: should make this a submenu Close with Project, then list files one by one as in save*/
			if (proj!=null && !MesquiteTrunk.isApplet()) {
				if (numLinkedFiles>1) {
					//newMenu.add(new MesquiteMenuItem("Close Linked Files", MesquiteModule.mesquiteTrunk, proj.getCloseCommand()));
					MesquiteSubmenu closeSubmenu=MesquiteSubmenu.getSubmenu("Close", newMenu, module);  //make submenu
					newMenu.add(closeSubmenu);
					closeSubmenu.add(new MesquiteMenuItem(projName, MesquiteModule.mesquiteTrunk, proj.getCloseFilesCommand()));
					closeSubmenu.add("-");
					addFilesToSubmenu(closeSubmenu, proj, false,  proj.getCloseCommand());
				}
				else {
					newMenu.add(new MesquiteMenuItem("Close File", MesquiteModule.mesquiteTrunk, proj.getCloseFilesCommand()));
				}
			}
			if (MesquiteTrunk.getProjectList() !=null && MesquiteTrunk.getProjectList().getNumProjects()>=1)
				newMenu.add(new MesquiteMenuItem("Close All", MesquiteModule.mesquiteTrunk, MesquiteTrunk.mesquiteTrunk.closeAllCommand));
			if (proj!=null && !MesquiteTrunk.isApplet() && (MesquiteTrunk.isMacOSX() || MesquiteTrunk.isWindows())) {
				if (numLinkedFiles>1) {
					//newMenu.add(new MesquiteMenuItem("Close Linked Files", MesquiteModule.mesquiteTrunk, proj.getCloseCommand()));
					MesquiteSubmenu showFileSubmenu=MesquiteSubmenu.getSubmenu("Show File Location", newMenu, module);  //make submenu
					newMenu.add(showFileSubmenu);
					addFilesToSubmenu(showFileSubmenu, proj, false,  proj.getShowFileOnDiskCommand());
				}
				else {
					newMenu.add(new MesquiteMenuItem("Show File Location", MesquiteModule.mesquiteTrunk, proj.getShowFileOnDiskCommand()));
				}
			}
			whichWindow.closeWindowMenuItem = new MesquiteMenuItem(whichWindow.closeWindowMenuItemSpec);
			whichWindow.closeWindowMenuItem.setShortcut(MesquiteWindow.closeWindowShortcut);	
			newMenu.add(whichWindow.closeWindowMenuItem);
			whichWindow.closeAllMenuItem = new MesquiteMenuItem(whichWindow.closeAllMenuItemSpec);
			if (whichWindow.closeAllMenuItem != null)
				newMenu.add(whichWindow.closeAllMenuItem);
			/*-------------- Save & Save As menu or submenu */
			newMenu.add("-");		
			if (proj!=null && !MesquiteTrunk.isApplet()) {
				if (numLinkedFiles>1) {
					MesquiteSubmenu saveSubmenu=MesquiteSubmenu.getSubmenu("Save", newMenu, module);  //make submenu
					newMenu.add(saveSubmenu);
					MesquiteMenuItem saveAllItem = new MesquiteMenuItem("All Files in " + projName, MesquiteModule.mesquiteTrunk, proj.getSaveFilesCommand());
					saveSubmenu.add(saveAllItem);
					saveAllItem.setShortcut(saveShortcut);	
					saveSubmenu.add("-");
					addFilesToSubmenu(saveSubmenu, proj, true,  proj.getSaveCommand());
					MesquiteSubmenu saveAsSubmenu=MesquiteSubmenu.getSubmenu("Save As", newMenu, module);  //make submenu
					newMenu.add(saveAsSubmenu);
					addFilesToSubmenu(saveAsSubmenu, proj, true,  proj.getSaveAsCommand());
				}
				else	{
					MesquiteMenuItem saveItem = new MesquiteMenuItem("Save File", MesquiteModule.mesquiteTrunk, proj.getSaveCommand());	
					saveItem.setShortcut(saveShortcut);	
					newMenu.add(saveItem);		
					//newMenu.add(new MesquiteSubMenuItem("Save File", MesquiteModule.mesquiteTrunk, proj.getSaveCommand()));		
					newMenu.add(new MesquiteMenuItem("Save File As...", MesquiteModule.mesquiteTrunk, proj.getSaveAsCommand()));
				}
				if (MesquiteTrunk.getProjectList().getNumProjects()>1){
					MesquiteMenuItem saveAllItem = new MesquiteMenuItem("Save All Files All Projects", MesquiteModule.mesquiteTrunk, MesquiteTrunk.mesquiteTrunk.saveAllCommand);	
					newMenu.add(saveAllItem);
				}	
				newMenu.add(new MesquiteMenuItem("Export...", MesquiteModule.mesquiteTrunk, proj.exportCommand));
				newMenu.add(new MesquiteMenuItem("Revert to Saved", MesquiteModule.mesquiteTrunk, proj.getRevertCommand()));
				MesquiteMenuItem gi;
				newMenu.add(gi = new MesquiteMenuItem("Get Info...", MesquiteModule.mesquiteTrunk, proj.getInfoCommand));
				gi.setShortcut(getInfoShortcut);
			}
			else if (MesquiteTrunk.isApplet() || MesquiteTrunk.getProjectList().getNumProjects()==0) {
				//can't save either because this is menu for window trunkward of projects, and there is no project open, or is applet.  Give user warning
				MesquiteMenuItem saveItem = new MesquiteMenuItem("Save File...", MesquiteModule.mesquiteTrunk, MesquiteModule.makeCommand("explainNoSave", MesquiteModule.mesquiteTrunk));	
				saveItem.setShortcut(saveShortcut);	
				newMenu.add(saveItem);		
			}
			else {
				MesquiteMenuItem saveAllItem = new MesquiteMenuItem("Save All Files All Projects", MesquiteModule.mesquiteTrunk, MesquiteTrunk.mesquiteTrunk.saveAllCommand);	
				saveAllItem.setShortcut(saveShortcut);	
				newMenu.add(saveAllItem);		
			}

			if (whichWindow!=null)
				newMenu.add(new MesquiteMenuItem("Save Window as Text...", module, whichWindow.saveAsTextCommand));
			newMenu.add("-");
			/*-------------- Print item */

			MesquiteMenuItem printItem = new MesquiteMenuItem(menuBar.getOwnerWindow().getPrintMenuItem(), MesquiteModule.mesquiteTrunk, menuBar.getOwnerWindow().printCommand);
			newMenu.add(printItem);		
			if (menuBar.getOwnerWindow() instanceof Fittable) {
				MesquiteMenuItem printToFitItem = new MesquiteMenuItem(menuBar.getOwnerWindow().getPrintToFitMenuItem());
				newMenu.add(printToFitItem);		
				printToFitItem.setShortcut(printShortcut);
			}
			else
				printItem.setShortcut(printShortcut);
			MesquiteMenuItem printPDFItem = new MesquiteMenuItem(menuBar.getOwnerWindow().getPrintToPDFMenuItemName(), MesquiteModule.mesquiteTrunk, menuBar.getOwnerWindow().printToPDFCommand);
			newMenu.add(printPDFItem);		
			newMenu.add("-");		
			/*-------------- Macros items *
			MesquiteSubmenu mac = composeMacrosSubmenu(MesquiteTrunk.class, FileCoordinator.class, newMenu, whichWindow);
			if (mac!=null && mac.getItemCount()>0) {
				newMenu.add(mac);
				newMenu.add("-");
			}
			/*-------------- items from employers & employees */
			composeSpecificMenuAncestors(menuSpec, newMenu);
			addMySpecificMenuItems(menuSpec, newMenu);
			ListableVector L =module.getEmployeeVector();
			if (L != null) {
				int num = L.size();
				for (int i=0; i<num; i++){
					Object obj = L.elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					if ( mb.window==null) 
						mb.composeSpecificMenuDescendants(menuSpec, newMenu);
				}
			}



			//defaultSubMenu=MesquiteSubmenu.getSubmenu("Defaults", newMenu, module);  //make submenu
			//newMenu.add(defaultSubMenu);
			//defaultsMenuSpec = defaultSubMenu.getSpec();


			addBottom(newMenu, null, "*");

			//newMenu.add(new MesquiteMenuItem("Edit Menu Visibility", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.editMenuVisibilityCommand));


			newMenu.add(new MesquiteMenuItem("Reset Menus", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.resetMenusCommand));
			//MesquiteMenuItem ccItem =new MesquiteMenuItem("Current Command...", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.currentCommandCommand);
			//ccItem.setShortcut(ccShortcut);
			//newMenu.add(ccItem);		
			//newMenu.add(new MesquiteMenuItem("Pending Commands...", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.pendingCommandsCommand));
			newMenu.add(new MesquiteMenuItem("Force Quit", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.forceQuitCommand));
			MesquiteMenuItem quitItem = new MesquiteMenuItem("Quit Mesquite", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.quitCommand);
			//if (!MesquiteTrunk.isMacOSX()) bug causes quit to ignore "cancel"
			quitItem.setShortcut(quitShortcut);
			newMenu.add(quitItem);
			//	composeSpecificMenuDescendants(menu, newMenu);
			if (newMenu.getItemCount()>0)
				menuBar.add(newMenu);
			//dumpMenu(newMenu, " ");
			return newMenu;
		}
		return null;
	}

	/*................................................................................................................. */
	public void setUndoMenuItemEnabled(boolean b) {
		if (undoMenuItem!=null) {
			undoEnabled=b;
			undoMenuItem.setEnabled(b);
			if (b)
				undoMenuItem.setLabel("Undo");
			else {
				undoMenuItem.setLabel("Can't Undo");
			}
		}

	}
	/*................................................................................................................. */
	private final MesquiteMenu composeEditMenu(MesquiteMenuBar menuBar, MesquiteMenuSpec menuSpec, MesquiteWindow whichWindow) {
		if (menuSpec!=null) {
			MesquiteMenu newMenu= MesquiteMenu.getMenu(menuSpec);


			MesquiteMenuItem cut, copy, paste, clear, selectAll;
			undoMenuItem =new MesquiteMenuItem("Undo", MesquiteModule.mesquiteTrunk, whichWindow.getUndoCommand());
			newMenu.add(undoMenuItem);		
			if (whichWindow.getUndoCommand()!=null)
				undoMenuItem.setShortcut(undoShortcut);
			previousToolMenuItem =new MesquiteMenuItem("Previous Tool", MesquiteModule.mesquiteTrunk, whichWindow.getPreviousToolCommand());
			newMenu.add(previousToolMenuItem);		
			if (whichWindow.getPreviousToolCommand()!=null)
				previousToolMenuItem.setShortcut(previousToolShortcut);
			setUndoMenuItemEnabled(undoEnabled);
			newMenu.add("-");		
			cut =new MesquiteMenuItem("Cut", MesquiteModule.mesquiteTrunk,MesquiteModule.makeCommand("cut", whichWindow));// whichWindow.getCutCommand());
			newMenu.add(cut);
			if(whichWindow.getCutCommand()!=null)
				cut.setShortcut(cutShortcut);	
			cut.setEnabled(whichWindow.getCutCommand()!=null); //window must return a copy command if command-based copying is to be enabled; otherwise allows automatic system

			MesquiteCommand copyCommand = MesquiteModule.makeCommand("copy", whichWindow);
			copyCommand.setSuppressLogging(true); //set true so that writing "copy" doesn't prevent copying from selection in log itself
			copy =new MesquiteMenuItem("Copy", MesquiteModule.mesquiteTrunk, copyCommand);
			newMenu.add(copy);		
			copy.setShortcut(copyShortcut);

			if (whichWindow.getCopySpecialCommand()!=null){
				newMenu.add(new MesquiteMenuItem(whichWindow.getCopySpecialName(), MesquiteModule.mesquiteTrunk, whichWindow.getCopySpecialCommand()));		
			}

			paste = new MesquiteMenuItem("Paste", MesquiteModule.mesquiteTrunk, MesquiteModule.makeCommand("paste", whichWindow)); //whichWindow.getPasteCommand());
			newMenu.add(paste);	
			if (whichWindow.getPasteCommand()!=null)
				paste.setShortcut(pasteShortcut);	
			paste.setEnabled(whichWindow.getPasteCommand()!=null); //window must return a copy command if command-based copying is to be enabled; otherwise allows automatic system
			if (whichWindow.getPasteSpecialCommand()!=null){
				newMenu.add(new MesquiteMenuItem(whichWindow.getPasteSpecialName(), MesquiteModule.mesquiteTrunk, whichWindow.getPasteSpecialCommand()));		
			}

			clear =new MesquiteMenuItem("Clear", MesquiteModule.mesquiteTrunk, MesquiteModule.makeCommand("clear", whichWindow));//whichWindow.getClearCommand());
			newMenu.add(clear);		
			if (whichWindow.getClearCommand()!=null)
				clear.setShortcut(clearShortcut);	
			clear.setEnabled(whichWindow.getClearCommand()!=null); //window must return a copy command if command-based copying is to be enabled; otherwise allows automatic system

			selectAll = new MesquiteMenuItem("Select All", MesquiteModule.mesquiteTrunk, MesquiteModule.makeCommand("selectAll", whichWindow));//whichWindow.getSelectAllCommand());
			newMenu.add(selectAll);		
			selectAll.setShortcut(selectAllShortcut);	
			newMenu.add("-");		

			whichWindow.fontSubmenu=MesquiteSubmenu.getFontSubmenu("Font", newMenu, module, menuBar.getOwnerWindow().setFontCommand);
			newMenu.add(whichWindow.fontSubmenu);
			MesquiteCommand setFontSizeCommand = menuBar.getOwnerWindow().setFontSizeCommand;
			whichWindow.fontSizeSubmenu=MesquiteSubmenu.getFontSizeSubmenu("Font Size", newMenu, module, setFontSizeCommand);
			newMenu.add(whichWindow.fontSizeSubmenu);
			Font font = menuBar.getOwnerWindow().currentFont;
			if (font != null) {
				whichWindow.fontSubmenu.checkName(font.getName());
				whichWindow.fontSizeSubmenu.checkName(Integer.toString(font.getSize()));
			}
			newMenu.add(new MenuItem("-"));

			composeSpecificMenuAncestors(menuSpec, newMenu);
			addMySpecificMenuItems(menuSpec, newMenu);
			ListableVector L =module.getEmployeeVector();
			if (L!=null) {
				int num = L.size();
				for (int i=0; i<num; i++){
					Object obj = L.elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					if (mb.window==null) 
						mb.composeSpecificMenuDescendants(menuSpec, newMenu);
				}
			}
			if (newMenu.getItemCount()>0)
				menuBar.add(newMenu);
			return newMenu;
		}
		return null;
	}
	/*.................................................................................................................
	This and the methods it calls below compose a single menu with specification menuSpec.
	This is used for the special File and Windows menus, and the auxiliary menus that MesquiteModules
	can have  */
	final MesquiteMenu composeSpecificMenu(MesquiteMenuBar menuBar, MesquiteMenu newMenu, MesquiteMenuSpec menuSpec, boolean addToMenuBar) {
		if (menuSpec!=null) {
			if (newMenu ==null)
				newMenu= MesquiteMenu.getMenu(menuSpec);

			//for (int i=menu.getItemCount()-1; i>=0; i--)   // 1.1 use menu.removeAll();
			//	menu.remove(i);

			composeSpecificMenuAncestors(menuSpec, newMenu);
			addMySpecificMenuItems(menuSpec, newMenu);
			ListableVector L =module.getEmployeeVector();
			if (L!=null) {
				int num = L.size();
				for (int i=0; i<num; i++) {
					Object obj = L.elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					if (mb.window==null)
						mb.composeSpecificMenuDescendants(menuSpec, newMenu);
				}
			}
			//	composeSpecificMenuDescendants(menu, newMenu);
			addBottom(newMenu, null, "#");
			if (newMenu.getItemCount()>0 && addToMenuBar)
				menuBar.add(newMenu);
			return newMenu;
		}
		return null;
	}
	/*.................................................................................................................
	This and the methods it calls below compose a single menu with specification menuSpec.
	This is used for the special File and Windows menus, and the auxiliary menus that MesquiteModules
	can have  */
	final Menu composeSpecificMenu(Vector menuBar, MesquitePopup newMenu, MesquiteMenuSpec menuSpec, boolean addToMenuBar) {
		if (menuSpec!=null) {
			if (newMenu ==null)
				newMenu= MesquitePopup.getPopupMenu(menuSpec, null);

			//for (int i=menu.getItemCount()-1; i>=0; i--)   // 1.1 use menu.removeAll();
			//	menu.remove(i);

			composeSpecificMenuAncestors(menuSpec, newMenu);
			addMySpecificMenuItems(menuSpec, newMenu);
			ListableVector L =module.getEmployeeVector();
			if (L!=null) {
				int num = L.size();
				for (int i=0; i<num; i++) {
					Object obj = L.elementAt(i);
					MesquiteModule mb = (MesquiteModule)obj;
					if (mb.window==null)
						mb.composeSpecificMenuDescendants(menuSpec, newMenu);
				}
			}
			//	composeSpecificMenuDescendants(menu, newMenu);
			addBottom(newMenu, null, "#");
			if (newMenu.getItemCount()>0 && addToMenuBar)
				menuBar.add(newMenu);
			return newMenu;
		}
		return null;
	}
	/*.................................................................................................................
	This and the methods it calls below compose a single menu with specification menuSpec.
	This is used for the special File and Windows menus, and the auxiliary menus that MesquiteModules
	can have  */
	final MesquiteMenu composeSpecificMenuByZones(MesquiteMenuBar menuBar, MesquiteMenu newMenu, MesquiteMenuSpec menuSpec, boolean addToMenuBar) {
		if (menuSpec!=null) {
			if (newMenu ==null)
				newMenu= MesquiteMenu.getMenu(menuSpec);
			for (int zone=0; zone<MesquiteMenuItemSpec.MAXZONE; zone++) {
				composeSpecificMenuAncestorsByZone(menuSpec, newMenu, zone);
				addMySpecificMenuItemsByZone(menuSpec, newMenu, zone);
				ListableVector L =module.getEmployeeVector();
				if (L!=null) {
					int num = L.size();
					for (int i=0; i<num; i++) {
						Object obj = L.elementAt(i);
						MesquiteModule mb = (MesquiteModule)obj;
						if (mb.window==null)
							mb.composeSpecificMenuDescendantsByZone(menuSpec, newMenu, zone);
					}
				}
			}
			addBottom(newMenu, null, "#");
			if (newMenu.getItemCount()>0 && addToMenuBar)
				menuBar.add(newMenu);
			return newMenu;
		}
		return null;
	}
	/*.................................................................................................................*/
	final void composeSpecificMenuAncestors(MesquiteMenuSpec menuSpec, Menu menu) {
		if (module.getEmployer()!=null) {
			module.getEmployer().composeSpecificMenuAncestors(menuSpec, menu);
			if (module.getEmployer()!=null)
				module.getEmployer().addMySpecificMenuItems(menuSpec, menu);
		}
	}
	/*.................................................................................................................*/
	final void composeSpecificMenuAncestorsByZone(MesquiteMenuSpec menuSpec, MesquiteMenu menu, int zone) {
		if (module.getEmployer()!=null) {
			module.getEmployer().composeSpecificMenuAncestorsByZone(menuSpec, menu, zone);
			if (module.getEmployer()!=null)
				module.getEmployer().addMySpecificMenuItemsByZone(menuSpec, menu, zone);
		}
	}
	/*.................................................................................................................*/
	final void composeSpecificMenuDescendantsByZone(MesquiteMenuSpec menuSpec, MesquiteMenu menu, int zone) {
		addMySpecificMenuItemsByZone(menuSpec, menu, zone);
		ListableVector L =module.getEmployeeVector();
		if (L!=null) {
			int num = L.size();
			for (int i=0; i<num; i++) {
				Object obj = L.elementAt(i);
				MesquiteModule mb = (MesquiteModule)obj;
				if (mb.window==null)
					mb.composeSpecificMenuDescendants(menuSpec, menu);
			}
		}
	}
	/*.................................................................................................................*/
	final void composeSpecificMenuDescendants(MesquiteMenuSpec menuSpec, Menu menu) {
		addMySpecificMenuItems(menuSpec, menu);
		ListableVector L =module.getEmployeeVector();
		if (L!=null) {
			int num = L.size();
			for (int i=0; i<num; i++) {
				Object obj = L.elementAt(i);
				MesquiteModule mb = (MesquiteModule)obj;
				if (mb.window==null)
					mb.composeSpecificMenuDescendants(menuSpec, menu);
			}
		}
	}
	/*.................................................................................................................
	 */
	final void addCurrentWindows(MesquiteMenu menu) {
		//other windows
		Menu currentWindows = new MesquiteMenu("Current Windows");
		MesquiteMenu.add(menu, currentWindows);
		Enumeration e = MesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw.getShowMenuLocation()==2){
				MesquiteMenuItem mmi = new MesquiteMenuItem(mw.getTitle(), MesquiteModule.mesquiteTrunk, mw.showCommand);
				MesquiteMenu.add(currentWindows, mmi);
				mmi.setEnabled(true);
			}
		}
		MesquiteMenu.add(currentWindows, new MenuItem("-"));
		e = MesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw.getShowMenuLocation()==0){
				MesquiteMenuItem mmi = new MesquiteMenuItem(mw.getTitle(), MesquiteModule.mesquiteTrunk, mw.showCommand);
				MesquiteMenu.add(currentWindows, mmi);
				mmi.setEnabled(true);
			}
		}
		//System windows
		e = MesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw.getShowMenuLocation()==1){
				MesquiteMenuItem mmi = new MesquiteMenuItem(mw.getTitle(), MesquiteModule.mesquiteTrunk, mw.showCommand);
				MesquiteMenu.add(menu, mmi);
				mmi.setEnabled(true);
			}
		}
		menu.add(new MesquiteMenuItem("Display License", MesquiteModule.mesquiteTrunk, MesquiteModule.mesquiteTrunk.showLicenseCommand));
		MesquiteMenu.add(menu, new MenuItem("-"));

	}
	/*.................................................................................................................*/
	final void addMySpecificMenuItemsByZone(MesquiteMenuSpec menuSpec, MesquiteMenu menu, int zone){
		if (menuItemsSpecs!=null) {
			for (int i=0; menuItemsSpecs!=null && i<menuItemsSpecs.size(); i++) {
				if (menuItemsSpecs==null)
					return;
				MesquiteMenuItemSpec mmi = (MesquiteMenuItemSpec)menuItemsSpecs.elementAt(i);
				int hiddenStatus = 0;
				String arg = null;
				if (mmi.getArgument() != null)
					arg = mmi.getArgument();
				else if (mmi.command != null)
					arg = mmi.command.getDefaultArguments();
				if (mmi.getZone() == zone && (mmi.whichMenu==menuSpec || getContainingMenuSpec() == menuSpec)) {
					if (InterfaceManager.isFilterable(menu) && (hiddenStatus = InterfaceManager.isHiddenMenuItem(mmi, mmi.getName(),  arg, mmi.command, null)) == InterfaceManager.HIDDEN) 
						;
					else if (mmi instanceof MesquiteSubmenuSpec) {  //add submenu
						MesquiteSubmenu submenu = addSubmenuWithListsIfAbsent(menu,  ((MesquiteSubmenuSpec)mmi));
						submenu.setHiddenStatus(hiddenStatus);
					}
					else if (mmi.submenuIn() != null) {
						MesquiteSubmenu submenu= addSubmenuIfAbsent(menu,  mmi.submenuIn()); //&&&
						if (submenu !=null) {  //CHECK
							if (mmi instanceof MesquiteCMenuItemSpec){
								MesquiteCheckMenuItem cmi = null;
								submenu.add(cmi = new MesquiteCheckMenuItem((MesquiteCMenuItemSpec)mmi));
								cmi.setHiddenStatus(hiddenStatus);
							}
							else {
								MesquiteMenuItem mi = null;
								submenu.add(mi = new MesquiteMenuItem(mmi));
								mi.setHiddenStatus(hiddenStatus);
							}
						}
					}
					else if (!addListsToMenu(mmi, menu)){
						if (mmi instanceof MesquiteCMenuItemSpec){
							MesquiteCheckMenuItem cmi = null;
							MesquiteMenu.add(menu, cmi = new MesquiteCheckMenuItem((MesquiteCMenuItemSpec)mmi));
							cmi.setHiddenStatus(hiddenStatus);
						}
						else {
							MesquiteMenuItem mi = null;
							MesquiteMenu.add(menu, mi = new MesquiteMenuItem(mmi));
							mi.setHiddenStatus(hiddenStatus);
						}
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	final void addMySpecificMenuItems(MesquiteMenuSpec menuSpec, Menu menu){
		if (menuItemsSpecs!=null) {
			for (int i=0; menuItemsSpecs!=null && i<menuItemsSpecs.size(); i++) {
				if (menuItemsSpecs==null)
					return;
				MesquiteMenuItemSpec mmi = (MesquiteMenuItemSpec)menuItemsSpecs.elementAt(i);
				int hiddenStatus = 0;
				String arg = null;
				if (mmi.getArgument() != null)
					arg = mmi.getArgument();
				else if (mmi.command != null)
					arg = mmi.command.getDefaultArguments();

				if (mmi.whichMenu==menuSpec || getContainingMenuSpec() == menuSpec) {
					if (InterfaceManager.isFilterable(menu) && (hiddenStatus = InterfaceManager.isHiddenMenuItem(mmi, mmi.getName(),  arg, mmi.command, null)) == InterfaceManager.HIDDEN) 
						;
					else if (mmi instanceof MesquiteSubmenuSpec) {  //add submenu
						MesquiteSubmenu submenu = addSubmenuWithListsIfAbsent(menu,  ((MesquiteSubmenuSpec)mmi));
						submenu.setHiddenStatus(hiddenStatus);
					}
					else if (mmi.submenuIn() != null) {
						MesquiteSubmenu submenu= addSubmenuIfAbsent(menu,  mmi.submenuIn()); //&&&
						if (submenu !=null) {  //CHECK
							if (mmi instanceof MesquiteCMenuItemSpec){
								MesquiteCheckMenuItem cmi = null;
								submenu.add(cmi = new MesquiteCheckMenuItem((MesquiteCMenuItemSpec)mmi));
								cmi.setHiddenStatus(hiddenStatus);
							}
							else {
								MesquiteMenuItem mi = null;
								submenu.add(mi = new MesquiteMenuItem(mmi));
								mi.setHiddenStatus(hiddenStatus);
							}
						}
					}
					else if (!addListsToMenu(mmi, menu)){
						if (mmi instanceof MesquiteCMenuItemSpec){
							MesquiteCheckMenuItem cmi = null;
							MesquiteMenu.add(menu, cmi = new MesquiteCheckMenuItem((MesquiteCMenuItemSpec)mmi));
							cmi.setHiddenStatus(hiddenStatus);
						}
						else {
							MesquiteMenuItem mi = null;
							MesquiteMenu.add(menu, mi = new MesquiteMenuItem(mmi));
							mi.setHiddenStatus(hiddenStatus);
						}
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	final MesquiteMenu findMenu(MesquiteMenuBar menuBar, String name){
		if (menuItemsSpecs!=null) {
			for (int i=0; i<menuBar.getMenuCount(); i++) {
				if (menuTracing) MesquiteMessage.notifyProgrammer("MENU " + menuBar.getMenu(i).getLabel());
				if (name.equalsIgnoreCase(menuBar.getMenu(i).getLabel()))
					return (MesquiteMenu)menuBar.getMenu(i);
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	final MesquiteSubmenu findSubmenu(Menu menu, MesquiteSubmenuSpec smSpec){
		
		if (smSpec!= null && menu!=null) {
			for (int i=0; i<menu.getItemCount(); i++) {
				if (smSpec.getSubmenuName().equalsIgnoreCase(menu.getItem(i).getLabel()) && menu.getItem(i) instanceof MesquiteSubmenu && ((MesquiteSubmenu)menu.getItem(i)).getOwnerModuleID() == smSpec.getOwnerModuleID()) {
					return (MesquiteSubmenu)menu.getItem(i);
				}
			}
		}
		return null;
	}
	private MenuItem inMenu(Menu menu, MesquiteMenuItemSpec spec){
		for (int i = 0; i< menu.getItemCount(); i++){
			MenuItem mmi = menu.getItem(i);
			MesquiteMenuItemSpec mmis = null;
			if (mmi instanceof MesquiteMenuItem)
				mmis = ((MesquiteMenuItem)mmi).getSpecification();
			else if (mmi instanceof MesquiteCheckMenuItem)
				mmis = ((MesquiteCheckMenuItem)mmi).getSpecification();
			else if (mmi instanceof MesquiteMenu)
				mmis = ((MesquiteMenu)mmi).getSpecification();
			if (mmis != null && mmis == spec)
				return mmi;
		}
		return null;
	}

	private void surveySpecs(Menu menu, MenuItem[] specsOrder, MesquiteInteger count, MenuItemsSpecsVector miSpecsVector){
		try {
			if (miSpecsVector!=null) {
				for (int i=0; miSpecsVector!=null && i<miSpecsVector.size(); i++) {
					if (miSpecsVector==null)
						return;
					MesquiteMenuItemSpec mmi = (MesquiteMenuItemSpec)miSpecsVector.elementAt(i);
					MenuItem mi = inMenu(menu, mmi);
					if (mi != null){
						specsOrder[count.getValue()] = mi;
						count.increment();
					}
				}
			}

		}
		catch (Exception e){
			MesquiteMessage.warnProgrammer("Exception in ListableVector");
		}

	}
	/*.................................................................................................................*/
	void surveyDescendents(Menu menu, MenuItem[] specsOrder, MesquiteInteger count) {
		surveySpecs(menu, specsOrder, count, menuItemsSpecs);
		ListableVector L =module.getEmployeeVector();
		if (L!=null) {
			int num = L.size();
			for (int i=0; i<num; i++) {
				Object obj = L.elementAt(i);
				MesquiteModule mb = (MesquiteModule)obj;
				if (mb.window==null)
					mb.surveyDescendents(menu, specsOrder, count);
			}
		}
	}
	/*.................................................................................................................*/
	void surveyAncestors(Menu menu, MenuItem[] specsOrder, MesquiteInteger count) {
		if (moduleMenuSpec!=null  && menu instanceof MesquiteMenu && ((MesquiteMenu)menu).getSpecification() == moduleMenuSpec) {
			surveySpecs(menu, specsOrder, count, menuItemsSpecs);
			if (module.getEmployer()!=null) 
				module.getEmployer().surveyAncestors(menu, specsOrder, count);
		}
		else {
			if (module.getEmployer()!=null) 
				module.getEmployer().surveyAncestors(menu, specsOrder, count);
			surveySpecs(menu, specsOrder, count, menuItemsSpecs);
		}

	}
	private void sortSubmenusBySpecsOrder(Menu menu){
		if (menu == null)
			return;
		if (menu instanceof MesquiteSubmenu){
			MenuItem[] specsOrder= new MenuItem[menu.getItemCount()];
			MesquiteInteger count = new MesquiteInteger(0);

			if (module.getEmployer()!=null) 
				module.getEmployer().surveyAncestors(menu, specsOrder, count);
			surveyDescendents(menu, specsOrder, count);

			for (int i = 0; i< specsOrder.length; i++)
				if (specsOrder[i] != null)
					menu.remove(specsOrder[i]);
			int ecount = 0;
			for (int i = 0; i< specsOrder.length; i++){
				if (specsOrder[i] != null){
					menu.insert(specsOrder[i], ecount);
					ecount++;
				}
			}
		}
		for (int i = 0; i< menu.getItemCount(); i++){
			MenuItem mmi = menu.getItem(i);
			if (mmi instanceof Menu)
				sortSubmenusBySpecsOrder((Menu)mmi);
		}
	}

	private void dumpMenu(Menu menu, String spacer){
		if (menu == null)
			return;
		String addM = "";
		if (menu instanceof MesquiteMenu && ((MesquiteMenu)menu).getSpecification() != null)
			addM = "  ====> " + ((MesquiteMenu)menu).getSpecification().getName();
		MesquiteMessage.println(spacer + "Menu: " + menu.getLabel() + addM);
		for (int i = 0; i< menu.getItemCount(); i++){
			MenuItem mmi = menu.getItem(i);
			if (mmi instanceof Menu)
				dumpMenu((Menu)mmi, spacer + "  ");
			else {
				String addMI = "";
				if (mmi instanceof MesquiteMenuItem && ((MesquiteMenuItem)mmi).getSpecification() != null)
					addMI = "  ====- " + ((MesquiteMenuItem)mmi).getSpecification().getName();
				else if (mmi instanceof MesquiteCheckMenuItem && ((MesquiteCheckMenuItem)mmi).getSpecification() != null)
					addMI = "  ====- " + ((MesquiteCheckMenuItem)mmi).getSpecification().getName();
				MesquiteMessage.println(spacer + mmi.getLabel() + addMI);
			}
		}
	}

	/*  Menu Keyboard Equivalents / Menu Shortcuts

	 A: Select All
	 B:
	 C:  Copy
	 D: Toggle Fades (Chromaseq)
	 E:
	 F:  Find
	 G:  Find Again
	 H:  Find Footnote String
	 I:  Get Info
	 J:
	 K:  
	 L: Make Item Label (AnnotPanel)
	 M: Move Selected (List Window)
	 N:  New File
	 O:  Open File
	 P:  Print
	 Q:  Quit
	 R:  Revert Selected to Called (Chromaseq)
	 S:  Save File
	 T:  Previous Tool
	 U:  
	 V:  Paste
	 W: Close Window
	 X:  Cut
	 Y:  
	 Z:  Undo

	 Clear: Clear
	 .:  ccShortcut
	 -: Convert Selected To Gaps (Chromaseq)

	 Right, Left:  Next, Previous in Trace Character History
	 Up, Down:  March Selection Forward / Reverse (Scattergram)
	 				next / previous Tree in TraceCharacterHistory

	 3: Find String   
	 6: Find Sequence Again (FindSequence)
	 8: Find Again  



	 */


}


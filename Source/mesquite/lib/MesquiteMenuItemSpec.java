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
import java.awt.event.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/** Specifications to later make menu items.*/

public class MesquiteMenuItemSpec implements Listable, Disposable {
	public MesquiteCommand command;
	private MesquiteModule ownerModule;
	protected String itemName;
	protected String referentID;
	protected MesquiteMenuSpec whichMenu;
	protected String argument = null;
	protected MesquiteSubmenuSpec submenu;
	protected Class dutyClass = null;
	protected Object compatibilityRestriction = null;
	protected QualificationsTest qualificationsTest = null;
	protected ListableVector lVector = null;
	protected Class subclassFilter = null;
	Class ownerClass = null;
	protected StringLister stringLister;
	protected MesquiteInteger shortcut = null;
	protected boolean shortcutNeedsShift = false;
	private int zone = 3; //to indicate what zone in menu to place; rarely used except Analysis menu
	private boolean enabled = true;
	long ownerID = 0;
	boolean document = true;
	public static final int MAXZONE = 7;
	public static CommandChecker checkerMMI = null;
	public static int totalCreated = 0; //to catch memory leaks
	public static int totalDisposed = 0;//to catch memory leaks
	public static int totalFinalized = 0;//to catch memory leaks
	//This is constructor used, e.g. when submenu of candidate employees not requested
	public MesquiteMenuItemSpec(MesquiteMenuSpec whichMenu, String itemName, MesquiteModule ownerModule, MesquiteCommand command) {
		this.whichMenu = whichMenu;
		this.itemName = itemName;
		if (ownerModule!=null){
			ownerID  = ownerModule.getID();
			ownerClass = 	ownerModule.getClass();
		}
		this.command = command;
		totalCreated++;
	}
	public MesquiteSubmenuSpec submenuIn() {
		return submenu;
	}
	public static MesquiteMenuItemSpec getMMISpec(MesquiteMenuSpec whichMenu, String itemName, MesquiteModule ownerModule, MesquiteCommand command){
		if (checkerMMI!=null) {
			checkerMMI.registerMenuItem(ownerModule, itemName, command);
			return null;
		}
		else {
			ownerModule.checkMISVector();
			MesquiteMenuItemSpec mmis = new MesquiteMenuItemSpec(whichMenu, itemName, ownerModule, command);
			ownerModule.getMenuItemSpecs().addElement(mmis, false);
			return mmis;
		}
	}
boolean alreadyDisposed = false;
	public void dispose(){
		if (alreadyDisposed)
			return;
		if (command != null)
			command.dispose();
		command = null;
		ownerModule = null;
		whichMenu= null;
		submenu= null;
		compatibilityRestriction = null;
		qualificationsTest = null;
		lVector = null;
		alreadyDisposed = true;
	}
	public void setZone(int zone){
		if (zone > MAXZONE)
			zone = MAXZONE;
		this.zone = zone;
	}
	public int getZone(){
		return zone;
	}
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	public boolean isEnabled(){
		return enabled;
	}
	public Class getOwnerClass(){
		return ownerClass;
	}
	public long getOwnerModuleID(){
		return ownerID;
	}
	public void setOwnerModuleID(long id){
		ownerID = id;
	}
	public String getName(){
		return getCurrentItemName();
	}
	NameHolder nameHolder;
	public void setNameHolder(NameHolder nH){
		nameHolder = nH;
	}
	public String getCurrentItemName(){  //used if external holder wants control of menu item's name
		if (nameHolder != null){
			String s = nameHolder.getMyName(this);
			if (s != null)
				return s;
		}
		return itemName;
	}
	public void setName(String name){
		itemName = name;
	}
	public void setReferentID(String name){
		referentID = name;
	}
	public String getReferentID(){
		return referentID;
	}
	public void disconnect(){
		//if (command!=null && command.getOwner()==ownerModule) //MEMORY shouldn't adways set null
		//	command.setOwner(null);
		if (command!=null)
			command.dispose();
		command = null;
		ownerModule =null;
		whichMenu =null;
		lVector = null;
		stringLister = null;
		submenu = null;
		totalDisposed++;
	}
	public void finalize() throws Throwable {
		totalFinalized++;
		super.finalize();
	}
	public void setInSubmenu(MesquiteSubmenuSpec submenu){
		this.submenu = submenu;
	}
	public void setInMenu(MesquiteMenuSpec menu){
		this.whichMenu = menu;
	}
	public MesquiteMenuSpec getMenu(){
		return whichMenu;
	}
	public void setArgument(String argument){
		this.argument = argument;
	}
	public String getArgument(){
		return argument;
	}
	public MesquiteCommand getCommand(){
		return command;
	}
	public void setCommand(MesquiteCommand command){
		this.command = command;
	}

	public void setList(Class dutyClass){
		this.dutyClass = dutyClass;
	}
	public void setList(ListableVector lVector){
		this.lVector = lVector;
	}
	public void setList(StringLister s){
		this.stringLister = s;
	}

	public ListableVector getListableVector(){
		return lVector;
	}
	public Class getDutyClass(){
		return dutyClass;
	}
	public void setShortcut(int shortcut){
		this.shortcut = new MesquiteInteger(shortcut);
	}
	public MesquiteInteger getShortcut(){
		return shortcut;
	}
	public void setShortcutNeedsShift(boolean needs){
		shortcutNeedsShift = needs;
	}
	public boolean getShortcutNeedsShift(){
		return shortcutNeedsShift;
	}
	public String[] getStrings(){
		if (stringLister==null) {
			return null;
		}
		else
			return stringLister.getStrings();
	}
	public Object getCompatibilityCheck(){
		return compatibilityRestriction;
	}
	public void setCompatibilityCheck(Object obj){
		this.compatibilityRestriction = obj;
	}
	public QualificationsTest getQualificationsTest(){
		return qualificationsTest;
	}
	public void setQualificationsTest(QualificationsTest obj){
		this.qualificationsTest = obj;
	}
	public Class getListableFilter(){
		return subclassFilter;
	}
	public void setListableFilter(Class subclassFilter){ //THIS should 
		this.subclassFilter = subclassFilter;
	}
	public void setDocumentItems(boolean document){
		this.document = document;
	}
	public boolean getDocumentItems(){
		return document;
	}
}





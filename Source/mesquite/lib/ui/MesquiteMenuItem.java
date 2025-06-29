/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.ui;

import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.accessibility.AccessibleContext;

import mesquite.lib.CommandChecker;
import mesquite.lib.Journal;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteEvent;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.ParseUtil;
import mesquite.lib.simplicity.InterfaceManager;


/* ======================================================================== */
/**A menu item.  Note that a command must be associated with each menu item.
Each menu item belongs to a MesquiteModule.*/

public class MesquiteMenuItem extends MenuItem implements ActionListener {
	public static long totalCreated = 0;//to catch memory leaks
	public static long totalDisposed =0;//to catch memory leaks
	public static long totalFinalized =0;//to catch memory leaks
	MesquiteCommand command; 
	private String itemName;
	private String argument;
	boolean disconnectable = true;
	boolean document = true;
	private long ownerModuleID =0;
	private Listable[] others;
	private MesquiteMenuItemSpec specification;
	int hiddenStatus = InterfaceManager.NORMAL;
	boolean hiddenStatusSet = false;
	boolean hideable = true;
	Class dutyClass = null;
	static boolean traceChoice = false;  //for debugging
	int constructorUsed = -1;

	public MesquiteMenuItem(String itemName, MesquiteModule ownerModule, MesquiteCommand command) {
		super();
		constructorUsed = 0;
		addActionListener(this);
		if (itemName == null) {
			MesquiteMessage.printStackTrace("Menu item with null name: ownerModule " + ownerModule +" command " + command);
			this.setLabel("untitled");
		}
		else
			this.setLabel(itemName);
		this.itemName = itemName;
		this.command = command;
		if (ownerModule!=null)
			this.ownerModuleID = ownerModule.getID();
		if (itemName == null) {
			MesquiteMessage.println("menu item with no name: ");
			setEnabled(false);
		}
		if (command==null) {
			setDocument(false);
			setEnabled(false);
		}
		totalCreated++;
	}


	public MesquiteMenuItem(String itemName, MesquiteModule ownerModule, MesquiteCommand command, String argument) {
		this( itemName,  ownerModule,  command);
		constructorUsed = 1;
		this.argument = argument;
		//totalCreated++;
	}

	//This is constructor used to make menu from specs
	public MesquiteMenuItem(MesquiteMenuItemSpec specification) {
		super();
	constructorUsed = 2;
		if (specification==null)
			return;
		addActionListener(this);
		this.specification = specification;
		if (specification.itemName == null) {
			MesquiteMessage.println("Menu item with null name: specification " + specification);
			MesquiteCommand c = specification.getCommand();
			if (c!=null) {
				MesquiteMessage.println("      -- command " + c.getName() + " to " + c.getOwner());
			}

			this.setLabel("untitled");
		}
		else
			this.setLabel(specification.itemName);
		if (specification.shortcut!=null)
			setShortcut(new MenuShortcut(specification.shortcut.getValue(), specification.shortcutNeedsShift));
		if (!specification.isEnabled())
			setEnabled(false);
		this.itemName = specification.itemName;
		this.ownerModuleID = specification.getOwnerModuleID();
		this.argument = specification.argument;
		this.command = specification.command;
		if (command==null) {
			setDocument(false);
			setEnabled(false);
		}
		//	addActionListener(this);
		totalCreated++;
	}
	
	public MesquiteMenuItemSpec getSpecification(){
		return specification;
	}
	public void setHiddenStatus(int hiddenStatus){
		setHiddenStatus(hiddenStatus, null);
	}
	public void setHideable(boolean h ){
		hideable = h;
	}
	public void setLabel(String label){
		super.setLabel(label);
	}
	public void resetLabel(){
		if (InterfaceManager.isEditingMode()){
			if (!hideable)
				setLabel("(*) " + itemName);
			else if (hiddenStatus == InterfaceManager.TOBEHIDDEN)
				setLabel("(OFF) " + itemName);
			else if (hiddenStatus == InterfaceManager.HIDDENCLASS)
				setLabel("(PACKAGE OFF) " + itemName);
			else
				setLabel("" + itemName);
		}
		else
			setLabel(itemName);
	}
	public void setHiddenStatus(int hiddenStatus, Class dutyClass){
		if (!hideable){
			if (InterfaceManager.isEditingMode())
				setLabel("(*) " + itemName);
			else
				setLabel(itemName);
			return;
		}
		hiddenStatusSet = true;
		this.hiddenStatus = hiddenStatus;
		if (dutyClass != null)
			this.dutyClass = dutyClass;
		if (hiddenStatus == InterfaceManager.TOBEHIDDEN)
			setLabel("(OFF) " + itemName);
		else if (hiddenStatus == InterfaceManager.HIDDENCLASS)
			setLabel("(PACKAGE OFF) " + itemName);
		else
			setLabel("" + itemName);
	}
	public void resetEnable() {
		if (specification != null){
			if (specification instanceof MesquiteSubmenuSpec && ((MesquiteSubmenuSpec)specification).getBehaviorIfNoChoice() == MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE)
				setEnabled(false);
			else
				setEnabled(specification.isEnabled());
		}
	}
	public Listable[] getOthers(){
		return others;
	}
	public void setOthers(Listable[] o){
		others = o;
	}
	public long getOwnerModuleID(){
		return ownerModuleID;
	}
	public MesquiteCommand getCommand(){
		return command;
	}
	public boolean getDocument(){
		return document;
	}
	/** this returns MesquiteMenuItem instead of void for reasons of historical intertia*/
	public MesquiteMenuItem setDocument(boolean document){
		this.document = document;
		return this;
	}

	Object referent = null;
	public void setReferent(Object referent){
		this.referent = referent;
	}
	public Object getReferent(){
		return referent;
	}
	public void disconnect(){
		if (disconnectable){
			command = null;
			totalDisposed++;
		}
		/*	else if (MesquiteTrunk.debugMode){
			MesquiteMessage.println("not disconnectable " + getLabel());
		}*/
	}
	Journal j =null;
	public void actionPerformed(ActionEvent e) {
	//Event queue
		if (command==null || !command.isExecutable())
			return ;//true;
		if (hideable && hiddenStatusSet && InterfaceManager.isEditingMode()){
			if (hiddenStatus == InterfaceManager.NORMAL){
				InterfaceManager.addMenuItemToHidden(itemName, argument, command, dutyClass, true);
				setHiddenStatus(InterfaceManager.TOBEHIDDEN);
				MesquiteTrunk.resetAllMenuBars();
			}
			else if (hiddenStatus != InterfaceManager.HIDDENCLASS) {
				InterfaceManager.removeMenuItemFromHidden(itemName, argument, command, dutyClass, true);
				setHiddenStatus(InterfaceManager.NORMAL);
				MesquiteTrunk.resetAllMenuBars();
			}
		}
		else	if ((e.getModifiers() & ActionEvent.ALT_MASK)!=0) {
			String s = "Menu item \"" + getLabel() + "\"";
			if (getParent() instanceof MesquiteSubmenu && ((MesquiteSubmenu)getParent()).getDutyClass() != null)
				s += " (duty class: " + ((MesquiteSubmenu)getParent()).getDutyClass().getName() + ")";
			if (getParent() instanceof MesquiteSubmenu && ((MesquiteSubmenu)getParent()).getFilter() != null)
				s += " (filter: " + ((MesquiteSubmenu)getParent()).getFilter().getName() + ")";
			MesquiteWindow.respondToQueryMode(s, command, this);
		}
		else {
			int mod = e.getModifiers();
			if (mod != 0)
				chooseItem(argument  + " " + MesquiteEvent.modifiersToString(mod));
			else
				chooseItem(argument); 
		}
	}

	public void chooseItem() {
		chooseItem(argument);
	}
	public void chooseItem(String arg) {
		if (traceChoice)
			System.err.println("Menu item chosen " + 	constructorUsed); //more details?

		if (command == null || !command.isExecutable() || MesquiteTrunk.suppressMenuResponse)
			return;
		if (!command.bypassQueue && MesquiteDialog.currentWizard != null){
			MesquiteTrunk.mesquiteTrunk.alert("Please complete the questions of the Wizard dialog before selecting menu items");
			MesquiteDialog.currentWizard.toFront();
			return;
		}
		if (others!=null) {
			//MesquiteTrunk.mesquiteTrunk.alert(StringArray.toString(others));
			MesquiteInteger io = new MesquiteInteger(0);
			MenuContainer cont = getParent();
			String intro = "C";

			if (cont instanceof MenuItem) {
				String n = ((MenuItem)cont).getLabel();
				if (n != null)
					intro ="For " + n + ", c";
			}
			ListDialog id = new ListDialog(MesquiteWindow.windowOfItem(this), "Select", intro + "hoose one of the following", true, MesquiteString.helpString,others, io, null,true, false);
			id.setVisible(true);
			id.dispose();
			if (io.isCombinable()) {
				if (argument == null)
					argument = "";
				command.setWindowContext(getWindow());
				command.doItMainThread(argument + ParseUtil.tokenize(others[io.getValue()].getName()), CommandChecker.getQueryModeString("Menu item", command, this), this, MesquiteDialog.useWizards);  // command invoked
			}
		}
		else{ 
			if (command.bypassQueue)
				MesquiteThread.acceptNonMesquiteThreads = true; //not elegant and not particular threadsafe, but as it's just to suppress the immune system, it's OK
			command.setWindowContext(getWindow());
			if (argument != null)
				command.doItMainThread(arg, CommandChecker.getQueryModeString("Menu item", command, this), this, MesquiteDialog.useWizards);  // command invoked
			else
				command.doItMainThread("", CommandChecker.getQueryModeString("Menu item", command, this), this, MesquiteDialog.useWizards);  // command invoked
			if (command.bypassQueue)
				MesquiteThread.acceptNonMesquiteThreads = false;
		}
	}
	public MesquiteWindow getWindow(){
		MenuContainer mc = getParent();
		while (mc != null && !(mc instanceof MenuBar) && !(mc instanceof InfoBar)){
			if (mc instanceof MenuComponent)
				mc = ((MenuComponent)mc).getParent();
			else
				mc = null;
		}
		if (mc != null){
			if (mc instanceof MenuBar){
				MenuBar mb = (MenuBar)mc;
				MenuContainer mcc = mb.getParent();
				if (mcc instanceof MesquiteFrame){
					MesquiteWindow w = ((MesquiteFrame)mcc).getFrontWindow();
					return w;
				}
			}
			else if (mc instanceof InfoBar){
				InfoBar mb = (InfoBar)mc;
				return mb.getWindow();
			}
		}
		return null;
	}
	public void finalize() throws Throwable{
		totalFinalized++;
		super.finalize();
	}
}




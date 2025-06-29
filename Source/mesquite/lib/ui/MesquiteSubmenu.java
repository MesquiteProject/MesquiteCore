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

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringArray;
import mesquite.lib.StringLister;
import mesquite.lib.StringUtil;
import mesquite.lib.simplicity.InterfaceManager;


/* ======================================================================== */
/**A submenu.*/
public class MesquiteSubmenu extends MesquiteMenu implements ActionListener {
	public MesquiteCommand command;
	private MesquiteModule ownerModule;
	public Menu ownerMenu;
	MesquiteSubmenuSpec msms;
	MesquiteString checkString = null;
	public boolean disconnectable = true;
	long ownerID;
//	static MesquiteSubmenu[] submenus = new MesquiteSubmenu[255];
	public MesquiteSubmenu(MesquiteSubmenuSpec msms, Menu ownerMenu, MesquiteModule ownerModule) {
		super(msms.getSubmenuName());  
		MesquiteMenuItem.totalCreated++;
		filterable = msms.isFilterable();
		addActionListener(this);
		this.msms = msms;
		this.autoCheckChecks = msms.getAutoCheckChecks();
		if (msms.getSubmenuName() == null) {
			System.out.println("submenu with no name");
		}
		if (!msms.isEnabled())
			setEnabled(false);
		this.ownerMenu = ownerMenu;
		this.ownerID = msms.getOwnerModuleID();
		this.command = msms.getCommand();
	}

	public MesquiteSubmenu(String submenuName, Menu ownerMenu, MesquiteModule ownerModule) {
		super(submenuName);  
		MesquiteMenuItem.totalCreated++;
		addActionListener(this);
		if (submenuName == null) {
			System.out.println("submenu with no name");
		}
		this.ownerMenu = ownerMenu;
		if (ownerModule!=null)
			this.ownerID = ownerModule.getID();
		this.command = command;
	}

	public MesquiteMenuSpec getSpecification(){
		return msms;
	}

	public long getOwnerModuleID(){
		return ownerID;
	}
	public MesquiteCommand getCommand(){
		return command;
	}
	public static MesquiteSubmenu getSubmenu(MesquiteSubmenuSpec msms, Menu ownerMenu, MesquiteModule ownerModule) {
		return new MesquiteSubmenu(msms, ownerMenu, ownerModule);
	}
	public static MesquiteSubmenu getSubmenu(String submenuName, Menu ownerMenu, MesquiteModule ownerModule) {
		return new MesquiteSubmenu(submenuName, ownerMenu, ownerModule);
	}

	/*public static StringLister getFontList() {
		String[] fonts = FontUtil.getSmallFontList();

		StringArray f = new StringArray(fonts.length);
		for (int i=0; i<fonts.length; i++)
			f.setValue(i,fonts[i]);
		return f;
	}
	*/
	public static StringLister getFontSizeList() {
		StringArray f = new StringArray(12);
		f.setValue(0,"6");
		f.setValue(1,"7");
		f.setValue(2,"8");
		f.setValue(3,"9");
		f.setValue(4,"10");
		f.setValue(5,"12");
		f.setValue(6,"14");
		f.setValue(7,"18");
		f.setValue(8,"24");
		f.setValue(9,"30");
		f.setValue(10,"36");
		f.setValue(11,"Other...");
		return f;
	}
	

	public static MesquiteSubmenu getFontSubmenu(String title, Menu ownerMenu, MesquiteModule ownerModule, MesquiteCommand setFontCommand) {
		MesquiteSubmenu submenuFont=getSubmenu(title, ownerMenu, ownerModule);
		submenuFont.setSelected(new MesquiteString(""));
		String[] fonts = FontUtil.getSmallFontList();
		for (int i=0; i<fonts.length; i++)
			submenuFont.add(new MesquiteCheckMenuItem(fonts[i],  null, setFontCommand, StringUtil.tokenize(fonts[i]), submenuFont.checkString));
		submenuFont.addSeparator();
		submenuFont.setAutoCheckChecks(true);
		submenuFont.add(new MesquiteMenuItem("Other...",  null, setFontCommand,FontUtil.otherFontArgument));
		return submenuFont;
	}
	public static MesquiteSubmenu getFontSizeSubmenu(String title, Menu ownerMenu, MesquiteModule ownerModule, MesquiteCommand setFontSizeCommand) {
		MesquiteSubmenu submenuSize=MesquiteSubmenu.getSubmenu(title, ownerMenu, ownerModule);
		submenuSize.setAutoCheckChecks(true);
		submenuSize.setSelected(new MesquiteString(""));
		submenuSize.add(new MesquiteCheckMenuItem("6",  ownerModule, setFontSizeCommand, "6", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("7",  ownerModule, setFontSizeCommand, "7", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("8",  ownerModule, setFontSizeCommand, "8", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("9",  ownerModule, setFontSizeCommand, "9", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("10",  ownerModule, setFontSizeCommand, "10", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("12",  ownerModule, setFontSizeCommand, "12", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("14",  ownerModule, setFontSizeCommand, "14", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("18",  ownerModule, setFontSizeCommand, "18", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("24",  ownerModule, setFontSizeCommand, "24", submenuSize.checkString));
		submenuSize.add(new MesquiteCheckMenuItem("36",  ownerModule, setFontSizeCommand, "36", submenuSize.checkString));
		//	submenuSize.add((new MesquiteMenuItem("36",  ownerModule, setFontSizeCommand, "36")).setDocument(false));
		//submenuSize.add(new MesquiteMenuItem("-",  null, null, null));
		submenuSize.add((new MesquiteMenuItem("Other...",  ownerModule, setFontSizeCommand, null)).setDocument(false));
		return submenuSize;
	}
	boolean autoCheckChecks = false;
	public void setAutoCheckChecks(boolean auto){
		autoCheckChecks = auto;
	}
	public boolean getAutoCheckChecks(){
		return autoCheckChecks;
	}
	public void checkName(String s){
		if (checkString != null)
			checkString.setValue(s);
		resetCheck();
	}
	/** A method to resent which CheckBoxMenuItem is checked. Used only for Font menus*/
	public void resetCheck() {
		try{
		if (checkString != null) {
			int numItems = getItemCount();
			for (int i = 0; i<numItems; i++) {
				MenuItem mi = getItem(i);
				if (mi instanceof CheckboxMenuItem && mi!=null) {
					if (mi.getLabel() != null && mi.getLabel().equals(checkString.getValue())) {
						if (mi instanceof MesquiteCheckMenuItem)
							((MesquiteCheckMenuItem)mi).resetCheck();
						else ((CheckboxMenuItem)mi).setState(true);
					}
					else {
						if (mi instanceof MesquiteCheckMenuItem)
							((MesquiteCheckMenuItem)mi).resetCheck();
						else ((CheckboxMenuItem)mi).setState(false);
					}

				}
			}
		}
		}
		catch (Exception e){
		}
	}
	public void resetEnable() {
		if (msms != null)
			setEnabled(msms.isEnabled());
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
			ownerModule = null;
			ownerMenu = null;
			msms = null;
			if (checkString!=null)
				checkString.releaseMenuItem();
			checkString = null;
			MesquiteMenuItem.totalDisposed++;
		}
	}
	public void setSelected(MesquiteString selected) {
		checkString = selected;
		if (checkString!=null)
			checkString.bindMenuItem();
	}
	public MesquiteString getSelected() {
		return checkString;
	}
	public void chooseItem(int itemNumber){
		if (itemNumber<0 || itemNumber>= getItemCount() || MesquiteTrunk.suppressMenuResponse)
			return;
		String s = getItem(itemNumber).getLabel();
		if (msms.getListableVector() == null && msms.getStrings() == null) {
			if (itemNumber>=0)
				chooseItem(StringUtil.tokenize(s) + " " + itemNumber); 
			else
				chooseItem(StringUtil.tokenize(s));
		}
		else if (msms.getListableVector() != null) {
			if (itemNumber>=0) {
				chooseItem(Integer.toString(itemNumber) + " " + StringUtil.tokenize(s));
			}
		}
		else {
			if (itemNumber>=0)
				chooseItem(StringUtil.tokenize(s) + " " + itemNumber); 
			else
				chooseItem(StringUtil.tokenize(s));
		}	 
		resetCheck();

	}

	public void actionPerformed(ActionEvent e) {
		//Event queue
		Object target = e.getSource();
		if (command != null)  { // there is a submenu-wide command; use it and pass name as argument
			MenuItem mi = ((MenuItem)target);
			if (hideable && InterfaceManager.isEditingMode()){
				if (hiddenStatus == InterfaceManager.NORMAL){
					InterfaceManager.addMenuItemToHidden(mi.getLabel(), StringUtil.tokenize(mi.getLabel()), command, dutyClass, true);
					setHiddenStatus(InterfaceManager.TOBEHIDDEN);
					MesquiteTrunk.resetAllMenuBars();
				}
				else if (hiddenStatus != InterfaceManager.HIDDENCLASS) {
					InterfaceManager.removeMenuItemFromHidden(mi.getLabel(), StringUtil.tokenize(mi.getLabel()), command, dutyClass, true);
					setHiddenStatus(InterfaceManager.NORMAL);
					MesquiteTrunk.resetAllMenuBars();
				}
				return;
			}
			if (mi == this){ //this submenu selected directly, not one of its items
				return;
			}
			int itemNumber = 0;
			while (itemNumber < getItemCount() && mi != getItem(itemNumber) )
				itemNumber++;
			if (itemNumber<0 || itemNumber<= getItemCount() || mi != getItem(itemNumber)) 
				itemNumber = -1;
			if ((e.getModifiers() & ActionEvent.ALT_MASK)!=0) {
				if (!(target instanceof MesquiteMenuItem) && !(target instanceof MesquiteCheckMenuItem)) {
					MesquiteWindow.respondToQueryMode("Menu item \"" + getLabel() + "\"", command, target);
				}
			}
			else if (msms.getListableVector() == null && msms.getStrings() == null) {
				if (itemNumber>=0)
					chooseItem(StringUtil.tokenize(e.getActionCommand()) + " " + itemNumber); 
				else
					chooseItem(StringUtil.tokenize(e.getActionCommand()));
			}
			else if (msms.getListableVector() != null) {
				if (itemNumber>=0) {
					chooseItem(Integer.toString(itemNumber) + " " + StringUtil.tokenize(getItem(itemNumber).getLabel()));
				}
			}
			else {
				if (itemNumber>=0)
					chooseItem(StringUtil.tokenize(e.getActionCommand()) + " " + itemNumber); 
				else
					chooseItem(StringUtil.tokenize(e.getActionCommand()));
			}
		}
		else if (target instanceof MesquiteMenuItem) {
			MesquiteCommand com = ((MesquiteMenuItem)target).getCommand();
			if (com==null)
				return;
			if ((e.getModifiers() & ActionEvent.ALT_MASK)!=0) {
				MesquiteWindow.respondToQueryMode("Menu item \"" + ((MesquiteMenuItem)target).getLabel() + "\"", command, target);
			}
			else
				((MesquiteMenuItem)target).chooseItem("");
		}
		else {
	}
		resetCheck();
	}
	public Class getFilter(){
		if (msms != null && msms.getListableFilter() != null)
			return msms.getListableFilter();
		return null;
	}
	public Class getDutyClass(){
		if (msms != null && msms.getDutyClass() != null)
			return msms.getDutyClass();
		return null;
	}
	public void chooseItem(String arg){
		if (command == null || MesquiteTrunk.suppressMenuResponse)
			return;
		if (!command.bypassQueue && MesquiteDialog.currentWizard != null){
			MesquiteTrunk.mesquiteTrunk.alert("Please complete the questions of the Wizard dialog before selecting menu items");
			MesquiteDialog.currentWizard.toFront();
			return;
		}
		command.doItMainThread(arg, CommandChecker.getQueryModeString("Submenu", command, this), this, MesquiteDialog.useWizards);
		resetCheck();
	}
}




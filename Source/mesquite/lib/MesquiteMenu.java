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
import mesquite.lib.simplicity.InterfaceManager;


/* ======================================================================== */
/** A menu.*/
public class MesquiteMenu extends Menu implements Commandable, Listable{
	public static int totalSubmenus = 0;
	public boolean recycle = false;
	MesquiteMenuSpec spec;
	//static MesquiteSubmenu[] menus = new MesquiteSubmenu[255];
	long id = 0;
	static long numInstances = 0;
	String menuName;
	int hiddenStatus = InterfaceManager.NORMAL;
	Class dutyClass = null;
	boolean hideable = false;
	protected boolean filterable = true;
	public MesquiteMenu(MesquiteMenuSpec spec) {
		super(spec.getLabel(), true);  // true to designate as tearoff; doesn't seem to work on macos
		id = numInstances++;
		filterable = spec.isFilterable();
		if (spec.getLabel() == null) {
			MesquiteMessage.println("menu with no name: ");
			setEnabled(false);
		}
		menuName = spec.getLabel();
		if (!spec.isEnabled())
			setEnabled(false);
		this.spec = spec;
	}
	public MesquiteMenu(String label) {
		super(label, true);  // true to designate as tearoff; doesn't seem to work on macos
		id = numInstances++;
		if (label == null) {
			MesquiteMessage.println("menu with no name: ");
			setEnabled(false);
		}
		menuName = label;
		this.spec = null;
	}
	public boolean isFilterable(){
		return filterable;
		//return filterable;
	}
	public void setHideable(boolean h){
		hideable = h;
	}
	public void resetLabel(){
		if (true)
			return;
		if (InterfaceManager.isEditingMode()){
			if (!hideable || !filterable)
				setLabel("(*) " + menuName);
			else if (hiddenStatus == InterfaceManager.TOBEHIDDEN)
				setLabel("(OFF) " + menuName);
			else if (hiddenStatus == InterfaceManager.HIDDENCLASS)
				setLabel("(PACKAGE OFF) " + menuName);
			else
				setLabel("" + menuName);
		}
		else
			setLabel(menuName);
	}
	public void setHiddenStatus(int hiddenStatus){
		setHiddenStatus(hiddenStatus, null);
	}
	public void setHiddenStatus(int hiddenStatus, Class dutyClass){
		if (!hideable || !filterable){
			if (InterfaceManager.isEditingMode())
				setLabel("(*) " + menuName);
			else
				setLabel(menuName);
			return;
		}
		hideable = true;
		this.hiddenStatus = hiddenStatus;
		if (dutyClass != null)
			this.dutyClass = dutyClass;
		if (hiddenStatus == InterfaceManager.TOBEHIDDEN)
			setLabel("(OFF) " + menuName);
		else if (hiddenStatus == InterfaceManager.HIDDENCLASS)
			setLabel("(PACKAGE OFF) " + menuName);
		else
			setLabel("" + menuName);
	}
	public MesquiteMenuSpec getSpecification(){
		return spec;
	}
	public static MesquiteMenu getMenu(MesquiteMenuSpec spec) {

		return new MesquiteMenu(spec);
	}
	public long getID(){
		return id;
	}
	public String getName(){
		if (this instanceof MesquiteSubmenu)
			return "Submenu: " + getLabel();
		else
			return "Menu: " + getLabel();
	}
	public void listItems(){
		String output = "";
		if (this instanceof MesquiteSubmenu)
			output += ("  Submenu: " + this.getLabel() +"\n");
		else
			output += ("  Menu: " + this.getLabel() +"\n");
		for (int j = 0; j<this.getItemCount(); j++) {
			if (!("-".equals(this.getItem(j).getLabel()))){
				output += ("      " + (j+1) + "  --  " + this.getItem(j).getLabel());
				if(this.getItem(j) instanceof Menu)
					output += " >";
				output += "\n";
			}
		}
		System.out.println(output);
		System.out.println("Enter number to select item");
	}
	/*.................................................................................................................*/
	/** A request for the object to perform a command.  It is passed two strings, the name of the command and the arguments.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(getClass(), null, null, commandName, "show")) {
			listItems();
		}
		else {
			MesquiteInteger pos = new MesquiteInteger();
			int im = MesquiteInteger.fromFirstToken(commandName, pos);
			if (MesquiteInteger.isCombinable(im)){
				im--;
				if (im >=0 && im<getItemCount()){
					MenuItem item = getItem(im);
					if (item instanceof MesquiteMenu){
						System.out.println(((MesquiteMenu)item).getName() + " selected");
						((MesquiteMenu)item).listItems();
						ConsoleThread.setConsoleObjectCommanded(item, true, false);
					}
					else if (item instanceof MesquiteMenuItem){
						if (this instanceof MesquiteSubmenu && ((MesquiteSubmenu)this).getCommand()!= null){
							System.out.println("Item " + (im+1) + " selected");
							((MesquiteSubmenu)this).chooseItem(im);
						}
						else  {
							System.out.println(((MesquiteMenuItem)item).getLabel() + " selected");
							((MesquiteMenuItem)item).chooseItem(arguments);  
						}
					}
				}
			}
		}
		return null;

	}
	boolean itemWithSameLabelExists(String label){
		for (int i=0; i<getItemCount(); i++)
			if (getItem(i).getLabel().equals(label))
				return true;
		return false;
	}
	static boolean itemWithSameLabelExists(Menu menu, String label){
		if (menu == null)
			return false;
		for (int i=0; i<menu.getItemCount(); i++)
			if (menu.getItem(i).getLabel().equals(label))
				return true;
		return false;
	}

	public MenuItem add(MenuItem mmi) {
		if (mmi==null)
			return null;
		if (mmi instanceof Menu)
			totalSubmenus++;
		if (!isFilterable()) {
			if (mmi instanceof MesquiteMenuItem){
				((MesquiteMenuItem)mmi).setHideable(false);
				((MesquiteMenuItem)mmi).resetLabel();
			}
			else if (mmi instanceof MesquiteCheckMenuItem){
				((MesquiteCheckMenuItem)mmi).setHideable(false);
				((MesquiteCheckMenuItem)mmi).resetLabel();
			}
			else if (mmi instanceof MesquiteSubmenu){
				((MesquiteSubmenu)mmi).setHideable(false);
				((MesquiteSubmenu)mmi).resetLabel();
			}

		}
		return super.add(mmi);
	}
	public static void add(Menu menu, MenuItem mmi) {
		if (mmi==null  || menu == null)
			return;

		menu.add(mmi);
	}
}



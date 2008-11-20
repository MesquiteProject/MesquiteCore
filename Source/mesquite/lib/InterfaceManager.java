package mesquite.lib;

import mesquite.lib.duties.WindowHolder;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class InterfaceManager implements Commandable{
	
	/*vvvvvvvvvvvvvvvvvvvvv*/
	public static final boolean enabled = false;
	/*^^^^^^^^^^^^^^^^^^^*/
	

	
	MesquiteModule interfaceWindowBabysitter;
	Parser parser = new Parser();
	UIWindow ww;
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hypertext link has been touched", "[link text]", commandName, "linkTouched")) {
			//linkTouched(parser.getFirstToken(arguments));
			Debugg.println("oops" + arguments);
		}
		return null;
	}
	public void makeWindow(){
		if (interfaceWindowBabysitter != null)
			return;
		interfaceWindowBabysitter = MesquiteTrunk.mesquiteTrunk.hireNamedEmployee (WindowHolder.class, "#WindowBabysitter");
		ww = new UIWindow(interfaceWindowBabysitter, this);
		interfaceWindowBabysitter.setModuleWindow(ww);
		for (int i=0; i< packages.size(); i++){
			MesquiteString ms = (MesquiteString)packages.elementAt(i);
			ww.addPackageToList(ms.getName(), ms.getValue());
		}
	}
	
	Vector packages = new Vector();
	public void addPackageToList(String name, String path){
		packages.addElement(new MesquiteString(name, path));
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

	public static ListableVector hiddenMenuItems;
	public static ListableVector hiddenPackages;
	static {
		hiddenMenuItems = new ListableVector();
		hiddenPackages = new ListableVector();
		//addPackageToHidden("mesquite.coalesce.");
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
			MesquiteMessage.println("HIDDEN " + h.getName() + " h commandable " + h.commandable + " arguments " + h.arguments + " command " + h.command);
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
	
	public static void addMenuItemToHidden(String label, String arguments, MesquiteCommand command, Class dutyClass){
		if (onHiddenMenuItemList(label, arguments, command, dutyClass))
			return;
		if (command == null)
			return;
		Class commandable = null;
		Object owner = command.getOwner();
		if (owner != null)
			commandable = owner.getClass();
		hiddenMenuItems.addElement(new MenuVisibility(label, arguments, command.getName(), commandable, dutyClass), false);
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
		for (int i = 0; i<hiddenMenuItems.size(); i++){
			MenuVisibility vis = (MenuVisibility)hiddenMenuItems.elementAt(i);
			if (vis.matchesMenuItem(label, arguments, command.getName(), commandable, dutyClass)){
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
		for (int i = 0; i<hiddenMenuItems.size(); i++){
			MenuVisibility vis = (MenuVisibility)hiddenMenuItems.elementAt(i);
			if (vis.matchesMenuItem(label, arguments, command, commandable, dutyClass)){
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
	public static int isHiddenMenuItem(String label, String arguments, MesquiteCommand command, Class moduleClass){
		return isHiddenMenuItem(label, arguments, command, moduleClass, (Class)null);
	}

	public static int isHiddenMenuItem(String label, String arguments, MesquiteCommand command, Class moduleClass, Class dutyClass){
		if (mode == ALL)
			return NORMAL;
		
		boolean classHidden = onHiddenClassList(moduleClass);
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
/*
	private static int XXisHiddenMenuItem(String label, String arguments, String command, Class commandable, Class moduleClass){
		return XXisHiddenMenuItem(label, arguments, command, commandable, moduleClass, null);
	}
	private static int XXisHiddenMenuItem(String label, String arguments, String command, Class commandable, Class moduleClass, Class dutyClass){
		if (mode == ALL)
			return NORMAL;
		boolean onList =  onHiddenMenuItemList(label, arguments, command, commandable, dutyClass);
		if (onList){ 
			if (mode == SIMPLE)
				return HIDDEN;
			if (mode == EDITING)
				return TOBEHIDDEN;
		}
		return NORMAL;
	}

	
	*/
	


}
/* ======================================================================== */
class ClassesPane extends ScrollPane{
	public ClassesPane (int scrollPolicy) {
		super(scrollPolicy);
	}
	public void addPanel(Component c){
		addImpl(c, null, 0);
	}
}
class CPanel extends Panel implements ItemListener {
	Vector v = new Vector();
	int h = 30;
	public CPanel(){
		super();
		setLayout(null);
	}
	public void paint(Graphics g){
		//g.setColor(Color.red);
		//g.fillRect(30, 30, 30, 30);
	}
	public Dimension getPreferredSize(){
		return new Dimension(50, v.size()*h);
	}
   public void itemStateChanged(ItemEvent e){
	   PackageCheckbox cb = (PackageCheckbox)e.getItemSelectable();
	   if (cb.getState())
		   InterfaceManager.removePackageFromHidden(cb.pkg);
	   else
		   InterfaceManager.addPackageToHidden(cb.pkg);
	   if (InterfaceManager.mode != InterfaceManager.ALL)
		   MesquiteTrunk.resetAllMenuBars();
   }
	void addPackage(String name, String path){
		PackageCheckbox cb = new PackageCheckbox(name,  path);
		cb.setState(!InterfaceManager.onHiddenClassList(path));
		cb.addItemListener(this);
		v.addElement(cb);
		add(cb);
		resetSizes();
	}
	void resetSizes(){
		for (int i=0; i<v.size(); i++){
			PackageCheckbox cb = (PackageCheckbox)v.elementAt(i);
			cb.setBounds(5, i*h, getWidth(), h);
		}
	}
	void checkStates(){
		for (int i=0; i<v.size(); i++){
			PackageCheckbox cb = (PackageCheckbox)v.elementAt(i);
			cb.setState(!InterfaceManager.onHiddenClassList(cb.pkg));
		}
	}
	public void setSize( int w, int h){
		super.setSize(w, h);
		resetSizes();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		resetSizes();
	}
}

class PackageCheckbox extends Checkbox {
	String pkg = null;
	public PackageCheckbox(String name, String pkg){
		super(name);
		this.pkg = pkg;
	}
}
class UIWindow extends MesquiteWindow implements SystemWindow {
	ClassesPane pane;
	CPanel field;
	
	public UIWindow(MesquiteModule module, InterfaceManager manager) {
		super(module, false);
			setWindowSize(400, 450);
			pane = new ClassesPane(ScrollPane.SCROLLBARS_AS_NEEDED);
			addToWindow(pane);
			field = new CPanel();
			field.setSize(50, 800);
			//field.setBackground(Color.blue);
			field.setLocation(0,0);
			pane.addPanel(field);
			field.setVisible(true);
			pane.setSize(getWidth()-10, getHeight()-50);
			pane.setLocation(0, 40);
			pane.setScrollPosition(0, 0);
			pane.setVisible(true);
			Adjustable adj = pane.getVAdjustable();
			adj.setUnitIncrement(65);
			pane.doLayout();
			resetTitle();
		}
	/*.................................................................................................................*/
	void addPackageToList(String name, String path){
		field.addPackage(name, path);
	}
		/*.................................................................................................................*/
		/** When called the window will determine its own title.  MesquiteWindows need
		to be self-titling so that when things change (names of files, tree blocks, etc.)
		they can reset their titles properly*/
		public void resetTitle(){
			setTitle("Simplify");
		}

		/*.................................................................................................................*/
		/** Gets the minimum height of the content area of the window */
		public int getMinimumContentHeight(){
			return 100;
		}

		/*.................................................................................................................*/
		public void windowResized(){
			super.windowResized();
			if (pane!=null) {
				field.setSize(50, 800);
				pane.setSize(getWidth()-10, getHeight()-50); //getFullWidth
				pane.doLayout();
			}
		}
	}



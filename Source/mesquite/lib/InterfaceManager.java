package mesquite.lib;

import mesquite.lib.duties.WindowHolder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class InterfaceManager {

	/*vvvvvvvvvvvvvvvvvvvvv*/
	public static final boolean enabled = false;
	public static boolean locked = false;
	/*^^^^^^^^^^^^^^^^^^^*/



	MesquiteModule interfaceWindowBabysitter;
	Parser parser = new Parser();
	UIWindow ww;
	/*.................................................................................................................*/
	public void makeWindow(){
		if (interfaceWindowBabysitter != null)
			return;
		interfaceWindowBabysitter = MesquiteTrunk.mesquiteTrunk.hireNamedEmployee (WindowHolder.class, "#WindowBabysitter");
		ww = new UIWindow(interfaceWindowBabysitter, this);
		interfaceWindowBabysitter.setModuleWindow(ww);
		for (int i=0; i< packages.size(); i++){
			ObjectContainer ms = (ObjectContainer)packages.elementAt(i);
			ww.addPackageToList(ms, i, packages.size());
		}
		ww.lock(locked);
	}

	Vector packages = new Vector();
	public void addPackageToList(String name, String path, String explanation){
		packages.addElement(new ObjectContainer(name, new String[]{path, explanation}));
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
		locked = lock;
		if (simplicityWindow != null)
			((UIWindow)simplicityWindow).lock(lock);
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
			((UIWindow)simplicityWindow).resetSimplicity();

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
/* ======================================================================== */
class ClassesPane extends ScrollPane{
	public ClassesPane () {
		//super(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		super(ScrollPane.SCROLLBARS_AS_NEEDED);
	}
	public void addPanel(Component c){
		addImpl(c, null, 0);
	}
}
class CPanel extends MousePanel implements ItemListener {
	Vector v = new Vector();
	int h = 30;
	TextArea explanation = new TextArea("", 20, 20, TextArea.SCROLLBARS_NONE);
	Font fontBig = new Font("SanSerif", Font.BOLD, 14);
	public CPanel(){
		super();
		add(explanation);
		explanation.setBounds(0,0,0,0);
		explanation.setVisible(false);
		explanation.setBackground(ColorTheme.getActiveLight());
		setBackground(ColorTheme.getInterfaceBackgroundPale());
		setLayout(null);
	}
	PackageCheckbox shown = null;
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		shown = getCheckbox(y);
		if (shown != null){
			if (shown.late)
				explanation.setBounds(20,getY(shown)-80,340,80);
			else 
				explanation.setBounds(20,getY(shown)+ shown.getHeight(),340,80);
			explanation.setText(shown.name +": " + shown.expl);
			explanation.setVisible(true);
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		explanation.setVisible(false);
		shown = null;
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		PackageCheckbox nowshown = getCheckbox(y);
		if (nowshown != shown){
			shown = nowshown;
			if (shown != null){
				if (shown.late)
					explanation.setBounds(20,getY(shown)-80,340,80);
				else 
					explanation.setBounds(20,getY(shown)+ shown.getHeight(),340,80);
				explanation.setText(shown.name +": " + shown.expl);
				explanation.setVisible(true);
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		explanation.setBounds(20,20,100,100);
		explanation.setVisible(true);
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*
	public void mouseUp(int modifiers, int x, int y, MesquiteTool toolTouching) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		explanation.setVisible(false);
			MesquiteWindow.uncheckDoomed(this);
	}
	 */
	public void paint(Graphics g){
		Font font = g.getFont();
		g.setFont(fontBig);
		g.drawString("?", 4, 20);//g.setColor(Color.red);
		g.setFont(font);
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
		if (InterfaceManager.mode != InterfaceManager.ALL){
			MesquiteTrunk.resetAllMenuBars();
			MesquiteTrunk.resetAllToolPalettes();
		}
	}

	void addPackage(String name, String path, String explanation, int i, int total){
		PackageCheckbox cb = new PackageCheckbox(name,  path, explanation);
		if (total-i<4)
			cb.setLate(true);
		cb.setState(!InterfaceManager.onHiddenClassList(path));
		cb.addItemListener(this);
		v.addElement(cb);
		cb.setVisible(true);
		add(cb);
		resetSizes();
	}
	void resetSizes(){
		for (int i=0; i<v.size(); i++){
			PackageCheckbox cb = (PackageCheckbox)v.elementAt(i);
			cb.setBounds(18, i*h, getWidth(), h);
		}
	}
	int getY(PackageCheckbox box){
		for (int i=0; i<v.size(); i++){
			if (box == v.elementAt(i))
				return (i)*h;
		}
		return 0;
	}
	PackageCheckbox getCheckbox(int y){
		for (int i=0; i<v.size(); i++){
			if (y< (1+i)*h)
				return (PackageCheckbox)v.elementAt(i);
		}
		return null;
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
	String expl = null;
	String name = null;
	boolean late = false;
	public PackageCheckbox(String name, String pkg, String explanation){
		super(name);
		setBackground(Color.white);
		this.name = name;
		this.pkg = pkg;
		this.expl = explanation;
	}
	void setLate(boolean late){
		this.late = late;
	}
}
class LSPanel extends MousePanel {
	MesquitePopup popup;
	Polygon dropDownTriangle;
	public LSPanel(){
		dropDownTriangle=MesquitePopup.getDropDownTriangle();
	}
	/*.................................................................................................................*/
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		String s = "Save/Load";
		Font font = g.getFont();
		FontMetrics fontMet = g.getFontMetrics(font);
		int hA = fontMet.getAscent();
		int w = fontMet.stringWidth(s)+16;
		int h = hA +fontMet.getDescent()+4;
		int y =16-hA-2;
		g.drawRoundRect(0, y, w, h , 3, 3);

		g.setColor(ColorTheme.getActiveLight());
		g.fillRoundRect(0, y, w, h , 3, 3);
		g.setColor(Color.black);
		g.drawString(s, 4, 16);
		g.drawRoundRect(0, y, w, h , 3, 3);


		dropDownTriangle.translate(w - 8 ,8);
		g.setColor(Color.white);
		g.drawPolygon(dropDownTriangle);
		g.setColor(Color.black);
		g.fillPolygon(dropDownTriangle);
		dropDownTriangle.translate(-w + 8 ,-8);
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		redoMenu();
		popup.show(this, 0,20);
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	void redoMenu() {
		if (popup==null)
			popup = new MesquitePopup(this);
		popup.removeAll();
		InterfaceManager.getLoadSaveMenuItems(popup);
		add(popup);
	}
}
class IPanel extends MousePanel {
	MesquiteCommand command;
	public IPanel(){
	}
	/*.................................................................................................................*/
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		String s = "Instructions";
		Font font = g.getFont();
		FontMetrics fontMet = g.getFontMetrics(font);
		int hA = fontMet.getAscent();
		int w = fontMet.stringWidth(s)+8;
		int h = hA +fontMet.getDescent()+4;
		int y = 16-hA-2;
		g.setColor(ColorTheme.getActiveLight());
		g.fillRoundRect(0, y, w, h , 3, 3);
		g.setColor(Color.black);
		g.drawString(s, 4, 16);
		g.drawRoundRect(0, y, w, h , 3, 3);
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool toolTouching) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		command = new MesquiteCommand("showInstructions", InterfaceManager.simplicityWindow);
		command.doItMainThread(null, null, null);
		MesquiteWindow.uncheckDoomed(this);
	}
}
class CBPanel extends Panel implements ItemListener {
	CheckboxGroup cbg;
	Checkbox powerCB, simplerCB, editSimpleCB;
	UIWindow w;
	Image editing, power, simple;
	MesquitePopup popup;
	LSPanel lsPanel;
	IPanel iPanel;
	int cbwidth = 160;
	Font fontBig = new Font("SansSerif", Font.BOLD, 12);
	int top = 20;
	public CBPanel(UIWindow w){
		super();
		this.w = w;
		cbg = new CheckboxGroup();
		powerCB = new Checkbox("Full Interface", cbg, true);
		simplerCB = new Checkbox("Simple Interface", cbg, true);
		editSimpleCB = new Checkbox("Edit Simple Interface", cbg, true);
		cbg.setSelectedCheckbox(powerCB);
		setLayout(null);
		add(powerCB);
		add(simplerCB);
		add(editSimpleCB);
		powerCB.setBounds(22, top + 2, cbwidth, 16);
		powerCB.setFont(fontBig);
		powerCB.setVisible(true);
		simplerCB.setBounds(22, top + 22, cbwidth, 16);
		simplerCB.setVisible(true);
		simplerCB.setFont(fontBig);
		editSimpleCB.setBounds(22, top + 42, cbwidth, 16);
		editSimpleCB.setVisible(true);
		editSimpleCB.setFont(fontBig);
		powerCB.addItemListener(this);
		simplerCB.addItemListener(this);
		editSimpleCB.addItemListener(this);
		power = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "power.gif");  
		simple = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "simple.gif");  
		editing = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "notesTool.gif");  

		lsPanel = new LSPanel();
		lsPanel.setBounds(getWidth()-100, top + 2, 100, 24);
		lsPanel.setFont(fontBig);
		lsPanel.setVisible(true);
		add(lsPanel);

		iPanel = new IPanel();
		iPanel.setBounds(getWidth()-100, top + 32, 100, 24);
		iPanel.setFont(fontBig);
		iPanel.setVisible(true);
		add(iPanel);
	}

	/*.................................................................................................................*/
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.setFont(fontBig);
		FontMetrics fontMet = g.getFontMetrics(fontBig);
		int hA = fontMet.getAscent();
		String title = "Interface Simplification";
		int w = fontMet.stringWidth(title)+16;
		g.drawString(title, (getWidth() - w)/2, 16);

		Composite composite = ColorDistribution.getComposite(g);
		if (InterfaceManager.mode != InterfaceManager.ALL)
			ColorDistribution.setTransparentGraphics(g,0.1f);
		g.drawImage(power, 4, top  + 2, this);

		ColorDistribution.setComposite(g,composite);
		if (InterfaceManager.mode != InterfaceManager.SIMPLE)
			ColorDistribution.setTransparentGraphics(g,0.1f);
		g.drawImage(simple, 4, top  + 22, this);

		ColorDistribution.setComposite(g,composite);
		if (InterfaceManager.mode != InterfaceManager.EDITING)
			ColorDistribution.setTransparentGraphics(g,0.1f);
		g.drawImage(editing, 4, top  + 42, this);

		ColorDistribution.setComposite(g,composite);

		MesquiteWindow.uncheckDoomed(this);
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		if (lsPanel != null){
			lsPanel.setBounds(getWidth()-100, top + 2, 100, 24);
			iPanel.setBounds(getWidth()-100, top + 32, 100, 24);
		}
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		if (lsPanel != null){
			lsPanel.setBounds(getWidth()-100, top + 2, 100, 24);
			iPanel.setBounds(getWidth()-100, top + 32, 100, 24);
		}
	}
	public void resetStates(){
		if (InterfaceManager.mode == InterfaceManager.ALL)
			powerCB.setState(true);
		else if (InterfaceManager.mode == InterfaceManager.SIMPLE)
			simplerCB.setState(true);
		else if (InterfaceManager.mode == InterfaceManager.EDITING)
			editSimpleCB.setState(true);
		repaint();
	}
	public void itemStateChanged(ItemEvent e){
		if (powerCB.getState())
			InterfaceManager.mode = InterfaceManager.ALL;
		if (simplerCB.getState())
			InterfaceManager.mode = InterfaceManager.SIMPLE;
		if (editSimpleCB.getState())
			InterfaceManager.mode = InterfaceManager.EDITING;
		repaint();
		MesquiteModule.resetAllMenuBars();
		MesquiteModule.resetAllToolPalettes();
		MesquiteWindow.resetAllSimplicity();
	}
}
class CHPanel extends Panel  {
	Font fontBig = new Font("SansSerif", Font.BOLD, 12);
	Font fontSmall = new Font("SansSerif", Font.PLAIN, 10);
	/*.................................................................................................................*/
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.drawLine(0, 0, getWidth(), 0);
		g.drawLine(0, 1, getWidth(), 1);
		g.setFont(fontBig);
		g.drawString("Packages Shown", 8, 18);
		g.setFont(fontSmall);
		g.drawString("Deslected packages are hidden when Simple interface is chosen.", 8, 34);

		MesquiteWindow.uncheckDoomed(this);
	}

}
class UIWindow extends MesquiteWindow implements SystemWindow {
	ClassesPane pane;
	CPanel field;
	CBPanel cb;
	CHPanel ch;
	int headingHeight = 84;
	int classesHeight = 130;
	String instructions;
	//SimplicityStrip simplicityStrip;
	public UIWindow(MesquiteModule module, InterfaceManager manager) {
		super(module, false);
		setWindowSize(400, 450);
		//ADD: title for classes
		//instructions
		//saved settings... load, save
		pane = new ClassesPane();
		addToWindow(pane);
		//simplicityStrip = new SimplicityStrip(this);
		addToWindow(cb = new CBPanel(this));
		//addToWindow(simplicityStrip);
		cb.setBounds(0, 0, getWidth(), headingHeight);
		cb.setVisible(true);
		String simpLoc = MesquiteTrunk.mesquiteTrunk.getRootPath() + "extras" + MesquiteFile.fileSeparator + "simplifications" + MesquiteFile.fileSeparator + "simplification.html";
		instructions = MesquiteFile.getFileContentsAsString(simpLoc);
		addToWindow(ch = new CHPanel());
		ch.setBackground(Color.white);
		//addToWindow(simplicityStrip);
		ch.setBounds(0, headingHeight, getWidth(), classesHeight - headingHeight);
		ch.setVisible(true);
		field = new CPanel();
		field.setSize(50, 800);
		field.setLocation(0,0);
		pane.addPanel(field);
		field.setVisible(true);
		pane.setSize(getWidth(), getHeight()-classesHeight-20);
		pane.setLocation(0, classesHeight);
		//pane.setScrollPosition(0, 0);
		pane.setVisible(true);
		//	Adjustable adj = pane.getVAdjustable();
		//	adj.setUnitIncrement(65);
		pane.doLayout();
		resetTitle();
		InterfaceManager.simplicityWindow = this;
		resetSimplicity();
	}
	
	public void lock(boolean L){
		setVisible(!L);
	}
	public void setVisible(boolean vis){
		if (InterfaceManager.locked && vis)
			return;
		super.setVisible(vis);
	}
	void resetSimplicity(){
		if (cb != null){
			cb.resetStates();
			field.checkStates();
		}
	}
	/*.................................................................................................................*/
	void addPackageToList(ObjectContainer cont, int i, int total){
		String name = cont.getName();
		String[] s = (String[])cont.getObject();
		field.addPackage(name, s[0], s[1], i, total);
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
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows instructions", null, commandName, "showInstructions")) {
			MesquiteModule.mesquiteTrunk.alertHTML(instructions, "Simplification Instructions", "Simplification Instructions", 600, 600);
			/*MesquiteModule.mesquiteTrunk.alert("The Mesquite interface can be shown in Simple mode, in which some menu items and tools are hidden, " +
					"or in Full mode, in which all available menu items and tools are shown. To toggle between modes, use the small menu you touched to make this message appear. " +
					"\n\nThere are two ways to choose what items are hidden.  First, you can go to the Simplify panel of the main Mesquite system window, and " + 
					" choose which packages are shown or hidden.  All or most menu items and tools pertaining to hidden packages will be hidden.  " +
					" Second, you can select the menu item \"Edit Simple Interface\" from this menu to choose particular items to hide. " +
					"Then, when you select menu items or tools, they will be highlighted to show they will be hidden. Hidden menu items will be marked with \"(OFF)\"" +
					" if you have chosen that particular item to hide, \"(PACKAGE OFF)\" if the item will be hidden because its package has been hidden. " +
					" Hidden tools will be ringed in red if you have chosen that particular item to hide, in blue if hidden because its package has been hidden. " +
			" After you have edited what you want to hide, choose \"Simple Interface\" in this menu to show the simplified interface.");
			*/
			return null;
		}
		else if (checker.compare(this.getClass(), "Saves the current simplification", null, commandName, "saveCurrent")) {
			InterfaceManager.saveCurrentSettingsFile();
		}
		else if (checker.compare(this.getClass(), "Loads a simplification", null, commandName, "load")) {
			int i = MesquiteInteger.fromString( new Parser(arguments));
			InterfaceManager.loadSettingsFile(i);
		}
		else if (checker.compare(this.getClass(), "Shows Instructions", null, commandName, "showInstructions")) {
			int i = MesquiteInteger.fromString( new Parser(arguments));
			InterfaceManager.loadSettingsFile(i);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void windowResized(){
		super.windowResized();
		if (pane!=null && field != null) {
			cb.setSize(getWidth(), headingHeight);
			ch.setSize(getWidth(), classesHeight-headingHeight);
			field.setSize(50, 800);
			pane.setSize(getWidth(), getHeight()-classesHeight-20);
			pane.doLayout();
		}
	}
}



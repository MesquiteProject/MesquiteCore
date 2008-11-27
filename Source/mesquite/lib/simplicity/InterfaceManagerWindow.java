package mesquite.lib.simplicity;

import mesquite.lib.ColorDistribution;
import mesquite.lib.ColorTheme;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MenuVisibility;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteImage;
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
import mesquite.lib.MousePanel;
import mesquite.lib.ObjectContainer;
import mesquite.lib.Parser;
import mesquite.lib.SystemWindow;
import mesquite.lib.XMLUtil;
import mesquite.lib.duties.WindowHolder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class InterfaceManagerWindow extends MesquiteWindow implements SystemWindow {
	ClassesPane pane;
	PackagesPanel field;
	CBPanel cb;
	CHPanel ch;
	int headingHeight = 84;
	int classesHeight = 130;
	String instructions;
	//SimplicityStrip simplicityStrip;
	public InterfaceManagerWindow(MesquiteModule module, InterfaceManager manager) {
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
		field = new PackagesPanel();
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
	void addPackages(Vector allPackages){
		field.addPackages(allPackages);
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
class PackagesPanel extends MousePanel implements ItemListener {
	Listable[] v;
	int h = 30;
	Image triangleRight, triangleDown;
	TextArea explanation = new TextArea("", 20, 20, TextArea.SCROLLBARS_NONE);
	Font fontBig = new Font("SanSerif", Font.BOLD, 14);
	public PackagesPanel(){
		super();
		add(explanation);
		explanation.setBounds(0,0,0,0);
		explanation.setVisible(false);
		explanation.setBackground(ColorTheme.getActiveLight());
		triangleRight = MesquiteImage.getImage(MesquiteTrunk.getRootImageDirectoryPath() + "triangleRightOn.gif");
		triangleDown = MesquiteImage.getImage(MesquiteTrunk.getRootImageDirectoryPath() + "triangleDownOn.gif");
		setBackground(ColorTheme.getInterfaceBackgroundPale());
		setLayout(null);
	}
	PackageCheckbox shown = null;
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		/*if (x>18){
			MesquiteWindow.uncheckDoomed(this);
			return;
		}*/
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
		/*if (x>18){
			MesquiteWindow.uncheckDoomed(this);
			return;
		}*/
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
	/*.................................................................................................................*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool toolTouching) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (x<18){
			PackageCheckbox cb = getCheckbox(y);
			if (cb != null && cb.isPackage){
				cb.collapsed = !cb.collapsed;
				resetSizes();
				repaint();
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void paint(Graphics g){
		g.setColor(Color.white);
		g.fillRect(16, 0, getWidth(), getHeight());
		g.setColor(Color.black);
		//Font font = g.getFont();
		//g.setFont(fontBig);
		//g.drawString("?", 4, 20);//g.setColor(Color.red);
		//g.setFont(font);
		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			if (cb.isPackage){
				if (cb.collapsed)
					g.drawImage(triangleRight, 0, cb.getY()+6, this);
				else 
					g.drawImage(triangleDown, 0, cb.getY()+6, this);
			}
		}
		//g.fillRect(30, 30, 30, 30);
	}
	public Dimension getPreferredSize(){
		if (v == null)
			return new Dimension(50, 600);
		return new Dimension(50, v.length*h);
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
		checkStates();
	}
	/*.................................................................................................................*/
	void addPackages(Vector allPackages){

		/*Listable[] m = new Listable[]{new MesquiteString("b", "b"), new MesquiteString("a", "a"), new MesquiteString("c", "c")};
		Debugg.println("before---------");
		for(int i=0; i<m.length; i++)
			Debugg.println("m " + m[i]);
		ListableVector.sort(m);
		Debugg.println("after---------");
		for(int i=0; i<m.length; i++)
			Debugg.println("m " + m[i]);
		int[] mm = new int[]{1, 0, 2};
		Debugg.println("before---------");
		for(int i=0; i<mm.length; i++)
			Debugg.println("mm " + mm[i]);
		IntegerArray.sort(mm);
		Debugg.println("after---------");
		for(int i=0; i<mm.length; i++)
			Debugg.println("mm " + mm[i]);
		 */
		v = new Listable[allPackages.size()];
		for (int i=0; i< allPackages.size(); i++){
			ObjectContainer ms = (ObjectContainer)allPackages.elementAt(i);
			String name = ms.getName();
			String[] s = (String[])ms.getObject();

			PackageCheckbox cb = new PackageCheckbox(name,  s[0], s[1],  "true".equals(s[2]), "true".equals(s[3]));
			if (allPackages.size()-i<4)
				cb.setLate(true);
			cb.setState(!InterfaceManager.onHiddenClassListExactly(s[0]));
			if (cb.getState() && InterfaceManager.onHiddenClassList(s[0]))
				cb.setEnabled(false);
			cb.addItemListener(this);
			v[i] = cb;
			cb.setVisible(true);
		}
		ListableVector.sort(v);
		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			add(cb);
			//Debugg.println(cb.getName());
		}
		resetSizes();
	}
	void resetSizes(){
		if (v == null)
			return;
		boolean on = true;
		int count = 0;
		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			if (cb.isPackage){
				on = !cb.collapsed;
				cb.setBounds(18,count++*h, getWidth(), h);
			}
			else if (!on){
				cb.setVisible(false);
			}
			else {
				cb.setVisible(true);
				cb.setBounds(38, count++*h, getWidth(), h);
			}
		}
	}
	int getY(PackageCheckbox box){
		if (v == null)
			return 0;
		boolean on = true;
		int count = 0;
		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			if (cb.isPackage){
				on = !cb.collapsed;
				int k = count++;
				if (box == v[i] && box.isVisible())
					return (count)*h;
			}
			else if (on) {
				int k = count++;
				if (box == v[i] && box.isVisible())
					return (count)*h;
			}
		}
		/*for (int i=0; i<v.length; i++){
			if (box == v[i] && box.isVisible())
				return (i)*h;
		}*/
		return 0;
	}
	PackageCheckbox getCheckbox(int y){
		if (v == null)
			return null;
		boolean on = true;
		int count = 0;
		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			if (cb.isPackage){
				on = !cb.collapsed;
				int k = count++;
				if (y< (1+k)*h)
					return (PackageCheckbox)v[i];
			}
			else if (on) {
				int k = count++;
				if (y< (1+k)*h)
					return (PackageCheckbox)v[i];
			}
		}
		/*for (int i=0; i<v.length; i++){
			if (y< (1+i)*h)
				return (PackageCheckbox)v[i];
		}*/
		return null;
	}
	void checkStates(){
		if (v == null)
			return;
		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			cb.setState(!InterfaceManager.onHiddenClassListExactly(cb.pkg));
			if (cb.getState() && InterfaceManager.onHiddenClassList(cb.pkg))
				cb.setEnabled(false);
			else if (!cb.hideable)
				cb.setEnabled(false);
			else
				cb.setEnabled(true);
		}
		repaint();
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

class PackageCheckbox extends Checkbox implements Listable {
	String pkg = null;
	String expl = null;
	String name = null;
	boolean late = false;
	boolean isPackage = false;
	boolean hideable = true;
	boolean collapsed = true;
	public PackageCheckbox(String name, String pkg, String explanation, boolean isHideable, boolean isPackage){
		super(name);
		setBackground(Color.white);
		this.name = name;
		this.pkg = pkg;
		this.expl = explanation;
		this.isPackage = isPackage;
		this.hideable = isHideable;
		setEnabled(hideable);
	}
	public String getName(){
		return pkg;
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
	InterfaceManagerWindow w;
	Image editing, power, simple;
	MesquitePopup popup;
	LSPanel lsPanel;
	IPanel iPanel;
	int cbwidth = 160;
	Font fontBig = new Font("SansSerif", Font.BOLD, 12);
	int top = 20;
	public CBPanel(InterfaceManagerWindow w){
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



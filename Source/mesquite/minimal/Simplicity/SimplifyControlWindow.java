package mesquite.minimal.Simplicity;

import mesquite.lib.*;
import mesquite.lib.simplicity.InterfaceManager;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.MQComponent;
import mesquite.lib.ui.MQComponentHelper;
import mesquite.lib.ui.MQJScrollPane;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MQScrollPane;
import mesquite.lib.ui.MQTextArea;
import mesquite.lib.ui.MQJEditorPane;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteSubmenu;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MessagePanel;
import mesquite.lib.ui.MousePanel;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

public class SimplifyControlWindow extends MesquiteWindow implements SystemWindow {
	ClassesPane classesPane;
	MessagePanel message;
	PackagesPanel packagesPanel;
	TrianglePanel trianglePanel;
	ModePanel modePanel;
	ClassHeadersPanel classesHeaderPanel;
	JEditorPane instructionsPanel;
	JScrollPane instructionsScrollPane;
	int modePanelHeight = 96;
	int classesHeight = 140;
	public String instructions;
	OuterPackagesPanel field;
	MovePanel movePanel;
	public SimplifyControlWindow(MesquiteModule module, InterfaceManager manager, Vector allPackages) {
		super(module, false);
		resetTitle();
		setWindowSize(400, 450);

		//instructions
		String simpLoc = module.getPath() + "instructions.html";
		instructions = MesquiteFile.getFileContentsAsString(simpLoc);
		instructions = StringUtil.replace(instructions, "RPATH", MesquiteFile.massageFilePathToURL(MesquiteModule.getRootImageDirectoryPath()));
		if (instructions == null)
			instructions = "";
		//saved settings... load, save
		classesPane = new ClassesPane();
		modePanel = new ModePanel(this);
		modePanel.setBounds(0, 0, getWidth(), modePanelHeight);
		modePanel.setVisible(true);
		instructionsPanel = new MQJEditorPane("text/html","<html></html>");
		instructionsPanel.setEditable(false);
		instructionsPanel.setBackground(Color.white);
		instructionsPanel.setForeground(Color.black);
		instructionsPanel.setText(instructions);
		instructionsScrollPane = new  MQJScrollPane(); 
		instructionsScrollPane.getViewport().add( instructionsPanel,  BorderLayout.CENTER ); 
		instructionsScrollPane.setVisible(true);
		instructionsScrollPane.setBounds(0, modePanelHeight, getWidth(), getHeight()-modePanelHeight);
		classesHeaderPanel = new ClassHeadersPanel();
		classesHeaderPanel.setBackground(Color.white);

		classesHeaderPanel.setBounds(0, modePanelHeight, getWidth(), classesHeight - modePanelHeight);
		classesHeaderPanel.setVisible(true);
		packagesPanel = new PackagesPanel(this);
		
		packagesPanel.addPackages(allPackages);
		field = new OuterPackagesPanel(packagesPanel);
		field.setBackground(Color.white);
		field.setLayout(null);
		field.setSize(50, packagesPanel.getH());
		field.add(packagesPanel);
		packagesPanel.setSize(50, packagesPanel.getH());
		packagesPanel.setLocation(18,0);
		trianglePanel = new TrianglePanel(packagesPanel);
		field.add(trianglePanel);
		trianglePanel.setBounds(0, 0, 18, packagesPanel.getH());
		
		classesPane.addPanel(field);
		movePanel = new MovePanel(this);
		addToWindow(classesPane);
		addToWindow(modePanel);
		addToWindow(instructionsScrollPane);
		addToWindow(classesHeaderPanel);
		addToWindow(movePanel);
		field.setVisible(true);
		classesPane.setSize(getWidth(), getHeight()-classesHeight-20);
		classesPane.setLocation(0, classesHeight);

		classesPane.setVisible(true);

		classesPane.doLayout();
		message = new MessagePanel(ColorTheme.getExtInterfaceBackground(), false);
		message.setTextColor(ColorTheme.getExtInterfaceTextContrast());
		addToWindow(message);
		
		message.setBounds(0, getHeight()-20, getWidth(), 20);
		if (InterfaceManager.themeName == null)
			message.setMessage("Custom");
		else
			message.setMessage(InterfaceManager.themeName);
		resetSizes();
		resetTitle();
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
	public void resetSimplicity(){
		if (modePanel != null){
			modePanel.resetStates();
			packagesPanel.checkStates();

		}
		if (!InterfaceManager.isSimpleMode() && !InterfaceManager.isEditingMode())
			message.setMessage("Using Full Interface");
		else if (InterfaceManager.themeName!= null){
			if (!InterfaceManager.isSimpleMode())
				message.setMessage("Theme: " + InterfaceManager.themeName);
			else 
				message.setMessage("Current Theme: " + InterfaceManager.themeName);
		}
		else
			if (!InterfaceManager.isSimpleMode())
				message.setMessage("Theme: Custom");
			else 
				message.setMessage("Current Theme: Custom");
		resetSizes();
	}
	/*.................................................................................................................*/
	public void addPackages(Vector allPackages){
		packagesPanel.addPackages(allPackages);
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
		to be self-titling so that when things change (names of files, tree blocks, etc.)
		they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Simplification");
	}

	/*.................................................................................................................*/
	/** Gets the minimum height of the content area of the window */
	public int getMinimumContentHeight(){
		return 100;
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows instructions", null, commandName, "showInstructions")) {
			MesquiteModule.mesquiteTrunk.alertHTML(MesquiteModule.mesquiteTrunk.containerOfModule().getParentFrame(), instructions, "Simplification Instructions", "Simplification Instructions", 600, 600);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	int smallInstructionsHeight = 120;

	void resetSizes(){
		if (classesPane!=null && field != null) {
			modePanel.setBounds(0, 0, getWidth(), modePanelHeight);
			message.setBounds(0, getHeight()-20, getWidth(), 20);

			if (InterfaceManager.isEditingMode()){
				classesHeaderPanel.setSize(getWidth(), classesHeight-modePanelHeight);
				if (smallInstructionsHeight < 60)
					smallInstructionsHeight = 60;
				int cpheight = getHeight()-classesHeight-2 -smallInstructionsHeight;
				if (cpheight < 100)
					smallInstructionsHeight = getHeight()-classesHeight-2 -100;
				
				classesPane.setSize(getWidth(), getHeight()-classesHeight-2 -smallInstructionsHeight);
				movePanel.setBounds(0,getHeight()- smallInstructionsHeight-2, getWidth(), 2);
				packagesPanel.setSize(getWidth()-38, packagesPanel.getH());
				trianglePanel.setSize(18, packagesPanel.getH());
				field.setSize(getWidth()-20, packagesPanel.getH());
				instructionsScrollPane.setBounds(0, getHeight()- smallInstructionsHeight, getWidth(), smallInstructionsHeight-20);
				try{
					classesPane.doLayout();
					instructionsScrollPane.doLayout();
				}
				catch (Exception e){
				}
			}
			else {
				classesHeaderPanel.setSize(0,0);
				classesPane.setSize(0, 0);
				packagesPanel.setSize(0,0);
				trianglePanel.setSize(0,0);
				movePanel.setBounds(0,0,0,0);
				field.setSize(0, 0);
				instructionsScrollPane.setBounds(0,modePanelHeight,getWidth(), getHeight()- modePanelHeight-20);
				try{
					classesPane.doLayout();
					instructionsScrollPane.doLayout();
				}
				catch (Exception e){
				}
			}
		}
	}
	/*.................................................................................................................*/
	public void windowResized(){
		resetSizes();
	}
}
class MovePanel extends MousePanel {
	SimplifyControlWindow window;
	int touchY = -1;
	public MovePanel(SimplifyControlWindow window){
		setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
		this.window = window;
		setBackground(Color.darkGray);
		setBackground(Color.blue);
	}
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		touchY = y;
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		int movedY = y - touchY;
		window.smallInstructionsHeight -= movedY;
		window.resetSizes();
		touchY = -1;
	}
}
/* ======================================================================== */
class ClassesPane extends MQScrollPane{

	public ClassesPane () {
		super(ScrollPane.SCROLLBARS_AS_NEEDED);
		Adjustable v = getVAdjustable();
		if (v != null)
			v.setUnitIncrement(20);
	}
	public void addPanel(Component c){
		addImpl(c, null, 0);
	}
}

class TrianglePanel extends MousePanel {
	Image triangleRight, triangleDown;
	PackagesPanel packagesPanel;
	public TrianglePanel(PackagesPanel pp){
		packagesPanel = pp;
		triangleRight = MesquiteImage.getImage(MesquiteTrunk.getRootImageDirectoryPath() + "triangleRightOn.gif");
		triangleDown = MesquiteImage.getImage(MesquiteTrunk.getRootImageDirectoryPath() + "triangleDownOn.gif");
		setBackground(ColorTheme.getInterfaceBackgroundPale());
	}
	public void paint(Graphics g){
		if (packagesPanel.v == null)
			return;
		g.setColor(Color.white);
		g.fillRect(16, 0, getWidth(), getHeight());
		g.setColor(Color.black);
		for (int i=0; i<packagesPanel.v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)packagesPanel.v[i];
			if (cb.isPackage){
				if (cb.collapsed)
					g.drawImage(triangleRight, 0, cb.getY()+6, this);
				else 
					g.drawImage(triangleDown, 0, cb.getY()+6, this);
			}
		}
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		packagesPanel.shown = packagesPanel.getCheckbox(y);
		if (packagesPanel.shown != null){
			if (packagesPanel.shown.late)
				packagesPanel.explanation.setBounds(4,packagesPanel.getY(packagesPanel.shown)-80,340,40);
			else 
				packagesPanel.explanation.setBounds(4,packagesPanel.getY(packagesPanel.shown)+ packagesPanel.shown.getHeight(),340,40);
			packagesPanel.explanation.setText(packagesPanel.shown.name +": " + packagesPanel.shown.expl);
			packagesPanel.explanation.setVisible(true);
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		packagesPanel.explanation.setVisible(false);
		packagesPanel.shown = null;
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		PackageCheckbox nowshown = packagesPanel.getCheckbox(y);
		if (nowshown != packagesPanel.shown){
			packagesPanel.shown = nowshown;
			if (packagesPanel.shown != null){
				if (packagesPanel.shown.late)
					packagesPanel.explanation.setBounds(4,packagesPanel.getY(packagesPanel.shown)-80,340,40);
				else 
					packagesPanel.explanation.setBounds(4,packagesPanel.getY(packagesPanel.shown)+ packagesPanel.shown.getHeight(),340,40);
				packagesPanel.explanation.setText(packagesPanel.shown.name +": " + packagesPanel.shown.expl);
				packagesPanel.explanation.setVisible(true);
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool toolTouching) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (x<18){
			PackageCheckbox cb = packagesPanel.getCheckbox(y);
			if (cb != null && cb.isPackage){
				cb.collapsed = !cb.collapsed;
				packagesPanel.resetSizesInclParents();
				repaint();
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}
}

class OuterPackagesPanel extends MQPanel {
	PackagesPanel packagesPanel;
	public OuterPackagesPanel(PackagesPanel pp){
		packagesPanel = pp;
	}
	public Dimension getPreferredSize(){
		int h = packagesPanel.checkSizes();
		return new Dimension(packagesPanel.getWidth() + 18, h); 
	}
}
class PackagesPanel extends MousePanel implements ItemListener {
	Listable[] v;
	int h = 30;
	TextArea explanation = new MQTextArea("", 20, 20, TextArea.SCROLLBARS_NONE);
	Font fontBig = new Font("SansSerif", Font.BOLD, 14);
	SimplifyControlWindow w;
	public PackagesPanel(SimplifyControlWindow w){
		super();
		this.w = w;
		add(explanation);
		explanation.setBounds(0,0,0,0);
		explanation.setVisible(false);
		explanation.setBackground(ColorTheme.getActiveLight());
		setBackground(Color.white);
		setLayout(null);
	}
	PackageCheckbox shown = null;
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		shown = getCheckbox(y);
		mouseEnteredCheckbox(shown);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		explanation.setVisible(false);
		shown = null;
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseEnteredCheckbox(PackageCheckbox shown) {
		if (shown != null){
			this.shown = shown;
			if (shown.late)
				explanation.setBounds(4,getY(shown)-80,340,40);
			else 
				explanation.setBounds(4,getY(shown)+ shown.getHeight(),340,40);
			explanation.setText(shown.name +": " + shown.expl);
			explanation.setVisible(true);
		}
	}
	public void mouseExitedCheckbox(PackageCheckbox shown) {
		explanation.setVisible(false);
		shown = null;
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		PackageCheckbox nowshown = getCheckbox(y);
		if (nowshown != shown){
			shown = nowshown;
			if (shown != null){
				if (shown.late)
					explanation.setBounds(4,getY(shown)-80,340,40);
				else 
					explanation.setBounds(4,getY(shown)+ shown.getHeight(),340,40);
				explanation.setText(shown.name +": " + shown.expl);
				explanation.setVisible(true);
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	
	public void itemStateChanged(ItemEvent e){
		if (	checkingStates)
			return;
		PackageCheckbox cb = (PackageCheckbox)e.getItemSelectable();
		if (cb.isSelected())
			InterfaceManager.removePackageFromHidden(cb.pkg, true);
		else
			InterfaceManager.addPackageToHidden(cb.pkg, true);
		checkStates();
		if (InterfaceManager.isEditingMode() || InterfaceManager.isSimpleMode()){
			MesquiteTrunk.resetAllMenuBars();
			MesquiteTrunk.resetAllToolPalettes();
		}
	}
	
	/*.................................................................................................................*/
	void addPackages(Vector allPackages){

		v = new Listable[allPackages.size()];
		for (int i=0; i< allPackages.size(); i++){
			ObjectContainer ms = (ObjectContainer)allPackages.elementAt(i);
			String name = ms.getName();
			String[] s = (String[])ms.getObject();

			PackageCheckbox cb = new PackageCheckbox(this, name,  s[0], s[1],  "true".equals(s[2]), "true".equals(s[3]));
			if (allPackages.size()-i<4)
				cb.setLate(true);
			cb.setSelected(!InterfaceManager.onHiddenClassListExactly(s[0]));
			if (cb.isSelected() && InterfaceManager.onHiddenClassList(s[0]))
				cb.setEnabled(false);
			cb.addItemListener(this);
			v[i] = cb;
			cb.setVisible(true);
		}

		ListableVector.sort(v);

		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			add(cb);
			cb.setVisible(true);
		}
	}
	 public void resetSizesInclParents(){
		 checkSizes();
		 w.resetSizes();
		w.field.invalidate();
		w.field.doLayout();
	}
	
	int getH(){
		return 50 + deepest;
	}
	public Dimension getPreferredSize(){
		int h = checkSizes();
		return new Dimension(getWidth(), h); 
	}
	int deepest = 0;
	int checkSizes(){
		if (v == null)
			return 0;
		boolean on = true;
		int count = 0;
		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			if (cb.isPackage){
				on = !cb.collapsed;
				cb.setBounds(0,count++*h, getWidth(), h);
			}
			else if (!on){
				cb.setVisible(false);
			}
			else {
				cb.setVisible(true);
				cb.setBounds(20, count++*h, getWidth(), h);
			}
		}
		deepest = count*h;
		return deepest;
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
					return (count-1)*h;
			}
			else if (on) {
				int k = count++;
				if (box == v[i] && box.isVisible())
					return (count-1)*h;
			}
		}
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
		return null;
	}
	boolean checkingStates = false;
	void checkStates(){
		if (v == null)
			return;
		boolean changed = false;
		for (int i=0; i<v.length; i++){
			PackageCheckbox cb = (PackageCheckbox)v[i];
			boolean was = cb.isSelected();
			checkingStates = true;
			cb.setSelected(!InterfaceManager.onHiddenClassListExactly(cb.pkg));
			checkingStates = false;
			if (was != cb.isSelected())
				changed = true;
			if (cb.isSelected() && InterfaceManager.onHiddenClassList(cb.pkg))
				cb.setEnabled(false);
			else if (!cb.hideable)
				cb.setEnabled(false);
			else
				cb.setEnabled(true);
		}
		
	if (changed)
		getParent().doLayout();
		repaint();
	}
	public void setSize( int w, int h){
		super.setSize(w, h);
		checkSizes();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		checkSizes();
	}
}

/*.................................................................................................................*/
//Includes workaround for Linux graphics StackOverflowError [Search for MQLINUX]
class PackageCheckbox extends JCheckBox implements MQComponent, Listable, MouseListener {
	String pkg = null;
	String expl = null;
	String name = null;
	boolean late = false;
	boolean isPackage = false;
	boolean hideable = true;
	boolean collapsed = true;
	PackagesPanel packagesPanel;
	public PackageCheckbox(PackagesPanel pp, String name, String pkg, String explanation, boolean isHideable, boolean isPackage){
		super(name);
		setBackground(Color.white);
		this.packagesPanel = pp;
		this.name = name;
		this.pkg = pkg;
		this.expl = explanation;
		this.isPackage = isPackage;
		this.hideable = isHideable;
		addMouseListener(this);
		setEnabled(hideable);
	}
	public String getName(){
		return pkg;
	}
	void setLate(boolean late){
		this.late = late;
	}
	public void mouseClicked(MouseEvent e){
	}
	public void mouseEntered(MouseEvent e){
		packagesPanel.mouseEnteredCheckbox(this);
	}
	public void mouseExited(MouseEvent e){
		packagesPanel.mouseExitedCheckbox(this);
	}
	public void mousePressed(MouseEvent e){
	}
	public void mouseReleased(MouseEvent e){
	}
	//###########################################################
	/*################################################################
	 *  The following overrides were built to avoid the frequent StackOverflowErrors on Linux Java post-1.8, 
	 *  but were extended in part to other OSs. See also others satisfying MQComponent interface.
	 */		
	MQComponentHelper helper = new MQComponentHelper(this);
	public MQComponentHelper getHelper(){
		return helper;
	}
	public void superValidate(){
		super.validate();
	}
	public void superSetBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
	}
	public void superSetFont (Font f){
	super.setFont(f);
	}
	public void superSetSize (int w, int h){
		super.setSize(w,h);
	}
	public void superSetLocation (int x, int y){
		super.setLocation(x,y);
	}
	public Dimension superGetPreferredSize(){
		return super.getPreferredSize();
	}
	public void superLayout(){
		super.layout();
	}
	public void superInvalidate(){
		super.invalidate();
	}
	/* - - - - - - */
	public void invalidate (){
		if (helper == null)
			superInvalidate();
		else
			helper.invalidate();
	}

	public void setFont (Font f){
		if (helper == null)
			superSetFont(f);
		else
			helper.setFont(f);
	}
	public void setSize (int w, int h){
		if (helper == null)
			superSetSize(w,h);
		else
			helper.setSize(w, h);
	}
	public void setLocation (int x, int y){
		if (helper == null)
			superSetLocation(x, y);
		else
			helper.setLocation(x,y);
	}
	public Dimension getPreferredSize() {
		if (helper == null)
			return superGetPreferredSize();
		else
			return helper.getPreferredSize();
	}
	public void layout(){
		if (helper == null)
			superLayout();
		else
			helper.layout();
	}
	public void validate(){
		if (helper == null)
			superValidate();
		else
			helper.validate();
	}
	public void setBounds(int x, int y, int w, int h){
		if (helper == null)
			superSetBounds(x,y,w,h);
		else
			helper.setBounds(x,y,w,h);
	}
	/*###########################################################*/
	//###########################################################

}
/*.................................................................................................................*/
class SaveRenameDeleteButton extends LoadSaveDeleteButton {
	public SaveRenameDeleteButton(){
		super("Save/Rename/Delete");
		setBackground(ColorTheme.getExtInterfaceBackground());
		setForeground(ColorTheme.getExtInterfaceTextContrast());
		//ColorTheme.getExtInterfaceElement());
	}
	void redoMenu() {// don't use this if can't save because don't have permissions
		if (popup==null)
			popup = new MesquitePopup(this);
		popup.removeAll(); 
		popup.add(new MesquiteMenuItem("Save Current...", null, new MesquiteCommand("saveCurrent", InterfaceManager.simplicityModule), null));
		MesquiteSubmenu ms = new MesquiteSubmenu("Rename...", popup, null);
		InterfaceManager.addSettingsMenuItems(ms, "rename", false);
		popup.add(ms);
		MesquiteSubmenu ms2 = new MesquiteSubmenu("Delete...", popup, null);
		InterfaceManager.addSettingsMenuItems(ms2, "delete", false);
		popup.add(ms2);

		add(popup);
	}
	public void resetSize(){
		if (!InterfaceManager.isEditingMode() || !InterfaceManager.settingsWritable()){
			setSize(0,0);
			return;
		}
		super.resetSize();
	}
}
/*.................................................................................................................*/
class LoadButton extends LoadSaveDeleteButton {
	public LoadButton(){
		super("Load Simplification");
		setForeground(Color.darkGray);
	}
	void redoMenu() {
		if (popup==null)
			popup = new MesquitePopup(this);
		popup.removeAll();
		InterfaceManager.addSettingsMenuItems(popup, "load", true);
		add(popup);
	}
}
/*.................................................................................................................*/
abstract class LoadSaveDeleteButton extends MousePanel {
	MesquitePopup popup;
	Polygon dropDownTriangle;
	String label;
	public LoadSaveDeleteButton(String label){
		dropDownTriangle=MesquitePopup.getDropDownTriangle();
		this.label = label;
		Font fontBig = new Font("SansSerif", Font.BOLD, 10);
		setFont(fontBig);
		resetSize();
	}
	public void resetSize(){
		Graphics g = getGraphics();
		if (g == null)
			return;
		Font font = g.getFont();
		FontMetrics fontMet = g.getFontMetrics(font);
		int w = fontMet.stringWidth(label)+17;
		int h = fontMet.getAscent() +fontMet.getDescent()+6;
		setSize(w, h);
	}
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		Font font = g.getFont();
		FontMetrics fontMet = g.getFontMetrics(font);
		int hA = fontMet.getAscent();
		int w = fontMet.stringWidth(label)+16;
		int h = hA +fontMet.getDescent()+4;
		int y =16-hA-2;
		w = getWidth() - 1;
		h = getHeight() - 1;
		y = 0;
		g.setColor(getForeground());
		g.drawString(label, 4, hA+2);
		g.drawLine(4, hA+6, w-8, hA+6);

		dropDownTriangle.translate(w - 10 ,8);
		g.setColor(Color.white);
		g.drawPolygon(dropDownTriangle);
		g.setColor(getForeground());
		g.fillPolygon(dropDownTriangle);
		dropDownTriangle.translate(-w + 10 ,-8);

		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		redoMenu();
		popup.show(this, 0,20);
		MesquiteWindow.uncheckDoomed(this);
	}
	abstract void redoMenu() ;
}

/*.................................................................................................................*/
class EditModeButton extends Checkbox implements ItemListener {
	MesquiteCommand command;
	Image editing;

	public EditModeButton(){
		super("Editing Mode");
		editing = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "notesTool.gif");  
		setState(false);
		setBackground(ColorTheme.getExtInterfaceElement());
		setForeground(ColorTheme.getExtInterfaceTextContrast()); //ColorTheme.getExtInterfaceElement());
		addItemListener(this);
	}

	void resetColors(){
		if (InterfaceManager.isEditingMode()){
			setBackground(ColorTheme.getExtInterfaceBackground());
			setForeground(ColorTheme.getExtInterfaceTextContrast());
		}
		else {
			setBackground(ColorTheme.getExtInterfaceElement());
			setForeground(ColorTheme.getExtInterfaceTextMedium());
		}
	}
	public void itemStateChanged(ItemEvent e){
		if (InterfaceManager.isEditingMode() == getState())
			return;
		InterfaceManager.setEditingMode(getState());
		resetColors();
		repaint();
		MesquiteModule.resetAllMenuBars();
		MesquiteModule.resetAllToolPalettes();
		MesquiteWindow.resetAllSimplicity();
	}

	
	
}

/*.................................................................................................................*/
class ModePanel extends MQPanel implements ItemListener {
	CheckboxGroup cbg;
	Checkbox powerCB, simplerCB;
	SimplifyControlWindow w;
	Image power, simple;
	MesquitePopup popup;
	LoadButton loadButton;
	SaveRenameDeleteButton saveRenameDeleteButton;
	EditModeButton editModeButton;
	int cbwidth = 160;
	Font fontBig = new Font("SansSerif", Font.BOLD, 12);
	Font fontBig14 = new Font("SansSerif", Font.BOLD, 14);
	int top = 24;
	public ModePanel(SimplifyControlWindow w){
		super();
		this.w = w;
		cbg = new CheckboxGroup();
		powerCB = new Checkbox("Full Interface", cbg, true);
		simplerCB = new Checkbox("Simple Interface", cbg, true);
		cbg.setSelectedCheckbox(powerCB);
		setLayout(null);
		add(powerCB);
		add(simplerCB);
		powerCB.setBounds(22, top + 2, cbwidth, 16);
		powerCB.setFont(fontBig14);
		powerCB.setVisible(true);
		simplerCB.setBounds(22, top + 22, cbwidth, 16);
		simplerCB.setVisible(true);
		simplerCB.setFont(fontBig14);
		powerCB.addItemListener(this);
		simplerCB.addItemListener(this);
		power = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "simplification/power.gif");  
		simple = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "simplification/simple.gif");  

		loadButton = new LoadButton();
		loadButton.setFont(fontBig);
		loadButton.setVisible(true);
		add(loadButton);

		saveRenameDeleteButton = new SaveRenameDeleteButton();
		saveRenameDeleteButton.setFont(fontBig);
		saveRenameDeleteButton.setVisible(true);
		add(saveRenameDeleteButton);

		editModeButton = new EditModeButton();
		resizeButtons();
		editModeButton.setFont(fontBig);
		editModeButton.setVisible(true);
		add(editModeButton);
		editModeButton.resetColors();

	}

	/*.................................................................................................................*/
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.setFont(fontBig);
		FontMetrics fontMet = g.getFontMetrics(fontBig);
		int hA = fontMet.getAscent();
		String title = "Simplification Control Panel";
		int w = fontMet.stringWidth(title)+16;
		g.drawString(title, (getWidth() - w)/2, 16);

		g.drawLine(16, top-2, getWidth()-16, top-2);
		Composite composite = ColorDistribution.getComposite(g);
		if (InterfaceManager.isSimpleMode())
			ColorDistribution.setTransparentGraphics(g,0.1f);
		if (power != null)
			g.drawImage(power, 4, top  + 2, this);

		ColorDistribution.setComposite(g,composite);

		if (!InterfaceManager.isSimpleMode())
			ColorDistribution.setTransparentGraphics(g,0.1f);
		if (simple != null)
			g.drawImage(simple, 4, top  + 22, this);

		ColorDistribution.setComposite(g,composite);
		g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
		if (InterfaceManager.isEditingMode()){
			g.setColor(Color.cyan);
			if (InterfaceManager.settingsWritable())
				g.fillRoundRect(getWidth()-editModeLeft-10, editModeTop + 4, editModeWidth +24, 58, 8, 8);
			else
				g.fillRoundRect(getWidth()-editModeLeft-10, editModeTop + 4, editModeWidth +24, 32, 8, 8);
		}
		if (editModeButton != null)
			g.setColor(editModeButton.getBackground());
		else
			g.setColor(ColorTheme.getExtInterfaceElement());
		if (InterfaceManager.isEditingMode() && InterfaceManager.settingsWritable()){
			g.fillRoundRect(getWidth()-editModeLeft-8, editModeTop + 6, editModeWidth +20, 54, 8, 8);
		}
		else {
			g.fillRoundRect(getWidth()-editModeLeft-8, editModeTop + 6, editModeWidth +20, 28, 8, 8);
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	int editModeTop = top+4;
	int editModeLeft = 170;
	int editModeWidth = 140;

	void resizeButtons(){
		if (loadButton != null){
			loadButton.resetSize();
			saveRenameDeleteButton.resetSize();
			loadButton.setLocation(50, top + 40);
			editModeButton.setLocation(getWidth()-editModeLeft, editModeTop + 8);
			editModeButton.setSize(140, 24);
			saveRenameDeleteButton.setLocation(getWidth()-editModeLeft, editModeTop + 36);
			editModeButton.resetColors();
		}
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		resizeButtons();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		resizeButtons();
	}
	public void resetStates(){

		if (!InterfaceManager.isSimpleMode())
			powerCB.setState(true);
		else if (InterfaceManager.isSimpleMode())
			simplerCB.setState(true);
		powerCB.setEnabled(!InterfaceManager.isEditingMode());
		simplerCB.setEnabled(!InterfaceManager.isEditingMode());
		repaint();
		editModeButton.repaint();
	}
	
	public void itemStateChanged(ItemEvent e){
		MesquiteCommand com;
		if (powerCB.getState())
			 com = MesquiteModule.makeCommand("full", w.ownerModule);
		else
			 com = MesquiteModule.makeCommand("simple", w.ownerModule);
		com.doItMainThread(null, null, this);
		repaint();
		
	}
}
class ClassHeadersPanel extends MQPanel  {
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



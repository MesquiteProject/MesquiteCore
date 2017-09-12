/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modified May 02 especially for annotations*/
package mesquite.lib;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;

import mesquite.lib.duties.*;
import mesquite.lib.simplicity.InterfaceManager;
import mesquite.lib.simplicity.SimplicityStrip;
/* ======================================================================== */
/** A stand alone window ("Frame").  The modules do not add their components directly to this window, but rather
to special Containers within the window.  This is done to deal with the difficulties of sizing, because of insets in non-MacOS systems,
given Mesquite's tendency to use null Layouts.  The first Component within the MesquiteWindow is the OuterContentsArea. It
contains the InfoBar and the InterContentArea.  The InterContentArea contains a series of ContentAreas.  One of these, graphics[0]
below, is the one to which a module should add any Components it needs to display (this is done by calling addToWindow()).  The
other ContentAreas are for textual information or Mesquite-controlled graphical information.*/
public abstract class MesquiteWindow implements Listable, Commandable, OwnedByModule, FileDirtier, Logger {
	LeakFinder leakFinder = MesquiteTrunk.leakFinderObject;
	//	static Vector cd;
	public MesquiteModule ownerModule;
	public MesquiteMenuBar menuBar;
	public static Vector classesCreated, countsOfClasses, countsOfClassesFinalized; //to detect memory leaks

	public boolean resetMenuPending = false;
	public int painting = 0;
	public boolean disposing = false;
	public static boolean autoShow = true;
	static int highestRank =0;
	static int lowestRank =0;
	int rank = -1;
	private Object currentObject = null;
	boolean minimalMenus = false;
	public static boolean headless = false;  //HEADLESS : set this to true for headless mode
	public static boolean suppressAllWindows = headless; //for command line use; pass argument -w
	public static boolean GUIavailable = !headless;
	public static boolean Java2Davailable = true;
	protected MesquiteCommand showCommand, showInfoBarCommand, saveAsTextCommand, printCommand, printToFitCommand;
	protected MesquiteCommand setFontCommand, setFontSizeCommand, listEmployeesCommand, doMacroCommand, showExplanationsCommand;
	protected MesquiteCommand showSnapshotCommand, sendScriptCommand, showFileCommand, closeWindowCommand, tileOutWindowCommand, popOutWindowCommand;
	protected MesquiteCommand printToPDFCommand;
	public static boolean pdfOutputAvailable = true; 
	boolean readyToPaint = true;
	protected Font currentFont;
	public static Font defaultFont;

	private MesquiteTool currentTool;
	private MesquiteTool previousTool = null;
	private ToolPalette palette;
	private ExplanationArea explanationArea, annotationArea;

	private Annotatable defaultAnnotatable;
	public int tickled = 0;  //repaint control to deal with Jaguar bug; see surveyNewWindows in MesquiteThread

	//	protected UndoInstructions undoInstructions = null;
	protected Undoer[] undoer = null;
	protected MesquiteCommand undoCommand = null; 
	protected MesquiteCommand previousToolCommand = null; 

	private int setSizeDebugg = 0;
	private boolean wasDisposed = false;
	private int menuResets = 0;
	private static boolean reportMenuResets = false;  
	OuterContentArea outerContents;
	private InterContentArea interContents;
	private ContentArea[] graphics;
	protected boolean windowFinishedBuilding = false;
	protected TextContentArea[]  text;
	protected InfoBar infoBar;
	protected Cursor waitCursor, restoreCursor;
	public static final int infoBarHeightAllowance = 22;
	protected int infoBarHeight = infoBarHeightAllowance;
	boolean queryMode = false;
	protected MesquiteMenuItem cloneWindowMenuItem, saveRecipeMenuItem, snapshotMenuItem, sendScriptMenuItem, popOutWindowMenuItem, tileOutWindowMenuItem;
	protected MesquiteMenuItem closeWindowMenuItem, closeAllMenuItem;

	protected MesquiteMenuItemSpec closeWindowMenuItemSpec, closeAllMenuItemSpec,explanationsMenuItemSpec;
	private MesquiteMenuItemSpec ptfMMIS;
	private MesquiteMenuItemSpec pPDFMMIS;
	private MesquiteMenuItemSpec popOutWindowMSpec;
	private boolean showInfoBar = false;
	private StringBuffer logText;
	public static Frame dialogAnchor;
	public static MesquiteTimer startingTime, resetMenuTime, windowShowTime, windowTimer;
	public static int numDialogs=0;
	public static int numNonWizardDialogs=0;
	static int numWindowsTotal = 0;
	int id = 0;
	static MenuShortcut closeWindowShortcut;
	public static ClassVector componentsPainted; //for detecting efficiency problems
	MesquiteFrame parentFrame;
	public static boolean compactWindows = true;
	boolean closeable = false;
	public static long totalCreated = 0;
	public static long totalDisposed = 0;
	public static long totalFinalized  = 0;
	public static long numDisposing = 0;
	int tileLocation;
	static {

		startingTime = new MesquiteTimer();
		windowShowTime = new MesquiteTimer();
		resetMenuTime = new MesquiteTimer();
		windowTimer = new MesquiteTimer();

		dialogAnchor = new Frame();
		dialogAnchor.setBounds(-64, -64, 32,32);
		closeWindowShortcut = new MenuShortcut(KeyEvent.VK_W);
		defaultFont = new Font ("SanSerif", Font.PLAIN, 10); //added 5 Nov 01
		if (MesquiteTrunk.checkMemory){
			classesCreated = new Vector();
			countsOfClasses = new Vector();
			countsOfClassesFinalized = new Vector();

		}
	}
	/** a constructor not to be used except internally!!!  This is used for accumulating commands*/
	public MesquiteWindow () {
		id = numWindowsTotal++;
		if (MesquiteTrunk.checkMemory)
			countCreated();
	}
	/**the standard constructor for MesquiteWindows*/
	public MesquiteWindow (MesquiteModule ownerModule, boolean showInfoBar) {
		super();
		if (MesquiteTrunk.checkMemory)
			countCreated();
		tileLocation = getDefaultTileLocation();
		readyToPaint = false;
		totalCreated++;
		id = numWindowsTotal++;
		this.showInfoBar = showInfoBar;
		if ((compactWindows || this instanceof SystemWindow) && ownerModule != null){
			MesquiteProject proj = ownerModule.getProject();
			MesquiteFrame frame = null;
			if (proj == null){
				if (MesquiteTrunk.mesquiteTrunk.containerOfModule()!= null)
					frame = MesquiteTrunk.mesquiteTrunk.containerOfModule().getParentFrame();
			}
			else
				frame = proj.getFrame();
			if (frame!= null){
				parentFrame = frame;
			}
		}
		if (parentFrame == null){
			parentFrame = new MesquiteFrame(isCompactible(), ColorTheme.getInterfaceBackground());
			parentFrame.setOwnerModule(ownerModule);
			/*following done for OS 9 because of crashing bug in MRJ 2.2.5 if application menu touched; also may help other OS's to establish insets early?? */
			Menu fM = new MesquiteMenu("File");
			MenuBar mBar = new MenuBar();
			mBar.add(fM);
			parentFrame.setMenuBar(mBar);
		}

		menuBar = new MesquiteMenuBar(this);  
		setMenuBar(menuBar); 

		this.ownerModule = ownerModule;
		undoCommand = MesquiteModule.makeCommand("undo", this);
		previousToolCommand = MesquiteModule.makeCommand("setToPreviousTool", this);

		startingTime.start();

		if (!showInfoBar)
			infoBarHeight = 0;
		//setLayout(new CardLayout());
		outerContents = new OuterContentArea(this);

		parentFrame.addPage(this); 
		outerContents.setForeground(Color.black);
		outerContents.requestFocusInWindow(); //this may address a MRJ 2.2.3 bug

		waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		restoreCursor = Cursor.getDefaultCursor();

		infoBar = new InfoBar(this);
		outerContents.add(infoBar, "InfoBar"); //"InfoBar"
		outerContents.setInfoArea(infoBar);

		explanationArea = new ExplanationArea(this, false); 
		explanationArea.setName("EXPL");  //for debugging purposes, so explanation area knows what sort it is

		outerContents.setExplanationArea(explanationArea);
		outerContents.add(explanationArea, "ExplanationArea"); 
		explanationArea.setBackground(ColorTheme.getInterfaceBackground());
		annotationArea = new ExplanationArea(this, true);
		annotationArea.setName("ANOT");  //for debugging purposes, so explanation area knows what sort it is
		annotationArea.setBackground(Color.white);
		outerContents.setAnnotationArea(annotationArea);
		setAnnotation(null);
		//windowAnnotation.addListener(this);
		//defaultAnnotation = windowAnnotation;
		outerContents.add(annotationArea, "AnnotationArea"); 

		interContents = new InterContentArea();
		outerContents.add(interContents, "InterContents"); 
		graphics = new ContentArea[2];
		graphics[0] = new ContentArea(this, true);   //this is the main graphics page
		graphics[1] = new ETContentArea(ownerModule);   // this is the employee tree graphics page
		interContents.addPage(graphics[0], Integer.toString(InfoBar.GRAPHICS)); // primary graphics page
		interContents.addPage(graphics[1], Integer.toString(InfoBar.EMPLOYEE_TREE));
		text = new TextContentArea[InfoBar.numModes];  //text pages
		text[InfoBar.TEXT_CONTENTS] = new TextContentArea();
		interContents.addPage(text[InfoBar.TEXT_CONTENTS], Integer.toString(InfoBar.TEXT_CONTENTS)); //strings correspond to modes
		text[InfoBar.TEXT_PARAMETERS] = new TextContentArea();
		interContents.addPage(text[InfoBar.TEXT_PARAMETERS], Integer.toString(InfoBar.TEXT_PARAMETERS)); //strings correspond to modes
		text[InfoBar.TEXT_CITATIONS] = new TextContentArea();
		interContents.addPage(text[InfoBar.TEXT_CITATIONS], Integer.toString(InfoBar.TEXT_CITATIONS)); //strings correspond to modes

		outerContents.setContentsArea(graphics, text, interContents);
		if (infoBar != null)
			infoBar.setContentsArea(graphics, text, interContents);
		outerContents.setSize(0,0);
		interContents.showPage(InfoBar.GRAPHICS);
		//if (isMainWindowOfBranch && ownerModule!=null)
		//	ownerModule.window=this;
		currentFont = defaultFont;
		setWindowFont(currentFont);

		graphics[0].setBackground(ColorTheme.getInterfaceElement());
		interContents.setBackground(ColorTheme.getInterfaceElement());
		outerContents.setBackground(ColorTheme.getInterfaceElement());
		MesquiteTrunk.windowVector.addElement(this, true);
		saveAsTextCommand = MesquiteModule.makeCommand("saveTextToFile",  this);
		showCommand = MesquiteModule.makeCommand("showWindow",  this);
		printCommand = MesquiteModule.makeCommand("printWindow",  this);
		printToFitCommand = MesquiteModule.makeCommand("printToFit",  this);
		if (this instanceof Fittable)
			ptfMMIS = new MesquiteMenuItemSpec(null, "Print Window To Fit Page...", MesquiteModule.mesquiteTrunk, printToFitCommand);
		printToPDFCommand = MesquiteModule.makeCommand("printToPDF", this);
		if (pdfOutputAvailable)
			pPDFMMIS = new MesquiteMenuItemSpec(null,"Save Window As PDF...", MesquiteModule.mesquiteTrunk, printToPDFCommand);

		setFontCommand =MesquiteModule.makeCommand("setFont",  this);
		setFontSizeCommand =MesquiteModule.makeCommand("setFontSize",  this);
		listEmployeesCommand = MesquiteModule.makeCommand("listEmployees",  this);
		showInfoBarCommand = MesquiteModule.makeCommand("toggleInfoBar", this);
		doMacroCommand =MesquiteModule.makeCommand("doMacro", this);
		showFileCommand = MesquiteModule.makeCommand("showFile", ownerModule);
		closeWindowCommand = MesquiteModule.makeCommand("closeWindow", this);
		popOutWindowCommand = MesquiteModule.makeCommand("togglePopOutWindow", this);
		tileOutWindowCommand = MesquiteModule.makeCommand("toggleTileOutWindow", this);
		showExplanationsCommand =MesquiteModule.makeCommand("showExplanations", this);
		showSnapshotCommand = MesquiteModule.makeCommand("showSnapshot", this);
		sendScriptCommand = MesquiteModule.makeCommand("sendScript", this);
		/*	infoBarMenuItem = new MesquiteCheckMenuItem(new MesquiteCMenuItemSpec(null,"Show Information Bar", getOwnerModule(), showInfoBarCommand, null));
		infoBarMenuItem.set(showInfoBar);
		infoBarMenuItem.disconnectable = false;*/
		explanationsMenuItemSpec = new MesquiteMenuItemSpec(null,"Menu & Control Explanations", getOwnerModule(), showExplanationsCommand);  
		cloneWindowMenuItem = new MesquiteMenuItem(new MesquiteMenuItemSpec(null, "Clone Window", getOwnerModule(), MesquiteModule.makeCommand("cloneWindow", this)));
		cloneWindowMenuItem.disconnectable = false;
		popOutWindowMSpec = new MesquiteMenuItemSpec(null, "Pop Out as Separate Window", getOwnerModule(), popOutWindowCommand);
		popOutWindowMenuItem = new MesquiteMenuItem(new MesquiteMenuItemSpec(null, "Pop Out as Separate Window", getOwnerModule(), popOutWindowCommand));
		popOutWindowMenuItem.disconnectable = false;
		tileOutWindowMenuItem = new MesquiteMenuItem(new MesquiteMenuItemSpec(null, "Put in Separate Tile", getOwnerModule(), tileOutWindowCommand));
		tileOutWindowMenuItem.disconnectable = false;
		saveRecipeMenuItem = new MesquiteMenuItem(new MesquiteMenuItemSpec(null, "Save Window as Macro...", getOwnerModule(), MesquiteModule.makeCommand("saveMacroForWindow", this)));
		saveRecipeMenuItem.disconnectable = false;
		snapshotMenuItem = new MesquiteMenuItem(new MesquiteMenuItemSpec(null,"Show Snapshot", getOwnerModule(), showSnapshotCommand));
		snapshotMenuItem.disconnectable = false;
		sendScriptMenuItem = new MesquiteMenuItem(new MesquiteMenuItemSpec(null,"Send Script", getOwnerModule(), sendScriptCommand));
		sendScriptMenuItem.disconnectable = false;
		if (getOwnerModule() instanceof FileCoordinator)
			closeWindowMenuItemSpec = new MesquiteMenuItemSpec(null, "Close Window", getOwnerModule(), closeWindowCommand);
		else
			closeWindowMenuItemSpec = new MesquiteMenuItemSpec(null, "Close Tab", getOwnerModule(), closeWindowCommand);
		MesquiteProject proj = ownerModule.getProject();
		if (proj != null)
			closeAllMenuItemSpec = new MesquiteMenuItemSpec(null, "Close All Tabs Of Project", getOwnerModule(), new MesquiteCommand("closeAllWindows", getOwnerModule().getFileCoordinator()));


		parentFrame.setWindowLocation(60, 10, false, false); //default window position
		addKeyListenerToAll(graphics[0], palette, true);
		outerContents.requestFocusInWindow(); //this may address a MRJ 2.2.3 bug
		startingTime.end();
		closeable = true;
		windowFinishedBuilding = true;
	}

	public void setPopTileMenuItemNames(){
		popOutWindowMenuItem.setLabel("Pop Out as Separate Window");
		tileOutWindowMenuItem.setLabel("Put in Separate Tile");
		if (isPoppedOut()) {
			if (getPopAsTile()) {
				tileOutWindowMenuItem.setLabel("Return Tile to Main Window");
				if (closeWindowMenuItem != null)
					closeWindowMenuItem.setLabel("Close Tab");
				closeWindowMenuItemSpec.setName("Close Tab");
			}
			else {
				popOutWindowMenuItem.setLabel("Reset within Main Window");
				if (closeWindowMenuItem != null)
					closeWindowMenuItem.setLabel("Close Window");
				closeWindowMenuItemSpec.setName("Close Window");
			}
		} 
		else {
			if (closeWindowMenuItem != null)
				closeWindowMenuItem.setLabel("Close Tab");
			closeWindowMenuItemSpec.setName("Close Tab");

		}

	}

	void countCreated(){
		if (classesCreated.indexOf(getClass())<0) {
			classesCreated.addElement(getClass());
			countsOfClasses.addElement(new MesquiteInteger(1));
			countsOfClassesFinalized.addElement(new MesquiteInteger(0));
		}
		else {
			MesquiteInteger c = (MesquiteInteger)countsOfClasses.elementAt(classesCreated.indexOf(getClass()));
			if (c!=null)
				c.increment();
		}
	}
	public void startWindowTimer(){
		if (windowTimer!=null)
			windowTimer.start();
	}
	public MesquiteTimer getWindowTimer(){
		return windowTimer;
	}
	public void resetTime(){
		if (windowTimer!=null)
			windowTimer.resetLastTime();
	}
	public void logTime(String message){
		if (windowTimer!=null)
			if (!StringUtil.blank(message))
				MesquiteMessage.println(message + "  " + windowTimer.timeSinceLast());
			else
				MesquiteMessage.println("  " + windowTimer.timeSinceLast());
	}

	public boolean isCompactible(){
		return compactWindows;
	}

	public boolean permitViewMode(){
		return true;
	}
	public int getTileLocation(){
		return tileLocation;
	}
	public void setTileLocation(int loc){
		this.tileLocation = loc;
	}
	boolean minimized = false;
	public boolean isMinimized(){  //for Resources window only as of 08
		return minimized;
	}
	public void setMinimized(boolean min){
		this.minimized = min;
	}
	public int getDefaultTileLocation(){
		return MesquiteFrame.MAIN;
	}
	boolean poppedOut = false;
	public boolean isPoppedOut(){
		return poppedOut;
	}
	boolean popAsTile = false;
	public boolean getPopAsTile(){
		return popAsTile;
	}
	public void setPopAsTile(boolean popAsTile){
		this.popAsTile = popAsTile;
		ownerModule.resetEmbeddedMenus(this);
	}

	public void popOut(boolean setVisible){
		if (!isLoneWindow() && parentFrame!=null){
			if (!parentFrame.anythingPoppedOtherThanMe(this) && MesquiteInteger.isCombinable(preferredPopoutWidth) && preferredPopoutWidth>0)
				parentFrame.setPopoutWidth(preferredPopoutWidth);
			parentFrame.popOut(this, setVisible);
			ownerModule.resetEmbeddedMenus(this);
		}
	}

	int preferredPopoutWidth = MesquiteInteger.unassigned;
	public void setPreferredPopoutWidth(int preferredWidth){
		preferredPopoutWidth = preferredWidth;
	}

	public void popIn(){
		if (isLoneWindow()){
			try {
				getOwnerModule().getFileCoordinator().getModuleWindow().getParentFrame().popIn(this);
			}
			catch(Exception e){
			}
		}
		else if ((getPopAsTile()) && parentFrame!=null){
			parentFrame.popIn(this);
		}
		ownerModule.resetEmbeddedMenus(this);
	}

	public boolean isLoneWindow(){
		return (isPoppedOut() && !popAsTile) || (parentFrame == null && parentFrame.getNumWindows()<=1);
	}
	public boolean isCompacted(){
		return (compactWindows || this instanceof SystemWindow) && !poppedOut;
	}
	public int getID(){
		return id;
	}
	protected void addWindowListener(WindowListener listener){
		parentFrame.addWindowListener(listener);
	}
	public MesquiteFrame getParentFrame(){
		return parentFrame;
	}
	public void setParentFrame(MesquiteFrame f){
		parentFrame = f;
		if (parentFrame != null)
			parentFrame.setMenuBar(this, menuBar);
	}
	public boolean isFrontMostInLocation(){
		return parentFrame.frontMostInLocation(getTileLocation()) == this;
	}
	public void setAsPrimaryMesquiteWindow(boolean p){
		if (parentFrame != null)
			parentFrame.setAsPrimaryMesquiteFrame(p);
	}
	boolean orphan = false;
	public void removedFromParent(){
		orphan = true;
	}
	public void inParent(){
		orphan = false;
	}
	public OuterContentArea getOuterContentsArea(){
		return outerContents;
	}
	/*.................................................................................................................*/
	public MesquiteModule getOwnerModule() {
		return ownerModule;
	}
	/*.................................................................................................................*/
	/** For FileDirtier interface */
	public void fileDirtiedByCommand(MesquiteCommand command){
		if (ownerModule!=null && ownerModule.getProject()!=null && ownerModule.getProject().getHomeFile()!=null)
			ownerModule.getProject().getHomeFile().setDirtiedByCommand(true);
	}
	public boolean setAsFrontOnClickAnyContent(){
		return true;
	}
	/*.................................................................................................................*/
	public int getColorScheme(){
		return ColorDistribution.getColorScheme(ownerModule);
	}
	/*.................................................................................................................*/
	public MesquiteCommand getPreviousToolCommand() {
		return previousToolCommand; 
	}
	/*.................................................................................................................*/
	public MesquiteCommand getUndoCommand() {
		return undoCommand; 
	}
	public void setUndoMenuItemEnabled(boolean b) {
		if (ownerModule!=null)
			ownerModule.setUndoMenuItemEnabled(b);
	}
	public Undoer[] getUndoer () {
		return undoer;
	}
	public void setNewUndoState (Object newState) {
		if (undoer!=null && undoer[0]!=null)
			undoer[0].setNewState(newState);
	}
	public void setUndoer (Undoer[] undoer) {
		if (this.undoer!=null) {
			for (int i=0; i<this.undoer.length; i++)
				if (this.undoer[i]!=null)
					this.undoer[i].dispose();
		}
		this.undoer = undoer;
		checkUndoMenuStatus();
	}
	public void setUndoer (Undoer undoer) {
		if (this.undoer!=null) {
			for (int i=0; i<this.undoer.length; i++)
				if (this.undoer[i]!=null)
					this.undoer[i].dispose();
		}
		this.undoer = new Undoer[] {undoer};
		checkUndoMenuStatus();
	}
	public void setUndoer (UndoReference undoReference) {
		if (undoReference!=null)
			setUndoer(undoReference.getUndoer());
		else
			setUndoer();
	}
	public void setUndoer () {
		setUndoer((Undoer[]) null);
		checkUndoMenuStatus();
	}
	public void checkUndoMenuStatus () {
		//resetMenus();
		setUndoMenuItemEnabled(undoer!=null);
	}
	/*.................................................................................................................*/
	/** Shows information in response to query mode*/
	public static void respondToQueryMode(String item, MesquiteCommand command, Object widget){
		if (command!=null && widget !=null) {
			MesquiteWindow w = null;
			if (widget instanceof Component)
				w = windowOfItem((Component)widget);
			else if (widget instanceof MenuComponent)
				w = windowOfItem((MenuComponent)widget);
			if (w==null)
				return;
			MesquiteTrunk.mesquiteTrunk.showLogWindow(false);
			String s = CommandChecker.getQueryModeString(item, command, widget);
			w.setExplanation(s);
		}
	}
	/*.................................................................................................................*/
	/** Returns where the MesquiteWindow containing the component is in queryMode.  Returns false if not contained in a MesquiteWindow.
	NOTE: this system was built in 2000 when the info bar had a ? button to turn on query mode.  Now (Jan 2001) that that's gone, and now that there is
	an explanation area at the bottom of windows, an alternative systems were developed for explaining items on the fly (including control-select for menu items).
	These query mode methods are left here in case it makes sense to resurrect them*/
	public static boolean getQueryMode(Component c){
		if (c==null)
			return false;
		if (c instanceof OuterContentArea)
			return ((OuterContentArea)c).ownerWindow.queryMode;

		Container cont = c.getParent();
		while (cont!= null && !(cont instanceof OuterContentArea))
			cont = cont.getParent();
		if (cont instanceof OuterContentArea)
			return ((OuterContentArea)cont).ownerWindow.queryMode;
		else 
			return false;
	}
	/*.................................................................................................................*/
	/** Returns where the MesquiteWindow containing the menu item is in queryMode.  Returns false if not contained in a MesquiteWindow.
	NOTE: this system was built in 2000 when the info bar had a ? button to turn on query mode.  Now (Jan 2001) that that's gone, and now that there is
	an explanation area at the bottom of windows, an alternative systems were developed for explaining items on the fly (including control-select for menu items).
	These query mode methods are left here in case it makes sense to resurrect them*/
	public static boolean getQueryMode(MenuComponent c){
		MesquiteWindow win = windowOfItem(c);
		if (win!=null)
			return win.queryMode;
		return false;
	}
	/*.................................................................................................................*/
	/** Returns the MesquiteWindow containing the menu item*/
	public static MesquiteWindow windowOfItem(MesquiteWindow c){
		return c;
	}
	/*.................................................................................................................*/
	/** Returns the MesquiteWindow containing the menu item*/
	public static MesquiteWindow windowOfItem(MenuComponent c){
		if (c==null)
			return null;
		if (c instanceof MenuBar)
			return getWindow((MenuBar)c);
		if (!(c instanceof MenuComponent))
			return null;
		MenuContainer cont = ((MenuComponent)c).getParent();
		while (cont!= null && !(cont instanceof MenuBar) && !(cont instanceof MesquitePopup)&& cont instanceof MenuComponent)
			cont =  ((MenuComponent)cont).getParent();
		if (cont instanceof MenuBar) {  
			return getWindow((MenuBar)cont);
		}
		else if (cont instanceof MesquitePopup){
			return windowOfItem(((MesquitePopup)cont).getComponent());
		}
		return null;
	}
	private static MesquiteWindow getWindow(MenuBar mb){
		if(mb==null)
			return null;

		Enumeration e = MesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw.getMenuBar()==mb)
				return mw;
		}
		return null;

	}
	/*.................................................................................................................*/
	/** Returns the MesquiteWindow containing the component.  Returns null if not contained in a MesquiteWindow*/
	public static MesquiteWindow windowOfItem(Component c){
		if (c==null)
			return null;
		if (c instanceof OuterContentArea)
			return ((OuterContentArea)c).ownerWindow;

		Container cont = c.getParent();
		while (cont!= null && !(cont instanceof OuterContentArea))
			cont = cont.getParent();
		if (cont instanceof OuterContentArea)
			return ((OuterContentArea)cont).ownerWindow;
		else 
			return null;
	}
	/*.................................................................................................................*/
	/** Returns the MesquiteWindow containing the component.  Returns null if not contained in a MesquiteWindow*/
	public static String getContext(Component c){
		if (c==null)
			return null;
		String s = "";

		Container cont = c.getParent();
		while (cont!= null){
			s += cont.getClass().getName() + " " + cont.getLayout() + "\n";			
			cont = cont.getParent();
		}
		return s;
	}
	/*.................................................................................................................*/
	/** Returns the MesquiteWindow containing the menu item*/
	public static boolean isReadyToPaint(MesquiteWindow c){
		return c.readyToPaint;
	}
	/*.................................................................................................................*/
	/** Returns the MesquiteWindow containing the component.  Returns null if not contained in a MesquiteWindow*/
	public static boolean isReadyToPaint(Component c){
		MesquiteWindow w = windowOfItem(c);
		if (w != null)
			return w.readyToPaint;
		return false;
	}
	/*.................................................................................................................*/
	/** Returns the Frame containing the component.  Returns null if not contained in a Frame*/
	public static Frame frameOfComponent(Frame c){
		return c;
	}
	/*.................................................................................................................*/
	/** Returns the Frame containing the component.  Returns null if not contained in a Frame*/
	public static Frame frameOfComponent(Component c){
		if (c==null)
			return null;
		if (c instanceof Frame)
			return (Frame)c;

		Container cont = c.getParent();
		while (cont!= null && !(cont instanceof Frame))
			cont = cont.getParent();
		if (cont instanceof Frame)
			return (Frame)cont;
		else 
			return null;
	}
	static boolean checkcheck = false;
	static long checkedCount = 0;
	static Component lastComponent = null;
	static boolean writeUnchecked = false;
	public static long totalCheckDoomedCount = 0;
	public static boolean checkDoomed(MesquiteWindow c){ 
		return checkDoomed(c.outerContents);
	}
	/*.................................................................................................................*/
	/** Returns whether the window belongs to a doomed module (one about to be closed).  To be called at the start of all
	paint methods to abort the paint.  At the same time, this method sets a flag in the window to say that painting is in progress.
	While this flag is set, the window will not be disposed.  The method uncheckDoomed(Component c) must be called at the end of the paint*/
	public static boolean checkDoomed(Component c){ 
		if (windowOfItem(c) == null)
			return false;
		totalCheckDoomedCount++;

		if (c!=null && componentsPainted!=null)
			componentsPainted.recordWithTime(c.getClass());
		if (checkcheck){
			if (c == lastComponent){
				checkedCount++;
				if (checkedCount % 100 == 0) {
					MesquiteFile.writeToLog("checkDoomed " + checkedCount + "  " + c + StringUtil.lineEnding());
					writeUnchecked = true;
				}
			}
			else {
				checkedCount =1;
				writeUnchecked = true;
			}
			lastComponent = c;
		}
		MesquiteWindow w = null;
		if (c instanceof OuterContentArea){//EMBEDDED : needs to be changed

			w = ((OuterContentArea)c).ownerWindow;
		}
		else {
			Container cont = c.getParent();
			while (cont!= null && !(cont instanceof OuterContentArea))
				cont = cont.getParent();
			if (cont instanceof OuterContentArea)
				w = ((OuterContentArea)cont).ownerWindow;
			else if (!(cont instanceof Frame))
				return true;
			else  {
				return false;
			}
		}
		if (w==null) {
			return false;
		}
		//		MesquiteFile.writeToLog("         checkDoomed window " + w + StringUtil.lineEnding());
		if (w.getOwnerModule()!=null && (w.getOwnerModule().isDoomed() || w.disposing)) {
			return true;
		}
		//		cd.addElement(c);
		w.painting++;
		return false;
	}
	public static void uncheckDoomed(MesquiteWindow c){ 
		uncheckDoomed(c.outerContents);
	}
	/*.................................................................................................................*/
	public static void uncheckDoomed(Component c){
		MesquiteWindow w = windowOfItem(c);
		if (c!=null && componentsPainted!=null)
			componentsPainted.stopTime(c.getClass());
		if (checkcheck && writeUnchecked && c == lastComponent) {
			MesquiteFile.writeToLog("         uncheckDoomed " + c + StringUtil.lineEnding());
		}
		if (MesquiteTrunk.isMacOSXJaguar())
			Toolkit.getDefaultToolkit().sync();
		if (w==null) 
			return;
		w.painting--;
	}

	/*--------------------------------PAINT/PRINT/SAVETEXT contents ----------------------------------*/
	public static void invalidateAll(Component c){
		if (c==null)
			return;
		c.invalidate();
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					invalidateAll(cc[i]);
		}

	}
	public void contentsChanged() {
		repaintAll();
		if (disposing)
			return;
		if (graphics==null || graphics[0]==null || graphics[1]==null)
			return;
		if (infoBar!=null&& infoBar.getMode()>0) 
			updateTextPage();
	}
	public void repaint() {
		//super.repaint();
		if (disposing)
			return;
		if (graphics==null || graphics[0]==null || graphics[1]==null)
			return;
		if (infoBar==null)
			graphics[0].repaint();
		else if (infoBar.getMode()==InfoBar.GRAPHICS)
			graphics[0].repaint();
		else if (infoBar.getMode()==InfoBar.EMPLOYEE_TREE)
			graphics[1].repaint();
		//	if (showInfoBar)
		infoBar.repaint();
	}
	/*.................................................................................................................*/
	public void repaintInfoBar() {
		if (infoBar != null){
			rpAll(infoBar);
		}
	}
	/*.................................................................................................................*/
	/** calls repaint of all components*/
	public void repaintAll() {
		if (infoBar != null){
			if (infoBar.getMode()==InfoBar.GRAPHICS) {
				rpAll(graphics[0]);
				rpAll(palette);
				rpAll(explanationArea);
				rpAll(annotationArea);
			}
			rpAll(infoBar);
			if (parentFrame != null && parentFrame.tabs != null)
				parentFrame.tabs.repaint();
		}
	}
	public static void rpAll(Component c){
		if (c==null)
			return;
		c.repaint();
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					rpAll(cc[i]);
		}

	}
	public void reportComponents(){
		reportComponents(outerContents, "");

	}
	public static void reportComponents(Component c, String spacer){
		if (c==null)
			return;
		spacer += "  ";
		MesquiteMessage.println(spacer + c);
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					reportComponents(cc[i], spacer);
		}

	}
	private Component focusWithin(Component c){
		if (c==null)
			return null;
		if (c.hasFocus())
			return c;
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++){
					Component q = focusWithin(cc[i]);
					if (q != null)
						return q;
				}
		}
		return null;

	}
	public Component componentWithFocus(){
		if (infoBar.getMode()==InfoBar.GRAPHICS) {
			Component cg = (focusWithin(graphics[0]));
			if (cg != null)
				return cg;
		}
		Component c = focusWithin(palette);
		if (c != null)
			return c;
		c = focusWithin(explanationArea);
		if (c != null)
			return c;
		c = focusWithin(annotationArea);
		if (c != null)
			return c;
		return null;
	}
	/*.................................................................................................................*/
	public static boolean belongsTo(Component possibleContent, Component possibleContainer){
		if (possibleContainer==possibleContent)
			return true;
		if (possibleContainer instanceof Container){
			Component[] cc = ((Container)possibleContainer).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++){
					if (belongsTo(possibleContent, cc[i]))
						return true;
				}
		}
		return false;

	}
	/*.................................................................................................................*/
	/** to be overridden by MesquiteWindows instead of paint to draw their contents*/
	public void paintContents(Graphics g) {
	}
	/*.................................................................................................................*/
	/** set the cursor to be the wait cursor.*/
	public void setGraphicsWaitCursor() {
		if (parentFrame == null)
			return;
		restoreCursor = parentFrame.getCursor();
		setGraphicsCursor(waitCursor);
	}
	/*...............................................................................................................*/
	/** set the cursor to be the default cursor.*/
	public void setGraphicsDefaultCursor() {
		if (parentFrame == null)
			return;
		restoreCursor = parentFrame.getCursor();
		setGraphicsCursor(Cursor.getDefaultCursor());
	}
	/*...............................................................................................................*/
	protected void setContentsCursor(Cursor c){
		if (graphics != null && graphics.length>0 && graphics[0] != null)
			graphics[0].setCursor(c);
	}
	/*...............................................................................................................*/
	/** set the cursor to be that passed.*/
	public void setGraphicsCursor(Cursor c) {
		if (parentFrame == null)
			return;
		restoreCursor = parentFrame.getCursor();
		if (c!=null)
			setContentsCursor(c);
	}
	/*...............................................................................................................*/
	/** restores the cursor to previous.*/
	public void restoreGraphicsCursor() {
		if (restoreCursor!=null)
			setContentsCursor(restoreCursor);
		restoreCursor = null;
	}
	/*...............................................................................................................*/
	/** restores the cursor of all windows to previous.*/
	public static void restoreAllCursors() {
		Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			MesquiteWindow win = (MesquiteWindow)e.nextElement();
			win.restoreGraphicsCursor();
		}
	}
	/*...............................................................................................................*/
	/** restores the cursor of all windows to wait cursor.*/
	public static void setAllWaitCursors() {
		Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			MesquiteWindow win = (MesquiteWindow)e.nextElement();
			win.setGraphicsWaitCursor();
		}
	}
	/*...............................................................................................................*/
	/** restores the cursor of all windows to previous.*/
	public static void repaintAllWindows() {
		Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			MesquiteWindow win = (MesquiteWindow)e.nextElement();
			win.repaintAll();
		}
	}
	/*.................................................................................................................*/
	/** to be called to update the current text page's contents*/
	public final void updateTextPage() {
		if (infoBar!=null)
			for (int i=1; i<InfoBar.numModes; i++)
				infoBar.refreshText(i);
	}
	/*.................................................................................................................*/
	/** Update the page showing the employee tree (to be called after employees added or
	subtracted)  */
	public void updateEmployeeTree(){
		if (graphics[1] instanceof ETContentArea) {
			HPanel b = ((ETContentArea)graphics[1]).getBrowser();
			if (b!=null)
				b.renew();
		}
	}
	/*.................................................................................................................*/
	public String searchData(String s, MesquiteString commandResult) {
		if (StringUtil.blank(s))
			return "<h2>Nothing to search for (searched: \"" + s + "\")</h2>";
		if (commandResult != null)
			commandResult.setValue((String)null);
		if (getOwnerModule() != null && getOwnerModule().getProject() != null){
			MesquiteProject proj = getOwnerModule().getProject();
			return proj.searchData(s, null);
		}
		Projects projects = MesquiteTrunk.getProjectList();
		if (projects == null || projects.getNumProjects() ==0)
			return "<h2>No projects open; therefore, no data to search (searched: \"" + s + "\")</h2>";
		String list = "";
		for (int i = 0; i<projects.getNumProjects(); i++){
			MesquiteProject proj = projects.getProject(i);
			list += proj.searchData(s, null);
		}
		return list;
	}
	/*.................................................................................................................*/
	/** to be overridden by MesquiteWindows for a text version of their contents*/
	public String getTextContents() {
		return "Sorry, there is not a text version of this window's contents";
	}
	boolean w = false;
	/*.................................................................................................................*/
	/** saves text file with contents of window*/
	public String saveTextToFile(String path, boolean append) {
		while (w)
			;
		w = true;
		String result = getTextContents();
		if (append)
			MesquiteFile.appendFileContents(path, result, false);
		else
			MesquiteFile.putFileContents(path, result, false);
		w = false;
		return result;
	}

	/** Returns menu location for item to bring the window to the for (0 = custom or don't show; 1 = system area of Windows menu; 2 = after system area of Windows menu)*/
	public int getShowMenuLocation(){
		return 2;
	}
	/*.................................................................................................................*/
	/** To be overridden to provide more appropriate print menu item (e.g., "Print Tree...", "Print Chart..."*/
	public String getPrintMenuItem() {
		return "Print Window...";
	}
	/*.................................................................................................................*/
	/** To be overridden to provide more appropriate print menu item (e.g., "Print Tree To Fit Page...", "Print Chart To Fit Page..."*/
	public String getPrintToFitMenuItemName() {
		return "Print Window To Fit Page...";
	}
	/*.................................................................................................................*/
	MesquiteMenuItemSpec getPrintToFitMenuItem() {
		if (ptfMMIS!=null)
			ptfMMIS.setName(getPrintToFitMenuItemName());
		return ptfMMIS;
	}
	/*.................................................................................................................*/
	/** To be overridden to provide more appropriate print menu item (e.g., "Print Tree to PDF...", "Print Chart..."*/
	public String getPrintToPDFMenuItemName() {
		return "Save Window as PDF...";
	}

	/*.................................................................................................................*/
	MesquitePrintJob pjob;
	/** Prints the current display in the window.  To be overridden to print in any special way.*/
	public void printWindow(MesquitePrintJob pjob) {
		if (pjob != null) {
			int mode;
			if (infoBar==null)
				mode =InfoBar.GRAPHICS;
			else mode = infoBar.getMode();
			if (mode==InfoBar.GRAPHICS || mode == InfoBar.EMPLOYEE_TREE) {//graphical mode
				if (mode==InfoBar.GRAPHICS)
					pjob.printComponent(graphics[0], null, currentFont);
				else
					pjob.printComponent(graphics[1], null, currentFont);
			}
			else if (infoBar!=null){  //text mode

				String s = infoBar.getText(mode);
				pjob.printText(s, new Font("Monospaced", Font.PLAIN, 12));
			}
		}
	}
	/*.................................................................................................................*/
	/** Print the parameters of the current module output. */
	public final void printParameters(MesquitePrintJob pjob) {
		if (pjob != null && infoBar!=null) {
			String s = infoBar.getText(InfoBar.TEXT_PARAMETERS);
			pjob.printText(s, outerContents.getFont());
		}
	}

	/*.................................................................................................................*/
	private void openPrWindow(int fitToPage) {
		Frame f = this.getParentFrame();
		if (!(f instanceof MesquiteFrame))
			return;
		int fitRule;
		if (infoBar.getMode()!= InfoBar.GRAPHICS)
			fitRule = MesquitePrintJob.AUTOFIT;
		else
			fitRule = fitToPage;
		pjob = MesquitePrintJob.getPrintJob((MesquiteFrame)f, "Print Window", fitRule);
	}
	/*.................................................................................................................*/
	private void doPrWindow() {
		if (pjob != null) {
			printWindow(pjob);
		}
	}
	/*.................................................................................................................*/
	private void closePrWindow() {
		if (pjob != null) {
			pjob.end();
		}
	}

	//for java 1.1 printing
	public Object getFitPrintObject(){
		return null;
	}
	/*.................................................................................................................*/
	protected void prWindow(int fitToPage) {
		Frame f = this.getParentFrame();
		if (!(f instanceof MesquiteFrame))
			return;
		MainThread.incrementSuppressWaitWindow();
		int fitRule;
		if (infoBar.getMode()!= InfoBar.GRAPHICS)
			fitRule = MesquitePrintJob.AUTOFIT;
		else
			fitRule = fitToPage;
		pjob = MesquitePrintJob.getPrintJob((MesquiteFrame)f, "Print Window", fitRule);
		if (pjob != null) {
			MesquiteTrunk.mesquiteTrunk.logln("Printing window");
			printWindow(pjob);
			/*
			if (infoBar !=null && infoBar.getMode()!=InfoBar.TEXT_PARAMETERS)
				printParameters(pjob);
			 */
			pjob.end();
		}
		MainThread.decrementSuppressWaitWindow();
	}
	/*.................................................................................................................*/
	MesquitePDFFile pdfFile;
	/**
	Writes the current display in the window to a PDF file.  To be overridden to display in any special way.
	@author Peter Midford
	 */
	public void windowToPDF(MesquitePDFFile pdfFile, int fitToPage) {
		if (pdfFile != null) {
			int mode;
			int fitRule = fitToPage;     //move fitRule here so it can be overridden
			pdfFile.setSizeOrientation(fitRule);        //will prompt for a size
			//if (infoBar.getMode()!= InfoBar.GRAPHICS)
			//	fitRule = MesquitePrintJob.AUTOFIT;
			//else
			//	fitRule = MesquitePrintJob.AUTOFIT; //fitToPage;
			if (infoBar==null)
				mode = InfoBar.GRAPHICS;
			else mode = infoBar.getMode();
			if (mode==InfoBar.GRAPHICS || mode == InfoBar.EMPLOYEE_TREE) {//graphical mode
				Component printingComponent;
				if (mode==InfoBar.GRAPHICS) 
					printingComponent = graphics[0];
				else
					printingComponent = graphics[1];
				pdfFile.printComponent(printingComponent,null,currentFont);
				//TODO: figure out why calling print(g) generates empty pages.
				//Graphics g = pdfFile.getPDFGraphicsForComponent(printingComponent, null);
				//printingComponent.print(g);
				//pdfFile.end();
			}
			else if (infoBar!=null){  //text mode	
				String s = infoBar.getText(mode);
				//Changed font size to 10, since courier is the only adobe supplied monospace font and
				//it is a little larger than the system supplied monospace font.
				pdfFile.printText(s, new Font("Monospaced", Font.PLAIN, 10));
			}
		}		
	}

	/*.................................................................................................................*/
	/**
	@author Peter Midford
	@param fitToPage int no longer used; PDF will always fit to one page; user can edit the file if necessary 
	 */
	protected void pdfWindow(int fitToPage) {
		MainThread.incrementSuppressWaitWindow();
		pdfFile = MesquitePDFFile.getPDFFile(this, "Save Window to PDF");
		if (pdfFile != null) {
			windowToPDF(pdfFile, fitToPage);
		}
		MainThread.decrementSuppressWaitWindow();
	}


	/*--------------------------------MODE (GRAPHICS, TEXT, INFO.) ----------------------------------*/
	/*.................................................................................................................*/
	/** The method to be called to add a component for display (adds it to the graphics[0] content area) */
	public Component addToWindow(Component c) {
		if (c == null || graphics == null || graphics[0] == null)
			return null;
		if (currentTool!=null) {
			removeKeyListener(graphics[0], currentTool);
		}
		removeKeyListener(graphics[0], palette);
		Component cc =  graphics[0].add(c);
		if (currentTool!=null) {
			addKeyListenerToAll(graphics[0], currentTool, true);
		}
		addKeyListenerToAll(graphics[0], palette, true);

		return cc;
	}
	/*.................................................................................................................*/
	/** The method to be called to remove a component from display (removes if from the graphics[0] content area) */
	public void removeFromWindow(Component c) {
		if (c == null || graphics == null || graphics[0] == null)
			return;
		if (currentTool!=null) {
			removeKeyListener(graphics[0], currentTool);
		}
		removeKeyListener(graphics[0], palette);
		if (graphics[0] !=null)
			graphics[0].remove(c);
		if (currentTool!=null) {
			addKeyListenerToAll(graphics[0], currentTool, true);
		}
		addKeyListenerToAll(graphics[0], palette, true);
	}
	/*.................................................................................................................*/
	/** Called in some circumstances (not all) when a component is added to a container in the window.  Currently used so that
	the Tree window knows that a component has been added to the TreeDisplay.*/
	public void componentAdded(Container cont, Component comp){
	}
	/*.................................................................................................................*/
	/** Causes the main graphics area to request focus*/
	public void graphicsRequestFocus() {
		if (graphics[0] !=null)
			graphics[0].requestFocus();
	}
	/*.................................................................................................................*/
	/** Gets the main graphics content area in which modules can add their components */
	public Component getGraphicsZero() {
		if (graphics == null)
			return null;
		return graphics[0];
	}
	/*.................................................................................................................*/
	/** Hides or disconnects the main graphics panel of the window */
	public void disconnectGraphics() {
		if (interContents == null )
			return;
		try {
			outerContents.remove(interContents);
		}
		catch (Exception e){
		}
	}
	/** Hides or disconnects the main graphics panel of the window */
	public void reconnectGraphics() {
		if (interContents == null )
			return;
		try {
			outerContents.add(interContents, "InterContents"); 
		}
		catch (Exception e){
		}
	}
	/*.................................................................................................................*/
	/** Gets the main graphics content area in which modules can add their components */
	public Panel getGraphicsArea() {
		if (graphics[0] != null)
			return graphics[0].getMainPanel();
		return null;
	}
	/** returns whether that panel is a side panel */
	public boolean hasSidePanel(MousePanel sp) {
		if (graphics[0] != null)
			return graphics[0].hasSidePanel(sp);
		return false;
	}
	/** requests that a side panel be added to main graphics panel */
	public void addSidePanel(MousePanel sp, int width) {
		if (graphics[0] != null)
			graphics[0].addSidePanel(sp, width);
	}
	/** requests that a side panel be added to main graphics panel */
	public void removeSidePanel(MousePanel sp) {
		if (graphics[0] != null)
			graphics[0].removeSidePanel(sp);
	}
	/** requests that a ledge panel be added to main graphics panel */
	public void addLedgePanel(MousePanel sp, int width) {
		if (graphics[0] != null)
			graphics[0].addLedgePanel(sp, width);
	}
	/** requests that a side panel be added to main graphics panel */
	public void removeLedgePanel(MousePanel sp) {
		if (graphics[0] != null)
			graphics[0].removeLedgePanel(sp);
	}
	public void checkPanelPositionsLegal(){  //available to be overridden
	}
	/*.................................................................................................................*/
	/** Sets the palette of the main graphics content area */
	void setPalette(ToolPalette palette) {
		this.palette = palette;
		if (graphics[0] == null)
			return;
		graphics[0].setPalette(palette); //setting palette into main graphics page
		removeKeyListener(graphics[0], palette);
		addKeyListenerToAll(graphics[0], palette, true);
	}
	/*.................................................................................................................*/
	/** Gets the tool palette of the main graphics content area */
	public ToolPalette getPalette() {
		return palette;
	}
	/*.................................................................................................................*/
	public static void addKeyListener(MesquiteWindow c, KeyListener k){
		addKeyListenerToAll(c.outerContents, k, true);
	}
	/*.................................................................................................................*/
	public static void addKeyListener(MesquiteWindow c, KeyListener k, boolean nonTextOnly){
		addKeyListenerToAll(c.outerContents, k, nonTextOnly);
	}
	/*.................................................................................................................*/
	public static void removeKeyListener(MesquiteWindow c, KeyListener k){
		removeKeyListener(c.outerContents, k);
	}
	/*.................................................................................................................*/
	public static void addKeyListenerToAll(Component c, KeyListener k, boolean nonTextOnly){
		if (c==null || k == null)
			return;
		if (!nonTextOnly || ( !(c instanceof TextComponent) || c == k))
			c.addKeyListener(k);
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					addKeyListenerToAll(cc[i], k, nonTextOnly);
		}

	}
	/*.................................................................................................................*/
	public static void removeKeyListener(Component c, KeyListener k){
		if (c==null || k == null)
			return;
		c.removeKeyListener(k);
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					removeKeyListener(cc[i], k);
		}

	}
	/*.................................................................................................................*/
	public void resetCursor(){  //May 02
		if (currentTool == null || currentTool.getOnlyWorksWhereSpecified()){
			setGraphicsDefaultCursor();
			return;
		}
		Cursor c = currentTool.getCursor();
		if (c!=null){
			setGraphicsCursor(c);
			if (palette!=null)
				palette.repaintAll();  //a workaround for Windows Java VM bug?
		}
		else
			setGraphicsDefaultCursor();
	}
	/*.................................................................................................................
	public void resetCursor(){  //May 02
		if (currentTool == null){
			setGraphicsDefaultCursor();
			return;
		}
		try { //just in case Java2 not available
			Image im = currentTool.getCursorImage();
			if (im == null){
				Dimension best = getToolkit().getBestCursorSize(16, 16);
				if ((best.width>16 || best.height>16) && MesquiteFile.fileExists(currentTool.getSizedImagePath(best.width))){
					im = MesquiteImage.getImage(currentTool.getSizedImagePath(best.width));
					if (im == null)
						im = MesquiteImage.getImage(currentTool.getImagePath());
				}
				else 
					im = MesquiteImage.getImage(currentTool.getImagePath());
				currentTool.setCursorImage(im);
			}
			Cursor c = getToolkit().createCustomCursor(im, currentTool.getHotSpot(), currentTool.getName());
			if (c!=null){
				setGraphicsCursor(c);
				if (palette!=null)
					palette.repaintAll();  //a workaround for Windows Java VM bug?
			}
		}
		catch (Throwable t){
			setGraphicsDefaultCursor();
		}
	}

	/*.................................................................................................................*/
	public void setCurrentTool(MesquiteTool tool){
		if (tool!=null && !tool.getEnabled())
			return;
		MesquiteTool prevTool = null;
		if (currentTool !=null) {
			currentTool.setInUse(false);
			previousTool = currentTool;
			removeKeyListener(graphics[0], currentTool); 
		}
		currentTool = tool;
		addKeyListenerToAll(graphics[0], currentTool, true); 
		setExplanation("");
		if (currentTool !=null) {
			currentTool.setInUse(true);
			if (currentTool.getDescription()!= null){
				if (currentTool.getExplanation()==null || currentTool.getDescription().equals(currentTool.getExplanation()))
					setExplanation("Tool: " + currentTool.getDescription());
				else
					setExplanation("Tool: " + currentTool.getDescription() + "\n(" +currentTool.getExplanation() + ")");
			}
			resetCursor();
		}
	}
	/*.................................................................................................................*/
	public MesquiteTool getCurrentTool(){
		return currentTool;
	}

	protected ToolPalette makeToolPalette(){
		return new ToolPalette(ownerModule, this, 1);
	}
	/*.................................................................................................................*/
	public MesquiteButton addTool(MesquiteTool tool){
		if (palette==null) {
			setPalette(makeToolPalette()); //note default of 1
		}
		palette.setEnabled(true);
		palette.setVisible(true);

		return palette.addTool(tool);
	}
	/*.................................................................................................................*/
	public void removeTool(MesquiteTool tool){
		if (palette !=null)
			palette.removeTool(tool);
	}
	/*.................................................................................................................*/
	public void toolTextChanged(){
		if (palette !=null)
			palette.toolTextChanged(); //fixed Apr 02
	}
	/*.................................................................................................................*/
	/** Gets the visibility of the InfoBar */
	public boolean getShowInfoBar() {
		return showInfoBar;
	}
	/*.................................................................................................................*/
	/** Sets the visibility of the InfoBar *
	public void setShowInfoBar(boolean vis) {
		int h = getHeight();
		showInfoBar = vis;
		if (infoBar==null && vis){
			infoBar = new InfoBar(this);
			outerContents.add(infoBar, "InfoBar"); //"InfoBar"
			outerContents.setInfoArea(infoBar);
			infoBar.setContentsArea(graphics, text, interContents);
		}
		if (showInfoBar) 
			infoBarHeight = 17;
		else
			infoBarHeight = 0;
		setWindowSize(getFullWidth(),h);
		showPage(InfoBar.GRAPHICS);
		if (infoBarMenuItem !=null)
			infoBarMenuItem.set(showInfoBar);
	}
	/*.................................................................................................................*/
	/** Gets the visibility of the explanation area */
	public boolean getShowExplanation() {
		return explanationHeight != 0;
	}
	/*.................................................................................................................*/
	/** Sets the visibility of the explanation area */
	public void setShowExplanation(boolean vis) {
		setShowExplanation(vis, ExplanationArea.minimumHeightExplanation);
	}
	/*.................................................................................................................*/
	/** Gets the height of the explanation area */
	public int getExplanationHeight() {
		return explanationHeight;
	}
	/*.................................................................................................................*/
	/** Sets the visibility of the explanation area */
	public void setShowExplanation(boolean vis, int height) {
		int h = getContentsHeight();
		int oldEH = explanationHeight;
		if (vis) 
			explanationHeight = height;
		else
			explanationHeight = 0;

		setWindowSize(MesquiteInteger.unassigned,h - (explanationHeight - oldEH));  //unassigned
	}
	/*.................................................................................................................*/
	/** Increments the height of the explanation area */
	public void incrementExplanationArea() {
		explanationArea.plus();
	}
	/*.................................................................................................................*/
	/** Increments the height of the annotation area */
	public void incrementAnnotationArea() {
		annotationArea.plus();
	}
	/*.................................................................................................................*/
	/** Gets the text in the explanation area.  This text is not editable */
	public String getExplanation(){
		if (explanationArea!=null) {
			return explanationArea.getExplanation();
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Gets the visibility of the Annotation area */
	public boolean getShowAnnotation() {
		return annotationHeight != 0;
	}
	/*.................................................................................................................*/
	/** Sets the visibility of the Annotation area */
	public void setShowAnnotation(boolean vis) {
		setShowAnnotation(vis, ExplanationArea.minimumHeightAnnotation);
	}
	/*.................................................................................................................*/
	/** Gets the height of the Annotation area */
	public int getAnnotationHeight() {
		return annotationHeight;
	}
	/*.................................................................................................................*/
	/** Sets the visibility of the Annotation area */
	public void setShowAnnotation(boolean vis, int height) {
		int h = getContentsHeight();
		int oldAH = annotationHeight;
		if (vis) 
			annotationHeight = height;
		else
			annotationHeight = 0;
		setWindowSize(MesquiteInteger.unassigned, h - (annotationHeight - oldAH));  //unassigned pass to say adjust height only
	}

	/*.................................................................................................................*/
	/** Returns whether annotation area has input focus */
	public boolean annotationHasFocus(){
		if (annotationArea!=null) {
			return annotationArea.hasFocus;
		}
		return false;
	}
	/*.................................................................................................................*/

	/** Set the text in the explanation area.  This text is not editable */
	public String getAnnotation(){
		if (annotationArea!=null) {
			try {
				return annotationArea.getExplanation();
			}
			catch(Exception e){
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** Sets the text in the explanation area.  This text is not editable */
	public void setExplanation(String text){
		if (explanationArea!=null) {
			try {
				explanationArea.setExplanation(text);
			}
			catch(Exception e){
			}
		}
	}
	/*.................................................................................................................*/
	/** Sets the Annotatable whose annotation will be used for the default text in the annotation area.  */
	public void setDefaultAnnotatable(Annotatable annot) {
		defaultAnnotatable = annot;
	}		
	/*.................................................................................................................*/
	/** Sets whether the explanation and annotation areas can receive focus.*/
	public void setAEFocusSuppression(boolean suppress){
		if (annotationArea!=null)
			annotationArea.setFocusSuppression(suppress);
		if (explanationArea!=null)
			explanationArea.setFocusSuppression(suppress);
	}
	/*.................................................................................................................*/
	/** Sets the text in the annotation area.  This text is editable; changes will be made in the MesquiteString object itself */
	public void setAnnotation(Annotatable annot){
		if (annotationArea!=null) {
			if (annot != null) {

				annotationArea.setExplanation(annot); 
				if (StringUtil.blank(annot.getName()))
					setExplanation("");
				else
					setExplanation("Footnote above refers to " + annot.getName());

			}
			else if (defaultAnnotatable !=null) {
				annotationArea.setExplanation(defaultAnnotatable);
				if (StringUtil.blank(defaultAnnotatable.getName()))
					setExplanation("");
				else
					setExplanation("Footnote above refers to " + defaultAnnotatable.getName());
			}
			else {
				annotationArea.setExplanation("");
				setExplanation("");
			}
		}
		resetCursor();
	}
	/*.................................................................................................................*/
	/** Sets the text in the annotation area.  This text is not editable */
	public void setAnnotation(String text, String annotationExplanation){
		if (annotationArea!=null) {
			if (StringUtil.blank(text)) {
				annotationArea.setExplanation("");
				setExplanation(annotationExplanation);
			}
			else {
				annotationArea.setExplanation(text);
				setExplanation(annotationExplanation);
			}
		}
	}
	/*.................................................................................................................*/
	public static void tickClock(String tickString){
		if (MesquiteTrunk.isMacOSXPanther() || MesquiteTrunk.isMacOSXBeforePanther())
			return;
		if (MesquiteModule.mesquiteTrunk.windowVector.size() == 0)
			return;
		else {
			Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				MesquiteWindow win = (MesquiteWindow)e.nextElement();
				win.explanationArea.tickClock(tickString);
			}
		}
	}
	/*.................................................................................................................*/
	public static void hideClock(){
		if (MesquiteModule.mesquiteTrunk.windowVector.size() == 0)
			return;
		else {
			Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				MesquiteWindow win = (MesquiteWindow)e.nextElement();
				win.explanationArea.hideClock();
			}
		}
	}
	/*.................................................................................................................*/
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){

	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	/*.................................................................................................................*/
	/** Return main object displayed.  Usage is specific to subclasses (see especially ListWindows)*/
	public Object getCurrentObject(){
		return currentObject;
	}

	/*.................................................................................................................*/
	/** Sets the main object displayed.  Usage is specific to subclasses (see especially ListWindows)*/
	public void setCurrentObject(Object object){
		currentObject = object;
	}
	/*.................................................................................................................*/
	boolean temp;
	/*.................................................................................................................*/
	/** Requests the page to be displayed (see InfoBar) */
	public void showPage(int mode) {
		if (mode>=0 && mode<InfoBar.numModes) {
			interContents.showPage(mode);
		}
		if (ptfMMIS!=null) {
			ptfMMIS.setEnabled(mode == InfoBar.GRAPHICS);
			MesquiteTrunk.resetMenuItemEnabling();
		}
	}
	/*.................................................................................................................*/
	/** Gets the mode (page) displayed */
	public int getMode() {
		if (infoBar!=null)
			return infoBar.getMode();
		else
			return 0;
	}

	/*--------------------------------WINDOW SIZE, POSITION ----------------------------------*/
	/*.................................................................................................................*/
	/** OuterContainer is telling window its size was reset MAY BE DEFUNCT*
	public void containerSizeReset(){
		if (setSizeDebugg>=0) {  //something else is setting container bounds; react by storing new content areas
			Insets insets = parentFrame.getInsets();
			if ((oldInsetTop<0 || oldInsetBottom <0 || oldInsetRight<0 || oldInsetLeft<0) && (0==insets.top || 0==insets.bottom || 0==insets.right || 0==insets.left) ) {
			}
			else if (oldInsetTop!=insets.top || oldInsetBottom !=insets.bottom || oldInsetRight!=insets.right || oldInsetLeft!=insets.left) {
				setWindowSize(contentWidth, contentHeight);
			}
		}
	}
	/*.................................................................................................................*/
	/** Widens the window. */
	public void widenWindow(int widthIncrement) {
		setWindowSize(getContentsWidth()+widthIncrement, MesquiteInteger.unassigned);  //unassigned to say widen only
	}
	/*.................................................................................................................*/
	/** Heightens the window. */
	public void heightenWindow(int heightIncrement) {
		setWindowSize(MesquiteInteger.unassigned, getContentsHeight() + heightIncrement); //unassigned to say heighten only
	}
	int contentHeight = 0;
	int contentWidth = 0;
	protected int explanationHeight = 0;
	protected int annotationHeight = 0;
	/*.................................................................................................................*/
	public int getWindowWidth() {
		return parentFrame.getBounds().width;
	}
	/*.................................................................................................................*/
	public int getWindowHeight() {
		return parentFrame.getBounds().height;
	}
	/*.................................................................................................................*/
	public void setWindowSizeDirect(int width, int height) {
		//before telling parent, indicate all the things I need
		int outerWidth = width;
		int outerHeight = height; //+infoBarHeight + explanationHeight + annotationHeight + graphics[0].getLedgePanelHeight();		
		parentFrame.setWindowSize(this, outerWidth, outerHeight);
	}
	protected boolean fromScriptCommand = false;
	/*.................................................................................................................*/
	/** Sets the window size.  To be used instead of setSize. */
	public void setWindowSize(int width, int height) {  //pass MesquiteInteger.unassigned to either width or height to keep as is
		if (MesquiteThread.isScripting() && !fromScriptCommand && !isLoneWindow()){
			if(MesquiteInteger.isCombinable(width) && width > 100)
				width = 100;
			if (MesquiteInteger.isCombinable(height) && height > 100)
				height = 100;
		}
		//before telling parent, indicate all the things I need
		int outerWidth = width;
		int outerHeight = height;
		int g0LPH = 0;
		if (graphics != null && graphics.length > 0 && graphics[0] != null)
			g0LPH = graphics[0].getLedgePanelHeight();
		if (MesquiteInteger.isCombinable(height))
			outerHeight += infoBarHeight + explanationHeight + annotationHeight + g0LPH;		
		parentFrame.setWindowSize(this, outerWidth, outerHeight, !isLoneWindow());
	}
	/*.................................................................................................................*/
	/** Forces a reset of the window size. */
	public void resetWindowSizeForce() {
		setWindowSizeForce(getContentsWidth(), getContentsHeight());
	}
	/*.................................................................................................................*/
	/** Sets the window size.  To be used instead of setSize. */
	public void setWindowSizeForce(int width, int height) {
		//before telling parent, indicate all the things I need
		int outerWidth = width;
		int g0LPH = 0;
		if (graphics != null && graphics.length > 0 && graphics[0] != null)
			g0LPH = graphics[0].getLedgePanelHeight();
		int outerHeight = height+infoBarHeight + explanationHeight + annotationHeight + g0LPH;		
		parentFrame.setWindowSize(this, outerWidth, outerHeight, !isLoneWindow());
	}

	public Insets getInsets(){
		if (parentFrame!=null)
			return parentFrame.getInsets();
		return null;
	}
	public void resetContentsSize(){  //the size returned may not be that requested!  Thus, recalcualte contentHeight here
		/*	Insets insets = parentFrame.getInsets();
	//	outerContents.setBounds(insets.left,insets.top ,width, height-30);//

		setSizeDebugg--;
		//setSize(parentFrame.contentWidth+insets.left+insets.right, outerHeight);
		setSizeDebugg++;
		outerContents.validate();
		 */
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (windowFinishedBuilding)
			outerContents.validate();
		windowResized();
		MesquiteWindow.uncheckDoomed(this);
	}
	public void pack(){
		setSizeDebugg--;
		parentFrame.pack();
		setSizeDebugg++;
	}
	/*.................................................................................................................*/
	/** Sets the size of the window (not to be used directly) *
	public void setSize(int w, int h) {
		if (setSizeDebugg>=0) {
			MesquiteMessage.warnProgrammer("Programmer: use setWindowSize instead of setSize for MesquiteWindows: " + getTitle());
		}
		super.setSize(w,h);
	}
	/*.................................................................................................................*/
	/** Sets the size of the window (not to be used directly) *
	public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, y, w,h); 

	}
	/*.................................................................................................................*/
	/** Gets the minimum height of the content area of the window */
	public int getMinimumContentHeight(){
		return 200;
	}
	/*.................................................................................................................*/
	/** Gets the minimum size of the window */
	public Dimension getMinimumSize(){
		//return new Dimension(100,100);
		Insets insets = getInsets();
		int width  = insets.right+infoBar.getInfoBarWidth()+4;

		return new Dimension(width, infoBarHeight + explanationHeight + annotationHeight+ insets.top +insets.bottom+getMinimumContentHeight());
	}
	/*.................................................................................................................*/
	public Dimension getMaximumSize() {
		return parentFrame.getMaximumSize();
	}

	public void setBackground(Color c){
		outerContents.setBackground(c);
	}
	public Color getBackground(){
		return outerContents.getBackground();
	}
	public Font getFont(){
		return outerContents.getFont();
	}
	public void setFont(Font f){
		outerContents.setFont(f);
	}
	public void setWindowLocation(int x, int y, boolean overridePrevious){
		setWindowLocation(x, y, overridePrevious, false);
	}
	public void setWindowLocation(int x, int y, boolean overridePrevious, boolean scripting){
		parentFrame.setWindowLocation(x, y, overridePrevious, scripting);
	}
	public void setLocation(int x, int y){
		parentFrame.setWindowLocation(x, y, false, false);
	}

	public Point getLocation(){
		return parentFrame.getLocation();
	}

	public Rectangle getBounds(){
		if (parentFrame == null)
			return new Rectangle(0,0,0,0);
		Rectangle bounds = parentFrame.getBounds(this);
		return bounds;
	}
	public boolean isShowing(){
		return parentFrame.isShowing();
	}
	public void requestFocus(){
		outerContents.requestFocus();
	}
	/*.................................................................................................................*/
	/** Gets the content width of the window (excluding the insets) */
	public int getContentsWidth(){
		///return	 (outerContents.getBounds().width);
		Insets insets = getInsets();
		return (getBounds().width);
	}
	/*.................................................................................................................*/
	/** Gets the content width of the window (excluding the insets) */
	public int getFullWidth(){
		///return	 (outerContents.getBounds().width);
		Insets insets = getInsets();
		return (getBounds().width);
	}
	/*.................................................................................................................*/
	/** Gets the content height of the window (excluding the insets) */
	public int getContentsHeight(){
		Insets insets = getInsets();
		if (insets==null)
			return (getBounds().height - infoBarHeight - explanationHeight -annotationHeight);
		return (getBounds().height - infoBarHeight -explanationHeight -annotationHeight);
	}
	/*.................................................................................................................*/
	/** Gets the content width of the graphics content area of the window (excluding the insets).  Also included tool palette if asked */

	public int getWidth(boolean includePalette){
		if (graphics==null)
			return 0;
		if (graphics[0] == null || getBounds() == null)
			return 0;
		int baseWidth = getBounds().width;
		if (!includePalette) {
			ToolPalette palette =graphics[0].getPalette();
			if (palette != null && palette.isVisible())
				baseWidth -= palette.getWidth();
		}
		baseWidth -= graphics[0].getSidePanelWidth();

		return baseWidth;
	}
	/*.................................................................................................................*/
	/** Gets the content width of the graphics content area of the window (excluding the insets AND any palette) */
	public int getWidth(){
		return getWidth(false);
	}
	/*.................................................................................................................*/
	/** Gets the content height of the window (excluding the insets) */
	public int getHeight(){
		if (graphics == null)
			return 0;
		if (graphics[0] == null)
			return 0;
		if (getBounds()==null)
			return 0;
		try {
			return (getBounds().height - infoBarHeight - explanationHeight -annotationHeight) - graphics[0].getLedgePanelHeight();
		}
		catch (Exception e){
		}
		return 0;
	}
	/*.................................................................................................................*/
	/** Center the passed window in the screen */
	public static void centerWindow(MesquiteDialog w){
		centerWindow(w.getParentDialog());
	}
	/*.................................................................................................................*/
	/** Center the passed window in the screen */
	public static void centerWindow(MesquiteDialogParent w){
		if (w.isWizard()){
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int v, h;
			h = (screenSize.width-MesquiteDialog.wizardWidth)/2;
			int menuBarHeight = 0;
			if (!StringUtil.blank(System.getProperty("mrj.version")))
				menuBarHeight = 32;
			v = (screenSize.height-MesquiteDialog.wizardHeight - menuBarHeight)/2 + menuBarHeight;
			if (v < menuBarHeight)
				v = menuBarHeight;

			w.setLocation(h, v);
		}
		else 
			centerWindow((Window)w);
	}
	/*.................................................................................................................*/
	/** Center the passed window in the screen */
	public static void centerWindow(MesquiteWindow w){
		MesquiteFrame f = w.getParentFrame();
		if (f.getNumWindows()>1)
			return; //don't center if other windows already tabbed in frame
		centerWindow(w.getParentFrame(), true, true);
	}
	/*.................................................................................................................*/
	/** Center the passed window in the screen */
	public static void centerWindow(Window w){
		centerWindow(w, true, true);
	}
	/*.................................................................................................................*/
	/** Center the passed window in the screen */
	public static void centerWindow(Window f, boolean horizontally, boolean vertically){

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int v, h;
		if (horizontally)
			h = (screenSize.width-f.getBounds().width)/2;
		else
			h = f.getBounds().x;
		int menuBarHeight = 0;
		if (!StringUtil.blank(System.getProperty("mrj.version")))
			menuBarHeight = 32;
		if (vertically) {
			v = (screenSize.height-f.getBounds().height - menuBarHeight)/2 + menuBarHeight;
			if (v < menuBarHeight)
				v = menuBarHeight;
		}
		else
			v = f.getBounds().y;

		f.setLocation(h, v);
	}
	/*.................................................................................................................*/
	/** Center the passed window in the screen */
	public static void centerWindowTile(Window f, int tile){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int fWidth = f.getBounds().width;
		int fHeight = f.getBounds().height;
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		int screenTop = 0;
		if (f instanceof ProgressWindow && ((ProgressWindow)f).isSpontaneous()){
			if (MesquiteDialog.currentWizard != null && MesquiteDialog.currentWizard.isVisible())
				screenTop = screenHeight - (screenHeight-MesquiteDialog.wizardHeight)/2;
			else
				screenTop = screenHeight - (screenHeight)/2;
			screenHeight = screenHeight - screenTop;
		}
		if (tile==0) 
			f.setLocation((screenWidth-fWidth)/2, screenTop + (screenHeight-fHeight)/2-48);
		else if (tile==1) 
			f.setLocation((screenWidth-fWidth)/2, screenTop + (screenHeight-fHeight)/2-48+fHeight+18);
		else {
			int tileVerAmount = (tile % 5)*(fHeight/2);
			int tileHorAmount = (tile / 5)*(fWidth/2);
			f.setLocation((screenWidth-fWidth)/2+tileHorAmount, screenTop + (screenHeight-fHeight)/2-48+tileVerAmount+fHeight+10);
		}
	}

	/*--------------------------------WINDOW order ----------------------------------*/
	/*.................................................................................................................*/
	/** INCOMPLETE. 
	public void toBack(){
		super.toBack();
		MesquiteMessage.warnProgrammer("Progammer: window.toBack() should be avoided until window ranks system works properly");
	}
/**/

	/*.................................................................................................................*/
	/** INCOMPLETE. */
	public void toFront(){
		if (doingShow)
			return;
		if (parentFrame == null)
			return;
		setVisible(true);
		parentFrame.setAsFrontWindow(this);
		if (isVisible() && rank!=highestRank) { //window of intermediate rank brought to front; must readjust ranks of all
			rank = ++highestRank;
		}
	}
	/*.................................................................................................................*/
	/** INCOMPLETE. */
	public int getRank(){
		return rank;
	}
	/*.................................................................................................................*/

	public boolean isVisible(){
		return parentFrame != null && parentFrame.isVisible() && parentFrame.windowPresent(this);  
	}
	/*.................................................................................................................*/
	boolean doingShow = false;


	public void show(){
		if (doingShow)
			return;
		setVisible(true);
		parentFrame.setVisible(true);
	}
	/** Shows the window */
	public void setVisible(boolean vis, boolean scripting) {
		if (scripting && vis && isCompacted()){
			setVisible(vis);
			parentFrame.setVisible(true);
		}
		else
			setVisible(vis);
	}
	public void removeAll(){
		try {
			outerContents.removeAll();
		}
		catch (Exception e){
			// sometimes exceptions are thrown, possible because of mistimed threads
		}
	}
	/** Shows the window */
	public void setVisible(boolean vis) {
		if (!vis && !closeable){
			if (MesquiteTrunk.debugMode)
				MesquiteMessage.printStackTrace("Using setVisible (false) before window is even instantiated!");
			return;
		}
		if (doingShow)
			return;
		boolean wasVis = isVisible();
		doingShow = true;
		if (vis){
			if (!GUIavailable || suppressAllWindows){
				doingShow = false;
				return;
			}
		}
		if (parentFrame == null)
			return;
		if (orphan){
			parentFrame.popIn(this);
			orphan = false;
		}
		parentFrame.setVisible(this, vis);
		if (vis)
			parentFrame.setVisible(true);
		doingShow = false;
		if (!wasVis && vis && graphics != null && graphics[0]!=null && (!MesquiteTrunk.mesquiteTrunk.isStartupShutdownThread(Thread.currentThread()) || (!(this instanceof SystemWindow) && !(this instanceof mesquite.trunk.AboutWindow))))
			graphics[0].requestFocusInWindow();
	}
	/*--------------------------------MENU BARS ----------------------------------*/

	void disposeMenuComponent(MenuComponent mc){
		if (mc == null)
			return;
		if (mc instanceof Menu){
			Menu m = (Menu)mc;
			for (int i= 0; i<m.getItemCount() ; i++){
				MenuComponent currentC = m.getItem(i);
				disposeMenuComponent(currentC);
				m.remove(currentC);
			}
			if (mc instanceof MesquiteSubmenu)
				((MesquiteSubmenu)mc).disconnect();
		}
		else if (mc instanceof MenuBar){
			MenuBar m = (MenuBar)mc;
			for (int i= 0; i<m.getMenuCount() ; i++){
				MenuComponent currentC = m.getMenu(i);
				disposeMenuComponent(currentC);
				m.remove(currentC);
			}
			if (mc instanceof MesquiteMenuBar)
				((MesquiteMenuBar)mc).disconnect();
		}
		else if (mc instanceof MesquiteMenuItem)
			((MesquiteMenuItem)mc).disconnect();
		else if (mc instanceof MesquiteCheckMenuItem) 
			((MesquiteCheckMenuItem)mc).disconnect();
	}

	/*.................................................................................................................*/
	/** DEBUGGING */
	public static void setReportMenuResets(boolean r){
		reportMenuResets = r;
	}
	/*.................................................................................................................*/
	/** Resets the menu bar of the window */
	static final boolean refreshMenusOnlyFrontWindows = true;  //if true, then uses new system that remakes menus only when window is front or brought to front

	/*........................................................*/
	boolean sameName(MenuComponent current, MenuComponent target){
		if (current instanceof Menu && target instanceof Menu){
			Menu currentMM = (Menu)current;
			Menu targetMM = (Menu)target;
			if ((currentMM.getLabel()== null && targetMM.getLabel()==null))
				return true;
			if (currentMM.getLabel()== null || targetMM.getLabel()==null)
					return false;
			return currentMM.getLabel().equals(targetMM.getLabel());
		}
		if (current instanceof MenuItem && target instanceof MenuItem){
			MenuItem currentMMI = (MenuItem)current;
			MenuItem targetMMI = (MenuItem)target;
			if ((currentMMI.getLabel()== null && targetMMI.getLabel()==null))
				return true;
			if (currentMMI.getLabel()== null || targetMMI.getLabel()==null)
					return false;
			return currentMMI.getLabel().equals(targetMMI.getLabel());
		}
		return true;
	}
	/*........................................................*/
	boolean sameSpecs(MenuComponent current, MenuComponent target){
		if (current instanceof MesquiteMenu && target instanceof MesquiteMenu){
			MesquiteMenu currentMM = (MesquiteMenu)current;
			MesquiteMenu targetMM = (MesquiteMenu)target;
			return (currentMM.getSpecification() == targetMM.getSpecification());
		}
		if (current instanceof MesquiteMenuItem && target instanceof MesquiteMenuItem){
			MesquiteMenuItem currentMMI = (MesquiteMenuItem)current;
			MesquiteMenuItem targetMMI = (MesquiteMenuItem)target;
			return (currentMMI.getSpecification() == targetMMI.getSpecification());
		}
		if (current instanceof MesquiteMenuItem || target instanceof MesquiteMenuItem || current instanceof MesquiteMenu || target instanceof MesquiteMenu)
			return false;
		return true;
	}
	/*........................................................*/
	boolean isSame(MenuComponent current, MenuComponent target){
		if (!sameName(current, target)) 
			return false;
		if (!sameSpecs(current, target))
			return false;
		if (current instanceof Menu && target instanceof Menu){
			Menu currentM = (Menu)current;
			Menu targetM = (Menu)target;
			if (currentM.getItemCount() != targetM.getItemCount())
				return false;
			for (int i= 0; i<currentM.getItemCount() ; i++){
				MenuComponent currentC = currentM.getItem(i);
				MenuComponent targetC = targetM.getItem(i);
				if (!isSame(currentC, targetC))
					return false;
			}
		}
		return true;
	}

	/*........................................................*
	String listItems(Menu c){
		String x = "";
		for (int i = 0; i< c.getItemCount(); i++){
			x += " (" + c.getItem(i).getLabel() + ")";
		}
		return x;
	}
	String listMenus(MenuBar c){
		String x = "";
		for (int i = 0; i< c.getMenuCount(); i++){
			x += " (" + c.getMenu(i).getLabel()+ ")";
		}
		return x;
	}
	/*........................................................*/
	int sameUntil(MenuBar current, MenuBar target){
		int it = 0;
		while (it<target.getMenuCount() && it<current.getMenuCount()){
			Menu thisTargetMenu = target.getMenu(it);
			Menu thisCurrentMenu = current.getMenu(it);
			if (!isSame(thisCurrentMenu, thisTargetMenu))
				return it;
			it++;
		}
		return it;
	}
	/*........................................................*/
	//This saves the menus of current up until a difference with target, then transfers target menus over
	void mergeMenus(MenuBar current, MenuBar target){
		int startOfDifference = sameUntil(current, target);
		if (startOfDifference == current.getMenuCount() && startOfDifference == target.getMenuCount())
			return;
		for (int it= current.getMenuCount()-1; it>=startOfDifference; it--){
			try{
				Menu m = current.getMenu(it);
			disposeMenuComponent(m);
			current.remove(it);
			}
			catch (NullPointerException e){
			}
		}
		Menu[] toTransfer = new Menu[target.getMenuCount()-startOfDifference+1];
		int k = 0;
		for (int it = startOfDifference; it<target.getMenuCount(); it++)
			toTransfer[k++] = target.getMenu(it);

		for (int it = 0; it<toTransfer.length; it++)
			current.add(toTransfer[it]);
	}


	/*........................................................*/
	static double totalTime =0;

	public void resetMenus(){
		resetMenus(true);
	}

	public void resetMenus(boolean generateRegardless){
		if (!generateRegardless && refreshMenusOnlyFrontWindows && parentFrame.frontWindow != this){ //this is the short circuit that makes it so that only frontmost windows have their menus reset
			needMenuBarReset = true;
			return;
		}

		resetMenuTime.start();
		MesquiteMenuBar tempMenuBar = new MesquiteMenuBar(this); 
		if (ownerModule==null) {
			MesquiteMessage.printStackTrace("@@@@@@@@@@@@@@@@@@null ownerModule in window");
			return;
		}
		else {
			ownerModule.composeMenuBar(tempMenuBar, this); 
		}
		mergeMenus(menuBar, tempMenuBar);

		setMenuBar(menuBar);  
		needMenuBarReset = false;
		menuResets++;
		if (reportMenuResets) {
			double time = resetMenuTime.timeSinceLastInSeconds();
			totalTime += time;
			ownerModule.logln("   " + Integer.toString(menuResets) + " menu resets for " + getTitle() + "    " +time + " seconds");
		}

		resetMenuTime.end();
	}
	public void setMenuBar(MenuBar mbar) {
		if (parentFrame!=null)
			parentFrame.setMenuBar(this, menuBar);  //this actually calls the setMenuBar only if this window is at front
	}
	public MenuBar getMenuBar() {
		return menuBar;
	}
	boolean needMenuBarReset = true;
	public MenuBar getMenuBar(boolean generateIfNeeded) { //MenuBar object is built on demand at this point
		if (generateIfNeeded && needMenuBarReset) {
			resetMenus(true);
		}
		return menuBar;
	}

	public void setMinimalMenus(boolean minimal){
		minimalMenus = minimal;
	}
	public boolean getMinimalMenus(){
		return minimalMenus;
	}

	public MesquiteCommand getCopySpecialCommand() {
		return null; 
	}
	public String getCopySpecialName() {
		return "Copy Special"; 
	}
	public MesquiteCommand getPasteSpecialCommand() {
		return null; 
	}
	public String getPasteSpecialName() {
		return "Paste Special"; 
	}
	public MesquiteCommand getCutCommand() {
		return null;
	}
	public MesquiteCommand getPasteCommand() {
		return null; 
	}
	public MesquiteCommand getClearCommand() {
		return null; 
	}
	public void selectAll() {
		if (annotationHasFocus()){
			annotationArea.getTextArea().selectAll();
		}
		else if (infoBar.getMode()==InfoBar.GRAPHICS) {
			selectAllGraphicsPanel();
		}
		else {
			TextArea textArea = infoBar.getTextArea(infoBar.getMode());
			if (textArea!=null){
				textArea.selectAll();
			}
		}
	}

	public void selectAllGraphicsPanel(){
	}

	public void copy(){
		if (annotationHasFocus()){
			String s = annotationArea.getTextArea().getSelectedText();
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection ss = new StringSelection(s);
			clip.setContents(ss, ss);
		}
		else if (infoBar.getMode()==InfoBar.GRAPHICS) {
			copyGraphicsPanel();
		}
		else {
			infoBar.refreshText(infoBar.getMode());
			TextArea textArea = infoBar.getTextArea(infoBar.getMode());
			if (textArea!=null){
				String s = textArea.getSelectedText();
				if (StringUtil.blank(s))
					s = textArea.getText();
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(s);
				clip.setContents(ss, ss);
			}
		}

	}
	public void copyGraphicsPanel(){

		Component focused = componentWithFocus();
		if (focused != null && focused instanceof TextComponent){
			String sel = ((TextComponent)focused).getSelectedText();
			if (sel != null){
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(sel);
				clip.setContents(ss, ss);
				return;
			}
		}
		infoBar.refreshText(InfoBar.TEXT_CONTENTS);
		TextArea textArea = infoBar.getTextArea(InfoBar.TEXT_CONTENTS);
		if (textArea!=null){
			String s = textArea.getSelectedText();
			if (StringUtil.blank(s))
				s = textArea.getText();
			if (StringUtil.blank(s))
				return;
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection ss = new StringSelection(s);
			clip.setContents(ss, ss);
		}
	}

	/*-------------------------------- COMMANDS  ----------------------------------*/
	/*.................................................................................................................*/
	/** Incorporate into the passed snapshot the complete snapshot commands for the window (including getWindow, tell It, and endTell)*/
	public void incorporateSnapshot(Snapshot snap, MesquiteFile file){
		Snapshot fromWindow =getSnapshot(file);
		snap.addLine("getWindow");
		snap.addLine("tell It");
		snap.incorporate(fromWindow, true);
		snap.addLine("endTell");
	}
	/*.................................................................................................................*/
	/** Gets basic snapshot for window, including size, location. */
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (isPoppedOut() && compactWindows){
			if (popAsTile)
				temp.addLine("popAsTile true");
			else
				temp.addLine("popAsTile false");
			temp.addLine("popOut");
		}
		//	if (getParentFrame().getWithTabWhenSaved())
		//		temp.addLine("tabbedWhenSaved");
		/*
		 * if (showInfoBar)
			temp.addLine("onInfoBar");
		else
			temp.addLine("offInfoBar");
		 */
		temp.addLine("setExplanationSize " + explanationHeight);
		temp.addLine("setAnnotationSize " + annotationHeight);
		temp.addLine("setFontIncAnnot " + StringUtil.tokenize(Integer.toString(annotationArea.fontIncrement)));
		temp.addLine("setFontIncExp " + StringUtil.tokenize(Integer.toString(explanationArea.fontIncrement)));
		temp.addLine("setSize " + getContentsWidth() + " " + getContentsHeight());
		temp.addLine("setLocation " + getLocation().x + " " + getLocation().y);
		temp.addLine("setFont " + ParseUtil.tokenize(currentFont.getName())); //fixed to tokenize 12 Oct 01
		temp.addLine("setFontSize " + currentFont.getSize());
		/*if (!StringUtil.blank(windowAnnotation.toString())) {
    	 		temp.addLine("setAnnotation " + ParseUtil.tokenize(windowAnnotation.toString()));
  	 	}
		 */
		if (graphics[0] != null && graphics[0].getPalette()!=null) {
			temp.addLine("getToolPalette");
			temp.addLine("tell It");
			temp.incorporate(graphics[0].getPalette().getSnapshot(file), true);
			temp.addLine("endTell");
		}
		if (getMode()>0)
			temp.addLine("showPage " + getMode());
		if (ownerModule != null && ownerModule.getProject()!=null && ownerModule.getProject().activeWindowOfProject == this)
			temp.addLine("setActive"); //so that file save will remember foremost window

		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public boolean setWindowFontSize(int fontSize) {
		if (!MesquiteInteger.isCombinable(fontSize))
			fontSize = MesquiteInteger.queryInteger(MesquiteWindow.windowOfItem(this), "Font size", "Font size for window", currentFont.getSize(), 4, 256);
		if (!MesquiteInteger.isCombinable(fontSize))
			return false;
		Font fontToSet = new Font (currentFont.getName(), currentFont.getStyle(), fontSize);
		setWindowFont(fontToSet);
		return true;
	}
	/*.................................................................................................................*/
	public void setWindowFont(String fontName) {

		Font fontToSet = new Font (fontName, currentFont.getStyle(), currentFont.getSize());
		setWindowFont(fontToSet);
	}
	MesquiteSubmenu fontSubmenu, fontSizeSubmenu;

	Font getCurrentFont(){
		return currentFont;
	}
	/*.................................................................................................................*/
	public void setWindowFont(Font fontToSet) {
		if (fontToSet!= null) {
			currentFont = fontToSet;
			//setFont(fontToSet);
			setComponentFont(graphics[0], currentFont);
			if (annotationArea!=null)
				annotationArea.setFont(currentFont);
			if (explanationArea!=null)
				explanationArea.setFont(currentFont);
			repaintAll();
			if (fontSubmenu != null)
				fontSubmenu.checkName(fontToSet.getName());
			if (fontSizeSubmenu != null)
				fontSizeSubmenu.checkName(Integer.toString(fontToSet.getSize()));
		}
		MesquiteTrunk.resetCheckMenuItems();
	}
	/*.................................................................................................................*/
	/** Returns the MesquiteWindow containing the menu item*/
	public void setComponentFont(Component c, Font f){
		if (c==null)
			return;

		if (!(c instanceof MousePanel))  //only inherited as far as Mesquite specific
			return;
		if (c instanceof MousePanel && ((MousePanel)c).fontExplicitlySet())
			return;
		c.setFont(f);
		if (MesquiteTrunk.isMacOSX())  //not needed on Mac; components inherite fonts from containers
			return;
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					setComponentFont(cc[i], f);
		}

	}

	/*.................................................................................................................*/
	public void setToPreviousTool() {
		if (previousTool !=null) {
			ToolPalette toolPalette = graphics[0].getPalette();
			if (toolPalette!=null)
				toolPalette.setCurrentTool(previousTool);
			setCurrentTool(previousTool);
		}
	}
	/*.................................................................................................................*/
	/** Respond to commands sent to the window. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(MesquiteWindow.class, "Makes the window visible", null, commandName, "showWindow")) {
			setVisible(true, MesquiteThread.isScripting());
		}
		else if (checker.compare(MesquiteWindow.class, "brings window to front", null, commandName, "setAsFront")) {
			getParentFrame().showPage(this);
			//	getParentFrame().showFrontWindow();
		}
		else if (checker.compare(MesquiteWindow.class, "Makes the window visible", null, commandName, "showWindowForce")) {
			setVisible(true);
			getParentFrame().showFrontWindow();
		}
		else if (checker.compare(MesquiteWindow.class, "Forces repaint of window", null, commandName, "forceRepaint")) {
			repaintAll();
		}
		else if (checker.compare(MesquiteWindow.class, "Pops Window out of project window (if compacted)", null, commandName, "popOut")) {
			if (compactWindows && !isPoppedOut() && parentFrame != null)
				parentFrame.popOut(this, false);
		}
		else if (checker.compare(MesquiteWindow.class, "Indicates whether to pop as tile or separate window", "[true or false]", commandName, "popAsTile")) {
			if (arguments != null && arguments.equalsIgnoreCase("true"))
				setPopAsTile(true);
			else
				setPopAsTile(false);
		}
		else if (checker.compare(MesquiteWindow.class, "Lists windows of frame", null, commandName, "frame")) {
			parentFrame.diagnose();
		}
		else if (checker.compare(MesquiteWindow.class, "Flips to window of frame", null, commandName, "first")) {
			parentFrame.showFirstPage();
		}
		else if (checker.compare(MesquiteWindow.class, "Flips to window of frame", null, commandName, "next")) {
			parentFrame.showNextPage();
		}
		else if (checker.compare(MesquiteWindow.class, "Sets this window to be foremost; to be used in scripting", null, commandName, "setActive")) {
			if (ownerModule != null && ownerModule.getProject()!=null)
				ownerModule.getProject().windowToActivate = this; //so that file read will restore foremost window
		}
		else if (checker.compare(MesquiteWindow.class, "Closes the window (calls owner module's windowGoAway)", null, commandName, "closeWindow")) {
			closeWindow();
		}
		else if (checker.compare(this.getClass(), "Undoes the previous change in the window", null, commandName, "undo")) {
			if (getUndoer()!=null){
				Undoer[] undoers = getUndoer();
				if (undoers!=null) {
					Undoer[] newUndoers = new Undoer[undoers.length];
					for (int i=0;i<undoers.length; i++)
						if (undoers[i] != null)
							newUndoers[i]=undoers[i].undo();
					setUndoer(newUndoers);
				}
			}
			else
				MesquiteMessage.discreetNotifyUser( "Can't Undo");
		}
		else if (checker.compare(this.getClass(), "Sets the tool to the previous tool.", null, commandName, "setToPreviousTool")) {
			setToPreviousTool();
		}
		else if (checker.compare(MesquiteWindow.class, "Composes and shows the menu and control explanations web page", null, commandName, "showExplanations")) {
			CommandChecker.showExplanations(graphics[0], getMenuBar(), getName()); 
		}
		else if (checker.compare(this.getClass(), "Shows the commands needed to put window and its modules into their current states", null, commandName, "showSnapshot")) {
			if (getOwnerModule() == null)
				return null;
			FileCoordinator mb = getOwnerModule().getFileCoordinator();
			if (mb!=null) {
				String s = "Snapshot: commands needed to put window and its modules into their current states\n" + Snapshot.getSnapshotCommands(getOwnerModule(), null, "");
				return mb.displayText(s, "Snapshot of commands");
			}
			else {
				MesquiteModule.mesquiteTrunk.alert("Snapshot not available (no module associated with window)");
			}
		}

		else if (checker.compare(this.getClass(), "Put text version of window into log", null, commandName, "text")) {
			if (ownerModule !=null)
				ownerModule.logln("Window " + getName() + "\n" + getTextContents());

		}
		else if (checker.compare(MesquiteWindow.class, "Copies current selection to clipboard", null, commandName, "copy")) {
			copy();
		}
		else if  (checker.compare(MesquiteWindow.class, "Copies current selection to clipboard and clears from selection", null, commandName, "cut")) {
			if (!annotationHasFocus()){
				MesquiteCommand com = getCutCommand();
				if (com!=null)
					com.doItMainThread("", null, this);  // command invoked
				else {
					Component focused = componentWithFocus();
					if (focused != null && focused instanceof TextComponent){
						TextComponent tf = (TextComponent)focused;
						String sel = tf.getSelectedText();
						String all = tf.getSelectedText();
						if (sel != null){
							Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
							StringSelection ss = new StringSelection(sel);
							clip.setContents(ss, ss);
							if (tf.isEditable())
								tf.setText(all.substring(tf.getSelectionStart(), tf.getSelectionEnd()));
						}
					}
				}
			}
			else {
				TextArea ta = annotationArea.getTextArea();
				String s = ta.getSelectedText();
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(s);
				clip.setContents(ss, ss);
				ta.replaceRange("", ta.getSelectionStart(), ta.getSelectionEnd());
				setAnnotation(ta.getText(), null);
			}
		}
		else if  (checker.compare(MesquiteWindow.class, "Clears current selection", null, commandName, "clear")) {
			if (!annotationHasFocus()){
				MesquiteCommand com = getClearCommand();
				if (com!=null)
					com.doItMainThread("", null, this);  // command invoked
			}
			else {
				TextArea ta = annotationArea.getTextArea();
				ta.replaceRange("", ta.getSelectionStart(), ta.getSelectionEnd());
				setAnnotation(ta.getText(), null);
			}
		}

		else if  (checker.compare(MesquiteWindow.class, "Pastes from clipboard into current selection", null, commandName, "paste")) {
			if (!annotationHasFocus()){
				MesquiteCommand com = getPasteCommand();
				if (com!=null)
					com.doItMainThread("", null, this);  // command invoked
			}
			else {
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				try {
					Transferable t = clip.getContents(this);
					String s = (String)t.getTransferData(DataFlavor.stringFlavor);
					if (s!=null) {
						TextArea ta = annotationArea.getTextArea();
						int st = ta.getSelectionStart();
						ta.replaceRange(s, ta.getSelectionStart(), ta.getSelectionEnd());
						Annotatable a = annotationArea.getAnnotatable();
						if (a == null)
							setAnnotation(ta.getText(), null);
						else
							a.setAnnotation(ta.getText(), true);
						ta.setSelectionStart(st + s.length());
						ta.setSelectionEnd(st + s.length());
					}
				}
				catch(Exception e){
					MesquiteMessage.printStackTrace(e);
				}
			}
		}
		else if  (checker.compare(MesquiteWindow.class, "Selects all", null, commandName, "selectAll")) {
			selectAll();
		}
		else if  (checker.compare(MesquiteWindow.class, "Sets the title", "[title]", commandName, "setTitle")) {
			Parser parser = new Parser(arguments);
			String name =parser.getFirstToken();
			if (StringUtil.notEmpty(name))
				setTitle(name);
		}


		else if (checker.compare(MesquiteWindow.class, "Presents dialog through which user can send commands to the module in charge of the window", null, commandName, "sendScript")) {
			MesquiteModule module = getOwnerModule();
			if (module!=null) {
				Puppeteer p = new Puppeteer(module);
				p.dialogScript(module, module.containerOfModule(), "owner module (" + module.getName() + ") of this window");
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Toggles whether this window is tiled out or not", null, commandName, "toggleTileOutWindow")) {  
			if (isPoppedOut()){
				popIn();
				popOutWindowMSpec.setEnabled(true);
			}
			else {
				setPopAsTile(true);
				popOut(true);
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Toggles whether this window is popped out or not", null, commandName, "togglePopOutWindow")) { 
			if (isPoppedOut())
				popIn();  
			else if (compactWindows){
				setPopAsTile(false);
				popOut(true);
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Sets which page of the window (graphics, text, explanations, parameters, employee tree, etc.) is showing", "[page number]", commandName, "showPage")) {
			if (infoBar==null)
				return null;
			MesquiteInteger pos = new MesquiteInteger(0);
			int mode = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(mode)){
				infoBar.setMode(mode);
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Prints the contents of the window", null, commandName, "printWindow")) {
			prWindow(MesquitePrintJob.NATURAL);
		}
		else if (checker.compare(MesquiteWindow.class, "Prints the contents of the window to fit a page", null, commandName, "printToFit")) {
			prWindow(MesquitePrintJob.AUTOFIT);
		}
		else if (checker.compare(MesquiteWindow.class, "Sends the contents of the window to a PDF file",null,commandName,"printToPDF")) {
			pdfWindow(MesquitePrintJob.AUTOFIT);
		}
		else if (checker.compare(MesquiteWindow.class, "Opens the print window (for cases where the printing calls are separated)", null, commandName, "openPrintWindowToFit")) {
			MainThread.incrementSuppressWaitWindow();
			openPrWindow(MesquitePrintJob.AUTOFIT);
			MainThread.decrementSuppressWaitWindow();
		}
		else if (checker.compare(MesquiteWindow.class, "Opens the print window (for cases where the printing calls are separated)", null, commandName, "openPrintWindow")) {
			MainThread.incrementSuppressWaitWindow();
			openPrWindow(MesquitePrintJob.NATURAL);
			MainThread.decrementSuppressWaitWindow();
		}
		else if (checker.compare(MesquiteWindow.class,  "Prints the window (for cases where the printing calls are separated)", null, commandName, "doPrintWindow")) {
			MainThread.incrementSuppressWaitWindow();
			doPrWindow();
			MainThread.decrementSuppressWaitWindow();
		}
		else if (checker.compare(MesquiteWindow.class,  "Closes the print window (for cases where the printing calls are separated)", null, commandName, "closePrintWindow")) {
			MainThread.incrementSuppressWaitWindow();
			closePrWindow();
			MainThread.decrementSuppressWaitWindow();
		}
		else if (checker.compare(MesquiteWindow.class, "Returns the tool palette of the window", null, commandName, "getToolPalette")) {
			return graphics[0].getPalette();
		}
		else if (checker.compare(MesquiteWindow.class, "Clones the window if possible", null, commandName, "cloneWindow")) {
			MesquiteModule cloned = (MesquiteModule)ownerModule.getEmployer().cloneEmployee(ownerModule);
		}
		else if (checker.compare(MesquiteWindow.class, "Saves a macro that serves as a macro to reproduce an analysis or window", null, commandName, "saveMacroForWindow")) {
			saveWindowMacro(0);
		}
		else if (checker.compare(MesquiteWindow.class, "Saves a macro that serves as a macro to reproduce an analysis or window", null, commandName, "saveMacroForAnalysis")) {
			saveWindowMacro(MesquiteMacro.ANALYSIS);
		}
		else if (checker.compare(MesquiteWindow.class, "Saves the text version of the window to a file", "[file name]", commandName, "saveTextToFile")) {
			if (StringUtil.blank(arguments)) {
				arguments = MesquiteFile.saveFileAsDialog("Save window text");
				if (StringUtil.blank(arguments))
					return null;
				return saveTextToFile(arguments, false);
			}
			else
				return saveTextToFile(MesquiteFile.composePath(ownerModule.getProject().getHomeDirectoryName(),ParseUtil.getFirstToken(arguments, pos)), false);

		}
		else if (checker.compare(MesquiteWindow.class, "Appends the text version of the window to an existing file", "[file name]", commandName, "appendTextToFile")) {
			if (StringUtil.blank(arguments)) {
				arguments = MesquiteFile.saveFileAsDialog("Append window text");
				if (StringUtil.blank(arguments))
					return null;
				return saveTextToFile(arguments, true);
			}
			else
				return saveTextToFile(MesquiteFile.composePath(ownerModule.getProject().getHomeDirectoryName(),ParseUtil.getFirstToken(arguments, pos)), true);
		}
		else if (checker.compare(MesquiteWindow.class, "Toggles whether the information bar at the top of the window is shown or not", null, commandName, "toggleInfoBar")) { 
			//setShowInfoBar(!showInfoBar); //todo why are this and the following two not replaced by a single setInfoBar [on or off]???
		}
		else if (checker.compare(MesquiteWindow.class, "Shows the information bar at the top of the window", null, commandName, "onInfoBar")) { 
			//	if (!showInfoBar)
			//		setShowInfoBar(true);
		}
		else if (checker.compare(MesquiteWindow.class, "Hides the information bar at the top of the window", null, commandName, "offInfoBar")) { 
			//	if (showInfoBar)
			//		setShowInfoBar(false);
		}
		else if (checker.compare(MesquiteWindow.class, "Executes the macro file", "[file name]", commandName, "doMacro")) { 
			if (ownerModule!=null) {
				Puppeteer p = new Puppeteer(ownerModule);
				p.applyMacroFile(ParseUtil.getFirstToken(arguments, pos), ownerModule);
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Returns the module owning the window", null, commandName, "getOwnerModule")) { 
			return ownerModule;
		}
		else if (checker.compare(MesquiteWindow.class, "Sets the size of the window", "[width in pixels][height in pixels]", commandName, "setSize")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int width= MesquiteInteger.fromString(arguments, io);
			int height= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(width) && MesquiteInteger.isCombinable(height)) {
				fromScriptCommand = true;//this is needed to counteract difficulties with popping in/out and size setting in window constructors
				setWindowSize(width, height);
				fromScriptCommand = false;
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Sets the height of the explanation area", "[height in pixels]", commandName, "setExplanationSize")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int height= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(height) && height>=0) {
				fromScriptCommand = true;//this is needed to counteract difficulties with popping in/out and size setting in window constructors
				setShowExplanation(height !=0,  height);
				fromScriptCommand = false;
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Sets the height of the annotation area", "[height in pixels]", commandName, "setAnnotationSize")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int height= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(height) && height>=0) {
				fromScriptCommand = true;//this is needed to counteract difficulties with popping in/out and size setting in window constructors
				setShowAnnotation(height !=0,  height);
				fromScriptCommand = false;
			}
		}
		/*
    	 	else if (checker.compare(MesquiteWindow.class, "Sets the annotation for the window.", "[annotation]", commandName, "setAnnotation")) {
   	 		if (useAnnotatableForAnnotation)
   	 			return null;
   	 		MesquiteInteger io = new MesquiteInteger(0);
   	 		String note = ParseUtil.getToken(arguments, io);
   	 		if (!StringUtil.blank(note)) {
   	 			windowAnnotation.setValue(note);
   	 			if (windowAnnotation == defaultAnnotation)
   	 				setAnnotation(windowAnnotation, false, null);

   	 		}
    	 	}
		 */
		else if (checker.compare(MesquiteWindow.class, "Sets the location of the window", "[x coordinate of upper left][y coordinate of upper left]", commandName, "setLocation")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(x) && MesquiteInteger.isCombinable(y)) {
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				if (x> screenSize.width-16)
					x=screenSize.width-64;
				else if (x+getBounds().width<16)
					x= 16 - getBounds().width;
				if (y> screenSize.height-16)
					y=screenSize.height-64;
				else if (y+getBounds().height<16)
					y= 16 - getBounds().height;
				if (y<0)
					y=0;
				if (MesquiteTrunk.isMacOSX() && y<22 && x<0)  //workaround for bug in OS X; June 2004
					x=0;
				setWindowLocation(x, y, false, true);
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Sets the font of the window", "[name of font]", commandName, "setFont")) {
			String fontName = ParseUtil.getFirstToken(arguments, pos);
			if (fontName.equalsIgnoreCase(FontUtil.otherFontArgument)) {
				fontName = FontUtil.getFontNameFromDialog(this);
				if (fontName!=null) 
					ownerModule.logln("Font chosen: " + fontName);
			}
			if (fontName!=null)
				setWindowFont(fontName);
		}
		else if (checker.compare(MesquiteWindow.class, "Sets the font size of the window", "[font size]", commandName, "setFontSize")) {
			int fontSize = MesquiteInteger.fromString(arguments);
			if (!setWindowFontSize(fontSize))
				return null;
		}
		else if (checker.compare(MesquiteWindow.class, "Sets the font size difference of the explanation area", "[font increment]", commandName, "setFontIncExp")) {
			int fontInc = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(fontInc) && fontInc< 100 && fontInc > -100) {
				explanationArea.fontIncrement= fontInc;
				explanationArea.resetFont();
			}
			return null;
		}
		else if (checker.compare(MesquiteWindow.class, "Sets the font size difference of the annotations area", "[font increment]", commandName, "setFontIncAnnot")) {
			int fontInc = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(fontInc) && fontInc< 100 && fontInc > -100) {
				annotationArea.fontIncrement= fontInc;
				annotationArea.resetFont();
			}
			return null;
		}
		else if (checker.compare(MesquiteWindow.class, "Dumps to the log a list of the employees of the owner module", null, commandName, "listEmployees")) {
			if (ownerModule!=null)
				ownerModule.logln(ownerModule.listEmployees(""));
		}
		else if (checker.compare(this.getClass(), "Selects tree", "[number of tree][id of tree block]", commandName, "selectTree")) {
			int whichPart = MesquiteInteger.fromFirstToken(arguments, pos);
			int whichBlock = MesquiteInteger.fromString(arguments, pos);
			selectPart(TreeVector.class, whichPart, whichBlock);		
			return null;
		}
		else if (checker.compare(this.getClass(), "Touches taxon (selects it and shows it if possible)", "[number of taxon][id of taxa block]", commandName, "touchTaxon")) {
			int whichPart = MesquiteInteger.fromFirstToken(arguments, pos);
			int whichBlock = MesquiteInteger.fromString(arguments, pos);
			selectPart(Taxa.class, whichPart, whichBlock);
			Projects projects = MesquiteTrunk.getProjectList();
			for (int i = 0; i<projects.getNumProjects(); i++){
				MesquiteProject project = projects.getProject(i);
				FileElement elem = project.getElementByID(Taxa.class, whichBlock);
				if (elem != null && elem instanceof Taxa){
					//taxa block found.  Tell all windows in project to select that 
					Enumeration e = MesquiteTrunk.mesquiteTrunk.windowVector.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						MesquiteWindow mw = (MesquiteWindow)obj;
						if (mw.isVisible() && mw.getOwnerModule()!=null && mw.getOwnerModule().getProject()== project)
							mw.doCommand("showTaxon", "" + whichBlock + " " + whichPart, CommandChecker.quietDefaultChecker);
					}
				}
			}

			return null;
		}
		else if (checker.compare(this.getClass(), "Selects character", "[number of character][id of character matrix]", commandName, "selectCharacter")) {
			int whichPart = MesquiteInteger.fromFirstToken(arguments, pos);
			int whichBlock = MesquiteInteger.fromString(arguments, pos);
			selectPart(mesquite.lib.characters.CharacterData.class, whichPart, whichBlock);		
			return null;
		}
		else {
			//AFTERDEMO:
			if (commandName!=null && !checker.getAccumulateMode() && checker.warnIfNoResponse) {
				MesquiteMessage.warnProgrammer("Window " + getName() + " did not respond to command " + commandName + " with arguments (" + arguments + ")");
			}
		}
		return null;
	}


	private void selectPart(Class c, int whichPart, int whichBlock){
		Projects projects = MesquiteTrunk.getProjectList();
		for (int i = 0; i<projects.getNumProjects(); i++){
			MesquiteProject project = projects.getProject(i);
			FileElement elem = project.getElementByID(c, whichBlock);
			if (elem != null){
				elem.setSelected(whichPart, !elem.getSelected(whichPart));
				elem.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
	}

	private void saveWindowMacro(int preferredMenu){
		MesquiteModule mb = ownerModule.getEmployer();
		if (mb == null)
			return;
		String cloneCommand = mb.getClonableEmployeeCommand(ownerModule);

		String beforeTell = "";
		String afterTell = "endTell;\n";

		while (mb !=null && mb.getModuleWindow()==null) {
			beforeTell = "getEmployee " + "#" + mb.getClass().getName()  + ";\ntell It;\n" + beforeTell;
			afterTell += "endTell;\n";

			mb = mb.getEmployer();
		}
		MesquiteModuleInfo mmi = mb.getModuleInfo();
		if (mmi == null)
			return;
		if (StringUtil.blank(cloneCommand))
			beforeTell += "getEmployee " + "#" + ownerModule.getClass().getName() + ";\ntell It;\n";
		else
			beforeTell += cloneCommand + "\ntell It;\n";
		String recipe =  beforeTell;
		recipe += Snapshot.getSnapshotCommands(ownerModule, null, "");
		recipe +=  afterTell;
		MesquiteMacro.saveMacro(mb, "Untitled Macro for Window", preferredMenu, recipe);
	}
	/*.................................................................................................................*/

	public String getName(){
		return getTitle();
	}

	/*.................................................................................................................*/
	Image icon;
	public Image getIcon(){
		return icon;
	}
	public void setIcon(String s){
		icon = MesquiteImage.getImage(s);
	}

	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public abstract void resetTitle();
	/*.................................................................................................................*/
	public static void resetAllTitles(){
		Enumeration e = MesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			mw.resetTitle();
			mw.infoBar.repaint();
		}
	}
	/*.................................................................................................................*/
	public static void repaintAllSearchStrips(){
		/*
		 * Enumeration e = MesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw.infoBar!= null && mw.infoBar.isVisible())
				mw.infoBar.repaintSearchStrip();
		}
		 */
		MesquiteTrunk.mesquiteTrunk.repaintSearchStrip();
	}

	/** to be called to add to log*/
	public final void logln(String s) {
		if (logText == null)
			logText = new StringBuffer("Log for window" + getTitle() +  "\n");
		logText.append(s + "\n");
	}
	/*.................................................................................................................*/
	/** to be called to add to log*/
	public final void log(String s) {
		if (logText == null)
			logText = new StringBuffer("Log for window" + getTitle() +  "\n");
		logText.append(s);
	}
	/*.................................................................................................................*/
	/** to be called to add to log*/
	public final String getLogText() {
		if (logText == null)
			logText = new StringBuffer("Log for window" + getTitle() +  "\n");
		return logText.toString();
	}
	/*.................................................................................................................*/
	public void hide(){
		if (parentFrame != null)
			parentFrame.hide(this);
		if (!suppressAllWindows && autoShow && noWindowsShowing() &&  !MesquiteTrunk.mesquiteTrunk.isDoomed()) {
			MesquiteModule.mesquiteTrunk.doCommand("showAbout","", CommandChecker.defaultChecker);
			if (!AlertDialog.query(MesquiteModule.mesquiteTrunk.containerOfModule(), "How to Quit", "If you hadn't intended to quit Mesquite, select \"Go back to Mesquite\"..  Because Mesquite needs at least one window open, the Mesquite window will be shown.  If you want to Force Quit right now, select \"Force Quit\" now (the standard and safer way to quit Mesquite is to select Quit Mesquite from the File menu).", "Go back to Mesquite", "Force Quit"))
				MesquiteModule.mesquiteTrunk.doCommand("goAwayForceQuit",null, CommandChecker.defaultChecker);
		}
	}

	/**/
	public boolean disposed() {
		return wasDisposed;
	}
	public void waitUntilDisposable() {
		/*int pp = 0;
		while (painting>0) {
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				return;
			}
			if (++pp % 5 == 0){ //TODO: should be 1000 ??
				MesquiteMessage.warnProgrammer("Waiting (" + pp + ") to dispose window " + getName() + " " +painting);
			}
			if (pp > 15)
				return;
		}
		 */
		if (painting>0) {
			try {
				Thread.sleep(50); //pause to allow some time to clean up
			}
			catch (InterruptedException e) {
				return;
			}
		}
	}
	private void disposeAll(Component c){
		if (c==null)
			return;
		if (c instanceof MousePanel)
			((MousePanel)c).dispose();
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					disposeAll(cc[i]);
		}

	}
	/*.................................................................................................................*/
	public void dispose() {
		removeAll();
		disposing = true;
		numDisposing++;
		if (ownerModule != null && ownerModule.getProject()!=null && ownerModule.getProject().activeWindowOfProject == this)
			ownerModule.getProject().activeWindowOfProject = null;
		hide();
		waitUntilDisposable();
		if (graphics != null)
			disposeAll(graphics[0]);
		disposeAll(palette);
		disposeAll(explanationArea);
		disposeAll(annotationArea);
		if (windowTimer!=null)
			windowTimer.end();
		if (graphics != null)
			removeKeyListener(graphics[0], currentTool); 
		if (annotationArea!=null)
			annotationArea.dispose();
		if (explanationArea!=null)
			explanationArea.dispose();
		annotationArea.setExplanation((Annotatable)null); 
		if (infoBar!=null)
			infoBar.dispose();
		if (ownerModule!=null)
			ownerModule.setModuleWindow(null);
		MesquiteModule.mesquiteTrunk.windowVector.removeElement(this, false);
		disposeMenuComponent(menuBar);
		setMenuBar(null);



		if (parentFrame != null)
			parentFrame.removePage(this);
		ETContentArea etc = (ETContentArea)graphics[1];
		outerContents.dispose();
		if (infoBar!=null)
			infoBar.dispose();
		if (etc!=null)
			etc.dispose();
		menuBar=null;
		wasDisposed = true;
		ownerModule = null;
		currentObject = null;
		currentTool = null;
		previousTool = null;


		/*if (infoBarMenuItem !=null) {
			infoBarMenuItem.disconnectable = true;
			infoBarMenuItem.disconnect();
		}*/
		if (showCommand!=null){
			showCommand.dispose();
			showInfoBarCommand.dispose();
			saveAsTextCommand.dispose();
			printCommand.dispose();
			printToFitCommand.dispose();
			printToPDFCommand.dispose();
			setFontCommand.dispose();
			setFontSizeCommand.dispose();
			listEmployeesCommand.dispose();
			doMacroCommand.dispose();
			showExplanationsCommand.dispose();
			showSnapshotCommand.dispose();
			sendScriptCommand.dispose();
			showFileCommand.dispose();
			closeWindowCommand.dispose();
		}
		showCommand=null;
		showInfoBarCommand=null;
		saveAsTextCommand.dispose();
		printCommand=null;
		printToFitCommand = null;
		printToPDFCommand = null;
		setFontCommand=null;
		setFontSizeCommand =null;
		listEmployeesCommand =null;
		doMacroCommand = null;
		showExplanationsCommand = null;
		showSnapshotCommand = null;
		sendScriptCommand = null;
		showFileCommand = null;
		closeWindowCommand = null;
		MesquiteWindow.totalDisposed++;

		//infoBarMenuItem =null;

		if (parentFrame != null)
			parentFrame.dispose(this);
		parentFrame = null;
		boolean done = false;
		numDisposing--;
		/*	while (!done){  //dangerous? but addressed bugs in OS X!
			try {
				parentFrame.dispose();
				done = true;
			}
			catch (Exception e){
				MesquiteMessage.warnProgrammer("Exception in disposing window " + e);
				MesquiteFile.throwableToLog(this, e);
				done = true;
			}
		}
		 */
	}
	public void finalize() throws Throwable {
		if (MesquiteTrunk.checkMemory && classesCreated.indexOf(getClass())>=0) {
			MesquiteInteger c = (MesquiteInteger)countsOfClassesFinalized.elementAt(classesCreated.indexOf(getClass()));
			if (c!=null)
				c.increment();
		}
		totalFinalized++;
		super.finalize();
	}
	/*.................................................................................................................*/
	/** windows with manually resized components (i.e. null layoutmanagers) can override to respond to window resizing here */
	public void windowResized(){
	}
	/*.................................................................................................................*/
	public void buttonHit(String label, Button button){
		//buttons can use this to say to the window they've been hit
	}
	/*.................................................................................................................*/
	protected void closeWindow(){
		if (!(this instanceof SystemWindow)){
			if (ownerModule!=null) {
				if (getOwnerModule().isDoomed() || disposing)
					return;
				if (autoShow && noWindowsShowing(this, ownerModule.getProject())) {
					FileCoordinator p = ownerModule.getFileCoordinator();
					if (p!=null)  {
						int response=2;
						response = AlertDialog.query(MesquiteModule.mesquiteTrunk.containerOfModule(), "Close Project?", "You have just requested to close the last open window of a project.  If you want to close the project, select \"Close Project\".  Otherwise, select \"Continue\", which will bring up a small window to represent the project.", "Continue", "Close Project", "Cancel", 3);
						if (response==1){  //hit close project;
							p.closeFile(p.getProject().getHomeFile());
							return;
						} else if (response==2) //cancel
							return;
					}
					ownerModule.windowGoAway(this);
					if (p!=null) {
						p.doCommand("showWindow","", CommandChecker.defaultChecker);
					}
					else
						MesquiteMessage.warnProgrammer("file coord null");
				}
				else 
					ownerModule.windowGoAway(this);
			}
			else {
				hide();
				dispose();
			}
		}
		else
			MesquiteMessage.println("This window cannot be closed without quitting Mesquite");
		if (!suppressAllWindows && autoShow && noWindowsShowing()) {
			MesquiteModule.mesquiteTrunk.doCommand("showAbout","", CommandChecker.defaultChecker);
			if (!AlertDialog.query(MesquiteModule.mesquiteTrunk.containerOfModule(), "How to Quit", "If you hadn't intended to quit Mesquite, select \"Go back to Mesquite\"..  Because Mesquite needs at least one window open, the Mesquite window will be shown.  If you want to Force Quit right now, select \"Force Quit\" now (the standard and safer way to quit Mesquite is to select Quit Mesquite from the File menu).", "Go back to Mesquite", "Force Quit"))
				MesquiteModule.mesquiteTrunk.doCommand("goAwayForceQuit",null, CommandChecker.defaultChecker);
		}
	}
	boolean noWindowsShowing(MesquiteWindow w, MesquiteProject proj){
		if (proj==null)
			return noWindowsShowing();
		if (MesquiteModule.mesquiteTrunk.windowVector.size() == 0)
			return true;
		else {
			Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				MesquiteWindow win = (MesquiteWindow)e.nextElement();
				if (win != w && !win.minimalMenus && win.isVisible() && win.ownerModule!=null && win.ownerModule.getProject()==proj)
					return false;
			}
			return true;
		}
	}
	boolean noWindowsShowing(){
		if (MesquiteModule.mesquiteTrunk.windowVector.size() == 0)
			return true;
		else {
			Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
			while (e.hasMoreElements()) {
				MesquiteWindow win = (MesquiteWindow)e.nextElement();
				if (!win.minimalMenus && win.isVisible())
					return false;
			}
			return true;
		}
	}
	/*.................................................................................................................*/
	/** This requests that simplicity strips be repainted.*/
	public static final void resetAllSimplicity() {
		Enumeration e = MesquiteModule.mesquiteTrunk.windowVector.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteWindow mw = (MesquiteWindow)obj;
			if (mw!=null) {
				InfoBar ib = mw.infoBar;
				if (ib != null) {
					SimplicityStrip ss = ib.simplicityStrip;	
					if (ss != null){
						/*if (InterfaceManager.locked)
							ss.setVisible(false);
						else {*/
						if (!ss.isVisible())
							ss.setVisible(true);
						ss.repaint();
						//}
					}
				}
			}
		}
		repaintAllWindows();
		InterfaceManager.resetSimplicity();
	}
	String title = "Mesquite Window";
	public void setTitle(String name) {
		if (title != null && name != null && title.equals(name))
			return;
		title = name;
		if (parentFrame != null)
			parentFrame.windowTitleChanged(this);
	}
	public String getTitle() {
		return title;
	}
	public void setResizable(boolean yes) {
		if (parentFrame!=null)
			parentFrame.setResizable(yes);
	}


	PopUpPanel quickPanel;
	public void showQuickMessage( int xPixel, int yPixel,  String message){
		showQuickMessage(getGraphicsArea(), null,xPixel, yPixel, message);
	}
	public void showQuickMessage(Panel panel, int xPixel, int yPixel,  String message){
		showQuickMessage(panel, null,xPixel, yPixel, message);
	}
	public void showQuickMessage(Panel panel, Rectangle rect,int xPixel, int yPixel,  String message){
		Rectangle boundingBox = rect;
		if (boundingBox==null)
			boundingBox = panel.getBounds();
		int boxWidth = 96;
		if (StringUtil.blank(message))
			return;
		if (quickPanel==null) {
			panel.add(quickPanel = new PopUpPanel());
			quickPanel.setBackground(ColorDistribution.sienna);
		}
		quickPanel.setText(message, panel.getFont(), boxWidth);
		int offsetRight = 12;
		StringInABox sb = quickPanel.getBox();
		int xOffset = boundingBox.x;
		int yOffset = boundingBox.y;
		int xPos=0;
		if (xPixel+boxWidth+8 + offsetRight>boundingBox.width) 
			xPos = xPixel-boxWidth + xOffset;
		else
			xPos = xPixel+4 + xOffset + offsetRight;
		int yPos=0;

		if (yPixel+sb.getHeight()>boundingBox.height + yOffset && yPixel-sb.getHeight() + yOffset>=0)
			yPos = yPixel-sb.getHeight() + yOffset;
		else
			yPos = yPixel + yOffset;
		quickPanel.setSize(boxWidth +2, sb.getHeight() +2);
		quickPanel.setLocation(xPos, yPos);
		quickPanel.setVisible(true);
		quickPanel.repaint();

	}
	/* ----------------------------------*/
	public void hideQuickMessage(MesquiteChart chart){
		if (quickPanel!=null)
			quickPanel.setVisible(false);
	}
}



class PopUpPanel extends Panel {
	String t;
	StringInABox sb;
	void setText(String s, Font f, int w){
		t = s;
		sb = new StringInABox(t, f, w);
	}
	StringInABox getBox(){
		return sb;
	}
	public void paint(Graphics g){
		if (sb == null)
			return;

		sb.drawInBox(g, getBackground(), 3, 1);
		g.setColor(Color.black);
		g.drawRect(0,0,getBounds().width-1, getBounds().height-1);
	}
}




/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import mesquite.lib.duties.*;
import mesquite.lib.simplicity.InterfaceManager;
/* ======================================================================== */
/** an intermediary class that can be changed to extend Panel versus Frame, to allow embedding versus not 
UNEMBEDDED VERSION */
public class MesquiteFrame extends Frame implements Commandable {

	Vector windows;
	Vector orderedWindows;
	MesquiteWindow frontWindow;
	private CardLayout mainLayout, resourcesLayout, poptileLayout;
	MesquiteProject project;
	MesquiteWindow projectWindow;
	MesquiteModule ownerModule;
	static int numTotal = 0;

	int num = 0;
	int id = 0;
	private Panel main;
	private Panel resources;
	private Panel  poptile;
	private BetweenPanel rBetweenPanel;
	private BetweenPanel pBetweenPanel;
	private static int BETWEENWIDTH = 6;
	public static final int RESOURCES = 0;  // for project
	public static final int MAIN = 1;
	public static final int POPTILE = 2;
	boolean resourcesFullWindow = false; //0 for closed; 1 for open partly; 2 for full window;
	boolean resourcesClosedWhenMinimized = false; //0 for closed; 1 for open partly; 2 for full window;
	int resourcesWidth = 0;
	int poptileWidth = 400;
	public static int defaultResourcesWidth = 100; 
	public static boolean respectFileSpecificResourceWidth = true; 
	public static int resourcesFontSize = 10;
	FrameTabsPanel tabs;
	int tabHeight = 36;
	boolean isPrimarylMesquiteFrame = false;  //true if it's the main window with projects/log/search
	boolean compacted = false;
	public static long totalCreated = 0;
	public static long totalDisposed = 0;
	public static long totalFinalized  = 0;
	Color backgroundColor;
	public MesquiteFrame(boolean compactible, Color backgroundColor) {
		super();
		this.backgroundColor = backgroundColor;
		totalCreated++;
		id = numTotal++;
		if (MesquiteTrunk.isMacOSXPanther33())
			compacted = false;
		else
			compacted = compactible;
		windows = new Vector();
		orderedWindows = new Vector();
		oldInsetTop=oldInsetBottom=oldInsetRight= oldInsetLeft= -1; 
		/**/
		setResizable(true);
		setBackground(backgroundColor);

		setLayout(null);
		rBetweenPanel = new BetweenPanel(this);
		add(rBetweenPanel);
		pBetweenPanel = new BetweenPanel(this);
		add(pBetweenPanel);
		poptile = new Panel();
		poptile.setBackground(backgroundColor);
		add(poptile);
		resources = new Panel();
		resources.setBackground(backgroundColor);
		add(resources);
		main = new Panel();
		main.setBackground(backgroundColor);
		if (compactible){
			tabs = new FrameTabsPanel(this);
			add(tabs);
		}
		else tabHeight = 0;
		add(main);


		resetSizes(true);
		main.setLayout(mainLayout = new CardLayout());
		resources.setLayout(resourcesLayout = new CardLayout());
		poptile.setLayout(poptileLayout = new CardLayout());
		addComponentListener(new MWCE(this));

		/* EMBEDDED if embedded remove this */
		addWindowListener(new MWWE(this));
	}
	public void finalize() throws Throwable {
		totalFinalized++;
		super.finalize();
	}
	public void setAsPrimaryMesquiteFrame(boolean p){
		isPrimarylMesquiteFrame = p;
	}
	public void setResizable(boolean r){
		if (project != null && !r)
			return;
		super.setResizable(r);
	}
	public int getID(){
		return id;
	}
	public MesquiteModule getOwnerModule(){ //mesquite or basic file coordinator
		return ownerModule;
	}
	public void setOwnerModule(MesquiteModule mb){ //mesquite or basic file coordinator
		ownerModule = mb;
		if (ownerModule != null)
			project = ownerModule.getProject();
	}

	/*.................................................................................................................*/
	boolean alreadyDisposed = false;
	public void dispose() {
		if (alreadyDisposed)
			return;
		removeAll();
		alreadyDisposed = true;
		if (activeWindow == this)
			activeWindow = null;
		totalDisposed++;

		super.dispose();
		ownerModule = null;
		if (project != null && project.getFrame() == this) {
			project.setFrame(null);
		}
		project = null;
		windows = null;
	}
	public void setMenuBar(MesquiteWindow which, MenuBar mbar) {
		if (which == frontWindow) {
			try {
			super.setMenuBar(mbar);
			which.repaintInfoBar();
			}
			catch(Exception e){
			}
		}
		checkInsets(true);
	}
	public int getNumWindows(){
		return windows.size();
	}
	public int getNumWindows(int location){
		int count =0;
		for (int i=orderedWindows.size()-1; i>=0; i--){
			MesquiteWindow w = (MesquiteWindow)orderedWindows.elementAt(i);
			if (w.getTileLocation() == location)
				count++;
		}
		return count;
	}
	public void windowTitleChanged(MesquiteWindow w) {
		if (windows.size() == 1)
			setTitle(w.getTitle());
		else if (w instanceof SystemWindow)
			setTitle("Mesquite");
		else if (project == null)
			setTitle("Mesquite Window");
		else if (project.hasName())
			setTitle(project.getName());
		else
			setTitle(project.getHomeFileName());
		if (tabs !=null)
			tabs.repaint();
	}
	public void setResourcesState(boolean resourcesFullWindow, boolean resourcesClosedWhenMinimized, int resourcesWidth){
		this.resourcesFullWindow = resourcesFullWindow; 
		this.resourcesClosedWhenMinimized = resourcesClosedWhenMinimized; 
		this.resourcesWidth = resourcesWidth;
		MesquiteWindow w = frontMostInLocation(RESOURCES);
		if (w != null)
			w.setMinimized(resourcesClosedWhenMinimized);
		if (tabs != null)
			tabs.repaint();
		resetSizes(true);
	}
	public boolean getResourcesFullWindow(){
		return resourcesFullWindow; 
	}
	public boolean getResourcesClosedWhenMinimized(){
		return resourcesClosedWhenMinimized; 
	}
	public int getResourcesWidth(){
		return resourcesWidth; 
	}
	public void setPopoutWidth(int poptileWidth){
		this.poptileWidth = poptileWidth;
		if (tabs != null)
			tabs.repaint();
		resetSizes(true);
	}
	public int getPopoutWidth(){
		return poptileWidth; 
	}
	public void removePage(MesquiteWindow w){
		if (w.getTileLocation()==MAIN)
			main.remove(w.outerContents);
		else if (w.getTileLocation()==RESOURCES)
			resources.remove(w.outerContents);
		else if (w.getTileLocation()==POPTILE)
			poptile.remove(w.outerContents);
		setAsFrontWindow(null);
		if (windows != null){
			windows.removeElement(w);
			orderedWindows.removeElement(w);
			showFrontWindow();
		}
	}
	/*.................................................................................................................*/
	public void addPage(MesquiteWindow w){
		addPage(w, w.getTileLocation());
	}
	/*.................................................................................................................*/
	public void addPage(MesquiteWindow w, int where){

		w.inParent();

		w.outerContents.setBackground(ColorTheme.getInterfaceElement());
		setBackground(ColorTheme.getInterfaceElement());
		//	if (tabs !=null)
		//		tabs.setBackground(light);
		String id = Integer.toString(w.getID());
		if (where == MAIN){
			main.add(w.outerContents, id);
			mainLayout.addLayoutComponent(w.getOuterContentsArea(), id);
			w.setTileLocation(MAIN);
		}
		else if (where == POPTILE){
			poptile.add(w.outerContents, id);
			poptileLayout.addLayoutComponent(w.getOuterContentsArea(), id);
			w.setTileLocation(POPTILE);
		}
		else if (where == RESOURCES){
			resourcesWidth = defaultResourcesWidth;
			resources.add(w.outerContents, id);
			projectWindow = w;
			resourcesLayout.addLayoutComponent(w.getOuterContentsArea(), id);
			w.setTileLocation(RESOURCES);
			w.setMinimized(resourcesClosedWhenMinimized);
		}
		windows.addElement(w);

		orderedWindows.addElement(w);
		setAsFrontWindow(w);
		resetSizes(true);
		if (tabs !=null)
			tabs.repaint();
		if (windows.size() == 1)
			setTitle(w.getTitle());
		else if (w instanceof SystemWindow)
			setTitle("Mesquite");
		else if (project == null)
			setTitle("Mesquite Window");
		else if (project.hasName())
			setTitle(project.getName());
		else
			setTitle(project.getHomeFileName());

	}
	public void incrementPanelWidth(BetweenPanel p, int w){
		if (p == rBetweenPanel){
			if (w+ resourcesWidth<3)
				return;
			resourcesWidth = w + resourcesWidth;

			resetSizes(true);
			defaultResourcesWidth = resourcesWidth;
			MesquiteTrunk.mesquiteTrunk.storePreferences();
		}
		else if (p == pBetweenPanel){
			if (poptileWidth-w<50)
				return;
			poptileWidth = poptileWidth-w;
			resetSizes(true);
		}
	}
	/*.................................................................................................................*/
	public boolean windowPresent(MesquiteWindow window){
		for (int i = 0; i<windows.size(); i++){
			MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
			if (w == window)
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	private MesquiteWindow findWindow(String s){
		for (int i = 0; i<windows.size(); i++){
			MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
			if (Integer.toString(w.getID()).equals(s))
				return w;
		}
		return null;
	}
	/*.................................................................................................................*/
	public void showPage(MesquiteWindow w){
		if (w == frontWindow)
			return;
		showPage(Integer.toString(w.getID()));
		setAsFrontWindow(w);

	}
	public void showFirstPage(){
		mainLayout.first(main);
	}
	public void showNextPage(){
		mainLayout.next(main);

	}
	/*.................................................................................................................*/
	public void showPage(int i){
		if (i <0 || i>=windows.size())
			return;
		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
		if (w == frontWindow)
			return;
		showPage(Integer.toString(w.getID()));

	}

	/*.................................................................................................................*/
	public void showPage(String s){
		MesquiteWindow w = findWindow(s);
		if (w == null){
			MesquiteMessage.printStackTrace("Attempting to show page; page not found " + s);
			return;
		}
		showInLayout(w.getTileLocation(), s);
		setAsFrontWindow(w);
		resetSizes(true);
		validate();
		if (tabs !=null)
			tabs.repaint();
		setMenuBar(w, w.getMenuBar());

	}
	void showInLayout(int location, String s){
		try {
		if (location == MAIN) {
			if (resourcesFullWindow){
				resourcesFullWindow = false;
				resetSizes(true);
			}
			mainLayout.show(main, s);
		}
		else if (location == POPTILE) {
			if (resourcesFullWindow){
				resourcesFullWindow = false;
				resetSizes(true);
			}
			poptileLayout.show(poptile, s);
		}
		else if (location == RESOURCES) {
			if (!resourcesFullWindow){
				resourcesFullWindow = true;
				resetSizes(true);
			}
			resourcesLayout.show(resources, s);
		}
		}
		catch (Throwable t){
			MesquiteMessage.warnProgrammer("Exception or Error in showInLayout (MesquiteFrame); details in Mesquite log file.");
			MesquiteFile.throwableToLog(this, t);
		}
	}
	/*.................................................................................................................*/
	public void dispose(MesquiteWindow w){
		if (windows == null || windows.size()==0)
			dispose();
	}
	void closeWindowRequested(){
		if (isPrimarylMesquiteFrame){
			if (MesquiteTrunk.mesquiteTrunk.projects.getNumProjects() == 0)
				MesquiteTrunk.mesquiteTrunk.doCommand("quit", null, CommandChecker.defaultChecker);
			else {
				toBack();
				MesquiteTrunk.mesquiteTrunk.doCommand("showAllWindows", null, CommandChecker.defaultChecker);

			}
		}
		else if (ownerModule != null && ownerModule.getProject() != null && compacted){

			MesquiteProject proj = ownerModule.getProject();
			FileCoordinator coord = ownerModule.getFileCoordinator();
			coord.closeFile(proj.getHomeFile());

		}

		else if (frontWindow != null && frontWindow.closeWindowCommand != null)
			frontWindow.closeWindowCommand.doIt(null); //this might be best done on separate thread, but because of menu disappearance bug after closing a window in Mac OS, is done immediately
	}

	/*.................................................................................................................*/
	boolean permitGoAway(int i){
		if (windows == null)
			return false;
		if (i <0 || i>=windows.size())
			return false;
		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
		if(w.getTileLocation() == RESOURCES  && !resourcesFullWindow)
			return false;  //don't show goaway if resources and not frontmost

		return !(w instanceof SystemWindow);
	}
	/*.................................................................................................................*/
	boolean goAwayOrShow(int i){
		if (!permitGoAway(i))
			return false;
		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);

		if(w.getTileLocation() != RESOURCES || resourcesFullWindow)
			return true;  //show goaway if not resources or if now full window

		if (resourcesClosedWhenMinimized)  //not full window; therefore closed
			return false;
		return true;

	}
	/*.................................................................................................................*/
	boolean showMinimizeMaximize(int i){
		if (windows == null || i <0 || i>=windows.size())
			return false;
		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
		if (w.getTileLocation() == RESOURCES){  //resources; different rules
			return !resourcesFullWindow;
		}
		return !(w instanceof SystemWindow);
	}
	/*.................................................................................................................*/
	public boolean showPopOut(int i){
		if (i <0 || i>=windows.size())
			return false;
		if (i ==0)
			return false;

		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
		if (w.getTileLocation() == MAIN && getNumWindows(MAIN)<=1)
			return false;
		return !(w instanceof SystemWindow);
	}
	public void popOut(MesquiteWindow w, boolean makeVisible){
		if (windows.indexOf(w)<0)
			return;
		popOut(windows.indexOf(w), makeVisible);
	}

	public void popOut(int i, boolean makeVisible){
		if (windows.size() == 1)
			return;
		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
		if (w.popAsTile){
			removePage(w); //setVisible(w, false);  //remove from resources
			w.poppedOut = true;
			addPage(w, POPTILE); 
			if (makeVisible){
				setVisible(w, makeVisible);
				setAsFrontWindow(w);
				showFrontWindow();
				tabs.repaint();
			}
		}
		else {
			 w.poppedOut = true;
			MesquiteFrame parentFrame = new MesquiteFrame(false, backgroundColor);
			parentFrame.setOwnerModule(ownerModule);
			Menu fM = new MesquiteMenu("File");
			MenuBar mBar = new MenuBar();
			mBar.add(fM);
			parentFrame.setMenuBar(mBar);
			parentFrame.setLocation(getLocation().x + 50, getLocation().y + 50);
			/*			Dimension s = getSize();
			s.height -= tabHeight;
			parentFrame.storeInsets(parentFrame.getInsets());
			parentFrame.setSize(s);
			 */			Rectangle s = getBounds(w);
			 //s.height -= tabHeight;
			 parentFrame.storeInsets(parentFrame.getInsets());
			 parentFrame.setBounds(s);
			 /**/
			 parentFrame.saveFullDimensions();
			 setVisible(w, false);
			 w.setParentFrame(parentFrame);
			 w.poppedOut = true;

			 parentFrame.addPage(w); 
			 parentFrame.setVisible(makeVisible);
				MesquiteModule mb = w.getOwnerModule();
				if (mb != null){
					mb.resetContainingMenuBar();
				}
		}

	}
	public void popIn(MesquiteWindow w){

		if (windows == null || (!w.popAsTile && windows.indexOf(w)>=0))
			return;
		if (w.popAsTile){
			setVisible(w, false);  //remove from resources
			w.poppedOut = false;
			addPage(w, MAIN); 
			setVisible(w, true);
		}
		else {
			MesquiteFrame parentFrame = w.getParentFrame();
			if (parentFrame == this)
				return;
			parentFrame.setVisible(w, false);
			parentFrame.hide();
			parentFrame.dispose();
			addPage(w);
			setVisible(w, true);
			w.setParentFrame(this);
			MesquiteModule mb = w.getOwnerModule();
			if (mb != null){
				mb.resetContainingMenuBar();
			}
			w.poppedOut = false;
		}
		resetSizes(true);

	}
	/*.................................................................................................................*/
	public MesquiteWindow getMesquiteWindow(){
		return (MesquiteWindow)windows.elementAt(0); //should return frontmost
	}
	/*.................................................................................................................*/
	public void hide(MesquiteWindow w){
		setVisible(w, false);
	}
	public MesquiteWindow frontMostInLocation(int location){
		for (int i=orderedWindows.size()-1; i>=0; i--){
			MesquiteWindow w = (MesquiteWindow)orderedWindows.elementAt(i);
			if (w.getTileLocation() == location)
				return w;
		}
		return null;
	}
	/*.................................................................................................................*/
	public void windowGoAway(int i){
		if (i <0 || i>=windows.size())
			return;
		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
		if (w.getTileLocation()==RESOURCES){
			//hide(w);
			resourcesFullWindow = false;
			MesquiteWindow ww = frontMostInLocation(MAIN);
			setAsFrontWindow(ww);
			resetSizes(true);

		}
		else if (w.ownerModule != null)
			w.ownerModule.windowGoAway(w);
		else if (w.closeWindowCommand != null)
			w.closeWindowCommand.doIt(null); 
	}
	/*.................................................................................................................*/
	public void toggleMinimize(int i){
		if (i <0 || i>=windows.size())
			return;
		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
		if (w.getTileLocation() == RESOURCES){
			w.setMinimized(!w.isMinimized());
			resourcesClosedWhenMinimized = w.isMinimized();
			resetSizes(true);
			if (tabs !=null)
				tabs.repaint();
		}
	}
	public void hide(int i){
		if (i <0 || i>=windows.size())
			return;
		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
		hide(w);
	}
	/*.................................................................................................................*/
	public void setVisible(MesquiteWindow w, boolean vis){
		if (windows == null)
			return;
		if (vis){
			if (w != null && project != null && project.isDoomed)
				return;
			if (windows.indexOf(w)<0){
				//	if (!CommandRecord.getRecNSIfNull().scripting())
				addPage(w);
			}
			if (!MesquiteThread.isScripting()){
				w.readyToPaint = true;
				showInLayout(w.getTileLocation(), Integer.toString(w.getID()));

			}
			setAsFrontWindow(w);
			if (tabs !=null)
				tabs.repaint();
			try {
				validate();
			}
			catch(Exception e){
			}
			if (!isVisible())
				setVisible(true);
		}
		else {
			if (w == frontWindow){
				setAsFrontWindow(null);
			}
			if (w == frontWindow && windows.size() ==1){ //if only one left, then treat it as a whole frame hide
				setVisible(false);
				return;
			}
			try {
				if (w.getTileLocation() == MAIN){
					mainLayout.removeLayoutComponent(w.getOuterContentsArea());	
					main.remove(w.getOuterContentsArea());
				}
				else if (w.getTileLocation() == RESOURCES){
					resourcesLayout.removeLayoutComponent(w.getOuterContentsArea());	
					resources.remove(w.getOuterContentsArea());
				}
				else if (w.getTileLocation() == POPTILE){
					poptileLayout.removeLayoutComponent(w.getOuterContentsArea());	
					poptile.remove(w.getOuterContentsArea());
				}
				windows.removeElement(w);
				w.removedFromParent();
				orderedWindows.removeElement(w);
				if (orderedWindows.size() > 0 && !MesquiteThread.isScripting())
					showPage((MesquiteWindow)orderedWindows.elementAt(orderedWindows.size()-1));
				if (tabs !=null)
					tabs.repaint();
				resetSizes(true);
				if (windows.size()==0){
					setVisible(false);
				}
				else if (windows.size() == 1)
					resetSizes(true);
			}
			catch (Exception e){  //this might occur if disposing as this call is coming in
			}
		}

	}
/*
	void listWindows(String heading){
		System.out.println("))))))))" + heading);
		for (int i = 0; i< orderedWindows.size(); i++){
			System.out.println("      " + i + " -- " + ((MesquiteWindow)orderedWindows.elementAt(i)).getTitle());
		}
		if (frontWindow != null)
			System.out.println("      front -- " + frontWindow.getTitle());
	}
*/
	public void setAsFrontWindow(MesquiteWindow w){
		//	frontWindow = null;
		if (w != null && windows.indexOf(w)>=0) {
			if (w.getOwnerModule() != null && w.getOwnerModule().isDoomed())
				return;

			w.readyToPaint = true;
			frontWindow = w;
			if (project != null && windows.indexOf(project.getCoordinatorModule().getModuleWindow())>=0)
				project.activeWindowOfProject = w;
			if (orderedWindows != null && orderedWindows.indexOf(w) != orderedWindows.size()-1){
				orderedWindows.remove(w);
				orderedWindows.addElement(w);
			}
		}	
		if (w != null)
			setMenuBar(w, w.getMenuBar());
		if (tabs !=null)
			tabs.repaint();
	}

	public void showFrontWindow(){
		if (frontWindow != null)
			showInLayout(frontWindow.getTileLocation(), Integer.toString(frontWindow.getID()));	
	}
	/*.................................................................................................................*/
	/** Shows the window */
	public void setVisible(boolean vis) {
		if (doingShow)
			return;
		doingShow = true;
		if (vis){
			if (!checkInsets(false))
				resetSizes(true);
			if (!MesquiteWindow.GUIavailable || MesquiteWindow.suppressAllWindows){
				doingShow = false;
				return;
			}
		}
		super.setVisible(vis);
		doingShow = false;
		ProgressWindow.allIndicatorsToFront();
	}
	int countMenus(){
		MenuBar b = getMenuBar();
		if (b == null)
			return 0;
		return b.getMenuCount();
	}
	/*.................................................................................................................*/
	public void diagnose(){
		System.out.println("  FRAME ~~~~~~~~~ visible = " + isVisible() + " size " + getBounds().width + " " + getBounds().height + " this =" + id);
		System.out.println("  layout  = " + mainLayout);
		for (int i = 0; i<windows.size(); i++){
			MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
			System.out.println("      " + w.getClass() + " visible = " + w.isVisible() + " loc " + w.getBounds().x + " " + w.getBounds().y + " size " + w.getBounds().width + " " + w.getBounds().height);
		}
	}
	/*.................................................................................................................*/
	boolean doingShow = false;
	private int oldInsetTop, oldInsetBottom, oldInsetRight, oldInsetLeft;

	int savedX = 0;
	int savedY = 0;
	int savedW = 0;
	int savedHWithoutTabs = 0;
	int savedHWithTabs = 0;
	int savedFullW = 0;
	int savedFullH = 0;
	/*.................................................................................................................*
	public void show(){
		if (doingShow)
			return;
		setVisible(true);
	}

		/*.................................................................................................................*/
	protected void saveFullDimensions(){
		savedFullW = getBounds().width;
		savedFullH = getBounds().height;
	}

	/*.................................................................................................................*/
	public void setSavedDimensions(int w, int h){
		savedW = w;
		if ((windows.size()>1)){
			savedHWithoutTabs = h - tabHeight;
			savedHWithTabs = h;
		}
		else {
			savedHWithoutTabs = h;
			savedHWithTabs = h + tabHeight;
		}
	}
	/*.................................................................................................................*/
	/** Sets the window size.  To be used instead of setSize. 
	 * Passed are the requested size of the contents. This frame must accommodate extra for insets in setting its own size*/
	public void setWindowSize(MesquiteWindow w, int width, int height) {
		setWindowSize(w, width, height, true);
	}
	/*.................................................................................................................*/
	public void setWindowSize(MesquiteWindow ww,int width, int height, boolean expandOnly) {
		
		Insets insets = getInsets();
		storeInsets(insets);
		boolean adjustWidthOnly = !MesquiteInteger.isCombinable(height);
		boolean adjustHeightOnly =  !MesquiteInteger.isCombinable(width);

		int totalNeededWidth = getWidth();
		if (!adjustHeightOnly){
			totalNeededWidth = width + insets.left + insets.right;
			if (ww.getTileLocation()== MAIN){
				if (poptileWidth>0 && frontMostInLocation(POPTILE) != null && frontMostInLocation(POPTILE).popAsTile)
					totalNeededWidth += poptileWidth; // + BETWEENWIDTH ;
			}
			else	if (ww.getTileLocation()== POPTILE){
				if (ww.popAsTile && frontMostInLocation(MAIN) != null)
					totalNeededWidth += frontMostInLocation(MAIN).getWidth(); // + BETWEENWIDTH ;
			}

			/*	if (ww.getTileLocation()== MAIN){
			MesquiteWindow w = frontMostInLocation(POPTILE);
			if (w != null)
				totalNeededWidth += w.getWidth() + BETWEENWIDTH;
		}
		else if (ww.getTileLocation()== POPTILE){
			MesquiteWindow w = frontMostInLocation(MAIN);
			if (w != null)
				totalNeededWidth += w.getWidth() + BETWEENWIDTH;
		}*/
			if (ww.getTileLocation()!= RESOURCES && resourcesWidth>0 && !resourcesFullWindow && !resourcesClosedWhenMinimized)
				totalNeededWidth += resourcesWidth; // + BETWEENWIDTH ;

			//	int savedWidth = neededWidth - (insets.left + insets.right);
			if (expandOnly && totalNeededWidth < getBounds().width){
				totalNeededWidth = getBounds().width;
			}
		}


		int totalNeededHeight = getHeight();
		if (!adjustWidthOnly){
			totalNeededHeight = height + insets.top +insets.bottom;

			//	int savedHeight = neededHeight - (insets.top +insets.bottom);
			if ((windows.size()>1))
				totalNeededHeight += tabHeight;
			if (expandOnly && totalNeededHeight < getBounds().height){
				totalNeededHeight = getBounds().height;
			}
		}
		setSavedDimensions(totalNeededWidth, totalNeededHeight);
		setSize(totalNeededWidth, totalNeededHeight);
		resetSizes(true);
		for (int i = 0; i<windows.size(); i++){
			MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
			w.resetContentsSize();
		}
		//storeInsets(getInsets());
		saveFullDimensions();
	}

	public Rectangle getBounds(MesquiteWindow w){
		if (w.getTileLocation() == RESOURCES)
			return resources.getBounds();
		else if (w.getTileLocation() == POPTILE)
			return poptile.getBounds();
		return main.getBounds();
	}
	/*.................................................................................................................*/
	public void resetSizes(boolean resizeContainedWindows){
		resetSizes(resizeContainedWindows, false);
	}
	/*.................................................................................................................*/
	public void resetSizes(boolean resizeContainedWindows, boolean force){
		Insets insets = getInsets();
		if (!force && (getBounds().width != savedFullW || getBounds().height != savedFullH)){
		}
		else if (oldInsetTop!=insets.top || oldInsetBottom !=insets.bottom || oldInsetRight!= insets.right || oldInsetLeft != insets.left) {

			int totalNeededWidth = savedFullW+(insets.right-oldInsetRight)+(insets.left-oldInsetLeft);
			int totalNeededHeight = savedFullH+(insets.top-oldInsetTop)+(insets.bottom-oldInsetBottom);
			setSavedDimensions(totalNeededWidth, totalNeededHeight);
			setSize(totalNeededWidth, totalNeededHeight);
			
		}
		saveFullDimensions();
		storeInsets(insets);
		int effectiveResourcesWidth = resourcesWidth;
		boolean effectiveResourcesFullWindow = resourcesFullWindow;
		if (windows.size()==1 && frontMostInLocation(RESOURCES)!= null)
			effectiveResourcesFullWindow= true;
		if (resourcesClosedWhenMinimized)
			effectiveResourcesWidth = 0;
		int effectivePoptileWidth = poptileWidth -BETWEENWIDTH;
		int effectivePBETWEENWIDTH = BETWEENWIDTH;

		if (windows.size()==1 || frontMostInLocation(POPTILE)==null)
			effectivePoptileWidth= 0;
		if (effectivePoptileWidth == 0)
			effectivePBETWEENWIDTH = 0;
		if (windows.size()>1 || frontMostInLocation(RESOURCES)!= null){
			if (tabs !=null){
				tabs.setVisible(true);
				tabs.setBounds(insets.left, insets.top, getBounds().width - insets.left - insets.right, tabHeight);
			}
			if (effectiveResourcesFullWindow){
				resources.setBounds(insets.left, insets.top + tabHeight, getBounds().width - insets.left - insets.right, getBounds().height - insets.top - insets.bottom - tabHeight);
				if (projectWindow != null)
					projectWindow.windowResized();
				main.setVisible(false);
				main.setBounds(0,0,0,0);
				rBetweenPanel.setVisible(false);
				rBetweenPanel.setBounds(0,0,0,0);
				pBetweenPanel.setVisible(false);
				pBetweenPanel.setBounds(0,0,0,0);
				poptile.setVisible(false);
				poptile.setBounds(0,0,0,0);
				resources.doLayout();
			}
			else {
				resources.setBounds(insets.left, insets.top + tabHeight, effectiveResourcesWidth- BETWEENWIDTH, getBounds().height - insets.top - insets.bottom - tabHeight);
				if (projectWindow != null)
					projectWindow.windowResized();
				
				main.setBounds(effectiveResourcesWidth + insets.left, insets.top + tabHeight, getBounds().width - insets.left - insets.right - effectiveResourcesWidth - effectivePoptileWidth-effectivePBETWEENWIDTH, getBounds().height - insets.top - insets.bottom - tabHeight);
				main.setVisible(true);
				if (effectivePoptileWidth>0){
					poptile.setVisible(true);
					poptile.setBounds(main.getWidth() + main.getX()+effectivePBETWEENWIDTH, insets.top + tabHeight, effectivePoptileWidth, getBounds().height - insets.top - insets.bottom - tabHeight );
					pBetweenPanel.setBounds(main.getWidth() + main.getX(), insets.top + tabHeight, effectivePBETWEENWIDTH, getBounds().height - insets.top - insets.bottom - tabHeight);
					pBetweenPanel.setVisible(true);
					poptile.doLayout();
				}
				else {
					poptile.setVisible(false);
					poptile.setBounds(0,0,0,0);
					pBetweenPanel.setVisible(false);
					pBetweenPanel.setBounds(0,0,0,0);

				}
				rBetweenPanel.setBounds(effectiveResourcesWidth + insets.left- BETWEENWIDTH, insets.top + tabHeight, BETWEENWIDTH, getBounds().height - insets.top - insets.bottom - tabHeight);
				rBetweenPanel.setVisible(true);
				resources.doLayout();
				main.doLayout();
			}
			if (resizeContainedWindows){

				for (int i = 0; i<windows.size(); i++){
					MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
					try{
						w.resetContentsSize();
					}
					catch(Throwable t){
					}
				}
			}
		}
		else {
			if (tabs !=null){
				tabs.setVisible(false);
				tabs.setBounds(0,0,0,0);
			}
			if (effectiveResourcesFullWindow){
				resources.setBounds(insets.left, insets.top, getBounds().width - insets.left - insets.right, getBounds().height - insets.top - insets.bottom);
				main.setVisible(false);
				main.setBounds(0,0,0,0);
				rBetweenPanel.setVisible(false);
				rBetweenPanel.setBounds(0,0,0,0);
				pBetweenPanel.setVisible(false);
				pBetweenPanel.setBounds(0,0,0,0);
				poptile.setVisible(false);
				poptile.setBounds(0,0,0,0);
				resources.doLayout();
			}
			else {
				resources.setBounds(insets.left, insets.top + tabHeight, effectiveResourcesWidth, getBounds().height - insets.top - insets.bottom - tabHeight);
				main.setVisible(true);
				main.setBounds(effectiveResourcesWidth + insets.left, insets.top, getBounds().width - insets.left - insets.right - effectiveResourcesWidth -effectivePoptileWidth-effectivePBETWEENWIDTH, getBounds().height - insets.top - insets.bottom );
				if (effectivePoptileWidth>0){
					poptile.setVisible(true);
					poptile.setBounds(main.getWidth() + main.getX()+effectivePBETWEENWIDTH, insets.top, effectivePoptileWidth, getBounds().height - insets.top - insets.bottom );
					pBetweenPanel.setBounds(main.getWidth() + main.getX(), insets.top, effectivePBETWEENWIDTH, getBounds().height - insets.top - insets.bottom);
					pBetweenPanel.setVisible(true);
					poptile.doLayout();
				}
				else {
					poptile.setVisible(false);
					poptile.setBounds(0,0,0,0);
					pBetweenPanel.setVisible(false);
					pBetweenPanel.setBounds(0,0,0,0);

				}
				rBetweenPanel.setBounds(effectiveResourcesWidth + insets.left- BETWEENWIDTH, insets.top, BETWEENWIDTH, getBounds().height - insets.top - insets.bottom);
				rBetweenPanel.setVisible(true);
				resources.doLayout();
				main.doLayout();
			}
			if (resizeContainedWindows){
				for (int i = 0; i<windows.size(); i++){
					MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
					try {
						w.resetContentsSize();
					}
					catch (Throwable t){
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	boolean locationPreviouslySet = false;
	boolean locationPreviouslySetScripting = false;
	public void setWindowLocation(int x, int y, boolean overridePrevious, boolean scripting){
		if (!overridePrevious && windows.size()>1 && ((!scripting && locationPreviouslySet)|| (scripting && locationPreviouslySetScripting)))
			return;
		if (scripting)
			locationPreviouslySetScripting = true;
		else
			locationPreviouslySet = true;
		savedX = x;
		savedY = y;
		setLocation(x, y);
	}
	public void paint(Graphics g){
		if (checkInsets(true))
			super.paint(g);
	}
	/*.................................................................................................................*/
	boolean checkInsets(boolean resetSizesIfNeeded){
		Insets insets = getInsets();
		if (oldInsetTop!=insets.top || oldInsetBottom !=insets.bottom || oldInsetRight!= insets.right || oldInsetLeft != insets.left) {
			//storeInsets(insets);
			if (resetSizesIfNeeded)
				resetSizes(true);
			return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	private void storeInsets(Insets insets){
		oldInsetTop=insets.top;
		oldInsetBottom=insets.bottom;
		oldInsetRight= insets.right;
		oldInsetLeft= insets.left; 
	}
	/*.................................................................................................................*/
	/** Respond to commands sent to the window. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Requests close", null, commandName, "closeWindowRequested")) {
			closeWindowRequested();
		}
		return null;
	}


	private boolean checkAndResetInsets(int width, int height){
		Insets insets = getInsets();
		if (oldInsetTop!=insets.top || oldInsetBottom !=insets.bottom || oldInsetRight!= insets.right || oldInsetLeft != insets.left) {
			int totalNeededWidth = width+(insets.right-oldInsetRight)+(insets.left-oldInsetLeft);
			int totalNeededHeight = height+(insets.top-oldInsetTop)+(insets.bottom-oldInsetBottom);
			setSavedDimensions(totalNeededWidth, totalNeededHeight);
			setSize(totalNeededWidth, totalNeededHeight);
			storeInsets(insets);
			saveFullDimensions();
			resetSizes(true);
			for (int i = 0; i<windows.size(); i++){
				MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
				w.resetContentsSize();
			}
			return true;
		}
		storeInsets(insets);
		return false;
	}
	/*.................................................................................................................*/
	class MWCE extends ComponentAdapter{
		MesquiteFrame f;
		public MWCE (MesquiteFrame f){
			super();
			this.f = f;
		}
		public void componentResized(ComponentEvent e){
			if (e==null || e.getComponent()!= f || (getOwnerModule()!=null && (getOwnerModule().isDoomed()))) // || disposing)))
				return;

			if (savedFullH== getBounds().height&& savedFullW == getBounds().width) {
				checkAndResetInsets(savedFullW, savedFullH);
			}
			else if (doingShow || !isResizable()){
				if (windows.size() >1 || frontMostInLocation(RESOURCES) != null)
					setSize(savedW, savedHWithTabs);
				else
					setSize(savedW, savedHWithoutTabs);
				resetSizes(true);
			}
			else {
				resetSizes(true);
			}

		}
		public void componentMoved(ComponentEvent e){
			if (e==null || e.getComponent()!= f || (getOwnerModule()!=null && (getOwnerModule().isDoomed())))// || disposing)))
				return;
			//	if (doingShow)
			//		setLocation(savedX,savedY);
		}
		public void componentShown(ComponentEvent e){
			Insets insets = getInsets();
			if (!checkAndResetInsets(savedFullW, savedFullH)) //if (!checkInsets(false))
				//resetSizes(true);
				//else
			{
				if (MesquiteTrunk.isMacOSXJaguar()) {
					for (int i = 0; i<windows.size(); i++){
						MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
						w.repaintAll();
					}
				}
			}
			Toolkit.getDefaultToolkit().sync();
			if (e.getComponent() instanceof Frame && !(e.getComponent() instanceof ProgressWindow))
				ProgressWindow.allIndicatorsToFront();

		}

	}

	public static MesquiteFrame activeWindow;
	/*.................................................................................................................*/
	class MWWE extends WindowAdapter{
		MesquiteFrame f;
		MesquiteCommand closeCommand;
		public MWWE (MesquiteFrame f){
			this.f = f;
			closeCommand = new MesquiteCommand("closeWindowRequested", f);
		}
		public void windowClosing(WindowEvent e){

			MesquiteModule.incrementMenuResetSuppression();
			try {
				if (MesquiteTrunk.isMacOS() && f!=activeWindow && activeWindow!=null) //workaround the Mac OS menu disappearance bug after closing a window
					toFront();
				closeCommand.doItMainThread(null, null, null);


			}
			catch (Exception ee){
			}
			MesquiteModule.decrementMenuResetSuppression();

		}
		public void windowActivated(WindowEvent e){
			try {
				if (f !=null) {
					activeWindow = f;//for workaround the Mac OS menu disappearance bug after closing a window
					if (f.ownerModule !=null && f.ownerModule.getProject() !=null && windows.indexOf(f.ownerModule.getProject().getCoordinatorModule().getModuleWindow())>=0){ //so that file save will remember foremost window
						f.ownerModule.getProject().activeWindowOfProject = f.frontWindow;
					}
					/*	if (e.getComponent() instanceof Frame && !(e.getComponent() instanceof ProgressWindow))
					ProgressWindow.allIndicatorsToFront();
					 */
				}
			}
			catch (Exception ex){
			}

		}
	}
}

class FrameTabsPanel extends MousePanel {
	MesquiteFrame frame;
	int[] lefts, rights;
	Font[] fonts = new Font[6];
	int frontEdge = 6;
	int backEdge = 20;
	Image goaway, popOut, popIn, minimize, mediumize, show;
	MesquitePopup popup=null;
	MesquiteCommand popCommand, tileCommand;
	public FrameTabsPanel(MesquiteFrame f){
		this.frame = f;
		//fonts[0] = new Font("SanSerif", Font.PLAIN, 13);
		fonts[0] = new Font("SanSerif", Font.PLAIN, 12);
		fonts[1] = new Font("SanSerif", Font.PLAIN, 11);
		fonts[2] = new Font("SanSerif", Font.PLAIN, 10);
		fonts[3] = new Font("SanSerif", Font.PLAIN, 9);
		fonts[4] = new Font("SanSerif", Font.PLAIN, 8);
		fonts[5] = new Font("SanSerif", Font.PLAIN, 7);
		//	f.diagnose();
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goawayTransparent.gif");
		minimize = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "minimizeTransparent.gif");
		mediumize = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "mediumizeTransparent.gif");
		popOut = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "decompactTransparent.gif");
		popIn = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "compactTransparent.gif");
		show = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "showTransparent.gif");
		popCommand = new MesquiteCommand("popOut", this);
		tileCommand = new MesquiteCommand("tileOut", this);
		setBackground(ColorTheme.getExtInterfaceBackground());

	}
	/*.................................................................................................................*/
	int findTab(int x){
		if (lefts == null || lefts.length == 0)
			return MesquiteInteger.unassigned;
		if (x < lefts[0])
			return -1;
		for (int i = 0; i<lefts.length; i++){
			if (x > lefts[i] && x < rights[i])
				return i;
		}
		for (int i = 0; i<lefts.length-1; i++){
			if (x > rights[i] && x < lefts[i+1])
				return 10000 + i;
		}
		return lefts.length;
	}
	int tabTouched = MesquiteInteger.unassigned;
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		tabTouched = findTab(x);
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		try {
		int i = findTab(x);
		if (i == MesquiteInteger.unassigned)
			return;
		if (tabTouched>=10000)
			return;
		if (i== tabTouched){  //down and up on same
			if (frame.permitGoAway(i) && x> rights[i] - 20 && y >4 && y<20){
				if (frame.goAwayOrShow(i))  //showing goaway
					frame.windowGoAway(i);
				else
					;
			}
			else if (frame.showMinimizeMaximize(i) && x> rights[i] - 20 && y >4 && y<20)
				frame.toggleMinimize(i);
			else if (frame.showPopOut(i) && x> rights[i] - 20 && y >20 && y<40){
				MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
				if (w.isPoppedOut())
					frame.popIn(w);
				else {
					redoMenu(i);
					popup.show(this, x,y);
				}
			}
			else
				frame.showPage(i);
		}
		else if (tabTouched != MesquiteInteger.unassigned){
			if (tabTouched<0 || tabTouched>= frame.windows.size())
				return;
			MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(tabTouched);
			if (w.isPoppedOut())
				return;
			if (MesquiteWindow.checkDoomed(w)){
				MesquiteWindow.uncheckDoomed(w);
				return;
			}

			if (i>=10000)
				i = i - 10000;
				if (i<tabTouched && tabTouched > 1){  //can't be first window other than project panel
					if (i<=0)
						i = 1;
					if (frame.windows.indexOf(w) != i){
						frame.windows.removeElement(w);
						if (i >= frame.windows.size())
							frame.windows.addElement(w);
						else
							frame.windows.insertElementAt(w, i);
					}
					frame.showPage(w);
					repaint();

				}
				else if (i> tabTouched && tabTouched >0 && tabTouched < lefts.length-1){
					if (frame.windows.indexOf(w) != i){
						frame.windows.removeElement(w);
						if (i >= frame.windows.size())
							frame.windows.addElement(w);
						else
							frame.windows.insertElementAt(w, i);
					}
					frame.showPage(w);
					repaint();
				}
		}
		}
		catch (Exception e){
		}
	}
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Pops out the window", null, commandName, "popOut")) {
			int i = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(i)){
				MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
				if (w != null)
					w.setPopAsTile(false);
				frame.popOut(i, true);
			}
		}
		else if (checker.compare(this.getClass(), "Tiles out the window", null, commandName, "tileOut")) {
			int i = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(i)){
				MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
				if (w != null)
					w.setPopAsTile(true);
				frame.popOut(i, true);
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	void redoMenu(int i) {
		if (popup==null)
			popup = new MesquitePopup(this);
		popup.removeAll();
		popCommand.setDefaultArguments(Integer.toString(i));
		tileCommand.setDefaultArguments(Integer.toString(i));
		MesquiteMenuItem mItem = new MesquiteMenuItem("Pop Out as Separate Window", null, popCommand);
		MesquiteMenuItem mItem2 = new MesquiteMenuItem("Put in Separate Tile", null, tileCommand);
		popup.add(mItem);
		popup.add(mItem2);
		add(popup);
	}
	public void paint(Graphics g){
		//frame.ontabDark = new Color(160, 82, 45);

		if (!frame.checkInsets(true)){
			repaint();
			return;
		}
		if (InterfaceManager.isEditingMode()){
			Color co = g.getColor();
			g.setColor(Color.cyan);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(co);
		}
		int width = getBounds().width;
		int height = getBounds().height;
		g.setColor(ColorDistribution.veryDarkMesquiteBrown);
		g.drawLine(0, height-1, width, height-1);/**/
		g.fillRect(0, height-4, width, 4);


		int numTabs = frame.windows.size();
		if (numTabs<2 && frame.frontMostInLocation(MesquiteFrame.RESOURCES) == null) //if has resources window, then show tabs
			return;
		if (lefts == null || lefts.length != numTabs){
			lefts = new int[numTabs];
			rights = new int[numTabs];
		}
		String totalString = "";
		int iconNumber = 0;
		for (int i = 0; i<frame.windows.size(); i++){
			MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
			String s = w.getTitle();
			if (s != null)
				totalString += w.getTitle();
			if (w.getIcon() !=null)
				iconNumber ++;
		}
		int iconsWidth = iconNumber * 20;
		int edges = (frontEdge + backEdge) * numTabs;
		if (frame.frontMostInLocation(MesquiteFrame.POPTILE)!=null)
			edges += 30;
		int i = 0;
		g.setFont(fonts[0]);
		int needed = 0;
		while ((needed = StringUtil.getStringDrawLength(g, totalString) + iconsWidth +edges) > width && i<fonts.length-1){
			i++;
			g.setFont(fonts[i]);
		}
		double scaling = 1.0;
		if (needed> width)
			scaling = (width-edges)*1.0/(StringUtil.getStringDrawLength(g, totalString) + iconsWidth);
		int left = 0;
		int right = width+ backEdge;
		for (i = 0; i<frame.windows.size(); i++){
			if (frame.permitGoAway(i) || frame.showMinimizeMaximize(i)){
				frontEdge = 4;
				backEdge = 26;
			}
			else {
				frontEdge = 6;
				backEdge = 20;
			}
			int tabLeft = left;
			int tabRight = left + backEdge-2;

			MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
			boolean popped = w.getTileLocation() == MesquiteFrame.POPTILE;
			Image icon = w.getIcon();
			int iconWidth = 0;
			if (icon != null)
				iconWidth = 20;
			if (!popped){
				left += frontEdge;
				tabLeft = left;
				tabRight = left + backEdge-2;
			}
			else {
				right -= backEdge;
				tabRight = right -2;
				tabLeft = right - frontEdge;
			}
			boolean isFrontOfType = !frame.resourcesFullWindow && ((popped && (frame.frontMostInLocation(MesquiteFrame.POPTILE)==w)) || (!popped && (frame.frontMostInLocation(MesquiteFrame.MAIN)==w)));
			isFrontOfType = isFrontOfType || (frame.resourcesFullWindow && w.getTileLocation() == MesquiteFrame.RESOURCES);
			int frontness = 0;
			if (isFrontOfType){
				if (frame.frontWindow == w)
					frontness = 2;
				else
					frontness = 1;
			}
			String title = w.getTitle();
			if (title != null){
				if (scaling < 1.0){
					int wish = StringUtil.getStringDrawLength(g, title) + iconWidth;
					int offer = (int)(scaling * wish);
					if (!popped)
						tabRight = left + offer + backEdge-2;
					else
						tabLeft = right - offer - frontEdge - backEdge +2;
					drawTab(g, frontness, tabLeft, tabRight, height);
					StringInABox box = new StringInABox(title, g.getFont(), offer);
					
					if (frontness == 2) 
						g.setColor(ColorTheme.getExtInterfaceTextContrast()); //Color.black);
					//g.setColor(ColorTheme.getProjectText(0)); //Color.black);
					else if (frontness == 1) 
						g.setColor(ColorTheme.getExtInterfaceTextMuted()); //Color.black);
					else 
						g.setColor(ColorTheme.getExtInterfaceTextMuted()); //Color.black);
					box.draw(g, tabLeft + frontEdge + iconWidth, 1);
					if (icon != null)
						g.drawImage(icon, tabLeft + frontEdge, 1, this);

					if (!popped)
						left += offer;
					else
						right -= offer;
				}
				else {
					int offer = StringUtil.getStringDrawLength(g, title) + iconWidth;
					if (!popped)
						tabRight = left + offer + backEdge-2;
					else
						tabLeft = right - offer - frontEdge - backEdge +2;
					drawTab(g, frontness, tabLeft, tabRight, height);
					if (frontness == 2) 
						g.setColor(ColorTheme.getExtInterfaceTextContrast()); //Color.black);
					//	g.setColor(ColorTheme.getProjectText(0)); //Color.black);
					else if (frontness == 1) 
						g.setColor(ColorTheme.getExtInterfaceTextMuted()); //Color.black);
					else 
						g.setColor(ColorTheme.getExtInterfaceTextMuted()); //Color.black);
					g.drawString(title, tabLeft + frontEdge + iconWidth, height -12);
					if (icon != null)
						g.drawImage(icon, tabLeft + frontEdge, height -22, this);
					if (!popped)
						left += offer;
					else
						right -= offer;
				}
			}
			else 
				MesquiteMessage.println("window without title " + w.getClass());

			if (!popped)
				left += backEdge;
			else
				right -= frontEdge;
			if (frame.permitGoAway(i)) {
				if (goaway != null && frame.goAwayOrShow(i))
					g.drawImage(goaway, tabRight-17, 4, this);
				else if (show != null && !frame.goAwayOrShow(i))
					g.drawImage(show, tabRight-17, 4, this);
			}
			else if (frame.showMinimizeMaximize(i)){
				if (minimize != null && !w.isMinimized())
					g.drawImage(minimize, tabRight-17, 4, this);
				else if (mediumize != null && w.isMinimized())
					g.drawImage(mediumize, tabRight-17, 4, this);
			}
			if (popOut != null && frame.showPopOut(i)){
				if (w.isPoppedOut())
					g.drawImage(popIn, tabRight-17, 16, this);
				else 
					g.drawImage(popOut, tabRight-17, 16, this);
			}
			g.setColor(ColorTheme.getExtInterfaceEdgeContrast()); //Color.black);
			try{
				lefts[i] = tabLeft;
				rights[i] = tabRight;
			}
			catch (ArrayIndexOutOfBoundsException e){
				repaint();
			}
		}
		/*g.setColor(frame.ontabDark);
		g.fillRect(0, height-4, width, 4);*/

	}
	void drawTab(Graphics g, int frontness, int tabLeft, int tabRight, int height){

		int roundness = 10;  //had been 16
		int top = 4;  //had been 2
		if (frontness>0){
			if (frontness>1){
				//	g.setColor(ColorTheme.getExtInterfaceElementPale()); //frame.medium);
				g.setColor(ColorTheme.getExtInterfaceElementContrast()); //frame.medium);
				//			GradientPaint gradient =  new GradientPaint(tabLeft, top, ColorTheme.getExtInterfaceElementContrast(), tabLeft, height, ColorDistribution.brighter(ColorTheme.getExtInterfaceElementContrast(),0.8)); 
				//			GradientPaint gradient =  new GradientPaint(tabLeft, top, ColorDistribution.brighter(ColorTheme.getExtInterfaceElementContrast(),0.8), tabLeft, height, ColorTheme.getExtInterfaceElementContrast()); 
				if (g instanceof Graphics2D) {
					GradientPaint gradient =  new GradientPaint(tabLeft, top, ColorDistribution.brighter(ColorTheme.getExtInterfaceElementContrast(),0.8), tabLeft, height+20, ColorDistribution.darker(ColorTheme.getExtInterfaceElementContrast(),0.3)); 
					((Graphics2D)g).setPaint(gradient);
				}
				g.fillRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);
				g.setColor(ColorTheme.getExtInterfaceEdgeContrast());
				g.drawRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);
			}
			else {
				//			g.setColor(ColorDistribution.veryLightGray);
				g.setColor(ColorTheme.getExtInterfaceElement());
				if (g instanceof Graphics2D) {
					GradientPaint gradient =  new GradientPaint(tabLeft, top, ColorDistribution.brighter(ColorTheme.getExtInterfaceElement(),0.9), tabLeft, height+20, ColorDistribution.darker(ColorTheme.getExtInterfaceElement(),0.3)); 
					((Graphics2D)g).setPaint(gradient);
				}
				g.fillRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);
				g.setColor(ColorTheme.getExtInterfaceEdgeContrast());
				g.drawRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);
			}
		}
		else {
			g.setColor(ColorTheme.getExtInterfaceElement());
			if (g instanceof Graphics2D) {
				GradientPaint gradient =  new GradientPaint(tabLeft, top, ColorDistribution.brighter(ColorTheme.getExtInterfaceElement(),0.9), tabLeft, height+20, ColorDistribution.darker(ColorTheme.getExtInterfaceElement(),0.3)); 
				((Graphics2D)g).setPaint(gradient);
			}
			g.fillRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);
			g.setColor(ColorDistribution.veryDarkMesquiteBrown);
			g.fillRect(tabLeft, height-4, tabRight-tabLeft, 4);
			//	g.setColor(ColorTheme.getExtInterfaceEdgePale());
			//	g.drawRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);
		}
		/*	if (frontness>0){
			if (frontness>1)
				g.setColor(frame.ontabLight); //frame.medium);
			else
				g.setColor(ColorDistribution.lightMesquiteBrown);
			g.fillRoundRect(tabLeft, 2, tabRight - tabLeft - 2, height+60, 16, 16);
			//g.setColor(frame.ontabDark);
			//g.drawRoundRect(tabLeft, 2, tabRight - tabLeft - 2, height+60, 16, 16);
		}
		else {
			g.setColor(Color.lightGray);
			g.setColor(ColorDistribution.mesquiteBrown);
			g.fillRoundRect(tabLeft, 2, tabRight - tabLeft - 2, height+60, 16, 16);
			g.setColor(ColorDistribution.lightMesquiteBrown);
			g.drawRoundRect(tabLeft, 2, tabRight - tabLeft - 2, height+60, 16, 16);
		}*/
	}
}
class BetweenPanel extends MousePanel{
	MesquiteFrame f;

	public BetweenPanel(MesquiteFrame f){ 
		super();
		setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
		this.f=f;
		setBackground(ColorDistribution.darkMesquiteBrown);
		//setBackground(Color.darkGray);
	}
	int touchX = -1;
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		touchX= x;
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (touchX>=0){
			f.incrementPanelWidth(this, x-touchX);
		}
		touchX = -1;
	}
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
	}
}



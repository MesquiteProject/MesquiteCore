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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

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
	boolean isSystemFrame = false;
	public static int cornerBuffer = 8;


	int num = 0;
	int id = 0;
	int leftPoptile = -1;
	Panel main;
	private Panel resources;
	Panel  poptile;
	private BetweenPanel rBetweenPanel;   // this is the vertical bar between the project panel and the main window region
	private BetweenPanel pBetweenPanel;
	private static int BETWEENWIDTH = 6;
	public static final int RESOURCES = 0;  // for project
	public static final int MAIN = 1;
	public static final int POPTILE = 2;
	boolean resourcesFullWindow = false; //0 for closed; 1 for open partly; 2 for full window;
	boolean resourcesClosedWhenMinimized = false; //0 for closed; 1 for open partly; 2 for full window;
	int resourcesWidth = 0;
	int poptileWidth = 300;
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
		isSystemFrame = mb == MesquiteTrunk.mesquiteTrunk;
		if (tabs != null)
			tabs.setBackground(ColorTheme.getExtInterfaceBackground(isSystemFrame));
	}

	/*.................................................................................................................*/
	boolean alreadyDisposed = false;
	public void dispose() {
		if (alreadyDisposed)
			return;
		try{
			removeAll();
		}
		catch (Exception e){
			//strange things can happen with threading...
		}
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
	static int countSMB = 0;
	public void setMenuBar(MenuBar mbar) {
		if (getMenuBar() == mbar)
			return;
		if (!MesquiteThread.isReadingThread())
			super.setMenuBar(mbar);
	}
	public void setMenuBar(MesquiteWindow which, MenuBar mbar) {
		if (which == frontWindow) {
			try {
				setMenuBar(mbar);
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
		if (resourcesWidth >=0)
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
	public boolean anythingPopped(){
		MesquiteWindow w = frontMostInLocation(MesquiteFrame.POPTILE);
		return w != null;
	}
	public boolean anythingPoppedOtherThanMe(MesquiteWindow me){
		for (int i=orderedWindows.size()-1; i>=0; i--){
			MesquiteWindow w = (MesquiteWindow)orderedWindows.elementAt(i);
			if (w.getTileLocation() == POPTILE && w != me)
				return true;
		}
		return false;
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
	public void fixFrontness(){
		MesquiteWindow w = frontMostInLocation(MesquiteFrame.POPTILE);
		if (w!= null && poptile.getComponentZOrder(w.outerContents) != 0){
			showInLayout(w.getTileLocation(), Integer.toString(w.getID()));	
			reconnect(w);
		}
		w = frontMostInLocation(MesquiteFrame.MAIN);
		if (w != null && main.getComponentZOrder(w.outerContents) != 0){
			showInLayout(w.getTileLocation(), Integer.toString(w.getID()));	
			reconnect(w);
		}
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
		if (windows == null)
			return false;
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
		//	if (w == frontWindow)
		//		return;
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
		//	if (w == frontWindow)
		//		return;
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
		w.requestFocus();
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
		return false; //!(w instanceof SystemWindow);
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
		MesquiteWindow w = null;
		try {
			w = (MesquiteWindow)windows.elementAt(i);
		}
		catch (Exception e){
			return;
		}
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

		MesquiteWindow fw = frontMostInLocation(MAIN);
		reconnect(fw);
	}
	public void popIn(MesquiteWindow w){
		if (windows == null || (!w.popAsTile && windows.indexOf(w)>=0))  //POPOUTBUGS: If window is popped out in separate window, then this doesn't work, in part as windows.indexOf(w)=0 but there is only one window.  
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
		fixFrontness();
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
			reconnect(w);
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
				int loc = w.getTileLocation();
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
				MesquiteWindow fw = frontMostInLocation(loc);
				if (fw != null)
					reconnect(fw);
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

	private void reconnect(MesquiteWindow w){
		if (w == null)
			return;
		if (w.getTileLocation() == RESOURCES || isPrimarylMesquiteFrame)
			return;
		for (int i = 0; i<windows.size(); i++){  //This is a workaround for bug in OS X Java 1.7 and higher by which panels behind would leak to panels in front
			MesquiteWindow ww = (MesquiteWindow)windows.elementAt(i);
			if (ww != w && ww.getTileLocation() == w.getTileLocation()){ 
				ww.disconnectGraphics();
			}
		}
		w.reconnectGraphics();
		invalidate();
		try {
			validate();
		}
		catch (Exception e){
		}

	}

	private void refreshGraphics(){
		for (int i = 0; i<windows.size(); i++){  //This is a workaround for bug in OS X Java 1.7 and higher by which panels behind would leak to panels in front
			MesquiteWindow ww = (MesquiteWindow)windows.elementAt(i);
			//	ww.validate();
		}
	}

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

			reconnect(w);


		}	
		if (w != null)
			setMenuBar(w, w.getMenuBar(true));
		if (tabs !=null)
			tabs.repaint();
	}
	public void OLDsetAsFrontWindow(MesquiteWindow w){
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
		fixFrontness();
		if (frontWindow != null){
			showInLayout(frontWindow.getTileLocation(), Integer.toString(frontWindow.getID()));	
			resetSizes(true);
			frontWindow.requestFocus();
		}
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
		if (windows != null && (windows.size()>1)){
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
		if (windows == null)
			return;
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
				tabs.repaint();
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
					leftPoptile = main.getWidth() + main.getX();
					poptile.setBounds(main.getWidth() + main.getX()+effectivePBETWEENWIDTH, insets.top + tabHeight, effectivePoptileWidth, getBounds().height - insets.top - insets.bottom - tabHeight );
					pBetweenPanel.setBounds(main.getWidth() + main.getX(), insets.top + tabHeight, effectivePBETWEENWIDTH, getBounds().height - insets.top - insets.bottom - tabHeight);
					pBetweenPanel.setVisible(true);
					poptile.doLayout();
				}
				else {
					leftPoptile = -1;
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
			if (resizeContainedWindows && windows != null){

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
					leftPoptile = main.getWidth() + main.getX();
					poptile.setBounds(main.getWidth() + main.getX()+effectivePBETWEENWIDTH, insets.top, effectivePoptileWidth, getBounds().height - insets.top - insets.bottom );
					pBetweenPanel.setBounds(main.getWidth() + main.getX(), insets.top, effectivePBETWEENWIDTH, getBounds().height - insets.top - insets.bottom);
					pBetweenPanel.setVisible(true);
					poptile.doLayout();
				}
				else {
					leftPoptile = -1;
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
			if (resizeContainedWindows & windows != null){
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
	int intertabSpace = 4;
	int defaultBackEdge = 10;
	public static int lowerBarHeight = 4;

	static Image goaway, popOut, popIn, minimize, mediumize, show, goawayMouseOver;
	static {
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goawayTransparent.gif");
		goawayMouseOver = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "windowIcons" + MesquiteFile.fileSeparator + "goaway.gif");
		minimize = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "minimizeTransparent.gif");
		mediumize = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "mediumizeTransparent.gif");
		popOut = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "decompactTransparent.gif");
		popIn = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "compactTransparent.gif");
		show = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "showTransparent.gif");
	}
	MesquitePopup popupForPopoutWindow=null;
	MesquiteCommand popCommand, tileCommand, closeCommand;
	public FrameTabsPanel(MesquiteFrame f){
		this.frame = f;
		//fonts[0] = new Font("SanSerif", Font.PLAIN, 13);
		fonts[0] = new Font("SanSerif", Font.PLAIN, 11);
		fonts[1] = new Font("SanSerif", Font.PLAIN, 11);
		fonts[2] = new Font("SanSerif", Font.PLAIN, 10);
		fonts[3] = new Font("SanSerif", Font.PLAIN, 9);
		fonts[4] = new Font("SanSerif", Font.PLAIN, 8);
		fonts[5] = new Font("SanSerif", Font.PLAIN, 7);
		//	f.diagnose();
		if (goaway == null){
			goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goawayTransparent.gif");
			goawayMouseOver = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "windowIcons" + MesquiteFile.fileSeparator + "goaway.gif");
			minimize = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "minimizeTransparent.gif");
			mediumize = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "mediumizeTransparent.gif");
			popOut = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "decompactTransparent.gif");
			popIn = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "compactTransparent.gif");
			show = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "showTransparent.gif");
		}
		closeCommand = new MesquiteCommand("closeWindow", this);
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
				return -1;
		}
		return lefts.length;
	}
	int tabTouched = MesquiteInteger.unassigned;
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		tabTouched = findTab(x);
		if (tabTouched>= 0 && MesquiteEvent.commandOrControlKeyDown(modifiers)){
			redoPopoutWindowMenu(tabTouched);
			popupForPopoutWindow.show(this, x,y);
		}
	}

	static int goAwayTop = 6;
	static int goAwayBottom = 18;

	int getGoAwayControlTop(){
		if (scaling < 1.0)
			return 6;
		return panelHeight-22;
	}
	int getGoAwayControlBottom(){
		return getGoAwayControlTop()+12;
	}
	int getGoAwayControlLeft(){
		return intertabSpace -1;
	}
	int getGoAwayControlRight(){
		return getGoAwayControlLeft() + 12;
	}
	public void explainTab(int whichTab) {
		frame.frontWindow.setExplanation(getTabTitle(whichTab));

	}
	private MesquiteWindow getWindow(int i){
		try {
			MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
			return w;
		}
		catch(Exception e){
		}
		catch(Error e){
		}
		return null;
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		try {
			int i = findTab(x);
			if (i == MesquiteInteger.unassigned)
				return;
			if (tabTouched>=10000 || tabTouched <0)
				return;
			if (i== tabTouched){  //down and up on same
				if (frame.permitGoAway(i) && x> lefts[i] + getGoAwayControlLeft() && x<lefts[i] +getGoAwayControlRight() && y >= getGoAwayControlTop() && y<=getGoAwayControlBottom()){
					if (frame.goAwayOrShow(i))  //showing goaway
						frame.windowGoAway(i);
					else
						;
				}
				//				else if (frame.showMinimizeMaximize(i) && x> rights[i] - 20 && y >4 && y<20){
				else if (frame.showMinimizeMaximize(i) && x< lefts[i] + 20 && y >12 && y<28){
					frame.toggleMinimize(i);
				}
				else {
					MesquiteWindow w = getWindow(tabTouched);
					if (isProjectWindow(w))
						return;
					frame.showPage(i);
				}
			}
			else if (tabTouched != MesquiteInteger.unassigned){
				if (tabTouched<0 || tabTouched>= frame.windows.size())
					return;
				MesquiteWindow w = getWindow(tabTouched);
				if (w == null)
					return;
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
	int lastTabOver = -1;
	int tabOver = -1;
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		if (frame==null || frame.windows==null){
			return;
		}
		tabOver = findTab(x);
		if (tabOver != lastTabOver && tabOver > 0 && (lefts==null || tabOver<lefts.length) ){
			Graphics g = getGraphics();
			if (g != null){
				MesquiteWindow w = getWindow(tabOver);
				if (w == null)
					return;
				drawAndFillTab (g, w,  lefts[tabOver],  rights[tabOver],  tabOver, scaling);
			}
			explainTab(tabOver);
		}
		if (tabOver != lastTabOver && lastTabOver>=0 && (lefts==null || lastTabOver<lefts.length)  && lastTabOver < frame.windows.size()){
			Graphics g = getGraphics();
			if (g != null){
				MesquiteWindow w = getWindow(lastTabOver);
				if (w == null)
					return;
				drawAndFillTab (g, w,  lefts[lastTabOver],  rights[lastTabOver],  lastTabOver, scaling);
			}
		}
		lastTabOver = tabOver;
	}
	long entryTime = -1;

	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (frame==null || frame.windows==null){
			return;
		}
		tabOver = findTab(x);
		if (entryTime<0)
			entryTime = System.currentTimeMillis();
		if (tabOver != lastTabOver && tabOver > 0 && (lefts==null || tabOver<lefts.length) ){
			Graphics g = getGraphics();
			if (g != null){
				MesquiteWindow w = getWindow(tabOver);
				if (w == null)
					return;
				drawAndFillTab (g, w,  lefts[tabOver],  rights[tabOver],  tabOver, scaling);
				entryTime=-1;
			}
		}
		if (tabOver != lastTabOver && lastTabOver>=0 && (lefts==null || lastTabOver<lefts.length)  && lastTabOver < frame.windows.size()){
			Graphics g = getGraphics();
			if (g != null){
				MesquiteWindow w = getWindow(lastTabOver);
				if (w == null)
					return;
				drawAndFillTab (g, w,  lefts[lastTabOver],  rights[lastTabOver],  lastTabOver, scaling);
			}
		}
		explainTab(tabOver);
		lastTabOver = tabOver;
	}


	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		tabOver  = -1;
		if (frame==null || frame.windows==null){
			return;
		}
		entryTime=-1;
		if (lastTabOver>=0 && (lefts==null || lastTabOver<lefts.length) && (lastTabOver < frame.windows.size())){
			Graphics g = getGraphics();
			if (g != null){
				MesquiteWindow w = getWindow(lastTabOver);
				if (w == null)
					return;
				drawAndFillTab (g, w,  lefts[lastTabOver],  rights[lastTabOver],  lastTabOver, scaling);
			}
			lastTabOver = -1;
		}

	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Pops out the window", null, commandName, "popOut")) {
			int i = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(i)){
				MesquiteWindow w = getWindow(i);
				if (w != null)
					w.setPopAsTile(false);
				frame.popOut(i, true);
			}
		}
		else if (checker.compare(this.getClass(), "Tiles out the window", null, commandName, "tileOut")) {
			int i = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(i)){
				MesquiteWindow w = getWindow(i);
				if (w != null)
					w.setPopAsTile(true);
				frame.popOut(i, true);
			}
		}
		else if (checker.compare(this.getClass(), "Closes window", null, commandName, "closeWindow")) {
			int i = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (MesquiteInteger.isCombinable(i)){
				MesquiteWindow w = getWindow(i);
				if (w != null){
					if (frame.goAwayOrShow(i))  //showing goaway
						frame.windowGoAway(i);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	void redoPopoutWindowMenu(int i) {
		if (popupForPopoutWindow==null)
			popupForPopoutWindow = new MesquitePopup(this);
		popupForPopoutWindow.removeAll();
		closeCommand.setDefaultArguments(Integer.toString(i));
		popCommand.setDefaultArguments(Integer.toString(i));
		tileCommand.setDefaultArguments(Integer.toString(i));
		if (i < frame.windows.size()){
			MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
			MesquiteMenuItem mtitle = new MesquiteMenuItem(w.getTitle(), null, null);

			popupForPopoutWindow.add(mtitle);
		}
		MesquiteMenuItem mItemClose = new MesquiteMenuItem("Close Window", null, closeCommand);
		MesquiteMenuItem mItem = new MesquiteMenuItem("Pop Out as Separate Window", null, popCommand);
		MesquiteMenuItem mItem2 = new MesquiteMenuItem("Put in Separate Tile", null, tileCommand);
		popupForPopoutWindow.add(mItemClose);
		popupForPopoutWindow.add(mItem);
		popupForPopoutWindow.add(mItem2);
		add(popupForPopoutWindow);
	}
	int getFrontness(MesquiteWindow w){
		boolean popped = w.getTileLocation() == MesquiteFrame.POPTILE;
		boolean isFrontOfType = !frame.resourcesFullWindow && ((popped && (frame.frontMostInLocation(MesquiteFrame.POPTILE)==w)) || (!popped && (frame.frontMostInLocation(MesquiteFrame.MAIN)==w)));
		isFrontOfType = isFrontOfType || (frame.resourcesFullWindow && w.getTileLocation() == MesquiteFrame.RESOURCES);
		int frontness = 0;
		if (isFrontOfType){
			if (frame.frontWindow == w)
				frontness = 2;
			else
				frontness = 1;
		}
		return frontness;
	}
	static final int tabBottomLineHeight = 4;
	void drawAndFillTab (Graphics g, MesquiteWindow w, int tabLeft, int tabRight, int whichTab, double scaling){
		int frontness = getFrontness(w);

		g.setFont(fonts[fontChosen]);
		Shape oldClip = g.getClip();
		if (w == frame.projectWindow)
			g.setClip(tabLeft-1, 0, tabRight -tabLeft+4, panelHeight-tabBottomLineHeight-1);
		else g.setClip(tabLeft-1, 0, tabRight -tabLeft+4, panelHeight);
		String title = w.getTitle();
		if (w == frame.projectWindow && projectPanelWidth <=0)
			title = "Project";
		Image icon = w.getIcon();
		int iconWidth = 0;
		if (icon != null || w == frame.projectWindow)
			iconWidth = 20;




		drawTab(g, whichTab, w, frontness, tabLeft, tabRight, panelHeight, projectPanelWidth, w == frame.projectWindow, w instanceof SystemWindow);
		if (w == frame.projectWindow)
			g.setClip(tabLeft-1, 0, tabRight -tabLeft+1, panelHeight-tabBottomLineHeight-1);
		else
			g.setClip(tabLeft-1, 0, tabRight -tabLeft+1, panelHeight-tabBottomLineHeight);
		if (frontness == 2) 
			g.setColor(ColorTheme.getExtInterfaceTextContrast(w instanceof SystemWindow)); //Color.black);
		else if (frontness == 1) 
			g.setColor(ColorTheme.getExtInterfaceTextMuted(w instanceof SystemWindow)); //Color.black);
		else 
			g.setColor(ColorTheme.getExtInterfaceTextMuted(w instanceof SystemWindow)); //Color.black);
		if (scaling < 1.0){
			int wish = StringUtil.getStringDrawLength(g, title);
			int offer = (int)(scaling * wish);
			StringInABox box = new StringInABox(title, g.getFont(), offer);
			box.drawTight(g, tabLeft + intertabSpace + iconWidth, 1);
			if (tabOver == whichTab && frame.permitGoAway(whichTab)){
				if (frontness>0){ // a tab for a selected window
					if (frontness>1)   //the selected window on the selected tile (i.e. the one owning the menu bar)
						g.setColor(ColorTheme.getExtInterfaceElementContrast(w instanceof SystemWindow)); 
					else //a selected window, but in the unselected tile (i.e. the other tile not owning the menu bar)
						g.setColor(ColorTheme.getExtInterfaceElementContrast2(w instanceof SystemWindow));
				}
				else  //a tab for a window hidden in the background
					g.setColor(ColorTheme.getExtInterfaceElement(w instanceof SystemWindow));
				g.fillRect(tabLeft  +getGoAwayControlLeft(), getGoAwayControlTop(), 16, 12);
				g.drawImage(goawayMouseOver, tabLeft +getGoAwayControlLeft(), getGoAwayControlTop(), this);
			}
			else if (icon != null)
				g.drawImage(icon, tabLeft + intertabSpace, 6, this);

		}
		else {
			g.drawString(title, tabLeft + intertabSpace + iconWidth, panelHeight -12);
			if (tabOver == whichTab && frame.permitGoAway(whichTab)){
				if (frontness>0){ // a tab for a selected window
					if (frontness>1)   //the selected window on the selected tile (i.e. the one owning the menu bar)
						g.setColor(ColorTheme.getExtInterfaceElementContrast(w instanceof SystemWindow)); 
					else //a selected window, but in the unselected tile (i.e. the other tile not owning the menu bar)
						g.setColor(ColorTheme.getExtInterfaceElementContrast2(w instanceof SystemWindow));
				}
				else  //a tab for a window hidden in the background
					g.setColor(ColorTheme.getExtInterfaceElement(w instanceof SystemWindow));
				g.fillRect(tabLeft  +getGoAwayControlLeft(), getGoAwayControlTop(), 16, 12);
				g.drawImage(goawayMouseOver, tabLeft +getGoAwayControlLeft(), getGoAwayControlTop(), this);
			}
			else if (icon != null)
				g.drawImage(icon, tabLeft + intertabSpace, panelHeight -22, this);
		}
		if (w == frame.projectWindow){
			if (minimize != null && !w.isMinimized())
				g.drawImage(minimize, tabLeft+intertabSpace, 12, this); //tabRight-17
			else if (mediumize != null && w.isMinimized())
				g.drawImage(mediumize, tabLeft+intertabSpace, 12, this);//tabRight-17
		}
		g.setClip(oldClip );
	}

	/*.................................................................................................................*/

	private boolean isProjectWindow(MesquiteWindow w) {
		return w==frame.projectWindow;
	}

	int paintCount =0;
	int panelWidth;
	int panelHeight;
	int projectPanelWidth;
	double scaling = 1.0;
	int fontChosen = 0;

	/*.................................................................................................................*/
	public String getTabTitle(int i) {
		if (i<0 || i>=frame.windows.size())
			return "";
		MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
		String s = w.getTitle();
		if (w == frame.projectWindow && projectPanelWidth<=0)
			s = "Project";
		return s;
	}

	/*.................................................................................................................*/
	public void paint(Graphics g){

		//frame.ontabDark = new Color(160, 82, 45);
		if (!(g instanceof Graphics2D))
			return;
		paintCount++;
		Graphics2D g2 = (Graphics2D)g;

		if (!frame.checkInsets(true)){
			repaint();
			return;
		}
		projectPanelWidth = 0;
		if (frame.projectWindow != null)
			projectPanelWidth = frame.projectWindow.getBounds().width;
		if (InterfaceManager.isEditingMode()){
			Color co = g2.getColor();
			g2.setColor(Color.cyan);
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.setColor(co);
		}
		panelWidth = getBounds().width;
		panelHeight = getBounds().height;
		Vector frameWindows = (Vector)frame.windows.clone();

		g2.setColor(ColorTheme.getContentFrame());
		//	BasicStroke stroke = new BasicStroke(4);
		g2.drawLine(projectPanelWidth+MesquiteFrame.cornerBuffer, panelHeight-1, panelWidth, panelHeight-1);
		g2.fillRect(projectPanelWidth+MesquiteFrame.cornerBuffer, panelHeight-4, panelWidth-4, 4);


		//g.fillRect(projectPanelWidth, height-4, width, 4);
		//g.setColor(Color.red);
		//	g.fillRect(projectPanelWidth-3, panelHeight-3, panelWidth, 3);
		//g.setColor(Color.green);
		//g.fillRect(projectPanelWidth-2, panelHeight-2, panelWidth, 2);
		//	g.setColor(ColorDistribution.veryDarkMesquiteBrown);

		int numWindows = frameWindows.size();
		int numTabs = numWindows;
		if (numTabs<2 && frame.frontMostInLocation(MesquiteFrame.RESOURCES) == null) //if has resources window, then show tabs
			return;
		if (lefts == null || lefts.length != numTabs){
			lefts = new int[numTabs];
			rights = new int[numTabs];
		}
		String totalString = "";
		int iconNumber = 0;
		String[] titles = new String[numWindows];
		for (int i = 0; i<numWindows; i++){
			MesquiteWindow w = (MesquiteWindow)frame.windows.elementAt(i);
			String s = w.getTitle();
			if (w == frame.projectWindow && projectPanelWidth<=0)
				s = "Project";
			titles[i] = s;
			if (s != null)
				totalString += s;
			if (w.getIcon() !=null)
				iconNumber ++;
		}
		int iconsWidth = iconNumber * 20;

		int edges = (intertabSpace + defaultBackEdge) * numTabs; //NOTE: this is not calculated quite properly, causing David's compaction when many windows
		if (frame.frontMostInLocation(MesquiteFrame.POPTILE)!=null)
			edges += 30;
		//~~~~~~Setting up sizing/scaling~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		fontChosen = 0;
		scaling = 1.0;
		boolean ready = false;
		int narrowest = 5000;
		int widest = 0;
		int numLoops = 0;

		while (!ready && numLoops<100){  //checking scaling/sizing
			numLoops++;
			fontChosen = 0;
			g2.setFont(fonts[0]);

			int needed = 0;
			while ((needed = StringUtil.getStringDrawLength(g2, totalString) + iconsWidth +edges) > panelWidth && fontChosen<4){  //was i<fonts.length-1
				fontChosen++;
				g2.setFont(fonts[fontChosen]);
			}
			needed = StringUtil.getStringDrawLength(g2, totalString) + iconsWidth +edges;
			scaling = 1.0;
			if (needed> panelWidth-edges){
				scaling = (panelWidth-edges-projectPanelWidth-MesquiteFrame.cornerBuffer)*1.0/(StringUtil.getStringDrawLength(g2, totalString) + iconsWidth);
			}

			//trial run the tab widths
			narrowest = 500000;
			widest = 0;
			int totalWidth = 0;
			//			int leftMargin = 0;
			//			int rightMargin = panelWidth - defaultBackEdge;
			//	int i = 0;
			for (int i = 0; i<numWindows; i++){
				MesquiteWindow w = (MesquiteWindow)frameWindows.elementAt(i);
				if (!isProjectWindow(w) && w.isVisible()) {   // not the project window
					/*				int backSpace;
				if (w == frame.projectWindow){
					backSpace = defaultBackEdge + 20;
				}
				else {
					backSpace = defaultBackEdge;
				}
					 */
					boolean popped = w.getTileLocation() == MesquiteFrame.POPTILE;
					Image icon = w.getIcon();
					int iconWidth = 0;
					if (icon != null)
						iconWidth = 20;
					/*				if (!popped)
					leftMargin += intertabSpace;
				else 
					rightMargin -= intertabSpace;
					 */
					String title=null;
					if (i>=0 && i<titles.length)
						title = titles[i];
					int sw = 0;
					if (StringUtil.notEmpty(title)) {
						sw = StringUtil.getStringDrawLength(g2, title);
						int offer = sw + iconWidth;
						if (scaling < 1.0)
							offer = (int)(scaling * offer);
						/*					if (!popped)
						leftMargin += offer;
					else
						rightMargin -= offer;		
						 */
						totalWidth += offer;
						if (offer + iconWidth > widest)
							widest = offer + iconWidth;
						else if (offer + iconWidth < narrowest){
							narrowest = offer + iconWidth;
						}
					} else {
					}

				}
			}		



			//if some tabs are very narrow AND there is a big variance in tab size, then delete characters from long tabs and try again 			
			if (1.0*widest/narrowest > 2.0 && narrowest < 30 && widest>30 && totalWidth + 100 > panelWidth){
				//Delete last character of longest string & recalculate totalString
				int max = 0;
				int whichMax = -1;
				for (int k = 0; k<titles.length; k++){
					int thisL = StringUtil.getStringDrawLength(g2, titles[k]);
					if (thisL>max){
						whichMax = k;
						max = thisL;
					}
				}
				if (whichMax>=0 && whichMax<titles.length)
					titles[whichMax] = titles[whichMax].substring(0, titles[whichMax].length()-1);
				totalString = "";
				for (int k = 0; k<titles.length; k++){
					totalString += titles[k];
				}
			}
			else 
				ready = true;
		}
		if (fontChosen >3 && narrowest > 20){
			fontChosen = 3;
			g2.setFont(fonts[fontChosen]);
			int needed = StringUtil.getStringDrawLength(g2, totalString) + iconsWidth +edges;
			scaling = 1.0;
			if (needed> panelWidth-edges-projectPanelWidth-MesquiteFrame.cornerBuffer){  // remove "projectPanelWidth" here and in next line if other tabs can go over project panel
				scaling = (panelWidth-edges-projectPanelWidth-MesquiteFrame.cornerBuffer)*1.0/(StringUtil.getStringDrawLength(g2, totalString) + iconsWidth);
			}
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		int leftMargin = 0;  //this is the current left margin, moving rightward as left-side tabs are added
		int rightMargin = panelWidth - intertabSpace;//this is the current right margin, moving leftward as right-side tabs are added

		//~~~~~~~~~~~~~~~Drawing tab by tab ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		for (int i = 0; i<numWindows; i++){
			int backSpace;
			MesquiteWindow w = (MesquiteWindow)frameWindows.elementAt(i);
			if (isProjectWindow(w)){ //project window has minimize button
				backSpace = defaultBackEdge + 20;
			}
			else {
				backSpace = defaultBackEdge;
			}
			int tabLeft, tabRight;     //these are the left and right edges of the current tab

			boolean popped = w.getTileLocation() == MesquiteFrame.POPTILE;
			Image icon = w.getIcon();
			int iconWidth = 0;
			if (icon != null)
				iconWidth = 20;
			if (!popped){
				leftMargin += intertabSpace;
				tabLeft = leftMargin;
				tabRight = leftMargin + backSpace-2;
			}
			else {
				rightMargin -= intertabSpace;
				tabRight = rightMargin -2;
				tabLeft = rightMargin - intertabSpace;
			}

			String title = null;
			if (i>=0 && i<titles.length)
				title = titles[i];
			if (title != null){
				if (isProjectWindow(w)){
					tabLeft = 0;
					tabRight = projectPanelWidth+MesquiteFrame.cornerBuffer;
					if (projectPanelWidth <0){
						title = "Project";
						titles[i] = title;
						tabRight = 64; //StringUtil.getStringDrawLength(g2, title) + iconWidth + intertabSpace;
					}
					leftMargin = tabRight;
				} 
				else if (scaling < 1.0){
					int wish = StringUtil.getStringDrawLength(g2, title) + iconWidth;
					int offer = (int)(scaling * wish);
					if (!popped){
						tabRight = leftMargin + offer + backSpace-2;
						leftMargin = tabRight;
					}
					else {
						tabLeft = rightMargin - offer - intertabSpace - backSpace +2;
						rightMargin = tabLeft;
					}					
				}
				else {
					int offer = StringUtil.getStringDrawLength(g2, title) + iconWidth;
					if (!popped) {
						tabRight = leftMargin + offer + backSpace-2;
						leftMargin = tabRight;
					}
					else {
						tabLeft = rightMargin - offer - intertabSpace - backSpace +2;
						rightMargin = tabLeft;
					}
				}
				drawAndFillTab (g2, w,  tabLeft, tabRight,  i, scaling);

			}
			else 
				MesquiteMessage.println("window without title " + w.getClass());

			/*if (w == frame.projectWindow){
				if (minimize != null && !w.isMinimized())
					g2.drawImage(minimize, tabRight-17, 4, this);
				else if (mediumize != null && w.isMinimized())
					g2.drawImage(mediumize, tabRight-17, 4, this);
			}*/



			g2.setColor(ColorTheme.getExtInterfaceEdgeContrast(w instanceof SystemWindow)); //Color.black);
			try{
				lefts[i] = tabLeft;
				rights[i] = tabRight;
			}
			catch (ArrayIndexOutOfBoundsException e){
				repaint();
			}
		}


		// this little section draws the rounded corner on the upper (and outer) left side of the main part of the window
		g2.setColor(ColorTheme.getContentFrame());
		Stroke st = g2.getStroke();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		MesquitePath2DFloat path = new MesquitePath2DFloat();
		if (path.OK()) {
			int adjust = 0;
			int pathLeft = projectPanelWidth;
			int pathRight = pathLeft+12;  //+14
			int pathTop = getBounds().height-FrameTabsPanel.lowerBarHeight-1;
			if (projectPanelWidth <=0)  //project panel closed, so lower the curve a bit to make it not leave negative corner
				pathTop +=1;
			int pathBottom = pathTop+MesquiteFrame.cornerBuffer+8;  //+10
			path.moveTo(adjust+pathRight, adjust+pathTop);
			path.lineTo(adjust+pathRight, adjust+pathBottom);
			path.lineTo(adjust+pathLeft,adjust+pathBottom);
			path.curveTo(adjust+pathLeft, adjust+pathTop+(pathBottom-pathTop)/3, adjust+pathLeft+(pathRight-pathLeft)/3, adjust+pathTop, adjust+pathRight, adjust+pathTop);
			path.closePath();
			path.fill(g2);

		}
		else {
			int pathLeft = projectPanelWidth;
			int pathRight = projectPanelWidth+4;  //+14
			int pathTop = panelHeight;
			g2.fillRect(pathLeft+4, pathTop-4, pathRight-pathLeft, 4);
			g2.fillRect(pathLeft+3, pathTop-3, pathRight-pathLeft, 3);
			g2.fillRect(pathLeft+2, pathTop-2, pathRight-pathLeft, 2);
			g2.fillRect(pathLeft+1, pathTop-1, pathRight-pathLeft, 1);
			g2.drawLine(pathLeft+3, pathTop-1, pathRight, pathTop-1);
		}

		g2.setStroke(st);

	}
	/*.................................................................................................................*/

	void drawTab(Graphics g, int whichTab, MesquiteWindow w, int frontness, int tabLeft, int tabRight, int height, int projectPanelWidth, boolean isProjectTab, boolean isMesquiteWindow){
		// to figure out whether strong lower edge of tab is needed, find out left edge of poptile, if showing
		boolean forceThickLine = false;
		if (frame.leftPoptile>0){
			boolean popped = w.getTileLocation() == MesquiteFrame.POPTILE;
			if (popped && tabRight<frame.leftPoptile)
				forceThickLine = true;
			else if (!popped && !isMesquiteWindow && !isProjectTab && tabLeft>frame.leftPoptile)
				forceThickLine = true;
		}
		int roundness = 10;  
		int top = 4;  
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (frontness>0){ // a tab for a selected window
			if (frontness>1){   //the selected window on the selected tile (i.e. the one owning the menu bar)

				g2.setColor(ColorTheme.getExtInterfaceElementContrast(isMesquiteWindow)); 
				g2.fillRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);  //background of tab
				g2.setColor(ColorTheme.getExtInterfaceEdgeContrast(isMesquiteWindow));
				g2.drawRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);  //highlight edge of tab
				g2.setColor(Color.black);  //black
				g2.drawRoundRect(tabLeft-1, top-1, tabRight - tabLeft, height+62, roundness+1, roundness+1); //primary edge of tab
				if (forceThickLine){
					g2.setColor(ColorTheme.getContentFrame());
					g2.fillRect(tabLeft-1, height-4, tabRight-tabLeft+1, 4);
					g2.drawLine(tabLeft-1, height-1, tabRight, height-1);
				}
				else if (!isMesquiteWindow){
					g2.setColor(Color.lightGray);
					g2.drawLine(tabLeft-1, top+height-5, tabRight, top+height-5);  //line under the tab
				}
			}
			else { //a selected window, but in the unselected tile (i.e. the other tile not owning the menu bar)
				g2.setColor(ColorTheme.getExtInterfaceElementContrast2(isMesquiteWindow));
				g2.fillRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness); //background of tab

				//g.setColor(ColorTheme.getExtInterfaceEdgeContrast());
				//	g.drawRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);
				g2.setColor(Color.darkGray);
				g2.drawRoundRect(tabLeft-1, top-1, tabRight - tabLeft, height+62, roundness+1, roundness+1);  //primary edge of tab

				if (forceThickLine){
					g2.setColor(ColorTheme.getContentFrame());
					g2.fillRect(tabLeft-1, height-4, tabRight-tabLeft+1, 4);
					g2.drawLine(tabLeft-1, height-1, tabRight, height-1);
				}
				else {
					g2.setColor(Color.lightGray);
					g2.drawLine(tabLeft-1, top+height-5, tabRight, top+height-5);//line under the tab
				}
			}
		}
		else { //a tab for a window hidden in the background
			g2.setColor(ColorTheme.getExtInterfaceElement(isMesquiteWindow));
			g2.fillRoundRect(tabLeft, top, tabRight - tabLeft - 2, height+60, roundness, roundness);
			if (!isProjectTab){
				g2.setColor(ColorTheme.getExtInterfaceEdgeMuted(isMesquiteWindow));
				g2.drawRoundRect(tabLeft-1, top-1, tabRight - tabLeft, height+62, roundness+1, roundness+1);
			}
			//now about to draw the lines/border beneath the tab


			if (tabLeft> projectPanelWidth+MesquiteFrame.cornerBuffer ){ //entirely over main windows
				g2.setColor(ColorTheme.getContentFrame());
				//g2.setColor(Color.red);
				g2.fillRect(tabLeft-1, height-4, tabRight-tabLeft+1, 4);
				g2.drawLine(tabLeft-1, height-1, tabRight, height-1);
			}
			else { //extending over project panel, but not project tab
				g2.setColor(getBackground());
				g2.fillRect(tabLeft-1, height-4, tabRight-tabLeft+1, tabBottomLineHeight);
				g2.setColor(ColorTheme.getContentFrame());
				if (!isProjectTab) {
					if (whichTab==1) {

						//g2.fillRect(tabLeft-4, height-tabBottomLineHeight, 5, tabBottomLineHeight);
						MesquitePath2DFloat path = new MesquitePath2DFloat();
						if (path.OK()) {
							int rightPath = 7;
							int topPath = 6;
							g2.fillRect(tabLeft+rightPath-1, height-tabBottomLineHeight, projectPanelWidth+MesquiteFrame.cornerBuffer-tabLeft+4, tabBottomLineHeight);
							path.moveTo(tabLeft-1, height-topPath);
							path.lineTo(tabLeft, height-topPath);
							path.curveTo(tabLeft+2, height-4,tabLeft+4, height-4, tabLeft+rightPath, height-tabBottomLineHeight);
							path.lineTo(tabLeft+rightPath, height);
							path.curveTo(tabLeft-1, height-2,tabLeft+2, height-4, tabLeft-1, height-topPath);
							path.closePath();
							path.fill(g2);
						}
						else {
							g2.fillRect(tabLeft+1, height-tabBottomLineHeight, projectPanelWidth+MesquiteFrame.cornerBuffer-tabLeft-1+5, tabBottomLineHeight);
							g2.fillRect(tabLeft, height-5, 3, 3);
							g2.fillRect(tabLeft, height-7, 1, 4);
						}

					} else {
						g2.fillRect(tabLeft-4, height-tabBottomLineHeight, projectPanelWidth+MesquiteFrame.cornerBuffer-tabLeft+4, tabBottomLineHeight);
					}

				}
				if (tabRight> projectPanelWidth){
					MesquitePath2DFloat path = new MesquitePath2DFloat();
					if (path.OK()) {
						int adjust = 0;
						int pathLeft = 0;
						int pathRight = 0+4;  //+14
						int pathTop = height;
						int pathBottom = height+4;  //+10
						path.moveTo(adjust+pathRight, adjust+pathTop);
						path.lineTo(adjust+pathLeft, adjust+pathTop);
						path.lineTo(adjust+pathLeft,adjust+pathBottom);
						path.curveTo(adjust+pathLeft, adjust+pathTop+(pathBottom-pathTop)/3, adjust+pathLeft+(pathRight-pathLeft)/3, adjust+pathTop, adjust+pathRight, adjust+pathTop);
						path.closePath();
						path.fill(g2);
					}
					else {
						g2.fillRect(projectPanelWidth+4, height-4, tabRight-projectPanelWidth, 4);
						g2.fillRect(projectPanelWidth+3, height-3, tabRight-projectPanelWidth, 3);
						g2.fillRect(projectPanelWidth+2, height-2, tabRight-projectPanelWidth, 2);
						g2.fillRect(projectPanelWidth+1, height-1, tabRight-projectPanelWidth, 1);
						g2.drawLine(projectPanelWidth+3, height-1, tabRight, height-1);
					}

				}

			}


		}

	}
	/*.................................................................................................................*/

}
class BetweenPanel extends MousePanel{
	MesquiteFrame f;

	public BetweenPanel(MesquiteFrame f){ 
		super();
		setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
		this.f=f;
		//setBackground(ColorDistribution.darkMesquiteBrown);
		//setBackground(Color.yellow);
		//setBackground(Color.darkGray);
	}
	int touchX = -1;
	public void paint (Graphics g) {
		if (!(g instanceof Graphics2D))
			return;
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(ColorTheme.getContentFrame());
		g2.fillRect(0,MesquiteFrame.cornerBuffer-1,getBounds().width, getBounds().height);
		Stroke st = g2.getStroke();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		MesquitePath2DFloat path = new MesquitePath2DFloat();
		if (path.OK()) {
			g2.setColor(ColorTheme.getContentFrame());
			int adjust = 0;
			int pathLeft = 0;
			int pathRight = 12;  //+14
			int pathTop = 0-FrameTabsPanel.lowerBarHeight;
			int pathBottom = 8;  //+10
			path.moveTo(adjust+pathRight, adjust+pathTop);
			path.lineTo(adjust+pathRight, adjust+pathBottom);
			path.lineTo(adjust+pathLeft,adjust+pathBottom);
			path.curveTo(adjust+pathLeft, adjust+pathTop+(pathBottom-pathTop)/3, adjust+pathLeft+(pathRight-pathLeft)/3, adjust+pathTop, adjust+pathRight, adjust+pathTop);
			path.closePath();
			path.fill(g2);
		}
		else {
			g2.setColor(ColorTheme.getContentFrame());
			int pathLeft = 0;
			int pathRight = 12;  //+14
			int pathTop = 0;
			int pathBottom = 8;  //+10
			g2.fillRect(pathLeft+4, pathTop-4, pathRight-pathLeft, 4);
			g2.fillRect(pathLeft+3, pathTop-3, pathRight-pathLeft, 3);
			g2.fillRect(pathLeft+2, pathTop-2, pathRight-pathLeft, 2);
			g2.fillRect(pathLeft+1, pathTop-1, pathRight-pathLeft, 1);
			g2.drawLine(pathLeft+3, pathTop-1, pathRight, pathTop-1);
		}

		g2.setStroke(st);


	}
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



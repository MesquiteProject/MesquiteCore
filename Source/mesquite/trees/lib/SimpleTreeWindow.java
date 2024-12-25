/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.lib;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/* ======================================================================== */
public class SimpleTreeWindow extends MesquiteWindow  {
	public TreeDisplay treeDisplay;
	public DrawTreeCoordinator treeDrawCoordTask;
	int totalWidth;
	int totalHeight;
	int firstTree=0;
	public MessagePanel messagePanel;
	Taxa taxa;
	public int highlightedBranch=0;
	TreeTool arrowTool;
	String defaultExplanation;
	ScrollPane scrollPane;
	protected Tree tree;
	protected SimpleTreeWindowMaker ownerModule;
	public SimpleTreeWindow ( SimpleTreeWindowMaker ownerModule, DrawTreeCoordinator treeDrawCoordTask){
		super(ownerModule, true); //infobar
		this.ownerModule = ownerModule;
		this.treeDrawCoordTask = treeDrawCoordTask;
		setShowExplanation(true);
		defaultExplanation = ownerModule.getDefaultExplanation();
		setExplanation(defaultExplanation);
		setWindowSize(500,400);
		this.ownerModule = ownerModule;
		//setLayout( null );
		arrowTool = new TreeTool(this, "arrow", MesquiteModule.getRootImageDirectoryPath(),"arrow.gif", 4,2,"Select", null);
		arrowTool.setIsArrowTool(true);
		arrowTool.setTouchedCommand(MesquiteModule.makeCommand("arrowTouch",  this));
		addTool(arrowTool);
		setCurrentTool(arrowTool);
		if (arrowTool!=null)
			arrowTool.setInUse(true);
		setBackground(Color.white);
		messagePanel=new MessagePanel(getColorScheme());
		addToWindow(messagePanel);
		messagePanel.setVisible(true);
		taxa = ownerModule.getProject().getTaxa(0); //TODO: IN FUTURE ALLOW different
		MesquiteSubmenuSpec mss = ownerModule.addSubmenu(null, "Analysis", MesquiteModule.makeCommand("newAssistant",  this), TreeDisplayAssistantA.class);
		mss = ownerModule.addSubmenu(null, "Display", MesquiteModule.makeCommand("newAssistantD",  this), TreeDisplayAssistantD.class);
		treeDisplay =treeDrawCoordTask.createOneTreeDisplay(taxa, this); //TODO: set tree display when tree is set for first time
		scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		scrollPane.add(treeDisplay);
		sizeDisplays();
		addToWindow(scrollPane);
		resetTitle();
	}
	/*.................................................................................................................*/
	public void addAssistant(TreeDisplayAssistant tda) {
		tda.setEmployeesInStartup(true);  // normally used only within EmployerEmployee, this helps assistants know they are still in startup phase
		treeDrawCoordTask.addAssistantTask(tda);
		TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplay);
		if (tce==null) 
			return;
		tce.setTree(tree);
		treeDisplay.addExtra(tce);
		treeDisplay.pleaseUpdate(false);
		if (getMode()>0)
			updateTextPage();
		tda.setEmployeesInStartup(false);
	}
	/*.................................................................................................................*/
	/** to be overridden by MesquiteWindows for a text version of their contents*/
	public String getTextContents() {

		String s = "";
		if (ownerModule.isDoomed())
			return"";
		if (tree != null) {
			s += "Tree: " + tree.writeTree() + "\n";
			s += "  " + treeDisplay.getTextVersion();
		}
		return s;

	}
	String title = "Tree";
	public void setWindowTitle(String t){
		title = t;
		resetTitle();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle(title); //TODO: what tree?
	}
	/*.................................................................................................................*/
	public int getNumSnapshotLines(MesquiteFile file) {
		int tot = 0;
		for (int i = 0; i<ownerModule.getNumberOfEmployees(); i++) {
			Object e=ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof TreeDisplayAssistantA) {
				tot++;
			}
		}
		return tot;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.incorporate(super.getSnapshot(file), false);
		ListableVector extras = treeDisplay.getExtras();
		if (extras!=null) {
			Enumeration enumeration=extras.elements();
			while (enumeration.hasMoreElements()){
				TreeDisplayExtra tde = (TreeDisplayExtra)enumeration.nextElement();
				MesquiteModule mb = tde.getOwnerModule();
				if (mb instanceof TreeDisplayAssistantA) {
					temp.addLine("newAssistant", mb);
				}
				else if (mb instanceof TreeDisplayAssistantD) {
					temp.addLine("newAssistantD", mb);
				}
			}
		}

		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns module coordinating tree drawing", null, commandName, "getTreeDrawCoordinator")) {
			return treeDrawCoordTask;
		}
		else if (checker.compare(this.getClass(), "Hires new assistant module (TreeDisplayAssistantA)", "[name of module]", commandName, "newAssistant")) {
			TreeDisplayAssistantA tda= (TreeDisplayAssistantA)ownerModule.hireNamedEmployee(TreeDisplayAssistantA.class, arguments);
			if (tda!=null){
				addAssistant(tda);
				if (!MesquiteThread.isScripting())
					ownerModule.resetContainingMenuBar();
				return tda;
			}
		}
		else if (checker.compare(this.getClass(), "Root current tree along branch", "[branch number]", commandName, "rootAlongBranch")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int atBranch= MesquiteInteger.fromString(arguments, io);
			if (tree instanceof MesquiteTree) {
				if (atBranch >0 &&  ((MesquiteTree)tree).reroot(atBranch, tree.getRoot(), true)) {
					((MesquiteTree)tree).standardize(tree.getRoot(), true, true);
				}
			}

		}
		else if (checker.compare(this.getClass(), "Hires new assistant module (TreeDisplayAssistantA)", "[name of module]", commandName, "newAssistantD")) {
			TreeDisplayAssistantD tda= (TreeDisplayAssistantD)ownerModule.hireNamedEmployee(TreeDisplayAssistantD.class, arguments);
			if (tda!=null){
				addAssistant(tda);
				if (!MesquiteThread.isScripting())
					ownerModule.resetContainingMenuBar();
				return tda;
			}
		}
		else if (checker.compare(this.getClass(), "Sets size of window", "[width in pixels of window] [height in pixels of window]", commandName, "setSize")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int width= MesquiteInteger.fromString(arguments, io);
			int height= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(width) && MesquiteInteger.isCombinable(height)) {
				fromScriptCommand = true;//this is needed to counteract difficulties with popping in/out and size setting in window constructors
				setWindowSize(width, height);
				fromScriptCommand = false;
				sizeDisplays();
			}
		}
		else if (checker.compare(this.getClass(), "Ladderizes the clade", "[branch number]", commandName, "ladderize")) {
			if (!(tree instanceof MesquiteTree))
				return null;
			Parser parser = new Parser();
			String s = parser.getFirstToken(arguments);
			int branchFound= MesquiteInteger.fromString(s);
			if (s.equalsIgnoreCase("root"))
				branchFound = tree.getRoot();
			else
				branchFound= MesquiteInteger.fromString(s);
			if (branchFound >0) {
				boolean direction = true;
				if (arguments.indexOf("option")>=0)
					direction = false;
				 if (((MesquiteTree)tree).standardize(branchFound, direction, true)){
					treeDisplay.recalculatePositions();
					treeDisplay.repaint();
					}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	int minFieldWidth = -1;
	int minFieldHeight = -1;
	public void setMinimumFieldSize(int w, int h){  // if either parameter is negative, then use natural window size
		minFieldWidth = w;
		minFieldHeight = h;
	}
	
	public int getOrientation(){
		if (treeDisplay == null)
			return -1;
		return treeDisplay.getOrientation();
	}
	/*.................................................................................................................*/
	public void sizeDisplays(boolean resetScrollLocation){
		if (treeDisplay == null || messagePanel == null)
			return;
		totalWidth = getWidth();
		totalHeight = getHeight() - 16;
		if (resetScrollLocation) 
			treeDisplay.setLocation(0,0); 
		else {
			Point loc = treeDisplay.getLocation();
			if (loc.x>treeDisplay.getWidth())
				loc.x=treeDisplay.getWidth();
			treeDisplay.setLocation(loc);
		}
		scrollPane.setSize(totalWidth,totalHeight);
		if (resetScrollLocation) 
			scrollPane.setLocation(0,0);
		else {
			Point loc = scrollPane.getLocation();
			if (loc.x>scrollPane.getWidth())
				loc.x=scrollPane.getWidth();
			if (loc.y>totalHeight)
				loc.y=totalHeight;
			scrollPane.setLocation(loc);
		}
		
		int w = totalWidth;
		int h = totalHeight;
		if (w<minFieldWidth)
			w = minFieldWidth;
		if (h<minFieldHeight)
			h = minFieldHeight;
		
		treeDisplay.setSize(w,h);
		treeDisplay.setFieldSize(w, h);
		messagePanel.setSize(totalWidth, 16);
		messagePanel.setLocation(0, totalHeight);
		resetDisplay(treeDisplay);
	}
	/*.................................................................................................................*/
	public void sizeDisplays(){
		sizeDisplays(true);
	}
	void resetDisplay(TreeDisplay treeDisplay){
		treeDisplay.setVisRect(new Rectangle(0, 0, treeDisplay.getWidth(), treeDisplay.getHeight()));
		Component[] cc = treeDisplay.getComponents();
		if (cc!=null && cc.length>0)
			for (int i=0; i<cc.length; i++) {
				if (cc[i] instanceof Legend){ //make sure legends are in bounds
					//adjustLocation
					Legend legend = (Legend)cc[i];
				
						legend.setConstrainingRectangle(treeDisplay.getBounds()); //treeDisplay.getBounds()
				
					legend.adjustLocation();
				}
			}
	}
	/*.................................................................................................................*/
	public TreeDisplay  getTreeDisplay(){
		return treeDisplay;
	}
	/*.................................................................................................................*/
	public void setTree(Tree newTree, boolean suppressDrawing){
		if (ownerModule.isDoomed())
				return;
		if (treeDisplay.getTree()!=null)
			treeDisplay.getTree().dispose();

		if (newTree!=null) {
			tree = newTree.cloneTree();//no need to establish listener to Taxa, as will be remade when needed?
			treeDisplay.setTree(tree);
			treeDisplay.suppressDrawing(suppressDrawing);
			treeDisplay.setVisible(true);
			treeDisplay.recalculatePositions();
			treeDisplay.forceRepaint();
			treeDisplay.setTreeAllExtras(tree);
			MesquiteModule employer = ownerModule.getEmployer();
			if (employer instanceof TreeWindowMaker && employer.getModuleWindow()!=null)
				messagePanel.setMessage(tree.getName() + " in " + employer.getModuleWindow().getName());
			else
				messagePanel.setMessage(tree.getName());  
		}
		else messagePanel.setMessage("Error: source tree is null");  
		messagePanel.repaint();
		if (ownerModule.contextListeners !=null) {
			Enumeration e = ownerModule.contextListeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeContextListener) {
					TreeContextListener tce = (TreeContextListener)obj;
					tce.treeChanged(tree);
				}
			}
		}
		contentsChanged();
	}

	/*.................................................................................................................*/
	public Tree getTree(){
		if (ownerModule.isDoomed())
				return null;
		return treeDisplay.getTree();
	}
	/*_________________________________________________*/
	public   void InvertBranchOld(TreeDisplay treeDisplay, Graphics g, int N) {
		if (ownerModule.isDoomed())
			return;
		highlightedBranch=N;
		treeDisplay.getTreeDrawing().fillBranchInverted(treeDisplay.getTree(), N, g);
	}
	/*_________________________________________________*/
	public   void RevertBranchOld(TreeDisplay treeDisplay, Graphics g, int N) {
		if (ownerModule.isDoomed())
			return;
		highlightedBranch=0;
		treeDisplay.getTreeDrawing().fillBranchInverted(treeDisplay.getTree(), N, g);
	}
	/*_________________________________________________*/
	public   void HighlightBranch(TreeDisplay treeDisplay, Graphics g, int N) {
		if (ownerModule.isDoomed())
			return;
		highlightedBranch=N;
		treeDisplay.getTreeDrawing().highlightBranch(treeDisplay.getTree(), N, g);
	}
	/*_________________________________________________*/
	public   void UnhighlightBranch(TreeDisplay treeDisplay, Graphics g, int N) {
		if (ownerModule.isDoomed())
			return;
		highlightedBranch=0;
		treeDisplay.getTreeDrawing().unhighlightBranch(treeDisplay.getTree(), N, g);
	}
	/*_________________________________________________*/
	public   void ScanFlash(TreeDisplay treeDisplay, Graphics g, int x, int y, int modifiers) {
		if (ownerModule.isDoomed())
				return;
		setExplanation(defaultExplanation);
		if (treeDisplay == null || treeDrawCoordTask == null || treeDrawCoordTask.getNamesTask() == null || treeDisplay.getTreeDrawing()==null)
			return;
		Tree tree = treeDisplay.getTree();
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot(); //TODO: remember drawnRoot!!!
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		MesquiteDouble d = new MesquiteDouble();
		int branchFound =treeDisplay.getTreeDrawing().findBranch(tree,  drawnRoot, x, y, d);
		if (highlightedBranch != 0) {
			if (branchFound==0) {
				notifyExtrasOfBranchExit(treeDisplay, g, highlightedBranch);
				UnhighlightBranch(treeDisplay, g, highlightedBranch);
			}
			else if (branchFound!=highlightedBranch)  {
				notifyExtrasOfBranchExit(treeDisplay, g, highlightedBranch);
				UnhighlightBranch(treeDisplay, g, highlightedBranch); 
				notifyExtrasOfBranchEnter(treeDisplay, g, branchFound);
				HighlightBranch(treeDisplay, g, branchFound);
			}
		}
		else if (branchFound!=0) {
			notifyExtrasOfBranchEnter(treeDisplay, g, branchFound);
			HighlightBranch(treeDisplay, g, branchFound); 
		}

	}
	/*_________________________________________________*/
	public   boolean ScanTouch(TreeDisplay treeDisplay, Graphics g, int x, int y, int modifiers) {
		if (ownerModule.isDoomed())
				return false;
	setExplanation(defaultExplanation);
		if (treeDisplay == null || treeDrawCoordTask == null || treeDrawCoordTask.getNamesTask() == null || treeDisplay.getTreeDrawing()==null)
			return false;
		Tree tree = treeDisplay.getTree();
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		MesquiteDouble d = new MesquiteDouble();
		int branchFound =treeDisplay.getTreeDrawing().findBranch(tree,  drawnRoot, x, y, d);
		if (branchFound!=0) {
			if (highlightedBranch != 0) {
				notifyExtrasOfBranchExit(treeDisplay, g, highlightedBranch);
				UnhighlightBranch(treeDisplay, g, highlightedBranch);
			}
			notifyExtrasOfBranchTouch(treeDisplay, g, branchFound);
			return true;
		}
		return false;
	}
	/*................................................................................................*/
	private void notifyExtrasOfBranchTouch(TreeDisplay treeDisplay,Graphics g, int N) {
		if (ownerModule.isDoomed())
				return;
		if (treeDisplay.getExtras()!=null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra)obj;
					tce.cursorTouchBranch(treeDisplay.getTree(), N, g);
				}
			}
		}
	}
	/*................................................................................................*/
	public void notifyExtrasOfBranchEnter(TreeDisplay treeDisplay, Graphics g, int N) {
		if (ownerModule.isDoomed())
				return;
		if (treeDisplay.getExtras()!=null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra)obj;
					tce.cursorEnterBranch(treeDisplay.getTree(), N, g);
				}
			}
		}
	}
	/*................................................................................................*/
	public void notifyExtrasOfBranchExit(TreeDisplay treeDisplay,Graphics g, int N) {
		if (ownerModule.isDoomed())
				return;
		if (treeDisplay.getExtras()!=null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra)obj;
					tce.cursorExitBranch(treeDisplay.getTree(), N, g);
				}
			}
		}
	}
	/*.................................................................................................................*/
	public void paintContents(Graphics g) {
		if (ownerModule.isDoomed())
			return;
		if (treeDisplay==null){
			MesquiteMessage.warnProgrammer("Oh no, tree display is null");
		}
		else {
			sizeDisplays();
		}

	}
	/*.................................................................................................................*/
	/** windows with manually resized components (i.e. null layoutmanagers) can override to respond to window resizing here */
	public void windowResized(){
		sizeDisplays();
	}
	public void dispose(){
		try{
		if (treeDisplay!=null){
			if (treeDisplay.getTree()!=null)
				treeDisplay.getTree().dispose();
			treeDisplay.dispose();
		}
		}
		catch (Throwable c){
		}
		super.dispose();
	}
}
/* ======================================================================== */
class SMessagePanel extends MQPanel {
	String message;

	public SMessagePanel(MesquiteWindow w) {  //in future pass general MesquiteWindow
		super();
		message="";
		setBackground(ColorTheme.getInterfaceBackgroundPale());

	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.drawRect(0,0, getBounds().width, getBounds().height);
		g.drawString(message,  4, 12);
		MesquiteWindow.uncheckDoomed(this);
	}

	public void setMessage(String s) {
		message = s;
		repaint();
	}	
}



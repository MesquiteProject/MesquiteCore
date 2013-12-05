/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.DependentTreeWindow;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class DependentTreeWindow extends TreeWindowAssistantN implements TreeDisplayActive {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DrawTreeCoordinator.class, getName() + "  needs a module to coordinate tree drawing.",
		"This is arranged automatically");
	}
	/*.................................................................................................................*/
	public DrawTreeCoordinator treeDrawCoordTask;
	DepTreeWindow dependentTreeWindow;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeDrawCoordTask= (DrawTreeCoordinator)hireEmployee(DrawTreeCoordinator.class, null);
		if (treeDrawCoordTask == null)
			return sorry(getName() + " couldn't start because no tree draw coordinator module found");
		makeMenu("Dependent");
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		if (m==treeDrawCoordTask)
			iQuit();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (source instanceof DrawTreeCoordinator){ //ignores since this should have directly called to update tree display
			return;
		}
		if (dependentTreeWindow != null)
			dependentTreeWindow.refresh();

		super.employeeParametersChanged(employee, source, notification);
	}
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (dependentTreeWindow == null){
			dependentTreeWindow= new DepTreeWindow( this, treeDrawCoordTask, tree.getTaxa());
			setModuleWindow(dependentTreeWindow);
			resetContainingMenuBar();
			employer.resetContainingMenuBar();
			resetAllWindowsMenus();
			dependentTreeWindow.sizeDisplays();
			if (!MesquiteThread.isScripting())
				dependentTreeWindow.setVisible(true);
		}
		dependentTreeWindow.setTree(tree);
	}
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (dependentTreeWindow ==null)
			return null;
		Snapshot fromWindow = dependentTreeWindow.getSnapshot(file);
		if (fromWindow == null || fromWindow.getNumLines() ==0)
			return null;
		Snapshot sn = new Snapshot();
		sn.addLine("getTreeDrawCoordinator", treeDrawCoordTask);
		sn.addLine("getWindow");
		sn.addLine("tell It");
		sn.incorporate(fromWindow, true);
		sn.addLine("endTell");
		sn.addLine("showWindow");

		return sn;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns module coordinating tree drawing", null, commandName, "getTreeDrawCoordinator")) {
			return treeDrawCoordTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
	public boolean mouseDownInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		if (!treeDisplay.getTree().isLocked())
			return dependentTreeWindow.ScanTouch(treeDisplay, g, x, y, modifiers);
		return false;
	}

	/*.................................................................................................................*/
	public boolean mouseUpInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		return true;
	}

	/*.................................................................................................................*/
	public boolean mouseMoveInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		if (!treeDisplay.getTree().isLocked())
			dependentTreeWindow.ScanFlash(treeDisplay, g, x, y, modifiers);
		return true;
	}
	/*.................................................................................................................*/
	public boolean mouseDragInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Dependent Tree Window";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Displays a single tree (the same as in a tree window)." ;
	}


}

/* ======================================================================== */
class DepTreeWindow extends MesquiteWindow implements Commandable, MesquiteListener  {
	public TreeDisplay treeDisplay;
	public DrawTreeCoordinator treeDrawCoordTask;
	int totalWidth;
	int totalHeight;
	int firstTree=0;
	public MessagePanel messagePanel;
	Taxa taxa;
	DepTreeExtra extra;
	public int highlightedBranch=0;
	TreeTool arrowTool;
	String defaultExplanation;
	public DepTreeWindow ( MesquiteModule ownerModule, DrawTreeCoordinator treeDrawCoordTask, Taxa taxa){
		super(ownerModule, true); //infobar
		this.treeDrawCoordTask = treeDrawCoordTask;
		setShowExplanation(true);
		defaultExplanation = "This window shows the same tree as seen in ";
		MesquiteWindow eW = ownerModule.getEmployer().getModuleWindow();
		if (eW !=null)
			defaultExplanation += eW.getTitle();
		else
			defaultExplanation += "a Tree Window";
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
		this.taxa =taxa;
		taxa.addListener(this);
		MesquiteSubmenuSpec mss = ownerModule.addSubmenu(null, "Analysis", MesquiteModule.makeCommand("newAssistant",  this), TreeDisplayAssistantA.class);
		mss = ownerModule.addSubmenu(null, "Display", MesquiteModule.makeCommand("newAssistantD",  this), TreeDisplayAssistantD.class);
		treeDisplay =treeDrawCoordTask.createOneTreeDisplay(taxa, this); //TODO: set tree display when tree is set for first time
		sizeDisplays();
		addToWindow(treeDisplay);
		extra = new DepTreeExtra(ownerModule, treeDisplay, this);
		treeDisplay.addExtra(extra);
		addAssistantsDI(ownerModule);
		resetTitle();
	}
	protected void addAssistantsDI(MesquiteModule ownerModule){
		ownerModule.hireAllEmployees(TreeDisplayAssistantDI.class);
		Enumeration e = ownerModule.getEmployeeVector().elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TreeDisplayAssistantDI) {
				TreeDisplayAssistant tda = (TreeDisplayAssistant)obj;
				treeDrawCoordTask.addAssistantTask(tda);
				TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplay);
				if (tce!=null) { 
					treeDisplay.addExtra(tce);
				}
			}
		}
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Dependent Tree Window"); //TODO: what tree?
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
		if (checker.compare(this.getClass(), "Hires new assistant module (TreeDisplayAssistantA)", "[name of module]", commandName, "newAssistant")) {
			TreeDisplayAssistantA tda= (TreeDisplayAssistantA)ownerModule.hireNamedEmployee(TreeDisplayAssistantA.class, arguments);
			if (tda!=null){
				treeDrawCoordTask.addAssistantTask(tda);

				TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplay);
				tce.setTree(treeDisplay.getTree());
				treeDisplay.addExtra(tce);
				treeDisplay.repaint();
				return tda;
			}
		}
		else if (checker.compare(this.getClass(), "Hires new assistant module (TreeDisplayAssistantA)", "[name of module]", commandName, "newAssistantD")) {
			TreeDisplayAssistantD tda= (TreeDisplayAssistantD)ownerModule.hireNamedEmployee(TreeDisplayAssistantD.class, arguments);
			if (tda!=null){
				treeDrawCoordTask.addAssistantTask(tda);

				TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplay);
				tce.setTree(treeDisplay.getTree());
				treeDisplay.addExtra(tce);
				treeDisplay.repaint();
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
				treeDisplay.redoCalculations(354);
				treeDisplay.forceRepaint();

			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void sizeDisplays(){
		if (treeDisplay == null)
			return;
		totalWidth = getWidth();
		totalHeight = getHeight() - 16;
		treeDisplay.setLocation(0,0);
		treeDisplay.setSize(totalWidth,totalHeight);
		treeDisplay.setFieldSize(totalWidth,totalHeight);
		treeDisplay.setVisRect(new Rectangle(0, 0, totalWidth,totalHeight));
		messagePanel.setSize(totalWidth, 16);
		messagePanel.setLocation(0, totalHeight);
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
		treeDisplay.redoCalculations(4417);
		treeDisplay.forceRepaint();
	}
	public void refresh(){
		treeDisplay.pleaseUpdate(true);
	}
	/*.................................................................................................................*/
	public void setTree(Tree newTree){
		Tree tree;
		if (newTree!=null) {
			if (treeDisplay.getTree()!=null)
				treeDisplay.getTree().dispose();
			if (taxa != newTree.getTaxa()){
				if (taxa != null)
					taxa.removeListener(this);
				taxa = newTree.getTaxa();
				taxa.addListener(this);
			}
			/*if (newTree.getTaxa() != taxa) {
				taxa = newTree.getTaxa();
				treeDisplay.setTaxa(taxa);
			}*/
			tree = newTree.cloneTree();
			treeDisplay.setTree(tree);
			treeDisplay.suppressDrawing(false);
			treeDisplay.setVisible(true);
			treeDisplay.repaint();
			treeDisplay.setTreeAllExtras(tree);
			MesquiteModule employer = ownerModule.getEmployer();
			if (employer instanceof TreeWindowMaker && employer.getModuleWindow()!=null)
				messagePanel.setMessage(tree.getName() + " in " + employer.getModuleWindow().getName());
			else
				messagePanel.setMessage(tree.getName());  
		}
		else messagePanel.setMessage("Error: source tree is null");  
		messagePanel.repaint();
	}
	/*.................................................................................................................*/
	/** passes which object changed (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof Taxa &&  (Taxa)obj ==taxa) {
			if (code==MesquiteListener.NAMES_CHANGED || code == MesquiteListener.SELECTION_CHANGED || code == MesquiteListener.ANNOTATION_CHANGED || code == MesquiteListener.ANNOTATION_DELETED || code == MesquiteListener.ANNOTATION_ADDED) 
				treeDisplay.pleaseUpdate(true);
		}
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	/*_________________________________________________*/

	public   void InvertBranch(TreeDisplay treeDisplay, Graphics g, int N) {

		highlightedBranch=N;
		if (!GraphicsUtil.useXORMode(g, true))
			return;
		g.setColor(Color.black);
		g.setXORMode(Color.white);  //for some reason color makes no difference in MacOS, but is inversion color in Win95 
		treeDisplay.getTreeDrawing().fillBranch(treeDisplay.getTree(), N, g);
		g.setPaintMode();
		g.setColor(Color.black);
	}

	/*_________________________________________________*/
	public   void RevertBranch(TreeDisplay treeDisplay, Graphics g, int N) {
		highlightedBranch=0;
		if (!GraphicsUtil.useXORMode(g, true))
			return;
		g.setColor(Color.black);
		g.setXORMode(Color.white);//for some reason color makes no difference in MacOS, but is inversion color in Win95
		treeDisplay.getTreeDrawing().fillBranch(treeDisplay.getTree(), N, g);
		g.setPaintMode();
		g.setColor(Color.black);
	}
	/*_________________________________________________*/
	public   void ScanFlash(TreeDisplay treeDisplay, Graphics g, int x, int y, int modifiers) {
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
				RevertBranch(treeDisplay, g, highlightedBranch);
			}
			else if (branchFound!=highlightedBranch)  {
				notifyExtrasOfBranchExit(treeDisplay, g, highlightedBranch);
				RevertBranch(treeDisplay, g, highlightedBranch); 
				notifyExtrasOfBranchEnter(treeDisplay, g, branchFound);
				InvertBranch(treeDisplay, g, branchFound);
			}
		}
		else if (branchFound!=0) {
			notifyExtrasOfBranchEnter(treeDisplay, g, branchFound);
			InvertBranch(treeDisplay, g, branchFound); 
		}

	}
	/*_________________________________________________*/
	public   boolean ScanTouch(TreeDisplay treeDisplay, Graphics g, int x, int y, int modifiers) {
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
				RevertBranch(treeDisplay, g, highlightedBranch);
			}
			notifyExtrasOfBranchTouch(treeDisplay, g, branchFound);
			return true;
		}
		else {
			if (getCurrentTool() instanceof TreeTool){
			boolean fieldTouchAccepted = ((TreeTool)getCurrentTool()).fieldTouched(x,y,tree,modifiers);
			//notify extras?
			return fieldTouchAccepted;
			}
		}
		return false;
	}
	/*................................................................................................*/
	private void notifyExtrasOfBranchTouch(TreeDisplay treeDisplay,Graphics g, int N) {
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
	public void windowResized(){
		sizeDisplays();
	}
	/*.................................................................................................................*/
	public void paintContents(Graphics g) {
		if (treeDisplay==null){
			MesquiteMessage.warnProgrammer("Oh no, tree display is null");
		}
		else {
			sizeDisplays();
		}

	}
	/*.................................................................................................................*/
	public String getTextContents() {
		if (treeDisplay==null)
			return "";
		String s = "Dependent tree window\n";
		if (messagePanel !=null)
			s += "Showing " + messagePanel.getMessage();
		if (taxa!=null)
			s += " for taxa \"" + taxa.getName() + "\"";
		s += "\n\n"  + treeDisplay.getTextVersion();
		return s;
	}
	public void dispose(){
		if (taxa != null)
			taxa.removeListener(this);

		if (treeDisplay!=null){
			if (treeDisplay.getTree()!=null)
				treeDisplay.getTree().dispose();
			treeDisplay.dispose();
		}
		super.dispose();
	}
}
/* ======================================================================== */
class DepMessagePanel extends Panel {
	String message;

	public DepMessagePanel(MesquiteWindow w) {  //in future pass general MesquiteWindow
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

	public String getMessage(){
		return message;
	}
}
/* ======================================================================== */
class DepTreeExtra extends TreeDisplayExtra {
	DepTreeWindow treeWindow;
	public DepTreeExtra (MesquiteModule ownerModule, TreeDisplay treeDisplay, DepTreeWindow treeWindow) {
		super(ownerModule, treeDisplay);
		this.treeWindow = treeWindow;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		//treeWindow.sizeDisplays();
	}
	public   void setTree(Tree tree) {
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
}



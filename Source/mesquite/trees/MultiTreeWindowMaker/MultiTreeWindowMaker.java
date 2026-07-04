/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.MultiTreeWindowMaker;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Enumeration;
import java.util.Vector;

import mesquite.categ.lib.MolecularData;
import mesquite.charMatrices.BasicDataWindowCoord.BasicDataWindowCoord;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.Commandable;
import mesquite.lib.Debugg;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteEvent;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTimer;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Notification;
import mesquite.lib.ParseUtil;
import mesquite.lib.Puppeteer;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowMaker;
import mesquite.lib.duties.DrawNamesTreeDisplay;
import mesquite.lib.duties.DrawTreeCoordinator;
import mesquite.lib.duties.FileAssistantT;
import mesquite.lib.duties.TreeDisplayAssistant;
import mesquite.lib.duties.TreeDisplayAssistantDI;
import mesquite.lib.duties.TreeDisplayAssistantMA;
import mesquite.lib.duties.TreeSource;
import mesquite.lib.duties.TreeSourceDefinite;
import mesquite.lib.duties.TreeVectorHolder;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayActive;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayExtraMW;
import mesquite.lib.tree.TreeDisplayHolder;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.InfoBar;
import mesquite.lib.ui.Legend;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MesquiteMenu;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.MesquitePDFFile;
import mesquite.lib.ui.MesquitePanel;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquitePrintJob;
import mesquite.lib.ui.MesquiteScrollbar;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MessagePanel;
import mesquite.lib.ui.Priority0;
import mesquite.trees.BasicTreeWindowCoord.BasicTreeWindowCoord;

/* ======================================================================== */
public class MultiTreeWindowMaker extends FileAssistantT implements TreeDisplayHolder, TreeDisplayActive, TreeVectorHolder {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DrawTreeCoordinator.class, getName() + "  needs a module to coordinate tree drawing.",
				"This is arranged automatically");
		EmployeeNeed e2 = registerEmployeeNeed(TreeSource.class, getName() + "  needs a source of trees.",
				"The source of trees can be selected initially or in the Tree Source submenu");
	}
	/*.................................................................................................................*/
	public DrawTreeCoordinator treeDrawCoordTask;
	public TreeSourceDefinite treeSourceTask;
	MultiTreeWindow multiTreeWindow;
	MesquiteString treeSourceName;
	Taxa taxa;
	MesquiteBoolean namesVisible;
	int numColumns = 3;
	int numRows = 2;
	MesquiteCommand tstC;
	MesquiteBoolean legendBotRight = new MesquiteBoolean(false);
	DataWindowMaker previousMatrixWindowMaker = null;
	MesquiteModule sepTreeWindowMaker = null;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a Multi-tree window?");
		if (taxa != null)
			taxa.addListener(this);

		treeDrawCoordTask= (DrawTreeCoordinator)hireEmployee(DrawTreeCoordinator.class, null);
		if (treeDrawCoordTask == null)
			return sorry(getName() + " couldn't start because no tree draw coordinating module obtained.");
		makeMenu("Multi-Tree");
		int numberOfTrees;
		treeSourceTask = (TreeSourceDefinite)hireNamedEmployee(TreeSourceDefinite.class, "$ #DefiniteTreeSource #StoredTrees");
	
		if (treeSourceTask == null) {
			return sorry(getName() + " couldn't start because no source of trees obtained.");
		} else {
			numberOfTrees = treeSourceTask.getNumberOfTrees(taxa);
		}
		addMenuItem( "Number of Columns...", makeCommand("setNumColumns",  this));
		addMenuItem( "Number of Rows...", makeCommand("setNumRows",  this));
		addCheckMenuItem( null,"Legend on Bottom Right", makeCommand("toggleLegendBotRight",  this), legendBotRight);

		namesVisible = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Show Names", MesquiteModule.makeCommand("setNamesVisible",  this), namesVisible);
		if (!MesquiteThread.isScripting()) {
			if (taxa==null)
				return sorry(getName() + " couldn't start because no block of taxa found.");
			multiTreeWindow= new MultiTreeWindow( this, treeSourceTask, treeDrawCoordTask);
			setModuleWindow(multiTreeWindow);
			multiTreeWindow.setVisible(true);
			resetContainingMenuBar();
			resetAllWindowsMenus();
		}
		return true;
	}
	/*............................................................................. */
	/** Finds a menu of given name from employers */
	public MesquiteMenuSpec findMenu(String menuName) {
		if (treeDrawCoordTask != null){
			MesquiteMenuSpec tdcM = treeDrawCoordTask.findMenu(menuName);
			if (tdcM != null)
				return tdcM;
		}
		return super.findMenu(menuName);
	}

	public boolean isPrerelease(){
		return false;
	}

	/** Returns true if other modules can control the orientation */
	public boolean allowsReorientation(){
		return true;
	}

	/*.................................................................................................................*/
	/**Returns tree vector.*/
	public TreeVector getCurrentTreeVector(Taxa taxa){
		if (treeSourceTask instanceof TreeVectorHolder){
			return ((TreeVectorHolder)treeSourceTask).getCurrentTreeVector(taxa);
		}
		return null;
	}


	public void employeeQuit(MesquiteModule m){
		if (m == treeDrawCoordTask)
			iQuit();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == taxa) {
			iQuit();
		}
	}
	/*.................................................................................................................*/
	/** Query module as to whether conditions are such that it will have to quit soon -- e.g. if its taxa block has been doomed.  The tree window, data window, 
	etc. override this to return true if their object is doomed. This is useful in case MesquiteListener disposing method is not called for an employer before one of its
	employees discovers that it needs to quit.  If the employer is going to quit anyway,there is no use to use auto rehire for the quit employee.*/
	public boolean quittingConditions(){
		return (taxa.isDoomed());
	}
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee == treeSourceTask || source == treeSourceTask){
			if ((multiTreeWindow!=null) ) 
				multiTreeWindow.renew(false, false);
		}
		else if (employee!=treeDrawCoordTask){
			if ((multiTreeWindow!=null) ) 
				multiTreeWindow.renew(true, false);
			else if ((multiTreeWindow!=null)  && Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED) {
				multiTreeWindow.contentsChanged();
				multiTreeWindow.renew(true, false);
			}
		}
		else if (source instanceof DrawNamesTreeDisplay){
			multiTreeWindow.contentsChanged();
			multiTreeWindow.renew(true, false);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (multiTreeWindow ==null)
			return null;
		Snapshot fromWindow = multiTreeWindow.getSnapshot(file);
		Snapshot temp = new Snapshot();

		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa));
		temp.addLine("setNumColumns " + multiTreeWindow.getNumColumns());
		temp.addLine("setNumRows " + multiTreeWindow.getNumRows());
		temp.addLine("getTreeSource",treeSourceTask);
		temp.addLine("makeWindow");
		temp.addLine("toggleLegendBotRight " + legendBotRight.toOffOnString());
		temp.addLine("setNamesVisible " + namesVisible.toOffOnString());
		temp.addLine("getWindow");
		temp.addLine("tell It");
		temp.incorporate(fromWindow, true);
		temp.addLine("endTell");
		temp.addLine("getTreeDrawCoordinator", treeDrawCoordTask);
		temp.addLine("showWindow");
		return temp;
	}
	/*.................................................................................................................*/
	public boolean getLegendBotRight() {
		return legendBotRight.getValue();
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the taxa block", "[block reference, number, or name]", commandName, "setTaxa")){
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				if (t != taxa)
					taxa.removeListener(this);
				
				taxa = t;
				taxa.addListener(this);
				return taxa;
			}
		} 

		else if (checker.compare(this.getClass(), "Toggles whether the legends are shown on the bottom right or top left", null, commandName, "toggleLegendBotRight")) {
			boolean current = legendBotRight.getValue();
			pos.setValue(0);
			legendBotRight.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (current != legendBotRight.getValue())
				multiTreeWindow.setLegendPosition(legendBotRight.getValue());

		}
		else if (checker.compare(this.getClass(), "Sets whether the taxon names are visible", "[on or off]", commandName, "setNamesVisible")) {
			namesVisible.toggleValue(parser.getFirstToken(arguments));
			if (multiTreeWindow!=null)
				multiTreeWindow.sizeDisplays(false);
		}

		else if (checker.compare(this.getClass(), "Sets the number of columns", "[number of columns]", commandName, "setNumColumns")) {
			int newColumns = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newColumns))
				newColumns= MesquiteInteger.queryInteger(containerOfModule(), "Set number of columns", "Columns:", numColumns);
			if (newColumns>0 && newColumns<16) {
				if (multiTreeWindow!=null)
					if (newColumns!=multiTreeWindow.numColumns)
						multiTreeWindow.setNumColumns(newColumns);

				numColumns = newColumns;
			}

		}
		else if (checker.compare(this.getClass(), "Sets the number of rows", "[number of rows]", commandName, "setNumRows")) {
			int newRows =MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newRows))
				newRows= MesquiteInteger.queryInteger(containerOfModule(), "Set number of rows", "Rows:", numRows);
			if (newRows>0 && newRows<16){
				if (multiTreeWindow!=null)
					if (newRows!=multiTreeWindow.numRows)
						multiTreeWindow.setNumRows(newRows);
				numRows = newRows;
			}

		}
		else if (checker.compare(this.getClass(), "Makes but doesn't show the window", null, commandName, "makeWindow")) {
			if (getModuleWindow()==null) {
				multiTreeWindow= new MultiTreeWindow( this, treeSourceTask, treeDrawCoordTask);
				setModuleWindow(multiTreeWindow);
				resetContainingMenuBar();
				resetAllWindowsMenus();
			}
			return multiTreeWindow;
		}
		else if (checker.compare(this.getClass(), "Shows the multi tree window", null, commandName, "showWindow")) {
			if (multiTreeWindow!=null)
				multiTreeWindow.setVisible(true);
			return multiTreeWindow;
		}
		else if (checker.compare(this.getClass(), "To warn user that this command is no longer viable.", null, commandName, "setTreeSource")) {
			MesquiteMessage.discreetNotifyUser("The file was saved with an older version of MultiTreeWindow.  For this reason, the tree source specified in the file could not be read.")  ;
		}    	
		else if (checker.compare(this.getClass(), "Returns treeSourceTask", null, commandName, "getTreeSource")) {
			return treeSourceTask;
		} else if (checker.compare(this.getClass(), "Returns the tree draw coordinating module", null, commandName, "getTreeDrawCoordinator")) {
			return treeDrawCoordTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);

		return null;
	}
	
	/* ................................................................................................................. */
	/** passes which object changed (from MesquiteListener interface) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof Taxa && (Taxa) obj == taxa) {
			multiTreeWindow.renew(Notification.appearsCosmeticOrSelection(notification), false);
		}
		super.changed(caller, obj, notification);
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Multi Tree Window";
	}
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Displays a special tree window with many trees simultaneously." ;
	}

	public boolean mouseDownInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		int branchFound = findBranch(treeDisplay, x, y);
		int taxonFound = findTaxon(treeDisplay, x, y);

		if (MesquiteEvent.rightClick(modifiers)){
			showTreePopup(x, y, treeDisplay, branchFound); 
			return true;
		}
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtraMW) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					if (taxonFound >=0)
						if (tce.cursorTouchTaxon(treeDisplay.getTree(), taxonFound, g, modifiers, null))
							return true;
					if (branchFound >=0)
						if (tce.cursorTouchBranch(treeDisplay.getTree(), branchFound, g, modifiers, null))
							return true;
					if (tce.cursorTouchField(treeDisplay.getTree(), g, x, y, modifiers, 0))
						return true;
				}
			}
		}
		return true;
	}
	/*...........................................*/
	void showTreePopup(int x, int y, TreeDisplay treeDisplay, int branchFound){
		MesquitePopup popup = new MesquitePopup(treeDisplay);
		popup.removeAll();
		notifyExtrasOfRightClickPopup(popup, treeDisplay, branchFound);
		if (popup.getItemCount()>0)
			popup.showPopup(x, y);
	}
	/* ................................................................................................ */
	void notifyExtrasOfRightClickPopup(MesquitePopup popup, TreeDisplay treeDisplay, int branchFound) {
		int numItemsTotal = popup.getItemCount();
		MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				int numItems = popup.getItemCount();
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtraMW) { 
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					if (tce instanceof Priority0)
						tce.addToRightClickPopup(popup, tree, branchFound);
				}
				if (popup.getItemCount()>numItems)
					popup.insert(new MenuItem("-"), numItems);
			}
			e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				int numItems = popup.getItemCount();
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtraMW) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
					if (!(tce instanceof Priority0))
						tce.addToRightClickPopup(popup, tree, branchFound);
				}
				if (popup.getItemCount()>numItems)
					popup.insert(new MenuItem("-"), numItems);
			}
		}
	}
	int oldTaxonFound = -1;
	/*_________________________________________________*/
	public boolean mouseMoveInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		int branchFound = findBranch(treeDisplay, x, y);
		int taxonFound = findTaxon(treeDisplay, x, y);

		if (MesquiteEvent.rightClick(modifiers)){
			showTreePopup(x, y, treeDisplay, branchFound); 
			return true;
		}
		if (treeDisplay.getExtras() != null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtraMW) {
					TreeDisplayExtra tce = (TreeDisplayExtra) obj;
				if (taxonFound >=0) {
						tce.cursorEnterTaxon(treeDisplay.getTree(), taxonFound, g);
						oldTaxonFound = taxonFound;
					}
					else if (taxonFound <0 && oldTaxonFound >=0) {
						tce.cursorExitTaxon(treeDisplay.getTree(), taxonFound, g);
						oldTaxonFound = -1;
					}
					else 
						tce.cursorMove(treeDisplay.getTree(), x, y, g, 0, null);
			//	public void cursorMove(Tree tree, int x, int y, Graphics g, int modifiers, MesquiteTool tool){
				}
			}
		}
		MTWExtra extra = findExtra(treeDisplay);
		int t = findTaxon(treeDisplay, x, y);
		if (t == extra.highlightedTaxon)
			;
		else if (extra.highlightedTaxon>=0){
			extra.highlightedTaxon = -1;
			treeDisplay.repaint();
			return false;
		}
		else if (t>=0) {
			extra.highlightedTaxon = t;
			treeDrawCoordTask.getNamesTask().fillTaxon(treeDisplay.getTreeDrawing(), g, t);
			return false;
		}
		int branch = findBranch(treeDisplay, x, y);
		if (branch == extra.highlightedBranch)
			;
		else if (extra.highlightedBranch>=0){
			extra.highlightedBranch = -1;
			treeDisplay.repaint();
			return false;
		}
		else if (branch>0) {
			extra.highlightedBranch = branch;
			Shape clip = g.getClip();
			g.setClip(null);
			treeDisplay.getTreeDrawing().highlightBranch(treeDisplay.getTree(), branch, g);
			g.setClip(clip);
			//treeDrawCoordTask.getNamesTask().fillTaxon(treeDisplay.getTreeDrawing(), g, t);
			return false;
		}

		return false;
	}
	/*_________________________________________________*/
	public boolean mouseUpInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		return false;
	}

	/*_________________________________________________*/

	public boolean mouseDragInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
		return false;
	}
	/*_________________________________________________*/
	MTWExtra findExtra(TreeDisplay treeDisplay){

		for (int i = 0; i<multiTreeWindow.extras.size(); i++){
			MTWExtra extra = (MTWExtra)multiTreeWindow.extras.elementAt(i);
			if (extra.getTreeDisplay() == treeDisplay)
				return extra;
		}
		return null;
	}
	/*_________________________________________________*/
	public int findBranch(TreeDisplay treeDisplay, int x, int y) {
		if (isDoomed())
			return -2;
		if (treeDisplay == null || treeDrawCoordTask == null || treeDrawCoordTask.getNamesTask() == null || treeDisplay.getTreeDrawing()==null)
			return -1;
		Tree tree = treeDisplay.getTree();
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		MesquiteDouble d = new MesquiteDouble();
		int branchFound =treeDisplay.getTreeDrawing().findBranch(tree,  drawnRoot, x, y, d);
		if (tree.nodeExists(branchFound))
			return branchFound;
		//in case it's in a terminal that is actually within a collapsed clade, in which case treat as if at that ancestor
		int taxonFound = findTaxon(treeDisplay, x, y);
		if (taxonFound>= 0) {
			branchFound = tree.nodeOfTaxonNumber(taxonFound);
			if (branchFound>0) {
				if (tree.withinCollapsedClade(branchFound))
					return tree.deepestCollapsedAncestor(branchFound);
				else
					return branchFound;
			}
		}
		return -3;
	}
	/*_________________________________________________*/
	private int findTaxon(TreeDisplay treeDisplay, int x, int y) {
		if (isDoomed())
			return -2;
		if (treeDisplay == null || treeDrawCoordTask == null || treeDrawCoordTask.getNamesTask() == null || treeDisplay.getTreeDrawing()==null)
			return -1;
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot(); 
		Tree tree = treeDisplay.getTree();
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		return treeDrawCoordTask.getNamesTask().findTaxon(treeDisplay.getTreeDrawing(), tree, drawnRoot, x, y);
	}

}

/* ======================================================================== */
class MultiTreeWindow extends MesquiteWindow implements KeyListener, Commandable  {
	public TreeDisplay[] treeDisplays;
	public DrawTreeCoordinator treeDrawCoordTask;
	TreeSourceDefinite treeSourceTask;
	MultiTreeWindowMaker MTWmodule;
	Taxa taxa;
	MTWScroll treeScroll;
	public int numColumns = 3;
	public int numRows = 2;
	int totalWidth;
	int totalHeight;
	int firstTree=0;
	int maxDisplays = 100;
	MessagePanel messagePanel;
	MultiTreeScrollPanel containingPanel;
	MesquiteTimer timer;
	TreeVector trees;
	Vector extras = new Vector();

	public MultiTreeWindow (MultiTreeWindowMaker ownerModule, TreeSourceDefinite treeSourceTask,   DrawTreeCoordinator treeDrawCoordTask){
		super(ownerModule, true); //infobar
		setWindowSize(500,400);
		MTWmodule=ownerModule;
		this.treeDrawCoordTask = treeDrawCoordTask;
		taxa = ownerModule.taxa;
		if (taxa==null) {
			taxa = ownerModule.getProject().chooseTaxa(this, "For which block of taxa do you want to show a Multi-tree window?");
		}
		trees = new TreeVector(taxa);
		numColumns = MTWmodule.numColumns;
		numRows = MTWmodule.numRows;
		setBackground(Color.white);
		addKeyListener(this,this);


		messagePanel=new MessagePanel(getColorScheme());
		addToWindow(messagePanel);
		messagePanel.setVisible(true);
		MesquiteMenuSpec aux = ownerModule.addAuxiliaryMenu("Analysis:Trees");
		ownerModule.addModuleMenuItems(aux, MesquiteModule.makeCommand("newAssistant",  this), TreeDisplayAssistantMA.class);
		//		public MTWScroll (MultiTreeWindow w, int value, int visible, int min, int max){
		//	treeScroll = new MTWScroll(this, 0, 2, 0, treeSourceTask.getNumberOfTrees(taxa)/numColumns + 1); //-1
		treeScroll = new MTWScroll(this, 0,numRows, 0, treeSourceTask.getNumberOfTrees(taxa)/numColumns + 1); //-1
		addToWindow(treeScroll);
		treeDisplays =treeDrawCoordTask.createTreeDisplays(maxDisplays,taxa, this);
		setTreeSource(treeSourceTask);

		setIcon(MesquiteModule.getRootImageDirectoryPath() + "windowIcons/trees.gif");

		containingPanel = new MultiTreeScrollPanel(this);
		addToWindow(containingPanel);
		for (int itree = 0; itree<maxDisplays; itree++) {
			containingPanel.add(treeDisplays[itree]);
			treeDisplays[itree].tightScaleBar = true;
			MTWExtra extra = new MTWExtra(ownerModule, treeDisplays[itree], itree);
			extras.addElement(extra);
			treeDisplays[itree].addExtra(extra);
		}

		/*
		for (int itree = 0; itree<maxDisplays; itree++) {
			addToWindow(treeDisplays[itree]);
		}
		 */

		treeScroll.setVisible(true);
		sizeDisplays(false);
		addAssistantsDI(ownerModule);
		setShowExplanation(true);
		//setShowAnnotation(true);
		setAnnotation("" ,"");
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
				for (int i=0; i<maxDisplays; i++){
					TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplays[i]);
					if (tce!=null) 
						treeDisplays[i].addExtra(tce);
				}
			}
		}
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Multi-Tree Window"); 
	}
	/*.................................................................................................................*/
	public void printWindow(MesquitePrintJob pjob) {
		if (pjob != null) {
			int mode;
			if (infoBar==null)
				mode =InfoBar.GRAPHICS;
			else mode = infoBar.getMode();
			if (mode==InfoBar.GRAPHICS) //graphical mode
				pjob.printComponent(containingPanel, null, currentFont);
			else 
				super.printWindow(pjob);
		}
	}
	/*.................................................................................................................*/
	/**
	 * @author Peter Midford
	 */
	public void windowToPDF(MesquitePDFFile pdfFile, int fitToPage) {
		if (pdfFile != null) {
			int mode;
			if (infoBar==null)
				mode =InfoBar.GRAPHICS;
			else mode = infoBar.getMode();
			if (mode==InfoBar.GRAPHICS) { //graphical mode
				Graphics g = pdfFile.getPDFGraphicsForComponent(containingPanel,null);
				for (int itree=0; itree<(numColumns*numRows)&& itree<treeDisplays.length; itree++) {
					int xLoc = (int)treeDisplays[itree].getLocation().getX();
					int yLoc = (int)treeDisplays[itree].getLocation().getY();
					g.translate(xLoc,yLoc);
					treeDisplays[itree].print(g);
					g.translate(-xLoc,-yLoc);
				}
				pdfFile.endDocument();
			}			
			else 
				super.windowToPDF(pdfFile, fitToPage);
		}
	}
	/**
	 * @author Peter Midford
	 */
	public String getPrintToPDFMenuItemName() {
		return "Save Multi Tree Window as PDF...";
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		for (int i = 0; i<ownerModule.getNumberOfEmployees(); i++) {
			Object e=ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof TreeDisplayAssistantMA) {
				temp.addLine("newAssistant " , ((MesquiteModule)e));
			}
		}
		temp.incorporate(super.getSnapshot(file), false);
		return temp;
	}
	public void setLegendPosition(boolean lowerRight){
		if (treeDisplays==null)
			return;
		for (int p = 0; p<treeDisplays.length; p++) {
			//cycle through all components getting those that are Legends 
			Component[] cc = treeDisplays[p].getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++) {
					if (cc[i] instanceof Legend){
						//use getOffsetX(); for current and 
						//adjustLocation
						Legend legend = (Legend)cc[i];
						Rectangle rect  = legend.getBounds();
						if (lowerRight) {
							legend.setOffsetX(treeDisplays[p].getBounds().width - legend.getWidth()-4);
							legend.setOffsetY(treeDisplays[p].getBounds().height - legend.getHeight() -8);
						}
						else {
							legend.setOffsetX(4);
							legend.setOffsetY(4);
						}
						legend.adjustLocation();
					}
				}
		}
	}
	public void keyTyped(KeyEvent e) {
	}


	public void keyPressed(KeyEvent e) {
	} 
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch( keyCode ) { 
		case KeyEvent.VK_UP:
			keyUpPressed();
			break;
		case KeyEvent.VK_DOWN:
			keyDownPressed();
			break;
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires a tree display assistant (A)", "[name of module]", commandName, "newAssistant")) {
			TreeDisplayAssistantMA tda= (TreeDisplayAssistantMA)ownerModule.hireNamedEmployee(TreeDisplayAssistantMA.class, arguments);
			if (tda!=null){
				treeDrawCoordTask.addAssistantTask(tda);

				for (int itree=0; itree<(maxDisplays); itree++) {
					TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplays[itree]);
					tce.setTree(treeDisplays[itree].getTree());
					treeDisplays[itree].addExtra(tce);
					treeDisplays[itree].accumulateRequestsFromExtras(treeDisplays[itree].getTree());
					treeDisplays[itree].repaint();
				}
				contentsChanged();
				renew(false, false);
				return tda;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/

	public void renew(boolean redrawOnly, boolean resetToZero) {
		if (!redrawOnly && treeScroll!=null && treeSourceTask!=null)
			treeScroll.setMaximum(treeSourceTask.getNumberOfTrees(taxa)/numColumns + 1); //-1);
		if (treeSourceTask!=null) {
			if (ownerModule.getProject().getNumberTaxas()<=1)
				messagePanel.setMessage("Trees from " + treeSourceTask.getNameAndParameters());
			else
				messagePanel.setMessage("Trees for taxa \"" + taxa.getName() + "\" from " + treeSourceTask.getNameAndParameters());
		}
		if (!redrawOnly){
			if (resetToZero)
			setFirstTree(0);
		else
			setFirstTree(firstTree);
		}
		for (int itree=0; itree<(numColumns*numRows)&& itree<treeDisplays.length; itree++) {
			treeDisplays[itree].redoCalculations(44513);
			treeDisplays[itree].repaint();
		}
	}
	public void redoExtras(){
	}

	/*.................................................................................................................*/

	public void setTreeSource(TreeSourceDefinite tsTask) {
		treeSourceTask = tsTask;
		tsTask.initialize(taxa);
		treeScroll.setMaximum(treeSourceTask.getNumberOfTrees(taxa)/numColumns + 1); //-1);
		if (treeSourceTask!=null) {
			if (ownerModule.getProject().getNumberTaxas()<=1)
				messagePanel.setMessage("Trees from " + treeSourceTask.getNameAndParameters());
			else
				messagePanel.setMessage("Trees for taxa \"" + taxa.getName() + "\" from " + treeSourceTask.getNameAndParameters());
		}
		setFirstTree(0);
	}
	public void setWindowSize(int width, int height){
		super.setWindowSize(width,height);
		sizeDisplays(false);
	}
	/*.................................................................................................................*/
	public synchronized void sizeDisplays(boolean hide){
		if (treeScroll == null || messagePanel == null || containingPanel == null)
			return;
		totalWidth = getWidth()-16;
		totalHeight = getHeight() - 16;
		treeScroll.setBounds(totalWidth, 0, 16, totalHeight);
		containingPanel.setBounds(0,0,totalWidth, totalHeight);
		int maxLow = 0;
		for (int itree=0; itree<(numColumns*numRows)&& itree<treeDisplays.length; itree++) {
			if (treeDisplays[itree] !=null){
				treeDisplays[itree].setTipsMargin(0);
				treeDisplays[itree].setTaxonNameBuffer(4);

				treeDisplays[itree].setFrame(true);
				treeDisplays[itree].suppressNames = !MTWmodule.namesVisible.getValue();
		
				int leftEdge = ((itree) % numColumns)*totalWidth/numColumns;
				treeDisplays[itree].setFieldSize(totalWidth/numColumns,totalHeight/numRows);
				treeDisplays[itree].setSize(totalWidth/numColumns,totalHeight/numRows);
				treeDisplays[itree].setLocation(leftEdge, (itree / numColumns)*totalHeight/numRows);
				int yLoc = totalHeight/numRows + (itree / numColumns)*totalHeight/numRows;

				
				if (yLoc>maxLow)
					maxLow = yLoc;
				
				if (hide) {
					treeDisplays[itree].setVisible(false);
				}
				else
					treeDisplays[itree].repaint();
				if (treeDisplays[itree].getTreeDrawing()!=null)
					treeDisplays[itree].getTreeDrawing().recalculatePositions(treeDisplays[itree].getTree()); //to force node locs recalc
				resetDisplay(treeDisplays[itree]);
				treeDisplays[itree].repaint(true);
			}
		}
		messagePanel.setSize(totalWidth, 16);
		messagePanel.setLocation(0, totalHeight);
		messagePanel.repaint();
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
	public void setFirstTree(int treeNum){
		sizeDisplays(false);
		firstTree = treeNum;
		trees.removeAllElements(false);
		for (int itree=0; itree<(maxDisplays); itree++) { //Debugg.println: this should not do max, but current number. Redo this if # displays changes
			Tree sourceTree=null;
			if (itree+treeNum <treeSourceTask.getNumberOfTrees(taxa))
				sourceTree = treeSourceTask.getTree(taxa, itree+treeNum);
			if (sourceTree!=null) {
				if (treeDisplays[itree].getTree()!=null)
					treeDisplays[itree].getTree().dispose();
				Tree tree = sourceTree.cloneTree();
				trees.addElement(tree, false); //for notification of taxa changes
				treeDisplays[itree].setTree(tree);
				treeDisplays[itree].setNotice(Integer.toString(itree+treeNum + 1) + ": " + tree.getName()); // for debugging purposes???
				treeDisplays[itree].suppressDrawing(false);
				if (itree<numColumns*numRows&& itree<treeDisplays.length) {
					treeDisplays[itree].setVisible(true);
					treeDisplays[itree].repaint();
				}
				treeDisplays[itree].setTreeAllExtras(tree);
			}
			else {
				treeDisplays[itree].setVisible(false);
			}
		}

		sizeDisplays(false);
	}
	/*.................................................................................................................*/
	public void setNumColumns(int newNum){
		if (newNum>0) {
			// need to reset all of tree display extras!!!!!
			for (int itree = 0; itree<numColumns*numRows&& itree<treeDisplays.length; itree++) {
				treeDisplays[itree].setVisible(false);
			}
			numColumns = newNum;
			for (int itree = 0; itree<numColumns*numRows&& itree<treeDisplays.length; itree++) {
				treeDisplays[itree].setVisible(true);
			}

			if (treeScroll!=null && treeSourceTask!=null) {
				treeScroll.setMaximum(treeSourceTask.getNumberOfTrees(taxa)/numColumns + 1); //-1);
			}
			setFirstTree(firstTree);
			//sizeDisplays(false);
			contentsChanged();
		}
	}
	/*.................................................................................................................*/
	public int getNumColumns(){
		return numColumns;
	}
	/*.................................................................................................................*/
	public void setNumRows(int newNum){
		if (newNum>0) {
			// need to reset all of tree display extras!!!!!
			for (int itree = 0; itree<numColumns*numRows && itree<treeDisplays.length; itree++) {
				treeDisplays[itree].setVisible(false);
			}
			numRows = newNum;
			for (int itree = 0; itree<numColumns*numRows && itree<treeDisplays.length; itree++) {
				treeDisplays[itree].setVisible(true);
			}

			setFirstTree(firstTree);
			//sizeDisplays(false);
			//for (int itree = 0; itree<numColumns*numRows; itree++)
			//	addToWindow(treeDisplays[itree]);
			contentsChanged();
		}
	}
	/*.................................................................................................................*/
	public int getNumRows(){
		return numRows;
	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		int amount = e.getScrollAmount() * 2;
		boolean blockScroll = e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL;
		boolean upleft=false;
		upleft = e.getWheelRotation()<0;
		if (blockScroll)
			amount = treeScroll.getBlockIncrement();
		else
			amount = treeScroll.getUnitIncrement() * amount;
		if (upleft) {
			amount = -amount;
			if (treeScroll.getValue() == 0)
				amount = 0;
		}
		if (amount != 0) {
			treeScroll.setValue(treeScroll.getValue() + amount); 
			treeScroll.updateView();
		}

	}

	public void keyUpPressed() {
		treeScroll.setValue(treeScroll.getValue() - numRows); 
		treeScroll.updateView();

	}

	public void keyDownPressed() {
		treeScroll.setValue(treeScroll.getValue() +numRows); 
		treeScroll.updateView();
	}

	/*.................................................................................................................*/
	public void windowResized() {
		super.windowResized();
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (treeDisplays==null)
			;//ownerModule.alert("Oh no, tree displays are null");
		else  {
			sizeDisplays(false);
			setLegendPosition(MTWmodule.getLegendBotRight());
		}

		MesquiteWindow.uncheckDoomed(this);
	}
	public void dispose(){
		for (int itree=0; itree<treeDisplays.length; itree++) {
			if (treeDisplays[itree]!=null){
				if (treeDisplays[itree].getTree()!=null)
					treeDisplays[itree].getTree().dispose();
				treeDisplays[itree].dispose();
			}
		}
		super.dispose();
	}
}


class MultiTreeScrollPanel extends MQPanel implements MouseWheelListener { 
	MultiTreeWindow window;
	//	MTWScroll treeScroll;
	//	Panel port;
	//	TreeDisplay treeDisplay;

	public MultiTreeScrollPanel(MultiTreeWindow window) {
		super();
		addMouseWheelListener(this);
		this.window = window;
		setLayout(null);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		window.mouseWheelMoved(e);
	}

}

/* ======================================================================== */
class MTWScroll extends MesquiteScrollbar {
	MultiTreeWindow w;
	int visible;
	public MTWScroll (MultiTreeWindow w, int value, int visible, int min, int max){
		super(Scrollbar.VERTICAL, value, visible, min, max);
		this.visible = visible;
		this.w=w;
	}

	public void scrollTouched(){
		int currentValue = getValue();
		w.setFirstTree(currentValue*w.numColumns);
	}
	public void updateView(){
		int currentValue = getValue();
		w.setFirstTree(currentValue*w.numColumns);
	}
	public boolean processDuringAdjustment() {
		return true;
	}
	public void print(Graphics g){
	}
}
/* ======================================================================== */
class MTWExtra extends TreeDisplayExtra implements Commandable, TreeDisplayExtraMW {
	MultiTreeWindowMaker module;
	int whichTreeDisplay = -1;
	int highlightedBranch = 0;
	int highlightedTaxon = -1;

	public MTWExtra(MultiTreeWindowMaker module, TreeDisplay treeDisplay, int whichTreeDisplay){
		super(module, treeDisplay);
		this.module = module;
		this.whichTreeDisplay = whichTreeDisplay;
	}

	public boolean cursorTouchField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){
		Taxa taxa = tree.getTaxa();
		taxa.deselectAll();
	//	setSelectTipsInClade((MesquiteTree)tree, tree.getRoot(), taxa, false);
		taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		return false;
	}
	public boolean cursorTouchTaxon(Tree tree, int M, Graphics g, int modifiers, MesquiteTool tool){ 
		Taxa taxa = tree.getTaxa();
		if (M >=0 && M<taxa.getNumTaxa())	
			taxa.setSelected(M, !taxa.getSelected(M));
		taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		return true;
	}

	
	//NOTE: THIS could use ShowLinkedMatrixMachine. The latter was derived from this code
	
	/**Add any desired menu items to the right click popup*/
	public void addToRightClickPopup(MesquitePopup popup, MesquiteTree tree, int branch){
		if (tree.nodeExists(branch)){
			if (tree.nodeIsInternal(branch)){
				popup.addItem("Select All Taxa in Clade", new MesquiteCommand("selectTerminals", this), Integer.toString(branch));
				popup.addItem("Deselect All Taxa in Clade", new MesquiteCommand("deselectTerminals", this), Integer.toString(branch));
				popup.addItem("-", (MesquiteCommand)null, null);
				popup.addItem("Select All Taxa Outside of Clade", new MesquiteCommand("selectOutside", this), Integer.toString(branch));
				popup.addItem("Deselect All Taxa Outside of Clade", new MesquiteCommand("deselectOutside", this), Integer.toString(branch));
			}
			else {
				Taxa taxa = tree.getTaxa();
				int taxon = tree.taxonNumberOfNode(branch);
				if (taxa.getSelected(taxon))
					popup.addItem("Deselect Taxon", new MesquiteCommand("deselectTerminals", this), Integer.toString(branch));
				else
					popup.addItem("Select Taxon", new MesquiteCommand("selectTerminals", this), Integer.toString(branch));
				CharacterData data = ((MesquiteTree)treeDisplay.getTree()).findLinkedMatrix(module.getProject());
				if (data != null && data instanceof MolecularData) {
						popup.addItem("Show Linked Sequence", module, new MesquiteCommand("showLinkedSequence", this), Integer.toString(taxon));
				}			
				}
			return;
		}
		CharacterData data = ((MesquiteTree)treeDisplay.getTree()).findLinkedMatrix(module.getProject());
		MesquiteProject proj = module.getProject();
		if (activeTreeWindow())
			popup.addItem("Show Tree in Tree Window", module, new MesquiteCommand("showInTreeWindow", this), "continue");
		else {
			MesquiteMenu whereWindowSubmenu = new MesquiteMenu("Show Tree in Tree Window");
			MesquiteMenuItem mmi = new MesquiteMenuItem("In Window", module, new MesquiteCommand("showInTreeWindow", this), "in");
			whereWindowSubmenu.add(mmi);
			mmi = new MesquiteMenuItem("In Separate Tile", module, new MesquiteCommand("showInTreeWindow", this), "tile");
			whereWindowSubmenu.add(mmi);
			mmi = new MesquiteMenuItem("Popped Out as Separate Window", module, new MesquiteCommand("showInTreeWindow", this), "poppedOut");
			whereWindowSubmenu.add(mmi);

			popup.add(whereWindowSubmenu);			
		}
		if (data != null) {
			if (activeDataWindow())
				popup.addItem("Show Linked Matrix", module, new MesquiteCommand("showLinkedMatrix", this), "continue");
			else {
				MesquiteMenu whereWindowSubmenu = new MesquiteMenu("Show Linked Matrix");
				MesquiteMenuItem mmi = new MesquiteMenuItem("In Window", module, new MesquiteCommand("showLinkedMatrix", this), "in");
				whereWindowSubmenu.add(mmi);
				mmi = new MesquiteMenuItem("In Separate Tile", module, new MesquiteCommand("showLinkedMatrix", this), "tile");
				whereWindowSubmenu.add(mmi);
				mmi = new MesquiteMenuItem("Popped Out as Separate Window", module, new MesquiteCommand("showLinkedMatrix", this), "poppedOut");
				whereWindowSubmenu.add(mmi);

				popup.add(whereWindowSubmenu);			
			}
		}
	}

	/*-----------------------------------------*/
	public void setSelectTipsInClade(MesquiteTree tree,  int node, Taxa taxa, boolean select) {
		if (tree.nodeIsTerminal(node))
			taxa.setSelected(tree.taxonNumberOfNode(node), select);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			setSelectTipsInClade(tree, d, taxa, select);
	}
	/*-----------------------------------------*/
	public void setSelectTipsOutsideClade(MesquiteTree tree,  int node, int targetNode, Taxa taxa, boolean select) {
		if (tree.nodeIsTerminal(node))
			taxa.setSelected(tree.taxonNumberOfNode(node), select);
		else if (node != targetNode)
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			setSelectTipsOutsideClade(tree, d, targetNode, taxa,  select);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows linked matrix", null, commandName, "showLinkedMatrix")) {
			MesquiteTree myTree = (MesquiteTree)treeDisplay.getTree();
			int where = 0;
			if (arguments == null || arguments.equals("in"))
				where = 0;
			else if (arguments.equalsIgnoreCase("tile"))
				where = 1;
			else if (arguments.equalsIgnoreCase("poppedOut"))
				where = 2;

			CharacterData data = myTree.findLinkedMatrix(module.getProject());
			if (data == null)
				module.logln("Sorry; no matrix linked to the tree has been found");
			else
				showMatrix(data, where);
			return null;
		}
		else if (checker.compare(this.getClass(), "Shows tree in regular tree window", null, commandName, "showInTreeWindow")) {
			int where = 0;
			if (arguments == null || arguments.equals("in"))
				where = 0;
			else if (arguments.equalsIgnoreCase("tile"))
				where = 1;
			else if (arguments.equalsIgnoreCase("poppedOut"))
				where = 2;
			showTreeInWindow(where);
			return null;
		}
		/*Test case for other use problem here is that system doesn't */
		 else if (checker.compare(this.getClass(), "Shows taxon's sequence in character matrix window", "[taxon number]", commandName, "showLinkedSequence")) {
			int taxon = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(taxon)){
				MesquiteTree myTree = (MesquiteTree)treeDisplay.getTree();
				CharacterData data = myTree.findLinkedMatrix(module.getProject());
				if (data != null){
					showMatrix(data, 0);
					data.showRow(taxon, true, false, module.previousMatrixWindowMaker);
				}
			}
			return null;
		}

		else if (checker.compare(this.getClass(), "Selects terminals in clade", null, commandName, "selectTerminals")) {
			int branch = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(branch)){
				MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
				setSelectTipsInClade(tree, branch, tree.getTaxa(), true);
				tree.getTaxa().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Deelects terminals in clade", null, commandName, "deselectTerminals")) {
			int branch = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(branch)){
				MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
				setSelectTipsInClade(tree, branch, tree.getTaxa(), false);
				tree.getTaxa().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Selects terminals outside clade", null, commandName, "selectOutside")) {
			int branch = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(branch)){
				MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
				setSelectTipsOutsideClade(tree, tree.getRoot(), branch, tree.getTaxa(), true);
				tree.getTaxa().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Deelects terminals outside clade", null, commandName, "deselectOutside")) {
			int branch = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(branch)){
				MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
				setSelectTipsOutsideClade(tree, tree.getRoot(), branch, tree.getTaxa(), false);
				tree.getTaxa().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
			return null;
		}
		return null;
	}
	/*---------------------------------------------------------------*/
	boolean activeDataWindow(){
		return module.previousMatrixWindowMaker != null && !module.previousMatrixWindowMaker.isDoomed();
	}
	/*---------------------------------------------------------------*/
	public void showMatrix(CharacterData data, int where) {
		MesquiteModule bdwC = module.findNearestColleagueWithDuty(BasicDataWindowCoord.class);
		MesquiteProject proj = module.getProject();
		int imNext = proj.getMatrixNumber(data);
		if (!activeDataWindow()){
			MesquiteModule mb = (MesquiteModule)bdwC.doCommand("showExtraDataWindow", Integer.toString(imNext));
			MesquiteWindow mw = mb.containerOfModule();
			if (where == 1)
				mw.doCommand("toggleTileOutWindow","true", CommandChecker.defaultChecker);
			else if (where == 2)
				mw.doCommand("togglePopOutWindow","true", CommandChecker.defaultChecker);
			module.previousMatrixWindowMaker = (DataWindowMaker)mb;		

		}
		else {
			CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
			CommandRecord record = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(record);
			MesquiteModule mb = (MesquiteModule)bdwC.doCommand("showExtraDataWindow", Integer.toString(imNext));
			String cloneCommand =  Snapshot.getSnapshotCommands(module.previousMatrixWindowMaker, null, "");
			Puppeteer p = new Puppeteer(module.previousMatrixWindowMaker);
			MesquiteInteger pos = new MesquiteInteger(0);
			MesquiteModule.incrementMenuResetSuppression();	
			Object obj = p.sendCommands(mb, cloneCommand, pos, "", false, null,CommandChecker.defaultChecker);
			MesquiteModule.decrementMenuResetSuppression();	
			MesquiteThread.setCurrentCommandRecord(previous);
			MesquiteWindow oldWindow = module.previousMatrixWindowMaker.containerOfModule();
			oldWindow.doCommand("closeWindow","true", CommandChecker.defaultChecker);
			module.previousMatrixWindowMaker = (DataWindowMaker)mb;		
		}
	}
	/*---------------------------------------------------------------*/
	boolean activeTreeWindow(){
		return module.sepTreeWindowMaker != null && !module.sepTreeWindowMaker.isDoomed();
	}
	/*---------------------------------------------------------------*/
	public void showTreeInWindow(int where) {
		int whichTree = module.multiTreeWindow.firstTree + whichTreeDisplay;
		MesquiteModule btwC = module.findNearestColleagueWithDuty(BasicTreeWindowCoord.class);
		MesquiteProject proj = module.getProject();
		String taxaRef = proj.getTaxaReferenceInternal(treeDisplay.getTree().getTaxa());
		TreeSource tSource = module.treeSourceTask.getTreeSource();
		String cloneCommandTreeSource =  Snapshot.getSnapshotCommands(tSource, null, "");
		if (!activeTreeWindow()){
			MesquiteModule.incrementMenuResetSuppression();	
			CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
			CommandRecord record = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(record);
			MesquiteModule mb = (MesquiteModule)btwC.doCommand("makeTreeWindow", taxaRef);
			Puppeteer p = new Puppeteer(module);
			cloneCommandTreeSource = "setTreeSource #" + tSource.getClassName() + ";\ntell It;\n" + cloneCommandTreeSource;
			cloneCommandTreeSource = cloneCommandTreeSource + "\nendTell;\ngetWindow;\ntell It;\nsetTreeNumber " + (whichTree+1) + ";\nendTell;\n";
			Object obj = p.sendCommands(mb, cloneCommandTreeSource, new MesquiteInteger(0), "", false, null,CommandChecker.defaultChecker);
			MesquiteThread.setCurrentCommandRecord(previous);

			if (mb != null){
				MesquiteWindow mw = mb.containerOfModule();
				if (where == 1)
					mw.doCommand("toggleTileOutWindow","true", CommandChecker.defaultChecker);
				else if (where == 2)
					mw.doCommand("togglePopOutWindow","true", CommandChecker.defaultChecker);
				mw.doCommand("showWindow","true", CommandChecker.defaultChecker);
			}
			module.sepTreeWindowMaker = (MesquiteModule)mb;		
			MesquiteModule.decrementMenuResetSuppression();	

		}
		else {
			CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
			CommandRecord record = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(record);
			cloneCommandTreeSource = "setTreeSource #" + tSource.getClassName() + ";\ntell It;\n" + cloneCommandTreeSource;
			cloneCommandTreeSource = cloneCommandTreeSource + "\nendTell;\ngetWindow;\ntell It;\nsetTreeNumber " + (whichTree+1) + ";\nendTell;\n";
			Puppeteer p = new Puppeteer(module.sepTreeWindowMaker);
			MesquiteModule.incrementMenuResetSuppression();	
			Object obj = p.sendCommands(module.sepTreeWindowMaker, cloneCommandTreeSource, new MesquiteInteger(0), "", false, null,CommandChecker.defaultChecker);
			MesquiteModule.decrementMenuResetSuppression();	
			MesquiteThread.setCurrentCommandRecord(previous);
		}
	}
	public void setTree(Tree tree) {
	}

	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
}


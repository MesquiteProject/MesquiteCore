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

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class MultiTreeWindowMaker extends FileAssistantT {
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

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to show a Multi-tree window?");
		treeDrawCoordTask= (DrawTreeCoordinator)hireEmployee(DrawTreeCoordinator.class, null);
		if (treeDrawCoordTask == null)
			return sorry(getName() + " couldn't start because no tree draw coordinating module obtained.");
		makeMenu("Multi-Tree");
		int numberOfTrees;
		treeSourceTask = (TreeSourceDefinite) hireEmployee(TreeSourceDefinite.class, "Source of Trees (Multi Tree Window)");
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

	public boolean isPrerelease(){
		return false;
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
		if (employee!=treeDrawCoordTask)
			if ((multiTreeWindow!=null) ) 
				multiTreeWindow.renew();
			else if ((multiTreeWindow!=null)  && Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED) {
				multiTreeWindow.contentsChanged();
				multiTreeWindow.renew();
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
				taxa = t;
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
}

/* ======================================================================== */
class MultiTreeWindow extends MesquiteWindow implements Commandable  {
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
	int maxDisplays = 36;
	MessagePanel messagePanel;
	MesquitePanel containingPanel;
	MesquiteTimer timer;
	TreeVector trees;


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

		messagePanel=new MessagePanel(getColorScheme());
		addToWindow(messagePanel);
		messagePanel.setVisible(true);
		MesquiteMenuSpec aux = ownerModule.addAuxiliaryMenu("Analysis:Trees");
		ownerModule.addModuleMenuItems(aux, MesquiteModule.makeCommand("newAssistant",  this), TreeDisplayAssistantMA.class);
		treeScroll = new MTWScroll(this, 0, 2, 0, treeSourceTask.getNumberOfTrees(taxa)/numColumns + 1); //-1
		addToWindow(treeScroll);
		treeDisplays =treeDrawCoordTask.createTreeDisplays(maxDisplays,taxa, this);
		setTreeSource(treeSourceTask);


		containingPanel = new MesquitePanel();
		addToWindow(containingPanel);
		for (int itree = 0; itree<maxDisplays; itree++) {
			containingPanel.add(treeDisplays[itree]);
		}

		/*
		for (int itree = 0; itree<maxDisplays; itree++) {
			addToWindow(treeDisplays[itree]);
		}
		 */

		treeScroll.setVisible(true);
		sizeDisplays(false);
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
		setTitle("Trees"); 
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
				for (int itree=0; itree<(numColumns*numRows); itree++) {
					int xLoc = (int)treeDisplays[itree].getLocation().getX();
					int yLoc = (int)treeDisplays[itree].getLocation().getY();
					g.translate(xLoc,yLoc);
					treeDisplays[itree].print(g);
					g.translate(-xLoc,-yLoc);
				}
				pdfFile.end();
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
					treeDisplays[itree].repaint();
				}
				contentsChanged();
				renew();
				return tda;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/

	public void renew() {
		if (treeScroll!=null && treeSourceTask!=null)
			treeScroll.setMaximum(treeSourceTask.getNumberOfTrees(taxa)/numColumns + 1); //-1);
		if (treeSourceTask!=null) {
			if (ownerModule.getProject().getNumberTaxas()<=1)
				messagePanel.setMessage("Trees from " + treeSourceTask.getNameAndParameters());
			else
				messagePanel.setMessage("Trees for taxa \"" + taxa.getName() + "\" from " + treeSourceTask.getNameAndParameters());
		}
		setFirstTree(0);
		for (int itree=0; itree<(numColumns*numRows); itree++) {
			treeDisplays[itree].repaint();
		}
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

		for (int itree=0; itree<(numColumns*numRows); itree++) {
			if (treeDisplays[itree] !=null){
				treeDisplays[itree].setTipsMargin(0);
				treeDisplays[itree].setTaxonNameBuffer(4);

				treeDisplays[itree].setFrame(true);
				treeDisplays[itree].suppressNames = !MTWmodule.namesVisible.getValue();
				treeDisplays[itree].setFieldSize(totalWidth/numColumns,totalHeight/numRows);
				treeDisplays[itree].setSize(totalWidth/numColumns,totalHeight/numRows);
				treeDisplays[itree].setLocation(((itree) % numColumns)*totalWidth/numColumns, (itree / numColumns)*totalHeight/numRows);
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
		for (int itree=0; itree<(maxDisplays); itree++) {
			Tree sourceTree=null;
			if (itree+treeNum <treeSourceTask.getNumberOfTrees(taxa))
				sourceTree = treeSourceTask.getTree(taxa, itree+treeNum);
			if (sourceTree!=null) {
				if (treeDisplays[itree].getTree()!=null)
					treeDisplays[itree].getTree().dispose();
				Tree tree = sourceTree.cloneTree();
				trees.addElement(tree, false); //for notification of taxa changes
				treeDisplays[itree].setTree(tree);
				treeDisplays[itree].setNotice(Integer.toString(itree+treeNum + 1)); // for debugging purposes???
				treeDisplays[itree].suppressDrawing(false);
				if (itree<numColumns*numRows) {
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
			for (int itree = 0; itree<numColumns*numRows; itree++) {
				treeDisplays[itree].setVisible(false);
			}
			numColumns = newNum;
			for (int itree = 0; itree<numColumns*numRows; itree++) {
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
			for (int itree = 0; itree<numColumns*numRows; itree++) {
				treeDisplays[itree].setVisible(false);
			}
			numRows = newNum;
			for (int itree = 0; itree<numColumns*numRows; itree++) {
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

/* ======================================================================== */
class MTWScroll extends MesquiteScrollbar {
	MultiTreeWindow w;
	public MTWScroll (MultiTreeWindow w, int value, int visible, int min, int max){
		super(Scrollbar.VERTICAL, value, visible, min, max);
		this.w=w;
	}

	public void scrollTouched(){
		int currentValue = getValue();
		w.setFirstTree(currentValue*w.numColumns);
	}
	public boolean processDuringAdjustment() {
		return true;
	}
	public void print(Graphics g){
	}
}



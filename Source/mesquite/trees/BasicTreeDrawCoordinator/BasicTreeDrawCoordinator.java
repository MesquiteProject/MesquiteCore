/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BasicTreeDrawCoordinator;
/*~~  */

import java.util.*;

import mesquite.assoc.lib.*;

import java.awt.*;
import java.io.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

import com.lowagie.text.pdf.PdfGraphics2D;

/** Coordinates the drawing of trees in windows (e.g., used in the Tree Window and other places) */
public class BasicTreeDrawCoordinator extends DrawTreeCoordinator {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DrawTree.class, "A specific Tree Drawer is needed to yield the desired style of tree.",
		"You can choose the style in the Tree Form submenu of the Drawing menu.");
		e.setSuppressListing(true);
		EmployeeNeed e2 = registerEmployeeNeed(DrawNamesTreeDisplay.class, "A Tree drawing shows the names of taxa.",
		"This is activated automatically.");
		e2.setSuppressListing(true);
	}
	private DrawTree treeDrawTask;
	private DrawNamesTreeDisplay terminalNamesTask;
	MesquiteString treeDrawName, bgColorName, brColorName;
	public Color bgColor=Color.white;
	public Color brColor=Color.black;
	boolean suppression = false;
	MesquiteCommand tdC;
	static String defaultDrawer = null;
	MesquiteBoolean showNodeNumbers, labelBranchLengths, showBranchColors;

	MesquiteBoolean centerBrLenLabels = new MesquiteBoolean(true);
	MesquiteBoolean showBrLensUnspecified = new MesquiteBoolean(true);
	MesquiteBoolean showBrLenLabelsOnTerminals = new MesquiteBoolean(true);
	MesquiteInteger numBrLenDecimals = new MesquiteInteger(6);
	public Color brLenColor=Color.blue;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		//addMenuSeparator();
		makeMenu("Display");
		if (defaultDrawer !=null && (condition == null || !(condition instanceof MesquiteBoolean) || ((MesquiteBoolean)condition).getValue() )) {
			treeDrawTask= (DrawTree)hireNamedEmployee(DrawTree.class, defaultDrawer);
			if (treeDrawTask == null)
				treeDrawTask= (DrawTree)hireEmployee(DrawTree.class, null);
		}
		else
			treeDrawTask= (DrawTree)hireEmployee(DrawTree.class, null);
		if (treeDrawTask==null)
			return sorry(getName() + " couldn't start because no tree drawing module was obtained");
		setAutoSaveMacros(true);
		treeDrawName = new MesquiteString(treeDrawTask.getName());
		bgColorName = new MesquiteString("White");
		brColorName = new MesquiteString("Black");
		terminalNamesTask = (DrawNamesTreeDisplay)hireEmployee(DrawNamesTreeDisplay.class, null);
		//TODO: if choice of terminalNamesTask, use setHriingCommand
		tdC = makeCommand("setTreeDrawer",  this);
		treeDrawTask.setHiringCommand(tdC);
		MesquiteSubmenuSpec mmis = addSubmenu(null, "Tree Form", tdC);
		addMenuItem("Set Current Form as Default", makeCommand("setFormToDefault",  this));
		mmis.setList(DrawTree.class);
		mmis.setSelected(treeDrawName);


		mmis = addSubmenu(null, "Background Color", makeCommand("setBackground",  this));
		mmis.setList(ColorDistribution.standardColorNames);
		mmis.setSelected(bgColorName);
		mmis = addSubmenu(null, "Default Branch Color", makeCommand("setBranchColor",  this));
		mmis.setList(ColorDistribution.standardColorNames);
		mmis.setSelected(brColorName);
		showNodeNumbers = new MesquiteBoolean(false);
		labelBranchLengths = new MesquiteBoolean(false);
		showBranchColors = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Show Node Numbers", MesquiteModule.makeCommand("showNodeNumbers",  this), showNodeNumbers);
		addCheckMenuItem(null, "Show Branch Colors", MesquiteModule.makeCommand("showBranchColors",  this), showBranchColors);

		mmis = addSubmenu(null, "Branch Length Labels");
		addCheckMenuItemToSubmenu(null, mmis, "Show Labels", MesquiteModule.makeCommand("labelBranchLengths",  this), labelBranchLengths);
		addCheckMenuItemToSubmenu(null, mmis, "Center Labels", MesquiteModule.makeCommand("centerBrLenLabels",  this), centerBrLenLabels);
		addCheckMenuItemToSubmenu(null, mmis, "Label Terminal Taxa", MesquiteModule.makeCommand("showBrLenLabelsOnTerminals",  this), showBrLenLabelsOnTerminals);
		addItemToSubmenu(null, mmis, "Label Color...", MesquiteModule.makeCommand("chooseBrLenLabelColor",  this));
		addItemToSubmenu(null, mmis, "Number of Decimal Places...", MesquiteModule.makeCommand("setNumBrLenDecimals",  this));
		addCheckMenuItemToSubmenu(null, mmis, "Include Missing Values", MesquiteModule.makeCommand("showBrLensUnspecified",  this), showBrLensUnspecified);
		return true;
	}

	public boolean getShowBrLensUnspecified(){
		return showBrLensUnspecified.getValue();
	}
	public boolean getCenterBrLenLabels(){
		return centerBrLenLabels.getValue();
	}
	public boolean getShowBrLenLabelsOnTerminals() {
		return showBrLenLabelsOnTerminals.getValue();
	}
	public int getNumBrLenDecimals() {
		return numBrLenDecimals.getValue();
	}
	public Color getBrLenColor() {
		return brLenColor;
	}
	public boolean hasPreferredSize(){
		return treeDrawTask.hasPreferredSize();
	}
	public Dimension getPreferredSize(){
		return treeDrawTask.getPreferredSize();
	}
	/*.................................................................................................................*/
	MesquiteModule getTreeWindowMaker() {
		TreeWindowMaker tw = (TreeWindowMaker) findEmployerWithDuty(TreeWindowMaker.class);
		if (tw!=null)
			return tw;
		TWindowMaker tw2 = (TWindowMaker) findEmployerWithDuty(TWindowMaker.class);
		return tw2;
	}

	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			defaultDrawer = prefs[0];
		}
	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "defaultDrawer", defaultDrawer);   
		return buffer.toString();
	}

	public void processSingleXMLPreference (String tag, String content) {
		if ("defaultDrawer".equalsIgnoreCase(tag))
			defaultDrawer = StringUtil.cleanXMLEscapeCharacters(content);
	}


	/*.................................................................................................................*
	public Snapshot getSnapshotForMacro(MesquiteFile file) {
		treeDrawCoordTask= (DrawTreeCoordinator)hireEmployee(DrawTreeCoordinator.class, null);
		if (treeDrawCoordTask== null) {
			sorry(getName() + " couldn't start because no tree draw coordinating module was obtained.");
			return null;
		}
		treeDrawCoordTask.setToLastEmployee(true);
		hireAllEmployees(TreeDisplayAssistantI.class);
		hireAllEmployees(TreeDisplayAssistantDI.class);
		Enumeration enumeration = getEmployeeVector().elements();
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			if (obj instanceof TreeDisplayAssistantDI) {
				TreeDisplayAssistantDI init = (TreeDisplayAssistantDI) obj;
				treeDrawCoordTask.requestGuestMenuPlacement(init);
			}
		}
		resetContainingMenuBar();
		return treeDrawCoordTask;
	}
	/*.................................................................................................................*/
	/** return whether or not this module should have snapshot saved when saving a macro given the current snapshot mode.*/
	public boolean satisfiesSnapshotMode(){
		return (MesquiteTrunk.snapshotMode == Snapshot.SNAPALL || MesquiteTrunk.snapshotMode == Snapshot.SNAPDISPLAYONLY);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("suppress");

		temp.addLine("setTreeDrawer " , treeDrawTask);
		if (bgColor !=null) {
			String bName = ColorDistribution.getStandardColorName(bgColor);
			if (bName!=null)
				temp.addLine("setBackground " + StringUtil.tokenize(bName));//quote
		}
		if (brColor !=null) {
			String bName = ColorDistribution.getStandardColorName(brColor);
			if (bName!=null)
				temp.addLine("setBranchColor " + StringUtil.tokenize(bName));//quote
		}
		temp.addLine("showNodeNumbers " + showNodeNumbers.toOffOnString()); 
		temp.addLine("showBranchColors " + showBranchColors.toOffOnString()); 
		temp.addLine("labelBranchLengths " + labelBranchLengths.toOffOnString()); 
		temp.addLine("centerBrLenLabels " + centerBrLenLabels.toOffOnString()); 
		temp.addLine("showBrLensUnspecified " + showBrLensUnspecified.toOffOnString());
		temp.addLine("showBrLenLabelsOnTerminals " + showBrLenLabelsOnTerminals.toOffOnString()); 
		temp.addLine("setBrLenLabelColor " + ColorDistribution.getColorStringForSnapshot(brLenColor));
		temp.addLine("setNumBrLenDecimals " + numBrLenDecimals.getValue());
		temp.addLine("desuppress");
		return temp;
	}
	/*.................................................................................................................*/
	/** return the value of items to be snapshotted when saving a macro.*/
	public int getMacroSnapshotMode(){
		return Snapshot.SNAPDISPLAYONLY;
	}
	/*.................................................................................................................*/
	/** return the module responsible for snapshotting when saving a macro.*/
	public MesquiteModule getMacroSnapshotModule(){
		return getTreeWindowMaker();
	}
	/*.................................................................................................................*/
	/** return the command string to get the module responsible for snapshotting when saving a macro.*/
	public String getMacroSnapshotModuleCommand(){
		return "getTreeWindowMaker";
	}

	public DrawNamesTreeDisplay getNamesTask(){
		return terminalNamesTask;
	}
	/*.................................................................................................................*/
	public void setBranchColor(Color c) {
		brColor = c;
	}
	/*.................................................................................................................*/
	public TreeDisplay createOneTreeDisplay(Taxa taxa, MesquiteWindow window) {
		treeDisplay = new BasicTreeDisplay(this, taxa);
		treeDisplay.setTreeDrawing(treeDrawTask.createTreeDrawing(treeDisplay, taxa.getNumTaxa()));
		treeDisplay.setDrawTaxonNames(terminalNamesTask);
		treeDisplay.suppressDrawing(suppression);
		return treeDisplay;
	}
	/*.................................................................................................................*/
	public TreeDisplay[] createTreeDisplays(int numDisplays, Taxa taxa, MesquiteWindow window) {
		int numTaxa = 100;
		if (taxa != null)
			numTaxa = taxa.getNumTaxa();
		treeDisplays = new BasicTreeDisplay[numDisplays];
		this.numDisplays=numDisplays;
		for (int i=0; i<numDisplays; i++) {
			treeDisplays[i] = new BasicTreeDisplay(this, taxa);
			treeDisplays[i].setDrawTaxonNames(terminalNamesTask);
			treeDisplays[i].setTreeDrawing(treeDrawTask.createTreeDrawing(treeDisplays[i], numTaxa));
			treeDisplays[i].suppressDrawing(suppression);
		}
		return treeDisplays;
	}
	/*.................................................................................................................*/
	public TreeDisplay[] createTreeDisplays(int numDisplays, Taxa[] taxas, MesquiteWindow window) {
		treeDisplays = new BasicTreeDisplay[numDisplays];
		this.numDisplays=numDisplays;
		for (int i=0; i<numDisplays; i++) {
			treeDisplays[i] = new BasicTreeDisplay(this, taxas[i]);
			treeDisplays[i].setTreeDrawing(treeDrawTask.createTreeDrawing(treeDisplays[i], taxas[i].getNumTaxa()));
			treeDisplays[i].suppressDrawing(suppression);
		}
		return treeDisplays;
	}
	/*.................................................................................................................
 	public void endJob() {
 		if (MesquiteTrunk.trackActivity) logln ("MesquiteModule " + getName() + "  closing down ");
		closeDownAllEmployees (this);
 		employees.removeElement(treeDrawTask);
 		if (treeDisplay != null)
 			treeDisplay.suppressDrawing(true);
 		treeDisplay = null;
   	 }

	/*.................................................................................................................*/
	private void updateTreeDisplays () {

		if (treeDisplay != null) {
			while (treeDisplay.getDrawingInProcess())
				;		
			if (!suppression)
				treeDisplay.pleaseUpdate(false);
		}
		else if (treeDisplays != null) {
			for (int i=0; i<numDisplays; i++) {
				while (treeDisplays[i].getDrawingInProcess())
					;		
				if (!suppression)
					treeDisplays[i].pleaseUpdate(false);
			}
		}
	}

	long progress   = 0;
	MesquiteInteger pos = new MesquiteInteger(0);
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the current tree form to be the default", null, commandName, "setFormToDefault")) {
			defaultDrawer = " #" + MesquiteModule.getShortClassName(treeDrawTask.getClass());
			storePreferences();
		}
		else if (checker.compare(this.getClass(), "Sets the module to be used to draw the tree", "[name of tree draw module]", commandName, "setTreeDrawer")) {
			incrementMenuResetSuppression();
			DrawTree temp = null;
			if (treeDisplay != null) {
				boolean vis = true;

				while (treeDisplay.getDrawingInProcess()) {
					;		
				}
				vis = treeDisplay.isVisible();
				treeDisplay.setVisible(false);
				treeDisplay.suppressDrawing(true);
				int currentOrientation = treeDisplay.getOrientation();
				temp = (DrawTree)replaceEmployee(DrawTree.class, arguments, "Form of tree?", treeDrawTask);
				if (temp!=null) {
					treeDrawTask = temp;
					if (treeDisplay.getTreeDrawing()!=null)
						treeDisplay.getTreeDrawing().dispose();
					treeDisplay.setTreeDrawing(null);
					treeDrawName.setValue(treeDrawTask.getName());
					treeDrawTask.setHiringCommand(tdC);
					treeDisplay.setTreeDrawing(treeDrawTask.createTreeDrawing(treeDisplay, treeDisplay.getTaxa().getNumTaxa()));
					treeDisplay.suppressDrawing(suppression);
					if (temp.allowsReorientation())
						treeDisplay.setOrientation(currentOrientation);
					else
						currentOrientation = treeDisplay.getOrientation();
					if (!suppression)
						treeDisplay.pleaseUpdate(true);
					treeDisplay.setVisible(vis);
				}
				else {
					treeDisplay.setVisible(vis);
					treeDisplay.suppressDrawing(suppression);
					decrementMenuResetSuppression();
					return null;
			}
			}
			else if (treeDisplays != null) { //many tree displays
				boolean[] vis = new boolean[numDisplays];
				int[] currentOrientations = new int[numDisplays];
				for (int i=0; i<numDisplays; i++) {
					while (treeDisplays[i].getDrawingInProcess())
						;		
					vis[i] = treeDisplays[i].isVisible();
					treeDisplays[i].setVisible(false);
					treeDisplays[i].suppressDrawing(true);
					if (treeDisplays[i].getTreeDrawing() != null)
						treeDisplays[i].getTreeDrawing().dispose();
					treeDisplays[i].setTreeDrawing(null);
					currentOrientations[i] = treeDisplays[i].getOrientation();
				}
				temp = (DrawTree)replaceEmployee(DrawTree.class, arguments, "Form of tree?", treeDrawTask);
				if (temp!=null) {
					treeDrawTask = temp;
					treeDrawName.setValue(treeDrawTask.getName());
					treeDrawTask.setHiringCommand(tdC);
				}
				for (int i=0; i<numDisplays; i++) {
					treeDisplays[i].setTreeDrawing(treeDrawTask.createTreeDrawing(treeDisplays[i], treeDisplays[i].getTaxa().getNumTaxa()));
				}
				for (int i=0; i<numDisplays; i++) {
					if (temp.allowsReorientation())
						treeDisplays[i].setOrientation(currentOrientations[i]);
					else
						currentOrientations[i] = treeDisplays[i].getOrientation();
					treeDisplays[i].suppressDrawing(suppression);
					if (!suppression)
						treeDisplays[i].repaint();
					treeDisplays[i].setVisible(vis[i]);
				}
			}


			decrementMenuResetSuppression();
			if (temp == null)
				return null;
			else {
				if (!MesquiteThread.isScripting())
					parametersChanged();
				return treeDrawTask;
			}
		}
		else if (checker.compare(this.getClass(), "Suppresses tree drawing", null, commandName, "suppress")) {
			suppression=true;
			if (treeDisplay != null) {
				treeDisplay.suppressDrawing(suppression);
			}
			else if (treeDisplays != null) {
				for (int i=0; i<numDisplays; i++) {
					treeDisplays[i].suppressDrawing(suppression);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Removes suppression of tree drawing", null, commandName, "desuppress")) {
			suppression=false;
			if (treeDisplay != null) {
				treeDisplay.suppressDrawing(suppression);
				treeDisplay.pleaseUpdate(true);
			}
			else if (treeDisplays != null) {
				for (int i=0; i<numDisplays; i++) {
					treeDisplays[i].suppressDrawing(suppression);
					treeDisplays[i].pleaseUpdate(true);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Gets current tree window maker", null, commandName, "getTreeWindowMaker")) {
			return getTreeWindowMaker();
		}
		else if (checker.compare(this.getClass(), "Sets background color of tree display", "[name of color]", commandName, "setBackground")) {
			String token = ParseUtil.getFirstToken(arguments, stringPos);
			Color bc = ColorDistribution.getStandardColor(token);
			if (bc == null)
				return null;
			bgColor = bc;
			bgColorName.setValue(token);
			if (treeDisplay != null) {
				while (treeDisplay.getDrawingInProcess())
					;		
				treeDisplay.setBackground(bc);
				Container c = treeDisplay.getParent();
				if (c!=null)
					c.setBackground(bc);
				terminalNamesTask.invalidateNames(treeDisplay);
				if (!suppression)
					treeDisplay.repaintAll();
			}
			else if (treeDisplays != null) {
				for (int i=0; i<numDisplays; i++) {
					while (treeDisplays[i].getDrawingInProcess())
						;		
					treeDisplays[i].setBackground(bc);
					Container c = treeDisplays[i].getParent();
					if (c!=null)
						c.setBackground(bc);
					terminalNamesTask.invalidateNames(treeDisplays[i]);
					if (!suppression)
						treeDisplays[i].repaintAll();
				}
			}
			if (!MesquiteThread.isScripting())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets default color of branches of tree in tree display", "[name of color]", commandName, "setBranchColor")) {
			String token = ParseUtil.getFirstToken(arguments, stringPos);
			Color bc = ColorDistribution.getStandardColor(token);
			if (bc == null)
				return null;
			Color bcD = ColorDistribution.getStandardColorDimmed(ColorDistribution.getStandardColorNumber(bc));
			brColor = bc;
			brColorName.setValue(token);
			if (treeDisplay != null) {
				while (treeDisplay.getDrawingInProcess())
					;		
				treeDisplay.branchColor = bc;
				treeDisplay.branchColorDimmed = bcD;
				Container c = treeDisplay.getParent();
				if (c!=null)
					c.setBackground(bc);
				if (!suppression)
					treeDisplay.pleaseUpdate(false);
			}
			else if (treeDisplays != null) {
				for (int i=0; i<numDisplays; i++) {
					while (treeDisplays[i].getDrawingInProcess())
						;		
					treeDisplays[i].branchColor = bc;
					treeDisplays[i].branchColorDimmed = bcD;
					if (!suppression)
						treeDisplays[i].pleaseUpdate(false);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the number of decimals in the branch length label", "[number]", commandName, "setNumBrLenDecimals")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set Number of Decimal Places Displayed", "Number of decimal places displayed in branch length labels:", numBrLenDecimals.getValue(), 0, 25);
			if (newNum>=0  && newNum!=numBrLenDecimals.getValue()) {
				numBrLenDecimals.setValue(newNum);
				updateTreeDisplays();
			}
		}
		else if (checker.compare(this.getClass(), "Shows node numbers on tree", "[on or off]", commandName, "showNodeNumbers")) {
			showNodeNumbers.toggleValue(arguments);
			updateTreeDisplays();

		}
		else if (checker.compare(this.getClass(), "Shows branch colors on tree", "[on or off]", commandName, "showBranchColors")) {
			showBranchColors.toggleValue(arguments);
			updateTreeDisplays();
		}

		else if (checker.compare(this.getClass(), "Specifies whether or not branch length labels, if shown, are centered along a branch", "[on or off]", commandName, "centerBrLenLabels")) {
			centerBrLenLabels.toggleValue(arguments);
			updateTreeDisplays();

		}
		else if (checker.compare(this.getClass(), "Specifies whether or not ? is shown or not for branches whose length is unspecified", "[on or off]", commandName, "showBrLensUnspecified")) {
			showBrLensUnspecified.toggleValue(arguments);
			updateTreeDisplays();

		}
		else if (checker.compare(this.getClass(), "Allows user to choose the color for branch length labels", "[on or off]", commandName, "chooseBrLenLabelColor")) {
			if (!MesquiteThread.isScripting()) {
				Color color = ColorDialog.queryColor(this.containerOfModule(), "Choose Color", "Color for state", brLenColor);
				if (color!=null){
					brLenColor = color;
					updateTreeDisplays();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Specifies the color", "[on or off]", commandName, "setBrLenLabelColor")) {
			pos.setValue(0); 
			brLenColor = ColorDistribution.getColorFromArguments(arguments, pos);
			updateTreeDisplays();
		}


		else if (checker.compare(this.getClass(), "Specifies whether or not branch length labels, if shown, are also shown on terminal branches of tree", "[on or off]", commandName, "showBrLenLabelsOnTerminals")) {
			showBrLenLabelsOnTerminals.toggleValue(arguments);
			updateTreeDisplays();
		}
		else if (checker.compare(this.getClass(), "Shows branch lengths on tree", "[on or off]", commandName, "labelBranchLengths")) {
			labelBranchLengths.toggleValue(arguments);
			updateTreeDisplays();
		}
		else if (checker.compare(this.getClass(), "Returns the tree drawing module in use", null, commandName, "getTreeDrawer")) {
			return treeDrawTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void endJob(){
		treeDrawTask=null;
		terminalNamesTask=null;
		if (treeDisplay != null) {
			treeDisplay=null;
		}
		else if (treeDisplays != null) {
			for (int i=0; i<treeDisplays.length; i++) {
				treeDisplays[i]=null;
			}
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (MesquiteThread.isScripting())
			return;
		if (source instanceof DrawNamesTreeDisplay && Notification.getCode(notification) == TreeDisplay.FONTSIZECHANGED ){
			MesquiteWindow w = null;
			if (treeDisplay != null) 
				w =MesquiteWindow.windowOfItem(treeDisplay);

			else if (treeDisplays != null && numDisplays>0) 
				w =MesquiteWindow.windowOfItem(treeDisplays[0]);
			if (w != null){
				w.windowResized();  //this is a hack to force them to update sizes
				return;
		}
		}
		
		if (treeDisplay != null) {
			((BasicTreeDisplay)treeDisplay).pleaseUpdate(true);
		}
		else if (treeDisplays != null) {
			for (int i=0; i<numDisplays; i++) {
				((BasicTreeDisplay)treeDisplays[i]).pleaseUpdate(true);
			}
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Basic Tree Draw Coordinator";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Tree Drawing";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates the drawing of a tree by maintaining the basic TreeDisplay and by hiring a DrawTree module." ;
	}

}

/* ======================================================================== */
class BasicTreeDisplay extends TreeDisplay  {
	boolean showPixels = false;//for debugging
	BasicTreeDrawCoordinator ownerDrawModule;
	public BasicTreeDisplay (BasicTreeDrawCoordinator ownerModule, Taxa taxa) {
		super(ownerModule, taxa);
		ownerDrawModule = ownerModule;
		suppress = true;
		setBackground(Color.white);
	}
	public void setTree(Tree tree) {
		if (ownerModule.isDoomed())
			return;
		boolean wasNull = (this.tree == null);
		((DrawTreeCoordinator)ownerModule).getNamesTask().setTree(tree);
		super.setTree(tree);//here ask for nodelocs to be calculated
		if (wasNull)
			repaint();

	}
	/* */
	public boolean autoFontSubmenu () {
		return false;
	}
	public void setOrientation(int o){
		boolean transpose =  (o == TreeDisplay.UP || o == TreeDisplay.DOWN) &&(getOrientation() == TreeDisplay.RIGHT || getOrientation() == TreeDisplay.LEFT);
		transpose = transpose ||  (getOrientation() == TreeDisplay.UP || getOrientation() == TreeDisplay.DOWN) &&(o == TreeDisplay.RIGHT || o == TreeDisplay.LEFT);
		super.setOrientation(o);
		if (transpose) {
			TreeWindowMaker tw = (TreeWindowMaker) ownerModule.findEmployerWithDuty(TreeWindowMaker.class);
			if (tw != null)
				tw.transposeField();
		}

	}
	/**/
	public void forceRepaint(){
		if (ownerModule.isDoomed())
			return;
		repaintsPending = 0;
		repaint(MesquiteThread.isScripting());
	}
	static int cr = 0;
	public void repaint(boolean resetTree) {  //TODO: this whole system needs revamping.  
		if (ownerModule == null || ownerModule.isDoomed())
			return;

		if (tree!=null && resetTree) {
			recalculatePositions();
		}
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w != null && (!w.isVisible() || !w.isFrontMostInLocation()))
			return;
		repaintRequests++;
		if (repaintRequests>1000){
			repaintRequests = 0;
			MesquiteMessage.warnProgrammer("more than 1000 repaint requests in Tree Display");
			MesquiteMessage.printStackTrace("more than 1000 repaint requests in Tree Display");
		}
		super.repaint();
	}

	public void repaint(){
		if (ownerModule == null || ownerModule.isDoomed())
			return;
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w != null && (!w.isVisible() || !w.isFrontMostInLocation()))
			return;
		repaintRequests++;
		if (repaintRequests>1000){
			repaintRequests = 0;
			MesquiteMessage.warnProgrammer("more than 1000 repaint requests in Tree Display");
			MesquiteMessage.printStackTrace("more than 1000 repaint requests in Tree Display");
		}

		super.repaint();
	}
	/*_________________________________________________*/
	long repaintRequests = 0;
	int retry =0;
	/*_________________________________________________*/
	public void paint(Graphics g) {
		if (getPrintingInProcess())
			return;
		if (ownerModule == null || ownerModule.isDoomed())
			return;
		setShowBranchColors(ownerDrawModule.showBranchColors.getValue());
		if (MesquiteWindow.checkDoomed(this)) 
			return;
		setDrawingInProcess(true);
		int initialPending = repaintsPending;
		which =0;
		if (bailOut(initialPending)) return;
		if (getParent().getBackground()!=getBackground())
			getParent().setBackground(getBackground());
		if (bailOut(initialPending)) return;

		if (getFieldWidth()==0 || getFieldHeight()==0)
			setFieldSize(getBounds().width, getBounds().height);
		if (bailOut(initialPending)) return;
		if (getTipsMargin()<0 && getTreeDrawing()!=null && tree !=null)
			getTreeDrawing().recalculatePositions(tree);
		if (bailOut(initialPending)) return;
		if (getTipsMargin()<0)
			setTipsMargin(0);
		if (bailOut(initialPending)) return;
		super.paint(g);
		if (bailOut(initialPending)) return;
		Tree tempTree = getTree();
		if (bailOut(initialPending)) return;
		if (tree==null) {
			//repaint();
		}
		else if (getTreeDrawing()==null) {
			repaint();
		}
		else if (suppress) {
			if (retry>500)
				System.out.println("Error: retried " + retry + " times to draw tree; remains suppressed");
			else {
				retry++;
				repaint();
			}
		}
		else if (!tree.isLocked() && tree.isDefined()) {
			int stage = -1;
			try {
				if (tree == null || tree.getTaxa().isDoomed()) {
					setDrawingInProcess(false);
					MesquiteWindow.uncheckDoomed(this);
					return;
				}
				if (bailOut(initialPending)) return;
				retry = 0;
				if (showPixels){
					for (int h=0; h<getFieldWidth() &&  h<getFieldHeight(); h += 50) {
						g.setColor(Color.red);
						g.drawString(Integer.toString(h), h, h);
					}
				}
				int dRoot = getTreeDrawing().getDrawnRoot();
				if (!tree.nodeExists(dRoot))
					dRoot = tree.getRoot();
				//getTreeDrawing().setHighlightsOn(tree.anySelectedInClade(dRoot));
				if (bailOut(initialPending)) return;
				stage = 0;
				drawAllBackgroundExtras(tree, dRoot, g);
				stage = 1;	

				if (bailOut(initialPending)) return;
				getTreeDrawing().drawTree(tree, dRoot, g); //ALLOW other drawnRoots!

				//showNodeLocations(tree, g, tree.getRoot());
				stage = 2;

				if (bailOut(initialPending)) return;
				drawAllExtras(tree, dRoot, g);
				if (bailOut(initialPending)) return;
				stage = 3;
				if (ownerDrawModule.labelBranchLengths.getValue())
					drawBranchLengthsOnTree(tree, dRoot, g);
				stage = 4;
				if (ownerDrawModule.showNodeNumbers.getValue())
					drawNodeNumbersOnTree(tree, dRoot, g);
				stage = 5;
				if (bailOut(initialPending)) return;

				if (!suppressNames && ownerModule!=null && ((DrawTreeCoordinator)ownerModule).getNamesTask()!=null)
					((DrawTreeCoordinator)ownerModule).getNamesTask().drawNames(this, tree, dRoot, g);
				stage = 6;
				if (bailOut(initialPending)) return;
				if (getTreeDrawing()!=null && tree !=null && getHighlightedBranch() > 0) 
					getTreeDrawing().highlightBranch(tree, getHighlightedBranch(),g); 
				stage = 7;
				if (bailOut(initialPending)) return;
			}
			catch (Throwable e){
				MesquiteMessage.println("Error or Exception in tree drawing (stage " + stage +") (" + e.toString() + ")");
				//				MesquiteMessage.println("Error or Exception in tree drawing (stage " + stage +")");
				MesquiteFile.throwableToLog(this, e);
			}
		}
		setDrawingInProcess(false);
		if (tempTree != tree) {
			repaint();
		}
		else if (bailOut(initialPending))
			return;
		else if (!isVisible())
			repaint();
		else
			repaintsPending = 0;
		repaintRequests = 0;
		MesquiteWindow.uncheckDoomed(this);
		setInvalid(false);

	}
	public void update(Graphics g){
		super.update(g);
	}
	private int which = 0;

	private boolean bailOut(int initialPending){
		which++;
		if (getPrintingInProcess() || repaintsPending>initialPending){
			setDrawingInProcess(false);
			repaintsPending  = 0;
			repaint();
			return true;
		}
		return false;
	}
	/*_________________________________________________*/
	private   void showNodeLocations(Tree tree, Graphics g, int N) {
		if (tree.nodeExists(N)) {
			g.setColor(Color.red);
			GraphicsUtil.fillOval(g,getTreeDrawing().x[N], getTreeDrawing().y[N], 4, 4);
			g.setColor(branchColor);
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				showNodeLocations( tree, g, d);
		}
	}

	/*_________________________________________________*/
	public void print(Graphics g) {
		printAll(g);
	}
	/*_________________________________________________*/
	public void printAll(Graphics g) {
		if (g == null)
			return;
		setPrintingInProcess(true);
		if (getFieldWidth()==0 || getFieldHeight()==0)
			setFieldSize(getBounds().width, getBounds().height);
		int ww = getWidth();
		int hh = getHeight();

		setSize(getFieldWidth(), getFieldHeight());
		//super.paint(g);
		if (tree==null)
			MesquiteMessage.warnProgrammer("tree NULL in tree draw coord");
		else if ((!suppress) && (!tree.isLocked())) {
			repaintsPending = 0;

			/* NEEDS TO DRAW BACKGROUND EXTRAS */
			int dRoot = getTreeDrawing().getDrawnRoot();
			if (!tree.nodeExists(dRoot))
				dRoot = tree.getRoot();
			printAllBackgroundExtras(tree, dRoot, g);
			getTreeDrawing().drawTree(tree, dRoot, g); //OTHER ROOTS
			printAllExtras(tree, dRoot, g);
			if (ownerDrawModule.labelBranchLengths.getValue())
				drawBranchLengthsOnTree(tree, dRoot, g);
			if (ownerDrawModule.showNodeNumbers.getValue())
				drawNodeNumbersOnTree(tree, dRoot, g);
			if (!suppressNames && ownerModule!=null && ((DrawTreeCoordinator)ownerModule).getNamesTask()!=null)
				((DrawTreeCoordinator)ownerModule).getNamesTask().drawNames(this, tree, dRoot, g);
			if (g instanceof PdfGraphics2D) 	//headless:  comment out
				printComponentsPDF(g);		//headless:  comment out
			else										//headless:  comment out
				printComponents(g);
		} 
		else MesquiteMessage.warnProgrammer("tree drawing suppressed");
		setSize(ww, hh);
		setPrintingInProcess(false);
	}
	void printComponentsPDF(Graphics g){
		Component[] comps = getComponents();
		for (int i = 0; i<comps.length; i++){
			Component comp = comps[i];
			g.translate(comp.getX(), comp.getY());
			comp.print(g);
			g.translate(-comp.getX(), -comp.getY());
		}
	}
	private int spotsize = 18;
	/*_________________________________________________*/
	private   void drawSpot(TreeDisplay treeDisplay, Tree tree, Graphics g, int N) {
		if (tree.nodeExists(N)) {
			if (treeDisplay.getVisRect() == null || treeDisplay.getVisRect().contains(treeDisplay.getTreeDrawing().x[N], treeDisplay.getTreeDrawing().y[N])){
			if (tree.nodeIsInternal(N) || true){  //replace true by show terminal
				//int i=0;
				//int j=2;
				String s = Integer.toString(N);
				FontMetrics fm = g.getFontMetrics(g.getFont());
				int width = fm.stringWidth(s) + 6;
				int height = fm.getAscent()+fm.getDescent() + 6;
				if (spotsize>width)
					width = spotsize;
				if (spotsize>height)
					height = spotsize;
				g.setColor(Color.white);
				double x = treeDisplay.getTreeDrawing().x[N] - width/2;
				double y = treeDisplay.getTreeDrawing().y[N] - height/2;
				GraphicsUtil.fillOval(g,x , y, width, height);
			/*	g.setColor(Color.red);
				Graphics2D g2 = (Graphics2D)g;
				GraphicsConfiguration gc;
				java.awt.geom.AffineTransform at, at0, nt;
				String ss = "";
				gc = g2.getDeviceConfiguration();
				 at0 = g2.getTransform();
				 at = gc.getDefaultTransform();
				 nt = gc.getNormalizingTransform();
				ss += " " +  at0.getTranslateX() + " " +  at.getTranslateX() + " " + nt.getTranslateX() + "/ ";
				g.fillRect(x, y, width, height);
				gc = g2.getDeviceConfiguration();
				 at0 = g2.getTransform();
				 at = gc.getDefaultTransform();
				 nt = gc.getNormalizingTransform();
				ss += " " +  at0.getTranslateX() + " " +  at.getTranslateX() + " " + nt.getTranslateX() + "/ ";
				*/
				g.setColor(Color.black);
			   GraphicsUtil.drawString(g,Integer.toString(N), x+2, y-4+ height);
				/*g.drawRect(x , y, width, height);
				gc = g2.getDeviceConfiguration();
				 at0 = g2.getTransform();
				 at = gc.getDefaultTransform();
				 nt = gc.getNormalizingTransform();
				ss += " " +  at0.getTranslateX() + " " +  at.getTranslateX() + " " + nt.getTranslateX() + "/ ";
				*/
				GraphicsUtil.drawOval(g,x , y, width, height);
				/*gc = g2.getDeviceConfiguration();
				 at0 = g2.getTransform();
				 at = gc.getDefaultTransform();
				 nt = gc.getNormalizingTransform();
				ss += " " +  at0.getTranslateX() + " " +  at.getTranslateX() + " " + nt.getTranslateX() + "/ ";
				*/
			}
			}
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawSpot(treeDisplay, tree, g, d);
		}
	}
	/*_________________________________________________*/
	public   void drawSpots(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Graphics g) {
		if (MesquiteTree.OK(tree)) {
			drawSpot(treeDisplay, tree, g, drawnRoot);  
		}
	}
	/*.................................................................................................................*/
	public   void drawNodeNumbersOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawSpots(this, tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public void writeLengthAtNode(Graphics g, int N,  Tree tree) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			writeLengthAtNode(g, d, tree);

		if (!MesquiteDouble.isCombinable(tree.getBranchLength(N)) && !ownerDrawModule.getShowBrLensUnspecified())
			return;

		if (ownerDrawModule.getShowBrLenLabelsOnTerminals() || !tree.nodeIsTerminal(N)) {
			StringBuffer sb = new StringBuffer();
			MesquiteDouble.toStringDigitsSpecified(tree.getBranchLength(N), ownerDrawModule.getNumBrLenDecimals(), sb);
			int shiftX = 10;
			int shiftY = 10;
			int nameLength = 0;
			int nameHeight = 0;
			double nodeX = getTreeDrawing().x[N];
			double nodeY = getTreeDrawing().y[N];
			MesquiteDouble angle = new MesquiteDouble();
			if (ownerDrawModule.getCenterBrLenLabels()){
				int motherNode = tree.motherOfNode(N);
				if (tree.nodeExists(motherNode)){
					shiftX = 0;
					shiftY = 0;
					nameLength = StringUtil.getStringDrawLength(g,sb.toString());
					nameHeight = StringUtil.getTextLineHeight(g);
					MesquiteNumber centeredNodeX = new MesquiteNumber();
					MesquiteNumber centeredNodeY = new MesquiteNumber();
					getTreeDrawing().getMiddleOfBranch(tree,N,centeredNodeX,centeredNodeY,angle);
					if (centeredNodeX.isCombinable() && centeredNodeY.isCombinable()){
						nodeX = centeredNodeX.getIntValue();
						nodeY = centeredNodeY.getIntValue();
					}
				}
			}

			if (getOrientation() == TreeDisplay.UP) {
				nodeY+=shiftY+ nameHeight/2;
				nodeX-=nameLength/2;
			}
			else if (getOrientation() == TreeDisplay.DOWN) {
				nodeY-=shiftY;
				nodeX-=nameLength/2;
			}
			else if (getOrientation() == TreeDisplay.RIGHT) {
				nodeX-=shiftX+ nameLength/2;
				nodeY-=2;
			}
			else if (getOrientation() == TreeDisplay.LEFT) {
				nodeX+=shiftX+getTreeDrawing().getEdgeWidth()-nameLength/2;
			}

			StringUtil.highlightString(g, sb.toString(), nodeX, nodeY, ownerDrawModule.getBrLenColor(), Color.white);
		}
	}
	/*.................................................................................................................*/
	public   void drawBranchLengthsOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (tree!=null) {
			g.setColor(Color.blue);
			writeLengthAtNode(g, drawnRoot, tree);
			g.setColor(Color.black);
		}
	}
	/*_________________________________________________*/
	public void fillTaxon(Graphics g, int M) {
		((DrawTreeCoordinator)ownerModule).getNamesTask().fillTaxon(g, M);
	}
	/*_________________________________________________*/
	public void redrawTaxa(Graphics g, int M) {
	((DrawTreeCoordinator)ownerModule).getNamesTask().drawNames(this, tree, getTreeDrawing().getDrawnRoot(), g);
		
	}
	/*_________________________________________________*/
	private boolean responseOK(){
		return (!getDrawingInProcess() && (tree!=null) && (!tree.isLocked()) && ownerModule!=null &&  (ownerModule.getEmployer() instanceof TreeDisplayActive));
	}
	/*_________________________________________________*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (responseOK()) {
			try{
				Graphics g = getGraphics();
				boolean dummy = ((TreeDisplayActive)ownerModule.getEmployer()).mouseMoveInTreeDisplay(modifiers,x,y,this, g);
				if (g!=null)
					g.dispose();
			}
			catch(Exception e){
			}
		}
		MesquiteWindow.uncheckDoomed(this);
		super.mouseMoved(modifiers,x,y, tool);
	}
	/*_________________________________________________*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		boolean somethingTouched = false;
		if (responseOK()) {
			try{
				Graphics g = getGraphics();
				somethingTouched = ((TreeDisplayActive)ownerModule.getEmployer()).mouseDownInTreeDisplay(modifiers,x,y,this, g);
				if (g!=null)
					g.dispose();
			}
			catch(Exception e){
			}
		}
		if (!somethingTouched)
			super.panelTouched(modifiers, x,y, true);
		MesquiteWindow.uncheckDoomed(this);
	}

	/*_________________________________________________*/
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (responseOK()) {
			try {
				Graphics g = getGraphics();
				boolean dummy = ((TreeDisplayActive)ownerModule.getEmployer()).mouseDragInTreeDisplay(modifiers,x,y,this, g);
				if (g!=null)
					g.dispose();
			}
			catch(Exception e){
			}
		}
		MesquiteWindow.uncheckDoomed(this);
		super.mouseDrag(modifiers,x,y, tool);
	}
	/*_________________________________________________*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (responseOK()) {
			try {Graphics g = getGraphics();
			boolean dummy = ((TreeDisplayActive)ownerModule.getEmployer()).mouseUpInTreeDisplay(modifiers,x,y,this, g);
			if (g!=null)
				g.dispose();
			}
			catch(Exception e){
			}
		}
		MesquiteWindow.uncheckDoomed(this);
		super.mouseUp(modifiers,x,y, tool);
	}
}


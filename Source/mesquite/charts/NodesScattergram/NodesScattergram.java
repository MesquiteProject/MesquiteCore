/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.NodesScattergram;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**=== Class NodesScattergram.  ===*/

public class NodesScattergram extends TreeWindowAssistantC implements ChartListener  {
	/*.................................................................................................................*/
	public String getName() {
		return "Nodes Scattergram";
	}
	public String getExplanation() {
		return "Shows values for nodes of tree via a scattergram." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumbersForNodes.class, getName() + " needs methods to calculate two values (such as depth, independent contrast, etc.) for a series of nodes of the tree in the tree window.",
		"You can select this either when the chart starts up, or using the Values submenus of the Chart menu.  (You may request the chart itself by selecting the Nodes item under New Chart For Tree in the Analysis menu of the Tree Window)");
		e.setPriority(1);
		EmployeeNeed e3 = registerEmployeeNeed(DrawChart.class, getName() + " needs an assistant to draw charts.",
		"The chart drawing module is arranged automatically");
	}
	/*.................................................................................................................*/
	NumbersForNodes numbersForNodesTaskX, numbersForNodesTaskY;
	public DrawChart charterTask;
	private Tree tree;
	int currentX = 0;
	int currentY = 1;
	NodeLabeller nsLabeller;
	boolean separateAxes = true;
	TreeDisplay treeDisplay;
	boolean showTreeLabels = false;
	MesquiteString numberTaskXName, numberTaskYName;
	MesquiteCommand setXCommand, setYCommand;
	MesquiteCommand ntxC, ntyC, ntC;
	NodeScattergramWindow window;
	boolean suspend=false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		charterTask = (DrawChart)hireNamedEmployee(DrawChart.class, "Scattergram");
		if (charterTask == null)
			return sorry(getName() + " couldn't start because no charting module obtained.");
		setXCommand = makeCommand("setX", this);
		setYCommand = makeCommand("setY", this);
		ntxC =makeCommand("setValuesX",  this);
		ntyC =makeCommand("setValuesY",  this);
		ntC =makeCommand("setValues",  this);
		numberTaskXName = new MesquiteString();
		numberTaskYName = new MesquiteString();

		window =  new NodeScattergramWindow(this);
		setModuleWindow(window);
		window.setWindowSize(400,400);
		if (!MesquiteThread.isScripting()) {
			String expl = "(For instance, the X and Y axes might show the same calculations but for different characters, or they may show two entirely different calculations.)";
			separateAxes = (numModulesAvailable(NumbersForNodesIncr.class)==0) || !AlertDialog.query(containerOfModule(), "Axes", "Choose same or different calculations for the two axes? " + expl, "Same", "Different");
			if (!separateAxes){
				numbersForNodesTaskX = (NumbersForNodesIncr)hireEmployee(NumbersForNodesIncr.class, "Values for axes");
				if (numbersForNodesTaskX == null)
					return sorry(getName() + " couldn't start because no calculating module obtained.");
				/*if (numModulesAvailable(NumbersForNodesIncr.class)>1) {
					MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, NumbersForNodesIncr.class);
		 			mss.setSelected(numberTaskXName);
				}*/
				numberTaskXName.setValue(numbersForNodesTaskX.getName());
				numbersForNodesTaskX.setHiringCommand(ntC);
				numbersForNodesTaskY = numbersForNodesTaskX;
			}
			else {
				/*MesquiteMenuSpec xMenu = addAuxiliaryMenu("X");
				if (numModulesAvailable(NumbersForNodes.class)>1){ 
					MesquiteSubmenuSpec mss = addSubmenu(xMenu, "Values on X", ntxC, NumbersForNodes.class);
					mss.setSelected(numberTaskXName);
				}*/
				numbersForNodesTaskX = (NumbersForNodes)hireEmployee(NumbersForNodes.class, "Values for X axis");
				if (numbersForNodesTaskX == null)
					return sorry(getName() + " couldn't start because no calculating module for X axis obtained.");
				//numbersForNodesTaskX.setMenuToUse(xMenu);
				numbersForNodesTaskX.setHiringCommand(ntxC);
				numberTaskXName.setValue(numbersForNodesTaskX.getName());


				/*MesquiteMenuSpec yMenu = addAuxiliaryMenu("Y");
				if (numModulesAvailable(NumbersForNodes.class)>1){
					MesquiteSubmenuSpec mss = addSubmenu(yMenu, "Values on Y", ntyC, NumbersForNodes.class);
					mss.setSelected(numberTaskYName);
				}*/
				numbersForNodesTaskY = (NumbersForNodes)hireEmployee(NumbersForNodes.class, "Values for Y axis");
				if (numbersForNodesTaskY == null)
					return sorry(getName() + " couldn't start because no calculating module for Y axis obtained.");
				//numbersForNodesTaskY.setMenuToUse(yMenu);
				numberTaskYName.setValue(numbersForNodesTaskY.getName());
				numbersForNodesTaskY.setHiringCommand(ntyC);
			}
			setMenus();
			window.setVisible(true);
		}
		if (getEmployer() instanceof TreeWindowMaker) {
			addMenuItem( "Tree labels", makeCommand("toggleTreeLabels",  this));
			treeDisplay = ((TreeWindowMaker)getEmployer()).getTreeDisplay();
			nsLabeller = new NodeLabeller(this, treeDisplay, (NodeScattergramWindow)window);
			nsLabeller.setShowLabels(showTreeLabels);

			treeDisplay.addExtra(nsLabeller);
		}
		makeMenu("Scattergram");
		resetContainingMenuBar();
		resetAllWindowsMenus();
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		if (m == charterTask)
			iQuit();
	}
	MesquiteMenuSpec xMenu, yMenu;
	private void setMenus(){
		if (!separateAxes){
			if (numModulesAvailable(NumbersForNodesIncr.class)>1) {
				MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, NumbersForNodesIncr.class);
				mss.setSelected(numberTaskXName);
			}
		}
		else {
			xMenu = addAuxiliaryMenu("X");
			if (numModulesAvailable(NumbersForNodes.class)>1){ 
				MesquiteSubmenuSpec mss = addSubmenu(xMenu, "Values on X", ntxC, NumbersForNodes.class);
				mss.setSelected(numberTaskXName);
			}
			if (numbersForNodesTaskX != null)
				numbersForNodesTaskX.setMenuToUse(xMenu);


			yMenu = addAuxiliaryMenu("Y");
			numberTaskYName = new MesquiteString();
			if (numModulesAvailable(NumbersForNodes.class)>1){
				MesquiteSubmenuSpec mss = addSubmenu(yMenu, "Values on Y", ntyC, NumbersForNodes.class);
				mss.setSelected(numberTaskYName);
			}
			if (numbersForNodesTaskY != null)
				numbersForNodesTaskY.setMenuToUse(yMenu);
		}

	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
		whichWindow.dispose();
		if (nsLabeller != null)
			nsLabeller.turnOff();
		iQuit();
	}
	/*...................................................................................................................*/
	public void pointMouseDown(MesquiteChart chart, int whichPoint, MesquiteNumber valueX, MesquiteNumber valueY, int x, int y, int modifiers, String message){
		((NodeScattergramWindow)window).pointTouched(tree, whichPoint);
	}
	/*...................................................................................................................*/
	public void pointMouseUp(MesquiteChart chart, int whichPoint, int x, int y, int modifiers, String message){
		((NodeScattergramWindow)window).pointReleased(tree, whichPoint);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suspend");
		temp.addLine("setChartType ", charterTask); 
		if (separateAxes){
			temp.addLine("differentAxes");
			temp.addLine("setValuesX ", numbersForNodesTaskX); 
			temp.addLine("setValuesY ", numbersForNodesTaskY); 
			if (numbersForNodesTaskX instanceof Incrementable){
				Incrementable inc = (Incrementable)numbersForNodesTaskX;
				temp.addLine("setX " + inc.toExternal(currentX)); 
			}
			if (numbersForNodesTaskY instanceof Incrementable){
				Incrementable inc = (Incrementable)numbersForNodesTaskY;
				temp.addLine("setY " + inc.toExternal(currentY)); 
			}
		}
		else {
			temp.addLine("sameAxes");
			temp.addLine("setValues ", numbersForNodesTaskX); 
			if (numbersForNodesTaskX instanceof Incrementable){
				Incrementable inc = (Incrementable)numbersForNodesTaskX;
				temp.addLine("setX " + inc.toExternal(currentX)); 
				temp.addLine("setY " + inc.toExternal(currentY)); 
			}
		}
		temp.addLine("toggleTreeLabels " + MesquiteBoolean.toOffOnString(showTreeLabels)); 
		if (window ==null)
			return null;
		Snapshot fromWindow = window.getSnapshot(file);
		temp.addLine("getWindow");
		temp.addLine("tell It");
		temp.incorporate(fromWindow, true);
		temp.addLine("endTell");
		temp.addLine("doCounts");
		temp.addLine("resume");
		temp.addLine("showWindow");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets chart drawing module", "[module name]", commandName, "setChartType")){
			DrawChart temp =  (DrawChart)replaceEmployee(DrawChart.class, arguments, "Type of chart", charterTask);
			if (temp!=null) {
				charterTask=  temp;

				if (tree instanceof Associable)
					charterTask.pointsAreSelectable(true, (Selectionable)tree, false);
				((NodeScattergramWindow)window).setCharter(charterTask.createCharter(this));
				getModuleWindow().contentsChanged();
				return temp;
			}
		} 
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspend = true;
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspend = false;
			doCounts();
		}
		else if (checker.compare(this.getClass(), "Requests that calculations be redone", null, commandName, "doCounts")) { 
			doCounts();
		}
		else if (checker.compare(this.getClass(), "Sets both axes to use the same calculation", null, commandName, "sameAxes")) { //todo: let this change after the chart is established
			separateAxes = false;
			setMenus();

		}
		else if (checker.compare(this.getClass(), "Sets the axes to use different calculations", null, commandName, "differentAxes")) {
			separateAxes = true;
			setMenus();
			/*if (numModulesAvailable(NumbersForNodes.class)>1){ 
				MesquiteSubmenuSpec mss = addSubmenu(null, "Values on X", ntxC, NumbersForNodes.class);
				mss.setSelected(numberTaskXName);
			}

			if (numModulesAvailable(NumbersForNodes.class)>1){
				MesquiteSubmenuSpec mss = addSubmenu(null, "Values on Y", ntyC, NumbersForNodes.class);
				mss.setSelected(numberTaskYName); 
			}*/
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating the numbers for the nodes for both axes", "[name of module]", commandName, "setValues")) {
			NumbersForNodes temp =  (NumbersForNodes)replaceEmployee(NumbersForNodes.class, arguments, "Choose values for axes",numbersForNodesTaskX);
			if (temp!=null) {
				numbersForNodesTaskX=  temp;
				numbersForNodesTaskY = temp;
				numberTaskXName.setValue(numbersForNodesTaskX.getName());
				numbersForNodesTaskX.setHiringCommand(ntC);
				currentX=0;
				currentY=1;
				doCounts();
				getModuleWindow().contentsChanged();
			}
			return numbersForNodesTaskX;
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating the numbers for the nodes for the X axis", "[name of module]", commandName, "setValuesX")) {
			NumbersForNodes temp =  (NumbersForNodes)replaceEmployee(NumbersForNodes.class, arguments, "Choose values for X axis",numbersForNodesTaskX);
			if (temp!=null) {
				numbersForNodesTaskX=  temp;
				numberTaskXName.setValue(numbersForNodesTaskX.getName());
				numbersForNodesTaskX.setHiringCommand(ntxC);
				currentX=0;
				if (separateAxes && yMenu != null && numbersForNodesTaskX != null)
					numbersForNodesTaskX.setMenuToUse(xMenu);
				doCounts();
				getModuleWindow().contentsChanged();
			}
			return numbersForNodesTaskX;
		}
		else if (checker.compare(this.getClass(), "Sets the module calculating the numbers for the nodes for the Y axis", "[name of module]", commandName, "setValuesY")) {
			NumbersForNodes temp =  (NumbersForNodes)replaceEmployee(NumbersForNodes.class, arguments, "Choose values for Y axis",numbersForNodesTaskY);
			if (temp!=null) {
				numbersForNodesTaskY=  temp;
				numberTaskYName.setValue(numbersForNodesTaskY.getName());
				numbersForNodesTaskY.setHiringCommand(ntyC);
				if (separateAxes && yMenu != null && numbersForNodesTaskY != null)
					numbersForNodesTaskY.setMenuToUse(yMenu);
				currentY=0;
				doCounts();
				getModuleWindow().contentsChanged();
			}
			return numbersForNodesTaskY;
		}
		else if (checker.compare(this.getClass(), "Sets which item is shown on the x axis (appropriate where the calculator can supply a series of items, e.g. characters)", "[item number]", commandName, "setX")) {
			if (numbersForNodesTaskX instanceof Incrementable){
				Incrementable inc = (Incrementable)numbersForNodesTaskX;
				int ic = (MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
				if (!MesquiteInteger.isCombinable(ic))
					ic = MesquiteInteger.queryInteger(containerOfModule(), "Choose ", "choose:", 1); //TODO have something more intelligent here
				int icInternal = (int)inc.toInternal(ic);
				if (MesquiteInteger.isCombinable(icInternal) && (icInternal>=inc.getMin()) && (icInternal<=inc.getMax())) {
					currentX=icInternal;
					if (!MesquiteThread.isScripting()){
						doCounts();
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Sets which item is shown on the y axis (appropriate where the calculator can supply a series of items, e.g. characters)", "[item number]", commandName, "setY")) {
			if (numbersForNodesTaskY instanceof Incrementable){
				Incrementable inc = (Incrementable)numbersForNodesTaskY;
				int ic = (MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
				if (!MesquiteInteger.isCombinable(ic))
					ic = MesquiteInteger.queryInteger(containerOfModule(), "Choose ", "choose:", 1); //TODO have something more intelligent here
				int icInternal = (int)inc.toInternal(ic);
				if (MesquiteInteger.isCombinable(icInternal) && (icInternal>=inc.getMin()) && (icInternal<=inc.getMax())) {
					currentY=icInternal;
					if (!MesquiteThread.isScripting()){
						doCounts();
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the tree labels are shown", "[on or off]", commandName, "toggleTreeLabels")) {
			//TODO: use MesquiteBoolean checkmark
			if (StringUtil.blank(arguments))
				showTreeLabels = !showTreeLabels;
			else {
				String s = ParseUtil.getFirstToken(arguments, pos);
				if ("on".equalsIgnoreCase(s))
					showTreeLabels = true;
				else if  ("off".equalsIgnoreCase(s))
					showTreeLabels = false;
			}
			nsLabeller.setShowLabels(showTreeLabels);

			treeDisplay.pleaseUpdate(false);
		} 
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	long oldTreeID = -1;
	long oldTreeVersion = 0;
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (tree==null)
			return;
		this.tree=tree;
		if (tree instanceof Associable)
			charterTask.pointsAreSelectable(true, (Selectionable)tree, false);
		if (oldTreeID==-1) {
			if (numbersForNodesTaskX==null)
				return;
			numbersForNodesTaskX.initialize(tree);
			if (numbersForNodesTaskX != numbersForNodesTaskY && numbersForNodesTaskY!=null)
				numbersForNodesTaskY.initialize(tree);
			doCounts();
		}
		else if (tree.getID() != oldTreeID || tree.getVersionNumber() != oldTreeVersion) {
			doCounts();
		}
		else {
			if (window!=null &&((NodeScattergramWindow)window).getChart()!=null ) {
				((NodeScattergramWindow)window).getChart().getField().repaint(); //for selection Only
				((NodeScattergramWindow)window).getChart().repaint(); //for selection Only
			}
		}
		oldTreeID = tree.getID();
		oldTreeVersion = tree.getVersionNumber();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee instanceof NumbersForNodes) {
			if (numbersForNodesTaskX!=null && numbersForNodesTaskY!=null)
				doCounts();
		}

	}
	int numItemsX(){
		if (tree==null)
			return 0;
		return 1;
		//numbersForNodesTaskX.getNumberOfCharacters(tree);
	}
	int numItemsY(){
		if (tree==null)
			return 0;
		return 1;
		//numbersForNodesTaskY.getNumberOfCharacters(tree);
	}
	/*.................................................................................................................*/
	public void doCounts() {
		if (!suspend) {
			((NodeScattergramWindow)window).blankChart();
			((NodeScattergramWindow)window).setTree(tree);
			((NodeScattergramWindow)window).recalcChart();
		}
	}
	public void endJob() {
		if (treeDisplay !=null)
			treeDisplay.removeExtra(nsLabeller);
		super.endJob();
	}
}

/*========================================================*/
class NodeLabeller extends TreeDisplayExtra {
	LabelsAtNodes labelsAtNodes;
	NodeScattergramWindow chartWindow;
	Color brightGreen, brightbrightGreen;
	boolean showLabels = true;
	int labelDrawn = -1;

	public NodeLabeller (MesquiteModule ownerModule, TreeDisplay treeDisplay, NodeScattergramWindow chartWindow) {
		super(ownerModule, treeDisplay);
		this.chartWindow =chartWindow;
		brightGreen = new Color((float)0.4, (float)1.0, (float)0.4);
		brightbrightGreen = new Color((float)0.6, (float)1.0, (float)0.6);
	}
	/*.................................................................................................................*/
	public void drawOneLabel(int N, Tree tree, Graphics g) {
		makeSureLabelsReady(tree);
		double nodeX = treeDisplay.getTreeDrawing().x[N];
		double nodeY = treeDisplay.getTreeDrawing().y[N];
		MesquiteLabel c = (MesquiteLabel)labelsAtNodes.getPanel(N);

		c.setColor(brightbrightGreen); 
		c.setText("Node " + N);  
		String[] results = chartWindow.getStrings(N);
		for (int i = 0; i<results.length; i++)
			c.addLine(results[i]);  

		c.setVisible(true);
		c.repaint();
		int w = c.getWidth(g);
		if (w+nodeX+16>treeDisplay.getBounds().width)
			c.setLocation((int)treeDisplay.getBounds().width- w, (int)nodeY + 18);  // integer nodeloc approximation
		else
			c.setLocation((int)nodeX + 16, (int)nodeY + 18); // integer nodeloc approximation
		g.setColor(brightGreen);
		g.setXORMode(Color.white);
		GraphicsUtil.drawLine(g,nodeX, nodeY, c.getBounds().x, c.getBounds().y);
		GraphicsUtil.drawLine(g,nodeX, nodeY+1, c.getBounds().x, c.getBounds().y + 1);
		GraphicsUtil.drawLine(g,nodeX, nodeY+2, c.getBounds().x, c.getBounds().y + 2);
		GraphicsUtil.drawLine(g,nodeX, nodeY+3, c.getBounds().x, c.getBounds().y + 3);
		g.setPaintMode();
		g.setColor(Color.black);
		labelDrawn = N;
	}
	/*.................................................................................................................*/
	public void hideOneLabel(int N, Graphics g) {
		if (labelsAtNodes == null)
			return;
		MesquiteLabel c = (MesquiteLabel)labelsAtNodes.getPanel(N);
		if (c==null)
			return;
		c.setVisible(false);
		if (g==null)
			return;
		double nodeX = treeDisplay.getTreeDrawing().x[N];
		double nodeY = treeDisplay.getTreeDrawing().y[N];
		g.setColor(brightGreen);
		g.setXORMode(Color.white);
		GraphicsUtil.drawLine(g,nodeX, nodeY, c.getBounds().x, c.getBounds().y);
		GraphicsUtil.drawLine(g,nodeX, nodeY+1, c.getBounds().x, c.getBounds().y + 1);
		GraphicsUtil.drawLine(g,nodeX, nodeY+2, c.getBounds().x, c.getBounds().y + 2);
		GraphicsUtil.drawLine(g,nodeX, nodeY+3, c.getBounds().x, c.getBounds().y + 3);
		g.setPaintMode();
		labelDrawn = -1;
	}
	/*.................................................................................................................*/
	private void drawLabels(int N, Tree tree, Graphics g) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawLabels(d, tree, g);
		drawOneLabel(N, tree, g);
	}
	/*.................................................................................................................*/
	public void hideLabels() {
		if (labelsAtNodes==null)
			return;
		Graphics g = treeDisplay.getGraphics();
		for (int N = 0; N<labelsAtNodes.getNumNodes(); N++) {
			if (N== labelDrawn)
				hideOneLabel(N, g);
			else
				labelsAtNodes.getPanel(N).setVisible(false);
		}
		if (g!=null)
			g.dispose();
	}
	public void setShowLabels(boolean show) {
		showLabels = show;
		if (labelsAtNodes!=null && !showLabels)
			hideLabels();

	}
	private void makeSureLabelsReady(Tree tree){
		if (labelsAtNodes==null) {
			labelsAtNodes = new LabelsAtNodes(ownerModule, tree.getNumNodeSpaces(), treeDisplay);
		}
		else if (labelsAtNodes.getNumNodes()!=tree.getNumNodeSpaces() ) {
			labelsAtNodes.resetNumNodes(tree.getNumNodeSpaces());
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (showLabels) {
			makeSureLabelsReady(tree);
			drawLabels(drawnRoot, tree, g);
			g.setColor(Color.black);
		}
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	/*.................................................................................................................*/
	public void turnOff() {
		super.turnOff();
		if (labelsAtNodes!=null)
			labelsAtNodes.dispose();
	}
}

/*========================================================*/
class NodeScattergramWindow extends ChartWindow {
	private int windowWidth=0;
	private int windowHeight=0;
	private NodesScattergram ownerModule;
	private NumberArray xArray, yArray, zArray;
	private boolean weightedBranches = true;
	String nameA, nameB;
	Charter charter;
	Tree tree;
	DoubleMiniScroll scrollBox;
	MesquiteChart chart;
	static int numMade = 0;
	int thisMade;

	public NodeScattergramWindow ( NodesScattergram ownerModule) {
		super(ownerModule, true); //infobar
		this.ownerModule = ownerModule;
		numMade++;
		thisMade = numMade;
		charter = ownerModule.charterTask.createCharter(ownerModule);
		charter.setShowNames(true);
		chart=new MesquiteChart(ownerModule, 100, 0, charter);
		setChart(chart);
		xArray = new NumberArray(3);
		yArray = new NumberArray(3);
		zArray = new NumberArray(3);
		chart.deassignChart();
		addToWindow(chart);
		chart.setVisible(true);
		chart.setBackground(getBackground());
		scrollBox = new DoubleMiniScroll(ownerModule.setXCommand, ownerModule.setYCommand, 0,0,0,0,0,0);
		chart.add(scrollBox);
		scrollBox.setYTitle("Y");
		scrollBox.setXTitle("X");
		//scrollBox.setLocation(10, getHeight()-10);
		scrollBox.setLocation(2, getHeight()-24- scrollBox.getBounds().height);
		resetTitle();
	}

	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Nodes Scattergram " + (thisMade));
	}
	/*.................................................................................................................*/
	private void resetScrolls(){
		if (ownerModule.numbersForNodesTaskX instanceof Incrementable){
			Incrementable inc = (Incrementable)ownerModule.numbersForNodesTaskX;
			scrollBox.setXTitle(inc.getItemTypeName());
			int min = (int)inc.toExternal(inc.getMin());
			int max = (int)inc.toExternal(inc.getMax());
			scrollBox.setXValues(min,  (int)inc.toExternal(ownerModule.currentX), max);
			scrollBox.setXVisible(true);
		}
		else {
			scrollBox.setXValues(0,0,0);
			scrollBox.setXVisible(false);
		}
		if (ownerModule.numbersForNodesTaskY instanceof Incrementable){
			Incrementable inc = (Incrementable)ownerModule.numbersForNodesTaskY;
			scrollBox.setYTitle(inc.getItemTypeName());
			int min = (int)inc.toExternal(inc.getMin());
			int max = (int)inc.toExternal(inc.getMax());
			scrollBox.setYValues(min,  (int)inc.toExternal(ownerModule.currentY), max);
			scrollBox.setYVisible(true);
		}
		else {
			scrollBox.setYValues(0,0,0);
			scrollBox.setYVisible(false);
		}
	}
	/*.................................................................................................................*/
	public void setVisible(boolean vis){
		if (vis)
			windowResized();
		super.setVisible(vis);
	}
	/*.................................................................................................................*/
	public void setCharter(Charter charter) {
		charter.setShowNames(true);
		chart.setCharter(charter);
	}
	/*.................................................................................................................*/
	public String[] getStrings(int index) {
		return new String[] {"X (" + ownerModule.numbersForNodesTaskX.getName() + "): " + xArray.toString(index), "Y (" + ownerModule.numbersForNodesTaskY.getName() + "): " + yArray.toString(index)};
	}
	/*.................................................................................................................*/
	public void setTree(Tree tree) {
		this.tree = tree;

	}
	/*.................................................................................................................*/
	public void recalcChart() {
		chart.deassignChart();
		if (ownerModule.numbersForNodesTaskX != null && ownerModule.numbersForNodesTaskY != null) {
			resetScrolls();
			int numNodes = tree.getNumNodeSpaces();
			xArray.resetSize(numNodes);
			yArray.resetSize(numNodes);
			chart.deConstrainMinimumX();
			chart.deConstrainMaximumX();
			chart.deConstrainMinimumY();
			chart.deConstrainMaximumY();
			//zArray.resetSize(numNodes);
			MesquiteNumber resultX = new MesquiteNumber();
			MesquiteNumber resultY = new MesquiteNumber();
			//MesquiteNumber resultZ = new MesquiteNumber();
			if (ownerModule.numbersForNodesTaskX instanceof Incrementable)
				((Incrementable)ownerModule.numbersForNodesTaskX).setCurrent(ownerModule.currentX); 
			xArray.deassignArray();
			yArray.deassignArray();
			ownerModule.numbersForNodesTaskX.calculateNumbers(tree, xArray, null);
			chart.setXAxisName(ownerModule.numbersForNodesTaskX.getNameAndParameters());   //set to correct name
			if (ownerModule.numbersForNodesTaskY instanceof Incrementable)
				((Incrementable)ownerModule.numbersForNodesTaskY).setCurrent(ownerModule.currentY);
			ownerModule.numbersForNodesTaskY.calculateNumbers(tree, yArray, null);
			chart.setYAxisName(ownerModule.numbersForNodesTaskY.getNameAndParameters());  //TODO: this should use string passed back from calculateNumbers
			for (int i=0; i<numNodes; i++) {
				if (tree.nodeInTree(i) && tree.nodeIsInternal(i)) { //todo: have toggle for internal only
					xArray.placeValue(i, resultX);
					yArray.placeValue(i, resultY);
					//zArray.placeValue(i, resultZ);

					int point = chart.addPoint(resultX, resultY);
					//int point = chart.addPoint(resultX, resultY, resultZ);
					String label = tree.getNodeLabel(i); //added 30 Oct 01
					if (label == null)//added 30 Oct 01
						chart.setName(point, Integer.toString(i));
					else
						chart.setName(point, label);//added 30 Oct 01
					//chart.setCategory(point, nodeJustInternal(tree, i));
				}
				else { //these are here to ensure number of points is in parallel with numbering of nodes, for selection and other purposes
					resultX.setToUnassigned();
					int point = chart.addPoint(resultX, resultX);
				}
			}
			contentsChanged();
			chart.munch();
		}
		ownerModule.treeDisplay.pleaseUpdate(false);

	}
	/*.................................................................................................................*/
	public void pointTouched(Tree tree, int which) {
		if (ownerModule.nsLabeller==null)
			return;
		int count = 0;
		Graphics g = ownerModule.treeDisplay.getGraphics();
		if (g!=null) {
			for (int i=0; i<tree.getNumNodeSpaces(); i++) {
				if (tree.nodeInTree(i) && tree.nodeIsInternal(i)) {
					if (count == which) {
						ownerModule.nsLabeller.drawOneLabel(i, tree, g);
					}
					count++;
				}
			}
			g.dispose();
		}

	}
	/*.................................................................................................................*/
	public void pointReleased(Tree tree, int which) {
		if (ownerModule.nsLabeller.showLabels)
			return;
		int count = 0;
		Graphics g = ownerModule.treeDisplay.getGraphics();
		if (g!=null) {
			for (int i=0; i<tree.getNumNodeSpaces(); i++) {
				if (tree.nodeInTree(i) && tree.nodeIsInternal(i)) {
					if (count == which) {
						ownerModule.nsLabeller.hideOneLabel(i, g);
					}
					count++;
				}
			}
			g.dispose();
		}
		ownerModule.nsLabeller.hideLabels();
	}

	public void windowResized() {
		super.windowResized();
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (chart!=null && (windowWidth!=getWidth() || windowHeight!=getHeight())) {
			windowWidth=getWidth();
			windowHeight=getHeight();
			chart.setLocation(0, 20);
			chart.setChartSize(windowWidth-20, windowHeight-20);
			scrollBox.setLocation(2, getHeight()-24- scrollBox.getBounds().height);
		}

		MesquiteWindow.uncheckDoomed(this);
	}
}



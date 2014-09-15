/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.NodeLocs2DPlot;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NodeLocs2DPlot extends NodeLocsPlot {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(NumbersForNodesIncr.class, getName() + " needs a method to calculate the X,Y values used to plot the tree.",
		"You can choose the values to calculate either initially or in the Node Values submenu.");
	}
	
	Point[] location;
	NumbersForNodesIncr numbersForNodesTask;
	int margin = 40;
	MesquiteString numberTaskName;
	MesquiteNumber tempNum;
	boolean veryFirstTime=true;
	int initialOffsetX = MesquiteInteger.unassigned;
	int initialOffsetY = MesquiteInteger.unassigned;
	Vector extras;
	public MesquiteBoolean showLegend;
	int currentX = 0;
	int currentY=1;
	String xString, yString;
	boolean hide = false;
	MesquiteCommand nfntC;
	MesquiteNumber xNumber = new MesquiteNumber();
	MesquiteNumber yNumber = new MesquiteNumber();

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
   		extras = new Vector();
		numbersForNodesTask= (NumbersForNodesIncr)hireEmployee(NumbersForNodesIncr.class, "Values to calculate for axes for Plot Tree");
		if (numbersForNodesTask == null )
			return sorry(getName() + " couldn't start because no modules calculating numbers for nodes obtained.");
		nfntC = makeCommand("setAxis",  this);
		numbersForNodesTask.setHiringCommand(nfntC);
		showLegend = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Show Plot Tree Legend", makeCommand("toggleShowLegend",  this), showLegend);
		makeMenu("Plot");
		numberTaskName = new MesquiteString(numbersForNodesTask.getName());
		if (numModulesAvailable(NumbersForNodesIncr.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Node Values", nfntC, NumbersForNodesIncr.class);
			mss.setSelected(numberTaskName);
		}
		tempNum = new MesquiteNumber();
		MesquiteSubmenuSpec xsub = addSubmenu(null, "X Axis");
		addItemToSubmenu(null, xsub, "Next", makeCommand("nextX",  this));
		addItemToSubmenu(null, xsub, "Previous", makeCommand("previousX",  this));
		addItemToSubmenu(null, xsub, "Choose", makeCommand("setX",  this));
		
		MesquiteSubmenuSpec ysub = addSubmenu(null, "Y Axis");
		addItemToSubmenu(null, ysub, "Next", makeCommand("nextY",  this));
		addItemToSubmenu(null, ysub, "Previous", makeCommand("previousY",  this));
		addItemToSubmenu(null, ysub, "Choose", makeCommand("setY",  this));
  		return true;
  	 }
  	 
  	/*.................................................................................................................*/
	 
   	public void endJob(){
   		hide = true;
  	 	if (extras!=null) {
	   		for (int i=0; i<extras.size(); i++){
	   			TreeDisplayExtra extra = (TreeDisplayExtra)extras.elementAt(i);
		   		if (extra!=null){
		   			TreeDisplay td = extra.getTreeDisplay();
		   			extra.turnOff();
		   			if (td!=null)
		   				td.removeExtra(extra);
		   		}
	   		}
	   		extras.removeAllElements();
  	 	}
   		super.endJob();
   	}
   	public boolean isSubstantive(){
   		return true;
   	}
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
 	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
  	 	if (extras!=null) {
	   		for (int i=0; i<extras.size(); i++){
	   			NodeLocs2DPlotExtra extra = (NodeLocs2DPlotExtra)extras.elementAt(i);
		   		if (extra!=null){
 					extra.pleaseAdjustScrolls=true;
		   		}
	   		}
  	 	}
    	 	parametersChanged(notification);
   	 }
   	 public NodeLocs2DPlotExtra getFirstExtra(){
  	 	if (extras!=null) {
	   		for (int i=0; i<extras.size(); i++){
	   			NodeLocs2DPlotExtra extra = (NodeLocs2DPlotExtra)extras.elementAt(i);
		   		if (extra!=null){
 					return extra;
		   		}
	   		}
  	 	}
  	 	return null;
   	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine( "hide");
  	 	temp.addLine( "setAxis " , numbersForNodesTask);
  	 	temp.addLine("toggleShowLegend " + showLegend.toOffOnString());
  	 	if (getFirstExtra()!=null && getFirstExtra().legend!=null) {
  	 		temp.addLine("setInitialOffsetX " + getFirstExtra().legend.getOffsetX()); //Should go operator by operator!!!
  	 		temp.addLine("setInitialOffsetY " + getFirstExtra().legend.getOffsetY());
  	 	}
   	 	temp.addLine( "setCurrentX " + CharacterStates.toExternal(currentX));
  	 	temp.addLine( "setCurrentY " + CharacterStates.toExternal(currentY));
 	 	temp.addLine( "show");
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the module that calculates coordinates for the nodes", "[name of module]", commandName, "setAxis")) {
    	 		NumbersForNodesIncr temp =  (NumbersForNodesIncr)replaceEmployee(NumbersForNodesIncr.class, arguments, "Value for axes", numbersForNodesTask);
 			if (temp!=null) {
 				numbersForNodesTask = temp;
				numbersForNodesTask.setHiringCommand(nfntC);
				numberTaskName.setValue(numbersForNodesTask.getName());
				resetContainingMenuBar();
 				parametersChanged();
 			}
 			return temp;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Hides the plot", null, commandName, "hide")) {
    	 		hide = true;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Shows the plot", null, commandName, "show")) {
    	 		hide = false;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the current item displayed on the x axis (used by scripting, before display)", "[number of item]", commandName, "setCurrentX")) {
    	 		int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
    	 			currentX = ic;
    	 			numbersForNodesTask.setCurrent(ic);
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the current item displayed on the y axis (used by scripting, before display)", "[number of item]", commandName, "setCurrentY")) {
    	 		int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
    	 			currentY = ic;
    	 			numbersForNodesTask.setCurrent(ic);
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the current item displayed on the x axis", "[number of item]", commandName, "setX")) {
    	 		int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
    	 		if (!MesquiteInteger.isCombinable(ic))
    	 			ic = MesquiteInteger.queryInteger(containerOfModule(), "Choose item (X axis)", "Item to map", 1);
    	 		if (MesquiteInteger.isCombinable(ic) && (ic>=numbersForNodesTask.getMin()) && (ic<=numbersForNodesTask.getMax())) {
    	 			currentX = ic;
    	 			numbersForNodesTask.setCurrent(ic);
		  	 	adjustScrolls();
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the current item displayed on the y axis", "[number of item]", commandName, "setY")) {
    	 		int ic = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
    	 		if (!MesquiteInteger.isCombinable(ic))
    	 			ic = MesquiteInteger.queryInteger(containerOfModule(), "Choose item (Y axis)", "Item to map", 1);
    	 		if (MesquiteInteger.isCombinable(ic) && (ic>=numbersForNodesTask.getMin()) && (ic<=numbersForNodesTask.getMax())) {
    	 			currentY = ic;
    	 			numbersForNodesTask.setCurrent(ic);
		  	 	adjustScrolls();
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to next item for X axis", null, commandName, "nextX")) {
    	 		if (currentX<numbersForNodesTask.getMax()) {
    	 			currentX++;
    	 			numbersForNodesTask.setCurrent(currentX);
		  	 	adjustScrolls();
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to next item for Y axis", null, commandName, "nextY")) {
    	 		if (currentY<numbersForNodesTask.getMax()) {
    	 			currentY++;
    	 			numbersForNodesTask.setCurrent(currentY);
		  	 	adjustScrolls();
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to previous item for X axis", null, commandName, "previousX")) {
    	 		if (currentX>numbersForNodesTask.getMin()) {
    	 			currentX--;
    	 			numbersForNodesTask.setCurrent(currentX);
		  	 	adjustScrolls();
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to previous item for Y axis", null, commandName, "previousY")) {
    	 		if (currentY>numbersForNodesTask.getMin()) {
    	 			currentY--;
    	 			numbersForNodesTask.setCurrent(currentY);
		  	 	adjustScrolls();
    	 			if (!MesquiteThread.isScripting())
    	 				parametersChanged();
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the initial horizontal offset from home position for the legend", "[offset in pixels]", commandName, "setInitialOffsetX")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;

    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the initial vertical offset from home position for the legend", "[offset in pixels]", commandName, "setInitialOffsetY")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetY = offset;
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether or not to show the legend", "[on or off]", commandName, "toggleShowLegend")) {
    	 		showLegend.toggleValue(parser.getFirstToken(arguments));
	  	 	if (extras!=null) {
		   		for (int i=0; i<extras.size(); i++){
		   			NodeLocs2DPlotExtra extra = (NodeLocs2DPlotExtra)extras.elementAt(i);
			   		if (extra!=null && extra.legend!=null){
	 					extra.legend.setVisible(showLegend.getValue());
			   		}
		   		}
	  	 	}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	 private void adjustScrolls(){
  	 	if (extras!=null) {
	   		for (int i=0; i<extras.size(); i++){
	   			NodeLocs2DPlotExtra extra = (NodeLocs2DPlotExtra)extras.elementAt(i);
		   		if (extra!=null && extra.legend!=null){
 					extra.legend.adjustScrolls();
		   		}
	   		}
  	 	}
   	 }
    	 public String getName() {
		return "Node Locations (2D plot)";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the node locations for a tree plotted in a two dimensional space." ;
   	 }
	/*_________________________________________________*/
   	public boolean compatibleWithOrientation(int orientation) {
   		return false;
   	}
 	/*_________________________________________________*/
  	boolean first=true;
	MesquiteNumber minX, maxX, minY, maxY;

	private void surveyValues (Tree tree, int node, NumberArray numbersX, NumberArray numbersY, MesquiteBoolean illegalValue){
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			surveyValues(tree, d, numbersX, numbersY, illegalValue);
		numbersX.placeValue(node, xNumber);
		numbersY.placeValue(node, yNumber);
		if (!xNumber.isCombinable() || !yNumber.isCombinable())
			illegalValue.setValue(true);
		if (first) {
			maxX.setValue(xNumber);
			minX.setValue(xNumber);
			maxY.setValue(yNumber);
			minY.setValue(yNumber);
			first = false;
		}
		else {
			maxX.setMeIfIAmLessThan(xNumber);
			minX.setMeIfIAmMoreThan(xNumber);
			maxY.setMeIfIAmLessThan(yNumber);
			minY.setMeIfIAmMoreThan(yNumber);
		}
	}
	/*_________________________________________________*/
	private void calcNodeLocs (Tree tree, int node, Rectangle rect, NumberArray numbersX, NumberArray numbersY){
			if (location==null|| xNumber==null|| yNumber==null)
				return;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				calcNodeLocs(tree, d, rect, numbersX, numbersY);
			numbersX.placeValue(node, xNumber);
			numbersY.placeValue(node, yNumber);
			if (node>= location.length ||location[node] == null)
				return;
			if (!xNumber.isCombinable() || !yNumber.isCombinable()){
				location[node].x = MesquiteInteger.unassigned;
				location[node].y = MesquiteInteger.unassigned;
			}
			else {
				location[node].x = xNumber.setWithinBounds(minX, maxX, rect.width - 2*margin) /*+ rect.x*/+ margin;
				location[node].y = yNumber.setWithinBounds(minY, maxY, rect.height - 2*margin) /*+ rect.y*/ + margin;
			}
	}
	
 	/*.................................................................................................................*/
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis").*/
	public String purposeOfEmployee(MesquiteModule employee){
		if (employee == numbersForNodesTask)
			return "for axes";
		else
			return "";
	}
	int rectWidth, rectHeight;
	/*_________________________________________________*/
	public void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Rectangle rect) {
		if (hide || isDoomed())
			return;
		if (MesquiteTree.OK(tree)) {
				if (treeDisplay == null || treeDisplay.getTreeDrawing() == null)
					return;
				if (veryFirstTime) {
					veryFirstTime=false;
					numbersForNodesTask.initialize(tree);
				}
				NodeLocs2DPlotExtra extra = null;
				if (treeDisplay.getExtras() !=null) {
					if (treeDisplay.getExtras().myElements(this)==null) {  
						extra = new NodeLocs2DPlotExtra(this, treeDisplay); 
						treeDisplay.addExtra(extra); 
						extras.addElement(extra);
					}
					else {
						Listable[] mine = treeDisplay.getExtras().myElements(this);
						if (mine !=null && mine.length>0)
							extra =(NodeLocs2DPlotExtra) mine[0];
					}
				}
				
				NumberArray numbersX= new NumberArray(tree.getNumNodeSpaces());
				NumberArray numbersY=new NumberArray(tree.getNumNodeSpaces());
				numbersForNodesTask.setCurrent(currentX);
				numbersForNodesTask.calculateNumbers(tree, numbersX, null);
				
				xString = numbersForNodesTask.getNameAndParameters();

				numbersForNodesTask.setCurrent(currentY);
				numbersForNodesTask.calculateNumbers(tree, numbersY, null);
				yString = numbersForNodesTask.getNameAndParameters();
				extra.textPositions.setLength(0);
				nodePositions(tree.getRoot(),  tree, extra.textPositions,  numbersX,  numbersY);
				if (extra!=null)
					extra.parameters = numbersForNodesTask.getParameters();
				first = true;
				int subRoot = tree.motherOfNode(drawnRoot);
				location = new Point[tree.getNumNodeSpaces()];
				for (int i=0; i<location.length; i++) {
					location[i]= new Point();
				}
				minX = new MesquiteNumber();
				maxX = new MesquiteNumber();
				minY = new MesquiteNumber();
				maxY = new MesquiteNumber();
				MesquiteBoolean illegalValue = new MesquiteBoolean(false);
				surveyValues(tree, drawnRoot, numbersX, numbersY, illegalValue); //check for illegal values added 13 Dec 01
				if (illegalValue.getValue()) {
					/*for (int i=0; i<tree.getNumNodeSpaces() && i<treeDisplay.getTreeDrawing().y.length; i++) {
						treeDisplay.getTreeDrawing().y[i] = 0;
						treeDisplay.getTreeDrawing().x[i] = 0;
						treeDisplay.getTreeDrawing().z[i] = 0;
					}*/
					if (extra!=null)
						extra.addWarning(true);
					//return;
				}
				else if (extra!=null)
					extra.addWarning(false);
				calcNodeLocs (tree, drawnRoot, rect, numbersX, numbersY);
				location[subRoot].x = location[drawnRoot].x;
				location[subRoot].y = location[drawnRoot].y;
				for (int i=0; i<tree.getNumNodeSpaces() && i<treeDisplay.getTreeDrawing().y.length; i++) {
					treeDisplay.getTreeDrawing().y[i] = location[i].y;
					treeDisplay.getTreeDrawing().x[i] = location[i].x;
				}
				this.rectWidth = rect.width;
				this.rectHeight = rect.height;
		}
	}
	
	/*.................................................................................................................*/
	public void nodePositions(int N,  Tree tree, StringBuffer sb, NumberArray numbersX, NumberArray numbersY) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				nodePositions(d, tree, sb, numbersX, numbersY);
		if (tree.nodeIsTerminal(N))
			sb.append(tree.getTaxa().getName(tree.taxonNumberOfNode(N)) + '\t');
		else
			sb.append("node " + N +  '\t');
		sb.append(numbersX.toString(N) + '\t' + numbersY.toString(N) + "\n");
	}
	
	private void drawString(Graphics g, String s, int x, int y){
		if (g == null || StringUtil.blank(s))
			return;
		try {
			g.drawString(s, x, y);
		}
		catch (Exception e){
		}
	}
	StringInABox xAxisWriter, yAxisWriter;
	public void drawGrid (Graphics g, int x, int y, int width, int height, TreeDisplay treeDisplay) {
		if (hide)
			return;
		if (minX == null || maxX == null || minY == null || maxY == null)
			return;
		boolean rulerOnly = false;
		int rulerWidth = 8;
		Color c=g.getColor();
		g.setColor(Color.cyan);
		int buffer = 8;
		
		double min, max;
		int minTicks = 20;
		
		
		//VERTICAL LINES
		min = minX.getDoubleValue();
		max = maxX.getDoubleValue();
		double firstTick = MesquiteDouble.firstScaleMark(max-min, min, minTicks);
		double tickIncrement = MesquiteDouble.scaleIncrement(max-min, min, minTicks);
		int t = 0;
		int bottomEdge = y+height;
		if (tickIncrement>0 && (max-firstTick)/tickIncrement>1000)
			tickIncrement = 0; //too dense; set this to pop out
		for (double tick=firstTick; tick<max && tickIncrement>0; tick += tickIncrement) {
			if (t % 10 == 0)
				g.setColor(Color.blue);
			else
				g.setColor(Color.cyan);
			tempNum.setValue(tick);
			int xPos = x+tempNum.setWithinBounds(minX, maxX, width);
			if (rulerOnly)
				g.drawLine(xPos, bottomEdge,  xPos,  bottomEdge-rulerWidth);
			else
				g.drawLine(xPos, bottomEdge,  xPos,  y);

			if (t++ % 10 == 0)
				drawString(g, MesquiteDouble.toString(tick), xPos, bottomEdge + buffer);
		}

		
		//HORIZONTAL LINES
		min = minY.getDoubleValue();
		max = maxY.getDoubleValue();
		firstTick = MesquiteDouble.firstScaleMark(max-min, min, minTicks);
		tickIncrement = MesquiteDouble.scaleIncrement(max-min, min, minTicks);
		t = 0;
		int rightEdge = x+width;
		if (tickIncrement>0 && (max-firstTick)/tickIncrement>1000)
			tickIncrement = 0; //too dense; set this to pop out
		for (double tick=firstTick; tick<max && tickIncrement>0; tick += tickIncrement) {
			if (t % 10 == 0)
				g.setColor(Color.blue);
			else
				g.setColor(Color.cyan);
			tempNum.setValue(tick);
			int yPos = y+tempNum.setWithinBounds(minY, maxY, height);
			if (rulerOnly)
				g.drawLine(rightEdge,  yPos,  rightEdge-rulerWidth,yPos);
			else
				g.drawLine(rightEdge,  yPos,  x, yPos);

			if (t++ % 10 == 0)
				drawString(g, MesquiteDouble.toString(tick), rightEdge + buffer, yPos);
		}
		g.setColor(Color.blue);
		
		if (xAxisWriter==null)
			xAxisWriter = new StringInABox(xString, g.getFont(), rightEdge-x);
		else {
			xAxisWriter.setWidth(rightEdge-x);
			xAxisWriter.setString(xString);
			xAxisWriter.setFont(g.getFont());
		}
		xAxisWriter.draw(g,x, y+height+10); 

		if (yAxisWriter==null)
			yAxisWriter = new StringInABox(yString, g.getFont(), height);
		else {
			yAxisWriter.setWidth(height);
			yAxisWriter.setString(yString);
			yAxisWriter.setFont(g.getFont());
		}
		yAxisWriter.draw(g,x-46, y+height, 0, rightEdge, treeDisplay, false); 
		if (c!=null) g.setColor(c);
	}
}



class NodeLocs2DPlotExtra extends TreeDisplayBkgdExtra {
	public NodeLocs2DPlotLegend legend;
	NodeLocs2DPlot locsModule;
	public boolean pleaseAdjustScrolls = false;
	public String parameters = "";
	StringBuffer textPositions;

	public NodeLocs2DPlotExtra (NodeLocs2DPlot ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		locsModule = ownerModule;
		textPositions = new StringBuffer();
	}
	boolean doWarn = false;
	public void addWarning(boolean warn){
		if (legend!=null)
			legend.addWarning(warn);
		doWarn = warn;
	}
	/*.................................................................................................................*/
	public   String infoAtNodes(Tree tree, int drawnRoot) {
		return parameters + "\n\nNodes with X, Y positions\n\n" + textPositions.toString();
	}
	/*.................................................................................................................*/
	public   String additionalText(Tree tree, int drawnRoot) {
		return parameters;
	}
	/*.................................................................................................................*/
	boolean legendMade = false;
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		locsModule.drawGrid(g, locsModule.margin/*+rect.x*/, locsModule.margin/*+rect.y*/, locsModule.rectWidth-2*locsModule.margin, locsModule.rectHeight-2*locsModule.margin, treeDisplay);
		if (!legendMade && legend == null) {
			legendMade= true;
			legend = new NodeLocs2DPlotLegend(locsModule, this);
		 	legend.adjustScrolls();
			legend.setVisible(locsModule.showLegend.getValue());
			legend.addWarning(doWarn);
			addPanelPlease(legend);
		}
		else if (pleaseAdjustScrolls && legend!=null){
			pleaseAdjustScrolls = false;
		 	legend.adjustScrolls();
		}
		if (legend!=null)
			legend.adjustLocation();
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	
	public void turnOff() {
		if (treeDisplay!=null && legend!=null)
			removePanelPlease(legend);
		super.turnOff();
	}
}
	
/* ======================================================================== */
class NodeLocs2DPlotLegend extends TreeDisplayLegend {
	private NodeLocs2DPlot ownerModule;
	public MiniScroll xScroll = null;
	public MiniScroll yScroll = null;
	private NodeLocs2DPlotExtra pD;
	private static final int defaultLegendWidth=180;
	private static final int defaultLegendHeight=220;
	private int oldX=-1;
	private int oldMaxX=0;
	private int oldY=-1;
	private int oldMaxY=0;
	public NodeLocs2DPlotLegend(NodeLocs2DPlot ownerModule, NodeLocs2DPlotExtra pD) {
		super(pD.treeDisplay,defaultLegendWidth, defaultLegendHeight);
		this.pD = pD;
		this.ownerModule = ownerModule;
		setLayout(null);
		setOffsetX(ownerModule.initialOffsetX);
		setOffsetY(ownerModule.initialOffsetY);
		
		yScroll = new MiniScroll(MesquiteModule.makeCommand("setCurrentY",  ownerModule), false, false, 1, 1, 1,"");
		add(yScroll);
		yScroll.setLocation(2,2);
		yScroll.setColor(Color.blue);
		
		xScroll = new MiniScroll(MesquiteModule.makeCommand("setCurrentX",  ownerModule), false, 1, 1, 1,"");
		add(xScroll);
		xScroll.setLocation(yScroll.getBounds().x+yScroll.getBounds().width, yScroll.getBounds().height-16);
		xScroll.setColor(Color.blue);

		setSize(xScroll.getBounds().x+xScroll.getBounds().width+1,xScroll.getBounds().y+xScroll.getBounds().height +8);
	}
	
	public void adjustScrolls() {
		Incrementable inc = ownerModule.numbersForNodesTask;
		xScroll.setVisible(true);
		int max = (int)inc.getMax();
		if (oldMaxX != max) {
			xScroll.setMaximumValue(CharacterStates.toExternal(max));
			oldMaxX = max;
		}
		if (oldX != ownerModule.currentX) {
			xScroll.setCurrentValue(CharacterStates.toExternal(ownerModule.currentX));
			oldX = ownerModule.currentX;
		}
		yScroll.setVisible(true);
		if (oldMaxY != max) {
			yScroll.setMaximumValue(CharacterStates.toExternal(max));
			oldMaxY = max;
		}
		if (oldY != ownerModule.currentY) {
			yScroll.setCurrentValue(CharacterStates.toExternal(ownerModule.currentY));
			oldY =ownerModule.currentY;
		}
		repaint();
		
	}
	boolean doWarn = false;
	public void addWarning(boolean warn){
		doWarn = warn;
	}
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (xScroll !=null)
			xScroll.setVisible(b);
		if (yScroll !=null)
			yScroll.setVisible(b);
		repaint();
	}
	
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		setSize(xScroll.getBounds().x+xScroll.getBounds().width+1,yScroll.getBounds().y+yScroll.getBounds().height +8);
		Color c = g.getColor();
		g.setColor(Color.blue);
		g.drawString("Plot Tree", yScroll.getBounds().x+yScroll.getBounds().width+8, 20);
		if (doWarn) 
			g.drawString("MISSING VALUES", yScroll.getBounds().x+yScroll.getBounds().width+8, 36); //added 13 Dec 01
		g.drawRect(0,0, getBounds().width-1, getBounds().height -1);
		Rectangle rect = xScroll.getBounds();
		g.drawString("X", rect.x+ (rect.width)/2, rect.y -1);
		rect = yScroll.getBounds();
		g.drawString("Y", rect.x+ rect.width, rect.y + rect.height/2);
		if (c!=null) g.setColor(c);
	   	super.paint(g);
		MesquiteWindow.uncheckDoomed(this);
	}
	
	
}



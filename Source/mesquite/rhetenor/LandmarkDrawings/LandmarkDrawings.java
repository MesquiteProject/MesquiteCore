/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.rhetenor.LandmarkDrawings;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;
import mesquite.rhetenor.lib.*;

/* ======================================================================== */
//todo: move plots via offset
public class LandmarkDrawings extends TreeDisplayAssistantMA implements LegendHolder {
	public String getName() {
		return "Landmark Drawings";
	}

	public String getExplanation() {
		return "Places drawing of landmarks at each node in tree." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharsStatesForNodes.class, getName() + "  needs a method to reconstruct ancestral landmarks.",
		"The method to reconstruct landmarks is selected initially");
		EmployeeNeed e2 = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of character matrices.",
		"The source of character matrices is selected initially");
	}
	public void getSubfunctions(){
		registerSubfunction(new FunctionExplanation("Show/hide Drawing", "(A tool of the Tree Window when Landmark Drawings is shown) Shows or hides the drawing at the node touched", null, getPath() +"landmark.gif"));
		super.getSubfunctions();
	}
	/*.................................................................................................................*/
	private Vector plotters;
	CharsStatesForNodes allCharsTask;
	MatrixSourceCoord characterSourceTask;
	int drawingWidth = 100;
	int drawingHeight = 60;
	int firstItem = 0;
	int secondItem = 1;
	String[] itemNames;
	int initialOffsetX=MesquiteInteger.unassigned;
	int initialOffsetY= MesquiteInteger.unassigned;
	MesquiteBoolean joinLastToFirst = new MesquiteBoolean(true);

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		plotters = new Vector();
		characterSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, ContinuousState.class,"Source of characters");
		if (characterSourceTask == null){
			return sorry(getName() + " couldn't start because no suitable source of characters was obtained");
		}
		//TODO: allow choices & use setHIringCommand
		allCharsTask = (CharsStatesForNodes)hireEmployee(CharsStatesForNodes.class, "Reconstruction method");
		if (allCharsTask == null){
			return sorry(getName() + " couldn't start because a suitable reconstructor of ancestral states was not obtained");
		}
		makeMenu("Landmarks");
		addMenuItem("Choose Items...", makeCommand("setItems",  this));
		addMenuItem("Drawing Sizes...", makeCommand("sizeDrawing",  this));
		addCheckMenuItem(null, "Join last to first", MesquiteModule.makeCommand("toggleLastToFirst",  this), joinLastToFirst);
		addMenuItem("Close Landmark Drawings", makeCommand("close",  this));
		addMenuSeparator();
		return true;
	}

	public String getExpectedPath(){
		return getPath() + "landmark.gif";
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("sizeDrawing " + drawingWidth + " " + drawingHeight);
		PlotOperator tco = (PlotOperator)plotters.elementAt(0);
		if (tco!=null && tco.legend!=null) {
			temp.addLine("setInitialOffsetX " + tco.legend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetY " + tco.legend.getOffsetY());
		}
		temp.addLine("setItems " + firstItem + " " + secondItem);
		temp.addLine("toggleLastToFirst " + joinLastToFirst.toOffOnString());
		temp.addLine("getCharacterSource ", characterSourceTask);
		temp.addLine("getReconstructor ", allCharsTask);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns source of matrices for Landmark Drawings", null, commandName, "getCharacterSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Returns reconstructor module for Landmark Drawings", null, commandName, "getReconstructor")) {
			return allCharsTask;
		}
		else if (checker.compare(this.getClass(), "Returns the source of matrices on which to do Landmark Drawings", null, commandName, "setCharacterSource")) { //TEMPORARY while old files are still around
			if (characterSourceTask != null)
				return characterSourceTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Sets initial horizontal offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetX")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;
				Enumeration e = plotters.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof PlotOperator) {
						PlotOperator tCO = (PlotOperator)obj;
						if (tCO.legend!=null)
							tCO.legend.setOffsetX(offset);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets initial vertical offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetY")) {
			MesquiteInteger pos = new MesquiteInteger();
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetY = offset;
				Enumeration e = plotters.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof PlotOperator) {
						PlotOperator tCO = (PlotOperator)obj;
						if (tCO.legend!=null)
							tCO.legend.setOffsetY(offset);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the size of the landmark drawings", "[width in pixels] [height in pixels]", commandName, "sizeDrawing")) {
			pos.setValue(0);
			int w = MesquiteInteger.fromString(arguments, pos);
			int h = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(h)  && MesquiteInteger.isCombinable(w)) {
				if (w>10 && h>10) {
					drawingWidth = w;
					drawingHeight = h;
					resizeAllPlotOperators();
				}
			}
			else { 
				MesquiteBoolean answer = new MesquiteBoolean(false);
				MesquiteInteger newWidth = new MesquiteInteger(drawingWidth);
				MesquiteInteger newHeight =new MesquiteInteger(drawingHeight);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Size of drawings ", "Width (Pixels)",  "Height (Pixels)", answer,  newWidth, newHeight,10,MesquiteInteger.unassigned,10, MesquiteInteger.unassigned,MesquiteString.helpString);
				if (answer.getValue() && newWidth.getValue()>10 && newHeight.getValue()>10) {
					drawingWidth = newWidth.getValue();
					drawingHeight = newHeight.getValue();
					resizeAllPlotOperators();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the size of the landmark drawings", "[x item number] [y item number]", commandName, "setItems")) {
			pos.setValue(0);
			int x = MesquiteInteger.fromString(arguments, pos);
			int y = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(x)  && MesquiteInteger.isCombinable(y)) {
				if ((itemNames == null || x<itemNames.length) && (itemNames == null || y<itemNames.length)){
					firstItem = x;
					secondItem = y;
					resizeAllPlotOperators();
					parametersChanged();
				}
				return null;
			}
			else if (itemNames == null || itemNames.length <2){
				discreetAlert( "Sorry, you cannot choose items for Landmark Drawings because a matrix has not yet been chosen or it has fewer than two items");
				return null;
			}
			else { 
				int d = ListDialog.queryList(containerOfModule(), "X axis", "Choose item for X axis of Landmark Drawings:", MesquiteString.helpString, itemNames, firstItem);
				if (!MesquiteInteger.isCombinable(d))
					return null;
				if (d<0 || d>=itemNames.length)
					d = 0;
				int dX = d;
				while (d == dX){
					d = ListDialog.queryList(containerOfModule(), "Y axis", "Choose item for Y axis of Landmark Drawings:", MesquiteString.helpString, itemNames, secondItem);
				}
				if (!MesquiteInteger.isCombinable(d))
					return null;
				if (d<0 || d>=itemNames.length) {
					if (dX == 0)
						d = 1;
					else
						d = 0;
				}
				firstItem = dX;
				secondItem =d;
				resizeAllPlotOperators();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to draw a line linking the last to the first landmark", "[on or off]", commandName, "toggleLastToFirst")) {
			joinLastToFirst.toggleValue(parser.getFirstToken(arguments));
			resizeAllPlotOperators();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Turn off the landmark drawings", null, commandName, "close")) {
			iQuit();
			resetContainingMenuBar();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		PlotOperator newPlotter = new PlotOperator(this, treeDisplay);
		plotters.addElement(newPlotter);
		return newPlotter;
	}
	/*.................................................................................................................*/
	public void resizeAllPlotOperators() {
		Enumeration e = plotters.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PlotOperator) {
				PlotOperator tCO = (PlotOperator)obj;
				tCO.resize();
			}
		}
	}
	/*.................................................................................................................*/
	public void closeAllPlotOperators() {
		Enumeration e = plotters.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PlotOperator) {
				PlotOperator tCO = (PlotOperator)obj;
				tCO.turnOff();
			}
		}
	}
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		recalcAllPlotOperators();
	}
	/*.................................................................................................................*/
	public void recalcAllPlotOperators() {
		Enumeration e = plotters.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PlotOperator) {
				PlotOperator tCO = (PlotOperator)obj;
				tCO.recalculate();
				if (tCO.getTreeDisplay()!=null)
					tCO.getTreeDisplay().pleaseUpdate(false);
			}
		}
	}
	public boolean showLegend(){
		return true;
	}
	public int getInitialOffsetX(){
		return initialOffsetX;
	}
	public int getInitialOffsetY(){
		return initialOffsetY;
	}
	public void endJob() {
		closeAllPlotOperators();
		super.endJob();
	}

	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
}

/* ======================================================================== */
class PlotOperator extends TreeDisplayDrawnExtra implements Commandable {
	private DrawingsAtNodes plotsAtNodes;
	private MesquiteNumber resultX, resultY;
	private MesquiteNumber minX, minY, maxX, maxY;
	private LandmarkDrawings plotModule;
	private Tree tree;
	MContinuousHistory charsStates=null;
	TreeTool showTool;
	boolean first = true;
	LandmarkLegend legend;
	MesquiteString resultString = new MesquiteString("");
	MesquiteString messageString = new MesquiteString("");

	public PlotOperator (LandmarkDrawings ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		plotModule = ownerModule;
		showTool = new TreeTool(this, "ShowDrawing", ownerModule.getPath(), "landmark.gif", 5,2,"Show or hide drawing", "This tool is used to show or hide Landmark Drawings at nodes.");
		showTool.setTouchedCommand(MesquiteModule.makeCommand("toggleShow",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(showTool);
		}
	}
	MesquiteInteger pos = new MesquiteInteger();
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Toggles whether or not the landmark drawing at a node is displayed", "[node number]", commandName, "toggleShow")) {
			int i = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(i))
				plotsAtNodes.toggleShowPanel(i);

		}
		/* *  	 	
		else return  super.doCommand(commandName, arguments, checker);

    	 /**/
		return null;
	}
	/*.................................................................................................................*/
	public   void checkPlotStorage(Tree tree){
		if (tree!=null) {
			if (plotsAtNodes==null) {
				plotsAtNodes = new DrawingsAtNodes(plotModule, tree.getNumNodeSpaces(), treeDisplay, this);
				resize();
			}
			else if (plotsAtNodes.getNumNodes()!=tree.getNumNodeSpaces()) {
				plotsAtNodes.resetNumNodes(tree.getNumNodeSpaces());
			}
		}
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree){
		this.tree = tree;
		recalculate();

	}
	/*.................................................................................................................*/
	public   void recalculate(){
		if (legend==null) {
			legend = new LandmarkLegend(plotModule, treeDisplay, messageString, "Landmark Drawings", Color.black);
			addPanelPlease(legend);
			legend.setVisible(true);
		}

		//check for missing and fail if missing?
		Object o = plotModule.allCharsTask.calculateStates(tree, plotModule.characterSourceTask.getCurrentMatrix(tree), charsStates, resultString);
		if (!(o instanceof MContinuousHistory))
			return;
		charsStates = (MContinuousHistory)o;
		messageString.setValue("Ancestral forms reconstructed by: " + plotModule.allCharsTask.getNameAndParameters() + "\n\nCharacters from: " + plotModule.characterSourceTask.getNameAndParameters());
		if (charsStates == null)
			return;
		messageString.append("\n" + resultString);
		checkPlotStorage(tree);
		String[] items = new String[charsStates.getNumItems()];
		for (int i=0; i<items.length; i++){
			items[i]= charsStates.getItemName(i);
		}
		if (charsStates.getNumItems()==1){
			if (first)
				plotModule.discreetAlert( "Sorry, the drawings cannot be shown properly because the characters supplied don't have two components (e.g. x and y components");
			first = false;
			plotModule.itemNames = null;
			plotsAtNodes.hideAllPanels();
			messageString.setValue("Drawings cannot be shown properly because the characters supplied don't have two components (e.g. x and y components");
		}
		else {
			//harvest item names for later use in user choice
			plotModule.itemNames = new String[charsStates.getNumItems()];
			for (int i=0; i<plotModule.itemNames.length; i++){
				plotModule.itemNames[i]= charsStates.getItemName(i);
				if (plotModule.itemNames[i] == null)
					plotModule.itemNames[i] = "#" + (i+1) + " (unnamed)";
			}
			plotsAtNodes.setCharsStates(charsStates, tree, plotModule.firstItem, plotModule.secondItem);
			messageString.setValue("Characters from: " + plotModule.characterSourceTask.getNameAndParameters());
			if (checkLegalValues(charsStates, tree, tree.getRoot(), plotModule.firstItem))
				messageString.append("\nHorizontal axis: item " + plotModule.itemNames[plotModule.firstItem]);
			else
				messageString.append("\nValues on horizontal axis (item " +plotModule.itemNames[plotModule.firstItem] + ") not shown because reconstruction invalid, possibly because of missing data");
			if (checkLegalValues(charsStates, tree, tree.getRoot(), plotModule.secondItem))
				messageString.append("\nVertical axis: item " + plotModule.itemNames[plotModule.secondItem]);
			else
				messageString.append("\nValues on vertical axis (item " +plotModule.itemNames[plotModule.secondItem] + ") not shown because reconstruction invalid, possibly because of missing data");
			messageString.append("\n\nAncestral forms reconstructed by: " + plotModule.allCharsTask.getNameAndParameters());
		}
		if (legend != null)
			legend.repaint();

	}
	private boolean checkLegalValues(MContinuousHistory history, Tree tree, int node, int item) {
		boolean illegalValue = false;
		for (int ic=0; ic<history.getNumChars(); ic++)
			if (!ContinuousState.isCombinable(charsStates.getState(ic, node, item)))
				return false;


		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			if (!checkLegalValues(history, tree, daughter, item))
				return false;
		return true;
	}
	public void resize(){
		if (plotsAtNodes==null)
			return;
		plotsAtNodes.repaintPanels(tree, tree.getRoot());
		for (int i=0; i<plotsAtNodes.getNumNodes(); i++) {
			Panel p = plotsAtNodes.getPanel(i);
			if (p!=null)
				p.setSize(plotModule.drawingWidth,plotModule.drawingHeight);
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		checkPlotStorage(tree);
		if (plotsAtNodes!=null) {
			if (treeDisplay!= plotsAtNodes.getTreeDisplay())
				plotsAtNodes.setTreeDisplay(treeDisplay);
			plotsAtNodes.locatePanels(tree, drawnRoot);
			plotsAtNodes.showPanels(tree, drawnRoot);
		}
		if (legend!=null)
			legend.adjustLocation();
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public void turnOff(){
		if (plotsAtNodes!=null)
			plotsAtNodes.dispose();
		if (plotModule!=null && plotModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)plotModule.containerOfModule()).removeTool(showTool);
		}
		if (legend!=null)
			removePanelPlease(legend);
		super.turnOff();
	}
}

/* ======================================================================== */
/** A class to hold an array of charts for the nodes of a tree. */
class DrawingsAtNodes extends PanelsAtNodes  {
	MContinuousHistory history;
	double minX, maxX, minY, maxY;
	MesquiteNumber minColor, maxColor;
	private static final int initialWidth = 100;
	private static final int initialHeight = 60;
	double[][] colors;
	LandmarkDrawings plotModule;
	int firstItemNumber = 0;
	int secondItemNumber = 1;
	PlotOperator plotOperator;
	Tree tree;
	double [][][] tGrids, configs;
	MesquiteColorTable colorTable = new ContColorTable();
	int steps = 8;
	public DrawingsAtNodes(LandmarkDrawings ownerModule, int numNodes, TreeDisplay treeDisplay, PlotOperator plotOperator){
		super(ownerModule, numNodes, treeDisplay);
		this.plotOperator = plotOperator;
		plotModule = ownerModule;
		minColor = new MesquiteNumber();
		maxColor = new MesquiteNumber();

	}

	public void setCharsStates(MContinuousHistory charsStates, Tree tree, int firstItem, int secondItem){
		history = charsStates;
		this.tree = tree;
		if (tGrids==null || tree.getNumNodeSpaces()< tGrids.length) {
			tGrids = new double[tree.getNumNodeSpaces()][][];
			configs = new double[tree.getNumNodeSpaces()][][];
		}
		if (configs[0] == null)
			for (int i=0; i<configs.length; i++) {
				configs[i] = new double[2][];
			}
		int numChars = history.getNumChars();
		if (configs[0][0]== null || configs[0][0].length!=numChars)
			for (int i=0; i<configs.length; i++)
				for (int dim=0; dim<2;dim++) {
					configs[i][dim] = new double[numChars];
				}
		for (int i=0; i<configs.length; i++)
			Double2DArray.deassignArray(configs[i]);
		firstItemNumber = firstItem;
		secondItemNumber = secondItem;

		MesquiteDouble max = new MesquiteDouble();
		MesquiteDouble min = new MesquiteDouble();
		history.getMinMax(tree, tree.getRoot(), firstItemNumber, min, max);
		minX = min.getValue();
		maxX = max.getValue();
		history.getMinMax(tree, tree.getRoot(), secondItemNumber, min, max);
		minY =min.getValue();
		maxY = max.getValue();

		//calculate squared difference 
		if (colors==null || colors.length!=numChars || colors.length<1 || colors[0].length!=history.getNumNodes())
			colors = new double[numChars][history.getNumNodes()];
		minColor.setToUnassigned();
		maxColor.setToUnassigned();

		contrastAncestor(tree, tree.getRoot());
		repaintPanels(tree, tree.getRoot());
		for (int i=0; i<getNumNodes(); i++) {
			Panel p =getPanel(i);
			if (p!=null) {
				if (tree.nodeIsTerminal(i))
					p.setBackground(ColorDistribution.lightBlue);
			}

		}
	}
	MesquiteDouble dub = new MesquiteDouble();
	private void contrastAncestor (Tree tree, int node) {
		boolean illegalValue = false;
		for (int ic=0; ic<history.getNumChars(); ic++){
			configs[node][0][ic]= history.getState(ic, node, firstItemNumber); //seems reversed???
			configs[node][1][ic]= history.getState(ic, node, secondItemNumber);
		}
		if (node != tree.getRoot()) {
			double[][] grid = MatrixUtil.makeGrid(configs[node], steps);
			tGrids[node] =  MatrixUtil.tps(grid, configs[tree.motherOfNode(node)], configs[node]);
		}
		else
			tGrids[node]  = MatrixUtil.makeGrid(configs[node], steps);

		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			contrastAncestor(tree, daughter);
		if (node != tree.getRoot()) {
			for (int ic=0; ic<history.getNumChars(); ic++){
				dub.setValue(0);
				dub.add(MesquiteDouble.squaredDifference(history.getState(ic, node, firstItemNumber), history.getState(ic, tree.motherOfNode(node), firstItemNumber)));
				dub.add(MesquiteDouble.squaredDifference(history.getState(ic, node, secondItemNumber), history.getState(ic, tree.motherOfNode(node), secondItemNumber)));
				colors[ic][node]= dub.getValue();
				minColor.setMeIfIAmMoreThan(colors[ic][node]);
				maxColor.setMeIfIAmLessThan(colors[ic][node]);
			}
		}

	}

	public Panel makePanel(int i){
		NodeDrawing c = new NodeDrawing(this, i);
		if (plotModule==null)
			c.setSize(initialWidth,initialHeight);
		else
			c.setSize(plotModule.drawingWidth,plotModule.drawingHeight);
		c.setBackground(ColorDistribution.lightGreen);
		return c;
	}
	public void setSizes(int w, int h){
		for (int i=0; i<numNodes; i++) {
			Panel p =getPanel(i);
			if (p !=null) {
				p.setSize(w, h);
			}
		}
	}
}
/* ======================================================================== */
class NodeDrawing extends MesquitePanel  {
	DrawingsAtNodes drawings;
	int whichNode =0;
	ContinuousState cs=null;
	ContinuousState ancCs = null;
	int radius = 4;
	public NodeDrawing(DrawingsAtNodes drawings, int whichNode){
		super();
		this.drawings = drawings;
		this.whichNode = whichNode;
	}
	// -----------------------minimum--------------------;
	public static double minimum(double[][] matrix, int firstIndex){
		int numRows = MatrixUtil.numFullRows(matrix);
		double result = MesquiteDouble.unassigned;
		for (int j=0; j<numRows; j++) {
			result = MesquiteDouble.minimum(matrix[firstIndex][j],result);
		}
		return result;
	}
	// -----------------------maximum--------------------;
	public static double maximum(double[][] matrix, int firstIndex){
		int numRows = MatrixUtil.numFullRows(matrix);
		double result = MesquiteDouble.unassigned;
		for (int j=0; j<numRows; j++) {
			result = MesquiteDouble.maximum(matrix[firstIndex][j],result);
		}
		return result;
	}
	/*............................................................................................*/
	public void plotGrid(Graphics g, double[][] grid, int steps, int width, int height){
		if (grid[0].length==0){
			MesquiteMessage.warnProgrammer("plot grid[0] length 0 ");
			return;
		}
		int[][] rescaled = new int[grid.length][grid[0].length];

		double minX = minimum(grid, 0);
		double maxX = maximum(grid, 0);
		double minY = minimum(grid,1);
		double maxY =maximum(grid, 1);
		for (int i=0; i<grid[0].length; i++) {
			rescaled[0][i]=  getX(grid[0][i], minX, maxX, width);
			rescaled[1][i]=  getY(grid[1][i], minY, maxY, height);
		}

		for ( int i=0; i<grid[0].length - steps; i++) {
			int ir = i+1;
			int ib = i+steps;
			if (i==0 || (i % steps) != (steps-1))
				g.drawLine(rescaled[0][i],rescaled[1][i], rescaled[0][ir], rescaled[1][ir]);
			g.drawLine(rescaled[0][i],rescaled[1][i], rescaled[0][ib], rescaled[1][ib]);	     
		} 
		for ( int k=0; k<(steps-1); k++) {
			int i  = steps*(k);
			int ir = i+steps;
			g.drawLine(rescaled[0][i], rescaled[1][i], rescaled[0][ir], rescaled[1][ir]);
		} 	
		for ( int l=0; l<(steps-1); l++) {
			int i  = steps*(steps-1)+(l);
			int ib = i+1;
			g.drawLine( rescaled[0][i], rescaled[1][i], rescaled[0][ib], rescaled[1][ib]);
		} 
	} 
	/*	*/
	int getX(double x, double minX, double maxX, int width){
		return (int)( (x-minX)/(maxX-minX /*+ 0.1*maxX*/) * width);
	}
	int getY(double y, double minY, double maxY, int height){
		return (int)(height - (int)(((y-minY)/(maxY-minY /*+ 0.1*maxY*/))* height));
	}
//	............................................................................................	*/
	public void drawOutline(Graphics g, double[][]x, double[][]y, double[][]colors,double minX, double maxX, double minY, double maxY, double minColor, double maxColor, Rectangle bounds){
		if (g==null || bounds==null || x==null || y== null)
			return;
		Color c = g.getColor();
		if (whichNode!=drawings.tree.getRoot()){
			g.setColor(Color.white);
			plotGrid(g, drawings.tGrids[whichNode], drawings.steps, bounds.width, bounds.height);
			if (c!=null) g.setColor(c);
		}
		int firstX = MesquiteInteger.unassigned;
		int firstY = MesquiteInteger.unassigned;
		int lastX = MesquiteInteger.unassigned;
		int lastY = MesquiteInteger.unassigned;

		for (int i=0; i<x.length; i++) {
			int thisX =getX(x[i][whichNode], minX, maxX, bounds.width);
			int thisY =getY(y[i][whichNode], minY, maxY,  bounds.height);
			if (MesquiteInteger.isCombinable(lastX))
				g.drawLine(thisX,thisY,lastX, lastY);
			else {
				firstX =thisX;
				firstY = thisY;
			}
			lastX =thisX;
			lastY = thisY;
		}
		if (MesquiteInteger.isCombinable(firstX) && drawings.plotModule.joinLastToFirst.getValue())
			g.drawLine(firstX,firstY,lastX, lastY);

		Color orig = g.getColor();
		for (int i=0; i<x.length; i++) {
			int thisX =getX(x[i][whichNode], minX, maxX, bounds.width);
			int thisY =getY(y[i][whichNode], minY, maxY,  bounds.height);
			if (!MesquiteDouble.isUnassigned(x[i][whichNode]) && !MesquiteDouble.isUnassigned(y[i][whichNode])) {
				g.fillOval(thisX-radius, thisY-radius, radius*2, radius*2);
				if (colors != null && MesquiteDouble.isCombinable(colors[i][whichNode]) && colors[i][whichNode]!=0) {
					g.setColor(drawings.colorTable.getColor(colors[i][whichNode], minColor, maxColor));
					g.fillOval(thisX-radius+1, thisY-radius+1, radius*2-2, radius*2-2);
					g.setColor(orig);
				}
				g.drawString(Integer.toString(CharacterStates.toExternal(i)), thisX+10, thisY);
			}
		}
	}
	/*_________________________________________________*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (tool == drawings.plotOperator.showTool) {
			drawings.hidePanel(this);
		}
	}

	public void paint(Graphics g){
		if (drawings.history!=null) {
			g.drawRect(0,0, getBounds().width-1, getBounds().height-1);
			int height = getBounds().height-radius*2;
			int width = getBounds().width-radius*2;
			int firstX = MesquiteInteger.unassigned;
			int firstY = MesquiteInteger.unassigned;
			int lastX = MesquiteInteger.unassigned;
			int lastY = MesquiteInteger.unassigned;
			Double2DArray dX = drawings.history.getItem(drawings.firstItemNumber);
			Double2DArray dY = drawings.history.getItem(drawings.secondItemNumber);
			if (dX==null || dY == null){
				return;
			}
			double[][] x = dX.getMatrix();
			double[][] y = dY.getMatrix();

			drawOutline(g, x, y, drawings.colors, drawings.minX, drawings.maxX, drawings.minY, drawings.maxY, drawings.minColor.getDoubleValue(), drawings.maxColor.getDoubleValue()*2, new Rectangle(0,0, width, height));
		}
	}
}


class LandmarkLegend extends TreeDisplayLegend {
	private LegendHolder traceModule;
	private MesquiteString resultString;
	private static final int defaultLegendWidth=184;
	private static final int defaultLegendHeight=120;
	TextArea specsBox;
	private boolean holding = false;
	final int defaultSpecsHeight = (114 + MesquiteModule.textEdgeCompensationHeight) * 1;
	private int specsHeight = defaultSpecsHeight;
	private int e = 22;
	private String title;
	private Color titleColor;

	public LandmarkLegend(LegendHolder traceModule, TreeDisplay treeDisplay, MesquiteString resultString, String title, Color titleColor) {
		super(treeDisplay,defaultLegendWidth, defaultLegendHeight);
		setVisible(false);
		this.title = title;
		this.titleColor = titleColor;
		this.resultString = resultString;
		legendWidth=defaultLegendWidth;
		legendHeight=defaultLegendHeight;
		setOffsetX(traceModule.getInitialOffsetX());
		setOffsetY(traceModule.getInitialOffsetY());
		this.traceModule = traceModule;
		setBackground(ColorDistribution.lightGreen);
		setLayout(null);
		setSize(legendWidth, legendHeight);

		specsBox = new TextArea(" ", 2, 2, TextArea.SCROLLBARS_NONE);
		specsBox.setEditable(false);
		if (traceModule.showLegend())// && traceModule.showReconstruct.getValue())
			specsBox.setVisible(false);
		specsBox.setBounds(3, e, legendWidth-6, specsHeight);
		add(specsBox);
		specsBox.setBackground(Color.white);
		reviseBounds();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		if (specsBox!=null)// && traceModule.showReconstruct.getValue())
			specsBox.setVisible(b);
	}

	public void refreshSpecsBox(){
		if (resultString!=null)
			specsBox.setText(resultString.getValue()); 
	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (!holding) {
			g.setColor(Color.black);
			g.setColor(Color.cyan);
			g.drawRect(0, 0, legendWidth-1, legendHeight-1);
			g.fillRect(legendWidth-6, legendHeight-6, 6, 6);
			g.drawLine(0, 0, legendWidth-1, 0);

			g.setColor(titleColor);
			g.drawString(title, 4, 14);
			g.setColor(Color.black);
			if (resultString!=null  && resultString.getValue()!=null && !resultString.getValue().equals(specsBox.getText())){
				specsBox.setText(resultString.getValue()); 
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void printAll(Graphics g) {
		g.setColor(Color.black);
		g.drawString(title, 4, 14);
		int QspecsHeight = 0;
		if (resultString!=null) {
			String info = resultString.getValue();
			StringInABox sib = new StringInABox(info, g.getFont(), legendWidth);
			sib.draw(g, 4, 16);
			QspecsHeight = sib.getHeight();
		}
	}

	public void legendResized(int widthChange, int heightChange){
		if ((specsHeight + heightChange)>= defaultSpecsHeight)
			specsHeight += heightChange;
		else
			specsHeight  = defaultSpecsHeight;
		checkComponentSizes();
	}

	public void reviseBounds(){
		checkComponentSizes();
		Point where = getLocation();
		Rectangle bounds = getBounds();
		if (bounds.width!=legendWidth || bounds.height!=legendHeight) //make sure a change is really needed
			setBounds(where.x,where.y,legendWidth, legendHeight);
	}
	public void checkComponentSizes(){
		specsBox.setBounds(3,e,legendWidth-6, specsHeight);
		specsBox.setVisible(true);
		legendHeight=specsHeight + e + 4;
	}

	public void onHold() {
		holding = true;
	}

	public void offHold() {
		holding = false;
	}
}






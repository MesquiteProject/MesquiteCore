/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.PlotTree;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
//import mesquite.rhetenor.*;

/* ======================================================================== */
public class PlotTree extends AnalyticalDrawTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(NodeLocsPlot.class, getName() + " uses a module to plot node locations.",
		"The method to choose node locations is either chosen automatically or when Plot Tree is first requested.");
	}

	NodeLocsPlot nodeLocsTask;
	Vector drawings;
	int spotSize = 16;
	int oldEdgeWidth = 4;
	public MesquiteBoolean showInternals, showTree, showTerminals;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsPlot)hireEmployee(NodeLocsPlot.class, "Method to choose node locations");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node location plotter module was obtained.");
		drawings = new Vector();
 		addMenuItem( "Spot Size...", makeCommand("setSpotDiameter",  this));
		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		showInternals = new MesquiteBoolean(true);
		showTerminals = new MesquiteBoolean(true);
		showTree = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Show Terminal Nodes", makeCommand("toggleShowTerminals",  this), showTerminals);
		addCheckMenuItem(null, "Show Internal Nodes", makeCommand("toggleShowInternals",  this), showInternals);
		addCheckMenuItem(null, "Show Branches", makeCommand("toggleShowTree",  this), showTree);
 		return true;
 	 }
  	 
/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
   	 public Dimension getPreferredSize(){
   	 	if (nodeLocsTask == null)
   	 		return null;
   	 		
   	 	return nodeLocsTask.getPreferredSize();
   	 }
 	/** Returns true if it will have a preferred size of the tree drawing */
 	public boolean hasPreferredSize(){
    	 	if (nodeLocsTask == null)
    	 		return false;
 		return nodeLocsTask.hasPreferredSize();
 	}
 	public void employeeQuit(MesquiteModule m){
 			iQuit();
 	}
	/*.................................................................................................................*/
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		PlotTreeDrawing treeDrawing =  new PlotTreeDrawing (treeDisplay, numTaxa, this, spotSize);
		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	/** Returns true if other modules can control the orientation */
	public boolean allowsReorientation(){
		return false;
	}

	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
		temp.addLine("setEdgeWidth " + oldEdgeWidth); 
  	 	temp.addLine("setNodeLocs " , nodeLocsTask);
  	 	temp.addLine("setSpotDiameter " + spotSize);
  	 	temp.addLine("toggleShowTerminals " + showTerminals.toOffOnString());
  	 	temp.addLine("toggleShowInternals " + showInternals.toOffOnString());
   	 	temp.addLine("toggleShowTree " + showTree.toOffOnString());
 	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets diameter of spots at nodes", "[diameter]", commandName, "setSpotDiameter")) {
			int newDiameter = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newDiameter))
				newDiameter = MesquiteInteger.queryInteger(containerOfModule(), "Set spot diameter", "Spot Diameter:", spotSize, 0, 100);
    	 			
    	 		if (newDiameter>-1 && newDiameter<100 && newDiameter!=spotSize) {
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					PlotTreeDrawing treeDrawing = (PlotTreeDrawing)obj;
		    	 			spotSize = newDiameter;
		    	 			treeDrawing.spotsize=newDiameter;
		    	 			treeDrawing.treeDisplay.setMinimumTaxonNameDistance(treeDrawing.spotsize/2, 4);
		    	 			parametersChanged();
	    	 		}
    	 		}
    	 		
    	 	}
    		else 	if (checker.compare(this.getClass(), "Sets the width of lines for drawing the tree", "[width in pixels]", commandName, "setEdgeWidth")) {

    			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
    			if (!MesquiteInteger.isCombinable(newWidth))
    				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set edge width", "Edge Width:", oldEdgeWidth, 1, 99);
    			if (newWidth>0 && newWidth<100 && newWidth!=oldEdgeWidth) {
    				oldEdgeWidth=newWidth;
    				Enumeration e = drawings.elements();
    				while (e.hasMoreElements()) {
    					Object obj = e.nextElement();
    					PlotTreeDrawing treeDrawing = (PlotTreeDrawing)obj;
    					treeDrawing.setEdgeWidth(newWidth);
    					//treeDrawing.treeDisplay.setMinimumTaxonNameDistance(newWidth, 6); 
    				}
    				if ( !MesquiteThread.isScripting()) parametersChanged();
    			}

    		}
    	 	else if (checker.compare(this.getClass(), "Sets whether or not internal nodes are shown", "[on = show;  off]", commandName, "toggleShowInternals")) {
    	 		showInternals.toggleValue(parser.getFirstToken(arguments));
	 		 parametersChanged();
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				PlotTreeDrawing treeDrawing = (PlotTreeDrawing)obj;
				treeDrawing.treeDisplay.repaint();
    	 		}
    	 	}
   	 	else if (checker.compare(this.getClass(), "Sets whether or not terminal nodes are shown", "[on = show;  off]", commandName, "toggleShowTerminals")) {
    	 		showTerminals.toggleValue(parser.getFirstToken(arguments));
	 		 parametersChanged();
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				PlotTreeDrawing treeDrawing = (PlotTreeDrawing)obj;
				treeDrawing.treeDisplay.repaint();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether or not the tree is shown", "[on = show;  off]", commandName, "toggleShowTree")) {
    	 		showTree.toggleValue(parser.getFirstToken(arguments));
	 		 parametersChanged();
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				PlotTreeDrawing treeDrawing = (PlotTreeDrawing)obj;
				treeDrawing.treeDisplay.repaint();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the module that calculates the node locations", "[name of module]", commandName, "setNodeLocs")) {
			NodeLocsPlot temp= (NodeLocsPlot)replaceEmployee(NodeLocsPlot.class, arguments, "Method choose node locations", nodeLocsTask);
    	 		if (temp!=null) {
    	 		 	nodeLocsTask=temp;
    	 		 	parametersChanged();
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					PlotTreeDrawing treeDrawing = (PlotTreeDrawing)obj;
					treeDrawing.treeDisplay.repaint();
	    	 		}
    	 		 }
	    	 	return temp;
 		}
    	 	else {
 			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Plot Tree";
   	 }
   	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Draws trees plotted in a two dimensional space." ;
   	 }
	/*.................................................................................................................*/
   	 
}

/* ======================================================================== */
class PlotTreeDrawing extends TreeDrawing  {

	public PlotTree ownerModule;
	public int edgewidth = 4;
	public int spotsize = 24;
	int oldNumTaxa = 0;
 	public static final int inset=1;
	private boolean ready=false;
	private int foundBranch;
	private NameReference colorNameRef;
	public PlotTreeDrawing (TreeDisplay treeDisplay, int numTaxa, PlotTree ownerModule, int spotSize) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		this.spotsize = spotSize;
		colorNameRef = NameReference.getNameReference("Color");
	    	treeDisplay.setMinimumTaxonNameDistance(spotsize/2, 4);
		treeDisplay.setOrientation(TreeDisplay.FREEFORM);
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;
		ready = true;
	}
	/*_________________________________________________*/
	private void calculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]=y[tree.motherOfNode(node)];
		lineBaseX[node]=x[tree.motherOfNode(node)];
	}
	
	private int getSpotSize(int node){
			return spotsize;
	}
	
	/*_________________________________________________*/
	/** Draw highlight for branch node */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		if (MesquiteDouble.isCombinable(x[node]) && MesquiteDouble.isCombinable(y[node])) {
			Color tC = g.getColor();
			if (flip)
				g.setColor(Color.red);
			else
				g.setColor(Color.blue);
			int s = getSpotSize(node);
			for (int i=1; i<4; i++)
				GraphicsUtil.drawOval(g, x[node]- s/2 - 2 - i, y[node]- s/2 - 2 - i, s + 3 + i + i, s + 3 + i + i);

			g.setColor(tC);
		}
	}
	/*_________________________________________________*/
	private   void drawLines(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			g.setColor(treeDisplay.getBranchColor(node));
			Composite composite = 	treeDisplay.setBranchTransparency(g, node);
	
			if (node == tree.getRoot()) {
			}
			else if (tree.motherOfNode(node) == tree.getRoot() && !tree.getRooted() && !tree.nodeIsPolytomous( tree.getRoot() )) {
				int sisterNode = 0;
				if (tree.nodeExists(tree.nextSisterOfNode(node)))
					sisterNode = tree.nextSisterOfNode(node);
				else if (tree.nodeExists(tree.previousSisterOfNode(node)))
					sisterNode = tree.previousSisterOfNode(node);
				if (sisterNode !=0) {
					if (MesquiteDouble.isCombinable(lineTipX[node]) && MesquiteDouble.isCombinable(lineTipY[node]) && MesquiteDouble.isCombinable(lineTipX[sisterNode]) && MesquiteDouble.isCombinable(lineTipY[sisterNode])) {
						GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node], lineTipX[sisterNode], lineTipY[sisterNode],edgewidth);
						/*GraphicsUtil.drawLine(g,lineTipX[node]+1, lineTipY[node], lineTipX[sisterNode]+1, lineTipY[sisterNode]);
						GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node], lineTipX[sisterNode], lineTipY[sisterNode]);
						GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node]+1, lineTipX[sisterNode], lineTipY[sisterNode]+1);
						GraphicsUtil.drawLine(g,lineTipX[node]+1, lineTipY[node]+1, lineTipX[sisterNode]+1, lineTipY[sisterNode]+1);*/
					}
				}
			}
			else  if (MesquiteDouble.isCombinable(lineTipX[node]) && MesquiteDouble.isCombinable(lineBaseX[node]) && MesquiteDouble.isCombinable(lineTipY[node]) && MesquiteDouble.isCombinable(lineBaseY[node])) {
				GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node], lineBaseX[node], lineBaseY[node], edgewidth);
				/*GraphicsUtil.drawLine(g,lineTipX[node]+1, lineTipY[node], lineBaseX[node]+1, lineBaseY[node]);
				GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node], lineBaseX[node], lineBaseY[node]);
				GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node]+1, lineBaseX[node], lineBaseY[node]+1);
				GraphicsUtil.drawLine(g,lineTipX[node]+1, lineTipY[node]+1, lineBaseX[node]+1, lineBaseY[node]+1); */
			}
			
			if (composite!=null)
				ColorDistribution.setComposite(g, composite);
			
			int thisSister = tree.firstDaughterOfNode(node);
			while (tree.nodeExists(thisSister)) {
				drawLines( tree, g, thisSister);
				thisSister = tree.nextSisterOfNode(thisSister);
			}
		}
	}
	/*_________________________________________________*/
	private boolean getDrawNode(Tree tree, int node) {
		return (tree.nodeIsTerminal(node) && ownerModule.showTerminals.getValue())|| (!tree.nodeIsTerminal(node) && ownerModule.showInternals.getValue());
	}
	
	/*_________________________________________________*/
	private   void drawSpots(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			g.setColor(treeDisplay.getBranchColor(node));
			Composite composite = 	treeDisplay.setBranchTransparency(g, node);
			if (getDrawNode(tree,node)){
				if (node == tree.getRoot()) {
					if (tree.getRooted()) {
						drawSpot( g, node);
						highlightSpot( g, node);
					}
				}
				else  {
					drawSpot( g, node);
				}
			}
			if (composite!=null)
				ColorDistribution.setComposite(g, composite);
			int thisSister = tree.firstDaughterOfNode(node);
			while (tree.nodeExists(thisSister)) {
				drawSpots( tree, g, thisSister);
				thisSister = tree.nextSisterOfNode(thisSister);
			}
		}
	}
	private int bumpUp(int i){
		return i + (255-i)/4;
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
	        if (MesquiteTree.OK(tree)) {
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	g.setColor(treeDisplay.branchColor);
			calculateLines(tree, drawnRoot);
	       	 	if (ownerModule.showTree.getValue())
	       	 		drawLines(tree, g, drawnRoot);  
	       	 	drawSpots(tree, g, drawnRoot);  
	       	 }
	   }
	
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
	        if (MesquiteTree.OK(tree) && !ownerModule.isDoomed()) {
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	if (!tree.nodeExists(getDrawnRoot()))
	        		setDrawnRoot(tree.getRoot());
			ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, getDrawnRoot(),  treeDisplay.getField()); 
		}
	}
	/*_________________________________________________*/
	private boolean inSpot(int node, int h, int v){
		int s = getSpotSize(node);
		if (h> x[node]+s || h < x[node] - s || v > y[node]+s || v < y[node]-s) //first test: within bounding box?
			return false;
		if ((h-x[node])*(h-x[node]) + (v-y[node])*(v-y[node]) < s*s/4) //use radius
			return true;
		else
			return false;
	}
	/*_________________________________________________*/
	private void drawSpot(Graphics g, int node){
		if (MesquiteDouble.isCombinable(x[node]) && MesquiteDouble.isCombinable(y[node])) {
			int s = getSpotSize(node);
			GraphicsUtil.fillOval(g, x[node]- s/2, y[node]- s/2, s, s);
		}
	}
	/*_________________________________________________*/
	private void highlightSpot(Graphics g, int node){
		if (MesquiteDouble.isCombinable(x[node]) && MesquiteDouble.isCombinable(y[node])) {
			int s = getSpotSize(node);
			for (int diam = s + 12; diam> s + 8; diam --)
				GraphicsUtil.drawOval(g, x[node]- (int)((double)diam/2 + 0.5), y[node]- (int)((double)diam/2 + 0.5), diam, diam);
		}
	}
	/*_________________________________________________*/
	private void fillSpot(Graphics g, int node){
		if (MesquiteDouble.isCombinable(x[node]) && MesquiteDouble.isCombinable(y[node])) {
			int s = getSpotSize(node);
			GraphicsUtil.fillOval(g, x[node]- s/2 + 2, y[node]- s/2 + 2, s - 4, s - 4);
		}
	}
	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && (getDrawNode(tree,node))) {
			if (MesquiteDouble.isCombinable(x[node]) && MesquiteDouble.isCombinable(y[node])) {
				Color c = g.getColor();
				int numColors = colors.getNumColors();
				if (numColors==1){
					g.setColor(colors.getColor(0, !tree.anySelected()|| tree.getSelected(node)));
					fillSpot(g,node);
				}
				else if (numColors>0) {
					int startAngle=90;
					double totalFreq=0;
					for (int i=0; i<numColors; i++) totalFreq += colors.getWeight(i);
					
					int arcAngle = 360/(numColors);
					int s = getSpotSize(node);
					for (int i=0; i<numColors; i++) {
						Color color;
						if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
							g.setColor(color);
						
						arcAngle = (int)((colors.getWeight(i)/totalFreq)*360);
						GraphicsUtil.fillArc(g, x[node]- s/2 + 2, y[node]- s/2 + 2, s - 4, s - 4, startAngle, arcAngle);
						startAngle+=arcAngle;
					}
				}
				if (c!=null) g.setColor(c);
			}
		}
	}

	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (node>0 && (tree.getRooted() || node != tree.getRoot()) && (getDrawNode(tree,node))) {
			fillSpot(g,node);
		}
	}
	   
	/*_________________________________________________*/
	private void ScanBranches(Tree tree, int node, int x, int y)
	{
		if (foundBranch==0) {
			if ( inSpot(node, x, y))
				foundBranch = node;

			int thisSister = tree.firstDaughterOfNode(node);
			while (tree.nodeExists(thisSister)) {
				ScanBranches(tree, thisSister, x, y);
				thisSister = tree.nextSisterOfNode(thisSister);
			}

		}
	}
	/*_________________________________________________*/
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) { 
	        if (MesquiteTree.OK(tree) && ready) {
	        	foundBranch=0;
	       		 ScanBranches(tree, drawnRoot, x, y);
	       		 return foundBranch;
	       	}
	       	return 0;
	}
	
	/*_________________________________________________*/
	public void reorient(int orientation) {
		treeDisplay.setOrientation(orientation);
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void setEdgeWidth(int edw) {
		edgewidth = edw;
	}
/*New code Feb.22.07 allows eavesdropping on edgewidth by the TreeDrawing oliver*/ //TODO: delete new code comments
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgewidth;
	}
/*End new code Feb.22.07 oliver*/
}
	



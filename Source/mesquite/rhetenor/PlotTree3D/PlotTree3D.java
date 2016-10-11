/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.PlotTree3D;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.rhetenor.lib.*;

/* ======================================================================== */
public class PlotTree3D extends AnalyticalDrawTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsPlot3D.class, getName() + "  needs a method to calculate positions of nodes.",
		"The method to calculate positions is arranged initially");
	}

	NodeLocsPlot3D nodeLocsTask;
	Vector drawings;
	int spotSize = 16;
	public MesquiteBoolean showInternals, showFog, showTree, showTerminals;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsPlot3D)hireEmployee(NodeLocsPlot3D.class, "Method to choose node locations");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node location plotter module was obtained.");
		drawings = new Vector();
 		addMenuItem( "Spot Size...", makeCommand("setSpotDiameter",  this));
		showInternals = new MesquiteBoolean(true);
		showTerminals = new MesquiteBoolean(true);
		showTree = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Show Terminal Nodes", makeCommand("toggleShowTerminals",  this), showTerminals);
		addCheckMenuItem(null, "Show Internal Nodes", makeCommand("toggleShowInternals",  this), showInternals);
		addCheckMenuItem(null, "Show Branches", makeCommand("toggleShowTree",  this), showTree);
		showFog = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Use Fog", makeCommand("toggleFog",  this), showFog);
 		return true;
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
  	 	temp.addLine("setNodeLocs " , nodeLocsTask);
  	 	temp.addLine("setSpotDiameter " + spotSize);
  	 	temp.addLine("toggleShowTerminals " + showTerminals.toOffOnString());
  	 	temp.addLine("toggleShowInternals " + showInternals.toOffOnString());
   	 	temp.addLine("toggleShowTree " + showTree.toOffOnString());
  	 	temp.addLine("toggleFog " + showFog.toOffOnString());
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets diameter of spots at nodes", "[diameter]", commandName, "setSpotDiameter")) {
			int newDiameter = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newDiameter))
				newDiameter = MesquiteInteger.queryInteger(containerOfModule(), "Set spot diameter", "Spot Diameter:", spotSize, 6, 100);
    	 			
    	 		if (newDiameter>6 && newDiameter<100 && newDiameter!=spotSize) {
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
    	 	else if (checker.compare(this.getClass(), "Sets whether or not to show distant nodes as if in fog", "[on = show;  off]", commandName, "toggleFog")) {
    	 		showFog.toggleValue(parser.getFirstToken(arguments));
	 		 parametersChanged();
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				PlotTreeDrawing treeDrawing = (PlotTreeDrawing)obj;
				treeDrawing.treeDisplay.repaint();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the module that calculates the node locations", "[name of module]", commandName, "setNodeLocs")) {
			NodeLocsPlot3D temp= (NodeLocsPlot3D)replaceEmployee(NodeLocsPlot3D.class, arguments, "Method choose node locations", nodeLocsTask);
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
		return "Plot Tree 3D";
   	 }
   	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Draws trees plotted in a three dimensional space." ;
   	 }
	/*.................................................................................................................*/
   	public boolean isSubstantive(){
   		return true;
   	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
}

/* ======================================================================== */
class PlotTreeDrawing extends TreeDrawing  {

	public PlotTree3D ownerModule;
	public int edgewidth = 4;
	public int spotsize = 24;
	int oldNumTaxa = 0;
 	public static final int inset=1;
	private boolean ready=false;
	private int foundBranch;
	double[] zScores;
	int[] nodes;
	private NameReference colorNameRef;
	public PlotTreeDrawing (TreeDisplay treeDisplay, int numTaxa, PlotTree3D ownerModule, int spotSize) {
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
	
	double minimumZ, maximumZ;
	private Color applyFog(Color c, int node){
		if (!ownerModule.showFog.getValue())
			return c;
		double fog = 0;
		double beyondMax = maximumZ*1.1;
		if (MesquiteDouble.isCombinable(z[node]) && (beyondMax-minimumZ)!=0)
			fog = (beyondMax - z[node])/(beyondMax-minimumZ);
		if (fog==0)
			return c;
		else
			return ColorDistribution.brighter(c, fog);
	}
	private int getSpotSize(int node){
		if (MesquiteDouble.isCombinable(z[node]) && z[node]>0 && (maximumZ-minimumZ)!=0) {
			int s = spotsize - (int)(spotsize*((z[node]-minimumZ)/(maximumZ-minimumZ) - 0.5));
			return s;
		}
		else
			return spotsize;
	}
	private void survey(Tree tree, int node, double[] zScores, int[] nodes, MesquiteInteger count){
		if (MesquiteDouble.isCombinable(z[node]) && z[node]>=0) {
			minimumZ = MesquiteDouble.minimum(minimumZ, z[node]);
			maximumZ = MesquiteDouble.maximum(maximumZ, z[node]);
			zScores[count.getValue()] = z[node];
			nodes[count.getValue()] = node;
			count.increment();
			
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			survey(tree, d, zScores, nodes, count);
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
	private   void drawLine(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			g.setColor(applyFog(treeDisplay.getBranchColor(node), node));
				
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
						GraphicsUtil.drawLine(g,lineTipX[node]+1, lineTipY[node], lineTipX[sisterNode]+1, lineTipY[sisterNode]);
						GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node], lineTipX[sisterNode], lineTipY[sisterNode]);
						GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node]+1, lineTipX[sisterNode], lineTipY[sisterNode]+1);
						GraphicsUtil.drawLine(g,lineTipX[node]+1, lineTipY[node]+1, lineTipX[sisterNode]+1, lineTipY[sisterNode]+1);
					}
				}
			}
			else  if (MesquiteDouble.isCombinable(lineTipX[node]) && MesquiteDouble.isCombinable(lineBaseX[node]) && MesquiteDouble.isCombinable(lineTipY[node]) && MesquiteDouble.isCombinable(lineBaseY[node])) {
				GraphicsUtil.drawLine(g,lineTipX[node]+1, lineTipY[node], lineBaseX[node]+1, lineBaseY[node]);
				GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node], lineBaseX[node], lineBaseY[node]);
				GraphicsUtil.drawLine(g,lineTipX[node], lineTipY[node]+1, lineBaseX[node], lineBaseY[node]+1);
				GraphicsUtil.drawLine(g,lineTipX[node]+1, lineTipY[node]+1, lineBaseX[node]+1, lineBaseY[node]+1);
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
			g.setColor(applyFog(treeDisplay.getBranchColor(node), node));
				
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
int count = 0;
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
	        if (MesquiteTree.OK(tree) && !ownerModule.isDoomed()) {
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	if (!tree.nodeExists(getDrawnRoot()))
	        		setDrawnRoot(tree.getRoot());
			ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, getDrawnRoot(),  treeDisplay.getField()); //Graphics g removed as parameter May 02
		}
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
	        if (MesquiteTree.OK(tree)) {
	        	if (tree.getNumNodeSpaces()!=numNodes) {
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	}
	        	if (zScores == null || zScores.length !=tree.getNumNodeSpaces()){
	        		zScores = new double[tree.getNumNodeSpaces()];
	        		nodes = new int[tree.getNumNodeSpaces()];
	        	}
	        	DoubleArray.deassignArray(zScores);
	        	IntegerArray.deassignArray(nodes);
	        	g.setColor(treeDisplay.branchColor);
			//ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField()); //Graphics g removed as parameter May 02
			minimumZ = MesquiteDouble.unassigned;
			maximumZ = MesquiteDouble.unassigned;
			MesquiteInteger count = new MesquiteInteger(0);
			survey(tree, drawnRoot, zScores, nodes, count);
			
			for (int i=1; i<zScores.length; i++) {
				for (int j= i-1; j>=0  &&  zScores[j]<zScores[j+1]; j--) {
					if (MesquiteDouble.isCombinable(zScores[j]) && MesquiteDouble.isCombinable(zScores[j+1])){
						double temp = zScores[j];
						zScores[j] = zScores[j+1];
						zScores[j+1]=temp;
						int tempN = nodes[j];
						nodes[j] = nodes[j+1];
						nodes[j+1]=tempN;
					}
				}
			}
			calculateLines(tree, drawnRoot);
	       	 	//drawSpots(tree, g, drawnRoot);  

			for (int i=0; i<nodes.length; i++) {
				if (tree.nodeExists(nodes[i])) {
					g.setColor(applyFog(treeDisplay.branchColor, nodes[i]));
					if (ownerModule.showTree.getValue())
						drawLine(tree, g, nodes[i]);
					if (getDrawNode(tree,nodes[i])){
						if (nodes[i] == tree.getRoot()) {
							if (tree.getRooted()) {
								drawSpot( g, nodes[i]);
								highlightSpot( g, nodes[i]);
							}
						}
						else  {
							drawSpot( g, nodes[i]);
						}
					}
				}
			}

	       	 }
	   }
	
	/*_________________________________________________*/
	private boolean inSpot(int node, int h, int v){
		int s = getSpotSize(node);
		if ((h-x[node])*(h-x[node]) + (v-y[node])*(v-y[node]) < s*s/4) //use radius
			return true;
		else
			return false;
		// ask if x, y is in node's spot    g.fillOval( x[node]- spotsize/2, y[node]- spotsize/2, spotsize, spotsize);
	}
	/*_________________________________________________*/
	private void drawSpot(Graphics g, int node){
		if (MesquiteDouble.isCombinable(x[node]) && MesquiteDouble.isCombinable(y[node])) {
			Color c = g.getColor();
			int s = getSpotSize(node);
			g.setColor(Color.white);
			GraphicsUtil.drawOval(g, x[node]- s/2-1, y[node]- s/2-1, s+2, s+2);
			if (c!=null) g.setColor(c);
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
					g.setColor(applyFog(colors.getColor(0, !tree.anySelected()|| tree.getSelected(node)), node));
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
							g.setColor(applyFog(color, node));
						
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
	



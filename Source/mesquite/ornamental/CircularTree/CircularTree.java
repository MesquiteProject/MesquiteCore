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
package mesquite.ornamental.CircularTree;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.geom.Arc2D;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class CircularTree extends DrawTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsCircle.class, getName() + "  needs a method to calculate node locations.",
		"The method to calculate node locations is arranged initially");
	}
	/*.................................................................................................................*/

	NodeLocsCircle nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	Vector drawings;
	int oldEdgeWidth = 6;
	
	/*----------------------------------------------------------------------------*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsCircle)hireEmployee(NodeLocsCircle.class, "Calculator of node locations");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node locations module obtained.");
		drawings = new Vector();
 		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		return true;
 	 }
 	 public void employeeQuit(MesquiteModule m){
 	 	iQuit();
 	 }
	/*----------------------------------------------------------------------------*/
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		CircleTreeDrawing treeDrawing =  new CircleTreeDrawing (treeDisplay, numTaxa, this);
		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setEdgeWidth " + oldEdgeWidth); 
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*----------------------------------------------------------------------------*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the thickness of lines used for the branches", "[width in pixels]", commandName, "setEdgeWidth")) {
			int newWidth = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth= MesquiteInteger.queryInteger(containerOfModule(), "Set edge width", "Edge Width:", oldEdgeWidth, 2, 99);
	    	 	if (newWidth>1 && newWidth<100 && newWidth!=oldEdgeWidth) {
	    	 		oldEdgeWidth = newWidth;
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					CircleTreeDrawing treeDrawing = (CircleTreeDrawing)obj;
			    	 			treeDrawing.setEdgeWidth(newWidth);
			 	}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the module assigning node locations", null, commandName, "getNodeLocsEmployee")) {
    	 		return nodeLocsTask;
    	 	}
 		else
 			return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Circular tree";
   	 }
   	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Draws trees in circular form." ;
   	 }
	/*.................................................................................................................*/
}

/* ======================================================================== */
class CircleTreeDrawing extends TreeDrawing  {

	public int highlightedBranch, branchFrom;
	public int xFrom, yFrom, xTo, yTo;
	public CircularTree ownerModule;
	public int edgewidth = 6;
	int oldNumTaxa = 0;
 	public static final int inset=1;
	private boolean ready=false;
	public Polygon[] branchPoly;
	public Polygon[] fillBranchPoly;
	BasicStroke defaultStroke;

	private int foundBranch;
	NameReference triangleNameRef;
	
	public CircleTreeDrawing (TreeDisplay treeDisplay, int numTaxa, CircularTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		try{
			defaultStroke = new BasicStroke();
		}
		catch (Throwable t){
		}
		triangleNameRef = NameReference.getNameReference("triangled");
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		treeDisplay.setOrientation(TreeDisplay.CIRCULAR);
		oldNumTaxa = numTaxa;
		namesFollowLines = true;
		ready = true;
	}
	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Polygon[numNodes];
		fillBranchPoly= new Polygon[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new Polygon();
			branchPoly[i].xpoints = new int[16];
			branchPoly[i].ypoints = new int[16];
			branchPoly[i].npoints=16;
			fillBranchPoly[i] = new Polygon();
			fillBranchPoly[i].xpoints = new int[16];
			fillBranchPoly[i].ypoints = new int[16];
			fillBranchPoly[i].npoints=16;
		}
	}
	/*----------------------------------------------------------------------------*/
	private void nodePolarToLoc (double polarlength, double angle, Point center, Point loc){
		loc.x = center.x + (int)Math.round(polarlength * Math.sin(angle));
		loc.y = center.y - (int)Math.round(polarlength * Math.cos(angle));
	}
	/*----------------------------------------------------------------------------*/
	private void nodeLocToPolar (Point loc, Point center, PolarCoord polar){
		polar.length = Math.sqrt((loc.x-center.x) *(loc.x-center.x) + (center.y-loc.y)*(center.y-loc.y));
		polar.angle = Math.asin((loc.x-center.x)/polar.length);
}
	private void makeSlantedRectangleOLD(Polygon poly, Point lowerCorner, double polarlength, double angle, int width){
		Point loc = new Point();
		poly.npoints=0;
		poly.addPoint(lowerCorner.x, lowerCorner.y);
		nodePolarToLoc (polarlength, angle, lowerCorner, loc);
		poly.addPoint(loc.x, loc.y);
		nodePolarToLoc (width, angle + Math.PI/2, loc, loc);
		poly.addPoint(loc.x, loc.y);
		nodePolarToLoc (polarlength, angle + Math.PI, loc, loc);
		poly.addPoint(loc.x, loc.y);
		poly.addPoint(lowerCorner.x, lowerCorner.y);
		poly.npoints=5;
	}
//	makeSlantedRectangle(branchPoly[node], loc, polarLength[node]-polarLength[motherN]+edgewidth, angle[node], edgewidth);
	private void makeSlantedRectangle(Polygon poly, double[] polarlength, double[] angle, int node, int motherN, int width){
		Point loc = new Point();
		Point w = new Point();
		nodePolarToLoc (width, angle[node] + Math.PI/2, ownerModule.nodeLocsTask.treeCenter, w);
		int wx2p = (w.x -ownerModule.nodeLocsTask.treeCenter.x)/2;
		if (wx2p == 0)
			wx2p = 1;
		int wx2m = -wx2p;
		int wy2p = (w.y -ownerModule.nodeLocsTask.treeCenter.y)/2;
		if (wy2p == 0)
			wy2p = 1;
		int wy2m = -wy2p;
		//if (wx2p == 0 && wy2p == 0)
		//	wx2p = 1;
		poly.npoints=0;
		nodePolarToLoc(polarlength[node], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
/**/
		poly.addPoint(loc.x + wx2p, loc.y + wy2p);
		poly.addPoint(loc.x + wx2m, loc.y + wy2m);
		nodePolarToLoc(polarlength[motherN], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		poly.addPoint(loc.x + wx2m, loc.y + wy2m);
		poly.addPoint(loc.x + wx2p, loc.y + wy2p);
		nodePolarToLoc(polarlength[node], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		poly.addPoint(loc.x + wx2p, loc.y + wy2p);
/**
 		poly.addPoint(loc.x + wx, loc.y + wy);
		poly.addPoint(loc.x, loc.y);
		nodePolarToLoc(polarlength[motherN], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		poly.addPoint(loc.x, loc.y);
		poly.addPoint(loc.x + wx, loc.y + wy);
		nodePolarToLoc(polarlength[node], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		poly.addPoint(loc.x + wx, loc.y + wy);
/**/
		poly.npoints=5;
	}
	private double findHighest(Tree tree, int node, double[] polarLength){
		if (tree.nodeIsTerminal(node))
			return polarLength[node];
		
		double highest = 0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			double thisHeight = findHighest( tree, d, polarLength);
			if (thisHeight>highest)
				highest = thisHeight;
		}
		return highest;
	}
	/*----------------------------------------------------------------------------*/
	private  void drawOneBranch(Tree tree, int node, Graphics g) {
		int motherN= tree.motherOfNode(node);
		Point loc = new Point();
		double[] polarLength= ownerModule.nodeLocsTask.polarLength;
		double[] angle= ownerModule.nodeLocsTask.angle;
		
		
		lineTipX[node]= treeDisplay.getTreeDrawing().x[node];
		lineTipY[node]= treeDisplay.getTreeDrawing().y[node];

		nodePolarToLoc(polarLength[motherN]-edgewidth, angle[node], ownerModule.nodeLocsTask.treeCenter, loc);

		lineBaseX[node]= loc.x;
		lineBaseY[node]= loc.y;

//		private void makeSlantedRectangle(Polygon poly, double[] polarlength, double[] angle, int node, int motherN, int width){
			makeSlantedRectangle(branchPoly[node],polarLength, angle, node, motherN, edgewidth);
			makeSlantedRectangle(fillBranchPoly[node],polarLength, angle, node, motherN, edgewidth-2);
	//	makeSlantedRectangle(fillBranchPoly[node], loc, polarLength[node]-polarLength[motherN]+edgewidth-2, angle[node], edgewidth-2);
		g.fillPolygon(branchPoly[node]);



		
		int L, R, T, B;
		drawArc(g, polarLength, angle, node, motherN, 0);

 
		if (tree.getAssociatedBit(triangleNameRef,node)) {
			double highestTerminal = findHighest(tree, node, polarLength);
			R = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.x + highestTerminal);
			L = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.x - highestTerminal);
			T =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.y - highestTerminal);
			B =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.y + highestTerminal);

			for (int i=0; i<3; i++)
				g.drawArc(L+i, T+i, R-L-i-i, B-T-i-i, convertToDegrees(myAngleToTheirs(angle[tree.leftmostTerminalOfNode(node)])), convertToDegrees(-angle[tree.rightmostTerminalOfNode(node)] +angle[tree.leftmostTerminalOfNode(node)]));
		
			for (int j=0; j<2; j++)
				for (int i=0; i<2; i++) {
					g.drawLine(x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
					//g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
					g.drawLine(x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
				}
		}
		if (emphasizeNodes()) {
			Color prev = g.getColor();
			g.setColor(Color.red);//for testing
			g.fillRect(x[node]-2, y[node]-2, 4, 4);
			//g.fillPolygon(nodePoly(node));
			g.setColor(prev);
		}
	}
	private void drawArc(Graphics g, double[] polarLength, double[] angle, int node, int motherN, int inset){

		if (g instanceof Graphics2D)
			try {
				double L, W, T;
				L = ownerModule.nodeLocsTask.treeCenter.x - polarLength[motherN];
				T =  ownerModule.nodeLocsTask.treeCenter.y - polarLength[motherN];
				
				W = 2 * polarLength[motherN];
				
				
//TODO:  This is all in bad shape!!!
				double edgeAngle = 0;
				/*Math.atan(edgewidth/polarLength[motherN])/2;
				if (edgeAngle <0)
					edgeAngle = -edgeAngle;*/
			Arc2D arc;
		/*
		 * 	if (angle[motherN]>angle[node])
				arc = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[motherN]+edgeAngle)), convertToDoubleDegrees(angle[motherN] -angle[node]), Arc2D.OPEN); 
			else
				arc = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[node]+edgeAngle)), convertToDoubleDegrees(angle[node] -angle[motherN]), Arc2D.OPEN); 
		*/
			if (angle[motherN]<angle[node])
				arc = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[motherN]+edgeAngle)), convertToDoubleDegrees(angle[motherN] -angle[node]), Arc2D.OPEN); 
			else
				arc = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[node]+edgeAngle)), convertToDoubleDegrees(angle[node] -angle[motherN]), Arc2D.OPEN); 
			//arc = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[motherN])), convertToDegrees(angle[motherN] -angle[node]), Arc2D.OPEN); 
			if (arc!=null) {
				BasicStroke wideStroke = new BasicStroke(edgewidth);
				Graphics2D g2 = (Graphics2D)g;
				g2.setStroke(wideStroke);
				g2.draw(arc);
				g2.setStroke(defaultStroke);
			}
			/*
			 * Arc2D arcE;
			if (angle[motherN]>angle[node])
				g.setColor(Color.red);
			else
				g.setColor(Color.green);
				arcE= new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[node])), 0.01, Arc2D.OPEN); 
				
				if (angle[motherN]>angle[node])
					arcE = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[motherN]-edgeAngle)), 0.1, Arc2D.OPEN); 
				else
					arcE = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[node]-edgeAngle)), 0.1, Arc2D.OPEN); 
				
				
			if (arcE!=null) {
				BasicStroke wideStroke = new BasicStroke(edgewidth);
				Graphics2D g2 = (Graphics2D)g;
				
				g2.setStroke(wideStroke);
				g2.draw(arcE);
				g2.setStroke(defaultStroke);
			}
			*/
		}
		catch (Throwable e){
			int L, R, T, B;
			R = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.x + polarLength[motherN]);
			L = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.x - polarLength[motherN]);
			T =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.y - polarLength[motherN]);
			B =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.y + polarLength[motherN]);
			
		for (int i=inset; i<edgewidth-inset; i++)
			g.drawArc(L+i, T+i, R-L-i-i, B-T-i-i, convertToDegrees(myAngleToTheirs(angle[motherN]))-1, convertToDegrees(angle[motherN] -angle[node]));
		}
	}
	/*----------------------------------------------------------------------------*/
	private double convertToDoubleDegrees(double angle){
		if (angle<0)
			return -Math.round((-angle/2/Math.PI) * 360);
		else
			return Math.round((angle/2/Math.PI) * 360);
	}
	/*----------------------------------------------------------------------------*/
	private int convertToDegrees(double angle){
		if (angle<0)
			return (int)-Math.round((-angle/2/Math.PI) * 360);
		else
			return (int)Math.round((angle/2/Math.PI) * 360);
	}
	/*----------------------------------------------------------------------------*/
	private double myAngleToTheirs(double myAngle) {
		return (-myAngle +Math.PI/2);
	}
	/*----------------------------------------------------------------------------*/
	private   void drawClade(Tree tree, int node, Graphics g) {
		if (tree.nodeExists(node)) {
			long c = tree.getAssociatedLong(ColorDistribution.colorNameReference, node);
			g.setColor(treeDisplay.getBranchColor(node));
			if (tree.getRooted() || tree.getRoot()!=node)
				drawOneBranch(tree, node, g);

			if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawClade( tree, d, g);
		}
	}
	/*----------------------------------------------------------------------------*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
	        if (MesquiteTree.OK(tree)) {
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	g.setColor(treeDisplay.branchColor);
	       	 	drawClade(tree, drawnRoot, g);  
	       	 }
	   }
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
	        if (MesquiteTree.OK(tree)) {
			if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
			if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
			if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	if (!tree.nodeExists(getDrawnRoot()))
	        		setDrawnRoot(tree.getRoot());
			ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, getDrawnRoot(),  treeDisplay.getField()); //Graphics g removed as parameter May 02
		}
	}
	/*----------------------------------------------------------------------------*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
	}
	/*----------------------------------------------------------------------------*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
	}
	/*----------------------------------------------------------------------------*/
	public  int findTerminalBox(Tree tree, int drawnRoot, int x, int y){
		return -1;
	}
	/*_________________________________________________*/
	private boolean ancestorIsTriangled(Tree tree, int node) {
		if (!tree.nodeExists(node))
			return false;
		if (tree.getAssociatedBit(triangleNameRef, tree.motherOfNode(node)))
			return true;
		if (tree.getRoot() == node || tree.getSubRoot() == node)
			return false;
		return ancestorIsTriangled(tree, tree.motherOfNode(node));
	}
	/*----------------------------------------------------------------------------*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			Color c = g.getColor();
			if (treeDisplay.getOrientation()==TreeDisplay.CIRCULAR) {
				int numColors = colors.getNumColors();
				for (int i=0; i<numColors; i++) {
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(fillBranchPoly[node]);
					int motherN= tree.motherOfNode(node);
					double[] polarLength= ownerModule.nodeLocsTask.polarLength;
					double[] angle= ownerModule.nodeLocsTask.angle;
					drawArc(g, polarLength, angle, node, motherN, 1);
					/*
					int L, R, T, B;
					R = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.x + polarLength[motherN]);
					L = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.x - polarLength[motherN]);
					T =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.y - polarLength[motherN]);
					B =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.y + polarLength[motherN]);

					for (int j=1; j<edgewidth-1; j++)
						g.drawArc(L+j, T+j, R-L-j-j, B-T-j-j, convertToDegrees(myAngleToTheirs(angle[motherN])), convertToDegrees(angle[motherN] -angle[node]));
					*/
				}
			}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			g.fillPolygon(fillBranchPoly[node]);
			int motherN= tree.motherOfNode(node);
			double[] polarLength= ownerModule.nodeLocsTask.polarLength;
			double[] angle= ownerModule.nodeLocsTask.angle;
			drawArc(g, polarLength, angle, node, motherN, 1);
			/*int L, R, T, B;
			R = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.x + polarLength[motherN]);
			L = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.x - polarLength[motherN]);
			T =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.y - polarLength[motherN]);
			B =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.y + polarLength[motherN]);

			for (int i=1; i<edgewidth-1; i++)
				g.drawArc(L+i, T+i, R-L-i-i, B-T-i-i, convertToDegrees(myAngleToTheirs(angle[motherN])), convertToDegrees(angle[motherN] -angle[node]));
			*/
			}
	}
	   
	public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if (tree==null || xValue==null || yValue==null)
			return;
		if (!tree.nodeExists(N))
			return;
		int motherNode = tree.motherOfNode(N);
		xValue.deassignAllValues();
		yValue.deassignAllValues();
		angle.setToUnassigned();
		if (tree.nodeExists(motherNode)) {
			double[] angles= ownerModule.nodeLocsTask.angle;
			double[] polarLength= ownerModule.nodeLocsTask.polarLength;
			MesquiteInteger xN = new MesquiteInteger(this.x[N]);
			MesquiteInteger yN = new MesquiteInteger(this.y[N]);
			double thisAngle = 3*Math.PI/2+angles[N];
			GraphicsUtil.translateAlongAngle(xN,yN, thisAngle,(int)(Math.abs(polarLength[N]-polarLength[motherNode])/2));
			angle.setValue(angles[N]);
			xValue.setValue(xN.getValue());
			yValue.setValue(yN.getValue());
		}
	}
	/*_________________________________________________*/
	public Polygon nodePoly(int node) {
		double[] angle= ownerModule.nodeLocsTask.angle;
		
		//drawArc(g, polarLength, angle, node, motherN, 1);

		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int halfNodeWidth = getNodeWidth()/2;
		MesquiteInteger startX = new MesquiteInteger(x[node]);
		MesquiteInteger startY = new MesquiteInteger(y[node]);
		GraphicsUtil.translateAlongAngle(startX,startY, angle[node],offset);
		return GraphicsUtil.createAngledSquare(startX.getValue(),startY.getValue(),-angle[node],getNodeWidth());
		}
	/*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		Polygon nodeP = nodePoly(node);
		if (nodeP!=null && nodeP.contains(x,y))
			return true;
		else
			return false;
	}
	/*_________________________________________________*/
	private void ScanBranches(Tree tree, int node, int x, int y, MesquiteDouble fraction)
	{
		if (foundBranch==0) {
			if (fillBranchPoly[node].contains(x, y) || inNode(node,x,y)) {

				foundBranch = node;
				if (fraction!=null)
					if (inNode(node,x,y))
						fraction.setValue(ATNODE);
					else {
						int motherNode = tree.motherOfNode(node);
						fraction.setValue(EDGESTART);  //TODO: this is just temporary: need to calculate value along branch.
						if (tree.nodeExists(motherNode)) {
							double[] angle= ownerModule.nodeLocsTask.angle;
							double[] polarLength= ownerModule.nodeLocsTask.polarLength;
							MesquiteInteger startX = new MesquiteInteger(this.x[node]);
							MesquiteInteger startY = new MesquiteInteger(this.y[node]);
							int offset = (getNodeWidth()-getEdgeWidth())/2;
							double thisAngle = 3*Math.PI/2+angle[node];
							GraphicsUtil.translateAlongAngle(startX,startY, thisAngle,(int)Math.abs(polarLength[node]-polarLength[motherNode]));
							fraction.setValue(GraphicsUtil.fractionAlongLine(x, y, startX.getValue(), startY.getValue(), this.x[node], this.y[node],false,false));
						}
					}
			}

			if (!tree.getAssociatedBit(triangleNameRef,node))
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					ScanBranches(tree, d, x, y, fraction);

		}
	}
	/*_________________________________________________*/
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) { 
	        if (MesquiteTree.OK(tree) && ready) {
	        	foundBranch=0;
	       		 ScanBranches(tree, drawnRoot, x, y, fraction);
	       		 if (foundBranch == tree.getRoot() && !tree.getRooted())
	       		 	return 0;
	       		 else
	       		 return foundBranch;
	       	}
	       	return 0;
	}
	
	/*_________________________________________________*/
	public void reorient(int orientation) {
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
class PolarCoord {
	public double length, angle;
}



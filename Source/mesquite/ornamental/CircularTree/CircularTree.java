/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.DrawTree;
import mesquite.lib.duties.NodeLocsCircle;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.GraphicsUtil;

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
		treeDisplay.collapsedCladeNameAtLeftmostAncestor = true;
		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	/** Returns true if other modules can control the orientation */
	public boolean allowsReorientation(){
		return false;
	}
	public Vector getDrawings(){
		return drawings;
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
					treeDrawing.treeDisplay.setMinimumTaxonNameDistanceFromTip(newWidth, 6); 
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
	public Path2D.Double[] branchPoly;
	public Path2D.Double[] fillBranchPoly;
	BasicStroke defaultStroke;

	private int foundBranch;

	public CircleTreeDrawing (TreeDisplay treeDisplay, int numTaxa, CircularTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		try{
			defaultStroke = new BasicStroke();
		}
		catch (Throwable t){
		}
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		treeDisplay.setOrientation(TreeDisplay.CIRCULAR);
		oldNumTaxa = numTaxa;
		namesFollowLines = true;
		treeDisplay.setMinimumTaxonNameDistanceFromTip(edgewidth, 6); //better if only did this if tracing on
		ready = true;
	}
	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Path2D.Double[numNodes];
		fillBranchPoly= new Path2D.Double[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new Path2D.Double();
			fillBranchPoly[i] = new Path2D.Double();
		}
	}
	/*----------------------------------------------------------------------------*/
	private void nodePolarToLoc (double polarlength, double angle, Point2D center, Point2D.Double loc){
		loc.setLocation(1.0*center.getX() + polarlength * Math.sin(angle),1.0*center.getY() - polarlength * Math.cos(angle));
	}
	/*----------------------------------------------------------------------------*/
	private void nodeLocToPolar (Point loc, Point center, PolarCoord polar){
		polar.length = Math.sqrt((loc.x-center.x) *(loc.x-center.x) + (center.y-loc.y)*(center.y-loc.y));
		polar.angle = Math.asin((loc.x-center.x)/polar.length);
	}
	//	makeSlantedRectangle(branchPoly[node], loc, polarLength[node]-polarLength[motherN]+edgewidth, angle[node], edgewidth);
	private void makeBranchPoly(Path2D.Double poly, double[] polarlength, double[] angle, int node, int motherN, int width){
		poly.reset();
		Point2D.Double loc = new Point2D.Double();
		Point2D.Double w = new Point2D.Double();
		nodePolarToLoc (width, angle[node] + Math.PI/2, ownerModule.nodeLocsTask.treeCenter, w);
		double wx2p = (w.getX() -ownerModule.nodeLocsTask.treeCenter.getX())/2;
		if (wx2p == 0)
			wx2p = 1;
		double wx2m = -wx2p;
		double wy2p = (w.getY() -ownerModule.nodeLocsTask.treeCenter.getY())/2;
		if (wy2p == 0)
			wy2p = 1;
		double wy2m = -wy2p;
		//if (wx2p == 0 && wy2p == 0)
		//	wx2p = 1;
		nodePolarToLoc(polarlength[node], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		/**/
		poly.moveTo(loc.getX() + wx2p, loc.getY() + wy2p);
		poly.lineTo(loc.getX() + wx2m, loc.getY() + wy2m);
		nodePolarToLoc(polarlength[motherN], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		poly.lineTo(loc.getX() + wx2m, loc.getY() + wy2m);
		poly.lineTo(loc.getX() + wx2p, loc.getY() + wy2p);
		nodePolarToLoc(polarlength[node], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		poly.lineTo(loc.getX() + wx2p, loc.getY() + wy2p);
		/**
 		poly.addPoint(loc.x + wx, loc.y + wy);
		poly.addPoint(loc.x, loc.y);
		nodePolarToLoc(polarlength[motherN], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		poly.addPoint(loc.x, loc.y);
		poly.addPoint(loc.x + wx, loc.y + wy);
		nodePolarToLoc(polarlength[node], angle[node], ownerModule.nodeLocsTask.treeCenter, loc);
		poly.addPoint(loc.x + wx, loc.y + wy);
/**/
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
		Point2D.Double loc = new Point2D.Double(0.0, 0.0);
		double[] polarLength= ownerModule.nodeLocsTask.polarLength;
		double[] angle= ownerModule.nodeLocsTask.angle;

		lineTipX[node]= treeDisplay.getTreeDrawing().x[node];
		lineTipY[node]= treeDisplay.getTreeDrawing().y[node];

		nodePolarToLoc(polarLength[motherN]-edgewidth, angle[node], ownerModule.nodeLocsTask.treeCenter, loc);

		lineBaseX[node]= loc.getX();
		lineBaseY[node]= loc.getY();

		makeBranchPoly(branchPoly[node],polarLength, angle, node, motherN, edgewidth);
		makeBranchPoly(fillBranchPoly[node],polarLength, angle, node, motherN, edgewidth-2); //TODO: include arc into fillBranchPoly

		GraphicsUtil.fill(g,branchPoly[node]);

		double L, R, T, B;
		drawArc(g, polarLength, angle, node, motherN, 0);


		if (false && tree.isCollapsedClade(node)) {
			double highestTerminal = findHighest(tree, node, polarLength);
			R = ownerModule.nodeLocsTask.treeCenter.getX() + highestTerminal;
			L = ownerModule.nodeLocsTask.treeCenter.getX() - highestTerminal;
			T =  ownerModule.nodeLocsTask.treeCenter.getY()  - highestTerminal;
			B =  ownerModule.nodeLocsTask.treeCenter.getY()  + highestTerminal;

			for (int i=0; i<3; i++)
				GraphicsUtil.drawArc(g,L+i, T+i, R-L-i-i, B-T-i-i, convertToDegrees(myAngleToTheirs(angle[tree.leftmostTerminalOfNode(node)])), convertToDegrees(-angle[tree.rightmostTerminalOfNode(node)] +angle[tree.leftmostTerminalOfNode(node)]));

			for (int j=0; j<2; j++)
				for (int i=0; i<2; i++) {
					GraphicsUtil.drawLine(g,x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
					//g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
					GraphicsUtil.drawLine(g,x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
				}
		}
		if (emphasizeNodes()) {
			Color prev = g.getColor();
			g.setColor(Color.red);//for testing
			GraphicsUtil.fillRect(g,x[node]-2, y[node]-2, 4, 4);
			//g.fillPolygon(nodePoly(node));
			g.setColor(prev);
		}
	}
	private void drawArc(Graphics g, double[] polarLength, double[] angle, int node, int motherN, int inset){

		if (g instanceof Graphics2D)
			try {
				double L, W, T;
				L = ownerModule.nodeLocsTask.treeCenter.getX() - polarLength[motherN];
				T =  ownerModule.nodeLocsTask.treeCenter.getY() - polarLength[motherN];

				W = 2 * polarLength[motherN];

				double edgeAngle = 0;
				Arc2D arc;

				if (angle[motherN]<angle[node])
					arc = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[motherN]+edgeAngle)), convertToDoubleDegrees(angle[motherN] -angle[node]), Arc2D.OPEN); 
				else
					arc = new Arc2D.Double(L, T, W, W, convertToDoubleDegrees(myAngleToTheirs(angle[node]+edgeAngle)), convertToDoubleDegrees(angle[node] -angle[motherN]), Arc2D.OPEN); 
				if (arc!=null) {
					BasicStroke wideStroke = new BasicStroke(edgewidth);
					Graphics2D g2 = (Graphics2D)g;
					g2.setStroke(wideStroke);
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.draw(arc);
					g2.setStroke(defaultStroke);
				}

			}
		catch (Throwable e){
			int L, R, T, B;
			R = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.getX() + polarLength[motherN]);
			L = (int)Math.round(ownerModule.nodeLocsTask.treeCenter.getX() - polarLength[motherN]);
			T =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.getY() - polarLength[motherN]);
			B =  (int)Math.round(ownerModule.nodeLocsTask.treeCenter.getY() + polarLength[motherN]);

			for (int i=inset; i<edgewidth-inset; i++)
				GraphicsUtil.drawArc(g,L+i, T+i, R-L-i-i, B-T-i-i, convertToDegrees(myAngleToTheirs(angle[motherN]))-1, convertToDegrees(angle[motherN] -angle[node]));
		}
	}
	/*----------------------------------------------------------------------------*/
	private double convertToDoubleDegrees(double angle){
		if (angle<0)
			return -(-angle/2/Math.PI) * 360;
		else
			return (angle/2/Math.PI) * 360;
	}
	/*----------------------------------------------------------------------------*/
	private double convertToDegrees(double angle){
		if (angle<0)
			return -(-angle/2/Math.PI) * 360;
		else
			return (angle/2/Math.PI) * 360;
	}
	/*----------------------------------------------------------------------------*/
	private double myAngleToTheirs(double myAngle) {
		return (-myAngle +Math.PI/2);
	}
	/*----------------------------------------------------------------------------*/
	private   void drawClade(Tree tree, int node, Graphics g) {
		if (tree.nodeExists(node)) {
			g.setColor(treeDisplay.getBranchColor(node));
			if (tree.isVisibleEvenIfInCollapsed(node))
				drawOneBranch(tree, node, g);

			if (tree.nodeIsInternal(node))
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					drawClade( tree, d, g);
		}
	}
	/*----------------------------------------------------------------------------*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
		if (MesquiteTree.OK(tree)) {
			treeDisplay.setMinimumTaxonNameDistanceFromTip(edgewidth, 6); //better if only did this if tracing on
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
			ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, getDrawnRoot()); //Graphics g removed as parameter May 02
		}
	}
	/*_________________________________________________*/
	private Shape[] getTerminalBoxes(Tree tree, int node, Graphics g, int numBoxes) {
		double slope = (lineBaseY[node]*1.0-lineTipY[node])*1.0/(lineBaseX[node]*1.0-lineTipX[node]);
		double radians = Math.atan(slope);
		boolean right = lineTipX[node]>lineBaseX[node];
		Path2D.Double[] boxes = new Path2D.Double[numBoxes];
		for (int i=0; i<numBoxes; i++){
			boxes[i] = new Path2D.Double();
		}
		if (numBoxes==1)
			boxes[0] = (Path2D.Double)getTerminalBox(tree,node, g, 1, 1);
		return boxes;
	}
	/*_________________________________________________*/
	double getXExtension (double radians, double separation) {
		return (Math.cos(radians)*separation);
	}
	/*_________________________________________________*/
	double getYExtension (double radians, double separation) {
		return (Math.sin(radians)*separation);

	}
	/*_________________________________________________*/
	private Shape getTerminalBox(Tree tree, int node, Graphics g, int numBoxes, int division) {
		double slope = (lineBaseY[node]*1.0-lineTipY[node])*1.0/(lineBaseX[node]*1.0-lineTipX[node]);
		double radians = Math.atan(slope);
		double tipSlope = 1.0/slope;
		double tipRadians = Math.PI/2 - radians;
		boolean right = lineTipX[node]>lineBaseX[node];
		double edgewidth = getEdgeWidth();
		double totalBoxLength = 3*getEdgeWidth()/4;
		if (totalBoxLength<numBoxes) totalBoxLength=numBoxes;
		double individualBoxLength = totalBoxLength/numBoxes;
		if (individualBoxLength<1) individualBoxLength=1.0;
		double separation = 4 + individualBoxLength*(division-1);

		double startX = 0.0;
		double startY = 0.0;
		double secondX = 0.0;
		double secondY = 0.0;
		double thirdX = 0.0;
		double thirdY = 0.0;
		double fourthX = 0.0;
		double fourthY = 0.0;

		Path2D.Double box = new Path2D.Double();
		//		Path2D.Double box = GraphicsUtil.createAngledSquare(lineTipX[node], lineTipX[node],radians, 4);
		if (right){
			double firstTipX = lineTipX[node] - Math.sin(radians)*edgewidth/2;
			double firstTipY = lineTipY[node] + Math.cos(radians)*edgewidth/2;
			startX = firstTipX+ getXExtension(radians, separation);
			startY = firstTipY+ getYExtension(radians, separation);
			double otherTipX =firstTipX + Math.sin(radians)*edgewidth;
			double otherTipY = firstTipY - Math.cos(radians)*edgewidth;
			secondX = otherTipX+getXExtension(radians, separation);
			secondY = otherTipY+ getYExtension(radians, separation);
			thirdX = otherTipX+getXExtension(radians, separation+individualBoxLength);
			thirdY = otherTipY+ getYExtension(radians, separation+individualBoxLength);
			fourthX = firstTipX+getXExtension(radians, separation+individualBoxLength);
			fourthY = firstTipY+ getYExtension(radians, separation+individualBoxLength);
		} else {
			double firstTipX = lineTipX[node] - Math.sin(radians)*edgewidth/2;
			double firstTipY = lineTipY[node] + Math.cos(radians)*edgewidth/2;
			startX = firstTipX- getXExtension(radians, separation);
			startY = firstTipY - getYExtension(radians, separation);
			double otherTipX = firstTipX + Math.sin(radians)*edgewidth;
			double otherTipY = firstTipY - Math.cos(radians)*edgewidth;
			secondX = otherTipX-getXExtension(radians, separation);
			secondY = otherTipY- getYExtension(radians, separation);
			thirdX = otherTipX-getXExtension(radians, separation+individualBoxLength);
			thirdY = otherTipY- getYExtension(radians, separation+individualBoxLength);
			fourthX = firstTipX-getXExtension(radians, separation+individualBoxLength);
			fourthY = firstTipY- getYExtension(radians, separation+individualBoxLength);
		}
		box.moveTo(startX, startY);
		box.lineTo(secondX, secondY);
		box.lineTo(thirdX, thirdY);
		box.lineTo(fourthX, fourthY);
		box.lineTo(startX, startY);


		return box;
	}
	
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		int numColors = colors.getNumColors();
		if (numColors == 0) numColors = 1;
		Shape[] boxes = getTerminalBoxes(tree,node,g, numColors);
		for (int i=0; i<colors.getNumColors(); i++) {
			Color color;
			if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null){
				g.setColor(color);
				GraphicsUtil.fill(g,boxes[i]);
			}
			//GraphicsUtil.fill(g,box.getX() + (i*box.getWidth()/colors.getNumColors()), box.getY(), box.getWidth()-  (i*box.getWidth()/numColors), box.getHeight());
		}
		g.setColor(treeDisplay.getBranchColor(node));
		Shape box = getTerminalBox(tree,node,g,1,1);
		GraphicsUtil.draw(g, box);
	}
	/*----------------------------------------------------------------------------*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && tree.isVisibleEvenIfInCollapsed(node)) {
			Color c = g.getColor();
			if (treeDisplay.getOrientation()==TreeDisplay.CIRCULAR) {
				int numColors = colors.getNumColors();
				if (numColors == 1){
					Color color;
					if ((color = colors.getColor(0, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					GraphicsUtil.fill(g,fillBranchPoly[node]);
					int motherN= tree.motherOfNode(node);
					double[] polarLength= ownerModule.nodeLocsTask.polarLength;
					double[] angle= ownerModule.nodeLocsTask.angle;
					drawArc(g, polarLength, angle, node, motherN, 1);
				}
				else if (numColors>0) {
					Color colorA = colors.getColor(0, !tree.anySelected()|| tree.getSelected(node));
					Color colorB = colors.getColor(numColors-1, !tree.anySelected()|| tree.getSelected(node));
					float x1 = (float)(treeDisplay.getTreeDrawing().x[node]);
					float x2 = x1 + 5;
					float y1 = (float)(treeDisplay.getTreeDrawing().y[node]); 
					float y2 = y1+5;
					GradientPaint grad = new GradientPaint(x1,y1, colorA, x2, y2, colorB, true);
					Graphics2D g2 =null;
					Paint oldPaint = null;
					if (g instanceof Graphics2D){
						g2 = (Graphics2D)g;
						oldPaint = g2.getPaint();
						g2.setPaint(grad);}

					GraphicsUtil.fill(g,fillBranchPoly[node]);
					if (g instanceof Graphics2D){
						g2.setPaint(oldPaint);
					}
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
		if (node>0 && tree.isVisibleEvenIfInCollapsed(node)) {
			GraphicsUtil.fill(g,fillBranchPoly[node]);
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
			MesquiteDouble xN = new MesquiteDouble(this.x[N]);
			MesquiteDouble yN = new MesquiteDouble(this.y[N]);
			double thisAngle = 3*Math.PI/2+angles[N];
			GraphicsUtil.translateAlongAngle(xN,yN, thisAngle,(int)(Math.abs(polarLength[N]-polarLength[motherNode])/2));
			angle.setValue(angles[N]);
			xValue.setValue(xN.getValue());
			yValue.setValue(yN.getValue());
		}
	}
	/*_________________________________________________*/
	public Path2D nodePoly(int node) {
		double[] angle= ownerModule.nodeLocsTask.angle;

		//drawArc(g, polarLength, angle, node, motherN, 1);

		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int halfNodeWidth = getNodeWidth()/2;
		MesquiteDouble startX = new MesquiteDouble(x[node]);
		MesquiteDouble startY = new MesquiteDouble(y[node]);
		GraphicsUtil.translateAlongAngle(startX,startY, angle[node],offset);
		return GraphicsUtil.createAngledSquare(startX.getValue(),startY.getValue(),-angle[node],getNodeWidth());
	}
	/*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		Path2D nodeP = nodePoly(node);
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
							MesquiteDouble startX = new MesquiteDouble(this.x[node]);
							MesquiteDouble startY = new MesquiteDouble(this.y[node]);
							int offset = (getNodeWidth()-getEdgeWidth())/2;
							double thisAngle = 3*Math.PI/2+angle[node];
							GraphicsUtil.translateAlongAngle(startX,startY, thisAngle,(int)Math.abs(polarLength[node]-polarLength[motherNode]));
							fraction.setValue(GraphicsUtil.fractionAlongLine(x, y, startX.getValue(), startY.getValue(), this.x[node], this.y[node],false,false));
						}
					}
			}

			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				ScanBranches(tree, d, x, y, fraction);

		}
	}
	/*_________________________________________________*/
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) { 
		if (MesquiteTree.OK(tree) && ready) {
			foundBranch=0;
			ScanBranches(tree, drawnRoot, x, y, fraction);
			if (!tree.isVisibleEvenIfInCollapsed(foundBranch))
				return 0;
			else {
				if (tree.withinCollapsedClade(foundBranch))
					foundBranch = tree.deepestCollapsedAncestor(foundBranch);
				return foundBranch;
			}
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
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgewidth;
	}
}
class PolarCoord {
	public double length, angle;
}



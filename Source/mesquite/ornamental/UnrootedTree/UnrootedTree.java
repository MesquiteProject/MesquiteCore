/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.UnrootedTree;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class UnrootedTree extends DrawTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsUnrooted.class, getName() + "  needs a method to calculate node locations.",
				"The method to calculate node locations is arranged initially");
	}
	/*.................................................................................................................*/

	NodeLocsUnrooted nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	Vector drawings;
	int oldEdgeWidth = 6;

	/*.................................................................................................................*/
	public boolean loadModule() {
		return false;
	}


	/*----------------------------------------------------------------------------*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsUnrooted)hireEmployee(NodeLocsUnrooted.class, "Calculator of node locations");
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
		UnrootedTreeDrawing treeDrawing =  new UnrootedTreeDrawing (treeDisplay, numTaxa, this);
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
					UnrootedTreeDrawing treeDrawing = (UnrootedTreeDrawing)obj;
					treeDrawing.setEdgeWidth(newWidth);
					treeDrawing.treeDisplay.setMinimumTaxonNameDistance(newWidth, 6); 
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
		return "Unrooted Tree Form";
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Draws trees as if they had no root." ;
	}
	/*.................................................................................................................*/
}

/* ======================================================================== */
class UnrootedTreeDrawing extends TreeDrawing  {

	public int highlightedBranch, branchFrom;
	public int xFrom, yFrom, xTo, yTo;
	public UnrootedTree ownerModule;
	public int edgewidth = 6;
	int oldNumTaxa = 0;
	public static final int inset=1;
	private boolean ready=false;
	public Path2D.Double[] branchPoly;
	public Path2D.Double[] fillBranchPoly;
	BasicStroke defaultStroke;

	private int foundBranch;
	NameReference triangleNameRef;

	public UnrootedTreeDrawing (TreeDisplay treeDisplay, int numTaxa, UnrootedTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		try{
			defaultStroke = new BasicStroke();
		}
		catch (Throwable t){
		}
		triangleNameRef = NameReference.getNameReference("triangled");
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		treeDisplay.setOrientation(TreeDisplay.UNROOTED);
		oldNumTaxa = numTaxa;
		namesFollowLines = true;
		treeDisplay.setMinimumTaxonNameDistance(edgewidth, 6); //better if only did this if tracing on
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

		//GraphicsUtil.drawCross(g, treeDisplay.getTreeDrawing().x[node],treeDisplay.getTreeDrawing().y[node], 2);
		lineTipX[node]= treeDisplay.getTreeDrawing().x[node];
		lineTipY[node]= treeDisplay.getTreeDrawing().y[node];
		lineBaseX[node]= treeDisplay.getTreeDrawing().x[motherN];
		lineBaseY[node]= treeDisplay.getTreeDrawing().y[motherN];

		//		private void makeSlantedRectangle(Polygon poly, double[] polarlength, double[] angle, int node, int motherN, int width){
//		makeBranchPoly(branchPoly[node],polarLength, angle, node, motherN, edgewidth);
//		makeBranchPoly(fillBranchPoly[node],polarLength, angle, node, motherN, edgewidth-2); //TODO: include arc into fillBranchPoly

		//	makeSlantedRectangle(fillBranchPoly[node], loc, polarLength[node]-polarLength[motherN]+edgewidth-2, angle[node], edgewidth-2);
		//GraphicsUtil.fill(g,branchPoly[node]);

		GraphicsUtil.drawLine(g, x[motherN], y[motherN], x[node], y[node], edgewidth);




		double L, R, T, B;
//		drawArc(g, polarLength, angle, node, motherN, 0);


		if (tree.getAssociatedBit(triangleNameRef,node)) {
			double highestTerminal = findHighest(tree, node, polarLength);
			R = ownerModule.nodeLocsTask.treeCenter.getX() + highestTerminal;
			L = ownerModule.nodeLocsTask.treeCenter.getX() - highestTerminal;
			T =  ownerModule.nodeLocsTask.treeCenter.getY()  - highestTerminal;
			B =  ownerModule.nodeLocsTask.treeCenter.getY()  + highestTerminal;

			for (int i=0; i<3; i++)
				GraphicsUtil.drawArc(g,L+i, T+i, R-L-i-i, B-T-i-i, convertToDegrees(myAngleToTheirs(angle[tree.leftmostTerminalOfNode(node)])), convertToDegrees(-angle[tree.rightmostTerminalOfNode(node)] +angle[tree.leftmostTerminalOfNode(node)]));

			for (int j=0; j<2; j++)
				for (int i=0; i<2; i++) {
					//GraphicsUtil.drawLine(g,x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
					//g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
					//GraphicsUtil.drawLine(g,x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
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
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
	private  void drawClade(Tree tree, int node, Graphics g) {
		if (tree.nodeExists(node)) {
			long c = tree.getAssociatedLong(ColorDistribution.colorNameReference, node);
			g.setColor(treeDisplay.getBranchColor(node));
			if (tree.getRoot()!=node)  //TODO: only draw if requested  tree.getRooted() || 
				drawOneBranch(tree, node, g);

			if (!tree.getAssociatedBit(triangleNameRef,node))
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					drawClade( tree, d, g);
		}
	}
	/*----------------------------------------------------------------------------*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
	/*	Rectangle rect = treeDisplay.getField();
		int inset = 10;
		g.setColor(Color.yellow);
		GraphicsUtil.fillRect(g,rect.getX()+inset, rect.getY()+inset, rect.getWidth()-inset*2, rect.getHeight()-inset*2);
		*/
		if (MesquiteTree.OK(tree)) {
			treeDisplay.setMinimumTaxonNameDistance(edgewidth, 6); //better if only did this if tracing on
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			g.setColor(treeDisplay.branchColor);
			drawClade(tree, drawnRoot, g);  
		}
	}
	int branchEdgeWidth(int node, boolean isTouch){
		if (isTouch)
			return getNodeWidth();
		else
			return edgewidth;
	}
	private int getOffset(int width, int node) {
		return (width-branchEdgeWidth(node,false))/2;
	}

	/*_________________________________________________*/
	private void defineBranchPoly(int node, Path2D.Double poly, boolean isTouch, boolean internalNode, double Nx, double Ny, double mNx, double mNy) {
		if (poly!=null) {
			poly.reset();
			Nx -= getOffset(branchEdgeWidth(node,isTouch), node);
			mNx -= getOffset(branchEdgeWidth(node,isTouch), node);
			if (internalNode)  {
				poly.moveTo(Nx, Ny);  // move to node's position.
				poly.lineTo(Nx+branchEdgeWidth(node, isTouch)/2, Ny-branchEdgeWidth(node, isTouch)/2);//Ny+branchEdgeWidth(node, isTouch)/2 for down
				poly.lineTo(Nx+branchEdgeWidth(node, isTouch), Ny);
				poly.lineTo(mNx+branchEdgeWidth(node, isTouch), mNy);
				poly.lineTo(mNx, mNy);  // move to mother node.
				poly.lineTo(Nx, Ny);
			}
			else {
				poly.moveTo(Nx, Ny);
				poly.lineTo(Nx+branchEdgeWidth(node, isTouch), Ny);
				poly.lineTo(mNx+branchEdgeWidth(node, isTouch), mNy);
				poly.lineTo(mNx, mNy);
				poly.lineTo(Nx, Ny);
			}
		}
	}
	/*_________________________________________________*/
	private void calcBranchPolys(Tree tree, int node, Path2D.Double[] polys, boolean isTouch)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {  // it's not collapses into a triangle
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				calcBranchPolys(tree, d, polys, isTouch);
			defineBranchPoly(node, polys[node], isTouch, tree.nodeIsInternal(node), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		}
		else {
			Path2D.Double poly = polys[node];
			int mN = tree.motherOfNode(node);
			int leftN = tree.leftmostTerminalOfNode(node);
			int rightN = tree.rightmostTerminalOfNode(node);
			poly.reset();
			poly.moveTo(x[node], y[node]);
			poly.lineTo(x[leftN], y[leftN]);
			poly.lineTo(x[rightN]+branchEdgeWidth(node, isTouch), y[rightN]);
			poly.lineTo(x[node]+branchEdgeWidth(node, isTouch), y[node]);
			poly.lineTo(x[mN]+branchEdgeWidth(node, isTouch), y[mN]);
			poly.lineTo(x[mN], y[mN]);
			poly.lineTo(x[node], y[node]);
		}
	}
	/*_________________________________________________*/
	private void calcBranchPolys(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}

		
			calcBranchPolys(tree, drawnRoot, branchPoly, false);
			//calcBranchPolys(tree, drawnRoot, touchPoly, true);
			//UPCalcFillBranchPolys(tree, drawnRoot);
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
			calcBranchPolys(tree, getDrawnRoot());
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
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Shape box = getTerminalBox(tree,node,g,1,1);
		GraphicsUtil.fill(g, box);
		g.setColor(treeDisplay.getBranchColor(node));
		GraphicsUtil.draw(g, box);
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
		if (node>0 && (tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			Color c = g.getColor();
			if (treeDisplay.getOrientation()==TreeDisplay.UNROOTED) {
				int numColors = colors.getNumColors();
				for (int i=0; i<numColors; i++) {
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);

					int motherN= tree.motherOfNode(node);

					//GraphicsUtil.drawCross(g, treeDisplay.getTreeDrawing().x[node],treeDisplay.getTreeDrawing().y[node], 2);
					lineTipX[node]= treeDisplay.getTreeDrawing().x[node];
					lineTipY[node]= treeDisplay.getTreeDrawing().y[node];
					lineBaseX[node]= treeDisplay.getTreeDrawing().x[motherN];
					lineBaseY[node]= treeDisplay.getTreeDrawing().y[motherN];

					//		private void makeSlantedRectangle(Polygon poly, double[] polarlength, double[] angle, int node, int motherN, int width){
//					makeBranchPoly(branchPoly[node],polarLength, angle, node, motherN, edgewidth);
//					makeBranchPoly(fillBranchPoly[node],polarLength, angle, node, motherN, edgewidth-2); //TODO: include arc into fillBranchPoly

					//	makeSlantedRectangle(fillBranchPoly[node], loc, polarLength[node]-polarLength[motherN]+edgewidth-2, angle[node], edgewidth-2);
					//GraphicsUtil.fill(g,branchPoly[node]);

					GraphicsUtil.drawLine(g, x[motherN], y[motherN], x[node], y[node], edgewidth);

					
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
		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int halfNodeWidth = getNodeWidth()/2;
		double startX =0;
		double startY =0;
			startX = x[node];
			startY= y[node]-offset;

		Path2D poly = new Path2D.Double();
		poly.moveTo(startX,startY);
		poly.lineTo(startX+halfNodeWidth,startY+halfNodeWidth);
		poly.lineTo(startX,startY+getNodeWidth());
		poly.lineTo(startX-halfNodeWidth,startY+halfNodeWidth);
		poly.lineTo(startX,startY);
		return poly;
	}

	/*_________________________________________________*
	public Path2D nodePoly(int node) {
		double[] angle= ownerModule.nodeLocsTask.angle;

		//drawArc(g, polarLength, angle, node, motherN, 1);

		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int halfNodeWidth = getNodeWidth()/2;
		MesquiteDouble startX = new MesquiteDouble(x[node]);
		MesquiteDouble startY = new MesquiteDouble(y[node]);
		//GraphicsUtil.translateAlongAngle(startX,startY, angle[node],offset);  
		
		//return GraphicsUtil.createAngledSquare(startX.getValue(),startY.getValue(),-angle[node],getNodeWidth());
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



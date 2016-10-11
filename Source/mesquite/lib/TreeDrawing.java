/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.awt.geom.Point2D;

import mesquite.lib.duties.*;
import mesquite.trees.lib.TaxonPolygon;
/*===  Mesquite Basic Class Library:  Trees    ===*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules    ===*/
/* This sublibrary includes taxa and tree classes */



/* ��������������������������� trees ������������������������������� */
/* ======================================================================== */
/**This is the base class for the drawing of the tree.  It contains the information about
the number of nodes, and the x and y positions of the nodes.  It should be subclassed by tree drawing modules, 
so that they can add their own needed field (like polygons for the
tree branches.  TreeDrawings are typically used within TreeDisplays.*/

public abstract class TreeDrawing  {
	public final static double ATNODE=1.0;
	public final static double EDGESTART = 0.0;
	public final static double EDGEEND = 1.0;
	public final static int MINNODEWIDTH = 6;
	public final static int ACCEPTABLETOUCHWIDTH = 10;
	public final static boolean SHOWTOUCHPOLYS = false;
	public double[] x; //x positions of nodes
	public double[] y; //y positions of nodes
	public double[] z; //z positions of nodes (closeness to viewer, smaller numbers closer)
	public double[] lineBaseX; //base of line on which to draw labels etc.
	public double[] lineBaseY; 
	public double[] lineTipX; //tip of line on which to draw labels etc.
	public double[] lineTipY; 
	/**labelOrientation indicates where label is to be drawn w.r.t. node, in degrees. 0 = normal horizontal 
	writing to right of node, as would be done for a tree with orientation RIGHT.
	This does not represent simple rotation, i.e. 180 is on left side, but the writing is not upside down.  Thus
	0 would be appropriate for tree oriented RIGHT, 90 for tree DOWN, 180 for tree LEFT, 270 for tree UP */
	public int[] labelOrientation; 
	
	/** namesFollowLines indicates whether orientation of names follows the base to tip line of the branch.
	Supercedes labelOrientation for terminals and permits Java2D rotation of taxon names in circular tree */
	public boolean namesFollowLines = false;

	/** namesAngle indicates rotation of names from default in radians  (useful only for Java 1.2 or higher)*/
	public double namesAngle = MesquiteDouble.unassigned;

	public int numNodes;
	public TreeDisplay treeDisplay;//TODO: many of these should be private fields!!!
	private int enableTerminalBoxesRequests = 0;
	private int drawnRoot=-1;
	public static long totalCreated = 0;
	public TaxonPolygon[] namePolys;
	
	public TreeDrawing (TreeDisplay treeDisplay, int numNodes) {
		this.treeDisplay = treeDisplay;
		totalCreated++;
		resetNumNodes(numNodes);
	}
	public void resetNumNodes(int numNodes){
		if (this.numNodes == numNodes && x != null)
			return;
		this.numNodes=numNodes;
		totalCreated++;
		x = new double[numNodes];
		y = new double[numNodes];
		z = new double[numNodes];
		labelOrientation = new int[numNodes];
		lineBaseX = new double[numNodes];
		lineBaseY = new double[numNodes];
		lineTipX = new double[numNodes];
		lineTipY = new double[numNodes];
		for (int i=0; i<numNodes; i++) {
			x[i]=0;
			y[i]=0;
			z[i]=MesquiteInteger.unassigned;
			lineBaseX[i]=0;
			lineBaseY[i]=0;
			lineTipX[i]=0;
			lineTipY[i]=0;
			labelOrientation[i] = MesquiteInteger.unassigned;
		}
	}

	public double getX(int node){
		return x[node];
	}
	public double getY(int node){
		return y[node];
	}
	public int getDrawnRoot(){
		return drawnRoot;
	}
	public void setDrawnRoot(int node){
		drawnRoot = node;
	}
	public double getBranchCenterX(int node){
		return Math.abs(lineBaseX[node] + lineTipX[node])/2;
}
	public double getBranchCenterY(int node){
		return Math.abs(lineBaseY[node] + lineTipY[node])/2;
}
	
	public double getNodeValueTextBaseX(int node, int edgewidth,  int stringwidth, int fontHeight, boolean horizontalText){
		double baseX = x[node];
		if (horizontalText){
			baseX = baseX - stringwidth/2;
		}
		else {
			baseX = baseX - fontHeight*2;
		}
		return baseX;
	}
	public double getNodeValueTextBaseY(int node, int edgewidth, int stringwidth, int fontHeight, boolean horizontalText){
		double baseY = y[node];
		if (horizontalText){
			baseY = baseY - fontHeight;
		}
		else {
			baseY = baseY + stringwidth/2;
		}
		return baseY;
	}


	/** Sets the tree.  This is done outside of a paint() call, and is the place that any complex calculations should be performed! */
	public abstract void recalculatePositions(Tree tree) ;

	/** Draw tree in graphics context */
	public abstract void drawTree(Tree tree, int drawnRoot, Graphics g) ;
	
	/** Fill branch N with current color of graphics context */
	public abstract void fillBranch(Tree tree, int N, Graphics g);
	

	/*_________________________________________________*/
	/** Does the basic inverting of the color of a branch **/
	public  void fillBranchInverted (Tree tree, int N, Graphics g) {
		if (GraphicsUtil.useXORMode(g, true))  {
			g.setColor(Color.black);
			g.setXORMode(Color.white);  //for some reason color makes no difference in MacOS, but is inversion color in Win95 
		//	GraphicsUtil.setToXOR(g);
			//	g.setColor(Color.yellow);
			try{
				fillBranch(tree, N, g);
			}
			catch (InternalError e){  //added because of bug in jdk 1.7_45 on windows, crashing with internal error on getRaster

			}
			g.setPaintMode();
			g.setColor(Color.black);
		}
	}
	/*_________________________________________________*/
	/** Does the basic highlighting of a branch **/
	public  void highlightBranch (Tree tree, int N, Graphics g) {
			g.setColor(Color.yellow);
			try{
				fillBranch(tree, N, g);
			}
			catch (InternalError e){  //added because of bug in jdk 1.7_45 on windows, crashing with internal error on getRaster

			}
			g.setPaintMode();
			g.setColor(Color.black);
	}
	/*_________________________________________________*/
	/** Does the basic unhighlighting of a branch **/
	public  void unhighlightBranch (Tree tree, int N, Graphics g) {
		treeDisplay.repaint();
	}
	
	/** Fill branch N to indicate missing data */
	public  void fillBranchWithMissingData(Tree tree, int N, Graphics g){  //default; to be overridden for better indication of equivocal
		Color c = g.getColor();
		if (g instanceof Graphics2D){
			Graphics2D g2 = (Graphics2D)g;
			g2.setPaint(GraphicsUtil.missingDataTexture);
		}
		else
			g.setColor(Color.lightGray);
		fillBranch(tree, N, g);
		if (c!=null) g.setColor(c);
	}

	
	/** Fill branch N with indicated set of colors */
	public abstract void fillBranchWithColors(Tree tree, int N, ColorDistribution colors, Graphics g);

	boolean fillBranchColorSequenceWarned = false;
	/** Fill branch N with indicated set of colors as a sequence, e.g. for stochastic character mapping.  This is not abstract because many tree drawers would have difficulty implementing it */
	public void fillBranchWithColorSequence(Tree tree, int N, ColorEventVector colors, Graphics g){
		if (!fillBranchColorSequenceWarned){
			fillBranchColorSequenceWarned = true;
			MesquiteModule module = MesquiteTrunk.mesquiteTrunk;
			if (treeDisplay != null && treeDisplay.getOwnerModule() != null)
				module = treeDisplay.getOwnerModule();
			module.alert("The current tree form does not support sequences of changes along branches.  Try selecting another, such as Classic Square Tree.");
		}
	}
	
	public boolean isAtNode(MesquiteDouble fraction) {
		return fraction.isCloseEnough(ATNODE,0.00001);
	}
	
	/** If true, then the TreeDrawing should draw the nodes distinctly.  Currently this is only for debugging purposes, so that the node boundaries are evident */
	public boolean emphasizeNodes() {
		return false;
	}

	/*   */
	 public void redCrosses(Graphics g, Tree tree, int node) {
		g.setColor(Color.red);
		MesquiteNumber xC = new MesquiteNumber();
		MesquiteNumber yC = new MesquiteNumber();
		MesquiteDouble angle = new MesquiteDouble();
		getMiddleOfBranch(tree,node,xC, yC, angle);			
		g.drawLine(xC.getIntValue()-3, yC.getIntValue()-3, xC.getIntValue()+3, yC.getIntValue ()+3);
		g.drawLine(xC.getIntValue()-3+1, yC.getIntValue()-3, xC.getIntValue()+3+1, yC.getIntValue ()+3);
		g.drawLine(xC.getIntValue()-3, yC.getIntValue()+3, xC.getIntValue()+3, yC.getIntValue ()-3);
		g.drawLine(xC.getIntValue()-3+1, yC.getIntValue()+3, xC.getIntValue()+3+1, yC.getIntValue ()-3);
		GraphicsUtil.drawAngledLine(g, xC.getIntValue(), yC.getIntValue(), angle.getValue(), 100);
	}
	/**/
	

		public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if (tree==null || xValue==null || yValue==null)
			return;
		if (!tree.nodeExists(N))
			return;
		xValue.deassignAllValues();
		yValue.deassignAllValues();
		angle.setToUnassigned();
	}
		public void getSingletonLocation(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue){
		if (tree==null || xValue==null || yValue==null)
			return;
		if (!tree.nodeExists(N))
			return;
		xValue.deassignAllValues();
		yValue.deassignAllValues();
	}

	/*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		if ((x-this.x[node])*(x-this.x[node]) + (y-this.y[node])*(y-this.y[node]) < getEdgeWidth()*getEdgeWidth()/4) //use radius
			return true;
		else
			return false;
	}

	/*_________________________________________________*/
	public int getNodeWidth() {
		int w = getEdgeWidth()+4;
		if (w<MINNODEWIDTH) return MINNODEWIDTH;
		return w;
	}

	/** Find which branch is at x,y (-1 if none) */
	public abstract int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction);
	
	/** Put in a request to enable terminal boxes (this allows taxon names to be drawn to leave room)*/
	public void incrementEnableTerminalBoxes(){
		enableTerminalBoxesRequests++;
	}
	/** Withdraw request to enable terminal boxes (this allows taxon names to be drawn to leave room)*/
	public void decrementEnableTerminalBoxes(){
		enableTerminalBoxesRequests--;
		if (enableTerminalBoxesRequests<0)
			enableTerminalBoxesRequests=0;
	}
	/** Returns if terminal boxes requested*/
	public boolean terminalBoxesRequested(){
		return (enableTerminalBoxesRequests>0);
	}
	/** Fill terminal box with current color. */
	public abstract void fillTerminalBox(Tree tree, int node, Graphics g);
	
	/** Fill terminal box of node "node" with indicated set of colors */
	public abstract void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g);
	
	/*.................................................................................................................*/
	/** Find which terminal box is at x,y */
	public int findTerminalBox(Tree tree,  int N, int x, int y) {
		int foundTaxon =-1;
		if  (tree.nodeIsTerminal(N)) {   //terminal

			int taxonNumber = tree.taxonNumberOfNode(N);
			if (taxonNumber<0) {
				//MesquiteMessage.warnProgrammer("error: negative taxon number found in findNameOnTree");
				return -1;
			}
			if (taxonNumber>=tree.getTaxa().getNumTaxa()) {
				MesquiteMessage.warnProgrammer("error:  taxon number too large found in findTerminalBox (" + taxonNumber + ") node: " + N); 
				return -1;
			}
			if (isInTerminalBox(tree, N, x, y))
					return taxonNumber;
		}
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d) && foundTaxon==-1; d = tree.nextSisterOfNode(d)){
				foundTaxon=findTerminalBox(tree, d, x, y);
			}
		}
		return foundTaxon;
	}
	public int findTerminalBox(Tree tree, int x, int y){
		int drawnRoot = getDrawnRoot(); 
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		return findTerminalBox(tree, drawnRoot, x, y); 
	}
	
	public  boolean isInTerminalBox(Tree tree, int node, int xPos, int yPos){
		return false;
	}
	/*.................................................................................................................*/

	/** Draw highlight for branch N with current color of graphics context */
	public void drawHighlight(Tree tree, int N, Graphics g, boolean flip){}

/** This allows access to edgeWidths */
	public abstract int getEdgeWidth();
	
	/** project point x,y onto the line between N's lineBase to lineTip. */
	public Point2D.Double projectionOnLine(int N, double x, double y) {
		double newX = x;
		double newY = y;
		try {
			if (lineTipX[N] == lineBaseX[N]) {   //slope infinite; projection uses same y and sets x to be lineBaseX
				newX=lineBaseX[N];
			}
			else if (lineTipY[N] == lineBaseY[N]) {  //slope zero; projection uses same x and sets y to be lineBaseY
				newY =lineBaseY[N];
			}
			else {
				double m = (lineTipY[N]-lineBaseY[N])*1.0/(lineTipX[N] - lineBaseX[N]);
				//MesquiteModule.mesquiteTrunk.logln(" m " + MesquiteDouble.toString(m));
				double msquare = m*m;
				newY = (msquare)/(msquare+1)*((x-lineTipX[N])/m + (lineTipY[N]/msquare) + (y));
				newX = m* (y-newY) + x;
				//MesquiteModule.mesquiteTrunk.logln(" y " + MesquiteDouble.toString(newY));
				//MesquiteModule.mesquiteTrunk.logln(" x " + MesquiteDouble.toString(newX));
			}
			Point2D.Double thePoint= new Point2D.Double(x, y);
		}
		catch (ArrayIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		return new Point2D.Double(newX, newY);		
	}
	
	/* Choose point on line from lineBase to lineTip that corresponds to the i'th of "total" units along line. 
	public Point placeOnLine(int N, int i, int total) {
		Point thePoint= new Point();
		try {
			if (lineTipX[N] == lineBaseX[N]) {   //slope infinite; projection uses same y and sets x to be lineBaseX
				thePoint.x =lineBaseX[N];
			}
			else if (lineTipY[N] == lineBaseY[N]) {  //slope zero; projection uses same x and sets y to be lineBaseY
				thePoint.y =lineBaseY[N];
			}
			else {
				double m = (lineTipY[N]-lineBaseY[N])*1.0/(lineTipX[N] - lineBaseX[N]);
				double msquare = m*m;
				//double newY = (msquare)/(msquare-1)*((x-lineTipX[N])/m + (lineTipY[N]/msquare) + (y));
				//double newX = m* (y-newY) + x;
				//thePoint.y =( int)newY;
				//thePoint.x =(int) newX;
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		return thePoint;
	}
	*/
	
	public void setHighlightsOn(boolean on){
		/*
		if (on) {
			if (highlightThread==null) 
				highlightThread = new HighlightThread(treeDisplay);
			if (!highlightThread.isOn())
				highlightThread.start();
		}
		else {
			if (highlightThread!=null) {
				highlightThread.stop();
				highlightThread.dispose();
			}
			highlightThread = null;
		}
		*/
		
	}
	public boolean getHighlightsOn(){
		return false;
		//return (highlightThread!=null);
	}
	public void dispose(){
		treeDisplay = null;
		setHighlightsOn(false);
	}
	public void finalize() throws Throwable {
		setHighlightsOn(false);
		super.finalize();
	}
}



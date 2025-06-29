/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.NodeLocsCircular;
/*~~  */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.NodeLocsCircle;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayBkgdExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquiteMenuItemSpec;

/* ======================================================================== */
public class NodeLocsCircular extends NodeLocsCircle {
	public TreeDrawing treeDrawing;
	public Tree tree;
	public TreeDisplay treeDisplay;
	MesquiteBoolean showScale;
	MesquiteBoolean showBranchLengths;
	boolean resetShowBranchLengths = false;
	int rootHeight=30;
	Vector extras;
	double angleBetweenTaxa;
	double fractionCoverage = 0.96;

	double firsttx = 0.02;
	double centerx, centery;
	double circleSlice;
	double radius;
	Point2D.Double[] location;
	//DoublePt[] sLoc;
	double lasttx;
	Rectangle treeRectangle;
	int emptyRootSlices;
	int oldNumTaxa=0;
	private MesquiteMenuItemSpec showScaleItem;
	double zoomFactorMultiplier = 1.25;
	double zoomFactor = 1.0;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showBranchLengths = new MesquiteBoolean(false);
		extras = new Vector();
		showScale = new MesquiteBoolean(true);
		addMenuItem("Fraction of Circle...", makeCommand("circleFraction", this));
		addMenuItem("Start of Circle...", makeCommand("circleStart", this));
		MesquiteMenuItemSpec mmis = addMenuItem("Zoom In", makeCommand("zoomIn", this));
		mmis.setShortcut(KeyEvent.VK_CLOSE_BRACKET);
		mmis = addMenuItem("Zoom Out", makeCommand("zoomOut", this));
		mmis.setShortcut(KeyEvent.VK_OPEN_BRACKET); 
		addCheckMenuItem(null, "Branches Proportional to Lengths", makeCommand("branchLengthsToggle", this), showBranchLengths);
		if (showBranchLengths.getValue()) {
			showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			resetShowBranchLengths=true;
		}
		return true;
	}

	/*.................................................................................................................*/

	public void endJob(){
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

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("branchLengthsToggle " + showBranchLengths.toOffOnString());
		temp.addLine("toggleScale " + showScale.toOffOnString());
		temp.addLine("setZoom " + MesquiteDouble.toString(zoomFactor));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not the branches are to be shown proportional to their lengths", "[on = proportional; off]", commandName, "branchLengthsToggle")) {
			showBranchLengths.toggleValue(parser.getFirstToken(arguments));
			if (showBranchLengths.getValue()) 
				showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			else
				deleteMenuItem(showScaleItem);
			resetContainingMenuBar();
			resetShowBranchLengths=true;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to draw the scale for branch lengths", "[on or off]", commandName, "toggleScale")) {
			showScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}

		else if (checker.compare(this.getClass(), "Zooms in (magnifies)", "[]", commandName, "zoomIn")) {
			zoomFactor *= zoomFactorMultiplier;
			if (!MesquiteThread.isScripting())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Zooms out (shrinks)", "[]", commandName, "zoomOut")) {
			zoomFactor /= zoomFactorMultiplier;
			if (!MesquiteThread.isScripting())
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets zoom factor", "[double]", commandName, "setZoom")) {
			zoomFactor = MesquiteDouble.fromString(arguments);
			if (!MesquiteThread.isScripting())
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the fraction of the circle covered by the tree", "[number between 0 and 1]", commandName, "circleFraction")) {
			double d = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(d))
				d = MesquiteDouble.queryDouble(containerOfModule(), "Fraction of circle", "Enter a number between 0 and 1 to indicate the fraction of the circle covered by the tree", fractionCoverage);
			if (MesquiteDouble.isCombinable(d))
				fractionCoverage =d;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the position of the start of the circle (0 = bottom; 0.5 = top)", "[number between 0 and 1]", commandName, "circleStart")) {
			double d = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(d))
				d = MesquiteDouble.queryDouble(containerOfModule(), "Start position of circle", "Enter a number between 0 and 1 to indicate the start position of the circle", firsttx);
			if (MesquiteDouble.isCombinable(d))
				firsttx =d;
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public String getName() {
		return "Node Locations (circle)";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Calculates the node locations for a tree drawn in circular fashion, with root at center." ;
	}

	/*_________________________________________________*/
	public boolean compatibleWithOrientation(int orientation) {
		return orientation==TreeDisplay.CIRCULAR;
	}
	public void setDefaultOrientation(TreeDisplay treeDisplay) {
		treeDisplay.setOrientation(TreeDisplay.CIRCULAR);
	}
	/*_________________________________________________*/
	double scaling;

	private double getBranchLength (int N) {
		if (tree.branchLengthUnassigned(N))
			return 1;
		else
			return tree.getBranchLength(N);
	}
	/*{-----------------------------------------------------------------------------}*/
	int numNodes;
	private int findTaxa (int node){
		if (tree.nodeIsInternal(node)){
			int maxAbove = 0;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				int aboveThis = findTaxa(d);
				if (aboveThis>maxAbove)
					maxAbove = aboveThis;
			}
			return maxAbove+1;
		}
		else //  {terminal node; see if Daughter of N}
			return 1;
	}
	private int mostNodesToTip (int node){  // returns number of nodes from the top of the tree}
		numNodes = 0;
		return findTaxa(node);
	}
	//	{-----------------------------------------------------------------------------*/
	private void nodePolarToLoc (double polarlength, double angle, Point2D.Double loc){
		loc.setLocation(1.0*treeCenter.getX() + polarlength * Math.sin(angle), 1.0*treeCenter.getY() - polarlength * Math.cos(angle));
	}
	/*{-----------------------------------------------------------------------------}
	 private void nodePolarToSingleLoc (double polarlength, double angle, DoublePt loc){
	 //{remember: sin = opp/hyp, cos = adj/hyp, tan = opp/adj}
	  loc.x = treeCenter.x + polarlength * Math.sin(angle);
	  loc.y = treeCenter.y - polarlength * Math.cos(angle);
	  }
	  /*----------------------------------------------------------------------------*/
	private void nodeLocToPolar (Point2D.Double loc, Point2D center, double targetAngle, PolarCoord polar){
		polar.length = Math.sqrt((loc.getX()-center.getX()) *(loc.getX()-center.getX()) + (center.getY()-loc.getY())*(center.getY()-loc.getY()));
		polar.angle = Math.asin((loc.getX()-center.getX())/polar.length);
		if (targetAngle>Math.PI/2.0*3.0)
			polar.angle = Math.PI*2.0+polar.angle;
		else if (targetAngle>Math.PI/2.0)
			polar.angle = Math.PI-polar.angle;

	}
	//	{-----------------------------------------------------------------------------}
	private double nodeAngle (int left, int right){
		double theAngle;
		if (angle[left]<= angle[right])
			theAngle = (angle[left] + angle[right]) / 2.0;
		else {
			theAngle = angle[left] + (2 * Math.PI - Math.abs(angle[left] - angle[right])) / 2.0;
			if (theAngle > 2 * Math.PI)
				theAngle = theAngle - 2 * Math.PI;
		}
		return theAngle;
	}
	//	{-----------------------------------------------------------------------------}
	PolarCoord polar = new PolarCoord();
	//	angle 0 = vertical up
	private void calcterminalPosition (int node){
		double firstangle;

		firstangle = angleBetweenTaxa;
		angle[node] =firstangle + lasttx; // {angle in radians horizontal from vertical}
		polarLength[node] =radius;

		//		nodePolarToLoc(polarLength[node], angle[node], location[node]);

		double degrees = (angle[node]/Math.PI/2*360);
		if (degrees < 45)
			treeDrawing.labelOrientation[node] = 270;
		else if (degrees < 135)
			treeDrawing.labelOrientation[node] = 0;
		else if (degrees < 225)
			treeDrawing.labelOrientation[node] = 90;
		else if (degrees < 315)
			treeDrawing.labelOrientation[node] = 180;
		else 
			treeDrawing.labelOrientation[node] = 270;

		lasttx = (angleBetweenTaxa + lasttx);

	}

	/*------============= for collapsed clades =============-------*/
	/*_________________________________________________*/
	private double highestPolarLength(int N) {
		if (tree.nodeIsInternal(N)) { //internal
			double highestInClade = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				double highestInSubclade = highestPolarLength(d);
				highestInClade = MesquiteDouble.maximum(highestInSubclade, highestInClade);
			}
			return highestInClade;
		}
		return polarLength[N];
	}

	private void pushedCollapsed (int node){		//{tree traversal to find locations}
		if (tree.withinCollapsedClade(node)) {
			angle[node] = angle[tree.deepestCollapsedAncestor(node)];
			if (tree.isLeftmostTerminalOfCollapsedClade(node)){
				polarLength[node]  = highestPolarLength(tree.deepestCollapsedAncestor(node));
			}
			else {//if (tree.nodeIsInternal(node) || !tree.isLeftmostTerminalOfCollapsedClade(node)){
				polarLength[node]  = polarLength[tree.deepestCollapsedAncestor(node)];
			}
		}

		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				pushedCollapsed(d);
		}
	}

	/*_________________________________________________*/


	private void termTaxaRec (int node){		//{tree traversal to find locations}
		if (tree.nodeIsTerminal(node)) {
			if (!tree.withinCollapsedClade(node) || tree.isLeftmostTerminalOfCollapsedClade(node))
				calcterminalPosition(node);
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				termTaxaRec(d);
		}
	}

	private void terminalTaxaLocs (int node){
		lasttx = firsttx*Math.PI * 2.0;
		termTaxaRec(node);
	}
	//	{-----------------------------------------------------------------------------}
	//	{-----------------------------------------------------------------------------}
	private void zoom (int node){		//{tree traversal to find locations}
		if (zoomFactor == 1.0)
			return;
		polarLength[node]  = polarLength[node]*zoomFactor;

		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				zoom(d);
		}
	}
	private void calcInternalNodeLocs (int node){
		if (tree.nodeIsInternal(node)){
			double min = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				calcInternalNodeLocs(d);
				min = MesquiteDouble.minimum(min, polarLength[d]);
			}
			int left = tree.firstDaughterOfNode(node);
			int right = tree.lastDaughterOfNode(node);
			angle[node] = nodeAngle(left, right);
			polarLength[node] = min - circleSlice;
			if (polarLength[node] < 0) 
				polarLength[node] = 0;

			//		nodePolarToLoc(polarLength[node], angle[node], location[node]);
		}
	}

	//	{-----------------------------------------------------------------------------}
	private void adjustNodeLocsWithLengths (int node, double polarHeightToAncestor, int root){
		if (node==root)
			polarLength[node] = rootHeight;
		else {
			polarLength[node] = polarHeightToAncestor + getBranchLength(node) * scaling;
		}
		if (polarLength[node] < 0)
			polarLength[node] = 0;

		//	nodePolarToLoc(polarLength[node], angle[node], location[node]);
		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				adjustNodeLocsWithLengths(d, polarLength[node], root);
		}
	}

	//	{-----------------------------------------------------------------------------}
	private void adjustForLengths (int root){
		double tpaD = tree.tallestPathAboveNode(root, 1.0);

		if (tpaD>0)
			scaling=(radius-rootHeight)/tpaD;
		else // all assigned 0's give arbitrary scaling (not great solution!)
			scaling=20;
		adjustNodeLocsWithLengths(root, 0, root);
	}

	//	{-----------------------------------------------------------------------------}
	private void polarsToLocs (int node){
		nodePolarToLoc(polarLength[node], angle[node], location[node]);
		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				polarsToLocs(d);
		}
	}

	/*.................................................................................................................*/
	public int effectiveNumberOfTerminalsInClade(Tree tree, int node){
		if (tree.isCollapsedClade(node) || tree.nodeIsTerminal(node))
			return 1;
		int num=0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			num += effectiveNumberOfTerminalsInClade(tree, d);
		}
		return num;
	}

	/*_________________________________________________*/
	public void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot) { //Graphics g removed as parameter May 02
		if (MesquiteTree.OK(tree)) {
			this.tree = tree;
			this.treeDisplay = treeDisplay;
			if (treeDisplay.getExtras() !=null) {
				if (treeDisplay.getExtras().myElements(this)==null) {  //todo: need to do one for each treeDisplay!
					NodeLocsCircularExtra extra = new NodeLocsCircularExtra(this, treeDisplay);
					treeDisplay.addExtra(extra); 
					extras.addElement(extra);
				}
			}
			int subRoot = tree.motherOfNode(drawnRoot);
			treeCenter = new Point();
			if (!compatibleWithOrientation(treeDisplay.getOrientation()))
				setDefaultOrientation(treeDisplay);
			int numNodes =tree.getNumNodeSpaces();
			if (oldNumTaxa != tree.getNumTaxa() || location == null || location.length != numNodes) {
				location = new Point2D.Double[numNodes];
				//sLoc = new DoublePt[numNodes];
				for (int i=0; i<numNodes; i++) {
					//sLoc[i]= new DoublePt();
					location[i] = new Point2D.Double(0.0,0.0);
				}
				angle = new double[numNodes];
				polarLength  = new double[numNodes];
				oldNumTaxa=tree.getNumTaxa();
			}
			else {
				for (int i=0; i<location.length && location[i]!=null; i++) {
					if (location[i]!=null){
						location[i].setLocation(0, 0);
					}
					polarLength[i] = 0;
					angle[i] = 0;
				}
			}
			if (resetShowBranchLengths){
				if (showBranchLengths.getValue())
					treeDisplay.branchLengthDisplay=TreeDisplay.DRAWUNASSIGNEDASONE;
				else
					treeDisplay.branchLengthDisplay=TreeDisplay.DRAWULTRAMETRIC;

			}
			else {
				if (treeDisplay.showBranchLengths() != showBranchLengths.getValue()) {
					showBranchLengths.setValue(treeDisplay.showBranchLengths());
					if (showBranchLengths.getValue()) 
						showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
					else
						deleteMenuItem(showScaleItem);
					resetContainingMenuBar();
				}
			}
			this.treeDrawing = treeDisplay.getTreeDrawing();

			emptyRootSlices=1;
			angleBetweenTaxa=(2 * Math.PI*fractionCoverage) /effectiveNumberOfTerminalsInClade( tree, drawnRoot);
			treeRectangle = treeDisplay.getField();
			if (treeRectangle.width<treeRectangle.height)
				radius=(treeRectangle.width * 3 )/ 8;
			else
				radius=(treeRectangle.height) * 3 / 8;

			circleSlice = radius / (mostNodesToTip(drawnRoot) + emptyRootSlices);  //{v4: have it based upon an ellipse}

			int centreX, centreY;
		/*	if (treeDisplay.g()>treeRectangle.width)  // why was it like this in 3.x?
				centreX = treeDisplay.getWidth()/2;
			else*/
				centreX = treeRectangle.width / 2;
	/*		if (treeDisplay.getHeight()>treeRectangle.height)
				centreY = treeDisplay.getHeight()/2;
			else */
				centreY = treeRectangle.height / 2;
			treeCenter.setLocation(centreX, centreY);

			//			centerx = treeCenter.x;
			//			centery = treeCenter.y;
			location[drawnRoot].setLocation(treeCenter.getX(), treeCenter.getY());
			location[subRoot].setLocation(treeCenter.getX(), treeCenter.getY());
			polarLength[subRoot] = 0;
			angle[subRoot] = 0;
			terminalTaxaLocs(drawnRoot);
			calcInternalNodeLocs (drawnRoot);
			if (showBranchLengths.getValue()) {
				adjustForLengths(drawnRoot);
			}
			pushedCollapsed(drawnRoot);
			zoom(drawnRoot);
			if (showBranchLengths.getValue()) {
				scaling *= zoomFactor;
			}
			polarsToLocs(drawnRoot);
			for (int i=0; i<numNodes && i<treeDrawing.y.length; i++) {
				treeDrawing.y[i] = location[i].getY();
				treeDrawing.x[i] = location[i].getX();
			}
		}
	}

	public void drawGrid (Graphics g, double totalHeight, double scaling,  Point2D treeCenter) {
		if (g == null)
			return;
		Color c=g.getColor();
		double log10 = Math.log(10.0);
		double hundredthHeight = Math.exp(log10* ((int) (Math.log(totalHeight)/log10)-1));
		int countTenths = 0;
		double thisHeight = totalHeight + hundredthHeight;
		while ( thisHeight>=0) {
			if (countTenths % 10 == 0)
				g.setColor(Color.darkGray);
			else
				g.setColor(Color.lightGray);

			thisHeight -= hundredthHeight;
			GraphicsUtil.drawOval(g,treeCenter.getX()- (int)(thisHeight*scaling) - rootHeight,treeCenter.getY()- (int)(thisHeight*scaling) - rootHeight, 2*((int)(thisHeight*scaling) + rootHeight),  2*((int)(thisHeight*scaling) + rootHeight));
			//if (countTenths % 10 == 0)
			//	g.drawString(MesquiteDouble.toString(totalScaleHeight - thisHeight), rightEdge + buffer, (int)(base- (thisHeight*scaling)));
			countTenths ++;
		}

		if (c!=null) g.setColor(c);
	}
}

class PolarCoord {
	public double length, angle;
}


class NodeLocsCircularExtra extends TreeDisplayExtra implements TreeDisplayBkgdExtra {
	NodeLocsCircular locsModule;

	public NodeLocsCircularExtra (NodeLocsCircular ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		locsModule = ownerModule;
	}
	/*.................................................................................................................*/
	public   String writeOnTree(Tree tree, int drawnRoot) {
		return null;
	}
	/*.................................................................................................................*/
	boolean showRectangles = false; //see also drawDebuggingLines in TreeDrawing
	void drawTranslatedRect(Graphics g, int x, int y, int w, int h, Color c){
		g.setColor(c);
		int offX = treeDisplay.effectiveFieldLeftMargin();
		int offY = treeDisplay.effectiveFieldTopMargin();
		g.drawRect(x+offX, y+offY, w, h);
	}
	/*.................................................................................................................*/
	public   void drawUnderTree(Tree tree, int drawnRoot, Graphics g) {
		if (showRectangles){  //rectangles
			drawTranslatedRect(g, 2, 2, treeDisplay.getField().width, treeDisplay.getField().height, Color.green);
			drawTranslatedRect(g, 2, 2, treeDisplay.effectiveFieldWidth(), treeDisplay.effectiveFieldHeight(), Color.cyan);
			
			g.setColor(Color.blue);
			g.drawRect(2, 2, treeDisplay.effectiveFieldLeftMargin()-2, treeDisplay.effectiveFieldTopMargin()-2);
			g.setColor(Color.red);
			g.drawRect(treeDisplay.getField().width - treeDisplay.effectiveFieldRightMargin(), treeDisplay.getField().height - treeDisplay.effectiveFieldBottomMargin(), treeDisplay.effectiveFieldRightMargin()-2, treeDisplay.effectiveFieldBottomMargin()-2);

			
			int xTips = treeDisplay.effectiveFieldWidth()+treeDisplay.effectiveFieldLeftMargin()-treeDisplay.getTipsMargin();
			g.setColor(ColorDistribution.lightBlue);
			g.fillRect(xTips, treeDisplay.effectiveFieldTopMargin(), treeDisplay.getTaxonNameDistanceFromTip(), treeDisplay.effectiveFieldHeight());
			
		//	g.setColor(ColorDistribution.lightBlue);
			
		//	g.fillRect(xTips + treeDisplay.getTaxonNameDistanceFromTip(), treeDisplay.effectiveFieldTopMargin(), treeDisplay.totalTipsFieldDistance(), treeDisplay.effectiveFieldHeight());
		}
		if (locsModule.showScale.getValue() && locsModule.showBranchLengths.getValue())
			locsModule.drawGrid(g, tree.tallestPathAboveNode(drawnRoot, 1.0), locsModule.scaling, locsModule.treeCenter);
	}
	/*.................................................................................................................*/
	public   void printUnderTree(Tree tree, int drawnRoot, Graphics g) {
		drawUnderTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	boolean showNodes = false; 
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (showNodes){
			if (tree.withinCollapsedClade(node)) {
				if (tree.isLeftmostTerminalOfCollapsedClade(node))
					g.setColor(Color.blue);
				else if (tree.nodeIsTerminal(node))
					g.setColor(Color.red);
				else
					g.setColor(Color.yellow);
			}
			else if (tree.nodeIsTerminal(node))
				g.setColor(Color.green);
			else
				g.setColor(Color.cyan);
			g.fillOval((int)treeDisplay.getTreeDrawing().x[node], (int)treeDisplay.getTreeDrawing().y[node], 5, 5);
			if (node == tree.getRoot()){  //just once, rectangles
				g.setColor(Color.blue);
				g.drawRect(2, 2, treeDisplay.getField().width-2, treeDisplay.getField().height-2);
				g.drawRect(2, 2, treeDisplay.effectiveFieldLeftMargin()-2, treeDisplay.effectiveFieldTopMargin()-2);
				g.setColor(Color.red);
				g.drawRect(treeDisplay.getField().width - treeDisplay.effectiveFieldRightMargin(), treeDisplay.getField().height - treeDisplay.effectiveFieldBottomMargin(), treeDisplay.effectiveFieldRightMargin()-2, treeDisplay.effectiveFieldBottomMargin()-2);
				g.setColor(Color.lightGray);
				g.fillOval((int)locsModule.treeCenter.getX(), (int)locsModule.treeCenter.getY(), 20, 20);
			}
		}

		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawOnTree(tree, d, g);


	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}

}



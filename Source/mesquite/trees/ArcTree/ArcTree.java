/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite is distributed under the terms of the GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ArcTree;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.trees.lib.*;

import java.awt.geom.*;
/* ======================================================================== */
public class ArcTree extends DrawTree {
	public String getName() {
		return "Curvogram";
	}
	public String getExplanation() {
		return "Draws trees with curved branches (as PHYLIP's 'Curvogram')." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsVH.class, getName() + "  needs the locations of nodes to be calculated.",
				"The calculator for node locations is chosen automatically or initially");
	}
	/*.................................................................................................................*/

	NodeLocsVH nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	MesquiteString orientationName;
	Vector drawings;
	int oldEdgeWidth = 8;
	int ornt;
	MesquiteString nodeLocsName;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsVH)hireNamedEmployee(NodeLocsVH.class, "#NodeLocsStandard");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node locator module was obtained");

		nodeLocsName = new MesquiteString(nodeLocsTask.getName());
		if (numModulesAvailable(NodeLocsVH.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Node Locations Calculator", makeCommand("setNodeLocs", this), NodeLocsVH.class);
			mss.setSelected(nodeLocsName);
		}
		drawings = new Vector();
		orientationName = new MesquiteString("Up");
		ornt = TreeDisplay.UP;
		MesquiteSubmenuSpec orientationSubmenu = addSubmenu(null, "Orientation");
		orientationSubmenu.setSelected(orientationName);
		addItemToSubmenu(null, orientationSubmenu, "Up", makeCommand("orientUp",  this));
		addItemToSubmenu(null, orientationSubmenu, "Right", makeCommand("orientRight",  this));
		addItemToSubmenu(null, orientationSubmenu, "Down", makeCommand("orientDown",  this));
		addItemToSubmenu(null, orientationSubmenu, "Left", makeCommand("orientLeft",  this));

		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		return true;

	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		ArcTreeDrawing treeDrawing =  new ArcTreeDrawing (treeDisplay, numTaxa, this);
		if (legalOrientation(treeDisplay.getOrientation())){
			orientationName.setValue(orient(treeDisplay.getOrientation()));
			ornt = treeDisplay.getOrientation();
		}
		else
			treeDisplay.setOrientation(ornt);
		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	public boolean legalOrientation (int orientation){
		return (orientation == TreeDisplay.UP || orientation == TreeDisplay.DOWN || orientation == TreeDisplay.RIGHT || orientation == TreeDisplay.LEFT);
	}

	public String orient (int orientation){
		if (orientation == TreeDisplay.UP)
			return "Up";
		else if (orientation == TreeDisplay.DOWN)
			return "Down";
		else if (orientation == TreeDisplay.RIGHT)
			return "Right";
		else if (orientation == TreeDisplay.LEFT)
			return "Left";
		else return "other";
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setNodeLocs", nodeLocsTask);
		temp.addLine("setEdgeWidth " + oldEdgeWidth); 
		if (ornt== TreeDisplay.UP)
			temp.addLine("orientUp"); 
		else if (ornt== TreeDisplay.DOWN)
			temp.addLine("orientDown"); 
		else if (ornt== TreeDisplay.LEFT)
			temp.addLine("orientLeft"); 
		else if (ornt== TreeDisplay.RIGHT)
			temp.addLine("orientRight"); 
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the node locations calculator", "[name of module]", commandName, "setNodeLocs")) {
			NodeLocsVH temp = (NodeLocsVH)replaceEmployee(NodeLocsVH.class, arguments, "Node Locations Calculator", nodeLocsTask);
			if (temp != null) {
				nodeLocsTask = temp;
				nodeLocsName.setValue(nodeLocsTask.getName());
				parametersChanged();
			}
			return nodeLocsTask;
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
					ArcTreeDrawing treeDrawing = (ArcTreeDrawing)obj;
					treeDrawing.setEdgeWidth(newWidth);
					treeDrawing.treeDisplay.setMinimumTaxonNameDistance(newWidth, 6); 
				}
				if ( !MesquiteThread.isScripting()) parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Returns the employee module that assigns node locations", null, commandName, "getNodeLocsEmployee")) {
			return nodeLocsTask;
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ArcTreeDrawing treeDrawing = (ArcTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.UP);
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at the bottom", null, commandName, "orientDown")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ArcTreeDrawing treeDrawing = (ArcTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.DOWN);
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at right", null, commandName, "orientRight")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ArcTreeDrawing treeDrawing = (ArcTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.RIGHT);
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at left", null, commandName, "orientLeft")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ArcTreeDrawing treeDrawing = (ArcTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.LEFT);
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else {
			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

}

/* ======================================================================== */
class ArcTreeDrawing extends TreeDrawing  {

	private int lastleft;
	private int taxspacing;
	public int highlightedBranch, branchFrom;
	public int xFrom, yFrom, xTo, yTo;
	public ArcTree ownerModule;
	public int edgewidth = 8;
	public int preferredEdgeWidth = 8;
	int oldNumTaxa = 0;
	public static final int inset=2;
	private boolean ready=false;
	BasicStroke defaultStroke;

	private int foundBranch;

	public ArcTreeDrawing (TreeDisplay treeDisplay, int numTaxa, ArcTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		treeDisplay.setMinimumTaxonNameDistance(edgewidth, 6); //better if only did this if tracing on
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;
		try{
			defaultStroke = new BasicStroke();
		}
		catch (Throwable t){
		}
		ready = true;
		treeDisplay.setOrientation(ownerModule.nodeLocsTask.getDefaultOrientation());
	}

	/*_________________________________________________*/
	private void calculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node] = y[tree.motherOfNode(node)];
		lineBaseX[node] =  x[tree.motherOfNode(node)];
	}

	public double getBranchCenterX(int node){
		double bX =lineBaseX[node];
		if (treeDisplay.getOrientation()==TreeDisplay.RIGHT || treeDisplay.getOrientation()==TreeDisplay.LEFT)
			return (bX-x[node])/3 + x[node];
		else 
			return x[node]; 
	}
	public double getBranchCenterY(int node){
		double bY = lineBaseY[node];
		if (treeDisplay.getOrientation()==TreeDisplay.RIGHT || treeDisplay.getOrientation()==TreeDisplay.LEFT)
			return y[node];
		else 
			return (bY-y[node])/3 + y[node];
	}
	/*_________________________________________________*/
	private void calcBranchStuff(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField()); //Graphics g removed as parameter May 02
		calculateLines(tree, drawnRoot);
		edgewidth = preferredEdgeWidth;
		if (treeDisplay.getTaxonSpacing()<edgewidth+2) {
			edgewidth= treeDisplay.getTaxonSpacing()-2;
			if (edgewidth<2)
				edgewidth=2;
		}
	}
	public void getSingletonLocation(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue){
		if(tree==null || xValue==null || yValue==null)
			return;
		if(!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
		yValue.setValue(getBranchCenterY(N));
		xValue.setValue(getBranchCenterX(N));
	}

	/*_________________________________________________*/
	/** Draw highlight for branch node with current color of graphics context */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		Color tC = g.getColor();
		if (flip)
			g.setColor(Color.yellow);
		else
			g.setColor(Color.blue);

		if (treeDisplay.getOrientation()==TreeDisplay.DOWN || treeDisplay.getOrientation()==TreeDisplay.UP){
			GraphicsUtil.fillOval(g, x[node]-4, y[node], 8, 8);
		}
		else {
			GraphicsUtil.fillOval(g,x[node], y[node]-4, 8,8);
		}

		g.setColor(tC);
	}
	/*_________________________________________________*/
	private   void drawClade(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			g.setColor(treeDisplay.getBranchColor(node));
			if (tree.getRooted() || tree.getRoot()!=node) {
				DrawTreeUtil.drawOneCurvedBranch(treeDisplay, x, y, getEdgeWidth(), tree, g, node, 0, edgewidth,0, emphasizeNodes(), nodePoly(node), defaultStroke);
			}

			int thisSister = tree.firstDaughterOfNode(node);
			while (tree.nodeExists(thisSister)) {
				drawClade( tree, g, thisSister);
				thisSister = tree.nextSisterOfNode(thisSister);
			}
		}
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
		if (MesquiteTree.OK(tree)) {
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			g.setColor(treeDisplay.branchColor);
			drawClade(tree, g, drawnRoot);  
		}
	}
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
		if (MesquiteTree.OK(tree)) {
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			if (!tree.nodeExists(getDrawnRoot()))
				setDrawnRoot(tree.getRoot());
			calcBranchStuff(tree, getDrawnRoot());
		}
	}
	/*_________________________________________________*/
	public  boolean isInTerminalBox(Tree tree, int node, int xPos, int yPos){
		int ew = edgewidth;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) 
			return xPos> x[node] && xPos < x[node]+ew && yPos > y[node]-ew-3 && yPos < y[node]-3;
			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) 
				return xPos> x[node] && xPos < x[node]+ew && yPos > y[node]+2 && yPos < y[node]+ew+2;
				else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) 
					return xPos> x[node]+1 && xPos < x[node]+ew +1 && yPos > y[node] && yPos < y[node] + ew;
					else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT) 
						return xPos> x[node]-ew-3 && xPos < x[node]-3 && yPos > y[node] && yPos < y[node] + ew;
						else 
							return xPos> x[node] && xPos < x[node]+ew && yPos > y[node] && yPos < y[node] + ew;
	}

	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle2D box;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			box = new Rectangle2D.Double(x[node], y[node]-edgewidth/2-2, edgewidth, edgewidth);
			GraphicsUtil.fillArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 0, 180);
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 0, 180);
			GraphicsUtil.drawLine(g,box.getX(), box.getY()+ edgewidth/2, box.getX()+edgewidth,  box.getY()+ edgewidth/2);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
			box = new Rectangle2D.Double(x[node], y[node] + 2, edgewidth, edgewidth);
			GraphicsUtil.fillArc(g,box.getX(), box.getY() -  box.getHeight()/2, box.getWidth(), box.getHeight(), 180, 180);
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX(), box.getY() -  box.getHeight()/2, box.getWidth(), box.getHeight(), 180, 180);
			GraphicsUtil.drawLine(g,box.getX(), box.getY() , box.getX()+edgewidth,  box.getY());
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			box = new Rectangle2D.Double(x[node] + 2, y[node], edgewidth, edgewidth);
			GraphicsUtil.fillArc(g,box.getX()- box.getWidth()/2, box.getY(), box.getWidth(), box.getHeight(), 270, 180);
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX()- box.getWidth()/2, box.getY(), box.getWidth(), box.getHeight(), 270, 180);
			GraphicsUtil.drawLine(g,box.getX(), box.getY(), box.getX() ,  box.getY()+edgewidth);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
			box = new Rectangle2D.Double(x[node]-edgewidth/2-2, y[node], edgewidth, edgewidth);
			GraphicsUtil.fillArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 90, 180);
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 90, 180);
			GraphicsUtil.drawLine(g,box.getX()+edgewidth/2, box.getY(), box.getX()+edgewidth/2,  box.getY()+edgewidth);
		}
		else {
			box = new Rectangle2D.Double(x[node], y[node], edgewidth, edgewidth);
			GraphicsUtil.fillArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 0, 360);
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 0, 360);
		}
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		Rectangle2D box;
		int numColors = colors.getNumColors();
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			box = new Rectangle2D.Double(x[node], y[node]-edgewidth/2-2, edgewidth, edgewidth);
			for (int i=0; i<numColors; i++) {
				g.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
				GraphicsUtil.fillArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 0+ (i*180/numColors), 180- (i*180/numColors));
			}
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 0, 180);
			GraphicsUtil.drawLine(g,box.getX(), box.getY()+ edgewidth/2, box.getX()+edgewidth,  box.getY()+ edgewidth/2);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
			box = new Rectangle2D.Double(x[node], y[node] + 2, edgewidth, edgewidth);
			for (int i=0; i<numColors; i++) {
				g.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
				GraphicsUtil.fillArc(g,box.getX(), box.getY() -  box.getHeight()/2, box.getWidth(), box.getHeight(), 180+ (i*180/numColors), 180- (i*180/numColors));
			}
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX(), box.getY() -  box.getHeight()/2, box.getWidth(), box.getHeight(), 180, 180);
			GraphicsUtil.drawLine(g,box.getX(), box.getY() , box.getX()+edgewidth,  box.getY());
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			box = new Rectangle2D.Double(x[node] + 2, y[node], edgewidth, edgewidth);
			for (int i=0; i<numColors; i++) {
				g.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
				GraphicsUtil.fillArc(g,box.getX()- box.getWidth()/2, box.getY(), box.getWidth(), box.getHeight(), 270+ (i*180/numColors), 180- (i*180/numColors));
			}
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX()- box.getWidth()/2, box.getY(), box.getWidth(), box.getHeight(), 270, 180);
			GraphicsUtil.drawLine(g,box.getX(), box.getY(), box.getX() ,  box.getY()+edgewidth);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
			box = new Rectangle2D.Double(x[node]-edgewidth/2-2, y[node], edgewidth, edgewidth);
			for (int i=0; i<numColors; i++) {
				g.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
				GraphicsUtil.fillArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 90+ (i*180/numColors), 180- (i*180/numColors));
			}
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 90, 180);
			GraphicsUtil.drawLine(g,box.getX()+edgewidth/2, box.getY(), box.getX()+edgewidth/2,  box.getY()+edgewidth);
		}
		else {
			box = new Rectangle2D.Double(x[node], y[node], edgewidth, edgewidth);
			for (int i=0; i<numColors; i++) {
				g.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
				GraphicsUtil.fillArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 0, 360);
			}
			g.setColor(treeDisplay.getBranchColor(node));
			GraphicsUtil.drawArc(g,box.getX(), box.getY(), box.getWidth(), box.getHeight(), 0, 360);
		}
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			Color c = g.getColor();
			int fillWidth = edgewidth-2*inset;
			int numColors = colors.getNumColors();
			for (int i=0; i<numColors; i++) {
				Color color;
				if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
					g.setColor(color);
				//			public static   void drawOneBranch(TreeDisplay treeDisplay, int[] x, int[] y, int edgewidth, Tree tree, Graphics g, int node, int start, int width, int adj, boolean emphasizeNodes, Polygon nodePoly, BasicStroke defaultStroke) {

				DrawTreeUtil.drawOneCurvedBranch(treeDisplay,x,y,getEdgeWidth(), treeDisplay.getTree(), g, node, inset + i*fillWidth/numColors,  (i+1)*fillWidth/numColors -i*fillWidth/numColors, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;
			}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			DrawTreeUtil.drawOneCurvedBranch(treeDisplay,x,y,getEdgeWidth(), tree, g, node, inset, edgewidth-inset*2, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;
		}
	}

	/*_________________________________________________*/
	public Path2D nodePoly(int node) {
		double offset = (getNodeWidth()-getEdgeWidth())/2;
		double doubleOffset = (getNodeWidth()-getEdgeWidth());
		double startX = x[node] - offset;
		double startY= y[node] - offset;
		if (treeDisplay.getOrientation()==TreeDisplay.RIGHT){
			startX -= getNodeWidth()-doubleOffset;
		} else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
			startY -= getNodeWidth()-doubleOffset;
		Path2D poly = new Path2D.Double();
		poly.moveTo(startX,startY);
		poly.lineTo(startX+getNodeWidth(),startY);
		poly.lineTo(startX+getNodeWidth(),startY+getNodeWidth());
		poly.lineTo(startX,startY+getNodeWidth());
		poly.lineTo(startX,startY);
		return poly;
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
			if (DrawTreeUtil.inBranch(treeDisplay, this.x, this.y, getEdgeWidth(), tree, node, x,y) || inNode(node,x,y)){
				foundBranch = node;
				if (fraction!=null)
					if (inNode(node,x,y))
						fraction.setValue(ATNODE);
					else {
						int motherNode = tree.motherOfNode(node);
						fraction.setValue(EDGESTART);  //TODO: this is just temporary: need to calculate value along branch.
						if (tree.nodeExists(motherNode)) {
							if (treeDisplay.getOrientation()==TreeDisplay.UP|| treeDisplay.getOrientation()==TreeDisplay.DOWN)  {
								fraction.setValue( Math.abs(1.0*(y-this.y[motherNode])/(this.y[node]-this.y[motherNode])));
							}
							else if (treeDisplay.getOrientation()==TreeDisplay.LEFT || treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
								fraction.setValue( Math.abs(1.0*(x-this.x[motherNode])/(this.x[node]-this.x[motherNode])));
							}
						}
					}
			}

			int thisSister = tree.firstDaughterOfNode(node);
			while (tree.nodeExists(thisSister)) {
				ScanBranches(tree, thisSister, x, y, fraction);
				thisSister = tree.nextSisterOfNode(thisSister);
			}

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
		treeDisplay.setOrientation(orientation);
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void setEdgeWidth(int edw) {
		edgewidth = edw;
		preferredEdgeWidth = edw;
	}
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgewidth;
	}
}



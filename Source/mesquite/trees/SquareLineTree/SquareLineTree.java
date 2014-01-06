
package mesquite.trees.SquareLineTree;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.trees.lib.*;

import java.awt.geom.*;
/* ======================================================================== */
public class SquareLineTree extends DrawTree {
	public String getName() {
		return "Square Line Tree";
	}
	public String getExplanation() {
		return "Draws trees with square branches made out of lines rather than polygons." ;
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
	int oldEdgeWidth = 4;
	int ornt;
	MesquiteString nodeLocsName;
	MesquiteBoolean showEdgeLines = new MesquiteBoolean(true);  //these needs to be set default true; otherwise Trace Character makes branches disappear in most common cases
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
		ornt = NodeLocsVH.defaultOrientation;  //should take out of preferences

		orientationName = new MesquiteString(orient(ornt));
		MesquiteSubmenuSpec orientationSubmenu = addSubmenu(null, "Orientation");
		orientationSubmenu.setSelected(orientationName);
		addItemToSubmenu(null, orientationSubmenu, "Up", makeCommand("orientUp",  this));
		addItemToSubmenu(null, orientationSubmenu, "Right", makeCommand("orientRight",  this));
		addItemToSubmenu(null, orientationSubmenu, "Down", makeCommand("orientDown",  this));
		addItemToSubmenu(null, orientationSubmenu, "Left", makeCommand("orientLeft",  this));

		
		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		addCheckMenuItem(null,"Show Edge Lines", makeCommand("showEdgeLines",  this), showEdgeLines);
		return true;

	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		SquareLineTreeDrawing treeDrawing =  new SquareLineTreeDrawing (treeDisplay, numTaxa, this);
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
		temp.addLine("showEdgeLines " + showEdgeLines.toOffOnString()); 

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
					SquareLineTreeDrawing treeDrawing = (SquareLineTreeDrawing)obj;
					treeDrawing.setEdgeWidth(newWidth);
					treeDrawing.treeDisplay.setMinimumTaxonNameDistance(newWidth, 6); 
				}
				if ( !MesquiteThread.isScripting()) parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Sets whether to show edge lines or not.", "", commandName, "showEdgeLines")) {
 			boolean current = showEdgeLines.getValue();
 			showEdgeLines.toggleValue(parser.getFirstToken(arguments));
 			if (current!=showEdgeLines.getValue()) {
 				parametersChanged();
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
				SquareLineTreeDrawing treeDrawing = (SquareLineTreeDrawing)obj;
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
				SquareLineTreeDrawing treeDrawing = (SquareLineTreeDrawing)obj;
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
				SquareLineTreeDrawing treeDrawing = (SquareLineTreeDrawing)obj;
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
				SquareLineTreeDrawing treeDrawing = (SquareLineTreeDrawing)obj;
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
	public boolean getShowEdgeLines() {
		return showEdgeLines.getValue();
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

}

/* ======================================================================== */
class SquareLineTreeDrawing extends TreeDrawing  {

	private int lastleft;
	private int taxspacing;
	public int highlightedBranch, branchFrom;
	public int xFrom, yFrom, xTo, yTo;
	public SquareLineTree ownerModule;
	public int edgewidth = 4;
	public int preferredEdgeWidth = 4;
	int oldNumTaxa = 0;
	float inset=(float)1.0;
	private boolean ready=false;
	BasicStroke defaultStroke;

	private int foundBranch;

	public SquareLineTreeDrawing (TreeDisplay treeDisplay, int numTaxa, SquareLineTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		treeDisplay.setMinimumTaxonNameDistance(edgewidth, 4); //better if only did this if tracing on
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;
		try{
			defaultStroke = new BasicStroke();
		}
		catch (Throwable t){
		}
		ready = true;
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

	public int getBranchCenterX(int node){
		int bX =lineBaseX[node];
		if (treeDisplay.getOrientation()==TreeDisplay.RIGHT || treeDisplay.getOrientation()==TreeDisplay.LEFT)
			return (bX-x[node])/3 + x[node];
		else 
			return x[node]; 
	}
	public int getBranchCenterY(int node){
		int bY = lineBaseY[node];
		if (treeDisplay.getOrientation()==TreeDisplay.RIGHT || treeDisplay.getOrientation()==TreeDisplay.LEFT)
			return y[node];
		else 
			return (bY-y[node])/3 + y[node];
	}
	/*_________________________________________________*/
	public int getNodeValueTextBaseX(int node, int edgewidth, int stringwidth, int fontHeight, boolean horizontalText){
		int bX =x[node];
		if (horizontalText) {
			if (treeDisplay.getOrientation()==treeDisplay.RIGHT) {
				bX -=edgewidth+stringwidth;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.LEFT) {
				bX +=edgewidth;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.DOWN) {
				bX -=stringwidth/2;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.UP) {
				bX -=stringwidth/2;
			}
		} else {
			if (treeDisplay.getOrientation()==treeDisplay.RIGHT) {
				bX -=fontHeight*2+edgewidth+2;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.LEFT) {
				bX -=fontHeight;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.DOWN) {
				bX -=fontHeight;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.UP) {
				bX -=fontHeight;
			}
		}
		return bX;
	}
	public int getNodeValueTextBaseY(int node, int edgewidth, int stringwidth, int fontHeight, boolean horizontalText){
		int bY =y[node];
		if (horizontalText) {
			if (treeDisplay.getOrientation()==treeDisplay.RIGHT) {
				bY -=fontHeight;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.LEFT) {
				bY -=fontHeight;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.DOWN) {
				bY -=edgewidth+fontHeight+2;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.UP) {
				bY +=edgewidth;
			}
		} else {
			if (treeDisplay.getOrientation()==treeDisplay.RIGHT) {
				bY +=stringwidth/2;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.LEFT) {
				bY +=stringwidth/2;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.DOWN) {
				bY -=edgewidth;
			}
			else if (treeDisplay.getOrientation()==treeDisplay.UP) {
				bY +=stringwidth+edgewidth;
			}
		}
		return bY;
	}
	/*_________________________________________________*/
	private void calcBranchStuff(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}

		treeDisplay.setTaxonSpacing(16);

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField()); //Graphics g removed as parameter May 02
		calculateLines(tree, drawnRoot);
		edgewidth = preferredEdgeWidth;
		if (treeDisplay.getTaxonSpacing()<edgewidth+2) {
			edgewidth= treeDisplay.getTaxonSpacing()-2;
			if (edgewidth<2)
				edgewidth=2;
		}
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
			g.fillOval(x[node]-4, y[node], 8, 8);
		}
		else {
			g.fillOval(x[node], y[node]-4, 8,8);
		}

		g.setColor(tC);
	}
	/*_________________________________________________*/
	private   void drawClade(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			g.setColor(treeDisplay.getBranchColor(node));
			if (tree.getRooted() || tree.getRoot()!=node) {
				DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, x, y, getEdgeWidth(), tree, g, null, node, 0, edgewidth,0, emphasizeNodes(), nodePoly(node), defaultStroke);
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
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle box;
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			box = new Rectangle(x[node], y[node]-edgewidth/2-2, edgewidth, edgewidth);
			g.fillArc(box.x, box.y, box.width, box.height, 0, 180);
			g.setColor(treeDisplay.getBranchColor(node));
			g.drawArc(box.x, box.y, box.width, box.height, 0, 180);
			g.drawLine(box.x, box.y+ edgewidth/2, box.x+edgewidth,  box.y+ edgewidth/2);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN) {
			box = new Rectangle(x[node], y[node] + 2, edgewidth, edgewidth);
			g.fillArc(box.x, box.y -  box.height/2, box.width, box.height, 180, 180);
			g.setColor(treeDisplay.getBranchColor(node));
			g.drawArc(box.x, box.y -  box.height/2, box.width, box.height, 180, 180);
			g.drawLine(box.x, box.y , box.x+edgewidth,  box.y);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			box = new Rectangle(x[node] + 2, y[node], edgewidth, edgewidth);
			g.fillArc(box.x- box.width/2, box.y, box.width, box.height, 270, 180);
			g.setColor(treeDisplay.getBranchColor(node));
			g.drawArc(box.x- box.width/2, box.y, box.width, box.height, 270, 180);
			g.drawLine(box.x, box.y, box.x ,  box.y+edgewidth);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT) {
			box = new Rectangle(x[node]-edgewidth/2-2, y[node], edgewidth, edgewidth);
			g.fillArc(box.x, box.y, box.width, box.height, 90, 180);
			g.setColor(treeDisplay.getBranchColor(node));
			g.drawArc(box.x, box.y, box.width, box.height, 90, 180);
			g.drawLine(box.x+edgewidth/2, box.y, box.x+edgewidth/2,  box.y+edgewidth);
		}
		else {
			box = new Rectangle(x[node], y[node], edgewidth, edgewidth);
			g.fillArc(box.x, box.y, box.width, box.height, 0, 360);
			g.setColor(treeDisplay.getBranchColor(node));
			g.drawArc(box.x, box.y, box.width, box.height, 0, 360);
		}
	}
	/*_________________________________________________*/
	public  boolean isInTerminalBox(Tree tree, int node, int xPos, int yPos){
		int ew = edgewidth-1;
		if (treeDisplay.getOrientation()==treeDisplay.UP) 
			return xPos> x[node] && xPos < x[node]+ew && yPos > y[node]-ew-3 && yPos < y[node]-3;
		else if (treeDisplay.getOrientation()==treeDisplay.DOWN)
			return xPos> x[node] && xPos < x[node]+ew && yPos > y[node]+1 && yPos < y[node]+ew+1;
		else  if (treeDisplay.getOrientation()==treeDisplay.RIGHT) 
			return xPos> x[node]+1 && xPos < x[node]+ew +1 && yPos > y[node] && yPos < y[node] + ew;
		else  if (treeDisplay.getOrientation()==treeDisplay.LEFT)
			return xPos> x[node]-ew-3 && xPos < x[node]-3 && yPos > y[node] && yPos < y[node] + ew;
		else 
			return xPos> x[node] && xPos < x[node]+ew && yPos > y[node] && yPos < y[node] + ew;
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		float localInset = 0;
		if (ownerModule.getShowEdgeLines())
			localInset=inset;
		Rectangle2D box;
		Rectangle2D colorBox;
		Graphics2D g2 = (Graphics2D)g;
		int numColors = colors.getNumColors();
		float ew = edgewidth-1;
		// the 0.5 values are the halfline width to deal with pen positioning
		if (treeDisplay.getOrientation()==treeDisplay.UP) 
			box = new Rectangle2D.Double(x[node]+0.5, y[node]-ew-2, ew, ew);
		else if (treeDisplay.getOrientation()==treeDisplay.DOWN)
			box = new Rectangle2D.Double(x[node]+0.5, y[node]+2, ew, ew);
		else  if (treeDisplay.getOrientation()==treeDisplay.RIGHT) 
			box = new Rectangle2D.Double(x[node]+2, y[node]+0.5, ew, ew);
		else  if (treeDisplay.getOrientation()==treeDisplay.LEFT)
			box = new Rectangle2D.Double(x[node]-ew-2, y[node]+0.5, ew, ew);
		else 
			box = new Rectangle2D.Double(x[node], y[node], ew, ew);
		for (int i=0; i<numColors; i++) {
			g2.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
			colorBox = new Rectangle2D.Double(box.getX() + (i*box.getWidth()/numColors), box.getY(), box.getWidth()-  (i*box.getWidth()/numColors), box.getHeight());
			g2.fill(colorBox);
		}
		g2.setColor(treeDisplay.getBranchColor(node));
		g2.draw(box);
	}
	/*_________________________________________________*
	public void fillBranchWithMissingData(Tree tree, int node, Graphics g) {

		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			Color c = g.getColor();
			if (g instanceof Graphics2D){
				Graphics2D g2 = (Graphics2D)g;
				g2.setPaint(GraphicsUtil.missingDataTexture);
			}
			else
				g.setColor(Color.lightGray);
			float localInset = 0;
			if (ownerModule.getShowEdgeLines())
				localInset=inset;
			float fillWidth = edgewidth-2*localInset;
			DrawTreeUtil.drawOneSquareLineBranch(treeDisplay,x,y,getEdgeWidth(), treeDisplay.getTree(), g, null, node, localInset,  fillWidth, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			Color c = g.getColor();
			float localInset = 0;
			if (ownerModule.getShowEdgeLines())
				localInset=inset;
			float fillWidth = edgewidth-2*localInset;
			int numColors = colors.getNumColors();
			Color color;
			if (numColors<=1) {
				if ((color = colors.getColor(0, !tree.anySelected()|| tree.getSelected(node)))!=null)
					g.setColor(color);
				DrawTreeUtil.drawOneSquareLineBranch(treeDisplay,x,y,getEdgeWidth(), treeDisplay.getTree(), g, colors, node, localInset,  fillWidth, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;
			}
			else
				for (int i=0; i<numColors; i++) {
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					//			public static   void drawOneBranch(TreeDisplay treeDisplay, int[] x, int[] y, int edgewidth, Tree tree, Graphics g, int node, int start, int width, int adj, boolean emphasizeNodes, Polygon nodePoly, BasicStroke defaultStroke) {
					float thickness = fillWidth/numColors;
					float start = i*thickness+localInset;
					DrawTreeUtil.fillOneSquareLineBranch(treeDisplay,x,y,getEdgeWidth(), treeDisplay.getTree(), g, colors, node, start,  thickness, localInset,emphasizeNodes(),nodePoly(node), defaultStroke) ;
				}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			//if (ownerModule.getShowEdgeLines())
			DrawTreeUtil.drawOneSquareLineBranch(treeDisplay,x,y,getEdgeWidth(), tree, g, null, node, inset, edgewidth-inset*2, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;
		}
	}

	/*_________________________________________________*/
	public Polygon nodePoly(int node) {
		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int doubleOffset = (getNodeWidth()-getEdgeWidth());
		int startX = x[node] - offset;
		int startY= y[node] - offset;
		if (treeDisplay.getOrientation()==TreeDisplay.RIGHT){
			startX -= getNodeWidth()-doubleOffset;
		} else if (treeDisplay.getOrientation()==TreeDisplay.DOWN)
			startY -= getNodeWidth()-doubleOffset;
		Polygon poly = new Polygon();
		poly.npoints=0;
		poly.addPoint(startX,startY);
		poly.addPoint(startX+getNodeWidth(),startY);
		poly.addPoint(startX+getNodeWidth(),startY+getNodeWidth());
		poly.addPoint(startX,startY+getNodeWidth());
		poly.addPoint(startX,startY);
		poly.npoints=5;
		return poly;
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
			if (DrawTreeUtil.inSquareLineBranch(treeDisplay, this.x, this.y, getEdgeWidth(), tree, node, x,y) || inNode(node,x,y)){
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


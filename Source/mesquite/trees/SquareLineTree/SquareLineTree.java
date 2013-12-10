
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
	int fixedTaxonDistance = 0;
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
		orientationName = new MesquiteString("Up");
		ornt = TreeDisplay.UP;
		MesquiteSubmenuSpec orientationSubmenu = addSubmenu(null, "Orientation");
		orientationSubmenu.setSelected(orientationName);
		addItemToSubmenu(null, orientationSubmenu, "Up", makeCommand("orientUp",  this));
		addItemToSubmenu(null, orientationSubmenu, "Right", makeCommand("orientRight",  this));
		addItemToSubmenu(null, orientationSubmenu, "Down", makeCommand("orientDown",  this));
		addItemToSubmenu(null, orientationSubmenu, "Left", makeCommand("orientLeft",  this));

		
		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		addMenuItem( "Fixed Distance Between Taxa...", makeCommand("setFixedTaxonDistance",  this));
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
		temp.addLine("setFixedTaxonDistance " + fixedTaxonDistance); 
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
		else 	if (checker.compare(this.getClass(), "Sets a fixed distance between taxa for drawing the tree", "[distance in pixels]", commandName, "setFixedTaxonDistance")) {

			int newDistance= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newDistance))
				newDistance = MesquiteInteger.queryInteger(containerOfModule(), "Set taxon distance", "Distance between taxa:", "(A value of 0 will tell Mesquite to use the default taxon spacing)", "", fixedTaxonDistance, 0, 99);
			if (newDistance>=0 && newDistance<100 && newDistance!=fixedTaxonDistance) {
				fixedTaxonDistance=newDistance;
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					SquareLineTreeDrawing treeDrawing = (SquareLineTreeDrawing)obj;
					treeDrawing.treeDisplay.setFixedTaxonSpacing(newDistance);
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
		Debugg.println("drawHighlight " + node);
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
				DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, x, y, getEdgeWidth(), tree, g, node, 0, edgewidth,0, emphasizeNodes(), nodePoly(node), defaultStroke);
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
		int ew = edgewidth-1;
		int halfEW = edgewidth/2+2;
		if (treeDisplay.getOrientation()==treeDisplay.UP) 
			box = new Rectangle(x[node], y[node]-ew-halfEW, ew, ew);
		else if (treeDisplay.getOrientation()==treeDisplay.DOWN)
			box = new Rectangle(x[node], y[node]+halfEW, ew, ew);
		else  if (treeDisplay.getOrientation()==treeDisplay.RIGHT) 
			box = new Rectangle(x[node]+halfEW, y[node], ew, ew);
		else  if (treeDisplay.getOrientation()==treeDisplay.LEFT)
			box = new Rectangle(x[node]-ew-halfEW, y[node], ew, ew);
		else 
			box = new Rectangle(x[node], y[node], ew, ew);
		g.fillRect(box.x, box.y, box.width, box.height);
		g.setColor(treeDisplay.getBranchColor(node));
		g.drawRect(box.x, box.y, box.width, box.height);
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		Rectangle box;
		int numColors = colors.getNumColors();
		int ew = edgewidth-1;
		int halfEW = edgewidth/2+2;
		if (treeDisplay.getOrientation()==treeDisplay.UP) 
			box = new Rectangle(x[node], y[node]-ew-halfEW, ew, ew);
		else if (treeDisplay.getOrientation()==treeDisplay.DOWN)
			box = new Rectangle(x[node], y[node]+halfEW, ew, ew);
		else  if (treeDisplay.getOrientation()==treeDisplay.RIGHT) 
			box = new Rectangle(x[node]+halfEW, y[node], ew, ew);
		else  if (treeDisplay.getOrientation()==treeDisplay.LEFT)
			box = new Rectangle(x[node]-ew-halfEW, y[node], ew, ew);
		else 
			box = new Rectangle(x[node], y[node], ew, ew);
		for (int i=0; i<numColors; i++) {
			g.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
			g.fillRect(box.x + (i*box.width/numColors), box.y, box.width-  (i*box.width/numColors), box.height);
		}
		g.setColor(treeDisplay.getBranchColor(node));
		g.drawRect(box.x, box.y, box.width, box.height);
	}
	/*_________________________________________________*/
	public  int findTerminalBox(Tree tree, int drawnRoot, int x, int y){
		return -1;
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			Color c = g.getColor();
			float fillWidth = edgewidth;
			if (ownerModule.getShowEdgeLines())
				fillWidth = edgewidth-2*inset;
			int numColors = colors.getNumColors();
			for (int i=0; i<numColors; i++) {
				Color color;
				if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
					g.setColor(color);
				//			public static   void drawOneBranch(TreeDisplay treeDisplay, int[] x, int[] y, int edgewidth, Tree tree, Graphics g, int node, int start, int width, int adj, boolean emphasizeNodes, Polygon nodePoly, BasicStroke defaultStroke) {

				DrawTreeUtil.drawOneSquareLineBranch(treeDisplay,x,y,getEdgeWidth(), treeDisplay.getTree(), g, node, inset + i*fillWidth/numColors,  (i+1)*fillWidth/numColors -i*fillWidth/numColors, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;
			}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			//if (ownerModule.getShowEdgeLines())
				DrawTreeUtil.drawOneSquareLineBranch(treeDisplay,x,y,getEdgeWidth(), tree, g, node, inset, edgewidth-inset*2, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;
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


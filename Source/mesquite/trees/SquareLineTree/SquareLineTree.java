
package mesquite.trees.SquareLineTree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.DrawTree;
import mesquite.lib.duties.DrawTreeCoordinator;
import mesquite.lib.duties.NodeLocsVH;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.SquareTipDrawer;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.trees.lib.DrawTreeUtil;
/* ======================================================================== */
public class SquareLineTree extends DrawTree implements SquareTipDrawer {
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
	DrawTreeCoordinator dtc;
	NodeLocsVH nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	Vector drawings;
	static final int defaultEdgeWidth = 4;
	int oldEdgeWidth = MesquiteInteger.unassigned;
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
		dtc = (DrawTreeCoordinator)findEmployerWithDuty(DrawTreeCoordinator.class);
		/*if (dtc != null) {
			int mem = dtc.getTreeDrawTaskEdgeWidthMemory();
			if (MesquiteInteger.isCombinable(mem)){
				oldEdgeWidth = mem;
			}
		}*/
		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		addCheckMenuItem(null,"Show Edge Lines", makeCommand("showEdgeLines",  this), showEdgeLines);
		return true;

	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		if (!MesquiteInteger.isCombinable(oldEdgeWidth))
			oldEdgeWidth = dtc.getTreeDrawTaskEdgeWidthMemory();
		if (!MesquiteInteger.isCombinable(oldEdgeWidth)) {
			oldEdgeWidth = defaultEdgeWidth;
			dtc.recordTreeDrawTaskEdgeWidthMemory(oldEdgeWidth);
		}
		SquareLineTreeDrawing treeDrawing =  new SquareLineTreeDrawing (treeDisplay, numTaxa, this);
		treeDisplay.collapsedCladeNameAtLeftmostAncestor = true;

		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	public Vector getDrawings(){
		return drawings;
	}
	public boolean legalOrientation (int orientation){
		return (orientation == TreeDisplay.UP || orientation == TreeDisplay.DOWN || orientation == TreeDisplay.RIGHT || orientation == TreeDisplay.LEFT);
	}


	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setNodeLocs", nodeLocsTask);
		temp.addLine("setEdgeWidth " + oldEdgeWidth); 
		temp.addLine("showEdgeLines " + showEdgeLines.toOffOnString()); 

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
				dtc.recordTreeDrawTaskEdgeWidthMemory(oldEdgeWidth);
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					SquareLineTreeDrawing treeDrawing = (SquareLineTreeDrawing)obj;
					treeDrawing.setEdgeWidth(newWidth);
					treeDrawing.treeDisplay.setMinimumTaxonNameDistanceFromTip(newWidth, 6); 
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
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {  //for legacy scripts
			if (nodeLocsTask != null)
				nodeLocsTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at the bottom", null, commandName, "orientDown")) {//for legacy scripts
			if (nodeLocsTask != null)
				nodeLocsTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at right", null, commandName, "orientRight")) {//for legacy scripts
			if (nodeLocsTask != null)
				nodeLocsTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at left", null, commandName, "orientLeft")) {//for legacy scripts
			if (nodeLocsTask != null)
				nodeLocsTask.doCommand(commandName, arguments, checker);
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
class SquareLineTreeDrawing extends TreeDrawing  {

	public int highlightedBranch, branchFrom;
	public int xFrom, yFrom, xTo, yTo;
	public SquareLineTree ownerModule;
	public int edgewidth = 4;
	
	public int preferredEdgeWidth;
	int oldNumTaxa = 0;
	float inset=(float)1.0;
	private boolean ready=false;
	BasicStroke defaultStroke, dashedStroke;

	private int foundBranch;

	public SquareLineTreeDrawing (TreeDisplay treeDisplay, int numTaxa, SquareLineTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		edgewidth = ownerModule.oldEdgeWidth;
		preferredEdgeWidth = ownerModule.dtc.getTreeDrawTaskEdgeWidthMemory();
		treeDisplay.setMinimumTaxonNameDistanceFromTip(edgewidth, 4); //better if only did this if tracing on
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;

		try{
			defaultStroke = new BasicStroke();
			dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 1}, 0);

		}
		catch (Throwable t){
		}
//		treeDisplay.setOrientation(ownerModule.nodeLocsTask.getDefaultOrientation());
		ready = true;
	}

	/*_________________________________________________*/
	private void calculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		if (treeDisplay.getOrientation() == TreeDisplay.LEFT || treeDisplay.getOrientation() == TreeDisplay.RIGHT ){
			lineBaseY[node]=y[node];
			lineBaseX[node]=x[tree.motherOfNode(node)];
		}
		else {
			lineBaseY[node]=y[tree.motherOfNode(node)];
			lineBaseX[node]=x[node];
	}
	/*	lineBaseY[node] = y[tree.motherOfNode(node)];
		lineBaseX[node] =  x[tree.motherOfNode(node)];
		*/
	}
	/*_________________________________________________*/
	

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

	// TODO: Check new code (getMiddleOfBranch copied [then modified] from SquareTreeDrawing).  Oliver.July.31.2015
	/**
	 * Set values for x and y coordinates of middle of branch; sets {@code angle} value 
	 * too, but unclear if this is ever used.
	 * 
	 *  @param	tree	the tree to extract coordinates from
	 *  @param	N	the node corresponding to branch of interest
	 *  @param	xValue	MesquiteNumber to store position on x-axis
	 *  @param	yValue	MesquiteNumber to store position on y-axis
	 *  @param	angle	MesquiteDouble...?
	 */
	public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if(tree==null || xValue==null || yValue==null)
			return;
		if(!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
		if(treeDisplay.getOrientation()==TreeDisplay.UP){
			xValue.setValue(x[N] + useEdgeWidth()/2);
			yValue.setValue(y[mother]+(y[N]-y[mother])/2 + useEdgeWidth()/2);
			angle.setValue(-Math.PI/2.0);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
			xValue.setValue(x[N] + useEdgeWidth()/2);
			yValue.setValue(y[N]+(y[mother]-y[N])/2 - useEdgeWidth()/2);
			angle.setValue(Math.PI/2.0);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			xValue.setValue(x[mother]+(x[N]-x[mother])/2 - useEdgeWidth()/2);
			yValue.setValue(y[N] + useEdgeWidth()/2);
			angle.setValue(0.0);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
			xValue.setValue(x[N]+(x[mother]-x[N])/2 + useEdgeWidth()/2);
			yValue.setValue(y[N] + useEdgeWidth()/2);
			angle.setValue(Math.PI);
		}
	}

	public void getSingletonLocation(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue){
		if(tree==null || xValue==null || yValue==null)
			return;
		if(!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
		int daughter = tree.firstDaughterOfNode(N);
		if(treeDisplay.getOrientation()==TreeDisplay.UP){
			xValue.setValue(x[daughter]);
			yValue.setValue(y[mother]+(y[daughter]-y[mother])/2);
		}
		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
			xValue.setValue(x[daughter]);
			yValue.setValue(y[mother]+(y[mother]-y[daughter])/2);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			//	int offset = (x[N]-x[mother])/2;
			xValue.setValue(x[mother]+(x[daughter]-x[mother])/2);
			yValue.setValue(y[daughter]);
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
			xValue.setValue(x[daughter]+(x[mother]-x[daughter])/2);
			yValue.setValue(y[daughter]);
		}
	}

	/*_________________________________________________*/
	public double getNodeValueTextBaseX(int node, int edgewidth, int stringwidth, int fontHeight, boolean horizontalText){
		double bX =x[node];
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
	public double getNodeValueTextBaseY(int node, int edgewidth, int stringwidth, int fontHeight, boolean horizontalText){
		double bY =y[node];
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

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot); //Graphics g removed as parameter May 02
		calculateLines(tree, drawnRoot);
		edgewidth = preferredEdgeWidth;
		if (treeDisplay.getTaxonSpacing()<edgewidth+2) {
			edgewidth= (int)treeDisplay.getTaxonSpacing()-2;
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
			GraphicsUtil.fillOval(g,x[node]-4, y[node], 8, 8);
		}
		else {
			GraphicsUtil.fillOval(g,x[node], y[node]-4, 8,8);
		}

		g.setColor(tC);
	}

/*	boolean branchVisible(Tree tree, int node){
		if (!tree.getRooted() && tree.getRoot()==node)
			return false;
		if (tree.withinCollapsedClade(node)){
			return (tree.nodeIsTerminal(node) && tree.leftmostTerminalOfNode(tree.deepestCollapsedAncestor(node)) ==node);
		}
		return true;
	}


	/*_________________________________________________*/
	private   void drawClade(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			g.setColor(treeDisplay.getBranchColor(node));
			if (tree.isVisibleEvenIfInCollapsed(node)) {
				if (tree.isLeftmostTerminalOfCollapsedClade(node)){
					DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, xSolid, ySolid, useEdgeWidth(), tree, g, null, node, 0, useEdgeWidth(),0, emphasizeNodes(), nodePoly(node), defaultStroke);
					DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, xDashed, yDashed, useEdgeWidth(), tree, g, null, node, 0, useEdgeWidth(),0, emphasizeNodes(), nodePoly(node), dashedStroke);
				}
				else if (!tree.withinCollapsedClade(node)) {
					DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, x, y, useEdgeWidth(), tree, g, null, node, 0, useEdgeWidth(),0, emphasizeNodes(), nodePoly(node), defaultStroke);
				}
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
			if (MesquiteTrunk.debugMode){
				treeDisplay.reportChain("SquareLineTree "  + StringUtil.getDateTimeWithSeconds()); 
				treeDisplay.enableChain(); //has to draw once, so this is called to start
			}

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
		int ew = useEdgeWidth()-1;
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
		if (getShowEdgeLines())
			localInset=inset;
		Rectangle2D box;
		Rectangle2D colorBox;
		Graphics2D g2 = (Graphics2D)g;
		int numColors = colors.getNumColors();
		float ew = useEdgeWidth()-1;
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
	 */
	/*_________________________________________________*/
	/*.................................................................................................................*/
	boolean getShowEdgeLines() {
		if (treeDisplay != null && treeDisplay.getTraceMode())
			return true;
		return ownerModule.showEdgeLines.getValue();
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			Color c = g.getColor();
			float localInset = 0;
			if (getShowEdgeLines())
				localInset=inset;
			float fillWidth = useEdgeWidth()-2*localInset;
			int numColors = colors.getNumColors();
			Color color;
			if (numColors<=1) {
				if ((color = colors.getColor(0, !tree.anySelected()|| tree.getSelected(node)))!=null)
					g.setColor(color);
				if (tree.isLeftmostTerminalOfCollapsedClade(node)){
					DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, xSolid, ySolid, useEdgeWidth(), treeDisplay.getTree(), g, colors, node, localInset,  fillWidth, 4,emphasizeNodes(),nodePoly(node), defaultStroke);
					DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, xDashed, yDashed, useEdgeWidth(), treeDisplay.getTree(), g, colors, node, localInset,  fillWidth, 4,emphasizeNodes(),nodePoly(node),  dashedStroke);
				}
				else if (!tree.withinCollapsedClade(node))
					DrawTreeUtil.drawOneSquareLineBranch(treeDisplay,x,y,useEdgeWidth(), treeDisplay.getTree(), g, colors, node, localInset,  fillWidth, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;

			}
			else
				for (int i=0; i<numColors; i++) {
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					float thickness = fillWidth/numColors;
					float start = i*thickness+localInset;
					if (tree.isLeftmostTerminalOfCollapsedClade(node)){
						DrawTreeUtil.fillOneSquareLineBranch(treeDisplay, xSolid, ySolid, useEdgeWidth(), treeDisplay.getTree(), g, colors, node, start,  thickness, localInset,emphasizeNodes(),nodePoly(node), defaultStroke);
						DrawTreeUtil.fillOneSquareLineBranch(treeDisplay, xDashed, yDashed, useEdgeWidth(), treeDisplay.getTree(), g, colors, node, start,  thickness, localInset,emphasizeNodes(),nodePoly(node), dashedStroke);
					}
					else if (!tree.withinCollapsedClade(node))
						DrawTreeUtil.fillOneSquareLineBranch(treeDisplay,x,y,useEdgeWidth(), treeDisplay.getTree(), g, colors, node, start,  thickness, localInset,emphasizeNodes(),nodePoly(node), defaultStroke) ;
				}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {

		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			//if (ownerModule.getShowEdgeLines())
			if (tree.isLeftmostTerminalOfCollapsedClade(node)){
				DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, xSolid, ySolid, useEdgeWidth(), tree, g, null, node, inset, useEdgeWidth()-inset*2, 4,emphasizeNodes(),nodePoly(node), defaultStroke);
				DrawTreeUtil.drawOneSquareLineBranch(treeDisplay, xDashed, yDashed, useEdgeWidth(), tree, g, null, node, inset, useEdgeWidth()-inset*2, 4,emphasizeNodes(),nodePoly(node), dashedStroke);
			}
			else if (!tree.withinCollapsedClade(node))
				DrawTreeUtil.drawOneSquareLineBranch(treeDisplay,x,y,useEdgeWidth(), tree, g, null, node, inset, useEdgeWidth()-inset*2, 4,emphasizeNodes(),nodePoly(node), defaultStroke) ;
		}
	}

	/*_________________________________________________*/
	public int getNodeWidth() {
		int w = useEdgeWidth()+4;
		if (w<MINNODEWIDTH) return MINNODEWIDTH;
		return w;
	}
	/*_________________________________________________*/
	public Path2D nodePoly(int node) {
		int offset = (getNodeWidth()-useEdgeWidth())/2;
		int doubleOffset = (getNodeWidth()-useEdgeWidth());
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
			if (DrawTreeUtil.inSquareLineBranch(treeDisplay, this.x, this.y, useEdgeWidth(), (int)treeDisplay.getTaxonSpacing(), tree, node, x,y) || inNode(node,x,y)){
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
		treeDisplay.setOrientation(orientation);
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void setEdgeWidth(int edw) {
		edgewidth = edw;
		preferredEdgeWidth = edw;
	}
	
	int useEdgeWidth(){
		if (treeDisplay != null && treeDisplay.getTraceMode() && edgewidth<6)
			return 6;
		return edgewidth;
	}
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgewidth;
	}
}


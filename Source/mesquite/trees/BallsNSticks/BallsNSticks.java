/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BallsNSticks;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.trees.lib.*;

import java.awt.geom.*;

/* ======================================================================== */
public class BallsNSticks extends DrawTree {
	public String getName() {
		return "Balls & Sticks";
	}

	public String getExplanation() {
		return "Draws trees with spots and the nodes and thin lines for branches." ;
	}

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsVH.class, getName() + "  needs the locations of nodes to be calculated.",
		"The calculator for node locations is chosen automatically or initially");
	}
	NodeLocsVH nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	MesquiteString orientationName, lineStyleName;
	Vector drawings;
	int oldEdgeWidth = 2;
	int oldSpotSize = 22;
	int ornt, style;
	static final int DIAGONAL = 0;
	static final int SQUARE = 1;
	static final int CURVED = 2;
	public MesquiteBoolean cosmic = new MesquiteBoolean(false);
	public MesquiteBoolean ballsInternal = new MesquiteBoolean(true);
	MesquiteSubmenuSpec orientationSubmenu;
	MesquiteSubmenuSpec lineStyleSubmenu;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsVH)hireEmployee(NodeLocsVH.class, "Calculator of node locations");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node location module was obtained.");
		drawings = new Vector();
		//addMenuItem("Display", "Edge width...", makeCommand("edgeWidth"));
		defineMenus(false);
		ornt = NodeLocsVH.defaultOrientation;  //should take out of preferences
		orientationName = new MesquiteString(orient(ornt));
		orientationSubmenu.setSelected(orientationName);
		lineStyleName = new MesquiteString("Diagonal");
		style = DIAGONAL;
		lineStyleSubmenu.setSelected(lineStyleName);
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public void defineMenus(boolean accumulating){
		orientationSubmenu = addSubmenu(null, "Orientation");
		addItemToSubmenu(null, orientationSubmenu, "Up", makeCommand("orientUp",  this));
		addItemToSubmenu(null, orientationSubmenu, "Right", makeCommand("orientRight",  this));
		addItemToSubmenu(null, orientationSubmenu, "Down", makeCommand("orientDown",  this));
		addItemToSubmenu(null, orientationSubmenu, "Left", makeCommand("orientLeft",  this));
		lineStyleSubmenu = addSubmenu(null, "Line Style");
		addItemToSubmenu(null, lineStyleSubmenu, "Diagonal", makeCommand("useDiagonal",  this));
		addItemToSubmenu(null, lineStyleSubmenu, "Square", makeCommand("useSquare",  this));
		addItemToSubmenu(null, lineStyleSubmenu, "Curved", makeCommand("useCurved",  this));
		//		addCheckMenuItem( null, "Curved Lines", makeCommand("toggleArc",  this), useArc);
		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		addMenuItem( "Preferred Spot Size...", makeCommand("setSpotDiameter",  this));
		addCheckMenuItem( null, "Balls On Internal Nodes", makeCommand("toggleBallsInternal",  this), ballsInternal);
		addCheckMenuItem( null, "Cosmic", makeCommand("toggleCosmic",  this), cosmic);
	}
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		BallsNSticksDrawing treeDrawing =  new BallsNSticksDrawing (treeDisplay, numTaxa, this);
		if (legalOrientation(treeDisplay.getOrientation())){
			orientationName.setValue(orient(treeDisplay.getOrientation()));
			ornt = treeDisplay.getOrientation();
		}
		else
			treeDisplay.setOrientation(ornt);
		drawings.addElement(treeDrawing);
		//	treeDisplay.inhibitStretchByDefault = false;
		return treeDrawing;
	}
	public boolean legalOrientation (int orientation){
		return (orientation == TreeDisplay.UP || orientation == TreeDisplay.DOWN || orientation == TreeDisplay.RIGHT || orientation == TreeDisplay.LEFT);
	}
	/*.................................................................................................................
 	public void endJob() {
 		if (MesquiteTrunk.trackActivity) logln ("MesquiteModule " + getName() + "  closing down ");
 		//dtd.disposePolys(coordTask.treeDisplay.getTree(), coordTask.treeDisplay.getTree().getRoot());
		closeDownAllEmployees (this);
 		employees.removeElement(nodeLocsTask);
 		nodeLocsTask= null;
   	 }

	/*.................................................................................................................*/
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
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setSpotDiameter " + oldSpotSize); 
		temp.addLine("setEdgeWidth " + oldEdgeWidth); 
		if (ornt== TreeDisplay.UP)
			temp.addLine("orientUp"); 
		else if (ornt== TreeDisplay.DOWN)
			temp.addLine("orientDown"); 
		else if (ornt== TreeDisplay.LEFT)
			temp.addLine("orientLeft"); 
		else if (ornt== TreeDisplay.RIGHT)
			temp.addLine("orientRight"); 
		if (style== DIAGONAL)
			temp.addLine("useDiagonal"); 
		else if (style== SQUARE)
			temp.addLine("useSquare"); 
		else if (style== CURVED)
			temp.addLine("useCurved"); 

		temp.addLine("toggleBallsInternal " + ballsInternal.toOffOnString());
		temp.addLine("toggleCosmic " + cosmic.toOffOnString());

		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the diameter of the spots", "[diameter in pixels]", commandName, "setSpotDiameter")) {
			int newDiameter= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newDiameter))
				newDiameter = MesquiteInteger.queryInteger(containerOfModule(), "Set spot diameter", "Enter preferred diameter of spots at nodes.  This sets the preferred spot size; if there is not room in the drawing for spots so large, then the actual spot size may be smaller.", oldSpotSize, 6, 100);
			if (newDiameter>=6 && newDiameter<100 && newDiameter!=oldSpotSize) {
				Enumeration e = drawings.elements();
				oldSpotSize = newDiameter;
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					BallsNSticksDrawing treeDrawing = (BallsNSticksDrawing)obj;
					treeDrawing.spotSize=newDiameter;
					treeDrawing.preferredSpotSize = newDiameter;
					treeDrawing.treeDisplay.setMinimumTaxonNameDistance(0, treeDrawing.spotSize/2  + 4);
				}
				parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Sets how wide the branches of the tree are drawn", "[width in pixels]", commandName, "setEdgeWidth")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set edge width", "Edge Width:", oldEdgeWidth, 1, 24);
			if (newWidth>0 && newWidth<24 && newWidth!=oldEdgeWidth) {
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					BallsNSticksDrawing treeDrawing = (BallsNSticksDrawing)obj;
					treeDrawing.setEdgeWidth(newWidth);
				}
				oldEdgeWidth = newWidth;
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not \"cosmic\" mode is on", "[on or off]", commandName, "toggleCosmic")) {
			boolean current = cosmic.getValue();
			cosmic.toggleValue(parser.getFirstToken(arguments));
			if (current!=cosmic.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not balls are shown on internal nodes", "[on or off]", commandName, "toggleBallsInternal")) {
			boolean current = ballsInternal.getValue();
			ballsInternal.toggleValue(parser.getFirstToken(arguments));
			if (current!=ballsInternal.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not arcs are to be used", "[on or off]", commandName, "toggleArc")) {
			MesquiteBoolean useArc = new MesquiteBoolean(style == CURVED);  //here for compatibility with 1. 06 scripts
			boolean current = useArc.getValue();
			useArc.toggleValue(parser.getFirstToken(arguments));
			if (useArc.getValue()){
				style = CURVED;
				lineStyleName.setValue("Curved");
			}
			if (current!=useArc.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets line style", null, commandName, "useDiagonal")) {
			int current = style;
			style = DIAGONAL;
			lineStyleName.setValue("Diagonal");
			if (current!=style)
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets line style", null, commandName, "useSquare")) {
			int current = style;
			style = SQUARE;
			lineStyleName.setValue("Square");
			if (current!=style)
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets line style", null, commandName, "useCurved")) {
			int current = style;
			style = CURVED;
			lineStyleName.setValue("Curved");
			if (current!=style)
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Returns the module employed to set the node locations", null, commandName, "getNodeLocsEmployee")) {
			return nodeLocsTask;
		}
		else if (checker.compare(this.getClass(), "Orients the tree so that the terminals are pointing up", null, commandName, "orientUp")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				BallsNSticksDrawing treeDrawing = (BallsNSticksDrawing)obj;
				treeDrawing.reorient(TreeDisplay.UP);
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree so that the terminals are pointing down", null, commandName, "orientDown")) {
			Enumeration e = drawings.elements();
			ornt = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				BallsNSticksDrawing treeDrawing = (BallsNSticksDrawing)obj;
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
				BallsNSticksDrawing treeDrawing = (BallsNSticksDrawing)obj;
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
				BallsNSticksDrawing treeDrawing = (BallsNSticksDrawing)obj;
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
	/*.................................................................................................................*/
}

/* ======================================================================== */

/* ======================================================================== */
class BallsNSticksDrawing extends TreeDrawing  {
	public Path2D[] branchPoly;
	public Path2D[] touchPoly;

	public BallsNSticks ownerModule;
	int spotSize;
	int edgeWidth;
	public int preferredSpotSize = 22;
	int oldNumTaxa = 0;
	public static final int inset=1;
	private boolean ready=false;

	private int foundBranch;
	NameReference triangleNameRef;
	BasicStroke defaultStroke;

	public BallsNSticksDrawing (TreeDisplay treeDisplay, int numTaxa, BallsNSticks ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		triangleNameRef = NameReference.getNameReference("triangled");
		this.ownerModule = ownerModule;
		spotSize = ownerModule.oldSpotSize;
		edgeWidth = ownerModule.oldEdgeWidth;
		if (ownerModule.cosmic.getValue())
			edgeWidth = 8;
		try{
			defaultStroke = new BasicStroke();
		}
		catch (Throwable t){
		}
		treeDisplay.setMinimumTaxonNameDistance(0, spotSize/2+ 4);
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;
		treeDisplay.setOrientation(ownerModule.nodeLocsTask.getDefaultOrientation());
		ready = true;
	}
	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Path2D[numNodes];
		touchPoly= new Path2D[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new Path2D.Double();
			touchPoly[i] = new Path2D.Double();
		}
	}
	private boolean isUP(){
		return treeDisplay.getOrientation()==TreeDisplay.UP;
	}
	private boolean isDOWN(){
		return treeDisplay.getOrientation()==TreeDisplay.DOWN;
	}
	private boolean isLEFT(){
		return treeDisplay.getOrientation()==TreeDisplay.LEFT;
	}
	private boolean isRIGHT(){
		return treeDisplay.getOrientation()==TreeDisplay.RIGHT;
	}

	public void getSingletonLocation(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue){
		if(tree==null || xValue==null || yValue==null)
			return;
		if(!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
		int daughter = tree.firstDaughterOfNode(N);
		xValue.setValue(x[mother]+(x[daughter]-x[mother])/2);
		yValue.setValue(y[mother]+(y[daughter]-y[mother])/2);
	//	xValue.setValue(x[mother]+(x[N]-x[mother])/2);
	//	yValue.setValue(y[mother]+(y[N]-y[mother])/2);
		}

	/*_________________________________________________*/
	private void UPCalcBranchPolys(Tree tree, int node, Path2D[] polys, int width)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPCalcBranchPolys(tree, d, polys, width);
		if (ownerModule.style == BallsNSticks.DIAGONAL)
			DrawTreeUtil.UPdefineDiagonalPoly(this,polys[node], width, tree.nodeIsInternal(node), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		else
			DrawTreeUtil.UPdefineSquarePoly(this,polys[node], width, (node==tree.getRoot()), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)],0);
	}
	/*_________________________________________________*/
	private void DOWNCalcBranchPolys(Tree tree, int node, Path2D[] polys, int width)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcBranchPolys(tree, d, polys, width);
		//DOWNdefinePoly(polys[node], width, tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		if (ownerModule.style == BallsNSticks.DIAGONAL)
			DrawTreeUtil.DOWNdefineDiagonalPoly(this,polys[node], width, tree.nodeIsInternal(node), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		else
			DrawTreeUtil.DOWNdefineSquarePoly(this,polys[node], width,(node==tree.getRoot()), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)],0);
	}
	/*_________________________________________________*/
	private void RIGHTCalcBranchPolys(Tree tree, int node, Path2D[] polys, int width)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcBranchPolys(tree, d, polys, width);
		if (ownerModule.style == BallsNSticks.DIAGONAL)
			DrawTreeUtil.RIGHTdefineDiagonalPoly(this,polys[node], width, tree.nodeIsInternal(node), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		else
			DrawTreeUtil.RIGHTdefineSquarePoly(this,polys[node], width, (node==tree.getRoot()), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)],0);
	}
	/*_________________________________________________*/
	private void LEFTCalcBranchPolys(Tree tree, int node, Path2D[] polys, int width)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcBranchPolys(tree, d, polys, width);
		if (ownerModule.style == BallsNSticks.DIAGONAL)
			DrawTreeUtil.LEFTdefineDiagonalPoly(this,polys[node], width, tree.nodeIsInternal(node), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		else
			DrawTreeUtil.LEFTdefineSquarePoly(this,polys[node], width, (node==tree.getRoot()), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)],0);
	}
	/*_________________________________________________*/
	private void calculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		if (ownerModule.style == BallsNSticks.SQUARE) {
			if (treeDisplay.getOrientation()==TreeDisplay.UP || treeDisplay.getOrientation()==TreeDisplay.DOWN){
				lineBaseY[node]=y[tree.motherOfNode(node)];
				lineBaseX[node]=x[node];
			} else {
				lineBaseY[node]=y[node];
				lineBaseX[node]=x[tree.motherOfNode(node)];
			}
		} else {
			lineBaseY[node]=y[tree.motherOfNode(node)];
			lineBaseX[node]=x[tree.motherOfNode(node)];
		}
	}
	/*_________________________________________________*/
	private void calcBranchPolys(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField()); //Graphics g removed as parameter May 02
		calculateLines(tree, drawnRoot);
		spotSize = preferredSpotSize;
		if (treeDisplay.getTaxonSpacing()<spotSize+2) {
			spotSize= treeDisplay.getTaxonSpacing()-2;
			if (spotSize<2)
				spotSize=2;
		}
		treeDisplay.setMinimumTaxonNameDistance(0, spotSize/2+ 4);
		if (treeDisplay.getTaxonSpacing()<edgeWidth+2) {
			edgeWidth = treeDisplay.getTaxonSpacing()-2;
			if (edgeWidth<2)
				edgeWidth =2;
		}
		if (treeDisplay.getOrientation()==TreeDisplay.UP) {
			UPCalcBranchPolys(tree, drawnRoot, branchPoly, getEdgeWidth());
			UPCalcBranchPolys(tree, drawnRoot, touchPoly, getNodeWidth());
		}

		else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
			DOWNCalcBranchPolys(tree, drawnRoot, branchPoly, getEdgeWidth());
			DOWNCalcBranchPolys(tree, drawnRoot, touchPoly, getNodeWidth());
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
			RIGHTCalcBranchPolys(tree, drawnRoot, branchPoly, getEdgeWidth());
			RIGHTCalcBranchPolys(tree, drawnRoot, touchPoly, getNodeWidth());
		}
		else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
			LEFTCalcBranchPolys(tree, drawnRoot, branchPoly, getEdgeWidth());
			LEFTCalcBranchPolys(tree, drawnRoot, touchPoly, getNodeWidth());
		}
	}

	/*_________________________________________________*/
	/** Draw highlight for branch node with current color of graphics context */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		if (tree.nodeIsInternal(node) && !ownerModule.ballsInternal.getValue())
			return;
		Color tC = g.getColor();
		if (flip)
			g.setColor(Color.red);
		else
			g.setColor(Color.blue);

		for (int i=1; i<4; i++)
			GraphicsUtil.drawOval(g, (int)x[node]- spotSize/2 - 2 - i, (int)y[node]- spotSize/2 - 2 - i, spotSize + 3 + i + i, spotSize + 3 + i + i);  // integer nodeloc approxmation


		g.setColor(tC);
	}


	/*_________________________________________________*/
	public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if (tree==null || xValue==null || yValue==null)
			return;
		if (!tree.nodeExists(N))
			return;

		int mother = tree.motherOfNode(N);
		if (ownerModule.style == BallsNSticks.SQUARE) {
			if (treeDisplay.getOrientation()==TreeDisplay.UP){
				xValue.setValue(x[N]);
				yValue.setValue(y[mother]+(y[N]-y[mother])/2);
				angle.setValue(-Math.PI/2.0);
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){
				xValue.setValue(x[N]);
				yValue.setValue(y[N]+(y[mother]-y[N])/2);
				angle.setValue(Math.PI/2.0);
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.RIGHT){
				xValue.setValue(x[mother]+(x[N]-x[mother])/2);
				yValue.setValue(y[N]);
				angle.setValue(0.0);
			}
			else if (treeDisplay.getOrientation()==TreeDisplay.LEFT){
				xValue.setValue(x[N]+(x[mother]-x[N])/2);
				yValue.setValue(y[N]);
				angle.setValue(Math.PI);
			}
		}
		else if (ownerModule.style == BallsNSticks.CURVED){
			xValue.deassignAllValues();
			yValue.deassignAllValues();
		}
		else {
			xValue.setValue(GraphicsUtil.xCenterOfLine(x[mother], y[mother], x[N], y[N]));
			yValue.setValue(GraphicsUtil.yCenterOfLine(x[mother], y[mother], x[N], y[N]));
			angle.setValue(GraphicsUtil.angleOfLine(x[mother], y[mother], x[N], y[N]));
		}
	}
	/*_________________________________________________*/
	private   void drawJustOneBranch(Tree tree, Graphics g, int node) {
		g.setColor(treeDisplay.getBranchColor(node));
		if (ownerModule.style == BallsNSticks.SQUARE) {
			if (SHOWTOUCHPOLYS) {  //fordebugging
				Color prev = g.getColor();
				g.setColor(ColorDistribution.burlyWood);
				GraphicsUtil.fill(g,touchPoly[node]); 
				g.setColor(prev);
			}

			GraphicsUtil.fill(g,branchPoly[node]);  //TODO: no longer supports cosmic!
		}
		else if (ownerModule.style == BallsNSticks.CURVED)
			DrawTreeUtil.drawOneCurvedBranch(treeDisplay, x, y, getEdgeWidth(), tree, g, node, 0, getEdgeWidth(),0, emphasizeNodes(), null, defaultStroke);

		else {  //diagonal lines
			if (SHOWTOUCHPOLYS) {  //fordebugging
				Color prev = g.getColor();
				g.setColor(ColorDistribution.burlyWood);
				GraphicsUtil.fill(g,touchPoly[node]); 
				g.setColor(prev);
			}
			GraphicsUtil.fill(g,branchPoly[node]);  //TODO: no longer supports cosmic!

			// if (drawnRoot==node && tree.getRoot()!=node)
			if (tree.numberOfParentsOfNode(node)>1) { //for reticulate trees
				for (int i=1; i<=tree.numberOfParentsOfNode(node); i++) {
					int anc =tree.parentOfNode(node, i);
					if (anc!= tree.motherOfNode(node)) {
						GraphicsUtil.drawLine(g,x[node],y[node], x[tree.parentOfNode(node, i)],y[tree.parentOfNode(node, i)]);
						GraphicsUtil.drawLine(g,x[node]+1,y[node], x[tree.parentOfNode(node, i)]+1,y[tree.parentOfNode(node, i)]);
						GraphicsUtil.drawLine(g,x[node],y[node]+1, x[tree.parentOfNode(node, i)],y[tree.parentOfNode(node, i)]+1);
						GraphicsUtil.drawLine(g,x[node]+1,y[node]+1, x[tree.parentOfNode(node, i)]+1,y[tree.parentOfNode(node, i)]+1);
					}
				}
			}
		}
		if (tree.getAssociatedBit(triangleNameRef,node)) {
			for (int j=0; j<2; j++)
				for (int i=0; i<2; i++) {
					GraphicsUtil.drawLine(g,x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
					GraphicsUtil.drawLine(g,x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
					GraphicsUtil.drawLine(g,x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
				}
		}
	}
	/*_________________________________________________*/
	private   void drawBranches(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			if (!tree.getAssociatedBit(triangleNameRef,node))
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					drawBranches( tree, g, d);
			if (tree.getRooted() || tree.getRoot()!=node) {
				drawJustOneBranch(tree,g,node);
				if (!tree.nodeIsInternal(node) || ownerModule.ballsInternal.getValue()){
					drawSpot( g, node);
					if (emphasizeNodes()) {
						Color prev = g.getColor();
						g.setColor(Color.red);
						drawSpot( g, node);
						g.setColor(prev);
					}
				}
			}
		}
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
		if (MesquiteTree.OK(tree)) {
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			g.setColor(treeDisplay.branchColor);
			drawBranches(tree, g, drawnRoot);  
		}
	}
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
		if (MesquiteTree.OK(tree)) {
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			if (!tree.nodeExists(getDrawnRoot()))
				setDrawnRoot(tree.getRoot());
			calcBranchPolys(tree, getDrawnRoot());
		}
	}

	/*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		if ((x-this.x[node])*(x-this.x[node]) + (y-this.y[node])*(y-this.y[node]) < spotSize*spotSize/4) //use radius
			return true;
		else
			return false;
		// ask if x, y is in node's spot    g.fillOval( x[node]- spotSize/2, y[node]- spotSize/2, spotSize, spotSize);
	}
	/*_________________________________________________*/
	private void drawSpot(Graphics g, int node){
		GraphicsUtil.fillOval(g, x[node]- spotSize/2, y[node]- spotSize/2, spotSize, spotSize, ownerModule.cosmic.getValue());
	}
	/*_________________________________________________*/
	private void fillSpot(Graphics g, int node){
		GraphicsUtil.fillOval(g, x[node]- spotSize/2 + 2, y[node]- spotSize/2 + 2, spotSize - 4, spotSize - 4, ownerModule.cosmic.getValue());
	}
	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		fillBranch(tree, node, g);
		// need to show more informative terminal information
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		fillBranchWithColors(tree, node, colors, g);
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

	/*_________________________________________________*
	public void fillBranchWithMissingData(Tree tree, int node, Graphics g) {
		
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			Color c = g.getColor();
			if (g instanceof Graphics2D){
				Graphics2D g2 = (Graphics2D)g;
				g2.setPaint(GraphicsUtil.missingDataTexture);
			}
			else
				g.setColor(Color.lightGray);
			GraphicsUtil.fillOval(g, x[node]- spotSize/2, y[node]- spotSize/2, spotSize, spotSize, false);
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			Color c = g.getColor();
			int numColors = colors.getNumColors();
			if (numColors==1){
				g.setColor(colors.getColor(0, !tree.anySelected()|| tree.getSelected(node)));
				if (!tree.nodeIsInternal(node) || ownerModule.ballsInternal.getValue())
					fillSpot(g,node);
			}
			else if (numColors>0) {
				int startAngle=90;//was 270
				double totalFreq=0;
				for (int i=0; i<numColors; i++) totalFreq += colors.getWeight(i);
				int arcAngle = 0;
				for (int i=0; i<numColors; i++) {
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);

					arcAngle = (int)((colors.getWeight(i)/totalFreq)*360);
					GraphicsUtil.fillArc(g, x[node]- spotSize/2 + 2, y[node]- spotSize/2 + 2, spotSize - 4, spotSize - 4, startAngle, arcAngle, ownerModule.cosmic.getValue());
					startAngle+=arcAngle; 
				}
			}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	/** Does the basic inverting of the color of a branch **/
	public  void fillBranchInverted (Tree tree, int node, Graphics g) {
		if (tree.nodeIsInternal(node) && !ownerModule.ballsInternal.getValue())
			return;
		if (GraphicsUtil.useXORMode(g, true))  {
			g.setColor(Color.black);
			g.setXORMode(Color.white);  //for some reason color makes no difference in MacOS, but is inversion color in Win95 
			//GraphicsUtil.setToXOR(g);
			GraphicsUtil.drawOval(g,x[node]- spotSize/2 + 2, y[node]- spotSize/2 + 2, spotSize - 4, spotSize - 4);
			GraphicsUtil.drawOval(g,x[node]- spotSize/2 + 3, y[node]- spotSize/2 + 3, spotSize - 6, spotSize - 6);

			g.setPaintMode();
			g.setColor(Color.black);
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)) {
			//drawJustOneBranch(tree,g,node);
			if (!tree.nodeIsInternal(node) || ownerModule.ballsInternal.getValue())
				fillSpot(g,node);
		}
	}

	public  boolean isInTerminalBox(Tree tree, int node, int xPos, int yPos){
		return inBranch(tree, touchPoly, node, xPos, yPos);
	}
	/*_________________________________________________*/
	private boolean inBranch(Tree tree, Path2D[] polys, int node, int x, int y){
		if (ownerModule.style == BallsNSticks.DIAGONAL||ownerModule.style == BallsNSticks.SQUARE) {
			if (polys!=null && polys[node]!=null && polys[node].contains(x, y))
				return true;
		}
		else  if (ownerModule.style == BallsNSticks.CURVED)
			if (DrawTreeUtil.inBranch(treeDisplay, this.x, this.y, getEdgeWidth(), tree, node, x, y))
				return true;

		return false;
	}
	/*_________________________________________________*/
	private void ScanBranches(Tree tree,Path2D[] polys, int node, int x, int y, MesquiteDouble fraction)
	{
		if (foundBranch==0) {
			if (inBranch(tree,polys,node,x,y) || inNode(node, x, y)) {
				foundBranch = node;
				if (fraction!=null)
					if (inNode(node,x,y))
						fraction.setValue(ATNODE);
					else {
						int motherNode = tree.motherOfNode(node);
						fraction.setValue(EDGESTART);  //TODO: this is just temporary: need to calculate value along branch.
						if (tree.nodeExists(motherNode)) {
							fraction.setValue(GraphicsUtil.fractionAlongLine(x, y, this.x[motherNode], this.y[motherNode], this.x[node], this.y[node],isRIGHT()||isLEFT(), isUP()||isDOWN()));
						}
					}

			}
			if (!tree.getAssociatedBit(triangleNameRef, node)) {
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					ScanBranches(tree, polys, d, x, y, fraction);
			}

		}
	}
	/*_________________________________________________*/
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) {
		if (MesquiteTree.OK(tree) && ready) {
			foundBranch=0;
			ScanBranches(tree, branchPoly, drawnRoot, x, y, fraction);
			if (foundBranch==0 && getEdgeWidth()<ACCEPTABLETOUCHWIDTH && ownerModule.style != BallsNSticks.CURVED)
				ScanBranches(tree, touchPoly, drawnRoot, x, y, fraction);  //then scan through thicker versions
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
		edgeWidth = edw;
	}
	/*New code Feb.22.07 allows eavesdropping on edgewidth by the TreeDrawing oliver*/ //TODO: delete new code comments
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgeWidth;
	}
	/*End new code Feb.22.07 oliver*/
	/*_________________________________________________*/
	public   void dispose() { 
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = null;
			touchPoly[i] = null;
		}
		super.dispose();
	}

}



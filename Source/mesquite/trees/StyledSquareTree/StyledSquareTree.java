/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code.
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.StyledSquareTree;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class StyledSquareTree extends DrawTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsVH.class, getName() + "  needs the locations of nodes to be calculated.",
		"The calculator for node locations is chosen automatically or initially");
	}
	NodeLocsVH nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	MesquiteString orientationName;
	Vector drawings;
	int oldEdgeWidth = 6;
    int oldStemWidth = 2;
    int ornt;
	MesquiteString nodeLocsName;
	MesquiteBoolean simpleTriangle = new MesquiteBoolean(true);

    MesquiteInteger pos = new MesquiteInteger();

    /*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		drawings = new Vector();
		nodeLocsTask= (NodeLocsVH)hireNamedEmployee(NodeLocsVH.class, "#NodeLocsStandard");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node location module was obtained.");
		nodeLocsName = new MesquiteString(nodeLocsTask.getName());
		if (numModulesAvailable(NodeLocsVH.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Node Locations Calculator", makeCommand("setNodeLocs", this), NodeLocsVH.class);
			mss.setSelected(nodeLocsName);
		}


		MesquiteSubmenuSpec orientationSubmenu = addSubmenu(null, "Orientation");
		ornt = nodeLocsTask.getDefaultOrientation();
		orientationName = new MesquiteString("Up");
		if (ornt != TreeDisplay.UP &&  ornt != TreeDisplay.DOWN && ornt != TreeDisplay.LEFT && ornt != TreeDisplay.RIGHT)
			ornt = TreeDisplay.UP;
		orientationName.setValue(orient(ornt));
		orientationSubmenu.setSelected(orientationName);
		addItemToSubmenu(null, orientationSubmenu, "Up", makeCommand("orientUp",  this));
		addItemToSubmenu(null, orientationSubmenu, "Right", makeCommand("orientRight",  this));
		addItemToSubmenu(null, orientationSubmenu, "Down", makeCommand("orientDown",  this));
		addItemToSubmenu(null, orientationSubmenu, "Left", makeCommand("orientLeft",  this));
		addMenuItem( "Branch Width...", makeCommand("setEdgeWidth",  this));
        addMenuItem( "Stem Width...", makeCommand("setStemWidth",  this));
        //	addCheckMenuItem(null, "Simple Triangle for Triangled Clades", makeCommand("toggleSimpleTriangle",  this), simpleTriangle);
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		StyledSquareTreeDrawing treeDrawing =  new StyledSquareTreeDrawing (treeDisplay, numTaxa, this);
		if (legalOrientation(treeDisplay.getOrientation())){
			orientationName.setValue(orient(treeDisplay.getOrientation()));
			ornt = treeDisplay.getOrientation();
		}
		else
			treeDisplay.setOrientation(ornt);
		//treeDisplay.inhibitStretchByDefault = true;
		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	public boolean legalOrientation (int orientation){
		return (orientation == TreeDisplay.UP || orientation == TreeDisplay.DOWN || orientation == TreeDisplay.RIGHT || orientation == TreeDisplay.LEFT);
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
		else if (checker.compare(this.getClass(), "Sets the thickness of branches", "[width in pixels]", commandName, "setEdgeWidth")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set branch width", "Branch Width:", oldEdgeWidth, 1, 99);
			if (newWidth>0 && newWidth<100 && newWidth!=oldEdgeWidth) {
				oldEdgeWidth=newWidth;
				if (drawings == null)
					return null;
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing)obj;
                    treeDrawing.setProperty(TreeDrawing.ALLNODES,"edgewidth",String.valueOf(newWidth));
                    if (treeDrawing.treeDisplay != null)
						treeDrawing.treeDisplay.setMinimumTaxonNameDistance(treeDrawing.edgewidth, 5); //better if only did this if tracing on
				}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}

		}
        else if (checker.compare(this.getClass(), "Sets the color of the branches", "[color]", commandName, "setBranchColor")) {
            int newColor= MesquiteInteger.fromFirstToken(arguments, pos);
            if (drawings == null)
                return null;
            Enumeration e = drawings.elements();
            while (e.hasMoreElements()) {
                Object obj = e.nextElement();
                StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing)obj;
                treeDrawing.treeDisplay.branchColor = ColorDistribution.getStandardColor(newColor);
            }
            if (!MesquiteThread.isScripting()) parametersChanged();
        }
        else if (checker.compare(this.getClass(), "Sets the thickness of the branch stems", "[width in pixels]", commandName, "setStemWidth")) {
            int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
            if (!MesquiteInteger.isCombinable(newWidth))
                newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set stem width", "Stem Width:", oldStemWidth, 1, 99);
            if (newWidth>0 && newWidth<100 && newWidth!=oldStemWidth) {
                oldStemWidth=newWidth;
                if (drawings == null)
                    return null;
                Enumeration e = drawings.elements();
                while (e.hasMoreElements()) {
                    Object obj = e.nextElement();
                    StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing) obj;
                    treeDrawing.setProperty(TreeDrawing.ALLNODES,"stemwidth",String.valueOf(newWidth));
                }
                if (!MesquiteThread.isScripting()) parametersChanged();
            }

        }
        else if (checker.compare(this.getClass(), "Gets the thickness of the branch stems", "[width in pixels]", commandName, "getStemWidth")) {
            return String.valueOf(oldStemWidth);
        }
        else if (checker.compare(this.getClass(), "Gets the thickness of the branches", "[width in pixels]", commandName, "getBranchWidth")) {
            return String.valueOf(oldEdgeWidth);
        }
        else if (checker.compare(this.getClass(), "Returns the module calculating node locations", null, commandName, "getNodeLocsEmployee")) {
			return nodeLocsTask;
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.UP;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.UP);
				if (treeDrawing.treeDisplay != null)
					ornt = treeDrawing.treeDisplay.getOrientation();
			}
				orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at the bottom", null, commandName, "orientDown")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.DOWN;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.DOWN);
				if (treeDrawing.treeDisplay != null)
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at right", null, commandName, "orientRight")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.RIGHT;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.RIGHT);
				if (treeDrawing.treeDisplay != null)
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at left", null, commandName, "orientLeft")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.LEFT;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.LEFT);
				if (treeDrawing.treeDisplay != null)
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether to draw triangled clades as simple triangles or not.", "", commandName, "toggleSimpleTriangle")) {
 			boolean current = simpleTriangle.getValue();
 			simpleTriangle.toggleValue(parser.getFirstToken(arguments));
 			if (current!=simpleTriangle.getValue()) {
 				Enumeration e = drawings.elements();
 				while (e.hasMoreElements()) {
 					Object obj = e.nextElement();
 					StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing)obj;
 					treeDrawing.setSimpleTriangle(simpleTriangle.getValue());
 				}
 				parametersChanged();
 			}
		}
		else return super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Styled square tree";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Draws trees with styled square branches (\"phenogram\")" ;
	}
	/*.................................................................................................................*/

}


/* ======================================================================== */
class StyledSquareTreeDrawing extends TreeDrawing   {
	public Polygon[] branchPoly;
	public Polygon[] touchPoly;

	public StyledSquareTree ownerModule;
	public int edgewidth = 6;
	public int preferredEdgeWidth = 6;
    public int stemwidth = 2;
    int oldNumTaxa = 0;
	private int foundBranch;
	private boolean ready=false;
	private static final int  inset=1;
	private Polygon utilityPolygon;
	NameReference triangleNameRef;
    BasicStroke defaultStroke;


	public StyledSquareTreeDrawing(TreeDisplay treeDisplay, int numTaxa, StyledSquareTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		treeDisplay.setMinimumTaxonNameDistance(edgewidth, 5); //better if only did this if tracing on
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		triangleNameRef = NameReference.getNameReference("triangled");
		try{
			defaultStroke = new BasicStroke();
		}
		catch (Throwable t){
		}

		oldNumTaxa = numTaxa;
		ready = true;
		utilityPolygon=new Polygon();
		utilityPolygon.xpoints = new int[16];
		utilityPolygon.ypoints = new int[16];
		utilityPolygon.npoints=16;
	}

	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Polygon[numNodes];
		touchPoly= new Polygon[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new Polygon();
			branchPoly[i].xpoints = new int[16];
			branchPoly[i].ypoints = new int[16];
			branchPoly[i].npoints=16;
			touchPoly[i] = new Polygon();
			touchPoly[i].xpoints = new int[16];
			touchPoly[i].ypoints = new int[16];
			touchPoly[i].npoints=16;
		}
	}

    public int getOrientation() {
        if (isUP()) {
            return UP;
        } else if (isDOWN()) {
            return DOWN;
        } else if (isRIGHT()) {
            return RIGHT;
        } else if (isLEFT()) {
            return LEFT;
        }
        return 0;
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

	private void calcBranchPolys(Tree tree, int node, Polygon[] polys, int width) {
        int newwidth = treeDisplay.getBranchWidth(node) + width;
        if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
            for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
                calcBranchPolys(tree, d, polys, width);
        }
        definePolyForNode(tree, polys[node], node, newwidth);
	}

	/*_________________________________________________*/
	private void calcBranches(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField());
		edgewidth = preferredEdgeWidth;
		if (treeDisplay.getTaxonSpacing()<edgewidth+2) {
			edgewidth= treeDisplay.getTaxonSpacing()-2;
			if (edgewidth<2)
				edgewidth=2;
		}
		treeDisplay.setMinimumTaxonNameDistance(edgewidth, 5);
		calcBranchPolys(tree, drawnRoot, branchPoly, edgewidth);
		calcBranchPolys(tree, drawnRoot, touchPoly, getNodeWidth());
        calculateLines(tree, drawnRoot);
    }

    private void calculateLines(Tree tree, int node) {
        for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
            calculateLines( tree, d);
        }
        lineTipY[node]=y[node];
        lineTipX[node]=x[node];
        if (isUP() || isDOWN()) {
            lineBaseY[node]=y[tree.motherOfNode(node)];
            lineBaseX[node]=x[node];
        } else if (isRIGHT() || isLEFT()) {
            lineBaseY[node]=y[node];
            lineBaseX[node]=x[tree.motherOfNode(node)];
        }
    }

    public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if(tree==null || xValue==null || yValue==null)
			return;
		if(!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
        Point parent = new Point(x[mother],y[mother]);
        Point child = new Point(x[N],y[N]);
        Point middle = pointRotatedToUp(getOrientation(),parent,child);
        middle.translate(0,(parent.y-middle.y)/2);
        middle = pointRotatedFromUp(getOrientation(),parent,middle);

        xValue.setValue(middle.x);
        yValue.setValue(middle.y);
	}

    private void definePolyForNode(Tree tree, Polygon poly, int node, int width) {
        int direction = getOrientation();
        Point parent = new Point(x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
        Point child = new Point(x[node],y[node]);
        int sign = 1;
        if ((direction == DOWN) || (direction == LEFT)) {
            sign = -1;
        }
        child = pointRotatedToUp(direction,parent,child);
        poly.reset();
        int right_offset = sign*width/2;
        int left_offset = sign*width/2;
        if (right_offset == 0) {
            right_offset = sign;
            left_offset = 0;
        }
        if (node==tree.getRoot()) {
            poly.addPoint(child.x-left_offset, child.y); //root left
            poly.addPoint(child.x+right_offset, child.y);	 //root right
            poly.addPoint(child.x+right_offset, parent.y); //subroot right
            poly.addPoint(child.x-left_offset, parent.y); //subroot left
            poly.addPoint(child.x-left_offset, child.y); //return to root left
        } else {
            if (child.x<parent.x) {       // branch on the left of the parent node
                poly.addPoint(child.x-left_offset, child.y); // daughter left
                poly.addPoint(child.x+right_offset, child.y);	//daughter right
                poly.addPoint(child.x+right_offset, parent.y); //corner right
                poly.addPoint(parent.x, parent.y); //mother up
                poly.addPoint(parent.x, parent.y+(sign*stemwidth)); //mother down
                poly.addPoint(child.x-left_offset, parent.y+(sign*stemwidth)); //corner left
                poly.addPoint(child.x-left_offset, child.y); //return to daughter left
            } else {       // branch on the right of the parent node
                poly.addPoint(child.x-left_offset, child.y);// daughter left
                poly.addPoint(child.x+right_offset, child.y);// daughter right
                poly.addPoint(child.x+right_offset, parent.y+(sign*stemwidth));// corner right
                poly.addPoint(parent.x, parent.y+(sign*stemwidth)); //mother down
                poly.addPoint(parent.x, parent.y); //mother up
                poly.addPoint(child.x-left_offset, parent.y); //corner left
                poly.addPoint(child.x-left_offset, child.y); //return to daughter left
            }
        }
        rotateFromUp(poly,direction,parent);
	}

/*_________________________________________________*/
	private void drawOneBranch(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			boolean draw = branchIsVisible(node);
			if (draw){
				g.setColor(treeDisplay.getBranchColor(node));
				if ((tree.getRooted() || tree.getRoot()!=node) && branchPoly[node] != null){
                    g.fillPolygon(branchPoly[node]);
				}
				if (tree.numberOfParentsOfNode(node)>1) {
					for (int i=1; i<=tree.numberOfParentsOfNode(node); i++) {
						int anc =tree.parentOfNode(node, i);
						if (anc!= tree.motherOfNode(node)) {
							g.drawLine(x[node],y[node], x[tree.parentOfNode(node, i)],y[tree.parentOfNode(node, i)]);
							g.drawLine(x[node]+1,y[node], x[tree.parentOfNode(node, i)]+1,y[tree.parentOfNode(node, i)]);
							g.drawLine(x[node],y[node]+1, x[tree.parentOfNode(node, i)],y[tree.parentOfNode(node, i)]+1);
							g.drawLine(x[node]+1,y[node]+1, x[tree.parentOfNode(node, i)]+1,y[tree.parentOfNode(node, i)]+1);
						}
					}
				}
				if (tree.getAssociatedBit(triangleNameRef,node))
					if (treeDisplay.getSimpleTriangle()) {
						for (int j=0; j<2; j++)
							for (int i=0; i<2; i++) {
								g.drawLine(x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
								g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
								g.drawLine(x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
							}
					}
			}
			if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle())
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					drawOneBranch(tree, g, d);

			if (draw && emphasizeNodes()) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				g.fillPolygon(branchPoly[node]);
                g.setColor(prev);
			}
		}
	}

	/*_________________________________________________*/
	public void drawTree(Tree tree, int drawnRoot, Graphics g) {
		if (MesquiteTree.OK(tree)) {
			if (treeDisplay == null)
				return;
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			g.setColor(treeDisplay.branchColor);
			drawOneBranch(tree, g, drawnRoot);
		}
	}
	/*_________________________________________________*/
	public void recalculatePositions(Tree tree) {
		if (MesquiteTree.OK(tree)) {
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			if (!tree.nodeExists(getDrawnRoot()))
				setDrawnRoot(tree.getRoot());
			calcBranches(tree, getDrawnRoot());
		}
	}
	/*_________________________________________________*/
	/** Draw highlight for branch node with current color of graphics context */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		Color tC = g.getColor();
		if (flip)
			g.setColor(Color.red);
		else
			g.setColor(Color.blue);
		if (isDOWN() || isUP()){
			for (int i=0; i<4; i++)
				g.drawLine(x[node]-2 - i, y[node], x[node]-2 - i, y[tree.motherOfNode(node)]);
		}
		else {
			for (int i=0; i<4; i++)
				g.drawLine(x[node], y[node]-2 - i, x[tree.motherOfNode(node)], y[node]-2 - i);
		}
		g.setColor(tC);
	}
	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle box;
		int ew = edgewidth-1;
		if (isUP())
			box = new Rectangle(x[node], y[node]-ew-3, ew, ew);
		else if (isDOWN())
			box = new Rectangle(x[node], y[node]+1, ew, ew);
		else  if (isRIGHT())
			box = new Rectangle(x[node]+1, y[node], ew, ew);
		else  if (isLEFT())
			box = new Rectangle(x[node]-ew-3, y[node], ew, ew);
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
		if (isUP())
			box = new Rectangle(x[node], y[node]-ew-3, ew, ew);
		else if (isDOWN())
			box = new Rectangle(x[node], y[node]+1, ew, ew);
		else  if (isRIGHT())
			box = new Rectangle(x[node]+1, y[node], ew, ew);
		else  if (isLEFT())
			box = new Rectangle(x[node]-ew-3, y[node], ew, ew);
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
	private boolean ancestorIsTriangled(Tree tree, int node) {
		return tree.ancestorHasNameReference(triangleNameRef, node);
	}
	public boolean branchIsVisible(int node){
		try {
		if (node >=0 && node <  branchPoly.length)
			return treeDisplay.getVisRect() == null || branchPoly[node].intersects(treeDisplay.getVisRect());
		}
		catch (Throwable t){
		}
		return false;
	}
	/*_________________________________________________*/
	public void fillBranch(Tree tree, int node, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && node<branchPoly.length && branchIsVisible(node)){
            g.fillPolygon(branchPoly[node]);
        }
	}

	/** Fill branch N with indicated set of colors as a sequence, e.g. for stochastic character mapping.  This is not abstract because many tree drawers would have difficulty implementing it */
	public void fillBranchWithColorSequence(Tree tree, int node, ColorEventVector colors, Graphics g){
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			Color c = g.getColor();
			int numEvents = colors.size();
			g.setColor(Color.lightGray);
			fillBranch(tree, node, g);
            for (int i=numEvents-1; i>=0; i--) {
                ColorEvent e = (ColorEvent)colors.elementAt(i);
//                if (i == numEvents-1)
//                else {
//                    ColorEvent ec = (ColorEvent)colors.elementAt(i+1);
//                }
                definePolyForNode(tree, utilityPolygon, node, edgewidth);
                g.setColor(e.getColor());
                g.fillPolygon(utilityPolygon);
                g.setColor(Color.black);
                g.drawPolygon(utilityPolygon);
            }
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			Color c = g.getColor();
			int numColors = colors.getNumColors();
				for (int i=0; i<numColors; i++) {
                    definePolyForNode(tree, utilityPolygon, node, edgewidth);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
                    g.setColor(Color.black);
                    g.drawPolygon(utilityPolygon);
				}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public Polygon nodePoly(int node) {
		return branchPoly[node];
	}
	/*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		Polygon nodeP = nodePoly(node);
		return (nodeP!=null && nodeP.contains(x,y));
	}
	/*_________________________________________________*/
	private void ScanBranches(Tree tree, Polygon[] polys, int node, int x, int y, MesquiteDouble fraction)
	{
		if (foundBranch==0) {
			if (polys != null && polys[node] != null && polys[node].contains(x, y) || inNode(node,x,y)){
				foundBranch = node;
				if (fraction!=null)
					if (inNode(node,x,y))
						fraction.setValue(ATNODE);
					else {
						int motherNode = tree.motherOfNode(node);
						fraction.setValue(EDGESTART);  //TODO: this is just temporary: need to calculate value along branch.
						if (tree.nodeExists(motherNode)) {
							if (isUP() || isDOWN())  {
								fraction.setValue( Math.abs(1.0*(y-this.y[motherNode])/(this.y[node]-this.y[motherNode])));
							}
							else if (isRIGHT() || isLEFT()) {
								fraction.setValue( Math.abs(1.0*(x-this.x[motherNode])/(this.x[node]-this.x[motherNode])));
							}
						}
					}
			}
			if (!tree.getAssociatedBit(triangleNameRef, node) || !treeDisplay.getSimpleTriangle())
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					ScanBranches(tree, polys, d, x, y, fraction);

		}
	}
	/*_________________________________________________*/
	public int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) {
		if (MesquiteTree.OK(tree) && ready) {
			foundBranch=0;
			ScanBranches(tree, branchPoly, drawnRoot, x, y, fraction);
			if (edgewidth<ACCEPTABLETOUCHWIDTH)
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
		if (treeDisplay == null)
			return;
		treeDisplay.setOrientation(orientation);
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
	public void setSimpleTriangle(boolean simpleTriangle) {
		if (treeDisplay == null)
			return;
		treeDisplay.setSimpleTriangle(simpleTriangle);
		treeDisplay.pleaseUpdate(true);
	}
	/*_________________________________________________*/
    public void setProperty(int node, String property, String value) {
        if (property.equals("edgewidth")) {
            int edw =Integer.parseInt(value);
            if (node==ALLNODES) {
                edgewidth = edw;
                preferredEdgeWidth = edw;
            }
        } else if (property.equals("stemwidth")) {
            if (node==ALLNODES) {
                stemwidth = Integer.parseInt(value);
            }
        } else if (property.equals("setsimpletriangle")) {
            setSimpleTriangle(Boolean.parseBoolean(value));
        } else {
            super.setProperty(node,property,value);
        }
    }

    public void setEdgeWidth(int edw) {
        setProperty(ALLNODES,"edgewidth",String.valueOf(edw));
    }

	/*_________________________________________________*/
	public int getEdgeWidth() {
        return 0;
	}

    public int getNodeWidth() {
        int w = edgewidth+4;
        if (w<MINNODEWIDTH) return MINNODEWIDTH;
        return w;
    }

    /*_________________________________________________*/
	public void dispose() {
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = null;
			touchPoly[i] = null;
		}
		super.dispose();
	}
}


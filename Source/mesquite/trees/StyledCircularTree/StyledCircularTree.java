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
package mesquite.trees.StyledCircularTree;

import java.awt.geom.Line2D;
import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class StyledCircularTree extends DrawTree {
    public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
        EmployeeNeed e = registerEmployeeNeed(NodeLocsCircle.class, getName() + "  needs a method to calculate node locations.",
                "The method to calculate node locations is arranged initially");
    }

    NodeLocsCircle nodeLocsTask;
	MesquiteString orientationName;
	Vector drawings;
	int defaultBranchWidth = 2;
    int defaultStemWidth = 2;
    int defaultBranchColor;
    int orientation;
	MesquiteString nodeLocsName;
	MesquiteBoolean simpleTriangle = new MesquiteBoolean(true);

    /*.................................................................................................................*/
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
    public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
        StyledCircularTreeDrawing treeDrawing =  new StyledCircularTreeDrawing (treeDisplay, numTaxa, this);
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
		temp.addLine("setEdgeWidth " + defaultBranchWidth);
        temp.addLine("setStemWidth " + defaultStemWidth);
        if (orientation == TreeDisplay.UP)
			temp.addLine("orientUp");
		else if (orientation == TreeDisplay.DOWN)
			temp.addLine("orientDown");
		else if (orientation == TreeDisplay.LEFT)
			temp.addLine("orientLeft");
		else if (orientation == TreeDisplay.RIGHT)
			temp.addLine("orientRight");
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
        boolean reorient = false;
        if (checker.compare(this.getClass(), "Sets the node locations calculator", "[name of module]", commandName, "setNodeLocs")) {
			NodeLocsCircle temp = (NodeLocsCircle)replaceEmployee(NodeLocsCircle.class, arguments, "Node Locations Calculator", nodeLocsTask);
			if (temp != null) {
				nodeLocsTask = temp;
				nodeLocsName.setValue(nodeLocsTask.getName());
				parametersChanged();
			}
			return nodeLocsTask;
		} else if (checker.compare(this.getClass(), "Sets the thickness of branches", "[width in pixels]", commandName, "setEdgeWidth")) {
			int newWidth = MesquiteInteger.fromString(arguments);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set branch width", "Branch Width:", defaultBranchWidth, 1, 99);
			if ((newWidth != defaultBranchWidth) && (newWidth > 0)) {
				defaultBranchWidth = newWidth;
                if (drawings == null) return null;
                for (Enumeration e = drawings.elements(); e.hasMoreElements();) {
                    StyledCircularTreeDrawing treeDrawing = (StyledCircularTreeDrawing) e.nextElement();
                    treeDrawing.setProperty(TreeDrawing.ALLNODES,"branchwidth",String.valueOf(defaultBranchWidth));
                }
                if (!MesquiteThread.isScripting()) parametersChanged();
            }
        } else if (checker.compare(this.getClass(), "Sets the thickness of the branch stems", "[width in pixels]", commandName, "setStemWidth")) {
            int newWidth= MesquiteInteger.fromString(arguments) ;
            if (!MesquiteInteger.isCombinable(newWidth))
                newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set stem width", "Stem Width:", defaultStemWidth, 1, 99);
            if ((newWidth != defaultStemWidth) && (newWidth > 0)){
                defaultStemWidth = newWidth;
                if (drawings == null) return null;
                for (Enumeration e = drawings.elements(); e.hasMoreElements();) {
                    StyledCircularTreeDrawing treeDrawing = (StyledCircularTreeDrawing) e.nextElement();
                    treeDrawing.setProperty(TreeDrawing.ALLNODES,"stemwidth",String.valueOf(defaultStemWidth));
                }
                if (!MesquiteThread.isScripting()) parametersChanged();
            }
        } else if (checker.compare(this.getClass(), "Sets the color of the branches", "[color]", commandName, "setBranchColor")) {
            int newColor= MesquiteInteger.fromString(arguments);
            if (drawings == null) return null;
            for (Enumeration e = drawings.elements(); e.hasMoreElements();) {
                StyledCircularTreeDrawing treeDrawing = (StyledCircularTreeDrawing) e.nextElement();
                defaultBranchColor = newColor;
                treeDrawing.setProperty(TreeDrawing.ALLNODES,"branchcolor",String.valueOf(newColor));
            }
            if (!MesquiteThread.isScripting()) parametersChanged();
        } else if (checker.compare(this.getClass(), "Gets the thickness of the branch stems", "[width in pixels]", commandName, "getStemWidth")) {
            return String.valueOf(defaultStemWidth);
        } else if (checker.compare(this.getClass(), "Gets the thickness of the branches", "[width in pixels]", commandName, "getBranchWidth")) {
            return String.valueOf(defaultBranchWidth);
        } else if (checker.compare(this.getClass(), "Returns the module calculating node locations", null, commandName, "getNodeLocsEmployee")) {
			return nodeLocsTask;
		} else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {
			orientation = TreeDisplay.UP;
            reorient = true;
		} else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at the bottom", null, commandName, "orientDown")) {
			orientation = TreeDisplay.DOWN;
            reorient = true;
        } else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at right", null, commandName, "orientRight")) {
			orientation = TreeDisplay.RIGHT;
            reorient = true;
        } else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at left", null, commandName, "orientLeft")) {
			orientation = TreeDisplay.LEFT;
            reorient = true;
        } else if (checker.compare(this.getClass(), "Sets whether to draw triangled clades as simple triangles or not.", "", commandName, "toggleSimpleTriangle")) {
 			boolean current = simpleTriangle.getValue();
 			simpleTriangle.toggleValue(parser.getFirstToken(arguments));
 			if (current!=simpleTriangle.getValue()) {
                 Enumeration e = drawings.elements();
                 while (e.hasMoreElements()) {
 					Object obj = e.nextElement();
 					StyledCircularTreeDrawing treeDrawing = (StyledCircularTreeDrawing)obj;
 					treeDrawing.setSimpleTriangle(simpleTriangle.getValue());
 				}
 				parametersChanged();
 			}
		} else {
            return super.doCommand(commandName, arguments, checker);
        }

		if (reorient) {
            Enumeration e = drawings.elements();
            while (e.hasMoreElements()) {
                StyledCircularTreeDrawing treeDrawing = (StyledCircularTreeDrawing) e.nextElement();
                treeDrawing.reorient(orientation);
                if (treeDrawing.treeDisplay != null)
                    orientation = treeDrawing.treeDisplay.getOrientation();
            }
            orientationName.setValue(orient(orientation));
            parametersChanged();
        }
        return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Styled circular tree";
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
		return "Draws trees in circular form." ;
	}
	/*.................................................................................................................*/

}


/* ======================================================================== */
class StyledCircularTreeDrawing extends TreeDrawing   {
    public Shape[] branchPolys;

    public int highlightedBranch;
    public StyledCircularTree ownerModule;
    public int edgewidth = 15;
    private int branchwidth = 2;
    private int defaultBranchWidth = 2;
    int oldNumTaxa = 0;
    private int stemwidth = 2;
    public static final int inset=1;
    private boolean ready=false;
    public Polygon[] branchPoly;
    BasicStroke defaultStroke;

    private int foundBranch;
    NameReference triangleNameRef;
	public Polygon[] touchPoly;

	private Polygon utilityPolygon;


    public StyledCircularTreeDrawing (TreeDisplay treeDisplay, int numTaxa, StyledCircularTree ownerModule) {
        super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
        try{
            defaultStroke = new BasicStroke();
        }
        catch (Throwable t){
            MesquiteMessage.notifyUser(ownerModule.getName() + " couldn't start because Graphics2D is not available. " + t.toString());
        }
        triangleNameRef = NameReference.getNameReference("triangled");
        this.ownerModule = ownerModule;
        this.treeDisplay = treeDisplay;
        treeDisplay.setOrientation(TreeDisplay.CIRCULAR);
        oldNumTaxa = numTaxa;
        namesFollowLines = true;
        ready = true;
    }

	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Polygon[numNodes];
        branchPolys = new Line2D.Double[numNodes];

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

    /********/


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
		branchwidth = defaultBranchWidth;
		if (treeDisplay.getTaxonSpacing()< branchwidth +2) {
			branchwidth = treeDisplay.getTaxonSpacing()-2;
			if (branchwidth <2)
				branchwidth =2;
		}
		treeDisplay.setMinimumTaxonNameDistance(branchwidth, 5);
		calcBranchPolys(tree, drawnRoot, branchPoly, branchwidth);
    }

    public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if(tree==null || xValue==null || yValue==null)
			return;
		if(!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
        Point parent = new Point(x[mother],y[mother]);
        Point child = new Point(x[N],y[N]);
        Point middle = pointRotatedToUp(UP,parent,child);
        middle.translate(0,(parent.y-middle.y)/2);
        middle = pointRotatedFromUp(UP,parent,middle);

        xValue.setValue(middle.x);
        yValue.setValue(middle.y);
	}

    private void definePolyForNode(Tree tree, Polygon poly, int node, int width) {
        double parentRadius = ownerModule.nodeLocsTask.polarLength[tree.motherOfNode(node)];
        double childAngle = ownerModule.nodeLocsTask.angle[node];
        double childRadius = ownerModule.nodeLocsTask.polarLength[node];

        if (tree.nodeIsTerminal(node)) {
            childRadius += 2;
        }
//        double offset = (double)width/2;
//        //draw a rectangle of the appropriate size/radius
//        //should start from parent radius to child radius
//        //then rotate it to the angle of child
//        poly.reset();
//        poly.addPoint((int)childRadius,(int)-offset);
//        poly.addPoint((int)childRadius,(int)offset);
//        poly.addPoint((int)parentRadius,(int)offset);
//        poly.addPoint((int)parentRadius,(int)-offset);
//        poly.addPoint((int)childRadius,(int)-offset);
//        poly.translate(ownerModule.nodeLocsTask.treeCenter.x, ownerModule.nodeLocsTask.treeCenter.y);
//        rotatePoly(poly,ownerModule.nodeLocsTask.treeCenter,childAngle);

////        Point parent = new Point(x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
////        Point child = new Point(x[node],y[node]);
////        child = pointRotatedToUp(direction,parent,child);
//        if (tree.nodeIsTerminal(node)) {
//            child.y -= 2;
//        }

        Point newChild = new Point((int)childRadius+ownerModule.nodeLocsTask.treeCenter.x,ownerModule.nodeLocsTask.treeCenter.y);
        Point newParent = new Point((int)parentRadius+ownerModule.nodeLocsTask.treeCenter.x,ownerModule.nodeLocsTask.treeCenter.y);
        Shape newshape = new Line2D.Double(newChild,newParent);
        rotateLine2D((Line2D.Double)newshape,ownerModule.nodeLocsTask.treeCenter,childAngle);

        branchPolys[node] = newshape;
    }

    public Polygon stemPoly(Tree tree, int node) {
        int leftNode = tree.firstDaughterOfNode(node);
        int rightNode = tree.lastDaughterOfNode(node);
        Point leftEnd = new Point(x[leftNode],y[leftNode]);
        Point rightEnd = new Point(x[rightNode],y[rightNode]);
        Point parent = new Point(x[node],y[node]);
        int direction = UP;
        int sign = 1;
        leftEnd = pointRotatedToUp(direction,parent,leftEnd);
        rightEnd = pointRotatedToUp(direction,parent,rightEnd);
        int leftwidth = treeDisplay.getBranchWidth(leftNode) + branchwidth;
        int rightwidth = treeDisplay.getBranchWidth(rightNode) + branchwidth;
        leftEnd.y = parent.y;
        rightEnd.y = parent.y;
        int right_offset = sign*rightwidth/2;
        int left_offset = sign*leftwidth/2;
        if (right_offset == 0) {
            right_offset = sign;
            left_offset = 0;
        }

        Polygon stemPoly = new Polygon();
        stemPoly.addPoint(leftEnd.x - left_offset, leftEnd.y);
        stemPoly.addPoint(rightEnd.x + right_offset, rightEnd.y);
        stemPoly.addPoint(rightEnd.x + right_offset, rightEnd.y + stemwidth);
        stemPoly.addPoint(leftEnd.x - left_offset, leftEnd.y + stemwidth);
        stemPoly.addPoint(leftEnd.x - left_offset, leftEnd.y);
        rotateFromUp(stemPoly, parent, direction);
        return stemPoly;
    }


    /*_________________________________________________*/
	private void drawOneBranch(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
            // if this node has daughters, draw the stem bar.
            if (tree.numberOfDaughtersOfNode(node) > 1) {
                Color prev = g.getColor();
                g.setColor(treeDisplay.branchColor);
//                g.fillPolygon(stemPoly(tree, node));
                g.drawArc(ownerModule.nodeLocsTask.treeCenter.x,ownerModule.nodeLocsTask.treeCenter.y,200,200,0,45);
                g.setColor(prev);
            }

            if (branchIsVisible(node)){
				if ((tree.getRooted() || tree.getRoot()!=node) && branchPoly[node] != null){
                    Color prev = g.getColor();
                    g.setColor(treeDisplay.getBranchColor(node));
//                    g.fillPolygon(branchPoly[node]);
                    ((Graphics2D)g).fill(nodePoly(node));
                    g.setColor(prev);
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
				if (tree.getAssociatedBit(triangleNameRef,node)) {
					if (treeDisplay.getSimpleTriangle()) {
						for (int j=0; j<2; j++) {
							for (int i=0; i<2; i++) {
								g.drawLine(x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
								g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
								g.drawLine(x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
							}
                        }
					}
                }
			}
			if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
					drawOneBranch(tree, g, d);
                }
            }
			if (emphasizeNodes()) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				g.fillPolygon(branchPoly[node]);
                g.setColor(prev);
			}
		}
        Polygon test = new Polygon();
        test.addPoint(ownerModule.nodeLocsTask.treeCenter.x,ownerModule.nodeLocsTask.treeCenter.y);
        test.addPoint(ownerModule.nodeLocsTask.treeCenter.x+10,ownerModule.nodeLocsTask.treeCenter.y);
        test.addPoint(ownerModule.nodeLocsTask.treeCenter.x+5,ownerModule.nodeLocsTask.treeCenter.y+5);
        test.addPoint(ownerModule.nodeLocsTask.treeCenter.x,ownerModule.nodeLocsTask.treeCenter.y);
        g.setColor(Color.red);
        g.fillPolygon(test);

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
			for (int i=0; i<4; i++)
				g.drawLine(x[node]-2 - i, y[node], x[node]-2 - i, y[tree.motherOfNode(node)]);
		g.setColor(tC);
	}
	/*_________________________________________________*/
	public void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle box;
		int ew = branchwidth -1;
			box = new Rectangle(x[node], y[node]-ew-3, ew, ew);
		g.fillRect(box.x, box.y, box.width, box.height);
		g.setColor(treeDisplay.getBranchColor(node));
		g.drawRect(box.x, box.y, box.width, box.height);
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		Rectangle box;
		int numColors = colors.getNumColors();
		int ew = branchwidth -1;
			box = new Rectangle(x[node], y[node]-ew-3, ew, ew);
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
//                utilityPolygon = nodePoly(node);
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
//                    utilityPolygon = nodePoly(node);
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
    public Shape nodePoly(int node) {
        BasicStroke stroke = new BasicStroke(getBranchWidth(node),BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER);
        return stroke.createStrokedShape(branchPolys[node]);
    }


    /*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
        Shape nodeP = nodePoly(node);
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
								fraction.setValue( Math.abs(1.0*(y-this.y[motherNode])/(this.y[node]-this.y[motherNode])));
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
			if (branchwidth <ACCEPTABLETOUCHWIDTH)
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
        if (property.equals("branchwidth")) {
            int edw =Integer.parseInt(value);
            if (node==ALLNODES) {
                branchwidth = edw;
                defaultBranchWidth = edw;
            }
        } else if (property.equals("stemwidth")) {
            if (node==ALLNODES) {
                stemwidth = Integer.parseInt(value);
            }
        } else if (property.equals("branchcolor")) {
            if (node==ALLNODES) {
                treeDisplay.branchColor = ColorDistribution.getStandardColor(value);
            }
        } else if (property.equals("setsimpletriangle")) {
            setSimpleTriangle(Boolean.parseBoolean(value));
        } else {
            super.setProperty(node,property,value);
        }
    }

    public void setEdgeWidth(int edw) {
        setProperty(ALLNODES,"branchwidth",String.valueOf(edw));
    }

    public int getBranchWidth(int node) {
        int width = treeDisplay.getBranchWidth(node);
        if (width==0) {
            width = defaultBranchWidth;
        }
        return width;
    }
    /*_________________________________________________*/
	public int getEdgeWidth() {
        return 0;
	}

    public int getNodeWidth() {
        int w = branchwidth +4;
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


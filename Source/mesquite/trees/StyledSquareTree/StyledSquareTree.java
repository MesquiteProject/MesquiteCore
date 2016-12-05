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

import java.awt.geom.Line2D;
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
		orientation = nodeLocsTask.getDefaultOrientation();
		orientationName = new MesquiteString("Up");
		if (orientation != TreeDisplay.UP &&  orientation != TreeDisplay.DOWN && orientation != TreeDisplay.LEFT && orientation != TreeDisplay.RIGHT)
			orientation = TreeDisplay.UP;
		orientationName.setValue(orient(orientation));
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
	public TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		StyledSquareTreeDrawing treeDrawing = new StyledSquareTreeDrawing (treeDisplay, numTaxa, this);
		if (legalOrientation(treeDisplay.getOrientation())){
			orientationName.setValue(orient(treeDisplay.getOrientation()));
			orientation = treeDisplay.getOrientation();
		}
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
			NodeLocsVH temp = (NodeLocsVH)replaceEmployee(NodeLocsVH.class, arguments, "Node Locations Calculator", nodeLocsTask);
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
                    StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing) e.nextElement();
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
                    StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing) e.nextElement();
                    treeDrawing.setProperty(TreeDrawing.ALLNODES,"stemwidth",String.valueOf(defaultStemWidth));
                }
                if (!MesquiteThread.isScripting()) parametersChanged();
            }
        } else if (checker.compare(this.getClass(), "Sets the color of the branches", "[color]", commandName, "setBranchColor")) {
            int newColor= MesquiteInteger.fromString(arguments);
            if (drawings == null) return null;
            for (Enumeration e = drawings.elements(); e.hasMoreElements();) {
                StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing) e.nextElement();
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
 					StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing)obj;
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
                StyledSquareTreeDrawing treeDrawing = (StyledSquareTreeDrawing) e.nextElement();
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
    public Shape[] branchPolys;

	public StyledSquareTree ownerModule;
	private int branchwidth = 2;
	private int defaultBranchWidth = 2;
    private int stemwidth = 2;
    int oldNumTaxa = 0;
	private boolean ready=false;
	NameReference triangleNameRef;
    BasicStroke defaultStroke;


	public StyledSquareTreeDrawing(TreeDisplay treeDisplay, int numTaxa, StyledSquareTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		treeDisplay.setMinimumTaxonNameDistance(branchwidth, 5); //better if only did this if tracing on
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		triangleNameRef = NameReference.getNameReference("triangled");
		try{
			defaultStroke = new BasicStroke();
		}
		catch (Throwable t){
            MesquiteMessage.notifyUser(ownerModule.getName() + " couldn't start because Graphics2D is not available. " + t.toString());
        }

		oldNumTaxa = numTaxa;
		ready = true;
	}

	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
        branchPolys = new Line2D.Double[numNodes];

        for (int i=0; i<numNodes; i++) {
            branchPolys[i] = new Line2D.Double();
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

	private void calcBranchPolys(Tree tree, int node) {
        if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
            for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
                calcBranchPolys(tree, d);
        }
        definePolyForNode(tree, node);
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
		calcBranchPolys(tree, drawnRoot);
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

    private void definePolyForNode(Tree tree, int node) {
        int direction = getOrientation();
        Point parent = new Point(x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
        Point child = new Point(x[node],y[node]);
        child = pointRotatedToUp(direction,parent,child);
        if (tree.nodeIsTerminal(node)) {
            child.y -= 2;
        }

        Point newChild = new Point(child.x,child.y);
        newChild = pointRotatedFromUp(direction,parent,newChild);
        Point newParent = new Point(child.x,parent.y);
        newParent = pointRotatedFromUp(direction,parent,newParent);

        branchPolys[node] = new Line2D.Double(newChild,newParent);
    }
/*_________________________________________________*/
	private void drawOneBranch(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			if (branchIsVisible(node)) {
				if ((tree.getRooted() || tree.getRoot()!=node) && branchPolys[node] != null){
                    Color prev = g.getColor();
                    g.setColor(treeDisplay.getBranchColor(node));
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
			}
            if (tree.getAssociatedBit(triangleNameRef, node) && treeDisplay.getSimpleTriangle()) {
                Polygon triangle = new Polygon();
                int leftTerminal = tree.leftmostTerminalOfNode(node);
                int rightTerminal = tree.rightmostTerminalOfNode(node);
                triangle.addPoint(x[node],y[node]);
                triangle.addPoint(x[leftTerminal],y[leftTerminal]);
                triangle.addPoint(x[rightTerminal],y[rightTerminal]);
                triangle.addPoint(x[node],y[node]);
                ((Graphics2D)g).setStroke(new BasicStroke(branchwidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
                ((Graphics2D)g).draw(triangle);
            } else {
                for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
                    drawOneBranch(tree, g, d);
                }
                if (tree.numberOfDaughtersOfNode(node) > 1) { // if this node has daughters, draw the stem bar.
                    ((Graphics2D)g).fill(stemLine(tree, node));
                }
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
            ((Graphics2D)g).fill(nodePoly(node));
		}
		else {
            ((Graphics2D)g).fill(nodePoly(node));
        }
		g.setColor(tC);
	}
	/*_________________________________________________*/
	public void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle box;
		int ew = branchwidth -1;
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
		int ew = branchwidth -1;
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
            if (node >=0 && node <  branchPolys.length) {
                return treeDisplay.getVisRect() == null || branchPolys[node].intersects(treeDisplay.getVisRect());
            }
		}
		catch (Throwable t){
		}
		return false;
	}
	/*_________________________________________________*/
	public void fillBranch(Tree tree, int node, Graphics g) {
		if ((tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node)){
            ((Graphics2D)g).fill(nodePoly(node));
        }
	}

	/** Fill branch N with indicated set of colors as a sequence, e.g. for stochastic character mapping.  This is not abstract because many tree drawers would have difficulty implementing it */
	public void fillBranchWithColorSequence(Tree tree, int node, ColorEventVector colors, Graphics g){
		if ((tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			Color c = g.getColor();
			int numEvents = colors.size();
			g.setColor(Color.lightGray);
			fillBranch(tree, node, g);
            for (int i=numEvents-1; i>=0; i--) {
                ColorEvent e = (ColorEvent)colors.elementAt(i);
                Shape outlined = nodePoly(node);
                g.setColor(e.getColor());
                ((Graphics2D)g).draw(outlined);
                g.setColor(Color.black);
                ((Graphics2D)g).setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
                ((Graphics2D)g).draw(outlined);
            }
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if ((tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			Color c = g.getColor();
			int numColors = colors.getNumColors();
				for (int i=0; i<numColors; i++) {
                    Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
                    Shape outlined = nodePoly(node);
                    ((Graphics2D)g).fill(outlined);
                    g.setColor(Color.black);
                    ((Graphics2D)g).setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
                    ((Graphics2D)g).draw(outlined);
                }
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public Shape nodePoly(int node) {
        BasicStroke stroke = new BasicStroke(getBranchWidth(node),BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER);
        return stroke.createStrokedShape(branchPolys[node]);
	}

    public Shape stemLine(Tree tree, int node) {
        int leftNode = tree.firstDaughterOfNode(node);
        int rightNode = tree.lastDaughterOfNode(node);
        Point leftEnd = new Point(x[leftNode],y[leftNode]);
        Point rightEnd = new Point(x[rightNode],y[rightNode]);
        Point parent = new Point(x[node],y[node]);
        int direction = getOrientation();
        leftEnd = pointRotatedToUp(direction,parent,leftEnd);
        rightEnd = pointRotatedToUp(direction,parent,rightEnd);
        double leftwidth = (getBranchWidth(leftNode))/2;
        double rightwidth = (getBranchWidth(rightNode))/2;
        leftEnd.y = parent.y;
        rightEnd.y = parent.y;
        if (((rightEnd.x-leftEnd.x)<0) || ((rightEnd.y-leftEnd.y)<0)) {
            leftEnd.x += leftwidth;
            rightEnd.x -= rightwidth;
        } else {
            leftEnd.x -= leftwidth;
            rightEnd.x += rightwidth;
        }
        leftEnd = pointRotatedFromUp(direction,parent,leftEnd);
        rightEnd = pointRotatedFromUp(direction,parent,rightEnd);
        BasicStroke stroke = new BasicStroke(stemwidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER);
        return stroke.createStrokedShape(new Line2D.Double(leftEnd,rightEnd));
    }

    /*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		Shape nodeP = nodePoly(node);
		return (nodeP!=null && nodeP.contains(x,y));
	}
	/*_________________________________________________*/
	private int ScanBranches(Tree tree, int node, int x, int y, int found) {
		if (found==0) {
			if (branchPolys[node] != null && inNode(node,x,y)){
                found = node;
			} else if (!tree.getAssociatedBit(triangleNameRef, node) || !treeDisplay.getSimpleTriangle()) {
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
					found = ScanBranches(tree, d, x, y, found);
                }
            }
		}
        return found;
	}
	/*_________________________________________________*/
    public int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) {
        int found = 0;
		if (MesquiteTree.OK(tree) && ready) {
            found = ScanBranches(tree, drawnRoot, x, y, found);
			if (found == tree.getRoot() && !tree.getRooted())
				return 0;
			else
				return found;
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
			branchPolys[i] = null;
		}
		super.dispose();
	}
}


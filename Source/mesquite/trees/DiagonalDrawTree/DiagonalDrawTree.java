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
package mesquite.trees.DiagonalDrawTree;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Draws trees in a basic diagonal-branch style.  See SquareTree and others in mesquite.basic and mesquite.ornamental. */
public class DiagonalDrawTree extends DrawTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsVH.class, getName() + "  needs the locations of nodes to be calculated.",
		"The calculator for node locations is chosen automatically or initially");
	}

	NodeLocsVH nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	MesquiteString orientationName;
	Vector drawings;
	int oldEdgeWidth =12;
	int ornt;
	MesquiteString nodeLocsName;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsVH)hireNamedEmployee(NodeLocsVH.class, "#NodeLocsStandard");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node locator module obtained");
		nodeLocsName = new MesquiteString(nodeLocsTask.getName());
		if (numModulesAvailable(NodeLocsVH.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Node Locations Calculator", makeCommand("setNodeLocs", this), NodeLocsVH.class);
			mss.setSelected(nodeLocsName);
		}
		drawings = new Vector();
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
		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}

	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		DiagonalTreeDrawing treeDrawing =  new DiagonalTreeDrawing (treeDisplay, numTaxa, this);
		if (legalOrientation(treeDisplay.getOrientation())){
			orientationName.setValue(orient(treeDisplay.getOrientation()));
			ornt = treeDisplay.getOrientation();
		}
		else
			treeDisplay.setOrientation(ornt);
		drawings.addElement(treeDrawing);
		//treeDisplay.inhibitStretchByDefault = false;
		return treeDrawing;
	}
	public boolean legalOrientation (int orientation){
		return (orientation == TreeDisplay.UP || orientation == TreeDisplay.DOWN || orientation == TreeDisplay.RIGHT || orientation == TreeDisplay.LEFT);
	}
	/*.................................................................................................................*/
	public void endJob() {
		nodeLocsTask= null;
		drawings.removeAllElements();
		super.endJob();
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
		else if (checker.compare(this.getClass(), "Sets thickness of lines used to draw tree", "[width in pixels]", commandName, "setEdgeWidth")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set edge width", "Edge Width:", oldEdgeWidth, 1, 99);
			if (newWidth>0 && newWidth<100 && newWidth!=oldEdgeWidth) {
				oldEdgeWidth=newWidth;
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
					treeDrawing.setEdgeWidth(newWidth);
					treeDrawing.treeDisplay.setMinimumTaxonNameDistance(newWidth, 5); //better if only did this if tracing on
				}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Returns module calculating node locations", null, commandName, "getNodeLocsEmployee")) {
			return nodeLocsTask;
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.UP;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.UP);
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at the bottom", null, commandName, "orientDown")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.DOWN;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.DOWN);
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at right", null, commandName, "orientRight")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.RIGHT;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.RIGHT);
				if (treeDrawing.treeDisplay != null)
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are at left", null, commandName, "orientLeft")) {
			Enumeration e = drawings.elements();
			ornt =TreeDisplay.LEFT;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				DiagonalTreeDrawing treeDrawing = (DiagonalTreeDrawing)obj;
				treeDrawing.reorient(TreeDisplay.LEFT);
				if (treeDrawing.treeDisplay != null)
				ornt = treeDrawing.treeDisplay.getOrientation();
			}
			orientationName.setValue(orient(ornt));
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else {
			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Diagonal tree";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Draws trees with standard diagonal branches (\"cladogram\")" ;
	}
	/*.................................................................................................................*/
}

/* ======================================================================== */

/* ======================================================================== */
class DiagonalTreeDrawing extends TreeDrawing  {
	public Polygon[] branchPoly;
	public Polygon[] touchPoly;
	public Polygon[] fillBranchPoly;

	private int lastleft;
	private int taxspacing;
	public int highlightedBranch, branchFrom;
	public int xFrom, yFrom, xTo, yTo;
	public DiagonalDrawTree ownerModule;
	public int edgeWidth = 12;
	public int preferredEdgeWidth = 12;
	int oldNumTaxa = 0;
	Polygon utilityPolygon;
	public static final int inset=1;
	private boolean ready=false;

	private int foundBranch;
	NameReference triangleNameRef;
	NameReference widthNameReference;
	DoubleArray widths = null;
	double maxWidth = 0;
	public DiagonalTreeDrawing (TreeDisplay treeDisplay, int numTaxa, DiagonalDrawTree ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
		widthNameReference = NameReference.getNameReference("width");
		treeDisplay.setMinimumTaxonNameDistance(edgeWidth, 5); //better if only did this if tracing on
		triangleNameRef = NameReference.getNameReference("triangled");
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;
		ready = true;
		treeDisplay.setOrientation(ownerModule.nodeLocsTask.getDefaultOrientation());
		utilityPolygon=new Polygon();
		utilityPolygon.xpoints = new int[16];
		utilityPolygon.ypoints = new int[16];
		utilityPolygon.npoints=16;
	}
	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Polygon[numNodes];
		touchPoly= new Polygon[numNodes];
		fillBranchPoly= new Polygon[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new Polygon();
			branchPoly[i].xpoints = new int[16];
			branchPoly[i].ypoints = new int[16];
			branchPoly[i].npoints=16;
			touchPoly[i] = new Polygon();
			touchPoly[i].xpoints = new int[16];
			touchPoly[i].ypoints = new int[16];
			touchPoly[i].npoints=16;
			fillBranchPoly[i] = new Polygon();
			fillBranchPoly[i].xpoints = new int[16];
			fillBranchPoly[i].ypoints = new int[16];
			fillBranchPoly[i].npoints=16;
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
	int branchEdgeWidth(int node, boolean isTouch){
		if (widths !=null && maxWidth!=0 && MesquiteDouble.isCombinable(maxWidth)) {
			double w = widths.getValue(node);
			if (MesquiteDouble.isCombinable(w))
				if (isTouch)
					return (int)((w/maxWidth) * getNodeWidth());
				else
					return (int)((w/maxWidth) * edgeWidth);
		}	
		if (isTouch)
			return getNodeWidth();
		else
			return edgeWidth;
	}
	private int getOffset(int width, int node) {
		return (width-branchEdgeWidth(node,false))/2;
	}
	/*_________________________________________________*/
	private void UPdefineFillPoly(int node, Polygon poly, boolean isTouch, boolean internalNode, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		if (poly!=null) {
			int sliceWidth=branchEdgeWidth(node, isTouch);
			if (numSlices>1) {
				Nx+= (sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices;
				mNx+= (sliceNumber-1)*(branchEdgeWidth(node,isTouch)-inset)/numSlices;
				sliceWidth=(branchEdgeWidth(node, isTouch)-inset)-((sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices);
			}
			if ((internalNode) && (numSlices==1)){ 
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny);
				poly.addPoint(Nx+sliceWidth/2, Ny-sliceWidth/2-inset);
				poly.addPoint(Nx+sliceWidth-inset, Ny);
				poly.addPoint(mNx+sliceWidth-inset, mNy);
				poly.addPoint(mNx+inset, mNy);
				poly.addPoint(Nx+inset, Ny);
				poly.npoints=6;
			}
			else {
				if (Nx==mNx) {
					if ((internalNode) && (numSlices>1)) {
						Ny-=(branchEdgeWidth(node, isTouch)-inset)/4;
					}
					poly.npoints=0;
					poly.addPoint(Nx+inset, Ny+inset);
					poly.addPoint(Nx+sliceWidth-inset, Ny+inset);
					poly.addPoint(mNx+sliceWidth-inset, mNy);
					poly.addPoint(mNx+inset, mNy);
					poly.addPoint(Nx+inset, Ny+inset);
					poly.npoints=5;
				}
				else if (Nx>mNx) {
					if ((internalNode) && (numSlices>1)) {
						Nx+=(branchEdgeWidth(node, isTouch)-inset)/4;
						Ny-=(branchEdgeWidth(node, isTouch)-inset)/4;
					}
					poly.npoints=0;
					poly.addPoint(Nx, Ny+inset);
					poly.addPoint(Nx+sliceWidth-inset-inset, Ny+inset);
					poly.addPoint(mNx+sliceWidth-inset, mNy);
					poly.addPoint(mNx+inset, mNy);
					poly.addPoint(Nx, Ny+inset);
					poly.npoints=5;
				}
				else if (Nx<mNx) {
					if ((internalNode) && (numSlices>1)) {
						Nx-=(branchEdgeWidth(node, isTouch)-inset)/4;
						Ny-=(branchEdgeWidth(node, isTouch)-inset)/4;
					}
					poly.npoints=0;
					poly.addPoint(Nx+inset+inset, Ny+inset);
					poly.addPoint(Nx+sliceWidth, Ny+inset);
					poly.addPoint(mNx+sliceWidth-inset, mNy);
					poly.addPoint(mNx+inset, mNy);
					poly.addPoint(Nx+inset+inset, Ny+inset);
					poly.npoints=5;
				}
			}
		}
	}
	/*_________________________________________________*/
	private void UPCalcFillBranchPolys(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPCalcFillBranchPolys(tree, d);
		UPdefineFillPoly(node, fillBranchPoly[node], false, tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void UPdefinePoly(int node, Polygon poly, boolean isTouch, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null) {
			Nx -= getOffset(branchEdgeWidth(node,isTouch), node);
			mNx -= getOffset(branchEdgeWidth(node,isTouch), node);
			if (internalNode)  {
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+branchEdgeWidth(node, isTouch)/2, Ny-branchEdgeWidth(node, isTouch)/2);//Ny+branchEdgeWidth(node, isTouch)/2 for down
				poly.addPoint(Nx+branchEdgeWidth(node, isTouch), Ny);
				poly.addPoint(mNx+branchEdgeWidth(node, isTouch), mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else {
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+branchEdgeWidth(node, isTouch), Ny);
				poly.addPoint(mNx+branchEdgeWidth(node, isTouch), mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void UPCalcBranchPolys(Tree tree, int node, Polygon[] polys, boolean isTouch)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPCalcBranchPolys(tree, d, polys, isTouch);
			UPdefinePoly(node, polys[node], isTouch, tree.nodeIsInternal(node), x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		}
		else {
			Polygon poly = polys[node];
			poly.npoints=0;
			int mN = tree.motherOfNode(node);
			int leftN = tree.leftmostTerminalOfNode(node);
			int rightN = tree.rightmostTerminalOfNode(node);
			poly.addPoint(x[node], y[node]);
			poly.addPoint(x[leftN], y[leftN]);
			poly.addPoint(x[rightN]+branchEdgeWidth(node, isTouch), y[rightN]);
			poly.addPoint(x[node]+branchEdgeWidth(node, isTouch), y[node]);
			poly.addPoint(x[mN]+branchEdgeWidth(node, isTouch), y[mN]);
			poly.addPoint(x[mN], y[mN]);
			poly.addPoint(x[node], y[node]);
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void DOWNdefineFillPoly(int node, Polygon poly, boolean isTouch, boolean internalNode, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=branchEdgeWidth(node, isTouch);
		if (numSlices>1) {
			Nx+= (sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices;
			mNx+= (sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices;
			sliceWidth=(branchEdgeWidth(node, isTouch)-inset)-((sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices);
		}
		if ((internalNode) && (numSlices==1)){ 
			poly.npoints=0;
			poly.addPoint(Nx+inset, Ny);
			poly.addPoint(Nx+sliceWidth/2, Ny+sliceWidth/2+inset);
			poly.addPoint(Nx+sliceWidth-inset, Ny);
			poly.addPoint(mNx+sliceWidth-inset, mNy);
			poly.addPoint(mNx+inset, mNy);
			poly.addPoint(Nx+inset, Ny);
			poly.npoints=6;
		}
		else {
			if (Nx==mNx) {
				if ((internalNode) && (numSlices>1)) {
					Ny+=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny-inset);
				poly.addPoint(Nx+sliceWidth-inset, Ny-inset);
				poly.addPoint(mNx+sliceWidth-inset, mNy);
				poly.addPoint(mNx+inset, mNy);
				poly.addPoint(Nx+inset, Ny-inset);
				poly.npoints=5;
			}
			else if (Nx>mNx) {
				if ((internalNode) && (numSlices>1)) {
					Nx+=(branchEdgeWidth(node, isTouch)-inset)/4;
					Ny+=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx, Ny-inset);
				poly.addPoint(Nx+sliceWidth-inset-inset, Ny-inset);
				poly.addPoint(mNx+sliceWidth-inset, mNy);
				poly.addPoint(mNx+inset, mNy);
				poly.addPoint(Nx, Ny-inset);
				poly.npoints=5;
			}
			else if (Nx<mNx) {
				if ((internalNode) && (numSlices>1)) {
					Nx-=(branchEdgeWidth(node, isTouch)-inset)/4;
					Ny+=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset+inset, Ny-inset);
				poly.addPoint(Nx+sliceWidth, Ny-inset);
				poly.addPoint(mNx+sliceWidth-inset, mNy);
				poly.addPoint(mNx+inset, mNy);
				poly.addPoint(Nx+inset+inset, Ny-inset);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void DOWNCalcFillBranchPolys(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcFillBranchPolys(tree, d);
		DOWNdefineFillPoly(node, fillBranchPoly[node], false, tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void DOWNdefinePoly(int node, Polygon poly,boolean isTouch, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null){
			Nx -= getOffset(branchEdgeWidth(node,isTouch), node);
			mNx -= getOffset(branchEdgeWidth(node,isTouch), node);
			if (internalNode) 
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+branchEdgeWidth(node, isTouch)/2, Ny+branchEdgeWidth(node, isTouch)/2);
				poly.addPoint(Nx+branchEdgeWidth(node, isTouch), Ny);
				poly.addPoint(mNx+branchEdgeWidth(node, isTouch), mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+branchEdgeWidth(node, isTouch), Ny);
				poly.addPoint(mNx+branchEdgeWidth(node, isTouch), mNy);
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void DOWNCalcBranchPolys(Tree tree, int node, Polygon[] polys, boolean isTouch)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcBranchPolys(tree, d, polys, isTouch);
			DOWNdefinePoly(node, polys[node], isTouch, tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		}
		else {
			Polygon poly = polys[node];
			poly.npoints=0;
			int mN = tree.motherOfNode(node);
			int leftN = tree.leftmostTerminalOfNode(node);
			int rightN = tree.rightmostTerminalOfNode(node);
			poly.addPoint(x[node], y[node]);
			poly.addPoint(x[leftN], y[leftN]);
			poly.addPoint(x[rightN]+branchEdgeWidth(node, isTouch), y[rightN]);
			poly.addPoint(x[node]+branchEdgeWidth(node, isTouch), y[node]);
			poly.addPoint(x[mN]+branchEdgeWidth(node, isTouch), y[mN]);
			poly.addPoint(x[mN], y[mN]);
			poly.addPoint(x[node], y[node]);
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void RIGHTdefineFillPoly(int node, Polygon poly, boolean isTouch, boolean internalNode, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=branchEdgeWidth(node, isTouch);
		if (numSlices>1) {
			Ny+= (sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices;
			mNy+= (sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices;
			sliceWidth=(branchEdgeWidth(node, isTouch)-inset)-((sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices);
		}
		if ((internalNode) && (numSlices==1)){ 
			poly.npoints=0;
			poly.addPoint(Nx, Ny+inset);
			poly.addPoint(Nx+sliceWidth/2+inset, Ny+sliceWidth/2);
			poly.addPoint(Nx, Ny+sliceWidth-inset);
			poly.addPoint(mNx, mNy+sliceWidth-inset);
			poly.addPoint(mNx, mNy+inset);
			poly.addPoint(Nx, Ny+inset);
			poly.npoints=6;
		}
		else {
			if (Ny==mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx+=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx-inset, Ny+inset);
				poly.addPoint(Nx-inset, Ny+sliceWidth-inset);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx-inset, Ny+inset);
				poly.npoints=5;
			}
			else if (Ny>mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx+=(branchEdgeWidth(node, isTouch)-inset)/4;
					Ny+=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx-inset, Ny);
				poly.addPoint(Nx-inset, Ny+sliceWidth-inset-inset);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx-inset, Ny);
				poly.npoints=5;
			}
			else if (Ny<mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx+=(branchEdgeWidth(node, isTouch)-inset)/4;
					Ny-=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx-inset, Ny+inset+inset);
				poly.addPoint(Nx-inset, Ny+sliceWidth);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx-inset, Ny+inset+inset);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void RIGHTCalcFillBranchPolys(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcFillBranchPolys(tree, d);
		RIGHTdefineFillPoly(node, fillBranchPoly[node], false, tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void RIGHTdefinePoly(int node, Polygon poly, boolean isTouch, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null) {
			Ny -= getOffset(branchEdgeWidth(node,isTouch), node);
			mNy -= getOffset(branchEdgeWidth(node,isTouch), node);
			if (internalNode) 
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx+branchEdgeWidth(node, isTouch)/2, Ny+branchEdgeWidth(node, isTouch)/2);
				poly.addPoint(Nx, Ny+branchEdgeWidth(node, isTouch));
				poly.addPoint(mNx, mNy+branchEdgeWidth(node, isTouch));
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx, Ny+branchEdgeWidth(node, isTouch));
				poly.addPoint(mNx, mNy+branchEdgeWidth(node, isTouch));
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void RIGHTCalcBranchPolys(Tree tree, int node, Polygon[] polys, boolean isTouch)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcBranchPolys(tree, d, polys, isTouch);
			RIGHTdefinePoly(node, polys[node], isTouch, tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		}
		else {
			Polygon poly = polys[node];
			poly.npoints=0;
			int mN = tree.motherOfNode(node);
			int leftN = tree.leftmostTerminalOfNode(node);
			int rightN = tree.rightmostTerminalOfNode(node);
			poly.addPoint(x[node], y[node]);
			poly.addPoint(x[leftN], y[leftN]);
			poly.addPoint(x[rightN], y[rightN]+branchEdgeWidth(node, isTouch));
			poly.addPoint(x[node], y[node]+branchEdgeWidth(node, isTouch));
			poly.addPoint(x[mN], y[mN]+branchEdgeWidth(node, isTouch));
			poly.addPoint(x[mN], y[mN]);
			poly.addPoint(x[node], y[node]);
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	/*_________________________________________________*/
	private void LEFTdefineFillPoly(int node, Polygon poly, boolean isTouch, boolean internalNode, int Nx, int Ny, int mNx, int mNy, int sliceNumber, int numSlices) {
		int sliceWidth=branchEdgeWidth(node, isTouch);
		if (numSlices>1) {
			Ny+= (sliceNumber-1)*((branchEdgeWidth(node, isTouch)-inset)-inset-inset)/numSlices;
			mNy+= (sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices;
			sliceWidth=(branchEdgeWidth(node, isTouch)-inset)-((sliceNumber-1)*(branchEdgeWidth(node, isTouch)-inset)/numSlices);
		}
		if ((internalNode) && (numSlices==1)){ 
			poly.npoints=0;
			poly.addPoint(Nx, Ny+inset);
			poly.addPoint(Nx-sliceWidth/2-inset, Ny+sliceWidth/2);
			poly.addPoint(Nx, Ny+sliceWidth-inset);
			poly.addPoint(mNx, mNy+sliceWidth-inset);
			poly.addPoint(mNx, mNy+inset);
			poly.addPoint(Nx, Ny+inset);
			poly.npoints=6;
		}
		else {
			if (Ny==mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx-=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny+inset);
				poly.addPoint(Nx+inset, Ny+sliceWidth-inset);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx+inset, Ny+inset);
				poly.npoints=5;
			}
			else if (Ny>mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx-=(branchEdgeWidth(node, isTouch)-inset)/4;
					Ny+=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny);
				poly.addPoint(Nx+inset, Ny+sliceWidth-inset-inset);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx+inset, Ny);
				poly.npoints=5;
			}
			else if (Ny<mNy) {
				if ((internalNode) && (numSlices>1)) {
					Nx-=(branchEdgeWidth(node, isTouch)-inset)/4;
					Ny-=(branchEdgeWidth(node, isTouch)-inset)/4;
				}
				poly.npoints=0;
				poly.addPoint(Nx+inset, Ny+inset+inset);
				poly.addPoint(Nx+inset, Ny+sliceWidth);
				poly.addPoint(mNx, mNy+sliceWidth-inset);
				poly.addPoint(mNx, mNy+inset);
				poly.addPoint(Nx+inset, Ny+inset+inset);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void LEFTCalcFillBranchPolys(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node))
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcFillBranchPolys(tree, d);
		LEFTdefineFillPoly(node, fillBranchPoly[node], false, tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0);
	}
	/*_________________________________________________*/
	private void LEFTdefinePoly(int node, Polygon poly, boolean isTouch, boolean internalNode, int Nx, int Ny, int mNx, int mNy) {
		if (poly!=null) {
			Ny -= getOffset(branchEdgeWidth(node,isTouch), node);
			mNy -= getOffset(branchEdgeWidth(node,isTouch), node);
			if (internalNode) 
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx-branchEdgeWidth(node, isTouch)/2, Ny+branchEdgeWidth(node, isTouch)/2);
				poly.addPoint(Nx, Ny+branchEdgeWidth(node, isTouch));
				poly.addPoint(mNx, mNy+branchEdgeWidth(node, isTouch));
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=6;
			}
			else
			{
				poly.npoints=0;
				poly.addPoint(Nx, Ny);
				poly.addPoint(Nx, Ny+branchEdgeWidth(node, isTouch));
				poly.addPoint(mNx, mNy+branchEdgeWidth(node, isTouch));
				poly.addPoint(mNx, mNy);
				poly.addPoint(Nx, Ny);
				poly.npoints=5;
			}
		}
	}
	/*_________________________________________________*/
	private void LEFTCalcBranchPolys(Tree tree, int node, Polygon[] polys, boolean isTouch)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcBranchPolys(tree, d, polys, isTouch);
			LEFTdefinePoly(node, polys[node], isTouch, tree.nodeIsInternal(node),x[node],y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]);
		}
		else {
			Polygon poly = polys[node];
			poly.npoints=0;
			int mN = tree.motherOfNode(node);
			int leftN = tree.leftmostTerminalOfNode(node);
			int rightN = tree.rightmostTerminalOfNode(node);
			poly.addPoint(x[node], y[node]);
			poly.addPoint(x[leftN], y[leftN]);
			poly.addPoint(x[rightN], y[rightN]+branchEdgeWidth(node, isTouch));
			poly.addPoint(x[node], y[node]+branchEdgeWidth(node, isTouch));
			poly.addPoint(x[mN], y[mN]+branchEdgeWidth(node, isTouch));
			poly.addPoint(x[mN], y[mN]);
			poly.addPoint(x[node], y[node]);
			poly.npoints=7;
		}
	}
	/*_________________________________________________*/
	private void calculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]=y[tree.motherOfNode(node)];
		lineBaseX[node]=x[tree.motherOfNode(node)];
	}
	/*_________________________________________________*/
	private void calcBranchPolys(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField());  //Graphics g removed as parameter May 02

		calculateLines(tree, drawnRoot);
		edgeWidth = preferredEdgeWidth;
		if (treeDisplay.getTaxonSpacing()<edgeWidth+2) {
			edgeWidth= treeDisplay.getTaxonSpacing()-2;
			if (edgeWidth<2)
				edgeWidth=2;
		}
		treeDisplay.setMinimumTaxonNameDistance(edgeWidth, 5);
		if (isUP()) {
			UPCalcBranchPolys(tree, drawnRoot, branchPoly, false);
			UPCalcBranchPolys(tree, drawnRoot, touchPoly, true);
			UPCalcFillBranchPolys(tree, drawnRoot);
		}

		else if (isDOWN()){
			DOWNCalcBranchPolys(tree, drawnRoot, branchPoly, false);
			DOWNCalcBranchPolys(tree, drawnRoot, touchPoly, true);
			DOWNCalcFillBranchPolys(tree, drawnRoot);
		}
		else  if (isRIGHT()) {
			RIGHTCalcBranchPolys(tree, drawnRoot, branchPoly, false);
			RIGHTCalcBranchPolys(tree, drawnRoot, touchPoly, true);
			RIGHTCalcFillBranchPolys(tree, drawnRoot);
		}
		else  if (isLEFT()){
			LEFTCalcBranchPolys(tree, drawnRoot, branchPoly, false);
			LEFTCalcBranchPolys(tree, drawnRoot, touchPoly, true);
			LEFTCalcFillBranchPolys(tree, drawnRoot);
		}
	}
	/*_________________________________________________*/
	/** Draw highlight for branch node with current color of graphics context */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		tC = g.getColor();
		if (flip)
			g.setColor(Color.red);
		else
			g.setColor(Color.blue);
		if (isDOWN() || isUP()){
			for (int i=0; i<4; i++)
				g.drawLine(x[node]-2 - i, y[node], x[tree.motherOfNode(node)]-2 - i, y[tree.motherOfNode(node)]);
		}
		else {
			for (int i=0; i<4; i++)
				g.drawLine(x[node], y[node]-2 - i, x[tree.motherOfNode(node)], y[tree.motherOfNode(node)]-2 - i);
		}
		g.setColor(tC);
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
	Color tC;
	public boolean branchIsVisible(int node){
		try {
			if (node >=0 && node <  branchPoly.length)
				return treeDisplay.getVisRect() == null || branchPoly[node].intersects(treeDisplay.getVisRect());
		}
		catch(Throwable t){
		}
		return false;
	}
	/*_________________________________________________*/
	private   void drawBranches(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			//g.setColor(Color.black);//for testing
			boolean draw = branchIsVisible(node);
			if (draw){
				g.setColor(treeDisplay.getBranchColor(node));
			if ((tree.getRooted() || tree.getRoot()!=node) && branchPoly[node]!=null) {
				if (SHOWTOUCHPOLYS) {  //fordebugging
					Color prev = g.getColor();
					g.setColor(ColorDistribution.burlyWood);
					g.fillPolygon(touchPoly[node]);
					g.setColor(prev);
				}
				g.fillPolygon(branchPoly[node]);
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
			if (tree.getAssociatedBit(triangleNameRef,node)) {
				if (isUP()) {
					/*g.setColor(Color.red);
					for (int i=0; i<edgeWidth; i++) 
						g.drawLine(x[node]+i,y[node], x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]);

					g.setColor(Color.blue);
					for (int i=0; i<edgeWidth; i++)
						g.drawLine(x[node]+i,y[node], x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]);

						g.setColor(Color.green);
					for (int i=0; i<edgeWidth*0.71; i++) {
						g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+i, x[tree.rightmostTerminalOfNode(node)]-i,y[tree.rightmostTerminalOfNode(node)]+i);
					}*/
				}
				else if (isDOWN()) {
					/*g.setColor(Color.blue);
					for (int i=0; i<edgeWidth; i++) {
						g.drawLine(x[node]+i,y[node], x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]);
						g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]-i, x[tree.rightmostTerminalOfNode(node)]-i,y[tree.rightmostTerminalOfNode(node)]-i);
						g.drawLine(x[node]+i,y[node], x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]);
					}
					 */
				}
				/*	for (int j=0; j<2; j++)
				for (int i=0; i<2; i++) {
					g.drawLine(x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
					g.drawLine(x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
					g.drawLine(x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
				}*/
			}
			}
			if (!tree.getAssociatedBit(triangleNameRef,node))
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					drawBranches( tree, g, d);
			if (draw && emphasizeNodes()) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				g.fillPolygon(nodePoly(node));
				g.setColor(prev);
			}

		}
	}
	/*_________________________________________________*/
	private double findMaxWidth(Tree tree, int node) {
		if (!tree.getAssociatedBit(triangleNameRef,node)) {
			if (tree.nodeIsTerminal(node))
				return widths.getValue(node);

			double mw = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				mw = MesquiteDouble.maximum(mw, findMaxWidth(tree, d));
			return mw;
		}
		return (MesquiteDouble.unassigned);
	}
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
		if (MesquiteTree.OK(tree)) {

			if (!tree.nodeExists(getDrawnRoot()))
				setDrawnRoot(tree.getRoot());
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			widths = tree.getWhichAssociatedDouble(widthNameReference);
			if (widths!=null)
				maxWidth = findMaxWidth(tree, getDrawnRoot());
			calcBranchPolys(tree, getDrawnRoot());

		}
	}
	/*_________________________________________________*/
	public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if (tree==null || xValue==null || yValue==null)
			return;
		if (!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
		xValue.setValue(GraphicsUtil.xCenterOfLine(x[mother], y[mother], x[N], y[N]));
		yValue.setValue(GraphicsUtil.yCenterOfLine(x[mother], y[mother], x[N], y[N]));
		angle.setValue(GraphicsUtil.angleOfLine(x[mother], y[mother], x[N], y[N]));
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
		
		if (MesquiteTree.OK(tree) && treeDisplay != null && g!= null) {
			//if (tree.getNumNodeSpaces()!=numNodes)
			//	resetNumNodes(tree.getNumNodeSpaces());
			g.setColor(treeDisplay.branchColor);
			drawBranches(tree, g, drawnRoot);  
		}
	}

	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle box;
		int ew = branchEdgeWidth(node, false)-2;
		if (isUP()) 
			box = new Rectangle(x[node], y[node]-ew-3, ew, ew);
		else if (isDOWN())
			box = new Rectangle(x[node], y[node]+2, ew, ew);
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
		if (numColors == 0) numColors = 1;
		int ew = branchEdgeWidth(node, false)-2;
		if (isUP()) 
			box = new Rectangle(x[node], y[node]-ew-3, ew, ew);
		else if (isDOWN())
			box = new Rectangle(x[node], y[node]+2, ew, ew);
		else  if (isRIGHT()) 
			box = new Rectangle(x[node]+1, y[node], ew, ew);
		else  if (isLEFT())
			box = new Rectangle(x[node]-ew-3, y[node], ew, ew);
		else 
			box = new Rectangle(x[node], y[node], ew, ew);
		for (int i=0; i<colors.getNumColors(); i++) {
			Color color;
			if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
				g.setColor(color);
			g.fillRect(box.x + (i*box.width/colors.getNumColors()), box.y, box.width-  (i*box.width/numColors), box.height);
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
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			int numColors = colors.getNumColors();
			if (isUP()) {
				for (int i=0; i<numColors; i++) {
					UPdefineFillPoly(node, utilityPolygon, false, tree.nodeIsInternal(node), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, colors.getNumColors());
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (isDOWN()) {
				for (int i=0; i<numColors; i++) {
					DOWNdefineFillPoly(node, utilityPolygon, false, tree.nodeIsInternal(node), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, colors.getNumColors());
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (isRIGHT()) {
				for (int i=0; i<numColors; i++) {
					RIGHTdefineFillPoly(node, utilityPolygon, false, tree.nodeIsInternal(node), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, colors.getNumColors());
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			else if (isLEFT()){
				for (int i=0; i<numColors; i++) {
					LEFTdefineFillPoly(node, utilityPolygon, false, tree.nodeIsInternal(node), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, colors.getNumColors());
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					g.fillPolygon(utilityPolygon);
				}
			}
			g.setColor(treeDisplay.getBranchColor(node));
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (fillBranchPoly[node] !=null && node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			g.fillPolygon(fillBranchPoly[node]);
		}
	}

	/*_________________________________________________*/
	public Polygon nodePoly(int node) {
		int offset = (getNodeWidth()-getEdgeWidth())/2;
		int halfNodeWidth = getNodeWidth()/2;
		int startX =0;
		int startY =0;
		if (isUP() || isDOWN()){
			startX = x[node]+halfNodeWidth-offset;
			startY= y[node] -halfNodeWidth;
		}	else if (isRIGHT() || isLEFT()){
			startX = x[node];
			startY= y[node]-offset;
		}
		Polygon poly = new Polygon();
		poly.npoints=0;
		poly.addPoint(startX,startY);
		poly.addPoint(startX+halfNodeWidth,startY+halfNodeWidth);
		poly.addPoint(startX,startY+getNodeWidth());
		poly.addPoint(startX-halfNodeWidth,startY+halfNodeWidth);
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
	private void ScanBranches(Tree tree,Polygon polys[], int node, int x, int y, MesquiteDouble fraction){
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
							fraction.setValue(GraphicsUtil.fractionAlongLine(x, y, this.x[motherNode], this.y[motherNode], this.x[node], this.y[node],isRIGHT()||isLEFT(), isUP()||isDOWN()));
						}
					}
			}
			if (!tree.getAssociatedBit(triangleNameRef, node)) 
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					ScanBranches(tree, polys, d, x, y, fraction);

		}
	}
	/*_________________________________________________*/
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) { 
		if (MesquiteTree.OK(tree) && ready) {
			foundBranch=0;
			ScanBranches(tree, branchPoly, drawnRoot, x, y, fraction);  //first scan through thin branches
			if (foundBranch==0 && getEdgeWidth()<ACCEPTABLETOUCHWIDTH)
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
		preferredEdgeWidth = edw;
		edgeWidth = edw;
		treeDisplay.setMinimumTaxonNameDistance(edgeWidth, 5);
	}
	/*New code Feb.22.07 allows eavesdropping on edgewidth by the TreeDrawing oliver*/ //TODO: delete new code comments
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgeWidth;
	}
	/*End new code Feb.22.07 oliver*/
	/*_________________________________________________*/
	public void dispose(){
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = null;
			fillBranchPoly[i] = null;
		}
		ownerModule=null;
		super.dispose(); //calls cleanup
	}

}



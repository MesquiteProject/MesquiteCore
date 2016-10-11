/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.SquareTree;

import java.util.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.MargLikeAncStForModel;
import mesquite.stochchar.lib.MargLikelihoodForModel;
import mesquite.trees.lib.*;

/* ======================================================================== */
public class SquareTree extends DrawTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsVH.class, getName() + "  needs the locations of nodes to be calculated.",
				"The calculator for node locations is chosen automatically or initially");
	}
	NodeLocsVH nodeLocsTask;
	MesquiteCommand edgeWidthCommand;
	MesquiteString orientationName;
	Vector drawings;
	int oldEdgeWidth = 6;
	int ornt;
	double shortcut = 0.0; //used of for eurogram 
	double shortcutDegree = 0.4;
	MesquiteString nodeLocsName;
	StringArray cornerModes;
	MesquiteString cornerModeName;
	int cornerMode = 0;
	int curvature = 50;
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
		cornerModes = new StringArray(3);  
		MesquiteSubmenuSpec cornersSubmenu = addSubmenu(null, "Corners", makeCommand("setCornerMode", this), cornerModes);
		cornerModes.setValue(0, "Right Angle");  //the strings passed will be the menu item labels
		cornerModes.setValue(1, "Diagonal");
		cornerModes.setValue(2, "Curved");
		cornerModeName = new MesquiteString(cornerModes.getValue(cornerMode));  //this helps the menu keep track of checkmenuitems
		cornersSubmenu.setSelected(cornerModeName);


		MesquiteSubmenuSpec orientationSubmenu = addSubmenu(null, "Orientation");
		ornt = NodeLocsVH.defaultOrientation;  //should take out of preferences
		orientationName = new MesquiteString(orient(ornt));
		orientationSubmenu.setSelected(orientationName);
		addItemToSubmenu(null, orientationSubmenu, "Up", makeCommand("orientUp",  this));
		addItemToSubmenu(null, orientationSubmenu, "Right", makeCommand("orientRight",  this));
		addItemToSubmenu(null, orientationSubmenu, "Down", makeCommand("orientDown",  this));
		addItemToSubmenu(null, orientationSubmenu, "Left", makeCommand("orientLeft",  this));
		addMenuItem( "Line Width...", makeCommand("setEdgeWidth",  this));
		//	addCheckMenuItem(null, "Simple Triangle for Triangled Clades", makeCommand("toggleSimpleTriangle",  this), simpleTriangle);
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		SquareTreeDrawing treeDrawing =  new SquareTreeDrawing (treeDisplay, numTaxa, this);
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
		//	temp.addLine("toggleCorners " + cutCorners.toOffOnString());
		temp.addLine("setCornerMode " + ParseUtil.tokenize(cornerModes.getValue(cornerMode)) + "  " + curvature);
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
		else	if (checker.compare(this.getClass(), "Sets the thickness of drawn branches", "[width in pixels]", commandName, "setEdgeWidth")) {
			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set edge width", "Edge Width:", oldEdgeWidth, 1, 99);
			if (newWidth>0 && newWidth<100 && newWidth!=oldEdgeWidth) {
				oldEdgeWidth=newWidth;
				if (drawings == null)
					return null;
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
					treeDrawing.setEdgeWidth(newWidth);
					if (treeDrawing.treeDisplay != null)
						treeDrawing.treeDisplay.setMinimumTaxonNameDistance(treeDrawing.edgewidth, 5); //better if only did this if tracing on
				}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Sets whether or not corners are cut", "[on = cut; off]", commandName, "toggleCorners")) {  //defunct; for combatibility with old files
			MesquiteBoolean cutCorners = new MesquiteBoolean(false);
			cutCorners.toggleValue(parser.getFirstToken(arguments));
			if (cutCorners.getValue()){
				cornerMode = 1;
				cornerModeName.setValue(cornerModes.getValue(cornerMode)); //so that menu item knows to become checked
			}
			Enumeration e = drawings.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
				treeDrawing.cornerMode = this.cornerMode;
			}
			if (!MesquiteThread.isScripting()) parametersChanged();
		}
		else if (checker.compare(getClass(), "Sets the corner mode", null, commandName, "setCornerMode")) {
			String name = parser.getFirstToken(arguments); //get argument passed of option chosen
			int newMode = cornerModes.indexOf(name); //see if the option is recognized by its name
			if (newMode >=0 && newMode!=cornerMode){
				cornerMode = newMode; //change mode
				cornerModeName.setValue(cornerModes.getValue(cornerMode)); //so that menu item knows to become checked
				if (cornerMode == 2){
					String curveS = parser.getNextToken();
					int curveD = MesquiteInteger.fromString(curveS);

					if (MesquiteInteger.isCombinable(curveD))
						curvature = curveD;
					else if (!MesquiteThread.isScripting()){
						curveD = MesquiteInteger.queryInteger(containerOfModule(), "Corner Curve", "Curvature (10 to 1000; suggested 50)", curvature, 10, 1000);
						if (MesquiteInteger.isCombinable(curveD))
							curvature = curveD;
					}
				}
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
					treeDrawing.cornerMode = this.cornerMode;
				}
				if (!MesquiteThread.isScripting()) parametersChanged();
			}
			else 	if (newMode == 2){
				String curveS = parser.getNextToken();
				int curveD = MesquiteInteger.fromString(curveS);

				if (MesquiteInteger.isCombinable(curveD))
					curvature = curveD;
				else if (!MesquiteThread.isScripting()){
					curveD = MesquiteInteger.queryInteger(containerOfModule(), "Corner Curve", "Curvature (10 to 1000; suggested 50)", curvature, 10, 1000);
					if (MesquiteInteger.isCombinable(curveD))
						curvature = curveD;
					if (!MesquiteThread.isScripting()) parametersChanged();
				}
			}

		}
		else if (checker.compare(this.getClass(), "Returns the module calculating node locations", null, commandName, "getNodeLocsEmployee")) {
			return nodeLocsTask;
		}
		else if (checker.compare(this.getClass(), "Orients the tree drawing so that the terminal taxa are on top", null, commandName, "orientUp")) {
			Enumeration e = drawings.elements();
			ornt = TreeDisplay.UP;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
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
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
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
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
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
				SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
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
					SquareTreeDrawing treeDrawing = (SquareTreeDrawing)obj;
					treeDrawing.setSimpleTriangle(simpleTriangle.getValue());
				}
				parametersChanged();
			}
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Classic Square tree";
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
		return "Draws trees with standard square branches (\"phenogram\")" ;
	}
	/*.................................................................................................................*/

}


/* ======================================================================== */
class SquareTreeDrawing extends TreeDrawing   {
	public Path2D.Double[] branchPoly;
	public Path2D.Double[] touchPoly;
	public Path2D.Double[] fillBranchPoly;
	private Path2D.Double utilityPolygon;

	public SquareTree ownerModule;
	public int edgewidth = 6;
	public int preferredEdgeWidth = 6;
	int oldNumTaxa = 0;
	private int foundBranch;
	private boolean ready=false;
	private static final int  inset=1;
	NameReference triangleNameRef;
	int cornerMode;
	BasicStroke defaultStroke;


	public SquareTreeDrawing(TreeDisplay treeDisplay, int numTaxa, SquareTree ownerModule) {
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

		cornerMode = ownerModule.cornerMode;

		oldNumTaxa = numTaxa;
		ready = true;
		treeDisplay.setOrientation(ownerModule.nodeLocsTask.getDefaultOrientation());
		utilityPolygon=new Path2D.Double();

	}

	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new Path2D.Double[numNodes];
		touchPoly= new Path2D.Double[numNodes];
		fillBranchPoly= new Path2D.Double[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new Path2D.Double();
			touchPoly[i] = new Path2D.Double();
			fillBranchPoly[i] = new Path2D.Double();
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
	/*_________________________________________________*/
	private double length(int node, int mother){
		if (isUP())
			return y[mother]-y[node];
		else if (isDOWN())
			return y[node]-y[mother];
		else if (isLEFT())
			return x[mother]-x[node];
		else if (isRIGHT())
			return x[node]-x[mother];
		return 0;
	}
	private double getShortcutOfDaughters(Tree tree, int node){
		if (cornerMode != 1)
			return 0;
		double s = MesquiteDouble.unassigned;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			s = MesquiteDouble.minimum(s, length(d, node));  //find shortest descendant
		return (ownerModule.shortcutDegree*s);
	}
	private int getOffset(int width) {
		return (width-getEdgeWidth())/2;
	}
	/*_________________________________________________*/
	//makes polygon clockwise
	private void UPdefineFillPoly(Path2D.Double poly, boolean isRoot, double Nx, double Ny, double mNx, double mNy, int sliceNumber, int numSlices, double nShortcut, boolean complete) {
		int sliceWidth=edgewidth;
		if (numSlices>1) {
			Nx+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			sliceWidth=(edgewidth-inset)-( (sliceNumber-1)*(edgewidth-inset)/numSlices);
		}
		//rightedge needs to join to right edge of mother, not left edge of mother
		poly.reset();
		if (isRoot) {
			poly.moveTo(Nx+inset, Ny+inset); // root left
			poly.lineTo(Nx+sliceWidth-inset, Ny+inset);	//root right 
			poly.lineTo(Nx+sliceWidth-inset, mNy); //subroot right
			poly.lineTo(Nx+inset, mNy); //subroot left
			poly.lineTo(Nx+inset, Ny+inset); //return to root left
		}
		else if (Nx<mNx) //left leaning (*)
		{
			if (numSlices>1)
				mNy+=inset;
			//if (numSlices>1)
			//	mNy-= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.moveTo(Nx+inset, Ny+inset); // daugher left
			poly.lineTo(Nx+sliceWidth-inset, Ny+inset);	//daughter right 
			poly.lineTo(Nx+sliceWidth-inset, mNy+inset - nShortcut); //corner right
			if (complete){
				poly.lineTo(mNx+sliceWidth-inset, mNy+inset); //mother up (right edge)
				poly.lineTo(mNx, mNy+sliceWidth-inset); //mother down (left edge)
				poly.lineTo(Nx+inset, mNy+sliceWidth-inset -  nShortcut); //corner left
			}
			else
				poly.lineTo(Nx+inset, mNy+inset -  nShortcut); //corner left
			poly.lineTo(Nx+inset, Ny+inset); //return to daughter left 
			//	poly.npoints=7;
		}
		else { //right leaning(*)
			if (numSlices>1)
				mNy+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.moveTo(Nx+inset, Ny+inset); // daugher left
			poly.lineTo(Nx+sliceWidth-inset, Ny+inset);//daughter right 
			if (complete){
				poly.lineTo(Nx+sliceWidth-inset, mNy+sliceWidth-inset -  nShortcut); //corner right
				poly.lineTo(mNx+sliceWidth-inset, mNy+sliceWidth-inset); //mother down (right edge)
				poly.lineTo(mNx, mNy+inset); //mother up (left edge)
			}
			else {
				poly.lineTo(Nx+sliceWidth-inset, mNy + inset -  nShortcut); //corner right
			}
			poly.lineTo(Nx+inset, mNy+inset -  nShortcut); //corner left
			poly.lineTo(Nx+inset, Ny+inset);  //return to daughter left
			//	poly.npoints=7;
		}
		poly.closePath();

	}
	/*_________________________________________________*/
	private void UPCalcFillBranchPolys(Tree tree, int node, double nShortcut)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
			double dShortcut = getShortcutOfDaughters(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPCalcFillBranchPolys(tree, d, dShortcut);
		}
		UPdefineFillPoly(fillBranchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0, nShortcut, true);
	}

	/*_________________________________________________*/
	private void UPCalcBranchPolys(Tree tree, int node, double nShortcut, Path2D.Double[] polys, int width)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
			double dShortcut = getShortcutOfDaughters(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				UPCalcBranchPolys(tree, d, dShortcut, polys, width);
		}
		DrawTreeUtil.UPdefineSquarePoly(this,polys[node], width, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], nShortcut);
	}
	/*_________________________________________________*/
	//makes polygon counterclockwise
	private void DOWNdefineFillPoly(Path2D.Double poly, boolean isRoot, double Nx, double Ny, double mNx, double mNy, int sliceNumber, int numSlices, double nShortcut, boolean complete) {
		int sliceWidth=edgewidth;
		if (numSlices>1) {
			Nx+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			sliceWidth=(edgewidth-inset)-((sliceNumber-1)*(edgewidth-inset)/numSlices);
		}
		poly.reset();

		if (isRoot) {
			poly.moveTo(Nx+inset, Ny-inset); //root right
			poly.lineTo(Nx+sliceWidth-inset, Ny-inset);	//root left
			poly.lineTo(Nx+sliceWidth-inset, mNy);  //subroot left
			poly.lineTo(Nx+inset, mNy); //subroot right
			poly.lineTo(Nx+inset, Ny-inset); //return to root right
		}
		else if (Nx>mNx) //left leaning
		{
			if (numSlices>1)
				mNy-= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.moveTo(Nx+inset, Ny-inset);// daughter right
			poly.lineTo(Nx+sliceWidth-inset, Ny-inset);//daughter left

			if (complete){
				poly.lineTo(Nx+sliceWidth-inset, mNy-sliceWidth+inset + nShortcut);//corner left
				poly.lineTo(mNx+sliceWidth-inset, mNy-sliceWidth+inset); //mother down * on x
				poly.lineTo(mNx, mNy-inset);//mother up 
			}
			else
				poly.lineTo(Nx+sliceWidth-inset, mNy+inset + nShortcut);//corner left

			poly.lineTo(Nx+inset, mNy-inset + nShortcut);//corner right

			poly.lineTo(Nx+inset, Ny-inset); //return to daughter right
		}
		else {
			if (numSlices>1)
				mNy-=inset;
			poly.moveTo(Nx+inset, Ny-inset); // daughter right
			poly.lineTo(Nx+sliceWidth-inset, Ny-inset);	//daughter left
			poly.lineTo(Nx+sliceWidth-inset, mNy-inset + nShortcut);//corner left
			if (complete){
				poly.lineTo(mNx+sliceWidth-inset, mNy-inset); //mother up * on x
				poly.lineTo(mNx, mNy-sliceWidth+inset); //mother down 
				poly.lineTo(Nx+inset, mNy-sliceWidth+inset + nShortcut); //corner right
			}
			else
				poly.lineTo(Nx+inset, mNy+inset + nShortcut); //corner right

			poly.lineTo(Nx+inset, Ny-inset); //return to daugher right
		}
	}
	/*_________________________________________________*/
	private void DOWNCalcFillBranchPolys(Tree tree, int node, double nShortcut)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
			double dShortcut = getShortcutOfDaughters(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcFillBranchPolys(tree, d, dShortcut);
		}
		DOWNdefineFillPoly(fillBranchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0, nShortcut, true);
	}
	/*_________________________________________________*/
	private void DOWNCalcBranchPolys(Tree tree, int node, double nShortcut, Path2D.Double[] polys, int width)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
			double dShortcut = getShortcutOfDaughters(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				DOWNCalcBranchPolys(tree, d, dShortcut, polys, width);
		}
		DrawTreeUtil.DOWNdefineSquarePoly(this,polys[node], width, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)],  nShortcut);
	}
	/*_________________________________________________*/
	//makes polygon clockwise
	private void RIGHTdefineFillPoly(Path2D.Double poly, boolean isRoot, double Nx, double Ny, double mNx, double mNy, int sliceNumber, int numSlices,  double nShortcut, boolean complete) {
		int sliceWidth=edgewidth;
		if (numSlices>1) {
			Ny+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			sliceWidth=(edgewidth-inset)-((sliceNumber-1)*(edgewidth-inset)/numSlices);
		}
		poly.reset();

		if (isRoot) {
			poly.moveTo(Nx-inset, Ny+inset);// root left
			poly.lineTo(Nx-inset, Ny+sliceWidth-inset);	//root right 
			poly.lineTo(mNx, Ny+sliceWidth-inset); //subroot right
			poly.lineTo(mNx, Ny+inset); //subroot left
			poly.lineTo(Nx-inset, Ny+inset); //return to root left
		}
		else if (Ny<mNy) //leans left
		{
			//if (numSlices>1)
			//	mNx+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			if (numSlices>1)
				mNx-=inset;
			poly.moveTo(Nx-inset, Ny+inset); // daughter left
			poly.lineTo(Nx-inset, Ny+sliceWidth-inset);	//daughter right
			poly.lineTo(mNx-inset + nShortcut, Ny+sliceWidth-inset);//corner right
			if (complete){
				poly.lineTo(mNx-inset, mNy +sliceWidth-inset); //mother up * on y
				poly.lineTo(mNx-sliceWidth+inset, mNy); //mother down
				poly.lineTo(mNx-sliceWidth+inset + nShortcut, Ny+inset); //corner left
			}
			else 

				poly.lineTo(mNx+inset + nShortcut, Ny+inset); //corner left
			poly.lineTo(Nx-inset, Ny+inset); //return to daughter left
		}
		else {
			if (numSlices>1)
				mNx-= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.moveTo(Nx-inset, Ny+inset);// daughter left
			poly.lineTo(Nx-inset, Ny+sliceWidth-inset);//daughter right
			if (complete){
				poly.lineTo(mNx-sliceWidth+inset + nShortcut, Ny+sliceWidth-inset);//corner right
				poly.lineTo(mNx-sliceWidth+inset, mNy+sliceWidth-inset);//mother down * on y
				poly.lineTo(mNx-inset, mNy);//mother up
			}
			else
				poly.lineTo(mNx+inset + nShortcut, Ny+sliceWidth-inset);//corner right
			poly.lineTo(mNx-inset + nShortcut, Ny+inset); //corner left

			poly.lineTo(Nx-inset, Ny+inset); //return to daughter left
		}
	}
	/*_________________________________________________*/
	private void RIGHTCalcFillBranchPolys(Tree tree, int node, double nShortcut)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
			double dShortcut = getShortcutOfDaughters(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcFillBranchPolys(tree, d, dShortcut);
		}
		RIGHTdefineFillPoly(fillBranchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0,  nShortcut, true);
	}
	/*_________________________________________________*/
	private void RIGHTCalcBranchPolys(Tree tree, int node, double nShortcut, Path2D.Double[] polys, int width)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()){
			double dShortcut = getShortcutOfDaughters(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				RIGHTCalcBranchPolys(tree, d, dShortcut, polys, width);
		}
		DrawTreeUtil.RIGHTdefineSquarePoly(this,polys[node], width, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)],  nShortcut);
	}
	/*_________________________________________________*/
	/* make polygon counterclockwise*/
	private void LEFTdefineFillPoly(Path2D.Double poly, boolean isRoot, double Nx, double Ny, double mNx, double mNy, int sliceNumber, int numSlices, double nShortcut, boolean complete) {
		int sliceWidth=edgewidth;
		if (numSlices>1) {
			Ny+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			sliceWidth=(edgewidth-inset)-((sliceNumber-1)*(edgewidth-inset)/numSlices);
		}
		poly.reset();

		if (isRoot) {
			poly.moveTo(Nx+inset, Ny+inset); // root right
			poly.lineTo(Nx+inset, Ny+sliceWidth-inset);	//root left
			poly.lineTo(mNx, Ny+sliceWidth-inset);//subroot left
			poly.lineTo(mNx, Ny+inset); //subroot right
			poly.lineTo(Nx+inset, Ny+inset);  //return to root right
		}
		else if (Ny>mNy) //left leaning
		{
			if (numSlices>1)
				mNx+=inset;
			poly.moveTo(Nx+inset, Ny+inset);// daughter right 
			poly.lineTo(Nx+inset, Ny+sliceWidth-inset);	//daughter left
			if (complete){
				poly.lineTo(mNx+sliceWidth -  nShortcut-inset, Ny+sliceWidth-inset);//corner left/
				poly.lineTo(mNx+sliceWidth-inset, mNy +sliceWidth-inset); //mother down
				poly.lineTo(mNx+inset, mNy); //mother up
			}
			else 
				poly.lineTo(mNx -  nShortcut-inset, Ny+sliceWidth-inset);//corner left/
			poly.lineTo(mNx+inset -  nShortcut, Ny+inset); //corner right
			poly.lineTo(Nx+inset, Ny+inset); //return to daughter right
		}
		else {
			if (numSlices>1)
				mNx+= (sliceNumber-1)*(edgewidth-inset)/numSlices;
			poly.moveTo(Nx+inset, Ny+inset);// daughter right
			poly.lineTo(Nx+inset, Ny+sliceWidth-inset);//daughter left
			poly.lineTo(mNx -  nShortcut+inset, Ny+sliceWidth-inset); // corner left
			if (complete){
				poly.lineTo(mNx+inset, mNy+sliceWidth-inset); //mother up
				poly.lineTo(mNx+sliceWidth-inset, mNy);  //mother down 
				poly.lineTo(mNx +sliceWidth-  nShortcut - inset, Ny+inset); //corner right
			}
			else
				poly.lineTo(mNx -  nShortcut - inset, Ny+inset); //corner right

			poly.lineTo(Nx+inset, Ny+inset); //return to daughter right
			poly.closePath();
		}
	}
	/*_________________________________________________*/
	private void LEFTCalcFillBranchPolys(Tree tree, int node, double nShortcut)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()) {
			double dShortcut = getShortcutOfDaughters(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcFillBranchPolys(tree, d, dShortcut);
		}
		LEFTdefineFillPoly(fillBranchPoly[node], (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 0, 0,  nShortcut, true);
	}
	/*_________________________________________________*/
	private void LEFTCalcBranchPolys(Tree tree, int node, double nShortcut, Path2D.Double[] polys, int width)
	{
		if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle()){
			double dShortcut = getShortcutOfDaughters(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				LEFTCalcBranchPolys(tree, d, dShortcut, polys, width);
		}
		DrawTreeUtil.LEFTdefineSquarePoly(this,polys[node], width, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)],  nShortcut);
	}
	/*_________________________________________________*/
	private void UPDOWNcalculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			UPDOWNcalculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]=y[tree.motherOfNode(node)];
		lineBaseX[node]=x[node];
	}
	/*_________________________________________________*/
	private void LEFTRIGHTcalculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			LEFTRIGHTcalculateLines( tree, d);
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]=y[node];
		lineBaseX[node]=x[tree.motherOfNode(node)];
	}
	int oops = 0;
	/*_________________________________________________*/
	private void calcBranches(Tree tree, int drawnRoot) {
		if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
		if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
		if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
		if (tree==null) { ownerModule.logln("tree null"); return;}

		ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, drawnRoot,  treeDisplay.getField()); //Graphics g removed as parameter May 02
		edgewidth = preferredEdgeWidth;
		if (treeDisplay.getTaxonSpacing()<edgewidth+2) {
			edgewidth= treeDisplay.getTaxonSpacing()-2;
			if (edgewidth<2)
				edgewidth=2;
		}
		treeDisplay.setMinimumTaxonNameDistance(edgewidth, 5);
		if (isUP()) {
			UPCalcBranchPolys(tree, drawnRoot, oops, branchPoly, getEdgeWidth());
			UPCalcBranchPolys(tree, drawnRoot, oops, touchPoly, getNodeWidth());
			UPCalcFillBranchPolys(tree, drawnRoot, oops);
			UPDOWNcalculateLines(tree, drawnRoot);
		}
		else if (isDOWN()){
			DOWNCalcBranchPolys(tree, drawnRoot, oops, branchPoly, getEdgeWidth());
			DOWNCalcBranchPolys(tree, drawnRoot, oops, touchPoly, getNodeWidth());
			DOWNCalcFillBranchPolys(tree, drawnRoot, oops);
			UPDOWNcalculateLines(tree, drawnRoot);
		}
		else  if (isRIGHT()) {
			RIGHTCalcBranchPolys(tree, drawnRoot, oops, branchPoly, getEdgeWidth());
			RIGHTCalcBranchPolys(tree, drawnRoot, oops, touchPoly, getNodeWidth());
			RIGHTCalcFillBranchPolys(tree, drawnRoot, oops);
			LEFTRIGHTcalculateLines(tree, drawnRoot);
		}
		else  if (isLEFT()){
			LEFTCalcBranchPolys(tree, drawnRoot, oops, branchPoly, getEdgeWidth());
			LEFTCalcBranchPolys(tree, drawnRoot, oops, touchPoly, getNodeWidth());
			LEFTCalcFillBranchPolys(tree, drawnRoot, oops);
			LEFTRIGHTcalculateLines(tree, drawnRoot);
		}
	}

	/*New version, accounting for width of drawn branches*/
	public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if(tree==null || xValue==null || yValue==null)
			return;
		if(!tree.nodeExists(N))
			return;
		int mother = tree.motherOfNode(N);
		if(isUP()){
			xValue.setValue(x[N] + getEdgeWidth()/2);
			yValue.setValue(y[mother]+(y[N]-y[mother])/2 + getEdgeWidth()/2);
			angle.setValue(-Math.PI/2.0);
		}
		else if (isDOWN()){
			xValue.setValue(x[N] + getEdgeWidth()/2);
			yValue.setValue(y[N]+(y[mother]-y[N])/2 - getEdgeWidth()/2);
			angle.setValue(Math.PI/2.0);
		}
		else  if (isRIGHT()) {
			xValue.setValue(x[mother]+(x[N]-x[mother])/2 - getEdgeWidth()/2);
			yValue.setValue(y[N] + getEdgeWidth()/2);
			angle.setValue(0.0);
		}
		else  if (isLEFT()){
			xValue.setValue(x[N]+(x[mother]-x[N])/2 + getEdgeWidth()/2);
			yValue.setValue(y[N] + getEdgeWidth()/2);
			angle.setValue(Math.PI);
		}
	}
	/*	
	public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if (tree==null || xValue==null || yValue==null)
			return;
		if (!tree.nodeExists(N))
			return;

		int mother = tree.motherOfNode(N);
		if (isUP()) {
			xValue.setValue(x[N]);
			yValue.setValue(y[mother]+(y[N]-y[mother])/2);
			angle.setValue(-Math.PI/2.0);
		}
		else if (isDOWN()){
			xValue.setValue(x[N]);
			yValue.setValue(y[N]+(y[mother]-y[N])/2);
			angle.setValue(Math.PI/2.0);
		}
		else  if (isRIGHT()) {
			xValue.setValue(x[mother]+(x[N]-x[mother])/2);
			yValue.setValue(y[N]);
			angle.setValue(0.0);
		}
		else  if (isLEFT()){
			xValue.setValue(x[N]+(x[mother]-x[N])/2);
			yValue.setValue(y[N]);
			angle.setValue(Math.PI);
		}
	}*/
	/*_________________________________________________*
	private   void drawLeftSideOfClade(Tree tree, Graphics g, int node) {
		if (tree.nodeIsInternal(node)) {
			int firstDaughter = tree.firstDaughterOfNode(node);
			if (tree.nodeExists(firstDaughter)) {
				GraphicsUtil.drawLine(g,x[node],y[node], x[tree.parentOfNode(node, i)],y[tree.parentOfNode(node, i)]);
				GraphicsUtil.drawLine(g,x[node]+1,y[node], x[tree.parentOfNode(node, i)]+1,y[tree.parentOfNode(node, i)]);
				GraphicsUtil.drawLine(g,x[node],y[node]+1, x[tree.parentOfNode(node, i)],y[tree.parentOfNode(node, i)]+1);
				GraphicsUtil.drawLine(g,x[node]+1,y[node]+1, x[tree.parentOfNode(node, i)]+1,y[tree.parentOfNode(node, i)]+1);
			}
		}



	}

	/*_________________________________________________*/
	private   void drawOneBranch(Tree tree, Graphics g, int node) {
		if (tree.nodeExists(node)) {
			//g.setColor(Color.black);//for testing
			boolean draw = branchIsVisible(node);
			if (draw){
				g.setColor(treeDisplay.getBranchColor(node));
				if ((tree.getRooted() || tree.getRoot()!=node) && branchPoly[node] != null){
					if (SHOWTOUCHPOLYS && touchPoly!=null && touchPoly[node]!=null) {  //fordebugging
						Color prev = g.getColor();
						g.setColor(ColorDistribution.burlyWood);
						GraphicsUtil.fill(g,touchPoly[node]);
						g.setColor(prev);
					}
					fillOneBranch(tree, g, branchPoly[node], node, true);
				}
				if (tree.numberOfParentsOfNode(node)>1) {
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
				if (tree.getAssociatedBit(triangleNameRef,node)) 
					if (treeDisplay.getSimpleTriangle()) {
						for (int j=0; j<2; j++)
							for (int i=0; i<2; i++) {
								GraphicsUtil.drawLine(g,x[node]+i,y[node]+j, x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j);
								GraphicsUtil.drawLine(g,x[tree.leftmostTerminalOfNode(node)]+i,y[tree.leftmostTerminalOfNode(node)]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
								GraphicsUtil.drawLine(g,x[node]+i,y[node]+j, x[tree.rightmostTerminalOfNode(node)]+i,y[tree.rightmostTerminalOfNode(node)]+j);
							}
					} else {
						//	drawLeftSideOfClade(tree, g, node);
					}
			}
			if (!tree.getAssociatedBit(triangleNameRef,node) || !treeDisplay.getSimpleTriangle())
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					drawOneBranch(tree, g, d);
			//g.setColor(Color.green);//for testing
			//g.fillPolygon(fillBranchPoly[node]); //for testing
			//g.setColor(Color.black);//for testing
			//	redCrosses(g, tree, node);

			if (draw && emphasizeNodes()) {
				Color prev = g.getColor();
				g.setColor(Color.red);//for testing
				fillOneBranch(tree, g, nodePoly(node), node, true);
				g.setColor(prev);
			}
		}
	}
	//normal 0, edgewidth
	// fill withcolours drawOneCurvedBranch( start, width inset + i*fillWidth/numColors,  (i+1)*fillWidth/numColors -i*fillWidth/numColors) ;
	// fill drawOneCurvedBranch(inset, edgewidth-inset*2) ;
	/*_________________________________________________*/
	private void drawOneCurvedBranch(Tree tree, Graphics g, int node, int start, int width) {
		if (tree.nodeExists(node)) {
			if (width<0)
				width = 0;
			int nM = tree.motherOfNode(node);
			double xN=x[node];
			double xnM = x[nM];
			double yN =y[node];
			double ynM = y[nM];

			if (treeDisplay.getOrientation()==TreeDisplay.UP || treeDisplay.getOrientation()==TreeDisplay.DOWN){
				xN += width/2+start;
				xnM += width/2;

			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT || treeDisplay.getOrientation()==TreeDisplay.LEFT){ 
				yN += width/2+start;
				ynM += width/2;
			}

			GeneralPath arc = null;
			double curveD = ownerModule.curvature;

			if (treeDisplay.getOrientation()==TreeDisplay.UP) {
				arc = new GeneralPath(); // left
				arc.moveTo(xN, yN);
				if (ynM-yN>curveD)
					arc.lineTo(xN, (float)(ynM-curveD));
				arc.curveTo(xN, ynM, xN, ynM, xnM, ynM);
				arc.lineTo(xnM, ynM);
			}

			else if (treeDisplay.getOrientation()==TreeDisplay.DOWN){//����
				arc = new GeneralPath(); // left
				arc.moveTo(xN, yN);
				if (yN - ynM>curveD)
					arc.lineTo(xN, (float)(ynM+curveD));
				arc.curveTo(xN, ynM, xN, ynM, xnM, ynM);
				arc.lineTo(xnM, ynM);
			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.RIGHT) {
				arc = new GeneralPath(); // left
				arc.moveTo(xN, yN);
				if (xN - xnM>curveD)
					arc.lineTo((float)(xnM+curveD), yN);
				arc.curveTo(xnM, yN, xnM, yN, xnM, ynM);
				arc.lineTo(xnM, ynM);
			}
			else  if (treeDisplay.getOrientation()==TreeDisplay.LEFT){ //����
				arc = new GeneralPath(); // left
				arc.moveTo(xN, yN);
				if (xnM - xN>curveD)
					arc.lineTo((float)(xnM-curveD), yN);
				arc.curveTo(xnM, yN, xnM, yN, xnM, ynM);
				arc.lineTo(xnM, ynM);
			}
			if (arc!=null) {
				BasicStroke wideStroke = new BasicStroke(width);
				Graphics2D g2 = (Graphics2D)g;
				g2.setStroke(wideStroke);
				g2.draw(arc);
				g2.setStroke(defaultStroke);
			}


		}
	}

	void fillOneBranch(Tree tree, Graphics g, Path2D.Double poly, int node, boolean total){
		if (cornerMode == 2){
			if (!total)
				drawOneCurvedBranch(tree, g, node, inset, edgewidth-inset*2) ;
			else
				drawOneCurvedBranch(tree, g, node, 0, edgewidth) ;
		}
		else
			GraphicsUtil.fill(g,poly);
		/*for (int i = 0 ; i < poly.npoints-1; i++){
					GraphicsUtil.drawLine(g,poly.xpoints[i], poly.ypoints[i], poly.xpoints[i+1] , poly.ypoints[i+1]);
		}
		 */
	}
	/*_________________________________________________*/
	public   void drawTree(Tree tree, int drawnRoot, Graphics g) {
		if (MesquiteTree.OK(tree)) {
			if (treeDisplay == null)
				return;
			if (tree.getNumNodeSpaces()!=numNodes)
				resetNumNodes(tree.getNumNodeSpaces());
			g.setColor(treeDisplay.branchColor);
			/*if (oldNumTaxa!= tree.getNumTaxa())
	        		adjustNumTaxa(tree.getNumTaxa()); */

			drawOneBranch(tree, g, drawnRoot);  
		}
	}
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
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
				GraphicsUtil.drawLine(g,x[node]-2 - i, y[node], x[node]-2 - i, y[tree.motherOfNode(node)]);
		}
		else {
			for (int i=0; i<4; i++)
				GraphicsUtil.drawLine(g,x[node], y[node]-2 - i, x[tree.motherOfNode(node)], y[node]-2 - i);
		}
		g.setColor(tC);
	}
	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
		Rectangle2D box;
		int ew = edgewidth-1;
		if (isUP()) 
			box = new Rectangle2D.Double(x[node], y[node]-ew-3, ew, ew);
		else if (isDOWN())
			box = new Rectangle2D.Double(x[node], y[node]+1, ew, ew);
		else  if (isRIGHT()) 
			box = new Rectangle2D.Double(x[node]+1, y[node], ew, ew);
		else  if (isLEFT())
			box = new Rectangle2D.Double(x[node]-ew-3, y[node], ew, ew);
		else 
			box = new Rectangle2D.Double(x[node], y[node], ew, ew);
		GraphicsUtil.fillRect(g,box.getX(), box.getY(), box.getWidth(), box.getHeight());
		g.setColor(treeDisplay.getBranchColor(node));
		GraphicsUtil.drawRect(g,box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
		Rectangle2D box;
		int numColors = colors.getNumColors();
		int ew = edgewidth-1;
		if (isUP()) 
			box = new Rectangle2D.Double(x[node], y[node]-ew-3, ew, ew);
		else if (isDOWN())
			box = new Rectangle2D.Double(x[node], y[node]+1, ew, ew);
		else  if (isRIGHT()) 
			box = new Rectangle2D.Double(x[node]+1, y[node], ew, ew);
		else  if (isLEFT())
			box = new Rectangle2D.Double(x[node]-ew-3, y[node], ew, ew);
		else 
			box = new Rectangle2D.Double(x[node], y[node], ew, ew);
		for (int i=0; i<numColors; i++) {
			g.setColor(colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)));
			GraphicsUtil.fillRect(g,box.getX() + (i*box.getWidth()/numColors), box.getY(), box.getWidth()-  (i*box.getWidth()/numColors), box.getHeight());
		}
		g.setColor(treeDisplay.getBranchColor(node));
		GraphicsUtil.drawRect(g,box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}
	/*_________________________________________________*/
	public  boolean isInTerminalBox(Tree tree, int node, int xPos, int yPos){
		int ew = edgewidth-1;
		if (isUP()){ 
			return xPos> x[node] && xPos < x[node]+ew && yPos > y[node]-ew-3 && yPos < y[node]-3;
		}
		else if (isDOWN())
			return xPos> x[node] && xPos < x[node]+ew && yPos > y[node]+1 && yPos < y[node]+ew+1;
		else  if (isRIGHT()) 
			return xPos> x[node]+1 && xPos < x[node]+ew +1 && yPos > y[node] && yPos < y[node] + ew;
		else  if (isLEFT())
			return xPos> x[node]-ew-3 && xPos < x[node]-3 && yPos > y[node] && yPos < y[node] + ew;
		else 
			return xPos> x[node] && xPos < x[node]+ew && yPos > y[node] && yPos < y[node] + ew;
	}
	/*_________________________________________________*
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
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && node<fillBranchPoly.length && branchIsVisible(node)){
			fillOneBranch(tree, g, fillBranchPoly[node], node, false);  
		}
	}

	/** Fill branch N with indicated set of colors as a sequence, e.g. for stochastic character mapping.  This is not abstract because many tree drawers would have difficulty implementing it */
	public void fillBranchWithColorSequence(Tree tree, int node, ColorEventVector colors, Graphics g){
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			Color c = g.getColor();
			int numEvents = colors.size();
			double nShortcut = getShortcutOfDaughters(tree, tree.motherOfNode(node));
			g.setColor(Color.lightGray);
			fillBranch(tree, node, g);
			if (isUP()) {
				double desc = y[node];
				double anc = y[tree.motherOfNode(node)];
				if (cornerMode == 2){
					anc = (int)(anc -ownerModule.curvature);
				}
				else if (cornerMode == 1){
					anc = anc - nShortcut;
					nShortcut = 0;
				}
				for (int i=numEvents-1; i>=0; i--) {
					ColorEvent e = (ColorEvent)colors.elementAt(i);
					double pos;
					if (i == numEvents-1)
						pos = 1.0;
					else {
						ColorEvent ec = (ColorEvent)colors.elementAt(i+1);
						pos = ec.getPosition();
					}
					UPdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], anc- (int)(pos*(anc-desc)), x[tree.motherOfNode(node)], anc, 0, 1, nShortcut, cornerMode == 0);
					//	if (cornerMode == 0 || i != 0){
					g.setColor(e.getColor());
					GraphicsUtil.fill(g,utilityPolygon);
					//	}

				}
			}
			else if (isDOWN()) {
				double desc = y[node];
				double anc = y[tree.motherOfNode(node)];
				if (cornerMode == 2){
					anc = (int)(anc +ownerModule.curvature);
				}
				else if (cornerMode == 1){
					anc = anc + nShortcut;
					nShortcut = 0;
				}
				for (int i=numEvents-1; i>=0; i--) {
					ColorEvent e = (ColorEvent)colors.elementAt(i);
					double pos;
					if (i == numEvents-1)
						pos = 1.0;
					else {
						ColorEvent ec = (ColorEvent)colors.elementAt(i+1);
						pos = ec.getPosition();
					}
					DOWNdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], anc + (int)(pos*(desc-anc)),  x[tree.motherOfNode(node)], anc, 0, 1, nShortcut, cornerMode == 0);
					g.setColor(e.getColor());
					GraphicsUtil.fill(g,utilityPolygon);
				}
			}
			else if (isRIGHT()) {
				double desc = x[node];
				double anc = x[tree.motherOfNode(node)];
				if (cornerMode == 2){
					anc = (int)(anc + ownerModule.curvature);
				}
				else if (cornerMode == 1){
					anc = anc + nShortcut;
					nShortcut = 0;
				}
				for (int i=numEvents-1; i>=0; i--) {
					ColorEvent e = (ColorEvent)colors.elementAt(i);
					double pos;
					if (i == numEvents-1)
						pos = 1.0;
					else {
						ColorEvent ec = (ColorEvent)colors.elementAt(i+1);
						pos = ec.getPosition();
					}
					RIGHTdefineFillPoly(utilityPolygon, (node==tree.getRoot()),  anc- (int)(pos*(anc-desc)), y[node], anc, y[tree.motherOfNode(node)], 0, 1, nShortcut, cornerMode == 0);
					g.setColor(e.getColor());
					GraphicsUtil.fill(g,utilityPolygon);
				}
			}
			else if (isLEFT()){
				double desc = x[node];
				double anc = x[tree.motherOfNode(node)];
				if (cornerMode == 2){
					anc = (int)(anc - ownerModule.curvature);
				}
				else if (cornerMode == 1) {
					anc = anc - nShortcut;
					nShortcut = 0;
				}

				for (int i=numEvents-1; i>=0; i--) {
					ColorEvent e = (ColorEvent)colors.elementAt(i);
					double pos;
					if (i == numEvents-1)
						pos = 1.0;
					else {
						ColorEvent ec = (ColorEvent)colors.elementAt(i+1);
						pos = ec.getPosition();
					}
					LEFTdefineFillPoly(utilityPolygon, (node==tree.getRoot()), anc + (int)(pos*(desc-anc)), y[node], anc, y[tree.motherOfNode(node)], 0, 1, nShortcut, cornerMode == 0);
					g.setColor(e.getColor());
					GraphicsUtil.fill(g,utilityPolygon);
				}
			}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*
	public void fillBranchWithMissingData(Tree tree, int node, Graphics g) {

		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			Color c = g.getColor();
			if (g instanceof Graphics2D){
				Graphics2D g2 = (Graphics2D)g;
				g2.setPaint(GraphicsUtil.missingDataTexture);
			}
			else
				g.setColor(Color.lightGray);
			int nShortcut = getShortcutOfDaughters(tree, tree.motherOfNode(node));
			if (cornerMode == 2){
				int fillWidth = edgewidth-2*inset;
				drawOneCurvedBranch(treeDisplay.getTree(), g, node, inset,  fillWidth) ;
			}
			else if (isUP())
				UPdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 1, 1, nShortcut, true);
			else if (isDOWN()) 
				DOWNdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 1, 1, nShortcut, true);
			else if (isRIGHT()) 
				RIGHTdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 1, 1, nShortcut, true);
			else if (isLEFT())
				LEFTdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], 1, 1, nShortcut, true);

			g.fillPolygon(utilityPolygon);
			if (c!=null) g.setColor(c);
		}	
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node) && !ancestorIsTriangled(tree, node) && branchIsVisible(node)) {
			Color c = g.getColor();
			int numColors = colors.getNumColors();
			double nShortcut = getShortcutOfDaughters(tree, tree.motherOfNode(node));
			if (cornerMode == 2){
				int fillWidth = edgewidth-2*inset;
				for (int i=0; i<numColors; i++) {
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					drawOneCurvedBranch(treeDisplay.getTree(), g, node, inset + i*fillWidth/numColors,  (i+1)*fillWidth/numColors -i*fillWidth/numColors) ;
				}
			}
			else if (isUP()) {
				for (int i=0; i<numColors; i++) {
					UPdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, numColors, nShortcut, true);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					GraphicsUtil.fill(g,utilityPolygon);
				}
			}
			else if (isDOWN()) {
				for (int i=0; i<numColors; i++) {
					DOWNdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, numColors, nShortcut, true);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					GraphicsUtil.fill(g,utilityPolygon);
				}
			}
			else if (isRIGHT()) {
				for (int i=0; i<numColors; i++) {
					RIGHTdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, numColors, nShortcut, true);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					GraphicsUtil.fill(g,utilityPolygon);
				}
			}
			else if (isLEFT()){
				for (int i=0; i<numColors; i++) {
					LEFTdefineFillPoly(utilityPolygon, (node==tree.getRoot()), x[node], y[node], x[tree.motherOfNode(node)], y[tree.motherOfNode(node)], i+1, numColors, nShortcut, true);
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					GraphicsUtil.fill(g,utilityPolygon);
				}
			}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public Path2D.Double nodePoly(int node) {
		double offset = (getNodeWidth()-getEdgeWidth())/2;
		int doubleOffset = (getNodeWidth()-getEdgeWidth());
		double startX = x[node] - offset;
		double startY= y[node] - offset;
		if (isRIGHT()){
			startX -= getNodeWidth()-doubleOffset;
		} else if (isDOWN())
			startY -= getNodeWidth()-doubleOffset;
		Path2D.Double poly = new Path2D.Double();
		poly.reset();
		poly.moveTo(startX,startY);
		poly.lineTo(startX+getNodeWidth(),startY);
		poly.lineTo(startX+getNodeWidth(),startY+getNodeWidth());
		poly.lineTo(startX,startY+getNodeWidth());
		poly.lineTo(startX,startY);
		return poly;
	}
	/*_________________________________________________*/
	public boolean inNode(int node, int x, int y){
		Path2D.Double nodeP = nodePoly(node);
		if (nodeP!=null && nodeP.contains(x,y))
			return true;
		else
			return false;
	}
	/*_________________________________________________*/
	private void ScanBranches(Tree tree, Path2D[] polys, int node, int x, int y, MesquiteDouble fraction)
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
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) { 
		if (MesquiteTree.OK(tree) && ready) {
			foundBranch=0;
			ScanBranches(tree, branchPoly, drawnRoot, x, y, fraction);
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
	public void setEdgeWidth(int edw) {
		edgewidth = edw;
		preferredEdgeWidth = edw;
	}
	/*New code Feb.22.07 allows eavesdropping on edgewidth by the TreeDrawing oliver*/ //TODO: delete new code comments
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgewidth;
	}
	/*End new code Feb.22.07 oliver*/
	/*_________________________________________________*/
	public   void dispose() { 
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = null;
			fillBranchPoly[i] = null;
			touchPoly[i] = null;
		}
		super.dispose();
	}
}



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ornamental.Constellation;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.trees.BallsNSticks.BallsNSticks;

/* ======================================================================== */
public class Constellation extends DrawTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NodeLocsCircle.class, getName() + "  needs a method to calculate node locations.",
		"The method to calculate node locations is arranged initially");
	}

	NodeLocs nodeLocsTask;
	Vector drawings;
	int oldSpotSize = 24;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		nodeLocsTask= (NodeLocsFree)hireEmployee(NodeLocsFree.class, "Calculator of node locations");
		if (nodeLocsTask == null)
			return sorry(getName() + " couldn't start because no node locator module obtained");
		drawings = new Vector();
 		addMenuItem( "Spot Size...", makeCommand("setSpotDiameter",  this));
 		return true;
 	 }
  	 
 	 public void employeeQuit(MesquiteModule m){
 	 	iQuit();
 	 }
	public   TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa) {
		ConstellationDrawing treeDrawing=  new ConstellationDrawing (treeDisplay, numTaxa, this);
		drawings.addElement(treeDrawing);
		return treeDrawing;
	}
	/** Returns true if other modules can control the orientation */
	public boolean allowsReorientation(){
		return false;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setSpotDiameter " + oldSpotSize); 
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the diameter of the spots", "[diameter]", commandName, "setSpotDiameter")) {
			int newDiameter= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newDiameter))
				newDiameter = MesquiteInteger.queryInteger(containerOfModule(), "Set spot diameter", "Spot Diameter:", oldSpotSize, 6, 100);
    	 		if (newDiameter>6 && newDiameter<100 && newDiameter!=oldSpotSize) {
    	 			oldSpotSize = newDiameter;
				Enumeration e = drawings.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					ConstellationDrawing treeDrawing = (ConstellationDrawing)obj;
    	 				treeDrawing.spotsize=newDiameter;
    	 				treeDrawing.treeDisplay.setMinimumTaxonNameDistance(treeDrawing.spotsize/2, 4);
    	 			}
	 				parametersChanged();
    	 		}
    	 		
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the module assigning node locations", null, commandName, "getNodeLocsEmployee")) {
    	 		return nodeLocsTask;
    	 	}
    	 	else {
 			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Constellation";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Draws trees with spots at the nodes in a more or less unrooted way." ;
   	 }
	/*.................................................................................................................*/
}

/* ======================================================================== */
class ConstellationDrawing extends TreeDrawing  {

	public Constellation ownerModule;
	public int edgewidth = 4;
	public int spotsize = 24;
	int oldNumTaxa = 0;
 	public static final int inset=1;
	private boolean ready=false;
	public RotatedRectangle[] branchPoly;

	private int foundBranch;
	
	public ConstellationDrawing (TreeDisplay treeDisplay, int numTaxa, Constellation ownerModule) {
		super(treeDisplay, MesquiteTree.standardNumNodeSpaces(numTaxa));
	    	treeDisplay.setMinimumTaxonNameDistance(spotsize/2, 4);
		treeDisplay.setOrientation(TreeDisplay.FREEFORM);
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		oldNumTaxa = numTaxa;
		ready = true;
	}
	public void resetNumNodes(int numNodes){
		super.resetNumNodes(numNodes);
		branchPoly= new RotatedRectangle[numNodes];
		for (int i=0; i<numNodes; i++) {
			branchPoly[i] = new RotatedRectangle();
		}
	}
	/*_________________________________________________*/
	private void calculateLines(Tree tree, int node) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			calculateLines( tree, d);
		int m =tree.motherOfNode(node);
		if (tree.motherOfNode(node) == tree.getRoot() && !tree.getRooted() && !tree.nodeIsPolytomous( tree.getRoot() )) {
			int sisterNode = 0;
			if (tree.nodeExists(tree.nextSisterOfNode(node)))
				sisterNode = tree.nextSisterOfNode(node);
			else if (tree.nodeExists(tree.previousSisterOfNode(node)))
				sisterNode = tree.previousSisterOfNode(node);
			if (sisterNode !=0) 
				m = sisterNode;
		}
		lineTipY[node]=y[node];
		lineTipX[node]=x[node];
		lineBaseY[node]=y[m];
		lineBaseX[node]=x[m];
		branchPoly[node].setShape(x[node], y[node], x[m], y[m], edgewidth, y[node]<y[m], RotatedRectangle.RECTANGLE);
	}
	/*_________________________________________________*/
	/** Draw highlight for branch node */
	public void drawHighlight(Tree tree, int node, Graphics g, boolean flip){
		Color tC = g.getColor();
		if (flip)
			g.setColor(Color.red);
		else
			g.setColor(Color.blue);
		for (int i=1; i<4; i++)
			GraphicsUtil.drawOval(g, x[node]- spotsize/2 - 2 - i, y[node]- spotsize/2 - 2 - i, spotsize + 3 + i + i, spotsize + 3 + i + i);

		g.setColor(tC);
	}
	/*_________________________________________________*/
	private   void drawOneBranch(Tree tree, Graphics g, int node, int drawnRoot) {
		if (tree.nodeExists(node)) {
			//g.setColor(Color.black);//for testing
			g.setColor(treeDisplay.getBranchColor(node));
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
			if (node == tree.getRoot() || node == drawnRoot) {
			}
			else if (tree.motherOfNode(node) == tree.getRoot() && !tree.getRooted() && !tree.nodeIsPolytomous( tree.getRoot() )) {
				int sisterNode = 0;
				if (tree.nodeExists(tree.nextSisterOfNode(node)))
					sisterNode = tree.nextSisterOfNode(node);
				else if (tree.nodeExists(tree.previousSisterOfNode(node)))
					sisterNode = tree.previousSisterOfNode(node);
				if (sisterNode !=0) {
					branchPoly[node].fill(g, true);
				}
			}
			else {
				branchPoly[node].fill(g, true);
			}
			int thisSister = tree.firstDaughterOfNode(node);
			while (tree.nodeExists(thisSister)) {
				drawOneBranch( tree, g, thisSister, drawnRoot);
				thisSister = tree.nextSisterOfNode(thisSister);
			}
			if (node == tree.getRoot()) {
				if (tree.getRooted() || tree.nodeIsPolytomous( tree.getRoot() )) {
					drawSpot( g, node);
					highlightSpot( g, node);
				}
			}
			else if (node == drawnRoot) {
				drawSpot( g, node);
				Color c = g.getColor();
				g.setColor(Color.gray);
				highlightSpot( g, node);
				if (c!=null) g.setColor(c);
			}
			else {
				drawSpot( g, node);
				
				if (emphasizeNodes()) {
					Color prev = g.getColor();
					g.setColor(Color.red);//for testing
					drawSpot(g,node);
					g.setColor(prev);
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
			calculateLines(tree, drawnRoot);
	       	 	drawOneBranch(tree, g, drawnRoot, drawnRoot);  
	       	 }
	   }
	
	/*_________________________________________________*/
	public   void recalculatePositions(Tree tree) {
	        if (MesquiteTree.OK(tree)) {
			if (ownerModule==null) {MesquiteTrunk.mesquiteTrunk.logln("ownerModule null"); return;}
			if (ownerModule.nodeLocsTask==null) {ownerModule.logln("nodelocs task null"); return;}
			if (treeDisplay==null) {ownerModule.logln("treeDisplay null"); return;}
	        	if (tree.getNumNodeSpaces()!=numNodes)
	        		resetNumNodes(tree.getNumNodeSpaces());
	        	if (!tree.nodeExists(getDrawnRoot()))
	        		setDrawnRoot(tree.getRoot());
			ownerModule.nodeLocsTask.calculateNodeLocs(treeDisplay,  tree, getDrawnRoot(),  treeDisplay.getField()); //Graphics g removed as parameter May 02
		}
	}
	/*_________________________________________________*/
	public void getMiddleOfBranch(Tree tree, int N, MesquiteNumber xValue, MesquiteNumber yValue, MesquiteDouble angle){
		if (tree==null || xValue==null || yValue==null)
			return;
		if (!tree.nodeExists(N))
			return;

		int mother = tree.motherOfNode(N);
		if (tree.nodeExists(mother)) {
			xValue.setValue(GraphicsUtil.xCenterOfLine(x[mother], y[mother], x[N], y[N]));
			yValue.setValue(GraphicsUtil.yCenterOfLine(x[mother], y[mother], x[N], y[N]));
			angle.setValue(GraphicsUtil.angleOfLine(x[mother], y[mother], x[N], y[N]));
		}
	}
	/*_________________________________________________*/
	public boolean inNode(int node, int h, int v){
		if ((h-x[node])*(h-x[node]) + (v-y[node])*(v-y[node]) < spotsize*spotsize/4) //use radius
			return true;
		else
			return false;
		// ask if x, y is in node's spot    g.fillOval( x[node]- spotsize/2, y[node]- spotsize/2, spotsize, spotsize);
	}
	/*_________________________________________________*/
	private void drawSpot(Graphics g, int node){
		GraphicsUtil.fillOval(g, x[node]- spotsize/2, y[node]- spotsize/2, spotsize, spotsize, true);
	}
	/*_________________________________________________*/
	private void highlightSpot(Graphics g, int node){
		for (int diam = spotsize + 12; diam> spotsize + 8; diam --)
			GraphicsUtil.drawOval(g,x[node]- (int)((double)diam/2 + 0.5), y[node]- (int)((double)diam/2 + 0.5), diam, diam);
	}
	/*_________________________________________________*/
	private void fillSpot(Graphics g, int node){
		GraphicsUtil.fillOval(g, x[node]- spotsize/2 + 2, y[node]- spotsize/2 + 2, spotsize - 4, spotsize - 4, true);
	}
	/*_________________________________________________*/
	public  void fillTerminalBox(Tree tree, int node, Graphics g) {
	}
	/*_________________________________________________*/
	public  void fillTerminalBoxWithColors(Tree tree, int node, ColorDistribution colors, Graphics g){
	}
	/*_________________________________________________*/
	public void fillBranchWithColors(Tree tree, int node, ColorDistribution colors, Graphics g) {
		if (node>0 && (tree.getRooted() || tree.getRoot()!=node)) {
			Color c = g.getColor();
			int numColors = colors.getNumColors();
			if (numColors==1){
				g.setColor(colors.getColor(0, !tree.anySelected()|| tree.getSelected(node)));
				fillSpot(g,node);
			}
			else if (numColors>0) {
				int startAngle=270;
				double totalFreq=0;
				for (int i=0; i<numColors; i++) totalFreq += colors.getWeight(i);
				
				int arcAngle = 360/(numColors);
				for (int i=0; i<numColors; i++) {
					Color color;
					if ((color = colors.getColor(i, !tree.anySelected()|| tree.getSelected(node)))!=null)
						g.setColor(color);
					
					arcAngle = (int)((colors.getWeight(i)/totalFreq)*360);
					GraphicsUtil.fillArc(g,  x[node]- spotsize/2 + 2, y[node]- spotsize/2 + 2, spotsize - 4, spotsize - 4, startAngle, arcAngle, true);
					startAngle+=arcAngle;
				}
			}
			if (c!=null) g.setColor(c);
		}
	}
	/*_________________________________________________*/
	public   void fillBranch(Tree tree, int node, Graphics g) {
		if (node>0 && (tree.getRooted() || node != tree.getRoot())) {
			fillSpot(g,node);
		}
	}
	   
	/*_________________________________________________*/
	private void ScanBranches(Tree tree, int node, int x, int y, MesquiteDouble fraction)
	{
		if (foundBranch==0) {
			if (branchPoly[node].contains(x, y) || inNode(node, x, y)) {
				foundBranch = node;
				if (fraction!=null)
					if (inNode(node,x,y))
						fraction.setValue(ATNODE);
					else {
						int motherNode = tree.motherOfNode(node);
						fraction.setValue(EDGESTART);  //TODO: this is just temporary: need to calculate value along branch.
						if (tree.nodeExists(motherNode)) {
							fraction.setValue(GraphicsUtil.fractionAlongLine(x, y, this.x[motherNode], this.y[motherNode], this.x[node], this.y[node],false,false));
						}
					}
				
			}
			
			int thisSister = tree.firstDaughterOfNode(node);
			while (tree.nodeExists(thisSister)) {
				ScanBranches(tree, thisSister, x, y,fraction);
				thisSister = tree.nextSisterOfNode(thisSister);
			}

		}
	}

	/*_________________________________________________*/
	public   int findBranch(Tree tree, int drawnRoot, int x, int y, MesquiteDouble fraction) { 
	        if (MesquiteTree.OK(tree) && ready) {
	        	foundBranch=0;
	       		 ScanBranches(tree, drawnRoot, x, y,fraction);
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
	}
/*New code Feb.22.07 allows eavesdropping on edgewidth by the TreeDrawing oliver*/ //TODO: delete new code comments
	/*_________________________________________________*/
	public int getEdgeWidth() {
		return edgewidth;
	}
/*End new code Feb.22.07 oliver*/
}
	


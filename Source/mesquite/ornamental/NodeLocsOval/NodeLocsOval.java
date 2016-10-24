/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ornamental.NodeLocsOval;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NodeLocsOval extends NodeLocsFree {
	public TreeDrawing treeDrawing;
	public Tree tree;
	public TreeDisplay treeDisplay;
	MesquiteBoolean showScale;
	MesquiteBoolean showBranchLengths;
	boolean resetShowBranchLengths = false;
	int rootHeight=30;
	Vector extras;
	double angleBetweenTaxa;
	double firsttx;
	double circleSlice;
	int radius;
	int centralRoot;
	Point[] location;
	//DoublePt[] sLoc;
	double lasttx;
	Rectangle treeRectangle;
	int emptyRootSlices;
	int oldNumTaxa=0;
	private MesquiteMenuItemSpec showScaleItem;
	/**angle of each branch from 0 degrees (defined as direction of standard positive "x" axis).*/
	public double [] angle;
	/**length of each branch measured radially from center.*/
	public double [] polarLength;
	/**center of circle about which tree is drawn.*/
	public Point treeCenter;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showBranchLengths = new MesquiteBoolean(false);
		showScale = new MesquiteBoolean(true);
		extras = new Vector();
		addCheckMenuItem(null, "Branches Proportional to Lengths", makeCommand("branchLengthsToggle", this), showBranchLengths);
		if (showBranchLengths.getValue()) {
			showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			resetShowBranchLengths=true;
		}
  		return true;
  	 }
  	 
   
  	/*.................................................................................................................*/
	 
   	public void endJob(){
  	 	if (extras!=null) {
	   		for (int i=0; i<extras.size(); i++){
	   			TreeDisplayExtra extra = (TreeDisplayExtra)extras.elementAt(i);
		   		if (extra!=null){
		   			TreeDisplay td = extra.getTreeDisplay();
		   			extra.turnOff();
		   			if (td!=null)
		   				td.removeExtra(extra);
		   		}
	   		}
	   		extras.removeAllElements();
  	 	}
   		super.endJob();
   	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("branchLengthsToggle " + showBranchLengths.toOffOnString());
  	 	temp.addLine("toggleScale " + showScale.toOffOnString());
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets whether or not the branches are to be shown proportional to their lengths", "[on = proportional; off]", commandName, "branchLengthsToggle")) {
    	 		showBranchLengths.toggleValue(parser.getFirstToken(arguments));
			if (showBranchLengths.getValue()) 
				showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
			else
				deleteMenuItem(showScaleItem);
			resetContainingMenuBar();
			resetShowBranchLengths=true;
			parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether or not to draw the scale for branch lengths", "[on or off]", commandName, "toggleScale")) {
    	 		showScale.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
    	 public String getName() {
		return "Node Locations (oval)";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the node locations for a tree drawn in oval fashion, with root at center." ;
   	 }
  
	/*_________________________________________________*/
   	public boolean compatibleWithOrientation(int orientation) {
   		return orientation==TreeDisplay.CIRCULAR;
   	}
   	public void setDefaultOrientation(TreeDisplay treeDisplay) {
   		treeDisplay.setOrientation(TreeDisplay.CIRCULAR);
   	}
	/*_________________________________________________*/
	double scaling;
	
	private double getBranchLength (int N) {
		if (tree.branchLengthUnassigned(N))
			return 1;
		else
			return tree.getBranchLength(N);
	}
/*{-----------------------------------------------------------------------------}*/
	int numNodes;
	private int findTaxa (int anc, int node){
		if (tree.nodeIsInternal(node)){
				int maxAbove = 0;
				for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d); d = tree.nextSisterOfNodeUR(anc, node, d)) {
					int aboveThis = findTaxa(node, d);
					if (aboveThis>maxAbove)
						maxAbove = aboveThis;
				}
				return maxAbove+1;
		}
		else //  {terminal node; see if Daughter of N}
			return 1;
	}
	private int mostNodesToTip (int anc, int node){  // returns number of nodes from the top of the tree}
		numNodes = 0;
		return findTaxa(anc, node);
	}
//{-----------------------------------------------------------------------------*/
	private void nodePolarToLoc (double polarlength, double angle, Point loc){
		loc.x = treeCenter.x + (int)Math.round(polarlength * Math.sin(angle));
		loc.y = treeCenter.y - (int)Math.round(polarlength * Math.cos(angle));
	}
//{-----------------------------------------------------------------------------}
	private void nodePolarToSingleLoc (double polarlength, double angle, DoublePt loc){
			//{remember: sin = opp/hyp, cos = adj/hyp, tan = opp/adj}
		loc.x = treeCenter.x + polarlength * Math.sin(angle);
		loc.y = treeCenter.y - polarlength * Math.cos(angle);
	}
//{-----------------------------------------------------------------------------}
	private double nodeAngle (int left, int right){
		double theAngle;
		if (angle[left]<= angle[right])
			theAngle = (angle[left] + angle[right]) / 2.0;
		else {
			theAngle = angle[left] + (2 * Math.PI - Math.abs(angle[left] - angle[right])) / 2.0;
			if (theAngle > 2 * Math.PI)
				theAngle = theAngle - 2 * Math.PI;
		}
		return theAngle;
	}
//{-----------------------------------------------------------------------------}
//angle 0 = vertical up
	private void calcterminalPosition (int node){
		double firstangle;
		firstangle = Math.PI + angleBetweenTaxa/2; //TODO: {here need to use stored value}
		angle[node] =firstangle + lasttx; // {angle in radians horizontal from vertical}
		polarLength[node] =radius;
		nodePolarToLoc(polarLength[node], angle[node], location[node]);
		int degrees = (int)(angle[node]/Math.PI/2*360) % 360;
		if (degrees < 45)
			treeDrawing.labelOrientation[node] = 270;
		else if (degrees < 135)
			treeDrawing.labelOrientation[node] = 0;
		else if (degrees < 225)
			treeDrawing.labelOrientation[node] = 90;
		else if (degrees < 315)
			treeDrawing.labelOrientation[node] = 180;
		else 
			treeDrawing.labelOrientation[node] = 270;
		
		//System.out.println(" loctn x " + location[node].x);
		//nodePolarToSingleLoc(polarLength[node], angle[node], sLoc[node]);
		lasttx = (angleBetweenTaxa + lasttx);
		
	}
//{-----------------------------------------------------------------------------}

	private void termTaxaRec (int anc, int node){		//{tree traversal to find locations}
		if (tree.nodeIsTerminal(node)) 
			calcterminalPosition(node);
		else {
			for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d); d = tree.nextSisterOfNodeUR(anc, node, d))
				termTaxaRec(node, d);
		}
	}
	
	private void terminalTaxaLocs (int anc, int node){
		lasttx = 0;
		termTaxaRec(anc, node);
	}
//{-----------------------------------------------------------------------------}
//{-----------------------------------------------------------------------------}
	private void calcNodeLocs (int anc, int node){
		if (tree.nodeIsInternal(node)){
			double min = MesquiteDouble.unassigned;
			for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d); d = tree.nextSisterOfNodeUR(anc, node, d)) {
				calcNodeLocs(node, d);
				min = MesquiteDouble.minimum(min, polarLength[d]);
			}
			int left = tree.firstDaughterOfNodeUR(anc, node);
			int right = tree.lastDaughterOfNodeUR(anc, node);
			angle[node] = nodeAngle(left, right);
			polarLength[node] = min - circleSlice;
			if (polarLength[node] < 0) 
				polarLength[node] = 0;
			//nodePolarToSingleLoc(polarLength[node], angle[node], sLoc[node]);
			nodePolarToLoc(polarLength[node], angle[node], location[node]);
		}
	}
	
//{-----------------------------------------------------------------------------}
	private void adjustNodeLocsWithLengths (int anc, int node, double polarHeightToAncestor, int root){
		if (node==root)
			polarLength[node] = rootHeight;
		else {
			polarLength[node] = polarHeightToAncestor + getBranchLength(node) * scaling;
		}
		if (polarLength[node] < 0)
			polarLength[node] = 0;
		//nodePolarToSingleLoc(polarLength[node], angle[node], sLoc[node]);
		nodePolarToLoc(polarLength[node], angle[node], location[node]);
		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d); d = tree.nextSisterOfNodeUR(anc, node, d))
				adjustNodeLocsWithLengths(node, d, polarLength[node], root);
		}
	}
	
//{-----------------------------------------------------------------------------}
	private void adjustForLengths (int root){
		int tpa;
		tpa=(int)tree.tallestPathAboveNodeUR(root, root, 1.0);
		if (tpa!=0)
			scaling=(radius-rootHeight)/tpa;
		else // all assigned 0's give arbitrary scaling (not great solution!)
			scaling=20;
		adjustNodeLocsWithLengths(root, root, 0, root);
	}
//{-----------------------------------------------------------------------------}
	private void squish (int anc, int node, int centerX, int centerY, int root, double xExpansion, double yExpansion){
		double nodeX = treeDrawing.x[node];
		double nodeY =treeDrawing.y[node];
		
		treeDrawing.x[node] = centerX + (int)(xExpansion*(nodeX-centerX));
		treeDrawing.y[node] = centerY + (int)(yExpansion*(nodeY-centerY));
		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d) && d!=root; d = tree.nextSisterOfNodeUR(anc, node, d)) {
				squish(node, d, centerX, centerY, root, xExpansion, yExpansion);
			}
		}
	}
//{-----------------------------------------------------------------------------}
	private int findSelected (int anc, int node, int root){
		if (tree.getSelected(node))
			return node;
		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNodeUR(anc, node); tree.nodeExists(d) && d!=root; d = tree.nextSisterOfNodeUR(anc, node, d)) {
				int s = findSelected(node, d, root);
				if (s>0)
					return s;
			}
		}
		return 0;
	}
	/*_________________________________________________*/
	public void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Rectangle rect) { //Graphics g removed as parameter May 02
		if (MesquiteTree.OK(tree)) {
			this.tree = tree;
			this.treeDisplay = treeDisplay;
			if (treeDisplay.getExtras() !=null) {
				if (treeDisplay.getExtras().myElements(this)==null) {  //todo: need to do one for each treeDisplay!
					NodeLocsOvalExtra extra = new NodeLocsOvalExtra(this, treeDisplay);
					treeDisplay.addExtra(extra); 
					extras.addElement(extra);
				}
			}
			
			centralRoot=0;
			if (tree.anySelectedInClade(drawnRoot))
				centralRoot = findSelected(drawnRoot, drawnRoot, drawnRoot);
			if (centralRoot == 0) {
				 centralRoot = drawnRoot;
			}
			//int subRoot = tree.motherOfNode(drawnRoot);
			treeCenter = new Point();
			if (!compatibleWithOrientation(treeDisplay.getOrientation()))
				setDefaultOrientation(treeDisplay);
			int numNodes =tree.getNumNodeSpaces();
			if (oldNumTaxa != tree.getNumTaxa()) {
				location = new Point[numNodes];
				//sLoc = new DoublePt[numNodes];
				for (int i=0; i<numNodes; i++) {
					//sLoc[i]= new DoublePt();
					location[i] = new Point(0,0);
				}
				angle = new double[numNodes];
				polarLength  = new double[numNodes];
				oldNumTaxa=tree.getNumTaxa();
			}
			if (resetShowBranchLengths)
				treeDisplay.showBranchLengths=showBranchLengths.getValue();
			else {
				if (treeDisplay.showBranchLengths != showBranchLengths.getValue()) {
					showBranchLengths.setValue(treeDisplay.showBranchLengths);
					if (showBranchLengths.getValue()) 
						showScaleItem = addCheckMenuItem(null, "Show scale", makeCommand("toggleScale", this), showScale);
					else
						deleteMenuItem(showScaleItem);
					resetContainingMenuBar();
				}
			}
			this.treeDrawing = treeDisplay.getTreeDrawing();

			emptyRootSlices=1;
			angleBetweenTaxa=2 * Math.PI / tree.numberOfTerminalsInClade(drawnRoot); //TODO: should deal with other centerings
			treeRectangle = rect;
			if (treeRectangle.width<treeRectangle.height)
				radius=(treeRectangle.width * 3 )/ 8;
			else
				radius=(treeRectangle.height) * 3 / 8;
			circleSlice = radius / (mostNodesToTip(centralRoot, centralRoot) + emptyRootSlices);  //{v4: have it based upon an ellipse}
			treeCenter.x = /*treeRectangle.x +*/ treeRectangle.width / 2;
			treeCenter.y = /*treeRectangle.y +*/ treeRectangle.height / 2;

			location[centralRoot].y=treeCenter.y;
			location[centralRoot].x=treeCenter.x;
			//location[subRoot].y=treeCenter.y;
			//location[subRoot].x=treeCenter.x;
			terminalTaxaLocs(centralRoot, centralRoot);
			calcNodeLocs (centralRoot, centralRoot);
			if (showBranchLengths.getValue()) {
				adjustForLengths(centralRoot);
				//if (showScale.getValue())
				//	drawGrid(g, tree.tallestPathAboveNodeUR(centralRoot, centralRoot, 1.0), scaling, treeCenter);
			}
			for (int i=0; i<numNodes && i<treeDrawing.y.length; i++) {
				treeDrawing.y[i] = location[i].y;
				treeDrawing.x[i] = location[i].x;
				
			}
			squish(centralRoot, centralRoot, treeCenter.x, treeCenter.y, centralRoot, treeRectangle.width/3.0/radius, treeRectangle.height/3.0/radius);
			if (tree.numberOfDaughtersOfNode(centralRoot) == 2 && !showBranchLengths.getValue()) {
				int left = tree.firstDaughterOfNode(centralRoot);
				int right = tree.lastDaughterOfNode(centralRoot);
				treeDrawing.y[centralRoot] = (location[left].y + location[right].y)/2;
				treeDrawing.x[centralRoot] =  (location[left].x + location[right].x)/2;
			}
		}
	}
	
	public void drawGrid (Graphics g, double totalHeight, double scaling,  Point treeCenter) {
		if (g == null)
			return;
		double xExpansion = treeRectangle.width/3.0/radius;
		double yExpansion = treeRectangle.height/3.0/radius;
		Color c=g.getColor();
		double log10 = Math.log(10.0);
		double hundredthHeight = Math.exp(log10* ((int) (Math.log(totalHeight)/log10)-1));
		int countTenths = 0;
		double thisHeight = totalHeight + hundredthHeight;
		while ( thisHeight>=0) {
			if (countTenths % 10 == 0)
				g.setColor(Color.blue);
			else
				g.setColor(Color.cyan);
			thisHeight -= hundredthHeight;
			g.drawOval(treeCenter.x- (int)(xExpansion*((thisHeight*scaling))) - rootHeight,treeCenter.y- (int)(yExpansion*((thisHeight*scaling))) - rootHeight, (int)(xExpansion*2*((thisHeight*scaling) + rootHeight)),  (int)(yExpansion*2*((thisHeight*scaling) + rootHeight)));
			//if (countTenths % 10 == 0)
			//	g.drawString(MesquiteDouble.toString(totalScaleHeight - thisHeight), rightEdge + buffer, (int)(base- (thisHeight*scaling)));
			countTenths ++;
		}
		
		if (c!=null) g.setColor(c);
	}
}

class DoublePt {
	public double x, y;
}
class NodeLocsOvalExtra extends TreeDisplayBkgdExtra {
	NodeLocsOval locsModule;

	public NodeLocsOvalExtra (NodeLocsOval ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		locsModule = ownerModule;
	}
	/*.................................................................................................................*/
	public   String writeOnTree(Tree tree, int drawnRoot) {
		return null;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (locsModule.showScale.getValue() && locsModule.showBranchLengths.getValue())
			locsModule.drawGrid(g, tree.tallestPathAboveNodeUR(locsModule.centralRoot, locsModule.centralRoot, 1.0), locsModule.scaling, locsModule.treeCenter);
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	
}



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.SisterLengthsProportionAdjust;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.geom.Point2D;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class SisterLengthsProportionAdjust extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "sisterLengthsAdjust.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		SLPAdjustToolExtra newPj = new SLPAdjustToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Slide length between sister nodes"; 
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Adjusts the proportion of length allocated to two sister nodes. Drag to shift the length from one sister to another. This is especially "
				+ "helpful at the root, e.g. the balance between the outgroup and study group. Option-click for dialog box in which to enter the proportion explicitly.";

	}
	public boolean isSubstantive(){
		return true;
	}   	 
	public boolean isPrerelease(){
		return false;
	}   	 
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
}

/* ======================================================================== */
class SLPAdjustToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool adjustTool;
	SisterLengthsProportionAdjust slideModule;
	Tree tree;
	double originalTouchX, originalTouchY, lastX, lastY, lastNodeX, lastNodeY, lastSisterX, lastSisterY;
	double distanceOrigPtToTip ;
	double distanceOrigPtToBase;
	boolean lineOn = false;
	boolean dragOn = false;
	double lastBL;	
	boolean editorOn = false;
	int editorNode = -1;
	MesquiteWindow window;
	public SLPAdjustToolExtra (SisterLengthsProportionAdjust ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		slideModule = ownerModule;
		adjustTool = new TreeTool(this,  "adjustor", ownerModule.getPath() , "sisterLengthsAdjust.gif", 2,1,"Slide branch length between sister nodes", "This tool adjusts branch lengths of two sister branches, sliding their total branch length between them. Option-click for dialog box.");
		adjustTool.setTouchedCommand(MesquiteModule.makeCommand("touchedLengthsAdjust",  this));
		adjustTool.setDraggedCommand(MesquiteModule.makeCommand("draggedLengthsAdjust",  this));
		adjustTool.setDroppedCommand(MesquiteModule.makeCommand("droppedLengthsAdjust",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			window = ((MesquiteWindow)ownerModule.containerOfModule());
			window.addTool(adjustTool);
		}
	}
	public void doTreeCommand(String command, String arguments){
		Tree tree = treeDisplay.getTree();
		if (!(tree instanceof MesquiteTree)){
			MesquiteMessage.warnProgrammer("Action can't be completed since tree is not a native Mesquite tree");
		}
		else if (tree!=null) {
			((MesquiteTree)tree).doCommand(command, arguments, CommandChecker.defaultChecker);
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
	}
	/*.................................................................................................................*/
	public Tree getTree() {
		return treeDisplay.getTree();
	}
	int touchedNode = 0;
	int sisterNode = 0;
	double totalBranchLength = 0;
	double screenLengthNode = 0;
	double gainBranchLengthByNode = 0;
	double totalScreenLength = 0;
	double proportionThis = 0;
	int ovalRadius = 8;
	boolean slideUnassignedSisterWarning = false;

	double length(double x, double y, double x2, double y2){
		return Math.sqrt((x-x2)*(x-x2) + (y-y2)*(y-y2));
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		MesquiteTree t=null; //
		if (treeDisplay!=null) {
			Tree trt = treeDisplay.getTree();
			if (trt instanceof MesquiteTree)
				t = (MesquiteTree)trt;
			else
				t = null;
		}

		if (checker.compare(this.getClass(), "Touch on branch to change its length versus its sister\'s", "[branch number] [x coordinate touched] [y coordinate touched] [modifiers]", commandName, "touchedLengthsAdjust")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			touchedNode= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);
			int mother = t.motherOfNode(touchedNode);
			dragOn = false;
			//If node has just one sister, proceed
			if (t.numberOfDaughtersOfNode(mother)==2) {
				sisterNode = 0;
				if (t.firstDaughterOfNode(mother) == touchedNode)
					sisterNode = t.lastDaughterOfNode(mother);
				else
					sisterNode = t.firstDaughterOfNode(mother);

				double thisLength = t.getBranchLength(touchedNode);
				double sisterLength = t.getBranchLength(sisterNode);
				if (thisLength == MesquiteDouble.unassigned)
					AlertDialog.notice(slideModule.containerOfModule(), "Tool needs assigned branch lengths", "This tool cannot be be applied to a branch without an assigned branch length. "
							+" You can assign branch lengths individually, or choose Tree>Alter>All Unassigned Branch Lengths to 1.0");
				else if (thisLength + sisterLength == 0)
					AlertDialog.notice(slideModule.containerOfModule(), "Tool needs nonzero branch lengths", "This tool can be applied only when least one of sisters has a non-zero branch length.");
				else if (mod != null && StringUtil.indexOfIgnoreCase(mod, "option")>=0){
					if (thisLength == MesquiteDouble.unassigned)
						thisLength = 0;
					if (sisterLength == MesquiteDouble.unassigned)
						sisterLength = 0;
					double totalLength = thisLength+sisterLength;
					String totalAsString = MesquiteDouble.toStringDigitsSpecified(totalLength, 5);
					double proportionThis = thisLength/totalLength;
					double newProportion = MesquiteDouble.queryDouble(slideModule.containerOfModule(), "Set proportion", "Branch lengths of this touched branch and its sister branch sum to " + totalAsString 
							+ ". The proportion of this length currently assigned to the touched branch is currently shown below. " 
							+ "To change this proportion, and thus slide branch length from one sister to another, please enter below a new proportion of length for the touched branch.", proportionThis);
					if (!MesquiteDouble.isCombinable(newProportion) || newProportion<=0 || newProportion>=1) {
						AlertDialog.notice(slideModule.containerOfModule(), "Tool needs nonzero branch lengths", "This tool can be applied only when least one of sisters has a non-zero branch length.");
						return null;
					}
					//OK, at this point, a good new proportion has been returned. Adjust lengths.
					thisLength = totalLength*newProportion;
					sisterLength = totalLength - thisLength;
					slideModule.logln("Setting touched branch\'s length to " + MesquiteDouble.toStringDigitsSpecified(thisLength, 5) + " and its sister\'s length to " +  MesquiteDouble.toStringDigitsSpecified(sisterLength, 5));
					t.setBranchLength(touchedNode, thisLength, false);
					t.setBranchLength(sisterNode, sisterLength, false);
					t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
				}
				else {
					Point2D.Double newOnLine = treeDisplay.getTreeDrawing().projectionOnLine(touchedNode, x, y);
					originalTouchX = newOnLine.getX();
					originalTouchY = newOnLine.getY();
					double tipX = treeDisplay.getTreeDrawing().lineTipX[touchedNode];
					double tipY = treeDisplay.getTreeDrawing().lineTipY[touchedNode];
					double baseX = treeDisplay.getTreeDrawing().lineBaseX[touchedNode];
					double baseY = treeDisplay.getTreeDrawing().lineBaseY[touchedNode];
					distanceOrigPtToTip = length(tipX, tipY, originalTouchX, originalTouchY);
					distanceOrigPtToBase = length(baseX, baseY, originalTouchX, originalTouchY);
					Point2D.Double tipOnLine = treeDisplay.getTreeDrawing().projectionOnLine(touchedNode, tipX, tipY);
					lastX = tipOnLine.getX();
					lastY = tipOnLine.getY();
					lastNodeX = lastX;
					lastNodeY = lastY;
					lastSisterX =  treeDisplay.getTreeDrawing().lineTipX[sisterNode];
					lastSisterY = treeDisplay.getTreeDrawing().lineTipY[sisterNode];
					double sisterBaseX =  treeDisplay.getTreeDrawing().lineBaseX[sisterNode];
					double sisterBaseY = treeDisplay.getTreeDrawing().lineBaseY[sisterNode];

					if (thisLength == MesquiteDouble.unassigned)
						thisLength = 0;
					if (sisterLength == MesquiteDouble.unassigned)
						sisterLength = 0;
					window.setExplanation("Branch length, this: " + MesquiteDouble.toString(thisLength) + " and sister: " + MesquiteDouble.toString(sisterLength));

					totalBranchLength = thisLength+sisterLength;
					screenLengthNode = length(lastX, lastY, baseX, baseY);
					totalScreenLength = screenLengthNode + length(lastSisterX, lastSisterY, sisterBaseX, sisterBaseY);
					proportionThis = thisLength/totalBranchLength;

					Graphics g = treeDisplay.getGraphics();
					Shape clip=g.getClip();
					g.setClip(0, 0, 99999, 99999);
					g.setColor(Color.blue);
					GraphicsUtil.fillXOROval(g,lastNodeX-ovalRadius, lastNodeY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
					g.setColor(Color.red);
					GraphicsUtil.fillXOROval(g,lastSisterX-ovalRadius, lastSisterY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
					lineOn=true;
					g.setClip(clip);
					g.dispose();
					lineOn = true;
				}
			}
			else AlertDialog.notice(slideModule.containerOfModule(), "Tool applies to binary divergences only", "This tool can be applied only to a branch that has a single sister branch.");

		}

		else if (checker.compare(this.getClass(), "Adjust tool is being dragged", "[branch number][x coordinate][y coordinate]", commandName, "draggedLengthsAdjust")) {
			if (t==null)
				return null;
			if (!lineOn)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			//The follow should be the same as from the touch!
			int node= MesquiteInteger.fromString(arguments, io);

			dragOn = true;
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			Point2D.Double newOnLine = treeDisplay.getTreeDrawing().projectionOnLine(node, x, y);
			double dragX = newOnLine.getX();
			double dragY = newOnLine.getY();
			double tipX = treeDisplay.getTreeDrawing().lineTipX[node];
			double tipY = treeDisplay.getTreeDrawing().lineTipY[node];
			double baseX = treeDisplay.getTreeDrawing().lineBaseX[node];
			double baseY = treeDisplay.getTreeDrawing().lineBaseY[node];
			double tipSisterX = treeDisplay.getTreeDrawing().lineTipX[sisterNode];
			double tipSisterY = treeDisplay.getTreeDrawing().lineTipY[sisterNode];
			double baseSisterX = treeDisplay.getTreeDrawing().lineBaseX[sisterNode];
			double baseSisterY = treeDisplay.getTreeDrawing().lineBaseY[sisterNode];
			//drag point isn't going to be either at base or at tip. Let's calcualte distance from both.  
			double distanceDragPtToTip = length(tipX, tipY, dragX, dragY);
			double distanceDragPtToBase =  length(baseX, baseY, dragX, dragY); 

			double gainScreenLengthByNode = 0;
			if (Math.abs((distanceOrigPtToTip + distanceOrigPtToBase) - (distanceDragPtToTip+distanceDragPtToBase)) <0.1){ // between
				gainScreenLengthByNode =  distanceDragPtToBase-distanceOrigPtToBase;

			}
			else if (distanceDragPtToTip < distanceDragPtToBase){ // above tip
					gainScreenLengthByNode =  distanceDragPtToBase-distanceOrigPtToBase;
			}
			else {// below base
				gainScreenLengthByNode =  -distanceOrigPtToBase;
			}
			double available = totalScreenLength-screenLengthNode;
			if (tree.branchLengthUnassigned(sisterNode)){
				available =  0; //can't gain from unassigned sister!
				if (gainScreenLengthByNode>0 && !slideUnassignedSisterWarning){
					slideModule.logln("You can't slide length from the sister branch, because it has unassigned length");
					slideUnassignedSisterWarning = true;
				}
			}
			
			if (gainScreenLengthByNode>available)
				gainScreenLengthByNode = available;
			gainBranchLengthByNode = gainScreenLengthByNode*totalBranchLength/totalScreenLength;

			double dX = 0;
			double dY = 0;
			double dSisterX = 0;
			double dSisterY = 0;
			double newX = 0;
			double newY = 0;
			double newSisterX = 0;
			double newSisterY = 0;
			if (screenLengthNode>0){
				if (tipX == baseX)
					dY = gainScreenLengthByNode;
				else {
					double slope = (tipY-baseY)/(tipX-baseX);
					dX = Math.sqrt(gainScreenLengthByNode*gainScreenLengthByNode/(slope*slope + 1));
					if (gainScreenLengthByNode<0)
						dX = -dX;
					dY = dX*slope;
				}
				if (tipSisterX == baseSisterX){
					dSisterY = gainScreenLengthByNode;
				}
				else {
					double slope = (tipSisterY-baseSisterY)/(tipSisterX-baseSisterX);
					dSisterX = Math.sqrt(gainScreenLengthByNode*gainScreenLengthByNode/(slope*slope + 1));
					if (gainScreenLengthByNode<0)
						dSisterX = -dSisterX;
					dSisterY = dSisterX*slope;
				}
			}
			else {
			}
			if (tipX>baseX)
				newX = tipX+dX;
			else
				newX = tipX-dX;
			if (tipY>baseY)
				newY = tipY+dY;
			else
				newY = tipY-dY;
			if (tipSisterX>baseSisterX)
				newSisterX = tipSisterX-dSisterX;
			else
				newSisterX = tipSisterX+dSisterX;
			if (tipSisterY>baseSisterY)
				newSisterY = tipSisterY-dSisterY;
			else
				newSisterY = tipSisterY+dSisterY;
			//OK, for this node, ned to extrapolate its spot from tip further by 

			Graphics g =  treeDisplay.getGraphics();
			Shape clip = g.getClip();
			g.setClip(0, 0, 99999, 99999);
			g.setColor(Color.blue);

			boolean showProjection = false;
			//UNDO spot
			if (showProjection){
				g.setColor(Color.green);
				GraphicsUtil.unfillXOROval(treeDisplay, g, (int)(lastX-8), (int)(lastY-8), 16, 16);
			}
			g.setColor(Color.blue);
			GraphicsUtil.fillXOROval(g,lastNodeX-ovalRadius, lastNodeY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
			g.setColor(Color.red);
			GraphicsUtil.fillXOROval(g,lastSisterX-ovalRadius, lastSisterY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);


			//REDRAW spot
			if (showProjection){
				g.setColor(Color.green);
				GraphicsUtil.unfillXOROval(treeDisplay, g, (int)(dragX-8), (int)(dragY-8), 16, 16);
			}
			g.setColor(Color.blue);
			GraphicsUtil.fillXOROval(g,newX-ovalRadius, newY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
			g.setColor(Color.red);
			GraphicsUtil.fillXOROval(g,newSisterX-ovalRadius, newSisterY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
			lastX = dragX;
			lastY = dragY;
			lastNodeX = newX;
			lastNodeY = newY;
			lastSisterX = newSisterX;
			lastSisterY = newSisterY;
			g.setClip(clip);

		}
		else if (checker.compare(this.getClass(), "Adjust tool is being dropped", "[branch number][x coordinate][y coordinate]", commandName, "droppedLengthsAdjust")) {
			if (t==null)
				return null;
			lineOn = false;
			//UNDO
			Graphics g =  treeDisplay.getGraphics();
			Shape clip = g.getClip();
			g.setClip(0, 0, 99999, 99999);
			g.setColor(Color.blue);
			GraphicsUtil.fillXOROval(g,lastNodeX-ovalRadius, lastNodeY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
			g.setColor(Color.red);
			GraphicsUtil.fillXOROval(g,lastSisterX-ovalRadius, lastSisterY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
			g.setClip(clip);
			originalTouchX = originalTouchY = lastX =  lastY = lastNodeX =  lastNodeY = lastSisterX = lastSisterY = screenLengthNode = totalBranchLength = totalScreenLength = proportionThis = 0;
			if (!dragOn) {
				gainBranchLengthByNode = 0;
				touchedNode = sisterNode = 0;
				return null;
			}
			dragOn = false;
			boolean sisterWasUnassigned = t.branchLengthUnassigned(sisterNode);
			if (gainBranchLengthByNode != 0){
				t.setBranchLength(touchedNode, t.getBranchLength(touchedNode, 0) + gainBranchLengthByNode, false);
				
				t.setBranchLength(sisterNode, t.getBranchLength(sisterNode, 0) - gainBranchLengthByNode, false);
				t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
				if (sisterWasUnassigned)
					slideModule.logln("\nBecause the sister branch had no assigned length, it was treated as having zero length to begin with.\n");
			}
			gainBranchLengthByNode = 0;
			touchedNode = sisterNode = 0;

		}

		return null;
	}
	public void turnOff() {

		slideModule.extras.removeElement(this);
		super.turnOff();
	}
}





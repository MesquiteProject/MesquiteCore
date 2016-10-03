/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.NodePositionAdjust;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NodePositionAdjust extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "adjustPosition.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		PAdjustToolExtra newPj = new PAdjustToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node Position Adjust";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool to adjust the depth of a node in the tree";
	}
	public boolean isSubstantive(){
		return false;
	}   	 
}

/* ======================================================================== */
class PAdjustToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool adjustTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	NodePositionAdjust selectModule;
	Tree tree;
	double originalX, originalY, lastX, lastY;
	double lastBL;	
	boolean lineOn = true;
	boolean editorOn = false;
	int editorNode = -1;
	boolean dragMode = true; //default mode - if true,then drags branch to extend; needs modifier keys for editor; if false, opposite
	int ovalRadius = 8;
	double upperLimit = 0.0;
	double lowerLimit = 0.0;

	public PAdjustToolExtra (NodePositionAdjust ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		selectModule = ownerModule;
		adjustTool = new TreeTool(this,  "posadjustor", ownerModule.getPath(),"adjustPosition.gif", 7,7,"Adjust node position", "This tool adjusts branch lengths in such a way as to keep the relative positions of nodes constant, except for the node moved.");
		adjustTool.setTouchedCommand(MesquiteModule.makeCommand("touchedPositionAdjust",  this));
		adjustTool.setDroppedCommand(MesquiteModule.makeCommand("droppedPositionAdjust",  this));
		adjustTool.setDraggedCommand(MesquiteModule.makeCommand("draggedPositionAdjust",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(adjustTool);
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		this.tree = tree;
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
	}

	public void drawThickLine(Graphics g, double fromX, double fromY, double toX, double toY){
		GraphicsUtil.drawLine(g,fromX, fromY, toX, toY);
		GraphicsUtil.drawLine(g,fromX+1, fromY, toX+1, toY);
		GraphicsUtil.drawLine(g,fromX+2, fromY, toX+2, toY);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		Tree trt = treeDisplay.getTree();
		MesquiteTree t = null;
		if (trt instanceof MesquiteTree)
			t = (MesquiteTree)trt;
		if (checker.compare(this.getClass(), "Adjust tool has touched branch", "[branch number][x coordinate touched][y coordinate touched][modifiers]", commandName, "touchedPositionAdjust")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);


			Point2D.Double newOnLine = treeDisplay.getTreeDrawing().projectionOnLine(node, x, y);
			originalX = newOnLine.getX();
			originalY = newOnLine.getY();
			//lastX= newOnLine.x;
			//lastY = newOnLine.y;
			Graphics g = null;
			if (GraphicsUtil.useXORMode(null, false)){
				g = treeDisplay.getGraphics();
				g.setXORMode(Color.white);
				g.setColor(Color.red);
			}
			//double bX = treeDisplay.getTreeDrawing().lineBaseX[node];
			//double bY = treeDisplay.getTreeDrawing().lineBaseY[node];
			//Math.sqrt((originalY-bY)*(originalY-bY) + (originalX-bX)*(originalX-bX));
			lastBL = tree.getBranchLength(node);
			double shortestAbove = MesquiteDouble.unassigned;
			for (int daughter = t.firstDaughterOfNode(node); t.nodeExists(daughter); daughter = t.nextSisterOfNode(daughter))
				shortestAbove = MesquiteDouble.minimum(shortestAbove, tree.getBranchLength(daughter));
			if (shortestAbove == MesquiteDouble.unassigned)
				upperLimit = MesquiteDouble.infinite;
			else if (MesquiteDouble.isCombinable(lastBL))
				upperLimit = shortestAbove + lastBL;
			else
				upperLimit = shortestAbove + 1.0;
			double ibX = treeDisplay.getTreeDrawing().lineBaseX[node];
			double ibY = treeDisplay.getTreeDrawing().lineBaseY[node];
			lastX = treeDisplay.getTreeDrawing().lineTipX[node];
			lastY = treeDisplay.getTreeDrawing().lineTipY[node];
			if (GraphicsUtil.useXORMode(null, false)){
				drawThickLine(g,ibX, ibY, lastX, lastY);
				for (int daughter = t.firstDaughterOfNode(node); t.nodeExists(daughter); daughter = t.nextSisterOfNode(daughter))
					drawThickLine(g,treeDisplay.getTreeDrawing().lineTipX[daughter], treeDisplay.getTreeDrawing().lineTipY[daughter], lastX, lastY);
				GraphicsUtil.fillOval(g,lastX-ovalRadius, lastY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
				try {
					GraphicsUtil.drawString(g,MesquiteDouble.toString(lastBL), lastX+10, lastY);
				}
				catch(InternalError e){  //workaround for bug on windows java 1.7.
				}
				catch(Throwable e){
				}
				lineOn=true;
				g.dispose();
			}
		}
		else if (checker.compare(this.getClass(), "Adjust tool has been dropped", "[branch number][x coordinate dropped][y coordinate dropped]", commandName, "droppedPositionAdjust")) {
			if (t==null)
				return null;
			if (editorOn)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (lineOn) {
				Point2D.Double newOnLine = treeDisplay.getTreeDrawing().projectionOnLine(node, x, y);
				double bX = treeDisplay.getTreeDrawing().lineBaseX[node];
				double bY = treeDisplay.getTreeDrawing().lineBaseY[node];
				double tX = treeDisplay.getTreeDrawing().lineTipX[node];
				double tY = treeDisplay.getTreeDrawing().lineTipY[node];
				double lengthLine =  Math.sqrt((originalY-bY)*(originalY-bY) + (originalX-bX)*(originalX-bX));
				double bL;
				if (lengthLine!=0) {
					double extension =  Math.sqrt((newOnLine.getY()-bY)*(newOnLine.getY()-bY) + (newOnLine.getX()-bX)*(newOnLine.getX()-bX))/lengthLine;
					if (t.getBranchLength(node)==0 || t.branchLengthUnassigned(node)) 
						bL = extension;
					else
						bL = t.getBranchLength(node)*extension;
				}
				else
					bL = 1;

				if (bL> upperLimit) 
					bL= upperLimit;
				else if (bL<lowerLimit)
					bL = lowerLimit;
				double oldBL = t.getBranchLength(node);
				if (!MesquiteDouble.isCombinable(oldBL))
					oldBL = 1.0;
				t.setBranchLength(node, bL, false);
				double difference = oldBL - t.getBranchLength(node);
				for (int daughter = t.firstDaughterOfNode(node); t.nodeExists(daughter); daughter = t.nextSisterOfNode(daughter))
					if (MesquiteDouble.isCombinable(t.getBranchLength(daughter)))
						t.setBranchLength(daughter, t.getBranchLength(daughter) + difference, false);
				t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
				Graphics g = treeDisplay.getGraphics();
				g.setPaintMode();
				g.dispose();
				treeDisplay.pleaseUpdate(true);
				lineOn=false;
			}
		}


		else if (checker.compare(this.getClass(), "Adjust tool is being dragged", "[branch number][x coordinate][y coordinate]", commandName, "draggedPositionAdjust")) {
			if (t==null)
				return null;
			if (editorOn)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (lineOn) {
				Point2D.Double newOnLine = treeDisplay.getTreeDrawing().projectionOnLine(node, x, y);
				//WARNING":  This shouldn't result in length increase if simple click and release with no drag; must subtract original X, Y
				Graphics g = null;
				if (GraphicsUtil.useXORMode(null, false)){
					g = treeDisplay.getGraphics();
					g.setXORMode(Color.white);
					g.setColor(Color.red);
				}
				//g.fillOval(lastX-ovalRadius, lastY-ovalRadius, ovalRadius + ovalRadius, ovalRadius + ovalRadius);
				//g.fillOval(newOnLine.x-ovalRadius, newOnLine.y -ovalRadius, ovalRadius + ovalRadius, ovalRadius + ovalRadius);

				//g.drawLine(originalX, originalY, lastX, lastY);
				//g.drawLine(originalX, originalY, newOnLine.x, newOnLine.y);

//				if decreasing, & unassigned involved: push unassigned down and assign values to unassigned above; if increasing, push unassigne up
				double ibX = treeDisplay.getTreeDrawing().lineBaseX[node];
				double ibY = treeDisplay.getTreeDrawing().lineBaseY[node];
				double itX = treeDisplay.getTreeDrawing().lineTipX[node];
				double itY = treeDisplay.getTreeDrawing().lineTipY[node];

				double bX = ibX;
				double bY = ibY;
				double tX =itX;
				double tY = itY;
				double lengthLine =  Math.sqrt((originalY-bY)*(originalY-bY) + (originalX-bX)*(originalX-bX));
				if (lengthLine!=0) {
					if (GraphicsUtil.useXORMode(null, false)){
						if (MesquiteTrunk.isMacOSX() && MesquiteTrunk.getJavaVersionAsDouble()>=1.5 && MesquiteTrunk.getJavaVersionAsDouble()<1.6)  //due to a JVM bug
							GraphicsUtil.fillRect(g,lastX, lastY-20, 100, 20);
						GraphicsUtil.drawString(g,MesquiteDouble.toString(lastBL), lastX+10, lastY);
						if (MesquiteTrunk.isMacOSX() && MesquiteTrunk.getJavaVersionAsDouble()>=1.5 && MesquiteTrunk.getJavaVersionAsDouble()<1.6)  //due to a JVM bug
							GraphicsUtil.fillRect(g,lastX, lastY-20, 100, 20);
					}
					double extension =  Math.sqrt((newOnLine.getY()-bY)*(newOnLine.getY()-bY) + (newOnLine.getX()-bX)*(newOnLine.getX()-bX))/lengthLine;
					double bL;
					if (t.getBranchLength(node)==0 || t.branchLengthUnassigned(node)) 
						bL = extension;
					else
						bL = t.getBranchLength(node)*extension;
					if (bL> upperLimit ) {
						bL= upperLimit;
						if (t.getBranchLength(node)==0 || t.branchLengthUnassigned(node))
							extension = upperLimit;
						else
							extension = upperLimit/t.getBranchLength(node);
					}
					else if (bL<lowerLimit) {
						bL = lowerLimit;
						if (t.getBranchLength(node)==0 || t.branchLengthUnassigned(node))
							extension = lowerLimit;
						else
							extension = lowerLimit/t.getBranchLength(node);
					}
					lastBL = bL;
					if (GraphicsUtil.useXORMode(null, false)){
						drawThickLine(g,ibX, ibY, lastX, lastY);
						for (int daughter = t.firstDaughterOfNode(node); t.nodeExists(daughter); daughter = t.nextSisterOfNode(daughter))
							drawThickLine(g,treeDisplay.getTreeDrawing().lineTipX[daughter], treeDisplay.getTreeDrawing().lineTipY[daughter], lastX, lastY);
						GraphicsUtil.fillOval(g,lastX-ovalRadius, lastY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
					}
					double newX =ibX+(int)(extension*(tX-bX));
					double newY = ibY+(int)(extension*(tY-bY));
					if (GraphicsUtil.useXORMode(null, false)){
						GraphicsUtil.drawString(g,MesquiteDouble.toString(bL), newX+10, newY);
						drawThickLine(g,ibX, ibY, newX, newY);
						for (int daughter = t.firstDaughterOfNode(node); t.nodeExists(daughter); daughter = t.nextSisterOfNode(daughter))
							drawThickLine(g,treeDisplay.getTreeDrawing().lineTipX[daughter], treeDisplay.getTreeDrawing().lineTipY[daughter], newX, newY);
						GraphicsUtil.fillOval(g,newX-ovalRadius, newY-ovalRadius, ovalRadius+ovalRadius, ovalRadius+ovalRadius);
					}
					lastX= newX;
					lastY = newY;
				}

				//lastX= newOnLine.x;
				//lastY = newOnLine.y;
			}
		}
		return null;
	}
	public void turnOff() {

		selectModule.extras.removeElement(this);
		super.turnOff();
	}
}




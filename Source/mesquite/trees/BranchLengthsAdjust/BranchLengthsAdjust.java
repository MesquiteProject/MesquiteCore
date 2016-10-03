/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BranchLengthsAdjust;

import java.util.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class BranchLengthsAdjust extends TreeDisplayAssistantI {
	public Vector extras;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		AdjustToolExtra newPj = new AdjustToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires a module to alter or transform branch lengths", "[name of module]", commandName, "alterBranchLengths")) {
			BranchLengthsAlterer ble = (BranchLengthsAlterer)hireNamedEmployee(BranchLengthsAlterer.class, arguments);
			if (ble!=null) {
				transformTree(ble);

				fireEmployee(ble); //todo: might be good to keep it around, and remembering it if user wants to change parameters
			}
			//if arguments have name of estimator then use it, passing it the trees
		}
		else if (checker.compare(this.getClass(), "Sets branch length of selected nodes", null, commandName, "acceptBranchLengths")) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				AdjustToolExtra tCO = (AdjustToolExtra)obj;
				tCO.doCommand("setLength", arguments, checker);
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	void transformTree(TreeTransformer ble){
		Enumeration e = extras.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			AdjustToolExtra tCO = (AdjustToolExtra)obj;
			Tree t = tCO.getTree();
			if (t instanceof AdjustableTree) {
				boolean success = ble.transformTree((AdjustableTree)t, null, true);
			}
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Branch Lengths Adjust";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool to adjust branch lengths of trees.";
	}
	public void getSubfunctions(){
		registerSubfunction(new FunctionExplanation("Set Branch Lengths", "(A tool of the Tree Window) Allows you to set length of touched branch", null, getPath() + "adjustLengths.gif"));
		registerSubfunction(new FunctionExplanation("Adjust Branch Lengths by Stretching", "(A tool of the Tree Window) By dragging, stretches branch touched", null, getPath() + "dragLengths.gif"));
		super.getSubfunctions();
	}

}

/* ======================================================================== */
class AdjustToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool adjustTool, stretchTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	BranchLengthsAdjust selectModule;
	Tree tree;
	double originalX, originalY, lastX, lastY;
	boolean lineOn = true;
	MiniNumberEditor miniEditor;
	double lastBL;	
	boolean editorOn = false;
	int editorNode = -1;
	boolean dragMode = false; //default mode - if true,then drags branch to extend; needs modifier keys for editor; if false, opposite

	public AdjustToolExtra (BranchLengthsAdjust ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		selectModule = ownerModule;
		adjustTool = new TreeTool(this,  "adjustor", ownerModule.getPath() , "adjustLengths.gif", 2,1,"Adjust branch length", "This tool adjusts branch lengths.  When a branch is touched, a small text editing box appears in which a new branch length can be entered; touching the little blue button enters the length.  If a modifier key such as Control is held down or the Right Mouse button is used as the branch is touched, you can drag to change the length of the branch.");
		adjustTool.setTouchedCommand(MesquiteModule.makeCommand("touchedLengthsAdjust",  this));
		adjustTool.setDroppedCommand(MesquiteModule.makeCommand("droppedLengthsAdjust",  this));
		adjustTool.setDraggedCommand(MesquiteModule.makeCommand("draggedLengthsAdjust",  this));
		stretchTool = new TreeTool(this,  "stretchAdjustor", ownerModule.getPath() , "dragLengths.gif", 2,2,"Stretch branch", "This tool adjusts branch lengths by grabbing and stretching.");
		stretchTool.setTouchedCommand(MesquiteModule.makeCommand("touchedLengthsStretch",  this));
		stretchTool.setDroppedCommand(MesquiteModule.makeCommand("droppedLengthsStretch",  this));
		MesquiteCommand dragCommand = MesquiteModule.makeCommand("draggedLengthsStretch",  this);
		stretchTool.setDraggedCommand(dragCommand);
		dragCommand.setDontDuplicate(true);
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(stretchTool);
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(adjustTool);
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
		this.tree = tree;
		if (editorOn) {
			if (tree.nodeExists(editorNode))
				miniEditor.setLocation((int)treeDisplay.getTreeDrawing().x[editorNode], (int)treeDisplay.getTreeDrawing().y[editorNode]);
			else hideMiniEditor();
		}
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
	}
	/*.................................................................................................................*/
	public Tree getTree() {
		return treeDisplay.getTree();
	}

	private void setMiniEditor(int node, int x,int y){
		Tree t = treeDisplay.getTree();
		if (t==null)
			return;
		editorNode = node;
		if (miniEditor == null) {
			miniEditor = new MiniNumberEditor(ownerModule, ownerModule.makeCommand("acceptLength", this));
			addPanelPlease(miniEditor);
		}
		miniEditor.setLocation((int)treeDisplay.getTreeDrawing().x[node], (int)treeDisplay.getTreeDrawing().y[node]);
		miniEditor.setNumber(t.getBranchLength(node));
		miniEditor.setVisible(true);
		editorOn = true;
	}
	private void hideMiniEditor(){
		miniEditor.setVisible(false);
		editorOn = false;
	}
	public void changeLengthsSelected(AdjustableTree tree, int node, double length) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			changeLengthsSelected(tree, d, length);
		if (tree.getSelected(node))
			tree.setBranchLength(node, length, false);
	}
	void stretchTouched(int node, int x, int y, String mod) {
		Point2D.Double newOnLine = treeDisplay.getTreeDrawing().projectionOnLine(node, x, y);
		originalX = newOnLine.getX();
		originalY = newOnLine.getY();
		lastBL = tree.getBranchLength(node);
		lastX = treeDisplay.getTreeDrawing().lineTipX[node];
		lastY = treeDisplay.getTreeDrawing().lineTipY[node];
		double ibX = treeDisplay.getTreeDrawing().lineBaseX[node];
		double ibY = treeDisplay.getTreeDrawing().lineBaseY[node];
		if (GraphicsUtil.useXORMode(null, false)){
			Graphics g = treeDisplay.getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.red);
			GraphicsUtil.drawString(g,MesquiteDouble.toString(lastBL), lastX+10, lastY);
			drawLine(g,ibX, ibY, lastX, lastY);
			drawLine(g,ibX+1, ibY, lastX+1, lastY);
			drawLine(g,ibX+2, ibY, lastX+2, lastY);
			g.dispose();
		}
		lineOn=true;
	}
	void drawLine(Graphics g, double x, double y, double x2, double y2){
		if (g==null)
			return;
		if (x==x2 && y>y2)
			GraphicsUtil.drawLine(g, x2, y2, x, y);
		else
			GraphicsUtil.drawLine(g,x, y, x2, y2);
	}
	int count=0;
	void stretchDragged(Tree t, int node, int x, int y){
		Point2D.Double newOnLine = treeDisplay.getTreeDrawing().projectionOnLine(node, x, y);
		//WARNING":  This shouldn't result in length increase if simple click and release with no drag; must subtract original X, Y
		double ibX = treeDisplay.getTreeDrawing().lineBaseX[node];
		double ibY = treeDisplay.getTreeDrawing().lineBaseY[node];
		double itX = treeDisplay.getTreeDrawing().lineTipX[node];
		double itY = treeDisplay.getTreeDrawing().lineTipY[node];
		if (GraphicsUtil.useXORMode(null, false)){
			Graphics g = treeDisplay.getGraphics();
			g.setXORMode(Color.white);
			g.setColor(Color.red);


			double bX = ibX;
			double bY = ibY;
			double tX =itX;
			double tY = itY;
			double lengthLine =  Math.sqrt((originalY-bY)*(originalY-bY) + (originalX-bX)*(originalX-bX));
			if (lengthLine!=0) {
				double extension =  Math.sqrt((newOnLine.getY()-bY)*(newOnLine.getY()-bY) + (newOnLine.getX()-bX)*(newOnLine.getX()-bX))/lengthLine;
				double bL;
				if (t.getBranchLength(node)==0) {
					bL = extension;
				}
				else{
					if (t.branchLengthUnassigned(node))
						bL = extension;
					else
						bL = t.getBranchLength(node)*extension;
				}

				drawLine(g,ibX, ibY, lastX, lastY);
				drawLine(g,ibX+1, ibY, lastX+1, lastY);
				drawLine(g,ibX+2, ibY, lastX+2, lastY);
				double newX =ibX+(extension*(tX-bX));
				double newY = ibY+(extension*(tY-bY));
				if (MesquiteTrunk.isMacOSX() && MesquiteTrunk.getJavaVersionAsDouble()>=1.5 && MesquiteTrunk.getJavaVersionAsDouble()<1.6)  //due to a JVM bug
					GraphicsUtil.fillRect(g,lastX, lastY-20, 100, 20);
				GraphicsUtil.drawString(g,MesquiteDouble.toString(lastBL), lastX+10, lastY);
				if (MesquiteTrunk.isMacOSX() && MesquiteTrunk.getJavaVersionAsDouble()>=1.5 && MesquiteTrunk.getJavaVersionAsDouble()<1.6)  //due to a JVM bug
					GraphicsUtil.fillRect(g,lastX, lastY-20, 100, 20);
				GraphicsUtil.drawString(g,MesquiteDouble.toString(bL), newX+10, newY);
				count++;
				lastBL = bL;
				lastX= newX;
				lastY = newY;
				drawLine(g,ibX, ibY, newX, newY);
				drawLine(g,ibX+1, ibY, newX+1, newY);
				drawLine(g,ibX+2, ibY, newX+2, newY);
			}
			g.setPaintMode();
			g.dispose();
		}
	}
	void stretchDropped(MesquiteTree t, int node, int x, int y){
		Point2D newOnLine = treeDisplay.getTreeDrawing().projectionOnLine(node, x, y);
		double bX = treeDisplay.getTreeDrawing().lineBaseX[node];
		double bY = treeDisplay.getTreeDrawing().lineBaseY[node];
		double tX = treeDisplay.getTreeDrawing().lineTipX[node];
		double tY = treeDisplay.getTreeDrawing().lineTipY[node];
		double lengthLine =  Math.sqrt((originalY-bY)*(originalY-bY) + (originalX-bX)*(originalX-bX));
		if (lengthLine!=0) {
			double extension =  Math.sqrt((newOnLine.getY()-bY)*(newOnLine.getY()-bY) + (newOnLine.getX()-bX)*(newOnLine.getX()-bX))/lengthLine;
			if (t.getBranchLength(node)==0) {
				t.setBranchLength(node, extension, false);
			}
			else{
				if (t.branchLengthUnassigned(node))
					t.setBranchLength(node, extension, false);
				else
					t.setBranchLength(node, t.getBranchLength(node)*extension, false);
			}
		}
		else
			t.setBranchLength(node, 1, false);
		t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
		Graphics g = treeDisplay.getGraphics();
		g.setPaintMode(); //why is this done? getGraphics creates a new graphics object.
		treeDisplay.pleaseUpdate(true);
		lineOn=false;
		if (g!=null)
			g.dispose();
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
		if (checker.compare(this.getClass(), "Enter the branch length of the current branch", "[length]", commandName, "acceptLength")){
			if (t==null)
				return null;
			if (editorOn) {
				if (StringUtil.blank(arguments)){
					hideMiniEditor();
					return null;
				}

				if ("?".equalsIgnoreCase(arguments) || "unassigned".equalsIgnoreCase(arguments)) {
					if (t.getSelected(editorNode)) {
						changeLengthsSelected(t, t.getRoot(), MesquiteDouble.unassigned);
						t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
					}
					else
						t.setBranchLength(editorNode, MesquiteDouble.unassigned, true);
					MesquiteModule mb = ownerModule.findEmployerWithDuty(TreeWindowMaker.class);
					mb.doCommand("treeEdited", null, checker);
				}
				else {
					double d = MesquiteDouble.fromString(arguments);
					if (MesquiteDouble.isCombinable(d)) {
						if (t.getSelected(editorNode)) {
							changeLengthsSelected(t, t.getRoot(), d);
							t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
						}
						else {
							t.setBranchLength(editorNode, d, true);
						}
						MesquiteModule mb = ownerModule.findEmployerWithDuty(TreeWindowMaker.class);
						mb.doCommand("treeEdited", null, checker);
					}
				}
			}
			hideMiniEditor();
		}
		else if (checker.compare(this.getClass(), "Sets the branch length of the current branch", "[length]", commandName, "setLength")){
			if (t==null)
				return null;
			if (editorNode>=0) {
				if (StringUtil.blank(arguments))
					return null;
				if ("unassigned".equalsIgnoreCase(arguments)) {
					if (t.getSelected(editorNode)) {
						changeLengthsSelected(t, t.getRoot(), MesquiteDouble.unassigned);
						t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
					}
					else
						t.setBranchLength(editorNode, MesquiteDouble.unassigned, true);
					MesquiteModule mb = ownerModule.findEmployerWithDuty(TreeWindowMaker.class);
					mb.doCommand("treeEdited", null, checker);
				}
				else {
					double d = MesquiteDouble.fromString(arguments);
					if (MesquiteDouble.isCombinable(d)) {
						if (t.getSelected(editorNode)) {
							changeLengthsSelected(t, t.getRoot(), d);
							t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
						}
						else {
							t.setBranchLength(editorNode, d, true);
						}
						MesquiteModule mb = ownerModule.findEmployerWithDuty(TreeWindowMaker.class);
						mb.doCommand("treeEdited", null, checker);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Touch on branch to stretch it", "[branch number] [x coordinate touched] [y coordinate touched] [modifiers]", commandName, "touchedLengthsStretch")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);
			stretchTouched(node, x, y, mod);

		}
		else if (checker.compare(this.getClass(), "Touch on branch to change its length", "[branch number] [x coordinate touched] [y coordinate touched] [modifiers]", commandName, "touchedLengthsAdjust")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);


			if (!treeDisplay.showBranchLengths) {
				setMiniEditor(node, x,y);
				return null;
			}
			else if (!dragMode && (mod == null || (mod.indexOf("control")<=0 && mod.indexOf("command")<=0))) { //not dragMode, no modifiers
				setMiniEditor(node, x,y);
				return null;
			}
			else if (dragMode && !(mod == null || (mod.indexOf("control")<=0 && mod.indexOf("command")<=0))) { //dragMode, modifiers
				setMiniEditor(node, x,y);
				return null;
			}
			else if (editorOn && node != editorNode)
				return null;
			stretchTouched(node, x, y, mod);

		}
		else if (checker.compare(this.getClass(), "Drop branch whose length is being changed.", "[branch number] [x coordinate dropped] [y coordinate dropped] ", commandName, "droppedLengthsAdjust")) {
			if (t==null)
				return null;
			if (editorOn)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (lineOn) {
				stretchDropped(t, node, x, y);
			}
		}
		else if (checker.compare(this.getClass(),  "Drop branch being stretched.", "[branch number] [x coordinate dropped] [y coordinate dropped] ", commandName, "droppedLengthsStretch")) {
			if (t==null)
				return null;
			if (editorOn)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (lineOn) {
				stretchDropped(t, node, x, y);
			}
		}


		else if (checker.compare(this.getClass(),  "Drag branch whose length is being changed.", "[branch number] [current x coordinate] [current y coordinate] ", commandName, "draggedLengthsAdjust")) {
			if (t==null)
				return null;
			if (editorOn)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (lineOn) {
				stretchDragged(t, node, x, y);
			}
		}
		else if (checker.compare(this.getClass(), "Drag branch being stretched.", "[branch number] [current x coordinate] [current y coordinate] ", commandName, "draggedLengthsStretch")) {
			if (t==null)
				return null;
			if (editorOn)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			if (lineOn) {
				stretchDragged(t, node, x, y);
			}
		}
		return null;
	}
	public void turnOff() {

		selectModule.extras.removeElement(this);
		if (miniEditor != null)
			treeDisplay.removePanelPlease(miniEditor);
		super.turnOff();
	}
}




/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.coalesce.LineageWidth;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class LineageWidth extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "adjustWidths.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		addMenuItem("Set all lineage width scaling factors...", makeCommand("setAllWidths",  this));
		return true;
	} 
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public String getExpectedPath(){
		return getPath() + "adjustWidths.gif";
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		WidthsToolExtra newPj = new WidthsToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the scaling factor of lineage widths for all branches", "[width scale]", commandName, "setAllWidths")) {
			pos.setValue(0);
			double w = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(w)){
				//w = MesquiteDouble.queryDouble(containerOfModule(), "Set scaling factor of all lineages", "Set scaling factor for all lineages to", 1.0);
				MesquiteDouble dub = new MesquiteDouble(1.0);
				if (!QueryDialogs.queryDouble(containerOfModule(), "Set scaling factor of all lineages", "Set scaling factor for all lineages to", null, dub))
					return null;
				w = dub.getValue();
			}
			if (MesquiteDouble.isCombinable(w) || MesquiteDouble.isUnassigned(w)) {

				Enumeration e = extras.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					WidthsToolExtra tCO = (WidthsToolExtra)obj;
					tCO.setAllWidths(w);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Adjust scaling factor of lineage widths";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to adjust the scaling factor used for lineage widths in a tree window.";
	}
}

/* ======================================================================== */
class WidthsToolExtra extends TreeDisplayDrawnExtra implements Commandable  {
	public TreeTool colorTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	LineageWidth widthsModule;
	Tree tree;
	NameReference widthNameReference = NameReference.getNameReference("width");
	DoubleArray widths = null;
	MiniNumberEditor miniEditor;
	boolean editorOn = false;
	int editorNode = -1;
	public WidthsToolExtra (LineageWidth ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		widthsModule = ownerModule;
		colorTool = new TreeTool(this, "LineageWidth", ownerModule.getPath(), "adjustWidths.gif", 9,7,"Adjust scaling factor of lineage widths", "This tool adjusts the scaling factor of lineage widths of a branch.  This scaling is multiplied by the designated lineage width (e.g., effective population sizes) to determine the final lineage width for that branch.  When a branch is touched, a small text editing box appears in which a new branch width scaling factor can be entered; touching the little blue button enters the scaling factor.");
		colorTool.setTouchedCommand(MesquiteModule.makeCommand("setWidths",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(colorTool);
		}
	}
	/*....................................................................................................*/
	private void drawWidths (Tree tree, int node, Graphics g) {
		double w =widths.getValue(node);
		if (MesquiteDouble.isCombinable(w)) {
			double nodeX = treeDisplay.getTreeDrawing().x[node];
			double nodeY = treeDisplay.getTreeDrawing().y[node];

			GraphicsUtil.drawString(g,MesquiteDouble.toString(w), nodeX,nodeY);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawWidths(tree, d, g);
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		this.tree = tree;
		if (editorOn) {
			if (tree.nodeExists(editorNode))
				miniEditor.setLocation((int)treeDisplay.getTreeDrawing().x[editorNode],(int) treeDisplay.getTreeDrawing().y[editorNode]); // integer nodeloc approximation
			else hideMiniEditor();
		}
		if (widths!=null) {
			Color c = g.getColor();
			g.setColor(Color.green);
			drawWidths(tree, drawnRoot, g);
			if (c!=null) g.setColor(c);
		}
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
		if (tree != null)
			widths = tree.getWhichAssociatedDouble(widthNameReference);
	}

	/*.................................................................................................................*/
	private   void setLineageWidths(MesquiteTree tree, int node, double w) {
		tree.setAssociatedDouble(widthNameReference, node, w, true);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			setLineageWidths(tree, d, w);
	}
	/*.................................................................................................................*/
	public   void setAllWidths(double w) {
		if (tree instanceof MesquiteTree) {
			if (((MesquiteTree)tree).getWhichAssociatedDouble(widthNameReference)==null)
				((MesquiteTree)tree).makeAssociatedDoubles("width");
			setLineageWidths((MesquiteTree)tree, tree.getRoot(), w);
			((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		//remember which colored
		return null;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	private void setWidth(MesquiteTree tree, int node, double w){
		if (tree.getWhichAssociatedDouble(widthNameReference)==null)
			tree.makeAssociatedDoubles("width");
		tree.setAssociatedDouble(widthNameReference, node, w, true);
		tree.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
	/*.................................................................................................................*/
	private void setMiniEditor(int node, int x,int y){
		Tree t = treeDisplay.getTree();
		if (t==null)
			return;
		editorNode = node;
		if (miniEditor == null) {
			miniEditor = new MiniNumberEditor(ownerModule, ownerModule.makeCommand("acceptWidth", this));
			addPanelPlease(miniEditor);
		}
		miniEditor.setLocation((int)treeDisplay.getTreeDrawing().x[node], (int)treeDisplay.getTreeDrawing().y[node]); // integer nodeloc approximation
		miniEditor.setNumber(tree.getAssociatedDouble(widthNameReference, node));
		miniEditor.setVisible(true);
		editorOn = true;
	}
	private void hideMiniEditor(){
		miniEditor.setVisible(false);
		editorOn = false;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		Tree trt = treeDisplay.getTree();
		MesquiteTree t = null;
		if (trt instanceof MesquiteTree)
			t = (MesquiteTree)trt;

		if (checker.compare(this.getClass(), "Sets the lineage width scaling factor (i.e., multiplier of population size) of the branch in the tree", "[lineage width scaling factor]", commandName, "acceptWidth")){
			if (t==null)
				return null;
			if (editorOn) {
				double d = MesquiteDouble.fromString(arguments);
				if (MesquiteDouble.isCombinable(d) || d == MesquiteDouble.unassigned) {
					setWidth(t, editorNode, d);
					MesquiteModule mb = ownerModule.findEmployerWithDuty(TreeWindowMaker.class);
					mb.doCommand("treeEdited", null, checker);
				}
			}
			hideMiniEditor();
		}
		else if (checker.compare(this.getClass(), "Causes a mini text editor to appear in which lineage width scaling factor of the branch can be entered", "[branch number] [x coordinate touched] [y coordinate touched]", commandName, "setWidths")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);

			setMiniEditor(node, x,y);
		}
		return null;
	}
	public void turnOff() {
		widthsModule.extras.removeElement(this);
		widthNameReference=null;
		super.turnOff();
	}
}





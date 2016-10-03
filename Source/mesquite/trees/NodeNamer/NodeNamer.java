/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodeNamer;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NodeNamer extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "nodeNamer.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		NodeNamerExtra newPj = new NodeNamerExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	void sendTreeCommands(String command, String arguments){
		Enumeration e = extras.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			NodeNamerExtra tCO = (NodeNamerExtra)obj;
			tCO.doTreeCommand(command, arguments);
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node Namer";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool to name the nodes of a tree";
	}

}

/* ======================================================================== */
class NodeNamerExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool adjustTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	NodeNamer selectModule;
	Tree tree;
	int originalX, originalY, lastX, lastY;
	boolean lineOn = true;
	MiniStringEditor miniEditor;
	double lastBL;	
	boolean editorOn = false;
	int editorNode = -1;
	boolean dragMode = false; //default mode - if true,then drags branch to extend; needs modifier keys for editor; if false, opposite

	public NodeNamerExtra (NodeNamer ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		selectModule = ownerModule;
		adjustTool = new TreeTool(this,  "nodeNamer", ownerModule.getPath() , "nodeNamer.gif", 1,0,"Name nodes", "This tool can be used to give names to clades. When a branch is touched, a small text editing box appears in which a new name can be entered; touching the little blue button enters it." );
		adjustTool.setTouchedCommand(MesquiteModule.makeCommand("touchedNamer",  this));
		adjustTool.setTouchedTaxonCommand(MesquiteModule.makeCommand("touchedTaxonNamer",  this));
		//adjustTool.setDroppedCommand(MesquiteModule.makeCommand("droppedNamer",  this));
		//adjustTool.setDraggedCommand(MesquiteModule.makeCommand("draggedNamer",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(adjustTool);
		}
	}
	public void doTreeCommand(String command, String arguments){
		Tree tree = treeDisplay.getTree();
		if (tree instanceof Commandable && tree!=null)
			((Commandable)tree).doCommand(command, arguments, CommandChecker.defaultChecker);
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

	private void setMiniEditor(int node, int x,int y){
		Tree t = treeDisplay.getTree();
		if (t==null)
			return;
		editorNode = node;
		if (miniEditor == null) {
			miniEditor = new MiniStringEditor(ownerModule, ownerModule.makeCommand("acceptName", this));
			treeDisplay.addPanelPlease(miniEditor);
		}
		miniEditor.setLocation((int)treeDisplay.getTreeDrawing().x[node], (int)treeDisplay.getTreeDrawing().y[node]);
		String lab = t.getNodeLabel(node);
		if (lab == null)
			lab = "";
		miniEditor.setText(lab);
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

		if (checker.compare(this.getClass(), "Assigns the given name to the current node", "[name of node]", commandName, "acceptName")){
			if (t==null)
				return null;
			if (editorOn) {
				if (t.nodeIsTerminal(editorNode)){
					if (arguments!=null) {

						int n = t.nodeOfLabel(arguments, true);
						if (n<0) {
							n = t.getTaxa().whichTaxonNumber(arguments, false);
							n = t.nodeOfTaxonNumber(n);
						}
						if (n!=editorNode && t.nodeExists(n)) // node with that label already exists
							if (!AlertDialog.query(ownerModule.containerOfModule(), "Name already exists", "That name already exists for some other node or terminal taxon.  Assigning the label to this terminal taxon may cause the tree to be misinterpreted if later re-read from a file.  Continue?"))
								return null;

						Taxa taxa = t.getTaxa();
						int it = t.taxonNumberOfNode(editorNode);
						if (it>=0) {
							taxa.setTaxonName(it, arguments);
							taxa.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
						}
					}
				}
				else {
					String lab = t.getNodeLabel(editorNode);
					if (arguments == null	 && lab == null) { //both null
						hideMiniEditor();
						return null;
					}
					if (arguments != null && arguments.equals(lab)) { //same as before
						hideMiniEditor();
						return null;
					}
					if (!MesquiteTree.cosmeticInternalNames){
						int n = t.nodeOfLabel(arguments, true);
						if (n<0) {
							n = t.getTaxa().whichTaxonNumber(arguments, false);
							n = t.nodeOfTaxonNumber(n);
						}
						if (n!=editorNode && t.nodeExists(n)) // node with that label already exists
							if (!AlertDialog.query(ownerModule.containerOfModule(), "Name already exists", "That name already exists for some other node or terminal taxon.  Assigning the label to this node may cause the tree to be interpreted as reticulate if later re-read from a file.  Continue?"))
								return null;
					}
					t.setNodeLabel(arguments, editorNode);
					if (arguments != null){
						Clade c = t.getTaxa().getClades().findClade(arguments);
						if (c==null)
							t.getTaxa().getClades().addClade(arguments);
					}
					t.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
					treeDisplay.pleaseUpdate(false);
				}
			}
			hideMiniEditor();
		}
		else if (checker.compare(this.getClass(), "Indicates the node name tool touched on a branch", "[branch number] [x coordinate] [y coordinate] [modifiers]", commandName, "touchedNamer")) {
			if (t==null)
				return null;
			if (miniEditor!=null)
				miniEditor.setText("");
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);

			if (t.nodeExists(node)){
				ownerModule.logln("Node " + node + " touched on to rename.");
				setMiniEditor(node, x,y);
			}
			else 
				ownerModule.logln("Node " + node + " touched on to rename, but node not in tree.");

		}
		else if (checker.compare(this.getClass(), "Indicates the node name tool touched on a terminal taxon name", "[taxon] [x coordinate] [y coordinate] [modifiers]", commandName, "touchedTaxonNamer")) {
			if (t==null)
				return null;
			if (miniEditor!=null)
				miniEditor.setText("");

			MesquiteInteger io = new MesquiteInteger(0);
			int taxon= MesquiteInteger.fromString(arguments, io);
			int node = t.nodeOfTaxonNumber(taxon);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);

			if (t.nodeExists(node)){
				ownerModule.logln("Node " + node + " touched on to rename.");
				setMiniEditor(node, x,y);
			}
			else 
				ownerModule.logln("Node " + node + " touched on to rename, but node not in tree.");
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




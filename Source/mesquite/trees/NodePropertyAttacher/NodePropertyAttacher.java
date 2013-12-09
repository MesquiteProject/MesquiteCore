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
package mesquite.trees.NodePropertyAttacher;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NodePropertyAttacher extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "NodePropertyAttacher.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
        setUseMenubar(false); //menu available by touching button
        return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		NodePropertyAttacherExtra newPj = new NodePropertyAttacherExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node Property Attacher";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool to annotate the nodes of a tree with properties";
	}

}

/* ======================================================================== */
class NodePropertyAttacherExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool adjustTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	NodePropertyAttacher selectModule;
	Tree tree;
	MiniStringEditor miniEditor;
	boolean editorOn = false;
	int editorNode = -1;
    MesquiteMenuItemSpec pushLabelsToPropertiesMenuItem,assignLabelsToPropertyMenuItem;
    String[] pvs;

	public NodePropertyAttacherExtra (NodePropertyAttacher ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		selectModule = ownerModule;
		adjustTool = new TreeTool(this,  "NodePropertyAttacher", ownerModule.getPath() , "NodePropertyAttacher.gif", 1,0,"Assign node properties", "This tool can be used to give names to clades. When a branch is touched, a small text editing box appears in which a new name can be entered; touching the little blue button enters it." );
		adjustTool.setTouchedCommand(MesquiteModule.makeCommand("touchedNamer",  this));
		adjustTool.setTouchedTaxonCommand(MesquiteModule.makeCommand("touchedTaxonNamer",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			ownerModule.containerOfModule().addTool(adjustTool);
            adjustTool.setPopUpOwner(ownerModule);
        }
        assignLabelsToPropertyMenuItem = ownerModule.addMenuItem( "Store values as property...", ownerModule.makeCommand("assignLabelsToProperty",  this));
        pushLabelsToPropertiesMenuItem = ownerModule.addMenuItem( "Make property values from current node labels", ownerModule.makeCommand("pushLabelsToProperty",  this));
        pvs = new String[treeDisplay.getTree().getNumNodeSpaces()];

    }
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
        setTree(tree);
		if (editorOn) {
			if (tree.nodeExists(editorNode))
				miniEditor.setLocation(treeDisplay.getTreeDrawing().x[editorNode], treeDisplay.getTreeDrawing().y[editorNode]);
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
        if (tree.getNumNodeSpaces() != pvs.length) {
            pvs = new String[tree.getNumNodeSpaces()];
        }
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
		miniEditor.setLocation(treeDisplay.getTreeDrawing().x[node], treeDisplay.getTreeDrawing().y[node]);
		String lab = getNodePV(node);
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
		MesquiteTree t = null;
		if (tree instanceof MesquiteTree)
			t = (MesquiteTree) tree;

        if (checker.compare(this.getClass(), "Edits a property value for the node ", "[name of node]", commandName, "acceptName")){
            if (t==null)
                return null;
            if (editorOn) {
                String lab = getNodePV(editorNode);
                if (arguments == null	 && lab == null) { //both null
                    hideMiniEditor();
                    return null;
                }
                if (!MesquiteTree.cosmeticInternalNames){
                    int n = t.nodeOfLabel(arguments, true);
                    if (n<0) {
                        n = t.getTaxa().whichTaxonNumber(arguments, false);
                        n = t.nodeOfTaxonNumber(n);
                    }
                }
                setNodePV(arguments, editorNode);
                if (arguments != null){
                    Clade c = t.getTaxa().getClades().findClade(arguments);
                    if (c==null)
                        t.getTaxa().getClades().addClade(arguments);
                }
                t.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
                treeDisplay.pleaseUpdate(false);
            }
            hideMiniEditor();
        }
        else if (checker.compare(this.getClass(), "Sets current node labels as values of a property ", "[property name]", commandName, "pushLabelsToProperty")){
            if (t!=null) {
                Vector nodes = t.nodesInClade(t.getRoot());
                System.out.println("there are "+nodes.size()+" nodes");

                for (int i=0;i<nodes.size();i++) {
                    int node = ((Integer) nodes.get(i)).intValue();
                    System.out.println("looking at node "+node);
                    String propVal = null;
                    if (t.nodeIsInternal(node)) {
                        propVal = t.getNodeLabel(node);
                    }
                    if (propVal != null) {
                        setNodePV(propVal,node);
                        t.setNodeLabel("",node);
                    }
                }
            }
        }
        else if (checker.compare(this.getClass(), "Sets current values for property ", "[property name]", commandName, "assignLabelsToProperty")){
            String s = ParseUtil.getFirstToken(arguments, new MesquiteInteger(0));
            if (StringUtil.blank(s))
                s = MesquiteString.queryString(this.getOwnerModule().containerOfModule(), "Set Property Name" , "Property name: ", "");
            if (s!=null) {
                if (t!=null) {
                    Vector nodes = t.nodesInClade(t.getRoot());
                    System.out.println("there are "+nodes.size()+" nodes");
                    for (int i=0;i<nodes.size();i++) {
                        int node = ((Integer) nodes.get(i)).intValue();
                        System.out.println("looking at node "+node);
                        String propVal = getNodePV(node);
                        if (propVal != null) {
                            System.out.println("setting property "+s+" with val "+propVal+" for node "+node);
                            t.setStringPropertyOnNode(s,propVal,node);
                        }
                    }
                }
            }
        }
        else if (checker.compare(this.getClass(), "Indicates the node name tool touched on a branch", "[branch number] [x coordinate] [y coordinate] [modifiers]", commandName, "touchedNamer")) {
			if (t==null)
				return null;
			if (miniEditor!=null) {

				miniEditor.setText("");
            }
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

    private void setNodePV (String newPV, int node) {
        if (newPV == null) {
            return;
        }
        if (pvs.length > node) {
            pvs[node] = newPV;
        }
    }

    private String getNodePV (int node) {
        if (pvs.length > node) {
            return pvs[node];
        }
        return null;
    }
}



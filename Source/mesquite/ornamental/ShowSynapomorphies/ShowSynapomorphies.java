/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.ShowSynapomorphies;
/*~~  */

import java.awt.Color;

import java.awt.Graphics;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.NameReference;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.duties.TreeDisplayAssistantI;
import mesquite.lib.duties.TreeDisplayAsstShowToggleable;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayDrawnExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.ui.BarDecoration;

/* ======================================================================== */
public class ShowSynapomorphies extends TreeDisplayAssistantI implements TreeDisplayAsstShowToggleable {
	ShowSynapExtra spots;
	boolean show = true;
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	public boolean getUserChooseable(){
		return false;  //this module is treated as defunct, now that the tree draw coordiantor does it directly, but is retained for compatibility with old scripts
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		spots = new ShowSynapExtra(this, treeDisplay, 0); //TODO: should remember all of these
		return spots;
	}
	//Sets whether to show or hide the extras
	public void toggleShowExtras(){
		show = !show;
		if (spots != null)
			spots.treeDisplay.pleaseUpdate();
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (spots != null)
			temp.addLine("show " + show); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows spots", null, commandName, "show")) {
			show = MesquiteBoolean.fromTrueFalseString(arguments);
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Show Synapomorphies";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Stored Synapomorphies";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows synapomorphies stored as annotations to nodes of a tree." ;
	}
	public void endJob(){
		if (spots !=null)
			spots.turnOff(); //should do all
		super.endJob();
	}
}

/* ======================================================================== */
class ShowSynapExtra extends TreeDisplayDrawnExtra {
	ShowSynapomorphies nnModule;
	NameReference synRef = NameReference.getNameReference("synapomorphies");

	public ShowSynapExtra (ShowSynapomorphies ownerModule, TreeDisplay treeDisplay, int numTaxa) {
		super(ownerModule, treeDisplay);
		nnModule = ownerModule;
	}
	StringArray synapomorphiesAtNode(Tree tree, int node){
		Object syns = tree.getAssociatedObject(synRef, node);
		if (syns != null && syns instanceof StringArray)
			return (StringArray)syns;
		return null;
	}



	/*_________________________________________________*/
	private   void drawSynapomorphies(TreeDisplay treeDisplay, Tree tree, Graphics g, int N) {
		if (tree.withinCollapsedClade(N))
			return;
		if (tree.nodeExists(N)) {
			StringArray syns = synapomorphiesAtNode(tree, N);
			if (syns!= null && syns.getSize()>0){
				Vector bars = new Vector();
				for (int i=0; i<syns.getSize(); i++)
					bars.addElement(new BarDecoration(N, Color.blue, Color.blue, syns.getValue(i), treeDisplay.getFont(), Color.blue));

				treeDisplay.drawBarDecorations(g,  bars,N, false, 20, true, treeDisplay.getEdgeWidth());
			}
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawSynapomorphies(treeDisplay, tree, g, d);
		}
	}

	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (nnModule.show && MesquiteTree.OK(tree))
			drawSynapomorphies(treeDisplay, tree, g, drawnRoot);
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	public   void setTree(Tree tree) {
	}
}




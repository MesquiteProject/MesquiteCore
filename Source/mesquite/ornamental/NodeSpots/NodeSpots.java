/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.NodeSpots;
/*~~  */

import java.util.*;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayDrawnExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class NodeSpots extends TreeDisplayAssistantDI {
	SpotsDrawing spots;
	MesquiteBoolean showSpots = new MesquiteBoolean(true); 

	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		MesquiteSubmenuSpec mss = addSubmenu(null, "Spots to Emphasize Nodes");
		addCheckMenuItemToSubmenu(null, mss, "Show Spots", makeCommand("showSpots", this), showSpots);
		addItemToSubmenu(null, mss, "Spot Size...", makeCommand("spotSize", this));
		addItemToSubmenu(null, mss, "Remove All Spots", makeCommand("removeSpots", this));

		return true;
	}
	int spotSize = 12;
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		spots = new SpotsDrawing(this, treeDisplay, 0); //TODO: should remember all of these
		return spots;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("spotSize " + spotSize); 
		temp.addLine("showSpots " + showSpots.toOffOnString());
		return temp;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Removes spots", null, commandName, "removeSpots")) {
			spots.removeAllSpots();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show or hide the spots", "[on or off]", commandName, "showSpots")) {
			showSpots.toggleValue(parser.getFirstToken(arguments));
			if (spots != null)
				spots.getTreeDisplay().repaint();
		}
		else if (checker.compare(this.getClass(), "Sets spot size", null, commandName, "spotSize")) {
			int ic = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(ic)){
				spotSize = ic;
			}
			else if (!MesquiteThread.isScripting()){
				ic = MesquiteInteger.queryInteger(containerOfModule(), "Spot Size", "Size of spots for emphasis", (int)spotSize, 1, 40);
				if (MesquiteInteger.isCombinable(ic)){
					spotSize = ic;
					if (spots != null)
						spots.getTreeDisplay().repaint();
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Add Spots";
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Adds spots to nodes on a tree." ;
	}
	public void endJob(){
		if (spots !=null)
			spots.turnOff(); //should do all
		super.endJob();
	}
}

/* ======================================================================== */
class SpotsDrawing extends TreeDisplayDrawnExtra implements Commandable {
	public TreeTool spotTool;
	NodeSpots nsModule;
	public SpotsDrawing (NodeSpots ownerModule, TreeDisplay treeDisplay, int numTaxa) {
		super(ownerModule, treeDisplay);
		nsModule = ownerModule;
		spotTool = new TreeTool(this, "AddSpots", ownerModule.getPath(), "pointer.gif", 1,1,"Add/Delete Spot at Node", "This tool adds a spot at a node of a tree.  This has cosmetic effect only. It can be used for emphasis. ");
		spotTool.setTouchedCommand(new MesquiteCommand("toggleSpot",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(spotTool);
		}
	}
	NameReference emphasisNR = NameReference.getNameReference("emphasizeNode");
	double offsetX = 0; double offsetY = 0;
	/*_________________________________________________*/
	private   void drawSpot(TreeDisplay treeDisplay, MesquiteTree tree, Graphics g, int N) {
		if (tree.withinCollapsedClade(N))
			return;
		if (tree.nodeExists(N)) {
			boolean emphasized = tree.getAssociatedBit(emphasisNR, N);
			if (emphasized){
				double x = treeDisplay.getTreeDrawing().x[N] + offsetX;
				double y = treeDisplay.getTreeDrawing().y[N] + offsetY;
				int spotSize = nsModule.spotSize;
				if (tree.isSelected(N) || !tree.anySelected())
					g.setColor(treeDisplay.branchColor);
				else 
					g.setColor(treeDisplay.branchColorDimmed);
				GraphicsUtil.fillOval(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize);
			}
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawSpot(treeDisplay, tree, g, d);
		}
	}
	/*_________________________________________________*/
	public   void drawSpots(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Graphics g) {
		double edgeWidth = treeDisplay.getTreeDrawing().getEdgeWidth();
		if (treeDisplay.isUp()){
			offsetX = edgeWidth/2;
			offsetY = edgeWidth/2;
		}
		else if (treeDisplay.isDown()){
			offsetX = edgeWidth/2;
			offsetY = -edgeWidth/2;
		}
		else if (treeDisplay.isRight()){
			offsetY = edgeWidth/2;
			offsetX = -edgeWidth/2;
		}
		else if (treeDisplay.isLeft()){
			offsetY = edgeWidth/2;
		}
		if (MesquiteTree.OK(tree)) {
			drawSpot(treeDisplay, (MesquiteTree)tree, g, drawnRoot);  
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (nsModule.showSpots.getValue())
			drawSpots(treeDisplay, tree, drawnRoot, g);
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	public   void setTree(Tree tree) {
	}
	public   void removeAllSpots() {
		MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
		tree.removeAssociatedBits(emphasisNR);
		tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
	}

	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Add or delete a spot on a branch", "[branch number][x coordinate touched][y coordinate touch][modifiers]", commandName, "toggleSpot")) {
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
			if (tree != null && tree.nodeExists(branchFound)){
				boolean emphasized = tree.getAssociatedBit(emphasisNR, branchFound);
				tree.setAssociatedBit(emphasisNR, branchFound, !emphasized);
				tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
				//treeDisplay.repaint();
			}
		}
		return null;
	}
}




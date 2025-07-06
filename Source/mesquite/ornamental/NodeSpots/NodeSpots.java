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
import mesquite.lib.ui.MesquiteCursor;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class NodeSpots extends TreeDisplayAssistantI {
	NodeSpotsDrawing spots;
	MesquiteBoolean showSpots = new MesquiteBoolean(true); 
	ListableVector spotTypes = new ListableVector();
	long spotType = 0;
	int spotSize = 12;

	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		setUseMenubar(false); //menu available by touching on button
		addCheckMenuItem(null, "Show Spots", makeCommand("showSpots", this), showSpots);
		MesquiteSubmenuSpec mss = addSubmenu(null, "Spot Type");
		spotTypes.addElement(new MesquiteInteger("Filled Circle (Spot)", 0), false);
		spotTypes.addElement(new MesquiteInteger("Open Circle", 1), false);
		spotTypes.addElement(new MesquiteInteger("Filled Square", 2), false);
		spotTypes.addElement(new MesquiteInteger("Open Square", 3), false);
		mss.setList(spotTypes);
		mss.setCommand(makeCommand("spotType",  this));
		addMenuItem("Spot Size...", makeCommand("spotSize", this));
		addMenuItem("Remove All Spots", makeCommand("removeSpots", this));

		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		spots = new NodeSpotsDrawing(this, treeDisplay, 0); //TODO: should remember all of these
		return spots;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("spotSize " + spotSize); 
		temp.addLine("spotType " + spotType);
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
		else if (checker.compare(this.getClass(), "Sets spot size", null, commandName, "spotType")) {
			int ic = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(ic)){
				spotType = ic;
				if (spots != null)
					spots.setSpotType(spotType);
			}
		}
	else if (checker.compare(this.getClass(), "Sets spot size", null, commandName, "spotSize")) {
			int ic = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(ic)){
				spotSize = ic;
				if (spots != null)
					spots.setSpotSize(spotSize);
		}
			else if (!MesquiteThread.isScripting()){
				ic = MesquiteInteger.queryInteger(containerOfModule(), "Spot Size", "Size of spots for emphasis", (int)spotSize, 1, 40);
				if (MesquiteInteger.isCombinable(ic)){
					spotSize = ic;
					if (spots != null)
						spots.setSpotSize(spotSize);
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
class NodeSpotsDrawing extends TreeDisplayDrawnExtra implements Commandable {
	TreeTool spotTool;
	NodeSpots nsModule;
	long currentSpotType = 0;
	long currentSpotSize = 12;
	MesquiteCursor[] cursors;
	public NodeSpotsDrawing (NodeSpots ownerModule, TreeDisplay treeDisplay, int numTaxa) {
		super(ownerModule, treeDisplay);
		nsModule = ownerModule;
		cursors = new MesquiteCursor[ownerModule.spotTypes.size()];
		for (int i=0; i<cursors.length; i++)
			cursors[i] =  new MesquiteCursor(this, "spot" + i, ownerModule.getPath(), "spot" + i + ".gif", 8, 4);
		
		spotTool = new TreeTool(this, "AddSpots", ownerModule.getPath(), "spot0.gif", 8,4 ,"Add/Delete Spot at Node", "This tool adds a spot at a node of a tree.  This has cosmetic effect only. It can be used for emphasis. ");
		currentSpotType = ownerModule.spotType;
		setSpotType(currentSpotType);
		spotTool.setTouchedCommand(new MesquiteCommand("toggleSpot",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(spotTool);
			spotTool.setPopUpOwner(ownerModule);
		}
	}
	
	void setSpotType(long t){
		spotTool.setCurrentStandardCursor(cursors[(int)t]);
		currentSpotType = t;
	}
	void setSpotSize(long t){
		currentSpotSize = t;
	}
	NameReference spotTypeNR = NameReference.getNameReference("spotType");
	NameReference spotSizeNR = NameReference.getNameReference("spotSize");
	double offsetX = 0; double offsetY = 0;
	/*_________________________________________________*/
	private   void drawSpot(TreeDisplay treeDisplay, MesquiteTree tree, Graphics g, int N) {
		if (tree.withinCollapsedClade(N))
			return;
		if (tree.nodeExists(N)) {
			long spotStored = tree.getAssociatedLong(spotTypeNR, N);
			long spotSizeStored = tree.getAssociatedLong(spotSizeNR, N);
			
			if (MesquiteLong.isCombinable(spotStored)){
				double x = treeDisplay.getTreeDrawing().x[N] + offsetX;
				double y = treeDisplay.getTreeDrawing().y[N] + offsetY;
				double spotSize = nsModule.spotSize;
				if (MesquiteLong.isCombinable(spotSizeStored))
					spotSize = (int)spotSizeStored;
				Color dark = treeDisplay.branchColorDimmed;
				if (tree.isSelected(N) || !tree.anySelected())
					dark = treeDisplay.branchColor;
				if (spotStored == 0){ //Filled circle
					g.setColor(dark);
					GraphicsUtil.fillOval(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize);
					GraphicsUtil.drawOval(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize, (float)1.5);
				}
				else if (spotStored == 1){ //Open circle
					g.setColor(Color.white);
					GraphicsUtil.fillOval(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize);
					g.setColor(dark);
					GraphicsUtil.drawOval(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize, (float)1.5);
				}
				else if (spotStored == 2){ //Filled square
					g.setColor(dark);
					spotSize =  (spotSize *0.92); //so squares aren't much larger area
					GraphicsUtil.fillRect(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize);
					GraphicsUtil.drawRect(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize, (float)1.6);
				}
				else if (spotStored == 3){ //Open square
					g.setColor(Color.white);
					spotSize =  (spotSize *0.92);//so squares aren't much larger area
					GraphicsUtil.fillRect(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize);
					g.setColor(dark);
					GraphicsUtil.drawRect(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize, (float)1.6);
				}
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
		tree.removeAssociatedLongs(spotTypeNR);
		tree.removeAssociatedLongs(spotSizeNR);
		tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
	}

	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Add or delete a spot on a branch", "[branch number][x coordinate touched][y coordinate touch][modifiers]", commandName, "toggleSpot")) {
			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
			MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
			if (tree != null && tree.nodeExists(branchFound)){
				long spotThere = tree.getAssociatedLong(spotTypeNR, branchFound);
				long spotSizeThere = tree.getAssociatedLong(spotSizeNR, branchFound);
				if (MesquiteLong.isCombinable(spotThere) && spotThere == currentSpotType && spotSizeThere == currentSpotSize){
					tree.setAssociatedLong(spotTypeNR, branchFound, MesquiteLong.unassigned);
					tree.setAssociatedLong(spotSizeNR, branchFound, MesquiteLong.unassigned);
				}
				else {
					tree.setAssociatedLong(spotTypeNR, branchFound, currentSpotType);
					tree.setAssociatedLong(spotSizeNR, branchFound, currentSpotSize);
				}

					
				tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
				//treeDisplay.repaint();
			}
		}
		return null;
	}
}




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
import mesquite.lib.ui.ColorDistribution;
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
	long spotType = 0; //These are module copies, but most of the action is in the TreeDisplayExtra below
	int spotSize = 8;
	static String black = "#000000";
	static String white = "#ffffff";
	String spotColor = black;
	boolean spotSizeSet = false;
	int offset = 0;

	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		setUseMenubar(false); //menu available by touching on button
		addCheckMenuItem(null, "Show Spots", makeCommand("showSpots", this), showSpots);
		addMenuSeparator();
		MesquiteSubmenuSpec mss = addSubmenu(null, "Shape of New Spots");
		spotTypes.addElement(new MesquiteInteger("Circle", 0), false);
		spotTypes.addElement(new MesquiteInteger("Square", 1), false);
		mss.setList(spotTypes);
		mss.setCommand(makeCommand("spotType",  this));
		mss = addSubmenu(null, "Color of New Spots");
		addItemToSubmenu(null, mss, "Black", new MesquiteCommand("spotColor", StringUtil.tokenize(black), this));
		addItemToSubmenu(null, mss, "White", new MesquiteCommand("spotColor", StringUtil.tokenize(white), this));
		addItemToSubmenu(null, mss, "Enter Hex Value of Color...", new MesquiteCommand("spotColor", this));
		addMenuItem("Size of New Spots...", makeCommand("spotSize", this));
		addMenuSeparator();
		addMenuItem("Set Size All Spots...", makeCommand("spotSizeAll", this));
		addMenuItem("Offset from Nodes...", makeCommand("offset", this));
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
		spots = new NodeSpotsDrawing(this, treeDisplay, 0); //TODO: should remember all of these or restrict to one
		return spots;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("showSpots " + showSpots.toOffOnString());
		temp.addLine("spotSize " + spotSize); 
		temp.addLine("spotSizeSet " + spotSizeSet); 
		temp.addLine("spotType " + spotType);
		temp.addLine("spotColor " + StringUtil.tokenize(spotColor));
		temp.addLine("offset " + offset);
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
		else if (checker.compare(this.getClass(), "Sets whether to spot size has been set", "[true or false]", commandName, "spotSizeSet")) {
			spotSizeSet = MesquiteBoolean.fromTrueFalseString(arguments);
		}
		else if (checker.compare(this.getClass(), "Sets current spot color", "[hex color, as in #0000ff for blue]", commandName, "spotColor")) {
			String temp = parser.getFirstToken(arguments);
			if (StringUtil.blank(temp) && !MesquiteThread.isScripting()){
				temp = MesquiteString.queryString(containerOfModule(), "Hex value of color", "Set current color as a hex string (e.g., red would be #ff0000):", spotColor);
			}
			Color c = ColorDistribution.colorFromHex(temp);
			if (c!= null)
				spotColor = temp;
			if (spots != null){
				spots.setSpotColor(spotColor);
			}
		}
		else if (checker.compare(this.getClass(), "Sets spot type", null, commandName, "spotType")) {
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
				ic = MesquiteInteger.queryInteger(containerOfModule(), "Spot Size", "Size of new spots", (int)spotSize, 1, 40);
				if (MesquiteInteger.isCombinable(ic)){
					spotSize = ic;
					if (spots != null)
						spots.setSpotSize(spotSize);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets spot size for all spots", null, commandName, "spotSizeAll")) {
			if (!MesquiteThread.isScripting()){
				int ic = MesquiteInteger.queryInteger(containerOfModule(), "Spot Size", "Size of all current spots", (int)spotSize, 1, 40);
				if (MesquiteInteger.isCombinable(ic)){
					if (spots != null)
						spots.setSpotSizeAll(ic);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets offset", null, commandName, "offset")) {
			int ic = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(ic)){
				offset = ic;
				if (spots != null)
					spots.setOffset(offset);
			}
			else if (!MesquiteThread.isScripting()){
				ic = MesquiteInteger.queryInteger(containerOfModule(), "Offset from node", "How far away is the spot from the node (0 = on the node)", offset, -200, 200);
				if (MesquiteInteger.isCombinable(ic)){
					offset = ic;
					if (spots != null)
						spots.setOffset(offset);
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
		return 401;  
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
	int currentOffset = 0;
	String currentColorHex = NodeSpots.black;
	Color currentColor = Color.black;
	MesquiteTree myTree = null;
	MesquiteCursor[] cursors;
	public NodeSpotsDrawing (NodeSpots ownerModule, TreeDisplay treeDisplay, int numTaxa) {
		super(ownerModule, treeDisplay);
		nsModule = ownerModule;
		cursors = new MesquiteCursor[ownerModule.spotTypes.size()];
		for (int i=0; i<cursors.length; i++)
			cursors[i] =  new MesquiteCursor(this, "spot" + i, ownerModule.getPath(), "spot" + i + ".gif", 8, 4);

		spotTool = new TreeTool(this, "AddSpots", ownerModule.getPath(), "spot0.gif", 8,4 ,"Add/Delete Spot at Node", "This tool adds a spot at a node of a tree.  This has cosmetic effect only. It can be used for emphasis. ");
		setSpotType(ownerModule.spotType);
		setSpotColor(ownerModule.spotColor);
		setSpotSize(ownerModule.spotSize);
		nsModule.spotSizeSet = false;
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
	private   void spotSizeAll(MesquiteTree tree, int N, long size) {
		long spotSizeStored = tree.getAssociatedLong(spotSizeNR, N);
		if (MesquiteLong.isCombinable(spotSizeStored))
			 tree.setAssociatedLong(spotSizeNR, N, size);
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				spotSizeAll(tree, d, size);
	}
	
	void setSpotSizeAll(long t){
		setSpotSize(t);
		if (myTree != null)
			spotSizeAll(myTree, myTree.getRoot(), t);
		treeDisplay.repaint();
		
	}
	void setSpotSize(long t){
		nsModule.spotSizeSet = true;
		currentSpotSize = t;
	}
	void setOffset(int t){
		currentOffset = t;
		treeDisplay.repaint();
	}
	void setSpotColor(String t){
		currentColorHex = t;
		currentColor = ColorDistribution.colorFromHex(t);
		
	}
	NameReference spotTypeNR = NameReference.getNameReference("spotType");
	NameReference spotSizeNR = NameReference.getNameReference("spotSize");
	NameReference spotColorNR = NameReference.getNameReference("spotColor");

	double centeringOffsetX = 0; double centeringOffsetY = 0;  //These are the offsets needed to center the spot over the branch, considering branch width and tree orientation
	/*_________________________________________________*/
	private void drawSpot(TreeDisplay treeDisplay, MesquiteTree tree, Graphics g, int N) {
		if (tree.withinCollapsedClade(N))
			return;
		if (tree.nodeExists(N)) {
			long spotStored = tree.getAssociatedLong(spotTypeNR, N);
			long spotSizeStored = tree.getAssociatedLong(spotSizeNR, N);
			String spotColorHexStored = tree.getAssociatedString(spotColorNR, N);
			Color dark = treeDisplay.branchColorDimmed;
			Color spotColorStored = ColorDistribution.colorFromHex(spotColorHexStored);
			if (spotColorStored == null)
				spotColorStored = dark;

			if (MesquiteLong.isCombinable(spotStored)){
				double x = treeDisplay.getTreeDrawing().x[N] + centeringOffsetX;
				double y = treeDisplay.getTreeDrawing().y[N] + centeringOffsetY;

				if (currentOffset!=0){
					double bx = treeDisplay.getTreeDrawing().lineBaseX[N];
					double by = treeDisplay.getTreeDrawing().lineBaseY[N];
					double tx = treeDisplay.getTreeDrawing().lineTipX[N];
					double ty = treeDisplay.getTreeDrawing().lineTipY[N];
					if (bx == tx) { //Vertical
						if (currentOffset>0){
							if (ty>by)
								y -= currentOffset;
							else
								y +=currentOffset;
						}
						else if (ty<by)
							y += currentOffset;
						else
							y -=currentOffset;
					}
					else {
						double slope = (by-ty)/(bx-tx);
						double dx = Math.sqrt((currentOffset*currentOffset)/(1+slope*slope));
						double dy = Math.abs(slope*dx);
						if (currentOffset<0) { //should go beyond node
							if (tx<bx)
								dx = -dx;						
						}
						else if (tx>bx)
							dx = -dx;
						if (currentOffset<0) {
							if (ty<by)
								dy = -dy;						
						}
						else if (ty>by)
							dy = -dy;
						x += dx;
						y += dy;
					}
				}
				/*To go away from the node we follow the node's line. The two end points of the line are:
				treeDisplay.getTreeDrawing().lineTipX[N], lineTipY[N]  //this is basically the x,y of the node
				treeDisplay.getTreeDrawing().lineBaseX[N], lineBaseY[N]  //you can think of this as x,y for the ancestor, but it is only if diagonal
				 * */
				double spotSize = nsModule.spotSize;
				if (MesquiteLong.isCombinable(spotSizeStored))
					spotSize = (int)spotSizeStored;
				if (tree.isSelected(N) || !tree.anySelected())
					dark = treeDisplay.branchColor;
				if (spotStored == 0){ //circle
					g.setColor(spotColorStored);
					GraphicsUtil.fillOval(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize);
					g.setColor(dark);
					GraphicsUtil.drawOval(g, x-spotSize/2, y-spotSize/2, spotSize,spotSize, (float)1.5);
				}
				else if (spotStored == 1){ //Square
					g.setColor(spotColorStored);
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
		if (!nsModule.spotSizeSet && currentSpotSize< (int)(edgeWidth+4)){
			nsModule.spotSize = (int)(edgeWidth+4);
			setSpotSize(nsModule.spotSize);
		}
		if (treeDisplay.isUp()){
			centeringOffsetX = edgeWidth/2;
			centeringOffsetY = edgeWidth/2;
		}
		else if (treeDisplay.isDown()){
			centeringOffsetX = edgeWidth/2;
			centeringOffsetY = -edgeWidth/2;
		}
		else if (treeDisplay.isRight()){
			centeringOffsetY = edgeWidth/2;
			centeringOffsetX = -edgeWidth/2;
		}
		else if (treeDisplay.isLeft()){
			centeringOffsetY = edgeWidth/2;
		}
		if (MesquiteTree.OK(tree)) {
			drawSpot(treeDisplay, (MesquiteTree)tree, g, drawnRoot);  
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		myTree = (MesquiteTree)tree;
		if (nsModule.showSpots.getValue()){
			drawSpots(treeDisplay, tree, drawnRoot, g);
		}
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		myTree = (MesquiteTree)tree;
		drawOnTree(tree, drawnRoot, g);
	}
	public   void setTree(Tree tree) {
		myTree = (MesquiteTree)tree;
	}
	public   void removeAllSpots() {
		MesquiteTree tree = (MesquiteTree)treeDisplay.getTree();
		tree.removeAssociatedLongs(spotTypeNR);
		tree.removeAssociatedLongs(spotSizeNR);
		tree.removeAssociatedStrings(spotColorNR);
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
				String spotColorThere = tree.getAssociatedString(spotColorNR, branchFound);
				if (MesquiteLong.isCombinable(spotThere) && spotThere == currentSpotType && spotSizeThere == currentSpotSize && (spotColorThere!= null && spotColorThere.equalsIgnoreCase( currentColorHex))){
					tree.setAssociatedLong(spotTypeNR, branchFound, MesquiteLong.unassigned);
					tree.setAssociatedLong(spotSizeNR, branchFound, MesquiteLong.unassigned);
					tree.setAssociatedString(spotColorNR, branchFound, null);
				}
				else {
					tree.setAssociatedLong(spotTypeNR, branchFound, currentSpotType);
					tree.setAssociatedLong(spotSizeNR, branchFound, currentSpotSize);
					tree.setAssociatedString(spotColorNR, branchFound, currentColorHex);
				}


				tree.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
			}
		}
		return null;
	}
}




/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ShowOtherTreeInTreeWindow;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayBkgdExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayRequests;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.tree.TreeUtil;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquiteCMenuItemSpec;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MiniScroll;
import mesquite.lib.ui.MousePanel;
import mesquite.trees.lib.TaxonPolygon;

/* ======================================================================== */
public class ShowOtherTreeInTreeWindow extends TreeWindowAssistantI  {
	DrawTreeCoordinator treeDrawCoordTask;
	TreeSource extraTreeSource;
	ShowOtherTreeExtra extra;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addCheckMenuItem(null, "Show & Compare Other Tree", new MesquiteCommand("showTree", this), showTree);
		treeDrawCoordTask= (DrawTreeCoordinator)hireEmployee(DrawTreeCoordinator.class, null);
		treeDrawCoordTask.setUseMenubar(false);
		extraTreeSource= (TreeSource)hireNamedEmployee(TreeSource.class, "#StoredTrees");
		return true;
	}


	//This method is optional for TreeWindowAssistants, unlike TreeDisplayAssistants
	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay){
		extra = new ShowOtherTreeExtra(this, treeDisplay);
		return extra;
	}


	public boolean isSubstantive(){
		return false;
	}

	// settings ******
	MesquiteBoolean showTree = new MesquiteBoolean(false);
	MesquiteBoolean rotateMainTree = new MesquiteBoolean(false);
	MesquiteCMenuItemSpec rotateMenuItem= null;
	int currentTree = 0;

	// ******************

	String treeName = null;
	boolean warned = false;
	Taxa taxa = null;
	Tree tree = null;
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		treeName = tree.getName();
		taxa = tree.getTaxa();
		this.tree = tree;
	}

	/* ................................................................................................................. */
	/** passes which object changed (from MesquiteListener interface) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (caller == this)
			return;
		if (obj instanceof Tree) 
			extra.forceRefresh();
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {

		Snapshot sn = new Snapshot();

		sn.addLine("showTree " + showTree.toOffOnString());
		if (!showTree.getValue()){
			sn.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa));
			sn.suppressCommandsToEmployee(treeDrawCoordTask);   //Debugg.println do not give tDC its script. 
			sn.suppressCommandsToEmployee(extraTreeSource);
		}
		else{
			sn.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa));
			sn.addLine("getTreeSource", extraTreeSource);
			sn.addLine("getTreeDrawCoordinator", treeDrawCoordTask);
			sn.addLine("setExtraTree " + (currentTree+1));
		}
		sn.addLine("rotateMain " + rotateMainTree.toOffOnString());
		sn.addLine("repaint");
		return sn;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the taxa block", "[block reference, number, or name]", commandName, "setTaxa")){
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				taxa = t;
				if (extraTreeSource!= null)
					extraTreeSource.setPreferredTaxa(taxa);

				return taxa;
			}
		} 
		else if (checker.compare(this.getClass(), "Whether to show a tree", "[true/false]", commandName, "showTree")) {
			showTree.toggleValue(arguments);
			if (extra!= null){
				if (!extra.getTreeDisplay().isUpDownRightLeft() &&showTree.getValue() && !MesquiteThread.isScripting()){
					alert("A comparison tree can be shown only if the tree is drawn in horizontal or vertical orientation.");
					showTree.setValue(false);
					return null;
				}
				extra.turnOnOff(showTree.getValue());
				if (showTree.getValue()){
					if (rotateMenuItem == null)
						rotateMenuItem = addCheckMenuItem(null, "Rotate Main Tree for Better Match", new MesquiteCommand("rotateMain", this), rotateMainTree);
				}
				else {
					deleteMenuItem(rotateMenuItem);
					rotateMenuItem = null;
				}
				resetContainingMenuBar();
				//extra.forceRefresh();
			}
		}
		else if (checker.compare(this.getClass(), "Whether to rotate the main tree", "[true/false]", commandName, "rotateMain")) {
			rotateMainTree.toggleValue(arguments);
			if (extra!= null){
				if (showTree.getValue())
					extra.rotateAndRefresh();
			}
		}
		else if (checker.compare(this.getClass(), "Repaint the tree display and the extra tree", "[]", commandName, "repaint")) {
			if (extra!= null){
				if (showTree.getValue())
				extra.repaintBoth();
			}
		}
		else if (checker.compare(this.getClass(), "Returns the tree source module", "[module]", commandName, "getTreeSource")) {
			return extraTreeSource;
		}
		else if (checker.compare(this.getClass(), "Returns the tree source module", "[module]", commandName, "getTreeDrawCoordinator")) {
			return treeDrawCoordTask;
		}
		else if (checker.compare(this.getClass(), "Returns the tree source module", "[module]", commandName, "silenceTreeDrawCoordinator")) {
			return new MesquiteCommandAbsorber();

		}
		else if (checker.compare(this.getClass(), "Sets which extra tree is shown", "[tree number]", commandName, "setExtraTree")) {
			int index = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0)); 
			if (MesquiteInteger.isCombinable(index)) {
				currentTree = index-1;
				if (extra!= null){
					if (extraTreeSource == null)
						return null;
					Tree tree = extraTreeSource.getTree(taxa, currentTree);
					extra.setExtraTree(extraTreeSource.getTree(taxa, currentTree), currentTree+1);
					//extra.forceRefresh();
				}
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	void turnOff(){
		showTree.setValue(false);
		if (extra!= null){
			extra.turnOnOff(false);
			deleteMenuItem(rotateMenuItem);
			rotateMenuItem = null;
			resetContainingMenuBar();
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Compare Other Tree In Tree Window";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Displays other tree with the current tree." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}

}

/* ===================================================================================================== */
/* ===================================================================================================== */
class ShowOtherTreeExtra extends TreeDisplayExtra  {
	ShowOtherTreeInTreeWindow ownerModule;
	TreeDisplay mainTreeDisplay, extraTreeDisplay;
	TreeDrawing mainTreeDrawing;
	boolean naiveFieldWidth = true;
	TreeDisplayRequests borders = new TreeDisplayRequests(0,0,0,0, 0, 0);
	DoubleArray boxEdges;
	MesquiteTree extraTree, mainTree;
	ExtraExtra extraExtra;
	Rectangle rotateRect = new Rectangle(0,0,0,0);
	
	public MiniScroll extraTreeScroll = null;
	Image rotateImage;
	public ShowOtherTreeExtra(ShowOtherTreeInTreeWindow ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		this.ownerModule = ownerModule;
		this.mainTreeDisplay = treeDisplay;
		rotateImage = MesquiteImage.getImage(ownerModule.getPath() +  "rotate.gif");  
		if (ownerModule.extraTreeSource != null)
			ownerModule.extraTreeSource.setPreferredTaxa(treeDisplay.getTaxa());
		extraTreeDisplay =ownerModule.treeDrawCoordTask.createOneTreeDisplay(mainTreeDisplay.getTaxa(), mainTreeDisplay.getWindow()); //TODO: set tree display when tree is set for first time
		mainTreeDisplay.add(extraTreeDisplay);
		extraTreeDisplay.autoStretchIfNeeded = true;
		if (mainTreeDisplay.isUp())
			extraTreeDisplay.setOrientation(TreeDisplay.DOWN);
		else if (mainTreeDisplay.isDown())
			extraTreeDisplay.setOrientation(TreeDisplay.UP);
		else if (mainTreeDisplay.isRight())
			extraTreeDisplay.setOrientation(TreeDisplay.LEFT);
		else if (mainTreeDisplay.isLeft())
			extraTreeDisplay.setOrientation(TreeDisplay.RIGHT);
		extraTreeDisplay.setAllowReorientation(false);
		extraTreeDisplay.setBounds(0, 0, 0, 0);
		extraTreeDisplay.setBackground(ColorDistribution.veryVeryVeryLightGray);
		extraTreeScroll = new TScroll(MesquiteModule.makeCommand("setExtraTree",  ownerModule), false, 1,1,1,"");
		extraTreeDisplay.add(extraTreeScroll);
		extraTreeScroll.setLocation(0,0); 
		extraExtra = new ExtraExtra(ownerModule, extraTreeDisplay, mainTreeDisplay, extraTreeScroll);
		extraTreeDisplay.addExtra(extraExtra);
		extraTreeDisplay.setVisible(true);
		extraTreeScroll.setVisible(true); 
	}
	public TreeDisplayRequests getRequestsOfTreeDisplay(Tree tree, TreeDrawing treeDrawing){
		borders.rightBorder = 0;
		borders.leftBorder = 0;
		borders.topBorder = 0;
		borders.bottomBorder = 0;
		naiveFieldWidth = false;
		recalculateField(1);
		return borders;
	}

	void recalculateField(int where){
		if (mainTreeDisplay == null || mainTreeDisplay.getTreeDrawing() == null)
			return;

		//System.err.println("@recalculateField " + where + " width " + fieldWidth);
		borders.rightBorder = 0;
		borders.leftBorder = 0;
		borders.topBorder = 0;
		borders.bottomBorder = 0;
		if (!ownerModule.showTree.getValue()){
			extraTreeDisplay.setBounds(0,0, 0, 0);
			extraTreeScroll.setLocation(-1000, -1000); 
		}
		else if (mainTreeDisplay.isUp()) { // x y w h
			borders.topBorder = mainTreeDisplay.getHeight()/2;
			extraTreeDisplay.setAllowReorientation(true);
			extraTreeDisplay.setOrientation(TreeDisplay.DOWN);
			extraTreeDisplay.setAllowReorientation(false);
			extraTreeDisplay.setBounds(mainTreeDisplay.effectiveFieldLeftMargin(), mainTreeDisplay.effectiveFieldTopMargin()-borders.topBorder, mainTreeDisplay.effectiveFieldWidth(),borders.topBorder);
			extraTreeScroll.setLocation(extraTreeDisplay.getWidth()-extraTreeScroll.getWidth()-2, 2); 
		}
		else if (mainTreeDisplay.isDown()) {  // x y w h
			borders.bottomBorder = mainTreeDisplay.getHeight()/2;
			extraTreeDisplay.setAllowReorientation(true);
			extraTreeDisplay.setOrientation(TreeDisplay.UP);
			extraTreeDisplay.setAllowReorientation(false);
			extraTreeDisplay.setBounds(mainTreeDisplay.effectiveFieldLeftMargin(), mainTreeDisplay.effectiveFieldTopMargin()+ mainTreeDisplay.effectiveFieldHeight(), mainTreeDisplay.effectiveFieldWidth(), borders.bottomBorder);
			extraTreeScroll.setLocation(extraTreeDisplay.getWidth()-extraTreeScroll.getWidth()-2, extraTreeDisplay.getHeight()-extraTreeScroll.getHeight()-2); 
		}
		else if (mainTreeDisplay.isRight()) { // x y w h
			borders.rightBorder = mainTreeDisplay.getWidth()/2;
			extraTreeDisplay.setAllowReorientation(true);
			extraTreeDisplay.setOrientation(TreeDisplay.LEFT);
			extraTreeDisplay.setAllowReorientation(false);
			extraTreeDisplay.setBounds(mainTreeDisplay.effectiveFieldLeftMargin()+ mainTreeDisplay.effectiveFieldWidth(), mainTreeDisplay.effectiveFieldTopMargin(), borders.rightBorder, mainTreeDisplay.effectiveFieldHeight());
			extraTreeScroll.setLocation(extraTreeDisplay.getWidth()-extraTreeScroll.getWidth()-2, extraTreeDisplay.getHeight()-extraTreeScroll.getHeight()-2); 
		}
		else if (mainTreeDisplay.isLeft()) {  // x y w h
			borders.leftBorder = mainTreeDisplay.getWidth()/2;
			extraTreeDisplay.setAllowReorientation(true);
			extraTreeDisplay.setOrientation(TreeDisplay.RIGHT);
			extraTreeDisplay.setAllowReorientation(false);
			extraTreeDisplay.setBounds(mainTreeDisplay.effectiveFieldLeftMargin()-borders.leftBorder, treeDisplay.effectiveFieldTopMargin(), borders.leftBorder, mainTreeDisplay.effectiveFieldHeight());
			extraTreeScroll.setLocation(2, extraTreeDisplay.getHeight()-extraTreeScroll.getHeight()-2); 
		}
		extraTreeDisplay.setFont(mainTreeDisplay.getFont());
		extraTreeDisplay.setFieldSize(extraTreeDisplay.getWidth(), extraTreeDisplay.getHeight());
	}


	void turnOnOff(boolean on){
		mainTreeDisplay.reviseBorders(false);
		forceRefresh();
	}

	void forceRefresh(){
		//	System.err.println("@forceRefresh");
		recalculateField(2);
		mainTreeDisplay.redoCalculations(78344);
		extraTreeDisplay.redoCalculations(78243);
		mainTreeDisplay.forceRepaint();
		extraTreeDisplay.forceRepaint();
		mainTreeDisplay.forceRepaint();
	}
	void repaintBoth(){
		mainTreeDisplay.forceRepaint();
		extraTreeDisplay.forceRepaint();
	}

	void rotateAndRefresh(){
		boolean mainRotated = false;
		boolean extraRotated = false;
		if (mainTree != null){
			extraRotated = TreeUtil.rotateToMatch(extraTree, mainTree);
			//System.err.println(" (1e) " + extraRotated);
			if (ownerModule.rotateMainTree.getValue()){
				mainRotated = TreeUtil.rotateToMatch(mainTree, extraTree);
				//System.err.println(" (2m) " + mainRotated);
				if (extraRotated || mainRotated){
					extraRotated = TreeUtil.rotateToMatch(extraTree, mainTree);
					//System.err.println(" (3e) " + extraRotated);
					if (extraRotated){
						boolean tmainRotated = TreeUtil.rotateToMatch(mainTree, extraTree);
						//System.err.println(" (4m) " + tmainRotated);
						mainRotated = mainRotated || tmainRotated;
					}
				}
			}
		}
		recalculateField(3);
		extraTreeDisplay.setTree(extraTree);
		extraTreeDisplay.redoCalculations(17463);
		extraTreeDisplay.recalculatePositions();
		extraTreeDisplay.setVisible(true);

		extraTreeDisplay.forceRepaint();	
		mainTreeDisplay.forceRepaint();	
		if (mainRotated)
			mainTree.notifyListeners(ownerModule, new Notification(MesquiteListener.PARTS_SWAPPED));
	}
	void setExtraTree(Tree tree, int whichNumber){
		if (extraTreeDisplay == null || tree == null)
			return;
		extraTree = tree.cloneTree();
		extraTreeScroll.setCurrentValue(whichNumber);
		rotateAndRefresh();
	}
	public void setTree(Tree tree) {
		mainTree = (MesquiteTree)tree;
		if (!ownerModule.showTree.getValue() || ownerModule.extraTreeSource == null)
			return;
		if (extraTreeScroll!= null)  //Make sure this is done if later turned on
			extraTreeScroll.setMaximumValue(ownerModule.extraTreeSource.getNumberOfTrees(tree.getTaxa()));
		if (ownerModule.currentTree>= ownerModule.extraTreeSource.getNumberOfTrees(tree.getTaxa()))
			ownerModule.currentTree = 0;
		setExtraTree(ownerModule.extraTreeSource.getTree(tree.getTaxa(), ownerModule.currentTree), ownerModule.currentTree+1);
		if (boxEdges == null)
			boxEdges = new DoubleArray(tree.getTaxa().getNumTaxa());
		else if (tree.getTaxa().getNumTaxa()>boxEdges.getSize())
			boxEdges.resetSize(tree.getTaxa().getNumTaxa());
		boxEdges.deassignArray();

	}


	int lastOrientation = -100;
	/* ========================================= */
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (!ownerModule.showTree.getValue())
			return;
		mainTree = (MesquiteTree)tree;
		if (!treeDisplay.isUpDownRightLeft()){
			ownerModule.turnOff();
			return;
		}
		else
			if (lastOrientation != treeDisplay.getOrientation()){
				recalculateField(4);
				extraTreeDisplay.redoCalculations(871129);
				treeDisplay.repaint();
				lastOrientation = treeDisplay.getOrientation();
			}
		if (extraTree == null)
			return;
		Taxa taxa = mainTree.getTaxa();

		TaxonPolygon[] mainPolys = mainTreeDisplay.getTreeDrawing().namePolys;
		if (mainPolys == null)
			return;
		TaxonPolygon[] extraPolys = extraTreeDisplay.getTreeDrawing().namePolys;
		if (extraPolys == null)
			return;

		if (ownerModule.rotateMainTree.getValue()){
			int rootX = (int)mainTreeDisplay.getTreeDrawing().x[mainTree.getRoot()];
			int rootY = (int)mainTreeDisplay.getTreeDrawing().y[mainTree.getRoot()];
			rotateRect.x = rootX+8; rotateRect.y=rootY+8;
			rotateRect.width = 22; rotateRect.height=22;
			if (mainTreeDisplay.isUp())
				g.drawImage(rotateImage, rootX+8, rootY+8, mainTreeDisplay);
			else if (mainTreeDisplay.isDown()){
				g.drawImage(rotateImage, rootX+8, rootY-8, mainTreeDisplay);
				rotateRect.y=rootY-8;
			}
			else if (mainTreeDisplay.isLeft())
				g.drawImage(rotateImage, rootX+8, rootY+8, mainTreeDisplay);
			else if (mainTreeDisplay.isRight()){
				g.drawImage(rotateImage, rootX-24, rootY+8, mainTreeDisplay);
				rotateRect.x = rootX-24;
			}
		}
		else {
			rotateRect.x = 0; rotateRect.y=0;
			rotateRect.width = 0; rotateRect.height=0;
	}

		for (int it = 0; it<taxa.getNumTaxa(); it++){
			int xMain=0;
			int yMain=0;
			int xExtra=0;
			int yExtra=0;
			int buffer = 4;
			if (mainTree.nodeExists(mainTree.nodeOfTaxonNumber(it)) && extraTree.nodeExists(extraTree.nodeOfTaxonNumber(it))){
				if (mainTreeDisplay.isUp()){
					xMain = mainPolys[it].getBounds().x +mainPolys[it].getBounds().width/2;
					yMain = mainPolys[it].getBounds().y - buffer;
					xExtra = extraPolys[it].getBounds().x +extraPolys[it].getBounds().width/2;
					yExtra = extraPolys[it].getBounds().y +extraPolys[it].getBounds().height + buffer;;
				}
				else if (mainTreeDisplay.isDown()){
					xMain = mainPolys[it].getBounds().x +mainPolys[it].getBounds().width/2;
					yMain = mainPolys[it].getBounds().y +mainPolys[it].getBounds().height + buffer;
					xExtra = extraPolys[it].getBounds().x +extraPolys[it].getBounds().width/2;
					yExtra = extraPolys[it].getBounds().y - buffer;
				}
				else if (mainTreeDisplay.isLeft()){
					xMain = mainPolys[it].getBounds().x - buffer;
					yMain = mainPolys[it].getBounds().y +mainPolys[it].getBounds().height/2;
					xExtra = extraPolys[it].getBounds().x +extraPolys[it].getBounds().width + buffer;
					yExtra = extraPolys[it].getBounds().y +extraPolys[it].getBounds().height/2;
				}
				else if (mainTreeDisplay.isRight()){
					xMain = mainPolys[it].getBounds().x +mainPolys[it].getBounds().width + buffer;
					yMain = mainPolys[it].getBounds().y +mainPolys[it].getBounds().height/2;
					xExtra = extraPolys[it].getBounds().x - buffer;
					yExtra = extraPolys[it].getBounds().y +extraPolys[it].getBounds().height/2;
				}
				drawLine(xMain, yMain, xExtra, yExtra, g);
			}

		}
		extraTreeDisplay.repaint();
	}

	void drawLine(int xMain, int yMain, int xExtra, int yExtra, Graphics g){
		Color c = g.getColor();
		//g.drawLine(xMain, yMain, xExtra + extraTreeDisplay.getBounds().x, yExtra + extraTreeDisplay.getBounds().y);
		GraphicsUtil.drawTransparentLine(g, xMain, yMain, xExtra + extraTreeDisplay.getBounds().x, yExtra + extraTreeDisplay.getBounds().y, Color.gray, 2);
		g.setColor(c);
	}
	/*.................................................................................................................*/


	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/* ========================================= */

	void resetFieldSize(int increase){
		if (((mainTreeDisplay.isRight() || mainTreeDisplay.isLeft()) && mainTreeDisplay.effectiveFieldWidth()-mainTreeDisplay.getTipsMargin()-increase<100)
				|| ((mainTreeDisplay.isUp() || mainTreeDisplay.isDown()) && mainTreeDisplay.effectiveFieldHeight()-mainTreeDisplay.getTipsMargin()<100))
			return;

		//	System.err.println("@resetFieldSize");

		mainTreeDisplay.reviseBorders(false);
		recalculateField(5);
		mainTreeDisplay.redoCalculations(78244);
		extraTreeDisplay.redoCalculations(78243);
		mainTreeDisplay.forceRepaint();
		extraTreeDisplay.forceRepaint();
		mainTreeDisplay.forceRepaint();

	}


	public void cursorMove(Tree tree, int x, int y, Graphics g){

	}
	public boolean cursorTouchField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){
		if (rotateRect.contains(x, y)) {
			ownerModule.alert("This symbol indicates that the primary tree is actively being reoriented by rotating the nodes to match to compared tree. This does not change the phylogenetic relationships of the primary tree; "
					+ "It changes only how the tree is presented.");
			return true;
		}
		return false;
	}

}
/* ===================================================================================================== */
class TScroll extends MiniScroll {
	public TScroll (MesquiteCommand command, boolean stacked, int currentValue, int minValue, int maxValue, String itemName) {
		super(command, stacked, currentValue, minValue, maxValue, itemName);
		setBackground(ColorDistribution.veryVeryLightGray);
	}
	public void mouseClicked(int modifiers, int x, int y, MesquiteTool tool) {
	}

	public void paint(Graphics g){
		GraphicsUtil.fillTransparentRect(g, 0, 0, getBounds().width, getBounds().height, ColorDistribution.veryLightGray, 2);
	}

}

/* ===================================================================================================== */
class OffPanel extends MousePanel {
	ShowOtherTreeInTreeWindow ownerModule;
	TreeDisplay treeDisplay;
	Image offImage;
	public OffPanel(ShowOtherTreeInTreeWindow ownerModule, TreeDisplay treeDisplay){
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		offImage = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goaway.gif");  
	}
	public void mouseClicked(int modifiers, int x, int y, MesquiteTool tool) {
		ownerModule.turnOff();
	}

	public void paint(Graphics g){
		GraphicsUtil.fillTransparentRect(g, 0, 0, getBounds().width, getBounds().height, ColorDistribution.veryLightGray, 2);

		g.drawImage(offImage, 0, 0, treeDisplay);
	}

}
/* ===================================================================================================== */

class ExtraExtra extends TreeDisplayExtra implements TreeDisplayBkgdExtra  {
	TreeDisplay mainTreeDisplay, extraTreeDisplay;
	MesquiteTree mainTree, extraTree;
	OffPanel offPanel;
	MiniScroll scroll;
	Rectangle offRect = new Rectangle(0,0,16, 16);
	ShowOtherTreeInTreeWindow ownerModule;
	public ExtraExtra(ShowOtherTreeInTreeWindow ownerModule, TreeDisplay treeDisplay, TreeDisplay mainTreeDisplay, MiniScroll scroll) {
		super(ownerModule, treeDisplay);
		this.ownerModule = ownerModule;
		extraTreeDisplay = treeDisplay;
		offPanel = new OffPanel(ownerModule, treeDisplay);
		offPanel.setBounds(0,0, 18, 18);
		extraTreeDisplay.add(offPanel);
		offPanel.setVisible(true);
		this.mainTreeDisplay = mainTreeDisplay;
		this.scroll = scroll;
	}

	public void setTree(Tree tree) {
		// TODO Auto-generated method stub

	}

	/* ========================================= */
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		extraTree = (MesquiteTree)tree;
		if (extraTree == null || mainTreeDisplay == null)
			return;
		Taxa taxa = extraTree.getTaxa();
		mainTree = (MesquiteTree)mainTreeDisplay.getTree();
		TaxonPolygon[] mainPolys = mainTreeDisplay.getTreeDrawing().namePolys;
		if (mainPolys == null)
			return;
		TaxonPolygon[] extraPolys = extraTreeDisplay.getTreeDrawing().namePolys;
		if (extraPolys == null)
			return;
		offPanel.setLocation(scroll.getLocation().x - 22, scroll.getLocation().y + 2);
		for (int it = 0; it<taxa.getNumTaxa(); it++){
			int xMain=0;
			int yMain=0;
			int xExtra=0;
			int yExtra=0;

			int buffer = 4;
			if (mainTree.nodeExists(mainTree.nodeOfTaxonNumber(it)) && extraTree.nodeExists(extraTree.nodeOfTaxonNumber(it))){
				if (mainTreeDisplay.isUp()){
					xMain = mainPolys[it].getBounds().x +mainPolys[it].getBounds().width/2;
					yMain = mainPolys[it].getBounds().y - buffer;
					xExtra = extraPolys[it].getBounds().x +extraPolys[it].getBounds().width/2;
					yExtra = extraPolys[it].getBounds().y +extraPolys[it].getBounds().height + buffer;;
				}
				else if (mainTreeDisplay.isDown()){
					xMain = mainPolys[it].getBounds().x +mainPolys[it].getBounds().width/2;
					yMain = mainPolys[it].getBounds().y +mainPolys[it].getBounds().height + buffer;
					xExtra = extraPolys[it].getBounds().x +extraPolys[it].getBounds().width/2;
					yExtra = extraPolys[it].getBounds().y - buffer;
				}
				else if (mainTreeDisplay.isLeft()){
					xMain = mainPolys[it].getBounds().x - buffer;
					yMain = mainPolys[it].getBounds().y +mainPolys[it].getBounds().height/2;
					xExtra = extraPolys[it].getBounds().x +extraPolys[it].getBounds().width + buffer;
					yExtra = extraPolys[it].getBounds().y +extraPolys[it].getBounds().height/2;
				}
				else if (mainTreeDisplay.isRight()){
					xMain = mainPolys[it].getBounds().x +mainPolys[it].getBounds().width + buffer;
					yMain = mainPolys[it].getBounds().y +mainPolys[it].getBounds().height/2;
					xExtra = extraPolys[it].getBounds().x - buffer;
					yExtra = extraPolys[it].getBounds().y +extraPolys[it].getBounds().height/2;
				}
				drawLine(xMain, yMain, xExtra, yExtra, g);
			}

		}
	}

	void drawLine(int xMain, int yMain, int xExtra, int yExtra, Graphics g){
		GraphicsUtil.drawTransparentLine(g, xMain - extraTreeDisplay.getBounds().x, yMain - extraTreeDisplay.getBounds().y, xExtra, yExtra, Color.gray, 2);
	}

	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}

	public void drawUnderTree(Tree tree, int drawnRoot, Graphics g) {
		int right = extraTreeDisplay.getBounds().width - 4;
		int bottom = extraTreeDisplay.getBounds().height - 4;
		GraphicsUtil.fillTransparentRect(g, 2, 2, right, bottom, ColorDistribution.veryLightGray, 2);
		/*	GraphicsUtil.drawTransparentLine(g, 2, 2, right, 2, Color.orange, 2);
		GraphicsUtil.drawTransparentLine(g, 2, 2, 2, bottom, Color.orange, 2);
		GraphicsUtil.drawTransparentLine(g, 2, bottom, right, bottom, Color.orange, 2);
		GraphicsUtil.drawTransparentLine(g, right, 2, right, bottom, Color.orange, 2);
		 */
	}


	public void printUnderTree(Tree tree, int drawnRoot, Graphics g) {
	}

}

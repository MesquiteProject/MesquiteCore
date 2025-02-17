/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.tree;

import java.awt.*;
import java.util.*;

import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NameReference;
import mesquite.lib.ProjectReadThread;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaTreeDisplay;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
/** The panel in which a tree is drawn.  This is used within the main tree window, and can be used by other
modules.  
If multiple trees need to be drawn, multiple TreeDisplays should be created.  A TreeDisplay is created
and supervised by a tree draw coordinator module.  A reference to a TreeDisplay is needed 
for shading and decorating the tree. <P>

Within the graphics Panel which is the TreeDisplay, a special object of class TreeDrawing is
actually the one responsible for drawing the tree itself.  The TreeDrawing is created by the
chosen tree drawing module (e.g. diagonal draw tree).  Modules can contribute to the drawing of the
TreeDisplay, whether shading or node pictures or a simple tree legend, by creating an object
of class TreeDisplayExtra and adding it to the vector of extras.  The TreeDisplay calls all of 
the extras after the tree is drawn, in case those extras need to contribute to the TreeDisplay drawing.
<p> This needs to be fixed up (e.g., fields made private with set and get methods).
 */
public class TreeDisplay extends TaxaTreeDisplay  {
	DrawNamesTreeDisplay namesTask;

	/** The tree being drawn */
	protected Tree tree;
	/** A tree which is to be drawn, but which is being held until drawing of the current tree is finished. */
	protected Tree holdingTree;
	/** Orientation of the tree unspecified */
	public static final int FREEFORM = -1;
	/** Orientation of the tree with tips up */
	public static final int UP = 0;
	/** Orientation of the tree with tips to right */
	public static final int RIGHT = 1;
	/** Orientation of the tree with tips to left */
	public static final int LEFT = 2;
	/** Orientation of the tree with tips down */
	public static final int DOWN = 3;
	/** Tree is drawn in circular arrangement of nodes */
	public static final int CIRCULAR = 4;
	/** Tree is drawn in circular arrangement of nodes */
	public static final int UNROOTED = 5;
	/** Tree orientation is not yet set; take from node locs module */
	public static final int NOTYETSET = -1;

	public static final int FONTSIZECHANGED = 18275;  //for notification

	/**  The margin from the tips to the edge of the drawing field*/
	public int tipsMargin = -1;
	/**  Scaling of the tree drawing*/
	public double scaling = 1.0;
	/**  If tree drawn with fixed depth, this is the depth.*/
	public double fixedDepthScale = 1.0;
	/**  Records whether fixed depth scale is in use.*/
	public boolean fixedScalingOn = false;
	/**  Records whether to show the scale bar.*/
	public boolean inhibitDefaultScaleBar = false;
	/**  If true, then in text version draw the extra information directly on the tree; otherwise use node lists*/
	public boolean textVersionDrawOnTree = false;
	private int dist=8;
	private int minDist=8;
	int minForTerminalBoxes = 0;

	/**  What is the mode for highlighting selected taxa in tree displays? */
	public static final int sTHM_NONE = 0;
	public static final int sTHM_GREYBOX = 1;
	public static final int sTHM_BIGNAME = 2; //selectedTaxonHighlightMode/bigNameDivisor is how much bigger
	// 2 should translate to 1.25X, 3 to 1.5X, 4 to 1.75X, 5 to 2X (i.e. (selectedTaxonHighlightMode + 3)/4)
	public static int sTHM_DEFAULT = sTHM_GREYBOX;
	public int selectedTaxonHighlightMode = sTHM_DEFAULT;

	protected boolean showBranchColors = true;
	public static boolean printTreeNameByDefault = false;

	/**  The color of the branches*/
	public Color branchColor;
	/**  The color of a dimmed branch*/
	public Color branchColorDimmed;
	/**  The panel in which the tree is actually drawn.*/
	private TreeDrawing treeDrawing;
	/**  The width of the drawn edges (branches)*/
	private int edgewidth;

	/**  whether the taxon name drawer should put the name of a collapsed clade at its leftmost ancestor (e.g. for Square Line tree) or at its MRCA (e.g., for plot tree)*/
	public boolean collapsedCladeNameAtLeftmostAncestor = false;

	/**  Spacing in pixels between taxa*/
	private int taxonSpacing;

	/**  Spacing in pixels between taxa as set by user*/
	private int fixedTaxonSpacing = 0;
	/**  Orientaton of the tree*/
	private int treeOrientation = NOTYETSET;
	/**  For vert/horizontal trees, is default to permit stretching by default of the tree.  Set by tree drawer*/
	//	public boolean inhibitStretchByDefault = false;
	/**  For vert/horizontal trees, is default to permit stretching by default of the tree.  Set by tree window*/
	public boolean autoStretchIfNeeded = false;
	/**  Is the orientation fixed, or can reorientation be done?*/
	private boolean allowReorient = true;
	private MesquiteInteger highlightedBranch  = new MesquiteInteger(0);
	MesquiteCommand recalcCommand;


	public TreeDisplay (MesquiteModule ownerModule, Taxa taxa) { 
		super(ownerModule,taxa);
		branchColor = Color.black;
		branchColorDimmed = Color.gray;
		recalcCommand = new MesquiteCommand("redoCalculations", this);
		recalcCommand.setSuppressLogging(true); 
	}

	public static final int DRAWULTRAMETRIC = 0; //	
	public static final int AUTOSHOWLENGTHS = 1;
	public static final int DRAWUNASSIGNEDASONE = 2; //if a branch has unassigned length, treat as length 1
	public int branchLengthDisplay = DRAWULTRAMETRIC;
	/**  If true, then branches are drawn proportional to their lengths*/
	public boolean showBranchLengths(){
		return branchLengthDisplay != DRAWULTRAMETRIC;
	}

	public int getMouseX(){
		return super.getMouseX();
	}
	public int getMouseY(){
		return super.getMouseY();
	}

	public Graphics getGraphics(){
		Graphics g = super.getGraphics();
		if (g == null)
			return null;
		g.clipRect(100, 100, 200, 200);
		return g;
	}

	public Font getTaxonNamesFont(){
		return namesTask.getFont();
	}

	/*_________________________________________________*/
	double[] scaleValues; // graphical start x, y; end x, y; time unit start, end.
	public void setScale(double[] values){
		this.scaleValues = values;
	}
	public double[] getScale(){
		return scaleValues;
	}
	/*_________________________________________________*/
	NameReference 	palenessRef = NameReference.getNameReference("drawPale");
	NameReference 	oldColourNameRef = NameReference.getNameReference("color");
	MesquiteInteger pos = new MesquiteInteger(0);
	public Color getBranchColor(int N){
		if (!showBranchColors || tree == null)
			return branchColor;
		Color color = null;
		String cRGB = null;
		if (tree.withinCollapsedClade(N)){
			cRGB = ((MesquiteTree)tree).uniformColorInClade(tree.deepestCollapsedAncestor(N));
		}
		if (cRGB == null)
			cRGB = (String)((MesquiteTree)tree).getColorAsHexString(N);
		if (cRGB != null) {
			pos.setValue(0);
			Color colRGB = ColorDistribution.getColorFromArguments(cRGB, pos);
			if (colRGB != null)
				color = colRGB;
		}
		if (color == null) {  //old, just in case
			long c = tree.getAssociatedLong(oldColourNameRef, N);
			Color col=null;
			if (!tree.anySelected() || tree.getSelected(N)) {
				if (MesquiteLong.isCombinable(c) && (col = ColorDistribution.getStandardColor((int)c))!=null)
					color = col;
				else
					color = branchColor;
			}
			else {
				if (MesquiteLong.isCombinable(c) && (col = ColorDistribution.getStandardColorDimmed((int)c))!=null)
					color = col;
				else
					color = branchColorDimmed;
			}
		}
		if (tree instanceof MesquiteTree) {
			double palenessMultiplier = ((MesquiteTree)tree).getAssociatedDouble(palenessRef, N);
			if (MesquiteDouble.isCombinable(palenessMultiplier)) {
				Color c = color;
				int red =paleComponent(c.getRed(), palenessMultiplier);
				int green =paleComponent(c.getGreen(), palenessMultiplier);
				int blue =paleComponent(c.getBlue(), palenessMultiplier);
				color = new Color(red, green, blue);
			}
		}
		return color;
	}
	int paleComponent(int c, double palenessMultiplier) {
		c = (int)(255 - palenessMultiplier*(255-c));
		if (c<0) {
			//	Debugg.println("--------------------colour " + c);
			return 0;
		}
		if (c>255) {
			// Debugg.println("++++++++++++++colour " + c);
			return 255;
		}
		return c;
	}
	public Composite setBranchTransparency(Graphics g, int N){
		if (!showBranchColors)
			return null;
		if (tree.anySelected() && !tree.getSelected(N)) {
			Composite composite = ColorDistribution.getComposite(g);
			ColorDistribution.setTransparentGraphics(g, 0.5f);
			return composite;
		}
		return null;
	}
	public void setTreeDrawing(TreeDrawing td) {
		treeDrawing = td;
		if (treeDrawing!=null && tree!=null)
			setTree(tree);
	}
	public TreeDrawing getTreeDrawing() {
		return treeDrawing;
	}

	public void setDrawingInProcess(boolean inProgress){
		this.inProgress= inProgress;
		if (!inProgress && holdingTree != null) {
			setTree(holdingTree);
			//repaint();
		}
	}
	public boolean repaintPending(){
		return repaintsPending > 0;
	}
	public void forceRepaint(){
		repaint();
	}

	public Tree getTree() {
		return tree;
	}

	public void setTree(Tree tree) {

		if (tree!=null && tree.getTaxa() != taxa)
			setTaxa(tree.getTaxa());

		if (inProgress){
			if (MesquiteTrunk.debugMode)
				addToChain("TD-setTree HOLDING " + StringUtil.getDateTimeWithSeconds());
			holdingTree = tree;
		}
		else {
			this.tree = tree;
			if (treeDrawing !=null) {
				if (tree !=null)
					treeDrawing.setDrawnRoot(tree.getRoot());
				else
					treeDrawing.setDrawnRoot(0);
			}
			redoCalculations(1);
			holdingTree = null;
		}
	}

	public DrawNamesTreeDisplay getDrawTaxonNames(){
		return namesTask;
	}
	public void setDrawTaxonNames(DrawNamesTreeDisplay dtn){
		namesTask = dtn;
	}

	//Distance from tip to taxon name
	boolean tndExplicitlySet = false;
	public void setMinimumTaxonNameDistanceFromTip(int minForTerminalBoxes, int min) {
		this.minForTerminalBoxes = minForTerminalBoxes;
		this.minDist = min;
		if (!tndExplicitlySet || dist<minDist)
			dist = minDist;
	}
	public void setTaxonNameDistanceFromTip(int newDist) { 
		if (newDist>=minDist) {
			this.dist = newDist;
			tndExplicitlySet = true;
		}
	}
	public int getTaxonNameDistanceFromTip() {
		if (treeDrawing != null && treeDrawing.terminalBoxesRequested())
			return dist + minForTerminalBoxes;
		else
			return dist;
	}
	public int effectiveFieldWidth(){
		return getField().width-effectiveFieldLeftMargin()-effectiveFieldRightMargin();
	}
	public int effectiveFieldHeight(){
		return getField().height-effectiveFieldTopMargin()-effectiveFieldBottomMargin();
	}
	public int effectiveFieldLeftMargin(){
		if (bordersRequestedByExtras ==null)
			return 0;
		return bordersRequestedByExtras[0];
	}
	public int effectiveFieldRightMargin(){
		if (bordersRequestedByExtras ==null)
			return 0;
		return bordersRequestedByExtras[2];
	}
	public int effectiveFieldTopMargin(){
		if (bordersRequestedByExtras ==null)
			return 0;
		return bordersRequestedByExtras[1];
	}
	public int effectiveFieldBottomMargin(){
		if (bordersRequestedByExtras ==null)
			return 0;
		return bordersRequestedByExtras[3];
	}
	public void setTipsMargin(int margin) {
		tipsMargin = margin;
	}
	public int getTipsMargin() {
		return tipsMargin;
	}
	public MesquiteInteger getHighlightedBranchMI() {  
		return highlightedBranch;
	}
	public int getHighlightedBranch() {  
		return highlightedBranch.getValue();
	}
	public void setHighlightedBranch(int value) {  
		highlightedBranch.setValue(value);
	}
	public void redoCalculations(int code){
		if (treeDrawing!=null && tree !=null)
			treeDrawing.recalculatePositions(tree); //to force node locs recalc

	}
	
	public void redoCalculationsMainThread(){
		recalcCommand.doItMainThread(null, null, null); 
		}
	
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Recalculates node positions", "[]", commandName, "redoCalculations")) {
			if (!(Thread.currentThread() instanceof ProjectReadThread))
				redoCalculations(134618);
		}
		else return super.doCommand(commandName, arguments, checker);
		return null;
	}

	public void setEdgeWidth(int sp) {
		this.edgewidth = sp;
	}
	public int getEdgeWidth() {
		return edgewidth;
	}
	public void setTaxonSpacing(int sp) {
		this.taxonSpacing = sp;
	}
	public int getTaxonSpacing() {
		return taxonSpacing;
	}
	public void setFixedTaxonSpacing(int sp) {
		this.fixedTaxonSpacing = sp;
	}
	public int getFixedTaxonSpacing() {
		return fixedTaxonSpacing;
	}
	
	boolean rectsEqual(int[] r1, int[] r2){
		if (r1 == null){
			if (r2 != null)
			return false;
			else
				return true;
			}
		if (r2== null)
			return false;
		return r1[0] == r2[0] && r1[1] == r2[1] && r1[2] == r2[2] && r1[3] == r2[3];
	}
	public void addExtra(TreeDisplayExtra extra) {
		if (extras != null){
			extras.addElement(extra, false);
			if (tree != null){
				int[] rect = getRequestedBorders();
				accumulateBordersFromExtras(tree);
				int[] rect2 = getRequestedBorders();
				if (!rectsEqual(rect, rect2))
					redoCalculationsMainThread();
			}
	}
	}
	public void removeExtra(TreeDisplayExtra extra) {
	 if (extras != null){
			extras.removeElement(extra, false);
			if (tree != null){
				int[] rect = getRequestedBorders();
				accumulateBordersFromExtras(tree);
				int[] rect2 = getRequestedBorders();
				if (!rectsEqual(rect, rect2))
					redoCalculationsMainThread();
			}
		}
	
	}
	public boolean findExtra(TreeDisplayExtra extra) {
		if (extras == null)
			return false;
		return (extras.indexOf(extra) >= 0);
	}

	int locationSetX = 0;
	int locationSetY = 0;
	public void adjustLocation(int x, int y){
		super.setLocation(locationSetX + x, locationSetY + y);
	}
	public void setLocation(int x, int y){
		locationSetX = x;
		locationSetY = y;
		super.setLocation(x, y);
	}
	public void moveExtraToFront(TreeDisplayExtra extra){
		if (extra == null)
			return;
		Vector panels = extra.getPanels();
		for (int i=0; panels !=null && i<panels.size(); i++) {
			Panel p = (Panel)panels.elementAt(i);
			remove(p);
			add(p);
		}
		getExtras().removeElement(extra, false);
		getExtras().addElement(extra, false);
		pleaseUpdate(false);
	}
	public TreeDisplayExtra[] getMyExtras(MesquiteModule mb) {
		if (extras != null) {
			int count = 0;
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex.getOwnerModule() == mb) 
					count++;
			}
			if (count == 0)
				return null;
			TreeDisplayExtra[] ee = new TreeDisplayExtra[count];
			e = extras.elements();
			count = 0;
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex.getOwnerModule() == mb) 
					ee[count++] = ex;
			}
			return ee;
		}
		return null;
	}
	public void setTreeAllExtras(Tree tree) {
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ownerModule==null || ownerModule.isDoomed()) 
					return;
				ex.setTree(tree);
			}
			accumulateBordersFromExtras(tree);
		}
	}

	int[] zeroBorders = new int[]{0, 0, 0, 0};  //left top right bottom
	int[] bordersRequestedByExtras = new int[]{0, 0, 0, 0};  //left top right bottom
	public int[] getRequestedBorders(){
		return bordersRequestedByExtras;
	}
	public void accumulateBordersFromExtras(Tree tree) {
		int[] bordersTemp = getBordersFromExtras(tree);
		if (bordersTemp != null && bordersTemp.length == 4)
			bordersRequestedByExtras = bordersTemp;
	}
	int[] getBordersFromExtras(Tree tree) {
		if (tree == null || tree.getTaxa().isDoomed())
			return zeroBorders;
		if (extras != null) {
			int[] overallBorder = new int[]{0, 0, 0, 0};
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ownerModule==null || ownerModule.isDoomed()) 
					return zeroBorders;
				int[] borderRequest = ex.getRequestedExtraBorders(tree, treeDrawing);
				if (borderRequest != null && borderRequest.length == 4){
					for (int i=0;i<4; i++)
						if (borderRequest[i]>overallBorder[i])
							overallBorder[i] = borderRequest[i];
				}

			}
			return overallBorder;
		}
		return zeroBorders;
	}
	public void drawAllBackgroundExtrasOfPlacement(Tree tree, int drawnRoot, Graphics g, int placement) {
		if (tree == null || tree.getTaxa().isDoomed())
			return;
		if (extras != null) {
			//EARLY
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex instanceof TreeDisplayEarlyExtra && ex instanceof TreeDisplayBkgdExtra && ex.getPlacement()==placement) {
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					((TreeDisplayBkgdExtra)ex).drawUnderTree(tree, drawnRoot, g);
				}
			}
			//DEFAULT
			e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (!(ex instanceof TreeDisplayEarlyExtra || ex instanceof TreeDisplayLateExtra) && ex instanceof TreeDisplayBkgdExtra && ex.getPlacement()==placement) {
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					((TreeDisplayBkgdExtra)ex).drawUnderTree(tree, drawnRoot, g);
				}
			}
			//LATE
			e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex instanceof TreeDisplayLateExtra && ex instanceof TreeDisplayBkgdExtra && ex.getPlacement()==placement) {
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					((TreeDisplayBkgdExtra)ex).drawUnderTree(tree, drawnRoot, g);
				}
			}
		}
	}
	public void drawAllBackgroundExtras(Tree tree, int drawnRoot, Graphics g) {
		drawAllBackgroundExtrasOfPlacement(tree,drawnRoot,g,TreeDisplayExtra.BELOW);
		drawAllBackgroundExtrasOfPlacement(tree,drawnRoot,g,TreeDisplayExtra.NORMAL);
		drawAllBackgroundExtrasOfPlacement(tree,drawnRoot,g,TreeDisplayExtra.ABOVE);
	}
	public void drawAllExtras(Tree tree, int drawnRoot, Graphics g) {
		if (tree == null || tree.getTaxa().isDoomed())
			return;

		if (extras != null) {
			Enumeration e = extras.elements();
			//EARLY
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex instanceof TreeDisplayEarlyExtra){
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					Shape clip = g.getClip();
					g.setClip(null);
					ex.drawOnTree(tree, drawnRoot, g);
					g.setClip(clip);
				}
			}
			//DEFAULT
			e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (!(ex instanceof TreeDisplayEarlyExtra || ex instanceof TreeDisplayLateExtra)){
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					Shape clip = g.getClip();
					g.setClip(null);
					ex.drawOnTree(tree, drawnRoot, g);
					g.setClip(clip);
				}
			}
			//LATE
			e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex instanceof TreeDisplayLateExtra){
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					Shape clip = g.getClip();
					g.setClip(null);
					ex.drawOnTree(tree, drawnRoot, g);
					g.setClip(clip);
				}
			}
		}
		if (notice!=null)
			g.drawString(notice, 6, getBounds().height-6);
		if (drawFrame)
			g.drawRoundRect(0, 0, getBounds().width, getBounds().height, 10, 10);
	}
	public void printAllBackgroundExtras(Tree tree, int drawnRoot, Graphics g) {
		if (tree == null || tree.getTaxa().isDoomed())
			return;

		if (extras != null) {
			//EARLY
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex instanceof TreeDisplayEarlyExtra && ex instanceof TreeDisplayBkgdExtra) {
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					((TreeDisplayBkgdExtra)ex).printUnderTree(tree, drawnRoot, g);
				}
			}
			//DEFAULT
			e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (!(ex instanceof TreeDisplayEarlyExtra || ex instanceof TreeDisplayLateExtra) && ex instanceof TreeDisplayBkgdExtra) {
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					((TreeDisplayBkgdExtra)ex).printUnderTree(tree, drawnRoot, g);
				}
			}
			//LATE
			e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex instanceof TreeDisplayLateExtra && ex instanceof TreeDisplayBkgdExtra) {
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					((TreeDisplayBkgdExtra)ex).printUnderTree(tree, drawnRoot, g);
				}
			}
		}
	}
	public void printAllExtras(Tree tree, int drawnRoot, Graphics g) {
		if (tree == null || tree.getTaxa().isDoomed())
			return;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;

				if (ownerModule==null || ownerModule.isDoomed()) 
					return;
				ex.printOnTree(tree, drawnRoot, g);
			}
		}
	}


	/*.................................................................................................................*/
	public void setOrientation(int orient) {
		if (allowReorient)
			treeOrientation = orient;
	}
	/*.................................................................................................................*/
	public int getOrientation() {
		return treeOrientation;
	}

	/*.................................................................................................................*/
	public void setAllowReorientation(boolean allow) {
		allowReorient = allow;
	}
	/**return a text version of information on tree*/
	private void branchLengthsOnTree(Tree tree, int node, String[] nodeStrings){
		if (tree.nodeIsInternal(node)){
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
				branchLengthsOnTree(tree, daughter, nodeStrings);
		}
		nodeStrings[node]= ""+ MesquiteDouble.toString(tree.getBranchLength(node));
	}

	/*.................................................................................................................*/
	public String branchLengthsAtNodes(Tree tree, int node){
		if (!tree.hasBranchLengths())
			return "";
		if (!tree.nodeInTree(node))
			node = tree.getRoot();
		String[] nodeStrings= new String[tree.getNumNodeSpaces()];
		branchLengthsOnTree(tree, node, nodeStrings);
		StringBuffer buff = new StringBuffer(50);
		for (int i=0; i<nodeStrings.length; i++) {
			String nodeType = "Terminal";
			if (!tree.nodeIsTerminal(i))
				nodeType="Internal";
			if (!StringUtil.blank(nodeStrings[i])) {
				buff.append("node " + i + ": \t" + nodeType+"\t");
				buff.append(nodeStrings[i] + "\n");
			}
		}

		if (StringUtil.notEmpty(buff.toString()))
			return "Branch lengths\nNode\tTerm/Int\tLength" + "\n\n" + buff.toString();
		return "";
	}

	/*.................................................................................................................*/
	public String getBranchLengthList(Tree tree) {
		return branchLengthsAtNodes(tree,tree.getRoot());
	}

	/*.................................................................................................................*/
	/**return a text version of information on tree*/
	private void associatesOnTree(MesquiteTree tree, int node, String[] nodeStrings){
		if (tree.nodeIsInternal(node)){
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
				associatesOnTree(tree, daughter, nodeStrings);
		}
		nodeStrings[node]= ""+tree.toString(node);
	}
	public String getAssociatesAtNodes(MesquiteTree tree){

		if (!tree.hasAnyAssociates())
			return "";
		int node = tree.getRoot();
		String[] nodeStrings= new String[tree.getNumNodeSpaces()];
		associatesOnTree(tree, node, nodeStrings);
		StringBuffer buff = new StringBuffer(50);
		for (int i=0; i<nodeStrings.length; i++) {
			if (!StringUtil.blank(nodeStrings[i])) {
				buff.append("node " + i + ": \t");
				buff.append(nodeStrings[i] + "\n");
			}
		}

		if (StringUtil.notEmpty(buff.toString()))
			return "Values associated with nodes\n\n" + buff.toString();
		return "";
	}

	/*.................................................................................................................*/
	public String getTextVersion() {
		if (tree==null || treeDrawing == null)
			return "";
		String s = "Tree Description:  " +  tree.writeTreeSimpleByNames();
		TextTree tt = new TextTree(tree);
		String[] nodeNumbers = new String[tree.getNumNodeSpaces()];
		for (int i=0; i< nodeNumbers.length; i++)
			nodeNumbers[i] = Integer.toString(i);
		StringBuffer buff = new StringBuffer(50);
		tt.drawTreeAsText(tree, buff, nodeNumbers);
		if (textVersionDrawOnTree)
			s+= "\n" + buff.toString();
		else
			s+= "\nTree with node numbers:\n" + buff.toString();
		String branchLengths = getBranchLengthList(tree);
		if (StringUtil.notEmpty(branchLengths))
			s+=branchLengths;

		if (tree instanceof MesquiteTree){
			String assoc = getAssociatesAtNodes((MesquiteTree)tree);
			s += "\n" + assoc + "\n";
		}
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex!=null){
					String sEx =null;
					if (textVersionDrawOnTree)
						sEx =ex.writeOnTree(tree, treeDrawing.getDrawnRoot());
					else
						sEx = ex.infoAtNodes(tree, treeDrawing.getDrawnRoot());
					if (!StringUtil.blank(sEx)) {
						String owner = "";
						if (ex.getOwnerModule()!=null)
							owner = ex.getOwnerModule().getName();
						s+= "\n\n--------------- " + owner + " ---------------";
						s+= "\n\n"+ sEx + "\n";
					}
				}
			}
		}
		return s;
	}

	public String getTableVersion() {
		if (tree==null || treeDrawing == null)
			return "";
		StringBuffer sb = new StringBuffer(100);
		nodesOnTree(tree, treeDrawing.getDrawnRoot(), sb);
		String s = "nodes" + sb.toString() + "\n" ;

		String[] nodeNumbers = new String[tree.getNumNodeSpaces()];
		for (int i=0; i< nodeNumbers.length; i++)
			nodeNumbers[i] = Integer.toString(i);
		StringBuffer buff = new StringBuffer(50);

		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				if (ex!=null){
					String sEx = ex.tableAtNodes(tree, treeDrawing.getDrawnRoot());
					if (!StringUtil.blank(sEx)) {
						String owner = "";
						if (ex.getOwnerModule()!=null)
							owner = ex.getOwnerModule().getName();
						s+= owner + sEx + "\n";
					}
				}
			}
		}
		return s;
	}
	/**returns list of node as done by textOnTree of TreeDisplayExtras*/
	private void nodesOnTree(Tree tree, int node, StringBuffer sb){
		if (tree.nodeIsInternal(node)){
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
				nodesOnTree(tree, daughter, sb);
		}
		sb.append("<tab>" + node);
	}

	public void dispose(){
		MesquiteWindow w = MesquiteWindow.windowOfItem(this);
		if (w!=null)
			w.waitUntilDisposable();

		ownerModule =null;
		if (treeDrawing !=null)
			treeDrawing.dispose();
		treeDrawing = null;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
				ex.dispose();
			}
		}
		destroyExtras();
		extras = null;
		/*
		protected Tree tree, holdingTree;
		public Vector extras;
		private TreeDrawing treeDrawing;
		protected MesquiteModule ownerModule;
		 */
		super.dispose();
	}

	public boolean getShowBranchColors() {
		return showBranchColors;
	}

	public void setShowBranchColors(boolean showBranchColors) {
		this.showBranchColors = showBranchColors;
	}

}



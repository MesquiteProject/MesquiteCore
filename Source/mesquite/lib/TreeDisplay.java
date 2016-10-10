/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;

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
	/** Tree orientation is not yet set; take from node locs module */
	public static final int NOTYETSET = -1;
	
	public static final int FONTSIZECHANGED = 18275;  //for notification

	/**  The margin from the tips to the edge of the drawing field*/
	public int tipsMargin = -1;
	/**  If true, then branches are drawn proportional to their lengths*/
	public boolean showBranchLengths = false;
	/**  Scaling of the tree drawing*/
	public double scaling = 1.0;
	/**  If tree drawn with fixed depth, this is the depth.*/
	public double fixedDepthScale = 1.0;
	/**  Records whether fixed depth scale is in use.*/
	public boolean fixedScalingOn = false;
	/**  If true, then in text version draw the extra information directly on the tree; otherwise use node lists*/
	public boolean textVersionDrawOnTree = false;
	private int dist=8;
	private int minDist=8;
	int minForTerminalBoxes = 0;
	
	protected boolean showBranchColors = true;
	
	
	/**  The color of the branches*/
	public Color branchColor;
	/**  The color of a dimmed branch*/
	public Color branchColorDimmed;
	/**  The panel in which the tree is actually drawn.*/
	private TreeDrawing treeDrawing;
	/**  The width of the drawn edges (branches)*/
	private int edgewidth;

	/**  Spacing in pixels between taxa*/
	private int taxonSpacing;
	
	/**  Spacing in pixels between taxa as set by user*/
	private int fixedTaxonSpacing;
	/**  Orientaton of the tree*/
	private int treeOrientation = NOTYETSET;
	/**  For vert/horizontal trees, is default to permit stretching by default of the tree.  Set by tree drawer*/
//	public boolean inhibitStretchByDefault = false;
	/**  For vert/horizontal trees, is default to permit stretching by default of the tree.  Set by tree window*/
	public boolean autoStretchIfNeeded = false;
	/**  Is the orientation fixed, or can reorientation be done?*/
	private boolean allowReorient = true;
	private MesquiteInteger highlightedBranch  = new MesquiteInteger(0);
	
	/**  whether "triangled" clades are shown as simple triangles or not*/
	private boolean simpleTriangle=true;
	
	
	public TreeDisplay (MesquiteModule ownerModule, Taxa taxa) { 
		super(ownerModule,taxa);
		branchColor = Color.black;
		branchColorDimmed = Color.gray;
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

	public Color getBranchColor(int N){
		if (!showBranchColors)
			return branchColor;
		long c = tree.getAssociatedLong(ColorDistribution.colorNameReference, N);
		Color col=null;
		if (!tree.anySelected() || tree.getSelected(N)) {
			if (MesquiteLong.isCombinable(c) && (col = ColorDistribution.getStandardColor((int)c))!=null)
				return col;
			else
				return branchColor;
		}
		else {
			if (MesquiteLong.isCombinable(c) && (col = ColorDistribution.getStandardColorDimmed((int)c))!=null)
				return col;
			else
				return branchColorDimmed;
		}
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

	 	if (inProgress)
	 		holdingTree = tree;
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


	boolean tndExplicitlySet = false;
	public void setMinimumTaxonNameDistance(int minForTerminalBoxes, int min) {
		this.minForTerminalBoxes = minForTerminalBoxes;
		this.minDist = min;
		if (!tndExplicitlySet || dist<minDist)
			dist = minDist;
	}
	public void setTaxonNameDistance(int newDist) { 
		if (newDist>=minDist) {
			this.dist = newDist;
			tndExplicitlySet = true;
		}
	}
	public int getTaxonNameDistance() {
		if (treeDrawing != null && treeDrawing.terminalBoxesRequested())
			return dist + minForTerminalBoxes;
		else
			return dist;
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
	public void addExtra(TreeDisplayExtra extra) {
		if (extras != null)
		extras.addElement(extra, false);
	}
	public void removeExtra(TreeDisplayExtra extra) {
		if (extras != null)
			extras.removeElement(extra, false);
	}
	public boolean findExtra(TreeDisplayExtra extra) {
		if (extras == null)
			return false;
		return (extras.indexOf(extra) >= 0);
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
		}
	}
	public void drawAllBackgroundExtrasOfPlacement(Tree tree, int drawnRoot, Graphics g, int placement) {
		if (tree == null || tree.getTaxa().isDoomed())
			return;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
	 			if (ex instanceof TreeDisplayBkgdExtra && ex.getPlacement()==placement) {
				   	if (ownerModule==null || ownerModule.isDoomed()) 
				   		return;
				   	ex.drawOnTree(tree, drawnRoot, g);
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
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
	 			if (!(ex instanceof TreeDisplayBkgdExtra)) {
				   	if (ownerModule==null || ownerModule.isDoomed()) 
				   		return;
	 				ex.drawOnTree(tree, drawnRoot, g);
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
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				TreeDisplayExtra ex = (TreeDisplayExtra)obj;
	 			if (ex instanceof TreeDisplayBkgdExtra) {
				   	if (ownerModule==null || ownerModule.isDoomed()) 
				   		return;
	 				ex.printOnTree(tree, drawnRoot, g);
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
	 			if (!(ex instanceof TreeDisplayBkgdExtra)) {
	 
				   	if (ownerModule==null || ownerModule.isDoomed()) 
				   		return;
		 			ex.printOnTree(tree, drawnRoot, g);
	 			}
	 		}
		}
	}
	
	
	/*.................................................................................................................*/
	public void setOrientation(int orient) {
		if (allowReorient)
			treeOrientation = orient;
	}
	/*_________________________________________________*/
	public void setSimpleTriangle(boolean simpleTriangle) {
		this.simpleTriangle=simpleTriangle;
	}
	/*_________________________________________________*/
	public boolean getSimpleTriangle() {
		return simpleTriangle;
	}
	/*.................................................................................................................*/
	public int getOrientation() {
		return treeOrientation;
	}

	/*.................................................................................................................*/
	public void setAllowReorientation(boolean allow) {
		allowReorient = allow;
	}
	/*.................................................................................................................*/
	public boolean getAllowReorientation() {
		return allowReorient;
	}
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



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

import mesquite.lib.Listable;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteModule;
import mesquite.lib.OwnedByModule;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteWindow;

import java.util.*;


/* ======================================================================== */
/** A class used for additional graphical and calculations elements to be drawn and calculated within 
TreeDisplayes -- tree legends, trace characters, etc. The TreeDisplayExtra is notified when the cursor 
is moved over a branch and so on, and when the tree is drawn the TreeDisplayExtra is notified via drawOnTree so that
it can add its items to the tree.*/
public abstract class TreeDisplayExtra implements Listable, OwnedByModule {  
	public TreeDisplay treeDisplay;
	public MesquiteModule ownerModule;
	public static long totalCreated = 0;
	public static int BELOW = 1;
	public static int NORMAL = 2;
	public static int ABOVE = 3;
	int placement=NORMAL;
	
	private Vector panels = new Vector();
	public TreeDisplayExtra (MesquiteModule ownerModule, TreeDisplay treeDisplay) {
		this.treeDisplay = treeDisplay;
		this.ownerModule=ownerModule;
		totalCreated++;
	}
	
	public MesquiteModule getOwnerModule(){
		return ownerModule;
	}
	
	public String getName(){
		if (ownerModule !=null)
			return ownerModule.getName();
		else
			return getClass().getName();
	}

	public int getPlacement(){
		return placement;
	}

	public void setPlacement(int placement){
		this.placement = placement;
	}
	
	/* The TreeDisplayRequests object has public int fields leftBorder, topBorder, rightBorder, bottomBorder (in pixels and in screen orientation)
	 * and a public double field extraDepthAtRoot (in branch lengths units and rootward regardless of screen orientation) */
	public TreeDisplayRequests getRequestsOfTreeDisplay(Tree tree, TreeDrawing treeDrawing){
		return null;
	}

	public TreeDisplay getTreeDisplay(){
		return treeDisplay;
	}
	public boolean requestTraceMode(){
		return false;
	}
	public void dispose(){
		ownerModule =null;
		treeDisplay=null;
	}
	/**notifies the TreeDisplayExtra that the tree has changed, so it knows to redo calculations, and so on*/
	public abstract void setTree(Tree tree);
	/**draw on the tree passed*/
	public abstract void drawOnTree(Tree tree, int drawnRoot, Graphics g);
	/**print on the tree passed*/
	public abstract void printOnTree(Tree tree, int drawnRoot, Graphics g);
	
	/**return true if any notes at nodes in clade*/
	private boolean anyText(Tree tree, int node){
		if (!StringUtil.blank(textAtNode(tree, node)))
			return true;
		if (tree.nodeIsInternal(node)){
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
					if (anyText(tree, daughter))
						return true;
			}
		}
		return false;
	}
	/**return a text version of information on tree*/
	private String textOnTree(Tree tree, int node){
		String s="";
		if (tree.nodeIsInternal(node)){
			s+='(';
			boolean first = true;
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
					if (!first)
						s+=",";
					s+= textOnTree(tree, daughter);
			}
			s+=')';
		}
		s+="[" + textAtNode(tree, node) + "]";
		return s;
	}
	/**return a text version of information on tree*/
	private void textOnTree(Tree tree, int node, String[] nodeStrings){
		if (tree.nodeIsInternal(node)){
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
					textOnTree(tree, daughter, nodeStrings);
		}
		nodeStrings[node]= textAtNode(tree, node);
	}
	/**return a text version of information on tree, displayed on a text version of the tree*/
	public String writeOnTree(Tree tree, int node){
		if (!tree.nodeInTree(node))
			node = tree.getRoot();
		String legend = textForLegend();
		String notes = additionalText(tree, node);
		if (StringUtil.blank(notes))
			notes = legend;
		else
			notes = legend + "\n" + notes;
		boolean textAtNodes =anyText(tree, node);
		if (textAtNodes) {
			String[] nodeStrings= new String[tree.getNumNodeSpaces()];
			textOnTree(tree, node, nodeStrings);
			TextTree tt = new TextTree(tree);
			StringBuffer buff = new StringBuffer(50);
			tt.drawTreeAsText(tree, buff, nodeStrings);
			return notes + "\n\n" + buff.toString();
		}
		else if (!StringUtil.blank(notes))
			return notes;
		
		return "";
	}
	/**return a text version of information on tree, displayed as list of nodes with information at each*/
	public String infoAtNodes(Tree tree, int node){
		if (!tree.nodeInTree(node))
			node = tree.getRoot();
		String legend = textForLegend();
		String notes = additionalText(tree, node);
		if (StringUtil.blank(notes))
			notes = legend;
		else
			notes = legend + "\n" + notes;
		boolean textAtNodes =anyText(tree, node);
		if (textAtNodes) {
			String[] nodeStrings= new String[tree.getNumNodeSpaces()];
			textOnTree(tree, node, nodeStrings);
			StringBuffer buff = new StringBuffer(50);
			for (int i=0; i<nodeStrings.length; i++)
				if (!StringUtil.blank(nodeStrings[i]))
					buff.append("node " + i + ":  " + nodeStrings[i] + "\n");
					
			return notes + "\n\n" + buff.toString();
		}
		else if (!StringUtil.blank(notes))
			return notes;
		
		return "";
	}
	/**return a text version of information on tree, as a table, displayed as list of nodes with information at each*/
	public String tableAtNodes(Tree tree, int node){
		if (!tree.nodeInTree(node))
			node = tree.getRoot();
		boolean textAtNodes =anyText(tree, node);
		if (textAtNodes) {
			String[] nodeStrings= new String[tree.getNumNodeSpaces()];
			textOnTree(tree, node, nodeStrings);
			StringBuffer buff = new StringBuffer(50);
			for (int i=0; i<nodeStrings.length; i++)
				if (!StringUtil.blank(nodeStrings[i]))
					buff.append("<tab>" + nodeStrings[i]);
					
			return buff.toString();
		}
		
		return "";
	}
	/** Returns true if this extra wants the taxon to have its name underlined */
	public boolean getTaxonUnderlined(Taxon taxon){
		return false;
	}
	/** Returns the color the extra wants the taxon name colored.*/
	public Color getTaxonColor(Taxon taxon){
		return null;
	}
	/** Returns any strings to be appended to taxon name.*/
	public String getTaxonStringAddition(Taxon taxon){
		return null;
	}
	/** Returns true if this extra wants the clade to have its label underlined */
	public boolean getCladeLabelUnderlined(String label, int N){
		return false;
	}
	/** Returns the color the extra wants the clade label colored.*/
	public Color getCladeLabelColor(String label, int N){
		return null;
	}
	/** Returns any strings to be appended to clade label.*/
	public String getCladeLabelAddition(String label, int N){
		return null;
	}
	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		return "";
	}
	/**return text to be placed in legends*/
	public String textForLegend(){
		return "";
	}
	/**return any additional explanatory text, e.g. if there is extensive information too verbose for a legend but which should be output to text view*/
	public String additionalText(Tree tree, int node){
		return "";
	}

	/**Add any desired menu items to the right click popup*/
	public void addToRightClickPopup(MesquitePopup popup, MesquiteTree tree, int branch){
		//popup.addItem("Show...", ownerModule, new MesquiteCommand(null, null), "argument");
	}
	public  int findBranch(Tree tree, int drawnRoot, int x, int y){return -1;} // should be renamed; need method to tell assistant to react to mousedown that might be in its node picture etc.
	/**to inform TreeDisplayExtra that cursor has just entered branch N*/
	public void cursorEnterBranch(Tree tree, int N, Graphics g){}
	/**to inform TreeDisplayExtra that cursor has just exited branch N*/
	public void cursorExitBranch(Tree tree, int N, Graphics g){}
	/**to inform TreeDisplayExtra that cursor has just touched branch N*/
	public void cursorTouchBranch(Tree tree, int N, Graphics g, int modifiers, boolean isArrowTool){
		cursorTouchBranch(tree, N, g);
	}
	/**to inform TreeDisplayExtra that cursor has just touched branch N*/
	public void cursorTouchBranch(Tree tree, int N, Graphics g){}
	/**to inform TreeDisplayExtra that cursor has just touched the field (not in a branch or taxon)*/
	public boolean cursorTouchField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){
		return false;
	}
	/**to inform TreeDisplayExtra that cursor has just dragged in the field (not in a branch or taxon)*/
	public void cursorDragField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){}
	/**to inform TreeDisplayExtra that cursor has just dropped the field (not in a branch or taxon)*/
	public void cursorDropField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){}
	/**to inform TreeDisplayExtra that cursor has just entered name of terminal taxon M*/
	public void cursorEnterTaxon(Tree tree, int M, Graphics g){}
	/**to inform TreeDisplayExtra that cursor has just exited name of terminal taxon M*/
	public void cursorExitTaxon(Tree tree, int M, Graphics g){}
	/**to inform TreeDisplayExtra that cursor has just touched name of terminal taxon M*/
	public void cursorTouchTaxon(Tree tree, int M, Graphics g, int modifiers, boolean isArrowTool){
		cursorTouchTaxon(tree, M, g);
	}
	/**to inform TreeDisplayExtra that cursor has just touched name of terminal taxon M*/
	public void cursorTouchTaxon(Tree tree, int M, Graphics g){}
	/**to inform TreeDisplayExtra that cursor has just moved OUTSIDE of taxa or branches*/
	public void cursorMove(Tree tree, int x, int y, Graphics g){}
	
	public void addPanelPlease(Panel p){
		if (panels == null || treeDisplay == null || ownerModule == null)
			return;
		panels.addElement(p);
		treeDisplay.addPanelPlease(p);
		MesquiteWindow w = ownerModule.containerOfModule();
		if (w == null)
			return;
		w.checkPanelPositionsLegal();
		
	}
	public void removePanelPlease(Panel p){
		if (panels !=null)
			panels.removeElement(p);
		if (treeDisplay != null)
			treeDisplay.removePanelPlease(p);
	}
	protected Vector getPanels(){
		return panels;
	}

	public void turnOff() {
		if (treeDisplay==null)
			return;
		treeDisplay.removeExtra(this);
		treeDisplay.repaint();
	}
	
}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BranchInfo;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquiteMenu;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteWindow;

/* ======================================================================== */
public class BranchInfo extends TreeDisplayAssistantDI {

	public Vector extras;
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
		BranchInfoExtra newPj = new BranchInfoExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Branch Information";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Shows information about branches.";
	}
}

/* ======================================================================== */
class BranchInfoExtra extends TreeDisplayExtra implements Commandable  {
	BranchInfo infoModule;
	MesquiteTree tree;
	MesquiteCommand respondCommand;
	Image warningGif;

	public BranchInfoExtra (BranchInfo ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		warningGif = MesquiteImage.getImage(ownerModule.getPath() + "rerootWarning.gif");

		respondCommand = ownerModule.makeCommand("respond", this);
		infoModule = ownerModule;
	}
	Rectangle warningRect = new Rectangle(0,0, 18, 18);
	double dist(int x, int y, int aX, int aY){
		return Math.sqrt((x-aX)*(x-aX) + (y-aY)*(y-aY));
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		this.tree =(MesquiteTree)tree;
		warningRect.x = -1000;
		warningRect.y = -1000;

		if (tree instanceof MesquiteTree){
			MesquiteTree mTree = (MesquiteTree)tree;
			
			if (mTree.anyUpsideDownProperties()){
				int originalRoot = mTree.getOriginalRootDaughter();
				TreeDrawing td = treeDisplay.getTreeDrawing();
				int x = (int)td.x[originalRoot];
				int y = (int)td.y[originalRoot];
				if (treeDisplay.isUpDownRightLeft()){
					int aX = (int)td.x[tree.motherOfNode(originalRoot)];
					int aY = (int)td.y[tree.motherOfNode(originalRoot)];
					int pX = (x+aX)/2;
					int pY = (y+aY)/2;
					if (dist(x, y, pX, pY)<40){
						x = pX;
						y = pY;
					}
					else {
						pX = (2*x + aX)/3;
						pY = (2*y + aY)/3;
						if (dist(x, y, pX, pY)<40){
							x = pX;
							y = pY;
						}
						else {	
							pX = (3*x + aX)/4;
							pY = (3*y + aY)/4;
							if (dist(x, y, pX, pY)<40){
								x = pX;
								y = pY;
							}
						}
					}

				}
				warningRect.x = x;
				warningRect.y = y;
				g.drawImage(warningGif, x,y, treeDisplay);

			}
		}
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = (MesquiteTree)tree;
	}
	MesquitePopup popup;
	MesquiteInteger pos = new MesquiteInteger();
	void addToPopup(String s, int node, int response){
		if (popup==null)
			return;
		popup.addItem(s, ownerModule, respondCommand, Integer.toString(node) + " " + Integer.toString(response));
	}
	void respondToPopup(String s,  int responseRequested,  int response){
		if (responseRequested == response)
			ownerModule.alert(s);
	}

	/**to inform TreeDisplayExtra that cursor has just touched branch N*/
	public void cursorTouchBranch(Tree tree, int N, Graphics g){
		/*
		MesquiteTree mTree = (MesquiteTree)tree;
		if (mTree.anyUpsideDownProperties()){
			int originalRoot = mTree.getOriginalRootDaughter();
			if (N == originalRoot)
				respondCommand.doItMainThread(null, null, this);
		}
	*/
	}
	/**to inform TreeDisplayExtra that cursor has just touched the field (not in a branch or taxon)*/
	public boolean cursorTouchField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){
		if (warningRect.contains(x, y)) {
			respondCommand.doItMainThread(null, null, this);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/**Add any desired menu items to the right click popup*/
	public void addToRightClickPopup(MesquitePopup popup, MesquiteTree tree, int branchFound){
		if (branchFound>0){
			this.tree = tree;
			Taxa taxa = tree.getTaxa();
			if (tree.numberOfTerminalsInClade(tree.getRoot()) < taxa.getNumTaxa()){
			MesquiteMenu addTaxaSubmenu = new MesquiteMenu("Add Taxon Here");
			for (int i=0; i< taxa.getNumTaxa(); i++){
				if (!tree.taxonInTree(i)) {
					MesquiteMenuItem mmi = new MesquiteMenuItem(taxa.getTaxonName(i), ownerModule, new MesquiteCommand("addTaxon", this), MesquiteInteger.toString(branchFound) + " " + ParseUtil.tokenize("" + i));
					addTaxaSubmenu.add(mmi);
				}
			}
			popup.add(addTaxaSubmenu);
			}
		}
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Explains", "", commandName, "respond")) {
			MesquiteTree mTree = (MesquiteTree)tree;
			if (mTree.anyUpsideDownProperties()){
				ListableVector v = mTree.getUpsideDownProperties();
				ownerModule.discreetAlert("This tree was originally rooted along this branch. It has since been rerooted. "
						+"Some properties associated with the branches/nodes imply a direction (i.e., polarity) of time, but this rerooting violated the polarity by turning branches upside down. These properties are:\n\n" + v.getList());
			}
		}
		else if (checker.compare(this.getClass(), "Adds taxon to branch", "[branch number][taxon number]", commandName, "addTaxon")) {
			if (tree == null)
				return null;
			Parser parser = new Parser();
			int branch = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			int it = MesquiteInteger.fromString(parser.getNextToken());
			if (MesquiteInteger.isCombinable(branch) && MesquiteInteger.isCombinable(it)){
				if (!tree.taxonInTree(it)) {
					double cBL = tree.getBranchLength(branch);
					double currentHeight = tree.tallestPathAboveNode(branch);
					tree.graftTaxon(it, branch, false);
					int node = tree.nodeOfTaxonNumber(it);
					if (MesquiteDouble.isCombinable(cBL)){
						tree.setBranchLength(branch, cBL/2, false);
						tree.setBranchLength(tree.motherOfNode(branch), cBL/2, false);
						currentHeight += cBL/2;
						tree.setBranchLength(node, currentHeight, false);  //Debugg.println(" OK to do this even though arbitrary?
						tree.setAssociatedString(ColorDistribution.colorRGBNameReference, node, "#33BB00");
					}
					else
						tree.setAssociatedString(ColorDistribution.colorRGBNameReference, node, "#00AA55");
					tree.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
				}
			}
		}

		return null;
	}

	/*.................................................................................................................*/
	public void turnOff() {
		if (infoModule.extras != null)
			infoModule.extras.removeElement(this);
		super.turnOff();
	}
}





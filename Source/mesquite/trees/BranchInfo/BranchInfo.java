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
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.tree.TreeTool;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.MesquiteImage;
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
		InfoToolExtra newPj = new InfoToolExtra(this, treeDisplay);
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
class InfoToolExtra extends TreeDisplayExtra implements Commandable  {
	BranchInfo infoModule;
	Tree tree;
	MesquiteCommand respondCommand;
	Image warningGif;

	public InfoToolExtra (BranchInfo ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		warningGif = MesquiteImage.getImage(ownerModule.getPath() + "rerootWarning.gif");

		respondCommand = ownerModule.makeCommand("respond", this);
		infoModule = ownerModule;
	}
	Rectangle warningRect = new Rectangle(0,0, 27, 27);
	double dist(int x, int y, int aX, int aY){
		return Math.sqrt((x-aX)*(x-aX) + (y-aY)*(y-aY));
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		this.tree = tree;
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
		this.tree = tree;
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
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Explains", "[branch number]", commandName, "respond")) {
			MesquiteTree mTree = (MesquiteTree)tree;
			if (mTree.anyUpsideDownProperties()){
				ListableVector v = mTree.getUpsideDownProperties();
				ownerModule.discreetAlert("This tree was originally rooted along this branch. It has since been rerooted. "
						+"Some properties associated with the branches/nodes imply a polarity of time, but this rerooting violated the polarity by turning branches upside down. These properties are:\n\n" + v.getList());
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





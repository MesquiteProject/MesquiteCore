/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.DrawQuickAssociateOnTree;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayEarlyExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.StringInABox;

/* ======================================================================== */
/* A lightweight module intended for scripting only, e.g. for a simple tree window to show consensusFrequency */
public class DrawQuickAssociateOnTree extends TreeDisplayAssistantDI {
	public Vector extras;
	MesquiteBoolean showing;
	DisplayableBranchProperty property = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		showing = new MesquiteBoolean(false);
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		QuickAssociateExtra newPj = new QuickAssociateExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Quick Show Tree Associate";
	}

	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows associate", "[name][kind]", commandName, "showAssociate")) {
			if (StringUtil.blank(arguments))
				showing.setValue(false);
			else {
				String name = parser.getFirstToken(arguments);
				int kind = MesquiteInteger.fromString(parser);
				property = new DisplayableBranchProperty(name, kind);
				showing.setValue(true);
				for (int i =0; i<extras.size(); i++){
					QuickAssociateExtra e = (QuickAssociateExtra)extras.elementAt(i);
					e.setShowProperty(showing.getValue(), property);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows an associate on the tree's branches" ;
	}
	public boolean isSubstantive(){
		return false;
	}   	 
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
}

/* ======================================================================== */
class QuickAssociateExtra extends TreeDisplayExtra   {
	DrawQuickAssociateOnTree propModule;
	boolean show;
	DisplayableBranchProperty property;
	StringInABox box;
	public QuickAssociateExtra (DrawQuickAssociateOnTree ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		propModule = ownerModule;
		show = propModule.showing.getValue();
		box = new StringInABox( "", treeDisplay.getFont(),50);
	}
	private void quickDrawAssociate(Tree tree, Graphics g, int node){
		int offsetY = 4;
		int offsetX = 4;
		box.setString(property.getStringAtNode(tree, node));
		double x, y;
		int stringWidth = box.getMaxWidthMunched();
		if (treeDisplay.getOrientation() == TreeDisplay.RIGHT) {
			offsetY = treeDisplay.getTreeDrawing().getEdgeWidth()-2;
			offsetX = -stringWidth-8;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.LEFT){
			offsetY = treeDisplay.getTreeDrawing().getEdgeWidth()-2;
			offsetX = 8;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.UP){
			offsetX = treeDisplay.getTreeDrawing().getEdgeWidth()+6;
			offsetY = 4;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.DOWN){
			offsetX = treeDisplay.getTreeDrawing().getEdgeWidth()+8;
			offsetY = -box.getHeight()-8;
		}
		x= treeDisplay.getTreeDrawing().x[node] + offsetX;
		y = treeDisplay.getTreeDrawing().y[node] + offsetY;
		Shape ss = g.getClip();
		g.setClip(null);
		box.draw(g,  x, y);
		g.setClip(ss);	
	}

	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (!tree.isVisibleEvenIfInCollapsed(node))
			return;
		if (show) {
			quickDrawAssociate(tree, g, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawOnTree(tree, d, g);


		}
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int node, Graphics g) {
		drawOnTree(tree, node, g);
	}

	void setShowProperty(boolean a, DisplayableBranchProperty property){
		show = a;
		this.property = property;
		treeDisplay.pleaseUpdate(false);
	}
	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		return null;
	}
	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		return null;
	}

	/*.................................................................................................................*/
	public   void setTree(Tree tree) {

	}

	public void turnOff() {
		propModule.extras.removeElement(this);
		super.turnOff();
	}

}




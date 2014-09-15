/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.MakeRerootings;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class MakeRerootings extends TreeDisplayAssistantI {
	public String getFunctionIconPath(){
		return getPath() + "allRerootings.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		AllRerootToolExtra newPj = new AllRerootToolExtra(this, treeDisplay);
		return newPj;
	}
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return false; //as init, the citation would show up even if this had not been used
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Make Rerootings of Clade";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Presents a tool by which you can touch on a tree;  a tree block is made consisting of trees representing all rerootings of the clade of the node touched.";
	}

}

/* ======================================================================== */
class AllRerootToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool rerootingsTool;
	public AllRerootToolExtra (MesquiteModule ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		rerootingsTool = new TreeTool(this, "allRerootings", ownerModule.getPath(), "allRerootings.gif", 11,5,"Make All Rerootings of Clade", "This tool constructs all rerootings within clade of node touched");
		rerootingsTool.setTouchedCommand(MesquiteModule.makeCommand("makeAllRerootings",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(rerootingsTool);
		}
	}
	/*.................................................................................................................*/
	public   void drawNode(Tree tree, int node, Graphics g) {}
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {}
	public   void setTree(Tree tree) {}

	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Makes and adds to file a tree block of all rerootings of current tree", "[node]", commandName, "makeAllRerootings")) {
			Tree tree = treeDisplay.getTree();
			int M = MesquiteInteger.fromFirstToken(arguments, pos);
			if (tree.nodeIsUnbranchedInternal(M) || tree.nodeIsUnbranchedInternal(tree.motherOfNode(M))){
				ownerModule.discreetAlert("Sorry, you can't chose an unbranched internal node or a descendant of one for making rerootings");
				return null;
			}
			String name = MesquiteString.queryString(ownerModule.containerOfModule(), "Rerooted clade", "Name of tree block:", "Rerootings");
			if (!StringUtil.blank(name)){

				TreeVector trees = new TreeVector(tree.getTaxa());
				trees.setName(name);
				tree.makeAllRootings(M, trees);

				trees.addToFile(null, ownerModule.getProject(), null);
				ownerModule.resetAllMenuBars();
				return trees;
			}
		}
		return null;
	}
}




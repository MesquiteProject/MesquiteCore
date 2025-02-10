/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.DepContTreeWindow;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.assoc.lib.*;
import mesquite.trees.lib.*;

/* ======================================================================== */
public class DepContTreeWindow extends SimpleTreeWindowMaker  {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	protected String getMenuName(){
		return "Contained_Tree";
	}
	protected String getDefaultExplanation(){
		String defaultExplanation = "This window shows the same tree as seen in ";
		MesquiteWindow eW = getEmployer().getModuleWindow();
		if (eW !=null)
			defaultExplanation += eW.getTitle();
		else
			defaultExplanation += "a Tree Window";
		return defaultExplanation;
	}
	protected SimpleTreeWindow makeTreeWindow(SimpleTreeWindowMaker stwm, DrawTreeCoordinator dtwc){
		SimpleTreeWindow tw =  new DepCTreeWindow( this, treeDrawCoordTask);
		tw.setWindowTitle("Contained Tree");
		return tw;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Dependent Contained Tree Window";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Displays a single tree (the same as contained within a tree in a tree window)." ;
	}


}

/* ======================================================================== */
class DepCTreeWindow extends SimpleTreeWindow   {
	Taxa taxa;
	DepTreeExtra extra;
	public int highlightedBranch=0;
	public DepCTreeWindow ( SimpleTreeWindowMaker ownerModule, DrawTreeCoordinator treeDrawCoordTask){
		super(ownerModule, treeDrawCoordTask); //infobar
		extra = new DepTreeExtra(ownerModule, treeDisplay, this);
		treeDisplay.addExtra(extra);
		resetTitle();
	}
	/*_________________________________________________*/
	private String findMigrationEvents(Tree tree, int node, ObjectArray array)
	{
		String cladeReport = "";
		Object mig = array.getValue(node);
		if (mig != null){
			Vector v = (Vector)mig;
			if (v.size() >0){
				cladeReport += "\nMigration events on gene tree branch " + node + ":";
				for (int i = 0; i< v.size(); i++){
					Dimension p = (Dimension)v.elementAt(i);
					cladeReport += ("\nTo node " + p.width + " at generation " + p.height + " from top of tree");
				}
			}
		}

		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			String s = findMigrationEvents(tree, d, array);
			if (!StringUtil.blank(s))
				cladeReport += "\n" + s;
		}

		return cladeReport;
	}
	NameReference migrRef = NameReference.getNameReference("MigrationEvents");
	/*.................................................................................................................*/
	/** to be overridden by MesquiteWindows for a text version of their contents*/
	public String getTextContents() {

		String s = "";
		if (ownerModule.isDoomed())
			return"";
		if (tree != null) {
			s += "Tree: " + tree.writeTree() + "\n";
			s += "  " + treeDisplay.getTextVersion();
			ObjectArray migrated = ((Associable)tree).getAssociatedObjects(migrRef);
			if (migrated != null)
				s += findMigrationEvents(tree, tree.getRoot(), migrated);
		}
		return s;

	}

}

/* ======================================================================== */
class DepTreeExtra extends TreeDisplayExtra {
	DepCTreeWindow treeWindow;
	NameReference migrRef = NameReference.getNameReference("MigrationEvents");
	public DepTreeExtra (MesquiteModule ownerModule, TreeDisplay treeDisplay, DepCTreeWindow treeWindow) {
		super(ownerModule, treeDisplay);
		this.treeWindow = treeWindow;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		treeWindow.sizeDisplays();
	}
	public   void setTree(Tree tree) {
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

	public void cursorEnterBranch(Tree tree, int node, Graphics g){
		if (!(tree instanceof Associable))
			return;
		if (ownerModule.isDoomed())
			return;
		ObjectArray migrated = ((Associable)tree).getAssociatedObjects(migrRef);
		if (migrated != null){
			Object mig = migrated.getValue(node);
			if (mig != null){
				Vector v = (Vector)mig;
				if (v.size() ==0)
					return;
				ownerModule.logln("Migration events on gene tree branch " + node + ":");
				for (int i = 0; i< v.size(); i++){
					Dimension p = (Dimension)v.elementAt(i);
					ownerModule.logln("To node " + p.width + " at generation " + p.height + " from top of containing branch");
				}
			}
		}

	}
}



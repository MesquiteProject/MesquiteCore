/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.lib;

import mesquite.lib.tree.Tree;
import mesquite.lib.ui.ClosablePanel;
import mesquite.lib.ui.ClosablePanelContainer;


public class TreeInfoExtraPanel extends ClosablePanel {
	protected Tree tree;
	protected int node = -1;
	public TreeInfoExtraPanel(ClosablePanelContainer container, String title){
		super(container, title);
		setShowTriangle(true);
	}
	public void setTree(Tree tree){
		this.tree = tree;
	}
	public void setNode(int node){
		this.node = node;
	}
	public void taxonEnter(int it){
	}
	public void taxonExit(int it){
	}
	public void taxonTouch(int it){
	}
	public void branchEnter(int node){
		setNode(node);
	}
	public void branchExit(int node){
		setNode(-1);
	}
	public void branchTouch(int node){
		setNode(node);
	}
}


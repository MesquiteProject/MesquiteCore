/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.AllRerootings;
/*~~  */

import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.treefarm.lib.DetTreeModifier;

/* ======================================================================== */
public class AllRerootings extends DetTreeModifier {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		return true;
  	 }
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public void modifyTree(Tree original, MesquiteTree modified, int ic){
   		if (original == null || modified == null)
   			return;
   		int root = original.getRoot();
   		int numReroots = original.numberOfNodesInClade(root)-2;
   		if (ic<0 | ic>=numReroots)
   			return;
   		int r = findRootingNode(original, root, ic);
   		
   		if (!modified.nodeExists(r))
   			return;
		modified.reroot(modified.nodeInTraversal(r, root), root, false);
		modified.setName("Rooting " + (ic+1) + " of " + original.getName());
		
   	}
   	int findRootingNode(Tree original, int cladeRoot, int target){
    	 	int numReroots = original.numberOfNodesInClade(cladeRoot)-1;
		int count = -1;
 		for (int i = 1; i<=numReroots; i++) {
 			int atNode = original.nodeInTraversal(i, cladeRoot);
 			if (original.nodeIsPolytomous(cladeRoot) || original.motherOfNode(atNode)!= cladeRoot) {
 				count++;
	 			if (count == target)
	 				return i;
 			}
 		}
 		return -1;
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Tree tree) {
		if (tree ==null)
			return 0;
		else
			return  tree.numberOfNodesInClade(tree.getRoot())-2;
   	}
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "All Rerootings";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Reroots tree at all nodes.";
   	 }
   	 
}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.SortTaxaByTree;

import mesquite.lib.*;
import mesquite.lib.duties.*;

import java.net.*;

/** ======================================================================== */

public class SortTaxaByTree extends TreeUtility {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
 	}
 	
	public  void useTree(Tree treeT) {
		MesquiteTree tree = (MesquiteTree)treeT;
		if (tree == null)
			return;
		Taxa taxa = tree.getTaxa();
		UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_MOVED, taxa);
		undoInstructions.recordPreviousOrder(taxa);
		UndoReference undoReference = new UndoReference(undoInstructions, this);
		MesquiteInteger target = new MesquiteInteger();
		MesquiteInteger count = new MesquiteInteger(0);
		int numTaxa = taxa.getNumTaxa();
		for (int k = 0; k<numTaxa; k++){
			target.setValue(k);
			count.setValue(0);
			moveOne(tree, tree.getRoot(), taxa, target, count);
			tree.reconcileTaxa(MesquiteListener.PARTS_MOVED, null);
		}
		taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));

//		taxa.moveTaxa(taxon, 1, count.getValue(), false);
	}
	
	
	/** These two methods adjust the vertical positions relative to the leftmost terminal taxon.*/
	void moveOne (Tree tree, int node, Taxa taxa, MesquiteInteger target, MesquiteInteger count){
		if (target.getValue()<0)
			return;
		if (tree.nodeIsTerminal(node)){
			int taxon = tree.taxonNumberOfNode(node);
			if (count.getValue() == target.getValue()) {
				taxa.moveTaxa(taxon, 1, target.getValue()-1, false);
				target.setValue(-1);
				return;
			}
			count.increment();
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && target.getValue()>=0; d = tree.nextSisterOfNode(d))
				moveOne(tree, d, taxa, target, count);
		}
	}
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Sort Taxa By Order in Tree";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Sorts taxa to match the order in the tree.";
   	 }
   	 
}



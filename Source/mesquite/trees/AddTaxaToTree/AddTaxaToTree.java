/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.AddTaxaToTree;

import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.duties.TreeAlterer;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.ui.ListDialog;

/* ======================================================================== */
public class AddTaxaToTree extends TreeAlterer {
	public String getName() {
		return "Add Taxa To Tree";
	}
	public String getExplanation() {
		return "To a tree with some taxa excluded, adds taxa." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		Taxa taxa = tree.getTaxa();
		//counting how many excluded
		int count = 0;
		for (int i=0; i< taxa.getNumTaxa(); i++){
			if (!tree.taxonInTree(i))
				count++;
		}
		if (count == 0) {
			discreetAlert( "The tree contains all of the taxa available in the block of taxa.  There are none available to be added");
			return true;
		}
		//getting list of excluded
		ListableVector excluded = new ListableVector(count);
		count = 0;
		for (int i=0; i< taxa.getNumTaxa(); i++){
			if (!tree.taxonInTree(i)) {
				excluded.addElement(taxa.getTaxon(i), false);
			}
		}

		//presenting choice
		Listable[] toInclude = ListDialog.queryListMultiple(containerOfModule(), "Add taxa to tree", "Select taxa to be added to the tree", MesquiteString.helpString,"Add", false, excluded, null);
		if (toInclude == null || toInclude.length ==0)
			return true;

		for (int i=0; i<toInclude.length; i++){
			int taxon = taxa.whichTaxonNumber((Taxon)toInclude[i]);
			tree.graftTaxon(taxon, tree.getRoot(), false);
		}
		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
		return true;
	}
}


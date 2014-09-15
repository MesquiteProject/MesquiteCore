/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;


/* ======================================================================== */
/** This evaluates a gene tree by calculating how much lineage sorting is implied by a containing species tree. */
public abstract class GeneTreeFit extends NumForGeneTInSpeciesT {
   	 
   	MesquiteTree t;
	MesquiteString r = new MesquiteString();
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || tree == null)
    	 		return;
    	clearResultAndLastResult(result);
		lastTree = tree;
		Taxa taxa = tree.getTaxa();
        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
        		association = associationTask.getCurrentAssociation(taxa); 
        		if (association == null)
        			association = associationTask.getAssociation(taxa, 0); 
        		if (association == null){
				if (resultString!=null)
					resultString.setValue("Deep coalescence (gene tree) not calculated (no association )");
				return;
			}
        		if (association.getTaxa(0)== taxa)
        			containingTaxa = association.getTaxa(1);
        		else
        			containingTaxa = association.getTaxa(0);
        	}
        	/*tree coming in is gene tree, hence need to find out for each taxon in gene tree if it has more than one associate.  If so, then 
        	can't do calculations since gene copy in more than one species*/
		for (int i=0; i< taxa.getNumTaxa(); i++){
			Taxon tax = taxa.getTaxon(i);
			if (association.getNumAssociates(tax)>1){
				if (resultString!=null)
					resultString.setValue("Deep coalescence not calculated (some genes in more than one species)");
				return;
			}
		}
		
		//get containing tree
		Tree containingTree = ((OneTreeSource)treeSourceTask).getTree(containingTaxa); 
		
		//cloning the contained tree in case we need to change its resolution
	        if (t==null || t.getTaxa()!=taxa || !(tree instanceof MesquiteTree))
	        	t = tree.cloneTree();
	        else
	        	((MesquiteTree)t).setToClone((MesquiteTree)tree);
	        	
	        	
		//calculate deep coalescence cost
	    calculateCost(reconstructTask, containingTree, t, association, result, r);
		lastContaining = containingTree;
		if (resultString!=null)
			resultString.setValue(r.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	public abstract void calculateCost(ReconstructAssociation reconstructTask, Tree speciesTree, MesquiteTree geneTree, TaxaAssociation association, MesquiteNumber result, MesquiteString r);
   
	public boolean biggerIsBetter() {
		return false;
	}
}


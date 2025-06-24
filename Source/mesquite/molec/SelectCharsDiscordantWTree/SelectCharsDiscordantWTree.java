/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.SelectCharsDiscordantWTree;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.consensus.lib.Bipartition;
import mesquite.consensus.lib.BipartitionVector;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;

/* ======================================================================== */
public class SelectCharsDiscordantWTree extends CharacterSelector {
	OneTreeSource oneTreeSourceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (oneTreeSourceTask == null) {
			oneTreeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of containing tree");
		}
		return oneTreeSourceTask != null;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

	/*-----------------------------------------*/
	private void harvestPartition(Tree tree, int node, int targetNode, boolean inTarget, Bits bitsIn, Bits bitsOut) {
		if (node == targetNode)
			inTarget = true;
		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				harvestPartition(tree, d, targetNode, inTarget, bitsIn, bitsOut);
		}
		else {
			int n = tree.taxonNumberOfNode(node);
			if (inTarget)
				bitsIn.setBit(n);
			else
				bitsOut.setBit(n);
		}
	}
	int target = 0;
	private void harvestPartitions(Tree tree, int node, Bits[][] partitions) {
		if (tree.nodeIsInternal(node)) {
			if (node != tree.getRoot()) {
				harvestPartition(tree, tree.getRoot(), node, false, partitions[target][0], partitions[target][1]);
			target++;
			}
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				harvestPartitions(tree, d, partitions);
		}

	}

	private boolean discordantWithBipartition(CategoricalData data, int ic, Bits bitsIn, Bits bitsOut){
		long inStates = 0;
		long outStates = 0;
		for (int it = 0; it<data.getNumTaxa(); it++) {
			long s = data.getState(ic, it);
			if (CategoricalState.isCombinable(s) && !CategoricalState.isUncertain(s)) {
				if (bitsIn.isBitOn(it)) //in the clade
					inStates = CategoricalState.union(inStates, s);
				else if (bitsOut.isBitOn(it))
					outStates = CategoricalState.union(outStates, s);
				//could check and bail but might cost more to check
			}
		}
		long overlap = CategoricalState.intersection(inStates, outStates);
		// If they overlap, but that's Ok as long as overlap is in only one state 
		if (CategoricalState.cardinality(overlap)>1) 
			return true;
		return false;
	}

	/** Called to select characters*/
	public void selectCharacters(CharacterData data){
		if (data!=null && data.getNumChars()>0){
			if (!(data instanceof CategoricalData)) {
				discreetAlert("Sorry, you can select with this way only for categorical or molecular data");
				return;
			}
			CategoricalData categData = (CategoricalData)data;
			Taxa taxa = data.getTaxa();
			oneTreeSourceTask.initialize(taxa);
			Tree tree = oneTreeSourceTask.getTree(taxa);
			if (tree != null) {
				data.deselectAll();
				Bits[][] partitions = new Bits[tree.numberOfInternalsInClade(tree.getRoot())][2];
				for (int i = 0; i<partitions.length; i++)
					for (int k =0; k<2; k++)
						partitions[i][k] = new Bits(taxa.getNumTaxa());
				target = 0;
				harvestPartitions(tree, tree.getRoot(), partitions);
				for (int i=0; i<partitions.length; i++) {
					Bits bitsIn = partitions[i][0];//we have a bipartition in the tree; now to deselect any characters incompatible with it
					Bits bitsOut = partitions[i][1];
					for (int ic = 0; ic<data.getNumChars(); ic++) {
						if (discordantWithBipartition(categData, ic, bitsIn, bitsOut))
							data.setSelected(ic, true);
					}
				}
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Discordant with Tree";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Selects characters discordant with the tree (i.e. as unordered characters they would have homoplasy on the tree)." ;
	}

}



/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.EquiprobableTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Creates a random tree such that each distinct labelled topology is equally probable. */
public class EquiprobableTrees extends TreeSimulate {
	RandomBetween randomTaxon;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		randomTaxon= new RandomBetween(1);
 		return true;
  	 }
  	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		return MesquiteInteger.infinite;
   	}
	/*.................................................................................................................*/
   	public Tree getSimulatedTree(Taxa taxa, Tree tree, int treeNumber, ObjectContainer extra, MesquiteLong seed) {
   	//save random seed used to make tree under tree.seed for use in recovering later
  			randomTaxon.setSeed(seed.getValue());
  			if (tree==null || !(tree instanceof MesquiteTree))
  				 tree = new MesquiteTree(taxa);
			((MesquiteTree)tree).setToDefaultBush(2, false);
			int whichNode;
			
			for (int taxon = 2; taxon < taxa.getNumTaxa(); taxon++) {
				whichNode=randomTaxon.randomIntBetween(0, tree.numberOfNodesInClade(tree.getRoot())-1);
				((MesquiteTree)tree).graftTaxon(taxon, tree.nodeInTraversal(whichNode), false);
			}

			seed.setValue(randomTaxon.nextLong());  //see for next time
	   		return tree;
   	}
   
   	public void initialize(Taxa taxa){
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Equiprobable Trees";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Generates trees randomly so that each possible labelled tree topology is equally likely." ;
   	 }
	/*.................................................................................................................*/
}


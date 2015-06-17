/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.
 
 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org
 
 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.SetToArbitrarySymmetrical;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class SetToArbitrarySymmetrical extends TreeAltererMult {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
  	 void formSymmetricalClade(AdjustableTree tree, int minTaxon, int maxTaxon){
		int range = maxTaxon-minTaxon + 1;
		if (range > 1) {
			int newRight = minTaxon + range/2;
			tree.splitTerminal(minTaxon, newRight, false);
			formSymmetricalClade(tree, minTaxon, newRight -1);
			formSymmetricalClade(tree, newRight, maxTaxon);
		}
  	 }
	/*.................................................................................................................*/
	public  boolean  transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (tree==null)
			return false;
		Taxa taxa = tree.getTaxa();
		int numTaxa = taxa.getNumTaxa();
		if (numTaxa == 1){
			tree.setToDefaultBush(1, false);
			return true;
		}
		tree.setToDefaultBush(2, false);
		int secondHalf = numTaxa/2;
		int rightNode = tree.nextSisterOfNode(tree.firstDaughterOfNode(tree.getRoot()));
		tree.setTaxonNumber(rightNode, secondHalf, false);
		formSymmetricalClade(tree, 0, secondHalf-1);
		formSymmetricalClade(tree, secondHalf, tree.getTaxa().getNumTaxa()-1);
		tree.setName("Default symmetrical");
		return true;
		
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 304;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Set to Arbitrary Symmetrical";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sets the tree to an arbitrary symmetrical tree." ;
	}
}


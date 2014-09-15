/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.CollessImbalance;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.*;

public class CollessImbalance extends NumberForTree {
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
	
	/** Calculates asymmetry*/
	private double asymmetry(Tree tree, int node) {
		double asymm = 0;
		if (tree.nodeIsInternal(node)) { 
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
	  			asymm += asymmetry(tree, daughter);
  	 		if (!tree.nodeIsPolytomous(node)){
	  	 		double numLeft =tree.numberOfTerminalsInClade(tree.firstDaughterOfNode(node));
	   	 		double numRight =tree.numberOfTerminalsInClade(tree.lastDaughterOfNode(node));
	 	 		if (numRight>numLeft)
	 	 			asymm +=(numRight-numLeft);
	 	 		else
	 	 			asymm +=(numLeft-numRight);
 	 		}
 	
  		}
  		return asymm;
  	 }
  	 
	/** (called in case this module needs to initialize anything; this module doesn't) */
   	public void initialize(Tree tree){
   	}
   	
	/** The key method of ObjFcnForTree modules have, called to return a number*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || tree == null)
    	 		return;
    	clearResultAndLastResult(result);
		double asymm = asymmetry(tree, tree.getRoot());
		int n = tree.numberOfTerminalsInClade(tree.getRoot());
		asymm = asymm*2.0/((n-1)*(n-2)); //Colless indicated to divide by (n(n-3)+1)/2 but that's in error: it is not the maximum asymmetry
		result.setValue(asymm);
		if (resultString!=null)
			resultString.setValue("Colless's Imbalance: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	
	/** Explains what the module does.*/
    	 public String getExplanation() {
		return "Calculates Colless's Imbalance statistic for tree asymmetry, normalized by maximum asymmetry.  Polytmous nodes are ignored in the calculation.";
   	 }
   	 
	/** Returns the name of the module in very short form, for use for column headings and other constrained places.*/
 	public String getVeryShortName(){
 		return "Imbalance";
 	}
 	
   	public boolean isPrerelease(){
   		return false;
   	}
	/** Name of module*/
    	 public String getName() {
		return "Colless's Imbalance";
   	 }

   	 public boolean showCitation(){
   	 	return true;
   	 }
}


/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.AvgTermBrLengthForTaxon;
/*~~  */

import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.NumberArray;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.treefarm.lib.NForTaxonWithTrees;

/** ======================================================================== */
/*
Average terminal branch length — over a set of trees,calculates the average terminal branch length for a taxon

*/

public class AvgTermBrLengthForTaxon extends NForTaxonWithTrees {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
 	}
 	
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
 	/*.................................................................................................................*/
	public void calculateNumbers(Taxa taxa, NumberArray results, MesquiteString resultsString){
		if (results==null || taxa == null)
			return;
		int numTaxa = taxa.getNumTaxa();
		results.resetSize(numTaxa);
	   	clearResultAndLastResult(results);
		results.zeroArray();
		
		int numTrees = getNumTrees(taxa);
		
		MesquiteNumber b = new MesquiteNumber();
		double[] sumBrLengths = new double[numTaxa];
		int[] numBrLengths = new int[numTaxa];

		
		
		for (int kt = 0; kt< numTrees; kt++){ //get tree for comparison
				Tree tree = getTree(taxa, kt);
				if (tree != null) {
					for (int itaxon = 0; itaxon<numTaxa; itaxon++){
							int node = tree.nodeOfTaxonNumber(itaxon);
							if (node>0) { //taxon is in tree
								if (!tree.branchLengthUnassigned(node)) {
									sumBrLengths[itaxon]+= tree.getBranchLength(node);
									numBrLengths[itaxon]++;
								}
							}
								
					}
				}
			}

		for (int itaxon = 0; itaxon<numTaxa; itaxon++){
			if (numBrLengths[itaxon]==0)
				results.setToUnassigned(itaxon);
			else
				results.setValue(itaxon, sumBrLengths[itaxon]/numBrLengths[itaxon]); 
		}
		String s = getVeryShortName() + " calculated";
		
		if (resultsString != null)
				resultsString.setValue(s);

		saveLastResult(results);
		saveLastResultString(resultsString);
	}
	
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "Avg. Terminal Br. Length";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Average Terminal Branch Length Among Trees";
   	 }
	/*.................................................................................................................*/
  	 public String getVersion() {
		return null;
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates for each taxon the average terminal branch length it as among a set of trees";
   	 }
   	 
}



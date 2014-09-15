/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.DeepCoalescencesSp;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;


/* ======================================================================== */
/** This evaluates a species tree by calculating how much lineage sorting is implied by a contained gene tree. */
public class DeepCoalescencesSp extends SpeciesTreeFit  {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
//		superStartJob(arguments, condition,  hiredByName);
		return true;
  	 }
 	   	/*.................................................................................................................*/
 	public void calculateCost(ReconstructAssociation reconstructTask, Tree speciesTree, MesquiteTree geneTree, TaxaAssociation association, MesquiteNumber result, MesquiteString r){
	        if (result != null){
	        	AssociationHistory history = reconstructTask.reconstructHistory(speciesTree, geneTree, association, result, r);
	        }
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
    	 public String getExplanation() {
		return "Counts W. Maddison's (1997) number of extra gene lineages (\"deep coalescences\") for species tree implied by a contained gene tree";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Deep Coalescences (species tree)";
   	 }
}


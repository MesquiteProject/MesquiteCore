/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import mesquite.lib.MesquiteModule;
import mesquite.lib.ResultCodes;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;


/* ======================================================================== */
/**This is superclass of modules to alter a data matrix.*/

public abstract class TaxonMerger extends MesquiteModule  {
	protected Taxa taxa;
	
   	public Class getDutyClass() {
   	 	return TaxonMerger.class;
   	 }
 	public String getDutyName() {
 		return "Taxon Merger";
   	}
   	
   	
	/*.................................................................................................................*/
   	/** A stub method for the employer to ask to query the user about options.*/
	public abstract boolean queryOptions(Taxa taxa, boolean[] toBeMerged, boolean formTaxonName, String dialogTitle, boolean permitRetainOriginals);

	/*.................................................................................................................*/
   	/** Called to merge the set of taxa indicated by the boolean array. If a bit is set, that is one of the taxa to be merged.  */
   	public abstract int mergeTaxa(Taxa taxa, boolean[] toBeMerged, String taxonName, StringBuffer report);
   	/*		return ResultCodes.NO_CHANGE;
   		return ResultCodes.SUCCEEDED;
   */


   	public boolean isSubstantive(){
   		return true;  
   	}
}






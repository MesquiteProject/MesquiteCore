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

public abstract class TaxonNameAlterer extends MesquiteModule  {
	protected MesquiteTable table;
	protected Taxa taxa;
	
   	public Class getDutyClass() {
   	 	return TaxonNameAlterer.class;
   	 }
 	public String getDutyName() {
 		return "Taxon Name Alterer";
   	}
   	
   	
	/*.................................................................................................................*/
   	/** Called to alter the taxon names in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(Taxa taxa, int it){
   		return false;
   	}
   	
	/*.................................................................................................................*/
   	/** A stub method for querying the user about options. If alterIndividualTaxonNames is used to 
   	alter the names, and options need to be specified for the operation, then optionsQuery should be overridden.*/
 	public  boolean getOptions(Taxa taxa, int firstSelected){
   		return true;
   	}

	/*.................................................................................................................*/
   	/** A stub method for doing any necessary cleanup after taxon names have been altered.*/
   	public void cleanupAfterAlterTaxonNames(){
   	}
	/*.................................................................................................................*/
   	/** Called to alter taxon names in table. This is used if the altering procedure can be done on one name
   	at a time, independent of all other name.  If the altering procedure involves dependencies between names,
   	then alterTaxonNames should be overridden with a method that uses another procedure.  */
   	public int alterIndividualTaxonNames(Taxa taxa, MesquiteTable table){
   		if (taxa==null)
   			return -1;
   		int first = 0;
   		if (table!=null) {
   			first = table.firstRowNameSelected();
   			if (first<0)
   				first = table.firstRowSelected();   		
   			if (first<0)
   				first = 0;
   		}
   		this.taxa = taxa;
   		this.table = table;
   	
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Asking to change taxon names"))
			if (!getOptions(taxa, first)) {
				return ResultCodes.USERCANCELONINITIALIZE;
			}
			
		boolean anyChanged = false;
		
		boolean okDoIt = !taxa.anySelected();
		if (table != null)
			okDoIt = okDoIt && !table.anythingSelected();
		for (int it=0; it<taxa.getNumTaxa(); it++){
			
			if ((okDoIt || taxa.getSelected(it) || table.isRowSelected(it) || table.isRowNameSelected(it)))
				if (alterName(taxa, it))	
					anyChanged = true;
		}
   		if (! anyChanged)
   			return ResultCodes.NO_CHANGE;
   		return ResultCodes.SUCCEEDED;
   	}

	/*.................................................................................................................*/
   	/** Called to alter taxon names in those cells selected in table.  Returns true if any taxon names are altered.
   	This should be overridden if the is doing something that involves dependencies between names.*/
   	public int alterTaxonNames(Taxa taxa, MesquiteTable table){
    	int altered = alterIndividualTaxonNames(taxa, table);
   		if (altered>=0)
   			cleanupAfterAlterTaxonNames();
  	
   		return altered;
   	}

   	public boolean isSubstantive(){
   		return false;  
   	}
}






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

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lists.lib.ListLVModule;


/* ======================================================================== */
/**This is superclass of modules to alter a data matrix.*/

public abstract class ListableNameAlterer extends MesquiteModule  {
	protected MesquiteTable table;
	protected ListableVector elements;
	
   	public Class getDutyClass() {
   	 	return ListableNameAlterer.class;
   	 }
 	public String getDutyName() {
 		return "Listable Name Alterer";
   	}
   	
   	//ADAPTED from taxon names alterer, and not all methods may yet be used.
	/*.................................................................................................................*/
   	/** Called to alter the element names in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(ListableVector elements, int it){
   		return false;
   	}
   	
	/*.................................................................................................................*/
   	/** A stub method for querying the user about options. If alterIndividualElementNames is used to 
   	alter the names, and options need to be specified for the operation, then optionsQuery should be overridden.*/
 	public  boolean getOptions(ListableVector elements, int firstSelected){
   		return true;
   	}
 	
 	protected String getElementNameSingular(){
   		ListLVModule lvModule = (ListLVModule)findEmployerWithDuty(ListLVModule.class);
   		return lvModule.getElementNameSingular();
	}
 	protected String getElementNamePlural(){
   		ListLVModule lvModule = (ListLVModule)findEmployerWithDuty(ListLVModule.class);
   		return lvModule.getElementNamePlural();
	}
	/*.................................................................................................................*/
   	/** A stub method for doing any necessary cleanup after element names have been altered.*/
   	public void cleanupAfterAlterElementNames(){
   	}
	/*.................................................................................................................*/
   	/** Called to alter element names in table. This is used if the altering procedure can be done on one name
   	at a time, independent of all other name.  If the altering procedure involves dependencies between names,
   	then alterElementNames should be overridden with a method that uses another procedure.  */
   	public boolean alterIndividualElementNames(ListableVector elements, MesquiteTable table){
   		if (elements==null)
   			return false;
   		int first = 0;
   		if (table!=null) {
   			first = table.firstRowNameSelected();
   			if (first<0)
   				first = table.firstRowSelected();   		
   			if (first<0)
   				first = 0;
   		}
   		this.elements = elements;
 		this.table = table;
   	
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Asking to change " + getElementNameSingular() + " names"))
			if (!getOptions(elements, first))
				return false;
			
		boolean anyChanged = false;
		
		boolean okDoIt = !elements.anySelected();
		if (table != null)
			okDoIt = okDoIt && !table.anythingSelected();
		for (int it=0; it<elements.size(); it++){
			
			if ((okDoIt || elements.getSelected(it) || table.isRowSelected(it) || table.isRowNameSelected(it)) && alterName(elements, it))	
				anyChanged = true;
		}
   		return anyChanged;
   	}

	/*.................................................................................................................*/
   	/** Called to alter element names in those cells selected in table.  Returns true if any element names are altered.
   	This should be overridden if the is doing something that involves dependencies between names.*/
   	public boolean alterElementNames(ListableVector elements, MesquiteTable table){
    	boolean altered = alterIndividualElementNames(elements, table);
   		if (altered)
   			cleanupAfterAlterElementNames();
   		return altered;
   	}

   	public boolean isSubstantive(){
   		return false;  
   	}
}






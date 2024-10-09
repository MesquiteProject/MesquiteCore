package mesquite.lists.lib;

import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Taxa;
import mesquite.lib.TaxaSelectionSet;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public abstract class TaxonsetsListUtility extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return TaxonsetsListUtility.class;
   	 }
 	public String getDutyName() {
 		return "Taxonsets list utility";
   	 }

   	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
   	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
   	public abstract boolean operateOnTaxa(Taxa taxa, MesquiteTable table);
}
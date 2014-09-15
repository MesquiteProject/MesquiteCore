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


/* ======================================================================== */
/**Supplies taxa.*/

public abstract class TaxonSource extends MesquiteModule implements ItemsSource  {
   	 public Class getDutyClass() {
   	 	return TaxonSource.class;
   	 }
 	public String getDutyName() {
 		return "Taxon Source";
   	 }
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#StoredTaxa"};
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Taxa taxa);


   	 /**Returns first tree, and sets current tree number to 0*/
   	public abstract Taxon getFirstTaxon(Taxa taxa);
   	
   	 /**Returns Taxon number iTaxon, and sets current Taxon number to iTaxon*/
   	public abstract Taxon getTaxon(Taxa taxa, int iTaxon);
   	
   	 /**Increments current Taxon number and returns that Taxon.*/
   	public abstract Taxon getNextTaxon(Taxa taxa);
   	
   	 /**Returns number of Taxons available.  If Taxons can be supplied indefinitely, returns MesquiteInteger.infinite*/
   	public abstract int getNumberOfTaxa(Taxa taxa);
   	/** returns name of item ic*/
   	public abstract String getTaxonName(Taxa taxa, int ic);

	/*===== For ItemsSource interface ======*/
   	/** returns item numbered ic*/
   	public Object getItem(Taxa taxa, int ic){
   		return getTaxon(taxa, ic);
   	}
   	/** returns number of characters for given Taxa*/
   	public int getNumberOfItems(Taxa taxa){
   		return getNumberOfTaxa(taxa);
   	}
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName(){
   		return "Taxon";
   	}
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural(){
   		return "Taxa";
   	}
   	
   	public Selectionable getSelectionable(){
   		return null;
   	}

   	public boolean isSubstantive(){
   		return false;  
   	}

  	ObjectSpecsSet partition;
  	/** zzzzzzzzzzzz*/
     	public void setEnableWeights(boolean enable){
    	}
  	public boolean itemsHaveWeights(Taxa taxa){
   		return false;
   	}
   	public double getItemWeight(Taxa taxa, int ic){
   		return MesquiteDouble.unassigned;
   	}
   	public void prepareItemColors(Taxa taxa){
		if (taxa==null)
			partition=null;
		else
			partition = (ObjectSpecsSet)taxa.getCurrentSpecsSet(TaxaPartition.class);
   	}
  	/** zzzzzzzzzzzz*/
   	public Color getItemColor(Taxa taxa, int ic){
   		if (partition==null || taxa == null)
   			return null;
		TaxaGroup mi =(TaxaGroup)partition.getProperty(ic);
		if (mi!=null && mi.getColor()!=null)
			return (mi.getColor());
		return null;
   	}
 	/** zzzzzzzzzzzz*/
   	public String getItemName(Taxa taxa, int ic){
   		return getTaxonName(taxa, ic);
   	}
  }






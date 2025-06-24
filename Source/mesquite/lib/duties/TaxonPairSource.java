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

import java.awt.Color;

import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteModule;
import mesquite.lib.ObjectSpecsSet;
import mesquite.lib.Selectionable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxonPair;


/* ======================================================================== */
/**Supplies taxa.*/

public abstract class TaxonPairSource extends MesquiteModule implements ItemsSource  {
   	 public Class getDutyClass() {
   	 	return TaxonPairSource.class;
   	 }
 	public String getDutyName() {
 		return "Taxon Pair Source";
   	 }
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#StoredTaxonPairs"};
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Taxa taxa);


   	 /**Returns TaxonPair number iPair, and sets current Taxon number to iPair*/
   	public abstract TaxonPair getTaxonPair(Taxa taxa, int iPair);
   	
   	 /**Returns number of Taxons available.  If Taxons can be supplied indefinitely, returns MesquiteInteger.infinite*/
   	public abstract int getNumberOfTaxonPairs(Taxa taxa);
   	/** returns name of item ic*/
   	public abstract String getTaxonPairName(Taxa taxa, int ic);

	/*===== For ItemsSource interface ======*/
   	/** returns item numbered ic*/
   	public Object getItem(Taxa taxa, int ic){
   		return getTaxonPair(taxa, ic);
   	}
   	/** returns number of pairs for given Taxa*/
   	public int getNumberOfItems(Taxa taxa){
   		return getNumberOfTaxonPairs(taxa);
   	}
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName(){
   		return "Taxon Pair";
   	}
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural(){
   		return "Taxon Pairs";
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
		partition=null;
  	}
  	/** zzzzzzzzzzzz*/
   	public Color getItemColor(Taxa taxa, int ic){
   		return null;
   	}
  	/** zzzzzzzzzzzz*/
   	public String getItemName(Taxa taxa, int ic){
   		return getTaxonPairName(taxa, ic);
   	}
 }






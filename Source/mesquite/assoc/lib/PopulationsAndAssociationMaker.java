/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.assoc.lib;

import mesquite.lib.MesquiteModule;
import mesquite.lib.ObjectContainer;
import mesquite.lib.taxa.Taxa;


/* ======================================================================== */
/**Supplies TaxaAssociation's for instance from a file or simulated.*/

public abstract class PopulationsAndAssociationMaker extends MesquiteModule  {

	public Class getDutyClass() {
   	 	return PopulationsAndAssociationMaker.class;
   	 }
 	public String getDutyName() {
 		return "Population and Association Maker";
   	 }
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#PopulationsFromTaxonNames"};
   	 }

   	
  	 /**Makes a set of populations to contain those geneTraxa; places new taxa block in ObjectContainer*/
   	public abstract TaxaAssociation makePopulationsAndAssociation(Taxa genesTaxa, ObjectContainer populationTaxaContainer);
   	
   
}



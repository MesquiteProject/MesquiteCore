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

import mesquite.lib.MesquiteModule;
import mesquite.lib.taxa.Taxa;


/* ======================================================================== */

public abstract class TaxonNameStyler extends MesquiteModule  {
	
   	 public Class getDutyClass() {
   	 	return TaxonNameStyler.class;
   	 }
    public String[] getDefaultModule() {
       	return new String[] { "#NoColorForTaxon"};
  }
	public String getDutyName() {
 		return "Supplier of color for taxon name";
   	 }
	public void initialize(Taxa taxa){
	}
	
	/*Use this to survey all the taxa as needed, e.g. for minima and maxima.
	 * This is called before a lot of requests for styles for individual taxa are made. 
	 * This is a bit dangerous. because the data could be stale because of multithreading.*/
	public void prepareToStyle(Taxa taxa){
	}
	
	public String getObjectComment(Object obj){
		return null;
	}
	public Color getTaxonNameColor(Taxa taxa, int it){
		return Color.red;
	}
	public boolean getTaxonNameBoldness(Taxa taxa, int it){
		return false;
	}

}



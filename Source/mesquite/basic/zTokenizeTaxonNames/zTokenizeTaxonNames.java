/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.zTokenizeTaxonNames;

import mesquite.lib.StringUtil;
import mesquite.lib.duties.TaxonNameAlterer;
import mesquite.lib.taxa.Taxa;

/* ======================================================================== */
public class zTokenizeTaxonNames extends TaxonNameAlterer {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	public boolean getOptions(Taxa taxa, int firstSelected){
   		return true;
   	}
	public boolean requestPrimaryChoice() {
		return false;
	}
	/*.................................................................................................................*/
   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(Taxa taxa, int it){
   		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);
		
		if (name!=null){
			taxa.setTaxonName(it, StringUtil.tokenize(name), false);
			nameChanged = true;
		}
		return nameChanged;
   	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Tokenize Names...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Tokenize Names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Tokenizes names.  This can be useful for reading trees produced from exported files.";
   	 }
}


	



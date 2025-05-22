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
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaBlock;


/* ======================================================================== */
/** Manages sets of taxa.  Also reads and writes TAXA NEXUS block.  Example module:
"Manage TAXA blocks" (class ManageTaxa)*/

public abstract class TaxaManager extends FileElementManager   {
	public boolean getSearchableAsModule(){
		return false;
	}

   	 public Class getDutyClass() {
   	 	return TaxaManager.class;
   	 }
 	public String getDutyName() {
 		return "Manager of sets of taxa, including read/write TAXA block";
   	 }
	
   	public boolean isSubstantive(){
   		return false;  
   	}
	/*.................................................................................................................*/
	/** make new Taxa object with given name and number of taxa*/
	public abstract Taxa makeNewTaxaBlock(String name, int numTaxa, boolean userQuery);
	public Class getElementClass(){
		return Taxa.class;
	}
	public abstract String getTaxaBlock(Taxa taxa, TaxaBlock tB, MesquiteFile file);
	public abstract MesquiteModule getListOfTaxaModule(Taxa taxa, boolean show);
	
	public abstract Taxa quietMakeNewTaxaBlock(int numTaxa);
	public abstract Taxa createTaxaBlockBasedOnPartition(Taxa baseTaxa);

}



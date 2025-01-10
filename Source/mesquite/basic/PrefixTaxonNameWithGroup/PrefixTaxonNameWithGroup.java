/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.PrefixTaxonNameWithGroup;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaPartition;

/* ======================================================================== */
public class PrefixTaxonNameWithGroup extends TaxonNameAlterer {
	TaxaPartition currentPartition=null;
	TaxaGroup tg = null;
	Taxa currentTaxa = null;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/
	public boolean getUserChooseable(){
		return true; 
	}
	/*.................................................................................................................*/
   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(Taxa taxa, int it){
   		
    	if (taxa!=currentTaxa){
    		currentTaxa=taxa;
    		currentPartition=null;
    	}
		if (currentPartition==null)
			currentPartition = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
		if (currentPartition!=null){
			tg = currentPartition.getTaxaGroup(it);
		}
		if (tg==null) return false;
		
    	
   		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);
		if (name!=null){
			String groupName = tg.getName();
			if (StringUtil.notEmpty(groupName)){
					taxa.setTaxonName(it, groupName + "." + name, false);
			}
			nameChanged = true;
		}
		return nameChanged;
   	}
	/*.................................................................................................................*
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Appends numbers to taxon names", "[length]", commandName, "appendNumbers")) {
	   	 		if (taxa !=null){
	   	 			alterTaxonNames(taxa,table);
	   	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Prefix with Taxon Group Name";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Prefixes the taxon name with the taxon group name.";
   	 }
 	/*.................................................................................................................*/
 	public boolean isPrerelease(){
 		return false;  
 	}
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return 300;  
 	}
}


	


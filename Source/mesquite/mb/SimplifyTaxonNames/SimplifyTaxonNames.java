/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.mb.SimplifyTaxonNames;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class SimplifyTaxonNames extends TaxonNameAlterer {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	/** A stub method for doing any necessary cleanup after taxon names have been altered.*/
   	public void cleanupAfterAlterTaxonNames(){
    	}
	/*.................................................................................................................*/
   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(Taxa taxa, int it){
//		String s=StringUtil.deletePunctuationAndSpaces(name);
 		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);
		if (name!=null){
			String modifiedName = StringUtil.simplifyIfNeededForOutput(name,true, true);
			if (!modifiedName.equalsIgnoreCase(name)) {
				taxa.setTaxonName(it, modifiedName, false);
				nameChanged = true;
			}
		}
		return nameChanged;
   	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Simplifies taxon names", "[length]", commandName, "simplify")) {
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
		return "Simplify Taxon Names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Simplifies taxon names so that they will work with MrBayes, RAxML, etc..";
   	 }
 	/*.................................................................................................................*/
  	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
  	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
  	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
     	public int getVersionOfFirstRelease(){
     		return 273;  
     	}
     	/*.................................................................................................................*/
     	public boolean isPrerelease(){
     		return false;
     	}

}


	


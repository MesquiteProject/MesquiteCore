/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.thinkTrees.AlphabeticTaxonNames;

import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Taxa;
import mesquite.lib.duties.TaxonNameAlterer;

public class AlphabeticTaxonNames extends TaxonNameAlterer {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
   		int count = 0;
		return true;
	}
	/*.................................................................................................................*/
   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(Taxa taxa, int it){
		int count = 0;
		int block = it /26;	
		String suffix = "";
		if (block>0) 
			suffix+=block;
		int AValue = 'A';
		int letterValue = AValue+ it % 26;
		
   		char c = (char)letterValue;
   		String s = "" + c+suffix;
		taxa.setTaxonName(it, s, false);
		return true;
   	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Alphabetic Taxon Names";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Alphabetic taxon names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Assigns as the taxon names the roman alphabet in sequence, with Z followed by A1 etc..";
   	 }
  	 
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return 330;  
 	}

}



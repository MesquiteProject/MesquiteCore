/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.FindMissing;
/*~~  */

import mesquite.lib.MesquiteInteger;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.molec.lib.FindSequenceCriterionG;

/* ======================================================================== */
public class FindMissing extends FindSequenceCriterionG {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	public boolean showOptions(CharacterData data, MesquiteTable table){
		return true;
	}
   	public boolean findNext(CharacterData data, MesquiteTable table, MesquiteInteger charFound, MesquiteInteger length, MesquiteInteger taxonFound) {
  			length.setValue(0);
  			int firstTaxon = taxonFound.getValue();
  			int firstChar = charFound.getValue();
  			int numTaxa = data.getNumTaxa();
  			int numChars = data.getNumChars();
			for (int it = firstTaxon; it< numTaxa; it++){
	   			for (int ic = firstChar; ic< numChars; ic++) {
	   				if (data.isUnassigned(ic, it)){
  						charFound.setValue(ic);
  						taxonFound.setValue(it);
  						length.setValue(1);
  						return true;
	   				}
   				}
   			}
   			return false;
   	}


	/*.................................................................................................................*/
    	 public String getName() {
		return "Find Missing";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Finds the next occurrence of missing data in a matrix of molecular data." ;
   	 }
	public boolean showOptions(boolean findAll, CharacterData data, MesquiteTable table) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return  300;  
	}

}


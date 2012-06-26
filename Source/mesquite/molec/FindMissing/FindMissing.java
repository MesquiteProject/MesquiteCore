package mesquite.molec.FindMissing;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class FindMissing extends FindSequenceCriterionG {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return true;
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
		return  NEXTRELEASE;  
	}

}


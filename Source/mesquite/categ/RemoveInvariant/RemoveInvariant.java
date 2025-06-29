/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.RemoveInvariant;
/*~~  */

import mesquite.categ.lib.CategoricalData;
import mesquite.lib.Bits;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererWholeCharacterAddRemove;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAlterer;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public class RemoveInvariant extends DataAlterer  implements AltererWholeCharacterAddRemove {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
   	
	public boolean removeCharactersThatAreInvariant(CategoricalData cData){
		boolean removedSome = false;
		Bits bits = new Bits(cData.getNumChars());
		bits.clearAllBits();
		for (int ic = 0; ic<cData.getNumChars(); ic++){  // first let's record which ones need deletion
			if (!cData.charIsVariable(ic, false)) {
				removedSome = true;
				bits.setBit(ic);
			}
		}
		
		cData.deletePartsFlagged(bits, false);
		cData.deleteInLinkedFlagged(bits, false);
		return removedSome;
	}

	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData cData, MesquiteTable table,  UndoReference undoReference){
		if (!(cData instanceof CategoricalData))
			return ResultCodes.INCOMPATIBLE_DATA;
		CategoricalData data = (CategoricalData)cData;
		int oldNumChars = data.getNumChars();
		removeCharactersThatAreInvariant(data);
		
		logln("" + (oldNumChars-data.getNumChars()) +  " characters removed");
		if (oldNumChars != data.getNumChars())
			return ResultCodes.SUCCEEDED;
			return ResultCodes.MEH;
   	}
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return false;
   	 }
 	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
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
     		return 270;  
     	}
  	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Remove Invariant Characters";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
    			return "Remove Invariant Characters";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Removes all characters that have one state or fewer." ;
   	 }
   	 
}


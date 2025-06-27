/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genomic.IncludeAllAlt;
/*~~  */


import mesquite.lib.AssociableWithSpecs;
import mesquite.lib.Notification;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererMetadata;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAlterer;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public class IncludeAllAlt extends DataAlterer implements AltererMetadata {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
   	
	
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		boolean changed = false;
		CharInclusionSet inclusionSet = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		if (inclusionSet == null) {
			return ResultCodes.SUCCEEDED; //if no inclusion set, then all must be included!
		}
		if (inclusionSet != null) {
			for (int i=0; i<data.getNumChars(); i++) {
				if (!inclusionSet.getSelected(i)) {
						inclusionSet.setSelected(i, true);
					changed = true;
				}
			}
		}

		if (changed)
			data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource

		return ResultCodes.SUCCEEDED;

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
     		return 400;  
     	}
  	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Include All Characters";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
    			return "Include All Characters";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Include all characters, if any are excluded." ;
   	 }
   	 
}



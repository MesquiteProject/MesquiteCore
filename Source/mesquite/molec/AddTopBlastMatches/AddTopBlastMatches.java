/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.AddTopBlastMatches;
/*~~  */

import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.MolecularDataAlterer;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAltererParallelizable;
import mesquite.lib.table.MesquiteTable;
import mesquite.molec.TopBlastMatches.TopBlastMatches;



/* ======================================================================== */
public class AddTopBlastMatches extends MolecularDataAlterer implements DataAltererParallelizable {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(TopBlastMatches.class, getName() + " needs a module that BLASTs.",
		"This is chosen automatically");
	}
	TopBlastMatches topBlastMatchTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		topBlastMatchTask = (TopBlastMatches)hireNamedEmployee(TopBlastMatches.class,"#TopBlastMatches");
		if (topBlastMatchTask == null) {
			return sorry(getName() + " couldn't start because no BLAST module was obtained.");
		}
//		topBlastMatchTask.setSensitiveToBranchSelection(true);
		return true;
	}
	/*.................................................................................................................*
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData cData, MesquiteTable table,  UndoReference undoReference){
		if (!(cData instanceof MolecularData))
			return ResultCodes.INCOMPATIBLE_DATA;
		if (topBlastMatchTask==null)
			return ResultCodes.MEH;
		
		MolecularData data = (MolecularData)cData;
		
		topBlastMatchTask.setAlwaysImportDataIfPossible(true);
		boolean found = topBlastMatchTask.searchData(data, table, true);
		data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
		data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
		if (found)
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
  	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
  	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
  	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
     	public int getVersionOfFirstRelease(){
     		return NEXTRELEASE;  
     	}
    /*.................................................................................................................*/
      	 public boolean isPrerelease(){
      	 	return true;
      	 }
  	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Add BLAST Matches Similar to Selected...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Add BLAST Matches Similar to Selected";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Adds BLAST matches similar to the selected sequences into the matrix." ;
   	 }
   	 
}



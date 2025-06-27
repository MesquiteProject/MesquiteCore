/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.ZapGaps;
/*~~  */

import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.MolecularDataAlterer;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.ResultCodes;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererAlignShift;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;



/* ======================================================================== */
public class ZapGaps extends MolecularDataAlterer implements AltererAlignShift {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
		MolecularData data = (MolecularData)cData;
		boolean found = false;
		MesquiteInteger row= new MesquiteInteger();
		MesquiteInteger firstColumn= new MesquiteInteger();
		MesquiteInteger lastColumn= new MesquiteInteger();
		if (table == null || !table.anythingSelected()){
			for (int it = 0; it<data.getNumTaxa(); it++)
				data.collapseGapsInCellBlock(it, 0, data.getNumChars()-1, false);
			found = true;
		}
		else while (table.nextSingleRowBlockSelected(row,firstColumn,lastColumn)) {
				data.collapseGapsInCellBlock(row.getValue(), firstColumn.getValue(), lastColumn.getValue(), false);
				found = true;
		}
		data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
		data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
		if ( found)
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
     		return -100;  
     	}
    /*.................................................................................................................*/
      	 public boolean isPrerelease(){
      	 	return false;
      	 }
  	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Collapse Sequences to Left (Remove all Gaps)";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Collapse Sequences to Left (Remove all Gaps)";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Collapses sequences in the selected block of cells to yield unaligned sequences, by removing all gaps." ;
   	 }
   	 
}



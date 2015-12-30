/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.ZapGapsRight;
/*~~  */

import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;



/* ======================================================================== */
public class ZapGapsRight extends MolecularDataAlterer  implements AltererAlignShift {
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
   	public boolean alterData(CharacterData cData, MesquiteTable table,  UndoReference undoReference){
		if (!(cData instanceof MolecularData))
			return false;
		boolean found = false;
		MolecularData data = (MolecularData)cData;

		MesquiteInteger row= new MesquiteInteger();
		MesquiteInteger firstColumn= new MesquiteInteger();
		MesquiteInteger lastColumn= new MesquiteInteger();
		if (table == null || !table.anythingSelected()){
			for (int it = 0; it<data.getNumTaxa(); it++)
				data.collapseGapsInCellBlockRight(it, 0, data.getNumChars()-1, false);
		}
		else while (table.nextSingleRowBlockSelected(row,firstColumn,lastColumn)) {
				data.collapseGapsInCellBlockRight(row.getValue(), firstColumn.getValue(), lastColumn.getValue(), false);
		}
		data.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));
		data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
		return found;
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
     		return 200;  
     	}
 	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
    			return "Collapse Sequences to Right (Remove all Gaps)";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
    			return "Collapse Sequences to Right (Remove all Gaps)";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Collapses gaps in the selected block of cells to the right, to yield unaligned sequences." ;
   	 }
   	 
}



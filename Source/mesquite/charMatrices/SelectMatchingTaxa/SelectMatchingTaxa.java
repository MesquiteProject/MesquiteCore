/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.SelectMatchingTaxa;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== *

 *new in 1.02*

/* ======================================================================== */
public class SelectMatchingTaxa extends DataWSelectionAssistant {
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean pleaseLeaveMeOn(){
		return false;
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
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		select();
	}
	/*.................................................................................................................*
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Selects",null, commandName, "selectSame")) {
   	 		select();
		}
   	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;

	}
/**/

	/*  this has been replaced by dataMatches in CharacterData
	boolean sequencesMatch(int it, int checkChar, int taxSel, int icStart, int icEnd, CharacterState cs1, CharacterState cs2) {
   		if (data == null)
   			return false;
   		if (checkChar + (icEnd-icStart)>=data.getNumChars()){ //would extend past end of sequence
			return false;
   		}
   		for (int ic= 0; ic < icEnd-icStart+1; ic++){
   			cs1 = data.getCharacterState(cs1, checkChar + ic, it);  
   			cs2 = data.getCharacterState(cs2,icStart+ic, taxSel);  //
   			if (!cs2.equals(cs1)) {
   				return false;
   			}
   		}
   		return true;

   	}
	 */
	void selectCells(int ic, int it, int length){
		if (table == null)
			return;
		for (int i = 0; i<length; i++)
			table.selectCell(ic + i, it);
	}



	public void select(){ //this should be passed boolean as to whether to notify re: selection?
		if (table != null && data!=null && data.getNumChars()>0){
			int rowsSel = table.numRowsSelected();
			if (rowsSel !=1){
				discreetAlert( "You must select a single taxon (a whole row) to select consistent taxa.");
				return;
			}
			int taxSel = table.firstRowSelected();

			//cells in character taxSel from taxon icStart to icEnd are selecteed
			CharacterState cs1 = data.getCharacterState(null, 0, 0); //to serve as persistent container
			CharacterState cs2  = data.getCharacterState(null, 0, 0);
			MesquiteInteger matchEnd =new MesquiteInteger();
			int instancesFound = 0;
			//find selected sequence, if only one
			for (int it = 0; it< data.getNumTaxa(); it++){
				boolean matches = true;
				for (int ic = 0; ic< data.getNumChars() && matches; ic++) {
					cs1 = data.getCharacterState(cs1, ic, taxSel);
					cs2 = data.getCharacterState(cs2, ic, it);
					if (!cs1.equals(cs2, true, true, true))
						matches = false;
				}
				if (matches) {
					data.getTaxa().setSelected(it, true);
					instancesFound++;
				}
			}
			data.getTaxa().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			if (instancesFound<2)
				discreetAlert( "Nothing selected based on current selection, because nothing else matched");
			else
				logln(Integer.toString(instancesFound) + " instances of sequence found, including original");

			table.repaintAll();
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Select Matching Taxa";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Selects taxa according to whether their sequence of states matches the sequence of states in the selected taxon" ;
	}

}



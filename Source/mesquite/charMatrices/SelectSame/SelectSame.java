/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectSame;
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
public class SelectSame extends DataWSelectionAssistant {
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//addMenuItem("Select Same", makeCommand("selectSame", this));
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

	

   	/** Called to select characters*/
   	public void select(){ //this should be passed boolean as to whether to notify re: selection?
   		if (table != null && data!=null && data.getNumChars()>0){
   			int taxSel = -1;
   			int icStart = -1;
   			int icEnd = -1;
   			boolean found = false;
   			//find selected sequence, if only one
			for (int it = 0; it< data.getNumTaxa(); it++){
   				for (int ic = 0; ic< data.getNumChars(); ic++) {
   					if (table.isCellSelectedAnyWay(ic, it)) {//selected cell found
   						if (found){  //second sequence of cells, can use only one sequence
   							discreetAlert( "Nothing selected based on current selection, because more than one discontiguous sections of sequences currently selected.  A contiguous sequence of states within a single taxon needs to be selected to define the search string.");
   							return;
   						}
   						taxSel = it;
   						icStart = ic;
   						int ic2;
   						for (ic2 = ic+1; ic2< data.getNumChars() && !found; ic2++){ //go to end of first sequence of selected cells
   							if (!table.isCellSelectedAnyWay(ic2, it)) {
   								found= true;
   								icEnd = ic2-1;
   							}
   						}
   						if (!found)
   							icEnd = data.getNumChars()-1;
   						found = true;
   						ic = ic2;
  					}
   				}
   			}
   			
   			if (taxSel <0){
     				discreetAlert( "Nothing selected based on current selection, because nothing is currently selected.  A contiguous sequence of states within a single taxon needs to be selected to define the search string.");
 				return;
   			}
   			//cells in character taxSel from taxon icStart to icEnd are selecteed
   			CharacterState cs1 = data.getCharacterState(null, 0, 0); //to serve as persistent container
   			CharacterState cs2  = data.getCharacterState(null, 0, 0);
   			MesquiteInteger matchEnd =new MesquiteInteger();
   			int instancesFound = 0;
   			   			//find selected sequence, if only one
			for (int it = 0; it< data.getNumTaxa(); it++){
	   			for (int ic = 0; ic< data.getNumChars(); ic++) {
 					if (data.dataMatches(it, ic, taxSel, icStart, icEnd, matchEnd, false, true, cs1, cs2)) {
  						selectCells(ic, it, icEnd-icStart+1);
   						instancesFound++;
   					}
   				}
   			}
   			if (instancesFound<2)
   				discreetAlert( "Nothing selected based on current selection, because nothing else matched");
   			else
   				logln(Integer.toString(instancesFound) + " instances of sequence found, including original");

			table.repaintAll();
   		}
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Select Same Subsequence";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects cells of the matrix according to whether their sequence of states matches the sequence of states in the selected taxon or block of cells" ;
   	 }
   	 
}



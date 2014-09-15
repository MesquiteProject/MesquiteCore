/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectSameVert;
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
public class SelectSameVert extends DataWSelectionAssistant {
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
		selectVert();
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
   	boolean sequencesMatchVert(int ic, int checkTaxon, int charSel, int itStart, int itEnd, CharacterState cs1, CharacterState cs2) {
   		if (data == null)
   			return false;
   		if (checkTaxon + (itEnd-itStart)>=data.getNumTaxa()){ //would extend past end of sequence
			return false;
   		}
   		for (int it= 0; it < itEnd-itStart+1; it++){
   			cs1 = data.getCharacterState(cs1,  ic, checkTaxon + it);  
   			cs2 = data.getCharacterState(cs2, charSel, itStart+it);  //
   			if (!cs2.equals(cs1)) {
   				return false;
   			}
   		}
   		return true;
   		
   	}
   	void selectCellsVert(int it, int ic, int length){
   		if (table == null)
   			return;
   		for (int i = 0; i<length; i++)
   			table.selectCell(ic, it+i);
   	}



   	/** Called to select characters*/
   	public void selectVert(){ //this should be passed boolean as to whether to notify re: selection?
   		if (table != null && data!=null && data.getNumChars()>0){
   			int numCharsSel = table.numColumnsSelected();
   			if (numCharsSel>1)
   				return;
   			int charSel = -1;
   			int itStart = -1;
   			int itEnd = -1;
   			boolean found = false;
   			//find selected sequence, if only one
   			for (int ic = 0; ic< data.getNumChars(); ic++) {
				for (int it = 0; it< data.getNumTaxa(); it++){
   					if (table.isCellSelectedAnyWay(ic, it)) {//selected cell found
   						if (found){  //second sequence of cells, can use only one sequence
   							discreetAlert( "Nothing selected based on current selection, because more than one distribution of states currently selected. A single contiguous vertical strip of cells (i.e. states of adjacent taxa in one character) needs to be selected to define the search string.");
   							return;
   						}
   						charSel = ic;
   						itStart = it;
   						int it2;
   						for (it2 = it+1; it2< data.getNumTaxa() && !found; it2++){ //go to end of first sequence of selected cells
   							if (!table.isCellSelectedAnyWay(ic, it2)) {
   								found= true;
   								itEnd = it2-1;
   							}
   						}
   						if (!found)
   							itEnd = data.getNumTaxa()-1;
   						it = it2;
   						found = true;
  					}
   				}
   			}
   			
   			if (charSel <0){
     				discreetAlert( "Nothing selected based on current selection, because nothing is currently selected.  A single contiguous vertical strip of cells (i.e. states of adjacent taxa in one character) needs to be selected to define the search string.");
 				return;
   			}
   			//cells in character taxSel from taxon icStart to icEnd are selecteed
   			CharacterState cs1 = data.getCharacterState(null, 0, 0); //to serve as persistent container
   			CharacterState cs2  = data.getCharacterState(null, 0, 0);
   			int instancesFound = 0;
   			   			//find selected sequence, if only one
   			for (int ic = 0; ic< data.getNumChars(); ic++) {
				for (int it = 0; it< data.getNumTaxa(); it++){
   					if (sequencesMatchVert(ic, it, charSel, itStart, itEnd, cs1, cs2)) {
  						selectCellsVert(it, ic, itEnd-itStart+1);
   						instancesFound++;
   					}
   				}
   			}
   			if (instancesFound<2)
   				discreetAlert( "Nothing selected based on current selection, because nothing else matched");
   			else
   				logln(Integer.toString(instancesFound) + " instances of distribution found, including original");

			table.repaintAll();
   		}
   	}



	/*.................................................................................................................*/
    	 public String getName() {
		return "Select Same Distribution";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects cells of the matrix according to whether their distribution of states across taxa in a character is the same as that in the selected character or block of cells" ;
   	 }
   	 
}



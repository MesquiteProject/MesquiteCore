/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.LowercaseEnds;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;
import java.awt.event.*;
/* ======================================================================== *

*new in 1.05*

/* ======================================================================== */
public class LowercaseEnds extends DataWSelectionAssistant {
	MesquiteTable table;
	CharacterData data;
	int goodInARow = 3;
	boolean findAll = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		int good = MesquiteInteger.queryInteger(containerOfModule(), "Selection criterion", "This selects the ends of a DNA sequence up to the first block of N sites in a row with uppercase (confident) symbols.  What value of N?  The larger the value, the more may be selected.", goodInARow);
		if (!MesquiteInteger.isCombinable(good))
			return false;
		goodInARow = good;
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
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
   	void selectCells(int ic, int it, int length){
   		if (table == null)
   			return;
   		for (int i = 0; i<length; i++)
   			table.selectCell(ic + i, it);
   	}


   	/** Called to select characters*/
   	public void select(){ //this should be passed boolean as to whether to notify re: selection?
   		if (!(data instanceof CategoricalData)) {
   			discreetAlert( "Sorry, Select Lowercase Ends can be applied only to categorical data");
   		}
   		else if ((table != null && data!=null && data.getNumChars()>0)){
   			CategoricalData dData = (CategoricalData)data;
   			boolean found = false;
   			//find selected sequence, if only one
			for (int it = 0; it< dData.getNumTaxa(); it++){
				boolean done = false;
				int goodToHere = 0;
   				for (int ic = 0; ic< dData.getNumChars() && !done; ic++) {  //starting from left end
   					long state = dData.getStateRaw(ic,it);
   					if (!CategoricalState.isInapplicable(state)){
   						if (CategoricalState.isLowerCase(state) || CategoricalState.isUnassigned(state)) {  //counts against
   							selectCells(ic, it, 1);
   							found = true;
   							if (goodToHere>0){
   								selectCells(ic-goodToHere, it, goodToHere);
   								goodToHere = 0;
   							}
   						}
   						else {
   							goodToHere++;
   							if (goodToHere>=goodInARow)
   								done = true;
   						}
   					}
   				}
				done = false;
				goodToHere = 0;
   				for (int ic = dData.getNumChars() -1; ic>=0 && !done; ic--) {  //starting from right end
   					long state = dData.getStateRaw(ic, it);
   					if (!CategoricalState.isInapplicable(state)){
   						if (CategoricalState.isLowerCase(state) || CategoricalState.isUnassigned(state)) {  //counts against
   							selectCells(ic, it, 1);
   							found = true;
   							if (goodToHere>0){
   								selectCells(ic+1, it, goodToHere);
   								goodToHere = 0;
   							}
   						}
   						else {
   							goodToHere++;
   							if (goodToHere>=goodInARow)
   								done = true;
   						}
   					}
   				}
   			}
   			if (!found)
   				discreetAlert( "No lowercase ends were found to select");
			table.repaintAll();
   		}
   	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "Select Lowercase Ends";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects ends of a DNA sequence up to the first block of N sites in a row with uppercase (confident) symbols (the user chooses N).  Assuming that lowercase letters are used for less certain base calls, this can be used to select and then trim (by painting with gaps) poorly-sequenced terminal regions." ;
   	 }
   	 
}



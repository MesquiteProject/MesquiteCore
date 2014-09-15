/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SearchSelect;
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
public class SearchSelect extends DataWindowAssistantI { //DataWSelectionAssistant {
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		MesquiteMenuItemSpec mm = addMenuItem(MesquiteTrunk.editMenu, "Find All...", MesquiteModule.makeCommand("findAll", this));
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
	}



   	/** Called to select characters*/
   	public void select(){ 
   		if (table != null && data!=null){
   			boolean found = false;
   			MesquiteString value = new MesquiteString("");
   			if (!QueryDialogs.queryShortString(containerOfModule(), "Search matrix", "Select cells of matrix according to if they contain the string:", value))
   				return;
   			String target = value.getValue();
   			if (StringUtil.blank(target))
   				return;
   			//find string in cell
			for (int it = 0; it< table.getNumRows(); it++){
				String name = table.getRowNameTextForDisplay(it);
				if (name != null && StringUtil.foundIgnoreCase(name, target))
					table.selectRowName(it);
   				for (int ic = 0; ic< table.getNumColumns(); ic++) {
   					
					String s = table.getMatrixTextForDisplay(ic, it);
					if (s != null && StringUtil.foundIgnoreCase(s, target))
						table.selectCell(ic, it);
   				}
   			}
   			for (int ic = 0; ic< table.getNumColumns(); ic++) {
 				String name = table.getColumnNameTextForDisplay(ic);
				if (name != null && StringUtil.foundIgnoreCase(name, target))
					table.selectColumnName(ic);
 			}
  			
			table.repaintAll();
   		}
   	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Returns the data matrix shown by the data window", null, commandName, "findAll")) {
    	 		select();
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Select By Search (Find all)";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects cells of the matrix according to whether their text contains a given string" ;
   	 }
   	 
}



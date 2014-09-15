/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SearchData; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.charMatrices.lib.RandomMatrixModifier;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class SearchData extends DataWindowAssistantI {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DataSearcher.class, getName() + " needs a method to search, for instance to find best matches in Genbank to a selected sequence.",
				"You can request a search using the Search submenu of the Matrix menu of the Character Matrix Editor.  This menu may not be available for some data types.");
	}
	MesquiteTable table;
	CharacterData data;
	MesquiteSubmenuSpec mss= null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		 mss = addSubmenu(null, "Search", makeCommand("doSearch",  this));
		mss.setList(DataSearcher.class);
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		mss.setCompatibilityCheck(data.getStateClass());
		resetContainingMenuBar();
		
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Chooses the module to search the data matrix", "[name of module]", commandName, "doSearch")) {
   	 		if (table!=null && data !=null){
	    	 		DataSearcher tda= (DataSearcher)hireNamedEmployee(DataSearcher.class, arguments);
				if (tda!=null) {
					boolean a = tda.searchData(data, table);
	 	   			if (a) {
	 	   				table.repaintAll();
						data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
					}
					fireEmployee(tda);
				}
			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
	return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Search Data";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages data-searching modules." ;
   	 }
   	 
}



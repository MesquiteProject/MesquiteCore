/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.MatrixSelection; 

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class MatrixSelection extends DataWindowAssistantI {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharSelectCoordinator.class, getName() + " uses a criterion to select characters in the Character Matrix Editor.",
		"These options are available in the Select menu of the Character Matrix Editor");
		e2.setSuppressListing(true);
		EmployeeNeed e3 = registerEmployeeNeed(TaxaSelectCoordinator.class, getName() + " uses a criterion to select taxa in the Character Matrix Editor.",
		"These options are available in the Select menu of the Character Matrix Editor");
		e3.setSuppressListing(true);
		EmployeeNeed e4 = registerEmployeeNeed(DataWSelectionAssistant.class, getName() + " uses a criterion to select taxa, characters and cells in the Character Matrix Editor.",
		"These options are available in the Select menu of the Character Matrix Editor");
		e4.setSuppressListing(true);
	}
	CharSelectCoordinator charSel;	
	TaxaSelectCoordinator taxaSel;	
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("Select");
		addMenuItem("Select All", makeCommand("selectAll",  this));
		addMenuItem("Deselect All", makeCommand("deselectAll",  this));
		addMenuSeparator();
		charSel = (CharSelectCoordinator)hireEmployee(CharSelectCoordinator.class, null);
		taxaSel = (TaxaSelectCoordinator)hireEmployee(TaxaSelectCoordinator.class, null);
 		addModuleMenuItems(null, makeCommand("newAssistant",  this), DataWSelectionAssistant.class);
 		addMenuSeparator();
		return true;
  	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Selects all in matrix", null, commandName, "selectAll")) {
			if (data !=null &&table!=null) { // 
				Taxa taxa = data.getTaxa();
 				for (int i=0; i<data.getNumChars(); i++)
 					data.setSelected(i, true);
 				for (int i=0; i<taxa.getNumTaxa(); i++)
 					taxa.setSelected(i, true);
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
 				table.selectBlock(0, 0, data.getNumChars()-1, data.getNumTaxa()-1);
				table.repaintAll();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Hires new data selection assistant module", "[name of module]", commandName, "newAssistant")) {
    	 		DataWSelectionAssistant as =  (DataWSelectionAssistant)hireNamedEmployee(DataWSelectionAssistant.class, arguments);
    	 		if (as == null)
    	 			return null;
    	 		as.setTableAndData(table, data);
    	 		if (!as.pleaseLeaveMeOn())
    	 			fireEmployee(as);
    	 		else
    	 			return as;
		}
    	 	else if (checker.compare(this.getClass(), "Deselects all in matrix", null, commandName, "deselectAll")) {
			if (data !=null && table!=null) {  //
 				table.deselectAll();
				Taxa taxa = data.getTaxa();
 				for (int i=0; i<data.getNumChars(); i++)
 					data.setSelected(i, false);
 				for (int i=0; i<taxa.getNumTaxa(); i++)
 					taxa.setSelected(i, false);
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				table.repaintAll();
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (data==null)
			return;
		this.table = table;
		this.data = data;
		charSel.setTableAndObject(table, data, true);
		taxaSel.setTableAndObject(table, data.getTaxa(), false);
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Character matrix selection coordinator";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Coordinates selecting taxa and characters in data matrix." ;
   	 }
}


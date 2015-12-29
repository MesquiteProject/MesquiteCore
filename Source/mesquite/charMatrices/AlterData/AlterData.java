/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.AlterData; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class AlterData extends DataWindowAssistantI {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(DataAlterer.class, getName() + " needs a particular method to alter data in the Character Matrix Editor.",
		"These options are available in the Alter/Transform submenu of the Matrix menu of the Character Matrix Editor");
		e2.setPriority(2);
	}
	MesquiteTable table;
	CharacterData data;
	MesquiteSubmenuSpec mss= null;
	MesquiteMenuSpec alterMenu; 
	MesquiteSubmenuSpec mssSimpleCell= null;
	MesquiteSubmenuSpec mssRandomizations= null;
	MesquiteSubmenuSpec mssConvertGapMissPolyUncert= null;
	MesquiteMenuSpec alter2Menu; 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		//SUBMENUS
		alterMenu = addAuxiliaryMenu("AlterBySubmenus");
		mssSimpleCell = addSubmenu(alterMenu, "Basic Cell Modifiers", makeCommand("doAlter",  this), AltererSimpleCell.class);
		mssRandomizations = addSubmenu(alterMenu, "Randomizations", makeCommand("doAlter",  this), AltererRandomizations.class);
		mssConvertGapMissPolyUncert = addSubmenu(alterMenu, "Convert Gap/Missing/Polymorph/Uncertain", makeCommand("doAlter",  this), AltererConvertGapMissPolyUncert.class);

		//GROUPS
		alter2Menu = addAuxiliaryMenu("AlterByGroups");
		addItemsOfInterface(alter2Menu, data.getStateClass(), AltererSimpleCell.class, "Basic Cell Modifiers");
		addItemsOfInterface(alter2Menu, data.getStateClass(), AltererRandomizations.class, "Randomizations");
		addItemsOfInterface(alter2Menu, data.getStateClass(), AltererConvertGapMissPolyUncert.class, "Convert Gap/Missing/Polymorph/Uncertain");
		
		//OLD
		mss = addSubmenu(null, "OLD Alter/Transform", makeCommand("doAlter",  this));
		mss.setList(DataAlterer.class);
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
   	 
   	 private void addItemsOfInterface(MesquiteMenuSpec menu, Class stateClass, Class alterInterface, String title){
   		addMenuItem(menu, "-", null);
   		addMenuItem(menu, title, null);
  		addModuleMenuItems(menu, makeCommand("doAlter",  this), alterInterface);
   	 }
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		//OLD
		mss.setCompatibilityCheck(data.getStateClass());
		
		//SUBMENUS
		mssSimpleCell.setCompatibilityCheck(data.getStateClass());
		mssRandomizations.setCompatibilityCheck(data.getStateClass());
		mssConvertGapMissPolyUncert.setCompatibilityCheck(data.getStateClass());
		
		//GROUPS
		alter2Menu.setCompatibilityCheck(data.getStateClass());
		
		resetContainingMenuBar();
		
	}
	
	/* possible alterers:
		recode (as in MacClade)
		reverse (sequences) �
		shuffle �
		random fill �
		fill (via dialog) �
		search and replace
		
	DNA only:
		complement �
		
	continuous only:
		log transform �
		scale �
		standardize (mean 0, variance 1)
	*/
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Hires module to alter the data matrix", "[name of module]", commandName, "doAlter")) {
   	 		if (table!=null && data !=null){
	    	 	if (data.getEditorInhibition()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
	    	 		return null;
	    	 	}
	    	 	DataAlterer tda= (DataAlterer)hireNamedEmployee(DataAlterer.class, arguments);
				if (tda!=null) {
					MesquiteWindow w = table.getMesquiteWindow();
					UndoReference undoReference = new UndoReference();
					AlteredDataParameters alteredDataParameters = new AlteredDataParameters();
					if (MesquiteTrunk.debugMode)
						logln("Memory available before data alterer invoked: " + MesquiteTrunk.getMaxAvailableMemory());
					boolean a = tda.alterData(data, table, undoReference, alteredDataParameters);
					if (MesquiteTrunk.debugMode)
						logln("Memory available after data alterer invoked: " + MesquiteTrunk.getMaxAvailableMemory());

	 	   			if (a) {
	 	   				table.repaintAll();
	 	   				Notification notification = new Notification(MesquiteListener.DATA_CHANGED, alteredDataParameters.getParameters(), undoReference);
	 	   				if (alteredDataParameters.getSubcodes()!=null)
	 	   					notification.setSubcodes(alteredDataParameters.getSubcodes());
						data.notifyListeners(this, notification);
					}
					if (!tda.pleaseLeaveMeOn()) {
						fireEmployee(tda);
					}
				}
			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
	return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Alter Data";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages data-transforming modules." ;
   	 }
   	 
}



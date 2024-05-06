/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.MaintainCharSelection; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public class MaintainCharSelection extends DataWSelectionAssistant {
	CharacterSelectorPersistent selectionTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		selectionTask = (CharacterSelectorPersistent)hireEmployee(CharacterSelectorPersistent.class, "Select characters according to");
		if (selectionTask == null)
			return false;
		selectionTask.pleasePersist();
		addMenuItem(null, "Change character selection", makeCommand("setSelector",  this));
		addMenuItem(null, "Stop maintaining character selection", makeCommand("stop",  this));
		return true;
	}
	
   	public boolean loadModule(){
   		return false;
   	}
   	public boolean pleaseLeaveMeOn(){
   		return true;
   	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Selects all characters", null, commandName, "setSelector")) {
			Debugg.println(" data " + data);
			if (data !=null){
				CharacterSelectorPersistent tda= null;
				if (arguments != null)
					tda = (CharacterSelectorPersistent)hireNamedEmployee(CharacterSelectorPersistent.class, arguments);
				else
					tda = (CharacterSelectorPersistent)hireEmployee(CharacterSelectorPersistent.class, "Select characters according to");
				if (tda!=null) {
					if (selectionTask != null)
						fireEmployee(selectionTask);
					selectionTask = tda;
					selectionTask.pleasePersist();
					selectionTask.selectCharacters(data);
					resetContainingMenuBar();
				}
			}
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
		
	/*Debugg.println
	 * -- set snapshot   
	 * -- set data listener
		/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee == selectionTask)
			selectionTask.selectCharacters(data);

	}
		/* ................................................................................................................. */
		/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface) */
		public void changed(Object caller, Object obj, Notification notification) {
			int code = Notification.getCode(notification);
			int[] parameters = Notification.getParameters(notification);
			if (obj instanceof CharacterData && (CharacterData) obj == data) {
				if (code == AssociableWithSpecs.SPECSSET_CHANGED) {
					resetContainingMenuBar();
					parametersChanged();
				}
			}
			super.changed(caller, obj, notification);
		}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.data = data;
		taxa = data.getTaxa(); 
		selectionTask.selectCharacters(data);
	}
	CharacterData data;
	Taxa taxa;
	/*.................................................................................................................*/
	public String getName() {
		return "Maintain Character Selection";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Maintain Character Selection...";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Shows a persistent character selection in the data matrix editor.";
	}



	public String getParameters(){
		return null;
	}
}






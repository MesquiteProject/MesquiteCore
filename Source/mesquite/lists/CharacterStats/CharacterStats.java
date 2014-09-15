/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharacterStats;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharacterStats extends CharListAssistant implements MesquiteListener {
	MesquiteMenuItemSpec selectedOnlyMenuItem;
	MesquiteBoolean selectedOnly;
	/*.................................................................................................................*/
	public String getName() {
		return "States Information (in List of Characters window)";
	}
	public String getNameForMenuItem() {
		return "States";
	}
	public String getExplanation() {
		return "Supplies basic character state information for characters in character list window." ;
	}
	/*.................................................................................................................*/
	CharacterData data=null;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		selectedOnly = new MesquiteBoolean(false);

		/* hire employees here */
		return true;
	}

	public void setTableAndData(MesquiteTable table, CharacterData data){
		deleteMenuItem(selectedOnlyMenuItem);
		selectedOnlyMenuItem = addCheckMenuItem(null,"Selected Taxa Only", makeCommand("toggleSelectedOnly",  this), selectedOnly);
		if (this.data !=null)
			this.data.removeListener(this);
		this.data = data;
		data.addListener(this);
		//table would be used if selection needed
	}
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	Snapshot temp = new Snapshot();
		temp.addLine("toggleSelectedOnly " + selectedOnly.toOffOnString());
 	 	return temp;
 	 }
	/*.................................................................................................................*/
	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
	 	if (checker.compare(this.getClass(), "Toggles whether only selected taxa are included in the calculation", "[on or off]", commandName, "toggleSelectedOnly")) {
	 		boolean current = selectedOnly.getValue();
	 		selectedOnly.toggleValue(parser.getFirstToken(arguments));
	 		if (current!=selectedOnly.getValue())
	 			parametersChanged();
	 	}
 	else 
	 		return  super.doCommand(commandName, arguments, checker);
	return null;
	 }
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		//TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged();
	}
	public String getTitle() {
		return "States";
	}
	public String getStringForCharacter(int ic){
		return data.getStatesSummary(ic, selectedOnly.getValue());
	}
	public String getWidestString(){
		return "88888888";
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

	public void endJob() {
		if (data !=null)
			data.removeListener(this);
		super.endJob();
	}

}


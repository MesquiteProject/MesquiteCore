/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.DefaultCharOrder;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class DefaultCharOrder extends CharListAssistant implements MesquiteListener {
	/*.................................................................................................................*/
	 public String getName() {
	return "Default Order (characters)";
	 }
	public String getExplanation() {
		return "Shows default order of character." ;
  	 }
	CharacterData data=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		addMenuItem("Set Current Order as Default", makeCommand("setDefault",  this));
		return true;
  	 }
  	 
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (this.data !=null)
			this.data.removeListener(this);
		this.data = data;
		data.addListener(this);
		//table would be used if selection needed
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the current order to be the default", null, commandName, "setDefault")) {
			if (data != null)
				data.resetDefaultOrderToCurrent();
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
	public String getStringForCharacter(int ic){
		if (data != null)
			return Integer.toString(data.getDefaultPosition(ic)+1); //+1 because zero based
		return "?";
	}
	public String getWidestString(){
		return "8888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Default Order";
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
   	 public void endJob() {
		if (data !=null)
			data.removeListener(this);
		super.endJob();
   	 }
   	 
}


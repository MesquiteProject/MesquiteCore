
/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;


/* ======================================================================== */
/**An assistant to the tree window specialized for charts.*/

public abstract class TreeWindowAssistantC extends TreeWindowAssistant  {
	
	/*.................................................................................................................*/
	/** superStartJob is called automatically when an employee is hired.  This is intended for use by superclasses of modules that need
	their own constructor-like call, without relying on the subclass to be polite enough to call super.startJob().*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName){
		addMenuItem("Show Tree Window", makeCommand("showContext", this));
		addMenuSeparator();
		return true;
	}

	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Shows the current context", null, commandName, "showContext")) {
	  		if (getEmployer() instanceof Showable)
	  			getEmployer().showMe();
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }

   	 public Class getDutyClass() {
   	 	return TreeWindowAssistantC.class;
   	 }
 	public String getDutyName() {
 		return "Chart Assistant for Tree Window";
   	 }

}




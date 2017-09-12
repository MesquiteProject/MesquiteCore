
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

import mesquite.lib.*;


/* ======================================================================== */
/**Base class for TreeWindowAssistant, TreeDisplayAssistant, TreeInfoPanelAssistant.*/

public abstract class TreeWDIAssistant extends MesquiteModule  {
	
	public String nameForWritableResults(){
		return getName();
	}
	public boolean suppliesWritableResults(){
		return false;
	}
	public Object getWritableResults(){
		return null;
	}
	public Object getResultsHeading(){
		return null;
	}
	
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns writable Results", null, commandName, "getWritableResults")) {
			return getWritableResults();
		}
		else if (checker.compare(this.getClass(), "Returns writable Results heading", null, commandName, "getResultsHeading")) {
			return getResultsHeading();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

}




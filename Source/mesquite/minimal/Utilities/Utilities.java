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
package mesquite.minimal.Utilities;
/*~~  */

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class Utilities extends MesquiteInit  {
	public String getName() {
		return "Utilities";
	}
	public String getExplanation() {
		return "Provides a menu for utilities";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(UtilitiesAssistant.class, "Utilities assistant modules perform various tasks.",
		"These are activated automatically. ");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("Utilities");
		hireAllEmployees(UtilitiesAssistant.class);
		return true;
	}

	/*.................................................................................................................*/
	/** Respond to commands sent to the window. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		return  super.doCommand(commandName, arguments, checker);
	}
}


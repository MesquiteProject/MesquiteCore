/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.UtilityCoordinator;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

//new 1.04
/* ======================================================================== */
public class UtilityCoordinator extends TreeWindowAssistantI {
	public String getName() {
		return "Tree Utility Coordinator";
	}

	public String getExplanation() {
		return "Coordinates use of tree utilities in tree window" ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeUtility.class, "Various utilities to use trees may be available in the Tree Window.",
		"Utilities to use trees may be available in the Utilities submenu of the Tree Window");
	}
	/*.................................................................................................................*/
	Tree tree;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addSubmenu(null, "Utilities", makeCommand("doUtility",  this), TreeUtility.class);
		return true;
	}

	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires utility module to use the tree", "[name of module]", commandName, "doUtility")) {
			if (tree!=null) {
				TreeUtility tda= (TreeUtility)hireNamedEmployee(TreeUtility.class, arguments);
				if (tda!=null) {
					tda.useTree(tree);
					//if (!tda.pleaseLeaveMeOn()) //if allowed to stay on, need to keep a list of active utilities and inform them when tree changes
					fireEmployee(tda);
				}
			}
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/

}


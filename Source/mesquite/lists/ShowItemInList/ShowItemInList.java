/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.ShowItemInList;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteMenuItemSpec;

/* ======================================================================== */
public class ShowItemInList extends MesquiteModule {
	TableTool tool = null;
	/*.................................................................................................................*/
	public String getName() {
		return "Show Item In List";
	}

	public String getExplanation() {
		return "Establishes a tool through which to request to see an item for lists with showable items." ;
	}

	MesquiteTable table=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//this is hired directly by ListWindow in constructor, but after it's registered itself as the window for the modul.e. Thus, containerOfMocule is already set.
		String sortExplanation = "Shows item listed. ";
		tool = new TableTool(this, "showItem", getPath(),"eye.gif", 0,0,"Show item", sortExplanation, MesquiteModule.makeCommand("showItem",  this) , null, null);
		tool.setWorksOnRowNames(true);
		((ListWindow)containerOfModule()).addTool(tool);
		return true;
	}
	void showItem(int row){
		ListModule listModule = (ListModule)findEmployerWithDuty(ListModule.class);
		if (listModule != null)
			listModule.showItemAtRow(row);
	}

	/*.................................................................................................................*
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the item to show", "[number of item/row]", commandName, "showItem")) {
			int row =
			showItem(ic);

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/** A request for the MesquiteModule to perform a command.  It is passed two strings, the name of the command and the arguments.
	This should be overridden by any module that wants to respond to a command.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(MesquiteModule.class, null, null, commandName, "showItem")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column= MesquiteInteger.fromString(arguments, io);
			int row= MesquiteInteger.fromString(arguments, io);
			showItem(row);
			
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}

	public Class getDutyClass() {
		return getClass();
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}

}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.DefaultTaxaOrder;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;

/* ======================================================================== */
public class DefaultTaxaOrder extends TaxonListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Default Order (taxa)";
	}
	public String getExplanation() {
		return "Shows default order of taxa." ;
	}
	Taxa taxa;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Set Current Order as Default", makeCommand("setDefault",  this));
		return true;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the current order to be the default", null, commandName, "setDefault")) {
			if (taxa != null)
				taxa.resetDefaultOrderToCurrent();
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		/* hire employees here */
		this.taxa = taxa;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Default Order";
	}
	public String getStringForTaxon(int ic){
		if (taxa!=null) {
			return Integer.toString(taxa.getDefaultPosition(ic)+1);//+1 because zero based
		}
		return "?";
	}
	public boolean useString(int ic){
		return true;
	}

	public String getWidestString(){
		return "8888 ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

}


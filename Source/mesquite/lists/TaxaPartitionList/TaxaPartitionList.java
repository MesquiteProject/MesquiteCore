/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxaPartitionList;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxaPartitionList extends TaxaSpecssetList {
	/*.................................................................................................................*/
	public String getName() {
		return "List of Taxa Partitions";
	}
	public String getExplanation() {
		return "Makes windows listing taxa partitions." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxaPartListAssistant.class, "The List of Taxa Partitions window can display columns showing information for each taxa partition.",
		"You can request that columns be shown using the Columns menu of the List of Taxa Partitions Window. ");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		addMenuItem("Make New Taxa Partition...", makeCommand("newTaxaPartition",  this));
		return true;
	}
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}
	/*.................................................................................................................*/
	public void showListWindow(Object obj){ ///TODO: change name to makeLIstWindow
		super.showListWindow(obj);
		TaxaPartListAssistant assistant = (TaxaPartListAssistant)hireNamedEmployee(TaxaPartListAssistant.class, "#TaxaPartListNumGroups");
		if (assistant!= null){
			((ListWindow)getModuleWindow()).addListAssistant(assistant);
			assistant.setUseMenubar(false);
		}
	}
	public Class getItemType(){
		return TaxaPartition.class;
	}
	public Class getAssistantClass(){
		return TaxaPartListAssistant.class;
	}
	public String getItemTypeName(){
		return "Taxa partition";
	}
	public String getItemTypeNamePlural(){
		return "Taxa partitions";
	}
	/* following required by ListModule*/
	public Object getMainObject(){
		return taxa;
	}
	public SpecsSet makeNewSpecsSet(Taxa taxa){
		if (taxa != null)
			return new TaxaPartition("Partition", taxa.getNumTaxa(), null, taxa);
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Instructs user as how to make new taxa partition", null, commandName, "newTaxaPartition")){
			if (taxa !=null &&AlertDialog.query(containerOfModule(), "New Partition", "To make a new partition of taxa, go to the List of Taxa window, make sure that a column for Current Partition appears, edit the column, then save the partition.  Would you like to go to the List of Taxa window now?", "OK", "Cancel")) {
				Object obj = taxa.doCommand("showMe", null, checker);
				if (obj !=null && obj instanceof Commandable){
					Commandable c = (Commandable)obj;
					c.doCommand("newAssistant", "#TaxonListCurrPartition", checker);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Returns taxa block in use", null, commandName, "getTaxa"))
			return taxa;
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

}



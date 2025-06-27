/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxonSetList;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteModule;
import mesquite.lib.SpecsSet;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaSelectionSet;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lists.lib.ListWindow;
import mesquite.lists.lib.TaxaSetListAssistant;
import mesquite.lists.lib.TaxaSpecsListWindow;
import mesquite.lists.lib.TaxaSpecssetList;
import mesquite.lists.lib.TaxonsetsListUtility;

/* ======================================================================== */
public class TaxonSetList extends TaxaSpecssetList {
	/*.................................................................................................................*/
	public String getName() {
		return "List of Taxon Sets";
	}
	public String getExplanation() {
		return "Makes windows listing taxon sets." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxaSetListAssistant.class, "The List of Taxa Sets window can display columns showing information for each taxa set.",
		"You can request that columns be shown using the Columns menu of the List of Taxa Sets Window. ");
	}
	/*.................................................................................................................*/
	public int currentTaxa = 0;
	//public Taxa taxa = null;
	TaxaSpecsListWindow window;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		addMenuItem("Make New Taxon Set...", makeCommand("newTaxSet",  this));
		return true;
	}
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}
	/*.................................................................................................................*/
	public void showListWindow(Object obj){ ///TODO: change name to makeLIstWindow
		super.showListWindow(obj);
		TaxaSetListAssistant assistant = (TaxaSetListAssistant)hireNamedEmployee(TaxaSetListAssistant.class, "#TaxaSetListNum");
		if (assistant!= null){
			((ListWindow)getModuleWindow()).addListAssistant(assistant);
			assistant.setUseMenubar(false);
		}
		MesquiteSubmenuSpec mss2 = addSubmenu(null, "Utilities", MesquiteModule.makeCommand("doUtility",  this));
		mss2.setList(TaxonsetsListUtility.class);

	}
	/*.................................................................................................................*/
	public boolean rowsMovable(){
		return true;
	}
	/*.................................................................................................................*/
	public Class getItemType(){
		return TaxaSelectionSet.class;
	}
	public String getItemTypeName(){
		return "Taxon set";
	}
	public String getItemTypeNamePlural(){
		return "Taxon sets";
	}
	public SpecsSet makeNewSpecsSet(Taxa taxa){
		if (taxa != null)
			return new TaxaSelectionSet("Taxa Set", taxa.getNumTaxa(), taxa);
		return null;
	}
	/*.................................................................................................................*/

	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires utility module to operate on the taxon sets", "[name of module]", commandName, "doUtility")) {

			if (taxa !=null){  // these are the taxa associated with this list
				MesquiteTable table = ((ListWindow)getModuleWindow()).getTable();
				TaxonsetsListUtility tda= (TaxonsetsListUtility)hireNamedEmployee(TaxonsetsListUtility.class, arguments);
				if (tda!=null) {
							boolean a = tda.operateOnTaxa(taxa, table);
				if (!tda.pleaseLeaveMeOn())
					fireEmployee(tda);
				}

			}
		}
		else if (checker.compare(this.getClass(), "Instructs user as how to make new taxon set (TAXSET)", null, commandName, "newTaxSet")){
			if (taxa !=null &&AlertDialog.query(containerOfModule(), "New Taxon Set", "To make a new taxon set (TAXSET), go to the List of Taxa window, select the taxa you want in the set, then choose List>Save selected as set.  Would you like to go to the List of Taxa window now?", "OK", "Cancel")) {
				Object obj = taxa.doCommand("showMe", null, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Returns taxa block in use", null, commandName, "getTaxa"))
			return taxa;
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


}



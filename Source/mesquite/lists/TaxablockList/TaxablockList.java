/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxablockList;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxablockList extends ListLVModule {
	/*.................................................................................................................*/
	public String getName() {
		return "Taxa blocks list";
	}
	public String getExplanation() {
		return "Makes windows listing blocks of Taxa and information about them." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxaBlocksListAssistant.class, "The List of Taxa Blocks window can display columns showing information for each taxa block.",
		"You can request that columns be shown using the Columns menu of the List of Taxa Blocks Window. ");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean showing(Object obj){
		return (getModuleWindow()!=null  && getProject().getTaxas()==obj);
	}

	public boolean resetMenusOnNameChange(){
		return true;
	}
	public void showListWindow(Object obj){
		setModuleWindow(new ListableVectorWindow(this));
		((ListableVectorWindow)getModuleWindow()).setObject(getProject().getTaxas());
		makeMenu("List");

		MesquiteSubmenuSpec mss2 = addSubmenu(null, "Utilities", MesquiteModule.makeCommand("doUtility",  this));
		mss2.setList(TaxaBlocksListUtility.class);
		if (!MesquiteThread.isScripting()){
			TaxaBlocksListAssistant assistant = (TaxaBlocksListAssistant)hireNamedEmployee(TaxaBlocksListAssistant.class, StringUtil.tokenize("Number of taxa in set"));
			if (assistant!= null){
				((ListableVectorWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
		}
		resetContainingMenuBar();
		resetAllWindowsMenus();
	}
	public String getItemTypeName(){
		return "Taxa block";
	}
	public String getItemTypeNamePlural(){
		return "Taxa blocks";
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return getProject().getTaxas();
	}
	public int getNumberOfRows(){
		if (getProject().getTaxas()==null)
			return 0;
		else
			return getProject().getTaxas().size();
	}
	public Class getAssistantClass(){
		return TaxaBlocksListAssistant.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (getModuleWindow()==null || !getModuleWindow().isVisible())
			return null;
		Snapshot temp = new Snapshot();
		if (getModuleWindow()!=null)
			getModuleWindow().incorporateSnapshot(temp, file);
		temp.addLine("showWindow"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires utility module to operate on the taxa blocks", "[name of module]", commandName, "doUtility")) {
			if (getProject().getTaxas() !=null){
				TaxaBlocksListUtility tda= (TaxaBlocksListUtility)hireNamedEmployee(TaxaBlocksListUtility.class, arguments);
				if (tda!=null) {
					boolean a = tda.operateOnTaxas(getProject().getTaxas());
					if (!tda.pleaseLeaveMeOn())
						fireEmployee(tda);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean rowsDeletable(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean rowDeletable(int row){
		return true; //(getProject().getNumberTaxas()>1);
	}
	public boolean deleteRow(int row, boolean notify){
		if (row<0 || row>= getNumberOfRows())
			return false;
		Taxa taxa = getProject().getTaxa(row);
		getProject().removeFileElement(taxa);//must remove first, before disposing
		taxa.dispose();
		return true;
	}

	/*.................................................................................................................*/

	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj == getProject().getTaxas() && code == MesquiteListener.ELEMENT_CHANGED && getModuleWindow()!=null){
			((TableWindow)getModuleWindow()).getTable().synchronizeRowSelection(getProject().getTaxas());
			((TableWindow)getModuleWindow()).getTable().repaintAll();
		}
	}

	/*.................................................................................................................*/
	/** Requests a getModuleWindow() to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
	}


}



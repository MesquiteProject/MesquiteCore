/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.DatasetList;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class DatasetList extends ListLVModule {
	/*.................................................................................................................*/
	public String getName() {
		return "Character Matrices List";
	}
	public String getExplanation() {
		return "Makes windows listing character data matrices and information about them." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DataSetsListAssistant.class, "The List of Character Matrices window can display columns showing information for each character matrix.",
		"You can request that columns be shown using the Columns menu of the List of Character Matrices Window. ");
	}
	ListableVector datas;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		datas = getProject().getCharacterMatrices();
		return true;
	}
	public boolean showing(Object obj){
		return (getModuleWindow()!=null && obj == datas);
	}

	public boolean resetMenusOnNameChange(){
		return true;
	}
	public void showListWindow(Object obj){
		CharMatricesListWindow charMatricesListWindow = new CharMatricesListWindow(this);
		charMatricesListWindow.setDatas(datas);
		setModuleWindow(charMatricesListWindow); 
		((ListableVectorWindow)getModuleWindow()).setObject(datas);
		datas.addListener(this);
		makeMenu("List");

		MesquiteSubmenuSpec mss2 = addSubmenu(null, "Utilities", MesquiteModule.makeCommand("doUtility",  this));
		mss2.setList(DatasetsListUtility.class);

		if (!MesquiteThread.isScripting()){
			DataSetsListAssistant assistant = (DataSetsListAssistant)hireNamedEmployee(DataSetsListAssistant.class, StringUtil.tokenize("Taxa of data matrix"));
			if (assistant!= null){
				((ListableVectorWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (DataSetsListAssistant)hireNamedEmployee(DataSetsListAssistant.class, StringUtil.tokenize("Number of characters of data matrix"));
			if (assistant!= null){
				((ListableVectorWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (DataSetsListAssistant)hireNamedEmployee(DataSetsListAssistant.class, "#DatasetsListClass");
			if (assistant!= null){
				((ListableVectorWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (DataSetsListAssistant)hireNamedEmployee(DataSetsListAssistant.class, "#MatrixListVisible");
			if (assistant!= null){
				((ListableVectorWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}

		}
		resetContainingMenuBar();
		resetAllWindowsMenus();
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj == datas && code == MesquiteListener.ELEMENT_CHANGED && getModuleWindow()!=null){
			((TableWindow)getModuleWindow()).getTable().synchronizeRowSelection(datas);
			((TableWindow)getModuleWindow()).getTable().repaintAll();
		}
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return datas;
	}
	public int getNumberOfRows(){
		if (datas==null)
			return 0;
		else
			return datas.size();
	}
	public Class getAssistantClass(){
		return DataSetsListAssistant.class;
	}
	/*.................................................................................................................*/
	public boolean rowsDeletable(){
		return true;
	}
	public void aboutToDeleteRow(int row){  //called just before superclass deletes rows, in case specific module needs to prepare for deletion
		if (row<0 || row>= getNumberOfRows())
			return;
		CharacterData rdata = getProject().getCharacterMatrixDoomedOrNot(row);
		if (rdata != null)
			rdata.doom();
	}
	public boolean deleteRow(int row, boolean notify){
		if (row<0 || row>= getNumberOfRows())
			return false;
		CharacterData data = getProject().getCharacterMatrixDoomedOrNot(row);
		getProject().removeFileElement(data);//must remove first, before disposing
		data.dispose();
		return true;
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
		if (checker.compare(this.getClass(), "Hires utility module to operate on the data matrices", "[name of module]", commandName, "doUtility")) { //12 Jan 02
			if (getProject().getCharacterMatrices() !=null){
				DatasetsListUtility tda= (DatasetsListUtility)hireNamedEmployee(DatasetsListUtility.class, arguments);
				if (tda!=null) {
					MesquiteTable table = ((ListWindow)getModuleWindow()).getTable();
					ListableVector datas = null;
					if (table.anyRowSelected()){
						ListableVector alldatas = getProject().getCharacterMatrices();
						
						datas = new ListableVector();
						for (int i=0; i<alldatas.size(); i++){
								
							if (table.isRowSelected(i))
								datas.addElement(alldatas.elementAt(i), true);
						}
					}
					else {
						ListableVector alldatas = getProject().getCharacterMatrices();
						datas = new ListableVector();
						for (int i=0; i<alldatas.size(); i++){
							datas.addElement(alldatas.elementAt(i), true);
						}
					}
					boolean a = tda.operateOnDatas(datas, table);
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
	public String getItemTypeName(){
		return "Character matrix";
	}
	public String getItemTypeNamePlural(){
		return "Character matrices";
	}
	/*.................................................................................................................*/
	/** Requests a window to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
	}


}



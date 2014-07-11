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
package mesquite.lists.TaxonGroupList;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxonGroupList extends ListModule {
	/*.................................................................................................................*/
	public String getName() {
		return "List of Taxon Group Labels";
	}
	public String getExplanation() {
		return "Makes windows listing taxon groups and information about them." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxonGroupListAssistant.class, "The List of Taxon Groups window can display columns showing information for each taxon group.",
		"You can request that columns be shown using the Columns menu of the List of Taxon Groups Window. ");
	}
	TaxaGroupVector groups;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean showing(Object obj){
		return (getModuleWindow()!=null && obj == groups);
	}

	public void showListWindow(Object obj){
		setModuleWindow(new TaxonGroupListWindow(this));
		groups = (TaxaGroupVector)getProject().getFileElement(TaxaGroupVector.class, 0);
		((TaxonGroupListWindow)getModuleWindow()).setObject(groups);
		//makeMenu("Character_Models");
		makeMenu("List");

		
		if (!MesquiteThread.isScripting()){
			TaxonGroupListAssistant assistant = (TaxonGroupListAssistant)hireNamedEmployee(TaxonGroupListAssistant.class, StringUtil.tokenize("#TaxonGroupListColor"));
			if (assistant!= null){
				((TaxonGroupListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
	/*		assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("#ModelsListParadigm"));
			if (assistant!= null){
				((TaxonGroupListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("#ModelsListWhole"));
			if (assistant!= null){
				((TaxonGroupListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("Data type for model"));
			if (assistant!= null){
				((TaxonGroupListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
*/
		}
	

		resetContainingMenuBar();
		resetAllWindowsMenus();
		//getModuleWindow().setVisible(true);
	}
	/*.................................................................................................................*/
	public boolean rowsMovable(){
		return true;
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return getProject().getFileElement(TaxaGroupVector.class, 0);
	}
	public int getNumberOfRows(){
		if (getProject().getFileElement(TaxaGroupVector.class, 0)==null)
			return 0;
		else
			return ((ListableVector)getProject().getFileElement(TaxaGroupVector.class, 0)).size();
	}
	public Class getAssistantClass(){
		return TaxonGroupListAssistant.class;
	}
	public String getItemTypeName(){
		return "Taxon Group";
	}
	public String getItemTypeNamePlural(){
		return "Taxon Groups";
	}
	/*.................................................................................................................*/
	public boolean rowsDeletable(){
		return true;
	}
	public boolean deleteRow(int row, boolean notify){
		TaxaGroup group = ((TaxonGroupListWindow)getModuleWindow()).getTaxonGroup(row);
		if (group!=null){
				getProject().removeFileElement(group);//must remove first, before disposing
				group.dispose();
				return true;
		
		}
		return false;
	}

	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ 
		if (row<0 || row>= getNumberOfRows())
			return null;
		TaxaGroup group = ((TaxonGroupListWindow)getModuleWindow()).getTaxonGroup(row);
		return group.getAnnotation();
	}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){
		if (row<0 || row>= getNumberOfRows())
			return;
		TaxaGroup group = ((TaxonGroupListWindow)getModuleWindow()).getTaxonGroup(row);
		group.setAnnotation(s, notify);
	}
	/** returns a String of explanation for a row*/
	public String getExplanation(int row){
		TaxaGroup group = ((TaxonGroupListWindow)getModuleWindow()).getTaxonGroup(row);
		if (group!=null)
			return group.getExplanation();
		return null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (getModuleWindow()==null || !getModuleWindow().isVisible())
			return null;
		Snapshot temp = new Snapshot();
		if (getModuleWindow()!=null)
			getModuleWindow().incorporateSnapshot(temp, file);
		//if (getModuleWindow()!=null && !getModuleWindow().isVisible())
		temp.addLine("showWindow"); 
		return temp;
	}


	/*.................................................................................................................*/
	/** Requests a getModuleWindow() to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		//Debug.println("disposing of getModuleWindow()");
		whichWindow.hide();
	}


}

/* ======================================================================== */
class TaxonGroupListWindow extends ListWindow implements MesquiteListener {
	TaxaGroupVector groups;
	public TaxonGroupListWindow (TaxonGroupList ownerModule) {
		super(ownerModule);
		MesquiteTable t = getTable();
		if (t!=null)
			t.setAutoEditable(false, false, false, false);
	}
	/*.................................................................................................................*/
	/** When called the getModuleWindow() will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Taxon Groups"); 
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return groups;
	}
	public void setCurrentObject(Object obj){
		if (obj instanceof ListableVector) {
			if (groups!=null)
				groups.removeListener(this);
			groups = (TaxaGroupVector)obj;
			groups.addListener(this);
			getTable().synchronizeRowSelection(groups);
		}
		super.setCurrentObject(obj);
	}
	/*...............................................................................................................*/
	/** returns whether or not a row name of table is editable.*/
	public boolean isRowNameEditable(int row){
		TaxaGroup group = getTaxonGroup(row);
		return (group!=null);
	}
	TaxaGroup getTaxonGroup(int row){
		if (groups!=null) {
			if (row>=0 && row<groups.size())
				return(TaxaGroup)groups.elementAt(row);
		}
		return null;
	}
	public boolean interceptRowNameTouch(int row, int regionInCellH, int regionInCellV, int modifiers){
		TaxaGroup group = getTaxonGroup(row);
		if (group!=null){
			getTable().editRowNameCell(row);
		}
		return true;
	}
	public void setRowName(int row, String name){
		TaxaGroup group = getTaxonGroup(row);
		if (group!=null){
				group.setName(name);
				resetAllTitles();
				getOwnerModule().resetAllMenuBars();
			
		}
	}
	public String getRowName(int row){
		if (groups!=null)
			return ((Listable)groups.elementAt(row)).getName();
		else
			return null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj ==groups)
			ownerModule.windowGoAway(this);
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		UndoReference undoReference = Notification.getUndoReference(notification);
		int code = Notification.getCode(notification);
		if (obj instanceof ListableVector && (ListableVector)obj ==groups) {
			if (code==MesquiteListener.NAMES_CHANGED) {
				getTable().redrawRowNames();
			}
			else if (code==MesquiteListener.SELECTION_CHANGED) { //listable vector, not Associable, hence dont' synchronize
				getTable().synchronizeRowSelection(groups);
				getTable().repaintAll();
			}
			else if (code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_DELETED || code==MesquiteListener.PARTS_CHANGED || code==MesquiteListener.PARTS_MOVED) {
				getTable().setNumRows(groups.size());
				getTable().synchronizeRowSelection(groups);
				getTable().repaintAll();
				if (code==MesquiteListener.PARTS_MOVED)
					setUndoer(undoReference);
				else
					setUndoer();
			}
			else if (code!=MesquiteListener.ANNOTATION_CHANGED && code!=MesquiteListener.ANNOTATION_ADDED && code!=MesquiteListener.ANNOTATION_DELETED) {
				getTable().setNumRows(groups.size());
				getTable().synchronizeRowSelection(groups);
				getTable().repaintAll();
			}
		}
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/

	public void dispose(){
		if (groups!=null)
			groups.removeListener(this);
		super.dispose();
	}
}



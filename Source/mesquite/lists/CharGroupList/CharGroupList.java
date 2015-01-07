/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharGroupList;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharGroupList extends ListModule {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharGroupListAssistant.class, "The List of Character Groups window can display columns showing information for each character group.",
				"You can request that columns be shown using the Columns menu of the List of Character Groups Window. ");
	}
	CharactersGroupVector groups;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("New Character Group...", MesquiteModule.makeCommand("newGroup",  this));
		return true;
	}
	public boolean showing(Object obj){
		return (getModuleWindow()!=null && obj == groups);
	}

	public void showListWindow(Object obj){
		setModuleWindow(new CharGroupListWindow(this));
		groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		((CharGroupListWindow)getModuleWindow()).setObject(groups);
		//makeMenu("Character_Models");
		makeMenu("List");


		if (!MesquiteThread.isScripting()){
			CharGroupListAssistant assistant = (CharGroupListAssistant)hireNamedEmployee(CharGroupListAssistant.class, StringUtil.tokenize("#CharGroupListColor"));
			if (assistant!= null){
				((CharGroupListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			/*		assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("#ModelsListParadigm"));
			if (assistant!= null){
				((CharGroupListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("#ModelsListWhole"));
			if (assistant!= null){
				((CharGroupListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("Data type for model"));
			if (assistant!= null){
				((CharGroupListWindow)getModuleWindow()).addListAssistant(assistant);
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
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Creates a new group", "[]", commandName, "newGroup")) {
			MesquiteString ms = new MesquiteString("");
			MesquiteFile file = getProject().chooseFile( "Select file to which to add the new group label"); 
			CharactersGroup group = CharListPartitionUtil.makeGroup(this,file,containerOfModule(), ms);      
			if (group!=null) {
				((CharGroupListWindow)getModuleWindow()).getTable().repaint();  
				parametersChanged();
			}
			return group;
		}
		else if (checker.compare(this.getClass(), "Returns data set whose characters are listed", null, commandName, "getData")) {
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return getProject().getFileElement(CharactersGroupVector.class, 0);     
	}
	public int getNumberOfRows(){
		if (getProject().getFileElement(CharactersGroupVector.class, 0)==null)
			return 0;
		else
			return ((ListableVector)getProject().getFileElement(CharactersGroupVector.class, 0)).size();
	}
	public Class getAssistantClass(){
		return CharGroupListAssistant.class;
	}
	public String getItemTypeName(){
		return "Character Group";
	}
	public String getItemTypeNamePlural(){
		return "Character Groups";
	}
	/*.................................................................................................................*/
	public boolean rowsDeletable(){
		return true;
	}
	public boolean deleteRow(int row, boolean notify){
		CharactersGroup group = ((CharGroupListWindow)getModuleWindow()).getCharGroup(row);
		if (group!=null){
			group.deleteMe(false);
			return true;

		}
		return false;
	}

	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ 
		if (row<0 || row>= getNumberOfRows())
			return null;
		CharactersGroup group = ((CharGroupListWindow)getModuleWindow()).getCharGroup(row);
		return group.getAnnotation();
	}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){
		if (row<0 || row>= getNumberOfRows())
			return;
		CharactersGroup group = ((CharGroupListWindow)getModuleWindow()).getCharGroup(row);
		group.setAnnotation(s, notify);
	}
	/** returns a String of explanation for a row*/
	public String getExplanation(int row){
		CharactersGroup group = ((CharGroupListWindow)getModuleWindow()).getCharGroup(row);
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

	/*.................................................................................................................*/
	public String getName() {
		return "List of Character Group Labels";
	}
	public String getExplanation() {
		return "Makes windows listing character groups and information about them." ;
	}

}

/* ======================================================================== */
class CharGroupListWindow extends ListWindow implements MesquiteListener {
	CharactersGroupVector groups;
	public CharGroupListWindow (CharGroupList ownerModule) {
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
		setTitle("Character Groups"); 
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return groups;
	}
	public void setCurrentObject(Object obj){
		if (obj instanceof ListableVector) {
			if (groups!=null)
				groups.removeListener(this);
			groups = (CharactersGroupVector)obj;
			groups.addListener(this);
			getTable().synchronizeRowSelection(groups);
		}
		super.setCurrentObject(obj);
	}
	/*...............................................................................................................*/
	/** returns whether or not a row name of table is editable.*/
	public boolean isRowNameEditable(int row){
		CharactersGroup group = getCharGroup(row);
		return (group!=null);
	}
	CharactersGroup getCharGroup(int row){
		if (groups!=null) {
			if (row>=0 && row<groups.size())
				return(CharactersGroup)groups.elementAt(row);
		}
		return null;
	}
	public boolean interceptRowNameTouch(int row, int regionInCellH, int regionInCellV, int modifiers){
		CharactersGroup group = getCharGroup(row);
		if (group!=null){
			getTable().editRowNameCell(row);
		}
		return true;
	}
	public void setRowName(int row, String name){
		CharactersGroup group = getCharGroup(row);
		if (group!=null){
			group.setName(name);
			resetAllTitles();
			getOwnerModule().resetAllMenuBars();

		}
	}
	public String getRowName(int row){
		if (groups!=null){
			if (row<0 && row >= groups.size())
				return null;
			return ((Listable)groups.elementAt(row)).getName();
		}
		else
			return null;
	}
	public String getRowNameForSorting(int row){
		return getRowName(row);
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
		if (obj instanceof GroupLabel) {
			getTable().repaintAll();
		}
		else if (obj instanceof ListableVector && (ListableVector)obj ==groups) {
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
			else  {
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



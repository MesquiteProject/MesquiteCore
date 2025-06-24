/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.ModelsList;
/*~~  */

import mesquite.lib.EmployeeNeed;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.table.EditorPanel;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lists.lib.ListModule;
import mesquite.lists.lib.ListTable;
import mesquite.lists.lib.ListWindow;
import mesquite.lists.lib.ModelsListAssistant;

/* ======================================================================== */
public class ModelsList extends ListModule {
	/*.................................................................................................................*/
	public String getName() {
		return "Character Models List";
	}
	public String getExplanation() {
		return "Makes windows listing character models and information about them." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ModelsListAssistant.class, "The List of Character Models window can display columns showing information for each character model.",
		"You can request that columns be shown using the Columns menu of the List of Character Models Window. ");
	}
	ListableVector models;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean showing(Object obj){
		return (getModuleWindow()!=null && obj == models);
	}

	public void showListWindow(Object obj){
		setModuleWindow(new ModelsListWindow(this));
		((ModelsListWindow)getModuleWindow()).setObject(models = getProject().getCharacterModels());
		//makeMenu("Character_Models");
		makeMenu("List");

		if (!MesquiteThread.isScripting()){
			ModelsListAssistant assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("#ModelsListType"));
			if (assistant!= null){
				((ModelsListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("#ModelsListParadigm"));
			if (assistant!= null){
				((ModelsListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("#ModelsListWhole"));
			if (assistant!= null){
				((ModelsListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			/**/
			assistant = (ModelsListAssistant)hireNamedEmployee(ModelsListAssistant.class, StringUtil.tokenize("Data type for model"));
			if (assistant!= null){
				((ModelsListWindow)getModuleWindow()).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}

		}
		resetContainingMenuBar();
		resetAllWindowsMenus();
		//getModuleWindow().setVisible(true);
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return getProject().getCharacterModels();
	}
	public int getNumberOfRows(){
		if (getProject().getCharacterModels()==null)
			return 0;
		else
			return getProject().getCharacterModels().size();
	}
	public Class getAssistantClass(){
		return ModelsListAssistant.class;
	}
	public String getItemTypeName(){
		return "Character model";
	}
	public String getItemTypeNamePlural(){
		return "Character models";
	}
	/*.................................................................................................................*/
	public boolean rowsDeletable(){
		return true;
	}
	public boolean deleteRow(int row, boolean notify){
		CharacterModel model = ((ModelsListWindow)getModuleWindow()).getModel(row);
		if (model!=null){
			if (model.isBuiltIn()) {
				alert("That character model cannot be deleted, because the model is built in");
				return false;
			}
			else {
				getProject().removeFileElement(model, notify);//must remove first, before disposing
				model.dispose();
				return true;
			}
		}
		return false;
	}

	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ 
		if (row<0 || row>= getNumberOfRows())
			return null;
		CharacterModel model = ((ModelsListWindow)getModuleWindow()).getModel(row);
		return model.getAnnotation();
	}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){
		if (row<0 || row>= getNumberOfRows())
			return;
		CharacterModel model = ((ModelsListWindow)getModuleWindow()).getModel(row);
		model.setAnnotation(s, notify);
	}
	/** returns a String of explanation for a row*/
	public String getExplanation(int row){
		CharacterModel model = ((ModelsListWindow)getModuleWindow()).getModel(row);
		if (model!=null)
			return model.getExplanation();
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
		if (whichWindow == null)
			return;
		whichWindow.hide();
	}


}

/* ======================================================================== */
class ModelsListWindow extends ListWindow implements MesquiteListener {
	ListableVector models;
	public ModelsListWindow (ModelsList ownerModule) {
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
		setTitle("Character models"); 
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return models;
	}
	public void setCurrentObject(Object obj){
		if (obj instanceof ListableVector) {
			if (models!=null)
				models.removeListener(this);
			models = (ListableVector)obj;
			models.addListener(this);
			getTable().synchronizeRowSelection(models);
		}
		super.setCurrentObject(obj);
	}
	/*...............................................................................................................*/
	/** returns whether or not a row name of table is editable.*/
	public boolean isRowNameEditable(int row){
		CharacterModel model = getModel(row);
		if (model==null)
			return false;
		else
			return !model.isBuiltIn();
	}
	CharacterModel getModel(int row){
		if (models!=null) {
			if (row>=0 && row<models.size())
				return(CharacterModel)models.elementAt(row);
		}
		return null;
	}
	public boolean interceptRowNameTouch(int row, EditorPanel editorPanel, int x, int y, int modifiers){
		CharacterModel model = getModel(row);
		if (model!=null){
			if (model.isBuiltIn()) {
				((ListTable)getTable()).superRowNameTouched(row,  editorPanel, x, y, modifiers,1);
			}
			else
				getTable().editRowNameCell(row);
			return true;
		}
		return false;
	}
	public void setRowName(int row, String name, boolean update){
		CharacterModel model = getModel(row);
		if (model!=null){
			if (model.isBuiltIn())
				ownerModule.alert("The name of that character model cannot be changed, because the model is built in");
			else {
				model.setName(name);
				if (update){
				resetAllTitles();
				getOwnerModule().resetAllMenuBars();
				}
			}
		}
	}
	public String getRowName(int row){
		if (models!=null){
			if (row<0 || row >= models.size())
				return null;
			return ((Listable)models.elementAt(row)).getName();
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
		if (obj ==models)
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
		if (obj instanceof ListableVector && (ListableVector)obj ==models) {
			if (code==MesquiteListener.NAMES_CHANGED) {
				getTable().redrawRowNames();
			}
			else if (code==MesquiteListener.SELECTION_CHANGED) { //listable vector, not Associable, hence dont' synchronize
				getTable().synchronizeRowSelection(models);
				getTable().repaintAll();
			}
			else if (code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_DELETED || code==MesquiteListener.PARTS_CHANGED || code==MesquiteListener.PARTS_MOVED) {
				getTable().setNumRows(models.size());
				getTable().synchronizeRowSelection(models);
				getTable().repaintAll();
				if (code==MesquiteListener.PARTS_MOVED)
					setUndoer(undoReference);
				else
					setUndoer();
			}
			else if (code!=MesquiteListener.ANNOTATION_CHANGED && code!=MesquiteListener.ANNOTATION_ADDED && code!=MesquiteListener.ANNOTATION_DELETED) {
				getTable().setNumRows(models.size());
				getTable().synchronizeRowSelection(models);
				getTable().repaintAll();
			}
		}
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/

	public void dispose(){
		if (models!=null)
			models.removeListener(this);
		super.dispose();
	}
}



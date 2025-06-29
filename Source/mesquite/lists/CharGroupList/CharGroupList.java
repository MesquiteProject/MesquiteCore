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


import mesquite.charMatrices.ManageCharPartitions.ManageCharPartitions;
import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.GroupLabel;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.characters.CharactersGroup;
import mesquite.lib.characters.CharactersGroupVector;
import mesquite.lib.duties.SpecsSetManager;
import mesquite.lib.table.EditorPanel;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lists.lib.CharGroupListAssistant;
import mesquite.lists.lib.CharListPartitionUtil;
import mesquite.lists.lib.ListModule;
import mesquite.lists.lib.ListWindow;

/* ======================================================================== */
public class CharGroupList extends ListModule {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharGroupListAssistant.class, "The List of Character Groups window can display columns showing information for each character group.",
				"You can request that columns be shown using the Columns menu of the List of Character Groups Window. ");
	}
	CharactersGroupVector groups;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("New Character Group Label...", MesquiteModule.makeCommand("newGroup",  this));
		ManageCharPartitions manageCharPart = (ManageCharPartitions)findElementManager(CharacterPartition.class);
		addMenuItem("Import Group Labels & Colors from File...", MesquiteModule.makeCommand("importLabels",  manageCharPart));
		addMenuItem("Export Group Labels & Colors to File...", MesquiteModule.makeCommand("exportLabels",  manageCharPart));
		return true;
	}
	public boolean showing(Object obj){
		return (getModuleWindow()!=null && obj == groups);
	}

	public void showListWindow(Object obj){
		if (getModuleWindow() != null){
			((CharGroupListWindow)getModuleWindow()).setObject(groups);
			return;
		}
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
	public boolean columnsMovable(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean rowsMovable(){
		return true;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Creates a new group label", "[]", commandName, "newGroup")) {
			MesquiteString ms = new MesquiteString("");
			MesquiteFile file = getProject().chooseFile( "Select file to which to add the new group label"); 
			CharactersGroup group = CharListPartitionUtil.makeGroup(this,file,containerOfModule(), ms);      
			if (group!=null) {
				((CharGroupListWindow)getModuleWindow()).getTable().repaint();  
				parametersChanged();
			}
			return group;
		}
		else if (checker.compare(this.getClass(), "Imports group labels from a text file.", "[]", commandName, "importLabels")) {
			MesquiteString directoryName = new MesquiteString();
			MesquiteString fileName = new MesquiteString();
			MesquiteFile.openFileDialog("Please select a text file that has the character group labels, as exported previously.", directoryName, fileName);
			if (!fileName.isBlank()){
				String[] lines = MesquiteFile.getFileContentsAsStrings(directoryName.getValue() + fileName.getValue());
				if (lines != null){
					SpecsSetManager manageCharPart = (SpecsSetManager)findElementManager(CharacterPartition.class);
					for (int i = 0; i<lines.length; i++){
						String command = lines[i]; 
						boolean success = manageCharPart.readNexusCommand(null, null, "LABELS", command, null,  null);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Exports group labels to a text file for later import.", "[]", commandName, "exportLabels")) {
			ManageCharPartitions manageCharPart = (ManageCharPartitions)findElementManager(CharacterPartition.class);
			String s = "";
			for (int row = 0; row<getNumberOfRows(); row++){
				CharactersGroup group = ((CharGroupListWindow)getModuleWindow()).getCharGroup(row);
				s += manageCharPart.getGroupLabelNexusCommand(group) + "\n";
			}
			if (!StringUtil.blank(s)){
				MesquiteFile.putFileContentsQuery("Exported file of group labels, for later import into other files", s, true);
				
			}
		}
		else if (checker.compare(this.getClass(), "Returns data set whose characters are listed", null, commandName, "getData")) {
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
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
		return "Character Group Label";
	}
	public String getItemTypeNamePlural(){
		return "Character Group Labels";
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
		if (whichWindow == null)
			return;
		whichWindow.hide();
	}

	/*.................................................................................................................*/
	public String getName() {
		return "List of Character Group Labels";
	}
	public String getExplanation() {
		return "Makes windows listing character group labels and information about them." ;
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
	public boolean interceptRowNameTouch(int row, EditorPanel editorPanel, int x, int y, int modifiers){
		CharactersGroup group = getCharGroup(row);
		if (group!=null){
			getTable().editRowNameCell(row);
			return true;
		}
		return false;
	}
	public void setRowName(int row, String name, boolean update){
		CharactersGroup group = getCharGroup(row);
		if (group!=null){
			group.setName(name);
			if (update){
			resetAllTitles();
			getOwnerModule().resetAllMenuBars();
			}

		}
	}
	public String getRowName(int row){
		if (groups!=null){
			if (row<0 || row >= groups.size())
				return null;
			Listable g = (Listable)groups.elementAt(row);
			return g.getName();
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



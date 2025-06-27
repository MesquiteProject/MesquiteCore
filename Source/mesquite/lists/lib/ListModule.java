/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.lib;

import java.util.Vector;

import mesquite.lib.Annotatable;
import mesquite.lib.CommandChecker;
import mesquite.lib.FileElement;
import mesquite.lib.FunctionExplanation;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Notification;
import mesquite.lib.StringUtil;
import mesquite.lib.Undoer;
import mesquite.lib.duties.ManagerAssistant;
import mesquite.lib.table.TableWindow;
import mesquite.lib.ui.MesquiteWindow;


/* ======================================================================== */
/**Supplies a column for a character list window.*/

public abstract class ListModule extends ManagerAssistant  {
   	 public Class getDutyClass() {
   	 	return ListModule.class;
   	 }
 	public String getDutyName() {
 		return "List Module";
   	 }
	public void getSubfunctions(){
		registerSubfunction(new FunctionExplanation("Sort Rows", "(A tool of the List Windows) Sorts the rows (taxa, characters, etc.) by the column touched", null, MesquiteModule.getRootImageDirectoryPath() +"sort.gif"));
		registerSubfunction(new FunctionExplanation("Select Same", "(The magic wand tool of the List Windows) Selects rows (taxa, characters, etc.) that match in the column touched", null, MesquiteModule.getRootImageDirectoryPath() +"wand.gif"));
		super.getSubfunctions();
	}
	
	
	public void employeeQuit(MesquiteModule m){
		if (m instanceof ListAssistant){
			ListWindow window = ((ListWindow)getModuleWindow());
			window.reviewColumnNumber();
				
		}
	}
	
	
	public abstract Class getAssistantClass();
	public abstract int getNumberOfRows();
	public abstract Object getMainObject();
	public abstract String getItemTypeName();
	public abstract String getItemTypeNamePlural();
	
	public String getTextContentsPreface(){
		String text = "";
		text += "Window showing " + getItemTypeNamePlural();
		Object obj = getMainObject();
		if (obj != null && obj instanceof Listable){
			text += " of ";
			if (obj instanceof FileElement)
				text += ((FileElement)obj).getTypeName() + " \'";
			text += ((Listable)obj).getName();
			if (obj instanceof FileElement)
				text += "\'";
		}
		if (obj instanceof Annotatable){
			Annotatable an = (Annotatable)obj;
			String s = an.getAnnotation();
			if (!StringUtil.blank(s))
				text += "\n\n----------------\nNotes:\n" + s + "\n---------------\n\n";
		}
		return text;
	}
	public void fileReadIn(MesquiteFile file){
		ListWindow window = ((ListWindow)getModuleWindow());
		if (window != null)
			window.requestFocus();
	}
	/*.................................................................................................................*/
	public boolean columnsMovable(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean rowsMovable(){
		return false;
	}
	/*...............................................................................................................*/
	public Undoer getSingleNameUndoInstructions(int row, String oldName, String s){
		return null;
	}

	public boolean resetMenusOnNameChange(){
		return false;
	}
	public Vector getAssistants() {
		Vector v = new Vector();
		for (int i=0; i< getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof ListAssistant) {
				v.addElement(obj);
			}
		}	
  	 	return v;
	}

	public void forceRecalculations() {
		if (getModuleWindow()== null)
			return;
		for (int i=0; i< getNumberOfEmployees(); i++) {
			Object obj =  getEmployeeVector().elementAt(i);
			if (obj instanceof ListAssistant) {
				((ListAssistant)obj).setTableAndObject(((TableWindow)getModuleWindow()).getTable(), ((TableWindow)getModuleWindow()).getCurrentObject());
			}
		}	
  	 	((ListWindow)getModuleWindow()).pleaseUpdate();  //todo: why do steps go blank if prob models changed???
	}
	/** returns a String of annotation for a row*/
	public abstract String getAnnotation(int row);

	/** sets the annotation for a row*/
	public abstract void setAnnotation(int row, String s, boolean notify);
	/** returns a String of explanation for a row*/
	public String getExplanation(int row){
		return null;
	}
	
	/** returns a String explaining a column*/
	public String getColumnExplanation(int column){
		return null;
	}
	public boolean rowsDeletable(){
		return false;
	}
	public boolean rowsShowable(){
		return false;
	}
	public void showItemAtRow(int row){
	}
	public boolean rowsAddable(){
		return false;
	}
	public boolean addRow(){
		return false;
	}
	public Object getAddColumnCompatibility(){
		return null;
	}
	/*.................................................................................................................*/
  	 public int getMyColumn(ListAssistant module) {
  	 	MesquiteWindow w =getModuleWindow();
  	 	if (w!=null && w instanceof ListWindow) {
  	 		return ((ListWindow)w).findAssistant(module);
  	 	}
  	 	return -1;
  	 }
	/*.................................................................................................................*/
	public boolean rowDeletable(int row){
		return rowsDeletable();
	}
	public abstract boolean deleteRow(int row, boolean notify);/*{
		return false;
	}
	*/
	public void aboutToDeleteRow(int row){  //called just before superclass deletes rows, in case specific module needs to prepare for deletion
	}
	public void aboutToDeleteRows(int first, int last, boolean notify){
		for (int i=last; i>=first; i--){
			aboutToDeleteRow(i);
		}
	}
	public boolean deleteRows(int first, int last, boolean notify){
		boolean touched = false;
		for (int i=last; i>=first; i--){
			if (deleteRow(i, notify))
				touched = true;
		}
		return touched;
	}
	/*.................................................................................................................*/
	public boolean isRowSelected(int row){
  	 	if (getModuleWindow()!=null && getModuleWindow() instanceof ListWindow)
  	 		return ((ListWindow)getModuleWindow()).isRowSelected(row);
  	 	return false;
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Hires a list assistant module", "[name of module]", commandName, "newAssistant")) {
 			if (getModuleWindow()!=null)
 				return getModuleWindow().doCommand(commandName, arguments, checker);
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	
	/*.................................................................................................................*/
  	 public void employeeOutputInvalid(MesquiteModule employee, MesquiteModule source) {
  	 	MesquiteWindow w =getModuleWindow();
  	 	if (w!=null && w instanceof ListWindow) {
  	 		((ListWindow)w).blankColumn(employee);
  	 	}
  	 }
	/*.................................................................................................................*/
  	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
  	 	if (getModuleWindow()!=null && getModuleWindow() instanceof ListWindow) {
  	 		((ListWindow)getModuleWindow()).reviewColumnWidths();
  	 		((ListWindow)getModuleWindow()).pleaseUpdate();  //todo: why do steps go blank if prob models changed???
  	 	}
  	 }
}





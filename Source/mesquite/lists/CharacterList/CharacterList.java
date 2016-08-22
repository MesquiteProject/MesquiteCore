/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharacterList;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class CharacterList extends ListModule {
	/*.................................................................................................................*/
	public String getName() {
		return "Character List";
	}
	public String getExplanation() {
		return "Makes windows listing characters and information about them." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharListAssistant.class, getName() + " uses various assistants to display columns showing information for each character.",
				"You can request that columns be shown using the Columns menu of the List of Characters Window. ");
		e.setEntryCommand("newAssistant");
		EmployeeNeed e2 = registerEmployeeNeed(CharListAssistantI.class, getName() + " uses various assistants to display columns showing information for each character.",
				"These are activated automatically. ");
		EmployeeNeed e3 = registerEmployeeNeed(CharSelectCoordinator.class, getName() + " uses various criteria to select characters in the List of Characters window.",
				"You can request selection methods using the List menu of the List of Characters Window. ");
	}
	/*.................................................................................................................*/
	int currentDataSet = 0;
	public CharacterData data = null;
	CharacterListWindow window;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Rename Matrix...", MesquiteModule.makeCommand("renameMatrix",  this));
		addMenuItem("Delete Matrix...", MesquiteModule.makeCommand("deleteMatrix",  this));
		addMenuItem("Show Matrix", MesquiteModule.makeCommand("showMatrix",  this));
		addMenuSeparator();
		return true;
	}
	/*.................................................................................................................*/
	public boolean showing(Object obj){
		if (obj instanceof String) {
			String arguments = (String)obj;
			int queryDataSet = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			return (queryDataSet == currentDataSet && window!=null);
		}
		if (obj instanceof CharacterData) {
			if (data == null){
				CharacterData d = (CharacterData)obj;
				int queryDataSet = getProject().getMatrixNumber(d);
				return (queryDataSet == currentDataSet && window!=null);
			}
			else {
				return data == obj;
			}
		}
		return false;
	}

	public Object getAddColumnCompatibility(){
		if (data == null)
			return null;
		else 
			return data.getStateClass();
	}
	/*.................................................................................................................*/
	public void showListWindow(Object obj){ ///TODO: change name to makeLIstWindow
		if (obj instanceof CharacterData) {
			data = (CharacterData)obj;
			currentDataSet = getProject().getMatrixNumber(data);
		}
		else 
			data = getProject().getCharacterMatrix(currentDataSet); //todo: 
		window = new CharacterListWindow(this);
		((CharacterListWindow)window).setObject(data);
		setModuleWindow(window);
		makeMenu("List");

		addMenuItem( "Save Selected as Set...", makeCommand("saveSelectedRows", this));
		addMenuSeparator();
		if (!MesquiteThread.isScripting()){
			CharListAssistant assistant= null;
			if (!(data instanceof MolecularData)){ //added 1. 06
				assistant = (CharListAssistant)hireNamedEmployee(CharListAssistant.class, "#DefaultCharOrder");
				if (assistant!= null){
					((CharacterListWindow)window).addListAssistant(assistant);
					assistant.setUseMenubar(false);
				}
			}
			assistant = (CharListAssistant)hireNamedEmployee(CharListAssistant.class, "#CharListInclusion");
			if (assistant!= null){
				((CharacterListWindow)window).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			/**/
			assistant = (CharListAssistant)hireNamedEmployee(CharListAssistant.class, "States");
			if (assistant!= null){
				((CharacterListWindow)window).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			/* default columns*/
			//	if (!(data instanceof MolecularData)){ //added 1. 06; removed after 2. 75
			assistant = (CharListAssistant)hireNamedEmployee(CharListAssistant.class, StringUtil.tokenize("#CharListPartition"));
			if (assistant!= null){
				((CharacterListWindow)window).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			//	}
			if (!(data instanceof MolecularData)){ 
				assistant = (CharListAssistant)hireNamedEmployee(CharListAssistant.class, StringUtil.tokenize("#CharListParsModels"));
				if (assistant!= null){
					((CharacterListWindow)window).addListAssistant(assistant);
					assistant.setUseMenubar(false);
				}
			}
			/*
			assistant = (CharListAssistant)hireNamedEmployee(CharListAssistant.class, StringUtil.tokenize("#CharListProbModels"));
			if (assistant!= null){
				((CharacterListWindow)window).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			 */
			if (data instanceof DNAData) {
				assistant = (CharListAssistant)hireNamedEmployee(CharListAssistant.class, StringUtil.tokenize("#CharListCodonPos"));
				if (assistant!= null){
					((CharacterListWindow)window).addListAssistant(assistant);
					assistant.setUseMenubar(false);
				}
			}
			/**/
		}

		resetContainingMenuBar();
		resetAllWindowsMenus();
	}
	/*...............................................................................................................*/
	public Undoer getSingleNameUndoInstructions(int row, String oldName, String s){
		if (window!=null)
			return new UndoInstructions(UndoInstructions.SINGLECHARACTERNAME, row, new MesquiteString(oldName), new MesquiteString(s),data,window.getTable());
		else 
			return new UndoInstructions(UndoInstructions.SINGLECHARACTERNAME, row, new MesquiteString(oldName), new MesquiteString(s),data,null);
	}
	/*.................................................................................................................*/
	public boolean columnsMovable(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean rowsMovable(){
		return true;
	}
	/* following required by ListModule*/
	public Object getMainObject(){
		return data;
	}
	public int getNumberOfRows(){
		if (data==null)
			return 0;
		else
			return data.getNumChars();
	}
	public Class getAssistantClass(){
		return CharListAssistant.class;
	}
	public boolean rowsDeletable(){
		return true;
	}
	public boolean deleteRow(int row, boolean notify){
		if (data!=null) {
			data.deleteCharacters(row, 1, notify);
			data.deleteInLinked(row, 1, notify);
			return true;
		}
		return false;
	}
	public boolean deleteRows(int first, int last, boolean notify){
		if (data!=null) {
			data.deleteCharacters(first, last-first+1, notify);
			data.deleteInLinked(first, last-first+1, notify);
			return true;
		}
		return false;
	}
	public void notifyRowDeletion(Object obj){
		if (obj == data) {
			((Listenable)obj).notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
			((CharacterData)obj).notifyInLinked(new Notification(MesquiteListener.PARTS_DELETED));
		}
	}
	/** returns either a String (if not editable) or a MesquiteString (if to be editable) */
	public String getAnnotation(int row){
		if (data !=null) {
			return data.getAnnotation(row);
		}
		return null;
	}
	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){
		if (data !=null) {
			data.setAnnotation(row, s);
		}
	}

	/** returns a String explaining the row */
	public String getExplanation(int row){
		return null;
	}
	public String getItemTypeName(){
		return "Character";
	}
	public String getItemTypeNamePlural(){
		return "Characters";
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (window==null || !window.isVisible())
			return null;
		Snapshot temp = new Snapshot();

		if (data != null)
			currentDataSet = getProject().getMatrixNumber(data);

		temp.addLine("setData " + currentDataSet); 

		if (window!=null)
			window.incorporateSnapshot(temp, file);
		//if (window!=null && !window.isVisible())
		temp.addLine("showWindow"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Renames the matrix", "[name of matrix]", commandName, "renameMatrix")) {
			if (data == null)
				return null;
			String token = ParseUtil.getFirstToken(arguments, new MesquiteInteger(0));
			if (StringUtil.blank(token) && !MesquiteThread.isScripting()) {
				token = MesquiteString.queryString(containerOfModule(), "Rename matrix", "New Name of Matrix:", data.getName(), 2);
				if (StringUtil.blank(token))
					return null;
			}
			data.setName(token);
			MesquiteWindow.resetAllTitles();

		}
		else if (checker.compare(this.getClass(), "Deletes the matrix", null, commandName, "deleteMatrix")) {
			if (data == null)
				return null;
			if (!MesquiteThread.isScripting())
				if (!AlertDialog.query(containerOfModule(), "Delete matrix?", "Are you sure you want to delete the matrix?  You cannot undo this."))
					return null;
			data.deleteMe(false);

		}
		else if (checker.compare(this.getClass(), "Shows the matrix", null, commandName, "showMatrix")) {
			data.showMatrix();
		}
		else if (checker.compare(this.getClass(), "Sets data set whose characters are to be displayed in this list window", "[number of data set]", commandName, "setData")) {
			int tempCurrDataSet = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (window!=null && MesquiteInteger.isCombinable(tempCurrDataSet) && tempCurrDataSet<getProject().getNumberCharMatrices()) {
				currentDataSet = tempCurrDataSet;
				data = getProject().getCharacterMatrix(currentDataSet);//checker.getFile(), 
				((CharacterListWindow)window).setObject(data);
				((CharacterListWindow)window).repaintAll();
			}
		}
		else if (checker.compare(this.getClass(), "Returns data set whose characters are listed", null, commandName, "getData")) {
			return ((CharacterListWindow)window).getCurrentObject();
		}
		else if (checker.compare(this.getClass(), "Saves the selected rows as a character set", null, commandName, "saveSelectedRows")) {
			((CharacterListWindow)window).saveSelectedRows();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void endJob(){
		if (data!=null) {
			data.removeListener(this);
			if (data.getTaxa()!=null)
				data.getTaxa().removeListener(this);
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		if (data!=null) {
			return "Character matrix: " + data.getName();
		}
		return "";
	}

	/*.................................................................................................................*/
	/** Requests a window to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
	}


}

/* ======================================================================== */
class CharacterListWindow extends ListWindow implements MesquiteListener {
	private int currentDataSet = 0;
	CharacterData data = null;
	CharSelectCoordinator selectionCoordinator;
	CharacterList listModule;
	public CharacterListWindow (CharacterList ownerModule) {
		super(ownerModule); //INFOBAR
		this.listModule = ownerModule;
		currentDataSet = ownerModule.currentDataSet;
		data = ownerModule.getProject().getCharacterMatrix(currentDataSet);
		getTable().setRowAssociable(data);
		MesquiteButton matrixButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("showMatrix",  ownerModule), null, true, MesquiteModule.getRootImageDirectoryPath() + "matrix.gif", 12, 16);
		matrixButton.setShowBackground(false);
		matrixButton.setButtonExplanation("Show Character Matrix Editor");
		setIcon(MesquiteModule.getRootImageDirectoryPath() + "windowIcons/listC.gif");

		table.addControlButton(matrixButton);
		ownerModule.hireAllCompatibleEmployees(CharTableAssistantI.class, data.getStateClass()); 
		Enumeration enumeration=ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof CharTableAssistantI) {
				CharTableAssistantI init = (CharTableAssistantI)obj;
				init.setTableAndData(getTable(), data, true);
			}
		}
		ownerModule.hireAllCompatibleEmployees(CharListAssistantI.class, data.getStateClass()); 
		enumeration=ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof CharListAssistantI) {
				CharListAssistantI init = (CharListAssistantI)obj;
				init.setTableAndData(getTable(), data);
			}
		}
		if (getTable() != null)
			getTable().setDropDown(-1, -1, true);
		selectionCoordinator = (CharSelectCoordinator)ownerModule.hireEmployee(CharSelectCoordinator.class, null);
		if (selectionCoordinator!=null)
			selectionCoordinator.setTableAndObject(getTable(), data, true);
	}
	/*
	protected ToolPalette makeToolPalette(){
		return new CharListPalette((CharacterList)ownerModule, this);
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		if (data!=null&& data.hasTitle()){
			if (data.uniquelyNamed())

				setTitle("Characters \"" + data.getName() + "\""); 
			else
				setTitle("Characters \"" + data.getName() + "\" [" + data.getID() + "]"); 

		}
		else if (data==null)
			setTitle("Characters (DATA NULL)" ); 
		else	if (data.uniquelyNamed())

			setTitle("Characters" ); 
		else
			setTitle("Characters [" + data.getID() + "]"); 

	}
	/*.................................................................................................................*/
	public String searchData(String s, MesquiteString commandResult) {
		if (StringUtil.blank(s) || data == null)
			return "<h2>Nothing to search for (searched: \"" + s + "\")</h2>";
		if (commandResult != null)
			commandResult.setValue((String)null);
		String listData = data.searchData(s, commandResult);

		if (!StringUtil.blank(listData))
			return "<h2>Matches to search string: \"" + s + "\"</h2>" + listData;
		else
			return "<h2>No matches found (searched: \"" + s + "\")</h2>";
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Selects character", "[number of character]", commandName, "selectCharacter")) {
			int which = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0));
			if (which >= 0 && which < data.getNumChars()){
				if (!table.isCellVisible(-1, which)){
					table.setFocusedCell(-1, which, true);
					table.repaintAll();
				}
				data.setSelected(which, !data.getSelected(which));
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public Object getCurrentObject(){
		return data;
	}

	public void setCurrentObject(Object obj){
		if (obj instanceof CharacterData) {
			if (data == null || data != obj){
				if (data!=null) {
					data.removeListener(this);
					if (data.getTaxa()!=null)
						data.getTaxa().removeListener(this);
				}
				data = (CharacterData)obj;
				data.addListener(this); //TODO: this needs to be done for taxon lists, etc.
				data.getTaxa().addListener(this);
			}
			getTable().setRowAssociable(data);
			getTable().setDropDown(-1, -1, true);
			resetTitle();
			if (selectionCoordinator!=null)
				selectionCoordinator.setTableAndObject(getTable(), data, true);
			Enumeration enumeration=ownerModule.getEmployeeVector().elements();
			while (enumeration.hasMoreElements()){
				Object ct = enumeration.nextElement();
				if (ct instanceof CharTableAssistantI) {
					CharTableAssistantI init = (CharTableAssistantI)ct;
					init.setTableAndData(getTable(), data, true);
				}
			}
			enumeration=ownerModule.getEmployeeVector().elements();
			while (enumeration.hasMoreElements()){
				Object ct = enumeration.nextElement();
				if (ct instanceof CharListAssistantI) {
					CharListAssistantI init = (CharListAssistantI)ct;
					init.setTableAndData(getTable(), data);
				}
			}
			getTable().synchronizeRowSelection(data);
		}
		super.setCurrentObject(obj);
	}
	public void focusInRow(int row){
		Enumeration enumeration=ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof CharListAssistantI) {
				CharListAssistantI init = (CharListAssistantI)obj;
				init.focusInRow(row);
			}
		}
	}
	public String getItemTypeName(){
		return "Character";
	}
	public String getItemTypeNamePlural(){
		return "Characters";
	}
	/*.................................................................................................................*/
	public void showSelectionPopup(Container cont, int x, int y) {
		if (selectionCoordinator!=null)
			selectionCoordinator.showPopUp(cont, x+5, y+5);
	}
	/*.................................................................................................................*/
	public void setRowName(int row, String name){
		if (data!=null) {
			String warning = data.checkNameLegality(row, name);
			if (warning == null)
				data.setCharacterName(row, name);
			else
				ownerModule.discreetAlert( warning);
		}
	}
	public String getRowName(int row){
		if (data!=null && row<data.getNumChars())
			return data.getCharacterName(row);
		else
			return null;
	}
	public String getRowNameForSorting(int row){
		if (data!=null && row<data.getNumChars()){
			if (!data.characterHasName(row))
				return "";
			return data.getCharacterName(row);
		}
		else
			return null;
	}
	/*...............................................................................................................*/
	public void setRowNameColor(Graphics g, int row){
		//		g.setColor(Color.black);
		if (data!=null) {
			if (!data.characterHasName(row))
				g.setColor(Color.gray);
		}
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (data==null || (obj instanceof Taxa &&  (Taxa)obj ==data.getTaxa())||(obj instanceof CharacterData && (CharacterData)obj ==data)) {
			data = null;
			if (ownerModule!=null) {
				ownerModule.iQuit();
			}
		}
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
		UndoReference undoReference = Notification.getUndoReference(notification);
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData && (CharacterData)obj ==data) {
			if (code==MesquiteListener.NAMES_CHANGED) {
				table.redrawRowNames();
				setUndoer(undoReference);
			}
			else if (code==MesquiteListener.SELECTION_CHANGED) {
				table.synchronizeRowSelection(data);
				table.repaintAll();
				setUndoer();
			}
			else if (code==MesquiteListener.PARTS_CHANGED || code==MesquiteListener.PARTS_MOVED || code==MesquiteListener.PARTS_DELETED || code==MesquiteListener.PARTS_ADDED) {
				table.offAllEditsDontRecord(); //1. 12
				table.setNumRows(data.getNumChars());
				table.synchronizeRowSelection(data);
				listModule.forceRecalculations();
				table.repaintAll();
				if (code==MesquiteListener.PARTS_MOVED || code==MesquiteListener.PARTS_ADDED)
					setUndoer(undoReference);
				else
					setUndoer();
			}
			else {
				table.setNumRows(data.getNumChars());
				table.synchronizeRowSelection(data);
				table.repaintAll();
			}
		}
		super.changed(caller, obj, notification);
	}

	//have selection be done by changing selection of characters, and listening here for selection to change selection of table!!!!!
	//likewise for data matrix 

	public void saveSelectedRows() {
		if (table.anyRowSelected()) {
			String nameOfCharSet = MesquiteString.queryString(this, "Name of character set", "Name of character set:", "Stored char. set");
			if (StringUtil.blank(nameOfCharSet))
				return;
			CharSelectionSet selectionSet= new CharSelectionSet(nameOfCharSet, data.getNumChars(), data);

			data.storeSpecsSet(selectionSet, CharSelectionSet.class);

			selectionSet.addToFile(data.getFile(), null, null);
			selectionSet.setNexusBlockStored("SETS");
			for (int ic= 0; ic< table.getNumRows(); ic++){
				if (table.isRowSelected(ic)) { 
					selectionSet.setSelected(ic, true);
				}
			}
			data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));
		}
	}


}


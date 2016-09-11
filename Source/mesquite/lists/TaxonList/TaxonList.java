/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxonList;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lists.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxonList extends ListModule {
	/*.................................................................................................................*/
	public String getName() {
		return "Taxon List";
	}
	public String getExplanation() {
		return "Makes windows listing taxa and information about them." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e00 = registerEmployeeNeed(TaxaSelectCoordinator.class, "The List of Taxa window has facilities to select taxa.",
				"This module is started automatically. ");
		EmployeeNeed e0 = registerEmployeeNeed(TaxaListAssistantI.class, "The List of Taxa window can display columns showing information for each taxon.",
				"This module is started automatically. ");
		EmployeeNeed e = registerEmployeeNeed(TaxonListAssistant.class, "The List of Taxa window can display columns showing information for each taxon.",
				"You can request that columns be shown using the Columns menu of the List of Taxa Window. ");
		e.setEntryCommand("newAssistant");
		EmployeeNeed e2 = registerEmployeeNeed(TaxonUtility.class, "Utilities operating on taxa can be used through the List of Taxa window.",
				"You can request such a utility using the Taxon Utilities submenu of the List menu of the List of Taxa Window. ");
		EmployeeNeed e3 = registerEmployeeNeed(TaxonNameAlterer.class, "Utilities to change taxon names can be used through the List of Taxa window.",
				"You can request such a utility using the Taxon Names submenu of the List menu of the List of Taxa Window. ");
	}
	Taxa taxa;
	TaxonListWindow window;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Rename Block of Taxa...", MesquiteModule.makeCommand("renameBlock",  this));
		addMenuItem("Delete Block of Taxa...", MesquiteModule.makeCommand("deleteBlock",  this));
		addMenuSeparator();
		return true;
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == taxa) {
			iQuit();
		}
	}
	/*...............................................................................................................*/
	public Undoer getSingleNameUndoInstructions(int row, String oldName, String s){
		if (window!=null)
			return new UndoInstructions(UndoInstructions.SINGLETAXONNAME, row, new MesquiteString(oldName), new MesquiteString(s),taxa,window.getTable());
		else 
			return new UndoInstructions(UndoInstructions.SINGLETAXONNAME, row, new MesquiteString(oldName), new MesquiteString(s),taxa, null);
	}
	public boolean showing(Object obj){
		if (obj instanceof String) {
			String arguments = (String)obj;
			int query = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			return (query == getProject().getTaxaNumber(taxa) && getModuleWindow()!=null);
		}
		if (obj instanceof Taxa) {
			return (this.taxa == obj && getModuleWindow()!=null);
		}
		return false;
	}

	public void showListWindow(Object obj){
		if (obj instanceof Taxa) {
			taxa = (Taxa)obj;
			window = new TaxonListWindow(this);
			setModuleWindow( window);

			window.setObject(taxa);
			//makeMenu("Taxon_List");
			makeMenu("List");

			MesquiteSubmenuSpec mss2 = addSubmenu(null, "Taxon Utilities", MesquiteModule.makeCommand("doUtility",  this));
			mss2.setList(TaxonUtility.class);
			MesquiteSubmenuSpec mss3 = addSubmenu(null, "Taxon Names", MesquiteModule.makeCommand("doNames",  this));
			mss3.setList(TaxonNameAlterer.class);
			addMenuItem( "Save selected as set...", makeCommand("saveSelectedRows", this));
			addMenuSeparator();

			/* default columns*
			TaxonListAssistant assistant = (TaxonListAssistant)hireNamedEmployee(TaxonListAssistant.class, StringUtil.tokenize("#DefaultTaxaOrder"));
			if (assistant!= null){
				((TaxonListWindow)window).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			/* removed as default v. 2. 01; returned for v. 2. 5 */
			TaxonListAssistant assistant = (TaxonListAssistant)hireNamedEmployee(TaxonListAssistant.class, StringUtil.tokenize("#TaxonListCurrPartition"));
			if (assistant!= null){
				((TaxonListWindow)window).addListAssistant(assistant);
				assistant.setUseMenubar(false);
			}
			/*	*/
			resetContainingMenuBar();
			resetAllWindowsMenus();
		}
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
	public Object getMainObject(){
		return taxa;
	}
	public int getNumberOfRows(){
		if (taxa==null)
			return 0;
		else
			return taxa.getNumTaxa();
	}
	public Class getAssistantClass(){
		return TaxonListAssistant.class;
	}
	public String getItemTypeName(){
		return "Taxon";
	}
	public String getItemTypeNamePlural(){
		return "Taxa";
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
	public boolean rowsDeletable(){
		return true;
	}
	public boolean deleteRow(int row, boolean notify){
		if (taxa!=null && taxa.getNumTaxa() > 1) {
			if (taxa.isEditInhibited()){
				discreetAlert("You cannot delete taxa; the taxa block is locked.");
				return false;
			}

			taxa.deleteTaxa(row,1, notify);
			return true;
		}
		return false;
	}
	public boolean deleteRows(int first, int last, boolean notify){
		if (taxa!=null) {
			if (taxa.isEditInhibited()){
				discreetAlert("You cannot delete taxa; the taxa block is locked.");
				return false;
			}
			taxa.deleteTaxa(first, last-first+1, notify);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public boolean rowsAddable(){
		return true;
	}
	public boolean addRow(){
		if (taxa!=null) {
			if (taxa.isEditInhibited()){
				discreetAlert("You cannot add taxa; the taxa block is locked.");
				return false;
			}
			taxa.addTaxa(taxa.getNumTaxa(), 1, false);
			return true;
		}
		return false;
	}
	public void focusInRow(int row){
	}
	/** returns either a String (if not editable) or a MesquiteString (if to be editable) */
	public String getAnnotation(int row){
		if (taxa !=null) {
			return taxa.getAnnotation(row);
		}
		return null;
	}
	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){
		if (taxa !=null) {
			taxa.setAnnotation(row, s);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (getModuleWindow()==null || !getModuleWindow().isVisible())
			return null;
		Snapshot temp = new Snapshot();


		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa)); 
		if (getModuleWindow()!=null)
			getModuleWindow().incorporateSnapshot(temp, file);
		//if (getModuleWindow()!=null && !getModuleWindow().isVisible())
		temp.addLine("showWindow"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Renames the block of taxa", "[name of block]", commandName, "renameBlock")) {
			if (taxa == null)
				return null;
			String token = ParseUtil.getFirstToken(arguments, new MesquiteInteger(0));
			if (StringUtil.blank(token) && !MesquiteThread.isScripting()) {
				token = MesquiteString.queryString(containerOfModule(), "Rename block of taxa", "New name for block of taxa:", taxa.getName(), 2);
				if (StringUtil.blank(token))
					return null;
			}
			taxa.setName(token);
			MesquiteWindow.resetAllTitles();

		}
		else if (checker.compare(this.getClass(), "Deletes the block of taxa", null, commandName, "deleteBlock")) {
			if (taxa == null)
				return null;
			if (!MesquiteThread.isScripting())
				if (!AlertDialog.query(containerOfModule(), "Delete block of taxa?", "Are you sure you want to delete the block of taxa?  You cannot undo this."))
					return null;
			taxa.deleteMe(false);

		}
		else if (checker.compare(this.getClass(), "Sets the taxa block used", "[block reference, number, or name]", commandName, "setTaxa")) {
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (getModuleWindow()!=null && t!=null){
				if (taxa!=null)
					taxa.removeListener(this);
				taxa = t;
				if (taxa!=null)
					taxa.addListener(this);
				((TaxonListWindow)getModuleWindow()).setObject(taxa);
				((TaxonListWindow)getModuleWindow()).repaintAll();
				return taxa;
			}
		}
		else if (checker.compare(this.getClass(), "Hires utility module to operate on the taxa", "[name of module]", commandName, "doUtility")) {
			if (taxa !=null){
				TaxonUtility tda= (TaxonUtility)hireNamedEmployee(TaxonUtility.class, arguments);
				if (tda!=null) {
					boolean a = tda.operateOnTaxa(taxa);
					if (!tda.pleaseLeaveMeOn())
						fireEmployee(tda);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires utility module to alter names of the taxa", "[name of module]", commandName, "doNames")) {
			if (taxa !=null && getModuleWindow() != null && ((TableWindow)getModuleWindow()).getTable()!=null){
				TaxonNameAlterer tda= (TaxonNameAlterer)hireNamedEmployee(TaxonNameAlterer.class, arguments);
				if (tda!=null) {
					UndoReference undoReference = new UndoReference(new UndoInstructions(UndoInstructions.ALLTAXONNAMES,taxa, taxa), this);
					boolean a = tda.alterTaxonNames(taxa, ((TableWindow)getModuleWindow()).getTable());
					fireEmployee(tda);
					if (a)
						taxa.notifyListeners(this, new Notification(NAMES_CHANGED, undoReference));
				}
			}
		}
		else if (checker.compare(this.getClass(), "Returns the taxa block shown", null, commandName, "getTaxa")) {
			return taxa;
		}
		else if (checker.compare(this.getClass(), "Saves selected taxa as a TAXSET", null, commandName, "saveSelectedRows")) {
			((TaxonListWindow)getModuleWindow()).saveSelectedRows();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
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
class TaxonListWindow extends ListWindow {
	private Taxa taxa = null;
	TaxaSelectCoordinator selectionCoordinator;
	public TaxonListWindow (TaxonList ownerModule) {
		super(ownerModule);

		taxa = ownerModule.taxa;
		if (taxa == null)
			taxa = ownerModule.getProject().getTaxa(0);
		ownerModule.hireAllEmployees(TaxaTableAssistantI.class); 
		Enumeration enumeration=ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof TaxaTableAssistantI) {
				TaxaTableAssistantI init = (TaxaTableAssistantI)obj;
				init.setTableAndTaxa(getTable(), taxa, true);
			}
		}
		setIcon(MesquiteModule.getRootImageDirectoryPath() + "windowIcons/listT.gif");
		ownerModule.hireAllEmployees(TaxaListAssistantI.class); 
		enumeration=ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof TaxaListAssistantI) {
				TaxaListAssistantI init = (TaxaListAssistantI)obj;
				init.setTableAndTaxa(getTable(), taxa);
			}
		}
		if (getTable() != null)
			getTable().setDropDown(-1, -1, true);
		selectionCoordinator = (TaxaSelectCoordinator)ownerModule.hireEmployee(TaxaSelectCoordinator.class, null);
		if (selectionCoordinator!=null)
			selectionCoordinator.setTableAndObject(getTable(), taxa, true);
	}
	public void focusInRow(int row){
		try {
			Enumeration enumeration=ownerModule.getEmployeeVector().elements();
			while (enumeration.hasMoreElements()){
				Object obj = enumeration.nextElement();
				if (obj instanceof TaxaListAssistantI) {
					TaxaListAssistantI init = (TaxaListAssistantI)obj;
					init.focusInRow(row);
				}
			}
		}
		catch (NullPointerException e){
		}
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		if (taxa==null || StringUtil.blank(taxa.getName()) || "untitled".equalsIgnoreCase(taxa.getName()))
			setTitle("Taxa"); 
		else
			setTitle("Taxa \"" + taxa.getName() + "\""); 
	}
	/*...............................................................................................................*/
	NameReference colorNameRef = NameReference.getNameReference("color");
	public void setRowNameColor(Graphics g, int row){
		//		g.setColor(Color.black);
		if (taxa!=null ) {
			long c = taxa.getAssociatedLong(colorNameRef, row);
			if (MesquiteLong.isCombinable(c))
				g.setColor(ColorDistribution.getStandardColor((int)c));
		}
	}
	/*.................................................................................................................*/
	public String searchData(String s, MesquiteString commandResult) {
		if (StringUtil.blank(s) || taxa == null)
			return "<h2>Nothing to search for (searched: \"" + s + "\")</h2>";
		if (commandResult != null)
			commandResult.setValue((String)null);
		String listData = taxa.searchData(s, commandResult);

		if (!StringUtil.blank(listData))
			return "<h2>Matches to search string: \"" + s + "\"</h2>" + listData;
		else
			return "<h2>No matches found (searched: \"" + s + "\")</h2>";
	}
	public void addRowsNotify(int first, int num) {
		taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {first, num}));
		//	this causes problems, as the taxa are added, AND numtaxa in CharacterData is incremented twice
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Selects taxa", "[number of taxon]", commandName, "selectTaxon")) {
			int which = MesquiteInteger.fromFirstToken(arguments, new MesquiteInteger(0));
			if (which >= 0 && which < taxa.getNumTaxa()){
				if (!table.isCellVisible(-1, which)){
					table.setFocusedCell(-1, which, true);
					table.repaintAll();
				}
				taxa.setSelected(which, !taxa.getSelected(which));
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
		else if (checker.compare(this.getClass(), "Shows taxon", "[id of taxa block][number of taxon]", commandName, "showTaxon")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			long whichTaxaBlock = MesquiteInteger.fromString(arguments, pos);
			if (whichTaxaBlock != taxa.getID())
				return null;
			int which = MesquiteInteger.fromString(arguments, pos);
			if (which >= 0 && which < taxa.getNumTaxa()){
				if (!table.isCellVisible(-1, which)) {
					table.setFocusedCell(-1, which, true);
					table.repaintAll();
				}
			}
			return null;
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public Object getCurrentObject(){
		return taxa;
	}
	public void setCurrentObject(Object obj){
		if (obj == null && taxa != null)
			taxa.removeListener(this);
		if (obj instanceof Taxa) {
			if (taxa!=null)
				taxa.removeListener(this);
			taxa = (Taxa)obj;
			getTable().setRowAssociable(taxa);
			if (taxa!=null)
				taxa.addListener(this);
			Enumeration enumeration=ownerModule.getEmployeeVector().elements();
			while (enumeration.hasMoreElements()){
				Object objc = enumeration.nextElement();
				if (objc instanceof TaxaTableAssistantI) {
					TaxaTableAssistantI init = (TaxaTableAssistantI)objc;
					init.setTableAndTaxa(getTable(), taxa, true);
				}
			}
			enumeration=ownerModule.getEmployeeVector().elements();
			while (enumeration.hasMoreElements()){
				Object objc = enumeration.nextElement();
				if (objc instanceof TaxaListAssistantI) {
					TaxaListAssistantI init = (TaxaListAssistantI)objc;
					init.setTableAndTaxa(getTable(), taxa);
				}
			}
			if (selectionCoordinator!=null)
				selectionCoordinator.setTableAndObject(getTable(), taxa, true);
			getTable().synchronizeRowSelection(taxa);
			resetTitle();
		}
		super.setCurrentObject(obj);
	}
	/*.................................................................................................................*/
	public void showSelectionPopup(Container cont, int x, int y) {
		if (selectionCoordinator!=null)
			selectionCoordinator.showPopUp(cont, x+5, y+5);
	}
	public String getItemTypeName(){
		return "Taxon";
	}
	public void setRowName(int row, String name){
		if (taxa!=null) {
			String warning = taxa.checkNameLegality(row, name);
			if (warning == null)
				taxa.setTaxonName(row, name);
			else
				ownerModule.discreetAlert( warning);
		}
	}
	public String getRowName(int row){
		if (taxa!=null){
			if (row>=0 && row<taxa.getNumTaxa())
				return taxa.getTaxonName(row);
			return null;
		}
		else
			return null;
	}
	public String getRowNameForSorting(int row){
		return getRowName(row);
	}
	public void saveSelectedRows() {
		if (table.anyRowSelected()) {
			String nameOfTaxonSet = MesquiteString.queryString(this, "Name of taxon set", "Name of taxon set:", "Stored taxon set");
			if (StringUtil.blank(nameOfTaxonSet))
				return;
			TaxaSelectionSet selectionSet= new TaxaSelectionSet(nameOfTaxonSet, taxa.getNumTaxa(), taxa);

			taxa.storeSpecsSet(selectionSet, TaxaSelectionSet.class);

			selectionSet.addToFile(taxa.getFile(), null, null);
			selectionSet.setNexusBlockStored("SETS");
			for (int ic= 0; ic< table.getNumRows(); ic++){
				if (table.isRowSelected(ic)) { 
					selectionSet.setSelected(ic, true);
				}
			}
			taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));
		}
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa &&  (Taxa)obj ==taxa && ownerModule !=null)
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
		if (obj instanceof Taxa && (Taxa)obj ==taxa) {
			if (code==MesquiteListener.NAMES_CHANGED) {
				table.redrawRowNames();
				setUndoer(undoReference);
			}
			else if (code==MesquiteListener.SELECTION_CHANGED) {
				table.synchronizeRowSelection(taxa);
				table.repaintAll();
			}
			else if (code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_DELETED || code==MesquiteListener.PARTS_CHANGED || code==MesquiteListener.PARTS_MOVED) {
				table.offAllEditsDontRecord(); //1. 12
				table.setNumRows(taxa.getNumTaxa());
				table.synchronizeRowSelection(taxa);
				table.repaintAll();
				if (code==MesquiteListener.PARTS_MOVED || code==MesquiteListener.PARTS_ADDED)
					setUndoer(undoReference);
				else
					setUndoer();
			}
			else {
				table.repaintAll();
			}
		}
		super.changed(caller, obj, notification);
	}

}



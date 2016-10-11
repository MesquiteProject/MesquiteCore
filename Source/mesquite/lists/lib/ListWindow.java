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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public abstract class ListWindow extends TableWindow implements KeyListener, MesquiteListener {
	protected ListTable table;
	private int windowWidth=360;
	private int windowHeight=400;
	ListModule owner;
	TableTool arrowTool, ibeamTool, wandTool, sortTool;
	MesquitePopup selectionPopup;
	MesquiteBoolean equals, greaterThan, lessThan, withinExistingSelection, nonMatching, deselectWandTouched;
	boolean defaultEquals = true;
	boolean defaultGT = false;
	boolean defaultLT = false;
	boolean defaultWithinSelection = false;
	boolean defaultNonMatching = false;
	boolean defaultDeselectWandTouched = false;

	MesquiteCollator collator;
	MesquiteCommand deleteCommand, deleteColumnCommand;

	/** constructor to be used only for accumulating list of commands*/
	public ListWindow () {
	}
	public ListWindow (ListModule ownerModule) {
		super(ownerModule, true); //INFOBAR
		equals = new MesquiteBoolean(defaultEquals);
		greaterThan = new MesquiteBoolean(defaultGT);
		lessThan = new MesquiteBoolean(defaultLT);
		withinExistingSelection = new MesquiteBoolean(defaultWithinSelection);
		nonMatching = new MesquiteBoolean(defaultNonMatching);
		deselectWandTouched = new MesquiteBoolean(defaultDeselectWandTouched);
		setWindowSize(windowWidth, windowHeight);
		ownerModule.setModuleWindow(this);
		owner = ownerModule;
		int numItems = owner.getNumberOfRows();
		table = new ListTable (numItems, 0, windowWidth, windowHeight, 130, this, ownerModule);
		setCurrentObject(owner.getMainObject());
		if (getCurrentObject() instanceof Associable)
			table.setRowAssociable((Associable)getCurrentObject());
		collator = new MesquiteCollator();

		//TODO:  note: strangely enough, the commands of the arrow tools are not used, since it uses default behavior of table
		String selectExplanation = "Select items in table.  If a single cell in the table is touched, and that cell is editable, then you will be given a little box in which you can edit the ";
		selectExplanation += "contents of the cell.  By holding down shift while clicking, the selection will be extended from the first to the last touched cell. ";
		selectExplanation += " A block of cells can be selected either by using shift-click to extend a previous selection, or by clicking on a cell and dragging with the mouse button still down";
		selectExplanation += " Discontinous selections are allowed, and can be obtained by a \"meta\"-click (right mouse button click, or command-click on a MacOS system). ";
		arrowTool = new TableTool(this, "arrow", MesquiteModule.getRootImageDirectoryPath(),"arrow.gif", 4,2,"Select", selectExplanation, null, null, null); //MesquiteModule.makeCommand("arrowTouchCell",  this) , MesquiteModule.makeCommand("arrowDragCell",  this), MesquiteModule.makeCommand("arrowDropCell",  this));
		arrowTool.setIsArrowTool(true);
		addTool(arrowTool);
		setCurrentTool(arrowTool);
		ibeamTool = new TableTool(this, "ibeam", MesquiteModule.getRootImageDirectoryPath(),"ibeam.gif", 7,7,"Edit", "This tool can be used to edit elements in the table.", MesquiteModule.makeCommand("editCell",  (Commandable)table) , null, null);
		ibeamTool.setWorksOnRowNames(true);
		ibeamTool.setWorksOnMatrixPanel(false);
		addTool(ibeamTool);
		String wandExplanation = "Select rows in table according to whether they have the same value as the cell touched in that column.  ";
		wandExplanation += "By holding down shift while clicking, the selection will be added to the previous selection. ";
		wandTool = new TableTool(this, "wand", MesquiteModule.getRootImageDirectoryPath(),"wand.gif", 4,4,"Select rows with same", wandExplanation, MesquiteModule.makeCommand("wandTouch",  this) , null, null);
		wandTool.setOptionsCommand(MesquiteModule.makeCommand("wandOptions", this));
		addTool(wandTool);

		if (owner.rowsMovable()){
			String sortExplanation = "Sorts items (that is, rows) in table from lowest to highest values in the column touched. ";
			sortExplanation += "By holding down option while clicking, items will be sorted from highest to lowest. ";
			sortTool = new TableTool(this, "sort", MesquiteModule.getRootImageDirectoryPath(),"sort.gif", 4,4,"Sort rows", sortExplanation, MesquiteModule.makeCommand("sortTouch",  this) , null, null);
			sortTool.setOptionImageFileName("revSort.gif", 8, 13);
			sortTool.setWorksOnRowNames(true);
			addTool(sortTool);
		}

		//System.out.println("number of chars " + numItems);
		table.setColumnWidthsUniform(100);
		table.setUserAdjust(MesquiteTable.RESIZE, MesquiteTable.RESIZE);
		addToWindow(table);
		table.setVisible(true);
		table.setLocation(0,0);

		MesquiteMenuSpec acm = ownerModule.addAuxiliaryMenu("Columns");
		MesquiteMenuItemSpec mss = ownerModule.addModuleMenuItems(acm, ownerModule.makeCommand("newAssistant", this), owner.getAssistantClass());
		if (mss != null)
			mss.setCompatibilityCheck(ownerModule.getAddColumnCompatibility());
		if (columnsRemovable()){
			ownerModule.addMenuItem(acm, "-", null);
			ownerModule.addMenuItem(acm, "Move Selected Columns...", ownerModule.makeCommand("moveSelectedColumns", this));
			ownerModule.addMenuItem(acm, "Hide Selected Columns", deleteColumnCommand = ownerModule.makeCommand("deleteSelectedColumns", this));
			
		}



		//ownerModule.addMenuSeparator();
		if (owner.rowsAddable())
			ownerModule.addMenuItem( "Add " + owner.getItemTypeNamePlural() + "...", ownerModule.makeCommand("addRows", this));
		if (owner.rowsDeletable()) {
			ownerModule.addMenuItem( "Delete Selected " + owner.getItemTypeNamePlural(), deleteCommand = ownerModule.makeCommand("deleteSelectedRows", this));
			MesquiteWindow.addKeyListener(this, this);
		}
		if (owner.rowsMovable()) {
			MesquiteMenuItemSpec mm = ownerModule.addMenuItem( "Move Selected " + owner.getItemTypeNamePlural() + " To...", ownerModule.makeCommand("moveSelectedTo", this));
			mm.setShortcut(KeyEvent.VK_M);
		}
		setShowExplanation(true);
		setShowAnnotation(true);
		table.requestFocusInWindow();
		MesquiteWindow.addKeyListener(this, table);
		resetTitle();
	}
	public void requestFocus(){
		table.requestFocus();
	}
	public String getFindLabel(){
		return "Find String in Table...";
	}
	public void keyTyped(KeyEvent e){
		int mod = MesquiteEvent.getModifiers(e);
		if (!MesquiteEvent.commandOrControlKeyDown(mod)) {
			if (getMode() != InfoBar.GRAPHICS || annotationHasFocus())
				return;

			if (owner.rowsDeletable() && (table == null || (table.anyRowSelected() && !table.editingAnything())) && (e.getKeyChar()== '\b' || e.getKeyCode()== KeyEvent.VK_BACK_SPACE || e.getKeyCode()== KeyEvent.VK_DELETE)) {
				deleteCommand.doItMainThread(null, "", this);
			}
			else if (columnsRemovable() && (table == null || (table.anyColumnSelected() && !table.editingAnything())) && (e.getKeyChar()== '\b' || e.getKeyCode()== KeyEvent.VK_BACK_SPACE || e.getKeyCode()== KeyEvent.VK_DELETE)) {
				deleteColumnCommand.doItMainThread(null, "", this);
			}
		}

	}
	public void keyPressed(KeyEvent e){
	}

	public void keyReleased(KeyEvent e){
	}
	public void dispose(){
		super.dispose();
	}
	/*.................................................................................................................*/
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == getCurrentObject() && getCurrentObject() instanceof Associable && caller != this && caller != table) {
			table.synchronizeRowSelection((Associable)getCurrentObject());
		}
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}

	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public abstract void resetTitle();

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (equals.getValue()!=defaultEquals)
			temp.addLine("toggleEquals " + equals.toOffOnString());
		if (greaterThan.getValue()!=defaultGT)
			temp.addLine("toggleGT " + greaterThan.toOffOnString());
		if (lessThan.getValue()!=defaultLT)
			temp.addLine("toggleLT " + lessThan.toOffOnString());
		if (withinExistingSelection.getValue()!=defaultWithinSelection)
			temp.addLine("toggleWithinSelection " + withinExistingSelection.toOffOnString());
		if (nonMatching.getValue()!=defaultNonMatching)
			temp.addLine("toggleNonMatching " + nonMatching.toOffOnString());
		if (deselectWandTouched.getValue()!=defaultDeselectWandTouched)
			temp.addLine("toggleDeselectWandTouched " + deselectWandTouched.toOffOnString());

		for (int i = 0; i<ownerModule.getNumberOfEmployees(); i++) { //if employee is number for character list, then hire indirectly its 
			MesquiteModule e=(MesquiteModule)ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof ListAssistant) {
				temp.addLine("newAssistant ", e);
			}
		}
		temp.incorporate(super.getSnapshot(file), false);
		return temp;
	}
	public abstract void setRowName(int row, String name);
	public abstract String getRowName(int row);
	public abstract String getRowNameForSorting(int row);
	public int getSingleNameUndoConstant() {
		return UndoInstructions.CANTUNDO;
	}
	public boolean columnsRemovable(){
		return true;
	}
	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int column, int row){
		ListAssistant assistant = findAssistant(column);
		if (assistant != null)
			return assistant.isCellEditable(row);
		return table.cellsEditableByDefault();
	}
	/*...............................................................................................................*/
	/** returns whether or not a row name of table is editable.*/
	public boolean isRowNameEditable(int row){
		return table.checkRowNameEditable(row);
	}
	/*...............................................................................................................*/
	/** returns whether or not a column name of table is editable.*/
	public boolean isColumnNameEditable(int column){
		return table.checkColumnNameEditable(column);
	}
	/*...............................................................................................................*/
	public void setRowNameColor(Graphics g, int row){
	}
	/** Returns menu location for item to bring the window to the for (0 = custom or don't show; 1 = system area of Windows menu; 2 = after system area of Windows menu)*/
	public int getShowMenuLocation(){
		return 0;
	}
	/*.................................................................................................................*/
	
	public static void BubbleSort( int [ ] num )
	{
	     boolean swapOccurred = true;   
	     int temp;  
	     while (swapOccurred) {
	    	 swapOccurred= false;    //set flag to false awaiting a possible swap
	            for(int j=0;  j < num.length -1;  j++) {
	                   if ( num[ j ] < num[j+1] )  {
	                           temp = num[ j ];                //swap elements
	                           num[ j ] = num[ j+1 ];
	                           num[ j+1 ] = temp;
	                           swapOccurred = true;              //shows a swap occurred  
	                  } 
	            } 
	      } 
	} 

	/*.................................................................................................................*/
	private void doWandTouch(String arguments){
		MesquiteInteger io = new MesquiteInteger(0);
		int column= MesquiteInteger.fromString(arguments, io);
		int row= MesquiteInteger.fromString(arguments, io);
		if (MesquiteInteger.isNonNegative(column)&& (MesquiteInteger.isNonNegative(row))) {  // it is in a real row and column that matters for the wand
			boolean deselects =deselectWandTouched.getValue();
			
			if (getCurrentObject() instanceof Associable){
				Associable assoc = (Associable)getCurrentObject();
				boolean withinSelection = withinExistingSelection.getValue() && assoc.getSelected(row);
				if (arguments.indexOf("shift")<0 && !withinSelection && !deselects)
					assoc.deselectAll();
				table.offAllEdits();
				String text = table.getMatrixText(column, row);
				for (int i=0; i<table.getNumRows(); i++){
					boolean satisfies = satisfiesCriteria(text, table.getMatrixText(column, i));
					if (nonMatching.getValue())
						satisfies = !satisfies;
					if (withinSelection) {
						if (assoc.getSelected(i)){
							if (!deselects)
								assoc.setSelected(i, satisfies);
							else if (satisfies)
								assoc.setSelected(i, false);
						}
					}
					else if (satisfies) {
						assoc.setSelected(i, !deselects);
					}
				}
				assoc.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
			else {
				boolean withinSelection = withinExistingSelection.getValue() && table.isRowSelected(row);
				if (arguments.indexOf("shift")<0 && !withinSelection && !deselects)
					table.deselectAll();
				table.offAllEdits();
				String text = table.getMatrixText(column, row);
				for (int i=0; i<table.getNumRows(); i++){
					boolean satisfies = satisfiesCriteria(text, table.getMatrixText(column, i));
					if (nonMatching.getValue())
						satisfies = !satisfies;
					if (withinSelection) {
						if (table.isRowSelected(i)){
							if (!deselects)
								if (satisfies)
									table.selectRow(i);
								else
									table.deselectRow(i);
							else if (satisfies)
								table.deselectRow(i);
						}
					}
					else if (satisfies) {
						if (!deselects)
							table.selectRow(i);
						else
							table.deselectRow(i);
					} 
				}
				table.repaintAll();
			}
		}
	
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hides the list window", null, commandName, "hideWindow")) {
			hide();
		}
		else if (checker.compare(this.getClass(), "Deletes the selected columns", null, commandName, "deleteSelectedColumns")) {
			deleteSelectedColumns();
		}
		else if (checker.compare(this.getClass(), "Moves the selected columns", null, commandName, "moveSelectedColumns")) {
			moveSelectedColumns();
		}
		else if (checker.compare(this.getClass(), "Present the popup menu to select options for magic wand", null, commandName, "wandOptions")) {
			MesquiteButton button = wandTool.getButton();
			if (button!=null){
				MesquiteInteger io = new MesquiteInteger(0);
				int x= MesquiteInteger.fromString(arguments, io); //getting x and y from arguments
				int y= MesquiteInteger.fromString(arguments, io);
				MesquitePopup popup = new MesquitePopup(button);
				MesquiteCheckMenuItem equalsItem = new MesquiteCheckMenuItem("Equal", ownerModule, MesquiteModule.makeCommand("toggleEquals", this), null, null);
				equalsItem.set(equals.getValue());
				popup.add(equalsItem);
				MesquiteCheckMenuItem greaterThanItem = new MesquiteCheckMenuItem("Greater than", ownerModule, MesquiteModule.makeCommand("toggleGT", this), null, null);
				greaterThanItem.set(greaterThan.getValue());
				popup.add(greaterThanItem);
				MesquiteCheckMenuItem lessThanItem = new MesquiteCheckMenuItem("Less than", ownerModule, MesquiteModule.makeCommand("toggleLT", this), null, null);
				lessThanItem.set(lessThan.getValue());
				popup.add(lessThanItem);
				popup.add(new MenuItem("-"));
				MesquiteCheckMenuItem withinSelectionItem = new MesquiteCheckMenuItem("Within Existing Selection", ownerModule, MesquiteModule.makeCommand("toggleWithinSelection", this), null, null);
				withinSelectionItem.set(withinExistingSelection.getValue());
				popup.add(withinSelectionItem);
				MesquiteCheckMenuItem notMatchingItem = new MesquiteCheckMenuItem("Choose Non-Matching", ownerModule, MesquiteModule.makeCommand("toggleNonMatching", this), null, null);
				notMatchingItem.set(nonMatching.getValue());
				popup.add(notMatchingItem);
				MesquiteCheckMenuItem deselectWandTouchedItem = new MesquiteCheckMenuItem("Deselect rather than Select", ownerModule, MesquiteModule.makeCommand("toggleDeselectWandTouched", this), null, null);
				deselectWandTouchedItem.set(deselectWandTouched.getValue());
				popup.add(deselectWandTouchedItem);
				popup.showPopup(x,y+6);
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value equal to that in cell touched", "[on = selects equal; off]", commandName, "toggleEquals")) {
			boolean current = equals.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			equals.toggleValue(ParseUtil.getFirstToken(arguments, io));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value greater than that in cell touched", "[on = selects greater than; off]", commandName, "toggleGT")) {
			boolean current = greaterThan.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			greaterThan.toggleValue(ParseUtil.getFirstToken(arguments, io));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects taxa with value less than that in cell touched", "[on = selects less than; off]", commandName, "toggleLT")) {
			boolean current = lessThan.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			lessThan.toggleValue(ParseUtil.getFirstToken(arguments, io));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects within list items that are otherwise already selected", "[on = selects within; off]", commandName, "toggleWithinSelection")) {
			boolean current = withinExistingSelection.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			withinExistingSelection.toggleValue(ParseUtil.getFirstToken(arguments, io));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects items that do NOT match that touched", "[on = selects non-matching; off]", commandName, "toggleNonMatching")) {
			boolean current = nonMatching.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			nonMatching.toggleValue(ParseUtil.getFirstToken(arguments, io));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand deselects items rather than selects them", "[on = deselects; off]", commandName, "toggleDeselectWandTouched")) {
			boolean current = deselectWandTouched.getValue();
			MesquiteInteger io = new MesquiteInteger(0);
			deselectWandTouched.toggleValue(ParseUtil.getFirstToken(arguments, io));
		}
		else if (checker.compare(this.getClass(), "Applies the magic wand tool to select like values", "[column touched] [row touched]", commandName, "wandTouch")) {
			doWandTouch(arguments);
		}
		else if (checker.compare(this.getClass(), "Sorts rows", "[column touched] [row touched]", commandName, "sortTouch")) {
			if (!(getCurrentObject() instanceof Associable))
				return null;
			Associable assoc = (Associable)getCurrentObject();
			MesquiteInteger io = new MesquiteInteger(0);
			boolean gT = true;
			if (arguments.indexOf("option")>=0)
				gT = false;
			int column= MesquiteInteger.fromString(arguments, io);
			int row= MesquiteInteger.fromString(arguments, io);
			if (!MesquiteInteger.isCombinable(column) && !MesquiteInteger.isCombinable(row))
				return null;
			UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_MOVED,assoc);
			undoInstructions.recordPreviousOrder(assoc);
			UndoReference undoReference = new UndoReference(undoInstructions, ownerModule);
			if (column>=0 && row >=0) {
				long[] fullChecksumBefore=null;
				if (assoc instanceof CharacterData) {
					fullChecksumBefore = ((CharacterData)assoc).getIDOrderedFullChecksum();
					if (((CharacterData)assoc).isMolecularSequence())
						if (!AlertDialog.query(this, "Sort Characters", "These are molecular sequences. Are you sure you want to reorder the sites?  It cannot be undone.", "Sort", "Cancel", 2))
							return null;
				}
				
				String[] text = new String[assoc.getNumberOfParts()];
				for (int i=0; i<assoc.getNumberOfParts(); i++) {
					text[i] = table.getMatrixText(column, i);
				}

				for (int i=1; i<assoc.getNumberOfParts(); i++) {
					for (int j= i-1; j>=0 && compare(gT, text[j], text[j+1]); j--) {
						swapParts(assoc, j, j+1, text);
					}
				}
				
				processPostSwap(assoc);
				assoc.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));
				if (assoc instanceof CharacterData){
					long[] fullChecksumAfter = ((CharacterData)assoc).getIDOrderedFullChecksum();
					((CharacterData)assoc).compareChecksums(fullChecksumBefore, fullChecksumAfter, true, "sorting of characters");
				}
			}
			else if (column == -1 && row >=0) { //row names selected; sort by name
				long[] fullChecksumBefore=null;
				if (assoc instanceof CharacterData) {
					fullChecksumBefore = ((CharacterData)assoc).getIDOrderedFullChecksum();
					if (((CharacterData)assoc).isMolecularSequence())
						if (!AlertDialog.query(this, "Sort Characters", "Are you sure you want to reorder the sequences?  It cannot be undone.", "Sort", "Cancel", 2))
							return null;
				}
				String[] text = new String[assoc.getNumberOfParts()];
				for (int i=0; i<assoc.getNumberOfParts(); i++) {
					text[i] = getRowNameForSorting(i);
				}
				for (int i=1; i<assoc.getNumberOfParts(); i++) {
					for (int j= i-1; j>=0 && compare(gT, text[j], text[j+1]); j--) {
						swapParts(assoc, j, j+1, text);
					}
				}
				assoc.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));
				if (assoc instanceof CharacterData){
					long[] fullChecksumAfter = ((CharacterData)assoc).getIDOrderedFullChecksum();
					((CharacterData)assoc).compareChecksums(fullChecksumBefore, fullChecksumAfter, true, "sorting of characters");
				}
			}
		}
		else if (checker.compare(this.getClass(), "Deletes the selected rows", null, commandName, "deleteSelectedRows")) {
			deleteSelectedRows(true);
		}
		else if (checker.compare(this.getClass(), "Moves the selected rows ", "[row to move after; -1 if at start]", commandName, "moveSelectedTo")) {
			if (!owner.rowsMovable())
				return null;
			if (!table.anyRowSelected()){
				owner.discreetAlert( "Sorry, to move " + owner.getItemTypeNamePlural() + " they must be selected first in the List window");
				return null;
			}
			MesquiteInteger io = new MesquiteInteger(0);
			int justAfter= MesquiteInteger.fromString(arguments, io);
			if (!MesquiteInteger.isCombinable(justAfter))
				justAfter = MesquiteInteger.queryInteger(this, "Move selected", "After which row should the selected rows be moved (enter 0 to move to first place)?", 0,  0, table.getNumRows()*10);
			if (MesquiteInteger.isCombinable(justAfter))
				table.selectedRowsDropped(justAfter-1); //-1 to convert to internal representation
		}
		else if (checker.compare(this.getClass(), "Adds rows (and their corresponding objects)", "[number of rows to add]", commandName, "addRows")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int num= MesquiteInteger.fromString(arguments, io);
			int first = table.getNumRows();
			if (!MesquiteInteger.isCombinable(num))
				num = MesquiteInteger.queryInteger(this, "Add", "Add how many?", 1);
			if (MesquiteInteger.isCombinable(num)){
				addRows(num);
				//					addRowsNotify(first, num);
			}
		}
		else if (checker.compare(this.getClass(), "Selects a row", "[number of row]", commandName, "selectRow")) { //TODO: should this use internal or external character numbers???
			selectRow(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
		}
		else if (checker.compare(this.getClass(), "Selects a column", "[number of column]", commandName, "selectColumn")) {
			selectColumn(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
		}
		else if (checker.compare(this.getClass(), "Hires a list assistant module", "[name of module]", commandName, "newAssistant")) {
			ListAssistant assistant= (ListAssistant)ownerModule.hireNamedEmployee(owner.getAssistantClass(), arguments);
			if (assistant!=null) {
				assistant.setUseMenubar(false);
				addListAssistant(assistant);
				repaintAll();
				ownerModule.resetContainingMenuBar();
			}
			else {
				for (int i = 0; i<ownerModule.getNumberOfEmployees(); i++) { //if already hired then pass along commands
					MesquiteModule e=(MesquiteModule)ownerModule.getEmployeeVector().elementAt(i);
					if (e instanceof ListAssistant && owner.getAssistantClass() != null && owner.getAssistantClass().isAssignableFrom(e.getClass())) {
						if (e.nameMatches(ParseUtil.getToken(arguments, new MesquiteInteger(0))) && !e.canHireMoreThanOnce())
							return assistant;
					}
				}
			}
			return assistant;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	boolean compare(boolean greaterThan, String one, String two){
		if (one == null || two == null)
			return false;
		double dOne = MesquiteDouble.fromString(one);
		double dTwo = MesquiteDouble.fromString(two);
		if (MesquiteDouble.isCombinable(dOne) && MesquiteDouble.isCombinable(dTwo)) {
			if (greaterThan && (dTwo<dOne))
				return true;
			if (!greaterThan && (dTwo>dOne))
				return true;
			return false;
		}
		int order = collator.compare(one, two);
		if ((order == 1 && greaterThan) ||(order == -1 && !greaterThan))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	/** Swaps parts first and second*/
	void swapParts(Associable assoc, int first, int second, String[] text){  
		String temp = text[first];
		text[first] = text[second];
		text[second] = temp;
		assoc.swapParts(first, second); 
	}
	public void processPostSwap(Associable assoc){
	}
	boolean satisfiesCriteria(String one, String two){
		if (StringUtil.blank(one) && StringUtil.blank(two))
			return true;
		if (StringUtil.blank(one) || StringUtil.blank(two))
			return false;
		if (equals.getValue() && one.equals(two))
			return true;
		double dOne = MesquiteDouble.fromString(one);
		double dTwo = MesquiteDouble.fromString(two);
		if (MesquiteDouble.isCombinable(dOne) && MesquiteDouble.isCombinable(dTwo)) {
			if (greaterThan.getValue() && (dTwo>dOne))
				return true;
			if (lessThan.getValue() && (dTwo<dOne))
				return true;
			return false;
		}
		int order = collator.compare(one, two);
		if (greaterThan.getValue() && (order == -1))
			return true;
		if (lessThan.getValue() && (order == 1))
			return true;
		if (equals.getValue() && (order == 0))
			return true;
		return false;
	}
	public void addListAssistant(ListAssistant assistant) {
		if (assistant !=null) {
			assistant.setTableAndObject(table, getCurrentObject());
			if (assistant.isDoomed() || ownerModule == null || ownerModule.getEmployeeVector().indexOf(assistant)<0)
				return;
			table.setNumColumns(table.getNumColumns()+1);
			if (!table.columnAdjusted(table.getNumColumns()-1)) {
				int w = assistant.getColumnWidth();
				if (w==0) {
					FontMetrics fm = table.getFontMetrics(table.getFont());
					if (fm!=null)
						w = fm.stringWidth(assistant.getWidestString());
					else
						w = 100;  // should something else be used as default?
					w *= 1.5;
				}
				table.setColumnWidth(table.getNumColumns()-1, w);
			}
		}
	}
	public void reviewColumnNumber() {
		ListAssistant assistant=null;
		int column = 0;
		for (int i=0; i< ownerModule.getNumberOfEmployees(); i++) {
			Object obj =  ownerModule.getEmployeeVector().elementAt(i);
			if (obj instanceof ListAssistant) {
				column++;
			}
		}

		table.setNumColumns(column);
		reviewColumnWidths();
		table.repaintAll();


	}
	public void reviewColumnWidths() {
		ListAssistant assistant=null;
		int column = 0;
		for (int i=0; i< ownerModule.getNumberOfEmployees(); i++) {
			Object obj =  ownerModule.getEmployeeVector().elementAt(i);
			if (obj instanceof ListAssistant) {
				if (!table.columnAdjusted(column)) {
					assistant = (ListAssistant)obj;
					int w = assistant.getColumnWidth();
					if (w==0) {
						FontMetrics fm = table.getFontMetrics(table.getFont());
						w = fm.stringWidth(assistant.getWidestString()+table.getNameStartOffset()*2);
					}
					if (w!=table.getColumnWidth(column))
						table.setColumnWidth(column, w);
				}
				column++;
			}
		}	
	}
	public ListAssistant findAssistant(int index) {
		ListAssistant assistant=null;
		int num=0;
		if (ownerModule == null || ownerModule.getEmployeeVector() == null)
			return null;
		for (int i=0; i< ownerModule.getNumberOfEmployees(); i++) {
			Object obj =  ownerModule.getEmployeeVector().elementAt(i);
			if (obj instanceof ListAssistant) {
				if (num== index)
					return (ListAssistant)obj;
				num++;
			}
		}	
		return null;
	}

	public int findAssistant(ListAssistant mb) {
		int num=0;
		for (int i=0; i< ownerModule.getNumberOfEmployees(); i++) {
			Object obj =  ownerModule.getEmployeeVector().elementAt(i);
			if (obj instanceof ListAssistant) {
				if (mb == obj)
					return num;
				num++;
			}
		}	
		return -1;
	}
	public void blankColumn(MesquiteModule mb){
		if (mb instanceof ListAssistant) {
			int which = findAssistant((ListAssistant)mb);
			if (which>=0)
				table.blankColumn(which);
		}
	}
	public boolean rowHighlighted(int row) {
		return false;
	}
	public void showSelectionPopup(Container cont, int x, int y) {
	}
	public void selectRow(int row) {
		if (getCurrentObject() instanceof Associable){
			((Associable)getCurrentObject()).setSelected(row, true);
			((Associable)getCurrentObject()).notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
		else {
			table.selectRow(row);
		}
	}

	public boolean isRowSelected(int row){
		return table.isRowSelected(row) || table.isRowNameSelected(row);  //note: new as of 2. 7 enough for row name to be selected!
	}

	public void selectColumn(int column) {
		table.selectColumn(column);
	}
	/*.................................................................................................................*/
	public void setCurrentTool(MesquiteTool tool){
		super.setCurrentTool(tool);
		if (tool.getEnabled())
			if ((getCurrentTool()!=ibeamTool) && (table!=null))
				table.offAllEdits();
	}

	public void focusInRow(int row){
	}
	/*.................................................................................................................*/
	public boolean interceptCellTouch(int  column, int row, int modifiers){
		return false;
	}
	public boolean interceptRowNameTouch(int row, int regionInCellH, int regionInCellV, int modifiers){
		return false;
	}
	public void deleteSelectedColumns() {
		int numSelected = 0;
		for (int ic = table.getNumColumns()-1; ic>=0; ic--){
			if (table.isColumnSelected(ic)) { 
				numSelected++;
			}
		}
		if (numSelected>0) {

			int[] which = new int[numSelected];
			int whichIs = 0;
			for (int ic = table.getNumColumns()-1; ic>=0; ic--){
				if (table.isColumnSelected(ic)) { //columns selected get deleted!
					which[whichIs]=ic;
					whichIs++;
				}
			}
			for (int ic = 0; ic<numSelected; ic++){
				table.deleteColumn(which[ic]);
				ListAssistant assistant = findAssistant(which[ic]);
				ownerModule.fireEmployee(assistant);
			}
			table.repaintAll();
		}
		else
			ownerModule.alert("Columns must be selected before \"Hide\" command is given");
	}

	void moveSelectedColumns() {
		int numSelected = 0;
		for (int ic = table.getNumColumns()-1; ic>=0; ic--){
			if (table.isColumnSelected(ic)) { 
				numSelected++;
			}
		}
		if (numSelected>0) {
			String summary = "[Current columns: ";
			for (int ic = 0; ic<table.getNumColumns(); ic++){
				ListAssistant mb =findAssistant(ic);
				mb.attachments.addElement(new MesquiteInteger("cwi", table.getColumnWidth(ic)), false);
				mb.attachments.addElement(new MesquiteBoolean("cwb", table.columnAdjusted(ic)), false);
				summary += " (" + (ic+1) + ") " + mb.getTitle() + ";";
			}
			summary += "]";

			int whereToMove = MesquiteInteger.queryInteger(this, "Move column(s) where?", "Indicate the column after which the selected columns are to be moved.  If you want to move them to be the first columns, enter 0. " + summary, 0);  //1-based column number after which selected should be moved  
			
			
			if (!MesquiteInteger.isCombinable(whereToMove))
				return;
			
			selectedColumnsDropped(whereToMove, false);
		}
		else
			ownerModule.alert("Columns must be selected before \"Move\" command is given");
	}
	void selectedColumnsDropped(int whereToMove, boolean mouseDrop){
		if (!owner.columnsMovable())  
			return;
		if (mouseDrop){
			for (int ic = 0; ic<table.getNumColumns(); ic++){
				ListAssistant mb =findAssistant(ic);
				mb.attachments.addElement(new MesquiteInteger("cwi", table.getColumnWidth(ic)), false);
				mb.attachments.addElement(new MesquiteBoolean("cwb", table.columnAdjusted(ic)), false);
			}
		}
		int numSelected = 0;
		for (int ic = table.getNumColumns()-1; ic>=0; ic--){
			if (table.isColumnSelected(ic)) { 
				numSelected++;
			}
		}
		ListAssistant boundaryEmployee = null;  //this is the assistant after which the selected columns should be moved; if null then move to start
		if (whereToMove>0) {
			if (whereToMove >table.getNumColumns())
				whereToMove = table.getNumColumns();

			boundaryEmployee = findAssistant(whereToMove-1);
		}


		ListAssistant[] which = new ListAssistant[numSelected];
		int count = 0;
		for (int ic = 0; ic<table.getNumColumns(); ic++){
			if (table.isColumnSelected(ic)) { //columns selected get deleted!
				which[count++]=findAssistant(ic);
			}
		}
		for (int ic = 0; ic<numSelected; ic++){
			ListAssistant assistant = which[ic];
			moveEmployeeAfter(assistant, boundaryEmployee);
			boundaryEmployee = assistant;
		}
		for (int ic = 0; ic<table.getNumColumns(); ic++){
			ListAssistant mb =findAssistant(ic);
			MesquiteInteger cwi = (MesquiteInteger)mb.attachments.getElement("cwi");
			MesquiteBoolean cwb = (MesquiteBoolean)mb.attachments.getElement("cwb");
			if (cwi != null && cwb != null)
				table.setColumnWidth(ic, cwi.getValue(), cwb.getValue());
			if (cwi != null)
				mb.attachments.removeElement(cwi, false);
			if (cwb != null)
				mb.attachments.removeElement(cwb, false);
		}
		table.deselectAllColumns(true);
		for (int ic = 0; ic<numSelected; ic++){
			int column = findAssistant(which[ic]);
			table.selectColumns(column, column);
		}
		table.repaintAll();
	}
	void moveEmployeeAfter(ListAssistant toBeMoved, ListAssistant after){
		//complex because want to keep column widths as is.  Thus, figure out 
		ownerModule.moveEmployeeAfter(toBeMoved, after);
	}
	public void addRowsNotify(int first, int num) {
	}

	public void addRows(int num) {
		MenuOwner.incrementMenuResetSuppression();
		int count =0;
		int currentNumRows = owner.getNumberOfRows();
		UndoReference undoReference = null;
		Associable assoc = (Associable)getCurrentObject();
		if (assoc!=null) {
			UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_ADDED, assoc);
			assoc.resetJustAdded();
			undoReference = new UndoReference(undoInstructions, null);
		}

		for (int ic = 0; ic<num; ic++){
			if (owner.addRow())
				count++;
		}
		table.setNumRows(currentNumRows+count);
		Object obj = getCurrentObject();
		if (obj instanceof Listenable)
			((Listenable)obj).notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, undoReference));
		table.repaintAll();

		MenuOwner.decrementMenuResetSuppression();

	}
	public void deleteSelectedRows(boolean byCommand) {
		int numSelected = 0;
		for (int ic = table.getNumRows()-1; ic>=0; ic--){
			if (table.isRowSelected(ic)) { 
				numSelected++;
			}
		}
		if (!byCommand && (table.editingAnything() || numSelected == 0))
			return;
		if (numSelected>0) {
			String message;
			if  (numSelected > 1)
				message = "Are you sure you want to permanently delete " +  numSelected + " " + owner.getItemTypeNamePlural() + "?";
			else
				message = "Are you sure you want to permanently delete the selected " + owner.getItemTypeName() + "?";
			if (!AlertDialog.query(this, "Delete?",message, "Yes", "No"))
				return;
			MenuOwner.incrementMenuResetSuppression();
			if (ownerModule != null && ownerModule.getProject() != null)
				ownerModule.getProject().incrementProjectWindowSuppression();
			/*int[] which = new int[numSelected];
			int howMany = 0;
			for (int ic = table.getNumRows()-1; ic>=0; ic--){
				if (table.isRowSelected(ic)) {
		 			if (!owner.rowDeletable(ic)){
			 			if (!MesquiteThread.isScripting())
			 				owner.alert("Sorry, you can't delete that");
			 		}
			 		else {
						which[howMany]=ic;
						howMany++;
					}
				}
			}
			 */

			int count =0;
			int currentNumRows = owner.getNumberOfRows();
			Object obj = getCurrentObject();

			//NOTE: this code allows reporting of what contiguous blocks were deleted, but causes full recalculations for each discontiguity

			int row = currentNumRows-1;
			int firstInBlockDeleted = -1;
			int lastInBlockDeleted = -1;
			while(row>=0) {
				if (table.isRowSelected(row) && owner.rowDeletable(row)){  // we've found a selected one
					lastInBlockDeleted = row;
					while(row>=0) {  // now let's look for the first non-selected one
						if (table.isRowSelected(row) && owner.rowDeletable(row))
							firstInBlockDeleted = row;
						else break;
						row--;
					}
					owner.aboutToDeleteRows(firstInBlockDeleted, lastInBlockDeleted, false);  // now prepare contiguous block for deletion
				}
				row--;
			}
			row = currentNumRows-1;
			firstInBlockDeleted = -1;
			lastInBlockDeleted = -1;
			while(row>=0) {
				if (table.isRowSelected(row) && owner.rowDeletable(row)){  // we've found a selected one
					lastInBlockDeleted = row;
					while(row>=0) {  // now let's look for the first non-selected one
						if (table.isRowSelected(row) && owner.rowDeletable(row))
							firstInBlockDeleted = row;
						else break;
						row--;
					}
					owner.deleteRows(firstInBlockDeleted, lastInBlockDeleted, false);  // now delete contiguous block
					count += lastInBlockDeleted-firstInBlockDeleted+1;
				}
				row--;
			}


			/*				int firstInBlockDeleted = -1;
				int lastInBlockDeleted = -1;
				for (int ic = 0; ic<howMany; ic++){
					if (lastInBlockDeleted < 0) {
						lastInBlockDeleted = which[ic];
						firstInBlockDeleted = lastInBlockDeleted;
					}
					else if (which[ic] == firstInBlockDeleted-1){ //still contiguous
						firstInBlockDeleted = which[ic];					
					}
					else {
						owner.deleteRows(firstInBlockDeleted, lastInBlockDeleted, false);
						count += lastInBlockDeleted-firstInBlockDeleted+1;
						lastInBlockDeleted = which[ic];
						firstInBlockDeleted = lastInBlockDeleted;
					}
				}
				if (lastInBlockDeleted>=0){
					owner.deleteRows(firstInBlockDeleted, lastInBlockDeleted, false);
					count += lastInBlockDeleted-firstInBlockDeleted+1;
				}
			 */

			/*  old code pre-2. 02
				int firstInBlockDeleted = -1;
				for (int ic = 0; ic<howMany; ic++){
					//NOTE: this code allows reporting of what contiguous blocks were deleted, but caused  full recalculations for each discontiguity
					if (notifyInBlocks && obj instanceof Associable && firstInBlockDeleted < 0) {
						firstInBlockDeleted = which[ic];
					}
			 		if (owner.deleteRow(which[ic], false))
						count++;
					//NOTE: this code allows reporting of what contiguous blocks were deleted, but caused  full recalculations for each discontiguity
					if (notifyInBlocks && obj instanceof Associable && (ic+1 >= howMany || which[ic+1] != which[ic]-1)){ //next one is not adjacent; notify listeners
						((Listenable)obj).notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED, new int[] {which[ic], firstInBlockDeleted - which[ic]+1}));
						((Listenable)obj).decrementNotifySuppress();
						((Listenable)obj).incrementNotifySuppress();
						firstInBlockDeleted = -1;
					}
				}
			 */

			table.setNumRows(currentNumRows-count);
			//NOTE: this code allows reporting of what contiguous blocks were deleted, but causes full recalculations for each discontiguity
			notifyRowDeletion(obj);
			table.repaintAll();
			if (count>=0)
				ownerModule.resetAllMenuBars();

			if (ownerModule != null && ownerModule.getProject() != null)
				ownerModule.getProject().decrementProjectWindowSuppression();
			MenuOwner.decrementMenuResetSuppression();
		}
		else
			ownerModule.alert("Rows must be selected before \"delete\" command is given");
	}
	public void notifyRowDeletion(Object obj){
		if (obj instanceof Listenable) {
			((Listenable)obj).notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
		}
	}
	public void setObject(Object object) {
		setCurrentObject(object);
		if (getCurrentObject() instanceof Associable)
			table.setRowAssociable((Associable)getCurrentObject());
		if (object instanceof Annotatable)
			setDefaultAnnotatable((Annotatable)object);
		ListAssistant assistant;
		Enumeration enumeration=ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof ListAssistant) {
				assistant =  (ListAssistant)obj;
				assistant.setTableAndObject(table, getCurrentObject());
			}
		}	
		table.setNumRows(owner.getNumberOfRows());
		table.repaintAll();
	}

	/*.................................................................................................................*/
	public void windowResized() {
		super.windowResized();
		if (MesquiteWindow.checkDoomed(this))
			return;
		//if (true || (getHeight()!=windowHeight) || (getWidth()!=windowWidth)) {
			windowHeight =getHeight();
			windowWidth = getWidth();
			if (table !=null)
				table.setSize(windowWidth, windowHeight);
	//	}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	public String getTextContents() {
		String text = owner.getTextContentsPreface();
		text += "\n\n" + super.getTextContents();
		return text;
	}
	/*.................................................................................................................*/
	public MesquiteTable getTable() {
		return table; 
	}
}




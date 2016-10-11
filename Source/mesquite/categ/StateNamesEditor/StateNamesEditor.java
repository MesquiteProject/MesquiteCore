/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.categ.StateNamesEditor;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;


/* ======================================================================== */
public class StateNamesEditor extends CategDataEditorInit {
	CategoricalData data;
	StateNamesWindow window;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		getEmployer().addMenuItem("Edit State Names", makeCommand("makeWindow", this));
		return true;
	}
	/** Called to alter data in all cells*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (!(data instanceof CategoricalData))
			return;
		//TODO: query here if state names editing allowed
		this.data = (CategoricalData)data;

	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public void endJob(){
		if (window != null){
			window.hide();
			window.dispose();
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (getModuleWindow()==null || !getModuleWindow().isVisible())
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("makeWindow"); 
		temp.addLine("tell It"); 
		Snapshot sn = getModuleWindow().getSnapshot(file);
		temp.incorporate(sn, true);
		temp.addLine("endTell"); 
		temp.addLine("showWindow"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Makes the state names editor window (but doesn't display it)", null, commandName, "makeWindow")) {
			if (data!=null ){
				if (getModuleWindow()==null) {
					setModuleWindow(window = new StateNamesWindow(this, data));
					resetContainingMenuBar();
					resetAllWindowsMenus();
				}
				if (!MesquiteThread.isScripting())
					getModuleWindow().setVisible(true);
				return getModuleWindow();
				//set up a listener
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void panelGoAway(Panel p){
		StateNamesWindow window = (StateNamesWindow)getModuleWindow();
		if (window == null)
			return;
		if (p == window.summaryPanel){
			window.showSummaryPanel.setValue(false);
			window.setSummaryPanel();
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Edit State Names";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Edits state names of a categorical data matrix." ;
	}

}

/* ======================================================================== */
class StateNamesWindow extends TableWindow implements MesquiteListener, AnnotPanelOwner {
	protected StateNamesTable table;
	private int windowWidth=360;
	private int windowHeight=400;
	StateNamesEditor owner;
	ToolPalette palette = null;
	TableTool arrowTool, ibeamTool, fillTool, dropperTool;
	CategoricalData data;
	String fillString;
	boolean neverFilled = true;
	MesquiteBoolean rowsAreCharacters;
	MesquiteMenuItemSpec constrainMenuItem, transposeMenuItem;
	MesquiteMenuSpec menu;
	MesquiteBoolean constrainedCW= new MesquiteBoolean(true);
	int constrainedColumnNum = 3;
	MesquiteBoolean showPanel = new MesquiteBoolean(false);
	AnnotationsPanel panel = null;
	MesquiteBoolean showSummaryPanel = new MesquiteBoolean(false);
	HTMLSidePanel summaryPanel = null;
	int currentCharacter, currentState, currentNoteNumber;
	MesquiteSubmenuSpec annotMenu;

	public StateNamesWindow (StateNamesEditor ownerModule, CategoricalData data) {
		super(ownerModule, true); //INFOBAR
		setWindowSize(windowWidth, windowHeight);
		ownerModule.setModuleWindow(this);
		owner = ownerModule;
		this.data = data;
		rowsAreCharacters = new MesquiteBoolean(true);
		table = new StateNamesTable (data, windowWidth, windowHeight, 130, this, rowsAreCharacters.getValue());
		setShowAnnotation(true);
		data.addListener(this);
		//ownerModule.addCheckMenuItem(null, "Characters On Rows", MesquiteModule.makeCommand("rowsAreCharacters",  this), rowsAreCharacters);
		//ownerModule.resetContainingMenuBar();
		String selectExplanation = "This tool selects items in the matrix.  By holding down shift while clicking, the selection will be extended from the first to the last touched cell. ";
		selectExplanation += " A block of cells can be selected either by using shift-click to extend a previous selection, or by clicking on a cell and dragging with the mouse button still down";
		selectExplanation += " Discontinous selections are allowed, and can be obtained by a \"meta\"-click (right mouse button click, or command-click on a MacOS system). ";
		arrowTool = new TableTool(this, "arrow", MesquiteModule.getRootImageDirectoryPath(), "arrow.gif", 4,2,"Select", selectExplanation, MesquiteModule.makeCommand("arrowTouchCell",  this) , MesquiteModule.makeCommand("arrowDragCell",  this), MesquiteModule.makeCommand("arrowDropCell",  this));
		arrowTool.setIsArrowTool(true);
		arrowTool.setUseTableTouchRules(true);
		addTool(arrowTool);
		getPalette().setFirstToolHeight(26);
		setCurrentTool(arrowTool);
		arrowTool.setInUse(true);
		ibeamTool = new TableTool(this, "ibeam", MesquiteModule.getRootImageDirectoryPath(), "ibeam.gif", 7,7,"Edit", "This tool can be used to edit the contents of cells in the matrix.", MesquiteModule.makeCommand("editCell",  (Commandable)table) , null, null);
		ibeamTool.setWorksOnRowNames(table.rowsAreCharacters); 
		addTool(ibeamTool);

		fillTool = new TableTool(this, "fill", MesquiteModule.getRootImageDirectoryPath(), "bucket.gif", 13,14,"Fill with unassigned", "This tool fills selected cells with text.  The text to be used can be determined by using the dropper tool.", MesquiteModule.makeCommand("fillTouchCell",  this) , null, null);
		fillTool.setOptionsCommand(MesquiteModule.makeCommand("fillOptions",  this));
		addTool(fillTool);
		dropperTool = new TableTool(this, "dropper", MesquiteModule.getRootImageDirectoryPath(), "dropper.gif", 1,14,"Copy states", "This tool fills the paint bucket with the text in the cell touched on", MesquiteModule.makeCommand("dropperTouchCell",  this) , null, null);
		addTool(dropperTool);
		menu = ownerModule.makeMenu("State_Names");
		constrainMenuItem = ownerModule.addCheckMenuItem(null, "Constrain Column Widths", ownerModule.makeCommand("toggleConstrainChar", this), constrainedCW);
		ownerModule.addMenuItem(null, "Min. # Columns if Constrained...", ownerModule.makeCommand("toggleConstrainCharNum", this));
		transposeMenuItem = ownerModule.addCheckMenuItem(null, "Rows Are Characters", ownerModule.makeCommand("rowsAreCharacters", this), rowsAreCharacters);
		ownerModule.addMenuSeparator();
		MesquiteMenuItemSpec mm = ownerModule.addMenuItem("Find String...", MesquiteModule.makeCommand("findString", this));
		mm.setShortcut(KeyEvent.VK_3);
		mm = ownerModule.addMenuItem("Find Again", MesquiteModule.makeCommand("findAgain", this));
		mm.setShortcut(KeyEvent.VK_8);
		ownerModule.addCheckMenuItem(null, "Show State Annotations", MesquiteModule.makeCommand("togglePanel", this), showPanel);
		ownerModule.addCheckMenuItem(null, "Show Annotations Summary", MesquiteModule.makeCommand("toggleSummaryPanel", this), showSummaryPanel);

		MesquiteSubmenuSpec annotMenu = ownerModule.addSubmenu(null, "Annotations");
		ownerModule.addItemToSubmenu(null, annotMenu, "-", null);
		MesquiteMenuItemSpec mmi = ownerModule.addItemToSubmenu(null, annotMenu, "Make Item Label", MesquiteModule.makeCommand("makeItemLabel", panel));
		mmi.setShortcut(KeyEvent.VK_L);
		ownerModule.addItemToSubmenu(null, annotMenu, "Recover Offscreen Labels", MesquiteModule.makeCommand("recoverLostLabels", panel));
		table.setColumnWidthsUniform(100);
		table.setConstrainMaxAutoColumn(constrainedCW.getValue());
		table.setConstrainMaxAutoRownames(constrainedCW.getValue());
		//table.setUserAdjust(MesquiteTable.NOADJUST, MesquiteTable.RESIZE);
		addToWindow(table);
		MesquiteButton annotButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("togglePanel",  this), null, true, MesquiteModule.getRootImageDirectoryPath() + "annot.gif", 12, 16);
		annotButton.setShowBackground(false);
		annotButton.setButtonExplanation("Show/Hide Annotations Panel");
		table.addControlButton(annotButton);
		table.setVisible(true);
		table.setLocation(0,0);
		MesquiteWindow.addKeyListener(this, table);
		setWindowSize(windowWidth, windowHeight);
		resetTitle();
	}
	public MesquiteModule getModule(){
		return ownerModule;
	}
	/*.................................................................................................................*/
	void setPanel(){
		if (showPanel.getValue()){
			if (panel == null) {
				panel = new AnnotationsPanel(this);
			}
			addSidePanel(panel, 300);
			panel.setVisible(true);

		}
		else {
			if (panel != null) {
				panel.setVisible(false);
				removeSidePanel(panel);
			}
		}

	}
	/*.................................................................................................................*/
	void setSummaryPanel(){
		if (showSummaryPanel.getValue()){
			if (summaryPanel == null) {
				summaryPanel = new HTMLSidePanel(ownerModule, "Annotations");
			}
			prevIC = -1;
			prevST = -1;
			addSidePanel(summaryPanel, 300);
			summaryPanel.setVisible(true);

		}
		else {
			prevIC = -1;
			prevST = -1;
			if (summaryPanel != null) {
				summaryPanel.setVisible(false);
				removeSidePanel(summaryPanel);
			}
		}

	}
	public void panelGoAway(Panel p){

		showPanel.setValue(false);
		setPanel();
	}
	public void showSummary(int ic, int st){
		if (showSummaryPanel.getValue() && summaryPanel != null){
			AttachedNotesVector anv = null;
			if (st<0)
				anv = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
			else
				anv = data.getStateAnnotationsVector(ic, st);
			if (anv != null && anv.getNumNotes() > 0){
				String notes = "";
				if (anv != null && anv.getNumNotes()>0){
					for (int i = 0; i< anv.getNumNotes(); i++) {
						AttachedNote note = anv.getAttachedNote(i);
						String c = note.getComment();
						String cont = "";
						if (!StringUtil.blank(note.getComment()))
							cont += note.getComment();
						if (!StringUtil.blank(note.getReference()))
							cont += " (Ref: " + note.getReference() + ")";
						if (!StringUtil.blank(note.getAuthorName()))
							cont += " <strong>(" + note.getAuthorName() + ")</strong>";
						if (!StringUtil.blank(cont))
							notes += "<li>" + cont + "</li>";				
					}
				}
				if (st<0){
					if (!StringUtil.blank(notes))
						summaryPanel.setText("<html><h3>Notes for character " + (ic+1) + "</h3><ul>" + notes + "</ul></html>");
					else
						summaryPanel.setText("<html><h3>No notes for character " + (ic+1) +  "</h3></html>");
				}
				else {
					if (!StringUtil.blank(notes))
						summaryPanel.setText("<html><h3>Notes for character " + (ic+1) + " state " + st + "</h3><ul>" + notes + "</ul></html>");
					else
						summaryPanel.setText("<html><h3>No notes for character " + (ic+1) + " state " + st + "</h3></html>");
				}
			}
			else if (ic<0 || st <-1)
				summaryPanel.setText("<html></html>");
			else if (st<0)
				summaryPanel.setText("<html><h3>No notes for character " + (ic+1)  + "</h3></html>");
			else 
				summaryPanel.setText("<html><h3>No notes for character " + (ic+1) + " state " + st + "</h3></html>");
		}
	}
	NameReference notesNameRef = NameReference.getNameReference("notes");
	/*.................................................................................................................*/
	void showNote(AttachedNotesVector aim, int ic, int st, int noteNumber){
		if (data == null || panel == null)
			return;
		if (ic<=-1 || st < -1 || st> CategoricalState.maxCategoricalState || ic >= data.getNumChars()){
			panel.setNotes(null, "", "", ic, st, noteNumber);
			panel.setAttachable(false);
			return;
		}
		currentCharacter  = ic;
		currentState = st;
		panel.setAttachable(true);
		String s = null;
		if (st<0)
			s = "Character " + (ic+1);
		else
			s = "State " + st + " in character " + (ic+1);
		String loc = Integer.toString(ic+1);
		if (st>=0)
			loc += " (" +st + ")";
		panel.setNotes(aim, s, loc , ic, st, noteNumber);
	}
	void showNote(int ic, int st, int noteNumber){
		if (data == null || panel == null)
			return;
		if (ic<=-1 || st < -1 || st> CategoricalState.maxCategoricalState || ic >= data.getNumChars()){
			showNote(null, ic, st, noteNumber);
			return;
		}
		AttachedNotesVector hL = null;
		if (st < 0)
			hL = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
		else
			hL = data.getStateAnnotationsVector(ic, st);
		showNote(hL, ic, st, noteNumber);
	}


	public AttachedNotesVector makeNotesVector(AnnotationsPanel w){
		AttachedNotesVector aiv = null;
		if (currentState == -1) {
			aiv = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, currentCharacter);
			if (aiv == null){
				aiv = new AttachedNotesVector(data);
				data.setAssociatedObject(notesNameRef, currentCharacter, aiv);
			}
		}
		else {
			aiv = data.getStateAnnotationsVector(currentCharacter, currentState);
			if (aiv == null){
				aiv = new AttachedNotesVector(data);
				data.setStateAnnotationsVector(currentCharacter, currentState, aiv);
			}
		}
		return aiv;
	}
	public void chooseAndAttachImage(AttachedNote hL, boolean local){
		if (hL == null)
			return;

		hL.attachImage(ownerModule, local);
		showNote(currentCharacter, currentState, currentNoteNumber);
		if (table != null)
			table.repaintAll();
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	int count = 0;
	int prevIC = -1;
	int prevST = -1;
	public void focusInCell(int ic, int state){
		if (data == null)
			return;
		if (prevIC == ic && prevST == state)
			return;
		prevIC = ic;
		prevST = state;
		showNote(ic, state, 0);
		showSummary(ic, state);
		return;
	}

	public boolean showFindMenuItems(){
		return false;
	}
	public String getFindMessageName(){
		return "Character or State name";
	}
	/*.................................................................................................................*/
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData && (CharacterData)obj ==data) {
			if (getMode()>0)
				updateTextPage();
			else if (code==MesquiteListener.NAMES_CHANGED) {
				table.repaintAll();
			}
			else if (code==MesquiteListener.PARTS_DELETED) {
				table.resetNumChars();
				table.checkSelection();
				table.repaintAll();
			}
			else if (code==MesquiteListener.PARTS_CHANGED) {
				table.resetNumChars();
				table.repaintAll();
			}
			else if (code==MesquiteListener.PARTS_ADDED) {
				table.resetNumChars();
				table.checkSelection();
				table.repaintAll();
			}
			else if (code==MesquiteListener.PARTS_MOVED) {
				table.checkSelection();
				table.repaintAll();
			}
			else if (code==MesquiteListener.SELECTION_CHANGED) {
				table.checkSelection();
				table.repaintAll();
			}
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
	/** When called the getModuleWindow() will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){

		if (data != null) {
			if (data.uniquelyNamed())
				setTitle("StateNames (" + data.getName() + ")");
			else
				setTitle("StateNames (" + data.getName() + " [" + data.getID() + "])");
		}
		else
			setTitle("State Names");
	}

	/*.................................................................................................................*/
	void setRowsAreCharacters(boolean rac){
		rowsAreCharacters.setValue(rac);
		ibeamTool.setWorksOnRowNames(rac); 
		table.resetRowsAreCharacters(rac);
	}
	/*.................................................................................................................*/
	String fillName;
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("rowsAreCharacters " + rowsAreCharacters.toOffOnString()); 
		temp.addLine("toggleConstrainChar " + constrainedCW.toOffOnString()); 
		temp.addLine("toggleConstrainCharNum " + constrainedColumnNum); 
		temp.addLine("togglePanel " + showPanel.toOffOnString());
		temp.addLine("toggleSummaryPanel " + showSummaryPanel.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hides state names editor window", null, commandName, "hideWindow")) {
			hide();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the annotations panel is shown", "[on = shown; off]", commandName, "togglePanel")) {
			Parser parser = new Parser();
			showPanel.toggleValue(parser.getFirstToken(arguments));
			setPanel();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the annotations summary panel is shown", "[on = shown; off]", commandName, "toggleSummaryPanel")) {
			Parser parser = new Parser();
			showSummaryPanel.toggleValue(parser.getFirstToken(arguments));
			setSummaryPanel();
		}
		else if (checker.compare(this.getClass(), "Selects a row in the editor", "[row number]", commandName, "selectRow")) { //TODO: should this use internal or external character numbers???
			selectRow(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
		}
		else if (checker.compare(this.getClass(), "Selects a column in the editor", "[column number]", commandName, "selectColumn")) {
			selectColumn(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
		}
		else if (checker.compare(this.getClass(), "Puts characters on rows, states on columns", "[on or off]", commandName, "rowsAreCharacters")) {
			rowsAreCharacters.toggleValue(arguments);
			setRowsAreCharacters(rowsAreCharacters.getValue());
		}
		else if (checker.compare(this.getClass(), "Constrains width of character names area", "[on or off]", commandName, "toggleConstrainChar")) {
			constrainedCW.toggleValue(arguments);
			table.setConstrainMaxAutoColumn(constrainedCW.getValue());
			table.setConstrainMaxAutoRownames(constrainedCW.getValue());
			if (!MesquiteThread.isScripting())
				table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Constrains width of character names aresa", "[number of columns]", commandName, "toggleConstrainCharNum")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(this, "Set Min number of columns", "Minimum number of columns", constrainedColumnNum, 1, MesquiteInteger.infinite);
			if (newNum>0  && newNum!=constrainedColumnNum) {
				constrainedColumnNum = newNum;
				table.setConstrainMaxAutoColumnNum(constrainedColumnNum);
				if (!MesquiteThread.isScripting())
					table.repaintAll();
			}
		}
		else if (checker.compare(this.getClass(), "Fills the touched cell with current paint", "[column][row]", commandName, "fillTouchCell")) {
			if (table!=null && data !=null){
				MesquiteInteger io = new MesquiteInteger(0);
				int column= MesquiteInteger.fromString(arguments, io);
				int row= MesquiteInteger.fromString(arguments, io);
				if (!table.rowLegal(row)|| !table.columnLegal(column))
					return null;
				if (table.anyCellSelected()) {
					if (table.isCellSelected(column, row)) {
						for (int i=0; i<table.getNumColumns(); i++)
							for (int j=0; j<table.getNumRows(); j++)
								if (table.isCellSelected(i,j))
									table.setStateName(j,i, fillName);
						table.repaintAll();
					}
				}
				else if (table.anyRowSelected()) {
					if (table.isRowSelected(row)) {
						for (int j=0; j<table.getNumRows(); j++) {
							if (table.isRowSelected(j))
								for (int i=0; i<table.getNumColumns(); i++)
									table.setStateName(j,i, fillName);
						}
						table.repaintAll();
					}
				}
				else if (table.anyColumnSelected()) {
					if (table.isColumnSelected(column)) {
						for (int i=0; i<table.getNumColumns(); i++){
							if (table.isColumnSelected(i))
								for (int j=0; j<table.getNumRows(); j++) 
									table.setStateName(j,i, fillName);
						}
						table.repaintAll();
					}
				}
				else {
					table.setStateName(row, column, fillName);
					table.redrawCell(column, row);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Queries the user to fill the selected cells with text", null, commandName, "fillSelected")) {
			fillName = MesquiteString.queryString(this, "Fill cells", "Fill cells with names:", "");
			if (table.anyCellSelected()) {
				for (int i=0; i<table.getNumColumns(); i++)
					for (int j=0; j<table.getNumRows(); j++)
						if (table.isCellSelected(i,j))
							table.setStateName(j,i, fillName);
				table.repaintAll();
			}
			else if (table.anyRowSelected()) {
				for (int j=0; j<table.getNumRows(); j++) {
					if (table.isRowSelected(j))
						for (int i=0; i<table.getNumColumns(); i++)
							table.setStateName(j,i, fillName);
				}
				table.repaintAll();
			}
			else if (table.anyColumnSelected()) {
				for (int i=0; i<table.getNumColumns(); i++){
					if (table.isColumnSelected(i))
						for (int j=0; j<table.getNumRows(); j++) 
							table.setStateName(j,i, fillName);
				}
				table.repaintAll();
			}
		}
		else if (checker.compare(this.getClass(), "Queries the user what paint to use", null, commandName, "fillOptions")) {
			if (table!=null && data !=null){
				fillName = MesquiteString.queryString(this, "Fill name", "Name with which to fill using paint bucket:", "");
				if (StringUtil.blank(fillName))
					return null;
				fillTool.setDescription("Fill with \"" + fillName+ " \"");
				dropperTool.setDescription("Copy name (current: " + fillName + ")");
				toolTextChanged();
			}


		}
		else if (checker.compare(this.getClass(), "Queries the user for the paint string", null, commandName, "touchTool")) {
			if (table!=null && data !=null){
				String newFillString  = MesquiteString.queryString(this, "Fill string", "String with which to fill using paint bucket:", fillString);
				if (StringUtil.blank(newFillString))
					return null;
				fillString = newFillString;
				neverFilled = false;
				fillTool.setDescription("Fill with \"" + fillString + " \"");
				dropperTool.setDescription("Copy string (current: " + fillString + ")");
				this.toolTextChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Fills the paint bucket with the string of the selected cell", "[column][row]", commandName, "dropperTouchCell")) {
			if (table!=null && data !=null){
				MesquiteInteger io = new MesquiteInteger(0);
				int column= MesquiteInteger.fromString(arguments, io);
				int row= MesquiteInteger.fromString(arguments, io);
				if (!table.rowLegal(row)|| !table.columnLegal(column))
					return null;
				fillName = table.getText(column, row);
				fillTool.setDescription("Fill with \"" + fillName+ " \"");
				dropperTool.setDescription("Copy name (current: " + fillName + ")");
				toolTextChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void selectRow(int row) {
		table.selectRow(row);
	}

	public void selectColumn(int column) {
		table.selectColumn(column);
	}

	/*.................................................................................................................*/
	public void windowResized() {
		super.windowResized();
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (table!=null && ((getHeight()!=windowHeight) || (getWidth()!=windowWidth))) {
			windowHeight =getHeight();
			windowWidth = getWidth();
			table.setSize(windowWidth, windowHeight);
		}
		if (table!=null)
			table.resetComponentSizes();
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	public MesquiteTable getTable() {
		return table; 
	}
	public void dispose(){
		if (this.data != null)
			data.removeListener(this);
		ownerModule.deleteMenuItem(constrainMenuItem);
		ownerModule.deleteMenuItem(transposeMenuItem);
		ownerModule.deleteMenuItem(menu);
		super.dispose();
	}
}

/* ======================================================================== */
class StateNamesTable extends MesquiteTable {
	StateNamesWindow window;
	CategoricalData data;
	StateAnnotation cellAnnotated;
	boolean rowsAreCharacters = false;
	Image transpose;

	public StateNamesTable (CategoricalData data, int totalWidth, int totalHeight, int columnNamesWidth, StateNamesWindow window, boolean rowsAreCharacters) {
		super(data.getNumChars(), CategoricalState.maxCategoricalState+1, totalWidth, totalHeight, columnNamesWidth, window.getColorScheme(),true,false);
		this.rowsAreCharacters = rowsAreCharacters;
		this.data = data;
		if (!rowsAreCharacters){
			setNumRows(CategoricalState.maxCategoricalState+1);
			setNumColumns(data.getNumChars());
		}
		this.window = window;
		transpose  = MesquiteImage.getImage(window.ownerModule.getPath() + "transpose.gif");
		autosizeColumns = true;
		cellAnnotated = new StateAnnotation(data);
		showRowGrabbers=true;
		showColumnGrabbers=true;
		//setAutoEditable(false, false, false, false);
		if (rowsAreCharacters){
			setEditable(true, true, false, false); 
			setShowRCNumbers(true, false);
			setSelectable(true, true, true, true, false, false);
			setColumnNamesCopyPaste(false);
			setRowAssociable(data);
		}
		else {
			setEditable(true, false, true, false); 
			setSelectable(true, true, true, false, true, false);
			setShowRCNumbers(false, true);
			setRowNamesCopyPaste(false);
			setColumnAssociable(data);
		}
	}

	void resetRowsAreCharacters(boolean rac){
		rowsAreCharacters = rac;
		if (rowsAreCharacters){
			setEditable(true, true, false, false); 
			setShowRCNumbers(true, false);
			setSelectable(true, true, true, true, false, false);
			setColumnNamesCopyPaste(false);
			setNumColumns(CategoricalState.maxCategoricalState+1);
			setRowAssociable(data);
			setNumRows(data.getNumChars());
		}
		else {
			setEditable(true, false, true, false); 
			setShowRCNumbers(false, true);
			setSelectable(true, true, true, false, true, false);
			setRowNamesCopyPaste(false);
			setNumRows(CategoricalState.maxCategoricalState+1);
			setColumnAssociable(data);
			setNumColumns(data.getNumChars());
		}
		repaintAll();
	}
	void checkSelection(){
		if (rowsAreCharacters){
			for (int i=0; i< data.getNumChars(); i++){
				if (data.getSelected(i) && !isRowSelected(i))
					selectRow(i);
				else if (!data.getSelected(i) && isRowSelected(i))
					deselectRow(i);
			}
		}
		else{
			for (int i=0; i< data.getNumChars(); i++){
				if (data.getSelected(i) && !isColumnSelected(i))
					selectColumn(i);
				else if (!data.getSelected(i) && isColumnSelected(i))
					deselectColumn(i);
			}

		}

	}
	/*-------------------------------*/
	void resetNumChars(){
		if (rowsAreCharacters)
			setNumRows(data.getNumChars());
		else
			setNumColumns(data.getNumChars());
	}
	/*...............................................................................................................*/
	/** returns a tab delimited text version of the table*/
	public String getTextVersion(){
		int maxStateNamed = -1;
		for (int ic=0; ic<data.getNumChars(); ic++) {
			for (int state=0; state<=CategoricalState.maxCategoricalState; state++)
				if (data.hasStateName(ic, state) && maxStateNamed < state)
					maxStateNamed = state;
		}
		int sbsize = maxStateNamed*data.getNumChars();
		if (sbsize<=0)
			sbsize = 1;
		StringBuffer outputBuffer = new StringBuffer(sbsize);
		for (int state=0; state<=maxStateNamed;state++)
			outputBuffer.append("\t" + getStateHeading(state));
		outputBuffer.append("\n");
		for (int ic=0; ic<data.getNumChars();ic++) {
			outputBuffer.append(data.getCharacterName(ic)); 
			for (int state=0; state<=maxStateNamed;state++)
				outputBuffer.append("\t" + getState(state,ic));
			outputBuffer.append("\n");
		}
		return outputBuffer.toString();

	}

	String getState(int state, int ic){
		if (data.hasStateName(ic, state))
			return data.getStateName(ic, state); //return state name
		else return "";
	}
	String getStateHeading(int state){
		if (state>9 ||  Integer.toString(state).charAt(0) != data.getSymbol(state))
			return "State "  + Integer.toString(state) + " (\'" + data.getSymbol(state) + "\')";
		return "State " + Integer.toString(state);
	}

	public void setStateName(int row, int column, String name){  
		if (rowsAreCharacters)
			data.setStateName( row, column, name);
		else
			data.setStateName( column, row, name);
	}
	public String getMatrixText(int column, int row){  
		if (rowsAreCharacters)
			return getState(column, row);
		else
			return getState(row, column);
	}

	/*...............................................................................................................*/
	public String getMatrixTextForDisplay(int column, int row){
		String s = getMatrixText(column, row);
		if (rowsAreCharacters){
			if (!StringUtil.blank(data.getStateNote(row, column))) 
				s += '*';
		}
		else {
			if (!StringUtil.blank(data.getStateNote(column, row))) 
				s += '*';
		}
		return s;
	}
	public boolean useString(int column, int row){  
		if (column<0 && row<0)
			return false;
		return true;
	}
	public String getCornerText(){
		if (rowsAreCharacters)
			return "Character \\ State";
		else
			return "State \\ Character";

	}

	public EditorPanel getCharNamesPanel (){
		if (rowsAreCharacters)
			return rowNames;
		else
			return columnNames;
	}

	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Selects all cells in the editor", null, commandName, "selectAll")) {
			if (matrix.getEditing()|| getCharNamesPanel().getEditing()){
				TextField edit = null;
				if (matrix.getEditing())
					edit = matrix.getEditField();
				else if (getCharNamesPanel().getEditing())
					edit = getCharNamesPanel().getEditField();
				if (edit!=null) {
					edit.selectAll();
				}
			}
			else {
				boolean selectA = (!anythingSelected());

				if ((selectA || anyCellSelected()))
					selectBlock(0, 0, numColumnsTotal-1, numRowsTotal-1);
				if ((selectA || anyRowSelected())) {
					selectRows(0, numRowsTotal-1);
				}
				if ((selectA || anyColumnSelected())) {
					selectBlock(0, 0, numColumnsTotal-1, numRowsTotal-1);
				}
				if (rowsAreCharacters){
					if ((selectA || anyRowNameSelected()))
						selectRowNames(0,numRowsTotal-1);
				}
				else {
					if ((selectA || anyColumnNameSelected()))
						selectColumnNames(0,numColumnsTotal-1);
				}
				repaintAll();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}	
	/*...............................................................................................................*/
	public void returnedMatrixText(int column, int row, String s){
		int ic, state;
		if (rowsAreCharacters){
			ic = row;
			state = column;
		}
		else {
			ic = column;
			state = row;
		}

		if (StringUtil.blank(s)) {
			data.setStateName(ic, state,null);
			if (data.hasStateName(ic, state))
				repaintAll();
			if (autoSizeColumns(getGraphics())) {
				matrix.repaint();
				columnNames.repaint();
			}

		}
		else if ((s.length()>1 || s.charAt(0)!=data.getSymbol(state)) && (!s.equals(data.getStateName(ic, state)))) {
			data.setStateName(ic, state, s);
			repaintAll();

			if (autoSizeColumns(getGraphics())) {
				matrix.repaint();
				columnNames.repaint();
			}

		}
	}
	
	Color getCharColor(int ic){
		CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
		if (part!=null){
			CharactersGroup mi = (CharactersGroup)part.getProperty(ic);
			if (mi!=null)
				return mi.getColor();
		}
		return null;
	}
	public void drawRowNameCell(Graphics g, int x, int y, int w, int h, int row) {
		if (rowsAreCharacters){
			Color c = getCharColor(row);
			if (c != null){
				Color prev = g.getColor();
				g.setColor(c);
				g.fillRect(x, y, w, h);
				g.setColor(prev);
			}
			
		}
		super.drawRowNameCell(g, x, y, w, h, row);
	}
	public void drawColumnNameCell(Graphics g, int x, int y, int w, int h, int column) {
		if (!rowsAreCharacters){
			Color c = getCharColor(column);
			if (c != null){
				Color prev = g.getColor();
				g.setColor(c);
				g.fillRect(x, y, w, h);
				g.setColor(prev);
			}
			
		}
		super.drawColumnNameCell(g, x, y, w, h, column);
	}
	/*...............................................................................................................*/
	public String getColumnNameText(int column){
		if (!rowsAreCharacters)
			return data.getCharacterName(column);
		else
			return getStateHeading(column);
	}
	/*...............................................................................................................*/
	public String getColumnNameTextForDisplay(int column){
		String s = getColumnNameText(column);
		if (!rowsAreCharacters && data.getAnnotation(column) != null)
			s += "*";
		return s;

	}
	/*...............................................................................................................
	 * 			CharacterPartition part = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);
			if (part!=null){
				CharactersGroup mi = (CharactersGroup)part.getProperty(row);
				if (mi!=null) {
					g.setColor(mi.getColor());
					return;
				}
			}
*/
	public String getRowNameText(int row){
		if (rowsAreCharacters)
			return data.getCharacterName(row);
		else
			return getStateHeading(row);
	}
	/*...............................................................................................................*/
	public String getRowNameTextForDisplay(int row){
		String s = getRowNameText(row);
		if (rowsAreCharacters && data.getAnnotation(row) != null)
			s += "*";
		return s;

	}
	/*...............................................................................................................*/
	public void returnedRowNameText(int row, String s){
		if (rowsAreCharacters)
			returnedCharNameText(row, s);
	}
	public void returnedColumnNameText(int column, String s){
		if (!rowsAreCharacters)
			returnedCharNameText(column, s);

	}

	public void returnedCharNameText(int ic, String s){
		if (data == null)
			return;
	if (StringUtil.blank(s) && data.characterHasName(ic)){
			data.setCharacterName(ic, null);
			repaintAll();

			if (autoSizeColumns(getGraphics())) {
				matrix.repaint();
				columnNames.repaint();
			}

		}
		else if (s == null)
			return;
		else if (!s.equals(data.getCharacterName(ic))) {
			String warning = data.checkNameLegality(ic, s);
			if (warning == null)
				data.setCharacterName(ic, s);
			else
				window.ownerModule.discreetAlert( warning);
			repaintAll();

			if (autoSizeColumns(getGraphics())) {
				matrix.repaint();
				columnNames.repaint();
			}

		}
	}
	public void selectRow(int row) {
		if (rowsAreCharacters)
			super.selectRow(row);
		else
			selectBlock(0, row, getLastColumn(), row);
	}

	public void selectRows(int first, int last) {
		if (!rowsAreCharacters)
			selectBlock(0, first, getLastColumn(), last);
		else
			super.selectRows(first, last);
	}
	public void selectColumn(int column) {
		if (rowsAreCharacters)
			selectBlock(column, 0, column, getLastRow());
		else
			super.selectColumn(column);
	}
	public void selectColumns(int first, int last) {
		if (rowsAreCharacters)
			selectBlock(first, 0, last, getLastRow());
		else
			super.selectColumns(first, last);
	}
	/*.................................................................................................................*/
	public void copyIt(StringBuffer s, boolean literal){//���may change on transpose
		if (s==null)
			return;
		int count = 0;
		boolean firstInLine = true;
		boolean allSelected = true;
		int numRows = numRowsTotal;
		int maxColumn = 0;
		boolean nothingSelected = !anythingSelected();

		for (int j = 0; j<numRows; j++) {
			boolean rowSel = nothingSelected ||  isRowSelected(j);
			firstInLine = true;
			if (rowSel || isRowNameSelected(j)) {
				String t =null;
				if (literal)
					t = getRowNameTextForDisplay(j);
				else 
					t = getRowNameText(j);
				if (!StringUtil.blank(t)) {
					s.append(t);
					firstInLine = false;
				}
			}

			for (int i = 0; i<numColumnsTotal; i++) {
				if (rowSel || isCellSelected(i, j)){
					String t =null;
					if (literal)
						t = getMatrixTextForDisplay(i,j);
					else 
						t = getMatrixText(i,j);


					if (!firstInLine) 
						s.append('\t');
					if (!StringUtil.blank(t)) 
						s.append(t);
					firstInLine = false;

				}

			}
			if (!firstInLine)
				s.append(StringUtil.lineEnding());
		}
	}
	public void clickOutside(){
		if ((!(matrix.getEditing()|| rowNames.getEditing() || columnNames.getEditing()) && !singleTableCellSelected())){
			window.focusInCell(-2, -2);
		}
	}
	public  void drawCornerCell(Graphics g, int x, int y,  int w, int h){
		super.drawCornerCell(g, x, y, w, h);
		g.drawImage(transpose, x, y, cornerCell);
	}
	NameReference notesNameRef = NameReference.getNameReference("notes");
	public boolean isAttachedNoteAvailable(int column, int row){
		if (data == null)
			return false;
		AttachedNotesVector anv = null;
		try {
			int ic = row;
			int st = column;
			if (!rowsAreCharacters){
				ic = column;
				st = row;
			}
			if (st < 0)
				anv = (AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
			else
				anv = data.getStateAnnotationsVector(ic, st);
		}
		catch(Exception e){
		}
		if (anv == null)
			return false;
		return (anv.getNumNotes()>0);
	}

	/*...............................................................................................................*/
	public void mouseInCell(int modifiers, int column,int subColumn, int row, int subRow, MesquiteTool tool){
		if ((!(matrix.getEditing()|| rowNames.getEditing() || columnNames.getEditing()) && !singleTableCellSelected())){
			if (rowsAreCharacters)
				window.focusInCell(row, column);
			else 
				window.focusInCell(column, row);
		}
		super.mouseInCell(modifiers, column, subColumn, row, subRow, tool);
	}
	/*...............................................................................................................*/
	public void cornerTouched(int x, int y, int modifiers) {
		super.cornerTouched(x, y, modifiers);
		if (x<12 && y<12){
			window.setRowsAreCharacters(!rowsAreCharacters);
		}
	}
	/*...............................................................................................................*/
	public void cellTouched(int column, int row, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		if (((TableTool)window.getCurrentTool()== (TableTool)window.arrowTool) && (clickCount>1)) {
			window.setCurrentTool((TableTool)window.ibeamTool);
			window.getPalette().setCurrentTool((TableTool)window.ibeamTool); 
			((TableTool)window.getCurrentTool()).cellTouched(column, row, regionInCellH, regionInCellV, modifiers);
		}
		else if (((TableTool)window.getCurrentTool()).useTableTouchRules())
			super.cellTouched(column, row, regionInCellH, regionInCellV, modifiers, clickCount);
		else
			((TableTool)window.getCurrentTool()).cellTouched(column, row, regionInCellH, regionInCellV, modifiers);
		if (window.getCurrentTool()== window.arrowTool || window.getCurrentTool()== window.ibeamTool){
			//deselectAll();
			setFocusedCell(column, row);
			repaintAll();
		}
		else
			window.setAnnotation("", null);
	}
	/*...............................................................................................................*/
	public void cellDrag(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (window.getCurrentTool()== window.arrowTool) 
			super.cellDrag(column, row, regionInCellH,  regionInCellV, modifiers);
		else
			((TableTool)window.getCurrentTool()).cellDrag(column, row, regionInCellH,  regionInCellV, modifiers);
	}
	/*...............................................................................................................*/
	public void cellDropped(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (window.getCurrentTool()== window.arrowTool) 
			super.cellDropped(column, row, regionInCellH, regionInCellV, modifiers);
		else
			((TableTool)window.getCurrentTool()).cellDropped(column, row, regionInCellH, regionInCellV, modifiers);
	}
	public void setFocusedCell(int column, int row){
		cellAnnotated.setCell(column, row, rowsAreCharacters);
		window.setAnnotation(cellAnnotated);
		if (rowsAreCharacters)
			window.focusInCell(row, column);
		else
			window.focusInCell(column, row);
		super.setFocusedCell(column, row);
	}
	/*...............................................................................................................*/
	public void rowNameTouched(int row, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		if (rowsAreCharacters){
			if ((window.getCurrentTool()==window.arrowTool) && (clickCount>1)) {
				window.setCurrentTool((TableTool)window.ibeamTool);
				window.getPalette().setCurrentTool((TableTool)window.ibeamTool); 
				((TableTool)window.getCurrentTool()).cellTouched(-1, row, regionInCellH, regionInCellV, clickCount);
			}
			else if (window.getCurrentTool()== window.arrowTool) 
				super.rowNameTouched( row, regionInCellH, regionInCellV, modifiers, clickCount);
			else
				((TableTool)window.getCurrentTool()).cellTouched(-1, row, regionInCellH, regionInCellV, modifiers); 
			if (window.getCurrentTool()== window.arrowTool || window.getCurrentTool()== window.ibeamTool){
				//deselectAll();
				setFocusedCell(-1, row);
				repaintAll();
			}
			else
				window.setAnnotation("", null);
		}
	}
	/*...............................................................................................................*/
	public void columnNameTouched(int column, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		if (!rowsAreCharacters){
			if ((window.getCurrentTool()==window.arrowTool) && (clickCount>1)) {
				window.setCurrentTool((TableTool)window.ibeamTool);
				window.getPalette().setCurrentTool((TableTool)window.ibeamTool); 
				((TableTool)window.getCurrentTool()).cellTouched(column, -1, regionInCellH, regionInCellV, clickCount);
			}
			else if (window.getCurrentTool()== window.arrowTool) {
				super.columnNameTouched( column, regionInCellH, regionInCellV, modifiers, clickCount);
			}
			else
				((TableTool)window.getCurrentTool()).cellTouched(column, -1, regionInCellH, regionInCellV, modifiers); 
			if (window.getCurrentTool()== window.arrowTool || window.getCurrentTool()== window.ibeamTool){
				//deselectAll();
				setFocusedCell(column, -1);
				repaintAll();
			}
			else
				window.setAnnotation("", null);
		}
	}
}

/* ======================================================================== */
class StateAnnotation implements Annotatable {
	String name;
	CategoricalData data;
	int state, character;
	public StateAnnotation(CategoricalData data) {
		this.data = data;
	}

	void setCell(int c, int r, boolean rowsAreCharacters){
		if (rowsAreCharacters){
			character = r;
			state = c;
		}
		else {
			character = c;
			state = r;
		}
	}
	public String getName(){
		if (data == null)
			return "";
		if (character == -1) {
			if (state == -1)
				return "matrix \"" + data.getName() + "\"";
			return null;
		}
		else if (state == -1) {
			return "character \"" +data.getCharacterName(character) + "\"";
		}
		else {
			return "state " + (state) + " in " +data.getCharacterName(character) ;
		}
	}
	public String getAnnotation(){
		if (data == null)
			return null;
		if (character == -1)
			return null;
		if (state == -1) {
			return data.getAnnotation(character);
		}
		else if (character > -1){
			return data.getStateNote(character, state);
		}
		return null;
	}
	public void setAnnotation(String s, boolean notify){
		if (data == null)
			return;
		if ("".equals(s))
			s = null;
		if (state == -1) {
			data.setAnnotation(character, s);
		}
		else if (character > -1){
			data.setStateNote(character, state, s);
		}
	}
}



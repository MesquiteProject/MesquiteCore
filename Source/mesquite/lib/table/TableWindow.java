/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.table;

import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.ui.MesquiteButton;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquitePDFFile;
import mesquite.lib.ui.MesquitePrintJob;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MesquiteWindow;


/* ======================================================================== */
/** A subclass of window designed to contain a MesquiteTable .*/
public abstract class TableWindow extends MesquiteWindow {
	protected static String findString = "";
	int findNumber = 0;

	/** constructor to be used only for accumulating list of commands*/
	public TableWindow () {
	}
	public TableWindow (MesquiteModule ownerModule, boolean showInfoBar) {
		super(ownerModule, showInfoBar); //INFOBAR
		setShowExplanation(true);
		if (showFindMenuItems()){
			ownerModule.addMenuItem(MesquiteTrunk.editMenu, "-", null);
			MesquiteMenuItemSpec mm = ownerModule.addMenuItem(MesquiteTrunk.editMenu, getFindLabel(), MesquiteModule.makeCommand("findString", this));
			mm.setShortcut(KeyEvent.VK_F);
			mm = ownerModule.addMenuItem(MesquiteTrunk.editMenu, "Find Again", MesquiteModule.makeCommand("findAgain", this));
			mm.setShortcut(KeyEvent.VK_G);
		}
	}

	public boolean showFindMenuItems(){
		return true;
	}
	public String getFindLabel(){
		return "Find String...";
	}
	public String getFindMessageName(){
		return "cell of Table";
	}
	public abstract MesquiteTable getTable();
	/*.................................................................................................................*/
	public void pleaseUpdate() {
		repaint();
		if (getTable()==null) return;
		getTable().repaintAll();
	}
	
	/** Return Mesquite commands that will put the table back to its current state (approximately). */
  	 public Snapshot getSnapshot(MesquiteFile file) {  //this allows employees to be dealt with
	   	 	if (getTable()==null)
	   	 		return null;
	   	 	Snapshot temp = new Snapshot();
	   	 	Snapshot ts = getTable().getSnapshot(file);
	   	 	if (ts!=null) {
				temp.addLine("getTable");
				
				temp.addLine("tell It");
				temp.incorporate(ts, true);
				temp.addLine("endTell");
	   	 	}
			temp.incorporate(super.getSnapshot(file), false);
	 	 	return temp;
  	 }
	/*...............................................................................................................*/
	/* these two methods are used for find facility  */
	protected void selectAndFocus(int ic, int it){
		MesquiteTable table = getTable();
		if (table == null)
			return;
		table.deselectAll();
		table.selectCell(ic, it);
		table.setFocusedCell(ic, it);
		table.repaintAll();
	}
	/* highlights ith cell with string */
	boolean highlightCellWithString(String s, int i){
		MesquiteTable table = getTable();
		if (table == null) 
			return false;
		int count = 0;
		//search row names
		for (int it = 0; it<table.getNumRows(); it++) {
			String c = table.getRowNameTextForDisplay(it);
			if (StringUtil.foundIgnoreCase(c, s)){
				if (count == i) {
					selectAndFocus(-1, it);
					return true;
				}
				count++;
			}
		}
		//search column names
		for (int ic = 0; ic<table.getNumColumns(); ic++) {
			String c = table.getColumnNameTextForDisplay(ic);
			if (StringUtil.foundIgnoreCase(c, s)){
				if (count == i) {
					selectAndFocus(ic, -1);
					return true;
				}
				count++;
			}
		}
		//search cells
		for (int ic = 0; ic<table.getNumColumns(); ic++) {
			for (int it = 0; it<table.getNumRows(); it++) {
			
				String c = table.getMatrixTextForDisplay(ic, it);
				if (StringUtil.foundIgnoreCase(c, s)){
					if (count == i) {
						selectAndFocus(ic, it);
						return true;
					}
					count++;
				}
			}
		}
		MesquiteMessage.println("There are no more occurrences of \"" + s + "\"");
		MesquiteMessage.beep();
		return false;
	}
	/*.................................................................................................................*/
	/** A request for the MesquiteModule to perform a command.  It is passed two strings, the name of the command and the arguments.
	This should be overridden by any module that wants to respond to a command.*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(MesquiteTable.class, "Returns the table object for the list window", null, commandName, "getTable")) {
			return getTable();
		}
    	 	else if (checker.compare(this.getClass(), "Finds cell containing string, selects it and ensures it is shown", null, commandName, "findString")) {
			if (MesquiteThread.isScripting()) //todo: should support argument passed
				return null;
			findNumber = 0;
			String temp = MesquiteString.queryString(this, "Find cell", "Find first " + getFindMessageName() + " containing the following string:", findString, 2);
			if (StringUtil.blank(temp)) {
				return null;
			}
			findString = temp;
			if (!highlightCellWithString(findString, findNumber))
				findNumber = 0;
			else
				findNumber = 1;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Finds cell containing string, selects it and ensures it is shown", null, commandName, "findAgain")) {
			if (MesquiteThread.isScripting()) //todo: should support argument passed
				return null;
			if (StringUtil.blank(findString))
				return null;
			if (!highlightCellWithString(findString, findNumber))
				findNumber = 0;
			else
				findNumber++;
    	 	}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
 	}
	/*.................................................................................................................*/
	public boolean shiftDown(String arguments){
		if (StringUtil.blank(arguments))
			return false;
		 return (arguments.indexOf("shift")>=0);
	}
	/*.................................................................................................................*/
	public boolean commandDown(String arguments){
		if (StringUtil.blank(arguments))
			return false;
		 return (arguments.indexOf("command")>=0);
	}
	/*.................................................................................................................*/
	public boolean optionDown(String arguments){
		if (StringUtil.blank(arguments))
			return false;
		 return (arguments.indexOf("option")>=0);
	}
	/*.................................................................................................................*/
	public void paintContents(Graphics g) {
		if (getTable()==null) return;
		getTable().repaintAll();
	}
	/*.................................................................................................................*/
	public String getTextContents() {
		if (getTable()==null) return null;
		return getTable().getTextVersion();
	}
	public void repaintAll() {
		if (getTable()==null) return;
		getTable().repaintAll();
	}
	/*...............................................................................................................*/
	/** Adds a button to the control strip at lower left of table.*/
	public void addControlButton(MesquiteButton b) {
		getTable().addControlButton(b);
	}
	/*...............................................................................................................*/
	/** Removes a button from the control strip at lower left of table.*/
	public void removeControlButton(MesquiteButton b) {
		getTable().removeControlButton(b);
	}
	/*.................................................................................................................*/
	public void printWindow(MesquitePrintJob pjob) {
		if (getTable()==null) return;
    	 	getTable().printTable(pjob, this);
	}
	/*.................................................................................................................*/
	public String getPrintToPDFMenuItemName() {
		return "Save Table As PDF...";
	}

	/*.................................................................................................................*/
	/**
	*@author Peter E. Midford
	*/
	public void windowToPDF(MesquitePDFFile pdfFile, int fitToPage) {
		if (getTable()==null) return;
		getTable().tableToPDF(pdfFile, this, fitToPage);
	}
	/*.................................................................................................................*
	public void windowToPDF(MesquitePDFFile pdfFile, int fitToPage) {
		if (pdfFile != null) {
			if (infoBar.getMode()>0)
				super.windowToPDF(pdfFile, fitToPage);
			else {
				Graphics g2 = pdfFile.getPDFGraphicsForComponent(getTable(),null);
				getTable().printAll(g2);
				pdfFile.end();
			}
		}
	}
	/*.................................................................................................................*/
	public void resetCursor(){  
		MesquiteTool tool = getCurrentTool();
		if (!(tool instanceof TableTool))
			return;
		if (((TableTool)tool).getWorksOnColumnNames()) {
			Cursor c = tool.getCursor();
			if (c != null)
				getTable().getColumnNamesPanel().setCursor(tool.getCursor());
		}
		if (((TableTool)tool).getWorksOnRowNames()) {
			Cursor c = tool.getCursor();
			if (c != null)
				getTable().getRowNamesPanel().setCursor(tool.getCursor());
		}
		if (((TableTool)tool).getWorksOnCornerPanel()) {
			Cursor c = tool.getCursor();
			if (c != null)
				getTable().getCornerPanel().setCursor(tool.getCursor());
		}
		if (((TableTool)tool).getWorksOnMatrixPanel()) {
			Cursor c = tool.getCursor();
			if (c != null)
				getTable().getMatrixPanel().setCursor(tool.getCursor());
		}
		super.resetCursor();
		//setCursor(Cursor.getDefaultCursor());
	}
	/*.................................................................................................................*/
	public void setCurrentTool(MesquiteTool tool){
		if (tool!=null && !tool.getEnabled())
			return;
		if (!(tool instanceof TableTool)) {
			MesquiteMessage.warnProgrammer("attempt to set non-table tool as current tool");
			return;
		}
		super.setCurrentTool(tool);
		if (getTable()==null) return;
		resetCursor();
	}
	/*.................................................................................................................*/
    	 public void copyGraphicsPanel() {
		if (getTable()==null || getTable().getCopyCommand() == null) 
			return;
		getTable().getCopyCommand().doItMainThread("", null, this);  // command invoked
   	 }
    	 public String getCopySpecialName() {
		return "Copy Literal"; 
   	 }
    	 public MesquiteCommand getCopySpecialCommand() {
		if (getTable()==null) return null;
		return getTable().getCopyLiteralCommand(); 
   	 }
    	 public MesquiteCommand getPasteCommand() {
		if (getTable()==null) return null;
		return getTable().getPasteCommand(); 
   	 }
    	 public MesquiteCommand getCutCommand() {
		if (getTable()==null) return null;
		return getTable().getCutCommand(); 
   	 }
    	 public MesquiteCommand getClearCommand() {
		if (getTable()==null) return null;
		return getTable().getClearCommand(); 
   	 }
  	public void selectAllGraphicsPanel(){
		if (getTable()==null && getTable().getSelectAllCommand()!=null) 
			return;
		getTable().getSelectAllCommand().doItMainThread(null, "", this); 
   	 }
}




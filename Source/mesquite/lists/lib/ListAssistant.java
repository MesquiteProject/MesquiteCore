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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.StringUtil;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.table.TableWindow;



/* ======================================================================== */
public abstract class ListAssistant extends MesquiteModule  {
	
   	 public Class getDutyClass() {
   	 	return ListAssistant.class;
   	 }
 	public String getDutyName() {
 		return "List assistant";
   	 }
	/**  Set the table and list's object (e.g, listable vector, data matrix for characters, etc.) */
	public abstract void setTableAndObject(MesquiteTable table, Object object);
	
	/** Returns whether to use the string from getStringForRow; otherwise call drawInCell*/
	public boolean useString(int ic){
		return (getStringForRow(ic)!=null);
	}
	/** Returns string to be placed in cell for row */
	public abstract String getStringForRow(int ic);

	/** Gets background color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getBackgroundColorOfCell(int ic, boolean selected){
		return null;
	}
	/** Gets text color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getTextColorOfCell(int ic, boolean selected){
		return null;
	}
	/*...............................................................................................................*/
	/** returns whether or not any cells can be pasted into.*/
	public boolean allowPasting(){
		return false;
	}
	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int row){
		return false;
	}
	/*...............................................................................................................*/
	/** for those permitting editing, indicates user has edited to incoming string.*/
	public void setString(int row, String s){
		
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Pastes", "", commandName, "paste")) {
			if (!allowPasting())
				return null;
			MesquiteTable table = findTable();
			if (table == null)
				return null;
			MesquiteBoolean success = pasteIntoRows(table);
			if (StringUtil.notEmpty(success.getName()))
				discreetAlert("Problem with pasting:" + success.getName());
			if (!MesquiteThread.isScripting() && success.getValue()) parametersChanged();
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	//See TreeListAttachment for an example use of this. Table is passed because it's the responsibility of the ListAssistant to remember it.
	public MesquiteBoolean pasteIntoRows(MesquiteTable table){
		//get rows from clipboard
		//for each row selected, paste next row in clipboard
		String[] lines = null; 
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = clip.getContents(this);
		String warning = "";
		boolean ineditable = false;
		boolean changed = false;
		try {
			String s = (String) t.getTransferData(DataFlavor.stringFlavor);
			if (s != null) {
				lines = StringUtil.getLines(s);
				if (lines != null && lines.length > 0){
					boolean tabs = false; //disallow tabs within lines
					for (int i=0; i<lines.length; i++){
						if (lines[i] != null && lines[i].indexOf("\t")>=0){
							tabs = true;
							break;
						}
					}
					if (tabs)
						warning += " There cannot be any tabs in the pasted items.";
					else {
						int numRowsSelected = table.numRowsSelected();
						//if none are selected, then the number of lines pasted must exactly match the number of rows, and the pasting is done for all
						if (numRowsSelected == 0){
							if (lines.length == table.getNumRows()){
								for (int i = 0; i<lines.length; i++) {
									if (!isCellEditable(i))
										ineditable = true;
									else
									setString(i, lines[i]);
								}
								changed = true;
							}
							else
								warning += " The number of rows does not match the number of items to be pasted.";	
								
						}
						//if just one is selected, then the lines are pasted from the there downward
						else if (numRowsSelected == 1){
							int firstRow = table.firstRowSelected();
							for (int i = 0; i<lines.length && i+firstRow<table.getNumRows(); i++){
								if (!isCellEditable(i+firstRow))
									ineditable = true;
								else
								setString(i+firstRow, lines[i]);
							}
							changed = true;
					}
						//if multiple are selected, then the either the number of lines pasted must be one, and it is put into all, ....
						else if (lines.length == 1){
							for (int row = 0;row<table.getNumRows(); row++)
								if (table.isRowSelected(row)){
									if (!isCellEditable(row))
										ineditable = true;
									else
								setString(row, lines[0]);
								}
							changed = true;
						}
						//     ...  or the number of lines pasted must match the number of rows selected
						else if (lines.length == numRowsSelected){
							int lineCount = 0;
							for (int row = 0;row<table.getNumRows(); row++)
								if (table.isRowSelected(row)){
									if (!isCellEditable(row))
										ineditable = true;
									else
									setString(row, lines[lineCount++]);
								}
							changed = true;
						}
						else 
							warning += " The number of rows selected (" + numRowsSelected + ") does not match the number of items to be pasted (" + numRowsSelected + ").";	

					}

				}
			}
		} catch (Exception e) {
			warning = "There was a problem in pasting.";
		}
		if (ineditable)
			warning += " Some or all are ineditable, and could not accept pasting.";
		return new MesquiteBoolean(warning, changed);
	}
	public ListModule getListModule(){
		return (ListModule)findEmployerWithDuty(ListModule.class);
	}
	protected MesquiteTable findTable(){
		return ((TableWindow)findEmployerWithDuty(ListModule.class).getModuleWindow()).getTable();
	}
	/** Draw cell for row ic */
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
	}
	/** Returns widest string in column; can return by default a long string for safety */
	public String getWidestString(){
		return "";
	}
	public String getExplanationForRow(int ic){
		return null;
	}
	
	public boolean canHireMoreThanOnce(){
		return false;
	}
	
	/*.................................................................................................................*/
	public boolean arrowTouchInRow(Graphics g, int ic, int x, int y, boolean doubleClick, int modifiers){ //so assistant can do something in response to arrow touch; return true if the event is to stop there, i.e. be intercepted
		return false;
	}
	
	/** following method is DEPRECATED */
	public boolean arrowTouchInRow(int ic, boolean doubleClick){ 
		return false;
	}
	

	/*.................................................................................................................*/
	public String moduleActiveWord() {
		return "shown";
	}
	public int getColumnWidth(){
		return 0;
	}
	/** Returns column title */
	public abstract String getTitle();
}


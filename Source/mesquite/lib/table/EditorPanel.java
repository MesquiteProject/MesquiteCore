/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. 
Version 1.1, May 2006.
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

/* ======================================================================== */
/** A superclass for the panels that make up a MesquiteTable.  Has an editable text field that can be repositioned over a cell.*/
public abstract class EditorPanel extends MesquitePanel {
	MesquiteTable tb;
	protected EditorTextField editField;
	protected Color fillColor= null;
	int editColumn=-1;
	int editRow = -1;
	protected int returningColumn = -2;
	protected int returningRow  = -2;
	Polygon dropDownTriangle;
	
	public EditorPanel (MesquiteTable table) {
		super();
		dropDownTriangle=MesquitePopup.getDropDownTriangle();
		tb = table;
		editField = new EditorTextField(this, 0, 0);
		add(editField);
		editField.setVisible(false);
		setBackground(ColorDistribution.light[table.colorScheme]);
	}
	public abstract void textReturned(int column, int row, String text, CommandRecord commandRec);
	public abstract String getText(int column, int row);
	public void setFillColor(Color toFill){
		fillColor = toFill;
	}
	public int startOfColumn(int column) {
			if (column<tb.firstColumnVisible)
				return -1;
			else if (column>tb.firstColumnVisible + tb.numColumnsTotal || column>=tb.numColumnsTotal)
				return -1;
			else {
				int lineX = 0;
			//	if (tb.showRowGrabbers)
			//		lineX = tb.getRowGrabberWidth();
				for (int c=tb.firstColumnVisible; (c<column); c++) {
					lineX += tb.columnWidths[c];
				}
				return lineX;
			}
	}
	public int endOfColumn(int column) {
		if (column<tb.firstColumnVisible)
			return -1;
		else if (column>tb.firstColumnVisible + tb.numColumnsTotal || column>=tb.numColumnsTotal)
			return -1;
		else
			return startOfColumn(column)+tb.columnWidths[column];
	}
	public int endOfLastColumn() {
			int lastColumn = tb.getLastColumn();
			if (lastColumn<0)
				return 0;
			else
				return startOfColumn(lastColumn)+tb.columnWidths[lastColumn];
	}
	public int startOfRow(int row) {
			if (row<tb.firstRowVisible)
				return -1;
			else if (row>tb.firstRowVisible + tb.numRowsTotal)
				return -1;
			else {
				int lineX = 0;
			//	if (tb.showRowGrabbers)
			//		lineX = tb.getRowGrabberWidth();
				for (int c=tb.firstRowVisible; (c<row); c++) {
					lineX += tb.rowHeights[c];
				}
				return lineX;
			}
	}
	public int leftEdgeOfRow(int row) {
		return startOfColumn(tb.firstColumnVisible);
	}
	public int rightEdgeOfRow(int row) {
		return endOfColumn(tb.getLastColumnVisible());
	}
	public int endOfRow(int row) {
		if (row<tb.firstRowVisible)
			return -1;
		else if (row>tb.firstRowVisible + tb.numRowsTotal)
			return -1;
		else
			return startOfRow(row)+tb.rowHeights[row];
	}
	public int endOfLastRow() {
			int lastRow = tb.getLastRow();
			if (lastRow<0)
				return 0;
			else
				return startOfRow(lastRow)+tb.rowHeights[lastRow];
	}
	
	public abstract int findRegionInCellH(int x);
	public abstract int findRegionInCellV(int y);
	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public abstract int findColumn(int x);
	/** returns in which row y lies, -1 if above, -2 if below.*/
	public abstract int findRow(int y);
	
	public MesquiteTable getTable(){
		return tb;
	}
	public int firstColumnVisible(){
		return tb.firstColumnVisible;
	}
	public int firstRowVisible(){
		return tb.firstRowVisible;
	}
	public int numColumnsVisible(){
		return tb.numColumnsTotal;
	}
	public int numRowsVisible(){
		return tb.numRowsTotal;
	}
	public int columnWidth(int column) {
		if (!tb.columnLegal(column))
			return -1;
		return tb.columnWidths[column];
	}
	public int rowHeight(int row) {
		if (!tb.rowLegal(row))
			return -1;
		return tb.rowHeights[row];
	}
		
	public boolean getEditing(){
		return editField.getEditing();
	}
	public boolean getEditingAllSelected(){
		if (!editField.getEditing())
			return false;
		return (editField.getSelectionStart()==0 && editField.getSelectionEnd()== editField.getText().length());
		
	}
	public EditorTextField getEditField(){
		return editField;
	}
	public boolean checkEditFieldLocation(){
		if (editField.getEditing()) {
			int column = editField.getColumn();
			int row = editField.getRow();
			if (column>=firstColumnVisible() && column<firstColumnVisible()+numColumnsVisible() && (row == -1 || (row>=firstRowVisible() && row<firstRowVisible()+numRowsVisible())) ) {
				boolean ok = checkBounds();
				if (!editField.isVisible())
					editField.setVisible(true);
				return ok;
			}
			else {
				editField.setVisible(false);
			}
		}
		else {
			if (editField.isVisible())
				editField.setVisible(false);
		}
		return true;
	}
	public void offEditFocusLost(){  
		int c = editColumn;
		int r = editRow;
		editField.offEdit();
		editField.setVisible(false);
		editField.setText(null);
		editField.setBounds(0, 0, 0, 0);
		editField.setColumnRow(-2, -2);
		editColumn=-2;
		editRow = -2;
		tb.setFocusedCell(c, r);
	}
	public void offEdit(){  
		editField.offEdit();
		editField.setVisible(false);
		editField.setText(null);
		editField.setBounds(0, 0, 0, 0);
		editField.setColumnRow(-2, -2);
		editColumn=-2;
		editRow = -2;
	}
	
	public void recordEdit(){  
		editField.recordEdit();
	}
	public abstract void deselectCell(int column,int row);
	
	public void aboutToReturnText(int column, int row){
		returningColumn = column;
		returningRow  = row;
		deselectCell(column, row);
	}
	public void editCell(int column, int row){  
		tb.offOtherEdits(this);
		if (editField.getEditing() && (editField.getColumn()!=column || editField.getRow() != row)) {
			recordEdit(); //used to be offEdit
		}
		tb.selectCell(column, row);
		editColumn=column;
		editRow = row;
		String t = getText(column, row);
		if (t==null)
			t="";
		editField.setEditing(true);
		//last
		editField.setColumnRow(column, row);
		editField.setText(t);
		checkBounds();
		editField.selectAll();
		editField.setVisible(true);
		editField.requestFocus();
		tb.setFocusedCell(column, row);
	}
	public abstract void redrawCell(int column, int row);
/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Indicates editing is complete and text is being passed back", "[column] [row] [tokenized text]", commandName, "returnText")) {
    	 		int column = MesquiteInteger.fromFirstToken(arguments, pos);
    	 		int row = MesquiteInteger.fromString(arguments, pos);
    	 		String s  = ParseUtil.getToken(arguments, pos);
			returningColumn = -2;
			returningRow  = -2;
			if (s !=null)
    	 			textReturned(column, row, s, commandRec);
    	 		redrawCell(column, row);
    	 		return null;
    	 	}
    	 	else
    	 		return super.doCommand(commandName, arguments, commandRec, checker);
   	 }
	/*...............................................................................................................*/
	protected void prepareCell(Graphics g, int x, int y, int w, int h, boolean focused, boolean selected, boolean dimmed, boolean editable){
			Color color;
			if (selected) {
				if (!MesquiteWindow.Java2Davailable)
					color = Color.black;
				else
					color = Color.white;
			}
			else if (focused)
				color = Color.lightGray;
			else if (fillColor!=null)
				color = fillColor;
			else if (dimmed)
				color = Color.lightGray;
			else if (editable)
				color = Color.white;
			else
				color = ColorDistribution.uneditable;
		//	color = Color.green;
			g.setColor(color);
			g.fillRect(x,y,w,h);
			if (selected)
				GraphicsUtil.fillTransparentSelectionRectangle(g,x,y,w,h);
			
			Color textColor;
			if (selected)
				textColor = Color.white;
			else if (dimmed)
				textColor = Color.gray;
			else
				textColor = Color.black;
			g.setColor(textColor);
	}
	public void enterPressed(KeyEvent e){
		if (!getEditing()) {
			return;
		}

		int r = editField.getRow();
		if (r+1<tb.numRowsTotal) {
			e.consume();
			editCell(editField.getColumn(), r+1);
		}
		else
			e.consume();
			
	}

	public void tabPressed(KeyEvent e){
		if (!getEditing())
			return;
		int c = editField.getColumn();
		if (c+1<tb.numColumnsTotal) {
			e.consume();
			editCell(c+1, editField.getRow());
		}
		else {
			e.consume();
		}
		
	}
	public void downArrowPressed(KeyEvent e){
		if (getEditing())
			enterPressed(e);
	}
	
	public void upArrowPressed(KeyEvent e){ //
		if (!getEditing())
			return;
		int r = editField.getRow();
		if (r-1>=0) {
			e.consume();
			editCell(editField.getColumn(), r-1);
		}
		else
			e.consume();
	}
	
	public void rightArrowPressed(KeyEvent e){
		if (getEditingAllSelected()) {
			tabPressed(e);
		}
	}
	public void leftArrowPressed(KeyEvent e){
		if (!getEditing() || !getEditingAllSelected()) {
			//if cell selected, move selection
			return;
		}
		int c = editField.getColumn();
		if (c-1>=0) {
			e.consume();
			editCell(c-1, editField.getRow());
		}
		else {
			e.consume();
		}
	}
	public boolean checkBounds(){
		int w;
		int left=startOfColumn(editColumn);
		int top = startOfRow(editRow);
		if (columnWidth(editColumn)+1>editField.getMinimalWidth()) 
			w = columnWidth(editColumn)+1;
		else
			w = editField.getMinimalWidth();
		if ((editColumn==-1) && tb.showRowGrabbers)
			left = tb.getRowGrabberWidth();
		if ((editRow==-1) && tb.showColumnGrabbers)
			top = tb.getColumnGrabberWidth();
		Rectangle current = editField.getBounds();
		
		if (current.width != w || (current.height < rowHeight(editRow)+1 || current.height > rowHeight(editRow)+2)){ //1. 06 permit to be one off (strange behaviour in java 1.4)
			editField.setBounds(left, top, w, rowHeight(editRow)+1);
			return false;
		}
		else if (current.x != left || current.y != top){
			editField.setLocation(left, top);
			return false;
		}
		return true;
	}
	//this is done to limit flickering & because of apparent bugs in Windows java VM (1.3)
	public void update(Graphics g){
		paint(g);
	}
}



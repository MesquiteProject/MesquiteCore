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
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquitePanel;
import mesquite.lib.ui.MesquitePopup;

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
	public static final int BEYONDMATRIX = -2;
	Polygon dropDownTriangle;

	public EditorPanel (MesquiteTable table) {
		super();
		dropDownTriangle=MesquitePopup.getDropDownTriangle();
		tb = table;
		editField = new EditorTextField(this, 0, 0);
		add(editField);
		editField.setVisible(false);
		setBackground(ColorTheme.getInterfaceBackgroundPale());
	}
	public abstract void textReturned(int column, int row, String text);
	public abstract String getText(int column, int row);
	public void setFillColor(Color toFill){
		fillColor = toFill;
	}
	public int startOfColumn(int column) {
		return tb.startOfColumn(column);
	}
	/*...............................................................................................................*/
	public void drawBetweenSelection(Graphics g){
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
		try {
		int lastColumn = tb.getLastColumn();
		if (lastColumn<0)
			return 0;
		else if (lastColumn<tb.columnWidths.length)
			return startOfColumn(lastColumn)+tb.columnWidths[lastColumn];
		else
			return startOfColumn(lastColumn);
		}
		catch (Exception e){
		}
		return -1;
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
			for (int c=tb.firstRowVisible; (c<row && c< tb.rowHeights.length); c++) {
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
	public abstract int findColumn(int x, int y);
	/** returns in which row y lies, -1 if above, -2 if below.*/
	public abstract int findRow(int x, int y);

	/*@@@...............................................................................................................*/
	/** Returns  the column immediately after the boundary between rows nearest to the y value, -1 if to the left of all columns, -2 if after all columns.*/
	public int findColumnBeforeBetween(int x, int y) {
		if (x<=0)
			return -1;
		int cx = 0;
		int columnCenterX = 0;
		int lastColumnCenterX=-1;
		for (int column=tb.firstColumnVisible; (column<tb.numColumnsTotal); column++) {
			cx += tb.columnWidths[column];
			columnCenterX = cx - tb.columnWidths[column]/2;
			if (column>= tb.numColumnsTotal)
				return -1;
			else if (x>lastColumnCenterX && x<= columnCenterX) {
				return column-1;
			} else if (columnCenterX>x)
				return column;
			lastColumnCenterX = columnCenterX;
		}
		return -2;//past the last column
	}
	/*@@@...............................................................................................................*/
	/** Returns  the row immediately after the boundary between rows nearest to the y value, -1 if above all rows, -2 if below all rows.*/
	public int findRowBeforeBetween(int x, int y) {
		if (y<0)
			return -1;
		int ry = 0;
		int rowCenterY = 0;
		int lastRowCenterY = -1;
		for (int row=tb.firstRowVisible; (row<tb.numRowsTotal); row++) {
			ry += tb.rowHeights[row];
			rowCenterY = ry-tb.rowHeights[row]/2;
			if (row>= tb.numRowsTotal) {
				return -2;
			}
			else if (y>lastRowCenterY && y<= rowCenterY) {
				return row-1;
			} else if (rowCenterY>y)
				return row;
			lastRowCenterY = rowCenterY;
		}

		return -2;//past the last row
	}
	/* ............................................................................................................... */
	protected int getNearZone(int rowColSize) {
		if (rowColSize<=2)
			return 0;
		else if (rowColSize<=4)
			return 1;
		else if (rowColSize<=10)
			return 2;
		return 3;
	}
	/* ............................................................................................................... */
	/** returns true if x is near the boundary of a column */
	public boolean nearColumnBoundary(int x, int y) {
		int columnBoundary = 0;
		int nearZoneOnRight;
		int nearZoneOnLeft;
		// int lastEdge=0;
		for (int column = tb.firstColumnVisible; (column < tb.numColumnsTotal) && (columnBoundary < x); column++) {
			if (column==tb.firstColumnVisible)
				nearZoneOnLeft = 0;
			else
				nearZoneOnLeft = getNearZone(tb.columnWidths[column]);
			if (column==tb.lastColumnVisible)
				nearZoneOnRight = 0;
			else
				nearZoneOnRight = getNearZone(tb.columnWidths[column]);

			columnBoundary += tb.columnWidths[column];
			if (x>columnBoundary-nearZoneOnLeft  && x<columnBoundary+nearZoneOnRight){
				return true;
			}
		}
		return false;

	}
	/* ............................................................................................................... */
	/** returns true if x is near the boundary of a column */
	public int nearWhichColumnBoundary(int x, int y) {
		int columnBoundary = 0;
		int nearZoneOnRight;
		int nearZoneOnLeft;
		// int lastEdge=0;
		for (int column = tb.firstColumnVisible; (column < tb.numColumnsTotal) && (columnBoundary < x); column++) {
			if (column==tb.firstColumnVisible)
				nearZoneOnLeft = 0;
			else
				nearZoneOnLeft = getNearZone(tb.columnWidths[column]);
			if (column==tb.lastColumnVisible)
				nearZoneOnRight = 0;
			else
				nearZoneOnRight = getNearZone(tb.columnWidths[column]);

			columnBoundary += tb.columnWidths[column];
			if (x>columnBoundary-nearZoneOnLeft  && x<columnBoundary+nearZoneOnRight){
				return column;
			}
		}
		return -1;

	}

	/* ............................................................................................................... */
	/** returns true if y is near the boundary of a row */
	public boolean nearRowBoundary(int x, int y) {
		int rowBoundary = 0;
		int nearZoneOnBottom;
		int nearZoneOnTop;
		// int lastEdge=0;
		for (int row = tb.firstRowVisible; (row < tb.numRowsTotal); row++) {
			if (row==tb.firstRowVisible)
				nearZoneOnTop = 0;
			else
				nearZoneOnTop = getNearZone(tb.rowHeights[row]);
			if (row==tb.lastRowVisible)
				nearZoneOnBottom = 0;
			else
				nearZoneOnBottom = getNearZone(tb.rowHeights[row]);

			rowBoundary += tb.rowHeights[row];
			if (y>rowBoundary-nearZoneOnTop  && y<rowBoundary+nearZoneOnBottom)
				return true;
		}
		return false;

	}
	public MesquiteTable getTable(){
		return tb;
	}
	public int firstColumnVisible(){
		if (tb == null)
			return 0;
		return tb.firstColumnVisible;
	}
	public int firstRowVisible(){
		if (tb == null)
			return 0;
		return tb.firstRowVisible;
	}
	public int numColumnsVisible(){
		if (tb == null)
			return 0;
		return tb.numColumnsTotal;
	}
	public int numRowsVisible(){
		if (tb == null)
			return 0;
		return tb.numRowsTotal;
	}
	public int columnWidth(int column) {
		if (tb == null)
			return 0;
		if (!tb.columnLegal(column))
			return -1;
		if (column>= tb.columnWidths.length)
			return -1;
		return tb.columnWidths[column];
	}
	public int rowHeight(int row) {
		if (tb == null)
			return 0;
		if (!tb.rowLegal(row))
			return -1;
		if (row>= tb.rowHeights.length)
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
	public void offEditDontRecord(){  //1. 12
		editField.offEditDontRecord();
		editField.setVisible(false);
		editField.setText(null);
		editField.setBounds(0, 0, 0, 0);
		editField.setColumnRow(-2, -2);
		editColumn=-2;
		editRow = -2;
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
	public void resetUndoAfterLeavingEditorCell(int column, int row){  
	}

	public void editCell(int column, int row){  
		tb.offOtherEdits(this);
		if (editField.getEditing() && (editField.getColumn()!=column || editField.getRow() != row)) {
			resetUndoAfterLeavingEditorCell(column, row);
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
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Indicates editing is complete and text is being passed back", "[column] [row] [tokenized text]", commandName, "returnText")) {
			int column = MesquiteInteger.fromFirstToken(arguments, pos);
			int row = MesquiteInteger.fromString(arguments, pos);
			String s  = ParseUtil.getToken(arguments, pos);
			returningColumn = -2;
			returningRow  = -2;
			if (s !=null)
				textReturned(column, row, s);
			redrawCell(column, row);
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*...............................................................................................................*/
	protected void prepareCell(Graphics g, int column, int row, int x, int y, int w, int h, boolean focused, boolean selected, boolean dimmed, boolean editable){
		Color color = tb.getBackgroundColor(column, row, selected);
		if (color==null) {
			if (selected) {
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
		}
		//		color = Color.lightGray;  //
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
		else if (r+1 == tb.numRowsTotal){
			e.consume();
			offEdit();
			tb.clickOutside();
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
		else if (c+1==tb.numColumnsTotal) {
			e.consume();
			offEdit();
			tb.clickOutside();
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
		else if (r-1<0) {
			e.consume();
			offEdit();
			tb.clickOutside();
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
		else if (c-1<0) {
			e.consume();
			offEdit();
			tb.clickOutside();
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

		int buffer = 3;
		left = left-buffer;
		w = w+buffer*2;
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




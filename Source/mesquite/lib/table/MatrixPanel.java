/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison. 
Version 2.01, December 2007.
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
import mesquite.lib.duties.FileInterpreter;

import java.io.*;


/* ======================================================================== */
/** A panel for the main body of cells (i.e., excluding the column and row headings) of a MesquiteTable */
public class MatrixPanel extends EditorPanel implements FocusListener {
	MesquiteTable table;
	public int width,  height;
	int hFloat = -1;
	int vFloat = -1;

	public MatrixPanel (MesquiteTable table , int w, int h) {
		super(table);
		this.table=table;
		this.width=w;
		this.height=h;
		addFocusListener(this);
		//setBackground(ColorDistribution.medium[table.colorScheme]);
		setBackground(Color.white);
	}
	/*...............................................................................................................*/
	public void setTableUnitSize (int w, int h) {
		this.width=w;
		this.height=h;
	}


	/*...............................................................................................................*/
	public String getText(int column, int row){
		return table.getMatrixText(column, row);
	}
	public void deselectCell(int column,int row){
		table.deselectCell(column, row);
	}
	/*...............................................................................................................*/
	static long redrawCells = 0;
	static long paints = 0;
	/*...............................................................................................................*/
	public void redrawCell(int column, int row){
		Graphics g = getGraphics();
		if (g!=null) {
			redrawCell(g, column, row);
			g.dispose();
		}
	}
	/*...............................................................................................................*/
	/*MesquiteTimer timer1 = new MesquiteTimer();
MesquiteTimer timer2 = new MesquiteTimer();
MesquiteTimer timer3 = new MesquiteTimer();
MesquiteTimer timer4 = new MesquiteTimer();
MesquiteTimer timer5 = new MesquiteTimer();
MesquiteTimer timer6 = new MesquiteTimer();

timer1.start(); //600

timer1.end();
timer2.start(); //648

timer2.end();
timer3.start(); //277

timer3.end();
timer4.start();  //57

timer4.end();
timer5.start(); //500

timer5.end();
timer6.start(); //270

timer6.end();
.println("timers 1 " + timer1.getAccumulatedTime() + " timer2 " + timer2.getAccumulatedTime()  + " timer3 " + timer3.getAccumulatedTime() + " timer4 " + timer4.getAccumulatedTime() + " timer5 " + timer5.getAccumulatedTime() + " timer6 " + timer6.getAccumulatedTime());
	 */
	public void redrawCell(Graphics g, int column, int row) {
		redrawCellOffset(g, column, row, 0, 0);
	}

	public void redrawCellBlock(Graphics g, int columnStart, int columnEnd, int rowStart, int rowEnd) {
		for (int i = columnStart; i<=columnEnd; i++)
			for (int j=rowStart; j<rowEnd; j++)
				redrawCellOffset(g, i, j, 0, 0);
	}

	public void redrawRow(Graphics g, int row) {
		int lineX = 0;
		int top = table.getTopOfRow(row);
		for (int i = table.getFirstColumnVisible(); i<=table.getLastColumnVisible(); i++) 
			if (i>=0 && i<table.getNumColumns()) {
				lineX += table.columnWidths[i];
				redrawCellOffset(g, i, row, 0, 0);
				g.setColor(Color.gray);

				g.drawLine(lineX,top, lineX, top+rowHeight(row));//matrixHeight + columnNamesRowHeight
				g.drawLine(lineX,top+rowHeight(row), lineX+columnWidth(i), top+rowHeight(row));//matrixHeight + columnNamesRowHeight
			}
	}


	//draws cell appearing at column, row, but with contents for cell column+offsetColumn, row+offsetRow.  Used with non-zero offsets for quick draw during manual sequence alignment
	public void redrawCellOffset(Graphics g, int column, int row, int offsetColumn, int offsetRow) {
		redrawCells++;
		int left = table.getFirstColumnVisible();
		int top = table.getFirstRowVisible();
		if (column<left || row<top)  
			return;  
		if (column== returningColumn && row == returningRow){
			//MesquiteTrunk.mesquiteTrunk.logln("WARNING: error drawing matrix (returning; MatrixPanel) " + column + "  " + row);
			return; //don't draw if text about to be returned to cell, and will soon be redrawn anyway
		}
		if (column>= table.columnWidths.length || row >= table.rowHeights.length)
			return;
		int leftSide = startOfColumn(column);
		int topSide = startOfRow(row);
		if (leftSide>getBounds().width || leftSide+columnWidth(column)<0) {
			return;
		}
		if (topSide>getBounds().height || topSide+rowHeight(row)<0) {
			return;
		}
		Shape clip = null;
		if (!table.useQuickMode()) {
			clip = g.getClip();
			g.setClip(leftSide,topSide,table.columnWidths[column], table.rowHeights[row]);
		}
		if ((offsetColumn!=0 || offsetRow !=0)){
			if (!table.columnLegal(column+offsetColumn) || !table.rowLegal(row+offsetRow))
				prepareCell(g, leftSide+1,topSide+1,table.columnWidths[column]-1, table.rowHeights[row]-1, false, false, true, true);
			else {
				boolean selected = table.isCellSelected(column+offsetColumn, row+offsetRow) || table.isRowSelected(row+offsetRow)|| table.isColumnSelected(column+offsetColumn);

				if (!table.useString(column,row)) {
					table.drawMatrixCell(g, leftSide,topSide,table.columnWidths[column], table.rowHeights[row], column+offsetColumn, row+offsetRow, selected);
				}
				else  {
					String supplied = table.getMatrixTextForDisplay(column+offsetColumn,row+offsetRow);
					prepareCell(g, leftSide+1,topSide+1,table.columnWidths[column]-1, table.rowHeights[row]-1, false, selected, table.getCellDimmed(column+offsetColumn, row+offsetRow), table.isCellEditable(column+offsetColumn, row+offsetRow));
					table.drawMatrixCellString(g, null, leftSide,topSide,table.columnWidths[column], table.rowHeights[row], column+offsetColumn, row+offsetRow, supplied);
				}
//				table.drawMatrixCellExtras(g, leftSide,topSide,table.columnWidths[column], table.rowHeights[row], column+offsetColumn, row+offsetRow);
			}
		}
		else {
			boolean selected = table.isCellSelected(column, row) || table.isRowSelected(row)|| table.isColumnSelected(column);

			if (!table.useString(column,row)) {
				table.drawMatrixCell(g, leftSide,topSide,table.columnWidths[column], table.rowHeights[row], column, row, selected);
			}
			else  {
				String supplied = table.getMatrixTextForDisplay(column,row);
				prepareCell(g, leftSide+1,topSide+1,table.columnWidths[column]-1, table.rowHeights[row]-1, false, selected, table.getCellDimmed(column, row), table.isCellEditable(column, row));
				table.drawMatrixCellString(g, null, leftSide,topSide,table.columnWidths[column], table.rowHeights[row], column, row, supplied);
			}
//			table.drawMatrixCellExtras(g, leftSide,topSide,table.columnWidths[column], table.rowHeights[row], column, row);

		}

		if (table.getDropDown(column, row)) {
			dropDownTriangle.translate(leftSide+1,topSide + 1);
			g.setColor(Color.white);
			g.drawPolygon(dropDownTriangle);
			g.setColor(Color.black);
			g.fillPolygon(dropDownTriangle);
			dropDownTriangle.translate(-(leftSide+1),-(topSide + 1));
		}
		if (!table.useQuickMode() && clip !=null) {
			g.setClip(clip);
		}
	}
	/*...............................................................................................................*/
	public void blankCell(int column, int row) {
		Graphics g = getGraphics();
		if (g==null)
			return;
		blankCell(column, row, g);
		g.dispose();
	}
	/*...............................................................................................................*/
	public void blankCell(int column, int row, Graphics g) {
		int left = table.getFirstColumnVisible();
		int top = table.getFirstRowVisible();

		if (column<left || row<top)  
			return;  
		int leftSide = startOfColumn(column);
		int topSide = startOfRow(row);
		if (leftSide>getBounds().width || leftSide+columnWidth(column)<0)
			return;
		if (topSide>getBounds().height || topSide+rowHeight(row)<0)
			return;
		Shape clip = g.getClip();
		g.setClip(leftSide,topSide,table.columnWidths[column], table.rowHeights[row]);

		prepareCell(g, leftSide+1,topSide+1,table.columnWidths[column]-1, table.rowHeights[row]-1, false, table.isCellSelected(column, row) || table.isRowSelected(row)|| table.isColumnSelected(column), false, table.isCellEditable(column, row));

		g.setClip(clip);
	}

	/*...............................................................................................................*/
	public void textReturned(int column, int row, String text){
		table.returnedMatrixText(column, row, text);
	}
	/*...............................................................................................................*/
	public void print(Graphics g) {
		int lineY;
		int lineX;
		if (table.frameMatrixCells) {
			int columnHeight = table.getTotalRowHeight();
			g.setColor(Color.gray);
			lineX = 0;
			for (int c=0; c<table.numColumnsTotal; c++) {
				lineX += table.columnWidths[c];
				g.drawLine(lineX, 0, lineX, columnHeight);//matrixHeight + columnNamesRowHeight
			}
			int rowLength = table.getTotalColumnWidth();

			lineY = 0;
			for (int r=0; (r<table.numRowsTotal); r++) {
				lineY += table.rowHeights[r];
				g.drawLine(0, lineY, rowLength, lineY);//rowNamesWidth+matrixWidth
			}
		}

		g.setColor(Color.black);
		lineY = 0;
		int oldLineY=lineY;

		int numCells = 0;//еее

		Shape clip = g.getClip();
		for (int r=0; (r<table.numRowsTotal); r++) {
			lineY += table.rowHeights[r];

			lineX = 0;
			int oldLineX=lineX;
			for (int c=0; (c<table.numColumnsTotal); c++) {
				numCells++;
				lineX += table.columnWidths[c];
				g.setClip(oldLineX,oldLineY,table.columnWidths[c], table.rowHeights[r]);
				String supplied = table.getMatrixTextForDisplay(c,r);
				Color bc = table.getBackgroundColor(c, r, false);
				if (bc != null)
					g.setColor(bc);
				else if (fillColor!=null)
					g.setColor(fillColor);
				else if (table.isCellEditable(c, r))
					g.setColor(Color.white);
				else
					g.setColor(ColorDistribution.uneditable);
				g.fillRect(oldLineX+1,oldLineY+1,table.columnWidths[c]-1, table.rowHeights[r]-1);
				g.setColor(Color.black);
				if (!table.useString(c,r))
					table.drawMatrixCell(g, oldLineX,oldLineY,table.columnWidths[c], table.rowHeights[r], c, r, false);
				else 
					table.drawMatrixCellString(g, null, oldLineX,oldLineY,table.columnWidths[c], table.rowHeights[r], c, r, supplied);
				oldLineX = lineX;
			}
			oldLineY=lineY;
		}
		g.setClip(clip);
	}

	/*...............................................................................................................*/
	public void drawBetweenSelection(Graphics g){
		if (table.selectedBetweenColumns()) {
			int x = table.getColumnX(table.getStartBetweenColumnSelection());
			int top = table.getRowY(table.getStartBetweenRowSelection()-1);
			int bottom = table.getRowY(table.getEndBetweenRowSelection());
			g.setColor(Color.blue);
			g.fillRect(x-MesquiteTable.BETWEENLINEWIDTH/2,top,MesquiteTable.BETWEENLINEWIDTH,bottom-top);
			g.setColor(Color.yellow);
			g.drawRect(x-MesquiteTable.BETWEENLINEWIDTH/2,top,MesquiteTable.BETWEENLINEWIDTH,bottom-top);
		}
		else if (table.selectedBetweenRows()) {
			int left = table.getColumnX(table.getStartBetweenColumnSelection()-1);
			int right = table.getColumnX(table.getEndBetweenColumnSelection());
			int top = table.getRowY(table.getStartBetweenRowSelection());
			g.setColor(Color.blue);
			g.fillRect(left,top-MesquiteTable.BETWEENLINEWIDTH/2, right-left,MesquiteTable.BETWEENLINEWIDTH);
			g.setColor(Color.yellow);
			g.drawRect(left,top-MesquiteTable.BETWEENLINEWIDTH/2, right-left,MesquiteTable.BETWEENLINEWIDTH);
		}

	}
	/*...............................................................................................................*/
	public void update(Graphics g){
		paint(g);
		hFloat = -1;
		vFloat = -1;
	}
	public void repaint(){
		checkEditFieldLocation();
		super.repaint();
	}

	/*...............................................................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;

		try {
			paints++;

			int lineY;
			int lineX;
			width = getBounds().width;//this is here to test if width/height should be reset here
			height = getBounds().height;
			if (table.useQuickMode()){
				g.setColor(Color.white);
				g.fillRect(0,0,width, height);
			}

			if (table.frameMatrixCells) {
				int columnHeight = 0;
				for (int r=table.firstRowVisible; (r<table.numRowsTotal) && (columnHeight<height); r++) {
					columnHeight += table.rowHeights[r];
				}
				if  (columnHeight>height)
					columnHeight=height;

				g.setColor(Color.gray);
				lineX = 0;
				for (int c=table.firstColumnVisible; (c<table.numColumnsTotal) && (lineX<width); c++) {
					lineX += table.columnWidths[c];
					g.drawLine(lineX, 0, lineX, columnHeight);//matrixHeight + columnNamesRowHeight
				}
				int rowLength;
				if  (lineX>width)
					rowLength=width;
				else
					rowLength = lineX;

				lineY = 0;
				for (int r=table.firstRowVisible; (r<table.numRowsTotal) && (lineY<height); r++) {
					lineY += table.rowHeights[r];
					g.drawLine(0, lineY, rowLength, lineY);//rowNamesWidth+matrixWidth
				}
			}
			g.setColor(Color.black);
			lineY = 0;
			int oldLineY=lineY;
			int resetWidth = getBounds().width;
			int resetHeight = getBounds().height;

			int numCells = 0;//еее
			FontMetrics fm = g.getFontMetrics(g.getFont());
			Shape clip = g.getClip();
			for (int r=table.firstRowVisible; (r<table.numRowsTotal) && (lineY<resetHeight); r++) {
				lineY += table.rowHeights[r];

				lineX = 0;
				int oldLineX=lineX;
				for (int c=table.firstColumnVisible; (c<table.numColumnsTotal) && (lineX<resetWidth); c++) {
					numCells++;
					lineX += table.columnWidths[c];
					if (c!= returningColumn || r != returningRow){ //don't draw if text about to be returned to cell, and will soon be redrawn anyway
						if (!table.useQuickMode())
							g.setClip(oldLineX,oldLineY,table.columnWidths[c], table.rowHeights[r]);
						boolean selected = table.isCellSelected(c, r) || table.isRowSelected(r)|| table.isColumnSelected(c);
						if (!table.useString(c,r)) {
							table.drawMatrixCell(g, oldLineX,oldLineY,table.columnWidths[c], table.rowHeights[r], c, r, selected);
						}
						else {
							String supplied = table.getMatrixTextForDisplay(c,r);
							Color color = table.getBackgroundColor(c, r, selected);
							if (color == null){
								if (selected) {
									color = Color.white;
								}
								else if (fillColor!=null)
									color = fillColor;
								else if (table.isCellEditable(c, r)){
									if (!table.useQuickMode())
										color = Color.white;
								}
								else
									color = ColorDistribution.uneditable;
							}
							if (color !=null){
								g.setColor(color);
								g.fillRect(oldLineX+1,oldLineY+1,table.columnWidths[c]-1, table.rowHeights[r]-1);
							}
							if (selected)
								GraphicsUtil.fillTransparentSelectionRectangle(g,oldLineX+1,oldLineY+1,table.columnWidths[c]-1, table.rowHeights[r]-1);
							Color textColor;
							if (selected)
								textColor = Color.white;
							else if (table.getCellDimmed(c,r))
								textColor = Color.gray;
							else
								textColor = Color.black;
							g.setColor(textColor);
							table.drawMatrixCellString(g, fm, oldLineX,oldLineY,table.columnWidths[c], table.rowHeights[r], c, r, supplied);
						}
					}
					oldLineX = lineX;
				}
				oldLineY=lineY;
			}
			if (table.getBetweenSelected()) {
				drawBetweenSelection(g);
			}
			if ((endOfLastColumn()>=0) && (endOfLastColumn()<table.matrixWidth)) {
				g.setClip(endOfLastColumn()+1, 0, table.matrixWidth-1, table.matrixHeight-1);
				g.setColor(ColorDistribution.medium[table.colorScheme]);
				g.fillRect(endOfLastColumn()+1, 0, table.matrixWidth-1, table.matrixHeight-1);
			}
			if ((endOfLastRow()>=0) && (endOfLastRow()<table.matrixHeight)) {
				g.setClip(0, endOfLastRow()+1, table.matrixWidth-1, table.matrixHeight-1);
				g.setColor(ColorDistribution.medium[table.colorScheme]);
				g.fillRect(0, endOfLastRow()+1, table.matrixWidth-1, table.matrixHeight-1);
			}
			g.setClip(clip);
			g.setColor(Color.black);

			g.drawRect(0, 0, resetWidth-1, resetHeight-1);
		}
		catch (Throwable e){
			MesquiteMessage.warnProgrammer("Exception or Error in drawing table (Matrix Panel); details in Mesquite log file");
			MesquiteFile.throwableToLog(this, e);
		}

		MesquiteWindow.uncheckDoomed(this);
	}
	public void upArrowPressed(KeyEvent e){ //
		if (getEditing() && (editField.getRow() == 0) && table.isColumnNameEditable(editField.getColumn()))
			table.editColumnNameCell(editField.getColumn());
		else
			super.upArrowPressed(e);
	}

	public void leftArrowPressed(KeyEvent e){
		if ((getEditingAllSelected()) && (editField.getColumn() == 0) && table.isRowNameEditable(editField.getRow()))
			table.editRowNameCell(editField.getRow());
		else
			super.leftArrowPressed(e);
	}

	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public int findColumn(int x, int y) {
		if (x<=0)
			return -1;
		int cx = 0;
		for (int column=table.firstColumnVisible; (column<table.numColumnsTotal) && (cx<x); column++) {
			cx += table.columnWidths[column];
			if (column>= table.numColumnsTotal)
				return -1;
			else if (cx>=x)
				return column;
		}
		return BEYONDMATRIX;//past the last column
	}
	/*@@@...............................................................................................................*/
	/** returns in which row y lies, -1 if above, -2 if below.*/
	public int findRow(int x, int y) {
		if (y<=0)
			return -1;
		int ry = 0;
		for (int row=table.firstRowVisible; (row<table.numRowsTotal) && (ry<y); row++) {
			ry += table.rowHeights[row];
			if (row>= table.numRowsTotal)
				return -1;
			else if (ry>=y)
				return row;
		}

		return BEYONDMATRIX;//past the last row
	}
	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public int findRegionInCellH(int x) {
		int count = 0;
		if (x<=0)
			return 50;  //column<=table.lastColumnVisible &&
		int cx = 0;
		for (int column=table.firstColumnVisible;  (column<table.numColumnsTotal) && (cx<x); column++) {
			count++;
			cx += table.columnWidths[column];
			if (column>= table.numColumnsTotal) {
				return 50;
			}
			else if (cx>=x) {
				int dXR = cx-x; //distance from right edge to 
				int dXL = x - (cx-table.columnWidths[column]); //distance from left edge to 
				return dXL*100/(dXR+dXL);
			}
		}
		return 50;
	}
	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public int findRegionInCellV(int y) {
		if (y<=0)
			return 50;

		int ry = 0;
		for (int row=table.firstRowVisible;  (row<table.numRowsTotal) && (ry<y); row++) {
			ry += table.rowHeights[row];
			if (row>= table.numRowsTotal)
				return 50;
			else if (ry>=y) {
				int dYB = ry-y; //distance from bottom edge to 
				int dYU = y - (ry-table.rowHeights[row]); //distance from left edge to 
				return dYU*100/(dYB+dYU);
			}
		}
		return 50;
	}
	int previousRowDragged = -1;
	int firstRowTouched = -1;
	boolean mouseDownInField = true;
	/*...............................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		table.stopAutoScrollThread();
		if (!(tool instanceof TableTool))
			return;
		int column = findColumn(x,y);
		int row = findRow(x, y);
		firstRowTouched = row;


		mouseDownInField = false;

		int regionInCellH = findRegionInCellH(x);
		int regionInCellV = findRegionInCellV(y);

		//((TableTool)tool).getWorksOnColumnNames();

		if (column>-1 && row > -1 && column<table.numColumnsTotal && row<table.numRowsTotal) {
			table.startAutoScrollThread(this);
			if (((TableTool)tool).getIsBetweenRowColumnTool()) {
				column = findColumnBeforeBetween(x, y);
				row = findRowBeforeBetween(x, y);  //this was findColumnBeforeBetween
			}
			table.cellTouched(column, row, regionInCellH, regionInCellV,modifiers, clickCount);
			if (((TableTool)tool).getEmphasizeRowsOnMouseDown()){
				table.emphasizeRow(-1,firstRowTouched, -1, false, Color.blue);
			}
			mouseDownInField = true;
//			if (((TableTool)tool).getEmphasizeRowsOnMouseDrag()){
//			table.emphasizeRow(-1,row, -1, false, Color.red);
//			}
		}
		else if (column==-2 && ((TableTool)tool).getWorksBeyondLastColumn())
			table.cellTouched(column, row, regionInCellH, regionInCellV,modifiers, clickCount);
		else if (row==-2 && ((TableTool)tool).getWorksBeyondLastRow())
			table.cellTouched(column, row, regionInCellH, regionInCellV,modifiers, clickCount);
		else 
			setWindowAnnotation("", null);


	}
	int counter = 0;
	/*_________________________________________________*/
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		int column = findColumn(x, y);
		int row = findRow(x, y);
		int regionInCellH = findRegionInCellH(x);
		int regionInCellV =  findRegionInCellV(y);

		if (column>-1 && row > -1 && column<table.numColumnsTotal && row<table.numRowsTotal) {
			table.checkForAutoScroll(this,x,y);
			table.cellDrag(column, row, regionInCellH,  regionInCellV,modifiers);
			if (((TableTool)tool).getEmphasizeRowsOnMouseDrag()){
				table.emphasizeRow(previousRowDragged,row, firstRowTouched, false, Color.blue);
				previousRowDragged = row;
			}

		} else if (column>=table.numColumnsTotal || row>=table.numRowsTotal)
			table.checkForAutoScroll(this,x,y);
	}
	/*_________________________________________________*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (!(tool instanceof TableTool))
			return;
		table.stopAutoScrollThread();
		int column = findColumn(x, y);
		int row = findRow(x, y);
		int regionInCellH = findRegionInCellH(x);
		int regionInCellV =  findRegionInCellV(y);

		if (((TableTool)tool).getEmphasizeRowsOnMouseDrag()){
			table.redrawFullRow(previousRowDragged);
			table.redrawFullRow(firstRowTouched);
		}

		if (((TableTool)tool).getEmphasizeRowsOnMouseDown()){
			table.redrawFullRow(firstRowTouched);
		}

		if (((TableTool)tool).acceptsOutsideDrops() || (column>-1 && row > -1 && column<table.numColumnsTotal && row<table.numRowsTotal)) {
			if (((TableTool)tool).getIsBetweenRowColumnTool())
				column = findColumnBeforeBetween(x, y);
			table.cellDropped(column, row, regionInCellH, regionInCellV,modifiers);
		}
		else if (column==-2 && ((TableTool)tool).getWorksBeyondLastColumn())
			table.cellDropped(column, row, regionInCellH, regionInCellV,modifiers);
		else if (row==-2 && ((TableTool)tool).getWorksBeyondLastRow())
			table.cellDropped(column, row, regionInCellH, regionInCellV, modifiers);
		else if (!mouseDownInField && ((TableTool)tool).getDeselectIfOutsideOfCells()) {

			table.offAllEdits();
			table.clickOutside();
			if (table.anythingSelected()) {
				table.deselectAllNotify();
				table.repaintAll();
			}
		}
		mouseDownInField = false;
	}
	/*...............................................................................................................*/
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		table.stopAutoScrollThread();
		if (!table.editingAnything() && !table.singleTableCellSelected()) 
			setWindowAnnotation("", null);
		setCursor(Cursor.getDefaultCursor());
		int column = findColumn(x, y);
		int row = findRow(x, y);
		table.mouseExitedCell(modifiers, column, -1, row, -1, tool);
	}
	/*...............................................................................................................*/
	public void setCurrentCursor(int modifiers, int column, int row, MesquiteTool tool) {
		if (tool == null || !(tool instanceof TableTool))
			setCursor(getDisabledCursor());
		else if (!((TableTool)tool).getWorksOnMatrixPanel()) {
			setCursor(getDisabledCursor());
		}
		else if ((!((TableTool)tool).getWorksBeyondLastRow() && ((row>table.numRowsTotal)||(row<0)))||(!((TableTool)tool).getWorksBeyondLastColumn() && ((column>table.numColumnsTotal)||(column<0)))) 
			setCursor(getDisabledCursor());
		else 
			setCursor(tool.getCursor());
	}
	/*...............................................................................................................*/
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		int column = findColumn(x, y);
		int row = findRow(x, y);
		setCurrentCursor(modifiers, column, row, tool);
		table.mouseInCell(modifiers, column,-1, row, -1,tool);
	}
	/*...............................................................................................................*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int column = findColumn(x, y);
		int row = findRow(x, y);
		setCurrentCursor(modifiers, column,  row, tool);
		table.mouseInCell(modifiers, column,-1,  row, -1, tool);

	}
	public void focusGained(FocusEvent arg0) {
	}
	public void focusLost(FocusEvent arg0) {
		if (table!=null) table.stopAutoScrollThread();
	}


}

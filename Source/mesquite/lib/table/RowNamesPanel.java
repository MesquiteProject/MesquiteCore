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

import java.io.*;

/* ======================================================================== */
/** A panel for row headings in a MesquiteTable.*/
public class RowNamesPanel extends EditorPanel implements FocusListener  {
	MesquiteTable table;
	public int width,  height;

	public RowNamesPanel (MesquiteTable table , int w, int h) {
		super(table);
		this.table=table;
		addFocusListener(this);
		//setBackground(ColorDistribution.medium[table.colorScheme]);
		setAutoscrollDirection(AUTOSCROLLVERTICAL);
		setBackground(Color.white);
		setTableUnitSize(w, h);
	}
	public void setTableUnitSize (int w, int h) {
		this.width=w;
		this.height=h;
		setSize(w, height);
	}
	public void setWidth (int w) {
		this.width=w;
		setSize(w, height);
	}
	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public int findColumn(int x, int y) {
		return -1; //if left of grabbers?
	}
	/*@@@...............................................................................................................*/
	/** returns in which row y lies, -1 if above all rows, -2 if below all rows.*/
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

		return -2;//past the last row
	}
	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public int findRegionInCellH(int x) {
		if (x<=0)
			return 50;
		return (x-startOfColumn(-1))*100/(columnWidth(-1)- startOfColumn(-1));
	}
	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public int findRegionInCellV(int y) {
		if (y<=0)
			return 50;

		int ry = 0;
		for (int row=table.firstRowVisible; (row<table.numRowsTotal) && (ry<y); row++) {
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
	public int startOfColumn(int column){
		return table.getColumnGrabberWidth()-2;
	}
	public int firstColumnVisible(){
		return -1;
	}
	public int numColumnsVisible(){
		return 1;
	}
	public int columnWidth(int column) { //todo: why does this not subtract grabbers, but ColumnNames does?
		return width;
	}
	public void textReturned(int column, int row, String text){
		table.returnedRowNameText(row, text);
	}
	public String getText(int column, int row){
		return table.getRowNameText(row);
	}
	public void deselectCell(int column,int row){
		table.deselectRowName(row);
	}
	public void redrawCell(int column, int row){
		Graphics g = getGraphics();
		if (g!=null) {
			redrawName(g, row);
			g.dispose();
		}
	}
	public void redrawName(Graphics g, int row) {
		int top = table.getFirstRowVisible();

		if (row<top) //TODO: should also fail to draw if to big
			return;  
		if (row == returningRow){
			return; //don't draw if text about to be returned to cell, and will soon be redrawn anyway
		}
		int leftSide = startOfColumn(-1);
		int topSide = startOfRow(row);
		if (topSide>getBounds().height || topSide+rowHeight(row)<0)
			return;

		Shape clip = g.getClip();
		g.setClip(0,topSide,columnWidth(-1), rowHeight(row));

		prepareCell(g, 1,topSide+1,columnWidth(-1), rowHeight(row)-2, table.focusRow == row,  table.isRowNameSelected(row) || table.isRowSelected(row), table.getCellDimmed(-1, row), table.isRowNameEditable(row));

		g.setClip(0,0, getBounds().width, getBounds().height);


		if (table.frameRowNames) {
			Color cg = g.getColor();
			g.setColor(Color.gray);
			g.drawLine(0, topSide+rowHeight(row), width, topSide+rowHeight(row));
			g.setColor(cg);
		}

		Font fnt = null;
		boolean doFocus = table.focusRow == row && table.boldFont !=null;
		if (doFocus){
			fnt = g.getFont();
			g.setFont(table.boldFont);
		}

		Color oldColor = g.getColor();
		if (table.showRowGrabbers) {
			if (table.showRowNumbers) 
				table.drawRowColumnNumber(g,row,true,0,topSide+1, table.getRowGrabberWidth(),rowHeight(row)-2);
			else
				table.drawRowColumnNumberBox(g,row,true,0,topSide+1, table.getRowGrabberWidth(),rowHeight(row)-2);
			g.setClip(0+table.getRowGrabberWidth(),topSide, width-table.getRowGrabberWidth(),rowHeight(row));
			table.setRowNameColor(g, row);
			table.drawRowNameCell(g, 0+table.getRowGrabberWidth(),topSide, width-table.getRowGrabberWidth(),rowHeight(row), row);
		}
		else {
			g.setClip(0,topSide, width,rowHeight(row));
			table.setRowNameColor(g, row);
			table.drawRowNameCell(g, 0,topSide, width,rowHeight(row), row);
		}
		g.setColor(oldColor);
		if (doFocus && fnt !=null){
			g.setFont(fnt);
		}
		g.setClip(0,0, getBounds().width, getBounds().height);

		g.setColor(Color.black);
		if (table.getDropDown(-1, row)) {
			int offset = 0;
			if (table.showRowGrabbers)
				offset = table.getRowGrabberWidth();

			dropDownTriangle.translate(1 + offset,topSide + 1);
			g.setColor(Color.white);
			g.drawPolygon(dropDownTriangle);
			g.setColor(Color.black);
			g.fillPolygon(dropDownTriangle);
			dropDownTriangle.translate(-(1 + offset),-(topSide + 1));
		}
		g.setClip(clip);
		g.drawLine(width-1, 0, width-1, height);
	}

	public void repaint(){
		checkEditFieldLocation();
		super.repaint();
	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		try {
			table.checkResetFont(g);

			int lineY = 0;
			int oldLineY=lineY;
			int resetWidth = getBounds().width;
			int resetHeight = getBounds().height;
			width = resetWidth;//this is here to test if width/height should be reset here
			height = resetHeight;
			Shape clip = g.getClip();

			for (int r=table.firstRowVisible; (r<table.numRowsTotal) && (lineY<height); r++) {
				redrawName(g, r);
			}

			g.setClip(0,0, getBounds().width, getBounds().height);
			if (false && getEditing()) {
				TextField edit = getEditField();
				if (edit!= null)
					edit.repaint();
			}
			if ((endOfLastRow()>=0) && (endOfLastRow()<table.matrixHeight)) {
				g.setColor(ColorDistribution.light[table.colorScheme]); //ggray
				g.fillRect(0, endOfLastRow()+1, getBounds().width, getBounds().height);
			}
			g.setColor(Color.black);
			if (table.frameRowNames)
				g.drawRect(0, 0, width, height-1);
			g.drawLine(width-1, 0, width-1, height);
			g.setClip(clip);
		}
		catch (Throwable e){
			MesquiteMessage.warnProgrammer("Exception or Error in drawing table (RNP); details in Mesquite log file");
			MesquiteFile.throwableToLog(this, e);
		}
		MesquiteWindow.uncheckDoomed(this);

	}
	public void print(Graphics g) {
		int lineY = 0;
		int oldLineY=lineY;
		Shape clip = g.getClip();

		g.setClip(0,0, getBounds().width, getBounds().height);
		for (int r=0; (r<table.numRowsTotal); r++) {
			lineY += table.rowHeights[r];
			g.setClip(0,oldLineY, width, table.rowHeights[r]);
			g.setColor(Color.black);
			table.drawRowNameCell(g, 0,startOfRow(r), width,rowHeight(r), r);
			//table.drawRowNameCell(g, 20,oldLineY, width,table.rowHeights[r], r);

			g.setColor(Color.black);
			oldLineY=lineY;
		}
		g.setClip(0,0, width, table.getTotalRowHeight());
		g.setColor(Color.black);
		g.drawLine(width-1, 0, width-1, table.getTotalRowHeight());

		g.setClip(clip);
	}
	public void OLDprint(Graphics g) {
		int lineY = 0;
		int oldLineY=lineY;
		int resetWidth = getBounds().width;
		int resetHeight = getBounds().height;
		width = resetWidth;//this is here to test if width/height should be reset here
		height = resetHeight;
		Shape clip = g.getClip();
		for (int r=table.firstRowVisible; (r<table.numRowsTotal) && (lineY<height); r++) {
			lineY += table.rowHeights[r];
			/*
			if (table.frameRowNames) {
				g.setColor(Color.gray);
				g.drawLine(0, lineY, width, lineY);
			}
			 */
			g.setColor(Color.black);
			g.setClip(0,oldLineY, width,table.rowHeights[r]);
			table.drawRowNameCell(g, 0,oldLineY, width,table.rowHeights[r], r);
			g.setClip(clip);

			g.setColor(Color.black);
			oldLineY=lineY;
		}
		g.setClip(0,0, getBounds().width, getBounds().height);
		g.setColor(Color.black);
		g.drawLine(width-1, 0, width-1, height);

		g.setClip(clip);
	}
	/*...............................................................................................................*/
	int touchY = -1;
	int lastY=-1;
	int touchRow;
	int previousRowDragged = -1;
	/*...............................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		table.stopAutoScrollThread();
		if (!(tool instanceof TableTool))
			return;
		touchY=-1;
		touchRow=-1;
		int possibleTouch = findRow(x, y);
		int regionInCellH = findRegionInCellH(x);
		int regionInCellV =findRegionInCellV(y);
		boolean isArrowEquivalent = ((TableTool)tool).isArrowKeyOnRow(x,table);

		if (possibleTouch>=0 && possibleTouch<table.numRowsTotal) {
			table.startAutoScrollThread(this);
			if (tool != null && isArrowEquivalent && table.getUserMoveRow() && table.isRowSelected(possibleTouch) && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.commandOrControlKeyDown(modifiers)) {
				touchY=y;
				lastY = y;
				touchRow=possibleTouch;
				table.shimmerHorizontalOn(touchY);
			}
			else if ((table.showRowGrabbers) && (x<table.getRowGrabberWidth())) {
				if (((TableTool)tool).getIsBetweenRowColumnTool() && !isArrowEquivalent)
					possibleTouch = findRowBeforeBetween(x, y);
				table.rowTouched(isArrowEquivalent, possibleTouch,regionInCellH, regionInCellV,modifiers);
				if (tool != null && isArrowEquivalent && table.getUserMoveRow() && table.isRowSelected(possibleTouch) && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.commandOrControlKeyDown(modifiers)) {
					touchY=y;
					lastY = MesquiteInteger.unassigned;;
					touchRow=possibleTouch;
					//table.shimmerHorizontalOn(touchY);
				}

			}
			else if (isArrowEquivalent) {
				table.rowNameTouched(possibleTouch,regionInCellH, regionInCellV, modifiers,clickCount);
			}
			else if (tool!=null && ((TableTool)tool).getWorksOnRowNames()) {
				if (((TableTool)tool).getIsBetweenRowColumnTool())
					possibleTouch = findRowBeforeBetween(x, y);
				touchY=y;
				lastY = y;
				touchRow=possibleTouch;
				table.rowNameTouched(possibleTouch,regionInCellH, regionInCellV, modifiers,clickCount);
			}
		}
		else if (possibleTouch==-2 && ((TableTool)tool).getWorksBeyondLastRow())
			table.rowTouched(isArrowEquivalent,possibleTouch,regionInCellH, regionInCellV,modifiers);
		else if (tool != null && tool.isArrowTool()){
			table.offAllEdits();
			if (table.anythingSelected()) {
				table.deselectAllNotify();
				table.repaintAll();
			}
		}
	}
	/*...............................................................................................................*/
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		if (touchRow>=0 && tool != null)
			if (((TableTool)tool).isArrowKeyOnRow(x,table)) {
				if (table.getUserAdjustColumn()==MesquiteTable.RESIZE) {
					table.shimmerHorizontalOff(lastY);
					table.shimmerHorizontalOn(y);
					lastY=y;
				}
				else if (table.getUserMoveColumn()) {
					table.shimmerHorizontalOff(lastY);
					table.shimmerHorizontalOn(y);
					lastY=y;
				}
				table.checkForAutoScroll(this, MesquiteInteger.unassigned,y);   // pass unassigned in x so it doesn't do anything in that direction
			}
			else if (((TableTool)tool).getWorksOnRowNames()) {
				table.checkForAutoScroll(this, MesquiteInteger.unassigned,y);   // pass unassigned in x so it doesn't do anything in that direction
				int dragRow = findRow(x, y);
				int regionInCellH = findRegionInCellH(x);
				int regionInCellV =findRegionInCellV(y);
				((TableTool)tool).cellDrag(-1,dragRow,regionInCellH,regionInCellV,modifiers);
				if (((TableTool)tool).getEmphasizeRowsOnMouseDrag()){
					table.emphasizeRow(previousRowDragged,dragRow, touchRow, false, Color.blue);
					previousRowDragged = dragRow;
				}
			}
	}
	/*...............................................................................................................*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		table.stopAutoScrollThread();
		if (touchRow>=0 && tool != null)
			if (((TableTool)tool).isArrowKeyOnRow(x,table)) {
				if (!table.anyRowSelected()) {

					if (table.getUserAdjustRow()==MesquiteTable.RESIZE) {
						/*table.shimmerVerticalOff(lastX);
						int newRH = table.rowHeights[touchRow] + x-touchX;
						if (newRH > 16) {
							table.setRowHeight(touchRow, newRH);
							table.rowHeightsAdjusted.setBit(touchRow);
							table.repaintAll();
						}*/
					}
					if (table.getUserMoveRow())
						table.shimmerHorizontalOff(lastY);
				}
				/*@@@*/
				else {
					if (table.getUserMoveRow()) {
						table.shimmerHorizontalOff(lastY);
						int dropRow = findRowBeforeBetween(x, y);
						if (dropRow == -2)
							dropRow = table.getNumRows();
						if (dropRow != touchRow && (dropRow != touchRow-1) && !table.isRowSelected(dropRow)) { //don't move dropped on row included in selection {
							table.selectedRowsDropped(dropRow);
						}
					}
					else if (table.getUserAdjustRow()==MesquiteTable.RESIZE)
						;//table.shimmerVerticalOff(lastX);
				}
			}
			else if (((TableTool)tool).getWorksOnRowNames()) {
				int dropRow = findRow(x, y);
				int regionInCellH = findRegionInCellH(x);
				int regionInCellV =findRegionInCellV(y);
				((TableTool)tool).cellDropped(-1,dropRow,regionInCellH,regionInCellV,modifiers);
			}

	}

	/*...............................................................................................................*/
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		table.stopAutoScrollThread();
		if (!(table.editingAnything() || table.singleTableCellSelected()) && tool != null && tool.isArrowTool())
			setWindowAnnotation("", null);
		setCursor(Cursor.getDefaultCursor());
		int row = findRow(x, y);
		table.mouseExitedCell(modifiers, -1, -1, row, -1, tool);
	}
	/*...............................................................................................................*/
	public void setCurrentCursor(int modifiers, int x, int row, MesquiteTool tool) {
		if (tool == null || !(tool instanceof TableTool))
			setCursor(getDisabledCursor());
		else if (row>=0 && row<table.numRowsTotal) {
			if (((TableTool)tool).isArrowKeyOnRow(x,table)) {
				setCursor(table.getHandCursor());
				if (!(table.getUserMoveRow() && table.isRowSelected(row) && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.controlKeyDown(modifiers))) {
					if (!(table.editingAnything() || table.singleTableCellSelected())) {
						String s = table.getRowComment(row);
						if (!StringUtil.blank(s))   
							setWindowAnnotation(s, "Footnote above refers to " + table.getRowNameText(row));
						else
							setWindowAnnotation("", null);

					}
				}
			}
			else if (((TableTool)tool).getWorksOnRowNames()) 
				setCursor(tool.getCursor());
			else
				setCursor(getDisabledCursor());
		}
		else if (((TableTool)tool).getWorksBeyondLastRow() && (row==-2))
			setCursor(tool.getCursor());
		else
			setCursor(getDisabledCursor());
	}
	/*...............................................................................................................*/
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (table == null)
			return;
		int row = findRow(x, y);
		setCurrentCursor(modifiers, x, row, tool);
		table.mouseInCell(modifiers, -1, -1, row, -1, tool);
	}
	/*...............................................................................................................*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int row = findRow(x, y);
		setCurrentCursor(modifiers, x, row, tool);
		table.mouseInCell(modifiers, -1, -1, row, -1, tool);
		table.checkForAutoScroll(this, MesquiteInteger.unassigned,y);   // pass unassigned in x so it doesn't do anything in that direction
	}
	/*...............................................................................................................*/


	public void tabPressed(KeyEvent e){
		if (!getEditing())
			return;
		if (table.getCellsEditable()){
			e.consume();
			table.editMatrixCell(0, editField.getRow());
		}
	}
	public void focusGained(FocusEvent arg0) {
	}
	public void focusLost(FocusEvent arg0) {
		if (table!=null) table.stopAutoScrollThread();
	}
}

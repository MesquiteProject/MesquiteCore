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

import java.io.*;

/* ======================================================================== */
/** A panel for column headings for use in MesquiteTable*/
public class ColumnNamesPanel extends EditorPanel implements FocusListener {
	MesquiteTable table;
	public int width,  height;
	int touchX = -1;
	int touchY = -1;
	int touchColumn = -1;
	int shimmerX = -1;
	int shimmerY = -1;

	int origShimmer = -1;
	int numRows = 1;
	int numInfoStrips = 0;
	static int defaultRowH = 20;
	int rowH=defaultRowH;
	int infoStripRowH = defaultRowH;
	boolean diagonal = false;
	TextRotator textRotator;
	Polygon diagonalMask, diagonalColumnMask;
	int diagonalSize = 1000;
	int diagonalHeight = 80;
	double diagonalAngle = -Math.PI/4;

	public ColumnNamesPanel (MesquiteTable table , int w, int h) {
		super(table);
		this.table=table;
		this.width=w;
		this.height=h;
		addFocusListener(this);
		setAutoscrollDirection(AUTOSCROLLHORIZONTAL);
		textRotator = new TextRotator(1);
		textRotator.assignBackground(null);
		//setBackground(ColorDistribution.medium[table.colorScheme]);
		setBackground(Color.white);
		setCursor(table.getHandCursor());
		setSize(w, h);
	}
	public void setTableUnitSize (int w, int h) {
		this.width=w;
		this.height=h;
		setSize(width, h);
	}

	public void setDiagonal(boolean d){
		diagonal = d;
		if (d)
			table.setColumnNamesRowHeight(diagonalHeight);
		else
			table.setColumnNamesRowHeight(defaultRowH);

		if (d) {
			textRotator.assignBackground(null);
			resetDiagonalHeight();
		}
		table.resetComponentSizes();
	}
	public void setDiagonalHeight(int dh){
		 diagonalHeight = dh;
		 setDiagonal(diagonal);
	}
	public int getDiagonalHeight(){
		return diagonalHeight;
	}
	void resetDiagonalMask(int width){
		int x= (int)(Math.cos(diagonalAngle)*diagonalHeight);
		int y = (int)(Math.sin(diagonalAngle)*diagonalHeight);
		double shrunk = (-diagonalHeight)*1.0/y;
		y =  (int)(shrunk*y);
		x = (int)(shrunk*x);
		diagonalMask.xpoints[2] = x + width; //2
		diagonalMask.xpoints[3] = width; //3
	}
	void resetDiagonalHeight(){
		int x= (int)(Math.cos(diagonalAngle)*diagonalHeight);
		int y = (int)(Math.sin(diagonalAngle)*diagonalHeight);
		double shrunk = (-diagonalHeight)*1.0/y;
		y =  (int)(shrunk*y);
		x = (int)(shrunk*x);
		diagonalMask = new Polygon();
		diagonalMask.npoints=0;
		diagonalMask.addPoint(0,0); //0
		diagonalMask.addPoint(x,y); //1
		diagonalMask.addPoint(x+diagonalSize,y); //2
		diagonalMask.addPoint(diagonalSize,0); //3
		diagonalMask.addPoint(0,0);
		diagonalMask.npoints=5;
		int xColumn= (int)(Math.cos(diagonalAngle)*diagonalHeight);
		int yColumn = (int)(Math.sin(diagonalAngle)*diagonalHeight);
		shrunk = (-diagonalHeight)*1.0/yColumn;
		yColumn =  (int)(shrunk*yColumn);
		xColumn = (int)(shrunk*xColumn);
		diagonalColumnMask = new Polygon();
		diagonalColumnMask.npoints=0;
		diagonalColumnMask.addPoint(0,0); //0
		diagonalColumnMask.addPoint(xColumn,yColumn); //1
		diagonalColumnMask.addPoint(xColumn,yColumn); //2 x updated for each column
		diagonalColumnMask.addPoint(0 , 0); //3 x updated for each column
		diagonalColumnMask.addPoint(0,0); //4
		diagonalColumnMask.npoints=5;
	}
	public boolean isDiagonal(){
		return diagonal;
	}
	/*...............................................................................................................*/
	/** Gets the height of the columnNames Panel.*/
	public int calcColumnNamesHeight() {
		return rowHeight(-1) + numInfoStrips*infoStripRowHeight(0) + table.getColumnGrabberWidth();
	}
	public void setHeight () {
		this.height=rowHeight(-1) + numInfoStrips*infoStripRowHeight(0) + table.getColumnGrabberWidth();
		setSize(width, height);
	}
	public int getNumRows () {
		return numRows;
	}
	public int getNumInfoStrips () {
		return numInfoStrips;
	}
	public void setNumInfoStrips (int num) {
		numInfoStrips = num;
//		numRows = numInfoStrips+1;
	}
	public void appendInfoStrip () {
		numInfoStrips++;
//		numRows++;
	}
	public void decrementInfoStrips () {
		numInfoStrips--;
//		numRows--;
	}

	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public int findRegionInCellH(int x) {
		if (x<=0)
			return 50;
		int cx = 0;
		for (int column=table.firstColumnVisible; (column<table.numColumnsTotal) && (cx<x); column++) {
			cx += table.columnWidths[column];
			if (column>= table.numColumnsTotal)
				return 50;
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
			return 50;  //???
		return (y-startOfRow(-1))*100/(rowHeight(-1));
	}
	public int startOfRow(int row){
		if (table.showColumnGrabbers)
			return (table.getColumnGrabberWidth());
		else
			return 0;
	}
	public int firstRowVisible(){
		return -1;
	}
	public int numRowsVisible(){
		return 1;
	}
	public int calcRowHeight(int row) {
		height=rowHeight(-1)*numRows + table.getColumnGrabberWidth();
		if (table.showColumnGrabbers)
			return (height -table.getColumnGrabberWidth())/getNumRows();
		else
			return (height)/getNumRows();
	}
	public int rowHeight(int num) {
		return rowH;
	}
	public void setRowHeight(int h) {
		rowH = h;
	}
	public int infoStripRowHeight(int num) {
		return infoStripRowH;
	}
	public int nameRowTop() {
		return (startOfRow(-1));
	}
	public int nameRowBottom() {
		return (startOfRow(-1) + nameRowHeight());
	}
	public int extraRowTop(int extraRow) {
		return (nameRowBottom() + infoStripRowHeight(0)*(extraRow));
	}
	public int nameRowHeight() {
		return (rowHeight(-1));
	}
	public int lastRowBottom() {
		return (startOfRow(-1) + rowHeight(-1) + numInfoStrips*infoStripRowHeight(0));
	}
	public void textReturned(int column, int row, String text){
		table.returnedColumnNameText(column, text);
	}
	public String getText(int column, int row){
		return table.getColumnNameText(column);
	}
	public void deselectCell(int column,int row){
		table.deselectColumnName(column);
	}
	public void redrawCell(int column, int row){
		Graphics g = getGraphics();
		if (g!=null) {
			redrawName(g, column);
			g.dispose();
		}
	}
	int leftmostNumber = 0;
	int rightmostNumber = 0;
	/*...............................................................................................................*/
	public void redrawName(Graphics g, int column) {
		int left = table.getFirstColumnVisible();
		if (column<left) //TODO: should also fail to draw if to big
			return;  
		if (column == returningColumn){
			return; //don't draw if text about to be returned to cell, and will soon be redrawn anyway
		}
		int leftSide = startOfColumn(column);
		if (leftSide>getBounds().width || leftSide+columnWidth(column)<0)
			return;
		int topSide = nameRowTop();
		int botSide = nameRowBottom();
		Shape clip = g.getClip();
		if (diagonal){
			Color cg = g.getColor();
			//g.setClip(0,0, getWidth(), getHeight());
			prepareCell(g, column, startOfColumn(column)+1, topSide,columnWidth(column), botSide, table.focusColumn == column, table.isColumnNameSelected(column) || table.isColumnSelected(column), table.getCellDimmed(column, -1), table.isColumnNameEditable(column));

			String name = table.getColumnNameTextForDisplay(column);
			if (name == null)
				name = "";
			int rRight = endOfColumn(column);
			int rLeft = startOfColumn(column);
			int offX= (int)(Math.cos(diagonalAngle)*1000);
			int offY =(int)(Math.sin(diagonalAngle)*1000);
			if (offY < -diagonalHeight) {
				double shrunk = (-diagonalHeight)*1.0/offY;
				offY =  (int)(shrunk*offY);
				offX = (int)(shrunk*offX);
			}
			if( column == 0)
				leftmostNumber = offX + rLeft;
			else if (column == table.firstColumnVisible + table.numColumnsVisible-1)
				rightmostNumber = rRight + offX;
			textRotator.drawFreeRotatedText(name, g,(rLeft+rRight)/2, botSide-2, diagonalAngle, null, true, null);
			if (table.frameColumnNames){
				g.setColor(Color.gray);
				g.drawLine(rRight,botSide-1,rRight+offX, botSide-1 + offY);
				if (column == 0)
					g.drawLine(rLeft,botSide-1,rLeft+offX, botSide-1 + offY);
			}
			leftSide += offX;
			botSide += offY;
			g.setColor(cg);
			Font fnt= null;
			boolean doFocus = table.focusColumn == column && table.boldFont !=null;
			if (doFocus){
				fnt = g.getFont();
				g.setFont(table.boldFont);
			}
			
			if (table.showColumnGrabbers) {
				if (table.showColumnNumbers) {

					table.drawRowColumnNumber(g,column,false,leftSide+1,0, columnWidth(column)-2, table.getColumnGrabberWidth());
				}
				else
					table.drawRowColumnNumberBox(g,column, false,leftSide+1,0, columnWidth(column)-2, table.getColumnGrabberWidth());
			}
			if (doFocus && fnt !=null){
				g.setFont(fnt);
			}
			g.setClip(0,0, getBounds().width, botSide);


			g.setColor(Color.black);
			if (table.getDropDown(column, -1)) {
				int offset = 0;
				if (table.showColumnGrabbers)
					offset = table.getColumnGrabberWidth();

				dropDownTriangle.translate(leftSide - 8 + columnWidth(column),1 + offset);
				g.setColor(Color.white);
				g.drawPolygon(dropDownTriangle);
				g.setColor(Color.black);
				g.fillPolygon(dropDownTriangle);
				dropDownTriangle.translate(-(leftSide - 8 + columnWidth(column)),-(1 + offset));
			}
//			g.drawLine(0, botSide-1, width, botSide-1);
			g.setClip(clip);
		}
		else {
			g.setClip(leftSide,0,columnWidth(column), botSide);

			prepareCell(g,column, -1, leftSide+1, 1,columnWidth(column), botSide, table.focusColumn == column, table.isColumnNameSelected(column) || table.isColumnSelected(column), table.getCellDimmed(column, -1), table.isColumnNameEditable(column));

			g.setClip(0,0, getBounds().width, botSide);
//			g.setClip(0,0, getBounds().width, getBounds().height);

			if (table.frameColumnNames) {
				Color cg = g.getColor();
				g.setColor(Color.gray); 
				g.drawLine(leftSide+columnWidth(column), 0, leftSide+columnWidth(column), botSide);
				g.setColor(cg);
			}
			Font fnt= null;
			boolean doFocus = table.focusColumn == column && table.boldFont !=null;
			if (doFocus){
				fnt = g.getFont();
				g.setFont(table.boldFont);
			}
			if (table.showColumnGrabbers) {
				if (table.showColumnNumbers) {

					table.drawRowColumnNumber(g,column,false,leftSide+1,0, columnWidth(column)-2, table.getColumnGrabberWidth());
				}
				else
					table.drawRowColumnNumberBox(g,column, false,leftSide+1,0, columnWidth(column)-2, table.getColumnGrabberWidth());
				g.setClip( leftSide,nameRowTop(), columnWidth(column), botSide);
				table.drawColumnNameCell(g, leftSide,nameRowTop(), columnWidth(column), nameRowHeight(), column);

			}
			else {
				g.setClip( leftSide,nameRowTop(), columnWidth(column), botSide-1);
				table.drawColumnNameCell(g, leftSide,nameRowTop(), columnWidth(column), nameRowHeight(), column);
			}
			if (doFocus && fnt !=null){
				g.setFont(fnt);
			}
			g.setClip(0,0, getBounds().width, botSide);


			g.setColor(Color.black);
			if (table.getDropDown(column, -1)) {
				int offset = 0;
				if (table.showColumnGrabbers)
					offset = table.getColumnGrabberWidth();

				dropDownTriangle.translate(leftSide - 8 + columnWidth(column),1 + offset);
				g.setColor(Color.white);
				g.drawPolygon(dropDownTriangle);
				g.setColor(Color.black);
				g.fillPolygon(dropDownTriangle);
				dropDownTriangle.translate(-(leftSide - 8 + columnWidth(column)),-(1 + offset));
			}
			g.setClip(clip);
		g.drawLine(0, botSide-1, width, botSide-1);
		}
		g.setClip(null);
	}
	public void offEdit(){  
		super.offEdit();
		repaint();
	}
	protected void prepareCell(Graphics g, int column, int x, int y, int w, int h, boolean focused, boolean selected, boolean dimmed, boolean editable){
		Color color = table.getColumnNameFillColor(column, fillColor, focused, selected, dimmed, editable);
		g.setColor(color);
		fillCell(g, x, y, w, h, selected);

		Color textColor;
		float[] hsb = new float[3];
		hsb[0]=hsb[1]=hsb[2]= 1;
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		textColor = ColorDistribution.getContrasting(selected, color, hsb, Color.white, Color.black);
		g.setColor(textColor);

	}
	/*...............................................................................................................*/
	public void fillCell(Graphics g, int x, int y, int w, int h, boolean selected){
		if (diagonal){
			if (diagonalColumnMask == null)
				return;
			diagonalColumnMask.translate(x, h);
			diagonalColumnMask.xpoints[2] += w;
			diagonalColumnMask.xpoints[3] += w;
			g.fillPolygon(diagonalColumnMask);
			if (selected)
				GraphicsUtil.fillTransparentSelectionPolygon(g,diagonalColumnMask);
			diagonalColumnMask.xpoints[2] -= w;
			diagonalColumnMask.xpoints[3] -= w;
			diagonalColumnMask.translate(-x, -h);
		}
		else {
			g.fillRect(x+1,y+1,w-1,h-1);
			if (selected)
				GraphicsUtil.fillTransparentSelectionRectangle(g,x+1,y+1,w-1,h-1);
		}
	}
	/*...............................................................................................................*/
	public void repaint(){
		checkEditFieldLocation();
		super.repaint();
	}

	/*...............................................................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		try {
			int lineX = 0;
			int oldLineX=lineX;
			int resetWidth = getBounds().width;
			int resetHeight = getBounds().height;
			width = resetWidth;//this is here to test if width/height should be reset here
			height = resetHeight;
			Shape clip = g.getClip();
			g.setClip(null);
			table.resetNumColumnsVisible();
			for (int c=table.firstColumnVisible; (c<=table.lastColumnVisible+2 && c<table.numColumnsTotal) && (lineX<width); c++) { // or lineX+table.columnWidths[c]
				redrawName(g, c);
			}
			g.setClip(null);
			if (diagonal) {
				if (diagonalMask == null)
					resetDiagonalHeight();
				int rLeft = startOfColumn(0);
				int rRight = endOfColumn(table.numColumnsTotal-1);
//				diagonalMask.translate(-rLeft -diagonalSize, height);
				int diagonalMaskShift = nameRowBottom();
				diagonalMask.translate(-rLeft -diagonalSize, diagonalMaskShift);  //shift it 
				
				g.setColor(ColorTheme.getContentBackgroundPale()); //ggray
				
				g.fillPolygon(diagonalMask);
				diagonalMask.translate(rLeft +diagonalSize, -diagonalMaskShift);
				diagonalMask.translate(rRight, diagonalMaskShift);
				g.fillPolygon(diagonalMask);
				diagonalMask.translate(-rRight, -diagonalMaskShift);  // move it back to where it was

				g.fillRect(rRight, nameRowBottom(), width, height);  // fill region to the left of the column numbers
				
				g.fillRect(0, 0, leftmostNumber-1, startOfRow(-1));  // fill region to the left of the column numbers
				//g.fillRect(0, 0, leftmostNumber-1, height - rowHeight(-1));

				g.fillRect(rightmostNumber+1, 0, width, startOfRow(-1));    // fill region to the right of the column numbers
				g.setColor(ColorTheme.getContentEdgeDark());
				g.drawLine(0, height-1, width, height-1);
				g.setClip(clip);
				table.drawColumnNamesPanelExtras(g, 0,nameRowBottom(),endOfLastColumn(), getBounds().height);
				return;
			}
			g.setClip(0,0, getBounds().width, getBounds().height);

			if ((endOfLastColumn()>=0) && (endOfLastColumn()<table.matrixWidth)) {
				g.setColor(ColorTheme.getContentBackgroundPale()); //ggray
				g.fillRect(endOfLastColumn(), 0, getBounds().width, getBounds().height);
			}

			table.drawColumnNamesPanelExtras(g, 0,nameRowBottom(),endOfLastColumn(), getBounds().height);
			if (lastRowBottom()<height) {
				g.setColor(ColorTheme.getContentBackgroundPale()); //ggray
				g.fillRect(0, lastRowBottom(), endOfLastColumn(), getBounds().height);
			}
	//		g.setColor(Color.black);
			g.setColor(ColorTheme.getContentEdgeDark());  //used to be light
			if (table.frameColumnNames)
				g.drawRect(0, 0, width-1, height);
			g.drawLine(0, height-1, width, height-1);
			g.setClip(clip);
			width = resetWidth;
		}
		catch (Throwable e){
			MesquiteMessage.warnProgrammer("Exception or Error in drawing table (CNP); details in Mesquite log file. d " + diagonalMask + " t " + table);
			MesquiteFile.throwableToLog(this, e);
		}

		MesquiteWindow.uncheckDoomed(this);
	}
	/*...............................................................................................................*/
	public void print(Graphics g) {
		int lineX = 0;
		int oldLineX=lineX;
		Shape clip = g.getClip();
		for (int c=0; (c<table.numColumnsTotal); c++) { 
			lineX += table.columnWidths[c];
			g.setColor(Color.black);
			//g.setClip( oldLineX,0, table.columnWidths[c], height);
			table.drawColumnNameCell(g, oldLineX,0, table.columnWidths[c], height, c);
			g.setColor(Color.black);

			oldLineX = lineX;
		}
		g.setColor(Color.black);
		g.setClip(0,0, table.getTotalColumnWidth(), height);
		g.drawLine(0, height-1,table.getTotalColumnWidth(), height-1);
		g.setClip(clip);
	}
	/*...............................................................................................................*/
	public void enterPressed(KeyEvent e){
		if (!getEditing())
			return;
		if (table.getCellsEditable())
			table.editMatrixCell(editField.getColumn(), 0);
	}
	/*...............................................................................................................*/
	public void downArrowPressed(KeyEvent e){
		if (getEditing())
			enterPressed(e);
		//here should move selection into matrix
	}



	/*...............................................................................................................*/
	/*@@@...............................................................................................................*/
	/** Returns in which column x lies, -1 if to left, -2 if to right. 
	Differs from findColumn in that*/
	public int findHalfColumn(int x, int y) {
		if (x<=0)
			return -1;
		if (diagonal){
			int cx = 0;
			int left = 0;
			int right = table.columnWidths[table.firstColumnVisible]/2;
			if (betweenDiagonal(x, y, -500, left))
				return -1;
			if (betweenDiagonal(x, y, left, right))
				return table.firstColumnVisible-1;
			int prevWidth = right*2;

			for (int column=table.firstColumnVisible; (column<table.numColumnsTotal) && (cx<x); column++) {
				cx += table.columnWidths[column]; //centre point
				left = cx - prevWidth/2;
				if (column < table.numColumnsTotal-1)
					prevWidth = table.columnWidths[column+1];
				else
					prevWidth = 0;
				right = cx + prevWidth/2;
				if (betweenDiagonal(x, y, left-1, right))
					return column;
				if (cx>=x)
					return -3;  //oops, something wrong
				//if (column+1 == table.numColumnsTotal)
				//	return -1;
			}
			return -2; //past the last column
		}
		int cx = 0;
		for (int column=table.firstColumnVisible; (column<table.numColumnsTotal) && (cx<x); column++) {
			cx += table.columnWidths[column];
			if (column>= table.numColumnsTotal)
				return -1;
			if (cx>=x)
				if (x+(table.columnWidths[column]/2) > cx) // then we are in the right half of the column
					return column;
				else
					return column - 1;
		}
		return -2; //past the last column
	}
	/*@@@...............................................................................................................*/
	/** returns in which column x lies, -1 if to left, -2 if to right.*/
	public int findColumn(int x, int y) {
		if (x<=0)
			return -1;

		int cx = 0;
		for (int column=table.firstColumnVisible; (column<table.numColumnsTotal) && (cx<x); column++) {
			if (diagonal){
				if (betweenDiagonal(x, y, startOfColumn(column), endOfColumn(column)))
					return column;
			}
			else {
				cx += table.columnWidths[column];
				if (column>= table.numColumnsTotal)
					return -1;
				else if (cx>=x)
					return column;
			}
		}
		return -2;//past the last column
	}
	boolean betweenDiagonal(int x, int y, int left, int right){
		diagonalMask.translate(right, height);  //first make sure not to follow
		int righttop = diagonalMask.xpoints[1];
		if (!diagonalMask.contains(x, y)){
			diagonalMask.translate(-right, -height);  
			diagonalMask.translate(left, height);  //now see if in current
			int lefttop = diagonalMask.xpoints[1];
			if (diagonalMask.contains(x, y)) {
				diagonalMask.translate(-left, -height);
				return true;
			}
			diagonalMask.translate(-left, -height);
			//not in diagonal portion; check if in column number areas
			if (y<=table.getColumnGrabberWidth() && x>=lefttop && x< righttop)
				return true;
		}
		else
			diagonalMask.translate(-right, -height);
		return false;
	}
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
			else if (diagonal){
				if (betweenDiagonal(x, y, lastColumnCenterX, columnCenterX))
					return column-1;
			}
			else if (x>lastColumnCenterX && x<= columnCenterX) {
				return column-1;
			} else if (columnCenterX>x)
				return column;
			lastColumnCenterX = columnCenterX;
		}
		return -2;//past the last column
	}
	/*@@@...............................................................................................................*/
	/** returns in which row y lies, -1 if above, -2 if below.*/
	public int findRow(int x, int y) {
		return -1;
	}
	/*@@@...............................................................................................................*/
	/** Returns in which subrow y lies, -1 if above, -2 if below.*/
	public int findSubRow(int x, int y) {
		if (y<=nameRowBottom()) {
			return -1;
		}
		else {
			int subrow = (int)(y-nameRowBottom())/infoStripRowHeight(0);
			return subrow;
		}
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
			int left = columnBoundary-nearZoneOnLeft;
			int right = columnBoundary+nearZoneOnRight;

			if (diagonal){
				if (betweenDiagonal(x, y, left, right))
					return true;
			}
			else if (x>columnBoundary-nearZoneOnLeft  && x<columnBoundary+nearZoneOnRight){
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
			int left = columnBoundary-nearZoneOnLeft;
			int right = columnBoundary+nearZoneOnRight;

			if (diagonal){
				if (betweenDiagonal(x, y, left, right))
					return column;
			}
			else if (x>columnBoundary-nearZoneOnLeft  && x<columnBoundary+nearZoneOnRight){
				return column;
			}
		}
		return -1;

	}
	/*...............................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (!(tool instanceof TableTool))
			return;
		table.stopAutoScrollThread();
		boolean isArrowEquivalent = ((TableTool)tool).isArrowKeyOnColumn(y,table);
		table.adjustingColumnWidth = false;
		touchX=-1;
		touchColumn=-1;
		/*@@@*/
		int possibleTouch = findColumn(x, y);
		int regionInCellH = findRegionInCellH(x);
		int regionInCellV = findRegionInCellV(y);
		int subRow = findSubRow(x, y);

		if (tool != null && isArrowEquivalent && isDiagonal() && y> height-3  && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.commandOrControlKeyDown(modifiers)) {
			touchY=y;
			shimmerY = touchY;
			//	table.shimmerHorizontalOn(shimmerY);
			table.adjustingColumnNamesHeight = true;
		}
		else if (possibleTouch<table.numColumnsTotal && possibleTouch>=0) {
			table.startAutoScrollThread(this);
			if (subRow>=0) {  // touch on subrow
				table.subRowTouched(subRow, possibleTouch,regionInCellH, regionInCellV, x, y, modifiers);
			}
			else if (table.touchColumnNameEvenIfSelected() && (table.showColumnGrabbers) && (y>=table.getColumnGrabberWidth())) {
				if (((TableTool)tool).getIsBetweenRowColumnTool())
					possibleTouch = findColumnBeforeBetween(x, y);
				table.columnNameTouched(possibleTouch,regionInCellH, regionInCellV, modifiers, clickCount);
			}
			else if (tool != null && isArrowEquivalent && table.getUserAdjustColumn()==MesquiteTable.RESIZE && nearColumnBoundary(x, y)  && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.commandOrControlKeyDown(modifiers)) {
				touchX=x;
				touchColumn=nearWhichColumnBoundary(x, y);
				shimmerX = touchX;
				if (diagonal && touchColumn>=0){
					shimmerX = (startOfColumn(touchColumn) + endOfColumn(touchColumn))/2;
				}
				origShimmer = shimmerX;
				table.shimmerVerticalOn(shimmerX);
				table.adjustingColumnWidth = true;
			}
			/*@@@*/
			else if (tool != null && isArrowEquivalent && table.getUserMoveColumn() && table.isColumnSelected(possibleTouch) && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.commandOrControlKeyDown(modifiers)) {
				touchX=x;
				touchColumn=possibleTouch;
				shimmerX = touchX;
				if (diagonal && touchColumn>=0){
					shimmerX = (startOfColumn(touchColumn) + endOfColumn(touchColumn))/2;
				}
				origShimmer = shimmerX;
				table.shimmerVerticalOn(shimmerX);
			}
			else if ((table.showColumnGrabbers) && (y<table.getColumnGrabberWidth())) {
				if (((TableTool)tool).getIsBetweenRowColumnTool() && !isArrowEquivalent)
					possibleTouch = findColumnBeforeBetween(x, y);
				table.columnTouched(isArrowEquivalent, possibleTouch,regionInCellH, regionInCellV, modifiers);
				if (tool != null && isArrowEquivalent && table.getUserMoveColumn() && table.isColumnSelected(possibleTouch) && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.commandOrControlKeyDown(modifiers)) {
					touchX=x;
					shimmerX = MesquiteInteger.unassigned;
					origShimmer = shimmerX;
					touchColumn=possibleTouch;
				}
			}
			else {
				if (((TableTool)tool).getIsBetweenRowColumnTool())
					possibleTouch = findColumnBeforeBetween(x, y);
				table.columnNameTouched(possibleTouch,regionInCellH, regionInCellV, modifiers, clickCount);
			}
		}
		else if (possibleTouch==-2 && ((TableTool)tool).getWorksBeyondLastColumn())
			table.columnTouched(isArrowEquivalent, possibleTouch,regionInCellH, regionInCellV, modifiers);
		else if (((TableTool)tool).getDeselectIfOutsideOfCells()) {
			table.offAllEdits();
			table.outOfBoundsTouched(modifiers, clickCount);
			if (table.anythingSelected()) {
				table.deselectAllNotify();
				table.repaintAll();
			}
		}
		else
			table.outOfBoundsTouched(modifiers, clickCount);

	}
	/*...............................................................................................................*/
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		if (table.adjustingColumnNamesHeight){
			//table.shimmerHorizontalOff(shimmerY);
			//table.shimmerHorizontalOn(y);
			shimmerY=y;
		}
		else if (touchColumn>=0) {

			if (table.getUserAdjustColumn()==MesquiteTable.RESIZE) {
				table.shimmerVerticalOff(shimmerX);
				if (diagonal){
					x = (x-touchX) + origShimmer;
				}
				table.shimmerVerticalOn(x);
				shimmerX=x;
			}
			else if (table.getUserMoveColumn() && tool != null && ((TableTool)tool).isArrowKeyOnColumn(y,table)) {
				table.shimmerVerticalOff(shimmerX);
				if (diagonal){
					x = (x-touchX) + origShimmer;
				}
				table.shimmerVerticalOn(x);
				shimmerX=x;
			}
			table.checkForAutoScroll(this,x,MesquiteInteger.unassigned);
		}
	}
	/*...............................................................................................................*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		table.stopAutoScrollThread();
		if (table.adjustingColumnNamesHeight){
			//table.shimmerHorizontalOff(shimmerY);
			diagonalHeight = diagonalHeight + (y - touchY);
			if (diagonalHeight < 30)
				diagonalHeight = 30;
			table.setColumnNamesRowHeight(diagonalHeight);
			resetDiagonalHeight();
			table.adjustingColumnNamesHeight = false;
			shimmerY=-1;
			touchY=-1;
			table.resetComponentSizes();
			table.repaintAll();
		}
		else 	if (touchColumn>=0 && tool != null ) {
			if (table.getUserAdjustColumn()==MesquiteTable.RESIZE && table.adjustingColumnWidth) {
				table.shimmerVerticalOff(shimmerX);

				int newColumnWidth = table.columnWidths[touchColumn] + x-touchX;
				if ((newColumnWidth >= table.getMinColumnWidth()) && (touchX>=0)) {
					table.setColumnWidth(touchColumn, newColumnWidth);
					table.columnWidthsAdjusted.setBit(touchColumn);
					table.repaintAll();
					//touchX=-1;
				}

			}
			else if (table.getUserMoveColumn() && ((TableTool)tool).isArrowKeyOnColumn(y,table)) {
				table.shimmerVerticalOff(shimmerX);
				int dropColumn = findHalfColumn(x, y);   //cursor; regionH; clickCount; colour by selected
				if (dropColumn == -2)
					dropColumn = table.getNumColumns();
				if (dropColumn != touchColumn && (dropColumn!=touchColumn-1) && !table.isColumnSelected(dropColumn)) //don't move dropped on column included in selection
					table.selectedColumnsDropped(dropColumn);
			}
			table.adjustingColumnWidth = false;
		}
	}
	/*...............................................................................................................*/
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		table.stopAutoScrollThread();
		if (!table.editingAnything() && !table.singleTableCellSelected()) 
			setWindowAnnotation("", null);
		setCursor(Cursor.getDefaultCursor());
		int column = findColumn(x, y);
		table.mouseExitedCell(modifiers, column, -1, -1, -1, tool);
	}
	/*...............................................................................................................*/
	public void setCurrentCursor(int modifiers, int column, int x, int y, MesquiteTool tool) {
		if (tool == null || !(tool instanceof TableTool))
			setCursor(getDisabledCursor());
		else if (isDiagonal() && y> height-3 && ((TableTool)tool).isArrowKeyOnColumn(y,table)  && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.controlKeyDown(modifiers)) 
			setCursor(table.getNResizeCursor());
		else if (column<table.numColumnsTotal && column>=0) {   //within bounds of normal columns
			if (tool != null && ((TableTool)tool).isArrowKeyOnColumn(y,table)) {
				if (table.getUserAdjustColumn()==MesquiteTable.RESIZE && nearColumnBoundary(x, y)  && !MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.controlKeyDown(modifiers)) 
					setCursor(table.getEResizeCursor());
				else {
					setCursor(table.getHandCursor());
					if (!(table.editingAnything() || table.singleTableCellSelected())) {
						String s = table.getColumnComment(column);
						if (s!=null)   
							setWindowAnnotation(s, "Footnote above refers to " + table.getColumnNameText(column));
						else
							setWindowAnnotation("", null);
					}
				}
			}
			else if (((TableTool)tool).getWorksOnColumnNames())
				setCursor(tool.getCursor());
			else {
				setCursor(getDisabledCursor());
			}
		}
		else if (((TableTool)tool).getWorksBeyondLastColumn() && (column==-2))
			setCursor(tool.getCursor());
		else {
			setCursor(getDisabledCursor());
		}

	}
	/*...............................................................................................................*/
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		int column = findColumn(x, y);
		setCurrentCursor(modifiers, column ,  x, y, tool);
		table.mouseInCell(modifiers, column,-1, -1, findSubRow(x, y),tool);
	}
	/*...............................................................................................................*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int column = findColumn(x, y);
		setCurrentCursor(modifiers, column ,  x, y, tool);
		table.mouseInCell(modifiers, column, -1,-1,  findSubRow(x, y), tool);
	}
	public void focusGained(FocusEvent arg0) {
	}
	public void focusLost(FocusEvent arg0) {
		if (table!=null) table.stopAutoScrollThread();
	}

}





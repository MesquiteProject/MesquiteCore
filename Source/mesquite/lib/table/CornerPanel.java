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

/* ======================================================================== */
/** A panel in upper left corner of MesquiteTable */
public class CornerPanel extends MesquitePanel {
	MesquiteTable table;
	public int width,  height;
	Polygon dropDownTriangle;
	public CornerPanel (MesquiteTable table , int w, int h) {
		super();
		dropDownTriangle=MesquitePopup.getDropDownTriangle();
		this.table=table;
		this.width=w;
		this.height=h;
		setBackground(ColorTheme.getContentBackgroundPale());
	}
	public void setTableUnitSize (int w, int h) {
		this.width=w;
		this.height=h;
	}
	public void deselectCell(int column,int row){
	}
	public void redrawCell(int column, int row){
		Graphics g = getGraphics();
		if (g!=null) {
			redrawName(g);
			g.dispose();
		}
	}
	public void redrawName(Graphics g){
		if (g ==null)
			return;
		int w = getBounds().width;
		int h = getBounds().height;
		if (table.cornerIsHeading){
			if (table.getCellDimmed(-1, -1))
				g.setColor(Color.lightGray);
			else
				g.setColor(ColorTheme.getContentBackgroundPale());
			g.fillRect(0, 0,w, h);
			g.setColor(Color.black);
			//if (table.showColumnGrabbers) 
			//	g.fillRect(0,0, w, table.getGrabberWidth());
		}
	/*
		g.setColor(Color.lightGray); 
		Polygon temp = new Polygon(new int[]{w-2, w+1, w+1, w-2}, new int[]{0, 0, h+1, h-2}, 4);
		g.fillPolygon(temp);
		g.setColor(Color.gray); 
		g.drawPolygon(temp);
	*/
		//g.fillRect(w-3, 0, 4, h+1);
		g.setColor(Color.gray);
		FontMetrics fm = g.getFontMetrics(g.getFont());
		g.drawString(table.getUpperCornerText(), 4, fm.getAscent()+fm.getDescent() + 2);
		if (table.getCellDimmed(-1, -1))
			g.setColor(Color.gray);
		else
			g.setColor(Color.black);
		
		
		g.drawString(table.getCornerText(), 4, h-MesquiteString.riseOffset);
		g.setColor(Color.black);
		if (table.getDropDown(-1, -1) && dropDownTriangle != null) {
			dropDownTriangle.translate(w-12,4);
			g.setColor(Color.white);
			g.drawPolygon(dropDownTriangle);
			g.setColor(Color.black);
			g.fillPolygon(dropDownTriangle);
			dropDownTriangle.translate(12-w,-4);
		}
	}
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		if (!table.useString(-1, -1))
			table.drawCornerCell(g, 0,0,getBounds().width, getBounds().height);  //added Apr 02
		else
			redrawName(g);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void print(Graphics g) {
		redrawName(g);
	}
	/*...............................................................................................................*/
	int touchX = -1;
	int lastX = -1;
	/*...............................................................................................................*/
   	public void shimmerOff(int x) {
		if (x<=getBounds().width) {
			table.shimmerVerticalOff(this,x);
			table.shimmerVerticalOff(table.rowNames,x);
		}
		else {
			table.shimmerVerticalOff(table.columnNames,x-touchX);
			table.shimmerVerticalOff(table.matrix,x-touchX);
		}
   	 }
	/*...............................................................................................................*/
   	public void shimmerOn(int x) {
		if (x<=getBounds().width) {
			table.shimmerVerticalOn(this,x);
			table.shimmerVerticalOn(table.rowNames,x);
		}
		else {
			table.shimmerVerticalOn(table.columnNames,x-touchX);
			table.shimmerVerticalOn(table.matrix,x-touchX);
		}
   	 }
	/*...............................................................................................................*/
	public boolean isArrowEquivalent(MesquiteTool tool) {
		if (tool==null)
			return false;
		return (tool.isArrowTool() ||  !((TableTool)tool).getWorksOnCornerPanel());
	}
	/*...............................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (tool == null || !isArrowEquivalent(tool))
			return;
		touchX=-1;
		if (x>getBounds().width-8) {
			touchX=x;
			lastX = x;
			shimmerOn(x);
		}
		else {
			table.cornerTouched(x,y, modifiers);
		}
	}
	/*...............................................................................................................*/
   	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		if (tool == null || !isArrowEquivalent(tool))
			return;
		if (touchX >=0) {
			shimmerOff(lastX);
			shimmerOn(x);
			lastX=x;
		}
   	 }
	/*...............................................................................................................*/
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (tool == null || !isArrowEquivalent(tool))
			return;
		if (touchX >=0) {
			shimmerOff(lastX);
			int newColumnWidth = getBounds().width + x-touchX-table.rowGrabberWidth;
			if (newColumnWidth > 16) {
				table.rowNamesWidthAdjusted = true;
				table.setRowNamesWidth(newColumnWidth);
				table.resetTableSize(false);
				table.repaintAll();
			}
		}
   	 }
	/*...............................................................................................................*/
	public void setCurrentCursor(int modifiers, int x, int y, MesquiteTool tool) {
		if (tool == null || !(tool instanceof TableTool))
				setCursor(getDisabledCursor());
		else if (tool.isArrowTool() || !((TableTool)tool).getWorksOnCornerPanel()) {
			if (x>getBounds().width-8) 
				setCursor(table.getEResizeCursor());
			else
				setCursor(table.getHandCursor());
		}
		else 	if (((TableTool)tool).getWorksOnCornerPanel()) 
			setCursor(tool.getCursor());
		else
			setCursor(getDisabledCursor());
	}
   	/*...............................................................................................................*/
   	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		setCursor(Cursor.getDefaultCursor());
		table.mouseExitedCell(modifiers, -1, -1, -1,-1,  tool);
  	}
	/*...............................................................................................................*/
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (tool == null)
			return;
		setCurrentCursor(modifiers, x, y, tool);
		table.mouseInCell(modifiers, -1, -1,-1, -1, tool);
	}
	/*...............................................................................................................*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		if (tool == null)
			return;
		setCurrentCursor(modifiers, x, y, tool);
		table.mouseInCell(modifiers, -1, -1, -1,-1,  tool);
	}
}


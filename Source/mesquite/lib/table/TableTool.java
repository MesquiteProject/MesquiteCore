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
import mesquite.lib.duties.*;
import mesquite.lib.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls
/* ======================================================================== */
	/** This subclass of MesquiteTool is used in data windows for manipulating data.
	*/
public class TableTool extends MesquiteTool {
	MesquiteCommand touchedCommand;
	MesquiteCommand droppedCommand;
	MesquiteCommand dragCommand;
	boolean useTableTouch;
	boolean worksOnRowNames = false;
	boolean worksOnColumnNames = false;
	boolean worksBeyondLastRow = false;
	boolean worksBeyondLastColumn = false;
	boolean worksOnCornerPanel = false;
	boolean worksOnMatrixPanel = true;
	boolean isBetweenRowColumnTool = false;
	boolean deselectIfOutsideOfCells = true;
	boolean worksAsArrowOnRowColumnNumbers = true;
	boolean specialToolForColumnNamesInfoStrips = false;
	boolean touchOnArrowKey = false;
	boolean emphasizeRowsOnMouseDrag = false;
	boolean emphasizeRowsOnMouseDown = false;
	boolean emphasizeColumnsOnMouseDown = false;
	boolean acceptsOutsideDrops = false;
	
	public TableTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand dragCommand, MesquiteCommand droppedCommand) {
		super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation);
		this.touchedCommand = touchedCommand;
		this.dragCommand = dragCommand;
		this.droppedCommand = droppedCommand;
		if (dragCommand != null)
			dragCommand.setSuppressLogging(true);
		useTableTouch = touchedCommand == null;
		setOnlyWorksWhereSpecified(true);
	}
	
	
	public void dispose(){ 
		if (touchedCommand!=null) 
			touchedCommand.dispose();
		else if (droppedCommand!=null) 
			droppedCommand.dispose();
		else if (dragCommand!=null) 
			dragCommand.dispose();
		touchedCommand=null;
		droppedCommand=null;
		dragCommand=null;
		super.dispose();
	}
	//can be overridden to, for instance, change the cursor
	public void cursorInPanel(int modifiers, int x, int y, MousePanel panel, boolean in){
		if (in && panel instanceof EditorPanel){
			EditorPanel ePanel = ((EditorPanel)panel);
			int column = ePanel.findColumn(x, y);
			int row = ePanel.findRow(x, y);
	   		int regionInCellH = ePanel.findRegionInCellH(x);
	   		int regionInCellV = ePanel.findRegionInCellV(y);
	   		cursorInCell(modifiers, column, row, regionInCellH, regionInCellV, ePanel);
		}
	}
	
	//tools can override this to find out when cursor is moved into a cell and a region of the cell
	public void cursorInCell(int modifiers, int column, int row, int regionInCellH, int regionInCellV, EditorPanel panel){
		
		//here can call panel.setCursor(cursor); to set current cursor of panel
	}
	
	public void setWorksOnCornerPanel(boolean u){
		worksOnCornerPanel = u;
	}
	public boolean getWorksOnCornerPanel(){
		return worksOnCornerPanel;
	}
	public void setWorksOnMatrixPanel(boolean u){
		worksOnMatrixPanel = u;
	}
	public boolean getWorksOnMatrixPanel(){
		return worksOnMatrixPanel;
	}
	public void setWorksBeyondLastRow(boolean u){
		worksBeyondLastRow = u;
	}
	public boolean getWorksBeyondLastRow(){
		return worksBeyondLastRow;
	}
	public void setWorksBeyondLastColumn(boolean u){
		worksBeyondLastColumn = u;
	}
	public boolean getWorksBeyondLastColumn(){
		return worksBeyondLastColumn;
	}
	public void setEmphasizeRowsOnMouseDrag(boolean u){
		emphasizeRowsOnMouseDrag = u;
	}
	public boolean getEmphasizeRowsOnMouseDrag(){
		return emphasizeRowsOnMouseDrag;
	}
	public void setEmphasizeRowsOnMouseDown(boolean u){
		emphasizeRowsOnMouseDown = u;
	}
	public boolean getEmphasizeRowsOnMouseDown(){
		return emphasizeRowsOnMouseDown;
	}

	public void setWorksAsArrowOnRowColumnNumbers(boolean u){
		worksAsArrowOnRowColumnNumbers = u;
	}
	public boolean getWorksAsArrowOnRowColumnNumbers(){
		return worksAsArrowOnRowColumnNumbers;
	}
	
	public void setWorksOnColumnNames(boolean u){
		worksOnColumnNames = u;
	}
	public boolean getWorksOnColumnNames(){
		return worksOnColumnNames;
	}
	public void setSpecialToolForColumnNamesInfoStrips(boolean u){
		specialToolForColumnNamesInfoStrips = u;
	}
	public boolean getSpecialToolForColumnNamesInfoStrips(){
		return specialToolForColumnNamesInfoStrips;
	}
	public void setWorksOnRowNames(boolean u){
		worksOnRowNames = u;
	}
	public boolean getWorksOnRowNames(){
		return worksOnRowNames;
	}
	public void setUseTableTouchRules(boolean u){
		useTableTouch = u;
	}
	public boolean useTableTouchRules(){
		return useTableTouch;
	}
	public void setDeselectIfOutsideOfCells(boolean u){
		deselectIfOutsideOfCells = u;
	}
	public boolean getDeselectIfOutsideOfCells(){
		return deselectIfOutsideOfCells;
	}
	
	public void setIsBetweenRowColumnTool(boolean u){
		isBetweenRowColumnTool = u;
	}
	public boolean getIsBetweenRowColumnTool(){
		return isBetweenRowColumnTool;
	}
	


	
	 
	/* Sets whether or not tool is requesting to be informed if the up, down, right or left arrow keys are touched.  This is done by responding as if new cell was touched */
	public void setTouchOnArrowKey(boolean u){
		touchOnArrowKey = u;
	}
	public boolean getTouchOnArrowKey(){
		return touchOnArrowKey;
	}
	public boolean isArrowKeyOnColumn(int y, MesquiteTable table){
		return super.isArrowTool() || (getWorksAsArrowOnRowColumnNumbers() && table.showColumnGrabbers  && y<=table.getColumnGrabberWidth());
	}
	public boolean isArrowKeyOnRow(int x, MesquiteTable table){
		return super.isArrowTool() || (getWorksAsArrowOnRowColumnNumbers() && table.showRowGrabbers  && x<=table.getRowGrabberWidth());
	}

	public void cellTouched(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (touchedCommand!=null)
			touchedCommand.doItMainThread(Integer.toString(column) + " " + row + " "  + regionInCellH + " " + regionInCellV + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", touchedCommand, this), false, false);  
	}
	public void cellDrag(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (dragCommand!=null)
			dragCommand.doItMainThread(Integer.toString(column) + " " + row + " " + regionInCellH + " " + regionInCellV + " " +  MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", dragCommand, this), false, false);  
	}
	public void cellDropped(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (droppedCommand!=null)
			droppedCommand.doItMainThread(Integer.toString(column) + " " + row + " " + regionInCellH + " " + regionInCellV + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", droppedCommand, this), false, false);  
	}


	public boolean acceptsOutsideDrops() {
		return acceptsOutsideDrops;
	}


	public void setAcceptsOutsideDrops(boolean acceptsOutsideDrops) {
		this.acceptsOutsideDrops = acceptsOutsideDrops;
	}
}


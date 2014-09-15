/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
/*=======================================================*/
/** A window that contains a table in which to edit a CategTModel.  Used for Stepmatrix editing, but could also be used for other similar models */
public class CategTModelEditWindow extends TableWindow implements MesquiteListener {
	CategTModelTable table;
	int windowWidth = 360;
	int windowHeight =300;
	ToolPalette palette = null;
	CharModelCurator curatorModule;
	TableTool arrowTool,ibeamTool, fillTool, dropperTool;
	MesquiteNumber fillNumber = new MesquiteNumber(0);
	public CategTModelEditWindow (CharModelCurator curatorModule, MesquiteModule ownerModule){
		super(ownerModule, true); 
		this.curatorModule = curatorModule;
		ownerModule.setModuleWindow(this);
		setWindowSize(windowWidth, windowHeight);
		table = new CategTModelTable(curatorModule, this, 32, 32, getBounds().width - 32, getBounds().height-32, 30);
		getGraphicsArea().setLayout(new BorderLayout());
		addToWindow(table);
   	 	table.setSize(windowWidth, windowHeight);
		String selectExplanation = "This tool selects items in the matrix.  By holding down shift while clicking, the selection will be extended from the first to the last touched cell. ";
		selectExplanation += " A block of cells can be selected either by using shift-click to extend a previous selection, or by clicking on a cell and dragging with the mouse button still down";
		selectExplanation += " Discontinous selections are allowed, and can be obtained by a \"meta\"-click (right mouse button click, or command-click on a MacOS system). ";
		arrowTool = new TableTool(this, "arrow", MesquiteModule.getRootImageDirectoryPath(),"arrow.gif", 4, 2, "Select", selectExplanation, MesquiteModule.makeCommand("arrowTouchCell",  this) , MesquiteModule.makeCommand("arrowDragCell",  this), MesquiteModule.makeCommand("arrowDropCell",  this));
		arrowTool.setIsArrowTool(true);
		arrowTool.setUseTableTouchRules(true);
		addTool(arrowTool);
  	 	setCurrentTool(arrowTool);
 		if (arrowTool!=null)
 			arrowTool.setInUse(true);
		ibeamTool = new TableTool(this, "ibeam", MesquiteModule.getRootImageDirectoryPath(),"ibeam.gif", 7,7,"Edit", "This tool can be used to edit the contents of cells in the matrix.", MesquiteModule.makeCommand("editCell",  (Commandable)table) , null, null);
		addTool(ibeamTool);
		ibeamTool.setWorksOnRowNames(true);

		fillTool = new TableTool(this, "fill", MesquiteModule.getRootImageDirectoryPath(),"bucket.gif", 13,14,"Fill with 1", "This tool fills selected cells with text.  The text to be used can be determined by using the dropper tool.", MesquiteModule.makeCommand("fillTouchCell",  this) , null, null);
		fillTool.setOptionsCommand(MesquiteModule.makeCommand("touchTool",  this));
		dropperTool = new TableTool(this, "dropper", MesquiteModule.getRootImageDirectoryPath(),"dropper.gif", 1,14,"Copy value", "This tool fills the paint bucket with the text in the cell touched on", MesquiteModule.makeCommand("dropperTouchCell",  this) , null, null);
		addTool(fillTool);
		addTool(dropperTool);
   	 	setShowExplanation(true);
   	 	setShowAnnotation(true);
		resetTitle();
		table.setVisible(true);
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Edit " + curatorModule.getNameOfModelClass()); 
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Fills the touched cell with current paint", "[column][row]", commandName, "fillTouchCell")) {
   	 		if (table!=null && fillNumber !=null && table.getModel()!=null){
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   	 		String entry = fillNumber.toString();
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
	   			if (!table.rowLegal(row)|| !table.columnLegal(column))
	   				return null;
	   			if (table.anyCellSelected()) {
		   			if (table.isCellSelected(column, row)) {
						for (int i=0; i<table.getNumColumns(); i++)
							for (int j=0; j<table.getNumRows(); j++)
								if (table.isCellSelected(i,j)) {
									table.returnedMatrixTextNotify(i,j, entry,false);
								}
		 	   			table.repaintAll();
					}
				}
				else if (table.anyRowSelected()) {
		   			if (table.isRowSelected(row)) {
						for (int j=0; j<table.getNumRows(); j++) {
							if (table.isRowSelected(j))
								for (int i=0; i<table.getNumColumns(); i++)
									table.returnedMatrixTextNotify(i,j, entry,false);
						}
		 	   			table.repaintAll();
					}
				}
				else if (table.anyColumnSelected()) {
		   			if (table.isColumnSelected(column)) {
						for (int i=0; i<table.getNumColumns(); i++){
							if (table.isColumnSelected(i))
								for (int j=0; j<table.getNumRows(); j++) 
									table.returnedMatrixTextNotify(i,j, entry,false);
						}
		 	   			table.repaintAll();
					}
				}
				else {
					table.returnedMatrixTextNotify(column, row, entry, false);
		 	   		table.repaintAll();
				}
				((CharacterModel)table.getModel()).notifyListeners(this, new Notification(MesquiteListener.UNKNOWN),CharacterModel.class, true);
				((CharacterModel)table.getModel()).notifyListeners(this, new Notification(MesquiteListener.UNKNOWN),CharacterModel.class, false);

			}
    	 	}
     	 	else if (checker.compare(this.getClass(), "Queries the user what paint to use", null, commandName, "touchTool")) {
   	 		if (table!=null){
				String fillString = MesquiteString.queryString(this, "Fill value", "Value with which to fill using paint bucket:", "");
		   		if (StringUtil.blank(fillString))
		   			return null;
		   		fillNumber.setValue(fillString);
	   			fillTool.setDescription("Fill with \"" + fillNumber.toString()+ " \"");
	   			dropperTool.setDescription("Copy value (current: " + fillNumber.toString() + ")");
				toolTextChanged();
			}
    	 	}
   	 	else if (checker.compare(this.getClass(), "Fills the paint bucket with the string of the selected cell", "[column][row]", commandName, "dropperTouchCell")) {
   	 		if (table!=null){
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
	   			if (!table.rowLegal(row)|| !table.columnLegal(column))
	   				return null;
	   			String fillString = table.getMatrixText(column, row);
		   		fillNumber.setValue(fillString);
	   			fillTool.setDescription("Fill with \"" + fillNumber.toString()+ " \"");
	   			dropperTool.setDescription("Copy value (current: " + fillNumber.toString() + ")");
				toolTextChanged();
			}
    	 	}

    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
	return null;
   	 }
    CategTModel model = null;
    public void dispose(){
		if (model != null && model instanceof FileElement && ((FileElement)model).getProject() != null)
			((FileElement)model).getProject().getCentralModelListener().removeListener(this);

    	super.dispose();
    }
	/*.................................................................................................................*/
	public void setModel(CategTModel model) {
		table.setModel(model);
		this.model= model;
		setTitle("Edit " + curatorModule.getNameOfModelClass()+ ": " + model.getName());

		if (model instanceof FileElement && ((FileElement)model).getProject() != null)
			((FileElement)model).getProject().getCentralModelListener().addListener(this);
		contentsChanged();
	}
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
	
		if (obj == model)
			contentsChanged();
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (obj == table.getModel() && ownerModule != null)
			ownerModule.windowGoAway(this);
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
		
	}

   	public void setMaxState(int maxState){
   		table.setMaxState(maxState);
   	}
   	public void setDiagnonalEditable(boolean editable){
   		table.setDiagnonalEditable(editable);
   	}
   	public boolean getDiagnonalEditable(){
   		return table.getDiagnonalEditable();
   	}
	public void windowResized() {
		super.windowResized();
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
   	 	if (table!=null && ((getHeight()!=windowHeight) || (getWidth()!=windowWidth))) {
   	 		windowHeight =getHeight();
   	 		windowWidth = getWidth();
   	 		table.setSize(windowWidth, windowHeight);
   	 	}
		MesquiteWindow.uncheckDoomed(this);
	}
	public MesquiteTable getTable(){
		return table;
	}
}

	/*=======================================================*/
class CategTModelTable extends MesquiteTable {
	CharModelCurator curatorModule;
	CategTModel model;
	MesquiteWindow window;
	boolean diagonalEditable = true;
	
	public CategTModelTable (CharModelCurator curatorModule, MesquiteWindow window, int numRowsTotal, int numColumnsTotal, int totalWidth, int totalHeight, int taxonNamesWidth) {
		super(numRowsTotal, numColumnsTotal, totalWidth, totalHeight, taxonNamesWidth, ColorDistribution.getColorScheme(curatorModule), true, true);
		this.window= window;
		this.curatorModule=curatorModule;
		frameColumnNames=false;
		frameRowNames=false;

		setEditable(true, false, false, false);
		setSelectable(true, false, false, false, false, false);
		setColumnWidthsUniform(48);
	}
   	public void setDiagnonalEditable(boolean editable){
   		diagonalEditable = editable;
   	}
   	public boolean getDiagnonalEditable(){
   		return diagonalEditable;
   	}
   	public void setMaxState(int maxState){
   		model.setMaxStateDefined(maxState);
   		setNumColumns(maxState+1);
   		setNumRows(maxState+1);
   		repaintAll();
   	}
	public CategTModel getModel() {
		return model;
	}
	public void setModel(CategTModel model) {
		this.model = model;
		if (getNumRows() != model.getMaxStateDefined()+1)
			setNumRows(model.getMaxStateDefined()+1);
		if (getNumColumns() != model.getMaxStateDefined()+1)
			setNumColumns(model.getMaxStateDefined()+1);
		repaint();
	}
	/** Called after editing a cell, passing the String resulting. 
	Can be overridden in subclasses to respond to editing.*/
	public void returnedMatrixTextNotify(int column, int row, String s,  boolean notify){ 
		if (column==row && !diagonalEditable)
			return;
		if (StringUtil.blank(s))
			return;
		boolean explicitlyUnassigned = ("unassigned".equalsIgnoreCase(s) || "estimate".equalsIgnoreCase(s) || "?".equalsIgnoreCase(s));
		
		MesquiteNumber i = new MesquiteNumber();
		MesquiteNumber c = new MesquiteNumber();
		i.setValue(s);
		if (i.isCombinable() || i.isInfinite() || explicitlyUnassigned){
			if (!i.equals(model.getTransitionValue(row,column, c))){
				model.setTransitionValue(row,column, i, notify);
				repaint();
			}
		}
			
	}
	/** Called after editing a cell, passing the String resulting. 
	Can be overridden in subclasses to respond to editing.*/
	public void returnedMatrixText(int column, int row, String s){ 
		if (column==row && !diagonalEditable)
			return;
		if (StringUtil.blank(s))
			return;
		boolean explicitlyUnassigned = ("unassigned".equalsIgnoreCase(s) || "estimate".equalsIgnoreCase(s) || "?".equalsIgnoreCase(s));
		
		MesquiteNumber i = new MesquiteNumber();
		MesquiteNumber c = new MesquiteNumber();
		i.setValue(s);
		if (i.isCombinable() || i.isInfinite() || explicitlyUnassigned){
			if (!i.equals(model.getTransitionValue(row,column, c))){
				model.setTransitionValue(row,column, i, true);
				repaint();
			}
		}
			
	}
	public boolean useString(int column, int row){
		return false;
	}
	
	public void drawMatrixCell(Graphics g, int x, int y,  int w, int h, int column, int row, boolean selected){  
		Color fontColor, bgColor;
		if (selected)
			fontColor = Color.white;
		else 
			fontColor = Color.black;
		if (column==row) {
			if (diagonalEditable)
				bgColor = ColorDistribution.veryLightGray;
			else 
				bgColor = Color.gray;
			g.setColor(bgColor);
			g.fillRect(x+1,y+1,w-2,h-2);
			g.setColor(fontColor);
			g.drawString(model.getTransitionValue(row,column, null).toString(), x+2, y+h-2);
			
		}
		else {
			if (selected)
				bgColor = Color.black;
			else 
				bgColor = Color.white;
			g.setColor(bgColor);
			g.fillRect(x+1,y+1,w-2,h-2);
			g.setColor(fontColor);
			g.drawString(model.getTransitionValue(row,column, null).toString(), x+2, y+h-2);
		}
	}
	
	/*...............................................................................................................*/
	public  void drawCornerCell(Graphics g, int x, int y,  int w, int h){
		FontMetrics fm=g.getFontMetrics(g.getFont());
		int lineHeight = fm.getAscent() + fm.getDescent() + 4;
		g.drawString("From", x+3, y+h-fm.getDescent());
		g.drawString("To", x+w -fm.stringWidth("To "), y+lineHeight);
	}
	public String getMatrixText(int column, int row){
		if (column==row) {
			return model.getTransitionValue(row,column, null).toString();
		}
		else  {
			return model.getTransitionValue(row,column, null).toString();
		}
	}
	public void drawColumnNameCell(Graphics g, int x, int y, int w, int h, int column){
		//g.clipRect(x,y,w,h);
		g.drawString(model.getStateSymbol(column), x+2, y+h-2);
		//g.clipRect();
		
	}
	public void drawRowNameCell(Graphics g, int x, int y,  int w, int h, int row){
		g.drawString(model.getStateSymbol(row), x+2, y+h-2);
	}
	/*...............................................................................................................*/
	public void cellTouched(int column, int row, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		if (column==row && !diagonalEditable)
			return;
		if (window.getCurrentTool().isArrowTool()) 
			super.cellTouched(column, row, regionInCellH, regionInCellV, modifiers, clickCount);
		else
			((TableTool)window.getCurrentTool()).cellTouched(column, row, regionInCellH, regionInCellV, modifiers);
		repaintAll();
	}
	/*...............................................................................................................*/
	public void cellDrag(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (window.getCurrentTool().isArrowTool()) 
			super.cellDrag(column, row, regionInCellH,  regionInCellV, modifiers);
		else
		((TableTool)window.getCurrentTool()).cellDrag(column, row, regionInCellH,  regionInCellV, modifiers);
	}
	/*...............................................................................................................*/
	public void cellDropped(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (column==row && !diagonalEditable)
			return;
		if (window.getCurrentTool().isArrowTool()) 
			super.cellDropped(column, row,  regionInCellH,  regionInCellV, modifiers);
		else
		  ((TableTool)window.getCurrentTool()).cellDropped(column, row, regionInCellH, regionInCellV, modifiers);
	}
}



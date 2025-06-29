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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.util.Vector;

import mesquite.lib.Annotatable;
import mesquite.lib.Associable;
import mesquite.lib.Bits;
import mesquite.lib.FileElement;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteEvent;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Notification;
import mesquite.lib.StringUtil;
import mesquite.lib.UndoInstructions;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.EditorPanel;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.table.TableTool;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.QueryDialogs;


/* ======================================================================== */
public class ListTable extends MesquiteTable {
	ListWindow window;
	LTCellAnnotation cellAnnotatable;
	ListModule ownerModule;
	public ListTable (int numRowsTotal, int numColumnsTotal, int totalWidth, int totalHeight, int rowNamesWidth, ListWindow window, ListModule ownerModule) {  
		super(numRowsTotal, numColumnsTotal, totalWidth, totalHeight, rowNamesWidth, window.getColorScheme(), true,false);
		this.window = window;
		cellAnnotatable = new LTCellAnnotation(ownerModule, window);
		this.ownerModule = ownerModule;
		showRowGrabbers=true;
		showColumnGrabbers=true;
		cornerIsHeading = true;
		//setAutoEditable(false, true, false, false);
		setEditable(true, true, false, false);
		//setSelectable(false, true, true, true, false, false);
		setSelectable(true, true, true, true, true, false);
		setUserMove(true, true);
		setMaximumRowNamesWidth(800);
	}
	
	/* ............................................................................................................... */
	/** Selects row */
	public void selectRow(int row) {
		super.selectRow(row);
		if (false && rowLegal(row)){
			if (getRowAssociable() != null) {
				getRowAssociable().setSelected(row, true);
				//getRowAssociable().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
	}

	/* ............................................................................................................... */
	/** Selects rows */
	public void selectRows(int first, int last) {
		super.selectRows(first, last);
		if (false && rowLegal(first) && rowLegal(last)) {
			int r1 = MesquiteInteger.minimum(first, last);
			int r2 = MesquiteInteger.maximum(first, last);
			for (int i = r1; i <= r2; i++) {
				if (getRowAssociable() != null) {
					getRowAssociable().setSelected(i, true);
				}
			}
		}
		if (false && getRowAssociable() != null) 
			getRowAssociable().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

	}
	/*...............................................................................................................*/
	public void selectedColumnsDropped(int after) {
			window.selectedColumnsDropped(after+1, true);
	}

	/*...............................................................................................................*/
	public void selectedRowsDropped(int after){
		if (window.owner.rowsMovable() && after >= -1) {
			if (after >= getNumRows())
				after = getNumRows();
			if (window.getCurrentObject() !=null && window.getCurrentObject() instanceof Associable){
				
				Bits b = getRowsSelected();
				if (b.numBitsOn()==1 && b.firstBitOn() == after){
					return;
				}
				Vector v = window.getOwnerModule().pauseAllPausables();
				Associable assoc = (Associable)window.getCurrentObject();
				long[] fullChecksumBefore = null;
				if (assoc instanceof CharacterData)
					fullChecksumBefore = ((CharacterData)assoc).getIDOrderedFullChecksum();
				
				int i = 0;
				boolean asked = false;

				UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_MOVED,assoc);
				undoInstructions.recordPreviousOrder(assoc);
				UndoReference undoReference = new UndoReference(undoInstructions, ownerModule);

				while (i<getNumRows()){
					if (b.isBitOn(i)) {
						if (assoc instanceof CharacterData && !asked && ((CharacterData)assoc).isMolecularSequence() && i!=after) {
							if (!AlertDialog.query(window, "Move?", "These are molecular sequences.  Are you sure you want to move the sites to a different location?  It cannot be undone.", "Move", "Cancel")) {
								window.getOwnerModule().unpauseAllPausables(v);
								return;
						}
						asked = true;
						}
						deselectRow(i);
						b.clearBit(i);
						assoc.moveParts(i,1,after);
						b.moveParts(i, 1, after);
						if (i>after)
							after++;
						i=0;
					}
					else
						i++;
				}

				
				if (assoc instanceof TreeVector)
					((TreeVector)assoc).resetAssignedNumbers();
				if (assoc instanceof CharacterData){
					long[] fullChecksumAfter = ((CharacterData)assoc).getIDOrderedFullChecksum();
					 ((CharacterData)assoc).compareChecksums(fullChecksumBefore, fullChecksumAfter, true, "character moving");
				}
				
				/*
				for (int i=0; i<getNumRows(); i++)
					if (isRowSelected(i)) {
						((Associable)window.getCurrentObject()).moveParts(i,1,after++);//TODO: not correct! need to adjust and see what new "after" is
					}
				*/
				synchronizeRowSelection(((Associable)window.getCurrentObject()));
				assoc.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));
				window.getOwnerModule().unpauseAllPausables(v);
				if (window.owner.resetMenusOnNameChange()){
					//MesquiteWindow.resetAllTitles();
					window.owner.getProject().refreshProjectWindow();
					window.owner.resetAllMenuBars();
				}
				// redo project panel if these are vectors of tree blocks or of character matrices  
		 		repaintAll();
	 		}
 		}
	}
	/* ............................................................................................................... */
	/** repaints all components of the table */
	public void repaintAll() {
		// checkResetFont(getGraphics());
		columnNames.repaint();
		repaint();
		rowNames.repaint();
		cornerCell.repaint();
		matrix.repaint();
	}
	public String getCornerText(){  
		if (getNumRows()==0)
			return "No " + window.owner.getItemTypeNamePlural();
		return window.owner.getItemTypeName();
	}
	/*...............................................................................................................*/
	/** Gets whether the cell is dimmed (true) or not (false).  Column -1 is for row names; row -1 is for column names.*/
	public boolean getCellDimmed(int column, int row){
		if (column == -1 && (row == -1))
				return getNumRows()<=0;
		return super.getCellDimmed(column, row);
	}
	/*...............................................................................................................*/
	/** returns whether or not a cells of table editable by default.*/
	public boolean cellsEditableByDefault(){
		return false;
	}
	/*...............................................................................................................*/
	/** returns whether or not a row name of table is editable.*/
	public boolean checkRowNameEditable(int row){
		return super.isRowNameEditable(row);
	}
	/*...............................................................................................................*/
	public void setRowNameColor(Graphics g, int row){
		window.setRowNameColor(g,row);
	}
	/*...............................................................................................................*/
	/** returns whether or not a column name of table is editable.*/
	public boolean checkColumnNameEditable(int column){
		return super.isColumnNameEditable(column);
	}
	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int column, int row){
		return window.isCellEditable(column, row);
	}
	/*...............................................................................................................*/
	/** returns whether or not a row name of table is editable.*/
	public boolean isRowNameEditable(int row){
		return window.isRowNameEditable(row);
	}
	/*...............................................................................................................*/
	/** returns whether or not a column name of table is editable.*/
	public boolean isColumnNameEditable(int column){
		return window.isColumnNameEditable(column);
	}
	public boolean useString(int column, int row){  
		ListAssistant assistant = window.findAssistant(column);
		if (assistant!=null) {
			return (assistant.useString(row));
		}
		return false;
	}
	public String getMatrixText(int column, int row){  
		ListAssistant assistant = window.findAssistant(column);
		if (assistant!=null) {
			try{
				return assistant.getStringForRow(row);
			}
			catch (NullPointerException e){
			}
		}
		return "?";
	}
	public Color getBackgroundColor(int column, int row, boolean selected){
		ListAssistant assistant = window.findAssistant(column);
		if (assistant == null)
			return null;
		return assistant.getBackgroundColorOfCell(row,selected);
	}
	
	/* ............................................................................................................... */
	public Color getTextColor(int column, int row, boolean selected){
		ListAssistant assistant = window.findAssistant(column);
		if (assistant == null)
			return null;
		return assistant.getTextColorOfCell(row,selected);
	}

	public void drawMatrixCell(Graphics g, int x, int y,  int w, int h, int column, int row, boolean selected){  
		ListAssistant assistant = window.findAssistant(column);
		if (assistant!=null) {
			String s = "";
			try{
				s = assistant.getStringForRow(row); 
			}
			catch (NullPointerException e){
				if (MesquiteTrunk.developmentMode)
					System.err.println("NPE in drawMatrixCell of ListTable");
			}
		
		if (assistant.useString(row) && s!=null) {
				FontMetrics fm = getFontMetrics(getFont());
				int sw = fm.stringWidth(s);
				int sh = fm.getMaxAscent()+ fm.getMaxDescent();
				try{
					Color old = g.getColor();
					Color c = assistant.getBackgroundColorOfCell(row,selected);
					if (c!=null)
						if (selected)
							c = Color.black;
						else if (isCellEditable(column, row))
							c = Color.cyan;
						else
							c = ColorDistribution.uneditable;
					if (c!=null) g.setColor(c);
					g.fillRect(x, y, w, h);
					if (selected)
						g.setColor(Color.red);
					else
						g.setColor(Color.black);
					g.drawString(assistant.getStringForRow(row), x+(w-sw)/2, y+h-(h-sh)/2);
					g.setColor(old);
					
				}
				catch (NullPointerException e){
					MesquiteMessage.printStackTrace("NPE");
				}
			}
			else
				assistant.drawInCell(row, g, x, y, w, h, selected);
		}
	}
	
	/*public void drawColumnNameCell(Graphics g, int x, int y, int w, int h, int column){
		ListAssistant assistant = window.findAssistant(column);
		if (assistant!=null)
			g.drawString(assistant.getTitle(), x+getNameStartOffset(), y+h-2);
	}
	*/
	public void drawRowNameCell(Graphics g, int x, int y,  int w, int h, int row){
		String s = window.getRowName(row);
		if (s == null)
			return;
		Shape clip = g.getClip();
		g.setClip(x,y,w,h);
		if (window.rowHighlighted(row)){
			Color c = g.getColor();
			g.setColor(ColorDistribution.straw);
			g.fillRect(x,y,w,h);
			if (c!=null) g.setColor(c);
		}

		Object wo = ownerModule.getAnnotation(row); 
		if (wo != null && (wo instanceof String || wo instanceof MesquiteString)){
			if (wo instanceof String && !StringUtil.blank((String)wo))
				s = "*" + s;
			else if (wo instanceof MesquiteString && !StringUtil.blank(((MesquiteString)wo).getValue()))
				s = "*" + s;
		}

		int gnso = x+getNameStartOffset();
		g.drawString(s, gnso, y+h-4);
		g.setClip(clip);
	}
	/*...............................................................................................................*/
	public void cellTouched(int column, int row, EditorPanel editorPanel, int x, int y, int modifiers, int clickCount) {
		window.setAnnotation("", null);
		if (!window.interceptCellTouch(column, row, modifiers)){
		
			if (window.getCurrentTool()== window.arrowTool)  {
				ListAssistant assistant = window.findAssistant(column);
				if (assistant!=null) {
				Graphics g = getGraphics();
					if (clickCount>1 && assistant.isCellEditable(row)){   //added Aug 2O17 to allow doubleclicking in tables and then editing
						window.setCurrentTool(window.ibeamTool);
						window.getPalette().setCurrentTool(window.ibeamTool); 
						editMatrixCell(column,row);
					} 
					else if (!assistant.arrowTouchInRow(g, row, getLeftOfColumn(column), getTopOfRow(row), clickCount>1, modifiers)){
						if (assistant.isCellEditable(row)) {
							super.cellTouched(column, row, editorPanel, x, y,  modifiers,  clickCount);
						}
						else
							rowTouched(true,row,editorPanel, x, y, modifiers);
					}
				}
				else
					rowTouched(true,row,editorPanel, x, y, modifiers);

			}
			else
				((TableTool)window.getCurrentTool()).cellTouched(column, row, editorPanel, x, y, modifiers);
		}
	}
	/*...............................................................................................................*/
	public void cellDrag(int column, int row, EditorPanel editorPanel, int x, int y, int modifiers) {
		if (window.getCurrentTool()== window.arrowTool) 
			super.cellDrag(column, row, editorPanel, x, y, modifiers);
		else
		((TableTool)window.getCurrentTool()).cellDrag(column, row, editorPanel, x, y, modifiers);
	}
	/*...............................................................................................................*/
	public void cellDropped(int column, int row, EditorPanel editorPanel, int x, int y, int modifiers) {
		/*
		if (window.getCurrentTool()== window.arrowTool) 
			rowTouched(row, modifiers);
		else  */
		   	((TableTool)window.getCurrentTool()).cellDropped(column, row, editorPanel, x, y, modifiers);
	}
	public void rowTouched(boolean asArrow, int row, EditorPanel editorPanel, int x, int y, int modifiers) {
		super.rowTouched(asArrow,  row, editorPanel, x, y, modifiers);
		//if (getRowAssociable() != null) 
		//	synchronizeRowSelection(getRowAssociable());
		showAnnotationAndExplanation(row);
	}
	private void showAnnotationAndExplanation(int row){
		if (row>=0){
			cellAnnotatable.setRow(row);
			window.setAnnotation(cellAnnotatable); 
			window.focusInRow(row);
		}
		else {
			window.setAnnotation("", null); 
			window.focusInRow(-1);
		}
	}
	public void setFocusedCell(int column, int row){
		showAnnotationAndExplanation(row);
		super.setFocusedCell(column, row);
	}
	
	/* ............................................................................................................... */
	/**
	 * Called if right clicked on cell. Can be overridden in subclasses to respond.
	 */
	public void cellRightClicked(int column, int row, EditorPanel editorPanel, int x, int y, int modifiers){
		if (column != -1 || row <0)
			return;
		Object a = ownerModule.getMainObject();
		if (a instanceof ListableVector){
			ListableVector v = (ListableVector)a;
			if (row>=v.size())
				return;
			Listable obj = v.elementAt(row);
			if (!(obj instanceof Annotatable))
				return;
			Annotatable anot = (Annotatable)obj;
			String current = anot.getAnnotation();
			if (current == null) current = "";
			MesquiteString value = new MesquiteString(current);
			String message = "Set footnote for " + obj.getName();
			boolean result = QueryDialogs.queryString(window, "Set Footnote", message,  value, 4, false, false);
			if (result)
				anot.setAnnotation(value.getValue(), true);
		
		}
		else if (a instanceof Vector){
			Vector v = (Vector)a;
			if (row>=v.size())
				return;
			Object obj = v.elementAt(row);
			if (!(obj instanceof Annotatable))
				return;
			Annotatable anot = (Annotatable)obj;
			String current = anot.getAnnotation();
			if (current == null) current = "";
			MesquiteString value = new MesquiteString(current);
			String message = "Set footnote";
			boolean result = QueryDialogs.queryString(window, "Set Footnote", message,  value, 4, false, false);
			if (result)
				anot.setAnnotation(value.getValue(), true);
		}
		else if (a instanceof Taxa){
			Taxa taxa = (Taxa)a;
			if (row>=taxa.getNumTaxa())
				return;
			String current = taxa.getAnnotation(row);
			if (current == null) current = "";
			MesquiteString value = new MesquiteString(current);
			String message = "Set footnote for taxon " +(row+1) + " (" + taxa.getTaxonName(row) + ")";
			boolean result = QueryDialogs.queryString(window, "Set Footnote", message,  value, 4, false, false);
			if (result) {
				taxa.setAnnotation(row, value.getValue());
				taxa.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
			}
		}
		else if (a instanceof CharacterData){
			CharacterData data = (CharacterData)a;
			if (row>=data.getNumChars())
				return;
			String current = data.getAnnotation(row);
			if (current == null) current = "";
			MesquiteString value = new MesquiteString(current);
			String message = "Set footnote for character " +(row+1);
			if (data.characterHasName(row))
				message += " (" + data.getCharacterName(row) + ")";
			boolean result = QueryDialogs.queryString(window, "Set Footnote", message,  value, 4, false, false);
			if (result) {
				data.setAnnotation(row, value.getValue());
				data.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
			}
		}
	}

	/*...............................................................................................................*/
	public void rowNameTouched(int row, EditorPanel editorPanel, int x, int y, int modifiers, int clickCount) {
		showAnnotationAndExplanation(row);
		if (window.getCurrentTool()== window.arrowTool) {
			if (!window.interceptRowNameTouch(row, editorPanel, x, y, modifiers)) {
				if (row >= 0 && MesquiteEvent.rightClick(modifiers)){
					Object a = ownerModule.getMainObject();
					if (a instanceof ListableVector){
						ListableVector v = (ListableVector)a;
						if (row < v.size()){
							Listable rObj = v.elementAt(row);
							if (rObj instanceof FileElement){
								FileElement hN = (FileElement)rObj;
								MesquitePopup popup = new MesquitePopup(this);
								hN.addToBrowserPopup(popup);
								popup.showPopup(0, getTopOfRow(row) + getColumnNamesRowHeight() + 20);						
								return;
							}
						}
							
					}
					else if (a instanceof Vector){
					}
					else if (a instanceof Taxa){
					}
					else if (a instanceof CharacterData){
					}
					//if not returned by now, then use default broadcast
					cellRightClicked(row, -1, editorPanel, x, y, modifiers); 
					
				}
				if (clickCount>1 && isRowNameEditable(row)){
					window.setCurrentTool(window.ibeamTool);
					window.getPalette().setCurrentTool(window.ibeamTool); 
					((TableTool)window.getCurrentTool()).cellTouched(-1, row, editorPanel, x, y, modifiers);
				}
				else {
					super.rowNameTouched(row,  editorPanel, x, y, modifiers,clickCount);
				}
			}
		}
		else
		   ((TableTool)window.getCurrentTool()).cellTouched(-1, row, editorPanel, x, y, modifiers);
		 
	}
	public void superRowNameTouched(int row, EditorPanel editorPanel, int x, int y, int modifiers, int clickCount){
		super.rowNameTouched(row, editorPanel, x, y, modifiers,clickCount);
	}
	/*...............................................................................................................*/
	public boolean getDropDown(int column, int row){
		if (row != -1)
			return false;
		if (column==-1)
			return super.getDropDown(column, row);
		ListAssistant assistant = window.findAssistant(column);
		if (assistant!=null)
			return assistant.needsMenu();
		else
			return false;
	}
	public boolean touchColumnNameEvenIfSelected(){
		return true;
	}
	/*...............................................................................................................*/
	public void columnNameTouched(int column, EditorPanel editorPanel, int x, int y, int modifiers, int clickCount) {
		window.setAnnotation("", null);
		ListAssistant assistant = window.findAssistant(column);
		if (assistant!=null) {
			assistant.showPopUp(getColumnNamesPanel(), getLeftOfColumn(column), getColumnNamesRowHeight()+8);
		}
	}
	/*...............................................................................................................*/
	/** Called if corner panel is touched.  Can be overridden in subclasses to respond to touch.*/
   	public void cornerTouched(int x, int y, int modifiers) {
		window.setAnnotation("", null);
 		if (x> getRowNamesWidth()-20)
 			window.showSelectionPopup(cornerCell, x, y);
  		else
  			super.cornerTouched(x,y,modifiers);
   	}
	/*...............................................................................................................*/
	public void mouseInCell(int modifiers, int column,int subColumn, int row, int subRow, MesquiteTool tool){
		if (row == -1 && column>=0){ //column names
			ListAssistant assistant = window.findAssistant(column);
			if (assistant !=null){
				String ex = assistant.getName() + "\n" + assistant.getExplanation();
				String par = assistant.accumulateParameters("   ");
				if (!StringUtil.blank(par))
					ex += "\nParameters:\n" + par;
				window.setExplanation(ex);
			}
		}
		else if (row >=0 && column>=0){ //internal cell
			ListAssistant assistant = window.findAssistant(column);
			if (assistant !=null){
				String ex = assistant.getExplanationForRow(row);
				if (StringUtil.blank(ex))
					ex = assistant.getName();
				window.setExplanation(ex);
			}
		}
	}
	/*...............................................................................................................*/
	public void mouseExitedCell(int modifiers, int column,int subColumn,int row, int subRow,MesquiteTool tool){
		window.setAnnotation(null);
		if (row == -1 && column>=0){//column names
				window.setExplanation("");
		}
	}
	/*...............................................................................................................*/
	/** Returns text in row name.  */
	public String getColumnNameText(int column){
		ListAssistant assistant = window.findAssistant(column);
		if (assistant!=null)
			return assistant.getTitle();
		return "";
	}
	/*...............................................................................................................*/
	/** Returns text in row name.  */
	public String getRowNameText(int row){
			return window.getRowName(row);
	}
	/*...............................................................................................................*/
	public void returnedRowNameText(int row, String s, boolean update){
		if (s!=null && !s.equals(window.getRowName(row))) {
			String oldName = window.getRowName(row);
			window.setRowName(row, s, update);
			window.setUndoer(ownerModule.getSingleNameUndoInstructions(row,oldName, s));
		}
		
	}
	public void rowNamesReturned() {
		if (window.owner.resetMenusOnNameChange()){
			window.resetAllTitles();
			window.owner.resetAllMenuBars();
		}
	}
	/*...............................................................................................................*/
	public void returnedMatrixText(int column, int row, String s){
		if (s!=null){
			ListAssistant assistant = window.findAssistant(column);
			if (assistant!=null)
				assistant.setString(  row, s);
		}
	}
}

/* ======================================================================== */
class LTCellAnnotation implements Annotatable {
	ListModule ownerModule;
	int row;
	ListWindow window;
	public LTCellAnnotation(ListModule ownerModule, ListWindow window) {
		this.ownerModule = ownerModule;
		this.window = window;
	}
	
	void setRow(int r){
		row = r;
	}
	public String getName(){
		if (ownerModule == null)
			return "";
		 return ownerModule.getItemTypeName() + " \"" + window.getRowName(row) + "\"" ;
	}
	public String getAnnotation(){
		if (ownerModule == null)
			return null;
		return ownerModule.getAnnotation(row);
	}
	public void setAnnotation(String s, boolean notify){
		if (ownerModule == null)
			return;
		ownerModule.setAnnotation(row, s, notify);
	}
 }


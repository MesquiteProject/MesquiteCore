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
import java.util.*;
import java.awt.event.*;
import java.awt.datatransfer.*;

import javax.swing.text.JTextComponent;

import pal.math.MathUtils;
import mesquite.lib.*;
import mesquite.lib.duties.FileInterpreter;

/* ======================================================================== */
/**
 * A base class to be extended for various spreadsheet-like tables. It provides very basic services like shaping the components, drawing the grid, fielding scrolling events and mousedowns, and provides hooks for superclasses to fill the cells. It is expected to be used for the type editor simple text charts, character data editor and list windows. Almost all of the burden of the memory design and management of the cells, rows, columns, etc. is left to the superclasses.
 * <p>
 * 
 * The table is composed of 6 panels: the column for the row names (e.g., "from" state names in type editor or taxon names in data editor), the row for the column names (e.g., "to" state names in type editor or character names in data editor), the little corner between them in the upper left, the main block of cells (the scrollable matrix), and the two scroll bars for the matrix.
 * <p>
 * 
 * Currently there are not too many settable parameters to customize the appearance of the table, but in the future it should be made possible to customize the table's appearance
 */
public class MesquiteTable extends MesquitePanel implements KeyListener, MouseWheelListener {
	protected ColumnNamesPanel columnNames = null;
	protected RowNamesPanel rowNames = null;
	protected MatrixPanel matrix;
	protected CornerPanel cornerCell;
	protected ControlStrip controlStrip;

	public TableMarchingAnts marchingAnts;
	static final int AUTOSCROLLBOUNDARY = 4;


	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int CENTERED = 2;

	protected boolean columnNamesCopyPaste = true;
	protected boolean rowNamesCopyPaste = true;
	int baseRowHeight = 16;
	//	int thinRowHeight=22;
	int baseColumnWidth = 16;
	int nameStartOffset = 5;
	int focusColumn = -2;
	int focusRow = -2;

	int justification = CENTERED;
	private Cursor handCursor;
	private Cursor eastResizeCursor;
	private Cursor northResizeCursor;

	TableScroll horizScroll;
	TableScroll vertScroll;

	Rectangle messageBox; // just to left of horizScroll

	int firstRowVisible = 0; // topmost row with current y scroll position
	int firstColumnVisible = 0; // leftmost column with current horiz scroll position
	int numRowsVisible = 0;
	int numColumnsVisible = 0;
	int lastRowVisible = 0; // bottom row visible, even if a partial row
	int lastColumnVisible = 0; // rightmost column visible, even if partial column

	public int numRowsTotal; // TODO: why is this used as if numRowsVisible?????
	public int numColumnsTotal;

	int columnGrabberWidth = 8;
	int rowGrabberWidth = 8;
	int columnNamesRowHeight = 20;
	int rowNamesWidth = 50;
	int matrixWidth;
	int matrixHeight;
	int[] columnWidths;
	int[] rowHeights;

	public MesquiteBoolean tight=new MesquiteBoolean(false);
	public MesquiteBoolean thinRows=new MesquiteBoolean(false);

	Font oldF = null;
	Font boldFont = null;
	public boolean frameRowNames = true;
	public boolean frameColumnNames = true;
	public boolean frameMatrixCells = true;
	public boolean paleGrid = false;
	public boolean boldCellText = false;
	public boolean paleCellColors = false;
	public boolean showRowGrabbers = false;
	public boolean showColumnGrabbers = false;
	public boolean showRowNumbers = false;
	public boolean showColumnNumbers = false;

	public boolean cornerIsHeading = false;
	protected boolean autosizeColumns = false;
	//	protected boolean autosizeRowNames = false;
	public MesquiteBoolean autoRowNameWidth = new MesquiteBoolean(false);

	private boolean cellsAutoEditable = false;
	private boolean rowNamesAutoEditable = false;
	private boolean columnNamesAutoEditable = false;
	private boolean cornerAutoEditable = false;
	private boolean cellsEditable = false;
	private boolean rowNamesEditable = false;
	private boolean columnNamesEditable = false;
	private boolean cornerEditable = false;
	private boolean cellsSelectable = true;
	private boolean rowNamesSelectable = true;
	private boolean columnNamesSelectable = true;
	private boolean rowsSelectable = true;
	private boolean columnsSelectable = true;
	private boolean cornerSelectable = false;

	private boolean quickMode = false;

	public static final int NOADJUST = 0; // these constants refer to column and row user-adjust -- INSERT or RESIZE if interstitial space pulled
	public static final int RESIZE = 1;
	public static final int INSERT = 2;
	private int userAdjustColumn = NOADJUST;
	private boolean userMoveColumn = false;
	private int userAdjustRow = NOADJUST;
	private boolean userMoveRow = false;
	public boolean adjustingColumnWidth = false;
	public boolean adjustingColumnNamesHeight = false;

	private Bits[] rowsSelected;
	private Bits[] columnsSelected;
	private Bits[] cellsSelected;
	private Bits[] columnNamesSelected;
	private Bits[] rowNamesSelected;
	private boolean[] cornerSelected;

	public static final int BETWEENLINEWIDTH = 4;
	private int startBetweenRowSelection = MesquiteInteger.unassigned;
	private int endBetweenRowSelection = MesquiteInteger.unassigned;
	private int startBetweenColumnSelection = MesquiteInteger.unassigned;
	private int endBetweenColumnSelection = MesquiteInteger.unassigned;


	private int numSelectTypes = 3; // 0 = selection; 1 = dimming; 2 = dropdown menu
	Bits rowWidthsAdjusted;
	Bits columnWidthsAdjusted;
	boolean rowNamesWidthAdjusted = false;
	private Associable columnAssociable = null;
	private Associable rowAssociable = null;
	protected int colorScheme;

	MesquiteCommand pasteCommand = MesquiteModule.makeCommand("paste", this);
	MesquiteCommand cutCommand = MesquiteModule.makeCommand("cut", this);
	MesquiteCommand clearCommand = MesquiteModule.makeCommand("clear", this);
	MesquiteCommand copyCommand = MesquiteModule.makeCommand("copy", this);
	MesquiteCommand copyLiteralCommand = MesquiteModule.makeCommand("copyLiteral", this);
	MesquiteCommand selectAllCommand = MesquiteModule.makeCommand("selectAll", this);
	boolean showRowNames = true;


	public static MesquiteTimer tableTime;

	static {
		tableTime = new MesquiteTimer();
	}

	public MesquiteTable(int numRowsTotal, int numColumnsTotal, int totalWidth, int totalHeight, int rowNamesWidth, int colorScheme, boolean showRowNumbers, boolean showColumnNumbers) {
		super();
		tableTime.start();
		this.colorScheme = colorScheme;
		this.numRowsTotal = numRowsTotal;
		this.numColumnsTotal = numColumnsTotal;
		this.rowNamesWidth = rowNamesWidth;
		this.showRowNumbers = showRowNumbers;
		this.showColumnNumbers = showColumnNumbers;
		if (showRowNumbers)
			setRowGrabberWidth(24);
		if (showColumnNumbers)
			setColumnGrabberWidth(14);
		matrixWidth = totalWidth - rowNamesWidth - 16 - rowGrabberWidth;
		matrixHeight = totalHeight - columnNamesRowHeight - 16 - columnGrabberWidth;
		addMouseWheelListener(this);

		columnWidths = new int[numColumnsTotal];
		rowWidthsAdjusted = new Bits(numRowsTotal);
		columnWidthsAdjusted = new Bits(numColumnsTotal);
		setColumnWidthsUniform(baseColumnWidth);
		rowHeights = new int[numRowsTotal];
		setRowHeightsUniform(baseRowHeight);
		//		setRowHeightsUniform(thinRowHeight);


		rowsSelected = new Bits[numSelectTypes];
		columnsSelected = new Bits[numSelectTypes];
		cellsSelected = new Bits[numSelectTypes];
		columnNamesSelected = new Bits[numSelectTypes];
		rowNamesSelected = new Bits[numSelectTypes];
		cornerSelected = new boolean[numSelectTypes];
		for (int i = 0; i < numSelectTypes; i++) {
			rowsSelected[i] = new Bits(numRowsTotal);
			columnsSelected[i] = new Bits(numColumnsTotal);
			cellsSelected[i] = new Bits((numRowsTotal) * (numColumnsTotal));
			columnNamesSelected[i] = new Bits(numColumnsTotal);
			rowNamesSelected[i] = new Bits(numRowsTotal);
			cornerSelected[i] = false;
		}

		setLayout(null);
		handCursor = new Cursor(Cursor.HAND_CURSOR);
		eastResizeCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
		northResizeCursor = new Cursor(Cursor.N_RESIZE_CURSOR);

		rowNames = new RowNamesPanel(this, rowNamesWidth + rowGrabberWidth, matrixHeight);
		add(rowNames);

		columnNames = new ColumnNamesPanel(this, matrixWidth, columnNamesRowHeight + columnGrabberWidth);
		add(columnNames);

		matrix = new MatrixPanel(this, matrixWidth, matrixHeight);
		add(matrix);

		cornerCell = new CornerPanel(this, rowNamesWidth - 1 + rowGrabberWidth, columnNamesRowHeight - 1 + columnGrabberWidth);
		add(cornerCell);

		horizScroll = new TableScroll(this, Scrollbar.HORIZONTAL, 0, 0, 0, numColumnsTotal);
		add(horizScroll);

		controlStrip = new ControlStrip(colorScheme);
		add(controlStrip);
		// controlStrip.setBackground(Color.green);

		vertScroll = new TableScroll(this, Scrollbar.VERTICAL, 0, 0, 0, numRowsTotal);
		add(vertScroll);
		rowNames.setVisible(true);
		columnNames.setVisible(true);
		setSize(totalWidth, totalHeight);
		matrix.setVisible(true);
		cornerCell.setVisible(true);
		horizScroll.setVisible(true);
		controlStrip.setVisible(true);
		vertScroll.setVisible(true);
		tableTime.end();
		// addKeyListener(this);
	}

	/** Return Mesquite commands that will put the table back to its current state (approximately). */
	public Snapshot getSnapshot(MesquiteFile file) { // this allows employees to be dealt with
		if (columnWidthsAdjusted.anyBitsOn() || rowNamesWidthAdjusted) {
			Snapshot temp = new Snapshot();
			if (rowNamesWidthAdjusted)
				temp.addLine("rowNamesWidth " + rowNamesWidth);
			for (int i = 0; i < numColumnsTotal; i++) {
				if (columnWidthsAdjusted.isBitOn(i))
					temp.addLine("columnWidth " + i + " " + getColumnWidth(i));
			}
			return temp;
		}
		else
			return null;
	}

	private String getSelectedText(TextField edit) {
		int selStart = edit.getSelectionStart();
		if (edit.getSelectionStart() < 0)
			return null;
		int selEnd = edit.getSelectionEnd();
		String text = edit.getText();
		if (text == null)
			return null;

		String copied = text.substring(selStart, selEnd);
		return copied;
	}

	/* ................................................................................................................. */
	public void adjustMarchingAnts(){
		if (marchingAnts!=null)
			marchingAnts.moveAnts();
	}

	/* ................................................................................................................. */
	public void cancelMarchingAnts(){
		if (marchingAnts!=null) {
			marchingAnts.cancel();
			marchingAnts=null;
		}
	}

	/* ................................................................................................................. */
	public void startMarchingAnts (int column1, int row1, int column2, int row2) {
		marchingAnts = new TableMarchingAnts(this, getMatrixPanel().getGraphics(),  column1, row1, column2, row2);
	}
	/* ................................................................................................................. */

	/* ................................................................................................................. */

	/**
	 * A request for the MesquiteModule to perform a command. It is passed two strings, the name of the command and the arguments. This should be overridden by any module that wants to respond to a command.
	 */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(MesquiteTable.class, "Copies current selection to clipboard", null, commandName, "copy")) {
			if (matrix.getEditing() || rowNames.getEditing() || columnNames.getEditing()) {
				TextField edit = null;
				if (matrix.getEditing())
					edit = matrix.getEditField();
				else if (rowNames.getEditing())
					edit = rowNames.getEditField();
				else if (columnNames.getEditing())
					edit = columnNames.getEditField();
				if (edit != null && edit.hasFocus()) {
					String copied = getSelectedText(edit);
					if (copied == null)
						return null;
					Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(copied);
					clip.setContents(ss, ss);
					return null;
				}
			}
			else {
				StringBuffer sb = new StringBuffer(100);
				copyCells(sb, false);
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(sb.toString());
				clip.setContents(ss, ss);
			}
		}
		else if (checker.compare(MesquiteTable.class, "Key pressed", null, commandName, "enterPressed")) {
			enterPressed(arguments);
		}
		else if (checker.compare(MesquiteTable.class, "Key pressed", null, commandName, "tabPressed")) {
			tabPressed(arguments);
		}
		else if (checker.compare(MesquiteTable.class, "Key pressed", null, commandName, "rightArrowPressed")) {
			rightArrowPressed(arguments);
		}
		else if (checker.compare(MesquiteTable.class, "Key pressed", null, commandName, "leftArrowPressed")) {
			leftArrowPressed(arguments);
		}
		else if (checker.compare(MesquiteTable.class, "Key pressed", null, commandName, "upArrowPressed")) {
			upArrowPressed(arguments);
		}
		else if (checker.compare(MesquiteTable.class, "Key pressed", null, commandName, "downArrowPressed")) {
			downArrowPressed(arguments);
		}
		else if (checker.compare(MesquiteTable.class, "Copies current selection to clipboard, literally (with full names)", null, commandName, "copyLiteral")) {
			if (matrix.getEditing() || rowNames.getEditing() || columnNames.getEditing()) {
				TextField edit = null;
				if (matrix.getEditing())
					edit = matrix.getEditField();
				else if (rowNames.getEditing())
					edit = rowNames.getEditField();
				else if (columnNames.getEditing())
					edit = columnNames.getEditField();
				if (edit != null && edit.hasFocus()) {
					String copied = getSelectedText(edit);
					if (copied == null)
						return null;
					Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(copied);
					clip.setContents(ss, ss);
					return null;
				}
			}
			else {
				StringBuffer sb = new StringBuffer(100);
				copyCells(sb, true);
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(sb.toString());
				clip.setContents(ss, ss);
			}
		}
		else if (checker.compare(MesquiteTable.class, "Copies current selection to clipboard and clears from selection", null, commandName, "cut")) {
			if (matrix.getEditing() || rowNames.getEditing() || columnNames.getEditing()) {
				TextField edit = null;
				if (matrix.getEditing())
					edit = matrix.getEditField();
				else if (rowNames.getEditing())
					edit = rowNames.getEditField();
				else if (columnNames.getEditing())
					edit = columnNames.getEditField();
				if (edit != null && edit.hasFocus()) {
					String copied = getSelectedText(edit);
					if (copied == null)
						return null;
					String oldText = edit.getText();
					String newText = oldText.substring(0, edit.getSelectionStart()) + oldText.substring(edit.getSelectionEnd(), oldText.length());
					edit.setText(newText);
					Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(copied);
					clip.setContents(ss, ss);
				}
			}
			else {
				StringBuffer sb = new StringBuffer(100);
				copyCells(sb, false);
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(sb.toString());
				clip.setContents(ss, ss);
				clearIt(true);
				repaintAll();
			}
		}
		else if (checker.compare(MesquiteTable.class, "Clears current selection", null, commandName, "clear")) {
			if (matrix.getEditing() || rowNames.getEditing() || columnNames.getEditing()) {
				TextField edit = null;
				if (matrix.getEditing())
					edit = matrix.getEditField();
				else if (rowNames.getEditing())
					edit = rowNames.getEditField();
				else if (columnNames.getEditing())
					edit = columnNames.getEditField();
				if (edit != null && edit.hasFocus()) {
					String text = edit.getText();
					String newText = text.substring(0, edit.getSelectionStart()) + text.substring(edit.getSelectionEnd(), text.length());
					edit.setText(newText);
					return null;
				}
			}
			else {
				MesquiteWindow ww = MesquiteWindow.windowOfItem(this);
				if (ww.componentWithFocus() != null){
					Component c = ww.componentWithFocus();
					if (c instanceof TextComponent){
						TextComponent tc = (TextComponent)c;
						if (tc.isEditable()){
							tc.setText("");
						}
						return null;
					}
					else if (c instanceof JTextComponent){
						JTextComponent tc = (JTextComponent)c;
						if (tc.isEditable()){
							tc.setText("");
						}
						return null;
					}

				}
			}

			clearIt(false);
			repaintAll();

		}
		else if (checker.compare(MesquiteTable.class, "Selects all of table, columns, rows, cells, or names, depending on current selection", null, commandName, "selectAll")) {
			if (matrix.getEditing() || rowNames.getEditing() || columnNames.getEditing()) {
				TextField edit = null;
				if (matrix.getEditing())
					edit = matrix.getEditField();
				else if (rowNames.getEditing())
					edit = rowNames.getEditField();
				else if (columnNames.getEditing())
					edit = columnNames.getEditField();
				if (edit != null && edit.hasFocus()) {
					edit.selectAll();
					return null;
				}
			}
			else {
				boolean selectA = (!anythingSelected());

				if (cellsSelectable && (selectA || anyCellSelected()))
					selectBlock(0, 0, numColumnsTotal - 1, numRowsTotal - 1);
				if (rowsSelectable && (selectA || anyRowSelected())) {
					selectRows(0, numRowsTotal - 1);
					if (rowAssociable != null)
						rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
				if (columnsSelectable && (selectA || anyColumnSelected())) {
					selectColumns(0, numColumnsTotal - 1);
					if (columnAssociable != null)
						columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}
				if (rowNamesSelectable && (selectA || anyRowNameSelected()))
					selectRowNames(0, numRowsTotal - 1);
				if (columnNamesSelectable && (selectA || anyColumnNameSelected()))
					selectColumnNames(0, numColumnsTotal - 1);
				repaintAll();
				/*
				 * if (cornerSelectable) selectCorner;
				 */
			}
		}
		else if (checker.compare(MesquiteTable.class, "Pastes from clipboard into current selection", null, commandName, "paste")) {
			MesquiteWindow ww = MesquiteWindow.windowOfItem(this);
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = clip.getContents(this);
			try {
				String s = (String) t.getTransferData(DataFlavor.stringFlavor);
				if (s != null) {
					if (matrix.getEditing() || rowNames.getEditing() || columnNames.getEditing()) {
						TextField edit = null;
						if (matrix.getEditing())
							edit = matrix.getEditField();
						else if (rowNames.getEditing())
							edit = rowNames.getEditField();
						else if (columnNames.getEditing())
							edit = columnNames.getEditField();
						if (edit != null && edit.hasFocus()) {
							String text = edit.getText();
							String newText = text.substring(0, edit.getSelectionStart()) + s + text.substring(edit.getSelectionEnd(), text.length());
							edit.setText(newText);
							return newText;
						}
					}
					else if (ww.componentWithFocus() != null){
						Component c = ww.componentWithFocus();
						if (c instanceof TextComponent){
							TextComponent tc = (TextComponent)c;
							if (tc.isEditable()){
								String text = tc.getText();
								String newText = text.substring(0, tc.getSelectionStart()) + s + text.substring(tc.getSelectionEnd(), text.length());
								tc.setText(newText);
							}
							return s;
						}
						else if (c instanceof JTextComponent){
							JTextComponent tc = (JTextComponent)c;
							if (tc.isEditable()){
								String text = tc.getText();
								String newText = text.substring(0, tc.getSelectionStart()) + s + text.substring(tc.getSelectionEnd(), text.length());
								tc.setText(newText);
							}
							return s;
						}

					}
					if (clipboardDimensionsFit(s)) {
						pasteIt(s);
						repaintAll();
					}

					else if (anythingSelected()) {
						MesquiteWindow frame = MesquiteTrunk.mesquiteTrunk.containerOfModule();
						if (ww != null) {
							MesquiteModule mm = ww.getOwnerModule();
							if (mm != null) {
								frame = mm.containerOfModule();
							}
						}
						if (AlertDialog.query(frame, "Paste shape mismatch", "Sorry, the number of lines and of items pasted don't match the spaces selected to be filled.  Would you like Mesquite to attempt to adjust the selected region so that you can paste?", "OK", "Cancel", 1))
							setSelectionToShape(getTabbedLines(s));

						return null;
					}
				}
			} catch (Exception e) {
				MesquiteMessage.printStackTrace(e);
			}
		}
		else if (checker.compare(MesquiteTable.class, "Sets width of the column containing the row names", "[width]", commandName, "rowNamesWidth")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int width = MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(width)) {
				setRowNamesWidth(width);
				resetComponentSizes();
				rowNamesWidthAdjusted = true;
			}

		}
		else if (checker.compare(MesquiteTable.class, "Sets width of a column", "[column][width]", commandName, "columnWidth")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column = MesquiteInteger.fromString(arguments, io);
			int width = MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(column) && MesquiteInteger.isCombinable(width)) {
				if (column >= 0 && column < numColumnsTotal) {
					setColumnWidth(column, width);
					columnWidthsAdjusted.setBit(column);
				}
			}

		}
		else if (checker.compare(MesquiteTable.class, "Edits cell", "[column][row]", commandName, "editCell")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column = MesquiteInteger.fromString(arguments, io);
			int row = MesquiteInteger.fromString(arguments, io);
			boolean doRepaint = (anyRowColumnSelected());
			deselectAllNotify();
			if (column < 0)
				editRowNameCell(row);
			else if (row < 0)
				editColumnNameCell(column);
			else
				editMatrixCell(column, row);
			if (doRepaint)
				repaintAll();
		}
		else if (checker.compare(MesquiteTable.class, "Returns column as string", "[column]", commandName, "getColumn")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column = MesquiteInteger.fromString(arguments, io);
			if (column > 0 && column < getNumColumns()){
				StringBuffer sb = new StringBuffer(getColumnNameText(column));
				for (int i=0; i<getNumRows(); i++){
					sb.append('\t');
					sb.append(getMatrixText(column, i));
				}
				return sb.toString();
			}
		}
		else if (checker.compare(MesquiteTable.class, "Returns row as string", "[row]", commandName, "getRow")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int row = MesquiteInteger.fromString(arguments, io);
			if (row > 0 && row < getNumRows()){
				StringBuffer sb = new StringBuffer(getRowNameText(row));
				for (int i=0; i<getNumColumns(); i++){
					sb.append('\t');
					sb.append(getMatrixText(i, row));
				}
				return sb.toString();
			}
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/* ................................................................................................................. */
	public boolean selectedBetweenRows() {
		return (getBetweenSelected() && startBetweenRowSelection==endBetweenRowSelection);
	}
	/* ................................................................................................................. */
	public boolean selectedBetweenColumns() {
		return (getBetweenSelected() && startBetweenColumnSelection==endBetweenColumnSelection);
	}
	/* ................................................................................................................. */
	public int betweenColumn() {
		return startBetweenColumnSelection;
	}
	/* ................................................................................................................. */
	public int betweenRow() {
		return startBetweenRowSelection;
	}
	/* ................................................................................................................. */

	public boolean inBetweenSelectionRowColumns( int column, int row) {
		int buff = 2;
		if (selectedBetweenColumns())
			return row>=getStartBetweenRowSelection() && row <= getEndBetweenRowSelection() && (column == getStartBetweenColumnSelection()) ;
			else if (selectedBetweenRows())
				return column>=getStartBetweenColumnSelection() && column <= getEndBetweenColumnSelection()  && (row == getStartBetweenRowSelection()) ;
				return false;
	}
	/* ................................................................................................................. */

	public boolean inBetweenSelection( int column, int row, int regionInCellH, int regionInCellV) {
		int buff = 2;
		if (selectedBetweenColumns())
			return (row>=getStartBetweenRowSelection() && row <= getEndBetweenRowSelection()) && ((regionInCellH>80 && column == getStartBetweenColumnSelection()) || (regionInCellH<20 && column == getStartBetweenColumnSelection()+1)) ;
		else if (selectedBetweenRows())
			return column>=getStartBetweenColumnSelection() && column <= getEndBetweenColumnSelection() && (regionInCellV>80||regionInCellV<20) && (row == getStartBetweenRowSelection()) ;
			return false;
	}
	/* ................................................................................................................. */

	public boolean inBetweenSelection(int x, int y) {
		int buff = 2;
		if (selectedBetweenColumns())
			return (x>= getColumnX(getStartBetweenColumnSelection()-buff - MesquiteTable.BETWEENLINEWIDTH)) && (x<= getColumnX(getEndBetweenColumnSelection()+buff + MesquiteTable.BETWEENLINEWIDTH)) && (y>= getRowY(getStartBetweenRowSelection()-buff)) && (y<= getRowY(getEndBetweenRowSelection()+buff));
		else if (selectedBetweenRows())
			return (x>= getColumnX(getStartBetweenColumnSelection()-buff)) && (x<= getColumnX(getEndBetweenColumnSelection()+buff)) && (y>= getRowY(getStartBetweenRowSelection()-buff - MesquiteTable.BETWEENLINEWIDTH)) && (y<= getRowY(getEndBetweenRowSelection()+buff+MesquiteTable.BETWEENLINEWIDTH));
		return false;
	}
	/* ................................................................................................................. */
	public boolean getBetweenSelected() {
		boolean assigned = MesquiteInteger.isCombinable(startBetweenRowSelection)&&MesquiteInteger.isCombinable(endBetweenRowSelection)&&MesquiteInteger.isCombinable(startBetweenColumnSelection)&&MesquiteInteger.isCombinable(endBetweenColumnSelection);
		if (!assigned)
			return false;
		boolean inRange =  (startBetweenRowSelection>=0 && startBetweenRowSelection<=getNumRows()) &&  (endBetweenRowSelection>=0 && endBetweenRowSelection<=getNumRows()) &&  (startBetweenColumnSelection>=0 && startBetweenColumnSelection<=getNumColumns()) &&  (endBetweenColumnSelection>=0 && endBetweenColumnSelection<=getNumColumns());
		if (!inRange)
			return false;
		return (Math.abs(startBetweenRowSelection-endBetweenRowSelection)>=0 || Math.abs(startBetweenColumnSelection-endBetweenColumnSelection)>=0);
	}

	// public static final int GRABBERS = 8;
	/* ................................................................................................................. */
	public int getRowGrabberWidth() {
		return rowGrabberWidth;
	}

	/* ................................................................................................................. */
	public void setRowGrabberWidth(int grabberWidth) {
		this.rowGrabberWidth = grabberWidth;
	}
	public void setColumnNamesDiagonal(boolean diagonal){
		columnNames.setDiagonal(diagonal);
	}
	/* ................................................................................................................. */
	public void setColumnNamesCopyPaste(boolean copyPastable) {
		this.columnNamesCopyPaste = copyPastable;
	}

	/* ................................................................................................................. */
	public void setRowNamesCopyPaste(boolean copyPastable) {
		this.rowNamesCopyPaste = copyPastable;
	}

	/* ................................................................................................................. */
	public int getColumnGrabberWidth() {
		return columnGrabberWidth;
	}

	/* ................................................................................................................. */
	public void setColumnGrabberWidth(int grabberWidth) {
		this.columnGrabberWidth = grabberWidth;
	}

	/* ................................................................................................................. */
	public String getColumnComment(int column) {
		return null;
	}

	/* ................................................................................................................. */
	public String getRowComment(int row) {
		return null;
	}

	/* ................................................................................................................. */
	protected void clearIt(boolean cut) {
		for (int i = 0; i < numColumnsTotal; i++) {
			if (isColumnNameSelected(i) || isColumnSelected(i)) {
				if (columnNamesEditable)
					returnedColumnNameText(i, null);
			}
		}
		for (int j = 0; j < numRowsTotal; j++) {
			if (isRowNameSelected(j) || isRowSelected(j)) {
				if (rowNamesEditable)
					returnedRowNameText(j, null);
			}
			for (int i = 0; i < numColumnsTotal; i++) {
				if (isCellSelected(i, j) || isRowSelected(j) || isColumnSelected(i)) {
					if (isCellEditable(i, j))
						returnedMatrixText(i, j, null);
				}
			}
		}
	}

	/* ................................................................................................................. */
	protected void OLDpasteIt(String s) {
		int count = 0;
		MesquiteInteger pos = new MesquiteInteger(0);
		if (columnNamesCopyPaste) {
			for (int i = 0; i < numColumnsTotal; i++) {
				if (isColumnNameSelected(i) || isColumnSelected(i)) {
					if (columnNamesEditable) {
						String t = StringUtil.getNextTabbedToken(s, pos);
						if (t != null)
							returnedColumnNameText(i, t);
					}
					count++;
				}
			}
		}
		for (int j = 0; j < numRowsTotal; j++) {
			if (rowNamesCopyPaste && (isRowNameSelected(j) || isRowSelected(j))) {
				if (rowNamesEditable) {
					String t = StringUtil.getNextTabbedToken(s, pos);
					if (t != null)
						returnedRowNameText(j, t);
				}
				count++;
			}
			for (int i = 0; i < numColumnsTotal; i++) {
				if (isCellSelected(i, j) || isRowSelected(j) || isColumnSelected(i)) {
					if (isCellEditable(i, j)) {
						String t = StringUtil.getNextTabbedToken(s, pos);
						if (t != null)
							returnedMatrixText(i, j, t);
					}
					count++;
				}
			}
		}
	}

	/* ................................................................................................................. */
	protected void pasteIt(String s) {
		int count = 0;
		MesquiteInteger pos = new MesquiteInteger(0);
			for (int i = 0; i < numColumnsTotal; i++) {
				if (isColumnNameSelected(i) || isColumnSelected(i)) {
						String t = StringUtil.getNextTabbedToken(s, pos);
						if (t != null && columnNamesEditable && columnNamesCopyPaste)
							returnedColumnNameText(i, t);
					count++;
				}
			}
		
		for (int j = 0; j < numRowsTotal; j++) {
			if (isRowNameSelected(j) || isRowSelected(j)) {
					String t = StringUtil.getNextTabbedToken(s, pos);
					if (t != null && rowNamesEditable && rowNamesCopyPaste)
						returnedRowNameText(j, t);
				count++;
			}
			for (int i = 0; i < numColumnsTotal; i++) {
				if (isCellSelected(i, j) || isRowSelected(j) || isColumnSelected(i)) {
						String t = StringUtil.getNextTabbedToken(s, pos);
						if (t != null && isCellEditable(i, j))
							returnedMatrixText(i, j, t);
					count++;
				}
			}
		}
	}

	/* ................................................................................................................. */
	public void copyIt(StringBuffer s, boolean literal) {
		copyIt(s, literal, true, true);
	}

	/* ................................................................................................................. */
	public void copyIt(StringBuffer s, boolean literal, boolean copyInsertTabs, boolean copyInsertReturns) {
		if (s == null)
			return;
		int count = 0;
		boolean firstInLine = true;
		boolean allSelected = true;
		boolean nothingSelected = !anythingSelected();
		if (columnNamesCopyPaste) {
			for (int i = 0; i < numColumnsTotal; i++) {
				if (nothingSelected || isColumnNameSelected(i) || isColumnSelected(i)) {
					if (!firstInLine)
						s.append('\t');
					firstInLine = false;
					String t = null;
					if (literal)
						t = getColumnNameTextForDisplay(i);
					else
						t = getColumnNameText(i);
					if (t != null)
						s.append(t);
				}
				else
					allSelected = false;
			}
		}
		else
			allSelected = false;
		if (!firstInLine)
			s.append(StringUtil.lineEnding());
		boolean previousWasRowName = false;
		for (int j = 0; j < numRowsTotal; j++) {
			firstInLine = true;
			if (rowNamesCopyPaste) {
				if (nothingSelected || isRowNameSelected(j) || isRowSelected(j)) {
					String t = null;
					if (literal)
						t = getRowNameTextForDisplay(j);
					else
						t = getRowNameText(j);
					if (t != null)
						s.append(t);
					firstInLine = false;
					previousWasRowName = true;
				}
				else
					allSelected = false;
			}
			else
				allSelected = false;

			for (int i = 0; i < numColumnsTotal; i++) {
				if (nothingSelected || isCellSelected(i, j) || isRowSelected(j) || isColumnSelected(i)) {
					if (previousWasRowName || (!firstInLine && copyInsertTabs)){
						s.append('\t');
						previousWasRowName = false;
					}
					firstInLine = false;
					String t = null;
					if (literal)
						t = getMatrixTextForDisplay(i, j);
					else
						t = getMatrixText(i, j);
					if (t != null)
						s.append(t);
				}
				else
					allSelected = false;
			}
			if (!firstInLine && copyInsertReturns)
				s.append(StringUtil.lineEnding());
		}
		if (allSelected && copyInsertTabs) // absolutely everyting is selected; prepend tab for corner cell
			s.insert(0, '\t');

	}
	/* ................................................................................................................. */
	public void copyCells(StringBuffer s, boolean literal) {
		copyIt(s,literal,true, true);
	}

	/* ................................................................................................................. */
	protected boolean clipboardDimensionsFit(String s) {
		int[] fString = getTabbedLines(s);
		int[] fSelected = getSelectedSpaces();
		if (fString == null || fSelected == null || fString.length != fSelected.length)
			return false;
		for (int i = 0; i < fString.length; i++)
			if (fString[i] != fSelected[i]) {
				return false;
			}
		return true;
	}
	/* ................................................................................................................. */
	protected Point getTopLeftSelected(){
		if (anyColumnSelected() || anyRowSelected() || anyCellSelected()) {
			int firstRow = firstRowSelected();
			int firstColumn = firstColumnSelected();
			int top = -1;
			int left = -1;
			boolean found = false;
			for (int row = 0; row < numRowsTotal && !found; row++) { // row dominant choice of upper left
				for (int column = 0; column < numColumnsTotal && !found; column++) {
					if (isCellSelected(column, row)) {
						top = row;
						left = column;
						found = true;
					}
				}
			}
			if (firstRow >= 0 && (firstRow < top || top < 0))
				top = firstRow;
			if (firstColumn >= 0 && (firstColumn < left || left < 0))
				left = firstColumn;
			if (top < 0 && left >= 0)
				top = 0;
			if (left < 0 && top >= 0)
				left = 0;
			if (anyColumnSelected())
				top = -1;
			if (anyRowSelected())
				left = -1;

			return new Point(left, top);
		}
		else if (anyRowNameSelected()) {
			int first = rowNamesSelected[0].firstBitOn();
			return new Point(-1, first);
		}
		else if (anyColumnNameSelected()) {
			int first = columnNamesSelected[0].firstBitOn();
			return new Point(first, -1);
		}
		return new Point(-1, -1);
	}
	/* ................................................................................................................. */
	public void setSelectionToShape(int[] lines) {
		if (lines == null)
			return;
		if (anyColumnSelected() || anyRowSelected() || anyCellSelected()) {
			int firstRow = firstRowSelected();
			int firstColumn = firstColumnSelected();
			int top = -1;
			int left = -1;
			boolean found = false;
			for (int row = 0; row < numRowsTotal && !found; row++) { // row dominant choice of upper left
				for (int column = 0; column < numColumnsTotal && !found; column++) {
					if (isCellSelected(column, row)) {
						top = row;
						left = column;
						found = true;
					}
				}
			}
			if (firstRow >= 0 && (firstRow < top || top < 0))
				top = firstRow;
			if (firstColumn >= 0 && (firstColumn < left || left < 0))
				left = firstColumn;
			if (top < 0 && left >= 0)
				top = 0;
			if (left < 0 && top >= 0)
				left = 0;
			if (lines.length == numRowsTotal + 1) { // whole columns had been selected; do likewise
				for (int column = 0; column < lines[0]; column++)
					selectColumn(column + left);
				if (lines[0] == numColumnsTotal + 1) // whole rows had been selected; do likewise
					for (int row = 0; row < lines.length; row++)
						selectRow(row + top);

			}
			else if (lines[0] == numColumnsTotal + 1) { // whole rows had been selected; do likewise
				for (int row = 0; row < lines.length; row++) {
					selectRow(row + top);
				}
			}
			else {

				if (top >= 0 && left >= 0) {
					deselectAll();
					for (int row = 0; row < lines.length; row++)
						for (int column = 0; column < lines[row]; column++) {
							selectCell(column + left, row + top);
						}
				}
				if (columnAssociable != null && firstColumn >= 0)
					columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				if (rowAssociable != null && firstRow >= 0)
					rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
		else if (anyRowNameSelected()) {
			int first = rowNamesSelected[0].firstBitOn();
			for (int row = first; row < lines.length + first; row++) {
				selectRowName(row);
			}
		}
		else if (anyColumnNameSelected()) {
			if (lines.length == 1) {
				int first = columnNamesSelected[0].firstBitOn();
				for (int column = first; column < lines[0] + first; column++) {
					selectColumnName(column);

				}
			}
		}
		repaintAll();
	}

	/* ................................................................................................................. */
	protected int[] getSelectedSpaces() {

		// for each row, how many things are selected needs to be calculated
		// first, figure out how many rows have selections in them.
		int lines = 0;
		if ((anyColumnNameSelected() || anyColumnSelected()) && columnNamesCopyPaste)
			lines++;
		if (anyColumnSelected())
			lines += numRowsTotal;
		else
			for (int j = 0; j < numRowsTotal; j++) {
				if ((isRowNameSelected(j) && rowNamesCopyPaste) || isRowSelected(j) || anyCellInRowSelected(j))
					lines++;
			}

		// now, figure out how many items in each line
		int[] result = new int[lines];
		int line = 0;
		int numInLine = 0;
		for (int i = 0; i < numColumnsTotal; i++)
			if ((isColumnNameSelected(i) || isColumnSelected(i)) && columnNamesCopyPaste)
				numInLine++;
		if (numInLine > 0) {
			result[line] = numInLine;
			line++;
		}
		for (int j = 0; j < numRowsTotal; j++) {
			numInLine = 0;
			if (rowNamesCopyPaste && (isRowNameSelected(j) || isRowSelected(j))) // for name of taxon
				numInLine++;
			for (int i = 0; i < numColumnsTotal; i++)
				if (isCellSelected(i, j) || isRowSelected(j) || isColumnSelected(i))
					numInLine++;
			if (numInLine > 0) {
				result[line] = numInLine;
				line++;
			}
		}
		return result;
	}

	/* ................................................................................................................. */
	protected boolean anyCellInRowSelected(int row) {
		if (!rowLegal(row))
			return false;
		for (int i = 0; i < numColumnsTotal; i++) {
			if (isCellSelected(i, row))
				return true;
		}
		return false;
	}

	/* ................................................................................................................. */
	protected String showInvisibles(String s) {
		if (s == null)
			return null;
		String sNew = "";
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '\t')
				sNew += "<^t>";
			else if (s.charAt(i) == '\r')
				sNew += "<^r>";
			else if (s.charAt(i) == '\n')
				sNew += "<^n>";
			else
				sNew += s.charAt(i);
		return sNew;
	}

	public void keyTyped(KeyEvent e) {
	}


	public void keyPressed(KeyEvent e) {  
		if (!MesquiteWindow.belongsTo(e.getComponent(), this))
			return;
		if (e.getComponent() instanceof Scrollbar)
			return;
		String arguments = "";
		if (e.isShiftDown())
			arguments += " shift ";
		if (e.isAltDown())
			arguments += " option ";

		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			MesquiteCommand key = new MesquiteCommand("enterPressed", arguments, this);
			key.setSuppressLogging(true);
			key.doItMainThread(arguments, null, this);
		}
		else if (e.getKeyCode() == KeyEvent.VK_TAB){
			MesquiteCommand key = new MesquiteCommand("tabPressed", arguments, this);
			key.setSuppressLogging(true);
			key.doItMainThread(arguments, null, this);
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT){
			MesquiteCommand key = new MesquiteCommand("rightArrowPressed", arguments, this);
			key.setSuppressLogging(true);
			key.doItMainThread(arguments, null, this);
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			MesquiteCommand key = new MesquiteCommand("leftArrowPressed", arguments, this);
			key.setSuppressLogging(true);
			key.doItMainThread(arguments, null, this);
		}
		else if (e.getKeyCode() == KeyEvent.VK_UP){
			MesquiteCommand key = new MesquiteCommand("upArrowPressed", arguments, this);
			key.setSuppressLogging(true);
			key.doItMainThread(arguments, null, this);
		}
		else if (e.getKeyCode() == KeyEvent.VK_DOWN){
			MesquiteCommand key = new MesquiteCommand("downArrowPressed", arguments, this);
			key.setSuppressLogging(true);
			key.doItMainThread(arguments, null, this);
		}

	}

	public void keyReleased(KeyEvent e) {
	}

	private EditorTextField getEditField() {
		if (matrix.getEditing())
			return matrix.getEditField();
		if (columnNames.getEditing())
			return columnNames.getEditField();
		if (rowNames.getEditing())
			return rowNames.getEditField();
		return null;
	}

	private boolean getEditing() {
		return matrix.getEditing() || columnNames.getEditing() || rowNames.getEditing();
	}

	public void enterPressed(String arguments) {
		if (getEditing()) // let the edit box handle it
			return;
		// go to editing
		if (singleTableCellSelected()) {
			Dimension sel = getFirstTableCellSelected();
			if (sel.width >= -1 && sel.height >= -1) {
				if (sel.width == -1 && sel.height >= 0) { // rowNames
					if (isRowNameEditable(sel.height))
						rowNames.editCell(sel.width, sel.height);
				}
				else if (sel.width >= 0 && sel.height == -1) { // columnNames
					if (isColumnNameEditable(sel.width))
						columnNames.editCell(sel.width, sel.height);
				}
				else if (sel.width >= 0 && sel.height >= 0) { // within matrix
					if (isCellEditable(sel.width, sel.height))
						matrix.editCell(sel.width, sel.height);
				}
			}

		}
	}

	public void tabPressed(String arguments) {
		if (getEditing())
			return;
		rightArrowPressed(arguments);
	}

	public void downArrowPressed(String arguments) {
		if (getEditing())
			return;
		if (StringUtil.foundIgnoreCase(arguments, "shift")){
			if (anyRowSelected()){ //accumulateSelection
				if (StringUtil.foundIgnoreCase(arguments, "option")){ //select all the way to end
					int first = firstRowSelected();
					selectRows(first, getNumRows()-1);
					if (rowAssociable != null) 
						rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

					repaintAll();
				}
				else {
					for (int i = getNumRows()-1; i >=0; i--){
						if (isRowSelected(i) && !isRowSelected(i+1))
							selectRow(i+1);
					}
					if (rowAssociable != null) 
						rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					repaintAll();
				}
			}
			if (anyCellSelected()){
				if (StringUtil.foundIgnoreCase(arguments, "option")){
					for (int column = 0; column< getNumColumns(); column++){
						boolean doneColumn = false;
						for (int row = 0; row< getNumRows() && !doneColumn; row++)
							if (isCellSelected(column, row)){
								selectBlock(column, row, column, getNumRows()-1);
								doneColumn = true;
							}
					}
					repaintAll();
				}
				else {
					int firstRow = firstRowWithSelectedCell();
					int lastRow = lastRowWithSelectedCell();
					for (int row = lastRow; row>= firstRow; row--){
						for (int column = 0; column< getNumColumns(); column++)
							if (isCellSelected(column, row) && !isCellSelected(column, row+1))
								selectCell(column, row+1);
					}
					repaintAll();
				}
			}
			if (anyRowNameSelected()){
				if (StringUtil.foundIgnoreCase(arguments, "option")){ //select all the way to end
					int first = firstRowNameSelected();
					selectRowNames(first, getNumRows()-1);
					repaintAll();
				}
				else {
					for (int i = getNumRows()-1; i >=0; i--){
						if (isRowNameSelected(i) && !isRowNameSelected(i+1))
							selectRowName(i+1);
					}
					repaintAll();
				}
			}
		}
		else if (singleTableCellSelected()) {
			Dimension sel = getFirstTableCellSelected();
			if (sel.width >= -1 && sel.height >= -1) {
				if (sel.height + 1 < numRowsTotal) {
					deselectCell(sel.width, sel.height);
					selectCell(sel.width, sel.height + 1);
					redrawCell(sel.width, sel.height);
					redrawCell(sel.width, sel.height + 1);
					setFocusedCell(sel.width, sel.height + 1);
				}
				else
					MesquiteMessage.beep();
			}
			else
				MesquiteMessage.beep();

		}
	}

	public void upArrowPressed(String arguments) { //
		if (getEditing())
			return;
		if (StringUtil.foundIgnoreCase(arguments, "shift")){
			if (anyRowSelected()){ //accumulateSelection
				if (StringUtil.foundIgnoreCase(arguments, "option")){ //select all the way to end
					int last = lastRowSelected();
					selectRows(0, last);
					if (rowAssociable != null) 
						rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					repaintAll();
				}
				else {
					for (int i = 1; i <getNumRows(); i++){
						if (isRowSelected(i) && !isRowSelected(i-1))
							selectRow(i-1);
					}
					if (rowAssociable != null) 
						rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					repaintAll();
				}
			}
			if (anyCellSelected()){
				if (StringUtil.foundIgnoreCase(arguments, "option")){
					for (int column = 0; column< getNumColumns(); column++){
						boolean doneColumn = false;
						for (int row = getNumRows()-1; row>=0 && !doneColumn; row--)
							if (isCellSelected(column, row)){
								selectBlock(column, 0, column, row);
								doneColumn = true;
							}
					}
					repaintAll();
				}
				else {
					int firstRow = firstRowWithSelectedCell();
					int lastRow = lastRowWithSelectedCell();
					for (int row = firstRow; row<= lastRow; row++){
						if (row >0)
							for (int column = 0; column< getNumColumns(); column++)
								if (isCellSelected(column, row) && !isCellSelected(column, row-1))
									selectCell(column, row-1);
					}
					repaintAll();
				}
			}
			if (anyRowNameSelected()){
				if (StringUtil.foundIgnoreCase(arguments, "option")){ //select all the way to end
					int last = lastRowNameSelected();
					selectRowNames(0, last);
					repaintAll();
				}
				else {
					for (int i = 1; i <getNumRows(); i++){
						if (isRowNameSelected(i) && !isRowNameSelected(i-1))
							selectRowName(i-1);
					}
					repaintAll();
				}
			}
		}
		else if (singleTableCellSelected()) {
			Dimension sel = getFirstTableCellSelected();
			if (sel.width >= -1 && sel.height >= 0 && sel.height - 1 < numRowsTotal && (sel.width >= 0 || sel.height > 0)) {
				deselectCell(sel.width, sel.height);
				selectCell(sel.width, sel.height - 1);
				redrawCell(sel.width, sel.height);
				redrawCell(sel.width, sel.height - 1);
				setFocusedCell(sel.width, sel.height - 1);
			}
			else
				MesquiteMessage.beep();

		}
	}

	public void rightArrowPressed(String arguments) {
		if (getEditing())
			return;
		if (StringUtil.foundIgnoreCase(arguments, "shift")){
			if (anyColumnSelected()){ //accumulateSelection
				if (StringUtil.foundIgnoreCase(arguments, "option")){ //select all the way to end
					int first = firstColumnSelected();
					selectColumns(first, getNumColumns()-1);
					if (columnAssociable != null) 
						columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					repaintAll();
				}
				else {
					for (int i = getNumColumns()-1; i >=0; i--){
						if (isColumnSelected(i) && !isColumnSelected(i+1))
							selectColumn(i+1);
					}
					if (columnAssociable != null) 
						columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					repaintAll();
				}
			}
			if (anyCellSelected()){
				if (StringUtil.foundIgnoreCase(arguments, "option")){
					for (int row = 0; row< getNumRows(); row++){
						boolean doneRow = false;
						for (int column = 0; column< getNumColumns() && !doneRow; column++)
							if (isCellSelected(column, row)){
								selectBlock(column, row, getNumColumns()-1, row);
								doneRow = true;
							}
					}
					repaintAll();
				}
				else {
					int firstColumn = firstColumnWithSelectedCell();
					int lastColumn = lastColumnWithSelectedCell();
					for (int column = lastColumn; column>= firstColumn; column--){
						for (int row = 0; row< getNumRows(); row++)
							if (isCellSelected(column, row) && !isCellSelected(column+1, row))
								selectCell(column+1, row);
					}
					repaintAll();
				}
			}
			if (anyColumnNameSelected()){
				if (StringUtil.foundIgnoreCase(arguments, "option")){ //select all the way to end
					int first = firstColumnNameSelected();
					selectColumnNames(first, getNumColumns()-1);
					repaintAll();
				}
				else {
					for (int i = getNumColumns()-1; i >=0; i--){
						if (isColumnNameSelected(i) && !isColumnNameSelected(i+1))
							selectColumnName(i+1);
					}
					repaintAll();
				}
			}
		}
		else if (singleTableCellSelected()) {
			Dimension sel = getFirstTableCellSelected();
			if (sel.width >= -1 && sel.height >= -1) {
				if (sel.width + 1 < numColumnsTotal) {
					deselectCell(sel.width, sel.height);
					selectCell(sel.width + 1, sel.height);
					redrawCell(sel.width, sel.height);
					redrawCell(sel.width + 1, sel.height);
					setFocusedCell(sel.width + 1, sel.height);

				}
				else {
					MesquiteMessage.beep();
				}
			}
			else
				MesquiteMessage.beep();

		}
	}

	public void leftArrowPressed(String arguments) {
		if (getEditing())
			return;
		if (StringUtil.foundIgnoreCase(arguments, "shift")){

			if (anyColumnSelected()){ //accumulateSelection
				if (StringUtil.foundIgnoreCase(arguments, "option")){ //select all the way to end
					int last = lastColumnSelected();
					selectColumns(0, last);
					if (columnAssociable != null) 
						columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					repaintAll();
				}
				else {
					for (int i = 1; i <getNumColumns(); i++){
						if (isColumnSelected(i) && !isColumnSelected(i-1))
							selectColumn(i-1);
					}
					if (columnAssociable != null) 
						columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					repaintAll();
				}
			}
			if (anyCellSelected()){
				if (StringUtil.foundIgnoreCase(arguments, "option")){
					for (int row = 0; row< getNumRows(); row++){
						boolean doneRow = false;
						for (int column = getNumColumns()-1; column>=0 && !doneRow; column--)
							if (isCellSelected(column, row)){
								selectBlock(0, row, column, row);
								doneRow = true;
							}
					}
					repaintAll();
				}
				else {
					int firstColumn = firstColumnWithSelectedCell();
					int lastColumn = lastColumnWithSelectedCell();
					for (int column = firstColumn; column<= lastColumn; column++){
						if (column >0)
							for (int row = 0; row< getNumRows(); row++)
								if (isCellSelected(column, row) && !isCellSelected(column-1, row))
									selectCell(column-1, row);
					}
					repaintAll();
				}
			}
			if (anyColumnNameSelected()){
				if (StringUtil.foundIgnoreCase(arguments, "option")){ //select all the way to end
					int last = lastColumnNameSelected();
					selectColumnNames(0, last);
					repaintAll();
				}
				else {
					for (int i = 1; i <getNumColumns(); i++){
						if (isColumnNameSelected(i) && !isColumnNameSelected(i-1))
							selectColumnName(i-1);
					}
					repaintAll();
				}
			}
		}
		else if (singleTableCellSelected()) {
			Dimension sel = getFirstTableCellSelected();
			if (sel.width >= 0 && sel.height >= -1 && sel.width - 1 < numColumnsTotal && (sel.width > 0 || sel.height >= 0)) {
				deselectCell(sel.width, sel.height);
				selectCell(sel.width - 1, sel.height);
				redrawCell(sel.width, sel.height);
				redrawCell(sel.width - 1, sel.height);
				setFocusedCell(sel.width - 1, sel.height);
			}
			else
				MesquiteMessage.beep();

		}
	}

	public void defocusCell() {
		int oldFC = focusColumn;
		int oldFR = focusRow;
		focusColumn = -2;
		focusRow = -2;
		if (oldFR >= -1)
			rowNames.redrawCell(-1, oldFR);
		if (oldFC >= -1)
			columnNames.redrawCell(oldFC, -1);
		if (oldFR >= 0 && oldFC >= 0) {
			matrix.redrawCell(oldFC, oldFR);
		}
	}

	public void setFocusedCell(int column, int row) {
		setFocusedCell(column, row, false);
	}

	public boolean isCellVisible(int column, int row){
		if (column < -1 || row < -1)
			return false;
		if (column > getLastColumnVisible() || column < getFirstColumnVisible())
			return false;
		if (row > getLastRowVisible() || row < getFirstRowVisible())
			return false;
		return true;
	}
	public void setFocusedCell(int column, int row, boolean evenIfMultipleSelected) {
		// can be overridden to respond to focus as in touch, edit etc. for explanation showing, but should call super.setFocusedCell(column, row);
		defocusCell();
		if (column < -1 || row < -1 || (!evenIfMultipleSelected && !singleTableCellSelected()))
			return;
		int count = 0;
		while (count++ < 5 && (column > getLastColumnVisible() || column < getFirstColumnVisible())) { // iterate in case column widths make it tough to figure out
			if (column > getLastColumnVisible()) {
				int newFirst = getFirstColumnVisible() + (column - getLastColumnVisible()) + 1;
				if (newFirst < 0)
					newFirst = 0;
				setFirstColumnVisible(newFirst, true);
			}
			else if (column < getFirstColumnVisible()) {
				setFirstColumnVisible(column, true);
			}
		}
		if (column < getFirstColumnVisible())
			setFirstColumnVisible(column, true);

		count = 0;
		while (count++ < 5 && (row > getLastRowVisible() || row < getFirstRowVisible())) { // iterate in case row heights make it tough to figure out
			if (row > getLastRowVisible()) {
				int newFirst = getFirstRowVisible() + (row - getLastRowVisible()) + 1;
				if (newFirst < 0)
					newFirst = 0;
				setFirstRowVisible(newFirst, true);
			}
			else if (row < getFirstRowVisible()) {
				setFirstRowVisible(row, true);
			}
		}
		if (row < getFirstRowVisible())
			setFirstRowVisible(row, true);

		focusColumn = column;
		focusRow = row;
		rowNames.redrawCell(-1, focusRow);
		columnNames.redrawCell(focusColumn, -1);
		if (focusRow >= 0 && focusColumn >= 0)
			matrix.redrawCell(focusColumn, focusRow);
	}

	/* attempts to show cells from column through column B along row */
	public void setFocusedSequence(int column, int columnB, int row) {
		// can be overridden to respond to focus as in touch, edit etc. for explanation showing, but should call super.setFocusedCell(column, row);
		defocusCell();
		if (column < -1 || columnB < -1 || row < -1)
			return;
		if (columnB > getLastColumnVisible()) {
			if (columnB - column == getNumColumnsVisible())
				setFirstColumnVisible(column);
			else if (columnB - column < getNumColumnsVisible())
				setFirstColumnVisible(getFirstColumnVisible() + columnB - getLastColumnVisible());
			else
				setFirstColumnVisible(column);
		}
		setFocusedCell(column, row, true);
	}

	public int getTableWidth() {
		return getBounds().width;
	}

	boolean constrainMaxAutoColumn = false;
	public void setConstrainMaxAutoColumn(boolean c) {
		constrainMaxAutoColumn = c;
	}

	public boolean getConstrainMaxAutoColumn() {
		return constrainMaxAutoColumn;
	}

	public void setConstrainMaxAutoColumnNum(int c) {
		contrainedMaxColumnNum = c;
	}

	public int getConstrainMaxAutoColumnNum() {
		return contrainedMaxColumnNum;
	}
	boolean constrainMaxAutoRownames = false;
	public void setConstrainMaxAutoRownames(boolean c) {
		constrainMaxAutoRownames = c;
	}

	public boolean getConstrainMaxAutoRownames() {
		return constrainMaxAutoRownames;
	}

	protected int contrainedMaxColumnNum = 3;

	/* ................................................................................................................. */
	public boolean autoSizeColumns(Graphics g) {

		if (g == null || g.getFont() == null)
			return false;
		FontMetrics fm = g.getFontMetrics(g.getFont());
		autoSizeRows(g);
		String s = getCornerText();
		boolean changed = false;
		if (StringUtil.blank(s))
			s = "GGGGG";
		int max = fm.stringWidth(s);

		int lengthString = 0;
		int tableWIDTHpart = (getTableWidth() - getRowNamesWidth()) / (contrainedMaxColumnNum + 1);
		int newCW = 0;
		int current = 0;

		// row names
		if (!rowNamesWidthAdjusted){
			for (int it = 0; it < getNumRows(); it++) {
				s = getRowNameTextForDisplay(it);
				lengthString = fm.stringWidth(s);
				if (lengthString > max)
					max = lengthString;
			}
			if (constrainMaxAutoRownames && max > tableWIDTHpart) // v. 1.01 e 81
				max = tableWIDTHpart;
			newCW = max + 2 + MesquiteModule.textEdgeCompensationHeight;
			current = getRowNamesWidth();
			if (newCW != current && (Math.abs(newCW - current) > 1)) {
				setRowNamesWidth(newCW);
				changed = true;
			}
		}
		// other columns
		max = fm.stringWidth("G");
		for (int ic = 0; ic < getNumColumns(); ic++) {
			if (!columnAdjusted(ic)) {

				max = 6;
				s = getColumnNameTextForDisplay(ic);
				lengthString = fm.stringWidth(s);
				if (lengthString > max)
					max = lengthString;
				for (int it = 0; it < getNumRows(); it++) {
					s = getMatrixTextForDisplay(ic, it);
					if (s != null) {
						lengthString = fm.stringWidth(s);
						if (lengthString > max)
							max = lengthString;
					}
				}

				if (constrainMaxAutoColumn && max > tableWIDTHpart)
					max = tableWIDTHpart;
				newCW = max + 2 + MesquiteModule.textEdgeCompensationHeight;
				current = getColumnWidth(ic);
				if (newCW != current && (Math.abs(newCW - current) > 1)) { // don't sweat small changes to avoid infinite loops
					setColumnWidth(ic, newCW);
					changed = true;
				}
			}
		}
		return changed;
	}
	public void autoSizeRows (Graphics g) {  
		FontMetrics fm=g.getFontMetrics(g.getFont());
		int h = fm.getMaxAscent()+ fm.getMaxDescent();
		if (!thinRows.getValue()) h+= MesquiteModule.textEdgeCompensationHeight; //2 + MesquiteString.riseOffset;
		setRowHeightsUniform(h);
		if (!columnNames.isDiagonal())
			setColumnNamesRowHeight(h);

	}
	/* ................................................................................................................. */
	int maximumRowNamesWidth = 5000;
	public void setMaximumRowNamesWidth(int m){
		maximumRowNamesWidth = m;
	}
	/* ................................................................................................................. */
	public boolean autoSizeRowNames(Graphics g) {
		if (g == null || g.getFont() == null)
			return false;
		FontMetrics fm = g.getFontMetrics(g.getFont());
		int h = fm.getMaxAscent() + fm.getMaxDescent() + MesquiteModule.textEdgeCompensationHeight; // 2 + MesquiteString.riseOffset;
		boolean changed = false;
		int max = 100;
		String s = "    ";
		// row names
		int lengthString = 0;
		for (int it = 0; it < getNumRows(); it++) {
			s = getRowNameTextForDisplay(it);
			if (!StringUtil.blank(s)) {
				lengthString = fm.stringWidth(s);
				if (lengthString > max)
					max = lengthString;
			}
		}
		if (max > maximumRowNamesWidth)
			max = maximumRowNamesWidth;
		int tableWIDTHpart = (getTableWidth() - getRowNamesWidth()) / (contrainedMaxColumnNum + 1);
		if (constrainMaxAutoRownames && max > tableWIDTHpart) // v. 1.01 e 81
			max = tableWIDTHpart;
		int newCW = max + 10 + MesquiteModule.textEdgeCompensationHeight;
		int current = getRowNamesWidth();
		if (newCW > current && (Math.abs(newCW - current) > 1)) {
			setRowNamesWidth(newCW);
			changed = true;
		}
		return changed;
	}
	/* ................................................................................................................. */
	private static final int NOMORE = 0;

	private static final int TAB = 1;

	private static final int LINE = 2;

	private static final int TOKENTabBounded = 3;

	private static final int TOKENReturnBounded = 4;

	private static int goToNext(String s, MesquiteInteger pos) {
		if (s == null || pos == null || pos.getValue() < -1 || pos.getValue() + 1 > s.length() - 1)
			return NOMORE;
		pos.increment();
		int i = pos.getValue();
		char c = s.charAt(i);
		if (c == '\t')
			return TAB;
		if (c == '\n' || c == '\r') {
			if (StringUtil.lineEnding().length() > 1)
				pos.increment();
			return LINE;
		}

		while (i < s.length() && !(c == '\n' || c == '\r' || c == '\t')) {
			i++;
			if (i < s.length())
				c = s.charAt(i);
			else
				c = 0;
		}
		if (i >= s.length()) {
			pos.setValue(i);
			return TOKENReturnBounded;
		}
		if (c == '\n' || c == '\r') {
			i--;
			pos.setValue(i);
			return TOKENReturnBounded;
		}
		pos.setValue(i);
		return TOKENTabBounded;

	}

	public int[] getTabbedLines(String s){
		return getTabbedLinesCount(s);
	}
	/* ................................................................................................................. */
	public static int[] getTabbedLinesCount(String s) {
		if (s == null)
			return null;
		int lines = 0;
		int token;
		boolean lastIsLine = true;
		MesquiteInteger pos = new MesquiteInteger(-1);
		while ((token = goToNext(s, pos)) != NOMORE) {
			if (token == LINE) {
				lines++;
				lastIsLine = true;
			}
			else
				lastIsLine = false;
		}
		if (!lastIsLine)
			lines++;
		int[] result = new int[lines];
		int line = 0;
		int item = 0;
		int pending = 0;
		pos.setValue(-1);
		while ((token = goToNext(s, pos)) != NOMORE) {
			if (token == LINE) {
				item += pending;
				if (item == 0)
					item = 1;
				result[line++] = item;
				pending = 0;
				item = 0;
				lastIsLine = true;
			}
			else {
				if (token == TOKENTabBounded || token == TAB) {
					pending = 1;
				}
				else {
					pending = 0;
					if (token == TOKENReturnBounded)
						lastIsLine = true;
					else
						lastIsLine = false;
				}
				item++;

			}
		}
		if (lastIsLine && line < result.length)
			result[line] = item;

		return result;
	}



	/* ................................................................................................................. */
	public MesquiteCommand getCopyCommand() {
		return copyCommand;
	}

	public MesquiteCommand getCopyLiteralCommand() {
		return copyLiteralCommand;
	}

	public MesquiteCommand getPasteCommand() {
		return pasteCommand;
	}

	public MesquiteCommand getCutCommand() {
		return cutCommand;
	}

	public MesquiteCommand getClearCommand() {
		return clearCommand;
	}

	public MesquiteCommand getSelectAllCommand() {
		return selectAllCommand;
	}

	/* ................................................................................................................. *
	public void setHorizScrollVisible(boolean vis) {
		horizScroll.setVisible(vis);
	}

	public void setVertScrollVisible(boolean vis) {
		vertScroll.setVisible(vis);
	}

	/* ................................................................................................................. */
	public int getLastRow() {
		return numRowsTotal - 1;
	}

	/* ................................................................................................................. */
	public int getLastColumn() {
		return numColumnsTotal - 1;
	}

	/* ................................................................................................................. */
	public int getMatrixWidth() {
		return matrixWidth;
	}

	public int getMatrixHeight() {
		return matrixHeight;
	}

	private synchronized void  printComponent(Graphics g, Panel c) {
		int shiftX = c.getBounds().x;
		int shiftY = c.getBounds().y;
		g.translate(shiftX, shiftY);
		c.print(g);
		g.translate(-shiftX, -shiftY);
	}

	/* ............................................................................................................... */
	public synchronized void printAll(Graphics g) {
		cornerCell.paint(g);
		printComponent(g, rowNames);
		printComponent(g, columnNames);
		printComponent(g, matrix);
	}

	/* ................................................................................................................. */
	public void printTable(MesquitePrintJob pjob, MesquiteWindow window) {
		if (pjob != null) {
			pjob.printComponent(this, new Dimension(getTotalColumnWidth() + getRowNamesWidth(), getTotalRowHeight() + getColumnNamesRowHeight()), null);
		}
	}

	/* ............................................................................................................... */
	/**
	 * @author Peter Midford
	 */
	public synchronized void tableToPDF(MesquitePDFFile pdfFile, MesquiteWindow window, int fitToPage) {
		if (pdfFile != null) {
			printAll(pdfFile.getPDFGraphicsForComponent(this, totalDimension()));
			pdfFile.end();
		}
	}

	/* ............................................................................................................... */
	/**
	 * @author Peter Midford
	 */
	private Dimension totalDimension() {
		return new Dimension(getTotalColumnWidth() + getRowNamesWidth() + 25, getTotalRowHeight() + getColumnNamesRowHeight() + 20);
	}

	/* ............................................................................................................... */
	public boolean checkResetFont(Graphics g) { // ^^^
		boolean doReset = false;
		if (g != null) {
			Font f = g.getFont();
			if (f != null && f != oldF) {
				FontMetrics fm = g.getFontMetrics(f);
				if (fm != null) {
					int height = fm.getHeight();
					setGrabberSize(fm);
					if (!columnNames.isDiagonal())
						setColumnNamesRowHeight(height + MesquiteModule.textEdgeCompensationHeight);
					for (int i = firstRowVisible; i < numRowsTotal; i++) {
						if (rowHeights[i] < height + MesquiteModule.textEdgeCompensationHeight) {
							setRowHeight(i, height + MesquiteModule.textEdgeCompensationHeight);
							doReset = true;
						}
					}
					oldF = f;
				}
				if (oldF != null)
					boldFont = new Font(oldF.getName(), Font.BOLD, oldF.getSize());
			}
			if (boldFont == null)
				boldFont = new Font(oldF.getName(), Font.BOLD, oldF.getSize());

		}
		return doReset;

	}

	/* ............................................................................................................... */
	public void paint(Graphics g) { // ^^^
		if (MesquiteWindow.checkDoomed(this))
			return;
		boolean resized = false;
		if (autoRowNameWidth.getValue())
			autoSizeRowNames(g);
		if (checkResetFont(g)) {
			if (autosizeColumns)
				autoSizeColumns(g);
			else{
				autoSizeRowNames(g);
				autoSizeRows(g);
			}
			repaintAll();
		}
		else if (autosizeColumns) {
			resized = autoSizeColumns(g);
		} else
			autoSizeRows(g);
		if (resized) {
			MesquiteWindow.uncheckDoomed(this);
			repaintAll();
		}
		else {
			super.paint(g);
			MesquiteWindow.uncheckDoomed(this);
		}
	}

	/* ............................................................................................................... */
	public void update(Graphics g) {
		paint(g);
	}

	public boolean flag = false;   //just in case
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
	/* ............................................................................................................... */
	/** repaints all components of the table */
	public void repaintCells(boolean repaintColumnNames) {
		if (repaintColumnNames)
			columnNames.repaint();
		//repaint();
		matrix.repaint();
	}


	/* ............................................................................................................... */
	/** returns a tab delimited text version of the table */
	public String getTextVersion() {
		StringBuffer outputBuffer = new StringBuffer(getNumColumns() * getNumRows());
		for (int column = 0; column < getNumColumns(); column++)
			outputBuffer.append("\t" + columnNames.getText(column, -1));
		outputBuffer.append("\n");
		for (int row = 0; row < getNumRows(); row++) {
			outputBuffer.append(rowNames.getText(-1, row));
			for (int column = 0; column < getNumColumns(); column++)
				outputBuffer.append("\t" + matrix.getText(column, row));
			outputBuffer.append("\n");
		}
		return outputBuffer.toString();

	}
	public Color getColumnNameFillColor(int column, Color defaultFillColor, boolean focused, boolean selected, boolean dimmed, boolean editable){
		Color color;
		if (selected) {
			color = Color.white;
		}
		else if (focused)
			color = Color.lightGray;
		else if (defaultFillColor!=null)
			color = defaultFillColor;
		else if (dimmed)
			color = Color.lightGray;
		else if (editable)
			color = Color.white;
		else
			color = ColorDistribution.uneditable;
		return color;
	}
	/* ............................................................................................................... */
	/** returns color of row or number box. */
	public Color getRCNumberBoxColor(boolean isRow, int number) {
		//return ColorDistribution.medium[colorScheme];
		return ColorTheme.getContentBackgroundPale();

	}

	/* ............................................................................................................... */
	/** returns color of row or number box. */
	public Color getRCNumberBoxDarkColor(boolean isRow, int number) {
		//return ColorDistribution.dark[colorScheme];
		return ColorTheme.getContentDarkElement();

	}

	/* ............................................................................................................... */
	/** Draws any extra content of a columnNamesPanel */
	public void drawColumnNamesPanelExtras(Graphics g, int left, int top, int width, int height) {
	}

	/* ............................................................................................................... */
	/**
	 * Draws the shaded box that houses row or column numbers in a table. Can also be used to just draw a shaded box, even if no row or column numbers are to be drawn.
	 */
	public void drawRowColumnNumberBox(Graphics g, int number, boolean isRow, int left, int top, int width, int height) {
		Color oldColor = g.getColor();
		// g.setColor(Color.lightGray);
		g.setColor(getRCNumberBoxColor(isRow, number));
		g.fillRect(left, top, width, height);
		if ((focusRow == number && isRow) || (focusColumn == number && !isRow))
			GraphicsUtil.fillTransparentSelectionRectangle(g, left, top, width, height);
		g.setColor(ColorDistribution.veryVeryLightGray);
		int newTop;
		if (isRow)
			newTop = top;
		else
			newTop = top + 1;
		int newLeft;
		if (isRow)
			newLeft = left + 1;
		else
			newLeft = left;
		g.drawLine(newLeft, newTop, left + width - 1, newTop);
		g.drawLine(newLeft, newTop, newLeft, top + height - 1);
		// g.setColor(Color.darkGray);
		g.setColor(getRCNumberBoxDarkColor(isRow, number));
		g.drawLine(left, top + height, left + width, top + height);
		g.drawLine(left + width, top + height, left + width, top);
		g.setColor(oldColor);
	}

	/* ............................................................................................................... */
	/** Draws the shaded box containing a row or column number in a table. */
	public void drawRowColumnNumber(Graphics g, int number, boolean isRow, int left, int top, int width, int height) {
		drawRowColumnNumberBox(g, number, isRow, left, top, width, height);
		String s = "" + (number + 1); // +1 is added because internally the rows and columns are 0-based.
		g.setClip(left, top, width, height);
		FontMetrics fm = g.getFontMetrics();
		int length = fm.stringWidth(s);
		g.drawString(s, left + (width - length) / 2, top + height - (height - fm.getAscent() - fm.getDescent()) / 2 - fm.getDescent());
	}

	/* ............................................................................................................... */
	/** Sets the size of the grabberbars */
	public void setGrabberSize() {
		MesquiteWindow mw = MesquiteWindow.windowOfItem(this);
		if (mw != null && mw.getFont() != null) {
			FontMetrics f = getFontMetrics(mw.getFont());
			setGrabberSize(f);
		}
	}

	/* ............................................................................................................... */
	/** Sets the size of the grabberbars */
	public void setGrabberSize(FontMetrics fm) {
		if (fm != null) {
			if (showRowNumbers)
				setRowGrabberWidth(fm.stringWidth("  " + numRowsTotal) + 4);
			if (showColumnNumbers)
				setColumnGrabberWidth(fm.getHeight() + 2);
		}
	}

	/* ............................................................................................................... */
	/** Sets whether or not to show the row or column numbers */
	public void setShowRCNumbers(boolean srn, boolean scn) {
		showRowNumbers = srn;
		showColumnNumbers = scn;
		if (showRowNumbers)
			setRowGrabberWidth(24);
		if (showColumnNumbers)
			setColumnGrabberWidth(14);
	}

	/* ............................................................................................................... */
	/** Sets the overall size of the table in pixels, matching the window size. */
	public void resetTableSize(boolean useFullWindowSize) {
		if (useFullWindowSize) {
			MesquiteWindow window = MesquiteWindow.windowOfItem(this);
			if (window != null)
				setSize(window.getWidth(), window.getHeight());
		}
		else {
			setSize(getBounds().width, getBounds().height);
		}
	}

	/* ............................................................................................................... */
	/** Sets the overall size of the table in pixels. */
	public void setSize(int totalWidth, int totalHeight) {
		setGrabberSize();
		matrixWidth = totalWidth - rowNamesWidth - 16 - rowGrabberWidth;
		matrixHeight = totalHeight - 16 - columnNames.calcColumnNamesHeight();
		resetComponentSizes();
		super.setSize(totalWidth, totalHeight);
	}

	/* ............................................................................................................... */
	/** Sets the overall size of the table in pixels. */
	public void setBounds(int x, int y, int w, int h) {
		setGrabberSize();
		matrixWidth = w - rowNamesWidth - 16 - rowGrabberWidth;
		matrixHeight = h - 16 - columnNames.calcColumnNamesHeight();
		resetComponentSizes();
		super.setBounds(x, y, w, h);
	}

	/* ............................................................................................................... */
	/** Gets the height of the columnNames Panel. */
	// public int recalcColumnNamesHeight() {
	// return columnNames.rowHeight(-1)*columnNames.getNumRows() + columnGrabberWidth;
	// }
	/* ............................................................................................................... */
	/** Resets sizes of all components. */
	public void resetComponentSizes() {
		// columnNamesRowHeight=columnNames.calcColumnNamesHeight()-columnGrabberWidth;
		rowNames.setSize(rowNamesWidth + rowGrabberWidth, matrixHeight);
		rowNames.setLocation(0, columnNames.calcColumnNamesHeight());
		rowNames.setTableUnitSize(rowNamesWidth + rowGrabberWidth, matrixHeight);
		columnNames.setSize(matrixWidth, columnNames.calcColumnNamesHeight());
		columnNames.setLocation(rowNamesWidth + rowGrabberWidth, 0);
		columnNames.setTableUnitSize(matrixWidth, columnNames.calcColumnNamesHeight());
		matrix.setSize(matrixWidth, matrixHeight);
		matrix.setLocation(rowNamesWidth + rowGrabberWidth, columnNames.calcColumnNamesHeight());
		matrix.setTableUnitSize(matrixWidth, matrixHeight);
		horizScroll.setBounds(rowNamesWidth + rowGrabberWidth, matrixHeight + columnNames.calcColumnNamesHeight(), matrixWidth, 16);
		vertScroll.setBounds(matrixWidth + rowNamesWidth + rowGrabberWidth, columnNames.calcColumnNamesHeight(), 16, matrixHeight);
		controlStrip.setBounds(0, matrixHeight + columnNames.calcColumnNamesHeight(), rowNamesWidth + rowGrabberWidth, 16);
		cornerCell.setLocation(0, 0);
		cornerCell.setSize(rowNamesWidth - 1 + rowGrabberWidth, columnNames.calcColumnNamesHeight()); // -1, -1
		cornerCell.setTableUnitSize(rowNamesWidth - 1 + rowGrabberWidth, columnNames.calcColumnNamesHeight());
		resetNumColumnsVisible();
		resetNumRowsVisible();
	}

	/* ............................................................................................................... */
	/** Deletes the given column (but doesn't call for repaint) */
	public void deleteColumn(int column) {
		if (!columnLegal(column))
			return;
		if (column < numColumnsTotal && column >= 0) {
			int[] newColumnWidths = new int[numColumnsTotal - 1];
			for (int c = 0; c < numColumnsTotal - 1; c++)
				if (c > column)
					newColumnWidths[c] = columnWidths[c + 1];
				else
					newColumnWidths[c] = columnWidths[c];

			columnWidths = newColumnWidths;

			numColumnsTotal = numColumnsTotal - 1;
			for (int i = 0; i < numSelectTypes; i++) {
				columnsSelected[i] = new Bits(numColumnsTotal);
				cellsSelected[i] = new Bits((numRowsTotal) * (numColumnsTotal));
				columnNamesSelected[i] = new Bits(numColumnsTotal);
			}
			if (firstColumnVisible >= numColumnsTotal || firstColumnVisible < 0)
				firstColumnVisible = 0; 
			if (horizScroll != null) {
				horizScroll.setValue(firstColumnVisible);
				horizScroll.setMaximum(numColumnsTotal);
			}
		}
	}

	/* ............................................................................................................... */
	public void moveColumns(int starting, int num, int justAfter) {
	}

	/* ............................................................................................................... */
	/** Sets the number of rows TOTAL (not just visible) */
	public void setNumRows(int rows) {
		if (rows < 0)
			return;
		int[] newRowHeights = new int[rows];
		for (int r = 0; r < numRowsTotal && r < rows; r++)
			newRowHeights[r] = rowHeights[r];
		int defHeight = baseRowHeight;

		if (rowHeights != null && rowHeights.length > 0)
			defHeight = rowHeights[0];
		if (rows > numRowsTotal)
			for (int r = numRowsTotal; r < rows; r++)
				newRowHeights[r] = defHeight;
		numRowsTotal = rows;
		rowHeights = newRowHeights;
		for (int i = 0; i < numSelectTypes; i++) {
			if (rowsSelected[i] == null)
				rowsSelected[i] = new Bits(numRowsTotal);
			else
				rowsSelected[i].resetSize(numRowsTotal);

			if (cellsSelected[i] == null)
				cellsSelected[i] = new Bits(numRowsTotal * numColumnsTotal);
			else {
				cellsSelected[i].resetSize(numRowsTotal * numColumnsTotal);
				cellsSelected[i].clearAllBits();
			}

			if (rowNamesSelected[i] == null)
				rowNamesSelected[i] = new Bits(numRowsTotal);
			else
				rowNamesSelected[i].resetSize(numRowsTotal);
		}
		if (rowWidthsAdjusted != null)
			rowWidthsAdjusted.resetSize(numRowsTotal);
		if (firstRowVisible >= numRowsTotal || firstRowVisible < 0)
			firstRowVisible = 0;
		if (vertScroll != null) {
			vertScroll.setValue(firstRowVisible);
			vertScroll.setMaximum(numRowsTotal);
		}
	}

	/* ............................................................................................................... */
	/** Inserts num columns just after "starting". Calls setNumColumns */
	public void insertColumns(int starting, int num) {
		int oldNumColumns = numColumnsTotal;
		setNumColumns(numColumnsTotal + num);
		if (starting < oldNumColumns - 2) {
			for (int c = columnWidths.length - 1; c > starting + num; c--)
				columnWidths[c] = columnWidths[c - num];
			for (int c = starting + 1; c < starting + num + 1; c++)
				columnWidths[c] = baseColumnWidth;
		}
	}

	/* ............................................................................................................... */
	/** Sets the number of columns TOTAL (not just visible) */
	public void setNumColumns(int columns) {
		if (columns < 0)
			return;
		if (numColumnsTotal == 0) {
			columnWidths = new int[columns];
			numColumnsTotal = columns;
			setColumnWidthsUniform(baseColumnWidth);
			for (int i = 0; i < numSelectTypes; i++) {
				columnsSelected[i] = new Bits(numColumnsTotal);
				cellsSelected[i] = new Bits((numRowsTotal) * (numColumnsTotal));
				columnNamesSelected[i] = new Bits(numColumnsTotal);
			}
			horizScroll.setValue(0);
			horizScroll.setMaximum(numColumnsTotal);
		}
		else if (columns != numColumnsTotal) {
			int[] newColumnWidths = new int[columns];
			for (int c = 0; c < columnWidths.length && c < newColumnWidths.length; c++)
				newColumnWidths[c] = columnWidths[c];
			int defWidth = baseColumnWidth;
			if (columnWidths != null && columnWidths.length > 0)
				defWidth = columnWidths[0];
			if (columns > numColumnsTotal)
				for (int c = numColumnsTotal; c < columns; c++)
					newColumnWidths[c] = defWidth;
			columnWidths = newColumnWidths;

			for (int i = 0; i < numSelectTypes; i++) {
				if (columnsSelected[i] == null)
					columnsSelected[i] = new Bits(columns);
				else
					columnsSelected[i].resetSize(columns);
				if (columnNamesSelected[i] == null)
					columnNamesSelected[i] = new Bits(columns);
				else
					columnNamesSelected[i].resetSize(columns);
				if (cellsSelected[i] == null)
					cellsSelected[i] = new Bits((numRowsTotal) * (columns));
				else {
					cellsSelected[i].resetSize(numRowsTotal * columns);
					cellsSelected[i].clearAllBits();
				}
			}
			if (columnWidthsAdjusted != null)
				columnWidthsAdjusted.resetSize(columns);
			numColumnsTotal = columns;
			if (firstColumnVisible >= columns || firstColumnVisible < 0)
				firstColumnVisible = 0;
			if (horizScroll != null) {
				horizScroll.setValue(firstColumnVisible);
				horizScroll.setMaximum(numColumnsTotal);
			}
		}
	}

	/* ............................................................................................................... */
	/** Gets the number of rows TOTAL (not just visible) */
	public int getNumRows() {
		return numRowsTotal;
	}

	/* ............................................................................................................... */
	/** Gets the number of columns TOTAL (not just visible) */
	public int getNumColumns() {
		return numColumnsTotal;
	}

	/* ............................................................................................................... */
	/**
	 * Calculates the number of visible columns, rounded up to the nearest whole number. Also sets the value of the lastColumnVisible, and sets the appropriate page increment for the horizontal scroll bar.
	 */
	public void resetNumColumnsVisible() {
		numColumnsVisible = numColumnsTotal - firstColumnVisible + 1;
		int sum = 0;
		for (int c = firstColumnVisible; c < numColumnsTotal && c < columnWidths.length; c++) {
			sum += columnWidths[c];
			if (sum >= matrixWidth) {
				break;
			}
			numColumnsVisible = (c - firstColumnVisible + 1); // had been +1 but last column may not be entirely visible
		}
		if (numColumnsVisible < 2)
			horizScroll.setBlockIncrement(1);
		else
			horizScroll.setBlockIncrement(numColumnsVisible - 1);
		lastColumnVisible = firstColumnVisible + numColumnsVisible - 1;
	}

	/* ............................................................................................................... */
	/**
	 * Calculates the number of visible rows, rounded up to the nearest whole number. Also sets the value of the lastRowVisible, and sets the appropriate page increment for the vertical scroll bar.
	 */
	int verticalScrollPageIncrement=1;
	public void resetNumRowsVisible() {
		numRowsVisible = (numRowsTotal - firstRowVisible + 1);
		int sum = 0;
		int numRows = 0;
		for (int r = firstRowVisible; r < numRowsTotal && r <rowHeights.length; r++) {
			sum += rowHeights[r];
			numRows++;
			if (sum >= matrixHeight) {
				numRowsVisible = (r - firstRowVisible + 1);
				break;
			}
		}
		verticalScrollPageIncrement = numRowsVisible - 1;
		if (sum>0) {
			double avgRowHeight = (1.0*sum)/numRows;
			int numRowsPossiblyVisible = (int)(matrixHeight/avgRowHeight)-1;
			if (numRowsPossiblyVisible>verticalScrollPageIncrement)
				verticalScrollPageIncrement = numRowsPossiblyVisible;
		}
		
		//vertScroll.setBlockIncrement(getNumRows()-1);
		if (numRowsTotal < 2)
			vertScroll.setBlockIncrement(1);
		else
			vertScroll.setBlockIncrement(verticalScrollPageIncrement);  //DRM  14 july 14: changed this so that it did full page scrolls

		lastRowVisible = firstRowVisible + numRowsVisible - 1;
	}

	/* ............................................................................................................... */
	/** Gets the number of visible columns. Rounded up to the nearest whole number. */
	public int getNumColumnsVisible() {
		return numColumnsVisible;
	}

	/* ............................................................................................................... */
	/** Gets the number of visible rows. Rounded up to the nearest whole number. */
	public int getNumRowsVisible() {
		return numRowsVisible;
	}

	/* ............................................................................................................... */
	/** Gets the number of the last visible column. */
	public int getLastColumnVisible() {
		return lastColumnVisible;
	}

	/* ............................................................................................................... */
	/** Gets the number of the last visible row. */
	public int getLastRowVisible() {
		return lastRowVisible;
	}

	/* ............................................................................................................... */
	/** Gets the height of rows TOTAL (not just visible) */
	public int getTotalRowHeight() {
		int sum = 0;
		for (int r = 0; r < numRowsTotal; r++)
			sum += rowHeights[r];
		return sum;
	}

	/* ............................................................................................................... */
	/** Gets the width of columns TOTAL (not just visible) */
	public int getTotalColumnWidth() {
		int sum = 0;
		for (int c = 0; c < numColumnsTotal; c++)
			sum += columnWidths[c];
		return sum;
	}

	/* ............................................................................................................... */
	/** Returns the base column width */
	public int getColumnWidthsUniform() {
		return baseColumnWidth;
	}

	/* ............................................................................................................... */
	/** Sets all column widths to be of uniform width, with given width */
	public void setColumnWidthsUniform(int width) {
		baseColumnWidth = width;
		for (int c = 0; c < numColumnsTotal && c<columnWidths.length; c++)
			columnWidths[c] = width;
		columnWidthsAdjusted.clearAllBits();
	}

	/* ............................................................................................................... */
	/** Sets all row heights to be of uniform height, with given height */
	public void setRowHeightsUniform(int height) {
		baseRowHeight = height;
		for (int r = 0; r < numRowsTotal; r++)
			rowHeights[r] = height;
		rowWidthsAdjusted.clearAllBits();
	}

	/* ............................................................................................................... */
	public int getMinColumnWidth() {
		return 8;
	}

	/* ............................................................................................................... */
	public int getMaxColumnWidth() {
		return 300;
	}

	/* ............................................................................................................... */
	/** set columnwidths according to given array */
	public void setColumnWidths(int[] widths) {
		for (int c = 0; c < numColumnsTotal && c < widths.length; c++)
			columnWidths[c] = widths[c];
		columnWidthsAdjusted.clearAllBits();
	}

	/* ............................................................................................................... */
	/** set rowheights according to given array */
	public void setRowHeights(int[] heights) {
		for (int r = 0; r < numRowsTotal && r < heights.length; r++)
			rowHeights[r] = heights[r];
		rowWidthsAdjusted.clearAllBits();
	}

	/* ............................................................................................................... */
	/** Sets width of given column. */
	public void setColumnWidth(int column, int width) {
		if (!columnLegal(column))
			return;
		columnWidths[column] = width;
		columnWidthsAdjusted.clearBit(column);
	}
	/* ............................................................................................................... */
	/** Sets width of given column. */
	public void setColumnWidth(int column, int width, boolean treatAsAdjusted) {
		if (!columnLegal(column))
			return;
		columnWidths[column] = width;
		if (treatAsAdjusted)
			columnWidthsAdjusted.setBit(column);
		else
			columnWidthsAdjusted.clearBit(column);

	}

	/* ............................................................................................................... */
	/** returns whether the column width has been adjusted */
	public boolean columnAdjusted(int column) {
		if (!columnLegal(column))
			return false;
		return columnWidthsAdjusted.isBitOn(column);
	}

	/* ............................................................................................................... */
	/** returns whether the row width has been adjusted */
	public boolean rowAdjusted(int row) {
		if (!rowLegal(row))
			return false;
		return rowWidthsAdjusted.isBitOn(row);
	}

	/* ............................................................................................................... */
	/** Sets height of given row. */
	public void setRowHeight(int row, int height) {
		if (!rowLegal(row))
			return;
		rowHeights[row] = height;
		rowWidthsAdjusted.clearBit(row);
	}

	/* ............................................................................................................... */
	/** Gets width of given column. */
	public int getColumnWidth(int column) {
		if (columnLegal(column))
			return columnWidths[column];
		else
			return 0;
	}

	/* ............................................................................................................... */
	/** Gets height of given row. */
	public int getRowHeight(int row) {
		if (rowLegal(row))
			return rowHeights[row];
		else
			return 0;
	}

	public int getColumnX(int column) {
		int lineX = 0;
		if (column == firstColumnVisible-1)
			return 0;
		for (int c=firstColumnVisible; (c<numColumnsTotal) && c<lastColumnVisible; c++) {
			lineX += columnWidths[c];
			if (column==c)
				return lineX;
		}
		return MesquiteInteger.unassigned;

	}
	public int getRowY(int row) {
		int lineY = 0;
		if (row == firstRowVisible-1)
			return 0;
		for (int c=firstRowVisible; (c<numRowsTotal) && c<lastRowVisible; c++) {
			lineY += rowHeights[c];
			if (row==c)
				return lineY;
		}
		return MesquiteInteger.unassigned;

	}
	/* ............................................................................................................... */
	/** Sets height of column names. */
	public void setColumnNamesRowHeight(int h) {
		int oldCNRH = columnNamesRowHeight;
		columnNamesRowHeight = h;
		columnNames.setRowHeight(h);
		columnNames.setHeight();
		int oldMatrixHeight = matrixHeight;
		matrixHeight = getHeight() - 16 - columnNames.calcColumnNamesHeight();
		if (oldMatrixHeight!=matrixHeight || oldCNRH != columnNamesRowHeight)
			resetComponentSizes();
		// cornerCell.setSize(cornerCell.getBounds().width, height+columnGrabberWidth);
	}

	/* ............................................................................................................... */
	/** Sets width of row names. */
	public void setRowNamesWidth(int width) {
		rowNamesWidth = width;
		rowNames.setWidth(width + rowGrabberWidth);
		cornerCell.setSize(width + rowGrabberWidth - 1, cornerCell.getBounds().height);
		matrixWidth = getTableWidth() - rowNamesWidth - 16 - rowGrabberWidth;
		resetComponentSizes();
	}

	/* ............................................................................................................... */
	/** Sets the message in the control strip at lower left of table. */
	public void setMessage(String message) {
		controlStrip.setMessage(message);
	}

	/* ............................................................................................................... */
	/** Adds a button to the control strip at lower left of table. */
	public void addControlButton(MesquiteButton b) {
		controlStrip.addButton(b);
	}

	/* ............................................................................................................... */
	/** Removes a button from the control strip at lower left of table. */
	public void removeControlButton(MesquiteButton b) {
		controlStrip.removeButton(b);
	}

	/* ............................................................................................................... */
	/** Sets whether or not different the message panel is visible. */
	public void add(boolean vis) {
		controlStrip.setVisible(vis);
	}

	/* ............................................................................................................... */
	/** set color of backgrond of column names */
	public void setColumnNamesColor(Color c) {
		columnNames.setFillColor(c);
	}

	/* ............................................................................................................... */
	/** set color of backgrond of column names */
	public void setRowNamesColor(Color c) {
		rowNames.setFillColor(c);
	}

	/* ............................................................................................................... */
	/** Sets whether or not different parts of table are editable. */
	public void setAutoEditable(boolean cells, boolean rowNames, boolean columnNames, boolean corner) {
		cellsAutoEditable = cells;
		rowNamesAutoEditable = rowNames;
		columnNamesAutoEditable = columnNames;
		cornerAutoEditable = corner;
	}

	/* ............................................................................................................... */
	/** Sets whether or not different parts of table are editable. */
	public void setEditable(boolean cellsE, boolean rowNamesE, boolean columnNamesE, boolean cornerE) {
		cellsEditable = cellsE;
		// matrix.setFillColor(editColor(cellsEditable));
		rowNamesEditable = rowNamesE;
		// rowNames.setFillColor(editColor(rowNamesEditable));
		columnNamesEditable = columnNamesE;
		// columnNames.setFillColor(editColor(columnNamesEditable));
		cornerEditable = cornerE;
		if (cornerE)
			cornerCell.setBackground(Color.white);
	}

	/* ............................................................................................................... */
	/** returns whether or not cells of table are editable. */
	public boolean getCellsAutoEditable() {
		return cellsAutoEditable;
	}

	/* ............................................................................................................... */
	/** returns whether or not row names of table are editable. */
	public boolean getRowNamesAutoEditable() {
		return rowNamesAutoEditable;
	}

	/* ............................................................................................................... */
	/** returns whether or not column names of table are auto- editable. */
	public boolean getColumnNamesAutoEditable() {
		return columnNamesAutoEditable;
	}

	/* ............................................................................................................... */
	/** returns whether or not corner cell of table is editable. */
	public boolean getCornerAutoEditable() {
		return cornerAutoEditable;
	}

	/* ............................................................................................................... */
	/** returns whether or not cells of table are editable. */
	public boolean getCellsEditable() {
		return cellsEditable;
	}

	/* ............................................................................................................... */
	/** returns whether or not a cell of table is editable. */
	public boolean isCellEditable(int column, int row) {
		if (!columnLegal(column) || !rowLegal(row))
			return false;
		return cellsEditable;
	}

	/* ............................................................................................................... */
	/** returns whether or not a cell of table is being edited. */
	public boolean isEditing(int column, int row) {
		if (column < 0 && row < 0)
			return false;
		if (column == -1 && rowNames.getEditing())
			return rowNames.getEditField().getRow() == row;
		if (row == -1 && columnNames.getEditing())
			return columnNames.getEditField().getColumn() == column;
		return (matrix.getEditing() && matrix.getEditField().getRow() == row && matrix.getEditField().getColumn() == column);
	}

	/* ............................................................................................................... */
	/** returns whether or not a row name of table is editable. */
	public boolean isRowNameEditable(int row) {
		if (!rowLegal(row))
			return false;
		return rowNamesEditable;
	}

	/* ............................................................................................................... */
	/** returns whether or not a column name of table is editable. */
	public boolean isColumnNameEditable(int column) {
		if (!columnLegal(column))
			return false;
		return columnNamesEditable;
	}

	/* ............................................................................................................... */
	/** returns whether or not corner cell of table is editable. */
	public boolean getCornerEditable() {
		return cornerEditable;
	}

	/* ............................................................................................................... */
	/** Sets whether or not different parts of table are selectable. */
	public void setSelectable(boolean cells, boolean rows, boolean columns, boolean rowNames, boolean columnNames, boolean corner) {
		cellsSelectable = cells;
		rowsSelectable = rows;
		columnsSelectable = columns;
		rowNamesSelectable = rowNames;
		columnNamesSelectable = columnNames;
		cornerSelectable = corner;
	}

	/* ............................................................................................................... */
	/** Sets user-adjust parameters of rows and columns. */
	public void setUserAdjust(int rowsAdjust, int columnsAdjust) {
		userAdjustColumn = columnsAdjust;
		userAdjustRow = rowsAdjust;
	}

	/* ............................................................................................................... */
	/** Sets user-move permission of rows and columns. */
	public void setUserMove(boolean rowsMove, boolean columnsMove) {
		userMoveColumn = columnsMove;
		userMoveRow = rowsMove;
	}

	/* ............................................................................................................... */
	public EditorPanel getColumnNamesPanel() {
		return columnNames;
	}

	/* ............................................................................................................... */
	public EditorPanel getRowNamesPanel() {
		return rowNames;
	}

	/* ............................................................................................................... */
	public EditorPanel getMatrixPanel() {
		return matrix;
	}

	/* ............................................................................................................... */
	public MesquitePanel getCornerPanel() {
		return cornerCell;
	}

	/* ............................................................................................................... */
	public TableScroll getVerticalScroll() {
		return vertScroll;
	}

	/* ............................................................................................................... */
	public TableScroll getHorizontalScroll() {
		return horizScroll;
	}

	/* ............................................................................................................... */
	public int getLeftOfColumn(int column) {
		return startOfColumn(column);
	}

	/* ............................................................................................................... */
	public int startOfColumn(int column) {

		if (column<firstColumnVisible)
			return -1;
		else if (column>firstColumnVisible + numColumnsTotal || column>=numColumnsTotal)
			return -1;
		else {
			int lineX = 0;
			//	if (tb.showRowGrabbers)
			//		lineX = tb.getRowGrabberWidth();
			for (int c=firstColumnVisible; c<column && c<columnWidths.length; c++) {
				lineX += columnWidths[c];
			}
			return lineX;
		}
	}


	/* ............................................................................................................... */
	public int getColumnNamesRowHeight() {
		return columnNames.rowHeight(-1); // (columnNames.getBounds().height);
	}

	/* ............................................................................................................... */
	public int getTopOfRow(int row) {
		return rowNames.startOfRow(row);
	}

	public int getRowNamesWidth() {
		return rowNamesWidth; // (rowNames.getBounds().width);
	}

	/* ............................................................................................................... */
	/** returns the current Hand cursor for dragging columns, etc. */
	public Cursor getHandCursor() {
		return handCursor;
	}

	/* ............................................................................................................... */
	/** returns the current E resize cursor for widening columns, etc. */
	public Cursor getEResizeCursor() {
		return eastResizeCursor;
	}
	/* ............................................................................................................... */
	/** returns the current N resize cursor for deepening column names, etc. */
	public Cursor getNResizeCursor() {
		return northResizeCursor;
	}

	/* ............................................................................................................... */
	/** returns the current mode for user-adjust of columns */
	public int getUserAdjustColumn() {
		return userAdjustColumn;
	}

	/* ............................................................................................................... */
	/** returns the current mode for user-adjust of rows */
	public int getUserAdjustRow() {
		return userAdjustRow;
	}

	/* @@@............................................................................................................... */
	/** returns the current permission for user-move of columns */
	public boolean getUserMoveColumn() {
		return userMoveColumn;
	}

	/* @@@............................................................................................................... */
	/** returns the current permission for user-move of rows */
	public boolean getUserMoveRow() {
		return userMoveRow;
	}

	/* ............................................................................................................... */
	/** Instantly forces a draw of column to show it as blank */
	public void blankColumn(int column) {
		if (!columnLegal(column))
			return;
		for (int r = firstRowVisible; r < getNumRows(); r++)
			// TODO: this and similar places (e.g. redrawMatrix) need to use getNumRowsVisible instead
			matrix.blankCell(column, r);
	}

	/* ............................................................................................................... */
	/** Instantly forces a draw of row to show it as blank */
	public void blankRow(int row) {
		if (!rowLegal(row))
			return;
		for (int c = firstColumnVisible; c < getNumColumns(); c++)
			matrix.blankCell(c, row);
	}

	/* ............................................................................................................... */
	/** Instantly forces a draw of row to show it as blank */
	public void blankCell(int column, int row) {
		if (!columnLegal(column) || !rowLegal(row))
			return;
		matrix.blankCell(column, row);
	}

	/* ............................................................................................................... */
	/** sets whether filling of rectangles is done in quick way (whole matrix or blocks at once). In this case, subclass of table has to take responsiblity for clip issues in matrixpanel */
	public void setQuickMode(boolean q) {
		quickMode = q;
	}

	/* ............................................................................................................... */
	/** returns whether filling of rectangles is done in quick way (whole matrix or blocks at once). In this case, subclass of table has to take responsiblity for clip issues in matrixpanel */
	public boolean useQuickMode() {
		return quickMode;
	}

	/* ............................................................................................................... */
	/** redraws visible part of matrix */
	public void redrawMatrix() {
		redrawBlock(firstColumnVisible, firstRowVisible, getNumColumns(), getNumRows());
	}

	/* ............................................................................................................... */
	/** Redraw (directly, without repaint()) all column names */
	public void redrawColumnNames() {
		for (int c = firstColumnVisible; c < getNumColumns(); c++)
			redrawColumnName(c);
	}

	/* ............................................................................................................... */
	/** Redraw (directly, without repaint()) all row names */
	public void redrawRowNames() {
		for (int c = firstRowVisible; c < getNumRows(); c++)
			redrawRowName(c);
	}

	/* ............................................................................................................... */
	/** Redraw (directly, without repaint()) the indicated row name */
	public void redrawRowName(int row) {
		if (rowLegal(row) && showRowNames) {
			Graphics g = rowNames.getGraphics();
			if (g == null)
				return;
			rowNames.redrawName(g, row);
			g.dispose();
		}
	}

	/* ............................................................................................................... */
	/** Redraw (directly, without repaint()) the indicated column name */
	public void redrawColumnName(int column) {
		if (columnLegal(column)) {
			Graphics g = columnNames.getGraphics();
			if (g == null)
				return;
			columnNames.redrawName(g, column);
			g.dispose();
		}
	}

	// public MesquiteTimer rCellTimer = new MesquiteTimer();
	/* ............................................................................................................... */
	/** Redraw (directly, without repaint()) the indicated cell */
	public void redrawCell(int column, int row) {
		if (column == -1)
			redrawRowName(row);
		else if (row == -1)
			redrawColumnName(column);
		else {
			Graphics g = matrix.getGraphics();
			if (g == null)
				return;
			redrawCell(column, row, g);
			g.dispose();
		}
	}

	/* ............................................................................................................... */
	/** Redraw (directly, without repaint()) the indicated cell */
	public void redrawCell(int column, int row, Graphics g) {
		if (columnLegal(column) && rowLegal(row)) {
			matrix.redrawCell(g, column, row);
		}
	}

	/* ............................................................................................................... */
	/** Redraw (directly, without repaint()) the indicated cell, offset from its usual place */
	public void redrawCellOffset(int column, int row, int offsetColumn, int offsetRow, Graphics g) {
		if (columnLegal(column) && rowLegal(row)) {
			matrix.redrawCellOffset(g, column, row, offsetColumn, offsetRow);
		}
	}

	/* ............................................................................................................... */
	/** Redraw block of cells temporarily offset from. */
	// draws cell appearing at column, row, but with contents for cell column+offsetColumn, row+offsetRow. Used with non-zero offsets for quick draw during manual sequence alignment
	public void redrawBlockOffset(int firstColumn, int firstRow, int lastColumn, int lastRow, int offsetColumn, int offsetRow) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow))
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		Graphics g = matrix.getGraphics();
		if (g == null)
			return;
		for (int i = c1; i <= c2; i++)
			for (int j = r1; j <= r2; j++)
				if (i < getNumColumns() && j < getNumRows())
					redrawCellOffset(i, j, offsetColumn, offsetRow, g);
		g.dispose();
	}

	/* ............................................................................................................... */
	/** Redraw block of cells. */
	public void redrawBlock(int firstColumn, int firstRow, int lastColumn, int lastRow) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow))
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		Graphics g = matrix.getGraphics();
		if (g == null)
			return;
		for (int i = c1; i <= c2; i++)
			for (int j = r1; j <= r2; j++)
				if (i < getNumColumns() && j < getNumRows())
					redrawCell(i, j, g);
		g.dispose();
	}
	/* ............................................................................................................... */
	/** Redraw block of cells. */
	public void redrawBlock(int firstColumn, int lastColumn, Bits whichRows) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || whichRows==null)
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		Graphics g = matrix.getGraphics();
		if (g == null)
			return;
		for (int i = c1; i <= c2; i++)
			for (int j = 0; j <= numRowsTotal; j++)
				if (whichRows.isBitOn(j) && i < getNumColumns() && j < getNumRows())
					redrawCell(i, j, g);
		g.dispose();
	}

	/* ............................................................................................................... */
	/** Redraw block of cells blank. */
	public void redrawBlockBlank(int firstColumn, int firstRow, int lastColumn, int lastRow) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow))
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		Graphics g = matrix.getGraphics();
		if (g == null)
			return;
		for (int i = c1; i <= c2; i++)
			for (int j = r1; j <= r2; j++)
				if (i < getNumColumns() && j < getNumRows())
					matrix.blankCell(i, j, g);
		g.dispose();
	}

	/* ............................................................................................................... */
	/** determines in cell in in the intersection of two blocks. */
	public boolean cellInIntersectionOfBlocks(int column, int row, int firstColumn, int newFirstColumn, int firstRow, int newFirstRow, int lastColumn, int newLastColumn, int lastRow, int newLastRow) {
		if (!columnLegal(column) || !rowLegal(row) || !columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow) || !columnLegal(newFirstColumn) || !columnLegal(newLastColumn) || !rowLegal(newFirstRow) || !rowLegal(newLastRow))
			return false;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		int nc1 = MesquiteInteger.minimum(newFirstColumn, newLastColumn);
		int nc2 = MesquiteInteger.maximum(newFirstColumn, newLastColumn);
		int nr1 = MesquiteInteger.minimum(newFirstRow, newLastRow);
		int nr2 = MesquiteInteger.maximum(newFirstRow, newLastRow);

		return (column >= c1) && (column <= c2) && (column >= nc1) && (column <= nc2) && (row >= r1) && (row <= r2) && (row >= nr1) && (row <= nr2);
	}

	/* ............................................................................................................... */
	/** Redraw block of cells that aren't in the intersection of two blocks. */
	public void redrawDifferenceBlock(int firstColumn, int newFirstColumn, int firstRow, int newFirstRow, int lastColumn, int newLastColumn, int lastRow, int newLastRow) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow) || !columnLegal(newFirstColumn) || !columnLegal(newLastColumn) || !rowLegal(newFirstRow) || !rowLegal(newLastRow))
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn, newFirstColumn, newLastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn, newFirstColumn, newLastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow, newFirstRow, newLastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow, newFirstRow, newLastRow);
		Graphics g = matrix.getGraphics();
		if (g == null)
			return;
		for (int i = c1; i <= c2; i++)
			for (int j = r1; j <= r2; j++)
				if ((!cellInIntersectionOfBlocks(i, j, firstColumn, newFirstColumn, firstRow, newFirstRow, lastColumn, newLastColumn, lastRow, newLastRow)) && i < getNumColumns() && j < getNumRows())
					redrawCell(i, j, g);
		g.dispose();
	}

	/* ............................................................................................................... */
	/** Redraw block of cells that bound selection. */
	public void redrawSelectedBlock() {
		if (anyRowSelected()) {
			repaintAll();
		}
		if (anyColumnSelected()) {
			repaintAll();
		}
		if (anyCellSelected())
			redrawBlock(firstColumnWithSelectedCell(), firstRowWithSelectedCell(), lastColumnWithSelectedCell(), firstColumnWithSelectedCell());
	}

	/* ............................................................................................................... */
	/** This doesnt' seem ever to get called! */
	public void redrawColumn(int column) {
		MesquiteMessage.warnProgrammer("draw column");
		if (columnLegal(column)) {
			MesquiteMessage.warnProgrammer("draw column, column legal");
			redrawColumnName(column);
			Graphics g = matrix.getGraphics();
			if (g == null)
				return;
			for (int i = firstRowVisible; i <= lastRowVisible; i++)
				redrawCell(column, i, g);
			g.dispose();
		}
	}

	/* ............................................................................................................... */
	/** ���� */
	public void redrawColumns(int firstColumn, int lastColumn) {
		for (int column = firstColumn; column <= lastColumn; column++)
			redrawColumn(column);
	}

	/* ............................................................................................................... */
	/** ���� */
	public void redrawRows(int firstRow, int lastRow) {
		System.out.println("redraw rows not working yet");
	}

	/* ............................................................................................................... */
	/**
	 * Draws text into the corner cell. Passed is the graphics context, and the rectangle framing the cell. Must be overridden in subclasses to fill in corner cell appropriately.
	 */
	public void drawCornerCell(Graphics g, int x, int y, int w, int h) {
		cornerCell.redrawName(g);
	}

	/* ............................................................................................................... */
	/**
	 * Draws cell of matrix of given column and row. Must be overridden in subclasses to fill in matrix cell appropriately.  Called only if useString is false for this cell.
	 */
	public void drawMatrixCell(Graphics g, int x, int y, int w, int h, int column, int row, boolean selected) {
		// g.drawString(Integer.toString(row + column), x+2, y+h-2);
	}
	/* ............................................................................................................... */
	//to be overridden to change color; works only for MatrixPanel, and only when useString & overriding permit it
	public Color getBackgroundColor(int column, int row, boolean selected){
		return null;
	}
	/* ............................................................................................................... */
	//to be overridden to change color; works only for MatrixPanel, and only when useString & overriding permit it
	public Color getTextColor(int column, int row, boolean selected){
		return null;
	}
	/* ............................................................................................................... */
	/**
	 * ���� HAVE THIS BY COLUMNS; ALSO HAVE COLUMN NAMES AND ROW NAMES JUSTIFYABLE
	 */
	public int getJustification() {
		return justification;
	}

	/* ............................................................................................................... */
	/** ���� */
	public void setJustification(int justification) {
		this.justification = justification;
	}

	/* ............................................................................................................... */
	/** ���� */
	public void drawMatrixCellString(Graphics g, FontMetrics fm, int x, int y, int w, int h, int column, int row, String supplied) {
		if (!StringUtil.blank(supplied)){
			Shape clip = null;
			boolean useClip=false;
			if (fm == null && g.getFont() != null)
				fm = g.getFontMetrics(g.getFont());
			int svp = StringUtil.getStringVertPosition(fm, y, h, null);
			if (fm == null) {
				clip = g.getClip();
				useClip=true;
				g.setClip(x, y, w, h);
				g.drawString(supplied, x + 2, svp);
			}
			else {
				int length = fm.stringWidth(supplied);
				if (length + 2 > w || svp > y + h) {
					clip = g.getClip();
					useClip=true;
					g.setClip(x, y, w, h);
				}
				int useX;
				if (justification == LEFT)
					useX = x + 2;
				else if (justification == RIGHT)
					useX = w - length - 2;
				else
					useX = x + (w - length) / 2;
				g.drawString(supplied, useX, svp);

			}
			if (useClip)
				g.setClip(clip);
		}
		if (isAttachedNoteAvailable(column, row)){
			g.drawLine(x+w-2,y+1, x+w-2,y+2); //left
			g.drawLine(x+w-2,y+2, x+w,y+2); //bottom
			g.drawLine(x+w,y+1, x+w,y+2); //right
			g.drawLine(x+w-2,y, x+w,y); //top
		}
	}

	/* ............................................................................................................... */
	/** ���� */
	public boolean isAttachedNoteAvailable(int column, int row) {
		return false;
	}
	/* ............................................................................................................... */
	/** ���� */
	public boolean useString(int column, int row) {
		return getMatrixTextForDisplay(column, row) != null;
	}

	/* ............................................................................................................... */
	/** Draws column name. */
	public void drawColumnNameCell(Graphics g, int x, int y, int w, int h, int column) {
		String supplied = getColumnNameTextForDisplay(column);
		if (supplied != null)
			g.drawString(supplied, x + getNameStartOffset(), StringUtil.getStringVertPosition(g, y, h, null));
		if (isAttachedNoteAvailable(column, -1)){
			g.drawLine(x+w-2,y+1, x+w-2,y+2); //left
			g.drawLine(x+w-2,y+2, x+w,y+2); //bottom
			g.drawLine(x+w,y+1, x+w,y+2); //right
			g.drawLine(x+w-2,y, x+w,y); //top
		}
	}


	/* ............................................................................................................... */
	public int getNameStartOffset() {
		return nameStartOffset;
	}

	/* ............................................................................................................... */

	public void setNameStartOffset(int nameStartOffset) {
		this.nameStartOffset = nameStartOffset;
	}

	/* ............................................................................................................... */
	public void setRowNameColor(Graphics g, int row) {
	}

	/* ............................................................................................................... */
	/** Draws row name. */
	public void drawRowNameCell(Graphics g, int x, int y, int w, int h, int row) {
		String supplied = getRowNameTextForDisplay(row);
		if (supplied != null){
			int svp = StringUtil.getStringVertPosition(g, y, h, null);
			int xgnso = x + getNameStartOffset();
			if (showRowNames)
				g.drawString(supplied, xgnso, svp);
		}
		if (isAttachedNoteAvailable(-1, row)){
			g.drawLine(x+w-3,y+1, x+w-3,y+3); //left
			g.drawLine(x+w-3,y+3, x+w,y+3); //bottom
			g.drawLine(x+w,y+1, x+w,y+3); //right
			g.drawLine(x+w-3,y, x+w,y); //top
		}
	}

	/* ............................................................................................................... */
	public void mouseInCell(int modifiers, int column, int subColumn, int row, int subRow, MesquiteTool tool) {
	}

	public void mouseExitedCell(int modifiers, int column, int subColumn, int row, int subRow, MesquiteTool tool) {
	}

	/* ............................................................................................................... */
	/** Called if corner panel is touched. Can be overridden in subclasses to respond to touch. */
	public void cornerTouched(int x, int y, int modifiers) {
		offAllEdits();
		if (anythingSelected()) {
			deselectAllNotify();
			repaintAll();
		}
		outOfBoundsTouched(modifiers, 1);
	}

	/* ............................................................................................................... */
	/** Called if part of panel out of bounds is touched. */
	public void outOfBoundsTouched( int modifiers, int clickCount) {
	}
	/* ............................................................................................................... */
	int rowFirstTouched = -2;

	int columnFirstTouched = -2;

	int rowLastTouched = 0;

	int columnLastTouched = 0;

	/* ............................................................................................................... */
	/** Called if cell is touched. Can be overridden in subclasses to change response to touch. */
	public void cellTouched(int column, int row, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		if (!columnLegal(column) || !rowLegal(row))
			return;
		if (!cellsSelectable && !isCellEditable(column, row))
			return;
		/*
		 * if ((column == columnFirstTouched && row == rowFirstTouched) && (anyCellSelected() || editingMatrixCell())) { offEditMatrixCell(); deselectAllNotify(); selectCell(column,row); redrawCell(column,row); } else
		 */
		if (MesquiteEvent.shiftKeyDown(modifiers) && (anyCellSelected() || editingMatrixCell())) {
			offEditMatrixCell();
			int firstRow = firstRowWithSelectedCell();
			int lastRow = lastRowWithSelectedCell();
			int firstColumn = firstColumnWithSelectedCell();
			int lastColumn = lastColumnWithSelectedCell();

			deselectAllNotify();

			if (!columnLegal(firstColumn) || !rowLegal(firstRow)) {
				selectCell(column, row);
				redrawCell(column, row);
			}
			else {

				int newFirstRow, newLastRow, newFirstColumn, newLastColumn;

				if (row <= firstRow) {
					newLastRow = lastRow;
					newFirstRow = row;
					rowFirstTouched = lastRow;
				}
				else {
					newFirstRow = firstRow;
					newLastRow = row;
					rowFirstTouched = firstRow;
				}

				if (column <= firstColumn) {
					newLastColumn = lastColumn;
					newFirstColumn = column;
					columnFirstTouched = lastColumn;
				}
				else {
					newFirstColumn = firstColumn;
					newLastColumn = column;
					columnFirstTouched = firstColumn;
				}

				selectBlock(newFirstColumn, newFirstRow, newLastColumn, newLastRow);
				// if (newLastColumn>lastColumn)
				// if (newLastRow>lastRow)

				redrawBlock(MesquiteInteger.minimum(firstColumn, newFirstColumn), MesquiteInteger.minimum(firstRow, newFirstRow), MesquiteInteger.maximum(lastColumn, newLastColumn), MesquiteInteger.maximum(lastRow, newLastRow));
				// redrawBlock(columnFirstTouched,rowFirstTouched, column, row);
				// redrawCell(columnFirstTouched,rowFirstTouched);
			}
		}
		else if (MesquiteEvent.commandOrControlKeyDown(modifiers) && (anyCellSelected() || editingMatrixCell())) {
			if (isCellSelected(column, row))
				deselectCell(column, row);
			else
				selectCell(column, row);
			offEditMatrixCell();
			redrawCell(column, row);
			// redrawCell(columnFirstTouched,rowFirstTouched);
			columnFirstTouched = column;
			rowFirstTouched = row;
		}

		else if (anyRowSelected() || anyColumnSelected()) {
			deselectAllNotify();
			columnFirstTouched = column;
			rowFirstTouched = row;
			selectCell(column, row);
			repaintAll();
		}
		else {
			boolean wasSelected = anyCellSelected();
			boolean wasOneCellSelected = (numCellsSelected() == 1);
			int firstRow = -1;
			int lastRow = -1;
			int firstColumn = -1;
			int lastColumn = -1;

			if (!wasOneCellSelected && wasSelected) {
				firstRow = firstRowWithSelectedCell();
				lastRow = lastRowWithSelectedCell();
				firstColumn = firstColumnWithSelectedCell();
				lastColumn = lastColumnWithSelectedCell();
			}

			// redrawSelectedBlock();
			deselectAllNotify();
			offEditMatrixCell();
			// if (columnFirstTouched>=0)
			// redrawBlock(columnFirstTouched,rowFirstTouched, columnLastTouched, rowLastTouched);
			selectCell(column, row);
			redrawCell(column, row);
			if (wasSelected)
				if (wasOneCellSelected) {
					redrawCell(columnLastTouched, rowLastTouched);
				}
				else
					redrawBlock(firstColumn, firstRow, lastColumn, lastRow);
			columnFirstTouched = column;
			rowFirstTouched = row;
		}
		columnLastTouched = column;
		rowLastTouched = row;
	}

	/* ............................................................................................................... */
	/**
	 * Called if mouse dragged over cell. Can be overridden in subclasses to respond.
	 */
	public void cellDrag(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (!columnLegal(column) || !rowLegal(row))
			return;
		if (!cellsSelectable)
			return;
		if ((column != columnLastTouched || row != rowLastTouched) && (anyCellSelected() || editingMatrixCell())) {
			offEditMatrixCell();
			if (!MesquiteEvent.commandOrControlKeyDown(modifiers))
				deselectAllNotify();
			selectBlock(columnFirstTouched, rowFirstTouched, column, row);
			redrawDifferenceBlock(columnFirstTouched, columnFirstTouched, rowFirstTouched, rowFirstTouched, columnLastTouched, column, rowLastTouched, row);
			// redrawBlock(columnFirstTouched,rowFirstTouched, columnLastTouched, rowLastTouched);
			// redrawBlock(columnFirstTouched,rowFirstTouched, column, row);
		}
		columnLastTouched = column;
		rowLastTouched = row;
	}

	/* ............................................................................................................... */
	/**
	 * Called if mouse is dropped on cell. Can be overridden in subclasses to respond.
	 */
	public void cellDropped(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (!singleTableCellSelected())
			defocusCell();
	}

	public boolean touchColumnNameEvenIfSelected(){
		return false;
	}
	/* ............................................................................................................... */
	/** Called if column name is touched. Can be overridden in subclasses to change response to touch. */
	public void columnNameTouched(int column, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		// TODO: have extension of selection if shift or command; have boolean if name editable and if selectable
		if (!columnLegal(column))
			return;
		if (!columnNamesEditable && !columnNamesSelectable)
			return;
		if ((column == columnFirstTouched && !MesquiteEvent.commandOrControlKeyDown(modifiers)) && (anyColumnNameSelected() || editingColumnName())) {
			deselectAllNotify();
			offAllEdits();
			selectColumnName(column);
			columnNames.repaint();
		}
		else if ((MesquiteEvent.shiftKeyDown(modifiers) || MesquiteEvent.commandOrControlKeyDown(modifiers)) && (anyColumnNameSelected() || editingColumnName())) {
			if (MesquiteEvent.commandOrControlKeyDown(modifiers)) {
				if (isColumnNameSelected(column))
					deselectColumnName(column);
				else
					selectColumnName(column);
				columnNames.repaint();
			}
			else {
				deselectAllNotify();
				selectColumnNames(columnFirstTouched, column);
				offAllEdits();
				columnNames.repaint();
			}
		}
		else {
			boolean doRepaint = (anyRowColumnSelected());
			offAllEdits();
			deselectAllNotify(true);
			if (columnNamesAutoEditable)
				editColumnNameCell(column);
			else if (columnNamesSelectable)
				selectColumnName(column);
			if (doRepaint)
				repaintAll();
			else
				columnNames.repaint();
			columnFirstTouched = column;
		}
	}

	/* ............................................................................................................... */
	/** Called if row name is touched. Can be overridden in subclasses to change response to touch. */
	public void rowNameTouched(int row, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		if (!rowNamesEditable && !rowNamesSelectable)
			return;
		if (!rowLegal(row))
			return;
		if ((row == rowFirstTouched && !MesquiteEvent.commandOrControlKeyDown(modifiers)) && (anyRowNameSelected() || editingRowName())) {
			deselectAllNotify();
			offAllEdits();
			selectRowName(row);
			rowNames.repaint();
		}
		else if ((MesquiteEvent.shiftKeyDown(modifiers) || MesquiteEvent.commandOrControlKeyDown(modifiers)) && (anyRowNameSelected() || editingRowName())) {
			if (MesquiteEvent.commandOrControlKeyDown(modifiers)) {
				if (isRowNameSelected(row))
					deselectRowName(row);
				else
					selectRowName(row);
				rowNames.repaint();
			}
			else {
				deselectAllNotify();
				selectRowNames(rowFirstTouched, row);
				offAllEdits();
				rowNames.repaint();
			}
		}
		else {
			boolean doRepaint = (anyRowColumnSelected());
			offAllEdits();
			deselectAllNotify(true);
			if (rowNamesAutoEditable) {
				editRowNameCell(row);
			}
			else if (rowNamesSelectable)
				selectRowName(row);
			if (doRepaint)
				repaintAll(); // repaint section only if something in other panel had been selected (not edited)
			else {
				rowNames.repaint();
			}
			rowFirstTouched = row;
		}
	}

	/* ............................................................................................................... */
	int firstSelectedColumn = 0;

	int firstSelectedRow = 0;


	/* ............................................................................................................... */
	public void setFirstSelectedRow (int row) {
		firstSelectedRow = row;
	}
	/* ............................................................................................................... */
	public void setFirstSelectedColumn (int column) {
		firstSelectedColumn = column;
	}

	/** Called if column is touched. Can be overridden in subclasses to change response to touch. */
	/* ............................................................................................................... */
	public void columnTouched(boolean isArrowEquivalent, int column, int regionInCellH, int regionInCellV, int modifiers) {
		// int[] columnsToRedraw = new int[2];
		// columnsToRedraw[0] = column;
		// columnsToRedraw[1] = column;

		if (!columnsSelectable)
			return;
		if (!columnLegal(column))
			return;
		if (column == -1) {
			firstSelectedColumn = column;
			offAllEdits();
			deselectAllNotify();
			selectRowNames(0, numRowsTotal - 1);
			repaintAll();
		}
		else if ((MesquiteEvent.shiftKeyDown(modifiers) || MesquiteEvent.commandOrControlKeyDown(modifiers)) && anyColumnSelected()) {
			if (MesquiteEvent.commandOrControlKeyDown(modifiers)) {
				if (isColumnSelected(column)) {
					deselectColumn(column);
					if (columnAssociable != null) {
						columnAssociable.setSelected(column, false);
						columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					}
				}
				else {
					selectColumn(column);
					if (columnAssociable != null) {
						columnAssociable.setSelected(column, true);
						columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					}
				}
				// redrawColumnName(column);
				repaintAll();
			}
			else { // shiftkeydown
				deselectAllNotify();
				selectColumns(firstSelectedColumn, column);
				if (columnAssociable != null)
					columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				repaintAll();
			}
		}
		else {
			// redrawColumn(firstSelectedColumn);
			firstSelectedColumn = column;
			offAllEdits();
			deselectAll();
			repaintAll();
			selectColumn(column);
			// redrawColumn(column);
			if (columnAssociable != null) {
				columnAssociable.setSelected(column, true);
				columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
			if (rowAssociable != null)
				rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
	}

	/* ............................................................................................................... */
	public void subRowTouched(int subRow, int column, int regionInCellH, int regionInCellV, int x, int y, int modifiers) {
	}

	/* ............................................................................................................... */
	/** Called if row is touched. Can be overridden in subclasses to change response to touch. */
	public void rowTouched(boolean asArrow, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (!rowsSelectable) {
			return;
		}
		if (!rowLegal(row)) {
			return;
		}
		if ((MesquiteEvent.shiftKeyDown(modifiers) || MesquiteEvent.commandOrControlKeyDown(modifiers)) && anyRowSelected()) {
			if (MesquiteEvent.commandOrControlKeyDown(modifiers)) {
				if (isRowSelected(row)) {
					deselectRow(row);
					if (rowAssociable != null) {
						rowAssociable.setSelected(row, false);
						rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					}
				}
				else {
					selectRow(row);
					if (rowAssociable != null) {
						//	rowAssociable.setSelected(row, true);
						rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
					}
				}
				// redrawRowName(row);
				repaintAll();
			}
			else { // shiftkeydown
				deselectAll();
				selectRows(firstSelectedRow, row);
				if (columnAssociable != null)
					columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				if (rowAssociable != null) {
					//rowAssociable.setSelected(row, true);
					rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				}

				repaintAll();
			}
		}
		else {
			firstSelectedRow = row;
			offAllEdits();
			deselectAll();
			selectRow(row);
			repaintAll();

			if (columnAssociable != null)
				columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			if (rowAssociable != null) {
				//	rowAssociable.setSelected(row, true);
				rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
	}

	/* @@@............................................................................................................... */
	/**
	 * Called if column was dragged and dropped. (after = -1 if dropped in front of first column; -2 if after last.) Can be overridden in subclasses to respond. It is the responsibility of subclass to decide if this results in moving column, or other columns were also selected and move along too. ?Should this be called columnDragDropped?
	 */
	public void selectedColumnsDropped(int after) {
		System.out.println("Columns moved   to " + after);
	}

	/* @@@............................................................................................................... */
	/**
	 * Called if row was dragged and dropped. (after = -1 if dropped above first row; -2 if below last.) Can be overridden in subclasses to respond. It is the responsibility of subclass to decide if this results in moving a row, or other row were also selected and move along too. ?Should this be called rowDragDropped?
	 */
	public void selectedRowsDropped(int after) {
		System.out.println("row moved   to " + after);
	}

	/* ............................................................................................................... */
	/**
	 * Returns text in cell of matrix. Should be overridden in subclasses if text returned is appropriate.
	 */
	public String getText(int column, int row) {
		if (column == -1 && row == -1)
			return "";
		else if (column == -1)
			return getRowNameText(row);
		else if (row == -1)
			return getColumnNameText(column);
		else
			return getMatrixText(column, row);
	}

	/* ............................................................................................................... */
	/**
	 * Returns text in corner of matrix. Should be overridden in subclasses if text returned is appropriate.  This draws low in the corner
	 */
	public synchronized String getCornerText() {
		return "";
	}
	/* ............................................................................................................... */
	/**
	 * Returns text in corner of matrix. Should be overridden in subclasses if text returned is appropriate. This draws high in the corner
	 */
	public synchronized String getUpperCornerText() {
		return "";
	}

	/* ............................................................................................................... */
	/**
	 * Returns text in cell of matrix, possibly adjusted to include asterisks for footnotes, etc.. Should be overridden in subclasses if text returned is appropriate.
	 */
	public synchronized String getMatrixTextForDisplay(int column, int row) {
		return getMatrixText(column, row);
	}

	/* ............................................................................................................... */
	/**
	 * Returns text in cell of matrix. Should be overridden in subclasses if text returned is appropriate.
	 */
	public synchronized String getMatrixText(int column, int row) {
		return "Column " + Integer.toString(column) + " Row " + Integer.toString(row);
	}

	/* ............................................................................................................... */
	/**
	 * Returns text in column name. Should be overridden in subclasses if text returned is appropriate.
	 */
	public String getColumnNameText(int column) {
		return "Column " + Integer.toString(column);
	}

	/* ............................................................................................................... */
	/**
	 * Returns text in column heading of matrix, possibly adjusted to include asterisks for footnotes, etc.. Should be overridden in subclasses if text returned is appropriate.
	 */
	public String getColumnNameTextForDisplay(int column) {
		return getColumnNameText(column);
	}

	/* ............................................................................................................... */
	/**
	 * Returns text in row name. Should be overridden in subclasses if text returned is appropriate.
	 */
	public synchronized String getRowNameText(int row) {
		return "Row " + Integer.toString(row);
	}

	/* ............................................................................................................... */
	/**
	 * Returns text in row name of matrix, possibly adjusted to include asterisks for footnotes, etc.. Should be overridden in subclasses if text returned is appropriate.
	 */
	public synchronized String getRowNameTextForDisplay(int row) {
		return getRowNameText(row);
	}

	/* ............................................................................................................... */
	/**
	 * Called after editing a cell, passing the String resulting. Can be overridden in subclasses to respond to editing.
	 */
	public void returnedMatrixText(int column, int row, String s) {
		System.out.println("Text [" + s + "] returned for Column " + Integer.toString(column) + " Row " + Integer.toString(row));
	}

	/* ............................................................................................................... */
	/**
	 * Called after editing a ColumnName, passing the String resulting. Can be overridden in subclasses to respond to editing.
	 */
	public void returnedColumnNameText(int column, String s) {
		System.out.println("Text [" + s + "] returned for Column " + Integer.toString(column));
	}

	/* ............................................................................................................... */
	/**
	 * Called after editing a row name, passing the String resulting. Can be overridden in subclasses to respond to editing.
	 */
	public void returnedRowNameText(int row, String s) {
		System.out.println("Text [" + s + "] returned for Row " + Integer.toString(row));
	}

	/* ............................................................................................................... */
	/** Remove all edit boxes; don't record the states. */
	public void offAllEditsDontRecord() {//1. 12
		defocusCell();

		matrix.offEditDontRecord();
		rowNames.offEditDontRecord();
		columnNames.offEditDontRecord();
	}
	/* ............................................................................................................... */
	/** Remove all edit boxes. */
	public void offAllEdits() {
		defocusCell();

		matrix.offEdit();
		rowNames.offEdit();
		columnNames.offEdit();
	}
	/* ............................................................................................................... */
	/** Called to just clean up whatever is now being edited etc. in matrix. */
	public void offAllEditingSelection() {
		if (anyCellSelected())
			deselectAllNotify();
		offAllEdits();
	}

	/* ............................................................................................................... */
	/** Remove all edit boxes but the one of the given panel. */
	public void offOtherEdits(EditorPanel panel) {
		if (panel != matrix)
			matrix.offEdit();
		if (panel != rowNames)
			rowNames.offEdit();
		if (panel != columnNames)
			columnNames.offEdit();
	}

	/* ............................................................................................................... */
	/** Place text edit box in cell with current value, to allow user to edit content. */
	public void editMatrixCell(int column, int row) {
		if (!isCellEditable(column, row))
			return;
		if (columnLegal(column) && rowLegal(row)) {
			deselectAllNotify();
			matrix.editCell(column, row);
		}
	}

	/* ............................................................................................................... */
	/** Remove text edit box from cell. */
	public void offEditMatrixCell() {
		matrix.offEdit();
	}

	/* ............................................................................................................... */
	/** Is a matrix cell being edited? */
	public boolean editingMatrixCell() {
		return matrix.getEditing();
	}

	/* ............................................................................................................... */
	/** Requests that the column name receive a editable text field to be edited by user. NON FUNCTIONAL */
	public void editColumnNameCell(int column) {
		if (!columnNamesEditable)
			return;
		if (columnLegal(column))
			;
		columnNames.editCell(column, -1);
	}

	/* ............................................................................................................... */
	/** Is a columnName being edited? */
	public boolean editingColumnName() {
		return columnNames.getEditing();
	}

	/* ............................................................................................................... */
	/** Requests that the row name receive a editable text field to be edited by user. NON FUNCTIONAL */
	public void editRowNameCell(int row) {
		if (!rowNamesEditable)
			return;
		if (rowLegal(row))
			;
		rowNames.editCell(-1, row);
	}

	/* ............................................................................................................... */
	/** Is a row name being edited? */
	public boolean editingRowName() {
		return rowNames.getEditing();
	}

	/* ............................................................................................................... */
	/** Is anything being edited? */
	public boolean editingAnything() {
		return (rowNames.getEditing() || columnNames.getEditing() || matrix.getEditing());
	}

	/* ............................................................................................................... */
	/** true if row in range of matrix. */
	public boolean rowLegal(int row) {
		return (row >= 0 && row < numRowsTotal);
	}

	/* ............................................................................................................... */
	/** true if column in range of matrix. */
	public boolean columnLegal(int column) {
		return (column >= 0 && column < numColumnsTotal);
	}

	/* ............................................................................................................... */
	/**
	 * Sets the Associable whose parts are tied to the columns (e.g., a CharacterData, whose characters are tied to columns) This is used to ensure that the Associable's selected parts are kept synchronous with the selection in this table.
	 */
	public void setColumnAssociable(Associable a) {
		columnAssociable = a;
	}

	/* ............................................................................................................... */
	/** Returns the Associable whose parts are tied to the columns (e.g., a CharacterData, whose characters are tied to columns). */
	public Associable getColumnAssociable() {
		return columnAssociable;
	}

	/* ............................................................................................................... */
	/**
	 * Sets the Associable whose parts are tied to the row (e.g., a Taxa, whose taxa are tied to rows) This is used to ensure that the Associable's selected parts are kept synchronous with the selection in this table.
	 */
	public void setRowAssociable(Associable a) {
		rowAssociable = a;
	}

	/* ............................................................................................................... */
	/** Returns the Associable whose parts are tied to the row (e.g., a Taxa, whose taxa are tied to rows). */
	public Associable getRowAssociable() {
		return rowAssociable;
	}

	/* ............................................................................................................... */
	/** Selects cell. */
	public void selectCell(int column, int row) {
		if (column < 0 && row < 0)
			return;
		else if (column == -1)
			selectRowName(row);
		else if (row == -1)
			selectColumnName(column);
		else if (columnLegal(column) && rowLegal(row))
			cellsSelected[0].setBit(row * numColumnsTotal + column);
	}
	/* ............................................................................................................... */
	/** Selects and redraws cell. */
	public void selectAndRedrawCell(int column, int row) {
		selectCell(column, row);
		redrawCell(column,row);
	}

	/* ............................................................................................................... */
	/** Selects row; DOES NOT notify listeners of ASSOCIABLE; responsibility of subclasses*/
	public void selectRow(int row) {
		if (rowLegal(row)){
			rowsSelected[0].setBit(row);
			if (rowAssociable != null)
				rowAssociable.setSelected(row, true);
		}
	}

	/* ............................................................................................................... */
	/** Selects rows */
	public void selectRows(int first, int last) {
		if (rowLegal(first) && rowLegal(last)) {
			int r1 = MesquiteInteger.minimum(first, last);
			int r2 = MesquiteInteger.maximum(first, last);
			for (int i = r1; i <= r2; i++) {
				rowsSelected[0].setBit(i);
				if (rowAssociable != null) {
					rowAssociable.setSelected(i, true);
				}
			}
		}
	}

	/* ............................................................................................................... */
	/** Select block of cells. */
	public void selectBlock(int firstColumn, int lastColumn, Bits whichRows) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || whichRows==null)
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		for (int i = c1; i <= c2; i++)
			for (int j =0; j <= numRowsTotal ; j++) 
				if (whichRows.isBitOn(j)){
					cellsSelected[0].setBit(j * numColumnsTotal + i);
				}
	}
	/* ............................................................................................................... */
	/** Select block of cells. */
	public void selectBlock(int firstColumn, int firstRow, int lastColumn, int lastRow) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow))
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		for (int i = c1; i <= c2; i++)
			for (int j = r1; j <= r2; j++) {
				cellsSelected[0].setBit(j * numColumnsTotal + i);
			}
	}
	/* ............................................................................................................... */
	/** Select block of cells. */
	public void deSelectBlock(int firstColumn, int firstRow, int lastColumn, int lastRow) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow))
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		for (int i = c1; i <= c2; i++)
			for (int j = r1; j <= r2; j++) {
				cellsSelected[0].clearBit(j * numColumnsTotal + i);
			}
	}


	/* ............................................................................................................... */
	/** Deselects all cells outside the specified rows */
	public void deSelectAndRedrawOutsideBlock(int firstColumn, int lastColumn, Bits whichRows) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || whichRows==null)
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		for (int row = 0; row <= numRowsTotal; row++)
			for (int column = 0; column <= numColumnsTotal; column++) {
				if ((column < c1) || (column > c2) || (!whichRows.isBitOn(row))) { // not in block
					if (isCellSelected(column, row)) {
						deselectCell(column, row);
						redrawCell(column, row);
					}
				}
			}
	}

	/* ............................................................................................................... */
	/** Deselects all cells outside the specified block */
	public void deSelectAndRedrawOutsideBlock(int firstColumn, int firstRow, int lastColumn, int lastRow) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow))
			return;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		for (int row = 0; row <= numRowsTotal; row++)
			for (int column = 0; column <= numColumnsTotal; column++) {
				if ((column < c1) || (column > c2) || (row < r1) || (row > r2)) { // not in block
					if (isCellSelected(column, row)) {
						deselectCell(column, row);
						redrawCell(column, row);
					}
				}
			}
	}

	/* ............................................................................................................... */
	/** returns true iff the column and row are within the bounds of the specified block */
	public boolean cellInBlock(int column, int row, int firstColumn, int firstRow, int lastColumn, int lastRow) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow))
			return false;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		return ((column >= c1) && (column <=c2) && (row >= r1) && (row <= r2)) ;
	}
	/* ............................................................................................................... */
	/** returns true iff the column and row are within the bounds of the specified block */
	public boolean cellInBlock(int column, int row, int firstColumn,int lastColumn, Bits whichRows) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || whichRows==null)
			return false;
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		return ((column >= c1) && (column <=c2) && whichRows.isBitOn(row)) ;
	}
	/* ............................................................................................................... */
	/** Redraws cells in old and new blocks */
	public void redrawOldAndNewBlocks(int firstColumnOld, int firstRowOld, int lastColumnOld, int lastRowOld, int firstColumn, int firstRow, int lastColumn, int lastRow, boolean considerAndMoveSelection) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || !rowLegal(firstRow) || !rowLegal(lastRow))
			return;
		if (!columnLegal(firstColumnOld) || !columnLegal(lastColumnOld) || !rowLegal(firstRowOld) || !rowLegal(lastRowOld))
			return;
		int c1old = MesquiteInteger.minimum(firstColumnOld, lastColumnOld);
		int c2old = MesquiteInteger.maximum(firstColumnOld, lastColumnOld);
		int r1old = MesquiteInteger.minimum(firstRowOld, lastRowOld);
		int r2old = MesquiteInteger.maximum(firstRowOld, lastRowOld);
		int c1min = firstColumnVisible;


		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		int r1 = MesquiteInteger.minimum(firstRow, lastRow);
		int r2 = MesquiteInteger.maximum(firstRow, lastRow);
		Graphics g = matrix.getGraphics();

		for (int row = r1old; row <= r2old; row++)
			for (int column = c1old; column <= c2old; column++) {
				if ((column < c1) || (column > c2) || (row < r1) || (row > r2)) { // not in block
					if ((isCellSelected(column, row)||!considerAndMoveSelection) && !cellInBlock(column, row, c1,r1,c2,r2)) {
						if (considerAndMoveSelection)
							deselectCell(column, row);
						if (column>=firstColumnVisible && column<=lastColumnVisible && row>=firstRowVisible && row<=lastRowVisible)
							redrawCell(column, row, g);
					}
				}
			}
		for (int i = c1; i <= c2; i++)
			for (int j = r1; j <= r2; j++) {
				if (considerAndMoveSelection)
					cellsSelected[0].setBit(j * numColumnsTotal + i);
				if (i>=firstColumnVisible && i<=lastColumnVisible && j>=firstRowVisible && j<=lastRowVisible)
					redrawCell(i, j,g);

			}
	}

	/* ............................................................................................................... */
	/** Redraws cells in old and new blocks */
	public void redrawOldAndNewBlocks(int firstColumnOld, int lastColumnOld, int firstColumn, int lastColumn, Bits whichRows, boolean considerAndMoveSelection) {
		if (!columnLegal(firstColumn) || !columnLegal(lastColumn) || whichRows==null)
			return;
		if (!columnLegal(firstColumnOld) || !columnLegal(lastColumnOld))
			return;
		Graphics g = matrix.getGraphics();

		int c1old = MesquiteInteger.minimum(firstColumnOld, lastColumnOld);
		int c2old = MesquiteInteger.maximum(firstColumnOld, lastColumnOld);
		int c1 = MesquiteInteger.minimum(firstColumn, lastColumn);
		int c2 = MesquiteInteger.maximum(firstColumn, lastColumn);
		if (!considerAndMoveSelection) {
			c1old = MesquiteInteger.minimum(c1old, firstColumnVisible);
			c2old = MesquiteInteger.maximum(c2old, lastColumnVisible);

			c1 = MesquiteInteger.minimum(c1, firstColumnVisible);
			c2 = MesquiteInteger.maximum(c2, lastColumnVisible);
			for (int row = firstRowVisible; row < numRowsTotal && row<=lastRowVisible+1; row++)
				for (int column = c1old; column <= c2old; column++) {
					if ((column < c1) || (column > c2 || !whichRows.isBitOn(row))) { // not in block
						if ((isCellSelected(column, row)) && !cellInBlock(column, row, c1,c2,whichRows)) {
							if (column>=firstColumnVisible && column<=lastColumnVisible+1 && row>=firstRowVisible && row<=lastRowVisible+1)
								redrawCell(column, row,g);
						}
					}
				}
			for (int i = c1; i <= c2; i++)
				for (int j = firstRowVisible; j < numRowsTotal && j<=lastRowVisible+1; j++)
					if (whichRows.isBitOn(j)){
						if (i>=firstColumnVisible && i<=lastColumnVisible+1 && j>=firstRowVisible && j<=lastRowVisible+1)
							redrawCell(i, j,g);
					}
		}
		else	{
			for (int row = 0; row <= numRowsTotal; row++)
				for (int column = c1old; column <= c2old; column++) {
					if ((column < c1) || (column > c2 || !whichRows.isBitOn(row))) { // not in block
						if ((isCellSelected(column, row)||!considerAndMoveSelection) && !cellInBlock(column, row, c1,c2,whichRows)) {
							deselectCell(column, row);
							if (column>=firstColumnVisible && column<=lastColumnVisible+1 && row>=firstRowVisible && row<=lastRowVisible+1)
								redrawCell(column, row,g);
						}
					}
				}
			for (int i = c1; i <= c2; i++)
				for (int j = 0; j <= numRowsTotal; j++) 
					if (whichRows.isBitOn(j)){
						cellsSelected[0].setBit(j * numColumnsTotal + i);
						if (i>=firstColumnVisible && i<=lastColumnVisible+1 && j>=firstRowVisible && j<=lastRowVisible+1)
							redrawCell(i, j,g);
					}
		}
	}

	/* ............................................................................................................... */
	/** Selects column.; DOES NOT notify listeners of ASSOCIABLE; responsibility of subclasses */
	public void selectColumn(int column) {
		if (columnLegal(column)){
			columnsSelected[0].setBit(column);
			if (columnAssociable != null)
				columnAssociable.setSelected(column, true);
		}
	}

	/* ............................................................................................................... */
	/** Selects columns */
	public void selectColumns(int first, int last) {
		if (columnLegal(first) && columnLegal(last)) {
			int r1 = MesquiteInteger.minimum(first, last);
			int r2 = MesquiteInteger.maximum(first, last);
			for (int i = r1; i <= r2; i++) {
				columnsSelected[0].setBit(i);
				if (columnAssociable != null)
					columnAssociable.setSelected(i, true);
			}
		}
	}

	/* ............................................................................................................... */
	/** Selects column names. */
	public void selectColumnNames(int first, int last) {
		if (columnLegal(first) && columnLegal(last)) {
			int r1 = MesquiteInteger.minimum(first, last);
			int r2 = MesquiteInteger.maximum(first, last);
			for (int i = r1; i <= r2; i++) {
				columnNamesSelected[0].setBit(i);
			}
		}
	}

	/* ............................................................................................................... */
	/** Selects column name. */
	public void selectColumnName(int column) {
		if (columnLegal(column))
			columnNamesSelected[0].setBit(column);
	}

	/* ............................................................................................................... */
	/** Selects row names */
	public void selectRowNames(int first, int last) {
		if (rowLegal(first) && rowLegal(last)) {
			int r1 = MesquiteInteger.minimum(first, last);
			int r2 = MesquiteInteger.maximum(first, last);
			for (int i = r1; i <= r2; i++) {
				rowNamesSelected[0].setBit(i);
			}
		}
	}

	/* ............................................................................................................... */
	/** Selects row name. */
	public void selectRowName(int row) {
		if (rowLegal(row))
			rowNamesSelected[0].setBit(row);
	}

	/* ............................................................................................................... */
	/** returns whether anything in matrix is selected. */
	public boolean anythingSelected() {
		return (anyRowSelected() || anyColumnSelected() || anyRowNameSelected() || anyColumnNameSelected() || anyCellSelected());
	}

	/* ............................................................................................................... */
	/** returns whether any cell in matrix is selected in any way. */
	public boolean anyCellSelectedAnyWay() {
		return (anyRowSelected() || anyColumnSelected() || anyCellSelected());
	}
	/* ............................................................................................................... */
	/** returns whether any cell in central matrix in column is selected. */
	public boolean anyCellSelectedInColumnAnyWay(int column) {
		if (isColumnSelected(column))
			return true;
		for (int row = 0; row<getNumRows(); row++)
			if (isCellSelected(column, row))
				return true;
		return false;
	}
	/* ............................................................................................................... */
	/** returns whether any cell in central matrix in column is selected. */
	public boolean anyCellSelectedInRowAnyWay(int row) {
		if (isRowSelected(row))
			return true;
		for (int column = 0; column<getNumColumns(); column++)
			if (isCellSelected(column, row))
				return true;
		return false;
	}

	/* ............................................................................................................... */
	/** returns whether any row or column in matrix is selected. */
	public boolean anyRowColumnSelected() {
		return (anyRowSelected() || anyColumnSelected());
	}

	/* ............................................................................................................... */
	/** returns whether any cell in central matrix is selected. */
	public boolean anyCellSelected() {
		return cellsSelected[0].anyBitsOn();
	}

	/* ............................................................................................................... */
	/** returns whether any row name is selected. */
	public boolean anyRowNameSelected() {
		return rowNamesSelected[0].anyBitsOn();
	}

	/* ............................................................................................................... */
	/** returns whether any column name is selected. */
	public boolean anyColumnNameSelected() {
		return columnNamesSelected[0].anyBitsOn();
	}

	/* ............................................................................................................... */
	/** returns whether any row is selected. */
	public boolean anyRowSelected() {
		return rowsSelected[0].anyBitsOn();
	}

	/* ............................................................................................................... */
	/** returns whether any column is selected. */
	public boolean anyColumnSelected() {
		return columnsSelected[0].anyBitsOn();
	}

	/* ............................................................................................................... */
	/** returns whether anything in the main table (cell, row or column) is selected. */
	public boolean anyMainTableCellSelected() {
		return (anyCellSelected() || anyRowSelected() || anyColumnSelected());
	}

	/* ............................................................................................................... */
	/** returns first row selected. */
	public int firstRowNameSelected() {
		return rowNamesSelected[0].firstBitOn();
	}
	/* ............................................................................................................... */
	/** returns first row selected. */
	public int firstRowSelected() {
		return rowsSelected[0].firstBitOn();
	}

	/* ............................................................................................................... */
	/** returns first column selected. */
	public int firstColumnNameSelected() {
		return columnNamesSelected[0].firstBitOn();
	}
	/* ............................................................................................................... */
	/** returns first column selected. */
	public int firstColumnSelected() {
		return columnsSelected[0].firstBitOn();
	}

	/* ............................................................................................................... */
	/** returns the first column in the central matrix in which there is at least one cell selected. */
	public int firstRowWithSelectedCell() {
		if (cellsSelected[0].anyBitsOn()) {
			for (int row = 0; row < numRowsTotal; row++) {
				for (int column = 0; column < numColumnsTotal; column++)
					if (cellsSelected[0].isBitOn(row * numColumnsTotal + column))
						return row;
			}
			return -1;
		}
		else
			return -1;
	}

	/* ............................................................................................................... */
	/** returns the first column in the central matrix in which there is at least one cell selected. */
	public int lastRowWithSelectedCell() {
		if (cellsSelected[0].anyBitsOn()) {
			for (int row = numRowsTotal - 1; row >= 0; row--) {
				for (int column = 0; column < numColumnsTotal; column++)
					if (cellsSelected[0].isBitOn(row * numColumnsTotal + column))
						return row;
			}
			return -1;
		}
		else
			return -1;
	}

	/* ............................................................................................................... */
	/** returns the first column in the central matrix in which there is at least one cell selected. */
	public int firstColumnWithSelectedCell() {
		if (cellsSelected[0].anyBitsOn()) {
			for (int column = 0; column < numColumnsTotal; column++) {
				for (int row = 0; row < numRowsTotal; row++)
					if (cellsSelected[0].isBitOn(row * numColumnsTotal + column))
						return column;
			}
			return -1;
		}
		else
			return -1;
	}

	/* ............................................................................................................... */
	/** returns the first column in the central matrix in which there is at least one cell selected. */
	public int lastColumnWithSelectedCell() {
		if (cellsSelected[0].anyBitsOn()) {
			for (int column = numColumnsTotal - 1; column >= 0; column--) {
				for (int row = 0; row < numRowsTotal; row++)
					if (cellsSelected[0].isBitOn(row * numColumnsTotal + column))
						return column;
			}
			return -1;
		}
		else
			return -1;
	}

	/* ............................................................................................................... */
	/**
	 * returns the boundaries of the next single block of cells selected within one row, starting from upper left, and going through each row before going to next row. 
	 * If rows and columns passed in as MesquiteInteger are unassigned, then this finds the first case. Subsequent calls, if passed the same MesquiteIntegers retaining 
	 * previously returned values, will find next block.
	 */
	public boolean nextSingleRowBlockSelected(MesquiteInteger row, MesquiteInteger firstColumn, MesquiteInteger lastColumn) {
		if (!anyCellSelectedAnyWay())
			return false;
		if (row==null||firstColumn==null||lastColumn==null){
			MesquiteMessage.warnProgrammer("Wanring: MesquiteIntegers passed to nextSingleRowBlockSelected cannot be null!");
			return false;
		}
		boolean nextRow=false;
		int incomingRow = row.getValue();
		if (!row.isCombinable()) {
			incomingRow = 0;
		}
		
		int incomingLastColumn = lastColumn.getValue();
		if (!lastColumn.isCombinable()){   // can only happen on first time through
			incomingLastColumn = -1;
		}
		else if (incomingLastColumn >= numColumnsTotal - 1) { // end of previous row, need to go to start of next row
			nextRow=true;;
			incomingLastColumn = -1;
		}
		else
			incomingLastColumn++;
		
		if (nextRow)
			incomingRow++;
		
		if (incomingRow >= numRowsTotal)  //incomingRow is 0-based but numRowsTotal is 1-based
			return false;
		
		for (int i = incomingRow; i < numRowsTotal; i++) { // go through the rows
			if (wholeRowSelectedAnyWay(i)) {
				row.setValue(i);
				firstColumn.setValue(0);
				lastColumn.setValue(numColumnsTotal - 1);
				return true;
			}
			else {
				for (int j = incomingLastColumn + 1; j < numColumnsTotal; j++) {
					if (isCellSelectedAnyWay(j, i)) {   
						row.setValue(i);
						firstColumn.setValue(j);
						for (int k = j + 1; k < numColumnsTotal; k++) {
							if (!isCellSelectedAnyWay(k, i)) {
								lastColumn.setValue(k - 1);
								return true;
							}
							else if (k == numColumnsTotal - 1) {
								lastColumn.setValue(numColumnsTotal - 1);
								return true;
							}
						}

					}

				}
			}
			incomingLastColumn = -1;

		}
		return false;
	}

	/* ............................................................................................................... */
	/** returns whether a single contiguous block in row is selected. Returns first contiguous block selected in row regardless if unique */
	public boolean singleContiguousBlockSelected(int row, MesquiteInteger firstColumn, MesquiteInteger lastColumn) {
		if (isRowSelected(row)) {
			if (firstColumn != null)
				firstColumn.setValue(0);
			if (lastColumn != null)
				lastColumn.setValue(numColumnsTotal - 1);
			return true;
		}
		else {
			boolean lastWasSelected = false;
			boolean someSelected = false;
			int startC = firstColumnSelected();
			int lastC = lastColumnSelected();
			for (int j = 0; j < numColumnsTotal; j++) {
				if ((j>=startC && j<=lastC && isColumnSelected(j)) || isCellSelected(j, row)) {
					if (!lastWasSelected && someSelected && j>0) {  //we are in a new section of selected
						return false;
					}
					if (!someSelected && !lastWasSelected && firstColumn != null)
						firstColumn.setValue(j);
					lastWasSelected = true;
					someSelected=true;
				}
				else if (lastWasSelected) {// hit end of selection
					if (lastColumn != null)
						lastColumn.setValue(j - 1);
					lastWasSelected = false;
				}

			}
			if (lastWasSelected && lastColumn != null)
				lastColumn.setValue(numColumnsTotal - 1);
			//if (lastWasSelected && someSelected) {
			//	return false; // second contig block selected at end;
			//}
			return someSelected;
		}
	}

	/* ............................................................................................................... */
	/** returns whether a single contiguous cell block in matrix is selected. Returns first contiguous block selected in matrix regardless if unique */
	public boolean boundedBlockSelected(MesquiteInteger firstRow, MesquiteInteger lastRow, MesquiteInteger firstColumn, MesquiteInteger lastColumn) {
		if (firstRow==null || !firstRow.isCombinable())
			return false;
		if (lastRow==null || !lastRow.isCombinable())
			return false;
		if (firstColumn==null || !firstColumn.isCombinable())
			return false;
		if (lastColumn==null || !lastColumn.isCombinable())
			return false;

		for (int column =firstColumn.getValue(); column <=lastColumn.getValue(); column++) {
			if(isColumnSelected(column) && firstRow.getValue()!=0 && lastRow.getValue()!=numRowsTotal-1 )
				return false;
		}
		if(lastColumn.getValue()<numColumnsTotal-1) {
			if (isColumnSelected(lastColumn.getValue()+1))  // the column just after is selected
				return false;
			for (int row =firstRow.getValue(); row <=lastRow.getValue(); row++) 
				if(isCellSelected(lastColumn.getValue()+1, row) )
					return false;

		}

		if(firstColumn.getValue()>0) {
			if (isColumnSelected(firstColumn.getValue()-1))  // the column just after is selected
				return false;
			for (int row =firstRow.getValue(); row <=lastRow.getValue(); row++) 
				if(isCellSelected(firstColumn.getValue()-1, row) )
					return false;
		}

		for (int row =firstRow.getValue(); row <=lastRow.getValue(); row++) {
			if(isRowSelected(row) && firstColumn.getValue()!=0 && lastColumn.getValue()!=numColumnsTotal-1 )
				return false;
		}

		if(lastRow.getValue()<numRowsTotal-1) {
			if (isRowSelected(lastRow.getValue()+1))  // the row just after is selected
				return false;
			for (int column =firstColumn.getValue(); column <=lastColumn.getValue(); column++) 
				if(isCellSelected(column, lastRow.getValue()+1) )
					return false;

		}
		if(firstRow.getValue()>0) {
			if ( isRowSelected(firstRow.getValue()-1))  // the row just after is selected
				return false;
			for (int column =firstColumn.getValue(); column <=lastColumn.getValue(); column++) 
				if(isCellSelected(column, firstRow.getValue()-1) )
					return false;


		}

		for (int column =firstColumn.getValue(); column <=lastColumn.getValue(); column++) {
			for (int row =firstRow.getValue(); row <=lastRow.getValue(); row++) { 
				if(!isCellSelected(column, row) )
					return false;
			}
		}



		return true;
	}


	/* ............................................................................................................... */
	/** Returns in the MesquiteIntegers the bounds of the selection growing out from the current cell */
	public boolean findBlockSurroundingCell(int centerColumn, int centerRow, MesquiteInteger firstRow, MesquiteInteger lastRow, MesquiteInteger firstColumn, MesquiteInteger lastColumn) {
		int firstR = centerRow;
		int lastR = centerRow;
		int firstC = centerColumn;
		int lastC = centerColumn;
		if (!isCellSelected(centerColumn, centerRow))
			return false;

		for (int row = centerRow; row < getNumRows(); row++) {
			if (isCellSelected(centerColumn, row))
				lastR = row;
			else
				break;
		}
		for (int row = centerRow; row >=0 ; row--) {
			if (isCellSelected(centerColumn, row))
				firstR = row;
			else
				break;
		}
		for (int column = centerColumn; column < getNumColumns(); column++) {
			if (isCellSelected(column, centerRow))
				lastC = column;
			else
				break;
		}
		for (int column = centerColumn; column >=0 ; column--) {
			if (isCellSelected(column, centerRow))
				firstC = column;
			else
				break;
		}
		firstRow.setValue(firstR);
		lastRow.setValue(lastR);
		firstColumn.setValue(firstC);
		lastColumn.setValue(lastC);
		return true;
	}

	/* ............................................................................................................... */
	/** returns whether a single contiguous cell block in matrix is selected. Returns first contiguous block selected in matrix regardless if unique */
	public boolean singleCellBlockSelected(MesquiteInteger firstRow, MesquiteInteger lastRow, MesquiteInteger firstColumn, MesquiteInteger lastColumn) {
		int firstR = -1;
		for (int row = 0; row < getNumRows(); row++) {
			firstColumn.setToUnassigned();
			lastColumn.setToUnassigned();
			if (singleContiguousBlockSelected(row, firstColumn, lastColumn)) { // we've found a row with a singleblock selected.
				firstR = row;
				break;
			}
		}
		if (firstR < 0)
			return false;

		boolean emptyRow = false;
		int lastR = firstR;
		for (int row = firstR; row < getNumRows(); row++) {
			MesquiteInteger firstCol = new MesquiteInteger();
			MesquiteInteger lastCol = new MesquiteInteger();
			if (singleContiguousBlockSelected(row, firstCol, lastCol)) { // we've found a row with a singleblock selected.
				if (emptyRow) // we've encountered an empty row in between two rows with selections
					return false;
				if (firstCol.getValue() != firstColumn.getValue() || lastCol.getValue() != lastColumn.getValue())
					return false;
				lastR = row;
			}
			else
				emptyRow = true;
		}
		firstRow.setValue(firstR);
		lastRow.setValue(lastR);
		return true;
	}

	/* ............................................................................................................... */
	public boolean onlySingleRowBlockSelected(MesquiteInteger row, MesquiteInteger firstColumn, MesquiteInteger lastColumn) {
		MesquiteInteger rowInternal = new MesquiteInteger();
		MesquiteInteger firstColumnInternal = new MesquiteInteger();
		MesquiteInteger lastColumnInternal = new MesquiteInteger();
		boolean foundBlock = nextSingleRowBlockSelected(rowInternal, firstColumnInternal, lastColumnInternal);
		if (!foundBlock)
			return false;
		row.setValue(rowInternal.getValue());
		firstColumn.setValue(firstColumnInternal.getValue());
		lastColumn.setValue(lastColumnInternal.getValue());
		foundBlock = nextSingleRowBlockSelected(rowInternal, firstColumnInternal, lastColumnInternal);
		if (foundBlock)
			return false;
		return true;
	}

	/* ............................................................................................................... */
	/** returns number of cells in central matrix are selected. */
	public int numCellsSelected() {
		return cellsSelected[0].numBitsOn();
	}
	/* ............................................................................................................... */
	/** returns number of cells in central matrix are selected any way. */
	public int numCellsSelectedAnyWay () {
		int num = 0;
		for (int ic= 0; ic< numColumnsTotal; ic++)
			for (int it= 0; it< numRowsTotal; it++)
				if (isCellSelectedAnyWay(ic, it))
					num++;
		return num;
	}
	/* ............................................................................................................... */
	/** returns number of cells in central matrix are selected within a column. */
	public int numCellsSelectedInColumn (int column) {
		if (column == -1)
			return 0;

		int num = 0;
		if (columnLegal(column)) {
			for (int i = 0; i < numRowsTotal; i++)
				if (isCellSelectedAnyWay(column, i))
					num++;
		}
		return num;
	}

	/* ............................................................................................................... */
	/** returns whether or not all cells are selected in a column. */
	public boolean wholeColumnSelectedAnyWay(int column) {
		if (isColumnSelected(column))
			return true;
		int num = numCellsSelectedInColumn(column);
		if (num == numRowsTotal)
			return true;
		return false;
	}

	/* ............................................................................................................... */
	/** returns whether or not all cells are selected in a column. */
	public boolean contiguousColumnsSelected() {
		boolean aWholeColumnSelected = false;
		for (int i = 0; i < numColumnsTotal; i++) {
			if (wholeColumnSelectedAnyWay(i)) {  // we've found the first selected one
				aWholeColumnSelected = true;
				for (int j = i; j < numColumnsTotal; j++) {
					if (!wholeColumnSelectedAnyWay(j)) { 
						// we've found the first unselected one after this
						if (j==i+1)
							return false;  // there's only one column selected
						for (int  emptyColumn=0; emptyColumn < i-1; emptyColumn++) {  // now let's scan through the earlier parts to see if there is anything in there
							for (int  emptyRow=0; emptyRow < numRowsTotal; emptyRow++) {
								if (isCellSelectedAnyWay(emptyColumn, emptyRow))
									return false;
							}
						}
						for (int  emptyColumn=j; emptyColumn <numColumnsTotal; emptyColumn++) {  // now let's scan through the later parts to see if there is anything in there
							for (int  emptyRow=0; emptyRow < numRowsTotal; emptyRow++) {
								if (isCellSelectedAnyWay(emptyColumn, emptyRow))
									return false;
							}
						}
						return true;
					}
				}

			}

		}
		return aWholeColumnSelected;
	}

	/* ............................................................................................................... */
	/** returns number of cells in central matrix are selected within a row. */
	public int numCellsSelectedInRow(int row) {
		if (row == -1)
			return 0;

		int num = 0;
		if (rowLegal(row)) {
			for (int i = 0; i < numColumnsTotal; i++)
				if (isCellSelectedAnyWay(i, row))
					num++;
		}
		return num;
	}

	/* ............................................................................................................... */
	/** returns true if all cells are selected within a row. */
	public boolean allCellsSelectedInRow(int row) {
		if (row == -1)
			return false;

		if (rowLegal(row)) {
			for (int i = 0; i < numColumnsTotal; i++)
				if (!isCellSelectedAnyWay(i, row))
					return false;
		}
		return true;
	}

	/* ............................................................................................................... */
	/** returns whether or not all cells are selected in a row. */
	public boolean wholeRowSelectedAnyWay(int row) {
		if (isRowSelected(row))
			return true;
		if (allCellsSelectedInRow(row))
			return true;
		return false;
	}

	/* ............................................................................................................... */
	/** returns number of row names are selected. */
	public int numRowNamesSelected() {
		return rowNamesSelected[0].numBitsOn();
	}

	/* ............................................................................................................... */
	/** returns number of column names are selected. */
	public int numColumnNamesSelected() {
		return columnNamesSelected[0].numBitsOn();
	}

	/* ............................................................................................................... */
	/** returns number of rows are selected. */
	public int numRowsSelected() {
		return rowsSelected[0].numBitsOn();
	}

	/* ............................................................................................................... */
	/** returns true iff only a single column name, row name, or cell selected. */
	public boolean singleTableCellSelected() {
		int total = cellsSelected[0].numBitsOnPlural();
		if (total > 1)
			return false;
		total += rowNamesSelected[0].numBitsOnPlural();
		if (total > 1)
			return false;
		total += columnNamesSelected[0].numBitsOnPlural();
		if (total > 1)
			return false;
		return ((total == 1) && !anyRowSelected() && !anyColumnSelected());
	}

	/* ............................................................................................................... */
	/** returns column and row of first cell selected (-2, -2 if none). */
	public Dimension getFirstTableCellSelected() {
		if (numColumnsTotal == 0)
			return new Dimension(-2, -2);
		int firstCell = cellsSelected[0].firstBitOn();
		if (firstCell >= 0)
			return new Dimension(firstCell % numColumnsTotal, firstCell / numColumnsTotal);
		firstCell = rowNamesSelected[0].firstBitOn();
		if (firstCell >= 0)
			return new Dimension(-1, firstCell);
		firstCell = columnNamesSelected[0].firstBitOn();
		if (firstCell >= 0)
			return new Dimension(firstCell, -1);
		return new Dimension(-2, -2);
	}
	/* ............................................................................................................... */
	/** returns column and row of first cell selected (-2, -2 if none). */
	public Dimension getLastTableCellSelected() {
		if (numColumnsTotal == 0)
			return new Dimension(-2, -2);
		int lastCell = cellsSelected[0].lastBitOn();
		if (lastCell >= 0)
			return new Dimension(lastCell % numColumnsTotal, lastCell / numColumnsTotal);
		lastCell = rowNamesSelected[0].lastBitOn();
		if (lastCell >= 0)
			return new Dimension(-1, lastCell);
		lastCell = columnNamesSelected[0].lastBitOn();
		if (lastCell >= 0)
			return new Dimension(lastCell, -1);
		return new Dimension(-2, -2);
	}

	/* ............................................................................................................... */
	/** returns number of columns are selected. */
	public int numColumnsSelected() {
		return columnsSelected[0].numBitsOn();
	}

	/* ............................................................................................................... */
	/** returns whether cell in central matrix is selected (does not return true if whole row or column is selected). */
	public boolean isCellSelected(int column, int row) {
		if (columnLegal(column) && rowLegal(row))
			return cellsSelected[0].isBitOn(row * numColumnsTotal + column);
		return false;
	}
	/* ............................................................................................................... */
	/** returns true if any cell in central matrix block is selected (does not return true if whole row or column is selected). */
	public boolean isAnyCellSelectedInBlock(int columnStart, int columnEnd, int rowStart, int rowEnd) {
		for (int column=columnStart; column<=columnEnd; column++)
			for (int row=rowStart; row<=rowEnd; row++)
				if (isCellSelected(column,row))
					return true;
		return false;
	}

	/* ............................................................................................................... */
	/** returns true if any cell in central matrix block is selected (does not return true if whole row or column is selected). */
	public boolean isAnyCellSelectedInBlock(int columnStart, int columnEnd, Bits whichRows) {
		for (int column=columnStart; column<=columnEnd; column++)
			for (int row=0; row<numRowsTotal; row++)
				if (whichRows.isBitOn(row) && isCellSelected(column,row))
					return true;
		return false;
	}


	/* ............................................................................................................... */
	/** returns whether cell in central matrix is selected. */
	public boolean isCellSelectedAnyWay(int column, int row) {
		if (column == -1)
			return isRowNameSelectedAnyWay(row);
		if (row == -1)
			return isColumnNameSelectedAnyWay(column);

		if (columnLegal(column) && rowLegal(row))
			return (cellsSelected[0].isBitOn(row * numColumnsTotal + column)) || isRowSelected(row) || isColumnSelected(column);
		return false;
	}

	public void synchronizeRowSelection(Associable a) {
		if (a == null)
			return;
		rowsSelected[0].clearAllBits();
		boolean first = true;
		for (int i = 0; i < numRowsTotal && i < a.getNumberOfParts(); i++)
			if (a.getSelected(i)) {
				selectRow(i);
				if (first) {
					setFirstSelectedRow(i);
					first=false;
				}
			}
	}

	public void synchronizeColumnSelection(Associable a) {
		if (a == null)
			return;
		columnsSelected[0].clearAllBits();
		boolean first = true;
		for (int i = 0; i < numColumnsTotal && i < a.getNumberOfParts(); i++)
			if (a.getSelected(i)) {
				selectColumn(i);
				if (first) {
					setFirstSelectedColumn(i);
					first=false;
				}
			}
	}

	/* ............................................................................................................... */
	/** returns the last row selected. */
	public int lastRowSelected() {
		for (int i = numRowsTotal - 1; i >= 0; i--)
			if (rowsSelected[0].isBitOn(i))
				return i;
		return -1;
	}
	/* ............................................................................................................... */
	/** returns the last row selected. */
	public int lastRowNameSelected() {
		for (int i = numRowsTotal - 1; i >= 0; i--)
			if (rowNamesSelected[0].isBitOn(i))
				return i;
		return -1;
	}

	/* ............................................................................................................... */
	/** returns the start of the last contiguous block of rows selected. */
	public int startOfLastRowBlockSelected() {
		int lastSelected = lastRowSelected();
		if (lastSelected < 0)
			return lastSelected;
		int i = 0;
		for (i = lastSelected - 1; i >= 0; i--)
			if (!rowsSelected[0].isBitOn(i))
				return i + 1;
		if (i < 0)
			return 0;
		return lastSelected;
	}

	/* ............................................................................................................... */
	/** returns the last column selected. */
	public int lastColumnSelected() {
		return columnsSelected[0].lastBitOn();
	}
	/* ............................................................................................................... */
	/** returns the last column selected. */
	public int lastColumnNameSelected() {
		for (int i = numColumnsTotal - 1; i >= 0; i--)
			if (columnNamesSelected[0].isBitOn(i))
				return i;
		return -1;
	}

	/* ............................................................................................................... */
	/** returns the start of the last contiguous block of columns selected. */
	public int startOfLastColumnBlockSelected() {
		int lastSelected = lastColumnSelected();
		if (lastSelected < 0)
			return lastSelected;
		int i = 0;
		for (i = lastSelected - 1; i >= 0; i--)
			if (!columnsSelected[0].isBitOn(i))
				return i + 1;
		if (i < 0)
			return 0;
		return lastSelected;
	}

	/* ............................................................................................................... */
	/** returns whether row is selected. */
	public boolean isRowSelected(int row) {
		if (rowLegal(row))
			return rowsSelected[0].isBitOn(row);
		return false;
	}

	/* ............................................................................................................... */
	/** is any row selected. */
	public boolean isAnyRowSelected() {
		return rowsSelected[0].anyBitsOn();
	}

	/* ............................................................................................................... */
	/** returns a Bits saying what rows are selected. */
	public Bits getRowsSelected() {
		return rowsSelected[0].cloneBits();
	}

	/* ............................................................................................................... */
	/** returns whether column is selected. */
	public boolean isColumnSelected(int column) {
		if (columnLegal(column))
			return columnsSelected[0].isBitOn(column);
		return false;
	}


	/* ............................................................................................................... */
	/** returns a Bits saying what rows are selected. */
	public Bits getColumnsSelected() {
		return columnsSelected[0].cloneBits();
	}

	/* ............................................................................................................... */
	/** returns whether column name is selected. */
	public boolean isColumnNameSelected(int column) {
		if (columnLegal(column))
			return columnNamesSelected[0].isBitOn(column);
		return false;
	}

	/* ............................................................................................................... */
	/** returns whether row name is selected. */
	public boolean isRowNameSelected(int row) {
		if (rowLegal(row))
			return rowNamesSelected[0].isBitOn(row);
		return false;
	}

	/* ............................................................................................................... */
	/** returns whether column name is selected in any way. */
	public boolean isColumnNameSelectedAnyWay(int column) {
		if (columnLegal(column))
			return columnNamesSelected[0].isBitOn(column) || columnsSelected[0].isBitOn(column);
		return false;
	}

	/* ............................................................................................................... */
	/** returns whether row name is selected in any way. */
	public boolean isRowNameSelectedAnyWay(int row) {
		if (rowLegal(row))
			return rowNamesSelected[0].isBitOn(row) || rowsSelected[0].isBitOn(row);
		return false;
	}

	/* ............................................................................................................... */
	/** deselects all cells, columns, etc etc. */
	public void deselectAll() {
		clearBetweenColumnSelection();
		deselectAllCells(false);
		deselectAllRows(false);
		deselectAllColumns(false);
		deselectAllColumnNames();
		deselectAllRowNames();
	}

	/* ............................................................................................................... */
	/** deselects all cells, columns, etc etc. and notify any Associables linked to rows or columns */
	public void deselectAllNotify() {
		clearBetweenColumnSelection();
		deselectAllCells(true);
		deselectAllRows(true);
		deselectAllColumns(true);
		deselectAllColumnNames(true);
		deselectAllRowNames(true);
	}

	/* ............................................................................................................... */
	/** deselects all cells, columns, etc etc. and notify any Associables linked to rows or columns */
	public void deselectAllNotify(boolean redrawCells) {
		clearBetweenColumnSelection();
		deselectAllCells(redrawCells, true);
		deselectAllRows(true);
		deselectAllColumns(true);
		deselectAllColumnNames(true);
		deselectAllRowNames(true);
	}

	/* ............................................................................................................... */
	/** deselects all cells in central matrix. */
	public void deselectAllCells(boolean notify) {
		cellsSelected[0].clearAllBits();
	}

	/* ............................................................................................................... */
	/** deselects all cells in central matrix. */

	public void deselectAllCells(boolean redrawOld, boolean notify) {
		Graphics g = matrix.getGraphics();
		if (g == null)
			return;
		for (int row = 0; (row < numRowsTotal); row++)
			for (int column = 0; (column < numColumnsTotal); column++)
				if (isCellSelected(column, row)) {
					deselectCell(column, row);
					if (redrawOld)
						redrawCell(column, row, g);
				}
		g.dispose();
	}

	/* ............................................................................................................... */
	/** deselects all rows. */
	public void deselectAllRows(boolean notify) {
		rowsSelected[0].clearAllBits();
		if (rowAssociable != null && rowAssociable.anySelected()) {
			rowAssociable.deselectAll();
			if (notify)
				rowAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
	}

	/* ............................................................................................................... */
	/** deselects all columns. */
	public void deselectAllColumns(boolean notify) {
		columnsSelected[0].clearAllBits();
		if (columnAssociable != null && columnAssociable.anySelected()) {
			columnAssociable.deselectAll();
			if (notify)
				columnAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
	}
	/* ............................................................................................................... */
	/** deselects all columns. */
	public void convertColumnSelectionToRows(boolean notify) {
		for (int row = 0; (row < numRowsTotal); row++)
			if (wholeRowSelectedAnyWay(row)) {
				selectRow(row);
			}
		deselectAllColumns(true);
	}

	/* ............................................................................................................... */
	/** deselects all column names. */
	public void deselectAllColumnNames() {
		columnNamesSelected[0].clearAllBits();
	}

	/* ............................................................................................................... */
	/** deselects all column names. */
	public void deselectAllColumnNames(boolean redrawOld) {
		for (int column = 0; (column < numColumnsTotal); column++)
			if (isColumnNameSelected(column)) {
				deselectColumnName(column);
				if (redrawOld)
					redrawColumnName(column);
			}
	}

	/* ............................................................................................................... */
	/** deselects all row names. */
	public void deselectAllRowNames() {
		rowNamesSelected[0].clearAllBits();
	}

	/* ............................................................................................................... */
	/** deselects all row names. */
	public void deselectAllRowNames(boolean redrawOld) {
		for (int row = 0; (row < numRowsTotal); row++)
			if (isRowNameSelected(row)) {
				deselectRowName(row);
				if (redrawOld)
					redrawRowName(row);
			}
	}

	/* ............................................................................................................... */
	/** deselects cell in central matrix. */
	public void deselectCell(int column, int row) {
		if (column < 0 && row < 0)
			return;
		else if (column == -1)
			deselectRowName(row);
		else if (row == -1)
			deselectColumnName(column);
		else if (columnLegal(column) && rowLegal(row))
			cellsSelected[0].clearBit(row * numColumnsTotal + column);
	}

	/* ............................................................................................................... */
	/** deselects row; DOES NOT UPDATE ASSOCIABLE*/
	public void deselectRow(int row) {
		if (rowLegal(row))
			rowsSelected[0].clearBit(row);

	}

	/* ............................................................................................................... */
	/** deselects column; DOES NOT UPDATE ASSOCIABLE */
	public void deselectColumn(int column) {
		if (columnLegal(column))
			columnsSelected[0].clearBit(column);
	}

	/* ............................................................................................................... */
	/** deselects column name */
	public void deselectColumnName(int column) {
		if (columnLegal(column))
			columnNamesSelected[0].clearBit(column);
	}

	/* ............................................................................................................... */
	/** deselects row name */
	public void deselectRowName(int row) {
		if (rowLegal(row))
			rowNamesSelected[0].clearBit(row);
	}

	/* ............................................................................................................... */
	/** Sets whether the cell is dimmed (true) or not (false). Column -1 is for row names; row -1 is for column names. */
	public void setCellDimmed(int column, int row, boolean dimmed) {
		if (column == -1) {
			if (row == -1)
				cornerSelected[1] = dimmed;
			else if (rowLegal(row))
				rowNamesSelected[1].setBit(row, dimmed);
		}
		else if (row == -1) {
			if (columnLegal(column))
				columnNamesSelected[1].setBit(column, dimmed);
		}
		else if (columnLegal(column) && rowLegal(row))
			cellsSelected[1].setBit(row * numColumnsTotal + column, dimmed);
	}

	/* ............................................................................................................... */
	/** Gets whether the cell is dimmed (true) or not (false). Column -1 is for row names; row -1 is for column names. */
	public boolean getCellDimmed(int column, int row) {
		if (column == -1) {
			if (row == -1)
				return cornerSelected[1];
			else if (rowLegal(row))
				rowNamesSelected[1].isBitOn(row);
		}
		else if (row == -1) {
			if (columnLegal(column))
				return columnNamesSelected[1].isBitOn(column);
		}
		else if (columnLegal(column) && rowLegal(row))
			return cellsSelected[1].isBitOn(row * numColumnsTotal + column);
		return false;
	}

	/* ............................................................................................................... */
	/** Sets whether the cell should be drawn with a triangle to indicate a drop down menu (true) or not (false). Column -1 is for row names; row -1 is for column names. */
	public void setDropDown(int column, int row, boolean enabled) {
		if (column == -1) {
			if (row == -1)
				cornerSelected[2] = enabled;
			else if (rowLegal(row))
				rowNamesSelected[2].setBit(row, enabled);
		}
		else if (row == -1) {
			if (columnLegal(column))
				columnNamesSelected[2].setBit(column, enabled);
		}
		else if (columnLegal(column) && rowLegal(row))
			cellsSelected[2].setBit(row * numColumnsTotal + column, enabled);
	}

	/* ............................................................................................................... */
	/** Gets whether the cell should be drawn with a triangle to indicate a drop down menu (true) or not (false). Column -1 is for row names; row -1 is for column names. */
	public boolean getDropDown(int column, int row) {
		if (column == -1) {
			if (row == -1)
				return cornerSelected[2];
			else if (rowLegal(row))
				rowNamesSelected[2].isBitOn(row);
		}
		else if (row == -1) {
			if (columnLegal(column))
				return columnNamesSelected[2].isBitOn(column);
		}
		else if (columnLegal(column) && rowLegal(row))
			return cellsSelected[2].isBitOn(row * numColumnsTotal + column);
		return false;
	}

	/* ............................................................................................................... */
	/** returns which row is the first visible. */
	public int getFirstRowVisible() {
		return firstRowVisible;
	}

	/* ............................................................................................................... */
	/** returns which column is the first visible. */
	public int getFirstColumnVisible() {
		return firstColumnVisible;
	}

	/* ............................................................................................................... */
	/** sets which row is the first visible. */
	public void setFirstRowVisible(int value, boolean repaintPlease) {
		if (value == firstRowVisible)
			return;
		if (value < getNumRows() && value >= 0)
			firstRowVisible = value;
		if (vertScroll != null)
			vertScroll.setValue(firstRowVisible);
		resetNumRowsVisible();
		if (repaintPlease) {
			repaint();
			matrix.repaint();
			rowNames.repaint();
		}
	}

	/* ............................................................................................................... */
	/** sets which row is the first visible. */
	public void setFirstRowVisible(int value) {
		setFirstRowVisible(value, true);
	}

	/* ............................................................................................................... */
	/** sets which row is the first visible. */
	public void setLastRowVisible(int value, boolean repaintPlease) {
		int firstRow = value - getNumRowsVisible() + 1;
		if (firstRow < 0)
			firstRow = 0;
		setFirstRowVisible(firstRow, repaintPlease);
	}

	/* ............................................................................................................... */
	/** sets which row is the last visible. */
	public void setLastRowVisible(int value) {
		setLastRowVisible(value, true);
	}

	/* ............................................................................................................... */
	/** sets which column is the first visible. */
	public void setFirstColumnVisible(int value, boolean repaintPlease) {
		if (value == firstColumnVisible)
			return;
		if (value < getNumColumns() && value >= 0)
			firstColumnVisible = value;
		if (horizScroll != null)
			horizScroll.setValue(firstColumnVisible);
		resetNumColumnsVisible();
		if (repaintPlease) {
			repaint();
			matrix.repaint();
			columnNames.repaint();
		}

	}

	/* ............................................................................................................... */
	/** sets which column is the first visible. */
	public void setFirstColumnVisible(int value) {
		setFirstColumnVisible(value, true);
	}

	/* ............................................................................................................... */
	/** sets which column is the last visible. */
	public void setLastColumnVisible(int value, boolean repaintPlease) {
		if (value == lastColumnVisible)
			return;
		if (value < getNumColumns() && value >= 0)
			lastColumnVisible = value;
		int firstColumn = value - getNumColumnsVisible() + 1;
		if (firstColumn < 0)
			firstColumn = 0;
		setFirstColumnVisible(firstColumn, repaintPlease);
	}

	/* ............................................................................................................... */
	/** sets which column is the last visible. */
	public void setLastColumnVisible(int value) {
		setLastColumnVisible(value, true);
	}

	AutoScrollThread scrollThread = null;
	/* ............................................................................................................... */
	/** Starts the thread for autoscrolling . */
	public void startAutoScrollThread(MousePanel panel) {
		stopAutoScrollThread();
		scrollThread = new AutoScrollThread(this,panel);
		scrollThread.start();
	}
	/* ............................................................................................................... */
	/** Stops the thread for autoscrolling . */
	public void  stopAutoScrollThread() {
		if (scrollThread !=null)
			scrollThread.abortThread();
	}
	/* ............................................................................................................... */
	public void suppressAutoscroll() {
		if (scrollThread !=null)
			scrollThread.setSuppressed(true);
	}
	/* ............................................................................................................... */
	public void allowAutoscroll() {
		if (scrollThread !=null)
			scrollThread.setSuppressed(false);
	}
	/* ............................................................................................................... */

	/* ............................................................................................................... */
	public boolean checkForAutoScroll(MousePanel panel, int x, int y) {
		if (scrollThread ==null ||  !scrollThread.active())
			return false;
		if (!MesquiteInteger.isCombinable(x) || !MesquiteInteger.isCombinable(y))
			return false;
		boolean scrolled = false;
		if (MesquiteInteger.isCombinable(y) && scrollThread.canAutoscrollVertically())
			if (y<=AUTOSCROLLBOUNDARY && getFirstRowVisible()>0) {
				setFirstRowVisible(getFirstRowVisible()-1);
				scrolled = true;
			}
			else if (y>=(panel.getHeight()-AUTOSCROLLBOUNDARY) && getLastRowVisible()<=getNumRows()) {
				setFirstRowVisible(getFirstRowVisible()+1);
				scrolled = true;
			}
		if (MesquiteInteger.isCombinable(x) && scrollThread.canAutoscrollHorizontally())
			if (x<=AUTOSCROLLBOUNDARY && getFirstColumnVisible()>0) {
				setFirstColumnVisible(getFirstColumnVisible()-1);
				scrolled = true;
			}
			else if (x>=(panel.getWidth()-AUTOSCROLLBOUNDARY) && getLastColumnVisible()<=getNumColumns()) {
				setFirstColumnVisible(getFirstColumnVisible()+1);
				scrolled = true;
			}
		return scrolled;
	}
	/* ............................................................................................................... */
	/** sets the value of the given scroll. */
	public void setValue(TableScroll whichScroll, int value) {
		if (whichScroll == vertScroll) {
			/*if (matrix.hFloat < 0){
				matrix.hFloat = matrix.getWidth() - 30;
				matrix.vFloat = value*(matrix.getHeight()-20)/getNumRows();
			}
			Graphics g = matrix.getGraphics();
			if (g != null){
				g.setColor(ColorDistribution.paleGoldenRod);
				g.fillRect(matrix.hFloat, matrix.vFloat, 40, 20);
				g.setColor(Color.black);
				g.drawRect(matrix.hFloat, matrix.vFloat, 40, 20);
				g.drawString(Integer.toString(value), matrix.hFloat+3, matrix.vFloat + 16);
				g.dispose();
			}*/
			/*	if (whichScroll.getValueIsAdjusting()){
				matrix.hFloat = -1;
				matrix.vFloat = -1;
				matrix.repaint();
			}
			 */	
			setFirstRowVisible(value);
		}
		else {
			/*	if (matrix.hFloat < 0){
				whichScroll.getValue();

			}*/
			setFirstColumnVisible(value);

		}
	}

	/* ............................................................................................................... */
	/** Scrolls to the specified cell. */
	public void scrollToColumn(int column) {
		if (horizScroll != null  && (column<getFirstColumnVisible() || column>getLastColumnVisible()))
			setFirstColumnVisible(column-1);
	}
	/* ............................................................................................................... */
	/** Scrolls to the specified cell. */
	public void scrollToRow(int row) {
		if (vertScroll != null  && (row<getFirstRowVisible() || row>getLastRowVisible()))
			setFirstRowVisible(row-1);
	}
	/* ............................................................................................................... */
	/** resets the value of the horizontal scroll to be current value plus value. */
	public void shiftHorizScroll(int value) {
		if (horizScroll != null)
			setFirstColumnVisible(horizScroll.getValue() + value);
	}

	/* ............................................................................................................... */
	public void clickOutside(){
	}
	/* ............................................................................................................... */
	public void shimmerVerticalOff(Panel panel, int x) {
		GraphicsUtil.shimmerVerticalOn(null,panel,0,matrixHeight,x);
	}

	/* ............................................................................................................... */
	public void shimmerVerticalOn(Panel panel, int x) {
		GraphicsUtil.shimmerVerticalOn(null,panel,0,matrixHeight,x);
	}

	/* ............................................................................................................... */
	public void shimmerVerticalOff(int x) {
		/** used for shimmering lines when columns adjusted */
		shimmerVerticalOff(matrix, x);
	}

	/* ............................................................................................................... */
	public void shimmerVerticalOn(int x) {
		/** used for shimmering lines when columns adjusted */
		shimmerVerticalOn(matrix, x);
	}

	/* ............................................................................................................... */
	public void shimmerHorizontalOff(int y) {
		GraphicsUtil.shimmerHorizontalOn(null,matrix,0,matrixWidth,y);
	}

	/* ............................................................................................................... */
	public void shimmerHorizontalOn(int y) {
		GraphicsUtil.shimmerHorizontalOn(null,matrix,0,matrixWidth,y);
	}
	/* ............................................................................................................... */
	public void deselectAndRedrawAllSelectedRows() {
		for (int it=0; it<getNumRows(); it++) {
			if (isRowSelected(it)) {
				deselectRow(it);
				if (it>=firstRowVisible && it<lastRowVisible)
					redrawFullRow(it);
			}
		}
	}
	/* ............................................................................................................... */
	public void redrawFullRow(int row) {
		if (row<0)
			return;
		Graphics mg = matrix.getGraphics();
		if (mg == null)
			return;
		matrix.redrawRow(mg, row);
		mg.dispose();
		Graphics rg = rowNames.getGraphics();
		if (rg == null)
			return;
		if (showRowNames)
			rowNames.redrawName(rg, row);
		rg.dispose();
	}

	/* ............................................................................................................... */
	public void emphasizeRow(int previousRow, int row, int rowNotToEmphasize, boolean emphasizeSelectedRows, Color color) {
		if (previousRow != row) {
			Graphics mg = matrix.getGraphics();
			if (mg == null)
				return;
			Graphics rg = rowNames.getGraphics();
			if (rg == null) {
				mg.dispose();
				return;
			}
			if (row != rowNotToEmphasize && (emphasizeSelectedRows || !isRowSelected(row))) {
				int left = matrix.leftEdgeOfRow(row);
				int top = matrix.startOfRow(row);
				int right = matrix.rightEdgeOfRow(row);
				GraphicsUtil.shadeRectangle(mg, left + 1, top + 1, right - left, matrix.endOfRow(row) - top, color);
				left = 0;
				top = matrix.startOfRow(row);
				right = getWidth();
				GraphicsUtil.shadeRectangle(rg, left + 1, top + 1, right - left, matrix.endOfRow(row) - top, color);
			}

			if (previousRow > -1) {
				matrix.redrawRow(mg, previousRow);
				if (showRowNames)
					rowNames.redrawName(rg, previousRow);
			}
			rg.dispose();
			mg.dispose();
		}
	}

	public void clearBetweenColumnSelection() {
		setStartBetweenColumnSelection(MesquiteInteger.unassigned);
		setEndBetweenColumnSelection(MesquiteInteger.unassigned);
	}
	public void clearBetweenRowSelection() {
		setStartBetweenRowSelection(MesquiteInteger.unassigned);
		setEndBetweenRowSelection(MesquiteInteger.unassigned);
	}

	public int getEndBetweenColumnSelection() {
		return endBetweenColumnSelection;
	}

	public void setEndBetweenColumnSelection(int endBetweenColumnSelection) {
		this.endBetweenColumnSelection = endBetweenColumnSelection;
	}

	public int getEndBetweenRowSelection() {
		return endBetweenRowSelection;
	}

	public void setEndBetweenRowSelection(int endBetweenRowSelection) {
		this.endBetweenRowSelection = endBetweenRowSelection;
	}

	public int getStartBetweenColumnSelection() {
		return startBetweenColumnSelection;
	}

	public void setStartBetweenColumnSelection(int startBetweenColumnSelection) {
		this.startBetweenColumnSelection = startBetweenColumnSelection;
	}

	public int getStartBetweenRowSelection() {
		return startBetweenRowSelection;
	}

	public void setStartBetweenRowSelection(int startBetweenRowSelection) {
		this.startBetweenRowSelection = startBetweenRowSelection;
	}
	public boolean getShowRowNames() {
		return showRowNames;
	}
	public void setShowRowNames(boolean showRowNames) {
		this.showRowNames = showRowNames;
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int amount = e.getScrollAmount();  //verticalScrollPageIncrement, numColumnsVisible
		boolean blockScroll = e.getScrollType()==MouseWheelEvent.WHEEL_BLOCK_SCROLL;
		boolean vert = !e.isShiftDown();
		boolean upleft=e.getWheelRotation()<0;
		if (vert) {
			if (blockScroll) 
				amount=verticalScrollPageIncrement;
			if (upleft){
				amount = -amount;
				if (vertScroll.getValue()==0)
					amount=0;
			}
			if (amount!=0)
				setValue(vertScroll, vertScroll.getValue()+amount);
		} else {
			if (blockScroll) 
				amount=numColumnsVisible-1;
			if (upleft){
				amount = -amount;
				if (horizScroll.getValue()==0)
					amount=0;
			}
			if (amount!=0)
				setValue(horizScroll, horizScroll.getValue()+amount);
		}

	}
}

/* ======================================================================== */
/** Scrollbar for central matrix of cells in table */
class TableScroll extends MesquiteScrollbar {
	MesquiteTable table;

	public TableScroll(MesquiteTable table, int orientation, int value, int visible, int min, int max) {
		super(orientation, value, visible, min, max);
		this.table = table;
		setFocusTraversalKeysEnabled(false);
	}

	public void scrollTouched() {
		int currentValue = getValue();
		table.setValue(this, currentValue);
	}

	public void print(Graphics g) {
	}
}

/* ======================================================================== */
/** A panel at the bottom of a table or window that can be used for buttons & messages. */
class ControlStrip extends MousePanel {
	String message;

	Color textColor;

	Vector buttons;

	public ControlStrip(int colorScheme) {
		super();
		setLayout(null);
		buttons = new Vector();
		message = "";
		setBackground(ColorTheme.getInterfaceBackground());

	}

	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.drawRect(0, 0, getBounds().width, getBounds().height - 1);
		if (textColor != null)
			g.setColor(textColor);
		if (message != null)
			g.drawString(message, buttons.size() * 20 + 6, 12);
		MesquiteWindow.uncheckDoomed(this);
	}

	public void setTextColor(Color c) {
		textColor = c;
	}

	public void setMessage(String s) {
		if (s == null)
			message = "";
		else
			message = s;
		repaint();
	}

	public void addButton(MesquiteButton s) {
		if (buttons.indexOf(s) < 0) {
			buttons.addElement(s);
			add(s);
		}
		s.setVisible(true);
		resetLocs();
		repaint();
	}

	public void removeButton(MesquiteButton s) {
		buttons.removeElement(s);
		remove(s);
		s.setVisible(false);
		resetLocs();
		repaint();
	}

	private void resetLocs() {
		int x = 4;
		for (int i = 0; i < buttons.size(); i++) {
			MesquiteButton b = (MesquiteButton) buttons.elementAt(i);
			b.setLocation(x, 2);
			x += 20;
		}

	}

	public String getMessage() {
		return message;
	}
}


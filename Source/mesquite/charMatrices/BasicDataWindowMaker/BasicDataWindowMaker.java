/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.BasicDataWindowMaker;

/*~~  */

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.text.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

import java.awt.datatransfer.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import mesquite.lib.table.*;
import mesquite.charMatrices.lib.MatrixInfoExtraPanel;
import mesquite.categ.lib.*;
import mesquite.cont.lib.ContinuousData;

/** Makes and manages the spreadsheet editor for character matrices. */
public class BasicDataWindowMaker extends DataWindowMaker implements CommandableOwner {
	public void getEmployeeNeeds() { // This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CellColorer.class, getName() + " uses various methods to color the cells of the character matrix.", "You can request the method to color cells in the Color Cells submenu of the Matrix menu.  You can also color the text of the cell using the Color Text submenu. ");
		EmployeeNeed e2 = registerEmployeeNeed(DataWindowAssistantI.class, getName() + " can employ various modules to add features to the Character Matrix Editor.", "The modules assisting the Character Matrix Editor are available typically in the Matrix menu of the editor window. ");
		EmployeeNeed e3 = registerEmployeeNeed(DataWindowAssistantA.class, "Various analyses can be done within the context of the Character Matrix Editor.", "You can request such an analysis in the Analysis menu of the Character Matrix Editor. ");
		EmployeeNeed e4 = registerEmployeeNeed(DataUtility.class, "Utilities are available to modify or summarize a matrix in the Character Matrix Editor.", "You can request such a utility in the Utilities submenu of the Matrix menu of the Character Matrix Editor. ");
	}

	public static final short cellWidth = 24;
	public static final short cellHeight = 16;
	CharacterData data;
	BasicDataWindow bdw;
	boolean isExtra = false;
	MesquiteMenuSpec matrixMenu, displayMenu;
	static boolean warnAgainAboutTaxonNameDuplication = true;
	
	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixMenu = makeMenu("Matrix");
		displayMenu = addAuxiliaryMenu("Display");
		resetContainingMenuBar();
		return true;
	}

	public Commandable[] getCommandablesForAccumulation() {
		Commandable[] cs = new Commandable[1];
		cs[0] = new BasicDataWindow();
		return cs;
	}

	public void setAsExtra(boolean e) {
		isExtra = e;
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		if (bdw == null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("getWindow");
		temp.addLine("tell It");
		Snapshot sn = bdw.getSnapshot(file);
		temp.incorporate(sn, true);
		temp.addLine("endTell");
		if (bdw.isVisible())
			temp.addLine("showWindow");
		if (!bdw.widthsSet && !bdw.table.showBirdsEyeView.getValue() && (bdw.table.tight.getValue() || bdw.table.showStates.getValue())) { // needed to ensure appears correctly first time under some OS's
			temp.addLine("getWindow");
			temp.addLine("tell It");
			temp.addLine("\tforceAutosize");
			temp.addLine("endTell");
		}
		if (!bdw.isVisible())
			temp.addLine("hideWindow");
		return temp;
	}

	MesquiteInteger pos = new MesquiteInteger();

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the data matrix shown by the data window", null, commandName, "getDataSet")) {
			return data;
		}
		else if (checker.compare(this.getClass(), "Hides the data matrix window", null, commandName, "hideWindow")) {
			if (bdw != null){
				bdw.hide();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Makes a data editor window (but doesn't display it)", "[number or reference string of data set to be shown]", commandName, "makeWindow")) {
			if (bdw != null)
				return bdw;
			CharacterData data = getProject().getCharacterMatrixByReference(checker.getFile(), parser.getFirstToken(arguments));
			if (data != null) {
				makeDataWindow(data);
				return bdw;
			}
		}

		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	public void resignCellColorer(MesquiteModule m) {
		if (bdw != null)
			bdw.resignCellColorer(m);
	}

	public void requestCellColorer(MesquiteModule m, int ic, int it, String message) {
		if (bdw != null)
			bdw.requestCellColorer(m, ic, it, message);
		// return false;
	}

	public void demandCellColorer(MesquiteModule m, int ic, int it, String message) {
		if (bdw != null)
			bdw.demandCellColorer(m, ic, it, message);
	}

	public void selectSameColor(int column, int row, boolean restrictToCharacter, boolean restrictToTaxon, boolean contiguous, boolean subtractFromSelection) {
		if (bdw != null)
			bdw.selectSameColor(column, row, restrictToCharacter, restrictToTaxon, contiguous, subtractFromSelection);
	}
	
	public void selectDataBlockInTaxon(int column, int row) {
		if (bdw != null)
			bdw.selectBlockInTaxon(column, row);

	}

	public void selectSameColorRow(int column, int row, boolean subtractFromSelection) {
		if (bdw != null)
			bdw.selectSameColorRow(column, row, subtractFromSelection);
	}

	/* ................................................................................................................. */
	public void linkEditor(DataWindowMaker mb, boolean linkeeIsNew) {
		if (bdw != null) {
			MesquiteWindow w = mb.getModuleWindow();
			if (w != null && w instanceof TableWindow) {
				MatrixTable t = bdw.getMatrixTable();
				t.linkTable(((TableWindow) w).getTable(), linkeeIsNew);
			}
		}
	}

	public void unlinkEditor(DataWindowMaker mb) {
		if (bdw != null) {
			MesquiteTable w = mb.getTable();
			if (w != null && w instanceof MatrixTable) {
				MatrixTable t = bdw.getMatrixTable();
				t.unlinkTable(w);
			}
		}
	}

	public MesquiteTable getTable() {
		if (bdw == null)
			return null;
		return bdw.getMatrixTable();
	}

	/* ................................................................................................................. */
	public void makeDataWindow(CharacterData data) {
		this.data = data;
		setModuleWindow(bdw = new BasicDataWindow(this, data));
		resetContainingMenuBar();
		resetAllWindowsMenus();
		if (!MesquiteThread.isScripting() && bdw != null) {
			bdw.setVisible(true);
			bdw.contentsChanged();
			bdw.toFront();
		}
	}

	/* ................................................................................................................. */
	public CharacterData getCharacterData() {
		if (data.isDisposed())
			return null;
		return data;
	}

	/* ................................................................................................................. */
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
		parametersChanged();
		if (isExtra) {
			whichWindow.dispose();
			iQuit();
		}
	}

	/* ................................................................................................................. */
	public boolean showCitation() {
		return true;
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() {
		return false;
	}

	/* ................................................................................................................. */
	public String getName() {
		return "Character Matrix Editor";
	}

	public void fileReadIn(MesquiteFile file){
		if (bdw != null)
			bdw.requestFocus();
	}
	/* ................................................................................................................. */
	// public BasicDataWindow getBasicDataWindow() {
	// return bdw;
	// }

	/* ................................................................................................................. */

	/** returns an explanation of what the module does. */
	public String getExplanation() {
		return "Makes editor windows to edit character data.";
	}

	public String getParameters() {
		if (bdw == null)
			return null;
		return bdw.getParameters();
	}

	/* ................................................................................................................. */
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (bdw != null)
			bdw.resetColorerIfNeeded(employee);
		if (employee == bdw.table.cellColorer) {
			bdw.table.repaintAll();
			if (bdw.showColorLegend.getValue()) {
				ColorRecord[] colors = ((CellColorer) bdw.table.cellColorer).getLegendColors();
				bdw.setColorLegend(colors, ((CellColorer) bdw.table.cellColorer).getColorsExplanation(), true);

			}
		}
		super.employeeParametersChanged(employee, source, notification);
	}

	public static boolean getWarnAgainAboutTaxonNameDuplication() {
		return warnAgainAboutTaxonNameDuplication;
	}

	public static void setWarnAgainAboutTaxonNameDuplication(boolean warnAgainAboutTaxonNameDuplication) {
		BasicDataWindowMaker.warnAgainAboutTaxonNameDuplication = warnAgainAboutTaxonNameDuplication;
	}
}

/* ======================================================================== */
class BasicDataWindow extends TableWindow implements MesquiteListener {
	CharacterData data;

	protected TableTool scrollTool;
	MatrixTable table;
	int windowWidth = 420;
	int windowHeight = 280;
	int columnWidth = 12;
	boolean widthsSet = false;
	TableTool arrowTool, ibeamTool;// , charWandTool;//, taxaWandTool
	MesquiteString cellColorerName, textColorerName, rowNamesColorerName, columnNamesColorerName, bgColorName;
	private int findNumber = 0;
	private String findFootnoteString = "";
	MesquiteBoolean constrainedCW = new MesquiteBoolean(true);
	MesquiteBoolean scrollLinkedTables = new MesquiteBoolean(false);
	MesquiteBoolean linkedScrolling = new MesquiteBoolean(true);
	MesquiteBoolean showTaxonNames = new MesquiteBoolean(true);
	MesquiteBoolean useDiagonal = new MesquiteBoolean(false);
	MesquiteBoolean allowAutosize;
	boolean oldShowStates;
	boolean oldSuppress;
	int oldColumnsWidths;
	MesquiteMenuItemSpec linkedScrollingItem = null;
	MesquiteButton colorLegendButton;
	MesquiteButton birdsEyeButton;
	MesquiteBoolean interleaved;
	MatrixInfoPanel matrixInfoPanel;
	boolean matrixInfoPanelEverShown = false;
	MesquiteBoolean infoPanelOn;
	MesquiteBoolean editingNotPermitted = new MesquiteBoolean(false);
	MesquiteBoolean showPaleExcluded = new MesquiteBoolean(false);

	public BasicDataWindow() {
	}

	public BasicDataWindow(BasicDataWindowMaker ownerModule, CharacterData data) {
		super(ownerModule, true); // INFOBAR
		this.data = data;
		ownerModule.setModuleWindow(this);
		ownerModule.setUndoMenuItemEnabled(false);
		allowAutosize = new MesquiteBoolean(!(data instanceof MolecularData));
		infoPanelOn = new MesquiteBoolean(false);
		useDiagonal.setValue(data.useDiagonalCharacterNames());
		// this.ownerModule = ownerModule;
		setWindowSize(windowWidth, windowHeight);
		if (data != null)
			columnWidth = data.getDefaultColumnWidth();
		MesquiteSubmenuSpec mssa = ownerModule.addSubmenu(ownerModule.addAuxiliaryMenu("Analysis:Matrix"), "Analyses for Matrix", MesquiteModule.makeCommand("newAssistant", this), DataWindowAssistantA.class);
		mssa.setCompatibilityCheck(data.getStateClass());
		table = new MatrixTable((BasicDataWindowMaker) ownerModule, this, data, data.getTaxa().getNumTaxa(), data.getNumChars(), windowWidth, windowHeight, 110);
		/**/
		/*	*/
		table.setQuickMode(true);
		setDefaultAnnotatable(data);
		data.getTaxa().addListener(this);
		data.addListener(this);
		addToWindow(table);
		table.setLocation(0, 0);
		table.setVisible(true);
		table.setColumnAssociable(data);
		table.setRowAssociable(data.getTaxa());
		if (data instanceof MolecularData)
			setIcon(MesquiteModule.getRootImageDirectoryPath() + "windowIcons/matrixSequence.gif");
		else
			setIcon(MesquiteModule.getRootImageDirectoryPath() + "windowIcons/matrix.gif");

		MesquiteButton infoButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("toggleInfoPanel", this), null, true, MesquiteModule.getRootImageDirectoryPath() + "showInfo.gif", 12, 16);
		infoButton.setUseWaitThread(false);
		infoButton.setShowBackground(false);
		infoButton.setButtonExplanation("Show Matrix Info Panel");
		// infoBar.addExtraButton(MesquiteModule.getRootImageDirectoryPath() + "showInfo.gif", MesquiteModule.makeCommand("toggleInfoPanel", this));
		table.addControlButton(infoButton);
		MesquiteButton listButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("showList", this), null, true, MesquiteModule.getRootImageDirectoryPath() + "listC.gif", 12, 16);
		listButton.setShowBackground(false);
		listButton.setButtonExplanation("Show List of Characters window");
		listButton.setUseWaitThread(false);
		table.addControlButton(listButton);
		listButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("showTaxaList", this), null, true, MesquiteModule.getRootImageDirectoryPath() + "listT.gif", 12, 16);
		listButton.setShowBackground(false);
		listButton.setButtonExplanation("Show List of Taxa window");
		table.addControlButton(listButton);
		colorLegendButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("toggleColorsPanel", this), null, true, MesquiteModule.getRootImageDirectoryPath() + "colorLegend.gif", 12, 16);
		colorLegendButton.setShowBackground(false);
		colorLegendButton.setButtonExplanation("Show/Hide Colors Legend");
		colorLegendButton.setUseWaitThread(false);

		if (data instanceof MolecularData) {
			birdsEyeButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("toggleBirdsEye", this), null, true, ownerModule.getPath() + "birdsEye.gif", 12, 16);
			birdsEyeButton.setShowBackground(false);
			birdsEyeButton.setButtonExplanation("Bird's eye view");
			birdsEyeButton.setUseWaitThread(false);
			table.addControlButton(birdsEyeButton);
		}

		oldShowStates = table.showStates.getValue();
		oldColumnsWidths = table.getColumnWidthsUniform();
		oldSuppress = table.suppressAutosize;
		interleaved = new MesquiteBoolean(data.interleaved);
		MesquiteSubmenuSpec cmm = ownerModule.addSubmenu(null, "Current Matrix");
		ownerModule.addItemToSubmenu(null, cmm, "Rename Matrix...", MesquiteModule.makeCommand("renameMatrix", this));
		ownerModule.addItemToSubmenu(null, cmm, "Delete Matrix...", MesquiteModule.makeCommand("deleteMatrix", this));
		ownerModule.addItemToSubmenu(null, cmm, "Show Character List", MesquiteModule.makeCommand("showList", this));
		ownerModule.addCheckMenuItemToSubmenu(null, cmm, "Write Interleaved", MesquiteModule.makeCommand("toggleInterleaved", this), interleaved);
		ownerModule.addItemToSubmenu(null, cmm, "Missing Data Symbol...", MesquiteModule.makeCommand("setUnassignedSymbol", this));
		ownerModule.addItemToSubmenu(null, cmm, "Inapplicable Symbol...", MesquiteModule.makeCommand("setInapplicableSymbol", this));
		ownerModule.addCheckMenuItem(null, "Show Matrix Info Panel", ownerModule.makeCommand("toggleInfoPanel", this), infoPanelOn);
		editingNotPermitted.setValue(data.isEditInhibited());
		
		ownerModule.addCheckMenuItemToSubmenu(null, cmm,"Editing Not Permitted", ownerModule.makeCommand("toggleEditingNotPermitted", this), editingNotPermitted);
		ownerModule.addMenuSeparator();
		
		
		MesquiteSubmenuSpec mCC = ownerModule.addSubmenu(ownerModule.displayMenu, "Color Matrix Cells", MesquiteModule.makeCommand("colorCells", this), ownerModule.getEmployeeVector());
		mCC.setListableFilter(CellColorerMatrix.class);

		mCC.setCompatibilityCheck(data.getStateClass());
		cellColorerName = new MesquiteString();
		mCC.setSelected(cellColorerName);
		ownerModule.addCheckMenuItem(ownerModule.displayMenu, "Show Color Legend", MesquiteModule.makeCommand("toggleColorsPanel", this), showColorLegend);

		MesquiteSubmenuSpec mCC3 = ownerModule.addSubmenu(ownerModule.displayMenu, "Color Taxon Names Background", MesquiteModule.makeCommand("colorRowNames", this), ownerModule.getEmployeeVector());
		mCC3.setListableFilter(CellColorerTaxa.class);
		mCC3.setCompatibilityCheck(data.getStateClass());
		rowNamesColorerName = new MesquiteString();
		mCC3.setSelected(rowNamesColorerName);

		MesquiteSubmenuSpec mCC4 = ownerModule.addSubmenu(ownerModule.displayMenu, "Color Character Name Background", MesquiteModule.makeCommand("colorColumnNames", this), ownerModule.getEmployeeVector());
		mCC4.setListableFilter(CellColorerCharacters.class);
		mCC4.setCompatibilityCheck(data.getStateClass());
		columnNamesColorerName = new MesquiteString();
		mCC4.setSelected(columnNamesColorerName);

		MesquiteSubmenuSpec mCC2 = ownerModule.addSubmenu(ownerModule.displayMenu, "Color Text", MesquiteModule.makeCommand("colorText", this), ownerModule.getEmployeeVector());
		mCC2.setListableFilter(CellColorer.class);
		mCC2.setCompatibilityCheck(data.getStateClass());
		textColorerName = new MesquiteString();
		mCC2.setSelected(textColorerName);


		MesquiteSubmenuSpec mSetColor = ownerModule.addSubmenu(ownerModule.displayMenu, "Assign Color to Selected", MesquiteModule.makeCommand("assignColor", this), ColorDistribution.standardColorNames);
		ownerModule.addMenuItem(ownerModule.displayMenu, "Remove Color from Selected", MesquiteModule.makeCommand("removeColor", this));

		MesquiteSubmenuSpec mmis = ownerModule.addSubmenu(ownerModule.displayMenu, "Background Color", MesquiteModule.makeCommand("setBackground", this));
		mmis.setList(ColorDistribution.standardColorNames);
		bgColorName = new MesquiteString();
		mmis.setSelected(bgColorName);

		ownerModule.addMenuItem(ownerModule.displayMenu, "-", null);
		ownerModule.addCheckMenuItem(ownerModule.displayMenu, "Bird's Eye View", MesquiteModule.makeCommand("toggleBirdsEye", this), table.showBirdsEyeView);
		MesquiteSubmenuSpec widthsSubmenu = ownerModule.addSubmenu(ownerModule.displayMenu, "Widths");
		//COLUMNS&ROWS
		table.setColumnNamesDiagonal(useDiagonal.getValue());
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, widthsSubmenu, "Narrow Columns", MesquiteModule.makeCommand("toggleTight", this), table.tight);
		ownerModule.addItemToSubmenu(ownerModule.displayMenu, widthsSubmenu, "Bird's Eye Width...", MesquiteModule.makeCommand("birdsEyeWidth", this));
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, widthsSubmenu, "Thin Rows", MesquiteModule.makeCommand("toggleThinRows", this), table.thinRows);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, widthsSubmenu, "Constrain Auto Widths", MesquiteModule.makeCommand("toggleConstrainCW", this), constrainedCW);
		table.setConstrainMaxAutoColumn(constrainedCW.getValue());
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, widthsSubmenu, "Enable Column Auto-Size", MesquiteModule.makeCommand("toggleAllowAutosize", this), table.allowAutosize);
		ownerModule.addItemToSubmenu(ownerModule.displayMenu, widthsSubmenu, "Restore Column Auto-Size", MesquiteModule.makeCommand("forceAutosize", this));
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, widthsSubmenu, "Auto-size includes Char. Names", MesquiteModule.makeCommand("toggleAutoWCharNames", this), table.autoWithCharNames);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, widthsSubmenu, "Auto-size Taxon Names", MesquiteModule.makeCommand("toggleAutoTaxonNames", this), table.autoRowNameWidth);
		MesquiteSubmenuSpec namesSubmenu = ownerModule.addSubmenu(ownerModule.displayMenu, "Names");
		//NAMES
		if (data.getClass() == CategoricalData.class || data.getClass() == ContinuousData.class)
			ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, namesSubmenu, "Diagonal Character Names", MesquiteModule.makeCommand("toggleDiagonal", this), useDiagonal);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, namesSubmenu, "Show Taxon Names", MesquiteModule.makeCommand("toggleShowTaxonNames", this), showTaxonNames);
		if (data instanceof CategoricalData && !(data instanceof DNAData) && !(data instanceof ProteinData))
			ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, namesSubmenu, "Show State Names", MesquiteModule.makeCommand("toggleShowNames", this), table.showNames);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, namesSubmenu, "Show States", MesquiteModule.makeCommand("toggleShowStates", this), table.showStates);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, namesSubmenu, "Show Default Char. Names", MesquiteModule.makeCommand("toggleShowDefaultCharNames", this), table.showDefaultCharNames);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, namesSubmenu, "Bold Cell Text", MesquiteModule.makeCommand("toggleShowBoldCellText", this), table.showBoldCellText);

		
		MesquiteSubmenuSpec softnessSubmenu = ownerModule.addSubmenu(ownerModule.displayMenu, "Lighten");
		//COLORS
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, softnessSubmenu, "Lighten Grid", MesquiteModule.makeCommand("toggleShowPaleGrid", this), table.showPaleGrid);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, softnessSubmenu, "Lighten Cell Colors", MesquiteModule.makeCommand("toggleShowPaleCellColors", this), table.showPaleCellColors);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, softnessSubmenu, "Lighten Gaps/Inapplicable", MesquiteModule.makeCommand("togglePaleInapplicable", this), table.paleInapplicable);
		ownerModule.addCheckMenuItemToSubmenu(ownerModule.displayMenu, softnessSubmenu, "Lighten Excluded Characters", MesquiteModule.makeCommand("toggleShowPaleExcluded", this), showPaleExcluded);
		
		//CHANGES
		ownerModule.addCheckMenuItem(ownerModule.displayMenu, "Show Changes Since Saved", MesquiteModule.makeCommand("toggleShowChanges", this), table.showChanges);
		if (data instanceof CategoricalData && !(data instanceof DNAData) && !(data instanceof ProteinData))
			ownerModule.addCheckMenuItem(ownerModule.displayMenu, "Lined States Explanation", MesquiteModule.makeCommand("toggleSeparateLines", this), table.statesSeparateLines);
		linkedScrollingItem = ownerModule.addCheckMenuItem(ownerModule.displayMenu, "Linked Scrolling", MesquiteModule.makeCommand("toggleLinkedScrolling", this), linkedScrolling);
		linkedScrollingItem.setEnabled(false);


		String selectExplanation = "This tool selects items in the matrix.  By holding down shift while clicking, the selection will be extended from the first to the last touched cell. ";
		selectExplanation += " A block of cells can be selected either by using shift-click to extend a previous selection, or by clicking on a cell and dragging with the mouse button still down";
		selectExplanation += " Discontinous selections are allowed, and can be obtained by a \"meta\"-click (right mouse button click, or command-click on a MacOS system). ";
		MesquiteCommand dragCommand = MesquiteModule.makeCommand("arrowDragCell", this);
		dragCommand.setSuppressLogging(true);
		arrowTool = new TableTool(this, "arrow", MesquiteModule.getRootImageDirectoryPath(), "arrow.gif", 4, 2, "Select", selectExplanation, MesquiteModule.makeCommand("arrowTouchCell", this), dragCommand, MesquiteModule.makeCommand("arrowDropCell", this));
		arrowTool.setIsArrowTool(true);
		arrowTool.setUseTableTouchRules(true);
		addTool(arrowTool);
		setCurrentTool(arrowTool);
		arrowTool.setInUse(true);
		// ibeam
		ibeamTool = new TableTool(this, "ibeam", MesquiteModule.getRootImageDirectoryPath(), "ibeam.gif", 7, 7, "Edit", "This tool can be used to edit the contents of cells in the matrix.", MesquiteModule.makeCommand("editCell", (Commandable) table), null, null);
		ibeamTool.setWorksOnRowNames(true);
		ibeamTool.setWorksOnColumnNames(true);
		addTool(ibeamTool);
		ibeamTool.setEnabled(!data.isEditInhibited());

		ListableVector v = ownerModule.getEmployeeVector();

		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#AlterData");
		
		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#AlignSequences");
		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#AddDeleteData");
		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#SearchData");
		// ownerModule.addMenuSeparator();
		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#NoColor");
		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#ColorByState");
		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#ColorCells");
		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#CharGroupColor");
		ownerModule.hireNamedEmployee(DataWindowAssistantI.class, "#TaxonGroupColor");

		ownerModule.hireAllOtherCompatibleEmployees(DataWindowAssistantI.class, data.getStateClass());
		Enumeration enumeration = ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			if (obj instanceof DataWindowAssistantI) {
				DataWindowAssistantI init = (DataWindowAssistantI) obj;
				if (init instanceof DataWindowAssistantID || init instanceof CategDataEditorInitD)
					init.setMenuToUse(ownerModule.displayMenu);
				
				init.setTableAndData(table, data);
			}
		}
		
		ownerModule.hireAllCompatibleEmployees(CharTableAssistantI.class, data.getStateClass());
		enumeration = ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			if (obj instanceof CharTableAssistantI) {
				CharTableAssistantI init = (CharTableAssistantI) obj;
				init.setTableAndData(table, data, false);
			}
		}
		ownerModule.hireAllEmployees(TaxaTableAssistantI.class);
		enumeration = ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			if (obj instanceof TaxaTableAssistantI) {
				TaxaTableAssistantI init = (TaxaTableAssistantI) obj;
				init.setTableAndTaxa(table, data.getTaxa(), true);
			}
		}
		if (data instanceof CategoricalData && !(data instanceof MolecularData) && ownerModule.findEmployeeWithDuty(mesquite.categ.StateNamesEditor.StateNamesEditor.class) != null) {
			MesquiteButton statesButton = new MesquiteButton(ownerModule, MesquiteModule.makeCommand("showStateNamesEditor", this), null, true, MesquiteModule.getRootImageDirectoryPath() + "listS.gif", 12, 16);
			statesButton.setShowBackground(false);
			statesButton.setButtonExplanation("Show State Names Editor window");
			statesButton.setUseWaitThread(false);
			table.addControlButton(statesButton);
		}
		MesquiteSubmenuSpec mss2 = ownerModule.addSubmenu(null, "Utilities", MesquiteModule.makeCommand("doUtility", this));
		mss2.setList(DataUtility.class);
		mss2.setCompatibilityCheck(data.getStateClass());

		MesquiteMenuItemSpec msct = ownerModule.addMenuItem("Move Selected Characters To...", ownerModule.makeCommand("moveCharsTo", this));
		msct.setShortcut(KeyEvent.VK_M);
		ownerModule.addMenuItem("Move Selected Taxa To...", ownerModule.makeCommand("moveTaxaTo", this));
		ownerModule.addMenuItem("Move Selected Block...", ownerModule.makeCommand("moveSelectedBlock", this));

		MesquiteSubmenuSpec mss4 = ownerModule.addSubmenu(null, "Character Inclusion/Exclusion");
		ownerModule.addItemToSubmenu(null, mss4, "Include Selected Characters", ownerModule.makeCommand("includeSelectedCharacters", this));
		ownerModule.addItemToSubmenu(null, mss4, "Exclude Selected Characters", ownerModule.makeCommand("excludeSelectedCharacters", this));

		// private, to fix frameshifted footnotes
		// ownerModule.addMenuItem( "Move Footnotes...", ownerModule.makeCommand("moveFootnotes", this));

		MesquiteSubmenuSpec mss2t = ownerModule.addSubmenu(null, "Taxon Utilities", MesquiteModule.makeCommand("doTaxonUtility", this));
		mss2t.setList(TaxonUtility.class);
		MesquiteSubmenuSpec mss3 = ownerModule.addSubmenu(null, "Taxon Names", MesquiteModule.makeCommand("doNames", this));
		mss3.setList(TaxonNameAlterer.class);
		ownerModule.addMenuSeparator();
		
	
		MesquiteModule noColor = ownerModule.findEmployeeWithName("#NoColor", true);
		if (data.colorCellsByDefault()) {
			MesquiteModule mbc = ownerModule.findEmployeeWithName("#ColorByState", true);
			setCellColorer(mbc);
		}
		else {
			setCellColorer(noColor);
		}
		MesquiteModule groupColor = ownerModule.findEmployeeWithName("#TaxonGroupColor", true);
		setRowNamesColorer(groupColor);
		groupColor = ownerModule.findEmployeeWithName("#CharGroupColor", true);
		setColumnNamesColorer(groupColor);
		setTextColorer(noColor);
		MesquiteSubmenuSpec mShowDataInfoStrip = ownerModule.addSubmenu(ownerModule.displayMenu, "Add Char Info Strip", ownerModule.makeCommand("hireDataInfoStrip", this), DataColumnNamesAssistant.class);
		//ownerModule.hireAllCompatibleEmployees(DataColumnNamesAssistant.class, data.getStateClass());
		
	/*
		 * TableTool colorWandTool = new TableTool(this, "colorMagicWand", ownerModule.getPath(), "colorWand.gif", 1,1,"Select same color", "This tool selects cells of the same color", MesquiteModule.makeCommand("selectSameColor", this) , null, null); colorWandTool.setWorksOnColumnNames(false); colorWandTool.setWorksOnRowNames(false); colorWandTool.setWorksOnMatrixPanel(true); colorWandTool.setWorksOnCornerPanel(false); addTool(colorWandTool); /*
		 * 
		 * scrollTool = new TableTool(this, "simScroller", ownerModule.getPath(), "simScrollerRight.gif", 8, 8,"Scrolls between similar items", "This tool scrolls to other similar items", MesquiteModule.makeCommand("simScroll", this) , null, null); scrollTool.setOptionImageFileName("simScrollerLeft.gif", 8, 8); scrollTool.setWorksOnColumnNames(true); scrollTool.setWorksOnRowNames(false); scrollTool.setWorksOnMatrixPanel(false); scrollTool.setWorksOnCornerPanel(false); scrollTool.setSpecialToolForColumnNamesInfoStrips(true);
		 * scrollTool.setOptionsCommand(MesquiteModule.makeCommand("scrollToolOptions", this)); addTool(scrollTool);
		 */

		// scrollTool.setPopUpOwner(ownerModule);
		// scrollTool.setUseMenubar(false); //menu available by touching on button
		// ownerModule.addCheckMenuItem(null, "Scroll linked windows", MesquiteModule.makeCommand("toggleScrollLinkedTables", this), scrollLinkedTables);

		matrixInfoPanel = new MatrixInfoPanel(this);
		matrixInfoPanel.setMatrix(data);
		ownerModule.hireAllCompatibleEmployees(MatrixInfoPanelAssistantI.class, data.getStateClass());
		enumeration = ownerModule.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			if (obj instanceof MatrixInfoPanelAssistantI) {
				MatrixInfoPanelAssistantI init = (MatrixInfoPanelAssistantI) obj;
				matrixInfoPanel.addExtraPanel(init.getPanel(matrixInfoPanel));
			}
		}
		matrixInfoPanel.startCellInfo();
		checkSelectionMatrixInfoPanel();
		setShowAnnotation(true);
		table.requestFocus();
		MesquiteWindow.addKeyListener(this, table);

		MesquiteMenuItemSpec mm = ownerModule.addMenuItem(MesquiteTrunk.editMenu, "Find Footnote...", MesquiteModule.makeCommand("findFootnoteString", this));
		mm.setShortcut(KeyEvent.VK_H);
		checkUndoMenuStatus();

		resetTitle();
	}
public void requestFocus(){
	table.requestFocus();
}
	/* ................................................................................................................. */
	/**
	 * When called the window will determine its own title. MesquiteWindows need to be self-titling so that when things change (names of files, tree blocks, etc.) they can reset their titles properly
	 */
	public void resetTitle() {
		String t;
		if (data != null && data.hasTitle()) {
			if (data.uniquelyNamed())
				t = data.getName();
			else
				t = data.getName() + " [" + data.getID() + "]";
		}
		else {
			t = "Character Matrix";
		}
		setTitle(t);
	}

	/* ................................................................................................................. */
	void setMatrixInfoPanel(boolean show) {
		infoPanelOn.setValue(show);
		if (show) {
			matrixInfoPanelEverShown = true;
			addSidePanel(matrixInfoPanel, MatrixInfoPanel.width);
			matrixInfoPanel.setVisible(true);
			String title = "Matrix Information";
			matrixInfoPanel.repaint();
		}
		else {
			if (matrixInfoPanel != null)
				removeSidePanel(matrixInfoPanel);
		}
	}

	/* ................................................................................................................. */
	void matrixInfoPanelGoAway() {
		setMatrixInfoPanel(false);
	}

	/* ................................................................................................................. */

	private boolean linked = false;

	void setTableLinked(boolean linked) {
		linkedScrollingItem.setEnabled(linked);
		if (this.linked != linked && linked) {
			this.linked = linked; // newly linked; need to sync
			table.setFirstColumnVisibleLinked(table.getFirstColumnVisible());
			table.setFirstRowVisibleLinked(table.getFirstRowVisible());
		}
		MesquiteTrunk.resetMenuItemEnabling();
	}

	/* ................................................................................................................. */
	public String searchData(String s, MesquiteString commandResult) {
		if (StringUtil.blank(s) || data == null)
			return "<h2>Nothing to search for (searched: \"" + s + "\")</h2>";
		if (commandResult != null)
			commandResult.setValue((String) null);
		String cRD = null;
		String cRT = null;
		String listData = data.searchData(s, commandResult);
		String listSequences = "";
		String cRS = null;
		if (s.length() > 2 && data instanceof MolecularData) {
			MolecularData mData = (MolecularData) data;
			String seq = StringUtil.delete(s, '\t');
			long[] states = new long[seq.length()];
			boolean goodSequence = true;
			boolean hasGaps = false;
			for (int i = 0; i < seq.length(); i++) {
				states[i] = mData.fromChar(seq.charAt(i));
				if (states[i] == CategoricalState.impossible)
					goodSequence = false;
				if (states[i] == CategoricalState.inapplicable)
					hasGaps = true;
			}
			if (goodSequence) {

				int numFound = 0;

				for (int it = 0; it < mData.getNumTaxa(); it++) {

					for (int ic = 0; ic < mData.getNumChars(); ic++) {
						int ls = sequenceMatches(states, hasGaps, mData, it, ic);
						if (ls > 0) {
							cRS = "selectSequence:" + it + " " + ic + " " + ls + " " + getID();
							listSequences += "<li>Subsequence in taxon " + (it + 1) + " starting at site " + (ic + 1) + ": <a href=\"" + cRS + "\">Select</a></li>";
							numFound++;
						}
					}
				}
				if (numFound != 1)
					cRS = null;
			}
		}

		if (commandResult != null)
			cRD = commandResult.getValue();
		String listTaxa = data.getTaxa().searchData(s, commandResult);
		if (commandResult != null)
			cRT = commandResult.getValue();
		if (!StringUtil.blank(cRD) && StringUtil.blank(listTaxa) && StringUtil.blank(listSequences))
			commandResult.setValue(cRD);
		else if (StringUtil.blank(listData) && !StringUtil.blank(cRT) && StringUtil.blank(listSequences))
			commandResult.setValue(cRT);
		else if (StringUtil.blank(listData) && StringUtil.blank(listTaxa) && !StringUtil.blank(cRS))
			commandResult.setValue(cRS);

		String results = "";
		if (!StringUtil.blank(listSequences))
			results += listSequences;
		if (!StringUtil.blank(listData))
			results += listData;
		if (!StringUtil.blank(listTaxa))
			results += listTaxa;
		if (!StringUtil.blank(results))

			return "<h2>Matches to search string: \"" + s + "\"</h2>" + results;
		else
			return "<h2>No matches found (searched: \"" + s + "\")</h2>";
	}

	int sequenceMatches(long[] states, boolean hasGaps, MolecularData mData, int it, int ic) {
		boolean done = false;
		int ik = ic;

		for (int ist = 0; ist < states.length; ist++) {
			if (ik >= mData.getNumChars())
				return 0;
			long state = states[ist];
			if (hasGaps) { // compare both sequences literally
				long compareTo = mData.getState(ik++, it);
				if ((CategoricalState.dataBitsMask & state) != (CategoricalState.dataBitsMask & compareTo))
					return 0;
			}
			else { // go to next non-gap character and compare
				long compareTo = CategoricalState.inapplicable;

				while (ik < mData.getNumChars() && (compareTo = mData.getState(ik++, it)) == CategoricalState.inapplicable) {
				}

				if ((CategoricalState.dataBitsMask & state) != (CategoricalState.dataBitsMask & compareTo))
					return 0;
			}

		}
		if (hasGaps)
			return states.length;
		else
			return ik - ic;

	}

	public UndoInstructions setUndoInstructions(int changeClass, int ic, int it, Object oldState, Object newState) {
		return new UndoInstructions(changeClass, ic, it, oldState, newState, data, table);
	}

	/* ................................................................................................................. */
	public int numDataColumnNamesAssistants() {
		int num = 0;
		if (ownerModule == null)
			return 0;
		for (int i = 0; i < ownerModule.getNumberOfEmployees(); i++) {
			MesquiteModule e = (MesquiteModule) ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof DataColumnNamesAssistant) {
				num++;
			}
		}
		return num;
	}

	/* ................................................................................................................. */
	public DataColumnNamesAssistant getDataColumnNamesAssistant(int num) {
		int count = 0;
		if (ownerModule == null)
			return null;
		for (int i = 0; i < ownerModule.getNumberOfEmployees(); i++) {
			MesquiteModule e = (MesquiteModule) ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof DataColumnNamesAssistant) {
				if (count >= num)
					return (DataColumnNamesAssistant) e;
				count++;
			}
		}
		return null;
	}

	public boolean showFindMenuItems() {
		return true;
	}

	public String getFindLabel() {
		return "Find String in Matrix...";
	}

	public String getFindMessageName() {
		return "cell of Character Matrix";
	}

	/* ................................................................................................................. */
	public MesquiteTable getTable() {
		return table;
	}

	/* ................................................................................................................. */
	private boolean containsShift(String s) {
		if (s == null)
			return false;
		else
			return (s.indexOf("shift") >= 0);
	}

	/* ................................................................................................................. */
	/* highlights ith cell with footnote */
	boolean highlightCellWithFootnote(String s, int i) {
		MesquiteTable table = getTable();
		if (table == null)
			return false;
		int count = 0;
		Taxa taxa = data.getTaxa();
		// search taxon footnotes
		for (int it = 0; it < table.getNumRows(); it++) {
			String c = taxa.getAnnotation(it);
			if (StringUtil.foundIgnoreCase(c, s)) {
				if (count == i) {
					selectAndFocus(-1, it);
					return true;
				}
				count++;
			}
		}
		// search character footnotes
		for (int ic = 0; ic < table.getNumColumns(); ic++) {
			String c = data.getAnnotation(ic);
			if (StringUtil.foundIgnoreCase(c, s)) {
				if (count == i) {
					selectAndFocus(ic, -1);
					return true;
				}
				count++;
			}
		}
		// search cell footnotes
		for (int ic = 0; ic < table.getNumColumns(); ic++) {
			for (int it = 0; it < table.getNumRows(); it++) {

				String c = data.getAnnotation(ic, it);
				if (StringUtil.foundIgnoreCase(c, s)) {
					if (count == i) {
						selectAndFocus(ic, it);
						return true;
					}
					count++;
				}
			}
		}
		return false;
	}

	/* ................................................................................................................. */
	int rowFirstTouched = 0;

	int columnFirstTouched = 0;

	int rowLastTouched = 0;

	int columnLastTouched = 0;

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.incorporate(super.getSnapshot(file), false);
		temp.addLine("setTool " + getCurrentTool().getName());

		if (table != null && table.cellColorer != null)
			temp.addLine("colorCells ", (MesquiteModule) table.cellColorer);
		if (table != null && table.rowNamesColorer != null)
			temp.addLine("colorRowNames ", (MesquiteModule) table.rowNamesColorer);
		if (table != null && table.columnNamesColorer != null)
			temp.addLine("colorColumnNames ", (MesquiteModule) table.columnNamesColorer);
		if (table != null && table.textColorer != null)
			temp.addLine("colorText ", (MesquiteModule) table.textColorer);
		if (table != null && table.bgColor != null) {
			String bName = ColorDistribution.getStandardColorName(table.bgColor);
			if (bName != null)
				temp.addLine("setBackground " + StringUtil.tokenize(bName));// quote
		}
		temp.addLine("toggleShowNames " + table.showNames.toOffOnString());
		temp.addLine("toggleShowTaxonNames " + showTaxonNames.toOffOnString());
		temp.addLine("toggleTight " + table.tight.toOffOnString());
		temp.addLine("toggleThinRows " + table.thinRows.toOffOnString());
		temp.addLine("toggleShowChanges " + table.showChanges.toOffOnString());
		temp.addLine("toggleSeparateLines " + table.statesSeparateLines.toOffOnString());
		temp.addLine("toggleShowStates " + table.showStates.toOffOnString());
		temp.addLine("toggleAutoWCharNames " + table.autoWithCharNames.toOffOnString());
		temp.addLine("toggleAutoTaxonNames " + table.autoRowNameWidth.toOffOnString());
		temp.addLine("toggleShowDefaultCharNames " + table.showDefaultCharNames.toOffOnString());
		temp.addLine("toggleConstrainCW " + constrainedCW.toOffOnString());
		if (widthsSet)
			temp.addLine("setColumnWidth " + columnWidth);
		temp.addLine("toggleBirdsEye " + table.showBirdsEyeView.toOffOnString());
		temp.addLine("toggleShowPaleGrid " + table.showPaleGrid.toOffOnString());
		temp.addLine("toggleShowPaleCellColors " + table.showPaleCellColors.toOffOnString());
		temp.addLine("toggleShowPaleExcluded " + showPaleExcluded.toOffOnString());
		temp.addLine("togglePaleInapplicable " + table.paleInapplicable.toOffOnString());
		temp.addLine("toggleShowBoldCellText " + table.showBoldCellText.toOffOnString());
		temp.addLine("toggleAllowAutosize " + table.allowAutosize.toOffOnString());
		temp.addLine("toggleColorsPanel " + showColorLegend.toOffOnString());
		if (data instanceof CategoricalData && !(data instanceof MolecularData)) {
			temp.addLine("toggleDiagonal " + useDiagonal.toOffOnString());
			temp.addLine("setDiagonalHeight " + ((ColumnNamesPanel) table.getColumnNamesPanel()).getDiagonalHeight());

		}

		temp.addLine("toggleLinkedScrolling " + linkedScrolling.toOffOnString());
		temp.addLine("toggleScrollLinkedTables " + scrollLinkedTables.toOffOnString());

		for (int i = 0; i < ownerModule.getNumberOfEmployees(); i++) {
			MesquiteModule e = (MesquiteModule) ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof DataColumnNamesAssistant) {
				temp.addLine("hireDataInfoStrip ", e);
			}
		}
		for (int i = 0; i < ownerModule.getNumberOfEmployees(); i++) {
			Object e = ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof DataWindowAssistant && !(e instanceof DataWindowAssistantI)) {
				temp.addLine("newAssistant ", ((MesquiteModule) e));
			}
		}
		if (matrixInfoPanelEverShown) {
			if (matrixInfoPanel != null) {
				temp.addLine("getInfoPanel");
				temp.addLine("tell It");
				temp.incorporate(matrixInfoPanel.getSnapshot(file), true);
				temp.addLine("endTell");
			}
			temp.addLine("toggleInfoPanel " + infoPanelOn.toOffOnString());
		}
//			temp.addLine("toggleEditingNotPermitted " + editingNotPermitted.toOffOnString());   //WAYNEASK:  Why is this here?
		return temp;
	}

	NameReference colorNameRef = NameReference.getNameReference("color");

	private void setColor(int ic, int it, int c) {
		if (data == null)
			return;
		if (ic < 0 && it < 0) {
		}
		else if (ic < 0) { // taxon
			data.getTaxa().setAssociatedLong(colorNameRef, it, c);
		}
		else if (it < 0) { // character
			data.setAssociatedLong(colorNameRef, ic, c);
		}
		else if (!MesquiteLong.isCombinable(c) || c < 0) {
			data.setCellObject(colorNameRef, ic, it, null);
		}
		else {
			MesquiteInteger ms = new MesquiteInteger((int) c);
			data.setCellObject(colorNameRef, ic, it, ms);
		}
	}

	/* ................................................................................................................. */
	private void removeColor(int ic, int it) {
		setColor(ic, it, -1);
	}

	void colorsPanelGoAway() {
		if (table.cellColorer != null)
			table.cellColorer.colorsLegendGoAway();
		setColorLegend(null, null, false);
	}

	void goToColor(Color c) {
		if (c == null)
			return;
		if ((table.cellColorer == null || ((MesquiteModule) table.cellColorer).nameMatches("#NoColor")) && (table.columnNamesColorer == null || ((MesquiteModule) table.columnNamesColorer).nameMatches("#NoColor")))
			return;

		if (!(table.cellColorer == null || ((MesquiteModule) table.cellColorer).nameMatches("#NoColor")))
			for (int ic = 0; ic < data.getNumChars(); ic++) {
				CommandRecord.tick("Looking for color in cells of character " + (ic + 1));
				for (int it = 0; it < data.getNumTaxa(); it++)
					if (c.equals(table.cellColorer.getCellColor(ic, it))) {
						table.setFocusedCell(ic, it, true);
						return;
					}
			}
		CommandRecord.tick("Looking for color in character headings");
		if (!(table.columnNamesColorer == null || ((MesquiteModule) table.columnNamesColorer).nameMatches("#NoColor")))
			for (int ic = 0; ic < data.getNumChars(); ic++)
				if (c.equals(table.columnNamesColorer.getCellColor(ic, -1))) {
					table.setFocusedCell(ic, -1, true);
					return;
				}
	}

	/* ................................................................................................................. */
	int numContigFound = 0;

	boolean[][] contigSel;

	void selectContiguous(int seedColumn, int seedRow, Color c, boolean subtractFromSelection, boolean horizontal, boolean forwards, int level) {
		if (seedColumn < 0 || seedRow < 0 || seedColumn >= table.getNumColumns() || seedRow >= table.getNumRows() || contigSel[seedColumn][seedRow])
			return;
		level++;
		boolean done = false;
		if (horizontal) {
			if (forwards) {
				int ic;
				for (ic = seedColumn; ic < table.getNumColumns() && !done; ic++)
					done = contigSel[ic][seedRow] || !checkCell(ic, seedRow, c, subtractFromSelection);

				int end = ic - 1;
				for (ic = seedColumn; ic < end; ic++) {
					selectContiguous(ic, seedRow + 1, c, subtractFromSelection, !horizontal, true, level);
					selectContiguous(ic, seedRow - 1, c, subtractFromSelection, !horizontal, false, level);
				}
			}
			else {
				int ic;
				for (ic = seedColumn; ic >= 0 && !done; ic--)
					done = contigSel[ic][seedRow] || !checkCell(ic, seedRow, c, subtractFromSelection);

				int end = ic + 1;
				for (ic = seedColumn; ic > end; ic--) {
					selectContiguous(ic, seedRow + 1, c, subtractFromSelection, !horizontal, true, level);
					selectContiguous(ic, seedRow - 1, c, subtractFromSelection, !horizontal, false, level);
				}
			}
		}
		else {
			if (forwards) {
				int it;
				for (it = seedRow; it < table.getNumRows() && !done; it++)
					done = contigSel[seedColumn][it] || !checkCell(seedColumn, it, c, subtractFromSelection);
				int end = it - 1;
				for (it = seedRow; it < end; it++) {
					selectContiguous(seedColumn + 1, it, c, subtractFromSelection, !horizontal, true, level);
					selectContiguous(seedColumn - 1, it, c, subtractFromSelection, !horizontal, false, level);
				}
			}
			else {
				int it;
				for (it = seedRow; it >= 0 && !done; it--)
					done = contigSel[seedColumn][it] || !checkCell(seedColumn, it, c, subtractFromSelection);
				int end = it + 1;
				for (it = seedRow; it > end; it--) {
					selectContiguous(seedColumn + 1, it, c, subtractFromSelection, !horizontal, true, level);
					selectContiguous(seedColumn - 1, it, c, subtractFromSelection, !horizontal, false, level);
				}
			}
		}
	}

	boolean checkCell(int ic, int it, Color c, boolean subtractFromSelection) {
		if (contigSel[ic][it])
			return false;
		contigSel[ic][it] = true;
		// table.selectCell(ic, it); //select cell
		if (satisfiesCriteria(c, table.cellColorer.getCellColor(ic, it))) {
			numContigFound++;
			if (numContigFound % 100 == 0)
				CommandRecord.tick(Integer.toString(numContigFound) + " cells found");
			if (subtractFromSelection) {
				table.deselectCell(ic, it); // deselect cell
			}
			else
				table.selectCell(ic, it); // select cell
			// table.repaintAll();
			return true;
		}
		return false;
	}

	private boolean satisfiesCriteria(Color c, Color c2) {

		if (c == c2)
			return true;
		else if (c != null && c2 != null) {
			if (c.equals(c2))
				return true;
		}
		return false;
	}

	public void selectSameColor(int column, int row, boolean restrictToCharacter, boolean restrictToTaxon, boolean contiguous, boolean subtractFromSelection) {
		if (data == null || table == null || table.cellColorer == null || ((MesquiteModule) table.cellColorer).nameMatches("#NoColor"))
			return;
		Color c = table.cellColorer.getCellColor(column, row);
		if (contiguous) {
			if (contigSel == null || contigSel.length != table.getNumColumns() || contigSel[0].length != table.getNumRows())
				contigSel = new boolean[table.getNumColumns()][table.getNumRows()];
			for (int i = 0; i < contigSel.length; i++)
				for (int k = 0; k < table.getNumRows(); k++)
					contigSel[i][k] = false;
			numContigFound = 0;
			selectContiguous(column + 1, row, c, subtractFromSelection, true, true, 0);
			selectContiguous(column - 1, row, c, subtractFromSelection, true, false, 0);
			selectContiguous(column, row + 1, c, subtractFromSelection, false, true, 0);
			selectContiguous(column, row - 1, c, subtractFromSelection, false, false, 0);
			checkCell(column, row, c, subtractFromSelection);
		}
		else {
			int rowStart = 0;
			int rowEnd = data.getNumTaxa();
			int columnStart = 0;
			int columnEnd = data.getNumChars();
			if (restrictToTaxon) {
				rowStart = row;
				rowEnd = row + 1;
			}
			else if (restrictToCharacter) {
				columnStart = column;
				columnEnd = column + 1;
			}
			for (int ic = columnStart; ic < columnEnd; ic++)
				for (int it = rowStart; it < rowEnd; it++) {
					Color c2 = table.cellColorer.getCellColor(ic, it);
					if (satisfiesCriteria(c, c2)) {
						if (subtractFromSelection)
							table.deselectCell(ic, it);
						else
							table.selectCell(ic, it);
					}
				}
			table.repaintAll();
		}

	}
	
	public void selectBlockInTaxon(int column, int row) {
		if (data == null || table == null)
			return;
		if (!data.isInapplicable(column,  row)) {
			for (int ic=column; ic>=0; ic--){
				if (!data.isInapplicable(ic, row))
					table.selectCell(ic, row);
				else
					break;
			}
			for (int ic=column+1; ic<data.getNumChars(); ic++){
				if (!data.isInapplicable(ic, row))
					table.selectCell(ic, row);
				else
					break;
			}
	
		}


	}


	public void selectSameColorRow(int column, int row, boolean subtractFromSelection) {
		if (data == null || table == null || table.rowNamesColorer == null || ((MesquiteModule) table.rowNamesColorer).nameMatches("#NoColor"))
			return;
		Color c = table.rowNamesColorer.getCellColor(column, row);
		int rowStart = 0;
		int rowEnd = data.getNumTaxa();
		for (int it = rowStart; it < rowEnd; it++) {
			Color c2 = table.rowNamesColorer.getCellColor(column, it);
			if (satisfiesCriteria(c, c2)) {
				if (subtractFromSelection)
					table.deselectRow(it);
				else
					table.selectRow(it);
			}

			table.repaintAll();
		}

	}

	/* ................................................................................................................. */
	MesquiteModule findCellColorerMatrix(String arguments) {
		Parser parser = new Parser();
		String s1 = parser.getFirstToken(arguments);
		String s2 = parser.getNextToken();
		int index = MesquiteInteger.fromFirstToken(arguments, pos);
		if (MesquiteInteger.isCombinable(index)) {
			// has number, but if simplification is on then it may not be good guide.
			ListableVector emp = ownerModule.getEmployeeVector();
			Enumeration enumeration = emp.elements();
			int count = 0;
			while (enumeration.hasMoreElements()) {
				MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
				if (mb instanceof CellColorerMatrix) {
					if (count == index) {
						if (!StringUtil.blank(s2)) { // name is included among arguments
							if (s2.equals(mb.getName()) || s2.equals(mb.getNameForMenuItem()))
								return mb;
							else { // name doesn't match; see if there is a better match
								MesquiteModule mb2 = (MesquiteModule) ownerModule.findEmployeeWithName(s2, true);
								if (mb2 == null)
									return mb;
								else
									return mb2;
							}
						}
						else
							return mb;
					}
					count++;
				}
			}
		}
		else {
			MesquiteModule mb = (MesquiteModule) ownerModule.findEmployeeWithName(s1, true);
			if (mb instanceof CellColorerMatrix) { // note: if by name doesn't turn off!
				return mb;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	MesquiteModule findCellColorer(String arguments) {
		Parser parser = new Parser();
		String s1 = parser.getFirstToken(arguments);
		String s2 = parser.getNextToken();
		int index = MesquiteInteger.fromFirstToken(arguments, pos);
		if (MesquiteInteger.isCombinable(index)) {
			// has number, but if simplification is on then it may not be good guide.
			ListableVector emp = ownerModule.getEmployeeVector();
			Enumeration enumeration = emp.elements();
			int count = 0;
			while (enumeration.hasMoreElements()) {
				MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
				if (mb instanceof CellColorer) {
					if (count == index) {
						if (!StringUtil.blank(s2)) { // name is included among arguments
							if (s2.equals(mb.getName()) || s2.equals(mb.getNameForMenuItem()))
								return mb;
							else { // name doesn't match; see if there is a better match
								MesquiteModule mb2 = (MesquiteModule) ownerModule.findEmployeeWithName(s2, true);
								if (mb2 == null)
									return mb;
								else
									return mb2;
							}
						}
						else
							return mb;
					}
					count++;
				}
			}
		}
		else {
			MesquiteModule mb = (MesquiteModule) ownerModule.findEmployeeWithName(s1, true);
			if (mb instanceof CellColorer) { // note: if by name doesn't turn off!
				return mb;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	MesquiteModule findCellColorerTaxa(String arguments) {
		Parser parser = new Parser();
		String s1 = parser.getFirstToken(arguments);
		String s2 = parser.getNextToken();
		int index = MesquiteInteger.fromFirstToken(arguments, pos);
		if (MesquiteInteger.isCombinable(index)) {
			// has number, but if simplification is on then it may not be good guide.
			ListableVector emp = ownerModule.getEmployeeVector();
			Enumeration enumeration = emp.elements();
			int count = 0;
			while (enumeration.hasMoreElements()) {
				MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
				if (mb instanceof CellColorerTaxa) {
					if (count == index) {
						if (!StringUtil.blank(s2)) { // name is included among arguments
							if (s2.equals(mb.getName()) || s2.equals(mb.getNameForMenuItem()))
								return mb;
							else { // name doesn't match; see if there is a better match
								MesquiteModule mb2 = (MesquiteModule) ownerModule.findEmployeeWithName(s2, true);
								if (mb2 == null)
									return mb;
								else
									return mb2;
							}
						}
						else
							return mb;
					}
					count++;
				}
			}
		}
		else {
			MesquiteModule mb = (MesquiteModule) ownerModule.findEmployeeWithName(s1, true);
			if (mb instanceof CellColorerTaxa) { // note: if by name doesn't turn off!
				return mb;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	MesquiteModule findCellColorerCharacters(String arguments) {
		Parser parser = new Parser();
		String s1 = parser.getFirstToken(arguments);
		String s2 = parser.getNextToken();
		int index = MesquiteInteger.fromFirstToken(arguments, pos);
		if (MesquiteInteger.isCombinable(index)) {
			// has number, but if simplification is on then it may not be good guide.
			ListableVector emp = ownerModule.getEmployeeVector();
			Enumeration enumeration = emp.elements();
			int count = 0;
			while (enumeration.hasMoreElements()) {
				MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
				if (mb instanceof CellColorerCharacters) {
					if (count == index) {
						if (!StringUtil.blank(s2)) { // name is included among arguments
							if (s2.equals(mb.getName()) || s2.equals(mb.getNameForMenuItem()))
								return mb;
							else { // name doesn't match; see if there is a better match
								MesquiteModule mb2 = (MesquiteModule) ownerModule.findEmployeeWithName(s2, true);
								if (mb2 == null)
									return mb;
								else
									return mb2;
							}
						}
						else
							return mb;
					}
					count++;
				}
			}
		}
		else {
			MesquiteModule mb = (MesquiteModule) ownerModule.findEmployeeWithName(s1, true);
			if (mb instanceof CellColorerCharacters) { // note: if by name doesn't turn off!
				return mb;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	MesquiteInteger pos = new MesquiteInteger(0);

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Renames the matrix", "[name of matrix]", commandName, "renameMatrix")) {
			String token = ParseUtil.getFirstToken(arguments, new MesquiteInteger(0));
			if (StringUtil.blank(token) && !MesquiteThread.isScripting()) {
				token = MesquiteString.queryString(this, "Rename matrix", "New Name of Matrix:", data.getName(), 2);
				if (StringUtil.blank(token))
					return null;
			}
			data.setName(token);
			MesquiteWindow.resetAllTitles();
			MesquiteTrunk.mesquiteTrunk.resetAllMenuBars();
		}
		/*
		 * else if (checker.compare(this.getClass(), "Selects cells of same color", "[column touched][row touched]", commandName, "selectSameColor")) { if (data == null || table == null || table.cellColorer == null) return null; MesquiteInteger io = new MesquiteInteger(0); int column= MesquiteInteger.fromString(arguments, io); int row= MesquiteInteger.fromString(arguments, io); boolean shiftDown = arguments.indexOf("shift")>=0; if (MesquiteInteger.isCombinable(column)&& (MesquiteInteger.isCombinable(row))) { if (!shiftDown) table.deselectAll(); //selectSameColor(column,
		 * row); } }
		 */
		else if (checker.compare(this.getClass(), "Returns the matrix info panel", null, commandName, "getInfoPanel")) {
			return matrixInfoPanel;
		}
		else if (checker.compare(this.getClass(), "Toggles whether the info panel is on", null, commandName, "toggleInfoPanel")) {
			infoPanelOn.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			setMatrixInfoPanel(infoPanelOn.getValue());
		}
		else if (checker.compare(this.getClass(), "Toggles whether editing is permitted or not", null, commandName, "toggleEditingNotPermitted")) {
			editingNotPermitted.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (editingNotPermitted.getValue())
				data.incrementEditInhibition();
			else
				data.decrementEditInhibition();
			inhibitionChanged();
			//setMatrixInfoPanel(infoPanelOn.getValue());
		}
		else if (checker.compare(this.getClass(), "Selects sequence", "[number of taxon][number of starting site][number of ending site]", commandName, "selectSequence")) {
			int it = MesquiteInteger.fromFirstToken(arguments, pos);
			int ic1 = MesquiteInteger.fromString(arguments, pos);
			int num = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(it) && MesquiteInteger.isCombinable(ic1) && MesquiteInteger.isCombinable(num)) {
				table.selectBlock(ic1, it, ic1 + num - 1, it);
				if (!table.isCellVisible(ic1, it)) {
					table.setFocusedCell(ic1, it, true);
				}
				table.repaintAll();
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Shows taxon", "[id of taxa block][number of taxon]", commandName, "showTaxon")) {
			pos.setValue(0);
			long whichTaxaBlock = MesquiteInteger.fromString(arguments, pos);
			Taxa taxa = data.getTaxa();
			if (whichTaxaBlock != taxa.getID())
				return null;
			int which = MesquiteInteger.fromString(arguments, pos);
			if (which >= 0 && which < taxa.getNumTaxa()) {
				if (!table.isCellVisible(-1, which)) {
					table.setFocusedCell(-1, which, true);
					table.repaintAll();
				}
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Selects taxon", "[number of taxon]", commandName, "selectTaxon")) {
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			Taxa taxa = data.getTaxa();
			if (which >= 0 && which < taxa.getNumTaxa()) {
				if (!table.isCellVisible(-1, which)) {
					table.setFocusedCell(-1, which, true);
					table.repaintAll();
				}
				taxa.setSelected(which, !taxa.getSelected(which));
				taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Selects character", "[number of character]", commandName, "selectCharacter")) {
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			if (which >= 0 && which < data.getNumChars()) {
				if (!table.isCellVisible(which, -1)) {
					table.setFocusedCell(which, -1, true);
					table.repaintAll();
				}
				data.setSelected(which, !data.getSelected(which));
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Deletes the matrix", null, commandName, "deleteMatrix")) {
			if (!MesquiteThread.isScripting())
				if (!AlertDialog.query(this, "Delete matrix?", "Are you sure you want to delete the matrix?  You cannot undo this."))
					return null;
			data.deleteMe(false);

		}
		else if (checker.compare(this.getClass(), "Allows one to set the missing data symbol", null, commandName, "setUnassignedSymbol")) {
			if (!MesquiteThread.isScripting()) {
				MesquiteString ms = new MesquiteString("" + data.getUnassignedSymbol());
				if (QueryDialogs.queryChar(this, "Missing Data Symbol", "Missing Data Symbol", ms)) {
					String s = ms.getValue();
					if (StringUtil.blank(s))
						return null;
					data.setUnassignedSymbol(s.charAt(0)); // check if editing missing data
					table.repaintAll();
				}
				else
					return null;
			}
		}
		else if (checker.compare(this.getClass(), "Allows one to set the inapplicable symbol", null, commandName, "setInapplicableSymbol")) {
			if (!MesquiteThread.isScripting()) {
				MesquiteString ms = new MesquiteString("" + data.getInapplicableSymbol());
				if (QueryDialogs.queryChar(this, "Inapplicable Symbol", "Inapplicable Symbol", ms)) {
					String s = ms.getValue();
					data.setInapplicableSymbol(s.charAt(0)); // check if editing missing data
					table.repaintAll();
				}
				else
					return null;
			}
		}
		else if (checker.compare(this.getClass(), "Shows the state names editor", null, commandName, "showStateNamesEditor")) {
			MesquiteModule mb = ownerModule.findEmployeeWithDuty(mesquite.categ.StateNamesEditor.StateNamesEditor.class);
			if (mb != null) {
				mb.doCommand("makeWindow", null, checker);
			}
		}
		else if (checker.compare(this.getClass(), "Shows the list of characters", null, commandName, "showList")) {
			data.showList();
		}
		else if (checker.compare(this.getClass(), "Shows the list of taxa", null, commandName, "showTaxaList")) {
			data.getTaxa().showMe();
		}
		else if (checker.compare(this.getClass(), "Sets the current tool", "[name of tool]", commandName, "setTool")) {
			ToolPalette palette = getPalette();
			if (palette == null)
				return null;
			TableTool newTool = (TableTool) palette.getToolWithName(arguments);
			if (newTool!=null) {
				setCurrentTool(newTool);
				palette.setCurrentTool(newTool);  //need to do this as otherwise the button is not set
			}
		}
		else if (checker.compare(this.getClass(), "Toggles whether scroll is of linked tables or not.", "[on = linked; off]", commandName, "toggleLinkedScrolling")) {
			boolean current = linkedScrolling.getValue();
			linkedScrolling.toggleValue(ParseUtil.getFirstToken(arguments, new MesquiteInteger(0)));
			if (linkedScrolling.getValue()) { // synch the two tables
				table.setFirstColumnVisibleLinked(table.getFirstColumnVisible());
				table.setFirstRowVisibleLinked(table.getFirstRowVisible());
			}

		}
		else if (checker.compare(this.getClass(), "Toggles whether scroll is of linked tables or not.", "[on = linked; off]", commandName, "toggleScrollLinkedTables")) {
			boolean current = scrollLinkedTables.getValue();
			scrollLinkedTables.toggleValue(ParseUtil.getFirstToken(arguments, new MesquiteInteger(0)));
		}
		else if (checker.compare(this.getClass(), "Toggles whether character names are shown diagonally.", "[on = diagonal; off]", commandName, "toggleDiagonal")) {
			useDiagonal.toggleValue(ParseUtil.getFirstToken(arguments, new MesquiteInteger(0)));
			table.setColumnNamesDiagonal(useDiagonal.getValue());
			table.doAutosize = true;
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets height of the column names area when in diagonal mode", "[height]", commandName, "setDiagonalHeight")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int h = MesquiteInteger.fromFirstToken(arguments, io);
			if (MesquiteInteger.isCombinable(h) && h >= 30 && h <= 500)
				((ColumnNamesPanel) table.getColumnNamesPanel()).setDiagonalHeight(h);

		}
		else if (checker.compare(this.getClass(), "Present the popup menu to select options for scroll to similar tool", null, commandName, "scrollToolOptions")) {
			if (scrollTool == null)
				return null;
			MesquiteButton button = scrollTool.getButton();
			if (button != null) {
				MesquiteInteger io = new MesquiteInteger(0);
				int x = MesquiteInteger.fromString(arguments, io); // getting x and y from arguments
				int y = MesquiteInteger.fromString(arguments, io);
				MesquitePopup popup = new MesquitePopup(button);
				MesquiteCheckMenuItem scrollLinkedItems = new MesquiteCheckMenuItem("Scroll Other Matrices", ownerModule, MesquiteModule.makeCommand("toggleScrollLinkedTables", this), null, null);
				scrollLinkedItems.set(scrollLinkedTables.getValue());
				popup.add(scrollLinkedItems);
				popup.showPopup(x, y + 6);
			}

		}
		else if (checker.compare(this.getClass(), "Scrolls to next similar characters", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "simScroll")) {
			if (table != null && data != null) {
				boolean optionDown = arguments.indexOf("option") >= 0;

				MesquiteInteger io = new MesquiteInteger(0);
				int column = MesquiteInteger.fromString(arguments, io);
				int subRow = MesquiteInteger.fromString(arguments, io);
				int percentHorizontal = MesquiteInteger.fromString(arguments, io);
				if (subRow >= 0 && column >= 0 && column <= data.getNumChars()) {
					DataColumnNamesAssistant assistant = getDataColumnNamesAssistant(subRow);
					if (assistant != null) {
						MesquiteInteger startBlock = new MesquiteInteger(-1);
						MesquiteInteger endBlock = new MesquiteInteger(-1);
						boolean found = assistant.getNextBlock(column, !optionDown, startBlock, endBlock);

						if (found && (endBlock.getValue() < table.getFirstColumnVisible() || startBlock.getValue() > table.getLastColumnVisible())) { // we now that it has been found, and is definitely off screen
							int blockSize = endBlock.getValue() - startBlock.getValue() + 1;
							// if (blockSize<=table.getNumColumnsVisible()) { // can get it all in at once
							if (scrollLinkedTables.getValue()) {
								if (optionDown) // looking left
									table.setLastColumnVisibleLinked(endBlock.getValue());
								else
									table.setFirstColumnVisibleLinked(startBlock.getValue());
							}
							else if (optionDown) // looking left
								table.setLastColumnVisible(endBlock.getValue());
							else
								table.setFirstColumnVisible(startBlock.getValue());
							// }
						}
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets background color of matrix", "[name of color]", commandName, "setBackground")) {
			String token = ParseUtil.getFirstToken(arguments, new MesquiteInteger(0));
			Color bc = ColorDistribution.getStandardColor(token);
			if (bc == null)
				return null;
			table.bgColor = bc;
			bgColorName.setValue(token);
			if (!MesquiteThread.isScripting())
				table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Deletes the selected rows", null, commandName, "deleteSelectedDataInfoStrips")) {
			// deleteSelectedRows(true);
		}
		else if (checker.compare(this.getClass(), "Hires a DataColumnNamesAssistant module for displaying info strips", "[name of module]", commandName, "hireDataInfoStrip")) {
			String name = ParseUtil.getToken(arguments, new MesquiteInteger(0));
			DataColumnNamesAssistant assistant = (DataColumnNamesAssistant) ownerModule.hireNamedEmployee(DataColumnNamesAssistant.class, arguments);
			if (assistant != null) {
				assistant.setUseMenubar(false);
				// addListAssistant(assistant);
				ownerModule.resetContainingMenuBar();
				((ColumnNamesPanel) table.getColumnNamesPanel()).appendInfoStrip();
				assistant.setTableAndData(table, data);
				assistant.setElement(0);
				table.resetTableSize(true);
				table.repaintAll();
			}
			else {
				for (int i = 0; i < ownerModule.getNumberOfEmployees(); i++) { // if already hired then pass along commands
					MesquiteModule e = (MesquiteModule) ownerModule.getEmployeeVector().elementAt(i);
					if (e instanceof DataColumnNamesAssistant) {
						if (e.nameMatches(arguments) && !e.canHireMoreThanOnce())
							return assistant;
					}
				}
			}

			return assistant;
		}
		else if (checker.compare(this.getClass(), "Assigns color assigned to selected cells", "[name of color]", commandName, "assignColor")) {
			if (table.anythingSelected()) {

				int bc = ColorDistribution.standardColorNames.indexOf(ParseUtil.getFirstToken(arguments, new MesquiteInteger(0)));
				if (bc >= 0 && MesquiteInteger.isCombinable(bc)) {
					for (int ic = -1; ic < data.getNumChars(); ic++)
						for (int it = -1; it < data.getNumTaxa(); it++) {
							if (table.isCellSelectedAnyWay(ic, it))
								setColor(ic, it, bc);
						}
				}
				if (!MesquiteThread.isScripting())
					table.repaintAll();
			}
			else if (!MesquiteThread.isScripting())
				ownerModule.alert("Cells must be selected in order to assign colors");
		}
		else if (checker.compare(this.getClass(), "Removes color assigned from selected cells", null, commandName, "removeColor")) {
			if (table.anythingSelected()) {

				for (int ic = -1; ic < data.getNumChars(); ic++)
					for (int it = -1; it < data.getNumTaxa(); it++) {
						if (table.isCellSelectedAnyWay(ic, it))
							removeColor(ic, it);
					}

				if (!MesquiteThread.isScripting())
					table.repaintAll();
			}
			else if (!MesquiteThread.isScripting())
				ownerModule.alert("Cells must be selected in order to assign colors");
		}
		else if (checker.compare(this.getClass(), "Finds footnote containing string, selects cell and ensures it is shown", null, commandName, "findFootnoteString")) {
			if (MesquiteThread.isScripting()) // todo: should support argument passed
				return null;
			findNumber = 0;
			String temp = MesquiteString.queryString(this, "Find cell", "Find first footnote containing the following string:", findFootnoteString, 2);
			if (StringUtil.blank(temp)) {
				return null;
			}
			findString = "";
			findFootnoteString = temp;
			if (!highlightCellWithFootnote(findFootnoteString, findNumber))
				findNumber = 0;
			else
				findNumber = 1;
		}
		else if (checker.compare(this.getClass(), "Finds footnote containing string, selects cell and ensures it is shown", null, commandName, "findAgain")) {
			if (MesquiteThread.isScripting()) // todo: should support argument passed
				return null;
			if (findString != null && !findString.equals("")) {
				return super.doCommand(commandName, arguments, checker);
			}
			if (StringUtil.blank(findFootnoteString))
				return null;
			if (!highlightCellWithFootnote(findFootnoteString, findNumber))
				findNumber = 0;
			else
				findNumber++;
		}
		else if (checker.compare(this.getClass(), "Not functioning", null, commandName, "moveSelection")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column = MesquiteInteger.fromString(arguments, io);
			int row = MesquiteInteger.fromString(arguments, io);
		}
		else if (checker.compare(this.getClass(), "Adds or inserts characters into matrix", "[character number after which new characters to be inserted] [number of new characters]", commandName, "addCharacters")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int starting = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, io));
			int number = MesquiteInteger.fromString(arguments, io);
			UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_ADDED, data);
			data.resetJustAdded();
			UndoReference undoReference = new UndoReference(undoInstructions, ownerModule);
			if (data.addParts(starting, number)) {
				table.setNumColumns(data.getNumChars());
				data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] { starting, number }, undoReference));
				data.addInLinked(starting, number, true);
			}
		}
		else if (checker.compare(this.getClass(), "Deletes characters", "[first character to be deleted] [number of characters]", commandName, "deleteCharacters")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int starting = CharacterStates.toInternal(MesquiteInteger.fromString(arguments, io));
			int number = MesquiteInteger.fromString(arguments, io);
			if (data.deleteCharacters(starting, number, false)) {
				data.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED, new int[] { starting, number }));
				data.deleteInLinked(starting, number, true);
				table.setNumColumns(data.getNumChars());
			}
		}
		else if (checker.compare(this.getClass(), "Excludes selected characters", "[] []", commandName, "excludeSelectedCharacters")) {
			if (data.setInclusionExclusion(ownerModule, table, false)) {
			}
		}
		else if (checker.compare(this.getClass(), "Includes selected characters", "[] []", commandName, "includeSelectedCharacters")) {
			if (data.setInclusionExclusion(ownerModule, table, true)) {
			}
		}
		else if (checker.compare(this.getClass(), "Adds taxa", "[taxon number after which new taxa to be inserted] [number of new taxa]", commandName, "addTaxa")) {
			if (data.getTaxa().isEditInhibited())
				ownerModule.discreetAlert("You cannot add taxa; the taxa block is locked.");
		MesquiteInteger io = new MesquiteInteger(0);
			int starting = Taxon.toInternal(MesquiteInteger.fromString(arguments, io));
			int number = MesquiteInteger.fromString(arguments, io);
			if (data.getTaxa().addTaxa(starting, number, true))
				table.setNumRows(data.getNumTaxa());
		}
		else if (checker.compare(this.getClass(), "Deletes taxa", "[first taxon to be deleted] [number of taxa]", commandName, "deleteTaxa")) {
			if (data.getTaxa().isEditInhibited())
				ownerModule.discreetAlert("You cannot delete taxa; the taxa block is locked.");

			MesquiteInteger io = new MesquiteInteger(0);
			int starting = Taxon.toInternal(MesquiteInteger.fromString(arguments, io));
			int number = MesquiteInteger.fromString(arguments, io);
			if (starting == 0 && number == data.getTaxa().getNumTaxa()) {
				ownerModule.discreetAlert("You cannot delete all taxa; the command will be ignored");
				return null;
			}
			if (data.getTaxa().deleteTaxa(starting, number, true))
				table.setNumRows(data.getNumTaxa());
		}
		else if (checker.compare(this.getClass(), "Moves the selected characters ", "[column to move after; -1 if at start]", commandName, "moveCharsTo")) {
			if (data.isEditInhibited()) {
				ownerModule.discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
				return null;
			}
			if (!table.anyColumnSelected()) {
				ownerModule.discreetAlert("Sorry, to move characters they must be selected first.");
				return null;
			}
			MesquiteInteger io = new MesquiteInteger(0);
			int justAfter = MesquiteInteger.fromString(arguments, io);
			if (!MesquiteInteger.isCombinable(justAfter))
				justAfter = MesquiteInteger.queryInteger(this, "Move characters", "After which column should the selected characters be moved (enter 0 to move to first place)?", 0, 0, table.getNumColumns() * 10);
			if (MesquiteInteger.isCombinable(justAfter))
				table.selectedColumnsDropped(justAfter - 1); // -1 to convert to internal representation
		}
		else if (checker.compare(this.getClass(), "Moves the selected taxa ", "[row to move after; -1 if at start]", commandName, "moveTaxaTo")) {
			if (!table.anyRowSelected()) {
				ownerModule.discreetAlert("Sorry, to move taxa they must be selected first");
				return null;
			}
			MesquiteInteger io = new MesquiteInteger(0);
			int justAfter = MesquiteInteger.fromString(arguments, io);
			if (!MesquiteInteger.isCombinable(justAfter))
				justAfter = MesquiteInteger.queryInteger(this, "Move taxa", "After which row should the selected taxa be moved (enter 0 to move to first place)?", 0, 0, table.getNumRows() * 10);
			if (MesquiteInteger.isCombinable(justAfter))
				table.selectedRowsDropped(justAfter - 1); // -1 to convert to internal representation
		}
		else if (checker.compare(this.getClass(), "Moves the selected block ", "[number of characters to move]", commandName, "moveSelectedBlock")) {
			MesquiteInteger firstRow = new MesquiteInteger();
			MesquiteInteger lastRow = new MesquiteInteger();
			MesquiteInteger firstColumn = new MesquiteInteger();
			MesquiteInteger lastColumn = new MesquiteInteger();
			if (!table.singleCellBlockSelected(firstRow, lastRow, firstColumn, lastColumn)) {
				ownerModule.discreetAlert("Sorry, a single block of cells must be selected before it can be moved.");
				return null;
			}
			
			MesquiteInteger io = new MesquiteInteger(0);
			int shiftAmount = MesquiteInteger.fromString(arguments, io);

			if (!(MesquiteInteger.isCombinable(shiftAmount) || shiftAmount==0) && !MesquiteThread.isScripting()) {
				String helpString ="Enter the amount to shift the block.  If you enter a positive number, the block will be shifted through that many characters to the right; a negative number, to the left. ";
				helpString+="The block will not be shifted over top of existing data; it will only be moved through gaps.  If you request a shift larger than can be accommodated, then ";
				helpString += "the block will be shifted as far as possible without overwriting data.";
			
				shiftAmount = MesquiteInteger.queryInteger(ownerModule.containerOfModule(), "Move Selected Block", "Number of characters to shift selected block", helpString, 1, MesquiteInteger.unassigned, MesquiteInteger.unassigned);
			}
			
			if (MesquiteInteger.isCombinable(shiftAmount) && shiftAmount!=0) {
				MesquiteBoolean dataChanged = new MesquiteBoolean();
				MesquiteInteger charAdded = new MesquiteInteger();
				MesquiteInteger distanceMoved = new MesquiteInteger();
				data.moveCells(firstColumn.getValue(), lastColumn.getValue(), shiftAmount, firstRow.getValue(), lastRow.getValue(),  false, false, true, true,  dataChanged,  charAdded, distanceMoved);
				if (distanceMoved.getValue()!=shiftAmount)
					MesquiteMessage.println("Block could not be moved as far as request.  Request: : " + shiftAmount + ". Amount moved: " + distanceMoved.getValue());
				table.deSelectBlock(firstColumn.getValue(), firstRow.getValue(), lastColumn.getValue(), lastRow.getValue());
				table.selectBlock(firstColumn.getValue()+distanceMoved.getValue(), firstRow.getValue(), lastColumn.getValue()+distanceMoved.getValue(), lastRow.getValue());
				if (dataChanged.getValue())
					contentsChanged();
			}
		}
		/* This is a hidden feature to help recover from consequences of bug of duplicate NOTES blocks in linked files in 1.0 to 1.02 */
		else if (checker.compare(this.getClass(), "Moves the footnotes of the selected characters ", "[column to move after; -1 if at start]", commandName, "moveFootnotes")) {
			if (!table.anyColumnSelected()) {
				ownerModule.discreetAlert("Sorry, to move footnotes, characters must be selected first.");
				return null;
			}
			MesquiteInteger io = new MesquiteInteger(0);
			int justAfter = MesquiteInteger.fromString(arguments, io);
			if (!MesquiteInteger.isCombinable(justAfter))
				justAfter = MesquiteInteger.queryInteger(this, "Move footnotes", "After which column should the footnotes of the selected characters be moved (enter 0 to move to first place)?", 0, 0, table.getNumColumns() * 10);
			if (MesquiteInteger.isCombinable(justAfter)) {
				int after = (justAfter - 1); // -1 to convert to internal representation
				if (after < -1)
					return null;
				if (after > table.getNumColumns())
					after = table.getNumColumns();
				int i = 0;
				Bits sel = table.getColumnsSelected();
				boolean asked = false;
				ObjectArray charNotes = data.getWhichAssociatedObject(NameReference.getNameReference("comments"));
				while (i < table.getNumColumns()) {
					if (sel.isBitOn(i)) {
						table.deselectColumn(i);
						sel.clearBit(i);
						if (i < after) {
							for (int ic = i; ic < after; ic++) {
								if (table.isColumnSelected(ic + 1))
									table.selectColumn(ic);
								else
									table.deselectColumn(ic);
							}
						}

						StringArray.moveColumns(data.getFootnotes(), i, 1, after);
						if (charNotes != null)
							charNotes.moveParts(i, 1, after);
						sel.moveParts(i, 1, after);
						if (i > after)
							after++;
						i = 0;
					}
					else
						i++;
				}
				table.synchronizeColumnSelection(data);
				data.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_ADDED));
				contentsChanged();
			}
		}
		/**/
		else if (checker.compare(this.getClass(), "Hires utility module to operate on the data", "[name of module]", commandName, "doUtility")) {
			if (table != null && data != null) {
				if (data.isEditInhibited()) {
					ownerModule.discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				DataUtility tda = (DataUtility) ownerModule.hireNamedEmployee(DataUtility.class, arguments);
				if (tda != null) {
					boolean a = tda.operateOnData(data);
					if (a) {
						table.repaintAll();
						data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
					}
					if (!tda.pleaseLeaveMeOn())
						ownerModule.fireEmployee(tda);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires utility module to alter names of the taxa", "[name of module]", commandName, "doNames")) {
			if (table != null && data != null) {
				Taxa taxa = data.getTaxa();
				TaxonNameAlterer tda = (TaxonNameAlterer) ownerModule.hireNamedEmployee(TaxonNameAlterer.class, arguments);
				if (tda != null) {
					UndoReference undoReference = new UndoReference(new UndoInstructions(UndoInstructions.ALLTAXONNAMES, taxa, taxa), ownerModule);
					boolean a = tda.alterTaxonNames(taxa, getTable());
					ownerModule.fireEmployee(tda);
					if (a) {
						table.redrawRowNames();
						taxa.notifyListeners(this, new Notification(NAMES_CHANGED, undoReference));

					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires utility module to operate on the taxa", "[name of module]", commandName, "doTaxonUtility")) {
			if (table != null && data != null) {
				Taxa taxa = data.getTaxa();
				if (taxa != null) {
					TaxonUtility tda = (TaxonUtility) ownerModule.hireNamedEmployee(TaxonUtility.class, arguments);
					if (tda != null) {
						boolean a = tda.operateOnTaxa(taxa);
						if (!tda.pleaseLeaveMeOn())
							ownerModule.fireEmployee(tda);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets width of columns of matrix", "[width]", commandName, "setColumnWidth")) {
			MesquiteInteger io = new MesquiteInteger(0);
			widthsSet = true;
			int newWidth = MesquiteInteger.fromFirstToken(arguments, io);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(this, "Set column width", "Column Width:", columnWidth, table.getMinColumnWidth(), table.getMaxColumnWidth());
			if (newWidth >= table.getMinColumnWidth() && newWidth <= table.getMaxColumnWidth() && newWidth != columnWidth) {
				columnWidth = newWidth;
				table.doAutosize = false;
				table.suppressAutosize = true;
				table.setColumnWidthsUniform(columnWidth);
				table.repaintAll();
			}

		}
		else if (checker.compare(this.getClass(), "Sets whether or not cells of matrix are colored (no longer used)", "[on]", commandName, "toggleColorCell")) {
			if ("on".equalsIgnoreCase(ParseUtil.getFirstToken(arguments, pos))) {
				MesquiteModule mb = ownerModule.findEmployeeWithName("#ColorByState", true);
				setCellColorer(mb);
			}
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets by what the cells in the matrix are colored", "[name of module]", commandName, "colorCells")) {
			MesquiteModule mb = findCellColorerMatrix(arguments);
			if (mb == null)
				return null;
			if (mb == table.cellColorer) {
				// setCellColorer(null);
			}
			else {
				setCellColorer(mb);
			}
			table.repaintAll();
			return mb;

		}
		else if (checker.compare(this.getClass(), "Sets by what the text in the matrix are colored", "[name of module]", commandName, "colorText")) {
			MesquiteModule mb = findCellColorer(arguments);
			if (mb == null)
				return null;
			if (mb == table.textColorer) {
				// setTextColorer(null);
			}
			else {
				setTextColorer(mb);
			}
			table.repaintAll();
			return mb;

		}
		else if (checker.compare(this.getClass(), "Sets by what the row name cells in the matrix are colored", "[name of module]", commandName, "colorRowNames")) {
			MesquiteModule mb = findCellColorerTaxa(arguments);
			if (mb == null)
				return null;
			if (mb == table.rowNamesColorer && !MesquiteThread.isScripting()) {
				// setRowNamesColorer(null);
			}
			else {
				setRowNamesColorer(mb);
			}
			table.repaintAll();
			return mb;

		}
		else if (checker.compare(this.getClass(), "Sets by what the column name cells in the matrix are colored", "[name of module]", commandName, "colorColumnNames")) {
			MesquiteModule mb = findCellColorerCharacters(arguments);
			if (mb == null)
				return null;
			if (mb == table.columnNamesColorer && !MesquiteThread.isScripting()) {
				// setColumnNamesColorer(null);
			}
			else {
				setColumnNamesColorer(mb);
			}
			table.repaintAll();
			return mb;

		}
		else if (checker.compare(this.getClass(), "Forces the columns to auto-size", null, commandName, "forceAutosize")) {
			offBirdsEye(false);
			table.doAutosize = true;
			table.suppressAutosize = false;
			// table.setColumnWidthsUniform(columnWidth);
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not states are to be written into cells", "[on or off]", commandName, "toggleShowStates")) {
			offBirdsEye(false);
			table.showStates.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.doAutosize = true;
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not default character names are shown", "[on or off]", commandName, "toggleShowDefaultCharNames")) {
			table.showDefaultCharNames.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.doAutosize = true;
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not autosize is to use character names also", "[on or off]", commandName, "toggleAutoWCharNames")) {
			offBirdsEye(false);
			table.autoWithCharNames.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.doAutosize = true;
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not taxon name column is to be autosized", "[on or off]", commandName, "toggleAutoTaxonNames")) {
			table.autoRowNameWidth.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.doAutosize = true;
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not column widths are constrained", "[on or off]", commandName, "toggleConstrainCW")) {
			offBirdsEye(false);
			constrainedCW.toggleValue(arguments);
			table.setConstrainMaxAutoColumn(constrainedCW.getValue());
			table.doAutosize = true;
			if (!MesquiteThread.isScripting())
				table.repaintAll();

		}
		else if (checker.compare(this.getClass(), "Sets whether or not columns are drawn as narrowly as possible", "[on or off]", commandName, "toggleTight")) {
			table.tight.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.suppressAutosize = false;
			table.doAutosize = true;
			if (table.tight.getValue()) {
				columnWidth = table.getNarrowDefaultColumnWidth();
				table.setColumnWidthsUniform(columnWidth);
			}
			else {
				// columnWidth = data.getDefaultColumnWidth();
			}
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not rows are drawn as narrowly as possible", "[on or off]", commandName, "toggleThinRows")) {
			table.thinRows.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to interleave the matrix when writing", "[on or off]", commandName, "toggleInterleaved")) {
			interleaved.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			data.interleaved = interleaved.getValue();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not changes since last save are to be highlighted", "[on or off]", commandName, "toggleShowChanges")) {
			table.showChanges.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not states are listed on separate lines in explanation", "[on or off]", commandName, "toggleSeparateLines")) {
			table.statesSeparateLines.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (table.cellAnnotated != null && table.cellAnnotated.getRow() > -1 && table.cellAnnotated.getColumn() > -1)
				setExplanation(table.getCellExplanation(table.cellAnnotated.getColumn(), table.cellAnnotated.getRow()));
		}
		else if (checker.compare(this.getClass(), "Sets whether or not full names are shown in the cells of matrix", "[on or off]", commandName, "toggleShowNames")) {
			offBirdsEye(false);
			table.showNames.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.doAutosize = true;
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not taxon names are shown", "[on or off]", commandName, "toggleShowTaxonNames")) {
			showTaxonNames.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.setShowRowNames(showTaxonNames.getValue());
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not auto-sizing of columns occurs", "[on or off]", commandName, "toggleAllowAutosize")) {
			allowAutosize.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.allowAutosize.setValue(allowAutosize.getValue());
		}
		else if (checker.compare(this.getClass(), "Sets whether or not birds eye view is shown", "[on or off]", commandName, "toggleBirdsEye")) {
			table.showBirdsEyeView.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (table.showBirdsEyeView.getValue()) {
				oldShowStates = table.showStates.getValue();
				oldColumnsWidths = table.getColumnWidthsUniform();
				oldSuppress = table.suppressAutosize;
				table.suppressAutosize = true;
				table.setColumnWidthsUniform(table.birdsEyeWidth);
				table.doAutosize = false;
			}
			else {
				offBirdsEye(true);
			}
			resetSequenceLedge();
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the grid is drawn in pale gray", "[on or off]", commandName, "toggleShowPaleGrid")) {
			table.showPaleGrid.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.paleGrid = table.showPaleGrid.getValue();
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the cell colors are pale.", "[on or off]", commandName, "toggleShowPaleCellColors")) {
			table.showPaleCellColors.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.paleCellColors = table.showPaleCellColors.getValue();
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not excluded characters are pale.", "[on or off]", commandName, "toggleShowPaleExcluded")) {
			showPaleExcluded.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.setShowPaleExcluded(showPaleExcluded.getValue());
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the gaps are pale.", "[on or off]", commandName, "togglePaleInapplicable")) {
			table.paleInapplicable.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the text of cells is shown in bold face.", "[on or off]", commandName, "toggleShowBoldCellText")) {
			table.showBoldCellText.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			table.boldCellText = table.showBoldCellText.getValue();
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets width of columns of matrix for bird's eye view", "[width]", commandName, "birdsEyeWidth")) {
			MesquiteInteger io = new MesquiteInteger(0);
			widthsSet = true;
			int newWidth = MesquiteInteger.fromFirstToken(arguments, io);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(this, "Set Bird's Eye Width", "Width of columns in bird's eye view:", table.birdsEyeWidth, 1, 6);
			if (newWidth >= 1 && newWidth <= 6 && newWidth != table.birdsEyeWidth) {
				table.birdsEyeWidth = newWidth;
				if (table.showBirdsEyeView.getValue()) {
					table.setColumnWidthsUniform(table.birdsEyeWidth);
					table.repaintAll();
				}
			}

		}
		else if (checker.compare(this.getClass(), "Sets whether or not the color legend panel is shown", "[on = shown; off]", commandName, "toggleColorsPanel")) {
			Parser parser = new Parser();
			showColorLegend.toggleValue(parser.getFirstToken(arguments));
			if (table.cellColorer != null && showColorLegend.getValue()) {
				ColorRecord[] colors = ((CellColorer) table.cellColorer).getLegendColors();
				setColorLegend(colors, ((CellColorer) table.cellColorer).getColorsExplanation(), true);
			}
			else
				setColorLegend(null, null, false);
		}
		else if (checker.compare(this.getClass(), "Hires new data editor assistant module", "[name of module]", commandName, "newAssistant")) {
			DataWindowAssistant dwa = (DataWindowAssistant) ownerModule.hireNamedEmployee(DataWindowAssistant.class, arguments);
			if (dwa != null)
				dwa.setTableAndData(table, data);
			return dwa;
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	SequenceLedge sequenceLedge;

	public void resetSequenceLedge() {
		if (table.showBirdsEyeView.getValue()) { // need to show
			if (sequenceLedge == null)
				sequenceLedge = new SequenceLedge();
			addLedgePanel(sequenceLedge, 22);
			sequenceLedge.setVisible(true);

		}
		else if (!table.showBirdsEyeView.getValue() && sequenceLedge != null) { // need to hide
			sequenceLedge.setVisible(false);
			removeLedgePanel(sequenceLedge);

		}
	}

	private void offBirdsEye(boolean doRegardless) {
		if (doRegardless || table.showBirdsEyeView.getValue()) {
			table.setColumnWidthsUniform(oldColumnsWidths);
			table.doAutosize = true;
			table.showStates.setValue(oldShowStates);
			table.suppressAutosize = oldSuppress;
			table.showBirdsEyeView.setValue(false);
		}
	}

	private void setTextColorer(MesquiteModule mb) {
		if (table.textColorer != null) {
			table.textColorer.setActiveColors(false);
			if (table.textColorer instanceof DataWindowAssistant)
				((DataWindowAssistant) table.textColorer).setActive(false);
		}
		table.textColorer = (CellColorer) mb;
		if (mb != null) {
			textColorerName.setValue(mb.getNameForMenuItem());
			boolean success = ((CellColorer) mb).setActiveColors(true);
			if (success && mb instanceof DataWindowAssistant)
				((DataWindowAssistant) mb).setActive(true);
		}
		else {
			textColorerName.setValue((String) null);
		}
	}

	private void setRowNamesColorer(MesquiteModule mb) {
		if (table.rowNamesColorer != null) {
			table.rowNamesColorer.setActiveColors(false);
			if (table.rowNamesColorer instanceof DataWindowAssistant)
				((DataWindowAssistant) table.rowNamesColorer).setActive(false);
		}
		table.rowNamesColorer = (CellColorer) mb;
		if (mb != null) {
			rowNamesColorerName.setValue(mb.getNameForMenuItem());
			boolean success = ((CellColorer) mb).setActiveColors(true);
			if (success && mb instanceof DataWindowAssistant)
				((DataWindowAssistant) mb).setActive(true);
		}
		else {
			rowNamesColorerName.setValue((String) null);
		}
	}

	private void setColumnNamesColorer(MesquiteModule mb) {
		if (table.columnNamesColorer != null) {
			table.columnNamesColorer.setActiveColors(false);
			if (table.columnNamesColorer instanceof DataWindowAssistant)
				((DataWindowAssistant) table.columnNamesColorer).setActive(false);
		}
		table.columnNamesColorer = (CellColorer) mb;
		if (mb != null) {
			columnNamesColorerName.setValue(mb.getNameForMenuItem());
			boolean success = ((CellColorer) mb).setActiveColors(true);
			if (success && mb instanceof DataWindowAssistant)
				((DataWindowAssistant) mb).setActive(true);
		}
		else {
			columnNamesColorerName.setValue((String) null);
		}
	}

	void resetColorerIfNeeded(MesquiteModule employee) {
		if (table.cellColorer != null && table.cellColorer == employee) {
			setColorLegend(table.cellColorer.getLegendColors(), table.cellColorer.getColorsExplanation(), showColorLegend.getValue());
		}
	}

	private void setCellColorer(MesquiteModule mb) {
		if (table.cellColorer != null) {
			table.cellColorer.setActiveColors(false);
			if (table.cellColorer instanceof DataWindowAssistant)
				((DataWindowAssistant) table.cellColorer).setActive(false);
		}
		table.cellColorer = (CellColorer) mb;
		if (mb != null) {
			boolean success = ((CellColorer) mb).setActiveColors(true);
			if (success) {
				cellColorerName.setValue(mb.getNameForMenuItem());
				if (mb instanceof DataWindowAssistant)
					((DataWindowAssistant) mb).setActive(true);
				table.addControlButton(colorLegendButton);
				setColorLegend(((CellColorer) mb).getLegendColors(), ((CellColorer) mb).getColorsExplanation(), showColorLegend.getValue());
			}
		}
		else {
			table.removeControlButton(colorLegendButton);
			cellColorerName.setValue((String) null);
			setColorLegend(null, null, false);
		}
	}

	ColorLegend panel;

	MesquiteBoolean showColorLegend = new MesquiteBoolean(false);

	// addCheckMenuItem(null, "Show Color Legend", makeCommand("togglePanel", this), showPanel);
	/* ................................................................................................................. */
	void setColorLegend(ColorRecord[] legendColors, String colorsExplanation, boolean show) {
		if (show) {
			if (panel == null)
				panel = new ColorLegend(this);
			addSidePanel(panel, ColorLegend.width);
			panel.setVisible(true);
			String title = "Explanation of colors";
			if (table.cellColorer instanceof MesquiteModule)
				title = ((MesquiteModule) table.cellColorer).getNameForMenuItem();
			else if (table.cellColorer instanceof Listable)
				title = ((Listable) table.cellColorer).getName();
			panel.setLegendColors(legendColors, title, colorsExplanation);
			panel.repaint();
		}
		else {
			if (panel != null)
				removeSidePanel(panel);
		}
	}

	/* ................................................................................................................. */
	public void resignCellColorer(MesquiteModule mb) {
		if (table.cellColorer == mb)
			setCellColorer(null);
		table.repaintAll();

	}

	/* ................................................................................................................. */
	public boolean requestCellColorer(MesquiteModule mb, int ic, int it, String message) {
		if (ic >= 0 && it >= 0) { // matrix cells
			if (table.cellColorer == mb)
				return true;
			if ((table.cellColorer == null || table.cellColorer instanceof mesquite.charMatrices.NoColor.NoColor) && (message != null && AlertDialog.query(this, "Cell colors", message, "Yes", "No"))) {
				setCellColorer(mb);
				table.repaintAll();
				return true;
			}
		}
		else if (ic < 0 && it >= 0) { // row names
			if (table.rowNamesColorer == mb)
				return true;
			if ((table.rowNamesColorer == null || table.rowNamesColorer instanceof mesquite.charMatrices.NoColor.NoColor) && (message != null && AlertDialog.query(this, "Row name colors", message, "Yes", "No"))) {
				setRowNamesColorer(mb);
				table.repaintAll();
				return true;
			}
		}
		else if (ic >= 0 && it < 0) { // column names
			if (table.columnNamesColorer == mb)
				return true;
			if ((table.columnNamesColorer == null || table.columnNamesColorer instanceof mesquite.charMatrices.NoColor.NoColor) && (message != null && AlertDialog.query(this, "Column name colors", message, "Yes", "No"))) {
				setColumnNamesColorer(mb);
				table.repaintAll();
				return true;
			}
		}
		return false;

	}

	/* ................................................................................................................. */
	public void demandCellColorer(MesquiteModule mb, int ic, int it, String message) {
		if (ic >= 0 && it >= 0) { // matrix cells
			if (table.cellColorer == mb)
				return;
			setCellColorer(mb);
			table.repaintAll();
		}
		else if (ic < 0 && it >= 0) { // rownames
			if (table.rowNamesColorer == mb)
				return;
			setRowNamesColorer(mb);
			table.repaintAll();
		}
		else if (ic >= 0 && it < 0) { // columnnames
			if (table.columnNamesColorer == mb)
				return;
			setColumnNamesColorer(mb);
			table.repaintAll();
		}
		if (message != null)
			ownerModule.alert(message);

	}

	/* ................................................................................................................. */
	public void pleaseUpdate() {
		table.repaintAll();
	}

	void checkSelectionMatrixInfoPanel() {
		if (table == null || matrixInfoPanel == null)
			return;
		if (table.numRowsSelected() == 1) {
			int row = table.firstRowSelected();
			matrixInfoPanel.cellTouch(row, -1);
		}
		if (table.numColumnsSelected() == 1) {
			int column = table.firstColumnSelected();
			matrixInfoPanel.cellTouch(column, -1);
		}

	}

	/* ................................................................................................................. */
	void inhibitionChanged(){
		editingNotPermitted.setValue(data.isEditInhibited());
		if (ibeamTool!=null)
			ibeamTool.setEnabled(!editingNotPermitted.getValue());
	}
	/* ................................................................................................................. */
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		if (caller == this)
			return;
		if ((caller instanceof BasicDataWindow || caller instanceof MatrixTable) && code != MesquiteListener.SELECTION_CHANGED && code != MesquiteListener.NAMES_CHANGED)
			return;
		UndoReference undoReference = Notification.getUndoReference(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof Taxa && (Taxa) obj == data.getTaxa()) {
			Taxa taxa = (Taxa) obj;
			if (code == MesquiteListener.NAMES_CHANGED) {
				table.redrawRowNames();
				setUndoer(undoReference);
			}
			else if (code == MesquiteListener.SELECTION_CHANGED) {
				table.synchronizeRowSelection(taxa);
				table.repaintAll();
				checkSelectionMatrixInfoPanel();

			}
			else if (code == MesquiteListener.PARTS_ADDED) {
				table.offAllEditsDontRecord();// 1. 12
				table.setNumRows(taxa.getNumTaxa());
				table.synchronizeRowSelection(taxa);
				table.repaintAll();
				setUndoer(undoReference);
			}
			else if (code == MesquiteListener.PARTS_DELETED) {
				table.offAllEditsDontRecord();// 1. 12
				table.setNumRows(taxa.getNumTaxa());
				table.synchronizeRowSelection(taxa);
				table.repaintAll();
				setUndoer();
			}
			else if (code == MesquiteListener.PARTS_MOVED) {
				table.offAllEditsDontRecord();// 1. 12
				table.setNumRows(taxa.getNumTaxa());
				table.synchronizeRowSelection(taxa);
				table.repaintAll();
				setUndoer(undoReference);
			}
			else if (code != MesquiteListener.ANNOTATION_CHANGED) {
				table.repaintAll();
				if (caller != table) { // if object provoking notification is me, then don't repaint
					table.refreshAnnotation();
				}
			}
			if (getMode() > 0)
				updateTextPage();
		}
		else if (obj instanceof CharacterData && (CharacterData) obj == data) {
			if (getMode() > 0)
				updateTextPage();
			else if (code == MesquiteListener.NAMES_CHANGED) {
				table.doAutosize = true;
				table.repaintAll();
				setUndoer(undoReference);
			}
			else if (code == MesquiteListener.LOCK_CHANGED) {
				inhibitionChanged();
			}
			else if (code == MesquiteListener.SELECTION_CHANGED) {
				if (caller != table) { // if object provoking notification is me, then don't repaint
					table.synchronizeColumnSelection(data);
					table.repaintAll();
					checkSelectionMatrixInfoPanel();
				}
			}
			else if (code == MesquiteListener.PARTS_DELETED) {
				table.doAutosize = true;
				table.offAllEditsDontRecord();// 1. 12
				if (parameters != null && parameters.length > 1) {
					int starting = parameters[0];
					int num = parameters[1];
					if (data.getNumChars() < table.getNumColumns()) {
						for (int i = starting + num - 1; i >= starting; i--)
							table.deleteColumn(i);
					}
				}
				else if (table.getNumColumns() != data.getNumChars())
					table.setNumColumns(data.getNumChars());
				if (table.getNumRows() != data.getTaxa().getNumTaxa())
					table.setNumRows(data.getTaxa().getNumTaxa());
				table.synchronizeColumnSelection(data);
				table.repaintAll();
				setUndoer();
			}
			else if (code == MesquiteListener.PARTS_ADDED) {
				table.doAutosize = true;
				table.offAllEditsDontRecord();// 1. 12
				if (parameters != null && parameters.length > 1) {
					int starting = parameters[0];
					int num = parameters[1];
					if (data.getNumChars() > table.getNumColumns()) {
						table.insertColumns(starting, num);
					}
				}
				else if (table.getNumColumns() != data.getNumChars())
					table.setNumColumns(data.getNumChars());
				if (table.getNumRows() != data.getTaxa().getNumTaxa())
					table.setNumRows(data.getTaxa().getNumTaxa());
				table.synchronizeColumnSelection(data);
				table.repaintAll();
				setUndoer(undoReference);
			}
			else if (code == MesquiteListener.PARTS_MOVED) {
				table.doAutosize = true;
				table.offAllEditsDontRecord();// 1. 12
				if (parameters != null && parameters.length > 1) {
					int starting = parameters[0];
					int num = parameters[1];
					int justAfter = parameters[2];
					if (data.getNumChars() == table.getNumColumns()) {
						table.moveColumns(starting, num, justAfter);
					}
				}
				if (table.getNumRows() != data.getTaxa().getNumTaxa())
					table.setNumRows(data.getTaxa().getNumTaxa());
				table.synchronizeColumnSelection(data);
				table.repaintAll();
				setUndoer(undoReference);
			}
			else if (code == MesquiteListener.DATA_CHANGED) {
				table.doAutosize = true;
				if (table.getNumRows() != data.getTaxa().getNumTaxa())
					table.setNumRows(data.getTaxa().getNumTaxa());
				if (table.getNumColumns() != data.getNumChars())
					table.setNumColumns(data.getNumChars());
				table.synchronizeColumnSelection(data);
				table.repaintAll();
				setUndoer(undoReference);
			}
			else if (code == MesquiteListener.ANNOTATION_CHANGED) {
				if (caller != table) { // if object provoking notification is me, then don't repaint
					table.refreshAnnotation();
				}
			}
			else {
				table.doAutosize = true;
				if (table.getNumRows() != data.getTaxa().getNumTaxa()) {
					table.offAllEditsDontRecord();// 1. 12
					table.setNumRows(data.getTaxa().getNumTaxa());
				}
				if (table.getNumColumns() != data.getNumChars()) {
					table.offAllEditsDontRecord();// 1. 12
					table.setNumColumns(data.getNumChars());
				}
				table.synchronizeColumnSelection(data);
				table.repaintAll();
			}
		}
		table.setMessage(data.getCellContentsDescription());
		super.changed(caller, obj, notification);
	}

	
	/* ................................................................................................................. */
	/** passes which object is being disposed (from MesquiteListener interface) */
	public void disposing(Object obj) {
		if ((obj instanceof Taxa && (Taxa) obj == data.getTaxa()) || (obj instanceof CharacterData && (CharacterData) obj == data)) {
			hide();
			MesquiteModule mb = ownerModule;
			dispose();
			mb.iQuit();

		}
	}

	/* ................................................................................................................. */
	/** passes which object is being disposed (from MesquiteListener interface) */
	public boolean okToDispose(Object obj, int queryUser) {
		return true;
	}

	/* ................................................................................................................. */
	/**
	 * Query module as to whether conditions are such that it will have to quit soon -- e.g. if its taxa block has been doomed. The tree window, data window, etc. override this to return true if their object is doomed. This is useful in case MesquiteListener disposing method is not called for an employer before one of its employees discovers that it needs to quit. If the employer is going to quit anyway,there is no use to use auto rehire for the quit employee.
	 */
	public boolean quittingConditions() {
		return (data.isDoomed() || data.getTaxa().isDoomed());
	}

	/** requests that a side panel be added to main graphics panel */
	public void addSidePanel(MousePanel sp, int width) {
		super.addSidePanel(sp, width);
		checkSizes();
	}

	/** requests that a side panel be added to main graphics panel */
	public void removeSidePanel(MousePanel sp) {
		super.removeSidePanel(sp);
		checkSizes();
	}

	/** requests that a ledge panel be added to main graphics panel */
	public void addLedgePanel(MousePanel sp, int width) {
		super.addLedgePanel(sp, width);
		checkSizes();
	}

	/** requests that a side panel be added to main graphics panel */
	public void removeLedgePanel(MousePanel sp) {
		super.removeLedgePanel(sp);
		checkSizes();
	}

	/* ................................................................................................................. */
	public void setWindowSize(int width, int height) {
		super.setWindowSize(width, height);
		checkSizes();
	}
	
	public void setVisible(boolean vis){
		super.setVisible(vis);
		if (table != null)
			table.requestFocus();
	}

	void checkSizes() {
		if (table != null) { // && ((getHeight() != windowHeight) || (getWidth() != windowWidth))) {
			windowHeight = getHeight();
			windowWidth = getWidth();
			table.setSize(windowWidth, windowHeight);
			table.doAutosize = true;
			table.repaintAll();
		}
	}

	/* ................................................................................................................. */
	public void setCurrentTool(MesquiteTool tool) {
		super.setCurrentTool(tool);
		if (tool != null && tool.getEnabled()) {
			if ((getCurrentTool() != ibeamTool) && (table != null))
				table.offAllEdits();
			boolean myTool = (getCurrentTool() == ibeamTool || getCurrentTool() == arrowTool || (getCurrentTool() != null && getCurrentTool().getAllowAnnotate()));
			setAEFocusSuppression(!myTool);
			if (!myTool)
				table.requestFocus();
		}
	}

	/* ................................................................................................................. */
	public String getPrintMenuItem() {
		return "Print Matrix...";
	}

	/* ................................................................................................................. */
	public String getPrintToFitMenuItemName() {
		return "Print Matrix To Fit Page...";
	}

	/* ................................................................................................................. */
	/**
	 * @author Peter Midford
	 */
	public String getPrintToPDFMenuItemName() {
		return "Save Matrix As PDF...";
	}

	/* ................................................................................................................. */
	/** Returns menu location for item to bring the window to the for (0 = custom or don't show; 1 = system area of Windows menu; 2 = after system area of Windows menu) */
	public int getShowMenuLocation() {
		return 0;
	}

	/* ................................................................................................................. */
	public MatrixTable getMatrixTable() {
		return table;
	}

	/* ................................................................................................................. */
	public void printWindow(MesquitePrintJob pjob) {
		table.printTable(pjob, this);
		// TODO: optionally print modelsets, etc.
	}

	/* ................................................................................................................. */
	public void paintContents(Graphics g) {
		table.repaintAll();
	}

	/* ................................................................................................................. */
	public String getTextContents() {
		String s = "Character matrix editor for matrix \"" + data.getName() + "\"\n";
		s += "Type of matrix: " + data.getDataTypeName();
		if (data instanceof CategoricalData) {
			if (((CategoricalData) data).usingShortMatrix())
				s += " (compacted)\n";
			else
				s += " (uncompacted)\n";
		}
		else
			s += "\n";
		s += "Number of characters: " + data.getNumChars() + "\n";
		s += "Number of taxa: " + data.getNumTaxa() + "\n";
		int countExcluded = 0;
		int countInapplicable = 0;
		int countUnassigned = 0;
		for (int ic = 0; ic < data.getNumChars(); ic++) {
			if (!data.isCurrentlyIncluded(ic))
				countExcluded++;
			for (int it = 0; it < data.getNumTaxa(); it++) {
				if (data.isInapplicable(ic, it))
					countInapplicable++;
				if (data.isUnassigned(ic, it))
					countUnassigned++;
			}

		}
		s += "Number of characters excluded: " + countExcluded + "\n";
		s += "Proportion of missing data: " + MesquiteDouble.toString(1.0 * countUnassigned / (data.getNumChars() * data.getNumTaxa())) + "\n";
		s += "Proportion of inapplicable codings: " + MesquiteDouble.toString(1.0 * countInapplicable / (data.getNumChars() * data.getNumTaxa())) + "\n\n";
		String an = data.getAnnotation();
		if (!StringUtil.blank(an)){
			s += "----------------\nNote about matrix:\n" + an + "\n----------------\n\n";
		}
		s += table.getTextVersion();
		return s;
	}

	String getParameters() {
		String s = "";
		if (table.cellColorer != null) {
			s += "Background of cells colored by: " + ((MesquiteModule) table.cellColorer).getName();
		}
		if (table.textColorer != null) {
			if (table.cellColorer != null)
				s += "; ";
			s += "Text of cells colored by: " + ((MesquiteModule) table.textColorer).getName();
		}
		return s;
	}

	/* ................................................................................................................. */
	public void windowResized() {
		super.windowResized();
		checkSizes();

		// data.notifyListeners(this, new Notification(MesquiteListener.VIEW_CHANGED));
	}

	public void dispose() {
		disposing = true;
		waitUntilDisposable();
		table.dispose();
		if (data.getTaxa() != null)
			data.getTaxa().removeListener(this);
		data.removeListener(this);
		super.dispose();
	}

}

/* ======================================================================== */
/* ======================================================================== */
class CellAnnotation implements Annotatable {
	String name;

	CharacterData data;

	int row = -1;

	int column = -1;

	public CellAnnotation(CharacterData data) {
		this.data = data;
	}

	int getRow() {
		return row;
	}

	int getColumn() {
		return column;
	}

	void setCell(int c, int r) {
		row = r;
		column = c;
	}

	public String getName() {
		if (data == null)
			return "";
		if (row == -1) {
			if (column == -1)
				return "matrix \"" + data.getName() + "\"";
			return "character \"" + data.getCharacterName(column) + "\"";
		}
		else if (column == -1) {
			return "taxon \"" + data.getTaxa().getTaxonName(row) + "\"";
		}
		else {
			return "character " + (column + 1) + " of taxon " + (row + 1);
		}
	}

	public String getAnnotation() {
		try {
			if (data == null)
				return null;
			if (row == -1) {
				if (column == -1)
					return data.getAnnotation();
				return data.getAnnotation(column);
			}
			else if (column == -1) {
				return data.getTaxa().getAnnotation(row);
			}
			else {
				return data.getAnnotation(column, row);
			}
		} catch (NullPointerException e) {
			return null;
		}
	}

	public void setAnnotation(String s, boolean notify) {
		if (data == null)
			return;
		if ("".equals(s))
			s = null;
		if (row == -1) {
			if (column == -1) {
				data.setAnnotation(s, notify);
				return;
			}
			data.setAnnotation(column, s);
		}
		else if (column == -1) {
			data.getTaxa().setAnnotation(row, s);
		}
		else {
			data.setAnnotation(column, row, s);
		}
	}
}

/* ======================================================================== */
class ColorLegend extends MousePanel {
	static final int width = 100;

	static final int height = 36;

	String title = null;

	String explanation = null;

	ColorRecord[] colors;

	StringInABox box;

	int[] locs;

	BasicDataWindow w;

	Image goaway;

	public ColorLegend(BasicDataWindow w) {
		super();
		this.w = w;
		setLayout(null);
		setBackground(Color.white);
		setFont(new Font("SansSerif", Font.PLAIN, 10));
		box = new StringInABox("", null, width);
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goaway.gif");
	}

	public void setLegendColors(ColorRecord[] colors, String title, String explanation) {
		this.colors = colors;
		this.title = title;
		this.explanation = explanation;
		if (colors != null)
			locs = new int[colors.length];
	}

	public void paint(Graphics g) {
		if (colors == null && title == null)
			return;
		Color c = g.getColor();
		g.drawImage(goaway, 2, 2, this);
		box.setWidth(getBounds().width);

		box.setFont(g.getFont());
		int vertical = 2;
		int BOXHEIGHT = 20;
		int spaceBetweenBoxes = 3;
		if (title != null) {
			box.setWidth(getBounds().width - 20);
			box.setString(title);
			box.draw(g, 20, vertical);
			vertical += 8 + box.getHeight();
		}
		box.setWidth(getBounds().width);
		g.drawLine(0, vertical - 4, getBounds().width, vertical - 4);
		if (explanation != null) {
			box.setString(explanation);
			box.draw(g, 4, vertical);
			vertical += 8 + box.getHeight();
		}
		box.setWidth(getBounds().width - 36);
		if (colors != null) {
			int maxHeight = (getBounds().height - vertical) / colors.length - 4;
			for (int i = 0; i < colors.length; i++) {
				g.setColor(colors[i].getColor());
				g.fillRect(4, vertical, 28, BOXHEIGHT);
				g.setColor(Color.black);
				g.drawRect(4, vertical, 28, BOXHEIGHT - 1);
				g.setColor(Color.gray);
				g.drawLine(5, vertical + BOXHEIGHT, 33, vertical + BOXHEIGHT);
				g.drawLine(33, vertical + BOXHEIGHT, 33, vertical + 1);
				g.setColor(Color.black);
				locs[i] = vertical;
				box.setString(colors[i].getString());
				box.draw(g, 36, vertical - 4);
				int h = box.getHeight();
				if (h > maxHeight) {
					vertical += 8 + maxHeight;
					g.setColor(Color.white);
					g.fillRect(1, vertical, getBounds().width, getBounds().height - vertical);
				}
				else
					vertical += 8 + h;
			}
		}
		g.setColor(c);
	}

	int findColor(int x, int y) {
		if (locs == null)
			return -1;
		for (int i = 0; i < locs.length; i++)
			if (y >= locs[i] && y <= locs[i] + 20 && x >= 4 && x <= 32)
				return i;
		return -1;
	}

	/* to be used by subclasses to tell that panel touched */
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (clickCount > 1) {
			int c = findColor(x, y);
			if (c >= 0) {
				if (AlertDialog.query(this, "Find Color?", "Do you want to look for this color in the matrix?"))
					w.goToColor(colors[c].getColor());
			}
		}
	}

	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (x < 16 && y < 16)
			w.colorsPanelGoAway();

	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		box.setWidth(w);
	}

	public void setSize(int w, int h) {
		super.setSize(w, h);
		box.setWidth(w);
	}
}

/* ======================================================================== */
class MatrixTable extends mesquite.lib.table.CMTable implements MesquiteDroppedFileHandler {
	BasicDataWindowMaker editorModule;

	Taxa taxa;

	MesquiteBoolean showStates;

	MesquiteBoolean showDefaultCharNames;

	MesquiteBoolean autoWithCharNames;

	MesquiteBoolean showNames;

	MesquiteBoolean showBirdsEyeView;

	MesquiteBoolean showChanges;

	MesquiteBoolean statesSeparateLines;

	MesquiteBoolean allowAutosize;

	MesquiteBoolean showPaleGrid;

	MesquiteBoolean showBoldCellText;
	MesquiteBoolean showPaleCellColors;
	MesquiteBoolean showPaleExcluded;
	MesquiteBoolean showEmptyDataAsClear;
	MesquiteBoolean paleInapplicable;

	int birdsEyeWidth = 2;

	static double showPaleExcludedValueText = 0.40;

	static double showPaleExcludedValueBackground = 0.40;

	boolean notifySuppressed = false;

	CellAnnotation cellAnnotated;

	CharacterData data;

	Font oldFont = null;

	Font boldFont;

	Parser parser = new Parser();

	BasicDataWindow window;

	CellColorer rowNamesColorer = null;

	CellColorer columnNamesColorer = null;

	CellColorer cellColorer = null;

	CellColorer textColorer = null;

	Color bgColor = Color.white;

	Vector linkedTables;

	static int totTables = 0;

	int oldFirstColumn = 0;

	int oldFirstRow = 0;

	int oldLastColumn = 0;

	int oldLastRow = 0;

	int id = 0;

	// DataColumnNamesAssistant assistant1;

	public MatrixTable(BasicDataWindowMaker editorModule, BasicDataWindow window, CharacterData data, int numRowsTotal, int numColumnsTotal, int totalWidth, int totalHeight, int taxonNamesWidth) {
		super(numRowsTotal, numColumnsTotal, totalWidth, totalHeight, taxonNamesWidth, ColorDistribution.getColorScheme(editorModule), true, true);
		setConstrainMaxAutoRownames(true);
		contrainedMaxColumnNum = 1;
		this.window = window;
		setUserMove(true, true);
		linkedTables = new Vector();
		this.data = data;
		cellAnnotated = new CellAnnotation(data);
		showStates = new MesquiteBoolean(true);
		showDefaultCharNames = new MesquiteBoolean(false);
		autoWithCharNames = new MesquiteBoolean(!(data instanceof MolecularData));
		showBirdsEyeView = new MesquiteBoolean(false);
		showPaleGrid = new MesquiteBoolean(false);
		showPaleCellColors = new MesquiteBoolean(false);
		showEmptyDataAsClear = new MesquiteBoolean(false);
		showPaleExcluded = new MesquiteBoolean(false);
		paleInapplicable = new MesquiteBoolean(true);
		showBoldCellText = new MesquiteBoolean(false);
		showChanges = new MesquiteBoolean(!(data instanceof MolecularData));
		allowAutosize = new MesquiteBoolean(!(data instanceof MolecularData));
		showNames = new MesquiteBoolean(true);
		statesSeparateLines = new MesquiteBoolean(false);
		setUserAdjust(MesquiteTable.NOADJUST, MesquiteTable.RESIZE);
		showRowGrabbers = true;
		showColumnGrabbers = true;
		setEditable(true, true, true, false);
		setSelectable(true, true, true, true, true, false);
		this.editorModule = editorModule;
		this.taxa = data.getTaxa();
		setMessage(data.getCellContentsDescription());
		setBackground(ColorTheme.getContentBackground());
		// setBackground(ColorDistribution.project[ColorDistribution.getColorScheme(editorModule)]);
		oldFont = getFont();
		if (oldFont != null)
			boldFont = new Font(oldFont.getName(), Font.BOLD, oldFont.getSize());
		synchronizeRowSelection(taxa);
		synchronizeColumnSelection(data);

		dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);

		id = totTables++;
	}

	void linkTable(MesquiteTable table, boolean linkeeIsNew) {
		linkedTables.addElement(table);
		// tell window table is linked so it can coordinate scrolling if it wants
		window.setTableLinked(true);
		if (linkeeIsNew) {
			for (int ic = 0; ic < table.getNumColumns(); ic++)
				// synchronizing at start
				for (int it = 0; it < table.getNumRows(); it++) {
					if (isCellSelected(ic, it) && !table.isCellSelected(ic, it))
						table.selectCell(ic, it);
					else if (!isCellSelected(ic, it) && table.isCellSelected(ic, it))
						table.deselectCell(ic, it);
				}
		}
	}

	void unlinkTable(MesquiteTable table) {
		linkedTables.removeElement(table);
		// tell window table is unlinked
		window.setTableLinked(false);
	}

	public void setShowPaleExcluded(boolean showPaleExcluded) {
		this.showPaleExcluded.setValue(showPaleExcluded);
	}

	/* ................................................................................................................. */
	public void copyCells(StringBuffer s, boolean literal) {
		copyIt(s, literal, !(data instanceof MolecularData), true);
	}

	/* ................................................................................................................. */
	/**
	 * A request for the MesquiteModule to perform a command. It is passed two strings, the name of the command and the arguments. This should be overridden by any module that wants to respond to a command.
	 */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Paste contents of clipboard into matrix", null, commandName, "paste")) {
			if (window.annotationHasFocus()) {
				return super.doCommand(commandName, arguments, checker);
			}
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
						}
						return s;
					}
					else if (window.componentWithFocus() != null) {
						Component c = window.componentWithFocus();
						if (c instanceof TextComponent) {
							TextComponent tc = (TextComponent) c;
							if (tc.isEditable()) {
								String text = tc.getText();
								String newText = text.substring(0, tc.getSelectionStart()) + s + text.substring(tc.getSelectionEnd(), text.length());
								tc.setText(newText);
							}
							return s;
						}
						else if (c instanceof JTextComponent) {
							JTextComponent tc = (JTextComponent) c;
							if (tc.isEditable()) {
								String text = tc.getText();
								String newText = text.substring(0, tc.getSelectionStart()) + s + text.substring(tc.getSelectionEnd(), text.length());
								tc.setText(newText);
							}
							return s;
						}

					}
					if (clipboardDimensionsFit(s)) {
						editorModule.getProject().incrementProjectWindowSuppression();
						data.incrementNotifySuppress();
						UndoReference pasteUndoReference = UndoReference.getUndoReferenceForMatrixSelection(data, this, editorModule, new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});  //TODO: check to see if there can ever be changes
						pasteIt(s);
						// deselectAll();
						data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, pasteUndoReference));
						data.decrementNotifySuppress();
						editorModule.getProject().decrementProjectWindowSuppression();

						repaintAll();
					}
					else {
						int[] lines = getTabbedLines(s);
						if (lines == null || lines.length == 0 || lines[0] == 0) {
							editorModule.alert("Sorry, the pasted text was not appropriate for the matrix.");
							return null;
						}
						if (data instanceof MolecularData) {

							editorModule.getProject().incrementProjectWindowSuppression();
							boolean success = reshapeMolecular(s, true);
							editorModule.getProject().decrementProjectWindowSuppression();
							if (success) {
								data.incrementNotifySuppress();
								UndoReference pasteUndoReference = UndoReference.getUndoReferenceForMatrixSelection(data, this, editorModule, new int[] {UndoInstructions.CHAR_ADDED_TO_END});
								pasteIt(s);
								// deselectAll();
								data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, pasteUndoReference));
								data.decrementNotifySuppress();
							}
							return null;

						}
						if (!IntegerArray.equalValues(lines)) {
							if (AlertDialog
									.query(editorModule.containerOfModule(), "Paste shape mismatch", "Sorry, the number of lines and of items pasted don't match the spaces selected to be filled.  Would you like Mesquite to attempt to adjust the selected region so that you can paste?  (Note: if you select OK, Mesquite will change the selection but will not paste. You will have to request again to paste.)", "OK", "Cancel", 1))
								setSelectionToShape(lines);
						}
						else {
							if (AlertDialog
									.query(editorModule.containerOfModule(), "Paste shape mismatch", "Sorry, Clipboard is a different size or shape than the region selected, and thus you cannot paste.  Would you like Mesquite to attempt to adjust the selected region so that you can paste?  (Note: if you select OK, Mesquite will change the selection but will not paste. You will have to request again to paste.)", "OK", "Cancel", 1))
								setSelectionToShape(lines);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	/* ................................................................................................................. */
	boolean reshapeMolecular(String s, boolean expandRightToFit) { // return true if succeeded
		Point p = getTopLeftSelected();
		String[] lines = getLines(s);
		int lineCount = 0;
		int top = p.y;
		boolean success = true;
		boolean expandedChars = false;
		boolean expandedTaxa = false;
		if (top < 0) {
			int numTabs = StringUtil.characterCount(lines[lineCount++], '\t');
			if (p.x + numTabs >= numColumnsTotal && expandRightToFit) {
				int starting = data.getNumChars();
				int number = (p.x + numTabs) + 1 - numColumnsTotal;
				if (data.addParts(starting + 1, number)) {
					setNumColumns(data.getNumChars());
					expandedChars = true;
					data.addInLinked(starting + 1, number, true);
				}
			}
			for (int ic = p.x; ic < numColumnsTotal && ic - p.x < numTabs + 1; ic++) {
				selectColumnName(ic);
			}
			top = 0;
		}
		if (p.y + lines.length > numRowsTotal) {
			if (AlertDialog.query(editorModule.containerOfModule(), "Add Taxa?", "To paste the clipboard you would need to expand the taxa block by adding new taxa.  Do you want to do this?", "Expand", "Cancel", 1)) {
				int starting = numRowsTotal;
				int number = p.y + lines.length - numRowsTotal;
				if (data.getTaxa().addTaxa(starting, number, true)) {
					setNumRows(data.getTaxa().getNumTaxa());
					expandedTaxa = true;
				}
			}
			else
				return false;
		}
		int matrixTop = lineCount;
		int maxNumSites = numColumnsTotal;
		for (int it = top; it < p.y + lines.length && it < numRowsTotal && lineCount < lines.length; it++) {
			StringBuffer sb = new StringBuffer(lines[lineCount++]);
			String token = molecToken(sb, p.x < 0);
			int siteCount = p.x;
			while (!StringUtil.blank(token)) {
				siteCount++;
				token = molecToken(sb, false);
			}
			if (siteCount > maxNumSites)
				maxNumSites = siteCount;
		}
		if (maxNumSites > numColumnsTotal) {
			if (!expandRightToFit)
				return false;
			int starting = data.getNumChars() + 1;
			int number = maxNumSites - numColumnsTotal;
			if (data.addParts(starting, number)) {
				setNumColumns(data.getNumChars());
				expandedChars = true;
				data.addInLinked(starting, number, true);
			}
		}
		lineCount = matrixTop;
		for (int it = top; it < p.y + lines.length && it < numRowsTotal && lineCount < lines.length; it++) {
			StringBuffer sb = new StringBuffer(lines[lineCount++]);
			String token = molecToken(sb, p.x < 0);
			for (int ic = p.x; ic < numColumnsTotal && !StringUtil.blank(token); ic++) {
				selectCell(ic, it);
				token = molecToken(sb, false);
			}
			if (!expandRightToFit && !StringUtil.blank(token))
				success = false;

		}
		if (lineCount < lines.length)
			success = false;
		// if (expandedTaxa)
		// data.getTaxa().notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
		if (expandedChars)
			data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
		repaintAll();
		return success;
	}

	boolean lineMatches(int left, int selected, String line) {
		StringBuffer sb = new StringBuffer(line);
		String token = molecToken(sb, left < 0);
		int siteCount = 0;
		while (!StringUtil.blank(token)) {
			siteCount++;
			token = molecToken(sb, false);
		}
		return (siteCount == selected);
	}

	boolean matchesMolecular(String s) {
		Point topLeft = getTopLeftSelected();

		int[] fSelected = getSelectedSpaces();
		String[] lines = getLines(s);
		if (fSelected == null && lines == null)
			return false;
		if (fSelected.length != lines.length)
			return false;
		int firstLine = 0;
		if (topLeft.y < 0) { // includes character names; hence must treat these differently
			int numTabs = StringUtil.characterCount(lines[0], '\t');
			if (numTabs + 1 != fSelected[0])
				return false;
			firstLine = 1;
		}
		for (int i = firstLine; i < lines.length; i++) {
			if (lines[i] == null && fSelected[i] != 0)
				return false;
			if (lines[i] != null) {
				if (!lineMatches(topLeft.x, fSelected[i], lines[i]))
					return false;
			}
		}
		return true;
	}

	/* ................................................................................................................. */
	protected boolean clipboardDimensionsFit(String s) {
		if (data instanceof MolecularData) {
			return (matchesMolecular(s));
		}
		return super.clipboardDimensionsFit(s);
	}

	/* ................................................................................................................. */
	public String[] getLines(String s) {
		return StringUtil.getLines(s);
	}

	/* ................................................................................................................. */
	public void setSelectionToShape(int[] lines) {
		super.setSelectionToShape(lines);
	}

	/* ................................................................................................................. */
	String molecToken(StringBuffer sb, boolean wholeEvenIfNoTab) {
		if (sb.indexOf("\t") >= 0) {
			String result = sb.substring(0, sb.indexOf("\t"));
			sb.delete(0, sb.indexOf("\t") + 1);
			return result;
		}
		else if (wholeEvenIfNoTab) {
			String result = sb.toString();
			sb.setLength(0);
			return result;
		}
		if (sb.length() == 0)
			return null;
		String result = sb.substring(0, 1);
		sb.delete(0, 1);
		return result;

	}
	/* ................................................................................................................. */
	void removeTaxonNameIfPresent(StringBuffer sb) {
		if (sb.indexOf("\t") >= 0) {
			String result = sb.substring(0, sb.indexOf("\t"));
			sb.delete(0, sb.indexOf("\t") + 1);
		}
	}

	/* ................................................................................................................. */
	boolean pasteMolecular(String s) {

		int[] fSelected = getSelectedSpaces();
		if (fSelected == null || fSelected.length == 0)
			return false;
		String[] lines = getLines(s);

		int lineCount = 0;
		boolean sbUsed = false;
		StringBuffer sb = new StringBuffer(lines[0]);
		for (int i = 0; i < numColumnsTotal; i++) {
			if ((isColumnNameSelected(i) || isColumnSelected(i)) && columnNamesCopyPaste) {
				sbUsed = true;
				returnedColumnNameText(i, molecToken(sb, true));
			}
		}
		if (sbUsed)
			lineCount++;
		boolean taxNamesChanged = false;
		boolean atLeastOneFullRowSelected = isAnyRowSelected();
		for (int j = 0; j < numRowsTotal && lineCount < lines.length; j++) {
			if (sbUsed)
				sb = new StringBuffer(lines[lineCount]);
			sbUsed = false;
			if (atLeastOneFullRowSelected) {  // need to remove part before tab if a tab is there
				removeTaxonNameIfPresent(sb);
			} else if (rowNamesCopyPaste && (isRowNameSelected(j))) { // for name of taxon
				returnedRowNameText(j, molecToken(sb, true));
				taxNamesChanged = true;
				sbUsed = true;
			}  
			for (int i = 0; i < numColumnsTotal; i++) {
				if (isCellSelected(i, j) || isRowSelected(j) || isColumnSelected(i)) {
					returnedMatrixText(i, j, molecToken(sb, false));
					sbUsed = true;
				}
			}
			if (sbUsed)
				lineCount++;
		}
		if (taxNamesChanged)
			data.getTaxa().notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
		return true;
	}

	/* ................................................................................................................. */
	protected void pasteIt(String s) {
		if (data instanceof MolecularData) {
			if (pasteMolecular(s)) {
				return;
			}

		}
		taxNC = false; // a thread unsafe way to discover if the superclass set taxon names
		super.pasteIt(s);
		if (taxNC)
			data.getTaxa().notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
		taxNC = false;
	}

	boolean taxNC = false;

	public boolean autoSizeColumns(Graphics g) { // this is EXTREMELY slow for large matrices.
		FontMetrics fm = g.getFontMetrics(g.getFont());
		// int h = fm.getMaxAscent()+ fm.getMaxDescent() + MesquiteModule.textEdgeCompensationHeight; //2 + MesquiteString.riseOffset;
		// setRowHeightsUniform(h);
		// if (!columnNames.isDiagonal())
		// setColumnNamesRowHeight(h);
		autoSizeRows(g);
		String s;
		int tableWIDTHpart = getTableWidth() / 3;
		boolean changed = false;
		int def = fm.stringWidth("G"); // WPMMAT 12
		if (columnNames.isDiagonal()) {
			int def2 = fm.getAscent() + fm.getDescent();
			if (def < def2)
				def = def2;
		}
		int max = def;
		for (int ic = 0; ic < data.getNumChars(false); ic++) {
			if (!columnAdjusted(ic)) {
				if (autoWithCharNames.getValue() && data.characterHasName(ic) && !columnNames.isDiagonal())
					max = fm.stringWidth(getColumnNameTextForDisplay(ic));
				else
					max = def;
				if (showStates.getValue()) {
					for (int it = 0; it < data.getNumTaxa(false); it++) {
						s = getMatrixTextForDisplay(ic, it);
						int lengthString = fm.stringWidth(s);
						if (lengthString > max)
							max = lengthString;
					}
				}
				if (getConstrainMaxAutoColumn() && max > tableWIDTHpart)
					max = tableWIDTHpart;
				int newCW = 0;
				int current = getColumnWidth(ic);
				if (tight.getValue())
					newCW = max + 2; // WPMMAT + MesquiteModule.textEdgeCompensationHeight
				else
					newCW = max + 2 + MesquiteModule.textEdgeCompensationHeight;
				if (newCW != current) {
					setColumnWidth(ic, newCW);
					changed = true;
				}
			}
		}
		return changed;

	}

	/* ............................................................................................................... */
	public FileInterpreter findFileInterpreter(String droppedContents, String fileName) {
		MesquiteWindow mw = getMesquiteWindow();
		if (mw != null) {
			FileCoordinator fileCoord = mw.getOwnerModule().getFileCoordinator();
			FileInterpreter fileInterpreter = fileCoord.findImporter(droppedContents, fileName, 0, StringUtil.argumentMarker + "fuseTaxaCharBlocks", true, data.getStateClass());   //DRM  9 April 2014   added "fuseTaxaCharBlocks" as that is what is happening here
			return fileInterpreter;
		}
		return null;
	}

	/* ............................................................................................................... */
	public void actUponDroppedFileContents(FileInterpreter fileInterpreter, String path) {
		if (fileInterpreter != null) {

			data.setCharNumChanging(true);
			((ReadFileFromString) fileInterpreter).readFileFromString(data, taxa, MesquiteFile.getFileContentsAsString(path), "");
			data.setCharNumChanging(false);

			taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null, null));
			data.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED, null, null));
			data.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED, null, null));
		}
	}

	boolean adjustNewSequences = false;
	int referenceSequence = 0;

	/* ................................................................................................................. */
	public boolean queryDroppedFileOptions() {
		if (!(data instanceof MolecularData))
			return false;
		
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(editorModule.containerOfModule(), "Adjust incoming sequences?", buttonPressed); // MesquiteTrunk.mesquiteTrunk.containerOfModule()
		String s="If you choose to adjust sequences on import, then Mesquite will examine each sequence it imports, and compare it against the reference sequence. ";
		s+="It will reverse complement any DNA sequences that need to be so treated, and then do a partial pairwise alignment (no new gaps will be inserted) of that sequence to the reference sequence.";
		dialog.appendToHelpString(s);
	//	if (data instanceof DNAData) {
	//		dialog.addLabel("Reverse complement a sequence (if needed)");
	//		dialog.addBlankLine();
	//	}
		IntegerField referenceSequenceBox = dialog.addIntegerField("Compare to reference sequence: ", referenceSequence + 1, 8, 1, data.getNumTaxa());
		dialog.completeAndShowDialog("Adjust Sequences", "Don't Adjust", true, null);

		if (buttonPressed.getValue() == 0) {
			referenceSequence = referenceSequenceBox.getValue() - 1;
		}
		dialog.dispose();
		return (buttonPressed.getValue() == 0);
	}
	
	public int numIters (Iterator iter) {
		int count = 0;
		for ( ; iter.hasNext() ; ++count ) iter.next();
		return count;
	}
	/* ................................................................................................................. */
	public void processFilesDroppedOnPanel(List files) {
		int count = 0;

		FileInterpreter fileInterpreter = null;
		int numFiles = numIters(files.iterator());

		for (Iterator iter = files.iterator(); iter.hasNext();) {
			File nextFile = (File) iter.next();
			if (!askListenersToProcess(nextFile, true)) {
				int originalLastTaxonNumber = data.getNumTaxa();
				if (count == 0) {
					fileInterpreter = findFileInterpreter(MesquiteFile.getFileContentsAsString(nextFile.getAbsolutePath()), nextFile.getName());
					if (fileInterpreter == null)
						return;
					fileInterpreter.startRecordingTaxa(taxa);
					fileInterpreter.setTotalFilesToImport(numFiles);
					fileInterpreter.setMultiFileImport(numFiles>1);
					fileInterpreter.setOriginalNumTaxa(data.getNumTaxa());
					fileInterpreter.setMaximumTaxonFilled(-1);

					if (!MesquiteThread.isScripting()) {
						if (data instanceof MolecularData)
							adjustNewSequences = queryDroppedFileOptions();
					}
					
				}
				fileInterpreter.setImportFileNumber(count);
				// system.out.println("next file dropped is: " + nextFile);
				MesquiteMessage.println("\n\nReading file " + nextFile.getName());
				CommandRecord.tick("\n\nReading file " + nextFile.getName());
				actUponDroppedFileContents(fileInterpreter, nextFile.getAbsolutePath());

				count++;
			}
		}
		if (fileInterpreter!=null) {
			if (adjustNewSequences) {
				MesquiteMessage.println("Adjusting sequences ");
				if (!data.someApplicableInTaxon(referenceSequence, false)){  
					MesquiteMessage.println("The reference sequence contains no data; adjustment cancelled.");
				    adjustNewSequences = false;
				}
				if (adjustNewSequences) {
					Bits newTaxa = fileInterpreter.getNewlyAddedTaxa(taxa);
					if (data instanceof DNAData){
						MolecularDataUtil.reverseComplementSequencesIfNecessary((DNAData) data, editorModule, taxa, newTaxa, referenceSequence, false, false); 
					}
					MolecularDataUtil.pairwiseAlignMatrix(editorModule, (MolecularData)data, referenceSequence, newTaxa,0, false);
					data.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED, null, null));
				}
			}
			fileInterpreter.setMaximumTaxonFilled(-1);
			fileInterpreter.endRecordingTaxa(taxa);
		}

		if (fileInterpreter != null)
			fileInterpreter.reset();

	}

	/* ................................................................................................................. */
	public void processFileStringDroppedOnPanel(String path) {
		String contents = MesquiteFile.getURLContentsAsString(path, -1);
		FileInterpreter fileInterpreter = null;
		fileInterpreter = findFileInterpreter(contents, "File Contents");
		if (fileInterpreter != null)
			actUponDroppedFileContents(fileInterpreter, path);

	}

	/* ............................................................................................................... */
	public void tellCellColorerViewChanged() {
		if (cellColorer != null)
			cellColorer.viewChanged();
		if (textColorer != null)
			textColorer.viewChanged();
	}

	boolean settingColumn = false;

	boolean settingRow = false;

	/* ............................................................................................................... */
	/** sets which column is the first visible. */
	public void setFirstColumnVisible(int value, boolean repaintPlease) {
		if (settingColumn)
			return;
		super.setFirstColumnVisible(value, repaintPlease);
		if (oldFirstColumn != getFirstColumnVisible())
			tellCellColorerViewChanged();
		oldFirstColumn = getFirstColumnVisible();
		if (window.linkedScrolling.getValue()) {
			settingColumn = true;
			setFirstColumnVisibleLinked(value);
			settingColumn = false;
		}
	}

	/* ............................................................................................................... */
	/** sets which row is the first visible. */
	public void setFirstRowVisible(int value, boolean repaintPlease) {
		if (settingRow)
			return;
		super.setFirstRowVisible(value, repaintPlease);
		if (oldFirstRow != getFirstRowVisible())
			tellCellColorerViewChanged();
		oldFirstRow = getFirstRowVisible();
		if (window.linkedScrolling.getValue()) {
			settingRow = true;
			setFirstRowVisibleLinked(value);
			settingRow = false;
		}
	}

	/* ............................................................................................................... */
	/** Resets sizes of all components. */
	public void resetComponentSizes() {
		super.resetComponentSizes();
		if (oldFirstColumn != getFirstColumnVisible() && oldFirstRow != getFirstRowVisible() && oldLastColumn != getLastColumnVisible() && oldLastRow != getLastRowVisible())
			tellCellColorerViewChanged();
		oldFirstColumn = getFirstColumnVisible();
		oldLastColumn = getLastColumnVisible();
		oldFirstRow = getFirstRowVisible();
		oldLastRow = getLastRowVisible();
	}

	/* ............................................................................................................... */
	public String getUpperCornerText() {
	//	if (data == null)
			return "";
		
//		return data.getName();
	}
	/* ............................................................................................................... */
	public String getCornerText() {
		return "Taxon  \\  Character";
	}

	/* ............................................................................................................... */
	public BasicDataWindow getBasicDataWindow() {
		return window;
	}

	/* ............................................................................................................... */
	/** sets which row is the first visible. */
	public void setFirstRowVisible(int value) {
		super.setFirstRowVisible(value);
		window.setExplanation("Characters " + (getFirstColumnVisible() + 1) + " to " + (getLastColumnVisible() + 1) + "; Taxa " + +(getFirstRowVisible() + 1) + " to " + (getLastRowVisible() + 1));
	}

	/* ............................................................................................................... */
	/** sets which column is the first visible. */
	public void setFirstColumnVisible(int value) {
		super.setFirstColumnVisible(value);
		window.setExplanation("Characters " + (getFirstColumnVisible() + 1) + " to " + (getLastColumnVisible() + 1) + "; Taxa " + +(getFirstRowVisible() + 1) + " to " + (getLastRowVisible() + 1));
	}

	/* ............................................................................................................... */
	/** returns color of row or number box. */
	public Color getRCNumberBoxColor(boolean isRow, int number) {
		Color color = null;
		if (!isRow)
			color = data.getDefaultCharacterColor(number);
		if (color != null)
			return color;
		else if (!isRow)
			return ColorTheme.getContentElement();
		return ColorTheme.getContentElement();
	}

	/* ............................................................................................................... */
	/** returns dark color of row or number box. */
	public Color getRCNumberBoxDarkColor(boolean isRow, int number) {
		Color color = null;
		if (!isRow)
			color = data.getDarkDefaultCharacterColor(number);
		if (color != null)
			return color;
		// return ColorDistribution.dark[colorScheme];
		return ColorTheme.getContentDarkElement();
	}

	/* ............................................................................................................... */
	public DataColumnNamesAssistant getDataColumnNamesAssistant(int subRow) {
		return window.getDataColumnNamesAssistant(subRow);
	}

	/* ............................................................................................................... */
	public void drawColumnNamesPanelExtras(Graphics g, int left, int top, int width, int height) {
		if (data == null)
			return;
		Color oldColor = g.getColor();
		for (int extraRow = 0; extraRow < window.numDataColumnNamesAssistants(); extraRow++) {
			DataColumnNamesAssistant assistant = window.getDataColumnNamesAssistant(extraRow);
			for (int c = getFirstColumnVisible(); (c < numColumnsTotal) && (c <= getLastColumnVisible() + 1); c++) {
				int leftSide = columnNames.startOfColumn(c);
				int topSide = columnNames.extraRowTop(extraRow);

				if (assistant != null)
					assistant.drawInCell(c, g, leftSide, topSide, columnNames.columnWidth(c), columnNames.rowHeight(-1), false);
				g.setColor(Color.gray);
				g.drawRect(leftSide, topSide, columnNames.columnWidth(c), columnNames.rowHeight(-1));
			}
		}
		g.setColor(oldColor);
	}

	/* ............................................................................................................... */
	float[] hsb = new float[3];

	/*
	 * MesquiteTimer timer1 = new MesquiteTimer(); MesquiteTimer timer2 = new MesquiteTimer(); MesquiteTimer timer3 = new MesquiteTimer(); MesquiteTimer timer4 = new MesquiteTimer(); MesquiteTimer timer5 = new MesquiteTimer(); MesquiteTimer timer6 = new MesquiteTimer();
	 * 
	 * timer1.start(); //600
	 * 
	 * timer1.end(); timer2.start(); //648
	 * 
	 * timer2.end(); timer3.start(); //277
	 * 
	 * timer3.end(); timer4.start(); //57
	 * 
	 * timer4.end(); timer5.start(); //500
	 * 
	 * timer5.end(); timer6.start(); //270
	 * 
	 * timer6.end(); .println("timers 1 " + timer1.getAccumulatedTime() + " timer2 " + timer2.getAccumulatedTime() + " timer3 " + timer3.getAccumulatedTime() + " timer4 " + timer4.getAccumulatedTime() + " timer5 " + timer5.getAccumulatedTime() + " timer6 " + timer6.getAccumulatedTime());
	 */
	NameReference notesNameRef = NameReference.getNameReference("notes");

	public boolean isAttachedNoteAvailable(int column, int row) {
		if (data == null)
			return false;
		AttachedNotesVector anv = null;
		try {
			if (column < 0)
				anv = (AttachedNotesVector) data.getTaxa().getAssociatedObject(notesNameRef, row);
			else if (row < 0)
				anv = (AttachedNotesVector) data.getAssociatedObject(notesNameRef, column);
			else
				anv = (AttachedNotesVector) data.getCellObject(notesNameRef, column, row);
		} catch (Exception e) {
		}
		if (anv == null)
			return false;
		return (anv.getNumNotes() > 0);
	}

	MesquiteBoolean overflow = new MesquiteBoolean(false);

	public void drawMatrixCell(Graphics g, int x, int y, int w, int h, int column, int row, boolean selected) {
		if (data == null)
			return;
		boolean writeStates = !showBirdsEyeView.getValue() && showStates.getValue();
		boolean leaveEdges = writeStates && !tight.getValue();
		drawMatrixCell(g, x, y, w, h, column, row, selected, writeStates, leaveEdges);

	}

	public CellColorer getCellColorer() {
		return cellColorer;
	}

	public synchronized void drawMatrixCell(Graphics g, int x, int y, int w, int h, int column, int row, boolean selected, boolean writeStates, boolean leaveEdges) {
		if (data == null)
			return;
		boolean changedSinceSave = showChanges.getValue() && data.getChangedSinceSave(column, row);

		boolean annotationAvailable = isAttachedNoteAvailable(column, row);

		Color c = g.getColor();
		hsb[0] = hsb[1] = hsb[2] = 1;

		Color fillColor = null;
		// ColorDistribution.setTransparentGraphics(g);
		// g.setColor(Color.gray);

		if (cellColorer != null) {
			try {
				fillColor = cellColorer.getCellColor(column, row);
				if (paleCellColors && fillColor != null)
					fillColor = ColorDistribution.brighter(fillColor, 0.45);
			} catch (Throwable e) {
			}
		}
		
/*		if (paleInapplicable.getValue() && data.isTerminalInapplicable(column, row)){
				fillColor = Color.white;
				//fillColor = Color.lightGray;
				g.setColor(ColorDistribution.veryVeryLightGray);
				g.drawRect(x, y+1, w, h-2);
		}
		else { */
			if (fillColor == null)
				fillColor = bgColor;
			if (showPaleExcluded.getValue() && !data.isCurrentlyIncluded(column))
				fillColor = ColorDistribution.brighter(fillColor, showPaleExcludedValueBackground);
			Color.RGBtoHSB(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), hsb);
		//}
		g.setColor(fillColor);

		if (leaveEdges)
			g.fillRect(x + 1, y + 1, w - 1, h - 1);
		else
			g.fillRect(x, y, w, h);

		if (selected) {
			if (leaveEdges)
				GraphicsUtil.fillTransparentSelectionRectangle(g, x + 1, y + 1, w - 1, h - 1);
			else
				GraphicsUtil.fillTransparentSelectionRectangle(g, x, y, w, h);
		}

		if (writeStates) {
			if (changedSinceSave) {
				g.setColor(ColorDistribution.getContrasting(selected, fillColor, hsb, Color.lightGray, Color.darkGray));
				g.drawLine(x, y + 1, x + 1, y);
				g.drawLine(x, y + 2, x + 2, y);
				g.drawLine(x, y + 3, x + 3, y);
			}
			if (annotationAvailable) {
				g.setColor(ColorDistribution.getContrasting(selected, fillColor, hsb, Color.white, Color.black));
				g.drawLine(x + w - 2, y + 1, x + w - 2, y + 2); // left
				g.drawLine(x + w - 2, y + 2, x + w, y + 2); // bottom
				g.drawLine(x + w, y + 1, x + w, y + 2); // right
				g.drawLine(x + w - 2, y, x + w, y); // top
				if (!selected) {
					g.setColor(Color.white);
					g.drawLine(x + w - 1, y + 1, x + w - 1, y + 1);
				}
			}

			try {
				Color textColor = null;
				if (textColorer != null && !(textColorer instanceof mesquite.charMatrices.NoColor.NoColor))
					textColor = textColorer.getCellColor(column, row);

				if (textColor == null) {
					if (paleInapplicable.getValue() && data.isInapplicable(column, row)){
					/*	if (data.isTerminalInapplicable(column, row))
							textColor = Color.white;
						else */
							textColor = Color.lightGray;
					}
					else
						textColor = ColorDistribution.getContrasting(selected, fillColor, hsb, Color.white, Color.black);
				}
				if (showPaleExcluded.getValue() && !data.isCurrentlyIncluded(column))
					textColor = ColorDistribution.brighter(textColor, showPaleExcludedValueText);
				g.setColor(textColor);
			} catch (Exception e) {
			}

			String st = getMatrixTextForDisplay(column, row);
			overflow.setValue(false);
			int cent = StringUtil.getStringCenterPosition(st, g, x, w, overflow);
			boolean useClip = overflow.getValue();
			int vert = StringUtil.getStringVertPosition(g, y, h, overflow);
			useClip |= overflow.getValue();
			Shape clip = null;
			if (useClip) {
				clip = g.getClip();
				g.setClip(x, y, w, h);
			}
			if (st != null)
				g.drawString(st, cent, vert); // this is very time costly on OSX java 1.4!! (as of 10.3.9 on Powerbook G4)
			if (useClip) {
				g.setClip(clip);
			}

		}
		if (c != null)
			g.setColor(c);

	}

	/* ............................................................................................................... */
	public boolean useString(int column, int row) {
		return false; // (cellColorer == null && !data.getChangedSinceSave(column, row));
	}

	public Color getColumnNameFillColor(int column, Color defaultFillColor, boolean focused, boolean selected, boolean dimmed, boolean editable) {
		Color color = null;
		if (selected) {
			color = Color.white;
		}
		else if (columnNamesColorer != null) {
			color = columnNamesColorer.getCellColor(column, -1);
		}
		if (color == null)
			color = bgColor;
		return color;
	}

	/* ............................................................................................................... */
	public void drawColumnNameCell(Graphics g, int x, int y, int w, int h, int column) {

		if (data == null)
			return;
		boolean annotationAvailable = isAttachedNoteAvailable(column, -1);
		Color fillColor = null;
		boolean selected = isColumnNameSelectedAnyWay(column);
		fillColor = getColumnNameFillColor(column, Color.white, false, selected, false, true);

		if (fillColor != null) {
			Color c = g.getColor();
			g.setColor(fillColor);
			columnNames.fillCell(g, x, y, w, h, selected);
			/*
			 * g.fillRect(x+1,y+1,w-1,h-1); if (selected) GraphicsUtil.fillTransparentSelectionRectangle(g,x+1,y+1,w-1,h-1);
			 */
			if (c != null)
				g.setColor(c);

		}

		hsb[0] = hsb[1] = hsb[2] = 1;
		Color.RGBtoHSB(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), hsb);
		Color oldColor = null;
		if (annotationAvailable) {
			oldColor = g.getColor();
			g.setColor(ColorDistribution.getContrasting(selected, fillColor, hsb, Color.white, Color.black));
			g.drawLine(x + w - 2, y + 1, x + w - 2, y + 2); // left
			g.drawLine(x + w - 2, y + 2, x + w, y + 2); // bottom
			g.drawLine(x + w, y + 1, x + w, y + 2); // right
			g.drawLine(x + w - 2, y, x + w, y); // top
			if (!selected) {
				g.setColor(Color.white);
				g.drawLine(x + w - 1, y + 1, x + w - 1, y + 1);
			}
			g.setColor(oldColor);
		}
		if (selected) {
			g.setColor(Color.white);
		}

		oldColor = g.getColor();

		Color textColor = null;
		if (textColorer != null && !(textColorer instanceof mesquite.charMatrices.NoColor.NoColor))
			textColor = textColorer.getCellColor(column, -1);

		if (textColor == null) {
			textColor = ColorDistribution.getContrasting(selected, fillColor, hsb, Color.white, Color.black);
		}
		g.setColor(textColor);

		String name = getColumnNameTextForDisplay(column);
		String s = data.getAnnotation(column);
		if (!StringUtil.blank(s))
			name = "*" + name;

		g.drawString(name, x + getNameStartOffset(), StringUtil.getStringVertPosition(g, y, h, null));

		g.setColor(oldColor);
	}

	/* ............................................................................................................... */
	public void drawRowNameCell(Graphics g, int x, int y, int w, int h, int row) {
		if (taxa == null || row >= taxa.getNumTaxa())
			return;
		Color fillColor = null;

		boolean annotationAvailable = isAttachedNoteAvailable(-1, row);
		boolean selected = isRowNameSelectedAnyWay(row);
		if (selected) {
			fillColor = Color.white;
		}
		else if (rowNamesColorer != null) {
			fillColor = rowNamesColorer.getCellColor(-1, row);
		}
		if (fillColor == null)
			fillColor = bgColor;
		if (fillColor != null) {
			Color c = g.getColor();
			g.setColor(fillColor);
			g.fillRect(x + 1, y + 1, w - 1, h - 1);
			if (selected)
				GraphicsUtil.fillTransparentSelectionRectangle(g, x + 1, y + 1, w - 1, h - 1);
			if (c != null)
				g.setColor(c);
		}
		hsb[0] = hsb[1] = hsb[2] = 1;
		Color.RGBtoHSB(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), hsb);

		Color oldColor = null;
		if (annotationAvailable) {
			oldColor = g.getColor();
			g.setColor(ColorDistribution.getContrasting(selected, fillColor, hsb, Color.white, Color.black));
			g.drawLine(x + w - 3, y + 1, x + w - 3, y + 2); // left
			g.drawLine(x + w - 3, y + 2, x + w - 1, y + 2); // bottom
			g.drawLine(x + w - 1, y + 1, x + w - 1, y + 2); // right
			g.drawLine(x + w - 3, y, x + w - 1, y); // top
			if (!selected) {
				g.setColor(Color.white);
				g.drawLine(x + w - 2, y + 1, x + w - 2, y + 1);
			}
			g.setColor(oldColor);
		}
		oldColor = g.getColor();
		Color textColor = null;
		if (textColorer != null && !(textColorer instanceof mesquite.charMatrices.NoColor.NoColor))
			textColor = textColorer.getCellColor(-1, row);

		if (textColor == null) {
			textColor = ColorDistribution.getContrasting(selected, fillColor, hsb, Color.white, Color.black);
		}
		g.setColor(textColor);

		String s = taxa.getAnnotation(row);

		int svp = StringUtil.getStringVertPosition(g, y, h, null);
		int xgnso = x + getNameStartOffset();

		if (getShowRowNames()){
			String t = taxa.getTaxon(row).getName();
			if (t == null)
				t = "";
			if (!StringUtil.blank(s))
				g.drawString("*" + t, xgnso, svp);
			else
				g.drawString(t, xgnso, svp);
		}

		g.setColor(oldColor);

	}

	Color distinguishTextColor(Graphics g, Color bg) {
		Color current = g.getColor();
		if (current.equals(bg)) {
			if (current.equals(Color.black))
				g.setColor(Color.white);
			else
				g.setColor(Color.black);
			return current;
		}
		return null;

	}

	StringBuffer sb = new StringBuffer(50);

	/* ............................................................................................................... */
	public synchronized String getMatrixText(int column, int row) {
		sb.setLength(0);
		data.statesIntoStringBuffer(column, row, sb, false);
		return sb.toString();
	}

	boolean displayModifications = false;

	private boolean checkDisplayModifications() {
		try {
			// go through all assistants to see if they claim hasDisplayModifications
			Enumeration enumeration = window.ownerModule.getEmployeeVector().elements();
			while (enumeration.hasMoreElements()) {
				Object obj = enumeration.nextElement();
				if (obj instanceof DataWindowAssistant) {
					DataWindowAssistant init = (DataWindowAssistant) obj;
					if (init.hasDisplayModifications()) {
						return true;
					}
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/* ............................................................................................................... */
	public void setLastColumnVisibleLinked(int column) {
		for (int i = 0; i < linkedTables.size(); i++) {
			MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
			t.setLastColumnVisible(column);
		}
	}

	/* ............................................................................................................... */
	public void setFirstColumnVisibleLinked(int column) {
		for (int i = 0; i < linkedTables.size(); i++) {
			MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
			t.setFirstColumnVisible(column);
		}
	}

	/* ............................................................................................................... */
	public void setLastRowVisibleLinked(int row) {
		for (int i = 0; i < linkedTables.size(); i++) {
			MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
			t.setLastRowVisible(row);
		}
	}

	/* ............................................................................................................... */
	public void setFirstRowVisibleLinked(int row) {
		for (int i = 0; i < linkedTables.size(); i++) {
			MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
			t.setFirstRowVisible(row);
		}
	}

	/* ............................................................................................................... */
	public synchronized String getMatrixTextForDisplay(int column, int row) {

		sb.setLength(0);
		data.statesIntoStringBuffer(column, row, sb, showNames.getValue());
		if (!StringUtil.blank(data.getAnnotation(column, row))) {
			sb.append('*');
		}
		if (displayModifications) {
			if (data.getCellObjectDisplay(column, row)) {
				// go through assistants that claim to have modifications to see what each wants to add
				Enumeration enumeration = window.ownerModule.getEmployeeVector().elements();
				while (enumeration.hasMoreElements()) {
					Object obj = enumeration.nextElement();
					if (obj instanceof DataWindowAssistant) {
						DataWindowAssistant init = (DataWindowAssistant) obj;
						if (init.hasDisplayModifications()) {
							String s = init.getDisplayModString(column, row);
							if (s != null)
								sb.append(s);
						}
					}
				}
			}
		}
		return sb.toString();

	}

	/* ................................................................................................................. */
	public String getColumnComment(int column) {
		return data.getAnnotation(column);
	}

	/* ................................................................................................................. */
	public String getRowComment(int row) {
		return taxa.getAnnotation(row);
	}

	/* ............................................................................................................... */
	/** Returns text in row name. */
	public synchronized String getRowNameText(int row) {
		Taxon t = taxa.getTaxon(row);
		if (t != null) {
			return t.getName();
		}
		else
			return "";
	}

	/* ............................................................................................................... */
	/** Returns text in column name. */
	public synchronized String getColumnNameText(int column) {
		return data.getCharacterName(column);
	}

	/* ............................................................................................................... */
	/** Returns text in column name. */
	public synchronized String getColumnNameTextForDisplay(int column) {
		if (data.characterHasName(column))
			return data.getCharacterName(column);
		else if (showDefaultCharNames.getValue())
			return Integer.toString(CharacterStates.toExternal(column));
		return "";
	}

	/* ............................................................................................................... */
	void broadcastFocusInCell(int column, int row) {
		if (window.sequenceLedge != null)
			window.sequenceLedge.setFocus(this, column, row);
		ListableVector v = null;
		try {
			v = window.ownerModule.getEmployeeVector();
		} catch (Exception e) {
		}
		if (v != null)
			for (int i = 0; i < v.size(); i++) {
				Object obj = null;
				// try {
				obj = v.elementAt(i);
				if (obj instanceof DataWindowAssistant) {
					DataWindowAssistant a = (DataWindowAssistant) obj;
					a.focusInCell(column, row);
				}

				/*
				 * } catch(Exception e){ MesquiteMessage.warnProgrammer("exception in broadcastFocusInCell to " + obj + "   " + e); e.printStackTrace(); }
				 */

			}
	}

	/* ............................................................................................................... */
	StringBuffer esb = new StringBuffer(100);

	String getCellExplanation(int column, int row) {
		if (data == null)
			return null;
		String s = "[";
		try {
			if (row >= 0 && row < data.getNumTaxa())
				s += "t." + (row + 1);
			if (column >= 0 && column < data.getNumChars())
				s += " c." + (column + 1);
			if (row >= 0 && row < data.getNumTaxa() && column >= 0 && column < data.getNumChars()) {
				esb.setLength(0);
				data.statesIntoStringBuffer(column, row, esb, false);
				s += " s." + esb;
			}
			s += "] ";
			if (column >= 0 && column < data.getNumChars())
				s += data.getCharacterName(column) + statesExplanation(column, row);
			if (row >= 0 && row < data.getNumTaxa() && column >= 0)
				s += " [in taxon \"" + data.getTaxa().getTaxonName(row) + "\"]";

			if (window == null || window.ownerModule == null)
				return s;
			ListableVector v = window.ownerModule.getEmployeeVector();
			if (v != null)
				for (int i = 0; i < v.size(); i++) {
					Object obj = v.elementAt(i);
					if (obj instanceof DataWindowAssistant) {
						DataWindowAssistant a = (DataWindowAssistant) obj;
						String ce = a.getCellExplanation(column, row);
						if (!StringUtil.blank(ce))
							s += "\n" + ce;
						if (a == cellColorer) {
							String colors = a.getCellString(column, row);
							if (!StringUtil.blank(colors))
								s += "\nColor of cell: " + colors + "\n";
						}
					}
				}

			AttachedNotesVector anv = null;
			if (column < 0)
				anv = (AttachedNotesVector) data.getTaxa().getAssociatedObject(notesNameRef, row);
			else if (row < 0)
				anv = (AttachedNotesVector) data.getAssociatedObject(notesNameRef, column);
			else
				anv = (AttachedNotesVector) data.getCellObject(notesNameRef, column, row);

			if (anv != null && anv.getNumNotes() > 0) {
				s += "\n-----------------";
				s += "\nAnnotations:";
				for (int i = 0; i < anv.getNumNotes(); i++) {
					AttachedNote note = anv.getAttachedNote(i);
					if (note != null) {
						String c = note.getComment();
						if (!StringUtil.blank(c)) {
							s += "\n" + (c.replace('\n', ' ')).replace('\r', ' ');
							if (!StringUtil.blank(note.getAuthorName()))
								s += "  (author: " + note.getAuthorName() + ")";
						}
					}
				}
				s += "\n-----------------";
			}
		} catch (Throwable t) {
			s = "";
			MesquiteFile.throwableToLog(null, t);
		}
		return s;
	}

	public void setFocusedCell(int column, int row) {
		super.setFocusedCell(column, row);
		cellAnnotated.setCell(column, row);
		window.setAnnotation(cellAnnotated);
		window.setExplanation(getCellExplanation(column, row));
		broadcastFocusInCell(column, row);
		if ((window.getCurrentTool() == window.arrowTool) && window.ibeamTool != null && (isEditing(column, row))) {
			window.setCurrentTool(window.ibeamTool);
			window.getPalette().setCurrentTool(window.ibeamTool);
		}
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellTouch(column, row);
	}

	void refreshAnnotation() {
		window.setAnnotation(cellAnnotated);
	}

	/* ............................................................................................................... */
	public void clickOutside() {
		setFocusedCell(-2, -2);
	}

	boolean suppressSelect = false;

	public void selectRow(int row) {
		super.selectRow(row);
		// data.getTaxa().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		// notifySelectionChanged();
	}

	public void selectRows(int first, int last) {
		super.selectRows(first, last);
		// data.getTaxa().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		// notifySelectionChanged();
	}

	public void selectColumn(int column) {
		super.selectColumn(column);
		// data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		// notifySelectionChanged();
	}

	public void selectColumns(int first, int last) {
		super.selectColumns(first, last);
		// data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		// notifySelectionChanged();
	}

	public void selectCell(int column, int row) {
		if (!suppressSelect) {
			super.selectCell(column, row);
			suppressSelect = true;
			for (int i = 0; i < linkedTables.size(); i++) {
				MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
				t.selectCell(column, row);
				t.redrawCell(column, row);
			}
			suppressSelect = false;
			notifySelectionChanged();
		}
	}

	public void deselectCell(int column, int row) {
		if (!suppressSelect) {
			super.deselectCell(column, row);
			suppressSelect = true;
			for (int i = 0; i < linkedTables.size(); i++) {
				MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
				t.deselectCell(column, row);
				t.redrawCell(column, row);
			}
			suppressSelect = false;
			notifySelectionChanged();
		}
	}

	public void selectBlock(int firstColumn, int firstRow, int lastColumn, int lastRow) {
		if (!suppressSelect) {
			super.selectBlock(firstColumn, firstRow, lastColumn, lastRow);
			suppressSelect = true;
			for (int i = 0; i < linkedTables.size(); i++) {
				MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
				t.selectBlock(firstColumn, firstRow, lastColumn, lastRow);
				t.redrawBlock(firstColumn, firstRow, lastColumn, lastRow);
			}
			suppressSelect = false;
			notifySelectionChanged();
		}
	}
	
	public void deSelectBlock(int firstColumn, int firstRow, int lastColumn, int lastRow) {
		if (!suppressSelect) {
			super.deSelectBlock(firstColumn, firstRow, lastColumn, lastRow);
			suppressSelect = true;
			for (int i = 0; i < linkedTables.size(); i++) {
				MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
				t.deSelectBlock(firstColumn, firstRow, lastColumn, lastRow);
				t.redrawBlock(firstColumn, firstRow, lastColumn, lastRow);
			}
			suppressSelect = false;
			notifySelectionChanged();
		}
	}

	public void deselectAllCells(boolean notify) {
		if (!suppressSelect) {
			super.deselectAllCells(notify);
			suppressSelect = true;
			for (int i = 0; i < linkedTables.size(); i++) {
				MesquiteTable t = (MesquiteTable) linkedTables.elementAt(i);
				t.deselectAllCells(false);
				t.repaintAll();
			}
			suppressSelect = false;
			if (notify)
				notifySelectionChanged();
		}
	}

	public void synchronizeRowSelection(Associable a) {
		super.synchronizeRowSelection(a);
		notifySelectionChanged();
	}

	public void synchronizeColumnSelection(Associable a) {
		super.synchronizeColumnSelection(a);
		notifySelectionChanged();
	}

	void notifySelectionChanged() {

		if (MesquiteWindow.checkDoomed(this))
			return;
		try {
			Enumeration enumeration = window.ownerModule.getEmployeeVector().elements();
			while (enumeration.hasMoreElements()) {
				Object obj = enumeration.nextElement();
				if (obj instanceof DataWindowAssistantI) {
					DataWindowAssistantI init = (DataWindowAssistantI) obj;
					init.tableSelectionChanged();
				}
			}
		} catch (Exception e) { // just in case being disposed....
		}
		MesquiteWindow.uncheckDoomed(this);

	}

	/* ............................................................................................................... */
	/** Called if part of panel out of bounds is touched. */
	public void outOfBoundsTouched(int modifiers, int clickCount) {
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellTouch(-2, -2);
	}

	/* ............................................................................................................... */
	public void cellTouched(int column, int row, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {

		if ((window.getCurrentTool() == window.arrowTool) && (clickCount > 1) && window.ibeamTool != null) {
			window.setCurrentTool(window.ibeamTool);
			window.getPalette().setCurrentTool(window.ibeamTool);
			((TableTool) window.getCurrentTool()).cellTouched(column, row, regionInCellH, regionInCellV, modifiers);
		}
		else if (((TableTool) window.getCurrentTool()).useTableTouchRules()) {
			super.cellTouched(column, row, regionInCellH, regionInCellV, modifiers, clickCount);
		}
		else {
			((TableTool) window.getCurrentTool()).cellTouched(column, row, regionInCellH, regionInCellV, modifiers);
		}
		if (window.getCurrentTool() == window.arrowTool || window.getCurrentTool() == window.ibeamTool || window.getCurrentTool().getAllowAnnotate() || ((TableTool) window.getCurrentTool()).useTableTouchRules()) {
			setFocusedCell(column, row);
		}
		else
			window.setAnnotation("", null);
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellTouch(column, row);
	}

	/* ............................................................................................................... */
	public void cellDrag(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (((TableTool) window.getCurrentTool()).useTableTouchRules())
			super.cellDrag(column, row, regionInCellH, regionInCellV, modifiers);
		else
			((TableTool) window.getCurrentTool()).cellDrag(column, row, regionInCellH, regionInCellV, modifiers);
	}

	/* ............................................................................................................... */
	public void cellDropped(int column, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (((TableTool) window.getCurrentTool()).useTableTouchRules())
			super.cellDropped(column, row, regionInCellH, regionInCellV, modifiers);
		else
			((TableTool) window.getCurrentTool()).cellDropped(column, row, regionInCellH, regionInCellV, modifiers);

	}

	/* ............................................................................................................... */
	private void checkTouchCurrentCell(int oldColumn, int oldRow) {
		if (((TableTool) window.getCurrentTool()).getTouchOnArrowKey()) {
			if (singleTableCellSelected()) {
				Dimension sel = getFirstTableCellSelected();
				if (oldColumn != sel.width || oldRow != sel.height) {
					((TableTool) window.getCurrentTool()).cellTouched(sel.width, sel.height, 50, 50, 0);
				}
			}
		}
	}

	/* ............................................................................................................... */
	public void downArrowPressed(String arguments) {
		Dimension sel = getFirstTableCellSelected();
		super.downArrowPressed(arguments);
		checkTouchCurrentCell(sel.width, sel.height);
	}

	/* ............................................................................................................... */
	public void upArrowPressed(String arguments) {
		Dimension sel = getFirstTableCellSelected();
		super.upArrowPressed(arguments);
		checkTouchCurrentCell(sel.width, sel.height);
	}

	/* ............................................................................................................... */
	public void rightArrowPressed(String arguments) {
		Dimension sel = getFirstTableCellSelected();
		super.rightArrowPressed(arguments);
		checkTouchCurrentCell(sel.width, sel.height);
	}

	/* ............................................................................................................... */
	public void leftArrowPressed(String arguments) {
		Dimension sel = getFirstTableCellSelected();
		super.leftArrowPressed(arguments);
		checkTouchCurrentCell(sel.width, sel.height);
	}

	/* ............................................................................................................... */
	public void rowNameTouched(int row, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		if (window.getCurrentTool() == window.arrowTool || window.getCurrentTool() == window.ibeamTool || window.getCurrentTool().getAllowAnnotate()) {
			cellAnnotated.setCell(-1, row);
			window.setAnnotation(cellAnnotated);
		}
		else
			window.setAnnotation("", null);

		if (window.getCurrentTool() == window.arrowTool)
			if (clickCount > 1 && window.ibeamTool != null) {
				window.setCurrentTool(window.ibeamTool);
				window.getPalette().setCurrentTool(window.ibeamTool);
				((TableTool) window.getCurrentTool()).cellTouched(-1, row, regionInCellH, regionInCellV, modifiers);
			}
			else
				super.rowNameTouched(row, regionInCellH, regionInCellV, modifiers, clickCount);
		else
			((TableTool) window.getCurrentTool()).cellTouched(-1, row, regionInCellH, regionInCellV, modifiers);
		broadcastFocusInCell(-1, row);
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellTouch(-1, row);
	}

	/* ............................................................................................................... */
	public void rowTouched(boolean isArrowEquivalent, int row, int regionInCellH, int regionInCellV, int modifiers) {
		if (((TableTool) window.getCurrentTool()).useTableTouchRules() || isArrowEquivalent)
			super.rowTouched(isArrowEquivalent, row, regionInCellH, regionInCellV, modifiers);
		else
			((TableTool) window.getCurrentTool()).cellTouched(-1, row, regionInCellH, regionInCellV, modifiers);
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellTouch(-1, row);
	}

	/* ............................................................................................................... */
	private String statesExplanation(int column, int row) {
		String s = "";
		if (data.getClass() == CategoricalData.class) {
			s += ":  ";
			if (statesSeparateLines.getValue())
				s += "\n";
			long state = ((CategoricalData) data).getState(column, row);
			CategoricalData cData = (CategoricalData) data;
			for (int i = 0; i <= CategoricalState.maxCategoricalState; i++)
				if (cData.hasStateName(column, i)) {
					s += "(" + cData.getSymbol(i) + ")";
					if (CategoricalState.isElement(state, i))
						s += "*";
					s += " " + cData.getStateName(column, i);
					if (statesSeparateLines.getValue())
						s += "\n";
					else
						s += "; ";
				}
		}
		else if (data instanceof MolecularData) {
			int count = 0;
			for (int ic = 0; ic <= column; ic++) {
				CategoricalData cData = (CategoricalData) data;
				if (!cData.isInapplicable(ic, row))
					count++;
			}
			if (count > 0) {
				s += "; Sequence site " + count;
			}
		}
		CharactersGroup g = data.getCurrentGroup(column);
		if (g!= null)
			s += " [group: " + g.getName() + "]";
		return s;
	}

	public void mouseExitedCell(int modifiers, int column, int subColumn, int row, int subRow, MesquiteTool tool) {
		super.mouseExitedCell(modifiers, column, subColumn, row, subRow, tool);
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellExit(column, row);
	}

	/* ............................................................................................................... */
	public void mouseInCell(int modifiers, int column, int subColumn, int row, int subRow, MesquiteTool tool) {
				
		if (column >= 0 && column <= data.getNumChars(false)) {
			if (subRow >= 0) {
				DataColumnNamesAssistant assistant = window.getDataColumnNamesAssistant(subRow);
				if (assistant != null) {
					String s = assistant.getStringForExplanation(column);
					window.setExplanation(s);
				}
			}
			else if ((!(matrix.getEditing() || rowNames.getEditing() || columnNames.getEditing()) && !singleTableCellSelected())) {
				window.setExplanation(getCellExplanation(column, row));
				broadcastFocusInCell(column, row);
			}
		}
		else if ((!(matrix.getEditing() || rowNames.getEditing() || columnNames.getEditing()) && !singleTableCellSelected())) {
			window.setExplanation(getCellExplanation(column, row));
			broadcastFocusInCell(column, row);
		}
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellEnter(column, row);
	}

	/* ............................................................................................................... */
	public void columnNameTouched(int column, int regionInCellH, int regionInCellV, int modifiers, int clickCount) {
		if (window.getCurrentTool() == window.arrowTool || window.getCurrentTool() == window.ibeamTool || window.getCurrentTool().getAllowAnnotate()) {
			cellAnnotated.setCell(column, -1);
			window.setAnnotation(cellAnnotated);

		}
		else
			window.setAnnotation("", null);

		if (window.getCurrentTool() == window.arrowTool) {
			if (clickCount > 1 && window.ibeamTool != null) {
				window.setCurrentTool(window.ibeamTool);
				window.getPalette().setCurrentTool(window.ibeamTool);
				((TableTool) window.getCurrentTool()).cellTouched(column, -1, regionInCellH, regionInCellV, modifiers);
			}
			else
				super.columnNameTouched(column, regionInCellH, regionInCellV, modifiers, clickCount);
		}
		else {
			((TableTool) window.getCurrentTool()).cellTouched(column, -1, regionInCellH, regionInCellV, modifiers);
		}
		broadcastFocusInCell(column, -1);
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellTouch(column, -1);
	}

	/* ............................................................................................................... */
	public void subRowTouched(int subRow, int column, int regionInCellH, int regionInCellV, int x, int y, int modifiers) {
		if (!columnLegal(column))
			return;
		if (column >= 0) {
			DataColumnNamesAssistant assistant = window.getDataColumnNamesAssistant(subRow);
			if (assistant != null){
				assistant.setColumnTouched(column);
				if (((TableTool) window.getCurrentTool()).getSpecialToolForColumnNamesInfoStrips())
					((TableTool) window.getCurrentTool()).cellTouched(column, subRow, regionInCellH, regionInCellV, modifiers);
				else
					assistant.showPopUp(columnNames, x + 5, y + 5);
			}
		}
	}

	/* ............................................................................................................... */
	public void columnTouched(boolean isArrowEquivalent, int column, int regionInCellH, int regionInCellV, int modifiers) {
		if (((TableTool) window.getCurrentTool()).useTableTouchRules() || isArrowEquivalent) {
			super.columnTouched(isArrowEquivalent, column, regionInCellH, regionInCellV, modifiers);
		}
		else {
			((TableTool) window.getCurrentTool()).cellTouched(column, -1, regionInCellH, regionInCellV, modifiers);
		}
		if (window.matrixInfoPanel != null && window.infoPanelOn.getValue())
			window.matrixInfoPanel.cellTouch(column, -1);
	}

	/* ............................................................................................................... */
	public void moveOneColumn(int column, int after) {
		
		int width = getColumnWidth(column);
		if (column < after) {
			for (int i = column; i < after; i++) {
				setColumnWidth(i, getColumnWidth(i + 1));
				if (isColumnSelected(i + 1))
					selectColumn(i);
				else
					deselectColumn(i);
			}
			setColumnWidth(after, width);
		}
		else if (column > after) {
			for (int i = column; i > after + 1; i--)
				setColumnWidth(i, getColumnWidth(i - 1));
			setColumnWidth(after + 1, width);
		}

		data.moveParts(column, 1, after);// TODO: not correct! need to adjust and see what new "after" is
		data.moveInLinked(column, 1, after, false);

	}

	/* ............................................................................................................... */
	public void moveColumns(int starting, int num, int justAfter) {
		UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_MOVED, data);
		undoInstructions.recordPreviousOrder(data);
		UndoReference undoReference = new UndoReference(undoInstructions, editorModule);
		// todo: check for illegal move
		if (justAfter < starting) { // moving columns left, closer to zero
			for (int i = starting; i < starting + num - 1; i++)
				moveOneColumn(i, justAfter++);// after each move, starting is now one column on
		}
		else {// moving columns right
			for (int i = 0; i < num; i++)
				moveOneColumn(starting, justAfter); // after each move, starting is now column moved up, and justafter has moved up
		}

		synchronizeColumnSelection(data);
		data.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));
		data.notifyInLinked(new Notification(MesquiteListener.PARTS_MOVED));

		editorModule.getModuleWindow().contentsChanged();
	}

	/* ............................................................................................................... */
	public void moveOneRow(int row, int after) {
		taxa.moveParts(row, 1, after);
	}

	/* ............................................................................................................ */
	/**
	 * Called if row was dragged and dropped. (after = -1 if dropped above first row; -2 if below last.)
	 */
	public void selectedRowsDropped(int after) {
		if (after < -1)
			return;
		Bits sel = getRowsSelected();
		if (after > getNumRows())
			after = getNumRows();
		if (sel.numBitsOn() == 1 && sel.firstBitOn() == after) {
			return;
		}
		UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_MOVED, taxa);
		undoInstructions.recordPreviousOrder(taxa);
		UndoReference undoReference = new UndoReference(undoInstructions, editorModule);
		int i = 0;
		while (i < getNumRows()) {
			if (sel.isBitOn(i)) {
				deselectRow(i);
				sel.clearBit(i);
				moveOneRow(i, after);
				sel.moveParts(i, 1, after);
				if (i > after)
					after++;
				i = 0;
			}
			else
				i++;
		}
		synchronizeRowSelection(data);
		taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));

		editorModule.getModuleWindow().contentsChanged();
	}

	/* ............................................................................................................... */
	public void selectedColumnsDropped(int after) {
		if (after < -1)
			return;
		if (after > getNumColumns())
			after = getNumColumns();
		int i = 0;
		Bits sel = getColumnsSelected();
		if (sel.numBitsOn() == 1 && sel.firstBitOn() == after) {
			return;
		}
		boolean asked = false;
		long[] fullChecksumBefore = data.getIDOrderedFullChecksum();
		UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_MOVED, data);
		undoInstructions.recordPreviousOrder(data);
		UndoReference undoReference = new UndoReference(undoInstructions, editorModule);
		while (i < getNumColumns()) {
			if (sel.isBitOn(i)) {
				if (!asked && data.isMolecularSequence() && i != after) {
					if (!AlertDialog.query(window, "Move?", "These are molecular sequences.  Are you sure you want to move the sites to a different location?", "Move", "Cancel"))
						return;
					asked = true;
				}
				deselectColumn(i);
				sel.clearBit(i);
				moveOneColumn(i, after);
				sel.moveParts(i, 1, after);
				if (i > after)
					after++;
				i = 0;
			}
			else
				i++;
		}
		long[] fullChecksumAfter = data.getIDOrderedFullChecksum();
		data.compareChecksums(fullChecksumBefore, fullChecksumAfter, true, "character moving");

		synchronizeColumnSelection(data);
		data.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));
		data.notifyInLinked(new Notification(MesquiteListener.PARTS_MOVED));

		editorModule.getModuleWindow().contentsChanged();
	}

	/* ............................................................................................................... */
	boolean doAutosize = false;

	boolean suppressAutosize = false;

	/* ................................................................................................................. */
	protected void clearIt(boolean cut) {
		notifySuppressed = true;
		boolean namesChanged = false;
		boolean changed = false;
		CharacterState cs = data.makeCharacterState(); // so as to get the default state
		UndoInstructions undoInstructions;
		if (anyCellSelected())
			undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
		else
			undoInstructions = new UndoInstructions(UndoInstructions.ALLCHARACTERNAMES, data, data);

		UndoReference undoReference = null;
		if (undoInstructions != null)
			undoReference = new UndoReference(undoInstructions, editorModule);

		for (int i = 0; i < numColumnsTotal; i++) {
			if (isColumnNameSelected(i) || isColumnSelected(i)) {
				returnedColumnNameText(i, "");
				namesChanged = true;
			}
		}
		for (int j = 0; j < numRowsTotal; j++) {
			/*
			 * if (isRowNameSelected(j) || isRowSelected(j)) { returnedRowNameText(j,""); }
			 */
			for (int i = 0; i < numColumnsTotal; i++) {
				if (isCellSelected(i, j) || isRowSelected(j) || isColumnSelected(i)) {
					data.setState(i, j, cs);
					// returnedMatrixText(i,j,"?");
					changed = true;
				}
			}
		}
		if (cut) {
			deleteSelected();
		}
		notifySuppressed = false;
		if (undoInstructions != null) {
			undoInstructions.setNewData(data);
			if (undoReference != null) {
				undoReference.setUndoer(undoInstructions);
			}
		}
		if (changed)
			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, null, undoReference));
		else if (namesChanged)
			data.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED, null, undoReference));
	}

	private void deleteSelected() {
		if (data != null) {
			if (anyColumnSelected()) {
				if (!MesquiteThread.isScripting() && !AlertDialog.query(window, "Delete characters?", "The data for the selected characters have been cleared.  Do you also want to delete the selected characters?", "Yes", "No"))
					return;
				Vector blocks = new Vector();
				while (anyColumnSelected()) {
					int lastOfBlock = lastColumnSelected();
					int firstOfBlock = startOfLastColumnBlockSelected();
					if (lastOfBlock >= 0) {
						for (int i = firstOfBlock; i <= lastOfBlock; i++)
							deselectColumn(i);
						data.deleteParts(firstOfBlock, lastOfBlock - firstOfBlock + 1);
						data.deleteInLinked(firstOfBlock, lastOfBlock - firstOfBlock + 1, false);
						blocks.addElement(new int[] { firstOfBlock, lastOfBlock - firstOfBlock + 1 }); // do as series of contiguous blocks
					}
				}

				if (blocks.size() == 1) {
					data.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED, (int[]) blocks.elementAt(0))); // do as series of contiguous blocks
					data.notifyInLinked(new Notification(MesquiteListener.PARTS_DELETED, (int[]) blocks.elementAt(0))); // do as series of contiguous blocks
				}
				else {
					data.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED)); // do as series of contiguous blocks
					data.notifyInLinked(new Notification(MesquiteListener.PARTS_DELETED)); // do as series of contiguous blocks
				}
			}
			if (anyRowSelected()) {
				Bits rows = getRowsSelected();
				if (rows.numBitsOn() == data.getTaxa().getNumTaxa()) {
					window.ownerModule.discreetAlert("You cannot delete all of the taxa; the data will be cleared but the taxa will remain.");
					return;
				}
				if (data.getTaxa().isEditInhibited()){
					window.ownerModule.discreetAlert("You cannot delete taxa; the taxa block is locked.");
					return;
				}
				if (!MesquiteThread.isScripting() && !AlertDialog.query(window, "Delete taxa?", "The data for the selected taxa have been cleared.  Do you also want to delete the selected taxa?", "Yes", "No"))
					return;

				for (int i = getNumRows() - 1; i >= 0; i--) {
					if (rows.isBitOn(i)) {
						data.getTaxa().deleteTaxa(i, 1, false);
					}
				}
				Notification nn;

				data.getTaxa().notifyListeners(this, nn = new Notification(MesquiteListener.PARTS_DELETED));// do as series of contiguous blocks
				data.notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED).setNotificationNumber(nn.getNotificationNumber()));// do as series of contiguous blocks
			}
		}
	}

	boolean turnOffWarnings = false;

	/* ............................................................................................................... */
	public void returnedMatrixText(int column, int row, String s) { // TODO: SHOULD CHECK FOR ERRORS
		if (window == null || window.ownerModule == null || window.ownerModule.isDoomed() || StringUtil.blank(s))
			return;

		CharacterState csBefore = data.getCharacterState(null, column, row);
		parser.setString(s);
		MesquiteString result = new MesquiteString("");
		int response = data.setState(column, row, parser, true, result); // receive errors?
		if (response == CharacterData.OK) {
			CharacterState csAfter = data.getCharacterState(null, column, row);
			if (csBefore != null && !csBefore.equals(csAfter) && !notifySuppressed) {
				int[] subcodes = new int[] { MesquiteListener.SINGLE_CELL };
				if (csBefore.isInapplicable() == csAfter.isInapplicable())
					subcodes = new int[] { MesquiteListener.SINGLE_CELL, MesquiteListener.CELL_SUBSTITUTION };
				UndoInstructions undoInstructions = window.setUndoInstructions(UndoInstructions.SINGLEDATACELL, column, row, csBefore, csAfter);
				Notification notification = new Notification(MesquiteListener.DATA_CHANGED, new int[] { column, row }, new UndoReference(undoInstructions, window.ownerModule));
				notification.setSubcodes(subcodes);
				data.notifyListeners(this, notification);
				doAutosize = true;
			}
		}
		else if (!result.isBlank()) {
			String ws = "Illegal entry for character " + (column + 1) + " in taxon " + (row + 1) + ": " + result.toString();
			if (turnOffWarnings || MesquiteThread.isScripting())
				window.ownerModule.logln(ws);
			else
				turnOffWarnings = !AlertDialog.query(window, "Illegal character state", ws, "OK", "Don't warn again");
		}
	}

	/* ............................................................................................................... */
	public void returnedRowNameText(int row, String s) {
		if (data == null || taxa == null || window == null)
			return;
		String oldTaxonName = taxa.getTaxonName(row);
		if (s != null && !s.equals(oldTaxonName)) {
			String warning = taxa.checkNameLegality(row, s);
			if (warning == null) {
				taxa.setTaxonName(row, s);
				taxNC = true; // for pasting, to discover if taxon names were changed (see pasteIt)
				window.setUndoer(window.setUndoInstructions(UndoInstructions.SINGLETAXONNAME, -1, row, new MesquiteString(oldTaxonName), new MesquiteString(s)));
			}
			else if (window.ownerModule != null){
				boolean giveWarning = BasicDataWindowMaker.getWarnAgainAboutTaxonNameDuplication();
				if (giveWarning)
					if (MesquiteThread.isScripting())
						window.ownerModule.logln(warning);
					else {
						giveWarning = AlertDialog.query(window, "Duplicate taxon name", warning, "OK", "Don't warn again");
						BasicDataWindowMaker.setWarnAgainAboutTaxonNameDuplication(giveWarning);
					}
			}
		}
	}

	/* ............................................................................................................... */
	public void returnedColumnNameText(int column, String s) {
		if (data == null || taxa == null || window == null)
			return;
		String oldCharacterName = data.getCharacterName(column);
		if (s != null && !s.equals(oldCharacterName)) {
			String warning = data.checkNameLegality(column, s);
			if (warning == null) {
				data.setCharacterName(column, s);
				window.setUndoer(window.setUndoInstructions(UndoInstructions.SINGLECHARACTERNAME, column, -1, new MesquiteString(oldCharacterName), new MesquiteString(s)));
			}
			else
				window.ownerModule.discreetAlert(warning);
		}
	}

	/* ............................................................................................................... */
	public int getMinColumnWidth() {
		return 3;
	}

	public void repaintAll() {
		if (MesquiteWindow.checkDoomed(this))
			return;
		displayModifications = checkDisplayModifications();
		if (data != null) {
			String s = data.checkIntegrity();
			if (s != null)
				MesquiteTrunk.mesquiteTrunk.alert(s + " (" + data + ")");
		}
		MesquiteWindow.uncheckDoomed(this);
		super.repaintAll();
	}

	/* ............................................................................................................... */
	public int getNarrowDefaultColumnWidth() {
		return data.getNarrowDefaultColumnWidth();
	}

	/* ............................................................................................................... */
	public int getNarrowDefaultColumnWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth("G");
		return width + 2;
	}

	/* ............................................................................................................... */
	public int getDefaultColumnWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth("G");
		return width + 2 + MesquiteModule.textEdgeCompensationHeight;
	}

	/* ............................................................................................................... */
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		displayModifications = checkDisplayModifications();
		Font gF = g.getFont();
		boolean resized = false;
		if (oldFont == null || !gF.equals(oldFont)) {
			if ((data.pleaseAutoSizeColumns()) && !showBirdsEyeView.getValue() && allowAutosize.getValue()) // TODO:
				resized = autoSizeColumns(g);
			else {
				if (showBirdsEyeView.getValue())
					setColumnWidthsUniform(birdsEyeWidth);
				else if (tight.getValue())
					setColumnWidthsUniform(getNarrowDefaultColumnWidth(g));
				else if (!data.pleaseAutoSizeColumns())
					setColumnWidthsUniform(getDefaultColumnWidth(g));

				autoSizeRows(g);
			}
			oldFont = g.getFont();
			boldFont = new Font(oldFont.getName(), Font.BOLD, oldFont.getSize());
		}
		else if (doAutosize && !suppressAutosize && allowAutosize.getValue()) {
			doAutosize = false;
			resized = autoSizeColumns(g);
		}
		else {
			if (showBirdsEyeView.getValue())
				setColumnWidthsUniform(birdsEyeWidth);
			else if (tight.getValue())
				setColumnWidthsUniform(getNarrowDefaultColumnWidth(g));
			else if (!data.pleaseAutoSizeColumns())
				setColumnWidthsUniform(getDefaultColumnWidth(g));
			autoSizeRows(g);
		}

		if (resized) {
			MesquiteWindow.uncheckDoomed(this);
			resetComponentSizes();
			repaintAll();
		}
		else {
			super.paint(g);
			MesquiteWindow.uncheckDoomed(this);
		}
	}
}

/* =========================================== */
/* =========================================== */

class BasicMatrixStatisticsPanel extends MatrixInfoExtraPanel {
	StringInABox statsBox;

	String matrixStats = null;

	int neededHeight = 20;

	public BasicMatrixStatisticsPanel(ClosablePanelContainer container) {
		super(container, "Basic Matrix Stats");
		statsBox = new StringInABox("", null, 50);
		setOpen(true);
	}

	public void setMatrixAndTable(CharacterData data, MesquiteTable table) {
		super.setMatrixAndTable(data, table);
		adjustMessage();
		container.requestHeightChange(this);
		repaint();
	}

	public void setCell(int ic, int it) {
		super.setCell(ic, it);
		repaint();
	}

	void adjustMessage() {
		if (data == null)
			matrixStats = "no matrix";
		else {
			matrixStats = "Characters: " + data.getNumChars();
		}
		/*
		 * int countCell = 0; int countTaxa = 0; int countChar = 0; for (int ic = 0; ic< data.getNumChars(); ic++){ if (!StringUtil.blank(data.getAnnotation(ic))) countChar++; for (int it = 0; it< data.getNumTaxa(); it++){ if (!StringUtil.blank(data.getAnnotation(ic, it))) countCell++; } } for (int it = 0; it< data.getNumTaxa(); it++) if (!StringUtil.blank(data.getTaxa().getAnnotation(it))) countTaxa++;
		 */
	}

	public int getRequestedHeight(int width) {
		if (!isOpen())
			return MINHEIGHT;
		statsBox.setFont(getFont());
		statsBox.setString(matrixStats);
		statsBox.setWidth(width - 4);
		neededHeight = statsBox.getHeight();
		return neededHeight + MINHEIGHT;
	}

	public void paint(Graphics g) {
		super.paint(g);
		// g.drawString("hello", 8, MINHEIGHT+20);
		statsBox.setWidth(getBounds().width - 4);
		statsBox.setFont(g.getFont());
		statsBox.setString(matrixStats);
		statsBox.draw(g, 4, MINHEIGHT);
	}
}

/* =========================================== */
class FootnotePanel extends MatrixInfoExtraPanel {
	String message = null;

	NoteField infoArea;

	public FootnotePanel(ClosablePanelContainer container) {
		super(container, "Footnote");
		infoArea = new NoteField(this);
		currentHeight = 40 + MINHEIGHT;
		setLayout(null);
		add(infoArea);
		resetLocs();
		setOpen(true);
	}

	public boolean userExpandable() {
		return true;
	}

	public void setMatrixAndTable(CharacterData data, MesquiteTable table) {
		super.setMatrixAndTable(data, table);
		container.requestHeightChange(this);
		repaint();
	}

	public void setCell(int ic, int it) {
		if (!isVisible())
			return;
		super.setCell(ic, it);
		adjustMessage();
		container.requestHeightChange(this);
		repaint();
	}

	private void adjustMessage() {
		if (data == null) {
			setTitle("Footnote (nothing selected)");
			infoArea.setText("", ic, it);
			infoArea.setEditable(false);
			infoArea.setBackground(ColorDistribution.veryLightGray);
		}
		else if (it < 0 && ic < 0) {
			setTitle("Footnote (nothing selected)");
			infoArea.setText("", ic, it);
			infoArea.setEditable(false);
			infoArea.setBackground(ColorDistribution.veryLightGray);
		}
		else {
			infoArea.setEditable(true);
			infoArea.setBackground(Color.white);
			message = "";
			if (ic < 0) {
				setTitle("Footnote (taxon: " + data.getTaxa().getTaxonName(it) + ")");
				if (data.getTaxa() == null)
					infoArea.setText("", ic, it);
				else
					infoArea.setText(data.getTaxa().getAnnotation(it), ic, it);
			}
			else if (it < 0) {
				if (data.characterHasName(ic))
					setTitle("Footnote (character: " + data.getCharacterName(ic) + ")");
				else
					setTitle("Footnote (character " + (ic + 1) + ")");
				infoArea.setText(data.getAnnotation(ic), ic, it);
			}
			else {
				setTitle("Footnote (character " + (ic + 1) + ", taxon " + data.getTaxa().getTaxonName(it) + ")");
				infoArea.setText(data.getAnnotation(ic, it), ic, it);
			}
		}
	}

	public void enterText(NoteField n, String text, int rIC, int rIT, boolean warn) {
		if (data == null)
			return;
		else if (it < 0 && ic < 0) {
			if (warn)
				n.setText("A taxon, character or cell must be touched or selected first before editing a footnote");
			return;
		}
		else {
			if (rIC != ic || rIT != it) {
				MesquiteMessage.warnProgrammer("rIC/ic or rIT/it mismatch in footnotespanel");
				return;
			}
			if (ic < 0) {
				data.getTaxa().setAnnotation(it, text);
				// data.getTaxa().notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
			}
			else if (it < 0) {
				data.setAnnotation(ic, text);
				// data.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
			}
			else {
				data.setAnnotation(ic, it, text);
				// data.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
			}

		}
		((MatrixTable) table).refreshAnnotation();

	}

	public void setOpen(boolean open) {
		infoArea.setVisible(open);
		resetLocs();
		super.setOpen(open);
	}

	void resetLocs() {
		infoArea.setBounds(2, MINHEIGHT + 4, getWidth() - 4, currentHeight - MINHEIGHT - 4);
	}

	public void setSize(int w, int h) {
		super.setSize(w, h);
		resetLocs();
	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		resetLocs();
	}

	public void cellEnter(int ic, int it) {
		// if a single column or cells within a single column are selected, then cut out
		if (table.singleTableCellSelected() || table.editingAnything())
			return;
		super.cellEnter(ic, it);
	}

	public void cellExit(int ic, int it) {
		if (table.singleTableCellSelected() || table.editingAnything())
			return;
		super.cellExit(ic, it);

	}

}

class NoteField extends TextArea implements FocusListener {
	FootnotePanel panel;

	boolean somethingTyped;

	int ic = -1;

	int it = -1;

	public NoteField(FootnotePanel panel) {
		super("", 4, 40, TextArea.SCROLLBARS_NONE);
		this.panel = panel;
		setText("");
		addKeyListener(new KListener(this));
		addFocusListener(this);
	}

	public void setText(String s, int ic, int it) {
		setText(s);
		this.ic = ic;
		this.it = it;
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {

		if (somethingTyped)
			panel.enterText(this, getText(), ic, it, false);
		somethingTyped = false;
	}

	class KListener extends KeyAdapter {
		NoteField nf = null;

		public KListener(NoteField nf) {
			super();
			this.nf = nf;
		}

		public void keyPressed(KeyEvent e) {
			// Event queue
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				if (somethingTyped) {
					String s = getText();
					panel.enterText(nf, s, ic, it, true);
					somethingTyped = false;
					e.consume();
					setSelectionStart(s.length());
					setSelectionEnd(s.length());
				}
			}
			else {
				somethingTyped = true;
			}

		}
	}

}

/* =========================================== */
class CellInfoPanel extends MatrixInfoExtraPanel {
	StringInABox statsBox;

	String message = null;

	int neededHeight = 20;

	String attachmentsMessage = "";

	Image query;

	public CellInfoPanel(ClosablePanelContainer container) {
		super(container, "Cell Info");
		query = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "queryGray.gif");
		statsBox = new StringInABox("", null, 50);
		setOpen(true);
	}

	public void setMatrixAndTable(CharacterData data, MesquiteTable table) {
		super.setMatrixAndTable(data, table);
		adjustMessage();
		container.requestHeightChange(this);
		repaint();
	}

	public void setCell(int ic, int it) {
		if (!isVisible())
			return;
		super.setCell(ic, it);
		try {
			adjustMessage();
			container.requestHeightChange(this);
			repaint();
		} catch (Exception e) {
		}
	}

	private void adjustMessage() {

		attachmentsMessage = "";
		if (data == null)
			message = "no matrix";
		else if (it < 0 && ic < 0)
			message = " \n \n";
		else {
			boolean hasAnnot = table.isAttachedNoteAvailable(ic, it);
			if (it < 0) {
				message = "Character ";
				if (data.characterHasName(ic))
					message += data.getCharacterName(ic) + " (" + (ic + 1) + ")";
				else
					message += (ic + 1);
				message += "\n ";
				if (hasAnnot)
					message += "NOTE: Character has annotations\n";
				// character info
				if (ic < data.getNumChars()) {
					for (int i = 0; i < data.getNumberAssociatedDoubles(); i++) {
						DoubleArray d = data.getAssociatedDoubles(i);
						NameReference nr = d.getNameReference();
						attachmentsMessage += nr.getValue();
						attachmentsMessage += ": " + MesquiteDouble.toString(d.getValue(ic));
						attachmentsMessage += "\n";
					}
					for (int i = 0; i < data.getNumberAssociatedLongs(); i++) {
						LongArray d = data.getAssociatedLongs(i);
						NameReference nr = d.getNameReference();
						attachmentsMessage += nr.getValue();
						attachmentsMessage += ": " + MesquiteLong.toString(d.getValue(ic));
						attachmentsMessage += "\n";
					}
				}
			}
			else if (ic < 0) {
				// taxon info
				Taxa taxa = data.getTaxa();
				message = " \nTaxon: ";
				message += taxa.getTaxonName(it) + " (" + (it + 1) + ")\n";
				if (hasAnnot)
					message += "NOTE: Taxon has annotations\n";
				if (it < taxa.getNumTaxa()) {
					for (int i = 0; i < taxa.getNumberAssociatedDoubles(); i++) {
						DoubleArray d = taxa.getAssociatedDoubles(i);
						NameReference nr = d.getNameReference();
						attachmentsMessage += nr.getValue();
						attachmentsMessage += ": " + MesquiteDouble.toString(d.getValue(ic));
						attachmentsMessage += "\n";
					}
					for (int i = 0; i < taxa.getNumberAssociatedLongs(); i++) {
						LongArray d = taxa.getAssociatedLongs(i);
						NameReference nr = d.getNameReference();
						attachmentsMessage += nr.getValue();
						attachmentsMessage += ": " + MesquiteLong.toString(d.getValue(ic));
						attachmentsMessage += "\n";
					}
				}
				Associable tInfo = data.getTaxaInfo(false);
				if (tInfo != null) {
					attachmentsMessage += tInfo.toString(it) + "\n";
				}
			}
			else {
				Taxa taxa = data.getTaxa();
				message = "Character ";
				if (data.characterHasName(ic))
					message += data.getCharacterName(ic) + " (" + (ic + 1) + ")";
				else
					message += (ic + 1);
				message += "\nTaxon: ";
				message += taxa.getTaxonName(it) + " (" + (it + 1) + ")\n";
				if (hasAnnot)
					message += "NOTE: Cell has annotations\n";
			}
			if (!StringUtil.blank(attachmentsMessage))
				message += "\n" + attachmentsMessage;
		}
	}

	public int getRequestedHeight(int width) {
		if (!isOpen())
			return MINHEIGHT;
		statsBox.setFont(getFont());
		statsBox.setString(message);
		statsBox.setWidth(width - 4);
		neededHeight = statsBox.getHeight();
		return neededHeight + MINHEIGHT;
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(query, getWidth() - 20, 4, this);
		statsBox.setWidth(getBounds().width - 4);
		statsBox.setFont(g.getFont());
		if (!StringUtil.blank(message)) {
			statsBox.setString(message);
			statsBox.draw(g, 4, MINHEIGHT);
		}
	}

	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (y < MINHEIGHT && (x > getWidth() - 20)) {
			MesquiteTrunk.mesquiteTrunk.alert("Attachments:  Attachments to the matrix or its cells are pieces of information like annotations, assigned colors, and so on. Move cursor over cells to see information attached there."); // query button hit
		}
		else
			super.mouseUp(modifiers, x, y, tool);
	}
}

/* =========================================== */

/* ======================================================================== */
class MatrixInfoPanel extends MousePanel implements ClosablePanelContainer {
	static final int width = 200;

	// static final int height = 66;
	String title = null;

	String explanation = null;

	StringInABox titleBox, explanationBox;

	int[] heights;

	BasicDataWindow w;

	Image goaway;

	CharacterData data;

	Vector extras = new Vector();

	Font titleFont;

	String matrixName, sourceName;

	Image add = null;

	BasicMatrixStatisticsPanel btsp;

	CellInfoPanel ap;

	FootnotePanel fp;

	public MatrixInfoPanel(BasicDataWindow w) {
		super();
		this.w = w;
		add = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "addGray.gif");
		setLayout(null);
		addExtraPanel(btsp = new BasicMatrixStatisticsPanel(this));
		setBackground(ColorDistribution.veryLightGray);
		setFont(new Font("SansSerif", Font.PLAIN, 10));
		titleFont = new Font("SansSerif", Font.BOLD, 10);
		titleBox = new StringInABox("", null, width);
		explanationBox = new StringInABox("", null, width);
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "minimizeTransparent.gif");
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("btspOpen " + btsp.isOpen());
		temp.addLine("apOpen " + ap.isOpen());
		temp.addLine("fpOpen " + fp.isOpen());
		return temp;
	}

	public void employeeQuit(MesquiteModule m) {
		if (m == null)
			return;
		// zap values panel line

	}

	public void startCellInfo() {
		addExtraPanel(fp = new FootnotePanel(this));
		addExtraPanel(ap = new CellInfoPanel(this));
	}

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets attachment panel open", null, commandName, "apOpen")) {
			if (ap != null)
				ap.setOpen(arguments == null || arguments.equalsIgnoreCase("true"));
		}
		else if (checker.compare(this.getClass(), "Sets the basic statistics panel open", null, commandName, "btspOpen")) {
			btsp.setOpen(arguments == null || arguments.equalsIgnoreCase("true"));
		}
		else if (checker.compare(this.getClass(), "Sets the footnotes panel open", null, commandName, "fpOpen")) {
			fp.setOpen(arguments == null || arguments.equalsIgnoreCase("true"));
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	public ClosablePanel getPrecedingPanel(ClosablePanel panel) {
		int i = extras.indexOf(panel);
		if (i > 0)
			return (ClosablePanel) extras.elementAt(i - 1);
		return null;
	}

	void addExtraPanel(MatrixInfoExtraPanel p) {
		extras.addElement(p);
		add(p);
		heights = new int[extras.size()];
		resetSizes(getWidth(), getHeight());
		p.setVisible(true);
		p.setMatrixAndTable(data, w.table);
	}

	void setMatrix(CharacterData data) {
		this.data = data;
		title = "Matrix: " + data.getName();
		for (int i = 0; i < extras.size(); i++) {
			MatrixInfoExtraPanel panel = ((MatrixInfoExtraPanel) extras.elementAt(i));
			panel.setMatrixAndTable(data, w.table);
		}
	}

	public void cellEnter(int ic, int it) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		for (int i = 0; i < extras.size(); i++) {
			MatrixInfoExtraPanel panel = ((MatrixInfoExtraPanel) extras.elementAt(i));
			panel.cellEnter(ic, it);
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void cellExit(int ic, int it) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		for (int i = 0; i < extras.size(); i++) {
			MatrixInfoExtraPanel panel = ((MatrixInfoExtraPanel) extras.elementAt(i));
			panel.cellExit(ic, it);
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void cellTouch(int ic, int it) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		for (int i = 0; i < extras.size(); i++) {
			MatrixInfoExtraPanel panel = ((MatrixInfoExtraPanel) extras.elementAt(i));
			panel.cellTouch(ic, it);
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void requestHeightChange(ClosablePanel panel) {
		int i = extras.indexOf(panel);
		if (i >= 0 && heights[i] == panel.getRequestedHeight(getWidth()))
			return;
		resetSizes(getWidth(), getHeight());
		repaint();
	}

	public void paint(Graphics g) {
		Color c = g.getColor();

		int vertical = 2;
		if (title != null) {
			g.setColor(ColorTheme.getInterfaceElement());
			g.fillRect(0, 0, getBounds().width, titleBox.getHeight() + 8);
			g.setColor(ColorTheme.getInterfaceTextContrast());
			titleBox.draw(g, 20, vertical);
			g.setColor(Color.black);
			vertical += 8 + titleBox.getHeight();
		}

		g.drawImage(goaway, 2, 2, this);
		g.drawLine(0, vertical - 4, getBounds().width, vertical - 4);
		if (explanation != null) {
			g.setColor(Color.white);
			g.fillRect(0, vertical, getBounds().width, 8 + explanationBox.getHeight());
			g.setColor(Color.black);
			explanationBox.draw(g, 4, vertical);
			vertical += 8 + explanationBox.getHeight();
		}
		g.setColor(Color.darkGray);
		g.fillRect(0, totalVertical, getBounds().width, 2);

		// WHEN CALCULATIONS CAN BE ADDED
		// g.drawImage(add, 2, totalVertical+4, this);
		g.setColor(c);

	}

	int totalVertical = 0;

	/* to be used by subclasses to tell that panel touched */
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (x < 30 && y > totalVertical + 4 && y < totalVertical + 20) {
			// WHEN CALCULATIONS CAN BE ADDED
			// MesquiteTrunk.mesquiteTrunk.alert("Sorry, doesn't do anything yet");
		}
	}

	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (x < 16 && y < 16)
			w.matrixInfoPanelGoAway();

	}

	void resetSizes(int w, int h) {
		int vertical = 2;
		if (title != null) {
			titleBox.setWidth(w - 20);
			titleBox.setFont(titleFont);
			titleBox.setString(title);
			vertical += 8 + titleBox.getHeight();
		}
		if (explanation != null) {
			explanationBox.setWidth(w - 4);
			explanationBox.setFont(getFont());
			explanationBox.setString(explanation);
			vertical += 8 + explanationBox.getHeight();
		}
		for (int i = 0; i < extras.size(); i++) {
			ClosablePanel panel = ((ClosablePanel) extras.elementAt(i));
			int requestedlHeight = panel.getRequestedHeight(w);
			heights[i] = requestedlHeight;
			panel.setBounds(0, vertical, w, requestedlHeight);
			vertical += requestedlHeight;
		}
		totalVertical = vertical;
	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		resetSizes(w, h);
	}

	public void setSize(int w, int h) {
		super.setSize(w, h);
		resetSizes(w, h);
	}
}

/* ============================================= */
class SequenceLedge extends MousePanel {
	MatrixTable table;

	int focusIC;

	int focusIT;

	int numRowsAboveBelow = 0;

	public SequenceLedge() {
	}

	public void setFocus(MatrixTable table, int ic, int it) {
		focusIC = ic;
		focusIT = it;
		this.table = table;
		repaint();
		setBackground(Color.lightGray);
	}

	public void paint(Graphics g) {
		if (table == null || focusIT < 0 || focusIT >= table.getNumRows())
			return;
		g.setColor(Color.black);
		int cellWidth = 16;
		int centrePoint = getBounds().width / 2;
		int defHeight = table.getRowHeight(0); // bit kudgy
		if (defHeight < 16)
			defHeight = 16;
		int numRows = getBounds().height / defHeight;
		if (numRows / 2 * 2 == numRows) // even
			numRows--;
		numRowsAboveBelow = numRows / 2;
		int rowHeight = getBounds().height / (numRowsAboveBelow * 2 + 1) - 2;
		int height = getBounds().height;
		// int centrePoint = table.getLeftOfColumn(focusIC) - 10;
		// g.fillRect( centrePoint-2, 0, cellWidth + 8, height);

		for (int it = focusIT - numRowsAboveBelow; it <= focusIT + numRowsAboveBelow; it++) {
			if (it >= 0 && it < table.getNumRows()) {
				int rowTop = (it - (focusIT - numRowsAboveBelow)) * rowHeight + 3;
				for (int ic = focusIC; ic < table.getNumColumns() && (ic - focusIC) * (cellWidth + 1) < centrePoint; ic++)
					table.drawMatrixCell(g, (ic - focusIC) * (cellWidth + 1) + centrePoint, rowTop, cellWidth, rowHeight - 2, ic, it, false, true, false);

				for (int ic = focusIC - 1; ic >= 0 && (focusIC - ic - 2) * (cellWidth + 1) < centrePoint; ic--)
					table.drawMatrixCell(g, (ic - focusIC) * (cellWidth + 1) + centrePoint, rowTop, cellWidth, rowHeight - 2, ic, it, false, true, false);
				table.drawMatrixCell(g, centrePoint, rowTop, cellWidth, rowHeight - 2, focusIC, it, false, true, true);
				if (it == focusIT) {
					g.setColor(Color.black);
					g.drawRect(centrePoint - 1, rowTop - 1, cellWidth + 2, rowHeight);
					g.drawRect(centrePoint, rowTop, cellWidth, rowHeight - 2);
				}
			}
		}
	}
}


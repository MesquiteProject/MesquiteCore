/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ancstates.TraceAllChanges;
/*~~  */

import java.applet.*;

import java.util.*;
import java.awt.*;
import mesquite.categ.lib.CategoricalHistory;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.MCategoricalStates;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDecorator;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayDrawnExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayLateExtra;
import mesquite.lib.tree.TreeDisplayLegend;
import mesquite.lib.tree.TreeDisplayLegendSimpleText;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquiteColorTable;
import mesquite.lib.ui.MesquiteFrame;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.StringInABox;
import mesquite.lib.ui.TextRotator;

/*======================================================================== */
public class TraceAllChanges extends TreeDisplayAssistantA {
	public void getEmployeeNeeds() { // This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharStatesForNodes.class, getName() + " needs a source of reconstructed ancestral states.", "The reconstruction method is chosen initially or using the Reconstruction Method submenu");
		EmployeeNeed e2 = registerEmployeeNeed(MatrixSourceCoord.class, getName() + " needs a source of matrices whose ancestral states will be reconstructed", "The source of characters is chosen initially or in the Source of Character Matrices submenu");
	}

	CharsStatesForNodes historyTask;

	// DisplayCharsStsAtNodes displayTask;
	MatrixSourceCoord characterSourceTask;
	DisplayStatesAtNodes shadeTask;

	Vector traces;

	// MesquiteString displayTaskName;
	MesquiteCommand dtC;

	MesquiteBoolean byCharacters, selectedOnly, selectedCharactersOnly, constantDistance, showTerminals, showTable, showStateNames, showCharNames, ambiguousChangesAlso, traceSingleCharacter, tickOnly;
	int shadedCharacter = -1;
	double barLength = 16;
	MesquiteMenuItemSpec chooseCharToTraceMenuItem;
	boolean suppressed = false;

	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		traces = new Vector();
		byCharacters = new MesquiteBoolean(true);
		selectedOnly = new MesquiteBoolean(true);
		selectedCharactersOnly = new MesquiteBoolean(false);
		showTerminals = new MesquiteBoolean(false);
		showTable = new MesquiteBoolean(false);
		showStateNames = new MesquiteBoolean(true);
		showCharNames = new MesquiteBoolean(true);
		ambiguousChangesAlso = new MesquiteBoolean(false); 
		constantDistance = new MesquiteBoolean(false); 
		traceSingleCharacter = new MesquiteBoolean(false); 
		tickOnly = new MesquiteBoolean(false); 
		makeMenu("Trace_All");
		characterSourceTask = (MatrixSourceCoord) hireCompatibleEmployee(MatrixSourceCoord.class, CategoricalState.class, "Source of characters (for Trace All Characters)");
		suppressed = MesquiteThread.isScripting();
		if (characterSourceTask == null) {
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		}
		MesquiteThread.addHint(new MesquiteString("#AncestralStatesAll", "#ParsAncestralStates"), this);
		historyTask = (CharsStatesForNodes) hireNamedEmployee(CharsStatesForNodes.class, "#AncestralStatesAll");
		MesquiteThread.removeMyHint(null, this);
		if (historyTask == null) {
			return sorry(getName() + " couldn't start because no character reconstructor was obtained.");
		}

		shadeTask = (DisplayStatesAtNodes)hireNamedEmployee(DisplayStatesAtNodes.class, "#ShadeStatesOnTree");


		addCheckMenuItem(null, "Selected Characters Only", makeCommand("toggleSelectedCharsOnly", this), selectedCharactersOnly);
		addMenuSeparator();
		addMenuItem("Display on Tree", null);
		addCheckMenuItem(null, "Show All Permissible Changes (including Ambiguous)", makeCommand("ambiguousChangesAlso", this), ambiguousChangesAlso);
		addCheckMenuItem(null, "Write Character Numbers", makeCommand("toggleShowCharNames", this), showCharNames);
		addCheckMenuItem(null, "Write State Numbers", makeCommand("toggleShowStateNames", this), showStateNames);
		addCheckMenuItem(null, "Narrow Lines", makeCommand("tickOnly", this), tickOnly);
		addCheckMenuItem(null, "Constant Distance Between Bars", makeCommand("constantDistance", this), constantDistance);
		addMenuItem("Bar Length...", makeCommand("setBarLength", this));
		addCheckMenuItem(null, "Trace Single Character", makeCommand("traceSingleCharacter", this), traceSingleCharacter);
		chooseCharToTraceMenuItem = addMenuItem("Choose Single Character to Trace...", makeCommand("setSingleCharacter", this));
		chooseCharToTraceMenuItem.setEnabled(traceSingleCharacter.getValue());
		addMenuSeparator();
		addMenuItem("Table of Changes", null);
		addCheckMenuItem(null, "Show Table", makeCommand("toggleShowTable", this), showTable);
		addCheckMenuItem(null, "List Selected Nodes Only", makeCommand("toggleSelectedOnly", this), selectedOnly);
		addCheckMenuItem(null, "List Terminal Nodes", makeCommand("toggleShowTerminals", this), showTerminals);
		addCheckMenuItem(null, "Table Rows are Characters", makeCommand("toggleByCharacters", this), byCharacters);
		addMenuSeparator();
		addMenuItem("Close Trace All", makeCommand("closeTrace", this));
		addMenuSeparator();
		resetContainingMenuBar();
		return true;
	}

	/* ................................................................................................................. */
	/** Generated by an employee who quit. The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == historyTask || employee == characterSourceTask)
			iQuit();
		if (employee instanceof TextDisplayer){
			showTable.setValue(false);
			resetTablesAllTraceOperators();
		}
	}


	/* ................................................................................................................. */
	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		TraceAllOperator newTrace = new TraceAllOperator(this, treeDisplay);
		traces.addElement(newTrace);
		return newTrace;
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("toggleByCharacters " + byCharacters.toOffOnString());
		temp.addLine("ambiguousChangesAlso " + ambiguousChangesAlso.toOffOnString());
		temp.addLine("traceSingleCharacter " + traceSingleCharacter.toOffOnString());
		temp.addLine("tickOnly " + tickOnly.toOffOnString());
		temp.addLine("constantDistance " + constantDistance.toOffOnString());
		temp.addLine("setSingleCharacter " + shadedCharacter);
		temp.addLine("setBarLength " + (int)barLength);
		temp.addLine("toggleSelectedOnly " + selectedOnly.toOffOnString());
		temp.addLine("toggleSelectedCharsOnly " + selectedCharactersOnly.toOffOnString());
		temp.addLine("toggleShowTerminals " + showTerminals.toOffOnString());
		temp.addLine("toggleShowTable " + showTable.toOffOnString());
		temp.addLine("toggleShowCharNames " + showCharNames.toOffOnString());
		temp.addLine("toggleShowStateNames " + showStateNames.toOffOnString());
		temp.addLine("getCharSource", characterSourceTask);
		temp.addLine("getReconstructor", historyTask);
		temp.addLine("desuppress");
		// temp.addLine("setDisplayMode ",displayTask);
		return temp;
	}

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		/*
		 * if (checker.compare(this.getClass(), "Sets module used to display reconstructed states", "[name of module]", commandName, "setDisplayMode")) { DisplayCharsStsAtNodes temp= (DisplayCharsStsAtNodes)replaceEmployee(DisplayCharsStsAtNodes.class, arguments, "Display mode", displayTask); if (temp != null) { displayTask= temp; displayTask.setHiringCommand(dtC); displayTaskName.setValue(displayTask.getName()); resetAllTraceOperators(); parametersChanged(); return displayTask; } }
		 */
		if (checker.compare(this.getClass(), "Undoes suppression of calculations when scripting", null, commandName, "desuppress")) {
			suppressed = false;
			recalcAllTraceOperators();
			resetAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show states only for selected nodes", "[on or off]", commandName, "toggleSelectedOnly")) {
			selectedOnly.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting())
				resetAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show reconstructed ancestral states of a single character", "[on or off]", commandName, "traceSingleCharacter")) {
			traceSingleCharacter.toggleValue(parser.getFirstToken(arguments));
			toggleSingleTrace(traceSingleCharacter.getValue());
		}
		else if (checker.compare(this.getClass(), "Sets whether to show reconstructed ancestral states of a single character", "[on or off]", commandName, "tickOnly")) {
			tickOnly.toggleValue(parser.getFirstToken(arguments));
			repaintAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to draw bars a constant width apart", "[on or off]", commandName, "constantDistance")) {
			constantDistance.toggleValue(parser.getFirstToken(arguments));
			repaintAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Chooses what character to shade on tree", null, commandName, "setSingleCharacter")) {
			int ic = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(ic)){
				shadedCharacter = ic;
			}
			else if (!MesquiteThread.isScripting()){
				MCharactersDistribution matrix = characterSourceTask.getCurrentMatrix(getTaxa());
				if (matrix != null){
					int numChars = matrix.getNumChars();
					ic = MesquiteInteger.queryInteger(containerOfModule(), "Which character to trace?", "Indicate the character (by its number) that you want to trace", shadedCharacter, 1, numChars);
					if (MesquiteInteger.isCombinable(ic)){
						shadedCharacter = ic-1;
					}
				}
			}
			repaintAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets length of bars", null, commandName, "setBarLength")) {
			int ic = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(ic)){
				barLength = ic;
			}
			else if (!MesquiteThread.isScripting()){
				ic = MesquiteInteger.queryInteger(containerOfModule(), "Length of bars", "Length of bars on tree (default 16)", (int)barLength, 2, 40);
				if (MesquiteInteger.isCombinable(ic)){
					barLength = ic;
				}
			}
			repaintAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show only the unambiguous changes, or all possible changes", "[on or off]", commandName, "ambiguousChangesAlso")) {
			ambiguousChangesAlso.toggleValue(parser.getFirstToken(arguments));
			Puppeteer p = new Puppeteer(this);
			MesquiteInteger pos = new MesquiteInteger(0);

			String commands =  "getMethod; tell It; toggleMPRsMode " + (ambiguousChangesAlso.getValue()) + "; endTell;";

			pos.setValue(0);
			p.execute(historyTask, commands, pos, "", false);
			if (!MesquiteThread.isScripting()){
				recalcAllTraceOperators();
				resetAllTraceOperators();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to show states only for selected characters", "[on or off]", commandName, "toggleSelectedCharsOnly")) {
			selectedCharactersOnly.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting())
				resetAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show states for terminal nodes", "[on or off]", commandName, "toggleShowTerminals")) {
			showTerminals.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting())
				resetAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show the table", "[on or off]", commandName, "toggleShowTable")) {
			showTable.toggleValue(parser.getFirstToken(arguments));
			resetTablesAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show character names", "[on or off]", commandName, "toggleShowCharNames")) {
			showCharNames.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting())
				repaintAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show state names", "[on or off]", commandName, "toggleShowStateNames")) {
			showStateNames.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting())
				repaintAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether to show states with rows = nodes and columns = characters, or the reverse", "[on or off]", commandName, "toggleByCharacters")) {
			byCharacters.toggleValue(parser.getFirstToken(arguments));
			if (!MesquiteThread.isScripting())
				resetAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Returns source of chars", null, commandName, "getCharSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Returns reconstructor", null, commandName, "getReconstructor")) {
			return historyTask;
		}
		else if (checker.compare(this.getClass(), "Turns off Trace All Characters", null, commandName, "closeTrace")) {
			iQuit();
			resetContainingMenuBar();
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	void toggleSingleTrace(boolean on){
		traceSingleCharacter.setValue(on);
		chooseCharToTraceMenuItem.setEnabled(on);
		if (!MesquiteThread.isScripting()){
			resetContainingMenuBar();
			resetAllTraceOperators();
		}
	}

	/* ................................................................................................................. */
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (!suppressed) {
			recalcAllTraceOperators();
			resetAllTraceOperators();
		}
	}

	void repaintAllTraceOperators() {
		if (suppressed)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceAllOperator) {
				TraceAllOperator tCO = (TraceAllOperator) obj;
				tCO.getTreeDisplay().repaint();
			}
		}
	}
	void recalcAllTraceOperators() {
		if (suppressed)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceAllOperator) {
				TraceAllOperator tCO = (TraceAllOperator) obj;
				tCO.recalculate();
			}
		}
	}
	/* ................................................................................................................. */
	public void resetTablesAllTraceOperators() {  //called ONLY when there is a change from table off to table on
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceAllOperator) {
				TraceAllOperator tCO = (TraceAllOperator) obj;
				if (showTable.getValue() && tCO.displayer == null){
					TextDisplayer displayer = (TextDisplayer) hireEmployee(TextDisplayer.class, null);
					if (displayer != null) {
						displayer.setWrap(false);
						displayer.setPoppedOut(1);
						displayer.showText("Trace All Characters", "Trace All Characters", true);
						tCO.setDisplayer(displayer);
						return; //only allows one table!
					}
				}
				else {
					fireEmployee(tCO.displayer);
					tCO.setDisplayer(null);
				}
			}
		}
	}
	/* ................................................................................................................. */
	public void closeDisplayersAllTraceOperators() {
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceAllOperator) {
				TraceAllOperator tCO = (TraceAllOperator) obj;
				tCO.refresh();
			}
		}
	}
	/* ................................................................................................................. */
	public void resetAllTraceOperators() {
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceAllOperator) {
				TraceAllOperator tCO = (TraceAllOperator) obj;
				tCO.refresh();
			}
		}
	}

	/* ................................................................................................................. */
	public void closeAllTraceOperators() {
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceAllOperator) {
				TraceAllOperator tCO = (TraceAllOperator) obj;
				tCO.turnOff();
			}
		}
	}

	/* ................................................................................................................. */
	public void endJob() {

		closeAllTraceOperators();
		super.endJob();
	}

	/* ................................................................................................................. */
	public String getName() {
		return "Trace All Changes (by Parsimony)";
	}

	/* ................................................................................................................. */
	public String getVersion() {
		return null;
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() {
		return true;
	}

	/* ................................................................................................................. */
	/** returns current parameters, for logging etc.. */
	public String getParameters() {
		if (characterSourceTask == null || historyTask == null)
			return "";
		return "Ancestral States:\n" + historyTask.getNameAndParameters() + "\nusing:\n" + characterSourceTask.getNameAndParameters();
	}

	/* ................................................................................................................. */
	public String getExplanation() {
		return "Summarizes for each node the reconstructed changes of states of all characters of the tree.";
	}
}

/* ======================================================================== */
class TraceAllOperator extends TreeDisplayDrawnExtra implements TreeDisplayLateExtra, MesquiteListener, Commandable {
	Tree myTree;

	TraceAllChanges traceAllModule;

	MCharactersHistory charsStates;

	MCharactersDistribution matrix;

	CharacterData oldData;

	TextDisplayer displayer;
	TreeDecorator decorator;
	boolean turnedOff = false;
	TreeDisplayLegendSimpleText legend;

	public TraceAllOperator(TraceAllChanges ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		traceAllModule = ownerModule;
		legend = new TreeDisplayLegendSimpleText(treeDisplay);
		decorator = traceAllModule.shadeTask.createTreeDecorator( treeDisplay, this);
		resetLegend();
		addPanelPlease(legend);
		legend.setVisible(true);	
	}
	void setDisplayer(TextDisplayer displayer){
		this.displayer = displayer;
		if (displayer != null)
			refresh();
	}
	/* ................................................................................................................. */
	public void recalculate() {

		if (turnedOff)
			return;
		if (traceAllModule.historyTask != null) {
			// note: following doesn't pass MesquiteString for results since does character by character and would only get message from last
			charsStates = traceAllModule.historyTask.calculateStates(myTree, matrix = traceAllModule.characterSourceTask.getCurrentMatrix(myTree), charsStates, null);
			int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();
			if (!myTree.nodeExists(drawnRoot))
				drawnRoot = myTree.getRoot();
			refresh();
		}
	}

	/* ................................................................................................................. */
	public void setTree(Tree tree) {
		myTree = tree;
		if (turnedOff)
			return;
		if (traceAllModule.historyTask != null) {
			charsStates = traceAllModule.historyTask.calculateStates(tree, matrix = traceAllModule.characterSourceTask.getCurrentMatrix(tree), charsStates, null);
			refresh();
		}
	}
	void resetLegend(){
		String s = null;
		if (traceAllModule.ambiguousChangesAlso.getValue()){
			s = "Trace All Characters:\nAll permissible changes, as reconstructed by parsimony (i.e., including ambiguous). Some shown may be mutually exclusive.";
		}
		else {
			s= "Trace All Characters:\nUnambiguous changes, as reconstructed by parsimony.";
		}
		legend.setText(s);
		legend.checkSize();
	}
	public void refresh() {
		if (turnedOff)
			return;
		resetLegend();
		if (displayer != null)
			displayer.showText(composeText(myTree, charsStates), "Trace All Characters", true);
		treeDisplay.repaint();
	}

	public String composeText(Tree tree, MCharactersHistory charStates) {
		if (traceAllModule == null || tree == null || charStates == null)
			return null;
		if (turnedOff)
			return null;
		StringBuffer sb = new StringBuffer(100);
		StringBuffer sb2 = new StringBuffer(100);
		MesquiteBoolean anyAdded = new MesquiteBoolean(false);
		CharacterData data = null;
		if (matrix != null)
			data = matrix.getParentData();
		if (oldData != null && data != null && data != oldData)
			oldData.removeListener(this);
		if (data != null && data != oldData)
			data.addListener(this);
		oldData = data;
		CharInclusionSet incl = null;

		if (data != null)
			incl = (CharInclusionSet) data.getCurrentSpecsSet(CharInclusionSet.class);

		sb.append("Trace All Characters\n\n");
		if (traceAllModule.historyTask != null) {
			sb.append(traceAllModule.getParameters());
			sb.append("\n======================================\n");
		}
		sb.append("Ancestral states are listed by character and by node on the tree.  Node numbers are shown in red on the tree drawing.\n\n");
		int drawnRoot = 0;
		if (treeDisplay != null && treeDisplay.getTreeDrawing() != null)
			drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		if (incl != null && incl.numberSelected() == 0) {
			sb.append("All characters are excluded.  You change the inclusion/exclusion of characters using the first column of the List of Characters window.");
		}
		/*
		 * else if (traceAllModule.selectedOnly.getValue() && !tree.anySelected()) { sb.append("No nodes are selected.  To show the results of Trace All Characters, either turn off the menu item \"Show Selected Nodes Only\", or select some branches of the tree.");
		 * 
		 * }
		 */
		else if (data != null && traceAllModule.selectedCharactersOnly.getValue() && !data.anySelected()) {
			sb.append("No characters are selected.  To show the results of Trace All Characters, either turn off the menu item \"Show Selected Characters Only\", or select some characters of the matrix.");

		}
		else if (traceAllModule.byCharacters.getValue()) {
			if (incl != null && data != null && (incl.numberSelected() < data.getNumChars()))
				sb.append(Integer.toString(incl.numberSelected()) + " of " + data.getNumChars() + " characters included.\n\n");
			sb.append("Char.\\Node");
			boolean ignoreSelection = !traceAllModule.selectedOnly.getValue() || !tree.anySelected();
			composeOneCharRec(tree, drawnRoot, null, sb, null, ignoreSelection);
			sb.append('\n');
			for (int ic = 0; ic < charStates.getNumChars(); ic++) {
				if ((!traceAllModule.selectedCharactersOnly.getValue() || data == null || data.getSelected(ic)) && (incl == null || incl.isBitOn(ic))) {
					CharacterHistory cH = charStates.getCharacterHistory(ic);
					anyAdded.setValue(false);
					sb2.setLength(0);
					composeOneCharRec(tree, drawnRoot, cH, sb2, anyAdded, ignoreSelection);
					if (anyAdded.getValue()) {
						sb.append("character ");
						sb.append(Integer.toString(ic + 1));
						sb.append(sb2.toString());
						sb.append('\n');
					}
				}
			}
		}
		else {
			if (incl != null && data != null && (incl.numberSelected() < data.getNumChars()))
				sb.append(Integer.toString(incl.numberSelected()) + " of " + data.getNumChars() + " characters included.\n\n");
			sb.append("Node\\Char.");
			for (int ic = 0; ic < charStates.getNumChars(); ic++) {
				if ((!traceAllModule.selectedCharactersOnly.getValue() || data == null || data.getSelected(ic)) && (incl == null || incl.isBitOn(ic))) {
					sb.append('\t');
					sb.append(Integer.toString(ic + 1));
				}
			}
			sb.append('\n');
			boolean ignoreSelection = !traceAllModule.selectedOnly.getValue() || !tree.anySelected();
			composeRecByNodes(tree, drawnRoot, charStates, data, incl, sb, ignoreSelection);
		}

		/*
		 * ====experimental sb.append("\nTaxa compared to immediate ancestor\n"); for (int it = 0; it < tree.getTaxa().getNumTaxa(); it++){ int taxon = tree.nodeOfTaxonNumber(it); if (taxon > 0){ int anc = tree.motherOfNode(taxon); if (anc >0){ int differences = 0; CharacterState csTaxon = null; CharacterState csAnc = null; for (int ic = 0; ic < charStates.getNumChars(); ic++){ csTaxon = charStates.getCharacterState(csTaxon, ic, taxon); csAnc = charStates.getCharacterState(csAnc, ic, anc); if (!csAnc.equals(csTaxon)) //if (!csAnc.equals(csTaxon, true, true, true))
		 * differences++; } if (differences == 0) tree.getTaxa().setSelected(it, true); sb.append(tree.getTaxa().getTaxonName(it) + " with " + differences + " differences compared to immediate ancestor\n");
		 * 
		 * } } }
		 */
		return sb.toString();

	}

	private String nodeName(Tree tree, int node, boolean verbose) {
		if (tree.nodeIsInternal(node)) {
			if (verbose)
				return ("node " + node);
			else
				return Integer.toString(node);
		}
		return tree.getTaxa().getName(tree.taxonNumberOfNode(node));

	}

	/* ................................................................................................................. */
	public void composeOneCharRec(Tree tree, int node, CharacterHistory states, StringBuffer sb, MesquiteBoolean anyAdded, boolean ignoreSelection) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			composeOneCharRec(tree, d, states, sb, anyAdded, ignoreSelection);
		if ((ignoreSelection || ((MesquiteTree) tree).getSelected(node)) && (traceAllModule.showTerminals.getValue() || tree.nodeIsInternal(node))) {
			sb.append('\t');
			if (states != null) {
				String s = states.toString(node, "; ");
				if (!StringUtil.blank(s)) {
					sb.append(s);
					if (anyAdded != null)
						anyAdded.setValue(true);
				}
			}
			else
				sb.append(nodeName(tree, node, false));
		}
	}

	/* ................................................................................................................. */
	public void composeRecByNodes(Tree tree, int node, MCharactersHistory states, CharacterData data, CharInclusionSet incl, StringBuffer sb, boolean ignoreSelection) {
		if (states == null)
			return;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			composeRecByNodes(tree, d, states, data, incl, sb, ignoreSelection);
		if ((ignoreSelection || ((MesquiteTree) tree).getSelected(node)) && (traceAllModule.showTerminals.getValue() || tree.nodeIsInternal(node))) {
			sb.append(nodeName(tree, node, true) + ": ");
			for (int ic = 0; ic < states.getNumChars(); ic++) {
				if ((!traceAllModule.selectedCharactersOnly.getValue() || data == null || data.getSelected(ic)) && (incl == null || incl.isBitOn(ic))) {
					sb.append('\t');
					CharacterHistory cH = states.getCharacterHistory(ic);
					sb.append(cH.toString(node, "; "));
				}
			}
			sb.append('\n');
		}
	}

	CharInclusionSet inclForDrawing = null;
	boolean showCharacter(int ic){
		if (!traceAllModule.selectedCharactersOnly.getValue())
			return true;
		CharacterData data = charsStates.getParentData();
		if (data != null && inclForDrawing == null)
			inclForDrawing = (CharInclusionSet) data.getCurrentSpecsSet(CharInclusionSet.class);
		return data == null || data.getSelected(ic) && (inclForDrawing == null || inclForDrawing.isBitOn(ic));
	}
	TextRotator textRotator = new TextRotator(1);

	Vector possibleChanges(Tree tree, int node, MCategoricalStates charsStates, int ic){
		Vector v = new Vector();
		CharacterState csAnc = charsStates.getCharacterState(null, ic, tree.motherOfNode(node));
		CategoricalHistory ch = (CategoricalHistory)((MCharactersHistory)charsStates).getCharacterHistory(ic);
		CategoricalState ancStt = (CategoricalState)csAnc;
		for (int i = 0; i<CategoricalState.maxCategoricalState; i++){
			if (ancStt.isElement(i)){ // i is the ancestral state
				long statesGivenAncSt = ch.getConditionalStateSet(node, i);
				if (!CategoricalState.isOnlyElement(statesGivenAncSt, i)){ //for parsimonious state i at ancestor, this node has a state set lacking i. Thus, an allowed change
					for (int k = 0; k<CategoricalState.maxCategoricalState; k++)
						if (CategoricalState.isElement(statesGivenAncSt, k) && k!= i) //k is in the descendant's conditional set, but is different from i; therefore possible synapomorphy
							v.addElement(new Point(i, k));
				}
			}
		}
		return v;
	}

	Font unambiguousFont, ambiguousFont;
	/* ................................................................................................................. */
	private void drawChanges(Tree tree, Graphics g, int node, MCategoricalStates charsStates) {
		if (tree.withinCollapsedClade(node))
			return;
		if (tree.getRoot() != node) {
			CharacterState csNode = null;
			CharacterState csAnc = null;
			CategoricalState csSynaps = null;
			int anc = tree.motherOfNode(node);
			double nodeX = treeDisplay.getTreeDrawing().lineTipX[node];
			double nodeY =  treeDisplay.getTreeDrawing().lineTipY[node];
			double ancX =  treeDisplay.getTreeDrawing().lineBaseX[node];
			double ancY =  treeDisplay.getTreeDrawing().lineBaseY[node];

			//First, survey the reconstructions to find and record the list of changes
			Vector vBarDecorations = new Vector();
			for (int ic = 0; ic < charsStates.getNumChars(); ic++) {
				if (showCharacter(ic)){
					int recordableChange = 0;
					csNode = charsStates.getCharacterState(csNode, ic, node);
					csAnc = charsStates.getCharacterState(csAnc, ic, tree.motherOfNode(node));
					long stateset = 0L;
					Color fillColor = null;
					String statesString = null;

					if (traceAllModule.ambiguousChangesAlso.getValue()){  // ========== ALL PERMITTED ================
						CategoricalHistory ch = (CategoricalHistory)((MCharactersHistory)charsStates).getCharacterHistory(ic);
						CategoricalState ancStt = (CategoricalState)csAnc;
						if (csSynaps == null)
							csSynaps = new CategoricalState();
						else
							csSynaps.setToUnassigned();
						for (int i = 0; i<CategoricalState.maxCategoricalState; i++){
							if (ancStt.isElement(i)){
								long statesGivenAncSt = ch.getConditionalStateSet(node, i);
								if (!CategoricalState.isOnlyElement(statesGivenAncSt, i)){ //for parsimonious state i at ancestor, this node has a state set lacking i. Thus, an allowed change
									csSynaps.addToSet(CategoricalState.clearFromSet(statesGivenAncSt, i)); 
								}
							}
						}
						if (!csSynaps.isUnassigned()){
							if ( !csNode.couldBeEqual(csAnc))
								recordableChange = 2;
							else
								recordableChange = 1;
							fillColor = MesquiteColorTable.getDefaultColor(charsStates.getMaxState(ic),CategoricalState.maximum(csSynaps.getValue()), MesquiteColorTable.COLORS);
							statesString = csSynaps.toDisplayString();
							stateset = ((CategoricalState)csSynaps).getValue();
						}
					}
					else {  // ========== UNAMBIGUOUS ================
						if ( !csNode.couldBeEqual(csAnc))
							recordableChange = 2;
						fillColor = charsStates.getColorOfStates(ic, node);
						statesString = csNode.toDisplayString();
						stateset = ((CategoricalState)csNode).getValue();
					}

					if (recordableChange>0) {
						String s = "";
						if (traceAllModule.showCharNames.getValue()){
							s += (ic+1);
							if (traceAllModule.showStateNames.getValue())
								s += ": " + statesString;
						}
						else if (traceAllModule.showStateNames.getValue())
							s += statesString;

						vBarDecorations.addElement(new BarDecorationRecord(fillColor, ic, s, stateset, recordableChange == 2));
					}
				}
			}

			//Next, now that we know how many there are to draw, we can draw them
			double barWidth = 8;
			double barSpacing = 8;
			if (traceAllModule.constantDistance.getValue())
				barSpacing = barWidth /2;
			int numBars = vBarDecorations.size();
			double offset = barWidth + barSpacing;
			double offsetRatio = 1;
			offsetRatio = 0;
			int extraGrabber = 32;
			double barLength = traceAllModule.barLength;
			boolean useColors = true;
			double leftTopBase = 0;
			if (numBars > 0) {
				double available = 0;
				if (treeDisplay.isUp() || treeDisplay.isDown()) {
					available = Math.abs(nodeY - ancY);
					if (available != 0)
						offsetRatio = (nodeX-ancX)/(nodeY-ancY);
					if (treeDisplay.isUp())
						leftTopBase = nodeX;
					else
						leftTopBase = ancX;
				}
				else if (treeDisplay.isRight() || treeDisplay.isLeft()) {
					available = Math.abs(nodeX - ancX);
					if (available != 0)
						offsetRatio = (nodeY-ancY)/(nodeX-ancX);
					if (treeDisplay.isLeft())
						leftTopBase = nodeY;
					else
						leftTopBase = ancY;
				}
				double perBarAvailable = (available) / (numBars + 2);
				if (perBarAvailable>=10){
					barWidth = 8;
				}
				else if (perBarAvailable>=8)
					barWidth = 6;
				else if (perBarAvailable>=6)
					barWidth = 4;
				else
					barWidth = 0;
				if (traceAllModule.constantDistance.getValue()) {
					barSpacing = barWidth /2;
					if (barSpacing < 0.00001)
						barSpacing = perBarAvailable;
				}
				else
					barSpacing = perBarAvailable - barWidth;

				if (barWidth < 4) {
					useColors = false;
				}
				offset = barWidth + barSpacing - 0.001;
				double total = barWidth*(numBars+1) + barSpacing*(numBars+2);
				Vector bars = getBarRecordsVectorAtNode(node);
				for (int ic = 0; ic < vBarDecorations.size(); ic++) {
					BarDecorationRecord bdr = (BarDecorationRecord) vBarDecorations.elementAt(ic);
					if (treeDisplay.isUp() || treeDisplay.isDown()) {  //======== UP/DOWN ======
						double topY = nodeY;

						double left = leftTopBase - (barLength-edgeWidth) / 2 + offsetRatio*offset;
						if (treeDisplay.isDown()){
							topY = nodeY-total; //ancY;
						}
						//if (true || offset + topY + barWidth + barSpacing < bottomY) {
						
							if (useColors) {
								if (traceAllModule.tickOnly.getValue()){
									g.setColor(Color.black);
									GraphicsUtil.drawLine(g, left, topY + offset + barWidth/2, left+ barLength, topY + offset + barWidth/2, 2);
								}
								else {
									g.setColor(bdr.color);
									GraphicsUtil.fillRect(g, left, topY + offset, barLength, barWidth);
									g.setColor(Color.black);
									GraphicsUtil.drawRect(g, left, topY + offset, barLength, barWidth);
								}
								bars.addElement(new BarRecord(new Rectangle((int)left, (int)(topY + offset-4), (int)barLength + extraGrabber, (int)barWidth +8), bdr.ic, bdr.stateset, node, bdr.unambiguous));
								
								if (bdr.unambiguous) {
									g.setFont(unambiguousFont);
								}
								else {
									g.setColor(Color.gray);
									g.setFont(ambiguousFont);
								}

								GraphicsUtil.drawString(g, bdr.text, left + barLength + 4, topY + offset + barWidth);
								g.setColor(Color.black);
								offset += barWidth + barSpacing;
							}
							else {
								GraphicsUtil.drawLine(g, left, topY + offset, left+barLength, topY + offset);
								offset += barSpacing;
							}
					//	}
					//	else if (MesquiteTrunk.developmentMode)
					//		System.err.println("Not enough room! " + node + " text " + bdr.text);
					}
					else if (treeDisplay.isRight() || treeDisplay.isLeft()) {  //======== RIGHT/LEFT ======
						double leftX = nodeX;
						double top = leftTopBase -(barLength-edgeWidth)/2 + offsetRatio*offset;
						if (treeDisplay.isRight()){
							leftX = nodeX-total;
						}
					//	if (offset + leftX + barWidth + barSpacing < rightX) {
							/*g.setColor(Color.gray);
							if (treeDisplay.isRight()) 
							g.fillRect((int)(leftX + offset-4), (int)(top), (int)barWidth+8, (int)barLength+extraGrabber);
							else 
								g.fillRect((int)(leftX + offset-4), (int)(top)-extraGrabber, (int)barWidth+8, (int)barLength + extraGrabber+8);
	*/
							if (useColors) {
								if (traceAllModule.tickOnly.getValue()){
									g.setColor(Color.black);
									GraphicsUtil.drawLine(g, leftX + offset + barWidth/2, top, leftX + offset + barWidth/2, top + barLength, 2);
								}
								else {
								g.setColor(bdr.color);
								GraphicsUtil.fillRect(g, leftX + offset, top, barWidth, barLength);
								g.setColor(Color.black);
								GraphicsUtil.drawRect(g, leftX + offset, top, barWidth, barLength);
								}
								if (bdr.unambiguous) {
									g.setFont(unambiguousFont);
								}
								else {
									g.setColor(Color.gray);
									g.setFont(ambiguousFont);
								}
								if (treeDisplay.isRight()) {
									textRotator.drawFreeRotatedText(bdr.text,  g, (int)(leftX + offset-barWidth - (8-barWidth)),(int)(top + barLength + 4), Math.PI/2, null, true, null); // the 8-barWidth is a mystery correction
									bars.addElement(new BarRecord(new Rectangle((int)(leftX + offset-4), (int)(top), (int)barWidth+8, (int)barLength+extraGrabber), bdr.ic, bdr.stateset, node, bdr.unambiguous));
								}
								else {
									textRotator.drawFreeRotatedText(bdr.text,  g, (int)(leftX + offset),(int)(top  - 4), -Math.PI/2, null, true, null);
									bars.addElement(new BarRecord(new Rectangle((int)(leftX + offset-4), (int)(top)-extraGrabber, (int)barWidth+8, (int)barLength + extraGrabber+8), bdr.ic, bdr.stateset, node, bdr.unambiguous));
								}
								g.setColor(Color.black);
								offset += barWidth + barSpacing;
							}
							else {
								GraphicsUtil.drawLine(g, leftX + offset, top, leftX + offset,top+barLength);
								offset += barSpacing;
							}
					//	}
					//	else
					//		System.err.println("Not enough room! " + node + " text " + bdr.text);
					}
				}
			}
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawChanges(tree, g, d, charsStates);

	}

	/* ................................................................................................................. */
	private void drawNumber(Tree tree, Graphics g, int N) {
		if (tree.withinCollapsedClade(N))
			return;
		if (tree.nodeExists(N)) {
			int x =  (int) treeDisplay.getTreeDrawing().x[N];
			int y =  (int) treeDisplay.getTreeDrawing().y[N];
			int offsetX = 0;
			int offsetY = 0;
			if (tree.nodeIsInternal(N)){
				if (treeDisplay.isUp()){
					offsetY = -4; //really should consider font size
				}
				else if (treeDisplay.isDown()){
					offsetY = edgeWidth + 8;
				}
				else if (treeDisplay.isRight()){
					offsetX = edgeWidth;
					offsetY = edgeWidth;
				}
				else if (treeDisplay.isLeft()){
					offsetX = -edgeWidth -8; //really should consider font size
					offsetY = edgeWidth;
				}
			}
			else {  //terminal
				if (treeDisplay.isUp()){
					offsetY = 16; //really should consider font size
					offsetX = edgeWidth+4; //really should consider font size
				}
				else if (treeDisplay.isDown()){
					offsetY = -16; //really should consider font size
					offsetX = edgeWidth+4; //really should consider font size
				}
				else if (treeDisplay.isRight()){
					offsetX = -24;
					offsetY = -8;
				}
				else if (treeDisplay.isLeft()){
					offsetX = 8;
					offsetY = -8;
				}
			}
			StringUtil.highlightString(g, Integer.toString(N), x + offsetX, y + offsetY, Color.red, Color.white); 
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				drawNumber(tree, g, d);
		}
	}

	Vector getBarRecordsVectorAtNode(int node){
		if (node < barRecords.getSize())
			return (Vector)barRecords.getValue(node);
		return null;
	}
	
	public boolean requestTraceMode(){
		return traceAllModule.traceSingleCharacter.getValue() && traceAllModule.shadedCharacter>=0;
	}
	ObjectArray barRecords = new ObjectArray(10);
	int edgeWidth = 0;
	/* ................................................................................................................. */
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if (traceAllModule == null || tree == null || charsStates == null)
			return;
		inclForDrawing = null;
		edgeWidth = treeDisplay.getTreeDrawing().getEdgeWidth();

		if (traceAllModule.traceSingleCharacter.getValue() && traceAllModule.shadedCharacter>=0){
			CategoricalHistory ch = (CategoricalHistory)((MCharactersHistory)charsStates).getCharacterHistory(traceAllModule.shadedCharacter);
			CharacterData parentData = charsStates.getParentData();
			if (ch != null && parentData != null){
				decorator.drawOnTree(tree, drawnRoot, ch, parentData.getCharacterDistribution(traceAllModule.shadedCharacter), null, g);
			}
		}
		if (traceAllModule.showTable.getValue())
			drawNumber(tree, g, drawnRoot);
		if (!treeDisplay.isUpDownRightLeft() || !(charsStates instanceof MCategoricalStates))
			return;
		Font font = g.getFont();
		if (traceAllModule.ambiguousChangesAlso.getValue()){
			unambiguousFont = new Font(font.getName(), Font.BOLD, font.getSize());
			ambiguousFont = new Font(font.getName(), Font.ITALIC, font.getSize());
		}
		else {
			unambiguousFont = font;
			ambiguousFont =font;
		}
		//resetting barRecords
		barRecords.resetSize(tree.getNumNodeSpaces());
		for (int i = 0; i<tree.getNumNodeSpaces(); i++){
			Vector v = (Vector)barRecords.getValue(i);
			if (v == null)
				barRecords.setValue(i, new Vector());
			else
				v.removeAllElements();
		}
		drawChanges(tree, g, drawnRoot, (MCategoricalStates) charsStates);
		g.setFont(font);
	}
	/* ................................................................................................................. */
	private BarRecord cursorCheck(Tree tree, Graphics g, int N, int x, int y) {
		if (tree.withinCollapsedClade(N))
			return null;
		if (tree.nodeExists(N)) {
			Vector bars = getBarRecordsVectorAtNode(N);
			for (int i=0; i<bars.size(); i++){
				BarRecord r = (BarRecord)bars.elementAt(i);
				if (r.contains(x, y)){
					return r;
				}
			}
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
				BarRecord found = cursorCheck(tree, g, d, x, y);
				if (found != null)
					return found;
			}
		}
		return null;
	}	

	/* ................................................................................................................. */
	void explain(BarRecord barFound ){
		MesquiteWindow w = traceAllModule.containerOfModule();
		String amb = "n unambiguous";
		if (traceAllModule.ambiguousChangesAlso.getValue())
			amb = " permissible";
		w.setExplanation("This bar shows that there is a" + amb + " change (according to parsimony) in character " + (barFound.ic+1));
	}
	/* ................................................................................................................. */
	/**to inform TreeDisplayExtra that cursor has just moved OUTSIDE of taxa or branches*/
	public void cursorMove(Tree tree, int x, int y, Graphics g){
		BarRecord barFound = cursorCheck(tree, null, tree.getRoot(), x, y);
		if (barFound != null){
			explain(barFound);
		}
	}
	/* ................................................................................................................. */
	/**to inform TreeDisplayExtra that cursor has just touched the field (not in a branch or taxon)*/
	public boolean cursorTouchField(Tree tree, Graphics g, int x, int y, int modifiers, int clickID){
		BarRecord barFound = cursorCheck(tree, null, tree.getRoot(), x, y);
		if (barFound != null){
			explain(barFound);
			MesquitePopup popup = new MesquitePopup(treeDisplay);
			if(traceAllModule.ambiguousChangesAlso.getValue()){
				Vector v = possibleChanges(tree, barFound.node, (MCategoricalStates)charsStates, barFound.ic);
				if (v != null) {
					if (barFound.unambiguous) {
						if (v.size()<=1)
							popup.addItem("Unambiguous change:",  MesquiteCommand.nullCommand, null);
						else
							popup.addItem("Unambiguous change; possibilities:",  MesquiteCommand.nullCommand, null);

					}
					else
						popup.addItem("Parsimony-permissible changes:",  MesquiteCommand.nullCommand, null);
					for (int i = 0; i<v.size(); i++){
						Point p = (Point)v.elementAt(i);
						String s = "  " + p.x + " to " + p.y;
						popup.addItem(s,  MesquiteCommand.nullCommand, null);
					}
					popup.addItem("-",  (MesquiteCommand)null, null);
				}	
			}
			else {
				CharacterState csNode = charsStates.getCharacterState(null, barFound.ic, barFound.node);
				CharacterState csAnc = charsStates.getCharacterState(null, barFound.ic, tree.motherOfNode(barFound.node));
				popup.addItem("Unambiguous change:",  MesquiteCommand.nullCommand, null);
				popup.addItem("  " + csAnc.toDisplayString() + " to " + csNode.toDisplayString(),  MesquiteCommand.nullCommand, null);
				popup.addItem("-",  (MesquiteCommand)null, null);
			}
			popup.addItem("Trace Character on Tree",  new MesquiteCommand("traceCharacter", Integer.toString(barFound.ic), this), null);
			popup.showPopup(x, y);
			return true;

		}
		return false;
	}
	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Traces character on tree", null, commandName, "traceCharacter")) {
			int ic = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(ic)){
				traceAllModule.shadedCharacter = ic;
				traceAllModule.toggleSingleTrace(true);
			}
			else {
				traceAllModule.shadedCharacter = -1;
				traceAllModule.toggleSingleTrace(false);

			}
			treeDisplay.repaint();
		}
		return null;
	}
	/* ................................................................................................................. */
	CharacterState cs = null;

	/** return a text version of information at node */
	public String textAtNode(Tree tree, int node) {
		if (charsStates != null) {
			return "(" + node + ")";
		}
		return "";
	}

	/** return any additional explanatory text, e.g. if there is extensive information too verbose for a legend but which should be output to text view */
	public String additionalText(Tree tree, int node) {
		return "";
	}

	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}

	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		if (obj == oldData && (code == MesquiteListener.SELECTION_CHANGED || code == MesquiteListener.NAMES_CHANGED)) {
			refresh();
		}
	}

	/** passes which object was disposed */
	public void disposing(Object obj) {
	}

	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?) */
	public boolean okToDispose(Object obj, int queryUser) {
		return true;
	}

	public void turnOff() {
		turnedOff = true;
		removePanelPlease(legend);
		if (oldData != null)
			oldData.removeListener(this);
		super.turnOff();
	}
}
class BarDecorationRecord {
	Color color; int ic;  String text; long stateset; boolean unambiguous;
	public BarDecorationRecord(Color c, int ic, String text, long stateset, boolean unambiguous){
		this.color = c;
		this.ic = ic;
		this.text=text;
		this.stateset = stateset;
		this.unambiguous = unambiguous;
	}
}
class BarRecord {
	Rectangle r; int ic;  long stateset; int node;boolean unambiguous;
	public BarRecord(Rectangle r, int ic, long stateset, int node, boolean unambiguous){
		this.r = r;
		this.ic = ic;
		this.stateset=stateset;
		this.node = node;
		this.unambiguous = unambiguous;
	}
	public boolean contains(int x, int y){
		return r.contains(x, y);
	}
}

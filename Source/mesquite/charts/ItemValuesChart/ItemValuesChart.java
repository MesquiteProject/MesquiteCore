/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.ItemValuesChart;

/* ~~ */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */

public class ItemValuesChart extends ItemsCharter {
	public void getEmployeeNeeds() { // This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.charts.Histogram.Histogram.class, getName() + " needs a module to draw the chart.", "The chart drawing module is selected automatically");
	}

	DrawChart charterTask;

	ItemsChartWindow cWindow;

	int suspend = 0;

	boolean doCountPending = false;

	boolean calculationsEnabled;
boolean startedAsScripting = false;
	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		charterTask = (DrawChart) hireNamedEmployee(DrawChart.class, "#mesquite.charts.Histogram.Histogram");
		if (charterTask == null)
			return sorry(getName() + " couldn't start because no charting module was obtained.");
		calculationsEnabled = !MesquiteThread.isScripting();
		startedAsScripting = MesquiteThread.isScripting();
		return true;
	}

	/* ................................................................................................................. */
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice() {
		return true;
	}

	/* ................................................................................................................. */
	public boolean isSubstantive() {
		return true;
	}

	public boolean isPrerelease() {
		return false;
	}

	/* ................................................................................................................. */
	public boolean showCitation() {
		return true;
	}

	public void employeeQuit(MesquiteModule m) {
		if (m == charterTask)
			iQuit();
	}

	/* ................................................................................................................. */
	public ChartWindow makeChartWindow(MesquiteModule requester) {
		cWindow = new ItemsChartWindow(requester, this);
		cWindow.setWindowSize(400, 400);
		return cWindow;
	}

	public void incrementSuspension() {
		suspend++;
	}

	public void decrementSuspension() {
		suspend--;
		if (suspend <= 0 && doCountPending && cWindow != null) {
			suspend = 0;
			doCountPending = false;
			if (cWindow == null || cWindow.autoCount == null || cWindow.autoCount.getValue())
				doCounts();
			else
				cWindow.showRecalcButton();
		}
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		if (cWindow == null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("getWindow");
		temp.addLine("tell It");
		Snapshot sn = cWindow.getSnapshot(file);
		temp.incorporate(sn, true);
		temp.addLine("endTell");
		temp.addLine("enableCalculations");
		return temp;
	}

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the chart drawing module", null, commandName, "getCharter")) {
			return charterTask;
		}
		else if (checker.compare(this.getClass(), "Returns the chart window", null, commandName, "getWindow")) {
			return cWindow;
		}
		else if (checker.compare(this.getClass(), "Increments the suspension level on the calculations", null, commandName, "suspendCalculations")) {
			incrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Decrements the suspension level on the calculations", null, commandName, "resumeCalculations")) {
			decrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Enables the calculations", null, commandName, "enableCalculations")) {
			calculationsEnabled = true;

		}
		else if (checker.compare(this.getClass(), "Requests that calculations be performed", null, commandName, "doCounts")) {
			if (suspend > 0)
				doCountPending = true;
			else {
				doCounts();
				doCountPending = false;
			}
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	/* ................................................................................................................. */
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (cWindow == null)
			return;
		if (employee == charterTask)
			((ItemsChartWindow) cWindow).recalcChart();
		else
			doCounts();
	}

	/* ................................................................................................................... */
	public void doCounts(int firstItem, int lastItem, boolean fullCount) {
		if (cWindow != null && !isDoomed()) {
			if (!calculationsEnabled)
				return;
			if (suspend > 0)
				doCountPending = true;
			else {
				((ItemsChartWindow) cWindow).doCounts(firstItem, lastItem, fullCount);
				doCountPending = false;
			}
		}
	}

	public void setTaxa(Taxa taxa) {
		if (cWindow != null)
			((ItemsChartWindow) cWindow).setTaxa(taxa);
	}

	/* ................................................................................................................... */
	public void setItemsSource(ItemsSource itemsSourceTask) {
		if (cWindow != null)
			((ItemsChartWindow) cWindow).setItemsSource(itemsSourceTask);
	}

	/* ................................................................................................................... */
	public void setNumberTask(NumberForItem numberTask) {
		if (cWindow != null)
			((ItemsChartWindow) cWindow).setNumberTask(numberTask);
	}

	/* ................................................................................................................. */
	public void setDefaultNumberOfItems(int def) {
		if (cWindow != null)
			((ItemsChartWindow) cWindow).setDefaultNumberOfItems(def);
	}

	/* ................................................................................................................... */
	public void setAuxiliary(Object object, boolean useAsFirstParameter) {
		if (cWindow != null)
			((ItemsChartWindow) cWindow).setAuxiliary(object, useAsFirstParameter);
	}

	/* ................................................................................................................. */
	public String getName() {
		return "Item values chart";
	}

	/* ................................................................................................................. */
	/** returns an explanation of what the module does. */
	public String getExplanation() {
		return "Manages chart of values for items.";
	}
}

/* ======================================================== */
class ItemsChartWindow extends ChartWindow implements ChartListener, ActionListener {
	private NumberArray values, clonedValues, weights;

	String averageString = "";

	String explanationString = "";

	private ItemValuesChart ownerModule;

	NumberForItem numberTask;

	ItemsSource itemsSourceTask;

	private int defaultNumberOfItems = 100;

	int numberOfItems = 100;

	private int numberOfItemsExamined = numberOfItems;

	private int numberOfItemsCounted = numberOfItems;

	private MesquiteMenuItemSpec numItemsItem;

	private Taxa taxa;

	Object auxiliary = null;

	boolean auxFirst = false;

	private int windowWidth = 0;

	private int windowHeight = 0;

	private int chartInsetTop = 10;

	private int chartInsetBottom = 65;

	private int chartInsetLeft = 0; // 10;

	private int chartInsetRight = 20;

	boolean itemsOnX = false;

	boolean firstTimeThrough = true;

	private String itemsOnXString = "Value (Y) by Items (X)";

	private String itemsOnYString = "Number of Items (Y) by Values (X)";

	private MesquiteString orientationName = new MesquiteString();

	ChartMessages messagePane;

	MesquiteBoolean orient;

	MesquiteNumber sum, average, med1, med2;

	private boolean wasDefinite = true;

	private boolean assigned = false;

	Panel scrollPanel;

	MesquiteBoolean colorItems;

	MesquiteDouble minInclude = new MesquiteDouble();

	MesquiteDouble maxInclude = new MesquiteDouble();

	MesquiteCommand xCommand;// NOT YET USED

	MiniScroll scrollBox;// NOT YET USED

	MesquiteChart chart;

	MesquiteCommand recalcCommand;

	MesquiteBoolean goAhead, autoCount;
	MesquiteBoolean writeTableFile = new MesquiteBoolean(false);
	boolean askedWriteTable = false;

	Button recalcButton;

	boolean liveChart = false;  //

	boolean chartBeingCalculated = false;

	public ItemsChartWindow(MesquiteModule requester, ItemValuesChart ownerModule) {
		super(requester, true);
		chartTitle = "Chart";

		sum = new MesquiteNumber();
		average = new MesquiteNumber();
		med1 = new MesquiteNumber();
		med2 = new MesquiteNumber();
		values = new NumberArray(0);
		orient = new MesquiteBoolean(true);
		ownerModule.addMenuItem(null, "Exclude Values...", ownerModule.makeCommand("excludeValues", this));
		MesquiteSubmenuSpec orientationSubmenu = ownerModule.addSubmenu(null, "Orientation");
		orientationName.setValue(itemsOnYString);
		orientationSubmenu.setSelected(orientationName);
		ownerModule.addItemToSubmenu(null, orientationSubmenu, itemsOnYString, ownerModule.makeCommand("itemsByValues", this));
		ownerModule.addItemToSubmenu(null, orientationSubmenu, itemsOnXString, ownerModule.makeCommand("valuesByItems", this));

		colorItems = new MesquiteBoolean(true);

		ownerModule.addCheckMenuItem(ownerModule.findMenuAmongEmployers("Colors"), "Color Items by Group", ownerModule.makeCommand("toggleColorItems", this), colorItems);

		xCommand = ownerModule.makeCommand("setX", this); // NOT YET USED
		scrollBox = new MiniScroll(xCommand, false, false, 0, 0, 0, "");// NOT YET USED

		this.ownerModule = ownerModule; // or should this be requester???
		chart = new MesquiteChart(ownerModule, 100, 0, ownerModule.charterTask.createCharter(this));

		addToWindow(chart);
		chart.setLocation(0, 0);
		setChart(chart);

		goAhead = new MesquiteBoolean(false);
		autoCount = new MesquiteBoolean(true);
		Panel f = chart.getField();
		if (f == null)
			autoCount.setValue(true);
		else {
			f.add(recalcButton = new Button("Recalculation needed"));
			recalcButton.setBounds(10, 10, 140, 30);
			recalcButton.setVisible(false);
			recalcButton.setBackground(Color.white);
			recalcButton.invalidate();
			recalcButton.addActionListener(this);
		}
		recalcCommand = ownerModule.makeCommand("recalculate", this);

		chart.deassignChart();
		if (itemsOnX)
			chart.constrainMinimumX(new MesquiteNumber(0));
		messagePane = new ChartMessages(this);
		addToWindow(messagePane);
		messagePane.setVisible(true);
		ownerModule.addCheckMenuItem(null, "Auto-recalculate", ownerModule.makeCommand("toggleAutoRecalc", this), autoCount);
		ownerModule.addMenuItem(null, "Force Recalculation", ownerModule.makeCommand("forceRecalculate", this));
		ownerModule.addCheckMenuItem(null, "Record Table to File", ownerModule.makeCommand("toggleWriteTableFile", this), writeTableFile);
		resetTitle();
		adjustComponentSizes();
	}

	public void setDefaultNumberOfItems(int def) {
		defaultNumberOfItems = def;
		numberOfItems = def;
	}

	/* ................................................................................................................... */
	public void setAverageString(String t) {
		averageString = t;
	}

	/* ................................................................................................................... */
	public void setExplanationString(String t) {
		explanationString = t;
	}

	/* ................................................................................................................... */
	public void setChartVisible() {
		adjustComponentSizes();
		// chart.setBackground(Color.pink);
		chart.setVisible(true);
		blankChart();
	}

	/* ................................................................................................................... */
	public void setScroller(Panel panel) {
		if (panel == null)
			return;
		scrollPanel = panel;
		chart.add(panel);
		scrollPanel.setLocation(0, windowHeight - chartInsetBottom - scrollPanel.getBounds().height - chartInsetTop);
		scrollPanel.setVisible(true);
	}

	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot sup = super.getSnapshot(file);
		sup.addLine("toggleAutoRecalc " + autoCount.toOffOnString());
		if (itemsOnX)
			sup.addLine("valuesByItems");
		else
			sup.addLine("itemsByValues");
		sup.addLine("setChartType ", ownerModule.charterTask);
		if (minInclude.isCombinable() || maxInclude.isCombinable()) {
			sup.addLine("excludeValues " + minInclude + "  " + maxInclude);
		}
		// if (highlightName !=null)
		// sup.addLine("highlight " + StringUtil.tokenize(highlightName));
		sup.addLine("toggleColorItems " + colorItems.toOffOnString());
		if (MesquiteInteger.isCombinable(numberOfItems))
			sup.addLine("setNumberItems " + numberOfItems);
		return sup;
	}

	/* ................................................................................................................. */
	public void setOrientation(boolean iOnX) { // orientation stored in chart as 0 = items by values; 1 = values by items
		itemsOnX = iOnX;
		if (itemsOnX) {
			chart.constrainMinimumX(new MesquiteNumber(0));
			chart.setOrientation(1);
		}
		else {
			chart.deConstrainMinimumX();
			chart.setOrientation(0);
		}
		if (!MesquiteThread.isScripting())
			recalcChart();
	}

	/* ................................................................................................................. */
	public boolean getOrientation() {
		return itemsOnX;
	}

	/* ................................................................................................................. */
	public void setCharter(Charter charter) {
		chart.setCharter(charter);
	}

	MesquiteInteger pos = new MesquiteInteger(0);

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the chart drawing module", "[module name]", commandName, "setChartType")) {
			String newc = ParseUtil.getFirstToken(arguments, pos);
			if (ownerModule.charterTask.getClass() != null && (ownerModule.charterTask.nameMatches(newc))) // already have this
				return ownerModule.charterTask;

			DrawChart temp = (DrawChart) ownerModule.replaceEmployee(DrawChart.class, arguments, "Type of chart", ownerModule.charterTask);
			if (temp != null) {
				ownerModule.charterTask = temp;

				setCharter(ownerModule.charterTask.createCharter(this));
				ownerModule.parametersChanged();
				return ownerModule.charterTask;
			}
		}
		else if (checker.compare(this.getClass(), "Returns median 1", null, commandName, "getMedian1")) {
			return med1;

		}
		else if (checker.compare(this.getClass(), "Returns median 2", null, commandName, "getMedian2")) {
			return med2;

		}
		else if (checker.compare(this.getClass(), "Returns average", null, commandName, "getAverage")) {
			return average;

		}
		else if (checker.compare(this.getClass(), "Returns values as tabbed line", null, commandName, "getValuesTabbed")) {
			if (values == null)
				return null;
				StringBuffer s = new StringBuffer();  
				for (int i =0; i<values.getSize(); i++){
					if (i != 0)
						s.append("\t");
					s.append(values.toString(i));
				}
				s.append(StringUtil.lineEnding());
			return s.toString();
		}

		else if (checker.compare(this.getClass(), "Sets whether or not to recalculate automatically when needed", "[on=auto; off]", commandName, "toggleAutoRecalc")) {
			Parser parser = new Parser();
			autoCount.toggleValue(parser.getFirstToken(arguments));
			if (autoCount.getValue()) {
				recalcButton.setVisible(false);
				doCounts();
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to output a table during calculations", "[on=auto; off]", commandName, "toggleWriteTableFile")) {
			Parser parser = new Parser();
			boolean c = writeTableFile.getValue();
			writeTableFile.toggleValue(parser.getFirstToken(arguments));
			if (writeTableFile.getValue()) {
				tableFile = new MesquiteFile();
				if (MesquiteThread.isScripting()){
					tableFile.changeLocation(ownerModule.getProject().getHomeDirectoryName(), "Table.txt");
				}
				else {
					if (StringUtil.blank(tableFile.getFileName()))
						tableFile.setFileName("Table.txt");
					tableFile.changeLocation("Record Table to File");
				}
				if (!c && !MesquiteThread.isScripting())
					doCounts();
			}
			else
				tableFile = null;
			return null;
		}
		else if (checker.compare(this.getClass(), "Sets the values to exclude", "[min value included][max value included]", commandName, "excludeValues")) {
			pos.setValue(0);
			double min = MesquiteDouble.fromString(arguments, pos);
			double max = MesquiteDouble.fromString(arguments, pos);
			MesquiteDouble minD = new MesquiteDouble(minInclude.getValue());
			MesquiteDouble maxD = new MesquiteDouble(maxInclude.getValue());
			if (!MesquiteDouble.isCombinable(min) && !MesquiteDouble.isCombinable(max)) {
				if (MesquiteThread.isScripting())
					return null;
				if (!QueryDialogs.queryTwoDoubles(this, "Exclude Values", "Exclude values less than:", minD, "Exclude values greater than:", maxD)) {
					return null;
				}
			}
			else {
				minD.setValue(min);
				maxD.setValue(max);
			}
			if (minD.isCombinable() && maxD.isCombinable() && minD.getValue() > maxD.getValue()) {
				ownerModule.discreetAlert("Sorry, minimum can't be greater than maximum");
				return null;
			}
			minInclude.setValue(minD.getValue());
			maxInclude.setValue(maxD.getValue());
			ownerModule.parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Requests recalculation", null, commandName, "recalculate")) {
			doCounts();
			return null;
		}
		else if (checker.compare(this.getClass(), "Requests recalculation", null, commandName, "forceRecalculate")) {
			if (recalcButton.isVisible()) {
				recalcButton.setVisible(false);
			}
			goAhead.setValue(true);
			doCounts();
			contentsChanged();

			return null;
		}
		else if (checker.compare(this.getClass(), "Sets which of a series of items is shown (appropriate when there could be a series of alternative values, e.g. for different characters or trees)", "[item number]", commandName, "setX")) {// NOT YET USED
			if (numberTask instanceof Incrementable) {
				Incrementable inc = (Incrementable) numberTask;
				int ic = MesquiteInteger.fromString(arguments, new MesquiteInteger(0)) - 1; // assuming itemType is 0 based internally and 1 based to user
				if (!MesquiteInteger.isCombinable(ic))
					ic = MesquiteInteger.queryInteger(ownerModule.containerOfModule(), "Choose ", "choose:", 1); // TODO have something more intelligent here
				if (MesquiteInteger.isCombinable(ic) && (ic >= inc.getMin()) && (ic <= inc.getMax())) {
					inc.setCurrent(ic);
					if (!MesquiteThread.isScripting()) {
						doCounts();
						contentsChanged();
					}
				}

			}
		}
		/*
		 * else if (checker.compare(this.getClass(), "Sets whether to use any available weights for the items", "[on or off]", commandName, "toggleUseWeights")) { boolean current = useWeights.getValue(); useWeights.toggleValue(ParseUtil.getFirstToken(arguments, pos)); if (current!=useWeights.getValue() && !MesquiteThread.isScripting()) getWeights(); }
		 */
		else if (checker.compare(this.getClass(), "Sets whether to color the items in the chart according to any natural item colors", "[on or off]", commandName, "toggleColorItems")) {
			boolean current = colorItems.getValue();
			colorItems.toggleValue(ParseUtil.getFirstToken(arguments, pos));
			if (current != colorItems.getValue() && !MesquiteThread.isScripting())
				recalcChart();
		}
		else if (checker.compare(this.getClass(), "[no longer used]", null, commandName, "toggleOrientation")) {
			if (StringUtil.blank(arguments))
				orient.toggleValue();
			else {
				String s = ParseUtil.getFirstToken(arguments, pos);
				if ("on".equalsIgnoreCase(s))
					orient.setValue(true);
				else if ("off".equalsIgnoreCase(s))
					orient.setValue(false);
			}
			setOrientation(orient.getValue());
		}
		else if (checker.compare(this.getClass(), "Sets the chart to show values on the Y axis and items (in order) on the X axis", null, commandName, "valuesByItems")) {
			itemsOnX = true;
			orient.setValue(itemsOnX);
			orientationName.setValue(itemsOnXString);
			setOrientation(itemsOnX);
		}
		else if (checker.compare(this.getClass(), "Sets the chart to show the number of items on the Y axis and the values on the X axis", null, commandName, "itemsByValues")) {
			itemsOnX = false;
			orient.setValue(itemsOnX);
			orientationName.setValue(itemsOnYString);
			setOrientation(itemsOnX);
		}
		else if (checker.compare(this.getClass(), "Sets the number of items (appropriate when the item source has an indefinite number)", "[number of items]", commandName, "setNumberItems")) {
			// TODO: this should first check if arguments say
			int newNum = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(ownerModule.containerOfModule(), "Set Number of " + itemsSourceTask.getItemTypeNamePlural(), "Number of " + itemsSourceTask.getItemTypeNamePlural(), numberOfItems, 0, MesquiteInteger.infinite);
			if (MesquiteInteger.isCombinable(newNum) && newNum > 0) {
				assigned = true;
				if (newNum != numberOfItems) {
					numberOfItems = newNum;
					if (!MesquiteThread.isScripting()) {
						ownerModule.doCounts();
						contentsChanged();
					}
				}
			}
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	/* ................................................................................................................... */
	public void pointMouseDown(MesquiteChart chart, int whichPoint, MesquiteNumber valueX, MesquiteNumber valueY, int x, int y, int modifiers, String message) {
		/*
		 * String name = chart.getName(whichPoint); if (name ==null){ Object item = itemsSourceTask.getItem(taxa, whichPoint, false); if (item instanceof Listable) name = ((Listable)item).getName(); } if (name==null) name = "#" + whichPoint; name += "\nx " + chart.getXArray().toString(whichPoint) + "\ny " + chart.getYArray().toString(whichPoint); if (numberTaskZ!=null) name += "\ncolor " + chart.getZArray().toString(whichPoint);
		 */
		String name = "";
		if (MesquiteInteger.isCombinable(whichPoint)) {
			if (itemsOnX) {
				name = chart.getName(whichPoint);
				if (name == null) {
					Object item = itemsSourceTask.getItem(taxa, whichPoint);
					if (item instanceof Listable)
						name = ((Listable) item).getName();
				}
				if (name == null)
					name = itemsSourceTask.getItemTypeName() + " #" + (whichPoint + 1) + "\n"; // todo: items source should have toExternal method for zero-based internal to 1-based external
				if (valueY != null)
					name += " with value " + valueY + "\n";
			}
			else {
				name = itemsSourceTask.getItemTypeNamePlural() + "\n";
				if (valueY != null)
					name += "Number: " + valueY + "\n";
				if (valueX != null)
					name += "Value: " + valueX + "\n";
			}
		}
		if (message != null)
			name += message;
		chart.showQuickMessage(whichPoint, x, y, name);
	}

	/* ................................................................................................................... */
	public void pointMouseUp(MesquiteChart chart, int whichPoint, int x, int y, int modifiers, String message) {
		chart.hideQuickMessage();
	}

	/* ................................................................................................................. */
	public void selectAllGraphicsPanel() {
		// this paradoxical deselection is done because chart treats all as selected if none are
		if (itemsSourceTask.getSelectionable() != null) {
			itemsSourceTask.getSelectionable().deselectAll();
			itemsSourceTask.getSelectionable().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
		else {
			chart.deselectAllPoints();
			chart.repaint();
			chart.getField().repaint();
		}
	}

	/* ................................................................................................................... */
	public void setTaxa(Taxa taxa) {
		this.taxa = taxa;
	}

	/* ................................................................................................................... */
	public void setItemsSource(ItemsSource itemsSourceTask) {
		this.itemsSourceTask = itemsSourceTask;
		if (itemsSourceTask != null)
			itemsSourceTask.setEnableWeights(true);
		if (ownerModule.charterTask != null && itemsSourceTask != null) {
			ownerModule.charterTask.pointsAreSelectable(itemsSourceTask.getSelectionable() != null, itemsSourceTask.getSelectionable(), true);
		}
	}

	MesquiteFile tableFile = null;
	/* ................................................................................................................... */
	public void setNumberTask(NumberForItem numberTask) {
		if (!ownerModule.startedAsScripting) {
			if (this.numberTask == null || (!this.numberTask.returnsMultipleValues() && numberTask.returnsMultipleValues())){
			if (numberTask.returnsMultipleValues()) {
				writeTableFile.setValue(AlertDialog.query(this, "Save Table?", "The calculator chosen can return multiple values for each item.  Should Mesquite save a table of the results in a file?"));
				if (writeTableFile.getValue()){
					tableFile = new MesquiteFile();
					tableFile.setFileName("Table.txt");
					tableFile.changeLocation("Record Table to File");
				}

			}
		}
		}
		this.numberTask = numberTask;
	}

	/* ................................................................................................................... */
	public void setAuxiliary(Object object, boolean useAsFirstParameter) {
		auxiliary = object;
		auxFirst = useAsFirstParameter;
	}

	/* ................................................................................................................... */
	private void resetSizes(Taxa taxa) {
		if (itemsSourceTask == null)
			return;
		int numItems = itemsSourceTask.getNumberOfItems(taxa);

		if (!MesquiteInteger.isFinite(numItems)) {
			if (wasDefinite && !assigned) {
				numberOfItems = defaultNumberOfItems;
				if (!MesquiteThread.isScripting()) {
					numberOfItems = MesquiteInteger.queryInteger(ownerModule.containerOfModule(), "Number of " + itemsSourceTask.getItemTypeNamePlural(), "Number of " + itemsSourceTask.getItemTypeNamePlural() + " for chart", numberOfItems);
					if (!MesquiteInteger.isCombinable(numberOfItems))
						numberOfItems = defaultNumberOfItems;
				}
			}
			wasDefinite = false;
			assigned = true;
			if (numItemsItem == null) {
				numItemsItem = ownerModule.addMenuItem("Number of  " + itemsSourceTask.getItemTypeNamePlural() + "...", ownerModule.makeCommand("setNumberItems", this));
				ownerModule.resetContainingMenuBar();
			}
		}
		else {
			if (numItemsItem != null) {
				ownerModule.deleteMenuItem(numItemsItem);
				ownerModule.resetContainingMenuBar();
				numItemsItem = null;
			}
			wasDefinite = true;
			assigned = false;
			numberOfItems = numItems;
		}
		resetSizeTo(numberOfItems);
	}

	private void resetSizeTo(int num) {
		if (!MesquiteInteger.isCombinable(num)) { // finite but indefinite; set storage to 100
			values.resetSize(100);
			if (weights != null)
				weights.resetSize(100);
		}
		else {
			values.resetSize(num);
			if (weights != null)
				weights.resetSize(num);
		}
	}

	private void checkSizes(int num) {
		if (num >= values.getSize()) {
			values.resetSize(values.getSize() + 100);
			if (weights != null)
				weights.resetSize(weights.getSize() + 100);
		}
	}

	int countt = 0;

	boolean weighted;

	/***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************
	 * ................................................................................................................. public void getWeights() { if (itemsSourceTask == null || !useWeights.getValue()) return; MesquiteModule.incrementMenuResetSuppression(); weighted = useWeights.getValue() && itemsSourceTask.itemsHaveWeights(taxa); if (weighted) { if (weights == null) weights = new NumberArray(numberOfItems); else if (weights!=null) weights.resetSize(numberOfItems); weights.deassignArray(); MesquiteNumber cNum = new MesquiteNumber(); for (int im=0; im<numberOfItems; im++ )
	 * weights.setValue(im, itemsSourceTask.getItemWeight(taxa, im); recalcChart(); }
	 * 
	 * MesquiteModule.decrementMenuResetSuppression(); } /*.................................................................................................................
	 */
	public void actionPerformed(ActionEvent e) {
		String buttonLabel = e.getActionCommand();
		if ("Recalculation needed".equalsIgnoreCase(buttonLabel)) { // Recalc button is pressed
			goAhead.setValue(true);
			recalcCommand.doItMainThread(null, null, this); // command invoked
		}
	}

	/* ................................................................................................................. */
	private boolean waitOnCalculation() {
		if (autoCount.getValue() || MesquiteThread.isScripting()) { // set to autocount; go ahead and do count
			if (recalcButton.isVisible())
				recalcButton.setVisible(false);
			return false;
		}
		else { // counts only if goAhead flag is set, which it will be following click on button
			if (goAhead.getValue()) {
				goAhead.setValue(false);
				if (recalcButton.isVisible())
					recalcButton.setVisible(false);
				return false;
			}
			else if (!recalcButton.isVisible())
				recalcButton.setVisible(true);
			if (chart.getNumPoints() > 0)
				chart.deassignChart();
			recalcButton.setSize(recalcButton.getPreferredSize());
			recalcButton.invalidate();
			recalcButton.doLayout();
			recalcButton.repaint();
		}
		return true;
	}
	void showRecalcButton(){
		if (!recalcButton.isVisible())
			recalcButton.setVisible(true);
		recalcButton.setSize(recalcButton.getPreferredSize());
		recalcButton.invalidate();
		recalcButton.doLayout();
		recalcButton.repaint();
	}
	/* ................................................................................................................. */
	public void doCounts() {
		doCounts(MesquiteInteger.unassigned, MesquiteInteger.unassigned, true);
	}

	/* ................................................................................................................. */
	public void doCounts(int firstItem, int lastItem, boolean fullCount) {
		if (chartBeingCalculated)
			return;
		if (values == null)
			return;
		if (itemsSourceTask == null)
			return;
		if (numberTask == null)
			return;
		if (!ownerModule.calculationsEnabled)
			return;
		boolean countAll = fullCount || firstTimeThrough;
		MesquiteModule.incrementMenuResetSuppression();
		resetSizes(taxa);
		if (waitOnCalculation()) {
			blankChart();
			MesquiteModule.decrementMenuResetSuppression();
			return;
		}
		chartBeingCalculated = true;
		Object item;
		int startItem, endItem;

		if (countAll) {
			med1.setToUnassigned();
			med2.setToUnassigned();
			blankChart();
			values.deassignArray();

			weighted = itemsSourceTask.itemsHaveWeights(taxa);
			if (weighted) {
				if (weights == null) {
					if (MesquiteInteger.isCombinable(numberOfItems))
						weights = new NumberArray(numberOfItems);
					else
						weights = new NumberArray(100);
				}
				weights.deassignArray();
			}
			numberOfItemsCounted = 0;
			sum.setValue(0.0);
			average.setValue(0.0);
		}

		if (MesquiteInteger.isUnassigned(firstItem))
			startItem = 0;
		else
			startItem = firstItem;
		if (MesquiteInteger.isUnassigned(lastItem))
			endItem = numberOfItems - 1;
		else
			endItem = lastItem;
		int numberOfItemsExaminedThisTime = endItem - startItem + 1;
		numberOfItemsExamined = numberOfItems;

		resetSizeTo(numberOfItemsExamined);
		// startProfiling();
		int everyWhich = 50;
		// if (everyWhich<1)
		// everyWhich = 1;
		MesquiteNumber cNum = new MesquiteNumber();

		ProgressIndicator prog = null;
		long startTime = System.currentTimeMillis();
		boolean progOn = false;

		int code = 0;
		int im;
		int lastAdded = startItem - 1;
		if (writeTableFile != null && writeTableFile.getValue()){
			tableFile.openWriting(true);
		}
		try {
			int oldnumberOfItemsExamined = numberOfItemsExamined;
			String numS = MesquiteInteger.toString(numberOfItemsExamined);
			if (numberOfItemsExamined == MesquiteInteger.finite)
				numS = "an undetermined number";
			if (countAll)
				recalcChartInit();
			long lastProgressTime = System.currentTimeMillis();
			MesquiteDialog.hideWizardForCalculation();
			for (im = startItem; im <= endItem && (prog == null || !prog.isAborted()); im++) {

				if (numberOfItemsExamined != oldnumberOfItemsExamined) {
					numS = MesquiteInteger.toString(numberOfItemsExamined);
					if (numberOfItemsExamined == MesquiteInteger.finite)
						numS = "an undetermined number";
					oldnumberOfItemsExamined = numberOfItemsExamined;
				}
				code = 1;
				if (!progOn && System.currentTimeMillis() - startTime > 5000) {
					prog = new ProgressIndicator(ownerModule.getProject(), "Calculating Chart", "Calculating value for " + itemsSourceTask.getItemTypeNamePlural(), numberOfItemsExamined, true);
					prog.start();
					progOn = true;
				}
				else if (progOn && System.currentTimeMillis()-lastProgressTime>200) {
					prog.setCurrentValue(im);
					prog.setText("Examining " + itemsSourceTask.getItemTypeName() + "  " + (im + 1) + " of " + numS, false);
					lastProgressTime = System.currentTimeMillis();
				}
				if (im % 5 == 0)
					CommandRecord.tick("Examining " + itemsSourceTask.getItemTypeName() + "  " + (im + 1) + " of " + numS);
				item = itemsSourceTask.getItem(taxa, im);
				code = 2;
				if (item == null) {
					// null item hit; source may have unexpectedly run out; reset total expected
					assigned = false;
					numberOfItemsExamined = im;
					endItem = im - 1;

					resetSizeTo(im);
					if (prog != null && progOn) {
						prog.goAway();
					}
				}
				else {
					checkSizes(im);
					if (weighted) {
						weights.setValue(im, itemsSourceTask.getItemWeight(taxa, im));
					}
					code = 3;
					cNum.setToUnassigned();

					if (auxFirst)
						numberTask.calculateNumber(auxiliary, item, cNum, null);
					else
						numberTask.calculateNumber(item, auxiliary, cNum, null);
					if (cNum.isCombinable() && minInclude.isCombinable() && cNum.isLessThan(minInclude.getValue()))
						cNum.setToUnassigned();
					if (cNum.isCombinable() && maxInclude.isCombinable() && cNum.isMoreThan(maxInclude.getValue()))
						cNum.setToUnassigned();

					if (writeTableFile != null && writeTableFile.getValue()){
						tableFile.writeLine(Integer.toString(im+1) + '\t' + cNum.toStringWithDetails());
					}
					code = 4;
					if (values != null)
						values.setValue(im, cNum);
					code = 5;
					if (cNum.isCombinable())
						numberOfItemsCounted++;

					sum.add(cNum);
					code = 6;
				}
				if (liveChart && im % everyWhich == 0 && im > startItem) {
					addPointsToChart(lastAdded + 1, im);
					lastAdded = im;
				}
			}
			if (prog != null && prog.isAborted()) {
				if (!AlertDialog.query(ownerModule.containerOfModule(), "Calculations Stopped", "Calculations stopped before all " + itemsSourceTask.getItemTypeNamePlural() + " were examined.  Do you want to display the results?", "Display", "Discard", 1)) {
					// here we discard results, and display a blank chart
					autoCount.setValue(false);
					waitOnCalculation();
					blankChart();
					prog.goAway();
					chartBeingCalculated = false;
					MesquiteModule.decrementMenuResetSuppression();
					return;
				}
				assigned = false;
				numberOfItemsExamined = im; // reset the number of items to the actual number processed
				endItem = im;
				values.resetSize(numberOfItemsExamined);
			}

		} catch (OutOfMemoryError e) {
			MesquiteMessage.println("Error in ItemValuesChart " + e + " ===== " + code + " -- insufficient memory.   See file memory.txt in the Mesquite_Folder.");
		}
		if (writeTableFile != null && writeTableFile.getValue())
			tableFile.closeWriting();
		addPointsToChart(lastAdded + 1, endItem + 1);

		if (progOn)
			prog.goAway();
		ownerModule.charterTask.pointsAreSelectable(itemsSourceTask.getSelectionable() != null, itemsSourceTask.getSelectionable(), true);
		if (clonedValues == null)
			clonedValues = values.cloneArray();
		else
			clonedValues.setToClone(values);
		clonedValues.sortAndPlaceMedian(med1, med2);
		if (countAll)
			recalcChart();
		else {
			resetMessagePaneText();
			messagePane.repaint();
		}
		MesquiteModule.decrementMenuResetSuppression();
		firstTimeThrough = false;
		chartBeingCalculated = false;
	}

	/* ................................................................................................................. */
	public String getTextContents() {
		String s = super.getTextContents();
		if (s == null)
			return null;
		String m = messagePane.getText();
		if (m == null)
			return s;
		return m + StringUtil.lineEnding() + StringUtil.lineEnding() + s;
	}

	/* ................................................................................................................. */
	public void resetMessagePaneText() {
		average.setValue(sum);
		average.divideBy(numberOfItemsCounted);
		// clonedValues.sortAndPlaceMedian(med1, med2);
		String medianString = "";
		if (med1.isCombinable() && med2.isCombinable() && !med1.equals(med2))
			medianString = "; Median: " + med1 + " to " + med2;
		else if (med1.isCombinable())
			medianString = "; Median: " + med1;
		else if (med2.isCombinable())
			medianString = "; Median: " + med2;
		averageString = "Average " + numberTask.getNameOfValueCalculated() + ": " + average.toString() + medianString + "  (n=" + numberOfItemsCounted + "  " + itemsSourceTask.getItemTypeNamePlural() + ")";
		if (minInclude.isCombinable() || maxInclude.isCombinable()) {
			averageString += "\nValues";
			if (minInclude.isCombinable())
				averageString += " less than " + minInclude;
			if (minInclude.isCombinable() && maxInclude.isCombinable())
				averageString += " or ";
			if (maxInclude.isCombinable())
				averageString += " greater than " + maxInclude;
			averageString += " are excluded ";
		}
		explanationString = "Source of  " + itemsSourceTask.getItemTypeNamePlural() + ": " + itemsSourceTask.getNameAndParameters();
		if (!StringUtil.blank(ownerModule.charterTask.getParameters()))
			explanationString += "\nChart:  " + ownerModule.charterTask.getParameters();
		// explanationString += "\nValues calculated: " + numberTask.getNameAndParameters();
		explanationString += "\nDetails of items plotted:\n" + itemsSourceTask.accumulateParameters("   ");
		explanationString += "\nDetails of  calculated values:\n" + numberTask.accumulateParameters("   ");
		messagePane.setText(averageString + "\n" + explanationString);
	}

	/* ................................................................................................................. */
	public boolean recalcChartInit() {
		chart.deassignChart();
		if (numberTask == null || itemsSourceTask == null)
			return false;
		if (!itemsOnX) {
			String valueString = "Number";
			if (chart.getCharter()!= null && chart.getCharter().getShowPercent())
				valueString = "Percent";
			chart.setXAxisName(numberTask.getNameOfValueCalculated());
			if (weighted)
				chart.setYAxisName("Weighted "+valueString +" of  " + itemsSourceTask.getItemTypeNamePlural() + chart.getYAxisNameSuffix());
			else
				chart.setYAxisName(valueString + " of  " + itemsSourceTask.getItemTypeNamePlural()+ chart.getYAxisNameSuffix());
		}
		else {

			if (chart.getCharter()!= null && chart.getCharter().getShowPercent())
				chart.setYAxisName(numberTask.getNameOfValueCalculated() + " (Percentage)"+ chart.getYAxisNameSuffix());
			else
				chart.setYAxisName(numberTask.getNameOfValueCalculated()+ chart.getYAxisNameSuffix());
			chart.setXAxisName(itemsSourceTask.getItemTypeName());
		}

		if (colorItems.getValue())
			itemsSourceTask.prepareItemColors(taxa);
		return true;
	}

	/* ................................................................................................................. */
	public void addPointsToChart(int startValue, int endValue) {
		MesquiteNumber resultX = new MesquiteNumber();
		MesquiteNumber resultY = new MesquiteNumber();
		resultY.setValue(1);
		/*
		 * if (highlightsExist && hE!=null) explanationString += " [Highlighted " + hE + "]";
		 */

		chart.getCharter().setSuspendDrawing(true);
		chart.getCharter().setSuspendChartCalculations(true);
		for (int i = startValue; i < endValue; i++) {
			if (i % 100 == 0)
				CommandRecord.tick("Constructing chart; adding item " + i);
			if (weighted && weights != null && weights.isCombinable(i))
				weights.placeValue(i, resultY);
			if (!itemsOnX) {
				if (!values.isCombinable(i))
					resultX.setToUnassigned();
				else
					values.placeValue(i, resultX);
				int point = chart.addPoint(resultX, resultY);
				if (colorItems.getValue()) {
					Color c = itemsSourceTask.getItemColor(taxa, i);
					if (c != null) {
						chart.setColor(point, c);
					}
				}
				
			}
			else {
				int j = i + 1; // TODO: this shouldn't always bump by one (zero based internal to 1 based for user)
				if (!values.isCombinable(i))
					resultY.setToUnassigned();
				else
					values.placeValue(i, resultY);
				resultX.setValue(j);
				int point = chart.addPoint(resultX, resultY);
				if (colorItems.getValue()) {
					Color c = itemsSourceTask.getItemColor(taxa, i);
					if (c != null) {
						chart.setColor(point, c);
					}
				}

			}
		}
		chart.getCharter().setSuspendDrawing(false);
		chart.getCharter().setSuspendChartCalculations(false);
		resetMessagePaneText();
		chart.munch();
		contentsChanged();
		messagePane.repaint();
	}

	/* ................................................................................................................. */
	public void recalcChart() {
		if (!recalcChartInit())
			return;
		addPointsToChart(0, values.getSize());

	}

	/* ................................................................................................................. */
	int counter = 0;

	private void adjustComponentSizes() {
		windowHeight = getHeight();
		windowWidth = getWidth();
		chart.setChartSize(windowWidth - chartInsetRight - chartInsetLeft, windowHeight - chartInsetTop - chartInsetBottom);
		chart.setLocation(chartInsetLeft, chartInsetTop);
		messagePane.setLocation(0, windowHeight - chartInsetBottom);
		messagePane.setSize(windowWidth, chartInsetBottom);
		if (scrollPanel != null)
			scrollPanel.setLocation(0, windowHeight - chartInsetBottom - scrollPanel.getBounds().height - chartInsetTop);
		recalcButton.setSize(recalcButton.getPreferredSize());
	}

	/* ................................................................................................................. */
	public void windowResized() {
		super.windowResized();
		if (chart == null || messagePane == null)
			return;
		if ((getHeight() != windowHeight) || (getWidth() != windowWidth) || (chart.getChartHeight() != windowHeight - chartInsetTop - chartInsetBottom) || (chart.getChartWidth() != windowWidth - chartInsetRight - chartInsetLeft)) {
			adjustComponentSizes();
		}
		recalcButton.setSize(recalcButton.getPreferredSize());
	}
}

/* ========================================================= */
class ChartMessages extends TextArea {
	ItemsChartWindow window;

	public ChartMessages(ItemsChartWindow window) {
		super("", 3, 3, TextArea.SCROLLBARS_VERTICAL_ONLY);
		this.window = window;
	}

	public void setText(String t) {
		super.setText(t);
		// setSelectionStart(0);
		// setSelectionEnd(0);
	}
}


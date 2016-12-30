/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.Histogram;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/*TODO:
 * 	- fix menu checking of Cumulative submenu
 * - check code for cumulative, with examples
 * - averageItem enabling extension fully to cumulative
 */

/* ======================================================================== */
/* A module drawing bar charts.  The chart represents a value for each of a series of items (trees, characters, etc.).   There are two
orientations: (1) values (y) by items (x) which shows the items in sequence along the x axis, and the value for each on the y and (2)
number of items by values which shows the possible values on the x axis, and the frequency of occurence of the values on the y axis.
The x axis can have a single bar for each item (native mode = no grouping) or can have each bar summarize a series of items (grouping).  When
items are thus "grouped" or "clumped" on the x axis, the clumping may be done by number of intervals or by interval width.  

The labels "x" and "y" could refer either to the horizontal and vertical pixels of the chart's field, or to the values displayed in the chart (e.g.
treelength of 100, item number 10, etc.). To limit confusion xPixel and yPixel are used in some cases when the variables refer to the pixel values.*/

public class Histogram extends DrawChart {
	/*.................................................................................................................*/
	public String getName() {
		return "Bar & Line Chart";
	}
	public String getExplanation() {
		return "Helps make bar & line charts." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(HistogramAssistantA.class, "Bar & Line Charts can optionally display extra information (such as means, percentiles).",
		"You can request such extra information under the Analysis submenu of the Chart menu of a Bar & Line Chart.");
	}
	/*.................................................................................................................*/
	boolean suspend = false;
	Vector charters;
	int oldIntervals = 20;
	double oldIntervalSize =1.0;
	double oldMovingWindowWidth = 5;
	double oldMovingWindowIncrement = 1.0;
	MesquiteMenuItemSpec averageItem, percentItem;
	MesquiteSubmenuSpec  cumulativeSubmenu;
	MesquiteMenuItemSpec barChartItem;
	MesquiteBoolean showAverage; //for clumped items, show average or total?
	int cumulativeMode;
	MesquiteBoolean showAsBarChart;
	MesquiteBoolean showRaw;  // in text view, show clumped data or raw data?
	int mode = HistogramCharter.AUTO;  //native mode, or clumped, or let the module choose automatically?
	int useMode;
	MesquiteBoolean sizeToFit = new MesquiteBoolean(true);
	MesquiteCommand sizeDrawingCommand;
	MesquiteMenuItemSpec sizeItem;
	MesquiteString modeName;
	MesquiteString cumulativeModeName;
	int totalFieldWidth= MesquiteInteger.unassigned;
	int totalFieldHeight= MesquiteInteger.unassigned;
	MesquiteBoolean showPercent = new MesquiteBoolean(false);
	boolean noSum = false;

	Vector holding;
	boolean showMenus = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		charters = new Vector();
		holding = new Vector();
		showAverage = new MesquiteBoolean(true);
		cumulativeMode = HistogramCharter.NO_CUMULATIVE;
		showRaw = new MesquiteBoolean(false);
		showAsBarChart = new MesquiteBoolean(true);
		if (condition instanceof MesquiteBoolean[]){
			MesquiteBoolean[] specs = (MesquiteBoolean[])condition;
			for (int i=0; i<specs.length; i++){
				if ("ShowMenus".equalsIgnoreCase(specs[i].getName()))
					showMenus = specs[i].getValue();
				else if ("showAsBarChart".equalsIgnoreCase(specs[i].getName()))
					showAsBarChart.setValue(specs[i].getValue());
				else if ("showAsCumulative".equalsIgnoreCase(specs[i].getName())){
					if (specs[i].getValue())
						cumulativeMode = HistogramCharter.CUMULATIVE;
				}
				else if ("setNativeMode".equalsIgnoreCase(specs[i].getName())){
					if (specs[i].getValue()){
						mode = HistogramCharter.NATIVE;
					}
				}
				else if ("setNoSum".equalsIgnoreCase(specs[i].getName())){
					if (specs[i].getValue()){
						noSum = true;
					}
				}
			}
		}

		modeName = new MesquiteString(getModeName(mode));
		cumulativeModeName = new MesquiteString(getCumulativeModeName(cumulativeMode));
		if (showMenus){
			MesquiteSubmenuSpec groupingSubmenu = addSubmenu(null, "Grouping on X");
			addItemToSubmenu(null, groupingSubmenu, getModeName(HistogramCharter.AUTO), makeCommand("setAutoMode", this));
			addItemToSubmenu(null, groupingSubmenu, getModeName(HistogramCharter.NATIVE), makeCommand("setNativeMode", this));
			addItemToSubmenu(null, groupingSubmenu, getModeName(HistogramCharter.NUM_INTERVALS), makeCommand("setNumberIntervals", this));
			addItemToSubmenu(null, groupingSubmenu, getModeName(HistogramCharter.WIDTH_INTERVALS), makeCommand("setIntervalSize", this));
			addItemToSubmenu(null, groupingSubmenu, getModeName(HistogramCharter.MOVING_WINDOW), makeCommand("setMovingWindowSize", this));
			groupingSubmenu.setSelected(modeName);
			/*
		REMOVED as not working well, especially axes labels and printing
		addCheckMenuItem(null, "Size To Window", MesquiteModule.makeCommand("toggleSizeToFit",  this), sizeToFit);
		sizeDrawingCommand = MesquiteModule.makeCommand("sizeDrawing",  this);
		if (!sizeToFit.getValue())
			sizeItem = addMenuItem("Drawing Size...", sizeDrawingCommand);
			 */
			MesquiteSubmenuSpec analysis = addSubmenu(null, "Auxiliary Analysis", makeCommand("newAssistant", this), HistogramAssistantA.class);
			MesquiteSubmenuSpec mCloseAsst = addSubmenu(null, "Close Auxiliary Analysis");
			addMenuSeparator();
			mCloseAsst.setList(getEmployeeVector());
			mCloseAsst.setListableFilter(HistogramAssistantA.class);
			mCloseAsst.setCommand(makeCommand("closeAssistant",  this));
			averageItem = addCheckMenuItem(null, "Show Average for Group", MesquiteModule.makeCommand("showAverageToggle",  this), showAverage);
			percentItem = addCheckMenuItem(null, "Show Percentage", MesquiteModule.makeCommand("showPercentToggle",  this), showPercent);
			cumulativeSubmenu = addSubmenu(null, "Cumulative");
			addItemToSubmenu(null, cumulativeSubmenu, getCumulativeModeName(HistogramCharter.NO_CUMULATIVE), makeCommand("setNoCumulative", this));
			addItemToSubmenu(null, cumulativeSubmenu, getCumulativeModeName(HistogramCharter.CUMULATIVE), makeCommand("setCumulative", this));
			addItemToSubmenu(null, cumulativeSubmenu, getCumulativeModeName(HistogramCharter.CUMULATIVE_AVERAGE), makeCommand("setCumulativeAverage", this));
			addItemToSubmenu(null, cumulativeSubmenu, getCumulativeModeName(HistogramCharter.CUMULATIVE_DOWN), makeCommand("setCumulativeDown", this));
			addItemToSubmenu(null, cumulativeSubmenu, getCumulativeModeName(HistogramCharter.CUMULATIVE_AVERAGE_DOWN), makeCommand("setCumulativeAverageDown", this));
			cumulativeSubmenu.setSelected(cumulativeModeName);
			//			cumulativeItem = addCheckMenuItem(null, "Show Cumulative", MesquiteModule.makeCommand("showCumulativeToggle",  this), showCumulative);
			barChartItem = addCheckMenuItem(null, "Show as Bar Chart", MesquiteModule.makeCommand("showAsBarChartToggle",  this), showAsBarChart);
			addCheckMenuItem(null, "Show Individual Points in Text View", MesquiteModule.makeCommand("showRawToggle",  this), showRaw);
			addMenuSeparator();
		}
		return true;
	}
	/*.................................................................................................................*/
	private void addAssistantToCharter(ChartAssistant tda, HistogramCharter tCO){

		if (tCO.chart!=null){
			ChartExtra tce = tda.createExtra(tCO.chart); //HOW TO REMOVE IF QUIT???
			tCO.chart.addExtra(tce);
			tce.doCalculations();
			tCO.chart.getField().repaint();
			tCO.chart.repaint();
		}
	}
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public void addAssistant(ChartAssistant tda) {
		Enumeration e = charters.elements();
		if (charters.size()==0) {
			holding.addElement(tda);
		}
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof HistogramCharter) {
				HistogramCharter tCO = (HistogramCharter)obj;
				addAssistantToCharter(tda, tCO);
			}
		}
		if (!MesquiteThread.isScripting())
			parametersChanged();
	}
	/*.................................................................................................................*/
	public String getCumulativeModeName(int m){
		if (m == HistogramCharter.NO_CUMULATIVE)
			return "Off";
		else if (m == HistogramCharter.CUMULATIVE)
			return "Simple Cumulative";
		else if (m == HistogramCharter.CUMULATIVE_AVERAGE)
			return "Average Cumulative";
		else if (m == HistogramCharter.CUMULATIVE_DOWN)
			return "Reverse Simple Cumulative";
		else if (m == HistogramCharter.CUMULATIVE_AVERAGE_DOWN)
			return "Reverse Average Cumulative";
		else return "";
	}
	/*.................................................................................................................*/
	public String getModeName(int m){
		if (m == HistogramCharter.AUTO)
			return "Automatic";
		else if (m == HistogramCharter.NATIVE)
			return "No Grouping";
		else if (m == HistogramCharter.NUM_INTERVALS)
			return "Fixed number of groups...";
		else if (m == HistogramCharter.WIDTH_INTERVALS)
			return "Fixed group width...";
		else if (m == HistogramCharter.MOVING_WINDOW)
			return "Moving window...";
		else return "";
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suspend");
		temp.addLine("showAverageToggle " + showAverage.toOffOnString()); 
		temp.addLine("showPercentToggle " + showPercent.toOffOnString()); 
		temp.addLine("setCumulativeMode " + cumulativeMode); 
		temp.addLine("showAsBarChartToggle " + showAsBarChart.toOffOnString()); 
		temp.addLine("showRawToggle " + showRaw.toOffOnString()); 
		temp.addLine("setNumberIntervals " + oldIntervals);  
		temp.addLine("setIntervalSize " + oldIntervalSize); 
		temp.addLine("setMovingWindowSize " + oldMovingWindowWidth + " " + oldMovingWindowIncrement); 
		temp.addLine("setMode " + mode);
		/*
		temp.addLine("toggleSizeToFit " + sizeToFit.toOffOnString());
		if (!sizeToFit.getValue() && (MesquiteInteger.isCombinable(totalFieldWidth)||MesquiteInteger.isCombinable(totalFieldHeight))) {
			temp.addLine("sizeDrawing " + MesquiteInteger.toString(totalFieldWidth) + " " + MesquiteInteger.toString(totalFieldHeight));
		}
		 */
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof HistogramAssistantA) {
				temp.addLine("\tnewAssistant " , ((MesquiteModule)e));
			}
		}
		temp.addLine("resume");
		return temp;
	}

	/*.................................................................................................................*/
	void switchCumulativeMode(int m, boolean suspend){
		cumulativeMode = m;
		Enumeration e = charters.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof HistogramCharter) {
				HistogramCharter tCO = (HistogramCharter)obj;
				tCO.setCumulativeMode(cumulativeMode, suspend);
			}
		}
		cumulativeModeName.setValue(getCumulativeModeName(cumulativeMode));
		//cumulativeSubmenu.setSelected(cumulativeModeName);
		//if (!MesquiteThread.isScripting())
		if (!suspend)
			parametersChanged();
		resetContainingMenuBar();
	}


	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		boolean resetMode = false;
		if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspend = true;
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspend = false;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the mode of the chart", "[0 = native; 1= fixed number of groups; 2= groups of fixed width; 3= moving window]", commandName, "setMode")) {
			int m  = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(m)) {
				mode = m;
				Enumeration e = charters.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof HistogramCharter) {
						HistogramCharter tCO = (HistogramCharter)obj;
						tCO.setMode(mode, suspend);
					}
				}
				modeName.setValue(getModeName(mode));
				//if (!MesquiteThread.isScripting())
				if (!suspend)
					parametersChanged();
				resetContainingMenuBar();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the chart is sized to fit into the field", "[on= size to fit;  off]", commandName, "toggleSizeToFit")) {
			sizeToFit.toggleValue(parser.getFirstToken(arguments));
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setSizeToFit(sizeToFit.getValue());

				}
			}
			if (showMenus){
				if (!sizeToFit.getValue())
					sizeItem = addMenuItem("Drawing Size...", sizeDrawingCommand);
				else if (sizeItem!=null)
					deleteMenuItem(sizeItem);
			}
			resetContainingMenuBar();
		}
		else if (checker.compare(this.getClass(), "Sets the size of the drawing (appropriate if not size to fit)", "[width in pixels][height in pixels]", commandName, "sizeDrawing")) {
			pos.setValue(0);
			int w = MesquiteInteger.fromString(arguments, pos);
			int h = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(h)  || MesquiteInteger.isCombinable(w)) {
				if (w>10 && h>10) {
					Enumeration e = charters.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						if (obj instanceof HistogramCharter) {
							HistogramCharter tCO = (HistogramCharter)obj;
							tCO.setTotalField(w,h);
						}
					}
					totalFieldHeight = h;
					totalFieldWidth = w;
				}
			}
			else { 
				MesquiteBoolean answer = new MesquiteBoolean(false);
				MesquiteInteger newWidth = new MesquiteInteger(totalFieldWidth);
				MesquiteInteger newHeight =new MesquiteInteger(totalFieldHeight);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Size of chart drawing", "Width (Pixels)",  "Height (Pixels)", answer,  newWidth, newHeight,10,MesquiteInteger.unassigned,10, MesquiteInteger.unassigned,MesquiteString.helpString);
				if (answer.getValue() &&( (newWidth.isCombinable()&&newWidth.getValue()>10) || (newHeight.isCombinable() && newHeight.getValue()>10))) {
					Enumeration e = charters.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						if (obj instanceof HistogramCharter) {
							HistogramCharter tCO = (HistogramCharter)obj;
							tCO.setTotalField(newWidth.getValue(),newHeight.getValue());
						}
					}
					totalFieldHeight = newHeight.getValue();
					totalFieldWidth = newWidth.getValue();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the number of groups (appropriate if mode is Fixed Number of Groups)", "[number of groups]", commandName, "setNumberIntervals")) {
			int intervals  = MesquiteInteger.fromFirstToken(arguments, pos);
			resetMode = false;
			if (!MesquiteInteger.isCombinable(intervals)) {
				intervals = MesquiteInteger.queryInteger(containerOfModule(), "Groups", "Number of groups on X axis:", oldIntervals, 0, 1000);
				if (!MesquiteThread.isScripting() && mode!=HistogramCharter.NUM_INTERVALS)
					resetMode = true;
			}
			if (!MesquiteInteger.isCombinable(intervals)  || intervals <= 0)
				return null;

			oldIntervals = intervals;
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setIntervals(intervals);
					if (resetMode)
						tCO.setMode(HistogramCharter.NUM_INTERVALS, suspend);
				}
			}
			if (resetMode)
				modeName.setValue(getModeName(HistogramCharter.NUM_INTERVALS));
			if (showMenus){
				averageItem.setEnabled(MesquiteInteger.isCombinable(intervals));
				cumulativeSubmenu.setEnabled(MesquiteInteger.isCombinable(intervals));
				percentItem.setEnabled(MesquiteInteger.isCombinable(intervals));

			}
			MesquiteTrunk.resetMenuItemEnabling();

			if (!MesquiteThread.isScripting())
				parametersChanged();
			resetContainingMenuBar();

		}
		else if (checker.compare(this.getClass(),  "Sets the width of intervals (appropriate if mode is Fixed Group Width)", "[width of groups]", commandName, "setIntervalSize")) {
			double s  = MesquiteDouble.fromString(ParseUtil.getFirstToken(arguments, pos));
			resetMode = false;
			if (!MesquiteDouble.isCombinable(s)) {
				s = MesquiteDouble.queryDouble(containerOfModule(), "Groups", "Width of group on X axis:", oldIntervalSize, 0, 1000);
				if (mode!=HistogramCharter.WIDTH_INTERVALS) {
					resetMode = true;
					mode = HistogramCharter.WIDTH_INTERVALS;
				}
			}
			if (!MesquiteDouble.isCombinable(s)  || s <= 0)
				return null;
			oldIntervalSize = s;
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setIntervalSize(s);
					if (resetMode)
						tCO.setMode(HistogramCharter.WIDTH_INTERVALS, suspend);
				}
			}
			if (resetMode)
				modeName.setValue(getModeName(HistogramCharter.WIDTH_INTERVALS));
			if (showMenus){
				averageItem.setEnabled(true);
				cumulativeSubmenu.setEnabled(true);
				percentItem.setEnabled(true);
			}
			MesquiteTrunk.resetMenuItemEnabling();
			if (!MesquiteThread.isScripting())  //WAYNE:??? why is this here
				if (!suspend)
					parametersChanged();
			resetContainingMenuBar();
		}
		else if (checker.compare(this.getClass(),  "Sets the size of the moving window (appropriate if mode is Moving Window)", "[Moving window size]", commandName, "setMovingWindowSize")) {
			pos.setValue(0);
			double sWidth = MesquiteDouble.fromString(arguments, pos);
			double sIncrement  = MesquiteDouble.fromString(arguments, pos);
			resetMode = false;
			if (!MesquiteDouble.isCombinable(sIncrement) || !MesquiteDouble.isCombinable(sWidth)) {
				MesquiteDouble sZ = new MesquiteDouble(oldMovingWindowWidth);
				MesquiteDouble sR = new MesquiteDouble(oldMovingWindowIncrement);
				MesquiteBoolean answer = new  MesquiteBoolean(false);

				MesquiteString str1 = new MesquiteString(sZ.toString());
				MesquiteString str2 = new MesquiteString(sR.toString());
				new TwoStringsDialog(containerOfModule(), "Moving Window", "Width of Moving Window (units on X axis):", "Increment of Moving Window:", answer, str1, str2,false);
				sZ.setValue(MesquiteDouble.fromString(str1.getValue()));
				sR.setValue(MesquiteDouble.fromString(str2.getValue())); 
				if (answer.getValue()) {
					if (!sZ.isCombinable() || !sR.isCombinable()) {
						return null;
					}
					sIncrement = sR.getValue();
					sWidth = sZ.getValue();
					oldMovingWindowIncrement = sIncrement;
					oldMovingWindowWidth = sWidth;
					if (mode!=HistogramCharter.MOVING_WINDOW)
						resetMode = true;
				}
				else
					return null;
			}

			oldMovingWindowWidth = sWidth;
			oldMovingWindowIncrement = sIncrement;

			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setMovingWindowSize(sIncrement, sWidth);
					if (resetMode)
						tCO.setMode(HistogramCharter.MOVING_WINDOW, suspend);
				}
			}

			if (resetMode) {
				modeName.setValue(getModeName(HistogramCharter.MOVING_WINDOW));
				if (!MesquiteThread.isScripting())
					if (!suspend)
						parametersChanged();
				resetContainingMenuBar();
			}
			else
				if (!suspend)
					parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets to automatic grouping mode", null, commandName, "setAutoMode")) {
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setMode(HistogramCharter.AUTO, suspend);
				}
			}
			if (showMenus){
				if (averageItem.isEnabled())
					averageItem.setEnabled(false);
				if (cumulativeSubmenu.isEnabled())
					cumulativeSubmenu.setEnabled(false);
				if (percentItem.isEnabled())
					percentItem.setEnabled(false);
			}
			MesquiteTrunk.resetMenuItemEnabling();
			modeName.setValue(getModeName(HistogramCharter.AUTO));
			if (!MesquiteThread.isScripting())
				if (!suspend)
					parametersChanged();
			resetContainingMenuBar();
		}
		else if (checker.compare(this.getClass(), "Sets to native mode", null, commandName, "setNativeMode")) {
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setMode(HistogramCharter.NATIVE, suspend);
				}
			}
			if (showMenus){
				if (averageItem.isEnabled())
					averageItem.setEnabled(false);
				if (cumulativeSubmenu.isEnabled())
					cumulativeSubmenu.setEnabled(false);
				if (percentItem.isEnabled())
					percentItem.setEnabled(false);
			}
			MesquiteTrunk.resetMenuItemEnabling();
			modeName.setValue(getModeName(HistogramCharter.NATIVE));
			if (!MesquiteThread.isScripting())
				if (!suspend)
					parametersChanged();
			resetContainingMenuBar();
		}

		else if (checker.compare(this.getClass(), "For orientation 1, shows average of each category", "[on or off]", commandName, "showAverageToggle")) {
			showAverage.toggleValue(arguments);
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setShowAverage(showAverage.getValue(), suspend);
				}
			}
			if (!MesquiteThread.isScripting())
				if (!suspend)
					parametersChanged();

		}
		else if (checker.compare(this.getClass(), "For orientation 1, shows precent of each category", "[on or off]", commandName, "showPercentToggle")) {
			showPercent.toggleValue(arguments);
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setShowPercent(showPercent.getValue(), suspend);
				}
			}
			if (!MesquiteThread.isScripting())
				if (!suspend)
					parametersChanged();

		}
		else if (checker.compare(this.getClass(), "Sets the cumulative mode of the chart", "[0 = off; 1= simple cumulative; 2= cumulative average]", commandName, "setCumulativeMode")) {
			int m  = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(m)) {					
				switchCumulativeMode(m, suspend);
			}
		}
		else if (checker.compare(this.getClass(), "For orientation 1, turns off cumulative values", "[on or off]", commandName, "setNoCumulative")) {
			switchCumulativeMode(HistogramCharter.NO_CUMULATIVE, suspend);
		}
		else if (checker.compare(this.getClass(), "For orientation 1, shows cumulative values", "[on or off]", commandName, "setCumulative")) {
			switchCumulativeMode(HistogramCharter.CUMULATIVE, suspend);
		}
		else if (checker.compare(this.getClass(), "For orientation 1, shows cumulative average values", "[on or off]", commandName, "setCumulativeAverage")) {
			switchCumulativeMode(HistogramCharter.CUMULATIVE_AVERAGE, suspend);
		}
		else if (checker.compare(this.getClass(), "For orientation 1, shows cumulative values in reverse", "[on or off]", commandName, "setCumulativeDown")) {
			switchCumulativeMode(HistogramCharter.CUMULATIVE_DOWN, suspend);
		}
		else if (checker.compare(this.getClass(), "For orientation 1, shows cumulative average values in reverse", "[on or off]", commandName, "setCumulativeAverageDown")) {
			switchCumulativeMode(HistogramCharter.CUMULATIVE_AVERAGE_DOWN, suspend);
		}
		else if (checker.compare(this.getClass(), "Show as bar chart or as line chart", "[on or off]", commandName, "showAsBarChartToggle")) {
			showAsBarChart.toggleValue(arguments);
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof HistogramCharter) {
					HistogramCharter tCO = (HistogramCharter)obj;
					tCO.setShowAsBarChart(showAsBarChart.getValue());
				}
			}
			if (!MesquiteThread.isScripting())
				if (!suspend)
					parametersChanged();

		}
		else if (checker.compare(this.getClass(), "Shows individual points in text view", "[on or off]", commandName, "showRawToggle")) {
			showRaw.toggleValue(arguments);
			if (!MesquiteThread.isScripting())
				if (!suspend)
					parametersChanged();

		}
		/**/
		else if (checker.compare(this.getClass(), "Hires a chart assistant module", "[name of assistant module]", commandName, "newAssistant")) {
			incrementMenuResetSuppression();
			HistogramAssistantA tda= (HistogramAssistantA)hireNamedEmployee(HistogramAssistantA.class, arguments);
			if (tda!=null) {
				addAssistant(tda);
				if (!MesquiteThread.isScripting()) {
					Enumeration e = charters.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						if (obj instanceof HistogramCharter) {
							HistogramCharter tCO = (HistogramCharter)obj;
							tCO.chart.munch();
						}
					}
					resetContainingMenuBar();
				}
				decrementMenuResetSuppression();
				return tda;
			}
			else {
				decrementMenuResetSuppression();
				return findEmployeeWithName(parser.getFirstToken(arguments));
			}
		}
		else if (checker.compare(this.getClass(), "Closes an assistant module", "[number of assistant module]", commandName, "closeAssistant")) {
			EmployeeVector ev = getEmployeeVector();
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(which))
				return null;
			int count =0;
			for (int i=0; i< ev.size(); i++){
				MesquiteModule mb = (MesquiteModule)ev.elementAt(i);
				if (mb!=null && mb instanceof HistogramAssistantA) {
					if (count== which) {
						fireEmployee(mb);
						Enumeration e = charters.elements();
						while (e.hasMoreElements()) {
							Object obj = e.nextElement();
							if (obj instanceof HistogramCharter) {
								HistogramCharter tCO = (HistogramCharter)obj;
								tCO.chart.removeAllExtrasOwned(mb);
								tCO.chart.getField().repaint();
								tCO.chart.repaint();
							}
						}
						return null;
					}
					count++;
				}
			}
		}
		else {
			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
	}

	/*.................................................................................................................*/
	public   Charter createCharter(ChartListener listener) {
		Charter c = new HistogramCharter(this, listener);
		charters.addElement(c);
		return c;
	}


	Selectionable pointsAssociable = null;
	boolean areParts = false;
	/* tells the module whether points are parts of an Associable object */
	public void pointsAreSelectable(boolean areParts, Selectionable a, boolean allowSequenceOptions){
		if (this.areParts!=areParts || pointsAssociable!=a){
			if (pointsAssociable!=null)
				pointsAssociable.removeListener(this);
			this.areParts = areParts;
			pointsAssociable =a;
			if (!areParts)
				pointsAssociable = null;
			if (pointsAssociable!=null)
				pointsAssociable.addListener(this);
			syncSel(false);
			resetContainingMenuBar();
		}
	}

	/* Synchronize selection of points in chart with parts of associable*/
	void syncSel(boolean recalc){
		Enumeration e = charters.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof HistogramCharter) {
				HistogramCharter tCO = (HistogramCharter)obj;
				tCO.synchronizeSelection(true);
				if (recalc){
					tCO.calculateChart(tCO.chart);
					tCO.chart.calculateAllExtras();
					tCO.chart.getField().repaint();
				}
			}
		}
	}
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if ((obj==pointsAssociable || obj instanceof Selectionable) && (code==MesquiteListener.PARTS_MOVED ||code == MesquiteListener.SELECTION_CHANGED || code == MesquiteListener.PARTS_DELETED || code == MesquiteListener.PARTS_ADDED)) {
			syncSel(true);
		}
	}
	public void endJob(){
		if (pointsAssociable!=null)
			pointsAssociable.removeListener(this);
		super.endJob();
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		if (mode == HistogramCharter.NUM_INTERVALS)
			return "(X axis grouped into " + oldIntervals + " intervals)"; 
		else if (mode == HistogramCharter.WIDTH_INTERVALS)
			return "(X axis grouped into intervals of size " + oldIntervalSize + ")"; 
		else if (mode == HistogramCharter.MOVING_WINDOW)
			return "(Moving window of width " + MesquiteDouble.toString(oldMovingWindowWidth) + " every " + MesquiteDouble.toString(oldMovingWindowIncrement) + " units)"; 
		else if (mode == HistogramCharter.AUTO) {
			if (useMode == HistogramCharter.WIDTH_INTERVALS)
				return "(Automatic grouping; Most recently used grouping:  X axis grouped into " + oldIntervals + " intervals)"; 
			else if (useMode == HistogramCharter.NATIVE)
				return "(Automatic grouping; Most recently used grouping:  X axis ungrouped)"; 
		}
		return "";
	}
}
/* ======================================================================== */
class HistogramCharter  extends Charter{
	MesquiteNumber valueX;
	MesquiteNumber valueY;
	MesquiteNumber totalY = new MesquiteNumber(0);
	MesquiteNumber totalX = new MesquiteNumber(0);

	Histogram ownerModule;
	MesquiteMenuItemSpec intervalsItem;
	boolean showAverage = true;
	int cumulativeMode = NO_CUMULATIVE;
	boolean showAsBarChart = true;
	int mode = AUTO;
	int useMode = mode;

	public static final int AUTO = 4;
	public static final int NATIVE = 0;
	public static final int NUM_INTERVALS = 1;
	public static final int WIDTH_INTERVALS = 2;
	public static final int MOVING_WINDOW = 3;

	public static final int NO_CUMULATIVE=0;
	public static final int CUMULATIVE=1;
	public static final int CUMULATIVE_AVERAGE=2;
	public static final int CUMULATIVE_DOWN=3;
	public static final int CUMULATIVE_AVERAGE_DOWN=4;
	int numIntervalsUsed = 0;
	int numIntervalsSet = 0;
	double intervalSize = 0;
	double movingWindowWidth = 1;
	double movingWindowIncrement = 1;
	int originalMarkerWidth = 4;
	int minTicks = 3;
	MesquiteNumber[] clump; //the value (height) for each of the clumps
	int[] numInClump; //how many data points are represented in each clump
	double[] clumpPropSel; //the portion of the clump whose items are currently selected
	static final int MAXCLUMPCOLORS = 10;
	Color[][] clumpColors;  //how are the items in the clump colored? (gray if mixed)
	int[][] clumpColorNumbers;  //how are the items in the clump colored? (gray if mixed)
	int numSelected = 0;  //how many items are selected
	MesquiteChart chart;
	ChartListener listener;
	int calculated = 0;
	boolean showPercent = true;

	public HistogramCharter (Histogram ownerModule, ChartListener listener) {	
		this.ownerModule = ownerModule;
		showAverage = ownerModule.showAverage.getValue();
		showPercent = ownerModule.showPercent.getValue();
		cumulativeMode = ownerModule.cumulativeMode;
		showAsBarChart = ownerModule.showAsBarChart.getValue();
		this.listener = listener;
		mode = ownerModule.mode;
		numIntervalsSet = ownerModule.oldIntervals;
		valueX = new MesquiteNumber();
		valueY = new MesquiteNumber();
		clump = new MesquiteNumber[numIntervalsSet]; //+1
		for (int i=0; i<numIntervalsSet; i++) //+1
			clump[i] = new MesquiteNumber(0);
		synchronizeSelection(true);
	}
	public void open(MesquiteChart chart){
		ownerModule.incrementMenuResetSuppression();
		this.chart = chart;
		chart.setXAxisEdge(98); //MAKE IT DEPEND ON FONT SIZE  WAS 64
		chart.setYAxisEdge(56); //MAKE IT DEPEND ON FONT SIZE
		chart.setXPixelBase(0);
		chart.setYPixelBase(0);
		chart.setSizeToFit(true);
		for (int i = 0; i<ownerModule.holding.size(); i++)
			addAssistant((ChartAssistant)(ownerModule.holding.elementAt(i)));
		ownerModule.decrementMenuResetSuppression();
	}
	public void close(){
	}
	void addAssistant(ChartAssistant tda){
		if (chart!=null){
			ChartExtra tce = tda.createExtra(chart); //HOW TO REMOVE IF QUIT???
			chart.addExtra(tce);
			tce.doCalculations();
			chart.getField().repaint();
			chart.repaint();
		}
	}
	public void setTotalField(int w, int h) {
		chart.setTotalField(w,h);
		chart.getField().repaint();
	}
	public void setSizeToFit(boolean b) {
		chart.setSizeToFit(b);
		if (!b){
			chart.setTotalFieldHeight(ownerModule.totalFieldHeight);
			chart.setTotalFieldWidth(ownerModule.totalFieldWidth);
		}
	}
	public boolean getShowPercent(){
		return (canShowPercentage() && chart.getCharter()!=null && ownerModule.showPercent.getValue());
	}
	public void setShowAverage(boolean show, boolean suspend) {
		showAverage = show;
		if (!suspend) {
			calculateChart(chart);
			chart.getField().repaint();
			chart.repaint();
		}
	}
	public void setShowPercent(boolean show, boolean suspend) {
		showPercent = show;
		if (!suspend) {
			calculateChart(chart);
			chart.getField().repaint();
			chart.repaint();
		}
	}
	public void setCumulativeMode(int m, boolean suspend) {
		cumulativeMode = m;
		if (!suspend) {
			calculateChart(chart);
			chart.getField().repaint();
			chart.repaint();
		}
	}
	public void setShowAsBarChart(boolean show) {
		showAsBarChart = show;
		chart.getField().repaint();
		chart.repaint();
	}
	public void setMode(int m, boolean suspend) {
		mode = m; 
		ownerModule.mode = m;
		if (!MesquiteThread.isScripting() && ! suspend){
			calculateChart(chart);
			chart.getField().repaint();
			chart.repaint();
		}
		else calculated = 0;
	}
	public void setIntervals(int numIntervals) {
		this.numIntervalsSet = numIntervals;
		clump = new MesquiteNumber[numIntervalsSet];//+1
		for (int i=0; i<numIntervalsSet; i++)//+1
			clump[i] = new MesquiteNumber(0);
		if (clumpColors!=null) {
			clumpColors = new Color[numIntervalsSet][MAXCLUMPCOLORS];//+1
			clumpColorNumbers = new int[numIntervalsSet][MAXCLUMPCOLORS];
		}
	}
	public void setIntervalSize(double intervalSize) {
		this.intervalSize = intervalSize;
	}

	/* ----------------------------------*/
	public String getYAxisNameSuffix(){
		if (cumulativeMode==NO_CUMULATIVE)
			return "";
		else
			return "("+ownerModule.getCumulativeModeName(cumulativeMode) + ")";
	}

	/* ----------------------------------*/
	public void setMovingWindowSize(double movingWindowIncrement, double movingWindowWidth) {
		this.movingWindowWidth = movingWindowWidth;
		this.movingWindowIncrement = movingWindowIncrement;
	}
	/* ----------------------------------*/
	void select(int which){
		if (ownerModule.pointsAssociable!=null)  
			ownerModule.pointsAssociable.setSelected(which, true);
		else
			chart.selectPoint(which);

	}
	void deselect(int which){
		if (ownerModule.pointsAssociable!=null)  
			ownerModule.pointsAssociable.setSelected(which, false);
		else
			chart.deselectPoint(which);
	}
	void selectInterval(int interval){
		int[] points = findPointsInInterval(interval, chart);
		if (points !=null)
			for (int i=0; i<points.length; i++)
				select(points[i]);
	}
	void deselectAll(){
		if (ownerModule.pointsAssociable!=null)  
			ownerModule.pointsAssociable.deselectAll();
		else
			chart.deselectAllPoints(); 
	}
	boolean isSelected(int which){
		if (ownerModule.pointsAssociable!=null)  
			return ownerModule.pointsAssociable.getSelected(which);
		else
			return chart.getSelected().isBitOn(which);
	}
	boolean anySelected(){
		if (ownerModule.pointsAssociable!=null)  
			return ownerModule.pointsAssociable.anySelected();
		else
			return chart.getSelected().anyBitsOn();
	}
	/*	It would be nice to have shift click have contiguous selections in contrast to command/control click, but given that the
x axis can represent different things (items, values, values by category) it is difficult to implement.  Here is a beginning.
	void shrinkWrap(boolean byPoints){
		if (byPoints || useMode == NATIVE){
			int min = minSelPoint();
			if (min>=0) {
				int max = maxSelPoint();
				for (int i = min; i<=max; i++) {
					select(i);
				}
			}
		}
		else {
			int min = minSelInterval();
			if (min>=0) {
				int max = maxSelInterval();
				for (int i = min; i<=max; i++) {
					selectInterval(i);
				}
			}
		}	
	}
	int minSelPoint(){
		for (int i = 0; i<chart.getNumPoints(); i++)
			if (isSelected(i))
				return i;
		return -1;
	}
	int maxSelPoint(){
		for (int i = chart.getNumPoints()-1; i>=0; i--)
			if (isSelected(i))
				return i;
		return -1;
	}
	int minSelInterval(){
		for (int interval = 0; interval<numIntervalsint[] points = findPointsInInterval(interval, chart);
		if (points !=null)
			for (int i=0; i<points.length; i++)
				select(points[i]);
		return -1;
	}
	int maxSelInterval(){
		return -1;
	}
	 */
	// handles cleanup after selection changed
	void wrapUpSelection(){
		if (ownerModule.pointsAssociable!=null)  {
			if (!ownerModule.pointsAssociable.amIListening(ownerModule)) //in case data matrix didn't exist when connected, do this just in case
				ownerModule.pointsAssociable.addListener(ownerModule);
			ownerModule.pointsAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
		else {
			calculateChart(chart);
			chart.calculateAllExtras();//extras need to recalculate in case they depended on selection
			chart.getField().repaint();
		}
	}
	/**/
	//for selection dragging
	void drawVerticalLine(Graphics g, int x){
		int y1 = 1;
		int y2 = chart.getField().getBounds().height-1;
		g.drawLine(x, y1, x, y2);
	}
	void drawRectByCorners(Graphics g, int x1, int y1, int x2, int y2){
		if (x1>x2){
			if (y1>y2)
				g.drawRect(x2, y2, x1-x2, y1-y2);
			else 
				g.drawRect(x2, y1, x1-x2, y2-y1);
		}
		else {
			if (y1>y2)
				g.drawRect(x1, y2, x2-x1, y1-y2);
			else 
				g.drawRect(x1, y1, x2-x1, y2-y1);
		}

	}
	void fillRectByCorners(Graphics g, int x1, int y1, int x2, int y2){
		if (x1>x2){
			if (y1>y2)
				g.fillRect(x2, y2, x1-x2, y1-y2);
			else 
				g.fillRect(x2, y1, x1-x2, y2-y1);
		}
		else {
			if (y1>y2)
				g.fillRect(x1, y2, x2-x1, y1-y2);
			else 
				g.fillRect(x1, y1, x2-x1, y2-y1);
		}
	}
	long numClicks = 0;
	/* ---------------------------------- */
	private int findExactXValue(int xPixel, int yPixel, MesquiteChart chart){  
		if (xPixel<0)
			xPixel =0;
		return xPixel;
	}
	/* ----------------------------------*/
	public boolean isNative(){
		return useMode==NATIVE;
	}
	/* ---------------------------------- */
	private int findIntervalOfX(MesquiteNumber valueX, MesquiteChart chart){
		if (useMode == NATIVE || (useMode ==NUM_INTERVALS && numIntervalsSet == 0) || (useMode ==WIDTH_INTERVALS && intervalSize == 0)) {
			return -1; //intervals don't exist
		}
		else if (useMode == NUM_INTERVALS) {//grouped into intervals
			if (!valueX.isCombinable())
				return -1;
			else if (valueX.equals(chart.getAxisMaximumX())){ //maximum value would be right on edge of highest interval
				return numIntervalsSet-1;
			}
			else if (valueX.isMoreThan(chart.getAxisMaximumX())){ //maximum value would be right on edge of highest interval
				return -1;
			}
			int whichInterval = valueX.findInterval(chart.getMinimumX(), chart.getMaximumX(), numIntervalsSet);
			if (whichInterval == numIntervalsSet)
				whichInterval = numIntervalsSet-1;
			return whichInterval;
		}
		else if (useMode ==WIDTH_INTERVALS) {
			return valueX.findInterval(chart.getMinimumX(), new MesquiteNumber(intervalSize));
		}
		return -1;
	}
	/* ----------------------------------*/
	public MesquiteNumber leftmostXDrawn(MesquiteChart chart){
		return new MesquiteNumber(chart.getMinimumX());
	}
	/* ----------------------------------*/
	public MesquiteNumber rightmostXDrawn(MesquiteChart chart){
		if (useMode == NATIVE) {
			MesquiteNumber xMax= new MesquiteNumber();
			int xPixel = xToPixel(chart.getMaximumX().getDoubleValue(), chart);
			pixelToX(xPixel+markerWidth, chart, xMax);
			return xMax;
		}
		double xMin = chart.getMinimumX().getDoubleValue();
		double xIntervalWidth = getIntervalWidth();
		return new MesquiteNumber(xIntervalWidth*(numIntervalsUsed) + xMin);
	}
	/* ----------------------------------*/
	public MesquiteNumber rightmostXOfInterval(MesquiteNumber x, MesquiteChart chart){
		if (x == null)
			return null;
		if (useMode == NATIVE) {
			MesquiteNumber xMax= new MesquiteNumber();
			int xPixel = xToPixel(x.getDoubleValue(), chart);
			pixelToX(xPixel+markerWidth, chart, xMax);
			return xMax;
		}
		int i = findIntervalOfX(x, chart);
		if (i<0)
			return null;
		double xMin = chart.getMinimumX().getDoubleValue();
		double xIntervalWidth = getIntervalWidth();
		return new MesquiteNumber(xIntervalWidth*(i+1) + xMin);
	}
	/* ----------------------------------*/
	public MesquiteNumber leftmostXOfInterval(MesquiteNumber x, MesquiteChart chart){
		if (x == null)
			return null;
		if (useMode == NATIVE) {
			MesquiteNumber xMax= new MesquiteNumber();
			int xPixel = xToPixel(x.getDoubleValue(), chart);
			pixelToX(xPixel, chart, xMax);
			return xMax;
		}
		int i = findIntervalOfX(x, chart);
		if (i<0)
			return null;
		double xMin = chart.getMinimumX().getDoubleValue();
		double xIntervalWidth = getIntervalWidth();
		return new MesquiteNumber(xIntervalWidth*(i) + xMin);
	}
	/* ----------------------------------*/
	public boolean moreThanOneValueInInterval(MesquiteNumber x, MesquiteChart chart) {
		if (x == null)
			return false;
		if (useMode == NATIVE) {
			return false;
		}
		if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items   (y by x)
			int interval = findIntervalOfX(x, chart);
			if (interval<0)
				return false;
			int[] points = findPointsInInterval (interval, chart);
			if (points !=null) {
				NumberArray xArray = chart.getXArray();
				double firstValue = MesquiteDouble.unassigned;
				for (int i=0; i<points.length; i++) {
					if (i==0)
						firstValue = xArray.getDouble(points[i]);
					else if (firstValue!=MesquiteDouble.unassigned) {
						if (firstValue != xArray.getDouble(points[i])) {
							return true;
						}
					}
				}
			}
		}
		else {  // not yet allowed
		}

		return false;
	}
	/* ----------------------------------*/
	public MesquiteNumber bottommostYOfInterval(MesquiteNumber y, MesquiteChart chart){
		return y;  //eventually need to be redone for useMode!=NATIVE
	}
	/* ----------------------------------*/
	public MesquiteNumber topmostYOfInterval(MesquiteNumber y, MesquiteChart chart){
		return y;  //eventually need to be redone for useMode!=NATIVE
	}
	/* ----------------------------------*/
	public MesquiteNumber topmostYDrawn(MesquiteChart chart){
		if (useMode == NATIVE) {
			return new MesquiteNumber(chart.getMaximumY());
		}
		else {
			MesquiteNumber yM = new MesquiteNumber();

			for (int i = 0; i<numInClump.length; i++)
				yM.setMeIfIAmLessThan(numInClump[i]);
			return yM;

		}
	}
	/* ----------------------------------*/
	public MesquiteNumber bottommostYDrawn(MesquiteChart chart){
		if (useMode == NATIVE) {
			return new MesquiteNumber(chart.getMinimumY());
		}
		return null;
	}
	/* ---------------------------------- */
	private int findIntervalOfPixel(int xPixel, MesquiteChart chart, boolean report){
		if (xPixel<0)
			xPixel =0;

		if (useMode == NATIVE) {
			MesquiteNumber xMin= new MesquiteNumber();
			MesquiteNumber xMax= new MesquiteNumber();

			pixelToX(xPixel-markerWidth, chart, xMin);
			pixelToX(xPixel, chart, xMax);


			NumberArray xArray = chart.getXArray();
			long numFound=0;
			int found = -1;
			for (int i=0; i<chart.getNumPoints(); i++){
				xArray.placeValue(i, tempNum);
				if (tempNum!=null && tempNum.isMoreThan(xMin) && tempNum.isLessThan(xMax)) {
					numFound++;
					found = i;
				}
			}
			if (numFound==1)
				return found;
			else if (numFound>0) {
				long target = numClicks % numFound;
				for (int i=0; i<chart.getNumPoints(); i++){
					xArray.placeValue(i, tempNum);
					if (tempNum!=null && tempNum.isMoreThan(xMin) && tempNum.isLessThan(xMax)) {
						if (target<=0)
							return i;
						target--;
					}
				}
			}
			return -1;
		}
		else {
			MesquiteNumber valueX= new MesquiteNumber();
			pixelToX(xPixel, chart, valueX);
			if (valueX.isCombinable()) 
				return findIntervalOfX(valueX, chart);//covers all cases except edges, e.g. on right side valueX could be unassigned 

			//check to see if in last interval
			double xIntervalWidth = getIntervalWidth(); 
			double leftX = xIntervalWidth*(numIntervalsUsed-1) + chart.getMinimumX().getDoubleValue();
			int minPixel = xToPixel(leftX, chart);
			int maxPixel = xToPixel(xIntervalWidth + leftX, chart); //minPixel+markerWidth;
			if ((xPixel>=minPixel && xPixel<= maxPixel)) {
				return numIntervalsUsed-1;
			}
			return -1;
		}
	}

	//finds points greater than pixel x and less than pixel x2 (lessthan or equal x2 if boolean is true)
	private int[] findXValuesWithinPixels(int x, int y, int x2, int y2, boolean includeUpperEdge, MesquiteChart chart){  
		MesquiteNumber xMin= new MesquiteNumber();
		MesquiteNumber xMax= new MesquiteNumber();
		MesquiteNumber tempNum= new MesquiteNumber();
		if (x2<x){
			int te = x;
			x=x2;
			x2=te;
		}
		pixelToX(x, chart, xMin);
		pixelToX(x2, chart, xMax);
		NumberArray xArray = chart.getXArray();

		int count =0;
		for (int i=0; i<chart.getNumPoints(); i++){
			xArray.placeValue(i, tempNum);
			if (tempNum!=null && tempNum.isMoreThan(xMin) && ((includeUpperEdge && !tempNum.isMoreThan(xMax)) || (!includeUpperEdge && tempNum.isLessThan(xMax)))) {
				count++;
			}
		}
		if (count>0) {
			int[] found = new int[count];
			count = 0;
			for (int i=0; i<chart.getNumPoints(); i++){
				xArray.placeValue(i, tempNum);
				if (tempNum!=null && tempNum.isMoreThan(xMin) &&  ((includeUpperEdge && !tempNum.isMoreThan(xMax)) || (!includeUpperEdge && tempNum.isLessThan(xMax)))) {
					found[count++] = i;
				}
			}
			return found;
		}
		return null;
	}
	//finds points greater than x and less than x2 (lessthan or equal x2 if boolean is true)
	private int[] findPointsInInterval(int interval, MesquiteChart chart){  
		NumberArray xArray = chart.getXArray();
		MesquiteNumber valueX= new MesquiteNumber();

		int count =0;
		for (int i= 0; i<chart.getNumPoints(); i++) {
			if (!xArray.isUnassigned(i)) {
				chart.getXArray().placeValue(i, valueX);
				if (findIntervalOfX(valueX, chart) == interval)
					count++;
			}
		}

		if (count>0) {
			int[] found = new int[count];
			count = 0;
			for (int i= 0; i<chart.getNumPoints(); i++) {
				chart.getXArray().placeValue(i, valueX);
				int fi = findIntervalOfX(valueX, chart);
				if (fi == interval) 
					found[count++] = i;

			}
			return found;
		}
		return null;
	}
	/* ----------------------------------*/
	int pointEntered = -1;
	public void mouseMoveInField(int modifiers, int xPixel, int yPixel, MesquiteChart chart, ChartTool tool) {
		whichDown = findIntervalOfPixel(xPixel, chart, false);
		if (pointEntered >=0 && whichDown!=pointEntered) {  //had been in other point recently; exit it
			ListableVector extras = chart.getExtras();
			for (int i=0; i<extras.size(); i++) {
				((ChartExtra)extras.elementAt(i)).cursorExitPoint(pointEntered, findExactXValue(xPixel,yPixel, chart),null);
			}
		}
		if (whichDown>=0) {
			if (whichDown!=pointEntered){
				ListableVector extras = chart.getExtras();
				for (int i=0; i<extras.size(); i++) {
					((ChartExtra)extras.elementAt(i)).cursorEnterPoint(whichDown, findExactXValue(xPixel,yPixel, chart), null);
				}
			}
			pointEntered = whichDown;
		}
		else
			pointEntered = -1;
	}
	/* ----------------------------------*/
	int whichDown = MesquiteInteger.unassigned;
	int xDown = MesquiteInteger.unassigned;
	int yDown = MesquiteInteger.unassigned;
	int xDrag = MesquiteInteger.unassigned;
	int yDrag = MesquiteInteger.unassigned;
	/* ----------------------------------*/
	public double totalClumpValues() {
		if (clump!=null) {
			MesquiteNumber count =new MesquiteNumber(0);
			for (int i=0; i<clump.length; i++) {
				count.add(clump[i]);
			}
			return count.getDoubleValue();
		}
		return 0.0;
	}
	/* ----------------------------------*/
	public double fractionBelow(int whichDown) {
		if (clump!=null && whichDown<clump.length) {
			MesquiteNumber count =new MesquiteNumber(0);
			for (int i=0; i<whichDown; i++) {
				count.add(clump[i]);
			}
			return count.getDoubleValue()/totalClumpValues();
		}
		return 0.0;
	}
	/* ----------------------------------*/
	public double fractionAbove(int whichDown) {
		if (clump!=null && whichDown<clump.length) {
			MesquiteNumber count =new MesquiteNumber(0);
			for (int i=whichDown+1; i<clump.length; i++) {
				count.add(clump[i]);
			}
			return count.getDoubleValue()/totalClumpValues();
		}
		return 0.0;
	}

	public boolean showingClumpSums(){
		if (chart.getOrientation() == 0)
			return false;
		return !(chart.getUseAverage() || showAverage || (cumulativeMode>NO_CUMULATIVE) || useMode == NATIVE || useMode == MOVING_WINDOW);
	}
	public boolean canShowPercentage(){
		return !((useMode == NATIVE || (useMode ==NUM_INTERVALS && numIntervalsSet == 0) || (useMode ==WIDTH_INTERVALS && intervalSize == 0)));
	}
	/* ----------------------------------*/
	public void mouseDownInField(int modifiers, int xPixel, int yPixel, MesquiteChart chart, ChartTool tool) {
		numClicks++;
		whichDown = findIntervalOfPixel(xPixel,chart, true);
		if (tool !=null && tool != chart.getArrowTool() && tool != chart.getInfoTool()) {
			if (whichDown>=0){
				tool.pointTouched(whichDown, xPixel, yPixel,modifiers);
				return;
			}
		}

		//arrow tool; start selection indication	
		if (tool == chart.getArrowTool()){
			xDown = xPixel;
			yDown = yPixel;
			xDrag=xPixel;
			yDrag=yPixel;
			ListableVector extras = chart.getExtras();
			for (int i=0; i<extras.size(); i++) {
				((ChartExtra)extras.elementAt(i)).cursorTouchPoint(whichDown, findExactXValue(xPixel,yPixel, chart), null);
			}
			return;
		}

		//info tool
		if (whichDown>=0 && listener !=null) {
			if (useMode == NATIVE){
				if (chart.getYArray()!=null && chart.getXArray()!=null) {
					NumberArray xs = chart.getXArray();
					NumberArray ys = chart.getYArray();
					xs.placeValue(whichDown, valueX);
					ys.placeValue(whichDown, valueY);
					double sum = 0;
					if (ownerModule.noSum)
						sum = valueY.getDoubleValue();
					else
						sum = sumSame(xs, ys, -1, valueX, null);
					valueY.setValue(sum);
					listener.pointMouseDown(chart, whichDown, valueX, valueY, xPixel, yPixel, modifiers, null);
				}
				else
					listener.pointMouseDown(chart, whichDown, null, null, xPixel, yPixel, modifiers, null);
			}
			else if (whichDown>=0 && clump!=null && whichDown<clump.length) {
				String message;
				if ((chart.getUseAverage() || (showAverage && chart.getOrientation()==1)) || useMode == MOVING_WINDOW)
					message = "Average " + clump[whichDown] + "\n (of " + numInClump[whichDown] + " items with X values ranging\n" +  describeEdges(whichDown) + ")";
				else {
					message = "Sum " + clump[whichDown] + "\n (of items with X values ranging\n" +  describeEdges(whichDown) + ")";
					message += "\nFraction < this:\n" + MesquiteDouble.toString(fractionBelow(whichDown));
					message += "\nFraction > this:\n " + MesquiteDouble.toString(fractionAbove(whichDown));
				}
				if (chart.getYArray()!=null) {
					chart.getYArray().placeValue(whichDown, valueY);
					listener.pointMouseDown(chart, MesquiteInteger.unassigned, null, valueY, xPixel, yPixel, modifiers, message);
				}
				else
					listener.pointMouseDown(chart, MesquiteInteger.unassigned, null, null, xPixel, yPixel, modifiers, message);
			}
		}
		ListableVector extras = chart.getExtras();
		for (int i=0; i<extras.size(); i++) {
			((ChartExtra)extras.elementAt(i)).cursorTouchPoint(whichDown, findExactXValue(xPixel,yPixel, chart), null);
		}
	}
	/* ----------------------------------*/
	public void mouseDragInField(int modifiers, int xPixel, int yPixel, MesquiteChart chart, ChartTool tool) {
		if (tool == chart.getArrowTool()){ 
			Graphics g=chart.getField().getGraphics();
			if (g!=null){
				g.setXORMode(Color.white);
				g.setColor(Color.black);
				drawVerticalLine(g, xDrag);
				drawVerticalLine(g, xPixel);
				xDrag=xPixel;
				yDrag=yPixel;
				g.dispose();
			}
		}
	}
	/* ----------------------------------*/
	public void mouseUpInField(int modifiers, int xPixel, int yPixel, MesquiteChart chart, ChartTool tool) {
		//if (MesquiteInteger.isNonNegative(whichDown)) { // had touched directly on point before
		if (tool !=null && tool != chart.getArrowTool()&& tool != chart.getInfoTool()) {
			if (whichDown>=0){
				tool.pointDropped(whichDown, xPixel, yPixel,modifiers);
				return;
			}
		}	
		int which = findIntervalOfPixel(xPixel, chart, false); //this is interval if nonnative
		if (listener!=null)
			listener.pointMouseUp(chart, which, xPixel, yPixel, modifiers, null);
		if (tool == chart.getArrowTool()){
			Graphics g=chart.getField().getGraphics();
			if (g!=null){
				g.setXORMode(Color.white);
				g.setColor(Color.black);
				drawVerticalLine(g, xDown);
				drawVerticalLine(g, xDrag);
				g.dispose();
			}
			//single point selected
			if (useMode == NATIVE && which == whichDown && MesquiteInteger.isNonNegative(which)) {
				if (MesquiteEvent.commandKeyDown(modifiers) || MesquiteEvent.shiftKeyDown(modifiers)) {
					if (isSelected(which))
						deselect(which);
					else
						select(which);
				}
				else {
					if (anySelected())
						deselectAll();
					select(which);
				}
				wrapUpSelection();

				/**/
			}
			else  {
				int[] whichPoints;
				if (which == whichDown && MesquiteInteger.isNonNegative(which)){ //click and up on same interval
					double xMin = chart.getMinimumX().getDoubleValue();
					double xIntervalWidth = getIntervalWidth();
					whichPoints = findPointsInInterval(which, chart);
				}
				else {
					whichPoints = findXValuesWithinPixels(xPixel,yPixel,xDown, yDown, false, chart);
				}

				if (whichPoints!=null && whichPoints.length>0) {

					if (!MesquiteEvent.shiftKeyDown(modifiers) && !MesquiteEvent.commandKeyDown(modifiers))
						deselectAll();
					for (int i = 0; i< whichPoints.length; i++){
						which = whichPoints[i];
						if (MesquiteEvent.commandKeyDown(modifiers) || MesquiteEvent.shiftKeyDown(modifiers)) {
							if (isSelected(which))
								deselect(which);
							else
								select(which);
						}
						else if (!isSelected(which)) {
							select(which);
						}
					}
					wrapUpSelection();


				}
				else if (chart.getSelected().anyBitsOn()){

					deselectAll();
					wrapUpSelection();

				}
			}
		}
		//}
		xDown = MesquiteInteger.unassigned;
		yDown = MesquiteInteger.unassigned;
		whichDown =MesquiteInteger.unassigned;
	}
	LittlePanel quickPanel;
	public void showQuickMessage(MesquiteChart chart, int whichPoint,int xPixel, int yPixel,  String message){
		int boxWidth = 96;
		if (StringUtil.blank(message))
			return;
		if (quickPanel==null) {
			chart.add(quickPanel = new LittlePanel());
			quickPanel.setBackground(ColorDistribution.lightGreen);
		}
		quickPanel.setText(message, chart.getField().getFont(), boxWidth);
		int offsetRight = 12;
		StringInABox sb = quickPanel.getBox();
		int xOffset = chart.getField().getBounds().x;
		int yOffset = chart.getField().getBounds().y;
		int xPos=0;
		if (xPixel+boxWidth+8 + offsetRight>chart.getField().getBounds().width) 
			xPos = xPixel-boxWidth + xOffset;
		else
			xPos = xPixel+4 + xOffset + offsetRight;
		int yPos=0;

		if (yPixel+sb.getHeight()>chart.getField().getBounds().height + yOffset && yPixel-sb.getHeight() + yOffset>=0)
			yPos = yPixel-sb.getHeight() + yOffset;
		else
			yPos = yPixel + yOffset;
		quickPanel.setSize(boxWidth +2, sb.getHeight() +2);
		quickPanel.setLocation(xPos, yPos);
		quickPanel.setVisible(true);
		quickPanel.repaint();

	}
	/* ----------------------------------*/
	public void hideQuickMessage(MesquiteChart chart){
		if (quickPanel!=null)
			quickPanel.setVisible(false);
	}
	/* ----------------------------------*/
	private void findEdges(int interval, MesquiteNumber minInterval, MesquiteNumber maxInterval){ 
		minInterval.setToUnassigned();
		maxInterval.setToUnassigned();
		if (chart.getMinimumX()==null || chart.getMaximumX() == null)
			return;
		if (useMode == NATIVE || (useMode ==NUM_INTERVALS && numIntervalsSet == 0) || (useMode ==WIDTH_INTERVALS && intervalSize == 0)) {
		}
		else if (useMode ==NUM_INTERVALS) {
			tempNum.setValue(chart.getMaximumX());
			tempNum.subtract(chart.getMinimumX());
			tempNum.divideBy(numIntervalsSet);

			minInterval.setValue(tempNum);
			minInterval.multiplyBy(interval);
			minInterval.add(chart.getMinimumX());

			maxInterval.setValue(tempNum);
			maxInterval.multiplyBy(interval+1);
			maxInterval.add(chart.getMinimumX());
		}
		else if (useMode == WIDTH_INTERVALS) {
			minInterval.setValue(intervalSize);
			minInterval.multiplyBy(interval);
			minInterval.add(chart.getMinimumX());

			maxInterval.setValue(intervalSize);
			maxInterval.multiplyBy(interval+1);
			maxInterval.add(chart.getMinimumX());
		}
		else if (useMode == MOVING_WINDOW) {
			tempNum.setValue(chart.getMaximumX());  //what about movingWindowIncrement
			tempNum.subtract(chart.getMinimumX());
			tempNum.divideBy(numIntervalsUsed);

			minInterval.setValue(interval);
			minInterval.multiplyBy(movingWindowIncrement);
			minInterval.add(chart.getMinimumX());

			maxInterval.setValue(minInterval);
			maxInterval.add(movingWindowWidth);
		}
	}
	/* ---------------------------------- */
	private String describeEdges(int interval){
		MesquiteNumber minInterval=new MesquiteNumber();
		MesquiteNumber maxInterval=new MesquiteNumber();
		if (useMode == NATIVE || (useMode ==NUM_INTERVALS && numIntervalsSet == 0) || (useMode ==WIDTH_INTERVALS && intervalSize == 0)) {
			//"item " + interval
			minInterval.setToUnassigned();
			maxInterval.setToUnassigned();
			return "Item " + interval;
		}
		else {
			if (chart.getMinimumX()==null || chart.getMaximumX() == null)
				return "no";
			findEdges(interval, minInterval, maxInterval);
			if (interval == numIntervalsUsed-1)
				return "from " + minInterval + "\n to " + maxInterval;
			else
				return "from " + minInterval + "\n to less than " + maxInterval;
		}
	}
	/* ----------------------------------*/
	void processClumps(int numIntervals) {
		if (clump!=null && clump.length>0){
			MesquiteNumber total = new MesquiteNumber(0);
			if (showPercent) {
				for (int i= 0; i<numIntervals; i++)
					total.add(clump[i]);
			}
			switch (cumulativeMode) {
			case CUMULATIVE: {
				for (int i= 1; i<numIntervals; i++)
					clump[i].add(clump[i-1]);
				break;
			}
			case CUMULATIVE_AVERAGE: {
				for (int i= 1; i<numIntervals; i++)
					clump[i].add(clump[i-1]);
				int count = 0;
				for (int i= 0; i<numIntervals; i++){
					count++;
					clump[i].divideBy(count);
				}
				break;
			}
			case CUMULATIVE_DOWN: {
				for (int i= numIntervals-2; i>=0; i--)
					clump[i].add(clump[i+1]);
				break;
			}
			case CUMULATIVE_AVERAGE_DOWN: {
				for (int i= numIntervals-2; i>=0; i--)
					clump[i].add(clump[i+1]);
				int count = 0;
				for (int i= numIntervals-1; i>=0; i--) {
					count++;
					clump[i].divideBy(count);
				}
				break;
			}
			}
			if (showPercent) {
				for (int i= 0; i<numIntervals; i++) {
					clump[i].divideBy(total);
					clump[i].multiplyBy(100);
				}
			}
			minY.setValue(clump[0]);
			maxY.setValue(clump[0]);
			for (int i= 0; i<numIntervals; i++) {
				maxY.setMeIfIAmLessThan(clump[i]);
				minY.setMeIfIAmMoreThan(clump[i]);
			}
		}
	}
	/* ----------------------------------*/
	public  void calculateChart(MesquiteChart chart){
		if (minY==null || maxY==null || chart==null)
			return;
		if (getSuspendChartCalculations())
			return;
		calculated = 1;

		minX.setValue(chart.getAxisMinimumX()); 
		maxX.setValue(chart.getAxisMaximumX());
		minY.setToUnassigned();
		maxY.setToUnassigned();
		boolean currentAverageIsEnabled = false;
		//	if (ownerModule.percentItem !=null)
		//		ownerModule.percentItem.setEnabled(canShowPercentage());
		if (ownerModule.averageItem !=null)
			ownerModule.averageItem.isEnabled();
		if (currentAverageIsEnabled && chart.getOrientation() == 0) {
			if (ownerModule.averageItem !=null)
				ownerModule.averageItem.setEnabled(false);
			MesquiteTrunk.resetMenuItemEnabling();
			ownerModule.resetContainingMenuBar();
		}
		useMode = mode;
		if (mode == AUTO) {
			if (chart.getOrientation() == 1) {   //orientation stored in chart as 0 = items by values; 1 = values by items
				if (chart.getMaximumX().getDoubleValue() - chart.getMinimumX().getDoubleValue()<15)
					useMode = NATIVE;
				else {
					useMode =WIDTH_INTERVALS;
					intervalSize = 1 + (int)((chart.getMaximumX().getDoubleValue() - chart.getMinimumX().getDoubleValue())/100);
				}
			}
			else {
				if (chart.getXArray().getValueClass() == NumberArray.DOUBLE){
					useMode =NUM_INTERVALS;
					numIntervalsSet = 50;
				}
				else {
					if (chart.getMaximumX().getDoubleValue() - chart.getMinimumX().getDoubleValue()<15)
						useMode = NATIVE;
					else {
						useMode =WIDTH_INTERVALS;
						intervalSize = 1 + (int)((chart.getMaximumX().getDoubleValue() - chart.getMinimumX().getDoubleValue())/100);
					}
				}
			}
		}
		ownerModule.useMode = useMode;
		boolean someSelected = anySelected();
		if (ownerModule.percentItem !=null)
			ownerModule.percentItem.setEnabled(canShowPercentage());

		if (useMode == NATIVE || (useMode ==NUM_INTERVALS && numIntervalsSet == 0) || (useMode ==WIDTH_INTERVALS && intervalSize == 0)) {
			if (currentAverageIsEnabled) {
				if (ownerModule.averageItem !=null)
					ownerModule.averageItem.setEnabled(false);
				MesquiteTrunk.resetMenuItemEnabling();
				ownerModule.resetContainingMenuBar();
			}
			minY.setValue(chart.getAxisMinimumY());
			maxY.setValue(chart.getAxisMaximumY());

			// calculate totals and divide by these for percent display
			if (getShowPercent()) {
				totalX.setValue(0.0);
				totalY.setValue(0.0);
				for (int i= 0; i<chart.getNumPoints(); i++) {   // calculate total for percentages
					if (getSuspendChartCalculations())
						return;
					if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
						chart.getXArray().placeValue(i, valueX);
						totalX.add(valueX);
						chart.getYArray().placeValue(i, valueY);
						totalY.add(valueY);
					}
				}
				totalX.divideBy(100.0);
				totalY.divideBy(100.0);
				chart.getYArray().switchToDoubles();
				for (int i= 0; i<chart.getNumPoints(); i++) {   
					if (getSuspendChartCalculations())
						return;
					if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
						chart.getYArray().divideBy(i, totalY);
					}
				}
			}

			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (getSuspendChartCalculations())
					return;
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					chart.getXArray().placeValue(i, valueX);
					NumberArray xs = chart.getXArray();
					if (!anySameLeft(xs, i, valueX)){
						double sum = 0;
						if (ownerModule.noSum)
							sum = chart.getYArray().getDouble(i);
						else
							sum = sumSame(xs, chart.getYArray(), i, valueX, null);
						maxY.setMeIfIAmLessThan(sum);
					}

				}
			}
		}
		else if (useMode ==NUM_INTERVALS) {
			numIntervalsUsed = numIntervalsSet;
			int expandedNumIntervals = numIntervalsUsed; // + 1;
			if (!currentAverageIsEnabled && chart.getOrientation() == 1) {
				if (ownerModule.averageItem !=null)
					ownerModule.averageItem.setEnabled(true);
				MesquiteTrunk.resetMenuItemEnabling();
				ownerModule.resetContainingMenuBar();
			}
			if (clump ==null || chart.getXArray()==null || chart.getYArray() == null)
				return;
			prepareClumps(expandedNumIntervals, someSelected, (chart.getUseAverage() || (showAverage && chart.getOrientation()==1)));

			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (getSuspendChartCalculations())
					return;
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					chart.getXArray().placeValue(i, valueX);
					chart.getYArray().placeValue(i, valueY);
					int whichInterval;

					if (valueX.equals(chart.getAxisMaximumX()))
						whichInterval = numIntervalsUsed-1;
					else {
						whichInterval = valueX.findInterval(chart.getMinimumX(), chart.getMaximumX(), numIntervalsUsed);
						if (whichInterval == numIntervalsUsed)
							whichInterval = numIntervalsUsed-1;
					}
					if (whichInterval>=clump.length || whichInterval <0){
						MesquiteMessage.warnProgrammer("Error in histogram: i " + i + " whichInterval " + whichInterval + " clump.length " + clump.length +" chart.getMinimumX() " + chart.getMinimumX()  +" chart.getMaximumY() " + chart.getMaximumY() + " valueX " + valueX);
					}
					else {
						if (someSelected && clumpPropSel!=null && whichInterval<clumpPropSel.length && isSelected(i) && valueY.isCombinable())
							clumpPropSel[whichInterval] += valueY.getDoubleValue();
						clump[whichInterval].add(valueY);

						if (chart.colorsExist() && chart.getColor(i) != null) {
							if (clumpColors == null){
								clumpColors = new Color[expandedNumIntervals][MAXCLUMPCOLORS];
								clumpColorNumbers = new int[expandedNumIntervals][MAXCLUMPCOLORS];
							}
							Color cat = chart.getColor(i);
							addToClumpColors(whichInterval, cat);
						}
						if ((chart.getUseAverage() || (showAverage && chart.getOrientation()==1)))
							numInClump[whichInterval]++;
					}
				}
			}
			if (someSelected && clumpPropSel!=null) {
				for (int i= 0; i<expandedNumIntervals; i++) {
					if (clump[i].getDoubleValue()>0) {
						clumpPropSel[i] /= clump[i].getDoubleValue();
					}
				}
			}
			if ((chart.getUseAverage() || (showAverage && chart.getOrientation()==1))) {
				for (int i= 0; i<expandedNumIntervals; i++) {
					if (numInClump[i]>0) {
						clump[i].divideBy(numInClump[i]);
					}
				}
			}
			processClumps(expandedNumIntervals);
			minY.setMeIfIAmMoreThan(chart.getAxisMinimumY()); //In case constrained
		}
		else if (useMode == WIDTH_INTERVALS) {
			if (!currentAverageIsEnabled && chart.getOrientation() == 1) {
				if (ownerModule.averageItem !=null)
					ownerModule.averageItem.setEnabled(true);
				MesquiteTrunk.resetMenuItemEnabling();
				ownerModule.resetContainingMenuBar();
			}
			numIntervalsUsed = (int)(0.99999999999+(chart.getMaximumX().getDoubleValue()-chart.getMinimumX().getDoubleValue())/intervalSize) + 1;
			if (numIntervalsUsed <0)
				numIntervalsUsed = 0;
			prepareClumps(numIntervalsUsed, someSelected, (chart.getUseAverage() || (showAverage && chart.getOrientation()==1)));

			MesquiteNumber nI = new MesquiteNumber(intervalSize);

			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (getSuspendChartCalculations())
					return;
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					chart.getXArray().placeValue(i, valueX);
					chart.getYArray().placeValue(i, valueY);
					int whichInterval = valueX.findInterval(chart.getMinimumX(), nI);
					if (whichInterval < 0)
						MesquiteMessage.warnProgrammer("Negative interval in chart; min = " + chart.getMinimumX() + "  valueX = " + valueX);
					else {
						clump[whichInterval].add(valueY); 
						if (someSelected && clumpPropSel!=null && whichInterval<clumpPropSel.length && isSelected(i) && valueY.isCombinable())
							clumpPropSel[whichInterval] += valueY.getDoubleValue();

						if (chart.colorsExist() && chart.getColor(i) != null) {
							Color cat = chart.getColor(i);
							addToClumpColors(whichInterval, cat);
						}
						if ((chart.getUseAverage() || (showAverage && chart.getOrientation()==1))) {
							if (whichInterval >= numInClump.length || whichInterval < 0)
								MesquiteMessage.warnProgrammer("Error in histogram: interval not found.  whichInterval " + whichInterval + " numInclump.length " + numInClump.length);
							else
								numInClump[whichInterval]++;
						}
					}
				}
			}
			if (someSelected && clumpPropSel!=null) {
				for (int i= 0; i<numIntervalsUsed; i++) {
					if (clump[i].getDoubleValue()>0) {
						clumpPropSel[i] /= clump[i].getDoubleValue();
					}
				}
			}
			if ((chart.getUseAverage() || (showAverage && chart.getOrientation()==1))) {
				for (int i= 0; i<numIntervalsUsed; i++) {
					if (numInClump[i]>0) {
						clump[i].divideBy(numInClump[i]);
					}
				}
			}
			processClumps(numIntervalsUsed);
			minY.setMeIfIAmMoreThan(chart.getAxisMinimumY()); //In case constrained
		}
		else if (useMode == MOVING_WINDOW) {
			if (!currentAverageIsEnabled && chart.getOrientation() == 1) {
				if (ownerModule.averageItem !=null)
					ownerModule.averageItem.setEnabled(true);
				MesquiteTrunk.resetMenuItemEnabling();
				ownerModule.resetContainingMenuBar();
			}
			boolean useSum = (chart.getOrientation()==0); //don't average over moving window if items by values
			numIntervalsUsed = (int)(0.99999999999+(chart.getMaximumX().getDoubleValue()-chart.getMinimumX().getDoubleValue())/movingWindowIncrement) +1;
			if (numIntervalsUsed<0)
				numIntervalsUsed = 0;
			//preparing clumps for as many intervals as dictated by movingWindowIncrement
			prepareClumps(numIntervalsUsed, someSelected, true);

			MesquiteNumber sum  = new MesquiteNumber(0);
			MesquiteNumber startInterval = new MesquiteNumber();
			MesquiteNumber endInterval = new MesquiteNumber();
			// first, go through all the intervals, in each finding who is inside its window
			for (int whichInterval= 0; whichInterval<numIntervalsUsed; whichInterval++) { 
				if (getSuspendChartCalculations())
					return;
				numInClump[whichInterval] =0;
				if (someSelected && clumpPropSel!=null && whichInterval<clumpPropSel.length)
					clumpPropSel[whichInterval]  = 0.0;
				sum.setValue(0);
				double sumSel = 0.0;
				startInterval.setValue(chart.getAxisMinimumX().getDoubleValue());
				startInterval.add(movingWindowIncrement * whichInterval);
				endInterval.setValue(startInterval.getDoubleValue() + movingWindowWidth);
				for (int i= 0; i<chart.getNumPoints(); i++) {
					if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
						chart.getXArray().placeValue(i, valueX);
						chart.getYArray().placeValue(i, valueY);

						// this value is in the window
						if (valueX.isLessThan(endInterval) && !valueX.isLessThan(startInterval)){
							numInClump[whichInterval]++;
							sum.add(valueY); 
							if (someSelected && isSelected(i) && valueY.isCombinable())
								sumSel += valueY.getDoubleValue();

							if (chart.colorsExist() && chart.getColor(i) != null) {
								Color cat = chart.getColor(i);
								addToClumpColors(whichInterval, cat);
							}
						}
					}
				}
				//the sum in the window is now stored  in sum
				if (numInClump[whichInterval] == 0)
					clump[whichInterval].setValue(0.0);
				else {
					if (useSum)
						clump[whichInterval].setValue(numInClump[whichInterval]);
					else
						clump[whichInterval].setValue(sum.getDoubleValue()/numInClump[whichInterval]);
					if (sum.getDoubleValue()!=0.0 && someSelected && clumpPropSel!=null && whichInterval<clumpPropSel.length)
						clumpPropSel[whichInterval] = sumSel/sum.getDoubleValue();
				}
			}
			processClumps(numIntervalsUsed);
			minY.setMeIfIAmMoreThan(chart.getAxisMinimumY()); //In case constrained
		}
		if (maxY.isCombinable()){
			maxY.setValue(maxY.getDoubleValue()*1.1);
		}
		numSelected = 0;
		if (someSelected)
			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					if (isSelected(i))
						numSelected++;
				}
			}

		calculated = 2;
	}

	int whichColor(Color[] colors, Color c){
		if (c == null || colors == null)
			return -1;
		for (int i = 0; i< colors.length; i++)
			if (c.equals(colors[i]))
				return i;
		//not found; determine next open slot, but last slot is prohibited
		for (int i = 0; i< colors.length-1; i++)
			if (colors[i] == null)
				return i;

		return -1;
	}
	void addToClumpColors(int whichInterval, Color cat){

		if (cat == null){
			return;
		}
		int colorFound = whichColor(clumpColors[whichInterval], cat);
		if (colorFound == -1){
			clumpColors[whichInterval][MAXCLUMPCOLORS-1] = Color.darkGray;
			colorFound = MAXCLUMPCOLORS-1;
		}
		else if (clumpColors[whichInterval][colorFound] == null) //not yet assigned
			clumpColors[whichInterval][colorFound] = cat;
		clumpColorNumbers[whichInterval][colorFound]++;
	}
	/* ----------------------------------*/
	void prepareClumps(int numIntervals, boolean someSelected, boolean numInClumpAlso){
		//preparing clumps for as many intervals as dictated by movingWindowIncrement
		if (clump == null || clump.length != numIntervals ) {
			clump = new MesquiteNumber[numIntervals];
			for (int i=0; i<numIntervals; i++) {
				clump[i] = new MesquiteNumber(0);
			}
		}
		for (int i= 0; i<clump.length; i++) 
			clump[i].setValue(0);
		if (numInClumpAlso){
			if (numInClump == null || numInClump.length != numIntervals )
				numInClump = new int[numIntervals];
			for (int i=0; i<numIntervals; i++)
				numInClump[i] = 0;
		}
		if (clumpColors == null || clumpColors.length != numIntervals){
			clumpColors = new Color[numIntervals][MAXCLUMPCOLORS];
			clumpColorNumbers = new int[numIntervals][MAXCLUMPCOLORS];
		}
		for (int i=0; i<numIntervals; i++)
			for (int k = 0; k< MAXCLUMPCOLORS; k++){
				clumpColors[i][k] = null;
				clumpColorNumbers[i][k] = 0;
			}

		if (someSelected) {
			if (clumpPropSel == null || clumpPropSel.length != numIntervals) {
				clumpPropSel = new double[numIntervals];
				for (int i=0; i<numIntervals; i++) 
					clumpPropSel[i] = 0;
			}
			else
				for (int i= 0; i<clumpPropSel.length; i++) 
					clumpPropSel[i] = 0;
		}
	}
	/* ----------------------------------*/
	public String getYAxisName(MesquiteChart chart){
		if (chart==null)
			return "";
		if ((chart.getUseAverage() || (showAverage && chart.getOrientation()==1)) && mode != NATIVE) {
			return "(Average) " + chart.yAxisName;
		}
		else {
			return chart.yAxisName;
		}
	}
	/* ----------------------------------*/
	Color bgColor = Color.white;
	Color axisColor = Color.cyan;
	Color gridColor = Color.cyan;
	Color barColor = Color.green;

	/*	boolean anySelected(){
		if (ownerModule.pointsAssociable==null)
			return false;
		return ownerModule.pointsAssociable.anySelected();
	}
	boolean isSelected(int i){
		if (ownerModule.pointsAssociable==null)
			return false;
		return ownerModule.pointsAssociable.getSelected(i);
	}
	/* ----------------------------------*/
	public double getIntervalWidth() {
		double xMin = chart.getMinimumX().getDoubleValue();
		double xIntervalWidth = 0;
		if (useMode == WIDTH_INTERVALS)
			xIntervalWidth = intervalSize; 
		else if (numIntervalsUsed!=0)
			xIntervalWidth = (chart.getMaximumX().getDoubleValue() - xMin)*1.0001/numIntervalsUsed; 
		return xIntervalWidth;
	}
	/* ----------------------------------*/

	public void drawChartBackground(Graphics g, MesquiteChart chart){ //g is from paint from ChartField
		//Shape clip = g.getClip();

		//g.setClip(0,0,fieldWidth, fieldHeight);
		//g.setColor(bgColor);
		//g.fillRect(margin, margin, fieldWidth-2*margin, fieldHeight-2*margin);
		// grid ========
		drawGrid(g, chart);
		//grid ===  

		//axes ====	
		/*
		g.drawLine(zeroX, 0 , zeroX, fieldHeight);
		g.drawLine(zeroX+1, 0 , zeroX+1, fieldHeight);
		g.drawLine(0, zeroY, fieldWidth , zeroY);
		g.drawLine(0, zeroY+1, fieldWidth , zeroY+1);
		 */

	}
	/* ----------------------------------*/

	public synchronized void drawChart(Graphics g, MesquiteChart chart){ //g is from paint from ChartField
		if (chart==null || chart.getXArray()==null || chart.getYArray()==null )
			return;
		if (chart.getNumPoints()==0){
			g.setColor(Color.black);
			g.drawString("The chart does not yet have any points", 10, 70);
			return;
		}
		if (calculated == 0)
			MesquiteMessage.warnProgrammer("Error: chart drawn while calculated = 0");
		if (getSuspendDrawing())
			return;
		chartDone = true;
		int fieldWidth =  chart.getFieldWidth();
		int fieldHeight =  chart.getFieldHeight();

		int margin = chart.getMargin();
		synchronizeSelection(false);
		//Shape clip = g.getClip();

		//g.setClip(0,0,fieldWidth, fieldHeight);
		//g.setColor(bgColor);
		//g.fillRect(margin, margin, fieldWidth-2*margin, fieldHeight-2*margin);
		// grid ========
		//		drawGrid(g, chart);
		//grid ===  

		//axes ====	
		g.setColor(axisColor);

		valueX.setValue(0);
		valueY.setValue(0);
//		int zeroX = xToPixel(0, chart);
		int zeroY = yToPixel(0, chart);

		/*
		g.drawLine(zeroX, 0 , zeroX, fieldHeight);
		g.drawLine(zeroX+1, 0 , zeroX+1, fieldHeight);
		g.drawLine(0, zeroY, fieldWidth , zeroY);
		g.drawLine(0, zeroY+1, fieldWidth , zeroY+1);
		 */
		//axes ====	
		if (chart.getField()==null)
			return;
		g.setColor(Color.black);
		String problem = "";
		g.drawRect(margin, margin, chart.getField().getBounds().width-1-2*margin, chart.getField().getBounds().height-1-2*margin);
		boolean someSelected = anySelected();
		int lastX=MesquiteInteger.unassigned;
		int lastY=MesquiteInteger.unassigned;
		if (useMode == NATIVE) {
			markerWidth = originalMarkerWidth;
			boolean wasGray = true;
			MesquiteDouble sel = new MesquiteDouble(0); //to keep track of whether all selected in bar (1) all deselected (0) or mix (2)
			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (getSuspendDrawing())
					return;
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					chart.getXArray().placeValue(i, valueX);
					chart.getYArray().placeValue(i, valueY);

					NumberArray xs = chart.getXArray();
					if (!anySameLeft(xs, i, valueX)){
						double sum = 0;
						if (ownerModule.noSum)
							sum = chart.getYArray().getDouble(i);
						else if (someSelected) {
							sel.setValue(0);
							sum = sumSame(xs, chart.getYArray(), i, valueX, sel);
						}
						else
							sum = sumSame(xs, chart.getYArray(), i, valueX, null);
						int xPixel = xToPixel(valueX.getDoubleValue(), chart);
						int yPixel = yToPixel(sum, chart);
						if (getSuspendDrawing())
							return;
						if (xPixel < 0 || xPixel > chart.getField().getBounds().width) {
							problem += "[xPixel " + xPixel + " w " + chart.getField().getBounds().width + " i " + i + "] " ;
							if (xPixel<0) xPixel = 0;
							if (xPixel>chart.getField().getBounds().width) xPixel = chart.getField().getBounds().width;
							
						}
						if (yPixel < 0 || yPixel > chart.getField().getBounds().height) {
							problem += "[yPixel " + yPixel + " h " + chart.getField().getBounds().height + " i " + i + "] " ;
							if (yPixel<0) yPixel = 0;
							if (yPixel>chart.getField().getBounds().height) yPixel = chart.getField().getBounds().height;
					}
						int selY = yPixel;

						boolean dimmed = false;
						if (someSelected && sel.getValue() != sum){
							dimmed = true;
							selY = yToPixel(sel.getValue(), chart);
						}
						Color fillStandard = Color.darkGray;
						Color dimStandard = Color.lightGray;
						Color line = Color.black;
						Color fill  = fillStandard;
						Color dim = dimStandard;

						if (chart.colorsExist() && chart.getColor(i)!=null) {
							fill = chart.getColor(i);
							if (dimmed)
								dim = ColorDistribution.brighter(fill, ColorDistribution.dimmingConstant);
						}

						g.setColor(fill);
						if (showAsBarChart) {
							if (!dimmed)
								fillRectByCorners(g, xPixel, yPixel, xPixel + markerWidth, zeroY);
							else {
								fillRectByCorners(g, xPixel, selY, xPixel + markerWidth, zeroY);
								g.setColor(dim);
								fillRectByCorners(g, xPixel, yPixel, xPixel + markerWidth, selY);
							}
							g.setColor(line);
							drawRectByCorners(g, xPixel, yPixel, xPixel + markerWidth, zeroY);
							g.setColor(Color.white);
							if (yPixel<zeroY)
								g.drawLine(xPixel, yPixel-1, xPixel+markerWidth, yPixel-1);
							else 
								g.drawLine(xPixel, yPixel+1, xPixel+markerWidth, yPixel+1);
						}
						else {
							if (MesquiteInteger.isCombinable(lastX)&&MesquiteInteger.isCombinable(lastX))
								g.drawLine(lastX,lastY,xPixel+markerWidth/2,yPixel);
							int markerRadius = MesquiteInteger.maximum(markerWidth/4, 2);
							g.fillOval(xPixel+markerWidth/2-markerRadius, yPixel-markerRadius, markerRadius*2, markerRadius*2);
							if (markerRadius>2)
								g.setColor(line);
							g.drawOval(xPixel+markerWidth/2-markerRadius, yPixel-markerRadius, markerRadius*2, markerRadius*2);
							lastX = xPixel+markerWidth/2;
							lastY = yPixel;
						}


					}

				}
			}
		}
		else if (numIntervalsUsed !=0) {
			double xMin = chart.getMinimumX().getDoubleValue();
			double xIntervalWidth = getIntervalWidth();

			markerWidth = xToPixel(xIntervalWidth + xMin, chart) - xToPixel(xMin, chart); 
			if (markerWidth == 0) //���� divide by zero
				markerWidth =1;

			for (int i= 0; i<numIntervalsUsed +1; i++) {
				if (getSuspendDrawing())
					return;
				if (clump!=null && i<clump.length && clump[i]!=null && !clump[i].isUnassigned()) {
					int xPixel = xToPixel(xIntervalWidth*i + xMin, chart);

					int yPixel = yToPixel(clump[i].getDoubleValue(), chart);
					if (xPixel >= 0 && yPixel >=0) {  //can be negative in some cases of update while calculations in flux?
						if (getSuspendDrawing())
							return;
						if (xPixel > chart.getField().getBounds().width)
							problem += "[xPixel " + xPixel + " w " + chart.getField().getBounds().width + " i " + i + "] " ;
						if (yPixel > chart.getField().getBounds().height)
							problem += "[yPixel " + yPixel + " h " + chart.getField().getBounds().height + " i " + i + "] " ;
						int selY = yPixel;

						boolean dimmed = false;
						if (someSelected && clumpPropSel!=null && clumpPropSel[i] < 1.0){
							dimmed = true;
							selY = yToPixel(clumpPropSel[i]*clump[i].getDoubleValue(), chart);
						}
						Color fill = Color.blue;
						Color line = Color.black;
						Color dim = ColorDistribution.veryLightBlue;
						if (chart.colorsExist() && clumpColors[i][1]!=null){  //MULTICOLOR
							if (showAsBarChart) {
								int sum = 0;
								int totColors = 0;
								for (int k = 0; k < MAXCLUMPCOLORS && clumpColors[i][k]!=null; k++){
									sum += clumpColorNumbers[i][k];
									totColors ++;
								}

								int offset = 0;
								for (int iColor = 0; iColor<totColors; iColor++){
									fill = clumpColors[i][iColor];
									if (dimmed)
										dim = ColorDistribution.brighter(fill, ColorDistribution.dimmingConstant);
									g.setColor(fill);
									if (!dimmed)
										fillRectByCorners(g, xPixel, yPixel+offset, xPixel + markerWidth, zeroY);
									else {
										//fillRectByCorners(g, xPixel, selY+offset, xPixel + markerWidth, zeroY);
										g.setColor(dim);
										fillRectByCorners(g, xPixel, yPixel+offset ,xPixel + markerWidth, zeroY);
									}
									offset += (zeroY-yPixel)*clumpColorNumbers[i][iColor]/sum;
								}
								fill = Color.darkGray;
								if (markerWidth >2)
									g.setColor(line);
								drawRectByCorners(g, xPixel, yPixel, xPixel + markerWidth, zeroY);
								g.setColor(Color.white);
								if (yPixel<zeroY)
									g.drawLine(xPixel, yPixel-1, xPixel+markerWidth, yPixel-1);
								else 
									g.drawLine(xPixel, yPixel+1, xPixel+markerWidth, yPixel+1);
							}
							else {
								fill = Color.darkGray;
								if (MesquiteInteger.isCombinable(lastX)&&MesquiteInteger.isCombinable(lastX))
									g.drawLine(lastX,lastY,xPixel+markerWidth/2,yPixel);
								int markerRadius = MesquiteInteger.maximum(markerWidth/4, 2);
								g.fillOval(xPixel+markerWidth/2-markerRadius, yPixel-markerRadius, markerRadius*2, markerRadius*2);
								if (markerRadius>2)
									g.setColor(line);
								g.drawOval(xPixel+markerWidth/2-markerRadius, yPixel-markerRadius, markerRadius*2, markerRadius*2);
								lastX = xPixel+markerWidth/2;
								lastY = yPixel;
							}
						}
						else {  //ONE COLOR
							if (chart.colorsExist()){
								if (clumpColors[i][0]!=null) {
									fill = clumpColors[i][0];
									if (dimmed)
										dim = ColorDistribution.brighter(fill, ColorDistribution.dimmingConstant);
								}
								else {
									fill = Color.darkGray;
									dim = Color.lightGray;
								}
							}

							g.setColor(fill);
							if (showAsBarChart) {
								if (!dimmed)
									fillRectByCorners(g, xPixel, yPixel, xPixel + markerWidth, zeroY);
								else {
									fillRectByCorners(g, xPixel, selY, xPixel + markerWidth, zeroY);
									g.setColor(dim);
									fillRectByCorners(g, xPixel, yPixel, xPixel + markerWidth, selY);
								}
								if (markerWidth >2)
									g.setColor(line);
								drawRectByCorners(g, xPixel, yPixel, xPixel + markerWidth, zeroY);
								g.setColor(Color.white);
								if (yPixel<zeroY)
									g.drawLine(xPixel, yPixel-1, xPixel+markerWidth, yPixel-1);
								else 
									g.drawLine(xPixel, yPixel+1, xPixel+markerWidth, yPixel+1);
							}
							else {
								if (MesquiteInteger.isCombinable(lastX)&&MesquiteInteger.isCombinable(lastX))
									g.drawLine(lastX,lastY,xPixel+markerWidth/2,yPixel);
								int markerRadius = MesquiteInteger.maximum(markerWidth/4, 2);
								g.fillOval(xPixel+markerWidth/2-markerRadius, yPixel-markerRadius, markerRadius*2, markerRadius*2);
								if (markerRadius>2)
									g.setColor(line);
								g.drawOval(xPixel+markerWidth/2-markerRadius, yPixel-markerRadius, markerRadius*2, markerRadius*2);
								lastX = xPixel+markerWidth/2;
								lastY = yPixel;
							}
						}

					}
				}
			}
		}
		else if (calculated == 2 && chart.getNumPoints()>0)
			MesquiteMessage.warnProgrammer("Warning: numIntervals 0 with non native mode in Histogram; mode = " + mode);
		if (problem.length() >0 && !getDrawWarningSuppressed())
			MesquiteMessage.warnProgrammer("Error: a histogram value was drawn outside of the bounds of the chart "  + problem);
		g.setColor(Color.black);
		g.drawRect(margin, margin, chart.getField().getBounds().width-1-2*margin, chart.getField().getBounds().height-1-2*margin);
		//g.setClip(clip);

	}
	public String getTextVersion(MesquiteChart chart){
		if (chart==null || chart.getXArray()==null || chart.getYArray()==null )
			return null;
		StringBuffer s = new StringBuffer();
		if (chart.getNumPoints()==0){
			return "The chart does not yet have any points.  It may be in the process of being calculated.";
		}
		if (useMode == NATIVE) {
			boolean multiFound = false;
			int[] ord = chart.getOrderByX();
			for (int j= 0; j<chart.getNumPoints(); j++) {
				int i = ord[j];
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					chart.getXArray().placeValue(i, valueX);
					NumberArray xs = chart.getXArray();
					if (!anySameLeft(xs, i, valueX)){
						double sum = 0;
						if (ownerModule.noSum)
							sum = chart.getYArray().getDouble(i);
						else
							sum = sumSame(xs, chart.getYArray(), i, valueX, null);
						//					if (chart.getCharter()!=null && chart.getCharter().getShowPercent())
						//						sum = sum/totalY.getDoubleValue();
						s.append(valueX.toString()+ ": " + MesquiteDouble.toString(sum) + StringUtil.lineEnding());
					}
					else
						multiFound = true;
				}
			}
			if (multiFound) {
				if (ownerModule.showRaw.getValue()) {
					s.append( StringUtil.lineEnding() + "========================" + StringUtil.lineEnding());
					chart.putRawTextVersion(ord, ": ", StringUtil.lineEnding(), false, false, s);
				}
				return s.toString();
			}
			else {
				s.setLength(0);
				chart.putRawTextVersion(ord, ": ", StringUtil.lineEnding(), false, false, s);
				return s.toString();
			}

		}
		else if (numIntervalsUsed !=0) {
			double xMin = chart.getMinimumX().getDoubleValue();
			double xIntervalWidth = getIntervalWidth();
			for (int i= 0; i<numIntervalsUsed +1; i++) {
				if (clump!=null && i<clump.length && clump[i]!=null && !clump[i].isUnassigned()) {
					s.append(MesquiteDouble.toString(xIntervalWidth*i + xMin) + " to " + MesquiteDouble.toString(xIntervalWidth*(i+1)+ xMin));
					s.append(": " + MesquiteDouble.toString(clump[i].getDoubleValue()) + StringUtil.lineEnding());
				}
			}
			if (ownerModule.showRaw.getValue()) {
				s.append( StringUtil.lineEnding() + "========================" + StringUtil.lineEnding());
				chart.putRawTextVersion(chart.getOrderByX(), ": ", StringUtil.lineEnding(), false, false, s);
			}
			return s.toString();
		}
		return null;
	}

	boolean anySameLeft(NumberArray xs, int index, MesquiteNumber valueX){
		for (int i=0; i < index; i++) {
			if (xs.equal(i, index))
				return true;
		}
		return false;
	}


	double sumSame(NumberArray xs, NumberArray ys, int index, MesquiteNumber valueX, MesquiteDouble sel){
		if (!valueX.isCombinable())
			return valueX.getDoubleValue();
		double sum = 0;
		double sumSel = 0;
		if (index >= 0){ //use x value at index
			for (int i=0; i < xs.getSize(); i++) {
				if (xs.equal(i, index) && ys.isCombinable(i)) {
					sum += ys.getDouble(i);
					if (sel !=null && isSelected(i))
						sumSel += ys.getDouble(i);
				}
			}
		}
		else { //use valueX
			for (int i=0; i < xs.getSize(); i++) {
				if (xs.equals(i, valueX) && ys.isCombinable(i)) {
					sum += ys.getDouble(i);
					if (sel !=null && isSelected(i))
						sumSel += ys.getDouble(i);
				}
			}
		}
		if (sel !=null) 
			sel.setValue(sumSel);
		return sum;
	}
	/* ----------------------------------*/
	public void drawBlank(Graphics g, MesquiteChart chart){
		int margin = chart.getMargin();
		Color c = g.getColor();
		g.setColor(bgColor);
		g.fillRect(margin, margin, chart.getFieldWidth()-2*margin, chart.getFieldHeight()-2*margin);
		g.setColor(Color.black);
		g.drawRect(margin, margin, chart.getFieldWidth()-2*margin, chart.getFieldHeight()-2*margin);
		g.setColor(c);
	}
	/* ----------------------------------*/
	public String getName() {
		return "Bar  & Line Chart";
	}
	void synchronizeSelection(boolean reset) {
		if (chart!=null){
			if (ownerModule.pointsAssociable!=null)
				chart.synchronizePointSelection(ownerModule.pointsAssociable);
			else if (reset)
				deselectAll();
		}
	}
}


class LittlePanel extends Panel {
	String t;
	StringInABox sb;
	void setText(String s, Font f, int w){
		t = s;
		sb = new StringInABox(t, f, w);
	}
	StringInABox getBox(){
		return sb;
	}
	public void paint(Graphics g){
		if (sb == null)
			return;

		sb.drawInBox(g, getBackground(), 3, 1);
		g.setColor(Color.black);
		g.drawRect(0,0,getBounds().width-1, getBounds().height-1);
	}
}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.
 
 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org
 
 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.ItemsBiplot;
/*~~  */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* A module to supervise scattergrams of items.  This coordinates calculations; Scattergram module handles most graphics.*/

public class ItemsBiplot extends ItemsBiplotter    {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.charts.Scattergram.Scattergram.class, getName() + " needs a module to draw the chart.",
		"The chart drawing module is selected automatically");
	}
	public DrawChart charterTask;
	ChartWindow cWindow;
	int suspend = 0;
	boolean doCountPending = false;
	boolean calculationsEnabled = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		charterTask = (DrawChart)hireNamedEmployee(DrawChart.class, "#mesquite.charts.Scattergram.Scattergram");
		if (charterTask == null)
			return sorry(getName() + " couldn't start because no charting module was obtained.");
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	public void employeeQuit(MesquiteModule m){
		if (m == charterTask)
			iQuit();
	}
	
	/*.................................................................................................................*/
	public ChartWindow makeChartWindow(MesquiteModule requester) {
		cWindow =  new ItemsBiplotWindow(requester, this);
		return cWindow;
	}
	
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (cWindow==null)
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
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the chart drawing module", null, commandName, "getCharter")) {
			return charterTask;
		}
		else if (checker.compare(this.getClass(), "Returns the window in which the chart appears", null, commandName, "getWindow")) {
			return cWindow;
		}
		else if (checker.compare(this.getClass(), "Increments the suspension level on the calculations", null, commandName, "suspendCalculations")){
			incrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Decrements the suspension level on the calculations", null, commandName, "resumeCalculations")){
			decrementSuspension();
		}
		else if (checker.compare(this.getClass(), "Enables the calculations", null, commandName, "enableCalculations")){
			calculationsEnabled = true;
			doCounts();
		}
		else if (checker.compare(this.getClass(), "Requests that calculations be performed", null, commandName, "doCounts")){
			if (suspend>0)
				doCountPending=true;
			else  {
				doCounts();
				doCountPending=false;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	public void incrementSuspension(){
		suspend++;
	}
	
	public void decrementSuspension(){
		suspend--;
		if (suspend<=0 && doCountPending && cWindow!=null) {
			suspend = 0;
			doCountPending=false;
			doCounts();
		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if(cWindow == null)
			return;
		if (employee == charterTask)
			((ItemsBiplotWindow)cWindow).recalcChart();
		else
			doCounts();
		
	}
	/*...................................................................................................................*/
	public void doCounts( int firstItem, int lastItem, boolean fullCount){
		if (cWindow!=null && !isDoomed()) {
			if (suspend>0)
				doCountPending=true;
			else  {
				((ItemsBiplotWindow)cWindow).doCounts(firstItem, lastItem, fullCount);
				doCountPending=false;
			}
		}
	}
	/*...................................................................................................................*/
	public void doCounts(){
		doCounts(MesquiteInteger.unassigned, MesquiteInteger.unassigned,true);
	}
	public void setTaxa(Taxa taxa){
		if (cWindow!=null)
			((ItemsBiplotWindow)cWindow).setTaxa(taxa);
	}
	/*...................................................................................................................*/
	public void setShowNames( boolean showNames){
		if (cWindow!=null)
			((ItemsBiplotWindow)cWindow).setShowNames(showNames);
	}
	/*...................................................................................................................*/
	public void setItemsSource( ItemsSource itemsSourceTask){
		if (cWindow!=null)
			((ItemsBiplotWindow)cWindow).setItemsSource(itemsSourceTask);
		
	}
	/*...................................................................................................................*/
	public void setNumberTaskX(NumberForItem numberTask){
		if (cWindow!=null)
			((ItemsBiplotWindow)cWindow).setNumberTaskX(numberTask);
	}
	/*...................................................................................................................*/
	public void setNumberTaskY(NumberForItem numberTask){
		if (cWindow!=null)
			((ItemsBiplotWindow)cWindow).setNumberTaskY(numberTask);
	}
	/*...................................................................................................................*/
	public void setNumberTaskZ(NumberForItem numberTask){
		if (cWindow!=null)
			((ItemsBiplotWindow)cWindow).setNumberTaskZ(numberTask);
	}
	/*...................................................................................................................*/
	/*.................................................................................................................*/
	public void setDefaultNumberOfItems(int def){
		if (cWindow!=null)
			((ItemsBiplotWindow)cWindow).setDefaultNumberOfItems(def);
	}
	/*...................................................................................................................*/
	public void setAuxiliary(Object object, boolean useAsFirstParameter){
		if (cWindow!=null)
			((ItemsBiplotWindow)cWindow).setAuxiliary(object, useAsFirstParameter);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Items bi-plot";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Makes a chart comparing two values for each of many items.";
	}
	
}

class ItemsBiplotWindow extends ChartWindow implements ChartListener, ActionListener {
	private NumberArray valuesX, valuesY, valuesZ;
	MesquiteNumber sum;
	private boolean wasDefinite = true;
	private boolean assigned = false;
	public String explanationString = "";
	NumberForItem numberTaskX, numberTaskY, numberTaskZ;
	ItemsSource itemsSourceTask;
	private int defaultNumberOfItems = 100;
	int numberOfItems = 100;
	int numberOfItemsUsed = numberOfItems;
	private MesquiteMenuItemSpec numItemsItem;
	private Taxa taxa;
	Object auxiliary = null;
	boolean auxFirst = false;
	//MesquiteNumber constrainedMinX, constrainedMaxX, constrainedMinY, constrainedMaxY; REMOVED temporarily(?)
	
	public MesquiteCommand setXCommand, setYCommand;
	public int currentX =0;
	public int currentY= 0;
	String xString, yString, zString;
	
	private int windowWidth=0;
	private int windowHeight=0;
	private int chartInsetTop = 10;
	private int chartInsetBottom = 60;
	private int chartInsetLeft = 0;
	private int chartInsetRight = 20;
	boolean showNames = false;
	ItemsBiplot ownerModule;
	MesquiteNumber utilNum;
	//MesquiteInteger positivize;  REMOVED temporarily(?)
	//MesquiteString positivizeName;
	TChartMessages messagePane;
//	MesquiteBoolean colorItems; REMOVED temporarily(?)
	DoubleMiniScroll scrollBox;
	boolean firstTime = true;
	ChartTool queryTool;
	MesquiteChart chart;
	MesquiteCommand recalcCommand;
	boolean firstTimeThrough = true;
	boolean liveChart = true;
	
	MesquiteBoolean goAhead, autoCount;
	Button recalcButton;
	
	public ItemsBiplotWindow (MesquiteModule requester, ItemsBiplot ownerModule) {
		super(requester,true); //infobar
		setWindowSize(400,400);
		this.ownerModule = ownerModule;
		firstTime=true;
		chartTitle = "Bi-plot";
		valuesX = new NumberArray(0);
		valuesY = new NumberArray(0);
		valuesZ = new NumberArray(0);
		/*
		 constrainedMinX = new MesquiteNumber();
		 constrainedMaxX = new MesquiteNumber();
		 constrainedMinY = new MesquiteNumber();
		 constrainedMaxY = new MesquiteNumber();
		 positivize = new MesquiteInteger();
		 positivizeName = new MesquiteString();
		 */
		chart=new MesquiteChart(ownerModule, 100, 0, ownerModule.charterTask.createCharter(this));
		setChart(chart);
		
		
		goAhead = new MesquiteBoolean(false);
		autoCount = new MesquiteBoolean(true);
		Panel f = chart.getField();
		if (f == null)
			autoCount.setValue(true);
		else {
			f.add(recalcButton = new Button("Recalculation needed"));
			recalcButton.setBounds(10,10, 140, 30);
			recalcButton.setVisible(false);
			recalcButton.setBackground(Color.white);
			recalcButton.addActionListener(this);
		}
		
		//colorItems = new MesquiteBoolean(true);REMOVED temporarily(?)
		//ownerModule.addCheckMenuItem(ownerModule.findMenuAmongEmployers("Colors"), "Color Items by Group", ownerModule.makeCommand("toggleColorItems",  this), colorItems);
		chart.deassignChart();
		
		setXCommand = ownerModule.makeCommand("setX",  this);
		setYCommand = ownerModule.makeCommand("setY",  this);
		recalcCommand = ownerModule.makeCommand("recalculate",  this);
		
		/*		MesquiteSubmenuSpec mss = ownerModule.addSubmenu(null, "Positivize");
		 positivizeName.setValue("none");
		 mss.setSelected(positivizeName);
		 
		 ownerModule.addItemToSubmenu(null, mss, "Constrain Minimum X", ownerModule.makeCommand("constrainMinX",  this));
		 ownerModule.addItemToSubmenu(null, mss, "Constrain Maximum X", ownerModule.makeCommand("constrainMaxX",  this));
		 ownerModule.addItemToSubmenu(null, mss, "Constrain Minimum Y", ownerModule.makeCommand("constrainMinY",  this));
		 ownerModule.addItemToSubmenu(null, mss, "Constrain Maximum Y", ownerModule.makeCommand("constrainMaxY",  this));
		 ownerModule.addItemToSubmenu(null, mss, "-", null);
		 ownerModule.addItemToSubmenu(null, mss, "Deconstrain Minimum X", ownerModule.makeCommand("deconstrainMinX",  this));
		 ownerModule.addItemToSubmenu(null, mss, "Deconstrain Maximum X", ownerModule.makeCommand("deconstrainMaxX",  this));
		 ownerModule.addItemToSubmenu(null, mss, "Deconstrain Minimum Y", ownerModule.makeCommand("deconstrainMinY",  this));
		 ownerModule.addItemToSubmenu(null, mss, "Deconstrain Maximum Y", ownerModule.makeCommand("deconstrainMaxY",  this));
		 ownerModule.addItemToSubmenu(null, mss, "-", null);
		 ownerModule.addCheckMenuItemToSubmenu(null, mss, "Positivize by X", ownerModule.makeCommand("positivizeX",  this), null);
		 ownerModule.addCheckMenuItemToSubmenu(null, mss, "Positivize by Y", ownerModule.makeCommand("positivizeY",  this), null);
		 */
		chart.setLocation(chartInsetLeft, chartInsetTop);
		chart.setChartSize(getWidth()-chartInsetRight - chartInsetLeft, getHeight()-chartInsetTop - chartInsetBottom);
		messagePane = new TChartMessages(this);
		addToWindow(messagePane);
		messagePane.setVisible(true);
		messagePane.setLocation(0, windowHeight- chartInsetBottom);
		messagePane.setSize(windowWidth, chartInsetBottom);
		scrollBox = new DoubleMiniScroll(setXCommand, setYCommand, 0,0,0,0,0,0);
		chart.add(scrollBox);
		scrollBox.setYTitle("Y");
		scrollBox.setXTitle("X");
		scrollBox.setLocation(2, getHeight()-24-chartInsetBottom- scrollBox.getBounds().height);
		scrollBox.setVisible(true);
		setWindowSize(400,400);
		ownerModule.addMenuSeparator();
		ownerModule.addCheckMenuItem(null, "Auto-recalculate", ownerModule.makeCommand("toggleAutoRecalc", this), autoCount);
	}
	public void setDefaultNumberOfItems(int def){
		defaultNumberOfItems=def;
		numberOfItems = def;
	}
	/*...................................................................................................................*/
	public void setChartVisible(){
		addToWindow(chart);
		chart.setLocation(chartInsetLeft, chartInsetTop);
		chart.setChartSize(getWidth()-chartInsetRight - chartInsetLeft, getHeight()-chartInsetTop - chartInsetBottom);
		chart.setVisible(true);
		scrollBox.repaint();
		windowResized();
		contentsChanged();
		if (recalcButton.isVisible())
			recalcButton.setSize(recalcButton.getPreferredSize());
		chart.repaint();
	}
	/*...................................................................................................................*/
	public void pointMouseDown(MesquiteChart chart, int whichPoint, MesquiteNumber valueX, MesquiteNumber valueY, int x, int y, int modifiers, String message){
		if (whichPoint>=0){
			String name = chart.getName(whichPoint);
			if (name ==null){
				Object item = itemsSourceTask.getItem(taxa, whichPoint);
				if (item instanceof Listable)
					name = ((Listable)item).getName();
			}
			if (name==null)
				name = "#" + whichPoint;
			name +=  "\nx " + chart.getXArray().toString(whichPoint) + "\ny " + chart.getYArray().toString(whichPoint);
			if (numberTaskZ!=null)
				name +=  "\ncolor " + chart.getZArray().toString(whichPoint);
			if (message !=null)
				name += "\n" + message;
			chart.showQuickMessage(whichPoint, x, y, name);
		}
		else {
			if (message !=null)
				chart.showQuickMessage(whichPoint, x, y, message);
		}
	}
	/*...................................................................................................................*/
	public void pointMouseUp(MesquiteChart chart, int whichPoint, int x, int y, int modifiers, String message){
		chart.hideQuickMessage();
	}
	/*.................................................................................................................*/
	public void setCharter(Charter charter) {
		chart.setCharter(charter);
		charter.setShowNames(true);
	}
	public void setExplanationString(String t) {
		explanationString=t;
	}
	
	/*...................................................................................................................*/
	public void setTaxa(Taxa taxa){
		this.taxa = taxa;
	}
	/*...................................................................................................................*/
	public void setShowNames( boolean showNames){
		this.showNames = showNames;
	}
	/*...................................................................................................................*/
	public void setItemsSource( ItemsSource itemsSourceTask){
		this.itemsSourceTask = itemsSourceTask;
		if (ownerModule.charterTask!=null && itemsSourceTask!=null) {
			ownerModule.charterTask.pointsAreSelectable(itemsSourceTask.getSelectionable()!=null, itemsSourceTask.getSelectionable(), true);
			
		}
	}
	private void resetScrolls(){
		if (numberTaskX instanceof Incrementable){
			Incrementable inc = (Incrementable)numberTaskX;
			scrollBox.setXTitle(inc.getItemTypeName());
			int min = (int)inc.toExternal(inc.getMin());
			int max = (int)inc.toExternal(inc.getMax());
			if (currentX> (int)inc.getMax())
				currentX = (int)inc.getMax();
			if (currentX< (int)inc.getMin())
				currentX = (int)inc.getMin();
			scrollBox.setXValues(min,  (int)inc.toExternal(currentX), max);
			scrollBox.setXVisible(true);
		}
		else {
			scrollBox.setXValues(0,0,0);
			scrollBox.setXVisible(false);
		}
		if (numberTaskY instanceof Incrementable){
			Incrementable inc = (Incrementable)numberTaskY;
			scrollBox.setYTitle(inc.getItemTypeName());
			int min = (int)inc.toExternal(inc.getMin());
			int max = (int)inc.toExternal(inc.getMax());
			if (currentY>(int) inc.getMax())
				currentY = (int)inc.getMax();
			if (currentY< (int)inc.getMin())
				currentY = (int)inc.getMin();
			scrollBox.setYValues(min,  (int)inc.toExternal(currentY), max);
			scrollBox.setYVisible(true);
		}
		else {
			scrollBox.setYValues(0,0,0);
			scrollBox.setYVisible(false);
		}
	}
	/*...................................................................................................................*/
	public void setNumberTaskX(NumberForItem numberTask){
		this.numberTaskX = numberTask;
	}
	/*...................................................................................................................*/
	public void setNumberTaskY(NumberForItem numberTask){
		this.numberTaskY = numberTask;
	}
	/*...................................................................................................................*/
	public void setNumberTaskZ(NumberForItem numberTask){
		this.numberTaskZ = numberTask;
	}
	/*...................................................................................................................*/
	public void setAuxiliary(Object object, boolean useAsFirstParameter){
		auxiliary = object;
		auxFirst = useAsFirstParameter;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (numberTaskX instanceof Incrementable){
			Incrementable inc = (Incrementable)numberTaskX;
			temp.addLine("setX " + inc.toExternal(currentX)); 
		}
		if (numberTaskY instanceof Incrementable){
			Incrementable inc = (Incrementable)numberTaskY;
			temp.addLine("setY " + inc.toExternal(currentY)); 
		}
		if (assigned)
			temp.addLine("setNumberItems " + numberOfItems);
		//NOTE: do not snapshot the auto-recalc.  It should always be on at first.
		
		
		/*
		 if (!constrainedMinX.isUnassigned())
		 temp.addLine("constrainMinX " + constrainedMinX);
		 if (!constrainedMaxX.isUnassigned())
		 temp.addLine("constrainMaxX " + constrainedMaxX);
		 if (!constrainedMinY.isUnassigned())
		 temp.addLine("constrainMinY " + constrainedMinY);
		 if (!constrainedMaxY.isUnassigned())
		 temp.addLine("constrainMaxY " + constrainedMaxY);
		 if (positivize.getValue()==0)
		 temp.addLine("positivizeX");
		 else if (positivize.getValue()==1)
		 temp.addLine("positivizeY");
		 temp.addLine("toggleColorItems " + colorItems.toOffOnString());
		 */
		temp.incorporate(super.getSnapshot(file), false);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the chart drawing module", "[name of module]", commandName, "setChartType")){
			String newc = ParseUtil.getFirstToken(arguments, pos);
			if (ownerModule.charterTask.getClass()!=null && (ownerModule.charterTask.nameMatches(newc))) //already have this
				return ownerModule.charterTask;
			DrawChart temp =  (DrawChart)ownerModule.replaceEmployee(DrawChart.class, arguments, "Type of chart", ownerModule.charterTask);
			if (temp!=null) {
				ownerModule.charterTask=  temp;
				if (itemsSourceTask!=null)
					temp.pointsAreSelectable(itemsSourceTask.getSelectionable()!=null, itemsSourceTask.getSelectionable(), true);
				else
					temp.pointsAreSelectable(false, null, true);
				setCharter(ownerModule.charterTask.createCharter(this));
				ownerModule.parametersChanged();
				return ownerModule.charterTask;
			}
		} 
		else if (checker.compare(this.getClass(), "Returns values as tabbed line", null, commandName, "getValuesTabbed")) {
			if (valuesX == null || valuesY == null)
				return null;
				StringBuffer s = new StringBuffer();  
				s.append("X");
				for (int i =0; i<valuesX.getSize(); i++)
					s.append("\t" + valuesX.toString(i));
				s.append(StringUtil.lineEnding());
				s.append("Y");
				for (int i =0; i<valuesY.getSize(); i++)
					s.append("\t" + valuesY.toString(i));
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
		else if (checker.compare(this.getClass(), "Requests recalculation", null, commandName, "recalculate")) {
			doCounts();
			return null;
		}
		/*
		 else if (checker.compare(this.getClass(), "Sets the minimum value on the X axis", "[x value]", commandName, "constrainMinX")) {
		 MesquiteNumber n = new MesquiteNumber();
		 pos.setValue(0);
		 n.setValue(ParseUtil.getFirstToken(arguments, pos));
		 if (!n.isCombinable() && !MesquiteThread.isScripting())
		 n = MesquiteNumber.queryNumber(ownerModule.containerOfModule(), "Minimum X", "Constrain the minimum value shown of X to be:", constrainedMinX);
		 if (n.isCombinable()){
		 constrainedMinX.setValue(n);
		 recalcChart();
		 }
		 }
		 else if (checker.compare(this.getClass(), "Sets the maximum value on the X axis", "[x value]", commandName, "constrainMaxX")) {
		 MesquiteNumber n = new MesquiteNumber();
		 pos.setValue(0);
		 if (StringUtil.blank(arguments))
		 n = MesquiteNumber.queryNumber(ownerModule.containerOfModule(), "Maximum X", "Constrain the maximum value shown of X to be:", n);
		 else
		 n.setValue(ParseUtil.getFirstToken(arguments, pos));
		 if (n.isCombinable()){
		 constrainedMaxX.setValue(n);
		 recalcChart();
		 }
		 }
		 else if (checker.compare(this.getClass(), "Sets the minimum value on the Y axis", "[y value]", commandName, "constrainMinY")) {
		 MesquiteNumber n = new MesquiteNumber();
		 pos.setValue(0);
		 if (StringUtil.blank(arguments))
		 n = MesquiteNumber.queryNumber(ownerModule.containerOfModule(), "Minimum Y", "Constrain the minimum value shown of Y to be:", n);
		 else
		 n.setValue(ParseUtil.getFirstToken(arguments, pos));
		 if (n.isCombinable()){
		 constrainedMinY.setValue(n);
		 recalcChart();
		 }
		 }
		 else if (checker.compare(this.getClass(), "Sets the maximum value on the Y axis", "[y value]", commandName, "constrainMaxY")) {
		 MesquiteNumber n = new MesquiteNumber();
		 pos.setValue(0);
		 if (StringUtil.blank(arguments))
		 n = MesquiteNumber.queryNumber(ownerModule.containerOfModule(), "Maximum Y", "Constrain the maximum value shown of Y to be:", n);
		 else
		 n.setValue(ParseUtil.getFirstToken(arguments, pos));
		 if (n.isCombinable()){
		 constrainedMaxY.setValue(n);
		 recalcChart();
		 }
		 }
		 else if (checker.compare(this.getClass(), "Deconstrains the minimum value on the X axis", null, commandName, "deconstrainMinX")) {
		 constrainedMinX.setToUnassigned();
		 recalcChart();
		 }
		 else if (checker.compare(this.getClass(), "Deconstrains the maximum value on the X axis", null, commandName, "deconstrainMaxX")) {
		 constrainedMaxX.setToUnassigned();
		 recalcChart();
		 }
		 else if (checker.compare(this.getClass(), "Deconstrains the minimum value on the Y axis", null, commandName, "deconstrainMinY")) {
		 constrainedMinY.setToUnassigned();
		 recalcChart();
		 }
		 else if (checker.compare(this.getClass(), "Deconstrains the maximum value on the Y axis", null, commandName, "deconstrainMaxY")) {
		 constrainedMaxY.setToUnassigned();
		 recalcChart();
		 }
		 
		 else if (checker.compare(this.getClass(), "Forces the X values to be positive, changing sign of other values in concert", null, commandName, "positivizeX")) {
		 if (positivize.getValue()!=0) {
		 positivize.setValue(0);
		 positivizeName.setValue("Positivize by X");
		 }
		 else {
		 positivize.setToUnassigned();
		 positivizeName.setValue("none");
		 }
		 recalcChart();
		 }
		 else if (checker.compare(this.getClass(), "Forces the Y values to be positive, changing sign of other values in concert", null, commandName, "positivizeY")) {
		 if (positivize.getValue()!=1) {
		 positivize.setValue(1);
		 positivizeName.setValue("Positivize by Y");
		 }
		 else {
		 positivize.setToUnassigned();
		 positivizeName.setValue("none");
		 }
		 recalcChart();
		 }
		 */
		else if (checker.compare(this.getClass(), "Sets which of a series of items is shown on the X axis (appropriate when there could be a series of alternative values on the x axis, e.g. for different characters or trees)", "[number of item]", commandName, "setX")) {
			if (numberTaskX instanceof Incrementable){
				Incrementable inc = (Incrementable)numberTaskX;
				int ic = (int)(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
				if (!MesquiteInteger.isCombinable(ic))
					ic = MesquiteInteger.queryInteger(ownerModule.containerOfModule(), "Choose ", "choose:", 1); //TODO have something more intelligent here
				int icInternal = (int) inc.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
				if (MesquiteInteger.isCombinable(icInternal) && icInternal != currentX){ //will catch invalid numbers later  && (icInternal>=inc.getMin()) && (icInternal<=inc.getMax())) {
					currentX=icInternal;
					if (!MesquiteThread.isScripting()){
						doCounts();
						contentsChanged();
					}
				}
				
			}
		}
		else if (checker.compare(this.getClass(), "Sets which of a series of items is shown on the Y axis (appropriate when there could be a series of alternative values on the y axis, e.g. for different characters or trees)", "[number of item]", commandName, "setY")) {//NOT YET USED
			if (numberTaskY instanceof Incrementable){
				Incrementable inc = (Incrementable)numberTaskY;
				int ic = (int)inc.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
				if (!MesquiteInteger.isCombinable(ic))
					ic = MesquiteInteger.queryInteger(ownerModule.containerOfModule(), "Choose ", "choose:", 1); //TODO have something more intelligent here
				int icInternal = (int) inc.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
				if (MesquiteInteger.isCombinable(icInternal) && icInternal != currentY){ //will catch invalid numbers later (icInternal>=inc.getMin()) && (icInternal<=inc.getMax())) {
					currentY=icInternal;
					
					if (!MesquiteThread.isScripting()){
						doCounts();
						contentsChanged();
					}
				}
				
			}
		}
		/*
		 else if (checker.compare(this.getClass(), "Sets whether to color the items in the chart according to any natural item colors", "[on or off]", commandName, "toggleColorItems")) {  
		 boolean current = colorItems.getValue();
		 colorItems.toggleValue(ParseUtil.getFirstToken(arguments, pos));
		 if (current!=colorItems.getValue())
		 recalcChart();
		 }
		 */
		else if (checker.compare(this.getClass(), "Sets the number of items plotted (appropriate if the items source has an indefinite number)", "[number of items]", commandName, "setNumberItems")) {
			int newNum = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum= MesquiteInteger.queryInteger(ownerModule.containerOfModule(), "Set Number of " + itemsSourceTask.getItemTypeNamePlural(), "Number of " + itemsSourceTask.getItemTypeNamePlural(), numberOfItems, 0, MesquiteInteger.infinite);
			if (MesquiteInteger.isCombinable(newNum) && newNum>0) {
				assigned = true;
				if (newNum!=numberOfItems){
					numberOfItems = newNum;
					if (!MesquiteThread.isScripting()){
						doCounts();
						contentsChanged();
					}
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	/*.................................................................................................................*/
	public void selectAllGraphicsPanel(){
		//this paradoxical deselection is done because chart treats all as selected if none are
		if (itemsSourceTask.getSelectionable()!=null)  {
			itemsSourceTask.getSelectionable().deselectAll();
			itemsSourceTask.getSelectionable().notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
		else {
			chart.deselectAllPoints(); 
			chart.repaint();
			chart.getField().repaint();
		}
	}
	/*...................................................................................................................*/
	private void resetArraySizes(int num){
		valuesX.resetSize(num);  
		valuesY.resetSize(num);  
		if (numberTaskZ!=null)
			valuesZ.resetSize(num);  
	}
	private void checkSizes(int num){
		if (num> valuesX.getSize()){
			resetArraySizes(valuesX.getSize()+100); 
		}
	}
	/*...................................................................................................................*/
	private void resetSizes(Taxa taxa){
		if (itemsSourceTask==null)
			return;
		int numItems=itemsSourceTask.getNumberOfItems(taxa);
		
		if (!MesquiteInteger.isFinite(numItems)) {
			if (wasDefinite && !assigned) {
				if (!MesquiteThread.isScripting())
					numberOfItems= MesquiteInteger.queryInteger(ownerModule.containerOfModule(), "Set Number of " + itemsSourceTask.getItemTypeNamePlural(), "Number of " + itemsSourceTask.getItemTypeNamePlural(), numberOfItems, 0, MesquiteInteger.infinite);
				else
					numberOfItems = defaultNumberOfItems;
				if (!MesquiteInteger.isCombinable(numberOfItems)) {
					numberOfItems = defaultNumberOfItems;
					assigned = true;
				}
			}
			wasDefinite = false;
			if (numItemsItem == null) {
				numItemsItem = ownerModule.addMenuItem( "Number of  " + itemsSourceTask.getItemTypeNamePlural() + "...", ownerModule.makeCommand("setNumberItems",  this));
				ownerModule.resetContainingMenuBar();
			}
		}
		else  {
			if (numItemsItem!= null) {
				ownerModule.deleteMenuItem(numItemsItem);
				ownerModule.resetContainingMenuBar();
				numItemsItem = null;
			}
			wasDefinite = true;
			assigned = false;
			numberOfItems = numItems;
		}
		if (!MesquiteInteger.isCombinable(numberOfItems)){ //finite but indefinite; set to 100
			resetArraySizes(100);
		}
		else {
			resetArraySizes(numberOfItems);
		}
	}	
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e){
		String buttonLabel = e.getActionCommand();
		if ("Recalculation needed".equalsIgnoreCase(buttonLabel)){  // Recalc button is pressed //send command
			goAhead.setValue(true);
			recalcCommand.doItMainThread(null, null, this);  // command invoked
			
		}
	}
	/*.................................................................................................................*/
	private boolean waitOnCalculation(){
		if (autoCount.getValue() || MesquiteThread.isScripting()){  //set to autocount; go ahead and do count
			if (recalcButton.isVisible())
				recalcButton.setVisible(false);
			return false;
		}
		else { // counts only if goAhead flag is set, which it will be following click on button
			if (goAhead.getValue()){
				goAhead.setValue(false);
				if (recalcButton.isVisible())
					recalcButton.setVisible(false);
				return false;
			}
			else if (!recalcButton.isVisible())
				recalcButton.setVisible(true);
			if (chart.getNumPoints()>0)
				chart.deassignChart();
			recalcButton.setSize(recalcButton.getPreferredSize());
			recalcButton.invalidate();
			recalcButton.doLayout();
			recalcButton.repaint();
		}
		return true;
	}
	/*.................................................................................................................*/
	public void doCounts() {
		doCounts(MesquiteInteger.unassigned, MesquiteInteger.unassigned, true);
	}
	int count = 0;
	/*.................................................................................................................*/
	public void doCounts(int firstItem, int lastItem, boolean fullCount) {
		count++;

		if (numberTaskX==null || numberTaskY==null)
			return;
		if (waitOnCalculation()) {
			blankChart();
			return;
		}
		//initializing
		MesquiteModule.incrementMenuResetSuppression();
		Object item;
		int startItem, endItem;
		boolean countAll = fullCount || firstTimeThrough;
//		boolean countAll = true;
		
		if (countAll) {
			blankChart();
			valuesX.deassignArray();
			valuesY.deassignArray();
			if (numberTaskZ!=null)
				valuesZ.deassignArray();
			int numDimensions = 2;
			if (numberTaskZ!=null)
				numDimensions = 3;
		}
		contentsChanged();
		resetSizes(taxa);
		if (numberTaskX instanceof Incrementable)
			((Incrementable)numberTaskX).setCurrent(currentX); 
		numberOfItemsUsed = numberOfItems;
		int clockInterval = numberOfItemsUsed/100;
		if (clockInterval <1)
			clockInterval = 1;
		MesquiteNumber cNum = new MesquiteNumber();
		ProgressIndicator prog = null;
		long startTime = System.currentTimeMillis();
		boolean progOn = false;
		boolean alreadyQueried = false;
		int oldnumberOfItemsUsed = numberOfItemsUsed;
		String numS = MesquiteInteger.toString(numberOfItemsUsed);
		if (numberOfItemsUsed == MesquiteInteger.finite)
			numS = "an undetermined number";
		
		if (MesquiteInteger.isUnassigned(firstItem) || countAll)
			startItem = 0;
		else
			startItem = firstItem;
		if (MesquiteInteger.isUnassigned(lastItem) || countAll)
			endItem = numberOfItemsUsed-1;
		else
			endItem =lastItem;
		int numberOfItemsExaminedThisTime = endItem-startItem+1;

		MesquiteDialog.hideWizardForCalculation();
		
		//Going through items doing calculations for x axis
		for (int im=startItem; im<=endItem && (prog==null || !prog.isAborted()); im++ ) {

			if (numberOfItemsUsed != oldnumberOfItemsUsed){
				numS = MesquiteInteger.toString(numberOfItemsUsed);
				if (numberOfItemsUsed == MesquiteInteger.finite)
					numS = "an undetermined number";
				oldnumberOfItemsUsed = numberOfItemsUsed;
			}
			if (!progOn && System.currentTimeMillis()- startTime>5000) {
				prog = new ProgressIndicator(ownerModule.getProject(),"Calculating Chart", "Calculating X value for " + itemsSourceTask.getItemTypeNamePlural(), numberOfItemsUsed, true);
				prog.start();
				progOn = true;
			}
			else if (progOn){
				prog.setCurrentValue(im);
				prog.setText("Examining " + itemsSourceTask.getItemTypeName() + "  " + (im+1) + " of " + numS, false);
			}
			if (im % 10 == 0) {
				CommandRecord.tick("Examining " + itemsSourceTask.getItemTypeName() + "  " + (im+1) + " of " + numS + " for X value");
			}
			item = itemsSourceTask.getItem(taxa, im);
			if (item == null) {
				//null item hit; source may have unexpectedly run out; reset total expected
				assigned = false;
				numberOfItemsUsed = im;
				endItem=im;
				resetArraySizes(numberOfItemsUsed);   

				
			}
			else {
				checkSizes(im);
				cNum.setToUnassigned();
				if (auxFirst)
					numberTaskX.calculateNumber(auxiliary, item, cNum, null);
				else
					numberTaskX.calculateNumber(item, auxiliary, cNum, null);
				if (valuesX!=null){
					if (im>= valuesX.getSize())
						valuesX.resetSize(im+1);
					valuesX.setValue(im, cNum);
				}

				
				if (prog != null && prog.isAborted()){
					
					String s;
					if (numberTaskZ!=null)
						s = "Y and Z axes";
					else
						s = "Y axis";
					if (!AlertDialog.query(ownerModule.containerOfModule(), "Continue with Chart?", "Calculations stopped before all " + itemsSourceTask.getItemTypeNamePlural() + " were examined.  Do you want to display the results?  If so, calculations will still need to be completed for the " + s + ".", "Display", "Discard", 1)) {
						// here we discard results, and display a blank chart
						autoCount.setValue(false);
						waitOnCalculation();
						blankChart();
						prog.goAway();
						MesquiteModule.decrementMenuResetSuppression();
						return;
					}
					assigned = false;
					numberOfItemsUsed = im;  // reset the number of items to the actual number processed
					endItem=im;
					resetArraySizes(numberOfItemsUsed);   
					alreadyQueried = true;
				}
			}
		}
		xString = numberTaskX.getNameAndParameters();
		
		
		//Going through items doing calculations for Y axis
		if (numberTaskY instanceof Incrementable)
			((Incrementable)numberTaskY).setCurrent(currentY);
		

		for (int im=startItem; im<=endItem; im++ ) {

			if (numberOfItemsUsed != oldnumberOfItemsUsed){
				numS = MesquiteInteger.toString(numberOfItemsUsed);
				if (numberOfItemsUsed == MesquiteInteger.finite)
					numS = "an undetermined number";
				oldnumberOfItemsUsed = numberOfItemsUsed;
			}
			if (!progOn && System.currentTimeMillis()- startTime>5000) {
				prog = new ProgressIndicator(ownerModule.getProject(),"Calculating Chart", "Calculating Y value for " + itemsSourceTask.getItemTypeNamePlural(), numberOfItemsUsed, true);
				prog.start();
				progOn = true;
			}
			else if (progOn){
				prog.setCurrentValue(im);
				prog.setText("Examining " + itemsSourceTask.getItemTypeName() + "  " + (im+1) + " of " + numS, false);
			}
			if (im % 10 == 0)
				CommandRecord.tick("Examining " + itemsSourceTask.getItemTypeName() + "  " + im + " of " + numS + " for Y value");
			item = itemsSourceTask.getItem(taxa, im);
			if (item == null) {
				//null item hit; source may have unexpectedly run out; reset total expected
				assigned = false;
				numberOfItemsUsed = im;
				endItem=im;

				resetArraySizes(numberOfItemsUsed);   
			}
			else {
				cNum.setToUnassigned();
				if (auxFirst)
					numberTaskY.calculateNumber(auxiliary, item, cNum, null);
				else
					numberTaskY.calculateNumber(item, auxiliary, cNum, null);
				if (valuesY!=null){
					if (im>= valuesY.getSize())
						valuesY.resetSize(im+1);
					valuesY.setValue(im, cNum);
				}
				if (prog != null && prog.isAborted()){
					
					String s = "";
					if (numberTaskZ!=null)
						s = "If so, calculations will still need to be completed for the Z axis. ";
					if (!AlertDialog.query(ownerModule.containerOfModule(), "Continue with Chart?", "Calculations stopped before all " + itemsSourceTask.getItemTypeNamePlural() + " were examined.  Do you want to display the results? " + s, "Display", "Discard", 1)) {
						// here we discard results, and display a blank chart
						autoCount.setValue(false);
						waitOnCalculation();
						blankChart();
						prog.goAway();
						MesquiteModule.decrementMenuResetSuppression();
						return;
					}
					assigned = false;
					numberOfItemsUsed = im;  // reset the number of items to the actual number processed
					endItem=im;
					resetArraySizes(numberOfItemsUsed);   
				}
			}
		}
		yString = numberTaskY.getNameAndParameters();
		
		
		//Going through items doing calculations for Z axis
		if (numberTaskZ!=null){
			int im;
			for (im=startItem; im<=endItem; im++ ) {
				if (numberOfItemsUsed != oldnumberOfItemsUsed){
					numS = MesquiteInteger.toString(numberOfItemsUsed);
					if (numberOfItemsUsed == MesquiteInteger.finite)
						numS = "an undetermined number";
					oldnumberOfItemsUsed = numberOfItemsUsed;
				}
				if (!progOn && System.currentTimeMillis()- startTime>5000) {
					prog = new ProgressIndicator(ownerModule.getProject(),"Calculating Chart", "Calculating Z value for " + itemsSourceTask.getItemTypeNamePlural(), numberOfItemsUsed, true);
					prog.start();
					progOn = true;
				}
				else if (progOn){
					prog.setCurrentValue(im);
					prog.setText("Examining " + itemsSourceTask.getItemTypeName() + "  " + (im+1) + " of " + numS, false);
				}
				if (im % 10 == 0)
					CommandRecord.tick("Examining " + itemsSourceTask.getItemTypeName() + "  " + im + " of " + numS + " for Z value");
				item = itemsSourceTask.getItem(taxa, im);
				if (item == null) {
					//null item hit; source may have unexpectedly run out; reset total expected
					assigned = false;
					numberOfItemsUsed = im;
					endItem=im;
					resetArraySizes(numberOfItemsUsed);   
				}
				else {
					cNum.setToUnassigned();
					if (auxFirst)
						numberTaskZ.calculateNumber(auxiliary, item, cNum, null);
					else
						numberTaskZ.calculateNumber(item, auxiliary, cNum, null);
					if (valuesZ!=null){
						if (im>= valuesZ.getSize())
							valuesZ.resetSize(im+1);
						valuesZ.setValue(im, cNum);
					}
				}
			}
			zString = numberTaskZ.getNameAndParameters();
			if (prog != null && prog.isAborted()){
				if (!alreadyQueried & !AlertDialog.query(ownerModule.containerOfModule(), "Continue with Chart?", "Calculations stopped before all " + itemsSourceTask.getItemTypeNamePlural() + " were examined.  Do you want to display the results?", "Display", "Discard", 1)) {
					// here we discard results, and display a blank chart
					autoCount.setValue(false);
					waitOnCalculation();
					blankChart();
					prog.goAway();
					MesquiteModule.decrementMenuResetSuppression();
					return;
				}
				assigned = false;
				numberOfItemsUsed = im;  // reset the number of items to the actual number processed
				resetArraySizes(numberOfItemsUsed);   
			}
		}
		ownerModule.charterTask.pointsAreSelectable(itemsSourceTask.getSelectionable()!=null, itemsSourceTask.getSelectionable(), true);
		
		if (prog != null)
			prog.goAway();
		//Asking to recalculate
		if (countAll) 
			recalcChart();
		else  {
			addPointsToChart( startItem, endItem+1);
			setExplanationString(itemsSourceTask.getNameAndParameters());
			resetMessagePaneText();
			messagePane.repaint();
		}
		MesquiteModule.decrementMenuResetSuppression();
		firstTimeThrough = false;
	}
	/*...................................................................................................................*/
	public void resetMessagePaneText() {
		String explanationString = "Results for  " + numberOfItemsUsed + " " + itemsSourceTask.getItemTypeNamePlural() + "\n";
		explanationString += "Source of  " + itemsSourceTask.getItemTypeNamePlural() + ": " + itemsSourceTask.getNameAndParameters();
		if (!StringUtil.blank(ownerModule.charterTask.getParameters()))
			explanationString += "\nChart:  " + ownerModule.charterTask.getParameters();
		//explanationString += "\nValues calculated:  " + numberTask.getNameAndParameters();
		explanationString += "\nDetails of items plotted:\n" + itemsSourceTask.accumulateParameters("   ");
		if (numberTaskX == numberTaskY){
			String x = numberTaskX.accumulateParameters("   ");
			explanationString += "\nDetails of values calculated: " + numberTaskX.getName() + ")\n" + x;
		}
		else {
			String x = numberTaskX.accumulateParameters("   ");
			explanationString += "\nDetails of X values calculated (" + numberTaskX.getName() + ")\n" + x;
			String y = numberTaskY.accumulateParameters("   ");
			explanationString += "\nDetails of Y values calculated (" + numberTaskY.getName() + ")\n" + y;
		}
		messagePane.setText(explanationString);
	}
	
	/*...................................................................................................................*/
	public boolean recalcChartInit() {
		resetMessagePaneText();
		chart.deassignChart();
		chart.getCharter().setShowNames(true);
		chart.setXAxisName(xString);
		chart.setYAxisName(yString);
		if (numberTaskZ!=null)
			chart.setZAxisName(zString);
		resetScrolls();
		if (taxa==null)
			return false;
		itemsSourceTask.prepareItemColors(taxa);
		return true;
	}
	/*...................................................................................................................*/
	public void addPointsToChart( int startValue, int endValue) {
		MesquiteNumber resultX = new MesquiteNumber();
		MesquiteNumber resultY = new MesquiteNumber();
		MesquiteNumber resultZ = new MesquiteNumber();
		chart.getCharter().setSuspendDrawing(true);
		chart.getCharter().setSuspendChartCalculations(true);
		for (int i=startValue; i<endValue; i++) {
			int point;
			if (i % 100 == 0)
				CommandRecord.tick("Constructing chart; adding item " + i);
			if (!valuesX.isCombinable(i))
				resultX.setToUnassigned();
			else
				valuesX.placeValue(i, resultX);
			if (!valuesY.isCombinable(i))
				resultY.setToUnassigned();
			else
				valuesY.placeValue(i, resultY);
			if (numberTaskZ!=null) {
				if (!valuesZ.isCombinable(i))
					resultZ.setToUnassigned();
				else
					valuesZ.placeValue(i, resultZ);
				
				point = chart.addPoint(resultX, resultY, resultZ);
			}
			else {
				point = chart.addPoint(resultX, resultY);
			}
			Object item = itemsSourceTask.getItem(taxa, i);
			if (showNames && item instanceof Listable)
				chart.setName(point, ((Listable)item).getName());
			//if (colorItems.getValue()) {
			Color c = itemsSourceTask.getItemColor(taxa, i);
			if (c!=null) {
				chart.setColor(point, c);
			}
			//}
		}
		chart.getCharter().setSuspendDrawing(false);
		chart.getCharter().setSuspendChartCalculations(false);
		/*
		 if (!constrainedMinX.isUnassigned())
		 chart.constrainMinimumX(constrainedMinX);
		 else
		 chart.deConstrainMinimumX();
		 if (!constrainedMaxX.isUnassigned())
		 chart.constrainMaximumX(constrainedMaxX);
		 else
		 chart.deConstrainMaximumX();
		 if (!constrainedMinY.isUnassigned())
		 chart.constrainMinimumY(constrainedMinY);
		 else
		 chart.deConstrainMinimumY();
		 if (!constrainedMaxY.isUnassigned())
		 chart.constrainMaximumY(constrainedMaxY);
		 else
		 chart.deConstrainMaximumY();
		 */
		resetMessagePaneText();
		
		chart.munch();
		contentsChanged();
		messagePane.repaint();
	}
	/*...................................................................................................................*/
	public void recalcChart() {
		if (!recalcChartInit())
			return;
		
		addPointsToChart(0,valuesX.getSize());
	}
	/*...................................................................................................................*/
	public void windowResized() {
		super.windowResized();
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (chart==null || messagePane==null || scrollBox==null) {
			MesquiteWindow.uncheckDoomed(this);
			return;
		}
		windowHeight =getHeight();
		windowWidth = getWidth();
		chart.setLocation(chartInsetLeft, chartInsetTop);
		chart.setChartSize(windowWidth-chartInsetRight - chartInsetLeft, windowHeight-chartInsetTop - chartInsetBottom);
		messagePane.setLocation(0, windowHeight- chartInsetBottom);
		messagePane.setSize(windowWidth, chartInsetBottom);
		scrollBox.setLocation(2, windowHeight-24-chartInsetBottom- scrollBox.getBounds().height);
		recalcButton.setSize(recalcButton.getPreferredSize());
		
		chart.repaint();
		messagePane.repaint();
		MesquiteWindow.uncheckDoomed(this);
	}
}

/*...................................................................................................................*/
class TChartMessages extends TextArea {
	ItemsBiplotWindow window;
	public TChartMessages (ItemsBiplotWindow window) {
		super("", 3, 3, TextArea.SCROLLBARS_VERTICAL_ONLY);
		this.window = window;
	}
}



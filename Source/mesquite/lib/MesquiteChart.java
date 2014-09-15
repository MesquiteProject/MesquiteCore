/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.awt.*;
import java.util.*;
import java.awt.datatransfer.*;


/* ======================================================================== */
/** Charts for general use.  Currently only a few chart types.  At the moment this is specialized for 
charts of points (2D), but in future basic class should be
more abstract, and point-chart should be a subclass */
public class MesquiteChart extends MesquitePanel  implements OwnedByModule {

	//orientation stored in chart as 0 = items by values; 1 = values by items   (y by x)
	public static final int VALUESONX = 0;
	public static final int ITEMSONX = 1;

	/**The core area of the chart that contains the graphical plot, just inside the axes*/
	private ChartField field;
	/**  ChartDisplayExtras attached to this chart*/
	private ListableVector extras;
	/** Dimensions of chart itself (pixels), including axis area and field*/
	private int chartWidth = 48;
	private int chartHeight = 48;
	/** Margin within chart field (pixels), from axis to first point etc*/
	private int margin = 0;
	/**Margin between left edge of chart and Y axis (i.e., between edge of chart and chart field*/
	private int xAxisEdge = 0; 
	/**Margin between bottom edge of chart and X axis (i.e., between edge of chart and chart field*/
	private int yAxisEdge = 0;
	/**Are max and min's for axes constrained to something other than max and min's among data points? 
	(used to make a series of charts have same ranges on axes*/
	private boolean maxXConstrained=false;
	private boolean maxYConstrained=false;
	private boolean maxZConstrained=false;
	private boolean minXConstrained=false;
	private boolean minYConstrained=false;
	private boolean minZConstrained=false;
	private boolean showOrigin = false;
	private String[] namesArray = null;
	private Color[] colorsArray = null;
	private Charter charter=null;
	private int numPoints;
	private MesquiteModule ownerModule;
	public TextRotator textRotator;
	public StringInABox yAxisWriter;
	private int idNumber=0;
	private NumberArray xArray;
	private NumberArray yArray;
	private NumberArray zArray;
	private Bits selected;
	private LongArray catArray;
	private MesquiteNumber minX, minY, maxX, maxY, minZ, maxZ, utilA, utilB;
	public MesquiteNumber axisMinX, axisMinY, axisMaxX, axisMaxY;
	/*the number of pixels the left margin of the field is offset (for scrolling -- home is 0, but if scrolled first pixel of field
	would be the effectively the nth pixel of the axis as it needs to be drawn*/
	private int xPixelBase = 0; 
	private int yPixelBase = 0; 
	private boolean sizeToFit = true;
	private int totalFieldWidth; 
	private int totalFieldHeight; 
	private int visWidth;
	private int visHeight;
	public String xAxisName="";
	public String yAxisName="";
	public String zAxisName="";
	public MesquiteNumber temp = new MesquiteNumber();
	//ChartScroll horizScroll, vertScroll;
	public static final int scrollWidth = 16;
	int initPosX = 0;
	int initPosY = 0;
	MesquiteCommand copyCommand;
	ChartTool arrowTool, infoTool;
	int orientation = 0; //specific to type of chart, used for charter etc. to record orientation
	int numPointsMunched = 0;
	public MesquiteChart(MesquiteModule ownerModule, int numToAllocate, int idNumber, Charter charter){
		this.ownerModule = ownerModule;
		this.idNumber=idNumber;
		setLayout(null);
		copyCommand = MesquiteModule.makeCommand("copy", this);
		extras = new ListableVector();
		xArray=new NumberArray(numToAllocate);
		yArray=new NumberArray(numToAllocate);
		selected = new Bits(numToAllocate);
		numPoints=0;
		minX = new MesquiteNumber();
		maxX = new MesquiteNumber();
		minY = new MesquiteNumber();
		maxY = new MesquiteNumber();
		minZ = new MesquiteNumber();
		maxZ = new MesquiteNumber();
		axisMinX = new MesquiteNumber();
		axisMaxX = new MesquiteNumber();
		axisMinY = new MesquiteNumber();
		axisMaxY = new MesquiteNumber();
		utilA = new MesquiteNumber();
		utilB = new MesquiteNumber();
		totalFieldWidth = MesquiteInteger.unassigned;
		totalFieldHeight = MesquiteInteger.unassigned;
		/*-----------

		add(t = new TextField());
		t.setBounds(0,0,64,64);
		t.setVisible(true);
		t.setBackground(Color.white);
		t.setText("88888");
		/*-----------*/
		field = new ChartField(this);
		togglePane(true);
		field.setVisible(true);
		setChartSize(chartWidth,chartHeight);
		setCharter(charter);

		//setBackground(Color.white);
	}
	public void dispose(){
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ChartExtra ex = (ChartExtra)obj;
				ex.dispose();
			}
		}
	}
	public ListableVector getExtras() {
		return extras;
	}
	public void addExtra(ChartExtra extra) {
		if (extras != null)
			extras.addElement(extra, false);
	}
	public void removeExtra(ChartExtra extra) {
		if (extras != null)
			extras.removeElement(extra, false);
	}
	public void removeAllExtrasOwned(MesquiteModule mb) {
		if (extras != null) {
			ChartExtra[] owned = new ChartExtra[extras.size()];
			int count =0;
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ChartExtra ex = (ChartExtra)obj;
				if (ex.getOwnerModule()== mb){
					owned[count++] = ex;
				}
			}
			for (int i=0; i<owned.length; i++)
				if (owned[i]!=null)
					removeExtra(owned[i]);
		}
	}
	public boolean findExtra(ChartExtra extra) {
		if (extras == null)
			return false;
		return (extras.indexOf(extra) >= 0);
	}
	public void destroyExtras(){
		if (extras!=null)
			extras.removeAllElements(true);
	}
	public boolean calculateAllExtras() {
		boolean remunch = false;
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ChartExtra ex = (ChartExtra)obj;
				remunch |= ex.doCalculations();
			}
		}
		return remunch;
	}
	public void drawAllBackgroundExtras(Graphics g) {
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ChartExtra ex = (ChartExtra)obj;
				if (ex instanceof ChartBkgdExtra) {
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					ex.drawOnChart(g);
				}
			}
		}
	}
	public void drawAllExtras(Graphics g) {
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ChartExtra ex = (ChartExtra)obj;
				if (!(ex instanceof ChartBkgdExtra)) {
					if (ownerModule==null || ownerModule.isDoomed()) 
						return;
					ex.drawOnChart( g);
				}
			}
		}
	}
	public void drawAllExtrasAxes(Graphics g) {
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ChartExtra ex = (ChartExtra)obj;
				ex.drawOnAxes(g, this);
			}
		}
	}
	public void printAllExtras(Graphics g) {
		if (extras != null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				ChartExtra ex = (ChartExtra)obj;
				if (ownerModule==null || ownerModule.isDoomed()) 
					return;
				ex.printOnChart(g);
			}
		}
	}
	public void addPanelPlease(Panel P){
		add(P);
	}
	public void removePanelPlease(Panel P){
		remove(P);
	}
	public void addComponentPlease(Component P){
		add(P);
	}
	public void removeComponentPlease(Component P){
		remove(P);
	}
	public void setArrowTool(ChartTool tool){
		arrowTool = tool;
	}
	public ChartTool getArrowTool(){
		return arrowTool;
	}
	public void setInfoTool(ChartTool tool){
		infoTool = tool;
	}
	public ChartTool getInfoTool(){
		return infoTool;
	}
	/*.................................................................................................................*/
	public long getID() {   
		return idNumber;
	}
	ChartScrollPane fieldPane;
	public void togglePane(boolean initiating) {
		if (!sizeToFit) {
			if (fieldPane==null || initiating) {
				if (!initiating)
					remove(field);
				fieldPane = new ChartScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
				//fieldPane.setBackground(Color.pink);
				sizeDisplay();
				add(fieldPane);
				fieldPane.addField(field);
				fieldPane.setScrollPosition(-initPosX, -initPosY);
				sizeDisplay();
				if (!initiating) {
					fieldPane.setVisible(true);
					field.repaint();
				}
			}
		}
		else {
			if (fieldPane!=null || initiating)  {
				if (!initiating)
					remove(fieldPane);
				fieldPane = null;
				sizeDisplay();
				add(field);
				sizeDisplay();
				if (!initiating) {
					field.setVisible(true);
					field.repaint();
				}
			}
		}
	}
	void sizeDisplay(){
		visWidth =chartWidth-getXAxisEdge();
		visHeight =chartHeight-getYAxisEdge();

		if (sizeToFit){
			field.setSize(visWidth, visHeight);
			field.setLocation(getXAxisEdge(), 0);
		}
		else {
			fieldPane.setSize(visWidth, visHeight);
			fieldPane.setLocation(getXAxisEdge(), 0);
			field.setLocation(0, 0);
			field.setSize(getFieldWidth(), getFieldHeight());
			fieldPane.doLayout();
		}
	}
	boolean setSizeDebugg = true;
	public void setChartSize(int w, int h) { 
		chartWidth=w;
		chartHeight=h;
		setSizeDebugg = false;
		setSize(w,h);
		setSizeDebugg = true;
		sizeDisplay();
	}
	public void setSize(int w, int h) {   
		if (setSizeDebugg)
			MesquiteMessage.warnProgrammer("Programmer: use setChartwSize instead of setSize for Charts: " + getName());
		super.setSize(w,h);
	}
	public void setTotalField(int w, int h) {   
		totalFieldWidth = w;
		totalFieldHeight = h;
		sizeDisplay();
	}
	public void setTotalFieldWidth(int w) {   
		totalFieldWidth = w;
		sizeDisplay();
	}
	public void setTotalFieldHeight( int h) {   
		totalFieldHeight = h;
		sizeDisplay();
	}
	public int getFieldWidth() {   
		if (sizeToFit)
			return visWidth;
		else if (!MesquiteInteger.isCombinable(totalFieldWidth))
			return visWidth-scrollWidth;
		else
			return totalFieldWidth;
	}
	public int getFieldHeight() {   
		if (sizeToFit)
			return visHeight;
		else if (!MesquiteInteger.isCombinable(totalFieldHeight))
			return visHeight-scrollWidth;
		else
			return totalFieldHeight;
	}
	public int getVisWidth() {   
		return visWidth;
	}
	public int getVisHeight() {   
		return visHeight;
	}
	public void setXPixelBase(int b) {   
		xPixelBase = b;
	}
	public void setYPixelBase(int b) {   
		yPixelBase = b;
	}
	public int getXPixelBase() {   
		return xPixelBase;
	}
	public int getYPixelBase() {   
		return yPixelBase;
	}
	public void setSizeToFit(boolean b) {   
		sizeToFit = b;
		togglePane(false);
		repaint();
		if (fieldPane!=null)
			fieldPane.repaint();
		field.repaint();
	}
	public boolean getSizeToFit() {   
		return sizeToFit;
	}
	public void setXAxisEdge(int edge) {   
		xAxisEdge=edge;
		sizeDisplay();
	}
	public void setYAxisEdge(int edge) {   
		yAxisEdge=edge;
		sizeDisplay();
	}
	public int getXAxisEdge() {   
		return xAxisEdge;
	}
	public int getYAxisEdge() {   
		return yAxisEdge;
	}
	public int getMargin() {   
		return margin;
	}
	public int getChartWidth() {   
		return chartWidth;
	}
	public int getChartHeight() {   
		return chartHeight;
	}
	public int getNumPoints() {   
		return numPoints;
	}
	public int getNumPointsMunched() {   
		return numPointsMunched;
	}
	public Charter getCharter() {   
		return charter;
	}
	public void setCharter(Charter c) {   
		if (c != charter && c!= null) {
			if (charter!=null)
				charter.close();
			this.charter=c;
			charter.open(this);
			charter.calculateChart(this);
			repaint();
			if (field!= null)
				field.repaint();
		}
	}
	public ChartField getField() {
		return field;
	}
	public MesquiteModule getOwnerModule() {
		return ownerModule;
	}

	/**
	public void setMinimumX(MesquiteNumber num) {   
		minX.setValue(num);
	}
	public void setMinimumY(MesquiteNumber num) {   
		minY.setValue(num);
	}
	public void setMinimumZ(MesquiteNumber num) {   
		minZ.setValue(num);
	}
/**/

	public MesquiteNumber getMinimumX() {   
		return minX;
	}
	public MesquiteNumber getMinimumY() {   
		return minY;
	}
	public MesquiteNumber getMinimumZ() {   
		return minZ;
	}
	public MesquiteNumber getMaximumX() {   
		return maxX;
	}
	public MesquiteNumber getMaximumY() {   
		return maxY;
	}
	public MesquiteNumber getMaximumZ() {   
		return maxZ;
	}
	public MesquiteNumber getAxisMinimumX() {   
		if (getShowOrigin() && axisMinX.isMoreThan(0)) axisMinX.setValue(0);
		return axisMinX;
	}
	public MesquiteNumber getAxisMinimumY() {   
		if (getShowOrigin() && axisMinY.isMoreThan(0)) axisMinY.setValue(0);
		return axisMinY;
	}
	public void putVisMinimumX(MesquiteNumber num) {   
		if (num==null)
			return;
		if (sizeToFit || fieldPane==null || fieldPane.getHAdjustable() == null)
			num.setValue(getAxisMinimumX());
		else {
			Adjustable p = fieldPane.getHAdjustable();
			double d = ((double)p.getValue())/(p.getMaximum() - p.getMinimum()) * (axisMaxX.getDoubleValue()-axisMinX.getDoubleValue());
			num.setValue(d);
		}
		//calculate scroll position on x
	}
	public void putVisMinimumY(MesquiteNumber num) {   
		if (num==null)
			return;
		if (sizeToFit || fieldPane==null || fieldPane.getVAdjustable() == null)
			num.setValue(getAxisMinimumY());
		else {
			Adjustable p = fieldPane.getVAdjustable();
			double d = ((double)p.getValue())/(p.getMaximum() - p.getMinimum()) * (axisMaxY.getDoubleValue()-axisMinY.getDoubleValue());
			num.setValue(d);
		}
	}
	public int scrollXOffset() {   
		if (sizeToFit || fieldPane==null || fieldPane.getHAdjustable() == null)
			return 0;
		else {
			Adjustable p = fieldPane.getHAdjustable();
			if (p==null)
				return 0;
			return p.getValue();
		}
	}
	public int scrollYOffset() {   
		if (sizeToFit || fieldPane==null || fieldPane.getVAdjustable() == null)
			return 0;
		else {
			Adjustable p = fieldPane.getVAdjustable();
			if (p==null)
				return 0;
			return p.getValue();
		}
	}
	public MesquiteNumber getAxisMaximumX() {   
		return axisMaxX;
	}
	public MesquiteNumber getAxisMaximumY() {   
		return axisMaxY;
	}
	public NumberArray getXArray() {
		return xArray;
	}
	public NumberArray getYArray() {
		return yArray;
	}
	public NumberArray getZArray() {
		return zArray;
	}
	public MesquiteNumber getXTotal() {
		return xArray.getTotal();
	}
	public MesquiteNumber getYTotal() {
		return yArray.getTotal();
	}
	public MesquiteNumber getZTotal() {
		return zArray.getTotal();
	}
	public Bits getSelected() {
		return selected;
	}

	/*.................................................................................................................*/
	public MesquiteCommand getCopyCommand() {
		return copyCommand;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Copies information from the chart (or its selected points)", null, commandName, "copy")) {
			String c = "";
			MesquiteWindow f = MesquiteWindow.windowOfItem(this);
			if (f instanceof MesquiteWindow){
				MesquiteWindow w = (MesquiteWindow)f;
				if (w.getMode()!=InfoBar.GRAPHICS)
					c = w.getTextContents();
				else
					c = getTextVersion("\t", true, true);
			}
			else
				c = getTextVersion("\t", true, true);
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection ss = new StringSelection(c);
			clip.setContents(ss, ss);
			return c;
		}
		else if (checker.compare(this.getClass(), "Selects all points", null, commandName, "selectAll")) {
			selectAllPoints();
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	public String getTextVersion(){
		return getTextVersion("\n", false, false);
	}
	public String getTextVersion(String spacer, boolean forClipboard, boolean selectedOnly){
		return getTextVersion(spacer, StringUtil.lineEnding(), forClipboard, selectedOnly);
	}
	public void putRawTextVersion(int[] ord, String spacer, String betweenPoints, boolean selectedOnly, boolean forClipboard, StringBuffer s){
		if (!forClipboard) {
			s.append( StringUtil.lineEnding() + "Individual Points" + StringUtil.lineEnding());
		}
		boolean first = true;
		for (int j= 0;j<getNumPoints(); j++) {
			int i=j;
			if (ord !=null && j<ord.length)
				i = ord[j];
			if ((!selectedOnly || selected.isBitOn(i)) && (!xArray.isUnassigned(i) || !yArray.isUnassigned(i))) {
				if (!first)
					s.append(betweenPoints);
				first = false;
				s.append(xArray.toString(i) + spacer + yArray.toString(i)) ;
				if (zArray !=null)
					s.append(spacer + zArray.toString(i));
				if (!forClipboard && namesExist()){
					String ss = getName(i);
					if (ss!=null)
						s.append(" (" + ss + ")");
				}
			}
		}
	}

	public String getTextVersion(String spacer, String betweenPoints, boolean forClipboard, boolean selectedOnly){
		if (getNumPoints() == 0)
			return "The chart does not yet have any points.  It may be in the process of being recalculated or needing recalculation.";
		int[] ord =  getOrderByX();
		StringBuffer s = new StringBuffer();  
		if (selectedOnly && !selected.anyBitsOn())
			selectedOnly = false;
		if (forClipboard){
			putRawTextVersion(ord, spacer, betweenPoints, selectedOnly, forClipboard, s);
		}
		else {
			s.append("X Axis - " + xAxisName + spacer + "Y Axis - " + yAxisName);
			if (zArray !=null)
				s.append(spacer + "Z Axis - " + zAxisName);
			s.append(StringUtil.lineEnding());
			if (charter !=null){
				String fromCharter = charter.getTextVersion(this);
				if (fromCharter !=null) {
					if (zArray !=null)
						s.append(StringUtil.lineEnding() + "Values (X: Y: Z)");
					else
						s.append(StringUtil.lineEnding() + "Values (X: Y)");

					s.append(StringUtil.lineEnding() + fromCharter + StringUtil.lineEnding());
				}
			}

			if (extras != null) {
				int loc = s.length();
				boolean appended = false;
				Enumeration e = extras.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					ChartExtra ex = (ChartExtra)obj;
					String sEx =ex.writeOnChart();
					if (!StringUtil.blank(sEx)) {
						appended = true;
						s.append(StringUtil.lineEnding() + StringUtil.lineEnding() + sEx);
					}
				}
				if (appended)
					s.insert(loc,StringUtil.lineEnding() + "========================" + StringUtil.lineEnding());
			}
		}
		return s.toString();
	}
	public LongArray getCatArray() {
		return catArray;
	}
	public void setXAxisName(String name) {   
		xAxisName=name;
		//repaint();
	}
	public String getYAxisNameSuffix() {   
		if (getCharter()==null)
			return "";
		else {
			String s = getCharter().getYAxisNameSuffix();
			if (StringUtil.blank(s)) 
				return "";
			else return " " + s;
		}
	}
	public void setYAxisName(String name) {   
		yAxisName=name;
		if (textRotator==null)
			textRotator = new TextRotator(100);
		if (yAxisWriter==null)
			yAxisWriter = new StringInABox(name, getFont(), getVisHeight());
		else
			yAxisWriter.setString(name);
		//repaint();
	}
	public void setZAxisName(String name) {   
		zAxisName=name;
		if (textRotator==null)
			textRotator = new TextRotator(100);
		//repaint();
	}
	public String getXAxisName() {   
		return xAxisName;
	}
	public String getYAxisName() {   
		return yAxisName;
	}
	public String getZAxisName() {   
		return zAxisName;
	}
	public void zeroChart() {   
		xArray.zeroArray();
		yArray.zeroArray();
		if (zArray!=null)
			zArray.zeroArray();
		selected.clearAllBits();
		numPoints=0;
	}
	public void deassignChart() {   
		xArray.deassignArrayToInteger();
		yArray.deassignArrayToInteger();
		if (zArray!=null)
			zArray.deassignArrayToInteger();
		zArray = null;
		if (catArray!=null)
			catArray.deassignArray();
		selected.clearAllBits();
		catArray = null;
		namesArray = null;
		colorsArray = null;
		numPoints=0;
	}
	public boolean colorsExist() {
		return (colorsArray!=null);
	}
	boolean legalPoint(int index){
		return (index>=0 && index<numPoints);
	}
	public Color getColor(int index) {
		if (!legalPoint(index))
			return null;
		if (colorsArray!=null && index>=0 && index < colorsArray.length)
			return (colorsArray[index]);
		else 
			return null;
	}
	public void setColor(int point, Color color) {  
		if (!legalPoint(point))
			return;
		if (colorsArray==null) {
			colorsArray = new Color[numPoints+100];
		}
		else if (numPoints>=colorsArray.length) {
			Color[] temp = new Color[numPoints+100];
			for (int i = 0; i<colorsArray.length; i++)
				temp[i]=colorsArray[i];
			colorsArray = temp;
		}
		colorsArray[point] = color;
	}
	public boolean namesExist() {
		return (namesArray!=null);
	}
	public String getName(int index) {
		if (namesArray!=null && index>=0 && index < namesArray.length)
			return (namesArray[index]);
		else 
			return null;
	}
	public void setName(int point, String name) {   
		if (!legalPoint(point))
			return;
		if (namesArray==null) {
			namesArray = new String[numPoints+100];
		}
		else if (numPoints>=namesArray.length) {
			String[] temp = new String[numPoints+100];
			for (int i = 0; i<namesArray.length; i++)
				temp[i]=namesArray[i];
			namesArray = temp;
		}
		namesArray[point] = name;
	}
	//currently not used; points can be assigned to categories
	public void setCategory(int point, long category) {   
		if (!legalPoint(point))
			return;
		if (catArray == null && category !=MesquiteLong.unassigned) {
			catArray = new LongArray(numPoints + 100);
			catArray.deassignArray();
		}
		else if (catArray!=null && numPoints>= catArray.getSize())
			catArray.resetSize(numPoints + 100);
		if (catArray!=null)
			catArray.setValue(point, category);
	}
	public void selectPoint(int point){
		if (!legalPoint(point))
			return;
		selected.setBit(point);
	}
	public void deselectPoint(int point){
		if (!legalPoint(point))
			return;
		selected.clearBit(point);
	}
	public void selectAllPoints(){
		selected.setAllBits();
	}
	public void deselectAllPoints(){
		selected.clearAllBits();
	}
	public void synchronizePointSelection(Selectionable a){
		if (a==null)
			return;
		selected.clearAllBits();
		for (int i=0; i<numPoints && i<a.getNumberOfSelectableParts(); i++)
			if (a.getSelected(i)) {
				selectPoint(i);
			}
	}
	public int addPoint(MesquiteNumber x, MesquiteNumber y) { 
		if (numPoints+1>=xArray.getSize())
			xArray.resetSize(xArray.getSize()+100);
		xArray.setValue(numPoints, x);
		if (numPoints+1>=yArray.getSize())
			yArray.resetSize(yArray.getSize()+100);
		yArray.setValue(numPoints, y);
		if (numPoints+1>=selected.getSize())
			selected.resetSize(selected.getSize()+100);
		selected.clearBit(numPoints);
		numPoints++;
		return numPoints-1;
	}

	public int addSumPoint(MesquiteNumber x, MesquiteNumber y) {   
		temp.setValue(y);
		int already = xArray.findValue(x);
		if (already!=-1) {
			yArray.addValue(already, temp); //use temp because temp's value will be changed
			return already;
		}
		else {
			return addPoint(x,y);
		}
	}

	public int addPoint(MesquiteNumber x, MesquiteNumber y, MesquiteNumber z) {   

		if (z!=null) {
			if (zArray==null)
				zArray = new NumberArray(numPoints+1);
			if (numPoints+1>=zArray.getSize())
				zArray.resetSize(zArray.getSize()+100);
			zArray.setValue(numPoints, z);
		}
		return addPoint(x,y);
	}

	public void constrainMaximumX(MesquiteNumber c){
		maxXConstrained=true;
		maxX.setValue(c);
		axisMaxX.setValue(c);
	}
	public void constrainMaximumY(MesquiteNumber c){
		maxYConstrained=true;
		maxY.setValue(c);
		axisMaxY.setValue(c);
	}
	public void constrainMaximumZ(MesquiteNumber c){
		maxZConstrained=true;
		maxZ.setValue(c);
		//axisMaxZ.setValue(c);
	}
	public void constrainMinimumX(MesquiteNumber c){
		minXConstrained=true;
		minX.setValue(c);
		axisMinX.setValue(c);
	}
	public void constrainMinimumY(MesquiteNumber c){
		minYConstrained=true;
		minY.setValue(c);
		axisMinY.setValue(c);
	}
	public void constrainMinimumZ(MesquiteNumber c){
		minZConstrained=true;
		minZ.setValue(c);
		//axisMinZ.setValue(c);
	}

	public boolean isMaximumXConstrained(){
		return maxXConstrained;
	}
	public boolean isMaximumYConstrained(){
		return maxYConstrained;
	}
	public boolean isMaximumZConstrained(){
		return maxZConstrained;
	}
	public boolean isMinimumXConstrained(){
		return minXConstrained;
	}
	public boolean isMinimumYConstrained(){
		return minYConstrained;
	}
	public boolean isMinimumZConstrained(){
		return minZConstrained;
	}

	public void deConstrainMaximumX(){
		maxXConstrained=false;
	}
	public void deConstrainMaximumY(){
		maxYConstrained=false;
	}
	public void deConstrainMaximumZ(){
		maxZConstrained=false;
	}
	public void deConstrainMinimumX(){
		minXConstrained=false;
	}
	public void deConstrainMinimumY(){
		minYConstrained=false;
	}
	public void deConstrainMinimumZ(){
		minZConstrained=false;
	}
	public int[] getOrderByX () {
		int numPts = numPoints; //prevents problems if numPoints modified meanwhile
		int[] ord = new int[numPts];
		NumberArray copy = xArray.cloneArray();
		for (int i = 1; i < numPts; i++)
			ord[i]=i;
		for (int i = 1; i < numPts; i++) {
			for (int j= i-1; j>=0 && copy.firstIsGreater(j,j+1); j--) {
				copy.swapValues(j,j+1);
				int temp = ord[j];
				ord[j]=ord[j+1];
				ord[j+1]=temp;
			}
		}
		return ord;
	}
	public int[] getOrderByY () {
		int numPts = numPoints; //prevents problems if numPoints modified meanwhile
		int[] ord = new int[numPts];
		NumberArray copy = yArray.cloneArray();
		for (int i = 1; i < numPts; i++)
			ord[i]=i;
		for (int i = 1; i < numPts; i++) {
			for (int j= i-1; j>=0 && copy.firstIsGreater(j,j+1); j--) {
				copy.swapValues(j,j+1);
				int temp = ord[j];
				ord[j]=ord[j+1];
				ord[j+1]=temp;
			}
		}
		return ord;
	}
	public int[] getOrderByZ () {
		int numPts = numPoints; //prevents problems if numPoints modified meanwhile
		int[] ord = new int[numPts];
		for (int i = 1; i < numPts; i++)
			ord[i]=i;
		if (zArray==null)
			return ord;
		NumberArray copy = zArray.cloneArray();
		for (int i = 1; i < numPts; i++) {
			for (int j= i-1; j>=0 && copy.firstIsGreater(j,j+1); j--) {
				copy.swapValues(j,j+1);
				int temp = ord[j];
				ord[j]=ord[j+1];
				ord[j+1]=temp;
			}
		}
		return ord;
	}
	public void sortByX () {
		for (int i = 1; i < numPoints; i++) {
			for (int j= i-1; j>=0 && xArray.firstIsGreater(j,j+1); j--) {
				xArray.swapValues(j,j+1);
				yArray.swapValues(j,j+1);
				if (zArray !=null)
					zArray.swapValues(j,j+1);
				selected.swapValues(j, j+1);
			}
		}
	}
	public void sortByY () {
		for (int i = 1; i < numPoints; i++) {
			for (int j= i-1; j>=0 && yArray.firstIsGreater(j,j+1); j--) {
				xArray.swapValues(j,j+1);
				yArray.swapValues(j,j+1);
				if (zArray !=null)
					zArray.swapValues(j,j+1);
				selected.swapValues(j, j+1);
			}
		}
	}
	public void sortByZ () {
		if (zArray!=null) {
			for (int i = 1; i < numPoints; i++) {
				for (int j= i-1; j>=0 && zArray.firstIsGreater(j,j+1); j--) {
					xArray.swapValues(j,j+1);
					yArray.swapValues(j,j+1);
					zArray.swapValues(j,j+1);
					selected.swapValues(j, j+1);
				}
			}
		}
	}
	public void munch(){
		while (inMunch())
			;
		field.repaint();  //the field will call the chart to be repainted, so that axes are drawn afterward
	}
	private boolean inMunch(){
		int expandX=0;
		if (!minXConstrained) {
			xArray.placeMinimumValue(minX, numPoints);
		}
		if (!maxXConstrained) {
			xArray.placeMaximumValue(maxX, numPoints);
		}
		if (!minYConstrained) {
			yArray.placeMinimumValue(minY, numPoints);

		}
		if (!maxYConstrained) {
			yArray.placeMaximumValue(maxY, numPoints);
		}
		if (zArray!=null) {
			if (!minZConstrained)
				zArray.placeMinimumValue(minZ, numPoints);
			if (!maxZConstrained) 
				zArray.placeMaximumValue(maxZ, numPoints);
		}
		utilA.setValue(maxX);
		utilA.subtract(minX);
		utilA.divideBy(20);
		if (utilA.isZero())
			utilA.add(1);
		if (!minXConstrained) {
			utilB.setValue(minX);
			utilB.subtract(utilA);
			axisMinX.setValue(utilB);
		}
		else {
			axisMinX.setValue(minX);
		}
		if (!maxXConstrained) {
			utilB.setValue(maxX);
			utilB.add(utilA);
			axisMaxX.setValue(utilB);
		}
		else {
			axisMaxX.setValue(maxX);
		}
		if (yArray.getValueClass() == NumberArray.DOUBLE){
			utilA.setValue(maxY);
			utilA.subtract(minY);
			utilA.divideBy(20);
			if (utilA.isZero())
				utilA.add(1);
			if (!minYConstrained) {
				utilB.setValue(minY);
				utilB.subtract(utilA);
				axisMinY.setValue(utilB);
			}
			else {
				axisMinY.setValue(minY);
			}
			if (!maxYConstrained) {
				utilB.setValue(maxY);
				utilB.add(utilA);
				axisMaxY.setValue(utilB);
			}
			else {
				axisMaxY.setValue(maxY);
			}
		}
		else {
			axisMinY.setValue(minY);
			axisMaxY.setValue(maxY);
			if (maxY.getLongValue()-minY.getLongValue()<20){
				axisMinY.subtract(1);
				axisMaxY.add(1);
			} 
			else {
				long inc = (maxY.getLongValue()-minY.getLongValue())/10;
				if (inc <1)
					inc = 1;
				axisMinY.subtract(inc); //'FIX
				axisMaxY.add(inc);
			}
		}
		numPointsMunched = numPoints;

		if (charter!=null)
			charter.calculateChart(this);
		else
			MesquiteMessage.printStackTrace("charter null in MesquiteChart");
		return calculateAllExtras();
	}
	public Color getFieldBackground() {   
		return field.getBackground();
	}
	public void showQuickMessage(int whichPoint, int x, int y, String message){
		if (charter!=null)
			charter.showQuickMessage(this, whichPoint, x, y, message);
	}
	public void hideQuickMessage(){
		charter.hideQuickMessage(this);
	}
	/**/
	public void drawAxes(boolean printing) {
		Graphics g = getGraphics();
		if (charter!=null && g!=null) {
			charter.drawAxes(g, printing, chartWidth, chartHeight, this);
			drawAllExtrasAxes(g);
			g.dispose();
		}
	}

	/*...............................................................................................................*/
	public void print(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		Color bgc = getBackground();
		setBackground(Color.white);
		if (charter!=null)
			charter.drawAxes(g, true, chartWidth, chartHeight, this);
		setBackground(bgc);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (charter!=null)
			charter.drawAxes(g, false, chartWidth, chartHeight, this);
		MesquiteWindow.uncheckDoomed(this);
	}


	/*.................................................................................................................*/
	public void printChart(MesquitePrintJob pjob, MesquiteWindow window) {
		if (pjob != null) {
			pjob.printComponent(this, new Dimension(getFieldWidth() + getXAxisEdge(), getFieldHeight() + getYAxisEdge() + 50), null);
			/*
	 		field.suppressBlank = true;
			Dimension dim = pjob.getPageDimension();
			int w;
			int h;
 			int currentWidth = getChartWidth();
 			int currentHeight = getChartHeight();
 			if (currentHeight == 0 || currentWidth == 0) {
 				w = dim.width;
 				h = dim.height;
 			}
 			else if (((double)dim.width)/currentWidth > ((double)dim.height)/currentHeight) {
 				w = (int)(((double)dim.height)/currentHeight * currentWidth);
 				h = dim.height;
 			}
 			else {
 				w = dim.width;
 				h = (int)(((double)dim.width)/currentWidth * currentHeight);
 			}
			setChartSize(w, h); // why doesn't this work???
			if (sizeToFit){
	 			Graphics pg = pjob.getGraphics();
	 			if (pg!=null) {
	 				pg.setFont(getFont());
	 				printAll(pg); 
	 				pg.dispose();
	 			}
 			}
 			else {
				int x = getXPixelBase();
				int y = getYPixelBase();
 	 			int pixelHeight = 0;
				while (pixelHeight<=totalFieldHeight) {
			 		setYPixelBase(pixelHeight);
	 	 			int pixelWidth = 0;
					while (pixelWidth<=totalFieldWidth) {
		    	 			Graphics pg = pjob.getGraphics();
			 			setXPixelBase(pixelWidth);
		    	 			if (pg!=null) {
		    	 				pg.setFont(getFont());
		    	 				printAll(pg); 
		    	 				pg.dispose();
		    	 			}
		    	 			pixelWidth += visWidth;
		 			}
		    	 		pixelHeight += visHeight;
	 			}
	 			setXPixelBase(x);
	 			setYPixelBase(y);
 			}
			setChartSize(currentWidth, currentHeight);
	 		field.suppressBlank = false;
			 */
		}
	}
	/*.................................................................................................................*/
	/**
	 * @author Peter Midford
	 */
	public void chartToPDF(MesquitePDFFile pdff, MesquiteWindow window, int fitToPage) {
		if (pdff != null) {
			Graphics g2 = pdff.getPDFGraphicsForComponent(this, new Dimension(getFieldWidth()+getXAxisEdge(), getFieldHeight()+getYAxisEdge()+50));
			this.print(g2);
			g2.translate(getXAxisEdge(),0);
			field.print(g2);
			this.printAllExtras(g2);
			pdff.end();
		}
	}
	/*.................................................................................................................*/
	public void drawBlank() {   
		field.drawBlank();
	}

	public void setOrientation(int orient){
		orientation = orient;
	}
	public int getOrientation(){
		return orientation;
	}
	boolean useAverage=false;
	public void setUseAverage(boolean ua){
		useAverage = ua;
	}
	public boolean getUseAverage(){
		return useAverage;
	}
	public boolean getShowOrigin() {
		return showOrigin;
	}
	public void setShowOrigin(boolean showOrigin) {
		this.showOrigin = showOrigin;
	}
}
/* ======================================================================== */
class ChartScrollPane extends ScrollPane {
	public ChartScrollPane (int scrollPolicy) {
		super(scrollPolicy);
	}
	public void addField(Component c){
		addImpl(c, null, 0);
	}
}


/* ======================================================================== */
/* scrollbar for chart */
class ChartScroll extends MesquiteScrollbar {
	MesquiteChart chart;
	public ChartScroll (MesquiteChart chart, int orientation, int value, int visible, int min, int max){
		super(orientation, value, visible, min, max);
		this.chart=chart;
	}

	public void scrollTouched(){
		int currentValue = getValue();
		//chart.setPosition(this, currentValue);
	}
}



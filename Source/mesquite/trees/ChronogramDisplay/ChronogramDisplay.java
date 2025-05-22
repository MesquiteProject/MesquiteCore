/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ChronogramDisplay;
/*~~  */

import java.util.*;


import java.awt.*;
import java.awt.event.KeyEvent;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayBkgdExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.tree.TreeDisplayRequests;
import mesquite.lib.tree.TreeDrawing;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.GraphicsUtil;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteSubmenu;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.StringInABox;
import mesquite.lib.ui.TextRotator;

/* ======================================================================== */
public class ChronogramDisplay extends TreeDisplayAssistantD {
	
	public Vector extras;
	ChonogramDisplayExtra newPj;
	MesquiteBoolean showGrayBars = new MesquiteBoolean(true);
	MesquiteBoolean showGeologicalTimeScale = new MesquiteBoolean(false);
	int grayBarInterval = 10;
	MesquiteBoolean showEpochNames = new MesquiteBoolean(true);
	MesquiteBoolean  showPeriodNames = new MesquiteBoolean(true);
	int epochFontSize = 10;
	int periodFontSize=12;
	MesquiteSubmenuSpec mss;
	MesquiteString periodFontSizeName;
	MesquiteSubmenuSpec mss2;
	MesquiteString epochFontSizeName;
	public static NameReference errorBarNameRef = NameReference.getNameReference("height 95% HPD");
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){

		extras = new Vector();  // a vector in case there are multiple tree displays in a window

		makeMenu("Chronogram");
		resetContainingMenuBar();
		addCheckMenuItem(null,"Show Time Bars", makeCommand("showGrayBars",  this), showGrayBars);
		addMenuItem( "Time Bar Intervals...", makeCommand("grayBarInterval",  this));
		addCheckMenuItem(null,"Show Geological Time Scale", makeCommand("showGeologicalTimeScale",  this), showGeologicalTimeScale);
		// show nodeCircles or not
		// years as Integers or doubles
		// font sizes for GTS

		addCheckMenuItem(null,"Show Period Names", makeCommand("showPeriodNames",  this), showPeriodNames);
		periodFontSizeName = new MesquiteString(Integer.toString(periodFontSize));
		mss = addSubmenu(null, "Period Font Size", makeCommand("setPeriodFontSize", this), MesquiteSubmenu.getFontSizeList());
		mss.setList(MesquiteSubmenu.getFontSizeList());
		mss.setDocumentItems(false);
		mss.setSelected(periodFontSizeName);
				
		addCheckMenuItem(null,"Show Epoch Names", makeCommand("showEpochNames",  this), showEpochNames);
		epochFontSizeName = new MesquiteString(Integer.toString(epochFontSize));
		mss2 = addSubmenu(null, "Epoch Font Size", makeCommand("setEpochFontSize", this), MesquiteSubmenu.getFontSizeList());
		mss2.setList(MesquiteSubmenu.getFontSizeList());
		mss2.setDocumentItems(false);
		mss2.setSelected(epochFontSizeName);
				

		addMenuSeparator();
		addMenuItem( "Close Chronogram", makeCommand("closeChronogram",  this));
		MesquiteTrunk.resetMenuItemEnabling();
		//add menu items
		return true;
	} 

	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		newPj = new ChonogramDisplayExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Chronogram Display";
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "grayBarInterval", grayBarInterval);  
		StringUtil.appendXMLTag(buffer, 2, "showGrayBars", showGrayBars);  
		StringUtil.appendXMLTag(buffer, 2, "showGeologicalTimeScale", showGeologicalTimeScale);  
		StringUtil.appendXMLTag(buffer, 2, "showPeriodNames", showPeriodNames);  
		StringUtil.appendXMLTag(buffer, 2, "showEpochNames", showEpochNames);  
		StringUtil.appendXMLTag(buffer, 2, "periodFontSize", periodFontSize);  
		StringUtil.appendXMLTag(buffer, 2, "epochFontSize", epochFontSize);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("showGrayBars".equalsIgnoreCase(tag))
			showGrayBars.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("showGeologicalTimeScale".equalsIgnoreCase(tag))
			showGeologicalTimeScale.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("showPeriodNames".equalsIgnoreCase(tag))
			showPeriodNames.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("showEpochNames".equalsIgnoreCase(tag))
			showEpochNames.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("grayBarInterval".equalsIgnoreCase(tag))
			grayBarInterval = MesquiteInteger.fromString(content);
		if ("periodFontSize".equalsIgnoreCase(tag))
			periodFontSize = MesquiteInteger.fromString(content);
		if ("epochFontSize".equalsIgnoreCase(tag))
			epochFontSize = MesquiteInteger.fromString(content);
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("grayBarInterval " + grayBarInterval); 
		temp.addLine("showGrayBars " + showGrayBars.toString());
		temp.addLine("showGeologicalTimeScale " + showGeologicalTimeScale.toString());
		temp.addLine("setPeriodFontSize " + periodFontSize); 
		temp.addLine("setEpochFontSize " + epochFontSize); 
		temp.addLine("showPeriodNames " + showPeriodNames.toString());
		temp.addLine("showEpochNames " + showEpochNames.toString());

		//	temp.addLine("setOffset " + xOffset + "  " + yOffset); 

		return temp;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the time interval for the gray bars", "[width in time units]", commandName, "grayBarInterval")) {

			int newWidth= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newWidth))
				newWidth = MesquiteInteger.queryInteger(containerOfModule(), "Set time intervals for gray bars", "Interval:", grayBarInterval, 1, 99);
			if (newWidth>0 && newWidth!=grayBarInterval) {
				grayBarInterval=newWidth;
				Enumeration e = extras.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ChonogramDisplayExtra) {
						ChonogramDisplayExtra cDE = (ChonogramDisplayExtra)obj;
						cDE.setTimeBarInterval(newWidth);
					}
				}

				if ( !MesquiteThread.isScripting()) parametersChanged();
			}

		}
		else if (checker.compare(this.getClass(), "Sets the font size used for the period names", "[size of font]", commandName, "setPeriodFontSize")) {
			int fontSize = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(fontSize) && fontSize>2) {
				periodFontSize=fontSize;
				periodFontSizeName.setValue(Integer.toString(periodFontSize));
				newPj.setPeriodFontSize(periodFontSize);
				newPj.update();
				if ( !MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the font size used for the epoch names", "[size of font]", commandName, "setEpochFontSize")) {
			int fontSize = MesquiteInteger.fromString(arguments);
			if (MesquiteInteger.isCombinable(fontSize) && fontSize>2) {
				epochFontSize=fontSize;
				epochFontSizeName.setValue(Integer.toString(epochFontSize));
				newPj.setEpochFontSize(epochFontSize);
				newPj.update();
				if ( !MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Show Period Names", "", commandName, "showPeriodNames")) {
			boolean current = showPeriodNames.getValue();
			String arg = parser.getFirstToken(arguments);
			if (StringUtil.blank(arg))
				showPeriodNames.toggleValue(arg);
			else {
				showPeriodNames.setValue(MesquiteBoolean.fromTrueFalseString(arg));
			}
			if (current!=showPeriodNames.getValue()) {
				Enumeration e = extras.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ChonogramDisplayExtra) {
						ChonogramDisplayExtra cDE = (ChonogramDisplayExtra)obj;
						cDE.setShowPeriodNames( showPeriodNames.getValue());
					}
				}
				if ( !MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Show Epoch Names", "", commandName, "showEpochNames")) {
			boolean current = showEpochNames.getValue();
			String arg = parser.getFirstToken(arguments);
			if (StringUtil.blank(arg))
				showEpochNames.toggleValue(arg);
			else {
				showEpochNames.setValue(MesquiteBoolean.fromTrueFalseString(arg));
			}
			if (current!=showEpochNames.getValue()) {
				Enumeration e = extras.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ChonogramDisplayExtra) {
						ChonogramDisplayExtra cDE = (ChonogramDisplayExtra)obj;
						cDE.setShowEpochNames( showEpochNames.getValue());
					}
				}
				if ( !MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Show Gray Time Bars", "", commandName, "showGrayBars")) {
			boolean current = showGrayBars.getValue();
			String arg = parser.getFirstToken(arguments);
			if (StringUtil.blank(arg))
				showGrayBars.toggleValue(arg);
			else {
				showGrayBars.setValue(MesquiteBoolean.fromTrueFalseString(arg));
			}
			if (current!=showGrayBars.getValue()) {
				Enumeration e = extras.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ChonogramDisplayExtra) {
						ChonogramDisplayExtra cDE = (ChonogramDisplayExtra)obj;
						cDE.setDrawGrayTimeBars(showGrayBars.getValue());
					}
				}
				if ( !MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Show Geological Time Scale", "", commandName, "showGeologicalTimeScale")) {
			boolean current = showGeologicalTimeScale.getValue();
			String arg = parser.getFirstToken(arguments);
			if (StringUtil.blank(arg))
				showGeologicalTimeScale.toggleValue(arg);
			else {
				showGeologicalTimeScale.setValue(MesquiteBoolean.fromTrueFalseString(arg));
			}
			if (current!=showGeologicalTimeScale.getValue()) {
				Enumeration e = extras.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ChonogramDisplayExtra) {
						ChonogramDisplayExtra cDE = (ChonogramDisplayExtra)obj;
						cDE.setDrawGeologicalTimeScale(showGeologicalTimeScale.getValue());
					}
				}
				if ( !MesquiteThread.isScripting()) parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Turns off chronogram display", null, commandName, "closeChronogram")) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ChonogramDisplayExtra) {
					ChonogramDisplayExtra cDE = (ChonogramDisplayExtra)obj;
					cDE.turnOff();
				}
			}
			iQuit();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows graphics associated with chronogram." ;
	}
	public boolean isSubstantive(){
		return true;
	}   	 

}

/* ======================================================================== */
class ChonogramDisplayExtra extends TreeDisplayExtra implements TreeDisplayBkgdExtra  {
	ChronogramDisplay ownerModule;
	NameReference heightNameRef = NameReference.getNameReference("height");
	TreeDisplay treeDisplay;
	TextRotator textRotator;

	boolean drawGrayTimeBars = true;
	boolean drawGeologicalTimeScale = false;

	int nodeCircleSize = 10;
	int HPDBarHeight = 6;
	int timeBarInterval = 10;
	boolean showEpochNames = true;

	boolean showPeriodNames = true;
	int epochFontSize = 10;

	int periodFontSize=12;


	boolean yearsAsIntegers = true;
	int gapBetweenScaleAndGeologicalTimeScale = 30;

	public ChonogramDisplayExtra (ChronogramDisplay ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		this.ownerModule = ownerModule;
		this.treeDisplay = treeDisplay;
		treeDisplay.inhibitDefaultScaleBar = true;
		textRotator = new TextRotator();

	}

	double pixelsPerTime = MesquiteDouble.unassigned;
	double startXValue = MesquiteDouble.unassigned;


	/*.................................................................................................................*/
	void reset() {
		pixelsPerTime = MesquiteDouble.unassigned;
		startXValue = MesquiteDouble.unassigned;
	}
	/*.................................................................................................................*/
	/* The TreeDisplayRequests object has public int fields leftBorder, topBorder, rightBorder, bottomBorder (in pixels and in screen orientation)
	 * and a public double field extraDepthAtRoot (in branch lengths units and rootward regardless of screen orientation) */
	TreeDisplayRequests borderRequests = new TreeDisplayRequests(0, 0, 0, 100, 0, 20); 
	public TreeDisplayRequests getRequestsOfTreeDisplay(Tree tree, TreeDrawing treeDrawing){
		calculateHeights(treeDisplay.getGraphics());
		borderRequests.bottomBorder = epochHeight+periodHeight;
		borderRequests.extraDepthAtRoot = findOldest((MesquiteTree)tree, treeDrawing.getDrawnRoot()) - getHeight((MesquiteTree)tree,  treeDrawing.getDrawnRoot());
		return borderRequests;
	}
	/*.................................................................................................................*/

	//NOTE: set treeDisplay.inhibitDefaultScaleBar = true;
	double getXFromTime(double time) {
		if (pixelsPerTime==MesquiteDouble.unassigned || startXValue==MesquiteDouble.unassigned) {
			double[] scale = treeDisplay.getScale();
			startXValue=scale[startX];
			double totalScale = scale[endScaleValue] - scale[startScaleValue];
			pixelsPerTime = (scale[startX]-scale[endX])/totalScale;
		}
		return startXValue-pixelsPerTime*time;
	}
	/*.................................................................................................................*/
	public   void drawNodes(MesquiteTree tree, int node, Graphics g) {
		if (tree.withinCollapsedClade(node))
			return;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawNodes(tree, d, g);

		//getting node positions
		double x= treeDisplay.getTreeDrawing().getX(node);
		double y = treeDisplay.getTreeDrawing().getY(node);

		//getting array at node

		Taxa taxa = tree.getTaxa();
		if (!tree.nodeIsTerminal(node)) {
			double v1 = getLower(tree, node);
			double v2 = getUpper(tree, node);
			int lowerX = (int)getXFromTime(v1);
			int upperX = (int)getXFromTime(v2);
			int halfBar = (int)(HPDBarHeight/2.0);
			
			g.setColor(new Color(0x0101ff));
			Composite composite = ColorDistribution.getComposite(g);
			ColorDistribution.setTransparentGraphics(g,0.3f);		
			GraphicsUtil.fillRoundRect(g,upperX, (int)(y-halfBar), (int)(lowerX-upperX), HPDBarHeight, HPDBarHeight, HPDBarHeight);
			ColorDistribution.setComposite(g, composite);		

			g.setColor(Color.blue);
			g.fillOval((int)(x-nodeCircleSize/2), (int)(y-nodeCircleSize/2), nodeCircleSize, nodeCircleSize);
		}
		else {
			//Debugg.println(taxa.getTaxonName(node)+" x="+x +", y="+y);
		}

	}
	/*.................................................................................................................*/
	public double findOldest(MesquiteTree tree, int node) {
		if (tree.withinCollapsedClade(node))
			return MesquiteDouble.unassigned;
		if (!tree.nodeIsTerminal(node)) {
			double oldest = getUpper(tree, node);
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				oldest = MesquiteDouble.maximum(oldest, findOldest(tree, d));
			return oldest;
		}
		return MesquiteDouble.unassigned;
	}
	
	/*.................................................................................................................*/
	double getLower(MesquiteTree tree, int node){
		Object obj = tree.getAssociatedObject(ChronogramDisplay.errorBarNameRef, node);
		if(obj != null && obj instanceof DoubleArray){
			DoubleArray bar = (DoubleArray)obj;
			if (bar.getSize() == 2){
				return  bar.getValue(0);
			}
		}
		return MesquiteDouble.unassigned;
	}
	/*.................................................................................................................*/
	double getUpper(MesquiteTree tree, int node){
		Object obj = tree.getAssociatedObject(ChronogramDisplay.errorBarNameRef, node);
		if(obj != null && obj instanceof DoubleArray){
			DoubleArray bar = (DoubleArray)obj;
			if (bar.getSize() == 2){
				return  bar.getValue(1);
			}
		}
		return MesquiteDouble.unassigned;
	}
	/*.................................................................................................................*/
	double getHeight(MesquiteTree tree, int node){
		double nodeHeight =  tree.getAssociatedDouble(heightNameRef, node);
		if (!MesquiteDouble.isCombinable(nodeHeight))
			nodeHeight = (getLower(tree, node)+getUpper(tree, node))/2.0;
		return  nodeHeight;

	}
	static int startX = 0;
	static int startY = 1;
	static int endX = 2;
	static int endY = 3;
	static int startScaleValue = 4;
	static int endScaleValue = 5;

	int epochHeight = 60;
	int periodHeight = 40;
	/*.................................................................................................................*/
	void calculateHeights(Graphics g) {
		epochHeight = 60;
		if (g!=null) {
			GraphicsUtil.setFontSize(epochFontSize, g);
			int max = MesquiteInteger.maximum(GraphicsUtil.stringWidth(g, "Terreneuvian"), GraphicsUtil.stringWidth(g, "Pennsylvanian"));
			if (epochHeight < max) 
				epochHeight=max+8;
		}
	}
	/*.................................................................................................................*/
	public void drawPeriodBox(String name, int top, double oldestTime, double youngestTime,Graphics2D g, Color color) {
		GraphicsUtil.setFontSize(periodFontSize, g);
		int textYPos = top+epochHeight+periodHeight/2;
		double oldestX = getXFromTime(oldestTime);
		double youngestX = getXFromTime(youngestTime);
		int textWidth = GraphicsUtil.stringWidth(g, name);
		boolean drawText = youngestX-oldestX-textWidth>4;
		int fontSize = periodFontSize;

		if (!drawText) {  // won't fit
			for (fontSize=periodFontSize-1; fontSize>3; fontSize--) {
				GraphicsUtil.setFontSize(fontSize, g);
				textWidth = GraphicsUtil.stringWidth(g, name);
				drawText = youngestX-oldestX-textWidth>4;
				if (drawText)
					break;

			}
		
		}

		
		if (youngestX>0) {
			g.setColor(color);
			GraphicsUtil.fillRect(g,oldestX,top+epochHeight,(youngestX-oldestX),periodHeight);
			g.setColor(Color.black);
			GraphicsUtil.drawRect(g,oldestX,top+epochHeight,(youngestX-oldestX),periodHeight);
			if (drawText && showPeriodNames) {
				GraphicsUtil.setFontSize(fontSize, g);
				g.drawString(name, (int)(oldestX+(youngestX-oldestX - GraphicsUtil.stringWidth(g, name))/2), textYPos);
			}
		}

	}
	/*.................................................................................................................*/
	
	
	/*.................................................................................................................*/
	public void drawEpochBox(String name, int top, double oldestTime, double youngestTime,Graphics2D g, Color color) {
		GraphicsUtil.setFontSize(epochFontSize, g);
		int textYPos = top + ((epochHeight - GraphicsUtil.stringWidth(g, name))/2);
		int textWidth = GraphicsUtil.stringHeight(g, name);
		int fontSize = epochFontSize;
		
		
		//textYPos = top+epochHeight;
		//textYPos = top;
		double oldestX = getXFromTime(oldestTime);
		double youngestX = getXFromTime(youngestTime);
		boolean drawText = youngestX-oldestX-textWidth>2;
		
		if (!drawText) {  // won't fit
			for (fontSize=epochFontSize-1; fontSize>3; fontSize--) {
				GraphicsUtil.setFontSize(fontSize, g);
				textWidth = GraphicsUtil.stringHeight(g, name);
				drawText = youngestX-oldestX-textWidth>2;
				textYPos = top + ((epochHeight - GraphicsUtil.stringWidth(g, name))/2);
				if (drawText)
					break;

			}
		
		}
		
		if (youngestX>0) {
			g.setColor(color);
			GraphicsUtil.fillRect(g,oldestX,top,(youngestX-oldestX),epochHeight);
			g.setColor(Color.black);
			GraphicsUtil.drawRect(g,oldestX,top,(youngestX-oldestX),epochHeight);
			if (drawText && showEpochNames) { 
				GraphicsUtil.setFontSize(fontSize, g);
				textRotator.drawRotatedText(name, g, (int)(oldestX+(youngestX-oldestX-textWidth)/2+2), textYPos);
			}
		}

	}
	/*.................................................................................................................*/
	public void drawEras(int top, Graphics g) {
		Color t = g.getColor();
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D)g;
			calculateHeights(g2);

//Epochs
			drawEpochBox("",top,2.58, 0,g2, new Color(0xfff1ae)); 
			drawEpochBox("Pliocene",top,5.33,2.58, g2, new Color(0xffffa6)); 
			drawEpochBox("Miocene",top,23.04,5.33, g2, new Color(0xffff54)); 
			drawEpochBox("Oligocene", top,33.9,23.04, g2, new Color(0xf4c284));
			drawEpochBox("Eocene", top,56,33.9, g2, new Color(0xf2b778));
			drawEpochBox("Paleocene", top,66,56, g2, new Color(0xf1ab6c));
			drawEpochBox("Upper", top,100.5,66, g2, new Color(0xb0d761));  // Cretaceous
			drawEpochBox("Lower", top,143.1,100.5, g2, new Color(0x9acb67));  // Cretaceous

			drawEpochBox("Upper", top,161.5,143.1, g2, new Color(0xbde2ec));  // Jurassic
			drawEpochBox("Middle", top,174.7,161.5, g2, new Color(0x92cdd6));  // Jurassic
			drawEpochBox("Lower", top,201.4,174.7, g2, new Color(0x61accc));  // Jurassic

			drawEpochBox("Upper", top,237,201.4, g2, new Color(0xb68ec0));  // Triassic
			drawEpochBox("Middle", top,246.7,237, g2, new Color(0xa76bad));  // Triassic
			drawEpochBox("Lower", top,251.9,246.7, g2, new Color(0x8d3f95));  // Triassic

			drawEpochBox("Lopingian", top,259.51,251.9, g2, new Color(0xefab98));  // Permian
			drawEpochBox("Guadalupian", top,274.4,259.51, g2, new Color(0xea7c64));  // Permian
			drawEpochBox("Cisuralian", top,298.9,274.4, g2, new Color(0xde624e));  // Permian

			drawEpochBox("Pennsylvanian", top,323.4,298.9, g2, new Color(0xa1c1b6));  // Carboniferous
			drawEpochBox("Mississipian", top,358.86,323.4, g2, new Color(0x6f8e6a));  // Carboniferous

			drawEpochBox("Upper", top,382.31,358.86, g2, new Color(0xF1E19D));  // Devonian
			drawEpochBox("Middle", top,393.47,382.31, g2, new Color(0xF1C868));  // Devonian
			drawEpochBox("Lower", top,419.62,393.47, g2, new Color(0xE5AC4D));  // Devonian

			drawEpochBox("Priodoli", top,422.7,419.62, g2, new Color(0xE6F5E1));  // Silurian
			drawEpochBox("Ludlow", top,426.7,422.7, g2, new Color(0xBFE6CF));  // Silurian
			drawEpochBox("Wenlock", top,432.9,426.7, g2, new Color(0xB3E1C2));  // Silurian
			drawEpochBox("Llandovery", top,443.1,432.9, g2, new Color(0x99D7B3));  // Silurian

			drawEpochBox("Upper", top,458.2,443.1, g2, new Color(0x7FCA93));  // Ordovician
			drawEpochBox("Middle", top,471.3,458.2, g2, new Color(0x4DB47E));  // Ordovician
			drawEpochBox("Lower", top,486.85,471.3, g2, new Color(0x1A9D6F));  // Ordovician

			drawEpochBox("Furongian", top,497,486.85, g2, new Color(0xB3E095));  // Cambrian
			drawEpochBox("Miaolingian", top,506.5,497, g2, new Color(0xA6CF86));  // Cambrian
			drawEpochBox("Series2", top,521,506.5, g2, new Color(0x99C078));  // Cambrian
			drawEpochBox("Terreneuvian", top,538.8,521, g2, new Color(0x8CB06C));  // Cambrian

//Periods
			drawPeriodBox("", top,2.6,0, g2, new Color(0xf4ed84));
			drawPeriodBox("Neogene", top,23,2.6, g2, new Color(0xfbe751));
			drawPeriodBox("Paleogene", top,66,23, g2, new Color(0xef9f60));
			drawPeriodBox("Cretaceous", top,143.1, 66, g2, new Color(0x8fc45f));
			drawPeriodBox("Jurassic", top,201.4,143.1, g2, new Color(0x5cafc6));
			drawPeriodBox("Triassic", top,251.9,201.4, g2, new Color(0x77318d));
			drawPeriodBox("Permian", top,298.9,251.9, g2, new Color(0xdd4f37));
			drawPeriodBox("Carboniferous", top,358.86,298.9, g2, new Color(0x75a399));
			drawPeriodBox("Devonian", top,419.6,358.86, g2, new Color(0xCB8C37));
			drawPeriodBox("Silurian", top,443.1,419.6, g2, new Color(0xB3E1B6));
			drawPeriodBox("Ordovician", top,486.9,443.1, g2, new Color(0x009270));
			drawPeriodBox("Cambrian", top,538.8,486.9,g2, new Color(0x7FA056));
			drawPeriodBox("Ediacaran", top,635, 538.8,g2, new Color(0xFED96A));
			drawPeriodBox("Cryogenian", top,720, 635,g2, new Color(0xFECC5C));
			drawPeriodBox("Tonian", top,1000,720,g2, new Color(0xFEBF4E));
			drawPeriodBox("Stenian", top,1200,1000,g2, new Color(0xFED94E));
			drawPeriodBox("Ectasian", top,1400,1200,g2, new Color(0xFDCC8A));
			drawPeriodBox("Calymmian", top,1600,1400,g2, new Color(0xFDC07A));		
			drawPeriodBox("Statherian", top,1800,1600,g2, new Color(0xF875A7));
			drawPeriodBox("Orosirian", top,2050,1800,g2, new Color(0xF76898));
			drawPeriodBox("Rhyacian", top,2300,2050,g2, new Color(0xF75B89));
			drawPeriodBox("Siderian", top,2500,2300,g2, new Color(0xF74F7C));

		}
		g.setColor(t);

	}
	public int getPeriodFontSize() {
		return periodFontSize;
	}

	public void setPeriodFontSize(int periodFontSize) {
		this.periodFontSize = periodFontSize;
	}
	public int getEpochFontSize() {
		return epochFontSize;
	}

	public void setEpochFontSize(int epochFontSize) {
		this.epochFontSize = epochFontSize;
	}

	public boolean getShowPeriodNames() {
		return showPeriodNames;
	}

	public void setShowPeriodNames(boolean showPeriodNames) {
		this.showPeriodNames = showPeriodNames;
	}

	public boolean getShowEpochNames() {
		return showEpochNames;
	}

	public void setShowEpochNames(boolean showEpochNames) {
		this.showEpochNames = showEpochNames;
	}

	/*.................................................................................................................*/
	public   void drawGrayBars(Tree tree, int node, Graphics g) {

		if (drawGrayTimeBars) {
			Color t = g.getColor();

			double[] scale = treeDisplay.getScale();
			double totalScale = scale[endScaleValue] - scale[startScaleValue];
			int numBars = (int)(totalScale / timeBarInterval) + 1;
			double intervalPixels = (timeBarInterval*((scale[startX]-scale[endX])/totalScale));
			int height = (int)scale[endY];

			g.setColor(ColorDistribution.veryVeryLightGray);
			for (int iX = 1; iX<=numBars; iX++)
				if (iX % 2 == 1)
					g.fillRect((int)(scale[startX]-iX*intervalPixels)+1, 0, (int) intervalPixels, height+gapBetweenScaleAndGeologicalTimeScale);
			g.setColor(t);

			g.setColor(Color.gray);
			for (int iX = 0; iX<numBars; iX++) {
				double value = scale[startScaleValue]+(iX*timeBarInterval);
				String year = ""+value;
				if (yearsAsIntegers)
					year = "" + (int)value;
				double x = scale[startX]-iX*intervalPixels - GraphicsUtil.stringWidth(g, year)/2;
				g.drawString(year, (int)x, height+15);
			}
			g.setColor(t);
		}
	}

	/*.................................................................................................................*/
	public   void drawUnderTree(Tree tree, int node, Graphics g) {
		if (treeDisplay.getOrientation()!=TreeDisplay.RIGHT)
			return;
		reset();

		if (drawGrayTimeBars) {
			drawGrayBars(tree,node,g);
		}


	}
	
	
	//StringInABox orientationWarningBox;
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (treeDisplay.getOrientation()!=TreeDisplay.RIGHT){
			double[] scale = treeDisplay.getScale();
			int top = (int)scale[endY]+gapBetweenScaleAndGeologicalTimeScale;
			Color c = g.getColor();
			g.setColor(Color.blue);
			g.drawString("Chronogram Display can be used only when tree is in \"Right\" orientation, with root at left and tips at right.", treeDisplay.effectiveFieldLeftMargin(), top);
			/*if (orientationWarningBox== null)
				orientationWarningBox = new StringInABox("Chronogram Display can be used only when tree is in \"Right\" orientation, with root at left and tips at right.", g.getFont(), 100);
			orientationWarningBox.draw(g, treeDisplay.effectiveFieldLeftMargin(), top);*/
			g.setColor(c);
			return;
		}
		
		/*reset();

		if (drawGrayTimeBars) {
			drawGrayBars(tree,node,g);
		}
		 */
		if (drawGeologicalTimeScale) {
			double[] scale = treeDisplay.getScale();
			int top = (int)scale[endY]+gapBetweenScaleAndGeologicalTimeScale;
			drawEras(top,g);
		}


		Color t = g.getColor();
		g.setColor(Color.blue);
		drawNodes((MesquiteTree)tree, node, g);
		g.setColor(t);
	}


	/*.................................................................................................................*/
	void update(){
		reset();
		treeDisplay.pleaseUpdate(false);
	}
	void setOn(boolean a){
		treeDisplay.pleaseUpdate(false);
	}
	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		return  MesquiteDouble.toString(getLower((MesquiteTree)tree, node)) + " to " + MesquiteDouble.toString(getUpper((MesquiteTree)tree, node));
	}


	/*.................................................................................................................*/
	public   void setTree(Tree tree) {

	}
	
	public void turnOff() {
		treeDisplay.inhibitDefaultScaleBar = false;
		ownerModule.extras.removeElement(this);
		super.turnOff();
	}

	public void setDrawGeologicalTimeScale(boolean drawGeologicalTimeScale) {
		this.drawGeologicalTimeScale = drawGeologicalTimeScale;
	}

	public int getTimeBarInterval() {
		return timeBarInterval;
	}
	public void setTimeBarInterval(int timeBarInterval) {
		this.timeBarInterval = timeBarInterval;
	}

	public void setDrawGrayTimeBars(boolean drawGrayTimeBars) {
		this.drawGrayTimeBars = drawGrayTimeBars;
	}

	public void printUnderTree(Tree tree, int drawnRoot, Graphics g) {
		if (drawGrayTimeBars) {
			drawGrayBars(tree,drawnRoot,g);
		}

	}

	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		/*if (drawGrayTimeBars) {
			drawGrayBars(tree,drawnRoot,g);
		}
		 */
		double[] scale = treeDisplay.getScale();
		int top = (int)scale[endY]+gapBetweenScaleAndGeologicalTimeScale;
		drawEras(top,g);

		Color t = g.getColor();
		g.setColor(Color.blue);
		drawNodes((MesquiteTree)tree, drawnRoot, g);
		g.setColor(t);
	}
}






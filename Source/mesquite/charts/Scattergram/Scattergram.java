/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charts.Scattergram;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
/* A module drawing scattergrams (scatterplots).  Each data point represents an item, for instance a tree or character, and is plotted according to the item's
values on x and y axes.   Each data point may be colored according to a third value, implicitly yielding a third (z) axis.

This is designed to accommodate at least three uses:
(1) a standard scatterplot with each point represented explicitly
(2) a density diagram where the field is divided into blocks, and each block is grayscale shaded to indicate how many data points fall within it
(3) a protein visualization in which the x y axes represent the position of the residue in space and the color of the point some value for the residue.  This last
use is probably temporary until another module is tailor-made for the purpose.  

The labels "x" and "y" could refer either to the horizontal and vertical pixels of the chart's field, or to the values displayed in the chart (e.g.
treelength of 100, asymmetry of 0.83, etc.). To limit confusion xPixel and yPixel are used in some cases when the variables refer to the pixel values.
 */

public class Scattergram extends DrawChart {
	/*.................................................................................................................*/
	public String getName() {
		return "Scattergram";
	}
	public String getExplanation() {
		return "Helps make scattergram charts." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ScattergramAssistantA.class, getName() + " can optionally display extra information (such as regression lines).",
				"You can request such extra information under the Analysis submenu of the Chart menu of a Scattergram.");
	}
	/*.................................................................................................................*/
	Vector charters;
	public int spotSize = 10;
	public boolean useCircle = true;
	public MesquiteBoolean sizeToFit = new MesquiteBoolean(true);  //currently always true; is the graph in scrollpane (false) or not (true)
	public MesquiteBoolean joinTheDots = new MesquiteBoolean(false); //are points joined by line; used for special effects like protein visualization
	public MesquiteBoolean joinLastToFirst = new MesquiteBoolean(true);
	public MesquiteBoolean thickLine = new MesquiteBoolean(false);//lines between points are thick; used for special effects like protein visualization
	public MesquiteBoolean showDots = new MesquiteBoolean(true); //points can be turned off e.g. for density view or protein visualizaiton
	public MesquiteBoolean showLegend = new MesquiteBoolean(true); //legend for colors of points
	public MesquiteBoolean sumByBlocks = new MesquiteBoolean(false);//is density by blocks shown?
	int movingWindowSizeZ = 1;  //used in coloring of points, in particular for special effects like protein visualization
	Selectionable pointsAssociable = null;
	boolean areParts = false;
	Vector holding;
	boolean allowSequenceOptions;

	MesquiteCommand sizeDrawingCommand;
	MesquiteMenuItemSpec sizeItem;
	MesquiteSubmenuSpec selectSubmenu;
	MesquiteMenuItemSpec[] menuItems;
	public int totalFieldWidth= MesquiteInteger.unassigned;
	public int totalFieldHeight= MesquiteInteger.unassigned;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		charters = new Vector();
		holding = new Vector();
		/*
		REMOVED as not working well
		addCheckMenuItem(null, "Size To Window", MesquiteModule.makeCommand("toggleSizeToFit",  this), sizeToFit);
		 */
		addMenuItem("Marker size...", MesquiteModule.makeCommand("markerSize",  this));
		selectSubmenu = addSubmenu(null, "Select");
		addItemToSubmenu(null, selectSubmenu, "Deselect all", MesquiteModule.makeCommand("selectionOff",  this));
		menuItems= new MesquiteMenuItemSpec[11];
		MesquiteSubmenuSpec analysis = addSubmenu(null, "Auxiliary Analysis", makeCommand("newAssistant", this), ScattergramAssistantA.class);
		MesquiteSubmenuSpec mCloseAsst = addSubmenu(null, "Close Auxiliary Analysis");
		addMenuSeparator();
		mCloseAsst.setList(getEmployeeVector());
		mCloseAsst.setListableFilter(ScattergramAssistantA.class);
		mCloseAsst.setCommand(makeCommand("closeAssistant",  this));

		resetMenus();
		/*
		sizeDrawingCommand = MesquiteModule.makeCommand("sizeDrawing",  this);
		if (!sizeToFit.getValue())
			sizeItem = addMenuItem("Drawing Size...", sizeDrawingCommand);
		 */
		return true;
	}
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	void resetMenus(){
		for (int i=0; i<menuItems.length; i++) {
			if (menuItems[i]!=null)
				deleteMenuItem(menuItems[i]);
			menuItems[i]=null;
		}
		if (pointsAssociable!=null && allowSequenceOptions){
			menuItems[0] = addItemToSubmenu(null, selectSubmenu, "Select range...", MesquiteModule.makeCommand("selectRange",  this));
			menuItems[1] = addItemToSubmenu(null, selectSubmenu, "March selection forward", MesquiteModule.makeCommand("marchSelection",  this));
			menuItems[1].setShortcut(KeyEvent.VK_UP); //right
			menuItems[2] = addItemToSubmenu(null, selectSubmenu, "March selection backward", MesquiteModule.makeCommand("unmarchSelection",  this));
			menuItems[2].setShortcut(KeyEvent.VK_DOWN);
		}

		menuItems[3] = addSubmenu(null, "Special Effects");

		menuItems[9] = addCheckMenuItemToSubmenu(null, (MesquiteSubmenuSpec)menuItems[3], "Show Density", MesquiteModule.makeCommand("sumByBlocks",  this), sumByBlocks);

		if (pointsAssociable!=null && allowSequenceOptions){
			menuItems[4] = addCheckMenuItemToSubmenu(null, (MesquiteSubmenuSpec)menuItems[3], "Join the dots", MesquiteModule.makeCommand("toggleJoin",  this), joinTheDots);
			menuItems[5] = addCheckMenuItemToSubmenu(null, (MesquiteSubmenuSpec)menuItems[3], "Join last to first", null, joinLastToFirst);
			menuItems[6] = addCheckMenuItemToSubmenu(null, (MesquiteSubmenuSpec)menuItems[3], "Thick joints", null, thickLine);
		}

		menuItems[7] =  addCheckMenuItemToSubmenu(null, (MesquiteSubmenuSpec)menuItems[3], "Show dots", null, showDots);

		if (pointsAssociable!=null && allowSequenceOptions){
			if (joinTheDots.getValue()) {
				menuItems[5].setCommand(MesquiteModule.makeCommand("toggleLastToFirst",  this));
				menuItems[6].setCommand(MesquiteModule.makeCommand("toggleThickJoin",  this));
				menuItems[7].setCommand(MesquiteModule.makeCommand("toggleShowDots",  this));
			}
			else {
				menuItems[5].setCommand(null);
				menuItems[6].setCommand(null);
				if (!sumByBlocks.getValue())
					menuItems[7].setCommand(null);
				else
					menuItems[7].setCommand(MesquiteModule.makeCommand("toggleShowDots",  this));
			}
		}
		else {
			if (!sumByBlocks.getValue())
				menuItems[7].setCommand(null);
			else
				menuItems[7].setCommand(MesquiteModule.makeCommand("toggleShowDots",  this));
		}

		if (pointsAssociable!=null && allowSequenceOptions){
			if (menuItems[8]==null) {
				MesquiteMenuSpec mmisc = findMenuAmongEmployers("Colors");
				if (mmisc !=null)
					menuItems[8] = addMenuItem(mmisc, "Moving Window for Colors...", MesquiteModule.makeCommand("movingWindowSize",  this));
			}
			//if (menuItems[9]==null)
			//menuItems[9] = addMenuItem(findMenuAmongEmployers("Colors"), "Animated moving window", MesquiteModule.makeCommand("animateWindow",  this));
			if (menuItems[10]==null)
				menuItems[10] = addMenuSeparator();
		}
		else {
			if (joinTheDots.getValue()) {
				joinTheDots.setValue(false);
				setJoinTheDots();
			}
			if (!showDots.getValue() && !sumByBlocks.getValue()) {
				showDots.setValue(true);
				setShowDots();
			}

		}
	}

	public   Charter createCharter(ChartListener listener) {
		ScattergramCharter c = new ScattergramCharter(this, listener);
		charters.addElement(c);
		return c;
	}
	/* tells the module whether points are parts of an Associable object */
	public void pointsAreSelectable(boolean areParts, Selectionable a, boolean allowSequenceOptions){
		if (this.areParts!=areParts || pointsAssociable!=a){
			if (pointsAssociable!=null)
				pointsAssociable.removeListener(this);
			this.areParts = areParts;
			pointsAssociable =a;
			this.allowSequenceOptions = allowSequenceOptions;
			if (!areParts)
				pointsAssociable = null;
			if (pointsAssociable!=null)
				pointsAssociable.addListener(this);
			syncSel();
			resetMenus();
			resetContainingMenuBar();
		}
	}

	/* Synchronize selection of points in chart with parts of associable*/
	void syncSel(){
		Enumeration e = charters.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ScattergramCharter) {
				ScattergramCharter tCO = (ScattergramCharter)obj;
				tCO.synchronizeSelection();
				tCO.chart.calculateAllExtras();
				tCO.chart.getField().repaint();
			}
		}
	}
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if ((obj==pointsAssociable || obj instanceof Selectionable) && (code == MesquiteListener.SELECTION_CHANGED ||code == MesquiteListener.PARTS_MOVED ||  code == MesquiteListener.PARTS_DELETED || code == MesquiteListener.PARTS_ADDED)) {
			syncSel();
		}
	}
	public void endJob(){
		if (pointsAssociable!=null)
			pointsAssociable.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		/*
		temp.addLine("toggleSizeToFit " + sizeToFit.toOffOnString());
		if (!sizeToFit.getValue() && (MesquiteInteger.isCombinable(totalFieldWidth)||MesquiteInteger.isCombinable(totalFieldHeight))) {
			temp.addLine("sizeDrawing " + MesquiteInteger.toString(totalFieldWidth) + " " + MesquiteInteger.toString(totalFieldHeight));
		}
		 */
		temp.addLine("markerSize " + spotSize);

		temp.addLine("toggleJoin " + joinTheDots.toOffOnString());
		if (joinTheDots.getValue()) {
			temp.addLine("toggleThickJoin " + thickLine.toOffOnString());
			temp.addLine("toggleLastToFirst " + joinLastToFirst.toOffOnString());
		}
		temp.addLine("toggleShowDots " + showDots.toOffOnString());
		temp.addLine("movingWindowSize " + movingWindowSizeZ);

		temp.addLine("sumByBlocks " + sumByBlocks.toOffOnString());
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ScattergramAssistantA) {
				temp.addLine("\tnewAssistant " , ((MesquiteModule)e));
			}
		}
		return temp;
	}
	void addAssistantToCharter(ChartAssistant tda, ScattergramCharter tCO){

		if (tCO.chart!=null){
			ChartExtra tce = tda.createExtra(tCO.chart); //HOW TO REMOVE IF QUIT???
			tCO.chart.addExtra(tce);
			tce.doCalculations();
			tCO.chart.getField().repaint();
			tCO.chart.repaint();
		}
	}
	/*.................................................................................................................*/
	public void addAssistant(ChartAssistant tda) {
		Enumeration e = charters.elements();
		if (charters.size()==0) {
			holding.addElement(tda);
		}
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ScattergramCharter) {
				ScattergramCharter tCO = (ScattergramCharter)obj;
				addAssistantToCharter(tda, tCO);
			}
		}
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Turns off selection of points", null, commandName, "selectionOff")) {
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.selectionOff();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires a chart assistant module", "[name of assistant module]", commandName, "newAssistant")) {
			incrementMenuResetSuppression();
			ScattergramAssistantA tda= (ScattergramAssistantA)hireNamedEmployee(ScattergramAssistantA.class, arguments);
			if (tda!=null) {
				addAssistant(tda);
				if (!MesquiteThread.isScripting()) {
					Enumeration e = charters.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						if (obj instanceof ScattergramCharter) {
							ScattergramCharter tCO = (ScattergramCharter)obj;
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
				if (mb!=null && mb instanceof ScattergramAssistantA) {
					if (count== which) {
						fireEmployee(mb);
						Enumeration e = charters.elements();
						while (e.hasMoreElements()) {
							Object obj = e.nextElement();
							if (obj instanceof ScattergramCharter) {
								ScattergramCharter tCO = (ScattergramCharter)obj;
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
		else if  (checker.compare(MesquiteModule.class, null, null, commandName, "fireEmployee")) {
			String name = parser.getFirstToken(arguments);
			MesquiteModule mb = findEmployeeWithName(name);
			if (mb!=null) {
				fireEmployee(mb);
				if (!MesquiteThread.isScripting()) {
					Enumeration e = charters.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						if (obj instanceof ScattergramCharter) {
							ScattergramCharter tCO = (ScattergramCharter)obj;
							tCO.chart.munch();
						}
					}
					resetContainingMenuBar();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Marches the selected points ahead one (appropriate if the points are in a relevant sequence)", null, commandName, "marchSelection")) {
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.marchSelection(true);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Marches the selected points backward one (appropriate if the points are in a relevant sequence)", null, commandName, "unmarchSelection")) {
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.marchSelection(false);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Selects a range of points", "[first point] [last point]", commandName, "selectRange")) {
			pos.setValue(0);
			int w = MesquiteInteger.fromString(arguments, pos);
			int h = MesquiteInteger.fromString(arguments, pos);
			if ((MesquiteInteger.isCombinable(h)) || (MesquiteInteger.isCombinable(w))) {
				Enumeration e = charters.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ScattergramCharter) {
						ScattergramCharter tCO = (ScattergramCharter)obj;
						tCO.selectRange(w-1,h-1);  //ASSUMES all will be 0 based: //todo: query about whether item is zero based??
					}
				}
			}
			else { 
				MesquiteBoolean answer = new MesquiteBoolean(false);
				MesquiteInteger first = new MesquiteInteger(0);
				MesquiteInteger last =new MesquiteInteger(0);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Select range of points", "First",  "Last", answer,  first, last,MesquiteInteger.unassigned,MesquiteInteger.unassigned,MesquiteInteger.unassigned, MesquiteInteger.unassigned,MesquiteString.helpString);
				if (answer.getValue() && first.isCombinable() && last.isCombinable()) {
					Enumeration e = charters.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						if (obj instanceof ScattergramCharter) {
							ScattergramCharter tCO = (ScattergramCharter)obj;
							tCO.selectRange(first.getValue()-1,last.getValue()-1);   //TODO: need to convert to internal using clues from items sourcetask or associable (Associable should indicate if parts numbered extenrallay by 1
						}
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the size of the moving window for coloring the markers via a third value (appropriate if there is a natural sequence to the points", null, commandName, "movingWindowSize")) {
			pos.setValue(0);
			int w = MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteInteger.isCombinable(w))
				w = MesquiteInteger.queryInteger(containerOfModule(), "Moving window", "Size of moving window for sequential coloring of markers (to turn off moving window, enter '1').", movingWindowSizeZ);
			if (MesquiteInteger.isCombinable(w)) {
				movingWindowSizeZ = w;
				Enumeration e = charters.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ScattergramCharter) {
						ScattergramCharter tCO = (ScattergramCharter)obj;
						tCO.setMovingWindowSizeZ(movingWindowSizeZ);
					}
				}
			}
		}
		/*	else if (checker.compare(this.getClass(), nullxxx, null, commandName, "animateWindow")) {
  	    	 	//animated = !animated;
			//here start up a thread that alters moving window size from 1 though numPoints
    	 	}
		 */
		else if (checker.compare(this.getClass(), "Sets the use of circular spots as markers", null, commandName, "useCircle")) {
			useCircle = true;
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.setUseCircle(true);
				}
			}

		}
		else if (checker.compare(this.getClass(), "Sets the use of square markers", null, commandName, "useSquare")) {
			useCircle = false;
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.setUseCircle(false);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the size of markers", "[size in pixels]", commandName, "markerSize")) {
			pos.setValue(0);
			int w = MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteInteger.isCombinable(w))
				w = MesquiteInteger.queryInteger(containerOfModule(), "Size of markers", "Size of markers.", spotSize);
			if (MesquiteInteger.isCombinable(w)) {
				spotSize = w;
				Enumeration e = charters.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ScattergramCharter) {
						ScattergramCharter tCO = (ScattergramCharter)obj;
						tCO.setSpotSize(spotSize); 
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the chart is sized to fit within the field", "[on = constrained; off]", commandName, "toggleSizeToFit")) {
			sizeToFit.toggleValue(parser.getFirstToken(arguments));
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.setSizeToFit(sizeToFit.getValue());

				}
			}
			if (!sizeToFit.getValue())
				sizeItem = addMenuItem("Drawing Size...", sizeDrawingCommand);
			else if (sizeItem!=null)
				deleteMenuItem(sizeItem);
			if (!sizeToFit.getValue() && !MesquiteThread.isScripting() && !MesquiteInteger.isCombinable(totalFieldWidth)&& !MesquiteInteger.isCombinable(totalFieldHeight))
				doCommand("sizeDrawing", null, checker);
			resetContainingMenuBar();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the points are joined by lines (appropriate if there is a natural sequence to the points", "[on=joined; off]", commandName, "toggleJoin")) {
			joinTheDots.toggleValue(parser.getFirstToken(arguments));
			setJoinTheDots();
			resetMenus();
			resetContainingMenuBar();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the last point is joined to the first by a line (appropriate if there is a natural sequence to the points", "[on=joined; off]", commandName, "toggleLastToFirst")) {
			joinLastToFirst.toggleValue(parser.getFirstToken(arguments));
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.setJoinLast(joinLastToFirst.getValue());
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to shade to indicate number of points in each block", "[on=sum; off]", commandName, "sumByBlocks")) {
			sumByBlocks.toggleValue(parser.getFirstToken(arguments));
			resetMenus();
			resetContainingMenuBar();
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.setSumByBlocks(sumByBlocks.getValue());
					tCO.calculateChart(tCO.chart);
					tCO.chart.calculateAllExtras();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the lines joining the points are thick (appropriate if there is a natural sequence to the points", "[on=thick; off]", commandName, "toggleThickJoin")) {
			thickLine.toggleValue(parser.getFirstToken(arguments));
			Enumeration e = charters.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ScattergramCharter) {
					ScattergramCharter tCO = (ScattergramCharter)obj;
					tCO.setThickJoin(thickLine.getValue());
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the circular or square markers are shown at the points", "[on=show; off]", commandName, "toggleShowDots")) {
			showDots.toggleValue(parser.getFirstToken(arguments));
			setShowDots();
		}
		else if (checker.compare(this.getClass(), "Sets the size of the chart drawing (appropriate if it is not constrained to fit in the field", "[width in pixels][height in pixels]", commandName, "sizeDrawing")) {
			pos.setValue(0);
			int w = MesquiteInteger.fromString(arguments, pos);
			int h = MesquiteInteger.fromString(arguments, pos);
			if ((MesquiteInteger.isCombinable(h) &&  h>10) || (MesquiteInteger.isCombinable(w) && w>10)) {
				Enumeration e = charters.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof ScattergramCharter) {
						ScattergramCharter tCO = (ScattergramCharter)obj;
						tCO.setTotalField(w,h);
					}
				}
				totalFieldHeight = h;
				totalFieldWidth = w;

			}
			else { 
				MesquiteBoolean answer = new MesquiteBoolean(false);
				MesquiteInteger newWidth = new MesquiteInteger(totalFieldWidth);
				MesquiteInteger newHeight =new MesquiteInteger(totalFieldHeight);
				MesquiteInteger.queryTwoIntegers(containerOfModule(), "Size of scattergram chart", "Width (Pixels)",  "Height (Pixels)", answer,  newWidth, newHeight,10,MesquiteInteger.unassigned,10, MesquiteInteger.unassigned,MesquiteString.helpString);
				if (answer.getValue() &&( (newWidth.isCombinable()&&newWidth.getValue()>10) || (newHeight.isCombinable() && newHeight.getValue()>10))) {
					Enumeration e = charters.elements();
					while (e.hasMoreElements()) {
						Object obj = e.nextElement();
						if (obj instanceof ScattergramCharter) {
							ScattergramCharter tCO = (ScattergramCharter)obj;
							tCO.setTotalField(newWidth.getValue(),newHeight.getValue());
						}
					}
					totalFieldHeight = newHeight.getValue();
					totalFieldWidth = newWidth.getValue();
				}
			}
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	void setJoinTheDots(){
		Enumeration e = charters.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ScattergramCharter) {
				ScattergramCharter tCO = (ScattergramCharter)obj;
				tCO.setJoin(joinTheDots.getValue());
			}
		}
	}
	void setShowDots(){
		Enumeration e = charters.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ScattergramCharter) {
				ScattergramCharter tCO = (ScattergramCharter)obj;
				tCO.setShowDots(showDots.getValue());
			}
		}
	}
}

/* ======================================================================== */
class ScattergramCharter extends Charter {
	MesquiteNumber valueX;
	MesquiteNumber valueY;
	MesquiteNumber valueZ;
	MesquiteNumber tempNum = new MesquiteNumber();
	int spotSize = 10;
	boolean useCircle = true;
	boolean showNames=false;
	MesquiteChart chart;
	Scattergram ownerModule;
	DragRectangle dragRectangle;
	boolean showSpots = true;
	boolean joinTheDots = false;
	boolean joinLastToFirst = true;
	boolean sumByBlocks = false;
	boolean thickJoin = false;
	int movingWindowSizeZ = 1;
	ChartListener listener;
	int minTicks = 3;
	ScattergramColorLegend legend;
	MesquiteColorTable colorTable = new ContColorTable();
	int[][] blockSums;
	int numBlockDivisions = 10;
	public ScattergramCharter (Scattergram ownerModule, ChartListener listener) {
		this.ownerModule = ownerModule;
		this.listener = listener;
		spotSize = ownerModule.spotSize;
		useCircle = ownerModule.useCircle;
		valueX = new MesquiteNumber();
		valueY = new MesquiteNumber();
		valueZ = new MesquiteNumber();
		joinTheDots = ownerModule.joinTheDots.getValue();
		joinLastToFirst = ownerModule.joinLastToFirst.getValue();
		setSumByBlocks(ownerModule.sumByBlocks.getValue());
	}
	public void open(MesquiteChart chart){
		this.chart = chart;
		chart.setXAxisEdge(98); //MAKE IT DEPEND ON FONT SIZE
		chart.setYAxisEdge(88); //MAKE IT DEPEND ON FONT SIZE
		chart.setSizeToFit(ownerModule.sizeToFit.getValue());
		ownerModule.incrementMenuResetSuppression();

		for (int i = 0; i<ownerModule.holding.size(); i++)
			addAssistant((ChartAssistant)(ownerModule.holding.elementAt(i)));
		ownerModule.decrementMenuResetSuppression();
		synchronizeSelection();
	}
	public  void calculateChart(MesquiteChart chart){
		if (chart == null)
			return;
		minX.setValue(chart.getAxisMinimumX());
		maxX.setValue(chart.getAxisMaximumX());
		minY.setValue(chart.getAxisMinimumY());
		maxY.setValue(chart.getAxisMaximumY());
		if (sumByBlocks){
			double minimumX = chart.getAxisMinimumX().getDoubleValue();
			double maximumX = chart.getAxisMaximumX().getDoubleValue();
			double minimumY = chart.getAxisMinimumY().getDoubleValue();
			double maximumY = chart.getAxisMaximumY().getDoubleValue();
			Integer2DArray.zeroArray(blockSums);

			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (getSuspendChartCalculations())
					return;
				if (i%100 == 0)
					CommandRecord.tick("Sorting points into categories for density (point " + i + ")");
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					chart.getXArray().placeValue(i, valueX);
					chart.getYArray().placeValue(i, valueY);
					double x = valueX.getDoubleValue();
					double y = valueY.getDoubleValue();
					//try {
					blockSums[category(x, minimumX, maximumX, numBlockDivisions)][category(y, minimumY, maximumY, numBlockDivisions)]++;
					//	}
					//	catch (ArrayIndexOutOfBoundsException e){
					//		MesquiteMessage.warnProgrammer(
					//	}
				}
			}
		}
	}
	void addAssistant(ChartAssistant tda){
		if (chart!=null){
			ChartExtra tce = tda.createExtra(chart); 
			chart.addExtra(tce);
			tce.doCalculations();
			repaintC();
		}
	}
	public void setShowNames(boolean showNames) {
		this.showNames = showNames;
	}
	/*---- Methods concerning selection of points ----*/
	void synchronizeSelection() {
		if (chart!=null && ownerModule.pointsAssociable!=null) {
			chart.synchronizePointSelection(ownerModule.pointsAssociable);
		}
	}
	public void selectionOff() {
		if (chart!=null) {
			chart.deselectAllPoints();
			chart.getField().repaint();
			chart.repaint();
		}
	}
	void deselectAll(){
		if (ownerModule.pointsAssociable!=null)  
			ownerModule.pointsAssociable.deselectAll();
		else
			chart.deselectAllPoints();
	}
	void select(int which){
		if (ownerModule.pointsAssociable!=null)   {
			ownerModule.pointsAssociable.setSelected(which, true);
		}
		else
			chart.selectPoint(which);
	}
	void deselect(int which){
		if (ownerModule.pointsAssociable!=null)  
			ownerModule.pointsAssociable.setSelected(which, false);
		else
			chart.deselectPoint(which);
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
	void wrapUpSelection(){
		if (ownerModule.pointsAssociable!=null) {
			ownerModule.pointsAssociable.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

		}
		else {
			repaintC();
		}
	}
	public void selectRange(int first, int last) { 
		if (chart!=null) {
			deselectAll();
			for (int i=first; i<=last; i++) 
				select(i);
			wrapUpSelection();
		}
	}
	public void marchSelection(boolean forward) {
		Graphics g = chart.getField().getGraphics();
		if (chart!=null) {
			if (!forward){
				int num  = chart.getNumPoints();
				for (int i=0; i<num; i++) {
					int next;
					if (i==num-1)
						next =0;
					else
						next  = (i+1);
					if (isSelected(next) != isSelected(i)){
						if (isSelected(next))
							select(i);
						else
							deselect(i);
						if (g!=null) redrawPoint(g, chart, i);
					}
				}
			}
			else {
				int num  = chart.getNumPoints();
				for (int i=num-1; i>=0; i--) {
					int next;
					if (i==0)
						next =num-1;
					else
						next  = (i-1);
					if (isSelected(next) != isSelected(i)){ 
						if (isSelected(next))
							select(i);
						else
							deselect(i);
						if (g!=null) redrawPoint(g, chart, i);
					}
				}
			}
			wrapUpSelection();
		}
		if (g!=null)
			g.dispose();
	}
	/*------------viewing options ------------*/
	public void setUseCircle(boolean useCircle) {
		this.useCircle = useCircle;
		repaintC();
	}
	public void setSpotSize(int s) {
		this.spotSize = s;
		repaintC();
	}
	public void setSizeToFit(boolean b) {
		chart.setSizeToFit(b);
		if (!b){
			chart.setTotalField(ownerModule.totalFieldWidth, ownerModule.totalFieldHeight);
		}
	}
	public void setJoin(boolean b) {
		joinTheDots = b;
		joinLastToFirst = ownerModule.joinLastToFirst.getValue();
		showSpots = ownerModule.showDots.getValue();
		thickJoin = ownerModule.thickLine.getValue();
		repaintC();
	}
	public void setJoinLast(boolean b) {
		joinLastToFirst = b;
		repaintC();
	}
	public void setThickJoin(boolean b) {
		thickJoin = b;
		repaintC();
	}
	public void setSumByBlocks(boolean b) {
		sumByBlocks = b;
		if (b && blockSums==null){
			blockSums = new int[numBlockDivisions][numBlockDivisions];
		}
		repaintC();
	}
	public void setShowDots(boolean b) {
		showSpots = b;
		repaintC();
	}

	public void setTotalField(int w, int h) {
		chart.setTotalField(w,h);
		repaintC();
	}
	/* ----------------------------------*/
	public void setMovingWindowSizeZ(int s) {
		this.movingWindowSizeZ = s;
		repaintC();
	}
	/* ----------------------------------*/
	private void repaintC(){
		if (chart!=null && chart.getField()!=null) {
			chart.getField().repaint();
			chart.repaint();
		}
	}

	/* ----------------------------------*/
	private int findExactPoint(int xPixel, int yPixel, MesquiteChart chart){   // todo:   need to fill this in.  gives value to screen resolution
		if (xPixel<0)
			xPixel =0;
		return xPixel;
	}
	/* ----------------------------------*/
	/* Returns which of the data points is at the pixel point indicated; included are points within a half markerwidth of the pixel.  If more than one data point qualifies, the next one
	in sequence is given (i.e. repeated clicks cycle through all at that location)*/
	long numClicks = 0;
	private int findPoint(int xPixel, int yPixel, MesquiteChart chart){
		if (xPixel<0)
			xPixel =0;
		if (yPixel<0)
			yPixel = 0;

		//first, find target area in graph units, converted from pixels 
		int halfSpot = spotSize/2;
		if (halfSpot <1)
			halfSpot = 1;
		else if (spotSize/2*2 != spotSize)
			halfSpot++;

		MesquiteNumber xMin = new MesquiteNumber();
		MesquiteNumber xMax = new MesquiteNumber();
		MesquiteNumber yMin = new MesquiteNumber();
		MesquiteNumber yMax = new MesquiteNumber();

		pixelToX(xPixel-halfSpot, chart, xMin);
		pixelToX(xPixel+halfSpot, chart, xMax);
		pixelToY(yPixel+halfSpot, chart, yMin);
		pixelToY(yPixel-halfSpot, chart, yMax);

		NumberArray xArray = chart.getXArray();
		NumberArray yArray = chart.getYArray();
		long numFound=0;
		int found = -1;
		for (int i=0; i<chart.getNumPoints(); i++){
			xArray.placeValue(i, tempNum);
			if (tempNum!=null && tempNum.isMoreThan(xMin) && tempNum.isLessThan(xMax)) {
				yArray.placeValue(i, tempNum);
				if (tempNum!=null && tempNum.isMoreThan(yMin) && tempNum.isLessThan(yMax)) {
					numFound++;
					found = i;
				}
			}
		}
		if (numFound==1)
			return found;
		else if (numFound>0) {
			long target = numClicks % numFound;
			for (int i=0; i<chart.getNumPoints(); i++){
				xArray.placeValue(i, tempNum);
				if (tempNum!=null && tempNum.isMoreThan(xMin) && tempNum.isLessThan(xMax)) {
					yArray.placeValue(i, tempNum);
					if (tempNum!=null && tempNum.isMoreThan(yMin) && tempNum.isLessThan(yMax)) {
						if (target<=0)
							return i;
						target--;
					}
				}
			}
		}
		return -1;
	}
	/* ----------------------------------*/
	/** returns an array of all data points within the pixel rectangle indicated*/
	private int[] findPointsWithin(int x1, int y1, int x2, int y2, MesquiteChart chart){
		if (MesquiteInteger.isCombinable(x1) && x1<0)
			x1 =0;
		if (MesquiteInteger.isCombinable(y1) && y1<0)
			y1 = 0;
		if (MesquiteInteger.isCombinable(x2) && x2<0)
			x2 =0;
		if (MesquiteInteger.isCombinable(y2) && y2<0)
			y2 = 0;
		if (MesquiteInteger.isCombinable(x1) && x1> chart.getField().getBounds().width)
			x1 =chart.getField().getBounds().width;
		if (MesquiteInteger.isCombinable(x2) && x2> chart.getField().getBounds().width)
			x2 =chart.getField().getBounds().width;
		if (MesquiteInteger.isCombinable(y1) && y1>chart.getField().getBounds().height)
			y1 = chart.getField().getBounds().height;
		if (MesquiteInteger.isCombinable(y2) && y2>chart.getField().getBounds().height)
			y2 = chart.getField().getBounds().height;
		if (!MesquiteInteger.isCombinable(x1) ||!MesquiteInteger.isCombinable(y1)) {
			int which = findPoint(x2, y2, chart);
			if (which>=0)
				return new int[]{which};
			return null;
		}
		else if (!MesquiteInteger.isCombinable(x2) ||!MesquiteInteger.isCombinable(y2)) {
			int which = findPoint(x1, y1, chart);
			if (which>=0)
				return new int[]{which};
			return null;
		}

		MesquiteNumber xMin= new MesquiteNumber();
		MesquiteNumber xMax= new MesquiteNumber();
		xMin.findWithinBounds(chart.getAxisMinimumX(), chart.getAxisMaximumX(), x1, chart.getFieldWidth());
		xMax.findWithinBounds(chart.getAxisMinimumX(), chart.getAxisMaximumX(), x2, chart.getFieldWidth());

		MesquiteNumber yMin= new MesquiteNumber();
		MesquiteNumber yMax= new MesquiteNumber();
		yMin.findWithinBounds(chart.getAxisMinimumY(), chart.getAxisMaximumY(), chart.getFieldHeight()-y1, chart.getFieldHeight());
		yMax.findWithinBounds(chart.getAxisMinimumY(), chart.getAxisMaximumY(), chart.getFieldHeight()-y2, chart.getFieldHeight());
		NumberArray xArray = chart.getXArray();
		NumberArray yArray = chart.getYArray();
		try{
			int numFound=0;
			for (int i=0; i<chart.getNumPoints(); i++){
				xArray.placeValue(i, tempNum);
				if (tempNum!=null && ((tempNum.isMoreThan(xMin) && tempNum.isLessThan(xMax))|| (tempNum.isLessThan(xMin) && tempNum.isMoreThan(xMax)))) {
					yArray.placeValue(i, tempNum);
					if (tempNum!=null && ((tempNum.isMoreThan(yMin) && tempNum.isLessThan(yMax)) || (tempNum.isLessThan(yMin) && tempNum.isMoreThan(yMax)))) {
						numFound++;
					}
				}
			}

			int[] found = new int[numFound];
			if (numFound>0) {
				int count =0;
				for (int i=0; i<chart.getNumPoints(); i++){
					xArray.placeValue(i, tempNum);
					if (tempNum!=null && ((tempNum.isMoreThan(xMin) && tempNum.isLessThan(xMax))|| (tempNum.isLessThan(xMin) && tempNum.isMoreThan(xMax)))) {
						yArray.placeValue(i, tempNum);
						if (tempNum!=null && ((tempNum.isMoreThan(yMin) && tempNum.isLessThan(yMax)) || (tempNum.isLessThan(yMin) && tempNum.isMoreThan(yMax)))) {
							found[count] = i;
							count++;
						}
					}
				}
			}
			return found;
		}
		catch (Throwable t) {
		}
		return null;
	}
	/* ----------------------------------*/
	void drawRect(Graphics g, int x1, int y1, int x2, int y2){
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
	/* ----------------------------------*/
	int pointEntered = -1;
	public void mouseMoveInField(int modifiers, int xPixel, int yPixel, MesquiteChart chart, ChartTool tool) {
		whichDown = findPoint(xPixel,yPixel, chart);
		if (pointEntered >=0 && whichDown!=pointEntered) {  //had been in other point recently; exit it
			ListableVector extras = chart.getExtras();
			for (int i=0; i<extras.size(); i++) {
				((ChartExtra)extras.elementAt(i)).cursorExitPoint(pointEntered,  findExactPoint(xPixel,yPixel, chart),  null);
			}
		}
		if (whichDown>=0) {
			if (whichDown!=pointEntered){
				ListableVector extras = chart.getExtras();
				for (int i=0; i<extras.size(); i++) {
					((ChartExtra)extras.elementAt(i)).cursorEnterPoint(whichDown, findExactPoint(xPixel,yPixel, chart), null);
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
	public void mouseDownInField(int modifiers, int xPixel, int yPixel, MesquiteChart chart, ChartTool tool) {
		numClicks++;
		whichDown = findPoint(xPixel,yPixel, chart);
		if (tool != chart.getArrowTool() && tool != chart.getInfoTool()) {
			if (whichDown>=0){
				tool.pointTouched(whichDown, xPixel, yPixel, modifiers);
				return;
			}
		}	

		//arrow tool; start selection rectangle
		if (tool == chart.getArrowTool()){
			xDown = xPixel;
			yDown = yPixel;
			dragRectangle = new DragRectangle(chart.getField().getGraphics(),xPixel, yPixel);
			ListableVector extras = chart.getExtras();
			for (int i=0; i<extras.size(); i++) {
				((ChartExtra)extras.elementAt(i)).cursorTouchPoint(whichDown, findExactPoint(xPixel,yPixel, chart), null);
			}
			return;
		}

		//info tool
		if (listener!=null && (sumByBlocks || whichDown >=0)) {
			String message = null;
			if (sumByBlocks){
				double minimumX = chart.getAxisMinimumX().getDoubleValue();
				double maximumX = chart.getAxisMaximumX().getDoubleValue();
				double minimumY = chart.getAxisMinimumY().getDoubleValue();
				double maximumY = chart.getAxisMaximumY().getDoubleValue();
				double incrementX = (maximumX-minimumX)/numBlockDivisions;
				double incrementY = (maximumY-minimumY)/numBlockDivisions;
				MesquiteNumber xPoint = new MesquiteNumber();
				MesquiteNumber yPoint = new MesquiteNumber();
				pixelToX(xPixel, chart, xPoint);
				pixelToY(yPixel, chart, yPoint);
				int xBlock = category(xPoint.getDoubleValue(), minimumX, maximumX, numBlockDivisions);
				int yBlock = category(yPoint.getDoubleValue(), minimumY, maximumY, numBlockDivisions);
				if (xBlock>=0 && xBlock < blockSums.length && yBlock>=0 && yBlock < blockSums[xBlock].length) {
					message = "Density: " + blockSums[xBlock][yBlock] + "\npoints in range\nx: " + MesquiteDouble.toStringDigitsSpecified(minimumX + xBlock*incrementX, 5) + " - " +  MesquiteDouble.toStringDigitsSpecified(minimumX + (xBlock+1)*incrementX, 5) + "\ny: " + MesquiteDouble.toStringDigitsSpecified(minimumY + yBlock*incrementY, 5) + " - " +  MesquiteDouble.toStringDigitsSpecified(minimumY + (yBlock+1)*incrementY, 5);
				}
			}
			int wd = whichDown;
			if (!showSpots)
				wd = -1;
			listener.pointMouseDown(chart, wd, null, null,  xPixel, yPixel, modifiers, message); //todo: send message here in last null
		}
		ListableVector extras = chart.getExtras();
		for (int i=0; i<extras.size(); i++) {
			((ChartExtra)extras.elementAt(i)).cursorTouchPoint(whichDown, findExactPoint(xPixel,yPixel, chart), null);
		}

	}
	/* ----------------------------------*/
	public void mouseDragInField(int modifiers, int xPixel, int yPixel, MesquiteChart chart, ChartTool tool) {
		if (tool == chart.getArrowTool()){ 
			if (dragRectangle!=null)
				dragRectangle.drawRectangleDrag(xPixel, yPixel);
		}
	}
	/* ----------------------------------*/
	public void mouseUpInField(int modifiers, int xPixel, int yPixel, MesquiteChart chart, ChartTool tool) {
		// neither arrow nor info tool
		if (tool !=null && tool != chart.getArrowTool()&& tool != chart.getInfoTool()) {
			if (whichDown>=0){
				tool.pointDropped(whichDown, xPixel, yPixel,modifiers);
				return;
			}
		}	
		int which = findPoint(xPixel, yPixel, chart);
		if (listener!=null)
			listener.pointMouseUp(chart, which, xPixel, yPixel, modifiers, null);

		//arrow tool; handle selection
		if (tool == chart.getArrowTool()){
			if (dragRectangle!=null)
				dragRectangle.drawRectangleUpDown();
			if (which == whichDown && MesquiteInteger.isNonNegative(which)) { // had touched directly on point before
				if (MesquiteEvent.shiftKeyDown(modifiers)) {
					if (isSelected(which))
						deselect(which);
					else
						select(which);
				}
				else if (!isSelected(which)) {
					if (anySelected())
						deselectAll();
					select(which);
				}
				wrapUpSelection();
			}
			else {
				int[] whichPoints = findPointsWithin(xPixel,yPixel,xDown, yDown, chart);
				if (whichPoints!=null && whichPoints.length>0) {
					if (!MesquiteEvent.shiftKeyDown(modifiers))
						deselectAll();
					for (int i = 0; i< whichPoints.length; i++){
						which = whichPoints[i];
						if (MesquiteEvent.shiftKeyDown(modifiers)) {
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
		xDown = MesquiteInteger.unassigned;
		yDown = MesquiteInteger.unassigned;
		whichDown =MesquiteInteger.unassigned;
	}
	/* ----------------------------------*/
	public void showQuickMessage(MesquiteChart chart, int whichPoint,int xO, int yO,  String message){
		if (StringUtil.blank(message))
			return;
		int xPixel = xO;
		int yPixel = yO;
		if (whichPoint>=0){
			chart.getXArray().placeValue(whichPoint, valueX);
			chart.getYArray().placeValue(whichPoint, valueY);
			markerWidth = spotSize;
			int markerHeight = spotSize;
			int fieldWidth = chart.getFieldWidth(); 
			int fieldHeight = chart.getFieldHeight();
			xPixel = chart.getMargin()+valueX.setWithinBounds(chart.getAxisMinimumX(), chart.getAxisMaximumX(), fieldWidth - 2*chart.getMargin())-markerWidth/2;
			yPixel = fieldHeight-chart.getMargin()-valueY.setWithinBounds(chart.getAxisMinimumY(), chart.getAxisMaximumY(), fieldHeight - 2*chart.getMargin())-markerHeight/2;
		}
		Graphics g = chart.getField().getGraphics();
		if (g!=null) {
			int w = 90;
			StringInABox sb = new StringInABox(message, chart.getField().getFont(), w);
			int xPos=0;
			if (xPixel+w + 32>chart.getField().getBounds().width)
				xPos = xPixel-w;
			else
				xPos = xPixel+32;
			int yPos=0;
			if (yPixel+sb.getHeight()>chart.getField().getBounds().height)
				yPos = yPixel-sb.getHeight();
			else
				yPos = yPixel;

			sb.drawInBox(g, chart.getField().getBackground(), xPos, yPos);
			g.dispose();
		}
	}
	/* ----------------------------------*/
	public void hideQuickMessage(MesquiteChart chart){
		chart.getField().repaint();
	}
	/* ----------------------------------*/
	/* returns color of ith data point */
	Color getPointColor(MesquiteChart chart, int i){
		if (chart.getZArray()!=null) {
			if (movingWindowSizeZ<=1 || !MesquiteInteger.isCombinable(movingWindowSizeZ))
				chart.getZArray().placeValue(i, valueZ);
			else {
				int first = 0;
				int last = 0;
				if (i-(movingWindowSizeZ/2)<0) {
					first = 0;
					last = i + (movingWindowSizeZ/2);
				}
				else {
					first = i-(movingWindowSizeZ/2);
					last = first + (movingWindowSizeZ);
					if (last>= chart.getNumPoints())
						last = chart.getNumPoints()-1;
				}
				valueZ.setToUnassigned();
				int count =0;
				for (int j = first; j<=last; j++) {
					chart.getZArray().placeValue(j, tempNum);
					if (tempNum.isCombinable()) {
						count++;
						valueZ.add(tempNum);
					}
				}
				if (count==0)
					valueZ.setToUnassigned(); 
				else
					valueZ.divideBy(count);
			}
			Color c= (colorTable.getColor(valueZ.getDoubleValue(), chart.getMinimumZ().getDoubleValue(), chart.getMaximumZ().getDoubleValue()));
			if (!chart.getSelected().anyBitsOn()|| chart.getSelected().isBitOn(i))
				return (c);
			else
				return (ColorDistribution.brighter(c, ColorDistribution.dimmingConstant));

		}
		else if (chart.colorsExist() && chart.getColor(i)!=null){
			Color c = chart.getColor(i);
			if (!chart.getSelected().anyBitsOn()|| chart.getSelected().isBitOn(i))
				return (c);
			else
				return (ColorDistribution.brighter(c, ColorDistribution.dimmingConstant));
		}
		else {
			//the following is not used as categories are not assigned anywhere
			LongArray cats = chart.getCatArray();
			if (cats==null || cats.getValue(i)==0){ //use some other coloring scheme
				if (!chart.getSelected().anyBitsOn()|| chart.getSelected().isBitOn(i))
					return (Color.blue);
				else
					return (ColorDistribution.lightBlue);
			}
			else{
				if (!chart.getSelected().anyBitsOn()|| chart.getSelected().isBitOn(i))
					return (Color.red);
				else
					return (Color.pink);
			}
		}
	}
	// returns outline color of ith data point
	Color getPointFrameColor(MesquiteChart chart, int i){
		if (!chart.getSelected().anyBitsOn() || chart.getSelected().isBitOn(i))
			return (Color.black);
		else
			return (Color.gray);

	}
	// redraws data point i.  Used currently only for march selection (to avoid entire redraw)
	void redrawPoint(Graphics g, MesquiteChart chart, int i){
		int margin = chart.getMargin();
		markerWidth = spotSize;
		int markerHeight = spotSize;
		int fieldWidth = chart.getField().getBounds().width;
		int fieldHeight = chart.getField().getBounds().height;
		RotatedRectangle rot = null;
		if (thickJoin)
			rot = new RotatedRectangle();
		int previous;
		if (i==0)
			previous = chart.getNumPoints()-1;
		else
			previous = i-1;

		if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
			chart.getXArray().placeValue(i, valueX);
			chart.getYArray().placeValue(i, valueY);
			int xPixel = xToPixel(valueX.getDoubleValue(), chart); //margin+valueX.setWithinBounds(chart.getAxisMinimumX(), chart.getAxisMaximumX(), coreWidth)-markerWidth/2;
			int yPixel = yToPixel(valueY.getDoubleValue(), chart); //fieldHeight-margin-valueY.setWithinBounds(chart.getAxisMinimumY(), chart.getAxisMaximumY(), coreHeight)-markerHeight/2;
			if (joinTheDots && (previous<i || joinLastToFirst)){
				chart.getXArray().placeValue(previous, valueX);
				chart.getYArray().placeValue(previous, valueY);
				int prevX = xToPixel(valueX.getDoubleValue(), chart); //margin+valueX.setWithinBounds(chart.getAxisMinimumX(), chart.getAxisMaximumX(), coreWidth)-markerWidth/2;
				int prevY = yToPixel(valueY.getDoubleValue(), chart); //fieldHeight-margin-valueY.setWithinBounds(chart.getAxisMinimumY(), chart.getAxisMaximumY(), coreHeight)-markerHeight/2;

				if (valueX.isCombinable() && valueY.isCombinable()){
					Color c =getPointColor(chart, i);
					if (thickJoin) {
						if (c!=null) g.setColor(c);
						rot.setShape(xPixel, yPixel, prevX, prevY, markerWidth, false, RotatedRectangle.RECTANGLE);
						rot.fill(g, false);
						g.setColor(getPointFrameColor(chart, i));
						rot.draw(g);
					}
					else {
						g.setColor(getPointFrameColor(chart, i));
						g.drawLine(xPixel, yPixel, prevX, prevY);
					}
				}
			}
			if (showSpots){
				g.setColor(getPointColor(chart, i));
				if (useCircle){
					g.fillOval(xPixel - markerWidth/2, yPixel - markerWidth/2, markerWidth, markerHeight);
					g.setColor(getPointFrameColor(chart, i));
					g.drawOval(xPixel - markerWidth/2, yPixel - markerWidth/2, markerWidth, markerHeight);
				}
				else {
					g.fillRect(xPixel - markerWidth/2, yPixel - markerWidth/2, markerWidth+1, markerHeight+1);
				}
			}
		}
	}

	//returns the category for density shading of a data point of value x within the range shown
	private int category(double x, double min, double max, int numDiv){
		int c = (int)((x-min)/(max-min)*numDiv);
		if (c == numDiv)
			c--;
		return c;
	}

	//Shades by density of data points
	private void shadeBlocks(Graphics g, MesquiteChart chart){
		double minimumX = chart.getAxisMinimumX().getDoubleValue();
		double maximumX = chart.getAxisMaximumX().getDoubleValue();
		double minimumY = chart.getAxisMinimumY().getDoubleValue();
		double maximumY = chart.getAxisMaximumY().getDoubleValue();
		int min = Integer2DArray.minimum(blockSums);
		int max = Integer2DArray.maximum(blockSums);
		double incrementX = (maximumX-minimumX)/numBlockDivisions;
		double incrementY = (maximumY-minimumY)/numBlockDivisions;
		double xLeft = minimumX;
		for (int i= 0; i<numBlockDivisions; i++) {
			double yTop = minimumY;
			int blockLeft = xToPixel(xLeft, chart);
			int blockWidth = xToPixel(xLeft+incrementX, chart) - blockLeft;
			for (int j= 0; j<numBlockDivisions; j++) {
				int blockTop = yToPixel(yTop, chart);
				int blockHeight = blockTop - yToPixel(yTop+incrementY, chart);
				g.setColor(MesquiteColorTable.getGrayScale((double)blockSums[i][j], (double)min, (double)max));
				g.fillRect(blockLeft, blockTop, blockWidth, blockHeight);
				yTop += incrementY;
			}
			xLeft += incrementX;
		}
	}
	/* ----------------------------------*/
	public void drawBackground(Graphics g, MesquiteChart chart){
		if (sumByBlocks)
			shadeBlocks(g, chart);
	}
	/* ----------------------------------*/
	public void drawChartBackground(Graphics g, MesquiteChart chart){
		g.setColor(Color.black);			
		drawGrid(g, chart);
	}
	/* ----------------------------------*/
	public void drawChart(Graphics g, MesquiteChart chart){
		if (chart==null || chart.getXArray()==null || chart.getYArray()==null )
			return;
		if (getSuspendDrawing())
			return;
		if (chart.getNumPoints()==0){
			g.setColor(Color.black);
			g.drawString("The chart does not yet have any points", 10, 70);
			return;
		}
		chartDone = true;
		//g.setColor(Color.white);
		int margin = chart.getMargin();
		markerWidth = spotSize;
		int markerHeight = spotSize;
		int fieldWidth = chart.getField().getBounds().width;
		int fieldHeight = chart.getField().getBounds().height;
		//g.fillRect(margin, margin, fieldWidth-2*margin, fieldHeight-2*margin);
		g.setColor(Color.black);
		g.drawRect(margin, margin, fieldWidth-2*margin, fieldHeight-2*margin);

		synchronizeSelection();

		if (chart.getZArray()!=null) {
			if (legend==null) {
				legend = new ScattergramColorLegend(ownerModule, this);
				chart.getField().add(legend);
			}
			if (!legend.isVisible())
				legend.setVisible(true);
			String colorExplanation = "Colors: " + chart.getZAxisName();
			if (movingWindowSizeZ>1 && MesquiteInteger.isCombinable(movingWindowSizeZ))
				colorExplanation += " (averaged in moving window of size " + movingWindowSizeZ + ")";
			legend.specsBox.setText(colorExplanation);
			legend.setMinMax(chart.getMinimumZ().getDoubleValue(), chart.getMaximumZ().getDoubleValue());
			legend.adjustLocation();
		}
		else if (legend!=null)
			legend.setVisible(false);

		// grid ========
		//		drawGrid(g, chart);
		//grid ===  

		String problem = null;

		g.setColor(Color.blue);
		valueX.setValue(0);
		valueY.setValue(0);
		int xZero = xToPixel(valueX.getDoubleValue(), chart); 
		int yZero =  yToPixel(valueY.getDoubleValue(), chart); 
		g.drawLine(xZero, margin, xZero, fieldHeight - margin);
		g.drawLine(margin, yZero, fieldWidth - margin, yZero);
		g.setColor(Color.black);
		if (showNames && chart.namesExist())
			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (getSuspendDrawing())
					return;
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					String name =chart.getName(i);
					if (name!=null) {
						chart.getXArray().placeValue(i, valueX);
						chart.getYArray().placeValue(i, valueY);
						int xPixel = xToPixel(valueX.getDoubleValue(), chart)-markerWidth/2; 
						int yPixel =  yToPixel(valueY.getDoubleValue(), chart)- markerWidth/2;
						Color fc = getPointFrameColor(chart, i);
						if (fc==Color.black) {
							StringUtil.highlightString(g, name, xPixel+ markerWidth + 4, yPixel+markerHeight/2+2, Color.black, Color.white);
						}
						else {
							g.setColor(fc);
							g.drawString(name, xPixel+ markerWidth + 4, yPixel+markerHeight/2+2);
							g.setColor(Color.black);
						}
					}
				}
			}
		if (joinTheDots){
			int lastX=MesquiteInteger.unassigned;
			int lastY = MesquiteInteger.unassigned;
			int firstX=MesquiteInteger.unassigned;
			int firstY = MesquiteInteger.unassigned;
			RotatedRectangle rot = null;
			if (thickJoin)
				rot = new RotatedRectangle();
			boolean first = true;
			for (int i= 0; i<chart.getNumPoints(); i++) {
				if (getSuspendDrawing())
					return;
				if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
					chart.getXArray().placeValue(i, valueX);
					chart.getYArray().placeValue(i, valueY);
					int xPixel = xToPixel(valueX.getDoubleValue(), chart); //margin+valueX.setWithinBounds(chart.getAxisMinimumX(), chart.getAxisMaximumX(), coreWidth)-markerWidth/2;
					int yPixel = yToPixel(valueY.getDoubleValue(), chart); //fieldHeight-margin-valueY.setWithinBounds(chart.getAxisMinimumY(), chart.getAxisMaximumY(), coreHeight)-markerHeight/2;
					if (first) {
						firstX = xPixel;
						firstY = yPixel;
						first = false;
					}
					if (!chart.isMinimumXConstrained() && !chart.isMaximumXConstrained()) {
						if (xPixel < 0 || xPixel > chart.getField().getBounds().width && problem == null)
							problem = "x " + valueX + " pixel " + xPixel + " field width " + chart.getField().getBounds().width + " axis Max " + chart.getAxisMaximumX();
					}
					if (!chart.isMinimumYConstrained() && !chart.isMaximumYConstrained()) {
						if (yPixel < 0 || yPixel > chart.getField().getBounds().height && problem == null)
							problem = "y " + valueY + " pixel " + yPixel + " field height " + chart.getField().getBounds().height+ " axis Max " + chart.getAxisMaximumY();
					}
					if (MesquiteInteger.isCombinable(lastX) && MesquiteInteger.isCombinable(lastY)){
						Color c =getPointColor(chart, i);
						if (thickJoin) {
							if (c!=null) g.setColor(c);
							rot.setShape(xPixel, yPixel, lastX, lastY, markerWidth, false, RotatedRectangle.RECTANGLE);
							rot.fill(g, false);
							g.setColor(getPointFrameColor(chart, i));
							rot.draw(g);
						}
						else {
							g.setColor(getPointFrameColor(chart, i));
							g.drawLine(xPixel, yPixel, lastX, lastY);
						}
					}
					lastX = xPixel;
					lastY = yPixel;
				}
			}
			if (joinLastToFirst && MesquiteInteger.isCombinable(lastX) && MesquiteInteger.isCombinable(lastY)&& MesquiteInteger.isCombinable(firstX) && MesquiteInteger.isCombinable(firstY))
				g.drawLine(firstX, firstY, lastX, lastY);
		}
		if (showSpots){
			boolean doSelected = false;
			boolean someAreSelected= chart.getSelected().anyBitsOn();
			for (int pass = 0; (pass<=1 && someAreSelected) || (pass<1); pass++){ // if any are selected, do two passes, first unselected ones, second selected ones so they sit on top.
				for (int i= 0; i<chart.getNumPoints(); i++) {
					if (getSuspendDrawing())
						return;
					if (!chart.getXArray().isUnassigned(i) && !chart.getYArray().isUnassigned(i)) {
						if (!someAreSelected || (someAreSelected && doSelected && chart.getSelected().isBitOn(i)) || (someAreSelected && !doSelected && !chart.getSelected().isBitOn(i))){
							chart.getXArray().placeValue(i, valueX);
							chart.getYArray().placeValue(i, valueY);
							int xPixel = xToPixel(valueX.getDoubleValue(), chart); //margin+valueX.setWithinBounds(chart.getAxisMinimumX(), chart.getAxisMaximumX(), coreWidth)-markerWidth/2;
							int yPixel = yToPixel(valueY.getDoubleValue(), chart); //fieldHeight-margin-valueY.setWithinBounds(chart.getAxisMinimumY(), chart.getAxisMaximumY(), coreHeight)-markerHeight/2;
							if (!chart.isMinimumXConstrained() && !chart.isMaximumXConstrained()) {
								if (xPixel < 0 || xPixel > chart.getField().getBounds().width && problem == null)
									problem = "x " + valueX + " pixel " + xPixel + " field width " + chart.getField().getBounds().width + " axis Max " + chart.getAxisMaximumX();
							}
							if (!chart.isMinimumYConstrained() && !chart.isMaximumYConstrained()) {
								if (yPixel < 0 || yPixel > chart.getField().getBounds().height && problem == null)
									problem = "y " + valueY + " pixel " + yPixel + " field height " + chart.getField().getBounds().height+ " axis Max " + chart.getAxisMaximumY();
							}
							g.setColor(getPointColor(chart, i));
							int markerW;
							int markerH;
							markerW = markerWidth;
							markerH = markerHeight;
							if (useCircle){
								g.fillOval(xPixel - markerWidth/2, yPixel - markerWidth/2, markerWidth, markerHeight);
								g.setColor(getPointFrameColor(chart, i));
								g.drawOval(xPixel - markerWidth/2, yPixel - markerWidth/2, markerWidth, markerHeight);
							}
							else {
								g.fillRect(xPixel - markerWidth/2, yPixel - markerWidth/2, markerWidth+1, markerHeight+1);
							}
						}
					}
				}
				doSelected = true;
			}
		}
		if (problem != null)
			MesquiteMessage.println("Error: a scattergram value was drawn outside of the bounds of the chart: " + problem);
		g.setColor(Color.black);
	}
	public void drawBlank(Graphics g, MesquiteChart chart){
		int margin = chart.getMargin();
		g.setColor(Color.white);
		g.fillRect(margin, margin, chart.getFieldWidth()-2*margin, chart.getFieldHeight()-2*margin);
		g.setColor(Color.black);
		g.drawRect(margin, margin, chart.getFieldWidth()-2*margin, chart.getFieldHeight()-2*margin);
	}
	public String getName() {
		return "Scattergram";
	}
}

/* legend for scattergrams in which the data points are colored */
class ScattergramColorLegend extends Legend {
	private Scattergram module;
	private String[] stateNames;
	private Color[] legendColors;
	private static final int defaultLegendWidth=142;
	private static final int defaultLegendHeight=120;
	private int legendWidth=defaultLegendWidth;
	private int legendHeight=defaultLegendHeight;
	private int numBoxes=10;
	private int oldNumChars = 0;
	private int oldNumBoxes=0;
	private int oldCurrentChar = -1;
	private boolean resizingLegend=false;
	ScattergramCharter charter;
	TextArea specsBox;
	private boolean holding = false;
	final int scrollAreaHeight = 6;
	private int messageHeight = 0;
	final int defaultSpecsHeight = (12 + MesquiteModule.textEdgeCompensationHeight) * 3;
	private int specsHeight = defaultSpecsHeight;
	double previousMin =MesquiteDouble.unassigned;
	double previousMax = MesquiteDouble.unassigned;
	boolean collapsed = false;
	public ScattergramColorLegend(Scattergram module, ScattergramCharter charter) {
		super(defaultLegendWidth, defaultLegendHeight);
		setVisible(false);
		setOffsetX(0);
		setOffsetY(0);
		this.charter = charter;
		this.module = module;

		setBackground(Color.white);
		setLayout(null);
		setSize(legendWidth, legendHeight);
		stateNames = new String[64];
		legendColors = new Color[64];
		for (int i=0; i<64; i++) {
			stateNames[i] = null;
			legendColors[i] = null;
		}
		specsBox = new TextArea(" ", 2, 2, TextArea.SCROLLBARS_NONE);
		specsBox.setEditable(false);
		if (module.showLegend.getValue())
			specsBox.setVisible(false);
		specsBox.setBounds(1,scrollAreaHeight+2,legendWidth-2, specsHeight);
		add(specsBox);

		defaultOffsets();

		reviseBounds();
	}


	public void defaultOffsets() {
		int buffer = 4;
		setOffsetX(buffer +1000);
		setOffsetY(buffer);
		//setOffsetX(-(defaultWidth+buffer));
		//setOffsetY(-(defaultHeight+buffer));
	}
	public void setVisible(boolean b) {
		super.setVisible(b);
		//if (messageBox!=null)
		//	messageBox.setVisible(b);
		if (specsBox!=null)
			specsBox.setVisible(b);
	}
	public void setMinMax(double min, double max){
		if (previousMin != min || previousMax!=max){
			previousMin = min;
			previousMax = max;
			for (int i=0; i<10; i++)
				legendColors[i]= (charter.colorTable.getColor(min + (i*((max-min)/10.0)), min, max));
			if (min == max && !collapsed){
				collapsed = true;
				reviseBounds();
			}
			repaint();
		}
	}

	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (!holding) {
			g.setColor(Color.black);
			if (numBoxes!=0) {
				for (int ibox=0; ibox<numBoxes; ibox++) {
					g.setColor(legendColors[ibox]);
					g.fillRect(4, ibox*16+scrollAreaHeight + specsHeight+ 2, 20, 12);
					g.setColor(Color.black);
					g.drawRect(4, ibox*16+scrollAreaHeight + specsHeight + 2, 20, 12);
					g.drawString(MesquiteDouble.toString(previousMin + (ibox*((previousMax-previousMin)/10.0))), 28, ibox*16 + specsHeight+scrollAreaHeight + 14);
				}
			}
			g.setColor(Color.cyan);
			g.drawRect(0, 0, legendWidth-1, legendHeight-1);
			g.fillRect(0, 0, legendWidth-1, scrollAreaHeight);
			g.setColor(Color.black);
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void printAll(Graphics g) {
		g.setColor(Color.black);
		int QspecsHeight = 0;
		int lastBox = scrollAreaHeight + QspecsHeight + 2 + 12;
		if (numBoxes!=0) {
			for (int ibox=0; ibox<numBoxes; ibox++) {
				g.setColor(legendColors[ibox]);
				g.fillRect(4, ibox*16+scrollAreaHeight + QspecsHeight + 2, 20, 12);
				g.setColor(Color.black);
				g.drawRect(4, ibox*16+scrollAreaHeight + QspecsHeight + 2, 20, 12);
				g.drawString(stateNames[ibox], 28, ibox*16 + QspecsHeight+scrollAreaHeight + 14);
				lastBox =ibox*16+scrollAreaHeight + QspecsHeight + 2 + 12;
			}
		}
	}


	public void reviseBounds(){
		specsHeight  = defaultSpecsHeight;
		specsBox.setBounds(1,scrollAreaHeight+2,legendWidth-2, specsHeight);
		specsBox.setVisible(true);

		/*messageHeight = messageBox.getHeightNeeded();
		if (messageHeight<20)
			messageHeight = 20;
		 */
		if (collapsed)
			legendHeight=scrollAreaHeight + specsHeight + 2 + messageHeight;
		else
			legendHeight=numBoxes*16+scrollAreaHeight + specsHeight + 2 + messageHeight;
		Point where = getLocation();
		setBounds(where.x,where.y,legendWidth, legendHeight);
		//messageBox.setBounds(2,legendHeight-messageHeight-1,legendWidth-4, messageHeight);
	}
	public void setMessage(String s) {
		if (s==null || s.equals("")) {
			//messageBox.setBackground(ColorDistribution.light);
			//messageBox.setText("\n");
			reviseBounds();
		}
		else {
			//messageBox.setBackground(Color.white);
			//messageBox.setText(s);
			reviseBounds();
		}
	}
	public void onHold() {
		holding = true;
	}

	public void offHold() {
		holding = false;
	}
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, ChartTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (clickCount>1 && y<scrollAreaHeight) {
			collapsed = !collapsed;
			reviseBounds();
		}
		super.mouseDown(modifiers, clickCount, when, x, y, tool);
		MesquiteWindow.uncheckDoomed(this);
	}
}


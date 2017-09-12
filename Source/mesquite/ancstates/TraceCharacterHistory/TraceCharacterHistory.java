/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ancstates.TraceCharacterHistory;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class TraceCharacterHistory extends TreeDisplayAssistantMA {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharHistorySource.class, getName() + " needs a source of character histories.",
				"The source of a traced history is be default a reconstruction from a supplied character distribution.  Afterwards you may choose an alternative source using the Character History Source submenu");
		EmployeeNeed e3 = registerEmployeeNeed(DisplayStatesAtNodes.class, getName() + " needs a method to display the results.",
				"The method to display results can be selected in the Trace Display Mode submenu");
		EmployeeNeed e4 = registerEmployeeNeed(TraceCharacterInit.class, getName() + " uses assistant modules to extend the analysis.",
				"These assistant modules are arranged automatically by " + getName());
	}
	CharHistorySource historyTask;
	DisplayStatesAtNodes displayTask;
	private CharacterHistory currentHistory=null;
	private Tree currentTree=null;
	private Taxa currentTaxa=null;
	int currentChar=0;
	long currentMapping = 0;
	int lastCharRetrieved = -1;
	private String currentCharacterName;
	MesquiteString historyTaskName;
	MesquiteString displayTaskName;
	public MesquiteBoolean showLegend, showWindow;
	int initialOffsetX=MesquiteInteger.unassigned;
	int initialOffsetY= MesquiteInteger.unassigned;
	int initialLegendWidth=MesquiteInteger.unassigned;
	int initialLegendHeight= MesquiteInteger.unassigned;
	Vector traces;
	MesquiteCommand htC, dtC;
	MesquiteBoolean showStateWeights;
	MesquiteInteger colorMode;
	boolean suspend = false;
	MesquiteMenuItemSpec propWeight = null;
	MesquiteMenuItemSpec  revertColorsItem;
	MesquiteMenuItemSpec  setColorsAsDefaultItem;
	MesquiteMenuItemSpec  binsMenuItem, numBinsMenuItem;
	MesquiteSubmenuSpec colorSubmenu;
	double[] binBoundaries;
	double[] usedBoundaries;
	boolean enableStore = false;  //should be false for release
	Point[] whichColorsModified = new Point[64];
	Color[] newColors = new Color[64];
	String startingColors = null;
	MesquiteString colorModeName;
	String[] colorModeNames;
	MesquiteModule windowBabySitter;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showLegend = new MesquiteBoolean(true);
		showWindow = new MesquiteBoolean(false);
		// todo: if hit goaway, then set showWindow to false and adjust menu checkmark;
		// correct size to begin with
		// snapshot
		suspend = MesquiteThread.isScripting();

		historyTask = (CharHistorySource)hireNamedEmployee(CharHistorySource.class, "#RecAncestralStates");
		if (historyTask == null)
			historyTask = (CharHistorySource)hireEmployee(CharHistorySource.class, "Source of character histories to trace");
		if (historyTask == null) {
			return sorry(getName() + " couldn't start because source of character histories not obtained.");
		}
		historyTaskName = new MesquiteString(historyTask.getName());
		traces = new Vector();
		displayTask = (DisplayStatesAtNodes)hireEmployee(DisplayStatesAtNodes.class, null);
		if (displayTask == null) {
			return sorry(getName() + " couldn't start because display module not obtained.");
		}
		dtC = makeCommand("setDisplayMode",  this);
		displayTask.setHiringCommand(dtC);
		makeMenu("Trace");
		resetContainingMenuBar();
		displayTaskName = new MesquiteString(displayTask.getName());

		htC = makeCommand("setHistorySource",  this);
		if (numModulesAvailable(CharHistorySource.class)>1) {
			historyTask.setHiringCommand(htC);
			MesquiteSubmenuSpec mss = addSubmenu(null, "Character History Source", htC, CharHistorySource.class);
			mss.setSelected(historyTaskName);
		}
		MesquiteMenuItemSpec mm = addMenuItem( "Next " + historyTask.getHistoryTypeName(), makeCommand("nextCharacter",  this));
		mm.setShortcut(KeyEvent.VK_RIGHT); //right
		mm = addMenuItem( "Previous " + historyTask.getHistoryTypeName(), makeCommand("previousCharacter",  this));
		mm.setShortcut(KeyEvent.VK_LEFT); //right
		addMenuItem( "Choose " + historyTask.getHistoryTypeName() + "...", makeCommand("chooseCharacter",  this));
		if (enableStore)
			addMenuItem( "Store History...", makeCommand("storeHistory",  this));
		addCheckMenuItem(null, "Show Legend", makeCommand("toggleShowLegend",  this), showLegend);
		MesquiteSubmenuSpec msDT = addSubmenu(null, "Trace Display Mode", dtC, DisplayStatesAtNodes.class); 
		msDT.setSelected(displayTaskName);

		colorSubmenu = addSubmenu(null, "Colors");

		colorMode = new MesquiteInteger(0);
		ListableVector f  = new ListableVector();
		colorModeNames = new String[5];
		colorModeNames[0] = "Full Colors";
		colorModeNames[1] = "Grayscale";
		colorModeNames[2] = "Redscale";
		colorModeNames[3] = "Greenscale";
		colorModeNames[4] = "Bluescale";
		colorSubmenu.setEnabled(false);

		for (int i= 0; i<colorModeNames.length; i++)
			f.addElement(new MesquiteString(colorModeNames[i]), false);
		MesquiteSubmenuSpec mcms = addSubmenu(null, "Colors", makeCommand("setColorMode", this), f);
		colorModeName = new MesquiteString(colorModeNames[colorMode.getValue()]);
		mcms.setSelected(colorModeName);

		numBinsMenuItem = addMenuItem("Set Number of Bins...", makeCommand("setNumBins", this));
		numBinsMenuItem.setEnabled(false);
		binsMenuItem = addMenuItem("Set Bin Boundaries...", makeCommand("setBins", this));
		binsMenuItem.setEnabled(false);
		setColorsAsDefaultItem = addMenuItem("Set Custom Colors as Defaults", makeCommand("saveModColors", this));
		setColorsAsDefaultItem.setEnabled(false);
		revertColorsItem = addMenuItem("Revert Default Colors to Factory", makeCommand("revertColors", this));
		revertColorsItem.setEnabled(false);
		showStateWeights = new MesquiteBoolean(true);
		propWeight = addCheckMenuItem(null, "Display Proportional to Weight", makeCommand("toggleWeights", this), showStateWeights);
		propWeight.setEnabled(historyTask.allowsStateWeightChoice());
		addCheckMenuItem(null, "Show Differences Window", makeCommand("toggleShowWindow",  this), showWindow);
		addMenuItem(null, "Export Table of Node Differences...", makeCommand("exportDifferences",  this));
		MesquiteTrunk.resetMenuItemEnabling();
		addMenuItem( "Close Trace", makeCommand("closeTrace",  this));
		addMenuSeparator();
		boolean someInits = false;
		hireAllEmployees(TraceCharacterInit.class);
		Enumeration enumeration=getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof TraceCharacterInit) {
				TraceCharacterInit init = (TraceCharacterInit)obj;
				init.setCharacterHistoryContainers(traces);
				someInits = true;
			}
		}
		loadPreferences();
		return true;
	}
	MesquiteHTMLWindow window;
	void initiateWindow(){
		if (window == null) {
			windowBabySitter = hireNamedEmployee (WindowHolder.class, "#WindowBabysitter");
			if (windowBabySitter == null)
				return;
			window = new MesquiteHTMLWindow(windowBabySitter, null, "Trace Character", false);
			window.setBackEnabled(false);
			windowBabySitter.setModuleWindow(window);
		}
		window.setText(getTracesText());
		window.setPopAsTile(true);
		window.setPreferredPopoutWidth(300);
		window.popOut(true);
		lastWindowStatePopped = true;
		window.setVisible(true);
		window.show();
		window.setWindowSize(300, MesquiteInteger.unassigned);
		resetAllMenuBars();

	}
	boolean lastWindowStatePopped = true;
	int resetCount = 0;
	/*.................................................................................................................*/
	void resetWindow(){
		if (window != null) {
			if (showWindow.getValue()){
				if (!window.isVisible()){
					window.setVisible(true);
				}

				if (lastWindowStatePopped && !window.isPoppedOut()){
					window.setPopAsTile(true);
					window.setPreferredPopoutWidth(300);
					window.popOut(true);
				}
				
				window.setText(getTracesText());
			}
			else {
				lastWindowStatePopped = window.isPoppedOut();
				window.setVisible(false);
			}
		}
	}
	String getTracesText(){
		StringBuffer sb = new StringBuffer();
		sb.append("<h2>Trace Character</h2>");
		sb.append(getTraceHTMLAllOperators());
		sb.append("<p>You can see the node numbers by going to the Tree Window and selecting the menu item Display&gt;Show Node Numbers, " 
		+ "or by looking at the text view of the Tree Window (menu item Window&gt;View Mode&gt;Text.");
		return sb.toString();
		
	}
	public boolean suppliesWritableResults(){
		return traces.size()<2;
	}
	public Object getWritableResults(){
		if (traces == null || traces.size() != 1)
			return null;
		TraceCharacterOperator trace = (TraceCharacterOperator)traces.elementAt(0);
		if (trace.history == null)
			return null;
		String results = historyTask.getName();

		results += "\t" + trace.history.toStringWithDetails();
		return results;
	}

	public Object getResultsHeading(){
		if (traces.size() != 1)
			return null;
		TraceCharacterOperator trace = (TraceCharacterOperator)traces.elementAt(0);
		String results = historyTask.getName();
		return results;
	}


	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == displayTask || employee == historyTask)  
			iQuit();
		else if (employee == windowBabySitter){
			windowBabySitter = null;
			showWindow.setValue(false);
			window = null;
			lastWindowStatePopped = true;
		}
	}

	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		currentTaxa = treeDisplay.getTaxa();
		TraceCharacterOperator newTrace = new TraceCharacterOperator(this, treeDisplay);
		traces.addElement(newTrace);
		if (startingColors != null){
			String command = parser.getFirstToken(startingColors);
			String arguments = parser.getRemaining();
			doCommand(command, arguments, CommandChecker.defaultChecker);
		}
		return newTrace;
	}
	/*.................................................................................................................*/
	public boolean showLegend(){
		return showLegend.getValue();
	}
	public int getInitialOffsetX(){
		return initialOffsetX;
	}
	public int getInitialOffsetY(){
		return initialOffsetY;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("startingColors".equalsIgnoreCase(tag)) {
			startingColors = StringUtil.cleanXMLEscapeCharacters(content);
			if (StringUtil.blank(startingColors))
				startingColors = null;
		}
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(20);
		if (startingColors == null)
			StringUtil.appendXMLTag(buffer, 2, "startingColors", "");  
		else
			StringUtil.appendXMLTag(buffer, 2, "startingColors", startingColors);  

		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("suspend ");
		temp.addLine("setDisplayMode ",displayTask);
		temp.addLine("setHistorySource ",historyTask);
		temp.addLine("setCharacter " + CharacterStates.toExternal(currentChar));
		if (binBoundaries != null) {
			temp.addLine("setNumBins " + (binBoundaries.length+1));
			String s = "";
			for (int i= 0;  i< binBoundaries.length; i++)
				s += " " + MesquiteDouble.toString(binBoundaries[i]);
			temp.addLine("setBins " + s);
		}

		temp.addLine("setMapping " + CharacterStates.toExternalLong(currentMapping));
		temp.addLine("toggleShowLegend " + showLegend.toOffOnString());
		if (showWindow.getValue())
			temp.addLine("toggleShowWindow " + showWindow.toOffOnString(), windowBabySitter);
			
		temp.addLine("setColorMode " + colorMode.getValue());
		temp.addLine("toggleWeights " + showStateWeights.toOffOnString());
		TraceCharacterOperator tco = (TraceCharacterOperator)traces.elementAt(0);
		if (tco!=null && tco.traceLegend!=null) {
			temp.addLine("setInitialOffsetX " + tco.traceLegend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetY " + tco.traceLegend.getOffsetY());
			temp.addLine("setLegendWidth " + tco.traceLegend.getLegendWidth()); //Should go operator by operator!!!
			temp.addLine("setLegendHeight " + tco.traceLegend.getLegendHeight());
		}
		temp.addLine("resume ");
		if (tco!=null && tco.traceLegend!=null) {
			temp.addLine(tco.traceLegend.getModColorsCommand());
		}
		return temp;
	}
	boolean suspendCommandReceived = false;
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module used to display character history", "[name of module]", commandName, "setDisplayMode")) {
			DisplayStatesAtNodes temp=  (DisplayStatesAtNodes)replaceEmployee(DisplayStatesAtNodes.class, arguments, "Display mode", displayTask);
			if (temp!=null) {
				displayTask= temp;
				displayTask.setHiringCommand(dtC);
				displayTaskName.setValue(displayTask.getName());
				resetFromScratchAllTraceOperators();
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
			return displayTask;
		}
		else if (checker.compare(this.getClass(), "Sets modified colors", "", commandName, "modifyColors")) {
			pos.setValue(0); 
			int i = 0; 
			whichColorsModified = new Point[64];
			newColors = new Color[64];
			int count = 0;
			while (MesquiteInteger.isCombinable(i= MesquiteInteger.fromString(arguments, pos)) && count<64){
				int j =  MesquiteInteger.fromString(arguments, pos);
				int red =  MesquiteInteger.fromString(arguments, pos);
				int green =  MesquiteInteger.fromString(arguments, pos);
				int blue =  MesquiteInteger.fromString(arguments, pos);
				if (MesquiteInteger.isCombinable(j) && MesquiteInteger.isCombinable(red) && MesquiteInteger.isCombinable(green) && MesquiteInteger.isCombinable(blue)){
					newColors[count] = new Color(red, green, blue);
					whichColorsModified[count++] = new Point(i, j);
				}
			}
			if (count>0){
				revertColorsItem.setEnabled(true);
				setColorsAsDefaultItem.setEnabled(true);
				MesquiteTrunk.resetMenuItemEnabling();
				notifyIncorpModColorsAllTO();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Reverts colors to defaults", "", commandName, "revertColors")) {
			revertColorsAllTO();
			revertColorsItem.setEnabled(false);
			startingColors = null;
			storePreferences();
			setColorsAsDefaultItem.setEnabled(false);
			MesquiteTrunk.resetMenuItemEnabling();
			parametersChanged();

		}
		else if (checker.compare(this.getClass(), "Remembers Modified Colors", "", commandName, "saveModColors")) {
			if (!MesquiteThread.isScripting())
				alert("This saves your currently modified colours as new defaults");  
			storePreferences();

			parametersChanged();

		}
		else if (checker.compare(this.getClass(), "Sets whether or not the legend is shown", "[on = show; off]", commandName, "toggleShowLegend")) {
			showLegend.toggleValue(parser.getFirstToken(arguments));
			toggleLegendAllTraceOperators();
			if (MesquiteThread.isScripting() && !suspendCommandReceived){ //must be old script
				suspend = false;
				recalculateAllTraceOperators(true);  
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Exports a text file summarizing differences from one node to the next", null, commandName, "exportDifferences")) {
			String s = getTraceTableAllOperators();
			if (StringUtil.blank(s))
				discreetAlert("Sorry, there are no traced histories with states");
			else {
				String where = MesquiteFile.saveFileAsDialog("Save table of nodes whose reconstructed state(s) differ from those of their ancestors");
				if (!StringUtil.blank(where))
					MesquiteFile.putFileContents(where, s, false);
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the trace window is shown", "[on = show; off]", commandName, "toggleShowWindow")) {
			showWindow.toggleValue(parser.getFirstToken(arguments));
			if (showWindow.getValue() && window == null)
				initiateWindow();
			else
				resetWindow();
			return windowBabySitter;
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the weights on states are shown (e.g. relative frequencies or likelihoods)", "[on; off]", commandName, "toggleWeights")) {
			showStateWeights.toggleValue(parser.getFirstToken(arguments));
			resetAllTraceOperators();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to use grayscale if continuous", "[on; off]", commandName, "toggleGray")) {
			String arg = parser.getFirstToken(arguments);
			if (arg != null && arg.equalsIgnoreCase("on")){
				colorMode.setValue(1);
				colorModeName.setValue(colorModeNames[1]);
				resetAllTraceOperators();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets colorMode if continuous", "[on; off]", commandName, "setColorMode")) {
			int w = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (w>=0 && w< colorModeNames.length) {
				colorMode.setValue(w);
				colorModeName.setValue(colorModeNames[w]);
				resetColorsAllTraceOperators();
				if (!MesquiteThread.isScripting()){
					resetAllTraceOperators();
					parametersChanged();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets number of bins for continuous", "[number of bins]", commandName, "setNumBins")) {

			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			int numBins = MesquiteInteger.unassigned;
			if (binBoundaries != null) 
				numBins = binBoundaries.length+1;
			if (!MesquiteInteger.isCombinable(newNum)) {
				MesquiteInteger io = new MesquiteInteger(numBins);

				boolean b = QueryDialogs.queryInteger(containerOfModule(), "Set number of bins", "Number of bins for continuous character:\n\n(Enter \"?\" to indicate default binning)", null, true, io);

				if (!b)
					return null;
				newNum = io.getValue();
			}
			if (!MesquiteInteger.isCombinable(newNum))
				binBoundaries = null;
			else if (newNum-1>=1 && newNum-1<50 && (newNum!=numBins || binBoundaries == null || binBoundaries.length+1 != newNum)){
				binBoundaries = new double[newNum-1];
				DoubleArray.deassignArray(binBoundaries);
			}



			resetAllTraceOperators();
			parametersChanged();

		}
		else if (checker.compare(this.getClass(), "Sets bins for continuous", "[vector of bins]", commandName, "setBins")) {
			boolean set = false;
			double binEdge = MesquiteDouble.unassigned;
			pos.setValue(0);
			int count = 0;
			while (MesquiteDouble.impossible != (binEdge = MesquiteDouble.fromString(arguments, pos))){
				if (count <binBoundaries.length)
					binBoundaries[count++] = binEdge;
				set = true;
			}
			if (set)
				return null;

			double[] qBins = queryBins(binBoundaries);
			if (qBins != null)
				binBoundaries = qBins;
			resetAllTraceOperators();
			parametersChanged();

		}
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspend = true;
			suspendCommandReceived = true;
		}
		else if (checker.compare(this.getClass(), "Resume calculations", null, commandName, "resume")) {
			suspend = false;
			suspendCommandReceived = false;
			recalculateAllTraceOperators(true);
			parametersChanged();
		}

		/*  The following is disabled for the moment, as storing and retrieving of character histories is not yet supported */
		else if (enableStore && checker.compare(this.getClass(), "Stores the current character history", null, commandName, "storeHistory")) {
			//todo: if more than one traced, query user which one to store��������������������
			TraceCharacterOperator tco = (TraceCharacterOperator)traces.elementAt(0);
			if (tco!=null && tco.traceLegend!=null && tco.history!=null && tco.myTree!=null) {
				CharacterHistory history = tco.history;
				CharacterHistory cloned = history.clone(null);

				Tree tree = tco.myTree.cloneTree();
				String s = MesquiteString.queryString(containerOfModule(), "Store History" , "Name of History: ", history.getName() );
				if (s==null)
					return null;
				cloned.setName(s);
				StoredCharacterHistory sHistory = new StoredCharacterHistory(cloned, tree);
				if (sHistory!=null) {
					sHistory.setName(s);
					sHistory.addToFile( getProject().getHomeFile(), getProject(), findElementManager(StoredCharacterHistory.class));  //TODO: should allow user to choose if more than one file
					getProject().addFileElement(sHistory);
				} //TODO: should set current tree to show this one if stored trees used
			}
		}
		/*  	*/
		else if (checker.compare(this.getClass(), "Sets initial horizontal offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetX")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;
				Enumeration e = traces.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TraceCharacterOperator) {
						TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
						if (tCO.traceLegend!=null)
							tCO.traceLegend.setOffsetX(offset);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets initial vertical offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetY")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetY = offset;
				Enumeration e = traces.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TraceCharacterOperator) {
						TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
						if (tCO.traceLegend!=null)
							tCO.traceLegend.setOffsetY(offset);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets initial width of legend", "[width in pixels]", commandName, "setLegendWidth")) {
			int x= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(x)) {
				initialLegendWidth = x;
				if (initialLegendWidth<50)
					initialLegendWidth=50;
				Enumeration e = traces.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TraceCharacterOperator) {
						TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
						if (tCO.traceLegend!=null)
							tCO.traceLegend.setLegendWidth(x);
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Sets initial height of legend", "[height in pixels]", commandName, "setLegendHeight")) {
			int x= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(x)) {
				initialLegendHeight = x;
				if (initialLegendHeight<50)
					initialLegendHeight=50;
				Enumeration e = traces.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TraceCharacterOperator) {
						TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
						if (tCO.traceLegend!=null)
							tCO.traceLegend.setLegendHeight(x);
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets module supplying character histories", "[name of module]", commandName, "setHistorySource")) {
			CharHistorySource temp =  (CharHistorySource)replaceEmployee(CharHistorySource.class, arguments, "Source of character histories", historyTask);
			if (temp!=null) {
				historyTask=  temp;
				historyTask.setHiringCommand(htC);
				historyTaskName.setValue(historyTask.getName());
				propWeight.setEnabled(historyTask.allowsStateWeightChoice());
				MesquiteTrunk.resetMenuItemEnabling();
				currentChar=0;
				resetAllTraceOperators();
				recalculateAllTraceOperators(true);
				if (!MesquiteThread.isScripting())
					parametersChanged();
			}
			return historyTask;
		}
		else if (checker.compare(this.getClass(), "NOT YET USED", null, commandName, "newAssistant")) {
			/* preparing for assistants to explore character correlation etc. based on reconstruction or shown character
    	 		incrementMenuResetSuppression();
    	 		TraceAssistant ntt= (TraceAssistant)hireNamedEmployee(TraceAssistant.class, arguments);
			decrementMenuResetSuppression();
			if (ntt!=null) {
    	 			ntt.setUseMenubar(false);
				Enumeration e = traces.elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					if (obj instanceof TraceLegendOperator) {
						TraceLegendOperator tLO = (TraceLegendOperator)obj;
						???????
			 		}
				}
				resetContainingMenuBar();
			}
			return ntt;
			 */
		}
		else if (checker.compare(this.getClass(), "Goes to next character history", null, commandName, "nextCharacter")) {
			if (currentChar>=historyTask.getNumberOfHistories(currentTaxa)-1)
				currentChar=0;
			else
				currentChar++;
			recalculateAllTraceOperators(true);

		}
		else if (checker.compare(this.getClass(), "Goes to previous character history", null, commandName, "previousCharacter")) {
			if (currentChar<=0)
				currentChar=historyTask.getNumberOfHistories(currentTaxa)-1;
			else
				currentChar--;
			recalculateAllTraceOperators(true);
		}

		else if (checker.compare(this.getClass(), "Queries user about which character history to use", null, commandName, "chooseCharacter")) {
			int ic=historyTask.queryUserChoose(currentTaxa, " to trace ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentChar = ic;
				recalculateAllTraceOperators(true);
			}
		}

		else if (checker.compare(this.getClass(), "Sets which character history to use", "[history number]", commandName, "setCharacter")) {
			pos.setValue(0);

			int icNum = MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && (ic<=historyTask.getNumberOfHistories(currentTaxa)-1)) {
				currentChar = ic;
				recalculateAllTraceOperators(true);
			}
		}
		else if (checker.compare(this.getClass(), "Sets which mapping to use", "[mapping number]", commandName, "setMapping")) {
			pos.setValue(0);
			int icNum = MesquiteInteger.fromString(arguments, pos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			currentMapping = CharacterStates.toInternalLong(icNum);
			recalculateAllTraceOperators(false);
		}
		else if (checker.compare(this.getClass(), "Returns the number of character histories available in the current character history source", null, commandName, "getNumberOfHistories")) {
			return new MesquiteInteger(historyTask.getNumberOfHistories(currentTaxa));
		}
		else if (checker.compare(this.getClass(), "Turns off trace character", null, commandName, "closeTrace")) {
			iQuit();
			resetContainingMenuBar();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	double[] queryBins(double[] bEdges){
		int numBins = MesquiteInteger.unassigned;
		if (bEdges != null) 
			numBins = bEdges.length+1;
		else
			numBins = 11;
		double[] qBinBoundaries = new double[numBins-1];
		for(int i=0; i<qBinBoundaries.length; i++)
			qBinBoundaries[i] = MesquiteDouble.unassigned;
		if (bEdges != null && qBinBoundaries.length == bEdges.length)
			for(int i=0; i<qBinBoundaries.length; i++)
				qBinBoundaries[i] = bEdges[i];
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Edges of Bins",buttonPressed);
		queryDialog.addLargeOrSmallTextLabel("Numerical values are currently in " + numBins + " bins.  Indicate the boundaries between bins.  Use \"?\" to let Mesquite choose the boundary automatically.  Current boundaries are shown at right.");
		SingleLineTextField[] fields = new SingleLineTextField[qBinBoundaries.length];
		for (int i = 0; i< qBinBoundaries.length; i++){
			fields[i] = queryDialog.addTextField("Boundary after bin " + (i+1), MesquiteDouble.toString(qBinBoundaries[i]), 16);
			if (usedBoundaries != null && i<usedBoundaries.length) {
				queryDialog.suppressNewPanel();
				queryDialog.addLabel(MesquiteDouble.toString(usedBoundaries[i]));
			}
		}
		queryDialog.completeAndShowDialog(true);
		for (int i = 0; i< qBinBoundaries.length; i++){

			qBinBoundaries[i] = MesquiteDouble.fromString(fields[i].getText());
		}
		queryDialog.dispose();
		if (buttonPressed.getValue()==0)
			return qBinBoundaries;
		return null;
	}
	/*.................................................................................................................*/
	public int getNumberOfHistories(Tree tree) {
		return historyTask.getNumberOfHistories(tree);
	}
	/*.................................................................................................................*/
	public long getNumberOfMappings(Tree tree) {
		return historyTask.getNumberOfMappings(tree, currentChar);
	}
	/*.................................................................................................................*/
	public void prepareHistory(Tree tree) {
		int maxnum = historyTask.getNumberOfHistories(tree);
		currentTaxa = tree.getTaxa();
		if (currentChar>= maxnum)
			currentChar = maxnum-1;
		if (currentChar<0)
			currentChar = 0;
		historyTask.prepareHistory(tree, currentChar);
		long nummap = historyTask.getNumberOfMappings(tree, currentChar);
		if (currentMapping>= nummap)
			currentMapping = nummap-1;
		if (currentMapping<0)
			currentMapping = 0;
		lastCharRetrieved = currentChar;
	}
	/*.................................................................................................................*/
	public CharacterHistory getMapping(Tree tree, MesquiteString resultString) {
		if (!MesquiteLong.isCombinable(currentMapping))
			currentMapping = 0;
		CharacterHistory currentHistory = historyTask.getMapping(currentMapping, null, resultString);
		return currentHistory;
	}
	/*.................................................................................................................*/
	public int getNumberCurrentHistory() {
		return CharacterStates.toExternal(currentChar);
	}
	/*.................................................................................................................*/
	public long getNumberCurrentMapping() {
		return CharacterStates.toExternalLong(currentMapping);
	}
	/*.................................................................................................................*/
	public void employeeOutputInvalid(MesquiteModule employee, MesquiteModule source) {
		blankAllTraceOperators();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==historyTask) {
			currentHistory = null;  // to force retrieval of new history
			if (propWeight.isEnabled() != historyTask.allowsStateWeightChoice()){
				propWeight.setEnabled(historyTask.allowsStateWeightChoice());
				MesquiteTrunk.resetMenuItemEnabling();
			}
			recalculateAllTraceOperators(true);
			resetAllTraceOperators();
			parametersChanged(notification);
		}
		else {
			resetAllTraceOperators();
		}
	}
	/*.................................................................................................................*/
	public void closeAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				tCO.turnOff();
			}
		}
	}
	boolean Q = true;
	/*.................................................................................................................*/
	public void resetColorsAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				tCO.resetColors();
			}
		}
	}
	public String getTraceTableAllOperators() {
		if (traces==null)
			return null;
		Enumeration e = traces.elements();
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				sb.append("\n");
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				tCO.getSummaryTable(sb);
			}
		}
		return sb.toString();
	}
	public String getTraceHTMLAllOperators() {
		if (traces==null)
			return null;
		Enumeration e = traces.elements();
		StringBuffer sb = new StringBuffer();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				tCO.getSummaryHTML(sb);
			}
		}
		return sb.toString();
	}
	public void resetAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				if (tCO.treeDisplay!=null)
					tCO.treeDisplay.pleaseUpdate(Q);
				if (tCO.traceLegend!=null)
					tCO.traceLegend.repaint();
			}
		}
	}
	/*.................................................................................................................*/
	public void resetFromScratchAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				if (tCO.decorator!=null)
					tCO.decorator.turnOff(); //why is this called?
				tCO.decorator = null;
				if (tCO.treeDisplay!=null)
					tCO.treeDisplay.pleaseUpdate(true);
				if (tCO.traceLegend!=null)
					tCO.traceLegend.repaint();
			}
		}
	}
	/*.................................................................................................................*/
	public void blankAllTraceOperators() {
		if (traces==null)
			return;
		currentHistory = null;  // to force retrieval of new observedStates
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				if (tCO.traceLegend!=null){
					tCO.traceLegend.setMessage("");
					tCO.traceLegend.setSpecsMessage("Calculating...");
				}
			}
		}
	}
	public void calculationsDone(){
		resetWindow();
	}
	/*.................................................................................................................*/
	public void recalculateAllTraceOperators(boolean doPreps) {
		if (traces==null || suspend)
			return;
		blankAllTraceOperators();
		currentHistory = null;  // to force retrieval of new observedStates
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				tCO.doCalculations(doPreps);
			}
		}

	}
	/*.................................................................................................................*/
	public void notifyIncorpModColorsAllTO() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				tCO.incorporateModColors();
				tCO.doCalculations(true);
			}
		}
	}
	/*.................................................................................................................*/
	public void revertColorsAllTO() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				tCO.revertColors();
			}
		}
	}
	/*.................................................................................................................*/
	public void toggleReconstructAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				tCO.toggleReconstruct();
			}
		}
	}
	/*.................................................................................................................*/
	public void toggleLegendAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCharacterOperator) {
				TraceCharacterOperator tCO = (TraceCharacterOperator)obj;
				if (tCO.treeDisplay!=null)
					tCO.treeDisplay.pleaseUpdate(Q);
				if (tCO.traceLegend!=null)
					tCO.traceLegend.setVisible(showLegend.getValue());
			}
		}
	}
	/*.................................................................................................................*/
	public void endJob() {
		closeAllTraceOperators();
		super.endJob();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Trace Character History";
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
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
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Traces a history of character evolution on the nodes of a drawn tree.";
	}
}



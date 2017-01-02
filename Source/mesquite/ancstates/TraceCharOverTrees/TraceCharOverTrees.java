/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ancstates.TraceCharOverTrees;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;


//TODO: ideally have it check to see if reconstructor generates frequencies before it lets user switch to average frequencies

/*======================================================================== */
public class TraceCharOverTrees extends TreeDisplayAssistantA implements TraceModule{
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharHistorySource.class, getName() + " needs a source of character histories.",
		"The source of a traced history can be chosen using the Character History Source submenu");
	}
	DisplayStatesAtNodes displayTask;
	Vector traces;
	MesquiteString displayTaskName;
	MesquiteCommand dtC;
	CharHistorySource historyTask;
	MesquiteString treeSourceName;
	MesquiteCommand cstC;
	TreeSource treeSourceTask;
	MesquiteMenuItemSpec numTreesItem = null;
	MesquiteMenuItemSpec showEquivocalItem = null;
	private int numTrees = 100;
	//not yet ready double minPercent = 0.0;
	public MesquiteBoolean showLegend;
	private MesquiteBoolean showNodeAbsentSlice = new MesquiteBoolean(true);
	private MesquiteBoolean showEquivocalSlice = new MesquiteBoolean(true);   

	int mode = TraceCOTOperator.UNIQUELY_BEST;
	int previousMode = TraceCOTOperator.UNIQUELY_BEST;

	StringArray modes;
	MesquiteString modeName;

	private boolean numTreesSet = false;
	boolean suppress = false;
	int currentChar=0;
	int initialOffsetX=MesquiteInteger.unassigned;
	int initialOffsetY= MesquiteInteger.unassigned;
	MesquiteCommand htC;
	MesquiteString historyTaskName;
	Taxa currentTaxa;
	int initialLegendWidth=MesquiteInteger.unassigned;
	int initialLegendHeight= MesquiteInteger.unassigned;
	int nodeToOutput=MesquiteInteger.unassigned;
	String nodeInfo = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showLegend = new MesquiteBoolean(true);
		if (MesquiteThread.isScripting())
			suppress = true;
		traces = new Vector();

		historyTask = (CharHistorySource)hireCompatibleEmployee(CharHistorySource.class, CategoricalState.class,"Source of character history (for Trace Character Over Trees)");

		if (historyTask == null) {
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		}
		makeMenu("Trace_Over_Trees");
		historyTaskName  = new MesquiteString(historyTask.getName());
		htC = makeCommand("setHistorySource",  this);
		if (numModulesAvailable(CharHistorySource.class)>1) {
			historyTask.setHiringCommand(htC);
			MesquiteSubmenuSpec mss = addSubmenu(null, "Character History Source", htC, CharHistorySource.class);
			mss.setSelected(historyTaskName);
		}
		MesquiteMenuItemSpec mm = addMenuItem( "Next Character History", makeCommand("nextCharacter",  this));
		mm.setShortcut(KeyEvent.VK_RIGHT); //right
		mm = addMenuItem( "Previous Character History", makeCommand("previousCharacter",  this));
		mm.setShortcut(KeyEvent.VK_LEFT); //right
		addMenuItem( "Choose Character History...", makeCommand("chooseCharacter",  this));

		modes = new StringArray(3);
		modes.setValue(TraceCOTOperator.UNIQUELY_BEST, "Count Trees with Uniquely Best States");
		modes.setValue(TraceCOTOperator.COUNT_EQUIVOCAL, "Count All Trees with State");
		modes.setValue(TraceCOTOperator.AVERAGE_FREQUENCIES, "Average Frequencies Across Trees");
		modeName = new MesquiteString(modes.getValue(mode));
		MesquiteSubmenuSpec mss = addSubmenu(null, "Calculate", makeCommand("setMode",  this), modes);
		mss.setSelected(modeName);

		addCheckMenuItem(null, "Show Legend", makeCommand("toggleShowLegend",  this), showLegend);
		addCheckMenuItem(null, "Show Fraction of Trees with Node Absent", makeCommand("toggleShowNodeAbsent",  this), showNodeAbsentSlice);
		showEquivocalItem = addCheckMenuItem(null, "Show Fraction of Trees with Equivocal", makeCommand("toggleShowEquivocal",  this), showEquivocalSlice);
		showEquivocalItem.setEnabled(mode == TraceCOTOperator.UNIQUELY_BEST);

		addMenuItem( "Save Values for a Node...", makeCommand("saveNodeValues",  this));

		displayTask = (DisplayStatesAtNodes)hireEmployee(DisplayStatesAtNodes.class, "Displayer of ancestral states");
		if (displayTask == null) {
			return sorry(getName() + " couldn't start because now display module was obtained.");
		}
		dtC = makeCommand("setDisplayMode",  this);
		displayTask.setHiringCommand(dtC);
		displayTaskName = new MesquiteString(displayTask.getName());
		if (numModulesAvailable(DisplayStatesAtNodes.class)>1){
			MesquiteSubmenuSpec mss2 = addSubmenu(null, "Trace Over Trees Display Mode", dtC, DisplayStatesAtNodes.class);
			mss2.setSelected(displayTaskName);
		}

		treeSourceTask = (TreeSource)hireEmployee(TreeSource.class,  "Source of trees for comparison by for Trace Character Over Trees");
		if (treeSourceTask == null) {
			return sorry(getName() + " couldn't start because no source of trees obtained");
		}
		treeSourceName = new MesquiteString(treeSourceTask.getName());


		cstC =  makeCommand("setTreeSource",  this);
		treeSourceTask.setHiringCommand(cstC);
		if (numModulesAvailable(TreeSource.class)>1){ 
			MesquiteSubmenuSpec mss3 = addSubmenu(null, "Tree source",cstC, TreeSource.class);
			mss3.setSelected(treeSourceName);
		}
		numTreesItem = addMenuItem("Number of Trees...", makeCommand("setNumTrees", this));
		numTreesItem.setEnabled(false);
		addMenuItem( "Close Trace Over Trees", makeCommand("closeTrace",  this));
		addMenuSeparator();
		resetContainingMenuBar();
		return true;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == displayTask || employee == historyTask)  
			iQuit();
	}
	public int getInitialOffsetX(){
		return initialOffsetX;
	}
	public int getInitialOffsetY(){
		return initialOffsetY;
	}
	public boolean showLegend(){
		return showLegend.getValue();
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		TraceCOTOperator newTrace = new TraceCOTOperator(this, treeDisplay);
		traces.addElement(newTrace);
		Tree tree = treeDisplay.getTree();
		if (tree !=null){
			currentTaxa = tree.getTaxa();
			int numTrees2 = treeSourceTask.getNumberOfTrees(currentTaxa);
			numTreesItem.setEnabled(!MesquiteInteger.isFinite(numTrees2));
			showEquivocalItem.setEnabled(mode == TraceCOTOperator.UNIQUELY_BEST);
			MesquiteTrunk.resetMenuItemEnabling();
		}
		return newTrace;
	}


	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("suppress ");
		temp.addLine("setHistorySource ",historyTask);
		temp.addLine("setCharacter " + CharacterStates.toExternal(currentChar));
		temp.addLine( "setTreeSource " , treeSourceTask);
		temp.addLine("setNumTrees " + numTrees);
		temp.addLine("setDisplayMode ",displayTask);
		temp.addLine("toggleShowLegend " + showLegend.toOffOnString());
		temp.addLine("toggleShowNodeAbsent " + showNodeAbsentSlice.toOffOnString());
		temp.addLine("toggleShowEquivocal " + showEquivocalSlice.toOffOnString());
		temp.addLine("setMode " + ParseUtil.tokenize(modes.getValue(mode)));
		//temp.addLine("setMinPercent " + minPercent);
		TraceCOTOperator tco = (TraceCOTOperator)traces.elementAt(0);
		if (tco!=null && tco.traceLegend!=null) {
			temp.addLine("setInitialOffsetX " + tco.traceLegend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetY " + tco.traceLegend.getOffsetY());
			temp.addLine("setLegendWidth " + tco.traceLegend.getLegendWidth()); //Should go operator by operator!!!
			temp.addLine("setLegendHeight " + tco.traceLegend.getLegendHeight());
		}
		temp.addLine("desuppress ");
		return temp;
	}
	/*.................................................................................................................*/
	private boolean querySaveNodeValues() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Save Values for Node",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Save Values for Node");
		String helpString = "This will let you save the values for a node for each tree to a text file.  You will need to enter the node number.  "
			+"To see node numbers, you can choose \"Show Node Numbers\" from the Display menu in the Tree window.";

		dialog.appendToHelpString(helpString);


		IntegerField nodeField = dialog.addIntegerField("Node Number:", nodeToOutput, 20);
		dialog.addHorizontalLine(1);


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			nodeToOutput = nodeField.getValue();

			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public void setToMode(int newMode) {
		previousMode = mode;
		mode = newMode;
		modeName.setValue(modes.getValue(mode));
		toggleModeAllTraceOperators();
		parametersChanged();
		showEquivocalItem.setEnabled(mode == TraceCOTOperator.UNIQUELY_BEST);
		MesquiteTrunk.resetMenuItemEnabling();
	}
	/*.................................................................................................................*/
	public void setToPreviousMode() {
		setToMode(previousMode);
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		/* not yet ready
    	 	if (checker.compare(this.getClass(), "Sets the minimum percent of occurances of a node for its states to be shown", "[minimum percent]", commandName, "setMinPercent")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		double T = MesquiteDouble.fromString(arguments, pos);
    	 		if (!MesquiteDouble.isCombinable(T))
				T = MesquiteDouble.queryDouble(containerOfModule(), "Minimum Percent Occurance", "Minimum percent of occurances of a node for its states to be shown.  If you set this to 10% and the node is present in only 9% of the trees, then its reconstructed states will not be summarized.", minPercent, 0, 100);
    	 		if (!MesquiteDouble.isCombinable(T))
    	 			return null;
	    	 	if (minPercent != T) {
	    	 		minPercent = T;
 				if (!MesquiteThread.isScripting())
 					redrawAllTraceOperators();
 			}
    	 	}
    	 	else 
		 */
		if (checker.compare(this.getClass(), "Sets module used to display reconstructed states", "[name of module]", commandName, "setDisplayMode")) {
			DisplayStatesAtNodes temp=  (DisplayStatesAtNodes)replaceEmployee(DisplayStatesAtNodes.class, arguments, "Display mode", displayTask);
			if (temp != null) {
				displayTask= temp;
				displayTask.setHiringCommand(dtC);
				displayTaskName.setValue(displayTask.getName());
				if (!suppress)
					parametersChanged();
				resetAllTraceOperators();
				return displayTask;
			}
		}
		else if (checker.compare(this.getClass(), "Goes to next character history", null, commandName, "nextCharacter")) {
			if (currentChar>=historyTask.getNumberOfHistories(currentTaxa)-1)
				currentChar=0;
			else
				currentChar++;
			recalcAllTraceOperators();

		}
		else if (checker.compare(this.getClass(), "Goes to previous character history", null, commandName, "previousCharacter")) {
			if (currentChar<=0)
				currentChar=historyTask.getNumberOfHistories(currentTaxa)-1;
			else
				currentChar--;
			recalcAllTraceOperators();
		}

		else if (checker.compare(this.getClass(), "Queries user about which character history to use", null, commandName, "chooseCharacter")) {
			int ic=historyTask.queryUserChoose(currentTaxa, " to trace ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentChar = ic;
				recalcAllTraceOperators();
			}
		}
		else if (checker.compare(this.getClass(), "Queries user about which node number to save values for", null, commandName, "saveNodeValues")) {
			if (querySaveNodeValues() && MesquiteInteger.isCombinable(nodeToOutput)) {
				nodeInfo=null;
				recalcAllTraceOperators();
				if (StringUtil.notEmpty(nodeInfo))
					MesquiteFile.putFileContentsQuery("Save file containing information about the designated node", nodeInfo,true);
			}
		}

		else if (checker.compare(this.getClass(), "Sets whether or not the legend is shown", "[on = show; off]", commandName, "toggleShowLegend")) {
			showLegend.toggleValue(parser.getFirstToken(arguments));
			toggleLegendAllTraceOperators();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the fraction of trees with node absent is shown", "[on = show; off]", commandName, "toggleShowNodeAbsent")) {
			showNodeAbsentSlice.toggleValue(parser.getFirstToken(arguments));
			recalcAllTraceOperators();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not the fraction of trees with equivocal states is shown", "[on = show; off]", commandName, "toggleShowEquivocal")) {
			showEquivocalSlice.toggleValue(parser.getFirstToken(arguments));
			recalcAllTraceOperators();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets how to count ambiguous state assignments at each node for each tree", "[0 = count state if in optimal state set, or has non-zero likelihood or other score, " +
				"1 = count state only if unique in assignment, with no others of non-zero score, 2 = count state if unique in assignment or if uniquely has best score]", commandName, "setMode")) {
			previousMode = mode;
			String name = parser.getFirstToken(arguments);
			int newMode = modes.indexOf(name);
			if (newMode >=0 && newMode!=mode){
				setToMode(newMode);
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
				recalcAllTraceOperators();
			}
		}
		else /**/
			if (checker.compare(this.getClass(), "Sets the number of trees", "[number]", commandName, "setNumTrees")) {
				int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
				if (!MesquiteInteger.isCombinable(newNum))
					newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set Number of Trees", "Number of Trees (for Trace Character over Trees):", numTrees, 0, MesquiteInteger.infinite);
				if (newNum>0  && newNum!=numTrees) {
					numTrees = newNum;
					numTreesSet = true;
					if (!suppress){
						recalcAllTraceOperators();
						parametersChanged();
					}
				}
				else if (numTrees == newNum)
					numTreesSet = true;
			}
			else /**/
				if (checker.compare(this.getClass(), "Sets the source of trees for comparison", "[name of module]", commandName, "setTreeSource")) {
					TreeSource temp =  (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees for comparison by for Trace Character Over Trees", treeSourceTask);
					if (temp!=null) {
						treeSourceTask=  temp;
						treeSourceTask.setHiringCommand(cstC);
						treeSourceName.setValue(treeSourceTask.getName());
						numTreesSet = false;
						if (!suppress){
							recalcAllTraceOperators();
							parametersChanged();
						}
					}	
					return temp;
				}
				else if (checker.compare(this.getClass(), "Sets module supplying character histories", "[name of module]", commandName, "setHistorySource")) {
					CharHistorySource temp =  (CharHistorySource)replaceCompatibleEmployee(CharHistorySource.class, arguments, historyTask, CategoricalState.class);
					if (temp!=null) {
						historyTask=  temp;
						historyTask.setHiringCommand(htC);
						historyTaskName.setValue(historyTask.getName());
						currentChar=0;
						resetAllTraceOperators();
						recalcAllTraceOperators();
						if (!suppress)
							parametersChanged();
					}
					return historyTask;
				}
				else if (checker.compare(this.getClass(), "Suppresses calculations", null, commandName, "suppress")) {
					suppress = true;
				}
				else if (checker.compare(this.getClass(), "Desuppresses calculations", null, commandName, "desuppress")) {
					suppress = false;
					parametersChanged();
				}
				else if (checker.compare(this.getClass(), "Returns ancestral states calculating module", null, commandName, "getAncStSource")) {
					return historyTask;
				}
				else if (checker.compare(this.getClass(), "Turns off Trace Character Over Trees", null, commandName, "closeTrace")) {
					iQuit();
					resetContainingMenuBar();
				}
				else if (checker.compare(this.getClass(), "Sets initial horizontal offset of legend from home position", "[offset in pixels]", commandName, "setInitialOffsetX")) {
					int offset= MesquiteInteger.fromFirstToken(arguments, pos);
					if (MesquiteInteger.isCombinable(offset)) {
						initialOffsetX = offset;
						Enumeration e = traces.elements();
						while (e.hasMoreElements()) {
							Object obj = e.nextElement();
							if (obj instanceof TraceCOTOperator) {
								TraceCOTOperator tCO = (TraceCOTOperator)obj;
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
							if (obj instanceof TraceCOTOperator) {
								TraceCOTOperator tCO = (TraceCOTOperator)obj;
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
							if (obj instanceof TraceCOTOperator) {
								TraceCOTOperator tCO = (TraceCOTOperator)obj;
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
							if (obj instanceof TraceCOTOperator) {
								TraceCOTOperator tCO = (TraceCOTOperator)obj;
								if (tCO.traceLegend!=null)
									tCO.traceLegend.setLegendHeight(x);
							}
						}
					}
				}
				else
					return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public int getNumTrees(){
		if (MesquiteThread.isScripting() || numTreesSet)
			return numTrees;
		int newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set Number of Trees", "Number of Trees (for Trace Character over Trees):", numTrees, 0, MesquiteInteger.infinite);
		if (newNum>0) {
			numTrees = newNum;
			numTreesSet = true;
		}
		return numTrees;
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee == displayTask)
			redrawAllTraceOperators();
		else if (Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED)
			recalcAllTraceOperators();
	}
	/*.................................................................................................................*/
	public void recalcAllTraceOperators() {
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCOTOperator) {
				TraceCOTOperator tCO = (TraceCOTOperator)obj;
				tCO.recalculate();
			}
		}
	}
	/*.................................................................................................................*/
	public void resetAllTraceOperators() {
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCOTOperator) {
				TraceCOTOperator tCO = (TraceCOTOperator)obj;
				tCO.decorator = null;
				tCO.resetTreeDecorator();
				if (tCO.traceLegend!=null)
					tCO.traceLegend.repaint();
			}
		}
	}
	/*.................................................................................................................*/
	public void redrawAllTraceOperators() {
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCOTOperator) {
				TraceCOTOperator tCO = (TraceCOTOperator)obj;
				if (tCO.traceLegend!=null)
					tCO.traceLegend.repaint();
				if (tCO.treeDisplay!=null)
					tCO.treeDisplay.pleaseUpdate(false);
			}
		}
	}
	/*.................................................................................................................*/
	public void closeAllTraceOperators() {
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCOTOperator) {
				TraceCOTOperator tCO = (TraceCOTOperator)obj;
				tCO.turnOff();
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
			if (obj instanceof TraceCOTOperator) {
				TraceCOTOperator tCO = (TraceCOTOperator)obj;
				if (tCO.treeDisplay!=null)
					tCO.treeDisplay.pleaseUpdate(false);
				if (tCO.traceLegend!=null)
					tCO.traceLegend.setVisible(showLegend.getValue());
			}
		}
	}
	/*.................................................................................................................*/
	public void toggleModeAllTraceOperators() {
		if (traces==null)
			return;
		Enumeration e = traces.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TraceCOTOperator) {
				TraceCOTOperator tCO = (TraceCOTOperator)obj;
				tCO.setMode(mode);
				tCO.recalculate();
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
		return "Trace Character Over Trees";
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Summarizes at each node reconstructions of the states of a character over a series of trees.  The summary is shown on the current tree; if you want to show it on a consensus of the trees, make sure that the current tree is that consensus.";
	}
	public boolean getShowNodeAbsentSlice() {
		return showNodeAbsentSlice.getValue();
	}
	public void setShowNodeAbsentSlice(boolean showNodeAbsentSlice) {
		this.showNodeAbsentSlice.setValue(showNodeAbsentSlice);
	}
	public boolean getShowEquivocalSlice() {
		return showEquivocalSlice.getValue();
	}
	public void setShowEquivocalSlice(boolean showEquivocalSlice) {
		this.showEquivocalSlice.setValue(showEquivocalSlice);
	}
	public int getNodeToOutput() {
		return nodeToOutput;
	}
	public void setNodeToOutput(int nodeToOutput) {
		this.nodeToOutput = nodeToOutput;
	}

	public void setNodeOutputString(String s) {
		nodeInfo =s;
	}
}

/*======================================================================== */
class TraceCOTOperator extends TreeDisplayDrawnExtra {
	Tree myTree;
	TraceCharOverTrees traceModule;
	CategoricalHistory charStates;
	public TraceLegend traceLegend;
	TreeDecorator decorator;
	MesquiteColorTable colorTable = MesquiteColorTable.DEFAULTCOLORTABLE;
	int currentChar = 0;
	Taxa taxa = null;
	long allStates = 0L;
	static final int UNIQUELY_BEST=0;
	static final int COUNT_EQUIVOCAL=1;
	static final int AVERAGE_FREQUENCIES =2;
	static final int NODE_EQUIVOCAL =  1;
	static final int NODE_ABSENT = 0;
	static final int numExtraFrequencies = 2;
	//	static int maxFrequencyBin = CategoricalState.maxCategoricalState+1;
	int mode;
	int[] treeCounts;
	int[] apparentTrees;
	int[] validTrees;
	int totalTrees =0;
	MesquiteString resultString = new MesquiteString();
	int[] numEquivocal;


	public TraceCOTOperator (TraceCharOverTrees ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		traceModule = ownerModule;
		mode = traceModule.mode;
	}

	public void setMode(int mode){
		this.mode = mode;
	}

	boolean firstTreeHasAllTaxa = true;
	/*.................................................................................................................*/
	public void resetTreeDecorator(int drawnRoot) {
		if (decorator==null)
			decorator = traceModule.displayTask.createTreeDecorator(treeDisplay, this);
		if (decorator!=null) {
			Graphics g = treeDisplay.getGraphics();
			if (g!=null) {
				decorator.drawOnTree(myTree, drawnRoot,  charStates, null, null, g); 
				g.dispose();
			}
		}
	}
	/*.................................................................................................................*/
	public void resetTreeDecorator() {
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();
		resetTreeDecorator(drawnRoot);
	}
	int[] nodeInfoTerminals;
	StringBuffer nodeInfoBuffer = null;
	/*.................................................................................................................*/
	public   void recalculate(){
		int place = 0;
		try {
			totalTrees =0;
			nodeInfoBuffer = null;
			if (traceModule == null || myTree == null || treeDisplay == null || traceModule.suppress)
				return;

			if (MesquiteInteger.isCombinable(traceModule.getNodeToOutput()))
				nodeInfoBuffer = new StringBuffer();
			place = 1;
			if (!traceModule.suppress && (traceModule.historyTask !=null) && traceModule.treeSourceTask != null && myTree !=null){ 
				/* preliminaries .......... */
				place = 2;
				if (traceLegend!=null && traceModule.showLegend != null && traceModule.showLegend.getValue()) {
					traceLegend.onHold();
					int max = traceModule.historyTask.getNumberOfHistories(myTree);
					if (traceModule.currentChar >= max)
						traceModule.currentChar = max-1;
					traceLegend.adjustScroll(CharacterStates.toExternal(traceModule.currentChar), max);

				}
				place = 3;
				taxa = myTree.getTaxa();
				traceModule.currentTaxa = taxa;
				nodeInfoTerminals=null;
				if (MesquiteInteger.isCombinable(traceModule.getNodeToOutput()))
					nodeInfoTerminals = myTree.getTerminalTaxa(traceModule.getNodeToOutput());
				int drawnRoot = 0;
				if (treeDisplay.getTreeDrawing() != null){
					drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();
					resetTreeDecorator(drawnRoot);
				}
				place = 4;

				if (charStates != null)
					charStates.deassignStates();
				/*	if (charStates == null)
 				charStates = new CategoricalHistory(taxa, myTree.getNumNodeSpaces());
 			charStates.adjustSize(myTree);
			charStates.deassignStates();
				 */
				if (resultString != null)
					resultString.setValue("Please wait; calculating.");
				if (traceLegend!=null && traceModule.showLegend != null && traceModule.showLegend.getValue()) {
					traceLegend.refreshSpecsBox();
				}

				place = 5;
				if (treeCounts == null || treeCounts.length != myTree.getNumNodeSpaces())
					treeCounts = new int[myTree.getNumNodeSpaces()];
				IntegerArray.zeroArray(treeCounts);

				if (apparentTrees == null || apparentTrees.length != myTree.getNumNodeSpaces())
					apparentTrees = new int[myTree.getNumNodeSpaces()];
				IntegerArray.zeroArray(apparentTrees);

				if (validTrees == null || validTrees.length != myTree.getNumNodeSpaces())
					validTrees = new int[myTree.getNumNodeSpaces()];
				IntegerArray.zeroArray(validTrees);
				place = 6;

				int numTrees = traceModule.treeSourceTask.getNumberOfTrees(taxa);
				boolean en = !MesquiteInteger.isFinite(numTrees);
				if (en)
					numTrees = traceModule.getNumTrees();
				if (traceModule.numTreesItem != null && en != traceModule.numTreesItem.isEnabled()) {
					traceModule.numTreesItem.setEnabled(en);
					MesquiteTrunk.resetMenuItemEnabling();
				}
				en = mode == TraceCOTOperator.UNIQUELY_BEST;
				if (traceModule.showEquivocalItem != null && en != traceModule.showEquivocalItem.isEnabled()) {
					traceModule.showEquivocalItem.setEnabled(en);
					MesquiteTrunk.resetMenuItemEnabling();
				}
				place = 7;


				if (!myTree.nodeExists(drawnRoot))
					drawnRoot = myTree.getRoot();
				CategoricalHistory tempCharStates = null;
				double[][] frequencies = new double[myTree.getNumNodeSpaces()][CategoricalState.maxCategoricalState+1];
				for (int i = 0; i<frequencies.length; i++)
					DoubleArray.zeroArray(frequencies[i]);
				double[][] extraFrequencies = new double[myTree.getNumNodeSpaces()][numExtraFrequencies];
				for (int i = 0; i<extraFrequencies.length; i++)
					DoubleArray.zeroArray(extraFrequencies[i]);
				if (numEquivocal == null || numEquivocal.length <= myTree.getNumNodeSpaces())
					numEquivocal = new int[myTree.getNumNodeSpaces()];
				IntegerArray.zeroArray(numEquivocal);
				place = 8;

				Tree tempTree = traceModule.treeSourceTask.getTree(taxa, 0);
				if (tempTree == null)
					return;
				place = 9;
				int maxnum = traceModule.historyTask.getNumberOfHistories(tempTree);
				allStates = 0L;
				if (traceModule.currentChar>= maxnum)
					traceModule.currentChar = maxnum-1;
				if (traceModule.currentChar<0)
					traceModule.currentChar = 0;
				place = 10;
				String modeString = "Shown for each state at each node is the number of trees on which the reconstructed state set at the node";
				if (mode == UNIQUELY_BEST)
					modeString += " contains that state as uniquely best according to the reconstruction criteria.";
				else if (mode == COUNT_EQUIVOCAL)
					modeString += " includes that state (it may also include other states as equally or sufficiently optimal according to the reconstruction criteria).";
				else if (mode == AVERAGE_FREQUENCIES)
					modeString = "Shown for each state at each node is the average value (e.g., likelihood) of that state across trees that have that node.";
				place = 11;
				String note =  traceModule.historyTask.getHistoryName(taxa, traceModule.currentChar) + "\n";
				note += traceModule.historyTask.getNameAndParameters();
				if (resultString != null){
					if (MesquiteInteger.isCombinable(numTrees))
						resultString.setValue(note + " over " + numTrees + " trees (" + traceModule.treeSourceTask.getName() + "; " + traceModule.treeSourceTask.getParameters() + ").  " + modeString);
					else
						resultString.setValue(note + " over trees (" + traceModule.treeSourceTask.getName() + "; " + traceModule.treeSourceTask.getParameters() + ").  " + modeString);
				}
				place = 12;
				totalTrees = numTrees;
				IntegerArray.zeroArray(validTrees);
				firstTreeHasAllTaxa = true;
				boolean firstTree=true;

				ProgressIndicator progIndicator = new ProgressIndicator(ownerModule.getProject(),ownerModule.getName(), "Surveying trees for character histories", numTrees, true);
				if (progIndicator!=null){
					progIndicator.setButtonMode(ProgressIndicator.OFFER_CONTINUE);
					progIndicator.setOfferContinueMessageString("Are you sure you want to stop the survey?");
					progIndicator.start();
				}
				place = 13;
				/* checking all trees .......... */
				int it = 0;
				boolean warnNoFrequencies = false;




				for (it = 0; it< numTrees && tempTree != null; it++){
					place = 14;
					if (progIndicator != null) {
						if (progIndicator.isAborted()) {
							progIndicator.goAway();
							traceModule.closeAllTraceOperators();
							return;
						}
						progIndicator.setText("Tree " + it);
						progIndicator.setCurrentValue(it);
					}
					if (MesquiteInteger.isCombinable(numTrees))
						CommandRecord.tick("Examining ancestral state reconstruction on tree " + it + " of " + numTrees);
					else 
						CommandRecord.tick("Examining ancestral state reconstruction on tree " + it);
					place = 15;
					traceModule.historyTask.prepareHistory(tempTree, traceModule.currentChar);
					//				CharacterHistory currentHistory = historyTask.getMapping(0, null, resultString);
					tempCharStates = (CategoricalHistory)traceModule.historyTask.getMapping(0, tempCharStates, null);


					if (tempCharStates == null)
						return;
					else if (tempCharStates.frequenciesExist() || mode != AVERAGE_FREQUENCIES) {  
						//this tree has the relevant information to process it

						place = 16;
						charStates = (CategoricalHistory)tempCharStates.adjustHistorySize(myTree, charStates);
						if (charStates == null)
							return;

						//					this next section captures the states in the terminal taxa. 
						//					if there are multiple states for a terminal taxon, then each is given equal frequencies
						if (firstTree || !firstTreeHasAllTaxa) {
							CharacterDistribution charDist = tempCharStates.getObservedStates();
							if (charDist == null)
								return;
							place = 17;
							CategoricalState cs = new CategoricalState();
							for (int i=0; i<taxa.getNumTaxa(); i++) 
								if (tempTree.taxonInTree(i)) {	//TODO: what if it is in myTree but not in TempTree?
									cs = (CategoricalState)charDist.getCharacterState (cs,i);
									if ((cs.isUnassigned() || cs.isInapplicable()) && charStates!=null && myTree!=null) {
										charStates.setState(myTree.nodeOfTaxonNumber(i), cs.getValue());  // set the charStates value so that the system will know later this was applicable or missing
									} else {
										long s = cs.getValue();
										int numStates = cs.cardinality(s);
										for (int e = 0; e < cs.getMaxPossibleState(); e++)
											if (cs.isElement(e)) {
												frequencies[myTree.nodeOfTaxonNumber(i)][e] = 1.0/numStates;
											}
									}
								} else if (firstTree)
									firstTreeHasAllTaxa = false;
							place = 18;
							firstTree = false;
						}

						allStates |= tempCharStates.getAllStates();
						//survey original nodes to see if node is in tree, and if so record char reconstructed
						visitOriginalNodes(myTree, drawnRoot, tempTree, tempCharStates, frequencies, extraFrequencies, numEquivocal);
						place = 19;

						if (it+1<numTrees)
							tempTree = traceModule.treeSourceTask.getTree(taxa, it+1);
					} 
					else if (!tempCharStates.frequenciesExist() && mode == AVERAGE_FREQUENCIES) {
						place = 20;
						if (!warnNoFrequencies)
							MesquiteMessage.discreetNotifyUser("Ancestral state reconstructor does not calculate frequencies, so the Average Frequencies mode is not available");
						warnNoFrequencies = true;
						if (progIndicator!=null) 
							progIndicator.goAway();
						traceModule.setToPreviousMode();
						return;
					}
				}


				place = 21;
				if (!firstTree)
					visitOriginalNodesAgain(myTree, drawnRoot, tempCharStates, frequencies, extraFrequencies, numEquivocal, numTrees);

				totalTrees = it;
				if (progIndicator!=null) 
					progIndicator.goAway();
				place = 22;



				/*for (int n = 0; n<frequencies.length; n++){
				double sum =0;
				for (int i=0; i<frequencies[n].length; i++)
					sum += frequencies[n][i];
				if (sum!=0.0)
					for (int i=0; i<frequencies[n].length; i++)
						frequencies[n][i] /= sum;

			}
				 */
				captureFrequencies(myTree, drawnRoot, charStates, frequencies, extraFrequencies);

				place = 23;
				ColorDistribution extraColors = new ColorDistribution();
				String[] extraNames =null;
				int numExtraNames = 2;
				boolean showNodeAbsent = traceModule.getShowNodeAbsentSlice();
				boolean showEquivocal = mode==UNIQUELY_BEST && traceModule.getShowEquivocalSlice();
				extraNames = new String[numExtraNames];
				for (int i = 0; i<numExtraNames; i++)
					extraNames[i] ="";
				place = 24;
				if (showEquivocal) {
					extraNames[NODE_EQUIVOCAL] = "Equivocal";
					extraColors.setColor(NODE_EQUIVOCAL,Color.lightGray);
				}
				if (showNodeAbsent){
					extraNames[NODE_ABSENT] = "Node Absent";
					extraColors.setColor(NODE_ABSENT,Color.red);
				}
				charStates.setExtraFrequencyNames(extraNames);
				charStates.setExtraFrequencyColors(extraColors);

				place = 25;
				/*
				 * 		for (int i=0; i<taxa.getNumTaxa(); i++) 
				if (myTree.taxonInTree(i)) {
					int node = myTree.nodeOfTaxonNumber(i);
					charStates.setFrequencies(node, stateset);
					charStates.setState(node, stateset);
				}
				 */


				charStates.prepareColors(myTree, drawnRoot);
				colorTable = charStates.getColorTable(colorTable);
				treeDisplay.pleaseUpdate(false);
				place = 26;
				if (traceLegend!=null && traceModule.showLegend()) {
					traceLegend.setColorTable(colorTable);
					traceLegend.offHold();
					traceLegend.repaint();
				}
			}

			if (nodeInfoBuffer!=null && (StringUtil.notEmpty(nodeInfoBuffer.toString())))
				traceModule.setNodeOutputString(nodeInfoBuffer.toString());
			place = 27;
		}
		catch(NullPointerException e){
			MesquiteMessage.println("NullPointerException in recalculate of TraceCharacterOverTrees " + place);
		}

	}
	TreeReference myTreeRef = null;
	/*.................................................................................................................*/
	boolean toShow = false;
	public   void setTree(Tree tree){
		if (tree != null && myTreeRef != null && tree instanceof MesquiteTree && ((MesquiteTree)tree).sameTreeVersions(myTreeRef, true, false) && !MesquiteThread.isScripting()) {
			myTreeRef = ((MesquiteTree)tree).getTreeReference(myTreeRef);
			return;
		}
		myTree = tree;
		if (tree==null)
			return;
		if (tree instanceof MesquiteTree)
			myTreeRef = ((MesquiteTree)tree).getTreeReference(myTreeRef);
		taxa = tree.getTaxa();
		traceModule.currentTaxa = taxa;
		int numTrees = traceModule.treeSourceTask.getNumberOfTrees(taxa);
		boolean en = !MesquiteInteger.isFinite(numTrees); 
		if (en != traceModule.numTreesItem.isEnabled()) {
			traceModule.numTreesItem.setEnabled(en);
			MesquiteTrunk.resetMenuItemEnabling();
		}
		if (traceLegend==null  && traceModule.showLegend()) {
			traceLegend = new TraceLegend(traceModule, treeDisplay, resultString, "Trace over Trees", Color.red);
			traceLegend.adjustScroll(CharacterStates.toExternal(traceModule.currentChar), traceModule.historyTask.getNumberOfHistories(myTree)); 
			addPanelPlease(traceLegend);
			toShow = true;
		}
		recalculate();
	}
	private long freqToStateSet(double[] f){
		if (f == null)
			return 0L;
		long result = 0L;
		for (int i=0; i<f.length && i<=CategoricalState.maxCategoricalState; i++)
			if (f[i]>0.0)
				result = CategoricalState.addToSet(result, i);
		return result;
	}
	private void captureFrequencies(Tree tree,int node, CategoricalHistory summaryCharStates, double[][] frequencies, double[][] extraFrequencies){
		if (summaryCharStates==null)
			return;
		if (tree.nodeIsInternal(node)){
			summaryCharStates.setFrequencies(node, frequencies[node]);
			summaryCharStates.setExtraFrequencies(node, extraFrequencies[node]);
			summaryCharStates.setState(node, freqToStateSet(frequencies[node]));
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				captureFrequencies(tree, daughter, summaryCharStates, frequencies, extraFrequencies); 

		} else {
			summaryCharStates.setFrequencies(node, frequencies[node]);
			summaryCharStates.setExtraFrequencies(node, extraFrequencies[node]);
			long s = freqToStateSet(frequencies[node]);
			if (s!=0L)  // don't overwrite the value saved for terminal nodes that are inapplicable or missing
				summaryCharStates.setState(node, s);
		}
	}


	private void visitOriginalNodesAgain(Tree tree,int node, CategoricalHistory tempCharStates, double[][] frequencies, double[][] extraFrequencies, int[] numEquivocal, int numTrees){
		if (tree.nodeIsInternal(node)){
			if (mode==COUNT_EQUIVOCAL) {
				for (int i=0; i<=tempCharStates.getMaxState(); i++)
					if (MesquiteDouble.isCombinable(frequencies[node][i]))
						frequencies[node][i] = frequencies[node][i]*validTrees[node]/apparentTrees[node];
			}
			else if (validTrees[node]>0) {
				if (mode == AVERAGE_FREQUENCIES){
					for (int i=0; i<=tempCharStates.getMaxState(); i++)
						if (MesquiteDouble.isCombinable(frequencies[node][i]))
							frequencies[node][i] = 1.0*frequencies[node][i] /validTrees[node];

					//frequencies will now sum up to 1.0.  We then want to compensate extraFrequencies by the same amount so that they are relative the same 
					for (int i=0; i<numExtraFrequencies; i++)
						extraFrequencies[node][i] =  extraFrequencies[node][i] /validTrees[node];  
				}
			}

			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				visitOriginalNodesAgain(tree, daughter, tempCharStates, frequencies, extraFrequencies, numEquivocal, numTrees);
		}
	}

	private void visitOriginalNodes(Tree tree,int node,  Tree otherTree, CategoricalHistory tempCharStates, double[][] frequencies, double[][] extraFrequencies, int[] numEquivocal){
		int[] taxaInClade = tree.getTerminalTaxa(node);
		boolean outputNodeInfo = false;
		if (nodeInfoTerminals!=null)
			outputNodeInfo= IntegerArray.arraysSame(taxaInClade,nodeInfoTerminals);


		if (tree.nodeIsInternal(node)){

			int c = cladeInTree(otherTree, otherTree.getRoot(), taxaInClade);
			if (c>0) {
				treeCounts[node]++; //record that node is in tree
				long stateSet = tempCharStates.getState(c);
				int card = CategoricalState.cardinality(stateSet);
				if (outputNodeInfo) {
					double[] tempFreq = tempCharStates.getFrequencies(c);
					if (tempFreq!=null){
						validTrees[node]++;
						for (int i=0; i<=CategoricalState.maxCategoricalState && i<tempFreq.length;  i++)
							nodeInfoBuffer.append(""+ tempFreq[i]+"\t");
					} else {
						nodeInfoBuffer.append(tempCharStates.toString(c,""));
					}
				}
				if (mode == UNIQUELY_BEST){
					if (card==1)
						frequencies[node][CategoricalState.minimum(stateSet)] += 1;
					else if (traceModule.getShowEquivocalSlice())
						extraFrequencies[node][NODE_EQUIVOCAL] += 1;
				}
				else if (mode == COUNT_EQUIVOCAL){
					if (card!=0){
						validTrees[node]++;
						//double worth = 1.0/CategoricalState.cardinality(stateSet);
						for (int i=0; i<=CategoricalState.maxCategoricalState; i++)
							if (CategoricalState.isElement(stateSet, i)) {
								frequencies[node][i] += 1;
								apparentTrees[node] +=1;
							}
					}
				}
				else if (mode == AVERAGE_FREQUENCIES){
					double[] tempFreq = tempCharStates.getFrequencies(c);
					if (outputNodeInfo)
						nodeInfoBuffer.append("-");
					if (tempFreq!=null){
						validTrees[node]++;
						//grab all of them,no just ones in state set
						for (int i=0; i<=CategoricalState.maxCategoricalState && i<tempFreq.length;  i++)
							frequencies[node][i] += tempFreq[i];
					}
				}
				if (card>1)
					numEquivocal[node]++;

			} else {
				if (outputNodeInfo)
					nodeInfoBuffer.append("-");
				if (traceModule.getShowNodeAbsentSlice()) {
					extraFrequencies[node][NODE_ABSENT] += 1;
				}
			}
			if (outputNodeInfo)
				nodeInfoBuffer.append("\n");

			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				visitOriginalNodes(tree, daughter, otherTree, tempCharStates, frequencies, extraFrequencies, numEquivocal);

		}
	}
	private int cladeInTree(Tree tree, int node,  int[] target){
		if (target == null)
			return -1;
		if (target.length == 1) {
			if (tree.nodeIsTerminal(node) && target[0] == tree.taxonNumberOfNode(node))
				return node;
			else 
				return -1;
		}

		if (tree.nodeIsInternal(node)){
			int[] taxaHere = tree.getTerminalTaxa(node);
			if (IntegerArray.arraysSame(taxaHere, target))
				return node;	
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				int t = (cladeInTree(tree, daughter, target));
				if (t>0)
					return t;
			}
		}
		return -1;
	}
	CharacterState cs = null;
	public void cursorEnterBranch(Tree tree, int N, Graphics g) {
		String nodeReport = "";
		if (tree.nodeIsTerminal(N)) {
			if (traceModule.showLegend() && traceLegend!=null && charStates!=null) {
				cs = charStates.getCharacterState(cs, N);
				nodeReport = "\nTerminal branch\nObserved states are: "+cs.toDisplayString();
				if (((CategoricalState)cs).cardinality() >1) {
					nodeReport += "\n(each state shown in equal proportion) ";
				}
				traceLegend.setMessage(nodeReport);
			}
		}
		else {
			if (treeCounts!=null && treeCounts.length>N)
				nodeReport = "\nNode present in " + treeCounts[N] + " of " + totalTrees + " trees";
			if (traceModule.showLegend() && traceLegend!=null && charStates!=null) {
				cs = charStates.getCharacterState(cs, N);
				if (!cs.isCombinable() && treeCounts[N]>0) {
					if (mode == UNIQUELY_BEST)
						nodeReport = "\nNo states reported for this node.  It may have no uniquely best states on any tree. " + nodeReport;
					else if (mode == COUNT_EQUIVOCAL)
						nodeReport = "\nNo states reported for this node.  " + nodeReport;
					else if (mode == AVERAGE_FREQUENCIES)
						nodeReport = "\nNo states reported for this node  " + nodeReport;
				}
				if (numEquivocal[N]==1)
					nodeReport = "\n" + numEquivocal[N] + " tree with equivocal reconstruction. " + nodeReport;
				else if (numEquivocal[N]>1)
					nodeReport = "\n" + numEquivocal[N] + " trees with equivocal reconstructions. " + nodeReport;
				traceLegend.setMessage(charStates.toString(N, "\n") + nodeReport);
			}
		}
	}
	public void cursorExitBranch(Tree tree, int N, Graphics g) {
		if (traceModule.showLegend() && traceLegend!=null)
			traceLegend.setMessage("");
	}
	public void cursorEnterTaxon(Tree tree, int M, Graphics g) {
		if (traceModule.showLegend() && traceLegend!=null && charStates!=null && charStates.getObservedStates()!=null)
			traceLegend.setMessage("Reconstruction counts not made for observed states in terminal taxa");
	}
	public void cursorExitTaxon(Tree tree, int M, Graphics g) {
		if (traceModule.showLegend() && traceLegend!=null)
			traceLegend.setMessage("");
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		if ((traceModule.historyTask!=null) && (charStates!=null) && (decorator!=null)) {
			decorator.useColorTable(colorTable);
			decorator.drawOnTree(tree, drawnRoot,  charStates, null, null, g); 
			if (traceModule.showLegend() && traceLegend !=null) {
				charStates.prepareColors(tree, drawnRoot);
				traceLegend.setStates(charStates);
				traceLegend.adjustLocation();
				if (toShow || !traceLegend.isVisible())
					traceLegend.setVisible(true);
				toShow=false;
			}
		}
	}

	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		if (tree.nodeIsTerminal(node))
			return null;
		String nodeReport = "";
		if (treeCounts!=null && treeCounts.length>node)
			nodeReport = "Node in " + treeCounts[node] + " trees.";
		if (charStates!=null) {
			cs = charStates.getCharacterState(cs, node);
			if (!cs.isCombinable() && treeCounts[node]>0) {
				if (mode == UNIQUELY_BEST)
					nodeReport += "No states reported for this node.  This may be due to its having no uniquely best states on any tree. ";
				else if (mode == COUNT_EQUIVOCAL || mode==AVERAGE_FREQUENCIES)
					nodeReport += "No states reported for this node.  ";
			}
			else if (mode==AVERAGE_FREQUENCIES)
				nodeReport += "  States and relative average probabilities of each across trees: " + charStates.toString(node, "; ");
			else
				nodeReport += "  Optimal states and count of trees with each: " + charStates.toString(node, "; ");
		}
		return nodeReport;
	}
	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		return resultString.getValue();
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	public void turnOff(){
		if (treeDisplay!=null && traceLegend !=null)
			treeDisplay.removePanelPlease(traceLegend);
		super.turnOff();

	}
}

interface TraceModule extends Commandable {
	public int getInitialOffsetX();
	public int getInitialOffsetY();
	public boolean showLegend();
}
/* ======================================================================== */
class TraceLegend extends TreeDisplayLegend {
	private TraceModule traceModule;
	public MiniScroll characterScroll = null;
	private MesquiteString resultString;
	private String[] stateNames;
	private Color[] legendColors;
	private static final int defaultLegendWidth=142;
	private static final int defaultLegendHeight=120;
	private int numBoxes=0;
	private int oldNumChars = 0;
	private int oldNumBoxes=0;
	private int oldCurrentChar = -1;
	private boolean resizingLegend=false;
	private TCMPanel messageBox;
	private TextArea specsBox;
	private boolean holding = false;
	final int scrollAreaHeight = 41;
	private int messageHeight = 22;
	final int defaultSpecsHeight = (14 + MesquiteModule.textEdgeCompensationHeight) * 3;
	private int specsHeight = defaultSpecsHeight;
	private int e = 4;
	private String title;
	private Color titleColor;

	public TraceLegend(TraceCharOverTrees traceModule, TreeDisplay treeDisplay, MesquiteString resultString, String title, Color titleColor) {
		super(treeDisplay,defaultLegendWidth, defaultLegendHeight);
		if (MesquiteInteger.isCombinable(traceModule.initialLegendWidth))
			legendWidth = traceModule.initialLegendWidth;
		if (MesquiteInteger.isCombinable(traceModule.initialLegendHeight))
			legendHeight = traceModule.initialLegendHeight;
		setVisible(false);
		this.title = title;
		this.titleColor = titleColor;
		this.resultString = resultString;
		legendWidth=defaultLegendWidth;
		legendHeight=defaultLegendHeight;
		setOffsetX(traceModule.getInitialOffsetX());
		setOffsetY(traceModule.getInitialOffsetY());
		this.traceModule = traceModule;
		//setBackground(ColorDistribution.light);
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
		if (traceModule.showLegend())// && traceModule.showReconstruct.getValue())
			specsBox.setVisible(false);
		specsBox.setBounds(1,scrollAreaHeight+2,legendWidth-2, specsHeight);
		add(specsBox);

		messageBox = new TCMPanel();
		messageBox.setBounds(2,legendHeight-messageHeight-8,legendWidth-6, messageHeight);
		messageBox.setText("\n");
		//messageBox.setColor(Color.pink);
		//messageBox.setBackground(Color.pink);
		add(messageBox);
	}

	public void adjustScroll(int currentCharExternal, int numChars) {
		if (characterScroll == null) {
			characterScroll = new MiniScroll( MesquiteModule.makeCommand("setCharacter",  (Commandable)traceModule), false, currentCharExternal, 1,  numChars, "character");
			add(characterScroll);
			characterScroll.setLocation(2,18);//18);
			characterScroll.setColor(Color.blue);
			repaint();
			oldNumChars = numChars;
			oldCurrentChar = currentCharExternal;
		}
		else {
			if (oldNumChars != numChars) {
				characterScroll.setMaximumValue(numChars);
				oldNumChars = numChars;
			}
			if (oldCurrentChar != currentCharExternal) {
				characterScroll.setCurrentValue(currentCharExternal);
				oldCurrentChar = currentCharExternal;
			}
		}
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		if (characterScroll !=null)
			characterScroll.setVisible(b);
		if (messageBox!=null)
			messageBox.setVisible(b);
		if (specsBox!=null)// && traceModule.showReconstruct.getValue())
			specsBox.setVisible(b);
	}

	public void refreshSpecsBox(){
		specsBox.setText(resultString.getValue()); 
	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (!holding) {
			g.setColor(Color.black);
			if (numBoxes!=0) {
				int boxCount =0;
				for (int ibox=0; ibox<numBoxes; ibox++) {
					if (!StringUtil.blank(stateNames[ibox])){
						g.setColor(legendColors[ibox]);
						g.fillRect(4, boxCount*16+scrollAreaHeight + specsHeight+ e, 20, 12);
						g.setColor(Color.black);
						g.drawRect(4, boxCount*16+scrollAreaHeight + specsHeight + e, 20, 12);
						if (stateNames[ibox]!=null)
							g.drawString(stateNames[ibox], 28, boxCount*16 + specsHeight+scrollAreaHeight + 12 + e);
						boxCount++;
					}
				}
			}
			g.setColor(Color.cyan);
			g.drawRect(0, 0, legendWidth-1, legendHeight-1);
			g.fillRect(legendWidth-6, legendHeight-6, 6, 6);
			g.drawLine(0, scrollAreaHeight, legendWidth-1, scrollAreaHeight);

			g.setColor(titleColor);
			g.drawString(title, 4, 14);
			g.setColor(Color.black);
			if (resultString.getValue()!=null && !resultString.getValue().equals(specsBox.getText()))
				specsBox.setText(resultString.getValue()); 

			if (specsBox.getBackground() != getBackground())
				specsBox.setBackground(getBackground());
		}
		MesquiteWindow.uncheckDoomed(this);
	}

	public void printAll(Graphics g) {
		g.setColor(Color.black);
		g.drawString("Trace Character", 4, 14);
		String info = resultString.getValue();
		StringInABox sib = new StringInABox(info, g.getFont(), legendWidth);
		sib.draw(g, 4, 16);
		int QspecsHeight = sib.getHeight();
		int lastBox = QspecsHeight + 20;
		//int QspecsHeight = 0;
		//int lastBox = scrollAreaHeight + QspecsHeight + 20 + 12;
		if (numBoxes!=0) {
			for (int ibox=0; ibox<numBoxes; ibox++) {
				g.setColor(legendColors[ibox]);
				g.fillRect(4, ibox*16+ QspecsHeight + 20, 20, 12);
				g.setColor(Color.black);
				g.drawRect(4, ibox*16+ QspecsHeight + 20, 20, 12);
				g.drawString(stateNames[ibox], 28, ibox*16 + QspecsHeight + 32);
				lastBox =ibox*16 + QspecsHeight + 20 + 12;
			}
		}
	}
	MesquiteColorTable colorTable;
	public void setColorTable(MesquiteColorTable colorTable){
		this.colorTable = colorTable;
	}
	public void setStates(CharacterHistory statesAtNodes){
		numBoxes = statesAtNodes.getLegendStates(legendColors, stateNames, null, colorTable);

		//		if (((CategoricalHistory)statesAtNodes).extraFrequenciesExist())
		//			numBoxes += (((CategoricalHistory)statesAtNodes).getExtraNumFreqCategories());
		if (numBoxes!=oldNumBoxes) {
			reviseBounds();
			oldNumBoxes=numBoxes;
		}
		repaint();
	}

	public void legendResized(int widthChange, int heightChange){
		if ((specsHeight + heightChange)>= defaultSpecsHeight)
			specsHeight += heightChange;
		else
			specsHeight  = defaultSpecsHeight;
		checkComponentSizes();
	}
	public void setLegendWidth(int w){
		legendWidth = w;
	}
	public void setLegendHeight(int h){
		legendHeight = h;
	}
	public void reviseBounds(){
		checkComponentSizes();
		Point where = getLocation();
		setBounds(where.x,where.y,legendWidth, legendHeight);
	}
	public void checkComponentSizes(){
		specsBox.setBounds(1,scrollAreaHeight+2,legendWidth-2, specsHeight);
		specsBox.setVisible(true);
		messageHeight = messageBox.getHeightNeeded();
		if (messageHeight<20)
			messageHeight = 20;
		legendHeight=numBoxes*16+scrollAreaHeight + specsHeight + e + messageHeight + 4;
		messageBox.setBounds(2,legendHeight-messageHeight-7,legendWidth-6, messageHeight);
	}

	public void setMessage(String s) {
		if (s==null || s.equals("")) {
			//messageBox.setBackground(ColorDistribution.light);
			messageBox.setText("\n");
			reviseBounds();
		}
		else {
			//messageBox.setBackground(Color.white);
			messageBox.setText(s);
			reviseBounds();
		}
	}
	public void onHold() {
		holding = true;
	}

	public void offHold() {
		holding = false;
	}
}

class TCMPanel extends Panel {
	String message = "";
	StringInABox box;
	public TCMPanel(){
		super();
		box =  new StringInABox("", null, getBounds().width);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
		box.setWidth(w);
	}
	public void setSize(int w, int h){
		super.setSize(w,h);
		box.setWidth(w);
	}
	public int getHeightNeeded(){
		return box.getHeight();
	}
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		box.setFont(g.getFont());
		box.draw(g,0, 0);
		MesquiteWindow.uncheckDoomed(this);
	}

	public void setText(String s) {
		message = s;
		box.setString(s);
		repaint();
	}
}



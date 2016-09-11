package mesquite.ancstates.SummarizeChanges;

/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.ancstates.lib.*;

/* 
 * 
 * doesn't listen properly to changes
 * 
 * */
/* this is a special module designed to be used in one of two ways, to summarize
 * changes over trees without reference to a tree with selected branches, the other wiith reference to such a tree.
 * If setup(taxa, boolean) is called, it is assumed the latter is in effect. (branchesMode = false)
 * if setTree(tree) is called, it is assumed the former is in effect   (branchesMode = true)*/

/*======================================================================== */
public class SummarizeChanges extends ChgSummarizerMultTrees {

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharHistorySource.class, getName() + " needs a source of character histories.",
				"The source of a traced history can be chosen using the Character History Source submenu");
	}

	static int maxNumMappings = 50;
	int maxChangesRecorded = 10;
	static boolean queryLimits = true;
	MesquiteBoolean saveDetailsToFile = new MesquiteBoolean(false);

	CharHistorySource historyTask;
	MesquiteString treeSourceName;
	MesquiteCommand cstC;
	TreeSource treeSourceTask;
	MesquiteMenuItemSpec numTreesItem = null;
	private int numTrees = 100;
	//not yet ready double minPercent = 0.0;
	boolean[][] allowedChanges =null;
	int maxState = CategoricalState.maxCategoricalState;


	StringArray modes;
	MesquiteString modeName;

	private boolean numTreesSet = false;
	boolean suppress = false;
	int currentChar=0;
	MesquiteCommand htC;
	MesquiteString historyTaskName;
	Taxa currentTaxa;
	MesquiteTextWindow textWindow;
	boolean branchesMode = false;
	boolean poppedOut;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (MesquiteThread.isScripting())
			suppress = true;
		historyTask = (CharHistorySource)hireCompatibleEmployee(CharHistorySource.class, CategoricalState.class,"Source of character history (for Summarize_Changes)");

		if (historyTask == null) {
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		}
		makeMenu("Summarize_Changes");
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
		addMenuItem( "Choose Character History", makeCommand("chooseCharacter",  this));
		addMenuItem( "Allowed Changes...", makeCommand("allowedChanges",  this));
		addMenuItem( "Maximum Number of Changes Recorded...", makeCommand("setMaxChangesRecorded",  this));
		addMenuItem( "Maximum Number of Mappings...", makeCommand("setMaxNumMappings",  this));


		treeSourceTask = (TreeSource)hireEmployee(TreeSource.class,  "Source of trees for comparison by for Summarize Changes");
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
		addCheckMenuItem(null, "Save Details To File...", makeCommand("toggleSaveDetails", this), saveDetailsToFile);

		addMenuItem( "Close Summarize Changes", makeCommand("closeSummarizeChanges",  this));
		addMenuSeparator();


		allowedChanges = new boolean[CategoricalState.maxCategoricalState][CategoricalState.maxCategoricalState];
		zeroAllowedChanges();

		loadPreferences();

		resetContainingMenuBar();

		return true;
	}
	//this is called if without tree window to show changes over whole trees
	public void setup(Taxa taxa, boolean poppedOut){
		currentTaxa = taxa;
		this.poppedOut = poppedOut;
		branchesMode = false;

		setup();
		if (!MesquiteThread.isScripting() && textWindow == null) {
			textWindow= new MesquiteTextWindow( this, "State Changes Over Trees", true, true, false);
			setModuleWindow(textWindow);
			textWindow.setPopAsTile(true);
			if (poppedOut)
				textWindow.popOut(true);
			else textWindow.setVisible(true);
			resetContainingMenuBar();
			resetAllWindowsMenus();
			recalculate();
		}
		if (textWindow != null)
			textWindow.setTitle("State Changes Over Trees");
	}
	/*.................................................................................................................*/
	public   void setup() {
		checkNumTreesFromSource();
		int numTrees2 = treeSourceTask.getNumberOfTrees(currentTaxa);
		numTreesItem.setEnabled(!MesquiteInteger.isFinite(numTrees2));
		MesquiteTrunk.resetMenuItemEnabling();
	}
	/*.................................................................................................................*/
	public void checkNumTreesFromSource() {
		if (treeSourceTask==null || currentTaxa==null)
			return;
		int nt = treeSourceTask.getNumberOfTrees(currentTaxa);
		if (MesquiteInteger.isFinite(nt)) {
			numTrees = nt;
			numTreesSet = true;
		}
	}
	/**/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		if (whichWindow != textWindow)
			return;
		whichWindow.hide();
		iQuit();
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == treeSourceTask || employee == historyTask)  
			iQuit();
	}

	/*.................................................................................................................*/
	public void zeroAllowedChanges(){
		for (int i=0; i<allowedChanges.length; i++)
			for (int j=0; j<allowedChanges[i].length; j++)
				allowedChanges[i][j]=true;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("maxNumMappings".equalsIgnoreCase(tag))
			maxNumMappings = MesquiteInteger.fromString(content);
		if ("maxChangesRecorded".equalsIgnoreCase(tag))
			maxChangesRecorded = MesquiteInteger.fromString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "maxNumMappings", maxNumMappings);  
		StringUtil.appendXMLTag(buffer, 2, "maxChangesRecorded", maxChangesRecorded);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("suppress ");
		temp.addLine("setBranchMode " + branchesMode);
		temp.addLine("setHistorySource ",historyTask);
		temp.addLine("setCharacter " + CharacterStates.toExternal(currentChar));
		temp.addLine("setMaxNumMappings " + maxNumMappings);
		temp.addLine("setMaxChangesRecorded " + maxChangesRecorded);
		temp.addLine( "setTreeSource " , treeSourceTask);
		temp.addLine("setNumTrees " + numTrees);
		temp.addLine("makeWindow");
		temp.addLine("tell It");
		Snapshot fromWindow = textWindow.getSnapshot(file);
		temp.incorporate(fromWindow, true);
		temp.addLine("endTell");
		temp.addLine("desuppress ");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/

	public boolean queryAllowedChanges(){
		if (MesquiteThread.isScripting())
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Allowed Changes",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Allowed Changes");

		int numStates = maxState+1;
		String[] labels = new String[numStates];
		for (int i=0; i<numStates; i++)
			labels[i]=""+i;
		Checkbox[][] changesAllowed = dialog.addCheckboxMatrix(numStates, numStates, labels, labels);

		dialog.addHorizontalLine(1);


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			for (int i=0; i<numStates && i<allowedChanges.length; i++)
				for (int j=0; j<numStates && j<allowedChanges[i].length; j++)
					allowedChanges[j][i]=changesAllowed[i][j].getState();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0) ;
	}
	/*.................................................................................................................*/
	public boolean saveDetails(){
		return saveDetailsToFile.getValue() && !MesquiteThread.isScripting();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Makes but doesn't show the window", null, commandName, "makeWindow")) {
			if (textWindow==null) {
				textWindow= new MesquiteTextWindow( this, "Summary of Changes", true, true, false);
				if (branchesMode)
					textWindow.setTitle("State Changes on Branches");
				else
					textWindow.setTitle("State Changes Over Trees");
				setModuleWindow(textWindow);
				resetContainingMenuBar();
				resetAllWindowsMenus();
			}
			return textWindow;
		}
		else if (checker.compare(this.getClass(), "Sets branches mode", null, commandName, "setBranchMode")) {
			String t = parser.getFirstToken(arguments);
			branchesMode = ("true".equalsIgnoreCase(t));
		}
		else if (checker.compare(this.getClass(), "Goes to next character history", null, commandName, "nextCharacter")) {
			if (currentChar>=historyTask.getNumberOfHistories(currentTaxa)-1)
				currentChar=0;
			else
				currentChar++;
			recalculate();

		}
		else if (checker.compare(this.getClass(), "Goes to previous character history", null, commandName, "previousCharacter")) {
			if (currentChar<=0)
				currentChar=historyTask.getNumberOfHistories(currentTaxa)-1;
			else
				currentChar--;
			recalculate();
		}
		else if (checker.compare(this.getClass(), "Goes to previous character history", null, commandName, "allowedChanges")) {
			if (queryAllowedChanges())
				recalculate();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to save all the calculation details to a file", "[on; off]", commandName, "toggleSaveDetails")) {
			saveDetailsToFile.toggleValue(parser.getFirstToken(arguments));
			if (saveDetails())
				recalculate();
		}
		else if (checker.compare(this.getClass(), "Sets the maximum number of mappings", null, commandName, "setMaxNumMappings")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(getModuleWindow(), "Maximum number of mappings to sample", "Maximum number of mappings to sample for the character on each tree",maxNumMappings, 1, Integer.MAX_VALUE);
			if (newNum>0 && MesquiteInteger.isCombinable(newNum) && maxNumMappings!=newNum) {
				maxNumMappings=newNum;
				storePreferences();
				recalculate();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the maximum number of changes recorded", null, commandName, "setMaxChangesRecorded")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(getModuleWindow(), "Maximum number of changes to record", "Maximum number of changes to record for the character on each tree",maxChangesRecorded, 1, Integer.MAX_VALUE);
			if (newNum>0 && MesquiteInteger.isCombinable(newNum) && maxChangesRecorded!=newNum) {
				maxChangesRecorded=newNum;
				storePreferences();
				recalculate();
			}
		}

		else if (checker.compare(this.getClass(), "Queries user about which character history to use", null, commandName, "chooseCharacter")) {
			int ic=historyTask.queryUserChoose(currentTaxa, " to trace ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentChar = ic;
				recalculate();
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
				recalculate();
			}
		}
		else /**/
			if (checker.compare(this.getClass(), "Sets the number of trees", "[number]", commandName, "setNumTrees")) {
				int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
				if (!MesquiteInteger.isCombinable(newNum))
					newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set Number of Trees", "Number of Trees (for Summarize Changes over Trees):", numTrees, 0, MesquiteInteger.infinite);
				if (newNum>0  && newNum!=numTrees) {
					numTrees = newNum;
					numTreesSet = true;
					if (!suppress){
					}
				}
				else if (numTrees == newNum)
					numTreesSet = true;
			}
			else /**/
				if (checker.compare(this.getClass(), "Sets the source of trees for comparison", "[name of module]", commandName, "setTreeSource")) {
					TreeSource temp =  (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees for comparison by for Summarize Changes over Trees", treeSourceTask);
					if (temp!=null) {
						treeSourceTask=  temp;
						treeSourceTask.setHiringCommand(cstC);
						treeSourceName.setValue(treeSourceTask.getName());
						numTreesSet = false;
						checkNumTreesFromSource();
						if (!suppress){
							recalculate();
							parametersChanged();
						}
					}	
					return temp;
				}
				else if (checker.compare(this.getClass(), "Sets module supplying character histories", "[name of module]", commandName, "setHistorySource")) {
					CharHistorySource temp =  (CharHistorySource)replaceEmployee(CharHistorySource.class, arguments, "Source of character histories", historyTask);
					if (temp!=null) {
						historyTask=  temp;
						historyTask.setHiringCommand(htC);
						historyTaskName.setValue(historyTask.getName());
						currentChar=0;
						recalculate();
						if (!suppress)
							recalculate();
					}
					return historyTask;
				}
				else if (checker.compare(this.getClass(), "Suppresses calculations", null, commandName, "suppress")) {
					suppress = true;
				}
				else if (checker.compare(this.getClass(), "Desuppresses calculations", null, commandName, "desuppress")) {
					suppress = false;
					if (textWindow != null)
						textWindow.setVisible(true);

					recalculate();
				}
				else if (checker.compare(this.getClass(), "Returns ancestral states calculating module", null, commandName, "getAncStSource")) {
					return historyTask;
				}
				else if (checker.compare(this.getClass(), "Turns off Summarize Changes over Trees", null, commandName, "closeSummarizeChanges")) {
					iQuit();
					resetContainingMenuBar();
				}
				else
					return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public int getNumTrees(){
		if (MesquiteThread.isScripting() || numTreesSet)
			return numTrees;
		int newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set Number of Trees", "Number of Trees (for Summarize Changes over Trees):", numTrees, 0, MesquiteInteger.infinite);
		if (newNum>0) {
			numTrees = newNum;
			numTreesSet = true;
		}
		return numTrees;
	}
	public void setNumTrees(int numTrees){
		if (numTrees>0 && MesquiteInteger.isCombinable(numTrees)) {
			this.numTrees = numTrees;
			numTreesSet = true;
		}
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj instanceof Tree) {
			if (code!=MesquiteListener.SELECTION_CHANGED  || getSensitiveToBranchSelection()) {
				recalculate();
			}
		}
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED)
			recalculate();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "State Change Summarizer (over trees)";
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
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}


	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Summarizes reconstructions of state changes of a character over a series of trees.";
	}
	public int getMaxState() {
		return maxState;
	}
	public void setMaxState(int maxState, CategStateChanges stateChanges) {
		this.maxState = maxState;
		stateChanges.adjustNumStates(maxState+1);
	}
	public void setAcceptableChanges(CategStateChanges stateChanges) {
		if (stateChanges!=null)
			for (int i=0; i<=maxState && i<allowedChanges.length; i++)
				for (int j=0; j<=maxState && j<allowedChanges[i].length; j++)
					stateChanges.setAcceptableChange(i, j,allowedChanges[i][j]);
	}

	/**********&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&*************/
	Tree myTree;
	int totalTrees =0;
	MesquiteString resultString = new MesquiteString();
	String currentText = "";

	boolean firstTreeHasAllTaxa = true;

	/*.................................................................................................................*/

	public void addFullDetailsHeader (int numStates, StringBuffer sb) {
		if (sb==null)
			return;
		sb.append("tree\t");

		for (int i=0; i<numStates; i++)
			for (int j=0; j<numStates; j++)
				if (i!=j)
					sb.append(""+i+"->"+j+"\t");
		sb.append("\n");
	}
	/*.................................................................................................................*/
	public   void recalculate(){
		totalTrees =0;
		if (!suppress && (historyTask !=null)){ 
			/* preliminaries .......... */

			if (branchesMode && (myTree == null || !myTree.anySelected())) {
				String s = "No changes are summarized because no branches are selected in current tree in Tree Window. Make sure only a single branch is selected, not an entire clade; use the Select Branch tool.";
				resultString.setValue(s);
				textWindow.setText(s);
				return;
			}

			/*if (myTree != null && myTree instanceof MesquiteTree) DAVID: I commented this out because was already done in setTree, and hence there would have been 2 additions & thus memory leaks
				((MesquiteTree)myTree).addListener(this);
			 */

			//		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();

			resultString.setValue("Please wait; calculating.");

			int numTrees = treeSourceTask.getNumberOfTrees(currentTaxa);
			boolean en = !MesquiteInteger.isFinite(numTrees);
			if (en)
				numTrees = getNumTrees();
			else
				setNumTrees(numTrees);
			if (en != numTreesItem.isEnabled()) {
				numTreesItem.setEnabled(en);
				MesquiteTrunk.resetMenuItemEnabling();
			}


			CategoricalHistory tempCharStates = null;

			MesquiteTree tempTree = (MesquiteTree)treeSourceTask.getTree(currentTaxa, 0);
			int maxnum = historyTask.getNumberOfHistories(tempTree);

			if (currentChar>= maxnum)
				currentChar = maxnum-1;
			if (currentChar<0)
				currentChar = 0;

			StringBuffer fullDetails= new StringBuffer();


			String modeString = "Shown for each state at each node is the number of trees on which the reconstructed state set at the node";
			if (MesquiteInteger.isCombinable(numTrees))
				resultString.setValue("Character " + CharacterStates.toExternal(currentChar) + "\n" + historyTask.getNameAndParameters() + " over " + numTrees + " trees (" + treeSourceTask.getName() + "; " + treeSourceTask.getParameters() + ").  " + modeString);
			else
				resultString.setValue("Character " + CharacterStates.toExternal(currentChar) + "\n" + historyTask.getNameAndParameters() + " over trees (" + treeSourceTask.getName() + "; " + treeSourceTask.getParameters() + ").  " + modeString);
			totalTrees = numTrees;
			//			IntegerArray.zeroArray(validTrees);
			firstTreeHasAllTaxa = true;
			boolean firstTree=true;

			ProgressIndicator progIndicator = new ProgressIndicator(getProject(),getName(), "Surveying trees for character histories", numTrees, true);
			if (progIndicator!=null){
				progIndicator.setButtonMode(ProgressIndicator.OFFER_CONTINUE);
				progIndicator.setOfferContinueMessageString("Are you sure you want to stop the survey?");
				progIndicator.start();
			}
			/* checking all trees .......... */
			int it = 0;
			int processedTrees=0;
			boolean oneValidMapping = false;

			CategStateChanges stateChanges=null;
			MesquiteInteger newMaxMappings = new MesquiteInteger();
			MesquiteInteger numMappingsSampled = new MesquiteInteger();
			int minSampled = Integer.MAX_VALUE;
			int maxSampled = 0;

			if (!branchesMode || (myTree != null && myTree.numberSelectedInClade(myTree.getRoot())<=1)) {
				for (it = 0; it< numTrees && tempTree != null; it++){
					if (progIndicator != null) {
						if (progIndicator.isAborted()) {
							progIndicator.goAway();
							return;
						}
						progIndicator.setText("Tree " + it);
						progIndicator.setCurrentValue(it);
					}
					if (MesquiteInteger.isCombinable(numTrees))
						CommandRecord.tick("Examining ancestral state reconstruction on tree " + it + " of " + numTrees);
					else 
						CommandRecord.tick("Examining ancestral state reconstruction on tree " + it);
					historyTask.prepareForMappings(true);
					historyTask.prepareHistory(tempTree, currentChar);

					CharacterHistory ch = historyTask.getMapping(0, tempCharStates, null);
					if (!(ch instanceof CategoricalHistory)){
						discreetAlert("To summarize changes, they must be of categorical characters.  Continuous and meristic characters cannot be summarized.  We don't know how you managed to get to this point, but the calculation will fail.");
						resultString.setValue("Calculation not done; character type incompatible");
						return;
					}
					tempCharStates = (CategoricalHistory)ch;
					if (tempCharStates!=null) {
						oneValidMapping = true;
						if (stateChanges==null) {
							stateChanges = new CategStateChanges(tempCharStates.getMaxState()+1, maxChangesRecorded+1);
							if (saveDetails())
								addFullDetailsHeader(tempCharStates.getMaxState()+1,fullDetails);
						}
						setMaxState(tempCharStates.getMaxState(), stateChanges);
						setAcceptableChanges(stateChanges);

						boolean processTree = true;
						int node = tempTree.getRoot();
						if (branchesMode && myTree.numberSelectedInClade(myTree.getRoot())==1) {
							node = myTree.getFirstSelected(myTree.getRoot());
							Bits terminals = myTree.getTerminalTaxaAsBits(node);
							processTree = tempTree.isClade(terminals);
							if (processTree)
								node = tempTree.mrca(terminals);
						}

						if (processTree){
							processedTrees++;
							stateChanges.addOneHistory(tempTree, historyTask, currentChar,node, numMappingsSampled, maxNumMappings, newMaxMappings, queryLimits, fullDetails, ""+(it+1));
							if (newMaxMappings.isCombinable()) {
								maxNumMappings=newMaxMappings.getValue();
								queryLimits = false;
								storePreferences();
							}
							if (numMappingsSampled.isCombinable()) {
								if (numMappingsSampled.getValue()<minSampled)
									minSampled = numMappingsSampled.getValue();
								if (numMappingsSampled.getValue()>maxSampled)
									maxSampled = numMappingsSampled.getValue();
							}


						} 
					}
					if (it+1<numTrees)
						tempTree = (MesquiteTree)treeSourceTask.getTree(currentTaxa, it+1);
					if (saveDetails())
						fullDetails.append("\n");

				}
			}

			logln(" " + processedTrees + " trees processed.");

			totalTrees = it;
			if (progIndicator!=null) 
				progIndicator.goAway();

			if (stateChanges!=null && processedTrees>0) {
				stateChanges.cleanUp();
				currentText = stateChanges.toVerboseString(); 
				String leadText;
				leadText = "\nSummarizing changes over trees in character " + (currentChar+1);
				if (branchesMode){
					leadText += " in the clade whose ancestor is selected in the tree in Tree Window.  Trees are considered only if they also have this " +
							"clade, and changes are counted only within the clade.";
				}
				leadText += "\nSource of Trees: " + treeSourceTask.getName() + "\n";
				leadText += "Further details about Source of Trees: " + treeSourceTask.getParameters() + "\n";
				leadText += "Number of Trees: " + processedTrees + "\n";
				if (minSampled==maxSampled)
					leadText += "Number of mappings sampled per tree: " + minSampled +"\n";
				else
					leadText += "Number of mappings sampled per tree: " + minSampled + "-"+maxSampled+"\n";
				currentText = leadText+"\n"+ currentText;
				textWindow.setText(currentText);
				if (saveDetails())
					MesquiteFile.putFileContentsQuery("Save file with full details", fullDetails.toString(), true);
			}
			else
				if (processedTrees<=0) {
					if (!oneValidMapping)
						textWindow.setText("Sorry, changes not calculated; character mappings could not be sampled for any of the trees examined.");
					else if (branchesMode)
						textWindow.setText("Sorry, changes not calculated; none of the trees examined have the selected branch.");
					else
						textWindow.setText("Sorry, changes not calculated.  No trees were processed.");
				}
				else if (branchesMode)
					textWindow.setText("Sorry, changes not calculated. Make sure only a single branch is selected, not an entire clade.  Use the arrow tool, and touch on the branch.");
				else
					textWindow.setText("Sorry, changes not calculated.");


		}
	}
	TreeReference myTreeRef = null;
	/*.................................................................................................................*/
	//	this is called if used by tree window assistant to show changes on branches
	public   void setTree(Tree tree){
		if (tree == null)
			return;
		currentTaxa = tree.getTaxa();
		setup();
		this.poppedOut = true;
		branchesMode = true;
		if (!MesquiteThread.isScripting() && textWindow == null) {
			textWindow= new MesquiteTextWindow( this, "State Changes on Branches", true, true, false);
			setModuleWindow(textWindow);
			textWindow.setPopAsTile(true);
			if (poppedOut)
				textWindow.popOut(true);
			else textWindow.setVisible(true);
			resetContainingMenuBar();
			resetAllWindowsMenus();
		}
		if (textWindow != null)
			textWindow.setTitle("State Changes on Branches");

		if (tree != null && myTreeRef != null && tree instanceof MesquiteTree && ((MesquiteTree)tree).sameTreeVersions(myTreeRef, true, false) && !MesquiteThread.isScripting()) {
			myTreeRef = ((MesquiteTree)tree).getTreeReference(myTreeRef);
			return;
		}
		if (myTree!=null && myTree instanceof MesquiteTree) {
			((MesquiteTree)tree).removeListener(this);
		}
		myTree = tree;
		if (tree==null)
			return;
		if (tree instanceof MesquiteTree) {
			myTreeRef = ((MesquiteTree)tree).getTreeReference(myTreeRef);
			((MesquiteTree)tree).addListener(this);
		}

		currentTaxa = tree.getTaxa();
		int numTrees = treeSourceTask.getNumberOfTrees(currentTaxa);
		boolean en = !MesquiteInteger.isFinite(numTrees); 
		if (!en)
			setNumTrees(numTrees);
		if (en != numTreesItem.isEnabled()) {
			numTreesItem.setEnabled(en);
			MesquiteTrunk.resetMenuItemEnabling();
		}
		recalculate();
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
}


/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.duties;

import java.awt.Checkbox;

import mesquite.lib.CommandChecker;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.OutputTextListener;
import mesquite.lib.Puppeteer;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaSelectionSet;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.trees.lib.SimpleTreeWindow;


/* ======================================================================== */
/**Supplies trees (compare to OneTreeSource), for instance from a file or simulated.  Most modules
are subclasses of the subclass TreeSource*/

public abstract class TreeInferer extends TreeBlockFiller {
	protected boolean userAborted=false;
	protected boolean userCancelled = false;
	Listened listened;
	TWindowMaker tWindowMaker;
	 MesquiteBoolean autoSaveFile = new MesquiteBoolean(false);
	 
	 boolean alwaysPrepareForAnyMatrices=false;

	 
	public Class getDutyClass() {
		return TreeInferer.class;
	}
	public String getDutyName() {
		return "Tree Inferer";
	}
	public String[] getDefaultModule() {
		return null;
	}
	
	public String getLogText() {
		return "";
	}
	
	
	/*.................................................................................................................*/
	public boolean getAlwaysPrepareForAnyMatrices() {
		return alwaysPrepareForAnyMatrices;
	}
	/*.................................................................................................................*/
	public void setAlwaysPrepareForAnyMatrices(boolean alwaysPrepareForAnyMatrices) {
		this.alwaysPrepareForAnyMatrices = alwaysPrepareForAnyMatrices;
	}

	/*.................................................................................................................*/
	public String getTitleOfTextCommandLink() {
		return "";
	}
	/*.................................................................................................................*/
	public String getCommandOfTextCommandLink() {
		return "";
	}
	/*.................................................................................................................*/
	public void processUserClickingOnTextCommandLink(String command) {
	}

	public  void setOutputTextListener(OutputTextListener textListener){
	}

	/*.................................................................................................................*/
	public  void setUserAborted(){
		userAborted=true;
	}
	/*.................................................................................................................*/
	public  boolean getUserAborted(){
		return userAborted;
	}
	/*.................................................................................................................*/
	public  void setUserCancelled(){
		userCancelled=true;
	}
	/*.................................................................................................................*/
	public  boolean getUserCancelled(){
		return userCancelled;
	}
	public String getMessageIfUserAbortRequested () {
		return "";
	}
	public String getMessageIfCloseFileRequested () {
		return "";
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("autoSaveFile".equalsIgnoreCase(tag))
			autoSaveFile.setFromTrueFalseString(content);
		super.processSingleXMLPreference(tag, content);
	}
	
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "autoSaveFile", autoSaveFile);  
	
		buffer.append(super.preparePreferencesForXML());
		return buffer.toString();
	}

	// override to give more information
	public String getHTMLDescriptionOfStatus(){
		return getName();
	}
	// override to give more information
	public String getInferenceName(){
		return getName();
	}
	public abstract boolean isReconnectable();
	
	
	public boolean canGiveIntermediateResults(){
		return false;
	}
	public String getInferenceDetails() {
		return "";
	}
	public Tree getLatestTree(Taxa taxa, MesquiteNumber score, MesquiteString titleForWindow){
		if (score != null)
			score.setToUnassigned();
		return null;
	}
	public boolean canStoreLatestTree(){
		Tree latestTree = getLatestTree(null, null, null);
		return latestTree!=null;
	}
	public TreeVector getCurrentMultipleTrees(Taxa taxa, MesquiteString titleForWindow){
		return null;
	}
	public boolean canStoreMultipleCurrentTrees(){
		TreeVector currentTrees = getCurrentMultipleTrees(null, null);
		if (currentTrees==null)
			return false;
		return currentTrees.size()>0;
	}
	
	public String getTreeBlockName(boolean completedRun){
		return null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (tWindowMaker == null)
			return null;
		Snapshot temp = new Snapshot();
		temp.addLine("setWindowMaker " , tWindowMaker);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module showing the tree", "[name of module]", commandName, "setWindowMaker")) {
			tWindowMaker = (TWindowMaker)hireNamedEmployee(TWindowMaker.class, "#ObedientTreeWindow");
			String commands = getExtraTreeWindowCommands(false, MesquiteLong.unassigned);  //DAVIDCHECK:
			MesquiteWindow w = tWindowMaker.getModuleWindow();
			
			if (w != null){
				if (w instanceof SimpleTreeWindow)
					((SimpleTreeWindow)w).setWindowTitle("Most Recent Tree");
				Puppeteer p = new Puppeteer(this);
				p.execute(w, commands, new MesquiteInteger(0), "end;", false);
			}
			return tWindowMaker;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	public void registerListener(MesquiteListener listener){
		if (listened == null)
			listened = new Listened();
		listened.addListener(listener);
	}
	public void deregisterListener(MesquiteListener listener){
		if (listened == null)
			return;
		listened.removeListener(listener);
	}
	
	boolean needToSizeDisplay = true;
	protected void newResultsAvailable(TaxaSelectionSet outgroupSet){
		MesquiteString title = new MesquiteString();
		Tree tree = getLatestTree(null, null, title);
		parametersChanged();
		
		if (tree instanceof AdjustableTree) {
			((AdjustableTree)tree).standardize(outgroupSet, false);
		}
		prepareIntermediatesWindow();
		if (tree != null && tWindowMaker != null){ 
			tWindowMaker.setTree(tree);
			MesquiteWindow w = tWindowMaker.getModuleWindow();
			if (w != null && w instanceof SimpleTreeWindow){
				SimpleTreeWindow stw = (SimpleTreeWindow)w;
				if (title.isBlank())
					stw.setWindowTitle(title.getValue());
				int taxonSpacing = 14;
				int numTaxaInTree = tree.numberOfTerminalsInClade(tree.getRoot());
				int orientation = stw.getOrientation();
				if (orientation == TreeDisplay.RIGHT || orientation == TreeDisplay.LEFT){
					stw.setMinimumFieldSize(-1, numTaxaInTree*taxonSpacing);  
				}
				else if (orientation == TreeDisplay.UP || orientation == TreeDisplay.DOWN){
					stw.setMinimumFieldSize(numTaxaInTree*taxonSpacing, -1);  
				}
				else 
					stw.setMinimumFieldSize(-1, -1); 
				if (needToSizeDisplay) stw.sizeDisplays(false);
				needToSizeDisplay = false;
			}
			String commands = getExtraIntermediateTreeWindowCommands();
			if (!StringUtil.blank(commands)) {
				if (w != null){
					Puppeteer p = new Puppeteer(this);
					p.execute(w, commands, new MesquiteInteger(0), "", false);
				}
			}


		}
		if (listened != null)
			listened.notifyListeners(this, new Notification(MesquiteListener.NEW_RESULTS));
	}
	
//This is used for just single current tree; a different system is used in ZephyrRunner for consensus trees
	public MesquiteWindow prepareIntermediatesWindow(){
		if (tWindowMaker == null) {
			needToSizeDisplay = true;
			tWindowMaker = (TWindowMaker)hireNamedEmployee(TWindowMaker.class, "#ObedientTreeWindow");
			String commands = getExtraTreeWindowCommands(false, MesquiteLong.unassigned);
			commands += " showWindowForce;";
			MesquiteWindow w = tWindowMaker.getModuleWindow();
			if (w != null){
				if (w instanceof SimpleTreeWindow)
					((SimpleTreeWindow)w).setWindowTitle("Most Recent Tree");
				Puppeteer p = new Puppeteer(this);
				p.execute(w, commands, new MesquiteInteger(0), "end;", false);
			}
			return w;
		}
		else
			return tWindowMaker.getModuleWindow();
	}
	
	Checkbox autoSaveFileCheckbox =  null;

	public boolean getAutoSave() {
		if (autoSaveFile!=null && autoSaveFile.getValue())
			return true;
		return false;
	}
	
	// given the opportunity to fill in options for user
	public  void addItemsToDialogPanel(ExtensibleDialog dialog){
		autoSaveFileCheckbox = dialog.addCheckBox("Auto-save file after inference", autoSaveFile.getValue());
	}
	public boolean optionsChosen(){
		if (autoSaveFileCheckbox!=null)
			autoSaveFile.setValue(autoSaveFileCheckbox.getState());
		return true;
	}


}



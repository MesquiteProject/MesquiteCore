/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.lib;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public abstract class LiveTreeBlocks extends FileAssistantT {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSourceDefinite.class, getName() + "  needs a source of trees.",
		"The source of trees can be selected initially");
	}
	/*.................................................................................................................*/
	TreeVector treeBlock;
	TreeSourceDefinite treeSource;
	TreesManager manager;
	Taxa taxa =null;
	MesquiteTextWindow window;

	MesquiteBoolean checkIfTreesAreSame;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("LiveTrees");
		checkIfTreesAreSame = new MesquiteBoolean(false);
		addCheckMenuItem( null, "Recheck Tree Identity", makeCommand("toggleCheckSame",  this), checkIfTreesAreSame);
		//"$ #NumForTreeList #NumberOfTaxa"
		treeSource = (TreeSourceDefinite)hireNamedEmployee(TreeSourceDefinite.class, "$ #DefiniteTreeSource");
		if (treeSource == null) {
			return sorry(getName() + " couldn't start because no source of trees to serve as a basis for modification or filtering was obtained.");
		}
		manager = (TreesManager)findElementManager(TreeVector.class);
		window = new MesquiteTextWindow(this, "Live Tree Block", true); //infobar
		setModuleWindow(window);
		window.setEditable(false);
		window.setWindowSize(200,200);
		if (manager==null)
			return sorry(getName() + " couldn't start because no tree manager module was found.");
		if (!MesquiteThread.isScripting())
			return establishBlock(null);
		if (window != null){
			window.setText(getTextForWindow());
			resetAllMenuBars();
		}
		if (!MesquiteThread.isScripting()){
			window.setVisible(true);
			window.show();
		}
		return true;
	}
	public String getTreeSourceName(){
		return "#DefiniteTreeSource";
	}
	/*.........................................................................................................h........*/
	public void settaxa(Taxa taxa) {
		if (taxa !=null && taxa.isDoomed())
			return;
		if (taxa!=this.taxa) {
			if (this.taxa !=null)
				this.taxa.removeListener(this);
			this.taxa = taxa;
			taxa.addListener(this);
		}
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (treeBlock !=null) {
			treeBlock.removeListener(this);
			//remove other listners!!!!!!!!!
		}

		super.endJob();
	}
	private String getTextForWindow(){
		if (treeBlock == null)
			return "There is no live tree block yet.";
		if (treeSource == null)
			return "There is no tree source";
		String s = "Live Tree Block\nThe tree block \"" + treeBlock.getName() + "\" is linked to the tree source " + treeSource.getNameAndParameters();
		s += "\nThe tree block will be available as a stored tree block (e.g., using Stored Trees as a tree source).\nIf The tree source changes its output, the tree block will be updated.";
		s += "However, when you save the file, the trees will be saved as a block in the file.";
		return s;
	}
	private int whichTreeBlock(){
		int total = manager.getNumberTreeBlocks(taxa, getProject().getHomeFile());
		for (int i=0; i<total; i++){
			TreeVector trees = manager.getTreeBlock(taxa, getProject().getHomeFile(), i);
			if (treeBlock == trees)
				return i;
		}
		return -1;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (taxa!=null && getProject().getNumberTaxas()>1)
			temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa));
		temp.addLine("getTreeSource ", treeSource); 
		temp.addLine("toggleCheckSame " + checkIfTreesAreSame.toOffOnString());
		if (treeBlock != null){
			int treeblockNumber = whichTreeBlock();
			if (treeblockNumber>=0)
				temp.addLine("linkToTreeBlock " + TreeVector.toExternal(treeblockNumber)); 
		}
		Snapshot fromWindow = window.getSnapshot(file);
		temp.addLine("getWindow");
		temp.addLine("tell It");
		temp.incorporate(fromWindow, true);
		temp.addLine("showWindow");
		temp.addLine("endTell");
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the source of the tree to be converted to live tree block", null, commandName, "getTreeSource")) {
			return treeSource;
		}
		else if (checker.compare(this.getClass(),  "Sets which block of trees to use", "[block number]", commandName, "linkToTreeBlock")) {
			int whichList = TreeVector.toInternal(MesquiteInteger.fromString(arguments, new MesquiteInteger(0)));
			if (MesquiteInteger.isCombinable(whichList)) {
				if (taxa==null && getProject().getNumberTaxas()==1)
					taxa = getProject().getTaxa(0);
				treeBlock = manager.getTreeBlock(taxa, getProject().getHomeFile(), whichList);  //must add to home file
				if (treeBlock ==null){
					discreetAlert( "No tree block found for linking"); 
					return null;
				}
				treeSource.initialize(taxa);

				window.setText(getTextForWindow());
				return treeBlock;
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show just the last tree", "[on or off]", commandName, "toggleCheckSame")) {
			boolean current = checkIfTreesAreSame.getValue();
			checkIfTreesAreSame.toggleValue(parser.getFirstToken(arguments));
			if (current!=checkIfTreesAreSame.getValue())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets which block of taxa to use", "[block reference, number, or name]", commandName, "setTaxa")) { 
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				establishBlock(t);
				window.setText(getTextForWindow());
				return t;
			}
		}
		else if (checker.compare(this.getClass(),  "Returns current tree block", null, commandName, "getTreeBlock")) {
			return treeBlock;
		}
		else if (checker.compare(this.getClass(),  "Returns the window", null, commandName, "getWindow")) {
			return window;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/

	private boolean establishBlock(Taxa tx){

		if (tx != null)
			taxa = tx;
		else 
			taxa = getProject().chooseTaxa(containerOfModule(), "Taxa for which to establish a live tree block");
		if (taxa == null)
			return false;
		settaxa(taxa);
		treeSource.initialize(taxa);
		treeBlock = manager.makeNewTreeBlock(taxa, "Trees from " + treeSource.getName(), getProject().getHomeFile());
		int numTrees = treeSource.getNumberOfTrees(taxa);
		for (int i = 0; i< numTrees; i++){
			Tree t = treeSource.getTree(taxa, i);
			treeBlock.addElement(t.cloneTree(), false);
		}
		window.setText(getTextForWindow());
		return true;
	}

	private void synchTreeBlock(){
		if (treeBlock == null)
			return;
		boolean changed = false;
		boolean added = false;
		int numTrees = treeSource.getNumberOfTrees(taxa);

		int numTreesInBlock = treeBlock.size();
		if (numTreesInBlock>numTrees){
			int numExcess = numTreesInBlock-numTrees;
			for (int i=0; i<numExcess; i++) {
				treeBlock.removeElementAt(treeBlock.size()-1, false);
				changed = true;
			}
		}

		if (checkIfTreesAreSame.getValue())
			for (int i = 0; i< numTrees && i < treeBlock.size(); i++){
				Tree t = treeSource.getTree(taxa, i);
				Tree tPresent = (Tree)treeBlock.elementAt(i);
				if (!t.equals(tPresent)) {
					treeBlock.replaceElement(tPresent, t.cloneTree(), false);
					changed = true;
				}
			}
		for (int i= treeBlock.size(); i<numTrees; i++){
			Tree t = treeSource.getTree(taxa, i);
			treeBlock.addElement(t.cloneTree(), false);
			added = true;
		}
		if (changed)
			treeBlock.notifyListeners(this, new Notification(MesquiteListener.ELEMENT_CHANGED));
		else if (added)
			treeBlock.notifyListeners(this, new Notification(MesquiteListener.ITEMS_ADDED, new int[]{numTreesInBlock, MesquiteInteger.finite }));

		window.setText(getTextForWindow());
	}


	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee == treeSource)
			synchTreeBlock();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return true; //checkIfTreesAreSame
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Live Tree Block...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Live Tree Block";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 111;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Establishes a tree block from a source and maintains it as a live tree block." ;  
	}

}



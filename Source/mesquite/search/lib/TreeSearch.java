/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.search.lib;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.Incrementable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.OutputTextListener;
import mesquite.lib.Reconnectable;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.TreeInferer;
import mesquite.lib.duties.TreeSearcher;
import mesquite.lib.misc.CanRetrieveTreeBlock;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;

/* ======================================================================== */
public abstract class TreeSearch extends TreeInferer implements Incrementable {
	protected TreeSearcher searchTask;
	
	public String getExplanation() {
		return "Supplies trees resulting from a search to optimize some value.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSearcher.class, getName() + "  needs a method to search for trees.",
		"The method to search for trees can be selected initially");
	}
	/*.................................................................................................................*/
public boolean isReconnectable(){
	return searchTask.isReconnectable();
}
public String getMessageIfUserAbortRequested () {
	if (searchTask!=null)
		return searchTask.getMessageIfUserAbortRequested();
	return null;
}
public String getInferenceDetails() {
	if (searchTask==null)
		return "";
	return searchTask.getInferenceDetails();
}

/*.................................................................................................................*/
public String getTitleOfTextCommandLink() {
	if (searchTask==null)
		return "";
	return searchTask.getTitleOfTextCommandLink();
}
/*.................................................................................................................*/
public String getCommandOfTextCommandLink() {
	if (searchTask==null)
		return "";
	return searchTask.getCommandOfTextCommandLink();
}

/*.................................................................................................................*/
public void processUserClickingOnTextCommandLink(String command) {
	if (searchTask!=null)
	 searchTask.processUserClickingOnTextCommandLink(command);
}


	/*.................................................................................................................*/
	public  void setUserAborted(){
		userAborted=true;
		searchTask.setUserAborted();
	}

	public  void setOutputTextListener(OutputTextListener textListener){
		if (searchTask != null)
			searchTask.setOutputTextListener(textListener);
	}
	public String getInferenceName(){
		return searchTask.getInferenceName();
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (arguments !=null) {
			searchTask = (TreeSearcher)hireNamedEmployee(TreeSearcher.class,arguments, condition);
			if (searchTask==null) {
				return sorry(getName() + " couldn't start because the requested tree searching module not obtained: "+arguments);
			}
		}
		else {
			searchTask= (TreeSearcher)hireCompatibleEmployee(TreeSearcher.class, condition, "Tree Searcher");
			if (searchTask==null) return sorry(getName() + " couldn't start because tree searching module not obtained.");
		}
		return true;
	}
	public String getLogText() {
		return searchTask.getLogText();
	}
/*
	// optional, in case employer wants to force use of a matrix
 public MatrixSourceCoord getMatrixSource() {
	 return searchTask.getMatrixSource();
 }
 public void setMatrixSource(MatrixSourceCoord msource) {
	 searchTask.setMatrixSource(msource);
 }

	/*.................................................................................................................*/
	public String getHTMLDescriptionOfStatus(){
		if (searchTask != null){
			return searchTask.getHTMLDescriptionOfStatus();
		}
		return getName();
	}
	public boolean stopInference(){
		searchTask.stopInference();
		return true;
	}

	public void abortFilling(){
		if (stopInference())
			abort = true;
	}

   	public Reconnectable getReconnectable(){
   		if (searchTask instanceof Reconnectable)
   			return (Reconnectable)searchTask;
   		return null;
   	}
   	
	public String getExtraTreeWindowCommands (boolean finalTree, long treeBlockID){
		 if (searchTask!=null)
			 return searchTask.getExtraTreeWindowCommands(finalTree, treeBlockID);
		 else
			 return "";
   	 }
	 
	 public String getExtraIntermediateTreeWindowCommands (){
		 if (searchTask!=null)
			 return searchTask.getExtraIntermediateTreeWindowCommands();
		 else
			 return "";
   	 }

		public String getTreeBlockName(boolean completedRun){
			return searchTask.getTreeBlockName(completedRun);
		}

  
   	 public boolean canGiveIntermediateResults(){
   		 return searchTask.canGiveIntermediateResults();
   	 }
 	public Tree getLatestTree(Taxa taxa, MesquiteNumber score, MesquiteString titleForWindow){
   		return searchTask.getLatestTree(taxa, score, titleForWindow);
   	 }
 	
 	
	public TreeVector getCurrentMultipleTrees(Taxa taxa, MesquiteString titleForWindow){
   		return searchTask.getCurrentMultipleTrees(taxa, titleForWindow);
   	 }



	/*.................................................................................................................*/
	public void setCurrent(long i){  //SHOULD NOT notify (e.g., parametersChanged)
		if (searchTask instanceof Incrementable)
			((Incrementable)searchTask).setCurrent(i);
	}
	public long getCurrent(){
		if (searchTask instanceof Incrementable)
			return ((Incrementable)searchTask).getCurrent();
		return 0;
	}
	public String getItemTypeName(){
		if (searchTask instanceof Incrementable)
			return ((Incrementable)searchTask).getItemTypeName();
		return "";
	}
	public long getMin(){
		if (searchTask instanceof Incrementable)
			return ((Incrementable)searchTask).getMin();
		return 0;
	}
	public long getMax(){
		if (searchTask instanceof Incrementable)
			return ((Incrementable)searchTask).getMax();
		return 0;
	}
	public long toInternal(long i){
		if (searchTask instanceof Incrementable)
			return ((Incrementable)searchTask).toInternal(i);
		return i-1;
	}
	public long toExternal(long i){ //return whether 0 based or 1 based counting
		if (searchTask instanceof Incrementable)
			return ((Incrementable)searchTask).toExternal(i);
		return i+1;
	}

	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return TreeSearcher.class;  
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setSearcher " , searchTask);
		temp.incorporate(super.getSnapshot(file), false);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module performing the tree searching", "[name of module]", commandName, "setSearcher")) {
			TreeSearcher temp= (TreeSearcher)replaceEmployee(TreeSearcher.class, arguments, "Tree Searcher", searchTask);
			if (temp!=null){
				searchTask=  temp;
				parametersChanged(); //?
			}
			return searchTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		searchTask.setTreeInferer(this);
		searchTask.initialize(taxa);
	}
	/*.................................................................................................................*/
	public int fillTreeBlock(TreeVector treeList, int numberIfUnlimited){
		//DISCONNECTABLE
		searchTask.setTreeInferer(this);
		return searchTask.fillTreeBlock(treeList);
	}
	/*.................................................................................................................*/
	public void retrieveTreeBlock(TreeVector treeList, int numberIfUnlimited){
		if (searchTask instanceof CanRetrieveTreeBlock)
			((CanRetrieveTreeBlock)searchTask).retrieveTreeBlock(treeList);
	}
/*.................................................................................................................*/
	public String getParameters() {
		if (searchTask==null)
			return("");
		else
			return "Searcher: " + searchTask.getName();
	}
	/*.................................................................................................................*/
	public boolean hasLimitedTrees(Taxa taxa){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}

}


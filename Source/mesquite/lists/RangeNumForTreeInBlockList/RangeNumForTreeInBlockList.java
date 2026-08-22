/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.RangeNumForTreeInBlockList;
/*~~  */

import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.NumberArray;
import mesquite.lib.Pausable;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.duties.NumberForTreeBlock;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.TreeVector;
import mesquite.lists.lib.TreeblocksListAssistant;

/* ======================================================================== */
public class RangeNumForTreeInBlockList extends TreeblocksListAssistant implements MesquiteListener, Pausable {
	/*.................................................................................................................*/
	public String getName() {
		return "Range of Num. for Tree in Block (in List of Tree Blocks window)";
	}
	public String getNameForMenuItem() {
		return "Range of Value among Trees";
	}
	public String getVeryShortName() {
		if (numberTask != null)
			return "Range: " + numberTask.getVeryShortName();
		return "Range Num. for Tree";
	}
	public String getExplanation() {
		return "Supplies range of numbers for trees in tree blocks for a tree blocks list window." ;
	}
	/*.................................................................................................................*/
	ListableVector treeBlocks=null;
	protected NumberForTree numberTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForTree)hireNamedEmployee(NumberForTree.class, arguments);
			if (numberTask==null) {
				return sorry("Range in number for trees (for tree block list) can't start because the requested module was not successfully hired");
			}
			return true;
		}
		numberTask = (NumberForTree)hireEmployee(NumberForTree.class, "Value to calculate for tree block (for tree block list)");
		if (numberTask==null) {
			return sorry("Range in number for trees (for tree block list) can't start because the no calculating module was successfully hired");
		}
		return true;
	}
	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public void setTableAndObject(MesquiteTable table, Object obj){
		if (treeBlocks !=null)
			treeBlocks.removeListener(this);
		if (obj instanceof ListableVector)
			this.treeBlocks = (ListableVector)obj;
		treeBlocks.addListener(this);
		doCalcs();
	}
	/** Indicate what could be paused */
	public void addPausables(Vector pausables) {
		if (pausables != null)
			pausables.addElement(this);
	}
	/** to ask Pausable to pause*/
	public void pause() {
		paused = true;
	}
	/** to ask a Pausable to unpause (i.e. to resume regular activity)*/
	public void unpause() {
		paused = false;
		doCalcs();
		parametersChanged(null);
	}
	/*.................................................................................................................*/
	boolean paused = false;
	boolean okToCalc() {
		return !paused;
	}
	public boolean isPaused(){
		return paused;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		//TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		doCalcs();
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return NumberForTree.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setValueTask ", numberTask); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a number for trees to calculate range in a block of trees", "[name of module]", commandName, "setValueTask")) {
			NumberForTree temp= (NumberForTree)hireNamedEmployee(NumberForTree.class, arguments);
			Class prev = null;
			if (numberTask != null)
				prev = numberTask.getClass();
			if (temp!=null) {
				numberTask = temp;
				if (prev != numberTask.getClass()){
					doCalcs();
					outputInvalid();
				}
				return temp;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public String getTitle() {
		if (numberTask==null)
			return "";
		return "Range: " + numberTask.getVeryShortName();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		doCalcs();
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	StringArray explArray = new StringArray(0);
	/*.................................................................................................................*/
	public void doCalcs(){
		if (!okToCalc() || numberTask==null || treeBlocks == null)
			return;
		outputInvalid();
		int numBlocks = treeBlocks.size();
		explArray.resetSize(numBlocks);
		MesquiteString expl = new MesquiteString();
		MesquiteNumber mn = new MesquiteNumber();
		MesquiteNumber min = new MesquiteNumber();
		MesquiteNumber max = new MesquiteNumber();
		for (int ic=0; ic<numBlocks; ic++) {
			TreeVector trees = (TreeVector)treeBlocks.elementAt(ic);
			min.setToUnassigned();
			max.setToUnassigned();
			for (int itr = 0; itr< trees.size(); itr++){
				mn.setToUnassigned();
				numberTask.calculateNumber(trees.getTree(itr), mn, expl);
				min.setMeIfIAmMoreThan(mn);
				max.setMeIfIAmLessThan(mn);
			}
			explArray.setValue(ic, min.toString(4) + " – " + max.toString(4));
		}
	}
	public String getExplanationForRow(int ic){
		if (explArray == null || explArray.getSize() <= ic)
			return null;
		return "Among trees in this block, range of values for " + numberTask.getVeryShortName() + ": " + explArray.getValue(ic);
	}
	public String getStringForRow(int ic){
		if (explArray==null)
			return "";

		return explArray.getValue(ic);
	}
	public String getWidestString(){
		if (numberTask==null)
			return "888888";
		return "Range: " + numberTask.getVeryShortName()+"   ";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	public void endJob() {
		if (treeBlocks !=null)
			treeBlocks.removeListener(this);
		super.endJob();
	}
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return NEXTRELEASE;  
 	}

	

}


/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison. 
Version 2.73, July 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.NumTopologyCongruent;

import mesquite.lib.*;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.duties.TreeSourceDefinite;

public class NumTopologyCongruent extends NumberForTree {
	TreeSourceDefinite treeSourceTask;
	MesquiteString treeSourceName;
	MesquiteCommand tstC;
	Taxa currentTaxa = null;
	boolean suspend = false;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(TreeSourceDefinite.class, getName() + " needs a source of trees which will then be condensed.",
		"The source of other trees is indicated initially.");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeSourceTask = (TreeSourceDefinite)hireEmployee(TreeSourceDefinite.class, "Tree Source");
		if (treeSourceTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained");
		tstC =  makeCommand("setTreeSource",  this);
		treeSourceTask.setHiringCommand(tstC);
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		if (numModulesAvailable(TreeSourceDefinite.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source", tstC, TreeSourceDefinite.class);
			mss.setSelected(treeSourceName);
		}
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suspend");
		temp.addLine("getTreeSource",treeSourceTask);
		temp.addLine("resume");
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns treeSourceTask", null, commandName, "getTreeSource")) 
			return treeSourceTask;
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspend = true;
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspend = false;
			parametersChanged();
		}
		else 
			super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		setPreferredTaxa(taxa);
	}
	public void endJob(){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		storePreferences();
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void disposing(Object obj){
		if (obj == currentTaxa) {
			setHiringCommand(null); //since there is no rehiring
			iQuit();
		}
	}

	/*.................................................................................................................*/

	public void setPreferredTaxa(Taxa taxa){
		if (taxa !=currentTaxa) {
			if (currentTaxa!=null)
				currentTaxa.removeListener(this);
			currentTaxa = taxa;
			currentTaxa.addListener(this);
		}

	}
	/*.................................................................................................................*/

	public boolean isPrerelease(){
		return false;
	}


	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (tree==null || result==null || treeSourceTask==null)
			return;
		clearResultAndLastResult(result);
		Taxa taxa = tree.getTaxa();
		int numOriginalTrees = treeSourceTask.getNumberOfTrees(taxa);

		int count=0;
		for (int i=0; i<numOriginalTrees; i++){
			Tree comparisonTree =  treeSourceTask.getTree(taxa, i);
			if (tree.equalsTopology(comparisonTree,false))
				count++;
		}
		result.setValue(count);
		if (resultString!=null)
			resultString.setValue("" + count + " trees congruent");
		saveLastResult(result);
		saveLastResultString(resultString);
	}

	public String getName() {
		return "Number of Congruent Trees";
	}
	public String getExplanation() {
		return "For the given tree, returns the number of trees in the tree source that are congruent with it.";
	}

	/*........................................................*/
    public int getVersionOfFirstRelease(){
        return 260;
    }


}

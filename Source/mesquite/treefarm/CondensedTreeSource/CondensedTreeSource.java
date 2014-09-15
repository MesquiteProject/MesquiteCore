/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.CondensedTreeSource;

import mesquite.lib.*;
import mesquite.lib.duties.*;

public class CondensedTreeSource extends TreeSource {
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

	
	public String getExplanation() {
		return "Produces a block of trees that are a condensed version of another block of trees, with duplicate trees removed.";
	}
	public String getName() {
		return "Condensed Tree Source";
	}

	public int[] getIntegerArrayOfIdentities(Taxa taxa, MesquiteInteger numCondensedTrees) {
		if (treeSourceTask==null)
			return null;
		if (taxa!=currentTaxa) {
			initialize(taxa);
			treeSourceTask.initialize(currentTaxa);
		}
		int numTrees = treeSourceTask.getNumberOfTrees(taxa);
		if (numTrees<=0)
			return null;
		int[] identities = new int[numTrees];
		for (int i=0; i<numTrees; i++) 
			identities[i]=-1;
		int treeType = -1;
		for (int i=0; i<numTrees; i++){
			if (identities[i]<0) {// we haven't encountered this one before
				treeType++;
				identities[i] = treeType;
				Tree tree = treeSourceTask.getTree(taxa, i);
				for (int candidate=i+1; candidate<numTrees; candidate++) {
					if (identities[candidate]<0) {// we haven't encountered this one before
						Tree candidateTree = treeSourceTask.getTree(taxa, candidate);
						if (candidateTree.equalsTopology(tree, false))
							identities[candidate] = treeType;
					}
				}
			}
		}
		if (numCondensedTrees !=null)
			numCondensedTrees.setValue(treeType+1);
		return identities;
		
	}

	public int getNumberOfTrees(Taxa taxa) {
		if (treeSourceTask==null)
			return 0;
		MesquiteInteger numTrees = new MesquiteInteger();
		getIntegerArrayOfIdentities(taxa,numTrees);
		return numTrees.getValue();
	}

	public Tree getTree(Taxa taxa, int itree) {
		if (treeSourceTask==null)
			return null;
		MesquiteInteger numTrees = new MesquiteInteger();
		int[] identities = getIntegerArrayOfIdentities(taxa,numTrees);
		int count = 0;
		int numOriginalTrees = treeSourceTask.getNumberOfTrees(taxa);
		for (int it=0; it<numOriginalTrees; it++){
			if (identities[it]==itree) 
				count++;
		}

		for (int i=0; i<numOriginalTrees; i++)
			if (identities[i]==itree) {
				Tree tree =  treeSourceTask.getTree(taxa, i).cloneTree();
				if (tree instanceof AdjustableTree)
					((AdjustableTree)tree).setName("tree " + (i+1) + " (" + count + ")");
				if (tree instanceof Attachable){
					MesquiteString s = new MesquiteString(""+count);
					s.setName("NumberOfOriginalTrees");
					((Attachable)tree).attachIfUniqueName(s);
				}
				return tree;
			}
		return null;
	}

	public String getTreeNameString(Taxa taxa, int i) {
		if (treeSourceTask==null)
			return null;
		MesquiteInteger numTrees = new MesquiteInteger();
		int[] identities = getIntegerArrayOfIdentities(taxa,numTrees);
		int numOriginalTrees = treeSourceTask.getNumberOfTrees(taxa);
		int count = 0;
		for (int it=0; it<numOriginalTrees; it++){
			if (identities[it]==i) 
				count++;
		}
		return "tree " + (i+1)+ " (" + count + ")";

/*		for (int it=0; it<numOriginalTrees; it++)
			if (identities[it]==i) {
				//Tree tree = treeSourceTask.getTree(taxa, it);;
			}
		return null;
		*/
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
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 201;  
	}

	/*.................................................................................................................*/
 	 
  	public boolean isPrerelease(){
  		return false;
  	}


}

/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.consensus.ConsensusTree;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class ConsensusTree extends TreeSource {
	int currentTree=0;
	TreeSource treeSource;
	Taxa oldTaxa=null;

	Consenser consenser = null;

	MesquiteString treeSourceName, consenserName; 
	MesquiteCommand tlsC, cC;
	int numTreesAssigned = MesquiteInteger.unassigned;
	int defaultNumberOfItems = 100;
	boolean assigned = false;
	private MesquiteMenuItemSpec numTreesItem;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		startTree = 0;
		treeSource= (TreeSource)hireEmployee(TreeSource.class, "Source of Trees for consensus");
		tlsC = makeCommand("setTreeSource",  this);
		cC = makeCommand("setConsenser", this);
		if (treeSource==null)
			return sorry(getName() + " couldn't start because no source of trees obtained");
		//treeSource.setHiringCommand(tlsC);
		treeSourceName = new MesquiteString();
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source for Consensus", tlsC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}

		consenser= (Consenser)hireEmployee(Consenser.class, "Consensus calculator");
		if (consenser==null)
			return sorry(getName() + " couldn't start because no consensus module obtained.");
		consenser.setHiringCommand(cC);
		consenserName = new MesquiteString();
		if (numModulesAvailable(Consenser.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Consensus module", cC, Consenser.class);
			mss.setSelected(consenserName);
		}
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		if (m instanceof TreeSource)
			iQuit();
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setTreeSource ", treeSource); 
		temp.addLine("setConsenser ", consenser); 
		if (MesquiteInteger.isCombinable(numTreesAssigned))
			temp.addLine("assignNumTrees " + numTreesAssigned);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module supplying tree blocks", "[name of module]", commandName, "setTreeSource")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees", treeSource);
			if (temp!=null) {
				treeSource = temp;
				startTree = 0;
			//	treeSource.setHiringCommand(tlsC);
				treeSourceName.setValue(treeSource.getName());
				parametersChanged();
			}
			return temp;
		}
		else if (checker.compare(this.getClass(), "Sets the number of trees", "[number of trees]", commandName, "assignNumTrees")) {
			//Changed so numTreesAssigned updated if queryNumTrees is called (previous version [see below] did not update numTreesAssigned)
			//J.C. Oliver Nov.20.2013
			int newNum = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum) || newNum < 0){
				newNum = queryNumTrees();
			}
			if(newNum > 0){
				numTreesAssigned = newNum;
				assigned = true;
				parametersChanged();
			}
			/*Old way:
			int newNum = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum)){
				queryNumTrees();
			}
			else if (newNum>0 ) {
				numTreesAssigned = newNum;
				assigned = true;
			}
			parametersChanged();
			 */
		}
		else
			if (checker.compare(this.getClass(), "Sets the module doing a consensus", "[name of module]", commandName, "setConsenser")) {
				Consenser temp = (Consenser)replaceEmployee(Consenser.class, arguments, "Consensus module", consenser);
				if (temp!=null) {
					consenser = temp;
					startTree = 0;
					consenser.setHiringCommand(cC);
					consenserName.setValue(consenser.getName());
					parametersChanged();
				}
				return temp;
			}
			else
				return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa){
		oldTaxa = taxa;
	}
	int queryNumTrees(){
		return MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of trees from \"" + treeSource.getName() + "\" for consensus", numTreesAssigned);
	}
	/*.................................................................................................................*/
	int checkNumTrees(Taxa taxa){
		int numTrees = treeSource.getNumberOfTrees(taxa);
		if (MesquiteInteger.isInfinite(numTrees)){
			if (numTreesItem == null) {
				numTreesItem = addMenuItem( "Number of Trees for Consensus...", makeCommand("assignNumTrees",  this));
				resetContainingMenuBar();
			}
			if (MesquiteInteger.isCombinable(numTreesAssigned))
				return numTreesAssigned;
			if (!MesquiteThread.isScripting()) 
				numTreesAssigned = queryNumTrees();
			if (!MesquiteInteger.isCombinable(numTreesAssigned)) 
				numTreesAssigned = defaultNumberOfItems;
			assigned = true;
			return numTreesAssigned;
		}
		else {
			if (numTreesItem != null) {
				deleteMenuItem(numTreesItem);
				numTreesItem = null;
				resetContainingMenuBar();
			}
			return numTrees;
		}
	}
	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		startTree = 0;
		if (treeSource!=null) {
			treeSource.initialize(taxa); //WHEN this module takes in tree from a source, should ask to initialize that source
			checkNumTrees(taxa);
		}
	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int ic) {  
		return getTree(taxa,ic,true);
	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int ic, boolean verbose) {  
		oldTaxa = taxa;

		int numTrees = checkNumTrees(taxa);
		Trees trees = new TSourceWrapper(treeSource, taxa);
		Tree tree = null;
		if (consenser instanceof IncrementalConsenser){
			IncrementalConsenser iConsenser = (IncrementalConsenser)consenser;
			if (startTree == 0)
				iConsenser.reset(taxa);
			boolean done = false;
			int count = 0;
			if (verbose) logln("Consensing trees ");
			for (int i= startTree; i<numTrees && !done; i++) {
				Tree t = trees.getTree(i);
				if (t == null)
					done = true;
				else {
					iConsenser.addTree(t);
					CommandRecord.tick("Processing tree " + (i+1));
					startTree++;
					count++;
					if (verbose) {
						if (startTree % 1000 == 0)
							log(" " + startTree + " ");
						else if (count % 100==0)
							log(".");
					}
				}
			}
			if (verbose)
				logln("Trees consensed");
			//			if (count>0)
			tree = iConsenser.getConsensus();
			if (tree instanceof MesquiteTree)
				if (count==0)
					((MesquiteTree)tree).setName(consenser.getName() + " of trees from " + treeSource.getNameAndParameters());
				else
					((MesquiteTree)tree).setName(consenser.getName() + " of " + count + " trees from " + treeSource.getNameAndParameters());
		}
		else {
			tree = consenser.consense(trees);
			if (tree instanceof MesquiteTree)
				((MesquiteTree)tree).setName(consenser.getName() + " from " + treeSource.getNameAndParameters());
		}
		if (verbose)
			logln("");

		return tree;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		return 1;
	}

	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		return consenser.getName() + " from " + treeSource.getNameAndParameters();
	}
	/**Returns name to show in windows etc. for tree block or source of trees.*/
	public String getTreesDescriptiveString(Taxa taxa){
		return "Consensus of " + treeSource.getTreesDescriptiveString(taxa);
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return consenser.getName() + " of trees from " + treeSource.getName();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Consensus Tree";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies consensus tree from a block of trees.";
	}
	int startTree = 0;
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (MesquiteThread.isScripting())
			return;

		if (employee == treeSource){
			if (notification == null){
				startTree = 0;

				parametersChanged();
			}
			else if (consenser instanceof IncrementalConsenser && notification.getCode() == MesquiteListener.ITEMS_ADDED){
				parametersChanged();
			}
			else if (notification.getCode() != MesquiteListener.SELECTION_CHANGED){
				startTree =  0;
				parametersChanged(new Notification(notification.getCode()));
			}
		}
	}

	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new ConsCompatibilityTest();
	}
	public boolean isPrerelease(){
		return false;
	}
	public boolean requestPrimaryChoice() { return true; } //WPM 06 set to true
}

class ConsCompatibilityTest extends CompatibilityTest{
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (prospectiveEmployer != null)
			return prospectiveEmployer.numModulesAvailable(Consenser.class)>0;
			else
				return MesquiteTrunk.mesquiteTrunk.numModulesAvailable(Consenser.class)>0;
	}
}

class TSourceWrapper implements Trees {
	TreeSource source;
	Taxa taxa;
	TSourceWrapper(TreeSource list, Taxa taxa){
		this.source = list;
		this.taxa = taxa;
	}
	/** Get the taxa to which the trees applies */
	public Taxa getTaxa(){
		return taxa;
	}

	public Tree getTree(int i){
		return source.getTree(taxa, i);
	}

	public int size(){
		return source.getNumberOfTrees(taxa);
	}

}



/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.ConsensusTreesFromBlocks;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class ConsensusTreesFromBlocks extends TreeSource {
	public String getName() {
		return "Consensus Trees from Tree Blocks";
	}
	public String getExplanation() {
		return "Supplies consensus trees from Tree blocks.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeBlockSource.class, getName() + "  needs a source of tree blocks of which to do the consensus.",
		"The source of tree blocks can be chosen initially or in the Tree Block Source submenu");
		//e.setDivertChainMessage("testing");
		EmployeeNeed e2 = registerEmployeeNeed(Consenser.class, getName() + "  needs a method to make consensus trees.",
		"The method to make consensus trees can be chosen initially or in the Consensus Method submenu");
	}

	/*.................................................................................................................*/
	int currentTree=0;
	TreeBlockSource treeListSource;
	Taxa oldTaxa=null;
	TreeVector treeList=null;
	int defaultNumberOfTreeBlocks = 100;
	int numberOfTreeBlocks;
	IntegerArray notes;
	Consenser consenser = null;
	private MesquiteMenuItemSpec numListsItem;
	MesquiteString treeSourceName, consenserName;
	MesquiteCommand tlsC, cC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments ==null) {
			treeListSource= (TreeBlockSource)hireNamedEmployee(TreeBlockSource.class, StringUtil.tokenize("Stored Tree Blocks"));
			if (treeListSource==null)
				treeListSource= (TreeBlockSource)hireEmployee(TreeBlockSource.class, "Source of Tree blocks for consensus");
		}
		else {
			treeListSource= (TreeBlockSource)hireNamedEmployee(TreeBlockSource.class, arguments);
			if (treeListSource == null) {
				treeListSource= (TreeBlockSource)hireNamedEmployee(TreeBlockSource.class, StringUtil.tokenize("Stored Tree Blocks"));
				if (treeListSource==null)
					treeListSource= (TreeBlockSource)hireEmployee(TreeBlockSource.class, "Source of Tree blocks for consensus");
			}
		}
		tlsC = makeCommand("setTreeBlockSource",  this);
		if (treeListSource==null)
			return sorry(getName() + " couldn't start because no source of trees obtained");

		treeListSource.setHiringCommand(tlsC);
		treeSourceName = new MesquiteString();
		if (numModulesAvailable(TreeBlockSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Block Source", tlsC, TreeBlockSource.class);
			mss.setSelected(treeSourceName);
		}
		cC = makeCommand("setConsenser",  this);
		consenser= (Consenser)hireEmployee(Consenser.class, "Consensus calculator");
		if (consenser==null)
			return sorry(getName() + " couldn't start because no consensus module obtained.");
		consenser.setHiringCommand(cC);
		consenserName = new MesquiteString();
		if (numModulesAvailable(Consenser.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Consensus Method", cC, Consenser.class);
			mss.setSelected(consenserName);
		}
		return true;
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return TreeBlockSource.class;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setTreeBlockSource ", treeListSource); 
		temp.addLine("setConsenser ", consenser); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module supplying tree blocks", "[name of module]", commandName, "setTreeBlockSource")) {
			TreeBlockSource temp = (TreeBlockSource)replaceEmployee(TreeBlockSource.class, arguments, "Source of tree blocks", treeListSource);
			if (temp!=null) {
				treeListSource = temp;
				treeListSource.setHiringCommand(tlsC);
				treeSourceName.setValue(treeListSource.getName());
				int numLists=treeListSource.getNumberOfTreeBlocks(oldTaxa);
				if (numLists==MesquiteInteger.infinite) {
					numberOfTreeBlocks = defaultNumberOfTreeBlocks;
					if (numListsItem==null)
						numListsItem = addMenuItem( "Number of Tree Blocks...", makeCommand("setNumberTreeBlocks",  this));
					resetContainingMenuBar();
				}
				else {
					numberOfTreeBlocks = numLists;
					if (numListsItem!=null)
						deleteMenuItem(numListsItem);
					numListsItem=null;
				}
				parametersChanged();
			}
			return temp;
		}
		else
			if (checker.compare(this.getClass(), "Sets the module doing a consensus", "[name of module]", commandName, "setConsenser")) {
				Consenser temp = (Consenser)replaceEmployee(Consenser.class, arguments, "Consensus module", consenser);
				if (temp!=null) {
					consenser = temp;
					consenser.setHiringCommand(tlsC);
					consenserName.setValue(consenser.getName());
					parametersChanged();
				}
				return temp;
			}
			else if (checker.compare(this.getClass(), "Sets the number of tree blocks from which consensuses are derived (useful if the source of tree blocks has an indefinite number)", "[number of blocks]", commandName, "setNumberTreeBlocks")) {
				int newNum= MesquiteInteger.fromFirstToken(arguments,pos);
				if (!MesquiteInteger.isCombinable(newNum))
					MesquiteInteger.queryInteger(containerOfModule(), "Set Number of Tree Blocks", "Number of Tree Blocks:", numberOfTreeBlocks, 0, MesquiteInteger.infinite);
				if (newNum>0  && newNum!=numberOfTreeBlocks) {
					numberOfTreeBlocks = newNum;
				}
			}
			else
				return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa){
		oldTaxa = taxa;
	}
	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		if (treeListSource!=null)
			treeListSource.initialize(taxa); //WHEN this module takes in tree from a source, should ask to initialize that source
	}
	int numTreesUsed = 0;
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int ic) {  
		oldTaxa = taxa;
		if (treeListSource == null)
			return null;
		treeList = treeListSource.getBlock(taxa, ic);
		if (treeList == null)
			return null;
		currentTree = ic;
		numTreesUsed = treeList.size();
		Tree tree = consenser.consense(treeList);
		return tree;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		int numLists=treeListSource.getNumberOfTreeBlocks(taxa);
		if (numLists==MesquiteInteger.infinite) {
			numLists = defaultNumberOfTreeBlocks;
			if (numListsItem==null)
				numListsItem = addMenuItem( "Number of Tree Blocks...", makeCommand("setNumberTreeBlocks",  this));
		}
		else {
			numberOfTreeBlocks = numLists;
			deleteMenuItem(numListsItem);
			numListsItem=null;
		}
		return numLists; //one consensus for each list
	}
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee == treeListSource)
			parametersChanged();

	}

	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		if (itree == currentTree)
			return "Consensus tree " + (itree+1) + " from " + numTreesUsed + " trees";
		return "Consensus tree " + (itree+1);
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return"Consensus tree of trees from " + treeListSource.getName();
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new ConsCompatibilityTest();
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  //This is basically the old Consensus Tree module renamed and changed slightly
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	public boolean requestPrimaryChoice() { return false; } 
}

class ConsCompatibilityTest extends CompatibilityTest{
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (prospectiveEmployer != null)
			return prospectiveEmployer.numModulesAvailable(Consenser.class)>0;
			else
				return MesquiteTrunk.mesquiteTrunk.numModulesAvailable(Consenser.class)>0;
	}
}


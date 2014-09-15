/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.OtherTreeBlocks;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.charMatrices.lib.RandomMatrixModifier;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Supplies tree blocks from other sources.*/
public class OtherTreeBlocks extends TreeBlockSource {
	public String getName() {
		return "Other Tree Blocks";
	}
	public String getExplanation() {
		return "Supplies blocks of trees from various sources";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeBlockFiller.class, getName() + "  uses a secondary source of tree blocks.",
		"The source of tree blocks can be chosen initially");
		//e.setDivertChainMessage("testing");

	}
	/*.................................................................................................................*/
	int currentTreeBlockIndex=MesquiteInteger.unassigned;
	TreeVector currentTreeBlock = null;
	TreeVector lastUsedTreeBlock = null;
	TreeBlockFiller fillerTask = null;
	Taxa preferredTaxa =null;

	int numTrees = 100;
	MesquiteMenuItemSpec ntreesItem = null;
	boolean nTreesSet = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments ==null) {
			if (MesquiteThread.isScripting())
				fillerTask = (TreeBlockFiller)hireNamedEmployee(TreeBlockFiller.class, "#StoredTrees");
			if (fillerTask == null)
				fillerTask = (TreeBlockFiller)hireEmployee(TreeBlockFiller.class, "Source of trees for tree blocks");
		}
		else {
			fillerTask = (TreeBlockFiller)hireNamedEmployee(TreeBlockFiller.class, arguments);
			if (fillerTask == null)
				fillerTask = (TreeBlockFiller)hireEmployee(TreeBlockFiller.class, "Source of trees for tree blocks");
		}

		if (fillerTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained.");
		ntreesItem = addMenuItem( "Number of Trees...", makeCommand("setNumberTrees",   this));
		return true;
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return TreeBlockFiller.class;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa) {
		preferredTaxa = taxa;
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		setPreferredTaxa(taxa);
		fillerTask.initialize(taxa);
		if (!fillerTask.hasLimitedTrees(taxa)){
			if (!MesquiteThread.isScripting()  && !nTreesSet){
				int n = MesquiteInteger.queryInteger(containerOfModule(), "Trees per block?", "How many trees to include per tree block?", numTrees);
				if (MesquiteInteger.isCombinable(n) && n>0)
					numTrees = n;
				nTreesSet = true;
			}
		}
		ntreesItem.setEnabled(!fillerTask.hasLimitedTrees(taxa));
		MesquiteTrunk.resetMenuItemEnabling();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setTreeFiller ", fillerTask);
		temp.addLine("setNumberTrees " + numTrees);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the number of trees included in each tree block", "[number of trees]", commandName, "setNumberTrees")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set Number of Trees", "Number of Trees:", numTrees, 0, MesquiteInteger.infinite);
			if (newNum>0  && newNum!=numTrees) {
				numTrees = newNum;
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the module supplying trees", "[name of module]", commandName, "setTreeFiller")) {
			TreeBlockFiller temp=  (TreeBlockFiller)replaceEmployee(TreeBlockFiller.class, arguments, "Source of trees for tree blocks", fillerTask);
			if (temp!=null) {
				fillerTask = temp;
				initialize(preferredTaxa);
				parametersChanged(); //?
			}
			return temp;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public TreeVector getFirstBlock(Taxa taxa) {
		currentTreeBlockIndex=0;
		return getCurrentBlock(taxa);
	}
	/*.................................................................................................................*/
	public TreeVector getBlock(Taxa taxa, int ic) {
		currentTreeBlockIndex=ic;
		return getCurrentBlock(taxa);
	}
	/*.................................................................................................................*/
	public TreeVector getCurrentBlock(Taxa taxa) {
		if (fillerTask instanceof Incrementable)
			((Incrementable)fillerTask).setCurrent(currentTreeBlockIndex);
		setPreferredTaxa(taxa);
		TreeVector treeList = new TreeVector(taxa);	

		fillerTask.fillTreeBlock(treeList, numTrees); 
		return treeList;
	}
	/*.................................................................................................................*/
	public TreeVector getNextBlock(Taxa taxa) {
		currentTreeBlockIndex++;
		return getCurrentBlock(taxa);
	}
	/*.................................................................................................................*/
	public int getNumberOfTreeBlocks(Taxa taxa) {
		if (fillerTask instanceof Incrementable)
			return (int)( ((Incrementable)fillerTask).getMax() -  ((Incrementable)fillerTask).getMin() + 1);
		return 1;
	}

	/*.................................................................................................................*/
	public String getTreeBlockNameString(Taxa taxa, int index) {
		return fillerTask.getName() + "  " + currentTreeBlockIndex;//todo!  not right
	}
	/*.................................................................................................................*/
	public String getCurrentTreeBlockNameString(Taxa taxa) {
		return fillerTask.getName() + "  " + currentTreeBlockIndex; //todo!  not right
	}
}


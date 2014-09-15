/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.DeepCoalMultLoci;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;


/* ======================================================================== */
/** This evaluates a species tree by calculating how much lineage sorting is implied by a set of contained gene trees. */
public class DeepCoalMultLoci extends NumberForTree implements Incrementable {
		public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
			EmployeeNeed e2 = registerEmployeeNeed(TreeBlockSource.class, getName() + " needs a source of trees whose fit within the containing tree will be assessed.",
			"The source of other trees can be indicated initially or later under the Gene Tree Source submenu.");
			EmployeeNeed e3 = registerEmployeeNeed(AssociationSource.class, getName() + " needs a taxa association to indicate how contained taxa fit within containing taxa.",
			"The source of associations may be arranged automatically or is chosen initially.");
			EmployeeNeed e4 = registerEmployeeNeed(ReconstructAssociation.class, getName() + "needs to fit or reconstruct the contained tree within the containing tree in order to measure its fit.",
			"The reconstruction method can be specified initially.");
		}
	MesquiteNumber nt;
	TreeBlockSource treeSourceTask;
	AssociationSource associationTask;
	ReconstructAssociation reconstructTask;
	MesquiteString treeSourceName;
	TaxaAssociation association;
	int currentContainedTreeBlock = MesquiteInteger.unassigned;
	Taxa containedTaxa;
	MesquiteCommand tstC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associations obtained.");
		reconstructTask = (ReconstructAssociation)hireEmployee(ReconstructAssociation.class, "Method to reconstruct association history");
		if (reconstructTask == null)
			return sorry(getName() + " couldn't start because no association reconstructor module obtained.");
		treeSourceTask = (TreeBlockSource)hireEmployee(TreeBlockSource.class, "Source of contained trees");
		if (treeSourceTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained");
		tstC =  makeCommand("setTreeSource",  this);
		treeSourceTask.setHiringCommand(tstC);
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		if (numModulesAvailable(TreeBlockSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Gene Tree Source", tstC, TreeBlockSource.class);
			mss.setSelected(treeSourceName);
		}
 		addMenuItem( "Next Contained Tree block", makeCommand("nextContained",  this));
 		addMenuItem( "Previous Contained Tree block", makeCommand("previousContained",  this));
 		nt= new MesquiteNumber();
 		return true;
  	 }
  	 
	public void employeeQuit(MesquiteModule m){
		if (m != treeSourceTask)
			iQuit();
	}  	 
	public boolean biggerIsBetter() {
		return false;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTreeSource ", treeSourceTask); 
  	 	temp.addLine("setContained " + MesquiteTree.toExternal(currentContainedTreeBlock));
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the source of the gene tree", "[name of module]", commandName, "setTreeSource")) {
			TreeBlockSource temp = (TreeBlockSource)replaceEmployee(TreeBlockSource.class, arguments, "Source of trees", treeSourceTask);
			if (temp !=null){
				treeSourceTask = temp;
				treeSourceTask.setHiringCommand(tstC);
				treeSourceName.setValue(treeSourceTask.getName());
				parametersChanged();
    	 			return treeSourceTask;
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to next block of contained gene trees", null, commandName, "nextContained")) {
    	 		if (MesquiteInteger.isUnassigned(currentContainedTreeBlock))
    	 			currentContainedTreeBlock = -1;
    	 		setContained(currentContainedTreeBlock+1);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to previous block of contained gene trees", null, commandName, "previousContained")) {
    	 		if (MesquiteInteger.isUnassigned(currentContainedTreeBlock))
    	 			currentContainedTreeBlock = 1;
    	 		setContained(currentContainedTreeBlock-1);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to block of contained gene trees", "[number of tree]", commandName, "setContained")) {
    	 		int ic = MesquiteTree.toInternal(MesquiteInteger.fromFirstToken(arguments, pos)); 
    	 		setContained(ic);
    	 	}
 		else return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	 
   	 
	public void setContained (int index){ 
		if (containedTaxa == null)
			return;
 		if (index<((TreeBlockSource)treeSourceTask).getNumberOfTreeBlocks(containedTaxa) && index>=0){
 			currentContainedTreeBlock=index;
 			parametersChanged();
 		}
	}
	Tree lastTree;
	MesquiteTree cTree;
   	TreeVector trees;
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){
   		if (tree ==null)
   			return;
   		Taxa taxa = tree.getTaxa();
        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
        		association = associationTask.getCurrentAssociation(taxa); 
        		if (association.getTaxa(0)== taxa)
        			containedTaxa = association.getTaxa(1);
        		else
        			containedTaxa = association.getTaxa(0);
        	}
   	}
 	public void setCurrent(long i){  //SHOULD NOT notify (e.g., parametersChanged)
 		currentContainedTreeBlock = (int)i;
 	}
 	public long getCurrent(){
 		return currentContainedTreeBlock;
 	}
 	public String getItemTypeName(){
 		return "Contained tree";
 	}
 	public long getMin(){
		return 0;
 	}
 	public long getMax(){
		int nt = ((TreeBlockSource)treeSourceTask).getNumberOfTreeBlocks(containedTaxa);
		return nt -1;
 	}
 	public long toInternal(long i){
 		return i-1;
 	}
 	public long toExternal(long i){ //return whether 0 based or 1 based counting
 		return i+1;
 	}
 	MesquiteNumber cost = new MesquiteNumber();
	MesquiteString r = new MesquiteString();
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || tree == null)
    	 		return;
    	clearResultAndLastResult(result);
		cost.setToUnassigned();
		lastTree = tree;
		Taxa taxa = tree.getTaxa();
		
		//getting association & contained taxa
        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
        		association = associationTask.getCurrentAssociation(taxa); 
        		if (association == null)
        			association = associationTask.getAssociation(taxa, 0); 
        		if (association == null){
				if (resultString!=null)
					resultString.setValue("Deep coalescence (Multiple loci) not calculated (no association )");
				return;
			}
        		if (association.getTaxa(0)== taxa)
        			containedTaxa = association.getTaxa(1);
        		else
        			containedTaxa = association.getTaxa(0);
        	}
        	
        	/* need to find out for each taxon among contained taxa if it has more than one associate.  If so, then 
        	can't do calculations since gene copy in more than one species*/
		for (int i=0; i< containedTaxa.getNumTaxa(); i++){
			Taxon tax = containedTaxa.getTaxon(i);
			if (association.getNumAssociates(tax)>1){
				if (resultString!=null)
					resultString.setValue("Deep coalescence (Multiple loci) not calculated (some genes in more than one species)");
				return;
			}
		}

        	//choosing which tree block (or other incrementable unit) from the tree block source
        	if (MesquiteInteger.isUnassigned(currentContainedTreeBlock)){
        		if (MesquiteThread.isScripting())
        			currentContainedTreeBlock = 0;
        		else {
				int nt = ((TreeBlockSource)treeSourceTask).getNumberOfTreeBlocks(containedTaxa);
			   	if (nt>1 && !MesquiteThread.isScripting()) {
   					currentContainedTreeBlock = ((TreeBlockSource)treeSourceTask).queryUserChoose(containedTaxa, "Which trees to serve as source of gene trees to fit into species tree to count deep coalescences?");
   					if (MesquiteInteger.isUnassigned(currentContainedTreeBlock))
   						currentContainedTreeBlock = 0;
   				}
   				else
   					currentContainedTreeBlock = 0;
				
        		}
        	}
        	
        	//getting the contained trees
		trees = ((TreeBlockSource)treeSourceTask).getBlock(containedTaxa, currentContainedTreeBlock); 
	        if (trees==null) {
			if (resultString!=null)
				resultString.setValue("Deep coalescences: unassigned (no gene trees)");
			return;
		}
		
		//get each tree and calculate deep coalescence cost
		int numTrees = trees.size();
		MesquiteNumber sum = new MesquiteNumber(0);
		for (int iTree = 0; iTree<numTrees; iTree++){
			 //cloning the contained tree in case we need to change its resolution
			Tree containedTree = trees.getTree(iTree); 
		        if (cTree == null || cTree.getTaxa()!=containedTaxa || !(containedTree instanceof MesquiteTree))
		        	cTree = containedTree.cloneTree();//no need to establish listener to Taxa, as will be remade when needed?
		        else
		        	cTree.setToClone((MesquiteTree)containedTree);
		        reconstructTask.calculateHistoryCost(tree, cTree, association, cost, r);
			sum.add(cost);
		}
		
		result.setValue(sum);
		if (resultString!=null)
			resultString.setValue("Deep coalescence cost " + result + " (" + numTrees + " gene trees \"" + trees.getName() + "\" in species tree " + lastTree.getName() + ")");
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Counts W. Maddison's (1997) number of extra gene lineages (\"deep coalescences\") for species tree containing a series of gene trees.  The gene trees must be represented by a single taxa block.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Deep Coalescences Multiple Loci";
   	 }
}


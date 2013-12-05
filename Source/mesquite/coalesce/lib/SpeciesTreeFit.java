/* Mesquite source code.  Copyright 1997-2010 W. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;


/* ======================================================================== */
/** This evaluates a species tree by calculating how much lineage sorting is implied by a contained gene tree. */
public abstract class SpeciesTreeFit extends NumberForTree implements Incrementable {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(OneTreeSource.class, getName() + " needs a source for the contained tree (e.g. a gene tree) to assess the fit of containing trees (e.g. sp0ecies trees).",
		"The source of other trees can be indicated initially or later under the Gene Tree Source submenu.");
		EmployeeNeed e3 = registerEmployeeNeed(AssociationSource.class, getName() + " needs a taxa association to indicate how contained taxa fit within containing taxa.",
		"The source of associations may be arranged automatically or is chosen initially.");
		EmployeeNeed e4 = registerEmployeeNeed(ReconstructAssociation.class, getName() + "needs to fit or reconstruct the contained tree within the containing tree in order to measure its fit.",
		"The reconstruction method can be specified initially.");
	}
	MesquiteNumber nt;
	TreeSource treeSourceTask;
	AssociationSource associationTask;
	ReconstructAssociation reconstructTask;
	MesquiteString treeSourceName;
	TaxaAssociation association;
	int currentContained = MesquiteInteger.unassigned;
	Taxa containedTaxa;
	MesquiteCommand tstC;
	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName) {
		associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associations obtained.");
		reconstructTask = (ReconstructAssociation)hireEmployee(ReconstructAssociation.class, "Method to reconstruct association history");
		if (reconstructTask == null)
			return sorry(getName() + " couldn't start because no association reconstructor module obtained.");
		treeSourceTask = (TreeSource)hireEmployee(TreeSource.class, "Source of contained trees");
		if (treeSourceTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained");
		tstC =  makeCommand("setTreeSource",  this);
		treeSourceTask.setHiringCommand(tstC);
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Gene Tree Source", tstC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}
 		addMenuItem( "Next Contained Tree", makeCommand("nextContained",  this));
 		addMenuItem( "Previous Contained Tree", makeCommand("previousContained",  this));
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
 	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
 		if (employee != treeSourceTask || Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED)
 			super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTreeSource ", treeSourceTask); 
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the source of the gene tree", "[name of module]", commandName, "setTreeSource")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees", treeSourceTask);
			if (temp !=null){
				treeSourceTask = temp;
				treeSourceTask.setHiringCommand(tstC);
				treeSourceName.setValue(treeSourceTask.getName());
				currentContained = MesquiteInteger.unassigned;
				parametersChanged();
    	 			return treeSourceTask;
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to next contained gene tree", null, commandName, "nextContained")) {
    	 		if (MesquiteInteger.isUnassigned(currentContained))
    	 			currentContained = -1;
    	 		setContained(currentContained+1);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to previous contained gene tree", null, commandName, "previousContained")) {
    	 		if (MesquiteInteger.isUnassigned(currentContained))
    	 			currentContained = 1;
    	 		setContained(currentContained-1);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to contained gene tree", "[number of tree]", commandName, "setContained")) {
    	 		int ic = MesquiteTree.toInternal(MesquiteInteger.fromFirstToken(arguments, pos)); //TODO: 0/1 based
    	 		setContained(ic);
    	 	}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	 
   	 
	public void setContained (int index){ 
		if (containedTaxa == null)
			return;
 		if (index<((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa) && index>=0){
 			currentContained=index;
 			parametersChanged();
 		}
	}
   	public void initTaxa(Taxa taxa){
   		if (taxa ==null)
   			return;
        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
        		association = associationTask.getCurrentAssociation(taxa); 
        		if (association.getTaxa(0)== taxa)
        			containedTaxa = association.getTaxa(1);
        		else
        			containedTaxa = association.getTaxa(0);
			treeSourceTask.initialize(containedTaxa);
        	}
   	}
	Tree lastTree;
   	MesquiteTree cTree;
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){
   		if (tree ==null)
   			return;
   		initTaxa(tree.getTaxa());
   	}
 	public void setCurrent(long i){  //SHOULD NOT notify (e.g., parametersChanged)
 		currentContained = (int)i;
 	}
 	public long getCurrent(){
 		return currentContained;
 	}
 	public String getItemTypeName(){
 		return "Contained tree";
 	}
 	public long getMin(){
		return 0;
 	}
 	public long getMax(){
   		//initTaxa(preferredTaxa);
		int nt = ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa);
		if (nt == MesquiteInteger.infinite) {
			return MesquiteLong.infinite;
		}
		return nt -1;
 	}
 	public long toInternal(long i){
 		return i-1;
 	}
 	public long toExternal(long i){ //return whether 0 based or 1 based counting
 		return i+1;
 	}
	MesquiteString r = new MesquiteString();
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || tree == null)
    	 		return;
    	clearResultAndLastResult(result);
		lastTree = tree;
		Taxa taxa = tree.getTaxa();
		
		//getting the association and contained taxa
        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
        		association = associationTask.getCurrentAssociation(taxa); 
        		if (association == null)
        			association = associationTask.getAssociation(taxa, 0); 
        		if (association == null){
				if (resultString!=null)
					resultString.setValue("Deep coalescence (Species tree) not calculated (no association )");
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
					resultString.setValue("Deep coalescence not calculated (some genes in more than one species)");
				return;
			}
		}
		
        	//choosing which tree to use from the tree source
        	if (MesquiteInteger.isUnassigned(currentContained)){
        		if (MesquiteThread.isScripting())
        			currentContained = 0;
        		else {
				int nt = ((TreeSource)treeSourceTask).getNumberOfTrees(containedTaxa);
			   	if (nt>1) {
   					currentContained = ((TreeSource)treeSourceTask).queryUserChoose(containedTaxa, "Which contained tree to fit into species tree to count deep coalescences?");
   					if (MesquiteInteger.isUnassigned(currentContained))
   						currentContained = 0;
   				}
   				else
   					currentContained = 0;
				
        		}
        			
        	}
        	
        	//getting the contained tree & cloning it in case we need to change its resolution
		Tree containedTree = ((TreeSource)treeSourceTask).getTree(containedTaxa, currentContained); 
	        if (containedTree==null) {
			if (resultString!=null)
				resultString.setValue("Deep coalescences: unassigned (no gene tree)");
			return;
		}
		
		
		//calculate deep coalescence cost
	        if (cTree == null || cTree.getTaxa()!=containedTaxa || !(containedTree instanceof MesquiteTree))
	        	cTree = containedTree.cloneTree();//no need to establish listener to Taxa, as will be remade when needed?
	        else
	        	cTree.setToClone((MesquiteTree)containedTree);
	    calculateCost(reconstructTask, tree, cTree, association, result, r);
		if (resultString!=null)
			resultString.setValue(r.toString() + " (gene tree " + TreeVector.toExternal(currentContained) + " in species tree " + lastTree.getName() + ")");
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	public abstract void calculateCost(ReconstructAssociation reconstructTask, Tree speciesTree, MesquiteTree geneTree, TaxaAssociation association, MesquiteNumber result, MesquiteString r);
  	/*.................................................................................................................*/
    	 public String getParameters() {
		if (lastTree !=null)
			return "Gene tree " + TreeVector.toExternal(currentContained);
		return "";
   	 }
}


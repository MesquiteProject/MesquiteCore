/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.ContainedCoalescSim;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;
/* ======================================================================== 
For version 1.02 this was split off from Contained Coalescence to allow alternatives to be swapped more easily in the future (e.g, with migration)*/
public class ContainedCoalescSim extends ContainedTreeSim {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(Coalescer.class, getName() + " uses a coalescent simulator to simulate gene trees.",
		"The coalescent simulator is either chosen automatically or you can choose it initially.");
	}
	Coalescer coalescenceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		coalescenceTask = (Coalescer)hireEmployee(Coalescer.class, "Coalescence simulator");
		if (coalescenceTask == null)
			return sorry(getName() + " couldn't start because no coalescence module obtained.");
 		return true;
  	 }
  	 public void endJob(){
  	 	if (savedBush!=null)
  	 		savedBush.dispose();
  	 	super.endJob();
  	 }
	/*.................................................................................................................*/
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	return temp;
  	 }
  	 /*---*/
  	
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if  (checker.compare(this.getClass(), "Returns the species tree being used for contained coalescence", null, commandName, "getSpeciesTree")) {
    	 		return null;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
	/*.................................................................................................................*/
	NameReference widthNameReference = NameReference.getNameReference("width");
	DoubleArray widths = null;
	double maxWidth = 0;
	/*.................................................................................................................*/
	double branchNEAdjustment(int node){
		if (widths !=null) {
			double w = widths.getValue(node);
			if (MesquiteDouble.isCombinable(w) && w>=0) {
				return w;
			}
		}	
		return 1.0;
	}
	/*.................................................................................................................*/
	boolean first = true;
	/*.................................................................................................................*/
   	int[] containing; //length of geneTree.getNumNodeSpaces();
	/*.................................................................................................................*/
	/*  coalesces the gene tree nodes contained within the species tree node "node". */
	private synchronized int coalesceDown (Coalescer coalescer, Tree speciesTree, MesquiteTree geneTree, TaxaAssociation association, Taxa geneTaxa, int node, int root, MesquiteLong seed) {
		int nodeNumber = -2;
		double br=speciesTree.getBranchLength(node);  //for calculating the number of generations in which coalescence can occur here
		long generations=0;
		//branch length is NOT scaled by population size; thus branch length is now treated as the number of generations to coalesce
		if (MesquiteDouble.isCombinable(br) && (long)(br)>1) //&& node != speciesTree.getRoot() 
			generations =  (long)(br); 
		else 
			generations =1;
		if (speciesTree.nodeIsTerminal(node)) { //if terminal node, make CoalescedNode array, one element for each contained gene
			int taxonNum = speciesTree.taxonNumberOfNode(node);
			Taxon species =speciesTree.getTaxa().getTaxon(taxonNum);
			nodeNumber=node;
			Taxon[] containedGenes = association.getAssociates(species);
			if (containedGenes != null) {
				for (int i=0; i<containedGenes.length; i++)  {
					int taxNum = geneTaxa.whichTaxonNumber(containedGenes[i]);
					int cN = geneTree.nodeOfTaxonNumber(taxNum);
					if (!geneTree.nodeExists(cN)) {
						MesquiteMessage.warnProgrammer("GeneTree node doesn't exist ContainedCoalescence containedGenes[i] " + containedGenes[i] + " taxNum " + taxNum + " cN " + cN);
						MesquiteMessage.warnProgrammer("   Gene tree:   " + geneTree.writeTree());
					}
					containing[cN] = node;  //can't use taxonNum because if 1, screws things up
				}
			}
		}
		else {
			for (int daughter = speciesTree.firstDaughterOfNode(node); speciesTree.nodeExists(daughter); daughter = speciesTree.nextSisterOfNode(daughter)){
				int nn = coalesceDown(coalescer, speciesTree, geneTree, association, geneTaxa, daughter, root, seed);
				if (nodeNumber==-2)
					nodeNumber = nn;
				else {
					//change all occurrences of nn in containing to nodeNumber
					for (int i =0; i<containing.length; i++)
						if (containing[i]== nn) {
							if (!geneTree.nodeExists(i))
								MesquiteMessage.warnProgrammer("GeneTree node doesn't exist ContainedCoalescence nn " + nn);
							containing[i]=nodeNumber;
						}
				}
			}
		}
		// now about to coalesce the gene tree nodes listed in containing as belonging to nodeNumber.
		if (MesquiteLong.isInfinite(generations)) 
			coalescer.coalesce(geneTree, containing, nodeNumber, branchNEAdjustment(node), 0, seed, true); //speciesTree.getRoot() == node);
		else 
			coalescer.coalesce(geneTree, containing, nodeNumber,  branchNEAdjustment(node), generations, seed, root == node); //speciesTree.getRoot() == node);
		return nodeNumber;
	}
	/*.................................................................................................................*/
   	MesquiteTree savedBush;
   	long oldTaxaVersion = -1;
   	boolean inProgress = false;
   	TreeReference speciesTreeRef, oldSpeciesTreeRef;
	/*.................................................................................................................*/
	public MesquiteTree simulateContainedTree (Tree speciesTree, Tree geneTree, TaxaAssociation association, Taxa geneTaxa, MesquiteLong seed){
   		if (speciesTree == null || association == null || geneTaxa == null)
   			return null;
		if (inProgress)
			return null;
		inProgress = true;
		if (speciesTree instanceof MesquiteTree)
			speciesTreeRef = ((MesquiteTree)speciesTree).getTreeReference(speciesTreeRef);
		else
			speciesTreeRef = null;
		widths = speciesTree.getWhichAssociatedDouble(widthNameReference);
		MesquiteTree t=null;
	
		if (geneTree==null || !(geneTree instanceof MesquiteTree)) 
			 t = new MesquiteTree(geneTaxa);
		else
			t = (MesquiteTree)geneTree;
		if ((speciesTreeRef == null || oldSpeciesTreeRef == null || !speciesTreeRef.equals(oldSpeciesTreeRef)) || savedBush==null || savedBush.getTaxa()!=t.getTaxa() || oldTaxaVersion != t.getVersionNumber()) {
			if (savedBush != null)
				savedBush.dispose();
			t.setToDefaultBush(t.getNumTaxa(), false);

			oldTaxaVersion = t.getVersionNumber();
			Taxon[] assocs = null;
				for (int i=0; i<geneTaxa.getNumTaxa(); i++){
			
				assocs = association.getAssociates(geneTaxa.getTaxon(i), assocs);  //get containing taxa

				//if any of containing taxa in speciesTree then keep, otherwise delete contained taxon from savedbush
				if (!inSpeciesTree(speciesTree, assocs, speciesTree.getTaxa())){
					t.snipClade(t.nodeOfTaxonNumber(i), false);
				}
			}
			savedBush = t.cloneTree();//no need to establish listener to Taxa, as will be remade when needed?
		}
		else {
			t.setToClone(savedBush);
		}
		oldSpeciesTreeRef = speciesTreeRef;
		if (containing ==null || containing.length!=t.getNumNodeSpaces())
			containing = new int[t.getNumNodeSpaces()];
		for (int i=0; i<containing.length; i++)
			containing[i] = 0;
		int root = speciesTree.getRoot();
		coalesceDown(coalescenceTask, speciesTree, t, association, geneTaxa, root, root, seed);
   		inProgress = false;
   		return t;
   	}
   	public boolean inSpeciesTree(Tree speciesTree, Taxon[] assocs, Taxa taxa){
   		if (assocs == null)
   			return false;
   		for (int i=0; i<assocs.length; i++){
   			int t = taxa.whichTaxonNumber(assocs[i]);
   			if (speciesTree.nodeOfTaxonNumber(t)>0)
   				return true;
   		}
  		return false;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Coalescence Simulated within Current Tree";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Generates tree by a simple coalescence model of a neutral gene with constant population size, within species trees. "
 		+"Branch lengths are assigned according to generation of coalescence." 
 		+"The species tree used is a current tree found in a Tree Window or other tree context." ;
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getParameters() {
		if (coalescenceTask!=null)
			return "Coalescence simulated by: " + coalescenceTask.getName();
		return "";
   	 }
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
 	/*.................................................................................................................*/
   	 /**@return	<p>The base population size from {@link #coalescenceTask}, if available. Returns {@code MesquiteInteger.unassigned} 
   	  * if {@code coalescenceTask == null}.</p>*/
   	 public int getPopulationSize() {
   		 if (coalescenceTask != null) {
   			 return coalescenceTask.getPopulationSize();
   		 } else {
   			 return MesquiteInteger.unassigned;
   		 }
   	 }
}



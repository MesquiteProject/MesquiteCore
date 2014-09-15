/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.SharedPartitions2Trees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;



/* ======================================================================== */
public class SharedPartitions2Trees extends DistanceBetween2Trees {
	StringArray terminalsAbove, terminalsBelow, otherTerminalsAbove, otherTerminalsBelow;
	boolean isDistance = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		isDistance = (getHiredAs() == DistanceBetween2Trees.class);
 		return true;
  	 }
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	public boolean largerIsFurther(){  
		return false;
	}
	/*.................................................................................................................*/
  	 private boolean arraysMatch(StringArray one, StringArray two){
   	 	return arrayIsSubset(one, two) && arrayIsSubset(two, one);
 	 }
	/*.................................................................................................................*/
  	 private boolean arrayIsSubset(StringArray one, StringArray two){
  	 	if (one==null || two == null)
  	 		return false;
  	 	for (int i=0; i<one.getSize(); i++){
  	 		if (one.getValue(i)!=null && two.indexOf(one.getValue(i))<0)
  	 			return false;
  	 	}
  	 	return true;
  	 }
	/*.................................................................................................................*/
  	 private void accumulateTerminals(Tree tree,int node, int target, boolean aboveTarget, StringArray terminalsAbove, StringArray terminalsBelow){
		if (tree.nodeIsTerminal(node)){
			if (aboveTarget)
				terminalsAbove.fillNextUnassigned(tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(node)));
			else
				terminalsBelow.fillNextUnassigned(tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(node)));
			
		}
		else for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				accumulateTerminals(tree, daughter, target, (node==target || aboveTarget), terminalsAbove, terminalsBelow);
  	 }
	/*.................................................................................................................*/
  	 /* Returns true if partitions are consistent (same taxa in each subset in each tree, or group in tree1 is subset of
  	 group in tree 2 AND other group in 1 is subset of other group in 2*/
  	 private boolean partitionsConsistent(Tree tree, int node, Tree otherTree, int otherNode){
		terminalsAbove.deassignArray();
		terminalsBelow.deassignArray();
		otherTerminalsAbove.deassignArray();
		otherTerminalsBelow.deassignArray();
		// first, load arrays with node numbers in taxa on either side of partition defined by above and below node
		accumulateTerminals(tree, tree.getRoot(), node, false, terminalsAbove, terminalsBelow);
		accumulateTerminals(otherTree, otherTree.getRoot(), otherNode, false, otherTerminalsAbove, otherTerminalsBelow);
		
		// Next, compare arrays to see that same
		if (arraysMatch(terminalsAbove, otherTerminalsAbove)){
			return (arraysMatch(terminalsBelow, otherTerminalsBelow)|| arrayIsSubset(terminalsBelow, otherTerminalsBelow) || arrayIsSubset(otherTerminalsBelow, terminalsBelow));
		}
		else if (arraysMatch(terminalsBelow, otherTerminalsBelow)){
			return (arrayIsSubset(terminalsAbove, otherTerminalsAbove) || arrayIsSubset(otherTerminalsAbove, terminalsAbove));
		}
		else if (arraysMatch(terminalsAbove, otherTerminalsBelow)){
			return (arraysMatch(terminalsBelow, otherTerminalsAbove)|| arrayIsSubset(terminalsBelow, otherTerminalsAbove) || arrayIsSubset(otherTerminalsAbove, terminalsBelow));
		}
		else if (arraysMatch(terminalsBelow, otherTerminalsAbove)){
			return (arraysMatch(terminalsAbove, otherTerminalsBelow)|| arrayIsSubset(terminalsAbove, otherTerminalsBelow) || arrayIsSubset(otherTerminalsBelow, terminalsAbove));
		}
		else if (arrayIsSubset(terminalsAbove, otherTerminalsAbove)){
			return (arrayIsSubset(terminalsBelow, otherTerminalsBelow));
		}
		else if (arrayIsSubset(otherTerminalsAbove, terminalsAbove)){
			return (arrayIsSubset(otherTerminalsBelow, terminalsBelow));
		}
		else
			return false;
  	 }
  	 
  	 
	/*.................................................................................................................*/
  	 private boolean consistentPartitionFound(Tree tree,int node,  Tree otherTree, int otherNode){
		if (otherTree.nodeIsInternal(otherNode)){
			if (otherNode != otherTree.getRoot() && partitionsConsistent(tree, node, otherTree, otherNode)) {
				return true;
			}
			
			boolean found = false;
			for (int daughter = otherTree.firstDaughterOfNode(otherNode); otherTree.nodeExists(daughter) && !found; daughter = otherTree.nextSisterOfNode(daughter)) {
				found = consistentPartitionFound(tree, node, otherTree, daughter);
				if (found)
					return true;
			}
			
		}
		return false;
  	 }
  	 private void visitOriginal(Tree tree,int node,  Tree otherTree, MesquiteInteger numConsistent){
		if (tree.nodeIsInternal(node)){
			boolean count = (node != tree.getRoot() && tree.numberOfDaughtersOfNode(tree.motherOfNode(node))!=1);
			boolean dichotDescOfRoot =tree.motherOfNode(node)==tree.getRoot() &&  !tree.nodeIsPolytomous(tree.getRoot());
			if (dichotDescOfRoot) {
				count = count && !(tree.nodeIsFirstDaughter(node));
				int sis = tree.nextSisterOfNode(node);
				if (!tree.nodeExists(sis))
					sis = tree.previousSisterOfNode(node);
				count = count && !tree.nodeIsTerminal(sis);
			}
			if (count && consistentPartitionFound(tree, node, otherTree, otherTree.getRoot()))
				numConsistent.increment();
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				visitOriginal(tree, daughter, otherTree, numConsistent);
			
		}
  	 }
  	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree t1, Tree t2) {
	
	}
	
	MesquiteTree tree1eq, tree2eq;
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree1, Tree tree2, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (tree1 == null)
			return;
		if (tree2 == null)
			return;
			
		Tree use1, use2;
		int numTaxa1 = tree1.getTaxa().getNumTaxa();
		int numTaxa2 = tree2.getTaxa().getNumTaxa();
		
		if (numTaxa1==numTaxa2 && numTaxa1 == tree1.numberOfTerminalsInClade(tree1.getRoot()) && numTaxa2 == tree2.numberOfTerminalsInClade(tree2.getRoot())){ //trees have full set of taxa)
			use1 = tree1;
			use2 = tree2;
		}
		else {  //this section added 1.02, April 2004 because was miscounting partitions if two trees had different terminals included
			if (tree1eq == null || tree1eq.getTaxa()!= tree1.getTaxa())
				tree1eq = new MesquiteTree(tree1.getTaxa());
			tree1eq.setToCloneFormOnly(tree1);
			if (tree2eq == null || tree2eq.getTaxa()!= tree2.getTaxa())
				tree2eq = new MesquiteTree(tree2.getTaxa());
			tree2eq.setToCloneFormOnly(tree2);
			
			int[] termsIn1 = tree1eq.getTerminalTaxa(tree1eq.getRoot());
		
			int[] termsIn2  = tree2eq.getTerminalTaxa(tree2eq.getRoot());
			for (int i=0; i<termsIn2.length; i++)
				if (IntegerArray.indexOf(termsIn1, termsIn2[i])<0) //terminal in 2 not found in 1; delete it from tree 2
					tree2eq.deleteClade(tree2eq.nodeOfTaxonNumber(termsIn2[i]), false);
			
			for (int i=0; i<termsIn1.length; i++)
				if (IntegerArray.indexOf(termsIn2, termsIn1[i])<0) //terminal in 1 not found in 2; delete it from tree 1
					tree1eq.deleteClade(tree1eq.nodeOfTaxonNumber(termsIn1[i]), false);
			//CHECK THAT BOTH TREES STILL EXIST
			use1 = tree1eq;
			use2 = tree2eq;
		}
		
		int numTerms = MesquiteInteger.maximum(use1.getTaxa().getNumTaxa(), use2.getTaxa().getNumTaxa());
		if (terminalsAbove==null || numTerms > terminalsAbove.getSize()) {
			terminalsAbove = new StringArray(numTerms);
			terminalsBelow = new StringArray(numTerms);
			otherTerminalsAbove = new StringArray(numTerms);
			otherTerminalsBelow = new StringArray(numTerms);
		}
		
		
		MesquiteInteger numCon = new MesquiteInteger(0);
		visitOriginal(use1, use1.getRoot(), use2, numCon);
		int numC = numCon.getValue();
		if (isDistance) {
			int numInt1 = use1.numberOfInternalsInClade(use1.getRoot());
			if (!use1.nodeIsPolytomous(use1.getRoot()))
				numInt1--;
			int numInt2 = use2.numberOfInternalsInClade(use2.getRoot());
			if (!use2.nodeIsPolytomous(use2.getRoot()))
				numInt2--;
			int maxPart = MesquiteInteger.maximum(numInt1, numInt2);
			numC = maxPart-1 - numC;  //to convert to a distance
		}
		result.setValue(numC);
		if (resultString!=null) {
			if (isDistance)
				resultString.setValue("Shared Partitions (converted to distance): "+ result.toString());
			else
				resultString.setValue("Shared Partitions: "+ result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	 
	/*.................................................................................................................*/
    	 public String getParameters() {
		if (isDistance)
			return "Shared Partitions converted to distance (Max. possible - observed)";
		else
			return "Shared Partitions in natural form (i.e., not converted to distance, but instead indicating number of partitions shared between trees)";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Shared Partitions";
   	 }
	/*.................................................................................................................*/
  	 public String getAuthors() {
		return "W. P. Maddison (slightly adjusted by J. Klingner)";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Calculates the number of shared partitions between two trees.  If used as a distance, then converted by subtracting shared partitions from the maximum possible, i.e. the number of partitions in the subtree of shared taxa in the tree with the most partitions.";
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
}


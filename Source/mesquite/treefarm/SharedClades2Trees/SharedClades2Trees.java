/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.SharedClades2Trees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;



/* ======================================================================== */
public class SharedClades2Trees extends DistanceBetween2Trees {
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
  	 private void visitOriginal(Tree tree,int node,  Tree otherTree, MesquiteInteger numConsistent){
		if (tree.nodeIsInternal(node)){
			Bits b = tree.getTerminalTaxaAsBits(node);
			if (otherTree.isClade(b))
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

		MesquiteInteger numCon = new MesquiteInteger(0);
		visitOriginal(tree1, tree1.getRoot(), tree2, numCon);
		if (tree1.getTerminalTaxaAsBits(tree1.getRoot()).equals(tree2.getTerminalTaxaAsBits(tree2.getRoot())))
			numCon.decrement();
		int numC = numCon.getValue();
		result.setValue(numC);
		if (resultString!=null) {
			resultString.setValue("Shared Clades: "+ result.toString());
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
		return "Shared Clades";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Shared Clades";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Calculates the number of shared clades between two trees (excludes the clade consisting of all taxa).";
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
}


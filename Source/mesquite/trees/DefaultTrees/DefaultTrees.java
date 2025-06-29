/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.DefaultTrees;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.Notification;
import mesquite.lib.duties.TreeSource;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;

/** Supplies default trees (e.g., ladder, bush).  Used as a last resort tree source. */
public class DefaultTrees extends TreeSource {  
	int currentTree=0; //AS OF 1. 06 the default is symmetrical to prevent deepest recursions with large trees (StackOverflowError)
	Taxa currentTaxa;
	static int BUSH = 1;
	static int LADDER = 2;
	static int SYMM = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		return true;
  	 }
  	 
	/*.................................................................................................................*/
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmeticOrSelection(notification))
			return;
		if (obj == currentTaxa) {
				parametersChanged(notification);
		}
	}
	 public boolean permitSeparateThreadWhenFilling(){
		 return false;
	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
     /**Returns whether or not the source can handle asking for the last tree, i.e. for what the source says is maxTrees - 1, even if that is unassigned or infinite, i.e., is not a combinable number. 
      * If asked, and the source has an indefinite number, it will supply a tree (e.g. from a live file) rather than just trying forever. 
      * Used for Pin to Last Tree in BasicTreeWindow.*/
    	public boolean permitsRequestForLastTree(Taxa taxa){
    		return true;
    	}

 	/* ................................................................................................................. */
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
 		if (checker.compare(this.getClass(), "Hires a tree display assistant module", "[name of assistant module]", commandName, "laxOff")) {
 		}
 		else
 			return super.doCommand(commandName, arguments, checker);
 		return null;
 	}

   	 
   	 void formSymmetricalClade(MesquiteTree tree, int minTaxon, int maxTaxon){
 		int range = maxTaxon-minTaxon + 1;
 		if (range > 1) {
 			int newRight = minTaxon + range/2;
 			tree.splitTerminal(minTaxon, newRight, false);
 			formSymmetricalClade(tree, minTaxon, newRight -1);
 			formSymmetricalClade(tree, newRight, maxTaxon);
 		}
   	 }
	/*.................................................................................................................*/
  	public void setPreferredTaxa(Taxa taxa){
	 		if (currentTaxa!=null)
	  			currentTaxa.removeListener(this);
	  		currentTaxa = taxa;
  			if (taxa != null)
  				currentTaxa.addListener(this);
  	}
	public void endJob(){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		super.endJob();
	}
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   	}
	/*.................................................................................................................*/
   	public Tree getTree(Taxa taxa, int itree) {
   		if (taxa == null)
   			return null;
   		currentTree = itree;
   		if (itree==BUSH) {
			MesquiteTree tree = new MesquiteTree(taxa);
			tree.setToDefaultBush(taxa.getNumTaxa(), false);
			tree.setName("Default bush");
	   		return tree;
   		}
   		else if (itree==LADDER) {
			MesquiteTree tree = new MesquiteTree(taxa);
			tree.setToDefaultLadder(taxa.getNumTaxa(), false);
			tree.setName("Default ladder");
	   		return tree;
   		}
   		else { //Symmetrical
   			int numTaxa = taxa.getNumTaxa();
			MesquiteTree tree = new MesquiteTree(taxa);
			if (numTaxa == 1){
				tree.setToDefaultBush(1, false);
				return tree;
			}
			tree.setToDefaultBush(2, false);
			int secondHalf = numTaxa/2;
			int rightNode = tree.nextSisterOfNode(tree.firstDaughterOfNode(tree.getRoot()));
			tree.setTaxonNumber(rightNode, secondHalf, false);
			formSymmetricalClade(tree, 0, secondHalf-1);
			formSymmetricalClade(tree, secondHalf, tree.getTaxa().getNumTaxa()-1);
			tree.setName("Default symmetrical");
	   		return tree;
   		}
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		return 3;
   	}
   
	/*.................................................................................................................*/
   	public String getTreeNameString(Taxa taxa, int itree) {
   		if (itree==BUSH)
			return "Default bush";
		else if (itree==LADDER)
			return "Default ladder";
		else
			return "Default symmetrical";
   	}
	/*.................................................................................................................*/
   	public String getCurrentTreeNameString() {
   		if (currentTree==BUSH)
			return "Default bush";
		else if (currentTree==LADDER)
			return "Default ladder";
		else
			return "Default symmetrical";
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Default Trees";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies simple default trees (bush, ladder).";
   	 }
   	 
}


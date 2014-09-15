/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.RandomResolve;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class RandomResolve extends RndTreeModifier {
	double proportion = 0.0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		if (!MesquiteThread.isScripting()){
    	 	double s = query();
	 		if (MesquiteDouble.isCombinable(s))
	 			proportion = s;
	 		else
				return false;
		}
		addMenuItem("Height Proportion for Polytomy Resolutions...", makeCommand("setProportion",  this));
  		return true;
  	 }
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	double query(){
   	 	double s = MesquiteDouble.queryDouble(containerOfModule(), "Proportion of Height from Polytomy", "How high should resolved branches be moved?  In resolving polytomies, sister branches are joined together.  " +
	 			" If 0 is entered, then they are joined together such that they retain their full length, and the new ancestral branch is of 0 length.  This effectively is not resolving the polytomy, but may help permit some calculations. "
	 			+ " If 0.5 is entered, then they are joined together such that the new ancestral branch is half the length of the shorter of the two sister branches, and each sister branch is shortened accordingly to maintain height from root.", proportion, 0.0, 1.0);
   	 	return s;
	}
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) { 
  	 	Snapshot temp = super.getSnapshot(file);
 	 	temp.addLine("setProportion " + proportion);
 	 	return temp;
 	 }
	/*.................................................................................................................*/
   	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Height Proportion for Polytomy Resolutions", "[number]", commandName, "setProportion")) {
   	 		double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
   	 		if (!MesquiteDouble.isCombinable(s)){
   	 			s = query();
   	 		}
   	 		if (MesquiteDouble.isCombinable(s)){
   	 			proportion = s;
 				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
   	 	}
   	 	else
   	 		return  super.doCommand(commandName, arguments, checker);
   	 	return null;
   	 }
 	/*.................................................................................................................*/
	void visitPolytomies(MesquiteTree tree, int node, RandomBetween rng){
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			visitPolytomies(tree, d, rng);
		while (tree.nodeIsPolytomous(node)){
			int numDaughters = tree.numberOfDaughtersOfNode(node);
			int sisterOne = rng.randomIntBetween(0, numDaughters-1);
			int sisterTwo = rng.randomIntBetween(0, numDaughters-2);
			if (sisterTwo>=sisterOne)
				sisterTwo++;
			int dOne = tree.indexedDaughterOfNode(node, sisterOne);
			int dTwo = tree.indexedDaughterOfNode(node, sisterTwo);
			tree.moveBranch(dOne, dTwo, false, true, proportion);  //
			
		}
		
	}
	/*.................................................................................................................*/
   	 public void modifyTree(Tree tree, MesquiteTree modified, RandomBetween rng){
   		if (tree == null || modified == null)
   			return;
     	// visit each polytomy.  For each, choose pair of descendants to join.  Do repeatedly until polytomy is resolved
		visitPolytomies(modified, modified.getRoot(), rng);
   	}
	/*.................................................................................................................*/
  	  public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
  	  public String getName() {
		return "Randomly Resolve Polytomies";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Randomly resolves polytomies in tree.  All possible resolutions are equiprobable. Thus, if the tree is a polytomous bush, the resulting resolved trees will be distributed equivalently to that from the Equiprobable Trees module.";
   	 }
   	 
}


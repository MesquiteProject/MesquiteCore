/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.RandomBranchMoves;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.ColorDistribution;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class RandomBranchMoves extends RndTreeModifier {
	int numMoves=1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		if (!MesquiteThread.isScripting()){
	    	 	int s = MesquiteInteger.queryInteger(containerOfModule(), "Number of random branch moves", "Enter the number of random branch moves", numMoves);
 	 		if (MesquiteInteger.isCombinable(s))
 	 			numMoves = s;
 	 		else
 				return false;
  		}
  		addMenuItem("Number of Random branch moves...", makeCommand("setNumberMoves",  this));
  		return true;
  	 }
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = super.getSnapshot(file);
  	 	temp.addLine("setNumberMoves " + numMoves);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	if (checker.compare(this.getClass(), "Sets the number of random branch moves", "[number]", commandName, "setNumberMoves")) {
    	 		int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
    	 		if (!MesquiteInteger.isCombinable(s)){
    	 			s = MesquiteInteger.queryInteger(containerOfModule(), "Number of moves", "Enter the number of random branch moves", numMoves);
    	 		}
    	 		if (MesquiteInteger.isCombinable(s)){
    	 			numMoves = s;
 				parametersChanged(); 
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
	/*.................................................................................................................*/
	NameReference colorNameRef = NameReference.getNameReference("color");
	private void setColor(Tree tree, int node){
		if (tree == null || !(tree instanceof Associable))
			return;
		Associable aTree = (Associable)tree;
		if (aTree.getWhichAssociatedLong(colorNameRef)==null)
			aTree.makeAssociatedLongs("color");
		aTree.setAssociatedLong(colorNameRef, node, ColorDistribution.numberOfRed, true);
	}
	/*.................................................................................................................*/
   	 public void modifyTree(Tree tree, MesquiteTree modified, RandomBetween rng){
   		if (tree == null || modified == null)
   			return;
		int numBranches =tree.numberOfNodesInClade(tree.getRoot());
		int numTerm =tree.numberOfTerminalsInClade(tree.getRoot());
		
		if (numBranches == 1)
			return;
   		for (int it = 0; it<numMoves; it++) {
   			int branchFrom;
   			int branchTo;
   			int count = 0;
   			boolean success = false;
   			do {
   				branchFrom = modified.nodeInTraversal(rng.randomIntBetween(0, numBranches), modified.getRoot());
   				branchTo = modified.nodeInTraversal(rng.randomIntBetween(0, numBranches), modified.getRoot());
   				count++;
   				if (count%10000 == 0){
   					MesquiteMessage.printStackTrace("Possible problem: in Random Branch Moves,  count >10000");
   				}
   				if (count<20001)
   					success = modified.moveBranch(branchFrom, branchTo, false);
   				if (success)
   					setColor(modified, branchFrom);
   			}
   			while (count<20001 && !success); //each attempt at a move is guaranteed to make a real move; thus from a 2-2 symm tree of four, 1 move is guaranteed to make 1-3 asymm
		//	modified.moveBranch(branchFrom, branchTo, false);
   		}
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		return"Number of random branch moves: " + numMoves;
   	}
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Random Branch Moves";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Rearranges tree by random branch moves.";
   	 }
   	 
}


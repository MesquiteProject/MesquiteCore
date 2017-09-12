/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

created:
25 May 08 (DRM)
 */

package mesquite.treefarm.NumSPRRearrangements;


import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumSPRRearrangements extends NumberForTree {
	boolean nodeIsRoot = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** This method is more for insurance = it guarantees that the root is used if that is what is needed.  Will avoid problems if root node has its number changed. */
	private int getBaseNode(Tree tree, int baseNode) {
		if (nodeIsRoot)
			return tree.getRoot();
		else
			return baseNode;
	}

	/*.................................................................................................................*/
	private void upCountTotal(Tree tree, int baseNode, int node,MesquiteLong total, int numNodesTotal){
		total.add(numberOfMovesNodeCanMake(tree, getBaseNode(tree,baseNode), node));
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			upCountTotal(tree, getBaseNode(tree,baseNode), d, total, numNodesTotal);
		}
	}
	
	/*.................................................................................................................*/
	/** Returns the number of moves a node can make.  Is number of nodes in the tree outside of its own clade (since node can't move into its own
	clade) and, if its mother is dichotomous, minus 2 (if dichtomous, it can't move onto its own ancestor since it's already there, and 
	it can move onto a sister only if it has more than one sister).*/
	private long numberOfMovesNodeCanMake(Tree tree,int baseNode,  int node){
		int root = getBaseNode(tree,baseNode);
		if (!tree.nodeExists(node) || root==node)
			return 0;
		int numInClade = tree.numberOfNodesInClade(node);
		int numInTree = tree.numberOfNodesInClade(root);
		int mother = tree.motherOfNode(node);
		if (!tree.nodeIsPolytomous(mother))
			return numInTree-numInClade-2;
		else
			return numInTree-numInClade;
	}
	/* TODO: currently this only works if baseNode = root */
	//  Bad things (e.g. thread death) can happen if baseNode != tree.getRoot(). J.C. Oliver 2013.
	/** baseNode <strong>must</strong> be the root of {@code tree}.*/
	public long numberOfRearrangements(Tree tree, int baseNode) {
		nodeIsRoot = baseNode == tree.getRoot();
		MesquiteLong counter = new MesquiteLong(0); 
		upCountTotal(tree, baseNode, baseNode, counter, tree.numberOfNodesInClade(baseNode));
		return counter.getValue();
	}

	/*.................................................................................................................*/
	/** Returns the number of rearrangments.*/
	public long numberOfRearrangements(Tree tree){
		nodeIsRoot = true;
		return numberOfRearrangements(tree, tree.getRoot());
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
	}
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || tree==null)
			return;
		clearResultAndLastResult(result);
		long numSPRs = numberOfRearrangements(tree);
		result.setValue(numSPRs);
		if (resultString!=null)
			resultString.setValue("Number of SPR Rearrangements: "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 330;  
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Number of SPR Rearrangements";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Calculates the number of Subtree-Pruning-Regrafting branch rearrangements that can be done to a tree.";
	}
}

/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.trees.BLFromNodeAgeConstraints;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.Tree;

/* ======================================================================== */
public class BLFromNodeAgeConstraints extends BranchLengthsAltererMult {
	double resultNum;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		double[] minAges = new double[tree.getNumNodeSpaces()];
		double[] maxAges = new double[tree.getNumNodeSpaces()];
		setByDeepest(tree, tree.getRoot(), minAges, maxAges);
		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	NameReference nodeAgeConstrRef = NameReference.getNameReference("nodeAgeConstraints");
	void getConstraint(Tree tree, int node, MesquiteDouble min, MesquiteDouble max){
		min.setToUnassigned();
		max.setToUnassigned();
		String constraint = (String)tree.getAssociatedString(nodeAgeConstrRef, node);
		// one number: fixed age
		// 0-max
		// min+
		// min-max
		if (!StringUtil.blank(constraint)){
			String token = parser.getFirstToken(constraint);
			double first = MesquiteDouble.fromString(token);
			double second = MesquiteDouble.unassigned;
			if (!MesquiteDouble.isCombinable(first)) {
				return;
			}
			token = parser.getNextToken();
			if (StringUtil.blank(token))
				second = first;
			else {
				if (token.equals("+"))
					second = MesquiteDouble.infinite;
				else if (token.equals("-")){
					token = parser.getNextToken();
					if (StringUtil.blank(token))
						second = MesquiteDouble.infinite;
					else {
						second = MesquiteDouble.fromString(token);
						if (!MesquiteDouble.isCombinable(second))
							second = MesquiteDouble.infinite;
					}
				}
			}
			min.setValue(first);
			max.setValue(second);
		}
	}
	/*.................................................................................................................*/
	private void setByDeepest(AdjustableTree tree, int node, double[] minAges, double[] maxAges){
		MesquiteDouble min = new MesquiteDouble();
		MesquiteDouble max = new MesquiteDouble();
		getConstraint(tree, node, min, max);
		minAges[node] = min.getValue();
		maxAges[node] = max.getValue();
		if (tree.nodeIsTerminal(node) && !MesquiteDouble.isCombinable(minAges[node])  && !MesquiteDouble.isCombinable(maxAges[node])) {
			minAges[node] = 0;
			maxAges[node] = 0;
		}
		double maxDepth = 0;
		for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
			setByDeepest(tree, daughter, minAges, maxAges);
			if (maxDepth<minAges[daughter])  //minages push down
				maxDepth = minAges[daughter];
		}
		if (tree.nodeIsInternal(node) && !MesquiteDouble.isCombinable(minAges[node])) {
			double spacer = (1.0 + 1.0/tree.numberOfTerminalsInClade(node));
			if (spacer > 1.1)
				spacer = 1.1;
			minAges[node] = maxDepth*spacer;
		}
		for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
			tree.setBranchLength(daughter, minAges[node] - minAges[daughter], false);
		}
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Enforce Minimum Node Age Constraints";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Uses minimum node age constraints to set the branch lengths on the tree." ;
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 273;  
	}

}

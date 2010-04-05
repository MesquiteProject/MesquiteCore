/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/
package mesquite.trees.BLFromNodeAgeConstraints;

import mesquite.lib.*;
import mesquite.lib.duties.*;

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
		return true;
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
		String constraint = (String)tree.getAssociatedObject(nodeAgeConstrRef, node);
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
}

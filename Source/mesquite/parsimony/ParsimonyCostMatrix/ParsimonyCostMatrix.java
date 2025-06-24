/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ParsimonyCostMatrix;
/*~~  */

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalHistory;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Number2DArray;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.characters.CharacterHistory;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.tree.Tree;
import mesquite.parsimony.lib.CostMatrixModel;
import mesquite.parsimony.lib.ParsAncStatesForModel;

/* ======================================================================== */
/** Based largely on, and tested against, MacClade's code for ordered categorical characters*/
public class ParsimonyCostMatrix extends ParsAncStatesForModel {
	public String getName() {
		return "Parsimony Stepmatrix";
	}
	public String getExplanation() {
		return "Reconstructs ancestral states of categorical characters using a stepmatrix (cost matrix)." ;
	}
	/*.................................................................................................................*/
	CharacterDistribution observedStates;
	int maxState = CategoricalState.maxCategoricalState;
	Number2DArray downCost, upCost, finalCost;
	MesquiteNumber tempNum1, tempNum2, tempNum3, tempNum4, tempNum5;
	CostMatrixModel model;
	static boolean alertSoft = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		tempNum1 = new MesquiteNumber(0);
		tempNum2 = new MesquiteNumber(0);
		tempNum3 = new MesquiteNumber(0);
		tempNum4 = new MesquiteNumber(0);
		tempNum5 = new MesquiteNumber(0);
		return true;
	}

	/*.................................................................................................................*/
	/*Calculates best cost from ancestral node A through descendant N and above, given the downpass costs stored in costsAbove
	and given that stateA is at node A*/
	private MesquiteNumber bestAbove(int stateA, int N, Number2DArray costsAbove, MesquiteNumber result) {
		result.setToInfinite();
		for (int state = 0; state <= maxState; state++) {
			tempNum1 = model.getTransitionValue(stateA, state, tempNum1);
			costsAbove.placeValue(N, state, tempNum2);
			tempNum1.add(tempNum2);
			result.setMeIfIAmMoreThan(tempNum1);
		}
		return result;
	}
	/*.................................................................................................................*/
	//returns distance from state1 to 2 to 3, or 1 to 3 to 2, or 1 to 3 and 1 to 2, whichever is shortest
	private void cascadeDistance(int state1, int state2, int state3, MesquiteNumber result, MesquiteNumber temp1, MesquiteNumber temp2){
		if (state1==state2) {
			model.getTransitionValue(state2, state3, result);
		}
		else if (state1==state3){
			model.getTransitionValue(state3, state2, result);
		}
		else {
			model.getTransitionValue(state1, state2, temp1);
			model.getTransitionValue(state2, state3, temp2);
			temp2.add(temp1);

			model.getTransitionValue(state1, state3, temp1);
			model.getTransitionValue(state3, state2, result);
			result.add(temp1);

			result.setMeIfIAmMoreThan(temp2);

			model.getTransitionValue(state1, state2, temp1);
			model.getTransitionValue(state1, state3, temp2);
			temp2.add(temp1);

			result.setMeIfIAmMoreThan(temp2);
		}
	}
	/*.................................................................................................................*/
	MesquiteNumber minToOthers = new MesquiteNumber();
	/*Sets downpass costs at terminal nodes.  Uses rules as in MacClade*/
	private void setTerminalDownCosts(int N, Tree tree, MesquiteBoolean dangerousPolymorphism){
		long stateSetObserved = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(N));
		if (CategoricalState.isUnassigned(stateSetObserved) || CategoricalState.isInapplicable(stateSetObserved))
			for (int i = 0; i<=maxState; i++)
				downCost.setToZero(N, i);
		else {
			//uncertainty: allow any of possible states with cost 0
			if (CategoricalState.isUncertain(stateSetObserved)){
				//for all states in state set observed, place 0 in cost; otherwise place infinite
				for (int i = 0; i<=maxState; i++)
					if (CategoricalState.isElement(stateSetObserved, i))
						downCost.setToZero(N, i);
					else 
						downCost.setToInfinite(N, i);
			}
			else {
				int card = CategoricalState.cardinality(stateSetObserved);
				//polymorphism of two states; for each state put cost to other or other two states
				if (card == 2) {
					//two in set; in one, put cost to two
					int min = CategoricalState.minimum(stateSetObserved);
					int max = CategoricalState.maximum(stateSetObserved);
					for (int i = 0; i<=maxState; i++) {
						cascadeDistance(i, min, max, minToOthers, tempNum2, tempNum3);
						downCost.setValue(N, i, minToOthers);
					}
				}
				else  if (card == 1) {
					//monomorphic in set; put infinite except for that state
					for (int i = 0; i<=maxState; i++)
						downCost.setToInfinite(N, i);
					int min = CategoricalState.minimum(stateSetObserved);
					downCost.setToZero(N, min);
				}
				else {
					dangerousPolymorphism.setValue(true);
					//2 or more in state set; for each of states, put minimum to states observed, as done by MacClade
					for (int i = 0; i<=maxState; i++) {
						if (CategoricalState.isElement(stateSetObserved, i)){
							minToOthers.setToInfinite();
							for (int j=0; j<=maxState; j++)
								if (i!=j && CategoricalState.isElement(stateSetObserved, j)) {
									tempNum1 = model.getTransitionValue(i, j, tempNum1);
									minToOthers.setMeIfIAmMoreThan(tempNum1);
								}
							downCost.setValue(N, i, minToOthers);
						}
						else {
							minToOthers.setToInfinite();
							for (int j=0; j<=maxState; j++)
								if (i!=j && CategoricalState.isElement(stateSetObserved, j)) {
									for (int k=0;k<=maxState; k++)
										if (i!=k && k!=j && CategoricalState.isElement(stateSetObserved, k)) {
											cascadeDistance(i, j, k, tempNum1, tempNum2, tempNum3);
											minToOthers.setMeIfIAmMoreThan(tempNum1);
										}
								}
							downCost.setValue(N, i, minToOthers);
						}
					}
				}
			}

		}
	}
	/*.................................................................................................................*/
	private   void downPass(int N, Tree tree, MesquiteBoolean dangerousPolymorphism, MesquiteBoolean softPolys) {

		if (tree.nodeIsTerminal(N)) 
			setTerminalDownCosts(N, tree, dangerousPolymorphism);
		else {
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				downPass(d, tree, dangerousPolymorphism, softPolys);

				for (int state = 0; state <= maxState; state++)
					downCost.addValue(N, state, bestAbove(state, d, downCost, tempNum3));
			}

			if (tree.nodeIsSoft(N))
				softPolys.setValue(true);  //record that soft polys found

			if (N == tree.getRoot())
				downCost.placeMinimum2nd(N, tempNum1); 
		}

	}
	/*.................................................................................................................*/
	private   void upPass(int N, Tree tree) {
		if ((tree.nodeIsInternal(N)) && (N!=tree.getRoot())) {
			for (int stateN = 0; stateN <= maxState; stateN++) { //try each state at N
				tempNum3.setToInfinite();  //best so far
				for (int stateA = 0; stateA <= maxState; stateA++) { //try each state at ancestor
					tempNum4 = model.getTransitionValue(stateA, stateN, tempNum4);  //cost from mother to N
					for (int d = tree.firstDaughterOfNode(tree.motherOfNode(N)); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
						if (d!=N) {
							tempNum5 = bestAbove(stateA, d, downCost, tempNum5);  //cost from mother of node to d and above if A at mother
							tempNum4.add(tempNum5);  
						}
					}
					//get upstate from ancestor
					if (tree.motherOfNode(N)!=tree.getRoot()) {
						upCost.placeValue(tree.motherOfNode(N), stateA, tempNum5);
						tempNum4.add(tempNum5);  
					}
					tempNum3.setMeIfIAmMoreThan(tempNum4);  //if best so far, remember it
				}
				upCost.setValue(N, stateN, tempNum3);
			}
		}

		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			upPass(d, tree);
	}
	/*.................................................................................................................*/
	/*Assigning final states using uppass and downpass results*/
	public   void finalPass(int N, Tree tree, CategoricalHistory statesAtNodes) {
		if (N==tree.getRoot()){
			downCost.placeMinimum2nd(N, tempNum1); 
			long best=0;
			for (int state = 0; state<= maxState; state++) {
				if (downCost.equals(N,state, tempNum1))
					best = CategoricalState.addToSet(best, state);
			}
			statesAtNodes.setState(N, best);
		}
		else if (tree.nodeIsTerminal(N)) {
			long stateSetObserved = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(N));
			if (CategoricalState.isUnassigned(stateSetObserved) || CategoricalState.isInapplicable(stateSetObserved))
				statesAtNodes.setState(N, statesAtNodes.getState(tree.motherOfNode(N)));
			else {
				if (CategoricalState.isUncertain(stateSetObserved)){
					long ancStates = statesAtNodes.getState(tree.motherOfNode(N));
					tempNum1.setToUnassigned();
					long result = 0L;
					// find the states in observed closest to state set below
					for (int desc = 0; desc<=maxState; desc++) {
						if (CategoricalState.isElement(stateSetObserved, desc ))
							for (int anc =0; anc<=maxState; anc++)
								if (CategoricalState.isElement(ancStates, anc)) {
									tempNum2 = model.getTransitionValue(anc, desc, tempNum2);
									if (tempNum2.equals(tempNum1)) {
										result = CategoricalState.addToSet(result, anc);
									}
									else if (tempNum2.isLessThan(tempNum1)) {
										result = CategoricalState.makeSet(anc);
										tempNum1.setValue(tempNum2);
									}
								}

					}
					statesAtNodes.setState(N,result);
				}
				else
					statesAtNodes.setState(N,stateSetObserved);
			}
		}
		else {
			// accumulate downstates from daughters
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				for (int state = 0; state <= maxState; state++)
					finalCost.addValue(N, state, bestAbove(state, d, downCost, tempNum3));
			}
			for (int state = 0; state <= maxState; state++)
				finalCost.addValue(N, state, bestAbove(state, N, upCost, tempNum3));
			//find all with minimal final cost
			finalCost.placeMinimum2nd(N, tempNum1);
			long best=0;
			for (int state = 0; state<= maxState; state++) {
				if (finalCost.equals(N,state, tempNum1))
					best = CategoricalState.addToSet(best, state);
			}
			statesAtNodes.setState(N, best);
		}

		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			finalPass(d, tree, statesAtNodes);
	}
	/*...................................................FOR DEBUGGING..............................................................*/
	private   void resetStates(int N, Tree tree, CategoricalHistory statesAtNodes, CategoricalHistory fromStates) {
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			resetStates(d, tree, statesAtNodes, fromStates);
		}
		statesAtNodes.setState(N, fromStates.getState(N));
	}
	/*.................................................................................................................*
	//for debugging
	private void showStates(Number2DArray s){
		for (int node = 2; node<8; node++){
			logln("node " + node);
			String m = "";
			for (int i=0; i<6; i++){
				m += "(" + i + " = " + s.toString(node, i) + ") ";
			}
			logln("   " + m);

		}
	}
	/*.................................................................................................................*/
	/*Resizing matrices as needed for tree size*/
	void adjustStorage(Tree tree) {
		if (downCost==null)
			downCost = new Number2DArray(tree.getNumNodeSpaces(), CategoricalState.maxCategoricalState+1);
		else if (downCost.getSizeC()!=tree.getNumNodeSpaces()) //had been getlengthx
			downCost.resetSize(tree.getNumNodeSpaces(), CategoricalState.maxCategoricalState+1);
		if (upCost==null)
			upCost = new Number2DArray(tree.getNumNodeSpaces(), CategoricalState.maxCategoricalState+1);
		else if (upCost.getSizeC()!=tree.getNumNodeSpaces())
			upCost.resetSize(tree.getNumNodeSpaces(), CategoricalState.maxCategoricalState+1);
		if (finalCost==null)
			finalCost = new Number2DArray(tree.getNumNodeSpaces(), CategoricalState.maxCategoricalState+1);
		else if (finalCost.getSizeC()!=tree.getNumNodeSpaces())
			finalCost.resetSize(tree.getNumNodeSpaces(), CategoricalState.maxCategoricalState+1);
	}
	/*.................................................................................................................*/
	boolean warnedReticulation = false;
	boolean warnedUnbranched = false;
	boolean warnedSoftPoly = false;
	boolean warnedUnrooted = false;
	/*Checking that situation allows calculations to proceed*/
	public boolean warn(Tree tree, CharacterDistribution observedStates, CostMatrixModel model, MesquiteString resultString){
		if (tree == null || observedStates == null || model == null)
			return false;
		if (tree.hasUnbranchedInternals(tree.getRoot())) {
			String message = "Trees with unbranched internal nodes not allowed in step matrix parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedUnbranched){
				discreetAlert( message);
				warnedUnbranched = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (!tree.getRooted() && !model.isSymmetrical()) {
			String message = "Unrooted trees not allowed in calculations with asymmetrical stepmatrices.  Calculations for one or more trees were not completed.";
			if (!warnedUnrooted){
				discreetAlert( message);
				warnedUnrooted = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (tree.hasReticulations()) {
			String message = "Trees with reticulations not allowed in step matrix parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedReticulation){
				discreetAlert( message);
				warnedReticulation = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (tree.hasSoftPolytomies(tree.getRoot())) {
			String message = "Trees with soft polytomies not allowed in step matrix parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedSoftPoly){
				discreetAlert( message);
				warnedSoftPoly = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public  void calculateStates(Tree tree, CharacterDistribution observedStates, CharacterHistory statesAtNodes, CharacterModel model, MesquiteString resultString, MesquiteNumber steps) {  
		if (model==null || observedStates==null || tree == null)
			return;
		if (steps!=null)
			steps.setToUnassigned();
		if (warn(tree, observedStates, (CostMatrixModel)model, resultString))
			return;
		if (resultString!=null)
			resultString.setValue("");
		this.model = (CostMatrixModel)model;
		this.observedStates = observedStates;
		//maxState, tempnums may give reentrancy problems, but best not to reinstantiate tempnums for each call
		maxState = this.model.getMaxState();
		adjustStorage(tree);
		downCost.deassignArray();
		upCost.deassignArray();
		finalCost.deassignArray();

		MesquiteBoolean softPolys = new MesquiteBoolean(false);
		MesquiteBoolean dangerousPolymorphism = new MesquiteBoolean(false);
		downPass(tree.getRoot(), tree, dangerousPolymorphism, softPolys);

		if (steps!=null)
			downCost.placeMinimum2nd(tree.getRoot(), steps);
		upPass(tree.getRoot(), tree);
		finalPass(tree.getRoot(), tree, (CategoricalHistory)statesAtNodes);
		((CategoricalHistory)statesAtNodes).polymorphToUncertainties(tree.getRoot(), tree);
		if (resultString!=null) {
			resultString.setValue("Parsimony reconstruction (stepmatrix: " + model.getName() + ") [Steps: " + steps + "]");
			if (softPolys.getValue())
				resultString.append(" NOTE: Soft polytomies encountered; reconstruction may be incorrect.");  //in fact, not reached because soft polys not allowed
			if (dangerousPolymorphism.getValue())
				resultString.append(" NOTE: Some terminal taxa polymorphic with more than two states; reconstruction may be incorrect.");
		}
	}
	/*.................................................................................................................*/
	public  void calculateSteps(Tree tree, CharacterDistribution observedStates, CharacterModel model, MesquiteString resultString, MesquiteNumber steps) {  
		if (model==null || observedStates==null || tree == null || steps == null)
			return;
		if (steps!=null)
			steps.setToUnassigned();
		if (warn(tree, observedStates,  (CostMatrixModel)model, resultString))
			return;
		if (resultString!=null)
			resultString.setValue("");
		this.model = (CostMatrixModel)model;
		this.observedStates = observedStates;
		maxState = this.model.getMaxState();
		adjustStorage(tree);
		downCost.deassignArray();

		MesquiteBoolean softPolys = new MesquiteBoolean(false);
		MesquiteBoolean dangerousPolymorphism = new MesquiteBoolean(false);
		downPass(tree.getRoot(), tree, dangerousPolymorphism, softPolys);
		downCost.placeMinimum2nd(tree.getRoot(), steps);
		if (resultString!=null) {
			resultString.setValue("Parsimony steps: " + steps + " (stepmatrix: " + model.getName() + ")");
			if (softPolys.getValue())
				resultString.append(" NOTE: Soft polytomies encountered; steps may be incorrectly counted."); //in fact, not reached because soft polys not allowed
			if (dangerousPolymorphism.getValue())
				resultString.append(" NOTE: Some terminal taxa polymorphic with more than two states; steps may be incorrectly counted.");
		}
	}

	/*.................................................................................................................*/
	public boolean compatibleWithContext(CharacterModel model, CharacterDistribution observedStates) {
		return (model instanceof CostMatrixModel) && observedStates instanceof CategoricalDistribution;
	}
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}

	public String getModelTypeName(){
		return "Stepmatrix model";
	}
}



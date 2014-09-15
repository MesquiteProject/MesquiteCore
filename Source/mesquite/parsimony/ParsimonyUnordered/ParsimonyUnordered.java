/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ParsimonyUnordered;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
/** Based largely on, and tested against, MacClade's code for ordered categorical characters*/
public class ParsimonyUnordered extends ParsAncStatesForModel {
	public String getName() {
		return "Parsimony Unordered";
	}
	public String getExplanation() {
		return "Reconstructs the ancestral states of categorical characters using parsimony, under the assumption that states are unordered (unordered or Fitch parsimony; nonadditive).  Also counts parsimony steps." ;
	}
	/*.................................................................................................................*/
	CharacterDistribution observedStates;
	CategoricalHistory downStates;
	CategoricalHistory upStates;
	int steps;
	int[] commonnest;
	long fullSet;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		commonnest = new int[64];
		zeroCommonnest();
		return true;
	}
	public boolean calculatingConditionalMPRSets(){
		return calcConditionalMPRs;
	}
	/*.................................................................................................................*/
	private void zeroCommonnest() {
		for (int i=0; i<64; i++)
			commonnest[i]=0;
	}
	private void addSetToCommonnest(long s) {
		for (int i=0; i<64; i++)
			if (CategoricalState.isElement(s, i))
				commonnest[i]++;
	}
	private long commonnestToSet() {
		int max = 0;
		long result=CategoricalState.emptySet();

		for (int i=0; i<64; i++)
			if (max<commonnest[i])
				max=commonnest[i];
		if (max>0)
			for (int i=0; i<64; i++)
				if (max==commonnest[i])
					result=CategoricalState.addToSet(result, i);
		return result;
	}
	/*.................................................................................................................*/
	private boolean covers(int N, int excluding, Tree tree, long test){
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			if (d!=excluding){
				long s=downStates.getState(d);
				if ((test & s)==0) {
					return false;
				}
			}
		}
		if (tree.getRoot()!=N){
			int anc = tree.motherOfNode(N);
			if (anc!=excluding) {
				long s=upStates.getState(N);
				if ((test & s)==0) {
					return false;
				}
			}
		}

		return true;
	}
	/*.................................................................................................................*/
	private long getFromSoftPolytomous(int N, int excluding, Tree tree, long union, boolean addToCount){

		if (union==0)
			return fullSet;
		int max = CategoricalState.maximum(union);
		long lucky = 0;
		int numInSet;
		int numLucky = 0;
		for (numInSet = 2; numInSet<=max+1 && lucky==0; numInSet++) {
			lucky = 0;
			long test = CategoricalState.firstSet(numInSet);
			while (test!=0) {
				if (covers(N, excluding, tree, test)) {
					lucky |= test;
					numLucky = numInSet;
				}
				test = CategoricalState.nextSet(test, max);
			}
		}
		if (addToCount)
			steps+= numLucky-1;
		return lucky;

	}
	private long getFromHardPolytomous(int N, int excluding, Tree tree, long union, boolean addToCount){
		if (union==0)
			return fullSet;
		zeroCommonnest();
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) { // get from daughters
			if (d!=excluding){
				long s=downStates.getState(d);
				addSetToCommonnest(s);
			}
		}
		if (tree.getRoot()!=N){  //get from mother
			int anc = tree.motherOfNode(N);
			if (anc!=excluding) {
				long s=upStates.getState(N);
				addSetToCommonnest(s);
			}
		}
		long c = commonnestToSet();
		if (addToCount){
			long cMin = CategoricalState.makeSet(CategoricalState.minimum(c));
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) { //steps to daughters
				if (d!=excluding && (downStates.getState(d) & cMin) == 0)
					steps++;
			}
			if (tree.getRoot()!=N){ //steps to mother
				int anc = tree.motherOfNode(N);
				if (anc!=excluding) {
					long s=upStates.getState(N);
					if ((s & cMin) == 0)
						steps++;
				}
			}
		}
		return c;

	}
	private void setTerminalDownStates(int N, Tree tree){
		long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(N));
		if (CategoricalState.isUnassigned(observed) || CategoricalState.isInapplicable(observed))
			downStates.setState(N, fullSet);
		else {
			if (!CategoricalState.isUncertain(observed)){
				int card = CategoricalState.cardinality(observed);
				if (card>1)
					steps += card-1;
			}
			downStates.setState(N, observed & CategoricalState.statesBitsMask);
		}
	}
	/*.................................................................................................................*/
	/* Seems to count polytomies ok, but not used on uppass.  Also, terminal uncertainties/polymorphisms not dealt with well */
	private   void downPass(int N, Tree tree) {
		if (tree.nodeIsTerminal(N)) {
			setTerminalDownStates(N, tree);
		}
		else {
			long intersection = fullSet;
			long union = 0;
			long unionNonMissing = 0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				downPass(d, tree);
				long s=downStates.getState(d);
				intersection &= s;  //intersection
				union |= s;  //union
				if (s!=fullSet)
					unionNonMissing |= s;  //union
			}
			if (intersection == 0) {
				if (tree.nodeIsPolytomous(N)) {
					if (tree.nodeIsSoft(N))
						downStates.setState(N, getFromSoftPolytomous(N, tree.motherOfNode(N), tree, unionNonMissing, true));
					else
						downStates.setState(N, getFromHardPolytomous(N, tree.motherOfNode(N), tree, unionNonMissing, true));
				}
				else {
					downStates.setState(N, union); 
					steps++;
				}
			}
			else {
				downStates.setState(N, intersection);
			}
		}

	}
	/*.................................................................................................................*/
	private   void upPass(int N, Tree tree) {
		if (N!=tree.getRoot()) {
			long intersection = fullSet;
			long union = 0;
			// accumulate downstates from sisters
			for (int d = tree.firstDaughterOfNode(tree.motherOfNode(N)); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (d!=N) {
					long s = downStates.getState(d);
					intersection &= s;  //intersection
					union |= s;  //union
				}
			}
			//get upstate from ancestor
			if (tree.motherOfNode(N)!=tree.getRoot()) {
				long s = upStates.getState(tree.motherOfNode(N));
				intersection &= s;  //intersection
				union |= s;  //union
			}
			if (intersection == 0) {
				if (tree.nodeIsPolytomous(tree.motherOfNode(N))) {
					if (tree.nodeIsSoft(tree.motherOfNode(N)))
						upStates.setState(N, getFromSoftPolytomous(tree.motherOfNode(N), N, tree, union, false));
					else
						upStates.setState(N, getFromHardPolytomous(tree.motherOfNode(N), N, tree, union, false));
				}
				else
					upStates.setState(N, union);
			}
			else 
				upStates.setState(N, intersection);
		}


		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			upPass(d, tree);
	}
	/*.................................................................................................................*/
	public   void finalPass(int N, Tree tree, CategoricalHistory statesAtNodes) {
		if (N==tree.getRoot())
			statesAtNodes.setState(N, downStates.getState(N));
		else if (tree.nodeIsTerminal(N)) {
			long observed =  ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(N));
			if (CategoricalState.isUnassigned(observed) || CategoricalState.isInapplicable(observed)) {
				statesAtNodes.setState(N, statesAtNodes.getState(tree.motherOfNode(N)));
				if (calcConditionalMPRs){
					for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){ //for each possible state at immediate ancestor
						statesAtNodes.setConditionalStateSet(CategoricalState.makeSet(ist), N, ist);
					}
				}
			}
			else {
				if (CategoricalState.isUncertain(observed)){
					// if ancestor state is entirely contained within this, use ancestor state
					// otherwise use this directly
					long result = CategoricalState.unassigned;
					long ancState =statesAtNodes.getState(tree.motherOfNode(N));
					if (CategoricalState.isSubset(ancState, observed))
						result = ancState;
					else
						result = observed;
					statesAtNodes.setState(N, result);
					if (calcConditionalMPRs){
						for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){//for each possible state at immediate ancestor
							result = CategoricalState.unassigned;
							ancState = CategoricalState.makeSet(ist);
							if (CategoricalState.isSubset(ancState, observed))
								result = ancState;
							else
								result = observed;
							statesAtNodes.setConditionalStateSet(result, N, ist);
						}
					}
				}
				else {
					statesAtNodes.setState(N,observed);
					if (calcConditionalMPRs){
						for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){//for each possible state at immediate ancestor
							statesAtNodes.setConditionalStateSet(observed, N, ist);
						}
					}
				}
			}
		}
		else 	if (tree.nodeIsPolytomous(N) && tree.nodeIsSoft(N)) {
			long result = CategoricalState.unassigned;
			long uS = upStates.getState(N);

			long dS = downStates.getState(N);
			if ((dS & uS) != 0L)
				result = dS & uS;
			else
				result = dS | uS;
			statesAtNodes.setState(N, result);
			if (calcConditionalMPRs){
				dS = downStates.getState(N);
				for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){//for each possible state at immediate ancestor
					result = CategoricalState.unassigned;
					uS = CategoricalState.makeSet(ist);

					if ((dS & uS) != 0L)
						result = dS & uS;
					else
						result = dS | uS;
					statesAtNodes.setConditionalStateSet(result, N, ist);
				}
			}
		}
		else {
			long result = CategoricalState.unassigned;
			long sU = upStates.getState(N);

			long intersection = fullSet;
			long union = 0;
			// accumulate downstates from daughters
			long s1 = downStates.getState(tree.firstDaughterOfNode(N));
			long s=0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				s = downStates.getState(d);
				intersection &= s;  //intersection
				union |= s;  //union
			}
			long unionDownStates = union;
			long intersectionDownStates = intersection;
			intersection &= sU;  //intersection
			union |= sU;  //union

			if (intersection == 0) {
				if (tree.nodeIsPolytomous(N)) {
					result = getFromHardPolytomous(N, -1, tree, union, false);
				}
				else {
					intersection=(s1&s)|(s1&sU)|(s&sU);
					if (intersection==0)
						result = union;
					else
						result = intersection;
				}
			}
			else 
				result = intersection;
			statesAtNodes.setState(N, result);
			if (calcConditionalMPRs){
				for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){//for each possible state at immediate ancestor
					result = CategoricalState.unassigned;
					sU = CategoricalState.makeSet(ist);
					union = unionDownStates;
					intersection = intersectionDownStates;

					intersection &= sU;  //intersection
					union |= sU;  //union

					if (intersection == 0) {
						if (tree.nodeIsPolytomous(N)) {
							result = getFromHardPolytomous(N, -1, tree, union, false);
						}
						else {
							intersection=(s1&s)|(s1&sU)|(s&sU);
							if (intersection==0)
								result = union;
							else
								result = intersection;
						}
					}
					else 
						result = intersection;
					statesAtNodes.setConditionalStateSet(result, N, ist);
				}
			}

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
	/*.................................................................................................................*/
	boolean warnedReticulation = false;
	boolean warnedUnbranched = false;
	public boolean warn(Tree tree, CharacterDistribution observedStates, MesquiteString resultString){
		if (tree == null || observedStates == null)
			return false;
		if (tree.hasUnbranchedInternals(tree.getRoot())) {
			String message = "Trees with unbranched internal nodes not allowed in unordered state parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedUnbranched) {
				discreetAlert( message);
				warnedUnbranched = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (tree.hasReticulations()) {
			String message = "Trees with reticulations not allowed in unordered state parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedReticulation) {
				discreetAlert( message);
				warnedReticulation = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public  void calculateStates(Tree tree, CharacterDistribution observedStates, CharacterHistory statesAtNodes, CharacterModel model, MesquiteString resultString, MesquiteNumber stepsObject) {  
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (resultString!=null)
			resultString.setValue("");
		if (observedStates==null || tree == null)
			return;
		if (warn(tree, observedStates, resultString))
			return;
		this.observedStates = observedStates;
		fullSet = ((CategoricalDistribution)observedStates).fullSet();
		steps = 0;  //note: not designed to be thread safe!!
		downStates=  (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)downStates);
		upStates=  (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)upStates);

		downPass(tree.getRoot(), tree);

		if (stepsObject!=null)
			stepsObject.setValue(steps);
		upPass(tree.getRoot(), tree);
		finalPass(tree.getRoot(), tree, (CategoricalHistory)statesAtNodes);
		((CategoricalHistory)statesAtNodes).polymorphToUncertainties(tree.getRoot(), tree);
		if (resultString!=null)
			resultString.setValue("Parsimony reconstruction (Unordered) [Steps: " + stepsObject + "]");
	}
	/*.................................................................................................................*/
	public  void calculateSteps(Tree tree, CharacterDistribution observedStates, CharacterModel model, MesquiteString resultString, MesquiteNumber stepsObject) {  
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (resultString!=null)
			resultString.setValue("");
		if (observedStates==null || tree == null || stepsObject == null)
			return;
		if (warn(tree, observedStates, resultString))
			return;
		this.observedStates = observedStates;
		fullSet = ((CategoricalDistribution)observedStates).fullSet();
		downStates=  (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)downStates);
		steps = 0;//note: not designed to be thread safe!!
		downPass(tree.getRoot(), tree);
		stepsObject.setValue(steps);
		if (resultString!=null)
			resultString.setValue("Parsimony steps: " + stepsObject + " (unordered)");
	}

	/*.................................................................................................................*/
	public boolean compatibleWithContext(CharacterModel model, CharacterDistribution observedStates) {
		return (model.getName().equalsIgnoreCase("Unordered")) && observedStates instanceof CategoricalDistribution;
	}

	/*.................................................................................................................*/
	/** returns whether this module is a prerelease version */
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** Returns whether or not the module does substantive calculations and thus should be cited.  If true, its citation will
 	appear in the citations panel of the windows */
	public boolean showCitation()  {
		return true;
	}
}



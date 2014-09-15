/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ParsimonyOrdered;
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
public class ParsimonyOrdered extends ParsAncStatesForModel  {
	public String getName() {
		return "Parsimony Ordered";
	}
	public String getExplanation() {
		return "Reconstructs the ancestral states of categorical characters using parsimony, under the assumption that states are ordered (ordered, Farris or Wagner parsimony; additive).  Also counts parsimony steps." ;
	}
	/*.................................................................................................................*/
	CharacterDistribution observedStates;
	CategoricalHistory downStates;
	CategoricalHistory upStates;
	int steps;
	MesquiteInteger  tempStepsObj;
	int show = 0;
	int[] toLeft, toRight;
	long fullSet;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		tempStepsObj  = new MesquiteInteger(0);
		/* debugging only  		
 		addMenuItem( "Final States", makeCommand("showFinalStates",  this));
 		addMenuItem( "Down States", makeCommand("showDownStates",  this));
 		addMenuItem( "Up States", makeCommand("showUpStates",  this));
		 */
		toLeft = new int[CategoricalState.maxCategoricalState+1];
		toRight = new int[CategoricalState.maxCategoricalState+1];
		return true;
	}
 	public boolean calculatingConditionalMPRSets(){
 		return calcConditionalMPRs;
 	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets output to be the final MPR sets", null, commandName, "showFinalStates")) {
			show = 0;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets output to be downpass sets", null, commandName, "showDownStates")) {
			show = 1;
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets output to be uppass sets", null, commandName, "showUpStates")) {
			show = 2;
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	private long orderedOperator3 (long set1, long set2, long set3) {  // assumes continuous sets
		int min1 = CategoricalState.minimum(set1);
		int max1 = CategoricalState.maximum(set1);
		int min2 = CategoricalState.minimum(set2);
		int max2 = CategoricalState.maximum(set2);
		int min3 = CategoricalState.minimum(set3);
		int max3 = CategoricalState.maximum(set3);

		/*Two most distant sets found then between-space intersected with remaining set */
		if ((min1 > min2) && (min1 > min3)) { //set1 minimum furthest right}
			if (max2 < max3) //most distant are 2 and 1}
				return (CategoricalState.span(max2, min1)  & set3);  //{[--2-(-]--3--)  [--1--] }
			else  //most distant are 3 and 1}
				return (CategoricalState.span(max3, min1)  & set2); //{[--3-(-]--2--)  [--1--] }
		}
		else if (min2 > min3) { // {set2 minimum furthest right}
			if (max1 < max3) //  {most distant are 1 and 2}
				return (CategoricalState.span(max1, min2)  & set3);   //{[--1-(-]--3--)  [--2--] }
			else  //{most distant are 3 and 2}
				return (CategoricalState.span(max3, min2)  & set1);   //{[--3-(-]--1--)  [--2--] }
		}
		else { //  {set3 furthest right}
			if (max1 < max2) // {most distant are 1 and 3}
				return (CategoricalState.span(max1, min3)  & set2);   //{[--1-(-]--2--)  [--3--] }
			else  //{most distant are 2 and 3}
				return (CategoricalState.span(max2, min3)  & set1); //{[--2-(-]--1--)  [--3--] }
		}
	}
	/*.................................................................................................................*/
	private long orderedOperator2 (long set1, long set2, MesquiteInteger o) {  // assumes continuous sets
		long result = set1 & set2;
		if (result !=0)
			return result;
		else {
			if (set1>set2) { // use maximum of set2 to minimum of set1
				int m1, m2;
				m1=CategoricalState.minimum(set1);
				m2=CategoricalState.maximum(set2);
				o.setValue(m1-m2);
				return CategoricalState.span(m2,m1);
			}
			else {// use maximum of set1 to minimum of set2
				int m1, m2;
				m1=CategoricalState.maximum(set1);
				m2=CategoricalState.minimum(set2);
				o.setValue(m2-m1);
				return CategoricalState.span(m1,m2);
			}
		}
	}
	private void setTerminalDownStates(int N, Tree tree){
		long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(N));
		if (CategoricalState.isUnassigned(observed) || CategoricalState.isInapplicable(observed))
			downStates.setState(N, fullSet);
		else {
			int card = CategoricalState.cardinality(observed);
			if (card>1){
				int min = CategoricalState.minimum(observed);
				int max = CategoricalState.maximum(observed);
				if (!CategoricalState.isUncertain(observed))
					steps += max-min;
				downStates.setState(N, CategoricalState.span(min, max));
			}
			else
				downStates.setState(N, observed & CategoricalState.statesBitsMask);
		}
	}
	private long getFromSoftPolytomous(int N, int excluding, Tree tree, boolean addToCount){
		int minOfMaxs = CategoricalState.maxCategoricalState;
		int maxOfMins = 0;
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			if (d!=excluding){
				long s = downStates.getState(d);
				minOfMaxs = MesquiteInteger.minimum(minOfMaxs, CategoricalState.maximum(s));
				maxOfMins = MesquiteInteger.maximum(maxOfMins, CategoricalState.minimum(s));
			}
		}
		if (N!=tree.getRoot()){
			int anc = tree.motherOfNode(N);
			if (anc !=excluding) {
				long s = upStates.getState(N);
				minOfMaxs = MesquiteInteger.minimum(minOfMaxs, CategoricalState.maximum(s));
				maxOfMins = MesquiteInteger.maximum(maxOfMins, CategoricalState.minimum(s));
			}
		}
		if (addToCount)
			steps+= maxOfMins - minOfMaxs;
		return CategoricalState.span(minOfMaxs, maxOfMins);
	}
	private long getFromHardPolytomous(int N, int excluding, Tree tree, boolean addToCount){
		for (int i=0; i< toLeft.length; i++){
			toLeft[i]=0;
			toRight[i]=0;
		}
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			if (d!=excluding){
				long s = downStates.getState(d);
				for (int i=0; i< toLeft.length; i++){
					int max = CategoricalState.maximum(s);
					int min = CategoricalState.minimum(s);
					if (max<i)
						toLeft[i]++;
					if (min>i)
						toRight[i]++;
				}
			}
		}
		if (tree.getRoot()!=N){  //get from mother
			int anc = tree.motherOfNode(N);
			if (anc!=excluding) {
				long s=upStates.getState(N);
				for (int i=0; i< toLeft.length; i++){
					int max = CategoricalState.maximum(s);
					int min = CategoricalState.minimum(s);
					if (max<i)
						toLeft[i]++;
					if (min>i)
						toRight[i]++;
				}
			}
		}
		long result = 0L;
		if (toRight[0]<= toLeft[1])
			result = CategoricalState.addToSet(result, 0);
		if (toLeft[toLeft.length-1] <= toRight[toLeft.length-2])
			result = CategoricalState.addToSet(result, toLeft.length-1);
		for (int i=1; i< toLeft.length-1; i++){
			if (toLeft[i] <= toRight[i-1] && toRight[i]<= toLeft[i+1])
				result = CategoricalState.addToSet(result, i);
		}
		if (addToCount){
			int target = CategoricalState.minimum(result);
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				long s = downStates.getState(d);
				int max = CategoricalState.maximum(s);
				int min = CategoricalState.minimum(s);
				if (target>max)
					steps += target-max;
				else if (target<min)
					steps += min-target;
			}
			if (tree.getRoot()!=N){  //get from mother
				int anc = tree.motherOfNode(N);
				if (anc!=excluding) {
					long s=upStates.getState(N);
					int max = CategoricalState.maximum(s);
					int min = CategoricalState.minimum(s);
					if (target>max)
						steps += target-max;
					else if (target<min)
						steps += min-target;
				}
			}
		}
		return result;

	}
	/*.................................................................................................................*/
	/* at present not good with polytomies */
	private   void downPass(int N, Tree tree) {
		if (tree.nodeIsTerminal(N)) {
			setTerminalDownStates(N, tree);
		}
		else {
			if (tree.nodeIsPolytomous(N)) {
				long intersection = fullSet;
				long union = 0;
				for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
					downPass(d, tree);
					long s=downStates.getState(d);
					intersection &= s;  //intersection
				}
				if (intersection == 0) {
					if (tree.nodeIsSoft(N)){
						downStates.setState(N, getFromSoftPolytomous(N, tree.motherOfNode(N), tree, true));
					}
					else {
						downStates.setState(N, getFromHardPolytomous(N, tree.motherOfNode(N), tree, true));
					}
				}
				else
					downStates.setState(N, intersection);
			}
			else {   //dichotomous
				long s1=0;
				long s2=0;
				for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
					downPass(d, tree);
					if (d==tree.firstDaughterOfNode(N))
						s1 = downStates.getState(d);
					else
						s2 = downStates.getState(d);
				}
				long intersection = s1 & s2;  //intersection
				if (intersection == 0) {
					downStates.setState(N, orderedOperator2(s1, s2, tempStepsObj));
					steps+= tempStepsObj.getValue();
				}
				else 
					downStates.setState(N, intersection);
			}
		}
	}
	/*.................................................................................................................*/
	/* at present not good with polytomies */
	private   void upPass(int N, Tree tree) {
		long intersection = fullSet;
		if (N!=tree.getRoot()) {
			long s=0; long t=0;
			// accumulate downstates from sisters
			for (int d = tree.firstDaughterOfNode(tree.motherOfNode(N)); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (d!=N) {
					s = downStates.getState(d);
					intersection &= s;  //intersection
				}
			}
			//get upstate from ancestor
			if (tree.motherOfNode(N)!=tree.getRoot()) {
				t = upStates.getState(tree.motherOfNode(N));
				intersection &= t;  //intersection
			}
			if (intersection == 0) {
				if (tree.nodeIsPolytomous(tree.motherOfNode(N))) {
					if (tree.nodeIsSoft(tree.motherOfNode(N))){
						upStates.setState(N, getFromSoftPolytomous(tree.motherOfNode(N), N, tree, false));
					}
					else
						upStates.setState(N, getFromHardPolytomous(tree.motherOfNode(N), N, tree, false));
				}
				else {
					upStates.setState(N, orderedOperator2(t, s, tempStepsObj));
				}
			}
			else 
				upStates.setState(N, intersection);
		}


		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			upPass(d, tree);
	}
	/*.................................................................................................................*/
	public   void finalPass(int N, Tree tree, CategoricalHistory statesAtNodes) {
		if (show == 1) {
			statesAtNodes.setState(N, downStates.getState(N));
		}
		else if (show == 2) {
			statesAtNodes.setState(N, upStates.getState(N));
		}
		else if (N==tree.getRoot())
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
					long ancState = statesAtNodes.getState(tree.motherOfNode(N));
					
					long result = CategoricalState.unassigned;
					if ((observed & ancState) != 0)
						result = observed & ancState;
					else {
						observed = CategoricalState.span(observed);
						if ((observed & ancState ) !=0)
							result = observed & ancState;
						else {
							if (observed>ancState)
								result = CategoricalState.makeSet(CategoricalState.minimum(observed));
							else
								result = CategoricalState.makeSet(CategoricalState.maximum(observed));
						}
					}
					statesAtNodes.setState(N, result);
					if (calcConditionalMPRs){
						for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){//for each possible state at immediate ancestor
							result = CategoricalState.unassigned;
							if ((observed & ancState) != 0)
								result = observed & ancState;
							else {
								observed = CategoricalState.span(observed);
								if ((observed & ancState ) !=0)
									result = observed & ancState;
								else {
									if (observed>ancState)
										result = CategoricalState.makeSet(CategoricalState.minimum(observed));
									else
										result = CategoricalState.makeSet(CategoricalState.maximum(observed));
								}
							}
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
		else if (tree.nodeIsPolytomous(N))  {
			if (tree.nodeIsSoft(N)){
				long result = CategoricalState.unassigned;
				long uS = upStates.getState(N);
				
				long dS = downStates.getState(N);
				if ((dS & uS) != 0L)
					result = dS & uS;
				else
					result = orderedOperator2(dS, uS, tempStepsObj);
				statesAtNodes.setState(N, result);
				if (calcConditionalMPRs){
					for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){//for each possible state at immediate ancestor
						result = CategoricalState.unassigned;
						uS = CategoricalState.makeSet(ist);

						if ((dS & uS) != 0L)
							result = dS & uS;
						else
							result = orderedOperator2(dS, uS, tempStepsObj);
						statesAtNodes.setConditionalStateSet(result, N, ist);
					}
				}
			}
			else {
				long intersection = fullSet;
				// accumulate downstates from daughters
				long s1 = downStates.getState(tree.firstDaughterOfNode(N));
				long s=0;
				for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
					s = downStates.getState(d);
					intersection &= s;  //intersection
				}
				long downAccumulated = intersection;
				
				long sU = upStates.getState(N);
				long result = CategoricalState.unassigned;
				intersection &= sU;  //intersection
				if (intersection == 0) 
					result = getFromHardPolytomous(N, -1, tree, false);
				else 
					result = intersection;
				statesAtNodes.setState(N, result);
				if (calcConditionalMPRs){
					for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){//for each possible state at immediate ancestor
						result = CategoricalState.unassigned;
						sU = CategoricalState.makeSet(ist);
						intersection = downAccumulated;
						intersection &= sU;  //intersection
						if (intersection == 0) 
							result = getFromHardPolytomous(N, -1, tree, false);
						else 
							result = intersection;
						statesAtNodes.setConditionalStateSet(result, N, ist);
					}
				}
			}
		}
		else  {
			long intersection = fullSet;
			// accumulate downstates from daughters
			long s1 = downStates.getState(tree.firstDaughterOfNode(N));
			long s=0;
			for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				s = downStates.getState(d);
				intersection &= s;  //intersection
			}
			long downAccumulated = intersection;
			
			long sU = upStates.getState(N);
			long result = CategoricalState.unassigned;
			intersection &= sU;  //intersection
			if (intersection == 0)
				result = orderedOperator3(s, s1, sU);
			else 
				result = intersection;
			statesAtNodes.setState(N, result);
			
			if (calcConditionalMPRs){
				for (int ist = 0; ist <= CategoricalState.maxCategoricalState; ist++){//for each possible state at immediate ancestor
					result = CategoricalState.unassigned;
					sU = CategoricalState.makeSet(ist);
					intersection = downAccumulated;
					intersection &= sU;  //intersection
					if (intersection == 0)
						result = orderedOperator3(s, s1, sU);
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
			String message = "Trees with unbranched internal nodes not allowed in ordered state parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedUnbranched) {
				discreetAlert( message);
				warnedUnbranched = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (tree.hasReticulations()) {
			String message = "Trees with reticulations not allowed in ordered state parsimony calculations.  Calculations for one or more trees were not completed.";
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
		if (resultString!=null)
			resultString.setValue("");
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (observedStates==null || tree == null || statesAtNodes == null)
			return;
		if (warn(tree, observedStates, resultString))
			return;
		this.observedStates = observedStates;

		fullSet = ((CategoricalDistribution)observedStates).fullSet();
		steps = 0;  //note: not thread safe!
		downStates=  (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)downStates);
		upStates=  (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)upStates);
		downPass(tree.getRoot(), tree);
		upPass(tree.getRoot(), tree);
		finalPass(tree.getRoot(), tree, (CategoricalHistory)statesAtNodes);
		((CategoricalHistory)statesAtNodes).polymorphToUncertainties(tree.getRoot(), tree);
		if (stepsObject != null)
			stepsObject.setValue(steps);
		if (resultString!=null)
			resultString.setValue("Parsimony reconstruction (ordered) [Steps: " + stepsObject + "]");
	}
	/*.................................................................................................................*/
	public  void calculateSteps(Tree tree, CharacterDistribution observedStates, CharacterModel model, MesquiteString resultString, MesquiteNumber stepsObject) {  
		if (resultString!=null)
			resultString.setValue("");
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (observedStates==null || tree == null || stepsObject == null)
			return;
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (warn(tree, observedStates, resultString))
			return;
		this.observedStates = observedStates;
		fullSet = ((CategoricalDistribution)observedStates).fullSet();
		downStates=  (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)downStates);
		steps = 0;  //note: not designed to be thread safe!!
		downPass(tree.getRoot(), tree);
		stepsObject.setValue(steps);
		if (resultString!=null)
			resultString.setValue("Parsimony steps: " + stepsObject + " (ordered)");
	}

	/*.................................................................................................................*/
	public boolean compatibleWithContext(CharacterModel model, CharacterDistribution observedStates) {
		return (model.getName().equalsIgnoreCase("Ordered")) && observedStates instanceof CategoricalDistribution;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	/** Returns whether or not the module does substantive calculations and thus should be cited.  If true, its citation will
 	appear in the citations panel of the windows */
	public boolean showCitation()  {
		return true;
	}
	/*.................................................................................................................*/
	public String getModelTypeName(){
		return "Parsimony model";
	}
}



/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.meristic.ParsimonyMeristicLinear;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.meristic.lib.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
/** Based largely on, and tested against, MacClade's code for ordered categorical characters*/
public class ParsimonyMeristicLinear extends ParsAncStatesForModel {
	public String getName() {
		return "Parsimony Meristic Linear";
	}
	public String getExplanation() {
		return "Reconstructs the ancestral states of mersitic characters so as to minimize the sum of absolute values of changes (linear, Wagner, Farris or Manhattan parsimony).  If the meristic character has multiple items, then length is reported on only the first item." ;
	}
	public int getVersionOfFirstRelease(){
		return 272;  
	}
	/*.................................................................................................................*/
	CharacterDistribution observedStates;
	MeristicHistory finalMin, finalMax, upMin, upMax, downMin, downMax;
	MesquiteInteger dummy, temp1, temp2, NYLength;
	int overallMin = MesquiteInteger.unassigned;
	int overallMax = MesquiteInteger.unassigned;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		dummy = new MesquiteInteger(0);
		temp1 = new MesquiteInteger(0);
		temp2 = new MesquiteInteger(0);
		NYLength = new MesquiteInteger(0);
		return true;
	}

	/*----------------------------------------------------------------------- */
	/*----------------------------------------------------------------------- */
	void combine (MesquiteInteger minA, MesquiteInteger maxA, int minD, int maxD, int minE, int maxE, MesquiteInteger  inc) {
		/*This is the Meristic value state set operator; takes state ranges}
	{[minD..maxD] and [minE..maxE] and combines them and outputs range [minA..maxA]}
	{inc is the increment on length caused by the combining */
		if (minD < minE) {
			if (maxD < minE) {  //{DDDDD EEEEE}
				minA.setValue(maxD);
				maxA.setValue(minE);
				inc.add(minE - maxD);
			}
			else if (maxD < maxE) {  //{DDDDDEDEDEEEEE}
				minA.setValue(minE);
				maxA.setValue(maxD);
			}
			else {  //{DDDDEDEDEDDDDD}
				minA.setValue(minE);
				maxA.setValue(maxE);
			}
		}
		else {
			if (maxE < minD) {	//{EEEEEEE   DDDDDDD}
				minA.setValue(maxE);
				maxA.setValue(minD);
				inc.add(minD - maxE);
			}
			else if (maxE < maxD) {   //{EEEEEEDEDEDDDDDD}
				minA.setValue(minD);
				maxA.setValue(maxE);
			}
			else {   //{EEEEEEDEDEDEEEEE}
				minA.setValue(minD);
				maxA.setValue(maxD);
			}
		}
	}

	/*_____________________________________________________________________ */
	void doCombineOneThenOther (MesquiteInteger minA, MesquiteInteger maxA, int min11,int  max11, int min22, int max22, int min33, int  max33) {
		combine(minA, maxA, min11, max11, min22, max22, dummy);
		combine(minA, maxA, minA.getValue(), maxA.getValue(), min33, max33, dummy);
	}

	/*_____________________________________________________________________ */
	void combinefromGreatestMin (MesquiteInteger minA, MesquiteInteger maxA, int minG,int  maxG, int min22, int max22, int min33, int  max33) {
		/*{here minG, maxG are min and max of set with rightmost minimum}
	{First we find out which of the other sets' maximum is the furthest left of minG, if either is}*/
		if (max33 < minG) {
			if (max22 < max33) //{max22 furthest left, and is left of minG; Thus combine set G and set 22 first, then 33}
				doCombineOneThenOther(minA, maxA, minG, maxG, min22, max22, min33, max33);
			else //{max33 furthest left, and is left of minG; Thus combine set G and set 33 first, then 22}
				doCombineOneThenOther(minA, maxA, minG, maxG, min33, max33, min22, max22);
		}
		else
			/*{Either: max22 furthest left, and is left of minG; Thus combine set G and set 22 first, then 33}
			{Or: Both max22 and max33 are right of minG, thus there must be triple intersection, and order doesn't matter}*/
			doCombineOneThenOther(minA, maxA, minG, maxG, min22, max22, min33, max33);
	}

	/*_____________________________________________________________________ */
	void tripleCombine (MesquiteInteger minA, MesquiteInteger maxA, int minD, int maxD, int minE, int maxE, int minF, int maxF) {
		/*{This is the Meristic value state set operator for 3 SETS; takes state ranges}
	{[minD..maxD], [minE..maxE] and [minF..maxF] and combines them and outputs range [minA..maxA]}
	{Uses the trick of taking two most distant state sets, applying binary set operator on them, then}
	{taking result and combine it with third set using binary set operator again}*/
		if (minD < minE) {
			if (minE < minF)  	//{¥ minF is greatest}
				combinefromGreatestMin(minA, maxA, minF, maxF, minD, maxD, minE, maxE);
			else 					//{¥ minE greatest}
				combinefromGreatestMin(minA, maxA, minE, maxE, minD, maxD, minF, maxF);
		}
		else if (minF > minD) //{¥ minF greatest}
			combinefromGreatestMin(minA, maxA, minF, maxF, minD, maxD, minE, maxE);
		else  						//{¥ minD greatest}
			combinefromGreatestMin(minA, maxA, minD, maxD, minF, maxF, minE, maxE);
	}

	/*_____________________________________________________________________ */
	/*{find min max range among observed for missing data}*/
	void NYsurvey (int numTaxa, MeristicDistribution observedStates,  int item) {
		overallMin = MesquiteInteger.unassigned;
		overallMax = MesquiteInteger.unassigned;
		for (int i=0; i<numTaxa; i++) {
			overallMin = MesquiteInteger.minimum(overallMin, observedStates.getState(i, item));
			overallMax = MesquiteInteger.maximum(overallMax, observedStates.getState(i, item));
		}
	}
	//TODO: allow missing data!
	//TODO: handle multiple items better; should use min and max items if available
	/*_____________________________________________________________________ */
	/*{Downpass of linear parsimony on Meristic characters}*/
	void NYdown (Tree tree, int N, int item) {
		if (tree.nodeIsInternal(N)) {  
			int L = tree.firstDaughterOfNode(N, deleted);
			int R = tree.lastDaughterOfNode(N, deleted);
			NYdown(tree, L, item);  //ASSUMES dichtomous
			NYdown(tree, R, item);
			//->downMin is minimum of state range; ->downMax is maximum}
			//note here we let combine increment the NYlength during downpass}
			combine(temp1, temp2, downMin.getState(L), downMax.getState(L), downMin.getState(R),downMax.getState(R), NYLength);
			downMin.setState(N, temp1.getValue());
			downMax.setState(N, temp2.getValue());
		}
		else { //terminal node
			int ts = ((MeristicDistribution)observedStates).getState(tree.taxonNumberOfNode(N), item);
			if (MesquiteInteger.isUnassigned(ts)) {
				downMin.setState(N, overallMin);
				downMax.setState(N, overallMax);
			}
			else {
				downMin.setState(N, ts);
				downMax.setState(N, downMin.getState(N)); 
			}
		}
	}
	/*_____________________________________________________________________ */
	/*This is combined UpPass and FinalPass for Meristic characters, linear parsimony}*/

	void NYfinal (Tree tree, int N, MeristicHistory statesAtNodes, int itemMin, int itemMax) {
		if (tree.nodeIsInternal(N)) {  
			int L = tree.firstDaughterOfNode(N, deleted);
			int R = tree.lastDaughterOfNode(N, deleted);
			if (N == tree.getRoot(deleted))  {
				finalMin.setState(N, downMin.getState(N));  //{use downpass states for root's final}
				finalMax.setState(N, downMax.getState(N));  //{use downpass states for root's final}
			}
			else {
				//{First calculate up states}
				int anc = tree.motherOfNode(N, deleted);
				int sis = tree.nextSisterOfNode(N, deleted);
				if (!tree.nodeExists(sis))
					sis = tree.previousSisterOfNode(N, deleted);
				if (anc == tree.getRoot(deleted)) {
					upMin.setState(N, downMin.getState(sis));
					upMax.setState(N, downMax.getState(sis));
				}
				else {
					dummy.setValue(0);
					combine(temp1, temp2, upMin.getState(anc), upMax.getState(anc), downMin.getState(sis), downMax.getState(sis),dummy);
					upMin.setState(N, temp1.getValue());
					upMax.setState(N, temp2.getValue());
				}
				//{Then calculate final states}
				tripleCombine(temp1, temp2, downMin.getState(L), downMax.getState(L), downMin.getState(R), downMax.getState(R), upMin.getState(N), upMax.getState(N));
				finalMin.setState(N, temp1.getValue());
				finalMax.setState(N, temp2.getValue());
			}
			NYfinal(tree, L, statesAtNodes, itemMin, itemMax);
			NYfinal(tree, R, statesAtNodes, itemMin, itemMax);
		}
		else {
			int ts = ((MeristicDistribution)observedStates).getState(tree.taxonNumberOfNode(N), 0); //just testing item 0?
			if (MesquiteInteger.isUnassigned(ts)) {
				finalMin.setState(N, finalMin.getState(tree.motherOfNode(N, deleted)));  //{Results stored in [finalMin .. finalMax]}
				finalMax.setState(N, finalMax.getState(tree.motherOfNode(N, deleted)));  
			}
			else {
				finalMin.setState(N, downMin.getState(N));  //{Results stored in [finalMin .. finalMax]}
				finalMax.setState(N, downMax.getState(N));  
			}
		}

		statesAtNodes.setState(N, itemMin, finalMin.getState(N));
		statesAtNodes.setState(N, itemMax, finalMax.getState(N));
	}
	/*_____________________________________________________________________ */
	/*{Reconstructs ancestral states for Meristic characters using linear parsimony.}
	{nb->downMin, downMax store the minima and maxima of downpass ranges}
	{nb->upMin, upMax store the minima and maxima of uppass ranges}
	{nb->finalMin, finalMax store the minima and maxima of final pass ranges}
	{	(final named C and D because finalMin doubly used; see MinSQReconstruct)}*/

	/*.................................................................................................................*/
	private void adjustStorage(Tree tree, CharacterDistribution observedStates) {
		downMin=  (MeristicHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)downMin);
		downMax=  (MeristicHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)downMax);
		upMin=  (MeristicHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)upMin);
		upMax=  (MeristicHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)upMax);
		finalMin=  (MeristicHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)finalMin);
		finalMax=  (MeristicHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)finalMax);
	}
	boolean warnedReticulation = false;
	boolean warnedUnbranched = false;
	boolean warnedPolytomies = false;
	boolean[] deleted;
	//boolean warnedMissing = false;
	public boolean warn(Tree tree, MeristicDistribution observedStates, MesquiteString resultString){
		if (tree == null || observedStates == null)
			return false;
		if (tree.hasPolytomies(tree.getRoot())) {
			String message = "Trees with polytomies not allowed in linear parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedPolytomies) {
				discreetAlert( message);
				warnedPolytomies = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		/*if (observedStates.hasMissing(tree, tree.getRoot()) || observedStates.hasInapplicable(tree, tree.getRoot())) {
			String s ="Missing data, gaps are not currently supported by linear parsimony calculations.  Calculations for one or more characters were not completed.";
			if (!warnedMissing) {
				discreetAlert( s);
				warnedMissing = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return true;
		}*/
		if (tree.hasUnbranchedInternals(tree.getRoot())) {
			String message = "Trees with unbranched internal nodes not allowed in linear parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedUnbranched) {
				discreetAlert( message);
				warnedUnbranched = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (tree.hasReticulations()) {
			String message = "Trees with reticulations not allowed in linear parsimony calculations.  Calculations for one or more trees were not completed.";
			if (!warnedReticulation) {
				discreetAlert( message);
				warnedReticulation = true;
			}
			if (resultString!=null)
				resultString.setValue(message);
			return true;
		}
		if (observedStates.hasMissing(tree, tree.getRoot()) || observedStates.hasInapplicable(tree, tree.getRoot())){
			if (deleted == null || deleted.length <  tree.getNumNodeSpaces())
				deleted = new boolean[tree.getNumNodeSpaces()];
			for (int i = 0; i<deleted.length; i++) deleted[i] = false;
			for (int it = 0; it< tree.getTaxa().getNumTaxa(); it++)
				if (observedStates.isUnassigned(it) || observedStates.isInapplicable(it)) {
					tree.virtualDeleteTaxon(it, deleted);
				}
		}
		else
			deleted = null;
		return false;
	}
	/*.................................................................................................................*/
	public  void calculateStates(Tree tree, CharacterDistribution observedStates, CharacterHistory statesAtNodes, CharacterModel model, MesquiteString resultString, MesquiteNumber stepsObject) {  
		if (observedStates==null || tree == null)
			return;
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (warn(tree, (MeristicDistribution)observedStates, resultString))
			return;
		this.observedStates = observedStates;
		MeristicDistribution cObs = (MeristicDistribution)observedStates;
		String[] items = new String[cObs.getNumItems()*2]; //todo: only do this if needed!!!!
		for (int i= 0; i<cObs.getNumItems(); i++){
			String n;
			if (StringUtil.blank(cObs.getItemName(i)))
				n = "";
			else
				n = cObs.getItemName(i);
			items[i*2] = n + " (min.)";
			items[i*2 + 1] = n + " (max.)";
		}
		((MeristicHistory)statesAtNodes).setItems(items);
		((MeristicHistory)statesAtNodes).deassignStates();

		adjustStorage(tree, observedStates);
		NYLength.setValue(0); //not designed to be thread safe
		for (int item =0; item<cObs.getNumItems(); item++) {
			NYsurvey (tree.getTaxa().getNumTaxa(),(MeristicDistribution)observedStates,  item);
			NYdown(tree, tree.getRoot(deleted), item);
			NYfinal(tree, tree.getRoot(deleted), (MeristicHistory)statesAtNodes, item*2, item*2 +1);
			if (item==0&& stepsObject!=null)
				stepsObject.setValue(NYLength.getValue());
		}
		//place states from finalC and finalD into states at nodes
		if (resultString!=null)
			resultString.setValue("Parsimony reconstruction (Linear Meristic) [Length: " + stepsObject + "]");
	}
	/*.................................................................................................................*/
	public  void calculateSteps(Tree tree, CharacterDistribution observedStates, CharacterModel model, MesquiteString resultString, MesquiteNumber stepsObject) {  
		if (observedStates==null || tree == null ||stepsObject == null)
			return;
		if (stepsObject!=null)
			stepsObject.setToUnassigned();
		if (warn(tree, (MeristicDistribution)observedStates, resultString))
			return;
		this.observedStates = observedStates;
		adjustStorage(tree, observedStates);
		NYLength.setValue(0); //not designed to be thread safe
		NYsurvey (tree.getTaxa().getNumTaxa(),(MeristicDistribution)observedStates,  0);
		NYdown(tree, tree.getRoot(deleted), 0);
		stepsObject.setValue(NYLength.getValue());
		if (resultString!=null)
			resultString.setValue("Parsimony length: " + stepsObject + " (Linear Meristic)");
	}

	/*.................................................................................................................*/
	public boolean compatibleWithContext(CharacterModel model, CharacterDistribution observedStates) {
		if (model==null)
			return false;
		return (model.getName().equalsIgnoreCase("Linear Meristic")) && observedStates instanceof MeristicDistribution;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}

}



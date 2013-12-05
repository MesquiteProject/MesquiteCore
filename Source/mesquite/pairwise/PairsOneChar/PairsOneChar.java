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
package mesquite.pairwise.PairsOneChar;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.pairwise.lib.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class PairsOneChar extends PairMakerChars {
	public String getName() {
		return "Pairs Contrasting in State of One Character";
	}
	public String getExplanation() {
		return "Chooses taxon pairings so as to maximize the number of pairs that are phylogenetically independent, subject to the constraint that each pair shows a contrast the states of a binary character." ;
	}
	TaxaPairerChars pairer;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public void setCharacters(CharacterDistribution statesA, CharacterDistribution statesB){
		if (pairer!=null)
			pairer.setCharacters(statesA, statesB);
	}
	/*.................................................................................................................*/
	public TaxaPairer getPairer(){
		return pairer = new OneCharTaxaPairer(this);
	}
	/*.................................................................................................................*/
	public String getAuthors() {
		return "Wayne P. Maddison";
	}

	public boolean isPrerelease(){
		return false;
	} 
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
}

/* ======================================================================== */
/*Objects of the following class create pairings that contrast the states of a binary character.  The tree is assumed dichotomous.
Most of the relevant code starts with the downPass (find it by searching for "!!!")
Explanation of some basic Mesquite classes:
-- CategoricalAdjustable stores state sets at nodes; state sets are stored as long's with bits on and off to indicate elements present in set
-- Tree objects store node relations as integer arrays; each node is represented by a number.  The node's parent, daughter and sisters can be found
with methods within the Tree object.  For example, tree.motherOfNode(node) is passed a node number and returns the number of the 
parent node.
-- TaxaPath stores a path between terminal taxa
-- TaxaPairing stores a set of such paths (a pairing)
 */
/* ======================================================================== */
class OneCharTaxaPairer extends TaxaPairerChars {
	private CategoricalDistribution observedStates;
	private CategoricalHistory downStates;
	private CategoricalHistory allStatesInClade;
	private int[] stateSought;
	private boolean[] usingFirstChoice, oneChoiceOnly;
	private TaxaPath[] currentPath;
	static final long set0 = 1;  //Mesquite uses long variables with bits on and off for sets; thus the set {0} has bit 0 set, translating to long integer = 1
	static final long set01 = 3; //{0,1} has bits 0 and 1 set, translating to integer = 3
	static final long set1 = 2; //{1} has bit 1 set, translating to integer = 2
	boolean done = false;
	PairMaker ownerModule = null;
	int currentPairing=0;
	int numPairings =0;
	boolean countNum = true;
	private int numPairs=0;

	/*.................................................................................................................*/
	public OneCharTaxaPairer (PairMaker ownerModule) {
		this.ownerModule = ownerModule;
	}
	/*.................................................................................................................*/
	public  void setCharacters(CharacterDistribution statesA, CharacterDistribution statesB){  //only the first of the two characters is used
		if (statesA instanceof CategoricalDistribution)
			observedStates = (CategoricalDistribution)statesA;
		else {
			ownerModule.alert("Warning: Character X for this pair selector must be a categorical-valued character.  An error may result");
			observedStates = null;
		}
		countNum=true;
	}
 	public void init(){
		countNum=true;
	}
	/*.................................................................................................................*/
	/* returns the first pairing*/
	public TaxaPairing getFirstPairing(Tree tree) {
 		if (tree == null)
 			return null;
		TaxaPairing tp = new TaxaPairing(tree.getNumTaxa());
		currentPairing = 0;
		countNum = true;
		if (observedStates != null) {
			done=false;
			int numNodeSpaces = tree.getNumNodeSpaces();
			usingFirstChoice= new boolean[numNodeSpaces];
			oneChoiceOnly = new boolean[numNodeSpaces];
			currentPath = new TaxaPath[numNodeSpaces];
			stateSought = new int[numNodeSpaces];
			downStates=(CategoricalHistory)observedStates.adjustHistorySize(tree,  (CharacterHistory)downStates);
			allStatesInClade= (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)allStatesInClade);
			numPairs = 0;
			downPass(tree.getRoot(), tree);
			firstPairingInClade(tree.getRoot(), tree);
			harvestPaths(tree, tree.getRoot(), tp);
			if (tp.getNumPairs()!= numPairs && !warnedNumPairs){
				MesquiteMessage.println("Error: expected numpairs does not match number harvested, gFP" + tp.getNumPairs() + "  " + numPairs);
				warnedNumPairs = true;
			}
		}
		return tp;
	}
	/*.................................................................................................................*/
	/* returns the next pairing*/
	public TaxaPairing getNextPairing(Tree tree) {
 		if (tree == null)
 			return null;
		TaxaPairing tp = new TaxaPairing(tree.getNumTaxa());
		if (observedStates != null) {
			if (!done) {
				if (!nextPairingInClade(tree, tree.getRoot())) 
					done=true;
				harvestPaths(tree, tree.getRoot(), tp);
				if (tp.getNumPairs()!= numPairs && !warnedNumPairs){
					MesquiteMessage.println("Error: expected numpairs does not match number harvested, gNP" + tp.getNumPairs() + "  " + numPairs);
					warnedNumPairs = true;
				}
				currentPairing++;
			}	
		}
		return tp;
	}
	/*.................................................................................................................*/
	/* returns the index'th pairing*/
	public TaxaPairing getPairing(Tree tree, int index) {
 		if (tree == null)
 			return null;
	TaxaPairing tp = new TaxaPairing(tree.getNumTaxa());
		if (observedStates != null) {
			int numNodeSpaces = tree.getNumNodeSpaces();
			usingFirstChoice= new boolean[numNodeSpaces];
			oneChoiceOnly = new boolean[numNodeSpaces];
			currentPath = new TaxaPath[numNodeSpaces];
			stateSought = new int[numNodeSpaces];
			downStates=(CategoricalHistory)observedStates.adjustHistorySize(tree,  (CharacterHistory)downStates);
			allStatesInClade= (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)allStatesInClade);
			numPairs = 0;
			downPass(tree.getRoot(), tree);
			firstPairingInClade(tree.getRoot(), tree);
			int count=0;

			while (count< index && count<(ownerModule.limitCheckOK(count)) && nextPairingInClade(tree, tree.getRoot())) {
				if (count % 100 == 0) CommandRecord.tick("Skipping pairing " + count);
				count++;
			}
			harvestPaths(tree, tree.getRoot(), tp);
			if (tp.getNumPairs()!= numPairs && !warnedNumPairsI){
				MesquiteMessage.println("Error: expected numpairs does not match number harvested, gP " + index + "  / " + tp.getNumPairs() + "  " + numPairs);
				warnedNumPairsI = true;
			}
			currentPairing= index;
		}
		return tp;
	}
	boolean warnedNumPairsI = false;
	/*.................................................................................................................*/
	/* this is a rather sad method that cycles through all the pairings to find out how many there are.  Hopefully,
 	someone will invent a way to calculate this number so they don't have to be enumerated.*/
	public int getNumPairings(Tree tree){
 		if (tree == null)
 			return 0;
		if (!countNum)
			return numPairings;
		else if (observedStates != null && countNum) {
			int cur = currentPairing;
			int numNodeSpaces = tree.getNumNodeSpaces();
			usingFirstChoice= new boolean[numNodeSpaces];
			oneChoiceOnly = new boolean[numNodeSpaces];
			currentPath = new TaxaPath[numNodeSpaces];
			stateSought = new int[numNodeSpaces];

			downStates=(CategoricalHistory)observedStates.adjustHistorySize(tree,  (CharacterHistory)downStates);
			allStatesInClade= (CategoricalHistory)observedStates.adjustHistorySize(tree, (CharacterHistory)allStatesInClade);

			numPairs = 0;
			downPass(tree.getRoot(), tree);
			firstPairingInClade(tree.getRoot(), tree);

			int count=1;
			int limit;
			while (count<(limit = ownerModule.limitCheckOK(count)) && nextPairingInClade(tree, tree.getRoot())) {
				count++;
				if (count % 10000 ==0) MesquiteMessage.println("Number of pairings so far: " + count);
				if (count % 1000 == 0) CommandRecord.tick("Counting pairings: " + count);
			}
			if (count > limit) 
				count = limit;


			getPairing(tree, cur);
			numPairings = count;
			countNum=false;
			return count;
		}
		else
			return 0;
	}
	/*.................................................................................................................*/
	public int getCurrentPairingNumber(){
		return currentPairing;
	}
	boolean warnedPoly = false;
	boolean warnedOne = false;
	boolean warnedNumPairs = false;
	/*.................................................................................................................*/
	/*!!!*/
	/* This traversal from tips to roots, does parsimony "downpass" and also accumulates allStates for each clade.  allStates records, for
	each clade, whether 0 or 1 or both are observed among the terminal taxa of the clade*/
	private void downPass(int node, Tree tree) {
		if (tree.nodeIsTerminal(node)) {
			long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node)); // get observed state for taxon
			//note: this does not handle missing data, or uncertainties or polymorphisms.
			if (!warnedPoly && CategoricalState.cardinality(observed)!=1){
				MesquiteMessage.println("Warning: pairs (one char) doesn't handle polymorphisms, uncertainties or missing data");
				warnedPoly = true;
			}
			else if (!warnedOne && CategoricalState.maximum(observed)>1){
				MesquiteMessage.println("Warning: pairs (one char) doesn't handle states higher than 1");
				warnedOne = true;
			}
			downStates.setState(node, observed); //set downstate to observed
			allStatesInClade.setState(node, observed);//set allStates to observed
		}
		else {
			int left = tree.firstDaughterOfNode(node);  //assumes dichotomous tree, so get left and right daughter
			int right = tree.lastDaughterOfNode(node);
			downPass(left, tree);
			downPass(right, tree);

			long sRight=downStates.getState(right);  //get downpass states already calculated for right daughter
			long sLeft=downStates.getState(left);//get downpass states already calculated for left daughter
			long intersection = sLeft & sRight;  //intersection

			if (intersection == 0) {
				downStates.setState(node, sLeft | sRight); //use union if intersection empty
				numPairs++; //count a step which must also be counting number of pairs
			}
			else 
				downStates.setState(node, intersection); //use intersection if not empty
			allStatesInClade.setState(node, allStatesInClade.getState(left) | allStatesInClade.getState(right)); // take union for states in clade
		}
	}
	/*.................................................................................................................*/
	/* The following six methods do the second traversal upward through the tree, choosing pairs by 
	creating and continuing paths between terminal taxa. 
	The first four methods are called when traversal needs to take the next step up the tree.  They deal with
	three circumstances, having to do with whether a path is being passed tipward, or a new path is to be formed,
	or no path is to be used.*/
	private void goUpWithNewPath(Tree tree, int node, int left, int right, int seekLeft, int seekRight) {
		if (currentPath[node]==null) 
			currentPath[node]=new TaxaPath();
		currentPath[node].setBase(node);
		goUp(tree, left, right, currentPath[node], currentPath[node], seekLeft, seekRight);
	}
	/*.................................................................................................................*/
	private void goUpWithoutPaths(Tree tree, int left, int right) {
		goUp(tree, left, right, null, null, 0, 0);
	}
	/*.................................................................................................................*/
	private void goUpWithPathLeft(Tree tree, int node, int left, int right) {
		goUp(tree, left, right, currentPath[node],null, stateSought[node], 0);
	}
	/*.................................................................................................................*/
	private void goUpWithPathRight(Tree tree, int node, int left, int right) {
		goUp(tree, left, right, null, currentPath[node],0, stateSought[node]);
	}
	/*.................................................................................................................*/
	private void goUp(Tree tree, int left, int right, TaxaPath pathLeft, TaxaPath pathRight, int seekLeft, int seekRight) {
		currentPath[left]=pathLeft;
		currentPath[right]=pathRight;
		stateSought[left]=seekLeft;
		stateSought[right]=seekRight;
		firstPairingInClade(left, tree);
		firstPairingInClade(right, tree);
	}
	/*.................................................................................................................*/
	/* The method firstPairingInClade traverses the tree from root to tips ("up") and chooses a pairing in the clade.  If more than one
	choice at a node allows a maximal pairing, the first choice is made and the fact that alternatives are available is noted
	so that the alternatives may be constructed later.  It turns out that there are at most two choices at any node for the
	one-character contrast pairing, and so booleans instead of integers are used to record how many choices there are and what is
	the current choice.  The method firstPairingInClade is used along with nextPairingInClade to cycle through all maximal pairings.
	To yield the first pairing, one simply calls the downPass method, followed by firstPairingInClade beginning at the root:
			downPass(tree.getRoot(), tree);
			firstPairingInClade(tree.getRoot(), tree);
	To obtain subsequent pairings, one then calls
			nextPairingInClade(tree, tree.getRoot());
	until nextPairingInClade returns false, indicating that there are no more pairings   */

	private void firstPairingInClade(int node, Tree tree) {
		usingFirstChoice[node]= true; //keep a record of which choice is being made (in case of alternatives)-- in this case, the first choice
		oneChoiceOnly[node]= true; //initialize count of choices to 1 (i.e., set boolean to true)

		if (tree.nodeIsTerminal(node)) { //terminal node; tie any current path to the terminal taxon
			if (currentPath[node]!=null) 
				currentPath[node].setNode(tree, node);
		}
		else {  //internal node
			int left = tree.firstDaughterOfNode(node); //tree assumed dichotomous; get left and right daughter nodes
			int right = tree.lastDaughterOfNode(node);
			if (currentPath[node]==null) {  //no path coming from below; thus must see if time to invent new one based at node
				if (allStatesInClade.getState(node)==set01) {	//if clade uniform: don't need to continue since no need to create paths  (*)
					if (downStates.getState(node)==set01) {  //downstates 01 thus 0/1 or 01/01 on left and right daughters
						if (downStates.getState(left)==set0)   //must be 1 on right; path must cross node, up left and right to allow maximal
							goUpWithNewPath(tree, node, left, right, 0, 1);
						else if  (downStates.getState(left)==set1) //must be 0 on left, and path must go through node
							goUpWithNewPath(tree, node, left, right, 1, 0);
						else 	//else must be 01 on both sides, and path can't go through node
							goUpWithoutPaths(tree, left, right);
					}
					else {  //downstate at node is either {0} or {1}
						int s, notS;  //record state in downstate in s; the other state in notS
						if (downStates.getState(node)==set0) {  // 0 downstate
							s = 0;
							notS = 1;
						}
						else {  // 1 downstate
							s = 1;
							notS = 0;
						}
						oneChoiceOnly[node]= false;  // in each of the circumstances below there must be two choices (create path, or wait)
						if (downStates.getState(left)==set01)  //downstate on right must be {s}
							goUpWithNewPath(tree, node, left, right, notS, s); // seek s on right, and seek notS up left into 01
						else if  (downStates.getState(right)==set01)//downstate on left must be {s}; must exist choice
							goUpWithNewPath(tree, node, left, right, s, notS); // seek s on left, and seek notS up right into 01
						else {	//else downstate must be {s} on both sides
							if (allStatesInClade.getState(left) == set01 )   //Left side contains notS, thus could cost one pair 
								goUpWithNewPath(tree, node, left, right, notS, s); // for first pairing
							else if (allStatesInClade.getState(right) == set01 ) //Right side contains notS, thus could cost one pair
								goUpWithNewPath(tree, node, left, right, s, notS); // for first pairing
							// else is not possible: if allstates uniform would have exited above (*)
						}
					}
				}
			}
			else {  //path is being passed from below because it's not null; need only to choose which side to continue with path
				long setSought;
				if (stateSought[node]==0) //first, translate integer value of state sought into set notation
					setSought = set0;
				else 
					setSought = set1;
				long nodeDownState = downStates.getState(node);
				long leftDownState = downStates.getState(left);
				long rightDownState = downStates.getState(right);
				if (nodeDownState==set01) {  // 01 downstate; must choose to send path up stateSought side if there is one
					if (leftDownState == setSought) 
						goUpWithPathLeft(tree, node, left, right); //must send stateSought into stateSought side
					else if (rightDownState == setSought) 
						goUpWithPathRight(tree, node, left, right); //must send stateSought into stateSought side
					else  {  //must be 01 downstate on both sides
						oneChoiceOnly[node]= false;
						goUpWithPathLeft(tree, node, left, right);//first pairing choose left side
					}
				}
				else if (nodeDownState==setSought) {  // stateSought downstate; choose either side that has stateSought downstate
					if (rightDownState == setSought && leftDownState == setSought) { //both with stateSought; choose either to send path
						oneChoiceOnly[node]= false;
						goUpWithPathLeft(tree, node, left, right);//first pairing choose left side
					}
					else if (leftDownState == setSought) 
						goUpWithPathLeft(tree, node, left, right);   //must send stateSought into stateSought side
					else if (rightDownState == setSought) 
						goUpWithPathRight(tree, node, left, right); //must send stateSought into stateSought side
				}
				else  {  // downstate is opposite of state sought; choose either side that has stateSought in it
					if (allStatesInClade.getState(left)==set01 && allStatesInClade.getState(right)==set01) { //both have stateSought; choose either
						oneChoiceOnly[node]= false;
						goUpWithPathLeft(tree, node, left, right);//first pairing choose left side
					}
					else if (allStatesInClade.getState(left)==set01)  //only left has stateSought; choose it for passing path up
						goUpWithPathLeft(tree, node, left, right);
					else if (allStatesInClade.getState(right)==set01)  //only right has stateSought; choose it for passing path up
						goUpWithPathRight(tree, node, left, right);
				}
			}
		}
	}

	/*.................................................................................................................*/
	/* This method makes the next choice at node, and continues up tree with first pairing given that choice*/
	private   void nextChoiceAtNode(int node, Tree tree) {  //this method needs to cover only those cases in which multiple choices could arise
		usingFirstChoice[node] =false; //record that no longer using first choice
		if (tree.nodeIsInternal(node)) {  //terminal nodes never have choice
			int left = tree.firstDaughterOfNode(node);
			int right = tree.lastDaughterOfNode(node);
			if (currentPath[node]==null || currentPath[node].getBase()==node) {  //no path coming from below or path based at node
				//in firstPairingInClade, every case where new path created had as second choice going up without paths; thus destroy path
				if (currentPath[node]!=null) 
					currentPath[node]=null;
				if (allStatesInClade.getState(node)==set01)	//clade not uniform, thus fruitful to continue
					goUpWithoutPaths(tree, left, right);  // every possibility with multiple choices postpones pairing and thus goes up without path
			}
			else {  //path is not null; need only to choose which side to continue.
				//In each case in firstPairingInClade, left path was followed first.  Now follow right.
				long s = downStates.getState(node);
				if (s==set01)  // 01 downstate; must choose stateSought side if there is one
					goUpWithPathRight(tree, node, left, right);// second pairing for case of downstate 01 both sides
				else if ((s==set0 && stateSought[node]==0)||(s==set1 && stateSought[node]==1))  //stateSought downstate; choose either side with stateSought
					goUpWithPathRight(tree, node, left, right);// second pairing; both with stateSought				}
				else if ((s==set0 && stateSought[node]==1)||(s==set1 && stateSought[node]==0))  //other downstate; choose either side with stateSought in it
					goUpWithPathRight(tree, node, left, right);// second pairing; both with stateSought
			}
		}
	}
	/*.................................................................................................................*/
	/*This is the central method that moves through the alternative pairings.  It returns false if there are no more pairings to be made in the clade.
	It is rather a difficult recursion to comprehend (at least to me, even though I wrote it), and is based on the Equivocal cycling 
	recursion in MacClade 3.
	 */
	private boolean nextPairingInClade(Tree tree, int node){
		boolean moreChoices = true;
		if (tree.nodeIsInternal(node)) {
			int left = tree.firstDaughterOfNode(node);
			int right = tree.lastDaughterOfNode(node);
			if (!nextPairingInClade(tree, left)){  //try going to next pairing on left; if false then had already reached last choice on left clade
				if (!nextPairingInClade(tree, right)){ //try going to next pairing on right; if false then had already reached last choice on right clade
					// left and right choices exhausted, thus go to next choice at node if there is one
					if (oneChoiceOnly[node] || usingFirstChoice[node] ==oneChoiceOnly[node])  //at maximum choice; return false to indicate choices exhausted
						moreChoices = false;
					else  // still other choices available at this node; do next one
						nextChoiceAtNode(node, tree);
				}
				else { //if right wasn't at last choice it would have been incremented within nextPairingInClade(right), thus reset left back to first choice
					if (currentPath[left]!=null && currentPath[left].getBase() == left)  //delete path based at left so it won't think it's being passed from below
						currentPath[left] = null;
					firstPairingInClade(left, tree);
				}
			} //else had successfully incremented left side to next choice
		}
		else //terminal node with one choice only
			moreChoices = false;
		return moreChoices;
	}
	/*.................................................................................................................*/
	/* harvest all the paths and put them in the pairing*/
	private   void harvestPaths(Tree tree, int node, TaxaPairing tp) {
		if (tree.nodeIsInternal(node)) {
			int left = tree.firstDaughterOfNode(node);
			int right = tree.lastDaughterOfNode(node);
			harvestPaths(tree, left, tp);
			harvestPaths(tree, right, tp);
			if (currentPath[node]!=null && currentPath[node].getBase()==node) 
				tp.addPath(currentPath[node]);
		}
	}
}





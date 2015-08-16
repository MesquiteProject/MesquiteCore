/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.pairwise.PairsNoChars;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.pairwise.lib.*;

/* ======================================================================== */
public class PairsNoChars extends PairMakerChars {
	public String getName() {
		return "Most Pairs";
	}
	public String getExplanation() {
		return "Chooses taxon pairings regardless of contrast in a character, so as to maximize the number of pairs that are phylogenetically independent." ;
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
		return pairer = new NoCharTaxaPairer(this);
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	} 
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}

}

/*Assumes  dichotomous.  On way down, store maximum numbers.  On way up, pass forward TaxaPath objects and state sought.  Each TaxaPath object should store
two taxa which it should decide once it gets to tips.  A new TaxaPath object is formed each time an open clade is created by a lack of committed TaxaPath sought
rising from below.  At each internal node must be stored the current choice at that internal node and the list of choices possible,
 so that all possible choices can be cycled through.  A choice is whether to continue TaxaPath up right or left if one is coming from below, or, if none
 is coming up, whether a TaxaPath can be started (which it always can be 
 ======================================================================== */
class NoCharTaxaPairer extends TaxaPairerChars {
	private boolean firstTime=true;
	int[][] maxPairs;
	final static int pathFromBelow = 0;
	final static int noPathFromBelow = 1;
	final static int pathAcrossRoot =2;
	final static int noPathAcrossRoot = 3;
	final static int pathUpLeft = 4;
	final static int pathUpRight = 5;
	final static int numConditions = 6;

	private int numPairs=0;
	Tree tree;
	private int[] currentChoice, numChoices;
	private TaxaPath[] currentPath;
	static final boolean lastChoice = true;
	boolean done = false;
	PairMaker ownerModule = null;
	int currentPairing=0;

	public NoCharTaxaPairer (PairMaker ownerModule) {
		this.ownerModule = ownerModule;
	}
	public void init(){
	}
	/* ..................................................................................................................................... */
	private void diagnoseDownPass(int node){
		System.out.println("==== node " + node + " ==== ");
		System.out.println("     max under noPathFromBelow " + maxPairs[noPathFromBelow][node]);
		System.out.println("     max under pathFromBelow " + maxPairs[pathFromBelow][node]);
		System.out.println("     max under pathAcrossRoot " + maxPairs[pathAcrossRoot][node]);
		System.out.println("     max under noPathAcrossRoot " + maxPairs[noPathAcrossRoot][node]);
		System.out.println("     max under pathUpLeft " + maxPairs[pathUpLeft][node]);
		System.out.println("     max under pathUpRight " + maxPairs[pathUpRight][node]);
	}
	boolean warnedSomeIllegal = false;
	int[] legality;
	CharacterState csA, csB;
	CharacterDistribution observedStatesA, observedStatesB;
	/*.................................................................................................................*/
	private void setLegality(int node, Tree tree) {
		if (tree.nodeIsTerminal(node)) {
			csA =observedStatesA.getCharacterState(csA, tree.taxonNumberOfNode(node)); //get observed states
			csB =observedStatesB.getCharacterState(csB, tree.taxonNumberOfNode(node)); //get observed states
			if ((csA == null || csA.isUnassigned() || csA.isInapplicable()) ||  //either of the two characters is missing data
					(csB == null || csB.isUnassigned() || csB.isInapplicable())) //either of the two characters is inapplicable
				legality[node] = MesquiteTree.ILLEGAL;
			else
				legality[node] = MesquiteTree.LEGAL;
			if (legality[node] == MesquiteTree.ILLEGAL)
				warningMessage = "Some taxa excluded (had character states that are missing or inapplicable).";

		}
		else {
			int left = tree.firstDaughterOfNode(node);  //assumes dichotomous tree, so get left and right daughter
			int right = tree.lastDaughterOfNode(node);
			setLegality(left, tree);
			setLegality(right, tree);
			if (legality[left] != MesquiteTree.ILLEGAL && legality[right] != MesquiteTree.ILLEGAL )
				legality[node] = MesquiteTree.LEGAL;
			else if (legality[left] != MesquiteTree.ILLEGAL || legality[right] != MesquiteTree.ILLEGAL)
				legality[node] = MesquiteTree.SEMILEGAL;
			else
				legality[node] = MesquiteTree.ILLEGAL;
		}
	}
	/*.................................................................................................................*/
	private void legalityCheck(Tree tree) {
		if (legality == null || legality.length != tree.getNumNodeSpaces())
			legality = new int[tree.getNumNodeSpaces()];
		for (int i = 0; i< legality.length; i++)
			legality[i] = MesquiteTree.LEGAL;
		warningMessage = "";
		if (observedStatesA== null || observedStatesB == null)// not needed except to exclude taxa with missing/inapplicable
			return;
		setLegality(tree.getRoot(), tree);
	}	/* ..................................................................................................................................... */
	private void downPass(Tree tree, int node) {
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextLegalSisterOfNode(daughter, legality))
			downPass(tree, daughter);
		if (tree.nodeIsInternal(node)) {
			int left = tree.firstLegalDaughterOfNode(node, legality);
			int right = tree.lastLegalDaughterOfNode(node, legality);

			if (tree.nodeIsInternal(left) && tree.nodeIsInternal(right)) {  // both are internal
				maxPairs[noPathFromBelow][node] = MesquiteInteger.maximum(maxPairs[noPathFromBelow][left] + maxPairs[noPathFromBelow][right], maxPairs[pathFromBelow][left]  + maxPairs[pathFromBelow][right]  + 1);
				maxPairs[pathFromBelow][node] = MesquiteInteger.maximum(maxPairs[noPathFromBelow][left] + maxPairs[pathFromBelow][right], maxPairs[pathFromBelow][left]  + maxPairs[noPathFromBelow][right]);
				maxPairs[pathAcrossRoot][node] = maxPairs[pathFromBelow][left]  + maxPairs[pathFromBelow][right]  + 1;
				maxPairs[noPathAcrossRoot][node] = MesquiteInteger.maximum(maxPairs[pathAcrossRoot][left], maxPairs[noPathAcrossRoot][left]) +  MesquiteInteger.maximum(maxPairs[pathAcrossRoot][right], maxPairs[noPathAcrossRoot][right]);
				maxPairs[pathUpLeft][node] = maxPairs[pathFromBelow][left] + maxPairs[noPathFromBelow][right];
				maxPairs[pathUpRight][node] = maxPairs[noPathFromBelow][left] + maxPairs[pathFromBelow][right];
			}
			else if (tree.nodeIsInternal(left)) {  //right only must be terminal
				maxPairs[noPathFromBelow][node] = MesquiteInteger.maximum(maxPairs[noPathFromBelow][left], maxPairs[pathFromBelow][left]  + 1);
				maxPairs[pathFromBelow][node] = MesquiteInteger.maximum(maxPairs[pathFromBelow][left], maxPairs[noPathFromBelow][left]);
				maxPairs[pathAcrossRoot][node] = maxPairs[pathFromBelow][left]  +1;
				maxPairs[noPathAcrossRoot][node] = MesquiteInteger.maximum(maxPairs[pathAcrossRoot][left], maxPairs[noPathAcrossRoot][left]);
				maxPairs[pathUpLeft][node] = maxPairs[pathFromBelow][left];
				maxPairs[pathUpRight][node] = maxPairs[noPathFromBelow][left];
			}
			else if (tree.nodeIsTerminal(right)) { //both be terminal
				maxPairs[noPathFromBelow][node] = 1;
				maxPairs[pathFromBelow][node] = 0;
				maxPairs[pathAcrossRoot][node] = 1;
				maxPairs[noPathAcrossRoot][node] = 0;
				maxPairs[pathUpLeft][node] = 0;
				maxPairs[pathUpRight][node] = 0;
			}
			else {	// left only must be terminal
				maxPairs[noPathFromBelow][node] = MesquiteInteger.maximum(maxPairs[noPathFromBelow][right], maxPairs[pathFromBelow][right]  + 1);
				maxPairs[pathFromBelow][node] = MesquiteInteger.maximum(maxPairs[pathFromBelow][right], maxPairs[noPathFromBelow][right]);
				maxPairs[pathAcrossRoot][node] = maxPairs[pathFromBelow][right]  +1;
				maxPairs[noPathAcrossRoot][node] = MesquiteInteger.maximum(maxPairs[pathAcrossRoot][right], maxPairs[noPathAcrossRoot][right]);
				maxPairs[pathUpLeft][node] = maxPairs[noPathFromBelow][right];
				maxPairs[pathUpRight][node] = maxPairs[pathFromBelow][right];
			}
			//diagnoseDownPass(node);
		}
	}

	/* ..................................................................................................................................... */
	public  void setCharacters(CharacterDistribution statesA, CharacterDistribution statesB){
		observedStatesA = statesA;
		observedStatesB = statesB;
	}
	/*.................................................................................................................*/
	public TaxaPairing getFirstPairing(Tree tree) {
		if (tree == null)
			return null;
		TaxaPairing tp = new TaxaPairing(tree.getNumTaxa());
		currentPairing = 0;
		if (tree.hasPolytomies(tree.getRoot())){
			warningMessage = "The tree has polytomies; pairwise comparisons cannot be done";
			tp.setCalculationNotDone(true);
			return tp;
		}
		done=false;
		int num = tree.getNumNodeSpaces();
		currentChoice= new int[num];
		numChoices = new int[num];
		currentPath = new TaxaPath[num];
		maxPairs = new int[numConditions][num];
		legalityCheck(tree);
		downPass(tree, tree.getRoot());
		numPairs = maxPairs[noPathFromBelow][ tree.getRoot()];
		firstPairingInClade(tree.getRoot(), tree);
		firstTime=false;
		harvestPaths(tree, tree.getRoot(), tp);
		if (tp.getNumPairs()!= numPairs && !warnedNumPairs){
			MesquiteMessage.println("Error: expected numpairs does not match number harvested, gFP" + tp.getNumPairs() + "  " + numPairs);
			warnedNumPairs = true;
		}
		return tp;
	}
	/*.................................................................................................................*/
	public TaxaPairing getNextPairing(Tree tree) {
		if (tree == null)
			return null;
		TaxaPairing tp = new TaxaPairing(tree.getNumTaxa());
		if (tree.hasPolytomies(tree.getRoot())){
			warningMessage = "The tree has polytomies; pairwise comparisons cannot be done";
			tp.setCalculationNotDone(true);
			return tp;
		}

		if (!done) {
			if (nextPairingInClade(tree, tree.getRoot())==lastChoice) 
				done=true;
			harvestPaths(tree, tree.getRoot(), tp);
			if (tp.getNumPairs()!= numPairs && !warnedNumPairs){
				MesquiteMessage.println("Error: expected numpairs does not match number harvested, gNP" + tp.getNumPairs() + "  " + numPairs);
				warnedNumPairs = true;
			}
			currentPairing++;
		}	
		return tp;
	}
	boolean warnedNumPairs = false;
	boolean warnedNumPairsI = false;
	/*.................................................................................................................*/
	public TaxaPairing getPairing(Tree tree, int index) {
		if (tree == null)
			return null;
		TaxaPairing tp = new TaxaPairing(tree.getNumTaxa());
		if (tree.hasPolytomies(tree.getRoot())){
			warningMessage = "The tree has polytomies; pairwise comparisons cannot be done";
			tp.setCalculationNotDone(true);
			return tp;
		}

		int num = tree.getNumNodeSpaces();
		currentChoice= new int[num];
		numChoices = new int[num];
		currentPath = new TaxaPath[num];

		maxPairs = new int[numConditions][num];

		legalityCheck(tree);
		downPass(tree, tree.getRoot());
		numPairs = maxPairs[noPathFromBelow][ tree.getRoot()];
		firstPairingInClade(tree.getRoot(), tree);

		int count=0;

		while (count< index && count<(ownerModule.limitCheckOK(count)) && nextPairingInClade(tree, tree.getRoot())!=lastChoice) {
			if (count % 100 == 0) CommandRecord.tick("Skipping pairing " + count);
			count++;
		}
		harvestPaths(tree, tree.getRoot(), tp);
		if (tp.getNumPairs()!= numPairs && !warnedNumPairsI){
			MesquiteMessage.println("Error: expected numpairs does not match number harvested, gP " + index + "  / " + tp.getNumPairs() + "  " + numPairs);
			warnedNumPairsI = true;
		}
		currentPairing= index;

		return tp;
	}
	/*.................................................................................................................*/
	public int getNumPairings(Tree tree){
		if (tree == null)
			return 0;
		if (tree.hasPolytomies(tree.getRoot())){
			warningMessage = "The tree has polytomies; pairwise comparisons cannot be done";
			return 0;
		}


		int cur = currentPairing;
		int num = tree.getNumNodeSpaces();
		currentChoice= new int[num];
		numChoices = new int[num];
		currentPath = new TaxaPath[num];

		maxPairs = new int[numConditions][num];
		legalityCheck(tree);
		downPass(tree, tree.getRoot());
		numPairs = maxPairs[noPathFromBelow][ tree.getRoot()];
		firstPairingInClade(tree.getRoot(), tree);

		int count=1;
		int limit;
		while (count<(limit = ownerModule.limitCheckOK(count)) && (nextPairingInClade(tree, tree.getRoot())!=lastChoice)) {
			count++;
			if (count % 10000 ==0) MesquiteMessage.println("Number of pairings so far: " + count);
			if (count % 1000 == 0) CommandRecord.tick("Counting pairings: " + count);
		}
		if (count > limit) 
			count = limit;


		getPairing(tree, cur);
		return count;

	}
	/*.................................................................................................................*/
	public int getCurrentPairingNumber(){
		return currentPairing;
	}
	/*.................................................................................................................*/
	private void goUpWithNewPath(Tree tree, int node, int left, int right) {
		if (currentPath[node]==null) 
			currentPath[node]=new TaxaPath();
		currentPath[node].setBase(node);
		currentPath[left]=currentPath[node];
		currentPath[right]=currentPath[node];
		firstPairingInClade(left, tree);
		firstPairingInClade(right, tree);
	}
	/*.................................................................................................................*/
	private void goUpWithoutPath(Tree tree, int left, int right) {
		currentPath[left]=null;
		currentPath[right]=null;
		firstPairingInClade(left, tree);
		firstPairingInClade(right, tree);
	}
	/*.................................................................................................................*/
	private void goUpWithPath(Tree tree, int left, int right, TaxaPath pathLeft, TaxaPath pathRight) {
		currentPath[left]=pathLeft;
		currentPath[right]=pathRight;
		firstPairingInClade(left, tree);
		firstPairingInClade(right, tree);
	}

	/*.................................................................................................................*/
	/* Go up tree, choosing pairing in clade by taking first choices wherever a choice is available*/
	private void firstPairingInClade(int node, Tree tree) {
		currentChoice[node]=0;
		numChoices[node]= 1;

		if (tree.nodeIsTerminal(node)) { //terminal node
			if (currentPath[node]!=null) {
				currentPath[node].setNode(tree, node);
			}
		}
		else {  //internal node
			int left = tree.firstLegalDaughterOfNode(node, legality);
			int right = tree.lastLegalDaughterOfNode(node, legality);
			if (currentPath[node]==null) {  //no path coming from below; thus must see if time to invent new one based at node
				//first, calculate how many choice are at node under the conditions

				/* if no path from below, then must decide whether to begin path or not.  Should begin path if |pathAcrossRoot is as good as |noPathAcrossRoot */
				if (maxPairs[pathAcrossRoot][node] == maxPairs[noPathAcrossRoot][node]) {
					numChoices[node] = 2;
					goUpWithNewPath(tree,  node, left, right);
				}
				else { 
					numChoices[node] = 1;
					if (maxPairs[pathAcrossRoot][node] > maxPairs[noPathAcrossRoot][node])
						goUpWithNewPath(tree,  node, left, right);
					else
						goUpWithoutPath(tree, left, right);
				}
			}
			else {  //path is being passed from below because it's not null; need only to choose which side to continue with path
				if (maxPairs[pathUpLeft][node] == maxPairs[pathUpRight][node]) {
					numChoices[node] = 2;
					goUpWithPath(tree, left, right, currentPath[node], null);
				}
				else {
					numChoices[node]=1;
					if (maxPairs[pathUpLeft][node] > maxPairs[pathUpRight][node]) {
						goUpWithPath(tree, left, right, currentPath[node], null);
					}
					else {
						goUpWithPath(tree, left, right, null, currentPath[node]);
					}
				}
			}
		}
	}

	/*.................................................................................................................*/
	/* Go to next choice at node, and continue up tree with first pairing given that choice */
	private   void nextChoiceAtNode(int node, Tree tree) {  //this method needs to cover only those cases in which multiple choices could arise
		currentChoice[node]++;
		if (tree.nodeIsInternal(node) && currentChoice[node]<numChoices[node]) {  //(terminal nodes never with choice)
			int left = tree.firstLegalDaughterOfNode(node, legality);
			int right = tree.lastLegalDaughterOfNode(node, legality);
			if (currentPath[node]==null ) {  
				goUpWithoutPath(tree, left, right);  //path not coming from below; only choice is to go without path
			}
			else if (currentPath[node].getBase()==node) {  
				currentPath[node]=null;
				goUpWithoutPath(tree, left, right);  //path not coming from below; only choice is to go without path
			}
			else {  //path is not null; need only to make next choice as to which side to continue, i.e. right side
				goUpWithPath(tree, left, right, null, currentPath[node]);
			}
		}
	}
	/*_________________________________________________*/
	private boolean nextPairingInClade(Tree tree, int node){
		boolean isItLastChoice = !lastChoice;
		if (tree.nodeIsInternal(node)) {
			int left = tree.firstLegalDaughterOfNode(node, legality);
			int right = tree.lastLegalDaughterOfNode(node, legality);
			//check if all above on left are at last choice; this function goes to next choice if not yet at last choice
			if (nextPairingInClade(tree, left) == lastChoice){  //reached last choice on left clade
				//check if all above on right are at last choice; this function goes to next choice if not yet at last choice
				if (nextPairingInClade(tree, right) == lastChoice){ //reached last choice on right clade
					// left and right choices exhausted, thus go to next choice at node if there is one
					if (currentChoice[node]+1 >=numChoices[node])  //at maximum choice; return lastChoice to indicate
						isItLastChoice = lastChoice;
					else  // still other choices available at this node; do next one
						nextChoiceAtNode(node, tree);
				}
				else {  // if right wasn't at last choice it would have been incremented within the nextPairingInClade(right) call, thus reset left back to first choice
					isItLastChoice = !lastChoice;
					if (currentPath[left]!=null && currentPath[left].getBase() == left)   // need to delete path based at left so it won't think it's being passed up from below
						currentPath[left] = null;
					firstPairingInClade(left, tree);
				}
			}
		}
		else //terminal node with one choice only
			isItLastChoice = lastChoice;
		return isItLastChoice;
	}
	/*.................................................................................................................*/

	private   void harvestPaths(Tree tree, int node, TaxaPairing tp) {
		if (tree.nodeIsInternal(node)) {
			int left = tree.firstLegalDaughterOfNode(node, legality);
			int right = tree.lastLegalDaughterOfNode(node, legality);
			harvestPaths(tree, left, tp);
			harvestPaths(tree, right, tp);
			if (currentPath[node]!=null && currentPath[node].getBase()==node) 
				tp.addPath(currentPath[node]);
		}
	}
}





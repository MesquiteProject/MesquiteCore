/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.pairwise.PairsTwoChars;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.pairwise.lib.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class PairsTwoChars extends PairMakerChars {
	public String getName() {
		return "Pairs Contrasting in State of Two Characters";
	}
	public String getExplanation() {
		return "Chooses taxon pairings so as to maximize the number of pairs that are phylogenetically independent, subject to the constraint that each pair shows a contrast the states of both of two binary characters." ;
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
		return pairer = new TwoCharTaxaPairer(this);
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

/* ======================================================================== 

The following classes create pairings that contrast the states of two binary characters.
The tree is assumed dichotomous.
At present, the code is probably rather inefficient, since on the traversal back up the tree recalculates things it could
have stored on the way down.

Explanation of some basic Mesquite classes:
-- CategoricalDistribution stores state sets at nodes; state sets are stored as long's with bits on and off to indicate elements present in set
-- Tree objects store node relations as integer arrays; each node is represented by a number.  The node's parent, daughter and sisters can be found
with methods within the Tree object.  For example, tree.motherOfNode(node) is passed a node number and returns the number of the 
parent node.
-- TaxaPath stores a path between terminal taxa
-- TaxaPairing stores a set of such paths (a pairing)
 */

/*  ======================================================================== */
/*Scans to see what are best conditons at node (i.e., which free path condition maximizes number of 
pairs (i.e., whether max given no free path is better than max given free path to 01)*/
class BestConditionsAtNode {
	boolean[] isItBest;
	int bestValue=0;
	int numBest = 0;
	/*..................................................*/
	public BestConditionsAtNode() {
		isItBest = new boolean[5];
	}
	/*..................................................*/
	/* Scans the maximum array for a given node to store which values are highest.
	Rather inefficiently done (sorts array first).*/
	public void scan(int[][] max, int node) {
		int[] q = new int[5];
		int swap;
		for (int i=0; i<5; i++) { //initialize results
			q[i] = max[i][node];
			isItBest[i] = false;
		}
		for (int i = 1; i < 5; i++) { //sort array
			for (int j= i-1; j>=0 && (q[j+1]>q[j]); j--) {
				swap = q[j+1];
				q[j+1] = q[j];
				q[j] = swap;
			}
		}
		numBest = 1; //find out how many are best
		for (int i = 1; i < 5; i++) {
			if (q[i] == q[0])
				numBest++;
		}
		bestValue = q[0]; //record which is best
		for (int i = 0; i < 5; i++) {
			if (max[i][node] == bestValue)
				isItBest[i] = true;
		}
	}
	/*..................................................*/
	public int getBestValue() {
		return bestValue;
	}
	/*..................................................*/
	public int getNumBest() {
		return numBest;
	}
	/*..................................................*/
	public boolean isBest(int which) {
		return isItBest[which];
	}
}
/*  ======================================================================== */
/* For a given condition at a node, e.g. given free path to 01, there are various possible combinations
of such conditions at the two descendants left and right that could allow such a conditon to be obtained at node.
Which of those conditions allows the maximum number of pairs?  This is the best combination of left
and right that serves as a basis for the condition at node.  Objects of this class determine these combinations
and store them.*/
class BestBasisCombos {
	int[][] bestCombos;
	final static int maxNumCombos=9;
	int bestValue=0;
	int numBest = 0;
	int[][] max;
	public BestBasisCombos() {
		bestCombos = new int[2][maxNumCombos];
	}
	public void initialize (int[][] max) {
		this.max = max;
		bestValue = -1;
		numBest = 0;
		zeroCombos();
	}
	public void zeroCombos() {
		for (int i= 0; i<2; i++) for (int j = 0; j<maxNumCombos; j++) bestCombos[i][j]=0;
	}
	/* Checks if the combination of conditionRight and conditionLeft is one of the best for these
	two nodes left and right, taking into consideration the supplement (possible 1 extra pair between the two clades).
	If so, records the combination as one of the best.*/
	public void checkIfBest(int conditionLeft, int conditionRight, int left, int right, int supplement) {
		if (max[conditionLeft][left]>=0 && max[conditionRight][right] >=0) {  
			//note below in downPass that max =  -1 used as indication of terminal node without states corresponding to condition
			int t = max[conditionLeft][left] + max[conditionRight][right] + supplement;

			if (t>bestValue) {
				zeroCombos();
				bestValue = t;
				bestCombos[0][0] = conditionLeft;
				bestCombos[1][0] = conditionRight;
				numBest = 1;
			}
			else if (t == bestValue) {
				bestCombos[0][numBest] = conditionLeft;
				bestCombos[1][numBest] = conditionRight;
				numBest++;
			}
		}
	}
	public void checkIfBest(int conditionLeft, int conditionRight, int left, int right) {
		checkIfBest(conditionLeft, conditionRight, left, right, 0);
	}
	public int getBestValue() {
		return bestValue;
	}
	public int getNumBest() {
		return numBest;
	}
	public int getBestLeft(int which) {
		if (which>=numBest)
			MesquiteMessage.println("error; ask for combo beyond bounds");
		return bestCombos[0][which];
	}
	public int getBestRight(int which) {
		if (which>=numBest)
			MesquiteMessage.println("error; ask for combo beyond bounds");
		return bestCombos[1][which];
	}
}
/*This is the class that actually supplies the pairings.*/
/* ======================================================================== */
class TwoCharTaxaPairer extends TaxaPairerChars {
	int[][] max;
	//The following are constants for the various conditions on local pairings:  there is no free path to a terminal taxon,
	//there is a free path to a taxon with states 0 and 0 in the two characters, there is a free path to 01, etc.
	final static int noFree = 4;
	final static int free00 = 0;
	final static int free01 = 1;
	final static int free10 = 2;
	final static int free11 = 3;
	final static int noCondition = -1;
	final static int numConditions = 5;

	Tree tree;
	CharacterDistribution observedStatesA; CharacterDistribution observedStatesB;
	BestConditionsAtNode surveyOfBestConditions;
	private int[] currentChoice, numChoices;
	private int[] conditionSought;
	private TaxaPath[] currentPath;
	static final long set0 = 1;
	static final long set01 = 3;
	static final long set1 = 2;
	boolean done = false;
	PairMaker ownerModule = null;
	int currentPairing=0;
	BestBasisCombos bestCombos;
	private int numPairs=0;

	/* ..................................................................................................................................... */
	public TwoCharTaxaPairer (PairMaker ownerModule) {
		this.ownerModule = ownerModule;
		surveyOfBestConditions = new BestConditionsAtNode();
		bestCombos = new BestBasisCombos();
	}
	/* ..................................................................................................................................... */
	public  void setCharacters(CharacterDistribution statesA, CharacterDistribution statesB){
		boolean warned = false;
		if (statesA instanceof CategoricalDistribution)
			this.observedStatesA = statesA;
		else {
			warned = true;
			ownerModule.alert("Warning: both characters for this Pair Selector must be categorical. An error may result.");
			this.observedStatesA = null;
		}
		if (statesB instanceof CategoricalDistribution)
			this.observedStatesB = statesB;
		else {
			if (!warned)
				ownerModule.alert("Warning: both characters for this Pair Selector must be categorical. An error may result.");
			this.observedStatesB = null;
		}
	}
	/*.................................................................................................................*/
	public int maximum (int a, int b, int c, int d, int e) {
		return MesquiteInteger.maximum(a,MesquiteInteger.maximum(b,MesquiteInteger.maximum(c,MesquiteInteger.maximum(d,e))));
	}
	/*.................................................................................................................*/
	public TaxaPairing getFirstPairing(Tree tree) {
 		if (tree == null)
 			return null;
		TaxaPairing tp = new TaxaPairing(tree.getNumTaxa());
		currentPairing = 0;
		legalityCheck( tree);
		if (tree.hasPolytomies(tree.getLegalRoot(legality))){
			warningMessage = "The tree has polytomies; pairwise comparisons cannot be done";
			tp.setCalculationNotDone(true);
		}
		else if (observedStatesA != null && observedStatesB != null) {
			done=false;
			int num = tree.getNumNodeSpaces();
			currentChoice= new int[num];
			numChoices = new int[num];
			currentPath = new TaxaPath[num];
			conditionSought = new int[num];
			conditionSought[tree.getLegalRoot(legality)] = noCondition;
			max = new int[numConditions][num];
			int root = tree.getLegalRoot(legality);
			downPass(tree, root);
			numPairs = maximum(max[noFree][root] ,max[free00][root] ,max[free01][root],max[free11][root],max[free10][root]);

			firstPairingInClade(tree.getLegalRoot(legality), tree);
			harvestPaths(tree, tree.getLegalRoot(legality), tp);
			if (tp.getNumPairs()!= numPairs && !warnedNumPairs){
				MesquiteMessage.println("Error: expected numpairs does not match number harvested, gFP" + tp.getNumPairs() + "  " + numPairs);
				warnedNumPairs = true;
			}
		}
		return tp;
	}
	/*.................................................................................................................*/
	public TaxaPairing getNextPairing(Tree tree) {
 		if (tree == null)
 			return null;
		TaxaPairing tp = new TaxaPairing(tree.getNumTaxa());
		if (tree.hasPolytomies(tree.getLegalRoot(legality))){
			warningMessage = "The tree has polytomies; pairwise comparisons cannot be done";
			tp.setCalculationNotDone(true);
		}
		else if (observedStatesA != null && observedStatesB != null) {
			if (!done) {
				if (!nextPairingInClade(tree, tree.getLegalRoot(legality))) 
					done=true;
				harvestPaths(tree, tree.getLegalRoot(legality), tp);
				if (tp.getNumPairs()!= numPairs && !warnedNumPairs){
					MesquiteMessage.println("Error: expected numpairs does not match number harvested, gNP" + tp.getNumPairs() + "  " + numPairs);
					warnedNumPairs = true;
				}
				currentPairing++;
			}	
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
		legalityCheck(tree);
		if (tree.hasPolytomies(tree.getLegalRoot(legality))){
			warningMessage = "The tree has polytomies; pairwise comparisons cannot be done";
			tp.setCalculationNotDone(true);
		}
		else if (observedStatesA != null && observedStatesB != null) {
			int num = tree.getNumNodeSpaces();
			currentChoice= new int[num];
			numChoices = new int[num];
			currentPath = new TaxaPath[num];

			max = new int[numConditions][num];

			int root = tree.getLegalRoot(legality);
			downPass(tree, root);
			numPairs = maximum(max[noFree][root] ,max[free00][root] ,max[free01][root],max[free11][root],max[free10][root]);
			firstPairingInClade(tree.getLegalRoot(legality), tree);

			int count=0;

			while (count< index && count<(ownerModule.limitCheckOK(count)) && nextPairingInClade(tree, tree.getLegalRoot(legality))) {
				if (count % 100 == 0) CommandRecord.tick("Skipping pairing " + count);
				count++;
			}
			harvestPaths(tree, tree.getLegalRoot(legality), tp);
			if (tp.getNumPairs()!= numPairs && !warnedNumPairsI){
				MesquiteMessage.println("Error: expected numpairs does not match number harvested, gP " + index + "  / " + tp.getNumPairs() + "  " + numPairs);
				warnedNumPairsI = true;
			}
			currentPairing= index;
		}
		return tp;
	}
 	public void init(){
	}
	/*.................................................................................................................*/
	/* this is a rather sad method that cycles through all the pairings to find out how many there are.  Hopefully,
 	someone will invent a way to calculate this number so they don't have to be enumerated.*/
	public int getNumPairings(Tree tree){
 		if (tree == null)
 			return 0;
		legalityCheck(tree);
 		if (tree.hasPolytomies(tree.getLegalRoot(legality))){
			warningMessage = "The tree has polytomies; pairwise comparisons cannot be done";
			return 0;
		}
		else if (observedStatesA != null && observedStatesB != null) {
			int cur = currentPairing;
			int num = tree.getNumNodeSpaces();
			currentChoice= new int[num];
			numChoices = new int[num];
			currentPath = new TaxaPath[num];

			max = new int[numConditions][num];
			int root = tree.getLegalRoot(legality);
			downPass(tree, root);
			numPairs = maximum(max[noFree][root] ,max[free00][root] ,max[free01][root],max[free11][root],max[free10][root]);
			firstPairingInClade(tree.getLegalRoot(legality), tree);

			int count=1;
			int limit;
			while (count<(limit = ownerModule.limitCheckOK(count)) && nextPairingInClade(tree, tree.getLegalRoot(legality))) {
				count++;
				if (count % 10000 ==0) MesquiteMessage.println("Number of pairings so far: " + count);
				if (count % 1000 == 0) CommandRecord.tick("Counting pairings: " + count);
			}
			if (count > limit) 
				count = limit;


			getPairing(tree, cur);
			return count;
		}
		else
			return 0;

	}
	/*.................................................................................................................*/
	public int getCurrentPairingNumber(){
		return currentPairing;
	}
	/* ..................................................................................................................................... */
	/* At node with daughters left and right, finds the maximum number of pairs given the condition,
	and stores the profile of the basis combinations in the "best" object for later use.
	Under each condition, it needs to check all of the conditions at left and right that would yield the input condition.
	For instance, consider the first condition (condition == noFree):
		There are five ways to achieve no free path:  Either there are no free paths
		up left or right, or there are free paths on both sides, but they are such that they allow a new path
		to cross the root (hence the supplement of 1).  There are four ways that allow this -- free01 in left and free10 in right,
		and three others.
	 */
	private int calculateBestGiven(int condition, int left, int right, BestBasisCombos best) {
		best.initialize(max);

		if (condition == noFree) { //
			best.checkIfBest(noFree, noFree, left, right);
			best.checkIfBest(free01, free10, left, right, 1);
			best.checkIfBest(free10, free01, left, right, 1);
			best.checkIfBest(free00, free11, left, right, 1);
			best.checkIfBest(free11, free00, left, right, 1);
			return best.getBestValue();
		}
		else if (condition == free00) {
			best.checkIfBest(free00, noFree, left, right);
			best.checkIfBest(free00, free00, left, right);
			best.checkIfBest(free00, free01, left, right);
			best.checkIfBest(free00, free11, left, right);
			best.checkIfBest(free00, free10, left, right);
			best.checkIfBest(noFree, free00, left, right);
			best.checkIfBest(free01, free00, left, right);
			best.checkIfBest(free10, free00, left, right);
			best.checkIfBest(free11, free00, left, right);
			return best.getBestValue();
		}
		else if (condition == free01) {
			best.checkIfBest(free01, noFree, left, right);
			best.checkIfBest(free01, free00, left, right);
			best.checkIfBest(free01, free01, left, right);
			best.checkIfBest(free01, free11, left, right);
			best.checkIfBest(free01, free10, left, right);
			best.checkIfBest(noFree, free01, left, right);
			best.checkIfBest(free00, free01, left, right);
			best.checkIfBest(free10, free01, left, right);
			best.checkIfBest(free11, free01, left, right);
			return best.getBestValue();
		}
		else if (condition == free11) {
			best.checkIfBest(free11, noFree, left, right);
			best.checkIfBest(free11, free00, left, right);
			best.checkIfBest(free11, free01, left, right);
			best.checkIfBest(free11, free11, left, right);
			best.checkIfBest(free11, free10, left, right);
			best.checkIfBest(noFree, free11, left, right);
			best.checkIfBest(free01, free11, left, right);
			best.checkIfBest(free10, free11, left, right);
			best.checkIfBest(free00, free11, left, right);
			return best.getBestValue();
		}
		else if (condition == free10) {
			best.checkIfBest(free10, noFree, left, right);
			best.checkIfBest(free10, free00, left, right);
			best.checkIfBest(free10, free01, left, right);
			best.checkIfBest(free10, free11, left, right);
			best.checkIfBest(free10, free10, left, right);
			best.checkIfBest(noFree, free10, left, right);
			best.checkIfBest(free01, free10, left, right);
			best.checkIfBest(free00, free10, left, right);
			best.checkIfBest(free11, free10, left, right);
			return best.getBestValue();
		}
		return 0;
	}
	boolean warnedOne = false;
	static boolean staticwarnedOne = false;
	int[] legality;
	/*.................................................................................................................*/
	private void setLegality(int node, Tree tree) {
		if (tree.nodeIsTerminal(node)) {
			long stateA =((CategoricalDistribution)observedStatesA).getState(tree.taxonNumberOfNode(node)); //get observed states
			long stateB =((CategoricalDistribution)observedStatesB).getState(tree.taxonNumberOfNode(node));
			if (CategoricalState.isUnassigned(stateA) || CategoricalState.isUnassigned(stateB) ||  //either of the two characters is missing data
					CategoricalState.isInapplicable(stateA) || CategoricalState.isInapplicable(stateB) || //either of the two characters is inapplicable
					CategoricalState.cardinality(stateA)!=1 || CategoricalState.cardinality(stateB)!=1 ||//either of the two characters is polymorphic/uncertain
					(CategoricalState.maximum(stateA)>1))  //the independent variable is non-binary
				legality[node] = MesquiteTree.ILLEGAL;
			else
				legality[node] = MesquiteTree.LEGAL;
			if (legality[node] == MesquiteTree.ILLEGAL)
				warningMessage = "Some taxa excluded (had non-binary states in the independent variable, or missing data, or polymorphic states, or uncertain states).";

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
		if (observedStatesA== null || observedStatesB == null)
			return;
		setLegality(tree.getRoot(), tree);
	}
	/* ..................................................................................................................................... */
	/* This tree traversal from tips to root ("down") calculates the maximum numbers of pairs given various conditions at each node. */
	private void downPass(Tree tree, int node) {
		for (int daughter = tree.firstLegalDaughterOfNode(node, legality); tree.nodeExists(daughter); daughter = tree.nextLegalSisterOfNode(daughter, legality))
			downPass(tree, daughter);
		if (tree.nodeIsInternal(node)) {  //internal node
			int left = tree.firstLegalDaughterOfNode(node, legality);
			int right = tree.lastLegalDaughterOfNode(node, legality);
			for (int i=0; i<numConditions; i++) 
				max[i][node] = calculateBestGiven(i, left, right, bestCombos); //calculate maximum under each condition
		}
		else { //terminal node
			long stateA =((CategoricalDistribution)observedStatesA).getState(tree.taxonNumberOfNode(node)); //get observed states
			long stateB =((CategoricalDistribution)observedStatesB).getState(tree.taxonNumberOfNode(node));
			
			max[noFree][node] = -1;
			max[free00][node] = -1;
			max[free01][node] = -1;
			max[free11][node] = -1;
			max[free10][node]= -1;

//			for (int i=0; i<numConditions; i++) //initialize to -1 (so as to know that observed states don't match these conditions)
//			max[i][node] = -1;
			if (stateA == set0) {  //set according to observed states
				if (stateB == set0)
					max[free00][node] = 0;
				else if (stateB== set1) 
					max[free01][node] = 0;
			}
			else if (stateA== set1) {
				if (stateB== set0)
					max[free10][node] = 0;
				else if (stateB== set1) 
					max[free11][node] = 0;
			}
		}
	}

	/*.................................................................................................................*/
	/* takes the second tree traversal one more step up, creating new path and seeking the given states
	on left and right sides*/
	private void goUpWithNewPath(Tree tree, int node, int left, int right, int seekLeft, int seekRight) {
		if (currentPath[node]==null) 
			currentPath[node]=new TaxaPath();
		currentPath[node].setBase(node);
		currentPath[left]=currentPath[node];
		currentPath[right]=currentPath[node];
		conditionSought[left]=seekLeft;
		conditionSought[right]=seekRight;
		firstPairingInClade(left, tree);
		firstPairingInClade(right, tree);
	}
	/*.................................................................................................................*/
	/* takes the second tree traversal one more step up, without any path*/
	private void goUpWithoutPath(Tree tree, int left, int right, int seekLeft, int seekRight) {
		currentPath[left]=null;
		currentPath[right]=null;
		conditionSought[left]=seekLeft;
		conditionSought[right]=seekRight;
		firstPairingInClade(left, tree);
		firstPairingInClade(right, tree);
	}
	/*.................................................................................................................*/
	/* takes the second tree traversal one more step up, creating new path and seeking the given states
	on left and right sides*/
	private void goUpWithPath(Tree tree, int left, int right, TaxaPath pathLeft, TaxaPath pathRight, int statesToBeSought) {
		currentPath[left]=pathLeft;
		currentPath[right]=pathRight;
		if (pathLeft == null) {
			conditionSought[right]=statesToBeSought;
			conditionSought[left]=noCondition;
		}
		else {
			conditionSought[left]=statesToBeSought;
			conditionSought[right]=noCondition;
		}
		firstPairingInClade(left, tree);
		firstPairingInClade(right, tree);
	}

	/*.................................................................................................................*/
	/* The method firstPairingInClade traverses the tree from root to tips ("up") and chooses a pairing in the clade.  If more than one
	choice at a node allows a maximal pairing, the first choice is made and the fact that alternatives are available is noted
	so that the alternatives may be constructed later.  The array numChoices used to record how many choices there are and 
	the array currentChoice records what is the current choice.  
	The method firstPairingInClade is used along with nextPairingInClade to cycle through all maximal pairings.
	To yield the first pairing, one simply calls the downPass method, followed by firstPairingInClade beginning at the root:
			downPass(tree.getRoot(), tree);
			firstPairingInClade(tree.getRoot(), tree);
	To obtain subsequent pairings, one then calls
			nextPairingInClade(tree, tree.getRoot());
	until nextPairingInClade returns false, indicating that there are no more pairings   */
	private void firstPairingInClade(int node, Tree tree) {
		currentChoice[node]=0; //0th choice is first one
		numChoices[node]= 1; //initialize number of choices to 1 (may be more found later)
		if (tree.nodeIsTerminal(node)) { //terminal node
			if (currentPath[node]!=null)  //if path is being passed, attach it to the terminal taxon
				currentPath[node].setNode(tree, node);
		}
		else {  //internal node
			int left = tree.firstLegalDaughterOfNode(node, legality); //tree assumed dichotomous; get left and right daughter nodes
			int right = tree.lastLegalDaughterOfNode(node, legality);
			surveyOfBestConditions.scan(max, node); //scan the max array made on the downpass to find what conditions are best
			if (currentPath[node]==null) {  //no path coming from below; thus must see if time to invent new one based at node
				if (surveyOfBestConditions.getBestValue()!=0) {	//if clade uniform don't need to continue since no path being carried up
					//first, calculate how many choices are at node under the conditions
					numChoices[node]= 0;
					if (conditionSought[node] != noCondition) { //(*) conditon imposed; calculate best you can do and find out how many choices are available in the basis
						calculateBestGiven(conditionSought[node], left, right, bestCombos);  
						numChoices[node]+= bestCombos.getNumBest();		
					}
					else { //no condition imposed, thus find the unconditionally best situations and add up how many choices total
						for (int condition = 0; condition<numConditions; condition++) {
							if (surveyOfBestConditions.isBest(condition)) { 
								calculateBestGiven(condition, left, right, bestCombos);  
								numChoices[node]+= bestCombos.getNumBest();		
							}
						}
					}
					// now go to first choice (number 0) by setting current to -1; in nextChoiceAtNode will increment to 0
					currentChoice[node]= -1;
					nextChoiceAtNode(node, tree);
				}
			}
			else {  //path is being passed from below; need only to choose which side to continue with path
				// Need to find best arrangements with free path of states sought, and pass along side with appropriate free path

				//first, calculate how many choice are at node under the conditions
				calculateBestGiven(conditionSought[node], left, right, bestCombos);  // in bestCombos will now be stored combos that gave maxima
				int goLeft = 0;
				int goRight = 0;
				for (int combo = 0; combo<bestCombos.getNumBest(); combo++) {
					if (bestCombos.getBestLeft(combo) == conditionSought[node]) //condition sought by path is compatible with left
						goLeft = 1;
					if (bestCombos.getBestRight(combo) == conditionSought[node]) //condition sought by path is compatible with right
						goRight = 1;
				}
				numChoices[node] = goLeft + goRight; //1 will have been placed if left or right is good
				if (goLeft ==1) //if left is possible, go there first (if more than one choice, right will be visited later)
					goUpWithPath(tree, left, right, currentPath[node], null, conditionSought[node]);
				else
					goUpWithPath(tree, left, right, null, currentPath[node], conditionSought[node]); //otherwise go up right
			}
		}
	}

	/*.................................................................................................................*/
	/* This method makes the next choice at node, and continues up tree with first pairing given that choice*/
	private   void nextChoiceAtNode(int node, Tree tree) {  //this method needs to cover only those cases in which multiple choices could arise
		currentChoice[node]++; //going to next choice
		if (tree.nodeIsInternal(node) && currentChoice[node]<numChoices[node]) {  //terminal nodes never with choice; make sure haven't run out of choices
			int left = tree.firstLegalDaughterOfNode(node, legality);
			int right = tree.lastLegalDaughterOfNode(node, legality);
			if (currentPath[node]==null || currentPath[node].getBase()==node) {  //no path or path based at this node
				currentPath[node] = null; //initialize path to null (may recreate new path)
				/* first, have to find which conditon and left-right basis combination corresponds to current choice.  Pretty inefficient, this.*/
				int whichCondition;
				int whichCombo;
				if (conditionSought[node] != noCondition) {
					whichCondition = conditionSought[node];  //a particular condition is being sought, thus constrained to one set of choices (see (*) in firstPairingInClade)
					whichCombo = currentChoice[node];
				}
				else {  /*no condition sought; thus have to cycle through the good conditions.  For instance, if we are seeking currentChoice number 8,
					and condition 0 supplies 3 choices, condition 1 supplies 1 choice, and condition 2 supplies 2 choices, and conditon 3 supplies 4 choices,
					then the 8th choice overall correspondes to the second choice of condition 3 (whichCondition = 3, whichCombo = 1 since first combo is numbered 0)*/
					whichCondition = -5;
					whichCombo = 0;
					int choices = 0;
					surveyOfBestConditions.scan(max, node);
					for (int condition = 0; condition<numConditions && whichCondition<0; condition++) {
						if (surveyOfBestConditions.isBest(condition)) { //if free-path condition at node is one of the ones that allows maximum number of pairs
							calculateBestGiven(condition, left, right, bestCombos);  //calculate best combos at left and right that give such a condition
							int previousChoices = choices;
							choices+= bestCombos.getNumBest();   // how many combinations give the condition best?; keep running total of number of choices
							if (choices>currentChoice[node]) {  //choicecount has surpassed current choice, thus, have found condition and combo to use for current choice
								whichCondition = condition;
								whichCombo = currentChoice[node]-previousChoices;
							}
						}	
					}
				}

				calculateBestGiven(whichCondition, left, right, bestCombos);  // in bestCombos will now be stored combos that gave maxima
				int bestLeft = bestCombos.getBestLeft(whichCombo);  
				int bestRight = bestCombos.getBestRight(whichCombo);

				// now have found the best combination of left and right conditions corresponding to this choice.
				if (whichCondition != noFree) // there is a free path, hence can't make new path since that would cover the root of the clade and close the free path
					goUpWithoutPath(tree, left, right, bestLeft, bestRight);
				else if (bestLeft == noFree && bestRight == noFree) { //no free paths up either side, therefore continue without path
					goUpWithoutPath(tree, left, right, bestLeft, bestRight);
				}
				else // there is no free path, and it's not because no free paths on either side.  Thus must be because path crosses root of clade.  Make it.
					goUpWithNewPath(tree, node, left, right, bestLeft, bestRight);
			}
			else {  //path is not null; need only to make next choice as to which side to continue
				goUpWithPath(tree, left, right, null, currentPath[node], conditionSought[node]);
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
			int left = tree.firstLegalDaughterOfNode(node, legality);
			int right = tree.lastLegalDaughterOfNode(node, legality);
			if (!nextPairingInClade(tree, left)){  //try going to next pairing on left; if false then had already reached last choice on left clade
				if (!nextPairingInClade(tree, right)){ //try going to next pairing on right; if false then had already reached last choice on right clade
					// left and right choices exhausted, thus go to next choice at node if there is one
					if (currentChoice[node]+1 >=numChoices[node])  //at maximum choice; thus return false to indicate choices exhausted
						moreChoices = false;
					else  // still other choices available at this node; do next one
						nextChoiceAtNode(node, tree);
				}
				else {   // if right wasn't at last choice it would have been incremented within the nextPairingInClade(right) call, thus reset left back to first choice
					if (currentPath[left]!=null && currentPath[left].getBase() == left)   // need to delete path based at left so it won't think it's being passed up from below
						currentPath[left] = null;
					firstPairingInClade(left, tree);
				}
			}//else had successfully incremented left side to next choice
		}
		else //terminal node with one choice only
			moreChoices = false;
		return moreChoices;
	}
	/*.................................................................................................................*/
	/* harvest all the paths and put them in the pairing*/
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





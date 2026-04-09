/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.tree;

import java.awt.Checkbox;
import java.awt.Label;

import mesquite.assoc.lib.TaxaAssociation;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.Associable;
import mesquite.lib.Bits;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleArray;
import mesquite.lib.IntegerArray;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NameReference;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxonNamer;
import mesquite.lib.ui.ExtensibleDialog;

public class TreeUtil {

	public static String translationTableFileName = "taxonNamesTranslationTable.txt";


	/*.................................................................................................................*/
	/** Returns true iff the two trees have the same topologies.  The two trees must use the same block of taxa */
	public static boolean identicalTopologies (Tree tree1, Tree tree2, boolean checkBranchLengths) {
		if (tree1==null || tree2==null)
			return false;
		if (tree1.getTaxa().equals(tree2.getTaxa())) {
			return tree1.equalsTopology(tree2, checkBranchLengths);
		}
		return false;
	}
	/*.................................................................................................................*/
	/** Returns true iff the contained taxa in all containing taxa are monophyletic . */
	public static boolean containedAllMonophyletic (Tree containedTree, Tree containingTree, TaxaAssociation association) {
		if (containedTree==null || containingTree==null || association == null)
			return false;
		Taxa containingTaxa = containingTree.getTaxa();

		for (int outer=0;  outer<containingTaxa.getNumTaxa(); outer++) {  
			Bits associates = association.getAssociatesAsBits(containingTaxa, outer);
			if (!containedTree.isClade(associates))
				return false;
		}
		return true;
	}
	static int[] getDaughters(MesquiteTree tree, int node){
		int num = tree.numberOfDaughtersOfNode(node);
		int[] daughters = new int[num];
		int count = 0;
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) 
			daughters[count++] = daughter;
		return daughters;
	}
	static int matchScore( int[] targetTaxaSequence, int[] clade){
		int minScore = MesquiteInteger.unassigned;
		int[]longer, shorter;
		if (targetTaxaSequence.length>clade.length){
			longer = targetTaxaSequence;
			shorter = clade;
		}
		else {
			longer = clade;
			shorter = targetTaxaSequence;
		}
		int diff = longer.length - shorter.length;
		for (int offset = 0; offset<=diff; offset++){
			int thisOffsetScore = 0;
			for (int i= 0; i<shorter.length; i++){
				int whereInTarget = IntegerArray.indexOf(longer, shorter[i]);
				thisOffsetScore += Math.abs(whereInTarget- (offset+i));
			}
			minScore = MesquiteInteger.minimum(minScore, thisOffsetScore); 
		}
		return minScore;
	}
	static boolean rotateIfUseful(MesquiteTree tree, int node, int[] targetTaxaSequence, boolean acceptEqual){
		boolean rotated = false;
		if (tree.nodeIsInternal(node)) { 
			int[] daughters = getDaughters(tree, node);
			int dX = 0; int dY = 0;
			int currentScore = matchScore(targetTaxaSequence, tree.getTerminalTaxa(node));
			int min = currentScore;
			boolean done = false;
			while (!done){
				//try all flips
				for (int d1 = 0; d1<daughters.length; d1++)
					for (int d2 = d1+1; d2<daughters.length; d2++){
						tree.interchangeBranches(daughters[d1], daughters[d2], true, false);
						int newScore = matchScore(targetTaxaSequence, tree.getTerminalTaxa(node) );
						if (newScore<min || (acceptEqual && newScore == min)){
							min = newScore;
							dX = daughters[d1];
							dY= daughters[d2];
						}
						tree.interchangeBranches(daughters[d1], daughters[d2], true, false); //undo previous
					}
				if (min<currentScore){ //better found remember as current score and do the interchange
					currentScore = min;
					tree.interchangeBranches(dX, dY, true, false);
					rotated = true;
				}
				else
					done = true;
			}

			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				boolean drotated = rotateIfUseful(tree, daughter, targetTaxaSequence, acceptEqual);
				rotated = rotated || drotated;
			}
		}
		return rotated;
	}

	//rotates one tree to match other as well as possible. Returns improvement in score (if 0, no rotation done)
	public static boolean rotateToMatch(MesquiteTree toBeRotated, Tree targetTree, boolean acceptEqual){
		int[] targetTerminals = targetTree.getTerminalTaxa(targetTree.getRoot());
		int count = 0;
		int originalScore = matchScore(targetTerminals, toBeRotated.getTerminalTaxa(toBeRotated.getRoot()));
		int oldScore = originalScore;
		boolean done = false;
		boolean rotated = false;
		while (!done){
			boolean trotated = rotateIfUseful((MesquiteTree)toBeRotated, toBeRotated.getRoot(), targetTerminals, acceptEqual);
			rotated = rotated || trotated;
			int newScore = matchScore(targetTerminals, toBeRotated.getTerminalTaxa(toBeRotated.getRoot()));
			if (newScore<oldScore || (acceptEqual && newScore == oldScore))
				oldScore = newScore;
			else
				done = true;
			count++;
			if (count>1000){
				done = true;
			}
		}
		return rotated;
	}
	

	/*======================================================*/
	static int stateHasntBranch = 1<<0;
	static int stateHasBranch = 1<<1;
	static int stateHasBoth = 1<<2;
	//passed the to be rerooted tree

	static void setBranchStates(Tree tree, int node, int[][] branchStates, int[][] taxonStates){
		int root = tree.getRoot() ;
		if (node != root){
			//This node inherits all of the states of its ancestor
			int motherOfNode = tree.motherOfNode(node);
			for (int k = 0; k< branchStates[node].length; k++)
				branchStates[node][k] = branchStates[motherOfNode][k];
			//and it adds a state of its own, unless it is the first daughter of the root
			if (motherOfNode != root || tree.firstDaughterOfNode(root) != node)
				branchStates[node][node] = stateHasBranch;
		}
		if (tree.nodeIsTerminal(node)) {
			int taxon = tree.taxonNumberOfNode(node);
			for (int k = 0; k< taxonStates[taxon].length; k++)
				branchStates[taxon][k] = branchStates[node][k];
		}
		else {
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				setBranchStates(tree, daughter, branchStates, taxonStates);
			}
		}
	}
	//passed the target tree
	static void inferAncestralBranchStatesDownpass(Tree tree, int node, int[][] branchStates, int[][] taxonStates, int[] tempSingleDouble){
		if (tree.nodeIsTerminal(node)){
			int taxon = tree.taxonNumberOfNode(node);
			for (int k = 0; k< branchStates[node].length; k++) // on the way down, zap all internals to leave states only at tips
				branchStates[node][k] = taxonStates[taxon][k];
		}
		else  { 
			for (int k = 0; k< branchStates[node].length; k++)
				tempSingleDouble[k] = 0;

			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				inferAncestralBranchStatesDownpass(tree, daughter, branchStates, taxonStates, tempSingleDouble);
				for (int k = 0; k< branchStates[daughter].length; k++)
					tempSingleDouble[k] = tempSingleDouble[k] | branchStates[daughter][k];  //accumulating all statesets
			}
			for (int k = 0; k< branchStates[node].length; k++) {
				int statesSeen = tempSingleDouble[k];
				if ((statesSeen & stateHasBranch) != 0 && (statesSeen & stateHasntBranch) != 0) //both seen; return both
					branchStates[node][k] = stateHasBoth;
				else if ((statesSeen & stateHasBranch) != 0) //hasbranch included; resolves to that
					branchStates[node][k] = stateHasBranch;
				else if ((statesSeen & stateHasntBranch) != 0) //hasntbranch included; resolves to that
					branchStates[node][k] = stateHasntBranch;
				else if ((statesSeen & stateHasBoth) != 0) //has both returned
					branchStates[node][k] = stateHasBoth;
			}

		}
	}

	/*.............................................................................................................*/
	static void inferAncestralBranchStatesUppass(Tree tree, int node, int[][] branchStatesDown, int[][] branchStatesUp, int[] tempSingleDouble){
		if (tree.nodeIsInternal(node)){
			for (int k = 0; k< branchStatesUp[node].length; k++)
				tempSingleDouble[k] = 0;

			// accumulate downstates from sisters
			for (int daughter = tree.firstDaughterOfNode(tree.motherOfNode(node)); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				if (daughter != node){
					for (int k = 0; k< branchStatesDown[daughter].length; k++)
						tempSingleDouble[k] = tempSingleDouble[k] | branchStatesDown[daughter][k];  //accumulating all statesets
				}
			}
			//get upstate from ancestor
			if (tree.motherOfNode(node)!=tree.getRoot()) {
				for (int k = 0; k< branchStatesUp[node].length; k++)
					tempSingleDouble[k] = tempSingleDouble[k] | branchStatesUp[tree.motherOfNode(node)][k];  //accumulating all statesets
			}
			else {
				for (int k = 0; k< branchStatesDown[node].length; k++) {
					if (tree.motherOfNode(k) == tree.getRoot())
						tempSingleDouble[k] = tempSingleDouble[k] | stateHasntBranch;  //for the root daughters, treats as if there is an outgroup, so as not to reroot if unneeded
				}
			}

			for (int k = 0; k< branchStatesUp[node].length; k++) {
				int statesSeen = tempSingleDouble[k];
				if ((statesSeen & stateHasBranch) != 0 && (statesSeen & stateHasntBranch) != 0) //both seen; return both
					branchStatesUp[node][k] = stateHasBoth;
				else if ((statesSeen & stateHasBranch) != 0) //hasbranch included; resolves to that
					branchStatesUp[node][k] = stateHasBranch;
				else if ((statesSeen & stateHasntBranch) != 0) //hasntbranch included; resolves to that
					branchStatesUp[node][k] = stateHasntBranch;
				else if ((statesSeen & stateHasBoth) != 0) //has both returned
					branchStatesUp[node][k] = stateHasBoth;
			}

			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				inferAncestralBranchStatesUppass(tree, d, branchStatesDown, branchStatesUp, tempSingleDouble);
		}
	}

	//final pass: if node is polytomous, is intersection /uinion of up anad downpass at node
	static void inferAncestralBranchStatesFinalpass(Tree tree, int node, int[][] branchStatesDown, int[][] branchStatesUp, int[][] branchStatesFinal, int[] tempSingleDouble){
		if (tree.nodeIsTerminal(node)){
			for (int k = 0; k< branchStatesFinal[node].length; k++)
				branchStatesFinal[node][k] = branchStatesDown[node][k];
		}
		else {
			for (int k = 0; k< branchStatesUp[node].length; k++)
				tempSingleDouble[k] = 0;

			// accumulate downstates from sisters
			for (int daughter = tree.firstDaughterOfNode(tree.motherOfNode(node)); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				for (int k = 0; k< branchStatesDown[daughter].length; k++)
					tempSingleDouble[k] = tempSingleDouble[k] | branchStatesDown[daughter][k];  //accumulating all statesets
			}
			//get upstate from node
			for (int k = 0; k< branchStatesDown[node].length; k++)
				tempSingleDouble[k] = tempSingleDouble[k] | branchStatesUp[node][k];  //accumulating all statesets

			for (int k = 0; k< branchStatesUp[node].length; k++) {
				int statesSeen = tempSingleDouble[k];
				if (node == tree.getRoot() && tree.motherOfNode(k) == tree.getRoot())
					branchStatesFinal[node][k] =stateHasntBranch;

				else if ((statesSeen & stateHasBranch) != 0 && (statesSeen & stateHasntBranch) != 0) //both seen; return both
					branchStatesFinal[node][k] = stateHasBoth;
				else if ((statesSeen & stateHasBranch) != 0) //hasbranch included; resolves to that
					branchStatesFinal[node][k] = stateHasBranch;
				else if ((statesSeen & stateHasntBranch) != 0) //hasntbranch included; resolves to that
					branchStatesFinal[node][k] = stateHasntBranch;
				else if ((statesSeen & stateHasBoth) != 0) //has both returned
					branchStatesFinal[node][k] = stateHasBoth;
			}
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				inferAncestralBranchStatesFinalpass(tree, d, branchStatesDown, branchStatesUp, branchStatesFinal, tempSingleDouble);

		}
	}

	static void findBestMatches(Tree tree, int node, int[][] branchStates, int[] targetAncSt, int[] ancStMatch){
		int countMatches = 0;
		for (int k = 0; k< branchStates[node].length; k++) {
			if (branchStates[node][k] == targetAncSt[k])
				countMatches++;
		}
		ancStMatch[node] = countMatches;

		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
			findBestMatches(tree, daughter, branchStates, targetAncSt, ancStMatch);
		}

	}

	/*reroots one tree to match other as well as possible. Returns whether a reroot was done. 
	This is a quick heuristic for visualization purposes */
	public static boolean rerootToMatch(MesquiteTree toBeRerooted, Tree targetTree, boolean acceptEqual, boolean notify){
		//get bipartition characters from toBeRerooted
		int numTaxa = toBeRerooted.getTaxa().getNumTaxa();
		int numNodeSpaces = MesquiteInteger.maximum(toBeRerooted.getNumNodeSpaces(), targetTree.getNumNodeSpaces());
		int[][] branchStatesTBR = new int[numNodeSpaces][numNodeSpaces];
		int[][] taxonStates = new int[numTaxa][numNodeSpaces];
		setBranchStates(toBeRerooted, toBeRerooted.getRoot(), branchStatesTBR, taxonStates);
		//map bipartition characters on target to find parsimony ancestral states
		int[] tempStatesDouble = new int[numNodeSpaces];
		int[][] branchStatesDown = new int[numNodeSpaces][numNodeSpaces];
		inferAncestralBranchStatesDownpass(targetTree, targetTree.getRoot(), branchStatesDown, taxonStates, tempStatesDouble);
		int[][] branchStatesUp = new int[numNodeSpaces][numNodeSpaces];
		inferAncestralBranchStatesUppass(targetTree, targetTree.getRoot(), branchStatesDown, branchStatesUp, tempStatesDouble);
		int[][] branchStatesFinal = new int[numNodeSpaces][numNodeSpaces];
		inferAncestralBranchStatesFinalpass(targetTree, targetTree.getRoot(), branchStatesDown, branchStatesUp, branchStatesFinal, tempStatesDouble);

		//now find branch in toBeRerooted whose bipartition states matches reconstructed ancestral as closely as possible
		int[] targetAncestralStates = branchStatesFinal[targetTree.getRoot()];
		int[] ancestralStateMatch = new int[numNodeSpaces];
		findBestMatches(toBeRerooted, toBeRerooted.getRoot(), branchStatesTBR, targetAncestralStates, ancestralStateMatch);

		int max = 0;
		int best = -1;
		for (int k = 0; k<ancestralStateMatch.length; k++){  //should look for multiple matches and go toward middle-ish one!
			if (ancestralStateMatch[k]>max){ 
				max =ancestralStateMatch[k];
				best = k;
			}
		}
		if (best != toBeRerooted.getRoot() || max > ancestralStateMatch[toBeRerooted.getRoot()])
			toBeRerooted.reroot(best, toBeRerooted.getRoot(), notify);  
		return best != toBeRerooted.getRoot();
	}
	/*
	 *  void furthestPoly(Tree tree, int node, MesquiteInteger d){
		if (tree.nodeIsTerminal(node)) { 
			TaxonPolygon poly = treeDisplay.getTreeDrawing().namePolys[tree.taxonNumberOfNode(node)];
			Rectangle rect = poly.getBounds();
			int edge = 0;
			if (treeDisplay.isUp()) {
				edge = rect.y;
				d.setValue(MesquiteInteger.minimum(d.getValue(), edge));
			}
			else if (treeDisplay.isDown()) {
				edge = rect.y + rect.height;
				d.setValue(MesquiteInteger.maximum(d.getValue(), edge));
			}
			else if (treeDisplay.isRight()) {
				edge = rect.x + rect.width;
				d.setValue(MesquiteInteger.maximum(d.getValue(), edge));
			}
			else if (treeDisplay.isLeft()) {
				edge = rect.x;
				d.setValue(MesquiteInteger.minimum(d.getValue(), edge));
			}
			}

			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				furthestPoly(tree, daughter, d);
			}
	}*/
	/*.................................................................................................................*/
	/*Old version 3.81 that couldn't read multiline trees*/
	/*public static TreeVector readNewickTreeFile (MesquiteFile file, String line, Taxa taxa, boolean permitTaxaBlockEnlarge, TaxonNamer namer, String arguments, String treeNameBase) {
		Parser parser = new Parser(arguments);
		String dialect = parser.getFileReadingArgumentSubtype(arguments, "newickDialect");
		if (dialect != null)
			MesquiteMessage.println("Trees read assuming Newick dialect: " + dialect);
		Parser treeParser = new Parser();
		treeParser.setQuoteCharacter((char)0);
		int numTrees = MesquiteInteger.infinite;
		MesquiteInteger stringLoc = new MesquiteInteger(0);
		if (line != null){
			treeParser.setString(line); 
			String token = treeParser.getNextToken();  // numTaxa
			numTrees = MesquiteInteger.fromString(token);
			stringLoc.setValue(line.length());
		}
		int iTree = 0;
		TreeVector trees = null;
		boolean abort = false;

		line =file.getFileContentsAsString(2^30);
		treeParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
		treeParser.setPosition(stringLoc.getValue());
		while (stringLoc.getValue()+1<line.length() && !abort && (iTree<numTrees)) {

			if (trees == null) {
				trees = new TreeVector(taxa);
				trees.setName("Imported trees");
			}
			MesquiteTree t = new MesquiteTree(taxa);
			t.setPermitTaxaBlockEnlargement(permitTaxaBlockEnlarge);
			if (StringUtil.notEmpty(dialect))
				t.setDialect(dialect);
			//t.setTreeVector(treeVector);
			t.readTree(line,stringLoc, namer, null, "():;,[]\'<>", true);  //tree reading adjusted to use Newick punctuation rather than NEXUS, except adding <>, so that associated will be read

			t.setName(treeNameBase + (iTree+1));
			trees.addElement(t, false);


			iTree++;
		//	line = file.readNextDarkLine();		
		//	if (file.getFileAborted())
		//		abort = true;
		}
		return trees;

	}
		/*.................................................................................................................*/
	//reading arguments can include dialect hints, e.g. @newickDialect.MrBayes, @newickDialect.ASTRAL, @newickDialect.DELINEATE, etc.
	public static TreeVector readNewickTreeFile (MesquiteFile file, String line, Taxa taxa, boolean permitTaxaBlockEnlarge, TaxonNamer namer, String arguments, String treeNameBase) {
		return readNewickTreeFile(file, line, taxa, permitTaxaBlockEnlarge, false, namer,arguments, treeNameBase);
	}

	/*.................................................................................................................*/
	//reading arguments can include dialect hints, e.g. @newickDialect.MrBayes, @newickDialect.ASTRAL, @newickDialect.DELINEATE, etc.
	public static TreeVector readNewickTreeFile (MesquiteFile file, String line, Taxa taxa, boolean permitTaxaBlockEnlarge, boolean enableT0Names, TaxonNamer namer, String arguments, String treeNameBase) {
		return readNewickTreeFile(file, line, taxa, permitTaxaBlockEnlarge, enableT0Names, namer,arguments, treeNameBase, true);
	}
	/*.................................................................................................................*/
	//reading arguments can include dialect hints, e.g. @newickDialect.MrBayes, @newickDialect.ASTRAL, @newickDialect.DELINEATE, etc.
	public static TreeVector readNewickTreeFile (MesquiteFile file, String line, Taxa taxa, boolean permitTaxaBlockEnlarge, boolean enableT0Names, TaxonNamer namer, String arguments, String treeNameBase, boolean logIfDialect) {
		TreeVector trees = null;

		Parser parser = new Parser(arguments);
		String dialect = parser.getFileReadingArgumentSubtype(arguments, "newickDialect");
		if (dialect == null)
			dialect = "Default";

		if (dialect != null && logIfDialect)
			MesquiteMessage.println("Trees read assuming Newick dialect: " + dialect);
		Parser treeParser = new Parser();
		treeParser.setQuoteCharacter((char)0);
		int numTrees = MesquiteInteger.infinite;
		MesquiteInteger stringLoc = new MesquiteInteger(0);

		/*
		if (line != null){
			treeParser.setString(line); 
			String token = treeParser.getNextToken();  // numTrees
			numTrees = MesquiteInteger.fromString(token);
		}
		 */
		int iTree = 0;
		boolean abort = false;

		line =file.getFileContentsAsString(1000000000);
		if (line == null)
			return null;

		treeParser.setString(line); 
		String token = treeParser.getNextToken();  // numTaxa
		int possibleNumTrees = MesquiteInteger.fromString(token);
		if (MesquiteInteger.isCombinable(possibleNumTrees)){ //we did read numTrees!
			stringLoc.setValue(treeParser.getPosition());
			numTrees = possibleNumTrees;
			token = treeParser.getNextToken();  // is there another number?
			int maybe = MesquiteInteger.fromString(token);
			if (MesquiteInteger.isCombinable(maybe)){ //we did read some other number. Trees must come after this!
				stringLoc.setValue(treeParser.getPosition());
			}
		}
		boolean done = false;
		boolean logProgress =  (line.length()>10000);
		if (logProgress)
			MesquiteMessage.print("Trees read: ");
		while (!done && stringLoc.getValue()+1<line.length() && !abort && (iTree<numTrees)) {

			if (trees == null) {
				trees = new TreeVector(taxa);
				trees.setName("Imported trees");
			}
			MesquiteTree t = new MesquiteTree(taxa);
			t.setPermitTaxaBlockEnlargement(permitTaxaBlockEnlarge);
			if (enableT0Names)
				t.setPermitT0Names(true);
			if (StringUtil.notEmpty(dialect))
				t.setDialect(dialect);
			long oldLoc = stringLoc.getValue();
			/*
			 Parser debugParser = new Parser(line);
			debugParser.setPosition(oldLoc);
			String c = debugParser.getNextToken();
			stringLoc.setValue(oldLoc);
			 */
			t.readTree(line,stringLoc, namer, StringUtil.defaultWhitespace + "\n\r", "():;,[]\'<>", true);  //tree reading adjusted to use Newick punctuation rather than NEXUS, except adding <>, so that associated will be read
			if (logProgress){
				if ((iTree+1) % 1000 == 0)
					MesquiteMessage.print(" " + (iTree + 1));
				else if ((iTree+1) % 100 == 0)
					MesquiteMessage.print(" .");
				
			}
			if (oldLoc < stringLoc.getValue()){
				t.setName(treeNameBase + (iTree+1));
				trees.addElement(t, false);
				iTree++;
			}
			else
				done = true;
		}
		if (logProgress)
			MesquiteMessage.println("");


		return trees;
	}
	/*.................................................................................................................*
	public static Tree readPhylipTree (String line, Taxa taxa, boolean permitTaxaBlockEnlarge, boolean permitSpaceUnderscoreEquivalent, TaxonNamer namer) {
		if (StringUtil.blank(line))
			return null;
		if (!validNewickEnds(line))
			return null;
		MesquiteTree t = new MesquiteTree(taxa);
		t.setPermitTaxaBlockEnlargement(permitTaxaBlockEnlarge);
		t.setPermitSpaceUnderscoreEquivalent(permitSpaceUnderscoreEquivalent);
		t.readTree(line, namer, null, "():;,[]\'"); //tree reading adjusted to use Newick punctuation rather than NEXUS
		return t;
	}

	/*.................................................................................................................*/
	public static void reinterpretNodeLabels(Tree tree, int node, NameReference[] nameRefs, boolean asText, double divisor){
		if (nameRefs==null || nameRefs.length == 0)
			return;
		MesquiteTree t = null;
		if (tree instanceof MesquiteTree)
			t=(MesquiteTree)tree;
		if (t==null)
			return;
		if (divisor==0.0) divisor=1.0;
		if (t.nodeIsInternal(node)){
			if (t.nodeHasLabel(node)){
				String label = t.getNodeLabel(node);
				if (asText) {
					t.setAssociatedString(nameRefs[0], node, label);
				} else {
					Parser parser = new Parser(label);
					parser.setWhitespaceString(parser.getWhitespaceString()+"/");
					double[] support = new double[nameRefs.length];
					for (int i=0; i<nameRefs.length; i++) {
						support[i] = MesquiteDouble.fromString(parser.getNextToken());
						if (MesquiteDouble.isCombinable(support[i]))
							t.setAssociatedDouble(nameRefs[i], node, support[i]/divisor);
					}
				}
				t.setNodeLabel(null, node);
			}
			for (int daughter = t.firstDaughterOfNode(node); t.nodeExists(daughter); daughter = t.nextSisterOfNode(daughter)) {
				reinterpretNodeLabels(t, daughter, nameRefs, asText, divisor);
			}
		}

	}
	/*.................................................................................................................*/
	//THIS IS NOT CALLED
	public  static boolean showAssociatedChoiceDialog(Associable tree, ListableVector names, String message, MesquiteModule module) {
		if (tree == null)
			return false;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedDoubles();
		boolean[] shown = new boolean[num + names.size()]; //bigger than needed probably
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (da != null){
				v.addElement(new MesquiteString(da.getName(), ""), false);
				if (names.indexOfByName(da.getName())>=0)
					shown[i] = true;
			}
		}
		for (int i = 0; i<names.size(); i++){
			String name = ((MesquiteString)names.elementAt(i)).getName();
			if (v.indexOfByName(name)<0){
				v.addElement(new MesquiteString(name, " (not in current tree)"), false);
				if (v.size()-1>= shown.length)
					shown[v.size()-1] = true;
			}
		}
		if (v.size()==0)
			module.alert("This Tree has no values associated with nodes");
		else {
			ExtensibleDialog queryDialog = new ExtensibleDialog(module.containerOfModule(), message,  buttonPressed);
			queryDialog.addLabel(message, Label.CENTER);
			Checkbox[] checks = new Checkbox[v.size()];
			for (int i=0; i<v.size(); i++){
				MesquiteString ms = (MesquiteString)v.elementAt(i);
				checks[i] = queryDialog.addCheckBox (ms.getName() + ms.getValue(), shown[i]);
			}

			queryDialog.completeAndShowDialog(true);

			boolean ok = (queryDialog.query()==0);

			if (ok) {
				names.removeAllElements(false);
				for (int i=0; i<checks.length; i++){
					MesquiteString ms = (MesquiteString)v.elementAt(i);
					if (checks[i].getState())
						names.addElement(new MesquiteString(ms.getName(), ms.getName()), false);
				}
			}

			queryDialog.dispose();
			return ok;

		}
		return false;
	}


}

/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.Tree;

/*June 28 2012.  There are some methods which encounter problems when a containing tree does not 
 * contain all nodes (internal or terminal) of the contained tree.  For example, if a containing tree
 * does not contain all containing taxa in the given association, and there are taxa in the contained
 * tree that are associated with those missing containing taxa, methods such as deepestNodeThatContainedEnters
 * will end up in an infinite loop.  A conditional has been added to this method to return 0 for any of these
 * containing nodes (by asking if locationOfContainedNode == 0).  Other similar methods (shallowestNode...) should
 * also be investigated for this effect.
 * 
 * In the current version (as of October 08), some internal nodes of the contained tree that are expected to be contained 
 * are not.  Specifically, if a node of the contained tree has:
 *   (1) An ancestor node in the containing node j
 *      and
 *   (2) A descendant node in the containing node j
 * it is not contained by any containing nodes in the containing tree.
 * 
 * In other words, this contained node (or, in this case it's easier to think of as a branch)
 * neither enters the contained node, nor exits the contained node, and is thus not considered 'contained'
 * This can be seen by calling the toString method.*/
/*-------------------------------------*/
/* For each node of a tree for one taxa, what are the nodes of contained tree are present and at one point in branch*/
public class AssociationHistory {
	/*in element i is stored an array containing a list of the branches in the containedTree (designated by their node numbers) that enter node i
	 * of the containing tree.  "Enter" is here meant in a time-backwards sense, i.e., at the most recent tip of the node.   */
	int[][] enteringContainedBranches;
	/*in element i is stored the depth along the branch of the containing tree in which the contained node i is placed*/
	//double[][] containedDepths;
	/*in element i is stored the node number in the containing tree in which the contained node itself is present */
	int[] locationOfContainedNode;
	Tree containingTree, containedTree;
	MesquiteTimer aTime, sTime;

	public AssociationHistory( ){
		aTime = new MesquiteTimer();
		sTime = new MesquiteTimer();
	}

	public AssociationHistory(Tree containing, Tree contained, int[] locationOfContainedNode ){
		aTime = new MesquiteTimer();
		sTime = new MesquiteTimer();
		setTrees(containing,  contained);
		if (locationOfContainedNode!=null && this.locationOfContainedNode.length==locationOfContainedNode.length)
			for (int j=0; j<locationOfContainedNode.length; j++)
				this.locationOfContainedNode[j]=locationOfContainedNode[j];
		calcEnteringContainedBranchesFromLocations();
	}

	public Tree getContainingTree() {
		return containingTree;
	}
	public Tree getContainedTree() {
		return containedTree;
	}
	public void resetArrays(){
		if (containingTree!=null) {
			if (enteringContainedBranches==null || enteringContainedBranches.length<containingTree.getNumNodeSpaces())
				enteringContainedBranches = new int[containingTree.getNumNodeSpaces()][];
			if (containedTree!=null){
				int numContained = containedTree.getNumNodeSpaces();
				if (locationOfContainedNode==null || locationOfContainedNode.length<numContained)
					locationOfContainedNode = new int[numContained];
				if (enteringContainedBranches[0]==null || enteringContainedBranches[0].length<numContained) {
					for (int i=0; i<enteringContainedBranches.length; i++) 
						enteringContainedBranches[i]= new int[numContained];
				}
				for (int i=0; i<enteringContainedBranches.length; i++) 
					for (int j=0; j<numContained; j++)
						enteringContainedBranches[i][j]=MesquiteInteger.unassigned;
			}
		}
	}
	public void setContainingTree(Tree tree){
		containingTree = tree;
		resetArrays();
	}
	public void setContainedTree(Tree tree){
		//check if TAXA correct
		containedTree = tree;
		//should it register as listener to tree?
		resetArrays();
	}
	public void setTrees(Tree containing, Tree contained){
		//check if TAXA correct
		containingTree = containing;
		containedTree = contained;
		resetArrays();
	}
	private boolean illegalContained(int contained){
		return !(contained>=0 && (enteringContainedBranches.length>0 && contained<enteringContainedBranches[0].length));
	}
	private boolean illegalContaining(int containing){
		return !(containing>=0 && containing< enteringContainedBranches.length);
	}
	public void setContainedNode(int containing, int contained){
		if (illegalContained(contained) || illegalContaining(containing))
			return;
		for (int i=0; i<enteringContainedBranches[containing].length; i++)
			if (enteringContainedBranches[containing][i] ==MesquiteInteger.unassigned) {
				enteringContainedBranches[containing][i] = contained;
				return;
			}
	}
	public boolean isNodeContainedByAny(int contained){
		if (illegalContained(contained))
			return false;
		for (int containing = 0; containing<enteringContainedBranches.length; containing++){
			for (int i=0; i<enteringContainedBranches[containing].length; i++)
				if (enteringContainedBranches[containing][i] ==contained) 
					return true;
		}
		return false;
	}
	public boolean isNodeContained(int containing, int contained){
		if (illegalContained(contained) || illegalContaining(containing))
			return false;
		for (int i=0; i<enteringContainedBranches[containing].length; i++)
			if (enteringContainedBranches[containing][i] ==contained) 
				return true;
		return false;
	}

	public boolean isAncestorContained(int containing, int contained, Tree containedTree){
		if (illegalContained(contained) || illegalContaining(containing))
			return false;
		for (int i=0; i<enteringContainedBranches[containing].length; i++)
			if (containedTree.descendantOf(contained, enteringContainedBranches[containing][i])) 
				return true;
		return false;
	}
	public int[] getContainedNodes(int node){
		if (illegalContaining(node))
			return null;
		return enteringContainedBranches[node];
	}
	public int getNumberContainedNodes(int node){
		if (illegalContaining(node))
			return 0;
		int sum = 0;
		for (int i=0; i<enteringContainedBranches[node].length; i++) {
			if (enteringContainedBranches[node][i] ==MesquiteInteger.unassigned) 
				return sum;
			else sum++;
		}
		return sum;
	}
	public int[] getContainingNodes(int node){
		if (illegalContained(node))
			return null;
		int count=0;
		for (int k=0; k<enteringContainedBranches.length; k++)
			for (int i=0; i<enteringContainedBranches[k].length; i++) 
				if (enteringContainedBranches[k][i] ==node) 
					count++;
		if (count == 0)
			return null;
		int[] containing = new int[count];
		count = 0;
		for (int k=0; k<enteringContainedBranches.length; k++)
			for (int i=0; i<enteringContainedBranches[k].length; i++) {
				if (enteringContainedBranches[k][i] ==node) 
					containing[count++] = k;
			}
		return containing;
	}
	/*_________________________________________________*/
	/** this method recurses up the containing (species?) tree and calculates how the contained tree fits into it */
	public void countDuplicationsExtinctions(Tree speciesTree, Tree geneTree, MesquiteInteger duplications, MesquiteInteger extinctions) {
		boolean[] deleted = new boolean[geneTree.getNumNodeSpaces()];
		for (int it = 0; it< geneTree.getTaxa().getNumTaxa(); it++){
			int[] containing = getContainingNodes(geneTree.nodeOfTaxonNumber(it));
			if (containing == null || !isContainedInTree(containing, speciesTree)){
				geneTree.virtualDeleteTaxon(it, deleted);
			}
		}

		recCountDuplicationsExtinctions( speciesTree,  geneTree,  deleted, speciesTree.getSubRoot(),  duplications,  extinctions);
	}
	private boolean isContainedInTree(int[] containing, Tree speciesTree){
		for (int i=0; i<containing.length; i++)
			if (speciesTree.nodeInTree(containing[i]))
				return true;
		return false;
	}
	/*_________________________________________________*/
	/** this method recurses up the containing (species?) tree and calculates how the contained tree fits into it */
	public void recCountDuplicationsExtinctions(Tree speciesTree, Tree geneTree, boolean[] deleted, int speciesNode, MesquiteInteger duplications, MesquiteInteger extinctions) {

		int[][] genesInDaughterSpecies = new int[speciesTree.numberOfDaughtersOfNode(speciesNode)][];
		int[] daughterSpecies = new int[speciesTree.numberOfDaughtersOfNode(speciesNode)];
		int k =0;
		for (int d = speciesTree.firstDaughterOfNode(speciesNode); speciesTree.nodeExists(d); d = speciesTree.nextSisterOfNode(d)){
			daughterSpecies[k] = d;
			genesInDaughterSpecies[k++] = getContainedNodes(d);
			recCountDuplicationsExtinctions(speciesTree,geneTree, deleted, d, duplications, extinctions);
		}
		int dup = duplications.getValue();
		int ext = extinctions.getValue();
		int [] genesInSpecies;
		if (speciesNode == speciesTree.getSubRoot())
			genesInSpecies = new int[]{geneTree.getRoot(deleted)};
		else
			genesInSpecies = getContainedNodes(speciesNode);
		/*for each of these contained nodes:
		- (1) if also present in a species daughter directly, count an extinction event for each of that species daughter's daughters-1
		- if all of its daughters are internalized, then count a duplication event
		- if all of its duaghters are in species daughter, then count nothing
		- if some of its daughters are internalized and some in species daughter, then count extinctions for those in species daughter and uplication for internalized
		 */
		for (int i = 0; i<genesInSpecies.length && MesquiteInteger.isCombinable(genesInSpecies[i]); i++){
			int gene = genesInSpecies[i];
			int ds = findDaughterSpeciesContainingGene(genesInDaughterSpecies, gene);
			//(1)
			if (ds>=0){ //also present in daughter directly
				int numGranddaughterSpecies = speciesTree.numberOfDaughtersOfNode(daughterSpecies[ds]);
				if (numGranddaughterSpecies>1)
					extinctions.add(numGranddaughterSpecies-1);
			}
			else
				examineInternalizedGeneNode(genesInDaughterSpecies, daughterSpecies, speciesTree, geneTree, deleted, gene, duplications, extinctions);
		}
	}

	/*_________________________________________________*/
	private void examineInternalizedGeneNode(int[][] genesInDaughterSpecies, int[] daughterSpecies, Tree speciesTree, Tree geneTree,  boolean[] deleted, int gene, MesquiteInteger duplications, MesquiteInteger extinctions) {
		int numInternalized = 0;
		int numExtinctions =  0;
		int numInTerminalSpecies = 0;
		boolean terminalSpecies = false;
		for (int d = geneTree.firstDaughterOfNode(gene, deleted); geneTree.nodeExists(d); d = geneTree.nextSisterOfNode(d, deleted)){
			int daughterSpeciesContaining = findDaughterSpeciesContainingGene(genesInDaughterSpecies, d);
			if (daughterSpeciesContaining<0){ //internalized
				examineInternalizedGeneNode(genesInDaughterSpecies,daughterSpecies, speciesTree, geneTree, deleted, d, duplications, extinctions);
				numInternalized++;
			}
			else { // daughter gene is in daughter species
				if (speciesTree.nodeIsTerminal(daughterSpecies[daughterSpeciesContaining]))//species node is terminal, thus count
					numInTerminalSpecies++;
				else {
					int numGranddaughters = speciesTree.numberOfDaughtersOfNode(daughterSpecies[daughterSpeciesContaining]);
					if (numGranddaughters>1)
						numExtinctions+= numGranddaughters-1;
				}
			}

		}
		if (numInternalized>0){
			duplications.increment();
			extinctions.add(numExtinctions);
		}
		else if (numInTerminalSpecies>1)
			duplications.increment();
	}
	/*_________________________________________________*/
	private int findDaughterSpeciesContainingGene(int[][] genesInDaughterSpecies, int gene){
		for (int d = 0; d< genesInDaughterSpecies.length; d++){
			for (int c = 0; c< genesInDaughterSpecies[d].length; c++){
				if (genesInDaughterSpecies[d][c] == gene)
					return d;
			}
		}
		return -1;
	}
	/*_________________________________________________*/
	/* takes list of nodes of given tree and returns whether at least one of them is represented in descendants
	of passed node*/
	private boolean cladeRepresented(Tree tree, int[] nodes, int node){
		if (nodes == null)
			return false;
		for (int i=0; i<nodes.length; i++)
			if (nodes[i]==node)
				return true;
		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (cladeRepresented(tree, nodes, d))
					return true;
			}
		}
		return false;
	}
	/*_________________________________________________*/
	/* takes list of nodes of given tree and returns whether an entire clade resolvable from the immediate
	descendants of the ancestral node is present*/
	private boolean cladeResolvable(Tree tree, Tree containingTree, int containingNode,  boolean useDepths, int[] nodes, int node){
		if (nodes == null || !tree.nodeIsPolytomous(node))
			return false;
		if (tree.nodeIsInternal(node) && cladeRepresented(tree, nodes, node)) {
			int numD = 0;
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (cladeFullyPresent(tree, containingTree, containingNode,  useDepths, nodes, d, true))
					numD++;
			}
			if (numD>1)
				return true;
		}
		return false;
	}
	/*_________________________________________________*/
	/* takes list of nodes of given tree and returns whether the entire clade of the ancestral node is present*/
	private boolean cladeFullyPresent(Tree tree, Tree containingTree, int containingNode,  boolean useDepths, int[] nodes, int node, boolean countDirectInclusion){
		if (nodes == null)
			return false;
		if (useDepths && containingNode!=containingTree.getRoot()){
			//if (depth from node to tips in contained tree is greater than depth of containingNode's ancestor, then return false)
			if (tree.tallestPathAboveNode(node) > (containingTree.tallestPathAboveNode(containingNode) +containingTree.getBranchLength(containingNode, 1.0)) )
				return false;
		}
		if (countDirectInclusion) {
			for (int i=0; i<nodes.length; i++) {
				if (nodes[i]==node)
					return true;
			}
		}
		if (tree.nodeIsInternal(node)) {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (!cladeFullyPresent(tree, containingTree, containingNode,  useDepths, nodes, d, true))
					return false;
			}
			return true;
		}
		return false;
	}
	/*_________________________________________________*/
	/* takes list of nodes of given tree and resolves the tree to put resolvable clades together*/
	private boolean resolveClades(AdjustableTree tree, Tree containingTree, int containingNode,  boolean useDepths,int[] nodes, int node){
		boolean did=false;
		if (!cladeFullyPresent(tree, containingTree, containingNode,  useDepths, nodes, node, true)) { //clade present, no need to resolve further
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (IntegerArray.indexOf(nodes, d)<0){ //daughter isn't in list of thosed contained in containingNode; ??must be contained higher up, so go there
					boolean resolvd = resolveClades(tree, containingTree, containingNode,  useDepths, nodes, d);
					did = did || resolvd;
				}
			}
			if (cladeResolvable(tree,containingTree, containingNode,  useDepths,  nodes, node)) {
				int numTogether=0;
				int oldest = 0;
				int[] ds = tree.daughtersOfNode(node);
				for (int i = 0; i<ds.length; i++) {
					if (cladeFullyPresent(tree, containingTree, containingNode,  useDepths, nodes, ds[i], true)) {
						tree.setSelected(ds[i], true);
						numTogether++;
						if (numTogether<2) {  //this is first of sisters to be joined; remember it as a target for later joining
							oldest = ds[i];
						}
						else { //at least two sisters found; join them
							tree.moveBranch(ds[i], oldest, false);
							if (numTogether==2) { //first joining; mark mother as result of auto-resolution
								tree.setSelected(tree.motherOfNode(ds[i]), true);
							}
							else { //subsequent joining; collapse newly formed node to maintain polytomy of new sisters
								tree.collapseBranch(tree.motherOfNode(ds[i]), false);
							}
							did = true;
						}
					}
				}
			}
		}
		return did;
	}
	/*_________________________________________________*/
	/* takes list of nodes of given tree and returns how many separate clades are present*/
	private int numCladesPresent(Tree tree, Tree containingTree, int containingNode,  boolean useDepths,int[] nodes, int node){
		if (cladeFullyPresent(tree, containingTree, containingNode,  useDepths, nodes, node, true))
			return 1;
		int sum = 0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			sum += numCladesPresent( tree,containingTree, containingNode,  useDepths, nodes, d);
		}
		return sum;
	}
	/*_________________________________________________*/
	/* takes list of nodes of given tree and returns a list of the contained nodes (from among "nodes") that can be placed comfortably within containingNode*/
	private void accumulateCladesPresent(Tree tree, Tree containingTree, int containingNode,  boolean useDepths, int[] nodes, int node, int[] result, MesquiteInteger cladeCount){
		if (cladeFullyPresent(tree, containingTree, containingNode,  useDepths, nodes, node, true)) {
			if (cladeCount.getValue()< result.length)
				result[cladeCount.getValue()] = node;
			cladeCount.increment();
			return;
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			accumulateCladesPresent(tree, containingTree, containingNode,  useDepths, nodes, d, result, cladeCount);
		}
	}
	/*_________________________________________________*/
	/* takes list of nodes of given tree and finds set of clades that contain all and only those nodes, 
	and returning list of ancestral nodes of these clades.  Intended for use in fitting a gene tree to species tree to minimize
	deep coalescences.  If flag "allowResolve" is set, then the containing tree can
	be resolved where polytomies present, to group all members of polytomy together and return ancestral node of new clade.
	If useDepths is true, the relative branch lengths of the containing and contained branches are used to decide if the ancestral node is still in the branch;
	if not the set of ancestral nodes returned are those ancestors still in the branch.  That is, if nodes A and B are sister nodes, and are included
	in int[] nodes, then if useDepth were true AND their mother node is so deep as to be in the mother of containingNode, then A and B are
	returned separately in the ancestral nodes returned.  If their mother node is not so deep, or useDepth is false, then the mother node
	alone is returned in the ancestral nodes returned.*/
	public int[] condenseClades(AdjustableTree tree, Tree containingTree, int containingNode,  int[] nodes, boolean allowResolve, boolean useDepths) {
		if (nodes == null)
			return null;

		int numValid = 0;
		int recall = MesquiteInteger.unassigned;
		for (int i=0; i<nodes.length; i++) { //counting how many contained nodes here
			if (MesquiteInteger.isCombinable(nodes[i])) {
				numValid++;
				recall = nodes[i];//in case only one, remember it
			}
		}
		if (numValid==1) { //only a single contained node here, just return it
			int[] temp = new int[1];
			temp[0] = recall;
			return temp;
		}
		//prepare storage for ancestral nodes to be returned
		int[] temp = new int[numCladesPresent(tree, containingTree, containingNode,  useDepths, nodes, tree.getRoot())];

		//put into temp all of the ancestors found so far
		MesquiteInteger cladeCount = new MesquiteInteger(0);
		accumulateCladesPresent(tree, containingTree, containingNode,  useDepths, nodes, tree.getRoot(), temp, cladeCount);

		//if autoresolution is allowed, attempt to resolve polytomies to condense further by grouping potential sisters among the ancestors found so far
		if (allowResolve){ //allowed to resolve polytomies to minimize deep coalescences

			if (resolveClades(tree, containingTree, containingNode,  useDepths, temp, tree.getRoot())) {
				//Autoresolution occurred, thus need to re-condense, this time without auto-resolution
				return condenseClades(tree, containingTree, containingNode,  temp, false,  useDepths);
			}
		}
		return temp;
	}

	/*_________________________________________________*/
	public void findShallowest(int containing, int contained, MesquiteInteger shallowest) {
		if (isNodeContained(containing, contained)) {
			shallowest.setValue(containing);
			if (containingTree.nodeIsInternal(containing)) {
				for (int d = containingTree.firstDaughterOfNode(containing); containingTree.nodeExists(d); d = containingTree.nextSisterOfNode(d)) {
					findShallowest(d, contained, shallowest);
				}
			}
		}
	}
	/*_________________________________________________*/
	/** Returns the node number in the containing tree in which the contained node is placed */
	public int shallowestNodeThatContainedEnters(int contained){
		if (illegalContained(contained)||containingTree==null ||containedTree==null)
			return 0;
		for (int k=0; k<enteringContainedBranches.length; k++) {   // going through nodes in containing tree
			if (isNodeContained(k, contained)) {  // we've found one with our branch
				MesquiteInteger shallowest = new MesquiteInteger(0);
				findShallowest(k,contained,shallowest);
				return shallowest.getValue();
			}
		}		
		return 0;
	}
	/*_________________________________________________*/
	/** Returns the node number in the containing tree in which the contained node is placed */
	public int deepestNodeThatContainedEnters(int contained){ 
		if (locationOfContainedNode[contained] == 0)// dJCOs Added conditional to just return 0 for any node that isn't contained by any containing nodes.  Without this conditional, some internal gene tree nodes that are not assigned to species tree nodes will result in infinite loop.  Oliver.June.28.2012.
			return 0;
		if (illegalContained(contained)||containingTree==null)
			return 0;
		for (int k=0; k<enteringContainedBranches.length; k++) {   // going through nodes in containing tree
			if (isNodeContained(k, contained)) {  // we've found one with our branch
				int prev = k;
				for (int anc=k; anc!= containingTree.getRoot(); anc = containingTree.motherOfNode(anc)) {   
					if (!isNodeContained(anc,contained))
						return prev;
					else
						prev = anc;
				}
				if (isNodeContained(containingTree.getRoot(),contained))
					return containingTree.getRoot();
				else
					return prev;
			}

		}		
		return 0;
	}
	/*_________________________________________________*/
	/** searches to see if a descendant in the contained tree of the node contained enters into the containing tree node containing; if so, returns true in found */
	public void findDescendentThatEnters(int containing, int contained, MesquiteBoolean found) {
		if (isNodeContained(containing, contained))
			found.setValue(true);
		if (found.getValue())
			return;
		if (containedTree.nodeIsInternal(contained)) {
			for (int d = containedTree.firstDaughterOfNode(contained); containedTree.nodeExists(d)&&!found.getValue(); d = containedTree.nextSisterOfNode(d)) {
				findDescendentThatEnters(containing, d, found);
			}
		} /*else {  // we are at a terminal in the contained tree
			int[] nodesContainedInTerminal = getContainedNodes(containing);
			for (int k=0; k<nodesContainedInTerminal.length; k++) {
				if (nodesContainedInTerminal[k]!=MesquiteInteger.unassigned){
					if (containingTree.descendantOf(nodesContainedInTerminal[k], contained)){
						found.setValue(true);
						return;
					}
				}
			}

		}
		 */
	}
	/*_________________________________________________*/
	/** does containing contain a descendant of contained */
	public void foundDescendant(int contained, MesquiteInteger containingDescendant) {
		int containing = deepestNodeThatContainedEnters(contained);
		if (containing!=0)  // we've found one
			containingDescendant.setValue(containing);
		if (containingDescendant.isCombinable())
			return;
		//if (containedTree.nodeIsInternal(containing)) { // dJCOs This was original implementation. Oliver.July.04.2012
		if (containedTree.nodeIsInternal(contained)) { // dJCOe Changed so containedTree is looking to see if contained node is internal. Oliver.July.04.2012
			for (int d = containedTree.firstDaughterOfNode(contained); containedTree.nodeExists(d)&&!containingDescendant.isCombinable(); d = containedTree.nextSisterOfNode(d)) {
				foundDescendant(d, containingDescendant);
			}
		} 

	}
	/*_________________________________________________*/
	public void findLocationOfContainedNodesRec(int contained) {

		if (containedTree.nodeIsInternal(contained)) {
			for (int d = containedTree.firstDaughterOfNode(contained); containedTree.nodeExists(d); d = containedTree.nextSisterOfNode(d)) {
				findLocationOfContainedNodesRec(d);
			}
			int shallowest = shallowestNodeThatContainedEnters(contained);
			if (shallowest==0){// couldn't find it, so it must be buried within the clade.
				//we need to look for descendants
				MesquiteInteger containingDescendant =new MesquiteInteger();
				for (int d = containedTree.firstDaughterOfNode(contained); containedTree.nodeExists(d); d = containedTree.nextSisterOfNode(d)) {
					foundDescendant(d, containingDescendant);
				}
				if (containingDescendant.isCombinable())
					locationOfContainedNode[contained] = containingDescendant.getValue();
			} else if (containingTree.nodeIsTerminal(shallowest))
				locationOfContainedNode[contained] = shallowest;
			else {  // we found the shallowest (closest to terminals) containing node into which our contained node enters
				// so we know the contained node itself is present in one of the immediate descendants of containing, but which one?
				for (int d = containingTree.firstDaughterOfNode(shallowest); containingTree.nodeExists(d); d = containingTree.nextSisterOfNode(d)) {
					MesquiteBoolean found = new MesquiteBoolean(false);
					findDescendentThatEnters(d, contained, found);
					if (found.getValue()) {
						locationOfContainedNode[contained] = d;
						break;
					}
				}
			}

		} else {  // it's a terminal node in the containedTree
			for (int containing=0; containing<enteringContainedBranches.length; containing++) {   // going through nodes in containing tree
				if (isNodeContained(containing, contained) && containingTree.nodeIsTerminal(containing)) {  // we've found the terminal with our branch
					locationOfContainedNode[contained] = containing;
					break;
				}
			}		
		}
	}

	/*_________________________________________________*/
	/* this must be called to fill in the array with the location of contained nodes */
	public void  findLocationOfContainedNodes() {
		if (containedTree!=null){
			int numContained = containedTree.getNumNodeSpaces();
			if (locationOfContainedNode==null || locationOfContainedNode.length<numContained)
				locationOfContainedNode = new int[numContained];
		}
		findLocationOfContainedNodesRec(containedTree.getRoot());

	}

	/*_________________________________________________*/
	private void findEnteringBranches (int contained) {
		if (containedTree.nodeIsInternal(contained)) {
			for (int d = containedTree.firstDaughterOfNode(contained); containedTree.nodeExists(d); d = containedTree.nextSisterOfNode(d)) {
				findEnteringBranches(d);
			}	
		} else {  // it's a terminal node in the containedTree
			setContainedNode(locationOfContainedNode[contained],  contained);
		}

		int anc = containedTree.motherOfNode(contained);
		if (containedTree.nodeExists(anc) && anc!=containedTree.getSubRoot()) {
			int locationOfMother = locationOfContainedNode[anc]; // so it must enter this containingNode, and any node back to it.
			int locationOfContained = locationOfContainedNode[contained];
			if (locationOfMother!=locationOfContained) 
				for (int containingAnc = containingTree.motherOfNode(locationOfContained); containingAnc>0 && containingAnc!=containingTree.getSubRoot(); containingAnc = containingTree.motherOfNode(containingAnc)){
					setContainedNode(containingAnc,  contained);
					if (containingAnc==locationOfMother)
						break;
				}
		}

	}

	/*_________________________________________________*/
	private void calcEnteringContainedBranchesFromLocations() {
		if (containedTree==null || containingTree==null || enteringContainedBranches==null || locationOfContainedNode==null)
			return;
		for (int i=0; i<enteringContainedBranches.length; i++) 
			for (int j=0; j<enteringContainedBranches[i].length; j++)
				enteringContainedBranches[i][j] = MesquiteInteger.unassigned;
		findEnteringBranches(containedTree.getRoot());
		//enteringContainedBranches
		//locationOfContainedNode
	}

	/*_________________________________________________*/
	public int  getLocationOfContainedNode(int contained) {
		return locationOfContainedNode[contained];
	}
	/*_________________________________________________*/
	public int[]  getLocationsOfContainedNodes() {
		return locationOfContainedNode;
	}


	/*_________________________________________________*/
	public String toString() {
		String s = "Association History" + ParseUtil.lineEnding();
		for (int i=0; i<enteringContainedBranches.length; i++) {
			if (enteringContainedBranches[i][0]!= MesquiteInteger.unassigned) {
				s+= "\tEntering containing node " + i + ": ";
				for (int j=0; j<enteringContainedBranches[i].length; j++)
					if (enteringContainedBranches[i][j] != MesquiteInteger.unassigned)
						s += enteringContainedBranches[i][j] + "; ";
				s += ParseUtil.lineEnding();
			}
		}
		if (locationOfContainedNode!=null) {
			s += "Locations of contained nodes" + ParseUtil.lineEnding();
			for (int i=0; i<locationOfContainedNode.length; i++) {
				if (locationOfContainedNode[i]!=0)
					s+= "\tContained node " + i + " is present in containing node: "+ locationOfContainedNode[i]+ ParseUtil.lineEnding();
			}
		}
		return s;
	}
}

/*-------------------------------------*/




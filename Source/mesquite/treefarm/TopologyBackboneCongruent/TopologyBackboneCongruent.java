package mesquite.treefarm.TopologyBackboneCongruent;

/*New October 7, 2008. oliver*/
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class TopologyBackboneCongruent extends BooleanForTree {

	OneTreeSource constraintTreeSource;
	Tree constraintTree;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		constraintTreeSource = (OneTreeSource)hireEmployee(OneTreeSource.class, "One Tree Source");
		if(constraintTreeSource==null){
			return sorry(getName() + " couldn't start because no constraint tree was obtained.");
		}
		return true;
	}
	/*..............................................................................*/
	/**Checks to see if passed Tree sTree is consistent with constraint tree cTree; is not currently set up to handle
	 * backbone constraints, but reciprocal calls of pruneToMatch accomodate this in future versions.*/
	private void checkClades(int node, Tree sTree, Taxa taxa, MesquiteBoolean isConsistent){
		while(isConsistent.getValue() && constraintTree.nodeExists(node)){
			if(constraintTree.nodeIsInternal(node)){
				Bits terms = constraintTree.getTerminalTaxaAsBits(node);
				if(!sTree.isClade(terms)){
					isConsistent.setValue(false);
				}
				if(isConsistent.getValue()){
					int d = constraintTree.firstDaughterOfNode(node);
					checkClades(d, sTree, taxa, isConsistent);
				}
			}
			node = constraintTree.nextSisterOfNode(node);
		}
	}
	/*..............................................................................*/
	/**Prunes the passed treeToPrune so it only has taxa that are present in treeToMatch (which may contain additional taxa that treeToPrune does not).  Allows comparison to backbone constraint tree.*/
	private void pruneToMatch(MesquiteTree treeToPrune, Tree treeToMatch){
		int numTaxa = treeToPrune.getNumTaxa();
		for(int iN = 0; iN < numTaxa; iN++){
			if(!treeToMatch.taxonInTree(iN) && treeToPrune.taxonInTree(iN)){
				treeToPrune.deleteClade(treeToPrune.nodeOfTaxonNumber(iN), false);
			}
		}
	}
	/*..............................................................................*/
	public void calculateBoolean(Tree tree, MesquiteBoolean result,	MesquiteString resultString) {
		if(tree==null || result==null){
			return;
		}
		constraintTree = constraintTreeSource.getTree(tree.getTaxa()).cloneTree();
		if(constraintTree==null || constraintTree.getTaxa()!=tree.getTaxa())
			return;
		MesquiteTree prunedTree = tree.cloneTree();
		pruneToMatch(prunedTree, constraintTree);
		MesquiteBoolean isConsistent = new MesquiteBoolean(true);
		if(!(prunedTree.numberOfTerminalsInClade(prunedTree.getRoot())>0)){
			isConsistent.setValue(false);
		}
		checkClades(constraintTree.getRoot(), prunedTree, tree.getTaxa(), isConsistent);
		result.setValue(isConsistent.getValue());
		if (resultString!=null)
			if (isConsistent.getValue())
				resultString.setValue("Tree congruent");
			else
				resultString.setValue("Tree incongruent");
	}
	/*..............................................................................*/
	public String getName() {
		return "Tree Congruent with Backbone Constraint Tree Topology";
	}
	/*..............................................................................*/
 	/** returns an explanation of what the module does.*/
	public String getExplanation(){
		return "Determines if tree matches topology of a given backbone constraint tree.";
	}
	/*........................................................*/
    public int getVersionOfFirstRelease(){
        return NEXTRELEASE;
    }
    /*........................................................*/
    public boolean isPrerelease(){
    	return true;
    }
}

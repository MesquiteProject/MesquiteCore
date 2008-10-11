package mesquite.treefarm.TopologyCongruent;

/*New October 7, 2008. oliver*/
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class TopologyCongruent extends BooleanForTree {
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
	/**Checks to make sure both trees contain same terminal taxa*/
	private boolean taxaCheck(Tree sTree, Tree cTree, Taxa taxa){
		boolean both = true;
		int taxonCount=0;
		while (both && taxonCount<taxa.getNumTaxa()){
			if(sTree.taxonInTree(taxonCount)){
				if(!cTree.taxonInTree(taxonCount)){
					both=false;
					return both;
				}
			}
			if(cTree.taxonInTree(taxonCount)){
				if(!sTree.taxonInTree(taxonCount)){
					both=false;
					return both;
				}
			}
			taxonCount++;
		}
		return both;
	}
	/*..............................................................................*/
	public void calculateBoolean(Tree tree, MesquiteBoolean result,	MesquiteString resultString) {
		if(tree==null || result==null){
			return;
		}
		constraintTree = constraintTreeSource.getTree(tree.getTaxa()).cloneTree();
		if(constraintTree==null || constraintTree.getTaxa()!=tree.getTaxa())
			return;
		MesquiteBoolean isConsistent = new MesquiteBoolean(true);
		if(!taxaCheck(tree, constraintTree, tree.getTaxa())){
			isConsistent.setValue(false);
		}
		else {
			checkClades(constraintTree.getRoot(), tree, tree.getTaxa(), isConsistent);
		}
		result.setValue(isConsistent.getValue());
		if (resultString!=null)
			if (isConsistent.getValue())
				resultString.setValue("Tree congruent");
			else
				resultString.setValue("Tree incongruent");
	}
	/*..............................................................................*/
	public String getName() {
		return "Tree Congruent with Constraint Tree Topology";
	}
	/*..............................................................................*/
 	/** returns an explanation of what the module does.*/
	public String getExplanation(){
		return "Determines if tree matches topology of a given constraint tree.  This module does not handle backbone constraints; all trees must have the same taxa present.  For backbone constraint trees, where the constraint tree need not contain all taxa, use the 'Tree Congruent with Backbone Constraint Tree Topology' module.";
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

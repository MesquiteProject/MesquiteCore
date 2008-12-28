package mesquite.trees.RemoveTaxaFromTree;

/*New October 7, '08. oliver*/
import mesquite.lib.*;
import mesquite.lib.duties.TreeAlterer;

public class RemoveTaxaFromTree extends TreeAlterer {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*...............................................................*/
	public boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if(tree==null)
			return false;
		Taxa taxa = tree.getTaxa();
		int count = 0;
		//counting the number of taxa currently in tree
		for (int it = 0; it < taxa.getNumTaxa(); it++){
			if(tree.taxonInTree(it)){
				count++;
			}
		}
		if(count==0){
			discreetAlert("The tree does not contain any taxa, so none could be removed");
			return true;
		}
		//getting list of included
		ListableVector included = new ListableVector(count);
		count = 0;
		for(int it=0; it<taxa.getNumTaxa(); it++){
			if(tree.taxonInTree(it)){
				included.addElement(taxa.getTaxon(it), false);
			}
		}
		//presenting list of taxa, user chooses which to remove from tree
		Listable[] toExclude = ListDialog.queryListMultiple(containerOfModule(), "Remove taxa from tree", "Select taxa to be removed from the tree", MesquiteString.helpString, "Remove", false, included, null);
		if(toExclude==null || toExclude.length==0){
			return true;
		}
		if(toExclude.length==tree.numberOfTerminalsInClade(tree.getRoot())){
			discreetAlert("Cannot remove all taxa from tree");
			return true;
		}
		//removing taxa from tree
		for(int it = 0; it < toExclude.length; it++){
			int taxon = tree.nodeOfTaxonNumber(taxa.whichTaxonNumber((Taxon)toExclude[it]));
//			int taxon = taxa.whichTaxonNumber((Taxon)toExclude[it]);
			tree.deleteClade(taxon, false);
		}
		if(notify && tree instanceof Listened) {
			((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
		}
		return true;
	}
	/*...............................................................*/
	public String getName() {
		return "Remove taxa from tree";
	}
	/*...............................................................*/
	public String getExplanation(){
		return "Removes taxa from tree.";
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

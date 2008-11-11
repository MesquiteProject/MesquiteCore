package mesquite.treefarm.RemoveTaxaFromTrees;

/*New October 7, 2008. oliver*/
import mesquite.lib.*;
import mesquite.lib.duties.TreeAltererMult;

public class RemoveTaxaFromTrees extends TreeAltererMult {
	Taxa currentTaxa = null;
	boolean unselectedAlreadyWarned = false;
	int treeNumber = 0;

	/*........................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*........................................................*/
	public void endJob(){
  	 	if (currentTaxa != null)
  	 		currentTaxa.removeListener(this);
 		super.endJob();
	}
	/*........................................................*/
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj == currentTaxa && code == MesquiteListener.SELECTION_CHANGED){
			parametersChanged();
		}
	}
 	/*........................................................*/
	/**The focal method, used to actually remove taxa from trees*/
	public boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if(tree==null)
			return false;
		Taxa taxa = tree.getTaxa();
		if(currentTaxa != taxa){
			if(currentTaxa != null){
				currentTaxa.removeListener(this);
			}
			currentTaxa = taxa;
			currentTaxa.addListener(this);
		}
		if(!taxa.anySelected()){
			if(!unselectedAlreadyWarned){
				discreetAlert("Before taxa can be removed from trees, some taxa must be selected");
			}
			unselectedAlreadyWarned = true;
			return false;
		}
		treeNumber++;
		Bits toCut = taxa.getSelectedBits();
		for (int it = 0; it < taxa.getNumTaxa(); it++){
			if(tree.taxonInTree(it)){
				int currentNode = tree.nodeOfTaxonNumber(it);
				int currentTaxon = tree.taxonNumberOfNode(currentNode);
				if(tree.nodeExists(currentNode) && toCut.isBitOn(currentTaxon)){
					tree.deleteClade(currentNode, true); //TODO:notify was false, but it was not notifying the Tree List.
				}
			}
		}
		if (notify && tree instanceof Listened) {
			((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
		}
		return true;
	
	}
 	/*........................................................*/
	public String getName() {
		return "Remove Selected Taxa from Trees";
	}
	/*........................................................*/
	public String getExplanation(){
		return "Removes selected taxa from trees.";
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

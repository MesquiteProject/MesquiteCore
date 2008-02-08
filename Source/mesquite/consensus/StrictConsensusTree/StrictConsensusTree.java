package mesquite.consensus.StrictConsensusTree;

import mesquite.consensus.lib.*;
import mesquite.lib.*;

/* ======================================================================== */
/** Does strict consensus .*/

public class StrictConsensusTree extends BasicTreeConsenser   {
	public String getName() {
		return "Strict Consensus";
	}
	public String getExplanation() {
		return "Calculates the strict consensus tree." ;
	}
  	public void addTree(Tree t){
		bipartitions.addTree(t);
	}
	/*.................................................................................................................*/
 	public void initialize() {
 		if (bipartitions!=null)
 			bipartitions.setMode(BipartitionVector.STRICTMODE);
 	}

 	public Tree getConsensus(){
		Tree t = bipartitions.makeTree();
		return t;
	}
}

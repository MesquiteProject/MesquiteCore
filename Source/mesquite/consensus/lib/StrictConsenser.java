package mesquite.consensus.lib;


import mesquite.lib.Taxa;
import mesquite.lib.Tree;

/* A utility class designed to be used by modules to easily make a strict consensus of trees */

public class StrictConsenser {
	
	protected BipartitionVector bipartitions = new BipartitionVector();
	protected int treeNumber = 0;

	/*.................................................................................................................*/
  	public void reset(Taxa taxa){
  		if (bipartitions==null)
  			bipartitions = new BipartitionVector();
  		else
  			bipartitions.removeAllElements();		// clean bipartition table
		bipartitions.setTaxa(taxa);
		bipartitions.zeroFrequencies();
		initialize();
	}
  	public void addTree(Tree t){
		if (t==null)
			return;
		bipartitions.addTree(t);
	}
	/*.................................................................................................................*/
 	public void initialize() {
 		if (bipartitions!=null)
 			bipartitions.setMode(BipartitionVector.STRICTMODE);
 	}
	/*.................................................................................................................*/
 	public Tree getConsensus(){
		Tree t = bipartitions.makeTree();
		return t;
	}
	/*.................................................................................................................*/

}


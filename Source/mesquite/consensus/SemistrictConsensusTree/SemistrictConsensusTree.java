package mesquite.consensus.SemistrictConsensusTree;

import mesquite.consensus.lib.*;
import mesquite.lib.*;

	/* ======================================================================== */
	/** Does semistrict consensus .*/

	public class SemistrictConsensusTree extends BasicTreeConsenser   {
		public String getName() {
			return "Semistrict Consensus";
		}
		
		public String getExplanation() {
			return "Calculates the semistrict consensus tree." ;
		}
	  	public void addTree(Tree t){
			bipartitions.addTree(t);
		}
	  	
		/*.................................................................................................................*/
	 	public void initialize() {
	 		if (bipartitions!=null)
	 			bipartitions.setMode(BipartitionVector.SEMISTRICTMODE);
	 	}

	 	public Tree getConsensus(){
			Tree t = bipartitions.makeTree();
			return t;
		}
	 	
		/*.................................................................................................................*/
	   	public boolean isPrerelease(){
	   		return true;  
	   	}
		/*.................................................................................................................*/
		public boolean isSubstantive(){
			return true;
		}   	 
		/*.................................................................................................................*/
		public int getVersionOfFirstRelease(){
			return NEXTRELEASE;  
		}

	}

package mesquite.align.AlignScoreForTaxon;

import mesquite.align.lib.*;
import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* TODO: 
 *   have it so the reference sequence can be chosen
 *   report pairwise alignment parameters
 *   snapshot, etc.
 */

public class AlignScoreForTaxon extends AlignScoreForTaxonGen {
	/*.................................................................................................................*/
	protected  void getAlignmentScore(DNAData data, MCategoricalDistribution observedStates,  int it1, int it2, MesquiteNumber score, CommandRecord commandRec) {
		if (aligner!=null)
			aligner.alignSequences(observedStates, it1, it2,MesquiteInteger.unassigned,MesquiteInteger.unassigned,false,score,commandRec);
   	 }
	/*.................................................................................................................*/
	 public String getName() {
	return "Aligment Score To First";  
	 }
	/*.................................................................................................................*/
	 public String getScoreName() {
	return "Aligment Score";  
	 }
  	 
}




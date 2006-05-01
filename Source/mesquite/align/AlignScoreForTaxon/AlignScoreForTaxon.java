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
  	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE double,
  	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
  	 * If a NEGATIVE double,  thne the number refers to the local version of the package, e.g. a third party package*/
     	public double getVersionOfFirstRelease(){
     		return 1.07;  
   }
	/*.................................................................................................................*/
	 public String getName() {
	return "Aligment Score To Ref.";  
	 }
	/*.................................................................................................................*/
	 public String getScoreName() {
	return "Aligment Score";  
	 }
  
		/*.................................................................................................................*/
	 public String getExplanation() {
		 String s = super.getExplanation();  
		 return s+ " The score for each taxon is the cost of aligning that taxon's sequence against the reference taxon using Mesquite's default pairwise aligner.  Lower scores mean a better alignment.";
	 }
	 /*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }

}




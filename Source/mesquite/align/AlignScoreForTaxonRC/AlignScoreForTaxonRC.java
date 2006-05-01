package mesquite.align.AlignScoreForTaxonRC;

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

public class AlignScoreForTaxonRC extends AlignScoreForTaxonGen {
	/*.................................................................................................................*/
	protected  void getAlignmentScore(DNAData data, MCategoricalDistribution observedStates, int it1, int it2, MesquiteNumber score, CommandRecord commandRec) {
		if (aligner==null)
			return;
		int firstSite = 0;
		int lastSite = data.getNumChars()-1;
		int numChars = lastSite - firstSite+1;
		
		long[] extracted1 = new long[numChars];
		long[] extracted2 = new long[numChars];
		
		for (int ic = firstSite; ic<=lastSite; ic++){
			extracted1[ic] = data.getState(ic, it1);
			extracted2[ic] = data.getState(ic, it2);
		}
		MesquiteNumber alignScore = new MesquiteNumber();
  		CategoricalState state = data.getNewState();
		aligner.alignSequences(extracted1, extracted2, false, alignScore);

		for (int ic = firstSite; ic<=lastSite; ic++){
			extracted1[lastSite-ic] = DNAData.complement(data.getState(ic, it1));
		}
		MesquiteNumber alignRCScore = new MesquiteNumber();
		aligner.alignSequences(extracted1, extracted2, false, alignRCScore);
		alignScore.divideBy(alignRCScore);
		score.setValue(alignScore);

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
	return "Align Score/RC Align Score To Ref.";  
	 }
	/*.................................................................................................................*/
	 public String getScoreName() {
	return "Alignment Score / Reverse Complement Alignment Score";  
	 }
		/*.................................................................................................................*/
	 public String getExplanation() {
		 String s = super.getExplanation();  
		 return s+ " The score for each taxon is the cost of aligning that taxon's sequence against the reference taxon using Mesquite's default pairwise aligner, divided by the score for aligning the reverse complement of the sequence against the reference.  Scores  greater than one mean that alignment is better if one of the sequences is reverse complemented .";
	 }
	  	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }
  	 
}




/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.72, December 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.align.AlignScoreForTaxonRC;

import mesquite.align.lib.*;
import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;



public class AlignScoreForTaxonRC extends AlignScoreForTaxonGen {
	/*.................................................................................................................*/
	protected  void getAlignmentScore(DNAData data, MCategoricalDistribution observedStates, int it1, int it2, MesquiteNumber score) {
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
  		CategoricalState state = (CategoricalState)data.makeCharacterState();
		aligner.alignSequences(extracted1, extracted2, false, alignScore);

		for (int ic = firstSite; ic<=lastSite; ic++){
			extracted1[lastSite-ic] = DNAState.complement(data.getState(ic, it1));
		}
		MesquiteNumber alignRCScore = new MesquiteNumber();
		aligner.alignSequences(extracted1, extracted2, false, alignRCScore);
		alignScore.divideBy(alignRCScore);
		score.setValue(alignScore);

   	 }
	   /*.................................................................................................................*/
  	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
  	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
  	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
     	public int getVersionOfFirstRelease(){
     		return -100;  
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




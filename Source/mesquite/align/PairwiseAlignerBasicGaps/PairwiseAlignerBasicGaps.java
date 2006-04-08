package mesquite.align.PairwiseAlignerBasicGaps;

import mesquite.align.lib.*;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;
import mesquite.categ.lib.*;

public class PairwiseAlignerBasicGaps extends TwoSequenceAlignerGaps {
	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		// TODO load prefs?  (see PhredPrap for example)
		//put in loadPreferences and storePreferences
		// processSinglePreferenceForXML ... 
        // preparePreferencesForXML
		return true;
	}

	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}

	/**
	 * Override method in superclass
	 */
	public long[][] alignSequences(long[] A_withGaps, long[] B_withGaps, boolean returnAlignment, MesquiteNumber score, CategoricalState state, CommandRecord commandRec) {
   		MesquiteInteger gapOpen = new MesquiteInteger();
   		MesquiteInteger gapExtend = new MesquiteInteger();
 		int alphabetLength = state.getMaxPossibleState()+1;
  		int subs[][] = AlignUtil.getDefaultCosts(gapOpen, gapExtend, alphabetLength);  


   		PairwiseAligner pa = new PairwiseAligner(true,subs,gapOpen.getValue(), gapExtend.getValue(), alphabetLength);
		return pa.alignSequences(A_withGaps,B_withGaps,returnAlignment, score);
	}
	
	
	public String getName() {
		return "Basic Pairwise Aligner, preserving gaps in first sequence";
	}

 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs a basic pairwise alignment, preserving gaps in the first sequence." ;
   	 }
}

package mesquite.align.PairwiseAlignerLowMemGaps;

import mesquite.align.lib.PairwiseAligner;
import mesquite.align.lib.TwoSequenceAligner;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.CommandRecord;
import mesquite.lib.Debugg;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;


public class PairwiseAlignerLowMemGaps extends TwoSequenceAligner {
	boolean isMinimize = true;	
    //	internal representation of gap costs is that first gap char costs gapOpen + gapExtend, and each additional character costs gapExtend 
	
	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		//TODO gather preferences
		return true;
	}

	public String getName() {
		return "Low Memory Pairwise Sequence Aligner, preserving gaps in first sequence";
	}

	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}

	
	public long[][] alignSequences(long[] sequence1, long[] sequence2, boolean returnAlignment, MesquiteNumber score, CommandRecord commandRec) {

   		MesquiteInteger gapOpen = new MesquiteInteger();
   		MesquiteInteger gapExtend = new MesquiteInteger();
   		int subs[][] = pickCosts(gapOpen, gapExtend);  

   		PairwiseAligner pa = new PairwiseAligner(true,subs,gapOpen.getValue(), gapExtend.getValue());
		pa.setUseLowMem(true);
		return pa.alignSequences(sequence1,sequence2,returnAlignment, score);

	}		

}

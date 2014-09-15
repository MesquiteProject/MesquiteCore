/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.lib;

/* ~~ */

import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public abstract class TwoSequenceAligner extends MesquiteModule {
	public Class getDutyClass() {
		return TwoSequenceAligner.class;
	}

	public String getDutyName() {
		return "Pairwise Sequence Aligner";
	}

	/**
	 * Subclasses can override this to have access to full information about
	 * data.  What are to be aligned are only the taxa indicated for the block (firstSite to lastSite) indicated.
	 * These sites, aligned, are to be returned as a long[][].  Thus, this array may be smaller in dimensions than the 
	 * MCategoricalDistribution passed.  The MCategoricalDistribution may be queried for states by
	 * long nucleotide = data.getState(iCharacter, iTaxon);
	 * Longs are used as bitsets for state sets (see mesquite.lib.CategoricalState).
	 * 
	 * Note that LIKE MOST PLACES IN MESQUITE, the long[][] returned here has characters first and taxa second;
	 * thus, it should be returned as a long[numChars][2]
	 * 
	 * If score is non-null, then the subclass should fill this with the score of the alignment.
	 *
	 */
	public long[][] alignSequences(MCategoricalDistribution data, int taxon1, int taxon2, int firstSite, int lastSite, boolean returnAlignment, MesquiteNumber score) {
		if (lastSite - firstSite+1 <0 || !MesquiteInteger.isCombinable(firstSite) || !MesquiteInteger.isCombinable(lastSite)){
			firstSite = 0;
			lastSite = data.getNumChars()-1;
		}
		if (firstSite<0)
			firstSite=0;
		if (lastSite>data.getNumChars()-1 || lastSite<0)
			lastSite = data.getNumChars()-1;
		int numChars = lastSite - firstSite+1;
		
		long[] extracted1 = new long[numChars];
		long[] extracted2 = new long[numChars];
		

		for (int ic = firstSite; ic<=lastSite; ic++){
			extracted1[ic-firstSite] = data.getState(ic, taxon1);
			extracted2[ic-firstSite] = data.getState(ic, taxon2);
		}
		CategoricalState state=(CategoricalState)(data.getParentData().makeCharacterState());
		long[][] aligned =  alignSequences(extracted1, extracted2, returnAlignment, score, state);
	
		return aligned;
	}

	/**
	 * This method is not called directly by Mesquite, but modules preferring to
	 * receive and return the information as long[][] can override this and let the previous
	 * method call it.  Also, this method receives only the pieces to be aligned.
	 * Note that the long that is returned should be long[][2] in size.  
	 */
	public long[][] alignSequences(long[] sequence1, long[] sequence2, boolean returnAlignment, MesquiteNumber score, CategoricalState state) {
		return null;
	}
	
	/** This method returns the score of two identical copies of the passed sequence.  Override if needed. *
	public MesquiteNumber getScoreOfIdenticalSequences(long[] sequence) {
		return new MesquiteNumber(0.0);
	}

	/** This method returns the score of the worst possible match with the passed sequence.  Acceptable if result is approximate.  Override if needed. 
	public MesquiteNumber getVeryBadScore(long[] sequence, CategoricalState state) {
		MesquiteNumber score = new MesquiteNumber();
		long[]opposite = new long[sequence.length];
		for (int i=0; i<sequence.length; i++) {
			opposite[i] = DNAState.complement(sequence[i]);
		}
		alignSequences(sequence,opposite, false, score, state);
		return score;
	}


	/*
	 * public int[][] pickCosts(MesquiteInteger gapOpen, MesquiteInteger gapExtend, int alphabetLength){
	    //	internal representation of gap costs is that first gap char costs gapOpen+gapExtend, and each additional character costs gapExtend
		gapOpen.setValue(8);
		gapExtend.setValue(3);
		
		int subs[][] =  new int[alphabetLength][alphabetLength];	
		subs[0][0] = subs[1][1] = subs[2][2] = subs[3][3] = 0;
		subs[0][1] = subs[1][0] = subs[0][3] = subs[3][0] = 10; //transversions involving A
		subs[0][2] = subs[2][0] = subs[1][3] = subs[3][1]  = 5; //transitions
		subs[2][3] = subs[3][2] = subs[1][2] = subs[2][1]= 10; //transversions involving G
		return subs;
	}
*/
	
	
}


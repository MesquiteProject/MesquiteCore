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
public abstract class TwoSequenceAlignerGaps extends TwoSequenceAligner {
	public Class getDutyClass() {
		return TwoSequenceAlignerGaps.class;
	}

	public String getDutyName() {
		return "Pairwise Sequence Aligner (Maintains Gaps)";
	}

	/**
	 * As a subclass of TwoSequenceAligner, the methods here have passed into them some 
	 * additional arguments that ideally would be dealt with.
	 * If maintainGaps is true, then the gaps that are present in sequence1 should be maintained.
	 * 
	 * Also, if maintainGaps is true, and newGaps1 is non-null, then newGaps1 should returned 
	 * with bits filled where new gaps are added to sequence1.  Imagine that newGaps1 came in
	 * as a Bits with 10 elements.  It should come in with all bits turned off, like this:  0000000000.  
	 * If 3 new gaps were added to
	 * sequence1, after positions 3, 6, and 8, then newGaps1 should come out with 13 elements, looking
	 * like this:  0001000100100.  To do this, use the methods in Bits.  e.g., Bits.addParts to insert new bits, and
	 * then Bits.setBit to turn on the bits for the newly added bits.
	 */

	public long[][] alignSequences(MCategoricalDistribution data, int taxon1, int taxon2, int firstSite, int lastSite, boolean returnAlignment, MesquiteNumber score, boolean maintainGaps, Bits newGaps1 ) {
		int numChars = lastSite - firstSite+1;
		if (numChars <0 || !MesquiteInteger.isCombinable(firstSite) || !MesquiteInteger.isCombinable(lastSite)){
			numChars = data.getNumChars();
			firstSite = 0;
			lastSite = numChars-1;
		}
		long[] extracted1 = new long[numChars];
		long[] extracted2 = new long[numChars];

		for (int ic = firstSite; ic<=lastSite; ic++){
			extracted1[ic-firstSite] = data.getState(ic, taxon1);
			extracted2[ic-firstSite] = data.getState(ic, taxon2);
		}
		CategoricalState state=(CategoricalState)(data.getParentData().makeCharacterState());
		long[][] aligned =  alignSequences(extracted1, extracted2, returnAlignment,score, state,  maintainGaps, newGaps1);
		return aligned;
	}

	public long[][] alignSequences(MCategoricalDistribution data, int taxon1, int taxon2, int firstSite, int lastSite, boolean returnAlignment, MesquiteNumber score) {
		return alignSequences(data,taxon1,taxon2,firstSite, lastSite,returnAlignment,score,false,null);
	}
	
	/**
	 * This method is not called directly by Mesquite, but modules preferring to
	 * receive and return the information as long[][] can override this and let the previous
	 * method call it.  Also, this method receives only the pieces to be aligned.
	 * Note that the long that is returned should be long[][2] in size.  
	 */
	public long[][] alignSequences(long[] sequence1, long[] sequence2, boolean returnAlignment, MesquiteNumber score, CategoricalState state,  boolean maintainGaps, Bits newGaps1) {
		return null;
	}
	
	public long[][] alignSequences(long[] sequence1, long[] sequence2, boolean returnAlignment, MesquiteNumber score, CategoricalState state) {
		return alignSequences(sequence1,sequence2,returnAlignment,score,state, false, null);
	}

}


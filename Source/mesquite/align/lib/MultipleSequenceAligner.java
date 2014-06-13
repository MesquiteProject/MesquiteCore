/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
 Version 2.75, September 2011.
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
public abstract class MultipleSequenceAligner extends MesquiteModule {
	protected Class characterDataClass = null;
	protected boolean isProtein = true;

	public Class getDutyClass() {
		return MultipleSequenceAligner.class;
	}

	public String getDutyName() {
		return "Multiple Sequence Aligner";
	}
 	 public String[] getDefaultModule() {
    	 	return new String[] {"#OpalMultiSeqAligner"};
    	 }

	public boolean permitSeparateThread(){
		return true;
	}
	/**
	 * Subclasses can override this to have access to full information about
	 * data.  What are to be aligned are only the taxa indicated for the block (firstSite to lastSite) indicated.
	 * These sites, aligned, are to be returned as a long[][].  Thus, this array may be smaller in dimensions than the 
	 * MCategoricalDistribution passed.  The MCategoricalDistribution may be queried for states by
	 * long nucleotide = data.getState(iCharacter, iTaxon);
	 * Longs are used as bitsets for state sets (see mesquite.lib.CategoricalState).

	 *	 * Note that LIKE MOST PLACES IN MESQUITE, the long[][] returned here has characters first and taxa second;
	 * thus, it should be returned as a long[numChars][numTaxa]
	 * 
	 */
	public long[][] alignSequences(MCategoricalDistribution matrix, boolean[] taxaToAlign, int firstSite, int lastSite, int firstTaxon, int lastTaxon) {
		characterDataClass=matrix.getCharacterDataClass();
		isProtein = (characterDataClass==ProteinData.class);
		int numChars = lastSite - firstSite+1;
		if (numChars <0 || !MesquiteInteger.isCombinable(firstSite) || !MesquiteInteger.isCombinable(lastSite)){
			numChars = matrix.getNumChars();
			firstSite = 0;
			lastSite = numChars-1;
		}
		int numTaxa = 0;
		if (taxaToAlign == null)
			numTaxa = lastTaxon-firstTaxon+1;
		else {
			for (int i = 0; i < taxaToAlign.length; i++)
				if (taxaToAlign[i])
					numTaxa++;
			
		}
		if (numTaxa<=1)
			return null;
		
		long[][] extracted = new long[numChars][numTaxa];

		for (int ic = firstSite; ic<=lastSite; ic++){
			int st = 0;
			for (int it = 0; it<matrix.getNumTaxa(); it++){
				if ((taxaToAlign == null && it>=firstTaxon && it<=lastTaxon) || taxaToAlign[it]){
					extracted[ic-firstSite][st] = matrix.getState(ic, it);
					st++;
				}
			}
		}
		long[][] aligned =  alignSequences(extracted);
		return aligned;
	}

	/**
	 * This method is not called directly by Mesquite, but modules preferring to
	 * receive and return the information as long[][] can override this and let the previous
	 * method call it.  Also, this method receives only the pieces to be aligned.
	 */
	protected long[][] alignSequences(long[][] sequences) {
		return null;
	}

}


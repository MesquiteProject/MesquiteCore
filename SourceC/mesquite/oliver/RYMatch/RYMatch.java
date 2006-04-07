package mesquite.oliver.RYMatch;


import mesquite.categ.lib.DNAState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class RYMatch extends DataMatcher {
	DNAState csOriginal, csCandidate;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName){
		return true;
	}
   	/** Returns whether candidate stretch of matrix matches the data contained in the CharacterState array*/
   	public double sequenceMatch(CharacterState[] csOriginalArray, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar, CommandRecord commandRec){
   		double numberOfMatches = 0.0;
   		int basesCompared =0;
   		
   		for (int ic = 0; ic< csOriginalArray.length && ic+candidateStartChar < data.getNumChars(); ic++){
   			csOriginal = (DNAState)csOriginalArray[ic];
   			csCandidate = (DNAState) data.getCharacterState(csCandidate, ic+candidateStartChar, candidateTaxon);
   			if ((csOriginal.equals(csCandidate, false, true)) || (csOriginal.hasPurine() && csCandidate.hasPurine()) || (csOriginal.hasPyrimidine() && csCandidate.hasPyrimidine()))
   				numberOfMatches += 1.0;
  			basesCompared++;
   		}	   		
   		if (candidateEndChar!=null)
   			candidateEndChar.setValue(MesquiteInteger.minimum(candidateStartChar + csOriginalArray.length-1, data.getNumChars()-1));
   		if (basesCompared == 0)
   			return 0;
   		return numberOfMatches/basesCompared;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Simple Match, with purine/pyrimidine ambiguities allowed";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Returns whether sequences compared match at selected sites; purine matches (A or G) and pyrimidine matches (T or C) are counted as full matches.";
   	 }
	
	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return true;
   	 }
		public double getBestMatchValue(CharacterState[] csOriginalArray, CommandRecord commandRec) {
			return 0;
		}
		public double getApproximateWorstMatchValue(CharacterState[] csOriginalArray, CommandRecord commandRec) {
			return 0;
		}
		public double sequenceMatch(CharacterState[] csOriginalArray, CharacterState[] csCandidateArray, CommandRecord commandRec) {
			return 0;
		}

}


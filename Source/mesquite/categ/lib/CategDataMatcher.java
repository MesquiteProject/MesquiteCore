package mesquite.categ.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public abstract class CategDataMatcher extends DataMatcher {
	

	public double sequenceMatch(CharacterState[] csOriginalArray, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar, CommandRecord commandRec) {
		if (csOriginalArray==null || csOriginalArray.length<=0)
			return MesquiteDouble.unassigned;
		if (!(csOriginalArray[0] instanceof CategoricalState))
			return MesquiteDouble.unassigned;
		long[] longArray = new long[csOriginalArray.length];
		for (int i=0; i<longArray.length; i++) 
			longArray[i] = ((CategoricalState)csOriginalArray[i]).getValue();
		return sequenceMatch(longArray,candidateTaxon, candidateStartChar, candidateEndChar, commandRec);
	}

	/*.................................................................................................................*/
   	/** Returns the match of the two CharacterState arrays*/
	public double sequenceMatch(CharacterState[] csOriginalArray, CharacterState[] csCandidateArray, CommandRecord commandRec){
		if (csOriginalArray==null || csOriginalArray.length<=0)
			return MesquiteDouble.unassigned;
		if (csCandidateArray==null || csCandidateArray.length<=0)
			return MesquiteDouble.unassigned;
		if (!(csOriginalArray[0] instanceof CategoricalState) || !(csCandidateArray[0] instanceof CategoricalState))
			return MesquiteDouble.unassigned;
		long[] originalArray = new long[csOriginalArray.length];
		for (int i=0; i<originalArray.length; i++) 
			originalArray[i] = ((CategoricalState)csOriginalArray[i]).getValue();
		long[] candidateArray = new long[csCandidateArray.length];
		for (int i=0; i<candidateArray.length; i++) 
			candidateArray[i] = ((CategoricalState)csCandidateArray[i]).getValue();
		return sequenceMatch(originalArray,candidateArray, commandRec);
	}
	
	public double getBestMatchValue(CharacterState[] csOriginalArray, CommandRecord commandRec){
		if (csOriginalArray==null || csOriginalArray.length<=0)
			return MesquiteDouble.unassigned;
		if (!(csOriginalArray[0] instanceof CategoricalState))
			return MesquiteDouble.unassigned;
		long[] longArray = new long[csOriginalArray.length];
		for (int i=0; i<longArray.length; i++) 
			longArray[i] = ((CategoricalState)csOriginalArray[i]).getValue();
		return getBestMatchValue(longArray, commandRec);
	}

	public  double getApproximateWorstMatchValue(CharacterState[] csOriginalArray, CommandRecord commandRec) {
		if (csOriginalArray==null || csOriginalArray.length<=0)
			return MesquiteDouble.unassigned;
		if (!(csOriginalArray[0] instanceof CategoricalState))
			return MesquiteDouble.unassigned;
		long[] longArray = new long[csOriginalArray.length];
		for (int i=0; i<longArray.length; i++) 
			longArray[i] = ((CategoricalState)csOriginalArray[i]).getValue();
		return getApproximateWorstMatchValue(longArray, commandRec);
	}
	 
	
	public abstract double getBestMatchValue(long[] originalArray, CommandRecord commandRec) ;

	public abstract double getApproximateWorstMatchValue(long[] originalArray, CommandRecord commandRec) ;
	 	
	public abstract double sequenceMatch(long[] originalArray, long[] candidateArray, CommandRecord commandRec);

	public abstract double sequenceMatch(long[] originalArray, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar, CommandRecord commandRec);

	public CompatibilityTest getCompatibilityTest() {
		return new CategoricalStateTest();
	}

}

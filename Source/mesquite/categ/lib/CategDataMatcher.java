/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.categ.lib;

import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.DataMatcher;

public abstract class CategDataMatcher extends DataMatcher {
	

	public double sequenceMatch(CharacterState[] csOriginalArray, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar) {
		if (csOriginalArray==null || csOriginalArray.length<=0)
			return MesquiteDouble.unassigned;
		if (!(csOriginalArray[0] instanceof CategoricalState))
			return MesquiteDouble.unassigned;
		long[] longArray = new long[csOriginalArray.length];
		for (int i=0; i<longArray.length; i++) 
			longArray[i] = ((CategoricalState)csOriginalArray[i]).getValue();
		return sequenceMatch(longArray,candidateTaxon, candidateStartChar, candidateEndChar);
	}

	/*.................................................................................................................*/
   	/** Returns the match of the two CharacterState arrays*/
	public double sequenceMatch(CharacterState[] csOriginalArray, CharacterState[] csCandidateArray){
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
		return sequenceMatch(originalArray,candidateArray);
	}
	
	public double getBestMatchValue(CharacterState[] csOriginalArray){
		if (csOriginalArray==null || csOriginalArray.length<=0)
			return MesquiteDouble.unassigned;
		if (!(csOriginalArray[0] instanceof CategoricalState))
			return MesquiteDouble.unassigned;
		long[] longArray = new long[csOriginalArray.length];
		for (int i=0; i<longArray.length; i++) 
			longArray[i] = ((CategoricalState)csOriginalArray[i]).getValue();
		return getBestMatchValue(longArray);
	}

	public  double getApproximateWorstMatchValue(CharacterState[] csOriginalArray) {
		if (csOriginalArray==null || csOriginalArray.length<=0)
			return MesquiteDouble.unassigned;
		if (!(csOriginalArray[0] instanceof CategoricalState))
			return MesquiteDouble.unassigned;
		long[] longArray = new long[csOriginalArray.length];
		for (int i=0; i<longArray.length; i++) 
			longArray[i] = ((CategoricalState)csOriginalArray[i]).getValue();
		return getApproximateWorstMatchValue(longArray);
	}
	 
	
	public abstract double getBestMatchValue(long[] originalArray) ;

	public abstract double getApproximateWorstMatchValue(long[] originalArray) ;
	 	
	public abstract double sequenceMatch(long[] originalArray, long[] candidateArray);

	public abstract double sequenceMatch(long[] originalArray, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar);

	public CompatibilityTest getCompatibilityTest() {
		return new RequiresAnyCategoricalData();
	}

}

/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.
Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.lib; 


import java.util.Enumeration;

import mesquite.align.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.MesquiteTable;
import mesquite.categ.lib.*;


/* ======================================================================== */
public abstract class AlignMatch extends CategDataMatcher {
	 boolean preferencesSet = false;
	PairwiseAligner aligner;
	long originalState, candidateState;
	int alphabetLength;
	int maxLengthDiff = 2;
	MesquiteCommand ptC;
	protected MesquiteSubmenuSpec mss;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){

		mss = addSubmenu(null, getName());//getFileCoordinator().
  		addItemToSubmenu(null, mss,"Allowed Length Differences...", makeCommand("setMaxLengthDiff", this));
		return true;
	}
	/*.................................................................................................................*/
 	/** Override if one wishes to modify the alignment costs away from the default. */
 	 	public int[][] modifyAlignmentCosts(int[][] defaultSubs) {
	 		return defaultSubs;
	}
	/*.................................................................................................................*/
	protected void initAligner() {
  		MesquiteInteger gapOpen = new MesquiteInteger();
   		MesquiteInteger gapExtend = new MesquiteInteger();
 		MesquiteInteger gapOpenTerminal = new MesquiteInteger();
   		MesquiteInteger gapExtendTerminal = new MesquiteInteger();
  		alphabetLength = ((CategoricalState)state).getMaxPossibleState()+1;
 		AlignUtil.getDefaultGapCosts(gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal);  
  		int subs[][] = AlignUtil.getDefaultSubstitutionCosts(alphabetLength);  
  		subs = modifyAlignmentCosts(subs);
   		aligner = new PairwiseAligner(false,subs,gapOpen.getValue(), gapExtend.getValue(), gapOpenTerminal.getValue(), gapExtendTerminal.getValue(), alphabetLength);
	}
	/*.................................................................................................................*/
	public void setTableAndData( MesquiteTable table, CharacterData data) {
		super.setTableAndData(table,data);
		if (state!=null)
			initAligner();
	}
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	Snapshot temp = new Snapshot();
	 	temp.addLine("setMaxLengthDiff " + maxLengthDiff);
	 	return temp;
 	 }
 
	/*.................................................................................................................*/
   	/** Returns whether or not better matches have higher values or not.*/
	public boolean getHigherIsBetter() {
		if (aligner==null)
			return false;
		return aligner.getHigherIsBetter();
	}   	
	
	/*.................................................................................................................*/
   	/** Returns the match of the two long arrays*/
	public double sequenceMatch(long[] originalArray, long[] candidateArray){
 		if (candidateArray==null || aligner ==null || originalArray==null)
   			return MesquiteDouble.unassigned;
  		MesquiteNumber score = new MesquiteNumber();
  		//CategoricalState state = ((CategoricalData)data).getNewState();
  		aligner.alignSequences(originalArray,candidateArray,false, score);
   		return score.getDoubleValue();
	}

	
	public double getBestMatchValue(long[] originalArray){
		if (aligner ==null || originalArray==null)
   			return MesquiteDouble.unassigned;
  		MesquiteNumber score = aligner.getScoreOfIdenticalSequences(originalArray);
   		return score.getDoubleValue();
	}
	
	public double getApproximateWorstMatchValue(long[] originalArray){
		if (aligner ==null || originalArray==null)
   			return MesquiteDouble.unassigned;
 		//CategoricalState state = ((CategoricalData)data).getNewState();
		MesquiteNumber score;
		double worstScore = MesquiteDouble.unassigned;
		int startLength = MesquiteInteger.maximum(1,originalArray.length-maxLengthDiff);
   		int endLength = MesquiteInteger.minimum(data.getNumChars(),originalArray.length+maxLengthDiff);
   		for (int length = startLength; length<=endLength; length++) {
   	  		score = aligner.getVeryBadScore(originalArray, length, alphabetLength);

   	  		if (!MesquiteDouble.isCombinable(worstScore))
   	  			worstScore = score.getDoubleValue();
	  		if ((aligner.getHigherIsBetter() && (worstScore>score.getDoubleValue())) || (!aligner.getHigherIsBetter() && (worstScore<score.getDoubleValue()))) {
	  			worstScore = score.getDoubleValue();
	  		}
  		}
   		
    		return worstScore;
	}

		
		/** Returns whether candidate stretch of matrix matches the data contained in the long array*/
   	public double sequenceMatch(long[] originalArray, int candidateTaxon, int candidateStartChar, MesquiteInteger candidateEndChar){
   		if (data==null || aligner ==null || originalArray==null)
   			return MesquiteDouble.unassigned;
  		
   		double numberOfMatches = 0.0;
   		int basesCompared =0;
   		
   		int startLength = MesquiteInteger.maximum(1,originalArray.length-maxLengthDiff);
   		int endLength = MesquiteInteger.minimum(data.getNumChars(),originalArray.length+maxLengthDiff);
   		MesquiteNumber score = new MesquiteNumber();
   		double bestScore = MesquiteDouble.unassigned;
   		int bestLength = originalArray.length;
   		for (int length = startLength; length<=endLength; length++) {
   	  		long[] candidateArray = ((CategoricalData)data).getLongArray(candidateStartChar,length,candidateTaxon, false);
   	  		candidateArray = getTransformedCandidateArray(candidateArray);
   	  		aligner.alignSequences(originalArray,candidateArray,false,score);
   	  		double newScore = 1.0*score.getIntValue();
   	  		if ((aligner.getHigherIsBetter() && (newScore>bestScore)) || (!aligner.getHigherIsBetter() && (newScore<bestScore))) {
   	  			bestScore = newScore;
   	  			bestLength = length;
   	  		}
   	  	}
   		

   		if (candidateEndChar!=null)
   			candidateEndChar.setValue(MesquiteInteger.minimum(candidateStartChar + bestLength-1, data.getNumChars()-1));
   		return bestScore;
	}
   	/*.................................................................................................................*/
	 public abstract long[] 	getTransformedCandidateArray(long[] candidateArray);
 
	 
 		MesquiteInteger pos = new MesquiteInteger(0);
 		/*.................................................................................................................*/
 		public Object doCommand(String commandName, String arguments, CommandChecker checker) {
  			if (checker.compare(this.getClass(), "Sets the maximum number of length differences between the two sequences", "[number]", commandName, "setMaxLengthDiff")) {
 				int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
 				if (!MesquiteInteger.isCombinable(newNum))
 					newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set Allowed Length Differences", "Allowed Length Differences:", maxLengthDiff, 0, MesquiteInteger.infinite);
 				if (newNum>=0  && newNum!=maxLengthDiff) {
 					maxLengthDiff = newNum;
 					parametersChanged();
 				}
 			}
 			else
 				return  super.doCommand(commandName, arguments, checker);
 			return null;
 		}

}


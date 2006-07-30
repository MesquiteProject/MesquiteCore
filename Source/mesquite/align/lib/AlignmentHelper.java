package mesquite.align.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;

public abstract class AlignmentHelper {

	int[] A;
	int[] B;		
	
	int[][] subs;
	int gapOpen;
	int gapExtend;
	int gapOpenTerminal;
	int gapExtendTerminal;	
	int alphabetLength;
	boolean isMinimize = true;
	
	int lengthA, lengthB;
	int gapInsertionArray[];

	
	
	long[][] ReInsertGaps (int k, int[] followsGapSize, int totalGapChars, boolean seqsWereExchanged, long[][] inputSequence) {	
	
		int i,j;
		//put the gaps back in
		long gappedSeq2return[][] = new long[k+totalGapChars][2];
		gapInsertionArray = new int[k+totalGapChars];
		for(i=0; i<k+totalGapChars; i++) {
			gapInsertionArray[i] = 0;
		}
		
		int usedGaps=0;
		int recentGapRunLength=0;
		j=0; // counts the number of letters in A seen so far
		int retainedGapsSeen = 0;
		for (i=0; i<k; i++) {
			if(inputSequence[i][0] == CategoricalState.inapplicable) {
				recentGapRunLength++;
				gapInsertionArray[j+retainedGapsSeen] = Math.max(0, recentGapRunLength - followsGapSize[j] );
				if (followsGapSize[j] > recentGapRunLength) { 	
					retainedGapsSeen++;
				}
			} else {
				for (int m=0 ; m < followsGapSize[j]-recentGapRunLength; m++){
					gappedSeq2return[i+usedGaps][0] =  CategoricalState.inapplicable; 
					gappedSeq2return[i+usedGaps][1] =  CategoricalState.inapplicable; 
					usedGaps++;
					retainedGapsSeen++;
				}
				j++;
				recentGapRunLength=0;
			}
			gappedSeq2return[i+usedGaps][0] = inputSequence[i][0] ;
			gappedSeq2return[i+usedGaps][1] = inputSequence[i][1] ;									
		}		
		return gappedSeq2return;	
	}

	
	
	public int[] getGapInsertionArray () {
		return gapInsertionArray;
	}			
		
	
	
}

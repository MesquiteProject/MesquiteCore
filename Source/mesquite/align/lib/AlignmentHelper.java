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
		int numPosnsToShift=0; // used at most, in the event of a terminal original gap in A that's longer than the one in the alignmnt of A and B  
		for (i=0; i<k; i++) {
			if(inputSequence[i][0] == CategoricalState.inapplicable) {
				recentGapRunLength++;
				if (j+retainedGapsSeen < gapInsertionArray.length && j < followsGapSize.length) //Wayne's workaround to crash
				gapInsertionArray[j+retainedGapsSeen] = Math.max(0, recentGapRunLength - followsGapSize[j] );
				if (j< followsGapSize.length && followsGapSize[j] > recentGapRunLength) { 	
					retainedGapsSeen++;
				}
			} else {

				if ( i == retainedGapsSeen && followsGapSize[j]>recentGapRunLength ){
					numPosnsToShift = retainedGapsSeen; // used at most once, to make the left side of the alignment look right 
														// in the event of a terminal original gap in A that's longer than the one in the alignmnt of A and B
				}
				for (int m=0 ; m < followsGapSize[j]-recentGapRunLength; m++){
					gappedSeq2return[i+usedGaps][0] =  CategoricalState.inapplicable; 
					gappedSeq2return[i+usedGaps][1] =  CategoricalState.inapplicable; 
					usedGaps++;
					retainedGapsSeen++;
				}
				if (numPosnsToShift>0) {//will only happen at most once, in special terminal gap case
					int c; 
					long tmp[] = new long[numPosnsToShift];
					for (c=0; c<numPosnsToShift; c++){
						tmp[c] = gappedSeq2return[c][1];
						gappedSeq2return[c][1] = CategoricalState.inapplicable;
					}
					for (c=0; c<numPosnsToShift; c++){
						gappedSeq2return[retainedGapsSeen-numPosnsToShift+c][1] = tmp[c];				
					}
					numPosnsToShift = 0;
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

/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.72, December 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.align.lib;

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
		boolean onLeftEnd = true;
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
				if ( onLeftEnd && i == retainedGapsSeen && followsGapSize[j]>recentGapRunLength ){
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
				onLeftEnd = false;
				j++;
				recentGapRunLength=0;
			}
			gappedSeq2return[i+usedGaps][0] = inputSequence[i][0] ;
			gappedSeq2return[i+usedGaps][1] = inputSequence[i][1] ;
		}		
/*		
		long gap_seqA[] = new long[gappedSeq2return.length];
		long gap_seqB[] = new long[gappedSeq2return.length];
		
		for (i=0; i<gappedSeq2return.length; i++) {
			gap_seqA[i] = gappedSeq2return[i][0];
			gap_seqB[i] = gappedSeq2return[i][1];
		}		
		long input_seqA[] = new long[inputSequence.length];
		long input_seqB[] = new long[inputSequence.length];
		
		for (i=0; i<inputSequence.length; i++) {
			input_seqA[i] = inputSequence[i][0];
			input_seqB[i] = inputSequence[i][0];
		}		
	
		*/
		
		return gappedSeq2return;	
	}

	
	
	public int[] getGapInsertionArray () {
		return gapInsertionArray;
	}			
		
	
	
}

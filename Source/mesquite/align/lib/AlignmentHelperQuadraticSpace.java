package mesquite.align.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.*;

public class AlignmentHelperQuadraticSpace extends AlignmentHelper {	
	
/*	int[] A;
	int[] B;
	int lengthA;
	int lengthB;
	
	int[][] subs;
	int gapOpen;
	int gapExtend;
	int alphabetLength;
	int gapInsertionArray[];
	
	boolean isMinimize = true;
*/	
	public AlignmentHelperQuadraticSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int alphabetLength) {
		A = seq1;
		B = seq2;
		this.lengthA = lengthA;
		this.lengthB = lengthB;
		this.subs = subs;
		this.gapOpen = gapOpen;
		this.gapExtend = gapExtend;
		this.alphabetLength=alphabetLength;
	}

	public long[][] doAlignment (boolean returnAlignment, MesquiteNumber score, boolean keepGaps, int[] followsGapSize, int totalGapChars) {
		//Height = lengthA, Width = lengthB
		
		int H[][] = new int[lengthA+1][lengthB+1];
		int D[][] = new int[lengthA+1][lengthB+1];
		int V[][] = new int[lengthA+1][lengthB+1];

		int i,j;		
		
		for (i=1; i<=lengthA; i++) {
			V[i][0] = D[i][0] = gapOpen + gapExtend*i;
			H[i][0] = 2*gapOpen +  gapExtend*i;
		}
		for (j=1; j<=lengthB; j++) {
			D[0][j] = H[0][j] = gapOpen + gapExtend*j;
			V[0][j] = 2*gapOpen +  gapExtend*j;
		}
		
		int gapOpenOnA;
		for (i=1; i<=lengthA; i++) {
			gapOpenOnA = gapOpen;
			if (keepGaps && followsGapSize[i]>0)
				gapOpenOnA = 0;

			for (j=1; j<=lengthB; j++) {
//				look at three preceding values.				
				
				if (isMinimize) {
					V[i][j] = Math.min(  V[i-1][j] + gapExtend,  
								Math.min ( D[i-1][j] + gapOpen + gapExtend,
												 H[i-1][j] + gapOpen + gapExtend));
	
					H[i][j] = Math.min(  V[i][j-1] + gapOpenOnA + gapExtend,  
								Math.min ( D[i][j-1] + gapOpenOnA + gapExtend ,
												 H[i][j-1] + gapExtend));

					D[i][j] = AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength)  +  Math.min(  V[i-1][j-1] , Math.min ( D[i-1][j-1] , H[i-1][j-1] ));
				} else { //maximize
					V[i][j] = Math.max(  V[i-1][j] + gapExtend,  
								Math.max( D[i-1][j] + gapOpen + gapExtend,
												H[i-1][j] + gapOpen + gapExtend));
				
					H[i][j] = Math.max(  V[i][j-1] + gapOpenOnA + gapExtend,  
								Math.max ( D[i][j-1] + gapOpenOnA + gapExtend ,
												 H[i][j-1] + gapExtend));					
	
					D[i][j] = AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength) +  Math.max(  V[i-1][j-1] , Math.max( D[i-1][j-1] , H[i-1][j-1] ));
				}
			}
		}
		
		//Debugg.println ("Final scores are: H=" + H[i-1][j-1] + ", D=" + D[i-1][j-1] + ", V=" + V[i-1][j-1]);
		i--;
		j--;
		int myScore ;
		if (isMinimize) {
			myScore = Math.min ( H[i][j], Math.min (D[i][j] ,V[i][j] )) ;
		} else {
			myScore = Math.max ( H[i][j], Math.max (D[i][j] ,V[i][j] )) ;
		}
		
		if (score != null)
			score.setValue( myScore );

		if ( !returnAlignment) { //save space
			return null;
		}
		
		//Trace back for sequence:
		long backtrack[][] = new long[lengthA+lengthB][2];
		int k=0;
		i = lengthA;
		j = lengthB;
		int a_cnt = 0; //count of letters from A used in path so far 
		while (i>0 || j>0 ) {
			if (i ==0) { 
				while (j>0){
					backtrack[k][0] = CategoricalState.inapplicable;
					backtrack[k][1] = CategoricalState.makeSetFromLowerBits(B[j-1]);
					j--;
					k++;
				}
			} else if ( j == 0 ) {
				while (i>0){
					backtrack[k][0] = CategoricalState.makeSetFromLowerBits(A[i-1]);
					backtrack[k][1] = CategoricalState.inapplicable;
					i--;
					k++;
					a_cnt++;
				}				
			} else if  ( V[i][j] == myScore) { //an optimal path came from vertical (letter from A with gap in B)
			
				backtrack[k][0] = CategoricalState.makeSetFromLowerBits(A[i-1]);
				backtrack[k][1] = CategoricalState.inapplicable;
				if (i>0 &&  V[i][j] == V[i-1][j] + gapExtend){
					myScore -= gapExtend;
				} else { //V[i][j]  == D[i-1][j] + gapOpen + gapExtend  or V[i][j] == H[i-1][j] + gapOpen + gapExtend
					myScore -= gapOpen + gapExtend;
				}
				i--;
				k++;
				a_cnt++;
			} else if (H[i][j] == myScore) { //an optimal path came from horizontal (letter from B with gap in A)
				gapOpenOnA = gapOpen;
				if (keepGaps && i>0 && followsGapSize[i]>0)
					gapOpenOnA = 0;
				
				backtrack[k][0] = CategoricalState.inapplicable;
				backtrack[k][1] = CategoricalState.makeSetFromLowerBits(B[j-1]);
				if ( j>0 && H[i][j] == H[i][j-1] + gapExtend){
					myScore -= gapExtend;
				} else { //H[i][j]  == D[i-1][j] + gapOpen + gapExtend  or H[i][j] == V[i-1][j] + gapOpen + gapExtend
						myScore -= gapOpenOnA + gapExtend;
				}
				j--;
				k++;
			} else if (D[i][j] == myScore) { // from diagonal
				backtrack[k][0] = CategoricalState.makeSetFromLowerBits(A[i-1]);
				backtrack[k][1] = CategoricalState.makeSetFromLowerBits(B[j-1]);
				myScore -= AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength) ;
				i--;
				j--;
				k++;
				a_cnt++;
			} else { 
				// error
				Debugg.println ("Error in recovering alignment");
				return null;
			}
		}
		
		//reverse the sequence, and trim off all the empty space at the end
		long seq2return[][] = new long[k][2];
		int left  = 0;          // index of leftmost element
		int right = k-1; // index of rightmost element
		  
		while (left <= right) {
		      seq2return[left][0]  = backtrack[right][0]; 
		      seq2return[right][0] = backtrack[left][0];

		      seq2return[left][1]  = backtrack[right][1]; 
		      seq2return[right][1] = backtrack[left][1];
		      
		      // move the bounds toward the center
		      left++;
		      right--;
		}
		
		
		if (keepGaps) {
			return ReInsertGaps(k, followsGapSize, totalGapChars, false, seq2return) ;
		} else {
			return seq2return;
		}
		
/*		if (!keepGaps) {
			return seq2return;
		} else {
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
				if(seq2return[i][0] == CategoricalState.inapplicable) {
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
				gappedSeq2return[i+usedGaps][0] = seq2return[i][0] ;
				gappedSeq2return[i+usedGaps][1] = seq2return[i][1] ;									
			}		
			return gappedSeq2return;
		}
*/		
	}
	
	
/*	public int[] getGapInsertionArray () {
		return gapInsertionArray;
	}		
*/
	
}


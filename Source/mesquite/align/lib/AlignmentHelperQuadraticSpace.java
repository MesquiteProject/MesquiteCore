package mesquite.align.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.MesquiteNumber;

public class AlignmentHelperQuadraticSpace {

	int[] A;
	int[] B;
	int lengthA;
	int lengthB;
	
	int[][] subs;
	int gapOpen;
	int gapExtend;
	int alphabetLength;
	boolean gapInsertionArray[];
	
	boolean isMinimize = true;
	
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
		int H[][] = new int[lengthA+1][lengthB+1];
		int D[][] = new int[lengthA+1][lengthB+1];
		int V[][] = new int[lengthA+1][lengthB+1];

		int i,j;		
		
		for (i=1; i<=lengthA; i++) {
			H[i][0] = D[i][0] = gapOpen + gapExtend*i;
			V[i][0] = 2*gapOpen +  gapExtend*i;
		}
		for (j=1; j<=lengthB; j++) {
			D[0][j] = V[0][j] = gapOpen + gapExtend*j;
			H[0][j] = 2*gapOpen +  gapExtend*j;
		}
		
		int gapOpenOnA;
		for (i=1; i<=lengthA; i++) {
			gapOpenOnA = gapOpen;
			if (keepGaps && followsGapSize[i-1]>0)
				gapOpenOnA = 0;

			for (j=1; j<=lengthB; j++) {
//				look at three preceding values.				

				if (isMinimize) {
					H[i][j] = Math.min(  H[i-1][j] + gapExtend,  
								Math.min ( D[i-1][j] + gapOpenOnA + gapExtend,
												 V[i-1][j] + gapOpenOnA + gapExtend));
	
					V[i][j] = Math.min(  H[i][j-1] + gapOpen + gapExtend,  
								Math.min ( D[i][j-1] + gapOpen + gapExtend ,
												 V[i][j-1] + gapExtend));

					D[i][j] = AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength)  +  Math.min(  H[i-1][j-1] , Math.min ( D[i-1][j-1] , V[i-1][j-1] ));
				} else { //maximize
					H[i][j] = Math.max(  H[i-1][j] + gapExtend,  
								Math.max( D[i-1][j] + gapOpenOnA + gapExtend,
												V[i-1][j] + gapOpenOnA + gapExtend));
				
					V[i][j] = Math.max(  H[i][j-1] + gapOpen + gapExtend,  
								Math.max ( D[i][j-1] + gapOpen + gapExtend ,
												 V[i][j-1] + gapExtend));					
	
					D[i][j] = AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength) +  Math.max(  H[i-1][j-1] , Math.max( D[i-1][j-1] , V[i-1][j-1] ));
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
		while (i>0 || j>0 ) {
			if (i ==0) { 
				while (j>0){
					backtrack[k][0] = CategoricalState.inapplicable;
					backtrack[k][1] = CategoricalState.makeSet(B[j-1]);
					j--;
					k++;
				}
			} else if ( j == 0 ) {
				while (i>0){
					backtrack[k][0] = CategoricalState.makeSet(A[i-1]);
					backtrack[k][1] = CategoricalState.inapplicable;
					i--;
					k++;
				}				
			} else if  ( H[i][j] == myScore) { //an optimal path came from horizontal

				gapOpenOnA = gapOpen;
				if (keepGaps && i>0 && followsGapSize[i-1]>0)
					gapOpenOnA = 0;
				
				backtrack[k][0] = CategoricalState.makeSet(A[i-1]);
				backtrack[k][1] = CategoricalState.inapplicable;
				if (i>0 &&  H[i][j] == H[i-1][j] + gapExtend){
					myScore -= gapExtend;
				} else { //H[i][j]  == D[i-1][j] + gapOpen + gapExtend  or H[i][j] == V[i-1][j] + gapOpen + gapExtend
					myScore -= gapOpenOnA + gapExtend;
				}
				i--;
				k++;
			} else if (V[i][j] == myScore) { //an optimal path came from vertical
				backtrack[k][0] = CategoricalState.inapplicable;
				backtrack[k][1] = CategoricalState.makeSet(B[j-1]);
				if ( j>0 && V[i][j] == V[i][j-1] + gapExtend){
					myScore -= gapExtend;
				} else { //V[i][j]  == D[i-1][j] + gapOpen + gapExtend  or V[i][j] == H[i-1][j] + gapOpen + gapExtend
					myScore -= gapOpen + gapExtend;
				}
				j--;
				k++;
			} else { // from diagonal
				backtrack[k][0] = CategoricalState.makeSet(A[i-1]);
				backtrack[k][1] = CategoricalState.makeSet(B[j-1]);
				myScore -= AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength) ;
				i--;
				j--;
				k++;
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
		
		if (!keepGaps) {
			return seq2return;
		} else {
			//put the gaps back in
			long gappedSeq2return[][] = new long[k+totalGapChars][2];
			gapInsertionArray = new boolean[k+totalGapChars];
			for(i=0; i<k+totalGapChars; i++) {
				gapInsertionArray[i] =false;
			}
			
			int usedGaps=0;
			int recentGapRunLength=0;
			j=0; // counts the number of letters in A seen so far
			for (i=0; i<k; i++) {
				if(seq2return[i][0] == CategoricalState.inapplicable) {
					recentGapRunLength++;
					gapInsertionArray[i+usedGaps]=true;
				} else {
					for (int m=0 ; m < followsGapSize[j]-recentGapRunLength; m++){
						gappedSeq2return[i+usedGaps][0] =  CategoricalState.inapplicable; 
						gappedSeq2return[i+usedGaps][1] =  CategoricalState.inapplicable; 
						usedGaps++;
					}
					j++;
					recentGapRunLength=0;
				}				
				gappedSeq2return[i+usedGaps][0] = seq2return[i][0] ;
				gappedSeq2return[i+usedGaps][1] = seq2return[i][1] ;									
			}		
			return gappedSeq2return;
		}
		
	}
	
	//for now, there's not a good way to get to this array; I'll add it when this interface gets worked out
	public boolean[] getGapInsertionArray () {
		return gapInsertionArray;
	}		
}


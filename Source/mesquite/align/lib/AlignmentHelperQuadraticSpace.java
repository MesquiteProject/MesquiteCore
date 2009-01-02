/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.align.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.*;

public class AlignmentHelperQuadraticSpace extends AlignmentHelper {	
	
	public AlignmentHelperQuadraticSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int alphabetLength) {
		this(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpen, gapExtend, alphabetLength);
	}

	public AlignmentHelperQuadraticSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal, int alphabetLength) {
		A = seq1;
		B = seq2;
		this.lengthA = lengthA;
		this.lengthB = lengthB;
		this.subs = subs;
		this.gapOpen = gapOpen;
		this.gapExtend = gapExtend;
		this.gapOpenTerminal = gapOpenTerminal;
		this.gapExtendTerminal = gapExtendTerminal;		
		this.alphabetLength=alphabetLength;
	}
	
	public long[][] doAlignment (boolean returnAlignment, MesquiteNumber score, boolean keepGaps, int[] followsGapSize, int totalGapChars) {
		//Height = lengthA, Width = lengthB
		
		int H[][] = new int[lengthA+1][lengthB+1];
		int D[][] = new int[lengthA+1][lengthB+1];
		int V[][] = new int[lengthA+1][lengthB+1];

		int i,j;		
		
		for (i=1; i<=lengthA; i++) {
			V[i][0] = D[i][0] = gapOpenTerminal + gapExtendTerminal*i;
			H[i][0] = gapOpen + gapOpenTerminal +  gapExtendTerminal*i;
		}
		for (j=1; j<=lengthB; j++) {
			D[0][j] = H[0][j] = gapOpenTerminal + gapExtendTerminal*j;
			V[0][j] = gapOpen + gapOpenTerminal +  gapExtendTerminal*j;
		}
		
		int gapOpenOnA, gapOpenOnB, gapExtendOnA, gapExtendOnB;
		for (i=1; i<=lengthA; i++) {
			gapOpenOnA =  (i==lengthA) ? gapOpenTerminal : gapOpen;
			gapExtendOnA = (i==lengthA) ? gapExtendTerminal : gapExtend ;			
			if (keepGaps && i<followsGapSize.length && followsGapSize[i]>0) {
				gapOpenOnA = 0;
			}

			for (j=1; j<=lengthB; j++) {
//				look at three preceding values.				

				gapOpenOnB =  (j==lengthB) ? gapOpenTerminal : gapOpen;
				gapExtendOnB = (j==lengthB) ? gapExtendTerminal : gapExtend ;
				
				if (isMinimize) {
					V[i][j] = Math.min(  V[i-1][j] + gapExtendOnB,  
								Math.min ( D[i-1][j] + gapOpenOnB + gapExtendOnB,
												 H[i-1][j] + gapOpenOnB + gapExtendOnB));
	
					H[i][j] = Math.min(  V[i][j-1] + gapOpenOnA + gapExtendOnA,  
								Math.min ( D[i][j-1] + gapOpenOnA + gapExtendOnA ,
												 H[i][j-1] + gapExtendOnA));

					D[i][j] = AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength)  +  Math.min(  V[i-1][j-1] , Math.min ( D[i-1][j-1] , H[i-1][j-1] ));
				} else { //maximize
					V[i][j] = Math.max(  V[i-1][j] + gapExtendOnB,  
								Math.max( D[i-1][j] + gapOpenOnB + gapExtendOnB,
												H[i-1][j] + gapOpenOnB + gapExtendOnB));
				
					H[i][j] = Math.max(  V[i][j-1] + gapOpenOnA + gapExtendOnA,  
								Math.max ( D[i][j-1] + gapOpenOnA + gapExtendOnA ,
												 H[i][j-1] + gapExtendOnA));					
	
					D[i][j] = AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength) +  Math.max(  V[i-1][j-1] , Math.max( D[i-1][j-1] , H[i-1][j-1] ));
				}
			}
		}
		
		//System.out.println ("Final scores are: H=" + H[i-1][j-1] + ", D=" + D[i-1][j-1] + ", V=" + V[i-1][j-1]);
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
					backtrack[k][1] = CategoricalState.expandFromInt(B[j-1]);
					j--;
					k++;
				}
			} else if ( j == 0 ) {
				while (i>0){
					backtrack[k][0] = CategoricalState.expandFromInt(A[i-1]);
					backtrack[k][1] = CategoricalState.inapplicable;
					i--;
					k++;
					a_cnt++;
				}				
			} else if  ( V[i][j] == myScore) { //an optimal path came from vertical (letter from A with gap in B)
				gapOpenOnB =  (j==lengthB) ? gapOpenTerminal : gapOpen;
				gapExtendOnB = (j==lengthB) ? gapExtendTerminal : gapExtend ;				
				
				backtrack[k][0] = CategoricalState.expandFromInt(A[i-1]);
				backtrack[k][1] = CategoricalState.inapplicable;
				if ( V[i][j] == V[i-1][j] + gapExtendOnB){
					myScore -= gapExtendOnB;
				} else { //V[i][j]  == D[i-1][j] + gapOpen + gapExtend  or V[i][j] == H[i-1][j] + gapOpen + gapExtend
					myScore -= gapOpenOnB + gapExtendOnB;
				}
				i--;
				k++;
				a_cnt++;
			} else if (H[i][j] == myScore) { //an optimal path came from horizontal (letter from B with gap in A)
				gapOpenOnA=  (i==lengthA) ? gapOpenTerminal : gapOpen;
				gapExtendOnA= (i==lengthA) ? gapExtendTerminal : gapExtend ;				
			
				if (keepGaps && i<followsGapSize.length && followsGapSize[i]>0)
					gapOpenOnA = 0;
				
				backtrack[k][0] = CategoricalState.inapplicable;
				backtrack[k][1] = CategoricalState.expandFromInt(B[j-1]);
				if ( H[i][j] == H[i][j-1] + gapExtendOnA){
					myScore -= gapExtendOnA;
				} else { //H[i][j]  == D[i-1][j] + gapOpen + gapExtend  or H[i][j] == V[i-1][j] + gapOpen + gapExtend
						myScore -= gapOpenOnA + gapExtendOnA;
				}
				j--;
				k++;
			} else if (D[i][j] == myScore) { // from diagonal
				backtrack[k][0] = CategoricalState.expandFromInt(A[i-1]);
				backtrack[k][1] = CategoricalState.expandFromInt(B[j-1]);
				myScore -= AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength) ;
				i--;
				j--;
				k++;
				a_cnt++;
			} else { 
				// error
				MesquiteMessage.println ("Error in recovering alignment");
				MesquiteMessage.println ("   myScore: " + myScore);
				MesquiteMessage.println ("   i = " + i + ", j = " + j);
				MesquiteMessage.println ("   D[i][j]: " + D[i][j]);
				MesquiteMessage.println ("   V[i][j]: " + V[i][j]);
				MesquiteMessage.println ("   H[i][j]: " + H[i][j]);
				MesquiteMessage.println ("   D[i][j]: " + D[i][j]);
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
		
	}
	
}


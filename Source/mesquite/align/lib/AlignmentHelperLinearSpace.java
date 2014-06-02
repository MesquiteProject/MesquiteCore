/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.align.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.Debugg;
import mesquite.lib.IntegerArray;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;

public class AlignmentHelperLinearSpace extends AlignmentHelper {

	
	boolean keepGaps = false;
	int[] followsGapSize;	
	
	/* enumerting the possible shapes (preceding and succeding) */
	public static int noGap = 0;
	public static int gapInA = 1;
	public static int gapInB = 2;
	
	public int bigNumber = MesquiteInteger.infinite/3; // can't use "infinite", because adding anything to it makes a negative number ... bad for minimization problems
	
	/* forward arrays*/
	public int fH[];
	public int fD[];
	public int fV[];
	
	/*reverse arrays*/
	public int rH[];
	public int rD[];
	public int rV[];	

	private int lastB_BeforeNextA[]; 
	private int shapeLeavingPosInA[];
	
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int alphabetLength) {
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpen, gapExtend, alphabetLength, false);
	}
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal, int alphabetLength) {
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal, alphabetLength, false);
	}
	
	
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int alphabetLength, boolean keepGaps, int[] followsGapSize) {
		this.keepGaps = keepGaps;
		this.followsGapSize = followsGapSize;
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpen, gapExtend, alphabetLength, false);
	}
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal, int alphabetLength, boolean keepGaps, int[] followsGapSize) {
		this.keepGaps = keepGaps;
		this.followsGapSize = followsGapSize;
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal, alphabetLength, false);
	}	
	
	
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int alphabetLength, boolean forwardArrayOnly) {
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpen, gapExtend, alphabetLength, forwardArrayOnly);
	}
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal, int alphabetLength, boolean forwardArrayOnly) {
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal, alphabetLength, forwardArrayOnly);
	}

	
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int alphabetLength, boolean forwardArrayOnly, boolean keepGaps, int[] followsGapSize) {
		this.keepGaps = keepGaps;
		this.followsGapSize = followsGapSize;
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpen, gapExtend, alphabetLength, forwardArrayOnly);
	}
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal, int alphabetLength, boolean forwardArrayOnly, boolean keepGaps, int[] followsGapSize) {
		this.keepGaps = keepGaps;
		this.followsGapSize = followsGapSize;
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal, alphabetLength, forwardArrayOnly);
	}	
	
	
	private void initialize (int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal, int alphabetLength, boolean forwardArrayOnly) {
		A = seq1;
		B = seq2;

		this.subs = subs;
		this.gapOpen = gapOpen;
		this.gapExtend = gapExtend;
		this.gapOpenTerminal = gapOpenTerminal;
		this.gapExtendTerminal = gapExtendTerminal;

		
		fH = new int[lengthB+1];
		fD = new int[lengthB+1];
		fV = new int[lengthB+1];
		rH = new int[lengthB+1];
		rD = new int[lengthB+1];
		rV = new int[lengthB+1];
		
		this.alphabetLength = alphabetLength;

		this.lengthA = lengthA;
		this.lengthB = lengthB;
		
		lastB_BeforeNextA = new int[lengthA +1];
		shapeLeavingPosInA  = new int[lengthA +1];
	}

	public void fillArrays (int firstRow, int lastRow, int firstColumn, int lastColumn, int precedingShape, int succeedingShape) {
		int middleRow = (firstRow+lastRow)/2;
		fillForward(firstRow, middleRow, firstColumn, lastColumn, precedingShape);
		fillReverse(middleRow+1,lastRow, firstColumn, lastColumn, succeedingShape);
	}
	
	
	public void fillForward(int firstRow, int lastRow, int firstColumn, int lastColumn, int shape) {
		
		int i,j;
		
		int gapExtendOnB, gapExtendOnA;
		int gapOpenOnB, gapOpenOnA;		

		fH[firstColumn] = fV[firstColumn] =   (0==firstColumn) ? gapOpenTerminal : gapOpen;
		fD[firstColumn] = 0;
		
		if (shape == gapInA) {
			fH[firstColumn] = 0;
		} else if ( shape == gapInB) {
			fV[firstColumn] = 0;
		}

		//fill row-by-row. First row is a special case
		gapExtendOnA = (0==firstRow || lengthA==firstRow) ? gapExtendTerminal : gapExtend ;
		for (j=firstColumn+1; j<=lastColumn; j++) {
			fD[j] = fV[j] = bigNumber;
			fH[j] = fH[j-1] + gapExtendOnA;
        }


		int tmp1H, tmp1D, tmp1V, tmp2H, tmp2D, tmp2V;
	//reuse the arrays, treating it as the next row in the DP table. Keep one temp variable for each array
		for (i=firstRow+1; i<=lastRow; i++) { // for each "row" in DP table
			tmp1H = fH[firstColumn];
			tmp1D = fD[firstColumn];
			tmp1V = fV[firstColumn];
		
			fD[firstColumn] = fH[firstColumn] = bigNumber;
			fV[firstColumn] += (0==firstColumn || lengthB==firstColumn) ? gapExtendTerminal : gapExtend ;

			gapOpenOnA =  (lengthA==i) ? gapOpenTerminal : gapOpen;
			gapExtendOnA = (lengthA==i) ? gapExtendTerminal : gapExtend ;
			//gapOpenOnA = gapOpen;
			if (keepGaps &&  i<followsGapSize.length && followsGapSize[i]>0)
				gapOpenOnA = 0;

			
			for (j=firstColumn+1; j<=lastColumn; j++) { // for each column
				tmp2H = fH[j];
				tmp2D = fD[j];
				tmp2V = fV[j];

				gapOpenOnB =  (lengthB==j) ? gapOpenTerminal : gapOpen;
				gapExtendOnB = (lengthB==j) ? gapExtendTerminal : gapExtend ;
				
				if (isMinimize) {
					fV[j] = Math.min(  fH[j] + gapOpenOnB + gapExtendOnB,  
							Math.min ( fD[j]  + gapOpenOnB + gapExtendOnB ,
									   fV[j] + gapExtendOnB));

					fH[j] = Math.min(  fH[j-1] + gapExtendOnA,  
							Math.min ( fD[j-1] + gapOpenOnA + gapExtendOnA,
					  				   fV[j-1] + gapOpenOnA + gapExtendOnA));
	
					fD[j] = AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength) +  Math.min(  tmp1H, Math.min ( tmp1D , tmp1V));
				} else { //maximize
					fV[j] = Math.max(  fH[j] + gapOpenOnB + gapExtendOnB,  
							Math.max ( fD[j] + gapOpenOnB + gapExtendOnB ,
									   fV[j] + gapExtendOnB));
					
					fH[j] = Math.max(  fH[j-1] + gapExtendOnA,  
							Math.max(  fD[j-1] + gapOpenOnA + gapExtendOnA,
									   fV[j-1] + gapOpenOnA + gapExtendOnA));
					
					fD[j] = AlignUtil.getCost(subs,A[i-1],B[j-1],alphabetLength) +  Math.max(  tmp1H , Math.max( tmp1D, tmp1V ));
				}

				tmp1H = tmp2H;
				tmp1D = tmp2D;
				tmp1V = tmp2V;
			}
		}
	}

	public void fillReverse(int firstRow, int lastRow, int firstColumn, int lastColumn, int shape) {

	    int i,j;
		int gapExtendOnA;
		int gapOpenOnA;	
		rH[lastColumn] = rV[lastColumn] = (lengthB==lastColumn) ? gapOpenTerminal : gapOpen;;
		rD[lastColumn] = 0;
		
		if (shape == gapInA || (keepGaps &&  lastRow<followsGapSize.length && followsGapSize[lastRow]>0)) {
			rH[lastColumn] = 0;
		} else if ( shape == gapInB) {
			rV[lastColumn] = 0;
		}
		
		//fill row-by-row. Last row is a special case 
		gapExtendOnA = (0==lastRow || lengthA==lastRow) ? gapExtendTerminal : gapExtend ;
		for (j=lastColumn-1; j>=firstColumn; j--) {
			rD[j] = rV[j] = bigNumber;
            rH[j] = rH[j+1] + gapExtendOnA;
		}
		
		int tmp1H, tmp1D, tmp1V, tmp2H, tmp2D, tmp2V;
		//reuse the arrays, treating it as the next row in the DP table. Keep one temp variable for each array
		for (i=lastRow-1; i>=firstRow; i--) { // for each "row" in DP table
			tmp1H = rH[lastColumn];
			tmp1D = rD[lastColumn];
			tmp1V = rV[lastColumn];

			rD[lastColumn] = rH[lastColumn] = bigNumber ;
			rV[lastColumn] +=  (0==lastColumn || lengthB==lastColumn) ? gapExtendTerminal : gapExtend ;
			
			gapOpenOnA =  (0==i) ? gapOpenTerminal : gapOpen;
			gapExtendOnA = (0==i) ? gapExtendTerminal : gapExtend ;

			if (keepGaps &&  i<followsGapSize.length && followsGapSize[i]>0) // followsGapSize[i] means reversePrecedsGapSize[i-1]
				gapOpenOnA = 0;
			
			for (j=lastColumn-1; j>=firstColumn; j--) { // for each column
				tmp2H = rH[j];
				tmp2D = rD[j];
				tmp2V = rV[j];

				
				if (isMinimize) {
					rV[j] = Math.min(  rH[j] + gapOpen + gapExtend,  
							Math.min ( rD[j]  + gapOpen + gapExtend ,
											 rV[j] + gapExtend));
					
					rH[j] = Math.min(  rH[j+1] + gapExtend,  
								Math.min ( rD[j+1] + gapOpenOnA + gapExtend,
												 rV[j+1] + gapOpenOnA + gapExtend));
	
					rD[j] = AlignUtil.getCost(subs,A[i],B[j],alphabetLength) +  Math.min(  tmp1H, Math.min ( tmp1D , tmp1V));		
				} else { //maximize
				
					rV[j] = Math.max(  rH[j] + gapOpen + gapExtend,  
								Math.max ( rD[j] + gapOpen + gapExtend ,
												 rV[j] + gapExtend));					

					rH[j] = Math.max(  rH[j+1] + gapExtend,  
							Math.max( rD[j+1] + gapOpenOnA + gapExtend,
											rV[j+1] + gapOpenOnA + gapExtend));

					rD[j] = AlignUtil.getCost(subs,A[i],B[j],alphabetLength)  +  Math.max(  tmp1H , Math.max( tmp1D, tmp1V ));
				}
				tmp1H = tmp2H;
				tmp1D = tmp2D;
				tmp1V = tmp2V;
			}
		}
	}

	/** Fills in portions of the full DP-table, then identifies which column (corresponding to a position in seqB) 
	 * an optimal alignment must pass through in the middle row (corresponding to the middle of seqA) of the full DP table.
	 * Fills shape array, which is used by recoverAlignment to find the actual alignment.  
	 * */
	public int recursivelyFillArray(int firstRow, int lastRow, int firstColumn, int lastColumn, int precedingShape, int succeedingShape) {

		int gapOpenOnB, gapExtendOnB;		
		
		//Height (of the 2d array)=lengthA, Width = lengthB
		
		fillArrays(firstRow, lastRow, firstColumn, lastColumn, precedingShape, succeedingShape );
		
		int midRow = (firstRow + lastRow)/2;  //in seqA

		int i, verticalColScore, diagonalColScore; 
		int bestColScore = bigNumber , bestCol = -1, bestColShape = noGap;
		for (i=firstColumn; i<=lastColumn; i++) {
			gapOpenOnB  =  (0==i || lengthB==i) ? gapOpenTerminal : gapOpen;
			gapExtendOnB = (0==i || lengthB==i) ? gapExtendTerminal : gapExtend ;
			// best of the ways of leaving the i,j cell of the full DP table with a vertical edge 
			verticalColScore = Math.min(fH[i] + rH[i] + gapExtendOnB + gapOpenOnB,
							Math.min(fH[i] + rD[i] + gapExtendOnB + gapOpenOnB,
							Math.min(fH[i] + rV[i] + gapExtendOnB,
							Math.min(fD[i] + rH[i] + gapExtendOnB + gapOpenOnB,
							Math.min(fD[i] + rD[i] + gapExtendOnB + gapOpenOnB,
							Math.min(fD[i] + rV[i] + gapExtendOnB,			
							Math.min(fV[i] + rH[i] + gapExtendOnB,
							Math.min(fV[i] + rD[i] + gapExtendOnB,
										fV[i] + rV[i] + gapExtendOnB - gapOpenOnB))))))));			

			// best of the ways of leaving the i,j cell of the full DP table with a diagonal edge
			if (i == lastColumn) { 
				diagonalColScore = verticalColScore + 1; // i.e. don't allow diagonal exit from i,j (make diagonalColScore > verticalColScore) 
					//why not leave with a diagonal? because "leaving" means "going to the first row of the reverse table" ... 
				    //  and there isn't a column called "lastColumn+1" to go to in the reverse table  
			} else {
				int a = A[midRow];
				int b = B[i];
				int s = AlignUtil.getCost(subs,a,b,alphabetLength);
				diagonalColScore = Math.min(fH[i], Math.min (fD[i], fV[i])) +
								   Math.min(rH[i+1], Math.min (rD[i+1], rV[i+1])) +
								   s;
			}
			// no need to track horizontal edges - those are already stored in the forward and reverse arrays
			if (verticalColScore < diagonalColScore) {
				if ( verticalColScore < bestColScore) {
					bestColScore = verticalColScore;
					bestCol = i;
					bestColShape = gapInB;
				}
			} else {
				if ( diagonalColScore < bestColScore) {
					bestColScore = diagonalColScore;
					bestCol = i;
					bestColShape = noGap;
				}				
			}
		}
		lastB_BeforeNextA[midRow] = bestCol;
		shapeLeavingPosInA[midRow] = bestColShape;
				

		//	Recurse to find the full list of cells through which the alignment passes.
		if ( firstRow != midRow){
			recursivelyFillArray(firstRow, midRow, firstColumn, bestCol, precedingShape, bestColShape);
		}
		if (midRow+1 != lastRow){
			int col_shift = (bestColShape == noGap) ? /*diagonal*/ 1 : /*vertical*/ 0;
			recursivelyFillArray(midRow+1, lastRow, bestCol+col_shift, lastColumn, bestColShape, succeedingShape); 
		}
		
		return bestColScore; // useful for the first level of recursion - returns the total alignment cost
	}
	
	/**  Based on the shape array filled out by the recursive call above, determine the alignment 
	 * */
	public long[][] recoverAlignment (int totalGapChars, boolean seqsWereExchanged) {
		int i=0, j=0, k=0;
		
		long[][] alignment = new long[lengthA + lengthB][2];
		for (i = 0; i<lengthA; i++) {

			//gap in A
			while ( j < lastB_BeforeNextA[i] ) {
				alignment[k][0] = CategoricalState.inapplicable;
				alignment[k][1] = CategoricalState.expandFromInt(B[j]);
				j++;
				k++;					
			}

			//now we're ready to burn off a letter from B, and possibly a letter from A if diagonal.
			alignment[k][0] = CategoricalState.expandFromInt(A[i]);
			if (shapeLeavingPosInA[i] == noGap) {
				alignment[k][1] = CategoricalState.expandFromInt(B[j]);
				j++;
			} else {		// gap In B								
				alignment[k][1] = CategoricalState.inapplicable;
			}
			k++;
		}

		while (j < lengthB) { //gap in A at the end 
			alignment[k][0] = CategoricalState.inapplicable;
			alignment[k][1] = CategoricalState.expandFromInt(B[j]);
			j++;
			k++;					
		}		
		
		
		//trim off all the empty space at the end
		long seq2return[][] = new long[k][2];
		int ii=0; 		
		for (i=0; i<k; i++) {
			if (seqsWereExchanged) {//exhange the sequences
				seq2return[i][0] = alignment[i][1];
				seq2return[i][1] = alignment[i][0];				
			} else {
				seq2return[i][0] = alignment[i][0];
				seq2return[i][1] = alignment[i][1];
			}
		}
				
		

		if (keepGaps) {
			return ReInsertGaps(k, followsGapSize, totalGapChars, seqsWereExchanged, seq2return) ;
			
		}  else {
			return seq2return;
		}
	}
	
}



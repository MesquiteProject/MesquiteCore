package mesquite.align.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;

public class AlignmentHelperLinearSpace {

	int[] A;
	int[] B;		
	
	int[][] subs;
	int gapOpen;
	int gapExtend;
	boolean keepGaps = false;
	int[] followsGapSize;	
	boolean isMinimize = true;
	
	/* enumerting the possible shapes (preceding and succeding) */
	public static int noGap = 0;
	public static int gapInA = 1;
	public static int gapInB = 2;
	
	/* forward arrays*/
	public int fH[];
	public int fD[];
	public int fV[];
	
	/*reverse arrays*/
	public int rH[];
	public int rD[];
	public int rV[];	

	public int bigNumber = MesquiteInteger.infinite/3; // can't use "infitine", because adding anything to it makes a negative number ... bad for minimization problems
	
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend) {
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, false);
	}

	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, boolean keepGaps, int[] followsGapSize) {
		this.keepGaps = keepGaps;
		this.followsGapSize = followsGapSize;
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, false);
	}

	
	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, boolean forwardArrayOnly) {
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, forwardArrayOnly);
	}

	public AlignmentHelperLinearSpace(int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, boolean forwardArrayOnly, boolean keepGaps, int[] followsGapSize) {
		this.keepGaps = keepGaps;
		this.followsGapSize = followsGapSize;
		initialize(seq1, seq2, lengthA, lengthB, subs, gapOpen, gapExtend, forwardArrayOnly);
	}
	
	
	private void initialize (int[] seq1, int[] seq2, int lengthA, int lengthB, int[][] subs, int gapOpen, int gapExtend, boolean forwardArrayOnly) {
		A = seq1;
		B = seq2;

		this.subs = subs;
		this.gapOpen = gapOpen;
		this.gapExtend = gapExtend;

		fH = new int[lengthA+1];
		fD = new int[lengthA+1];
		fV = new int[lengthA+1];
		rH = new int[lengthA+1];
		rD = new int[lengthA+1];
		rV = new int[lengthA+1];

	}

	public void fillArrays (int firstRow, int lastRow, int firstColumn, int lastColumn, int precedingShape, int succeedingShape) {
		int middleRow = (firstRow+lastRow)/2;
		fillForward(firstRow, middleRow, firstColumn, lastColumn, precedingShape);
		fillReverse(middleRow+1,lastRow, firstColumn, lastColumn, succeedingShape);
	}
	
	
	public void fillForward(int firstRow, int lastRow, int firstColumn, int lastColumn, int shape) {
		int lengthA = lastColumn - firstColumn +1;
		int lengthB = lastRow - firstRow +1;
		int i;
		
		
		fH[firstColumn] = fV[firstColumn] = gapOpen;
		fD[firstColumn] = 0;
		
		if (shape == gapInA) {
			fV[firstColumn] = 0;
		} else if ( shape == gapInB) {
			fH[firstColumn] = 0;
		}
		
		//fill row-by-row. First row is a special case 
		
		for (i=firstColumn+1; i<=lastColumn; i++) {
			fD[i] = fV[i] = bigNumber;
			if (shape == gapInB) {
				fH[i] = gapExtend*(i-firstColumn);
			} else { //gapIna or noGap
				fH[i] = gapOpen + gapExtend*(i-firstColumn);
			}			
		}
		
		int gapOpenOnA;
		int j;
		int tmp1H, tmp1D, tmp1V, tmp2H, tmp2D, tmp2V;
	//reuse the arrays, treating it as the next row in the DP table. Keep one temp variable for each array
		for (j=firstRow+1; j<=lastRow; j++) { // for each "row" in DP table
			tmp1H = fH[firstColumn];
			tmp1D = fD[firstColumn];
			tmp1V = fV[firstColumn];

			fD[firstColumn] = fH[firstColumn] = bigNumber;
			if (shape == gapInA) {
				fV[firstColumn] = gapExtend*(j-firstRow);
			} else { //gapInB or noGap
				fV[firstColumn] = gapOpen + gapExtend*(j-firstRow);
			}
				
			for (i=firstColumn+1; i<=lastColumn; i++) { // for each column
				tmp2H = fH[i];
				tmp2D = fD[i];
				tmp2V = fV[i];

				gapOpenOnA = gapOpen;
				if (keepGaps && followsGapSize[i-1]>0)
					gapOpenOnA = 0;

				if (isMinimize) {
					fV[i] = Math.min(  fH[i] + gapOpen + gapExtend,  
							Math.min ( fD[i]  + gapOpen + gapExtend ,
											 fV[i] + gapExtend));

					fH[i] = Math.min(  fH[i-1] + gapExtend,  
								Math.min ( fD[i-1] + gapOpenOnA + gapExtend,
												 fV[i-1] + gapOpenOnA + gapExtend));
	
					fD[i] = subs[A[i-1]][B[j-1]] +  Math.min(  tmp1H, Math.min ( tmp1D , tmp1V));
				} else { //maximize
					fV[i] = Math.max(  fH[i] + gapOpen + gapExtend,  
							Math.max ( fD[i] + gapOpen + gapExtend ,
											 fV[i] + gapExtend));					
					
					fH[i] = Math.max(  fH[i-1] + gapExtend,  
								Math.max( fD[i-1] + gapOpenOnA + gapExtend,
												fV[i-1] + gapOpenOnA + gapExtend));
					
					fD[i] = subs[A[i-1]][B[j-1]] +  Math.max(  tmp1H , Math.max( tmp1D, tmp1V ));
				}
				tmp1H = tmp2H;
				tmp1D = tmp2D;
				tmp1V = tmp2V;
			}
		}
		
		
		
	}
	public void fillReverse(int firstRow, int lastRow, int firstColumn, int lastColumn, int shape) {
		int lengthA = lastColumn - firstColumn +1;
		int lengthB = lastRow - firstRow +1;
		int i;
		
		rH[lastColumn] = rV[lastColumn] = gapOpen;
		rD[lastColumn] = 0;
		
		if (shape == gapInA) {
			rV[lastColumn] = 0;
		} else if ( shape == gapInB) {
			rH[lastColumn] = 0;
		}
		
		//fill row-by-row. First row is a special case 
		
		for (i=lastColumn-1; i>=firstColumn; i--) {
			rD[i] = rV[i] = bigNumber;
			if (shape == gapInB) {
				rH[i] = gapExtend*(lastColumn-i);
			} else { //gapInA or noGap
				rH[i] = gapOpen + gapExtend*(lastColumn-i);
			}
		}
		
		int gapOpenOnA;
		int j;
		int tmp1H, tmp1D, tmp1V, tmp2H, tmp2D, tmp2V;
	//reuse the arrays, treating it as the next row in the DP table. Keep one temp variable for each array
		for (j=lastRow-1; j>=firstRow; j--) { // for each "row" in DP table
			tmp1H = rH[lastColumn];
			tmp1D = rD[lastColumn];
			tmp1V = rV[lastColumn];
			
			rD[lastColumn] = rH[lastColumn] = bigNumber;
			if (shape == gapInA) {
				rV[lastColumn] = gapExtend*(lastRow - j);
			} else { //gapInB or noGap
				rV[lastColumn] = gapOpen + gapExtend*(lastRow - j );
			}
			
			
			for (i=lastColumn-1; i>=firstColumn; i--) { // for each column
				tmp2H = rH[i];
				tmp2D = rD[i];
				tmp2V = rV[i];

				gapOpenOnA = gapOpen; 
				
				if (keepGaps && i>=firstColumn+2 && followsGapSize[i]>0) // followsGapSize[i] means reversePrecedsGapSize[i-1]
					gapOpenOnA = 0;

				if (isMinimize) {
					rV[i] = Math.min(  rH[i] + gapOpen + gapExtend,  
							Math.min ( rD[i]  + gapOpen + gapExtend ,
											 rV[i] + gapExtend));
					
					rH[i] = Math.min(  rH[i+1] + gapExtend,  
								Math.min ( rD[i+1] + gapOpenOnA + gapExtend,
												 rV[i+1] + gapOpenOnA + gapExtend));
	
					rD[i] = subs[A[i]][B[j]] +  Math.min(  tmp1H, Math.min ( tmp1D , tmp1V));
				} else { //maximize
				
					rV[i] = Math.max(  rH[i] + gapOpen + gapExtend,  
								Math.max ( rD[i] + gapOpen + gapExtend ,
												 rV[i] + gapExtend));					

					rH[i] = Math.max(  rH[i+1] + gapExtend,  
							Math.max( rD[i+1] + gapOpenOnA + gapExtend,
											rV[i+1] + gapOpenOnA + gapExtend));

					rD[i] = subs[A[i]][B[j]] +  Math.max(  tmp1H , Math.max( tmp1D, tmp1V ));
				}
				tmp1H = tmp2H;
				tmp1D = tmp2D;
				tmp1V = tmp2V;
			}
		}
	}

}


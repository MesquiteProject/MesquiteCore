package mesquite.align.lib;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteTrunk;

public class PairwiseAligner  {

	boolean preferencesSet = false;
	boolean isMinimize = true;	

    //first gap char costs gapOpen + gapExtend, and each additional character costs gapExtend 
	private int gapOpen;
	private int gapExtend;
	//private int alphabetSize;
	private int subs[][];
	private boolean keepGaps = false;
	private boolean useLowMem = false;
	
	public int totalGapChars = 0;
		
	public int A[];
	public int B[];
	public int followsGapSize[] = null; // unused if "keepGaps" is false
	public int lengthA;
	public int lengthB;
	
	private int lastAWhenBAligned[]; //lowMem alignment only
	private int shapeWhenBAligned[];//lowMem alignment only
	private boolean gapInsertionArray[];//lowMem alignment only
	
	private boolean gapCostsInitialized = false;
	private boolean subCostsInitialized = false;
		
	
	public boolean seqsWereExchanged=false;
	
	public PairwiseAligner (boolean keepGaps, int[][] subs, int gapOpen, int gapExtend) {
		setSubsCostMatrix(subs);
		setGapCosts(gapOpen, gapExtend);
		setKeepGaps (keepGaps);
		gapCostsInitialized = subCostsInitialized = true;
	}	
	
	public PairwiseAligner () {
		// not much to do here ???
	}

	public long[][] alignSequences( long[] A_withGaps, long[] B_withGaps, boolean returnAlignment, MesquiteNumber score) {
		
		if (!gapCostsInitialized  || !subCostsInitialized) {
			//announce an error?
			score.setValue( -1 );
			return null;
		}
		
		totalGapChars = preProcess(A_withGaps, B_withGaps);
		
		if ( returnAlignment) { 
			if (useLowMem) {
				//low memory (but slower, due to recursion) alignment
				AlignmentHelperLinearSpace helper = new AlignmentHelperLinearSpace(A, B, lengthA, lengthB, subs, gapOpen, gapExtend);
				lastAWhenBAligned = new int[lengthB +1];
				shapeWhenBAligned  = new int[lengthB +1];
				int myScore = recursivelyFillArray(helper, 0, lengthA, 0, lengthB, helper.noGap, helper.noGap);
				
				MesquiteTrunk.mesquiteTrunk.logln("score is " + myScore);
				
				return recoverAlignment(helper);
			} else {
//				 fast (but quadratic space) alignment
				AlignmentHelperQuadraticSpace helper = new AlignmentHelperQuadraticSpace(A, B, lengthA, lengthB, subs, gapOpen, gapExtend);
				long ret[][] = helper.doAlignment(returnAlignment,score,keepGaps, followsGapSize, totalGapChars);
				MesquiteTrunk.mesquiteTrunk.logln("score is " + score);
				return ret;
			}
		} else { 
			//linear space, and since it only makes one pass, it's the fastest option for score-only requests.	
			AlignmentHelperLinearSpace helper = new AlignmentHelperLinearSpace(A, B, lengthA, lengthB, subs, gapOpen, gapExtend, true);
			helper.fillForward(0,lengthB,0,lengthA,helper.noGap);			
			int myScore = Math.min(helper.fH[lengthA], Math.min (helper.fD[lengthA], helper.fV[lengthA])) ;
			
			if (score != null)
				score.setValue( myScore );

			MesquiteTrunk.mesquiteTrunk.logln("score is " + myScore);			
			
			return null;  // no alignment
		}
	}
	

	public int preProcess (long[] A_withGaps, long[] B_withGaps) { //translates sequences to ints, strips gaps, and possibly swaps A and B.
		int i;
		int totalGapChars = 0;
		
		A = new int[A_withGaps.length];
		B = new int[B_withGaps.length];
		
		if (keepGaps) { // only do this in case where gaps in A are tracked
			followsGapSize = new int[A_withGaps.length];
			//translate sequences to ints, and remove gaps
			for (i=0; i<A_withGaps.length; i++) 
				followsGapSize[i] = 0;
		}
		
		//translate sequences to ints, and remove gaps
		for (i=0; i<A_withGaps.length; i++) {
			if (!CategoricalState.isInapplicable(A_withGaps[i])) {
				int state = (int)CategoricalState.getOnlyElement(A_withGaps[i]);
				if (state >=0)  //Travis: added this to protect against ambiguity codes; ideally do this better
					A[lengthA]= state;
				lengthA++;
			}	else if (keepGaps) {
				followsGapSize[lengthA]++;
				totalGapChars++;	
			}
		}

		for (i=0; i<B_withGaps.length; i++) { 
			if (!CategoricalState.isInapplicable(B_withGaps[i])) {
				int state = (int)CategoricalState.getOnlyElement(B_withGaps[i]);
				if (state>=0)  //Travis: added this to protect against ambiguity codes; ideally do this better
						B[lengthB]= state;
				lengthB++;
			}
		}	
		
		
		//Just to decrease the amount of memory used to O(min of A_length and B_length)
		//note: the "withGaps" version won't do this (at least at first), since then the "A" that needs to have gaps retained would be wrong. 
		if ( !keepGaps && lengthB < lengthA) {
			int [] tmp2 = A;
			A = B;
			B = tmp2;

			int tmp = lengthA;
			lengthA = lengthB;
			lengthB = tmp;
			
			seqsWereExchanged = true;
		}		
		
		return totalGapChars;
	}
	
	public void setGapCosts(int gapOpen, int gapExtend){
	    //	first gap char costs gapOpen+gapExtend, and each additional character costs gapExtend
		this.gapOpen = gapOpen;
		this.gapExtend = gapExtend;
		gapCostsInitialized = true;
	}

	public void setSubsCostMatrix(/*int alphabetSize,*/ int[][] subs){
		//this.alphabetSize = alphabetSize;
		this.subs = subs;
		subCostsInitialized = true;
	}
	
	public void setKeepGaps (boolean keep) {
		keepGaps = keep;	
	}
	
	public void setUseLowMem (boolean lowMem) {
		this.useLowMem = lowMem;	
	}

	public void setIsMinimizationProblem (boolean isMin) {
		isMinimize = isMin;
	}
	
	/* ************************************************* */
	/* Everything below here is for lowMem alignment */
	/* ************************************************* */
	public int recursivelyFillArray(AlignmentHelperLinearSpace helper, int firstColumn, int lastColumn, int firstRow, int lastRow, int precedingShape, int succeedingShape) {
		
		helper.fillArrays(firstRow, lastRow, firstColumn, lastColumn, precedingShape, succeedingShape );
		
		int midRow = (firstRow + lastRow)/2;
		
		int i, verticalColScore, diagonalColScore; 
		int bestColScore = helper.bigNumber , bestCol = -1, bestColShape = helper.noGap;
		for (i=firstColumn; i<=lastColumn; i++) {			
			// best of the ways of leaving the i,j cell of the full DP table with a vertical edge 
			verticalColScore = Math.min(helper.fH[i] + helper.rH[i] + gapExtend + gapOpen,
							Math.min(helper.fH[i] + helper.rD[i] + gapExtend + gapOpen,
							Math.min(helper.fH[i] + helper.rV[i] + gapExtend,
							Math.min(helper.fD[i] + helper.rH[i] + gapExtend + gapOpen,
							Math.min(helper.fD[i] + helper.rD[i] + gapExtend + gapOpen,
							Math.min(helper.fD[i] + helper.rV[i] + gapExtend,			
							Math.min(helper.fV[i] + helper.rH[i] + gapExtend,
							Math.min(helper.fV[i] + helper.rD[i] + gapExtend,
										helper.fV[i] + helper.rV[i] + gapExtend - gapOpen))))))));			

			// best of the ways of leaving the i,j cell of the full DP table with a diagonal edge
			if (i == lastColumn) { 
				diagonalColScore = verticalColScore + 1;
			} else {
				int a = A[i];
				int b = B[midRow];
				int s = subs[a][b];
				diagonalColScore = Math.min(helper.fH[i], Math.min (helper.fD[i], helper.fV[i])) +
										Math.min(helper.rH[i+1], Math.min (helper.rD[i+1], helper.rV[i+1])) +
										s;
			}
			// no need to track horizontal edges - those are already stored in the forward and reverse arrays
			
			if (verticalColScore < diagonalColScore) {
				if ( verticalColScore < bestColScore) {
					bestColScore = verticalColScore;
					bestCol = i;
					bestColShape = helper.gapInA;
				}
			} else {
				if ( diagonalColScore < bestColScore) {
					bestColScore = diagonalColScore;
					bestCol = i;
					bestColShape = helper.noGap;
				}				
			}
		}
		
		lastAWhenBAligned[midRow] = bestCol;
		shapeWhenBAligned[midRow] = bestColShape;
				
		//	Recurse to find the full list of cells through which the alignment passes.
		if ( firstRow != midRow){
			recursivelyFillArray(helper, firstColumn, bestCol, firstRow, midRow, precedingShape, bestColShape);
		}
		if (midRow+1 != lastRow){
			if (bestColShape == helper.noGap){
				recursivelyFillArray(helper, bestCol+1, lastColumn, midRow+1, lastRow, bestColShape, succeedingShape); 
			} else {//gapInA
				recursivelyFillArray(helper, bestCol, lastColumn, midRow+1, lastRow, bestColShape, succeedingShape);
			}		
		}
		
		return bestColScore; // useful for the first level of recursion - returns the total alignment cost
	}
	
	
	public long[][] recoverAlignment (AlignmentHelperLinearSpace helper) {
		int i=0,j, k=0;
		
		long[][] alignment = new long[lengthA + lengthB][2];
		for (j = 0; j<lengthB; j++) {

			//gap in B
			while (i < lastAWhenBAligned[j]) {
				alignment[k][0] = CategoricalState.makeSet(A[i]);
				alignment[k][1] = CategoricalState.inapplicable;
				i++;
				k++;					
			}

			//now we're ready to burn off a letter from B, and possibly a letter from A if diagonal.
			if (shapeWhenBAligned[j] == helper.noGap) {
				alignment[k][0] = CategoricalState.makeSet(A[i]);
				alignment[k][1] = CategoricalState.makeSet(B[j]);
				i++;
			} else {		// gapInA								
				alignment[k][0] = CategoricalState.inapplicable;
				alignment[k][1] = CategoricalState.makeSet(B[j]);
			}
			k++;
		}

		while (i < lengthA) {
			alignment[k][0] = CategoricalState.makeSet(A[i]);
			alignment[k][1] = CategoricalState.inapplicable;
			i++;
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
			long finalSeq2return[][] = new long[k+totalGapChars][2];
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
						finalSeq2return[i+usedGaps][0] =  CategoricalState.inapplicable; 
						finalSeq2return[i+usedGaps][1] =  CategoricalState.inapplicable; 
						usedGaps++;
					}
					j++;
					recentGapRunLength=0;
				}				
				finalSeq2return[i+usedGaps][0] = seq2return[i][0] ;
				finalSeq2return[i+usedGaps][1] = seq2return[i][1] ;									
			}		
			return finalSeq2return;
		} 
		
		return seq2return;
	}
	
	
}
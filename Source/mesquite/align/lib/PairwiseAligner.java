package mesquite.align.lib;

import mesquite.categ.lib.*;
import mesquite.lib.*;


/*TODO:
 *  need to be able to specify whether costs are to be applied for opening gaps on the left or right side of each sequence
 */
public class PairwiseAligner  {

	boolean preferencesSet = false;
	boolean isMinimize = true;	
	int defaultCharThresholdForLowMemory = 5000000;
																 
	int charThresholdForLowMemory = defaultCharThresholdForLowMemory;

    //first gap char costs gapOpen + gapExtend, and each additional character costs gapExtend 
	private int gapOpen;
	private int gapExtend;
	private int gapOpenTerminal;
	private int gapExtendTerminal;
	private int subs[][];
	private boolean keepGaps = false;
	
	public int totalGapChars = 0;
		
	public int A[];
	public int B[];
	public int followsGapSize[] = null; // unused if "keepGaps" is false
	public int lengthA;
	public int lengthB;
	
	private int alphabetLength=4;
	private int gapInsertionArray[];
		
	private boolean gapCostsInitialized = false;
	private boolean subCostsInitialized = false;
	private boolean seqsWereExchanged = false;
		
	public PairwiseAligner (boolean keepGaps, int[][] subs, int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal, int alphabetLength) {
		setSubsCostMatrix(subs);
		setGapCosts(gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal);
		setKeepGaps (keepGaps);
		this.alphabetLength=alphabetLength;
		gapCostsInitialized = subCostsInitialized = true;
	}	

	public PairwiseAligner (boolean keepGaps, int[][] subs, int gapOpen, int gapExtend, int alphabetLength) {
		setSubsCostMatrix(subs);
		setGapCosts(gapOpen, gapExtend);
		setKeepGaps (keepGaps);
		this.alphabetLength=alphabetLength;
		gapCostsInitialized = subCostsInitialized = true;
	}	
	
	public PairwiseAligner () {
		// not much to do here ???
	}

	public long[][] stripEmptyBases(long[][] alignment, int minLength){
		int numExtras = 0;
		for (int i=alignment.length-1; i>=0; i--) {
			if ((CategoricalState.isInapplicable(alignment[i][0]) || CategoricalState.isEmpty(alignment[i][0])) && (CategoricalState.isInapplicable(alignment[i][1])| CategoricalState.isEmpty(alignment[i][1])))
				numExtras++;
			else
				break;
		}
		if (numExtras>0) {
			long[][] ret = new long[alignment.length-numExtras][2];
			for (int i=0; i<alignment.length-numExtras; i++) {
				ret[i][0] = alignment[i][0];
				ret[i][1] = alignment[i][1];
			}
			return ret;
		}
		return alignment;

	}
	
	/** This method returns a 2d-long array ([site][taxon]) representing the alignment of the passed sequences.
	 * If object has been told to retain gaps, gaps in A_withGaps will remain intact (new ones  may be added)*/
	public synchronized long[][] alignSequences( long[] A_withGaps, long[] B_withGaps, boolean returnAlignment, MesquiteNumber score) {
		
		if (!gapCostsInitialized  || !subCostsInitialized) {
			score.setValue( -1 );
			return null;
		}
		
		totalGapChars = preProcess(A_withGaps, B_withGaps);
		
		if ( returnAlignment) { 
		
			long ret[][];
			if ((lengthA*lengthB)>getCharThresholdForLowMemory()) { 
				//low memory (but slower, due to recursion) alignment
				AlignmentHelperLinearSpace helper = new AlignmentHelperLinearSpace(A, B, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal, alphabetLength, keepGaps, followsGapSize);
				
				int myScore =  helper.recursivelyFillArray(0, lengthA, 0, lengthB, helper.noGap, helper.noGap);
		
				ret = helper.recoverAlignment(totalGapChars, seqsWereExchanged);
				gapInsertionArray = helper.getGapInsertionArray();   

			} else {
//				 fast (but quadratic space) alignment
				AlignmentHelperQuadraticSpace helper = new AlignmentHelperQuadraticSpace(A, B, lengthA, lengthB, subs, gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal, alphabetLength);
				ret = helper.doAlignment(returnAlignment,score,keepGaps, followsGapSize, totalGapChars);
				gapInsertionArray = helper.getGapInsertionArray();
			}
		
			if (ret==null)
				return null;
			for (int i=0; i<ret.length; i++) {
				if (ret[i][0]==0L)
					ret[i][0] = CategoricalState.inapplicable;
				if (ret[i][1] ==0L)
					ret[i][1] = CategoricalState.inapplicable;
			}
			
			if (ret.length>lengthA && ret.length>lengthB)
				return stripEmptyBases(ret, MesquiteInteger.maximum(lengthA, lengthB));
			
			return ret;

		} else { 
			//linear space, and since it only makes one pass, it's the fastest option for score-only requests.	
			AlignmentHelperLinearSpace helper = new AlignmentHelperLinearSpace(A, B, lengthA, lengthB, subs, gapOpen, gapExtend, alphabetLength, true, keepGaps, followsGapSize);
			helper.fillForward(0,lengthA,0,lengthB,helper.noGap);			
			int myScore = Math.min(helper.fH[lengthA], Math.min (helper.fD[lengthA], helper.fV[lengthA])) ;
			
			if (score != null)
				score.setValue( myScore );

			
			return null;  // no alignment
		}
	}
	
	/** private method used to convert seqs from mesquite long values to the correct indexes for the subs table
	 * */
	private synchronized int preProcess (long[] A_withGaps, long[] B_withGaps) { //translates sequences to ints, strips gaps, and possibly swaps A and B.
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
		lengthA = 0; 
		lengthB = 0;
		for (i=0; i<A_withGaps.length; i++) {
			if (!CategoricalState.isInapplicable(A_withGaps[i])) {
				A[lengthA]= MolecularState.compressToInt(A_withGaps[i]);  //gets the lower 32 bits of the state set
				lengthA++;
			}	else if (keepGaps) {
				followsGapSize[lengthA]++;
				totalGapChars++;	
			}
		}
		//followsGapSize[lengthA] = 0; //the final entry in this array is spurious ... it says the character after the last character in the string follows a bunhc of gap characters; not meaningful. 
		
		lengthB = 0;
		for (i=0; i<B_withGaps.length; i++) { 
			if (!CategoricalState.isInapplicable(B_withGaps[i])) {
				if (lengthB>=B.length) {
					Debugg.println("|||||||| OVERFLOW B   |||||||||\n   i: " + i + "\n   A.length: " + A.length + "\n   lengthA: " + lengthA + "\n   B.length: " + B.length + "\n   lengthB: " + lengthB + "\n   A_withGaps.length: " + A_withGaps.length + "\n   B_withGaps.length: " + B_withGaps.length + "\n      keepGaps: " + keepGaps);
				}
				B[lengthB] = MolecularState.compressToInt(B_withGaps[i]);  //gets the lower 32 bits of the state set
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
	
	
	/** If object wasn't called with gap cost arguments, this must be called or alignment will fail*/
	public void setGapCosts(int gapOpen, int gapExtend){
	    //	first gap char costs gapOpen+gapExtend, and each additional character costs gapExtend
		this.gapOpen = gapOpen;
		this.gapExtend = gapExtend;
		this.gapOpenTerminal = gapOpen;
		this.gapExtendTerminal = gapExtend;
		gapCostsInitialized = true;
	}

	/** If object wasn't called with gap cost arguments, this must be called or alignment will fail*/
	public void setGapCosts(int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal){
	    //	first gap char costs gapOpen+gapExtend, and each additional character costs gapExtend
		this.gapOpen = gapOpen;
		this.gapExtend = gapExtend;
		this.gapOpenTerminal = gapOpenTerminal;
		this.gapExtendTerminal = gapExtendTerminal;
		gapCostsInitialized = true;
	}

	/** If object wasn't called with subs cost arguments, this must be called or alignment will fail*/
	public void setSubsCostMatrix(int[][] subs){
		this.subs = subs;
		subCostsInitialized = true;
	}
	
	public void setKeepGaps (boolean keep) {
		keepGaps = keep;	
	}
	
	public void setUseLowMem (boolean lowMem) {
		if (lowMem)
			setCharThresholdForLowMemory(0);
		else
			setCharThresholdForLowMemory(Integer.MAX_VALUE);
		//this.useLowMem = lowMem;	
	}

	public long getCharThresholdForLowMemory(){
		//return charThresholdForLowMemory;
		long charsFitInMemWithQuadraticDPTable = MesquiteTrunk.getMaxAvailableMemory()/10; //rough approximation
		return charsFitInMemWithQuadraticDPTable/2; // conservative - if it should only use up half of available memory, use quadratic space algorithm (faster); else use linear space	
	}

	public void setCharThresholdForLowMemory(int numChars){
		 charThresholdForLowMemory = numChars;
	}
	
	public void setCharThresholdForLowMemoryToDefault(){
		 charThresholdForLowMemory = defaultCharThresholdForLowMemory;
	}
		
	
	public void setIsMinimizationProblem (boolean isMin) {
		isMinimize = isMin;
	}
	
	/** returns an array where the ith position indicates the number of new gap positions inserted 
	 * in front of the ith site of the fixed sequence. */
	public int[] getGapInsertionArray() {
		return gapInsertionArray;
	}			
	
	
	/** This method returns true if better alignment scores are higher.  Override if needed. */
	public boolean getHigherIsBetter() {
		return false;
	}   	

	/** This method returns the score of two identical copies of the passed sequence.  Override if needed. */
	public MesquiteNumber getScoreOfIdenticalSequences(long[] sequence, CommandRecord commandRec) {
		return new MesquiteNumber(0.0);
	}


	/** This method returns the score of the worst possible match with the passed sequence.  Acceptable if result is approximate.  Override if needed. */
	public MesquiteNumber getVeryBadScore(long[] sequence, int oppositeLength, int alphabetLength, CommandRecord commandRec) {
		MesquiteNumber score = new MesquiteNumber();
		long[]opposite = new long[oppositeLength];
		for (int i=0; i<sequence.length && i<oppositeLength; i++) {
			long newSet = CategoricalState.emptySet();
			boolean switched =false;
			if (!switched) {
				for (int e=0; e<alphabetLength; e++) {
					if (CategoricalState.isElement(sequence[i],e))  {
						int farElement = -1;
						int maxSub = 0;
						for (int j = 0; j<alphabetLength; j++) {
							if (maxSub<subs[e][j]) {
								maxSub=subs[e][j];
								farElement = j;
							}
						}
						if (farElement>=0 && !CategoricalState.isElement(sequence[i],farElement)) {
							newSet = CategoricalState.addToSet(newSet,farElement);
							switched = true;
							break;
						}
					}
				}
			}
			int first = (int)Math.random() * alphabetLength;  // pick random bit
			if (!switched) {
				if (first>alphabetLength-1)
					first = alphabetLength-1;
				if (first<0)
					first = 0;
				for (int e=first; e<alphabetLength; e++) {
					if (!CategoricalState.isElement(sequence[i],e))  {
						newSet = CategoricalState.addToSet(newSet,e);
						switched = true;
						break;
					}
				}
			}
			if (!switched)
				for (int e=first; e>=0; e--) {
					if (!CategoricalState.isElement(sequence[i],e))  {
						newSet = CategoricalState.addToSet(newSet,e);
						switched = true;
						break;
					}
				}
			if (!switched)
				opposite[i]=sequence[i];
			else
				opposite[i] =newSet;
		}
		alignSequences(sequence,opposite, false, score);
		return score;
	}

	/** This method returns a 2d-long array ([site][taxon]) representing the alignment of the sequences identified by the "taxon" and "site" arguments.
	 * If object has been told to retain gaps, gaps in taxon1 will remain intact (new ones  may be added)*/
	public long[][] alignSequences(MCategoricalDistribution data, int taxon1, int taxon2, int firstSite, int lastSite, boolean returnAlignment, MesquiteNumber score, CommandRecord commandRec) {
		if (lastSite - firstSite+1 <0 || !MesquiteInteger.isCombinable(firstSite) || !MesquiteInteger.isCombinable(lastSite)){
			firstSite = 0;
			lastSite = data.getNumChars()-1;
		}
		if (firstSite<0)
			firstSite=0;
		if (lastSite>data.getNumChars()-1 || lastSite<0)
			lastSite = data.getNumChars()-1;
		int numChars = lastSite - firstSite+1;
		
		long[] extracted1 = new long[numChars];
		long[] extracted2 = new long[numChars];
		

		for (int ic = firstSite; ic<=lastSite; ic++){
			extracted1[ic-firstSite] = data.getState(ic, taxon1);
			extracted2[ic-firstSite] = data.getState(ic, taxon2);
		}
		CategoricalState state=(CategoricalState)(data.getParentData().makeCharacterState());
		long[][] aligned =  alignSequences(extracted1, extracted2, returnAlignment, score);
	
		return aligned;
	}

}
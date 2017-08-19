/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.align.lib;

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
	private boolean allowNewInternalGaps = true;
	private boolean maintainOrder = false;
	private boolean flagGapArrayTerminals = true;
	
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
		
	public PairwiseAligner (boolean keepGaps, boolean allowNewInternalGaps, int[][] subs, int gapOpen, int gapExtend, int gapOpenTerminal, int gapExtendTerminal, int alphabetLength) {
	    setSubsCostMatrix(subs);
		setGapCosts(gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal);
		setKeepGaps (keepGaps);
	    setAllowNewInternalGaps(allowNewInternalGaps);
		this.alphabetLength=alphabetLength;
		gapCostsInitialized = subCostsInitialized = true;
	}	

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

	public static PairwiseAligner getDefaultAligner(boolean keepGaps, MolecularData data){
		MesquiteInteger gapOpen = new MesquiteInteger();
		MesquiteInteger gapExtend = new MesquiteInteger();
		MesquiteInteger gapOpenTerminal = new MesquiteInteger();
		MesquiteInteger gapExtendTerminal = new MesquiteInteger();
		AlignUtil.getDefaultGapCosts(gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal);  
		int alphabetLength = ((CategoricalState)data.makeCharacterState()).getMaxPossibleState()+1;	  
		int subs[][] = AlignUtil.getDefaultSubstitutionCosts(alphabetLength);  
		PairwiseAligner aligner = new PairwiseAligner(keepGaps,subs,gapOpen.getValue(), gapExtend.getValue(), gapOpenTerminal.getValue(), gapExtendTerminal.getValue(), alphabetLength);
		aligner.setUseLowMem(true);
		return aligner;
	}
	
	public static PairwiseAligner getDefaultAligner(MolecularData data){
		return getDefaultAligner(false,data);
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
	
	int preSequenceTerminalFlaggedGap = -1;
	int postSequenceTerminalFlaggedGap = -1;
	
	
	
	public int getPreSequenceTerminalFlaggedGap() {
		return preSequenceTerminalFlaggedGap;
	}

	public int getPostSequenceTerminalFlaggedGap() {
		return postSequenceTerminalFlaggedGap;
	}

	private void doFlagGapArrayTerminals(long[] recipientSequence) {
		if (gapInsertionArray==null)
			return;
		for (int i=0; i<gapInsertionArray.length; i++) { // going up from start
			if (gapInsertionArray[i]>0) {
				boolean terminal=true;
				for (int j=0; j<=i+1 && j<recipientSequence.length; j++) {
					if (recipientSequence[j]!=MolecularState.inapplicable)
						terminal=false;
				}
				if (terminal) {
					gapInsertionArray[i] = -gapInsertionArray[i];  //having them as negative will flag them as terminal gaps
					preSequenceTerminalFlaggedGap = i;
				}
				break;
			}
		}
		for (int i=gapInsertionArray.length-1; i>=0; i--) {
			if (gapInsertionArray[i]>0) {
				boolean terminal=true;
				for (int j=recipientSequence.length-1; j>i && j>=0; j--) {
					if (recipientSequence[j]!=MolecularState.inapplicable)
						terminal=false;
				}
				if (terminal) {
					gapInsertionArray[i] = -gapInsertionArray[i]; //having them as negative will flag them as terminal gaps
					postSequenceTerminalFlaggedGap = i;
				}
				break;  
			}
		}
	}
	
	
	/** This method returns a 2d-long array ([site][taxon]) representing the alignment of the passed sequences.
	 * If object has been told to retain gaps, gaps in A_withGaps will remain intact (new ones  may be added)*/
	public synchronized long[][] alignSequences( long[] A_withGaps, long[] B_withGaps, boolean returnAlignment, MesquiteNumber score) {
		
		if (!gapCostsInitialized  || !subCostsInitialized) {
			if (score != null)
				score.setValue( -1 );
			return null;
		}
		
		int gO  = gapOpen;
		int gE  = gapExtend;
        int gOt = gapOpenTerminal;
        int gEt = gapExtendTerminal;
        if (! this.allowNewInternalGaps)  // make internal gaps impossibly expensive .. but not high enough to cause wrap around of the capacity of an int
            gE = Integer.MAX_VALUE / 10 ;
        
		totalGapChars = preProcess(A_withGaps, B_withGaps);
		
		if ( returnAlignment) { 
		
			long ret[][];
			long charThreshold = getCharThresholdForLowMemory();
			if ((lengthA*lengthB)>charThreshold) { 
				//low memory (but slower, due to recursion) alignment
			    AlignmentHelperLinearSpace helper = new AlignmentHelperLinearSpace(A, B, lengthA, lengthB, subs, gO, gE, gOt, gEt, alphabetLength, keepGaps, followsGapSize);
				int myScore =  helper.recursivelyFillArray(0, lengthA, 0, lengthB, helper.noGap, helper.noGap);
				ret = helper.recoverAlignment(totalGapChars, seqsWereExchanged);
				gapInsertionArray = helper.getGapInsertionArray();  
	            if (score != null)
	                score.setValue( myScore );
			} else {
//				 fast (but quadratic space) alignment
			    AlignmentHelperQuadraticSpace helper = new AlignmentHelperQuadraticSpace(A, B, lengthA, lengthB, subs, gO, gE, gOt, gEt, alphabetLength);
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
				ret = stripEmptyBases(ret, MesquiteInteger.maximum(lengthA, lengthB));
			
			if (flagGapArrayTerminals && gapInsertionArray!=null) {
				doFlagGapArrayTerminals(A_withGaps);
			}
			return ret;

		} else { 
			if (lengthA == 0 || lengthB == 0){
				if (score != null)
					score.setToUnassigned(  );
				return null;
			}
			//linear space, and since it only makes one pass, it's the fastest option for score-only requests.
			AlignmentHelperLinearSpace helper = new AlignmentHelperLinearSpace(A, B, lengthA, lengthB, subs, gO, gE, gOt, gEt, alphabetLength, true, keepGaps, followsGapSize);
			helper.fillForward(0,lengthA,0,lengthB,helper.noGap);			
			int myScore = Math.min(helper.fH[lengthB], Math.min (helper.fD[lengthB], helper.fV[lengthB])) ;
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
			followsGapSize = new int[A_withGaps.length + 1];

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
		if (keepGaps)
			followsGapSize[lengthA] = 0; //the final entry in this array is a placeholder ... it says the character after the last character in the string follows 0 spacers ...  
		
		lengthB = 0;
		for (i=0; i<B_withGaps.length; i++) { 
			if (!CategoricalState.isInapplicable(B_withGaps[i])) {
				if (lengthB>=B.length) {
					MesquiteMessage.println("|||||||| OVERFLOW B   |||||||||\n   i: " + i + "\n   A.length: " + A.length + "\n   lengthA: " + lengthA + "\n   B.length: " + B.length + "\n   lengthB: " + lengthB + "\n   A_withGaps.length: " + A_withGaps.length + "\n   B_withGaps.length: " + B_withGaps.length + "\n      keepGaps: " + keepGaps);
				}
				B[lengthB] = MolecularState.compressToInt(B_withGaps[i]);  //gets the lower 32 bits of the state set
				lengthB++;
			}
		}	
		
		
		//Just to decrease the amount of memory used to O(min of A_length and B_length)
		//note: the "withGaps" version won't do this (at least at first), since then the "A" that needs to have gaps retained would be wrong. 
		if (!maintainOrder && !keepGaps && lengthB < lengthA) {
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
		if (gapOpen >=0)  //allows passing of -1 to leave as is
			this.gapOpen = gapOpen;
		if (gapExtend >=0)
			this.gapExtend = gapExtend;
		if (gapOpenTerminal >=0)
			this.gapOpenTerminal = gapOpenTerminal;
		if (gapExtendTerminal >=0)
			this.gapExtendTerminal = gapExtendTerminal;
		gapCostsInitialized = true;
	}

	/** Sets just the terminal gap costs*/
	public void setTerminalGapCosts(int gapOpenTerminal, int gapExtendTerminal){
		if (gapOpenTerminal >=0)
			this.gapOpenTerminal = gapOpenTerminal;
		if (gapExtendTerminal >=0)
			this.gapExtendTerminal = gapExtendTerminal;
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
		return charsFitInMemWithQuadraticDPTable/3; // conservative - if it should only use up third of available memory, use quadratic space algorithm (faster); else use linear space	
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
	
	public boolean isFlagGapArrayTerminals() {
		return flagGapArrayTerminals;
	}

	public void setFlagGapArrayTerminals(boolean flagGapArrayTerminals) {
		this.flagGapArrayTerminals = flagGapArrayTerminals;
	}


		
	/** This method returns true if better alignment scores are higher.  Override if needed. */
	public boolean getHigherIsBetter() {
		return false;
	}   	

	/** This method returns the score of two identical copies of the passed sequence.  Override if needed. */
	public MesquiteNumber getScoreOfIdenticalSequences(long[] sequence) {
		return new MesquiteNumber(0.0);
	}


	/** This method returns the score of the worst possible match with the passed sequence.  Acceptable if result is approximate.  Override if needed. */
	public MesquiteNumber getVeryBadScore(long[] sequence, int oppositeLength, int alphabetLength) {
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

	public long[][] getAlignment(MolecularData data1, int it1, MolecularData data2, int it2, MesquiteNumber alignScore, boolean maintainOrder) {
		if (data1==null || data2==null)
			return null;
		long[] extracted1 = new long[data1.getNumChars()];
		long[] extracted2 = new long[data2.getNumChars()];
		
		for (int ic = 0; ic<data1.getNumChars(); ic++){
			extracted1[ic] = data1.getState(ic, it1);
		}
		for (int ic = 0; ic<data2.getNumChars(); ic++){
			extracted2[ic] = data2.getState(ic, it2);
		}
		setMaintainOrder(maintainOrder);
		long[][] aligned = alignSequences(extracted1, extracted2, true, alignScore);
		return aligned;
	}


	/** This method returns a 2d-long array ([site][taxon]) representing the alignment of the sequences identified by the "taxon" and "site" arguments.
	 * If object has been told to retain gaps, gaps in taxon1 will remain intact (new ones  may be added)*/
	public long[][] alignSequences(MCategoricalDistribution data, int taxon1, int taxon2, int firstSite, int lastSite, boolean returnAlignment, MesquiteNumber score) {
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

	/** This method returns a 2d-long array ([site][taxon]) representing the alignment of the sequences identified by the "taxon" and "site" arguments.
	 * If object has been told to retain gaps, gaps in taxon1 will remain intact (new ones  may be added)*/
	public long[][] alignSequences(MCategoricalDistribution data, int taxon1,  int firstSite1, int lastSite1, int taxon2, int firstSite2, int lastSite2, boolean returnAlignment, MesquiteNumber score) {
		if (lastSite1 - firstSite1+1 <0 || !MesquiteInteger.isCombinable(firstSite1) || !MesquiteInteger.isCombinable(lastSite1)){
			firstSite1 = 0;
			lastSite1 = data.getNumChars()-1;
		}
		if (firstSite1<0)
			firstSite1=0;
		if (lastSite1>data.getNumChars()-1 || lastSite1<0)
			lastSite1 = data.getNumChars()-1;
		int numChars1 = lastSite1 - firstSite1+1;

		if (lastSite2 - firstSite2+1 <0 || !MesquiteInteger.isCombinable(firstSite2) || !MesquiteInteger.isCombinable(lastSite2)){
			firstSite2= 0;
			lastSite2 = data.getNumChars()-1;
		}
		if (firstSite2<0)
			firstSite2=0;
		if (lastSite2>data.getNumChars()-1 || lastSite2<0)
			lastSite2 = data.getNumChars()-1;
		int numChars2 = lastSite2 - firstSite2+1;

		long[] extracted1 = new long[numChars1];
		long[] extracted2 = new long[numChars2];
		

		for (int ic = firstSite1; ic<=lastSite1; ic++){
			extracted1[ic-firstSite1] = data.getState(ic, taxon1);
		}
		for (int ic = firstSite2; ic<=lastSite2; ic++){
			extracted2[ic-firstSite2] = data.getState(ic, taxon2);
		}
		CategoricalState state=(CategoricalState)(data.getParentData().makeCharacterState());
		
		long[][] aligned =  alignSequences(extracted1, extracted2, returnAlignment, score);
	
		return aligned;
	}

	public boolean isMaintainOrder() {
		return maintainOrder;
	}

	public void setMaintainOrder(boolean maintainOrder) {
		this.maintainOrder = maintainOrder;
	}

	public boolean getAllowNewInternalGaps() {
		return allowNewInternalGaps;
	}

	public void setAllowNewInternalGaps(boolean allowNewInternalGaps) {
		this.allowNewInternalGaps = allowNewInternalGaps;
	}

}

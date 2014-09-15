/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**This class provides some basic utilities for sets of states of categorical characters.  Many of the utilities are static to be used for basic
set calculations, but a categorical set can also be instantiated so as to have a parameter to pass by reference.

In most calculates, state sets of categorical characters are NOT stored with these objects because it would be too costly.  Instead,
they are stored as simple long variables (64 bits, 56 possible states).*/
public class CategoricalState extends CharacterState{
	/** The state (1L<<63) that corresponds to missing data.*/
	public static final long unassigned = 1L<<63;  //-9223372036854775808
	/** The state that corresponds to a gap (inapplicable)*/
	public static final long inapplicable = 1L<<59;   //576460752303423488
	/** The state (1L<<62) that corresponds to an invalid CategoricalState.*/
	public static final long impossible = 1L<<62;  //4611686018427387904
	
	public static final short unassignedShort = compressToShort(unassigned);
	public static final short inapplicableShort = compressToShort(inapplicable);
	public static final short impossibleShort = compressToShort(impossible);

	/** a value for arrays that store information about frequency of states; this would need to change if ever maxCategoricalState goes to 58*/
	public static final int polymorphismElement = 58;
	/** The bit that signals missing data */
	public static final int unassignedBit = 63;
	/** The bit that signals a gap (inapplicable) */
	public static final int inapplicableBit = 59;
	/** The bit that signals uncertainty */
	public static final int uncertainBit = 61;
	/** The bit that signals the symbol is to be shown lower case */
	public static final int lowerCaseBit = 60;
	/** The maximum allowed categorical state (55) */
	public static  final int maxCategoricalState = 55;
	/** The mask that preserves the high bits used for special flags*/
	public static final long highBitsMask = (~0L)<<59;
	/** The mask that preserves bits 0.. maxCategoricalState*/
	public static final long statesBitsMask = (~0L)>>>8;
	/** The mask that preserves bits 0.. maxCategoricalState and the unassigned, impossible and uncertain bits */
	public static final long dataBitsMask = (statesBitsMask | unassigned | impossible | inapplicable | (1L<<uncertainBit));
	public static final short highBitsMaskShort = compressToShort(highBitsMask);
	public static final short statesBitsMaskShort = compressToShort(statesBitsMask);
	public static final short dataBitsMaskShort = compressToShort(dataBitsMask);

	/** Bits to be used if int used instead of long for state storage */
	public static final int unassignedBitInt= 31;
	public static final int inapplicableBitInt = 27;
	public static final int uncertainBitInt = 29;
	public static final int statesBitsMaskInt = (~0)>>>8;
	
	
	public static final int TOTALBITS = 64;  //for use by other classes that might want to make, say, an array for the bits
	
	private static final int MAXBIT = 63; //MAXBIT for bitwise manipulations
	long set; //the value stored for an instantiation of this
	
	public CategoricalState () {
		set = unassigned;
	}
	public CategoricalState (long initial) {
		set = initial;
	}
	/*..........................................CategoricalState.....................................*/
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return CategoricalData.DATATYPENAME;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the class of the corresponding (i.e. of same data type) CharacterData object (in this case, CategoricalData.class). */
	public Class getCharacterDataClass() {
		return CategoricalData.class;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the Adjustable class of the corresponding (i.e. of same data type) MCharactersDistribution object (in this case, MCategoricalAdjustable.class). */
	public Class getMCharactersDistributionClass(){
		return MCategoricalAdjustable.class;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the Adjustable class of the corresponding (i.e. of same data type) CharacterDistribution object (in this case, CategoricalAdjustable.class). */
	public Class getCharacterDistributionClass() {
		return CategoricalAdjustable.class;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the class of the corresponding (i.e. of same data type) CharacterHistory object (in this case, CategoricalHistory.class). */
	public Class getCharacterHistoryClass() {
		return CategoricalHistory.class;
	}
	public AdjustableDistribution makeAdjustableDistribution(Taxa taxa, int numNodes){
		return new CategoricalAdjustable(taxa, numNodes);
	}
	public CharacterHistory makeCharacterHistory(Taxa taxa, int numNodes){
		return new CategoricalHistory(taxa, numNodes);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state set passed is unassigned (missing data) */
	public static boolean isUnassigned(short s) {
		return s == unassignedShort;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state set passed is unassigned (missing data) */
	public static boolean isUnassigned(long s) {
		return s == unassigned;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff stateset is unassigned (missing data) */
	public boolean isUnassigned() {
		return set == unassigned;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state set passed is inapplicable. */
	public static boolean isInapplicable(short s) {
		return s == inapplicableShort;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state set passed is inapplicable. */
	public static boolean isInapplicable(long s) {
		return s == inapplicable;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state set is inapplicable */
	public boolean isInapplicable() {
		return set == inapplicable;
	}
	/*..........................................CategoricalState.....................................*/
	/** returns whether value is valid or not.*/
	public boolean isImpossible(){
		return set == impossible;
	}
	/*..........................................CategoricalState.....................................*/
	/** returns whether value is valid or not.*/
	public static boolean isImpossible(short s){
		return s == impossibleShort;
	}
	/*..........................................CategoricalState.....................................*/
	/** returns whether value is valid or not.*/
	public static boolean isImpossible(long s){
		return s == impossible;
	}
	/*..........................................CategoricalState.....................................*/
	/** returns whether value is combinable (i.e. a valid assigned state) or not.*/
	public boolean isCombinable(){
		return !(set == impossible || set == inapplicable || set == unassigned || (set & statesBitsMask) == 0L);
	}
	/*..........................................CategoricalState.....................................*/
	/** returns whether value is combinable (i.e. a valid assigned state) or not.*/
	public static boolean isCombinable(long s){
		return !(s == impossible || s == inapplicable || s == unassigned || (s & statesBitsMask) == 0L);
	}
	/*..........................................CategoricalState.....................................*/
	/** returns whether state is within allowed range.*/
	public static boolean legalState(int e){
		return e>= 0 && e<=maxCategoricalState;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s) {
		return equals(s, false, false);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s, boolean allowMissing) {
		return equals(s, allowMissing, false);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s, boolean allowMissing, boolean allowNearExact) {
		return equals(s, allowMissing, allowNearExact, false);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s, boolean allowMissing, boolean allowNearExact, boolean allowSubset) {
		if (s==null)
			return false;
		if (!(s instanceof CategoricalState))
			return false;
		if (allowMissing && (isUnassigned() || s.isUnassigned())) //fixed June 02
				return true;
		long t = getValue();
		long tO = ((CategoricalState)s).getValue();
		if (allowSubset) {
			if (t == tO)
				return true;
			if (isUncertain(t) || isUncertain(tO)){
				t = statesBitsMask & t;
				tO = statesBitsMask & tO;
				return t == (t | tO) || tO == (t | tO);
			}
			if (allowNearExact)
				return (setLowerCase(tO, false) == setLowerCase(t,false));
			return false;
		}
		if (allowNearExact)
			return (setLowerCase(tO, false) == setLowerCase(t,false));
		return t == tO;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff state sets are same */
	public boolean equalsIgnoreCase(CategoricalState s) {
		if (s==null)
			return false;
		long t = getValue();
		long tO = ((CategoricalState)s).getValue();
		return (setLowerCase(tO, false) == setLowerCase(t,false));
	}
	/*...............................................................................*/
	/**returns new state  */
	public static CategoricalState newState() {
		return new CategoricalState();  
	}
	/*..........................................CategoricalState.....................................*/
	/**returns  state set */
	public static boolean isEmpty(long s) {
		return s==emptySet();  
	}
	/*..........................................CategoricalState.....................................*/
	/**returns  state set */
	public static long emptySet() {
		return 0L;  
	}
	/*..........................................CategoricalState.....................................*/
	/**returns state set consisting of all elements */
	public static long fullSet() {
		return statesBitsMask; //(0xFFFFFFFFFFFFFFFFL);  
	}
	/*.................................................................................................................*/
	/**Returns the set that is {0,...,(numInSet-1)}  That is, the first "numInSet" bits are set.
	This is designed for use with next set, to generate for instance all possible sets containing
	numInSet elements*/
	public static long firstSet(int numInSet) { 
		if (numInSet==0)
			return 0;
		else
			return span(0, numInSet-1);//fullSet()>>>(64-numInSet);
	}
	/*.................................................................................................................*/
	/**When passed a set, returns the next set in sequence with the same
	number of elements and with maximum element maxS.  This can be called
	repeatedly (after starting with firstSet) to generate all sets with a
	certain number of elements.  When the current set is the last one,
	returns an empty set.*/
	public static long nextSet(long current, int maxS) { //todo: have mask instead of just maxS
		if (current==0)
			return 0;
		long result = current;
		int max = maximum(current);
		if (max < maxS) { //still room to move over left; do it
			result = clearFromSet(result, max);
			return addToSet(result, max +1);
		}
		else { //no; get next from all but last
			result = clearFromSet(result, max);
			result = nextSet(result, max-1);
			if (result==0){ //hadn't been any more
				return 0L;
			}
			else  {
				int newMax = maximum(result);
				return addToSet(result, newMax+1);
			}
		}
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff e is element in state set */
	public boolean isElement(int e) {
		if (!legalState(e))
			return false;
		else
			return (((1L<<e)&set)!=0L);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff e is element in state set s */
	public static boolean isElement(long s, int e) {
		if (!legalState(e))
			return false;
		else
			return (((1L<<e)&s)!=0L);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true iff the first long is a subset of the second */
	public static boolean isSubset(long sub, long s) {
		return (statesBitsMask & sub & s) == (statesBitsMask & sub);
	}
	
	/*..........................................CategoricalState.....................................*/
	/**returns minimum element in state set s */
	public static int minimum(long s) {
		if ((statesBitsMask & s)==0L)
			return -1;
		for (int e=0; e<=getMaxPossibleStateStatic(); e++) {
			if (((1L<<e)&s)!=0L)
				return e;
		}
		return -1;  
	}
	/*..........................................CategoricalState.....................................*/
	/**Returns the only element in state set s.  If more than one element, or no elements, returns -1. */
	public static int getOnlyElement(long s) {
		int min = minimum(s);
		int max = maximum(s);
		if (min==max)
			return min;
		return -1;  
	}
	/*..........................................CategoricalState.....................................*/
	/**returns number of states in state set shortS */
	public static int cardinality(int shortS) {
		int count =0;
			for (int e=0; e<= getMaxPossibleStateStatic(); e++) {
				if (((1L<<e)&shortS)!=0) {  //test bit
					count++;
				}
			}
			return count;

	}
	/*..........................................CategoricalState.....................................*/
	/**returns number of states in state set s */
	public static int cardinality(long s) {
		if ((statesBitsMask & s)==0L)
		 	return 0;  
		
		else {
			int count =0;
			for (int e=0; e<= getMaxPossibleStateStatic(); e++) {
				if (((1L<<e)&s)!=0L) {  //test bit
					count++;
				}
			}
			return count;
		}
	}
	/*..........................................CategoricalState.....................................*/
	/**returns number of states in state set */
	public  int cardinality() {
		long s = getValue();
		if ((statesBitsMask & s)==0L)
		 	return 0;  
		
		else {
			int count =0;
			for (int e=0; e<= getMaxPossibleStateStatic(); e++) {
				if (((1L<<e)&s)!=0L) {  //test bit
					count++;
				}
			}
			return count;
		}
	}
	/*..........................................CategoricalState.....................................*/
	/**returns whether number of states in state set s is greater than 1 */
	public static boolean hasMultipleStates(long s) {
		if ((statesBitsMask & s)==0L)
		 	return false;  
		
		else {
			int count =0;
			for (int e=0; e<= getMaxPossibleStateStatic(); e++) {
				if (((1L<<e)&s)!=0L) {  //test bit
					count++;
					if (count>1)
						return true;
				}
			}
			return false;
		}
	}
	/*..........................................CategoricalState.....................................*/
	/**return maximum value of states in state set s */
	public static int maximum(long s) {
		if (s==0L) {
		 	return -1;  
		}
		else {
			int max = -1;
			for (int e=0; e<= getMaxPossibleStateStatic(); e++) {
				if (((1L<<e)&s)!=0L) {  //test bit
					max=e;
				}
			}
			return max;
		}
/*
		Attempt 1 (doesn't work on IE4/W95)
		for (int e=maxCategoricalState; e>=0; e--) {
			if (((1L<<e)&s)!=0)
				return e;
		}
		Attempt 2 (doesn't work on IE4/W95)
			long mask = ~(0L);
			for (int e=0; e<= maxCategoricalState; e++) {
				mask <<= 1;
				if ((mask & s)==0L)
					return e;
			}
		Attempt 3 (doesn't work on IE4/W95)
			for (int e=0; e<= maxCategoricalState; e++) {
				if (((1L<<e)&s)!=0) {  //test bit
					s &= ~(1L<<e);  //clear bit
					if (s==0L)
						return e;
				}
			}
*/
	//	return -1;  // should be missing!!!!
	}
	/*..........................................CategoricalState.....................................*/
	/**returns true if e is only state in state set s */
	public static boolean isOnlyElement(long s, int e) {
		if (!legalState(e))
			return false;
		else
			return ((1L<<e)==s);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns a state set including only the state e */
	public static long makeSet(int e) {
		if (legalState(e))
			return (1L<<e);
		else 
			return impossible;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns a state set including only elements in shortS; if shortS has multiple elements, the uncertainty bit is set. */
	public static long makeSetFromLowerBits(int shortS) {
		if (cardinality(shortS)<=1)
			return (long)shortS;
		else 
			return setUncertainty((long)shortS,true);
	}

	/*..........................................CategoricalState.....................................*/
	/**returns a state set including only the states e1 & e2 */
	public static long makeSet(int e1, int e2) {
		long b = makeSet(e1);
		return addToSet(b, e2);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns a state set including only the states e1 & e2 & e3 */
	public static long makeSet(int e1, int e2, int e3) {
		long b = makeSet(e1);
		return addToSet(addToSet(b, e2), e3);
	}
	/*..........................................CategoricalState.....................................*/
	/**returns a state set including only the states e1 & e2 & e3 & e4 */
	public static long makeSet(int e1, int e2, int e3, int e4) {
		long b = makeSet(e1);
		return addToSet(addToSet(addToSet(b, e2), e3), e4);
	}
	/*..........................................CategoricalState.....................................*/
	/**return a state set with all states a through b inclusive. */
	public static long span(int a, int b) {
		if (!legalState(a) || !legalState(b))
			return impossible;
		else if (a>b) {
			int c=a;
			a = b;
			b = c;
		}
		return ((~(0L)) << a) & ((~(0L)) >>> (MAXBIT-b));
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the state set spanning min to max of s */
	public static long span(long s) {
		if ((s & statesBitsMask) ==0L) //todo: put more queries like this
			return 0;
		for (int min=0; min<=maxCategoricalState; min++) {
			if (((1L<<min)&s)!=0L) {
				int max = -1;
				for (int e=min; e<= maxCategoricalState; e++) {
					if (((1L<<e)&s)!=0L) {  //test bit
						max=e;
					}
				}
				return span(min, max);
			}
		}
		return 0;
	}
	/*..........................................CategoricalData.....................................*/
	/**merges the states  */
	public static long mergeStates(long s1, long s2) {
		boolean bothHaveStates = CategoricalState.cardinality(s1) >0 && CategoricalState.cardinality(s2) >0 ;
		long sMerged = CategoricalState.union(s1,s2);
		if (bothHaveStates) {
			if (CategoricalState.cardinality(sMerged)>=2) {
				long s1StatesOnly = s1& statesBitsMask;
				long s2StatesOnly = s2 & statesBitsMask;
				long sMergedStatesOnly = sMerged & statesBitsMask;
				if (((sMergedStatesOnly | s1StatesOnly)==s1StatesOnly) && !CategoricalState.isUncertain(s1)) //s1 contains all the states and it is not uncertain
					sMerged = s1;
				else if (((sMergedStatesOnly | s2StatesOnly)==s2StatesOnly) && !CategoricalState.isUncertain(s2)) //s2 contains all the states and it is not uncertain
					sMerged = s2;
				else {
					boolean oneIsUncertain = CategoricalState.isUncertain(s1) || CategoricalState.isUncertain(s2);
					sMerged = CategoricalState.setUncertainty(sMerged,oneIsUncertain);
				}
			}
			// if the cardinality is less than 2, then they both must have the same state, and we don't do anything
		}
		else if (CategoricalState.cardinality(s1) >0) // s1 has values, s2 doesn't, so just use s1
			sMerged=s1;
		else if (CategoricalState.cardinality(s2) >0) // s2 has values, s1 doesn't, so just use s2
			sMerged=s2;
		else if (CategoricalState.isUnassigned(s1) || CategoricalState.isUnassigned(s2))  // neither has a state, at least one is missing
			sMerged = CategoricalState.unassigned;
		return sMerged;
	}
	/*..........................................CategoricalState.....................................*/
	/**Adds or clears the inapplicable bit of state set s , used for merging operations*/
	public static long setInapplicable(long s, boolean b) {
		if (b)
			return inapplicable | s;
		else 
			return (~(inapplicable))&s;
	}
	/*..........................................CategoricalState.....................................*/
	/**Adds or clears the unassigned bit of state set s , used for merging operations*/
	public static long setUnassigned(long s, boolean b) {
		if (b)
			return unassigned | s;
		else 
			return (~(unassigned))&s;
	}
	/*..........................................CategoricalState.....................................*/
	/**return the result of setting or clearing the lowerCase flag of state set s */
	public static long setLowerCase(long s, boolean lowerCase) {
		if (lowerCase)
			return (1L<<lowerCaseBit) | s;
		else 
			return (~(1L<<lowerCaseBit))&s;
	}
	/*..........................................CategoricalState.....................................*/
	/**return whether lowerCase flag is set in state set s */
	public static boolean isLowerCase(long s) {
		return (((1L<<lowerCaseBit)&s)!=0L);
	}
	/*..........................................CategoricalState.....................................*/
	/**return whether lowerCase flag is set in state set s */
	public boolean isLowerCase() {
		return (((1L<<lowerCaseBit)&set)!=0L);
	}
	/*..........................................CategoricalState.....................................*/
	/**return the result of setting or clearing the uncertainty flag of state set s */
	public static long setUncertainty(long s, boolean uncertain) {
		if (uncertain)
			return (1L<<uncertainBit) | s;
		else 
			return (~(1L<<uncertainBit))&s;
	}
	/*..........................................CategoricalState.....................................*/
	/**return whether uncertainty flag is set in state set s */
	public static boolean isUncertain(long s) {
		return (((1L<<uncertainBit)&s)!=0L);
	}
	/*..........................................CategoricalState.....................................*/
	/**return the result of adding state e to state set s */
	public static long addToSet(long s, int e) {
		if (legalState(e))
			return (1L<<e) | s;
		else 
			return s;
	}
	/*..........................................CategoricalState.....................................*/
	/**return the result of taking the union of two states */
	public static long union(long s1, long s2) {
		return s1 | s2;
	}
	/*..........................................CategoricalState.....................................*/
	/**return the result of taking the intersection of two states */
	public static long intersection(long s1, long s2) {
		return s1 & s2;
	}
	/*..........................................CategoricalState.....................................*/
	/**return whether or not the two state sets share any states */
	public static boolean statesShared(long s1, long s2) {
		return (statesBitsMask & s1 & s2) != emptySet();
	}
	/*..........................................CategoricalState.....................................*/
	/**return whether or not the two state sets share any states */
	public boolean statesShared(CategoricalState cs) {
		return (statesBitsMask & getValue() & cs.getValue()) != emptySet();
	}
	/*..........................................CategoricalState.....................................*/
	/**return the result of subtracting state e to state set s */
	public static long clearFromSet(long s, long e) {
		if (isCombinable(e))
			return (~e)&s;
		else 
			return s;
	}
	/*..........................................CategoricalState.....................................*/
	/**return the result of subtracting state e to state set s */
	public static long clearFromSet(long s, int e) {
		if (legalState(e))
			return (~(1L<<e))&s;
		else 
			return s;
	}

	/*.................................................................................................................*/
	/* finds which states are best and returns state set */
	public static long chooseHighestWithinFactor(double[] values, double factor) {
		if (values == null)
			return 0L;
		double bestSoFar = MesquiteDouble.unassigned;
		long resultSet = 0L;
		for (int i=0; i< values.length; i++) {
			if (MesquiteDouble.greaterThan(values[i], bestSoFar, 0.00000001)) {
				if (MesquiteDouble.greaterThanByFactor(values[i], bestSoFar, factor))
					resultSet = makeSet(i);
				else
					resultSet = addToSet(resultSet, i);
				bestSoFar = values[i];
			}
			else if (MesquiteDouble.equalsWithinFactor(values[i], bestSoFar, factor))
				resultSet = addToSet(resultSet, i);
		}
		return resultSet;
	}
	/*.................................................................................................................*/
	/* finds which states are best and returns state set */
	public static long chooseHighest(double[] values, double toleranceProportion) {
		if (values == null)
			return 0L;
		double bestSoFar = MesquiteDouble.unassigned;
		long resultSet = 0L;
		for (int i=0; i< values.length; i++) {
			if (MesquiteDouble.greaterThan(values[i], bestSoFar, toleranceProportion)){  
				bestSoFar = values[i];
				resultSet = makeSet(i);
			}
			else if (MesquiteDouble.equals(values[i], bestSoFar, toleranceProportion))
				resultSet = addToSet(resultSet, i);
		}
		return resultSet;
	}
	/*.................................................................................................................*/
	/* finds which states are best and returns state set */
	public static long chooseLowest(double[] values, double toleranceProportion) {
		if (values == null)
			return 0L;
		double bestSoFar = MesquiteDouble.unassigned;
		long resultSet = 0;
		for (int i=0; i< values.length; i++) {
			if (MesquiteDouble.lessThan(values[i], bestSoFar, toleranceProportion)){  
				bestSoFar = values[i];
				resultSet = makeSet(i);
			}
			else if (MesquiteDouble.equals(values[i], bestSoFar, toleranceProportion))
				resultSet = addToSet(resultSet, i);
		}
		return resultSet;
	}
	/** The masks that preserves bits 0.. maxCategoricalState*/
	public static final long shortStatesBitsMaskL = (~0L)>>>53; //5 for high bits + 48 for reduction from 64 to 16
	public static final long intStatesBitsMaskL = (~0L)>>>37; //5 for high bits + 32 for reduction from 64 to 32

	/*..........................................CategoricalState.....................................*/
	public static boolean compressibleToShort(long s){
		return (s & shortStatesBitsMaskL) == (s & CategoricalState.statesBitsMask);
	}
	/*..........................................CategoricalState.....................................*/
	public static short compressToShort(long s){
		return (short)((s & shortStatesBitsMaskL) | ((s & CategoricalState.highBitsMask)>>>48));
	}
	/*..........................................CategoricalState.....................................*/
	public static long expandFromShort(short s){
		long LS = ((long)s);
		return (LS & shortStatesBitsMaskL) | ((LS << 48) & CategoricalState.highBitsMask);
	}
	/*..........................................CategoricalState.....................................*/
	public static boolean compressibleToInt(long s){
		return (s & intStatesBitsMaskL) == (s & CategoricalState.statesBitsMask);
	}
	/*..........................................CategoricalState.....................................*/
	public static int compressToInt(long s){
		return (int)((s & intStatesBitsMaskL) | ((s & CategoricalState.highBitsMask)>>>32));
	}
	/*..........................................CategoricalState.....................................*/
	public static long expandFromInt(int s){
		long LS = ((long)s);
		return (LS & intStatesBitsMaskL) | ((LS << 32) & CategoricalState.highBitsMask);
	}
	/*..........................................CategoricalState.....................................*/
	/** Expands state set (passed as long) to full array of included elements. */
	public static int[] expand(long s) {
		int numStates = cardinality(s);
		int count=0;
		int[] result = new int[numStates];
		for (int e=0; e<=maxCategoricalState; e++) {
			if (isElement(s, e)) {
				result[count]=e;
				count++;
			}
		}
		return result;
	}
	/*..........................................CategoricalState.....................................*/
	/** Compresses array listing included elements into state set form (long, with bits set).  This is inverse of expand.*/
	public static long compressFromList(int[] states) {
		if (states == null) //added 20 Oct 01
			return 0L;
		long s=0L;
		for (int i=0; i<states.length; i++) {
			if (states[i]>=0 && states[i]<=maxCategoricalState)
				s= addToSet(s, states[i]);
		}
		return s;
	}
	/*..........................................CategoricalState.....................................*/
	/** Compresses array with presence (>0) or absence (<=0) for each element into state set form (long, with bits set).*/
	public static long compressFromPresence(double[] states) {
		if (states == null) //added 20 Oct 01
			return 0L;
		long s=0L;
		for (int i=0; i<states.length; i++) {
			if (states[i]>0.0) 
				s= addToSet(s, i);
		}
		return s;
	}
	/*..........................................CategoricalState.....................................*/
	/** sets the value to inapplicable*/
	public void setToInapplicable() {
		set = inapplicable;
	}
	/*..........................................CategoricalState.....................................*/
	/** sets the value to unassigned*/
	public void setToUnassigned() {
		set = unassigned;
	}
	/*..........................................CategoricalState.....................................*/
	/**gets the value of the state set */
	public long getValue() {
		return set;
	}
	/*..........................................CategoricalState.....................................*/
	/**sets the value of the state set to the long variable passed*/
	public void setValue(long value) {
		set = value;
	}
	
	
	/*..........................................CategoricalState.....................................*/
	/**sets the value using the passed CharacterState's value if of same type */
	public void setValue(CharacterState cs){
		if (cs!=null && cs instanceof CategoricalState) {
			setValue(((CategoricalState)cs).getValue());
		}
	}
	/*..........................................CategoricalState.....................................*/
	/** sets its value to the value given by the String passed to it starting at position pos �*
	public void setValue(String s, MesquiteInteger pos){
		if (s==null){
			set = unassigned;
			return;
		}
		boolean polymorphOn = false;
		boolean uncertainOn = false;
		boolean done = false;
		long stateSet = 0L;
		int loc = pos.getValue();
		while (loc<s.length() && !done) { 
			char c = s.charAt(loc++); //get next dark character in string
			while (ParseUtil.whitespace(c, null)  && c!=0 && loc<s.length())
				c = s.charAt(loc++);
				
			if (!ParseUtil.whitespace(c, null)){
				if (c == '(')
					polymorphOn = true;
				else if (c == '{')
					uncertainOn = true;
				else if (c == '}' || c == ')') 
					done = true;
				else if (polymorphOn || uncertainOn) {
					long state =  fromChar(c);
					if (CategoricalState.isCombinable(state))
						stateSet |=  state;
				}
				else {
					stateSet =  fromChar(c);
					done = true;
				}
			}
		}
		pos.setValue(loc);
		if (uncertainOn)
			stateSet = setUncertainty(stateSet, true);
		setValue(stateSet);
	}
	/*..........................................CategoricalState.....................................*/
	/* Sets the value of this CharacterState according to the string, assuming the parent data is as given �*/
	public void setValue(String s, CharacterData parentData) {
		if (s==null){
			set = unassigned;
			return;
		}
		MesquiteInteger pos = new MesquiteInteger(0);
		boolean polymorphOn = false;
		boolean uncertainOn = false;
		boolean done = false;
		set = 0L;
		int loc = pos.getValue();
		while (loc<s.length() && !done) {
			char c = s.charAt(loc++);
			while (ParseUtil.whitespace(c, null)  && c!=0 && loc<s.length())
				c = s.charAt(loc++);
			if (!ParseUtil.whitespace(c, null)){
				if (c == '(')
					polymorphOn = true;
				else if (c == '{')
					uncertainOn = true;
				else if (c == '}' || c == ')') 
					done = true;
				else if (polymorphOn || uncertainOn) {
					long state;
					if (parentData == null || !(parentData instanceof CategoricalData))
						state =  fromChar(c);
					else
						state = ((CategoricalData)parentData).fromChar(c);
					if (CategoricalState.isCombinable(state))
						set |=  state;
				}
				else {
					if (parentData == null || !(parentData instanceof CategoricalData))
						set =  fromChar(c);
					else
						set =  ((CategoricalData)parentData).fromChar(c);
					done = true;
				}
			}
		}
		if (uncertainOn)
			set = setUncertainty(set, true);
	}
	/*..........................................CategoricalState.....................................*/
	/**return the state set containing the state represented by the character (e.g., '0' to {0}); 
	Used default symbols since no CharacterData is specified �*/
	public long fromChar(char c) { //this doesn't work with symbols!
		if (c == '?')
			return unassigned;
		if (c == '-')
			return inapplicable;
		int e=whichState(c); //find which state this character is, among default symbols
		if (legalState(e))
			return (1L<<e); //return set with just that element
		else {
			return impossible;
		}
	}
	/*..........................................CategoricalState.....................................*/
	/*find which state this character refers to, comparing against default symbols �*/
	public int whichState(char c){
		for (int i=0; i<CategoricalData.defaultSymbols.length; i++){
			if (CategoricalData.defaultSymbols[i] == c)
				return i;
		}
		return -1;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the String description of the state set*/
	public String toString() {
		return toString(set);
	}
	/*..........................................CategoricalState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses braces, and does not use character state names*/
	public static String toString(long s) {
		return toString(s, null, 0, true);
	}
	/*..........................................CategoricalState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Does not use character state names*/
	public static String toString(long s, boolean useBraces) {
		return toString(s, null, 0, useBraces);
	}
	/*..........................................CategoricalState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses character state names if available.*/
	public static String toString(long s, CategoricalData data, int ic, boolean useBraces) {
		return toString(s, data, ic, useBraces, false);
	}
	/*..........................................CategoricalState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses character state names if available. �*/
	public static String toString(long s, CategoricalData data, int ic, boolean useBraces, boolean useSymbols) {
		if (s == impossible)
			return "impossible";
		else if (s== unassigned) {
			if (data == null)
				return "?";
			else
				return String.valueOf(data.getUnassignedSymbol());
		}
		else if (s == inapplicable){
			if (data == null)
				return "-";
			else
				return String.valueOf(data.getInapplicableSymbol());
		}
			
			
		boolean first=true;
		String temp;
		if (useBraces) 
			temp="{";
		else
			temp="";
		for (int e=0; e<=maxCategoricalState; e++) {
			if (isElement(s, e)) {
				if (!first)
					temp+=" ";
				if (data != null) {
					if (useSymbols)
						temp+=data.getSymbol(e);
					else
						temp+=data.getStateName(ic, e);
				}
				else if (useSymbols)
					temp+=CategoricalData.defaultSymbols[e];
				else
					temp+=Integer.toString(e);
				first=false;
			}
		}
		if (first)
			temp += '!'; //no state found!
		if (useBraces)
			temp+="}";
		return temp;
	}
	/*..........................................CategoricalState.....................................*/
	/** Returns string as would be displayed to user (not necessarily internal shorthand).  �*/
	public  String toDisplayString(){
		if (isInapplicable(set))
			return "" + CharacterData.defaultInapplicableChar;
		if (isUnassigned(set))
			return "" + CharacterData.defaultMissingChar;
		char sep = '&';
		if (isUncertain(set))
			sep = '/';
		boolean first=true;
		String temp="";
		for (int e=0; e<=maxCategoricalState; e++) {
			if (((1L<<e)&set)!=0L) {
				if (!first)
					temp+=sep;
				temp+=CategoricalData.defaultSymbols[e];
				first=false;
			}
		}
		if (first)
			temp += '!'; //no state found!
		return temp;
	}
	/*..........................................CategoricalState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public static String toSimpleString(long s) {
		if (isInapplicable(s))
			return "" + CharacterData.defaultInapplicableChar;
		if (isUnassigned(s))
			return "" + CharacterData.defaultMissingChar;
		boolean first=true;
		String temp="";
		for (int e=0; e<=maxCategoricalState; e++) {
			if (((1L<<e)&s)!=0L) {
				if (!first)
					temp+=" ";
				temp+=CategoricalData.defaultSymbols[e];
				first=false;
			}
		}
		if (first)
			temp += '!'; //no state found!
		if (temp.length()>1)
			temp ="{" + temp + "}";
		return temp;
	}
	/*..........................................CategoricalState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public String toNEXUSString() {
		return toNEXUSString(set);
	}
	/*..........................................CategoricalState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public static String toNEXUSString(long s) {
		if (isInapplicable(s))
			return "" + CharacterData.defaultInapplicableChar;
		if (isUnassigned(s))
			return "" + CharacterData.defaultMissingChar;
		String temp="";
		for (int e=0; e<=maxCategoricalState; e++) {
			if (((1L<<e)&s)!=0L) {
				temp+=CategoricalData.defaultSymbols[e];
			}
		}
		if (temp.length()>1)
			temp ="{" + temp + "}";
		return temp;
	}
	/*..........................................CategoricalState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states.  Includes High Bits.  Used for development/debugging. �*/
	public static String toSimpleStringHB(long s) {
		if (isInapplicable(s))
			return "" + CharacterData.defaultInapplicableChar;
		if (isUnassigned(s))
			return "" + CharacterData.defaultMissingChar;
		boolean first=true;
		String temp="";
		for (int e=0; e<=maxCategoricalState; e++) {
			if (((1L<<e)&s)!=0L) {
				if (!first)
					temp+=",";
				temp+=CategoricalData.defaultSymbols[e];
				first=false;
			}
		}
		for (int e=maxCategoricalState+1; e<=MAXBIT; e++) {
			if (((1L<<e)&s)!=0L) {
				if (!first)
					temp+=",";
				temp+= " " + Integer.toString(e);
				first=false;
			}
		}
		if (temp.length()>1)
			temp ="{" + temp + "}";
		return temp;
	}

	/*..........................................CategoricalState.....................................*/
	/** converts passed long as bits. */
	public static String toStringBits(long s) {
		StringBuffer temp= new StringBuffer();
		for (int e=63; e>=0; e--) {
			if (((1L<<e)&s)!=0L) 
				temp.append('1');
			else
				temp.append('0');
		}
		
		return temp.toString();
	}

	/*..........................................CategoricalState.....................................*/
	/**returns the maximum possible state */
	public static int getMaxPossibleStateStatic() {
		return maxCategoricalState;
	}
	public static int getTotalBitsInStateSet() {
		return TOTALBITS;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the maximum possible state */
	public int getMaxPossibleState() {
		return maxCategoricalState;
	}

	/*..........................................CategoricalState.....................................*/
	/**return whether uncertainty flag is set in state set s */
	public static boolean isUnassignedInt(int s) {
		return (((1<<unassignedBitInt)&s)!=0);
	}

}



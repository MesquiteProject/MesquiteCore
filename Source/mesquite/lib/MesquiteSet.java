/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;

/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules
/* ���������������������������bits������������������������������� */


/* ======================================================================== */
/**This class provides some basic utilities for small sets, as in the CategoricalState (which maintains many of the
same methods independently; this is done so CategoricalState can subclass CharacterState).*/
public class MesquiteSet {
	/** The state (1L<<63) that corresponds to missing data.*/
	public static final long unassigned = 1L<<63;
	/** The state (0) that corresponds to a gap (inapplicable)*/
	public static final long inapplicable = 0L;
	/** The maximum allowed categorical state (60) */
	public static  final int maxElement = 60;
	/** The state (1L<<62) that corresponds to an invalid MesquiteSet.*/
	public static final long impossible = 1L<<62;
	
	private static final int MAXBIT = 63; //MAXBIT for bitwise manipulations
	
	/*..........................................MesquiteSet.....................................*/
	/**returns empty state set */
	public static long emptySet() {
		return 0L;  
	}
	/*..........................................MesquiteSet.....................................*/
	/**returns state set consisting of all elements */
	public static long fullSet() {
		return 0xFFFFFFFFFFFFFFFFL;  
	}
	/*.................................................................................................................*/
	/**Returns the set that is {0,...,(numInSet-1)}  That is, the first "numInSet" bits are set.
	This is designed for use with next set, to generate for instance all possible sets containing
	numInSet elements */
	public static long firstSet(int numInSet) { 
		if (numInSet==0)
			return 0;
		else
			return fullSet()>>>(64-numInSet);
	}
	/*.................................................................................................................*/
	/**When passed a set, returns the next set in sequence with the same
	number of elements and with maximum element maxS.  This can be called
	repeatedly (after starting with firstSet) to generate all sets with a
	certain number of elements.  When the current set is the last one,
	returns an empty set.    */
	//
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
				return 0;
			}
			else  {
				int newMax = maximum(result);
				return addToSet(result, newMax+1);
			}
		}
	}
	
	/*..........................................MesquiteSet.....................................*/
	/**returns true iff e is element in state set s */
	public static boolean isElement(long s, int e) {
		if ((e>maxElement) || (e<0))
			return false;
		else
			return (((1L<<e)&s)!=0);
	}
	
	/*..........................................MesquiteSet.....................................*/
	/**returns minimum element in state set s */
	public static int minimum(long s) {
		for (int e=0; e<=maxElement; e++) {
			if (((1L<<e)&s)!=0)
				return e;
		}
		return maxElement;  
	}
	/*..........................................MesquiteSet.....................................*/
	/**returns number of states in state set s */
	public static int cardinality(long s) {
		if (s==0L) {
		 	return 0;  
		}
		else {
			int count =0;
			for (int e=0; e<= maxElement; e++) {
				if (((1L<<e)&s)!=0) {  //test bit
					count++;
				}
			}
			return count;
		}
	}
	/*..........................................MesquiteSet.....................................*/
	/**return maximum value of states in state set s */
	public static int maximum(long s) {
		if (s==0L) {
		 	return -1;  
		}
		else {
			int max = -1;
			for (int e=0; e<= maxElement; e++) {
				if (((1L<<e)&s)!=0) {  //test bit
					max=e;
				}
			}
			return max;
		}
	}
	/*..........................................MesquiteSet.....................................*/
	/**returns true if e is only state in state set s */
	public static boolean isOnlyElement(long s, int e) {
		if ((e>maxElement) || (e<0))
			return false;
		else
			return ((1L<<e)==s);
	}
	/*..........................................MesquiteSet.....................................*/
	/**returns a state set including only the state e */
	public static long makeSet(int e) {
		if ((e<=maxElement) || (e>=0))
			return (1L<<e);
		else 
			return impossible;
	}

	/*..........................................MesquiteSet.....................................*/
	/**return a state set with all states a through b inclusive. */
	public static long span(int a, int b) {
		if (((a>maxElement) || (a<0)) || ((b>maxElement) || (b<0)))
			return impossible;
		else if (a>b) {
			int c=a;
			a = b;
			b = c;
		}
		return ((~(0L)) << a) & ((~(0L)) >>> (MAXBIT-b));
	}
	/*..........................................MesquiteSet.....................................*/
	/**return the result of adding state e to state set s */
	public static long addToSet(long s, int e) {
		if ((e<=maxElement) || (e>=0))
			return (1L<<e) | s;
		else 
			return s;
	}
	/*..........................................MesquiteSet.....................................*/
	/**return the result of subtracting state e to state set s */
	public static long clearFromSet(long s, int e) {
		if ((e<=maxElement) || (e>=0))
			return (~(1L<<e))&s;
		else 
			return s;
	}

	/*..........................................MesquiteSet.....................................*/
	/** converts passed long (treated as MesquiteSet) to string.  Uses braces, and does not use character state names*/
	public static String toString(long s) {
		return toString(s, true);
	}
	/*..........................................MesquiteSet.....................................*/
	/** converts passed long (treated as MesquiteSet) to string.  Uses character state names if available.*/
	public static String toString(long s, boolean useBraces) {
		boolean first=true;
		String temp;
		if (useBraces) 
			temp="{";
		else
			temp="";
		for (int e=0; e<=maxElement; e++) {
			if (isElement(s, e)) {
				if (!first)
					temp+=",";
				temp+=Integer.toString(e);
				first=false;
			}
		}
		if (useBraces)
			temp+="}";
		return temp;
	}
}


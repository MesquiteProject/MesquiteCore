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
/** A subclass of CategoricalState set for DNA (max state enforced at 3). */
public class DNAState extends MolecularState{
	public final static long A= 1L;
	public final static long C= 2L;
	public final static long G= 4L;
	public final static long T= 8L;
	public final static long a = setLowerCase(A, true);
	public final static long c = setLowerCase(C, true);
	public final static long g = setLowerCase(G, true);
	public final static long t = setLowerCase(T, true);
	public static final int maxDNAState = 3;
	
	public DNAState(long initial){
		super(initial);
	}
	public DNAState(){
		super();
	}
	public Class getCharacterDataClass() {
		return DNAData.class;
	}
	public String getDataTypeName(){
		return DNAData.DATATYPENAME;
	}
	public Class getMCharactersDistributionClass(){
		return MDNAAdjustable.class;
	}
	
	public static long fullSet(){
		return CategoricalState.span(0, maxDNAState);
	}
	public Class getCharacterDistributionClass() {
		return DNACharacterAdjustable.class;
	}
	public Class getCharacterHistoryClass() {
		return DNACharacterHistory.class;
	}
	public AdjustableDistribution makeAdjustableDistribution(Taxa taxa, int numNodes){
		return new DNACharacterAdjustable(taxa, numNodes);
	}
	public CharacterHistory makeCharacterHistory(Taxa taxa, int numNodes){
		return new DNACharacterHistory(taxa, numNodes);
	}
	
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
				if (e<4)
					temp += toString(e, isLowerCase(set));
				else
					temp+= Integer.toString(e); //there should be no such states, but just in case, show as integers
				first=false;
			}
		}
		if (first)
			temp += '!'; //no state found!
		return temp;
	}
	/*..........................................DNAState.....................................*/
	/**Returns the only element in state set s.  If more than one element, or no elements, returns -1. */
	public static int getOnlyElement(long s) {
		if (s == A)
			return 0;
		else if (s == C)
			return 1;
		else if (s == G)
			return 2;
		else if (s == T)
			return 3;
		return -1;  
	}
	/*..........................................DNAState.....................................*/
	/**return maximum value of states in state set s */
	public static int maximum(long s) {
		if (s==0L) {
		 	return -1;  
		}
		else {
			int max = -1;
			for (int e=0; e<= maxDNAState; e++) {
				if (((1L<<e)&s)!=0L) {  //test bit
					max=e;
				}
			}
			return max;
		}
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


	/*..........................................CategoricalState.....................................*/
	/**returns true iff state sets are same except for case */
	public static boolean equalsIgnoreCase(long s1, long s2) {
		return setLowerCase(s1, false) == setLowerCase(s2, false) ;
	}
	/*..........................................DNAState.....................................*/
	/** converts passed int (treated as DNAState) to string.  Uses character state names if available. �*/
	public static String toString(int e, boolean isLowerCase) {
		if (isLowerCase){
			if (e==0)
			return "a";
		else if (e==1)
			return "c";
		else if (e==2)
			return "g";
		else if (e==3)
			return "t";
		else
			return Integer.toString(e);
		}
		else {
			if (e==0)
				return "A";
			else if (e==1)
				return "C";
			else if (e==2)
				return "G";
			else if (e==3)
				return "T";
			else
				return Integer.toString(e);
		}
	}
	/*..........................................DNAState.....................................*/
	/** converts passed int (treated as DNAState) to string.  Uses character state names if available. �*/
	public static String toString(int e) {
		if (e==0)
			return "A";
		else if (e==1)
			return "C";
		else if (e==2)
			return "G";
		else if (e==3)
			return "T";
		else
			return Integer.toString(e);
	}
	/*..........................................DNAState.....................................*/
	/** converts passed int (treated as DNAState) to string.  Uses character state names if available. �*/
	public static char toChar(int e, CharacterData data, boolean lowerCase) {
		if (lowerCase){
			if (e==0)
				return 'a';
			else if (e==1)
				return 'c';
			else if (e==2)
				return 'g';
			else if (e==3) {
				if (data == null || !(data instanceof DNAData) || !((DNAData)data).displayAsRNA)
					return 't';
				else
					return 'u';
			}
			else
				return '!';
		}
		if (e==0)
			return 'A';
		else if (e==1)
			return 'C';
		else if (e==2)
			return 'G';
		else if (e==3) {
			if (data == null || !(data instanceof DNAData) || !((DNAData)data).displayAsRNA)
				return 'T';
			else
				return 'U';
		}
		else
			return '!';
	}
	/*..........................................DNAState.....................................*/
	/** converts passed long array (treated as DNAStates) to string.  */
	public static String toString(long[] s) {
		if (s == null)
			return null;
		String seq = "";
		for (int i=0; i<s.length; i++)
		 seq += toCharStatic(s[i]);
		return seq;
	}
	/*..........................................DNAState.....................................*/
	/** converts passed long array (treated as DNAStates) to string.  */
	public static String toStringRow(long[][] s, int row) {
		if (s == null || s.length == 0 || row >= s[0].length)
			return null;
		String seq = "";
		for (int i=0; i<s.length; i++)
		 seq += toCharStatic(s[i][row]);
		return seq;
	}
	/*..........................................DNAState.....................................*/
	/** converts passed long (treated as DNAState) to string.  Uses braces*/
	public static String toString(long s) {
		return toString(s, null, 0, true);
	}
	/*..........................................DNAState.....................................*/
	/** converts passed long (treated as DNAState) to string.  */
	public static String toString(long s, boolean useBraces) {
		return toString(s, null, 0, useBraces);
	}
	/*..........................................DNAState.....................................*/
	/** converts passed long (treated as DNAState) to string.  .*/
	public static String toString(long s, CategoricalData data, int ic, boolean useBraces) {
		return toString(s, data, ic, useBraces, false);
	}
	/*..........................................DNAState.....................................*/
	/** converts passed long (treated as DNAState) to string. �*/
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
		for (int e=0; e<=maxDNAState; e++) {
			if (isElement(s, e)) {
				if (!first)
					temp+=" ";
				if (data != null) {
					temp+=data.getStateName(ic, e);
				}
				else if (e>=0 && e<=3)
					temp+= toChar(e, data, CategoricalState.isLowerCase(s));
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
	/*..........................................DNAState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public String toNEXUSString() {
		return DNAState.toNEXUSString(set);
	}
	/*..........................................DNAState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public static String toNEXUSString(long s) {
		if (isInapplicable(s))
			return "" + CharacterData.defaultInapplicableChar;
		if (isUnassigned(s))
			return "" + CharacterData.defaultMissingChar;
		return DNAData.getIUPACSymbol(s);
	}
	/*.................................................................................................................*/
	/** Returns whether or not the codon has any base that is a gap (inapplicable) */
	 		public static boolean hasInapplicable (long[] codon) {
 			if (codon==null && codon.length>=3)
 				return false;
 	 		return (isInapplicable(codon[0])|| isInapplicable(codon[1]) || isInapplicable(codon[2]));
		}
	/*.................................................................................................................*/
  	/** Returns whether or not the codon has any base that is ambiguous or otherwise has mulitple states */
 		public static boolean hasMultipleStates (long[] codon) {
 			if (codon==null && codon.length>=3)
 				return false;
 	 		return (hasMultipleStates(codon[0])|| hasMultipleStates(codon[1]) || hasMultipleStates(codon[2]));
		}
 		/* .......................................... DNAState .................................................. */
 		/** Returns Complement of a state */
 		public static long complement(long s) {
 			if (s == CategoricalState.unassigned || s == CategoricalState.impossible || s == CategoricalState.inapplicable)
 				return s;
 			long comp = CategoricalState.emptySet();

 			if (CategoricalState.isElement(s, 0)) // there is an A in the original
 				comp = CategoricalState.addToSet(comp, 3);
 			if (CategoricalState.isElement(s, 1)) // there is an C in the original
 				comp = CategoricalState.addToSet(comp, 2);
 			if (CategoricalState.isElement(s, 2)) // there is an G in the original
 				comp = CategoricalState.addToSet(comp, 1);
 			if (CategoricalState.isElement(s, 3)) // there is an T in the original
 				comp = CategoricalState.addToSet(comp, 0);
 			if (CategoricalState.isLowerCase(s))
 				comp = CategoricalState.setLowerCase(comp, true);
 			if (CategoricalState.isUncertain(s))
 				comp = CategoricalState.setUncertainty(comp, true);
 			return comp;
 		}
	/*..........................................DNAState.....................................*/
	/**Returns the state set contains a purine, i.e., A or G. */
	public boolean hasPurine() {   
		return isElement(0) || isElement (2);
	}
	/*..........................................DNAState.....................................*/
	/**Static method that returns the state set contains a purine, i.e., A or G. */
	public static boolean hasPurine(long s) {   
		return isElement(s, 0) || isElement (s, 2);
	}
	/*..........................................DNAState.....................................*/
	/**Returns the state set contains a pyrimidine, i.e., C or T. */
	public boolean hasPyrimidine() {   
		return isElement(1) || isElement (3);
	}
	/*..........................................DNAState.....................................*/
	/**Static method that eturns the state set contains a pyrmidine, i.e., C or T. */
	public static boolean hasPyrimidine(long s) {   
		return isElement(s, 1) || isElement (s, 3);
	}
	/*..........................................DNAState.....................................*/
	/**return the state set containing the state represented by the character (e.g., '0' to {0}) �*/
	public long fromChar(char c) { 
		return fromCharStatic(c);
	}
	/*..........................................DNAState.....................................*/
	/**return the state set containing the state represented by the character (e.g., '0' to {0}) �*/
	public static long fromCharStatic(char c) { 
		if (c == '?')
			return unassigned;
		else if (c == '-')
			return inapplicable;
		else if (c == 'A') 
			return A;
		else if (c == 'C')
			return C;
		else if (c == 'G')
			return G;
		else if (c == 'T' || c == 'U')
			return T;
 		else if (c == 'a') 
			return CategoricalState.setLowerCase(A, true);
		else if (c == 'c')
			return CategoricalState.setLowerCase(C, true);
		else if (c == 'g')
			return CategoricalState.setLowerCase(G, true);
		else if (c == 't' || c == 'u')
			return CategoricalState.setLowerCase(T, true);
		else if (c == 'R' || c == 'r') // A or G
			return CategoricalState.setUncertainty(A | G, true);
		else if (c == 'Y' || c == 'y') // C or T
			return CategoricalState.setUncertainty(C | T, true);
		else if (c == 'B' || c == 'b')  // 123
			return CategoricalState.setUncertainty(C | G | T, true);
		else if (c == 'D' || c == 'd')  // 023
			return CategoricalState.setUncertainty(A | G | T, true);
		else if (c == 'H' || c == 'h')  // 013
			return CategoricalState.setUncertainty(A | C | T, true);
		else if (c == 'K' || c == 'k')  // 23
			return CategoricalState.setUncertainty(G | T, true);
		else if (c == 'M' || c == 'm')  // 01
			return CategoricalState.setUncertainty(A | C , true);
		else if (c == 'S' || c == 's')  // 12
			return CategoricalState.setUncertainty(C | G, true);
		else if (c == 'V' || c == 'v')  // 012
			return CategoricalState.setUncertainty(A | C | G, true);
		else if (c == 'W' || c == 'w')  // 03
			return CategoricalState.setUncertainty(A | T, true);
		else if (c == 'N' || c == 'n' || c == 'X' || c == 'x')  // ?
			return unassigned;
		int s = Character.digit(c, 10);
		if (s>=0 && s<= DNAState.maxDNAState)
			return CategoricalState.makeSet(s);
  		return CategoricalState.impossible;
	}
	/*..........................................DNAState.....................................*/
	/**return the state set containing the state represented by the character (e.g., '0' to {0}) �*/
	public static char toCharStatic(long c) { 
		if (c == unassigned)
			return '?';
		else if (c == inapplicable)
			return '-';
		c = c & statesBitsMask;
		if (c == A) //should make this more efficient by using constants!
			return 'A';
		else if (c == C)
			return 'C';
		else if (c == G)
			return 'G';
		else if (c == T)
			return 'T';
 		else if (c == (A | C) )
 			return 'M';
		else if (c == (A  | G)  )
 			return 'R';
		else if (c == (A  | T)  )
 			return 'W';
		else if (c ==  (C | G )  )
 			return 'S';
		else if (c ==  (C |  T)  )
 			return 'Y';
		else if (c ==   (G | T)  )
 			return 'K';
		else if (c == (A | C | G )  )
 			return 'V';
		else if (c == (A | C | T)  )
 			return 'H';
		else if (c == (A |  G | T)  )
 			return 'D';
		else if (c ==  (C | G | T) ) 
 			return 'B';
		else if (c == (A | C | G | T)  )
 			return 'N';
 		return '!';
	}

	/*..........................................CategoricalState.....................................*/
	/**returns the maximum possible state */
	public static int getMaxPossibleStateStatic() {
		return maxDNAState;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the maximum possible state */
	public  int getMaxPossibleState() {
		return maxDNAState;
	}
}


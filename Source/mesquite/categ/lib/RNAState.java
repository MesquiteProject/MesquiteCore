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
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/* ======================================================================== */
/** A subclass of CategoricalState for RNA data */
public class RNAState extends DNAState{
	public RNAState(long initial){
		super(initial);
	}
	public RNAState(){
		super();
	}
	public Class getCharacterDataClass() {
		return RNAData.class;
	}
	public Class getMCharactersDistributionClass(){
		return MCategoricalDistribution.class;
	}
	public Class getCharacterDistributionClass() {
		return RNACharacterAdjustable.class;
	}
	public Class getCharacterHistoryClass() {
		return RNACharacterHistory.class;
	}
	public String getDataTypeName(){
		return RNAData.DATATYPENAME;
	}
	public static long fullSet(){
		return CategoricalState.span(0, 3);
	}
	public AdjustableDistribution makeAdjustableDistribution(Taxa taxa, int numNodes){
		return new RNACharacterAdjustable(taxa, numNodes);
	}
	public CharacterHistory makeCharacterHistory(Taxa taxa, int numNodes){
		return new RNACharacterHistory(taxa, numNodes);
	}
	/*..........................................RNAState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public String toNEXUSString() {
		return RNAState.toNEXUSString(set);
	}
	/*..........................................RNAState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public static String toNEXUSString(long s) {
		if (isInapplicable(s))
			return "" + CharacterData.defaultInapplicableChar;
		if (isUnassigned(s))
			return "" + CharacterData.defaultMissingChar;
		return RNAData.getIUPACSymbol(s);
	}
	/*..........................................RNAState.....................................*/
	/** converts passed int (treated as RNAState) to string.  Uses character state names if available. �*/
	public static String toString(int e) {
		if (e==0)
			return "A";
		else if (e==1)
			return "C";
		else if (e==2)
			return "G";
		else if (e==3)
			return "U";
		else
			return Integer.toString(e);
	}
	/*..........................................RNAState.....................................*/
	/** Returns string as would be displayed to user (not necessarily internal shorthand).  Repeated from DNAState so it uses RNAState's toString(e) �*/
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
					temp += toString(e);
				else
					temp+= Integer.toString(e); //there should be no such states, but just in case, show as integers
				first=false;
			}
		}
		if (first)
			temp += '!'; //no state found!
		return temp;
	}
	/*..........................................RNAState.....................................*/
	/** converts passed long (treated as RNAState) to string.  Uses braces*/
	public static String toString(long s) {
		return toString(s, null, 0, true);
	}
	/*..........................................RNAState.....................................*/
	/** converts passed long (treated as RNAState) to string.  */
	public static String toString(long s, boolean useBraces) {
		return toString(s, null, 0, useBraces);
	}
	/*..........................................RNAState.....................................*/
	/** converts passed long (treated as RNAState) to string.  .*/
	public static String toString(long s, CategoricalData data, int ic, boolean useBraces) {
		return toString(s, data, ic, useBraces, false);
	}
	/*..........................................RNAState.....................................*/
	/** converts passed long (treated as RNAState) to string.  Repeated from DNAState so that it uses RNAState's toChar. �*/
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
	/*..........................................RNAState.....................................*/
	/** converts passed int (treated as RNAState) to string.  Uses character state names if available. �*/
	public static char toChar(int e, CharacterData data, boolean lowerCase) {
		if (lowerCase){
			if (e==0)
				return 'a';
			else if (e==1)
				return 'c';
			else if (e==2)
				return 'g';
			else if (e==3) 
				return 'u';
			else
				return '!';
		}
		if (e==0)
			return 'A';
		else if (e==1)
			return 'C';
		else if (e==2)
			return 'G';
		else if (e==3) 
			return 'U';
		else
			return '!';
	}
}


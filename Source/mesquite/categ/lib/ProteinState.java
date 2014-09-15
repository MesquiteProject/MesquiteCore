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
/** A subclass of CategoricalState set for Protein (max state enforced at 4). */
public class ProteinState extends MolecularState{
	public static final int maxProteinState = ProteinData.proteinSymbols.length-1;
	public ProteinState(long initial){
		super(initial);
	}
	public ProteinState(){
		super();
	}
	public Class getCharacterDataClass() {
		return ProteinData.class;
	}
	public String getDataTypeName(){
		return ProteinData.DATATYPENAME;
	}
	public Class getMCharactersDistributionClass(){
		return MProteinAdjustable.class;
	}
	public Class getCharacterDistributionClass() {
		return ProteinAdjustable.class;
	}
	public Class getCharacterHistoryClass() {
		return ProteinCharacterHistory.class;
	}
	public AdjustableDistribution makeAdjustableDistribution(Taxa taxa, int numNodes){
		return new ProteinAdjustable(taxa, numNodes);
	}
	public CharacterHistory makeCharacterHistory(Taxa taxa, int numNodes){
		return new ProteinCharacterHistory(taxa, numNodes);
	}
	/*..........................................ProteinState.....................................*/
	public static long fullSet(){
		return CategoricalState.span(0, maxProteinState);
	}
	/*..........................................ProteinState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public String toNEXUSString() {
		return ProteinState.toNEXUSString(set);
	}
	/*..........................................ProteinState.....................................*/
	/** converts passed long (treated as CategoricalState) to string.  Uses default symbols for states. */
	public static String toNEXUSString(long s) {
		if (isInapplicable(s))
			return "" + CharacterData.defaultInapplicableChar;
		if (isUnassigned(s))
			return "" + CharacterData.defaultMissingChar;
		String temp="";
		for (int e=0; e<=maxCategoricalState; e++) {
			if (((1L<<e)&s)!=0L) {
				temp+=ProteinData.getDefaultStateSymbol(e);
			}
		}
		if (temp.length()>1)
			temp ="{" + temp + "}";
		return temp;
	}
	/*..........................................ProteinState.....................................*/

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
		for (int e=0; e<=maxProteinState; e++) {
			if (((1L<<e)&set)!=0) {
				if (!first)
					temp+=sep;
				temp+=ProteinData.proteinSymbols[e];
				first=false;
			}
		}
		if (first)
			temp += '!'; //no state found!
		return temp;
	}
	/*..........................................ProteinState.....................................*/
	/** converts passed long (treated as ProteinState) to string.  Uses character state names if available.*/
	public static String toString(int e) {
		if (e<0 || e>maxProteinState)
			return "impossible";
		return ProteinData.symbolsAsStrings[e];
	}
	/*..........................................ProteinState.....................................*/
	/** converts passed long (treated as ProteinState) to string.  .*/
	public static String toString(long s, boolean useBraces) {
		return toString(s, null, 0, useBraces);
	}
	/*..........................................ProteinState.....................................*/
	/** converts passed long (treated as ProteinState) to string.  .*/
	public static String toString(long s) {
		return toString(s, null, 0, true);
	}
	/*..........................................ProteinState.....................................*/
	/** converts passed long (treated as ProteinState) to string.  .*/
	public static String toString(long s, CategoricalData data, int ic, boolean useBraces) {
		return toString(s, data, ic, useBraces, false);
	}
	/*..........................................ProteinState.....................................*/
	/** converts passed long (treated as ProteinState) to string.  Uses character state names if available.*/
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
		for (int e=0; e<=maxProteinState; e++) {
			if (isElement(s, e)) {
				if (!first)
					temp+=" ";
				temp+=ProteinData.symbolsAsStrings[e];
				first=false;
			}
		}
		if (first)
			temp += '!'; //no state found!
		if (useBraces)
			temp+="}";
		return temp;
	}
	/*..........................................ProteinState.....................................*/
	/**return the state set containing the state represented by the character (e.g., '0' to {0}) �*/
	public long fromChar(char c) { 
		return fromCharStatic(c);
	}
	/*..........................................ProteinState.....................................*/
	/**return the state set containing the state represented by the character (e.g., '0' to {0}) �*/
	public static long fromCharStatic(char c) { 
		if (c == '?')
			return unassigned;
		if (c == '-')
			return inapplicable;
   		if (c == 'B' || c == 'b')  // 2, 11
   				return CategoricalState.setUncertainty(CategoricalState.makeSet(2, 11), true);
   		else if (c == 'Z' || c == 'z')  // 3, 13
   				return CategoricalState.setUncertainty(CategoricalState.makeSet(3, 13), true);
		else {
			char C;
			if (c == 'j' || c == 'J'){ //translate j to w
				c = 'w';
				C = 'W';
			}
			else
				C = Character.toUpperCase(c);
			for (int i = 0; i<=ProteinState.maxProteinState; i++) //go through all default symbols to find c
				if (c==ProteinData.proteinSymbols[i] || C==ProteinData.proteinSymbols[i])
					return CategoricalState.makeSet(i);
  		}
  		return CategoricalState.impossible;
	}

	/*..........................................CategoricalState.....................................*/
	/**returns the maximum possible state */
	public static int getMaxPossibleStateStatic() {
		return maxProteinState;
	}
	/*..........................................CategoricalState.....................................*/
	/**returns the maximum possible state */
	public  int getMaxPossibleState() {
		return maxProteinState;
	}
}



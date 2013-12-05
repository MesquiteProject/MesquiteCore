/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.meristic.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;
/* ======================================================================== */
/**This class provides some basic utilities for Meristic characters.  It represents the entry for one character
and one taxon.  Thus, it can contain more than one item (min, max, mean, variance, etc.).*/
public class MeristicState extends CharacterState implements ItemContainer {
	/** The state (MesquiteInteger.unassigned) that corresponds to missing data.*/
	public static final int unassigned = MesquiteInteger.unassigned; 
	/** The state (MesquiteInteger.inapplicable) that corresponds to a gap (inapplicable data).*/
	public static final int inapplicable = MesquiteInteger.inapplicable;
	/** The state (MesquiteInteger.infinite) that corresponds to infinite.*/
	public static final int infinite = MesquiteInteger.infinite;
	/** The state (MesquiteInteger.impossible) that corresponds to an impossible value.  
	This is returned when a value is invalid, e.g. a string is parsed for a number but no valid number is found.*/
	public static final int impossible = MesquiteInteger.impossible;
	/** The maximum allowed number of items*/
	public static final int MAXITEMS = 32;
	/** The symbol that corresponds to a gap (inapplicable data).*/
	public static final char inapplicableChar = 'x';
	
	/* the stored values (for the various items)*/
	int[] values;
	/* names of the items*/
	NameReference[] names;
	/*number of items*/
	int numItems=0;
	
	public MeristicState () {
		numItems = MAXITEMS;
		values = new int[MAXITEMS];
		names = new NameReference[MAXITEMS];
		for (int i=0; i<MAXITEMS; i++) {
			values[i] = MesquiteInteger.inapplicable;
			names[i] = null;
		}
	}
	public MeristicState (int value) {
		numItems = MAXITEMS;
		values = new int[MAXITEMS];
		names = new NameReference[MAXITEMS];
		for (int i=0; i<MAXITEMS; i++) {
			values[i] = MesquiteInteger.inapplicable;
			names[i] = null;
		}
		values[0] = value;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return MeristicData.DATATYPENAME;
	}
	/*..........................................MeristicState................*/
	/** returns the maximum number of items (min, max, mean, variance, etc.) in this state*/
//	public int getMaxItems(){
//		return 32;
//	}
	/*..........................................MeristicState................*/
	/** returns the number of items (min, max, mean, variance, etc.) in this state*/
	public int getNumItems(){
		return numItems;
	}
	/*..........................................MeristicState................*/
	/** sets the number of items in this state*/
	public void setNumItems(int n){
		if (n<=MAXITEMS)
			numItems = n;
		else if (n>=0)
			numItems = n;
		for (int i=numItems; i<MAXITEMS; i++) {
			values[i] = MesquiteInteger.unassigned;
			names[i] = null;
		}
	}
	/*..........................................MeristicState................*/
	/** gets name of item n*/
	public String getItemName(int n){
		if (names != null && n>=0 && n<numItems) {
			NameReference nr = names[n];
			if (nr==null)
				return null;
			return nr.getValue();
		}
		else
			return null;
	}
	/*..........................................MeristicState................*/
	/** gets NameReferece of item n*/
	public NameReference getItemReference(String name){
		return NameReference.getNameReference(name);
	}
	/*..........................................MeristicState................*/
	/** gets NameReferece of item n*/
	public NameReference getItemReference(int n){
		if (names != null && n>=0 && n<numItems)
			return names[n];
		else
			return null;
	}
	/*..........................................MeristicState................*/
	/** sets NameReference of item n*/
	public void setItemReference(int n, NameReference nr){
		if (n>=0 && n<numItems)
			names[n] = nr;
	}
	/*..........................................MeristicState................*/
	/** returns which item number has NameReference nr nr*/
	public int getItemNumber(NameReference nr){
		if (nr==null)
			return -1;
		for (int i=0; i<numItems; i++)
			if (nr.equals(names[i]))
				return i;
		return -1;
	}
	/*..........................................MeristicState................*/
	public void setItemsAs(ItemContainer iCont){
		if (iCont == null)
			return;
		setNumItems(iCont.getNumItems());
		for (int i=0; i<getNumItems(); i++)
			setItemReference(i, iCont.getItemReference(i));
	}
	/*..........................................MeristicState................*/
	public Class getCharacterDataClass() {
		return MeristicData.class;
	}
	public Class getMCharactersDistributionClass(){
		return MMeristicAdjustable.class;
	}
	public Class getCharacterDistributionClass(){
		return MeristicAdjustable.class;
	}
	public Class getCharacterHistoryClass(){
		return MeristicHistory.class;
	}
	public AdjustableDistribution makeAdjustableDistribution(Taxa taxa, int numNodes){
		return new MeristicAdjustable(taxa, numNodes);
	}
	public CharacterHistory makeCharacterHistory(Taxa taxa, int numNodes){
		return new MeristicHistory(taxa, numNodes, null);
	}
	/*..........................................MeristicState................*/
	/** returns true if missing data (unassigned)*/
	public static boolean isUnassigned(int s) {
		return s == unassigned;
	}
	/*..........................................MeristicState................*/
	/** returns true if all items have missing data (unassigned)*/
	public boolean isUnassigned() {
		for (int i=0; i<numItems; i++)
			if (values[i] != MesquiteInteger.unassigned)
				return false;
		return true;
	}
	/*..........................................MeristicState................*/
	/** returns true if inapplicable*/
	public static boolean isInapplicable(int s) {
		return s == inapplicable;
	}
	/*..........................................MeristicState................*/
	/** returns true if all items are inapplicable*/
	public boolean isInapplicable() { //check all items
		boolean inappPresent = false;
		for (int i=0; i<numItems; i++)
			if (values[i] == MesquiteInteger.inapplicable)
				inappPresent = true;
			else if (values[i] != MesquiteInteger.unassigned)
				return false;
		return inappPresent;
	}
	/*..........................................MeristicState................*/
	/** returns true if value is invalid*/
	public static boolean isImpossible(int s) {
		return s == impossible;
	}
	/*..........................................MeristicState................*/
	/** returns true if at least one item is invalid*/
	public boolean isImpossible() { //check all items
		for (int i=0; i<numItems; i++)
			if (values[i] == MesquiteInteger.impossible)
				return true;
		return false;
	}
	/*..........................................MeristicState.....................................*/
	/** returns true if at least one item is combinable (i.e. a valid assigned state).*/
	public boolean isCombinable(){
		for (int i=0; i<numItems; i++)
			if (MesquiteInteger.isCombinable(values[i]))
				return true;
		return false;
	}
	/*..........................................MeristicState.....................................*/
	/** returns whether value is combinable (i.e. a valid assigned state) or not.*/
	public static boolean isCombinable(int d){
		return MesquiteInteger.isCombinable(d);
	}
	/*..........................................MeristicState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s) {
		return equals(s, false);
	}
	/*..........................................MeristicState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s, boolean allowMissing) {
		if (s==null)
			return false;
		if (!(s instanceof MeristicState))
			return false;
		if (allowMissing && (isUnassigned() || s.isUnassigned())) //fixed 13 Dec 01
				return true;
		MeristicState cS = (MeristicState)s;
		if (cS.getNumItems()!=getNumItems())
			return false;
		for (int i=0; i<getNumItems(); i++)
			if  (cS.getValue(i) != getValue(i))
				return false;
		return true;
	}
	/*..........................................MeristicState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s, boolean allowMissing, boolean allowNearExact) {
		return equals(s,allowMissing);
	}
	/*..........................................MeristicState................*/
	public static int absolute(int d) {
		if (d== unassigned || d==inapplicable || d==impossible || d==infinite)
			return d;
		if (d<0)
			return -d;  
		else
			return d;
	}
	/*..........................................MeristicState................*/
	public static int maximum(int d1, int d2) {
		if (d1==d2)
			return d1;
		if (d2==infinite)
			return d2;
		if (d1==infinite)
			return d1;
			
		if (d1== unassigned || d1==inapplicable || d1==impossible) {
			if (d2== unassigned || d2==inapplicable || d2==impossible)
				return impossible;
			else
				return d2;
		}
		if (d2== unassigned || d2==inapplicable || d2==impossible)
			return d1;
		
		if (d1<d2)
			return d2;  
		else
			return d1;
	}
	/*..........................................MeristicState................*/
	public static int minimum(int d1, int d2) { 
		if (d1==d2)
			return d1;
		if (d1== unassigned || d1==inapplicable || d1==impossible  || d1==infinite) {
			if (d2== unassigned || d2==inapplicable || d2==impossible  || d2==infinite)
				return impossible;
			else
				return d2;
		}
		if (d2== unassigned || d2==inapplicable || d2==impossible  || d2==infinite)
			return d1;
		if (d1>d2)
			return d2;  
		else
			return d1;
	}
	/*..........................................MeristicState................*/
	/** sets the value to inapplicable*/
	public void setToInapplicable() {
			for (int i=0; i<numItems; i++) {
				values[i] = inapplicable;
			}
	}
	/*..........................................MeristicState................*/
	/** sets the value to unassigned*/
	public void setToUnassigned() {
			for (int i=0; i<numItems; i++) {
				values[i] = unassigned;
			}
	}
	/*..........................................MeristicState................*/
	/** sets value of item n */
	public void setValue(int n, int d){
		if (n>=0 && n<numItems)
			values[n] = d;
	}
	/*..........................................MeristicState................*/
	public void setValue(CharacterState cs){
		if (cs!=null && cs instanceof MeristicState) {
			MeristicState c = (MeristicState)cs;
			setNumItems(c.getNumItems());
			for (int i=0; i<numItems; i++) {
				values[i] = c.getValue(i);
				names[i] = c.getItemReference(i);
			}
		}
	}
	/*..........................................MeristicState................*
	/** sets its value to the value given by the String passed to it starting at position pos*/
	public void setValue(String st, CharacterData data){
		if (StringUtil.blank(st)){
			setNumItems(1);
			setValue(0, MesquiteInteger.unassigned);
			return;
		}
		MesquiteInteger pos = new MesquiteInteger(0);
		int n= 0;
		String s;
		int status = 1;
		while (pos.getValue()<st.length() && n < 32 && status>=0) { 
			char c = st.charAt(pos.getValue());
			if (c == '(') {
				status = 0;
				pos.increment();
			}
			else if (c == ')')
				status = -1;
			else if (c == ',' || c == ' ')
				pos.increment();
			else {
				if (status !=0)
					status = -1;
				String f =ParseUtil.getToken(st,pos); 
				char inapp = inapplicableChar;
				if (data != null)
					inapp = data.getInapplicableSymbol();
				int result = 0;
				if (f != null && f.length() == 1 && f.charAt(0) == inapp)
					result = inapplicable; 
				else
					result = MesquiteInteger.fromString(f, false); 
				setValue(n, result);
				n++;
			}
		}
		setNumItems(n);
	}
	/*..........................................MeristicState................*/
	/** gets the value of item # n*/
	public int getValue(int n){
		if (n>=0 && n<numItems)
			return values[n];
		else
			return unassigned;
	}
	/*..........................................MeristicState................*/
	/** gets the name value of item with NameReference NR*/
	public int getValue(NameReference nr){
		if (nr==null)
			return MesquiteInteger.unassigned;
		for (int i=0; i<numItems; i++)
			if (nr.equals(names[i]))
				return values[i];
		return MesquiteInteger.unassigned;
	}
	/*..........................................MeristicState................*
	/** sets its value to the value given by the String passed to it starting at position pos*
	public void setValue(String st, MesquiteInteger pos){
		if (StringUtil.blank(st)){
			setNumItems(1);
			setValue(0, MesquiteInteger.unassigned);
			return;
		}
		int n= 0;
		String s;
		int status = 1;
		while (pos.getValue()<st.length() && n < 32 && status>=0) { 
			char c = st.charAt(pos.getValue());
			if (c == '(') {
				status = 0;
				pos.increment();
			}
			else if (c == ')')
				status = -1;
			else if (c == ',' || c == ' ')
				pos.increment();
			else {
				if (status !=0)
					status = -1;
				setValue(n, fromString(st, pos));
				n++;
			}
		}
		setNumItems(n);
	}
	/** Returns integer parsed from given string, starting at string position given.
	Assumes only first token from pos is to be parsed. *
	public int fromString(String s, MesquiteInteger pos) {
		String f =ParseUtil.getToken(s,pos); 
		if ("-".equals(f))
			return inapplicable; 
		else
			return MesquiteInteger.fromString(f, false); 
	}
	/*..........................................MeristicState................*/
	private String valueToString(int v){
		return MeristicState.toString(v);
	}
	/*..........................................MeristicState................*/
	public static String toString(int v){
		if (v == MesquiteInteger.unassigned)
			return "?";
		else if (v == MesquiteInteger.inapplicable)
			return "" + MeristicState.inapplicableChar;
		else
			return MesquiteInteger.toString(v);
	}
	public String toString() {
		String s = "";
		boolean first = true;
		for (int i=0; i<numItems; i++) {
			String name = getItemName(i);
			if (name == null || "unnamed".equalsIgnoreCase(name)) {
				name = "";
				if (!first)
					s += " " + valueToString(values[i]);
				else
					s += valueToString(values[i]);
			}
			else {
				if (!first)
					s += " " + name + " " + valueToString(values[i]);
				else
					s += name + " " + valueToString(values[i]);
			}
			first = false;
		}
		return s;
	}
	/*..........................................MeristicState.....................................*/

	/** Returns string as would be displayed to user (not necessarily internal shorthand).  */
	public String toDisplayString(){
		String s = "";
		boolean first = true;
		if (numItems>1)
			s += "(";
		for (int i=0; i<numItems; i++) {
			if (!first)
				s += ", " + valueToString(values[i]);
			else
				s +=  valueToString(values[i]);
			first = false;
		}
		if (numItems>1)
			s += ")";
		return s;
	}
}



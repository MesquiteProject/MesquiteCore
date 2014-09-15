/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
/* ======================================================================== */
/**This class provides some basic utilities for Continuous characters.  It represents the entry for one character
and one taxon.  Thus, it can contain more than one item (min, max, mean, variance, etc.).*/
public class ContinuousState extends CharacterState implements ItemContainer {
	/** The state (MesquiteDouble.unassigned) that corresponds to missing data.*/
	public static final double unassigned = MesquiteDouble.unassigned; 
	/** The state (MesquiteDouble.inapplicable) that corresponds to a gap (inapplicable data).*/
	public static final double inapplicable = MesquiteDouble.inapplicable;
	/** The state (MesquiteDouble.infinite) that corresponds to infinite.*/
	public static final double infinite = MesquiteDouble.infinite;
	/** The state (MesquiteDouble.impossible) that corresponds to an impossible value.  
	This is returned when a value is invalid, e.g. a string is parsed for a number but no valid number is found.*/
	public static final double impossible = MesquiteDouble.impossible;
	/** The maximum allowed number of items*/
	public static final int MAXITEMS = 32;
	
	/* the stored values (for the various items)*/
	double[] values;
	/* names of the items*/
	NameReference[] names;
	/*number of items*/
	int numItems=0;
	
	public ContinuousState () {
		numItems = MAXITEMS;
		values = new double[MAXITEMS];
		names = new NameReference[MAXITEMS];
		for (int i=0; i<MAXITEMS; i++) {
			values[i] = MesquiteDouble.unassigned;
			names[i] = null;
		}
	}
	public ContinuousState (double value) {
		numItems = MAXITEMS;
		values = new double[MAXITEMS];
		names = new NameReference[MAXITEMS];
		for (int i=0; i<MAXITEMS; i++) {
			values[i] = MesquiteDouble.unassigned;
			names[i] = null;
		}
		values[0] = value;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return ContinuousData.DATATYPENAME;
	}
	/*..........................................ContinuousState................*/
	/** returns the maximum number of items (min, max, mean, variance, etc.) in this state*/
//	public int getMaxItems(){
//		return 32;
//	}
	/*..........................................ContinuousState................*/
	/** returns the number of items (min, max, mean, variance, etc.) in this state*/
	public int getNumItems(){
		return numItems;
	}
	/*..........................................ContinuousState................*/
	/** sets the number of items in this state*/
	public void setNumItems(int n){
		if (n<=MAXITEMS)
			numItems = n;
		else if (n>=0)
			numItems = n;
		for (int i=numItems; i<MAXITEMS; i++) {
			values[i] = MesquiteDouble.unassigned;
			names[i] = null;
		}
	}
	/*..........................................ContinuousState................*/
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
	/*..........................................ContinuousState................*/
	/** gets NameReferece of item n*/
	public NameReference getItemReference(String name){
		return NameReference.getNameReference(name);
	}
	/*..........................................ContinuousState................*/
	/** gets NameReferece of item n*/
	public NameReference getItemReference(int n){
		if (names != null && n>=0 && n<numItems)
			return names[n];
		else
			return null;
	}
	/*..........................................ContinuousState................*/
	/** sets NameReference of item n*/
	public void setItemReference(int n, NameReference nr){
		if (n>=0 && n<numItems)
			names[n] = nr;
	}
	/*..........................................ContinuousState................*/
	/** returns which item number has NameReference nr nr*/
	public int getItemNumber(NameReference nr){
		if (nr==null)
			return -1;
		for (int i=0; i<numItems; i++)
			if (nr.equals(names[i]))
				return i;
		return -1;
	}
	/*..........................................ContinuousState................*/
	public void setItemsAs(ItemContainer iCont){
		if (iCont == null)
			return;
		setNumItems(iCont.getNumItems());
		for (int i=0; i<getNumItems(); i++)
			setItemReference(i, iCont.getItemReference(i));
	}
	/*..........................................ContinuousState................*/
	public Class getCharacterDataClass() {
		return ContinuousData.class;
	}
	public Class getMCharactersDistributionClass(){
		return MContinuousAdjustable.class;
	}
	public Class getCharacterDistributionClass(){
		return ContinuousAdjustable.class;
	}
	public Class getCharacterHistoryClass(){
		return ContinuousHistory.class;
	}
	public AdjustableDistribution makeAdjustableDistribution(Taxa taxa, int numNodes){
		return new ContinuousAdjustable(taxa, numNodes);
	}
	public CharacterHistory makeCharacterHistory(Taxa taxa, int numNodes){
		return new ContinuousHistory(taxa, numNodes, null);
	}
	/*..........................................ContinuousState................*/
	/** returns true if missing data (unassigned)*/
	public static boolean isUnassigned(double s) {
		return s == unassigned;
	}
	/*..........................................ContinuousState................*/
	/** returns true if all items have missing data (unassigned)*/
	public boolean isUnassigned() {
		for (int i=0; i<numItems; i++)
			if (values[i] != MesquiteDouble.unassigned)
				return false;
		return true;
	}
	/*..........................................ContinuousState................*/
	/** returns true if inapplicable*/
	public static boolean isInapplicable(double s) {
		return s == inapplicable;
	}
	/*..........................................ContinuousState................*/
	/** returns true if all items are inapplicable*/
	public boolean isInapplicable() { //check all items
		boolean inappPresent = false;
		for (int i=0; i<numItems; i++)
			if (values[i] == MesquiteDouble.inapplicable)
				inappPresent = true;
			else if (values[i] != MesquiteDouble.unassigned)
				return false;
		return inappPresent;
	}
	/*..........................................ContinuousState................*/
	/** returns true if value is invalid*/
	public static boolean isImpossible(double s) {
		return s == impossible;
	}
	/*..........................................ContinuousState................*/
	/** returns true if at least one item is invalid*/
	public boolean isImpossible() { //check all items
		for (int i=0; i<numItems; i++)
			if (values[i] == MesquiteDouble.impossible)
				return true;
		return false;
	}
	/*..........................................ContinuousState.....................................*/
	/** returns true if at least one item is combinable (i.e. a valid assigned state).*/
	public boolean isCombinable(){
		for (int i=0; i<numItems; i++)
			if (MesquiteDouble.isCombinable(values[i]))
				return true;
		return false;
	}
	/*..........................................ContinuousState.....................................*/
	/** returns whether value is combinable (i.e. a valid assigned state) or not.*/
	public static boolean isCombinable(double d){
		return MesquiteDouble.isCombinable(d);
	}
	/*..........................................ContinuousState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s) {
		return equals(s, false);
	}
	/*..........................................ContinuousState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s, boolean allowMissing) {
		if (s==null)
			return false;
		if (!(s instanceof ContinuousState))
			return false;
		if (allowMissing && (isUnassigned() || s.isUnassigned())) //fixed 13 Dec 01
				return true;
		ContinuousState cS = (ContinuousState)s;
		if (cS.getNumItems()!=getNumItems())
			return false;
		for (int i=0; i<getNumItems(); i++)
			if  (cS.getValue(i) != getValue(i))
				return false;
		return true;
	}
	/*..........................................ContinuousState.....................................*/
	/**returns true iff state sets are same */
	public boolean equals(CharacterState s, boolean allowMissing, boolean allowNearExact) {
		return equals(s,allowMissing);
	}
	/*..........................................ContinuousState................*/
	public static double absolute(double d) {
		if (d== unassigned || d==inapplicable || d==impossible || d==infinite)
			return d;
		if (d<0)
			return -d;  
		else
			return d;
	}
	/*..........................................ContinuousState................*/
	public static double maximum(double d1, double d2) {
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
	/*..........................................ContinuousState................*/
	public static double minimum(double d1, double d2) { 
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
	/*..........................................ContinuousState................*/
	/** sets the value to inapplicable*/
	public void setToInapplicable() {
			for (int i=0; i<numItems; i++) {
				values[i] = inapplicable;
			}
	}
	/*..........................................ContinuousState................*/
	/** sets the value to unassigned*/
	public void setToUnassigned() {
			for (int i=0; i<numItems; i++) {
				values[i] = unassigned;
			}
	}
	/*..........................................ContinuousState................*/
	/** sets value of item n */
	public void setValue(int n, double d){
		if (n>=0 && n<numItems)
			values[n] = d;
	}
	/*..........................................ContinuousState................*/
	public void setValue(CharacterState cs){
		if (cs!=null && cs instanceof ContinuousState) {
			ContinuousState c = (ContinuousState)cs;
			setNumItems(c.getNumItems());
			for (int i=0; i<numItems; i++) {
				values[i] = c.getValue(i);
				names[i] = c.getItemReference(i);
			}
		}
	}
	/*..........................................ContinuousState................*/
	public void setValue(String st, CharacterData parentData) {
		MesquiteInteger pos = new MesquiteInteger(0);
		if (StringUtil.blank(st)){
			setNumItems(1);
			setValue(0, MesquiteDouble.unassigned);
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
				setValue(n, MesquiteDouble.fromString(st, pos));
				n++;
			}
		}
		setNumItems(n);
	}
	/*..........................................ContinuousState................*/
	/** gets the value of item # n*/
	public double getValue(int n){
		if (n>=0 && n<numItems)
			return values[n];
		else
			return unassigned;
	}
	/*..........................................ContinuousState................*/
	/** gets the name value of item with NameReference NR*/
	public double getValue(NameReference nr){
		if (nr==null)
			return MesquiteDouble.unassigned;
		for (int i=0; i<numItems; i++)
			if (nr.equals(names[i]))
				return values[i];
		return MesquiteDouble.unassigned;
	}
	/*..........................................ContinuousState................*
	/** sets its value to the value given by the String passed to it starting at position pos*
	public void setValue(String st, MesquiteInteger pos){
		if (StringUtil.blank(st)){
			setNumItems(1);
			setValue(0, MesquiteDouble.unassigned);
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
				setValue(n, MesquiteDouble.fromString(st, pos));
				n++;
			}
		}
		setNumItems(n);
	}

	/*..........................................ContinuousState................*/
	private String valueToString(double v){
		if (v == MesquiteDouble.unassigned)
			return "?";
		else if (v == MesquiteDouble.inapplicable)
			return "-";
		else
			return MesquiteDouble.toString(v);
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
	/*..........................................ContinuousState.....................................*/

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



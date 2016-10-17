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

/*Last documented:  August 1999 */

/* ======================================================================== */
/**This long wrapper class is used to be able to pass integers by reference and have the
	original change as needed.*/
public class MesquiteLong implements Listable {
	public static final long unassigned = Long.MIN_VALUE+1;
	public static final long inapplicable = Long.MAX_VALUE - 1;
	public static final long infinite = Long.MAX_VALUE;
	public static final long negInfinite = Long.MIN_VALUE;
	public static final long impossible = Long.MAX_VALUE - 2;
	private long value;
	public static long totalCreated = 0;
	private String name = null;
	
	public MesquiteLong(long value) {
		this.value=value;
		totalCreated++;
	}
	
	public MesquiteLong(String name, long value) {
		this.value=value;
		this.name = name;
		totalCreated++;
	}
	public MesquiteLong() {
		this.value=unassigned;
		totalCreated++;
	}
	public void setName(String s){
		name = s;
	}
	public String getName(){
		return name;
	}
	/*--------------------------------GET/SET--------------------------*/
	/** Returns value as a long */
	public long getValue() {
		return value;
	}

	/** Sets value */
	public void setValue(String s) {
		this.value=fromString(s);
	}
	/** Sets value */
	public void setValue(long value) {
		this.value=value;
	}
	/** Adds one to value */
	public void increment() {
		value++;
	}
	/** Subtracts one from value */
	public void decrement() {
		value--;
	}
	/** Sets value to unassigned */
	public void setToUnassigned() {
		value=unassigned;
	}
	/*--------------------------------CONVERSION--------------------------*/
	public static long toLong(int v) {
		if (v==MesquiteInteger.unassigned)
			return MesquiteLong.unassigned;
		else if (v==MesquiteInteger.infinite)
			return MesquiteLong.infinite;
		else if (v==MesquiteInteger.negInfinite)
			return MesquiteLong.negInfinite;
		else if (v==MesquiteInteger.impossible)
			return MesquiteLong.impossible;
		else if (v==MesquiteInteger.inapplicable)
			return MesquiteLong.inapplicable;
		else
			return v;
	}
	/*--------------------------------QUERY--------------------------*/
	/** Returns whether value is unassigned */
	public boolean isUnassigned() {
		return (value==unassigned);
	}
	/** Returns whether value is unassigned */
	public static boolean isUnassigned(long i) {
		return (i==unassigned);
	}
	/** Returns whether value is infinite */
	public boolean isInfinite() {
		return (value==infinite || value == negInfinite);
	}
	/** Returns whether value is infinite */
	public static boolean isInfinite(long i) {
		return (i==infinite || i == negInfinite);
	}
	/** Returns whether value is impossible */
	public boolean isImpossible() {
		return (value==impossible);
	}
	/** Returns whether value is impossible */
	public static boolean isImpossible(long i) {
		return (i==impossible);
	}
	/** Returns whether characteristic is inapplicable */
	public boolean isInapplicable() {
		return (value==inapplicable);
	}
	/** Returns whether characteristic is inapplicable */
	public static boolean isInapplicable(long i) {
		return (i==inapplicable);
	}
	/** Returns whether value is a regular number (NOT unassigned, infinite, inapplicable, impossible) */
	public boolean isCombinable() {
		return (value!=unassigned && value!=infinite && value!=impossible&& value!=inapplicable);
	}
	/** Returns whether value is a regular number (NOT unassigned, infinite, inapplicable, impossible) */
	public static boolean isCombinable(long i) {
		return (i!=unassigned && i!=infinite && i!=impossible&& i!=inapplicable);
	}
	/*--------------------------------MIN-MAX--------------------------*/
	/** Returns maximum of two long integers.  Accounts for infinite, unassigned, etc. */
	public static long maximum(long a, long b) {
		if (a == unassigned || a == inapplicable || a == negInfinite || a == impossible) {
			if (b==unassigned || b == inapplicable || b== negInfinite || b == impossible)
				return unassigned;
			else
				return b;
		}
		else if (b==unassigned || b == inapplicable || b== negInfinite || b == impossible)
			return a;
		else if (a==infinite)
			return a;
		else if (b==infinite)
			return b;
		else if (a>b)
			return a;
		else
			return b;
	}
	/** Returns minimum of two long integers.  Accounts for infinite, unassigned, etc. */
	public static long minimum(long a, long b) {
		if (a == unassigned || a == inapplicable || a == infinite || a == impossible){
			if (b==unassigned || b == inapplicable || b== infinite || b == impossible)
				return unassigned;
			else
				return b;
		}
		else if (b==unassigned || b == inapplicable || b== infinite || b == impossible)
			return a;
		else if (a==negInfinite)
			return a;
		else if (b==negInfinite)
			return b;
		else if (a<b)
			return a;
		else
			return b;
	}
	/*--------------------------------Arithmetics--------------------------*/
	/** turns value to absolute value. */
	public void abs() {
		if (isCombinable(value))
			value = Math.abs(value);
	}
	/** Adds to current value.  Considers infinite, unassigned etc. */
	public void add(long toAdd) {
		value = add(toAdd, value);
	}
	/** Subtracts from current value.  Considers infinite, unassigned etc. */
	public void subtract(long toSubtract) {
		value = subtract(value, toSubtract);
	}
	/** Adds two doubles.  Considers infinite, unassigned etc. */
	public static long add(long value1, long value2) {
		if (value1 == infinite){
			if (value2== negInfinite)
				return 0;
			else
				return infinite;
		}
		else if (value2 == infinite){
			if (value1== negInfinite)
				return 0;
			else
				return infinite;
		}
		else if (value1 == negInfinite)
			return negInfinite;
		else if (value2 == negInfinite)
			return negInfinite;
		else if (isCombinable(value1)) {
			if (isCombinable(value2))
				return value1 + value2;
			else
				return value1;
		}
		else
			return value2;
	}
	/** Subtracts value2 from value 1.  Considers infinite, unassigned etc. */
	public static long subtract(long value1, long value2) {
		if (value2 == negInfinite){
			if (value1== negInfinite)
				return 0;
			else
				return infinite;
		}
		else if (value2 == infinite){
			if (value1== infinite)
				return 0;
			else
				return negInfinite;
		}
		else if (value1 == infinite)
			return infinite;
		else if (value1 == negInfinite)
			return negInfinite;
		else if (isCombinable(value2)) {
			if (isCombinable(value1))
				return value1 - value2;
			else
				return -value2;
		}
		else
			return value1;
	}
	/** Multiplies two longs.  Considers infinite, unassigned etc. */
	public static long multiply(long value1, long value2) {
		if (value1 == 0 || value2==0)
			return 0;
		else if (isCombinable(value1)){
			if (isCombinable(value2))
				return value1*value2;
			else {  //value 2 not combinable
				if (value2 == infinite){
					if (value1<0)
						return negInfinite;
					else 
						return infinite;
				}
				else if (value2== negInfinite) {
					if (value1<0)
						return infinite;
					else 
						return negInfinite;
				}
				else
					return unassigned;
			}
		}
		else {//value 1 not combinable
			if (isCombinable(value2)){
				if (value1 == infinite){
					if (value2<0)
						return negInfinite;
					else 
						return infinite;
				}
				else if (value1== negInfinite) {
					if (value2<0)
						return infinite;
					else 
						return negInfinite;
				}
				else
					return unassigned;
			}
			else { //neither combinable
				if (value1 == infinite && value2 == infinite)
					return infinite;
				else if (value1 == infinite && value2 == negInfinite)
					return negInfinite;
				else if (value1 == negInfinite && value2 == infinite)
					return negInfinite;
				else if (value1 == negInfinite && value2 == negInfinite)
					return infinite;
				else 
					return unassigned;
			}
		}
		
	}
	/** Divides first long by second long.  Considers infinite, unassigned etc. */
	public static double divide(long value1, long value2) {
		if (value2==0)
			return MesquiteDouble.infinite;
		else if (value1 == 0)
			return 0;
		else if (isCombinable(value1)){
			if (isCombinable(value2))
				return ((double)value1)/value2;
			else {  //value 2 not combinable
				if (value2 == infinite || value2== negInfinite)
					return 0;
				else
					return MesquiteDouble.unassigned;
			}
		}
		else {//value 1 not combinable
			if (isCombinable(value2)){
				return MesquiteDouble.unassigned;
			}
			else { //neither combinable
				if (value2 == infinite || value2== negInfinite)
					return 0;
				else
					return MesquiteDouble.unassigned;
			}
		}
	}
	/*--------------------------------STRINGS--------------------------*/
	/** Returns string version of this value.  Returns "unassigned" etc. if needed*/
	public String toString() {
		return toString(value);
	}
	
	/** Returns string version of value.  Returns "unassigned" etc. if needed*/
	public static String toString(long v) {
		if (v==unassigned)
			return "?";  //changed from "unassigned" June 02
		else if (v==infinite)
			return "infinite";
		else if (v==negInfinite)
			return "neg.infinite";  
		else if (v==impossible)
			return "impossible";  
		else if (v==inapplicable)
			return "inapplicable"; 
		else {
			try {
				return Long.toString(v);
			}
			catch (NumberFormatException ne){
				return "impossible";
			}
		}
	}
	/** Returns long parsed from given string.  Assumes entire string is what should be parsed. */
	public static long fromString(String s) { 
		return fromString(s, false);
	}
	/** Returns long parsed from given string; warns about NumberFormatException if boolean is true.
	  Assumes entire string is what should be parsed. */
	public static long fromString(String s, boolean warn) {
		if (StringUtil.blank(s))
			return impossible;
		try {
			return Long.parseLong(s);
		}
		catch (NumberFormatException e) {
			if (s !=null) {
				if (s.equalsIgnoreCase("unassigned") || s.equalsIgnoreCase("estimate") || s.equalsIgnoreCase("?"))
					return unassigned;
				else if (s.equalsIgnoreCase("infinite")|| s.equalsIgnoreCase("i"))
					return infinite;
				else if (s.equalsIgnoreCase("impossible"))
					return impossible;
				else if (s.equalsIgnoreCase("neg.Infinite"))
					return negInfinite;
				else if (s.equalsIgnoreCase("inapplicable"))
					return inapplicable;
			}
			if (warn)
				MesquiteMessage.println("NumberFormatException in parsing string \"" + s + "\"");
			return impossible;
		}
	}
	/** Returns long parsed from given string, starting at string position given.
	Assumes only first token from pos is to be parsed. */
	public static long fromString(String s, MesquiteInteger pos) {
		String f =ParseUtil.getToken(s,pos); 
		if ("-".equals(f))
			return -fromString(ParseUtil.getToken(s,pos), false); 
		else
			return fromString(f, false); 
	}
	/** Returns long parsed from first token of given string, and sets value of pos
	to character at end of this token. */
	public static long fromFirstToken(String s, MesquiteInteger pos) {
		pos.setValue(0);
		return fromString(s,pos);
	}
	/** Returns whether a string can be interpreted as a long. */
	public static boolean interpretableAsLong(String s,  MesquiteInteger pos) {
		int oldPos = pos.getValue();
		boolean interpretable = fromString(s, pos)!= impossible;
		pos.setValue(oldPos);
		return interpretable;
	}	
	/** Returns whether a string can be interpreted as a long. */
	public static boolean interpretableAsLong(String s,  MesquiteInteger pos, int posToSet) {
		int oldPos = pos.getValue();
		if (posToSet >=0)
			pos.setValue(posToSet);
		boolean interpretable = fromString(s, pos)!= impossible;
		pos.setValue(oldPos);
		return interpretable;
	}	
	/** Presents dialog querying user for an longs, with no check for minimum and maximum */
	public static long queryLong(MesquiteWindow parent, String title, String message, long current) {
		MesquiteLong io = new MesquiteLong(current);
		//IntegerDialog id = new IntegerDialog(parent, title, message, io);
		//	id.dispose();
		QueryDialogs.queryLong(parent, title, message, io);
		return io.getValue();
	}
}


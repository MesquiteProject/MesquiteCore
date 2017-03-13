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
import java.text.*;
import corejava.Format;

/*Last documented:  August 1999 */

/* ======================================================================== */
/** This double wrapper class is used to be able to pass doubles by reference and have the
	original change as needed*/
public class MesquiteDouble implements Listable {
	public static final double unassigned = Double.MAX_VALUE * 0.9999;
	public static final double inapplicable = Double.MAX_VALUE * 0.99999;
	public static final double infinite = Double.POSITIVE_INFINITY;
	public static final double negInfinite = Double.NEGATIVE_INFINITY;
	public static final double impossible = Double.MAX_VALUE * 0.999999;
	public static final double veryLargeNumber = 1.0e100;
	private double value;
	private String name = null;
	static double log10 = Math.log(10.0);
	public static long totalCreated = 0;
	public static int defaultDigits = 8; //changed from 6, 17 Dec 01
	
	public MesquiteDouble(double value) {
		this.value=value;
		totalCreated++;
	}
	public MesquiteDouble(String name, double value) {
		this.value=value;
		this.name = name;
		totalCreated++;
	}
	public MesquiteDouble() {
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
	/** Returns value as double */
	public double getValue() {
		return value;
	}

	/** Sets value */
	public void setValue(double value) {
		this.value=value;
	}
	/** Sets value */
	public void setValue(String s) {
		this.value=fromString(s);
	}
	/** Adds one to value */
	public void increment() {
		value+=1;
	}
	/** Subtracts one from value */
	public void decrement() {
		value-=1;
	}
	/** Sets value to unassigned */
	public void setToUnassigned() {
		value=unassigned;
	}
	/*--------------------------------CONVERSION--------------------------*/
	/** Returns the double conversion of the passed integer, considering
	unassigned, infinite etc. values */
	public static double toDouble(int v) {
		if (v==MesquiteInteger.unassigned)
			return MesquiteDouble.unassigned;
		else if (v==MesquiteInteger.infinite)
			return MesquiteDouble.infinite;
		else if (v==MesquiteInteger.negInfinite)
			return MesquiteDouble.negInfinite;
		else if (v==MesquiteInteger.inapplicable)
			return MesquiteDouble.inapplicable;
		else if (v==MesquiteInteger.impossible)
			return MesquiteDouble.impossible;
		else
			return v;
	}
	/** Returns the double conversion of the passed long, considering
	unassigned, infinite etc. values */
	public static double toDouble(long v) {
		if (v==MesquiteLong.unassigned)
			return MesquiteDouble.unassigned;
		else if (v==MesquiteLong.infinite)
			return MesquiteDouble.infinite;
		else if (v==MesquiteLong.negInfinite)
			return MesquiteDouble.negInfinite;
		else if (v==MesquiteLong.inapplicable)
			return MesquiteDouble.inapplicable;
		else if (v==MesquiteLong.impossible)
			return MesquiteDouble.impossible;
		else
			return v;
	}

	/*--------------------------------QUERY--------------------------*/
	/** Returns whether value is unassigned */
	public boolean isUnassigned() {
		return (value==unassigned);
	}
	/** Returns whether value is unassigned */
	public static boolean isUnassigned(double d) {
		return (d==unassigned);
	}
	/** Returns whether value is infinite */
	public boolean isInfinite() {
		return (value==infinite || value == negInfinite);
	}
	/** Returns whether value is infinite */
	public static boolean isInfinite(double d) {
		return (d==infinite || d == negInfinite);
	}
	/** Returns whether value is impossible */
	public boolean isImpossible() {
		return (value==impossible);
	}
	/** Returns whether value is impossible */
	public static boolean isImpossible(double d) {
		return (d==impossible);
	}
	/** Returns whether characteristic is inapplicable */
	public boolean isInapplicable() {
		return (value==inapplicable);
	}
	/** Returns whether characteristic is inapplicable */
	public static boolean isInapplicable(double d) {
		return (d==inapplicable);
	}
	/** Returns whether value is a regular number (NOT unassigned, infinite, inapplicable, impossible) */
	public boolean isCombinable() {
		return (value!=unassigned && value!=infinite && value!=impossible && value != negInfinite && value!=inapplicable && (value!=Double.NaN));
	}
	/** Returns whether value is a regular number (NOT unassigned, infinite, inapplicable, impossible) */
	public static boolean isCombinable(double d) {
		return (d!=unassigned && d!=infinite && d!=impossible && d!=inapplicable && d!=negInfinite  && (!Double.isNaN(d))); //NaN test added Apr 02
	}

	/*--------------------------------ARITHMETICS--------------------------*/
	/** Adds to current value.  Considers infinite, unassigned etc. */
	public void add(double toAdd) {
		value = add(toAdd, value);
	}
	/** Subtracts from current value.  Considers infinite, unassigned etc. */
	public void subtract(double toSubtract) {
		value = subtract(value, toSubtract);
	}
	/*--------------------------------CONTAINS--------------------------*/
	/** Returns true iff the double a is contained within the bounds of b1 through b2, inclusive. */
	public static boolean contains(double a, double b1, double b2) {
		if (!isCombinable(a) || !isCombinable(b1)||!isCombinable(b2))
			return false;
		if (b1<=b2)
			if (a>=b1 && a<=b2)
				return true;
		if (b1>=b2)
			if (a<=b1 && a>=b2)
				return true;
		return false;
	}
	/** Adds two doubles.  Considers infinite, unassigned etc. */
	public static double add(double value1, double value2) {
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
	public static double subtract(double value1, double value2) {
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
	/** Multiplies two doubles.  Considers infinite, unassigned etc. */
	public static double multiply(double value1, double value2) {
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
	/** Returns whether the two values differ by less than p OR their ratio differs from 1 by less than p. */
	public static boolean closeEnough(double value1, double value2, double p) {
		if (value1== value2)
			return true;
		if (!isCombinable(value1) || !isCombinable(value2))
			return false;
		if ( Math.abs(value1-value2)<p)
			return true;
		return (Math.abs(1.0 - value1/value2) < p);
			
	}
	/** Squared difference betwen two doubles.  Considers infinite, unassigned etc. */
	public static double squaredDifference(double value1, double value2) {
		if (value1 == 0 || value2==0 || value1 == value2)
			return 0;
		else if (isCombinable(value1)){
			if (isCombinable(value2)) {
				double diff = value1-value2;
				return diff*diff;
			}
		}
		return unassigned;
	/*
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
		*/
		
	}
	/** Divides first double by second double.  Considers infinite, unassigned etc. */
	public static double divide(double value1, double value2) {
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
					return unassigned;
			}
		}
		else {//value 1 not combinable
			if (isCombinable(value2)){
				return unassigned;
			}
			else { //neither combinable
				if (value2 == infinite || value2== negInfinite)
					return 0;
				else
					return unassigned;
			}
		}
	}
	/** returns the negative log of the value. */
	public static double negLog(double value) {
		if (!isCombinable(value))
			return value;
		else
			return -Math.log(value);
	
	}
	/*--------------------------------MIN-MAX--------------------------*/

	/** Returns maximum of two doubles.  Accounts for infinite, unassigned, etc. */
	public static double maximum(double a, double b) {
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
	/** Returns minimum of two doubles.  Accounts for infinite, unassigned, etc. */
	public static double minimum(double a, double b) {
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
	/*--------------------------------INEQUALITIES--------------------------*/

	/** Returns true if value 1 is less than value 2.  Its response is conservative 
	by a proportional tolerance as given (i.e., if tolerance is 0.01, then this returns false even if
	value1 is greater than or equal to value 2, as long as the difference between
	value1 and value2 is less than 1% of the larger of the absolute values of value1 and value2*/
	public static boolean lessThan(double value1, double value2, double toleranceProportion) {
		if (value1 == infinite) 
			return false;
		else if (value2 == infinite) 
			return true;
		else if (value1 == negInfinite) 
			return true;
		else if (value2 == negInfinite) 
			return false;
		else if (!isCombinable(value2))
			return isCombinable(value1);
		else if (!isCombinable(value1))
			return false;
		else if (toleranceProportion >0.0) { //if tolerance negative, ignore
			double a1 = Math.abs(value1);
			double a2 = Math.abs(value2);
			double den;
			if (a1> a2)
				den = a1;
			else
				den = a2;
			if (den == 0.0)
				return false;  //equal and 0, thus not less than
			else if ( Math.abs((value1-value2)/den) < toleranceProportion) //proportional difference less than tolerance
				return false;
			else
				return (value1<value2);
		}
		else 
			return (value1<value2);
	}
	/** Returns true if value 1 is greater than value 2.  Its response is conservative 
	by a proportional tolerance as given (i.e., if tolerance is 0.01, then this returns false even if
	value1 is less than or equal to value 2, as long as the difference between
	value1 and value2 is less than 1% of the larger of the absolute values of value1 and value2*/
	public static boolean greaterThan(double value1, double value2, double toleranceProportion) {
		if (value1 == infinite) 
			return true;
		else if (value2 == infinite) 
			return false;
		else if (value1 == negInfinite) 
			return false;
		else if (value2 == negInfinite) 
			return true;
		else if (!isCombinable(value2)) //TODO: think about these rules
			return isCombinable(value1);
		else if (!isCombinable(value1))
			return false;
		else if (toleranceProportion >0.0) { //if tolerance negative, ignore
			double a1 = Math.abs(value1);
			double a2 = Math.abs(value2);
			double den;
			if (a1> a2)
				den = a1;
			else
				den = a2;
			if (den == 0.0)
				return false;  //equal and 0, thus not less than
			else if ( Math.abs((value1-value2)/den) < toleranceProportion) //proportional difference less than tolerance
				return false;
			else
				return (value1>value2);
		}
		else 
			return (value1>value2);
	}
	/** Returns true if value 1 is equal to value 2,
	within the proportional tolerance as given (i.e., if tolerance is 0.01, then this returns false even if
	value1 is less than or equal to value 2, as long as the difference between
	value1 and value2 is less than 1% of the larger of the absolute values of value1 and value2*/
	public static boolean equals(double value1, double value2, double toleranceProportion) {
		if (value1 == value2) 
			return true;
		else if (value1 == infinite || value2 == infinite || value1 == negInfinite || value2 == negInfinite) 
			return false;
		else if (toleranceProportion>0.0){
			double a1 = Math.abs(value1);
			double a2 = Math.abs(value2);
			double den;
			if (a1> a2)
				den = a1;
			else
				den = a2;
			if (den == 0.0)
				return true; 
			if (Math.abs((value1-value2)/den) < toleranceProportion) //less than 0.000001 proportion difference
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
		/** Returns true if value 1 is greater than value 2 by at least the factor given.  Its response is conservative 
	by a proportional tolerance as given (i.e., if tolerance is 0.01, then this returns false even if
	value1 is less than or equal to value 2, as long as the difference between
	value1 and value2 is less than 1% of the larger of the absolute values of value1 and value2*/
	public static boolean greaterThanByFactor(double value1, double value2, double factor) {
		if (value1 == value2) 
			return false;
		else if (factor<=0)
			return true;
		else if (value2 == infinite) 
			return false;
		else if (value1 == negInfinite) 
			return false;
		else if (value2 == negInfinite) 
			return true;
		else if (!isCombinable(value2)) 
			return isCombinable(value1);
		else if (!isCombinable(value1))
			return false;
		else if (value2== 0 && value1 > 0) 
			return true;
		else if (value1== 0 || value2 == 0) 
			return false;
		else if (value1 > 0  && value2 < 0) //not same sign
			return true;
		else if (value1<0 &&  value2 > 0) //not same sign
			return false;
		else if (value1/value2 >= factor){
			return true;
		}
		else
			return false;
	}
	/** Returns true if otherValue is equal to current value or is within closeness of it*/
	public boolean isCloseEnough(double otherValue, double closeness) {
		if (otherValue == value) 
			return true;
		else if (!isCombinable(otherValue) || !isCombinable(value)) 
			return false;
		else if (otherValue* value < 0) //not same sign
			return false;
		else {
			if (Math.abs(otherValue-value) <= closeness){
				return true;
			}
			else
				return false;
		}
	}
	/** Returns true if value 1 is equal to value 2,
	within the factor as given (i.e., if factor is 2, then this returns true if
	value1/ value 2 is between 0.5 and 2.0)*/
	public static boolean equalsWithinFactor(double value1, double value2, double factor) {
		if (value1 == value2) 
			return true;
		else if (!isCombinable(value1) || !isCombinable(value2)) 
			return false;
		else if (value1== 0 || value2 == 0) 
			return false;
		else if (factor<=0)
			return false;
		else if (value1* value2 < 0) //not same sign
			return false;
		else {
			if (factor< 1)
				factor = 1/factor;
			if (value1>value2 && Math.abs(value1/value2) < factor){
				return true;
			}
			else if (value2>value1 &&  Math.abs(value2/value1) < factor){
				return true;
			}
			else
				return false;
		}
	}
/** DOCUMENT */
	public static double magnitude(double d) {
		if (!isCombinable(d))
			return impossible;
		double ad = Math.abs(d);
		double result = Math.exp(log10* (powerOfTen(ad)));
		return result;
	}
	/** DOCUMENT */
	public static int powerOfTen(double d) {
		if (!isCombinable(d))
			return MesquiteInteger.impossible;
		double ad = Math.abs(d);
		if (d<1) {
			double t =Math.log(ad)/log10 ;
			if (t== (int)(t))
				return (int) (t);
			else
				return (int) (t-1.0);
		}
		else
			return (int) (Math.log(ad)/log10);
	}
	/** DOCUMENT */
	public static double nextOfMagnitude(double mag, double x) {
		if (!isCombinable(x))
			return impossible;
		if (x==0)
			return 0;
		else if (x<0) {
			 double y = -x;
			 if ((int)(y/mag)<=1)
				return 0;
			else
				return -mag*((int)(y/mag));			
		}
		else if ((int)(x/mag)<=1)
			return mag;
		else
			return mag*((int)(x/mag) + 1);
	}
	/** DOCUMENT */
	public static int orderOfMagnitude(double x) {
		if (!isCombinable(x))
			return MesquiteInteger.impossible;
		double log10 = Math.log(10.0);
		if (x<1)
			return - ((int) (Math.log(1/x)/log10)+1);
		else
			return ((int) (Math.log(x)/log10));
	}
	
	/*--------------------------------RULERS--------------------------*/
	/** For a ruler or marked scale that is "span" long, 
	with start point at min and at least minTicks marks, what is first scale mark? */
	public static double firstScaleMark(double span, double min, int minTicks) {
		if (!isCombinable(min) || !isCombinable(span))
			return impossible;
		double mag = magnitude(span);
		if (span/mag < minTicks) //ensure at least this many tick marks
			mag /= 10.0;
		return nextOfMagnitude(mag, min);
	}
	/** For a ruler span long, with start point at min and at least minTicks marks, 
	what is increment between ticks? */
	public static double scaleIncrement(double span, double min, int minTicks) {
		if (!isCombinable(min) || !isCombinable(span))
			return impossible;
		double mag = magnitude(span);
		if (span/mag < minTicks) //ensure at least this many tick marks
			mag /= 10.0;
		return mag;
	}
	/*--------------------------------STRINGS--------------------------*/
	/** Returns string version of this value.  Returns "unassigned" etc. if needed*/
	public String toString() {
		return toStringDigitsSpecified(value, defaultDigits);
	}
	/** Appends string version of value to StringBuffer.  Uses "unassigned" etc. if needed*/
	public static void toString(double d, StringBuffer buffer) {  
		toStringDigitsSpecified(d, defaultDigits, buffer);
	}
	/** Returns string version of value.  Returns "unassigned" etc. if needed*/
	public static String toString(double d) {  
		return toStringDigitsSpecified(d, defaultDigits);
	}
	/** Returns string version of value.  Returns "unassigned" etc. if needed*/
	public static String toStringInRange(double d, double range) {  
		double threshold = Math.abs(range)*0.0001; //if close enough to zero compared to range, call it that
		double absD = Math.abs(d);
		if (absD<threshold)
			return "0.0";
		return toStringDigitsSpecified(d, defaultDigits);
	}
	/** Returns string version of value, showing the given number of digits.
	 Returns "unassigned" etc. if needed*/
	public String toString(int digits) {
		return toStringDigitsSpecified(value, digits);
	}
	public static void toStringDecimal(double d, int digits, StringBuffer s) { 
		if (s==null)
			return;
		if (d>1) {
			MesquiteMessage.println("toStringDecimal passed number bigger than 1");
			return;
		}
		double rounder = 0.5;
		for (int i=0; i<digits; i++) {
			rounder *= 0.1;
		}
		d+= rounder;
		for (int i=0; i<digits-1; i++) {
			s.append(Integer.toString((int)(d*10.0)));
			d = (d*10) - (int)(d*10.0);
		}
		s.append( Long.toString((long)(d*10.0)));
	}
	/** Returns string version of value, showing the given number of digits*/
	public static String toStringDigitsSpecified(double d, int digits) {  
		if (d == MesquiteDouble.unassigned)
			return "?";
		int stringSize = digits; 
		if (digits<0) //added 17 Dec 01 
			stringSize = 1;
		StringBuffer s = new StringBuffer(stringSize);
		toStringDigitsSpecified(d, digits, s);
		return s.toString();
	}
	/*.................................................................................................................*/
	/** Returns string version of value, showing the given number of digits*/
   	 public static String toPrecisionSpecifiedString(double value, int precision){
  	 	DecimalFormat df = (DecimalFormat)NumberFormat.getInstance();
  	 	df.setDecimalFormatSymbols(new EnglishDecimalFormatSymbols());
  	 	df.setMaximumFractionDigits(precision);
  	 	return df.format(value);
   	 }
	public static String toStringNoNegExponential(double d){ //added 14 apr 02 -- needs to use string buffers etc!
		boolean neg = false;
		if (d<0) {
			d = -d;
			neg = true;
		}
		String s = Double.toString(d);
		if (s.indexOf("E-")>=0){ //neg exponential; move decim place to left
			String e = s.substring(s.indexOf("E-")+2, s.length());
			int numMoves = Integer.parseInt(e);
			int decPlace = s.indexOf(".");
			int extraZeros = numMoves - decPlace;
			String qs = "0.";
			for (int i=0; i<extraZeros; i++)
				qs += '0';
			char c = '\0';
			for (int i=0; i<s.length() && c != 'E'; i++) {
				c = s.charAt(i);
				if (c != '.' && c != 'E')
					qs += c;
			}
			s = qs;
				
		}
		
		if (neg)
			return "-" + s;
		else
			return s;
		
	}
	
	/** Appends string version of value, showing the given number of digits, to StringBuffer*/
	public static void toStringDigitsSpecified(double d, int digits, StringBuffer s) {  
		toStringDigitsSpecified(d,digits,s, true);
	}
	
	
	/** Appends string version of value, showing the given number of digits, to StringBuffer*/
	public static String toStringDigitsSpecified(double d, int digits, boolean allowExponentialNotation) {  
		StringBuffer sb = new StringBuffer();
		if (d==unassigned|| d==infinite||d==negInfinite ||d==impossible ||d==inapplicable){
			if (d==unassigned) 
				sb.append("?"); //changed from "unassigned" June 02
			else if (d==infinite) 
				sb.append("infinite");
			else if (d==negInfinite)
				sb.append("neg.infinite");  
			else if (d==impossible)
				sb.append("impossible");  
			else if (d==inapplicable)
				sb.append("inapplicable"); 
			return sb.toString();
		}
		else if (d == 0.0){
			sb.append("0.0");
			return sb.toString();
		}
		if (d<0){
			d = -d;
			sb.append('-');
		}
		String sFromDouble;
		
		
		if (digits<0){ //added 17 Dec 01.  digits < 0 signals full accuracy
			digits=20;
		}
		String dec = "";
		double lowerLimit=1.0;
		for(int i=1; i<=digits; i++){
			dec+="#";
			lowerLimit*=0.1;
		}
		DecimalFormat myFormatter=null;
		EnglishDecimalFormatSymbols formatSymbols = new EnglishDecimalFormatSymbols();
		
		if (allowExponentialNotation && (digits>0 || d>=1.0)){   // only allow exponentional if digits are > 0, or, if 0, value is greater than 1
			if (d>=100000 || d<lowerLimit)   // change from 10000 to 100000 March 2017
				myFormatter = new DecimalFormat("0."+dec+"E0",formatSymbols);
			else 
				myFormatter = new DecimalFormat("#."+dec,formatSymbols);
		}
		else {
			myFormatter = new DecimalFormat("#."+dec,formatSymbols);
		}

		sFromDouble = myFormatter.format(d);
		if (digits==0) 
			sFromDouble = StringUtil.removeLastCharacterIfMatch(sFromDouble, '.');
		sb.append(sFromDouble);
		return sb.toString();

	}
	/** Appends string version of value, showing the given number of digits, to StringBuffer*/
	public static void toStringDigitsSpecified(double d, int digits, StringBuffer s, boolean allowExponentialNotation) {  
		if (s==null)
			return;
		s.append(toStringDigitsSpecified(d,digits,allowExponentialNotation));
	}
	/** Appends string version of value, showing the given number of digits, to StringBuffer*/
	public static void toStringDigitsSpecifiedOld(double d, int digits, StringBuffer s, boolean allowExponentialNotation) {  
		if (s==null)
			return;
		if (d==unassigned|| d==infinite||d==negInfinite ||d==impossible ||d==inapplicable){
			if (d==unassigned) 
				s.append("?"); //changed from "unassigned" June 02
			else if (d==infinite) 
				s.append("infinite");
			else if (d==negInfinite)
				s.append("neg.infinite");  
			else if (d==impossible)
				s.append("impossible");  
			else if (d==inapplicable)
				s.append("inapplicable"); 
			return;
		}
		if (d<0){
			d = -d;
			s.append('-');
		}
		String sFromDouble;
		
		
		if (allowExponentialNotation)
			sFromDouble = Double.toString(d);
		else {
			DecimalFormat myFormatter = new DecimalFormat("0.000000000000000000");
			sFromDouble = myFormatter.format(d);
		}

		if (digits<0){ //added 17 Dec 01
			s.append(sFromDouble);
			return;
		}
		int decimalPos = sFromDouble.indexOf(".");
		int zerosPos = -1;
		if (digits<3)
			zerosPos = sFromDouble.indexOf("000");
		else if (digits<4)
			zerosPos = sFromDouble.indexOf("0000");
		else if (digits<5)
			zerosPos = sFromDouble.indexOf("00000");
		else if (digits<6)
			zerosPos = sFromDouble.indexOf("000000");
		else if (digits<7)
			zerosPos = sFromDouble.indexOf("0000000");
		else if (digits<8)
			zerosPos = sFromDouble.indexOf("00000000");
		else if (digits<9)
			zerosPos = sFromDouble.indexOf("000000000");
		int ninesPos = -1;
		if (digits<3)
			ninesPos = sFromDouble.indexOf("999");
		else if (digits<4)
			ninesPos = sFromDouble.indexOf("9999");
		else if (digits<5)
			ninesPos = sFromDouble.indexOf("99999");
		else if (digits<6)
			ninesPos = sFromDouble.indexOf("999999");
		else if (digits<7)
			ninesPos = sFromDouble.indexOf("9999999");
		else if (digits<8)
			ninesPos = sFromDouble.indexOf("99999999");
		else if (digits<9)
			ninesPos = sFromDouble.indexOf("999999999");
		
			if (decimalPos>=0 && ninesPos>decimalPos){
				String expo = getExpo(sFromDouble);
				if (decimalPos+1 == ninesPos) {
					long dLong = MesquiteLong.fromString(sFromDouble.substring(0, decimalPos)) + 1;
					sFromDouble = Long.toString(dLong) + ".0";
				}
				else {
					char c = sFromDouble.charAt(ninesPos-1);
					sFromDouble = sFromDouble.substring(0, ninesPos-1);
					sFromDouble += (++c);
				}
				sFromDouble += expo;
			}
			else if (decimalPos>=0 && zerosPos>decimalPos){
				String expo = getExpo(sFromDouble);
				sFromDouble = sFromDouble.substring(0, zerosPos+1);
				sFromDouble += expo;
			}
		
		boolean countOn = false;
		int digitCount = 0;

		for (int i=0; i<sFromDouble.length(); i++){
			char c = sFromDouble.charAt(i);
			if (c=='.'){ //start counting digits
				countOn = true;
				if (digits>0)
					s.append(c);
			}
			else if (c=='e' || c=='E'){ //stop counting digits
				countOn = false;
				s.append(c);
			}
			else if (countOn) { //in decimal places after "."
				digitCount++;
				if (digitCount == digits && i+1<sFromDouble.length()) {
					char cNext = sFromDouble.charAt(i+1);
                    if (cNext>='5') { //round up

                        if (c=='0')
                            s.append('1');
                        else if (c=='1')
                            s.append('2');
                        else if (c=='2')
                            s.append('3');
                        else if (c=='3')
                            s.append('4');
                        else if (c=='4')
                            s.append('5');
                        else if (c=='5')
                            s.append('6');
                        else if (c=='6')
                            s.append('7');
                        else if (c=='7')
                            s.append('8');
                        else if (c=='8')
                            s.append('9');
                        else {
                            s.append(c);
                            s.append(cNext);
                        }
                    }
                    else
                        s.append(c);
					
				}
				else if (digitCount<=digits){ //still within first digits
					s.append(c);
				}
			}
			else {
				s.append(c);
			}
		}
/*
		double backTranslate = fromString(s.toString());
		if (Math.abs(backTranslate-d)/d > 0.1)
			MesquiteMessage.warnProgrammer("Error in double string representation: original " + d + "; string " + s 
					+ " zerosPos " + zerosPos	+ " ninesPos " + ninesPos);
*/
	}
	static String getExpo(String s){
		if (s==null)
			return "";
		int e = s.indexOf("E");
		if (e<0)
			e = s.indexOf("e");
		if (e<0)
			return "";
		return s.substring(e, s.length());
	}
	
	
	public static String toFixedWidthString(double d, int width) {
		return toFixedWidthString(d,width,false);
	}
	
	public static String toFixedWidthString(double d, int width, boolean noExponent) {
		if (Math.abs(d) < 1)
			return toFixedWidthString(d,width,noExponent,"0");
		if (d>= 100000)
			return toFixedWidthString(d,width,noExponent,"+INF");
		// either it's small or there's no problem
		return toFixedWidthString(d,width,noExponent,"-INF");
	}


	public static String toFixedWidthString(double d, int width, boolean noExponent, String failString) {
		String myFmt;
		String filler = "                             ";

		if ((Math.abs(Math.log(Math.abs(d))/log10) >= width) && noExponent)  // is 10 the right value here???
			return filler.substring(0,width-failString.length()) + failString;
		else if (noExponent) {
		 	myFmt = "%" + Integer.toString(width) + "." + Integer.toString(width-2) + "F";
		 	String formatResult = new corejava.Format(myFmt).form(d); // force the correct width!
		 	return formatResult;
			}
		else {
	 		myFmt = "%" + Integer.toString(width) + "." + Integer.toString(width-5) + "E";
			String formatResult =  new corejava.Format(myFmt).form(d);
			if (formatResult.indexOf("E") >0)        
				return  formatResult.substring(0,formatResult.length()-3) + formatResult.substring(formatResult.length()-2,formatResult.length());
			else 
				return formatResult;
		}
	}

	/** Returns string version of value, showing the given number of digits*/
	public static String OLDtoString(double d, int digits) {  
		StringBuffer s = new StringBuffer(digits);
		OLDtoString(d, digits, s);
		return s.toString();
	}
	/** Appends string version of value, showing the given number of digits, to StringBuffer*/
	public static void OLDtoString(double d, int digits, StringBuffer s) {  
		if (s==null)
			return;
		if (d==unassigned|| d==infinite||d==negInfinite ||d==impossible ||d==inapplicable){
			if (d==unassigned) 
				s.append("?"); //changed from "unassigned" June 02
			else if (d==infinite) 
				s.append("infinite");
			else if (d==negInfinite)
				s.append("neg.infinite");  
			else if (d==impossible)
				s.append("impossible");  
			else if (d==inapplicable)
				s.append("inapplicable"); 
			return;
		}
		int initialPosition = s.length();
		boolean addNegative = (d<0);
		double ad = Math.abs(d);
		if (ad == 0.0) {
			s.append('0');
			s.append('.');
			toStringDecimal(ad, digits, s);
		}
		else if (ad == 1.0 || (ad<1.0 && ad>0.999999999)) {
			s.append('1');
			s.append('.');
			for (int i=0; i<digits-1; i++) {
				s.append('0');
			}
		}
		else if (ad>1.0) {
			if (ad>1000000) {
				long pow = powerOfTen(ad);
				long first = (long)(ad/magnitude(ad)); //todo: this doesn't work for 100,000
				if (first==0)
					first = 1L;
				s.append(Long.toString(first));
				s.append('.');
				digits--;
				while (ad > 1 && digits>0) {
					ad = ad - first*magnitude(ad);
					first = (long)(ad/magnitude(ad));
					digits--;
					s.append( Long.toString(first));
				}
				s.append('e');
				s.append(Long.toString(pow));
			}
			else {
				long whole = (long) ad;
				if (ad-whole>0.999999999)
					whole+=1;
				 s.append(Long.toString(whole));
				int digitsLeft = digits - s.length() ;
				if (digitsLeft<1) digitsLeft =1;
				s.append('.');
				toStringDecimal(ad - whole, digitsLeft, s);
			}
				
		}
		else {
			if (ad < 0.001) {
				long pow = powerOfTen(ad);
				long first = (long)(ad/magnitude(ad));
				s.append(Long.toString(first));
				s.append('.');
				ad = ad/magnitude(ad) - first;
				toStringDecimal(ad, digits, s);
				s.append('e');
				s.append(Long.toString(pow));
			}
			else {
				s.append('0');
				s.append('.');
				toStringDecimal(ad, digits, s);
			}
		}
		
		if (addNegative)
			s.insert(initialPosition, "-");
	}
	
	/** Returns double parsed from given string.  Assumes entire string is what should be parsed. */
	public static double fromString(String s) {
		return fromString(s, false);
	}
	/** Returns double parsed from given string; warns about NumberFormatException if boolean is true.
	  Assumes entire string is what should be parsed. */
	public static double fromString(String s, boolean warn) {
		if (StringUtil.blank(s))
			return impossible;
		try {
			double d = Double.valueOf(s).doubleValue();
			return d;
		}
		catch (NumberFormatException e) {
			if (s !=null) {
				if (s.equals("-")) //TODO: use gap symbol
					return inapplicable;
				else if (s.equals("?")) //TODO: use missing symbol
					return unassigned;
				else if (s.equalsIgnoreCase("unassigned"))
					return unassigned;
				else if (s.equalsIgnoreCase("estimate"))
					return unassigned;
				else if (s.equalsIgnoreCase("infinite")|| s.equalsIgnoreCase("i"))
					return infinite;
				else if (s.equalsIgnoreCase("inapplicable"))
					return inapplicable;
				else if (s.equalsIgnoreCase("neg.Infinite"))
					return negInfinite;
				else if (s.equalsIgnoreCase("impossible"))
					return impossible;
			}
			if (warn)
				MesquiteMessage.println("NumberFormatException in parsing string \"" + s + "\"");
			return impossible;
		}
	}
	/** returns first number found in string; allows exponentials and negative numbers*/
	public static double fromString (String sT, MesquiteInteger stringLoc) {
  		String c = ParseUtil.getToken(sT, stringLoc);
  		if (c!=null && c.length()>0) {
			if (c.equals("?")) //TODO: use missing symbol
				return unassigned;
			else if (c.equalsIgnoreCase("unassigned"))
				return unassigned;
			else if (c.equalsIgnoreCase("estimate"))
				return unassigned;
			else if (c.equalsIgnoreCase("infinite")|| c.equalsIgnoreCase("i"))
				return infinite;
			else if (c.equalsIgnoreCase("inapplicable"))
				return inapplicable;
			else if (c.equalsIgnoreCase("neg.Infinite"))
				return negInfinite;
			else if (c.equalsIgnoreCase("impossible"))
				return impossible;
  			char clast = c.charAt(c.length()-1);
  			if (clast == 'e' || clast == 'E') { //must be number with exponential to negative
  				String f =ParseUtil.getToken(sT, stringLoc);
  				c +=f;
  				if ("-".equals(f) || "+".equals(f))
  					c +=ParseUtil.getToken(sT, stringLoc);
  			}
  			else if (clast == '-') { //negative
  				return -fromString(sT, stringLoc);
  			}
  		}
  		try {
  			double d = Double.valueOf(c).doubleValue();
  			return d;
  		}
  		catch (NumberFormatException e){}
  		catch (NullPointerException e){}
  		return impossible;
	}
	/** returns first number found in string; allows exponentials and negative numbers*/
	public static double fromString (Parser parser) {
  		String c = parser.getNextToken();
 		if (c!=null && c.length()>0) {
			if (c.equals("?")) //TODO: use missing symbol
				return unassigned;
			else if (c.equalsIgnoreCase("unassigned"))
				return unassigned;
			else if (c.equalsIgnoreCase("estimate"))
				return unassigned;
			else if (c.equalsIgnoreCase("infinite")|| c.equalsIgnoreCase("i"))
				return infinite;
			else if (c.equalsIgnoreCase("inapplicable"))
				return inapplicable;
			else if (c.equalsIgnoreCase("neg.Infinite"))
				return negInfinite;
			else if (c.equalsIgnoreCase("impossible"))
				return impossible;
  			char clast = c.charAt(c.length()-1);
  			if (clast == 'e' || clast == 'E') { //must be number with exponential to negative
  				String f =parser.getNextToken();
  				c +=f;
  				if ("-".equals(f) || "+".equals(f))
  					c +=parser.getNextToken();
  			}
  			else if (clast == '-') { //negative
  				return -fromString(parser);
  			}
  		}
  		try {
  			double d = Double.valueOf(c).doubleValue();
  			return d;
  		}
  		catch (NumberFormatException e){}
  		catch (NullPointerException e){}
  		return impossible;
	}
	/** Returns whether a string can be interpreted as a double. */
	public static boolean interpretableAsDouble(String s,  MesquiteInteger pos) {
		int oldPos = pos.getValue();
		boolean interpretable = fromString(s, pos)!= impossible;
		pos.setValue(oldPos);
		return interpretable;
	}	
	/** Returns whether a string can be interpreted as a double. */
	public static boolean interpretableAsDouble(String s,  MesquiteInteger pos, int posToSet) {
		int oldPos = pos.getValue();
		if (posToSet >=0)
			pos.setValue(posToSet);
		boolean interpretable = fromString(s, pos)!= impossible;
		pos.setValue(oldPos);
		return interpretable;
	}	
	/*--------------------------------QUERY DIALOGS--------------------------*/
	/** Presents dialog querying user for a double, with no check for minimum and maximum */
	public static double queryDouble(MesquiteWindow parent, String title, String message, double current) {
		MesquiteDouble io = new MesquiteDouble(current);
		QueryDialogs.queryDouble(parent, title, message, io);
		return io.getValue();
	}
	/*--------------------------------QUERY DIALOGS--------------------------*/
	/** Presents dialog querying user for a double, with no check for minimum and maximum */
	public static double queryDouble(MesquiteWindow parent, String title, String message, String help, double current) {
		MesquiteDouble io = new MesquiteDouble(current);
		QueryDialogs.queryDouble(parent, title, message, help, io);
		return io.getValue();
	}
	/** Presents dialog querying user for a double, with a check for minimum and maximum */
	public static double queryDouble(MesquiteWindow parent, String title, String message, double current, double minimum, double maximum) {
		return queryDouble(parent, title, message, "", current, minimum, maximum);
	}
	/** Presents dialog querying user for a double, with a check for minimum and maximum */
	public static double queryDouble(MesquiteWindow parent, String title, String message, String help, double current, double minimum, double maximum) {
		if ((current>maximum && MesquiteDouble.isCombinable(maximum) )|| (current<minimum && MesquiteDouble.isCombinable(minimum)))
			current=minimum;
		MesquiteDouble io = new MesquiteDouble(current);
		if (StringUtil.blank(help))
			help = "<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a number.  <p>The initial value is " + MesquiteDouble.toString(current) 
			+ "; the minimum value permitted is " + MesquiteDouble.toString(minimum)+ " and the maximum value permitted is " + MesquiteDouble.toString(maximum);
		boolean done=false;
		while (!done) {
			//DoubleDialog id = new DoubleDialog(parent, title, message, io);
			//id.dispose();
			QueryDialogs.queryDouble(parent,title,message,help,io);
			if (!io.isCombinable() || (io.getValue()<=maximum && io.getValue()>=minimum))
				done=true;
			else {
				MesquiteModule.mesquiteTrunk.alert("Value must be between " + MesquiteDouble.toString(minimum) + " and " + MesquiteDouble.toString(maximum));
				io.setValue(current);
			}
		}
		return io.getValue();
	}
	public static boolean queryTwoDoubles(MesquiteWindow parent, String title, String message1, MesquiteDouble value1, String message2, MesquiteDouble value2) {
		return QueryDialogs.	queryTwoDoubles( parent,  title,  message1,  value1,  message2,  value2);

	}
	/** Presents dialog querying user for two doubles */
	public static void queryTwoDoubles(MesquiteWindow parent, String title, String label1, String label2, MesquiteBoolean answer, MesquiteDouble num1, MesquiteDouble num2) {
		 MesquiteString str1 = new MesquiteString(num1.toString());
		 MesquiteString str2 = new MesquiteString(num2.toString());
		 TwoStringsDialog tsd = new TwoStringsDialog(parent, title, label1, label2, answer, str1, str2,false);
		 tsd.dispose();
		 num1.setValue(str1.getValue());
		 num2.setValue(str2.getValue());
	}
}


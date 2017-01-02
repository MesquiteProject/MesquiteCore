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
/**This class is used to store numbers flexibly, so that system doesn't need to know whether int, long or double.
Automatically upgrades calculations to use long or double as needed.  Because they have the overhead of objects,
it is best to avoid their wanton proliferation.  If an array is needed, better to use the NumberArray,
which behaves similarly but stores the data as a primitive type arrays.  
Most of the methods in MesquiteNumber use the incoming parameter 
	as a guide to how the data should be stored.  Thus there are int, long, and
	double versions of many methods.  Calculations involving mixed types automatically
	upgrade the storage to the bigger type. */
public class MesquiteNumber implements Listable, WithStringDetails{
	private int intValue=0;
	private long longValue=0;
	private double doubleValue=0;
	private int valueClass = 0;
	public static final int INT = 0;
	public static final int LONG = 1;
	public static final int DOUBLE = 2;
	private boolean unassignedFlag = true; // have inappplicable?
	private boolean infiniteFlag = false;
	private MesquiteNumber temp = null;
	private MesquiteNumber temp2 = null;
	private MesquiteNumber[] auxiliaries = null;
	private String name = null;
	public static long totalCreated = 0;
	boolean watchpoint = false;
	public MesquiteNumber() {
		setToUnassigned();
		totalCreated++;
	}
	public MesquiteNumber(int value) {
		this.intValue=value;
		unassignedFlag = value==MesquiteInteger.unassigned;
		valueClass =INT;
		totalCreated++;
	}

	public MesquiteNumber(long value) {
		this.longValue=value;
		unassignedFlag = value==MesquiteLong.unassigned;
		valueClass = LONG;
		totalCreated++;
	}
	public MesquiteNumber(double value) {
		this.doubleValue=value;
		unassignedFlag = value==MesquiteDouble.unassigned;
		valueClass = DOUBLE;
		totalCreated++;
	}
	public MesquiteNumber(MesquiteNumber toCopy) {
		this(toCopy, false);
	}
	public MesquiteNumber(MesquiteNumber toCopy, boolean copyAuxiliaries) {
		setValue(toCopy);
		setName(toCopy.getName());
		copyAuxiliaries(toCopy.getAuxiliaries());
		totalCreated++;
	}
	public void setWatchpoint(boolean w){
		watchpoint = w;
	}
	public int getValueClass(){
		return valueClass;
	}
	public void copyFrom(MesquiteNumber n){
		intValue = n.intValue;
		longValue = n.longValue;
		doubleValue = n.doubleValue;
		valueClass = n.valueClass;
		unassignedFlag = n.unassignedFlag;
		infiniteFlag = n.infiniteFlag;
		name = n.name;
		watchpoint = n.watchpoint;
	}
	public void copyFrom(MesquiteParameter n){
		intValue = 0;
		longValue = 0;
		doubleValue = n.getValue();
		unassignedFlag = doubleValue==MesquiteDouble.unassigned;
		valueClass = DOUBLE;
		name = n.getName();
	}
	public MesquiteNumber[] getAuxiliaries(){  // in case a complex result with multiple numbers is needed
		return auxiliaries;
	}
	public void copyAuxiliaries(MesquiteNumber[] aux){  // in case a complex result with multiple numbers is needed
		if (aux == null){
			auxiliaries = null;
			return;
		}
		if (auxiliaries == null || auxiliaries.length != aux.length)
			auxiliaries = new MesquiteNumber[aux.length];
		for (int i = 0; i< auxiliaries.length; i++) {
			if (auxiliaries[i] == null){
				auxiliaries[i] = new MesquiteNumber(aux[i]);
			}
			else
				auxiliaries[i].copyFrom(aux[i]);
		}
	}
	public void copyAuxiliaries(MesquiteParameter[] aux){  // in case a complex result with multiple numbers is needed
		if (aux == null){
			auxiliaries = null;
			return;
		}
		if (auxiliaries == null || auxiliaries.length != aux.length)
			auxiliaries = new MesquiteNumber[aux.length];
		for (int i = 0; i< auxiliaries.length; i++) {
			if (auxiliaries[i] == null)
				auxiliaries[i] = new MesquiteNumber();
			auxiliaries[i].copyFrom(aux[i]);
			if (aux[i].getConstrainedTo() != null && auxiliaries[i].getName()!= null)
				auxiliaries[i].setName(auxiliaries[i].getName() + " (= " + aux[i].getConstrainedTo().getName() + ")");
		}
	}
	public void setName(String n){
		name = n;
	}
	public String getName(){
		return name;
	}
	/*--------------------------------GET/SET--------------------------*/
	/** Returns the stored value as an int */
	public int getIntValue() {
		if (isInfinite())
			return MesquiteInteger.infinite;
		if (isUnassigned())
			return MesquiteInteger.unassigned;

		if (valueClass==INT)
			return intValue;
		else if (valueClass == LONG)
			return (int)(longValue);
		else if (valueClass == DOUBLE)
			return (int)doubleValue;
//		return java.lang.Math.round(doubleValue);
		else return 0;
	}
	/*...........................................................*/
	/** Returns the stored value as a long */
	public long getLongValue() {
		if (isInfinite())
			return MesquiteLong.infinite;
		if (isUnassigned())
			return MesquiteLong.unassigned;
		if (valueClass==INT)
			return intValue;
		else if (valueClass == LONG)
			return longValue;
		else if (valueClass == DOUBLE)
			return (long)doubleValue;
		else return 0;
	}
	/*...........................................................*/
	/** Returns the stored value as a double */
	public double getDoubleValue() {
		if (isInfinite())
			return MesquiteDouble.infinite;
		if (isUnassigned())
			return MesquiteDouble.unassigned;
		if (valueClass==INT)
			return intValue;
		else if (valueClass == LONG)
			return (longValue);
		else if (valueClass == DOUBLE)
			return doubleValue;
		else return 0;
	}
	/** Sets the stored value to the passed integer */
	public void setValue(int v) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value to " + v);
		intValue = v;
		valueClass = INT;
		unassignedFlag = (v==MesquiteInteger.unassigned);
		infiniteFlag = (v==MesquiteInteger.infinite);
	}

	/** Sets the stored value to the passed long */
	public void setValue(long v) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value to " + v);
		longValue = v;
		valueClass =LONG;

		unassignedFlag = (v==MesquiteLong.unassigned);
		infiniteFlag = (v==MesquiteLong.infinite);
	}

	/** Sets the stored value to the passed double */
	public void setValue(double v) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value to " + v);
		doubleValue = v;
		valueClass=DOUBLE;
		unassignedFlag = (v==MesquiteDouble.unassigned);
		infiniteFlag = (v==MesquiteDouble.infinite);
	}

	/** Sets the values to be the same as the passed MesquiteNumber */
	public void setValue(MesquiteNumber v) {
		if (v==null)
			return;
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value to " + v);
		valueClass=v.valueClass;
		intValue=v.intValue;
		longValue=v.longValue;
		doubleValue=v.doubleValue;
		unassignedFlag = v.unassignedFlag;
		infiniteFlag = v.infiniteFlag;
	}
	/*...........................................................*/
	/** Sets the value to be that represented by the string*/
	public void setValue(String toBeParsed) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value to " + toBeParsed);
		if (toBeParsed==null || toBeParsed.equalsIgnoreCase("unassigned") || toBeParsed.equalsIgnoreCase("?") || toBeParsed.equalsIgnoreCase("estimate")) //"?" included Apr 02
			setToUnassigned();
		else if (toBeParsed.equalsIgnoreCase("infinite")|| toBeParsed.equalsIgnoreCase("i"))
			setToInfinite();
		else if (toBeParsed.indexOf(".")>=0) {
			setValue(MesquiteDouble.fromString(toBeParsed));
		}
		else {
			try {
				int d = Integer.parseInt(toBeParsed);
				setValue(d);
			}
			catch (NumberFormatException e) {
				try {
					long d = Long.valueOf(toBeParsed).longValue();
					setValue(d);
				}
				catch (NumberFormatException e2) {
					setToUnassigned();
				}
			}
		}
	}
	/*...........................................................*/
	/** Sets the value to unassigned of this and auxiliaries */
	public void deassignAllValues() {
		setToUnassigned();
		if (auxiliaries != null){
			for (int i = 0; i< auxiliaries.length; i++)
				auxiliaries[i].setToUnassigned();
		}
	}
	/*...........................................................*/
	/** Sets the value to unassigned */
	public void setToUnassigned() {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value to unassigned");
		unassignedFlag = true;
		infiniteFlag = false;
		intValue=MesquiteInteger.unassigned;
		longValue=MesquiteLong.unassigned;
		doubleValue=MesquiteDouble.unassigned;
		valueClass =INT;
	}
	/*...........................................................*/
	/** Sets the value to Infinity */
	public void setToInfinite() {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value to infinity");
		infiniteFlag = true;
		unassignedFlag = false;
		intValue=MesquiteInteger.infinite;
		longValue=MesquiteLong.infinite;
		doubleValue=MesquiteDouble.infinite;
		valueClass =INT;
	}
	/*--------------------------------QUERY--------------------------*/
	/** Returns true if value is unassigned */
	public boolean isUnassigned() {
		if (unassignedFlag)
			return true;
		else if (valueClass==INT) {
			return intValue==MesquiteInteger.unassigned;
		}
		else if (valueClass == LONG) {
			return longValue==MesquiteLong.unassigned;
		}
		else if (valueClass == DOUBLE) {
			return doubleValue==MesquiteDouble.unassigned;
		}
		return false;
	}
	/** Returns true if value is infinite */
	public boolean isInfinite() {
		if (infiniteFlag)
			return true;
		else if (valueClass==INT) {
			return intValue==MesquiteInteger.infinite;
		}
		else if (valueClass == LONG) {
			return longValue==MesquiteLong.infinite;
		}
		else if (valueClass == DOUBLE) {
			return doubleValue==MesquiteDouble.infinite;
		}
		return false;
	}
	/** Returns true if value is combinable (not unassigned, infinite, inapplicable, impossible) */
	public boolean isCombinable() {
		if (isUnassigned() || isInfinite())
			return false;
		else if (valueClass==INT) {
			return (MesquiteInteger.isCombinable(intValue));
		}
		else if (valueClass == LONG) {
			return (MesquiteLong.isCombinable(longValue));
		}
		else if (valueClass == DOUBLE) {
			return (MesquiteDouble.isCombinable(doubleValue));
		}
		return false;
	}
	/** Returns true if value is combinable (not unassigned, infinite, inapplicable, impossible) */
	public boolean isCombinable(int v) {
		return (MesquiteInteger.isCombinable(v));
	}
	/** Returns true if value is combinable (not unassigned, infinite, inapplicable, impossible) */
	public boolean isCombinable(long v) {
		return (MesquiteLong.isCombinable(v));
	}
	/** Returns true if value is combinable (not unassigned, infinite, inapplicable, impossible) */
	public boolean isCombinable(double v) {
		return (MesquiteDouble.isCombinable(v));
	}
	/*...........................................................*/
	/** Returns true if value is 0 */
	public boolean isZero() {
		if (isUnassigned() || isInfinite())
			return false;
		else if (valueClass==INT) {
			return intValue==0;
		}
		else if (valueClass == LONG) {
			return longValue==0;
		}
		else if (valueClass == DOUBLE) {
			return doubleValue==0;
		}
		return false;
	}
	/*...........................................................*/
	/** Returns true if value is negative */
	public boolean isNegative() {
		if (isUnassigned() || isInfinite())
			return false;
		else if (valueClass==INT) {
			return intValue<0;
		}
		else if (valueClass == LONG) {
			return longValue<0;
		}
		else if (valueClass == DOUBLE) {
			return doubleValue<0;
		}
		return false;
	}
	/*...........................................................*/
	/** Changes sign of value */
	public void changeSign() {
		if (isUnassigned() || isInfinite())
			return;
		else if (valueClass==INT) {
			intValue = -intValue;
		}
		else if (valueClass == LONG) {
			longValue = -longValue;
		}
		else if (valueClass == DOUBLE) {
			doubleValue = -doubleValue;
		}
	}
	/*--------------------------------ARITHMETICS--------------------------*/
	/**  Sets to absolute value */
	public void abs() {
		if (valueClass==INT) {
			intValue = Math.abs(intValue);
		}
		else if (valueClass == LONG)
			longValue = Math.abs(longValue);
		else if (valueClass == DOUBLE)
			doubleValue = Math.abs(doubleValue);

	}
	/** Adds value to current value */
	public void add(int toAdd) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (add)  " + toAdd);
		if (valueClass==INT)
			setValue(MesquiteInteger.add(intValue, toAdd));
		else if (valueClass == LONG)
			setValue(MesquiteLong.add(longValue, MesquiteLong.toLong(toAdd)));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.add(doubleValue, MesquiteDouble.toDouble(toAdd)));
	}

	/**  Adds value to current value */
	public void add(long toAdd) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (add)  " + toAdd);
		if (valueClass==INT)
			setValue(MesquiteLong.add(MesquiteLong.toLong(intValue), toAdd));
		else if (valueClass == LONG)
			setValue(MesquiteLong.add(longValue, toAdd));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.add(doubleValue, MesquiteDouble.toDouble(toAdd)));
	}

	/**  Adds value to current value */
	public void add(double toAdd) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (add)  " + toAdd);
		if (valueClass==INT)
			setValue(MesquiteDouble.add(MesquiteDouble.toDouble(intValue), toAdd));
		else if (valueClass == LONG)
			setValue(MesquiteDouble.add(MesquiteDouble.toDouble(longValue), toAdd));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.add(doubleValue, toAdd));
	}
	/**  Adds value to current value */
	public void add(MesquiteNumber toAdd) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (add)  " + toAdd);
		if (toAdd!=null) {
			if (toAdd.valueClass==INT)
				add(toAdd.intValue);
			else if (toAdd.valueClass == LONG)
				add(toAdd.longValue);
			else if (toAdd.valueClass == DOUBLE)
				add(toAdd.doubleValue);
		}
	}
	/*...........................................................*/
	/**  Subtracts value from current value */
	public void subtract(int toSubtract) {  
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (subtract)  " + toSubtract);
		if (valueClass==INT)
			setValue(MesquiteInteger.subtract(intValue, toSubtract));
		else if (valueClass == LONG)
			setValue(MesquiteLong.subtract(longValue, MesquiteLong.toLong(toSubtract)));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.subtract(doubleValue, MesquiteDouble.toDouble(toSubtract)));
	}

	/**  Subtracts value from current value */
	public void subtract(long toSubtract) { 
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (subtract)  " + toSubtract);
		if (valueClass==INT)
			setValue(MesquiteLong.subtract(MesquiteLong.toLong(intValue), toSubtract));
		else if (valueClass == LONG)
			setValue(MesquiteLong.subtract(longValue, toSubtract));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.subtract(doubleValue, MesquiteDouble.toDouble(toSubtract)));
	}

	/**  Subtracts value from current value */
	public void subtract(double toSubtract) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (subtract)  " + toSubtract);
		if (valueClass==INT)
			setValue(MesquiteDouble.subtract(MesquiteDouble.toDouble(intValue), toSubtract));
		else if (valueClass == LONG)
			setValue(MesquiteDouble.subtract(MesquiteDouble.toDouble(longValue), toSubtract));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.subtract(doubleValue, toSubtract));
	}
	/**  Subtracts value from current value */
	public void subtract(MesquiteNumber toSubtract) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (subtract)  " + toSubtract);
		if (toSubtract!=null) {
			if (toSubtract.valueClass==INT)
				subtract(toSubtract.intValue);
			else if (toSubtract.valueClass == LONG)
				subtract(toSubtract.longValue);
			else if (toSubtract.valueClass == DOUBLE)
				subtract(toSubtract.doubleValue);
		}
	}
	/*...........................................................*/
	/** Multiplies current value by passed value */
	public void multiplyBy(int toMultiply) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (multiplyBy)  " + toMultiply);
		if (valueClass==INT)
			setValue(MesquiteInteger.multiply(intValue, toMultiply));
		else if (valueClass == LONG)
			setValue(MesquiteLong.multiply(longValue, MesquiteLong.toLong(toMultiply)));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.multiply(doubleValue, MesquiteDouble.toDouble(toMultiply)));
	}

	/** Multiplies current value by passed value */
	public void multiplyBy(long toMultiply) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (multiplyBy)  " + toMultiply);
		if (valueClass==INT)
			setValue(MesquiteLong.multiply(MesquiteLong.toLong(intValue), toMultiply));
		else if (valueClass == LONG)
			setValue(MesquiteLong.multiply(longValue, toMultiply));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.multiply(doubleValue, MesquiteDouble.toDouble(toMultiply)));
	}

	/** Multiplies current value by passed value */
	public void multiplyBy(double toMultiply) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (multiplyBy)  " + toMultiply);
		if (valueClass==INT)
			setValue(MesquiteDouble.multiply(MesquiteDouble.toDouble(intValue), toMultiply));
		else if (valueClass == LONG)
			setValue(MesquiteDouble.multiply(MesquiteDouble.toDouble(longValue), toMultiply));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.multiply(doubleValue, toMultiply));
	}
	/** Multiplies current value by passed value */
	public void multiplyBy(MesquiteNumber toMultiply) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (multiplyBy)  " + toMultiply);
		if (toMultiply==null)
			return;
		if (toMultiply.valueClass==INT)
			multiplyBy(toMultiply.intValue);
		else if (toMultiply.valueClass == LONG)
			multiplyBy(toMultiply.longValue);
		else if (toMultiply.valueClass == DOUBLE)
			multiplyBy(toMultiply.doubleValue);
	}
	/*...........................................................*/
	/** Divides current value by passed value */
	public void divideBy(int toDivide) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (divideBy)  " + toDivide);
		if (valueClass==INT)
			setValue(MesquiteInteger.divide(intValue, toDivide));
		else if (valueClass == LONG)
			setValue(MesquiteLong.divide(longValue, MesquiteLong.toLong(toDivide)));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.divide(doubleValue, MesquiteDouble.toDouble(toDivide)));
	}

	/** Divides current value by passed value */
	public void divideBy(long toDivide) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (divideBy)  " + toDivide);
		if (valueClass==INT)
			setValue(MesquiteLong.divide(MesquiteLong.toLong(intValue), toDivide));
		else if (valueClass == LONG)
			setValue(MesquiteLong.divide(longValue, toDivide));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.divide(doubleValue, MesquiteDouble.toDouble(toDivide)));
	}

	/** Divides current value by passed value */
	public void divideBy(double toDivide) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (divideBy)  " + toDivide);
		if (valueClass==INT)
			setValue(MesquiteDouble.divide(MesquiteDouble.toDouble(intValue), toDivide));
		else if (valueClass == LONG)
			setValue(MesquiteDouble.divide(MesquiteDouble.toDouble(longValue), toDivide));
		else if (valueClass == DOUBLE)
			setValue(MesquiteDouble.divide(doubleValue, toDivide));
	}
	/** Divides current value by passed value */
	public void divideBy(MesquiteNumber toDivide) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (divideBy)  " + toDivide);
		if (toDivide!=null) {
			
			if (toDivide.valueClass==INT)
				divideBy(toDivide.intValue);
			else if (toDivide.valueClass == LONG)
				divideBy(toDivide.longValue);
			else if (toDivide.valueClass == DOUBLE)
				divideBy(toDivide.doubleValue);
		}
	}
	/*--------------------------------INEQUALITIES--------------------------*/
	/** Determines if current value equals passed value.  If "mustBeCombinable" is true, then
	returns false if either current value or passed value are not combinable. Otherwise, returns
	true if the stored values are identical*/
	public boolean equals(int value, boolean mustBeCombinable) {
		if (mustBeCombinable && (!isCombinable() || !isCombinable(value)))
			return false;
		else if (valueClass==INT) 
			return (intValue==value);
		else if (valueClass == LONG)
			return (longValue==MesquiteLong.toLong(value));
		else if (valueClass == DOUBLE)
			return (doubleValue==MesquiteDouble.toDouble(value));
		return false;
	}
	/** Determines if current value equals passed value.  If "mustBeCombinable" is true, then
	returns false if either current value or passed value are not combinable. Otherwise, returns
	true if the stored values are identical*/
	public boolean equals(long value, boolean mustBeCombinable) {
		if (mustBeCombinable && (!isCombinable() || !isCombinable(value)))
			return false;
		else if (valueClass==INT) 
			return (MesquiteLong.toLong(intValue)==value);
		else if (valueClass == LONG)
			return (longValue==value);
		else if (valueClass == DOUBLE)
			return (doubleValue==MesquiteDouble.toDouble(value));
		return false;
	}
	/** Determines if current value equals passed value.  If "mustBeCombinable" is true, then
	returns false if either current value or passed value are not combinable. Otherwise, returns
	true if the stored values are identical*/
	public boolean equals(double value, boolean mustBeCombinable) {
		if (mustBeCombinable && (!isCombinable() || !isCombinable(value)))
			return false;
		else if (valueClass==INT) 
			return (MesquiteDouble.toDouble(intValue)==value);
		else if (valueClass == LONG)
			return (MesquiteDouble.toDouble(longValue)==value);
		else if (valueClass == DOUBLE)
			return (doubleValue==value);
		return false;
	}
	/** Determines if current value equals passed value.*/
	public boolean equals(MesquiteNumber n) {
		if (unassignedFlag!=n.unassignedFlag)  
			return false;
		if (infiniteFlag!=n.infiniteFlag)  
			return false;
		if (valueClass==INT)
			return n.equals(intValue, false);
		else if (valueClass == LONG) 
			return  n.equals(longValue, false);
		else if (valueClass == DOUBLE)
			return n.equals(doubleValue, false);
		return false;
	}
	/*...........................................................*/
	/** Sets this number to incoming if current is greater (used to find minima). */
	public void setMeIfIAmMoreThan(MesquiteNumber n) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setMeIfIAmMoreThan)  " + n);
		if (isUnassigned() || isInfinite())
			setValue(n);
		else if (n.valueClass==INT) {
			setMeIfIAmMoreThan(n.intValue);
		}
		else if (n.valueClass == LONG) {
			setMeIfIAmMoreThan(n.longValue);
		}
		else if (n.valueClass == DOUBLE) {
			setMeIfIAmMoreThan(n.doubleValue);
		}
	}
	/*...........................................................*/
	/** Sets this number to incoming if current is greater (used to find minima). */
	public void setMeIfIAmMoreThan(int value) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setMeIfIAmMoreThan)  " + value);
		if (isUnassigned() || isInfinite())
			setValue(value);
		else if (!MesquiteInteger.isCombinable(value))
			return;
		else if (valueClass==INT) {
			if (intValue>value)
				setValue(value);
		}
		else if (valueClass == LONG) {
			if (longValue>value)
				setValue(value);
		}
		else if (valueClass == DOUBLE) {
			if (doubleValue>value)
				setValue(value);
		}
	}
	/** Sets this number to incoming if current is greater (used to find minima). */
	public void setMeIfIAmMoreThan(long value) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setMeIfIAmMoreThan)  " + value);
		if (isUnassigned() || isInfinite())
			setValue(value);
		else if (!MesquiteLong.isCombinable(value))
			return;
		else if (valueClass==INT) {
			if (intValue>value)
				setValue(value);
		}
		else if (valueClass == LONG) {
			if (longValue>value)
				setValue(value);
		}
		else if (valueClass == DOUBLE) {
			if (doubleValue>value)
				setValue(value);
		}
	}
	/** Sets this number to incoming if current is greater (used to find minima). */
	public void setMeIfIAmMoreThan(double value) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setMeIfIAmMoreThan)  " + value);
		if (isUnassigned() || isInfinite())
			setValue(value);
		else if (!MesquiteDouble.isCombinable(value))
			return;
		else if (valueClass==INT) {
			if (intValue>value)
				setValue(value);
		}
		else if (valueClass == LONG) {
			if (longValue>value)
				setValue(value);
		}
		else if (valueClass == DOUBLE) {
			if (doubleValue>value)
				setValue(value);
		}
	}
	/*...........................................................*/
	/** Sets this number to incoming if current is less (used to find maxima). */
	public void setMeIfIAmLessThan(MesquiteNumber n) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setMeIfIAmLessThan)  " + n);
		if (isUnassigned())
			setValue(n);
		else if (isInfinite())
			return;
		else if (n.valueClass==INT) 
			setMeIfIAmLessThan(n.intValue);
		else if (n.valueClass == LONG) 
			setMeIfIAmLessThan(n.longValue);
		else if (n.valueClass == DOUBLE) 
			setMeIfIAmLessThan(n.doubleValue);
	}
	/*...........................................................*/
	/** Sets this number to incoming if current is less (used to find maxima). */
	public void setMeIfIAmLessThan(int value) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setMeIfIAmLessThan)  " + value);
		if (isUnassigned())
			setValue(value);
		else if (isInfinite())
			return;
		else if (!MesquiteInteger.isCombinable(value))
			return;
		else if (valueClass==INT) {
			if (intValue<value)
				setValue(value);
		}
		else if (valueClass == LONG) {
			if (longValue<(long)value)
				setValue(value);
		}
		else if (valueClass == DOUBLE) {
			if (doubleValue<(double)value)
				setValue(value);
		}
	}
	/** Sets this number to incoming if current is less (used to find maxima). */
	public void setMeIfIAmLessThan(long value) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setMeIfIAmLessThan)  " + value);
		if (isUnassigned())
			setValue(value);
		else if (isInfinite())
			return;
		else if (!MesquiteLong.isCombinable(value))
			return;
		else if (valueClass==INT) {
			if ((long)intValue<value)
				setValue(value);
		}
		else if (valueClass == LONG) {
			if (longValue<value)
				setValue(value);
		}
		else if (valueClass == DOUBLE) {
			if (doubleValue<(double)value)
				setValue(value);
		}
	}
	/** Sets this number to incoming if current is less (used to find maxima). */
	public void setMeIfIAmLessThan(double value) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setMeIfIAmLessThan)  " + value);
		if (isUnassigned())
			setValue(value);
		else if (isInfinite())
			return;
		else if (!MesquiteDouble.isCombinable(value))
			return;
		else if (valueClass==INT) {
			if ((double)intValue<value)
				setValue(value);
		}
		else if (valueClass == LONG) {
			if ((double)longValue<value)
				setValue(value);
		}
		else if (valueClass == DOUBLE) {
			if (doubleValue<value)
				setValue(value);
		}
	}	/*...........................................................*/
	/** Returns true if current value is less than value of n . */
	public boolean isBetterThan(MesquiteNumber n, boolean biggerIsBetter) {
		if (biggerIsBetter)
			return isMoreThan(n);
		return isLessThan(n);
	}

	/*...........................................................*/
	/** Returns true if current value is less than value of n . */
	public boolean isLessThan(MesquiteNumber n) {
		if (n==null) return false;
		if (isUnassigned() || isInfinite()) return false;
		if (n.isUnassigned() || n.isInfinite()) return true;
		if (valueClass==INT) {
			if (n.valueClass==INT)
				return intValue<n.intValue;
			else if (n.valueClass == LONG)
				return intValue<n.longValue;
			else if (n.valueClass == DOUBLE)
				return intValue<n.doubleValue;
		}
		else if (valueClass == LONG) {
			if (n.valueClass==INT)
				return longValue<n.intValue;
			else if (n.valueClass == LONG)
				return longValue<n.longValue;
			else if (n.valueClass == DOUBLE)
				return longValue<n.doubleValue;
		}
		else if (valueClass == DOUBLE) {
			if (n.valueClass==INT)
				return doubleValue<n.intValue;
			else if (n.valueClass == LONG)
				return doubleValue<n.longValue;
			else if (n.valueClass == DOUBLE)
				return doubleValue<n.doubleValue;
		}
		return false;
	}
	/*...........................................................*/
	/** Returns true if current value is more than value of n . */
	public boolean isMoreThan(MesquiteNumber n) {
		if (n==null) return false;
		if (isUnassigned() || n.isInfinite()) return false;
		if (n.isUnassigned() || isInfinite()) return true;
		if (valueClass==INT) {
			if (n.valueClass==INT)
				return intValue>n.intValue;
				else if (n.valueClass == LONG)
					return intValue>n.longValue;
					else if (n.valueClass == DOUBLE)
						return intValue>n.doubleValue;
		}
		else if (valueClass == LONG) {
			if (n.valueClass==INT)
				return longValue>n.intValue;
				else if (n.valueClass == LONG)
					return longValue>n.longValue;
					else if (n.valueClass == DOUBLE)
						return longValue>n.doubleValue;
		}
		else if (valueClass == DOUBLE) {
			if (n.valueClass==INT)
				return doubleValue>n.intValue;
				else if (n.valueClass == LONG)
					return doubleValue>n.longValue;
					else if (n.valueClass == DOUBLE)
						return doubleValue>n.doubleValue;
		}
		return false;
	}
	/*...........................................................*/
	/** Returns true if current value is less than d . */
	public boolean isLessThan(double d) {
		if (isUnassigned() || isInfinite()) return false;
		if (MesquiteDouble.isUnassigned(d) || MesquiteDouble.isInfinite(d)) return true;
		if (!MesquiteDouble.isCombinable(d) || !isCombinable())
			return false;
		if (valueClass==INT) {
			return intValue<d;
		}
		else if (valueClass == LONG) {
			return longValue<d;
		}
		else if (valueClass == DOUBLE) {
			return doubleValue<d;
		}
		return false;
	}
	/*...........................................................*/
	/** Returns true if current value is more than d . */
	public boolean isMoreThan(double d) {
		if (isUnassigned() || MesquiteDouble.isInfinite(d)) return false;
		if (MesquiteDouble.isUnassigned(d) || isInfinite()) return true;
		if (!MesquiteDouble.isCombinable(d) || !isCombinable())
			return false;
		if (valueClass==INT) {
			return intValue>d;
		}
		else if (valueClass == LONG) {
			return longValue>d;
		}
		else if (valueClass == DOUBLE) {
			return doubleValue>d;
		}
		return false;
	}
	/*_________________________________________________*/
	/** Checks to see if the number value is less than min (in which case min is reset) or greater than max
	(in which case max is reset).  If it is the first time this function is called for this particular use, 
	the values of min and max are set to the value . */
	public static void checkMinMax (MesquiteBoolean first, MesquiteNumber min, MesquiteNumber max, MesquiteNumber value){
		if (first.getValue()) {
			min.setValue(value);
			max.setValue(value);
			first.setValue(false);
		}
		else {
			max.setMeIfIAmLessThan(value);
			min.setMeIfIAmMoreThan(value);
		}
	}
	/*...........................................................*/
	/** Returns true if current value is more than value of n . */
	public static void exchangeValues(MesquiteNumber n1, MesquiteNumber n2) {
		MesquiteNumber ntemp = new MesquiteNumber(n1);
		n1.setValue(n2);
		n2.setValue(ntemp);
	}
	/*_________________________________________________*/
	private int mag(long d){
		if (d<10)
			return 1;
		else if (d<100)
			return 10;
		else if (d<1000)
			return 100;
		else if (d<10000)
			return 1000;
		else
			return 10000;
	}
	/*--------------------------------BYTE CONVERSION--------------------------*/
	public static byte[] longToBytes(long dt, byte[] bytes){
		if (bytes == null || bytes.length!=8)
			bytes = new byte[8];
		for (int i = 0; i<8; i++) 
			bytes[i] =getByte(dt, i); //storing block of bits
		return bytes;
	}
	public static byte[] shortToBytes(short dt, byte[] bytes){
		if (bytes == null || bytes.length!=2)
			bytes = new byte[2];
		for (int i = 0; i<2; i++) 
			bytes[i] =getByte(dt, i); //storing block of bits
		return bytes;
	}
	public static byte[] doubleToBytes(double d, byte[] bytes){
		long dt =Double.doubleToLongBits(d);
		if (bytes == null || bytes.length!=8)
			bytes = new byte[8];
		for (int i = 0; i<8; i++) 
			bytes[i] =getByte(dt, i); //storing block of bits
		return bytes;
	}
	public static double bytesToDouble(byte[] bytes ){
		if (bytes == null || bytes.length!=8)
			return 0;
		long temp = 0L; //ready to be filled
		for (int i = 0; i<8; i++) {
			temp |= putByteInLong(bytes[i], i);
		}
		return Double.longBitsToDouble(temp);
	}
	public static long bytesToLong(byte[] bytes ){
		if (bytes == null || bytes.length!=8)
			return 0;
		long temp = 0L; //ready to be filled
		for (int i = 0; i<8; i++) {
			temp |= putByteInLong(bytes[i], i);
		}
		return temp
		;
	}
	public static long putByteInLong(byte b, int whichByte){
		long L = ((long) b) & 0x00000000000000FFL;
		L <<= whichByte*8;
		return L;
	}
	public static long getByteLong(long original, int whichByte){
		original >>>= whichByte*8;
		return original & 0x00000000000000FFL;
	}
	public static byte getByte(long original, int whichByte){
		original >>>= whichByte*8;
		return (byte)(original & 0x00000000000000FFL);
	}
	/*--------------------------------RULERS--------------------------*/
	/** For a ruler or marked scale that is with start point at min, end at max, and at least minTicks marks, what is first scale mark? */
	public void setToFirstScaleMark(MesquiteNumber min, MesquiteNumber max, int minTicks) {
		if (min == null|| max == null)
			return;
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setToFirstScaleMark)  ");
		if (!min.isCombinable() || !max.isCombinable()) {
			setToUnassigned();
			return;
		}
		if ((min.valueClass == INT || min.valueClass == LONG) && (max.valueClass == INT || max.valueClass == LONG)) {
			//all integer
			long inc = 1;
			long spanL = max.getLongValue()-min.getLongValue(); //finding tick increment first

			if (spanL> minTicks) {
				int magn = mag(spanL);
				if (magn == 1 || spanL/magn>minTicks) {
					inc = magn;
				}
				else {
					inc = magn/5;
				}
			}
			if (inc == 1) {
				valueClass = LONG;
				setValue(min.getLongValue()-1);
				return;
			}
			else {
				long maxL = max.getLongValue();
				// find something divisible by 10
				valueClass = LONG;
				int magn = mag(maxL);
				setValue(min.getLongValue()/inc*inc/magn*magn - magn);
				return;
			}

		}
		double span = max.getDoubleValue()-min.getDoubleValue();
		double mag = MesquiteDouble.magnitude(span);
		if (span/mag < minTicks) //ensure at least this many tick marks
			mag /= 10.0;
		setValue(MesquiteDouble.nextOfMagnitude(mag, min.getDoubleValue()));
	}
	/** For a ruler or marked scale that is with start point at min, end at max, and at least minTicks marks, what is increment between ticks? */
	public void setToScaleIncrement(MesquiteNumber min, MesquiteNumber max, int minTicks) {
		if (watchpoint) MesquiteMessage.printStackTrace("MesquiteNumber changed value (setToScaleIncrement)  ");
		if (min == null|| max == null)
			return;
		if (!min.isCombinable() || !max.isCombinable()) {
			setToUnassigned();
			return;
		}
		if ((min.valueClass == INT || min.valueClass == LONG) && (max.valueClass == INT || max.valueClass == LONG)) {
			//all integer
			long spanL = max.getLongValue()-min.getLongValue();
			if (spanL<= minTicks) {
				valueClass = INT;
				setValue(1);
				return;
			}
			else {
				int inc =1;
				int magn = mag(spanL);
				if (magn == 1 || spanL/magn>minTicks) {
					inc = magn;
				}
				else {
					inc = magn/5;
				}
				setValue(inc);
				return;
			}
		}
		double span = max.getDoubleValue()-min.getDoubleValue();
		double mag = MesquiteDouble.magnitude(span);
		if (span/mag < minTicks) //ensure at least this many tick marks
			mag /= 10.0;
		setValue(mag);
	}
	/*...........................................................*/
	/** this method is used to return the object's value as an integer between
	0 and numUnits, scaling it according to its position between min and max's values.
	This is useful for graphing and so on, since the graphing program doesn't have to 
	know the data types of this object, min or max in order to calculate the appropriate pixel placement 
	in a graph, for instance */
	public synchronized int setWithinBounds(MesquiteNumber min, MesquiteNumber max, int numUnits) {
		if (min==null || max == null)
			return 0;
		MesquiteNumber div = new MesquiteNumber(max); 
		div.subtract(min);  //div now holds the difference between max and min; this is the scaling factor.
		if (div.isZero())
			return 0;
		MesquiteNumber num = new MesquiteNumber(this);
		num.subtract(min);
		num.divideBy(div);
		num.multiplyBy(numUnits);
		return num.getIntValue();
		//	MesquiteModule.mesquiteTrunk.alert("out of getBounds within setWithinBounds" + " /min " + min.toString() + " /this " + this.toString() + " /max " + max.toString());
	}
	/*...........................................................*/
	/** this method does the inverse of setWithinBounds.  
	 */
	public double getUnboundedValue(MesquiteNumber min, MesquiteNumber max, int numUnits) {
		if (min==null || max == null)
			return 0;
		if (numUnits==0)
			return 0;
		MesquiteNumber num = new MesquiteNumber(this);
		num.divideBy(numUnits);  // fraction of bound that this is
		MesquiteNumber mult = new MesquiteNumber(max);
		mult.subtract(min);
		num.multiplyBy(mult);
		num.add(min);
		return num.getDoubleValue();
	}
	/*...........................................................*/
	/** this method is used to set the object's value according to value, a number between 0 and bound,
	so that the difference between min and max is scaled to bound */
	public void findWithinBounds(MesquiteNumber min, MesquiteNumber max, int value, int bound) {
		if (min==null || max == null)
			setToUnassigned();
		else if (value>=0 && value<= bound && min.isLessThan(max) && bound>0) {
			setValue(max);
			subtract(min);
			divideBy(bound);
			multiplyBy(value);
			add(min);
		}
	}
	/*...........................................................*/
	/** this method is returns which interval the value occurs in, if the given range 
	is cut into the given number of intervals.
	This is useful for graphing histograms that group into intervals. */
	public int findInterval(MesquiteNumber min, MesquiteNumber max, int numIntervals) {
		if ((!isLessThan(min)) && !isMoreThan(max)) {
			if (temp2 == null)
				temp2 = new MesquiteNumber(max);
			else
				temp2.setValue(max);
			temp2.subtract(min);
			if (temp2.isZero())
				return 0;
			temp2.divideBy(numIntervals);
			if (temp == null)
				temp = new MesquiteNumber(this);
			else
				temp.setValue(this);
			temp.subtract(min);
			temp.divideBy(temp2);
			return temp.getIntValue();
		}
		//	MesquiteModule.mesquiteTrunk.alert("out of getBounds within setWithinBounds" + " /min " + min.toString() + " /this " + this.toString() + " /max " + max.toString());
		return -1;
	}
	/*...........................................................*/
	/** this method is returns which interval the value occurs in, if the given range 
	is cut into intervals of width "unit".
	This is useful for graphing histograms that group into intervals. */
	public int findInterval(MesquiteNumber min, MesquiteNumber unit) {
		if (!isLessThan(min)) {
			if (temp == null)
				temp = new MesquiteNumber(this);
			else
				temp.setValue(this);
			temp.subtract(min);
			temp.divideBy(unit);
			return temp.getIntValue();
		}
		//	MesquiteModule.mesquiteTrunk.alert("out of getBounds within setWithinBounds" + " /min " + min.toString() + " /this " + this.toString() + " /max " + max.toString());
		return -1;
	}
	/*--------------------------------STRINGS--------------------------*/
	/** Returns string representation of this number*/
	public String toString() {
		if (isUnassigned())
			return "?"; //changed from "unassigned" June 02
		else if (isInfinite())
			return "infinite";
		else if (valueClass==INT)
			return MesquiteInteger.toString(intValue);// + " (int)";
		else if (valueClass == LONG)
			return MesquiteLong.toString(longValue);// + " (long)";
		else if (valueClass == DOUBLE)
			return MesquiteDouble.toString(doubleValue);// + " (double)";
		else return "";
	}
	/*--------------------------------STRINGS--------------------------*/
	/** Returns string representation of an array of MesquiteNumbers*/
	public static String toString(MesquiteNumber[] numbers) {
		if (numbers==null)
			return null;
		if (numbers.length==0)
			return "";
		StringBuffer sb = new StringBuffer("[");
		for (int i=0; i<numbers.length; i++){
			sb.append(numbers[i].toString());
			if (i<numbers.length-1)
				sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
		
	}
	/*--------------------------------STRINGS--------------------------*/
	/** Returns string representation of the names*/
	public String toStringNames() {
		String s = "";

		if (name != null)
			s += name;
		s += "\t";
		if (auxiliaries != null){
			for (int i = 0; i< auxiliaries.length; i++){
				if (auxiliaries[i] != null && auxiliaries[i].getName() != null)
					s += auxiliaries[i].getName();
				s += "\t";
			}
		}
		return s;
	}
	/*--------------------------------STRINGS--------------------------*/
	/** Returns string representation of this number*/
	public String toStringWithDetails() {
		String s = "";

		if (name != null)
			s += name;
		s += "\t";
		s += toString();
		s += "\t";
		if (auxiliaries != null){
			for (int i = 0; i< auxiliaries.length; i++){
				if (auxiliaries[i] != null && auxiliaries[i].getName() != null)
					s += auxiliaries[i].getName();
				s += "\t";
				if (auxiliaries[i] != null)
					s += auxiliaries[i].toString();
				s += "\t";
			}
		}
		return s;
	}
	/*--------------------------------STRINGS--------------------------*/
	/** Returns string representation of this number.  Number is indicated as 0 if number is double && abs(number)< range*0.0001*/
	public String toString(double range) {
		if (isUnassigned())
			return "?"; //changed from "unassigned" June 02
		else if (isInfinite())
			return "infinite";
		else if (valueClass==INT)
			return MesquiteInteger.toString(intValue);// + " (int)";
		else if (valueClass == LONG)
			return MesquiteLong.toString(longValue);// + " (long)";
		else if (valueClass == DOUBLE)
			return MesquiteDouble.toStringInRange(doubleValue, range);// + " (double)";
		else return "";
	}
	public String toString(int digits, boolean allowExponentialNotation) {
		
		if (isUnassigned())
			return "?"; //changed from "unassigned" June 02
		else if (isInfinite())
			return "infinite";
		else if (valueClass==INT)
			return MesquiteInteger.toString(intValue);// + " (int)";
		else if (valueClass == LONG)
			return MesquiteLong.toString(longValue);// + " (long)";
		else if (valueClass == DOUBLE)
			return MesquiteDouble.toStringDigitsSpecified(doubleValue, digits, allowExponentialNotation);// + " (double)";
		else return "";

	}

	/*...........................................................*/
	/** Returns type of number stored here*/
	public String valueTypeToString() {
		if (isUnassigned())
			return "?"; //changed from "unassigned" June 02
		else if (isInfinite())
			return "infinite";
		else if (valueClass==INT)
			return "int";
		else if (valueClass == LONG)
			return "long";
		else if (valueClass == DOUBLE)
			return "double";
		else return "";
	}
	/** Presents dialog querying user for a number, with no check for minimum and maximum */
	public static MesquiteNumber queryNumber(MesquiteWindow parent, String title, String message, MesquiteNumber current) {
		MesquiteNumber io = new MesquiteNumber(current);
		//NumberDialog id = new NumberDialog(parent, title, message, io);
		//	id.dispose();
		QueryDialogs.queryMesquiteNumber(parent,title,message,io);
		return io;
	}
}



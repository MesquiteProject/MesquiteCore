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
Automatically upgrades to use long or double as needed.
This stores a whole array of similarly typed numbers.  For single numbers, use NumberObjects instead.*/
public class NumberArray {
	public int[] intValues;
	public long[] longValues;
	public double[] doubleValues;
	public static final int INT = 0;
	public static final int LONG = 1;
	public static final int DOUBLE = 2;
	public int valueClass = INT;
	private int length = 0;  // the size of the array
	private NameReference name;

	public NumberArray() {
	}
	public NumberArray(int length) {
		intValues = new int[length];
		this.length=length;
		for (int i=0; i<length; i++)
			intValues[i]=0;
		this.valueClass = 0;
	}
	/*...........................................................*/
	public NumberArray(int length, int valueClass) {
		this.length=length;
		if (valueClass==INT) {
			intValues = new int[length];
			for (int i=0; i<length; i++)
				intValues[i]=0;
		}
		else if (valueClass == LONG) {
			longValues = new long[length];
			for (int i=0; i<length; i++)
				longValues[i]=0;
		}
		else if (valueClass == DOUBLE) {
			doubleValues = new double[length];
			for (int i=0; i<length; i++)
				doubleValues[i]=0;
		}
		this.valueClass = valueClass;
	}
	/*...........................................................*/
	public NumberArray(double[] values) {
		if (values==null)
			length=0;
		else 
			length=values.length;
		valueClass = DOUBLE;
		doubleValues = new double[length];
		for (int i=0; i<length; i++)
			doubleValues[i]=values[i];

	}
	public int getValueClass(){
		return valueClass;
	}
	/*...........................................................*/
	public void switchToDoubles(){
		if (valueClass==INT) {
			doubleValues = new double[length];
			for (int i=0; i<length; i++)
				doubleValues[i]=intValues[i]*1.0;
		}
		else if (valueClass == LONG) {
			doubleValues = new double[length];
			for (int i=0; i<length; i++)
				doubleValues[i]=longValues[i]*1.0;
		}
		valueClass= DOUBLE;
	}
	/*...........................................................*/
	public NumberArray cloneArray(){
		NumberArray c = new NumberArray();
		c.length = length;
		if (valueClass==INT) {
			c.intValues = new int[length];
			for (int i=0; i<length; i++)
				c.intValues[i]=intValues[i];
		}
		else if (valueClass == LONG) {
			c.longValues = new long[length];
			for (int i=0; i<length; i++)
				c.longValues[i]=longValues[i];
		}
		else if (valueClass == DOUBLE) {
			c.doubleValues = new double[length];
			for (int i=0; i<length; i++)
				c.doubleValues[i]=doubleValues[i];
		}
		c.valueClass = valueClass;
		c.name = name;
		return c;
	}
	/*...........................................................*/
	/** Sets this array to be a clone of the passed array*/
	public void setToClone(NumberArray a){
		if (a == null)
			return;
		if (a.length != length || a.valueClass != valueClass){
			if (a.valueClass==INT) {
				intValues = new int[a.length];
			}
			else if (a.valueClass == LONG) {
				longValues = new long[a.length];
			}
			else if (a.valueClass == DOUBLE) {
				doubleValues = new double[a.length];
			}
			length = a.length;
			valueClass = a.valueClass;
		}
		if (valueClass==INT) {
			for (int i=0; i<length; i++)
				intValues[i]=a.intValues[i];
		}
		else if (valueClass == LONG) {
			for (int i=0; i<length; i++)
				longValues[i]=a.longValues[i];
		}
		else if (valueClass == DOUBLE) {
			for (int i=0; i<length; i++)
				doubleValues[i]=a.doubleValues[i];
		}
		name = a.name;
	}
	/*...........................................................*/
	public boolean legalIndex(int i){
		return (i>=0 && i<length);
	}
	/*--------------------------------GET/SET-----------------------------*/
	/*...........................................................*/
	/** Returns int value at index. */
	public int getInt(int index) {
		if (!legalIndex(index)) {
			MesquiteMessage.printStackTrace("NumberArray index out of bounds (a1) " + index + "  max: " + length);
			//MesquiteMessage.warnProgrammer("NumberArray index out of bounds (a1) " + index + "  max: " + length);
		}
		else  {
			if (valueClass==INT)
				return getI(index);
			else if (valueClass == LONG)
				return (int)getL(index);
			else if (valueClass == DOUBLE)
				return (int)getD(index);
		}
		return MesquiteInteger.unassigned;
	}
	/*...........................................................*/
	/** Returns long value at index. */
	public long getLong(int index) {
		if (!legalIndex(index)) {
			MesquiteMessage.warnProgrammer("NumberArray index out of bounds (a2) " + index + "  max: " + length);
		}
		else  {
			if (valueClass==INT)
				return getI(index);
			else if (valueClass == LONG)
				return getL(index);
			else if (valueClass == DOUBLE)
				return (long)getD(index);
		}
		return MesquiteLong.unassigned;
	}
	/*...........................................................*/
	/** Returns double value at index. */
	public double getDouble(int index) {
		if (!legalIndex(index)) {
			MesquiteMessage.warnProgrammer("NumberArray index out of bounds (a3) " + index + "  max: " + length);
		}
		else  {
			if (valueClass==INT)
				return getI(index);
			else if (valueClass == LONG)
				return getL(index);
			else if (valueClass == DOUBLE)
				return getD(index);
		}
		return MesquiteDouble.unassigned;
	}
	//these protect against NPE's but give warning; special use; only in connection to above methods
	private double getD(int index){
		if (doubleValues == null){
			MesquiteMessage.printStackTrace("mistaken value class in Number Array " + valueClass);
			return MesquiteDouble.unassigned;
		}
		return doubleValues[index];
		}
	private long getL(int index){
		if (longValues == null){
			MesquiteMessage.printStackTrace("mistaken value class in Number Array " + valueClass);
			return MesquiteLong.unassigned;
		}
		return longValues[index];
	}
	private int getI(int index){
		if (intValues == null){
			MesquiteMessage.printStackTrace("mistaken value class in Number Array " + valueClass);
			return MesquiteInteger.unassigned;
		}
		return intValues[index];
	}
	/*...........................................................*/
	/** Returns average value of doubles. */
	public double getDoubleAverage() {
		double value;
		double count = 0;
		double total = 0.0;
		for (int i=0; i<length; i++) {
			value = getDouble(i);
			if (MesquiteDouble.isCombinable(value)){
				count++;
				total += value;
			}		
		}
		if (count==0)
			return MesquiteDouble.unassigned;
		return total/count;
	}
	/*...........................................................*/
	/** Places the value at array element "index" into
	the MesquiteNumber n.  Used instead of a simple getValue to avoid having to create
	a MesquiteNumber object. */
	public void placeValue(int index, MesquiteNumber n) {
		if (!legalIndex(index)) {
			MesquiteMessage.printStackTrace("NumberArray index out of bounds (a4) " + index + "  max: " + length);
			return;
		}
		if (n!=null) {
			if (valueClass==INT)
				n.setValue(intValues[index]);
			else if (valueClass == LONG)
				n.setValue(longValues[index]);
			else if (valueClass == DOUBLE)
				n.setValue(doubleValues[index]);
		}
	}
	/*...........................................................*/
	/** Sets value of element "index" to the passed value */
	public void setValue(int index, int v) {
		if (!legalIndex(index)) {
			MesquiteMessage.warnProgrammer("NumberArray index out of bounds (b) " + index + "  max: " + length);
			return;
		}

		if (valueClass==INT)
			intValues[index] = v;
		else if (valueClass == LONG)
			longValues[index] = MesquiteLong.toLong(v);
		else if (valueClass == DOUBLE)
			doubleValues[index]=MesquiteDouble.toDouble(v);
	}

	/** Sets value of element "index" to the passed value */
	public void setValue(int index, long v) {
		if (!legalIndex(index)) {
			MesquiteMessage.warnProgrammer("NumberArray index out of bounds (c) " + index + "  max: " + length);
			return;
		}
		if (valueClass==INT) {
			longValues = new long[length];
			for (int i=0; i<length; i++) {
				longValues[i]=MesquiteLong.toLong(intValues[i]);
			}
			longValues[index] = v;
			intValues=null;
			valueClass=LONG;
		}
		else if (valueClass == LONG)
			longValues[index] = v;
		else if (valueClass == DOUBLE)
			doubleValues[index]=MesquiteDouble.toDouble(v);
	}

	/** Sets value of element "index" to the passed value */
	public void setValue(int index, double v) {
		if (!legalIndex(index)) {
			MesquiteMessage.warnProgrammer("NumberArray index out of bounds (d) " + index + "  max: " + length);
			return;
		}
		if (valueClass==INT) {
			doubleValues = new double[length];
			for (int i=0; i<length; i++)
				doubleValues[i]=MesquiteDouble.toDouble(intValues[i]);
			doubleValues[index] = v;
			intValues=null;
			valueClass=DOUBLE;
		}
		else if (valueClass == LONG) {
			doubleValues = new double[length];
			for (int i=0; i<length; i++)
				doubleValues[i]=MesquiteDouble.toDouble(longValues[i]);
			doubleValues[index] = v;
			longValues=null;
			valueClass=DOUBLE;
		}
		else if (valueClass == DOUBLE)
			doubleValues[index]=v;
	}

	/** Sets value of element "index" to the passed value */
	public void setValue(int index, MesquiteNumber v) {
		if (!legalIndex(index)) {
			MesquiteMessage.printStackTrace("NumberArray index out of bounds (e) " + index + "  max: " + length);
			return;
		}
		if (v==null)
			return;
		if (v.isUnassigned())
			setToUnassigned(index);
		else if (v.isInfinite())
			setToInfinite(index);
		else if (v.getValueClass()==INT)
			setValue(index, v.getIntValue());
		else if (v.getValueClass() == LONG)
			setValue(index, v.getLongValue());
		else if (v.getValueClass() == DOUBLE)
			setValue(index, v.getDoubleValue());
	}
	/** Sets value of the next open (unassigned) element to the passed value */
	private int findOpenValue() {
		if (valueClass==INT) {
			for (int i=0; i<length; i++)
				if (MesquiteInteger.isUnassigned(intValues[i]))
					return i;
		}
		else if (valueClass == LONG){
			for (int i=0; i<length; i++)
				if (MesquiteLong.isUnassigned(longValues[i]))
					return i;
		}
		else if (valueClass == DOUBLE){
			for (int i=0; i<length; i++)
				if (MesquiteDouble.isUnassigned(doubleValues[i]))
					return i;
		}
		return -1;
	}
	/** Sets value of the next open (unassigned) element to the passed value */
	public void setOpenValue(MesquiteNumber v) {
		if (v == null || v.isUnassigned())
			return;
		int index = findOpenValue();
		if (index<0)
			return;
		if (v.isInfinite())
			setToInfinite(index);
		else if (v.getValueClass()==INT)
			setValue(index, v.getIntValue());
		else if (v.getValueClass() == LONG)
			setValue(index, v.getLongValue());
		else if (v.getValueClass() == DOUBLE)
			setValue(index, v.getDoubleValue());
	}
	/*...........................................................*/
	/** Sets value of element "index" to the passed value */
	public void setValues(double[] values) {
		if (values==null)
			deassignArray();
		else {
			valueClass = DOUBLE;
			length=values.length;
			if (doubleValues==null || doubleValues.length !=length)
				doubleValues = new double[length];
			for (int i=0; i<length; i++)
				doubleValues[i]=values[i];
		}
	}
	/*...........................................................*/
	/** Sets value of element "index" to unassigned */
	public void setToUnassigned(int index) {
		if (!legalIndex(index)) {
			MesquiteMessage.warnProgrammer("NumberArray index out of bounds (f) " + index + "  max: " + length);
			return;
		}
		if (valueClass==INT)
			intValues[index]= MesquiteInteger.unassigned;
		else if (valueClass == LONG)
			longValues[index]= MesquiteLong.unassigned;
		else if (valueClass == DOUBLE)
			doubleValues[index]= MesquiteDouble.unassigned;
	}
	/*...........................................................*/
	/** Sets value of element "index" to the infinite */
	public void setToInfinite(int index) {
		if (!legalIndex(index)) {
			MesquiteMessage.warnProgrammer("NumberArray index out of bounds (g) " + index + "  max: " + length);
			return;
		}
		if (valueClass==INT)
			intValues[index]= MesquiteInteger.infinite;
		else if (valueClass == LONG)
			longValues[index]= MesquiteLong.infinite;
		else if (valueClass == DOUBLE)
			doubleValues[index]= MesquiteDouble.infinite;
	}
	/*...........................................................*/
	/** Swaps the values at i and j*/
	public void swapValues(int i, int j) {
		if (!legalIndex(i) && !legalIndex(j)) 
			return;
		if (valueClass==INT) {
			int q = intValues[i];
			intValues[i] = intValues[j];
			intValues[j] = q;
		}
		else if (valueClass == LONG) {
			long q = longValues[i];
			longValues[i] = longValues[j];
			longValues[j] = q;
		}
		else if (valueClass == DOUBLE) {
			double q = doubleValues[i];
			doubleValues[i] = doubleValues[j];
			doubleValues[j] = q;
		}
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void deassignArrayToInteger() {
		valueClass = INT;
		if (intValues== null || intValues.length!=length)
			intValues = new int[length];
		for (int i=0; i<intValues.length; i++)
			intValues[i]=MesquiteInteger.unassigned;
		longValues = null; //TODO: would be better to leave arrays around so as not to have them comjing and going
		doubleValues = null;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void zeroArray() {
		if (valueClass==INT) {
			for (int i=0; i<length; i++)
				intValues[i]=0;
		}
		else if (valueClass == LONG) {
			for (int i=0; i<length; i++)
				longValues[i]=0L;
		}
		else if (valueClass == DOUBLE) {
			for (int i=0; i<length; i++)
				doubleValues[i]=0.0;
		}
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void deassignArray() {
		if (valueClass==INT) {
			for (int i=0; i<length; i++)
				intValues[i]=MesquiteInteger.unassigned;
		}
		else if (valueClass == LONG) {
			for (int i=0; i<length; i++)
				longValues[i]=MesquiteLong.unassigned;
		}
		else if (valueClass == DOUBLE) {
			for (int i=0; i<length; i++)
				doubleValues[i]=MesquiteDouble.unassigned;
		}
	}
	/*...........................................................*/
	/** this method calculates the minimum value in the objects array from 
	elements 0 through numToCheck-1, and places the minimum in the MesquiteNumber r */
	public void placeMinimumValue(MesquiteNumber r, int numToCheck) {
		if (r!=null) {
			if (valueClass==INT) {
				if (intValues.length==0)
					return;
				int min = intValues[0];
				for (int i=0; i<numToCheck; i++)
					min=MesquiteInteger.minimum(min, intValues[i]);
				r.setValue(min);
			}
			else if (valueClass == LONG) {
				if (longValues.length==0)
					return;
				long min = longValues[0];
				for (int i=0; i<numToCheck; i++)
					min = MesquiteLong.minimum(min, longValues[i]);
				r.setValue(min);
			}
			else if (valueClass == DOUBLE) {
				if (doubleValues.length==0)
					return;
				double min = doubleValues[0];
				for (int i=0; i<numToCheck; i++)
					min = MesquiteDouble.minimum(min,doubleValues[i]);
				r.setValue(min);
			}
		}
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void placeMinimumValue(MesquiteNumber r) {
		placeMinimumValue(r, length);
	}
	/*...........................................................*/
	/** this method calculates the maximum value in the objects array from 
	elements 0 through numToCheck-1, and places the maximum in the MesquiteNumber r */
	public void placeMaximumValue(MesquiteNumber r, int numToCheck) {
		if (r!=null) {
			if (valueClass==INT) {
				if (intValues.length==0)
					return;
				int max = intValues[0];
				for (int i=0; i<numToCheck; i++)
					max = MesquiteInteger.maximum(max, intValues[i]);
				r.setValue(max);
			}
			else if (valueClass == LONG) {
				if (longValues.length==0)
					return;
				long max = longValues[0];
				for (int i=0; i<numToCheck; i++)
					max = MesquiteLong.maximum(max, longValues[i]);
				r.setValue(max);
			}
			else if (valueClass == DOUBLE) {
				if (doubleValues.length==0)
					return;
				double max = doubleValues[0];
				for (int i=0; i<numToCheck; i++)
					max = MesquiteDouble.maximum(max, doubleValues[i]);
				r.setValue(max);
			}
		}
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void placeMaximumValue(MesquiteNumber r) {
		placeMaximumValue(r, length);
	}
	/*--------------------------------------QUERY-----------------------------------*/
	/** DOCUMENT */
	public boolean isZero(int i) {
		if (!legalIndex(i))
			return false;
		if (valueClass==INT) {
			if (intValues !=null)
				return intValues[i]==0;
		}
		else if (valueClass == LONG) {
			if (longValues !=null)
				return longValues[i]==0;
		}
		else if (valueClass == DOUBLE) {
			if (doubleValues !=null)
				return doubleValues[i]==0;
		}
		return false;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public boolean isCombinable(int i) {
		if (!legalIndex(i))
			return false;
		if (valueClass==INT) {
			if (intValues !=null)
				return MesquiteInteger.isCombinable(intValues[i]);
		}
		else if (valueClass == LONG) {
			if (longValues !=null)
				return MesquiteLong.isCombinable(longValues[i]);
		}
		else if (valueClass == DOUBLE) {
			if (doubleValues !=null)
				return MesquiteDouble.isCombinable(doubleValues[i]);
		}
		return true;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public boolean isUnassigned(int i) {
		if (!legalIndex(i))
			return false;
		if (valueClass==INT) {
			if (intValues !=null)
				return intValues[i]==MesquiteInteger.unassigned;
		}
		else if (valueClass == LONG) {
			if (longValues !=null)
				return longValues[i]==MesquiteLong.unassigned;
		}
		else if (valueClass == DOUBLE) {
			if (doubleValues !=null)
				return doubleValues[i]== MesquiteDouble.unassigned;
		}
		return true;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public boolean isInfinite(int i) {
		if (!legalIndex(i))
			return false;
		if (valueClass==INT) {
			if (intValues !=null)
				return intValues[i]==MesquiteInteger.infinite;
		}
		else if (valueClass == LONG) {
			if (longValues !=null)
				return longValues[i]==MesquiteLong.infinite;
		}
		else if (valueClass == DOUBLE) {
			if (doubleValues !=null)
				return doubleValues[i]== MesquiteDouble.infinite;
		}
		return false;
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Multiplies the element "index" by the value in the passed MesquiteNumber*/
	public void multiplyBy(int index, MesquiteNumber n) {
		if (!legalIndex(index))
			return;
		if (n!=null) {
			if (valueClass==INT)
				intValues[index] = intValues[index]*n.getIntValue();
			else if (valueClass == LONG)
				longValues[index] = longValues[index]*n.getLongValue();
			else if (valueClass == DOUBLE)
				doubleValues[index] = doubleValues[index]*n.getDoubleValue();
		}
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Divides all by the value in the passed MesquiteNumber*/
	public void multiplyAllBy(MesquiteNumber n) {
		for (int i = 1; i < length; i++) {
				multiplyBy(i,n);
		}
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Divide the element "index" by the value in the passed MesquiteNumber*/
	public void divideBy(int index, MesquiteNumber n) {
		if (!legalIndex(index))
			return;
		if (n!=null) {
			if (valueClass==INT)
				intValues[index] = intValues[index]/n.getIntValue();
			else if (valueClass == LONG)
				longValues[index] = longValues[index]/n.getLongValue();
			else if (valueClass == DOUBLE)
				doubleValues[index] = doubleValues[index]/n.getDoubleValue();
		}
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Divides all by the value in the passed MesquiteNumber*/
	public void divideAllBy(MesquiteNumber n) {
		for (int i = 1; i < length; i++) {
				divideBy(i,n);
		}
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Add the value in the passed MesquiteNumber to that of element "index".  
	NOTE: the sum will be returned in the MesquiteNumber passed!*/
	public void addValue(int index, MesquiteNumber n) {
		if (!legalIndex(index))
			return;
		if (n!=null) {
			if (valueClass==INT)
				n.add(intValues[index]);
			else if (valueClass == LONG)
				n.add(longValues[index]);
			else if (valueClass == DOUBLE)
				n.add(doubleValues[index]);
			setValue(index, n);
		}
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Add the values in the passed numberArray to the equivalent elements of this array */
	public void addArray(NumberArray otherArray) {
		if (length!=otherArray.length)
			return;
		for (int index = 1; index < length; index++) {
				if (valueClass==INT)
					intValues[index] += otherArray.getInt(index);
				else if (valueClass == LONG)
					longValues[index] += otherArray.getLong(index);
				else if (valueClass == DOUBLE)
					doubleValues[index] += otherArray.getDouble(index);
			}
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Add the values in the passed numberArray to the equivalent elements of this array */
	public void subtractArray(NumberArray otherArray) {
		if (length!=otherArray.length)
			return;
		for (int index = 1; index < length; index++) {
				if (valueClass==INT)
					intValues[index] -= otherArray.getInt(index);
				else if (valueClass == LONG)
					longValues[index] -= otherArray.getLong(index);
				else if (valueClass == DOUBLE)
					doubleValues[index] -= otherArray.getDouble(index);
			}
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Squares the values */
	public void squareValues() {
		for (int index = 1; index < length; index++) {
				if (valueClass==INT)
					intValues[index] *= intValues[index];
				else if (valueClass == LONG)
					longValues[index] *= longValues[index];
				else if (valueClass == DOUBLE)
					doubleValues[index] *= doubleValues[index];
			}
	}
	/*--------------------------------ARITHMETRIC, COMPARISON-----------------------------*/
	/**Takes the square root of the values */
	public void squareRootValues() {
		if (valueClass!=DOUBLE)
			switchToDoubles();
		for (int index = 1; index < length; index++) {
				doubleValues[index] = Math.sqrt(doubleValues[index]);
			}
	}
	/*...........................................................*/
	/** Sorts values from highest to lowest; uncombinable put at end*/
	public void sortDown () {
		for (int i = 1; i < length; i++) {
			for (int j= i-1; j>=0 && firstIsGreaterUC(j+1,j); j--) {
				swapValues(j,j+1);
			}
		}
	}
	/*...........................................................*/
	/** Sorts values from lowest to highest; uncombinable put at end*/
	public void sortUp () {
		for (int i = 1; i < length; i++) {
			for (int j= i-1; j>=0 && firstIsGreater(j,j+1); j--) {
				swapValues(j,j+1);
			}
		}
	}
	/*...........................................................*/
	/** Sorts values from lowest to highest AND places medians in passed numbers*/
	public void sortAndPlaceMedian(MesquiteNumber med1, MesquiteNumber med2){
		if (med1 == null || med2 == null)
			return;
		sortUp();
		int numValid = 0;
		med1.setToUnassigned();
		med2.setToUnassigned();
		if (valueClass==INT) {
			for (int i = 0; i< intValues.length && MesquiteInteger.isCombinable(intValues[i]); i++)
				numValid++;
			int count = 0;
			int target = numValid/2; 
			if (target * 2 ==numValid) //even; subtract one
				target--;
			int first = 0;
			int second  = 0;
			for (int i = 0; i< intValues.length && count<= target+1; i++) {
				if (MesquiteInteger.isCombinable(intValues[i])){
					if (count == target)
						first = intValues[i];
					else	if (count == target + 1)
						second = intValues[i];
					count++;
				}
			}
			med1.setValue(first);
			if (first !=second)
				med2.setValue(second);
		}
		else if (valueClass == LONG) {
			for (int i = 0; i< longValues.length && MesquiteLong.isCombinable(longValues[i]); i++)
				numValid++;
			int count = 0;
			int target = numValid/2;
			if (target * 2 ==numValid) //even; subtract one
				target--;
			long first = 0;
			long second  = 0;
			for (int i = 0; i< longValues.length && count<= target+1; i++) {
				if (MesquiteLong.isCombinable(longValues[i])){
					if (count == target)
						first = longValues[i];
					else	if (count == target + 1)
						second = longValues[i];
					count++;
				}
			}
			med1.setValue(first);
			if (first !=second)
				med2.setValue(second);
		}
		else if (valueClass == DOUBLE) {
			for (int i = 0; i< doubleValues.length && MesquiteDouble.isCombinable(doubleValues[i]); i++)
				numValid++;
			int count = 0;
			int target = numValid/2;
			if (target * 2 ==numValid) //even; subtract one
				target--;
			double first = 0;
			double second  = 0;
			for (int i = 0; i< doubleValues.length && count<= target+1; i++) {
				if (MesquiteDouble.isCombinable(doubleValues[i])){
					if (count == target)
						first = doubleValues[i];
					else	if (count == target + 1)
						second = doubleValues[i];
					count++;
				}
			}
			med1.setValue(first);
			if (first !=second)
				med2.setValue(second);
		}
		if (numValid / 2 * 2 != numValid)
			med2.setToUnassigned();
	}
	/*...........................................................*/
	/** returns true if the value at i is greater than that at j; all uncombinable treated as larger*/
	public boolean firstIsGreater(int i, int j) {
		if (!legalIndex(i) && !legalIndex(j)) 
			return false;
		if (valueClass==INT) {
			return  (intValues[i] > intValues[j] || !MesquiteInteger.isCombinable(intValues[i])) && (MesquiteInteger.isCombinable(intValues[j]));
		}
		else if (valueClass == LONG) {
			return (longValues[i] > longValues[j] || !MesquiteLong.isCombinable(longValues[i])) && (MesquiteLong.isCombinable(longValues[j]));
		}
		else if (valueClass == DOUBLE) {
			return (doubleValues[i] > doubleValues[j] || !MesquiteDouble.isCombinable(doubleValues[i]))  && (MesquiteDouble.isCombinable(doubleValues[j]));
		}
		return false;
	}
	/*...........................................................*/
	/** returns true if the value at i is greater than that at j; all uncombinable treated as smaller*/
	public boolean firstIsGreaterUC(int i, int j) {
		if (!legalIndex(i) && !legalIndex(j)) 
			return false;
		if (valueClass==INT) {
			return  intValues[i] > intValues[j] || !MesquiteInteger.isCombinable(intValues[j]);
		}
		else if (valueClass == LONG) {
			return longValues[i] > longValues[j]  || !MesquiteLong.isCombinable(longValues[j]);
		}
		else if (valueClass == DOUBLE) {
			return doubleValues[i] > doubleValues[j] || !MesquiteDouble.isCombinable(doubleValues[j]);
		}
		return false;
	}
	/*...........................................................*/
	/** returns true if the value at i is equal to that at j*/
	public boolean equal(int i, int j) {
		if (!legalIndex(i) && !legalIndex(j)) 
			return false;
		try {
			if (valueClass==INT) {

				return intValues[i] == intValues[j];
			}
			else if (valueClass == LONG) {
				return longValues[i] == longValues[j];
			}
			else if (valueClass == DOUBLE) {
				return doubleValues[i] == doubleValues[j];
			}
		}
		catch (Exception e){
		}
		return false;
	}
	/*...........................................................*/
	/** finds a number with value; returns -1 if not found */
	public int findValue(MesquiteNumber n) {
		if (n!=null) {
			if (valueClass==INT) {
				int v = n.getIntValue();
				for (int index=0; index<length; index++)
					if (v == intValues[index])
						return index;
			}
			else if (valueClass == LONG) {
				long v = n.getLongValue();
				for (int index=0; index<length; index++)
					if (v == longValues[index])
						return index;
			}
			else if (valueClass == DOUBLE) {
				double v = n.getDoubleValue();
				for (int index=0; index<length; index++)
					if (v == doubleValues[index])
						return index;
			}
		}
		return -1;
	}
	/*...........................................................*/
	/** finds a number with value; returns -1 if not found */
	public MesquiteNumber getTotal() {
		MesquiteNumber mn = new MesquiteNumber(0);
		if (valueClass==INT) {
			int v = 0;
			for (int index=0; index<length; index++) 
				if (isCombinable(index))
					v += intValues[index];
			mn.setValue(v);
		}
		else if (valueClass == LONG) {
			long v = 0;
			for (int index=0; index<length; index++)
				if (isCombinable(index))
					v += longValues[index];
			mn.setValue(v);
		}
		else if (valueClass == DOUBLE) {
			double v = 0.0;
			for (int index=0; index<length; index++) 
				if (isCombinable(index))
					v +=  doubleValues[index];
			mn.setValue(v);
		}
		return mn;				
	}
	/*...........................................................*/
	/** returns the MesquiteNumber at a particular index */
	public MesquiteNumber getMesquiteNumber(int index) {
		MesquiteNumber mn = new MesquiteNumber(0);
		if (valueClass==INT) {
			mn.setValue(intValues[index]);
		}
		else if (valueClass == LONG) {
			mn.setValue(longValues[index]);
		}
		else if (valueClass == DOUBLE) {
			mn.setValue(doubleValues[index]);
		}
		return mn;				
	}
	/*...........................................................*/
	/** returns whether or not the value at index is equal to that in the passed MesquiteNumber */
	public boolean equals(int index, MesquiteNumber n) {
		if (!legalIndex(index)) 
			return false;
		if (n!=null) {
			if (valueClass==INT) {
				if (n.equals(intValues[index], true))
					return true;
			}
			else if (valueClass == LONG) {
				if (n.equals(longValues[index], true))
					return true;
			}
			else if (valueClass == DOUBLE) {
				if (n.equals(doubleValues[index], true))
					return true;
			}
		}
		return false;
	}
	/*--------------------------------------SIZE-----------------------------------*/
	/** DOCUMENT */
	public int getSize() {
		return length;
	}
	/*...........................................................*/
	/** this changes the array size */
	public void resetSize(int newNum) {
		if (newNum>length) {
			if (valueClass==INT) {
				//System.out.println("grow nao 1");
				int[] newIntValues = new int[newNum];
				for (int i=0; i<length; i++)
					newIntValues[i]=intValues[i];
				for (int i=length; i<newNum; i++)
					newIntValues[i]=0;
				intValues=newIntValues;
			}
			else if (valueClass == LONG) {
				//System.out.println("grow nao 2");
				long[] newLongValues = new long[newNum];
				for (int i=0; i<length; i++)
					newLongValues[i]=longValues[i];
				for (int i=length; i<newNum; i++)
					newLongValues[i]=0;
				longValues=newLongValues;
			}
			else if (valueClass == DOUBLE) {
				//System.out.println("grow nao 3");
				double[] newDoubleValues = new double[newNum];
				for (int i=0; i<length; i++)
					newDoubleValues[i]=doubleValues[i];
				for (int i=length; i<newNum; i++)
					newDoubleValues[i]=0;
				doubleValues=newDoubleValues;
			}
			length=newNum;
		}
		else if (newNum<length) {
			if (valueClass==INT) {
				//System.out.println("shrink nao 1");
				int[] newIntValues = new int[newNum];
				for (int i=0; i<newNum; i++)
					newIntValues[i]=intValues[i];
				intValues=newIntValues;
			}
			else if (valueClass == LONG) {
				//System.out.println("shrink nao 2");
				long[] newLongValues = new long[newNum];
				for (int i=0; i<newNum; i++)
					newLongValues[i]=longValues[i];
				longValues=newLongValues;
			}
			else if (valueClass == DOUBLE) {
				//System.out.println("shrink nao 3");
				double[] newDoubleValues = new double[newNum];
				for (int i=0; i<newNum; i++)
					newDoubleValues[i]=doubleValues[i];
				doubleValues=newDoubleValues;
			}
			length=newNum;
		}
	}
	/*...........................................................*/
	public void addParts(int starting, int num) {
		if (num<=0)
			return;
		intValues = IntegerArray.addParts(intValues, starting, num);
		doubleValues = DoubleArray.addParts(doubleValues, starting, num);
		longValues = LongArray.addParts(longValues, starting, num, MesquiteLong.unassigned);
		length += num;
	}
	/*...........................................................*/
	public void deleteParts(int starting, int num) {
		if (num<=0)
			return;
		intValues = IntegerArray.deleteParts(intValues, starting, num);
		doubleValues = DoubleArray.deleteParts(doubleValues, starting, num);
		longValues = LongArray.deleteParts(longValues, starting, num);
		length -= num;
	}
	/*...........................................................*/
	public void deletePartsByBlocks(int[][] blocks) {
		intValues = IntegerArray.deletePartsByBlocks(intValues, blocks);
		doubleValues = DoubleArray.deletePartsByBlocks(doubleValues, blocks);
		longValues = LongArray.deletePartsByBlocks(longValues, blocks);
		int shift = 0;
		for (int block = 0; block<blocks.length; block++) 
			shift += blocks[block][1]-blocks[block][0]+1;
		length -= shift;
	}
	/*...........................................................*/
	public void moveParts (int starting, int num, int justAfter){
		if (num<=0)
			return;
		intValues = IntegerArray.moveParts(intValues, starting, num, justAfter);
		doubleValues = DoubleArray.getMoveParts(doubleValues, starting, num, justAfter);
		longValues = LongArray.getMoveParts(longValues, starting, num, justAfter);
	}
	/*...........................................................*/
	public void swapParts (int first, int second){
		if (first<0 || first>=length || second<0 || second>=length) 
			return;
		IntegerArray.swapParts(intValues, first, second);
		DoubleArray.swapParts(doubleValues, first, second);
		LongArray.swapParts(longValues, first, second);
	}
	/*...........................................................*/
	public int getNumParts() {
		return length;
	}
	/*--------------------------------------STRINGS, NAME----------------------------------*/
	/** DOCUMENT */
	public String toString(int index, int digits, boolean allowExponentialNotation) {
		if (!legalIndex(index)) 
			return MesquiteInteger.toString(MesquiteInteger.unassigned);
		if (valueClass==INT) {
			return MesquiteInteger.toString(intValues[index]);// + " (int)";
		}
		else if (valueClass == LONG) {
			return MesquiteLong.toString(longValues[index]);// + " (long)";
		}
		else if (valueClass == DOUBLE) {
			return MesquiteDouble.toStringDigitsSpecified(doubleValues[index], digits, allowExponentialNotation);// + " (double)";
		}
		else return "";
	}
	/** DOCUMENT */
	public String toString(int index) {
		if (!legalIndex(index)) 
			return MesquiteInteger.toString(MesquiteInteger.unassigned);
		if (valueClass==INT) {
			return MesquiteInteger.toString(intValues[index]);// + " (int)";
		}
		else if (valueClass == LONG) {
			return MesquiteLong.toString(longValues[index]);// + " (long)";
		}
		else if (valueClass == DOUBLE) {
			return MesquiteDouble.toString(doubleValues[index]);// + " (double)";
		}
		else return "";
	}
	/** DOCUMENT */
	public String toString() {
		String s = "[ ";
		if (valueClass==INT) {
			for (int i=0; i<length; i++)
				s += MesquiteInteger.toString(intValues[i]) + " ";
		}
		else if (valueClass == LONG) {
			for (int i=0; i<length; i++)
				s +=  MesquiteLong.toString(longValues[i]) + " ";
		}
		else if (valueClass == DOUBLE) {
			for (int i=0; i<length; i++)
				s +=  MesquiteDouble.toString(doubleValues[i]) + " ";
		}
		else return "";
		s += "]";
		return s;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void setNameReference(NameReference nr){
		name = nr;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public NameReference getNameReference(){
		return name;
	}
}



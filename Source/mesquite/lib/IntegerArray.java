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
import java.util.Random;

/*Last documented:  August 1999 */
/* ======================================================================== */
public class IntegerArray  implements Listable  {
	int[] values;
	NameReference name=null;
	int autoExpandAmount = 0;
	
	public IntegerArray(int num){	
		this(num, 0);
	}
	public IntegerArray(int num, int autoExpandAmount){
		values = new int[num];
		for (int i=0; i<num; i++)
			values[i] =  MesquiteInteger.unassigned;
		this.autoExpandAmount = autoExpandAmount;
	}
	/*...........................................................*/
	public boolean legalIndex(int i){
		return (values!=null && i>=0 && i<values.length);
	}
	/*...........................................................*/
	public int[] getMatrix(){
		return values;
	}
	/*...........................................................*/
	public void copyTo(IntegerArray d){
		if (d==null)
			return;
		if (d.values.length!=values.length)
			d.resetSize(values.length);
		for (int i=0; i<values.length; i++)
			d.values[i] =  values[i];
	}
	/*...........................................................*/
	public static int[] reverse(int[] d){
		if (d==null)
			return null;
		int[] values = new int[d.length];
		for (int i=0; i<values.length; i++)
			values[values.length-1-i] =  d[i];
		return values;
	}
	/*...........................................................*/
	public static int[] copy(int[] d){
		if (d==null)
			return null;
		int[] values = new int[d.length];
		for (int i=0; i<values.length; i++)
			values[i] =  d[i];
		return values;
	}
	/*...........................................................*/
	public static int[] copyIntoDifferentSize(int[] d, int newLength, int defaultValue){
		if (d==null)
			return null;
		int[] values = new int[newLength];
		for (int i=0; i<values.length; i++)
			if (i<d.length)
				values[i] =  d[i];
			else
				values[i] = defaultValue;
		return values;
	}
	/*...........................................................*/
	public int getValue(int index){
		if (!legalIndex(index))
			return MesquiteInteger.unassigned;
		else
			return values[index];
	}
	/*...........................................................*/
	public void setValue(int index, int value) {
		if (legalIndex(index))
			values[index] = value;
		else if (autoExpandAmount>0){
			if (index >= values.length){
				int newSize = 0;
				if (index-values.length+1 < autoExpandAmount)
					newSize = values.length + autoExpandAmount;
				else
					newSize = index + autoExpandAmount;
				resetSize(newSize);
				if (legalIndex(index))
					values[index] = value;
			}
		}
	}
	/*...........................................................*/
	public void setValues(int[] values) {
		if (values==null)
			this.values = new int[0];
		else
			this.values= values;
	}
	public static boolean blank(int[] values){
		if (values == null)
			return true;
		for (int i = 0; i<values.length; i++)
			if (values[i] != 0)
				return false;
		return true;
	}
	/*...........................................................*/
	public void zeroArray(){
		for (int i=0; i<values.length; i++)
			values[i] =  0;
	}
	public static void zeroArray(int[] values){
		if (values==null)
			return;
		for (int i=0; i<values.length; i++)
			values[i] =  0;
	}
	/*...........................................................*/
	public void deassignArray(){
		for (int i=0; i<values.length; i++)
			values[i] =  MesquiteInteger.unassigned;
	}
	/*...........................................................*/
	public static void deassignArray(int[] values){
		if (values==null)
			return;
		for (int i=0; i<values.length; i++)
			values[i] =  MesquiteInteger.unassigned;
	}
	/*...........................................................*/
	/** Fills array values with random numbers (integers from 0 to length, randomly ordered) */
	public static void fillWithRandomOrderValues(int[] values, Random rng){
		if (values==null)
			return;
		deassignArray(values);
		int candidate;
		int value = 0;
		for (int bins=values.length; bins>0; bins--) {
			candidate= (int)(rng.nextDouble()*(bins)-Double.MIN_VALUE)+1;  //casting as (int) will truncate
			int count = 1;
			for (int i = 0; i<values.length; i++) {
				if (!MesquiteInteger.isCombinable(values[i])) {
						if (candidate==count){
							values[i] = value;
							break;
						}
						count++;
				}
			}
			value++;
		}
		for (int i = 0; i<values.length; i++) {
			if (!MesquiteInteger.isCombinable(values[i])) {
					MesquiteMessage.warnProgrammer("Some values in int[] not filled with random order values");
					return;
			}
		}
	}
	/*...........................................................*/
	public static boolean equalValues(int[] arr){
		if (arr==null)
			return false;
		if (arr.length==0)
			return true;
		int first = arr[0];
		for (int i=0; i<arr.length; i++)
			if (arr[i] != first)
				return false;
		return true;
	}
	/*...........................................................*/
	public int indexOf(int match){
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int indexOf(int[] a, int match){
		if (a==null)
			return -1;
		for (int i=0; i<a.length; i++)
			if (a[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int countSame(int[] a, int match){
		if (a==null)
			return 0;
		int count= 0;
		for (int i=0; i<a.length; i++)
			if (a[i]== match)
				count++;
		return count;
	}
	/*...........................................................*/
	public static boolean moreThanOneOccurrence(int[] a, int match){
		if (a==null)
			return false;
		boolean alreadyFound = false;
		for (int i=0; i<a.length; i++) {
			if (a[i]== match) {
				if (alreadyFound)
					return true;
				alreadyFound = true;
			}
		}
		return false;
	}
	/*...........................................................*/
	public int getSize() {
		if (values==null)
			return 0;
		return values.length;
	}
	/*...........................................................*/
	public boolean anyAssigned(){
		for (int i=0; i<values.length; i++)
			if (values[i] != MesquiteInteger.unassigned) {

				return true;
			}
		return false;
	}
	/*...........................................................*/
	public boolean fillNextUnassigned(int v){
		for (int i=0; i<values.length; i++)
			if (values[i]== MesquiteInteger.unassigned) {
				values[i] = v;
				return true;
			}
		return false;
	}
	/*...........................................................*/
	/** Returns the index of the maximum value within the integer array */
	public static int indexOfMaximum(int[] values){
		if (values==null)
			return MesquiteInteger.unassigned;
		int index = 0;
		for (int i=0; i<values.length; i++) {
			if (MesquiteInteger.whichIsGreater(values[i],values[index])>0)  // then the first one is greater
					index=i;
		}
		return index;
	}
	/*...........................................................*/
	public static int maximum(int[] values){
		if (values==null)
			return MesquiteInteger.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteInteger.unassigned);
		for (int i=0; i<values.length; i++)
			d.setMeIfIAmLessThan(values[i]);
		return d.getIntValue();
	}
	/*...........................................................*/
	public static void sort(int[] array){
		if (array==null || array.length<=1)
			return;
		
		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && array[j]>array[j+1]; j--) {
				int temp = array[j];
				array[j] = array[j+1];
				array[j+1]=temp;
			}
		}
		
	}
	/*...........................................................*/
	public void resetSize(int newNum) {
		if (newNum == getSize())
			return;
		int[] newIntValues = new int[newNum];
		for (int i=0; i<getSize() && i<newNum; i++)
			newIntValues[i]=values[i];
		if (newNum>getSize())
			for (int i=getSize(); i<newNum; i++)
				newIntValues[i]=MesquiteInteger.unassigned;
		values=newIntValues;
	}
	/*...........................................................*/
	public void addParts(int starting, int num) {
		values=addParts(values, starting, num);
	}
	/*...........................................................*/
	public static int[] addParts(int[] d, int starting, int num) {
		if (num<=0 || d==null)
			return d;
		/*#
		if (d.length <= 0) {
			int[] newMatrix=new int[num];
			for (int ic=0; ic<num; ic++){
					newMatrix[ic]= defaultValue; //filling with missing data
			}
			return newMatrix;
		}
		*/
		if (starting<0) 
			starting = -1;
		if (starting>=d.length) 
			starting = d.length-1;
		int newNum = d.length + num;
		int[] newValues = new int[newNum];
		for (int i=0; i<=starting; i++)
			newValues[i]=d[i];
		for (int i=0; i<num ; i++)
			newValues[starting + i + 1]=MesquiteInteger.unassigned;
		for (int i=0; i<d.length-starting-1; i++) 
			newValues[i +starting+num+1]=d[starting + i + 1];
		return newValues;
	}
	/*...........................................................*/
	public static int[] addParts(int[] d, int num) {
		if (d==null)
			return null;
		int starting=d.length-1;
		return addParts(d,d.length-1,num);
	}
	/*...........................................................*/
	public void deleteParts(int starting, int num) {
		if (num<=0 || starting<0 || starting>=getSize())
			return;
		values = deleteParts(values, starting, num);
	}
	/*...........................................................*/
	public static int[] deleteParts(int[] d, int starting, int num) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		if (num+starting>d.length)
			num = d.length-starting;
		int newNum =d.length - num;
		int[] newValues = new int[newNum];
		for (int i=0; i<starting; i++)
			newValues[i]=d[i];
		for (int i=starting+num; i<d.length; i++)
			newValues[i-num]=d[i];
		return newValues;
	}
	/*...........................................................*/
	public static void swapParts(int[] d, int first, int second) {
		if (d == null || first<0 || first>=d.length || second<0 || second>=d.length) 
			return;
		int temp = d[first];
		d[first] = d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static int[] moveParts(int[] d, int starting, int num, int justAfter) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		int[] newValues = new int[d.length];

		if (starting>justAfter){
			int count =0;
			for (int i=0; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues[count++]=d[i];
			for (int i=starting+num; i<d.length; i++)
				newValues[count++]=d[i];
		}
		else {
			int count =0;
			for (int i=0; i<=starting-1; i++)
				newValues[count++]=d[i];
			for (int i=starting+num; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<d.length; i++)
				newValues[count++]=d[i];
		}

		return newValues;
	}
	/*...........................................................*/
	public String getName(){
		if (name!=null)
			return name.getValue();
		else
			return "";
	}
	/*...........................................................*/
	public void setNameReference(NameReference nr){
		name = nr;
	}
	/*...........................................................*/
	public NameReference getNameReference(){
		return name;
	}
	/*...........................................................*/
	public String toString(){
		return "IntegerArray " + toString(values);
	}
	/*...........................................................*/
	public static String toString(int[] vector){
		if (vector==null ||  vector.length==0)
			return null;  
		StringBuffer result = new StringBuffer(vector.length*2);
		result.append('[');
		for (int i=0; i<vector.length; i++) {
			if (vector[i] == MesquiteInteger.unassigned)
				result.append('?');
			else
				result.append(Integer.toString(vector[i]));
			result.append(' ');
		}
		result.append(']');
		return result.toString();
	}
	/** returns a string listing the elements of the array that are equal to the passed number.  In the format
	of NEXUS character, taxa lists (e.g., "1- 3 6 201-455".  The offset is what the first element is to be numbered
	(e.g., 0 or 1)  */
	public static String getListOfMatches(NumberArray values, int target, int offset) {
		int continuing = 0;
		String s="";
		boolean found=false;
		int lastWritten = -1;
		for (int i=0; i<values.getSize(); i++) {
			if (values.getInt(i)==target) {
				found=true;
				if (continuing == 0) {
					s += " " + (i + offset);
					lastWritten = i;
					continuing = 1;
				}
				else if (continuing == 1) {
					s += " - ";
					continuing = 2;
				}
			}
			else if (continuing >0) {
				if (lastWritten != i-1) {
					s += " " + (i-1 + offset);
					lastWritten = i-1;
				}
				else
					lastWritten = -1;
				continuing = 0;
			}
		}
		if (continuing>1)
			s += " " + (values.getSize()-1 + offset);
		if (found)
			return s;
		else
			return null;
	}
	/** returns a string listing the elements of the array that are equal to the passed number.  In the format
	of NEXUS character, taxa lists (e.g., "1- 3 6 201-455".  The offset is what the first element is to be numbered
	(e.g., 0 or 1)  */
	public static String getListOfMatches(int[] values, int target, int offset) {
		int continuing = 0;
		String s="";
		boolean found=false;
		int lastWritten = -1;
		for (int i=0; i<values.length; i++) {
			if (values[i]==target) {
				found=true;
				if (continuing == 0) {
					s += " " + (i + offset);
					lastWritten = i;
					continuing = 1;
				}
				else if (continuing == 1) {
					s += " - ";
					continuing = 2;
				}
			}
			else if (continuing >0) {
				if (lastWritten != i-1) {
					s += " " + (i-1 + offset);
					lastWritten = i-1;
				}
				else
					lastWritten = -1;
				continuing = 0;
			}
		}
		if (continuing>1)
			s += " " + (values.length-1 + offset);
		if (found)
			return s;
		else
			return null;
	}
	/** returns a string listing the elements of the array that are equal to the passed number.  In the format
	of NEXUS character, taxa lists (e.g., "1- 3 6 201-455".  The offset is what the first element is to be numbered
	(e.g., 0 or 1)  */
	public static String getListOfMatches(MesquiteInteger[] values, int target, int offset) {
		int continuing = 0;
		String s="";
		boolean found=false;
		int lastWritten = -1;
		for (int i=0; i<values.length; i++) {
			if (values[i]!=null && values[i].getValue()==target) {
				found=true;
				if (continuing == 0) {
					s += " " + (i + offset);
					lastWritten = i;
					continuing = 1;
				}
				else if (continuing == 1) {
					s += " - ";
					continuing = 2;
				}
			}
			else if (continuing >0) {
				if (lastWritten != i-1) {
					s += " " + (i-1 + offset);
					lastWritten = i-1;
				}
				else
					lastWritten = -1;
				continuing = 0;
			}
		}
		if (continuing>1)
			s += " " + (values.length-1 + offset);
		if (found)
			return s;
		else
			return null;
	}
	/*-----------------------------------------*/
	public static int[] subtractArrays(int[] firstArray, int[] secondArray){  
		int count = 0;
		for (int i=0; i<firstArray.length; i++) {
			if (inArray(firstArray[i],secondArray))  { // element is in firstArray, therefore will be subtracted
				count ++;
			}
		}
		int[] subtraction = new int[firstArray.length-count];
		if (subtraction.length==0)  // empty array
			return null;
		count = 0;
		for (int i=0; i<firstArray.length; i++) {
			if (!inArray(firstArray[i],secondArray))  { // keep this one
				subtraction[count] = firstArray[i];
				count ++;
			}
		}
		return subtraction;
	}
	/*-----------------------------------------*/
  	 public static boolean inArray(int n, int[] a){
  	 	if (a==null)
  	 		return false;
  	 	for (int i = 0; i<a.length; i++)
  	 		if (a[i] == n)
  	 			return true;
  	 	return false;
  	 }
	/*-----------------------------------------*/
   	 public static boolean arraysSame(int[]a, int[] b){ //assumes numbers not duplicated
  	 	if (a==null || b == null)
  	 		return false;
		if (a.length != b.length)
			return false;
		
		for (int i = 0; i<a.length; i++) {
			if (!inArray(a[i],b))
				return false;
		}
		return true;
  	 }
}


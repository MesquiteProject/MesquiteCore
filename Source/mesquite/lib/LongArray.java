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
/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules


/* ======================================================================== */

public class LongArray implements Listable {
	long[] values;
	NameReference name=null;
	int autoExpandAmount = 0;
	
	public LongArray(int num){	
		this(num, 0);
	}

	public LongArray(int num, int autoExpandAmount){
		values = new long[num];
		for (int i=0; i<num; i++)
			values[i] =  MesquiteLong.unassigned;
		this.autoExpandAmount = autoExpandAmount;
	}
		
	//used for Associables that might need to record whether reference is to part or in between part
	//intially for MesquiteTree to know if info applies to node or branch ancestral to node
	boolean between = false;
	public void setBetweenness(boolean b){
		between = true;  
	}
	public boolean isBetween(){
		return between;
	}
	/*...........................................................*/
	public boolean legalIndex(int i){
		return (values!=null && i>=0 && i<values.length);
	}
	/*...........................................................*/
	public long[] getMatrix(){
		return values;
	}
	/*...........................................................*/
	public long getValue(int index){
		if (!legalIndex(index))
			return MesquiteLong.unassigned;
		else
			return values[index];
	}
	/*...........................................................*/
	public void setValue(int index, long value) {
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
	public void setValues(long[] values) {
		if (values==null)
			this.values = new long[0];
		else
			this.values= values;
	}
	/*...........................................................*/
	public void copyTo(LongArray d){
		if (d==null || d.values.length!=values.length)
			return;
		for (int i=0; i<values.length; i++)
			d.values[i] =  values[i];
	}
	/*...........................................................*/
	public void copyTo(DoubleArray d){
		if (d==null || d.values.length!=values.length)
			return;
		for (int i=0; i<values.length; i++){
			d.values[i] =  MesquiteDouble.toDouble(values[i]);
		}
	}
	/*...........................................................*/
	/** turns value to absolute value. */
	public void abs() {
		for (int i=0; i<values.length; i++){
			if (MesquiteLong.isCombinable(values[i]))
				values[i] = Math.abs(values[i]);
		}
	}
	public void zeroArray(){

		for (int i=0; i<values.length; i++)
			values[i] =  0;
	}
	public static void zeroArray(long[] values){
		if (values==null)
			return;
		for (int i=0; i<values.length; i++)
			values[i] =  0;
	}
	/*...........................................................*/
	public void deassignArray(){
		for (int i=0; i<values.length; i++)
			values[i] =  MesquiteLong.unassigned;
	}
	/*...........................................................*/
	public static void deassignArray(long[] values){
		if (values==null)
			return;
		for (int i=0; i<values.length; i++)
			values[i] =  MesquiteLong.unassigned;
	}
	/*...........................................................*/
	public static long[] reverse(long[] d){
		if (d==null)
			return null;
		long[] values = new long[d.length];
		for (int i=0; i<values.length; i++)
			values[values.length-1-i] =  d[i];
		return values;
	}
	/*...........................................................*/
	public int indexOf(long match){
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int indexOf(long[] values, long match){
		if (values==null)
			return -1;
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int countSame(long[] a, long match){
		if (a==null)
			return 0;
		int count= 0;
		for (int i=0; i<a.length; i++)
			if (a[i]== match)
				count++;
		return count;
	}
	/*...........................................................*/
	public boolean fillNextUnassigned(long v){
		for (int i=0; i<values.length; i++)
			if (values[i]== MesquiteLong.unassigned) {
				values[i] = v;
				return true;
			}
		return false;
	}
	/*...........................................................*/
	public static void sort(long[] array){
		if (array==null || array.length<=1)
			return;

		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && array[j]>array[j+1]; j--) {
				long temp = array[j];
				array[j] = array[j+1];
				array[j+1]=temp;
			}
		}

	}
	/*...........................................................*/
	public int getSize() {
		if (values==null)
			return 0;
		return values.length;
	}
	/*...........................................................*/
	public void resetSize(int newNum) {
		if (newNum == getSize())
			return;
		long[] newLongValues = new long[newNum];
		for (int i=0; i<getSize() && i<newNum; i++)
			newLongValues[i]=values[i];
		if (newNum>getSize())
			for (int i=getSize(); i<newNum; i++)
				newLongValues[i]=MesquiteLong.unassigned;
		values=newLongValues;
	}
	/*...........................................................*/
	public void addParts(int starting, int num, long defaultValue) {
		values=addParts(values, starting, num, defaultValue);
	}
	/*...........................................................*/
	public static long[] addParts(long[] d, int starting, int num, long defaultValue) {
		if (num<=0 || d==null)
			return d;

		if (d.length <= 0) {
			long[] newMatrix=new long[num];
			for (int ic=0; ic<num; ic++){
				newMatrix[ic] = defaultValue; //filling with missing data
			}
			return newMatrix;
		}

		if (starting<0) 
			starting = -1;
		if (starting>d.length) 
			starting = d.length-1;
		int newNum = d.length + num;
		long[] newValues = new long[newNum];
		for (int i=0; i<=starting; i++)
			newValues[i]=d[i];
		for (int i=0; i<num ; i++)
			newValues[starting + i + 1]=defaultValue;
		for (int i=0; i<d.length-starting-1; i++)
			newValues[i +starting+num+1]=d[starting + i + 1];
		return newValues;
	}
	/*...........................................................*/
	public void deleteParts(int starting, int num) {
		if (num<=0 || starting<0 || starting>=getSize())
			return;
		values = deleteParts(values, starting, num);
	}
	/*...........................................................*/
	public static long[] deleteParts(long[] d, int starting, int num) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		if (num+starting>d.length)
			num = d.length-starting;
		int newNum =d.length - num;
		long[] newValues = new long[newNum];
		for (int i=0; i<starting; i++)
			newValues[i]=d[i];
		for (int i=starting+num; i<d.length; i++)
			newValues[i-num]=d[i];
		return newValues;
	}
	/*...........................................................*/
	public static long[] getMoveParts(long[] d, int starting, int num, int justAfter) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		long[] newValues = new long[d.length];

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
	public void moveParts(int starting, int num, int justAfter) {
		moveParts(values, starting, num, justAfter);
	}
	/*...........................................................*/
	public boolean swapParts(int first, int second) {
		if (first<0 || first>=values.length || second<0 || second>=values.length) 
			return false;
		long temp = values[first];
		values[first] = values[second];
		values[second] = temp;
		return true;
	}
	/*...........................................................*/
	public static void swapParts(long[] d, int first, int second) {
		if (d == null || first<0 || first>=d.length || second<0 || second>=d.length) 
			return;
		long temp = d[first];
		d[first] = d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void moveParts(long[] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		long[] newValues = new long[d.length];
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
		for (int i=0; i<d.length; i++)
			d[i]=newValues[i];
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
	public static String toString(long[] vector){
		if (vector==null ||  vector.length==0)
			return null;  
		StringBuffer result = new StringBuffer(vector.length*2);
		result.append('[');
		for (int i=0; i<vector.length; i++) {
			if (vector[i] == MesquiteLong.unassigned)
				result.append('?');
			else
				result.append(Long.toString(vector[i]));
			result.append(' ');
		}
		result.append(']');
		return result.toString();
	}
}


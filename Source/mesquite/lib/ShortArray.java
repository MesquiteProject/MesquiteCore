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

public class ShortArray implements Listable {
	short[] values;
	NameReference name=null;
	
	public static final short unassigned = Short.MIN_VALUE+1;
	public ShortArray(int num){
		values = new short[num];
		for (int i=0; i<num; i++)
			values[i] =  unassigned;
	}
	/*...........................................................*/
	public boolean legalIndex(int i){
		return (values!=null && i>=0 && i<values.length);
	}
	/*...........................................................*/
	public short[] getMatrix(){
		return values;
	}
	/*...........................................................*/
	public short getValue(int index){
		if (!legalIndex(index))
			return unassigned;
		else
			return values[index];
	}
	/*...........................................................*/
	public void setValue(int index, short value) {
		if (legalIndex(index))
			values[index] = value;
	}
	/*...........................................................*/
	public void setValues(short[] values) {
		if (values==null)
			this.values = new short[0];
		else
			this.values= values;
	}
	/*...........................................................*/
	public void copyTo(ShortArray d){
		if (d==null || d.values.length!=values.length)
			return;
		for (int i=0; i<values.length; i++)
			d.values[i] =  values[i];
	}
	/*...........................................................*/
	public void zeroArray(){
		
		for (int i=0; i<values.length; i++)
			values[i] =  0;
	}
	public static void zeroArray(short[] values){
		if (values==null)
			return;
		for (int i=0; i<values.length; i++)
			values[i] =  0;
	}
	/*...........................................................*/
	public void deassignArray(){
		for (int i=0; i<values.length; i++)
			values[i] =  unassigned;
	}
	/*...........................................................*/
	public static void deassignArray(short[] values){
		if (values==null)
			return;
		for (int i=0; i<values.length; i++)
			values[i] =  unassigned;
	}
	/*...........................................................*/
	public int indexOf(short match){
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int indexOf(short[] values, short match){
		if (values==null)
			return -1;
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int countSame(short[] a, short match){
		if (a==null)
			return 0;
		int count= 0;
		for (int i=0; i<a.length; i++)
			if (a[i]== match)
				count++;
		return count;
	}
	/*...........................................................*/
	public boolean fillNextUnassigned(short v){
		for (int i=0; i<values.length; i++)
			if (values[i]== unassigned) {
				values[i] = v;
				return true;
			}
		return false;
	}
	/*...........................................................*/
	public static void sort(short[] array){
		if (array==null || array.length<=1)
			return;
		
		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && array[j]>array[j+1]; j--) {
				short temp = array[j];
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
		short[] newLongValues = new short[newNum];
		for (int i=0; i<getSize() && i<newNum; i++)
			newLongValues[i]=values[i];
		if (newNum>getSize())
			for (int i=getSize(); i<newNum; i++)
				newLongValues[i]=unassigned;
		values=newLongValues;
	}
	/*...........................................................*/
	public void addParts(int starting, int num, short defaultValue) {
		values=addParts(values, starting, num, defaultValue);
	}
	/*...........................................................*/
	public static short[] addParts(short[] d, int starting, int num, short defaultValue) {
		if (num<=0 || d==null)
			return d;
		
		if (d.length <= 0) {
			short[] newMatrix=new short[num];
			for (int ic=0; ic<num; ic++){
					newMatrix[ic]= defaultValue; //filling with missing data
			}
			return newMatrix;
		}
		
		if (starting<0) 
			starting = -1;
		if (starting>d.length) 
			starting = d.length-1;
		int newNum = d.length + num;
		short[] newValues = new short[newNum];
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
	public static short[] deleteParts(short[] d, int starting, int num) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		if (num+starting>d.length)
			num = d.length-starting;
		int newNum =d.length - num;
		short[] newValues = new short[newNum];
		for (int i=0; i<starting; i++)
			newValues[i]=d[i];
		for (int i=starting+num; i<d.length; i++)
			newValues[i-num]=d[i];
		return newValues;
	}
	/*...........................................................*/
	public static short[] getMoveParts(short[] d, int starting, int num, int justAfter) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		short[] newValues = new short[d.length];

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
		short temp = values[first];
		values[first] = values[second];
		values[second] = temp;
		return true;
	}
	/*...........................................................*/
	public static void swapParts(short[] d, int first, int second) {
		if (d == null || first<0 || first>=d.length || second<0 || second>=d.length) 
			return;
		short temp = d[first];
		d[first] = d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void moveParts(short[] d, int starting, int num, int justAfter) {   //DRM new 7 March 08
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		short[] newValues = new short[d.length];
		if (starting>justAfter){
			int count =justAfter+1;
			
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues[count++]=d[i];

			for (int i=justAfter+1; i<=starting+num-1; i++)
				d[i]=newValues[i];

		}
		else {  // moving down			
			int count =starting;
			
			for (int i=starting+num; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			
			for (int i=starting; i<=justAfter; i++)
				d[i]=newValues[i];
		}
	}
	/*...........................................................*/
	public static void movePartsOriginal(short[] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		short[] newValues = new short[d.length];
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
	public static String toString(short[] vector){
		if (vector==null ||  vector.length==0)
			return null;  
		StringBuffer result = new StringBuffer(vector.length*2);
		result.append('[');
		for (int i=0; i<vector.length; i++) {
			if (vector[i] == unassigned)
				result.append('?');
			else
				result.append(Short.toString(vector[i]));
			result.append(' ');
		}
		result.append(']');
		return result.toString();
	}
}


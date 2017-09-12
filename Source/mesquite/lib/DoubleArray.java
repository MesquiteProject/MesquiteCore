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
public class DoubleArray implements Listable  {
	double[] values;
	NameReference name=null;
	int autoExpandAmount = 0;
	public DoubleArray(int num, int autoExpandAmount){	
		values = new double[num];
		for (int i=0; i<num; i++)
			values[i] =  MesquiteDouble.unassigned;
		this.autoExpandAmount = autoExpandAmount;
	}
	public DoubleArray(int num){	
		this(num, 0);
	}
	//used for Associables that might need to record whether reference is to part or in between part
	//intially for MesquiteTree to know if info applies to node or branch ancestral to node
	boolean between = false;
	public void setBetweenness(boolean b){
		between = b;  
	}
	public boolean isBetween(){
		return between;
	}
	/*...........................................................*/
	public static double[] clone(double[] d){
		if (d==null)
			return d;
		double[] q = new double[d.length];
		for (int i=0; i< d.length; i++)
			q[i] = d[i];
		return q;
	}
	/*...........................................................*/
	public static double[] copyIntoDifferentSize(double[] d, int newLength, double defaultValue){
		if (d==null)
			return null;
		double[] values = new double[newLength];
		for (int i=0; i<values.length; i++)
			if (i<d.length)
				values[i] =  d[i];
			else
				values[i] = defaultValue;
		return values;
	}
	/*...........................................................*/
	public boolean legalIndex(int i){
		return (values!=null && i>=0 && i<values.length);
	}
	/*...........................................................*/
	public double[] getMatrix(){
		return values;
	}
	/*...........................................................*/
	public double getValue(int index){
		if (!legalIndex(index))
			return MesquiteDouble.unassigned;
		else
			return values[index];
	}
	/*...........................................................*/
	public void setValue(int index, double value) {
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
	public void setValues(double[] values) {
		if (values==null)
			this.values = new double[0];
		else
			this.values= values;
	}
	/*...........................................................*/
	public void copyTo(DoubleArray d){
		if (d==null || d.values.length!=values.length)
			return;
		for (int i=0; i<values.length; i++)
			d.values[i] =  values[i];
	}
	/*...........................................................*/
	public static double[] copy(double[] d){
		if (d==null)
			return null;
		double[] values = new double[d.length];
		for (int i=0; i<values.length; i++)
			d[i] =  values[i];
		return values;
	}
	/*...........................................................*/
	public void zeroArray(){
		for (int i=0; i<values.length; i++)
			values[i] =  0;
	}
	public static void zeroArray(double[] values){
		if (values==null)
			return;
		for (int i=0; i<values.length; i++)
			values[i] =  0;
	}
	/*...........................................................*/
	public void deassignArray(){
		for (int i=0; i<values.length; i++)
			values[i] =  MesquiteDouble.unassigned;
	}
	/*...........................................................*/
	public static void deassignArray(double[] values){
		if (values==null)
			return;
		for (int i=0; i<values.length; i++)
			values[i] =  MesquiteDouble.unassigned;
	}
	public void resetSize(int newNum) {
		if (newNum == getSize())
			return;
		double[] newValues = new double[newNum];
		for (int i=0; i<getSize() && i<newNum; i++)
			newValues[i]=values[i];
		if (newNum>getSize())
			for (int i=getSize(); i<newNum; i++)
				newValues[i]=MesquiteDouble.unassigned;
		values=newValues;
	}
	/*...........................................................*/
	public void addParts(int starting, int num) {
		values=addParts(values, starting, num);
	}
	/*...........................................................*/
	public static double[] addParts(double[] d, int starting, int num) {
		if (num<=0 || d==null)
			return d;
		/*#
		if (d.length <= 0) {
			double[] newMatrix=new double[num];
			for (int ic=0; ic<num; ic++){
					newMatrix[ic]= defaultValue; //filling with missing data
			}
			return newMatrix;
		}
		*/
		if (starting<0) 
			starting = -1;
		if (starting>d.length) 
			starting = d.length-1;
		int newNum = d.length + num;
		double[] newValues = new double[newNum];
		for (int i=0; i<=starting; i++)
			newValues[i]=d[i];
		for (int i=0; i<num ; i++)
			newValues[starting + i + 1]=MesquiteDouble.unassigned;
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
	public static double[] deleteParts(double[] d, int starting, int num) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		if (num+starting>d.length)
			num = d.length-starting;
		int newNum =d.length - num;
		double[] newValues = new double[newNum];
		for (int i=0; i<starting; i++)
			newValues[i]=d[i];
		for (int i=starting+num; i<d.length; i++)
			newValues[i-num]=d[i];
		return newValues;
	}
	/*...........................................................*/
	public void moveParts(int starting, int num, int justAfter) {
		moveParts(values, starting, num, justAfter);
	}
	/*...........................................................*/
	public static void moveParts(double[] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		double[] newValues = new double[d.length];
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
	public static void swapParts(double[] d, int first, int second) {
		if (d == null || first<0 || first>=d.length || second<0 || second>=d.length) 
			return;
		double temp = d[first];
		d[first] = d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public boolean swapParts(int first, int second) {
		if (first<0 || first>=values.length || second<0 || second>=values.length) 
			return false;
		double temp = values[first];
		values[first] = values[second];
		values[second] = temp;
		return true;
	}
	/*...........................................................*/
	public static double[] getMoveParts(double[] d, int starting, int num, int justAfter) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		double[] newValues = new double[d.length];

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
	public int indexOf(double match){
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int indexOf(double[] values, double match){
		if (values==null)
			return -1;
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int countSame(double[] a, double match){
		if (a==null)
			return 0;
		int count= 0;
		for (int i=0; i<a.length; i++)
			if (a[i]== match)
				count++;
		return count;
	}
	/*...........................................................*/
	public static double maximum(double[] values){
		if (values==null)
			return MesquiteDouble.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteDouble.unassigned);
		for (int i=0; i<values.length; i++)
			d.setMeIfIAmLessThan(values[i]);
		return d.getDoubleValue();
	}
	/*...........................................................*/
	public double maximum(){
		if (values==null)
			return MesquiteDouble.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteDouble.unassigned);
		for (int i=0; i<values.length; i++)
			d.setMeIfIAmLessThan(values[i]);
		return d.getDoubleValue();
	}
	/*...........................................................*/
	public static double minimum(double[] values){
		if (values==null)
			return MesquiteDouble.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteDouble.unassigned);
		for (int i=0; i<values.length; i++)
			d.setMeIfIAmMoreThan(values[i]);
		return d.getDoubleValue();
	}
	/*...........................................................*/
	public double minimum(){
		if (values==null)
			return MesquiteDouble.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteDouble.unassigned);
		for (int i=0; i<values.length; i++)
			d.setMeIfIAmMoreThan(values[i]);
		return d.getDoubleValue();
	}
	/*...........................................................*/
	public boolean fillNextUnassigned(double v){
		for (int i=0; i<values.length; i++)
			if (values[i]== MesquiteDouble.unassigned) {
				values[i] = v;
				return true;
			}
		return false;
	}
	/*...........................................................*/
	public static void sort(double[] array){
		if (array==null || array.length<=1)
			return;
		
		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && array[j]>array[j+1]; j--) {
				double temp = array[j];
				array[j] = array[j+1];
				array[j+1]=temp;
			}
		}
		
	}
	/*...........................................................*/
	public static void sortByFirst(double[] array, double[] other){
		if (array==null || array.length<=1 || other==null || other.length<=1 || array.length !=other.length)
			return;
		
		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && array[j]>array[j+1]; j--) {
				double temp = array[j];
				array[j] = array[j+1];
				array[j+1]=temp;
				temp = other[j];
				other[j] = other[j+1];
				other[j+1]=temp;
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
	/** DOCUMENT */
	public String toString() {
		String s = "[ ";
		for (int i=0; i<values.length; i++)
			s +=  MesquiteDouble.toString(values[i]) + " ";
		s += "]";
		return s;
	}
	public static String toString(double[] values) {
		if (values==null)
			return "";
		String s = "[ ";
		for (int i=0; i<values.length; i++)
			s += values[i] + "  ";//s +=  MesquiteDouble.toString(values[i]) + " ";
		s += "]";
		return s;
	}
}


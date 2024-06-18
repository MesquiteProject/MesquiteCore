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
public class Long2DArray {
	long[][] values;
	int numC;
	 int numT;
	NameReference name=null;
	public Long2DArray(int numC, int numT){
		this.numC = numC;
		this.numT = numT;	
		values = new long[numC][numT];
		for (int i=0; i<numC; i++)
			for (int j=0; j<numT; j++)
				values[i][j] =  MesquiteLong.unassigned;
	}
	public Long2DArray(long[][] input){
		this(numFullColumns(input), numFullRows(input));
		for (int i=0; i<numC; i++)
			for (int j=0; j<numT; j++)
				values[i][j] =  input[i][j];
	}
	/*...........................................................*/
	public static int getMaxColumnLength(long[][] matrix){
		int maxLength = 0;
		for (int i=0; i<matrix.length; i++) {
			if (matrix[i]!=null)
				maxLength = MesquiteInteger.maximum(matrix[i].length, maxLength);  //maxLength stores the maximum row length
		}
		return maxLength;
	}
	/*...........................................................*/
	public static long[][] transpose(long[][] matrix){
		if (matrix==null || matrix.length==0 || matrix[0] == null  || matrix[0].length == 0 )
			return null;
		int maxLength = getMaxColumnLength(matrix);
		long[][] transposed = new long[maxLength][matrix.length];
		for (int ic=0; ic<transposed.length; ic++)
			for (int it=0; it<transposed[ic].length; it++)
				transposed[ic][it] =  0;
		for (int ic=0; ic<matrix.length; ic++)
			for (int it=0; it<matrix[ic].length; it++)
				transposed[it][ic] =  matrix[ic][it];
		return transposed;
	}
	/*...........................................................*/
	public static int numFullRows(long[][] matrix){
		if (matrix==null || matrix.length==0 || matrix[0] == null  || matrix[0].length == 0 )
			return 0;
		int n = matrix[0].length;
		for (int i=0; i<matrix.length; i++) {
			if (matrix[i]==null || n!= matrix[i].length)
				return 0;
		}
		return n;
	}
	/*...........................................................*/
	public static int numFullColumns(long[][] matrix){
		if (matrix==null || matrix.length==0 || matrix[0] == null  || matrix[0].length == 0 )
			return 0;
		int n = matrix[0].length;
		for (int i=0; i<matrix.length; i++) {
			if (matrix[i]==null || n!= matrix[i].length)
				return 0;
		}
		return matrix.length;
	}
	/*...........................................................*/
	public long getValue(int ic, int it){
		if (values==null || (ic <0 || ic >= values.length) || (it <0 || it >= values[ic].length))
			return MesquiteLong.unassigned;
		else
			return values[ic][it];
	}
	/*...........................................................*/
	public long[] getValues(int ic) {
		if (values == null || ic <0 || ic >= values.length)
			return null;
		else
			return values[ic];
	}
	/*...........................................................*/
	public long[][] getMatrix() {
		return values;
	}
	/*...........................................................*/
	public void setValue(int ic, int it, long value) {
		if (values!=null && ic >=0 && ic < values.length)
		 	if (it >=0 && it < values[ic].length)
				values[ic][it] = value;
	}
	/*...........................................................*/
	public void setValues(Long2DArray incoming) {
		if (incoming !=null) {
			resetSize(incoming.getSizeC(), incoming.getSizeT());
			for (int it=0; it<incoming.getSizeT() && it < getSizeT(); it++) {
				for (int ic=0; ic<incoming.getSizeC() && ic < getSizeC(); ic++) {
					setValue(ic, it, incoming.getValue(ic, it));
				}
			}
		}
	}
	/*...........................................................*/
	public void zeroArray(){
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
			values[ic][it] =  0;
	}
	/*...........................................................*/
	public static void zeroArray(long [][] array){
		for (int ic=0; ic<array.length; ic++)
			for (int it=0; it<array[ic].length; it++)
			array[ic][it] =  0;
	}
	/*...........................................................*/
	public void deassignArray(){
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
			values[ic][it] =  MesquiteLong.unassigned;
	}
	/*...........................................................*/
	/** Changes the array size to the new dimensions*/
	public void resetSize(int newNumC, int newNumT) {
		if (newNumC == numC && newNumT == numT)
			return;
		long[][] newLongValues = new long[newNumC][newNumT];
		for (int i=0; i<newNumC; i++)
			for (int j=0; j<newNumT; j++) {
				if (i<numC && j<numT)
					newLongValues[i][j]=values[i][j];
				else
					newLongValues[i][j]=0;
			}
		values=newLongValues;
		numC=newNumC;
		numT=newNumT;
	}
	/*...........................................................*/
	public int getSizeC() {
		return numC;
	}
	/*...........................................................*/
	public int getSizeT() {
		return numT;
	}
	/*...........................................................*/
	public static long maximumInColumn(long[][] values, int column){
		if (values==null || values.length<=column || values[column]==null)
			return MesquiteLong.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteLong.unassigned);
		for (int i=0; i<values[column].length; i++)
			d.setMeIfIAmLessThan(values[column][i]);
		return d.getLongValue();
	}
	/*...........................................................*/
	public static long minimumInColumn(long[][] values, int column){
		if (values==null || values.length<=column || values[column]==null)
			return MesquiteLong.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteLong.unassigned);
		for (int i=0; i<values[column].length; i++)
			d.setMeIfIAmMoreThan(values[column][i]);
		return d.getLongValue();
	}
	/*...........................................................*/
	public static void swapColumns(long[][] d, int first, int second) {
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		long[] temp = d[first];
		d[first]=d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void moveColumns(long[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		long[][] newValues = new long[d.length][];
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
	public static long[][] addColumns(long[][] d, int numRows, long defaultValue, int starting, int num){
		if (num <= 0 || d == null)
			return d;
		int numChars = d.length;
		if (d.length <= 0) {
			long[][] newMatrix=new long[numChars+num][numRows];
			for (int ic=0; ic<numChars+num; ic++){
				for (int it=0; it<numRows; it++) 
					newMatrix[ic][it] = defaultValue; //filling with missing data
			}
			return newMatrix;
		}
		int numTaxa = d[0].length;
		if (numTaxa == 0 && numRows >=0)
			numTaxa = numRows;
		if (starting<0)
			starting = -1;
		else if (starting>=numChars)
			starting = numChars-1;
		int newNumChars = numChars + num;
		
		long[][] newMatrix=new long[newNumChars][numTaxa];
		for (int ic=0; ic<newNumChars; ic++){
			for (int it=0; it<numTaxa; it++) 
				newMatrix[ic][it] = defaultValue; //filling with missing data
		}
		for (int ic=0; ic<=starting && ic < d.length; ic++){
			for (int it=0; it<numTaxa; it++) 
				newMatrix[ic][it] = d[ic][it]; //transferring old first part
		}
		for (int ic=0; ic<numChars - starting -1 ; ic++){
			for (int it=0; it<numTaxa; it++) 
				newMatrix[ic + starting + num + 1][it] = d[starting + ic+1][it]; //transferring old second part
		}
		return newMatrix;
		
	}
	/*.................................................................................................................*/
	public static long[] extractRow(long[][] array, int row) {
		long[] newArray = new long[array.length];
		for (int i = 0; i<array.length; i++) {
			newArray[i] = array[i][row];
		}
		return newArray;
	}
	/*...........................................................*/
	public static long[][] addColumns(long[][] d, long defaultValue, int starting, int num){
		return addColumns(d, -1, defaultValue, starting, num);
	}
	/*...........................................................*/
	public static long[][] deleteColumns(long[][] d, int starting, int num){
		if (num <= 0 || d == null)
			return d;
		int numChars = d.length;
		if (starting<0)
			return d;
		else if (starting>numChars)
			return d;
		if (num+starting>numChars)
			num = numChars-starting;
		int newNumChars =numChars-num;


		if (d.length <= 0)
			return new long[numChars-num][];
		int numTaxa = d[0].length;
		
		long[][] newMatrix=new long[newNumChars][numTaxa];
		for (int ic=0; ic<starting; ic++){
			for (int it=0; it<numTaxa && it< d[ic].length; it++) 
				newMatrix[ic][it] = d[ic][it]; //transferring old first part
		}
		for (int ic=starting + num; ic<numChars  && ic-num<newNumChars; ic++){
			for (int it=0; it<numTaxa && it< d[ic].length; it++) 
				newMatrix[ic-num][it] = d[ic][it]; //transferring old second part
		}
		return newMatrix;
	}
	
	/*...........................................................*/
	public static long[][] deleteColumnsByBlocks(long[][] d, int[][] blocks){
		if (d == null)
			return null;
		if (d.length <= 0)
			return d;
		if (blocks == null || blocks.length == 0)
			return d;

		int numRows= d[0].length;
		int availableSlot = blocks[0][0];
		//First shift storage toward the start of the array. Later, we'll delete the leftovers at the end.
		for (int block = 0; block<blocks.length; block++) {
			int startOfPreserved = blocks[block][1]+1;
			int endOfPreserved = d.length-1;
			if (block+1<blocks.length) //there's another block coming afterward
				endOfPreserved = blocks[block+1][0]-1;
			for (int ic=startOfPreserved; ic<=endOfPreserved; ic++) {
				for (int it=0; it<numRows && it< d[ic].length; it++) {
					d[availableSlot][it] = d[ic][it];
				}
				availableSlot++;
			}
		}
		//Next, trim leftovers
		int newNumColumns = availableSlot;
		long[][] newMatrix=new long[newNumColumns][numRows];
		for (int ic=0; ic<newNumColumns; ic++) 
			for (int it=0; it<numRows && it< d[ic].length; it++) 
			newMatrix[ic][it] = d[ic][it];
		return newMatrix;
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
	public String getName(){
		if (name!=null)
			return name.getValue();
		else
			return "";
	}
	public static String toString(long[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		StringBuffer result = new StringBuffer(2*numRows*numColumns);
		for (int j=0; j<numRows; j++) {
			result.append('[');
			for (int i=0; i<numColumns; i++) {
				
				result.append(MesquiteLong.toString(matrix[i][j]));
				result.append(' ');
			}
			result.append(']');
			result.append('\n');
		}
		return result.toString();
	}
}


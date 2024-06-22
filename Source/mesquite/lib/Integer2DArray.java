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
public class Integer2DArray {
	int[][] values;
	int numC;
	 int numT;
	NameReference name=null;
	public Integer2DArray(int numC, int numT){
		this.numC = numC;
		this.numT = numT;	
		values = new int[numC][numT];
		for (int i=0; i<numC; i++)
			for (int j=0; j<numT; j++)
				values[i][j] =  MesquiteInteger.unassigned;
	}
	public Integer2DArray(int[][] input){
		this(numFullColumns(input), numFullRows(input));
		for (int i=0; i<numC; i++)
			for (int j=0; j<numT; j++)
				values[i][j] =  input[i][j];
	}
	/*...........................................................*/
	public void dispose(){
		values = null;
	}
	/*...........................................................*/
	public static int[][] clone(int[][] d){
		if (d==null || d.length==0)
			return d;
		int[][] q = new int[d.length][d[0].length];
		for (int i=0; i< d.length; i++)
			for (int j=0; j< d[i].length;j++)
				q[i][j] = d[i][j];
		return q;
	}
	/*...........................................................*/
	public static int[][] cloneIncreaseSize(int[][] d, int first, int second){
		if (d==null || d.length==0)
			return d;
		first = MesquiteInteger.maximum(first, d.length);
		second= MesquiteInteger.maximum(second, d[0].length);
		int[][]  q = new int[first][second];
		for (int i=0; i< q.length; i++)
			for (int j=0; j< q[i].length;j++)
				if (i>=d.length || j>=d[i].length)
					q[i][j] = 0;
				else
					q[i][j] = d[i][j];
		return q;
	}

	/*...........................................................*/
	public void deleteColumnsFlagged(Bits toDelete){
		values = deleteColumnsFlagged(values, toDelete);
		numC = values.length;
		if (numC == 0)
			numT = 0;
		else
			numT = values[0].length;
	}
	/*...........................................................*/
	public static int[][] deleteColumnsFlagged(int[][] d, Bits toDelete) {
		if (d == null)
			return null;
		if (d.length <= 0)
			return d;
		int numRows= d[0].length;
		if (numRows == 0)
			return d;
		if (toDelete == null)
			return d;

		Bits flags = toDelete.cloneBits(); 
		int toFill =flags.nextBit(0, true); //find next to be cleared
		int source = flags.nextBit(toFill, false); //find source to move into it
		int highestFilled = toFill-1; //
		while (source >=0 && toFill >=0) { //First, compact storage toward the start of the array.
			for (int it=0; it<numRows; it++)
				d[toFill][it] = d[source][it]; //move content from source to place
			highestFilled = toFill;
			flags.setBit(source, true); // set to available to receive
			toFill =flags.nextBit(++toFill, true);
			source =flags.nextBit(++source, false);	
		}
		//Next, trim leftovers
		int newNumColumns = highestFilled+1;
		int[][] newMatrix=new int[newNumColumns][numRows];
		for (int ic=0; ic<newNumColumns; ic++) 
			for (int it=0; it<numRows && it< d[ic].length; it++) 
				newMatrix[ic][it] = d[ic][it];
		return newMatrix;
	}	
	/*...........................................................*
	public void deleteColumnsBy Blocks(int[][] blocks){
		values = deleteColumnsBy Blocks(values, blocks);
		numC = values.length;
		if (numC == 0)
			numT = 0;
		else
			numT = values[0].length;
	}
	/*...........................................................*
	public static int[][] deleteColumnsBy Blocks(int[][] d, int[][] blocks){
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
		int[][] newMatrix=new int[newNumColumns][numRows];
		for (int ic=0; ic<newNumColumns; ic++) 
			for (int it=0; it<numRows && it< d[ic].length; it++) 
			newMatrix[ic][it] = d[ic][it];
		return newMatrix;
}
	/*...........................................................*/
	public static void swapColumns(int[][] d, int first, int second) {
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		int[] temp = d[first];
		d[first]=d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void moveColumns(int[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		int[][] newValues = new int[d.length][];
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
	public static int numFullRows(int[][] matrix){
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
	public static int numFullColumns(int[][] matrix){
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
	public int getValue(int ic, int it){
		if (values==null || (ic <0 || ic >= values.length) || (it <0 || it >= values[ic].length))
			return MesquiteInteger.unassigned;
		else
			return values[ic][it];
	}
	/*...........................................................*/
	public int[] getValues(int ic) {
		if (values==null || ic <0 || ic >= values.length)
			return null;
		else
			return values[ic];
	}
	/*...........................................................*/
	public int[][] getMatrix() {
		return values;
	}
	/*...........................................................*/
	public void setValue(int ic, int it, int value) {
		if (values!=null && ic >=0 && ic < values.length)
		 	if (it >=0 && it < values[ic].length)
				values[ic][it] = value;
	}
	/*...........................................................*/
	public void setValues(Integer2DArray incoming) {
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
	public static void zeroArray(int[][] values){
		if (values==null)
			return;
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
			values[ic][it] =  0;
	}
	/*...........................................................*/
	public static void deassignArray(int[][] values){
		if (values==null)
			return;
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
			values[ic][it] =  MesquiteInteger.unassigned;
	}
	/*...........................................................*/
	public void deassignArray(){
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
			values[ic][it] =  MesquiteInteger.unassigned;
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
	/** Changes the array size to the new dimensions*/
	public void resetSize(int newNumC, int newNumT) {
		if (newNumC == numC && newNumT == numT)
			return;
		int[][] newIntValues = new int[newNumC][newNumT];
		for (int i=0; i<newNumC; i++)
			for (int j=0; j<newNumT; j++) {
				if (i<numC && j<numT)
					newIntValues[i][j]=values[i][j];
				else
					newIntValues[i][j]=0;
			}
		values=newIntValues;
		
		numC=newNumC;
		numT=newNumT;
	}
	/*...........................................................*/
	public static int minimum(int[][] values){
		if (values==null)
			return MesquiteInteger.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteInteger.unassigned);
		for (int i=0; i<values.length; i++)
			if (values[i]!=null)
				for (int j=0; j<values[i].length; j++)
					d.setMeIfIAmMoreThan(values[i][j]);
		return d.getIntValue();
	}
	/*...........................................................*/
	public static int maximum(int[][] values){
		if (values==null)
			return MesquiteInteger.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteInteger.unassigned);
		for (int i=0; i<values.length; i++)
			if (values[i]!=null)
				for (int j=0; j<values[i].length; j++)
					d.setMeIfIAmLessThan(values[i][j]);
		return d.getIntValue();
	}
	/*...........................................................*/
	public static int maximumInColumn(int[][] values, int column){
		if (values==null || values.length<=column || values[column]==null)
			return MesquiteInteger.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteInteger.unassigned);
		for (int i=0; i<values[column].length; i++)
			d.setMeIfIAmLessThan(values[column][i]);
		return d.getIntValue();
	}
	/*...........................................................*/
	public static int minimumInColumn(int[][] values, int column){
		if (values==null || values.length<=column || values[column]==null)
			return MesquiteInteger.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteInteger.unassigned);
		for (int i=0; i<values[column].length; i++)
			d.setMeIfIAmMoreThan(values[column][i]);
		return d.getIntValue();
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
	public static String toString(int[][] matrix){
		return toString(matrix, false);
	}

	public static String toString(int[][] matrix, boolean showAsTranspose){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		StringBuffer result = new StringBuffer(2*numRows*numColumns);
		result.append("rows: " + numRows + "; columns: " + numColumns + "\n");
		if (showAsTranspose){
			for (int j=0; j<numColumns; j++) {
				result.append('[');
				for (int i=0; i<numRows; i++) {
					
					result.append(MesquiteInteger.toString(matrix[j][i]));
					result.append('\t');
				}
				result.append(']');
				result.append('\n');
			}
		}
		else {
			for (int j=0; j<numRows; j++) {
				result.append('[');
				for (int i=0; i<numColumns; i++) {
					
					result.append(MesquiteInteger.toString(matrix[i][j]));
					result.append('\t');
				}
				result.append(']');
				result.append('\n');
			}
		}
		return result.toString();
	}
}


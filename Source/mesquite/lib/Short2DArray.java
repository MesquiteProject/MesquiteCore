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

/* ======================================================================== */
public class Short2DArray {
	short[][] values;
	int numC;
	 int numT;
	NameReference name=null;
	public Short2DArray(int numC, int numT){
		this.numC = numC;
		this.numT = numT;	
		values = new short[numC][numT];
		for (int i=0; i<numC; i++)
			for (int j=0; j<numT; j++)
				values[i][j] =  ShortArray.unassigned;
	}
	public Short2DArray(short[][] input){
		this(numFullColumns(input), numFullRows(input));
		for (int i=0; i<numC; i++)
			for (int j=0; j<numT; j++)
				values[i][j] =  input[i][j];
	}
	/*...........................................................*/
	public static int numFullRows(short[][] matrix){
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
	public static int numFullColumns(short[][] matrix){
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
	public short getValue(int ic, int it){
		if (values==null || (ic <0 || ic >= values.length) || (it <0 || it >= values[ic].length))
			return ShortArray.unassigned;
		else
			return values[ic][it];
	}
	/*...........................................................*/
	public short[] getValues(int ic) {
		if (values == null || ic <0 || ic >= values.length)
			return null;
		else
			return values[ic];
	}
	/*...........................................................*/
	public short[][] getMatrix() {
		return values;
	}
	/*...........................................................*/
	public void setValue(int ic, int it, short value) {
		if (values!=null && ic >=0 && ic < values.length)
		 	if (it >=0 && it < values[ic].length)
				values[ic][it] = value;
	}
	/*...........................................................*/
	public void setValues(Short2DArray incoming) {
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
	public void deassignArray(){
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
			values[ic][it] =  ShortArray.unassigned;
	}
	/*...........................................................*/
	/** Changes the array size to the new dimensions*/
	public void resetSize(int newNumC, int newNumT) {
		if (newNumC == numC && newNumT == numT)
			return;
		short[][] newLongValues = new short[newNumC][newNumT];
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
	public static void swapColumns(short[][] d, int first, int second) {
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		short[] temp = d[first];
		d[first]=d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void moveColumns(short[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		short[][] newValues = new short[d.length][];
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
	public static short[][] addColumns(short[][] d, int numRows, short defaultValue, int starting, int num){
		if (num <= 0 || d == null)
			return d;
		int numChars = d.length;
		if (d.length <= 0) {
			short[][] newMatrix=new short[numChars+num][numRows];
			for (int ic=0; ic<numChars+num; ic++){
				for (int it=0; it<numRows; it++) 
					newMatrix[ic][it] = defaultValue; //filling with missing data
			}
			return newMatrix;
		}
		int numTaxa = d[0].length;
		if (numTaxa <= 0 && numRows>=0)
			numTaxa = numRows;
		else if (numTaxa<0)
			numTaxa=0;
		if (starting<0)
			starting = -1;
		else if (starting>=numChars)
			starting = numChars-1;
		int newNumChars = numChars + num;
		if (newNumChars<0) {
			MesquiteMessage.printStackTrace("Short2DArray.addColumns with newNumChars = " + newNumChars);
			MesquiteMessage.println("numChars: " + numChars + ", num: " + num+ ", d.length: " + num);
			return d;
		}
		if (numTaxa<0) {
			MesquiteMessage.printStackTrace("Short2DArray.addColumns with numTaxa = " + numTaxa);
			return d;
		}
		short[][] newMatrix=new short[newNumChars][numTaxa];
		for (int ic=0; ic<newNumChars; ic++){
			for (int it=0; it<numTaxa; it++) 
				newMatrix[ic][it] = defaultValue; //filling with missing data
		}
		for (int ic=0; ic<=starting; ic++){
			for (int it=0; it<numTaxa; it++) 
				newMatrix[ic][it] = d[ic][it]; //transferring old first part
		}
		for (int ic=0; ic<numChars - starting -1 ; ic++){
			for (int it=0; it<numTaxa; it++) 
				newMatrix[ic + starting + num + 1][it] = d[starting + ic+1][it]; //transferring old second part
		}
		return newMatrix;
		
	}
	/*...........................................................*/
	public static short[][] addColumns(short[][] d, short defaultValue, int starting, int num){
		return addColumns(d, -1, defaultValue, starting, num);
	}
	/*...........................................................*/
	public static short[][] deleteColumns(short[][] d, int starting, int num){
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
			return new short[numChars-num][];
		int numTaxa = d[0].length;
		
		short[][] newMatrix=new short[newNumChars][numTaxa];
		for (int ic=0; ic<starting; ic++){
			for (int it=0; it<numTaxa && it< d[ic].length; it++) 
				newMatrix[ic][it] = d[ic][it]; //transferring old first part
		}
		for (int ic=starting + num; ic<numChars && ic-num<newNumChars; ic++){
			for (int it=0; it<numTaxa && it< d[ic].length; it++) {
				newMatrix[ic-num][it] = d[ic][it]; //transferring old second part
			}
		}
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
	public static String toString(short[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		StringBuffer result = new StringBuffer(2*numRows*numColumns);
		for (int j=0; j<numRows; j++) {
			result.append('[');
			for (int i=0; i<numColumns; i++) {
				
				result.append(Short.toString(matrix[i][j]));
				result.append(' ');
			}
			result.append(']');
			result.append('\n');
		}
		return result.toString();
	}
}


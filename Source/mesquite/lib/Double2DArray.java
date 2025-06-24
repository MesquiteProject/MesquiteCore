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

/*Last documented:  August 1999 */
/* ======================================================================== */
public class Double2DArray {
	double[][] values;
	int numC;
	int numT;
	NameReference name=null;
	public Double2DArray(int numC, int numT){
		this.numC = numC;
		this.numT = numT;	
		values = new double[numC][numT];
		for (int i=0; i<numC; i++)
			for (int j=0; j<numT; j++)
				values[i][j] =  MesquiteDouble.unassigned;
	}
	public Double2DArray(double[][] input){
		this(numFullColumns(input), numFullRows(input));
		for (int i=0; i<numC; i++)
			for (int j=0; j<numT; j++)
				values[i][j] =  input[i][j];
	}
	/*...........................................................*/
	public static double[][] clone(double[][] d){
		if (d==null || d.length==0)
			return d;
		double[][] q = new double[d.length][d[0].length];
		for (int i=0; i< d.length; i++)
			for (int j=0; j< d[i].length;j++)
				q[i][j] = d[i][j];
		return q;
	}
	/*...........................................................*/
	public static double[][] cloneIncreaseSize(double[][] d, int first, int second){
		if (d==null || d.length==0)
			return d;
		first = MesquiteInteger.maximum(first, d.length);
		second= MesquiteInteger.maximum(second, d[0].length);
		double[][]  q = new double[first][second];
		for (int i=0; i< q.length; i++)
			for (int j=0; j< q[i].length;j++)
				if (i>=d.length || j>=d[i].length)
					q[i][j] = 0.0;
				else
					q[i][j] = d[i][j];
		return q;
	}
	/*...........................................................*/
	public static double[][][] cloneIncreaseSize(double[][][] d, int first, int second, int third){
		if (d==null || d.length==0)
			return d;
		first = MesquiteInteger.maximum(first, d.length);
		second= MesquiteInteger.maximum(second, d[0].length);
		third= MesquiteInteger.maximum(third, d[0][0].length);
		double[][][]  q = new double[first][second][third];
		for (int i=0; i< q.length; i++)
			for (int j=0; j< q[i].length;j++)
				for (int k=0; k< q[i][j].length;k++)
				if (i>=d.length || j>=d[i].length || k>=d[i][j].length)
					q[i][j][k]= 0.0;
				else
					q[i][j][k] = d[i][j][k];
		return q;
	}
	/*...........................................................*/
	public void dispose(){
		values = null;
	}
	/*...........................................................*/
	public static int numFullColumns(double[][] matrix){
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
	public static int numFullRows(double[][] matrix){
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
	public double getValue(int ic, int it){
		if (values==null || (ic <0 || ic >= values.length) || (it <0 || it >= values[ic].length))
			return MesquiteDouble.unassigned;
		else
			return values[ic][it];
	}
	/*...........................................................*/
	public double[] getValues(int ic) {
		if (values == null || ic <0 || ic >= values.length)
			return null;
		else
			return values[ic];
	}
	/*...........................................................*/
	public double[][] getMatrix() {
		return values;
	}
	/*...........................................................*/
	public void setValue(int ic, int it, double value) {
		if (values!=null && ic >=0 && ic < values.length)
			if (it >=0 && it < values[ic].length)
				values[ic][it] = value;
	}
	/*...........................................................*/
	public void setValues(Double2DArray incoming) {
		if (incoming !=null) {
			resetSize(incoming.getSizeC(), incoming.getSizeT());
			for (int it=0; it < getSizeT(); it++) {
				for (int ic=0; ic < getSizeC(); ic++) {
					setValue(ic, it, incoming.getValue(ic, it));
				}
			}
		}
	}
	/*...........................................................*/
	public void setValues(double[][] matrix) {
		if (matrix !=null) {
			numC = matrix.length;
			if (numC>0)
				numT = matrix[0].length;	
			else
				numT=0;
			values = matrix;
		}
	}
	/*...........................................................*/
	public void zeroArray(){
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
				values[ic][it] =  0;
	}
	/*...........................................................*/
	public static void zeroArray(double[][] values){
		if (values==null)
			return;
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
				values[ic][it] =  0;
	}
	/*...........................................................*/
	public void deassignArray(){
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
				values[ic][it] =  MesquiteDouble.unassigned;
	}
	/*...........................................................*/
	public static void deassignArray(double[][] values){
		if (values==null)
			return;
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
				values[ic][it] =  MesquiteDouble.unassigned;
	}
	/*...........................................................*/
	public static void setToIdentityMatrix(double[][] values){
		if (values==null)
			return;
		for (int ic=0; ic<values.length; ic++)
			for (int it=0; it<values[ic].length; it++)
				if (it==ic)
					values[ic][it] =  1;
				else
					values[ic][it] =  0;
	}
	/*...........................................................*/
	/** Changes the array size to the new dimensions*/
	public void resetSize(int newNumC, int newNumT) {
		if (newNumC == numC && newNumT == numT)
			return;
		double[][] newDoubleValues = new double[newNumC][newNumT];
		for (int i=0; i<newNumC; i++)
			for (int j=0; j<newNumT; j++) {
				if (i<numC && j<numT)
					newDoubleValues[i][j]=values[i][j];
				else
					newDoubleValues[i][j]=0;
			}
		values=newDoubleValues;

		numC=newNumC;
		numT=newNumT;
	}
	/*...........................................................*/
	public static double maximum(double[][] values, boolean ignoreUncombinable){
		if (values==null)
			return MesquiteDouble.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteDouble.unassigned);
		for (int ic = 0; ic<values.length; ic++)
			for (int i=0; i<values[ic].length; i++) {
				if (!ignoreUncombinable || MesquiteDouble.isCombinable(values[ic][i]))
					d.setMeIfIAmLessThan(values[ic][i]);
			}
		return d.getDoubleValue();
	}
	/*...........................................................*/
	public static double minimum(double[][] values, boolean ignoreUncombinable){
		if (values==null)
			return MesquiteDouble.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteDouble.unassigned);
		for (int ic = 0; ic<values.length; ic++)
			for (int i=0; i<values[ic].length; i++){
				if (!ignoreUncombinable || MesquiteDouble.isCombinable(values[ic][i]))
					d.setMeIfIAmMoreThan(values[ic][i]);
			}
		return d.getDoubleValue();
	}
	/*...........................................................*/
	public static double maximumInColumn(double[][] values, int column){
		if (values==null || values.length<=column || values[column]==null)
			return MesquiteDouble.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteDouble.unassigned);
		for (int i=0; i<values[column].length; i++)
			d.setMeIfIAmLessThan(values[column][i]);
		return d.getDoubleValue();
	}
	/*...........................................................*/
	public static double minimumInColumn(double[][] values, int column){
		if (values==null || values.length<=column || values[column]==null)
			return MesquiteDouble.unassigned;
		MesquiteNumber d = new MesquiteNumber(MesquiteDouble.unassigned);
		for (int i=0; i<values[column].length; i++)
			d.setMeIfIAmMoreThan(values[column][i]);
		return d.getDoubleValue();
	}
	/*...........................................................*/
	/** borrowed from rhetenor.MatrixUtil*/
	public static double[][] multiply(double[][] matrix1, double[][] matrix2, double[][] result){
		int numRows1 = numFullRows(matrix1);
		int numRows2 = numFullRows(matrix2);
		int numColumns1 = numFullColumns(matrix1);
		int numColumns2 = numFullColumns(matrix2);
		boolean flag = (numColumns1!= numRows2 || numRows1*numRows2*numColumns1*numColumns2 ==0);
		if (flag) MesquiteMessage.println("Wow! You are trying to multiply a:   " 
				+Integer.toString(numRows1) +"x" +Integer.toString(numColumns1)  
				+" by a  " 
				+Integer.toString(numRows2) +"x" +Integer.toString(numColumns2)  ); 
		if (flag)
			return null; 
		if (result == null || result.length != numColumns2 || (result.length>0 && result[0].length != numRows1))
			result = new double[numColumns2][numRows1];
		zeroArray(result);
		for (int i=0; i<numColumns2; i++) {
			for (int j=0; j<numRows1; j++) {
				double sum =0;
				for (int k=0; k<numColumns1; k++) {
					sum += matrix1[k][j]* matrix2[i][k];
				}
				result[i][j]=sum;
			}
		}
		return result;
	}
	/*...........................................................*/
	/** borrowed from rhetenor.MatrixUtil*/
	public static double[][] transpose(double[][] matrix){
		return transpose(matrix, null);
	}
	/*...........................................................*/
	/** borrowed from rhetenor.MatrixUtil*/
	public static double[][] transpose(double[][] matrix, double[][] result){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		if (result == null || result.length != numRows || (result.length>0 && result[0].length != numColumns))
			result = new double[numRows][numColumns];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[j][i]=matrix[i][j];
			}
		}
		return result;
	}
	/*...........................................................*/
	/** flip matrix both ways*/
	public static double[][] squnch(double[][] matrix, double[][] result){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		if (result == null || result.length != numRows || (result.length>0 && result[0].length != numColumns))
			result = new double[numRows][numColumns];
		zeroArray(result);
		//flip columns
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[i][numRows - j - 1]=matrix[i][j];
			}
		}
		//flip rows
		for (int i=0; i<numColumns/2; i++) {
			for (int j=0; j<numRows; j++) {
				double t = result[numColumns-i-1][j];
				result[numColumns-i-1][j]=result[i][j];
				result[i][j] = t;
			}
		}
		return result;
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
	public void deleteColumnsFlagged(Bits toDelete){
		values = deleteColumnsFlagged(values, toDelete);
		numC = values.length;
	}
	/*...........................................................*/
	public static double[][] deleteColumnsFlagged(double[][] d, Bits toDelete) {
		if (d == null)
			return null;
		if (d.length <= 0)
			return d;
		int numRows= d[0].length;
		if (numRows == 0)
			return d;
		if (toDelete == null)
			return d;

		int toFill =toDelete.nextBit(0, true); //find next to be cleared
		if (toFill <0)
			return d;
		Bits flags = toDelete.cloneBits(); 
		int source = flags.nextBit(toFill, false); //find source to move into it
		int highestFilled = toFill-1; //
		while (source >=0 && source < d.length && toFill >=0) { //First, compact storage toward the start of the array.
			for (int it=0; it<numRows; it++)
				d[toFill][it] = d[source][it]; //move content from source to place
			highestFilled = toFill;
			flags.setBit(source, true); // set to available to receive
			toFill =flags.nextBit(++toFill, true);
			source =flags.nextBit(++source, false);	
		}
		//Next, trim leftovers
		int newNumColumns = highestFilled+1;
		double[][] newMatrix=new double[newNumColumns][numRows];
		for (int ic=0; ic<newNumColumns; ic++) 
			for (int it=0; it<numRows && it< d[ic].length; it++) 
				newMatrix[ic][it] = d[ic][it];
		return newMatrix;
	}	
	/*...........................................................*
	public void deleteColumnsBy Blocks(int[][] blocks){
		values = deleteColumnsBy Blocks(values, blocks);
		numC = values.length;
	}
	/*...........................................................*
	public static double[][] deleteColumnsBy Blocks(double[][] d, int[][] blocks){
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
		double[][] newMatrix=new double[newNumColumns][numRows];
		for (int ic=0; ic<newNumColumns; ic++) 
			for (int it=0; it<numRows && it< d[ic].length; it++) 
			newMatrix[ic][it] = d[ic][it];
		return newMatrix;
}


	/*...........................................................*/
	public static void swapColumns(double[][] d, int first, int second) {
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		double[] temp = d[first];
		d[first]=d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void moveColumns(double[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		double[][] newValues = new double[d.length][];
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
	public static String toString(double[][] matrix){
		if (matrix == null)
			return "Matrix null";
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		StringBuffer result = new StringBuffer(2*numRows*numColumns);
		for (int j=0; j<numRows; j++) {
			result.append('[');
			for (int i=0; i<numColumns; i++) {
				if (!MesquiteDouble.isCombinable(matrix[i][j]))
					MesquiteDouble.toString(matrix[i][j], result);
				else
					result.append(Double.toString(matrix[i][j]));
				result.append(' ');
			}
			result.append(']');
			result.append('\n');
		}
		return result.toString();
	}
	public static String toStringRC(double[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		StringBuffer result = new StringBuffer(2*numRows*numColumns);
		for (int i=0; i<matrix.length; i++) {
			result.append('[');
			for (int j=0; j<matrix[i].length; j++) {
				if (!MesquiteDouble.isCombinable(matrix[i][j]))
					MesquiteDouble.toString(matrix[i][j], result);
				else
					result.append(Double.toString(matrix[i][j]));
				result.append(' ');
			}
			result.append(']');
			result.append('\n');
		}
		return result.toString();
	}
}



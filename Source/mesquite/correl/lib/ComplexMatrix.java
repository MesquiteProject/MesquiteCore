/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.correl.lib;

import mesquite.lib.MesquiteDouble;
import Jama.Matrix;

// A very minimal implementation of matrices of complex numbers.  Really this
// only implments multiplication and copying to and from real number matrices
public class ComplexMatrix {
	
	// lay this out almost like the Jama Matrix class
	private double[][][] C;
	private int m;
	private int n;

	// simple constructor - just specify size
	public ComplexMatrix(int m,int n){
		this.m = m;
		this.n = n;
		C = new double[m][n][2];
	}
	
	public double getRealValue(int m, int n) {
		return C[m][n][0];
	}
	public double getImaginaryValue(int m, int n) {
		return C[m][n][1];
	}
	public void setRealValue(double value, int m, int n) {
		C[m][n][0] = value;
	}
	public void setImaginaryValue(double value, int m, int n) {
		C[m][n][1] = value;
	}
	
	public double[][] extractRealMatrix() {
		double[][] result = new double[C.length][C[0].length];
		for(int i=0;i<result.length;i++)
			for (int j=0;j<result.length;j++)
				result[i][j]=C[i][j][0];
		return result;
	}
	
	// construct from 2 dimensional array of real doubles; all imaginary parts set to 0
	   public ComplexMatrix(double[][] A) {
		      m = A.length;
		      n = A[0].length;
		      // keep the checking
		      for (int i = 0; i < m; i++) {
		         if (A[i].length != n) {
		            throw new IllegalArgumentException("All rows must have the same length.");
		         }
		      }
		      C = new double[2][m][n];
		      // fill
		      for (int i=0;i<m;i++)
		    	  	for (int j=0;j<n;j++){
		    	  		C[0][i][j]=A[i][j];
		    	  		C[1][i][j]=0;
		    	  	}
	   }
	   
	   // returns diagonal matrix from arrays of real and imaginary parts
	   public static ComplexMatrix makeDiagonal(double[] realParts, double[] imaginaryParts){
		   if (realParts.length != imaginaryParts.length)
			   throw new IllegalArgumentException("Real and imaginary parts must have same length");
		   ComplexMatrix result = new ComplexMatrix(realParts.length,realParts.length);
		   
		   for (int i=0;i<realParts.length;i++)
			   for (int j=0;j<realParts.length;j++){
				   if (i==j) {
					   result.C[i][j][0]= realParts[i];
					   result.C[i][j][1] = imaginaryParts[i];
				   }
				   else {
					   result.C[i][j][0] = 0;
					   result.C[i][j][1] = 0;
				   }
			   }
	   		return result;
	   }
	   
	   /** Get row dimension.
	   @return     m, the number of rows.
	   */

	   public int getRowDimension () {
	      return m;
	   }

	   /** Get column dimension.
	   @return     n, the number of columns.
	   */

	   public int getColumnDimension () {
	      return n;
	   }


	   public double[][] realPortion(){
		   double[][] result = new double[m][n];
		   for (int i=0;i<m;i++)
			   for (int j=0;j<n;j++)
				   result[i][j] = C[i][j][0];
		   return result;
	   }
	   

		// multiply by a real number constant and return new matrix
	   public ComplexMatrix times(double x){
		   ComplexMatrix result = new ComplexMatrix(this.m,this.n);
		   for (int i=0;i<m;i++)
			   for (int j=0;j<m;j++){
				   result.C[i][j][0] = this.C[i][j][0]*x;
				   result.C[i][j][1] = this.C[i][j][1]*x;
			   }
		   return result;
	   }
	   //multiply in place by a real number constant
	   public void timesEquals (double x){
		   for (int i=0;i<m;i++)
			   for (int j=0;j<m;j++){
				   this.C[i][j][0] *= x;
				   this.C[i][j][1] *= x;
			   }
	   }
	   
	   // Since multiplication is non-communitive this will support right and left
	   // multiplication with real-valued matrices (avoids converting real->complex
	   // matrix conversion).
	   // returns C=A*B (B real)
	   public ComplexMatrix rightMultiply (double[][] B){
		   if (B.length != n) {
			   throw new IllegalArgumentException("Matrix inner dimensions must agree.");
		   }
		   ComplexMatrix X = new ComplexMatrix(m,B.length);
		   double[][][] XArray = X.C;
		   double[] Bcolj = new double[n];
		   double[][] Barray = B;
		      for (int j = 0; j < B.length; j++) {
		         for (int k = 0; k < n; k++) {
		            Bcolj[k] = Barray[k][j];
		         }
		         for (int i = 0; i < m; i++) {
		            double[][] Crowi = C[i];
		            double sr = 0;
		            double sc = 0;
		            for (int k = 0; k < n; k++) {
		               sr += Crowi[k][0]*Bcolj[k];
		               sc += Crowi[k][1]*Bcolj[k];
		            }
		            XArray[i][j][0] = sr;
		            XArray[i][j][1] = sc;
		         }
		      }
		   return X;
	   }
	   
	   /**
	    * 
	    * @param B Matrix of real values
	    * @return C = B*A 
	    */
	   public ComplexMatrix leftMultiply(double [][] B){
		   if (B[0].length != m) {
			   throw new IllegalArgumentException("Matrix inner dimensions must agree.");
		   }
		   ComplexMatrix X = new ComplexMatrix(B[0].length,n);
		   double[][][] XArray = X.C;
		   double[][] Acolj = new double[m][2];
		   for (int j = 0;j< this.getRowDimension();j++) {
			   for (int k = 0; k< m; k++) {
				   Acolj[k][0] = C[k][j][0];
				   Acolj[k][1] = C[k][j][1];
			   }
			   for (int i = 0; i < n; i++){
				   double[] Browi = B[i];
				   double sr = 0;
				   double sc = 0;
				   for (int k = 0; k < m; k++){
					   sr += Browi[k]*Acolj[k][0];
					   sc += Browi[k]*Acolj[k][1];
				   }
				   XArray[i][j][0] = sr;
				   XArray[i][j][1] = sc;
			   }
		   }
		   return X;
		   
	   }
	   
	   
	   // based on code from Mesquite2DArray
	   
		public String toString(){
			int numRows = m;
			int numColumns = n;
			if (numRows==0 ||  numColumns==0)
				return null;  
			StringBuffer result = new StringBuffer(4*numRows*numColumns);
			for (int j=0; j<numRows; j++) {
				result.append('[');
				for (int i=0; i<numColumns; i++) {
					if (!MesquiteDouble.isCombinable(C[i][j][0]))
						MesquiteDouble.toString(C[i][j][0], result);
					else
						result.append(Double.toString(C[i][j][0]));
					if (!MesquiteDouble.isCombinable(C[i][j][1])) {
						if (C[i][j][1] >= 0)
							result.append('+');
						else result.append('-');
						MesquiteDouble.toString(Math.abs(C[i][j][1]), result);
					}
					else
						result.append("+" + Double.toString(C[i][j][1]));
					result.append("i ");
				}
				result.append(']');
				result.append('\n');
			}
			return result.toString();
		}
		
		public String toStringRC(){
			int numRows = m;
			int numColumns = n;
			StringBuffer result = new StringBuffer(4*numRows*numColumns);
			for (int i=0; i<numColumns; i++) {
				result.append('[');
				for (int j=0; j<numRows; j++) {
					if (!MesquiteDouble.isCombinable(C[i][j][0]))
						MesquiteDouble.toString(C[i][j][0], result);
					else
						result.append(Double.toString(C[i][j][0]));
					result.append('+');
					if (!MesquiteDouble.isCombinable(C[i][j][1])){
						if (C[i][j][1] >= 0)
							result.append('+');
						else result.append('-');
						MesquiteDouble.toString(C[i][j][1], result);
					}
					else
						result.append(Double.toString(C[i][j][1]));
					result.append(' ');
				}
				result.append(']');
				result.append('\n');
			}
			return result.toString();
		}
	   
	   // 
	 //  public ComplexMatrix multiply(ComplexMatrix B){
	//	   return X;
	//   }

}

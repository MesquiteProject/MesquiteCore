/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.lib; 

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
//import Jampack.*;  
import Jama.*;  



	public  class EigenAnalysis {
		public double[][] eigenVectors;
		public double[] eigenValues;
		public double[] imagEigenValues;   //Added 4 April 2006 P. Midford
		/*...........................................................*/
		/** Jama version */
		public EigenAnalysis(double[][] matrix, boolean transpose, boolean positivize, boolean sort){
			int numRows = Double2DArray.numFullRows(matrix);
			int numColumns = Double2DArray.numFullColumns(matrix);
			if (numRows!=0 &&  numColumns!=0){
				int n = numRows;
				if (numRows!=numColumns)
					MesquiteMessage.warnProgrammer("not same number columns & rows in eigenanalysis");
				EigenvalueDecomposition eig = new EigenvalueDecomposition(new Matrix(matrix));      	//using Eig from Jampack 
				eigenVectors = eig.getV().getArrayCopy();  	//eigenVectors
		     	eigenValues = eig.getRealEigenvalues();
		     	imagEigenValues = eig.getImagEigenvalues();
				if (transpose)
					eigenVectors = Double2DArray.transpose(eigenVectors);
				if (positivize)
					positivize(eigenValues, eigenVectors);
				if (sort)
					sortByEigenvalues(eigenValues, imagEigenValues, eigenVectors);
			 }
		}
		/*...........................................................*/
		/** Jampack version *
		public EigenAnalysis(double[][] matrix){
			int numRows = Double2DArray.numFullRows(matrix);
			int numColumns = Double2DArray.numFullColumns(matrix);
			if (numRows!=0 &&  numColumns!=0){
				int n = numRows;
				if (numRows!=numColumns)
					MesquiteMessage.warnProgrammer("not same number columns & rows in eigenanalysis");
				try{
					Eig eig = new Eig(new Zmat(matrix));      	//using Eig from Jampack 
					eigenVectors = eig.X.getRe();  	//eigenVectors
			     		eigenValues = new double[eig.D.n]; 
			     		for (int i=0; i<eig.D.n; i++)  //retrieving eigenValues from eig 
						eigenValues[i]=eig.D.get0(i).re;  
					eigenVectors = Double2DArray.transpose(eigenVectors);
				}
				catch(JampackException e){
					MesquiteMessage.println("Jampack exception in EigenAnalysis; matrix:\n" + Double2DArray.toString(matrix));
					e.printStackTrace();
				 }
			 }
		}
		/*...........................................................*/
		// don't need to positivize any imaginary components, since the complex eigenvalues
		// always come in conjugate pairs
		private void positivize(double[] eigenValues, double[][] eigenVectors){
			if (eigenValues == null)
				return;
			for (int i=0; i<eigenValues.length; i++)
				if (eigenValues[i]<0) {
					eigenValues[i] = -eigenValues[i];
					for (int j=0; j<eigenVectors[i].length; j++)
						eigenVectors[i][j]= -eigenVectors[i][j];
				}
		}
		/*...........................................................*/
		private void sortByEigenvalues(double[] eigenValues, double[] imagEigenValues, double[][] eigenVectors){
			if (eigenValues == null)
				return;
			double[] tempVector;
			for (int i=0; i<eigenValues.length; i++) {
				for (int j= i-1; j>=0 && eigenValues[j]<eigenValues[j+1]; j--) {
					double temp = eigenValues[j];
					eigenValues[j] = eigenValues[j+1];
					eigenValues[j+1]=temp;
					double itemp = imagEigenValues[j];
					imagEigenValues[j] = imagEigenValues[j+1];
					imagEigenValues[j+1] = itemp;
					tempVector = eigenVectors[j];
					eigenVectors[j]= eigenVectors[j+1];
					eigenVectors[j+1] = tempVector;
				}
			}
		}
		/*----------getEigenvalues ----------*/
		public double[] getEigenvalues(){
			return eigenValues;
	  	}
		/* ----------getEigenDiagonal  ----------*/
		public double[] getImagEigenValues(){
			return imagEigenValues;
		}
		
		/* ----------getEigenvectors  ----------*/
		public double[][] getEigenvectors(){
			return eigenVectors;
		}
}


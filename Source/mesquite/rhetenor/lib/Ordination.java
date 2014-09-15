/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.lib; 

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;
/* ======================================================================== 
This contains the Ordination class, whose subclasses (PCAOrdination, CVAOrdination) 
perform analyses such as Principal Components and Canonical Variates analyses.
/* ======================================================================== */

	public abstract class Ordination implements StringLister{
		protected double[][] eigenVectors;
		protected double[] eigenValues;
		protected double[][] scores;
		protected double[] percentExplained;
		
		protected void doEigenAnalysis(double[][] matrix, double[][] original){
			EigenAnalysis eA = new EigenAnalysis(matrix, true, true, true);
	     		eigenValues = eA.getEigenvalues();  
			eigenVectors = eA.getEigenvectors();
			if (eigenVectors!=null)
				scores = MatrixUtil.multiply(original,eigenVectors);	
			if (eigenValues!=null)
				percentExplained = MatrixUtil.percentage(eigenValues);
		}
	 	public double[][] transformAnother(double[][] matrix){			
				return MatrixUtil.multiply(matrix, eigenVectors); 
	 	}
		/*----------setting results to be same as passed Ordination ----------*/
		public void setResults(Ordination ordRes){
			eigenValues = ordRes.getEigenvalues();
			eigenVectors = ordRes.getEigenvectors();
			scores = ordRes.getScores();
			percentExplained = ordRes.getPercentExplained();
	  	}
		/*----------getEigenvalues ----------*/
		public double[] getEigenvalues(){
			return eigenValues;
	  	}
		/* ----------getEigenvectors  ----------*/
		public double[][] getEigenvectors(){
			return eigenVectors;
		}
		public double[][] getScores(){
			return scores;
		}
		public double[] getPercentExplained(){
			return percentExplained;
		}
		public String report(){
			if (scores==null)
				return "";
			String s = "";
			s+=("eigenValues\n" + MatrixUtil.toString(eigenValues) + "\n\n");
			s+=("Percent variance Explained\n" + MatrixUtil.toString(percentExplained) + "\n\n");
			s+=("PCA scores\n" + MatrixUtil.toString(scores) + "\n\n");
			return s;
		}
	 	public int getNumberOfAxes(){
	 		if (eigenValues==null)
	 			return 0;
	 		else
	 			return eigenValues.length;
	 	}
	 	public abstract String getAxisName(int i);
	 	
	 	public String[] getStrings(){
	 		String[] s = new String[getNumberOfAxes()];
	 		for (int i=0; i<getNumberOfAxes(); i++)
	 			s[i]= getAxisName(i);
	 		return s;
	 	}
 }


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
import Jama.*;  

/* ======================================================================== 
This library contains a utility class (MatrixUtil) with static methods for manipulating matrices.  
Some of these methods are very basic (e.g., matrix multiplication) while others are a bit fancier,
designed to help with the ordination procedures.
*/

// *********************** MATRIX UTILITIES ********************************* //
public class MatrixUtil {

// -----------------MatrixUtil  Version 2.01  By Eric Dyreson and Wayne Maddison
// --- stripExcluded added



// ..........COMMENTARY, ERROR CHECKING, and OUTPUT STYLE ...............................................................................//
public static boolean writeMistakes = false;
public static boolean writeCommentary = false;
public static boolean writeIntermediates = false;
//...................
public static void mistakesWereMade(String c){
        if (writeMistakes)
                System.out.println(" .......OOPS!............ " +c);
}
//...................
public static void runningCommentary(String c){
        if (writeCommentary)
                System.out.println("....."  +c +"......");
}
//...................
public static void intermediates(String c){
        if (writeIntermediates)
                System.out.println(c);
}
//...................
public static String dimensions(double[][] matrix){
		int numRows   	  = MatrixUtil.numFullRows(matrix);
		int numColumns  = MatrixUtil.numFullColumns(matrix);
            return "...."  +Integer.toString(numRows) +"x"  + Integer.toString(numColumns) +"...." ;
}
//...................
public static String dimensions(double[][] matrix1,double[][] matrix2){
            return dimensions(matrix1) +"-->"  +dimensions(matrix2);
}
//...................
public static String dimensions(double[][] matrix1,double[][] matrix2,double[][] matrix3){
            return dimensions(matrix1) +","  +dimensions(matrix2) +"-->"  +dimensions(matrix3) ;
}
//...................
 public static boolean checkDimensions(double[][] matrix1, double[][] matrix2){
		int numRows1    = MatrixUtil.numFullRows(matrix1);
		int numRows2    = MatrixUtil.numFullRows(matrix2);
		int numColumns1 = MatrixUtil.numFullColumns(matrix1);
		int numColumns2 = MatrixUtil.numFullColumns(matrix2);
		boolean flag = (numRows1!= numRows2 || numRows1==0 || numColumns1!=numColumns2 || numColumns1==0) ;
		if (flag) mistakesWereMade("Once again, your dimensions don't match up:   " 
		        +Integer.toString(numRows1) +"x" +Integer.toString(numColumns1)  
		             +" by  " 
		        +Integer.toString(numRows2) +"x" +Integer.toString(numColumns2)  ); 
		return flag;
		}
/*...........................................................*/
	public static String toString(double[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		StringBuffer result = new StringBuffer(2*numRows*numColumns);
		for (int j=0; j<numRows; j++) {
			result.append('[');
			for (int i=0; i<numColumns; i++) {
				
				MesquiteDouble.toString(matrix[i][j], result);
				result.append(' ');
			}
			result.append(']');
			result.append('\n');
		}
		return result.toString();
	}
	/*...........................................................*/
	public static String toString(double[] vector){
		if (vector==null ||  vector.length==0)
			return null;  
		StringBuffer result = new StringBuffer(vector.length*2);
		result.append('[');
		for (int i=0; i<vector.length; i++) {
			MesquiteDouble.toString(vector[i], result);
			result.append(' ');
		}
		result.append(']');
		return result.toString();
	}
	/*...........................................................*/
	public static String toString(int[] vector){
		if (vector==null ||  vector.length==0)
			return null;  
		StringBuffer result = new StringBuffer(vector.length*2);
		result.append('[');
		for (int i=0; i<vector.length; i++) {
			result.append(Integer.toString(vector[i]));
			result.append(' ');
		}
		result.append(']');
		return result.toString();
	}
//..................................................................................................................................................................................//

	public static MContinuousDistribution stripExcluded(MContinuousDistribution original, MesquiteBoolean wasStripped){
		CharacterData data = original.getParentData();
		if (data ==null)
			return original;
		
		CharInclusionSet incl = (CharInclusionSet) data.getCurrentSpecsSet(CharInclusionSet.class);
		if (incl == null || incl.numberSelected() == data.getNumChars())
			return original;
		MContinuousAdjustable stripped = new MContinuousAdjustable(data.getTaxa());
		stripped.setItemsAs(original);
		int strippedNumChars = incl.numberSelected();
		int numTaxa = data.getNumTaxa();
		stripped.setSize(strippedNumChars, numTaxa);
		int count = 0;
		ContinuousState cs = null;
		for (int ic = 0; ic<data.getNumChars(); ic++){
			if (incl.isBitOn(ic)){
				for (int it = 0; it<numTaxa; it++){
					cs = (ContinuousState)data.getCharacterState(cs, ic, it);
					stripped.setState(count, it, cs);
				}
				count++;
			}
		}
		if (wasStripped != null)
			wasStripped.setValue(true);
		return stripped;
	}


//.............ROW AND COLUMN MANIPTULATIONS............................................................................................................//
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
	public static double[][] getRow(double[][] matrix,int which){
		if (MatrixUtil.numFullColumns(matrix)==0)
			return null;
		double[][] selected = new double[matrix.length][1];
		for (int i=0; i<matrix.length; i++) {
			 selected[i][0]=matrix[i][which];
		}
		if (writeCommentary) runningCommentary("getRow " +MesquiteInteger.toString(which) +dimensions(matrix,selected));  
		if (writeIntermediates) intermediates(MatrixUtil.toString(selected)); 
		return selected;
	}
	/*...........................................................*/
      public static double[][] getColumn(double[][] matrix,int which){
		int rows=MatrixUtil.numFullRows(matrix);
	      if (rows==0)  mistakesWereMade("Hey, I can't get blood from a turnip! This matrix has no columns to select!!");
	      if (rows==0)
  			return null;
	      double[][] selected = new double[1][rows];
		for (int i=0; i<rows; i++) {
				selected[0][i]=matrix[which][i];
		}
		if (writeCommentary) runningCommentary("getColumn " +MesquiteInteger.toString(which) +dimensions(matrix,selected));   
		if (writeIntermediates) intermediates(MatrixUtil.toString(selected));  
		return selected;
	}
	/*...........................................................*/
	public static double[][] columnAverages(double[][] matrix){
		if (numFullColumns(matrix)==0)
			return null;
		double[][] avg = new double[matrix.length][1];
		for (int i=0; i<matrix.length; i++) {
			double sum = 0;
			for (int j=0; j<matrix[i].length; j++)
				sum+=matrix[i][j];
			avg[i][0]= sum/matrix[i].length;
		}
		if (writeCommentary) runningCommentary("columnAverages: " +dimensions(matrix,avg) ); 
		if (writeIntermediates) intermediates(MatrixUtil.toString(avg));  
		return avg;
	}
	/*...........................................................*/
	public static double[][] rowAverages(double[][] matrix){ 
		if (numFullRows(matrix)==0)
			return null;
		double[][] avg = new double[1][matrix[0].length]; //don't count unassigned values?
		for (int j=0; j<matrix[0].length; j++) {
			double sum = 0;
			for (int i=0; i<matrix.length; i++) {
				if (j<matrix[i].length)
					sum=sum+matrix[i][j];
			}
			avg[0][j]= sum/matrix.length;
		}
		if (writeCommentary) runningCommentary("rowAverages" +dimensions(matrix,avg)); 
		if (writeIntermediates) intermediates(MatrixUtil.toString(avg));  
		return avg;
	}
	/*...........................................................*/
	public static double[] reverse(double[] matrix){
		if (matrix == null)
			return null;
		double[] result = new double[matrix.length];
		for (int i=0; i<result.length; i++) {
			result[result.length-i-1]=matrix[i];
		}
		return result;
	}
	/*...........................................................*/
	public static double[][] horizontalFlip(double[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		double[][] result = new double[numRows][numColumns];
		for (int i=0; i<numColumns; i++) {
			result[numColumns-i-1]=matrix[i];
		}
		if (writeCommentary) runningCommentary("horizontalFlip"   +dimensions(matrix,result) );
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}
	/*...........................................................*/
	public static double[][] transpose(double[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		double[][] result = new double[numRows][numColumns];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[j][i]=matrix[i][j];
			}
		}
		if (writeCommentary) runningCommentary("transpose"   +dimensions(matrix,result) );
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}
// *-------------------------------------------------------------Haugment--------;
	public static double[][] Haugment(double[][] matrix1, double[][] matrix2){
		int numRows1 = numFullRows(matrix1);
		int numColumns1 = numFullColumns(matrix1);
	      int numRows2 = numFullRows(matrix2);
		int numColumns2 = numFullColumns(matrix2);
	      boolean flag =  (numRows1!=numRows2)  ;
		if (flag) System.out.println("Oh No! You are trying to horizontally augment a:   " 
		        +Integer.toString(numRows1) +"x" +Integer.toString(numColumns1)  
		             +" to a  " 
		        +Integer.toString(numRows2) +"x" +Integer.toString(numColumns2)  ); 
		if (flag)
		    return null; 		
    		double[][] result = new double[numColumns1+numColumns2][numRows1];
		for (int i=0; i<numColumns1; i++) {
			for (int j=0; j<numRows1; j++) {
				result[i][j]=matrix1[i][j];
			}
		}
		for (int i=0; i<numColumns2; i++) {
			for (int j=0; j<numRows2; j++) {
				result[i+numColumns1][j]=matrix2[i][j];
			}
		}
		if (writeCommentary) runningCommentary("Haugment" +dimensions(matrix1,matrix2,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
  	}
// *-------------------------------------------------------------Vaugment--------;
	public static double[][] Vaugment(double[][] matrix1, double[][] matrix2){
		int numRows1 = numFullRows(matrix1);
		int numColumns1 = numFullColumns(matrix1);
		if (numRows1==0 ||  numColumns1==0)
			return null; 
 		int numRows2 = numFullRows(matrix2);
		int numColumns2 = numFullColumns(matrix2);
		if (numRows2==0 ||  numColumns2==0)
			return null; 
		boolean flag =  (numColumns1!=numColumns2) ;
		if (flag) System.out.println("Oh No! You are trying to vertically augment a:   " 
		        +Integer.toString(numRows1) +"x" +Integer.toString(numColumns1)  
		             +" to a  " 
		        +Integer.toString(numRows2) +"x" +Integer.toString(numColumns2)  ); 
		if (flag)
		    return null; 						
	  		double[][] result = new double[numColumns1][numRows1+numRows2];
		for (int i=0; i<numColumns1; i++) {
			for (int j=0; j<numRows1; j++) {
				result[i][j]=matrix1[i][j];
			}
		}
		for (int i=0; i<numColumns2; i++) {
			for (int j=0; j<numRows2; j++) {
				result[i][j+numRows1]=matrix2[i][j];
			}
		}
		if (writeCommentary) runningCommentary("Vaugment"  +dimensions(matrix1,matrix2,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;																										
  	}
//..................................................................................................................................................................................//
  	


//..........................................CREATING NEW MATRICIES.................................................................................................//
	/*...........................................................*/
	public static double[][] identityMatrix(int rc){
		double[][] result = new double[rc][rc];
		for (int i=0; i<rc; i++)
			for (int j=0; j<rc; j++){
				result[i][j]=0;
				if (i==j) result[i][j]=1.0;
		}
	if (writeCommentary) runningCommentary("identityMatrix "  +dimensions(result));
	if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
	return result;
	}
	/*...........................................................*/
	public static double[][] fillMatrix(int row,int col,double number){
		double[][] result = new double[row][col];
		for (int i=0; i<row; i++)
			for (int j=0; j<col; j++){
				result[i][j]=number;
		}
		if (writeCommentary) runningCommentary("fillMatrix" +dimensions(result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}	
//.........................................................................................................................................................................................//
	



// ...............BASIC MATRIX ALGEBRA ...................................................................................................................................//
	/*...........................................................*/
	public static double[][] add(double[][] matrix1, double[][] matrix2){
	if (MatrixUtil.checkDimensions(matrix1,matrix2))
			return null;  
	      int numRows1 = numFullRows(matrix1);
		int numRows2 = numFullRows(matrix2);
		int numColumns1 = numFullColumns(matrix1);
		int numColumns2 = numFullColumns(matrix2);
		if (numRows1!= numRows2 || numRows1==0 || numColumns1!=numColumns2 || numColumns1==0)
			return null;  
		
		double[][] sum = new double[numColumns1][numRows1];
		for (int i=0; i<numColumns1; i++) {
			for (int j=0; j<numRows1; j++) {
				sum[i][j]=matrix1[i][j] + matrix2[i][j];
			}
		}
		if (writeCommentary) runningCommentary("add " +dimensions(matrix1,matrix2,sum));
	      if (writeIntermediates) intermediates(MatrixUtil.toString(sum)); 
	      return sum;
	}	
	 /*...........................................................*/
	public static double[][] subtract(double[][] matrix1, double[][] matrix2){
		if (MatrixUtil.checkDimensions(matrix1,matrix2))
			return null;  
   		int numRows1 = numFullRows(matrix1);
		int numColumns1 = numFullColumns(matrix1);
		double[][] sub = new double[numColumns1][numRows1];
		for (int i=0; i<numColumns1; i++) {
			for (int j=0; j<numRows1; j++) {
				sub[i][j]=matrix1[i][j] - matrix2[i][j];
			}
		}
		if (writeCommentary) runningCommentary("subtract " +dimensions(matrix1,matrix2,sub));
	     if (writeIntermediates) intermediates(MatrixUtil.toString(sub)); 
	      return sub;
		}
	 // ------------------------multiplyElementwise--------------------;
    	public static double[][] multiplyElementwise(double[][] matrix1, double[][] matrix2){
		if (MatrixUtil.checkDimensions(matrix1,matrix2))
			return null;  
		int numRows1 = MatrixUtil.numFullRows(matrix1);
		int numColumns1 = MatrixUtil.numFullColumns(matrix1);
		int numRows2 = MatrixUtil.numFullRows(matrix2);
		int numColumns2 = MatrixUtil.numFullColumns(matrix2);
		if (numRows1==0 ||  numColumns1==0)
			return null; 
		if (numRows1 != numRows2 ||  numColumns1 != numColumns2)
			return null; 
		double[][] result = new double[numColumns1][numRows1];
		for (int i=0; i<numColumns1; i++) {
			for (int j=0; j<numRows1; j++) {
				result[i][j]=(matrix1[i][j])*(matrix2[i][j]);
			}
		}
		if (writeCommentary) runningCommentary("multiplyElementwise" +dimensions(matrix1,matrix2,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
 		return result;
	}
      /*...........................................................*/
	public static double[][] multiply(double[][] matrix1, double[][] matrix2){
		int numRows1 = MatrixUtil.numFullRows(matrix1);
		int numRows2 = MatrixUtil.numFullRows(matrix2);
		int numColumns1 = MatrixUtil.numFullColumns(matrix1);
		int numColumns2 = MatrixUtil.numFullColumns(matrix2);
		boolean flag = (numColumns1!= numRows2 || numRows1*numRows2*numColumns1*numColumns2 ==0);
		if (flag) System.out.println("Wow! You are trying to multiply a:   " 
		        +Integer.toString(numRows1) +"x" +Integer.toString(numColumns1)  
		             +" by a  " 
		        +Integer.toString(numRows2) +"x" +Integer.toString(numColumns2)  ); 
		if (flag)
		    return null; 
		double[][] result = new double[numColumns2][numRows1];
		for (int i=0; i<numColumns2; i++) {
			for (int j=0; j<numRows1; j++) {
				double sum =0;
				for (int k=0; k<numColumns1; k++) {
					sum += matrix1[k][j]* matrix2[i][k];
				}
				result[i][j]=sum;
			}
		}
		if (writeCommentary) runningCommentary("multiply" +dimensions(matrix1,matrix2,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}
     /*...........................................................*/
	public static double[][] multiply(double scalar, double[][] matrix){
		int numRows = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
            if (numRows*numColumns==0) 
               return null;
		double[][] result = new double[numColumns][numRows];
		for (int i=0; i<numRows; i++) {
			for (int j=0; j<numColumns; j++) {
					result[j][i] = scalar* matrix[j][i] ;	
			}
		}
		if (writeCommentary) runningCommentary("multiply by scalar" +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}
//............................................................................................................................................................................................//




//........................OPERATIONS ON MATRICES.............................................................................................................................//
   // ------------------------abs--------------------;
	public static double[][] abs(double[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		double[][] result = new double[numColumns][numRows];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[i][j]=Math.abs(matrix[i][j]);
			}
		}
		if (writeCommentary) runningCommentary("abs " +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	  }
	 // -----------------------minimum--------------------;
	public static double minimum(double[][] matrix){
		int numRows = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
		double result = matrix[0][0];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
//eric: Mesquite has the concept of numbers having the value "unassigned", and this can screw up min/max calculations
// either we should check for unassigned values first, or we should use Mesquite's minimum method
				result = Math.min(matrix[i][j],result);
			}
		}		
		if (writeCommentary) runningCommentary("minimum" +MatrixUtil.dimensions(matrix)); 
		if (writeIntermediates) intermediates(Double.toString(result)); 
		return result;
	}
  // -----------------------maximum--------------------;
	public static double maximum(double[][] matrix){
		int numRows 	= MatrixUtil.numFullRows(matrix);
		int numColumns 	= MatrixUtil.numFullColumns(matrix);
		double result = matrix[0][0];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result = Math.max(matrix[i][j],result); 
			}
		}		
		if (writeCommentary) runningCommentary("maximum" +MatrixUtil.dimensions(matrix)); 
		if (writeIntermediates) intermediates(Double.toString(result)); 
		return result;
	}
	// ----------square elements of matrix---------------------------------square--;
	public static double[][] square(double[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		double[][] result = new double[numColumns][numRows];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[i][j]=(matrix[i][j])*(matrix[i][j]);
			}
		}
		if (writeCommentary) runningCommentary("square " +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}
	// ------------------------log--------------------;
	public static double[][] log(double[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		double[][] result = new double[numColumns][numRows];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[i][j]=Math.log(matrix[i][j]);
			}
		}
		if (writeCommentary) runningCommentary("log" +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}	
// -----------------------logprotected--------------------;
	public static double[][] logprotected(double[][] matrix){
		int numRows = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
		double[][] result = new double[numColumns][numRows];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				if (matrix[i][j] == 0) result[i][j]=0.0;				
				if (matrix[i][j] != 0) result[i][j]=Math.log(matrix[i][j]);
			}
		}		
		if (writeCommentary) runningCommentary("logprotected" +dimensions(matrix,result)); 
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}
//..................................................................................................................................................................................//



 
 
//......................................BASIC STATISTICAL PROCEDURES.........................................................................................//
// Design matix	 ----- ------ ;
		public static double[][] design(TaxaPartition partition){
			Taxa taxa = partition.getTaxa();
			int numTaxa = taxa.getNumTaxa();
			int numPartitions = partition.getNumberOfGroups();
			
			double[][] g = new double[numPartitions][numTaxa];      	
			for (int i=0; i<numPartitions;i++)
				for (int j=0; j<numTaxa; j++)		// fill with zeros
					 g[i][j] = 0;
			TaxaGroup[] groups = partition.getGroups();
			if (groups==null)
				return null;
			int ccc=0;							
			for (int i=0; i<numPartitions;i++) {
			        int nGroup = partition.getNumberInGroup(groups[i]);
			        for (int j=0; j<nGroup;j++) {
			                g[i][ccc] = 1;
			        	ccc=ccc+1;			    			      
				}
			} 
                  if (writeCommentary) runningCommentary("design" +dimensions(g));
                  if (writeIntermediates) intermediates(MatrixUtil.toString(g));
			return g;
		}
// ----------inverse of matrix ---------------------------------------inv---;
	 public static double[][] inverse(double[][] matrix){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  

		Matrix m = new Matrix(matrix);    
		Matrix r = m.inverse();
		double[][] result  = r.getArrayCopy();
                  if (writeCommentary) runningCommentary("inverse" +dimensions(matrix,result));
                  if (writeIntermediates) intermediates(MatrixUtil.toString(result));
			return result;
		/**/
		/**/
	}
// *----------------zero centers matrix by column---------------------zerocenter----;
	public static double[][] zeroCenter(double[][] matrix){
		int numRows = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null; 
		double[][] colmeans = MatrixUtil.columnAverages(matrix); 
		double[][] result   = new double[numColumns][numRows];
		for (int i=0; i<numRows; i++) {
			for (int j=0; j<numColumns; j++) {
				result[j][i]=matrix[j][i]-colmeans[j][0];
			}
		}	
		if (writeCommentary) runningCommentary("zeroCenter" +MatrixUtil.dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result));
		return result;
  	}
// *-------------------standard deviation----------------------------------------;
	public static double[][] standardDeviation(double[][] matrix){
		int numRows    = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null; 
		double[][] zerocentered  = MatrixUtil.zeroCenter(matrix);
   		double[][] result = new double[numColumns][1];
		double     divisor =  numRows-1;
		for (int i=0; i<numColumns; i++) {
			double sum = 0;
			for (int j=0; j<numRows; j++)
				sum+=(zerocentered[i][j])*(zerocentered[i][j]);
			result[i][0]= Math.sqrt(sum/divisor);
		}
		if (writeCommentary) runningCommentary("standardDeviation" +MatrixUtil.dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result));
		return result;
  	}
// *------------------group meansMatrix----------------------------------------groupMeansMatrix----------;
 	public static double[][] groupMeans(double[][] matrix, TaxaPartition partition){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
	  	double[][] G  = design(partition);
	  	double[][] GT = transpose(G);
	    	double[][] mean_w = multiply(multiply(inverse(multiply(GT,G)), GT), matrix);  
		double[][] result = multiply(G,mean_w);		 		 								
		if (writeCommentary) runningCommentary("groupMeans" +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result));
		return result;
	}
// *------------------Within group means ----------------------------------------WithingroupMeans----------;
 	public static double[][] withinGroupMeans(double[][] matrix, TaxaPartition partition){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  
	  	  double[][] G  = design(partition);
	  	  double[][] GT = transpose(G);
	    	double[][] mean_w = multiply(multiply(inverse(multiply(GT,G)), GT), matrix);  
		double[][] result = mean_w;
		if (writeCommentary) runningCommentary("withinGroupMeans" +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result));						
		return result;
	}
// *-------------------------------------------------------------pool----------;
	 public static double[][] pool(double[][] matrix, TaxaPartition partition){
	    	double[][] gmeans  = groupMeans(matrix,partition);  
		double[][] result  = subtract(matrix,gmeans);
		if (writeCommentary) runningCommentary("pool" +dimensions(result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result));
		return result;
	}
// *-------------------------------------------------------------zscores--------;
	public static double[][] zScores(double[][] matrix){
		int numRows     = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			 return null; 
		double[][] zerocentered  =  MatrixUtil.zeroCenter(matrix);
		double[][] stdevs            =  MatrixUtil.standardDeviation(matrix);
   		double[][] result             =  new double[numColumns][numRows];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[i][j]=zerocentered[i][j]/stdevs[i][0];
			}
		}
		if (writeCommentary) runningCommentary("zscores" +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result));
		return result;
  	}
 // *-------------------correlation matrix-------------------------------correlation---------;
	public static double[][] correlation(double[][] matrix1){
		int numRows1    = MatrixUtil.numFullRows(matrix1);
		int numColumns1 = MatrixUtil.numFullColumns(matrix1);
		if (numRows1==0 ||  numColumns1==0)
			return null; 
     		double[][] zscores1 = MatrixUtil.zScores(matrix1);
     		double[][] result   = MatrixUtil.multiply(1.0/(numRows1-1.0),(MatrixUtil.multiply(MatrixUtil.transpose(zscores1),zscores1)));
     		if (writeCommentary) runningCommentary("correlation" +MatrixUtil.dimensions(matrix1,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 	
            return result;
    }
   // *-------------------correlation for 2 different matrices------------------------------correlation2---------;
	public static double[][] correlation(double[][] matrix1, double[][] matrix2){
		int numRows1 = numFullRows(matrix1);
		int numColumns1 = numFullColumns(matrix1);
		if (numRows1==0 ||  numColumns1==0)
			return null; 
 		int numRows2 = numFullRows(matrix2);
		int numColumns2 = numFullColumns(matrix2);
		if (numRows2==0 ||  numColumns2==0)
			return null; 
		if (numRows1!=numRows2 ||  numColumns1!=numColumns2) 
			return null; 						
     		double[][] zscores1  =   zScores(matrix1);
     		double[][] zscores2  =   zScores(matrix2);
     		double[][] result = multiply(1.0/(numRows1-1.0),(multiply(transpose(zscores1),zscores2)));
  	  	if (writeCommentary) runningCommentary("correlation" +dimensions(matrix1,matrix2,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result));
		return result; 
  	}
 // *-------------------covariance matrix-------------------------------covariance---------;
	public static double[][] covariance(double[][] matrix){  
		int numRows    = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null; 					
      		double[][] zcent  =   zeroCenter(matrix);
      		double[][] result =   MatrixUtil.multiply(1.0/(numRows-1.0),(MatrixUtil.multiply(MatrixUtil.transpose(zcent),zcent)));
      	if (writeCommentary) runningCommentary("covariance" +MatrixUtil.dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
  	}
 // *-------------------covariance matrix from two different matrices----------------------covariance2---------;
	public static double[][] covariance(double[][] matrix1, double[][] matrix2){  
		int numRows1 = numFullRows(matrix1);
		int numColumns1 = numFullColumns(matrix1);
		if (numRows1==0 ||  numColumns1==0)
			return null; 
 		int numRows2 = numFullRows(matrix2);
		int numColumns2 = numFullColumns(matrix2);
		if (numRows2==0 ||  numColumns2==0)
			return null; 
		if (numRows1!=numRows2 ||  numColumns1!=numColumns2)
			return null; 						
     		double[][] zcent1  =   zeroCenter(matrix1);
     		double[][] zcent2  =   zeroCenter(matrix2);
            double[][] result = multiply(1.0/(numRows1-1.0),(multiply(transpose(zcent1),zcent2)));
     		if (writeCommentary) runningCommentary("covariance" +dimensions(matrix1,matrix2,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
  	}
// *------------------Pooled within-group covariance matrix---------------pwc---------;
	public static double[][] pooledWithinGroupCovariance(double[][] matrix, TaxaPartition partition){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null; 
		double[][] G  = 	design(partition);
		int numGroups = numFullColumns(G);  		
		double[][] pooled  = 	pool(matrix,partition); 
		double[][] result = multiply(1.0/(numRows-numGroups),(multiply(transpose(pooled),pooled)));
     		if (writeCommentary) runningCommentary("pooledWithinGroupCovariance" +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
  	}
// *----------------vector correlations---------------------------------vec_corr------;  
	public static double[][] vectorCorrelation(double[][] matrix,double[][] evec){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null; 
		double[][] rotated       = multiply(matrix,evec);
   		double[][] result         = correlation(matrix,rotated);
		if (writeCommentary) runningCommentary("vectorCorrelations" +dimensions(matrix,evec,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
  	}
	// *-----rescale matrix by vector-----------------------------------rescale--------;
	public static double[][] rescale(double[][] matrix, double[] vector){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  				 
   		double[][] result = new double[numColumns][numRows];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[i][j]=(matrix[i][j])*(vector[i]);
			}
		}
 		if (writeCommentary) runningCommentary("rescale" +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;	
	}
	// *-----rescale matrix by 1/ sqrt of vector-----------------------------rescalesqrt-----------;
	public static double[][] rescalesqrt(double[][] matrix, double[] vector){
		int numRows = numFullRows(matrix);
		int numColumns = numFullColumns(matrix);
		if (numRows==0 ||  numColumns==0)
			return null;  			
		double[][] result = new double[numColumns][numRows];
		for (int i=0; i<numColumns; i++) {
			for (int j=0; j<numRows; j++) {
				result[i][j]=(matrix[i][j])*Math.sqrt(1.0/vector[i]);
			}
		}
		if (writeCommentary) runningCommentary("rescalesqrt" +dimensions(matrix,result));
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;
	}
	// *-----percent explained-----------------------------percentage-----------;
	//  This is the one utility that uses a double[] because the PCA, ePCA code depends on it //
	public static double[] percentage(double[] vector){
		double[] result = new double[vector.length];
		double sum = 0;
		for (int i=0; i<vector.length; i++) {
			sum +=vector[i];
		}
		for (int j=0; j<vector.length; j++) {
			result[j]=vector[j]/sum;
		}
		if (writeCommentary) runningCommentary("percentage" );
		if (writeIntermediates) intermediates(MatrixUtil.toString(result)); 
		return result;	
	}
//..................................................................................................................................................................................//





//....................GENERAL LINEAR MODEL (GLM)..................................................................................................................//
//.................................
	public static double[][] GLMslopes(double[][] X,double[][] Y){
		int numRowsX    = MatrixUtil.numFullRows(X);
		int numColumnsX = MatrixUtil.numFullColumns(X);
		if (numRowsX==0 ||  numColumnsX==0)
			return null; 
		int numRowsY    = MatrixUtil.numFullRows(Y);
		int numColumnsY = MatrixUtil.numFullColumns(Y);
		if (numRowsX==0 ||  numColumnsX==0)
			return null; 
		if (numRowsX != numRowsY)
			return null; 
		double[][] Xaug = MatrixUtil.Haugment(X,MatrixUtil.fillMatrix(1,numRowsX,1.0)); 
   		double[][] TXaug = MatrixUtil.transpose(Xaug);
		double[][] slopeMatrix = MatrixUtil.multiply( MatrixUtil.multiply( MatrixUtil.inverse(MatrixUtil.multiply(TXaug,Xaug)),TXaug), Y);
		double[][] predMatrix  = MatrixUtil.multiply(Xaug,slopeMatrix);
		double[][] residMatrix = MatrixUtil.subtract(Y,predMatrix);
	   	if (writeCommentary) runningCommentary("GLMslopes" +MatrixUtil.dimensions(X,Y,slopeMatrix));
  	    	if (writeIntermediates) intermediates(MatrixUtil.toString(slopeMatrix));
  	    	return slopeMatrix;
  	} 
//.................................
	public static double[][] GLMresiduals(double[][] Y, double[][] predMatrix){
		double[][] residMatrix = MatrixUtil.subtract(Y,predMatrix);
	    	if (writeCommentary) runningCommentary("GLMresiduals" +MatrixUtil.dimensions(Y,predMatrix,residMatrix));
  	    	if (writeIntermediates) intermediates(MatrixUtil.toString(residMatrix));
  	     	return residMatrix;
  	} 
//.................................
	public static double[][] GLMpredicted(double[][] X, double[][] slopeMatrix){
		int numRowsX    = MatrixUtil.numFullRows(X);	
		double[][] Xaug = MatrixUtil.Haugment(X,MatrixUtil.fillMatrix(1,numRowsX,1.0)); 
		double[][] predMatrix  = MatrixUtil.multiply(Xaug,slopeMatrix);
	   	if (writeCommentary) runningCommentary("GLMpredicted"  +MatrixUtil.dimensions(X,slopeMatrix,predMatrix));
		if (writeIntermediates) intermediates(MatrixUtil.toString(predMatrix));
  	    	return predMatrix;
  	} 
 //..................................................................................................................................................................................//





//..............................THIN-PLATE SPLINES.....................................................................................................................//
//...........................principal warps...................................
	public static double[][] pwarps(double[][] matrix){
		int numRows    = MatrixUtil.numFullRows(matrix);
		int numColumns = MatrixUtil.numFullColumns(matrix);
		double [][] xvector = MatrixUtil.getColumn(matrix,0);
		double [][] yvector = MatrixUtil.getColumn(matrix,1);
		// function evaluation for weighting 
			double[][] postvector= MatrixUtil.fillMatrix(numRows,1,1.0);
			double[][] prevector= MatrixUtil.fillMatrix(1,numRows,1.0);
			double[][] R1= MatrixUtil.square(MatrixUtil.subtract(MatrixUtil.multiply(xvector,postvector),MatrixUtil.multiply(prevector,MatrixUtil.transpose(xvector))));
			double[][] R2= MatrixUtil.square(MatrixUtil.subtract(MatrixUtil.multiply(yvector,postvector),MatrixUtil.multiply(prevector,MatrixUtil.transpose(yvector))));
			double[][] Re=MatrixUtil.abs(MatrixUtil.add(R1,R2)); 
			double [][] logr2 = MatrixUtil.log( MatrixUtil.add(Re, MatrixUtil.identityMatrix(numRows)) );  
   		// Construct L matrix ;
			double[][]  K = MatrixUtil.multiplyElementwise(Re,logr2);
			double[][]  P = MatrixUtil.Haugment(prevector,matrix);
			double[][]  L = MatrixUtil.Vaugment(MatrixUtil.Haugment(K,P),MatrixUtil.Haugment(MatrixUtil.transpose(P),MatrixUtil.fillMatrix(3,3,0.0) ));	        
			double[][] Linv = MatrixUtil.inverse(L); 	
		// solve for Warp matrix, if one wants it, not necessary for transformation;
		// Lin = Linv[1:n,1:n];
		// Warp=MatrixUtil.multiply( Lin, MatrixUtil.multiply(K,Lin));
		// eigenanalysis of Warp matrix;
		// call eigen(eval,evec,Warp);
		if (writeCommentary) runningCommentary("pwarps" +MatrixUtil.dimensions(matrix,Linv));
		if (writeIntermediates) intermediates("pwarps" +MatrixUtil.toString(Linv)); 
	    return Linv;
	}
    // ....................thin-plate spline transformation................................................
   		// ...................takes any collection of points (points) and transforms them to new points (transformed);
   		// ...................must include base and target landmark configurations to calculate transformation; 
   public static double[][] tps(double[][] points, double[][] base_config,double[][]targ_config){
   	     int numRows = MatrixUtil.numFullRows(points);   
   	     int numRowsBase = MatrixUtil.numFullRows(base_config);  
		// mapping uses base configuration
			double[][] Linv = pwarps(base_config);
           // add in offset to target configuration
			double[][] LV=MatrixUtil.multiply(Linv,MatrixUtil.Vaugment(targ_config,MatrixUtil.fillMatrix(2,3, 0.0)));
			if (writeIntermediates) intermediates("LV" +MatrixUtil.toString(LV)); 
		// make some necessary ones vectors 
			double[][] ones2x1  = MatrixUtil.fillMatrix(1, 2, 1.0);
			double[][] onesrx1  = MatrixUtil.fillMatrix(1,numRowsBase,1.0);
			double[][] ones1x1  = MatrixUtil.fillMatrix(1, 1, 1.0);
		// calculate weighting function from base configuration
			double[][] transformed = new double[2][numRows];
			double[][] transF= new double[1][2];
			for (int i=0; i<numRows; i++) { 
				double[][] xy = MatrixUtil.getRow(points,i);
		  		double[][] Ud = MatrixUtil.multiply(
		 			 MatrixUtil.square(
					    MatrixUtil.subtract(base_config,
			    		        MatrixUtil.multiply(onesrx1,xy)
	  							)
		  					) 
		  				 , ones2x1);
				double[][] Ud1 = MatrixUtil.multiplyElementwise(Ud,MatrixUtil.logprotected(Ud));
				//Log-protected in case there are zeros.  
				// Ud2 is a weighting function, then a 1, then the point (xy) all appended together to form column vector  	
				double[][] Ud2 = MatrixUtil.transpose(MatrixUtil.Vaugment(Ud1,MatrixUtil.Vaugment(ones1x1,MatrixUtil.transpose(xy))));
                 		// transform arbitrary point (xy) according to LV matrix and append to transformed
				transF =MatrixUtil.multiply(Ud2,LV);
				transformed[0][i]=transF[0][0];
				transformed[1][i]=transF[1][0];
		  	}
		if (writeCommentary) runningCommentary("tps" +MatrixUtil.dimensions(points,transformed));
		if (writeIntermediates) intermediates("tps" +MatrixUtil.toString(transformed)); 
	  	return transformed;
	}
//......................................................................................................................................................................................//



//....................GRAPHING ROUTINES......................................................................................................................................//
//................................ transform 3D coordinates to 2D coordinates..............................//
		//  ...................................uses three viewing angles: theta, phi, and rho, and one viewing distance: D 
		 // .................................. angles are in radians, D is non-zero and generally positive 
public static double[][] transform3Dto2D (double[][] ABC, 
        		  double theta, // angle of viewpoint
                    double phi, // angle of viewpoint
                    double rho, //distance from viewpoint to 0,0,0
                    double D){ //  distance to viewplane
	double S1 = Math.sin(theta);
	double C1 = Math.cos(theta);
	double S2 = Math.sin(phi);
	double C2 = Math.cos(phi);
	double[][] XYZ  =   zeroCenter(ABC); 
	int numRows  = MatrixUtil.numFullRows(XYZ);
	double[][] result = new double[3][numRows];
	for (int i=0; i<3; i++) {
		for (int j=0; j<numRows; j++) {
			double X = XYZ[0][j];
			double Y = XYZ[1][j];
			double Z = XYZ[2][j];
			double ZE=   rho  -  Z*C2 - Y*S1*S2   - X*S2*C1;
			result[0][j] = D*(Y*C1 - X*S1)/ZE;
			result[1][j] = D*(Z*S2 - Y*C2*S1 - X*C1*C2)/ZE;
			result[2][j] = ZE;
		}
	}	 
 	if (writeCommentary) runningCommentary("transform3Dto2D" +MatrixUtil.dimensions(XYZ,result));
 	if (writeIntermediates) intermediates(MatrixUtil.toString(XYZ));
 	if (writeIntermediates) intermediates(MatrixUtil.toString(result));
 	return result;
}
 //........................................................
//...........................making and plotting grids for tps visualization...................................//
	//............................here's how I make a grid to transform. there is probably a better way in java
	//.......................... .basically, the idea is to make a grid to the base points, 
	// ...........................then use the trans function above to warp it to show the deformation produced by the tps transformation 
	public static double[][] makeGrid(double[][] points,int steps){
	    double[][] x = MatrixUtil.getColumn(points,0);
          double[][] y = MatrixUtil.getColumn(points,1);
	    double minx = minimum(x);
	    double miny = minimum(y);	    	    
	    double rangex = Math.abs(maximum(x) - minx);
	    double rangey = Math.abs(maximum(y) - miny);	   
	    double basex  = minx-rangex/10.0; // make visually pleasing
	    double basey  = miny-rangey/10.0;  // make visually pleasing
	    double stepx  = 1.2*rangex/steps;
	    double stepy  = 1.2*rangey/steps; 		
          double[][] grid = new double[2][steps*steps];
		    for ( int j=0; j<steps; j++) {
		      for ( int i=0; i<steps; i++) {
	   	        grid[0][j*steps+i]= (j+1)*stepx+basex ;
	  	         grid[1][j*steps+i]= (i+1)*stepy+basey ;        
	 	     }
		    }  		
		if (writeCommentary) runningCommentary("makeGrid"  +dimensions(grid));
		if (writeIntermediates) intermediates(MatrixUtil.toString(grid));
		return grid; 
	}
  //........................................................
  //........ here's a line drawing substitute I used for testing purposes........
	public static void drawLine(Graphics g, double x1, double y1, double x2, double y2){
		if (g!=null)
			g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
		else if (writeIntermediates) intermediates(" draw   ("
		   							+Double.toString(x1)  +", "
		   							+Double.toString(y1)  +")  to  ("
		   							+Double.toString(x2)  +", "
		   							+Double.toString(y2)  +")  " ) ;
     return;
     }
//............................................................................................	
	public static double[][] plotGrid(Graphics g, double[][] grid, int steps){
		for ( int k=0; k<(steps-1); k++) {
	 	   	for ( int l=0; l<(steps-1); l++) {
	 	   	     int i  = steps*(k)+1;
	 	   	     int ir = i+1;
	 	   	     int ib = i+steps-1;
                       drawLine(g, grid[0][i],grid[1][i], grid[0][ir], grid[1][ir]);
	                 drawLine(g, grid[0][i],grid[1][i], grid[0][ib], grid[1][ib]);	     
	    		} 
	    	} 
		for ( int k=0; k<(steps-1); k++) {
	 	   	     int i  = steps*(k);
	 	   	     int ir = i+steps;
                 		drawLine(g, grid[0][i],grid[1][i], grid[0][ir], grid[1][ir]);
	    	} 		
		for ( int l=0; l<(steps-1); l++) {
	 	   	     int i  = steps*(steps-1)+(l);
	 	   	     int ib = i+1;
		        	drawLine(g, grid[0][i],grid[1][i], grid[0][ib], grid[1][ib]);
	   	 } 
		if (writeCommentary) runningCommentary("gridPlot" +MatrixUtil.dimensions(grid));
		if (writeIntermediates) intermediates(MatrixUtil.toString(grid));
		return grid; 
		} 
}
//..................................................................................................................................................................................//






/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.Correlation2Trees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;


public class Correlation2Trees extends DistanceBetween2Trees {
	PatristicDistances p1, p2;
	boolean isDistance = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		p1 = new PatristicDistances();
		p2 = new PatristicDistances();
		isDistance = (getHiredAs() == DistanceBetween2Trees.class);
		
 		return true;
  	 }
	public boolean largerIsFurther(){
		return false;
	}
  	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree t1, Tree t2) {
	
	}
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree1, Tree tree2, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (tree1 == null)
			return;
		if (tree2 == null)
			return;

		int numTaxa = tree1.getTaxa().getNumTaxa();
		
		double[][] patristic1 = null;
		patristic1 = p1.calculatePatristic(tree1, numTaxa, patristic1); //for this tree calculate patristic distances (number of nodes separating terminals; no branch lengths)
		double[][] patristic2 = null;
		patristic2 =  p2.calculatePatristic(tree2, numTaxa, patristic2); //for this tree calculate patristic distances (number of nodes separating terminals; no branch lengths)
		
		
		double correl = offDiagonalPMCorrelationFILTERED(patristic1, patristic2);
		if (isDistance && (MesquiteDouble.isCombinable(correl)))
			correl = -correl + 1.0;  //shifting 1 to -1 to be 0 to 2 to act as distance
		result.setValue(correl);
		if (resultString!=null){
			if (isDistance)
				resultString.setValue("Patristic correlation (converted to distance): "+ result.toString());
			else
				resultString.setValue("Patristic correlation: "+ result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
		
	}
	
	//by filtered means -ve and uncombinable numbers excluded
	double offDiagonalPMCorrelationFILTERED(double[][] m1, double[][] m2){
		if (m1==null || m2 == null)
			return MesquiteDouble.unassigned;
			
		double mean1 = meanFILTERED(m1);
		double mean2 = meanFILTERED(m2);
		if (!MesquiteDouble.isCombinable(mean1) || !MesquiteDouble.isCombinable(mean2))
			return MesquiteDouble.unassigned;
		
		double sumSq1 = sumSqFILTERED(m1, mean1);
		double sumSq2 = sumSqFILTERED(m2,mean2);
		double sumProd = 0;
		for (int i= 0; i<m1.length; i++)
			for (int j= 0; j<i; j++) {
				double d1 = m1[i][j];
				double d2 = m2[i][j];
				if (d1>=-0.0000000001 && MesquiteDouble.isCombinable(d1) && d2>=-0.0000000001 && MesquiteDouble.isCombinable(d2))
					sumProd += (d1-mean1)*(d2-mean2);
			}
		if (sumSq1 == 0 || sumSq2 == 0)
			return MesquiteDouble.unassigned;
		
		return sumProd/Math.sqrt(sumSq1*sumSq2);
	}
	double meanFILTERED(double[][] m){
		double sum = 0;
		int n = 0;
		for (int i= 0; i<m.length; i++)
			for (int j= 0; j<i; j++) {
				double d = m[i][j];
				if (d>=-0.0000000001 && MesquiteDouble.isCombinable(d)) {
					sum += d;
					n++;
				}
			}
		if (n==0)
			return MesquiteDouble.unassigned;
		return sum/n;
	}
	double sumSqFILTERED(double[][] m, double mean){
		double sumSq = 0;
		for (int i= 0; i<m.length; i++)
			for (int j= 0; j<i; j++) {
				double d = m[i][j];
				if (d>=-0.0000000001 && MesquiteDouble.isCombinable(d))
					sumSq += (d-mean)*(d-mean);
			}
		return sumSq;
	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	 
	/*.................................................................................................................*/
    	 public String getParameters() {
		if (isDistance)
			return "Correlation converted to distance (1 to -1 becomes 0 to 2)";
		else
			return "Correlation in natural form (i.e., not converted to distance)";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Patristic distance correlation";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Calculates the product moment correlation coefficient among the off-diagonal elements of the patristic distances matrices of two trees.  Ignores unassigned and negative values in the matrices.  If employed as a distance, converts scores from 1 to -1 to scores from 0 to 2";
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
}


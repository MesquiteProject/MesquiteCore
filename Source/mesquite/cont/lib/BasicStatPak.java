/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.71, September 2009.
This file copyright 2006 P. Midford and W. Maddison

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.lib;

import JSci.maths.statistics.TDistribution;
import mesquite.lib.MesquiteDouble;

public class BasicStatPak {

	private int n;
	private int n1;
	private int n2;
	private double min[];
	private double max[];
	private double sx;
	private double sx2;
	private double sy;
	private double sy2;
	private double sxy;
	private TDistribution tDist;
	private boolean ordinalStats = false;
	private boolean parametricStats = false;
	private double[] x;
	private double[] y;
	
	/**
	 * creates a StatPak for the values x and y
	 * @param x data values for independent variable
	 * @param y data values for dependent variable
	 */
	public BasicStatPak(double[] x, double[]y) {
		super();
		this.x = x;
		this.y = y;
	}

	/**
	 * 
	 * @return sample size (number of points) or -1 if x and y have different numbers of values
	 */
	public int n(){
		if (!ordinalStats)
			doOrdinalStats();
		return n;
	}
	
	/**
	 * 
	 * @return sample size for independent variable (x)
	 */
	public int n1(){
		if (!ordinalStats)
			doOrdinalStats();
		return n1;
	}

	/**
	 * 
	 * @return sample size for dependent variable (y)
	 */
	public int n2(){
		if (!ordinalStats)
			doOrdinalStats();
		return n2;
	}
	
	/**
	 * 
	 * @return largest value in x
	 */
	public double maxX(){
		if (!ordinalStats)
			doOrdinalStats();
		return max[0];
	}

	/**
	 * 
	 * @return largest value in y
	 */
	public double maxY(){
		if (!ordinalStats)
			doOrdinalStats();
		return max[1];
	}

	/**
	 * 
	 * @return smallest value in x
	 */
	public double minX(){
		if (!ordinalStats)
			doOrdinalStats();
		return min[0];
	}

	/**
	 * 
	 * @return smallest value in y
	 */
	public double minY(){
		if (!ordinalStats)
			doOrdinalStats();
		return min[1];
	}
	
	/**
	 * 
	 * @return mean of x, or unassigned if x is empty
	 */
	public double meanX(){
		if (!parametricStats)
			doParametricStats();
		if (n1 < 1)
			return MesquiteDouble.unassigned;
		else
			return sx/n1;
	}

	/**
	 * 
	 * @return mean of y, or unassigned if y is empty
	 */
	public double meanY(){
		if (!parametricStats)
			doParametricStats();
		if (n2 < 1)
			return MesquiteDouble.unassigned;
		else
			return sy/n2;
	}

	/**
	 * 
	 * @return variance of x (n-1 weighted), or unassigned if x has less than two values
	 */
	public double variance1(){
		if (!parametricStats)
			doParametricStats();
		if (n1 < 2)
			return MesquiteDouble.unassigned;
		else
			return ((sx2-(sx*sx/n1))/(n1-1));
	}

	
	/**
	 * 
	 * @return variance of y (n-1 weighted), or unassigned if y has less than two values 
	 */
	public double variance2(){
		if (!parametricStats)
			doParametricStats();
		if (n2 < 2)
			return MesquiteDouble.unassigned;
		else
			return (sy2-(sy*sy/n2))/(n2-1);
	}

	/**
	 * 
	 * @return covariance of x and y, or unassigned if x and y have different numbers of values
	 */
	public double covariance(){
		if (!parametricStats)
			doParametricStats();
		if (n1 != n2)
			return MesquiteDouble.unassigned;
		else
			return (sxy-(sx*sy/n));
	}

	/**
	 * 
	 * @return pearson correlation of x and y
	 */
	public double correlation(){
		double cov = covariance();
		if (!MesquiteDouble.isCombinable(cov))
			return MesquiteDouble.unassigned;
		else	{
			double s12= sx2-(sx*sx/n);
			double s22= sy2-(sy*sy/n);
			return cov/Math.sqrt(s12*s22);
		}
	}
	
	/**
	 * 
	 * @return the slope of a least squares regression
	 */
	public double slope(){
		if (!parametricStats)
			doParametricStats();
		if (n1 != n2  || n < 1)
			return MesquiteDouble.unassigned;
		else		
			return (sxy-(sx*sy/n))/(sx2-(sx*sx/n));
	}
	
	/**
	 * 
	 * @return the y-intercept of a least squares regression
	 */
	public double intercept(){
		double slope = slope();
		if (!MesquiteDouble.isCombinable(slope))
			return MesquiteDouble.unassigned;
		else		
			return meanY()-slope*meanX();
	}
	
	
	/**
	 * 
	 * @return the t-statistic (sqrt(F1,n-2)) for the significance of the regression
	 */
	public double regressionT(){
		if (!parametricStats)
			doParametricStats();
		if (n1 != n2  || n < 1)
			return MesquiteDouble.unassigned;
		double regSlope = slope();
		double regIntercept = intercept();
		double yhat2 = 0;
		for(int i=0;i<n;i++){
			double yhat = regSlope*x[i] + regIntercept;
			yhat2 += yhat*yhat;
		}
		double s2YX = (sy2-yhat2)/(n-2);
		double seSlope = Math.sqrt(s2YX/(sx2-(sx*sx/n)));
		return regSlope/seSlope;
	}
	
	/**
	 * 
	 * @return significance value for the regression, based on t-statistic above
	 */
	public double regressionPValue(){
 		double stat = regressionT();
 		if (!MesquiteDouble.isCombinable(stat))
 			return MesquiteDouble.unassigned;
 		tDist = new TDistribution(n-2);
		double cumulative = tDist.cumulative(stat);
		if (cumulative > 0.5)
			return 1-cumulative;
		else return cumulative;

	}
	
	
	/**
	 * 
	 * @return the slope of a least-squares regression through the origin (intercept = 0)
	 */
	public double slopeOrg(){
		if (!parametricStats)
			doParametricStats();
		if (n1 != n2  || n < 1)
			return MesquiteDouble.unassigned;	
		return sxy/sx2;
	}
	
	/**
	 * 
	 * @return the major axis (principal component) slope
	 */
	public double maSlope(){
		if (!parametricStats)
			doParametricStats();
		if (n1 != n2  || n < 1)
			return MesquiteDouble.unassigned;	
		double v1 = (sx2-(sx*sx/n1))/(n-1);
		double v2 = (sy2-(sy*sy/n2))/(n-1);
		double cov = covariance()/(n-1);
		double d = Math.sqrt(((v1+v2)*(v1+v2)) - 4*(v1*v2-cov*cov));
		double lambda1 = (v1+v2+d)/2;
		
		// Sokal and Rolf 1981 set Y1 for vertical axis, for example on 595-596.
		if (lambda1 == v1) {
			if (cov >0)
				return MesquiteDouble.infinite;
			else return MesquiteDouble.negInfinite;
		}
		else return cov/(lambda1-v1);
		
	}
	
	/**
	 * 
	 * @return y-intercept corresponding to the major axis slope
	 */
	public double maIntercept(){
		double slope = maSlope();
		if (!MesquiteDouble.isCombinable(slope))
			return MesquiteDouble.unassigned;
   		return meanY()-slope*meanX();

	}
	
	
	/**
	 * 
	 * @return the reduced major axis slope
	 */
	public double rmaSlope(){
		if (!parametricStats)
			doParametricStats();
		if (n1 != n2  || n < 1)
			return MesquiteDouble.unassigned;
		double v1 = variance1();
		double v2 = variance2();
		if (MesquiteDouble.isCombinable(v1) && v1>0) 
			if (sxy >0) // make sure the sign of the reduced major axis slope agrees with 
				return Math.sqrt(v2/v1);
			else return -1*Math.sqrt(v2/v1);
		else
			return MesquiteDouble.unassigned;
	}
	
	/**
	 * 
	 * @return y-intercept corresponding to the reduced major axis slope
	 */
	public double rmaIntercept(){
		double slope = rmaSlope();
		if (!MesquiteDouble.isCombinable(slope))
			return MesquiteDouble.unassigned;
		else return meanY()-slope*meanX();
	}
	
	/**
	 * 
	 * @return the t-statistic comparing x and y as paired values
	 */
	public double pairedTStat(){
		if (!parametricStats)
			doParametricStats();
		if (n1 != n2  || n < 2)
			return MesquiteDouble.unassigned;
 		double diff;
 		double diffSum = 0;
 		double diffSumSq = 0;
 		for(int i=0;i<n;i++){
 			diff = x[i]-y[i];
 			diffSum += diff;
 			diffSumSq += diff*diff;
 		}
 		double diffMean = diffSum/n;
 		double diffS2 = (diffSumSq-(diffSum*diffSum/n))/(n-1);
 		double diffErr = Math.sqrt(diffS2)/Math.sqrt(n);
 		double result = diffMean/diffErr;
 		return result;
	}

	/**
	 * 
	 * @return the p-value for a t-test comparing x and y as unpaired values
	 */
	public double pairedTPvalue(){
 		double stat = pairedTStat();
 		if (stat == MesquiteDouble.unassigned)
 			return stat;
 		tDist = new TDistribution(n-1);
		double cumulative = tDist.cumulative(stat);
		if (cumulative > 0.5)
			return 1-cumulative;
		else return cumulative;
	}
	
	
	/**
	 * 
	 * @return the t-statistic comparing x and y as unpaired values
	 */
	public double unPairedTStat(){
		if (!parametricStats)
			doParametricStats();
		if (n1 < 2 || n2 < 2)
			return MesquiteDouble.unassigned;
		double pooledS = Math.sqrt(((n1-1)*variance1() + (n2-1)*variance2())/(n1+n2-2));
		return (meanX()-meanY())/(pooledS*Math.sqrt(1/(double)n1 + 1/(double)n2));
	}
	
	/**
	 * 
	 * @return the p-value for a t-test comparing x and y as unpaired values
	 */
	public double unPairedTPvalue(){
		double stat = unPairedTStat();
		if (stat == MesquiteDouble.unassigned)
			return stat;
		tDist = new TDistribution(n-2);
		double cumulative = tDist.cumulative(stat);
		if (cumulative > 0.5)
			return 1-cumulative;
		else return cumulative;
	}
	
	
	private void doOrdinalStats(){
		ordinalStats = true;
		max = new double[2];
		min = new double[2];
		max[0]=MesquiteDouble.negInfinite;
		max[1]=MesquiteDouble.negInfinite;
		min[0]=MesquiteDouble.infinite;
		min[1]=MesquiteDouble.infinite;
		if (x != null){
			for(n1=0;n1<x.length;n1++){
				if (x[n1]==MesquiteDouble.unassigned)
					break;
				double curX = x[n1];
				if (max[0]< curX)
					max[0] = curX;
				if (min[0] > curX)
					min[0] = curX;
			}
		}
		if (y != null){
			for(n2=0;n2<y.length;n2++){
				if (y[n2]==MesquiteDouble.unassigned)
					break;
				double curY = y[n2];
				if (max[1]< curY)
					max[1] = curY;
				if (min[1] > curY)
					min[1] = curY;
			}
		}
		if (n1 == n2)
			n = n1;
		else
			n = -1;
	}
	
	private void doParametricStats(){
		if (!ordinalStats)
			doOrdinalStats();
		parametricStats = true;
		sx = 0;
		sx2 = 0;
		sy = 0;
		sy2 = 0;
		sxy = 0;
		if (x != null){
			for (int i=0;i<n1;i++){
				double curX = x[i];
				sx += curX;
				sx2 += curX*curX;
				if (y != null) {
					if (i< n2){
						double curY = y[i];
						sy += curY;
						sy2 += curY*curY;
						sxy += curX*curY;
					}
				}
			}
		}
		if (y != null && n2> n1){
			for (int i=n1;i<n2;i++){
				double curY = y[i];
				sy += curY;
				sy2 += curY*curY;
			}
		}
	}
	
	
}

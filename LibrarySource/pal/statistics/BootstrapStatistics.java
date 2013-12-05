// BootstrapStatistics.java
//
// (c) 2000-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.statistics;

import pal.util.*;


/**
 * computation of bootstrap estimators (BIAS, SD, VAR, CI)
 * given a statistic theta and corresponding bootstrap replicates.
 *
 * See for background theory:
 * i) B. Efron and R. J.Tibshirani. 1993. An introduction
 *    to the bootstrap. Chapman and Hall, New York
 * ii) P. Hall. 1992. The bootstrap and Edgeworth expansion.
 *     Springer, New York
 *
 * @version $Id: BootstrapStatistics.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class BootstrapStatistics
{
	//
	// Public stuff
	//

	/**
	 * compute bias of a statistic thetaHat in estimating the true theta
	 * 
	 * @param thetaHat      the statistic
	 * @param thetaHatStar  bootstrap replicates of thetaHat
	 *
	 * @return estimate of bias (notation: BIAS with hat)
	 */
	public static double computeBIAS(double thetaHat, double[] thetaHatStar)
	{
		double thetaHatStarMean = DiscreteStatistics.mean(thetaHatStar);
		
		return thetaHatStarMean - thetaHat;
	}

	/**
	 * correct a statistic thetaHat for its bias
	 * 
	 * @param thetaHat      the statistic
	 * @param thetaHatStar  bootstrap replicates of thetaHat
	 *
	 * @return bias-corrected estimate
	 */
	public static double biasCorrectedEstimate(double thetaHat, double[] thetaHatStar)
	{
		return thetaHat - computeBIAS(thetaHat, thetaHatStar);
	}

	/**
	 * compute variance of a statistic thetaHat
	 * 
	 * @param thetaHatStar  bootstrap replicates of statistic thetaHat
	 *
	 * @return estimate of variance of thetaHat (notation: VAR with hat)
	 */
	public static double computeVAR(double[] thetaHatStar)
	{
		return DiscreteStatistics.variance(thetaHatStar);
	}

	/**
	 * compute standard error (accuracy) of a statistic thetaHat
	 * 
	 * @param thetaHatStar  bootstrap replicates of statistic thetaHat
	 *
	 * @return estimate of standard error of thetaHat (notation: SD with hat)
	 */
	public static double computeSD(double[] thetaHatStar)
	{
		return DiscreteStatistics.stdev(thetaHatStar);
	}
	

	/**
	 * compute approximate central confidence interval for thetaHat
	 * (Efron percentile method)
	 * 
	 * @param level        confidence level (e.g., 0.95)
	 * @param thetaHatStar bootstrap replicates of statistic thetaHat
	 *
	 * @return confidence set (array of two doubles)
	 */
	public static double[] efronCI(double level, double[] thetaHatStar)
	{
		int[] indices = new int[thetaHatStar.length];
		
		return efronCI(level, thetaHatStar, indices);
	}

	/**
	 * compute approximate central confidence interval for thetaHat
	 * (Efron percentile method)
	 * 
	 * @param level        confidence level (e.g., 0.95)
	 * @param thetaHatStar bootstrap replicates of statistic thetaHat
	 * @param array        helper integer array (same length as thetaHatStar) 
	 *
	 * @return confidence set (array of two doubles)
	 */
	public static double[] efronCI(double level, double[] thetaHatStar, int[] array)
	{
		HeapSort.sort(thetaHatStar, array);
		
		double alpha = (1.0-level)/2.0;
		double[] result = new double[2];
		
		result[0] = DiscreteStatistics.quantile(alpha, thetaHatStar, array);
		result[1] = DiscreteStatistics.quantile(1.0-alpha, thetaHatStar, array);
		
		return result;
	}

	/**
	 * compute approximate central confidence interval for thetaHat
	 * (Hall percentile method)
	 * 
	 * @param level        confidence level (e.g., 0.95)
	 * @param thetaHat     the statistic
	 * @param thetaHatStar bootstrap replicates of statistic thetaHat
	 *
	 * @return confidence set (array of two doubles)
	 */
	public static double[] hallCI(double level, double thetaHat, double[] thetaHatStar)
	{
		int[] indices = new int[thetaHatStar.length];
		
		return hallCI(level, thetaHat, thetaHatStar, indices);
	}

	/**
	 * compute approximate central confidence interval for thetaHat
	 * (Hall percentile method)
	 * 
	 * @param level        confidence level (e.g., 0.95)
	 * @param thetaHat     the statistic
	 * @param thetaHatStar bootstrap replicates of statistic thetaHat
	 * @param array        helper integer array (same length as thetaHatStar) 
	 *
	 * @return confidence set (array of two doubles)
	 */
	public static double[] hallCI(double level, double thetaHat, double[] thetaHatStar, int[] array)
	{
		HeapSort.sort(thetaHatStar, array);
		
		double alpha = (1.0-level)/2.0;
		double[] result = new double[2];
		
		// confidence set for the bias (=thetaHatStar-thetaHat)
		double t1 = DiscreteStatistics.quantile(alpha, thetaHatStar, array)-thetaHat;
		double t2 = DiscreteStatistics.quantile(1.0-alpha, thetaHatStar, array)-thetaHat;
		
		// confidence set for the theta
		result[0] = thetaHat-t2;
		result[1] = thetaHat-t1;
		
		return result;
	}
}


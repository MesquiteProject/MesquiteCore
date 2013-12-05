// DiscreteStatistics.java
//
// (c) 2000-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.statistics;

import pal.util.*;


/**
 * simple discrete statistics (mean, variance, cumulative probability, quantiles etc.)
 *
 * @version $Id: DiscreteStatistics.java,v 1.5 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class DiscreteStatistics
{
	//
	// Public stuff
	//

	/**
	 * compute mean
	 * 
	 * @param x list of numbers
	 *
	 * @return mean
	 */
	public static double mean(double[] x)
	{
		double m = 0;
		int len = x.length;
		for (int i = 0; i < len; i++)
		{
			m += x[i];
		}
		
		return m/(double) len;
	}

	/**
	 * compute variance (ML estimator)
	 * 
	 * @param x list of numbers
	 * @param mean assumed mean of x
	 *
	 * @return variance of x (ML estimator)
	 */
	public static double variance(double[] x, double mean)
	{
		double var = 0;
		int len = x.length;
		for (int i = 0; i < len; i++)
		{
			double diff = x[i]-mean;
			var += diff*diff;
		}
		
		int n;
		if (len < 2)
		{
			n = 1; // to avoid division by zero
		}
		else
		{
			n = len-1; // for ML estimate
		}
		
		return var/ (double) n;
	}
	
	/**
	 * compute fisher skewness 
	 * 
	 * @param x list of numbers
	 *
	 * @return skewness of x 
	 */
	public static double skewness(double[] x) {
	
		double mean = mean(x);
		double stdev = stdev(x);
		double skew = 0.0;
		double len = x.length;
		
		for (int i = 0; i < x.length; i++)
		{
			double diff = x[i]-mean;
			diff /= stdev;
			
			skew += (diff*diff*diff);
		}

		skew *= (len / ((len - 1) * (len - 2)));
	
		return skew;
	}

	/**
	 * compute standard deviation 
	 * 
	 * @param x list of numbers
	 *
	 * @return standard deviation of x 
	 */
	public static double stdev(double[] x) {
		return Math.sqrt(variance(x));
	}

	/**
	 * compute variance (ML estimator)
	 * 
	 * @param x list of numbers
	 *
	 * @return variance of x (ML estimator)
	 */
	public static double variance(double[] x)
	{
		double m = mean(x);
		return variance(x, m);
	}


	/**
	 * compute variance of sample mean (ML estimator)
	 * 
	 * @param x list of numbers
	 * @param mean assumed mean of x
	 *
	 * @return variance of x (ML estimator)
	 */
	public static double varianceSampleMean(double[] x, double mean)
	{
		return variance(x, mean)/(double) x.length;
	}

	/**
	 * compute variance of sample mean (ML estimator)
	 * 
	 * @param x list of numbers
	 *
	 * @return variance of x (ML estimator)
	 */
	public static double varianceSampleMean(double[] x)
	{
		return variance(x)/(double) x.length;
	}


	/**
	 * compute the q-th quantile for a distribution of x
	 * (= inverse cdf)
	 * 
	 * @param q quantile (0 < q <= 1)
	 * @param x discrete distribution (an unordered list of numbers)
	 * @param indices index sorting x
	 *
	 * @return q-th quantile
	 */
	public static double quantile(double q, double[] x, int[] indices)
	{
		if (q < 0.0 || q > 1.0) throw new IllegalArgumentException("Quantile out of range");
		
		if (q == 0.0)
		{
			// for q==0 we have to "invent" an entry smaller than the smallest x
			
			return x[indices[0]] - 1.0;
		}
		
		return x[indices[(int) Math.ceil(q*x.length)-1]];
	}	

	/**
	 * compute the q-th quantile for a distribution of x
	 * (= inverse cdf)
	 * 
	 * @param q quantile (0 <= q <= 1)
	 * @param x discrete distribution (an unordered list of numbers)
	 *
	 * @return q-th quantile
	 */
	public static double quantile(double q, double[] x)
	{
		int[] indices = new int[x.length];
		HeapSort.sort(x, indices);
		
		return quantile(q, x, indices);
	}
	
	/**
	 * compute the cumulative probability Pr(x <= z) for a given z
	 * and a distribution of x
	 * 
	 * @param z threshold value
	 * @param x discrete distribution (an unordered list of numbers)
	 * @param indices index sorting x
	 *
	 * @return cumulative probability
	 */
	public static double cdf(double z, double[] x, int[] indices)
	{
		int i;
		for (i = 0; i < x.length; i++)
		{
			if (x[indices[i]] > z) break;
		}
		
		return (double) i/ (double) x.length;
	}
	
	/**
	 * compute the cumulative probability Pr(x <= z) for a given z
	 * and a distribution of x
	 * 
	 * @param z threshold value
	 * @param x discrete distribution (an unordered list of numbers)
	 *
	 * @return cumulative probability
	 */
	public static double cdf(double z, double[] x)
	{
		int[] indices = new int[x.length];
		HeapSort.sort(x, indices);
		
		return cdf(z, x, indices);
	}
}


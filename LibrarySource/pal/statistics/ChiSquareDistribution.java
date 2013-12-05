// ChiSquareDistribution.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.statistics;

import pal.math.*;


/**
 * chi-square distribution
 * (distribution of sum of squares of n uniform random variables)
 *
 * (Parameter: n; mean: n; variance: 2*n)
 *
 * The chi-square distribution is a special case of the Gamma distribution
 * (shape parameter = n/2.0, scale = 2.0).
 *
 * @version $Id: ChiSquareDistribution.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class ChiSquareDistribution extends GammaDistribution
{
	//
	// Public stuff
	//

	/**
	 * probability density function of the chi-square distribution
	 * 
	 * @param x argument
	 * @param n degrees of freedom
	 *
	 * @return pdf value
	 */
	public static double pdf(double x, double n)
	{
		return pdf(x, n/2.0, 2.0);
	}

	/**
	 * cumulative density function of the chi-square distribution
	 * 
	 * @param x argument
	 * @param n degrees of freedom
	 *
	 * @return cdf value
	 */
	public static double cdf(double x, double n)
	{
		return cdf(x, n/2.0, 2.0);
	}


	/**
	 * quantile (inverse cumulative density function) of the chi-square distribution
	 * 
	 * @param x argument
	 * @param n degrees of freedom
	 *
	 * @return icdf value
	 */
	public static double quantile(double y, double n)
	{
		return quantile(y, n/2.0, 2.0);
	}
	
	/**
	 * mean of the chi-square distribution
	 * 
	 * @param n degrees of freedom
	 *
	 * @return mean
	 */
	public static double mean(double n)
	{
		return n;
	}

	/**
	 * variance of the chi-square distribution
	 * 
	 * @param n degrees of freedom
	 *
	 * @return variance
	 */
	public static double variance(double n)
	{
		return 2.0*n;
	}
}


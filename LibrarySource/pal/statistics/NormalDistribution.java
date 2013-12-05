// NormalDistribution.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.statistics;

import pal.math.*;

/**
 * normal distribution (pdf, cdf, quantile)
 *
 * @version $Id: NormalDistribution.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class NormalDistribution
{
	//
	// Public stuff
	//

	/**
	 * probability density function
	 *
	 * @param x argument
	 * @param m mean
	 * @param sd standard deviation
	 *
	 * @return pdf at x
	 */
	public static double pdf(double x, double m, double sd)
	{
		double a = 1.0/(Math.sqrt(2.0*Math.PI)*sd);
		double b = -(x-m)*(x-m)/(2.0*sd*sd);
		
		return a*Math.exp(b);
	}

	/**
	 * cumulative density function
	 *
	 * @param x argument
	 * @param m mean
	 * @param sd standard deviation
	 *
	 * @return cdf at x
	 */
	public static double cdf(double x, double m, double sd)
	{
		double a = (x-m)/(Math.sqrt(2.0)*sd);
		
		return 0.5*(1.0+ErrorFunction.erf(a));
	}
	
	/**
	 * quantiles (=inverse cumulative density function)
	 *
	 * @param z argument
	 * @param m mean
	 * @param sd standard deviation
	 *
	 * @return icdf at z
	 */
	public static double quantile(double z, double m, double sd)
	{
		return m + Math.sqrt(2.0)*sd*ErrorFunction.inverseErf(2.0*z-1.0);
	}
	
	/**
	 * mean
	 *
	 * @param m mean
	 * @param sd standard deviation
	 *
	 * @return mean
	 */
	public static double mean(double m, double sd)
	{
		return m;
	}

	/**
	 * variance
	 *
	 * @param m mean
	 * @param sd standard deviation
	 *
	 * @return variance
	 */
	public static double variance(double m, double sd)
	{
		return sd*sd;
	}
}


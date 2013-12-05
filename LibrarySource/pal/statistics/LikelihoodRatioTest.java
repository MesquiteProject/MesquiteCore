// LikelihoodRatioTest.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.statistics;

import pal.math.*;


/**
 * Likelihood ratio test based on chi-square statistics
 *
 * @version $Id: LikelihoodRatioTest.java,v 1.5 2001/07/13 14:39:13 korbinian Exp $ 
 *
 * @author Korbinian Strimmer
 */
public class LikelihoodRatioTest
{
	//
	// Public stuff
	//
	
	/**
	 * compute significance level for the differences
	 * in log-likelihood (based on chi-square distribution)
	 *
	 * @param deltaL  difference of Log Likelihood values (>=0)
	 * @param df      degrees of freedom
	 *
	 * @return        significance level
	 */
	public static double getSignificance(double deltaL, int df)
	{
		return 1.0-ChiSquareDistribution.cdf(2.0*deltaL, df);
	}
}


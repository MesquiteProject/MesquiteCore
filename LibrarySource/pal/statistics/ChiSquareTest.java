// ChiSquareTest.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.statistics;

import pal.math.*;


/**
 * chi-square test
 *
 * @version $Id: ChiSquareTest.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class ChiSquareTest
{
	//
	// Public stuff
	//
	
	/** 
	 * chi square test
	 *
	 * @param  ef      expected frequencies (sum up to 1 !!)
	 * @param  of      observed frequencies (sum up to the number of samples)
	 *
	 * @return critical significance level (negative if in chi2 test may be invalid)
	 */
	public static double compare(double[] ef, int[] of)
	{	
		int samples;
		boolean chi2failed = false;
		
		/* compute number of samples */
		samples = 0;
		for (int i = 0; i <of.length; i++)
		{
			samples = samples + of[i];
		}
		
		/* compute chi square */
		double chi2 = 0;
		int below1 = 0;
		int below5 = 0;
		for (int i = 0; i < of.length; i++)
		{
			double efn = ef[i]*((double) samples);
			if (efn < 1.0)
			{
				below1++;
			}
			if (efn < 5.0)
			{
				below5++;
			}
			chi2 = chi2 + ((double) of[i]-efn)*((double) of[i]-efn)/efn;
		}
	
		/* compute significance */
		double criticals = chi2prob(of.length-1, chi2);
		
		/* no expected frequency category (sum up to # samples) below 1.0 */
		if (below1 > 0)
		{
			chi2failed = true;
		}
		/* no more than 1/5 of the frequency categories below 5.0 */
		if (below5 > (int) Math.floor(samples/5.0))
		{
			chi2failed = true;
		}
	
		if (chi2failed)
		{
			return -criticals;
		}
		else
		{
			return criticals;
		}
	}
	
	/**
	 * probability that the observed chi-square
	 * exceeds chi2 even if model is correct
	 *
	 * @param deg degrees of freedom
	 * @param chi2 chi-square
	 *
	 * @return probability
	 */
	private static double chi2prob (int deg, double chi2)
	{
		return 1.0-ChiSquareDistribution.cdf(chi2, deg);
	}	
}


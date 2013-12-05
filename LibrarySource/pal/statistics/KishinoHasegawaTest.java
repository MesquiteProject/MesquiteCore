// KishinoHasegawaTest.java
//
// (c) 1999-2001 PAL Core Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.statistics;

import pal.math.*;
import pal.misc.*;
import pal.io.*;

import java.io.*;

/**
 * Kishino-Hasegawa-(Templeton)-Test (1989, 1983) to
 * compare a set of evolutionary hypotheses
 *
 * @version $Id: KishinoHasegawaTest.java,v 1.5 2001/07/13 14:39:13 korbinian Exp $
 * 
 * @author Korbinian Strimmer
 */
public class KishinoHasegawaTest implements Report
{
	//
	// Public stuff
	//

	/**
	 * number of maximum likelihood hypothesis
	 */ 	
	public int bestH;
	
	/**
	 * log-likelihood difference to maximum likelihood hypothesis
	 */ 	
	public double[] delta;
	
	/**
	 * estimated error of log-likelihood differences
	 */ 	
	public double[] deltaSE;
	
	/**
	 * corresponding p-value (two-sided test on normal distribution)
	 */ 	
	public double[] pval;

	/** 
	 * Compare all given hypotheses to the best (ML) hypothesis
	 * and store results in public arrays delta, deltaSE, pval
	 * (which will automatically be created by this procedure).
	 *
	 * @param  sLogL       log-likelihoods of each site
	 */	
	public void compare(double[][] sLogL)
	{
		khtest(sLogL, null);
	}

	/** 
	 * Compare all given hypotheses to the best (ML) hypothesis
	 * and store results in public arrays delta, deltaSE, pval
	 * (which will automatically be created by this procedure).
	 *
	 * @param  pLogL       log-likelihoods of each pattern
	 * @param  alias       map of patterns to sites in sequence
	 */	
	public void compare(double[][] pLogL, int[] alias)
	{
		khtest(pLogL, alias);
	}

	public void report(PrintWriter out)
	{
		FormattedOutput fo = FormattedOutput.getInstance();
		out.println("KISHINO-HASEGAWA TEST:");
		out.println();
		out.println("tree\tdeltaL\tS.E.\tpval (two-sided)");
		out.println("----------------------------------------");
		for (int i = 0; i < pval.length; i++)
		{
			out.print((i+1) + "\t");
			fo.displayDecimal(out, delta[i], 2);
			out.print("\t");
			fo.displayDecimal(out, deltaSE[i], 2);
			out.print("\t");
			fo.displayDecimal(out, pval[i], 4);
			if (pval[i] < 0.05)
			{
				out.println(" **");
			}
			else
			{
				out.println();
			}
		}
		out.println();
		out.println("** indicates a tree that is significantly worse than the ML tree (5% level)");
	}

	//
	// Private stuff
	//
		
	private void khtest(double[][] pLogL, int[] alias)
	{
		// number of hypotheses 
		int numH = pLogL.length;

		// allocate memory for results
		delta = new double[numH];
		deltaSE = new double[numH];
		pval = new double[numH];
		
		// number of sites
		// if alias==null assume one-to-one mapping of sites and patterns
		int numSites;
		if (alias == null)
		{
			numSites = pLogL[0].length;
		}
		else
		{
			numSites = alias.length;
		}
		
		// log likelihood of each hypothesis
		double[] logL = new double[numH];
		for (int i = 0; i < numSites; i++)
		{
			int p;
			if (alias == null)
			{
				p = i;
			}
			else
			{
				p = alias[i];
			}
			
			for (int j = 0; j < numH; j++)
			{
				logL[j] += pLogL[j][p];
			}
		}

		// find maximum-likelihood hypothesis
		bestH = 0;
		double maxLogL = logL[0];
		for (int i = 1; i < numH; i++)
		{
			if (logL[i] > maxLogL)
			{
				bestH = i;
				maxLogL = logL[i];
			}
		}
				
		// compute log-likelihood differences to best hypothesis
		for (int i = 0; i < numH; i++)
		{
			delta[i] = logL[bestH]-logL[i];
		}
		
		// estimate standard error of log-likelihood differences
		for (int i = 0; i < numH; i++)
		{
			double mean = delta[i]/numSites;
			double var = 0.0;
			for (int j = 0; j < numSites; j++)
			{
				int p;
				if (alias == null)
				{
					p = j;
				}
				else
				{
					p = alias[j];
				}
				
				double diff = pLogL[bestH][p]-pLogL[i][p]-mean;
				var += diff*diff;
			}
			
			deltaSE[i] = Math.sqrt(numSites*var/(numSites-1));
		}
		
		// compute corresponding p-values
		for (int i = 0; i < numH; i++)
		{
			pval[i] = ErrorFunction.erfc(delta[i]/deltaSE[i]/Math.sqrt(2.0));
		}
		
		// free memory
		logL = null;
	}
}


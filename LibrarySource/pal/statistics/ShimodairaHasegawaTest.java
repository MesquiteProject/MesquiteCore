// ShimodairaHasegawaTest.java
//
// (c) 1999-2001 PAL Core Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.statistics;

import pal.math.*;
import pal.io.*;
import pal.misc.*;

import java.io.*;


/**
 * Shimodaira-Hasegawa-Test (1999) to
 * compare a set of evolutionary hypotheses
 *
 * @version $Id: ShimodairaHasegawaTest.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class ShimodairaHasegawaTest implements Report
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
	 * corresponding p-value
	 */ 	
	public double[] pval;

	/**
	 * number of bootstrap replicates
	 */ 	
	public int numBootstraps;


	/** 
	 * Compare all given hypotheses to the best (ML) hypothesis
	 * and store results in public arrays delta, pval
	 * (which will automatically be created by this procedure).
	 *
	 * @param  sLogL       log-likelihoods of each site
	 * @param  numBoot     number of bootstraps
	 */	
	public void compare(double[][] sLogL, int numBoot)
	{
		shtest(sLogL, null, numBoot);
	}

	/** 
	 * Compare all given hypotheses to the best (ML) hypothesis
	 * and store results in public arrays delta, pval
	 * (which will automatically be created by this procedure).
	 *
	 * @param  pLogL       log-likelihoods of each pattern
	 * @param  alias       map of patterns to sites in sequence
	 * @param  numBoot     number of bootstraps
	 */	
	public void compare(double[][] pLogL, int[] alias, int numBoot)
	{
		shtest(pLogL, alias, numBoot);
	}

	public void report(PrintWriter out)
	{
		FormattedOutput fo = FormattedOutput.getInstance();
		out.println("SHIMODAIRA-HASEGAWA TEST (" + numBootstraps + " bootstraps):");
		out.println();
		out.println("tree\tdeltaL\tpval");
		out.println("--------------------------");

		for (int i = 0; i < pval.length; i++)
		{
			out.print((i+1) + "\t");
			fo.displayDecimal(out, delta[i], 2);
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
		
	private void shtest(double[][] pLogL, int[] alias,  int numBoot)
	{
		// number of hypothesis 
		int numH = pLogL.length;
		
		// number of bootstrap replicates
		numBootstraps = numBoot;
		
		// allocate memory for results
		delta = new double[numH];
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
		
		// allocate temporary memory for resampling procedure
		double[][] rs = new double[numH][numBoot];
		
		// Resample data
		MersenneTwisterFast mt = new MersenneTwisterFast();
		for (int i = 0; i < numBoot; i++)
		{
			for (int j = 0; j < numSites; j++)
			{
				int s = mt.nextInt(numSites);
				
				int p;
				if (alias == null)
				{
					p = s;
				}
				else
				{
					p = alias[s];
				}
				
				for (int k = 0; k < numH; k++)
				{
					rs[k][i] += pLogL[k][p];
				}
			}
		}  
		
		// center resampled log-likelihoods
		for (int i = 0; i < numH; i++)
		{
			double m = DiscreteStatistics.mean(rs[i]);
			
			for (int j = 0; j < numBoot; j++)
			{
				rs[i][j] = rs[i][j]-m;
			}
		}
		
		// compute resampled log-likelihood differences
		for (int i = 0; i < numBoot; i++)
		{
			double max = findMaxInColumn(rs, i);
						
			for (int j = 0; j < numH; j++)
			{
				rs[j][i] = max - rs[j][i];
			}
		}
			
		// compute p-values for each hypothesis
		for (int i = 0; i < numH; i++)
		{
			int count = 0;
			for (int j = 0; j < numBoot; j++)
			{
				if (rs[i][j] >= delta[i])
				{
					count++;
				}
			}
			
			pval[i] = (double) count/(double) numBoot;
		}
		
		// free memory
		rs = null;
		logL = null;
	}
	
	private double findMaxInColumn(double[][] array, int column)
	{
		int len = array.length;
		
		int best = 0;
		double max = array[0][column];
		for (int i = 1; i < len; i++)
		{
			if (array[i][column] > max)
			{
				best = i;
				max = array[i][column];
			}
		}
		
		return max;
	}
}


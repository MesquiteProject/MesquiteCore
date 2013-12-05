// SkylinePlot.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

 
package pal.coalescent;

import pal.tree.*;
import pal.misc.*;
import pal.math.*;
import pal.io.*;
import pal.statistics.*;

import java.io.*;

/**
 * Skyline plot derived from a strictly bifurcating tree
 * or a coalescent interval.
 *
 * This class provides the "classic" skyline plot method by
 * Pybus, Rambaut and Harvey .2000. Genetics 155:1429-1437, as well
 * as the "generalized" skyline plot method  described in
 * Strimmer and Pybus. 2001. MBE submitted.
 * 
 * @version $Id: SkylinePlot.java,v 1.16 2001/07/11 13:51:16 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class SkylinePlot implements Report, Units, Serializable
{
	//
	// Public stuff
	//
	

	/**
	 * Construct skyline plot from tree
	 *
	 * @param epsilon smoothing parameter (if set < 0 then epsilon will be optimized)
	 */
	public SkylinePlot(Tree tree, double epsilon)
	{
		this(IntervalsExtractor.extractFromClockTree(tree), epsilon);
	}


	/**
	 * Construct skyline plot from given coalescent intervals
	 * 
	 * @param epsilon smoothing parameter (if set < 0 then epsilon will be optimized)
	 */
	public SkylinePlot(CoalescentIntervals ci, double epsilon)
	{
		if (!ci.isBinaryCoalescent())
		{
			throw new IllegalArgumentException("All coalescent intervals must contain only a single coalescent");
		}
		
		fo = FormattedOutput.getInstance();
		
		size = ci.getIntervalCount();
		
		this.ci = ci;
		
		// population size in each coalescent interval
		populationSize = new double[size];
				
		// cumulative interval sizes
		cis = new double[size];
		
		maxTime = 0.0;
		for (int i = 0; i < size; i++)
		{
			cis[i] = maxTime;
			maxTime += ci.getInterval(i);
		}
		
		if (epsilon == 0.0)
		{
			/* init with classic skyline plot */
			computeClassic();
		}
		else if (epsilon > 0.0)
		{
			/* init with generalized skyline plot */
			computeGeneralized(epsilon);
		}
		else
		{
			// find optimal generalized skyline plot
			optimize();
		}
	}

	
	public String toString()
	{		
		OutputTarget out = OutputTarget.openString();
		report(out);
		out.close();
		
		return out.getString();
	}
	
	public void report(PrintWriter out)
	{
		out.println("Skyline Plot");
		out.println();
		out.print("Smoothing parameter epsilon = " + eps + " ");
		if (eps == 0.0) out.println("(classic skyline plot)");
		else out.println("(generalized skyline plot)");
		
		out.print("Unit of time: ");
		if (ci.getUnits() == GENERATIONS)
		{
			out.print("generations");
		}
		else
		{
			out.print("expected substitutions");
		}
		out.println();
		
		printIntervals(out);
		
		out.println();
		out.println("For each composite interval the first and the last simple interval is given.");
		out.println();
		out.println("log L = " + getLogLikelihood());
		out.println("Number of intervals: " + size);
		out.println("Number of composite intervals:" + params);
		if (params > size-2)
		out.println("log L(AICC) not available");
		else
		out.println("log L(AICC) = " + getAICC());
	}

	private void printIntervals(PrintWriter out)
	{
		out.println("Int.\tTime\tEstimated N(t)");
		double total = 0.0;
		for (int i = 0; i < size; i++)
		{
			double m = populationSize[i];
			
			printLine(out, i, total, m);
			total += ci.getInterval(i);
			
			int j;
			for (j = i+1; j < size; j++)
			{
				if (populationSize[j] != m) break;
			}
			i=j-1;
			
			printLine(out, i, total, m);
		}
	}


	private void printLine(PrintWriter out, int i, double total, double m)
	{
			out.print((size-i) + "\t");
			fo.displayDecimal(out, total, 4);
			out.print("\t");
			fo.displayDecimal(out, m, 4);
			out.println();
	}



	/**
	 * Compute classic skyline plot
	 */
	public void computeClassic()
	{
		for (int i = 0; i < size; i++)
		{
			double w = ci.getInterval(i);
			double n = ci.getNumLineages(i);
			
			populationSize[i] = w * (n*(n-1))/2.0 ;
		}
		
		params = size;
		eps = 0.0;
	}

	/**
	 * Compute generalized skyline plot
	 */
	public void computeGeneralized(double epsilon)
	{
		params = 0;
		double cw = 0; //cumulative w
		
		for (int i = 0; i < size; i++)
		{
			double n = ci.getNumLineages(i);
			
			double w = ci.getInterval(i);
			
			int start = i;
			int k = 1;
			while (w < epsilon && i < size-1)
			{
				i++;
				k++;
				w += ci.getInterval(i);
				
				//System.out.println(ci.getInterval(i));
			} 
			
			//System.out.println("w=" + w + " k=" + k + "  i=" + i);
			
			// if remainder is smaller than epsilon
			// continue pooling until the end
			if (maxTime - cw - w < epsilon) 
			{				
				for (int j = i+1; j < size; j++)
				{
					i++;
					k++;
					w += ci.getInterval(i);
				}

			}
			
			double m = w * (n*(n-k))/(2.0*k);
			
			// assign the same pop.size to all sub intervals
			for (int j = start; j < start+k; j++)
			{
				populationSize[j] = m;
			}
			params++;
			cw += w;
		}
		eps = epsilon;
	}

	/**
	 * Optimize generalized skyline plot
	 */
	public void optimize()
	{
		// this is the naive way of doing this ...
		
		double besteps = getMaxTime();
		computeGeneralized(besteps);
		double bestaicc = getAICC();
		
		int GRID = 1000;
		double delta = besteps/GRID;
		
		double MINEPS = 1e-6;
		// Why MINEPS?
		// Because most "clock-like" trees are not properly
		// clock-like for a variety of reasons, i.e. the heights
		// of the tips are not exactly zero.
		
		
		
		eps = eps - delta;
		while(eps > MINEPS)
		{
			computeGeneralized(eps);
			double aicc = getAICC();
			
			if (aicc > bestaicc && params < size-1)
			{
				besteps = eps;
				bestaicc = aicc;
			}
			eps = eps - delta;
		}
		
		computeGeneralized(besteps);
	}

	/**
	 * Compute log-likelihood
	 */
	public double getLogLikelihood()
	{
		double logL = 0.0;
		
		for (int i = 0; i < size; i++)
		{
			double w = ci.getInterval(i);
			double m = populationSize[i];
			
			double n = ci.getNumLineages(i);
			double nc2 = n*(n-1.0)/2.0;
			
			logL += Math.log(nc2/m) - w*nc2/m   ;
		}
		
		return logL;
	}

	/**
	 * Compute AICC-corrected log-likelihood
	 */
	public double getAICC()
	{
		double logL = getLogLikelihood();
	
		return PenalizedLikelihood.AICC(logL, params, size);
	}

	/**
	 * Find interval corresponding to a specific time
	 */
	public double findInterval(double time)
	{
		if (time < 0) throw new IllegalArgumentException("Negative values for time are not allowed");
		
		for (int i = 0; i < size-1; i++)
		{
			if (time >= cis[i] && time < cis[i+1]) return i;
		}
		
		return size-1;
	}
	 
	/**
	 * Returns the largest value of time defined in this plot 
	 * (= maximum value for epsilon)
	 */
	public double getMaxTime()
	{
		return maxTime;
	}


	/**
	 * Returns the largest estimate of population size.
	 */
	public double getMaxPopulationSize() {
		double max = 0.0;
		for (int i = 0; i < size; i++) {
			if (populationSize[i] > max) {
				max = populationSize[i];
			}
		}

		return max;
	}

	/**
	 * Returns the coalescent intervals in this skyline plot.
	 */
	public CoalescentIntervals getIntervals() {
		return ci;
	}

	/**
	 * Returns the number of intervals in this skyline plot.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Returns the number of composite intervals (=number of parameters).
	 */
	public int getParameterCount() {
		return params;
	}

	/**
	 * Returns epsilon
	 */
	public double getEpsilon() {
		return eps;
	}
	
	/**
	 * Returns the population size in interval i.
	 */
	public double getPopulationSize(int i) {
		return populationSize[i];
	}

	/**
	 * Returns unit of time.
	 */
	public int getUnits()
	{
		return ci.getUnits();
	}


	// private
	
	private CoalescentIntervals ci;
	private FormattedOutput fo;
	private int size;
	private double maxTime;
	private double eps;
	private int params;
	
	/** cummulative interval sizes */
	private double[] cis;
	
	/** estimated population size in a coalescent interval */
	private double[] populationSize; 


}


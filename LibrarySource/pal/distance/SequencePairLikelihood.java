// SequencePairLikelihood.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.distance;

import pal.alignment.*;
import pal.substmodel.*;
import pal.misc.*;
import pal.math.*;


/**
 * computation of the (negative) log-likelihood for a pair of sequences
 *
 * @version $Id: SequencePairLikelihood.java,v 1.5 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class SequencePairLikelihood implements UnivariateFunction
{
	/**
	 * initialisation
	 *
	 * @param sp site pattern
	 * @param m model of substitution
	 */
	public SequencePairLikelihood(SitePattern sp, SubstitutionModel m)
	{
		updateSitePattern(sp);		
		updateModel(m);
	}
	
	/**
	 * update model of substitution
	 *
	 * @param model of substitution
	 */
	public void updateModel(SubstitutionModel m)
	{
		model = m;
		numRates = model.rateDistribution.numRates;
		rateProb = model.rateDistribution.probability;
		rateMatrix = model.rateMatrix;
	}

	/**
	 * update site pattern 
	 *
	 * @param site pattern
	 */
	public void updateSitePattern(SitePattern sp)
	{
		sitePattern = sp;
		
		numPatterns = sp.numPatterns;
		numSites = sp.getSiteCount();
		numStates = sp.getDataType().getNumStates();
		weight = sp.weight;
	}

	
	/**
	 * specification of two sequences in the given alignment
	 * 
	 * @param s1 number of first sequence
	 * @param s2 number of second sequence
	 */
	public void setSequences(int s1, int s2)
	{
		setSequences(sitePattern.pattern[s1], sitePattern.pattern[s2]);
	}

	/**
	 * specification of two sequences (not necessarily in the given
	 * alignment but with the same weights in the site pattern)
	 * 
	 * @param s1 site pattern of first sequence
	 * @param s2 site pattern of second sequence
	 */
	public void setSequences(byte[] s1, byte[] s2)
	{
		seqPat1 = s1;
		seqPat2 = s2;
	}
	
	/**
	 * compute (negative) log-likelihood for a given distance
	 * between the two sequences
	 *
	 * @param arc expected distance
	 *
	 * @return negative log-likelihood
	 */
	public double evaluate(double arc)
	{
		model.setDistance(arc);
		
		double loglkl = 0;
		for (int i = 0; i < numPatterns; i++)
		{
			double sumprob = 0;
			for (int r = 0; r < numRates; r++)
			{
				sumprob += rateProb[r]*probConfig(r, seqPat1[i], seqPat2[i]);
			}
			loglkl += weight[i]*Math.log(sumprob);
		}
		
		return -loglkl;
	}
	
	public double getLowerBound()
	{
		return BranchLimits.MINARC;
	}
	
	public double getUpperBound()
	{
		return BranchLimits.MAXARC;
	}
		
	
	//
	// Private stuff
	//
	
	private SubstitutionModel model;
	private RateMatrix rateMatrix;
	private SitePattern sitePattern;	
	private int numPatterns;
	private int numSites;
	private int numStates;
	private int numRates;
	private int[] weight;
	private double[] rateProb;
	private byte[] seqPat1;
	private byte[] seqPat2;
	
	private double probConfig(int r, int i, int j)
	{
		double p;
		
		if (i == numStates && j == numStates)
		{
			p = 1.0;
		}
		else if (i == numStates)
		{
			p = rateMatrix.getEqulibriumFrequency(j);
		}
		else if (j == numStates)
		{
			p = rateMatrix.getEqulibriumFrequency(i);
		}
		else
		{
			p = rateMatrix.getEqulibriumFrequency(i)*model.transProb(r, i, j);
		}
		
		return p;
	}	
}


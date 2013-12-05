// SubstitutionModel.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.math.*;

import java.io.*;


/**
 * <b>model of sequence substitution (rate matrix + rate variation)</b>.
 * provides a convenient interface for the computation of transition probabilities
 *
 * @version $Id: SubstitutionModel.java,v 1.9 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class SubstitutionModel implements Parameterized, Report, java.io.Serializable, Cloneable {
	//
	// Public stuff
	//
	
	/** rate matrix */
	public RateMatrix rateMatrix;
	
	/** rate distribution */
	public RateDistribution rateDistribution;
	
	/** dimension of rate matrix */	
	public int dimension;
	
	/** number of categories in discrete rate distribution */
	public int numRates;

	/**
	 * constructor 1 (uniform rate distribution)
	 *
	 * @param rmat rate matrix
	 */
	public SubstitutionModel(RateMatrix rmat)
	{
		this(rmat, new UniformRate());
	}

	/**
	 * constructor 2 (gamma rate distribution)
	 *
	 * @param rmat rate matrix
	 * @param n number of rate categories
	 * @param a shape parameter
	 */
	public SubstitutionModel(RateMatrix rmat, int n, double a)
	{
		this(rmat, new GammaRates(n, a));
	}
	
	/**
	 * constructor 3 (arbitrary rate distribution)
	 *
	 * @param rmat rate matrix
	 * @param rdist rate distribution
	 */
	public SubstitutionModel(RateMatrix rmat, RateDistribution rdist)
	{
		dimension = rmat.getDimension();
		numRates = rdist.numRates;
		
		rateMatrix = rmat;
		rateDistribution = rdist;
		
		tpm = rmat.getTransitionProbability();
		probs = new double[numRates][dimension][dimension];
		
		numRmatParams = rmat.getNumParameters();
		numRdistParams = rdist.getNumParameters();
		numParams = numRmatParams + numRdistParams;
	}
	
	/**
	 * Return a copy of this substitution model.
	 */
	public SubstitutionModel(SubstitutionModel model) {
	
		dimension = model.rateMatrix.getDimension();
		numRates = model.rateDistribution.numRates;
		numRmatParams = model.rateMatrix.getNumParameters();
		numRdistParams = model.rateDistribution.getNumParameters();
		
		rateMatrix = (RateMatrix)model.rateMatrix.clone();
		rateDistribution = 
			(RateDistribution)model.rateDistribution.clone();
		
		tpm =  rateMatrix.getTransitionProbability();
		probs = new double[numRates][dimension][dimension];
		
		numParams = numRmatParams + numRdistParams;
	}

	// interface Report
	public void report(PrintWriter out)
	{
		rateMatrix.report(out);
		out.println();
		rateDistribution.report(out);
	}

	/**
	 * Return string representation of substitution model.
	 */
	public String toString() {
		StringWriter sw = new StringWriter();
		report(new PrintWriter(sw));
		return sw.toString();
	}
	
	// interface Parameterized

	public int getNumParameters()
	{
		return numParams;
	}
	
	public void setParameter(double param, int n)
	{
		if (n < numRmatParams)
		{
			rateMatrix.setParameter(param, n);
			tpm = rateMatrix.getTransitionProbability(tpm);
		}
		else
		{
			rateDistribution.setParameter(param, n-numRmatParams);
		}		
	}

	public double getParameter(int n)
	{
		if (n < numRmatParams)
		{
			return rateMatrix.getParameter(n);
		}
		else
		{
			return rateDistribution.getParameter(n-numRmatParams);
		}		
	}

	public void setParameterSE(double paramSE, int n)
	{
		if (n < numRmatParams)
		{
			rateMatrix.setParameterSE(paramSE, n);
		}
		else
		{
			rateDistribution.setParameterSE(paramSE, n-numRmatParams);
		}		
	}

	public double getLowerLimit(int n)
	{
		if (n < numRmatParams)
		{
			return rateMatrix.getLowerLimit(n);
		}
		else
		{
			return rateDistribution.getLowerLimit(n-numRmatParams);
		}		
	}
	
	public double getUpperLimit(int n)
	{
		if (n < numRmatParams)
		{
			return rateMatrix.getUpperLimit(n);
		}
		else
		{
			return rateDistribution.getUpperLimit(n-numRmatParams);
		}		
	}
	
	public double getDefaultValue(int n)
	{
		if (n < numRmatParams)
		{
			return rateMatrix.getDefaultValue(n);
		}
		else
		{
			return rateDistribution.getDefaultValue(n-numRmatParams);
		}		
	}


	/**
	 * set distance and corresponding computation transition probabilities
	 *
	 * @param k distance
	 */
	public void setDistance(double k)
	{
		for (int r = 0; r < numRates; r++)
		{
			double kk = k*rateDistribution.rate[r];
			
			if (kk < BranchLimits.MINARC)
			{
				kk = BranchLimits.MINARC;
			}
			if (kk > BranchLimits.MAXARC)
			{
				kk = BranchLimits.MAXARC;
			}
		
			tpm.setDistance(kk);
			
			for (int i = 0; i < dimension; i++)
			{
				for (int j = 0; j < dimension; j++)
				{
					probs[r][i][j] = tpm.getTransitionProbability(i,j);
				}
			}
		}
	}

	/**
	 * get transition probability for the preselected model and
	 * the previously specified distance
	 *
	 * @param r rate category
	 * @param i start state
	 * @param j end state
	 *
	 * @return transition probability
	 */
	public double transProb(int r, int i, int j)
	{
		return probs[r][i][j];
	}

	public boolean isSimpleJukesCantor() {
		
		// if more then one rate the not a simple jukes cantor
		if (numRates > 1) return false;
		
		// NOT if frequencies are different
		double[] frequencies = rateMatrix.getEqulibriumFrequencies();
		double freq = frequencies[0];
		for (int i = 1; i < frequencies.length; i++) {
			if (frequencies[i] != freq) {
				return false;	
			}
		}

		double[][] relRates = rateMatrix.getRelativeRates();
		// NOT if rates are different
		double rate = relRates[0][1];
		for (int i = 0; i < relRates.length; i++) {
			for (int j = 0; j < relRates[i].length; j++) {
				if ((i != j) && (relRates[i][j] != rate)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	//
	// Private stuff
	//
	
	private TransitionProbability tpm;
	private double[][][] probs;
	private int numRmatParams, numRdistParams, numParams;
}


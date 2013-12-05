// DemographicLikelihoodValue.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

// - partial likelihoods need a lot of memory storage
//   memory usage could be opimized by working in a single site


package pal.eval;

import pal.coalescent.*;
import pal.alignment.*;

/**
 * Estimates the likelihood for a tree using a specified
 * model of sequence evolution and a sequence alignment and
 * a specific demographic model as a prior on coalescent intervals.
 *
 * <em>Must be used in conjunction with DemographicClockTree! </em>
 *
 * @version $Id: DemographicLikelihoodValue.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class DemographicLikelihoodValue extends LikelihoodValue
{
	//
	// Public stuff
	//	

	/**
	 * Parameter taking a site pattern.
	 */
	public DemographicLikelihoodValue(SitePattern sp) {
		super(sp);
	}


	/**
	 * compute log-likelihood
	 * for current branch lengths and model
	 *
	 * return negative log-likelihood
	 */
	public double compute()
	{		
		super.compute();

		logL += ((DemographicTree)tree).computeDemoLogLikelihood();

		return -logL;
	}
}



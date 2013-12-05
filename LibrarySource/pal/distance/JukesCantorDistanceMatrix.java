// JukesCantorDistanceMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.distance;

import pal.alignment.*;
import pal.substmodel.*;
import pal.misc.*;


/**
 * compute jukes-cantor corrected distance matrix
 *
 * @version $Id: JukesCantorDistanceMatrix.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
public class JukesCantorDistanceMatrix extends DistanceMatrix
{
	//
	// Public stuff
	//

	/**
	 * compute jukes-cantor corrected distances
	 * (assumes nucleotides as underlying data)
	 *
	 * @param dist distance matrix
	 */
	public JukesCantorDistanceMatrix(DistanceMatrix dist)
	{
		this(dist, 4);
	}


	/**
	 * compute jukes-cantor corrected distances
	 *
	 * @param dist distance matrix
	 * @param numStates number of states of underlying data
	 */
	public JukesCantorDistanceMatrix(DistanceMatrix dist, int numStates)
	{
		numSeqs = dist.numSeqs;
		idGroup = dist.getIdGroup();
		distance = new double[numSeqs][numSeqs];
		
		obsDistance = dist.distance;
		
		double n = numStates;

		const1 = (n-1)/n;
		const2 = n/(n-1);

		computeDistances();
	}


	/**
	 * compute jukes-cantor corrected distances
	 *
	 * @param alignment Alignment
	 */
	public JukesCantorDistanceMatrix(Alignment alignment)
	{
		this(new SitePattern(alignment));
	}

	/**
	 * compute jukes-cantor corrected distances
	 *
	 * @param sitePattern SitePattern
	 */
	public JukesCantorDistanceMatrix(SitePattern sitePattern)
	{
		this(	new AlignmentDistanceMatrix(sitePattern),
			sitePattern.getDataType().getNumStates());
	}


	//
	// Private stuff
	//
	
	private double[][] obsDistance;

	//used in correction formula
	private double const1, const2;
	
	private void computeDistances()
	{
		for (int i = 0; i < numSeqs-1; i++)
		{
			distance[i][i] = 0.0;
			for (int j = i+1; j < numSeqs; j++)
			{
				distance[i][j] = distance[j][i] = jccorrection(obsDistance[i][j]);
			}
		}
	}

    
	private double jccorrection(double obsdist)
	{
		if (obsdist == 0.0) return 0.0;
	
		if (obsdist >= const1)
		{
			return BranchLimits.MAXARC;
		} 
        
		double expDist = -const1 * Math.log(1.0 - (const2 * obsdist));

		if (expDist < BranchLimits.MAXARC)
		{
			return expDist;
		}
		else
		{
			return BranchLimits.MAXARC;
		}
	}
}


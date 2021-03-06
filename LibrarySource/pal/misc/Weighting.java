// Weighting.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * interface for classes which provide weighting mechanisms
 *
 * @version $Id: Weighting.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Matthew Goode
 */
public interface Weighting extends Parameterized, Report, java.io.Serializable {

	/** @return an array of weights.
		*	@note Users of this array should not alter it in anyway as it may be the array that is used internally by a weighting.
		*/
	double[] getWeights();
	/** For people who don't like casting...*/
	Weighting getWeightingCopy();
}


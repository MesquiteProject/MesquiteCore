// TransistionProbability.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

import pal.math.*;


/**
 * For objects that represent a source of transition probabilities
 *
 * TransitionProbability.java,v 1.3 2000/08/08 22:58:29 alexi Exp $
 *
 * @author Matthew Goode
 */
public interface TransitionProbability extends Cloneable, java.io.Serializable {

	/**
	 * compute transition probabilities for a expected distance
	 * using the prespecified rate matrix
	 *
	 * @param arc expected distance
	 */
	void setDistance(double arc);
	/**
	 * compute transition probabilities for a expected time span
	 * using the prespecified rate matrix
	 *
	 * @param start start time
	 * @param end end time
	 */
	void setTime(double start, double end);

	/**
		* Returns the transition probability for changing from
		* startState into endState
		* @param startState - the starting state
		* @param endState - the resulting state
		*/
	double getTransitionProbability(int startState, int endState);

	int getDimension();
}


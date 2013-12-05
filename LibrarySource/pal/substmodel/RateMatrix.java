// RateMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.io.*;
import pal.datatype.*;

import java.io.*;


/**
 * abstract base class for all rate matrices
 *
 * @version $Id: RateMatrix.java,v 1.14 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public interface RateMatrix
	extends Parameterized, Report, Cloneable, Serializable {

	/**
	 * get numerical code describing the data type
	 *
	 * @return integer code identifying a data type
	 */
	int getTypeID();

	int getDimension();

	/**
		* @return stationary frequencies (sum = 1.0)
		*/
	double[] getEqulibriumFrequencies();

	/**
		* @return stationary frequencie (sum = 1.0) for ith state
		* Preferred method for infrequent use.
		*/
	double getEqulibriumFrequency(int i);

	/**
	 * get numerical code describing the model type
	 *
	 * @return integer code identifying a substitution model
	 */
	int getModelID();



	/** For those that like Getter/Setters */
	DataType getDataType();

	/**
	 * @return rate matrix (transition: from 1st index to 2nd index)
	 * @note should be deprecated. Try not to use!
	 */
	double[][] getRelativeRates();

	
	/**
		Return a transitionProbability calculting thing appropriate to this ratematrix (generally returns SimpleRateMatrix())
	*/
	TransitionProbability getTransitionProbability();

	/**
		Return a matrix exponential appropriate to this ratematrix (generally returns SimpleRateMatrix())
		given an old matrix exponential (may reuse old matrix exponential)
	*/
	TransitionProbability getTransitionProbability(TransitionProbability old);

	// interface Report (remains abstract)

	// interface Parameterized (remains abstract)

	Object clone();

}


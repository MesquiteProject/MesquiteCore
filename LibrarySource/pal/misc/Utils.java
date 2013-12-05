// Utils.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

import pal.math.*;

/**
 * Provides some miscellaneous methods.
 *
 * @version $Id: Utils.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Matthew Goode
 */
public class Utils {

	/** Clones an array of doubles
		* @return null if input is null, otherwise return complete copy.
		*/
	public static final double[] getCopy(double[] array) {
		if(array == null) {
			return null;
		}
		double[] copy = new double[array.length];
		for(int i = 0 ; i < copy.length ; i++) {
			copy[i] = array[i];
		}
		return copy;
	}

	/** A simple toString method for an array of doubles.
		* No fancy formating.
		* Puts spaces between each value
		*/
	public static final String toString(double[] array) {
		StringBuffer sb = new StringBuffer(array.length*7);
		for(int i = 0 ; i < array.length ; i++) {
			sb.append(array[i]);
			sb.append(' ');
		}
		return sb.toString();
	}

	/** Creates an interface between a parameterised object to allow it to act as
			a multivariate minimum.
	*/
	public static final MultivariateFunction combineMultivariateFunction(MultivariateFunction base, Parameterized[] additionalParameters) {
		return new U_CombineMultiParam(base,additionalParameters);
	}
}

class U_CombineMultiParam implements MultivariateFunction {
	Parameterized[] additionalParameters_;
	MultivariateFunction base_;

	double[] baseArgumentStorage_;
	double[] lowerBounds_;
	double[] upperBounds_;

	public U_CombineMultiParam(MultivariateFunction base, Parameterized[] additionalParameters) {
		this.additionalParameters_ = additionalParameters;
		this.base_ = base;
		int numberOfArguments = base_.getNumArguments();
		baseArgumentStorage_ = new double[numberOfArguments];

		for(int i = 0 ; i < additionalParameters.length ; i++) {
			numberOfArguments+=additionalParameters[i].getNumParameters();
		}
		lowerBounds_ = new double[numberOfArguments];
		upperBounds_ = new double[numberOfArguments];
		int argumentNumber = 0;
		for(int i = 0 ; i < base_.getNumArguments() ; i++) {
			lowerBounds_[argumentNumber] = base_.getLowerBound(i);
			upperBounds_[argumentNumber] = base_.getUpperBound(i);
			argumentNumber++;
		}
		for(int i = 0 ; i < additionalParameters.length ; i++) {
			for(int j = 0 ; j < additionalParameters[i].getNumParameters() ; j++) {
				lowerBounds_[argumentNumber] = additionalParameters[i].getLowerLimit(j);
				upperBounds_[argumentNumber] = additionalParameters[i].getUpperLimit(j);
				argumentNumber++;
			}
		}

	}


	public double evaluate(double[] argument) {
		int argumentNumber = 0;
		for(int i = 0 ; i < baseArgumentStorage_.length ; i++) {
			baseArgumentStorage_[i] = argument[argumentNumber];
			argumentNumber++;
		}
		for(int i = 0 ; i < additionalParameters_.length ; i++) {
			int numParam = additionalParameters_[i].getNumParameters();
			for(int j = 0 ; j < numParam ;  j++) {
				additionalParameters_[i].setParameter(argument[argumentNumber], j);
				argumentNumber++;
			}
		}
		return base_.evaluate(baseArgumentStorage_);
	}
	public int getNumArguments() {
		return lowerBounds_.length;
	}
	public double getLowerBound(int n) {
		return lowerBounds_[n];
	}

	public double getUpperBound(int n) {
		return upperBounds_[n];
	}
}


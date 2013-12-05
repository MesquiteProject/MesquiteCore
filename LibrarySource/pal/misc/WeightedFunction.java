// Product.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * A GeneralFucnction that multiplies all the input values.
 *
 * @version $Id: WeightedFunction.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Matthew Goode
 */

public class WeightedFunction implements GeneralFunction {
	GeneralFunction toWeight_;
	Weighting weighting_;
	double[] parameterStore_ = null;

	public WeightedFunction(GeneralFunction toWeight, Weighting weighting) {
		this.toWeight_ = toWeight;
		this.weighting_ = weighting;
	}

	protected WeightedFunction(WeightedFunction toCopy) {
		this.toWeight_ = toCopy.toWeight_.getGeneralFunctionCopy();
		this.weighting_ = toCopy.weighting_.getWeightingCopy();
		this.parameterStore_ = Utils.getCopy(toCopy.parameterStore_);
	}

	/** Assumes number of weights returned by weighting matches number of parameters (ignores additional parameters if not the case)
	*/
	public double compute(double[] parameters) {
		double[] weights = weighting_.getWeights();
		if(parameterStore_ == null || parameterStore_.length != weights.length) {
			parameterStore_ = new double[weights.length];
		}
		for(int i = 0 ; i < parameterStore_.length ; i++) {
			parameterStore_[i] = weights[i]*parameters[i];
		}
		return toWeight_.compute(parameterStore_);
	}

	public GeneralFunction getGeneralFunctionCopy() {
		return new WeightedFunction(this);
	}
} 


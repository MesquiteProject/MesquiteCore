// Weighting.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;
import java.io.*;

/**
 * A weighting for n values. Has n-1 parameters. Weightings sum to 1.
 *
 * @version $Id: SumToOneWeighting.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Matthew Goode
 */

public class SumToOneWeighting implements Weighting {
	double[] parameters_; /** The parameters */
	double[] parametersSE_; /** The parameters' SE*/
	double[] defaultValues_; /** The default values for the parameters */
	double[] weightings_; /** The weightings (dependent on p0, p1) */
	boolean rigidParameters_;

	/**
		* @param defaultValues The default, and initial, parameters (n is determined by the number of parameters + 1)
		*/
	public SumToOneWeighting(double[] defaultParameters) {
		this(defaultParameters,defaultParameters.length+1);
	}
	/**
		* @param defaultValues The default, and initial, parameters and n the number of weights (ignores extra parameters)
		* @param n The number of weights (not the number of parameters!)
		* @note Parameters are not rigid and can be adjusted.
		*/
	public SumToOneWeighting(double[] defaultParameters, int n) {
		this(defaultParameters,n,false);
	}
	/**
		* @param defaultValues The default, and initial, parameters and n the number of weights (ignores extra parameters)
		* @param n The number of weights (not the number of parameters!)
		* @param rigidParam If true than this weighting has not adjustable paramters (that is, is reported is as having zero parameters in the Paraterizable interface)
		*/
	public SumToOneWeighting(double[] defaultParameters, int n, boolean rigidParameters) {
		this.rigidParameters_ = rigidParameters;
		weightings_ = new double[n];
		defaultValues_ = new double[n-1];
		parameters_ = new double[n-1];
		parametersSE_ = new double[n-1];
		for(int i = 0 ; i < n ; i++) {
			defaultValues_[i] = defaultParameters[i];
			parameters_[i] = defaultParameters[i];
		}
		recalculateWeightings();

	}
	public SumToOneWeighting(SumToOneWeighting toCopy) {
		int n = toCopy.weightings_.length;
		weightings_ = new double[n];
		defaultValues_ = new double[n-1];
		parameters_ = new double[n-1];
		parametersSE_ = new double[n-1];
		for(int i = 0 ; i < (n-1) ; i++) {
			this.defaultValues_[i] = toCopy.defaultValues_[i];
			this.parameters_[i] = toCopy.parameters_[i];
			this.parametersSE_[i] = toCopy.parametersSE_[i];
		}
		recalculateWeightings();
	}

	public double getWeight(int weightNumber) {
		return weightings_[weightNumber];
	}

	public double[] getWeights() {
		return weightings_;
	}
	/** For people who don't like casting...*/
	public Weighting getWeightingCopy() {
		return new SumToOneWeighting(this);
	}
	/**
	 * get number of parameters
	 *
	 * @return number of parameters
	 */
	public int getNumParameters() {
		return (rigidParameters_ ? 0 : parameters_.length);
	}

	/** Calculate the weightings from the current parameter values */
	private void recalculateWeightings() {
    double total = 1;
		for(int i = 0 ; i < parameters_.length ; i++) {
			total+=parameters_[i];
		}
		for(int i = 0 ; i < parameters_.length ; i++) {
			weightings_[i] = parameters_[i]/total;
		}
		weightings_[parameters_.length] = 1/total;
	}

	/**
	 * set model parameter
	 *
	 * @param param  parameter value
	 * @param n  parameter number
	 */
	public void setParameter(double param, int n) {
		if(rigidParameters_) {
			throw new RuntimeException("Assertion error - Attempting to set rigid paramters");
		}
		parameters_[n] = param;
		recalculateWeightings();
	}

	/**
	 * get model parameter
	 *
	 * @param n  parameter number
	 *
	 * @return parameter value
	 */
	public double getParameter(int n) {
		return parameters_[n];
	}


	/**
	 * set standard errors for model parameter
	 *
	 * @param paramSE  standard error of parameter value
	 * @param n parameter number
	 */
	public void setParameterSE(double paramSE, int n) {
		parametersSE_[n] = paramSE;
	}


	/**
	 * get lower parameter limit
	 *
	 * @param n parameter number
	 *
	 * @return lower bound
	 */
	public double getLowerLimit(int n) {
		return 0;
	}

	/**
	 * get upper parameter limit
	 *
	 * @param n parameter number
	 *
	 * @return upper bound
	 */
	public double getUpperLimit(int n) {
		return Double.MAX_VALUE;
	}


	/**
	 * get default value of parameter
	 *
	 * @param n parameter number
	 *
	 * @return default value
	 */
	public double getDefaultValue(int n) {
		return defaultValues_[n];
	}

	public void report(PrintWriter out) {
		out.print("Sum To One Weighting");
		if(rigidParameters_) {
			out.println("(Rigid Parameters)");
		} else {
			out.println("");
		}
		out.print("Weights:");
		for(int i = 0 ; i < weightings_.length ; i++) {
			out.print(weightings_[i]+" ");
		}
		out.println();
		out.println("Parameters:");
		for(int i = 0 ; i < parameters_.length ; i++) {
			out.print(parameters_[i]+" ");
		}
		out.println();
		out.println("Parameters SE:");
		for(int i = 0 ; i < parametersSE_.length ; i++) {
			out.print(parametersSE_[i]+" ");
		}
	}
}


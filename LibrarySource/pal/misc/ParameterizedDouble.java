// ParameterizedDouble.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

import java.io.*;


/**
 * interface for a double that might be used as a parameter
 *
 * @version $Id: ParameterizedDouble.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Matthew Goode
 */
public class ParameterizedDouble implements Parameterized{
	double defaultValue_;
	double currentValue_;
	double minimumValue_;
	double maximumValue_;
	double se_;

	/** The default value is also the initial value.
	*/
	public ParameterizedDouble(double defaultValue, double minimumValue, double maximumValue) {
		this.currentValue_ = defaultValue;
		this.defaultValue_ = defaultValue;
		this.minimumValue_ = minimumValue;
		this.maximumValue_ = maximumValue;
	}

	/** Set the current value of this double */
	public final void setValue(double value) {
		this.currentValue_ = value;
	}

	/** Get the current value of this double */
	public final double getValue() {
   	return currentValue_;
	}

	public final double getLowerLimit() {
		return minimumValue_;
	}

	public final double getUpperLimit() {
		return maximumValue_;
	}

	public final double getDefaultValue() {
		return defaultValue_;
	}

	public final double getSE() {
		return se_;
	}

	public final void setSE(double value) {
		se_ = value;
	}



	/**
	 *
	 * @return 1
	 */
	public int getNumParameters() {
		return 1;
	}

	/**
	 * set model parameter
	 *
	 * @param param  parameter value
	 * @param n  parameter number (ignored)
	 */
	public void setParameter(double param, int n) {
		this.currentValue_ = param;
	}

	/**
	 * get model parameter
	 *
	 * @param n  parameter number
	 *
	 * @return parameter value
	 */
	public double getParameter(int n) {
   	return currentValue_;
	}


	/**
	 * set standard errors for model parameter
	 *
	 * @param paramSE  standard error of parameter value
	 * @param n parameter number
	 */
	public void setParameterSE(double paramSE, int n) {
		this.se_ = paramSE;
	}

	
	/**
	 * get lower parameter limit
	 *
	 * @param n parameter number
	 *
	 * @return lower bound
	 */
	public double getLowerLimit(int n) {
   	return minimumValue_;
	}

	/**
	 * get upper parameter limit
	 *
	 * @param n parameter number
	 *
	 * @return upper bound
	 */
	public double getUpperLimit(int n) {
   	return maximumValue_;
	}


	/**
	 * get default value of parameter
	 *
	 * @param n parameter number
	 *
	 * @return default value
	 */
	public double getDefaultValue(int n) {
		return defaultValue_;
	}
}


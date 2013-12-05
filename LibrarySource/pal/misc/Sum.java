// Sum.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * A GeneralFucnction that sums all the input values.
 *
 * @version $Id: Sum.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Matthew Goode
 */
 
public class Sum implements GeneralFunction {
	public double compute(double[] parameters) {
		double sum = 0.0;
		for(int i = 0 ; i < parameters.length ; i++) {
			sum += parameters[i];
		}
		return sum;
	}

	/** No actual cloning is performed (returns self), as no state is
			kept */
	public GeneralFunction getGeneralFunctionCopy() {
		return this;
	}
} 


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
 * @version $Id: Product.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Matthew Goode
 */

public class Product implements GeneralFunction {
	public double compute(double[] parameters) {
		double product = 1.0;
		for(int i = 0 ; i < parameters.length ; i++) {
			product *= parameters[i];
		}
		return product;
	}
	/** No actual cloning is performed (returns self), as no state is
		kept */
	public GeneralFunction getGeneralFunctionCopy() {
		return this;
	}
} 


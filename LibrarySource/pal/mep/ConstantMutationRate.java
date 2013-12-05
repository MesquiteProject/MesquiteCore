// ConstantMutationRate.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)
 
package pal.mep;

import pal.math.*;
import pal.misc.*;
import pal.io.*;

import java.io.*;

/**
 * This class models a constant mutation rate
 * (parameter: mu = mutation rate). <BR>
 *
 * @version $Id: ConstantMutationRate.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class ConstantMutationRate extends MutationRateModel implements Report, Summarizable, Parameterized, Serializable
{

	//
	// private stuff
	//
	/** The summary descriptor stuff for the public values of this
			class
			@see Summarizable, getSummaryDescriptors()
	*/
	private static final String[] CP_SUMMARY_TYPES = {"mu", "muSE"}; //This is still 1.0 compliant...

	//
	// Public stuff
	//

	/** mutation rate */
	public double mu;
	public double muSE;

	/**
	 * Construct demographic model with default settings
	 */
	public ConstantMutationRate(int units) {
	
		super();
	
		setUnits(units);

		mu = getDefaultValue(0);
	}


	/**
	 * Construct mutation rate model of a give rate in given units.
	 */
	public ConstantMutationRate(double rate, int units) {
	
		super();
	
		mu = rate;
		setUnits(units);
	}

	public Object clone()
	{
		return new ConstantMutationRate(getMu(), getUnits()); 
	}

	public String[] getSummaryTypes() {
		return CP_SUMMARY_TYPES;
	}

	public double getSummaryValue(int summaryType) {
		switch(summaryType) {
			case 0 : {
				return mu;
			}
			case 1 : {
				return muSE;
			}
		}
		throw new RuntimeException("Assertion error: unknown summary type :"+summaryType);
	}

	/**
	 * returns initial population size.
	 */
	public double getMu()
	{
		return mu;
	}

		
	// Implementation of abstract methods
	
	public final double getMutationRate(double t)
	{
		return mu;
	}

	public final double getExpectedSubstitutions(double t)
	{
		return mu * t;
	}

	public final double getTime(double expectedSubs) {
		return expectedSubs / mu;
	}

	/**
	 * Linearly scales this mutation rate model.
	 * @param scale getExpectedSubstitutions should return scale instead of 1.0 at time t.
	 */
	public final void scale(double scale) {
		mu *= scale;
	}

	// Parameterized interface

	public int getNumParameters()
	{
		return 1;
	}
	
	public double getParameter(int k)
	{
		return mu;
	}

	public double getUpperLimit(int k)
	{
		return 1e12;
	}

	public double getLowerLimit(int k)
	{
		return 1e-12;
	}

	public double getDefaultValue(int k)
	{
		//arbitrary default values
		if (getUnits() == GENERATIONS) {
			return 1e-6;
		} else {
			return 1e-6;
		}
	}

	public void setParameter(double value, int k)
	{
		mu = value;
	}

	public void setParameterSE(double value, int k) {
		muSE = value;
	}

	public String toString()
	{
		OutputTarget out = OutputTarget.openString();
		report(out);
		out.close();
		
		return out.getString();
	}
	
	public void report(PrintWriter out)
	{
		out.println("Mutation rate model: constant mutation rate ");
		out.println("Function: mu(t) = mu");
		
		out.print("Unit of time: ");
		if (getUnits() == GENERATIONS)
		{
			out.print("generations");
		}
		else
		{
			out.print("expected substitutions");
		}
		out.println();
		out.println();
		out.println("Parameters of function:");
		out.print(" mutation rate: ");
		fo.displayDecimal(out, mu, 6);
		out.println();
	}

	public String toSingleLine() {
		return "mu\t" + mu;
	}	
}


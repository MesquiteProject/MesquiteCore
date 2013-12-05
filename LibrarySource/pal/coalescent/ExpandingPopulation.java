// ExpandingPopulation.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

 
package pal.coalescent;

import pal.math.*;
import pal.misc.*;
import pal.io.*;

import java.io.*;

/**
 * This class models a population that grows 
 * exponentially from an inital population size alpha N0 to a present-day size N0.
 * (Parameters: N0=present-day population size; r=growth rate; alpha: ratio of
 * population sizes).
 * This model is nested with the exponential-growth model (alpha -> 0).
 * 
 *
 * @version $Id: ExpandingPopulation.java,v 1.6 2001/07/12 12:17:43 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class ExpandingPopulation extends ExponentialGrowth implements Report, Parameterized, Serializable
{
	
	//
	// Public stuff
	//
	
	/** ratio of pop. sizes */
	public double alpha; 


	/** standard error of time alpha  */
	public double alphaSE; 


	/**
	 * Construct demographic model with default settings
	 */
	public ExpandingPopulation(int units) {
	
		super(units);
		
		alpha = getDefaultValue(2);
	}


	/**
	 * Construct demographic model of an expanding population
	 *
	 */
	public ExpandingPopulation(double size, double growth, double ratio, int units) {
	
		super(size, growth, units);
	
		alpha = ratio;
	}

	public Object clone() {
		return new ExpandingPopulation(getN0(), getGrowthRate(), getRatio(), getUnits()); 
	}
		
	/**
	 * returns ratio of population sizes
	 */
	public double getRatio()
	{
		return alpha;
	}
	
	// Implementation of abstract methods
	
	public double getDemographic(double t)
	{
		if (r == 0)
		{
			return N0;
		}
		else if (alpha == 0)
		{
			return N0 * Math.exp(-t * r);
		}
		else
		{
			return N0 * (alpha + (1.0-alpha) * Math.exp(-t * r));
		}
	}

	public double getIntensity(double t)
	{
		if (r == 0)
		{
			return t/N0;
		}
		else if (alpha == 0)
		{
			return (Math.exp(t*r)-1.0)/N0/r;
		}
		else
		{
			return Math.log(1.0+alpha*(Math.exp(t*r)-1.0))/alpha/N0/r;
		}
	}
	
	public double getInverseIntensity(double x)
	{
		if (r == 0)
		{
			return N0*x;
		}
		else if (alpha == 0)
		{
			return Math.log(1.0+N0*x*r)/r;
		}
		else
		{
			return Math.log( (alpha-1.0+Math.exp(alpha*N0*x*r) )/alpha )/r;
		}
	}
	
	// Parameterized interface

	public int getNumParameters()
	{
		return 3;
	}
	
	public double getParameter(int k)
	{
		switch (k)
		{
			case 0: return N0;
			case 1: return r;
			case 2: return alpha;
			default: return 0;
		}
	}

	public double getUpperLimit(int k)
	{
		double max = 0;
		switch (k)
		{
			case 0: max = 1e50; break;
			case 1: max = 1000; break;
			// we have to to compute lots of exp(rt) !!
			case 2: max = 1; break;
			default: break;
		}

		return max;
	}

	public double getLowerLimit(int k)
	{
		double min = 0;
		switch (k)
		{
			case 0: min = 1e-12; break;
			case 1: min = 0; break;
			case 2: min = 0; break;
			default: break;
		}
		return min;
	}

	public double getDefaultValue(int k)
	{
	
		if (k == 0)
		{
			//arbitrary default values
			if (getUnits() == GENERATIONS) {
				return 1000.0;
			} else {
				return 0.2;
			}
		}
		else if (k == 1)
		{
			return 0; //constant population
		}
		else
		{
			return 0.5; 
		}
	}

	public void setParameter(double value, int k)
	{
		switch (k)
		{
			case 0: N0 = value; break;
			case 1: r = value; break;
			case 2: alpha = value; break;
			default: break;
		}
	}

	public void setParameterSE(double value, int k)
	{
		switch (k)
		{
			case 0: N0SE = value; break;
			case 1: rSE = value; break;
			case 2: alphaSE = value; break;
			default: break;
		}
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
		out.println("Demographic model: expanding population");
		out.println("Demographic function: N(t) = N0 (alpha + (1-alpha) exp(-r t)");
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
		out.println("Parameters of demographic function:");
		out.print(" present-day population size N0: ");
		fo.displayDecimal(out, N0, 6);
		if (N0SE != 0.0)
		{
			out.print(" (S.E. ");
			fo.displayDecimal(out, N0SE, 6);
			out.print(")");
		}	
		out.println();
		
		out.print(" growth rate r: ");
		fo.displayDecimal(out, r, 6);
		if (rSE != 0.0)
		{
			out.print(" (S.E. ");
			fo.displayDecimal(out, rSE, 6);
			out.print(")");
		}	
		out.println();

		out.print(" ratio of population sizes alpha: ");
		fo.displayDecimal(out, alpha, 6);
		if (alphaSE != 0.0)
		{
			out.print(" (S.E. ");
			fo.displayDecimal(out, alphaSE, 6);
			out.print(")");
		}	
		out.println();
		
		out.println();
		out.print(" initial poulation size alpha N0: ");
		fo.displayDecimal(out, alpha*N0, 6);
		out.println();

		if (getLogL() != 0.0)
		{
			out.println();
			out.print("log L: ");
			fo.displayDecimal(out, getLogL(), 6);
			out.println();
		}
	}
}


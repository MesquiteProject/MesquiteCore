// ConstExpGrowth.java
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
 *   or
 * (Parameters: N0=present-day population size; r=growth rate; N1: pre-growth
 * ancestral population size).
 * This model is nested with the exponential-growth model (alpha -> 0 or N1 = N0).
 * It is similar but not identical to the model used in ExpandingPopulation.
 * 
 *
 * @version $Id: ConstExpGrowth.java,v 1.4 2001/07/12 12:17:43 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Andrew Rambaut
 */
public class ConstExpGrowth extends ExponentialGrowth implements Report, Parameterized, Serializable
{
	
	//
	// Public stuff
	//
	
	/** Constants for different parameterizations  */
	public final int ALPHA_PARAMETERIZATION = 0; 
	public final int N1_PARAMETERIZATION = 1; 

	/** parameterization */
	public int parameterization; 


	/** ratio of pop. sizes */
	public double alpha; 

	/** standard error of time alpha  */
	public double alphaSE; 

	/** ancestral pop. size */
	public double N1; 

	/** standard error of ancestral pop. size */
	public double N1SE; 


	/**
	 * Construct demographic model with default settings
	 */
	public ConstExpGrowth(int units, int parameterization) {
	
		super(units);
		
		this.parameterization = parameterization;
		
		switch (this.parameterization) {
			case ALPHA_PARAMETERIZATION:
				alpha = getDefaultValue(2);
			break;
			
			case N1_PARAMETERIZATION:
				N1 = getDefaultValue(2);
			break;
		}
	}


	/**
	 * Construct demographic model of an expanding population
	 *
	 */
	public ConstExpGrowth(double size, double growth, double ancestral, int units, int parameterization) {
	
		super(size, growth, units);
	
		this.parameterization = parameterization;
		
		switch (this.parameterization) {
			case ALPHA_PARAMETERIZATION:
				alpha = ancestral;
			break;
			
			case N1_PARAMETERIZATION:
				N1 = ancestral;
			break;
		}
	}

	public Object clone() {
		return new ConstExpGrowth(getN0(), getGrowthRate(), getAncestral(), getUnits(), getParameterization()); 
	}
		
	/**
	 * returns ancestral population sizes
	 */
	public double getAncestral()
	{
		if (this.parameterization == N1_PARAMETERIZATION)
			return N1;
		else
			return alpha;
	}
	
	/**
	 * returns parameterization
	 */
	public int getParameterization()
	{
		return parameterization;
	}
	
	// Implementation of abstract methods
	
	public double getDemographic(double t)
	{
		if (this.parameterization == N1_PARAMETERIZATION)
			alpha = N1 / N0;

		if (alpha == 1.0 || r == 0.0)
		{
			return N0;
		}
		else if (alpha == 0.0)
		{
			return N0 * Math.exp(-t * r);
		}
		else
		{
			double tc = -Math.log(alpha)/r;
			
			if (t < tc)
			{
				return N0 * Math.exp(-t * r); 
			}
			else
			{
				return N0 * alpha;
			} 		
		}
	}

	public double getIntensity(double t)
	{
		if (this.parameterization == N1_PARAMETERIZATION)
			alpha = N1 / N0;

		if (alpha == 1.0 || r == 0.0)
		{
			return t/N0;
		}
		else if (alpha == 0.0)
		{
			return (Math.exp(t*r)-1.0)/N0/r;
		}
		else
		{
			double tc = -Math.log(alpha)/r;
			
			if (t < tc)
			{
				return (Math.exp(r*t)-1.0)/(N0*r); 
			}
			else
			{
				return (1.0-alpha+r*t+Math.log(alpha))/(alpha*N0*r);
			} 		
		}
	}
	
	public double getInverseIntensity(double x)
	{
		if (this.parameterization == N1_PARAMETERIZATION)
			alpha = N1 / N0;

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
			double xc = (1.0-alpha)/(alpha*N0*r);
			
			if (x < xc)
			{
				return Math.log(1.0+N0*r*x)/r; 
			}
			else
			{
				return (alpha-1.0+alpha*N0*r*x-Math.log(alpha))/r;
			} 		
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
			case 2: 		
				if (this.parameterization == N1_PARAMETERIZATION)
					return N1;
				else
					return alpha;
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
			case 2: 		
				if (this.parameterization == N1_PARAMETERIZATION)
					max = 1e50;
				else
					max = 1.0;
			break;
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
			if (this.parameterization == N1_PARAMETERIZATION)
				return getDefaultValue(0); 
			else
				return 0.5; 
		}
	}

	public void setParameter(double value, int k)
	{
		switch (k)
		{
			case 0: N0 = value; break;
			case 1: r = value; break;
			case 2: 
				if (this.parameterization == N1_PARAMETERIZATION)
					N1 = value; 
				else
					alpha = value; 
			break;
			default: break;
		}
	}

	public void setParameterSE(double value, int k)
	{
		switch (k)
		{
			case 0: N0SE = value; break;
			case 1: rSE = value; break;
			case 2: 
				if (this.parameterization == N1_PARAMETERIZATION)
					N1SE = value; 
				else
					alphaSE = value; 
			break;
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
		out.println("Demographic model: const-exp growth");
		if (this.parameterization == N1_PARAMETERIZATION) {
			out.println("Demographic function: N(t) = N0 exp(-r t) for t < -ln(N1/N0)/r");
			out.println("                             N1           otherwise");
		} else {
			out.println("Demographic function: N(t) = N0 exp(-r t) for t < -ln(alpha)/r");
			out.println("                             N0 alpha     otherwise");
		}	
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

		if (this.parameterization == N1_PARAMETERIZATION) {
			out.print(" pre-growth population size N1: ");
			fo.displayDecimal(out, N1, 6);
			if (N1SE != 0.0)
			{
				out.print(" (S.E. ");
				fo.displayDecimal(out, N1SE, 6);
				out.print(")");
			}	
			
			out.println();
			out.print(" Ratio of poulation sizes alpha: ");
			fo.displayDecimal(out, N1/N0, 6);
			out.println();
		} else {
			out.print(" ratio of population sizes alpha: ");
			fo.displayDecimal(out, alpha, 6);
			if (alphaSE != 0.0)
			{
				out.print(" (S.E. ");
				fo.displayDecimal(out, alphaSE, 6);
				out.print(")");
			}	

			out.println();
			out.print(" initial population size alpha N0: ");
			fo.displayDecimal(out, alpha*N0, 6);
			out.println();
		}	
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


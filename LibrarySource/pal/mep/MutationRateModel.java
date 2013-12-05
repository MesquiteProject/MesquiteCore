// MutationRateModel.java
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
 * This abstract class contains methods that are of general use for
 * modelling mutation rate changes over time.
 *
 * @version $Id: MutationRateModel.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public abstract class MutationRateModel implements Units,
	Parameterized, Report, Cloneable, Serializable, Summarizable
{
	
	//
	// Public stuff
	//
	
	public MutationRateModel()
	{	
		units = GENERATIONS;
		
		fo = FormattedOutput.getInstance();
	}

	public abstract Object clone();

	//
	// functions that define a demographic model (left for subclass)
	//

	/**
	 * Gets the mutation rate, value of mu(t) at time t.
	 */
	public abstract double getMutationRate(double t);

	/**
	 * Returns integral of mutation rate function
	 * (= integral mu(x) dx from 0 to t).
	 */
	public abstract double getExpectedSubstitutions(double t);

	/**
	 * Return the time at which expected substitutions has occurred.
	 */
	public abstract double getTime(double expectedSubs);
	

	/**
	 * Linearly scales this mutation rate model.
	 * @param scale getExpectedSubstitutions should return scale instead of 1.0 at time t.
	 */
	public abstract void scale(double scale);

	// Parameterized and Report interface is also left for subclass


	// general functions

	/**
	 * Calculates the integral 1/mu(x) dx between start and finish.
	 */
	public double getExpectedSubstitutions(double start, double finish)
	{
		return getExpectedSubstitutions(finish) - getExpectedSubstitutions(start);
	}

	/**
	 * Units in which time units are measured.
	 */
	private int units;

	/**
	 * sets units of measurement.
	 *
	 * @param u units
	 */
	public void setUnits(int u)
	{
		units = u;
	}

	/**
	 * returns units of measurement.
	 */
	public int getUnits()
	{
		return units;
	}
		
	public abstract String toSingleLine();
		
	//
	// Private and protected stuff
	//
		
	protected FormattedOutput fo;
}


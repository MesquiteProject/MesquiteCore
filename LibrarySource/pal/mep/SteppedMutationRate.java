// SteppedMutationRate.java
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
 * This class models a step-wise mutation rate. <BR>
 * parameters: <BR>
 * mus[] = vector of mutation rates <BR>
 * muChanges[] = vector of change times <P>
 * Drummond, Forsberg and Rodrigo (2001). The inference of step-wise changes in substitution rates using serial sequence samples. accepted in MBE.
 *
 * @version $Id: SteppedMutationRate.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class SteppedMutationRate extends MutationRateModel implements Report, Summarizable, Parameterized, Serializable
{	
	//
	// Public stuff
	//

	/** mutation rates */
	public double[] mus;

	/** mutation rate SEs */
	public double[] muSEs;
	
	/** mutation rate change times */
	public double[] muChanges;

	/** whether or not the mu values are optimizable */
	public boolean fixedMus = false;

	/**
	 * Construct demographic model with default settings
	 */
	public SteppedMutationRate(double[] muChanges, int units) {
	
		super();
	
		setUnits(units);

		this.muChanges = muChanges;

		mus = new double[muChanges.length + 1];
		muSEs = new double[muChanges.length + 1];
		for (int i = 0; i < mus.length; i++) {
			mus[i] = getDefaultValue(0);
		}
	}

	/**
	 * Construct mutation rate model of a give rate in given units.
	 */
	public SteppedMutationRate(double[] rates, double[] muChanges, int units) {
		this(rates, muChanges, units, false);
	}

	/**
	 * Construct mutation rate model of a give rate in given units.
	 */
	public SteppedMutationRate(double[] rates, double[] muChanges, int units, boolean fixed) {
	
		super();
	
		fixedMus = fixed;
		mus = rates;
		muSEs = new double[rates.length];
		this.muChanges = muChanges;
		setUnits(units);
	}

	public Object clone()
	{
		return new SteppedMutationRate(getMus(), getMuChanges(), getUnits(), fixedMus); 
	}

	public String[] getSummaryTypes() {
		if (summaryTypes == null) {
			summaryTypes = new String[mus.length];
			for (int i = 0; i < summaryTypes.length; i++) {
				summaryTypes[i] = "mu " + i;
			}
		}
		return summaryTypes;
	}

	public double getSummaryValue(int summaryType) {
		
		if (summaryType < mus.length) {
			return mus[summaryType];
		}
		throw new RuntimeException("Assertion error: unknown summary type :"+summaryType);
	}

	/**
	 * returns current day mutation rate.
	 */
	public double getMu()
	{
		return mus[0];
	}

		
	// Implementation of abstract methods
	
	public final double getMutationRate(double t)
	{
		int muIndex = 0;
		while ((muIndex < muChanges.length) && (t > muChanges[muIndex])) {
			muIndex += 1;
		}
		return mus[muIndex];
	}

	public final double getExpectedSubstitutions(double time)
	{
		double currentTime = 0.0;
		double height = 0.0;
		int muIndex = 0;
		double timeInterval = 0.0;	
	
		while (time > currentTime) {
		
			// if no more changes in mu go straight to the end
			if (muIndex >= muChanges.length) {
				timeInterval = time - currentTime;

				//update current time
				currentTime = time;
				
			} else {
				//find out the next time interval
				timeInterval = muChanges[muIndex] - currentTime;
			
				//truncate time interval if it exceeds node height.
				if ((currentTime + timeInterval) > time) {
					timeInterval = time - currentTime;
					
					//update current time
					currentTime = time;
				} else {
					//update current time
					currentTime = muChanges[muIndex];
				}

			}

			
			// update total height in substitutions
			height += mus[muIndex] * timeInterval;
	
			//update mu index
			muIndex += 1;
	
		}
		return height;
	}

	public final double getTime(double expectedSubs) {
			
		int changePoint = 0;
		while ((changePoint < muChanges.length) && 
			(expectedSubs < getExpectedSubstitutions(muChanges[changePoint]))) {
			changePoint += 1;
		}
		
		if (changePoint == 0) {
			// before first change point
			return expectedSubs / mus[changePoint];
		} else {
			double time = muChanges[changePoint-1];
			double expectedSoFar = getExpectedSubstitutions(time);
			time += (expectedSubs - expectedSoFar) / mus[changePoint];
			return time;
		}
	}
		
	/**
	 * Linearly scales this mutation rate model.
	 * @param scale getExpectedSubstitutions should return scale instead of 1.0 at time t.
	 */
	public final void scale(double scale) {
		for (int i =0 ; i < mus.length; i++) {
			mus[i] *= scale;
		}
	}

	public static double[] getTimeIntervals(double[] muChanges, double smallTime, double bigTime) {
		
		double[] intervals = new double[muChanges.length + 1];
	
		double currentTime = smallTime;
		double height = 0.0;
		
		int muIndex = 0;
		while((muIndex < muChanges.length) && (muChanges[muIndex] < smallTime)) {
			muIndex += 1;
		}
		
		double timeInterval = 0.0;
		
		while (bigTime > currentTime) {
		
			// if no more changes in mu go straight to the end
			if (muIndex >= muChanges.length) {
				intervals[muIndex] = bigTime - currentTime;
			
				//update current time
				currentTime = bigTime;
			} else {
				//find out the next time interval
				intervals[muIndex] = muChanges[muIndex] - currentTime;
			
				//truncate time interval if it exceeds node height.
				if ((currentTime + intervals[muIndex]) > bigTime) {
					intervals[muIndex] = bigTime - currentTime;

					//update current time
					currentTime = bigTime;
				} else {
					//update current time
					currentTime = muChanges[muIndex];
				}
			}
		
			//update mu index
			muIndex += 1;
		}
	
		return intervals;
	}

	
	public double[] getDeltas(double[] times) {
		double height = 0.0;
		double[] deltas = new double[times.length-1];
		for (int i = 0; i < deltas.length; i++) {
			deltas[i] = getExpectedSubstitutions(times[i+1]) - height;
			height += deltas[i];
		}
		return deltas;
	}
	
	// Parameterized interface

	public int getNumParameters()
	{
		if (fixedMus) 
			return 0;
		else return mus.length;
	}
	
	public double getParameter(int k)
	{
		return mus[k];
	}

	public double getUpperLimit(int k)
	{
		return 1e12;
	}

	public double getLowerLimit(int k)
	{
		return 0.0;
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
		mus[k] = value;
	}

	public void setParameterSE(double value, int k) {
		muSEs[k] = value;
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
		out.println("Mutation rate model: stepped mutation rate ");
			
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
		out.println(" mu\tinterval");
		for (int i = 0; i < mus.length; i++) {
			fo.displayDecimal(out, mus[i], 6);
			if (i == 0) {
				out.print("\t0.0 to ");
				fo.displayDecimal(out, muChanges[i], 6);
				out.println();
			} else {
				out.print("\t");
				fo.displayDecimal(out, muChanges[i-1], 6);
				out.print(" to ");
				if (i < muChanges.length) {
					fo.displayDecimal(out, muChanges[i], 6);
					out.println();
				} else {
					out.println("infinity");
				}
			}
		}
	}

	public double[] getMus() {
		double[] newMus = new double[mus.length];
		for (int i = 0; i < newMus.length; i++) {
			newMus[i] = mus[i];
		}
		return newMus;
	}

	private double[] getMuChanges() {
		double[] newMCs = new double[muChanges.length];
		for (int i = 0; i < newMCs.length; i++) {
			newMCs[i] = muChanges[i];
		}
		return newMCs;
	}

	public String toSingleLine() {
		String line = "";
		for (int i = 0; i < mus.length; i++) {
			line += "mu[" + i + "]\t" + mus[i];
			if (i < (mus.length - 1)) {
				line += "\t";
			}
		}
		return line;
	}

	String[] summaryTypes = null;
}


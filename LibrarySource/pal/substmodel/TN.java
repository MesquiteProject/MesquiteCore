// TN.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;

import java.io.*;


/**
 * Tamura-Nei model of nucleotide evolution
 *
 * @version $Id: TN.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class TN extends NucleotideModel
{
	/**
	 * constructor 1
	 *
	 * @param kappa transition/transversion rate ratio
	 * @param r pyrimidine/purin transition rate ratio
	 * @param freq nucleotide frequencies
	 */
	public TN(double kappa, double r, double[] freq)
	{
		super(freq);
		
		this.kappa = kappa;
		this.r = r;
		
		makeTN();
		fromQToR();
		
		showSE = false;
	}

	/**
	 * constructor 2
	 *
	 * @param params parameter list
	 * @param freq nucleotode frequencies
	 */
	public TN(double[] params, double[] freq)
	{
		this(params[0], params[1], freq);
	}
 
	// Get numerical code describing the model type
	public int getModelID()
	{
		return 1;
	}
 
 	// interface Parameterized
 
	public void report(PrintWriter out)
	{
		out.println("Model of substitution: TN (Tamura-Nei 1993)");
		out.print("Transition/transversion rate ratio kappa: ");
		format.displayDecimal(out, kappa, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, kappaSE, 2);
			out.print(")");
		}
		out.println();
		
		out.print("Y/R transition rate ratio: ");
		format.displayDecimal(out, r, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, rSE, 2);
			out.print(")");
		}
		out.println();
		
		out.println();
		printFrequencies(out);
		printRatios(out);
	}	

	// interface Parameterized

	public int getNumParameters()
	{
		return 2;
	}
	
	public void setParameter(double param, int n)
	{
		switch(n)
		{
			case 0: kappa = param; break;
			case 1: r = param; break;

			default: throw new IllegalArgumentException();
		}

		makeTN();
		fromQToR();
	}

	public double getParameter(int n)
	{
		double value;
		
		switch(n)
		{
			case 0: value = kappa; break;
			case 1: value = r; break;

			default: throw new IllegalArgumentException();
		}
				
		return value;
	}

	public void setParameterSE(double paramSE, int n)
	{
		switch(n)
		{
			case 0: kappaSE = paramSE; break;
			case 1: rSE = paramSE; break;

			default: throw new IllegalArgumentException();
		}

		showSE = true;
	}

	public double getLowerLimit(int n)
	{
		return 0.0001;
	}
	
	public double getUpperLimit(int n)
	{
		return 100.0;
	}
	
	public double getDefaultValue(int n)
	{
		double value;
		
		switch(n)
		{
			case 0: value = 4.0; break;
			case 1: value = 0.5; break;

			default: throw new IllegalArgumentException();
		}
				
		return value;
	}

	
	//
	// Private stuff
	// 

	private boolean showSE;
	private double kappa, kappaSE, r, rSE;

	// Make TN model
	private void makeTN()
	{
		// Q matrix
		rate[0][1] = 1; rate[0][2] = 2.0*kappa/(r+1.0); rate[0][3] = 1;
		rate[1][2] = 1; rate[1][3] = 2.0*kappa*r/(r+1.0);
		rate[2][3] = 1;
	}
}


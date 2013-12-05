// GTR.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;

import java.io.*;


/**
 * GTR (general time reversible) model of nucleotide evolution
 *
 * @version $Id: GTR.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class GTR extends NucleotideModel implements Serializable
{
	/**
	 * constructor 1
	 *
	 * @param a entry in rate matrix
	 * @param b entry in rate matrix
	 * @param c entry in rate matrix
	 * @param d entry in rate matrix
	 * @param e entry in rate matrix
	 * @param freq nucleotide frequencies
	 */
	public GTR(double a, double b, double c, double d, double e, double[] freq)
	{
		super(freq);
		
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		
		makeGTR();
		fromQToR();
		
		showSE = false;
	}

	/**
	 * constructor 2
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 */
	public GTR(double[] params, double[] freq)
	{
		this(params[0], params[1], params[2],
			params[3], params[4], freq);
	}

	// Get numerical code describing the model type
	public int getModelID()
	{
		return 0;
	}
 
 	// interface Report
 
	public void report(PrintWriter out)
	{
		out.println("Model of substitution: GTR (Lanave et al. 1984)");
		
		out.print("Parameter a: ");
		format.displayDecimal(out, a, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, aSE, 2);
			out.print(")");
		}
		out.println();
	
		out.print("Parameter b: ");
		format.displayDecimal(out, b, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, bSE, 2);
			out.print(")");
		}
		out.println();

		out.print("Parameter c: ");
		format.displayDecimal(out, c, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, cSE, 2);
			out.print(")");
		}
		out.println();

		out.print("Parameter d: ");
		format.displayDecimal(out, d, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, dSE, 2);
			out.print(")");
		}
		out.println();

		out.print("Parameter e: ");
		format.displayDecimal(out, e, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, eSE, 2);
			out.print(")");
		}
		out.println();

		out.println("                                   A  C  G  T");
		out.println("Corresponding rate matrix      ----------------");
		out.println("(shown without frequencies):     A    a  b  c");
		out.println("                                 C       d  e");
		out.println("                                 G          1");
				
		out.println();
		printFrequencies(out);
		printRatios(out);
	}	

	// interface Parameterized

	public int getNumParameters()
	{
		return 5;
	}
	
	public void setParameter(double param, int n)
	{
		switch(n)
		{
			case 0: a = param; break;
			case 1: b = param; break;
			case 2: c = param; break;
			case 3: d = param; break;
			case 4: e = param; break;
			
			default: throw new IllegalArgumentException();
		}
				
		makeGTR();
		fromQToR();
	}

	public double getParameter(int n)
	{
		double value;
		
		switch(n)
		{
			case 0: value = a; break;
			case 1: value = b; break;
			case 2: value = c; break;
			case 3: value = d; break;
			case 4: value = e; break;
			
			default: throw new IllegalArgumentException();
		}
				
		return value;
	}

	public void setParameterSE(double paramSE, int n)
	{
		switch(n)
		{
			case 0: aSE = paramSE; break;
			case 1: bSE = paramSE; break;
			case 2: cSE = paramSE; break;
			case 3: dSE = paramSE; break;
			case 4: eSE = paramSE; break;
			
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
		return 10000.0;
	}
	
	public double getDefaultValue(int n)
	{
		return 1.0;
	}
	

	//
	// Private stuff
	// 

	private boolean showSE;
	private double a, b, c, d, e;
	private double aSE, bSE, cSE, dSE, eSE;

	// Make REV model
	private void makeGTR()
	{
		// Q matrix
		rate[0][1] = a; rate[0][2] = b; rate[0][3] = c;
		rate[1][2] = d; rate[1][3] = e;
		rate[2][3] = 1;
	}
}


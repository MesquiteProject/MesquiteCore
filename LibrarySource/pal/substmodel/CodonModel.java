// CodonModel.java
//
// (c) 1999-2001 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.datatype.*;

import java.io.*;


/**
 * base class for nucleotide rate matrices
 *
 * @version $Id: CodonModel.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Andrew Rambaut
 */
abstract public class CodonModel extends AbstractRateMatrix implements RateMatrix, Serializable
{
	//
	// Public stuff
	//

	/**
	 * Create codon substitution model according to model type
	 *
	 * @param modelID model code
	 * @param params model parameters
	 * @param freq  model frequencies
	 *
	 * @return codon rate matrix
	 */
	public static CodonModel getInstance(int modelID, double[] params, double[] freq)
	{
		if (modelID == 0)
		{
			return new YangCodonModel(params, freq);
		}
		else
		{
// Throw error?
			return new YangCodonModel(params, freq);
		}
	}

	// interface Report (inherited, remains abstract)
	
	// interface Parameterized (inherited, remains abstract)
	

	//
	// Protected stuff (for use in derived classes)
	//

	// Constructor
	protected CodonModel(double[] f)
	{
		// Dimension = 64
		super(64);
		
		dataType = new Codons();
		setFrequencies(f);
	}

	protected void printFrequencies(PrintWriter out)
	{
		out.println("Codon frequencies:");
		super.printFrequencies(out);
	}
	
	protected void printRatios(PrintWriter out)
	{
	}

	//
	// Private stuff
	//

	private void computeRatios()
	{
	}
}


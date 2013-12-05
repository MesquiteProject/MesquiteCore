// YangCodonModel.java
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
 * Yang's model of codon evolution
 *
 * @version $Id: YangCodonModel.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Andrew Rambaut
 */
public class YangCodonModel extends CodonModel implements Serializable
{
	/**
	 * constructor 1
	 *
	 * @param omega N/S rate ratio
	 * @param kappa transition/transversion rate ratio
	 * @param freq codon frequencies
	 * @param codonTable codon table
	 */
	public YangCodonModel(double omega, double kappa, double[] freq,
		CodonTable codonTable)
	{
		super(freq);
		
		this.kappa = kappa;
		this.omega = omega;
		this.codonTable = codonTable;
				
		makeYangModel();
		fromQToR();
		
		showSE = false;
	}
	
	/**
	 * constructor 2 (universal codon table)
	 *
	 * @param omega N/S rate ratio
	 * @param kappa transition/transversion rate ratio
	 * @param freq codon frequencies
	 */
	public YangCodonModel(double omega, double kappa, double[] freq)
	{
		this(omega, kappa, freq, CodonTableFactory.createUniversalTranslator());
	}

	/**
	 * constructor 4 (universal codon table)
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 */
	public YangCodonModel(double[] params, double[] freq)
	{
		this(params[0], params[1], freq, CodonTableFactory.createUniversalTranslator());
	}

	/**
	 * constructor 3
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 * @param codonTable codon table
	 */
	public YangCodonModel(double[] params, double[] freq,
		CodonTable codonTable)
	{
		this(params[0], params[1], freq, codonTable);
	}


	// Get numerical code describing the model type
	public int getModelID()
	{
		return 0;
	}
 
 	// interface Report
 
	public void report(PrintWriter out)
	{
		out.println("Model of substitution: YANG (Yang, ????)");
		
		out.print("Parameter kappa: ");
		format.displayDecimal(out, kappa, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, kappaSE, 2);
			out.print(")");
		}
		out.println();
	
		out.print("Parameter omega: ");
		format.displayDecimal(out, omega, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, omegaSE, 2);
			out.print(")");
		}
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
			case 1: omega = param; break;
			
			default: throw new IllegalArgumentException();
		}
				
		makeYangModel();
		fromQToR();
	}

	public double getParameter(int n)
	{
		double value;
		
		switch(n)
		{
			case 0: value = kappa; break;
			case 1: value = omega; break;
			
			default: throw new IllegalArgumentException();
		}
				
		return value;
	}

	public void setParameterSE(double paramSE, int n)
	{
		switch(n)
		{
			case 0: kappaSE = paramSE; break;
			case 1: omegaSE = paramSE; break;
			
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
		return 1.0;
	}
	

	//
	// Private stuff
	// 

	private boolean showSE;
	private double kappa, omega;
	private double kappaSE, omegaSE;
	private byte[] rateMap;

	// Make REV model
	private void makeYangModel()
	{
		int numRates = ( dimension * (dimension - 1) ) / 2;
		rateMap = new byte[numRates];
	
//	NewArray(rateMap, char, numRates);
	
		int u, v, rateClass;
		char aa1, aa2;
		char[] codon1, codon2;
		GeneralizedCodons generalizedCodons = new GeneralizedCodons();
		
		for (u = 0; u < dimension; u++) {
			codon1 = generalizedCodons.getCodonFromCodonIndex(u);

			for (v = u+1; v < dimension; v++) {
				codon2 = generalizedCodons.getCodonFromCodonIndex(v);
				
				rateClass = -1;
				if (codon1[0] != codon2[0]) {
					if ( (codon1[0] == 'A' && codon2[0] == 'G') || 
						(codon1[0] == 'G' && codon2[0] == 'A') || // A <-> G
						(codon1[0] == 'C' && codon2[0] == 'T') || 
						(codon1[0] == 'T' && codon2[0] == 'C') )  // C <-> T
						rateClass = 1; // Transition
					else
						rateClass = 2; // Transversion
				}
				if (codon1[1] != codon2[1]) {
					if (rateClass == -1) {
						if ( (codon1[1] == 'A' && codon2[1] == 'G') || 
							(codon1[1] == 'G' && codon2[1] == 'A') || // A <-> G
							(codon1[1] == 'C' && codon2[1] == 'T') || 
							(codon1[1] == 'T' && codon2[1] == 'C') )  // C <-> T
							rateClass = 1; // Transition
						else
							rateClass = 2; // Transversion
					} else 
						rateClass = 0; // Codon changes at more than one position
				}
				if (codon1[2] != codon2[2]) {
					if (rateClass == -1) {
						if ( (codon1[2] == 'A' && codon2[2] == 'G') || 
							(codon1[2] == 'G' && codon2[2] == 'A') || // A <-> G
						 	(codon1[2] == 'C' && codon2[2] == 'T') || 
							(codon1[2] == 'T' && codon2[2] == 'C') )  // C <-> T
							rateClass = 1; // Transition
						else
							rateClass = 2; // Transversion
					} else 
						rateClass = 0; // Codon changes at more than one position
				}
				 					 
	 			if (rateClass != 0) {
					aa1 = codonTable.getAminoAcidChar(codon1);
					aa2 = codonTable.getAminoAcidChar(codon2);
					if (aa1 == AminoAcids.TERMINATE_CHARACTER || aa2 == AminoAcids.TERMINATE_CHARACTER)
						rateClass = 0; // Can't change to a stop codon
					else if (aa1 != aa2)
						rateClass += 2; // Is a non-synonymous change
				}
				
				switch (rateClass) {
					case 0: rate[u][v] = 0.0; break;	// codon changes in more than one codon position
					case 1: rate[u][v] = kappa; break;			// synonymous transition
					case 2: rate[u][v] = 1.0; break;			// synonymous transversion
					case 3: rate[u][v] = kappa * omega; break;	// non-synonymous transition
					case 4: rate[u][v] = omega; break;			// non-synonymous transversion
				}
				rate[v][u] = rate[u][v];
			}
			rate[u][u] = 0.0;
		}
	}
	
	// genetic code used to figure out stop codons
	private CodonTable codonTable;

}


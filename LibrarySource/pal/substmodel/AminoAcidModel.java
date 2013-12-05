// AminoAcidModel.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

import pal.misc.*;
import pal.datatype.*;

import java.io.*;


/**
 * base class of rate matrices for amino acids
 *
 * @version $Id: AminoAcidModel.java,v 1.6 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public abstract class AminoAcidModel extends AbstractRateMatrix implements RateMatrix
{

	/**
	 * Create amino acid model according to model type
	 *
	 * @param modelID model code
	 * @param freq  model frequencies
	 *
	 * @return amino acid rate matrix
	 */
	public static AminoAcidModel getInstance(int modelID, double[] freq)
	{
		if (modelID == AminoAcidModelID.DAYHOFF)
		{
			return new Dayhoff(freq);
		}
		else if (modelID == AminoAcidModelID.JTT)
		{
			return new JTT(freq);
		}
		else if (modelID == AminoAcidModelID.MTREV24)
		{
			return new MTREV24(freq);
		}
		else if (modelID == AminoAcidModelID.BLOSUM62)
		{
			return new BLOSUM62(freq);
		}
		else if (modelID == AminoAcidModelID.VT)
		{
			return new VT(freq);
		}
		else if (modelID == AminoAcidModelID.WAG)
		{
			return new WAG(freq);
		}
		else if (modelID == AminoAcidModelID.CPREV)
		{
			return new CPREV(freq);
		}
		else
		{
			return new Dayhoff(freq);
		}
	}

	/**
	 * get numerical code of amino acid model that would probably
	 * be suitable for a given sequence data set
	 *
	 * @param freq amino acid frequencies of the data set
	 *
	 * @return numerical code of suitable AminoAcidModel
	 */
	public static int getSuitableModelID(double[] freq)
	{	
		int NUMMODELS = AminoAcidModelID.MODELCOUNT;
		double[] ofreq = new double[20];
		double[] dist = new double[NUMMODELS];
		
		Dayhoff.getOriginalFrequencies(ofreq);
		dist[0] = getDist(freq, ofreq);
		JTT.getOriginalFrequencies(ofreq);
		dist[1] = getDist(freq, ofreq);
		MTREV24.getOriginalFrequencies(ofreq);
		dist[2] = getDist(freq, ofreq);
		BLOSUM62.getOriginalFrequencies(ofreq);
		dist[3] = getDist(freq, ofreq);
		VT.getOriginalFrequencies(ofreq);
		dist[4] = getDist(freq, ofreq);
		WAG.getOriginalFrequencies(ofreq);
		dist[5] = getDist(freq, ofreq);
		CPREV.getOriginalFrequencies(ofreq);
		dist[6] = getDist(freq, ofreq);

		int bestModel = 0;
		double minDist = dist[0];
		
		for (int i = 1; i < NUMMODELS; i++)
		{
			if (dist[i] < minDist)
			{
				minDist = dist[i];
				bestModel = i;
			}
		}
		
		return bestModel; 
	}

	// interface Report

	public abstract void report(PrintWriter out);
	
	// interface Parameterized

	public int getNumParameters()
	{
		return 0;
	}
	
	public void setParameter(double param, int n)
	{
		return;
	}

	public double getParameter(int n)
	{
		return 0.0;
	}

	public void setParameterSE(double paramSE, int n)
	{
		return;
	}

	public double getLowerLimit(int n)
	{
		return 0.0;
	}
	
	public double getUpperLimit(int n)
	{
		return 0.0;
	}
	
	public double getDefaultValue(int n)
	{
		return 0.0;
	}
	

	
	//
	// Protected stuff
	//

	// Constructor
	protected AminoAcidModel(double[] f)
	{
		// Dimension = 20
		super(20);
		
		dataType = new AminoAcids();
		setFrequencies(f);
	}
	
	protected void printFrequencies(PrintWriter out)
	{
		out.println("Amino acid frequencies:");
		super.printFrequencies(out);
	}
	
	//
	// Private stuff
	//
	
	private static double getDist(double[] f1, double[] f2)
	{
		double sum = 0.0;
		for (int i = 0; i < f1.length; i++)
		{
			double diff = f1[i]-f2[i];
			sum += diff*diff;
		}
		return sum;
	}
}


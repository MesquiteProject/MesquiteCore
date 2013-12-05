// RateMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.io.*;
import pal.datatype.*;

import java.io.*;


/**
 * abstract base class for all rate matrices
 *
 * @version $Id: AbstractRateMatrix.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
abstract public class AbstractRateMatrix implements RateMatrix
{
	//
	// Public stuff
	//

	// Constraints and conventions:
	// - first argument: row
	// - second argument: column
	// - transition: from row to column
	// - sum of every row = 0
	// - sum of frequencies = 1
	// - frequencies * rate matrix = 0 (stationarity)
	// - expected number of substitutions = 1 (Sum_i pi_i*R_ii = 0)

	/** dimension */
	public int dimension;

	/** stationary frequencies (sum = 1.0) */
	public double[] frequency;

	/**
	 * rate matrix (transition: from 1st index to 2nd index)
	 */
	public double[][] rate;

	/** data type */
	public DataType dataType;

	/**
	 * get numerical code describing the data type
	 *
	 * @return integer code identifying a data type
	 */
	public int getTypeID()	{
		return dataType.getTypeID();
	}

	/**
	 * get numerical code describing the model type
	 *
	 * @return integer code identifying a substitution model
	 */
	abstract public int getModelID();

	public int getDimension() {
   	return dimension;
	}

	/**
		* @return stationary frequencies (sum = 1.0)
		*/
	public double[] getEqulibriumFrequencies() {
		return frequency;
	}

	/**
		* @return stationary frequencie (sum = 1.0) for ith state
		*/
	public double getEqulibriumFrequency(int i) {
		return frequency[i];
	}


	/** For those that like Getter/Setters */
	public DataType getDataType() {
		return dataType;
	}

	/**
		Return a matrix exponential appropriate to this ratematrix (generally returns SimpleRateMatrix())
	*/
	public TransitionProbability getTransitionProbability() {
		return new MatrixExponential(this);
	}

		/**
		Return a matrix exponential appropriate to this ratematrix (generally returns SimpleRateMatrix())
		given an old matrix exponential (may reuse old matrix exponential)
	*/
	public TransitionProbability getTransitionProbability(TransitionProbability old) {
		if(old instanceof MatrixExponential) {
			((MatrixExponential)old).setMatrix(this);
			return old;
		}
		return getTransitionProbability();
	}

	/**
	 * @return rate matrix (transition: from 1st index to 2nd index)
	 */
	public double[][] getRelativeRates() {
		return rate;
	}

	// interface Report (remains abstract)
	
	// interface Parameterized (remains abstract)


 	//
	// Protected stuff (for use in derived classes)
	//

	// Constructor 
	protected AbstractRateMatrix(int dim)
	{
		format = FormattedOutput.getInstance();
		
		dimension = dim;
		frequency = new double[dim];
		rate = new double[dim][dim];
	}
 
	protected FormattedOutput format;
	
	protected void printFrequencies(PrintWriter out)
	{
		for (int i = 0; i < dimension; i++)
		{
			out.print("pi(" + dataType.getChar(i) + ") = ");
			format.displayDecimal(out, frequency[i], 5);
			out.println();
		}
		out.println();
	}
 
 	protected void setFrequencies(double[] f)
	{
		for (int i = 0; i < dimension; i++)
		{
			frequency[i] = f[i];
		}
		checkFrequencies();
	}
 
	// Computes normalized rate matrix from Q matrix (general reversible model)
	// - Q_ii = 0
	// - Q_ij = Q_ji
	// - Q_ij is stored in R_ij (rate)
	// - only upper triangular is used
	protected void fromQToR()
	{
		double q;
		
		for (int i = 0; i < dimension; i++)
		{
			for (int j = i + 1; j < dimension; j++)
			{
				q = rate[i][j];
				rate[i][j] = q*frequency[j];
				rate[j][i] = q*frequency[i];
			}
		}
	
		makeValid();
		normalize();
	}


 	//
	// Private stuff
	//
	
	// Make it a valid rate matrix (make sum of rows = 0)
	private void makeValid()
	{
		for (int i = 0; i < dimension; i++)
		{
			double sum = 0.0;
			for (int j = 0; j < dimension; j++)
			{
				if (i != j)
				{
					sum += rate[i][j];
				}
			}
			rate[i][i] = -sum;
		}
	}
	
	// Normalize rate matrix to one expected substitution per unit time
	private void normalize()
	{
		double subst = 0.0;
		
		for (int i = 0; i < dimension; i++)
		{
			subst += -rate[i][i]*frequency[i];
		}
		for (int i = 0; i < dimension; i++)
		{
			for (int j = 0; j < dimension; j++)
			{
				rate[i][j] = rate[i][j]/subst;
			}
		}
	}

	/**
	 * ensures that frequencies are not smaller than MINFREQ and
	 * that two frequencies differ by at least 2*MINFDIFF.
	 * This avoids potentiak problems later when eigenvalues
	 * are computed.
	 */
	private void checkFrequencies()
	{
		// required frequency difference
		double MINFDIFF = 1e-10;

		// lower limit on frequency
		double MINFREQ = 1e-10;  

		int maxi = 0;
		double sum = 0.0;
		double maxfreq = 0.0;
		for (int i = 0; i < dimension; i++)
		{
			double freq = frequency[i];
			if (freq < MINFREQ) frequency[i] = MINFREQ;
			if (freq > maxfreq)
			{
				maxfreq = freq;
				maxi = i;
			}
			sum += frequency[i];
		}
		frequency[maxi] += 1.0 - sum;
	
		for (int i = 0; i < dimension - 1; i++)
		{
			for (int j = i+1; j < dimension; j++)
			{
				if (frequency[i] == frequency[j])
				{
					frequency[i] += MINFDIFF;
					frequency[j] -= MINFDIFF;
				}
			}
		}
	}

	public Object clone() {
		try {
			RateMatrix matrix = (RateMatrix)super.clone();
			return matrix;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen
			throw new InternalError();
		}
	}
}


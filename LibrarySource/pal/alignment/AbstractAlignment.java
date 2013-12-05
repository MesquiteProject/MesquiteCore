// AbstractAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.alignment;

import pal.io.*;
import pal.datatype.*;
import pal.misc.*;

import java.io.*;

/**
 * abstract base class for any alignment data.
 *
 * @version $Id: AbstractAlignment.java,v 1.3 2001/07/13 14:39:12 korbinian Exp $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
abstract public class AbstractAlignment implements Alignment, Serializable, IdGroup, Report
{
	//
	// Public stuff
	//

	public AbstractAlignment()
	{
		
	}

	/** number of sequences */
	protected int numSeqs;

	/** length of each sequence */
	protected int numSites;

	/** sequence identifiers */
	protected IdGroup idGroup;

	/**
	 * frequencies of the allowed states 
	 * (scaled to sum to 1.0 and suitable for RateMatrix)
	 */
	protected double[] frequency;

	/** data type */
	protected DataType dataType;
		
	// Abstract method

	/** sequence alignment at (sequence, site) */
	abstract public char getData(int seq, int site);

	/** 
	 * returns true if there is a gap in the give position.
	 */
	public boolean isGap(int seq, int site) {
		return DataTypeUtils.isGap(dataType, getData(seq, site));
	}

	/** Guess data type */
	public void guessDataType()
	{
		dataType = AlignmentUtils.getSuitableInstance(this);
	}

	/** Returns the datatype of this alignment */
	public DataType getDataType()
	{
		return dataType;
	}

	/** Sets the datatype of this alignment */
	public void setDataType(DataType d)
	{
		dataType = d;

		// if dataType was guessed incorrectly  then when corrected
		// the frequencies should be recalculated.
		AlignmentUtils.estimateFrequencies(this);
	}

	/** returns representation of this alignment as a string */
	public String toString() {
	
		StringWriter sw = new StringWriter();
		AlignmentUtils.print(this, new PrintWriter(sw));
		
		return sw.toString();
	}

	// interface Report
	
	public void report(PrintWriter out)
	{
		AlignmentUtils.report(this, out);
	}

	
	/**
	 * Fills a [numsequences][length] matrix with indices. 
	 * Each index represents the sequence state, -1 means a gap.
	 */
	public int[][] getStates() {
	
		int[][] indices = new int[numSeqs][numSites];

		for (int i = 0; i < numSeqs; i++) {
			int seqcounter = 0;
	   
			for (int j = 0; j < numSites; j++) {
		
				indices[i][j] = dataType.getState(getData(i, j));
			
				if (indices[i][j] >= dataType.getNumStates()) {
					indices[i][j] = -1;
				}
			}
		}

		return indices;
	}

   	/**
	 * Return number of sites in this alignment
	 */
	public final int getLength() {
		return numSites;
	}

	/**
	 * Return number of sequences in this alignment
	 */
	public final int getSequenceCount() {
		return numSeqs;
	}

	/**
	 * Return number of sites for each sequence in this alignment
	 * @note for people who like accessor methods over public instance variables...
	 */
	public final int getSiteCount() {
		return numSites;
	}
	/**
	 * Returns a string representing a single sequence (including gaps)
	 * from this alignment.
	 */
	public String getAlignedSequenceString(int seq) {
		char[] data = new char[numSites];
		for (int i = 0; i < numSites; i++) {
			data[i] = getData(seq, i);
		}
		return new String(data);
	}

	//IdGroup interface
	public Identifier getIdentifier(int i) {return idGroup.getIdentifier(i);}
	public void setIdentifier(int i, Identifier ident) { idGroup.setIdentifier(i, ident); }
	public int getIdCount() { return idGroup.getIdCount(); }
	public int whichIdNumber(String name) { return idGroup.whichIdNumber(name); }

	public double[] getFrequency() {
		return frequency;
	}

	public void setFrequency(double[] f) {
		frequency = f;
	}

}


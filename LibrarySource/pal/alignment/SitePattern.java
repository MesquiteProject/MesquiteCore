// SitePattern.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


// Known bugs and limitations:
// - computational complexity O(numSeqs*numSites)


package pal.alignment;

import pal.datatype.*;
import pal.misc.*;


/**
 * takes an Alignment and determines its site patterns
 *
 * @version $Id: SitePattern.java,v 1.6 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class SitePattern extends AbstractAlignment
{
	//
	// Public stuff
	//


 	/** number of site patterns */
	public int numPatterns;

	/** site -> site pattern */
	public int[] alias;

	/** weights of each site pattern */
	public int weight[];

	/** site patterns [sequence][site pattern] */
	public byte[][] pattern;

 
	// Implementation of abstract Alignment method

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site)
	{
		return dataType.getChar(pattern[seq][alias[site]]);
	}

	/**
	 * infer site patterns for a given alignment
	 *
	 * @param a alignment
	 */
	public SitePattern(Alignment a)
	{
		if (a.getDataType() == null)
		{
			a.setDataType(AlignmentUtils.getSuitableInstance(a));
		}

		// Fields from Alignment
		dataType = a.getDataType();
		numSites = a.getSiteCount();
		numSeqs = a.getSequenceCount();
		idGroup = a;
		frequency = a.getFrequency(); // Bernard Suh <bsuh@tigr.org>
		

		makeSitePattern(a);
	}
	
	/**
	 * construct SitePattern from scratch
	 *
	 * @param dataType data type
	 * @param numSites number of sites
	 * @param numSeqs number of sequences
	 * @param idGroup sequence identifiers
	 * @param numPatterns number of site patterns
	 * @param alias link site -> site pattern
	 * @param weight frequency of a site pattern
	 * @param pattern site patterns
	 */
	public SitePattern(DataType dataType, int numSites, int numSeqs, IdGroup idGroup,
		int numPatterns, int[] alias, int[] weight, byte[][] pattern)
	{
		this.dataType = dataType;
		this.numSites = numSites;
		this.numSeqs =  numSeqs;
		this.idGroup = idGroup;

		this.numPatterns = numPatterns;
		this.alias = alias;
		this.weight = weight;
		this.pattern = pattern;
		
		AlignmentUtils.estimateFrequencies(this);  // Bernard Suh <bsuh@tigr.org>
	}
	

	//
	// Private stuff
	//

	private int[] patSort;

	private void makeSitePattern(Alignment alignment)
	{
		alias = new int[numSites];

		if (numSeqs > 0 && numSites > 0)
		{
			patSort = new int[numSites];

			getNumpattern(alignment);
			pattern = new byte[numSeqs][numPatterns];
			weight = new int[numPatterns];
			copypattern(alignment);

			patSort = null;
		}
		else
		{
			numPatterns = 0;
			pattern = null;
 			weight = null;
		}
	}

	private int stateData(Alignment al, int seq, int site)
	{
		return dataType.getState(al.getData(seq, site));
	}

	private void getNumpattern(Alignment alignment)
	{	
		byte tpmradix = (byte) dataType.getNumStates();

		int[] awork = new int[numSites];
		int[] count = new int[tpmradix+1];

		for (int j = 0; j < numSites; j++)
		{
			patSort[j] = j;
		}
		for (int i = numSeqs - 1; i >= 0; i--)
		{
			for (int k = 0; k < tpmradix+1; k++)
			{
 				count[k] = 0;
			}
			for (int j = 0; j < numSites; j++)
			{
				count[stateData(alignment, i, patSort[j])]++;
			}
			for (int k = 1; k < tpmradix+1; k++)
			{
				count[k] += count[k-1];
			}
			for (int j = numSites-1; j >= 0; j--)
			{
				awork[ --count[stateData(alignment, i, patSort[j])] ] = patSort[j];
			}
			for (int j = 0; j < numSites; j++)
			{
				patSort[j] = awork[j];
			}
		}
		awork = null;
		count = null;

		numPatterns = 1;
		for (int j = 1; j < numSites; j++)
		{
			int s = patSort[j-1];
			int t = patSort[j];
			for (int i = 0; i < numSeqs; i++)
			{
				if (	stateData(alignment, i, t) !=
					stateData(alignment, i, s))
				{
					numPatterns++;
					break;
				}
			}
		}
	}

	void copypattern(Alignment alignment)
	{
		int k, n;
		boolean isSame;

		n = 0;
		k = patSort[n];
		for (int i = 0; i < numSeqs; i++)
		{
			pattern[i][n] = (byte) stateData(alignment, i, k);
		}
		weight[n] = 1;
		alias[k] = 0;

		for (int j = 1; j < numSites; j++)
		{
			k = patSort[j];

			isSame = true;
			for (int i = 0; i < numSeqs; i++)
			{
				if (pattern[i][n] != (byte) stateData(alignment, i, k))
				{
					isSame = false;
					break;
				}
			}

			if (isSame)
			{	
				weight[n]++;
				alias[k] = n;
			}
			else
			{
				n++;
				for (int i = 0; i < numSeqs; i++)
				{
					pattern[i][n] = (byte) stateData(alignment, i, k);
				}
				weight[n] = 1;
				alias[k] = n;
			}
		}
	}

	// ====================================== Static Methods ===========================
	/**
		@param a An alignment
		@return alignment as a site pattern if it isn't already one (other wise just returns alighnment)
	*/
	public static final SitePattern getSitePattern(Alignment a) {
		if(a instanceof SitePattern) {
			return (SitePattern)a;
		}
		return new SitePattern(a);
	}
}


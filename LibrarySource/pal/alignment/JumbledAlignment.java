// JumbledAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.alignment;

import pal.math.*;
import pal.misc.*;


/**
 * generates jumbled alignments (randomizing input order of sequences)
 *
 * @version $Id: JumbledAlignment.java,v 1.4 2001/07/13 14:39:12 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class JumbledAlignment extends AbstractAlignment
{
	//
	// Public stuff
	//
	
	/*
	 * create jumbled alignment
	 *
	 * @param raw original alignment
	 */
	public JumbledAlignment(Alignment raw)
	{
		rawAlignment = raw;
		
		numSeqs = raw.getSequenceCount();
		numSites = raw.getSiteCount();
		
		idGroup = new SimpleIdGroup(numSeqs);
		alias = new int[numSeqs];
		
		urn = new UrnModel(numSeqs);
		
		jumble();
	}

	// Implementation of abstract Alignment method

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site)
	{
		return rawAlignment.getData(alias[seq], site);
	}
	
	
	/** jumble sequences (rearrange input order) */
	public void jumble()
	{
		urn.reset();
		for (int i = 0; i < numSeqs; i++)
		{
			alias[i] = urn.drawDontPutBack();
			idGroup.setIdentifier(i, rawAlignment.getIdentifier(alias[i]));
		}
	}
	
	
	//
	// Private stuff
	//
	
	private UrnModel urn;
	private Alignment rawAlignment;
	private int[] alias;
}


// SplitSystem.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

import java.io.*;

import pal.misc.*;

/**
 * data structure for a set of splits 
 *
 * @version $Id: SplitSystem.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class SplitSystem
{
	//
	// Public stuff
	//

	/**
	 * @param idGroup  sequence labels
	 * @param size     number of splits
	 */
	public SplitSystem(IdGroup idGroup, int size)
	{
		this.idGroup = idGroup;
		
		labelCount = idGroup.getIdCount();
		splitCount = size;
		
		splits = new boolean[splitCount][labelCount];
	}

	/** get number of splits */
	public int getSplitCount()
	{		
		return splitCount;
	}

	/** get number of labels */
	public int getLabelCount()
	{		
		return labelCount;
	}

	/** get split vector */
	public boolean[][] getSplitVector()
	{		
		return splits;
	}

	/** get split */
	public boolean[] getSplit(int i)
	{		
		return splits[i];
	}


	/** get idGroup */
	public IdGroup getIdGroup()
	{		
		return idGroup;
	}

	/**
	  + test whether a split is contained in this split system
	  * (assuming the same leaf order)
	  *
	  * @param split split
	  */
	public boolean hasSplit(boolean[] split)
	{
		for (int i = 0; i < splitCount; i++)
		{
			if (SplitUtils.isSame(split, splits[i])) return true;
		}
			
		return false;
	}


	/** print split system */
	public String toString()
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		for (int i = 0; i < labelCount; i++)
		{
			pw.println(idGroup.getIdentifier(i));
		}
		pw.println();
		
		
		for (int i = 0; i < splitCount; i++)
		{
			for (int j = 0; j < labelCount; j++)
			{
				if (splits[i][j] == true)
					pw.print('*');
				else
					pw.print('.');
			}
			
			pw.println();
		}

		return sw.toString();
	}

	
	//
	// Private stuff
	//
	
	private int labelCount, splitCount;
	private IdGroup idGroup;
	private boolean[][] splits;
}


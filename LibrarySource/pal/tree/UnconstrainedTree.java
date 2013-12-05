// UnconstrainedTree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import pal.misc.*;


/**
 * provides parameter interface to an unconstrained tree
 * (parameters are all available branch lengths)
 *
 * @version $Id: UnconstrainedTree.java,v 1.5 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class UnconstrainedTree extends ParameterizedTree
{
	//
	// Public stuff
	//

	/**
	 * take any tree and afford it with an interface
	 * suitable for an unconstrained tree (parameters
	 * are all available branch lengths) 
	 */
	public UnconstrainedTree(Tree t)
	{
		setBaseTree(t);
		
		if (getRoot().getChildCount() < 3)
		{
			throw new IllegalArgumentException(
			"The root node must have at least three childs!");
		}
		
		// set default values
		for (int i = 0; i < getNumParameters(); i++)
		{
			setParameter(getDefaultValue(i), i);
		}
	}
	
	
	// interface Parameterized

	public int getNumParameters()
	{
		return getInternalNodeCount()+getExternalNodeCount()-1;
	}

	public void setParameter(double param, int n)
	{
		if (n < getExternalNodeCount())
		{
			getExternalNode(n).setBranchLength(param);
		}
		else
		{
			getInternalNode(n-getExternalNodeCount()).setBranchLength(param);
		}
	}

	public double getParameter(int n)
	{
		if (n < getExternalNodeCount())
		{
			return getExternalNode(n).getBranchLength();
		}
		else
		{
			return getInternalNode(n-getExternalNodeCount()).getBranchLength();
		}
	}

	public void setParameterSE(double paramSE, int n)
	{
		if (n < getExternalNodeCount())
		{
			getExternalNode(n).setBranchLengthSE(paramSE);
		}
		else
		{
			getInternalNode(n-getExternalNodeCount()).setBranchLengthSE(paramSE);
		}
	}

	public double getLowerLimit(int n)
	{
		return BranchLimits.MINARC;
	}
	
	public double getUpperLimit(int n)
	{
		return BranchLimits.MAXARC;
	}
	
	public double getDefaultValue(int n)
	{
		return BranchLimits.DEFAULT_LENGTH;
	}
}


// SplitUtils.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

import pal.misc.*;

/**
 * utilities for split systems
 *
 * @version $Id: SplitUtils.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class SplitUtils
{
	//
	// Public stuff
	//

	/**
	 * creates a split system from a tree
	 * (using a pre-specified order of sequences)
	 *
	 * @param idGroup  sequence order for the matrix
	 * @param tree
	 */
	public static SplitSystem getSplits(IdGroup idGroup, Tree tree)
	{
		tree.createNodeList();
		
		int size = tree.getInternalNodeCount()-1;
		SplitSystem splitSystem = new SplitSystem(idGroup, size);
		
		boolean[][] splits = splitSystem.getSplitVector();
		
		for (int i = 0; i < size; i++)
		{
			getSplit(idGroup, tree.getInternalNode(i), splits[i]);
		}
		
		
		return splitSystem;
	}



	/**
	 * creates a split system from a tree
	 * (using tree-induced order of sequences)
	 *
	 * @param tree
	 */
	public static SplitSystem getSplits(Tree tree)
	{
		IdGroup idGroup = TreeUtils.getLeafIdGroup(tree);
		
		return getSplits(idGroup, tree);
	}



	/**
	 * get split for branch associated with internal node
	 *
	 * @param idGroup order of labels
	 * @param internalNode Node
	 * @param boolean[] split
	 */
	public static void getSplit(IdGroup idGroup, Node internalNode, boolean[] split)
	{
		if (internalNode.isLeaf() || internalNode.isRoot())
		{
			throw new IllegalArgumentException("Only internal nodes (and no root) nodes allowed");
		}
		
		// make sure split is reset
		for (int i = 0; i < split.length; i++)
		{
			split[i] = false;
		}
		
		// mark all leafs downstream of the node
		
		for (int i = 0; i < internalNode.getChildCount(); i++)
		{
			markNode(idGroup, internalNode, split);
		}
		
		// standardize split (i.e. first index is alway true)
		if (split[0] == false)
		{
			for (int i = 0; i < split.length; i++)
			{
				if (split[i] == false)
					split[i] = true;
				else
					split[i] = false;
			}
		}		
	}

	/**
	 * checks whether two splits are identical
	 * (assuming they are of the same length
	 * and use the same leaf order)
	 *
	 * @param s1 split 1
	 * @param s2 split 2
	 */
	public static boolean isSame(boolean[] s1, boolean[] s2)
	{
		boolean reverse;
		if (s1[0] == s2[0]) reverse = false;
		else reverse = true;
		
		if (s1.length != s2.length) 
			throw new IllegalArgumentException("Splits must be of the same length!");
		
		for (int i = 0; i < s1.length; i++)
		{
			if (reverse)
			{
				// splits not identical
				if (s1[i] == s2[i]) return false;			
			}
			else
			{
				// splits not identical
				if (s1[i] != s2[i]) return false;
			}
		}
		
		return true;
	}

	//
	// Private stuff
	//
	
	private static void markNode(IdGroup idGroup, Node node, boolean[] split)
	{
		if (node.isLeaf())
		{
			String name = node.getIdentifier().getName();
			int index = idGroup.whichIdNumber(name);
			
			if (index < 0)
			{
				throw new IllegalArgumentException("INCOMPATIBLE IDENTIFIER (" + name + ")");
			} 
			
			split[index] = true;
		}
		else
		{
			for (int i = 0; i < node.getChildCount(); i++)
			{
				markNode(idGroup, node.getChild(i), split);
			}
		}
	}
	
}


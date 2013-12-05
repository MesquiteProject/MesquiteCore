// TreeDistanceMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

import pal.distance.*;
import pal.misc.*;


/**
 * computes distance matrix induced by a tree
 * (needs only O(n^2) time, following algorithm DistanceInTree by
 * D.Bryant and P. Wadell. 1998. MBE 15:1346-1359)
 * 
 *
 * @version $Id: TreeDistanceMatrix.java,v 1.8 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class TreeDistanceMatrix extends DistanceMatrix
{
	//
	// Public stuff
	//

	/**
	 * compute induced distance matrix
	 *
	 * @param idGroup  sequence order for the matrix
	 * @param t tree
	 * @param countEdges boolean variable deciding whether the actual
	 *                   branch lengths are used in computing the distance
	 *                   or whether simply all edges larger or equal a certain
	 *                   threshold length are counted (each with weight 1.0)
	 * @param epsilon    minimum branch length for a which an edge is counted
	 */
	public TreeDistanceMatrix(IdGroup idGroup, Tree t, boolean countEdges, double epsilon)
	{
		numSeqs = idGroup.getIdCount();
		this.idGroup = idGroup;
		tree = t;
		
		distance = new double[numSeqs][numSeqs];
		
		alias = TreeUtils.mapExternalIdentifiers(idGroup, tree);
		
		dist = new double[tree.getExternalNodeCount()];
		idist = new double[tree.getInternalNodeCount()];
		
		computeDistances(countEdges, epsilon);
	}

	/**
	 * compute induced distance matrix using actual branch lengths
	 *
	 * @param idGroup  sequence order for the matrix
	 * @param t tree
	 */
	public TreeDistanceMatrix(IdGroup idGroup, Tree t)
	{
		this(idGroup, t, false, 0.0);
	}

	/**
	 * compute induced distance matrix
	 * (using tree-induced order of sequences)
	 *
	 * @param t tree
	 * @param countEdges boolean variable deciding whether the actual
	 *                   branch lengths are used in computing the distance
	 *                   or whether simply all edges larger or equal a certain
	 *                   threshold length are counted (each with weight 1.0)
	 * @param epsilon    minimum branch length for a which an edge is counted
	 */
	public TreeDistanceMatrix(Tree t, boolean countEdges, double epsilon)
	{
		this(TreeUtils.getLeafIdGroup(t), t, countEdges, epsilon);
	}

	/**
	 * compute induced distance matrix using actual branch lengths
	 * (using tree-induced order of sequences)
	 *
	 * @param t tree
	 */
	public TreeDistanceMatrix(Tree t)
	{
		this(t, false, 0.0);
	}



	/** recompute distances (actual branch lengths) */
	public void computeDistances()
	{		
		computeDistances(false, 0.0);
	}
	
	/** recompute distances
	 * @param countEdges boolean variable deciding whether the actual
	 *                   branch lengths are used in computing the distance
	 *                   or whether simply all edges larger or equal a certain
	 *                   threshold length are counted (each with weight 1.0)
	 * @param epsilon    minimum branch length for a which an edge is counted
	 */	
	public void computeDistances(boolean countEdges, double epsilon)
	{		
		// fast O(n^2) computation of induced distance matrix
		for (int i = 0; i < tree.getExternalNodeCount(); i++)
		{
			TreeUtils.computeAllDistances(tree, i, dist, idist, countEdges, epsilon);
			int ai = alias[i];
			
			for (int j = 0; j < tree.getExternalNodeCount(); j++)
			{
				distance[ai][alias[j]] = dist[j];				
			}
		}

	}
	
	//
	// Private stuff
	//
	
	private int[] alias;
	private Tree tree;
	private double[] dist, idist;
}


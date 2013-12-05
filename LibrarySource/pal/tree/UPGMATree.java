// UPGMATree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

// Known bugs and limitations:
// - computational complexity O(numSeqs^3)
//   (this could be brought down to O(numSeqs^2)
//   but this needs more clever programming ...)


package pal.tree;

import pal.distance.*;

/**
 * constructs a UPGMA tree from pairwise distances
 *
 * @version $Id: UPGMATree.java,v 1.9 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class UPGMATree extends SimpleTree
{
	//
	// Public stuff
	//	

	/**
	 * constructor UPGMA tree
	 *
	 * @param m distance matrix
	 */
	public UPGMATree(DistanceMatrix m)
	{
		if (m.numSeqs < 2)
		{
			new IllegalArgumentException("LESS THAN 2 TAXA IN DISTANCE MATRIX");
		}
		if (!m.isSymmetric())
		{
			new IllegalArgumentException("UNSYMMETRIC DISTANCE MATRIX");
		}
		
		init(m);

		while (true)
		{
			findNextPair();
			newBranchLengths();
			
			if (numClusters == 2)
			{
				break;
			}
			
			newCluster();
		}
		
		finish();
		createNodeList();
	}


	//
	// Private stuff
	//
	
	private int numClusters;
	private Node newCluster;
	private int besti, abi;
	private int bestj, abj;
	private int[] alias;
	private double[][] distance;

	private double[] height;
	private int[] oc;

	private double getDist(int a, int b)
	{
		return distance[alias[a]][alias[b]];
	}
	
	private void init(DistanceMatrix m)
	{
		numClusters = m.numSeqs;

		distance = new double[numClusters][numClusters];
		for (int i = 0; i < numClusters; i++)
		{
			for (int j = 0; j < numClusters; j++)
			{
				distance[i][j] = m.distance[i][j];
			}
		}

		for (int i = 0; i < numClusters; i++)
		{
			Node tmp = NodeFactory.createNode();
			tmp.setIdentifier(m.getIdentifier(i));
			getRoot().addChild(tmp);
		}
		
		alias = new int[numClusters];
		for (int i = 0; i < numClusters; i++)
		{
			alias[i] = i;
		}
				
		height = new double[numClusters];
		oc = new int[numClusters];
		for (int i = 0; i < numClusters; i++)
		{
			height[i] = 0.0;
			oc[i] = 1;
		}
	}

	private void finish()
	{
		distance = null;		
	}

	private void findNextPair()
	{
		besti = 0;
		bestj = 1;
		double dmin = getDist(0, 1);
		for (int i = 0; i < numClusters-1; i++)
		{
			for (int j = i+1; j < numClusters; j++)
			{
				if (getDist(i, j) < dmin)
				{
					dmin = getDist(i, j);
					besti = i;
					bestj = j;
				}
			}
		}
		abi = alias[besti];
		abj = alias[bestj];
	}

	private void newBranchLengths()
	{
		double dij = getDist(besti, bestj);
		
		getRoot().getChild(besti).setBranchLength(dij/2.0-height[abi]);
		getRoot().getChild(bestj).setBranchLength(dij/2.0-height[abj]);
	}

	private void newCluster()
	{
		// Update distances
		for (int k = 0; k < numClusters; k++)
		{
			if (k != besti && k != bestj)
			{
				int ak = alias[k];	
				distance[ak][abi] = distance[abi][ak] = updatedDistance(besti, bestj, k);
			}
		}
		distance[abi][abi] = 0.0;

		// Update UPGMA variables
		height[abi] = getDist(besti, bestj)/2.0;
		oc[abi] += oc[abj];
		
		// Index besti now represent the new cluster
		NodeUtils.joinChilds(getRoot(), besti, bestj);
		
		// Update alias
		for (int i = bestj; i < numClusters-1; i++)
		{
			alias[i] = alias[i+1];
		}
		
		numClusters--;
	}

	
	/**
	 * compute updated distance between the new cluster (i,j)
	 * to any other cluster k
	 */
	private double updatedDistance(int i, int j, int k)
	{
		int ai = alias[i];
		int aj = alias[j];
		
		double ocsum = (double) (oc[ai]+oc[aj]);
		
		return 	(oc[ai]/ocsum)*getDist(k, i) +
			(oc[aj]/ocsum)*getDist(k, j);
	}
}


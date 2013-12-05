// SUPGMATree.java
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
import pal.misc.*;

/**
 * constructs an SUPGMA tree from pairwise distances. <BR>
 * Reference: <BR>
 * Alexei Drummond and Allen G. Rodrigo (2000). Reconstructing Genealogies of Serial Samples Under the Assumption of a Molecular Clock Using Serial-Sample UPGMA. Molecular Biology and Evolution 17:1807-1815
 *
 * @version $Id: SUPGMATree.java,v 1.6 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class SUPGMATree extends UPGMATree
{
	//
	// Public stuff
	//	

	/**
	 * constructor SUPGMA tree
	 *
	 * @param m *uncorrected* distance matrix
	 */
	public SUPGMATree(DistanceMatrix m, TimeOrderCharacterData tocd, double rate) {
		
		this(m, tocd, rate, true);	
	}


	/**
	 * constructor SUPGMA tree
	 *
	 * @param m *uncorrected* distance matrix
	 */
	public SUPGMATree(DistanceMatrix m, TimeOrderCharacterData tocd, double rate, boolean allowNegatives) {
		
		super(new SUPGMADistanceMatrix(m, tocd, rate));
		
		this.tocd = tocd;

		IdGroup idgroup = tocd.getIdGroup();
		
		createNodeList();
		
		
		/*//go through and check for maximum allowed rate
		if (!allowNegatives) {
			for (int i = 0; i < getExternalNodeCount(); i++) {
				int index = idgroup.whichIdNumber(getExternalNode(i).getIdentifier().getName());
				double time = tocd.getTime(index);
				
				double maxRateThisTip = getExternalNode(i).getParent().getNodeHeight() / time;

				if (maxRateThisTip < rate) {
					rate = maxRateThisTip;
				}
			}
		}*/

		// go through and set heights.
		for (int i = 0; i < getExternalNodeCount(); i++) {
			int index = idgroup.whichIdNumber(getExternalNode(i).getIdentifier().getName());
			getExternalNode(i).setNodeHeight(tocd.getHeight(index, rate));		
			if (!allowNegatives) {
				if (getExternalNode(i).getParent().getNodeHeight() < getExternalNode(i).getNodeHeight()) {
					fixHeight(getExternalNode(i).getParent(), getExternalNode(i).getNodeHeight());
				}
			}
		}
	}
	
	public DatedTipsClockTree getDatedTipsClockTree() {
		return new DatedTipsClockTree(this, tocd, false);	
	}

	private void fixHeight(Node node, double height) {
		node.setNodeHeight(height);
		if (!node.isRoot()) {
			if (node.getParent().getNodeHeight() < height) {
				fixHeight(node.getParent(), height);
			}
		}
	}
		
	private TimeOrderCharacterData tocd;
}


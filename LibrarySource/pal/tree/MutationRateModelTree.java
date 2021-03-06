// MutationRateModelTree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)
//

package pal.tree;

import pal.misc.*;
import pal.mep.*;


import pal.util.*;

/**
 * Provides parameter interface to any clock-like tree with
 * serially sampled tips (parameters are the minimal node height differences
 * at each internal node). Any mutation rate model can be used. <P>
 * @see pal.mep.MutationRateModel
 *
 * @version $Id: MutationRateModelTree.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class MutationRateModelTree extends ParameterizedTree {

	//
	// Public stuff
	//

	TimeOrderCharacterData tocd = null;
	MutationRateModel model = null;
	int numParameters;
	
	//private

	private double MIN_MU = 1e-12;
	private double MIN_DELTA = 1e-12;
	private double lnL = 0.0; 

	/**
	 * take any tree and afford it with an interface
	 * suitable for a clock-like tree (parameters
	 * are the minimal node height differences at each internal node).
	 * <p>
	 * <em>This parameterisation of a clock-tree, ensuring that
	 * all parameters are independent of each other is due to
	 * Andrew Rambaut (personal communication).</em>
	 */
	public MutationRateModelTree(Tree t, TimeOrderCharacterData tocd, MutationRateModel model) throws RuntimeException {

  		setBaseTree(t);
	
		this.tocd = tocd;
		this.model = model;
		
		if (t.getRoot().getChildCount() < 2) {
			throw new RuntimeException(
				"The root node must have at least two childs!");
		}
		
		NodeUtils.heights2Lengths(getRoot());
		
		numParameters = getInternalNodeCount() + model.getNumParameters();
		
		
		if (!tocd.hasTimes()) {
			throw new RuntimeException("Must have times!");
		}
		
		parameter = new double[numParameters - model.getNumParameters()];
		heights2parameters();
	}
	
	// interface Parameterized
	
	public int getNumParameters() {
		return numParameters;
	}
   
	public void setParameter(double param, int n) {
	
		if (n < getInternalNodeCount()) {
			parameter[n] = param;
		} else model.setParameter(param, n - getInternalNodeCount());

		// call this parameter2Heights
		parameters2Heights();
		NodeUtils.heights2Lengths(getRoot());
	}
	
	public double getParameter(int n) {
		if (n < getInternalNodeCount()) {
			return parameter[n];
		} else {
			return model.getParameter(n - getInternalNodeCount());
		}
	}

	/**
	 * Returns lower limit of parameter estimate.
	 */
	public double getLowerLimit(int n) {
		if (n < getInternalNodeCount()) {
			return BranchLimits.MINARC;
		} else {
			return MIN_MU;
		}
	}

	//
	// Private stuff
	//
	
	private double[] parameter;

	public double getDefaultValue(int n) {
		return BranchLimits.DEFAULT_LENGTH;
	}

	public void setParameterSE(double paramSE, int n) {
		return; // DEBUG - not yet done
	}

	public double getUpperLimit(int n) {
		return BranchLimits.MAXARC;
	}

	/**
	 * returns mu
	 */
	public MutationRateModel getMutationRateModel() {
		return model;
	}

	protected void parameters2Heights() {
		// nodes have been stored by a post-order traversal

		int index;
	
		for (int i = 0; i < getExternalNodeCount(); i++) {
	
			index = tocd.getIdGroup().whichIdNumber(getExternalNode(i).getIdentifier().getName());
			//System.err.println(index + ":" + i);

			getExternalNode(i).setNodeHeight(model.getExpectedSubstitutions(tocd.getTime(index)));	
		}

		// this could be more efficient
		for (int i = 0; i < getInternalNodeCount(); i++) {
			Node node = getInternalNode(i);
			node.setNodeHeight(parameter[i] + NodeUtils.findLargestChild(node));
		}
	}

	protected void heights2parameters() {
		for (int i = 0; i < getInternalNodeCount(); i++) {
			Node node = getInternalNode(i);
			parameter[i] = node.getNodeHeight()-NodeUtils.findLargestChild(node);
		}

		// need to convert heights to model parameters somehow!	
	}	

	public void setLnL(double lnL) {
		this.lnL = lnL; 
	}

	public double getLnL() {
		return lnL;
	}
}


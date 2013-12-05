// DemographicClockTree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.coalescent;

import pal.misc.*;
import pal.tree.*;

/**
 * Provides parameter interface to a clock-like genealogy which is
 * assumed to have some demographic pattern of theta (diversity) as 
 * well as branch parameters (the minimal node height differences
 * at each internal node).
 *
 * <em>Must be used in conjunction with DemographicLikelihoodFunction! </em>
 *
 * @author Alexei Drummond
 */
public class DemographicClockTree extends ClockTree implements DemographicTree {
	
	//
	// Public stuff
	//

	DemographicModel model;

	/**
	 * take any tree and afford it with an interface
	 * suitable for a clock-like genealogy, under a certain demographic
	 * assumption.
	 * <p>
	 * <em>This parameterisation of branches, ensuring that
	 * all parameters are independent of each other is due to
         * Andrew Rambaut (personal communication).</em>
	 */
	public DemographicClockTree(Tree t, DemographicModel model)
	{
		setBaseTree(t);

		this.model = model;

		if (t.getRoot().getChildCount() < 2)
		{
			throw new IllegalArgumentException(
			"The root node must have at least two childs!");
		}
				
		NodeUtils.heights2Lengths(getRoot());

		parameter = new double[getInternalNodeCount() + model.getNumParameters()];
		heights2parameters();
	}

	/**
	 * Returns the likelihood of the current demographic model, given
	 * the current branch lengths.
	 */
	public double computeDemoLogLikelihood() {
	
		CoalescentIntervals ci = IntervalsExtractor.extractFromTree(this);
	
		double value = ci.computeLogLikelihood(model);
		
		return value;
	}
	
	// interface Parameterized

	public int getNumParameters()
	{
		return getInternalNodeCount() + model.getNumParameters();
	}		
	
	//
	// Private stuff
	//
		
	protected void parameters2Heights()
	{
		super.parameters2Heights();
	
		for (int i = 0; i < model.getNumParameters(); i++) {
			model.setParameter(parameter[getInternalNodeCount() + i], i);
		}
	}
	
	protected void heights2parameters()
	{
		super.heights2parameters();
		
		if (model != null) {
			for (int i = 0; i < model.getNumParameters(); i++) {
				parameter[getInternalNodeCount() + i] = model.getParameter(i);
			}
		}
	}

	/** 
	 * Return the demographic model being used to optimize the
	 * likelihood of this tree.
	 */
	public DemographicModel getDemographicModel() {
		return model;
	}
}


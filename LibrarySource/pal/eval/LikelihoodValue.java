// LikelihoodValue.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.eval;

import pal.alignment.*;
import pal.substmodel.*;
import pal.tree.*;
import pal.distance.*;
import pal.math.*;
import pal.misc.*;

import java.io.*;
import java.util.*;


/**
 * Computes the likelihood for a tree given a
 * model of sequence evolution and a sequence alignment;
 * also optimises tree parameters such as branch lengths
 * by maximising the likelihood (for optimal performance
 * special optimisation procedures are
 * employed for UnconstrainedTree, ClockTree and DatedTipsClockTree;
 * a general optimisation precedure is used for another
 * ParameterizedTree). 
 *
 * @version $Id: LikelihoodValue.java,v 1.32 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class LikelihoodValue
{
	//
	// Public stuff
	//

	/** Log-Likelihood */
	public double logL;
	
	/** log-likelihood for each site pattern */
	public double[] siteLogL;
	
	/** map estimation of rate at site pattern */
	public int[] rateAtSite;


	/**
	 * initialization
	 *
	 * @param sp site pattern
	 */
	public LikelihoodValue(SitePattern sp)
	{	
		sitePattern = sp;
		
		numPatterns = sp.numPatterns;
		siteLogL = new double[numPatterns];
		rateAtSite = new int[numPatterns];
	}

	/**
	 * Returns the site pattern of this likelihood value
	 */
	public SitePattern getSitePattern() {
		return sitePattern;
	}


	/**
	 * Set new site pattern (while keeping tree and model)
	 */
	public void renewSitePattern(SitePattern sp)
	{
		sitePattern = sp;
		
		numPatterns = sp.numPatterns;
		siteLogL = new double[numPatterns];
		rateAtSite = new int[numPatterns];

		setModel(getModel());
		setTree(getTree());
	}
	 

	/**
	 * define model
	 * (a site pattern must have been set before calling this method)
	 *
	 * @param m model of substitution (rate matrix + rate distribution)
	 */
	public void setModel(SubstitutionModel m)
	{
		model = m;
			
		frequency = model.rateMatrix.getEqulibriumFrequencies();
		rprob = model.rateDistribution.probability;
		numStates = model.dimension;
		numRates = model.numRates;
				
		int maxNodes = 2*sitePattern.getSequenceCount()-2;
		
		allocatePartialMemory(maxNodes);
	}

	/**
	 * Returns the model of this likelihood value.
	 */
	public SubstitutionModel getModel() {
		return model;
	}


	/**
	 * define (parameterized) tree
	 *,(must only be called only after a site pattern has been defined).
	 *
	 * @param t tree
	 */
	public void setTree(Tree t)
	{
		tree = t;
		
		// Assign sequences to leaves
		int[] alias = 
		    TreeUtils.mapExternalIdentifiers(sitePattern, tree);
		
		for (int i = 0; i < tree.getExternalNodeCount(); i++)
		{
			tree.getExternalNode(i).setSequence(sitePattern.pattern[alias[i]]);	
		}
		
		if (tree instanceof ParameterizedTree)
		{
			ptree = (ParameterizedTree) tree;
			numParams = ptree.getNumParameters();
		}
		else
		{
			ptree = null;
			numParams = 0;
		}
	}

	/**
	 * Returns the (potentially parameterized) tree of this likelihood value.
	 */
	public Tree getTree()
	{
		return tree;
	}

	
	/**
	 * compute log-likelihood for current tree (fixed branch lengths and model)
	 *
	 * return log-likelihood
	 */
	public double compute()
	{		
		treeLikelihood();

		return logL;
	}


	/**
	 * optimise parameters of tree by maximising its likelihood
	 * (this assumes that tree is a ParameterizedTree)
	 *
	 * @return minimimum log-likelihood value 
	 */
	public double optimiseParameters()
	{
		return optimiseParameters(null);
	}


	/**
	 * optimise parameters of tree by maximising its likelihood
	 * (this assumes that tree is a ParameterizedTree)
	 *
	 * @param mm optimiser for generic ParameterisedTree
	 *
	 * @return minimimum log-likelihood value 
	 */
	public double optimiseParameters(MultivariateMinimum mm)
	{		
		if (!(tree instanceof ParameterizedTree))
		{
			// we need a ParameterizedTree here!
			new IllegalArgumentException("ParameterizedTree required");
		}

		double[] estimate = new double[numParams];

		// we need these in any case
		if (um == null) um = new UnivariateMinimum();
		if (tl == null) tl = new TreeLikelihood(this);
		
		if (bl == null) bl = new BranchLikelihood(this);
		else bl.update();
	
		if (tree instanceof UnconstrainedTree)
		{
			optimiseUnconstrainedTree(true);
		}
		else if (tree instanceof ClockTree)
		{
			if (nl == null) nl = new NodeLikelihood(this);
			else nl.update();
			
			int ns;			
			do
			{
				optimiseClockTree(false);
							
				ns = collapseShortInternalBranches();
				
				// make consistent
				((ClockTree) ptree).update();
								
				numParams = numParams - ns;
			}
			while (ns != 0);
			
			numParams += restoreShortInternalBranches();
			
			// make consistent
			((ClockTree) ptree).update();
		}
		else if (tree instanceof DatedTipsClockTree)
		{
			if (nl == null) nl = new NodeLikelihood(this);
			else nl.update();
			if (rl == null) rl = new RateLikelihood(this);
			else rl.update();
						
			int ns;			
			do
			{
				optimiseClockTree(true);
			
				ns = collapseShortInternalBranches();
				
				// make consistent
				((DatedTipsClockTree) ptree).update();
				
				numParams = numParams - ns;
			}
			while (ns != 0);
			
			numParams += restoreShortInternalBranches();
			
			// make consistent
			((DatedTipsClockTree) ptree).update();
		}
		else
		{
			for (int i = 0; i < numParams; i++)
			{
				estimate[i] = ptree.getParameter(i);
			}

			if (mm == null)
			{
				if (mvm == null) mvm = new DifferentialEvolution(numParams);
			}
			else
			{
				mvm = mm;
			}
			mvm.findMinimum(tl, estimate, BranchLimits.FRACDIGITS,
				BranchLimits.FRACDIGITS);
		}
	
		// compute estimates for SEs of branch lengths
		optimiseUnconstrainedTree(false);
		
		for (int i = 0; i < numParams; i++)
		{
			estimate[i] = ptree.getParameter(i);
		}
				
		return -tl.evaluate(estimate);
	}

	
	//
	// Friendly stuff
	//

	int numStates;
	int numRates;
	int numPatterns;
	double[] frequency;
	double[] rprob;
	SitePattern sitePattern;
	int numParams;
	Tree tree;
	ParameterizedTree ptree;

	/** get partial likelihood of a branch */
	double[][][] getPartial(Node branch)
	{
		return partials[getKey(branch)];
	}
	
	/** get next branch around a center node
	   (center may be root, but root is never returned) */
	Node getNextBranch(Node branch, Node center)
	{		
		Node b = getNextBranchOrRoot(branch, center);
		
		if (b.isRoot())
		{
			b = b.getChild(0);
		}
		
		return b;
	}
		
	/** multiply partials into the neighbour of branch */
	void productPartials(Node branch, Node center)
	{		
		int numBranches = getBranchCount(center);
		
		Node nextBranch = getNextBranch(branch, center);
		double[][][] partial = getPartial(nextBranch);
		
		for (int i = 0; i < numBranches-2; i++)
		{
			nextBranch = getNextBranch(nextBranch, center);
			double[][][] partial2 = getPartial(nextBranch);
			
			for (int l = 0; l < numPatterns; l++)
			{
				for (int r = 0; r < numRates; r++)
				{
					double[] p = partial[l][r];
					double[] p2 = partial2[l][r];

					for (int d = 0; d < numStates; d++)
					{ 
						p[d] *= p2[d];
					}
						
				}
			}
		}
	}	
	

	/** compute partials for branch around center node
	    (it is assumed that multiplied partials are available in
	    the neighbor branch) */
	void partialsInternal(Node branch, Node center)
	{
		double[][][] partial = getPartial(branch);
		double[][][] multPartial = getPartial(getNextBranch(branch, center));
		
		model.setDistance(branch.getBranchLength());
		for (int l = 0; l < numPatterns; l++)
		{
			for (int r = 0; r < numRates; r++)
			{
				double[] p = partial[l][r];
				double[] mp = multPartial[l][r];
				
				for (int d = 0; d < numStates; d++)
				{
					double sum = 0;
					for (int j = 0; j < numStates; j++)
					{
						sum += model.transProb(r, d, j)*mp[j];
					}
					p[d] = sum;
				}
			}
		}		
	}

	/** compute partials for external branch */
	void partialsExternal(Node branch)
	{
		double[][][] partial = getPartial(branch);
		byte[] seq = branch.getSequence();
		
		model.setDistance(branch.getBranchLength());
		
		for (int l = 0; l < numPatterns; l++)
		{
			for (int r = 0; r < numRates; r++)
			{
				double[] p = partial[l][r];
				int sl = seq[l];
				
				if (sl == numStates)
				{
					for (int d = 0; d < numStates; d++)
					{ 
						p[d] = 1;	
					}		
				}
				else
				{
					for (int d = 0; d < numStates; d++)
					{ 
						p[d] = model.transProb(r, d, sl);	
					}		
				}				
			}
		}
	}



	//
	// Private stuff
	//

	// max. number of iterations in ml optimization
	private int MAXROUNDS = 1000;
	
	private SubstitutionModel model;
	private AlignmentDistanceMatrix distMat;
	private double[][][][] partials;
	private boolean down;
	private Node currentBranch;
	private UnivariateMinimum um;
	private MultivariateMinimum mvm;
	private BranchLikelihood bl;
	private TreeLikelihood tl;
	private NodeLikelihood nl;
	private RateLikelihood rl;
		
	private void allocatePartialMemory(int numNodes) {

		// I love the profiler!
		// This 'if' statement sped my MCMC algorithm up by nearly 300%
		// Never underestimate the time it takes to allocate and de-allocate memory!
		// AD 
		if (
			(partials == null) || 
			(numNodes != partials.length) ||
			(numPatterns != partials[0].length) ||
			(numRates != partials[0][0].length) ||
			(numStates != partials[0][0][0].length)) {
				
			partials = new double[numNodes][numPatterns][numRates][numStates];
		}
	}

	/** get next branch around a center node
	   (center may be root, and root may also be returned) */
	private Node getNextBranchOrRoot(Node branch, Node center)
	{		
		int numChilds = center.getChildCount();
		
		int num;
		for (num = 0; num < numChilds; num++)
		{
			if (center.getChild(num) == branch)
			{
				break;
			}
		}
		
		// num is now child number (if num = numChilds then branch == center)
		
		// next node
		num++;
		
		if (num > numChilds)
		{
			num = 0;
		}
		
		if (num == numChilds)
		{
			return center;
		}
		else
		{
			return center.getChild(num);
		}
	}

	private int getKey(Node node)
	{
		int key;
		if (node.isLeaf())
		{
			key = node.getNumber();
		}
		else
		{
			key = node.getNumber() + tree.getExternalNodeCount();
		}
		
		return key;		
	}
	
	/** returns number of branches centered around an internal node */
	private int getBranchCount(Node center)
	{
		if (center.isRoot())
		{
			return center.getChildCount();
		}
		else
		{
			return center.getChildCount()+1;
		}
	}
	
	private void traverseTree()
	{
		if ((!currentBranch.isLeaf() && down) || currentBranch.isRoot()) 
		{
			currentBranch = currentBranch.getChild(0);
			down = true;
		}
		else
		{
			Node center = currentBranch.getParent();
			currentBranch = getNextBranchOrRoot(currentBranch, center);
			
			if (currentBranch == center)
			{
				down = false;
			}
			else
			{
				down = true;
			}
		}
	}
	
	/** init partial likelihoods */
	private void initPartials()
	{
		currentBranch = tree.getRoot();
		down = true;
		
		Node firstBranch = currentBranch;
				
		do
		{	
			if (currentBranch.isRoot())
			{
				//do nothing
			}
			else
			if (currentBranch.isLeaf())
			{
				partialsExternal(currentBranch);
			}
			else if (!down)
			{
				productPartials(currentBranch, currentBranch);
				partialsInternal(currentBranch, currentBranch);
			}
			
			traverseTree();
		}
		while (currentBranch != firstBranch);
	}

	/** calculate likelihood of any tree and infer MAP estimates of rates at a site */
	private void treeLikelihood()
	{
		initPartials();
		
		Node center = tree.getRoot();
				
		Node firstBranch = center.getChild(0);
		Node lastBranch = center.getChild(center.getChildCount()-1);
		
		double[][][] partial1 = getPartial(firstBranch);
		double[][][] partial2 = getPartial(lastBranch);

		productPartials(lastBranch, center);

		logL = 0;
		for (int l = 0; l < numPatterns; l++)
		{
			int bestR = 0;
			double maxSum = 0;
			
			double rsum = 0.0;
			for (int r = 0; r < numRates; r++)
			{
				double[] p1 = partial1[l][r];
				double[] p2 = partial2[l][r];
				
				double sum = 0.0;
				for (int d = 0; d < numStates; d++)
				{ 
					sum += frequency[d]*p1[d]*p2[d];
				}
				sum *= rprob[r];
				
				// find rate category that contributes the most
				if (r == 0)
				{
					bestR = 0;
					maxSum = sum;
				}
				else
				{
					if (sum > maxSum)
					{
						bestR = r;
						maxSum = sum;
					}
				}
				
				rsum += sum;	
			}
			
			siteLogL[l] = Math.log(rsum);
			rateAtSite[l] =  bestR;
			logL += siteLogL[l]*sitePattern.weight[l];
		}
		
	}

	/** optimise branch lengths and find SEs (UnconstrainedTree) */
	private void optimiseUnconstrainedTree(boolean optimise)
	{
		int numBranches = tree.getInternalNodeCount() + tree.getExternalNodeCount()-1;
		
		initPartials();		
				
		Node firstBranch = currentBranch;
		double len, lenOld, lenDiff;
		int nconv = 0;
		
		int numRounds = 0;
		
		double lenSE;
		double INVARC = 1.0/(BranchLimits.MAXARC*BranchLimits.MAXARC);
		do
		{
			if (currentBranch.isRoot())
			{
				// do nothing
			}	
			else if (currentBranch.isLeaf())
			{
				productPartials(currentBranch, currentBranch.getParent());
				bl.setBranch(currentBranch);
				lenOld = currentBranch.getBranchLength();
				
				//optimise
				if (optimise)
				{
					len = um.findMinimum(lenOld, bl, BranchLimits.FRACDIGITS);
					currentBranch.setBranchLength(len);
				}
				else
				{
					// find corresponding SE
					len = lenOld;
					lenSE = NumericalDerivative.secondDerivative(bl, lenOld);
					if (INVARC < lenSE)
						lenSE = Math.sqrt(1.0/lenSE);
					else
						lenSE = BranchLimits.MAXARC;
					currentBranch.setBranchLengthSE(lenSE);
				}
								
				// check progress				
				lenDiff = Math.abs(len-lenOld);
				if (lenDiff < BranchLimits.ABSTOL) nconv++;
				else nconv = 0;
				
				if (nconv >= numBranches || numRounds == MAXROUNDS)
				{
					bl.evaluate(len);
					break;
				}				
				
				// update partials
				partialsExternal(currentBranch);
			}
			else if (down)
			{
				productPartials(currentBranch, currentBranch.getParent());
				partialsInternal(currentBranch, currentBranch.getParent());				
			}
			else // !down
			{
				productPartials(currentBranch, currentBranch);
				bl.setBranch(currentBranch);
				lenOld = currentBranch.getBranchLength();
				
				//optimise
				if (optimise)
				{
					len = um.findMinimum(lenOld, bl, BranchLimits.FRACDIGITS);
					currentBranch.setBranchLength(len);
				}
				else
				{
					// find corresponding SE
					len = lenOld;
					lenSE = NumericalDerivative.secondDerivative(bl, lenOld);
					if (INVARC < lenSE)
						lenSE = Math.sqrt(1.0/lenSE);
					else
						lenSE = BranchLimits.MAXARC;
					currentBranch.setBranchLengthSE(lenSE);
				}
				
				// check progress
				lenDiff = Math.abs(len-lenOld);
				if (lenDiff < BranchLimits.ABSTOL) nconv++;
				else nconv = 0;
				
				if (nconv >= numBranches || numRounds == MAXROUNDS)
				{
					bl.evaluate(len);
					break;
				}
				
				// update branch length and partials
				partialsInternal(currentBranch, currentBranch);
			}
			
			traverseTree();
			
			if (currentBranch == firstBranch) numRounds++;
		}
		while (true);
	}
	
	private Vector shortBranches = null;

	/** collapse internal branches that are close to zero */
	private int collapseShortInternalBranches()
	{
		// minus 1 because root node has no own branch
		int numInternalBranches = tree.getInternalNodeCount()-1;
		int numShortBranches = 0;
		for (int i = 0; i < numInternalBranches; i++)
		{
			Node b = tree.getInternalNode(i);
			if (b.getBranchLength() <= 2*BranchLimits.MINARC)
			{
				numShortBranches++;
				
				NodeUtils.removeBranch(b);
				
				if (shortBranches == null) shortBranches = new Vector();
				
				shortBranches.addElement(b);	
			}
		}
		
		//numParams = numParams - numShortBranches;
		tree.createNodeList();
		
		return numShortBranches;
	}
	
	/** restore internal branches */
	private int restoreShortInternalBranches()
	{
		int size = 0;
		
		if (shortBranches != null)
		{
			size = shortBranches.size();
			for (int i = size-1; i >= 0; i--)
			{
				Node node = (Node) shortBranches.elementAt(i);
				NodeUtils.restoreBranch(node);
				node.setBranchLength(BranchLimits.MINARC);
				node.setNodeHeight(node.getParent().getNodeHeight()-BranchLimits.MINARC);
				shortBranches.removeElementAt(i);
			}
		}
		
		//numParams = numParams+size;
		tree.createNodeList();
		
		return size;
	}

	
	/** optimise branch lengths (ClockTree) */
	private void optimiseClockTree(boolean datedTips)
	{
		int numNodes = tree.getInternalNodeCount();
		
		double MAXHEIGHT = numNodes*BranchLimits.MAXARC;
				
		initPartials();		
				
		Node firstBranch = currentBranch;
		double h, hOld, hDiff, hMin, hMax, hSE;
		int nconv = 0;
		
		int numRounds = 0;
		
		double INVMAX = 1.0/(MAXHEIGHT*MAXHEIGHT);
		do
		{
			if (currentBranch.isRoot())
			{
				if (datedTips && numRounds > 0)
				{
					// in the first round we did not adjust the rate
					// so we assume that the likelihood has not converged
					if (numRounds == 1) nconv = 0;
					//nconv = 0;
					
					double oldLogL = logL;
					
					// optimise rate
					DatedTipsClockTree dtree = (DatedTipsClockTree) ptree;
					double rOld = dtree.getRate();
					double maxR = dtree.getMaxRate();
					double r = um.findMinimum(rOld, rl);
					rl.evaluate(r);	
					
					// find corresponding SE	
					double rSE = um.f2minx;
					if (1 < rSE)
						rSE = Math.sqrt(1.0/rSE);
					else
						rSE = 1;
					dtree.setRateSE(rSE);
					
					// check progress				
					/*double logLDiff = Math.abs(logL-oldLogL);
					if (logLDiff > 0.001)
					{
						// reset 
						nconv = 0;
					}*/
				}
						
				// min-max heights
				hMin = NodeUtils.findLargestChild(currentBranch)+BranchLimits.MINARC;
				hMax = MAXHEIGHT-BranchLimits.MINARC;
				
				//optimise
				nl.setBranch(currentBranch, hMin, hMax);
				hOld = currentBranch.getNodeHeight();
				h = um.findMinimum(hOld, nl, BranchLimits.FRACDIGITS);
				nl.evaluate(h);
								
				// find corresponding SE	
				hSE = um.f2minx;
				if (INVMAX < hSE)
					hSE = Math.sqrt(1.0/hSE);
				else
					hSE = MAXHEIGHT;
				currentBranch.setNodeHeightSE(hSE);
				
				// check progress				
				hDiff = Math.abs(h-hOld);
				if (hDiff < BranchLimits.ABSTOL) nconv++;
				else nconv = 0;
				
				if (nconv >= numNodes || numRounds == MAXROUNDS)
				{
					break;
				}
			}	
			else if (currentBranch.isLeaf())
			{
				productPartials(currentBranch, currentBranch.getParent());
				partialsExternal(currentBranch);
			}
			else if (down)
			{
				productPartials(currentBranch, currentBranch.getParent());
				
				// min-max heights
				hMin = NodeUtils.findLargestChild(currentBranch)+BranchLimits.MINARC;
				hMax = currentBranch.getParent().getNodeHeight()-BranchLimits.MINARC;
				
				//optimise
				nl.setBranch(currentBranch, hMin, hMax);
				hOld = currentBranch.getNodeHeight();
				h = um.findMinimum(hOld, nl, BranchLimits.FRACDIGITS);
				nl.evaluate(h);
								
				// find corresponding SE	
				hSE = um.f2minx;
				if (INVMAX < hSE)
					hSE = Math.sqrt(1.0/hSE);
				else
					hSE = MAXHEIGHT;
				currentBranch.setNodeHeightSE(hSE);
				
				// check progress				
				hDiff = Math.abs(h-hOld);
				if (hDiff < BranchLimits.ABSTOL) nconv++;
				else nconv = 0;
				
				if (nconv >= numNodes || numRounds == MAXROUNDS)
				{
					break;
				}				
				
				partialsInternal(currentBranch, currentBranch.getParent());				
			}
			else // !down
			{
				productPartials(currentBranch, currentBranch);
				partialsInternal(currentBranch, currentBranch);
			}
			
			traverseTree();
			
			if (currentBranch == firstBranch) numRounds++;
		}
		while (true);
	}
}

class RateLikelihood implements UnivariateFunction
{
	public RateLikelihood(LikelihoodValue lv)
	{
		this.lv = lv;
		update();
	}
	
	public void update()
	{
		dtree = (DatedTipsClockTree) lv.ptree;
	}
	
	public double evaluate(double param)
	{
		// set rate parameters
		dtree.setRate(param);

		return -lv.compute();
	}
		
	public double getLowerBound()
	{
		return 0;
	}

	public double getUpperBound()
	{
		return dtree.getMaxRate();
	}
	
	// private stuff
	private LikelihoodValue lv;
	private DatedTipsClockTree dtree;
}

class TreeLikelihood implements MultivariateFunction
{



	public TreeLikelihood(LikelihoodValue lv)
	{
		this.lv = lv;
	}

	public double evaluate(double[] params)
	{
		// set tree parameters
		for (int i = 0; i < lv.numParams; i++)
		{
			lv.ptree.setParameter(params[i], i);
		}

		return -lv.compute();
	}

	public int getNumArguments()
	{
		return lv.numParams;
	}

	public double getLowerBound(int n)
	{
		return lv.ptree.getLowerLimit(n);
	}

	public double getUpperBound(int n)
	{
		return lv.ptree.getUpperLimit(n);
	}

	// private stuff
	private LikelihoodValue lv;
}

class ModelLikelihood implements MultivariateFunction
{

	public ModelLikelihood(LikelihoodValue lv)
	{
		this.lv = lv;
		this.model_ = lv.getModel();
	}

	public double evaluate(double[] params)
	{
		// set tree parameters
		for (int i = 0; i < lv.numParams; i++)
		{
			model_.setParameter(params[i], i);
		}

		return -lv.compute();
	}

	public int getNumArguments()
	{
		return model_.getNumParameters();
	}

	public double getLowerBound(int n)
	{
		return model_.getLowerLimit(n);
	}

	public double getUpperBound(int n)
	{
		return model_.getUpperLimit(n);
	}

	// private stuff
	private LikelihoodValue lv;
	private SubstitutionModel model_; //Cached results
}

/** Basically for cobmining model and tree likelihood optimising functions */
class CombinedLikelihood implements MultivariateFunction {
	public CombinedLikelihood(MultivariateFunction f1, MultivariateFunction f2, LikelihoodValue lv) {
		this.f1_ = f1;
		this.f2_ = f2;
		this.f1Params_ = new double[f1.getNumArguments()];
		this.f2Params_ = new double[f2.getNumArguments()];
	}
	public double evaluate(double[] params)	{
		for(int i = 0 ; i < f1Params_.length ; i++) {
			f1Params_[i] = params[i];
		}
		for(int i = 0 ; i < f2Params_.length ; i++) {
			f2Params_[i] = params[i-f1Params_.length];
		}
		return -lv.compute();
	}

	public int getNumArguments() {
		return f1Params_.length+f2Params_.length;
	}

	public double getLowerBound(int n) {
		if(n<f1Params_.length) {
			f1_.getLowerBound(n);
		}
		return f2_.getLowerBound(n-f1Params_.length);
	}

	public double getUpperBound(int n) {
		if(n<f1Params_.length) {
			f1_.getUpperBound(n);
		}
		return f2_.getUpperBound(n-f1Params_.length);
	}

	private double[] f1Params_;
	private double[] f2Params_;

	private LikelihoodValue lv;
	private MultivariateFunction f1_, f2_; //Cached results
}


class BranchLikelihood implements UnivariateFunction
{
	public BranchLikelihood(LikelihoodValue lv)
	{
		this.lv = lv;
		update();
	}
	
	public void update()
	{
		model = lv.getModel();
		
		numPatterns = lv.numPatterns;
		numRates = lv.numRates;
		numStates = lv.numStates;
		frequency = lv.frequency;
		rprob = lv.rprob;
		
		sitePattern = lv.sitePattern;	
	}
	
	public void setBranch(Node branch)
	{
		if (branch.isRoot())
		{
			throw new IllegalArgumentException();
		}
		else if (branch.isLeaf())
		{
			Node multNode = lv.getNextBranch(branch, branch.getParent());
			multPartial = lv.getPartial(multNode);
			seq = branch.getSequence();
		}
		else
		{
			Node multNode1 = lv.getNextBranch(branch, branch.getParent());
			multPartial1 = lv.getPartial(multNode1);
		
			Node multNode2 = lv.getNextBranch(branch, branch);
			multPartial2 = lv.getPartial(multNode2);		
		}
		this.branch = branch;
	}

	public double evaluate(double arc)
	{
		model.setDistance(arc);
		
		lv.logL = 0;
		for (int l = 0; l < numPatterns; l++)
		{
			double rsum = 0.0;
			for (int r = 0; r < numRates; r++)
			{
				double sum = 0;
				
				if (branch.isLeaf())
				{
					double[] mp = multPartial[l][r];
					int sl = seq[l];
				
					if (sl == numStates)
					{
						for (int d = 0; d < numStates; d++)
						{ 
							sum += frequency[d]*mp[d];
						}
					}
					else
					{
						for (int d = 0; d < numStates; d++)
						{ 
							sum += frequency[d]*mp[d]*model.transProb(r, d, sl);	
						}
					}		
				}
				else
				{
					double[] mp1 = multPartial1[l][r];
					double[] mp2 = multPartial2[l][r];
				
					for (int i = 0; i < numStates; i++)
					{ 
						double sum2 = 0.0;
						for (int j = 0; j < numStates; j++)
						{ 
							sum2 += mp2[j]*model.transProb(r, i, j);
						}
						sum += frequency[i]*mp1[i]*sum2;
					}		
				}
		
				
				sum *= rprob[r];
				rsum += sum;
			}
			
			lv.siteLogL[l] = Math.log(rsum);
			lv.logL += lv.siteLogL[l]*sitePattern.weight[l];
		}
	
		return -lv.logL;
	}
	
	public double getLowerBound()
	{
		return BranchLimits.MINARC;
	}
	
	public double getUpperBound()
	{
		return BranchLimits.MAXARC;
	}
	
	// private
	
	private LikelihoodValue lv;
	private double[][][] multPartial, multPartial1, multPartial2;
	private byte[] seq;
	private int numStates;
	private int numRates;
	private int numPatterns;
	private double[] frequency;
	private double[] rprob;
	private SitePattern sitePattern;
	private SubstitutionModel model;
	private Node branch;
}

class NodeLikelihood implements UnivariateFunction
{
	public NodeLikelihood(LikelihoodValue lv)
	{
		this.lv = lv;
		update();
	}
	
	public void update()
	{
		model = lv.getModel();
		
		numPatterns = lv.numPatterns;
		numRates = lv.numRates;
		numStates = lv.numStates;
		frequency = lv.frequency;
		rprob = lv.rprob;
		
		sitePattern = lv.sitePattern;	
	}
	
	public void setBranch(Node branch, double min, double max)
	{
		center = branch;
		
		if (center.isLeaf())
		{
			throw new IllegalArgumentException();
		}
		else if (center.isRoot())
		{
			firstBranch = center.getChild(0);
			lastBranch = center.getChild(center.getChildCount()-1);
		
			partial1 = lv.getPartial(firstBranch);
			partial2 = lv.getPartial(lastBranch);
		}
		else
		{
			partial1 = lv.getPartial(center);
			partial2 = lv.getPartial(lv.getNextBranch(center, center.getParent()));
		}
		
		minHeight = min;
		maxHeight = max;
	}

	public double evaluate(double h)
	{
		center.setNodeHeight(h);
								
		// branch lengths to child
		for (int i = 0; i < center.getChildCount(); i++)
		{
			Node child = center.getChild(i);
			child.setBranchLength(h-child.getNodeHeight());
		}
		
		// update partials of childs
		for (int i = 0; i < center.getChildCount(); i++)
		{
			Node child = center.getChild(i);
			if (child.isLeaf())
			{
				lv.partialsExternal(child);
			}
			else
			{
				lv.partialsInternal(child, child);
			}
		}

		if (center.isRoot())
		{
			lv.productPartials(lastBranch, center);
		}
		else
		{
			// multiply child partials
			lv.productPartials(center, center);
			
			center.setBranchLength(maxHeight-h);
			
			lv.partialsInternal(center, center);
		}
		
		lv.logL = 0;
		for (int l = 0; l < numPatterns; l++)
		{
			double rsum = 0.0;
			for (int r = 0; r < numRates; r++)
			{
				double[] p1 = partial1[l][r];
				double[] p2 = partial2[l][r];
				
				double sum = 0.0;
				for (int d = 0; d < numStates; d++)
				{ 
					sum += frequency[d]*p1[d]*p2[d];
				}
				sum *= rprob[r];
				rsum += sum;	
			}
			
			lv.siteLogL[l] = Math.log(rsum);
			lv.logL += lv.siteLogL[l]*sitePattern.weight[l];
		}
	
		return -lv.logL;
	}
	
	public double getLowerBound()
	{
		return minHeight;
	}
	
	public double getUpperBound()
	{
		return maxHeight;
	}
	
	// private
	
	private LikelihoodValue lv;
	private double[][][] partial1;
	private double[][][] partial2;
	private byte[] seq;
	private int numStates;
	private int numRates;
	private int numPatterns;
	private double[] frequency;
	private double[] rprob;
	private SitePattern sitePattern;
	private SubstitutionModel model;
	private double minHeight, maxHeight;
	private Node center, firstBranch, lastBranch;
}


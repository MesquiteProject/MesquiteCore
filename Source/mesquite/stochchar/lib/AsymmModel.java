/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.stochchar.lib;

/* ~~ */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;
import mesquite.stochchar.lib.*;
import Jama.*;

public class AsymmModel extends ProbPhenCategCharModel implements CModelEstimator, Evaluator {
	double param0 = MesquiteDouble.unassigned;

	double param1 = MesquiteDouble.unassigned;

	double[][] probMatrix, rateMatrix, eigenVectors, inverseEigenVectors;

	double[] eigenValues;

	boolean hasDefaultValues = true;

	MesquiteInteger pos = new MesquiteInteger(0);

	boolean recalcProbsNeeded = true;

	boolean needToPrepareMatrices = true;

	double previousBranchLength = MesquiteDouble.unassigned;

	boolean unassigned = true;

	boolean param0Unassigned = true;

	boolean param1Unassigned = true;

	private boolean useRateBiasNotation = false;

	private boolean useEquilFreqAsPrior = true;

	public AsymmModel(String name, Class dataClass) {
		super(name, dataClass);
		maxStateDefined = 1; // restriction new as of 1. 1
		maxState = 1; // restriction new as of 1. 1
		// maxStateDefined = CategoricalState.maxCategoricalState;
		// maxState = CategoricalState.maxCategoricalState;
		needToPrepareMatrices = true;
		recalcProbsNeeded = true;
		prepareMatrices();
	}

	/* --------------------------------------------- */
	public double evaluate(MesquiteDouble param, Object param2) { // used when a single parameter is being estimated
		if (param2 instanceof TreeDataModelBundle && param != null) {
			TreeDataModelBundle b = (TreeDataModelBundle) param2;
			CommandRecord cr = b.getCommandRecord();
			if (param0Unassigned) {
				if (param.getValue() < 0) // param0
					return MesquiteDouble.veryLargeNumber;
				param0 = param.getValue();
				prepareMatrices();
				double result = -b.getLikelihoodCalculator().logLikelihoodCalc(b.getTree(), this, b.getCharacterDistribution());
				if (cr != null)
					cr.tick("Estimating asymm model:  param0 " + param0 + " -logL " + result);
				return result;
			}
			else {
				if (param.getValue() <= 0) // param1
					return MesquiteDouble.veryLargeNumber;
				param1 = param.getValue();
				prepareMatrices();
				double result = -b.getLikelihoodCalculator().logLikelihoodCalc(b.getTree(), this, b.getCharacterDistribution());
				if (cr != null)
					cr.tick("Estimating asymm model:  param1 " + param1 + " -logL " + result);
				return result;
			}
		}
		else
			return 0;
	}

	/* --------------------------------------------- */
	double limit = 10000;

	int allowedEdgeHits = 0; // to stop the optimization from wandering if very high rates; edge allowed to be hit only a certain number of tiems

	int beginningAllowed = 6;

	public double evaluate(double[] x, Object param) {// used when both parameters are being estimated
		if (param instanceof TreeDataModelBundle && x != null && x.length == 2) {
			TreeDataModelBundle b = (TreeDataModelBundle) param;
			Tree t = b.getTree();
			CommandRecord cr = b.getCommandRecord();
			double height = t.tallestPathAboveNode(t.getRoot()); // to stop the optimization from wandering if very high rates
			if (x[1] * height > limit && allowedEdgeHits-- < 0) { // param1
				return MesquiteDouble.veryLargeNumber;
			}
			if (x[0] * height > limit && allowedEdgeHits-- < 0) { // param0
				return MesquiteDouble.veryLargeNumber;
			}
			if (x[1] <= 0) { // param1 //parameters shouldn't be 0 or negative unless constant, but constant filtered out before this
				return MesquiteDouble.veryLargeNumber;
			}
			if (x[0] <= 0) { // param0
				return MesquiteDouble.veryLargeNumber;
			}
			param0 = x[0];
			param1 = x[1];

			prepareMatrices();
			double result = -b.getLikelihoodCalculator().logLikelihoodCalc(b.getTree(), this, b.getCharacterDistribution());
			if (cr != null)
				cr.tick("Estimating asymm model:  params " + param0 + " " + param1 + " -logL " + result);
			return result;
		}
		else
			return 0;
	}

	/*
	 * public Object getLikelihoodSurface(Tree tree, CharacterDistribution observedStates, CLikelihoodCalculator lc, int divisions, double[] outputBounds){ //also pass upper & lower bounds //THIS DOESN:T work at present Optimizer opt = new Optimizer(this); TreeDataModelBundle bundle = new TreeDataModelBundle(tree, this, observedStates, null, lc, null); boolean rU = param0Unassigned; boolean bU = param1Unassigned; double r = param0; double b = param1; param0Unassigned = true; param1Unassigned = true; param0 = MesquiteDouble.unassigned; param1 = MesquiteDouble.unassigned; if
	 * (param0Unassigned && param1Unassigned) { double[] params = new double[2]; params[0] = 0.1; params[1]=1.0; opt.optimize(params, bundle); double[] lower = opt.getLowerBoundsSearched(); double[] upper = opt.getUpperBoundsSearched(); double lowParam0 = lower[0]; double upperParam0 = upper[0]; double lowParam1 = lower[1]; double upperParam1 = upper[1]; if (lowParam0<0) lowParam0 =0; if (lowParam1<0) lowParam1=0; double[][] surface = opt.grid(lowParam0, upperParam0, lowParam1, upperParam1, divisions, bundle); outputBounds[0]= lowParam0; //check to see if outputBounds not
	 * null & big enough! outputBounds[1]= upperParam0; outputBounds[2]= lowParam1; outputBounds[3]= upperParam1; param0Unassigned = rU; param1Unassigned = bU; param0 = r; param1 = b; return surface; } param0Unassigned = rU; param1Unassigned = bU; param0 = r; param1 = b; return null; }
	 */
	/* --------------------------------------------- */
	public void deassignParameters() {
		param0 = MesquiteDouble.unassigned;
		param1 = MesquiteDouble.unassigned;
		unassigned = true;
		param0Unassigned = true;
		param1Unassigned = true;
	}

	/* --------------------------------------------- */
	MkModel mkHelper = new MkModel("asymm helper", CategoricalState.class); // used to provide preliminary guess at rate

	// Optimization settings
	public static final int BASE_ON_MK1 = 0;

	public static final int FLIP_FLOP = 1;

	public static final int MK1_AND_FLIP_FLOP = 2; // conservative and slow

	public static int optimizationMode = MK1_AND_FLIP_FLOP;

	static final boolean scaleRescale = true;

	/* --------------------------------------------- */
	public void estimateParameters(Tree originalTree, CharacterDistribution observedStates, CLikelihoodCalculator lc) {
		boolean constant = (observedStates.isConstant(originalTree, originalTree.getRoot()));
		if (constant) {
			if (param0Unassigned && param1Unassigned) {
				if (useRateBiasNotation) {
					param0 = 0; // rate
					param1 = 1.0; // bias
				}
				else {
					param0 = 0; // backward
					param1 = 0; // forward
				}
			}
			else if (param0Unassigned) { // 0 is rate or backwards
				param0 = 0;
			}
			else { // 1 is bias or forwards
				if (useRateBiasNotation)
					param1 = 1.0; // bias
				else
					param1 = 0; // forward
			}
			prepareMatrices();
		}
		else {
			if (param0Unassigned && param1Unassigned) { // both parameters need to be estimated
				boolean wasURBN = useRateBiasNotation;
				useRateBiasNotation = false; // turn this off in this case
				Tree tree = null;
				double height = 0;
				if (scaleRescale) { // rescales tree to 1.0 height to help optimization, which takes longer with larger numbers
					MesquiteTree mTree = originalTree.cloneTree();
					mTree.setAllUnassignedBranchLengths(1.0, false);
					height = mTree.tallestPathAboveNode(mTree.getRoot(), 1.0); // to adjust start point depending on how tall is tree
					if (height != 0)
						mTree.scaleAllBranchLengths(1.0 / height, false);
					tree = mTree;
				}
				else
					tree = originalTree;
				Optimizer opt = new Optimizer(this);
				TreeDataModelBundle bundle = new TreeDataModelBundle(tree, this, observedStates, null, lc);
				double best = Double.MAX_VALUE;
				if (optimizationMode != FLIP_FLOP) {
					/* here use Mk1 to yield rate that is used as starting value for estimation */
					mkHelper.deassignParameters();
					mkHelper.estimateParameters(tree, observedStates, lc);
					double rate = mkHelper.getInstantaneousRate();

					double[] params = new double[2];
					// initialize parameters using rate estimated from mk1
					if (useRateBiasNotation) {
						params[0] = rate; // rate
						params[1] = 1.0; // bias
					}
					else {
						params[0] = rate; // backward
						params[1] = rate; // forward
					}
					allowedEdgeHits = beginningAllowed;

					best = opt.optimize(params, bundle);
					param0 = params[0];
					param1 = params[1];
				}

				if (optimizationMode != BASE_ON_MK1) {
					/*
					 * here use flipflop method, which tries three separate starting points for estimation of forward and backward rates: 1.0 - 1.0 1.0 - 0.1 0.1 - 1.0
					 */
					double[] params = new double[2];
					params[0] = 1;
					params[1] = 1;
					double b0;
					double b1;
					if (optimizationMode != MK1_AND_FLIP_FLOP) {
						b0 = params[0];
						b1 = params[1];
					}
					else {
						b0 = param0;
						b1 = param1;
					}

					double next = opt.optimize(params, bundle);

					if (!acceptableResults(next, params[0], params[1])) {
						MesquiteMessage.println("Warning: NaN encountered in AsymmModel optimization");
					}
					else if ((next < best || optimizationMode == FLIP_FLOP)) {
						best = next;
						b0 = params[0];
						b1 = params[1];

					}
					if (useRateBiasNotation) {
						params[0] = 0.316227755; // rate
						params[1] = 10; // bias
					}
					else {
						params[0] = 0.1; // backward
						params[1] = 1; // forward
					}
					param0 = params[0];
					param1 = params[1];
					allowedEdgeHits = beginningAllowed;
					next = opt.optimize(params, bundle);
					if (!acceptableResults(next, params[0], params[1])) {
						MesquiteMessage.println("Warning: NaN encountered in AsymmModel optimization");
					}
					else if (next < best) {
						best = next;
						b0 = params[0];
						b1 = params[1];
					}
					if (useRateBiasNotation) {
						params[0] = 0.316227755; // rate
						params[1] = .1; // bias
					}
					else {
						params[0] = 1; // backward
						params[1] = 0.1; // forward
					}
					param0 = params[0];
					param1 = params[1];
					allowedEdgeHits = beginningAllowed;
					next = opt.optimize(params, bundle);
					if (!acceptableResults(next, params[0], params[1])) {
						MesquiteMessage.println("Warning: NaN encountered in AsymmModel optimization");
					}
					else if (next < best) {
						best = next;
						b0 = params[0];
						b1 = params[1];
					}
					param0 = b0;
					param1 = b1;
				}

				if (scaleRescale && height != 0) {
					if (useRateBiasNotation) { // UNDO the scaling of the tree
						param0 *= 1.0 / height; // rate
					}
					else {
						param0 *= 1.0 / height; // backward
						param1 *= 1.0 / height; // forward
					}
				}
				if (wasURBN) {
					double bias = param1 / param0;
					double rate = Math.sqrt(bias) * param0;
					param0 = rate;
					param1 = bias;
					useRateBiasNotation = true;
				}
			}
			else { // only one parameter needs estimation
				Tree tree = originalTree;
				Optimizer opt = new Optimizer(this);
				TreeDataModelBundle bundle = new TreeDataModelBundle(tree, this, observedStates, null, lc);
				if (param0Unassigned) { // 0 is rate or backwards
					MesquiteDouble param = new MesquiteDouble(1.0);
					// changed from 0.993
					progressiveOptimize(tree, bundle, opt, param, 0, MkModel.optWidth);
					param0 = param.getValue();
				}
				else { // 1 is bias or forwards
					MesquiteDouble param = new MesquiteDouble(1.0);
					if (!useRateBiasNotation) { // param is forwards
						progressiveOptimize(tree, bundle, opt, param, 0, MkModel.optWidth);
					}
					else { // param is bias
						progressiveOptimize(tree, bundle, opt, param, 0, MkModel.optWidth);
					}
					param1 = param.getValue();

				}
			}
			prepareMatrices();
		}
	}

	private boolean acceptableResults(double like, double b, double c) {
		return !(Double.isNaN(like) || Double.isNaN(b) || Double.isNaN(c));
	}

	/* --------------------------------------------- */
	public double getParam0() {
		return param0;
	}

	/* --------------------------------------------- */
	public void setParam0(double r) {
		hasDefaultValues = false;
		param0 = r;
		if (MesquiteDouble.isCombinable(param0))
			param0Unassigned = false;
		prepareMatrices();
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN), CharacterModel.class, true);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN), CharacterModel.class, false);
	}

	/* --------------------------------------------- */
	public double getParam1() {
		return param1;
	}

	/* --------------------------------------------- */
	public void setParam1(double r) {
		hasDefaultValues = false;
		param1 = r;
		if (MesquiteDouble.isCombinable(param1))
			param1Unassigned = false;
		prepareMatrices();
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN), CharacterModel.class, true);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN), CharacterModel.class, false);
	}

	/* --------------------------------------------- */
	private void prepareMatrices() {
		if (maxState < 0)
			return;
		if (MesquiteDouble.isUnassigned(param0) || MesquiteDouble.isUnassigned(param1)) {
			unassigned = true;
			return;
		}
		needToPrepareMatrices = false;
		double biasRoot = Math.sqrt(param1);
		boolean changed = false;
		unassigned = false;
		if (rateMatrix == null || rateMatrix.length != maxState + 1) {
			rateMatrix = new double[maxState + 1][maxState + 1];
			changed = true;
		}
		// filling off diagonal elements
		// NOTE: this may not need to be so complex now that maxstate is restricted to 1
		for (int i = 0; i < maxState + 1; i++)
			for (int j = 0; j < maxState + 1; j++) {
				double r = MesquiteDouble.unassigned;
				if (i == j)
					;
				else {
					if (j > i) {
						if (useRateBiasNotation)
							r = MesquiteDouble.multiply(param0, biasRoot);
						else
							r = param1;
					}
					else {
						if (useRateBiasNotation)
							r = MesquiteDouble.divide(param0, biasRoot);
						else
							r = param0;
					}
					if (rateMatrix[i][j] != r) {
						rateMatrix[i][j] = r;
						changed = true;
						if (!MesquiteDouble.isCombinable(r))
							unassigned = true;
					}
				}
			}
		// adjusting the diagonal to make row sum to 0
		for (int i = 0; i < maxState + 1; i++) {
			double rowSum = 0;
			for (int j = 0; j < maxState + 1; j++)
				if (i != j)
					rowSum += rateMatrix[j][i]; // why is this j i? -- seems to be what works!
			double r = -rowSum;
			if (rateMatrix[i][i] != r) {
				rateMatrix[i][i] = r;
				changed = true;
			}
		}
		if (changed) {
			recalcProbsNeeded = true;
			if (unassigned) {
				if (eigenVectors == null)
					eigenVectors = new double[maxState + 1][maxState + 1];
				if (eigenValues == null)
					eigenValues = new double[maxState + 1];
				DoubleArray.deassignArray(eigenValues);
				Double2DArray.deassignArray(eigenVectors);
			}
			else {
				EigenAnalysis e = new EigenAnalysis(rateMatrix, true, false, true);
				eigenValues = e.getEigenvalues();
				eigenVectors = e.getEigenvectors();
				Matrix m = new Matrix(eigenVectors);
				Matrix r = m.inverse();
				inverseEigenVectors = r.getArrayCopy();
			}
		}

	}

	private double getEquilFreq0() {
		double r01, r10;
		if (!MesquiteDouble.isCombinable(param1) || !MesquiteDouble.isCombinable(param0))
			return MesquiteDouble.unassigned;

		if (useRateBiasNotation) { // had been forward/backward
			r01 = Math.sqrt(param1) * param0;
			r10 = param0 / Math.sqrt(param1);
		}
		else {
			r01 = param1;
			r10 = param0;
		}

		double f0 = r10 / (r01 + r10);
		return f0;

	}
	double[][] p, tprobMatrix;
	/* --------------------------------------------- */
	public void recalcProbabilities(double branchLength) {

		if (unassigned)
			return;
		previousBranchLength = branchLength;
		double[][] tent = new double[maxState + 1][maxState + 1];
		for (int i = 0; i < maxState + 1; i++)
			for (int j = 0; j < maxState + 1; j++) {
				if (i == j)
					tent[i][j] = Math.exp(eigenValues[i] * branchLength);
				else
					tent[i][j] = 0;
			}
		p = Double2DArray.multiply(eigenVectors, tent, p);
		tprobMatrix = Double2DArray.multiply(p, inverseEigenVectors, tprobMatrix);
		probMatrix = Double2DArray.squnch(tprobMatrix, probMatrix);
		recalcProbsNeeded = false;
	}

	/* --------------------------------------------- */
	public void setCharacterDistribution(CharacterStatesHolder cStates) {
		super.setCharacterDistribution(cStates);
		prepareMatrices();
	}

	/* --------------------------------------------- */
	public int evolveState(int beginState, Tree tree, int node) {
		double r = randomNumGen.nextDouble();
		double branchLength = tree.getBranchLength(node, 1.0);
		if (needToPrepareMatrices)
			prepareMatrices();
		if (recalcProbsNeeded || previousBranchLength != branchLength)
			recalcProbabilities(branchLength);

		double accumProb = 0;

		if (probMatrix == null)
			return -1;
		else if (beginState > probMatrix.length)
			return -1;

		for (int i = 0; i < probMatrix[beginState].length && i < maxState + 1; i++) {
			accumProb += probMatrix[beginState][i];
			if (r < accumProb)
				return i;
		}
		return maxState;
	}

	/* --------------------------------------------- */
	public double transitionProbability(int beginState, int endState, Tree tree, int node) {
		if (!inStates(beginState) || !inStates(endState)) {
			return 0;
		}
		else {
			if (tree == null)
				return 0;
			if (needToPrepareMatrices)
				prepareMatrices();
			double branchLength = tree.getBranchLength(node, 1.0);
			if (recalcProbsNeeded || previousBranchLength != branchLength)
				recalcProbabilities(branchLength);
			if (probMatrix == null)
				return 0;
			return probMatrix[beginState][endState];
		}
	}

	/* --------------------------------------------- */
	public double instantaneousRate(int beginState, int endState, Tree tree, int node) {
		if (beginState < 0 || beginState > maxState || endState < 0 || endState > maxState) {
			return 0;
		}
		else {
			if (rateMatrix == null || needToPrepareMatrices)
				prepareMatrices();
			if (rateMatrix == null)
				return 0;
			return rateMatrix[beginState][endState];
		}
	}

	public boolean priorAlwaysFlat(){
		return false;
	}
	/* --------------------------------------------- */
	public double priorProbability(int state) {
		if (!inStates(state))
			return 0;

		if (useEquilFreqAsPrior && MesquiteDouble.isCombinable(getEquilFreq0())) {

			if (state == 0)
				return getEquilFreq0();
			else if (state == 1)
				return 1 - getEquilFreq0();
			MesquiteMessage.warnProgrammer("Error: asymmMk model requested priorProb on state > 1");
			return 0;
		}
		else {
			if (maxState == 0)
				return (0.5); // flat prior
			else
				return (1.0 / (maxState + 1)); // flat prior
		}

	}

	/* --------------------------------------------- */
	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md) {
		if (md == null || !(md instanceof AsymmModel))
			return;
		super.copyToClone(md);
		AsymmModel model = (AsymmModel) md;
		model.param0 = param0;
		model.param1 = param1;
		model.param0Unassigned = param0Unassigned;
		model.param1Unassigned = param1Unassigned;
		model.recalcProbsNeeded = true;
		model.needToPrepareMatrices = true;
		model.useRateBiasNotation = useRateBiasNotation;
		model.useEquilFreqAsPrior = useEquilFreqAsPrior;
		model.prepareMatrices();
		model.notifyListeners(model, new Notification(MesquiteListener.UNKNOWN));
	}

	/* --------------------------------------------- */
	public String getExplanation() {
		return "Asymmetrical 2-parameter Markov k-state model, with two different rates of change (one for increases in state, one for decreases in state).  Can be expressed either as forward vs. backward rates, or as rate plus bias.";
	}

	/* --------------------------------------------- */
	/** Returns whether parameters of model are fully specified. If so, it can be used for evolving states. */
	public boolean isFullySpecified() {
		return !param0Unassigned && !param1Unassigned && param0 != MesquiteDouble.unassigned && param1 != MesquiteDouble.unassigned;
	}

	/* ................................................................................................................. */
	/** Performs command (for Commandable interface) */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the instantaneous rate of change in the model (if using rate/bias notation) or backward rate of change (if using forward/backward notation)", "[rate of change; must be > 0]", commandName, "setParam0")) {
			pos.setValue(0);
			String token = ParseUtil.getToken(arguments, pos);
			if (MesquiteString.explicitlyUnassigned(token)) {
				param0Unassigned = true;
				setParam0(MesquiteDouble.unassigned);
				return null;
			}
			pos.setValue(0);
			double newRate = MesquiteDouble.fromString(arguments, pos);
			double a = getParam0();
			if (newRate >= 0 && newRate != a && MesquiteDouble.isCombinable(newRate))
				setParam0(newRate);
		}
		else if (checker.compare(this.getClass(), "Sets the bias of change (gains over loses) in the model (if using rate/bias notation) or forward rate of change (if using forward/backward notation)", "[rate of change; must be > 0]", commandName, "setParam1")) {
			pos.setValue(0);
			String token = ParseUtil.getToken(arguments, pos);
			if (MesquiteString.explicitlyUnassigned(token)) {
				param1Unassigned = true;
				setParam1(MesquiteDouble.unassigned);
				return null;
			}
			pos.setValue(0);
			double newBias = MesquiteDouble.fromString(arguments, pos);
			double a = getParam1();
			if (newBias >= 0 && newBias != a && MesquiteDouble.isCombinable(newBias))
				setParam1(newBias);
		}
		else if (checker.compare(this.getClass(), "Sets whether the asymmetrical model is described in terms of a rate and a bias, or a backward and forward rate", "[on or off to indicate whether uses rate-bias]", commandName, "toggleNotation")) {
			boolean onOff = !useRateBiasNotation;
			if (!StringUtil.blank(arguments))
				onOff = MesquiteBoolean.fromOffOnString(ParseUtil.getFirstToken(arguments, pos));
			if (useRateBiasNotation != onOff) {
				setUseRateBiasNotation(onOff);
				notifyListeners(this, new Notification(MesquiteListener.UNKNOWN), null, true);
				return null;

			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the asymmetrical model uses the equilibrium frequencies as prior at the root", "[on or off to indicate whether uses equilibrium frequencies]", commandName, "toggleEquilibPrior")) {
			boolean onOff = !useEquilFreqAsPrior;
			if (!StringUtil.blank(arguments))
				onOff = MesquiteBoolean.fromOffOnString(ParseUtil.getFirstToken(arguments, pos));
			if (useEquilFreqAsPrior != onOff) {
				setUseEquilFreqAsPrior(onOff);
				notifyListeners(this, new Notification(MesquiteListener.UNKNOWN), null, true);
				return null;

			}
		}
		else if (checker.compare(this.getClass(), "Sets the maximum allowed state for this model", "[0...maxCategoricalState]", commandName, "setMaxState")) {
			if (isBuiltIn())
				return null;
			int m = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(m))
				m = MesquiteInteger.queryInteger(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Set maximum allowed state", "Maximum allowed state for this model:", maxStateDefined);
			if (m > 0 && m <= CategoricalState.maxCategoricalState && m != maxStateDefined) {
				maxState = m;
				maxStateDefined = m;
				prepareMatrices();
				notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
			}

		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/* --------------------------------------------- */
	/** returns name of model class (e.g. "stepmatrix") SHOULD MATCH NAME returned by getNEXUSNameOfModelClass of module */
	public String getNEXUSClassName() {
		return "AsymmMk";
	}

	/* --------------------------------------------- */
	public boolean getUseEquilFreqAsPrior() {
		return useEquilFreqAsPrior;
	}

	/* --------------------------------------------- */
	public void setUseEquilFreqAsPrior(boolean u) {
		useEquilFreqAsPrior = u;
	}

	/* --------------------------------------------- */
	public boolean getUseRateBiasNotation() {
		return useRateBiasNotation;
	}

	/* --------------------------------------------- */
	public void setUseRateBiasNotation(boolean u) {
		if (u != useRateBiasNotation) {
			useRateBiasNotation = u;
			if (param0Unassigned && param1Unassigned)
				return;
			else {
				if (param0Unassigned)
					param0 = 1.0;
				if (param1Unassigned)
					param1 = 1.0;
				param0Unassigned = false; // added after 1.03
				param1Unassigned = false; // added after 1.03
				if (useRateBiasNotation) { // had been forward/backward
					double bias = param1 / param0;
					double rate = Math.sqrt(bias) * param0;
					param0 = rate;
					param1 = bias;
				}
				else { // had been forward/backward
					double forward = Math.sqrt(param1) * param0;
					double backward = param0 / Math.sqrt(param1);
					param0 = backward;
					param1 = forward;
				}
				prepareMatrices();
			}
		}
	}

	/* --------------------------------------------- */
	public void fromString(String description, MesquiteInteger stringPos, int format) {
		hasDefaultValues = false;
		String s = ParseUtil.getToken(description, stringPos);
		useRateBiasNotation = !("backward".equalsIgnoreCase(s) || "forward".equalsIgnoreCase(s));
		double first = MesquiteDouble.fromString(description, stringPos);
		if ("backward".equalsIgnoreCase(s) || "rate".equalsIgnoreCase(s))
			param0 = first;
		else
			param1 = first;
		s = ParseUtil.getToken(description, stringPos);
		double second = MesquiteDouble.fromString(description, stringPos);
		if ("forward".equalsIgnoreCase(s) || "bias".equalsIgnoreCase(s))
			param1 = second;
		else
			param0 = second;

		s = ParseUtil.getToken(description, stringPos);
		if ("maxState".equalsIgnoreCase(s)) {
			int m = MesquiteInteger.fromString(description, stringPos);
			if (m != 1)
				MesquiteMessage.warnUser("WARNING: asymmMk model has maxState set to other than 1.  This is no longer permitted");
			s = ParseUtil.getToken(description, stringPos);
		}
		/*
		 * in 1. 1 no longer permit maxState as anything other than 1 if (MesquiteInteger.isCombinable(m) && "maxState".equalsIgnoreCase(s)) { maxState = m; maxStateDefined = m; } else { maxState = CategoricalState.maxCategoricalState; maxStateDefined = CategoricalState.maxCategoricalState; }
		 */
		useEquilFreqAsPrior = !("flatPrior".equalsIgnoreCase(s));

		if (MesquiteDouble.isCombinable(param0))
			param0Unassigned = false;
		if (MesquiteDouble.isCombinable(param1))
			param1Unassigned = false;
		prepareMatrices();
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN), CharacterModel.class, true);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN), CharacterModel.class, false);
	}

	/* --------------------------------------------- */
	/** Return an explanation of the settings used in calculations (e.g., optimization settings). These are not the parameters of the model. */
	public String getSettingsString() {
		if (optimizationMode == BASE_ON_MK1)
			return "(Opt.:  Mk1 base)";
		else if (optimizationMode == FLIP_FLOP)
			return "(Opt.:  Sym + 2 Asymm)";
		else
			return "(Opt.:  Mk1 + Sym + 2 Asymm)";
	}

	/* --------------------------------------------- */
	private String paramToString(double param, boolean unassigned) {
		if (unassigned) {
			if (MesquiteDouble.isCombinable(param))
				return MesquiteDouble.toString(param) + " [est.]";
			else
				return " ? ";
		}
		else
			return MesquiteDouble.toString(param);
	}

	/* --------------------------------------------- */
	public String toString() {
		return "AsymmMk model (id " + getID() + ") " + getParameters();
	}

	/* --------------------------------------------- */
	public String paramString() {
		String priorString;
		if (useEquilFreqAsPrior)
			priorString = " equilibAsPrior";
		else
			priorString = " flatPrior";
		if (useRateBiasNotation)
			return "rate " + paramToString(param0, param0Unassigned) + " bias " + paramToString(param1, param1Unassigned) + priorString;
		else
			return "forward " + paramToString(param1, param1Unassigned) + " backward " + paramToString(param0, param0Unassigned) + priorString;
	}

	/* --------------------------------------------- */
	public String getNexusSpecification() {
		String s = " ";
		if (!StringUtil.blank(getAnnotation()))
			s += " [!" + getAnnotation() + "] ";
		s += paramString();
		/*
		 * as of 1. 1 no longer pfermits maxState other than 1 if (maxStateDefined != CategoricalState.maxCategoricalState && maxStateDefined>=0) s += " maxState " + maxStateDefined;
		 */
		return s;
	}

	/* --------------------------------------------- */
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone) {
		AsymmModel j = new AsymmModel(name, getStateClass());
		j.recalcProbsNeeded = true;
		j.needToPrepareMatrices = true;
		completeDaughterClone(formerClone, j);
		j.prepareMatrices();
		return j;

	}
	MesquiteNumber[] forExport;
	/* --------------------------------------------- */
	public MesquiteNumber[] getParameterValues() {
		if (forExport == null){
			forExport = new MesquiteNumber[2];
			forExport[0] = new MesquiteNumber();
			forExport[1] = new MesquiteNumber();
		}

		if (useRateBiasNotation) {
			forExport[0].setName("bias");
			forExport[0].setValue(param1);
			forExport[1].setName("rate");
			forExport[1].setValue(param0);
		}
		else {
			forExport[0].setName("forward");
			forExport[0].setValue(param1);
			forExport[1].setName("backward");
			forExport[1].setValue(param0);
		}
		return forExport;
	}
	/* --------------------------------------------- */
	public String getParameters() {
		String priorString;
		if (useEquilFreqAsPrior)
			priorString = " root: equilibrium";
		else
			priorString = " root: flat prior";
		if (useRateBiasNotation)
			return "bias: " + param1 + " rate: " + param0 + priorString;
		else
			return "forward rate: " + param1 + " backward rate: " + param0 + priorString;
	}

	public String getModelTypeName() {
		return "Asymm. 2-par. Markov";
	}
}


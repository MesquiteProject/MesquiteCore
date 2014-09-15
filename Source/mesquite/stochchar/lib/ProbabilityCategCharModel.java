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

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
/** A character model for Categorical characters to be used in stochastic simulations and in likelihood calculations.
It must serve both for calculating probabilities (via the transitionProbability method) and for simulating character evolution
(via the evolveState method).*/
public abstract class ProbabilityCategCharModel  extends ProbabilityModel {
	protected long allStates = 0L;
	protected int maxState = -1;  //maximum state to be used
	protected int maxStateDefined = -1; //maximum state for which this model is defined
	//protected int numStates = 0;
	protected Random randomNumGen;
	
	public ProbabilityCategCharModel (String name, Class dataClass) {
		super(name, dataClass);
 		randomNumGen = new Random();
	}
	/** Returns natural log of the transition probability from beginning to ending state in the given tree on the given node*/
	public double transitionLnProbability (int beginState, int endState, Tree tree, int node){
		return Math.log(transitionProbability(beginState, endState, tree, node));
	}
    /** Returns instanteous rate from beginning to ending state in given tree on the given node */
	// Broken until issues with this method in ProbabilityDNAModel are resolved PEM 6-Jan-2006
    // public abstract double instantaneousRate(int beginState, int endState, Tree tree, int node);
	/** Returns transition probability from beginning to ending state in the given tree on the given node*/
	public abstract double transitionProbability (int beginState, int endState, Tree tree, int node);
	/** Randomly generates a character state according to model, on branch on tree, from beginning state*/
	public void evolveState (CharacterState beginState, CharacterState endState, Tree tree, int node){
		if (endState==null) {
			return;
		}
		if (beginState==null || !(beginState instanceof CategoricalState) || !(endState instanceof CategoricalState)){
			endState.setToUnassigned();
			return;
		}
		CategoricalState bState = (CategoricalState)beginState;
		CategoricalState eState = (CategoricalState)endState;
		int r = evolveState(CategoricalState.minimum(bState.getValue()), tree, node); //todo: issue error if beginning is polymorphic?  choose randomly among beginning?
		eState.setValue(CategoricalState.makeSet(r));
	}
	/** Randomly generates a character state according to model, on branch on tree, from beginning state*/
	public abstract int evolveState (int beginState, Tree tree, int node);
	
	/** Returns instantaneous rate of change from beginState to endState along branch in tree
	NOT READY YET
	public abstract double instantaneousRate (int beginState, int endState, Tree tree, int node);
	*/
	
	/** Randomly generates according to model an ancestral state for root of tree*/
	public CharacterState getRootState (CharacterState state, Tree tree){
		if (state==null || !(state instanceof CategoricalState))
			state = new CategoricalState();
		((CategoricalState)state).setValue(getRootState(tree));
		return state;
	}
	/** Reports current parameters*/
	public abstract String getParameters ();
	/** Reports current parameters*/
	public abstract MesquiteNumber[] getParameterValues ();

	/** Randomly generates according to model an ancestral state for root of tree*/
	public long getRootState (Tree tree){
		if (maxState <= 0) 
			return (1L);  //if only state 0 allowed, return it
		else {
			double r = randomNumGen.nextDouble();
			double probEach =1.0/(maxState+1);
			double accumProb = 0;
			for (int i=0; i<maxState; i++) {
				accumProb +=  priorProbability(i);
				if (r< accumProb)
					return CategoricalState.makeSet(i);
			}
			return CategoricalState.makeSet(maxState);
		}
	}
	public boolean priorAlwaysFlat(){
		return true;
	}
	public double priorProbability (int state){
		if (!inStates(state)) 
			return 0;
		if (maxState == 0)
			return (0.5);  // flat prior
		else
			return (1.0/(maxState+1));  // flat prior
		
	}
	
	/* ---------------------------------------------*/
	public boolean inStates(int state) { //todo: these should be part of standard categ models
		if (state>maxState || state<0)
			return false;
		else
			return (CategoricalState.isElement(allStates, state));
	}
	/* ---------------------------------------------*/
	public void setMaxStateDefined(int m) {  
			maxState = m;
			maxStateDefined = m;
			allStates = CategoricalState.span(0,maxState);
	}
	/* ---------------------------------------------*/
	public void setMaxStateSimulatable(int m) {  
			maxState = m;
			if (m>maxStateDefined)
				maxState = maxStateDefined;
			allStates = CategoricalState.span(0,maxState);
	}
	/* ---------------------------------------------*/
	public void setCharacterDistribution(CharacterStatesHolder cStates) { 
		super.setCharacterDistribution(cStates);
	//This expansion costs a lot of time.  Perhaps use allStates based calculations instead of states[] based calculations?
		if (DNAState.class == cStates.getStateClass() || DNAState.class.isAssignableFrom(cStates.getStateClass())) {
			maxState = 3;
			if (maxState>maxStateDefined)
				maxState = maxStateDefined;
			allStates = CategoricalState.span(0,maxState);
		}
		else {
			allStates = ((CategoricalDistribution)cStates).getAllStates();
			maxState = CategoricalState.maximum(allStates);
			if (maxState>maxStateDefined) {
				maxState = maxStateDefined;
				allStates = allStates & CategoricalState.span(0,maxState);
			}
			else if (maxState < 1) {
				allStates = CategoricalState.span(0,1);
				maxState = 1;
			}
		}
	}
	/* ---------------------------------------------*/
	public int getMaxStateDefined() {
		return maxStateDefined;
	}
	/* ---------------------------------------------*/
	public int getMaxState() {
		return maxState;
	}
	/* ---------------------------------------------*/
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md){
		if (md == null)
			return;
		super.copyToClone(md);
		ProbabilityCategCharModel model = (ProbabilityCategCharModel)md;
		model.allStates = allStates;
		model.maxState = maxState;
		model.maxStateDefined = maxStateDefined;
		if (model.allStates == 0L)
			model.allStates = CategoricalState.span(0,maxState);
	}
	/* ---------------------------------------------*/
	public void setSeed(long seed){
		randomNumGen.setSeed(seed);
	}
	
	/* ---------------------------------------------*/
	public long getSeed(){
		return randomNumGen.nextLong();
	}
	
	protected double progressiveOptimize(Tree tree, TreeDataModelBundle bundle, Optimizer opt, MesquiteDouble param, double min, double optWidth){
		double max = optWidth/tree.tallestPathAboveNode(tree.getRoot(), 1.0);
		if (max <= min)
			max = min + 0.1;
 		param.setValue((max-min)/2);
 		double best = opt.optimize(param, min, max, bundle);
 		double mult = optWidth;
 		if (optWidth < 2)
 			mult = 2;
 		int count = 1;
		if (param.getValue()< min*1.00001){ //parameter very close to minimum; try narrowing the range to see if get better answer
			while (param.getValue()< min*1.00001 && min> 0.00000001 && count<10) { 
				double oldMin = min;
				min = min/mult; 
	 			best = opt.optimize(param,min, (oldMin - min)*0.5 + min, bundle);
	 			count++;
	 		}
		}
		else { 
			double oldBest = 0;
			
			//check if parameter is near maximum; if near enough then expand range and try again.  Keep trying as long as -ln likelihood improves a bit
			while (param.getValue() > max*0.999 && max<100000 && count<10 && (oldBest == 0 || (best<oldBest && Math.abs(best/oldBest)<0.999))) { //trying again in case seems to be bumping up against max
				double oldMax = max;
				max = max*mult; 
				oldBest = best;
	 			best = opt.optimize(param, (oldMax-min)*0.8 + min, max, bundle);
	 			count++;
	 		}
 		}
 		return best;
	}
	
}


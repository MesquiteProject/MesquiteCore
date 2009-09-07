/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.71, September 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.correl.lib;

import java.util.Random;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAState;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.Optimizer;
import mesquite.lib.Tree;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.CharacterStatesHolder;
import mesquite.stochchar.lib.TreeDataModelBundle;

/* ======================================================================== */
/** A character model for multiple Categorical characters to be used in stochastic simulations and in 
likelihood calculations. It must serve both for calculating probabilities (via the transitionProbability 
method) and for simulating character evolution
(via the evolveState method).*/
public abstract class MultipleProbCategCharModel extends MultipleProbabilityModel {
	protected long[] allStates;
	protected int[] maxState;  //maximum state to be used (on a per char basis??)
	protected int[] maxStateDefined; //maximum state for which this model is defined
	//protected int numStates = 0;
	protected Random randomNumGen;
	protected int numChars;       //
	
	public MultipleProbCategCharModel (String name, Class dataClass, int dim) {
		super(name, dataClass);
 		randomNumGen = new Random();
 		numChars = dim;
 		allStates = new long[dim];
 		maxState = new int[dim];
 		maxStateDefined = new int[dim];
 		for (int i=0;i<dim;i++)
 			allStates[i]=0L; 
 		for (int i=0;i<dim;i++)
 			maxState[i]=-1;
 		for (int i=0;i<dim;i++)
 			maxStateDefined[i]=-1;
	}

	/** Returns natural log of the transition probability from beginning to ending state in the given tree on the given node*/
	public double transitionLnProbability (int[] beginState, int[] endState, Tree tree, int node){
		return Math.log(transitionProbability(beginState, endState, tree, node));
	}

    /** Returns instanteous rate from beginning to ending state in given tree on the given node */
	// Currently not supporting MultipleProbDNAModel, so this is supported PEM 22-Mar-2006
	/** Returns transition probability from beginning to ending state in the given tree on the given node*/
	public abstract double transitionProbability (int beginState[], int endState[], Tree tree, int node);
	/** Randomly generates a character state according to model, on branch on tree, from beginning state*/
	public void evolveState (CharacterState[] beginState, CharacterState[] endState, Tree tree, int node){
		if (endState==null) {
			return;
		}
		if (beginState==null || !(beginState instanceof CategoricalState[]) || !(endState instanceof CategoricalState[])){
			for (int i=0;i<endState.length;i++)
				endState[i].setToUnassigned();  //set all to unassigned?
			return;
		}
		CategoricalState bState[] = (CategoricalState[])beginState;
		CategoricalState eState[] = (CategoricalState[])endState;
		int [] catStates = new int[bState.length];
		for(int i=0;i<bState.length;i++)
			catStates[i]=CategoricalState.minimum(bState[i].getValue());
		int[] r = evolveState(catStates, tree, node); //todo: issue error if beginning is polymorphic?  choose randomly among beginning?
		for(int i=0;i<r.length;i++)
			eState[i].setValue(CategoricalState.makeSet(r[i]));
	}
	
	/** Randomly generates a character state according to model, on branch on tree, from beginning state*/
	public abstract int[] evolveState (int beginState[], Tree tree, int node);
	
	/** Returns instantaneous rate of change from beginState to endState along branch in tree
	NOT READY YET
	public abstract double instantaneousRate (int beginState, int endState, Tree tree, int node);
	*/
	
	/** Randomly generates according to model an ancestral state for root of tree*/
	public CharacterState[] getRootState (CharacterState[] state, Tree tree){
		if (state==null || !(state instanceof CategoricalState[]))
			state = new CategoricalState[numChars];      //TODO handle this better - this should never happen
		for(int i=0;i<state.length;i++)
			((CategoricalState)state[i]).setValue(getRootState(tree,i));
		return state;
	}
	/** Reports current parameters*/
	public abstract String getParameters ();

	/** Randomly generates according to model an ancestral state for root of tree
	 * @param tree
	 * @return
	 */
	public long getRootState (Tree tree,int c){
		if (maxState[c] <= 0) 
			return (1L);  //if only state 0 allowed, return it
		else {
			double r = randomNumGen.nextDouble();
			double probEach =1.0/(maxState[c]+1);
			double accumProb = 0;
			for (int i=0; i<maxState[c]; i++) {
				accumProb +=  priorProbability(i,c);
				if (r< accumProb)
					return CategoricalState.makeSet(i);
			}
			return CategoricalState.makeSet(maxState[c]);
		}
	}
	public double priorProbability (int state,int c){
		if (!inStates(state,c)) 
			return 0;
		/* if (state == 0)
			return 1;
		else 
			return 0;
		/* */
		
		if (maxState[c] == 0)
			return (0.5);  // flat prior
		else
			return (1.0/(maxState[c]+1));  // flat prior
		
	}
	public boolean priorAlwaysFlat(){
		return true;
	}
	
	/* ---------------------------------------------*/
	public boolean inStates(int state,int c) { //todo: these should be part of standard categ models
		if (state>maxState[c] || state<0)
			return false;
		else
			return (CategoricalState.isElement(allStates[c], state));
	}
	/* ---------------------------------------------*/
	public void setMaxStateDefined(int m,int c) {  
			maxState[c] = m;
			maxStateDefined[c] = m;
			allStates[c] = CategoricalState.span(0,maxState[c]);
	}
	/* ---------------------------------------------*/
	public void setMaxStateSimulatable(int m,int c) {  
			maxState[c] = m;
			if (m>maxStateDefined[c])
				maxState[c] = maxStateDefined[c];
			allStates[c] = CategoricalState.span(0,maxState[c]);
	}
	/* ---------------------------------------------*/
	public void setCharacterDistribution(CharacterStatesHolder[] cStates) { 
		super.setCharacterDistribution(cStates);
		numChars= cStates.length;
		for (int c=0;c<numChars;c++){
			//This expansion costs a lot of time.  Perhaps use allStates based calculations instead of states[] based calculations?
			if (DNAState.class == cStates[c].getStateClass() || DNAState.class.isAssignableFrom(cStates[c].getStateClass())) {
				maxState[c] = 3;
				if (maxState[c]>maxStateDefined[c])
					maxState[c] = maxStateDefined[c];
				allStates[c] = CategoricalState.span(0,maxState[c]);
			}
			else {
				allStates[c] = ((CategoricalDistribution)cStates[c]).getAllStates();
				maxState[c] = CategoricalState.maximum(allStates[c]);
				if (maxState[c]>maxStateDefined[c]) {
					maxState[c] = maxStateDefined[c];
					allStates[c] = allStates[c] & CategoricalState.span(0,maxState[c]);
				}
				else if (maxState[c] < 1) {
					allStates[c] = CategoricalState.span(0,1);
					maxState[c] = 1;
				}
			}
		}
	}
	/* ---------------------------------------------*/
	public int getMaxStateDefined(int c) {
		return maxStateDefined[c];
	}
	/* ---------------------------------------------*/
	public int getMaxState(int c) {
		return maxState[c];
	}
	/* ---------------------------------------------*/
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md){
		if (md == null)
			return;
		super.copyToClone(md);
		MultipleProbCategCharModel model = (MultipleProbCategCharModel)md;
		model.allStates = allStates;
		model.maxState = maxState;
		model.maxStateDefined = maxStateDefined;
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

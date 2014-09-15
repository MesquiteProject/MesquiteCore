/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/** A class that provides root states for probabilistic models, using frequencies for the root states. 
This class must be subclassed, with the method setStateProbabilities specified. */
/* ======================================================================== */
public abstract class RateMatrixCatModel  extends ProbSubModel{
	long allStates = 0L;
	int[] availableStates;
	int numStates;
	CharRatesModel charRatesModel;
	private double[][] fullRateMatrix = null;
	
	/*These probability matrices are numStates X numStates X numNodes, so as to maintain a separate
	probability matrix at each node of the tree.  This is done to help minimize recalculations of probability matrices,
	allowing a separate matrix to be stored at each node. (Note: where there is rate variation, this will help but be of less importance, since
	other archiving systems like that in GTR model will become more important. */
	private double[][][] changeProbabilities = null;
	private double[][][] cumChangeProbabilities = null;
	//private double[] rateParameters = null;
	protected Random randomNumber;
	

	public RateMatrixCatModel (CompositProbCategModel probabilityModel, int numStates, Class stateClass) {
		super (null, stateClass);
		this.numStates = numStates;
 		fullRateMatrix = new double[numStates][numStates];
 		changeProbabilities = new double[numStates][numStates][1];
 		cumChangeProbabilities = new double[numStates][numStates][1];
 		randomNumber = new Random();
 		initAvailableStates();
 		zeroChangeProbabilities();
 		this.probabilityModel = probabilityModel;
		
 		//setChangeProbabilities(branchlength);
 	}
 	/*.................................................................................................................*/
	public void initialize() {
	}
 	/*.................................................................................................................*/
	public void taxaSet() {
	}
 	public String getExplanation(){
 		return "Rate matrix model";
 	}
 	public String getParadigm(){
 		return "CharRateMatrices";
 	}
	/** Returns nexus command introducing this model.*/
//	public String getNEXUSCommand() {
//		return "CharRateMatrix";
//	}
	/*.................................................................................................................*/
	public abstract void initAvailableStates ();
 	/*.................................................................................................................*/
	//public abstract CharacterModel cloneModelWithMotherLink(CharacterModel formerClone);
  	/*.................................................................................................................*/
	/** Sets the equilibrium change frequencies in the model. Must be specified by the subclass. */
	protected abstract void setChangeProbabilities (double branchlength, Tree tree, int node); 
	/*.................................................................................................................*/
	public double getChangeProbability (int stateFrom, int stateTo, int node){
		if (stateFrom<changeProbabilities.length && stateTo < changeProbabilities[stateFrom].length && changeProbabilities[stateFrom][stateTo]!=null && node < changeProbabilities[stateFrom][stateTo].length)
			return changeProbabilities[stateFrom][stateTo][node];
		else
			return 0.0;
	}
	/*.................................................................................................................*/
	public void setCharRatesModel (CharRatesModel charRatesModel){
		this.charRatesModel = charRatesModel;
	}
	/*.................................................................................................................*/
	public CharRatesModel getCharRatesModel (){
		return charRatesModel;
	}
	/*.................................................................................................................*/
	public double getStateFreq (int state, Tree tree, int node){
		return probabilityModel.getEquilStatesModel().getStateFreq(state, tree, node);
	}
	/*.................................................................................................................*/
	public int getNumStates (){
		return numStates;
	}
	/*.................................................................................................................*/
	/** Sets the state frequencies to zero.*/
	public void zeroChangeProbabilities (){
		for (int i=0; i<numStates; i++) 
			for (int j=0; j<numStates; j++) 
				for (int node =0; node<changeProbabilities[i][j].length; node++)
					changeProbabilities[i][j][node] = 0.0;
	}
	/*.................................................................................................................*/
	public void setChangeProbability (int stateFrom, int stateTo, int node, double freq){
		if (stateFrom<changeProbabilities.length && stateTo < changeProbabilities[stateFrom].length && changeProbabilities[stateFrom][stateTo]!=null && node < changeProbabilities[stateFrom][stateTo].length){
			changeProbabilities[stateFrom][stateTo][node] = freq;
			calcCumChangeProbability(node);
		}
	}
	/*.................................................................................................................*/
	/** Calculates the cumulative change frequencies.*/
	public void calcCumChangeProbability (int node){
		for (int i=0; i<numStates; i++)  {
			double accumProb = 0;
			for (int j=0; j<numStates; j++) {
				accumProb +=  changeProbabilities[i][j][node];
				cumChangeProbabilities[i][j][node] = accumProb;
			}
		}
	}
	
	/*.................................................................................................................*/
//	public abstract double[][] getProbabilityMatrix (double branchLength);


	/** Checks to see that the node-by-node storage of change probabilities is big enough for tree;
	if not, makes newer bigger ones and invalidates probabilities to require them to be calculated again */
	private void checkMatrixSizes(Tree tree){
		if (changeProbabilities[0][0].length<tree.getNumNodeSpaces()) {
	 		changeProbabilities = new double[numStates][numStates][tree.getNumNodeSpaces()];
	 		cumChangeProbabilities = new double[numStates][numStates][tree.getNumNodeSpaces()];
	 		invalidateProbabilitiesAtNodes();
		}
			
	}
	public abstract void invalidateProbabilitiesAtNodes();
 	/*.................................................................................................................*/
	public int evolveState (int beginState, Tree tree, int node) {
		double r = randomNumber.nextDouble();
		checkMatrixSizes(tree);
		setChangeProbabilities(tree.getBranchLength(node, 1.0)*probabilityModel.getScalingFactor()*probabilityModel.getCharRatesModel().getRate(), tree, node);
		checkProbabilitySums(node);

		double accumProb = 0.0;
		
		for (int i=0; i<availableStates.length; i++) {
			accumProb += changeProbabilities[beginState][i][node];
			if (r< accumProb) {
				return availableStates[i];
			}
		}
		return availableStates[availableStates.length-1];

	}
	private void checkProbabilitySums(int node){
	
		double accumProb = 0.0;
		for (int i=0; i<availableStates.length; i++) {
			for (int j=0; j<availableStates.length;j++) {
				accumProb += changeProbabilities[i][j][node];
			}
		}
		double deviation = Math.abs(accumProb - availableStates.length);
		if (deviation > 0.01)
			MesquiteMessage.warnProgrammer("probability matrix rows don't add up to 1 " + deviation + " (model : " + getName() + ")");
	}
 	/*.................................................................................................................*/
	public double transitionProbability (int beginState, int endState, Tree tree, int node){
		if (!inStates(beginState) || !inStates(endState)) {
			return 0;
		}
		else {
			checkMatrixSizes(tree);
			setChangeProbabilities(tree.getBranchLength(node, 1.0)*probabilityModel.getCharRatesModel().getRate(),  tree,  node);
			checkProbabilitySums(node);
			if (beginState == endState) {
				if (availableStates.length == 1)
					return 1.0;
				else
					return changeProbabilities[beginState][endState][node];
			}
			else {
				return changeProbabilities[beginState][endState][node];
			}
		}
	}
	/*.................................................................................................................*/
	/** Given a random number between 0 and 1, returns the state value.*/
	public long getRandomState (int stateFrom, double randomDouble, int node){
		if (availableStates.length == 1)
			return (allStates);  
		else {
			for (int j=0; j<numStates; j++)
				if (randomDouble< cumChangeProbabilities[stateFrom][j][node])
					return CategoricalState.makeSet(availableStates[j]);
			return CategoricalState.makeSet(numStates-1);
		}
	}
	/*.................................................................................................................*/
	/** Randomly generates according to model a state.*/
	public long getRandomState (int stateFrom, int node){
		double r = randomNumber.nextDouble();
		return getRandomState(stateFrom, r, node);
	}
 	/*.................................................................................................................*/
	public boolean inStates(int state) { //todo: these should be part of standard categ models
		if (availableStates==null)
			return false;
		else {
			for (int i=0; i<availableStates.length; i++)
				if (state == availableStates[i])
					return true;
			return false;
		}
	}
 	/*.................................................................................................................*/
	public void setSeed(long seed){
		randomNumber.setSeed(seed);
	}
 	/*.................................................................................................................*/
	public long getSeed(){
		return randomNumber.nextLong();
	}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "";
	}
	public String getModelTypeName(){
		return "Rate matrix model";
	}
}


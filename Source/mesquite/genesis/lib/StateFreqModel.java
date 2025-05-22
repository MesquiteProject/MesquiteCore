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
import mesquite.lib.tree.Tree;
import mesquite.categ.lib.*;

/** A class that provides root states for probabilistic models, using frequencies for the root states. 
This class must be subclassed, with the method setStateFrequencies specified. */
/* ======================================================================== */
public abstract class StateFreqModel extends ProbSubModel {
	long allStates = 0L;
	int[] availableStates;
	int numStates = 0;
	private double[] stateFrequencies = null;
	private double[] cumStateFreq = null;
	protected Random randomNumber;
	boolean checkForZero=false;
	boolean nodeVariable=false;
	

	public StateFreqModel (CompositProbCategModel probabilityModel, int numStates) {
		super(null, CategoricalState.class);
		this.numStates = numStates;
 		stateFrequencies = new double[numStates];
 		cumStateFreq = new double[numStates];
 		randomNumber = new Random();
 		this.probabilityModel = probabilityModel;
 		initAvailableStates();
 		zeroStateFrequencies();
 		setStateFrequencies();
 	}
 	/*.................................................................................................................*/
 	public String getExplanation(){
 		return "Model of state frequencies";
 	}
 	public String getParadigm(){
 		return "StateFreqModel";
 	}
	/** Returns nexus command introducing this model.*/
/*	public String getNEXUSCommand() {
		return "StateFreqModel";
	}
 	/*.................................................................................................................*/
	public void initialize() {
	}
 	/*.................................................................................................................*/
	public void taxaSet() {
	}
	/*.................................................................................................................*/
	public abstract void initAvailableStates ();
	/*.................................................................................................................*/
	public int getNumStates (){
		return stateFrequencies.length;
	}
	/*.................................................................................................................*/
	public void setNumStates (int numstates){
		this.numStates = numstates;
	}
	/*.................................................................................................................*/
	/** Ask if the state frequencies are all zero.*/
	public boolean stateFrequenciesAllZero (){
		double freqSum = 0;
		for (int i=0; i<stateFrequencies.length; i++) 
			freqSum += stateFrequencies[i];
		checkForZero = true;
		return (freqSum<0.5);
	}
	/*.................................................................................................................*/
	/** Sets the state frequencies to zero.*/
	public void zeroStateFrequencies (){
		for (int i=0; i<stateFrequencies.length; i++) 
			stateFrequencies[i] = 0.0;
	}
  	/*.................................................................................................................*/
	/** Sets the equilibrium state frequencies in the model. Must be specified by the subclass. */
	public abstract void setStateFrequencies ();
  	/*.................................................................................................................*/
	/** Returns the state frequencies in the model.  */
	public double[]  getStateFrequencies () {
		return stateFrequencies;
	}
  	/*.................................................................................................................*/
	/** Sets the equilibrium state frequencies in the model; used to reset them if things have changed. */
	public void resetStateFrequencies () {
		setStateFrequencies();
	}
	/*.................................................................................................................*/
	public void setStateFreq (int state, double freq){
		stateFrequencies[state] = freq;
		calcCumStateFreq();
	}
	/*.................................................................................................................*/
	public double getStateFreq (int state){
		return stateFrequencies[state];
	}
	/*.................................................................................................................*/
	/** Calculates the cumulative state frequencies.*/
	public void calcCumStateFreq (){
		double accumProb = 0;
		for (int i=0; i<stateFrequencies.length; i++) {
			accumProb +=  stateFrequencies[i];
			cumStateFreq[i] = accumProb;
		}
	}
	
	/*.................................................................................................................*/
	public boolean checkValidityStateFreq (){
		if (!checkForZero && stateFrequenciesAllZero()) {
			if (stateFrequenciesAllZero()) {
				checkForZero = false;
				return false;
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	public double getStateFreq (int state, Tree tree, int node){
		if (!checkValidityStateFreq())
			return 0.0;
		else
			return stateFrequencies[state];
	}
	/*.................................................................................................................*/
	public double getCumulativeStateFreq (int state, Tree tree, int node){
		if (!checkValidityStateFreq())
			return 0.0;
		else
			return cumStateFreq[state];
	}
	/*.................................................................................................................*/
	/** Given a random number between 0 and 1, returns the state value.*/
	public long getState (double randomDouble, Tree tree, int node){
		if (availableStates.length == 1)
			return (allStates);  
		else {
			if (!checkValidityStateFreq())
				return CategoricalState.makeSet(availableStates.length-1);
			for (int i=0; i<availableStates.length; i++)
				if (randomDouble< getCumulativeStateFreq(i, tree, node))
					return CategoricalState.makeSet(availableStates[i]);
			return CategoricalState.makeSet(availableStates.length-1);
				
		}
	}
	/*.................................................................................................................*/
	/** Randomly generates according to model a state.*/
	public long getState (Tree tree, int node){
		double r = randomNumber.nextDouble();
		return getState(r, tree, node);
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
		return "State frequencies model";
	}
}


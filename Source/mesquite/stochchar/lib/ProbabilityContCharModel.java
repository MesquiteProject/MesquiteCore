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
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;
/*==========================  Mesquite Basic Class Library ==========================*/
/*===  the basic classes used by the kernel of Mesquite (i.e. the trunk object) and available to the modules

/* ======================================================================== */
/** A character model for continuous characters to be used in stochastic simulations and in likelihood calculations.
Needs to include other methods, such as ones dealing with pdf for transitions*/
public abstract class ProbabilityContCharModel  extends ProbabilityModel {
	public ProbabilityContCharModel (String name, Class dataClass) {
		super(name, dataClass);
	}
	/** Randomly generates according to model an end state on branch from beginning states*/
	public abstract double evolveState (double beginState, Tree tree, int node);
	
	public void evolveState (CharacterState beginState, CharacterState endState, Tree tree, int node){
		if (beginState==null || endState==null || !(beginState instanceof ContinuousState) || !(endState instanceof ContinuousState))
			return;
		ContinuousState bState = (ContinuousState)beginState;
		ContinuousState eState = (ContinuousState)endState;
		double b = bState.getValue(0);
		eState.setValue(0,evolveState(b, tree, node));
	}
	/** Randomly generates according to model an ancestral state for root of tree*/
	public abstract double getRootState (Tree tree);
	
	/** Randomly generates according to model an ancestral state for root of tree*/
	public CharacterState getRootState (CharacterState state, Tree tree){
		if (state==null || !(state instanceof ContinuousState))
			state = new ContinuousState();
		((ContinuousState)state).setValue(0,getRootState(tree));
		return state;
	}
	
}


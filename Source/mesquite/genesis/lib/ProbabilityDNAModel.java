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
/*~~  */

import mesquite.stochchar.lib.CategProbModelCurator;

public abstract class ProbabilityDNAModel extends CompositProbCategModel {
	
 	/*.................................................................................................................*/
	public ProbabilityDNAModel (String name, Class dataClass, CategProbModelCurator curator){
		super(name, dataClass, curator);
		maxStateDefined = 3;
		maxState = 3;
 	}
	
 	/*.................................................................................................................*
	public void initAvailableStates() {
		availableStates = new int[4];
		for (int i=0; i<4; i++)
			availableStates[i]=i;
	}
 	/*.................................................................................................................*
	public void setStates(long allStates, CharacterDistribution cStates) { 
	//This expansion costs a lot of time.  Perhaps use allStates based calculations instead of states[] based calculations?
		if (!(DNAState.class.isAssignableFrom(cStates.getStateClass()))) {
			this.availableStates = CategoricalState.expand(allStates);
			if (availableStates.length <= 1) {
				int state = availableStates[0];
				availableStates = new int[2];
				availableStates[0] = state;
				if (availableStates[0]==1)
					availableStates[1] = 0;
				else
					availableStates[1]=1;
			}
		}
		else 
			initAvailableStates();
		allStates = CategoricalState.compressFromList(availableStates);
	}*/
}



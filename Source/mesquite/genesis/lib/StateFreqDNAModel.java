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

/** A class that provides root states for probabilistic models, using frequencies for the root states. 
This class must be subclassed, with the method setStateFrequencies specified. */
/* ======================================================================== */
public abstract class StateFreqDNAModel extends StateFreqModel {

	public StateFreqDNAModel (CompositProbCategModel probabilityModel) {
 		super(probabilityModel, 4);
 	}
 	/*.................................................................................................................*/
	public void initAvailableStates() {
		availableStates = new int[4];
		for (int i=0; i<4; i++)
			availableStates[i]=i;
	}

}


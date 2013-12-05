/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
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
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**Assesses likelihood for a character using likelihood, for a specific character model.*/

public abstract class MargLikelihoodForModel extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return MargLikelihoodForModel.class;
   	 }
 	public String getDutyName() {
 		return "Maximum Likelihood using Character Model";
   	 }

	public  abstract void calculateLogProbability(Tree tree, CharacterDistribution charStates, CharacterModel model, MesquiteString resultString,  MesquiteNumber prob);
	
	//not yet working: public  abstract Object getLikelihoodSurface(Tree tree, CharacterDistribution charStates, CharacterModel model, double[] inputBounds, double[] outputBounds);

	public  abstract void estimateParameters(Tree tree, CharacterDistribution charStates, CharacterModel model, MesquiteString resultString);

	public boolean compatibleWithContext(CharacterModel model, CharacterDistribution observedStates) {
		return false;
	}

}




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

import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.characters.CharacterHistory;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.tree.Tree;


/* ======================================================================== */
/**Reconstructs the ancestral states for a character using stochastic model (joint reconstruction), for a specific character model.
Also returns posterior probability.

CURRENTLY NOT extended by any active modules*/

public abstract class JointLikeAncStForModel extends MesquiteModule  {
	public int oldNumTaxa = 0;

   	 public Class getDutyClass() {
   	 	return JointLikeAncStForModel.class;
   	 }
 	public String getDutyName() {
 		return "Joint likelihood ancestral state reconsturction";
   	 }

	public abstract void calculateStates(Tree tree, CharacterDistribution charStates, CharacterHistory statesAtNodes, CharacterModel model, MesquiteString resultString, MesquiteNumber prob);
	
	public  abstract void calculateLogProbability(Tree tree, CharacterDistribution charStates, CharacterModel model, MesquiteString resultString, MesquiteNumber prob) ;
	public boolean compatibleWithContext(CharacterModel model, CharacterDistribution observedStates) {
		return false;
	}

}




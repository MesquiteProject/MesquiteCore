/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**Assigns states to nodes of tree for a character.
Example modules:  ParsAncestralStates, MaxPosteriorJoint, MLEAncestralStates.
In 1. 1 this was moved to be subclass of CharMapper.  Employers using as before would call calculateStates and it should behave as before.  Employers using as CharMapper
have that one's abstract methods filled in to use the calcu*/

public abstract class CharStatesForNodes extends CharMapper  {
   	 public Class getDutyClass() {
   	 	return CharStatesForNodes.class;
   	 }
 	public String getDutyName() {
 		return "Assign states of character to nodes";
   	 }
   	 public String[] getDefaultModule() {
 		return new String[] {"#ParsAncestralStates"};
   	 }
   	public  void prepareForMappings( boolean permissiveOfNoSeparateMappings) {
  	}
	
	public abstract boolean calculateStates(Tree tree, CharacterDistribution charStates, CharacterHistory resultStates, MesquiteString resultString); 
	/*.................................................................................................................*/
	protected Tree tree;
	protected CharacterDistribution observedStates;
	public  void setObservedStates(Tree tree, CharacterDistribution observedStates){
			this.tree = tree;
			this.observedStates = observedStates;
			
	}
	/*.................................................................................................................*/
 	
  	public  boolean getMapping(long i, CharacterHistory resultStates, MesquiteString resultString){
  		if (tree == null || observedStates == null)
  			return false;
  		return calculateStates(tree, observedStates, resultStates, resultString);
  	}

	public  long getNumberOfMappings(){
		return 1;
	}
	public String getMappingTypeName(){
		return "Mapping";
	}

}




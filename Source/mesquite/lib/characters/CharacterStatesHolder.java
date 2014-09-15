/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import java.awt.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/*Last documented:  April 2003 */
/* ======================================================================== */
/**Stores the character states at a series of terminal taxa or nodes in a tree.  The class on which both
CharacterDistribution and CharacterHistory are based.  See general discussion of character storage classes under CharacterState*/
public interface CharacterStatesHolder extends Listable, Renamable, WithStringDetails {  
	/*.................................................................................................................*/
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass ();
	/*.................................................................................................................*/
	/**returns the corresponding CharacterState subclass*/
	public Class getStateClass ();
	/*.................................................................................................................*/
	/** passed the file, finds the default character model.  This is not done well yet.  Should be passed the paradigm.  Is passed the
	so the CharacterModel object can be looked up*/
	public CharacterModel getDefaultModel(MesquiteProject proj, String paradigm);
	/*.................................................................................................................*/
	/** Returns the Taxa to which this applies */ 
	public Taxa getTaxa();
	/*.................................................................................................................*/
	/** This is the same as getNumNodes.  This may be unexpected, because it does NOT necessarily give the same results as getTaxa().getNumTaxa().
	For instance, if this is a CharacterHistory, then this is the same as number of nodes, not the number of terminal taxa.  The reason for this unusual usage is that
	*/
	public int getNumTaxa();
	/*.................................................................................................................*/
	/** returns number of nodes for which CharacterStates is defined 
	(either terminal taxa, if CharacterDistribution, or all nodes in tree, if CharacterHistory) */
	public int getNumNodes();
	/*.................................................................................................................*/
	/** returns parent data of this CharacterStates.  There is not a corresponding set procedure because the parent data
	is set either by the constructor (for Embedded distributions) or by a set procedure (for History or Adjustables) */
	public CharacterData getParentData ();
	/*.................................................................................................................*/
	/** returns parent character of this CharacterStates*/
	public int getParentCharacter ();
	/*.................................................................................................................*/
	/** return whether states at node N is in some way greater than that at node M*/
	public boolean firstIsGreater (int N, int M);
	/*.................................................................................................................*/
	/** get blank CharacterState object*/
	public CharacterState getCharacterState ();
	/*.................................................................................................................*/
	/** get CharacterState at node N*/
	public CharacterState getCharacterState (CharacterState cs, int N);
	/*.................................................................................................................*/
   	/** returns whether the character is inapplicable at node/taxon N*/
   	public boolean isInapplicable(int N);
	/*.................................................................................................................*/
   	/** returns whether the state of character is missing in node/taxon N*/
   	public boolean isUnassigned(int N);
	/*.................................................................................................................*/
   	/** returns whether the state of character has uncertainty in node/taxon N*/
   	public boolean isUncertain(int N);
	/*.................................................................................................................*/
	/** output to log a list of the states.  for debugging purposes*/
	public void logStates();
	/*.................................................................................................................*/
	/** get string describing character states at terminal taxon or node.*/
	public String toString (int taxon, String lineEnding);
	
	public String toStringWithDetails();
	/*.................................................................................................................*/
	/** returns whether states at nodes n and m are equal*/
	public boolean statesEqual(int n, int m);
	
}


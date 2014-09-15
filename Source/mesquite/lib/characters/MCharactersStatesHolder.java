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
/**Stores the character states for multiple characters at a series of terminal taxa or nodes in a tree.  The class on which both
MCharactersDistribution and MCharactersHistory are based.
  See general discussion of character storage classes under CharacterState*/
public interface MCharactersStatesHolder extends Listable, Renamable   {
	/*.................................................................................................................*/
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass ();
	/*.................................................................................................................*/
	/**returns the corresponding CharacterState subclass*/
	public Class getStateClass ();

	/**Returns the type of data stored. */
	public String getDataTypeName();
	
	/** Returns the Taxa to which this applies */ 
	public Taxa getTaxa();
	/** Returns the parent CharacterData */ 
	public CharacterData getParentData();
	/** get CharacterState of character ic at node or taxon it*/
	public CharacterState getCharacterState (CharacterState cs, int ic, int it);

	/** Returns whether character ic is currently included */ 
	public boolean isCurrentlyIncluded(int ic);
	
	public int getNumChars();
	/*.................................................................................................................*/
	/** returns number of terminal taxa for which MCharactersStates is defined*/
	public int getNumTaxa();
	/*.................................................................................................................*/
	/** returns number of nodes for which MCharactersStates is defined 
	(either terminal taxa, if MCharactersDistribution, or all nodes in tree, if MCharactersHistory) */
	public int getNumNodes();
}


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
package mesquite.lib.characters; 

import java.awt.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/* ======================================================================== */
/**An interface to signal that a CharacterDistribution can be changed (i.e. its states set, its parent data set, the number
of taxa readjusted, and so on.  Classes that implement this should override setParentData and setParentCharacter to
avoid their use, as AdjustableDistributions should not be dependent on a parent CharacterData.  
Some CharacterDistributions, such as those that report characters from stored matrices in
a CharacterData object, are not adjustable.
  See general discussion of character storage classes under CharacterState*/
public interface AdjustableDistribution extends CharacterStatesHolder  {
	/*.........................................AdjustableDistribution.................................................*/
	/** Adjusts the size of this to store states for each of the terminal taxa.*/
	public AdjustableDistribution adjustSize(Taxa taxa);
	/*.........................................AdjustableDistribution.................................................*/
	/**set all states to missing (unassigned)*/
	public void deassignStates();
	/**Trade states of nodes it and it2 */
	public void tradeStatesBetweenTaxa(int it, int it2);
	
	/*.................................................................................................................*/
	/** sets CharacterState at node/taxon it*/
	public void setCharacterState (int it, CharacterState cs);
	
	/**sets the parent data from which this CharacterDistribution is derived or related*/
	public void setParentData (CharacterData cd);
	/*.................................................................................................................*/
	/**sets the parent character number from which this CharacterDistribution is derived or related*/
	public void setParentCharacter (int ic);
}



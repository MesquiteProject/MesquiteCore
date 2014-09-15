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
/**An interface to signal that a MCharactersDistribution can be changed (i.e. its states set, its parent data set, the number
of taxa readjusted, and so on.  Some MCharactersDistributions, such as those that report characters from stored matrices in
a CharacterData object, are not adjustable.  See general discussion of character storage classes under CharacterState*/
public interface MAdjustableDistribution extends MCharactersDistribution, Annotatable  {
	/** Set the taxa to which matrix applies */ 
	public void setTaxa(Taxa taxa);
	/** Set the size of the matrix */ 
	public void setSize(int numChars, int numTaxa);
	/**sets the parent CharacterData from which this CharacterDistribution is derived or related*/
	public void setParentData (CharacterData cd);
	
	/**sets the character state of character ic and taxon it to that in the passed CharacterState*/
   	public void setCharacterState(CharacterState s, int ic, int it);
   	
   	/** trades the states of character ic between taxa it and it2.  Used for reshuffling.*/
	public void tradeStatesBetweenTaxa(int ic, int it, int it2);
	/** assign missing data (unassigned) to all of the characters*/
	public void deassignStates();
	/** obtain the states of character ic from the given CharacterDistribution*/
	public void transferFrom(int ic, CharacterDistribution s);
	
}


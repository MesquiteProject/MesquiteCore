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
/**Stores states at each of the terminal taxa, for each of many characters.  See general discussion of character storage classes under CharacterState*/
public interface MCharactersDistribution extends MCharactersStatesHolder  {
	/**return CharacterDistribution object for character ic */
	public CharacterDistribution getCharacterDistribution (int ic);  //TODO: pass existing to save instantiations

	/**return blank adjustable MCharactersDistribution if this same type */
	public MAdjustableDistribution makeBlankAdjustable();

	/**return CharacterData filled with same values as this matrix */
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa);
	
	/** Adjusts the size of the passed MCharacterHistory to be of the same type as this MCharactersDistribution and to be prepared
	to store states for each of the nodes of the tree.*/
	public MCharactersHistory adjustHistorySize(Tree tree, MCharactersHistory charStates);
	/*------------*/


	/**get the tree on which this was simulated or otherwise based, if any */
	public Tree getBasisTree();
	/**sets the tree on which this was simulated or otherwise based, if any */
	public void setBasisTree(Tree tree);
}


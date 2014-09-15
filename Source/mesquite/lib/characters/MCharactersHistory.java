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

/*Last documented:  April 2003 */
/* ======================================================================== */
/**Stores the reconstructed or simulated states at each of the nodes of a tree, for each of many characters.
  See general discussion of character storage classes under CharacterState*/
public interface MCharactersHistory extends MAdjustableDistribution  {
	/**return CharacterHistory object for character ic */
	public abstract CharacterHistory getCharacterHistory (int ic);//TODO: pass existing to save instantiations
	/*------------*/
	/** obtain the states of character ic from the given CharacterDistribution*/
	public abstract void transferFrom(int ic, CharacterHistory s);
}


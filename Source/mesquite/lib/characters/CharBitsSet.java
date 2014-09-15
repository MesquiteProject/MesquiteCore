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

/* ======================================================================== */
/** A specset with bit information about characters.   Called "character bits set" because it amounts to a selection (bit on = yes, bit off  = no)
for a whole list of characters.  It is abstract because it doesn't define cloneSpecsSet  */
public abstract class CharBitsSet extends BitsSpecsSet implements CharSpecsSet { //should extend a generic BitsSpecsSet
	CharacterData data;
	public CharBitsSet(String name, int numChars, CharacterData data){
		super(name, numChars);
		this.data = data;
	}
	public String getTypeName(){
		return "Character selection set";
	}
 	/*.................................................................................................................*/
	public CharacterData getCharacterData(){
		return data;
	}
}



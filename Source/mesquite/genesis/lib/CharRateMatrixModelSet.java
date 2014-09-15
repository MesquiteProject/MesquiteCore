/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;

/*======================================================================== */
/** a ModelSet for probability models. */
public class CharRateMatrixModelSet  extends ModelSet {
	
	public CharRateMatrixModelSet (String name, int numChars, CharacterModel defaultModel, CharacterData data) {
		super(name, numChars, defaultModel, data);
	}
	public String getTypeName(){
		return "Character Rate Matrix Model set";
	}
	public SpecsSet cloneSpecsSet(){
		CharRateMatrixModelSet ms = new CharRateMatrixModelSet(new String(name), getNumberOfParts(), (CharacterModel)getDefaultProperty(), data);
		for (int i=0; i<getNumberOfParts(); i++) {
			ms.setModel(getModel(i), i);
		}
		return ms;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof CharacterData))
			return null;
		return new CharRateMatrixModelSet("Rate Matrix Model Set", numParts, (CharacterModel)getDefaultProperty(), (CharacterData)parent);
	}
}


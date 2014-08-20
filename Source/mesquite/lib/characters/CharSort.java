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

/*======================================================================== */
/** a sorting for characters. */
public class CharSort  extends CharNumSet {
	
	public CharSort (String name, int numChars, CharacterData data) {
		super(name, numChars, new MesquiteNumber(), data);
	}
	public String getTypeName(){
		return "Character Order";
	}
	public SpecsSet cloneSpecsSet(){
		CharSort ms = new CharSort(new String(name), getNumberOfParts(),  data);
		MesquiteNumber order = new MesquiteNumber();
		for (int i=0; i< getNumberOfParts(); i++) {
			placeValue(i, order);
			ms.setValue(i, order);
		}
		return ms;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof CharacterData))
			return null;
		return new CharSort("Character Order", numParts, (CharacterData)parent);
	}
}



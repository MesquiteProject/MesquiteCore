/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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
/** a weight set for characters. */
public class CharWeightSet  extends CharNumSet {
	
	public CharWeightSet (String name, int numChars, CharacterData data) {
		super(name, numChars, new MesquiteNumber(1), data);
		setDefaultValue(new MesquiteNumber(1));
			for (int i = 0; i<numChars; i++)
 				setValue(i, 1);
	}
	public String getTypeName(){
		return "Character Weight set";
	}
	
	public SpecsSet cloneSpecsSet(){
		CharWeightSet ms = new CharWeightSet(new String(name), getNumberOfParts(),  data);
		MesquiteNumber weight = new MesquiteNumber();
		for (int i=0; i< getNumberOfParts(); i++) {
			placeValue(i, weight);
			ms.setValue(i, weight);
		}
		return ms;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof CharacterData))
			return null;
		return new CharWeightSet("Weight Set", numParts, (CharacterData)parent);
	}
}



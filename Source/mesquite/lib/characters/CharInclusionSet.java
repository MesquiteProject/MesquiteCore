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
/** A character inclusion set.   */
public class CharInclusionSet extends CharBitsSet { 
	public CharInclusionSet(String name, int numChars, CharacterData data){
		super(name, numChars, data);
	}
	public String getTypeName(){
		return "Character Inclusion set";
	}
	public SpecsSet cloneSpecsSet(){
		CharInclusionSet bss = new CharInclusionSet("cloned", getNumberOfParts(), data);
		for (int i=0; i< getNumberOfParts(); i++) {
			bss.setSelected(i, isBitOn(i));
		}
		return bss;
	}
	public SpecsSet makeSpecsSet(AssociableWithSpecs parent, int numParts){
		if (!(parent instanceof CharacterData))
			return null;
		CharInclusionSet cis = new CharInclusionSet("Inclusion Set", numParts, (CharacterData)parent);
		cis.selectAll();
		return cis;
	}
	
	/*.................................................................................................................*/
	/** Add num parts just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num){  
		boolean success = super.addParts(starting, num); 
		if (success) {
			for (int i=starting+1; i< starting+num+1; i++) //default is for the bit to be set
				setSelected(i, true);
		}
		return success;
	}

}


